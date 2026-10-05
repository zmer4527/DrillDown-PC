package de.dakror.quarry.structure.power;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.GeneratorStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class WaterWheel extends GeneratorStructure {
   public static final GeneratorStructure.GeneratorSchema classSchema = new GeneratorStructure.GeneratorSchema(
         0,
         StructureType.WaterWheel,
         2,
         2,
         "waterwheel",
         new Item.Items(Item.ItemType.Wood, 20, Item.ItemType.WoodPlank, 10, Item.ItemType.Scaffolding, 8, Item.ItemType.SteelIngot, 2, Item.ItemType.Dynamo, 1),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(new GeneratorStructure.GeneratorRecipe("water", 1200).input(new Item.Items.Amount[]{new Item.Items.Amount(Item.ItemType.Water, 500)}));
            }
         },
         new Sfx("waterwheel" + Const.SFX_FORMAT),
         new Dock(0, 1, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(0, 1, Direction.North, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.WaterUsage, Science.ScienceType.Electricity})
      .flags(Schema.Flags.TextureAlwaysUpright);

   public WaterWheel(int x, int y) {
      super(x, y, classSchema);
   }
}
