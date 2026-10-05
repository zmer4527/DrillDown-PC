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

public class TubeBender extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.TubeBender,
         2,
         3,
         "tubebender",
         new Item.Items(Item.ItemType.IronIngot, 20, Item.ItemType.SteelIngot, 20, Item.ItemType.Brick, 40),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(12.0F, "cu_tube")
                     .input(new Item.Items.Amount(Item.ItemType.CopperPlate, 1))
                     .output(new Item.Items.Amount(Item.ItemType.CopperTube, 1))
               );
               this.add(
                  new RecipeList.Recipe(20.0F, "steel_tube")
                     .input(new Item.Items.Amount(Item.ItemType.SteelPlate, 2))
                     .output(new Item.Items.Amount(Item.ItemType.SteelTube, 1))
               );
            }
         },
         new Sfx("bender" + Const.SFX_FORMAT),
         false,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CopperPlate, Item.ItemType.SteelPlate))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Metalworking});

   public TubeBender(int x, int y) {
      super(x, y, classSchema);
   }
}
