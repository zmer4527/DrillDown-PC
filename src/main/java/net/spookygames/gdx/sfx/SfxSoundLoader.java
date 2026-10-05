package net.spookygames.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class SfxSoundLoader extends AsynchronousAssetLoader<SfxSound, SfxSoundLoader.SoundParameters> {
   private static SoundDurationResolver durationResolver = null;
   private final float defaultDuration;
   private Sound sound;

   public static void setDurationResolver(SoundDurationResolver soundDurationResolver) {
      durationResolver = soundDurationResolver;
   }

   public SfxSoundLoader(FileHandleResolver resolver) {
      this(resolver, 1.0F);
   }

   public SfxSoundLoader(FileHandleResolver resolver, float defaultDuration) {
      super(resolver);
      this.defaultDuration = defaultDuration;
   }

   public void loadAsync(AssetManager manager, String fileName, FileHandle file, SfxSoundLoader.SoundParameters parameter) {
      this.sound = Gdx.audio.newSound(file);
   }

   public SfxSound loadSync(AssetManager manager, String fileName, FileHandle file, SfxSoundLoader.SoundParameters parameter) {
      Sound sound = this.sound;
      this.sound = null;
      String title = null;
      if (parameter != null) {
         title = parameter.title;
      }

      if (title == null) {
         title = fileName;
      }

      float duration = -1.0F;
      if (parameter != null) {
         duration = parameter.duration;
      }

      if (duration <= 0.0F && durationResolver != null) {
         duration = durationResolver.resolveSoundDuration(sound, file);
      }

      if (duration <= 0.0F) {
         duration = this.defaultDuration;
      }

      float pitchRange = 0.0F;
      if (parameter != null) {
         pitchRange = parameter.pitchRange;
      }

      if (pitchRange > 1.0F) {
         pitchRange = 1.0F;
      }

      return (SfxSound)(pitchRange <= 0.0F ? new SfxSoundWrapper(sound, title, duration) : new SfxPitchShiftingSoundWrapper(sound, title, duration, pitchRange));
   }

   public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SfxSoundLoader.SoundParameters parameter) {
      return null;
   }

   public static class SoundParameters extends AssetLoaderParameters<SfxSound> {
      public String title = null;
      public float duration = -1.0F;
      public float pitchRange = 0.0F;
   }
}
