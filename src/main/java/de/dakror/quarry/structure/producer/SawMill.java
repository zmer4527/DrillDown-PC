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

public class SawMill extends ProducerStructure {
   public static ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.SawMill,
         4,
         2,
         "sawmill",
         new Item.Items(Item.ItemType.StoneBrick, 10, Item.ItemType.SteelIngot, 25),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(8.0F, "plank")
                     .input(new Item.Items.Amount(Item.ItemType.Wood, 1))
                     .output(new Item.Items.Amount(Item.ItemType.WoodPlank, 2))
               );
            }
         },
         new Sfx("sawmill" + Const.SFX_FORMAT),
         true,
         new Dock(3, 0, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Wood))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.SteelProduction})
      .flags(Schema.Flags.NoDustEffect);

   public SawMill(int x, int y) {
      super(x, y, classSchema);
   }
}
