package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.CopyRegionHelper;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.Arrays;
import java.util.HashSet;

public class CopperCable extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0, StructureType.CopperCable, false, 1, 1, "copper_cable", new Item.Items(Item.ItemType.CopperWire, 1), null
      )
      .<Schema>sciences(Science.ScienceType.Electricity)
      .flags(Schema.Flags.Draggable, Schema.Flags.NoDustEffect, Schema.Flags.NotRotatable);
   static final TextureRegion ew = Quarry.Q.atlas.findRegion("structure_copper_cable_ew");
   static final TextureRegion es = Quarry.Q.atlas.findRegion("structure_copper_cable_es");
   static final int plus = 36;
   static final int minus = 28;
   Dock.ConnectionType[] dirs = new Dock.ConnectionType[4];
   int directions = 0;
   Structure<?>[] structures = new Structure[4];
   CopperCable cachedEnd;
   int[] structs;

   public CopperCable(int x, int y) {
      super(x, y, classSchema);
      Arrays.fill(this.dirs, Dock.ConnectionType.No);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      if (this.layer == null) {
         this.directions = 0;
         Arrays.fill(this.dirs, Dock.ConnectionType.No);
         Arrays.fill(this.structures, null);
         this.updateFacing(true);
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      boolean hasFlag = dirtyBounds.hasAnyFlag(68);
      if (hasFlag) {
         this.cachedEnd = null;
      }

      if (this.layer == null) {
         this.updateFacing(true);
      } else if (dirtyBounds.touches(this)) {
         this.updateFacing(true);
      }
   }

   @Override
   public void postLoad() {
      if (this.structs != null) {
         for (int i = 0; i < 4; i++) {
            this.structures[i] = this.structs[i] == -1 ? null : this.layer.getAny(this.structs[i], false);
            if (this.dirs[i] == Dock.ConnectionType.No) {
               this.structures[i] = null;
            }
         }

         this.structs = null;
      }

      this.updateFacing(true);
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      if (this.directions > 0) {
         int off = this.directions < 2 ? 4 : 0;

         for (Direction d : Direction.values) {
            if (this.dirs[d.ordinal()] != Dock.ConnectionType.No) {
               int mx = this.x * 64;
               int my = this.y * 64;
               int rot = 0;
               switch (d) {
                  case East:
                     mx += 36 + off;
                     my += 28;
                     break;
                  case North:
                     mx += 36;
                     my += 36 + off;
                     rot = 90;
                     break;
                  case South:
                     rot = -90;
                     mx += 28;
                     my += 28 - off;
                     break;
                  case West:
                     my += 28;
               }

               spriter.add(ew, (float)mx, (float)my, -2.0F, 0.0F, 0.0F, (float)(28 - off), 8.0F, 1.0F, 1.0F, (float)rot);
            }
         }

         int mx = this.x * 64;
         int my = this.y * 64;
         int rot = 0;
         TextureRegion filler = null;
         if (this.dirs[0] != Dock.ConnectionType.No && this.dirs[1] != Dock.ConnectionType.No) {
            filler = es;
            mx += 36;
            my += 28;
            rot = 90;
         }

         if (this.dirs[1] != Dock.ConnectionType.No && this.dirs[2] != Dock.ConnectionType.No) {
            filler = es;
            mx += 28;
            my += 28;
            rot = 0;
         }

         if (this.dirs[0] != Dock.ConnectionType.No && this.dirs[3] != Dock.ConnectionType.No) {
            filler = es;
            mx += 36;
            my += 36;
            rot = 180;
         }

         if (this.dirs[2] != Dock.ConnectionType.No && this.dirs[3] != Dock.ConnectionType.No) {
            filler = es;
            mx += 28;
            my += 36;
            rot = -90;
         }

         if (this.dirs[0] != Dock.ConnectionType.No && this.dirs[2] != Dock.ConnectionType.No) {
            mx = this.x * 64 + 36;
            my = this.y * 64 + 28;
            filler = ew;
            rot = 90;
         }

         if (this.dirs[1] != Dock.ConnectionType.No && this.dirs[3] != Dock.ConnectionType.No) {
            mx = this.x * 64 + 28;
            my = this.y * 64 + 28;
            filler = ew;
         }

         if (filler != null) {
            spriter.add(filler, (float)mx, (float)my, -2.0F, 0.0F, 0.0F, 8.0F, 8.0F, 1.0F, 1.0F, (float)rot);
         }
      }

      if (this.directions <= 1) {
         int w = 16;
         float var10002 = (float)(this.x * 64 + 24);
         float var10003 = (float)(this.y * 64 + 24);
         spriter.add(this.getSchema().tex, var10002, var10003, -1.9F, (float)w, (float)w);
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
   }

   public boolean connectTo(Structure<?> s, Direction myDirectionToS) {
      if (this.structures[myDirectionToS.ordinal()] != null && this.structures[myDirectionToS.ordinal()].equals(s)) {
         return true;
      } else if (this.directions == 2) {
         return false;
      } else {
         Dock.ConnectionType old = this.dirs[myDirectionToS.ordinal()];
         this.dirs[myDirectionToS.ordinal()] = Dock.ConnectionType.Open;
         this.structures[myDirectionToS.ordinal()] = s;
         if (old == Dock.ConnectionType.No) {
            this.directions++;
         }

         this.setDirty();
         return true;
      }
   }

   public void updateFacing(boolean propagate) {
      boolean changed = false;

      for (Direction d : Direction.values) {
         int ord = d.ordinal();
         Structure<?> s = null;
         if (this.layer == null) {
            s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + d.dx) * Game.G.layer.height + this.y + d.dy);
         } else if (s == null) {
            s = this.layer.getAny(this.x + d.dx, this.y + d.dy, false);
         }

         if (s == this || s != null && !(s instanceof CopperCable) && s.getSchema().powerDocks == 0) {
            s = null;
         }

         if (this.structures[ord] != null) {
            if (s == null) {
               changed = true;
               this.structures[ord] = null;
               this.dirs[ord] = Dock.ConnectionType.No;
               this.directions = Math.max(0, this.directions - 1);
            } else if (this.structures[ord] != s) {
               Dock.ConnectionType t = this.getConnection(s, d);
               if (this.dirs[ord] != Dock.ConnectionType.No) {
                  if (t == Dock.ConnectionType.No) {
                     this.structures[ord] = null;
                     this.dirs[ord] = Dock.ConnectionType.No;
                     this.directions = Math.max(0, this.directions - 1);
                     changed = true;
                  } else {
                     this.structures[ord] = s;
                     this.dirs[ord] = t;
                  }
               }
            } else {
               if (propagate && this.structures[ord] instanceof CopperCable && this.structures[ord].layer != null) {
                  ((CopperCable)s).updateFacing(false);
               }

               Dock.ConnectionType t = this.getConnection(s, d);
               if (this.dirs[ord] != Dock.ConnectionType.No && t == Dock.ConnectionType.No) {
                  this.dirs[ord] = t;
                  this.directions = Math.max(0, this.directions - 1);
                  this.structures[ord] = null;
                  changed = true;
                  if (s instanceof CopperCable && propagate && this.layer != null) {
                     ((CopperCable)s).updateFacing(false);
                  }
               }
            }
         }
      }

      if (this.directions < 2) {
         label118:
         for (int i = 0; i < 2; i++) {
            for (Direction d : Direction.values) {
               if (this.directions >= 2) {
                  break label118;
               }

               int ordx = d.ordinal();
               Structure<?> sx = this.structures[ordx];
               if (sx == null) {
                  if (this.layer == null) {
                     sx = (Structure<?>)Game.G.activeStructureTrail.get((this.x + d.dx) * Game.G.layer.height + this.y + d.dy);
                  } else if (sx == null) {
                     sx = this.layer.getAny(this.x + d.dx, this.y + d.dy, false);
                  }
               }

               Dock.ConnectionType p = this.dirs[ordx];
               if (i == 0 && sx instanceof CopperCable || i == 1 && sx != null) {
                  Dock.ConnectionType t = this.getConnection(sx, d);
                  if (p != t && t != Dock.ConnectionType.No && (!(sx instanceof CopperCable) || ((CopperCable)sx).connectTo(this, d.inv()))) {
                     this.dirs[ordx] = t;
                     this.structures[ordx] = sx;
                     if (p == Dock.ConnectionType.No) {
                        this.directions++;
                     }

                     changed = true;
                  }
               }
            }
         }
      }

      if (changed) {
         this.setDirty();
      }
   }

   public int getDirections() {
      return this.directions;
   }

   public CopperCable walkToEnd() {
      if (this.cachedEnd != null) {
         return this.cachedEnd;
      } else {
         CopperCable c = this;
         int prevDirection = -1;
         HashSet<CopperCable> seen = new HashSet<>();

         while (c != null) {
            for (int i = 0; i < 4; i++) {
               if (i != prevDirection
                  && c.dirs[i] == Dock.ConnectionType.Open
                  && c.structures[i] instanceof CopperCable
                  && !Game.G.activeStructureTrail.containsValue(c.structures[i], true)
                  && !seen.contains(c)) {
                  seen.add(c);
                  c = (CopperCable)c.structures[i];
                  prevDirection = (i + 2) % 4;
                  break;
               }
            }
            break;
         }

         this.cachedEnd = c;
         return c;
      }
   }

   protected Dock.ConnectionType getConnection(Structure<?> s, Direction d) {
      if (s.getSchema().type == StructureType.CopperCable) {
         CopperCable c = (CopperCable)s;
         if (c.dirs[d.inv().ordinal()] == Dock.ConnectionType.Open) {
            return Dock.ConnectionType.Open;
         } else if (c.dirs[d.inv().ordinal()] == Dock.ConnectionType.Dock) {
            return Dock.ConnectionType.No;
         } else {
            return c.directions < 2 ? Dock.ConnectionType.Open : Dock.ConnectionType.No;
         }
      } else {
         for (Dock k : s.getDocks()) {
            if (k.type == Dock.DockType.Power && s.x + k.x + k.dir.dx == this.x && s.y + k.y + k.dir.dy == this.y && d == k.dir.inv()) {
               return Dock.ConnectionType.Dock;
            }
         }

         return Dock.ConnectionType.No;
      }
   }

   public Structure<?>[] getStructures() {
      return this.structures;
   }

   @Override
   public void saveData(NBT.Builder b) {
      super.saveData(b);
      byte[] dir = new byte[4];
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         dir[i] = this.dirs[i].value;
         str[i] = this.structures[i] == null ? -1 : this.structures[i].x * this.layer.height + this.structures[i].y;
      }

      b.ByteArray("dirs", dir).IntArray("structs", str);
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      byte[] dir = new byte[4];
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         int tx = this.x + Direction.values[i].dx;
         int ty = this.y + Direction.values[i].dy;
         if (tx >= copyRegion[0] && ty >= copyRegion[1] && tx < copyRegion[0] + copyRegion[2] && ty < copyRegion[1] + copyRegion[3]) {
            dir[i] = this.dirs[i].value;
            str[i] = this.structures[i] == null
               ? -1
               : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.structures[i].x * this.layer.height + this.structures[i].y);
         } else {
            dir[i] = Dock.ConnectionType.No.value;
            str[i] = -1;
         }
      }

      b.ByteArray("dirs", dir).IntArray("structs", str);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);

      try {
         byte[] dir = tag.ByteArray("dirs");

         for (int i = 0; i < 4; i++) {
            this.dirs[i] = Dock.connectionTypes[dir[i]];
         }

         int[] str = tag.IntArray("structs");

         for (int i = 0; i < 4; i++) {
            this.structures[i] = CopyRegionHelper.getAny(this, pasteRegion, str[i], false);
         }
      } catch (NBT.NBTException var7) {
         Quarry.Q.pi.message(1, var7);
      }

      this.directions = 0;

      for (Dock.ConnectionType b : this.dirs) {
         if (b != Dock.ConnectionType.No) {
            this.directions++;
         }
      }

      this.cachedEnd = null;
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);

      try {
         byte[] dir = tag.ByteArray("dirs");

         for (int i = 0; i < 4; i++) {
            this.dirs[i] = Dock.connectionTypes[dir[i]];
         }

         this.structs = tag.IntArray("structs");
      } catch (NBT.NBTException var6) {
         Quarry.Q.pi.message(1, var6);
      }

      this.directions = 0;

      for (Dock.ConnectionType b : this.dirs) {
         if (b != Dock.ConnectionType.No) {
            this.directions++;
         }
      }
   }
}
