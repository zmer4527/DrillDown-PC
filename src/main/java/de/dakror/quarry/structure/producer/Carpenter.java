package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class Carpenter extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Carpenter,
         2,
         2,
         "carpenter",
         new Item.Items(Item.ItemType.Stone, 15, Item.ItemType.Wood, 5, Item.ItemType.IronIngot, 6),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(12.0F, "scaffolding")
                     .input(new Item.Items.Amount(Item.ItemType.Wood, 1))
                     .output(new Item.Items.Amount(Item.ItemType.Scaffolding, 1))
               );
            }
         },
         new Sfx("carpenter" + Const.SFX_FORMAT),
         true,
         new Dock(1, 1, Direction.North, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.South, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Wood))
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});

   public Carpenter(int x, int y) {
      super(x, y, classSchema);
   }
}
