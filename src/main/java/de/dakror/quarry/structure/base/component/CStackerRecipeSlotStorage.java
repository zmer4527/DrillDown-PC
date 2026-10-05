package de.dakror.quarry.structure.base.component;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.RecipeList;

public class CStackerRecipeSlotStorage extends CRecipeSlotStorage {
   public CStackerRecipeSlotStorage(RecipeList recipes, int index) {
      super(recipes, index);
   }

   @Override
   public boolean canAccept(Item.ItemType item) {
      if (Item.base(item) == Item.ItemType.Pallet) {
         return false;
      } else {
         for (RecipeList.Recipe r : this.recipes.recipes) {
            if (r.getInput() != null && r.getInput().entries.length > this.index) {
               Item.Items.Amount a = r.getInput().entries[this.index];
               int num = this.get(item);
               return this.count == 0 || num > 0 && num < a.getAmount();
            }
         }

         return false;
      }
   }

   @Override
   public boolean add(Item.ItemType item, int amount) {
      if (Item.base(item) == Item.ItemType.Pallet) {
         return false;
      } else {
         for (RecipeList.Recipe r : this.recipes.recipes) {
            if (r.getInput() != null && r.getInput().entries.length > this.index) {
               Item.Items.Amount a = r.getInput().entries[this.index];
               int num = this.get(item);
               if (this.count == 0 || num > 0 && num + amount <= a.getAmount()) {
                  this.addUnsafe(item, amount);
                  return true;
               }
            }
         }

         return false;
      }
   }
}
