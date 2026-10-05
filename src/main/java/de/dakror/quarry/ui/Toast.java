package de.dakror.quarry.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;

@LmlTag(
   tagName = "toast"
)
public class Toast extends Window implements CustomTag {
   public Toast(Skin skin, String style) {
      super("", skin, style);
      this.getTitleTable().padTop(80.0F);
      this.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Toast.this.setVisible(false);
         }
      });
   }

   @Override
   public void postInit() {
   }

   public void show(String text) {
      if (this.isVisible()) {
         this.clearActions();
      }

      this.toFront();
      this.getTitleLabel().setText(text);
      this.setSize(500.0F, 80.0F);
      this.setPosition((this.getStage().getWidth() - this.getWidth()) / 2.0F, 140.0F);
      this.addAction(
         Actions.sequence(Actions.fadeIn(0.25F, Interpolation.fade), Actions.delay(1.5F), Actions.fadeOut(0.5F, Interpolation.fade), Actions.visible(false))
      );
      this.setColor(0.1F, 0.1F, 0.1F, 0.0F);
      this.setVisible(true);
   }
}
