package de.dakror.common.libgdx;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

public abstract class EditorCameraControl extends InputAdapter {
   protected Viewport viewport;
   protected OrthographicCamera cam;
   protected GestureDetector gd;
   public InputMultiplexer input;
   public float minZoom = 0.1F;
   public float maxZoom = 10.0F;
   public boolean elementPlaceable;
   protected boolean canPan = true;
   protected float dragX = -1.0F;
   protected float dragY = -1.0F;
   protected boolean zoomed;
   protected boolean p0Down = false;
   protected boolean p1Down = false;
   protected float initialZoom;
   protected boolean cameraChanged;
   protected final Vector2 activeElementPos = new Vector2();
   protected final Vector2 dragStart = new Vector2();
   protected final Vector3 dragStartCamPos = new Vector3();
   protected final Vector2 tmp = new Vector2();
   protected int tileSize;
   static final int scale = 10000;

   public EditorCameraControl(int tileSize, Viewport viewport) {
      this.tileSize = tileSize;
      this.viewport = viewport;
      this.cam = (OrthographicCamera)viewport.getCamera();
      this.gd = new GestureDetector(this);
      this.input = new InputMultiplexer(new InputProcessor[]{this, this.gd});
   }

   @Override
   public boolean touchDown(float x, float y, int pointer, int button) {
      this.initialZoom = this.cam.zoom;
      this.viewport.unproject(this.tmp.set(x, y));
      int tileX = (int)(this.tmp.x / (float)this.tileSize);
      int tileY = (int)(this.tmp.y / (float)this.tileSize);
      if (this.isActiveElementEnabled() && this.isWithinActiveElement((int)this.tmp.x, (int)this.tmp.y, tileX, tileY)) {
         this.canPan = false;
         this.dragX = x;
         this.dragY = y;
         this.setParamRawPosition(this.activeElementPos, (int)this.tmp.x, (int)this.tmp.y, tileX, tileY);
         return true;
      } else {
         return false;
      }
   }

   protected abstract boolean isWithinActiveElement(int var1, int var2, int var3, int var4);

   protected abstract void setParamRawPosition(Vector2 var1, int var2, int var3, int var4, int var5);

   @Override
   public boolean tap(float x, float y, int count, int button) {
      if (button == 1 && this.isActiveElementEnabled()) {
         this.resetActiveElement();
         return true;
      } else {
         this.viewport.unproject(this.tmp.set(x, y));
         int tileX = (int)(this.tmp.x / (float)this.tileSize);
         int tileY = (int)(this.tmp.y / (float)this.tileSize);
         this.updateActiveElementPlaceable();
         if (this.elementPlaceable && this.isWithinActiveElement((int)this.tmp.x, (int)this.tmp.y, tileX, tileY) && this.isActiveElementEnabled()) {
            this.placeActiveElement();
            this.updateActiveElementPlaceable();
            return true;
         } else if (this.handleInitialPlacement(tileX, tileY)) {
            this.setParamRawPosition(this.activeElementPos, (int)this.tmp.x, (int)this.tmp.y, tileX, tileY);
            this.updateActiveElementPlaceable();
            return true;
         } else {
            return this.handleTap((int)this.tmp.x, (int)this.tmp.y, tileX, tileY);
         }
      }
   }

   protected abstract void placeActiveElement();

   protected abstract void resetActiveElement();

   protected boolean handleTap(int x, int y, int tileX, int tileY) {
      return false;
   }

   protected abstract boolean handleInitialPlacement(int var1, int var2);

   @Override
   public boolean longPress(float x, float y) {
      return false;
   }

   @Override
   public boolean fling(float velocityX, float velocityY, int button) {
      return false;
   }

   @Override
   public boolean pan(float x, float y, float deltaX, float deltaY) {
      if (this.dragX > -1.0F) {
         float ratio = this.viewport.getWorldWidth() / (float)this.viewport.getScreenWidth() * this.cam.zoom;
         float dX = (this.dragX - x) * ratio;
         float dY = (y - this.dragY) * ratio;
         if (this.canPan) {
            this.cam.translate(dX, dY);
            if (this.cam.zoom > this.minZoom * 2.0F) {
               this.cam.position.x = (float)Math.round(this.cam.position.x);
               this.cam.position.y = (float)Math.round(this.cam.position.y);
            } else {
               this.cam.position.x = this.cam.position.x;
               this.cam.position.y = this.cam.position.y;
            }

            this.cameraChanged = true;
         } else if (this.isActiveElementEnabled()) {
            this.activeElementPos.sub(dX, dY);
            this.setActiveElementPosition(this.activeElementPos);
            this.updateActiveElementPlaceable();
         }
      }

      this.dragX = x;
      this.dragY = y;
      return false;
   }

   public void updateActiveElementPlaceable() {
      this.elementPlaceable = this.checkActiveElementPlaceable();
   }

   protected abstract void setActiveElementPosition(Vector2 var1);

   protected abstract boolean isActiveElementEnabled();

   @Override
   public boolean panStop(float x, float y, int pointer, int button) {
      this.dragX = -1.0F;
      this.dragY = -1.0F;
      return false;
   }

   @Override
   public boolean touchUp(int screenX, int screenY, int pointer, int button) {
      if (pointer != 1 && button != 1 || this.zoomed || this.p0Down || this.p1Down) {
         this.canPan = true;
         this.activeElementPos.setZero();
      }

      this.zoomed = false;
      if (pointer == 0) {
         this.p0Down = false;
      }

      if (pointer == 1) {
         this.p1Down = false;
      }

      this.dragX = -1.0F;
      this.dragY = -1.0F;
      return false;
   }

   @Override
   public boolean zoom(float initialDistance, float distance) {
      this.clampZoom(this.initialZoom * initialDistance / distance);
      this.zoomed = true;
      this.p0Down = true;
      this.p1Down = true;
      return false;
   }

   @Override
   public boolean scrolled(int amount) {
      this.clampZoom(this.cam.zoom + (float)amount / 10.0F);
      return true;
   }

   protected boolean clampZoom(float zoom) {
      float clamped = MathUtils.clamp(zoom, this.minZoom, this.maxZoom);
      this.cam.zoom = (float)Math.round(clamped * 10000.0F) / 10000.0F;
      this.cameraChanged = true;
      return zoom == clamped;
   }

   public abstract void clampCam(OrthographicCamera var1);

   public boolean isDragging() {
      return this.dragX > -1.0F;
   }

   public void update() {
      if (this.cameraChanged) {
         this.clampCam(this.cam);
         if (this.isActiveElementEnabled()) {
            this.updateActiveElementPlaceable();
         }

         this.cameraChanged = false;
      }
   }

   protected abstract boolean checkActiveElementPlaceable();
}
