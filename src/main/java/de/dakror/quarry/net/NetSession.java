package de.dakror.quarry.net;

import com.badlogic.gdx.Gdx;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Generator;
import de.dakror.quarry.scenes.Game;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 极简 TCP 会话（阶段 1 · M1b 传输 + M1c 锁步命令）。
 *
 * 线格式：每帧 = [int 长度][byte 类型][正文]
 *   1 HELLO      正文 = int 协议版本, int build, long 种子
 *   2 HELLO_OK   同上（主机回）
 *   3 HELLO_FAIL 正文 = UTF-8 原因
 *   4 WORLD      正文 = long tick + 整份世界（NBT + LZ4）
 *   5 CMD        正文 = long 目标 tick + byte 条数 + 若干条 NetCommand
 *   6 HASH       正文 = long tick + long simHash（漂移检测）
 *   7 RESYNC     正文 = 空（客户端发现漂移，请主机重发世界）
 *
 * 锁步模型（输入延迟）：
 * - 玩家操作先入 pendingLocal；每个 tick 结束时打包，标记为「tick + INPUT_DELAY 执行」，
 *   同时存进本地队列并发给对端 —— 两端拿到的包内容完全一致。
 * - 推进到 tick T 之前必须已收到对端的 T 包（T < startTick + INPUT_DELAY 时豁免，因为对端那时还不存在）。
 *   这样两端在同一 tick 应用同一批命令；对端落后超过 INPUT_DELAY 时本地停下等（不跳 tick）。
 * - 命令应用顺序：主机先、客户端后（两端一致），与谁先按下无关。
 *
 * 线程模型：读线程阻塞收包、写线程从队列发帧；渲染线程只入队，绝不阻塞 I/O。
 * 触碰模拟状态的动作（序列化/应用世界、算哈希）都通过 Gdx.app.postRunnable 回到渲染线程。
 */
public class NetSession {
   // 线格式变更（例如命令条数字段从 byte 改 short）时必须 +1，否则新旧构建混用会静默解析错位
   public static final int PROTOCOL = 3;
   public static final int DEFAULT_PORT = 25565;
   public static final int INPUT_DELAY = 8;
   private static final int HASH_INTERVAL = 60;
   private static final byte MSG_HELLO = 1;
   private static final byte MSG_HELLO_OK = 2;
   private static final byte MSG_HELLO_FAIL = 3;
   private static final byte MSG_WORLD = 4;
   private static final byte MSG_CMD = 5;
   private static final byte MSG_HASH = 6;
   private static final byte MSG_RESYNC = 7;
   private static final int MAX_FRAME = 268435456;
   private static final int PRUNE_KEEP = 128;

   private static NetSession active;
   private static String lastStatus = "未连接";

   private final boolean isHost;
   private final String target;
   private final int port;
   private final ConcurrentLinkedQueue<byte[]> outbox = new ConcurrentLinkedQueue<>();
   private final ConcurrentLinkedQueue<NetCommand> pendingLocal = new ConcurrentLinkedQueue<>();
   private final ConcurrentHashMap<Long, List<NetCommand>> localByTick = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, List<NetCommand>> remoteByTick = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, Long> localHashes = new ConcurrentHashMap<>();
   private final ConcurrentHashMap<Long, Long> remoteHashes = new ConcurrentHashMap<>();
   private volatile boolean running = true;
   private volatile String status = "";
   private volatile Socket socket;
   private volatile DataOutputStream out;
   /** 客户端从哪一 tick 开始跟随主机；host 接受连接后、client 收到世界后设置 */
   private volatile long startTick = -1L;
   private volatile long lastDivergedTick = -1L;
   private volatile long lastRemoteCmdTick = -1L;
   private volatile long nextWaitLog = 0L;
   private volatile boolean waitToastShown = false;

   private NetSession(boolean isHost, String target, int port) {
      this.isHost = isHost;
      this.target = target;
      this.port = port;
   }

   // ---------- 静态门面（全部在渲染线程调用） ----------

   public static boolean isActive() {
      return active != null;
   }

   public static boolean isHost() {
      return active != null && active.isHost;
   }

