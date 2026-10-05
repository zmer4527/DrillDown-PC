package de.dakror.quarry.structure.producer;

import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.game.Tile;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.util.Sfx;
import java.util.EnumMap;
import java.util.Map.Entry;

public class Excavator extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0,
         StructureType.Excavator,
         4,
         4,
         "excavator",
         new Item.Items(
            Item.ItemType.StoneBrick,
            300,
            Item.ItemType.MachineFrame,
            20,
            Item.ItemType.AdvancedMachineFrame,
            2,
            Item.ItemType.SteelCable,
            50,
            Item.ItemType.Rotor,
            10
         ),
         new RecipeList() {
            @Override
            protected void init() {
               this.add(
                  new RecipeList.Recipe(3.0F, "ore", 3334)
                     .input(new Item.Items.Amount(Item.ItemType.Dynamite, 1))
                     .output(new Item.Items.Amount(Item.ItemType._Dust, 30), new Item.Items.Amount(Item.ItemType._Dust, 30))
               );
            }
         },
         new Sfx("excavator" + Const.SFX_FORMAT),
         true,
         new Dock(0, 2, Direction.West, Dock.DockType.ItemIn, new Dock.DockFilter(Item.ItemType.Dynamite)),
         new Dock(0, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(3, 0, Direction.South, Dock.DockType.ItemOut),
         new Dock(3, 2, Direction.East, Dock.DockType.Power)
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.HighTech})
      .flags(Schema.Flags.MirroredTextureHorizontal);
   public static final int MIN_ORE_TILES = 7;
   public static final float BASE_TIMEOUT = 3.0F;
   public Item.ItemType mineableItem;
   int successiveProductions;

   public Excavator(int x, int y) {
      super(x, y, classSchema);
      ((CInventory)this.outputInventories[0]).setPumpOutSpeed(0.0F);
      ((CInventory)this.outputInventories[1]).setPumpOutSpeed(0.0F);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading) {
         this.updateMineableItem();
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateMineableItem();
   }

   protected void updateMineableItem() {
      Layer l = this.layer;
      if (l == null) {
         l = Game.G.layer;
      }

      if (l != null) {
         this.mineableItem = null;
         EnumMap<Tile.TileType, Integer> counts = new EnumMap<>(Tile.TileType.class);

         for (int i = 0; i < this.getWidth(); i++) {
            for (int j = 0; j < this.getHeight(); j++) {
               Tile.TileType tile = l.get(this.x + i, this.y + j);
               if (tile == Tile.TileType.CopperOre || tile == Tile.TileType.IronOre || tile == Tile.TileType.TinOre || tile == Tile.TileType.CoalOre) {
                  counts.put(tile, (counts.containsKey(tile) ? counts.get(tile) : 0) + 1);
               }
            }
         }

         for (Entry<Tile.TileType, Integer> e : counts.entrySet()) {
            if (e.getValue() >= 7) {
               this.mineableItem = Item.get(Item.ItemType._Dust, e.getKey().itemDrop);
               break;
            }
         }
      }
   }

   @Override
   protected void doProductionStep() {
      if (this.mineableItem != null) {
         this.outputInventories[0].addUnsafe(this.mineableItem, this.activeRecipe.getOutput().entries[0].getAmount());
         this.outputInventories[1].addUnsafe(this.mineableItem, this.activeRecipe.getOutput().entries[1].getAmount());
         this.successiveProductions++;
      }
   }
}
