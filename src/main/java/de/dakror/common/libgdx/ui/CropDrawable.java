package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class CropDrawable extends TextureRegionDrawable {
   float heightRatio;
   float widthRatio;

   public CropDrawable() {
   }

   public CropDrawable(TextureRegion region) {
      super(region);
   }

   public CropDrawable(TextureRegionDrawable drawable) {
      super(drawable);
   }

   public void setWidthRatio(float width) {
      this.widthRatio = width;
   }

   public void setHeightRatio(float height) {
      this.heightRatio = height;
   }

   public void setRegion(TextureRegion region) {
      super.setRegion(region);
      this.widthRatio = 1.0F;
      this.heightRatio = 1.0F;
   }

   public void draw(Batch batch, float x, float y, float width, float height) {
      TextureRegion tr = this.getRegion();
      batch.draw(
         tr.getTexture(),
         x,
         y,
         this.widthRatio * width,
         this.heightRatio * height,
         tr.getU(),
         tr.getV2(),
         tr.getU2(),
         tr.getV() + (tr.getV2() - tr.getV()) * (1.0F - this.heightRatio)
      );
   }
}
