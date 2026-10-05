package de.dakror.quarry.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import de.dakror.quarry.Quarry;

public class Science {
   public static final Science.ScienceType[] sciences = new Science.ScienceType[256];
   private static int maxBuyCosts = 0;

   public static int getMaxBuyCosts() {
      return maxBuyCosts;
   }

   public static enum ScienceType {
      Start(0, null, null, 0.0F),
      OreProcessing(2, "icon_crusher", new Item.Items(Item.ItemType.IronOre, 60, Item.ItemType.CopperOre, 50), 37.0F, Start),
      Metalworking(3, "icon_fe_ingot", new Item.Items(Item.ItemType.IronIngot, 75, Item.ItemType.CopperIngot, 50), 55.0F, Start),
      SteelProduction(4, "icon_steel_ingot", new Item.Items(Item.ItemType.IronIngot, 30), 40.0F, Start),
      BetterStorage(5, "icon_storage", new Item.Items(Item.ItemType.Scaffolding, 60, Item.ItemType.StoneBrick, 90), 46.0F, SteelProduction),
      WaterUsage(6, "icon_water", new Item.Items(Item.ItemType.SteelIngot, 50, Item.ItemType.CopperTube, 30), 50.0F, Metalworking),
      CharcoalProduction(8, "icon_charcoal", new Item.Items(Item.ItemType.Wood, 75), 35.0F, Start),
      MineExpansion(9, "icon_drill_button", new Item.Items(Item.ItemType.IronIngot, 150, Item.ItemType.SteelPlate, 10), 50.0F, SteelProduction, WaterUsage),
      ConsiderateConstruction(
         11,
         "science_considerate",
         new Item.Items(Item.ItemType.Scaffolding, 30, Item.ItemType.Brick, 60, Item.ItemType.SteelIngot, 95),
         45.0F,
         SteelProduction
      ),
      Magnetism(12, "icon_magnet", new Item.Items(Item.ItemType.IronIngot, 150), 25.0F, Metalworking),
      Electricity(
         13,
         "science_power",
         new Item.Items(Item.ItemType.CopperWire, 100, Item.ItemType.CarbonBlock, 10, Item.ItemType.Magnet, 8, Item.ItemType.BronzePlate, 5),
         55.0F,
         Magnetism
      ),
      HighTech(22, "science_power", new Item.Items(Item.ItemType.MachineFrame, 40, Item.ItemType.SteelWire, 60), 50.0F, Electricity, WaterUsage),
      OilProcessing(14, "icon_crude_oil", new Item.Items(Item.ItemType.SteelTube, 70, Item.ItemType.Dynamo, 4), 70.0F, MineExpansion, HighTech),
      MineralExtraction(15, "icon_minerals", new Item.Items(Item.ItemType.StoneDust, 350), 105.0F, Electricity),
      WaferGrowth(16, "icon_wafer", new Item.Items(Item.ItemType.SiliconDust, 90), 65.0F, MineralExtraction),
      PlasticMolding(17, "icon_molding", new Item.Items(Item.ItemType.PlasticBeads, 200), 78.0F, OilProcessing),
      ComponentAssembly(
         18,
         "icon_assembly",
         new Item.Items(Item.ItemType.SiliconWafer, 30, Item.ItemType.PlasticCasing, 20, Item.ItemType.GoldDust, 15, Item.ItemType.TinWire, 250),
         98.0F,
         PlasticMolding,
         WaferGrowth
      ),
      SolarPower(
         19,
         "icon_solar",
         new Item.Items(Item.ItemType.SiliconWafer, 2, Item.ItemType.Glass, 90, Item.ItemType.CopperWire, 300, Item.ItemType.Battery, 25),
         25.0F,
         WaferGrowth
      ),
      HighPower(20, "science_power", new Item.Items(Item.ItemType.SteelCable, 65, Item.ItemType.TinPlate, 150, Item.ItemType.Battery, 10), 70.0F, HighTech),
      Boosting(21, "symb_ff", new Item.Items(Item.ItemType.BronzePlate, 240, Item.ItemType.Battery, 40), 50.0F, MineralExtraction),
      Blueprints(23, "icon_copy", new Item.Items(Item.ItemType.Wood, 250, Item.ItemType.IronPlate, 250), 40.0F, HighTech),
      AdvancedTransport(
         24, "icon_empty_barrel", new Item.Items(Item.ItemType.TinPlate, 80, Item.ItemType.SteelPlate, 60, Item.ItemType.WoodPlank, 120), 40.0F, HighTech
      );

      public static final Science.ScienceType[] values = values();
      public final TextureRegion icon;
      public final byte id;
      public final String title;
      public final String description;
      public final Item.Items costs;
      public final float workingTime;
      public final Science.ScienceType[] required;

      private ScienceType(int id, String icon, Item.Items costs, float workingTime, Science.ScienceType... required) {
         this.workingTime = workingTime;
         this.costs = costs;
         this.id = (byte)id;
         this.required = required;
         if (id > 0) {
            this.icon = Quarry.Q.atlas.findRegion(icon);
            if (icon.equals("temp")) {
               System.err.println("[ICON] science '" + this.name() + "'");
            }

            this.title = Quarry.Q.i18n.get("science." + this.name().toLowerCase());
            this.description = Quarry.Q.i18n.get("science." + this.name().toLowerCase() + ".desc");
            int count = 0;

            for (Item.Items.Amount a : costs.entries) {
               if (a.getCat() != null) {
                  throw new IllegalStateException("Cost categories are not allowed.");
               }

               count += a.getAmount();
            }

            Science.maxBuyCosts = Math.max(Science.maxBuyCosts, count);
         } else {
            this.icon = null;
            this.title = null;
            this.description = null;
         }

         if (Science.sciences[id] != null) {
            throw new IllegalStateException("Science with ID " + id + " is already taken!");
         } else {
            Science.sciences[id] = this;
         }
      }
   }
}
