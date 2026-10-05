package de.dakror.quarry.structure.power;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class AnchorPortal extends Substation {
   public static final Substation.SubstationSchema classSchema = new Substation.SubstationSchema(
         0,
         StructureType.AnchorPortal,
         7,
         2,
         200000,
         0,
         "anchorportal",
         new Item.Items(Item.ItemType.SteelCable, 100, Item.ItemType.Brick, 80, Item.ItemType.TinPlate, 350, Item.ItemType.Battery, 30),
         new Sfx("anchorportal" + Const.SFX_FORMAT),
         0,
         new Dock(1, 1, Direction.North, Dock.DockType.BigPower),
         new Dock(3, 1, Direction.North, Dock.DockType.BigPower),
         new Dock(5, 1, Direction.North, Dock.DockType.BigPower),
         new Dock(0, 0, Direction.South, Dock.DockType.Power),
         new Dock(1, 0, Direction.South, Dock.DockType.Power),
         new Dock(2, 0, Direction.South, Dock.DockType.Power),
         new Dock(3, 0, Direction.South, Dock.DockType.Power),
         new Dock(4, 0, Direction.South, Dock.DockType.Power),
         new Dock(5, 0, Direction.South, Dock.DockType.Power),
         new Dock(6, 0, Direction.South, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.HighPower});

   public AnchorPortal(int x, int y) {
      super(x, y, classSchema);
   }
}
