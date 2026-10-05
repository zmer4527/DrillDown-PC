package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.IntMap.Entry;
import de.dakror.common.libgdx.Pair;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.ItemEntity;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;

public class Hopper extends ConveyorBridge {
   public static final Conveyor.ConveyorSchema classSchema = new Conveyor.ConveyorSchema(
      0, StructureType.Hopper, true, 1, 1, "hopper", new Item.Items(Item.ItemType.Wood, 8, Item.ItemType.Stone, 8), null
   );
   static TextureRegion tex = Quarry.Q.atlas.findRegion("structure_conveyor_we");
   static TextureRegion pad = Quarry.Q.atlas.findRegion("structure_conveyor_pad1");
   Structure<?> target;
   int blockerSlot;
   int deciderSlot;
   int minSlot;
   int maxSlot;
   int dirSlot;
   int invDirSlot;

   public Hopper(int x, int y) {
      super(x, y, classSchema);
   }

   public Hopper(int x, int y, Direction direction, boolean prev, Conveyor.ConveyorSchema schema) {
      super(x, y, direction, prev, schema);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null) {
         this.updateTarget();
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateTarget();
   }

   protected void updateTarget() {
      this.target = this.layer.getStructure(this.x + this.dir.dx, this.y + this.dir.dy);
      this.dirSlot = Layer.getStartingSlot(this.dir);
      this.invDirSlot = Layer.getStartingSlot(this.dir.inv());
      this.minSlot = Math.min(this.dirSlot, this.invDirSlot);
      this.maxSlot = Math.max(this.dirSlot, this.invDirSlot);
      this.deciderSlot = this.dirSlot + 2 * (this.invDirSlot == this.maxSlot ? 1 : -1);
      this.blockerSlot = this.invDirSlot - 2 * (this.invDirSlot == this.maxSlot ? 1 : -1);
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      spriter.add(pad, (float)(this.x * 64), (float)(this.y * 64 + 12), -5.0F, 32.0F, 20.0F, 10.0F, 40.0F, 1.0F, 1.0F, (float)(this.getDirection2().rot - 180));
      spriter.add(tex, (float)(this.x * 64), (float)(this.y * 64), -5.0F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)this.dir.rot);
      float var10002 = (float)(this.x * 64);
      float var10003 = (float)(this.y * 64);
      spriter.add(this.getSchema().tex, var10002, var10003, -3.0F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)(this.dir.rot + 90));
   }

   @Override
   public boolean isItemSlotFree(int slot) {
      Direction d = this.getDirection2();
      if (d == Direction.East && slot == 7) {
         return false;
      } else if (d == Direction.West && slot == 13) {
         return false;
      } else if (d == Direction.North && slot == 0) {
         return false;
      } else {
         return d == Direction.South && slot == 6 ? false : super.isItemSlotFree(slot);
      }
   }

   private boolean isQueueFree() {
      int dir = this.dirSlot == this.minSlot ? 1 : -1;

      for (int i = 0; i < 2; i++) {
         if (!this.isItemSlotFree(this.blockerSlot + i * dir) && this.items[this.blockerSlot + i * dir].dir == this.getDirection2()) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected void updateItemWithin(ItemEntity e, boolean chain) {
      if (e.slot == this.deciderSlot && this.target != null && !this.target.canAccept(e.item, this.x, this.y, this.dir)) {
         if (this.isQueueFree()) {
            e.dir = this.getDirection2();
         } else {
            e.dir = null;
         }
      }

      super.updateItemWithin(e, chain);
   }

   @Override
   protected void notifyNeighbors(boolean chain) {
      if (this.structures[2] instanceof Conveyor && this.isItemSlotFree(Layer.getStartingSlot(this.dir))) {
         ((Conveyor)this.structures[2]).itemChanges = true;
         if (chain) {
            ((Conveyor)this.structures[2]).updateItems(0.0, 1, true, false);
         }
      }
   }

   public boolean isValidRotation(Direction dir) {
      Layer l = this.layer;
      if (l == null) {
         l = Game.G.layer;
      }

      Structure<?> s = l.getStructure(this.x + dir.dx, this.y + dir.dy);
      if (Game.G.pasteMode) {
         for (Entry<Pair<Structure<?>, NBT.CompoundTag>> pair : Game.G.copyStructures.entries()) {
            Structure<?> q = (Structure<?>)((Pair)pair.value).getKey();
            if (q.x <= this.x + dir.dx && q.x + q.getWidth() > this.x + dir.dx && q.y <= this.y + dir.dy && q.y + q.getHeight() > this.y + dir.dy) {
               s = q;
               break;
            }
         }
      }

      if (s != null && s.getDocks().length != 0) {
         for (Dock d : s.getDocks()) {
            if (s.isNextToDock(this.x, this.y, dir, d) && d.type == Dock.DockType.ItemIn) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   @Override
   public void setRotation(Direction direction) {
      if (this.isValidRotation(direction) || this.layer == null) {
         super.setRotation(direction);
      }
   }

   @Override
   public float getItemZ(Item.ItemType type, Direction direction) {
      return -4.0F;
   }

   @Override
   public Direction getCurrentDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      return this.target != null
            && this.target.canAccept(item, this.x, this.y, this.dir)
            && slot >= Math.min(this.dirSlot, this.invDirSlot)
            && slot <= Math.max(this.dirSlot, this.invDirSlot)
         ? this.dir
         : this.getDirection2();
   }

   @Override
   public Direction getNextDirection(Item.ItemType item, Direction prev, float currentZ, int slot) {
      return this.target != null && this.target.canAccept(item, this.x, this.y, this.dir) ? this.dir : this.getDirection2();
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (dirtyBounds.touches(this)) {
         this.updateTarget();
      }
   }

   @Override
   public Object clone() {
      return new Hopper(this.x, this.y, this.dir, this.dir2Prev, classSchema);
   }
}
