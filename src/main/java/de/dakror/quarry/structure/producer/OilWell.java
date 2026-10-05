package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class OilWell extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.OilWell,
         4,
         4,
         "oilwell",
         new Item.Items(Item.ItemType.MachineFrame, 4, Item.ItemType.SteelTube, 10, Item.ItemType.SteelWire, 80, Item.ItemType.Dynamo, 4),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(new RecipeList.Recipe(25.0F, "oil", 200).output(new Item.Items.Amount(Item.ItemType.CrudeOil, 12000)));
            }
         },
         new Sfx("oilwell" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(3, 0, Direction.East, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing});
   public static final int MIN_OIL_TILES = 4;

   public OilWell(int x, int y) {
      super(x, y, classSchema);
   }
}
