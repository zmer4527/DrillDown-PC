package net.spookygames.gdx.sfx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class SfxMusicLoader extends AsynchronousAssetLoader<SfxMusic, SfxMusicLoader.MusicParameters> {
   private static MusicDurationResolver durationResolver = null;
   private final float defaultDuration;
   private Music music;

   public static void setDurationResolver(MusicDurationResolver musicDurationResolver) {
      durationResolver = musicDurationResolver;
   }

   public SfxMusicLoader(FileHandleResolver resolver) {
      this(resolver, 60.0F);
   }

   public SfxMusicLoader(FileHandleResolver resolver, float defaultDuration) {
      super(resolver);
      this.defaultDuration = defaultDuration;
   }

   public void loadAsync(AssetManager manager, String fileName, FileHandle file, SfxMusicLoader.MusicParameters parameter) {
      this.music = Gdx.audio.newMusic(file);
   }

   public SfxMusic loadSync(AssetManager manager, String fileName, FileHandle file, SfxMusicLoader.MusicParameters parameter) {
      Music music = this.music;
      this.music = null;
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
         duration = durationResolver.resolveMusicDuration(music, file);
      }

      if (duration <= 0.0F) {
         duration = this.defaultDuration;
      }

      return new SfxMusicWrapper(music, title, duration);
   }

   public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SfxMusicLoader.MusicParameters parameter) {
      return null;
   }

   public static class MusicParameters extends AssetLoaderParameters<SfxMusic> {
      public String title = null;
      public float duration = -1.0F;
   }
}
