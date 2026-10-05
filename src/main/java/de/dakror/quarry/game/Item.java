package de.dakror.quarry.game;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.ui.PalletDrawable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;

public class Item {
   private static final Item.ItemType[] items = new Item.ItemType[65536];
   public static final Item.ItemType[] palletSourceItems = new Item.ItemType[65536];
   public static final Item.ItemType[] palletTargetItems = new Item.ItemType[256];
   private static final Item.ItemCategory[] categories = new Item.ItemCategory[256];
   private static final Item.FluidType[] fluids = new Item.FluidType[256];
   private static final Item.Element[] elements = new Item.Element[256];
   private static final Item.Composite[] composites = new Item.Composite[256];

   public static Item.ItemType get(short value) {
      return items[value & '\uffff'];
   }

   public static Item.ItemCategory category(byte value) {
      return categories[value & 255];
   }

   public static Item.FluidType fluid(byte value) {
      return fluids[value & 255];
   }

   public static Item.ItemType get(Item.ItemType base, Item.ItemType variant) {
      Item.ItemType t = items[base.value & 255 | variant.value & '\uff00'];
      if (t != base && t != null) {
         return t;
      } else {
         throw new IllegalArgumentException("Variant " + variant + " of " + base + " does not exist");
      }
   }

   public static boolean exists(Item.ItemType base, Item.ItemType variant) {
      return items[base.value & 255 | variant.value & '\uff00'] != null;
   }

   public static Item.ItemType base(Item.ItemType type) {
      return items[type.value & 255];
   }

   public static Item.Element element(Item.ItemType type) {
      return elements[type.value >> 8 & 0xFF];
   }

   public static Item.Composite composite(Item.ItemType type) {
      return composites[type.value >> 8 & 0xFF];
   }

   static {
      for (Item.Element e : Item.Element.values) {
         if (elements[e.value & 255] != null) {
            throw new IllegalStateException("Element with ID " + e.value + " is already taken!");
         }

         elements[e.value & 255] = e;
      }

      for (Item.Composite e : Item.Composite.values) {
         if (composites[e.value & 255] != null) {
            throw new IllegalStateException("Composite with ID " + e.value + " is already taken!");
         }

         composites[e.value & 255] = e;
      }

      for (Item.ItemType t : Item.ItemType.values) {
         if (items[t.value & '\uffff'] != null) {
            throw new IllegalStateException("Item with ID " + t.value + " is already taken!");
         }

         items[t.value & '\uffff'] = t;
         if (t.stackable != null) {
            palletSourceItems[t.stackable.value & '\uffff'] = t;
            palletTargetItems[t.value >> 8 & 0xFF] = t.stackable;
         }
      }

      for (Item.FluidType t : Item.FluidType.values) {
         if (fluids[t.type.value & 255] != null) {
            throw new IllegalStateException("Fluid with ID " + t.type.value + " is already taken!");
         }

         fluids[t.type.value & 255] = t;
      }

      for (Item.ItemCategory t : Item.ItemCategory.values) {
         if (categories[t.id & 255] != null) {
            throw new IllegalStateException("Category with ID " + t.id + " is already taken!");
         }

         categories[t.id & 255] = t;
      }
   }

   public static enum Composite {
      Stone(200),
      Steel(201),
      Bronze(202),
      Pyrite(203),
      Quartz(204),
      HardenedSteel(205);

      public static final Item.Composite[] values = values();
      public final byte value;

      private Composite(int value) {
         this.value = (byte)value;
      }
   }

   public static enum Element {
      C(1),
      Fe(2),
      Cu(3),
      Sn(4),
      Si(5),
      S(6),
      Au(7),
      Ti(8);

      public static final Item.Element[] values = values();
      public final byte value;

      private Element(int value) {
         this.value = (byte)value;
      }
   }

