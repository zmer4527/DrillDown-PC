package de.dakror.quarry.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.dakror.common.libgdx.ChangeNotifier;
import de.dakror.common.libgdx.Pair;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.DepthSpriter;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ITube;
import de.dakror.quarry.structure.base.StorageStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.Hopper;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Savable;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.EnumSet;

public class Layer implements Disposable, Savable, ChangeNotifier.Listener<Layer> {
   Chunk[] chunks;
   byte[] flags;
   public static final int FLAG_STRUCTURE_COLLISION = 1;
   public static final int FLAG_TUBE_COLLISION = 2;
   public static final int FLAG_ITEM_DOCK_COLLISION = 4;
   public static final int FLAG_FLUID_DOCK_COLLISION = 8;
   public static final int FLAG_POWER_DOCK_COLLISION = 16;
   public Array<StorageStructure> storages;
   public EnumSet<Item.ItemType> minerals;
   Array<PooledEffect> pfxBelow;
   Array<PooledEffect> pfxAbove;
   final Object chunkLock = new Object();
   final Object pfxLock = new Object();
   final Object loudnessLock = new Object();
   public int width;
   public int height;
   int chunksH;
   final int index;
   Tile.TileType defaultTile;
   float lastLoudnessCalculation;
   float[] loudness;
   float[] initialLoudness;
   float[] intermediateLoudness;
   boolean updateLoudnessFlag;
   static float[] kernel;
   static int kernelSize = 4;
   static float[] zeros;
   public Array<Generator.VeinDebug> veinDebugs = new Array();
   boolean fromLoading;
   boolean initialized;
   public boolean fake;
   public final Bounds dirtyBounds = new Bounds();

   /**
    * 模拟专用脏区，与 {@link #dirtyBounds}（渲染用）必须分开。
    * dirtyBounds 的排空时机取决于"当前层"：当前层由 draw()（渲染线程）排空、
    * 其它层由 tick 排空 —— 而当前层是每个玩家自己的视角。
    * 联机时两端各看一层，传送带/电缆的更新（Chunk.update 里 hasWidth/hasFlag 门控）
    * 就会不一样 → 必然分叉。simBounds 由 tick 对所有层统一排空，保证一致。
    */
   public final Bounds simBounds = new Bounds();
   /**
    * 自动拼接专用的脏区队列。与 {@link #dirtyBounds} 分开是必须的：dirtyBounds 的排空时机
    * 取决于渲染（当前层每帧在 draw 里排空、其他层每 tick 在 postUpdate 里排空），而拼接会改动瓦片数据，
    * 属于模拟状态——一旦跟渲染共用，两端会因为帧率/所处层不同而拼出不同结果（哈希不一致）。
    * 这里只由 Layer.set 写入、由 Layer.update 在 tick 内消费，完全确定。
    */
   public final Bounds promoteBounds = new Bounds();
   private final Bounds promoteTile = new Bounds();
   public final Bounds lastBounds = new Bounds();
   public final Bounds pendingBounds = new Bounds();
   long timeSum = 0L;
   long last = 0L;

   public Layer(int index, int initialWidth, int initialHeight, Tile.TileType defaultTile, boolean initChunks, boolean initGL) {
      this.width = initialWidth;
      this.height = initialHeight;
      this.defaultTile = defaultTile;
      this.index = index;
      this.flags = new byte[initialWidth * initialHeight];
      this.storages = new Array();
      this.minerals = EnumSet.noneOf(Item.ItemType.class);
      this.chunks = new Chunk[initialWidth / 32 * (initialHeight / 32)];
      this.pfxAbove = new Array(false, 100);
      this.pfxBelow = new Array(false, 100);
      if (initChunks) {
         this.initChunks();

         for (int i = 0; i < this.width; i += 32) {
            for (int j = 0; j < this.height; j += 32) {
               Chunk c = this.chunks[i / 32 * this.chunksH + j / 32];
               c.setFrameBuffer(Quarry.Q.chunkFBOs[i / 32 * this.chunksH + j / 32]);
            }
         }
      }
   }

   @Override
   public void onChangeEvent(ChangeNotifier.Event<Layer> event) {
      if (event.getData() != null && Math.abs(event.getData().getIndex() - this.getIndex()) > 3) {
         this.dispose();
      }
   }

