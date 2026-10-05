package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ConveyorBridge;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;

public abstract class RouterStructure extends Structure<RouterStructure.RouterSchema> {
   protected Item.ItemType currentItem;
   protected Structure<?> currentSource;
   protected Direction currentSourceDirection;
   protected Conveyor[] tubes = new Conveyor[4];
   protected int[] tubeIdx;
   protected boolean stuck;

   protected RouterStructure(int x, int y, RouterStructure.RouterSchema schema) {
      super(x, y, schema);
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return this.currentItem == null && !item.categories.contains(Item.ItemCategory.Fluid);
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      if (this.currentItem == null && !item.categories.contains(Item.ItemCategory.Fluid)) {
         this.currentItem = item;
         this.currentSource = source;
         this.currentSourceDirection = dir.inv();
         return true;
      } else {
         return false;
      }
   }

   protected void updateTubes() {
      for (int i = 0; i < 4; i++) {
         Direction d = Direction.values[i];
         Structure<?> s = this.layer.getStructure(this.x + d.dx, this.y + d.dy);
         this.tubes[i] = s == null
               || (s.getSchema().type != StructureType.Conveyor || ((Conveyor)s).getDirection() == d.inv())
                  && (s.getSchema().type != StructureType.Hopper || ((Conveyor)s).getDirection() != d)
                  && (s.getSchema().type != StructureType.ElectricConveyor || ((Conveyor)s).getDirection() == d.inv())
                  && (s.getSchema().type != StructureType.ElectricConveyorCore || ((Conveyor)s).getDirection() != d)
                  && (
                     s.getSchema().type != StructureType.ConveyorBridge
                        || ((ConveyorBridge)s).getDirection() == d.inv()
                        || ((ConveyorBridge)s).getDirection2() == d.inv()
                  )
            ? null
            : (Conveyor)s;
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.tubes = new Conveyor[4];

      for (int i = 0; i < 4; i++) {
         this.tubes[i] = this.tubeIdx[i] == -1 ? null : (Conveyor)this.layer.getStructure(this.tubeIdx[i]);
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.stuck) {
         this.drawFullState(spriter);
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.layer != null && (dirtyBounds.touches(this) || this.tubes == null)) {
         this.updateTubes();
      }

      if (this.currentItem != null && gameSpeed > 0) {
         if (this.dispatchItem()) {
            this.stuck = false;
            this.currentItem = null;
            this.currentSource = null;
            this.setItemNotifications();
         } else {
            this.stuck = true;
         }
      }
   }

   protected abstract boolean dispatchItem();

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.currentItem = Item.get(tag.Short("item", (short)0));
      this.tubeIdx = tag.IntArray("tubes", new int[4]);
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      this.tubeIdx = new int[4];

      for (int i = 0; i < 4; i++) {
         this.tubeIdx[i] = this.tubes[i] != null ? this.tubes[i].x * this.layer.height + this.tubes[i].y : -1;
      }

      b.Short("item", this.currentItem != null ? this.currentItem.value : 0).IntArray("tubes", this.tubeIdx);
   }

   public static class RouterSchema extends Schema {
      public RouterSchema(int version, StructureType type, boolean clickable, String tex, Item.Items buildCosts, Sfx sfx) {
         super(
            version,
            type,
            clickable,
            1,
            1,
            tex,
            buildCosts,
            sfx,
            new Dock(0, 0, Direction.North, Dock.DockType.ItemIn),
            new Dock(0, 0, Direction.East, Dock.DockType.ItemIn),
            new Dock(0, 0, Direction.South, Dock.DockType.ItemIn),
            new Dock(0, 0, Direction.West, Dock.DockType.ItemIn)
         );
         this.flags(new Schema.Flags[]{Schema.Flags.NotRotatable});
      }
   }
}
