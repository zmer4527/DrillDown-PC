package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.base.component.CRecipeSlotStorage;
import de.dakror.quarry.structure.base.component.CStackerRecipeSlotStorage;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.util.Sfx;

public class Stacker extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Stacker,
         3,
         3,
         "stacker",
         new Item.Items(
            Item.ItemType.MachineFrame,
            5,
            Item.ItemType.SteelIngot,
            80,
            Item.ItemType.SteelWire,
            50,
            Item.ItemType.TinPlate,
            80,
            Item.ItemType.Dynamo,
            15,
            Item.ItemType.Rotor,
            6
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(8.0F, "stack", 1000)
                     .input(new Item.Items.Amount(Item.ItemType.Pallet, 1), new Item.Items.Amount(Item.ItemCategory.Stackable, 24))
                     .output(new Item.Items.Amount(Item.ItemType._FilledPallet, 1))
               );
               this.add(
                  new RecipeList.Recipe(8.0F, "unstack", 1000)
                     .input(new Item.Items.Amount(Item.ItemCategory.FilledPallet, 1))
                     .output(new Item.Items.Amount(Item.ItemType.Pallet, 1), new Item.Items.Amount(Item.ItemType._AnyStackable, 24))
               );
            }
         },
         new Sfx("stacker" + Const.SFX_FORMAT),
         true,
         new Dock(0, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Pallet, Item.ItemCategory.FilledPallet)),
         new Dock(2, 2, Direction.North, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemCategory.Stackable)),
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(2, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(0, 1, Direction.West, Dock.DockType.Power)
      )
      .sciences(new Science.ScienceType[]{Science.ScienceType.AdvancedTransport});

   public Stacker(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   protected void initInventories() {
      int in = 0;
      int out = 0;
      int comps = this.schema.getComponents().size;

      for (int i = 0; i < this.docks.length; i++) {
         Dock d = this.docks[i];
         if (d.type == Dock.DockType.ItemIn) {
            if (in == 0) {
               this.inputInventories[in] = new CRecipeSlotStorage(this.schema.recipeList, in);
            } else {
               this.inputInventories[in] = new CStackerRecipeSlotStorage(this.schema.recipeList, in);
            }

            in++;
         } else if (d.type == Dock.DockType.ItemOut) {
            Component c = new CInventory(this.schema.outputBuffer ? this.schema.recipeList.outputSizes.get(out) : 0, i).setPumpOutSpeed(0.0F);
            c.setStructure(this);
            c.init();
            this.outputInventories[out] = (IStorage)c;
            this.components[comps++] = c;
            out++;
         }
      }
   }

   @Override
   protected void doProductionStep() {
      if (this.activeRecipe.getOutput().entries.length == 1) {
         this.outputInventories[0].addUnsafe(Item.palletSourceItems[this.activeItems.entries[1].getItem().value & '\uffff'], 1);
      } else {
         this.outputInventories[0].addUnsafe(Item.ItemType.Pallet, 1);
         this.outputInventories[1]
            .addUnsafe(Item.palletTargetItems[this.activeItems.entries[0].getItem().value >> 8 & 0xFF], this.activeRecipe.getOutput().entries[1].getAmount());
      }
   }
}
