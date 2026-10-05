package de.dakror.quarry.structure.power;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class Capacitor extends Substation {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         1,
         StructureType.Capacitor,
         3,
         3,
         20000000,
         100,
         "capacitor",
         new Item.Items(Item.ItemType.Brick, 180, Item.ItemType.SteelCable, 30, Item.ItemType.SteelPlate, 30, Item.ItemType.Battery, 40),
         new Sfx("node" + Const.SFX_FORMAT, 0.5F),
         0,
         new Dock(1, 2, Direction.North, Dock.DockType.Power),
         new Dock(1, 0, Direction.South, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity, Science.ScienceType.BetterStorage})
      .flags(Schema.Flags.TextureAlwaysUpright, Schema.Flags.MirroredTextureHorizontal);
   public static final Substation.SubstationSchema classSchemaV113 = new Substation.SubstationSchema(
         0,
         StructureType.Capacitor,
         3,
         3,
         20000000,
         100,
         "capacitor",
         new Item.Items(Item.ItemType.Brick, 180, Item.ItemType.SteelCable, 30, Item.ItemType.SteelPlate, 30, Item.ItemType.Battery, 40),
         new Sfx("node" + Const.SFX_FORMAT, 0.5F),
         0,
         new Dock(1, 2, Direction.North, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity, Science.ScienceType.BetterStorage})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public Capacitor(int x, int y, int version) {
      super(x, y, selectSchema(version, new Substation.SubstationSchema[]{classSchema, classSchemaV113}));
   }

   public Capacitor(int x, int y) {
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
