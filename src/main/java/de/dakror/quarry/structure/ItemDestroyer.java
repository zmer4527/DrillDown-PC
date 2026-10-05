package de.dakror.quarry.structure;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;

public class ItemDestroyer extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
      0,
      StructureType.ItemDestroyer,
      true,
      1,
      1,
      "itemdestroyer",
      new Item.Items(Item.ItemType.WoodPlank, 2, Item.ItemType.IronIngot, 2, Item.ItemType.Scaffolding, 4),
      null,
      new Dock(0, 0, Direction.North, Dock.DockType.ItemIn)
   );

   public ItemDestroyer(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (item != null && dir != null) {
         if (item.categories.contains(Item.ItemCategory.Fluid)) {
            return false;
         } else {
            for (Dock d : this.getDocks()) {
               if (d.type == Dock.DockType.ItemIn && this.isNextToDock(x, y, dir, d)) {
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      return item == null ? false : !item.categories.contains(Item.ItemCategory.Fluid);
   }
}
