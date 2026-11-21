package de.dakror.quarry.structure.storage;

import de.dakror.common.BiCallback;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;

public class Silo extends Tank {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.Silo,
         true,
         2,
         7,
         "silo",
         new Item.Items(Item.ItemType.SteelPlate, 250, Item.ItemType.SteelIngot, 10, Item.ItemType.SteelTube, 8),
         null,
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn),
         new Dock(1, 0, Direction.East, Dock.DockType.FluidOut)
      )
      .<Schema>components(new CTank(16000000, 1, false).setPumpOutDelay(0.0F).setMaxOutput(1000))
      .<Schema>flags(Schema.Flags.ConfirmDestruction)
      .<Schema>button(new Schema.ButtonDef("icon_pump_out", "button.pump", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((CTank)((Silo)data).components[0]).setOutputEnabled(on);
         }
      }))
      .sciences(Science.ScienceType.BetterStorage, Science.ScienceType.WaterUsage);

   public Silo(int x, int y) {
      super(x, y, classSchema);
   }
}
