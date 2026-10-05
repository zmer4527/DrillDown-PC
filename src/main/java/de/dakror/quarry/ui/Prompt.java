package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.Response;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.util.Util;

@LmlTag(
   tagName = "prompt"
)
public class Prompt extends Window implements CustomTag {
   Label text;
   TextField input;
   Response<String, Boolean> callback;
   Ui ui;

   public Prompt(Skin skin, String style) {
      super("", skin, style);
      this.setName("prompt");
   }

   @Override
   public void postInit() {
      this.text = Util.id("label");
      this.getTitleTable().padTop(50.0F);
      this.input = Util.id("input");
      this.input.setOnlyFontChars(true);
      Util.<Actor>id("ok").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (Prompt.this.input.getText().trim().length() != 0) {
               boolean hide = true;
               if (Prompt.this.callback != null) {
                  hide = Prompt.this.callback.call(Prompt.this.input.getText());
               }

               if (hide) {
                  Prompt.this.input.getOnscreenKeyboard().show(false);
                  Prompt.this.callback = null;
                  Prompt.this.ui.hide(Prompt.this);
               }
            }
         }
      });
      Util.<Actor>id("cancel").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            boolean hide = true;
            if (Prompt.this.callback != null) {
               hide = Prompt.this.callback.call(null);
            }

            if (hide) {
               Prompt.this.callback = null;
               Prompt.this.input.getOnscreenKeyboard().show(false);
               Prompt.this.ui.hide(Prompt.this);
            }
         }
      });
   }

   public void show(Ui ui, String title, String value, Response<String, Boolean> callback) {
      if (this.getStage() == null) {
         this.ui = ui;
         this.callback = callback;
         this.text.setText(title);
         this.input.setText(value);
         this.pack();
         ui.getStage().setKeyboardFocus(this.input);
         this.input.getOnscreenKeyboard().show(true);
         this.setPosition((Const.UI_W - this.getWidth()) / 2.0F, (Const.UI_H - this.getHeight()) / 3.0F * 2.0F);
         ui.show(this);
      }
   }
}