   public static String getStatus() {
      return active == null ? lastStatus : active.status;
   }

   /** 本地玩家操作入队，等 tick 边界统一生效。 */
   public static void issue(NetCommand command) {
      if (active != null) {
         active.pendingLocal.add(command);
      }
   }

   /** 推进到 tick 之前：对端的命令包到了吗？ */
   public static boolean canAdvance(long tick) {
      return active == null || active.canAdvanceInternal(tick);
   }

   /** 本 tick 要执行的命令（主机在前、客户端在后），两端顺序一致。 */
   public static List<NetCommand> commandsFor(long tick) {
      return active == null ? null : active.commandsForInternal(tick);
   }

   /** 每个 tick 结束时调用：打包本 tick 产生的命令，标记到 tick + INPUT_DELAY。 */
   public static void onTickEnd(long tick) {
      if (active != null) {
         active.onTickEndInternal(tick);
      }
   }

   /** 每 hashInterval() 个 tick 调用一次（哈希由渲染线程算好传进来）。 */
   public static void sendHash(long tick, long hash) {
      if (active != null) {
         active.sendHashInternal(tick, hash);
      }
   }

   public static int hashInterval() {
      return HASH_INTERVAL;
   }

   /** 解析世界包正文里的 tick / NBT 部分。 */
   public static long worldTickOf(byte[] body) {
      try {
         return new DataInputStream(new ByteArrayInputStream(body)).readLong();
      } catch (IOException var2) {
         return 0L;
      }
   }

   public static byte[] worldPayloadOf(byte[] body) {
      byte[] nbt = new byte[body.length - 8];
      System.arraycopy(body, 8, nbt, 0, nbt.length);
      return nbt;
   }

   /** 锁步卡住时每秒打一条，说明在等哪个 tick、对端最近到哪了。 */
   public static void noteWaiting(long tick) {
      NetSession s = active;
      if (s == null) {
         return;
      }

      if (!s.waitToastShown) {
         s.waitToastShown = true;
         s.setStatus("等待对端…（对方窗口可能未激活/最小化，游戏在失焦时会停止模拟）");
      }

      if (System.currentTimeMillis() >= s.nextWaitLog) {
         s.nextWaitLog = System.currentTimeMillis() + 1000L;
         System.out.println(
            "[Net] 等待对端命令包 tick="
               + tick
               + "（对端最近 tick="
               + s.lastRemoteCmdTick
               + "，本地 startTick="
               + s.startTick
               + "，本机是"
               + (s.isHost ? "主机" : "客户端")
               + "）"
         );
      }
   }

   /** 由 Game 在应用完世界后调用，标记起点 tick。 */
   public static void markStartTick(long tick) {
      NetSession s = active;
      if (s != null) {
         s.startTick = tick;
         // 只丢本地待发命令；**不能清 remoteByTick/localByTick**：
         // 对端不会重发已经发过的包，清掉就永远补不回来（重发世界后会直接卡死）。
         // pendingLocal 是加入前本机旧局里攒下的操作，丢掉避免污染共享世界。
         s.pendingLocal.clear();
      }
   }

   // ---------- 连接管理 ----------

   /** 开主机：监听端口，等一个客户端连进来，握手后把整份世界发给它。 */
   public static void host(int port) {
      if (refuseIfActive("开主机")) {
         return;
      }

      active = new NetSession(true, null, port);
      active.start();
   }

   /** 加入：spec 支持 "ip" 或 "ip:port"，省略端口时用 DEFAULT_PORT。 */
   public static void join(String spec) {
      if (refuseIfActive("加入")) {
         return;
      }

      String host = spec == null ? "" : spec.trim();
      int p = DEFAULT_PORT;
      int i = host.indexOf(':');
      if (i > 0) {
         try {
            p = Integer.parseInt(host.substring(i + 1).trim());
         } catch (NumberFormatException var5) {
            p = DEFAULT_PORT;
         }

         host = host.substring(0, i).trim();
      }

      if (host.isEmpty()) {
         host = "127.0.0.1";
      }

      active = new NetSession(false, host, p);
      active.start();
   }

