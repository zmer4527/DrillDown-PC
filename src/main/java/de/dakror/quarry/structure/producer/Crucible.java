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

public class Crucible extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Crucible,
         3,
         3,
         "crucible",
         new Item.Items(Item.ItemType.Brick, 100, Item.ItemType.SteelIngot, 75, Item.ItemType.BronzePlate, 80),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(90.0F, "wafer")
                     .input(new Item.Items.Amount(Item.ItemType.MoltenSilicon, 6000))
                     .output(new Item.Items.Amount(Item.ItemType.SiliconWafer, 70))
               );
            }
         },
         new Sfx("crucible" + Const.SFX_FORMAT),
         false,
         new Dock(1, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(1, 2, Direction.North, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.MoltenSilicon))
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.WaferGrowth});

   public Crucible(int x, int y) {
      super(x, y, classSchema);
   }
}
