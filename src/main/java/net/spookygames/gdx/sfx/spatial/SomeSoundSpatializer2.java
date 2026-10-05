package net.spookygames.gdx.sfx.spatial;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class SomeSoundSpatializer2 implements Spatializer<Vector2> {
   private float horizontalRange;
   private float verticalRange;
   private final Vector3 center = new Vector3();

   public float getHorizontalRange() {
      return this.horizontalRange;
   }

   public void setHorizontalRange(float horizontalRange) {
      if (this.horizontalRange != horizontalRange) {
         this.horizontalRange = horizontalRange;
      }
   }

   public float getVerticalRange() {
      return this.verticalRange;
   }

   public void setVerticalRange(float verticalRange) {
      if (this.verticalRange != verticalRange) {
         this.verticalRange = verticalRange;
      }
   }

   public Vector3 getCenter() {
      return this.center;
   }

   public void setCenter(float x, float y, float z) {
      if (!this.center.epsilonEquals(x, y, z, 0.01F)) {
         this.center.set(x, y, z);
      }
   }

   public void setCenter(Vector3 center) {
      if (!this.center.epsilonEquals(center, 0.01F)) {
         this.center.set(center);
      }
   }

   @Override
   public void spatialize(SpatializedSound<Vector2> instance, float nominalVolume) {
      Vector2 position = instance.getPosition();
      float x = position.x;
      float y = position.y;
      float centerX = this.center.x;
      float centerY = this.center.y;
      float range2 = this.horizontalRange * this.horizontalRange;
      float dst2 = Vector2.dst2(x, y, centerX, centerY);
      float hRatio = 1.0F - MathUtils.clamp(dst2 / range2, 0.0F, 1.0F);
      float centerZ = this.center.z;
      float vRatio = 1.0F - MathUtils.clamp(centerZ / this.verticalRange, 0.0F, 1.0F);
      vRatio = vRatio * vRatio * vRatio;
      float panning = (x - centerX) / this.horizontalRange;
      float volume = nominalVolume * hRatio * vRatio;
      instance.setPan(MathUtils.clamp(panning, -1.0F, 1.0F), MathUtils.clamp(volume, 0.0F, 1.0F));
   }
}
