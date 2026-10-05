package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.GameUi;

public class ResourceAmountLabel extends IconLabel {
   private TextTooltip tooltip;
   private Item.ItemType item;
   private Item.ItemCategory cat;
   private float amount;

   public ResourceAmountLabel(int iconSize, Skin skin, String icon, float amount) {
      this(iconSize, skin, skin.getDrawable(icon), amount);
   }

   public ResourceAmountLabel(int iconSize, Skin skin, Drawable icon, float amount, Object... userObject) {
      this(iconSize, skin, icon, amount, "default-font", userObject);
   }

   public ResourceAmountLabel(int iconSize, Skin skin, Drawable icon, float amount, String font, Object... userObject) {
      super(iconSize, skin, icon, Float.toString(amount), font, userObject);
      this.setAmount(amount);
   }

   public ResourceAmountLabel(int iconSize, Skin skin, Item.ItemType item, float amount, Object... userObject) {
      this(iconSize, skin, item.drawable, amount, item, userObject);
      this.setTouchable(Touchable.enabled);
      this.item = item;
      this.tooltip = new TextTooltip("", skin);
      this.addListener(this.tooltip);
   }

   public ResourceAmountLabel(int iconSize, Skin skin, Item.ItemCategory cat, float amount, Object... userObject) {
      this(iconSize, skin, cat.drawable, amount, userObject);
      this.setTouchable(Touchable.enabled);
      this.cat = cat;
      this.tooltip = new TextTooltip("", skin);
      this.addListener(this.tooltip);
   }

   public Item.ItemType getItem() {
      return this.item;
   }

   public Item.ItemCategory getCat() {
      return this.cat;
   }

   public float getAmount() {
      return this.amount;
   }

   public void setAmount(float amount) {
      this.amount = amount;
      String text = null;
      if ((this.item == null || !this.item.categories.contains(Item.ItemCategory.Fluid)) && this.cat != Item.ItemCategory.Fluid) {
         text = GameUi.formatResourceAmount((int)amount);
      } else {
         text = GameUi.formatResourceAmount(amount / 1000.0F) + "L";
      }

      this.setText(text);
      String name = "";
      if (this.item != null) {
         name = " " + this.item.title;
      } else if (this.cat != null) {
         name = " " + this.cat.title;
      }

      if (this.tooltip != null) {
         this.tooltip.getActor().setText(text + name);
      }
   }
}
