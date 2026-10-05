package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.util.Sfx;

public class InductionFurnace extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         1,
         StructureType.InductionFurnace,
         5,
         5,
         "inductionfurnace",
         new Item.Items(
            Item.ItemType.MachineFrame,
            8,
            Item.ItemType.CopperWire,
            150,
            Item.ItemType.SteelIngot,
            3,
            Item.ItemType.CopperTube,
            40,
            Item.ItemType.Brick,
            50,
            Item.ItemType.CarbonBlock,
            20
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(2.0F, "ore", 500)
                     .input(new Item.Items.Amount(Item.ItemType._Ore, 1), new Item.Items.Amount(Item.ItemType.Water, 3000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 2, 0), new Item.Items.Amount(Item.ItemType.Steam, 15000))
               );
               this.add(
                  new RecipeList.Recipe(2.0F, "gravel", 750)
                     .input(new Item.Items.Amount(Item.ItemType._Gravel, 4), new Item.Items.Amount(Item.ItemType.Water, 7000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 4, 0), new Item.Items.Amount(Item.ItemType.Steam, 35000))
               );
               this.add(
                  new RecipeList.Recipe(2.0F, "dust", 1000)
                     .input(new Item.Items.Amount(Item.ItemType._Dust, 12), new Item.Items.Amount(Item.ItemType.Water, 14000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 6, 0), new Item.Items.Amount(Item.ItemType.Steam, 70000))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "steel", 2000)
                     .input(new Item.Items.Amount(Item.ItemType.IronIngot, 12), new Item.Items.Amount(Item.ItemType.Water, 18000))
                     .output(new Item.Items.Amount(Item.ItemType.SteelIngot, 6), new Item.Items.Amount(Item.ItemType.Steam, 90000))
               );
               this.add(
                  new RecipeList.Recipe(3.0F, "titanium", 4000)
                     .input(new Item.Items.Amount(Item.ItemType.TitaniumDust, 12), new Item.Items.Amount(Item.ItemType.Water, 25000))
                     .output(new Item.Items.Amount(Item.ItemType.TitaniumIngot, 6), new Item.Items.Amount(Item.ItemType.Steam, 125000))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 100000);
            }
         },
         new Sfx("node" + Const.SFX_FORMAT, 0.7F),
         true,
         new Dock(2, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 0, Direction.West, Dock.DockType.FluidOut),
         new Dock(
            2,
            4,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemCategory.RawOre, Item.ItemType.IronIngot, Item.ItemType.TitaniumDust).exclude(Item.ItemType.CoalOre)
         ),
         new Dock(0, 4, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(0, 2, Direction.West, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.HighTech})
      .flags(Schema.Flags.TextureAlwaysUpright, Schema.Flags.MirroredTextureHorizontal);
   public static final ProducerStructure.ProducerSchema classSchemaV110 = new ProducerStructure.ProducerSchema(
         0,
         StructureType.InductionFurnace,
         5,
         5,
         "inductionfurnace",
         new Item.Items(
            Item.ItemType.MachineFrame,
            8,
            Item.ItemType.CopperWire,
            150,
            Item.ItemType.SteelIngot,
            3,
            Item.ItemType.CopperTube,
            40,
            Item.ItemType.Brick,
            50,
            Item.ItemType.CarbonBlock,
            20
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(2.0F, "ore", 500)
                     .input(new Item.Items.Amount(Item.ItemType._Ore, 1), new Item.Items.Amount(Item.ItemType.Water, 3000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 1, 0))
               );
               this.add(
                  new RecipeList.Recipe(2.0F, "gravel", 750)
                     .input(new Item.Items.Amount(Item.ItemType._Gravel, 4), new Item.Items.Amount(Item.ItemType.Water, 7000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 3, 0))
               );
               this.add(
                  new RecipeList.Recipe(2.0F, "dust", 1000)
                     .input(new Item.Items.Amount(Item.ItemType._Dust, 12), new Item.Items.Amount(Item.ItemType.Water, 14000))
                     .output(new RecipeList.ParameterizedAmount(Item.ItemType._Ingot, 5, 0))
               );
            }

            @Override
            public void validateRecipes(Schema schema) {
               super.validateRecipes(schema);
               this.fluidInputs.set(0, 50000);
            }
         },
         new Sfx("node" + Const.SFX_FORMAT, 0.7F),
         true,
         new Dock(2, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(
            2,
            4,
            Direction.North,
            Dock.DockType.ItemIn,
            new Dock.DockFilter(Item.ItemCategory.RawOre).exclude(Item.ItemType.CoalOre, Item.ItemType.TitaniumDust)
         ),
         new Dock(0, 4, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemType.Water)),
         new Dock(0, 2, Direction.West, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.HighTech})
      .flags(Schema.Flags.TextureAlwaysUpright, Schema.Flags.MirroredTextureHorizontal);

   public InductionFurnace(int x, int y, int version) {
      super(x, y, selectSchema(version, new ProducerStructure.ProducerSchema[]{classSchemaV110, classSchema}));
      ((CInventory)this.outputInventories[0]).setPumpOutSpeed(0.0F);
      if (this.outputInventories.length == 2) {
         ((CTank)this.outputInventories[1]).setPumpOutDelay(0.0F).setMaxOutput(10000);
      }
   }

   public InductionFurnace(int x, int y) {
      super(x, y, classSchema);
      ((CInventory)this.outputInventories[0]).setPumpOutSpeed(0.0F);
      ((CTank)this.outputInventories[1]).setPumpOutDelay(0.0F).setMaxOutput(10000);
   }
}
