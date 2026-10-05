package net.spookygames.gdx.sfx;

import com.badlogic.gdx.math.MathUtils;

public class FadeIn extends SfxMusicEffect {
   private float baseVolume;

   @Override
   protected void begin() {
      super.begin();
      this.baseVolume = this.music.getVolume();
   }

   @Override
   protected boolean apply(float position) {
      float ratio = position / this.getDuration();
      if (ratio <= 1.0F) {
         this.music.setVolume(MathUtils.clamp(this.getInterpolation().apply(0.0F, this.baseVolume, ratio), 0.0F, 1.0F));
         return false;
      } else {
         return true;
      }
   }

   @Override
   protected void end() {
      super.end();
      this.music.setVolume(this.baseVolume);
   }

   @Override
   public void reset() {
      super.reset();
      this.baseVolume = 0.0F;
   }
}
