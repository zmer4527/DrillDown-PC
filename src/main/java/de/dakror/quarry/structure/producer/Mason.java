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

public class Mason extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Mason,
         2,
         2,
         "mason",
         new Item.Items(Item.ItemType.Stone, 5, Item.ItemType.Wood, 15, Item.ItemType.Scaffolding, 15, Item.ItemType.IronIngot, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(15.0F, "stone")
                     .input(new Item.Items.Amount(Item.ItemType.Stone, 1))
                     .output(new Item.Items.Amount(Item.ItemType.StoneBrick, 1))
               );
            }
         },
         new Sfx("mason" + Const.SFX_FORMAT),
         true,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Stone))
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});

   public Mason(int x, int y) {
      super(x, y, classSchema);
   }
}
