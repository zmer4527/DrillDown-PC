package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.scenes.Game;

@LmlTag(
   tagName = "volumebutton"
)
public class VolumeButton extends ImageButton implements CustomTag {
   int state;
   String style;

   public VolumeButton(Skin skin, String style) {
      super(skin, style);
      this.style = style;
   }

   private void setImage() {
      this.getStyle().imageUp = this.getSkin().getDrawable("symb_" + this.style + "_" + this.state);
   }

   public void initListener() {
      this.state = (int)(Quarry.Q.prefs.getFloat(this.style, 1.0F) * 3.0F);
      this.setImage();
      this.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            VolumeButton.this.state = (VolumeButton.this.state + 1) % 4;
            if (VolumeButton.this.style.equals("sound")) {
               if (VolumeButton.this.state > 0) {
                  Quarry.Q.sound.setSoundVolume((float)VolumeButton.this.state / 3.0F);
               }

               Quarry.Q.sound.setPlaySound(VolumeButton.this.state > 0);
               if (Quarry.Q.getScene() instanceof Game) {
                  if (VolumeButton.this.state == 0) {
                     Game.G.stopSfx();
                  }

                  Game.G.spatializedPlayer.setVolume((float)VolumeButton.this.state / 3.0F);
               }
            } else {
               if (VolumeButton.this.state > 0) {
                  Quarry.Q.sound.setMusicVolume((float)VolumeButton.this.state / 3.0F);
               }

               Quarry.Q.sound.setPlayMusic(VolumeButton.this.state > 0);
            }

            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Quarry.Q.prefs.putFloat(VolumeButton.this.style, (float)VolumeButton.this.state / 3.0F).flush();
            VolumeButton.this.setImage();
         }
      });
   }

   @Override
   public void postInit() {
   }
}
