package de.dakror.quarry.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.DepthSpriter;
import de.dakror.common.libgdx.render.MeshBuilderDelegate;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.power.PowerNetwork;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.StorageStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.FBOable;
import de.dakror.quarry.util.Savable;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.HashMap;

public class Chunk implements Disposable, FBOable, Savable {
   public static final float WHITE = Color.WHITE.toFloatBits();
   static final TextureRegion overhang = Quarry.Q.atlas.findRegion("overhang");
   static final TextureRegion fogOverhang = Quarry.Q.atlas.findRegion("overhang_fog");
   static final TextureRegion fogOverhangInner = Quarry.Q.atlas.findRegion("overhang_fog_inner");
   static final TextureRegion fogOfWar = Quarry.Q.atlas.findRegion("tile_fog");
   static final int COR = 0;
   static final int INN = 1;
   static final int MID = 2;
   static final int HOL = 3;
   static final HashMap<Tile.TileType, HashMap<Tile.TileType, TextureRegion[]>> texLookup = new HashMap<>();
   final Object structLock = new Object();
   short[] data;
   int x;
   int y;
   int maxX;
   int maxY;
   int ax;
   int ay;
   Layer layer;
   public boolean dirty;
   boolean dataSet;
   boolean glSet;
   IntMap<Structure<?>> spatial;
   Array<Structure<?>> structures;
   Array<Conveyor> conveyors;
   IntMap<CopperCable> cables;
   MeshBuilderDelegate builder;
   FrameBuffer fbo;
   private boolean inBounds;

   public Chunk(int x, int y, Layer layer) {
      this.x = x;
      this.y = y;
      this.ax = x * 32;
      this.ay = y * 32;
      this.maxX = this.ax + 32;
      this.maxY = this.ay + 32;
      this.layer = layer;
      this.dirty = true;
   }

   public void init() {
      if (!this.dataSet) {
         this.data = new short[1024];
         if (this.layer.getIndex() == 0) {
            for (int i = 0; i < this.data.length; i++) {
               this.data[i] = (short)(this.layer.defaultTile.value | this.layer.defaultTile.meta << 8);
            }
         } else {
            short val = (short)(this.layer.defaultTile.value | (this.layer.defaultTile.meta | 1) << 8);

            for (int i = 0; i < this.data.length; i++) {
               this.data[i] = val;
            }
         }

         this.spatial = new IntMap();
         this.structures = new Array(false, 100, Structure.class);
         this.conveyors = new Array(false, 100, Conveyor.class);
         this.cables = new IntMap(100);
         this.dataSet = true;
      }
   }

   public boolean isInit() {
      return this.dataSet;
   }

