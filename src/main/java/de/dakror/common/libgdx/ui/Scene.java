package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;

public abstract class Scene {
   public Stage stage;
   protected float alpha = 1.0F;
   protected boolean fadeIn;
   protected boolean fadeOut;

   public abstract void init();

   public InputProcessor getInput() {
      return this.stage;
   }

   public void update(double deltaTime) {
      if (this.fadeIn) {
         if (this.alpha == 1.0F) {
            this.fadeIn = false;
         }

         this.alpha = (float)Math.min(1.0, (double)this.alpha + deltaTime * 4.0);
      }

      if (this.fadeOut) {
         this.alpha = (float)Math.max(0.0, (double)this.alpha - deltaTime * 4.0);
      }

      if (this.stage != null) {
         this.stage.act((float)deltaTime);
      }
   }

   public void draw() {
      if (this.stage != null) {
         if (this.alpha == 1.0F) {
            this.stage.draw();
         } else if (this.alpha != 0.0F) {
            Camera camera = this.stage.getViewport().getCamera();
            camera.update();
            if (!this.stage.getRoot().isVisible()) {
               return;
            }

            Batch batch = this.stage.getBatch();
            if (batch != null) {
               batch.setProjectionMatrix(camera.combined);
               batch.begin();
               this.stage.getRoot().draw(batch, this.alpha);
               batch.end();
            }
         }
      }
   }

   public void show() {
      this.fadeIn = true;
      this.fadeOut = false;
      this.alpha = 0.0F;
   }

   public void hide() {
   }

   public void pause() {
   }

   public void resume() {
   }

   public void dispose() {
      this.stage.dispose();
   }

   public void resize(final int width, final int height) {
      Gdx.app.postRunnable(new Runnable() {
         @Override
         public void run() {
            if (Scene.this.stage != null) {
               Scene.this.stage.getViewport().update(width, height, true);
            }
         }
      });
   }
}
