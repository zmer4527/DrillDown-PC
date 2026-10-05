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

public class InjectionMolder extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         2,
         StructureType.InjectionMolder,
         3,
         3,
         "injectionmolder",
         new Item.Items(
            Item.ItemType.StoneBrick,
            120,
            Item.ItemType.MachineFrame,
            40,
            Item.ItemType.AdvancedMachineFrame,
            2,
            Item.ItemType.CopperIngot,
            150,
            Item.ItemType.SteelIngot,
            100,
            Item.ItemType.Rotor,
            16
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(5.0F, "sleeve", 500)
                     .input(new Item.Items.Amount(Item.ItemType.PlasticBeads, 30), new Item.Items.Amount(Item.ItemType.SteelWire, 4))
                     .output(new Item.Items.Amount(Item.ItemType.SteelCable, 1))
               );
               this.add(
                  new RecipeList.Recipe(34.0F, "casing", 800)
                     .input(new Item.Items.Amount(Item.ItemType.PlasticBeads, 170), new Item.Items.Amount(Item.ItemType.BronzeIngot, 1))
                     .output(new Item.Items.Amount(Item.ItemType.PlasticCasing, 1))
               );
            }
         },
         new Sfx("injection" + Const.SFX_FORMAT),
         true,
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.PlasticBeads)),
         new Dock(1, 0, Direction.South, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SteelWire, Item.ItemType.BronzeIngot)),
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(1, 2, Direction.North, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.PlasticMolding});
   public static final ProducerStructure.ProducerSchema classSchemaV104 = new ProducerStructure.ProducerSchema(
         1,
         StructureType.InjectionMolder,
         3,
         3,
         "injectionmolder",
         new Item.Items(
            Item.ItemType.StoneBrick,
            120,
            Item.ItemType.MachineFrame,
            40,
            Item.ItemType.AdvancedMachineFrame,
            2,
            Item.ItemType.CopperIngot,
            150,
            Item.ItemType.SteelIngot,
            100,
            Item.ItemType.Rotor,
            16
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(5.0F, "sleeve", 500)
                     .input(new Item.Items.Amount(Item.ItemType.PlasticBeads, 30), new Item.Items.Amount(Item.ItemType.SteelWire, 4))
                     .output(new Item.Items.Amount(Item.ItemType.SteelCable, 1))
               );
               this.add(
                  new RecipeList.Recipe(34.0F, "casing", 800)
                     .input(new Item.Items.Amount(Item.ItemType.PlasticBeads, 170))
                     .output(new Item.Items.Amount(Item.ItemType.PlasticCasing, 1))
               );
            }
         },
         new Sfx("injection" + Const.SFX_FORMAT),
         true,
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.PlasticBeads)),
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SteelWire)),
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(1, 2, Direction.North, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.PlasticMolding});
   public static final ProducerStructure.ProducerSchema classSchemaV103 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.InjectionMolder,
         3,
         3,
         "injectionmolder",
         new Item.Items(
            Item.ItemType.StoneBrick,
            120,
            Item.ItemType.MachineFrame,
            40,
            Item.ItemType.AdvancedMachineFrame,
            2,
            Item.ItemType.CopperIngot,
            150,
            Item.ItemType.SteelIngot,
            100,
            Item.ItemType.Rotor,
            16
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(34.0F, "casing", 800)
                     .input(new Item.Items.Amount(Item.ItemType.PlasticBeads, 170))
                     .output(new Item.Items.Amount(Item.ItemType.PlasticCasing, 1))
               );
            }
         },
         new Sfx("injection" + Const.SFX_FORMAT),
         true,
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.PlasticBeads)),
         new Dock(2, 1, Direction.East, Dock.DockType.ItemOut),
         new Dock(1, 2, Direction.North, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.PlasticMolding});

   public InjectionMolder(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchema, classSchemaV104, classSchemaV103}));
   }

   public InjectionMolder(int x, int y) {
      super(x, y, classSchema);
   }
}
