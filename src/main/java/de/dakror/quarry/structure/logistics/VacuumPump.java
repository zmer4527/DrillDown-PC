package de.dakror.quarry.structure.logistics;

import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.util.Sfx;

public class VacuumPump extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.VacuumPump,
         2,
         1,
         "vacuumpump",
         new Item.Items(Item.ItemType.SteelPlate, 20, Item.ItemType.Dynamo, 1, Item.ItemType.SteelTube, 10),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(0.1F, "pump", 10)
                     .input(new Item.Items.Amount(Item.ItemCategory.Fluid, 1000))
                     .output(new RecipeList.SameAmount(0, 1000))
               );
            }
         },
         new Sfx("centrifuge" + Const.SFX_FORMAT, 0.5F),
         false,
         new Dock(0, 0, Direction.West, Dock.DockType.FluidIn, new Dock.DockFilter(Item.ItemCategory.Fluid)),
         new Dock(1, 0, Direction.East, Dock.DockType.FluidOut),
         new Dock(0, 0, Direction.South, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.Electricity});

   public VacuumPump(int x, int y) {
      super(x, y, classSchema);
      ((CTank)this.outputInventories[0]).setPumpOutDelay(0.0F).setMaxOutput(1000);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      if (tag.has("dir")) {
         this.setUpDirection(Direction.values[tag.Byte("dir", (byte)0)]);
      }
   }
}