   public static enum FluidType {
      MoltenMetal(1, Item.ItemType._MoltenMetal, 1.0F, 0.2F),
      Water(2, Item.ItemType.Water, 0.2F, 0.5F),
      Steam(3, Item.ItemType.Steam, 0.0F, 0.7F),
      PressurizedSteam(4, Item.ItemType.PressurizedSteam, 0.0F, 0.9F),
      CrudeOil(5, Item.ItemType.CrudeOil, 0.5F, 0.4F),
      IntermediateOilToColumn(6, Item.ItemType.IntermediateOilToColumn, 0.4F, 0.4F),
      IntermediateOilToRefinery(7, Item.ItemType.IntermediateOilToRefinery, 0.4F, 0.4F),
      RefinedOil(8, Item.ItemType.RefinedOil, 0.25F, 0.6F),
      Lubricant(9, Item.ItemType.Lubricant, 0.4F, 0.4F),
      Gas(10, Item.ItemType.NaturalGas, 0.0F, 0.9F);

      public static final Item.FluidType[] values = values();
      public final byte id;
      public final Item.ItemType type;
      public final TextureRegion icon;
      public final float viscosity;
      public final float pressure;

      private FluidType(int id, Item.ItemType type, float viscosity, float pressure) {
         this.id = (byte)id;
         this.icon = Quarry.Q.atlas.findRegion("fluid_" + type.name);
         this.viscosity = viscosity;
         this.pressure = pressure;
         this.type = type;
      }
   }

   public static enum ItemCategory {
      Nul(255, null),
      Abstract(1, null),
      Material(2, "item_stone"),
      Fluid(3, "item_water"),
      RawOre(4, null),
      CoalFuel(5, "item_fuel"),
      Component(6, null),
      IntermediateOil(7, null),
      RawOreDust(8, "item_dust"),
      Stackable(9, "item_ore"),
      FilledPallet(10, "item_filled_pallet");

      public static final Item.ItemCategory[] values = values();
      public final byte id;
      public final TextureRegion icon;
      public final Drawable drawable;
      public final String title;

      private ItemCategory(int id, String icon) {
         this.icon = Quarry.Q.atlas.findRegion(icon);
         this.drawable = icon != null ? Quarry.Q.skin.getDrawable(icon) : null;
         this.title = Quarry.Q.i18n.get("cat." + this.name());
         this.id = (byte)id;
      }
   }

