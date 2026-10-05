package de.dakror.quarry.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;

public class PalletDrawable extends BaseDrawable implements TransformDrawable {
   TransformDrawable base;
   TransformDrawable stackable;
   final float s = 0.58F;

   public PalletDrawable(TransformDrawable base, TransformDrawable stackable) {
      this.base = base;
      this.stackable = stackable;
      this.setMinWidth(1.0F);
      this.setMinHeight(1.0F);
   }

   public void draw(Batch batch, float x, float y, float width, float height) {
      this.base.draw(batch, x, y, width, height);
      this.stackable.draw(batch, x + (width - width * 0.58F) / 2.0F, y + (height - height * 0.58F) / 2.0F, width * 0.58F, height * 0.58F);
   }

   public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
      this.base.draw(batch, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
      this.stackable
         .draw(
            batch,
            x + (width - width * 0.58F) / 2.0F,
            y + (height - height * 0.58F) / 2.0F,
            originX,
            originY,
            width * 0.58F,
            height * 0.58F,
            scaleX,
            scaleY,
            rotation
         );
   }
}
