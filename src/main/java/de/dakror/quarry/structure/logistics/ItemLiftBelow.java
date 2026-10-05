package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;

public class ItemLiftBelow extends ItemLift {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.ItemLiftBelow,
         true,
         1,
         1,
         "itemliftout",
         ItemLift.classSchema.buildCosts,
         null,
         new Dock(0, 0, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.West, Dock.DockType.ItemIn)
      )
      .components(new CInventory(1, 0));

   public ItemLiftBelow(int x, int y) {
      super(x, y, false, classSchema);
   }
}
