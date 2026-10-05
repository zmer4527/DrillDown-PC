package com.badlogic.gdx.scenes.scene2d.ui.modified;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TextTooltip extends Tooltip<Label> {
   public TextTooltip(String text, Skin skin) {
      this(text, TooltipManager.getInstance(), (TextTooltip.TextTooltipStyle)skin.get(TextTooltip.TextTooltipStyle.class));
   }

   public TextTooltip(String text, Skin skin, String styleName) {
      this(text, TooltipManager.getInstance(), (TextTooltip.TextTooltipStyle)skin.get(styleName, TextTooltip.TextTooltipStyle.class));
   }

   public TextTooltip(String text, TextTooltip.TextTooltipStyle style) {
      this(text, TooltipManager.getInstance(), style);
   }

   public TextTooltip(String text, TooltipManager manager, Skin skin) {
      this(text, manager, (TextTooltip.TextTooltipStyle)skin.get(TextTooltip.TextTooltipStyle.class));
   }

   public TextTooltip(String text, TooltipManager manager, Skin skin, String styleName) {
      this(text, manager, (TextTooltip.TextTooltipStyle)skin.get(styleName, TextTooltip.TextTooltipStyle.class));
   }

   public TextTooltip(String text, TooltipManager manager, TextTooltip.TextTooltipStyle style) {
      super(null, manager);
      Label label = new Label(text, style.label);
      this.container.center().height(48.0F);
      this.container.setActor(label);
      label.setAlignment(1);
      this.container.width(new Value() {
         public float get(Actor context) {
            return ((Label)context).getPrefWidth() + 40.0F;
         }
      });
      this.setStyle(style);
   }

   public void setStyle(TextTooltip.TextTooltipStyle style) {
      if (style == null) {
         throw new NullPointerException("style cannot be null");
      } else if (!(style instanceof TextTooltip.TextTooltipStyle)) {
         throw new IllegalArgumentException("style must be a TextTooltipStyle.");
      } else {
         ((Label)this.container.getActor()).setStyle(style.label);
         this.container.setBackground(style.background);
         this.container.maxWidth(style.wrapWidth);
      }
   }

   public static class TextTooltipStyle {
      public LabelStyle label;
      public Drawable background;
      public float wrapWidth;

      public TextTooltipStyle() {
      }

      public TextTooltipStyle(LabelStyle label, Drawable background) {
         this.label = label;
         this.background = background;
      }

      public TextTooltipStyle(TextTooltip.TextTooltipStyle style) {
         this.label = new LabelStyle(style.label);
         this.background = style.background;
      }
   }
}
