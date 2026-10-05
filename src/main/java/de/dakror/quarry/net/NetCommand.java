package de.dakror.quarry.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 一条玩家命令（阶段 1）。定长编码：byte type + int[6] 载荷，字段含义按 type 解释。
 *   PAUSE  p0 = 是否暂停
 *   SPEED  p0 = 倍速
 *   PLACE  p0 = layerIndex, p1 = x, p2 = y, p3 = typeId, p4 = upDir(ordinal)
 *   REMOVE p0 = layerIndex, p1 = x, p2 = y, p3 = 是否电缆
 * 定长是为了收发简单；一条命令 25 字节，对局域网 PoC 完全够。
 */
public class NetCommand {
   public static final byte PAUSE = 1;
   public static final byte SPEED = 2;
   public static final byte PLACE = 3;
   public static final byte REMOVE = 4;

   public final byte type;
   public final int[] p = new int[6];

   public NetCommand(byte type, int p0, int p1, int p2, int p3) {
      this.type = type;
      this.p[0] = p0;
      this.p[1] = p1;
      this.p[2] = p2;
      this.p[3] = p3;
   }

   public static NetCommand pause(boolean paused) {
      return new NetCommand(PAUSE, paused ? 1 : 0, 0, 0, 0);
   }

   public static NetCommand speed(int speed) {
      return new NetCommand(SPEED, speed, 0, 0, 0);
   }

   public static NetCommand place(int layerIndex, int x, int y, int typeId, int upDir, boolean free) {
      NetCommand c = new NetCommand(PLACE, layerIndex, x, y, typeId);
      c.p[4] = upDir;
      // 免单（原版 GOD_MODE 语义）必须随命令下发：GOD_MODE 是单端本地标志，两端要按同一结果执行
      c.p[5] = free ? 1 : 0;
      return c;
   }

   public static NetCommand remove(int layerIndex, int x, int y, boolean cable) {
      return new NetCommand(REMOVE, layerIndex, x, y, cable ? 1 : 0);
   }

   public void write(DataOutputStream out) throws IOException {
      out.writeByte(this.type);

      for (int i = 0; i < 6; i++) {
         out.writeInt(this.p[i]);
      }
   }

   public static NetCommand read(DataInputStream in) throws IOException {
      NetCommand c = new NetCommand(in.readByte(), in.readInt(), in.readInt(), in.readInt(), in.readInt());

      for (int i = 4; i < 6; i++) {
         c.p[i] = in.readInt();
      }

      return c;
   }

   @Override
   public String toString() {
      switch (this.type) {
         case PAUSE:
            return "PAUSE(" + (this.p[0] == 1) + ")";
         case SPEED:
            return "SPEED(" + this.p[0] + ")";
         case PLACE:
            return "PLACE(layer=" + this.p[0] + " @" + this.p[1] + "," + this.p[2] + " type=" + this.p[3] + " dir=" + this.p[4] + ")";
         case REMOVE:
            return "REMOVE(layer=" + this.p[0] + " @" + this.p[1] + "," + this.p[2] + (this.p[3] == 1 ? " cable" : "") + ")";
         default:
            return "CMD(" + this.type + ")";
      }
   }
}
