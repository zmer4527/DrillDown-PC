package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

public interface SoundDurationResolver {
   float resolveSoundDuration(Sound var1, FileHandle var2);
}
