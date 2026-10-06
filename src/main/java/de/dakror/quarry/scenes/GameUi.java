package de.dakror.quarry.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TooltipManager;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.dakror.common.BiCallback;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.ConstantSupplyAmount;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.structure.Boiler;
import de.dakror.quarry.structure.Booster;
import de.dakror.quarry.structure.DistillationColumn;
import de.dakror.quarry.structure.ItemDestroyer;
import de.dakror.quarry.structure.Refinery;
import de.dakror.quarry.structure.ScienceLab;
import de.dakror.quarry.structure.ShaftDrill;
import de.dakror.quarry.structure.ShaftDrillHead;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.GeneratorStructure;
import de.dakror.quarry.structure.base.IFlippable;
import de.dakror.quarry.structure.base.IRotatable;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CRecipeSlotStorage;
import de.dakror.quarry.structure.base.component.CTank;
import de.dakror.quarry.structure.base.component.Component;
import de.dakror.quarry.structure.base.component.IStorage;
import de.dakror.quarry.structure.logistics.BrickChannel;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ConveyorBridge;
import de.dakror.quarry.structure.logistics.CopperTube;
import de.dakror.quarry.structure.logistics.Distributor;
import de.dakror.quarry.structure.logistics.ElectricConveyor;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.structure.logistics.Filter;
import de.dakror.quarry.structure.logistics.Hopper;
import de.dakror.quarry.structure.logistics.ItemLift;
import de.dakror.quarry.structure.logistics.SteelTube;
import de.dakror.quarry.structure.logistics.TubeShaft;
import de.dakror.quarry.structure.logistics.VacuumPump;
import de.dakror.quarry.structure.logistics.Valve;
import de.dakror.quarry.structure.power.AnchorPortal;
import de.dakror.quarry.structure.power.CableShaft;
import de.dakror.quarry.structure.power.Capacitor;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.structure.power.GasTurbine;
import de.dakror.quarry.structure.power.HighPowerShaft;
import de.dakror.quarry.structure.power.PowerPole;
import de.dakror.quarry.structure.power.SolarPanel;
import de.dakror.quarry.structure.power.SolarPanelOutlet;
import de.dakror.quarry.structure.power.SteamTurbine;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.structure.power.SuperCapacitor;
import de.dakror.quarry.structure.power.WaterWheel;
import de.dakror.quarry.structure.producer.AirPurifier;
import de.dakror.quarry.structure.producer.ArcWelder;
import de.dakror.quarry.structure.producer.Assembler;
import de.dakror.quarry.structure.producer.BallMill;
import de.dakror.quarry.structure.producer.BarrelDrainer;
import de.dakror.quarry.structure.producer.BlastFurnace;
import de.dakror.quarry.structure.producer.Carpenter;
import de.dakror.quarry.structure.producer.Centrifuge;
import de.dakror.quarry.structure.producer.CharcoalMound;
import de.dakror.quarry.structure.producer.Compactor;
import de.dakror.quarry.structure.producer.Condenser;
import de.dakror.quarry.structure.producer.Crucible;
import de.dakror.quarry.structure.producer.DeviceFabricator;
import de.dakror.quarry.structure.producer.Excavator;
import de.dakror.quarry.structure.producer.FillingMachine;
import de.dakror.quarry.structure.producer.Furnace;
import de.dakror.quarry.structure.producer.GroundwaterPump;
import de.dakror.quarry.structure.producer.InductionFurnace;
import de.dakror.quarry.structure.producer.IngotMold;
import de.dakror.quarry.structure.producer.InjectionMolder;
import de.dakror.quarry.structure.producer.Kiln;
import de.dakror.quarry.structure.producer.Lumberjack;
import de.dakror.quarry.structure.producer.Mason;
import de.dakror.quarry.structure.producer.Mine;
import de.dakror.quarry.structure.producer.Mixer;
import de.dakror.quarry.structure.producer.OilWell;
import de.dakror.quarry.structure.producer.Polarizer;
import de.dakror.quarry.structure.producer.Polymerizer;
import de.dakror.quarry.structure.producer.RockCrusher;
import de.dakror.quarry.structure.producer.RollingMachine;
import de.dakror.quarry.structure.producer.SawMill;
import de.dakror.quarry.structure.producer.Stacker;
import de.dakror.quarry.structure.producer.TubeBender;
import de.dakror.quarry.structure.producer.WireDrawer;
import de.dakror.quarry.structure.storage.Barrel;
import de.dakror.quarry.structure.storage.DigitalStorage;
import de.dakror.quarry.structure.storage.Silo;
import de.dakror.quarry.structure.storage.Storage;
import de.dakror.quarry.structure.storage.Tank;
import de.dakror.quarry.structure.storage.Warehouse;
import de.dakror.quarry.ui.Alert;
import de.dakror.quarry.ui.BoilerRecipe;
import de.dakror.quarry.ui.Confirm;
import de.dakror.quarry.ui.DistillationRecipe;
import de.dakror.quarry.ui.EndOfGame;
import de.dakror.quarry.ui.ItemSelection;
import de.dakror.quarry.ui.LayerSelection;
import de.dakror.quarry.ui.Menu;
import de.dakror.quarry.ui.Prompt;
import de.dakror.quarry.ui.RefineryRecipe;
import de.dakror.quarry.ui.SeedPrompt;
import de.dakror.quarry.ui.TileUI;
import de.dakror.quarry.ui.Toast;
import de.dakror.quarry.ui.Tutorial;
import de.dakror.quarry.ui.Ui;
import de.dakror.quarry.ui.Upgrade;
import de.dakror.quarry.util.Util;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class GameUi implements Ui {
   public static final int ICONS_PER_COLUMN = 3;
   public static final int TOOLTIP_ICON_SIZE = 32;
   public static final int RESOURCES_ICON_SIZE = 24;
   static DecimalFormat intFormat;
   static DecimalFormat decFormat;
   public static DateFormat dateFormat;
   Stage stage;
   Label tooltipDescription;
   Table tooltipCosts;
   Table tooltipRecipes;
   Table tooltip;
   ImageButton tooltipClose;
   ImageButton tooltipCollapse;
   public Structure<?> tooltipCurrentStructure;
   public RecipeList.Recipe currentRecipe;
   public boolean canAffordStructure;
   VerticalGroup[] buildCategories;
   ButtonGroup<Button> buildTabButtons;
   ButtonGroup<Button>[] buildCategoryButtons;
   ScrollPane buildScrollPane;
   VerticalGroup buildTabs;
   ButtonStyle menuButtonStyle;
   ImageButton buildClose;
   ImageButton buildBack;
   Array<Actor> buildMenuStars = new Array();
   public EnumSet<Science.ScienceType> buildMenuSciences = EnumSet.noneOf(Science.ScienceType.class);
   VerticalGroup scienceMenu;
   ScrollPane scienceScrollPane;
   EnumMap<Science.ScienceType, Button> scienceButtons;
   ButtonGroup<Button> scienceGroup;
   ImageButton scienceClose;
   ImageButton buildButton;
   ImageButton bulkButton;
   ImageButton bulkCableButton;
   ImageButton flipButton;
   public ImageButton destroyButton;
   ImageButton cableRemoveButton;
   public ImageButton pauseButton;
   ImageButton copyButton;
   VerticalGroup resources;
   Table[] resourceRows;
   Table structureUI;
   Label structureUITitle;
   Table structureUIContent;
   ImageButton structureUIStorage;
   ImageButton structureUIRecipes;
   public Structure<?> currentClickedStructure;
   ImageButton[] structureUIButtons;
   BiCallback<Boolean, Structure<?>>[] structureUIButtonCallbacks;
   VerticalGroup structureUIInventory;
   ScrollPane structureUIInventoryScrollPane;
   EnumMap<Item.ItemType, Integer> structureUIInventorySum;
   EnumMap<Item.ItemType, Table> structureUIInventoryCells;
   Table copyTable;
   Table copyCosts;
   public Alert alert;
   public Toast toast;
   public Confirm confirm;
   public Upgrade upgrade;
   public Prompt prompt;
   public SeedPrompt seedPrompt;
   public ItemSelection itemSelection;
   LayerSelection layerSelection;
   TileUI tileUI;
   public Menu menu;
   BoilerRecipe boilerRecipe;
   RefineryRecipe refRecipe;
   public DistillationRecipe distRecipe;
   public EndOfGame endOfGame;
   public Tutorial tutorial;
   public Color c = Color.WHITE;
   static final Color off = new Color(0.0F, 0.0F, 0.0F, 0.25F);
   static final Vector2 tmp = new Vector2();
   float h;
   float s;
   float v;
   public static final Drawable trButton = Quarry.Q.skin.newDrawable("button", 1.0F, 1.0F, 1.0F, 0.25F);
   boolean wantToUpdateResources;
   boolean debug = false;
   private static HashMap<String, NinePatchDrawable> drawableCache = new HashMap<>();

   public GameUi(Viewport viewport) {
      Batch batch = null;
      Batch var4 = new SpriteBatch();
      this.stage = new Stage(new FitViewport(Const.UI_W, Const.UI_H), var4) {
         public void draw() {
            if (Game.UI_VISIBLE) {
               try {
                  super.draw();
               } catch (Exception var2) {
                  var2.printStackTrace();
               }
            }
         }

         public void addActor(Actor actor) {
            if (actor.getName() != null && !actor.getName().equals("menu")) {
               GameUi.this.clampToInsets(actor);
            }

            super.addActor(actor);
         }
      };
      this.stage.setActionsRequestRendering(true);
      this.stage.addCaptureListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            TooltipManager.getInstance().hideAll();
            if (GameUi.this.scienceScrollPane.isVisible() && GameUi.this.stage.hit(x, y, true) == null) {
               GameUi.this.hideScienceUI();
               return true;
            } else {
               return false;
            }
         }
      });
      Game.G.input.addProcessor(0, this.stage);
      intFormat = (DecimalFormat)NumberFormat.getNumberInstance(Quarry.Q.i18n.getLocale());
      intFormat.setGroupingUsed(false);
      decFormat = (DecimalFormat)NumberFormat.getNumberInstance(Quarry.Q.i18n.getLocale());
      decFormat.setGroupingUsed(false);
      decFormat.setMinimumFractionDigits(3);
      decFormat.setMaximumFractionDigits(3);
      dateFormat = DateFormat.getDateTimeInstance(3, 3, Quarry.Q.i18n.getLocale());
      Skin skin = Quarry.Q.skin;
      this.initButtons(skin);
      this.initResources(skin);
      this.initBuildMenu(skin);
      this.initScienceUI(skin);
      this.initStructureUI(skin);
      this.initTooltip(skin);
      this.initCopyTable(skin);
      this.menu = new Menu(this.stage, skin);
      this.boilerRecipe = new BoilerRecipe(skin);
      this.refRecipe = new RefineryRecipe(skin);
      this.distRecipe = new DistillationRecipe(skin);
      this.initWindows(skin);
      this.tutorial = new Tutorial(this.stage, skin);
      this.endOfGame = new EndOfGame(this.stage);
   }

   public void clampToInsets(Actor actor) {
      if (actor.getX() < (float)Quarry.Q.safeInsets[0]) {
         actor.setX((float)Quarry.Q.safeInsets[0]);
      }

      if (actor.getY() < (float)Quarry.Q.safeInsets[3]) {
         actor.setY((float)Quarry.Q.safeInsets[3]);
      }

      if (actor.getY() + actor.getHeight() >= Const.UI_H - (float)Quarry.Q.safeInsets[1]) {
         actor.setY(Const.UI_H - (float)Quarry.Q.safeInsets[1] - actor.getHeight());
      }

      if (actor.getX() + actor.getWidth() >= Const.UI_W - (float)Quarry.Q.safeInsets[2]) {
         actor.setX(Const.UI_W - (float)Quarry.Q.safeInsets[2] - actor.getWidth());
      }
   }

   protected void initButtons(Skin skin) {
      this.layerSelection = Util.lml("layer-selection");
      this.layerSelection.addListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
      });
      Game.G.layerChangeNotifier.addListener(this.layerSelection);
      this.layerSelection.setName("layerSelection");
      this.stage.addActor(this.layerSelection);
      this.layerSelection.setX(this.layerSelection.getX() + 40.0F);
      Util.lml("buttons");
      this.flipButton = Util.id("flip");
      this.flipButton.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            GameUi.this.flipActiveStructure();
         }
      });
      this.flipButton.getImageCell().size(64.0F);
      this.flipButton.setPosition(Const.UI_W / 2.0F + this.flipButton.getWidth() / 4.0F, 500.0F);
      this.flipButton.setName("flipButton");
      this.flipButton.addListener(new TextTooltip(Quarry.Q.i18n.get("button.flip"), skin));
      this.stage.addActor(this.flipButton);
      this.buildButton = this.roundButton(skin, skin.getDrawable("icon_build"), new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (((ImageButton)event.getListenerActor()).isChecked()) {
               GameUi.this.destroyButton.setChecked(false);
               GameUi.this.copyButton.setChecked(false);
               GameUi.this.showBuildMenu();
               GameUi.this.hideTooltip();
               GameUi.this.hideCopyTable();
            } else {
               Game.G.resetActiveStructure();
               GameUi.this.hideTooltip();
               GameUi.this.hideBuildMenu();
            }
         }
      }, "button.build");
      this.buildButton.setPosition(20.0F, 260.0F);
      this.buildButton.setName("buildButton");
      this.stage.addActor(this.buildButton);
      this.cableRemoveButton = this.roundButton(skin, skin.getDrawable("icon_cableremove"), new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Game.G.resetActiveStructure();
            GameUi.this.hideTooltip();
            if (((ImageButton)actor).isChecked()) {
               GameUi.this.bulkButton.setChecked(false);
               GameUi.this.bulkCableButton.setChecked(false);
            }

            Game.G.cableDestroyMode = ((ImageButton)actor).isChecked();
            Game.G.structureDestroyMode = !((ImageButton)actor).isChecked();
         }
      }, "button.cable");
      this.cableRemoveButton.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.cableRemoveButton.setVisible(false);
      this.cableRemoveButton.setName("cableRemoveButton");
      this.stage.addActor(this.cableRemoveButton);
      this.bulkCableButton = this.roundButton(skin, skin.getDrawable("icon_cableremovebulk"), new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Game.G.resetActiveStructure();
            GameUi.this.hideTooltip();
            if (((ImageButton)actor).isChecked()) {
               GameUi.this.bulkButton.setChecked(false);
               GameUi.this.cableRemoveButton.setChecked(false);
            }

            Game.G.bulkCableMode = ((ImageButton)actor).isChecked();
            Game.G.structureDestroyMode = !((ImageButton)actor).isChecked();
         }
      }, "button.cablebulk");
      this.bulkCableButton.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.bulkCableButton.setVisible(false);
      this.bulkCableButton.setName("bulkCableButton");
      this.stage.addActor(this.bulkCableButton);
      this.destroyButton = this.roundButton(
         skin,
         skin.getDrawable("icon_destroy"),
         new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
               if (GameUi.this.destroyButton.isChecked()) {
                  if (Game.G.hasScience(Science.ScienceType.Electricity) || Game.GOD_MODE) {
                     GameUi.this.cableRemoveButton
                        .addAction(
                           Actions.sequence(
                              Actions.alpha(0.0F),
                              Actions.visible(true),
                              Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY()),
                              Actions.parallel(Actions.fadeIn(0.1F), Actions.moveBy(240.0F, 0.0F, 0.15F, Interpolation.swingOut))
                           )
                        );
                     GameUi.this.bulkCableButton
                        .addAction(
                           Actions.sequence(
                              Actions.alpha(0.0F),
                              Actions.visible(true),
                              Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY()),
                              Actions.parallel(Actions.fadeIn(0.1F), Actions.moveBy(360.0F, 0.0F, 0.15F, Interpolation.swingOut))
                           )
                        );
                  }

                  GameUi.this.bulkButton
                     .addAction(
                        Actions.sequence(
                           Actions.alpha(0.0F),
                           Actions.visible(true),
                           Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY()),
                           Actions.parallel(Actions.fadeIn(0.1F), Actions.moveBy(120.0F, 0.0F, 0.15F, Interpolation.swingOut))
                        )
                     );
                  GameUi.this.hideStructureUI();
                  GameUi.this.hideTooltip();
                  GameUi.this.hideCopyTable();
                  GameUi.this.tileUI.hide();
                  GameUi.this.copyButton.setChecked(false);
                  GameUi.this.hideBuildMenu();
               } else {
                  if (Game.G.hasScience(Science.ScienceType.Electricity) || Game.GOD_MODE) {
                     GameUi.this.cableRemoveButton
                        .addAction(
                           Actions.sequence(
                              Actions.parallel(
                                 Actions.fadeOut(0.1F),
                                 Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY(), 0.15F, Interpolation.swingOut)
                              ),
                              Actions.visible(false)
                           )
                        );
                     GameUi.this.cableRemoveButton.setChecked(false);
                     GameUi.this.bulkCableButton
                        .addAction(
                           Actions.sequence(
                              Actions.parallel(
                                 Actions.fadeOut(0.1F),
                                 Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY(), 0.15F, Interpolation.swingOut)
                              ),
                              Actions.visible(false)
                           )
                        );
                     GameUi.this.bulkCableButton.setChecked(false);
                  }

                  GameUi.this.bulkButton
                     .addAction(
                        Actions.sequence(
                           Actions.parallel(
                              Actions.fadeOut(0.1F),
                              Actions.moveTo(GameUi.this.destroyButton.getX(), GameUi.this.destroyButton.getY(), 0.15F, Interpolation.swingOut)
                           ),
                           Actions.visible(false)
                        )
                     );
                  GameUi.this.bulkButton.setChecked(false);
               }

               Game.G.resetActiveStructure();
               Game.G.structureDestroyMode = GameUi.this.destroyButton.isChecked();
            }
         },
         "button.destroy"
      );
      this.destroyButton.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.destroyButton.setPosition(20.0F, 140.0F);
      this.bulkButton = this.roundButton(skin, skin.getDrawable("icon_destroybulk"), new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Game.G.resetActiveStructure();
            GameUi.this.hideTooltip();
            GameUi.this.hideCopyTable();
            if (((ImageButton)actor).isChecked()) {
               GameUi.this.bulkCableButton.setChecked(false);
               GameUi.this.cableRemoveButton.setChecked(false);
            }

            Game.G.bulkDestroyMode = ((ImageButton)actor).isChecked();
            Game.G.structureDestroyMode = !((ImageButton)actor).isChecked();
         }
      }, "button.destroybulk");
      this.bulkButton.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.bulkButton.setVisible(false);
      this.bulkButton.setName("bulkButton");
      this.stage.addActor(this.bulkButton);
      this.destroyButton.setName("destroyButton");
      this.stage.addActor(this.destroyButton);
      this.copyButton = this.roundButton(skin, skin.getDrawable("icon_copy"), new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (GameUi.this.copyButton.isChecked()) {
               GameUi.this.destroyButton.setChecked(false);
               GameUi.this.buildButton.setChecked(false);
               GameUi.this.hideBuildMenu();
               GameUi.this.hideTooltip();
               GameUi.this.showCopyTable();
               Game.G.resetActiveStructure();
            } else {
               Game.G.pasteMode = false;
               Arrays.fill(Game.G.copyRegion, 0);
               Game.G.copyCost.clear();
               GameUi.this.hideCopyTable();
            }

            Game.G.copyMode = GameUi.this.copyButton.isChecked();
         }
      }, "button.copy");
      this.copyButton.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.copyButton.setPosition(20.0F, 20.0F);
      this.copyButton.setName("copyButton");
      this.copyButton.setVisible(false);
      this.stage.addActor(this.copyButton);
      this.pauseButton = this.roundButton(skin, skin.getDrawable("symb_pause"), new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.resetSpeed();
            Game.G.setPaused(GameUi.this.pauseButton.isChecked());
         }
      }, "button.pause");
      ImageButtonStyle style = this.pauseButton.getStyle();
      style.imageChecked = skin.getDrawable("symb_play");
      this.pauseButton.setStyle(style);
      this.pauseButton.getImageCell().size(36.0F);
      this.pauseButton.setPosition(124.0F, Const.UI_H - 112.0F);
      this.pauseButton.setName("pauseButton");
      this.stage.addActor(this.pauseButton);
   }

   public void rotateActiveStructure() {
      if (this.tooltipCurrentStructure != null) {
         if (!(this.tooltipCurrentStructure instanceof IRotatable)) {
            if (!this.tooltipCurrentStructure.getSchema().has(Schema.Flags.NotRotatable)) {
               this.tooltipCurrentStructure.setUpDirection(this.tooltipCurrentStructure.getUpDirection().next());
            }
         } else if (!Game.G.activeStructureTrail.isEmpty() && Game.G.endB.x > -1.0F) {
            int tx = (int)Game.G.endA.x;
            int ty = (int)Game.G.endA.y;
            Game.G.endA.set(Game.G.endB);
            Game.G.endB.set((float)tx, (float)ty);
            Structure<?> last = null;
            Collections.reverse(Game.G.activeStructurePath);

            for (int i : Game.G.activeStructurePath) {
               Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get(i);
               if (last != null) {
                  Direction rotation = null;
                  if (s.x == last.x) {
                     rotation = s.y > last.y ? Direction.South : Direction.North;
                  } else {
                     rotation = s.x > last.x ? Direction.West : Direction.East;
                  }

                  ((IRotatable)s).setRotation(rotation);
               } else {
                  ((IRotatable)s).setRotation(((IRotatable)s).getDirection().inv());
               }

               last = s;
            }

            for (int i : Game.G.activeStructurePath) {
               Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get(i);
               s.update(0.0, 1, Game.G.layer.dirtyBounds);
            }

            Game.G.camControl.updateTrail();
         } else {
            ((IRotatable)this.tooltipCurrentStructure).rotate();
         }

         Game.G.camControl.updateActiveElementPlaceable();
      } else if (this.currentClickedStructure instanceof IRotatable) {
         ((IRotatable)this.currentClickedStructure).rotate();
      } else if (Game.G.activeStructure != null && !Game.G.activeStructure.getSchema().has(Schema.Flags.NotRotatable)) {
         Game.G.activeStructure.setUpDirection(Game.G.activeStructure.getUpDirection().next());
         Game.G.camControl.updateActiveElementPlaceable();
      }
   }

   public void flipActiveStructure() {
      if (this.tooltipCurrentStructure instanceof IFlippable) {
         ((IFlippable)this.tooltipCurrentStructure).flip();
         Game.G.camControl.updateActiveElementPlaceable();

         for (Structure<?> s : Game.G.activeStructureTrail.values()) {
            ((IFlippable)s).flip();
         }
      } else if (this.currentClickedStructure instanceof IFlippable) {
         ((IFlippable)this.currentClickedStructure).flip();
      }
   }

   protected void initTooltip(Skin skin) {
      TooltipManager man = TooltipManager.getInstance();
      man.initialTime = 0.0F;
      man.animations = false;
      man.resetTime = 0.0F;
      man.instant();
      this.tooltip = Util.lml("tooltip");
      this.tooltip.setPosition(Const.UI_W - this.tooltip.getWidth() - 20.0F, 20.0F);
      this.tooltip.addListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
      });
      this.tooltipCosts = Util.id("costs");
      this.tooltipRecipes = Util.id("recipes");
      this.tooltipDescription = Util.id("desc_label");
      Table descTable = Util.id("desc");
      descTable.setBackground(getDrawable(skin, "panel_metal", 0, 0, 0, 0));
      this.tooltip.setName("tooltip");
      this.stage.addActor(this.tooltip);
      this.tooltipClose = this.createXButton();
      this.tooltipClose.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.resetActiveStructure();
            GameUi.this.hideTooltip();
         }
      });
      this.tooltipClose.setPosition(this.tooltip.getX() + this.tooltip.getWidth() - 40.0F, this.tooltip.getY() + this.tooltip.getHeight() - 40.0F);
      this.tooltipClose.setName("tooltipClose");
      this.stage.addActor(this.tooltipClose);
      this.tooltipCollapse = new ImageButton(skin, "collapse");
      this.tooltipCollapse.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            GameUi.this.tooltip.clearActions();
            GameUi.this.tooltipClose.clearActions();
            GameUi.this.tooltipCollapse.clearActions();
            float delta = GameUi.this.tooltip.getHeight() - 60.0F;
            if (GameUi.this.tooltipCollapse.isChecked()) {
               GameUi.this.tooltip.addAction(Actions.sequence(Actions.moveBy(0.0F, -delta, 0.15F, Interpolation.circle)));
               GameUi.this.tooltipClose.addAction(Actions.sequence(Actions.moveBy(0.0F, -delta, 0.15F, Interpolation.circle)));
               GameUi.this.tooltipCollapse.addAction(Actions.sequence(Actions.moveBy(0.0F, -delta, 0.15F, Interpolation.circle)));
               GameUi.this.flipButton.addAction(Actions.sequence(Actions.moveBy(0.0F, -delta, 0.15F, Interpolation.circle)));
            } else {
               GameUi.this.tooltip.addAction(Actions.sequence(Actions.moveBy(0.0F, delta, 0.15F, Interpolation.circle)));
               GameUi.this.tooltipClose.addAction(Actions.sequence(Actions.moveBy(0.0F, delta, 0.15F, Interpolation.circle)));
               GameUi.this.tooltipCollapse.addAction(Actions.sequence(Actions.moveBy(0.0F, delta, 0.15F, Interpolation.circle)));
               GameUi.this.flipButton.addAction(Actions.sequence(Actions.moveBy(0.0F, delta, 0.15F, Interpolation.circle)));
            }
         }
      });
      this.tooltipCollapse.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      this.tooltipCollapse.setPosition(this.tooltip.getX() + 36.0F, this.tooltip.getY() + this.tooltip.getHeight() - 26.0F);
      this.tooltipCollapse.setName("tooltipCollapse");
      this.stage.addActor(this.tooltipCollapse);
   }

   protected void initBuildMenu(Skin skin) {
      this.menuButtonStyle = new ButtonStyle(null, trButton, trButton);
      this.buildTabs = new VerticalGroup();
      this.buildTabs.expand().fill().top().left();
      this.buildTabButtons = new ButtonGroup();
      int tube = this.buildMenuTab(Conveyor.classSchema.icon, Quarry.Q.i18n.get("tab.conveyors"));
      int rout = this.buildMenuTab(Filter.classSchema.icon, Quarry.Q.i18n.get("tab.routers"));
      int stor = this.buildMenuTab(Storage.classSchema.icon, Quarry.Q.i18n.get("tab.storage"));
      int mine = this.buildMenuTab(Mine.classSchema.icon, Quarry.Q.i18n.get("tab.raw"));
      int furn = this.buildMenuTab(Furnace.classSchema.icon, Quarry.Q.i18n.get("tab.ore"));
      int iron = this.buildMenuTab(skin.getDrawable("icon_fe_ingot"), Quarry.Q.i18n.get("tab.metal"));
      int misc = this.buildMenuTab(skin.getDrawable("icon_misc"), Quarry.Q.i18n.get("tab.misc"));
      int wate = this.buildMenuTab(skin.getDrawable("icon_liquids"), Quarry.Q.i18n.get("tab.water"));
      int powe = this.buildMenuTab(skin.getDrawable("icon_power"), Quarry.Q.i18n.get("tab.power"));
      int high = this.buildMenuTab(Polymerizer.classSchema.icon, Quarry.Q.i18n.get("tab.hightech"));
      this.buildCategories = new VerticalGroup[this.buildTabs.getChildren().size];
      this.buildCategoryButtons = new ButtonGroup[this.buildTabs.getChildren().size];

      for (int i = 0; i < this.buildCategories.length; i++) {
         VerticalGroup t = new VerticalGroup();
         t.expand().fill();
         this.buildCategories[i] = t;
         ButtonGroup<Button> bg = new ButtonGroup();
         bg.setMinCheckCount(0);
         this.buildCategoryButtons[i] = bg;
      }

      this.buildMenuItem(tube, new Conveyor(-1, 0));
      this.buildMenuItem(tube, new ConveyorBridge(-1, 0));
      this.buildMenuItem(tube, new BrickChannel(-1, 0));
      this.buildMenuItem(tube, new CopperTube(-1, 0));
      this.buildMenuItem(tube, new SteelTube(-1, 0));
      this.buildMenuItem(tube, new CopperCable(-1, 0));
      this.buildMenuItem(tube, new ElectricConveyor(-1, 0));
      this.buildMenuItem(tube, new ElectricConveyorCore(-1, 0));
      this.buildMenuItem(rout, new Hopper(-1, 0));
      this.buildMenuItem(rout, new Filter(-1, 0));
      this.buildMenuItem(rout, new Distributor(-1, 0));
      this.buildMenuItem(rout, new Valve(-1, 0));
      this.buildMenuItem(rout, new VacuumPump(-1, 0));
      this.buildMenuItem(rout, new ItemLift(-1, 0));
      this.buildMenuItem(rout, new TubeShaft(-1, 0));
      this.buildMenuItem(rout, new CableShaft(-1, 0));
      this.buildMenuItem(rout, new HighPowerShaft(-1, 0));
      this.buildMenuItem(rout, new ItemDestroyer(-1, 0));
      this.buildMenuItem(mine, new Mine(-1, 0));
      this.buildMenuItem(mine, new Lumberjack(-1, 0));
      this.buildMenuItem(mine, new Carpenter(-1, 0));
      this.buildMenuItem(mine, new Mason(-1, 0));
      this.buildMenuItem(mine, new SawMill(-1, 0));
      this.buildMenuItem(mine, new Excavator(-1, 0));
      this.buildMenuItem(furn, new Kiln(-1, 0));
      this.buildMenuItem(furn, new Furnace(-1, 0));
      this.buildMenuItem(furn, new IngotMold(-1, 0));
      this.buildMenuItem(furn, new RockCrusher(-1, 0));
      this.buildMenuItem(furn, new CharcoalMound(-1, 0));
      this.buildMenuItem(furn, new BallMill(-1, 0));
      this.buildMenuItem(furn, new BlastFurnace(-1, 0));
      this.buildMenuItem(furn, new Crucible(-1, 0));
      this.buildMenuItem(iron, new RollingMachine(-1, 0));
      this.buildMenuItem(iron, new TubeBender(-1, 0));
      this.buildMenuItem(iron, new WireDrawer(-1, 0));
      this.buildMenuItem(iron, new Polarizer(-1, 0));
      this.buildMenuItem(iron, new ArcWelder(-1, 0));
      this.buildMenuItem(stor, new Storage(-1, 0));
      this.buildMenuItem(stor, new Barrel(-1, 0));
      this.buildMenuItem(stor, new Warehouse(-1, 0));
      this.buildMenuItem(stor, new DigitalStorage(-1, 0));
      this.buildMenuItem(stor, new Tank(-1, 0));
      this.buildMenuItem(stor, new Silo(-1, 0));
      this.buildMenuItem(stor, new Capacitor(-1, 0));
      this.buildMenuItem(stor, new SuperCapacitor(-1, 0));
      this.buildMenuItem(misc, new ScienceLab(-1, 0));
      this.buildMenuItem(misc, new Mixer(-1, 0));
      this.buildMenuItem(misc, new ShaftDrill(-1, 0));
      this.buildMenuItem(misc, new AirPurifier(-1, 0));
      this.buildMenuItem(misc, new Booster(-1, 0));
      this.buildMenuItem(misc, new Stacker(-1, 0));
      this.buildMenuItem(misc, new FillingMachine(-1, 0));
      this.buildMenuItem(misc, new BarrelDrainer(-1, 0));
      this.buildMenuItem(high, new Assembler(-1, 0));
      this.buildMenuItem(high, new Compactor(-1, 0));
      this.buildMenuItem(high, new InductionFurnace(-1, 0));
      this.buildMenuItem(high, new Polymerizer(-1, 0));
      this.buildMenuItem(high, new Centrifuge(-1, 0));
      this.buildMenuItem(high, new InjectionMolder(-1, 0));
      this.buildMenuItem(high, new DeviceFabricator(-1, 0));
      this.buildMenuItem(wate, new GroundwaterPump(-1, 0));
      this.buildMenuItem(wate, new Boiler(-1, 0));
      this.buildMenuItem(wate, new Condenser(-1, 0));
      this.buildMenuItem(wate, new OilWell(-1, 0));
      this.buildMenuItem(wate, new Refinery(-1, 0));
      this.buildMenuItem(wate, new DistillationColumn(-1, 0));
      this.buildMenuItem(powe, new Substation(-1, 0));
      this.buildMenuItem(powe, new PowerPole(-1, 0));
      this.buildMenuItem(powe, new AnchorPortal(-1, 0));
      this.buildMenuItem(powe, new WaterWheel(-1, 0));
      this.buildMenuItem(powe, new SteamTurbine(-1, 0));
      this.buildMenuItem(powe, new SolarPanelOutlet(-1, 0));
      this.buildMenuItem(powe, new SolarPanel(-1, 0));
      this.buildMenuItem(powe, new GasTurbine(-1, 0));
      this.buildScrollPane = new ScrollPane(this.buildTabs, skin, "container");
      ScrollPaneStyle sps = new ScrollPaneStyle(this.buildScrollPane.getStyle());
      sps.background = getDrawable(skin, "panel_metalDark", 25, 25, 25, 25);
      this.buildScrollPane.setStyle(sps);
      this.buildScrollPane.setScrollingDisabled(true, false);
      if (Quarry.Q.desktop) {
         this.buildScrollPane.setSize(600.0F, Const.UI_H - 575.0F - (float)Quarry.Q.safeInsets[1] - (float)Quarry.Q.safeInsets[3]);
         this.buildScrollPane.setPosition(70.0F, 434.0F);
      } else {
         int menuW = 700;
         this.buildScrollPane.setSize((float)menuW, Const.UI_H - 500.0F);
         this.buildScrollPane.setPosition(20.0F, 400.0F);
      }

      this.buildScrollPane.setVisible(false);
      this.buildClose = this.createXButton();
      this.buildClose.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.resetActiveStructure();
            GameUi.this.hideTooltip();
            GameUi.this.hideBuildMenu();
         }
      });
      this.buildClose
         .setPosition(
            this.buildScrollPane.getX() + this.buildScrollPane.getWidth() - 40.0F, this.buildScrollPane.getY() + this.buildScrollPane.getHeight() - 40.0F
         );
      this.buildBack = Util.lml("back-button");
      this.buildBack.getImageCell().size(40.0F);
      this.buildBack.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            GameUi.this.buildTabButtons.uncheckAll();
            if (GameUi.this.buildScrollPane.getActor() instanceof VerticalGroup) {
               for (Actor c : ((WidgetGroup)GameUi.this.buildScrollPane.getActor()).getChildren()) {
                  if (c instanceof Button) {
                     ((Button)c).setChecked(false);
                  }
               }
            }

            GameUi.this.buildScrollPane.setActor(GameUi.this.buildTabs);
            GameUi.this.buildScrollPane.setScrollingDisabled(true, false);
            GameUi.this.buildBack.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
         }
      });
      this.buildBack
         .setPosition(
            this.buildScrollPane.getX() - this.buildBack.getWidth() + 20.0F,
            this.buildScrollPane.getY() + this.buildScrollPane.getHeight() - this.buildBack.getHeight() - 20.0F
         );
      this.buildBack.setVisible(false);
      this.buildBack.setName("buildBack");
      this.stage.addActor(this.buildBack);
      this.buildScrollPane.setName("buildScrollPane");
      this.stage.addActor(this.buildScrollPane);
      this.buildClose.setName("buildClose");
      this.stage.addActor(this.buildClose);
   }

   protected void initScienceUI(Skin skin) {
      this.scienceGroup = new ButtonGroup();
      this.scienceGroup.setMinCheckCount(0);
      this.scienceButtons = new EnumMap<>(Science.ScienceType.class);
      this.scienceMenu = new VerticalGroup();
      this.scienceMenu.grow();
      this.scienceScrollPane = new ScrollPane(this.scienceMenu, skin);
      ScrollPaneStyle sps = new ScrollPaneStyle(this.scienceScrollPane.getStyle());
      sps.background = getDrawable(skin, "panel_metalDark", 30, 30, 30, 30);
      this.scienceScrollPane.setStyle(sps);
      this.scienceScrollPane.setScrollingDisabled(true, false);
      this.scienceScrollPane.setSize(600.0F, Const.UI_H - 575.0F);
      this.scienceScrollPane.setPosition((Const.UI_W - this.scienceScrollPane.getWidth()) / 2.0F, 434.0F);
      this.scienceScrollPane.setVisible(false);
      this.scienceScrollPane.setName("scienceScrollPane");
      this.stage.addActor(this.scienceScrollPane);
      this.scienceClose = this.createXButton();
      this.scienceClose.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            GameUi.this.hideScienceUI();
         }
      });
      this.scienceClose
         .setPosition(
            this.scienceScrollPane.getX() + this.scienceScrollPane.getWidth() - 40.0F,
            this.scienceScrollPane.getY() + this.scienceScrollPane.getHeight() - 40.0F
         );
      this.scienceClose.setName("scienceClose");
      this.stage.addActor(this.scienceClose);
   }

   protected void initResources(Skin skin) {
      this.resources = new VerticalGroup();
      this.resources.left().columnLeft().space(2.0F);
      Item.ItemType[] t = Item.ItemType.values;
      this.resourceRows = new Table[t.length];

      for (int i = 0; i < t.length; i++) {
         this.resourceRows[i] = createResourceTable(24, skin, t[i], "");
         this.resourceRows[i].setTouchable(Touchable.enabled);
      }

      ScrollPane scroll = new ScrollPane(this.resources);
      scroll.addListener(new InputListener() {
         public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);
            GameUi.this.stage.setScrollFocus(event.getTarget());
         }

         public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);
            GameUi.this.stage.setScrollFocus(toActor);
         }
      });
      scroll.setScrollingDisabled(true, false);
      scroll.setOverscroll(false, false);
      Table table = new Table();
      table.pad(60.0F, 40.0F, 40.0F, 60.0F);
      table.add(scroll).maxHeight(400.0F);
      table.setBackground(skin.getDrawable("panel_boltsDetailSquare"));
      table.setName("resources");
      this.stage.addActor(table);
      this.updateResources(true);
   }

   protected void initWindows(Skin skin) {
      this.toast = Util.lml("toast");
      this.toast.setName("toast");
      this.stage.addActor(this.toast);
      this.seedPrompt = Util.lml("seed-prompt");
      this.prompt = Util.lml("prompt");
      this.alert = Util.lml("alert");
      this.confirm = Util.lml("confirm");
      if (!Quarry.Q.fullVersion) {
         this.upgrade = Util.lml("upgrade");
      }
   }

   protected void initStructureUI(Skin skin) {
      this.itemSelection = new ItemSelection(skin, null);
      this.itemSelection.setName("itemSelection");
      this.stage.addActor(this.itemSelection);
      this.tileUI = Util.lml("tileui");
      this.tileUI.setName("tileUI");
      this.stage.addActor(this.tileUI);
      this.structureUI = new Table();
      this.structureUI.setTouchable(Touchable.enabled);
      this.structureUI.addListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
      });
      this.structureUI.setBackground(skin.getDrawable("panel_boltsBlue"));
      this.structureUI.setSize(450.0F, 300.0F);
      this.structureUI.pad(30.0F).top();
      this.structureUI.setPosition((Const.UI_W - this.structureUI.getWidth()) / 2.0F, 130.0F);
      this.structureUITitle = new Label("", skin);
      this.structureUITitle.setFontScale(1.25F);
      Table titleTable = new Table();
      titleTable.setBackground(new TiledDrawable(skin.getRegion("patternStripesShadow_large")));
      titleTable.add(this.structureUITitle).height(48.0F).expandX().left().padLeft(10.0F);
      this.structureUI.add(titleTable).growX().height(48.0F);
      this.structureUI.row().padTop(6.0F).grow();
      this.structureUIContent = new Table();
      this.structureUI.add(this.structureUIContent);
      this.structureUIButtons = new ImageButton[3];
      this.structureUIButtonCallbacks = new BiCallback[3];

      for (int i = 0; i < 3; i++) {
         final ImageButton ib = new ImageButton(skin, "round");
         ib.setSize(80.0F, 80.0F);
         ib.getImageCell().maxSize(50.0F);
         ib.setPosition(this.structureUI.getX() + this.structureUI.getWidth() + 8.0F, (float)(130 + (2 - i) * 90 + 20));
         ib.setVisible(false);
         final int j = i;
         ib.addListener(
            new ChangeListener() {
               public void changed(ChangeEvent event, Actor actor) {
                  boolean c = ib.isChecked();
                  if (c) {
                     for (int i = 0; i < 3; i++) {
                        if (i != j
                           && GameUi.this.structureUIButtons[i] != null
                           && GameUi.this.structureUIButtons[i].getUserObject() == Schema.ButtonDef.ButtonType.TempRadio) {
                           GameUi.this.structureUIButtons[i].setChecked(false);
                        }
                     }
                  }

                  if (GameUi.this.structureUIButtonCallbacks[j] != null) {
                     GameUi.this.structureUIButtonCallbacks[j].call(c, GameUi.this.currentClickedStructure);
                  }
               }
            }
         );
         ib.addCaptureListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
               Quarry.Q.sound.play(Quarry.Q.clickSfx);
            }
         });
         this.structureUIButtons[i] = ib;
         ib.setName("structure_button_" + i);
         this.stage.addActor(ib);
      }

      this.structureUIStorage = new ImageButton(skin, "round");
      ImageButtonStyle ibs = new ImageButtonStyle(this.structureUIStorage.getStyle());
      ibs.imageUp = Quarry.Q.skin.getDrawable("icon_cinventory");
      this.structureUIStorage.setStyle(ibs);
      this.structureUIStorage.setSize(80.0F, 80.0F);
      this.structureUIStorage.getImageCell().size(40.0F);
      this.structureUIStorage.setPosition(this.structureUI.getX() - 88.0F, 330.0F);
      this.structureUIStorage.setVisible(false);
      this.structureUIStorage.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (GameUi.this.structureUIStorage.isChecked()) {
               GameUi.this.structureUIRecipes.setChecked(false);

               for (int i = 0; i < 3; i++) {
                  if (GameUi.this.structureUIButtons[i] != null && GameUi.this.structureUIButtons[i].getUserObject() == Schema.ButtonDef.ButtonType.TempRadio) {
                     GameUi.this.structureUIButtons[i].setChecked(false);
                  }
               }

               GameUi.this.structureUIContent.clear();
               GameUi.this.structureUITitle.setText(Quarry.Q.i18n.get("ui.inventory"));
               GameUi.this.structureUIContent.add(GameUi.this.structureUIInventoryScrollPane).growX().top().left().expand();
               GameUi.this.updateStructureUIInventory();
            } else {
               GameUi.this.structureUIContent.clear();
               GameUi.this.structureUITitle.setText(GameUi.this.currentClickedStructure.getSchema().name);
               GameUi.this.currentClickedStructure.onClick(GameUi.this.structureUIContent);
            }
         }
      });
      this.structureUIStorage.setName("structureUIStorage");
      this.stage.addActor(this.structureUIStorage);
      this.structureUIRecipes = new ImageButton(skin, "round");
      ibs = new ImageButtonStyle(this.structureUIRecipes.getStyle());
      ibs.imageUp = Quarry.Q.skin.getDrawable("icon_crusher");
      this.structureUIRecipes.setStyle(ibs);
      this.structureUIRecipes.setSize(80.0F, 80.0F);
      this.structureUIRecipes.getImageCell().size(40.0F);
      this.structureUIRecipes.setPosition(this.structureUI.getX() - 88.0F, 240.0F);
      this.structureUIRecipes.setVisible(false);
      this.structureUIRecipes.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            if (GameUi.this.structureUIRecipes.isChecked()) {
               GameUi.this.structureUIStorage.setChecked(false);

               for (int i = 0; i < 3; i++) {
                  if (GameUi.this.structureUIButtons[i] != null && GameUi.this.structureUIButtons[i].getUserObject() == Schema.ButtonDef.ButtonType.TempRadio) {
                     GameUi.this.structureUIButtons[i].setChecked(false);
                  }
               }

               GameUi.this.structureUIContent.clear();
               GameUi.this.structureUITitle.setText(Quarry.Q.i18n.get("ui.recipes"));
               GameUi.this.buildRecipes(GameUi.this.currentClickedStructure, GameUi.this.structureUIContent, GameUi.this.tooltipDescription, false);
            } else {
               GameUi.this.structureUIContent.clear();
               GameUi.this.structureUITitle.setText(GameUi.this.currentClickedStructure.getSchema().name);
               GameUi.this.currentClickedStructure.onClick(GameUi.this.structureUIContent);
            }
         }
      });
      this.structureUIRecipes.setName("structureUIRecipes");
      this.stage.addActor(this.structureUIRecipes);
      this.structureUI.setVisible(false);
      this.structureUI.setName("structureUI");
      this.stage.addActor(this.structureUI);
      this.structureUIInventory = new VerticalGroup();
      this.structureUIInventory.columnLeft().grow();
      this.structureUIInventoryScrollPane = new ScrollPane(this.structureUIInventory);
      this.structureUIInventoryScrollPane.setScrollingDisabled(true, false);
      this.structureUIInventoryCells = new EnumMap<>(Item.ItemType.class);
      this.structureUIInventorySum = new EnumMap<>(Item.ItemType.class);
   }

   protected void initCopyTable(Skin skin) {
      this.copyTable = Util.lml("copy");
      this.copyTable.setPosition((Const.UI_W - this.tooltip.getWidth()) / 2.0F, 130.0F);
      this.copyTable.addListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
      });
      this.copyCosts = Util.id("costs");
      this.copyTable.setName("copyTable");
      this.stage.addActor(this.copyTable);
   }

   protected ImageButton createXButton() {
      ImageButton ib = Util.lml("x-button");
      ib.getImageCell().size(24.0F);
      return ib;
   }

   protected void showBuildMenu() {
      this.buildTabButtons.uncheckAll();
      if (this.buildScrollPane.getActor() instanceof VerticalGroup) {
         for (Actor c : ((WidgetGroup)this.buildScrollPane.getActor()).getChildren()) {
            if (c instanceof Button) {
               ((Button)c).setChecked(false);
            }
         }
      }

      for (Actor cx : this.buildMenuStars) {
         cx.setVisible(true);
      }

      this.buildScrollPane.setActor(this.buildTabs);
      this.buildScrollPane.setScrollingDisabled(true, false);
      Game.G.resetActiveStructure();
      this.buildClose.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.fadeIn(0.1F)));
      this.buildScrollPane.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.fadeIn(0.1F)));
      this.updateBuildMenuResources();
      this.updateBuildMenuSciences();
   }

   public void hideBuildMenu() {
      this.hideBuildMenu(false);
   }

   public void hideBuildMenu(boolean instantly) {
      this.buildButton.setChecked(false);
      if (instantly) {
         this.buildClose.setVisible(false);
         this.buildScrollPane.setVisible(false);
         this.buildBack.setVisible(false);
      } else {
         this.buildClose.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
         this.buildScrollPane.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
         this.buildBack.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
      }

      for (Actor c : this.buildMenuStars) {
         c.setVisible(false);
      }

      this.stage.unfocusAll();
   }

   protected int buildMenuTab(Drawable icon, String title) {
      final int index = this.buildTabs.getChildren().size;
      final Button c = new Button(this.menuButtonStyle) {
         public float getPrefHeight() {
            return this.isVisible() ? super.getPrefHeight() : 0.0F;
         }
      };
      c.padTop(10.0F);
      Label l = new Label(title, Quarry.Q.skin);
      c.add(new Image(icon)).size(80.0F).spaceRight(10.0F);
      c.add(l).grow().top().left().pad(10.0F);
      sep(c).spaceTop(10.0F).colspan(2).fillX();
      this.buildTabs.addActor(c);
      this.buildTabButtons.add(c);
      c.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            if (c.isChecked()) {
               GameUi.this.buildBack.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.fadeIn(0.1F)));
            }

            GameUi.this.buildScrollPane.setActor(GameUi.this.buildCategories[index]);
            GameUi.this.buildScrollPane.setScrollingDisabled(true, false);
            GameUi.this.updateBuildMenuResources();

            for (Actor c : GameUi.this.buildCategories[index].getChildren()) {
               if (c instanceof Button) {
                  ((Button)c).setChecked(false);
               }
            }

            GameUi.this.hideTooltip();
         }
      });
      c.addCaptureListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      return this.buildTabs.getChildren().size - 1;
   }

   protected void buildMenuItem(int tab, final Structure<?> template) {
      final Button t = new Button(this.menuButtonStyle) {
         public float getPrefHeight() {
            return this.isVisible() ? super.getPrefHeight() : 0.0F;
         }
      };
      t.padTop(10.0F);
      Label l = new Label(template.getSchema().name, Quarry.Q.skin);
      t.add(new Image(template.getSchema().icon)).size(80.0F).spaceRight(10.0F);
      t.add(l).grow().top().left().pad(10.0F);
      sep(t).spaceTop(10.0F).colspan(2).fillX();
      t.setUserObject(template);
      t.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
         }
      });
      t.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            if (!GameUi.this.buildScrollPane.isVisible() || GameUi.this.buildScrollPane.getActions().size > 0) {
               return;
            }

            if (t.isChecked()) {
               GameUi.this.selectBuildStructure(t, template);
            } else {
               Game.G.resetActiveStructure();
            }
         }
      });
      this.buildCategories[tab].addActor(t);
      this.buildCategoryButtons[tab].add(t);
   }

   public void clearBuildSelection() {
      if (this.buildCategoryButtons == null) {
         return;
      }

      for (ButtonGroup<Button> group : this.buildCategoryButtons) {
         if (group != null) {
            Button checked = group.getChecked();
            if (checked != null) {
               checked.setProgrammaticChangeEvents(false);
               group.uncheckAll();
               checked.setProgrammaticChangeEvents(true);
            }
         }
      }
   }

   private void selectBuildStructure(Button button, Structure<?> template) {
      Game.G.structureDestroyMode = false;
      boolean buildable = Game.GOD_MODE || ((Actor)button.getChildren().get(1)).getColor().equals(Color.WHITE);
      Structure<?> selected = null;
      if (buildable) {
         selected = (Structure<?>)template.clone();
         if (selected != null) {
            selected.x = -1;
            selected.y = 0;
            if (!(selected instanceof ElectricConveyorCore)) {
               selected.setUpDirection(Direction.North);
            }

            Game.G.activeStructure = selected;
         }
      }

      this.showTooltip(selected != null ? selected : template);
      this.tooltipClose.setVisible(false);
      this.tooltipCollapse.setChecked(false);
      this.tooltipCollapse.setVisible(false);
      if (selected != null) {
         this.showOrHideFlipButton();
      }
   }

   protected Button scienceMenuItem(Science.ScienceType science) {
      Button b = this.scienceButtons.get(science);
      if (b != null) {
         return b;
      } else {
         final Button t = new Button(this.menuButtonStyle) {
            public float getPrefHeight() {
               return this.isVisible() ? super.getPrefHeight() : 0.0F;
            }
         };
         t.padTop(10.0F);
         t.add(new Image(science.icon)).size(80.0F).spaceRight(10.0F).top();
         Table text = new Table();
         text.defaults().growX();
         text.add(new Label(science.title, Quarry.Q.skin));
         text.row();
         Label l = new Label(science.description, Quarry.Q.skin, "small-font", Color.LIGHT_GRAY);
         l.setWrap(true);
         text.add(l).fill();
         t.add(text).grow().top().left().space(10.0F);
         t.row();
         HorizontalGroup hg = new HorizontalGroup();
         hg.space(10.0F).left();

         for (Item.Items.Amount a : science.costs.entries) {
            hg.addActor(createResourceTable(32, Quarry.Q.skin, a.getItem(), formatResourceAmount(a.getAmount())));
         }

         t.add();
         t.add(hg).growX().left();
         sep(t).spaceTop(10.0F).colspan(2).fillX();
         t.setUserObject(science);
         t.getListeners()
            .insert(
               0,
               new ClickListener() {
                  public void clicked(InputEvent event, float x, float y) {
                     Quarry.Q.sound.play(Quarry.Q.clickSfx);
                     if (GameUi.this.currentClickedStructure instanceof ScienceLab
                        && t.isChecked()
                        && (((Actor)((Table)t.getChildren().get(1)).getChildren().get(0)).getColor().equals(Color.WHITE) || Game.GOD_MODE)) {
                        Science.ScienceType s = (Science.ScienceType)t.getUserObject();
                        if (Game.DRAW_DEBUG) {
                           Game.G.addScience(s);
                        } else {
                           ((ScienceLab)GameUi.this.currentClickedStructure).setActiveScience(s);
                        }

                        GameUi.this.hideScienceUI();
                     }
                  }
               }
            );
         this.scienceButtons.put(science, t);
         this.scienceGroup.add(t);
         return t;
      }
   }

   protected ImageButton roundButton(Skin skin, Drawable icon, EventListener listener, String tooltip) {
      ImageButton ib = new ImageButton(skin, "round") {
         public float getPrefWidth() {
            return super.getWidth();
         }

         public float getPrefHeight() {
            return super.getHeight();
         }
      };
      ib.setSize(100.0F, 100.0F);
      ImageButtonStyle ibs = new ImageButtonStyle(ib.getStyle());
      ibs.imageUp = icon;
      ib.setStyle(ibs);
      ib.addListener(listener);
      ib.getImageCell().size(60.0F);
      ib.addListener(new TextTooltip(Quarry.Q.i18n.get(tooltip), skin));
      return ib;
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

   protected void showOrHideFlipButton() {
      if (this.flipButton != null && this.tooltip != null) {
         if (Game.G.activeStructure == this.tooltipCurrentStructure || this.currentClickedStructure != null) {
            this.flipButton.clearActions();
            if (!(this.tooltipCurrentStructure instanceof IFlippable) && !(this.currentClickedStructure instanceof IFlippable)) {
               this.flipButton
                  .addAction(
                     Actions.sequence(
                        Actions.parallel(
                           Actions.moveTo(this.flipButton.getX(), this.tooltip.getY() + 200.0F, 0.15F, Interpolation.swingOut), Actions.fadeOut(0.1F)
                        ),
                        Actions.visible(false),
                        Actions.alpha(1.0F)
                     )
                  );
            } else {
               this.flipButton
                  .addAction(
                     Actions.sequence(
                        Actions.alpha(1.0F),
                        Actions.visible(true),
                        Actions.moveTo(this.flipButton.getX(), this.tooltip.getY() + this.tooltip.getHeight() + 20.0F, 0.15F, Interpolation.swingOut)
                     )
                  );
            }
         }
      }
   }

   public void updateResources(boolean justRegister) {
      if (this.tooltip != null && this.tooltip.isVisible()) {
         this.updateTooltipResources();
      }

      if (this.copyTable != null && this.copyTable.isVisible()) {
         this.updateCopyTableResources(false);
      }

      if (this.buildScrollPane != null && this.buildScrollPane.isVisible()) {
         this.updateBuildMenuResources();
      }

      if (justRegister) {
         this.wantToUpdateResources = true;
      } else if (this.wantToUpdateResources) {
         try {
            this.resources.clear();

            for (Entry<Item.ItemType, Integer> r : Game.G.getAllResources()) {
               Table ac = this.resourceRows[r.getKey().ordinal()];
               ((Label)((Cell)ac.getCells().get(1)).getActor()).setText("" + r.getValue());
               ((TextTooltip)ac.getListeners().get(0)).getActor().setText(r.getValue() + " " + r.getKey().title);
               if (Game.G.hasSeenResource(r.getKey())) {
                  this.resources.addActor(ac);
               }
            }

            this.resources.fill();
            this.resources.pack();
         } catch (IndexOutOfBoundsException var6) {
         }

         int w = (int)this.resources.getWidth();
         int round = 50;
         this.resources.setWidth((float)(w + (round - w % round)));
         ScrollPane p = (ScrollPane)this.resources.getParent();
         Table t = (Table)p.getParent();
         t.pack();
         t.setPosition(Const.UI_W - t.getWidth() + 40.0F, Const.UI_H - t.getHeight() + 40.0F);
         this.clampToInsets(t);
         t.moveBy(40.0F, 40.0F);
         this.wantToUpdateResources = false;
      }
   }

   public void updateBuildMenuResources() {
      if (this.buildScrollPane.getActor() != this.buildTabs) {
         for (Actor c : ((VerticalGroup)this.buildScrollPane.getActor()).getChildren()) {
            if (c instanceof Button) {
               boolean canAfford = true;
               if (this.tutorial.getStepNum() > -1) {
                  if (this.tutorial.getStepNum() < 10) {
                     canAfford = c.getUserObject() instanceof Mine;
                  } else {
                     canAfford = c.getUserObject().getClass().equals(Conveyor.class);
                  }
               }

               if (canAfford) {
                  for (Item.Items.Amount a : ((Structure)c.getUserObject()).getSchema().buildCosts.entries) {
                     if (Game.G.getResource(a.getItem()) < a.getAmount()) {
                        canAfford = false;
                        break;
                     }
                  }
               }

               if (canAfford) {
                  ((Actor)((Table)c).getChildren().get(1)).setColor(Color.WHITE);
               } else {
                  ((Actor)((Table)c).getChildren().get(1)).setColor(off);
               }
            }
         }
      }
   }

   public void updateBuildMenuSciences() {
      int i = 0;
      if (this.buildMenuSciences.isEmpty()) {
         this.buildMenuSciences.addAll(Game.G.sciences);
      }

      Array<Actor> categoryAddStar = new Array();
      Array<Actor> structureAddStar = new Array();

      for (VerticalGroup vg : this.buildCategories) {
         boolean any = false;
         int _new = 0;

         for (Actor c : vg.getChildren()) {
            if (c instanceof Button) {
               Structure<?> s = (Structure<?>)c.getUserObject();
               Collection<Science.ScienceType> sciences = s.getSchema().sciencesRequired;
               boolean vis = Game.G.hasSciences(sciences);
               if (vis && !this.buildMenuSciences.containsAll(sciences)) {
                  _new++;
                  structureAddStar.add(c);
               } else if (s instanceof ProducerStructure) {
                  for (RecipeList.Recipe r : ((ProducerStructure.ProducerSchema)s.getSchema()).recipeList.recipes) {
                     if (Game.G.hasSciences(r.getRequiredSciences()) && !this.buildMenuSciences.containsAll(r.getRequiredSciences())) {
                        _new++;
                        structureAddStar.add(c);
                     }
                  }
               }

               c.setVisible(vis);
               if (vis) {
                  any = true;
               }
            }
         }

         vg.invalidate();
         vg.validate();
         Actor a = (Actor)this.buildTabs.getChildren().get(i);
         if (!any) {
            a.setVisible(false);
         } else {
            boolean visx = a.isVisible();
            a.setVisible(true);
            if (!visx || _new > 0) {
               categoryAddStar.add(a);
            }
         }

         i++;
      }

      if (!categoryAddStar.isEmpty() || !structureAddStar.isEmpty()) {
         for (Actor cx : this.buildMenuStars) {
            cx.remove();
         }

         this.buildMenuStars.clear();
      }

      this.buildTabs.invalidate();
      this.buildTabs.validate();

      for (final Actor a : categoryAddStar) {
         if (this.buildMenuStars == null) {
            this.buildMenuStars = new Array();
         }

         final Image bg = new Image(Quarry.Q.skin.getDrawable("icon_new_bg")) {
            public boolean isVisible() {
               return GameUi.this.buildButton.isChecked() && GameUi.this.buildScrollPane.getActor() == GameUi.this.buildTabs;
            }
         };
         bg.setTouchable(Touchable.disabled);
         bg.setSize(50.0F, 50.0F);
         bg.setOrigin(bg.getWidth() / 2.0F, bg.getHeight() / 2.0F);
         bg.addAction(Actions.forever(Actions.rotateBy(360.0F, 30.0F)));
         bg.setPosition(160.0F, 110.0F);
         this.buildMenuStars.add(bg);
         bg.setName("star_bg");
         ((Group)a).addActor(bg);
         final Image fg = new Image(Quarry.Q.skin.getDrawable("icon_new_fg")) {
            public boolean isVisible() {
               return GameUi.this.buildButton.isChecked() && GameUi.this.buildScrollPane.getActor() == GameUi.this.buildTabs;
            }
         };
         fg.setSize(bg.getWidth(), bg.getHeight());
         fg.setTouchable(Touchable.disabled);
         fg.setPosition(bg.getX(), bg.getY());
         fg.setName("star_fg");
         ((Group)a).addActor(fg);
         this.buildMenuStars.add(fg);
         a.addCaptureListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
               fg.remove();
               bg.remove();
               a.removeListener(this);
            }
         });
      }

      for (final Actor a : structureAddStar) {
         if (this.buildMenuStars == null) {
            this.buildMenuStars = new Array();
         }

         final Image bg = new Image(Quarry.Q.skin.getDrawable("icon_new_bg")) {
            public boolean isVisible() {
               return GameUi.this.buildButton.isChecked() && GameUi.this.buildScrollPane.getActor() == a.getParent();
            }
         };
         bg.setTouchable(Touchable.disabled);
         bg.setSize(50.0F, 50.0F);
         bg.setOrigin(bg.getWidth() / 2.0F, bg.getHeight() / 2.0F);
         bg.addAction(Actions.forever(Actions.rotateBy(360.0F, 30.0F)));
         bg.setPosition(50.0F, 50.0F);
         this.buildMenuStars.add(bg);
         bg.setName("star_bg");
         ((Group)a).addActor(bg);
         final Image fg = new Image(Quarry.Q.skin.getDrawable("icon_new_fg")) {
            public boolean isVisible() {
               return GameUi.this.buildButton.isChecked() && GameUi.this.buildScrollPane.getActor() == a.getParent();
            }
         };
         fg.setSize(bg.getWidth(), bg.getHeight());
         fg.setTouchable(Touchable.disabled);
         fg.setPosition(bg.getX(), bg.getY());
         fg.setName("star_fg");
         ((Group)a).addActor(fg);
         this.buildMenuStars.add(fg);
         a.addCaptureListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
               fg.remove();
               bg.remove();
               a.removeListener(this);
            }
         });
      }

      this.buildMenuSciences.addAll(Game.G.sciences);
   }

   public void showTooltip(Structure<?> structure) {
      this.hideStructureUI();
      this.tileUI.hide();
      this.tooltipCurrentStructure = structure;
      this.tooltip.setVisible(true);
      this.tooltipClose.setVisible(true);
      this.tooltipCollapse.setVisible(true);
      Skin skin = Quarry.Q.skin;
      this.tooltipCosts.clear();
      Table tiles = createResourceTable(32, skin, "icon_tiles", structure.getWidth() + "x" + structure.getHeight());
      tiles.setBackground(skin.getDrawable("shadow02-bg"));
      tiles.pad(0.0F, 5.0F, 0.0F, 5.0F);
      tiles.left();
      Cell<Table> tilesCell = this.tooltipCosts.add(tiles).padRight(5.0F);
      Item.Items costs = structure.getSchema().buildCosts;
      if (costs != null) {
         int len = costs.entries.length;
         if (len > 3) {
            tilesCell.colspan(2);
         }

         this.tooltipCosts.row();

         for (int i = 0; i < Math.min(len, 3); i++) {
            Item.Items.Amount e = costs.entries[i];
            this.tooltipCosts.add(createResourceTable(32, skin, e.getItem(), formatResourceAmount(e.getAmount()))).padRight(15.0F);
            if (len > i + 3) {
               Item.Items.Amount f = costs.entries[i + 3];
               this.tooltipCosts.add(createResourceTable(32, skin, f.getItem(), formatResourceAmount(f.getAmount())));
            }

            this.tooltipCosts.row();
         }
      }

      this.tooltipRecipes.clear();
      this.buildRecipes(structure, this.tooltipRecipes, this.tooltipDescription, true);
      this.updateTooltipResources();
   }

   public void showCopyTable() {
      this.hideStructureUI();
      this.hideTooltip();
      this.copyTable.setVisible(true);
      this.updateCopyTableResources(true);
   }

   protected void buildRecipes(Structure<?> structure, Table recipeTable, final Label description, boolean allRecipes) {
      if (structure instanceof ProducerStructure) {
         final Array<RecipeList.Recipe> recipes = new Array(((ProducerStructure.ProducerSchema)structure.getSchema()).recipeList.recipes);
         if (structure instanceof GasTurbine) {
            recipes.clear();
            recipes.addAll(((GasTurbine)structure).getInstancedRecipes());
         }

         if (structure instanceof Mine && !allRecipes) {
            recipes.clear();
            Mine m = (Mine)structure;

            for (RecipeList.Recipe r : ((ProducerStructure.ProducerSchema)structure.getSchema()).recipeList.recipes) {
               Item.ItemType outItem = r.getOutput().entries[0].getItem();
               Item.ItemCategory outCat = r.getOutput().entries[0].getCat();

               for (Item.ItemType t : m.mineableItems) {
                  if (outItem == t || Item.base(t) == outItem || t.categories.contains(outCat)) {
                     recipes.add(r);
                     break;
                  }
               }
            }
         }

         Iterator<RecipeList.Recipe> iter = recipes.iterator();

         while (iter.hasNext()) {
            RecipeList.Recipe r = iter.next();
            if (!Game.G.hasSciences(r.getRequiredSciences())) {
               iter.remove();
            }
         }

         if (recipes.size == 1) {
            RecipeList.Recipe r = (RecipeList.Recipe)recipes.get(0);
            this.currentRecipe = r;
            Table table = renderRecipe(Quarry.Q.skin, r, true);
            recipeTable.add(table).expandY().right().top().height(145.0F).minWidth(300.0F);
            description.setText(r.getDescription());
         } else {
            float width = 360.0F;
            final ImageButton chevronLeft = new ImageButton(Quarry.Q.skin.getDrawable("caret_left"));
            chevronLeft.getImageCell().padLeft(10.0F);
            final ImageButton chevronRight = new ImageButton(Quarry.Q.skin.getDrawable("caret_right"));
            chevronRight.getImageCell().padRight(10.0F);
            recipeTable.add(chevronLeft).growY();
            Table tx = new Table();
            tx.defaults().growX();
            final ScrollPane scroll = new ScrollPane(tx) {
               public void act(float delta) {
                  super.act(delta);
                  float mod = this.getScrollX() % 360.0F;
                  float vis = this.getVisualScrollX();
                  float max = this.getMaxX();
                  if ((this.isFlinging() || this.isPanning()) && (mod < 10.0F || mod > 340.0F) && vis >= 0.0F && vis <= max) {
                     this.visualScrollX((float)Math.round(this.getScrollX() / 360.0F) * 360.0F);
                  }

                  if (!this.isFlinging() && !this.isPanning()) {
                     this.setScrollX((float)Math.round(this.getScrollX() / 360.0F) * 360.0F);
                  }

                  if (this.isFlinging() || this.isPanning() || this.getVisualScrollX() != this.getScrollX()) {
                     RecipeList.Recipe r = (RecipeList.Recipe)recipes.get(MathUtils.clamp(Math.round(this.getScrollX() / 360.0F), 0, recipes.size - 1));
                     GameUi.this.currentRecipe = r;
                     description.setText(r.getDescription());
                     float scroll = this.getScrollX();
                     chevronLeft.setVisible(scroll > 0.0F);
                     chevronRight.setVisible(scroll < max);
                  }
               }
            };
            chevronLeft.setVisible(false);
            chevronLeft.addListener(new ClickListener() {
               public void clicked(InputEvent event, float x, float y) {
                  Quarry.Q.sound.play(Quarry.Q.clickSfx);
                  scroll.setScrollX(scroll.getScrollX() - 300.0F);
               }
            });
            chevronRight.addListener(new ClickListener() {
               public void clicked(InputEvent event, float x, float y) {
                  Quarry.Q.sound.play(Quarry.Q.clickSfx);
                  scroll.setScrollX(scroll.getScrollX() + 300.0F);
               }
            });

            for (RecipeList.Recipe r : recipes) {
               tx.add(renderRecipe(Quarry.Q.skin, r, false)).width(360.0F);
            }

            description.setText(((RecipeList.Recipe)recipes.get(0)).getDescription());
            this.currentRecipe = (RecipeList.Recipe)recipes.get(0);
            scroll.setScrollingDisabled(false, true);
            recipeTable.add(scroll).pad(0.0F, 10.0F, 0.0F, 10.0F).width(360.0F).height(145.0F);
            recipeTable.add(chevronRight).growY();
         }
      } else {
         description.setText(structure.getSchema().description);
         if (!(structure instanceof Storage) && !(structure instanceof Tank) && !(structure instanceof Barrel)) {
            if (structure instanceof Substation) {
               recipeTable.add(createResourceTable(32, Quarry.Q.skin, "icon_power", formatPowerAmount((double)((Substation)structure).getSchema().capacity)))
                  .top()
                  .expand();
            } else if (structure instanceof SolarPanelOutlet || structure instanceof SolarPanel) {
               recipeTable.add(createResourceTable(32, Quarry.Q.skin, "icon_power", formatPowerAmount(6000.0) + "/s")).top().expand();
            } else if (structure instanceof Boiler) {
               recipeTable.add(this.boilerRecipe).expandY().right().top().height(145.0F).minWidth(300.0F);
            } else if (structure instanceof Refinery) {
               recipeTable.add(this.refRecipe).expandY().right().top().height(145.0F).minWidth(340.0F);
            } else if (structure instanceof DistillationColumn) {
               recipeTable.add(this.distRecipe).expandY().right().top().height(145.0F).minWidth(340.0F);
            }
         } else {
            for (Component c : structure.getSchema().getComponents()) {
               if (c instanceof CTank) {
                  recipeTable.add(createResourceTable(32, Quarry.Q.skin, "icon_tank", formatResourceAmount((float)((CTank)c).getSize() / 1000.0F) + "L"))
                     .top()
                     .expand();
               } else {
                  recipeTable.add(createResourceTable(32, Quarry.Q.skin, "icon_cinventory", ((IStorage)c).getSize() + "")).top().expand();
               }
            }
         }
      }
   }

   public void updateTooltipResources() {
      if (this.tooltipCurrentStructure != null) {
         boolean canAfford = true;
         Item.Items costs = this.tooltipCurrentStructure.getSchema().buildCosts;

         for (Cell<?> c : this.tooltipCosts.getCells()) {
            Table t = (Table)c.getActor();
            if (t.getUserObject() != null) {
               Item.ItemType res = (Item.ItemType)t.getUserObject();
               int scale = 1;
               if (Game.G.endB.x > -1.0F) {
                  scale = Game.G.activeStructureTrail.size;
               }

               boolean more = Game.G.getResource(res) >= scale * costs.getAmount(res);
               Label l = (Label)((Cell)t.getCells().get(1)).getActor();
               l.setText(formatResourceAmount(costs.getAmount(res) * scale));
               l.setColor(more ? Color.WHITE : Color.FIREBRICK);
               if (!more) {
                  canAfford = false;
               }
            }
         }

         this.canAffordStructure = canAfford;
      }
   }

   public void updateCopyTableResources(boolean reset) {
      if (reset) {
         Skin skin = Quarry.Q.skin;
         this.copyCosts.clear();
         Table tiles = createResourceTable(32, skin, "icon_tiles", Game.G.copyRegion[2] + "x" + Game.G.copyRegion[3]);
         tiles.setBackground(skin.getDrawable("shadow02-bg"));
         tiles.pad(0.0F, 5.0F, 0.0F, 5.0F);
         tiles.left();
         this.copyCosts.add(tiles).padRight(5.0F);
         int i = 1;
         if (!Game.G.pasteMode) {
            Game.G.copyCost.put(Item.ItemType.Blueprint, 0);
         } else {
            Game.G.copyCost.remove(Item.ItemType.Blueprint);
         }

         for (Entry<Item.ItemType, Integer> x : Game.G.copyCost.entrySet()) {
            if (i++ % 4 == 0) {
               this.copyCosts.row();
            }

            this.copyCosts.add(createResourceTable(32, skin, x.getKey(), formatResourceAmount(x.getValue()))).padRight(15.0F);
         }
      }

      boolean canAfford = true;

      for (Cell<?> c : this.copyCosts.getCells()) {
         Table t = (Table)c.getActor();
         if (t.getUserObject() == null) {
            Label l = (Label)((Cell)t.getCells().get(1)).getActor();
            l.setText(Game.G.copyRegion[2] + "x" + Game.G.copyRegion[3]);
         } else {
            Item.ItemType res = (Item.ItemType)t.getUserObject();
            boolean more = Game.G.getResource(res) >= Game.G.copyCost.get(res);
            Label l = (Label)((Cell)t.getCells().get(1)).getActor();
            l.setText(formatResourceAmount(Game.G.copyCost.get(res)));
            l.setColor(more ? Color.WHITE : Color.FIREBRICK);
            if (!more) {
               canAfford = false;
            }
         }
      }

      this.canAffordStructure = canAfford;
   }

   public void hideTooltip() {
      this.tooltipRecipes.clear();
      this.tooltipCurrentStructure = null;
      this.tooltip.setVisible(false);
      this.tooltipClose.setVisible(false);
      this.tooltipCollapse.setVisible(false);
      this.tooltipCollapse.setChecked(false);
      this.flipButton.addAction(Actions.sequence(Actions.visible(false), Actions.moveTo(this.flipButton.getX(), this.tooltip.getY() + 200.0F)));
      this.stage.unfocusAll();
   }

   public void hideCopyTable() {
      Game.G.copyMode = false;
      Game.G.pasteMode = false;
      this.copyTable.setVisible(false);
      this.copyButton.setChecked(false);
      this.stage.unfocusAll();
   }

   public void showStructureUI(Structure<?> s) {
      if (this.currentClickedStructure != s) {
         if (s instanceof ShaftDrillHead) {
            s = ((ShaftDrillHead)s).getDrill();
         }

         this.hideStructureUI();
         this.tileUI.hide();
         this.currentClickedStructure = s;

         if (s instanceof IFlippable) {
            this.showOrHideFlipButton();
         }

         if (s instanceof Conveyor && !(s instanceof ElectricConveyorCore)) {
            s.onClick(this.structureUIContent);
         } else {
            if (s instanceof ProducerStructure) {
               for (Dock d : s.getDocks()) {
                  if (d.type == Dock.DockType.ItemIn || d.type == Dock.DockType.FluidIn) {
                     this.structureUIStorage.setVisible(true);
                     break;
                  }
               }
            }

            this.structureUIRecipes
               .setVisible(
                  s instanceof ProducerStructure
                     || s instanceof GeneratorStructure
                     || s.getSchema().type == StructureType.Refinery
                     || s.getSchema().type == StructureType.Boiler
                     || s.getSchema().type == StructureType.DistillationColumn
               );
            Arrays.fill(this.structureUIButtonCallbacks, null);
            this.structureUITitle.setText(s.getSchema().name);
            this.structureUIContent.clear();
            int len = s.getSchema().buttons.size;

            for (int i = 0; i < 3; i++) {
               ImageButton ib = this.structureUIButtons[i];
               if (i >= len) {
                  ib.setVisible(false);
               } else {
                  Schema.ButtonDef b = (Schema.ButtonDef)s.getSchema().buttons.get(i);
                  ImageButtonStyle ibs = new ImageButtonStyle(ib.getStyle());
                  ibs.imageUp = Quarry.Q.skin.getDrawable(b.icon);
                  if (b.type == Schema.ButtonDef.ButtonType.SinglePress) {
                     ibs.checked = null;
                  } else {
                     ibs.checked = Quarry.Q.skin.getDrawable("round_metalActive");
                  }

                  if (b.type == Schema.ButtonDef.ButtonType.StateToggle) {
                     ib.setChecked(s.getButtonState(i));
                  } else if (b.type == Schema.ButtonDef.ButtonType.TempRadio) {
                     ib.setChecked(false);
                  }

                  ib.setStyle(ibs);
                  ib.setUserObject(b.type);
                  Iterator<EventListener> iter = ib.getListeners().iterator();

                  while (iter.hasNext()) {
                     if (iter.next() instanceof TextTooltip) {
                        iter.remove();
                        break;
                     }
                  }

                  ib.addListener(new TextTooltip(Quarry.Q.i18n.get(b.tooltip), Quarry.Q.skin));
                  this.structureUIButtonCallbacks[i] = b.listener;
                  ib.setVisible(true);
               }
            }

            s.onClick(this.structureUIContent);
            this.structureUI.setVisible(true);
         }
      }
   }

   public void hideStructureUI() {
      if (this.currentClickedStructure != null) {
         this.currentClickedStructure.onUnclick();

         for (int i = 0; i < 3; i++) {
            if (this.structureUIButtons[i] != null && this.structureUIButtons[i].getUserObject() == Schema.ButtonDef.ButtonType.TempRadio) {
               this.structureUIButtons[i].setChecked(false);
            }
         }

         this.structureUI.setVisible(false);
         this.currentClickedStructure = null;

         for (int ix = 0; ix < 3; ix++) {
            ImageButton ib = this.structureUIButtons[ix];
            ib.setVisible(false);
         }

         this.showOrHideFlipButton();
      }

      this.itemSelection.hide();
      this.structureUIStorage.setChecked(false);
      this.structureUIStorage.setVisible(false);
      this.structureUIRecipes.setChecked(false);
      this.structureUIRecipes.setVisible(false);
      this.stage.unfocusAll();
   }

   public void showScienceUI() {
      if (!this.scienceScrollPane.isVisible()) {
         this.scienceMenu.clear();

         for (Science.ScienceType s : Science.ScienceType.values) {
            if (s.required.length != 0
               && (!Game.G.hasScience(s) || Game.GOD_MODE)
               && !Game.G.hasCurrentScience(s)
               && (Game.G.hasSciences(s.required) || Game.GOD_MODE)) {
               Button button = this.scienceMenuItem(s);
               this.scienceMenu.addActor(button);
            }
         }

         this.scienceScrollPane.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.fadeIn(0.1F)));
         this.scienceClose.addAction(Actions.sequence(Actions.alpha(0.0F), Actions.visible(true), Actions.fadeIn(0.1F)));
      }
   }

   public void hideScienceUI() {
      this.hideScienceUI(false);
   }

   public void hideScienceUI(boolean instantly) {
      if (this.scienceScrollPane.isVisible()) {
         if (this.currentClickedStructure instanceof ScienceLab) {
            this.structureUIButtons[0].setChecked(false);
         }

         if (instantly) {
            this.scienceScrollPane.setVisible(false);
            this.scienceClose.setVisible(false);
         } else {
            this.scienceScrollPane.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
            this.scienceClose.addAction(Actions.sequence(Actions.fadeOut(0.1F), Actions.visible(false)));
         }

         this.stage.unfocusAll();
      }
   }

   public void updateStructureUIInventory() {
      if (this.currentClickedStructure != null && this.currentClickedStructure instanceof ProducerStructure) {
         this.structureUIInventory.clear();
         this.structureUIInventorySum.clear();
         Structure<?> structure = this.currentClickedStructure;
         IStorage[] inputs = ((ProducerStructure)structure).getInputInventories();
         int i = 0;

         for (int j = 0; i < structure.getDocks().length; i++) {
            if (structure.getDocks()[i].type != Dock.DockType.ItemIn) {
               if (structure.getDocks()[i].type == Dock.DockType.FluidIn) {
                  CTank tank = (CTank)inputs[j];
                  if (!tank.isEmpty()) {
                     this.structureUIInventorySum.put(tank.getFluid(), tank.get(tank.getFluid()));
                  }

                  j++;
               }
            } else {
               for (Entry<Item.ItemType, Integer> e : ((CRecipeSlotStorage)inputs[j]).getAll()) {
                  Integer val = this.structureUIInventorySum.get(e.getKey());
                  if (val == null) {
                     val = 0;
                  }

                  this.structureUIInventorySum.put(e.getKey(), val + e.getValue());
               }

               j++;
            }
         }

         for (Entry<Item.ItemType, Integer> e : this.structureUIInventorySum.entrySet()) {
            Table t = this.structureUIInventoryCells.get(e.getKey());
            String text = "";
            if (e.getKey().categories.contains(Item.ItemCategory.Fluid)) {
               text = formatResourceAmount((float)e.getValue().intValue() / 1000.0F, true) + "L";
            } else {
               text = formatResourceAmount(e.getValue());
            }

            if (t == null) {
               t = createResourceTable(32, Quarry.Q.skin, e.getKey(), text, e.getKey());
               this.structureUIInventoryCells.put(e.getKey(), t);
               ((Label)((Cell)t.getCells().get(1)).getActor()).setAlignment(8);
            } else {
               ((Label)t.getChildren().get(1)).setText(text);
               ((TextTooltip)t.getListeners().get(0)).getActor().setText(text + " " + e.getKey().title);
            }

            this.structureUIInventory.addActor(t);
         }
      }
   }

   public void update(double deltaTime) {
      try {
         this.stage.act((float)deltaTime);
      } catch (Exception var4) {
         Quarry.Q.pi.message(1, var4);
      }

      this.tutorial.update();
      this.updateResources(false);
      this.menu
         .fps
         .setText(
            String.format("FPS: %d\nUPS: %d", Gdx.graphics.getFramesPerSecond(), Math.round(1.0F / Math.max(0.016666668F, Quarry.Q.getUpdateTime() / 1000.0F)))
         );
   }

   public void draw() {
      this.stage.draw();
   }

   public void resize(int width, int height) {
      this.stage.getViewport().update(width, height);
   }

   @Override
   public Stage getStage() {
      return this.stage;
   }

   public void toggleDebug() {
      this.stage.setDebugAll(this.debug = !this.debug);
   }

   public void onScienceChange() {
      this.copyButton.setVisible(Game.G.hasScience(Science.ScienceType.Blueprints));
      this.menu.onScienceChange();
      this.layerSelection.onScienceChange();
   }

   public static Cell<?> sep(Table t) {
      t.row();
      return t.add(new Image(Quarry.Q.skin.getDrawable("button"))).height(1.0F).top();
   }

   public static String formatResourceAmount(int amount) {
      return intFormat.format((long)amount);
   }

   public static String formatResourceAmount(float amount) {
      return intFormat.format((double)amount);
   }

   public static String formatResourceAmount(float amount, boolean fix) {
      return fix ? decFormat.format((double)amount) : intFormat.format((double)amount);
   }

   public static String formatPowerAmount(double rawAmount) {
      int amount = (int)Math.round(rawAmount);
      if (amount >= 1000000000) {
         return formatResourceAmount((float)Math.round((float)(amount / 100000000)) / 10.0F) + " GJ";
      } else if (amount >= 1000000) {
         return formatResourceAmount((float)Math.round((float)(amount / 100000)) / 10.0F) + " MJ";
      } else {
         return amount >= 1000 ? formatResourceAmount((float)Math.round((float)(amount / 100)) / 10.0F) + " kJ" : formatResourceAmount(amount) + " J";
      }
   }

   public static NinePatchDrawable getDrawable(Skin skin, String name, int padTop, int padLeft, int padRight, int padBot) {
      String key = name + "-" + padTop + "-" + padLeft + "-" + padRight + "-" + padBot;
      NinePatchDrawable d = drawableCache.get(key);
      if (d != null) {
         return d;
      } else {
         d = (NinePatchDrawable)skin.newDrawable(name);
         d.setMinWidth(1.0F);
         d.setMinHeight(1.0F);
         d.setTopHeight((float)padTop);
         d.setLeftWidth((float)padLeft);
         d.setRightWidth((float)padRight);
         d.setBottomHeight((float)padBot);
         drawableCache.put(key, d);
         return d;
      }
   }

   public static Table renderRecipe(Skin skin, RecipeList.Recipe recipe, boolean singleMode) {
      Table table = new Table();
      table.setBackground(skin.getDrawable("panel_metal"));
      table.pad(20.0F);
      table.defaults();
      if (recipe.getInput() != null) {
         Table inputs = new Table();
         inputs.defaults().left();

         for (Item.Items.Amount e : recipe.getInput().entries) {
            boolean fluid = e.getCat() == Item.ItemCategory.Fluid || e.getItem() != null && e.getItem().categories.contains(Item.ItemCategory.Fluid);
            String text = formatResourceAmount(fluid ? (float)e.getAmount() / 1000.0F : (float)e.getAmount());
            if (fluid) {
               text = text + "L";
            }

            if (e instanceof ConstantSupplyAmount) {
               text = text + "/s";
            }

            if (e.getCat() != null) {
               inputs.add(createResourceTable(32, skin, e.getCat(), text)).padRight(5.0F);
            } else {
               inputs.add(createResourceTable(32, skin, e.getItem(), text)).padRight(5.0F);
            }
         }

         table.add(inputs).expandX();
      }

      table.row();
      Table t = new Table();
      if (recipe.getPower() > 0.0F && !(recipe instanceof GeneratorStructure.GeneratorRecipe)) {
         Cell<?> c = t.add(
            createResourceTable(25, skin, skin.getDrawable("icon_power"), formatPowerAmount((double)(recipe.getPower() * 60.0F)) + "/s", "small-font")
         );
         if (recipe.getInput() == null) {
            c.padRight(5.0F);
         }
      }

      if (recipe.getInput() != null) {
         Image arrow = new Image(skin.getDrawable("symb_rightarrow"));
         arrow.setOrigin(12.0F, 7.0F);
         arrow.setRotation(-90.0F);
         t.add(arrow).size(24.0F, 15.0F).pad(10.0F);
      }

      Table timeTable = createResourceTable(25, skin, skin.getDrawable("icon_time"), formatResourceAmount(recipe.workingTime) + "s", "small-font");
      ((Cell)timeTable.getCells().get(1)).getActor().setName("time");
      t.add(timeTable);
      table.add(t);
      if (recipe.getOutput() != null || recipe instanceof GeneratorStructure.GeneratorRecipe) {
         table.row();
         Table outputs = new Table();
         outputs.defaults().left();
         if (recipe instanceof GeneratorStructure.GeneratorRecipe) {
            outputs.add(
                  createResourceTable(
                     32,
                     skin,
                     skin.getDrawable("icon_power"),
                     formatPowerAmount((double)((GeneratorStructure.GeneratorRecipe)recipe).getPowerGeneration()) + "/s"
                  )
               )
               .padRight(5.0F);
         }

         if (recipe.getOutput() != null) {
            for (Item.Items.Amount e : recipe.getOutput().entries) {
               boolean fluidx = e.getCat() == Item.ItemCategory.Fluid || e.getItem() != null && e.getItem().categories.contains(Item.ItemCategory.Fluid);
               String textx = formatResourceAmount(fluidx ? (float)e.getAmount() / 1000.0F : (float)e.getAmount());
               if (fluidx) {
                  textx = textx + "L";
               }

               if (e.getCat() != null) {
                  outputs.add(createResourceTable(32, skin, e.getCat(), textx)).padRight(5.0F);
               } else {
                  outputs.add(createResourceTable(32, skin, e.getItem(), textx)).padRight(5.0F);
               }
            }
         }

         Cell<?> c = table.add(outputs).center();
         if (recipe.getInput() == null) {
            c.expandX();
         }
      }

      return table;
   }

   public static Table createResourceTable(int iconSize, Skin skin, String icon, String text) {
      return createResourceTable(iconSize, skin, skin.getDrawable(icon), text);
   }

   public static Table createResourceTable(int iconSize, Skin skin, Drawable icon, String text, Object... userObject) {
      return createResourceTable(iconSize, skin, icon, text, "default-font", userObject);
   }

   public static Table createResourceTable(int iconSize, Skin skin, Drawable icon, String text, String font, Object... userObject) {
      Table t = new Table();
      if (userObject.length > 0) {
         t.setUserObject(userObject[0]);
      }

      Image i = new Image(icon);
      t.add(i).size((float)iconSize);
      Label l = new Label(text, skin, font, Color.WHITE);
      l.setAlignment(16);
      t.add(l).height((float)(iconSize + 4)).padLeft(5.0F).right().grow();
      return t;
   }

   public static Table createResourceTable(int iconSize, Skin skin, Item.ItemType item, String text, Object... userObject) {
      Table t = createResourceTable(iconSize, skin, item.drawable, text, item, userObject);
      t.setTouchable(Touchable.enabled);
      t.addListener(new TextTooltip(text.trim() + " " + item.title, skin));
      return t;
   }

   public static Table createResourceTable(int iconSize, Skin skin, Item.ItemCategory cat, String text, Object... userObject) {
      Table t = createResourceTable(iconSize, skin, cat.drawable, text, userObject);
      t.setTouchable(Touchable.enabled);
      t.addListener(new TextTooltip(text.trim() + " " + cat.title, skin));
      return t;
   }
}
