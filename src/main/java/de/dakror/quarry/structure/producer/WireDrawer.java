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

public class WireDrawer extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.WireDrawer,
         1,
         7,
         "wiredrawer",
         new Item.Items(Item.ItemType.StoneBrick, 7, Item.ItemType.Scaffolding, 5, Item.ItemType.SteelIngot, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(45.0F, "wire")
                     .input(new Item.Items.Amount(Item.ItemType.SteelIngot, 1))
                     .output(new Item.Items.Amount(Item.ItemType.SteelWire, 8))
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "wire")
                     .input(new Item.Items.Amount(Item.ItemType.CopperIngot, 1))
                     .output(new Item.Items.Amount(Item.ItemType.CopperWire, 8))
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "wire")
                     .input(new Item.Items.Amount(Item.ItemType.TinIngot, 1))
                     .output(new Item.Items.Amount(Item.ItemType.TinWire, 8))
               );
               this.add(
                  new RecipeList.Recipe(45.0F, "wire")
                     .input(new Item.Items.Amount(Item.ItemType.GoldIngot, 1))
                     .output(new Item.Items.Amount(Item.ItemType.GoldWire, 8))
               );
            }
         },
         new Sfx("drawer" + Const.SFX_FORMAT),
         true,
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            0,
            6,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemType.SteelIngot, Item.ItemType.CopperIngot, Item.ItemType.TinIngot, Item.ItemType.GoldIngot)
         )
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Metalworking});

   public WireDrawer(int x, int y) {
      super(x, y, classSchema);
   }
}
