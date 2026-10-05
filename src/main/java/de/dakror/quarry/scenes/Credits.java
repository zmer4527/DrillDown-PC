package de.dakror.quarry.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.dakror.common.libgdx.ui.Scene;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;

public class Credits extends Scene {
   TextureRegion px;
   ScrollPane sp;
   public static final Credits instance = new Credits();

   private Credits() {
      this.init();
   }

   @Override
   public void init() {
      this.px = Quarry.Q.atlas.findRegion("pixel");
      this.stage = new Stage(new FitViewport(Const.UI_W, Const.UI_H));
      this.stage.setActionsRequestRendering(false);
      Table table = new Table();
      table.setFillParent(true);
      table.row().grow();
      Label l = new Label(Quarry.Q.i18n.get("button.credits") + "\r\n\r\n" + Gdx.files.internal("CREDITS.txt").readString(), Quarry.Q.skin);
      l.setWrap(true);
      l.setAlignment(10);
      Table wrap = new Table();
      wrap.row().padLeft(35.0F).grow();
      wrap.add(l);
      this.sp = new ScrollPane(wrap);
      this.sp.setScrollBarPositions(true, false);
      this.sp.setVariableSizeKnobs(false);
      this.sp.setScrollingDisabled(true, false);
      this.sp.setSmoothScrolling(false);
      table.add(this.sp).left().top().pad(20.0F).grow();
      this.stage.addActor(table);
      this.stage.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            if (Credits.this.alpha == 1.0F) {
               Credits.this.fadeOut = true;
            }
         }
      });
      this.stage.addListener(new InputListener() {
         public boolean keyUp(InputEvent event, int keycode) {
            if (keycode == 4) {
               if (Credits.this.alpha == 1.0F) {
                  Credits.this.fadeOut = true;
               }

               return true;
            } else {
               return false;
            }
         }
      });
   }

   @Override
   public void show() {
      super.show();
      this.sp.setScrollY(0.0F);
   }

   @Override
   public void update(double deltaTime) {
      super.update(deltaTime);
      if (this.alpha == 0.0F && this.fadeOut) {
         Quarry.Q.dropScene();
      }
   }

   @Override
   public void draw() {
      this.stage.getBatch().begin();
      this.stage.getBatch().setColor(0.0F, 0.0F, 0.0F, 0.5F * this.alpha);
      this.stage.getBatch().draw(this.px, 0.0F, 0.0F, this.stage.getWidth(), this.stage.getHeight());
      this.stage.getBatch().setColor(1.0F, 1.0F, 1.0F, 1.0F);
      this.stage.getBatch().end();
      super.draw();
   }
}
