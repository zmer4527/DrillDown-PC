package de.dakror.common.libgdx.audio;

import com.badlogic.gdx.audio.Sound;

public class AmbientSound {
   Sound sound;
   long soundId;
   double nominalVolume;
   double oldVolume;
   double volume;
   double progress;
   double interpolationTime;

   public AmbientSound(Sound sound, float interpolationTime) {
      this.sound = sound;
      this.interpolationTime = (double)interpolationTime;
      this.soundId = -1L;
   }

   public void setVolume(float volume) {
      if ((double)volume != this.nominalVolume) {
         this.oldVolume = this.volume;
         this.nominalVolume = (double)volume;
         this.progress = 0.0;
      }
   }

   public void update(double deltaTime) {
      if (this.volume != this.nominalVolume || this.soundId == -1L) {
         if (this.progress >= this.interpolationTime) {
            this.volume = this.nominalVolume;
            this.progress = 0.0;
         } else {
            this.volume = this.oldVolume + (this.nominalVolume - this.oldVolume) * (this.progress / this.interpolationTime);
            this.progress += deltaTime;
         }

         if (this.soundId == -1L) {
            this.soundId = this.sound.loop((float)this.volume);
         } else {
            this.sound.setVolume(this.soundId, (float)this.volume);
         }
      }
   }

   public void stop() {
      this.sound.stop();
      this.soundId = -1L;
   }
}
