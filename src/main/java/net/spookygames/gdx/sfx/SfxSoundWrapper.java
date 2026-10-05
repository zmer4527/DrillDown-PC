package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Sound;

public class SfxSoundWrapper implements SfxSound {
   private final Sound wrapped;
   private final String title;
   private final float duration;

   public SfxSoundWrapper(Sound wrappedSound, String title, float duration) {
      this.wrapped = wrappedSound;
      this.title = title;
      this.duration = duration;
   }

   @Override
   public String getTitle() {
      return this.title;
   }

   @Override
   public float getDuration() {
      return this.duration;
   }

   public long play() {
      return this.wrapped.play();
   }

   public long play(float volume) {
      return this.wrapped.play(volume);
   }

   public long play(float volume, float pitch, float pan) {
      return this.wrapped.play(volume, pitch, pan);
   }

   public long loop() {
      return this.wrapped.loop();
   }

   public long loop(float volume) {
      return this.wrapped.loop(volume);
   }

   public long loop(float volume, float pitch, float pan) {
      return this.wrapped.loop(volume, pitch, pan);
   }

   public void stop() {
      this.wrapped.stop();
   }

   public void pause() {
      this.wrapped.pause();
   }

   public void resume() {
      this.wrapped.resume();
   }

   public void dispose() {
      this.wrapped.dispose();
   }

   public void stop(long soundId) {
      this.wrapped.stop(soundId);
   }

   public void pause(long soundId) {
      this.wrapped.pause(soundId);
   }

   public void resume(long soundId) {
      this.wrapped.resume(soundId);
   }

   public void setLooping(long soundId, boolean looping) {
      this.wrapped.setLooping(soundId, looping);
   }

   public void setPitch(long soundId, float pitch) {
      this.wrapped.setPitch(soundId, pitch);
   }

   public void setVolume(long soundId, float volume) {
      this.wrapped.setVolume(soundId, volume);
   }

   public void setPan(long soundId, float pan, float volume) {
      this.wrapped.setPan(soundId, pan, volume);
   }

   @Override
   public String toString() {
      return this.title + " [" + this.duration + "] (SfxSoundWrapper)";
   }
}
