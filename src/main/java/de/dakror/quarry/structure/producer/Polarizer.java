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

public class Polarizer extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Polarizer,
         3,
         2,
         "polarizer",
         new Item.Items(Item.ItemType.StoneBrick, 20, Item.ItemType.SteelPlate, 10, Item.ItemType.IronIngot, 30),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(17.0F, "magnet")
                     .input(new Item.Items.Amount(Item.ItemType.IronIngot, 2))
                     .output(new Item.Items.Amount(Item.ItemType.Magnet, 1))
               );
            }
         },
         new Sfx("polarizer" + Const.SFX_FORMAT),
         false,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(1, 1, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.IronIngot))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Magnetism, Science.ScienceType.OreProcessing});

   public Polarizer(int x, int y) {
      super(x, y, classSchema);
   }
}
