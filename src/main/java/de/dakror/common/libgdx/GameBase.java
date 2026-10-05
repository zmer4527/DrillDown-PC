package de.dakror.common.libgdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.WindowedMean;
import de.dakror.common.libgdx.ui.Scene;
import java.util.Stack;

public abstract class GameBase extends ApplicationAdapter {
   protected final Stack<Scene> sceneStack = new Stack<>();
   protected InputMultiplexer input;
   public PlatformInterface pi;
   int w;
   int h;
   protected final WindowedMean updateTimeWindow = new WindowedMean(10);
   protected final WindowedMean frameTimeWindow = new WindowedMean(10);
   long lastUpdateTime;
   float updateTime;
   long lastFrameTime;
   float frameTime;
   GameBase.WindowMode mode;
   protected float updateRate = 0.016666668F;
   double currentTime;
   public boolean desktop;

   public GameBase(GameBase.WindowMode mode, boolean desktop, PlatformInterface pi) {
      this.pi = pi;
      this.desktop = desktop;
      this.mode = mode;
   }

   protected void setWindowMode(GameBase.WindowMode mode) {
      if (Gdx.app.getType() == ApplicationType.Desktop) {
         Gdx.input.setCursorCatched(false);
         if (mode == GameBase.WindowMode.Borderless) {
            Gdx.graphics.setUndecorated(true);
            Gdx.graphics.setWindowedMode(Gdx.graphics.getDisplayMode().width, Gdx.graphics.getDisplayMode().height);
            Gdx.graphics.setResizable(true);
         } else if (mode == GameBase.WindowMode.Fullscreen && !Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
         }

         this.mode = mode;
      }
   }

   public void create() {
      this.input = new InputMultiplexer();
      if (Gdx.app.getType() == ApplicationType.Desktop) {
         this.input.addProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
               if (keycode == 254) {
                  if (GameBase.this.mode == null) {
                     GameBase.this.mode = GameBase.WindowMode.Borderless;
                  }

                  GameBase.this.setWindowMode(GameBase.WindowMode.values()[(GameBase.this.mode.ordinal() + 1) % GameBase.WindowMode.values().length]);
                  return true;
               } else {
                  return false;
               }
            }
         });
         this.setWindowMode(this.mode);
      }

      Gdx.input.setInputProcessor(this.input);
      this.currentTime = (double)System.nanoTime() / 1.0E9;
   }

   public Scene getScene() {
      synchronized (this.sceneStack) {
         return this.sceneStack.isEmpty() ? null : this.sceneStack.peek();
      }
   }

   public void addScene(Scene scene) {
      synchronized (this.sceneStack) {
         if (scene.getInput() != null) {
            this.input.addProcessor(0, scene.getInput());
         }

         this.sceneStack.push(scene);
         scene.show();
         scene.resize(this.w, this.h);
      }
   }

   public void addSceneBelow(Scene scene) {
      synchronized (this.sceneStack) {
         if (scene.getInput() != null) {
            this.input.addProcessor(1, scene.getInput());
         }

         this.sceneStack.add(Math.max(0, this.sceneStack.size() - 1), scene);
         scene.show();
         scene.resize(this.w, this.h);
      }
   }

   public boolean dropScene(Scene scene) {
      synchronized (this.sceneStack) {
         scene.hide();
         boolean res = this.sceneStack.remove(scene);
         if (scene.getInput() != null) {
            this.input.removeProcessor(scene.getInput());
         }

         return res;
      }
   }

   public boolean hasScene(Scene scene) {
      synchronized (this.sceneStack) {
         return this.sceneStack.contains(scene);
      }
   }

   public Scene dropScene() {
      synchronized (this.sceneStack) {
         this.sceneStack.peek().hide();
         this.input.removeProcessor(0);
         return this.sceneStack.pop();
      }
   }

   public void resize(int width, int height) {
      this.w = width;
      this.h = height;
      synchronized (this.sceneStack) {
         for (Scene scene : this.sceneStack) {
            scene.resize(width, height);
         }
      }
   }

   public void pause() {
      synchronized (this.sceneStack) {
         for (Scene scene : this.sceneStack) {
            scene.pause();
         }
      }
   }

   public void resume() {
      this.currentTime = (double)System.nanoTime() / 1.0E9;
      synchronized (this.sceneStack) {
         for (Scene scene : this.sceneStack) {
            scene.resume();
         }
      }
   }

   public void update() {
      double newTime = (double)System.nanoTime() / 1.0E9;
      double deltaTime = Math.min(newTime - this.currentTime, 1.0);
      this.currentTime = newTime;
      long t = System.nanoTime();
      synchronized (this.sceneStack) {
         for (int i = this.sceneStack.size() - 1; i > -1; i--) {
            Scene s = null;

            try {
               s = this.sceneStack.get(i);
            } catch (ArrayIndexOutOfBoundsException var12) {
               var12.printStackTrace();
            }

            if (s != null) {
               s.update(deltaTime);
            }
         }
      }

      long realDelta = System.nanoTime() - t;
      this.updateTimeWindow.addValue((float)realDelta);
   }

   public void render() {
      try {
         synchronized (this.sceneStack) {
            this.update();
            long t = System.nanoTime();
            Gdx.gl.glClear(16384);

            for (Scene scene : this.sceneStack) {
               scene.draw();
            }

            this.frameTimeWindow.addValue((float)(System.nanoTime() - t));
         }
      } catch (Exception var8) {
         this.pi.message(1, var8);
         if (var8 instanceof RuntimeException) {
            throw var8;
         }
      }
   }

   public void dispose() {
      synchronized (this.sceneStack) {
         for (Scene scene : this.sceneStack) {
            scene.dispose();
         }
      }
   }

   public float getUpdateTime() {
      if (System.currentTimeMillis() - this.lastUpdateTime > 1000L) {
         this.updateTime = this.updateTimeWindow.getMean() / 1000000.0F;
         this.lastUpdateTime = System.currentTimeMillis();
      }

      return this.updateTime;
   }

   public float getFrameTime() {
      if (System.currentTimeMillis() - this.lastFrameTime > 1000L) {
         this.frameTime = this.frameTimeWindow.getMean() / 1000000.0F;
         this.lastFrameTime = System.currentTimeMillis();
      }

      return this.frameTime;
   }

   public static enum WindowMode {
      Fullscreen,
      Borderless;
   }
}