   public static enum ItemType {
      Nil(255, "nil", 0),
      Stone(1, "stone", 1, Item.ItemCategory.Material),
      Wood(2, "wood", 2, Item.ItemCategory.Material),
      StoneBrick(3, "stone_brick", 151, Item.ItemCategory.Material),
      Dirt(4, "dirt", 1, Item.ItemCategory.Material),
      Scaffolding(5, "scaffolding", 122, Item.ItemCategory.Material),
      Charcoal(6, "charcoal", 100, Item.ItemCategory.CoalFuel),
      Clay(7, "clay", 1, Item.ItemCategory.Material),
      Brick(8, "brick", 136, Item.ItemCategory.Material),
      WoodPlank(9, "planks", 41, Item.ItemCategory.Material),
      Sand(10, "sand", 70, Item.ItemCategory.Material),
      Glass(11, "glass", 1414, Item.ItemCategory.Material),
      Cement(12, "cement", 122, Item.ItemCategory.Material),
      ConcretePowder(13, "concretepowder", 306, Item.ItemCategory.Material),
      SyntheticQuartz(14, "syntheticquartz", 895742, Item.ItemCategory.Material),
      _Ore(20, "ore", 15, Item.ItemCategory.RawOre, Item.ItemCategory.Abstract),
      CoalOre(_Ore, 15, Item.Element.C, Item.ItemCategory.CoalFuel),
      IronOre(_Ore, 15, Item.Element.Fe),
      CopperOre(_Ore, 15, Item.Element.Cu),
      TinOre(_Ore, 15, Item.Element.Sn),
      _Gravel(21, "gravel", 48, Item.ItemCategory.Abstract),
      IronGravel(_Gravel, 48, Item.Element.Fe, Item.ItemCategory.RawOre),
      CopperGravel(_Gravel, 48, Item.Element.Cu, Item.ItemCategory.RawOre),
      TinGravel(_Gravel, 48, Item.Element.Sn, Item.ItemCategory.RawOre),
      StoneGravel(_Gravel, 41, Item.Composite.Stone),
      _Dust(220, "dust", 66, Item.ItemCategory.Abstract),
      CoalDust(_Dust, 45, Item.Element.C),
      IronDust(_Dust, 66, Item.Element.Fe, Item.ItemCategory.RawOre, Item.ItemCategory.RawOreDust),
      GoldDust(_Dust, 4612516, Item.Element.Au, Item.ItemCategory.RawOre, Item.ItemCategory.RawOreDust),
      CopperDust(_Dust, 66, Item.Element.Cu, Item.ItemCategory.RawOre, Item.ItemCategory.RawOreDust),
      TinDust(_Dust, 66, Item.Element.Sn, Item.ItemCategory.RawOre, Item.ItemCategory.RawOreDust),
      SiliconDust(_Dust, 21964, Item.Element.Si),
      SulfurDust(_Dust, 36270, Item.Element.S),
      TitaniumDust(_Dust, 356560, Item.Element.Ti),
      StoneDust(_Dust, 64, Item.Composite.Stone),
      BronzeDust(_Dust, 278, Item.Composite.Bronze, Item.ItemCategory.RawOre, Item.ItemCategory.RawOreDust),
      PyriteDust(_Dust, 8180, Item.Composite.Pyrite),
      QuartzDust(_Dust, 5774, Item.Composite.Quartz),
      _MoltenMetal(40, "molten_metal", 0, Item.ItemCategory.Fluid, Item.ItemCategory.Abstract),
      MoltenIron(_MoltenMetal, 0, Item.Element.Fe),
      MoltenGold(_MoltenMetal, 11532, Item.Element.Au),
      MoltenCopper(_MoltenMetal, 0, Item.Element.Cu),
      MoltenTin(_MoltenMetal, 0, Item.Element.Sn),
      MoltenSilicon(_MoltenMetal, 55, Item.Element.Si),
      MoltenSteel(_MoltenMetal, 1, Item.Composite.Steel),
      MoltenBronze(_MoltenMetal, 1, Item.Composite.Bronze),
      MoltenTitanium(_MoltenMetal, 1, Item.Element.Ti),
      _Ingot(23, "ingot", 490, Item.ItemCategory.Abstract),
      IronIngot(_Ingot, 490, Item.Element.Fe),
      GoldIngot(_Ingot, 11532029, Item.Element.Au),
      CopperIngot(_Ingot, 490, Item.Element.Cu),
      TinIngot(_Ingot, 490, Item.Element.Sn),
      SteelIngot(_Ingot, 1452, Item.Composite.Steel),
      BronzeIngot(_Ingot, 1434, Item.Composite.Bronze),
      TitaniumIngot(_Ingot, 1000000, Item.Element.Ti),
      _Wire(24, "wire", 0, Item.ItemCategory.Abstract),
      GoldWire(_Wire, 1441579, Item.Element.Au),
      CopperWire(_Wire, 136, Item.Element.Cu),
      TinWire(_Wire, 136, Item.Element.Sn),
      SteelWire(_Wire, 257, Item.Composite.Steel),
      TitaniumWire(_Wire, 0, Item.Element.Ti),
      _Plate(25, "plate", 690, Item.ItemCategory.Abstract),
      IronPlate(_Plate, 690, Item.Element.Fe),
      GoldPlate(_Plate, 11532229, Item.Element.Au),
      CopperPlate(_Plate, 690, Item.Element.Cu),
      TinPlate(_Plate, 690, Item.Element.Sn),
      SteelPlate(_Plate, 1652, Item.Composite.Steel),
      BronzePlate(_Plate, 1634, Item.Composite.Bronze),
      TitaniumPlate(_Plate, 1000200, Item.Element.Ti),
      HardenedSteelPlate(_Plate, 1078904, Item.Composite.HardenedSteel),
      _Tube(26, "tube", 0, Item.ItemCategory.Abstract),
      CopperTube(_Tube, 810, Item.Element.Cu),
      SteelTube(_Tube, 3505, Item.Composite.Steel),
      Magnet(50, "magnet", 1151, Item.ItemCategory.Component),
      CoalChunk(51, "coalchunk", 460, Item.ItemCategory.Component),
      CarbonBlock(52, "carbonblock", 1940, Item.ItemCategory.Component),
      Dynamo(53, "dynamo", 18874, Item.ItemCategory.Component),
      PlasticBeads(54, "plasticbeads", 1000, Item.ItemCategory.Component),
      SiliconWafer(55, "siliconwafer", 4757, Item.ItemCategory.Component),
      PlasticCasing(56, "plasticcasing", 244490, Item.ItemCategory.Component),
      Battery(57, "battery", 889218, Item.ItemCategory.Component),
      Chip(58, "chip", 136641952, Item.ItemCategory.Component),
      Turbine(59, "turbine", 1078847, Item.ItemCategory.Component),
      Rotor(60, "rotor", 156747, Item.ItemCategory.Component),
      MachineFrame(61, "machineframe", 29208, Item.ItemCategory.Component),
      AdvancedMachineFrame(62, "advancedmachineframe", 6679663, Item.ItemCategory.Component),
      PlateStack(63, "platestack", 1078504, Item.ItemCategory.Component),
      SteelCable(64, "steel_cable", 39688, Item.ItemCategory.Component),
      WoodChips(65, "wood_chips", 1, Item.ItemCategory.Component),
      Cellulose(66, "cellulose", 33502, Item.ItemCategory.Component),
      Paper(67, "paper", 167534, Item.ItemCategory.Component),
      Blueprint(68, "blueprint", 677712, Item.ItemCategory.Component),
      Gunpowder(71, "gunpowder", 72850, Item.ItemCategory.Component),
      Dynamite(70, "dynamite", 1640401, Item.ItemCategory.Component),
      Water(100, "water", 100, Item.ItemCategory.Fluid),
      Steam(101, "steam", 200, Item.ItemCategory.Fluid),
      PressurizedSteam(102, "pressurized_steam", 400, Item.ItemCategory.Fluid),
      CrudeOil(103, "crude_oil", 500, Item.ItemCategory.Fluid),
      IntermediateOilToColumn(104, "oil", 0, Item.ItemCategory.Fluid, Item.ItemCategory.IntermediateOil),
      IntermediateOilToRefinery(105, "oil", 0, Item.ItemCategory.Fluid, Item.ItemCategory.IntermediateOil),
      RefinedOil(106, "oil", 1000, Item.ItemCategory.Fluid),
      Lubricant(107, "lubricant", 3000, Item.ItemCategory.Fluid),
      NaturalGas(108, "gas", 1000000, Item.ItemCategory.Fluid),
      EmptyBarrel(150, "empty_barrel", 46990),
      WaterBarrel(151, "water_barrel", 50793),
      CrudeOilBarrel(152, "crude_oil_barrel", 53993),
      RefinedOilBarrel(153, "oil_barrel", 57993),
      LubricantBarrel(154, "lubricant_barrel", 73993),
      EmptyTank(170, "empty_tank", 66894),
      GasTank(171, "gas_tank", 12071242),
      PressurizedSteamTank(172, "pressurized_steam_tank", 76041),
      _AnyStackable(190, "ore", 0),
      Pallet(191, "pallet", 0),
      _FilledPallet(Pallet, 1, "filled", 0),
      StonePallet(Pallet, 2, Stone),
      WoodPallet(Pallet, 3, Wood),
      StoneBrickPallet(Pallet, 4, StoneBrick),
      DirtPallet(Pallet, 5, Dirt),
      ScaffoldingPallet(Pallet, 6, Scaffolding),
      CharcoalPallet(Pallet, 7, Charcoal),
      ClayPallet(Pallet, 8, Clay),
      BrickPallet(Pallet, 9, Brick),
      WoodPlankPallet(Pallet, 10, WoodPlank),
      SandPallet(Pallet, 11, Sand),
      GlassPallet(Pallet, 12, Glass),
      CementPallet(Pallet, 13, Cement),
      ConcretePowderPallet(Pallet, 14, ConcretePowder),
      SyntheticQuartzPallet(Pallet, 15, SyntheticQuartz),
      CoalOrePallet(Pallet, 16, CoalOre),
      IronOrePallet(Pallet, 17, IronOre),
      CopperOrePallet(Pallet, 18, CopperOre),
      TinOrePallet(Pallet, 19, TinOre),
      IronGravelPallet(Pallet, 20, IronGravel),
      CopperGravelPallet(Pallet, 21, CopperGravel),
      TinGravelPallet(Pallet, 22, TinGravel),
      StoneGravelPallet(Pallet, 23, StoneGravel),
      CoalDustPallet(Pallet, 24, CoalDust),
      IronDustPallet(Pallet, 25, IronDust),
      GoldDustPallet(Pallet, 26, GoldDust),
      CopperDustPallet(Pallet, 27, CopperDust),
      TinDustPallet(Pallet, 28, TinDust),
      SiliconDustPallet(Pallet, 29, SiliconDust),
      SulfurDustPallet(Pallet, 30, SulfurDust),
      TitaniumDustPallet(Pallet, 31, TitaniumDust),
      StoneDustPallet(Pallet, 32, StoneDust),
      BronzeDustPallet(Pallet, 33, BronzeDust),
      PyriteDustPallet(Pallet, 34, PyriteDust),
      QuartzDustPallet(Pallet, 35, QuartzDust),
      IronIngotPallet(Pallet, 36, IronIngot),
      GoldIngotPallet(Pallet, 37, GoldIngot),
      CopperIngotPallet(Pallet, 38, CopperIngot),
      TinIngotPallet(Pallet, 39, TinIngot),
      SteelIngotPallet(Pallet, 40, SteelIngot),
      BronzeIngotPallet(Pallet, 41, BronzeIngot),
      TitaniumIngotPallet(Pallet, 42, TitaniumIngot),
      GoldWirePallet(Pallet, 43, GoldWire),
      CopperWirePallet(Pallet, 44, CopperWire),
      TinWirePallet(Pallet, 45, TinWire),
      SteelWirePallet(Pallet, 46, SteelWire),
      TitaniumWirePallet(Pallet, 47, TitaniumWire),
      IronPlatePallet(Pallet, 48, IronPlate),
      GoldPlatePallet(Pallet, 49, GoldPlate),
      CopperPlatePallet(Pallet, 50, CopperPlate),
      TinPlatePallet(Pallet, 51, TinPlate),
      SteelPlatePallet(Pallet, 52, SteelPlate),
      BronzePlatePallet(Pallet, 53, BronzePlate),
      TitaniumPlatePallet(Pallet, 54, TitaniumPlate),
      HardenedSteelPlatePallet(Pallet, 55, HardenedSteelPlate),
      CopperTubePallet(Pallet, 56, CopperTube),
      SteelTubePallet(Pallet, 57, SteelTube),
      MagnetPallet(Pallet, 58, Magnet),
      CoalChunkPallet(Pallet, 59, CoalChunk),
      CarbonBlockPallet(Pallet, 60, CarbonBlock),
      DynamoPallet(Pallet, 61, Dynamo),
      PlasticBeadsPallet(Pallet, 62, PlasticBeads),
      SiliconWaferPallet(Pallet, 63, SiliconWafer),
      PlasticCasingPallet(Pallet, 64, PlasticCasing),
      BatteryPallet(Pallet, 65, Battery),
      ChipPallet(Pallet, 66, Chip),
      TurbinePallet(Pallet, 67, Turbine),
      RotorPallet(Pallet, 68, Rotor),
      MachineFramePallet(Pallet, 69, MachineFrame),
      AdvancedMachineFramePallet(Pallet, 70, AdvancedMachineFrame),
      PlateStackPallet(Pallet, 71, PlateStack),
      SteelCablePallet(Pallet, 72, SteelCable),
      WoodChipsPallet(Pallet, 73, WoodChips),
      CellulosePallet(Pallet, 74, Cellulose),
      PaperPallet(Pallet, 75, Paper),
      BlueprintPallet(Pallet, 76, Blueprint),
      GunpowderPallet(Pallet, 77, Gunpowder),
      DynamitePallet(Pallet, 78, Dynamite),
      EmptyBarrelPallet(Pallet, 79, EmptyBarrel),
      WaterBarrelPallet(Pallet, 80, WaterBarrel),
      CrudeOilBarrelPallet(Pallet, 81, CrudeOilBarrel),
      RefinedOilBarrelPallet(Pallet, 82, RefinedOilBarrel),
      LubricantBarrelPallet(Pallet, 83, LubricantBarrel),
      EmptyTankPallet(Pallet, 84, EmptyTank),
      GasTankPallet(Pallet, 85, GasTank),
      PressurizedSteamTankPallet(Pallet, 86, PressurizedSteamTank);

