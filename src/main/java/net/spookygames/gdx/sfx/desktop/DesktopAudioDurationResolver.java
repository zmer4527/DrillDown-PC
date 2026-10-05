package net.spookygames.gdx.sfx.desktop;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl.audio.OpenALSound;
import com.badlogic.gdx.files.FileHandle;
import com.jcraft.jorbis.JOrbisException;
import com.jcraft.jorbis.VorbisFile;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;
import net.spookygames.gdx.sfx.MusicDurationResolver;
import net.spookygames.gdx.sfx.SfxMusicLoader;
import net.spookygames.gdx.sfx.SfxSoundLoader;
import net.spookygames.gdx.sfx.SoundDurationResolver;

public class DesktopAudioDurationResolver implements MusicDurationResolver, SoundDurationResolver {
   @Override
   public float resolveMusicDuration(Music music, FileHandle musicFile) {
      if (music instanceof com.badlogic.gdx.backends.lwjgl.audio.Wav.Music) {
         try {
            return this.wavDuration(musicFile);
         } catch (UnsupportedAudioFileException var4) {
            Gdx.app.error("gdx-sfx", "Unable to resolve duration of wav file " + musicFile.toString(), var4);
         } catch (IOException var5) {
            Gdx.app.error("gdx-sfx", "Unable to resolve duration of wav file " + musicFile.toString(), var5);
         }
      } else if (music instanceof com.badlogic.gdx.backends.lwjgl.audio.Mp3.Music) {
         try {
            return this.mp3Duration(musicFile);
         } catch (BitstreamException var7) {
            Gdx.app.error("gdx-sfx", "Unable to resolve duration of mp3 file " + musicFile.toString(), var7);
         }
      } else if (music instanceof com.badlogic.gdx.backends.lwjgl.audio.Ogg.Music) {
         try {
            return this.oggDuration(musicFile);
         } catch (JOrbisException var6) {
            Gdx.app.error("gdx-sfx", "Unable to resolve duration of ogg file " + musicFile.toString(), var6);
         }
      }

      return -1.0F;
   }

   private float wavDuration(FileHandle musicFile) throws UnsupportedAudioFileException, IOException {
      AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile.file());
      AudioFormat format = audioInputStream.getFormat();
      long frames = audioInputStream.getFrameLength();
      return (float)frames / format.getFrameRate();
   }

   private float mp3Duration(FileHandle musicFile) throws BitstreamException {
      Bitstream bitstream = new Bitstream(musicFile.read());
      int length = (int)musicFile.length();
      int streamPos = bitstream.header_pos();
      Header header = bitstream.readFrame();
      if (streamPos > 0 && length != -1 && streamPos < length) {
         length -= streamPos;
      }

      float totalMilliseconds = header.total_ms(length);
      return totalMilliseconds / 1000.0F;
   }

   private float oggDuration(FileHandle musicFile) throws JOrbisException {
      String path = null;
      File file = musicFile.file();
      FileHandle tmpFile = null;
      if (file.exists()) {
         path = file.getAbsolutePath();
      } else {
         tmpFile = FileHandle.tempFile("gdx-sfx.ogg.");
         musicFile.copyTo(tmpFile);
         path = tmpFile.file().getAbsolutePath();
      }

      VorbisFile vorbis = new VorbisFile(path);
      float durationInSeconds = vorbis.time_total(-1);
      if (tmpFile != null) {
         tmpFile.delete();
      }

      return durationInSeconds;
   }

   @Override
   public float resolveSoundDuration(Sound sound, FileHandle soundFile) {
      return ((OpenALSound)sound).duration();
   }

   public static void initialize() {
      DesktopAudioDurationResolver resolver = new DesktopAudioDurationResolver();
      SfxMusicLoader.setDurationResolver(resolver);
      SfxSoundLoader.setDurationResolver(resolver);
   }
}
