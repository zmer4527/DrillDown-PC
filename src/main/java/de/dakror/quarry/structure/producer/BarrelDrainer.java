package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.util.Sfx;

public class BarrelDrainer extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.BarrelDrainer,
         2,
         2,
         "barreldrainer",
         new Item.Items(
            Item.ItemType.SteelTube, 10, Item.ItemType.CopperTube, 10, Item.ItemType.BronzeIngot, 30, Item.ItemType.MachineFrame, 5, Item.ItemType.Dynamo, 10
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(1.0F, "drain", 250)
                     .input(new Item.Items.Amount(Item.ItemType.WaterBarrel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.Water, 8000))
               );
               this.add(
                  new RecipeList.Recipe(1.0F, "drain", 250)
                     .input(new Item.Items.Amount(Item.ItemType.CrudeOilBarrel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.CrudeOil, 8000))
               );
               this.add(
                  new RecipeList.Recipe(1.0F, "drain", 250)
                     .input(new Item.Items.Amount(Item.ItemType.RefinedOilBarrel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.RefinedOil, 8000))
               );
               this.add(
                  new RecipeList.Recipe(1.0F, "drain", 250)
                     .input(new Item.Items.Amount(Item.ItemType.LubricantBarrel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1), new Item.Items.Amount(Item.ItemType.Lubricant, 8000))
               );
               this.add(
                  new RecipeList.Recipe(1.0F, "tank", 550)
                     .input(new Item.Items.Amount(Item.ItemType.GasTank, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyTank, 1), new Item.Items.Amount(Item.ItemType.NaturalGas, 12000))
               );
               this.add(
                  new RecipeList.Recipe(1.0F, "tank", 550)
                     .input(new Item.Items.Amount(Item.ItemType.PressurizedSteamTank, 1))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyTank, 1), new Item.Items.Amount(Item.ItemType.PressurizedSteam, 12000))
               );
            }
         },
         new Sfx("compactor" + Const.SFX_FORMAT, 1.5F),
         true,
         new Dock(
            0,
            1,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(
               Item.ItemType.WaterBarrel,
               Item.ItemType.CrudeOilBarrel,
               Item.ItemType.RefinedOilBarrel,
               Item.ItemType.LubricantBarrel,
               Item.ItemType.GasTank,
               Item.ItemType.PressurizedSteamTank
            )
         ),
         new Dock(1, 0, Direction.East, Dock.DockType.ItemOut),
         new Dock(1, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(0, 1, Direction.North, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.AdvancedTransport});

   public BarrelDrainer(int x, int y) {
      super(x, y, classSchema);
      ((CTank)this.outputInventories[1]).setPumpOutDelay(0.0F).setMaxOutput(1000);
   }
}
