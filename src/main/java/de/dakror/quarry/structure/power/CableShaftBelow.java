package de.dakror.quarry.structure.power;

import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.StructureType;

public class CableShaftBelow extends CableShaft {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         0,
         StructureType.CableShaftBelow,
         1,
         1,
         200000,
         80,
         "cableshaftbelow",
         CableShaft.classSchema.buildCosts,
         null,
         1,
         new Dock(0, 0, Direction.West, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.MineExpansion, Science.ScienceType.Electricity});

   public CableShaftBelow(int x, int y) {
      super(x, y, classSchema, -1);
   }
}
