package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class GroundwaterPump extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.GroundwaterPump,
         2,
         2,
         "groundwaterpump",
         new Item.Items(Item.ItemType.CopperTube, 9, Item.ItemType.SteelPlate, 5),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(new RecipeList.Recipe(5.0F, "water").output(new Item.Items.Amount(Item.ItemType.Water, 500)));
            }
         },
         new Sfx("pump1" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.South, Dock.DockType.FluidOut)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.WaterUsage})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public GroundwaterPump(int x, int y) {
      super(x, y, classSchema);
   }
}
