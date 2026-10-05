package de.dakror.quarry.structure.base.component;

import de.dakror.quarry.game.Item;

public interface IStorage {
   int getCount();

   int getSize();

   void setSize(int var1);

   boolean isEmpty();

   float getFillRatio();

   boolean hasSpace();

   int get(Item.ItemType var1);

   int getSum(Item.ItemCategory var1);

   Item.Items.Amount[] getSimilar(Item.ItemType var1);

   Item.Items.Amount[] get(Item.ItemCategory var1);

   boolean add(Item.ItemType var1, int var2);

   int addWithRest(Item.ItemType var1, int var2);

   void addUnsafe(Item.ItemType var1, int var2);

   boolean remove(Item.ItemType var1, int var2);

   boolean remove(Item.ItemCategory var1, int var2);

   int removeWithRest(Item.ItemType var1, int var2);

   boolean canAccept(Item.ItemType var1);

   int getOutput();

   void setOutput(int var1);
}
