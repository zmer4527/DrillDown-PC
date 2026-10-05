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

public class Lumberjack extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0, StructureType.Lumberjack, 2, 2, "lumberjack", new Item.Items(Item.ItemType.IronIngot, 3, Item.ItemType.Stone, 8), new RecipeList() {
            @Override
            protected void init() {
               this.add(new RecipeList.Recipe(40.0F, "wood").output(new Item.Items.Amount(Item.ItemType.Wood, 4)));
            }
         }, new Sfx("woodcutter" + Const.SFX_FORMAT), true, new Dock(0, 1, Direction.West, Dock.DockType.ItemOut)
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});

   public Lumberjack(int x, int y) {
      super(x, y, classSchema);
   }
}