      public static final Item.ItemType[] values = values();
      public final short value;
      public final String title;
      public final String name;
      public final int worth;
      public final EnumSet<Item.ItemCategory> categories;
      public TextureRegion icon;
      public Drawable drawable;
      public final Item.ItemType stackable;

      private ItemType(Item.ItemType type, int worth, Item.Element element, Item.ItemCategory... categories) {
         this(type, element.value, element.name().toLowerCase(), worth, categories);
      }

      private ItemType(Item.ItemType type, int worth, Item.Composite composite, Item.ItemCategory... categories) {
         this(type, composite.value, composite.name().toLowerCase(), worth, categories);
      }

      private ItemType(Item.ItemType type, int meta, String name, int worth, Item.ItemCategory... categories) {
         this.value = (short)(type.value | (byte)meta << 8);
         this.worth = worth;
         this.categories = EnumSet.copyOf(type.categories);
         this.categories.remove(Item.ItemCategory.Abstract);
         this.categories.add(Item.ItemCategory.Stackable);
         Collections.addAll(this.categories, categories);
         this.title = Quarry.Q.i18n.get("item." + this.name().replace("_", ""));
         this.name = name + "_" + type.name;
         this.icon = Quarry.Q.atlas.findRegion("item_" + this.name);
         if (this.icon == null) {
            this.icon = type.icon;
            this.drawable = type.drawable;
         } else {
            this.drawable = Quarry.Q.skin.getDrawable("item_" + this.name);
         }

         this.stackable = null;
         if (type.name.equals("molten_metal")) {
            this.icon = Quarry.Q.atlas.findRegion("item_molten_metal_actual");
            this.drawable = Quarry.Q.skin.getDrawable("item_molten_metal_actual");
         }
      }

