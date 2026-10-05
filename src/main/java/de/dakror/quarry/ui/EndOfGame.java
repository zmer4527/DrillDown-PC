package de.dakror.quarry.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.Callback;
import de.dakror.common.libgdx.ui.PfxActor;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.MainMenu;
import de.dakror.quarry.util.Util;

public class EndOfGame {
   Window endWindow;
   Image endOverlay;
   PfxActor endPfx;

   public EndOfGame(Stage stage) {
      Util.lml("endofgame");
      this.endOverlay = Util.id("overlay");
      this.endOverlay.setName("endOverlay");
      stage.addActor(this.endOverlay);
      this.endOverlay.setPosition(0.0F, 0.0F);
      this.endOverlay.setHeight(this.endOverlay.getHeight() + (float)Quarry.Q.safeInsets[1] + (float)Quarry.Q.safeInsets[3]);
      this.endPfx = new PfxActor(Game.G.confettiPfx);
      this.endPfx.setName("endPfx");
      stage.addActor(this.endPfx);
      this.endWindow = Util.id("window");
      this.endWindow.setVisible(false);
      this.endWindow.setPosition((Const.UI_W - this.endWindow.getWidth()) / 2.0F, (Const.UI_H - this.endWindow.getHeight()) / 2.0F);
      this.endWindow.getTitleTable().pad(80.0F, 0.0F, 20.0F, 0.0F);
      Button leave = Util.id("leave");
      leave.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            EndOfGame.this.hide();
            Game.G.ui.menu.save(new Callback<Void>() {
               public void call(Void e) {
                  Quarry.Q.addScene(MainMenu.M);
                  Quarry.Q.dropScene(Game.G);
               }
            });
         }
      });
      Button stay = Util.id("stay");
      stay.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            EndOfGame.this.hide();
            Game.G.setInfinite(true);
            Game.G.ui.menu.save(new Callback<Void>() {
               public void call(Void e) {
               }
            });
         }
      });
      this.endWindow.setName("endWindow");
      stage.addActor(this.endWindow);
   }

   public void show() {
      Game.G.setPaused(true);
      Game.G.ui.pauseButton.setChecked(true);
      Game.G.confettiPfx.reset();
      Game.G.confettiPfx.setPosition(Const.UI_W / 2.0F, Const.UI_H / 2.0F);
      this.endWindow
         .addAction(
            Actions.sequence(
               Actions.alpha(0.0F),
               Actions.visible(true),
               Actions.parallel(Actions.fadeIn(0.2F), Actions.sequence(Actions.delay(0.1F), Actions.run(new Runnable() {
                  @Override
                  public void run() {
                     EndOfGame.this.endPfx.start();
                  }
               })))
            )
         );
      this.endOverlay.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.alpha(0.4F, 0.15F, Interpolation.fade)));
   }

   public void hide() {
      this.endWindow.addAction(Actions.sequence(Actions.fadeOut(0.2F), Actions.visible(false)));
      this.endOverlay.addAction(Actions.sequence(Actions.fadeOut(0.2F), Actions.visible(false)));
   }
}
