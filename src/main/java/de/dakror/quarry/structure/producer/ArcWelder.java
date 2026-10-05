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

public class ArcWelder extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.ArcWelder,
         3,
         3,
         "arcwelder",
         new Item.Items(Item.ItemType.StoneBrick, 90, Item.ItemType.BronzePlate, 100, Item.ItemType.SteelPlate, 100, Item.ItemType.SteelWire, 80),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(8.0F, "machineframe", 500)
                     .input(new Item.Items.Amount(Item.ItemType.SteelPlate, 6), new Item.Items.Amount(Item.ItemType.CopperWire, 40))
                     .output(new Item.Items.Amount(Item.ItemType.MachineFrame, 1))
               );
               this.add(
                  new RecipeList.Recipe(12.0F, "barrel", 800)
                     .input(new Item.Items.Amount(Item.ItemType.TinPlate, 25), new Item.Items.Amount(Item.ItemType.IronPlate, 5))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1))
                     .science(Science.ScienceType.AdvancedTransport)
               );
               this.add(
                  new RecipeList.Recipe(15.0F, "tank", 1400)
                     .input(new Item.Items.Amount(Item.ItemType.SteelPlate, 10), new Item.Items.Amount(Item.ItemType.CopperPlate, 10))
                     .output(new Item.Items.Amount(Item.ItemType.EmptyTank, 1))
                     .science(Science.ScienceType.AdvancedTransport)
               );
               this.add(
                  new RecipeList.Recipe(25.0F, "platestack", 1500)
                     .input(new Item.Items.Amount(Item.ItemType.SteelPlate, 2), new Item.Items.Amount(Item.ItemType.TitaniumPlate, 1))
                     .output(new Item.Items.Amount(Item.ItemType.PlateStack, 1))
                     .science(Science.ScienceType.HighTech, Science.ScienceType.MineralExtraction)
               );
               this.add(
                  new RecipeList.Recipe(40.0F, "advancedmachineframe", 4000)
                     .input(new Item.Items.Amount(Item.ItemType.HardenedSteelPlate, 6), new Item.Items.Amount(Item.ItemType.SteelWire, 40))
                     .output(new Item.Items.Amount(Item.ItemType.AdvancedMachineFrame, 1))
                     .science(Science.ScienceType.HighTech, Science.ScienceType.MineralExtraction)
               );
            }
         },
         new Sfx("arcwelder" + Const.SFX_FORMAT),
         false,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.West, Dock.DockType.Power),
         new Dock(
            0,
            2,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.SteelPlate, Item.ItemType.TinPlate, Item.ItemType.HardenedSteelPlate)
         ),
         new Dock(
            2,
            2,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(
               Item.ItemType.CopperWire, Item.ItemType.CopperPlate, Item.ItemType.SteelWire, Item.ItemType.TitaniumPlate, Item.ItemType.IronPlate
            )
         )
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Electricity});

   public ArcWelder(int x, int y) {
      super(x, y, classSchema);
   }
}