   private void initGL() {
      if (!this.glSet) {
         VertexAttributes attributes = new VertexAttributes(
            new VertexAttribute[]{new VertexAttribute(1, 3, "a_position"), new VertexAttribute(16, 2, "a_texCoord0")}
         );
         this.builder = new MeshBuilderDelegate(attributes, (Texture)Quarry.Q.atlas.getTextures().first());
         this.glSet = true;
      }
   }

   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (this.dataSet) {
         if (this.dirty && this.layer == Game.G.layer && Game.G.activeStructure != null) {
            Game.G.camControl.updateTrail();
            Game.G.camControl.updateActiveElementPlaceable();
         }

         boolean hasWidth = dirtyBounds.getWidth() > 0;
         synchronized (this.structLock) {
            for (Structure<?> st : (Structure[])this.structures.items) {
               if (st == null) {
                  break;
               }

               if (hasWidth || st.getSchema().type != StructureType.Conveyor) {
                  st.update(deltaTime, gameSpeed * st.getSpeedScale(), dirtyBounds);
               }
            }

            for (Conveyor st : (Conveyor[])this.conveyors.items) {
               if (st == null) {
                  break;
               }

               st.updateItems(deltaTime, gameSpeed, true);
            }

            if (dirtyBounds.hasFlag(4) || hasWidth) {
               for (CopperCable c : this.cables.values()) {
                  c.update(deltaTime, gameSpeed, dirtyBounds);
               }
            }
         }
      }
   }

   public void postUpdate(Bounds dirtyBounds) {
      if (this.dataSet) {
         synchronized (this.structLock) {
            for (Structure<?> st : (Structure[])this.structures.items) {
               if (st == null) {
                  break;
               }

               st.postUpdate(dirtyBounds);
            }

            if (dirtyBounds.hasAnyFlag(68)) {
               for (Structure<?> st : (Structure[])this.structures.items) {
                  if (st == null) {
                     break;
                  }

                  st.reloadPowerNetwork();
               }
            }
         }
      }
   }

   public boolean isInBounds(OrthographicCamera cam, boolean compute) {
      if (compute) {
         this.inBounds = cam.position.x < (float)(this.maxX * 64) + cam.viewportWidth / 2.0F * cam.zoom
            && cam.position.y < (float)(this.maxY * 64) + cam.viewportHeight / 2.0F * cam.zoom
            && cam.position.x + cam.viewportWidth / 2.0F * cam.zoom > (float)(this.x * 2048)
            && cam.position.y + cam.viewportHeight / 2.0F * cam.zoom > (float)(this.y * 2048);
      }

      return this.inBounds;
   }

   public boolean isInDirtyBounds(Bounds dirtyBounds) {
      return dirtyBounds.intersects(this.ax, this.ay, 32, 32);
   }

   /**
    * 瓦片自动拼接：被四个正交邻居全是同一种「可混合」瓦片（meta & BLENDING）包住的瓦片会被同化为该类型。
    * 规则与原 drawBaseTile 内联实现逐条等价（邻居取 base 归一化、四个正交邻居都要非空且不等于自身、
    * 同化结果取 y-1 那个邻居），区别只是从渲染期挪到了模拟步，这样地图内容不再依赖渲染时机，联机各端一致。
    * 传入的是调用方在进入本方法前取的脏区快照，避免本方法内 set() 反过来扩大遍历范围（级联留给后续 tick）。
    */
   public void promoteSurroundedTiles(int bx, int by, int bw, int bh) {
      if (bw <= 0 || bh <= 0 || bx + bw <= this.ax || bx >= this.ax + 32 || by + bh <= this.ay || by >= this.ay + 32) {
         return;
      }

      int x0 = Math.max(bx, this.ax) - this.ax;
      int x1 = Math.min(bx + bw, this.ax + 32) - this.ax;
      int y0 = Math.max(by, this.ay) - this.ay;
      int y1 = Math.min(by + bh, this.ay + 32) - this.ay;

      for (int x = x0; x < x1; x++) {
         int tx = this.ax + x;

         for (int y = y0; y < y1; y++) {
            int ty = this.ay + y;
            Tile.TileType t = this.layer.get(tx, ty);
            if (t.name == null) {
               continue;
            }

            if (t.base != null) {
               t = t.base;
            }

            Tile.TileType south = this.blendingNeighbor(tx, ty - 1, t);
            Tile.TileType north = this.blendingNeighbor(tx, ty + 1, t);
            Tile.TileType west = this.blendingNeighbor(tx - 1, ty, t);
            Tile.TileType east = this.blendingNeighbor(tx + 1, ty, t);
            if (south != null && north != null && west != null && east != null) {
               this.layer.set(tx, ty, south);
            }
         }
      }
   }

   private Tile.TileType blendingNeighbor(int x, int y, Tile.TileType t) {
      Tile.TileType u = this.layer.get(x, y);
      if (u.base != null) {
         u = u.base;
      }

      return (u.meta & 64) == 64 && u != t ? u : null;
   }

   private void drawBaseTile(Tile.TileType t, int x, int y, Batch batch) {
      if (texLookup.isEmpty()) {
         initTexCache();
      }

      batch.draw(t.tex, (float)(x * 64), (float)(y * 64), 64.0F, 64.0F);
      Tile.TileType[][] neighbors = new Tile.TileType[3][3];
      boolean any = false;

      for (int j = -1; j < 2; j++) {
         for (int k = -1; k < 2; k++) {
            if (j != 0 || k != 0) {
               Tile.TileType u = this.layer.get(this.ax + x + j, this.ay + y + k);
               if (u.base != null) {
                  u = u.base;
               }

               if ((u.meta & 64) == 64 && u != t) {
                  neighbors[j + 1][k + 1] = u;
                  any = true;
               }
            }
         }
      }

      if (any) {
         if (neighbors[1][0] != null && neighbors[1][2] != null && neighbors[0][1] != null && neighbors[2][1] != null) {
            // 同化已挪到模拟步（Chunk.promoteSurroundedTiles），这里只负责画
            batch.draw(neighbors[1][0].tex, (float)(x * 64), (float)(y * 64), 64.0F, 64.0F);
            return;
         }

         if (neighbors[0][0] != null && neighbors[1][0] == null && neighbors[0][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[0][0])[0], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 0.0F);
         }

         if (neighbors[0][2] != null && neighbors[1][2] == null && neighbors[0][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[0][2])[0], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, -90.0F);
         }

         if (neighbors[2][2] != null && neighbors[1][2] == null && neighbors[2][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[2][2])[0], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
         }

         if (neighbors[2][0] != null && neighbors[1][0] == null && neighbors[2][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[2][0])[0], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
         }

         if (neighbors[1][0] != null && neighbors[0][1] == null && neighbors[2][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[1][0])[2], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 0.0F);
         }

         if (neighbors[0][1] != null && neighbors[1][0] == null && neighbors[1][2] == null) {
            batch.draw(texLookup.get(t).get(neighbors[0][1])[2], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, -90.0F);
         }

         if (neighbors[1][2] != null && neighbors[0][1] == null && neighbors[2][1] == null) {
            batch.draw(texLookup.get(t).get(neighbors[1][2])[2], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
         }

         if (neighbors[2][1] != null && neighbors[1][0] == null && neighbors[1][2] == null) {
            batch.draw(texLookup.get(t).get(neighbors[2][1])[2], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
         }

         if (neighbors[1][0] != null && neighbors[0][1] == neighbors[1][0] && neighbors[1][2] == neighbors[0][1]) {
            batch.draw(texLookup.get(t).get(neighbors[1][0])[3], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 0.0F);
         } else if (neighbors[0][1] != null && neighbors[1][2] == neighbors[0][1] && neighbors[2][1] == neighbors[0][1]) {
            batch.draw(texLookup.get(t).get(neighbors[0][1])[3], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, -90.0F);
         } else if (neighbors[1][0] != null && neighbors[1][2] == neighbors[1][0] && neighbors[2][1] == neighbors[1][0]) {
            batch.draw(texLookup.get(t).get(neighbors[1][0])[3], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
         } else if (neighbors[0][1] != null && neighbors[2][1] == neighbors[0][1] && neighbors[1][0] == neighbors[0][1]) {
            batch.draw(texLookup.get(t).get(neighbors[0][1])[3], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
         } else {
            if (neighbors[1][0] != null && neighbors[0][1] == neighbors[1][0]) {
               batch.draw(texLookup.get(t).get(neighbors[1][0])[1], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 0.0F);
            }

            if (neighbors[0][1] != null && neighbors[1][2] == neighbors[0][1]) {
               batch.draw(texLookup.get(t).get(neighbors[0][1])[1], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, -90.0F);
            }

            if (neighbors[2][1] != null && neighbors[1][2] == neighbors[2][1]) {
               batch.draw(texLookup.get(t).get(neighbors[2][1])[1], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
            }

            if (neighbors[1][0] != null && neighbors[2][1] == neighbors[1][0]) {
               batch.draw(texLookup.get(t).get(neighbors[1][0])[1], (float)(x * 64), (float)(y * 64), 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
            }
         }
      }
   }

   public void setFrameBuffer(FrameBuffer fbo) {
      this.fbo = fbo;
   }

   @Override
   public void draw(OrthographicCamera cam, OrthographicCamera fboCam, Viewport viewport, Batch batch) {
      if (this.dirty) {
         if (!this.dataSet) {
            this.init();
         }

         batch.end();
         this.fbo.begin();
         Gdx.gl.glClear(16640);
         batch.setProjectionMatrix(fboCam.combined);
         batch.begin();
         this.dirty = false;

         for (int i = 0; i < this.data.length; i++) {
            byte b = (byte)(this.data[i] & 255);
            int m = this.data[i] >> 8;
            Tile.TileType t = Tile.tiles[b];
            if (t.name != null) {
               int x = i / 32;
               int y = i % 32;
               int tx = x * 64;
               int ty = y * 64;
               if ((m & 1) == 1) {
                  batch.draw(fogOfWar, (float)tx, (float)ty, 64.0F, 64.0F);
               } else {
                  if (t.base != null) {
                     this.drawBaseTile(t.base, x, y, batch);
                  }

                  TextureRegion tex = t.tex;
                  if ((m & 2) == 2) {
                     if ((t.meta & 2) == 0) {
                        m &= -3;
                     } else {
                        tex = t.texAlt;
                     }
                  }

                  if ((m & 4) == 4) {
                     if ((t.meta & 4) == 0) {
                        m &= -5;
                     } else {
                        int rot = 0;
                        if ((m & 8) == 8) {
                           rot = 90;
                        } else if ((m & 16) == 16) {
                           rot = 180;
                        } else if ((m & 32) == 32) {
                           rot = 270;
                        }

                        batch.draw(tex, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)rot);
                     }
                  } else if ((m & 128) == 128) {
                     this.drawBaseTile(t, x, y, batch);
                  } else {
                     batch.draw(tex, (float)tx, (float)ty, 64.0F, 64.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x, this.ay + y + 1) & 1) == 1) {
                     batch.draw(fogOverhang, (float)tx, (float)ty, 64.0F, 64.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x, this.ay + y - 1) & 1) == 1) {
                     batch.draw(fogOverhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x - 1, this.ay + y) & 1) == 1) {
                     batch.draw(fogOverhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x + 1, this.ay + y) & 1) == 1) {
                     batch.draw(fogOverhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 270.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x - 1, this.ay + y - 1) & 1) == 1
                     && (this.layer.getMeta(this.ax + x - 1, this.ay + y) & 1) != 1
                     && (this.layer.getMeta(this.ax + x, this.ay + y - 1) & 1) != 1) {
                     batch.draw(fogOverhangInner, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x - 1, this.ay + y + 1) & 1) == 1
                     && (this.layer.getMeta(this.ax + x - 1, this.ay + y) & 1) != 1
                     && (this.layer.getMeta(this.ax + x, this.ay + y + 1) & 1) != 1) {
                     batch.draw(fogOverhangInner, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x + 1, this.ay + y - 1) & 1) == 1
                     && (this.layer.getMeta(this.ax + x + 1, this.ay + y) & 1) != 1
                     && (this.layer.getMeta(this.ax + x, this.ay + y - 1) & 1) != 1) {
                     batch.draw(fogOverhangInner, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, -90.0F);
                  }

                  if ((this.layer.getMeta(this.ax + x + 1, this.ay + y + 1) & 1) == 1
                     && (this.layer.getMeta(this.ax + x + 1, this.ay + y) & 1) != 1
                     && (this.layer.getMeta(this.ax + x, this.ay + y + 1) & 1) != 1) {
                     batch.draw(fogOverhangInner, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 0.0F);
                  }

                  if (this.ay + y == this.layer.height - 1) {
                     batch.draw(overhang, (float)tx, (float)ty, 64.0F, 64.0F);
                  }

                  if (this.ay + y == 0) {
                     batch.draw(overhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 180.0F);
                  }

                  if (this.ax + x == 0) {
                     batch.draw(overhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 90.0F);
                  }

                  if (this.ax + x == this.layer.width - 1) {
                     batch.draw(overhang, (float)tx, (float)ty, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, 270.0F);
                  }
               }
            }
         }

         batch.end();
         this.fbo.end();
         ((Texture)this.fbo.getColorBufferTexture()).setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
         if (viewport != null) {
            viewport.apply();
         }

         batch.setProjectionMatrix(cam.combined);
         batch.begin();
      }

      if (this.dataSet) {
         batch.draw((Texture)this.fbo.getColorBufferTexture(), (float)(this.x * 2048), (float)(this.y * 2048), 2048.0F, 2048.0F, 0.0F, 0.0F, 1.0F, 1.0F);
      }
   }

   public void drawStructures(DepthSpriter spriter, Bounds dirtyBounds) {
      if (this.dataSet) {
         if (this.isInDirtyBounds(dirtyBounds) || !this.glSet) {
            if (!this.glSet) {
               this.initGL();
            }

            this.builder.begin();
            synchronized (this.structLock) {
               for (Structure<?> st : (Structure[])this.structures.items) {
                  if (st == null) {
                     break;
                  }

                  st.draw(this.builder);
               }

               for (Entry<CopperCable> st : this.cables.entries()) {
                  ((CopperCable)st.value).draw(this.builder);
               }
            }

            this.builder.end();
         }

         Gdx.gl.glEnable(3042);
         Mesh mesh = this.builder.getMesh();
         if (mesh != null) {
            mesh.render(spriter.getShader(), 4);
         }
      }
   }

   public void drawFrameStructures(DepthSpriter spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      if (this.dataSet) {
         synchronized (this.structLock) {
            for (Structure<?> st : (Structure[])this.structures.items) {
               if (st == null) {
                  break;
               }

               st.drawFrame(spriter, shaper, pfxBatch);
            }

            if (Game.DRAW_DEBUG) {
               for (Entry<CopperCable> st : this.cables.entries()) {
                  ((CopperCable)st.value).drawFrame(spriter, shaper, pfxBatch);
               }
            }
         }
      }
   }

   public Structure<?> getStructure(int x, int y) {
      if (!this.dataSet) {
         return null;
      } else {
         synchronized (this.structLock) {
            return (Structure<?>)this.spatial.get(x * 32 + y);
         }
      }
   }

   public CopperCable getCable(int x, int y) {
      if (!this.dataSet) {
         return null;
      } else {
         synchronized (this.structLock) {
            return (CopperCable)this.cables.get(x * 32 + y);
         }
      }
   }

   public boolean removeCable(int x, int y) {
      if (!this.dataSet) {
         return false;
      } else {
         CopperCable c = (CopperCable)this.cables.remove(x * 32 + y);
         if (c != null) {
            c.onDestroy();
            this.layer.markDirty(c, 512);
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean removeStructure(int x, int y) {
      if (!this.dataSet) {
         return false;
      } else {
         synchronized (this.structLock) {
            Structure<?> q = (Structure<?>)this.spatial.remove(x * 32 + y);
            if (q == null) {
               return false;
            } else {
               for (int i = 0; i < q.getWidth(); i++) {
                  for (int j = 0; j < q.getHeight(); j++) {
                     this.removeSpatial(q.x + i, q.y + j);
                  }
               }

               this.structures.removeValue(q, true);
               this.maxX = this.ax + 32;
               this.maxY = this.ay + 32;

               for (int i = 0; i < this.structures.size; i++) {
                  Structure<?> s = (Structure<?>)this.structures.get(i);
                  this.maxX = Math.max(this.maxX, s.x + s.getWidth());
                  this.maxY = Math.max(this.maxY, s.y + s.getHeight());
               }

               this.layer.setCollision(q, false);
               q.onDestroy();
               this.layer.markDirty(q, 512);
               if (q instanceof StorageStructure) {
                  this.layer.storages.removeValue((StorageStructure)q, true);
               } else if (q instanceof Conveyor) {
                  this.conveyors.removeValue((Conveyor)q, true);
               }

               if (q.getSchema().powerDocks > 0 && q.getPowerNetwork() != null) {
                  q.getPowerNetwork().removeVertex(q, true);
               }

               return true;
            }
         }
      }
   }

   private void removeSpatial(int x, int y) {
      if (x >= this.ax && y >= this.ay && x < this.ax + 32 && y < this.ay + 32) {
         this.spatial.remove((x - this.ax) * 32 + (y - this.ay));
      } else {
         Chunk c = this.layer.getChunk(x, y);
         if (c == null) {
            return;
         }

         c.spatial.remove((x - c.ax) * 32 + (y - c.ay));
      }
   }

   private void setSpatial(int x, int y, Structure<?> s) {
      if (x >= this.ax && y >= this.ay && x < this.ax + 32 && y < this.ay + 32) {
         this.spatial.put((x - this.ax) * 32 + (y - this.ay), s);
      } else {
         Chunk c = this.layer.getChunk(x, y);
         if (c == null) {
            return;
         }

         c.spatial.put((x - c.ax) * 32 + (y - c.ay), s);
      }
   }

   public boolean addStructure(Structure<?> s, boolean fromLoading) {
      if (!this.dataSet) {
         this.init();
      }

      s.layer = this.layer;
      synchronized (this.structLock) {
         int m = (s.x - this.ax) * 32 + (s.y - this.ay);
         if (s instanceof CopperCable) {
            if (this.cables.containsKey(m)) {
               return false;
            }

            this.cables.put(m, (CopperCable)s);
         } else {
            for (int i = 0; i < s.getWidth(); i++) {
               for (int j = 0; j < s.getHeight(); j++) {
                  if (this.layer.getStructure(s.x + i, s.y + j) != null) {
                     return false;
                  }
               }
            }

            if (!fromLoading) {
               for (int i = 0; i < s.getWidth(); i++) {
                  for (int jx = 0; jx < s.getHeight(); jx++) {
                     this.setSpatial(s.x + i, s.y + jx, s);
                  }
               }
            }

            this.maxX = Math.max(this.maxX, s.x + s.getWidth());
            this.maxY = Math.max(this.maxY, s.y + s.getHeight());
            this.structures.add(s);
         }

         if (!(s instanceof CopperCable)) {
            this.layer.setCollision(s, true);
         }

         if (!fromLoading) {
            this.layer.markDirty(s, 256);

            for (Dock d : s.getDocks()) {
               this.layer.setItemNotification(s.x + d.x + d.dir.dx, s.y + d.y + d.dir.dy);
            }
         }

         if (s instanceof StorageStructure) {
            this.layer.storages.add((StorageStructure)s);
         } else if (s instanceof Conveyor) {
            this.conveyors.add((Conveyor)s);
         }

         if (s.getSchema().powerDocks > 0) {
            PowerNetwork network = new PowerNetwork(Game.G.powerGrid);
            Game.G.powerGrid.addNetwork(network);
            network.addVertex(s);
         }
      }

      s.onPlacement(fromLoading);
      return true;
   }

   public IntMap<CopperCable> getCables() {
      return this.cables;
   }

   public Array<Structure<?>> getStructures() {
      return this.structures;
   }

   public int getItemCount() {
      synchronized (this.structLock) {
         int sum = 0;

         for (Structure<?> s : this.structures) {
            if (s instanceof Conveyor) {
               sum += ((Conveyor)s).getItemCount();
            }
         }

         return sum;
      }
   }

   public void stopSfx() {
      synchronized (this.structLock) {
         for (Structure<?> s : this.structures) {
            s.stopSfx();
         }
      }
   }

   public void dispose() {
      if (this.builder != null && this.builder.getMesh() != null) {
         this.builder.dispose();
      }

      this.glSet = false;
      this.dirty = true;
   }

   @Override
   public void save(NBT.Builder b) {
      if (!this.dataSet) {
         this.init();
      }

      b.Compound().Byte("x", (byte)this.x).Byte("y", (byte)this.y).ShortArray("data", this.data);
      synchronized (this.structLock) {
         b.List("Structures", NBT.TagType.Compound);

         for (Structure<?> s : this.structures) {
            s.save(b);
         }

         b.End();
         b.List("Cables", NBT.TagType.Compound);

         for (CopperCable s : this.cables.values()) {
            s.save(b);
         }

         b.End();
      }

      b.End();
   }

   public void postLoad() {
      for (Structure<?> s : this.structures) {
         for (int i = 0; i < s.getWidth(); i++) {
            for (int j = 0; j < s.getHeight(); j++) {
               this.setSpatial(s.x + i, s.y + j, s);
            }
         }
      }
   }

   public void postAllLayersLoad(boolean firstCycle) {
      if (firstCycle) {
         for (Structure<?> s : this.structures) {
            s.postLoad();
         }

         for (CopperCable s : this.cables.values()) {
            s.postLoad();
         }
      } else {
         for (Structure<?> s : this.structures) {
            s.reloadPowerNetwork();
         }
      }
   }

   private static void initTexCache() {
      HashMap<Tile.TileType, TextureRegion[]> stone = new HashMap<>();
      stone.put(
         Tile.TileType.Dirt,
         new TextureRegion[]{
            Quarry.Q.atlas.findRegion("tile_dirt_corner"),
            Quarry.Q.atlas.findRegion("tile_dirt_inner"),
            Quarry.Q.atlas.findRegion("tile_dirt_middle"),
            Quarry.Q.atlas.findRegion("tile_dirt_hole")
         }
      );
      stone.put(
         Tile.TileType.CrudeOil,
         new TextureRegion[]{
            Quarry.Q.atlas.findRegion("tile_crude_oil_corner"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_inner"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_middle"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_hole")
         }
      );
      HashMap<Tile.TileType, TextureRegion[]> dirt = new HashMap<>();
      dirt.put(
         Tile.TileType.CrudeOil,
         new TextureRegion[]{
            Quarry.Q.atlas.findRegion("tile_crude_oil_dirt_corner"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_dirt_inner"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_dirt_middle"),
            Quarry.Q.atlas.findRegion("tile_crude_oil_dirt_hole")
         }
      );
      texLookup.put(Tile.TileType.Stone, stone);
      texLookup.put(Tile.TileType.Dirt, dirt);
   }

   public void loadStructures(NBT.CompoundTag tag) throws NBT.NBTException {
      try {
         for (NBT.Tag t : tag.List("Structures", NBT.TagType.Compound).data) {
            try {
               Structure<?> s = Structure.load((NBT.CompoundTag)t);
               if (s != null) {
                  this.addStructure(s, true);
               }
            } catch (NBT.NBTException var6) {
               Quarry.Q.pi.message(1, var6);
            }
         }
      } catch (NBT.NBTException var8) {
         Quarry.Q.pi.message(1, var8);
      }

      try {
         for (NBT.Tag t : tag.List("Cables", NBT.TagType.Compound).data) {
            try {
               Structure<?> s = Structure.load((NBT.CompoundTag)t);
               if (s != null) {
                  this.addStructure(s, true);
               }
            } catch (NBT.NBTException var5) {
               Quarry.Q.pi.message(1, var5);
            }
         }
      } catch (NBT.NBTException var7) {
         Quarry.Q.pi.message(1, var7);
      }
   }

   public static Chunk load(NBT.CompoundTag tag, Layer layer) throws NBT.NBTException {
      Chunk c = new Chunk(tag.Byte("x"), tag.Byte("y"), layer);
      c.init();
      c.data = new short[1024];
      System.arraycopy(tag.ShortArray("data"), 0, c.data, 0, c.data.length);
      return c;
   }
}
