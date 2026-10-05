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

public class BallMill extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.BallMill,
         4,
         1,
         "ballmill",
         new Item.Items(Item.ItemType.Scaffolding, 10, Item.ItemType.IronPlate, 10, Item.ItemType.IronIngot, 6),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(10.0F, "sand")
                     .input(new Item.Items.Amount(Item.ItemType.StoneGravel, 1))
                     .output(new Item.Items.Amount(Item.ItemType.Sand, 2))
               );
               this.add(
                  new RecipeList.Recipe(15.0F, "ore")
                     .input(new Item.Items.Amount(Item.ItemType._Gravel, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Dust, 3, 0))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "stonedust")
                     .input(new Item.Items.Amount(Item.ItemType.Sand, 1))
                     .output(new Item.Items.Amount(Item.ItemType.StoneDust, 2))
               );
               this.add(
                  new RecipeList.Recipe(12.0F, "coal")
                     .input(new Item.Items.Amount(Item.ItemType.CoalOre, 1))
                     .output(new Item.Items.Amount(Item.ItemType.CoalDust, 3))
               );
            }
         },
         new Sfx("ballmill" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.West, Dock.DockType.ItemOut),
         new Dock(3, 0, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType._Gravel, Item.ItemType.CoalOre, Item.ItemType.Sand))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.OreProcessing});

   public BallMill(int x, int y) {
      super(x, y, classSchema);
   }
}
