package de.dakror.quarry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.I18NBundle;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.DefaultLmlSyntax;
import com.github.czyzby.lml.util.Lml;
import com.github.czyzby.lml.util.LmlParserBuilder;
import de.dakror.common.libgdx.GameBase;
import de.dakror.common.libgdx.I18NBundleDelegate;
import de.dakror.common.libgdx.PlatformInterface;
import de.dakror.common.libgdx.audio.SoundManager;
import de.dakror.common.libgdx.ui.Scene;
import de.dakror.gen.CustomTagRegistrator;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.LoadingScreen;
import de.dakror.quarry.scenes.MainMenu;
import de.dakror.quarry.ui.UiLayoutMetrics;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Quarry extends GameBase implements PlatformInterface {
   public static Quarry Q;
   public BitmapFont font;
   public Skin skin;
   public LmlParser lml;
   public TextureAtlas atlas;
   public I18NBundleDelegate i18n;
   public Preferences prefs;
   public int[] safeInsets;
   public UiLayoutMetrics uiMetrics;
   public FrameBuffer[] chunkFBOs;
   public AssetManager assets;
   public SoundManager sound;
   public final boolean fullVersion;
   public final int versionNumber;
   public final String version;
   public boolean newAndroid;
   private int filePermissionState;
   public Sound clickSfx;
   public ExecutorService threadPool = Executors.newSingleThreadExecutor();

   public Quarry(PlatformInterface pi, boolean fullVersion, int versionNumber, String version, boolean desktop, boolean newAndroid, GameBase.WindowMode mode) {
      super(mode, desktop, pi);
      this.fullVersion = fullVersion;
      this.versionNumber = versionNumber;
      this.version = version;
      this.newAndroid = newAndroid;
   }

   public boolean isVersion() {
      return this.versionNumber > 1 && !this.version.equals("debug");
   }

   @Override
   public void create() {
      super.create();
      this.safeInsets = (int[])this.pi.message(15, null);
      this.uiMetrics = new UiLayoutMetrics();
      if (this.desktop) {
         float ratio = Math.max(1920.0F / (float)Gdx.graphics.getWidth(), 1080.0F / (float)Gdx.graphics.getHeight());
         Const.W = (float)Gdx.graphics.getWidth() * ratio;
         Const.H = (float)Gdx.graphics.getHeight() * ratio;
         Const.UI_H = Const.H * 1.5F;
         Const.UI_W = Const.W * 1.5F;
      } else {
         float ratio = Math.max(1080.0F / (float)Gdx.graphics.getWidth(), 1920.0F / (float)Gdx.graphics.getHeight());
         Const.W = (float)Gdx.graphics.getWidth() * ratio;
         Const.H = (float)Gdx.graphics.getHeight() * ratio;
         Const.UI_H = Const.H * 0.75F;
         Const.UI_W = Const.W * 0.75F;
      }

      this.uiMetrics.update(Const.UI_W, Const.UI_H, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this.safeInsets);
      if (!this.isVersion()) {
         Gdx.app.setLogLevel(3);
      }

      if (Gdx.app.getType() == ApplicationType.iOS) {
         Const.SFX_FORMAT = ".wav";
         Const.MUSIC_FORMAT = ".mp3";
      }

      Q = this;
      this.prefs = Gdx.app.getPreferences("TheQuarry");
      this.i18n = new I18NBundleDelegate(
         I18NBundle.createBundle(Gdx.files.internal("i18n/TheQuarry"), this.prefs.getBoolean("english", false) ? Locale.ENGLISH : Locale.SIMPLIFIED_CHINESE)
      );

      try {
         this.prefs.getFloat("sound", 1.0F);
      } catch (Exception var3) {
         this.prefs.putFloat("sound", 1.0F).flush();
      }

      try {
         this.prefs.getFloat("music", 1.0F);
      } catch (Exception var2) {
         this.prefs.putFloat("music", 1.0F).flush();
      }

      this.sound = new SoundManager(this.prefs.getFloat("sound", 1.0F), this.prefs.getFloat("music", 1.0F), 2.0F);
      this.sound.setPlaySound(this.sound.getSoundVolume() > 0.0F);
      this.sound.setPlayMusic(this.sound.getMusicVolume() > 0.0F);
      Scene s = new LoadingScreen();
      s.init();
      this.addScene(s);
   }

   @Override
   public void pause() {
      try {
         this.threadPool.shutdown();
         this.threadPool.awaitTermination(10L, TimeUnit.SECONDS);
      } catch (InterruptedException var2) {
         var2.printStackTrace();
      }

      this.threadPool = Executors.newSingleThreadExecutor();
      super.pause();
      this.sound.pauseMusic();
   }

   @Override
   public void resume() {
      super.resume();
      this.sound.resumeMusic();
   }

   @Override
   public void update() {
      super.update();
      this.sound.update(this.updateRate);
   }

   public void loadingFinished() {
      this.clickSfx = (Sound)this.assets.get("sfx/click3" + Const.SFX_FORMAT, Sound.class);
      long t = System.currentTimeMillis();
      LmlParserBuilder b = Lml.parser(this.skin).i18nBundle(this.i18n.getBundle()).syntax(new DefaultLmlSyntax() {
         public char getIdSeparatorMarker() {
            return '~';
         }
      });
      CustomTagRegistrator.addCustomTags(b);
      this.lml = b.build();
      this.lml.setStrict(false);
      this.lml.getData().addArgument("UI_W", Const.UI_W);
      this.lml.getData().addArgument("UI_H", Const.UI_H);
      this.lml.getData().addArgument("BUILD_RING_ITEM_SIZE", 100);
      System.out.println("LML took " + (System.currentTimeMillis() - t) + " ms");
      t = System.currentTimeMillis();
      Game g = new Game();
      g.init();
      Q.pi.message(-1230, this);
      System.out.println("Game init took " + (System.currentTimeMillis() - t) + " ms");
      this.chunkFBOs = new FrameBuffer[4];
      int chunksH = (int)Math.ceil(2.0);

      for (int i = 0; i < 64; i += 32) {
         for (int j = 0; j < 64; j += 32) {
            this.chunkFBOs[i / 32 * chunksH + j / 32] = new FrameBuffer(Format.RGB888, 2048, 2048, false);
         }
      }

      Scene s = new MainMenu();
      s.init();
      this.addSceneBelow(s);
      Scene q = this.dropScene();
      q.dispose();
   }

   public FileHandle[] listFiles() {
      FileHandle[] local = Gdx.files.local("TheQuarry/saves/").list(".qsf");
      FileHandle[] external = Gdx.files.external("TheQuarry/saves/").list(".qsf");
      FileHandle[] all = new FileHandle[local.length + external.length];
      System.arraycopy(local, 0, all, 0, local.length);
      System.arraycopy(external, 0, all, local.length, external.length);
      return all;
   }

   public FileHandle file(String text, boolean write) {
      if (this.hasFilePerm()) {
         FileHandle local = Gdx.files.local(text);
         FileHandle fh = Gdx.files.external("TheQuarry/saves");
         fh.mkdirs();
         if (!fh.exists()) {
            return local;
         } else {
            FileHandle external = Gdx.files.external(text);
            if (write) {
               return external;
            } else if (local.exists()) {
               return local.lastModified() > external.lastModified() ? local : external;
            } else {
               return external;
            }
         }
      } else {
         return Gdx.files.local(text);
      }
   }

   private boolean hasFilePerm() {
      if (this.newAndroid) {
         return true;
      } else {
         this.filePermissionState = -1;
         if (!(Boolean)this.pi.message(13, null)) {
            return false;
         } else {
            this.filePermissionState = 1;
            return this.filePermissionState == 1;
         }
      }
   }

   @Override
   public Object message(int messageCode, Object payload) {
      switch (messageCode) {
         case 13:
            this.filePermissionState = (Boolean)payload ? 1 : 0;
         default:
            return null;
      }
   }
}