      private ItemType(int value, String name, int worth, Item.ItemCategory... categories) {
         this.value = (short)((byte)value & 255);
         this.worth = worth;
         this.name = name;
         this.title = Quarry.Q.i18n.get("item." + this.name().replace("_", ""));
         this.categories = EnumSet.noneOf(Item.ItemCategory.class);
         this.categories.add(Item.ItemCategory.Stackable);
         Collections.addAll(this.categories, categories);
         this.stackable = null;
         this.icon = Quarry.Q.atlas.findRegion("item_" + name);
         if (this.icon == null) {
            throw new IllegalArgumentException("could not find texture for item " + this);
         } else {
            this.drawable = Quarry.Q.skin.getDrawable("item_" + name);
         }
      }

      private ItemType(Item.ItemType type, int meta, Item.ItemType stackable) {
         this.value = (short)(type.value | (byte)meta << 8);
         this.worth = 24 * stackable.worth;
         this.categories = EnumSet.copyOf(type.categories);
         this.categories.add(Item.ItemCategory.FilledPallet);
         this.stackable = stackable;
         this.name = stackable.name + "_" + type.name;
         this.title = stackable.title + " " + type.title;
         this.icon = type.icon;
         this.drawable = new PalletDrawable((TextureRegionDrawable)type.drawable, (TextureRegionDrawable)stackable.drawable);
      }
   }

