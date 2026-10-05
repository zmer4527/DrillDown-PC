package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;

public class ItemLift extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.ItemLift,
         true,
         1,
         1,
         "itemlift",
         new Item.Items(Item.ItemType.SteelIngot, 10, Item.ItemType.StoneBrick, 4),
         null,
         new Dock(0, 0, Direction.West, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.East, Dock.DockType.ItemIn)
      )
      .sciences(Science.ScienceType.MineExpansion);
   ItemLift other;
   boolean hasOutput;
   boolean upper;
   Item.ItemType currentItem;
   Structure<?> currentSource;

   public ItemLift(int x, int y) {
      this(x, y, true, classSchema);
   }

   protected ItemLift(int x, int y, boolean upper, Schema schema) {
      super(x, y, schema);
      this.upper = upper;
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null && this.upper) {
         Layer below = Game.G.getLayer(this.layer.getIndex() + 1);
         if (below != null) {
            this.other = new ItemLiftBelow(this.x, this.y);
            this.other.setUpDirection(this.upDirection);
            this.other.other = this;
            below.addStructure(this.other);
         }
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.other = (ItemLift)Game.G.getLayer(this.layer.getIndex() + (this.upper ? 1 : -1)).getStructure(this.x, this.y);
      this.updateOutput();
      this.setItemNotifications();
      this.other.setItemNotifications();
   }

   protected void updateOutput() {
      Direction d = this.getDocks()[0].dir;
      Structure<?> s = this.layer.getStructure(this.x + d.dx, this.y + d.dy);
      this.hasOutput = s != null
         && (
            s.getSchema().type == StructureType.Conveyor && ((Conveyor)s).getDirection() != d.inv()
               || s.getSchema().type == StructureType.ElectricConveyor && ((Conveyor)s).getDirection() != d.inv()
               || s.getSchema().type == StructureType.ElectricConveyorCore && ((Conveyor)s).getDirection() == d
               || s.getSchema().type == StructureType.ConveyorBridge
                  && (((ConveyorBridge)s).getDirection() != d.inv() || ((ConveyorBridge)s).getDirection2() != d.inv())
               || s.getSchema().type == StructureType.Hopper && ((Conveyor)s).getDirection() == d
         );
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (dirtyBounds.touches(this)) {
         this.updateOutput();
         this.other.updateOutput();
         this.setItemNotifications();
         this.other.setItemNotifications();
      }

      if (this.currentItem != null && this.other.layer.addItemEntity(this.currentItem, this.other, this.other.getDocks()[0].dir, this.currentSource)) {
         this.currentItem = null;
         this.currentSource = null;
         this.setItemNotifications();
         this.other.setItemNotifications();
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return !item.categories.contains(Item.ItemCategory.Fluid)
         && this.isNextToDock(x, y, dir, this.getDocks()[1])
         && this.other.hasOutput
         && this.currentItem == null;
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      if (!item.categories.contains(Item.ItemCategory.Fluid) && this.other.hasOutput && this.currentItem == null) {
         this.currentItem = item;
         this.currentSource = source;
         this.setItemNotifications();
         this.other.setItemNotifications();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.other.layer.removeStructure(this.other);
   }
}
