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
   tagName = "seedprompt"
)
public class SeedPrompt extends Window implements CustomTag {
   Label text;
   TextField input;
   Response<String, Boolean> callback;
   Ui ui;

   public SeedPrompt(Skin skin, String style) {
      super("", skin, style);
      this.setName("seedprompt");
   }

   @Override
   public void postInit() {
      this.text = Util.id("label");
      this.getTitleTable().padTop(50.0F);
      this.input = Util.id("input");
      this.input.setOnlyFontChars(true);
      Util.<Actor>id("paste").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Object paste = Quarry.Q.pi.message(17, null);
            if (paste != null) {
               SeedPrompt.this.input.setText(paste.toString());
            }
         }
      });
      Util.<Actor>id("ok").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (SeedPrompt.this.input.getText().trim().length() != 0) {
               boolean hide = true;
               if (SeedPrompt.this.callback != null) {
                  hide = SeedPrompt.this.callback.call(SeedPrompt.this.input.getText());
               }

               if (hide) {
                  SeedPrompt.this.input.getOnscreenKeyboard().show(false);
                  SeedPrompt.this.callback = null;
                  SeedPrompt.this.ui.hide(SeedPrompt.this);
               }
            }
         }
      });
      Util.<Actor>id("cancel").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            boolean hide = true;
            if (SeedPrompt.this.callback != null) {
               hide = SeedPrompt.this.callback.call(null);
            }

            if (hide) {
               SeedPrompt.this.callback = null;
               SeedPrompt.this.input.getOnscreenKeyboard().show(false);
               SeedPrompt.this.ui.hide(SeedPrompt.this);
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
