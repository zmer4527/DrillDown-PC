package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.audio.Sound;

public class FadingSpatializedSound<T> extends SpatializedSound<T> {
   private float fadeTime;
   private float fadeProgress = -1.0F;
   private boolean fadeIn;
   private float realVolume;
   private boolean stop = false;

   @Override
   public void reset() {
      super.reset();
      this.fadeTime = 0.0F;
      this.fadeProgress = -1.0F;
      this.stop = false;
      this.fadeIn = false;
      this.realVolume = 0.0F;
   }

   public long initialize(Sound sound, boolean looping, float duration, T position, float volume, float pitch, float panning, float fadeTime, boolean fadeIn) {
      long id = super.initialize(sound, looping, duration, position, volume, pitch, panning);
      this.fadeTime = fadeTime;
      if (fadeIn) {
         this.fadeIn();
      }

      return id;
   }

   public boolean isFading() {
      return this.fadeProgress > -1.0F;
   }

   @Override
   public void setVolume(float volume) {
      if (this.elapsed == 0.0F && this.fadeProgress > -1.0F) {
         this.realVolume = volume;
      } else if (this.getVolume() != volume) {
         super.setVolume(volume);
      }
   }

   @Override
   public void setPan(float pan, float volume) {
      if (this.elapsed == 0.0F && this.fadeProgress > -1.0F) {
         this.realVolume = volume;
         super.setPan(pan, 0.0F);
      } else {
         super.setPan(pan, volume);
      }
   }

   @Override
   public boolean update(float deltaTime) {
      if (this.getSound() == null) {
         return true;
      } else {
         if (this.fadeTime > 0.0F && this.fadeProgress > -1.0F) {
            float progress = this.fadeProgress / this.fadeTime;
            if (!this.fadeIn) {
               progress = 1.0F - progress;
            }

            this.setVolume(progress * this.realVolume);
            this.fadeProgress += deltaTime;
            if (this.fadeProgress >= this.fadeTime) {
               this.fadeProgress = -1.0F;
               this.setVolume(this.fadeIn ? this.realVolume : 0.0F);
               if (!this.fadeIn) {
                  if (this.stop) {
                     this.reset();
                  } else {
                     super.pause();
                  }
               }
            }
         }

         return super.update(deltaTime);
      }
   }

   @Override
   public void stop() {
      if (this.fadeTime > 0.0F) {
         this.stop = true;
         this.fadeOut();
      } else {
         super.stop();
      }
   }

   @Override
   public void resume() {
      if (this.getSound() != null) {
         super.resume();
         if (this.fadeTime > 0.0F) {
            this.fadeIn();
         }
      }
   }

   @Override
   public void pause() {
      if (this.fadeTime > 0.0F) {
         this.fadeOut();
      } else if (this.getSound() != null) {
         super.pause();
      }
   }

   public void fadeIn() {
      if (this.fadeTime > 0.0F) {
         this.realVolume = this.getVolume();
         this.setVolume(0.0F);
         this.fadeIn = true;
         this.fadeProgress = 0.0F;
      }
   }

   public void fadeOut() {
      if (this.fadeTime > 0.0F) {
         this.realVolume = this.getVolume();
         this.fadeIn = false;
         this.fadeProgress = 0.0F;
      }
   }
}
