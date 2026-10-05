package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.StructureType;

public class BrickChannel extends FluidTubeStructure {
   public static FluidTubeStructure.FluidTubeSchema classSchema = new FluidTubeStructure.FluidTubeSchema(
      0, StructureType.BrickChannel, 1000, 1, 1, "brick_channel", new Item.Items(Item.ItemType.Brick, 2), null
   );

   public BrickChannel(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected boolean isAllowedFluid(Item.ItemType i) {
      return Item.base(i) == Item.ItemType._MoltenMetal;
   }
}