   /** 已有会话时不静默顶掉旧会话（否则同一个窗口里按 F9 会把刚开的 F8 主机关掉）。 */
   private static boolean refuseIfActive(String what) {
      if (active != null) {
         lastStatus = "已有联机会话（" + "请先按 F7 断开" + "），忽略本次" + what;
         System.out.println("[Net] " + lastStatus);

         try {
            Game.G.ui.toast.show(lastStatus);
         } catch (Exception var2) {
         }

         return true;
      }

      return false;
   }

   /** 断开当前会话。 */
   public static void disconnect() {
      NetSession s = active;
      if (s == null) {
         lastStatus = "当前没有联机会话";
         System.out.println("[Net] " + lastStatus);
      } else {
         s.setStatus("已断开");
      }

      close();
   }

   /** 列出本机可用于局域网联机的 IPv4 地址，方便对端填 IP。 */
   public static List<String> localAddresses() {
      List<String> list = new ArrayList<>();

      try {
         for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (ni.isUp() && !ni.isLoopback()) {
               for (InetAddress ia : Collections.list(ni.getInetAddresses())) {
                  if (ia instanceof Inet4Address) {
                     list.add(ia.getHostAddress());
                  }
               }
            }
         }
      } catch (Exception var6) {
         Quarry.Q.pi.message(1, var6);
      }

