package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class SfxPitchShiftingSoundWrapper extends SfxSoundWrapper {
   private final float minPitch;
   private final float maxPitch;

   public SfxPitchShiftingSoundWrapper(Sound wrappedSound, String title, float duration, float pitchRange) {
      super(wrappedSound, title, duration);
      float half = pitchRange / 2.0F;
      this.minPitch = 1.0F - half;
      this.maxPitch = 1.0F + half;
   }

   public SfxPitchShiftingSoundWrapper(Sound wrappedSound, String title, float duration, float minPitch, float maxPitch) {
      super(wrappedSound, title, duration);
      this.minPitch = minPitch;
      this.maxPitch = maxPitch;
   }

   @Override
   public long play() {
      return super.play(1.0F, this.randomPitchShift(), 0.0F);
   }

   @Override
   public long play(float volume) {
      return super.play(volume, this.randomPitchShift(), 0.0F);
   }

   @Override
   public long play(float volume, float pitch, float pan) {
      return super.play(volume, pitch * this.randomPitchShift(), pan);
   }

   @Override
   public long loop() {
      return super.loop(1.0F, this.randomPitchShift(), 0.0F);
   }

   @Override
   public long loop(float volume) {
      return super.loop(volume, this.randomPitchShift(), 0.0F);
   }

   @Override
   public long loop(float volume, float pitch, float pan) {
      return super.loop(volume, pitch, pan);
   }

   @Override
   public void setPitch(long soundId, float pitch) {
      super.setPitch(soundId, pitch * this.randomPitchShift());
   }

   private float randomPitchShift() {
      return MathUtils.random(this.minPitch, this.maxPitch);
   }
}
