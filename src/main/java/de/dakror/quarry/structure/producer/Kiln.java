package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.OvenStructure;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class Kiln extends OvenStructure {
   public static ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
      0,
      StructureType.Kiln,
      1,
      3,
      "kiln",
      new Item.Items(Item.ItemType.Stone, 10, Item.ItemType.Dirt, 10),
      new RecipeList() {
         @Override
         protected void init() {
            this.add(
               new RecipeList.Recipe(16.0F, "brick")
                  .input(new Item.Items.Amount(Item.ItemCategory.CoalFuel, 2), new Item.Items.Amount(Item.ItemType.Clay, 8))
                  .output(new Item.Items.Amount(Item.ItemType.Brick, 3))
            );
            this.add(
               new RecipeList.Recipe(32.0F, "glass")
                  .input(new Item.Items.Amount(Item.ItemCategory.CoalFuel, 6), new Item.Items.Amount(Item.ItemType.Sand, 7))
                  .output(new Item.Items.Amount(Item.ItemType.Glass, 1))
                  .science(Science.ScienceType.OreProcessing)
            );
         }
      },
      new Sfx("kiln" + Const.SFX_FORMAT),
      true,
      new Dock(0, 0, Direction.East, Dock.DockType.ItemOut),
      new Dock(0, 0, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemCategory.CoalFuel)),
      new Dock(0, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Clay, Item.ItemType.Sand))
   );

   public Kiln(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected void initPfx() {
      this.pfx.setPosition(((float)this.x + 0.5F) * 64.0F, ((float)this.y + 0.4F) * 64.0F);
   }
}
