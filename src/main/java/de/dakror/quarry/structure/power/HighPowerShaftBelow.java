package de.dakror.quarry.structure.power;

import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.StructureType;

public class HighPowerShaftBelow extends HighPowerShaft {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         0,
         StructureType.HighPowerShaftBelow,
         1,
         1,
         200000,
         200,
         "highpowershaftbelow",
         HighPowerShaft.classSchema.buildCosts,
         null,
         1,
         new Dock(0, 0, Direction.East, Dock.DockType.BigPower)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.MineExpansion, Science.ScienceType.HighPower});

   public HighPowerShaftBelow(int x, int y) {
      super(x, y, classSchema, -1);
   }
}
