package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Pools;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.ItemEntity;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.IRotatable;
import de.dakror.quarry.structure.base.ITube;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.CopyRegionHelper;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public class Conveyor extends Structure<Conveyor.ConveyorSchema> implements IRotatable, ITube {
   public static final Conveyor.ConveyorSchema classSchema = new Conveyor.ConveyorSchema(
         0, StructureType.Conveyor, true, 1, 1, "conveyor", new Item.Items(Item.ItemType.Stone, 1), null
      )
      .<Schema>flags(new Schema.Flags[]{Schema.Flags.Draggable, Schema.Flags.NoDustEffect})
      .loudness(0.5F);
   protected Direction dir;
   static final int s = 40;
   static final int w = 18;
   public static float k = 9.142858F;
   Structure<?>[] structures = new Structure[4];
   final Object itemLock = new Object();
   ItemEntity[] items = new ItemEntity[14];
   int itemCount = 0;
   boolean itemChanges;
   boolean notification;
   int[] structs;
   boolean touches;
   static TextureRegion caret;

   public Conveyor(int x, int y) {
      super(x, y, classSchema);
      this.dir = Direction.East;
   }

   protected Conveyor(int x, int y, Conveyor.ConveyorSchema schema) {
      super(x, y, schema);
      this.dir = Direction.East;
   }

   public Conveyor(int x, int y, Direction direction) {
      super(x, y, classSchema);
      this.dir = direction;
   }

   public Structure<?> getStructureInDirection(Direction dir) {
      if (dir == this.dir.inv()) {
         return this.structures[0];
      } else if (dir == this.dir.next()) {
         return this.structures[1];
      } else if (dir == this.dir.prev()) {
         return this.structures[2];
      } else {
         return dir == this.dir ? this.structures[3] : null;
      }
   }

   public boolean moveItem(ItemEntity e, Direction dir, int deltaSlot, boolean chain) {
      if (dir == null) {
         synchronized (this.itemLock) {
            if (this.isItemSlotFree(e.slot + deltaSlot)) {
               this.items[e.slot] = null;
               this.items[e.slot + deltaSlot] = e;
               e.lastSlot = e.slot;
               e.slot += deltaSlot;
               e.x = this.x;
               e.y = this.y;
               this.itemChanges = true;
               this.notifyNeighbors(chain);
               return true;
            } else {
               return false;
            }
         }
      } else {
         Structure<?> s = this.getStructureInDirection(dir);
         if (s instanceof Conveyor) {
            Conveyor c = (Conveyor)s;
            synchronized (this.itemLock) {
               boolean var10000;
               synchronized (c.itemLock) {
                  if (c.isItemSlotFree(e.slot + deltaSlot)) {
                     this.items[e.slot] = null;
                     c.items[e.slot + deltaSlot] = e;
                     e.lastSlot = e.slot;
                     e.slot += deltaSlot;
                     e.x = c.x;
                     e.y = c.y;
                     this.itemCount--;
                     c.itemCount++;
                     this.itemChanges = true;
                     c.itemChanges = true;
                     this.notifyNeighbors(chain);
                     return true;
                  }

                  var10000 = false;
               }

               return var10000;
            }
         } else {
            return false;
         }
      }
   }

   public boolean isItemSlotFree(int slot) {
      return slot != 3 && slot != 10 ? this.items[slot] == null : this.items[3] == null && this.items[10] == null;
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      this.touches = dirtyBounds.touches(this);
      if (this.touches || this.layer == null) {
         this.updateStructures();
      }
   }

   public void updateItems(double deltaTime, int gameSpeed, boolean chain) {
      this.updateItems(deltaTime, gameSpeed, this.touches, chain);
   }

   protected void updateItemWithin(ItemEntity e, boolean chain) {
      if (e.dir != Direction.South && e.dir != Direction.North) {
         if (e.dir == Direction.West || e.dir == Direction.East) {
            if (e.slot >= 7) {
               if (this.moveItem(e, null, e.dir.dx, chain)) {
                  e.interp--;
               }
            } else if (e.slot != 3) {
               if (this.moveItem(e, null, e.slot > 3 ? -1 : 1, chain)) {
                  e.interp--;
               }
            } else if (this.moveItem(e, null, 7 + e.dir.dx, chain)) {
               e.interp--;
            }
         }
      } else if (e.slot < 7) {
         if (this.moveItem(e, null, e.dir.dy, chain)) {
            e.interp--;
         }
      } else if (e.slot != 10) {
         if (this.moveItem(e, null, e.slot > 10 ? -1 : 1, chain)) {
            e.interp--;
         }
      } else if (this.moveItem(e, null, -7 + e.dir.dy, chain)) {
         e.interp--;
      }
   }

   public void updateItems(double deltaTime, int gameSpeed, boolean touch, boolean chain) {
      if (this.itemChanges || touch || this.notification) {
         this.itemChanges = false;
         synchronized (this.itemLock) {
            for (int i = 0; i < this.items.length; i++) {
               ItemEntity e = this.items[i];
               if (e != null) {
                  if (e.item == null) {
                     System.out.println("Deleting nonsense item");
                     this.items[i] = null;
                     this.itemChanges = true;
                     Pools.free(e);
                  } else if (e.dir != null || this.notification || touch) {
                     if (touch || this.notification) {
                        Direction nd = this.getCurrentDirection(e.item, e.dir, e.z, e.slot);
                        float nz = this.getItemZ(e.item, nd);
                        if (nd != e.dir || nz != e.z) {
                           this.itemChanges = true;
                           e.dir = nd;
                           e.z = nz;
                        }
                     }

                     if (e.interp >= 1.0) {
                        boolean atEdge = false;
                        if (e.slot == 0 && e.dir == Direction.South) {
                           atEdge = true;
                        } else if (e.slot == 6 && e.dir == Direction.North) {
                           atEdge = true;
                        } else if (e.slot == 7 && e.dir == Direction.West) {
                           atEdge = true;
                        } else if (e.slot == 13 && e.dir == Direction.East) {
                           atEdge = true;
                        } else {
                           this.updateItemWithin(e, chain);
                        }

                        if (atEdge || e.dir == null) {
                           Structure<?> s = this;
                           if (e.dir != null) {
                              s = this.getStructureInDirection(e.dir);
                           }

                           boolean blocked = false;
                           if (atEdge && e.dir != null) {
                              if (e.slot == 0) {
                                 if (this.moveItem(e, e.dir, 6, chain)) {
                                    e.interp--;
                                 } else {
                                    blocked = true;
                                 }
                              } else if (e.slot == 6) {
                                 if (this.moveItem(e, e.dir, -6, chain)) {
                                    e.interp--;
                                 } else {
                                    blocked = true;
                                 }
                              } else if (e.slot == 7) {
                                 if (this.moveItem(e, e.dir, 6, chain)) {
                                    e.interp--;
                                 } else {
                                    blocked = true;
                                 }
                              } else if (e.slot == 13) {
                                 if (this.moveItem(e, e.dir, -6, chain)) {
                                    e.interp--;
                                 } else {
                                    blocked = true;
                                 }
                              }
                           }

                           if (s instanceof Conveyor) {
                              if (!blocked) {
                                 e.dir = ((Conveyor)s).getNextDirection(e.item, e.dir, e.z, e.slot);
                                 e.z = ((Conveyor)s).getItemZ(e.item, e.dir);
                              }
                           } else if (s != null && s.canAccept(e.item, e.x, e.y, e.dir) && s.acceptItem(e.item, e.src, e.dir)) {
                              this.items[e.slot] = null;
                              Pools.free(e);
                              this.itemCount--;
                              this.itemChanges = true;
                              this.notifyNeighbors(chain);
                           } else {
                              e.dir = null;
                           }
                        }
                     } else {
                        e.interp += deltaTime * 5.0 * (double)gameSpeed;
                        this.itemChanges = true;
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.itemCount > 0) {
         for (ItemEntity e : this.items) {
            if (e != null && e.src != null) {
               if (!e.src.isDestroyed()) {
                  e.src.putBack(e.item, 1);
               }

               this.items[e.slot] = null;
               Pools.free(e);
            }
         }
      }
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      int flag = 0;

      for (int i = 0; i < 4; i++) {
         if (this.structures[i] != null) {
            flag |= 1 << i;
         }
      }

      TextureRegion tex = this.getSchema().tex;
      int flip = 1;
      switch (flag) {
         case 1:
         case 8:
         case 9:
            tex = this.schema.texWE;
            break;
         case 2:
         case 10:
            tex = this.schema.texNE;
            flip = -1;
            break;
         case 3:
         case 11:
            tex = this.schema.texWNE;
            flip = -1;
            break;
         case 4:
         case 12:
            tex = this.schema.texNE;
            break;
         case 5:
         case 13:
            tex = this.schema.texWNE;
            break;
         case 6:
         case 14:
            tex = this.schema.texNSE;
            break;
         case 7:
         case 15:
            tex = this.schema.texWNSE;
      }

      spriter.add(tex, (float)(this.x * 64), (float)(this.y * 64), -5.0F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, (float)flip, (float)this.dir.rot);
   }

   protected void notifyNeighbors(boolean chain) {
      if (this.structures[0] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.dir))) {
         ((Conveyor)this.structures[0]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[0]).updateItems(0.0, 1, true, false);
         }
      }

      if (this.structures[1] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.dir.prev()))) {
         ((Conveyor)this.structures[1]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[1]).updateItems(0.0, 1, true, false);
         }
      }

      if (this.structures[2] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.dir.next()))) {
         ((Conveyor)this.structures[2]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[2]).updateItems(0.0, 1, true, false);
         }
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.clicked || Game.G.activeStructure != null && Game.G.activeStructure == this && this.getSchema().type == StructureType.Hopper) {
         if (caret == null) {
            caret = Quarry.Q.atlas.findRegion("caret_down");
         }

         spriter.add(
            caret,
            ((float)this.x + 0.5F) * 64.0F - 6.0F,
            ((float)this.y + 0.5F) * 64.0F - 2.5F,
            -1.0F,
            6.0F,
            2.5F,
            12.0F,
            5.0F,
            1.0F,
            1.0F,
            (float)(this.dir.rot + 90)
         );
      }

      if (this.itemCount > 0) {
         this.drawItems(spriter);
      }

      if (Game.DRAW_DEBUG) {
         synchronized (this.itemLock) {
            shaper.set(ShapeType.Line);

            for (ItemEntity e : this.items) {
               if (e != null) {
                  shaper.setColor(e.z == -4.0F ? Color.GREEN : Color.YELLOW);
                  float ix = 0.0F;
                  float iy = 0.0F;
                  if (e.slot < 7) {
                     ix = ((float)e.x + 0.5F) * 64.0F - k / 2.0F;
                     iy = (float)(e.y * 64) + (float)e.slot * k;
                  } else {
                     ix = (float)(e.x * 64) + (float)(e.slot - 7) * k;
                     iy = ((float)e.y + 0.5F) * 64.0F - k / 2.0F;
                  }

                  if (e.slot == 3) {
                     shaper.circle(ix + k / 2.0F, iy + k / 2.0F, k / 2.0F, 16);
                  } else {
                     shaper.rect(ix + 1.0F, iy + 1.0F, k - 2.0F, k - 2.0F);
                  }

                  if (e.dir == null) {
                     shaper.x(ix + k / 2.0F, iy + k / 2.0F, k / 4.0F);
                  } else {
                     shaper.line(
                        ix + k / 2.0F, iy + k / 2.0F, ix + k / 2.0F + k / 3.0F * 2.0F * (float)e.dir.dx, iy + k / 2.0F + k / 3.0F * 2.0F * (float)e.dir.dy
                     );
                  }
               }
            }
         }
      }
   }

   public void drawItems(SpriteRenderer spriter) {
      synchronized (this.itemLock) {
         for (ItemEntity e : this.items) {
            if (e != null) {
               float ix = 0.0F;
               float iy = 0.0F;
               Direction dir = null;
               if (e.dir != null && e.lastSlot > -1) {
                  dir = e.dir;
                  int x = e.x;
                  int y = e.y;
                  if (Math.abs(e.lastSlot - e.slot) > 1) {
                     if (e.lastSlot == 0 && e.slot == 6) {
                        y++;
                        dir = Direction.South;
                     } else if (e.lastSlot == 6 && e.slot == 0) {
                        y--;
                        dir = Direction.North;
                     } else if (e.lastSlot == 13 && e.slot == 7) {
                        x--;
                        dir = Direction.East;
                     } else if (e.lastSlot == 7 && e.slot == 13) {
                        x++;
                        dir = Direction.West;
                     }
                  } else if ((dir == Direction.South || dir == Direction.North) && e.lastSlot >= 7 && e.lastSlot != 10) {
                     dir = e.lastSlot > 10 ? Direction.West : Direction.East;
                  } else if ((dir == Direction.West || dir == Direction.East) && e.lastSlot < 7 && e.lastSlot != 3) {
                     dir = e.lastSlot > 3 ? Direction.South : Direction.North;
                  }

                  if (e.lastSlot < 7) {
                     ix = ((float)x + 0.5F) * 64.0F - 12.0F;
                     iy = (float)(y * 64) + (float)e.lastSlot * k - (24.0F - k) / 2.0F;
                  } else {
                     ix = (float)(x * 64) + (float)(e.lastSlot - 7) * k - (24.0F - k) / 2.0F;
                     iy = ((float)y + 0.5F) * 64.0F - 12.0F;
                  }

                  double prog = MathUtils.clamp(e.interp, 0.0, 1.0);
                  ix = (float)((double)ix + (double)k * prog * (double)dir.dx);
                  iy = (float)((double)iy + (double)k * prog * (double)dir.dy);
               } else if (e.slot < 7) {
                  ix = ((float)e.x + 0.5F) * 64.0F - 12.0F;
                  iy = (float)(e.y * 64) + (float)e.slot * k - (24.0F - k) / 2.0F;
               } else {
                  ix = (float)(e.x * 64) + (float)(e.slot - 7) * k - (24.0F - k) / 2.0F;
                  iy = ((float)e.y + 0.5F) * 64.0F - 12.0F;
               }

               float newZ = e.z + ix / (float)(this.layer.width * 64) - iy / (float)(this.layer.height * 64);
               if (e.item.stackable != null) {
                  spriter.add(e.item.stackable.icon, ix + 5.0F, iy + 5.0F, newZ, 14.0F, 14.0F);
               }

               spriter.add(e.item.icon, ix, iy, newZ, 24.0F, 24.0F);
            }
         }
      }
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      if (!fromLoading) {
         this.updateStructures();
      }
   }

   public void setItemNotification() {
      this.notification = true;
   }

   protected Structure<?> getNeighbor(Direction dir) {
      Layer l = this.layer == null ? Game.G.layer : this.layer;
      Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + dir.dx) * l.height + this.y + dir.dy);
      if (s == null) {
         s = l.getStructure(this.x + dir.dx, this.y + dir.dy);
      }

      return s != null && this.isDockOrConveyor(s, dir) ? s : null;
   }

   public void updateStructures() {
      Structure<?> from = this.getNeighbor(this.dir.inv());
      if (from == null) {
         this.structures[0] = from;
      } else if (from instanceof Conveyor) {
         if (((Conveyor)from).getDirection() != this.dir && (!(from instanceof ConveyorBridge) || ((ConveyorBridge)from).getDirection2() != this.dir)) {
            this.structures[0] = null;
         } else {
            this.structures[0] = from;
         }
      } else {
         this.structures[0] = from;
      }

      Structure<?> to = this.getNeighbor(this.dir);
      if (to != null
         && (!(to instanceof Conveyor) || ((Conveyor)to).getDirection() != this.dir.inv())
         && (!(to instanceof ConveyorBridge) || ((ConveyorBridge)to).getDirection2() != this.dir.inv())
         && (
            !(to instanceof ElectricConveyorCore)
               || ((ElectricConveyorCore)to).getDirection() == this.dir
               || ((ElectricConveyorCore)to).getDirection() == this.dir.inv()
         )) {
         this.structures[3] = to;
      } else {
         this.structures[3] = null;
      }

      if (this instanceof ElectricConveyorCore) {
         this.structures[1] = null;
         this.structures[2] = null;
      } else {
         Direction d = this.dir.next();
         Structure<?> s = this.getNeighbor(d);
         if (s != null && this.isDockOrInputConveyor(s, d)) {
            this.structures[1] = s;
         } else {
            this.structures[1] = null;
         }

         d = this.dir.prev();
         s = this.getNeighbor(d);
         if (s != null && this.isDockOrInputConveyor(s, d)) {
            this.structures[2] = s;
         } else {
            this.structures[2] = null;
         }
      }
   }

   private boolean isDockOrConveyor(Structure<?> s, Direction dir) {
      if (s instanceof Conveyor) {
         return true;
      } else {
         for (Dock d : s.getDocks()) {
            if (s.x + d.x == this.x + dir.dx && s.y + d.y == this.y + dir.dy && d.dir == dir.inv() && this.canConnectToDock(d)) {
               return true;
            }
         }

         return false;
      }
   }

   private boolean isDockOrInputConveyor(Structure<?> s, Direction dir) {
      if (s instanceof Conveyor) {
         if (s.getSchema().type == StructureType.Conveyor || s.getSchema().type == StructureType.ElectricConveyor) {
            return ((Conveyor)s).getDirection() == dir.inv();
         } else if (s.getSchema().type == StructureType.ConveyorBridge || s.getSchema().type == StructureType.Hopper) {
            return ((ConveyorBridge)s).getDirection() == dir.inv() || ((ConveyorBridge)s).getDirection2() == dir.inv();
         } else {
            return s.getSchema().type == StructureType.ElectricConveyorCore ? ((Conveyor)s).getDirection() == dir.inv() : false;
         }
      } else {
         for (Dock d : s.getDocks()) {
            if (s.x + d.x == this.x + dir.dx && s.y + d.y == this.y + dir.dy && d.dir == dir.inv() && this.canConnectToDock(d)) {
               return true;
            }
         }

         return false;
      }
   }

   protected boolean canConnectToDock(Dock d) {
      return d.type == Dock.DockType.ItemIn || d.type == Dock.DockType.ItemOut;
   }

   @Override
   public void rotate() {
      int i = (this.dir.ordinal() + 1) % 4;
      if (i < 0) {
         i += 4;
      }

      this.setRotation(Direction.values[i]);
   }

   @Override
   public void setRotation(Direction direction) {
      this.dir = direction;
      if (this.layer == null) {
         this.updateStructures();
      } else {
         this.setDirty();
      }
   }

   @Override
   public Direction getDirection() {
      return this.dir;
   }

   public Direction getCurrentDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      return this.dir;
   }

   public Direction getNextDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      return this.dir;
   }

   public int getItemCount() {
      return this.itemCount;
   }

   public ItemEntity[] getItems() {
      return this.items;
   }

   public boolean addItemEntity(Item.ItemType value, int lastSlot, int slot, Direction dir, Structure<?> src) {
      synchronized (this.itemLock) {
         if (this.items[slot] != null) {
            return false;
         } else {
            ItemEntity e = (ItemEntity)Pools.obtain(ItemEntity.class);
            e.item = value;
            e.x = this.x;
            e.y = this.y;
            e.z = this.getItemZ(value, dir);
            e.dir = dir;
            e.src = src;
            e.lastSlot = lastSlot;
            e.slot = slot;
            Game.G.addSeenResource(value);
            this.items[slot] = e;
            this.itemCount++;
            this.itemChanges = true;
            return true;
         }
      }
   }

   public void transferItemsTo(Conveyor target) {
      if (target == null || target == this) {
         return;
      }

      synchronized (this.itemLock) {
         synchronized (target.itemLock) {
            for (int i = 0; i < this.items.length; i++) {
               ItemEntity e = this.items[i];
               if (e != null) {
                  target.items[i] = e;
                  e.x = target.x;
                  e.y = target.y;
                  this.items[i] = null;
                  target.itemCount++;
               }
            }

            this.itemCount = 0;
            target.itemChanges = true;
         }
      }
   }

   public float getItemZ(Item.ItemType type, Direction direction) {
      return -4.0F;
   }

   @Override
   public Object clone() {
      return new Conveyor(this.x, this.y, this.dir);
   }

   @Override
   public void postLoad() {
      if (this.structs != null) {
         for (int i = 0; i < 4; i++) {
            if (this.structs[i] > -1) {
               this.structures[i] = this.layer.getStructure(this.structs[i]);
            }
         }

         this.structs = null;
      }

      for (ItemEntity e : this.items) {
         if (e != null) {
            e.postLoad();
         }
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("dir", (byte)this.dir.ordinal());
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         str[i] = this.structures[i] == null ? -1 : this.structures[i].x * this.layer.height + this.structures[i].y;
      }

      b.IntArray("structs", str);
      b.List("Items", NBT.TagType.Compound);
      synchronized (this.itemLock) {
         for (ItemEntity e : this.items) {
            if (e != null) {
               e.save(b);
            }
         }
      }

      b.End();
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         str[i] = this.structures[i] == null
            ? -1
            : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.structures[i].x * this.layer.height + this.structures[i].y);
      }

      b.Byte("dir", (byte)this.dir.ordinal()).IntArray("structs", str);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);

      try {
         this.dir = Direction.values[tag.Byte("dir", (byte)0)];
         int[] str = tag.IntArray("structs");

         for (int i = 0; i < 4; i++) {
            this.structures[i] = CopyRegionHelper.getStructure(this, pasteRegion, str[i]);
         }
      } catch (NBT.NBTException var5) {
         Quarry.Q.pi.message(1, var5);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.dir = Direction.values[tag.Byte("dir", (byte)0)];
      this.structs = tag.IntArray("structs", null);

      for (NBT.Tag t : tag.List("Items", NBT.TagType.Compound).data) {
         try {
            ItemEntity e = (ItemEntity)Pools.obtain(ItemEntity.class);
            e.load((NBT.CompoundTag)t);
            this.items[e.slot] = e;
            this.itemChanges = true;
            this.itemCount++;
         } catch (NBT.NBTException var5) {
            Quarry.Q.pi.message(1, var5);
         }
      }
   }

   public static class ConveyorSchema extends Schema {
      TextureRegion texWE;
      TextureRegion texNE;
      TextureRegion texNSE;
      TextureRegion texWNE;
      TextureRegion texWNSE;

      public ConveyorSchema(
         int version, StructureType type, boolean clickable, int width, int height, String tex, Item.Items buildCosts, Sfx sfx, Dock... docks
      ) {
         super(version, type, clickable, width, height, tex, buildCosts, sfx, docks);
         this.texWE = Quarry.Q.atlas.findRegion("structure_" + tex + "_we");
         this.texNE = Quarry.Q.atlas.findRegion("structure_" + tex + "_ne");
         this.texNSE = Quarry.Q.atlas.findRegion("structure_" + tex + "_nse");
         this.texWNE = Quarry.Q.atlas.findRegion("structure_" + tex + "_wne");
         this.texWNSE = Quarry.Q.atlas.findRegion("structure_" + tex + "_wnse");
      }
   }
}
