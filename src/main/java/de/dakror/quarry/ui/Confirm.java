package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.Callback;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.util.Util;

@LmlTag(
   tagName = "confirm"
)
public class Confirm extends Window implements CustomTag {
   Label text;
   Callback<Boolean> callback;
   Ui ui;

   public Confirm(Skin skin, String style) {
      super("", skin, style);
      this.setName("confirm");
   }

   @Override
   public void postInit() {
      this.text = Util.id("label");
      Util.<Actor>id("yes").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (Confirm.this.callback != null) {
               Confirm.this.callback.call(true);
            }

            Confirm.this.callback = null;
            Confirm.this.ui.hide(Confirm.this);
         }
      });
      Util.<Actor>id("no").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (Confirm.this.callback != null) {
               Confirm.this.callback.call(false);
            }

            Confirm.this.callback = null;
            Confirm.this.ui.hide(Confirm.this);
         }
      });
      Util.<Actor>id("cancel").addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (Confirm.this.callback != null) {
               Confirm.this.callback.call(null);
            }

            Confirm.this.callback = null;
            Confirm.this.ui.hide(Confirm.this);
         }
      });
   }

   public void show(Ui ui, String title, Callback<Boolean> callback) {
      if (this.getStage() == null) {
         this.ui = ui;
         this.callback = callback;
         this.text.setText(title);
         this.pack();
         this.setPosition((Const.UI_W - this.getWidth()) / 2.0F, (Const.UI_H - this.getHeight()) / 3.0F * 2.0F);
         ui.show(this);
      }
   }
}
