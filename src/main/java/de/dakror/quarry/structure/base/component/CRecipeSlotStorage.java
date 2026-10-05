package de.dakror.quarry.structure.base.component;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.RecipeList;

public class CRecipeSlotStorage extends CInventory {
   int index;
   RecipeList recipes;

   public CRecipeSlotStorage(RecipeList recipes, int index) {
      super(Integer.MAX_VALUE);
      this.recipes = recipes;
      this.index = index;
   }

   @Override
   public boolean canAccept(Item.ItemType item) {
      for (RecipeList.Recipe r : this.recipes.recipes) {
         if (r.getInput() != null && r.getInput().entries.length > this.index) {
            Item.Items.Amount a = r.getInput().entries[this.index];
            if (a.getCat() != null && item.categories.contains(a.getCat()) && this.getSum(a.getCat()) < a.getAmount()
               || a.getItem() != null && (a.getItem() == item || a.getItem() == Item.base(item)) && this.get(item) < a.getAmount()) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean add(Item.ItemType item, int amount) {
      for (RecipeList.Recipe r : this.recipes.recipes) {
         if (r.getInput() != null && r.getInput().entries.length > this.index) {
            Item.Items.Amount a = r.getInput().entries[this.index];
            if (a.getCat() != null && item.categories.contains(a.getCat()) && this.getSum(a.getCat()) + amount <= a.getAmount()
               || a.getItem() != null && (a.getItem() == item || a.getItem() == Item.base(item) && this.get(item) + amount <= a.getAmount())) {
               this.addUnsafe(item, amount);
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public int addWithRest(Item.ItemType item, int amount) {
      throw new RuntimeException("addWithRest not supported");
   }
}
