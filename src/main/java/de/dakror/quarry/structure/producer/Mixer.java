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

public class Mixer extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         1,
         StructureType.Mixer,
         2,
         2,
         "mixer",
         new Item.Items(Item.ItemType.Brick, 10, Item.ItemType.IronIngot, 2, Item.ItemType.SteelPlate, 4),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(3.0F, "cement")
                     .input(new Item.Items.Amount(Item.ItemType.Sand, 3), new Item.Items.Amount(Item.ItemType.Clay, 4))
                     .output(new Item.Items.Amount(Item.ItemType.Cement, 2))
               );
               this.add(
                  new RecipeList.Recipe(5.0F, "concretepowder")
                     .input(new Item.Items.Amount(Item.ItemType.Cement, 2), new Item.Items.Amount(Item.ItemType.StoneDust, 5))
                     .output(new Item.Items.Amount(Item.ItemType.ConcretePowder, 2))
               );
               this.add(
                  new RecipeList.Recipe(6.0F, "bronze")
                     .input(new Item.Items.Amount(Item.ItemType.CopperDust, 12), new Item.Items.Amount(Item.ItemType.TinDust, 4))
                     .output(new Item.Items.Amount(Item.ItemType.BronzeDust, 4))
               );
               this.add(
                  new RecipeList.Recipe(15.0F, "cellulose")
                     .input(new Item.Items.Amount(Item.ItemType.WoodChips, 8), new Item.Items.Amount(Item.ItemType.WaterBarrel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.Cellulose, 1), new Item.Items.Amount(Item.ItemType.EmptyBarrel, 1))
                     .science(Science.ScienceType.Blueprints)
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "gunpowder")
                     .input(new Item.Items.Amount(Item.ItemType.SulfurDust, 4), new Item.Items.Amount(Item.ItemType.CoalDust, 8))
                     .output(new Item.Items.Amount(Item.ItemType.Gunpowder, 6))
                     .science(Science.ScienceType.MineralExtraction, Science.ScienceType.HighTech)
               );
            }
         },
         new Sfx("mixer" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            1,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.CopperDust, Item.ItemType.Cement, Item.ItemType.Sand, Item.ItemType.WoodChips, Item.ItemType.SulfurDust)
         ),
         new Dock(
            1,
            1,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(
               Item.ItemType.TinDust, Item.ItemType.Clay, Item.ItemType.StoneDust, Item.ItemType.WaterBarrel, Item.ItemType.CoalDust, Item.ItemType.Gunpowder
            )
         )
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OreProcessing})
      .flags(Schema.Flags.TextureAlwaysUpright);
   public static final ProducerStructure.ProducerSchema classSchemaV103 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Mixer,
         2,
         2,
         "mixer",
         new Item.Items(Item.ItemType.Brick, 10, Item.ItemType.IronIngot, 2, Item.ItemType.SteelPlate, 4),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(3.0F, "cement")
                     .input(new Item.Items.Amount(Item.ItemType.Sand, 3), new Item.Items.Amount(Item.ItemType.Clay, 4))
                     .output(new Item.Items.Amount(Item.ItemType.Cement, 2))
               );
               this.add(
                  new RecipeList.Recipe(5.0F, "concretepowder")
                     .input(new Item.Items.Amount(Item.ItemType.Cement, 2), new Item.Items.Amount(Item.ItemType.StoneDust, 5))
                     .output(new Item.Items.Amount(Item.ItemType.ConcretePowder, 2))
               );
               this.add(
                  new RecipeList.Recipe(6.0F, "bronze")
                     .input(new Item.Items.Amount(Item.ItemType.CopperDust, 12), new Item.Items.Amount(Item.ItemType.TinDust, 4))
                     .output(new Item.Items.Amount(Item.ItemType.BronzeDust, 4))
               );
            }
         },
         new Sfx("mixer" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CopperDust, Item.ItemType.Cement, Item.ItemType.Sand)),
         new Dock(1, 1, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.TinDust, Item.ItemType.Clay, Item.ItemType.StoneDust))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OreProcessing})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public Mixer(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchema, classSchemaV103}));
   }

   public Mixer(int x, int y) {
      super(x, y, classSchema);
   }
}