      return list;
   }

   public static void close() {
      NetSession s = active;
      active = null;
      if (s != null) {
         s.running = false;
         s.outbox.clear();

         try {
            if (s.socket != null) {
               s.socket.close();
            }
         } catch (IOException var2) {
         }
      }
   }

   private void start() {
      Thread reader = new Thread(new Runnable() {
         @Override
         public void run() {
            if (NetSession.this.isHost) {
               NetSession.this.runHost();
            } else {
               NetSession.this.runClient();
            }
         }
      }, this.isHost ? "net-host" : "net-client");
      reader.setDaemon(true);
      Thread writer = new Thread(new Runnable() {
         @Override
         public void run() {
            NetSession.this.runWriter();
         }
      }, "net-writer");
      writer.setDaemon(true);
      reader.start();
      writer.start();
   }

   // ---------- 主机 ----------

   private void runHost() {
      try {
         ServerSocket server;
         try {
            server = new ServerSocket(this.port);
         } catch (IOException var10) {
            this.setStatus("无法监听 :" + this.port + "（端口被占用？）：" + var10.getMessage());
            return;
         }

         List<String> ips = localAddresses();
         this.setStatus("监听 :" + this.port + (ips.isEmpty() ? "" : "  局域网 IP: " + ips + "（对端填这个）"));
         Socket s = server.accept();
         server.close();
         s.setTcpNoDelay(true);
         this.socket = s;
         this.out = new DataOutputStream(s.getOutputStream());
         DataInputStream in = new DataInputStream(s.getInputStream());
         byte[] hello = this.readFrame(in);
         if (hello == null || hello[0] != MSG_HELLO) {
            this.fail("握手失败：首个包不是 HELLO");
            return;
         }

         DataInputStream hb = new DataInputStream(new ByteArrayInputStream(hello, 1, hello.length - 1));
         int protocol = hb.readInt();
         int build = hb.readInt();
         long seed = hb.readLong();
         if (protocol != PROTOCOL) {
            this.fail("协议版本不一致：本机 " + PROTOCOL + "，对方 " + protocol);
            return;
         }

         if (build != Quarry.Q.versionNumber) {
            this.fail("游戏版本不一致：本机 build " + Quarry.Q.versionNumber + "，对方 " + build);
            return;
         }

         this.sendHandshake(MSG_HELLO_OK, seed);
         this.setStatus("已接受 " + s.getInetAddress().getHostAddress() + "，正在发送世界");
         this.sendWorld();
         this.readLoop(in);
      } catch (Exception var3) {
         if (this.running) {
            this.setStatus("主机出错：" + var3);
            Quarry.Q.pi.message(1, var3);
         }

         close();
      }
   }

   // ---------- 客户端 ----------

   private void runClient() {
      try {
         this.setStatus("连接 " + this.target + ":" + this.port + " …");
         Socket s = new Socket();
         s.connect(new InetSocketAddress(this.target, this.port), 5000);
         s.setTcpNoDelay(true);
         this.socket = s;
         this.out = new DataOutputStream(s.getOutputStream());
         DataInputStream in = new DataInputStream(s.getInputStream());
         this.sendHandshake(MSG_HELLO, Generator.G.getSeed());
         byte[] reply = this.readFrame(in);
         if (reply == null || reply[0] == MSG_HELLO_FAIL) {
            String why = reply == null ? "对方断开" : new String(reply, 1, reply.length - 1, "UTF-8");
            this.setStatus("无法加入：" + why);
            close();
            return;
         }

         this.setStatus("握手成功，等待世界…");
         this.readLoop(in);
      } catch (Exception var4) {
         if (this.running) {
            this.setStatus("连接失败 " + this.target + ":" + this.port + " —— " + var4 + "（主机是否已按 F8 开好？）");
            Quarry.Q.pi.message(1, var4);
         }

         close();
      }
   }

   // ---------- 收包 ----------

   private void readLoop(DataInputStream in) throws IOException {
      while (this.running) {
         byte[] frame = this.readFrame(in);
         if (frame == null) {
            break;
         }

         DataInputStream body = new DataInputStream(new ByteArrayInputStream(frame, 1, frame.length - 1));
         switch (frame[0]) {
            case MSG_WORLD: {
               final long tick = body.readLong();
               final boolean paused = body.readByte() == 1;
               final int speed = body.readInt();
               final byte[] nbt = new byte[frame.length - 14];
               body.readFully(nbt);
               // 关键：立刻在收包线程上标记已锚定。Game 侧应用世界是异步的（postRunnable），
               // 若等到那时才标记，世界到达后、应用完成前到达的命令包会被当成"未锚定的旧包"丢掉，
               // 而对端不会重发 → 对端推进完豁免窗口就死锁（表现为静止、无法放置）。
               this.startTick = tick;
               // 注意：这里**不能**解析 NBT（NBT 是静态单例，渲染线程同时在用会互相踩，读出来是垃圾）。
               // 哈希与解析都放到下面的 render 线程 runnable 里做。
               this.setStatus("收到世界（" + nbt.length + " 字节, tick=" + tick + "），正在载入");
               Gdx.app.postRunnable(new Runnable() {
                  @Override
                  public void run() {
                     try {
                        System.out.println("[Net] 收到世界 worldHash=" + Long.toHexString(Game.G.worldBytesHash(nbt)));
                        Game.G.applyWorldBytes(nbt, tick, paused, speed);
                        NetSession.this.setStatus("已载入主机世界，从 tick " + tick + " 开始");
                     } catch (Exception var2) {
                        Quarry.Q.pi.message(1, var2);
                        NetSession.this.setStatus("载入世界失败：" + var2);
                     }
                  }
               });
               break;
            }
            case MSG_CMD: {
               if (this.startTick < 0L) {
                  // 还没锚定（主机=还没发出世界，客户端=还没收到世界）：这时收到的包是对方旧局的编号，直接丢
                  break;
               }

               long tick = body.readLong();
               int n = body.readShort();
               List<NetCommand> list = new ArrayList<>(n);

               for (int i = 0; i < n; i++) {
                  list.add(NetCommand.read(body));
               }

               this.remoteByTick.put(tick, list);
               this.lastRemoteCmdTick = tick;
               break;
            }
            case MSG_HASH: {
               if (this.startTick < 0L) {
                  break;
               }

               long tick = body.readLong();
               long hash = body.readLong();
               this.remoteHashes.put(tick, hash);
               Long mine = this.localHashes.get(tick);
               if (mine == null) {
                  System.out.println("[Net] tick=" + tick + " 对端 hash=" + Long.toHexString(hash) + "（本地还没算到）");
               } else {
                  boolean same = mine.longValue() == hash;
                  System.out.println(
                     "[Net] tick=" + tick + " local=" + Long.toHexString(mine) + " peer=" + Long.toHexString(hash) + (same ? " OK" : " MISMATCH")
                  );
                  if (!same) {
                     this.onDiverged(tick, mine, hash);
                  }
               }
               break;
            }
            case MSG_RESYNC:
               if (this.isHost) {
                  this.setStatus("客户端报告漂移，重发世界");
                  this.sendWorld();
               }
               break;
            default:
               break;
         }
      }

      if (this.running) {
         this.setStatus("对端断开，已退出联机（回到单机模拟）");
      }

      // 关键：会话必须清掉。否则 canAdvance 会永远等一个不再存在的对端，模拟永久冻结；
      // 而且后面想重新开/加入也会被 refuseIfActive 挡住。
      close();
   }

   private void runWriter() {
      try {
         while (this.running) {
            byte[] frame = this.outbox.poll();
            DataOutputStream o = this.out;
            if (frame == null || o == null) {
               Thread.sleep(5L);
            } else {
               o.writeInt(frame.length);
               o.write(frame);
               o.flush();
            }
         }
      } catch (Exception var3) {
         if (this.running) {
            this.setStatus("发送失败：" + var3);
         }
      }
   }

   // ---------- 锁步 ----------

   private boolean canAdvanceInternal(long tick) {
      long from = this.startTick;
      // 注意是 <= ：包编号 = 发出方 simTick + INPUT_DELAY，而对齐后双方都会先执行 tick=startTick，
      // 于是对端能给出的第一个包号是 startTick + INPUT_DELAY + 1。多含一格才能和它对上，
      // 否则会永远等一个永远不会发出的包号（差一）。
      return from < 0L || tick <= from + INPUT_DELAY || this.remoteByTick.containsKey(tick);
   }

   private List<NetCommand> commandsForInternal(long tick) {
      List<NetCommand> mine = this.localByTick.get(tick);
      List<NetCommand> theirs = this.remoteByTick.get(tick);
      if ((mine == null || mine.isEmpty()) && (theirs == null || theirs.isEmpty())) {
         return null;
      }

      List<NetCommand> merged = new ArrayList<>();
      if (this.isHost) {
         if (mine != null) {
            merged.addAll(mine);
         }

         if (theirs != null) {
            merged.addAll(theirs);
         }
      } else {
         if (theirs != null) {
            merged.addAll(theirs);
         }

         if (mine != null) {
            merged.addAll(mine);
         }
      }

      return merged;
   }

   private void onTickEndInternal(long tick) {
      if (!this.isHost && this.startTick < 0L) {
         // 客户端在收到世界之前不要发：否则是用自己旧局的 tick 编号发，会被主机当成对端结果
         return;
      }

      this.waitToastShown = false;
      long target = tick + INPUT_DELAY;
      List<NetCommand> list = new ArrayList<>();
      NetCommand c;
      while ((c = this.pendingLocal.poll()) != null) {
         list.add(c);
      }

      if (!list.isEmpty()) {
         this.localByTick.put(target, list);
      }

      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(baos);

      try {
         d.writeLong(target);
         d.writeShort(list.size());

         for (NetCommand cmd : list) {
            cmd.write(d);
         }

         d.flush();
      } catch (IOException var9) {
         return;
      }

      this.sendFrame(MSG_CMD, baos.toByteArray());
      if (tick % 64L == 0L) {
         this.prune(tick);
      }
   }

   private void prune(long tick) {
      long keepFrom = tick - PRUNE_KEEP;

      for (Entry<Long, List<NetCommand>> e : this.localByTick.entrySet()) {
         if (e.getKey() < keepFrom) {
            this.localByTick.remove(e.getKey());
         }
      }

      for (Entry<Long, List<NetCommand>> e : this.remoteByTick.entrySet()) {
         if (e.getKey() < keepFrom) {
            this.remoteByTick.remove(e.getKey());
         }
      }

      for (Entry<Long, Long> e : this.localHashes.entrySet()) {
         if (e.getKey() < keepFrom) {
            this.localHashes.remove(e.getKey());
         }
      }
   }

   private void sendHashInternal(long tick, long hash) {
      if (!this.isHost && this.startTick < 0L) {
         return;
      }

      this.localHashes.put(tick, hash);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(baos);

      try {
         d.writeLong(tick);
         d.writeLong(hash);
         d.flush();
      } catch (IOException var7) {
         return;
      }

      this.sendFrame(MSG_HASH, baos.toByteArray());
      Long theirs = this.remoteHashes.get(tick);
      if (theirs != null && theirs.longValue() != hash) {
         this.onDiverged(tick, hash, theirs);
      }
   }

   private void onDiverged(long tick, long mine, long theirs) {
      if (tick <= this.lastDivergedTick) {
         return; // 同一 tick 只处理一次，避免重发风暴
      }

      this.lastDivergedTick = tick;
      System.out.println("[Net] DIVERGED tick=" + tick + " 本地=" + Long.toHexString(mine) + " 对端=" + Long.toHexString(theirs));
      if (!this.isHost) {
         this.setStatus("检测到漂移，向主机请求重同步");
         this.sendFrame(MSG_RESYNC, null);
      } else {
         this.setStatus("检测到漂移，重发世界");
         this.sendWorld();
      }
   }

   /** 让渲染线程序列化世界并发送（带上当前 tick，供对端对齐）。 */
   private void sendWorld() {
      Gdx.app.postRunnable(new Runnable() {
         @Override
         public void run() {
            try {
               byte[] nbt = Game.G.serializeWorld();
               long tick = Game.G.simTick;
               ByteArrayOutputStream baos = new ByteArrayOutputStream(nbt.length + 13);
               DataOutputStream d = new DataOutputStream(baos);
               d.writeLong(tick);
               // 暂停/倍速必须随世界一起过去：只靠命令会在生效前的那几 tick 里两端速度不同，直接判成漂移
               d.writeByte(Game.G.isPaused() ? 1 : 0);
               d.writeInt(Game.G.getGameSpeed());
               d.write(nbt);
               d.flush();
               byte[] body = baos.toByteArray();
               NetSession.markStartTick(tick);
               NetSession.this.sendFrame(MSG_WORLD, body);
               NetSession.this.setStatus(
                  "已发送世界（tick=" + tick + "，" + nbt.length + " 字节）worldHash=" + Long.toHexString(Game.G.worldBytesHash(nbt))
               );
            } catch (Exception var8) {
               Quarry.Q.pi.message(1, var8);
               NetSession.this.setStatus("序列化世界失败：" + var8);
            }
         }
      });
   }

   // ---------- 底层收发 ----------

   private byte[] readFrame(DataInputStream in) throws IOException {
      int len;
      try {
         len = in.readInt();
      } catch (IOException var6) {
         return null;
      }

      if (len <= 0 || len > MAX_FRAME) {
         return null;
      }

      byte[] frame = new byte[len];
      in.readFully(frame);
      return frame;
   }

   private void sendFrame(byte type, byte[] body) {
      byte[] frame = new byte[(body == null ? 0 : body.length) + 1];
      frame[0] = type;
      if (body != null) {
         System.arraycopy(body, 0, frame, 1, body.length);
      }

      this.outbox.add(frame);
   }

   private void sendHandshake(byte type, long seed) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      DataOutputStream d = new DataOutputStream(baos);
      d.writeInt(PROTOCOL);
      d.writeInt(Quarry.Q.versionNumber);
      d.writeLong(seed);
      d.flush();
      this.sendFrame(type, baos.toByteArray());
   }

   private void fail(String why) throws IOException {
      this.setStatus(why);
      this.sendFrame(MSG_HELLO_FAIL, why.getBytes("UTF-8"));

      try {
         Thread.sleep(200L);
      } catch (InterruptedException var4) {
      }

      close();
   }

   /** 状态变化：控制台 + 游戏内 toast（toast 必须在渲染线程）。 */
   private void setStatus(final String s) {
      this.status = s;
      lastStatus = s;
      System.out.println("[Net] " + s);
      if (Game.G != null && Game.G.ui != null) {
         Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
               try {
                  Game.G.ui.toast.show(s);
               } catch (Exception var2) {
               }
            }
         });
      }
   }
}
