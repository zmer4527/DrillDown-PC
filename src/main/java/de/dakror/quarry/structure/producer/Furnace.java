package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.OvenStructure;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class Furnace extends OvenStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Furnace,
         2,
         2,
         "smelter",
         new Item.Items(Item.ItemType.Stone, 15, Item.ItemType.Brick, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(7.5F, "ore")
                     .input(new Item.Items.Amount(Item.ItemType._Ore, 1), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._MoltenMetal, 1000, 0))
               );
               this.add(
                  new RecipeList.Recipe(7.5F, "gravel")
                     .input(new Item.Items.Amount(Item.ItemType._Gravel, 1), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._MoltenMetal, 750, 0))
                     .science(Science.ScienceType.OreProcessing)
               );
               this.add(
                  new RecipeList.Recipe(7.5F, "dust")
                     .input(new Item.Items.Amount(Item.ItemType._Dust, 1), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 1))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._MoltenMetal, 400, 0))
                     .science(Science.ScienceType.OreProcessing)
               );
            }
         },
         new Sfx("furnace" + Const.SFX_FORMAT),
         false,
         new Dock(1, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(
            0,
            1,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemCategory.RawOre).exclude(Item.ItemType.CoalOre, Item.ItemType.TitaniumDust)
         ),
         new Dock(1, 1, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemCategory.CoalFuel))
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});

   public Furnace(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected void initPfx() {
      this.pfx.scaleEffect(1.5F);
      this.pfx.setPosition((float)((this.x + 1) * 64), ((float)this.y + 0.75F) * 64.0F);
   }
}
