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

public class BlastFurnace extends OvenStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.BlastFurnace,
         4,
         4,
         "smelter",
         new Item.Items(Item.ItemType.StoneBrick, 30, Item.ItemType.Brick, 10, Item.ItemType.IronIngot, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(16.0F, "steel")
                     .input(new Item.Items.Amount(Item.ItemType.IronIngot, 3), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 9))
                     .output(new Item.Items.Amount(Item.ItemType.MoltenSteel, 2200))
               );
               this.add(
                  new RecipeList.Recipe(23.0F, "silicon")
                     .input(new Item.Items.Amount(Item.ItemType.SiliconDust, 10), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 24))
                     .output(new Item.Items.Amount(Item.ItemType.MoltenSilicon, 400))
                     .science(Science.ScienceType.MineralExtraction)
               );
               this.add(
                  new RecipeList.Recipe(40.0F, "titanium")
                     .input(new Item.Items.Amount(Item.ItemType.TitaniumDust, 1), new Item.Items.Amount(Item.ItemCategory.CoalFuel, 40))
                     .output(new Item.Items.Amount(Item.ItemType.MoltenTitanium, 400))
                     .science(Science.ScienceType.MineralExtraction)
               );
            }
         },
         new Sfx("furnace" + Const.SFX_FORMAT, 0.5F),
         true,
         new Dock(2, 0, Direction.South, Dock.DockType.FluidOut),
         new Dock(
            2, 3, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.IronIngot, Item.ItemType.SiliconDust, Item.ItemType.TitaniumDust)
         ),
         new Dock(3, 2, Direction.East, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemCategory.CoalFuel))
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.SteelProduction})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public BlastFurnace(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected void initPfx() {
      this.pfx.scaleEffect(3.5F);
      this.pfx.setPosition((float)((this.x + 2) * 64), ((float)this.y + 1.5F) * 64.0F);
   }
}
