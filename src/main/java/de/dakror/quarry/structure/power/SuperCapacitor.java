package de.dakror.quarry.structure.power;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class SuperCapacitor extends Substation {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         1,
         StructureType.SuperCapacitor,
         3,
         3,
         1000000000,
         200,
         "supercapacitor",
         new Item.Items(Item.ItemType.MachineFrame, 50, Item.ItemType.AdvancedMachineFrame, 10, Item.ItemType.SteelCable, 200, Item.ItemType.Battery, 60),
         new Sfx("node" + Const.SFX_FORMAT, 0.2F),
         0,
         new Dock(1, 2, Direction.North, Dock.DockType.BigPower),
         new Dock(1, 0, Direction.South, Dock.DockType.BigPower)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.HighPower, Science.ScienceType.BetterStorage})
      .flags(Schema.Flags.TextureAlwaysUpright, Schema.Flags.MirroredTextureHorizontal);

   public SuperCapacitor(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public int getDonorPriority() {
      return 20;
   }

   @Override
   public int getReceiverPriority() {
      return 20;
   }
}
