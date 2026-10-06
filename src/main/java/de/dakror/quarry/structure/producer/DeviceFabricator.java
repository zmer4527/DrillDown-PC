package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;

public class DeviceFabricator extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.DeviceFabricator,
         24,
         9,
         "devicefabricator",
         new Item.Items(
            Item.ItemType.Glass,
            650,
            Item.ItemType.MachineFrame,
            400,
            Item.ItemType.AdvancedMachineFrame,
            100,
            Item.ItemType.Dynamo,
            250,
            Item.ItemType.BronzePlate,
            500,
            Item.ItemType.SteelCable,
            300
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(860.0F, "chip", 60000)
                     .input(
                        new Item.Items.Amount(Item.ItemType.PlasticCasing, 2),
                        new Item.Items.Amount(Item.ItemType.GoldWire, 80),
                        new Item.Items.Amount(Item.ItemType.SiliconWafer, 5),
                        new Item.Items.Amount(Item.ItemType.TinWire, 50),
                        new Item.Items.Amount(Item.ItemType.SyntheticQuartz, 5)
                     )
                     .output(new Item.Items.Amount(Item.ItemType.Chip, 1))
               );
            }
         },
         new Sfx("devicefabricator" + Const.SFX_FORMAT),
         true,
         new Dock(23, 4, Direction.East, Dock.DockType.ItemOut),
         new Dock(0, 2, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.PlasticCasing)),
         new Dock(0, 3, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.GoldWire)),
         new Dock(0, 4, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SiliconWafer)),
         new Dock(0, 5, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.TinWire)),
         new Dock(0, 6, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.SyntheticQuartz)),
         new Dock(4, 8, Direction.North, Dock.DockType.BigPower),
         new Dock(4, 0, Direction.South, Dock.DockType.BigPower)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.ComponentAssembly})
      .flags(Schema.Flags.MirroredTextureVertical);

   public DeviceFabricator(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected void doProductionStep() {
      super.doProductionStep();
      if (!Game.G.isInfinite()) {
         // doProductionStep 跑在模拟线程上，UI 必须回迁
         Game.G.postUi(() -> Game.G.ui.endOfGame.show());
      }
   }
}
