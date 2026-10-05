package de.dakror.quarry.structure.producer;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;

public class IngotMold extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.IngotMold,
         2,
         2,
         "ingotmold",
         new Item.Items(Item.ItemType.Stone, 30),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(10.0F, "ingot")
                     .input(new Item.Items.Amount(Item.ItemType._MoltenMetal, 1000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 1, 0))
               );
            }
         },
         null,
         true,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType._MoltenMetal).exclude(Item.ItemType.MoltenSilicon))
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});

   public IngotMold(int x, int y) {
      super(x, y, classSchema);
   }
}
