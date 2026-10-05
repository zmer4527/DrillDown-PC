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

public class Assembler extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         3,
         StructureType.Assembler,
         6,
         6,
         "assembler",
         new Item.Items(
            Item.ItemType.StoneBrick,
            35,
            Item.ItemType.SteelIngot,
            50,
            Item.ItemType.WoodPlank,
            85,
            Item.ItemType.Scaffolding,
            60,
            Item.ItemType.CopperIngot,
            80
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(28.0F, "dynamo")
                     .input(
                        new Item.Items.Amount(Item.ItemType.CopperWire, 40),
                        new Item.Items.Amount(Item.ItemType.Magnet, 3),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 4),
                        new Item.Items.Amount(Item.ItemType.CarbonBlock, 2)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamo, 1))
               );
               this.add(
                  new RecipeList.Recipe(25.0F, "rotor", 30)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 20),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 40),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 30)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Rotor, 1))
               );
               this.add(
                  new RecipeList.Recipe(20.0F, "pallet", 500)
                     .input(new Item.Items.Amount(Item.ItemType.WoodPlank, 36), new Item.Items.Amount(Item.ItemType.SteelWire, 12))
                     .output(new Item.Items.Amount(Item.ItemType.Pallet, 1))
                     .science(Science.ScienceType.AdvancedTransport)
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "blueprint", 100)
                     .input(new Item.Items.Amount(Item.ItemType.Paper, 4), new Item.Items.Amount(Item.ItemType.IronPlate, 2))
                     .output(new Item.Items.Amount(Item.ItemType.Blueprint, 1))
                     .science(Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(5.0F, "dynamite", 400)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Paper, 8),
                        new Item.Items.Amount(Item.ItemType.Gunpowder, 4),
                        new Item.Items.Amount(Item.ItemType.Sand, 14),
                        new Item.Items.Amount(Item.ItemType.Clay, 3)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamite, 1))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(13.0F, "battery", 200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 8),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 8),
                        new Item.Items.Amount(Item.ItemType.SulfurDust, 24),
                        new Item.Items.Amount(Item.ItemType.PlasticCasing, 1)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Battery, 1))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.OilProcessing)
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "turbine", 1200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Rotor, 4),
                        new Item.Items.Amount(Item.ItemType.Dynamo, 8),
                        new Item.Items.Amount(Item.ItemType.SteelTube, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 50),
                        new Item.Items.Amount(Item.ItemType.Lubricant, 10000)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Turbine, 1))
                     .science(Science.ScienceType.OilProcessing)
               );
            }
         },
         new Sfx("assembler" + Const.SFX_FORMAT),
         false,
         new Dock(5, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            5,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CopperWire, Item.ItemType.BronzePlate, Item.ItemType.Rotor, Item.ItemType.Paper, Item.ItemType.WoodPlank)
         ),
         new Dock(
            5,
            5,
            Direction.East,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(
               Item.ItemType.Magnet, Item.ItemType.IronPlate, Item.ItemType.Dynamo, Item.ItemType.IronPlate, Item.ItemType.Gunpowder, Item.ItemType.SteelWire
            )
         ),
         new Dock(
            5,
            3,
            Direction.East,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.SteelIngot, Item.ItemType.SulfurDust, Item.ItemType.SteelTube, Item.ItemType.Sand)
         ),
         new Dock(
            0,
            3,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CarbonBlock, Item.ItemType.PlasticCasing, Item.ItemType.SteelPlate, Item.ItemType.Clay)
         ),
         new Dock(1, 5, Direction.North, Dock.DockType.Power),
         new Dock(4, 5, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity})
      .flags(Schema.Flags.TextureAlwaysUpright);
   public static final ProducerStructure.ProducerSchema classSchemaV113 = new ProducerStructure.ProducerSchema(
         2,
         StructureType.Assembler,
         6,
         6,
         "assembler",
         new Item.Items(
            Item.ItemType.StoneBrick,
            35,
            Item.ItemType.SteelIngot,
            50,
            Item.ItemType.WoodPlank,
            85,
            Item.ItemType.Scaffolding,
            60,
            Item.ItemType.CopperIngot,
            80
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(28.0F, "dynamo")
                     .input(
                        new Item.Items.Amount(Item.ItemType.CopperWire, 40),
                        new Item.Items.Amount(Item.ItemType.Magnet, 3),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 4),
                        new Item.Items.Amount(Item.ItemType.CarbonBlock, 2)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamo, 1))
               );
               this.add(
                  new RecipeList.Recipe(25.0F, "rotor", 30)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 20),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 40),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 30)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Rotor, 1))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "blueprint", 100)
                     .input(new Item.Items.Amount(Item.ItemType.Paper, 4), new Item.Items.Amount(Item.ItemType.IronPlate, 2))
                     .output(new Item.Items.Amount(Item.ItemType.Blueprint, 1))
                     .science(Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(5.0F, "dynamite", 400)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Paper, 8),
                        new Item.Items.Amount(Item.ItemType.Gunpowder, 4),
                        new Item.Items.Amount(Item.ItemType.Sand, 14),
                        new Item.Items.Amount(Item.ItemType.Clay, 3)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamite, 1))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(13.0F, "battery", 200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 8),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 8),
                        new Item.Items.Amount(Item.ItemType.SulfurDust, 24),
                        new Item.Items.Amount(Item.ItemType.PlasticCasing, 1)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Battery, 1))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.OilProcessing)
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "turbine", 1200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Rotor, 4),
                        new Item.Items.Amount(Item.ItemType.Dynamo, 8),
                        new Item.Items.Amount(Item.ItemType.SteelTube, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 50),
                        new Item.Items.Amount(Item.ItemType.Lubricant, 10000)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Turbine, 1))
                     .science(Science.ScienceType.OilProcessing)
               );
               this.add(
                  new RecipeList.Recipe(20.0F, "pallet", 500)
                     .input(new Item.Items.Amount(Item.ItemType.WoodPlank, 36), new Item.Items.Amount(Item.ItemType.SteelWire, 12))
                     .output(new Item.Items.Amount(Item.ItemType.Pallet, 1))
                     .science(Science.ScienceType.AdvancedTransport)
               );
            }
         },
         new Sfx("assembler" + Const.SFX_FORMAT),
         false,
         new Dock(5, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            5,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CopperWire, Item.ItemType.BronzePlate, Item.ItemType.Rotor, Item.ItemType.Paper, Item.ItemType.WoodPlank)
         ),
         new Dock(
            5,
            5,
            Direction.East,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(
               Item.ItemType.Magnet, Item.ItemType.IronPlate, Item.ItemType.Dynamo, Item.ItemType.IronPlate, Item.ItemType.Gunpowder, Item.ItemType.SteelWire
            )
         ),
         new Dock(
            5,
            3,
            Direction.East,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.SteelIngot, Item.ItemType.SulfurDust, Item.ItemType.SteelTube, Item.ItemType.Sand)
         ),
         new Dock(
            0,
            3,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CarbonBlock, Item.ItemType.PlasticCasing, Item.ItemType.SteelPlate, Item.ItemType.Clay)
         ),
         new Dock(1, 5, Direction.North, Dock.DockType.Power),
         new Dock(4, 5, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity})
      .flags(Schema.Flags.TextureAlwaysUpright);
   public static final ProducerStructure.ProducerSchema classSchemaV106 = new ProducerStructure.ProducerSchema(
         1,
         StructureType.Assembler,
         6,
         6,
         "assembler",
         new Item.Items(
            Item.ItemType.StoneBrick,
            35,
            Item.ItemType.SteelIngot,
            50,
            Item.ItemType.WoodPlank,
            85,
            Item.ItemType.Scaffolding,
            60,
            Item.ItemType.CopperIngot,
            80
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(28.0F, "dynamo")
                     .input(
                        new Item.Items.Amount(Item.ItemType.CopperWire, 40),
                        new Item.Items.Amount(Item.ItemType.Magnet, 3),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 4),
                        new Item.Items.Amount(Item.ItemType.CarbonBlock, 2)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamo, 1))
               );
               this.add(
                  new RecipeList.Recipe(25.0F, "rotor", 30)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 20),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 40),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 30)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Rotor, 1))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "blueprint", 100)
                     .input(new Item.Items.Amount(Item.ItemType.Paper, 4), new Item.Items.Amount(Item.ItemType.IronPlate, 2))
                     .output(new Item.Items.Amount(Item.ItemType.Blueprint, 1))
                     .science(Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(13.0F, "battery", 200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 8),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 8),
                        new Item.Items.Amount(Item.ItemType.SulfurDust, 24),
                        new Item.Items.Amount(Item.ItemType.PlasticCasing, 1)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Battery, 1))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.OilProcessing)
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "turbine", 1200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Rotor, 4),
                        new Item.Items.Amount(Item.ItemType.Dynamo, 8),
                        new Item.Items.Amount(Item.ItemType.SteelTube, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 50),
                        new Item.Items.Amount(Item.ItemType.Lubricant, 10000)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Turbine, 1))
                     .science(Science.ScienceType.OilProcessing)
               );
            }
         },
         new Sfx("assembler" + Const.SFX_FORMAT),
         false,
         new Dock(5, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            5,
            Direction.West,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CopperWire, Item.ItemType.BronzePlate, Item.ItemType.Rotor, Item.ItemType.Paper)
         ),
         new Dock(
            5,
            5,
            Direction.East,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.Magnet, Item.ItemType.IronPlate, Item.ItemType.Dynamo, Item.ItemType.IronPlate)
         ),
         new Dock(5, 3, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SteelIngot, Item.ItemType.SulfurDust, Item.ItemType.SteelTube)),
         new Dock(
            0, 3, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CarbonBlock, Item.ItemType.PlasticCasing, Item.ItemType.SteelPlate)
         ),
         new Dock(1, 5, Direction.North, Dock.DockType.Power),
         new Dock(4, 5, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity})
      .flags(Schema.Flags.TextureAlwaysUpright);
   public static final ProducerStructure.ProducerSchema classSchemaV103 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Assembler,
         6,
         6,
         "assembler",
         new Item.Items(
            Item.ItemType.StoneBrick,
            35,
            Item.ItemType.SteelIngot,
            50,
            Item.ItemType.WoodPlank,
            85,
            Item.ItemType.Scaffolding,
            60,
            Item.ItemType.CopperIngot,
            80
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(28.0F, "dynamo")
                     .input(
                        new Item.Items.Amount(Item.ItemType.CopperWire, 40),
                        new Item.Items.Amount(Item.ItemType.Magnet, 3),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 4),
                        new Item.Items.Amount(Item.ItemType.CarbonBlock, 2)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Dynamo, 1))
               );
               this.add(
                  new RecipeList.Recipe(25.0F, "rotor", 30)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 20),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 40),
                        new Item.Items.Amount(Item.ItemType.SteelIngot, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 30)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Rotor, 1))
               );
               this.add(
                  new RecipeList.Recipe(13.0F, "battery", 200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.BronzePlate, 8),
                        new Item.Items.Amount(Item.ItemType.IronPlate, 8),
                        new Item.Items.Amount(Item.ItemType.SulfurDust, 24),
                        new Item.Items.Amount(Item.ItemType.PlasticCasing, 1)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Battery, 1))
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "turbine", 1200)
                     .input(
                        new Item.Items.Amount(Item.ItemType.Rotor, 4),
                        new Item.Items.Amount(Item.ItemType.Dynamo, 8),
                        new Item.Items.Amount(Item.ItemType.SteelTube, 25),
                        new Item.Items.Amount(Item.ItemType.SteelPlate, 50),
                        new Item.Items.Amount(Item.ItemType.Lubricant, 10000)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Turbine, 1))
               );
            }
         },
         new Sfx("assembler" + Const.SFX_FORMAT),
         false,
         new Dock(5, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 5, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CopperWire, Item.ItemType.BronzePlate, Item.ItemType.Rotor)),
         new Dock(5, 5, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Magnet, Item.ItemType.IronPlate, Item.ItemType.Dynamo)),
         new Dock(5, 3, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SteelIngot, Item.ItemType.SulfurDust, Item.ItemType.SteelTube)),
         new Dock(
            0, 3, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CarbonBlock, Item.ItemType.PlasticCasing, Item.ItemType.SteelPlate)
         ),
         new Dock(1, 5, Direction.North, Dock.DockType.Power),
         new Dock(4, 5, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Lubricant))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.Electricity})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public Assembler(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchemaV113, classSchemaV103, classSchemaV106, classSchema}));
   }

   public Assembler(int x, int y) {
      super(x, y, classSchema);
   }
}
