package de.dakror.quarry.structure.power;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.GeneratorStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class SteamTurbine extends GeneratorStructure {
   public static final GeneratorStructure.GeneratorSchema classSchema = new GeneratorStructure.GeneratorSchema(
         0,
         StructureType.SteamTurbine,
         3,
         3,
         "steamturbine",
         new Item.Items(Item.ItemType.SteelPlate, 80, Item.ItemType.CopperTube, 20, Item.ItemType.Dynamo, 2, Item.ItemType.Rotor, 2),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new GeneratorStructure.GeneratorRecipe("steam", 18000)
                     .input(new Item.Items.Amount[]{new Item.Items.Amount(Item.ItemType.PressurizedSteam, 1000)})
                     .output(new Item.Items.Amount(Item.ItemType.Steam, 2500))
               );
            }
         },
         new Sfx("turbine" + Const.SFX_FORMAT),
         new Dock(0, 1, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.PressurizedSteam)),
         new Dock(1, 2, Direction.North, Dock.DockType.Power),
         new Dock(2, 1, Direction.East, Dock.DockType.FluidOut)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.WaterUsage, Science.ScienceType.Electricity});

   public SteamTurbine(int x, int y) {
      super(x, y, classSchema);
   }
}
