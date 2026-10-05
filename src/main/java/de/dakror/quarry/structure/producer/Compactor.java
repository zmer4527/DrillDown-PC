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

public class Compactor extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Compactor,
         3,
         2,
         "compactor",
         new Item.Items(Item.ItemType.StoneBrick, 25, Item.ItemType.SteelPlate, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(10.0F, "coalchunk")
                     .input(new Item.Items.Amount(Item.ItemType.CoalDust, 8))
                     .output(new Item.Items.Amount(Item.ItemType.CoalChunk, 1))
               );
               this.add(
                  new RecipeList.Recipe(10.0F, "carbonblock")
                     .input(new Item.Items.Amount(Item.ItemType.CoalChunk, 4))
                     .output(new Item.Items.Amount(Item.ItemType.CarbonBlock, 1))
               );
               this.add(
                  new RecipeList.Recipe(490.0F, "quartz")
                     .input(new Item.Items.Amount(Item.ItemType.QuartzDust, 120))
                     .output(new Item.Items.Amount(Item.ItemType.SyntheticQuartz, 1))
                     .science(Science.ScienceType.MineralExtraction)
               );
            }
         },
         new Sfx("compactor" + Const.SFX_FORMAT),
         true,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(1, 1, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.CoalDust, Item.ItemType.CoalChunk, Item.ItemType.QuartzDust))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.OreProcessing});

   public Compactor(int x, int y) {
      super(x, y, classSchema);
   }
}
