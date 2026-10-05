package net.spookygames.gdx.sfx;

import com.badlogic.gdx.math.MathUtils;

public class FadeOut extends SfxMusicEffect {
   private float baseVolume;
   private float beginning;

   @Override
   public void setMusic(SfxMusic music) {
      super.setMusic(music);
      if (music != null) {
         this.beginning = music.getDuration() - this.getDuration();
      }
   }

   @Override
   protected void begin() {
      super.begin();
      this.baseVolume = -1.0F;
   }

   @Override
   protected boolean apply(float position) {
      if (position < this.beginning) {
         return false;
      } else {
         float ratio = (position - this.beginning) / this.getDuration();
         if (ratio <= 1.0F) {
            float target = this.baseVolume;
            if (target < 0.0F) {
               this.baseVolume = target = this.music.getVolume();
            }

            this.music.setVolume(MathUtils.clamp(this.getInterpolation().apply(target, 0.0F, ratio), 0.0F, 1.0F));
            return false;
         } else {
            return true;
         }
      }
   }

   @Override
   public void stop(float position) {
      this.beginning = position;
   }

   @Override
   protected void end() {
      super.end();
      this.music.setVolume(0.0F);
   }

   @Override
   public void reset() {
      super.reset();
      this.beginning = 0.0F;
   }

   @Override
   public void restart() {
      super.restart();
   }
}
