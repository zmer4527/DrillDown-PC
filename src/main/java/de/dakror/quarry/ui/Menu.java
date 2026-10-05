package de.dakror.quarry.ui;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ActorGestureListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import de.dakror.common.Callback;
import de.dakror.common.Response;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Generator;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.scenes.MainMenu;
import de.dakror.quarry.util.Util;
import java.util.Arrays;
import java.util.Comparator;

public class Menu {
   public ImageButton menuButton;
   Image menuOverlay;
   Image modalOverlay;
   ScrollPane menuSaves;
   VerticalGroup menuSavesContainer;
   Table menu;
   TextButton newGame;
   TextButton save;
   TextButton saveAs;
   TextButton load;
   TextButton quit;
   TextButton uiMode;
   TextButton export;
   TextButton import_;
   VolumeButton sound;
   VolumeButton music;
   public Label fps;

   public Menu(final Stage stage, final Skin skin) {
      Util.lml("menu");
      this.fps = Util.id("fps");
      this.menuOverlay = Util.id("overlay");
      this.menuOverlay.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Menu.this.menuButton.setChecked(false);
         }
      });
      this.menuOverlay.setName("menuOverlay");
      stage.addActor(this.menuOverlay);
      this.menuOverlay.setPosition(0.0F, 0.0F);
      this.menuOverlay.setHeight(this.menuOverlay.getHeight() + (float)Quarry.Q.safeInsets[1] + (float)Quarry.Q.safeInsets[3]);
      this.menuSavesContainer = Util.id("saves_cont");
      this.menuSavesContainer.grow();
      this.menuSavesContainer.setRound(false);
      this.menuSaves = Util.id("saves");
      ScrollPaneStyle sps = new ScrollPaneStyle(this.menuSaves.getStyle());
      sps.background = GameUi.getDrawable(skin, "panel_metalDark", 24, 24, 24, 24);
      this.menuSaves.setStyle(sps);
      this.menuSaves.setPosition(-this.menuSavesContainer.getWidth(), -20.0F);
      this.menuSaves.setName("menuSaves");
      stage.addActor(this.menuSaves);
      this.menuButton = Util.id("button");
      this.menuButton.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            boolean checked = ((ImageButton)actor).isChecked();
            float amount = Menu.this.menu.getWidth() * (float)(checked ? 1 : -1);
            Menu.this.menuButton.addAction(Actions.moveBy(amount, 0.0F, 0.15F, Interpolation.fade));
            if (checked) {
               Quarry.Q.sound.play(Quarry.Q.clickSfx);
               Menu.this.menuOverlay.addAction(Actions.sequence(Actions.visible(true), Actions.alpha(0.4F, 0.15F, Interpolation.fade)));
               Menu.this.menu.addAction(Actions.sequence(Actions.visible(true), Actions.moveBy(amount, 0.0F, 0.15F, Interpolation.fade)));
            } else {
               Menu.this.menu.addAction(Actions.sequence(Actions.moveBy(amount, 0.0F, 0.15F, Interpolation.fade), Actions.visible(false)));
               Menu.this.menuOverlay.addAction(Actions.sequence(Actions.alpha(0.0F, 0.15F, Interpolation.fade), Actions.visible(false)));
               Menu.this.save.setChecked(false);
               Menu.this.saveAs.setChecked(false);
               Menu.this.load.setChecked(false);
            }

            Menu.this.menu.toFront();
         }
      });
      this.menuButton.setPosition(12.0F, Const.UI_H - this.menuButton.getHeight() - 12.0F);
      this.menuButton.setName("menuButton");
      this.menuButton.addListener(new TextTooltip(Quarry.Q.i18n.get("button.menu"), skin));
      stage.addActor(this.menuButton);
      this.menu = Util.id("menu");
      this.menu.addListener(new ActorGestureListener() {
         public void fling(InputEvent event, float velocityX, float velocityY, int button) {
            if (Math.abs(velocityX / 1000.0F) > 1.0F && Math.abs(velocityY / 1000.0F) < 0.4F) {
               Menu.this.menuButton.setChecked(false);
            }
         }
      });
      this.menu.setPosition(-this.menu.getWidth() - 32.0F + (float)Quarry.Q.safeInsets[0], (float)(-32 + Quarry.Q.safeInsets[3]));
      this.menu.setHeight(this.menu.getHeight() - (float)Quarry.Q.safeInsets[1] - (float)Quarry.Q.safeInsets[3]);
      this.menu.setVisible(false);
      this.menu.setName("menu");
      stage.addActor(this.menu);
      this.newGame = Util.id("new_game");
      this.newGame
         .addListener(
            new ClickListener() {
               public void clicked(InputEvent event, float x, float y) {
                  Quarry.Q.sound.play(Quarry.Q.clickSfx);
                  final Callback<Void> cb = new Callback<Void>() {
                     public void call(Void data1) {
                        Game.G
                           .ui
                           .seedPrompt
                           .show(
                              Game.G.ui,
                              Quarry.Q.i18n.get("prompt.seed"),
                              Long.toString((long)(Math.random() * 9.223372E18F)),
                              new Response<String, Boolean>() {
                                 public Boolean call(String data) {
                                    if (data != null) {
                                       long seed = 0L;

                                       try {
                                          seed = Long.parseLong(data.trim());
                                       } catch (NumberFormatException var5) {
                                          seed = Util.hash(data);
                                       }

                                       Generator.G.setSeed(seed);
                                       Game.G.startNewGame();
                                       Menu.this.menuButton.setChecked(false);
                                       Menu.this.newGame.setChecked(false);
                                    }

                                    return true;
                                 }
                              }
                           );
                     }
                  };
                  Game.G.ui.confirm.show(Game.G.ui, Quarry.Q.i18n.get("confirm.save_game"), new Callback<Boolean>() {
                     public void call(Boolean data) {
                        if (data != null) {
                           if (data) {
                              Menu.this.save(cb);
                           } else {
                              cb.call(null);
                           }
                        }

                        Menu.this.newGame.setChecked(false);
                     }
                  });
               }
            }
         );
      this.save = Util.id("save_game");
      this.save.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Menu.this.save(null);
         }
      });
      this.saveAs = Util.id("save_as");
      this.saveAs.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            final boolean p = Game.G.isPaused();
            Game.G.ui.pauseButton.setChecked(true);
            Game.G.setPaused(true);
            Menu.this.save.setChecked(false);
            Menu.this.load.setChecked(false);
            Menu.this.saveAs(new Callback<String>() {
               public void call(String data) {
                  if (!p) {
                     Game.G.ui.pauseButton.setChecked(false);
                     Game.G.setPaused(false);
                  }
               }
            });
         }
      });
      this.load = Util.id("load_game");
      this.load
         .addListener(
            new ChangeListener() {
               public void changed(ChangeEvent event, Actor actor) {
                  Menu.this.save.setChecked(false);
                  Menu.this.saveAs.setChecked(false);
                  if (!Menu.this.load.isChecked()) {
                     Menu.this.menuSaves
                        .addAction(
                           Actions.sequence(
                              Actions.moveTo(-Menu.this.menuSaves.getWidth(), Menu.this.menuSaves.getY(), 0.15F, Interpolation.fade), Actions.visible(false)
                           )
                        );
                  } else {
                     Quarry.Q.sound.play(Quarry.Q.clickSfx);
                     Menu.this.menuSavesContainer.clear();
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
                        final ImageButton del = new ImageButton(skin, "delete");
                        del.addListener(new ClickListener() {
                           public void clicked(InputEvent event, float x, float y) {
                              Quarry.Q.sound.play(Quarry.Q.clickSfx);
                              Game.G.ui.confirm.show(Game.G.ui, Quarry.Q.i18n.format("confirm.delete_save", n), new Callback<Boolean>() {
                                 public void call(Boolean data) {
                                    if (data != null && data) {
                                       if (Quarry.Q.file("TheQuarry/saves/" + n + ".qsf", false).delete()) {
                                          Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.save_deleted"));
                                          Menu.this.menuSavesContainer.removeActor(entry);
                                       } else {
                                          Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.save_not_deleted"));
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
                                 Menu.this.modalOverlay.toFront();
                                 Menu.this.modalOverlay.addAction(Actions.sequence(Actions.visible(true), Actions.alpha(0.4F, 0.25F, Interpolation.fade)));
                                 Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.loading_game"));
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
                                                         Menu.this.modalOverlay
                                                            .addAction(Actions.sequence(Actions.alpha(0.0F, 0.25F, Interpolation.fade), Actions.visible(false)));
                                                         if (data instanceof Boolean && (Boolean)data) {
                                                            stage.unfocusAll();
                                                            Game.G.resetActiveStructure();
                                                            Game.G.ui.hideTooltip();
                                                            Game.G.ui.destroyButton.setChecked(false);
                                                            Game.G.ui.hideBuildMenu();
                                                            Menu.this.load.setChecked(false);
                                                            Menu.this.menuButton.setChecked(false);
                                                            Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.game_loaded"));
                                                         } else if (data instanceof Boolean && !(Boolean)data) {
                                                            Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.game_not_loaded"));
                                                         } else if (data instanceof Exception) {
                                                            Quarry.Q.pi.message(1, data);
                                                            Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.game_not_loaded"));
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
                           if (data == null) {
                              continue;
                           }

                           Texture t = Game.G.getThumbnail(n);
                           Image i = null;
                           if (t == null) {
                              i = new Image(skin.getDrawable("thumb"));
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
                           details.add(new Label(data.String("name", Quarry.Q.i18n.get("menu.no_save_name")), skin));
                           details.row();
                           long playtime = data.Long("playTime", 0L) / 1000L;
                           Label l2 = new Label(
                              String.format(
                                 "%s\n%s - %d:%02d:%02dh %s\nSeed: %d",
                                 n,
                                 GameUi.dateFormat.format(fh.lastModified()),
                                 playtime / 3600L,
                                 playtime % 3600L / 60L,
                                 playtime % 60L,
                                 Quarry.Q.i18n.get("ui.played"),
                                 data.Long("seed", 0L)
                              ),
                              skin
                           );
                           l2.setColor(Color.LIGHT_GRAY);
                           l2.setFontScale(0.75F);
                           details.add(l2).padTop(-5.0F);
                           entry.add(details).expandX().left().top().space(10.0F);
                           entry.add(del).right().spaceRight(20.0F);
                           data.free();
                        } catch (Exception var18) {
                           Quarry.Q.pi.message(1, var18);
                        }

                        GameUi.sep(entry).colspan(99).fillX();
                        Menu.this.menuSavesContainer.addActor(entry);
                     }

                     Menu.this.menuSaves.setScrollY(0.0F);
                     Menu.this.menuSaves.toFront();
                     Menu.this.menuSaves
                        .addAction(Actions.sequence(Actions.visible(true), Actions.moveTo(-20.0F, Menu.this.menuSaves.getY(), 0.15F, Interpolation.fade)));
                  }
               }
            }
         );
      final TextButton menu = Util.id("main_menu");
      menu.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            menu.setChecked(false);
            Game.G.ui.confirm.show(Game.G.ui, Quarry.Q.i18n.get("confirm.save_game"), new Callback<Boolean>() {
               public void call(Boolean data) {
                  if (data != null) {
                     if (data) {
                        Menu.this.save(new Callback<Void>() {
                           public void call(Void data1) {
                              Menu.this.menuButton.setChecked(false);
                              de.dakror.quarry.net.NetSession.disconnect();
                              Game.G.reset();
                              Quarry.Q.addScene(MainMenu.M);
                              Quarry.Q.dropScene(Game.G);
                           }
                        });
                     } else {
                        Menu.this.menuButton.setChecked(false);
                        de.dakror.quarry.net.NetSession.disconnect();
                        Game.G.reset();
                        Quarry.Q.addScene(MainMenu.M);
                        Quarry.Q.dropScene(Game.G);
                     }
                  }
               }
            });
         }
      });
      final ImageButton de = Util.id("lang");
      de.setChecked(Quarry.Q.i18n.getLocale().getLanguage().equals("zh"));
      de.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.ui.alert.show(Game.G.ui, Quarry.Q.i18n.get("alert.language_change"), new Callback<Void>() {
               public void call(Void data) {
               }
            });
            Quarry.Q.prefs.putBoolean("english", !de.isChecked()).flush();
         }
      });
      this.sound = Util.id("sfx");
      this.sound.initListener();
      this.music = Util.id("music");
      this.music.initListener();
      this.export = Util.id("export");
      this.export.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            ((TextButton)event.getListenerActor()).setChecked(false);
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (Game.G.pasteMode) {
               Game.G.exportBlueprint();
               Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.selection_copied"));
            } else {
               Game.G.ui.toast.show(Quarry.Q.i18n.get("toast.no_selection"));
            }
         }
      });
      this.export.setVisible(false);
      this.import_ = Util.id("import");
      this.import_.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            ((TextButton)event.getListenerActor()).setChecked(false);
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Menu.this.menuButton.setChecked(false);
            Game.G.importBlueprint();
         }
      });
      this.import_.setVisible(false);
      TextButton recording = Util.id("recording");
      recording.setVisible(true);
      recording.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.RECORDMODE = !Game.RECORDMODE;
         }
      });
      this.modalOverlay = Util.id("modal_overlay");
      stage.addActor(this.modalOverlay);
      this.modalOverlay.setPosition(0.0F, 0.0F);
      this.modalOverlay.setHeight(this.modalOverlay.getHeight() + (float)Quarry.Q.safeInsets[1] + (float)Quarry.Q.safeInsets[3]);
   }

   public void init() {
      this.sound.setChecked(!Quarry.Q.sound.isPlaySound());
      this.music.setChecked(!Quarry.Q.sound.isPlayMusic());
   }

   public void onScienceChange() {
      this.export.setVisible(Game.G.hasScience(Science.ScienceType.Blueprints));
      this.import_.setVisible(Game.G.hasScience(Science.ScienceType.Blueprints));
   }

   public void save(final Callback<Void> o) {
      final boolean p = Game.G.isPaused();
      Game.G.setPaused(true);
      Game.G.ui.pauseButton.setChecked(true);
      this.load.setChecked(false);
      this.saveAs.setChecked(false);
      if (Game.G.currentGameName == null) {
         this.saveAs(new Callback<String>() {
            public void call(String data) {
               Game.G.currentGameName = data;
               Menu.this.save.setChecked(false);
               if (!p) {
                  Game.G.setPaused(false);
                  Game.G.ui.pauseButton.setChecked(false);
               }

               if (o != null) {
                  o.call(null);
               }
            }
         });
      } else {
         Game.G.save(o);
         this.save.setChecked(false);
         if (!p) {
            Game.G.ui.pauseButton.setChecked(false);
            Game.G.setPaused(false);
         }
      }
   }

   public void saveAs(final Callback<String> o) {
      Game.G.ui.prompt.show(Game.G.ui, Quarry.Q.i18n.get("prompt.save_name"), null, new Response<String, Boolean>() {
         public Boolean call(final String data) {
            if (data != null) {
               if (Game.G.saveExists(data)) {
                  Game.G.ui.confirm.show(Game.G.ui, Quarry.Q.i18n.get("confirm.save_exists"), new Callback<Boolean>() {
                     public void call(Boolean d) {
                        if (d != null && d) {
                           Menu.this.save.setChecked(false);
                           Menu.this.saveAs.setChecked(false);
                           Game.G.save(data, true, new Callback<Void>() {
                              public void call(Void x) {
                                 o.call(data);
                              }
                           });
                           Game.G.ui.hide(Game.G.ui.prompt);
                        }
                     }
                  });
                  return false;
               }

               Menu.this.save.setChecked(false);
               Menu.this.saveAs.setChecked(false);
               Game.G.save(data, true, new Callback<Void>() {
                  public void call(Void x) {
                     o.call(data);
                  }
               });
            }

            Menu.this.save.setChecked(false);
            Menu.this.saveAs.setChecked(false);
            return true;
         }
      });
   }
}
