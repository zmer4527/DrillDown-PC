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

public class Condenser extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Condenser,
         3,
         1,
         "condenser",
         new Item.Items(Item.ItemType.CopperTube, 8, Item.ItemType.SteelPlate, 10, Item.ItemType.Scaffolding, 50, Item.ItemType.Glass, 25),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(2.0F, "water")
                     .input(new Item.Items.Amount(Item.ItemType.Steam, 2000))
                     .output(new Item.Items.Amount(Item.ItemType.Water, 250))
               );
            }
         },
         new Sfx("condenser" + Const.SFX_FORMAT),
         true,
         new Dock(2, 0, Direction.East, Dock.DockType.FluidOut),
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Steam))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.WaterUsage});

   public Condenser(int x, int y) {
      super(x, y, classSchema);
   }
}