   public void initChunks() {
      this.chunksH = (int)Math.ceil((double)(this.height / 32));

      for (int i = 0; i < this.width; i += 32) {
         for (int j = 0; j < this.height; j += 32) {
            Chunk c = new Chunk(i / 32, j / 32, this);
            this.chunks[i / 32 * this.chunksH + j / 32] = c;
         }
      }
   }

   public void addParticleEffect(PooledEffect e, boolean below) {
      synchronized (this.pfxLock) {
         if (below) {
            this.pfxBelow.add(e);
         } else {
            this.pfxAbove.add(e);
         }
      }
   }

   public void removeParticleEffect(PooledEffect e, boolean below) {
      synchronized (this.pfxLock) {
         boolean removed = false;
         if (below) {
            removed = this.pfxBelow.removeValue(e, true);
         } else {
            removed = this.pfxAbove.removeValue(e, true);
         }

         if (removed) {
            e.free();
         }
      }
   }

   public void updateLoudness(double deltaTime) {
      this.lastLoudnessCalculation = (float)((double)this.lastLoudnessCalculation - deltaTime);
      if (this.lastLoudnessCalculation <= 0.0F || this.updateLoudnessFlag) {
         synchronized (this.loudnessLock) {
            if (this.loudness == null) {
               this.loudness = new float[this.width * this.height];
               this.intermediateLoudness = new float[this.width * this.height];
               this.initialLoudness = new float[this.width * this.height];
               if (zeros == null) {
                  zeros = new float[this.width * this.height];
                  float mean = 0.0F;
                  float sigma = 1.0F;
                  kernel = new float[2 * kernelSize + 1];
                  float sum = 0.0F;

                  for (int j = 0; j < kernel.length; j++) {
                     int x = j - kernelSize;
                     float val = (float)Math.exp(-0.5 * Math.pow((double)(((float)x - mean) / sigma), 2.0));
                     kernel[j] = val;
                     sum += val;
                  }

                  for (int j = 0; j < kernel.length; j++) {
                     kernel[j] = kernel[j] / sum;
                  }
               }
            }

            for (Chunk c : this.getChunks()) {
               if (c != null && c.isInit()) {
                  for (Structure<?> s : c.getStructures()) {
                     float l = s.getLoudness();

                     for (int i = 0; i < s.getWidth(); i++) {
                        for (int j = 0; j < s.getHeight(); j++) {
                           this.initialLoudness[(s.x + i) * this.height + s.y + j] = l;
                        }
                     }
                  }
               }
            }

            for (int i = 0; i < this.width; i++) {
               for (int j = 0; j < this.height; j++) {
                  float sum = 0.0F;

                  for (int k = -kernelSize; k <= kernelSize; k++) {
                     if (j + k >= 0 && j + k < this.height) {
                        sum += this.initialLoudness[i * this.height + j + k] * kernel[k + kernelSize];
                     }
                  }

                  this.intermediateLoudness[i * this.height + j] = sum;
               }
            }

            for (int i = 0; i < this.height; i++) {
               for (int j = 0; j < this.width; j++) {
                  float sum = 0.0F;

                  for (int kx = -kernelSize; kx <= kernelSize; kx++) {
                     if (j + kx >= 0 && j + kx < this.width) {
                        sum += this.intermediateLoudness[(j + kx) * this.height + i] * kernel[kx + kernelSize];
                     }
                  }

                  this.loudness[j * this.height + i] = Math.min(1.0F, sum / 1.25F);
               }
            }

            this.lastLoudnessCalculation = 1.0F;
            this.updateLoudnessFlag = false;
         }
      }
   }

   public void update(double deltaTime, int gameSpeed) {
      // 拼接用独立的、tick 内确定性的脏区队列（不吃渲染时序，也不受当前所在层影响）
      int bx = this.promoteBounds.getX();
      int by = this.promoteBounds.getY();
      int bw = this.promoteBounds.getWidth();
      int bh = this.promoteBounds.getHeight();
      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            c.update(deltaTime, gameSpeed, this.simBounds);
         }

