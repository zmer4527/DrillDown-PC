package de.dakror.quarry.game;

public class ConstantSupplyAmount extends Item.Items.Amount {
   public ConstantSupplyAmount(Item.ItemType item, int amount) {
      super(item, amount);
   }

   public ConstantSupplyAmount(Item.ItemCategory cat, int amount) {
      super(cat, amount);
   }
}
