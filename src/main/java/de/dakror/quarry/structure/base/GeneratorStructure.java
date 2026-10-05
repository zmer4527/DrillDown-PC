package de.dakror.quarry.structure.base;

import de.dakror.quarry.game.Item;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;

public abstract class GeneratorStructure extends ProducerStructure {
   public GeneratorStructure(int x, int y, GeneratorStructure.GeneratorSchema schema) {
      super(x, y, schema);

      for (Component c : this.components) {
         if (c instanceof CTank) {
            ((CTank)c).setMaxOutput(10000).setPumpOutDelay(0.0F);
         }
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      this.offerPower(deltaTime, gameSpeed);
   }

   protected void offerPower(double deltaTime, int gameSpeed) {
      if (!this.sleeping
         && this.activeRecipe != null
         && (this.hasCapacity = this.hasCapacityForProduction())
         && !this.noPower
         && this.workDelay != -1.0F
         && !this.additionalWorkBlockingCondition()
         && gameSpeed != 0) {
         this.powerNetwork
            .offerPower(
               deltaTime, gameSpeed, (double)((GeneratorStructure.GeneratorRecipe)this.activeRecipe).getPowerGeneration() * deltaTime * (double)gameSpeed, this
            );
      }
   }

   @Override
   protected void setRecipe(int index) {
      this.setItemNotifications();
      this.activeRecipeIndex = index;
      if (index > -1) {
         this.activeRecipe = this.getSchema().recipeList.recipes[this.activeRecipeIndex];
         this.workDelay = this.activeRecipe.workingTime;
      } else {
         this.activeRecipe = null;
         this.activeItems = null;
      }

      this.updateUI();
   }

   @Override
   public double acceptPower(double amount, double networkStrength) {
      return amount;
   }

   @Override
   public double getPowerCapacity() {
      return 0.0;
   }

   @Override
   public double getPowerLevel() {
      return 0.0;
   }

   @Override
   public int getReceiverPriority() {
      return 0;
   }

   public static class GeneratorRecipe extends RecipeList.Recipe {
      private int powerGeneration;

      public GeneratorRecipe(String description, int powerGeneration) {
         super(1.0F, description);
         this.powerGeneration = powerGeneration;
      }

      public int getPowerGeneration() {
         return this.powerGeneration;
      }
   }

   public static class GeneratorSchema extends ProducerStructure.ProducerSchema {
      public GeneratorSchema(
         int version, StructureType type, int width, int height, String tex, Item.Items buildCosts, RecipeList recipes, Sfx sfx, Dock... docks
      ) {
         super(version, type, width, height, tex, buildCosts, recipes, sfx, true, docks);

         for (RecipeList.Recipe r : recipes.recipes) {
            if (!(r instanceof GeneratorStructure.GeneratorRecipe)) {
               throw new RuntimeException("Recipe is not of type GeneratorRecipe");
            }
         }
      }
   }
}
