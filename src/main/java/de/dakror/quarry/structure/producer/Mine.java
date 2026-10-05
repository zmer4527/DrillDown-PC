package de.dakror.quarry.structure.producer;

import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Tile;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Sfx;
import java.util.TreeSet;

public class Mine extends ProducerStructure {
   public static final ProducerStructure.ProducerSchema classSchema = new ProducerStructure.ProducerSchema(
         0, StructureType.Mine, 2, 2, "mine", new Item.Items(Item.ItemType.Stone, 10, Item.ItemType.Scaffolding, 2), new RecipeList() {
            @Override
            protected void init() {
               this.add(new RecipeList.Recipe(16.0F, "stone").output(new Item.Items.Amount(Item.ItemType.Stone, 4)));
               this.add(new RecipeList.Recipe(16.0F, "dirt").output(new Item.Items.Amount(Item.ItemType.Dirt, 4)));
               this.add(new RecipeList.Recipe(20.0F, "clay").output(new Item.Items.Amount(Item.ItemType.Clay, 4)));
               this.add(new RecipeList.Recipe(48.0F, "ore").output(new Item.Items.Amount(Item.ItemType._Ore, 4)));
            }
         }, new Sfx("mine" + Const.SFX_FORMAT), true, new Dock(0, 1, Direction.North, Dock.DockType.ItemOut)
      )
      .flags(new Schema.Flags[]{Schema.Flags.TextureAlwaysUpright});
   protected Item.ItemType activeItem;
   public TreeSet<Item.ItemType> mineableItems = new TreeSet<>();

   public Mine(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading) {
         this.updateMineableItems();
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateMineableItems();
   }

   protected void updateMineableItems() {
      Layer l = this.layer;
      if (l == null) {
         l = Game.G.layer;
      }

      if (l != null) {
         this.mineableItems.clear();

         for (int i = 0; i < this.getWidth(); i++) {
            for (int j = 0; j < this.getHeight(); j++) {
               Tile.TileType t = l.get(this.x + i, this.y + j);
               if (t != null && t.itemDrop != null) {
                  boolean anyRecipe = false;

                  for (RecipeList.Recipe r : this.getSchema().recipeList.recipes) {
                     if (r.getOutput().entries[0].getItem() == t.itemDrop || r.getOutput().entries[0].getItem() == Item.base(t.itemDrop)) {
                        anyRecipe = true;
                        break;
                     }
                  }

                  if (anyRecipe) {
                     this.mineableItems.add(t.itemDrop);
                  }
               }
            }
         }
      }
   }

   public int getMineableItems() {
      this.updateMineableItems();
      return this.mineableItems.size();
   }

   @Override
   protected void pickRandomActiveRecipe() {
      int x = (int)(Math.random() * 2.0);
      int y = (int)(Math.random() * 2.0);
      Tile.TileType t = this.layer.get(this.x + x, this.y + y);
      boolean anyRecipe = false;
      int i = 0;

      for (RecipeList.Recipe r : this.getSchema().recipeList.recipes) {
         if (r.getOutput().entries[0].getItem() == t.itemDrop || r.getOutput().entries[0].getItem() == Item.base(t.itemDrop)) {
            this.activeRecipeIndex = i;
            this.activeRecipe = this.getSchema().recipeList.recipes[i];
            anyRecipe = true;
            break;
         }

         i++;
      }

      if (anyRecipe) {
         this.activeItem = t.itemDrop;
         this.workDelay = this.activeRecipe.workingTime;
         this.updateUI();
      } else {
         for (int k = 0; k < this.getWidth() && !anyRecipe; k++) {
            for (int j = 0; j < this.getHeight() && !anyRecipe; j++) {
               t = this.layer.get(this.x + k, this.y + j);
               anyRecipe = false;
               i = 0;

               for (RecipeList.Recipe r : this.getSchema().recipeList.recipes) {
                  if (r.getOutput().entries[0].getItem() == t.itemDrop || r.getOutput().entries[0].getItem() == Item.base(t.itemDrop)) {
                     this.activeRecipeIndex = i;
                     this.activeRecipe = this.getSchema().recipeList.recipes[i];
                     anyRecipe = true;
                     break;
                  }

                  i++;
               }

               if (anyRecipe) {
                  this.activeItem = t.itemDrop;
                  this.workDelay = this.activeRecipe.workingTime;
                  this.updateUI();
                  break;
               }
            }
         }
      }
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      super.draw(spriter);
      int i = 0;

      for (Item.ItemType t : this.mineableItems) {
         spriter.add(t.icon, (float)(this.x * 64 + 12 + i * 25), (float)(this.y * 64 + 12), -1.0F, 16.0F, 16.0F);
         i++;
      }
   }

   @Override
   protected void doProductionStep() {
      if (this.activeItem != null) {
         this.outputInventories[0].add(this.activeItem, this.activeRecipe.getOutput().entries[0].getAmount());
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      if (this.activeItem != null) {
         b.Short("activeItem", this.activeItem.value);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      short item = tag.Short("activeItem", (short)0);
      this.activeItem = item == 0 ? null : Item.get(item);
   }
}
