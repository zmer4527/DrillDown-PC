package de.dakror.quarry.structure.base;

import de.dakror.quarry.structure.Boiler;
import de.dakror.quarry.structure.Booster;
import de.dakror.quarry.structure.DistillationColumn;
import de.dakror.quarry.structure.ItemDestroyer;
import de.dakror.quarry.structure.Refinery;
import de.dakror.quarry.structure.ScienceLab;
import de.dakror.quarry.structure.ShaftDrill;
import de.dakror.quarry.structure.ShaftDrillHead;
import de.dakror.quarry.structure.logistics.BrickChannel;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ConveyorBridge;
import de.dakror.quarry.structure.logistics.CopperTube;
import de.dakror.quarry.structure.logistics.Distributor;
import de.dakror.quarry.structure.logistics.ElectricConveyor;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.structure.logistics.Filter;
import de.dakror.quarry.structure.logistics.Hopper;
import de.dakror.quarry.structure.logistics.ItemLift;
import de.dakror.quarry.structure.logistics.ItemLiftBelow;
import de.dakror.quarry.structure.logistics.SteelTube;
import de.dakror.quarry.structure.logistics.TubeShaft;
import de.dakror.quarry.structure.logistics.TubeShaftBelow;
import de.dakror.quarry.structure.logistics.VacuumPump;
import de.dakror.quarry.structure.logistics.Valve;
import de.dakror.quarry.structure.power.AnchorPortal;
import de.dakror.quarry.structure.power.CableShaft;
import de.dakror.quarry.structure.power.CableShaftBelow;
import de.dakror.quarry.structure.power.Capacitor;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.structure.power.CopperCableCrossing;
import de.dakror.quarry.structure.power.GasTurbine;
import de.dakror.quarry.structure.power.HighPowerShaft;
import de.dakror.quarry.structure.power.HighPowerShaftBelow;
import de.dakror.quarry.structure.power.PowerPole;
import de.dakror.quarry.structure.power.PowerPoleGhost;
import de.dakror.quarry.structure.power.SolarPanel;
import de.dakror.quarry.structure.power.SolarPanelOutlet;
import de.dakror.quarry.structure.power.SteamTurbine;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.structure.power.SuperCapacitor;
import de.dakror.quarry.structure.power.WaterWheel;
import de.dakror.quarry.structure.producer.AirPurifier;
import de.dakror.quarry.structure.producer.ArcWelder;
import de.dakror.quarry.structure.producer.Assembler;
import de.dakror.quarry.structure.producer.BallMill;
import de.dakror.quarry.structure.producer.BarrelDrainer;
import de.dakror.quarry.structure.producer.BlastFurnace;
import de.dakror.quarry.structure.producer.Carpenter;
import de.dakror.quarry.structure.producer.Centrifuge;
import de.dakror.quarry.structure.producer.CharcoalMound;
import de.dakror.quarry.structure.producer.Compactor;
import de.dakror.quarry.structure.producer.Condenser;
import de.dakror.quarry.structure.producer.Crucible;
import de.dakror.quarry.structure.producer.DeviceFabricator;
import de.dakror.quarry.structure.producer.Excavator;
import de.dakror.quarry.structure.producer.FillingMachine;
import de.dakror.quarry.structure.producer.Furnace;
import de.dakror.quarry.structure.producer.GroundwaterPump;
import de.dakror.quarry.structure.producer.InductionFurnace;
import de.dakror.quarry.structure.producer.IngotMold;
import de.dakror.quarry.structure.producer.InjectionMolder;
import de.dakror.quarry.structure.producer.Kiln;
import de.dakror.quarry.structure.producer.Lumberjack;
import de.dakror.quarry.structure.producer.Mason;
import de.dakror.quarry.structure.producer.Mine;
import de.dakror.quarry.structure.producer.Mixer;
import de.dakror.quarry.structure.producer.OilWell;
import de.dakror.quarry.structure.producer.Polarizer;
import de.dakror.quarry.structure.producer.Polymerizer;
import de.dakror.quarry.structure.producer.RockCrusher;
import de.dakror.quarry.structure.producer.RollingMachine;
import de.dakror.quarry.structure.producer.SawMill;
import de.dakror.quarry.structure.producer.Stacker;
import de.dakror.quarry.structure.producer.TubeBender;
import de.dakror.quarry.structure.producer.WireDrawer;
import de.dakror.quarry.structure.storage.Barrel;
import de.dakror.quarry.structure.storage.DigitalStorage;
import de.dakror.quarry.structure.storage.Silo;
import de.dakror.quarry.structure.storage.Storage;
import de.dakror.quarry.structure.storage.Tank;
import de.dakror.quarry.structure.storage.Warehouse;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public final class StructureType {
   private static final List<StructureType> REGISTRY = new ArrayList<>();
   private static volatile boolean frozen = false;
   public final short id;
   public final String name;
   public final Constructor<?> constr;
   public final boolean versionSwitch;
   public static final StructureType Conveyor = register(1, "Conveyor", Conveyor.class);
   public static final StructureType ConveyorBridge = register(2, "ConveyorBridge", ConveyorBridge.class);
   public static final StructureType BrickChannel = register(3, "BrickChannel", BrickChannel.class);
   public static final StructureType CopperTube = register(4, "CopperTube", CopperTube.class);
   public static final StructureType SteelTube = register(5, "SteelTube", SteelTube.class);
   public static final StructureType ElectricConveyor = register(6, "ElectricConveyor", ElectricConveyor.class);
   public static final StructureType ElectricConveyorCore = register(7, "ElectricConveyorCore", ElectricConveyorCore.class);
   public static final StructureType Filter = register(10, "Filter", Filter.class);
   public static final StructureType Distributor = register(11, "Distributor", Distributor.class);
   public static final StructureType Valve = register(12, "Valve", Valve.class);
   public static final StructureType Hopper = register(13, "Hopper", Hopper.class);
   public static final StructureType VacuumPump = register(14, "VacuumPump", VacuumPump.class);
   public static final StructureType Storage = register(20, "Storage", Storage.class);
   public static final StructureType Warehouse = register(21, "Warehouse", Warehouse.class);
   public static final StructureType Tank = register(22, "Tank", Tank.class);
   public static final StructureType Barrel = register(23, "Barrel", Barrel.class);
   public static final StructureType Silo = register(24, "Silo", Silo.class);
   public static final StructureType DigitalStorage = register(25, "DigitalStorage", DigitalStorage.class);
   public static final StructureType Mine = register(40, "Mine", Mine.class);
   public static final StructureType Lumberjack = register(41, "Lumberjack", Lumberjack.class);
   public static final StructureType GroundwaterPump = register(42, "GroundwaterPump", GroundwaterPump.class);
   public static final StructureType ShaftDrill = register(43, "ShaftDrill", ShaftDrill.class);
   public static final StructureType AirPurifier = register(44, "AirPurifier", AirPurifier.class);
   public static final StructureType OilWell = register(45, "OilWell", OilWell.class);
   public static final StructureType Excavator = register(46, "Excavator", Excavator.class);
   public static final StructureType Carpenter = register(70, "Carpenter", Carpenter.class);
   public static final StructureType Furnace = register(71, "Furnace", Furnace.class);
   public static final StructureType RockCrusher = register(72, "RockCrusher", RockCrusher.class);
   public static final StructureType BallMill = register(73, "BallMill", BallMill.class);
   public static final StructureType IngotMold = register(74, "IngotMold", IngotMold.class);
   public static final StructureType CharcoalMound = register(75, "CharcoalMound", CharcoalMound.class);
   public static final StructureType Mason = register(76, "Mason", Mason.class);
   public static final StructureType Boiler = register(77, "Boiler", Boiler.class);
   public static final StructureType WireDrawer = register(78, "WireDrawer", WireDrawer.class);
   public static final StructureType RollingMachine = register(79, "RollingMachine", RollingMachine.class);
   public static final StructureType BlastFurnace = register(80, "BlastFurnace", BlastFurnace.class);
   public static final StructureType Condenser = register(81, "Condenser", Condenser.class);
   public static final StructureType Mixer = register(82, "Mixer", Mixer.class);
   public static final StructureType Kiln = register(83, "Kiln", Kiln.class);
   public static final StructureType SawMill = register(84, "SawMill", SawMill.class);
   public static final StructureType TubeBender = register(85, "TubeBender", TubeBender.class);
   public static final StructureType Polarizer = register(86, "Polarizer", Polarizer.class);
   public static final StructureType Compactor = register(87, "Compactor", Compactor.class);
   public static final StructureType Assembler = register(88, "Assembler", Assembler.class);
   public static final StructureType Refinery = register(89, "Refinery", Refinery.class);
   public static final StructureType DistillationColumn = register(90, "DistillationColumn", DistillationColumn.class);
   public static final StructureType Polymerizer = register(91, "Polymerizer", Polymerizer.class);
   public static final StructureType Centrifuge = register(92, "Centrifuge", Centrifuge.class);
   public static final StructureType InjectionMolder = register(93, "InjectionMolder", InjectionMolder.class);
   public static final StructureType Crucible = register(94, "Crucible", Crucible.class);
   public static final StructureType DeviceFabricator = register(95, "DeviceFabricator", DeviceFabricator.class);
   public static final StructureType InductionFurnace = register(96, "InductionFurnace", InductionFurnace.class);
   public static final StructureType FillingMachine = register(97, "FillingMachine", FillingMachine.class);
   public static final StructureType BarrelDrainer = register(98, "BarrelDrainer", BarrelDrainer.class);
   public static final StructureType ArcWelder = register(99, "ArcWelder", ArcWelder.class);
   public static final StructureType Substation = register(100, "Substation", Substation.class);
   public static final StructureType CopperCable = register(101, "CopperCable", CopperCable.class);
   public static final StructureType Capacitor = register(102, "Capacitor", Capacitor.class);
   public static final StructureType PowerPole = register(103, "PowerPole", PowerPole.class);
   public static final StructureType PowerPoleGhost = register(104, "PowerPoleGhost", PowerPoleGhost.class);
   public static final StructureType AnchorPortal = register(105, "AnchorPortal", AnchorPortal.class);
   public static final StructureType SuperCapacitor = register(106, "SuperCapacitor", SuperCapacitor.class);
   public static final StructureType CopperCableCrossing = register(107, "CopperCableCrossing", CopperCableCrossing.class);
   public static final StructureType WaterWheel = register(110, "WaterWheel", WaterWheel.class);
   public static final StructureType SteamTurbine = register(111, "SteamTurbine", SteamTurbine.class);
   public static final StructureType SolarPanelOutlet = register(112, "SolarPanelOutlet", SolarPanelOutlet.class);
   public static final StructureType SolarPanel = register(113, "SolarPanel", SolarPanel.class);
   public static final StructureType GasTurbine = register(114, "GasTurbine", GasTurbine.class);
   public static final StructureType ScienceLab = register(150, "ScienceLab", ScienceLab.class);
   public static final StructureType Booster = register(175, "Booster", Booster.class);
   public static final StructureType ShaftDrillHead = register(200, "ShaftDrillHead", ShaftDrillHead.class);
   public static final StructureType ItemLift = register(201, "ItemLift", ItemLift.class);
   public static final StructureType ItemLiftBelow = register(202, "ItemLiftBelow", ItemLiftBelow.class);
   public static final StructureType TubeShaft = register(203, "TubeShaft", TubeShaft.class);
   public static final StructureType TubeShaftBelow = register(204, "TubeShaftBelow", TubeShaftBelow.class);
   public static final StructureType CableShaft = register(205, "CableShaft", CableShaft.class);
   public static final StructureType CableShaftBelow = register(206, "CableShaftBelow", CableShaftBelow.class);
   public static final StructureType HighPowerShaft = register(207, "HighPowerShaft", HighPowerShaft.class);
   public static final StructureType HighPowerShaftBelow = register(208, "HighPowerShaftBelow", HighPowerShaftBelow.class);
   public static final StructureType Stacker = register(210, "Stacker", Stacker.class);
   public static final StructureType ItemDestroyer = register(211, "ItemDestroyer", ItemDestroyer.class);

   private StructureType(int id, String name, Class<?> clazz) {
      this.id = (short)id;
      this.name = name;
      Constructor<?> con = null;
      boolean switch_ = false;

      try {
         con = clazz.getConstructor(int.class, int.class, int.class);
         clazz.getConstructor(int.class, int.class);
         switch_ = true;
      } catch (Exception var9) {
         try {
            con = clazz.getConstructor(int.class, int.class);
            switch_ = false;
         } catch (Exception var8) {
            throw new IllegalStateException("Structure type " + name + " does not contain appropriate constructor (int, int) or (int, int, int)");
         }
      }

      this.constr = con;
      this.versionSwitch = switch_;
      if (Structure.types[id] != null) {
         throw new IllegalStateException("Structure with ID " + id + " is already taken!");
      } else {
         Structure.types[id] = this;
      }
   }

   public static synchronized StructureType register(int id, String name, Class<?> clazz) {
      if (frozen) {
         throw new IllegalStateException("Registry frozen after initialization");
      } else {
         for (StructureType t : REGISTRY) {
            if (t.id == id) {
               throw new IllegalStateException("Structure ID " + id + " already registered");
            }

            if (t.name.equals(name)) {
               throw new IllegalStateException("Structure name " + name + " already registered");
            }
         }

         StructureType type = new StructureType(id, name, clazz);
         REGISTRY.add(type);
         return type;
      }
   }

   public static void freezeRegistry() {
      frozen = true;
   }

   public static StructureType[] values() {
      return REGISTRY.toArray(new StructureType[0]);
   }

   public static StructureType valueOf(String name) {
      for (StructureType t : REGISTRY) {
         if (t.name.equals(name)) {
            return t;
         }
      }

      return null;
   }

   public String name() {
      return this.name;
   }

   public int ordinal() {
      return REGISTRY.indexOf(this);
   }

   @Override
   public String toString() {
      return this.name;
   }

   static {
      freezeRegistry();
   }
}
