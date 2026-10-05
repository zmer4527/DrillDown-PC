package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.CopyRegionHelper;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class PowerPole extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.PowerPole,
         true,
         1,
         1,
         "powerpole",
         new Item.Items(Item.ItemType.SteelPlate, 25, Item.ItemType.TinPlate, 15, Item.ItemType.SteelCable, 60, Item.ItemType.Brick, 20),
         null
      )
      .<Schema>sciences(Science.ScienceType.HighPower)
      .flags(Schema.Flags.NotRotatable);
   static final float range = 5.5F;
   PowerPoleGhost aGhost;
   PowerPoleGhost bGhost;
   PowerPole cachedEnd;
   Structure<?> a;
   Structure<?> b;
   int aIndex;
   int bIndex;
   int aGhostIndex;
   int bGhostIndex;

   public PowerPole(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void postLoad() {
      super.postLoad();
      if (this.aIndex > -1) {
         this.a = this.layer.getStructure(this.aIndex);
      }

      if (this.bIndex > -1) {
         this.b = this.layer.getStructure(this.bIndex);
      }

      if (this.aGhostIndex > -1) {
         try {
            this.aGhost = (PowerPoleGhost)this.layer.getStructure(this.aGhostIndex);
         } catch (Exception var3) {
            this.aGhost = null;
            this.a = null;
         }
      }

      if (this.bGhostIndex > -1) {
         try {
            this.bGhost = (PowerPoleGhost)this.layer.getStructure(this.bGhostIndex);
         } catch (Exception var2) {
            this.bGhost = null;
            this.b = null;
         }
      }
   }

   public void cleanConnections() {
      if (this.layer != null) {
         if (this.a != null && this.layer.getStructure(this.a.x * this.layer.height + this.a.y) != this.a) {
            this.a = null;
            if (this.aGhost != null) {
               this.layer.removeStructure(this.aGhost);
               this.aGhost = null;
            }
         }

         if (this.b != null && this.layer.getStructure(this.b.x * this.layer.height + this.b.y) != this.b) {
            this.b = null;
            if (this.bGhost != null) {
               this.layer.removeStructure(this.bGhost);
               this.bGhost = null;
            }
         }
      }
   }

   protected void tryToConnectTo(Structure<?> s) {
      if (s != null && s != this && (this.a == null || this.b == null) && s != this.a && s != this.b) {
         if (s instanceof PowerPole) {
            PowerPole p = (PowerPole)s;
            if (p.a != null && p.b != null) {
               return;
            }

            p.cleanConnections();
            if (p.a == null) {
               if (this.a == null) {
                  p.a = this;
                  this.a = p;
               } else {
                  p.a = this;
                  this.b = p;
               }
            } else if (this.a == null) {
               p.b = this;
               this.a = p;
            } else {
               p.b = this;
               this.b = p;
            }
         } else if (s.getSchema().powerDocks > 0) {
            float minDistance = 5.5F;
            Dock dock = null;

            for (Dock d : s.getDocks()) {
               if (d.type == Dock.DockType.BigPower && this.layer.getAny(s.x + d.x + d.dir.dx, s.y + d.y + d.dir.dy, true) == null) {
                  float dist = Vector2.dst((float)(s.x + d.x + d.dir.dx), (float)(s.y + d.y + d.dir.dy), (float)this.x, (float)this.y);
                  if (dist <= minDistance) {
                     minDistance = dist;
                     dock = d;
                  }
               }
            }

            if (dock != null) {
               if (this.a == null) {
                  this.a = s;
                  this.aGhost = new PowerPoleGhost(s.x + dock.x + dock.dir.dx, s.y + dock.y + dock.dir.dy, this, s);
                  this.layer.addStructure(this.aGhost);
                  return;
               }

               this.b = s;
               this.bGhost = new PowerPoleGhost(s.x + dock.x + dock.dir.dx, s.y + dock.y + dock.dir.dy, this, s);
               this.layer.addStructure(this.bGhost);
               return;
            }
         }
      }
   }

   protected void updateConnections() {
      if (this.layer != null) {
         this.cleanConnections();
         if (this.b == null || this.a == null) {
            for (int i = 1; (float)i < 5.5F; i++) {
               for (int x = -i; x <= i; x++) {
                  for (int y = -i; y <= i; y++) {
                     if (!(Math.sqrt((double)(x * x + y * y)) <= (double)((float)i - 0.5F))
                        && !(Math.sqrt((double)(x * x + y * y)) > (double)((float)i + 0.5F))) {
                        if (this.b != null && this.a != null) {
                           return;
                        }

                        Structure<?> s = this.layer.getStructure((this.x + x) * this.layer.height + this.y + y);
                        if (s != null) {
                           this.tryToConnectTo(s);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      if (!dirtyBounds.isEmpty()) {
         this.cachedEnd = null;
         this.updateConnections();
      }
   }

   public PowerPole walkToEnd() {
      if (this.cachedEnd != null) {
         return this.cachedEnd;
      } else {
         PowerPole active = this;
         PowerPole prev = null;

         while (active != null) {
            if (active.a instanceof PowerPole && active.a != prev) {
               prev = active;
               active = (PowerPole)active.a;
            } else {
               if (!(active.b instanceof PowerPole) || active.b == prev) {
                  break;
               }

               prev = active;
               active = (PowerPole)active.b;
            }
         }

         active.cachedEnd = this;
         this.cachedEnd = active;
         return active;
      }
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      this.updateConnections();
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (Game.G.activeStructure == this || this.clicked) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(1.0F, 0.498F, 0.314F, 0.2F);
         shaper.circle(((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F, ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F, 352.0F);
      }

      if (this.a != null) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(0.33F, 0.33F, 0.33F, 1.0F);
         if (this.aGhost == null) {
            shaper.rectLine(
               ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F,
               ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F,
               ((float)this.a.x + (float)this.a.getWidth() / 2.0F) * 64.0F,
               ((float)this.a.y + (float)this.a.getHeight() / 2.0F) * 64.0F,
               4.0F
            );
         } else {
            shaper.rectLine(
               ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F,
               ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F,
               ((float)this.aGhost.x + (float)this.aGhost.getWidth() / 2.0F) * 64.0F,
               ((float)this.aGhost.y + (float)this.aGhost.getHeight() / 2.0F) * 64.0F,
               4.0F
            );
         }
      }

      if (this.b != null) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(0.33F, 0.33F, 0.33F, 1.0F);
         if (this.b instanceof PowerPole && ((PowerPole)this.b).b == this) {
            shaper.rectLine(
               ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F,
               ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F,
               ((float)this.b.x + (float)this.b.getWidth() / 2.0F) * 64.0F,
               ((float)this.b.y + (float)this.b.getHeight() / 2.0F) * 64.0F,
               4.0F
            );
         } else if (this.bGhost != null) {
            shaper.rectLine(
               ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F,
               ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F,
               ((float)this.bGhost.x + (float)this.bGhost.getWidth() / 2.0F) * 64.0F,
               ((float)this.bGhost.y + (float)this.bGhost.getHeight() / 2.0F) * 64.0F,
               4.0F
            );
         }
      }

      if (Game.DRAW_DEBUG) {
         if (this.cachedEnd != null) {
            shaper.setColor(Color.YELLOW);
            shaper.rectLine(
               ((float)this.x + 0.5F) * 64.0F,
               ((float)this.y + 0.5F) * 64.0F,
               ((float)this.cachedEnd.x + 0.5F) * 64.0F,
               ((float)this.cachedEnd.y + 0.5F) * 64.0F,
               3.0F
            );
         } else {
            shaper.setColor(Color.YELLOW);
            shaper.circle(((float)this.x + 0.5F) * 64.0F, ((float)this.y + 0.5F) * 64.0F, 3.0F);
         }
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.aGhost != null) {
         this.layer.removeStructure(this.aGhost);
      }

      if (this.bGhost != null) {
         this.layer.removeStructure(this.bGhost);
      }
   }

   public Structure<?> getA() {
      return this.a;
   }

   public PowerPoleGhost getAGhost() {
      return this.aGhost;
   }

   public Structure<?> getB() {
      return this.b;
   }

   public PowerPoleGhost getBGhost() {
      return this.bGhost;
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Int("a", this.a == null ? -1 : this.a.x * this.layer.height + this.a.y)
         .Int("b", this.b == null ? -1 : this.b.x * this.layer.height + this.b.y)
         .Int("aGhost", this.aGhost == null ? -1 : this.aGhost.x * this.layer.height + this.aGhost.y)
         .Int("bGhost", this.bGhost == null ? -1 : this.bGhost.x * this.layer.height + this.bGhost.y);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.aIndex = tag.Int("a", -1);
      this.bIndex = tag.Int("b", -1);
      this.aGhostIndex = tag.Int("aGhost", -1);
      this.bGhostIndex = tag.Int("bGhost", -1);
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Int("a", this.a == null ? -1 : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.a.x * this.layer.height + this.a.y))
         .Int("b", this.b == null ? -1 : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.b.x * this.layer.height + this.b.y))
         .Int("aGhost", this.aGhost == null ? -1 : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.aGhost.x * this.layer.height + this.aGhost.y))
         .Int("bGhost", this.bGhost == null ? -1 : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.bGhost.x * this.layer.height + this.bGhost.y));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.a = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("a", 0));
      this.b = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("b", 0));
      Structure<?> s = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("aGhost", 0));
      if (s instanceof PowerPoleGhost) {
         this.aGhost = (PowerPoleGhost)s;
      } else {
         this.aGhost = null;
      }

      s = CopyRegionHelper.getStructure(this, pasteRegion, tag.Int("bGhost", 0));
      if (s instanceof PowerPoleGhost) {
         this.bGhost = (PowerPoleGhost)s;
      } else {
         this.bGhost = null;
      }
   }
}
