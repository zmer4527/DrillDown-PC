package de.dakror.common.libgdx.ui;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.TemporalAction;

public class CameraAction extends TemporalAction {
   Vector3 from;
   Vector3 to;
   float zoomFrom;
   float zoomTo;
   OrthographicCamera cam;
   boolean done;

   public CameraAction(float duration, Vector3 target, float zoom, OrthographicCamera cam) {
      this(duration, target, zoom, cam, Interpolation.fade);
   }

   public CameraAction(float duration, Vector3 target, float zoom, OrthographicCamera cam, Interpolation interpolation) {
      super(duration, interpolation);
      this.from = new Vector3(cam.position);
      this.to = target;
      this.zoomFrom = cam.zoom;
      this.zoomTo = zoom;
      this.cam = cam;
   }

   protected void update(float percent) {
      this.cam
         .position
         .set(
            this.from.x + (this.to.x - this.from.x) * percent,
            this.from.y + (this.to.y - this.from.y) * percent,
            this.from.z + (this.to.z - this.from.z) * percent
         );
      this.cam.zoom = this.zoomFrom + (this.zoomTo - this.zoomFrom) * percent;
      this.cam.update();
   }

   protected void end() {
      this.done = true;
   }

   public boolean isDone() {
      return this.done;
   }
}
