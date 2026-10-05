package de.dakror.quarry.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class IconLabel extends Table {
   private Image icon;
   private Label label;
   private TextTooltip textTooltip;

   public IconLabel(int iconSize, Skin skin, String icon, String text) {
      this(iconSize, skin, skin.getDrawable(icon), text);
   }

   public IconLabel(int iconSize, Skin skin, Drawable icon, String text, Object... userObject) {
      this(iconSize, skin, icon, text, "default-font", userObject);
   }

   public IconLabel(int iconSize, Skin skin, Drawable icon, String text, String font, Object... userObject) {
      if (userObject.length > 0) {
         this.setUserObject(userObject[0]);
      }

      this.icon = new Image(icon);
      this.add(this.icon).size((float)iconSize);
      this.label = new Label(text, skin, font, Color.WHITE);
      this.add(this.label).height((float)(iconSize + 4)).padLeft(5.0F).right();
   }

   public IconLabel tooltip(Skin skin) {
      return this.tooltip(skin, "");
   }

   public IconLabel tooltip(Skin skin, String text) {
      this.textTooltip = new TextTooltip(text, skin);
      this.setTouchable(Touchable.enabled);
      this.addListener(this.textTooltip);
      return this;
   }

   public void setText(String text) {
      this.label.setText(text);
   }

   public void setTooltipText(String text) {
      this.textTooltip.getActor().setText(text);
   }

   public void setIcon(Drawable icon) {
      this.icon.setDrawable(icon);
   }
}
