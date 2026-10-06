package de.dakror.quarry.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import de.dakror.common.libgdx.ui.CameraAction;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.producer.Mine;
import java.util.Locale;

public class Tutorial {
   Array<Tutorial.Step> steps;
   int stepNum;
   Window win;
   Label l;
   public Image indicator;
   Tutorial.IndicatorAction indicatorAction;

   public Tutorial(Stage stage, Skin skin) {
      this.win = new Window("", skin);
      this.win.setResizable(false);
      this.win.setMovable(false);
      this.win.setVisible(false);
      this.win.pad(32.0F);
      this.win.setSize(65.0F, 65.0F);
      this.win.setKeepWithinStage(false);
      this.win.setPosition(Const.UI_W / 2.0F, Const.UI_H / 2.0F);
      this.win.setTouchable(Touchable.enabled);
      this.win
         .addListener(
            new ClickListener() {
               public void clicked(InputEvent event, float x, float y) {
                  Tutorial.Step step = Tutorial.this.getStep();
                  if (step != null) {
                     if (step.tappable
                        && event.getStageX() >= step.x
                        && event.getStageX() <= step.x + step.width
                        && event.getStageY() >= step.y
                        && event.getStageY() <= step.y + step.height) {
                        Quarry.Q.sound.play(Quarry.Q.clickSfx);
                        Tutorial.this.next();
                     }
                  }
               }
            }
         );
      this.l = new Label("", skin);
      this.l.setWrap(true);
      this.l.setAlignment(10);
      this.l.getStyle().font.getData().markupEnabled = true;
      this.win.add(this.l).grow().top();
      this.win.setName("tutorial");
      stage.addActor(this.win);
      this.indicator = new Image(skin.getDrawable("caret_down"));
      this.indicator.addAction(this.indicatorAction = new Tutorial.IndicatorAction());
      this.indicator.setSize(24.0F, 10.0F);
      this.indicator.setVisible(false);
      this.indicator.setName("tutorial_indicator");
      stage.addActor(this.indicator);
      this.steps = new Array();
      this.reset();
      this.initSteps();
   }

   public void update() {
      Tutorial.Step step = this.getStep();
      if (step != null && !step.tappable && step.isDone()) {
         this.next();
      }
   }

   public Tutorial.Step getStep() {
      return this.stepNum >= 0 && this.stepNum < this.steps.size ? (Tutorial.Step)this.steps.get(this.stepNum) : null;
   }

   public int getStepNum() {
      return this.stepNum;
   }

   public void reset() {
      this.stepNum = -1;
      if (this.win.getStage() != null) {
         this.win.addAction(Actions.sequence(Actions.fadeOut(0.15F), Actions.visible(false)));
         this.indicator.clearActions();
         this.indicator.setVisible(false);
      }
   }

