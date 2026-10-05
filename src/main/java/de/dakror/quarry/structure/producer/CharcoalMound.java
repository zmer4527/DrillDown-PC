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

public class CharcoalMound extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         1,
         StructureType.CharcoalMound,
         3,
         3,
         "charcoalmound",
         new Item.Items(Item.ItemType.Dirt, 40, Item.ItemType.Wood, 25),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(180.0F, "charcoal")
                     .input(new Item.Items.Amount(Item.ItemType.Wood, 10), new Item.Items.Amount(Item.ItemType.Dirt, 30))
                     .output(new Item.Items.Amount(Item.ItemType.Charcoal, 48))
               );
            }
         },
         new Sfx("charcoalmound" + Const.SFX_FORMAT),
         true,
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Wood)),
         new Dock(0, 0, Direction.South, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Dirt))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.CharcoalProduction});
   public static final ProducerStructure.ProducerSchema classSchemaV103 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.CharcoalMound,
         3,
         3,
         "charcoalmound",
         new Item.Items(Item.ItemType.Dirt, 40, Item.ItemType.Wood, 25),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(180.0F, "charcoal")
                     .input(new Item.Items.Amount(Item.ItemType.Wood, 5))
                     .output(new Item.Items.Amount(Item.ItemType.Charcoal, 24))
               );
            }
         },
         new Sfx("charcoalmound" + Const.SFX_FORMAT),
         true,
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Wood))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.CharcoalProduction});

   public CharcoalMound(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchema, classSchemaV103}));
   }

   public CharcoalMound(int x, int y) {
      super(x, y, classSchema);
   }
}