   public static class Items {
      public final Item.Items.Amount[] entries;

      public Items(Item.Items.Amount[] entries) {
         this.entries = entries;
      }

      public Items(Object... param) {
         if (param.length % 2 != 0) {
            throw new IllegalArgumentException("Must be multiple of 2 parameter count");
         } else {
            this.entries = new Item.Items.Amount[param.length / 2];

            for (int i = 0; i < param.length / 2; i++) {
               try {
                  Object o = param[i * 2];
                  if (o instanceof Item.ItemType) {
                     this.entries[i] = new Item.Items.Amount((Item.ItemType)o, (Integer)param[i * 2 + 1]);
                  } else {
                     this.entries[i] = new Item.Items.Amount((Item.ItemCategory)o, (Integer)param[i * 2 + 1]);
                  }
               } catch (RuntimeException var4) {
                  throw new IllegalArgumentException("Illegal arguments given for Items", var4);
               }
            }
         }
      }

      public int getAmount(Item.ItemType item) {
         for (Item.Items.Amount e : this.entries) {
            if (e != null && e.item == item) {
               return e.getAmount();
            }
         }

         return 0;
      }

      public int getAmount(Item.ItemCategory cat) {
         for (Item.Items.Amount e : this.entries) {
            if (e != null && e.cat == cat) {
               return e.getAmount();
            }
         }

         return 0;
      }

