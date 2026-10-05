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

public class RockCrusher extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.RockCrusher,
         2,
         2,
         "crusher",
         new Item.Items(Item.ItemType.Stone, 15, Item.ItemType.SteelIngot, 15, Item.ItemType.Scaffolding, 5),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(8.0F, "ore")
                     .input(new Item.Items.Amount(Item.ItemType._Ore, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Gravel, 2, 0))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "stone")
                     .input(new Item.Items.Amount(Item.ItemType.Stone, 1))
                     .output(new Item.Items.Amount(Item.ItemType.StoneGravel, 2))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "wood")
                     .input(new Item.Items.Amount(Item.ItemType.Wood, 1))
                     .output(new Item.Items.Amount(Item.ItemType.WoodChips, 20))
                     .science(Science.ScienceType.Blueprints)
               );
            }
         },
         new Sfx("rockcrusher" + Const.SFX_FORMAT),
         true,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            1,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType._Ore, Item.ItemType.Stone, Item.ItemType.Wood).exclude(Item.ItemType.CoalOre)
         )
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OreProcessing})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public RockCrusher(int x, int y) {
      super(x, y, classSchema);
   }
}
