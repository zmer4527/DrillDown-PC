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

public class FillingMachine extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.FillingMachine,
         4,
         4,
         "fillingmachine",
         new Item.Items(
            Item.ItemType.MachineFrame, 10, Item.ItemType.SteelIngot, 20, Item.ItemType.Brick, 90, Item.ItemType.CopperTube, 20, Item.ItemType.Dynamo, 5
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(3.0F, "barrel", 167)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.Water, 8000))
                     .output(new Item.Items.Amount(Item.ItemType.WaterBarrel, 1))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "barrel", 167)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.CrudeOil, 8000))
                     .output(new Item.Items.Amount(Item.ItemType.CrudeOilBarrel, 1))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "barrel", 167)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.RefinedOil, 8000))
                     .output(new Item.Items.Amount(Item.ItemType.RefinedOilBarrel, 1))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "barrel", 167)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.Lubricant, 8000))
                     .output(new Item.Items.Amount(Item.ItemType.LubricantBarrel, 1))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "tank", 334)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyTank, 1), new Item.Items.Amount(Item.ItemType.NaturalGas, 12000))
                     .output(new Item.Items.Amount(Item.ItemType.GasTank, 1))
               );
               this.add(
                  new RecipeList.Recipe(4.0F, "tank", 500)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyTank, 1), new Item.Items.Amount(Item.ItemType.Steam, 50000))
                     .output(new Item.Items.Amount(Item.ItemType.PressurizedSteamTank, 1))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "tank", 334)
                     .input(new Item.Items.Amount(Item.ItemType.EmptyTank, 1), new Item.Items.Amount(Item.ItemType.PressurizedSteam, 12000))
                     .output(new Item.Items.Amount(Item.ItemType.PressurizedSteamTank, 1))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 60000);
            }
         },
         new Sfx("compactor" + Const.SFX_FORMAT, 0.7F),
         true,
         new Dock(0, 3, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.EmptyBarrel, Item.ItemType.EmptyTank)),
         new Dock(
            3,
            3,
            Direction.East,
            Dock.DockType.FluidIn,
            new Dock.DockFilter(
               Item.ItemType.Water,
               Item.ItemType.CrudeOil,
               Item.ItemType.RefinedOil,
               Item.ItemType.Lubricant,
               Item.ItemType.Steam,
               Item.ItemType.NaturalGas,
               Item.ItemType.PressurizedSteam
            )
         ),
         new Dock(3, 0, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.West, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.AdvancedTransport});

   public FillingMachine(int x, int y) {
      super(x, y, classSchema);
   }
}
