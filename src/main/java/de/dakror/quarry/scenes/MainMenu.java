package de.dakror.quarry.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.dakror.common.Callback;
import de.dakror.common.Response;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.DepthSpriter;
import de.dakror.common.libgdx.ui.Scene;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Generator;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.LoadingCompat;
import de.dakror.quarry.ui.Alert;
import de.dakror.quarry.ui.Confirm;
import de.dakror.quarry.ui.Prompt;
import de.dakror.quarry.ui.SeedPrompt;
import de.dakror.quarry.ui.Toast;
import de.dakror.quarry.ui.Ui;
import de.dakror.quarry.ui.VolumeButton;
import de.dakror.quarry.util.SpriterDelegateBatch;
import de.dakror.quarry.util.Util;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Comparator;

public class MainMenu extends Scene implements Ui {
   public static MainMenu M;
   ScrollPane saves;
   VerticalGroup savesList;
   TextButton tutorial;
   Image overlay;
   Image modalOverlay;
   Toast toast;
   Confirm confirm;
   Array<Layer> layers;
   boolean inited;
   boolean newGame;
   SpriteBatch batch;
   FitViewport viewport;
   OrthographicCamera cam;
   OrthographicCamera fboCam;
   Music music;
   public Alert alert;
   public Prompt prompt;
   public SeedPrompt seedPrompt;
   int uResolution;
   int uChannel;
   int uDirection;
   DepthSpriter spriter;
   ShapeRenderer shaper;
   SpriterDelegateBatch pfxBatch;

   @Override
   public void init() {
      M = this;
      this.stage = new Stage(new FitViewport(Const.UI_W, Const.UI_H));
      this.stage.setActionsRequestRendering(false);
      this.music = (Music)Quarry.Q.assets.get("music/Impact Prelude" + Const.MUSIC_FORMAT, Music.class);
      this.shaper = new ShapeRenderer();
      this.shaper.setAutoShapeType(true);
      this.spriter = new DepthSpriter((Texture)Quarry.Q.atlas.getTextures().first(), 8191);
      this.pfxBatch = new SpriterDelegateBatch(this.spriter);
      this.initUI();
   }

