package de.dakror.quarry.structure.logistics;

import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;

public class TubeShaftBelow extends TubeShaft {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.TubeShaftBelow,
         true,
         1,
         1,
         "tubeshaftout",
         TubeShaft.classSchema.buildCosts,
         null,
         new Dock(0, 0, Direction.East, Dock.DockType.FluidOut),
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn)
      )
      .components(new CTank(20000, 0).setPumpOutDelay(0.0F).setMaxOutput(1000));

   public TubeShaftBelow(int x, int y) {
      super(x, y, classSchema, -1);
   }
}