      public void sort() {
         try {
            Arrays.sort(this.entries, new Comparator<Item.Items.Amount>() {
               public int compare(Item.Items.Amount a, Item.Items.Amount b) {
                  Item.ItemType ab = Item.base(a.item);
                  Item.ItemType bb = Item.base(b.item);
                  return ab == bb ? (a.item.value & 65535) - (b.item.value & 65535) : ab.value - bb.value;
               }
            });
         } catch (NullPointerException var2) {
         }
      }

      @Override
      public String toString() {
         return Arrays.toString((Object[])this.entries);
      }

      public static class Amount {
         private Item.ItemType item;
         private Item.ItemCategory cat;
         private int amount;

         public Amount(Item.ItemType item, int amount) {
            this.item = item;
            this.amount = amount;
            this.cat = null;
         }

         public Amount(Item.ItemCategory cat, int amount) {
            this.cat = cat;
            this.amount = amount;
            this.item = null;
         }

         @Override
         public String toString() {
            return this.cat != null ? "C " + this.cat + "=" + this.getAmount() : this.item + "=" + this.getAmount();
         }

         public int getAmount() {
            return this.amount;
         }

         public Item.ItemType getItem() {
            return this.item;
         }

         public void setItem(Item.ItemType item) {
            this.item = item;
         }

         public void setCat(Item.ItemCategory cat) {
            this.cat = cat;
         }

         public Item.ItemCategory getCat() {
            return this.cat;
         }
      }
   }
}