         if (bw > 0 && bh > 0) {
            for (Chunk c : this.chunks) {
               c.promoteSurroundedTiles(bx, by, bw, bh);
            }

            // 拼接过程中自己的 set() 会往 promoteBounds 里追加，留给下一个 tick 处理（级联，确定）
            this.promoteBounds.clear();
         }

         for (Chunk c : this.chunks) {
            c.postUpdate(this.simBounds);
         }
      }

      synchronized (this.pfxLock) {
         for (int i = this.pfxBelow.size - 1; i >= 0; i--) {
            PooledEffect effect = (PooledEffect)this.pfxBelow.get(i);
            effect.update((float)deltaTime);
            if (effect.isComplete()) {
               effect.free();
               this.pfxBelow.removeIndex(i);
            }
         }

         for (int ix = this.pfxAbove.size - 1; ix >= 0; ix--) {
            PooledEffect effect = (PooledEffect)this.pfxAbove.get(ix);
            effect.update((float)deltaTime);
            if (effect.isComplete()) {
               effect.free();
               this.pfxAbove.removeIndex(ix);
            }
         }
      }

      if (gameSpeed > 0 && Game.G.layer == this) {
         this.updateLoudness(deltaTime);
      }

      // 模拟脏区每个 tick 结束统一排空（对所有层一视同仁）—— 联机确定性的关键。
      // 渲染用的 dirtyBounds 仍由 draw()/postUpdate() 各自排空，互不影响。
      this.simBounds.clear();
   }

   /** 同时标进模拟脏区与渲染脏区。所有"结构变了"的标记都必须走这里。 */
   public void markDirty(Structure<?> s, int flags) {
      this.dirtyBounds.add(s, flags);
      this.simBounds.add(s, flags);
   }

   public void postUpdate() {
      if (!this.dirtyBounds.isEmpty() || this.fromLoading) {
         synchronized (this.loudnessLock) {
            this.updateLoudnessFlag = true;
         }

         this.lastBounds.set(this.dirtyBounds);
         this.pendingBounds.add(this.dirtyBounds);
         this.dirtyBounds.clear();
         this.fromLoading = false;
      }
   }

   public void draw(
      OrthographicCamera cam,
      OrthographicCamera fboCam,
      Viewport viewport,
      Batch batch,
      DepthSpriter spriter,
      ShapeRenderer shaper,
      SpriterDelegateBatch delegateBatch
   ) {
      if (!this.initialized) {
         Game.G.layerChangeNotifier.addListener(this);
         this.initialized = true;
      }

      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            if (c.isInBounds(cam, true) || this.dirtyBounds.intersects(c.ax, c.ay, 32, 32)) {
               c.draw(cam, fboCam, viewport, batch);
            }
         }
      }

      synchronized (this.pfxLock) {
         for (int i = this.pfxBelow.size - 1; i >= 0; i--) {
            PooledEffect effect = (PooledEffect)this.pfxBelow.get(i);
            effect.draw(batch);
         }
      }

      batch.end();
      Gdx.gl.glDepthMask(true);
      Gdx.gl.glDepthFunc(513);
      Gdx.gl.glEnable(3042);
      Gdx.gl.glBlendFunc(770, 771);
      spriter.setProjectionMatrix(cam.combined);
      spriter.begin(false);
      synchronized (this.chunkLock) {
         for (Chunk cx : this.chunks) {
            if (cx.isInBounds(cam, false) || this.dirtyBounds.intersects(cx.ax, cx.ay, 32, 32)) {
               cx.drawStructures(spriter, this.dirtyBounds);
            }
         }
      }

      if (!this.dirtyBounds.isEmpty() || this.fromLoading) {
         synchronized (this.loudnessLock) {
            this.updateLoudnessFlag = true;
         }

         this.lastBounds.set(this.dirtyBounds);
         this.dirtyBounds.clear();
         this.fromLoading = false;
      }

      spriter.end();
      batch.begin();
      synchronized (this.pfxLock) {
         for (int i = this.pfxAbove.size - 1; i >= 0; i--) {
            PooledEffect effect = (PooledEffect)this.pfxAbove.get(i);
            effect.draw(batch);
         }
      }

      batch.end();
      Gdx.gl.glDepthMask(true);
      Gdx.gl.glDepthFunc(513);
      Gdx.gl.glEnable(3042);
      Gdx.gl.glBlendFunc(770, 771);
      spriter.setProjectionMatrix(cam.combined);
      spriter.begin(true);
      shaper.setProjectionMatrix(cam.combined);
      shaper.begin(ShapeType.Filled);
      synchronized (this.chunkLock) {
         for (Chunk cxx : this.chunks) {
            if (cxx.isInBounds(cam, false)) {
               cxx.drawFrameStructures(spriter, shaper, delegateBatch);
            }
         }
      }

      spriter.draw();
      spriter.end();
      shaper.flush();
      if (Game.DRAW_DEBUG && !this.lastBounds.isEmpty()) {
         shaper.setColor(0.0F, 0.0F, 1.0F, 0.2F);
         shaper.rect(
            (float)(this.lastBounds.getX() * 64),
            (float)(this.lastBounds.getY() * 64),
            (float)(this.lastBounds.getWidth() * 64),
            (float)(this.lastBounds.getHeight() * 64)
         );
      }

      shaper.end();
      Gdx.gl.glDisable(3042);
      if (Game.DRAW_DEBUG) {
         shaper.begin(ShapeType.Line);
         shaper.setColor(Color.GREEN);

         for (Generator.VeinDebug vd : this.veinDebugs) {
            shaper.circle(vd.x * 64.0F, vd.y * 64.0F, vd.radius * 64.0F);
         }

         shaper.end();
      }

      batch.begin();
   }

   public Chunk getChunk(int x, int y) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         synchronized (this.chunkLock) {
            Chunk c = this.chunks[x / 32 * this.chunksH + y / 32];
            if (c == null) {
               return null;
            } else {
               if (!c.dataSet) {
                  c.init();
               }

               return c;
            }
         }
      } else {
         return null;
      }
   }

   public Structure<?> getStructure(int x, int y) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         synchronized (this.chunkLock) {
            Chunk c = this.getChunk(x, y);
            return c == null ? null : c.getStructure(x - c.ax, y - c.ay);
         }
      } else {
         return null;
      }
   }

   public Structure<?> getStructure(int i) {
      return this.getStructure(i / this.height, i % this.height);
   }

   public CopperCable getCable(int i) {
      return this.getCable(i / this.height, i % this.height);
   }

   public CopperCable getCable(int x, int y) {
      synchronized (this.chunkLock) {
         Chunk c = this.getChunk(x, y);
         return c == null ? null : c.getCable(x - c.ax, y - c.ay);
      }
   }

   public Structure<?> getAny(int i, boolean structuresFirst) {
      if (structuresFirst) {
         Structure<?> s = this.getStructure(i);
         return (Structure<?>)(s != null ? s : this.getCable(i));
      } else {
         CopperCable c = this.getCable(i);
         return (Structure<?>)(c != null ? c : this.getStructure(i));
      }
   }

   public Structure<?> getAny(int x, int y, boolean structuresFirst) {
      if (structuresFirst) {
         Structure<?> s = this.getStructure(x, y);
         return (Structure<?>)(s != null ? s : this.getCable(x, y));
      } else {
         CopperCable c = this.getCable(x, y);
         return (Structure<?>)(c != null ? c : this.getStructure(x, y));
      }
   }

   public boolean addStructure(Structure<?> s) {
      if (s.x >= 0 && s.y >= 0 && s.x < this.width && s.y < this.height) {
         Chunk c = this.getChunk(s.x, s.y);
         if (c == null) {
            return false;
         } else {
            return s instanceof CopperCable && this.getCable(s.x, s.y) != null ? false : c.addStructure(s, false);
         }
      } else {
         return false;
      }
   }

   public boolean removeStructure(int x, int y) {
      Structure<?> s = this.getStructure(x, y);
      return s == null ? false : this.removeStructure(s);
   }

   public boolean removeStructure(Structure<?> s) {
      Chunk c = this.getChunk(s.x, s.y);
      return c == null ? false : c.removeStructure(s.x - c.ax, s.y - c.ay);
   }

   public boolean removeCable(int x, int y) {
      Chunk c = this.getChunk(x, y);
      return c == null ? false : c.removeCable(x - c.ax, y - c.ay);
   }

   public boolean removeCable(CopperCable s) {
      return this.removeCable(s.x, s.y);
   }

   public void setItemNotification(int x, int y) {
      Structure<?> s = this.getStructure(x, y);
      if (s instanceof Conveyor) {
         ((Conveyor)s).setItemNotification();
      }
   }

   public boolean isPowerDockCollision(int x, int y) {
      return x >= 0 && y >= 0 && x < this.width && y < this.height ? (this.flags[x * this.height + y] & 16) != 0 : true;
   }

   public Chunk[] getChunks() {
      return this.chunks;
   }

   /** 清空本层所有结构的瞬态历史（发世界前调用）。 */
   public void clearTransientState() {
      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            c.clearTransientState();
         }
      }
   }

   protected void setCollision(Structure<?> s, boolean colliding) {
      for (int i = 0; i < s.getWidth(); i++) {
         int j = (s.x + i) * this.height + s.y;
         if (s instanceof ITube) {
            if (colliding) {
               this.flags[j] = (byte)(this.flags[j] | 2);
            } else {
               this.flags[j] = (byte)(this.flags[j] & -3);
            }
         } else if (colliding) {
            for (int k = 0; k < s.getHeight(); k++) {
               this.flags[j + k] = (byte)(this.flags[j + k] | 1);
            }
         } else {
            for (int k = 0; k < s.getHeight(); k++) {
               this.flags[j + k] = (byte)(this.flags[j + k] & -2);
            }
         }
      }

      for (Dock d : s.getDocks()) {
         if (d.type == Dock.DockType.Power) {
            if (colliding) {
               this.flags[(s.x + d.x) * this.height + s.y + d.y] = (byte)(this.flags[(s.x + d.x) * this.height + s.y + d.y] | 16);
            } else {
               this.flags[(s.x + d.x) * this.height + s.y + d.y] = (byte)(this.flags[(s.x + d.x) * this.height + s.y + d.y] & -17);
            }
         } else if (d.type == Dock.DockType.BigPower) {
            if (colliding) {
               this.flags[(s.x + d.x) * this.height + s.y + d.y] = (byte)(this.flags[(s.x + d.x) * this.height + s.y + d.y] | 16);
               this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
                  this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] | 17
               );
            } else {
               this.flags[(s.x + d.x) * this.height + s.y + d.y] = (byte)(this.flags[(s.x + d.x) * this.height + s.y + d.y] & -17);
               this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
                  this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] & -18
               );
            }
         } else if (d.type != Dock.DockType.ItemIn && d.type != Dock.DockType.ItemOut) {
            if (d.type == Dock.DockType.FluidIn || d.type == Dock.DockType.FluidOut) {
               if (colliding) {
                  this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
                     this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] | 8
                  );
               } else {
                  this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
                     this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] & -9
                  );
               }
            }
         } else if (colliding) {
            this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
               this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] | 4
            );
         } else {
            this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] = (byte)(
               this.flags[(s.x + d.x + d.dir.dx) * this.height + s.y + d.y + d.dir.dy] & -5
            );
         }
      }
   }

   public boolean isCollision(int x, int y, boolean checkItemDocks, boolean checkFluidDocks) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         byte flag = this.flags[x * this.height + y];
         byte query = 3;
         if (checkItemDocks) {
            query = (byte)(query | 4);
         }

         if (checkFluidDocks) {
            query = (byte)(query | 8);
         }

         return (flag & query) != 0;
      } else {
         return true;
      }
   }

   public byte getFlags(int x, int y) {
      return x >= 0 && y >= 0 && x < this.width && y < this.height ? this.flags[x * this.height + y] : 0;
   }

   public boolean isColliding(Structure<?> s, boolean checkItemDocks, boolean checkFluidDocks) {
      if (s.x < 0) {
         return false;
      } else {
         for (int i = 0; i < s.getWidth(); i++) {
            int j = 0;

            while (j < s.getHeight()) {
               if (s.x + i >= 0 && s.x + i < this.width) {
                  if (s.y + j >= 0 && s.y + j < this.height) {
                     if (this.isCollision(s.x + i, s.y + j, checkItemDocks, checkFluidDocks)) {
                        return true;
                     }

                     j++;
                     continue;
                  }

                  return true;
               }

               return true;
            }
         }

         for (Dock d : s.getDocks()) {
            int sx = s.x + d.x + d.dir.dx;
            int sy = s.y + d.y + d.dir.dy;
            if (d.type != Dock.DockType.StackIn && d.type != Dock.DockType.StackOut) {
               if (sx < 0 || sy < 0 || sx >= this.width || sy >= this.height) {
                  return true;
               }

               if (d.type == Dock.DockType.Power) {
                  if ((this.flags[sx * this.height + sy] & 16) != 0) {
                     return true;
                  }
               } else if (d.type == Dock.DockType.BigPower) {
                  if ((this.flags[sx * this.height + sy] & 16) != 0 || (this.flags[sx * this.height + sy] & 1) != 0) {
                     return true;
                  }
               } else if ((this.flags[sx * this.height + sy] & 1) != 0) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean isInFogOfWar(int x, int y) {
      return (this.getMeta(x, y) & 1) != 0;
   }

   public boolean isNotInFogOfWar(Structure<?> s, boolean partly) {
      if (s.x < 0) {
         return false;
      } else {
         for (int i = 0; i < s.getWidth(); i++) {
            for (int j = 0; j < s.getHeight(); j++) {
               if ((this.getMeta(s.x + i, s.y + j) & 1) != 0) {
                  if (!partly) {
                     return false;
                  }
               } else if (partly) {
                  return true;
               }
            }
         }

         return true;
      }
   }

   public synchronized Tile.TileType get(int x, int y) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         Chunk c = this.getChunk(x, y);
         if (c == null) {
            return Tile.TileType.Air;
         } else {
            int addr = (x - c.ax) * 32 + (y - c.ay);
            return Tile.tiles[(byte)(c.data[addr] & 255)];
         }
      } else {
         return Tile.TileType.Air;
      }
   }

   public synchronized int getMeta(int x, int y) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         Chunk c = this.getChunk(x, y);
         if (c == null) {
            return 0;
         } else {
            int addr = (x - c.ax) * 32 + (y - c.ay);
            return c.data[addr] >> 8 & 0xFF;
         }
      } else {
         return 0;
      }
   }

   /**
    * 纯装饰性随机（tile 变体贴图位/旋转位）用位置哈希，而不是 SimRandom。
    * 原因：Chunk.draw 会在渲染阶段调用 set()，若消耗共享随机流，地图内容就会依赖渲染时机
    * （滚动、分辨率、可见区块），联机时必然不同步。位置哈希对随机流零影响，各端结果一致。
    */
   public static float varRandom(int x, int y, int salt) {
      long h = Generator.G.getSeed() * -7046029254386353131L;
      h = h * 31L + (long)x * 73856093L;
      h = h * 31L + (long)y * 19349663L;
      h = h * 31L + (long)salt;
      h ^= h >>> 33;
      h *= -49064778989728563L;
      h ^= h >>> 33;
      h *= -4265267296055464877L;
      h ^= h >>> 33;
      return (float)((h >>> 11) / (double)(1L << 53));
   }

   public synchronized void set(int x, int y, Tile.TileType tile) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         Chunk c = this.getChunk(x, y);
         if (c != null) {
            int addr = (x - c.ax) * 32 + (y - c.ay);
            if (tile.itemDrop != null) {
               this.minerals.add(tile.itemDrop);
            }

            c.data[addr] = (short)tile.value;
            byte meta = tile.meta;
            if ((tile.meta & 2) == 2 && varRandom(x, y, 0) >= 0.5F) {
               meta = (byte)(meta | 2);
            }

            if ((tile.meta & 4) == 4) {
               float rot = varRandom(x, y, 1);
               if (rot >= 0.75F) {
                  meta = (byte)(meta | 36);
               } else if (rot >= 0.5F) {
                  meta = (byte)(meta | 20);
               } else if (rot >= 0.25F) {
                  meta = (byte)(meta | 12);
               }
            }

            c.data[addr] = (short)(c.data[addr] | meta << 8);
            c.dirty = true;
            this.markSurroundingTilesAsDirty(x, y);
            // 记进"待拼接"队列：自己 + 周围一圈（同化要看的正是邻格）
            if (this.promoteBounds.isEmpty()) {
               this.promoteBounds.set(x - 1, y - 1, 3, 3, 0);
            } else {
               this.promoteTile.set(x - 1, y - 1, 3, 3, 0);
               this.promoteBounds.add(this.promoteTile);
            }
         }
      }
   }

   public synchronized void addMeta(int x, int y, int metaFlag) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         Chunk c = this.getChunk(x, y);
         if (c != null) {
            int addr = (x - c.ax) * 32 + (y - c.ay);
            c.data[addr] = (short)(c.data[addr] | metaFlag << 8);
            c.dirty = true;
            this.markSurroundingTilesAsDirty(x, y);
         }
      }
   }

   public synchronized void removeMeta(int x, int y, int metaFlag) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         Chunk c = this.getChunk(x, y);
         if (c != null) {
            int addr = (x - c.ax) * 32 + (y - c.ay);
            c.data[addr] = (short)(c.data[addr] & ~(metaFlag << 8));
            c.dirty = true;
            this.markSurroundingTilesAsDirty(x, y);
         }
      }
   }

   protected void markSurroundingTilesAsDirty(int x, int y) {
      for (Direction d : Direction.values) {
         Chunk c = this.getChunk(x + d.dx, y + d.dy);
         if (c != null) {
            c.dirty = true;
         }
      }
   }

   public synchronized boolean addItemEntity(Item.ItemType value, int x, int y, int lastSlot, int slot, Direction dir, Structure<?> source) {
      Structure<?> s = this.getStructure(x, y);
      return !(s instanceof Conveyor) ? false : ((Conveyor)s).addItemEntity(value, lastSlot, slot, dir, source);
   }

   public static int getStartingSlot(Direction dir) {
      switch (dir) {
         case East:
            return 7;
         case North:
            return 0;
         case South:
            return 6;
         case West:
            return 13;
         default:
            return 0;
      }
   }

   public boolean isItemSlotFree(int x, int y, int slot) {
      Structure<?> s = this.getStructure(x, y);
      return s instanceof Conveyor ? ((Conveyor)s).isItemSlotFree(slot) : false;
   }

   public synchronized boolean addItemEntity(Item.ItemType value, Structure<?> source, Direction dir, Structure<?> originalSource) {
      Structure<?> s = this.getStructure(source.x + dir.dx, source.y + dir.dy);
      int startingSlot = getStartingSlot(dir);
      if (s instanceof Conveyor) {
         Direction gotoDir = ((Conveyor)s).getNextDirection(value, dir, -4.0F, startingSlot);
         return this.addItemEntity(
            value, source.x + dir.dx, source.y + dir.dy, getStartingSlot(dir.inv()), startingSlot, gotoDir, originalSource == null ? source : originalSource
         );
      } else {
         return false;
      }
   }

   public synchronized boolean addItemEntity(Item.ItemType value, Structure<?> source, Dock dock, Structure<?> originalSource) {
      Structure<?> s = this.getStructure(source.x + dock.x + dock.dir.dx, source.y + dock.y + dock.dir.dy);
      Direction gotoDir = dock.dir;
      int startingSlot = getStartingSlot(dock.dir);
      if (s instanceof Conveyor) {
         gotoDir = ((Conveyor)s).getNextDirection(value, dock.dir, -4.0F, startingSlot);
         return this.addItemEntity(
            value,
            source.x + dock.x + dock.dir.dx,
            source.y + dock.y + dock.dir.dy,
            getStartingSlot(dock.dir.inv()),
            startingSlot,
            gotoDir,
            originalSource == null ? source : originalSource
         );
      } else {
         return false;
      }
   }

   public int getItemCount() {
      synchronized (this.chunkLock) {
         int sum = 0;

         for (Chunk c : this.chunks) {
            if (c != null && c.isInit()) {
               sum += c.getItemCount();
            }
         }

         return sum;
      }
   }

   public synchronized float getLoudness(int x, int y) {
      if (x >= 0 && y >= 0 && x < this.width && y < this.height) {
         synchronized (this.loudnessLock) {
            if (this.loudness == null) {
               this.updateLoudnessFlag = true;
               return 0.0F;
            } else {
               return this.loudness[x * this.height + y];
            }
         }
      } else {
         return 0.0F;
      }
   }

   public synchronized float[] getLoudness() {
      if (this.loudness == null) {
         this.updateLoudnessFlag = true;
         return null;
      } else {
         return this.loudness;
      }
   }

   public void dispose() {
      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            c.dispose();
         }
      }

      this.initialized = false;
      Game.G.layerChangeNotifier.removeListener(this);
   }

   public int getIndex() {
      return this.index;
   }

   public int getStructureCount() {
      synchronized (this.chunkLock) {
         int sum = 0;

         for (Chunk c : this.getChunks()) {
            if (c != null && c.isInit()) {
               sum += c.getStructures().size;
            }
         }

         return sum;
      }
   }

   @Override
   public void save(NBT.Builder b) {
      b.Compound().Byte("chunksW", (byte)this.chunksH).Int("width", this.width).Int("height", this.height).Byte("defaultTile", this.defaultTile.value);
      synchronized (this.chunkLock) {
         b.List("Chunks", NBT.TagType.Compound);

         for (Chunk c : this.chunks) {
            c.save(b);
         }

         b.End();
      }

      b.End();
   }

   public void postAllLayersLoad(boolean firstCycle) {
      if (firstCycle) {
         // 读档/收世界后把整层标进待拼接队列：让两端的同化状态收敛到同一结果
         // （否则"存档快照前刚 set 过、还没同化"的那一格，主机下一 tick 会同化，客户端永远不会）
         this.promoteBounds.set(0, 0, this.width, this.height, 0);
      }

      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            c.postAllLayersLoad(firstCycle);
         }
      }
   }

   public void stopSfx() {
      synchronized (this.chunkLock) {
         for (Chunk c : this.chunks) {
            c.stopSfx();
         }
      }
   }

   public static Layer load(int index, NBT.CompoundTag tag) throws NBT.NBTException {
      int chunksW = tag.Byte("chunksW");
      Layer l = new Layer(index, tag.Int("width"), tag.Int("height"), Tile.tiles[tag.Byte("defaultTile")], false, false);
      l.chunksH = chunksW;
      l.fromLoading = true;
      Array<Pair<Chunk, NBT.CompoundTag>> pairs = new Array();

      for (NBT.Tag t : tag.List("Chunks", NBT.TagType.Compound).data) {
         try {
            Chunk c = Chunk.load((NBT.CompoundTag)t, l);
            l.chunks[c.x * chunksW + c.y] = c;
            c.setFrameBuffer(Quarry.Q.chunkFBOs[c.x * chunksW + c.y]);
            pairs.add(new Pair<>(c, (NBT.CompoundTag)t));
         } catch (NBT.NBTException var10) {
            Quarry.Q.pi.message(1, var10);
         }
      }

      for (int i = 0; i < l.width; i++) {
         for (int j = 0; j < l.height; j++) {
            Item.ItemType t = l.get(i, j).itemDrop;
            if (t != null) {
               l.minerals.add(t);
            }
         }
      }

      for (Pair<Chunk, NBT.CompoundTag> p : pairs) {
         try {
            p.getKey().loadStructures(p.getVal());
         } catch (NBT.NBTException var9) {
            Quarry.Q.pi.message(1, var9);
         }
      }

      for (Chunk c : l.chunks) {
         c.postLoad();
      }

      return l;
   }

   public boolean hasHoppersAttached(Structure<?> s) {
      for (Dock d : s.getDocks()) {
         Structure<?> q = this.getStructure(s.x + d.x + d.dir.dx, s.y + d.y + d.dir.dy);
         if (q instanceof Hopper && ((Hopper)q).getDirection() == d.dir.inv()) {
            return true;
         }
      }

      return false;
   }

   public int removeAttachedHoppers(Structure<?> s) {
      int count = 0;

      for (Dock d : s.getDocks()) {
         Structure<?> q = this.getStructure(s.x + d.x + d.dir.dx, s.y + d.y + d.dir.dy);
         if (q instanceof Hopper && ((Hopper)q).getDirection() == d.dir.inv()) {
            this.removeStructure(q);
            count++;
         }
      }

      return count;
   }
}
