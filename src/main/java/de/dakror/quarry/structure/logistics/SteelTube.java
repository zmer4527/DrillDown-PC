package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.StructureType;

public class SteelTube extends FluidTubeStructure {
   public static FluidTubeStructure.FluidTubeSchema classSchema = new FluidTubeStructure.FluidTubeSchema(
         0, StructureType.SteelTube, 10000, 1, 1, "steel_tube", new Item.Items(Item.ItemType.SteelTube, 4), null
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing});

   public SteelTube(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected boolean isAllowedFluid(Item.ItemType i) {
      return i.categories.contains(Item.ItemCategory.Fluid) && Item.base(i) != Item.ItemType._MoltenMetal;
   }
}