   protected void initSteps() {
      int[][] heights = new int[][]{
         {400, 480},
         {330, 330},
         {250, 330},
         {230, 300},
         {370, 370},
         {380, 400},
         new int[0],
         {300, 270},
         {430, 520},
         {330, 330},
         {300, 300},
         {440, 480},
         {440, 440},
         {440, 440}
      };
      int l = Quarry.Q.i18n.getLocale().getLanguage().equals(new Locale("de").getLanguage()) ? 1 : 0;
      this.steps.add(new Tutorial.Step((Const.UI_W - 600.0F) / 2.0F, (Const.UI_H - (float)heights[0][l]) / 2.0F, 600.0F, (float)heights[0][l], true, true) {
         @Override
         public void onShow(Window w) {
            super.onShow(w);
            Game.G.ui.destroyButton.setDisabled(true);
            Game.G.ui.destroyButton.setVisible(false);
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, 0.0F, Const.UI_W, (float)heights[1][l], true, true));
      this.steps.add(new Tutorial.Step(0.0F, 0.0F, Const.UI_W, (float)heights[2][l], false, false) {
         @Override
         public boolean isDone() {
            return Game.G.getZoom() >= 3968.0F / Const.W;
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, 0.0F, Const.UI_W, (float)heights[3][l], false, false) {
         @Override
         public boolean isDone() {
            return true;
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, Const.UI_H - (float)heights[4][l], Const.UI_W - 150.0F, (float)heights[4][l], true, true));
      this.steps.add(new Tutorial.Step(250.0F, Const.UI_H - (float)heights[5][l], Const.UI_W - 250.0F, (float)heights[5][l], true, true));
      this.steps
         .add(
            new Tutorial.Step(0.0F, 0.0F, 0.0F, 0.0F, false, true) {
               CameraAction action;

               @Override
               public void onShow(Window w) {
                  this.action = new CameraAction(
                     0.5F,
                     new Vector3(((float)Game.G.layer.width + 0.75F) * 64.0F / 2.0F, (float)((Game.G.layer.height - 7) * 64 / 2), 0.0F),
                     0.75F,
                     Game.G.getCamera()
                  );
                  Tutorial.this.win.addAction(this.action);
               }

               @Override
               public boolean isDone() {
                  return this.action.isDone();
               }
            }
         );
      this.steps.add(new Tutorial.Step(0.0F, 450.0F, Const.UI_W, (float)heights[7][l], false, false) {
         @Override
         public boolean isDone() {
            return Game.G.ui.tooltipCurrentStructure instanceof Mine;
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, 450.0F, Const.UI_W, (float)heights[8][l], false, false) {
         @Override
         public boolean isDone() {
            return Game.G.activeStructure instanceof Mine;
         }
      });
      this.steps
         .add(
            new Tutorial.Step(0.0F, 120.0F, Const.UI_W, (float)heights[9][l], false, false) {
               CameraAction action;

               @Override
               public void onShow(Window w) {
                  this.action = new CameraAction(
                     0.5F,
                     new Vector3(((float)Game.G.layer.width + 0.75F) * 64.0F / 2.0F, (float)((Game.G.layer.height - 14) * 64 / 2), 0.0F),
                     0.75F,
                     Game.G.getCamera()
                  );
                  Tutorial.this.win.addAction(this.action);
                  Game.G.highlightArea(2009, 2010, 2073, 2074);
               }

               @Override
               public boolean isDone() {
                  return this.action.isDone() && Game.G.getTutorialHighlight().size == 0;
               }
            }
         );
      this.steps.add(new Tutorial.Step(0.0F, 120.0F, Const.UI_W, (float)heights[10][l], false, false) {
         @Override
         public void onShow(Window w) {
            Game.G.resetActiveStructure();
            Game.G.ui.hideTooltip();
         }

         @Override
         public boolean isDone() {
            return Game.G.activeStructure != null && Game.G.activeStructure.getSchema().type == StructureType.Conveyor;
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, 120.0F, Const.UI_W, (float)heights[11][l], false, false) {
         @Override
         public void onShow(Window w) {
            Game.G.highlightArea(2011, 2012, 2013, 2014);
         }

         @Override
         public boolean isDone() {
            return Game.G.getTutorialHighlight().size == 0;
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, Const.UI_H - (float)heights[12][l], Const.UI_W, (float)heights[12][l], false, false) {
         boolean hasBamboozled = false;

         @Override
         public void onShow(Window w) {
            synchronized (Game.renderThreadLock) {
               Game.G.renderThreadTasks.add(new Runnable() {
                  @Override
                  public void run() {
                     Conveyor c = (Conveyor)Game.G.layer.getStructure(2012);
                     c.setRotation(Direction.East);
                     hasBamboozled = true;
                  }
               });
            }
         }

         @Override
         public boolean isDone() {
            if (!this.hasBamboozled) {
               return false;
            } else {
               for (int i = 2011; i <= 2014; i++) {
                  if (Game.G.layer.getStructure(i) == null || ((Conveyor)Game.G.layer.getStructure(i)).getDirection() != Direction.North) {
                     return false;
                  }
               }

               return true;
            }
         }
      });
      this.steps.add(new Tutorial.Step(0.0F, Const.UI_H - (float)heights[13][l], Const.UI_W - 150.0F, (float)heights[13][l], true, true) {
         @Override
         public void onShow(Window w) {
            super.onShow(w);
            Game.G.ui.destroyButton.setDisabled(false);
            Game.G.ui.destroyButton.setVisible(true);
         }
      });
   }

   public void next() {
      Tutorial.Step old = this.getStep();
      this.win.clearActions();
      this.stepNum++;
      this.indicator.clearActions();
      this.indicator.setVisible(false);
      if (this.stepNum >= this.steps.size) {
         this.reset();
      } else {
         float durationB = 0.4F;
         float durationA = 0.2F;
         Interpolation interpA = Interpolation.pow3In;
         Interpolation interpB = Interpolation.pow3Out;
         Tutorial.Step step = this.getStep();
         step.onShow(this.win);
         if (Quarry.Q.desktop) {
            step.width = Math.min(720.0F, step.width);
            step.x = (Const.UI_W - step.width) / 2.0F;
         }

         this.win.setWidth(step.width);
         this.l.setWidth(step.width - 64.0F);
         String text = Quarry.Q.i18n.get("tutorial.step" + this.stepNum);
         if (!Quarry.Q.fullVersion) {
            String demoText = Quarry.Q.i18n.get("tutorial.step" + this.stepNum + "_demo");
            if (demoText != null && !demoText.startsWith("???")) {
               text = demoText;
            }
         } else if (Quarry.Q.desktop) {
            String desktopText = Quarry.Q.i18n.get("tutorial.step" + this.stepNum + "_desktop");
            if (desktopText != null && !desktopText.startsWith("???")) {
               text = desktopText;
            }
         }

         this.l.setText(text.replace("\r", ""));
         this.win.invalidateHierarchy();
         this.win.validate();
         float oh = step.height;
         step.height = this.win.getPrefHeight();
         if (step.y == Const.UI_H - oh) {
            step.y = step.y - (step.height - oh);
         }

         if (step.x < (float)Quarry.Q.safeInsets[0]) {
            step.x = (float)Quarry.Q.safeInsets[0];
         }

         if (step.y < (float)Quarry.Q.safeInsets[3]) {
            step.y = (float)Quarry.Q.safeInsets[3];
         }

         if (step.y + step.height >= Const.UI_H - (float)Quarry.Q.safeInsets[1]) {
            step.y = Const.UI_H - (float)Quarry.Q.safeInsets[1] - step.height;
         }

         if (step.x + step.width >= Const.UI_W - (float)Quarry.Q.safeInsets[2]) {
            step.x = Const.UI_W - (float)Quarry.Q.safeInsets[2] - step.width;
         }

         if (this.stepNum == 0) {
            this.win
               .addAction(
                  Actions.sequence(
                     Actions.alpha(0.0F), Actions.moveTo(step.x, step.y), Actions.sizeTo(step.width, step.height), Actions.visible(true), Actions.fadeIn(0.15F)
                  )
               );
         } else if (step.width > 0.0F) {
            this.win
               .addAction(
                  Actions.sequence(
                     Actions.parallel(
                        Actions.alpha(0.4F, durationA),
                        Actions.sizeTo(100.0F, 100.0F, durationA, interpA),
                        Actions.moveTo(this.win.getX() + old.width / 2.0F - 50.0F, this.win.getY() + old.height / 2.0F - 50.0F, durationA, interpA)
                     ),
                     Actions.parallel(
                        Actions.fadeIn(durationB),
                        Actions.moveTo(step.x, step.y, durationB, interpB),
                        Actions.sizeTo(step.width, step.height, durationB, interpB)
                     )
                  )
               );
         } else {
            this.win.addAction(Actions.alpha(0.0F));
         }

         if (step.tappable) {
            this.indicatorAction.y = step.y + 30.0F;
            this.indicator.addAction(this.indicatorAction);
            this.indicator
               .addAction(
                  Actions.sequence(
                     Actions.delay(1.0F),
                     Actions.alpha(0.0F),
                     Actions.visible(true),
                     Actions.moveTo(step.x + step.width - 50.0F, this.indicatorAction.y),
                     Actions.fadeIn(0.1F)
                  )
               );
            this.indicator.toFront();
         }
      }
   }

   class IndicatorAction extends Action {
      float y;
      float counter = 0.0F;

      public boolean act(float delta) {
         this.counter += delta;
         if (this.counter >= (float) (Math.PI * 2)) {
            this.counter -= (float) (Math.PI * 2);
         }

         this.getActor().setY(this.y + MathUtils.sin(this.counter * 5.0F) * 3.0F);
         this.getActor().toFront();
         return false;
      }
   }

   public static class Step {
      float x;
      float y;
      float width;
      float height;
      boolean modal;
      boolean tappable;

      public Step(float x, float y, float width, float height, boolean tappable, boolean modal) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height;
         this.tappable = tappable;
         this.modal = modal;
      }

      public void onShow(Window w) {
      }

      public boolean isDone() {
         return false;
      }
   }
}
