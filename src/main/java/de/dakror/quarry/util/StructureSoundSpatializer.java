package de.dakror.quarry.util;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import net.spookygames.gdx.sfx.spatial.SpatializedSound;
import net.spookygames.gdx.sfx.spatial.Spatializer;

public class StructureSoundSpatializer implements Spatializer<Structure<?>> {
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

   public boolean canPlayAt(Structure<?> position) {
      if (Game.G.layer.getIndex() != position.layer.getIndex() && position.getSchema().type != StructureType.ShaftDrill) {
         return false;
      } else {
         float x = (float)position.x + (float)position.getWidth() / 2.0F;
         float y = (float)position.y + (float)position.getHeight() / 2.0F;
         float centerX = this.center.x / 64.0F;
         float centerY = this.center.y / 64.0F;
         float range2 = this.horizontalRange * this.horizontalRange;
         float dst2 = Vector2.dst2(x, y, centerX, centerY);
         float hRatio = 1.0F - MathUtils.clamp(dst2 / range2, 0.0F, 1.0F);
         float centerZ = this.center.z;
         float vRatio = 1.0F - MathUtils.clamp(centerZ / this.verticalRange, 0.0F, 1.0F);
         vRatio = vRatio * vRatio * vRatio;
         return hRatio * vRatio > 0.0F;
      }
   }

   @Override
   public void spatialize(SpatializedSound<Structure<?>> instance, float nominalVolume) {
      Structure<?> position = instance.getPosition();
      float x = (float)position.x + (float)position.getWidth() / 2.0F;
      float y = (float)position.y + (float)position.getHeight() / 2.0F;
      float centerX = this.center.x / 64.0F;
      float centerY = this.center.y / 64.0F;
      float range2 = this.horizontalRange * this.horizontalRange;
      float dst2 = Vector2.dst2(x, y, centerX, centerY);
      float hRatio = 1.0F - MathUtils.clamp(dst2 / range2, 0.0F, 1.0F);
      float centerZ = this.center.z;
      float vRatio = 1.0F - MathUtils.clamp(centerZ / this.verticalRange, 0.0F, 1.0F);
      vRatio = vRatio * vRatio * vRatio;
      float panning = (x - centerX) / this.horizontalRange;
      float volume = nominalVolume * hRatio * vRatio;

      try {
         if (volume != 0.0F && (Game.G.layer.getIndex() == position.layer.getIndex() || position.getSchema().type == StructureType.ShaftDrill)) {
            instance.setPan(MathUtils.clamp(panning, -1.0F, 1.0F), MathUtils.clamp(volume, 0.0F, 1.0F));
         } else {
            position.stopSfx();
         }
      } catch (NullPointerException var16) {
         position.stopSfx();
      }
   }
}