   @Override
   public void show(Window w) {
      this.stage.addActor(w);
      w.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.fadeIn(0.15F, Interpolation.fade)));
   }

   @Override
   public void hide(Window w) {
      w.addAction(Actions.sequence(Actions.fadeOut(0.15F, Interpolation.fade), Actions.removeActor()));
   }

   @Override
   public void show() {
      super.show();
      this.tutorial.setVisible(!Quarry.Q.prefs.getBoolean("tutorial", true));
      Quarry.Q.sound.playMusic(this.music, true);
   }

   @Override
   public void dispose() {
      super.dispose();
      ((Layer)this.layers.get(0)).dispose();
      this.music.dispose();
      this.spriter.dispose();
      this.shaper.dispose();
   }

   protected void initUI() {
      this.stage.getActors().clear();
      Table t = Util.lml("main-menu");
      t.setSize(Const.UI_W, Const.UI_H);
      this.stage.addActor(t);
      this.confirm = Util.lml("confirm");
      this.toast = Util.lml("toast");
      this.alert = Util.lml("alert");
      this.prompt = Util.lml("prompt");
      this.seedPrompt = Util.lml("seed-prompt");
      this.stage.addActor(this.toast);
      final ImageButton de = Util.id("lang");
      de.setChecked(Quarry.Q.i18n.getLocale().getLanguage().equals("zh"));
      de.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            MainMenu.this.alert.show(MainMenu.this, Quarry.Q.i18n.get("alert.language_change"), new Callback<Void>() {
               public void call(Void data) {
               }
            });
            Quarry.Q.prefs.putBoolean("english", !de.isChecked()).flush();
         }
      });
      ((VolumeButton)Util.id("sfx")).initListener();
      ((VolumeButton)Util.id("music")).initListener();
      Util.<Actor>id("new_game")
         .addListener(
            new ChangeListener() {
               public void changed(ChangeEvent event, Actor actor) {
                  ((TextButton)actor).setChecked(false);
                  MainMenu.this.seedPrompt
                     .show(
                        MainMenu.this, Quarry.Q.i18n.get("prompt.seed"), Long.toString((long)(Math.random() * 9.223372E18F)), new Response<String, Boolean>() {
                           public Boolean call(String data) {
                              if (data != null) {
                                 long seed = 0L;

                                 try {
                                    seed = Long.parseLong(data.trim());
                                 } catch (NumberFormatException var5) {
                                    seed = Util.hash(data);
                                 }

                                 Generator.G.setSeed(seed);
                                 MainMenu.this.fadeOut = true;
                                 MainMenu.this.newGame = true;
                              }

                              return true;
                           }
                        }
                     );
               }
            }
         );
      Util.<Actor>id("new_game").addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      Util.<Actor>id("credits").addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.addScene(Credits.instance);
            ((Button)actor).setChecked(false);
         }
      });
      Util.<Actor>id("credits").addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.tutorial = Util.id("tutorial");
      this.tutorial.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.prefs.putBoolean("tutorial", true).flush();
            MainMenu.this.fadeOut = true;
            MainMenu.this.newGame = true;
            ((TextButton)actor).setChecked(false);
         }
      });
      this.tutorial.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      final TextButton loadGame = Util.id("load_game");
      loadGame.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      loadGame.addListener(
         new ChangeListener() {
            public void changed(ChangeEvent event, final Actor actor) {
               if (!((TextButton)actor).isChecked()) {
                  MainMenu.this.overlay.addAction(Actions.sequence(Actions.alpha(0.0F, 0.25F, Interpolation.fade), Actions.visible(false)));
                  MainMenu.this.saves
                     .addAction(
                        Actions.sequence(
                           Actions.moveTo(MainMenu.this.saves.getX(), -MainMenu.this.saves.getHeight(), 0.25F, Interpolation.fade), Actions.visible(false)
                        )
                     );
               } else {
                  MainMenu.this.overlay.addAction(Actions.sequence(Actions.visible(true), Actions.alpha(0.4F, 0.25F, Interpolation.fade)));
                  MainMenu.this.saves
                     .addAction(Actions.sequence(Actions.visible(true), Actions.moveTo(MainMenu.this.saves.getX(), -16.0F, 0.25F, Interpolation.fade)));
                  MainMenu.this.savesList.clear();
                  FileHandle[] list = Quarry.Q.listFiles();
                  Arrays.sort(list, new Comparator<FileHandle>() {
                     public int compare(FileHandle o1, FileHandle o2) {
                        long y = o1.lastModified();
                        long x = o2.lastModified();
                        return x < y ? -1 : (x == y ? 0 : 1);
                     }
                  });

                  for (FileHandle fh : list) {
                     final String n = fh.nameWithoutExtension();
                     final Table entry = new Table();
                     final ImageButton del = new ImageButton(Quarry.Q.skin, "delete");
                     del.addListener(new ClickListener() {
                        public void clicked(InputEvent event, float x, float y) {
                           Quarry.Q.sound.play(Quarry.Q.clickSfx);
                           MainMenu.this.confirm.show(MainMenu.this, Quarry.Q.i18n.format("confirm.delete_save", n), new Callback<Boolean>() {
                              public void call(Boolean data) {
                                 if (data != null && data) {
                                    if (Quarry.Q.file("TheQuarry/saves/" + n + ".qsf", false).delete()) {
                                       MainMenu.this.toast.show(Quarry.Q.i18n.get("toast.save_deleted"));
                                       MainMenu.this.savesList.removeActor(entry);
                                    } else {
                                       MainMenu.this.toast.show(Quarry.Q.i18n.get("toast.save_not_deleted"));
                                    }
                                 }
                              }
                           });
                        }
                     });
                     entry.setTouchable(Touchable.enabled);
                     entry.addListener(
                        new ClickListener() {
                           public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                              super.touchDown(event, x, y, pointer, button);
                              if (event.getTarget().isDescendantOf(del)) {
                                 return false;
                              } else {
                                 entry.setBackground(GameUi.trButton);
                                 return true;
                              }
                           }

                           public void clicked(InputEvent event, float x, float y) {
                              Quarry.Q.sound.play(Quarry.Q.clickSfx);
                              MainMenu.this.modalOverlay.toFront();
                              MainMenu.this.modalOverlay.addAction(Actions.sequence(Actions.visible(true), Actions.alpha(0.4F, 0.25F, Interpolation.fade)));
                              MainMenu.this.toast.show(Quarry.Q.i18n.get("toast.loading_game"));
                              Quarry.Q
                                 .threadPool
                                 .execute(
                                    new Runnable() {
                                       @Override
                                       public void run() {
                                          Game.G
                                             .load(
                                                n,
                                                new Callback<Object>() {
                                                   @Override
                                                   public void call(Object data) {
                                                      MainMenu.this.modalOverlay
                                                         .addAction(Actions.sequence(Actions.alpha(0.0F, 0.25F, Interpolation.fade), Actions.visible(false)));
                                                       if (data instanceof Boolean && (Boolean)data) {
                                                          ((TextButton)actor).setChecked(false);
                                                         MainMenu.this.fadeOut = true;
                                                         MainMenu.this.newGame = false;
                                                      } else if (data instanceof Boolean && !(Boolean)data) {
                                                         MainMenu.this.toast.show(Quarry.Q.i18n.get("toast.game_not_loaded"));
                                                      } else if (data instanceof Exception) {
                                                         Quarry.Q.pi.message(1, data);
                                                         MainMenu.this.toast.show(Quarry.Q.i18n.get("toast.game_not_loaded"));
                                                      }
                                                   }
                                                }
                                             );
                                       }
                                    }
                                 );
                           }

                           public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                              super.touchUp(event, x, y, pointer, button);
                              if (!event.getTarget().isDescendantOf(del)) {
                                 entry.setBackground((Drawable)null);
                              }
                           }
                        }
                     );

                     try {
                        NBT.CompoundTag data = Game.G.loadMetaData(n);
                        if (data != null) {
                           Texture t = Game.G.getThumbnail(n);
                           Image i = null;
                           if (t == null) {
                              i = new Image(Quarry.Q.skin.getDrawable("thumb"));
                           } else {
                              i = new Image(t);
                           }

                           if (Quarry.Q.desktop) {
                              entry.add(i).size(178.0F, 100.0F);
                           } else {
                              entry.add(i).size(56.25F, 100.0F);
                           }

                           Table details = new Table();
                           details.defaults().left().top();
                           details.add(new Label(data.String("name", Quarry.Q.i18n.get("menu.no_save_name")), Quarry.Q.skin));
                           details.row();
                           long playtime = data.Long("playTime", 0L) / 1000L;
                           Label l2 = new Label(
                              String.format(
                                 Quarry.Q.i18n.getLocale(),
                                 "%s\n%s - %d:%02d:%02dh %s\nSeed: %d",
                                 n,
                                 GameUi.dateFormat.format(fh.lastModified()),
                                 playtime / 3600L,
                                 playtime % 3600L / 60L,
                                 playtime % 60L,
                                 Quarry.Q.i18n.get("ui.played"),
                                 data.Long("seed", 0L)
                              ),
                              Quarry.Q.skin
                           );
                           l2.setColor(Color.LIGHT_GRAY);
                           l2.setFontScale(0.75F);
                           details.add(l2).padTop(-5.0F);
                           entry.add(details).expandX().left().top().space(10.0F);
                           entry.add(del).right().spaceRight(20.0F);
                           data.free();
                           GameUi.sep(entry).colspan(99).fillX();
                           MainMenu.this.savesList.addActor(entry);
                        }
                     } catch (Exception var18) {
                        Quarry.Q.pi.message(1, var18);
                     }
                  }

                  MainMenu.this.saves.setScrollY(0.0F);
                  MainMenu.this.saves.toFront();
               }
            }
         }
      );
      TextButton selectExternal = Util.id("select_external");
      selectExternal.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      selectExternal.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.pi.message(19, null);
            ((TextButton)event.getListenerActor()).setChecked(false);
         }
      });
      selectExternal.setVisible(Quarry.Q.newAndroid);
      if (Gdx.app.getType() == ApplicationType.iOS) {
         Util.<Actor>id("quit_game").setVisible(false);
      } else {
         Util.<Actor>id("quit_game").addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
               System.exit(0);
            }
         });
         Util.<Actor>id("quit_game").addCaptureListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
               Quarry.Q.sound.play(Quarry.Q.clickSfx);
            }
         });
      }

      Util.lml("saves");
      this.overlay = Util.id("overlay");
      this.overlay.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            loadGame.setChecked(false);
         }
      });
      this.stage.addActor(this.overlay);
      this.saves = Util.id("saves");
      ScrollPaneStyle sps = new ScrollPaneStyle(this.saves.getStyle());
      sps.background = GameUi.getDrawable(Quarry.Q.skin, "panel_metalDark", 24, 24, 24, 24);
      this.saves.setStyle(sps);
      this.savesList = Util.id("saves_cont");
      this.savesList.grow();
      this.savesList.setRound(false);
      this.saves.setVisible(true);
      this.saves.setPosition(-16.0F, -this.saves.getHeight());
      this.stage.addActor(this.saves);
      this.modalOverlay = Util.id("modal_overlay");
      this.stage.addActor(this.modalOverlay);
      this.cam = new OrthographicCamera(Const.W, Const.H);
      this.fboCam = new OrthographicCamera(2048.0F, 2048.0F);
      this.fboCam.position.set(1024.0F, 1024.0F, this.fboCam.near);
      this.fboCam.update();
      Label l = new Label(Quarry.Q.version, Quarry.Q.skin);
      l.setFontScale(0.75F);
      l.setWidth(Const.UI_W);
      l.setAlignment(1);
      l.setX(0.0F);
      l.setY(5.0F);
      this.stage.addActor(l);
      this.batch = new SpriteBatch();

      try {
         NBT.CompoundTag meta = NBT.read(new BufferedInputStream(Gdx.files.internal("main-menu.qmf").read()), NBT.CompressionType.Fast);
         NBT.CompoundTag data = NBT.read(new BufferedInputStream(Gdx.files.internal("main-menu.qsf").read()), NBT.CompressionType.Fast);
         int build = meta.Int("build", 0);
         LoadingCompat.instance.upgrade(data, build);
         this.layers = new Array();
         this.layers.add(Layer.load(0, (NBT.CompoundTag)data.List("Map", NBT.TagType.Compound).data.first()));
         ((Layer)this.layers.get(0)).postAllLayersLoad(true);
         ((Layer)this.layers.get(0)).postAllLayersLoad(false);
         ((Layer)this.layers.get(0)).fake = true;
         data.free();
         meta.free();
         this.cam.position.set((float)(((Layer)this.layers.get(0)).width * 64) * 0.5F, (float)(((Layer)this.layers.get(0)).height * 64) * 0.5F, 0.0F);
         this.cam.zoom = (float)((Layer)this.layers.get(0)).height * 0.7F * 64.0F / Math.max(Const.W, Const.H);
         this.cam.update();
      } catch (Exception var10) {
         Quarry.Q.pi.message(1, var10);
      }
   }

   @Override
   public void draw() {
      Gdx.gl.glClear(16640);
      this.cam.update();
      this.batch.setProjectionMatrix(this.cam.combined);
      this.batch.begin();
      ((Layer)this.layers.get(0)).draw(this.cam, this.fboCam, this.stage.getViewport(), this.batch, this.spriter, this.shaper, this.pfxBatch);
      this.batch.end();
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

   @Override
   public void update(double deltaTime) {
      super.update(deltaTime);
      ((Layer)this.layers.get(0)).update(deltaTime, 1);
      if (this.fadeOut && !Quarry.Q.hasScene(Game.G)) {
         Quarry.Q.addSceneBelow(Game.G);
         if (this.newGame) {
            Game.G.newGame();
         }
      }

      if (this.fadeOut && this.alpha == 0.0F) {
         Quarry.Q.dropScene(this);
      }
   }

   @Override
   public Stage getStage() {
      return this.stage;
   }
}
