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

public class RollingMachine extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.RollingMachine,
         3,
         3,
         "rollingmachine",
         new Item.Items(Item.ItemType.Scaffolding, 10, Item.ItemType.StoneBrick, 10, Item.ItemType.SteelIngot, 20),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(15.0F, "plate")
                     .input(new Item.Items.Amount(Item.ItemType._Ingot, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Plate, 1, 0))
               );
               this.add(
                  new RecipeList.Recipe(4.0F, "paper")
                     .input(new Item.Items.Amount(Item.ItemType.Cellulose, 1))
                     .output(new Item.Items.Amount(Item.ItemType.Paper, 1))
                     .science(Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(40.0F, "platestack")
                     .input(new Item.Items.Amount(Item.ItemType.PlateStack, 1))
                     .output(new Item.Items.Amount(Item.ItemType.HardenedSteelPlate, 1))
                     .science(Science.ScienceType.HighTech, Science.ScienceType.MineralExtraction)
               );
            }
         },
         new Sfx("rollingmachine" + Const.SFX_FORMAT),
         true,
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType._Ingot, Item.ItemType.PlateStack, Item.ItemType.Cellulose))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Metalworking});

   public RollingMachine(int x, int y) {
      super(x, y, classSchema);
   }
}
