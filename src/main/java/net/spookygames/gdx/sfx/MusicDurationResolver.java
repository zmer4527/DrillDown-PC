package net.spookygames.gdx.sfx;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;

public interface MusicDurationResolver {
   float resolveMusicDuration(Music var1, FileHandle var2);
}
