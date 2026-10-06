package de.dakror.quarry.scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import de.dakror.common.Callback;
import de.dakror.common.Response;
import de.dakror.common.debug.Delta;
import de.dakror.common.libgdx.ChangeNotifier;
import de.dakror.common.libgdx.EditorCameraControl;
import de.dakror.common.libgdx.Pair;
import de.dakror.common.libgdx.audio.AmbientSound;
import de.dakror.common.libgdx.io.ByteArrayFileHandle;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.math.AStar;
import de.dakror.common.libgdx.render.BatchDelegate;
import de.dakror.common.libgdx.render.DepthSpriter;
import de.dakror.common.libgdx.ui.ColorUtil;
import de.dakror.common.libgdx.ui.GameScene;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Chunk;
import de.dakror.quarry.game.Generator;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.LoadingCompat;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.game.SimHash;
import de.dakror.quarry.game.SimRandom;
import de.dakror.quarry.game.Tile;
import de.dakror.quarry.game.power.Edge;
import de.dakror.quarry.game.power.PowerGrid;
import de.dakror.quarry.game.power.PowerNetwork;
import de.dakror.quarry.net.NetCommand;
import de.dakror.quarry.net.NetSession;
import de.dakror.quarry.structure.Boiler;
import de.dakror.quarry.structure.DistillationColumn;
import de.dakror.quarry.structure.Refinery;
import de.dakror.quarry.structure.ShaftDrillHead;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.IRotatable;
import de.dakror.quarry.structure.base.ProducerStructure;
import de.dakror.quarry.structure.base.RecipeList;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.StorageStructure;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.structure.base.component.CInventory;
import de.dakror.quarry.structure.base.component.CSingleInventory;
import de.dakror.quarry.structure.logistics.BrickChannel;
import de.dakror.quarry.structure.logistics.Conveyor;
import de.dakror.quarry.structure.logistics.ConveyorBridge;
import de.dakror.quarry.structure.logistics.ElectricConveyorCore;
import de.dakror.quarry.structure.logistics.Filter;
import de.dakror.quarry.structure.logistics.Hopper;
import de.dakror.quarry.structure.logistics.ItemLift;
import de.dakror.quarry.structure.logistics.ItemLiftBelow;
import de.dakror.quarry.structure.logistics.TubeShaft;
import de.dakror.quarry.structure.logistics.TubeShaftBelow;
import de.dakror.quarry.structure.power.CableShaft;
import de.dakror.quarry.structure.power.CableShaftBelow;
import de.dakror.quarry.structure.power.CopperCable;
import de.dakror.quarry.structure.power.GasTurbine;
import de.dakror.quarry.structure.power.HighPowerShaft;
import de.dakror.quarry.structure.power.HighPowerShaftBelow;
import de.dakror.quarry.structure.power.PowerPole;
import de.dakror.quarry.structure.power.SolarPanel;
import de.dakror.quarry.structure.power.SolarPanelOutlet;
import de.dakror.quarry.structure.power.Substation;
import de.dakror.quarry.structure.producer.AirPurifier;
import de.dakror.quarry.structure.producer.Excavator;
import de.dakror.quarry.structure.producer.Mine;
import de.dakror.quarry.structure.producer.OilWell;
import de.dakror.quarry.structure.storage.Barrel;
import de.dakror.quarry.structure.storage.Storage;
import de.dakror.quarry.structure.storage.Tank;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.QuarrySoundPlayer;
import de.dakror.quarry.util.SpriterDelegateBatch;
import de.dakror.quarry.util.StructureSoundSpatializer;
import de.dakror.quarry.util.Util;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Game extends GameScene {
   public static boolean GOD_MODE = false;
   public static boolean DRAW_DEBUG = false;
   public static boolean FLUIDMODE = false;
   public static boolean FILLMODE = false;
   public static boolean RECORDMODE = false;
   public static boolean SCREENSHOT = false;
   public static boolean FOGMODE = false;
   public static volatile boolean SINGLE_FRAME = false;
   public static boolean LOCKSTEP = Quarry.Q.prefs.getBoolean("lockstep", false);
   public static boolean UI_VISIBLE = true;
   public static boolean SMOOTH_CAMERA = false;
   public static boolean INCREASE_MODE = Quarry.Q.prefs.getBoolean("increase_mode", false);
   private static final Pattern fileRegex = Pattern.compile("[^0-9a-zA-Z-_]");
   private static final HashMap<String, Texture> saveThumbnailCache = new HashMap<>();
   private static final Object layerLock = new Object();
   private static final Object resourceLock = new Object();
   private static final Object highlightLock = new Object();
   public static final Object renderThreadLock = new Object();
   static final Bounds tempBounds = new Bounds();
   public static Game G;
   Batch batch;
   BatchDelegate delegate;
   OrthographicCamera cam;
   OrthographicCamera fboCam;
   ShaderProgram colorShader;
   ShapeRenderer shaper;
   DepthSpriter spriter;
   SpriterDelegateBatch pfxBatch;
   public GameUi ui;
   public InputMultiplexer input;
   public final Array<Runnable> renderThreadTasks = new Array();
   public Viewport viewport;
   private volatile Array<Layer> layers;
   private Game.LayerIterable layerIter = new Game.LayerIterable();
   FrameBuffer[] chunkFBOs;
   int layerIndex;
   public Layer layer;
   int deltaLayer;
   public final PowerGrid powerGrid = new PowerGrid();
   AStar<Integer> tilePathfinding;
   AStar.Network<Integer> tileNetwork = new AStar.Network<Integer>() {
      public float getH(Integer start, Integer end) {
         int x1 = start / Game.this.layer.height;
         int y1 = start % Game.this.layer.height;
         int x2 = end / Game.this.layer.height;
         int y2 = end % Game.this.layer.height;
         return (float)(Math.abs(x2 - x1) + Math.abs(y2 - y1));
      }

      public float getEdgeLength(Integer start, Integer end) {
         return Game.this.layer.get(end / Game.this.layer.height, end % Game.this.layer.height) != Tile.TileType.Stone ? 1.25F : 1.0F;
      }

      public void visitNeighbors(Integer node, Integer start, Integer end, AStar.Visitor<Integer> visitor) {
         int x1 = node / Game.this.layer.height;
         int y1 = node % Game.this.layer.height;

         for (Direction d : Direction.values) {
            int x2 = x1 + d.dx;
            int y2 = y1 + d.dy;
            if (x2 >= 0 && y2 >= 0 && x2 < Game.this.layer.width && y2 < Game.this.layer.height) {
               int index = x2 * Game.this.layer.height + y2;
               if (Game.this.trailStructureCanBePlaced(index, start, end)) {
                  visitor.visit(index);
               }
            }
         }
      }
   };
   AStar.Network<Integer> cableNetwork = new AStar.Network<Integer>() {
      public float getH(Integer start, Integer end) {
         int x1 = start / Game.this.layer.height;
         int y1 = start % Game.this.layer.height;
         int x2 = end / Game.this.layer.height;
         int y2 = end % Game.this.layer.height;
         return (float)(Math.abs(x2 - x1) + Math.abs(y2 - y1));
      }

      public float getEdgeLength(Integer start, Integer end) {
         return 1.0F;
      }

      public void visitNeighbors(Integer node, Integer start, Integer end, AStar.Visitor<Integer> visitor) {
         int x1 = node / Game.this.layer.height;
         int y1 = node % Game.this.layer.height;

         for (Direction d : Direction.values) {
            int x2 = x1 + d.dx;
            int y2 = y1 + d.dy;
            if (x2 >= 0 && y2 >= 0 && x2 < Game.this.layer.width && y2 < Game.this.layer.height) {
               int index = x2 * Game.this.layer.height + y2;
               if (Game.this.trailStructureCanBePlaced(index, start, end)) {
                  visitor.visit(index);
               }
            }
         }
      }
   };
   public EnumSet<Science.ScienceType> sciences;
   public EnumSet<Science.ScienceType> currentSciences;
   protected EnumMap<Item.ItemType, Integer> resources;
   protected EnumSet<Item.ItemType> seenResources;
   FrameBuffer fbo;
   OrthographicCamera thumbCam;
   boolean saveMap;
   Callback<Void> saveCallback;
   final ByteArrayFileHandle bafh = new ByteArrayFileHandle();
   String saveName;
   long playTime;
   long lastTimerStart;
   FrameBuffer recordFbo;
   OrthographicCamera recordCam;
   long lastScreenshot;
   FrameBuffer screenshotFbo;
   OrthographicCamera screenshotCam;
   public String currentGameName;
   public Structure<?> activeStructure;
   public final IntMap<Structure<?>> activeStructureTrail = new IntMap();
   public final List<Integer> activeStructurePath = new LinkedList<>();
   public boolean structureDestroyMode;
   public boolean cableDestroyMode;
   public boolean bulkDestroyMode;
   public boolean bulkCableMode;
   public boolean copyMode;
   public boolean pasteMode;
   public Game.QuarryCameraControl camControl;
   protected TextureRegion caret;
   public final Vector2 endA = new Vector2(-1.0F, 0.0F);
   public final Vector2 endB = new Vector2(-1.0F, 0.0F);
   int activeEnd = 0;
   public IntMap<Pair<Structure<?>, NBT.CompoundTag>> copyStructures = new IntMap();
   public IntMap<Pair<Structure<?>, NBT.CompoundTag>> copyCables = new IntMap();
   List<Structure<?>> copyCollisions = new ArrayList<>();
   public int[] copyRegion = new int[4];
   EnumMap<Item.ItemType, Integer> copyCost;
   final TextureRegion endMarker = Quarry.Q.atlas.findRegion("end_marker");
   final TextureRegion endMarkerGlow = Quarry.Q.atlas.findRegion("end_marker_glow");
   final TextureRegion endMarkerA = Quarry.Q.atlas.findRegion("end_marker_a");
   final Vector2 activeTile = new Vector2(-1.0F, 0.0F);
   int hoverTileX;
   int hoverTileY;
   private volatile int gameSpeed = 1;
   private volatile boolean gamePaused = false;
   public static final double TICK = 1.0 / 60.0;
   private static final long TICK_NANOS = (long)(TICK * 1.0E9);
   /** 等对端命令包时的重试间隔（毫秒）。 */
   private static final long WAIT_RETRY_MILLIS = 2L;
   /** 节拍落后超过这个量就丢弃积压，不做"追帧"。 */
   private static final long MAX_TICK_LAG_NANOS = 200_000_000L;
   /**
    * 模拟线程与渲染线程的分界锁，也是全部世界状态的保护锁。
    * 约定：它是**最外层锁**——layerLock/resourceLock/pfxLock 等都在它内部获取，
    * 任何路径都不允许"先持其它锁再拿 simLock"，否则会死锁。
    */
   private final Object simLock = new Object();
   private volatile boolean simThreadRunning;
   private volatile Thread simThread;
   /** 渲染线程句柄。UI 相关方法用它判断"我现在是不是在渲染线程上"。 */
   private static volatile Thread renderThread;
   public volatile long simTick;
   private boolean startNewGame;
   private boolean infinite;
   private Game.LiningUpState liningUpInput = Game.LiningUpState.NoOther;
   private Game.LiningUpState liningUpOutput = Game.LiningUpState.NoOther;
   private Structure<?> liningUpStructureInput;
   private Structure<?> liningUpStructureOutput;
   final Vector2 tmp = new Vector2();
   ChangeNotifier<Pair<Item.ItemType, Integer>> resourceChangeNotifier = new ChangeNotifier<>();
   final Pair<Item.ItemType, Integer> resourceChangePair = new Pair<>();
   public ChangeNotifier<Layer> layerChangeNotifier = new ChangeNotifier<>();
   public ParticleEffectPool dustPfxU;
   public ParticleEffectPool dustPfxD;
   public ParticleEffectPool dustPfxL;
   public ParticleEffectPool dustPfxR;
   public ParticleEffectPool firePfx;
   public ParticleEffect confettiPfx;
   IntSet tutorialHighlight = new IntSet();
   StructureSoundSpatializer spatializer;
   public QuarrySoundPlayer spatializedPlayer;
   Sound buildSfx;
   Sound destroySfx;
   Sound cableSfx;
   AmbientSound heavyAmb;
   AmbientSound emptyAmb;
   AmbientSound baseAmb;
   Music music;
   Thread loudnessCalculatorThread;
   long lastAutosave;
   boolean autosaving;
   Vector3 cameraVelocity = new Vector3();
   final Vector3 tmp3 = new Vector3();
   float cameraAcc = 400.0F;
   float cameraMaxSpeed = 600.0F;
   float cameraZoomAcc = 0.0F;

   @Override
   public void init() {
      G = this;
      renderThread = Thread.currentThread();
      this.music = (Music)Quarry.Q.assets.get("music/Fading_into_the_Dream" + Const.MUSIC_FORMAT, Music.class);
      this.input = new InputMultiplexer(new InputProcessor[]{this});
      this.colorShader = new ShaderProgram(Gdx.files.internal("glsl/color.vs"), Gdx.files.internal("glsl/color.fs"));
      if (!this.colorShader.isCompiled()) {
         throw new IllegalArgumentException("Error compiling shader: " + this.colorShader.getLog());
      } else {
         this.batch = new SpriteBatch(8191, this.colorShader);
         this.cam = new OrthographicCamera();
         this.shaper = new ShapeRenderer();
         this.shaper.setAutoShapeType(true);
         this.spriter = new DepthSpriter((Texture)Quarry.Q.atlas.getTextures().first(), 8191);
         this.delegate = new BatchDelegate((Texture)Quarry.Q.atlas.getTextures().first(), this.batch);
         this.pfxBatch = new SpriterDelegateBatch(this.spriter);
         this.fboCam = new OrthographicCamera(2048.0F, 2048.0F);
         this.viewport = new FitViewport(Const.W, Const.H, this.cam);
         this.fboCam.position.set(1024.0F, 1024.0F, this.fboCam.near);
         this.fboCam.update();
         this.caret = Quarry.Q.atlas.findRegion("caret_down");
         this.currentSciences = EnumSet.noneOf(Science.ScienceType.class);
         this.sciences = EnumSet.noneOf(Science.ScienceType.class);
         this.resources = new EnumMap<>(Item.ItemType.class);
         this.seenResources = EnumSet.noneOf(Item.ItemType.class);
         this.copyCost = new EnumMap<>(Item.ItemType.class);
         this.initParticles();
         this.ui = new GameUi(this.viewport);
         this.camControl = new Game.QuarryCameraControl(this.viewport);
         this.input.addProcessor(this.camControl.input);
         this.chunkFBOs = new FrameBuffer[4];
         this.spatializer = new StructureSoundSpatializer();
         this.spatializer.setVerticalRange(8.0F);
         this.spatializer.setHorizontalRange(8.0F);
         this.spatializedPlayer = new QuarrySoundPlayer();
         this.spatializedPlayer.setSpatializer(this.spatializer);
         this.spatializedPlayer.setVolume(Quarry.Q.sound.getSoundVolume());
         this.spatializedPlayer.setFadeTime(0.15F);
         this.buildSfx = (Sound)Quarry.Q.assets.get("sfx/build" + Const.SFX_FORMAT, Sound.class);
         this.destroySfx = (Sound)Quarry.Q.assets.get("sfx/destroy" + Const.SFX_FORMAT, Sound.class);
         this.cableSfx = (Sound)Quarry.Q.assets.get("sfx/cable" + Const.SFX_FORMAT, Sound.class);
         this.emptyAmb = new AmbientSound((Sound)Quarry.Q.assets.get("sfx/ambience_empty" + Const.SFX_FORMAT, Sound.class), 0.25F);
         this.baseAmb = new AmbientSound((Sound)Quarry.Q.assets.get("sfx/ambience_base" + Const.SFX_FORMAT, Sound.class), 0.25F);
         this.heavyAmb = new AmbientSound((Sound)Quarry.Q.assets.get("sfx/ambience_heavy" + Const.SFX_FORMAT, Sound.class), 0.25F);
         this.tilePathfinding = new AStar<>();
         this.tilePathfinding.setMaxTime(100L);
      }

      this.installLockedInput();
      this.startSimThread();
   }

   // ---------- 模拟线程：固定 60Hz 推进世界，与渲染线程解耦 ----------

   private void startSimThread() {
      if (this.simThread != null) {
         return;
      }

      this.simThreadRunning = true;
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            Game.this.simLoop();
         }
      }, "sim");
      t.setDaemon(true);
      this.simThread = t;
      t.start();
   }

   private void stopSimThread() {
      this.simThreadRunning = false;
      Thread t = this.simThread;
      this.simThread = null;
      if (t != null) {
         t.interrupt();
      }
   }

   /**
    * 固定步长模拟循环。窗口失焦/最小化时渲染线程会卡在 GL 呈现上，但那次阻塞发生在
    * listener.render() 之外，不持有 simLock，所以本线程照常推进——这正是"失焦即冻结"的解法。
    */
   private void simLoop() {
      long next = System.nanoTime();
      while (this.simThreadRunning) {
         boolean did = false;
         synchronized (this.simLock) {
            if (this.layers == null || this.worldApplying) {
               // 还没开局，或者正在替换世界（联机收世界/重同步），空转等待
            } else if (SINGLE_FRAME) {
               SINGLE_FRAME = false;
               if (NetSession.canAdvance(this.simTick)) {
                  this.stepSimulation(TICK, this.gameSpeed);
                  did = true;
               } else {
                  NetSession.noteWaiting(this.simTick);
               }
            } else if (NetSession.canAdvance(this.simTick)) {
               this.stepSimulation(TICK, this.gamePaused ? 0 : this.gameSpeed);
               did = true;
            } else {
               // 联机：对端这一 tick 的命令包还没到，停下等，绝不越过 tick
               NetSession.noteWaiting(this.simTick);
            }
         }

         long now = System.nanoTime();
         if (did) {
            next += TICK_NANOS;
            long sleep = next - now;
            if (sleep > 0L) {
               LockSupport.parkNanos(sleep);
            } else if (sleep < -MAX_TICK_LAG_NANOS) {
               next = now;   // 落后太多就重新对表，不做追帧
            }
         } else {
            next = now;
            try {
               Thread.sleep(WAIT_RETRY_MILLIS);
            } catch (InterruptedException e) {
               Thread.currentThread().interrupt();
               return;
            }
         }
      }
   }

   /** 把一段 UI/音频操作回迁到渲染线程执行——模拟线程碰 UI 必须先经此中转。 */
   public void postUi(final Runnable r) {
      Gdx.app.postRunnable(new Runnable() {
         @Override
         public void run() {
            synchronized (Game.this.simLock) {
               r.run();
            }
         }
      });
   }

   /** 当前线程是不是渲染线程。UI 方法用它决定是否需要把工作回迁。 */
   public static boolean isRenderThread() {
      return Thread.currentThread() == renderThread;
   }

   /** 世界正在被替换（联机收世界/重同步），模拟线程在此期间必须停 tick。 */
   private volatile boolean worldApplying;

   /** 供其它线程（如网络线程的 runnable）在与模拟互斥的前提下读世界状态。 */
   public void withSimLock(Runnable r) {
      synchronized (this.simLock) {
         r.run();
      }
   }

   /** 清空所有结构的瞬态历史（滑动窗口、倒计时等）。主机发世界前调用，让序列化结果与客机全新加载一致。 */
   public void clearTransientSimState() {
      synchronized (layerLock) {
         if (this.layers == null) {
            return;
         }

         for (Layer l : this.layers) {
            l.clearTransientState();
         }
      }
   }

   /**
    * 把输入回调纳入 simLock。输入事件在 LwjglInput.processEvents() 里派发，那一步在
    * listener.render() 之外，不在 update/draw 的锁范围内，所以必须单独包一层。
    */
   private void installLockedInput() {
      final InputProcessor inner = Gdx.input.getInputProcessor();
      if (inner == null) {
         return;
      }

      Gdx.input.setInputProcessor(new InputProcessor() {
         @Override
         public boolean keyDown(int keycode) {
            synchronized (Game.this.simLock) {
               return inner.keyDown(keycode);
            }
         }

         @Override
         public boolean keyUp(int keycode) {
            synchronized (Game.this.simLock) {
               return inner.keyUp(keycode);
            }
         }

         @Override
         public boolean keyTyped(char character) {
            synchronized (Game.this.simLock) {
               return inner.keyTyped(character);
            }
         }

         @Override
         public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            synchronized (Game.this.simLock) {
               return inner.touchDown(screenX, screenY, pointer, button);
            }
         }

         @Override
         public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            synchronized (Game.this.simLock) {
               return inner.touchUp(screenX, screenY, pointer, button);
            }
         }

         @Override
         public boolean touchDragged(int screenX, int screenY, int pointer) {
            synchronized (Game.this.simLock) {
               return inner.touchDragged(screenX, screenY, pointer);
            }
         }

         @Override
         public boolean mouseMoved(int screenX, int screenY) {
            synchronized (Game.this.simLock) {
               return inner.mouseMoved(screenX, screenY);
            }
         }

         @Override
         public boolean scrolled(int amount) {
            synchronized (Game.this.simLock) {
               return inner.scrolled(amount);
            }
         }
      });
   }

   protected void initParticles() {
      ParticleEffect fire = new ParticleEffect();
      fire.load(Gdx.files.internal("pfx/oven_fire.p"), Quarry.Q.atlas);
      this.firePfx = new ParticleEffectPool(fire, 16, 100);
      this.confettiPfx = new ParticleEffect();
      this.confettiPfx.load(Gdx.files.internal("pfx/confetti.p"), Quarry.Q.atlas);
      ParticleEmitter pe = new ParticleEmitter((ParticleEmitter)this.confettiPfx.getEmitters().first());
      this.confettiPfx.getEmitters().clear();

      for (int r = 0; r <= 360; r += 45) {
         Color c = r == 360 ? Color.WHITE : ColorUtil.HSVtoRGB((float)r, 100.0F, 100.0F);
         ParticleEmitter p = new ParticleEmitter(pe);
         p.getTint().setColors(new float[]{c.r, c.g, c.b});
         this.confettiPfx.getEmitters().add(p);
      }

      ParticleEffect dust = new ParticleEffect();
      dust.load(Gdx.files.internal("pfx/dust.p"), Quarry.Q.atlas);
      this.dustPfxU = new ParticleEffectPool(dust, 16, 100);
      ParticleEffect dust1 = new ParticleEffect();
      dust1.load(Gdx.files.internal("pfx/dust.p"), Quarry.Q.atlas);
      ((ParticleEmitter)dust1.getEmitters().get(0))
         .getVelocity()
         .setHigh(
            -((ParticleEmitter)dust1.getEmitters().get(0)).getVelocity().getHighMin(),
            -((ParticleEmitter)dust1.getEmitters().get(0)).getVelocity().getHighMax()
         );
      this.dustPfxD = new ParticleEffectPool(dust1, 16, 100);
      ParticleEffect dust2 = new ParticleEffect();
      dust2.load(Gdx.files.internal("pfx/dust_h.p"), Quarry.Q.atlas);
      this.dustPfxL = new ParticleEffectPool(dust2, 16, 100);
      ParticleEffect dust3 = new ParticleEffect();
      dust3.load(Gdx.files.internal("pfx/dust_h.p"), Quarry.Q.atlas);
      ((ParticleEmitter)dust3.getEmitters().get(0))
         .getVelocity()
         .setHigh(
            -((ParticleEmitter)dust3.getEmitters().get(0)).getVelocity().getHighMin(),
            -((ParticleEmitter)dust3.getEmitters().get(0)).getVelocity().getHighMax()
         );
      this.dustPfxR = new ParticleEffectPool(dust3, 16, 100);
   }

   public void reset() {
      synchronized (this.simLock) {
         this.resetLocked();
      }
   }

   private void resetLocked() {
      this.ui.tileUI.hide();
      this.ui.tutorial.reset();
      this.resetActiveTile();
      this.resetActiveStructure();
      this.ui.destroyButton.setChecked(false);
      this.ui.cableRemoveButton.setChecked(false);
      this.ui.hideBuildMenu(true);
      this.ui.hideScienceUI(true);
      this.ui.hideStructureUI();
      this.ui.hideTooltip();
      this.ui.hideCopyTable();
      this.tutorialHighlight.clear();
      this.powerGrid.clear();
      this.currentSciences.clear();
      this.sciences.clear();
      this.resources.clear();
      this.getSeenResources().clear();

      try {
         this.spatializedPlayer.stop();
      } catch (Exception var3) {
      }

      this.heavyAmb.stop();
      this.emptyAmb.stop();
      this.baseAmb.stop();
      this.ui.pauseButton.setChecked(false);
      this.ui.destroyButton.setDisabled(false);
      this.ui.destroyButton.setVisible(true);
      this.ui.copyButton.setVisible(false);

      for (Actor c : this.ui.buildMenuStars) {
         c.remove();
      }

      this.ui.buildMenuStars.clear();
      this.ui.buildMenuSciences.clear();
   }

   public void newGame() {
      synchronized (this.simLock) {
         this.newGameLocked();
      }
   }

   private void newGameLocked() {
      this.currentGameName = null;
      this.setPaused(false);
      this.reset();
      this.lastAutosave = System.currentTimeMillis();
      this.addScience(Science.ScienceType.Start);
      this.infinite = false;
      Layer l0 = null;
      synchronized (layerLock) {
         if (this.layers == null) {
            this.layers = new Array();
         } else {
            for (Layer l : this.layers) {
               l.dispose();
            }

            this.layers.clear();
         }

         l0 = new Layer(0, 64, 64, Tile.TileType.Stone, true, true);
         Storage st = new Storage(31, 31);
         st.setRefundStorage(true);
         l0.addStructure(st);
         this.layers.add(l0);
         st.addToInventory(Item.ItemType.Wood, 40, null);
         st.addToInventory(Item.ItemType.Scaffolding, 40, null);
         st.addToInventory(Item.ItemType.Stone, 20, null);
      }

      this.layer = l0;
      this.layerIndex = 0;
      this.playTime = 0L;
      if (Quarry.Q.prefs.getBoolean("tutorial", true)) {
         Generator.G.setSeed(5044591726400517120L);
         this.ui.tutorial.next();
         Quarry.Q.prefs.putBoolean("tutorial", false).flush();
      }

      SimRandom.G.setSeed(Generator.G.getSeed());
      Generator.G.generate(l0);
      this.cam.zoom = 1.0F;
      this.cam.position.set(((float)l0.width + 0.75F) * 64.0F / 2.0F, (float)(l0.height * 64 / 2), 0.0F);
      this.layerChangeNotifier.notify(ChangeNotifier.Event.Type.RESET, this.layer);
      this.lastTimerStart = System.currentTimeMillis();
   }

   public void startNewGame() {
      this.startNewGame = true;
   }

   @Override
   public void show() {
      super.show();
      Quarry.Q.sound.playMusic(this.music, true);
      this.ui.menu.init();
      Gdx.input.setCatchBackKey(true);
      int chunksH = (int)Math.ceil(2.0);

      for (int i = 0; i < 64; i += 32) {
         for (int j = 0; j < 64; j += 32) {
            this.chunkFBOs[i / 32 * chunksH + j / 32] = new FrameBuffer(Format.RGB565, 2048, 2048, false);
         }
      }
   }

   @Override
   public void hide() {
      super.hide();

      for (int i = 0; i < this.chunkFBOs.length; i++) {
         try {
            this.chunkFBOs[i].dispose();
         } catch (Exception var3) {
            var3.printStackTrace();
         }

         this.chunkFBOs[i] = null;
      }
   }

   /**
    * 走一步模拟。固定步长 + 固定倍率调用，让模拟不再依赖渲染帧率，这是回放与联机同步可复现的前提。
    * gameSpeed 保持「倍率」语义（一步里带上倍率），不是「多跑几步」，否则 100 倍速会变成每帧上千次 tick。
    */
   /**
    * 换层。原本写在 draw() 里（渲染线程、时机不确定），但 layerIndex 会影响 stepSimulation
    * （当前层跳过 postUpdate、并决定响度更新），所以必须放到 tick 边界执行，
    * 保证所有端在同一 tick 换层。draw() 只负责画。
    */
   private void applyLayerChange() {
      if (this.deltaLayer == 0) {
         return;
      }

      synchronized (layerLock) {
         int l = this.layerIndex + this.deltaLayer;
         if (l >= 0 && l < this.layers.size) {
            this.layerIndex += this.deltaLayer;
            this.layer = this.layers.get(this.layerIndex);

            for (Chunk c : this.layer.getChunks()) {
               c.dirty = true;
            }

            if (!this.layer.pendingBounds.isEmpty()) {
               this.layer.dirtyBounds.add(this.layer.pendingBounds);
               this.layer.pendingBounds.clear();
            }

            if (this.activeStructure != null) {
               this.camControl.updateTrail();
               this.camControl.updateActiveElementPlaceable();
            }

            // 这个通知不能在模拟线程上发：Layer.onChangeEvent 会 dispose 自己（释放 GL mesh），
            // LayerSelection 会改 Stage。两者都必须在渲染线程做，所以回迁。
            final Layer changed = this.layer;
            this.postUi(() -> this.layerChangeNotifier.notify(ChangeNotifier.Event.Type.CHANGE, changed));
         }

         this.deltaLayer = 0;
      }
   }

   private void stepSimulation(double dt, int speed) {
      this.applyLayerChange();
      if (NetSession.isActive()) {
         List<NetCommand> commands = NetSession.commandsFor(this.simTick);
         if (commands != null) {
            for (NetCommand command : commands) {
               this.applyNetCommand(command);
            }
         }
      }

      this.powerGrid.update(dt, speed);
      synchronized (layerLock) {
         for (Layer l : this.layers) {
            l.update(dt, speed);
            if (l.getIndex() != this.layerIndex) {
               l.postUpdate();
            }
         }
      }

      this.simTick++;
      if (NetSession.isActive()) {
         NetSession.onTickEnd(this.simTick);
         if (NetSession.isAnchored() && this.simTick % NetSession.hashInterval() == 0L) {
            NetSession.sendHash(this.simTick, this.simHash());
            // 诊断：每次发哈希都打印子系统拆分，联机分叉时靠它定位（对两个窗口的日志即可看出）
            System.out.println("[Net] debug " + this.simHashDebug());
         }
      }
   }

   /** 执行一条来自命令流的行为：两端在同一 tick、以同一顺序执行，所以结果一致。 */
   private void applyNetCommand(NetCommand command) {
      switch (command.type) {
         case NetCommand.PAUSE: {
            final boolean paused = command.p[0] == 1;
            this.applyPause(paused);
            // 模拟线程不能直接碰 Stage，回迁到渲染线程
            this.postUi(() -> this.ui.pauseButton.setChecked(paused));
            break;
         }
         case NetCommand.SPEED:
            this.applySpeed(command.p[0]);
            break;
         case NetCommand.PLACE:
            this.placeStructureFromCommand(command.p[0], command.p[1], command.p[2], command.p[3], command.p[4], command.p[5] == 1);
            break;
         case NetCommand.REMOVE:
            this.removeStructureFromCommand(command.p[0], command.p[1], command.p[2], command.p[3] == 1);
            break;
         default:
            break;
      }
   }

   // ---------- 命令执行侧：世界修改只在这里发生，且不读任何 UI/预览态 ----------

   /**
    * 按类型 id 造一个建筑（不加入世界）。走 Structure.types 注册表 + 反射构造，与读档同一条路径。
    * dirValue 的语义与发出方一致：IRotatable（传送带等）用它的 dir，其余建筑用 upDirection。
    */
   private Structure<?> createStructureFor(int typeId, int x, int y, int dirValue) {
      StructureType type = Structure.types[typeId & 65535];
      if (type == null) {
         return null;
      }

      try {
         Structure<?> s = (Structure<?>)type.constr.newInstance(x, y);
         Direction dir = Direction.values[dirValue & 255];
         if (s instanceof IRotatable) {
            // 传送带这类建筑的朝向存在 dir 里（存档写的也是 dir），不是 upDirection
            ((IRotatable)s).setRotation(dir);
         } else {
            s.setUpDirection(dir);
         }

         return s;
      } catch (Exception var7) {
         Quarry.Q.pi.message(1, var7);
         return null;
      }
   }

   /**
    * 命令执行侧的可用性判定：只看 resources 与 buildCosts。
    * 绝不能用 GameUi.canAffordStructure —— 它是 UI 派生值（还会按拖拽数量缩放），两端可能不同。
    */
   private boolean canAffordFor(Schema schema) {
      for (Item.Items.Amount e : schema.buildCosts.entries) {
         if (this.getResource(e.getItem()) < e.getAmount()) {
            return false;
         }
      }

      return true;
   }

   /**
    * 扣资源的「模拟核心」：只改 resources 与各层 Storage 库存，去掉 UI 副作用，
    * 并且**跳过调试用的 INCREASE_MODE**（命令执行必须在两端逐字面一致）。
    */
   private boolean removeResourcesCore(Item.ItemType item, int amount) {
      if (amount <= 0) {
         return true;
      }

      synchronized (resourceLock) {
         Integer val = this.resources.get(item);
         if (val == null) {
            return false;
         }

         val = Math.max(0, val - amount);
         this.resources.put(item, val);
         this.resourceChangeNotifier.notify(ChangeNotifier.Event.Type.REMOVE, this.resourceChangePair.set(item, amount));
         this.getSeenResources().add(item);
         int remaining = amount;
         synchronized (layerLock) {
            for (Layer layer : this.layers) {
               for (Chunk c : layer.getChunks()) {
                  if (c != null && c.isInit()) {
                     for (Structure<?> s : c.getStructures()) {
                        if (s instanceof StorageStructure) {
                           remaining = ((StorageStructure)s).removeFromInventoryWithRest(item, remaining);
                           if (remaining == 0) {
                              break;
                           }
                        }
                     }
                  }
               }
            }
         }

         return remaining <= 0;
      }
   }

   /** 退资源的「模拟核心」：只改 resources 与退款仓库，去掉 toast。 */
   private void addResourcesCore(Item.ItemType item, int amount) {
      if (amount <= 0) {
         return;
      }

      synchronized (resourceLock) {
         int remaining = amount;
         synchronized (layerLock) {
            label60:
            for (Layer layer : this.layerIter) {
               for (StorageStructure b : layer.storages) {
                  if (b.isRefundStorage() && (b.getSchema().type != StructureType.Barrel || !b.isEmpty())) {
                     remaining = b.addToInventoryWithRest(item, remaining);
                     if (remaining == 0) {
                        break label60;
                     }
                  }
               }
            }

            if (remaining > 0) {
               amount -= remaining;
            }
         }

         Integer val = this.resources.get(item);
         if (val == null) {
            val = 0;
         }

         this.resources.put(item, val + amount);
         this.resourceChangeNotifier.notify(ChangeNotifier.Event.Type.ADD, this.resourceChangePair.set(item, amount));
         this.getSeenResources().add(item);
      }
   }

   /**
    * 放置命令：自校验（资源 + 层内占用）后落到目标层。两端跑同一段代码，所以结果一致。
    * 占用判定直接复用 Layer.addStructure（内部已含越界与 footprint 重叠检查）。
    */
   public boolean placeStructureFromCommand(int layerIndex, int x, int y, int typeId, int upDir, boolean free) {
      Layer target = this.getLayer(layerIndex);
      if (target == null) {
         System.out.println("[Net] PLACE 失败: 层 " + layerIndex + " 不存在");
         return false;
      }

      Structure<?> s = this.createStructureFor(typeId, x, y, upDir);
      if (s == null) {
         System.out.println("[Net] PLACE 失败: 无法构造 typeId=" + typeId);
         return false;
      }

      if (!free && !this.canAffordFor(s.getSchema())) {
         System.out.println("[Net] PLACE 失败: 资源不足 " + s.getSchema().type.name + " @" + x + "," + y);

         for (Item.Items.Amount e : s.getSchema().buildCosts.entries) {
            System.out.println("[Net]    需要 " + e.getItem() + " x" + e.getAmount() + "，现有 " + this.getResource(e.getItem()));
         }

         return false;
      }

      if (!target.addStructure(s)) {
         System.out.println(
            "[Net] PLACE 失败: 位置不可用 " + s.getSchema().type.name + " @" + x + "," + y + "（越界或已被占用）"
         );
         return false;
      }

      if (!free) {
         for (Item.Items.Amount e : s.getSchema().buildCosts.entries) {
            this.removeResourcesCore(e.getItem(), e.getAmount());
         }
      }

      return true;
   }

   /** 拆除命令：与 deleteStructure 的退料规则一致，但不带音效/UI 副作用。 */
   public boolean removeStructureFromCommand(int layerIndex, int x, int y, boolean cable) {
      Layer target = this.getLayer(layerIndex);
      if (target == null) {
         return false;
      }

      Structure<?> s = cable ? target.getCable(x, y) : target.getStructure(x, y);
      if (s == null || s.getSchema().has(Schema.Flags.Indestructible)) {
         return false;
      }

      if (cable) {
         target.removeCable((CopperCable)s);
      } else {
         target.removeStructure(s);
      }

      int removedHoppers = target.removeAttachedHoppers(s);
      float factor = this.hasScience(Science.ScienceType.ConsiderateConstruction) ? 0.8F : 0.25F;

      for (Item.Items.Amount e : s.getSchema().buildCosts.entries) {
         if (e.getAmount() > 1) {
            this.addResourcesCore(e.getItem(), (int)Math.ceil((double)((float)e.getAmount() * factor)));
         }
      }

      for (Item.Items.Amount e : Hopper.classSchema.buildCosts.entries) {
         if (e.getAmount() > 1) {
            this.addResourcesCore(e.getItem(), (int)Math.ceil((double)((float)(e.getAmount() * removedHoppers) * factor)));
         }
      }

      return true;
   }

   @Override
   public void update(double deltaTime) {
      renderThread = Thread.currentThread();
      synchronized (this.simLock) {
         this.updateLocked(deltaTime);
      }
   }

   /** update 的本体。必须在 simLock 内调用（读世界状态、改相机/UI）。 */
   private void updateLocked(double deltaTime) {
      if (this.startNewGame) {
         this.newGame();
         this.startNewGame = false;
      }

      if (this.layers != null) {
         if (Quarry.Q.desktop && (this.ui.prompt.getStage() == null || !this.ui.prompt.isVisible()) && !this.ui.menu.menuButton.isChecked()) {
            if (!SMOOTH_CAMERA) {
               if (Gdx.input.isKeyPressed(29)) {
                  this.cam.position.x = (float)((double)this.cam.position.x - (double)(700.0F * this.cam.zoom) * deltaTime);
               }

               if (Gdx.input.isKeyPressed(32)) {
                  this.cam.position.x = (float)((double)this.cam.position.x + (double)(700.0F * this.cam.zoom) * deltaTime);
               }

               if (Gdx.input.isKeyPressed(51)) {
                  this.cam.position.y = (float)((double)this.cam.position.y + (double)(700.0F * this.cam.zoom) * deltaTime);
               }

               if (Gdx.input.isKeyPressed(47)) {
                  this.cam.position.y = (float)((double)this.cam.position.y - (double)(700.0F * this.cam.zoom) * deltaTime);
               }
            } else {
               this.cam.position.add(this.tmp3.set(this.cameraVelocity.x, this.cameraVelocity.y, 0.0F).scl((float)deltaTime));
               this.camControl.clampCam(this.cam);
               this.camControl.clampZoom(this.cam.zoom + this.cameraVelocity.z);
               if (Gdx.input.isKeyPressed(29)) {
                  this.cameraVelocity.x = (float)Math.max(
                     (double)(-this.cameraMaxSpeed),
                     (double)this.cameraVelocity.x
                        - (double)this.cameraAcc * deltaTime * (double)this.cam.zoom * (double)(this.cameraVelocity.y > 0.0F ? 2 : 1)
                  );
               } else if (Gdx.input.isKeyPressed(32)) {
                  this.cameraVelocity.x = (float)Math.min(
                     (double)this.cameraMaxSpeed,
                     (double)this.cameraVelocity.x
                        + (double)this.cameraAcc * deltaTime * (double)this.cam.zoom * (double)(this.cameraVelocity.y < 0.0F ? 2 : 1)
                  );
               } else if (Math.abs(this.cameraVelocity.x) < 1.0F) {
                  this.cameraVelocity.x = 0.0F;
               } else {
                  this.cameraVelocity.x = (float)(
                     (double)this.cameraVelocity.x
                        + Math.min((double)this.cameraAcc * deltaTime * (double)this.cam.zoom, (double)Math.abs(this.cameraVelocity.x))
                           * (double)(-Math.signum(this.cameraVelocity.x))
                  );
               }

               if (Gdx.input.isKeyPressed(47)) {
                  this.cameraVelocity.y = (float)Math.max(
                     (double)(-this.cameraMaxSpeed),
                     (double)this.cameraVelocity.y
                        - (double)this.cameraAcc * deltaTime * (double)this.cam.zoom * (double)(this.cameraVelocity.y > 0.0F ? 2 : 1)
                  );
               } else if (Gdx.input.isKeyPressed(51)) {
                  this.cameraVelocity.y = (float)Math.min(
                     (double)this.cameraMaxSpeed,
                     (double)this.cameraVelocity.y
                        + (double)this.cameraAcc * deltaTime * (double)this.cam.zoom * (double)(this.cameraVelocity.y < 0.0F ? 2 : 1)
                  );
               } else if (Math.abs(this.cameraVelocity.y) < 1.0F) {
                  this.cameraVelocity.y = 0.0F;
               } else {
                  this.cameraVelocity.y = (float)(
                     (double)this.cameraVelocity.y
                        + Math.min((double)this.cameraAcc * deltaTime * (double)this.cam.zoom, (double)Math.abs(this.cameraVelocity.y))
                           * (double)(-Math.signum(this.cameraVelocity.y))
                  );
               }

               if (Math.abs(this.cameraZoomAcc) < 1.0E-5F) {
                  this.cameraZoomAcc = 0.0F;
               } else {
                  this.cameraZoomAcc = (float)(
                     (double)this.cameraZoomAcc + Math.min((double)Math.abs(this.cameraZoomAcc), 20.0 * deltaTime) * (double)(-Math.signum(this.cameraZoomAcc))
                  );
               }

               this.cameraVelocity.z = (float)((double)this.cameraVelocity.z + (double)this.cameraZoomAcc * deltaTime * 0.001F);
               if (this.cameraZoomAcc == 0.0F) {
                  this.cameraVelocity.z = (float)(
                     (double)this.cameraVelocity.z
                        + Math.min((double)Math.abs(this.cameraVelocity.z), 0.02F * deltaTime) * (double)(-Math.signum(this.cameraVelocity.z))
                  );
               }
            }
         }

         this.camControl.update();
         // 模拟推进已交给 simLoop()（独立线程）。这里只剩渲染侧的工作：相机、UI、音频。

         this.ui.update(deltaTime);
         this.spatializer.setCenter(this.cam.position.x, this.cam.position.y, this.cam.zoom / 0.5F);
         this.spatializedPlayer.update((float)deltaTime);
         int tileX = (int)(this.cam.position.x / 64.0F);
         int tileY = (int)(this.cam.position.y / 64.0F);
         float loudness = this.layer.getLoudness(tileX, tileY);
         boolean sfx = Quarry.Q.sound.isPlaySound();
         this.emptyAmb
            .setVolume(
               !sfx
                  ? 0.0F
                  : this.spatializedPlayer.getVolume()
                     * MathUtils.clamp((this.cam.zoom - this.camControl.minZoom) / (this.camControl.maxZoom - this.camControl.minZoom), 0.0F, 1.0F)
            );
         this.baseAmb
            .setVolume(
               this.spatializedPlayer.getVolume()
                  * MathUtils.clamp(
                     !(this.cam.zoom > 4.0F) && !this.gamePaused && sfx ? Math.min(0.5F, loudness) * (1.0F - this.cam.zoom / 4.0F) : 0.0F, 0.0F, 1.0F
                  )
            );
         this.heavyAmb
            .setVolume(
               this.spatializedPlayer.getVolume()
                  * MathUtils.clamp(
                     !(this.cam.zoom > 2.0F) && !this.gamePaused && sfx ? Math.max(0.0F, loudness - 0.6F) * 4.0F * (1.0F - this.cam.zoom / 2.0F) : 0.0F,
                     0.0F,
                     1.0F
                  )
            );
         this.emptyAmb.update(deltaTime);
         this.baseAmb.update(deltaTime);
         this.heavyAmb.update(deltaTime);
         long delta = System.currentTimeMillis() - this.lastAutosave;
         if (!this.saveMap && !this.autosaving && delta >= 180000L) {
            this.autosaving = true;
            this.saveMap = true;
         }
      }
   }

   private void takeScreenShot(final boolean record) {
      FrameBuffer fbo = record ? this.recordFbo : this.screenshotFbo;
      OrthographicCamera cam = record ? this.recordCam : this.screenshotCam;
      final int s = fbo.getWidth();
      fbo.begin();
      Gdx.gl.glClear(16640);
      this.batch.setProjectionMatrix(cam.combined);
      this.batch.begin();
      this.layer.draw(cam, this.fboCam, null, this.batch, this.spriter, this.shaper, this.pfxBatch);
      this.batch.end();
      Gdx.gl.glPixelStorei(3333, 1);
      final Pixmap pixmap;

      try {
         pixmap = new Pixmap(s, s, Format.RGB888);
      } catch (Exception var8) {
         var8.printStackTrace();
         return;
      }

      ByteBuffer pixels = pixmap.getPixels();
      Gdx.gl.glReadPixels(0, 0, s, s, 6407, 5121, pixels);
      Quarry.Q.threadPool.execute(new Runnable() {
         @Override
         public void run() {
            ByteBuffer px = pixmap.getPixels();
            int numBytes = s * s * 3;
            byte[] lines = new byte[numBytes];
            int numBytesPerLine = s * 3;

            for (int i = 0; i < s; i++) {
               ((Buffer)px).position((s - i - 1) * numBytesPerLine);
               px.get(lines, i * numBytesPerLine, numBytesPerLine);
            }

            ((Buffer)px).clear();
            px.put(lines);
            ((Buffer)px).clear();

            try {
               String filename = "TheQuarry/" + (record ? "record" : "screenshots") + "/" + System.currentTimeMillis() + ".png";
               PixmapIO.writePNG(Quarry.Q.file(filename, true), pixmap);
            } catch (Exception var6) {
               Game.this.ui.toast.show(Quarry.Q.i18n.get("toast.save_error"));
            }

            pixmap.dispose();
         }
      });
      fbo.end();
   }

   @Override
   public void draw() {
      synchronized (this.simLock) {
         this.drawLocked();
      }
   }

   /** draw 的本体。必须在 simLock 内调用（要读世界状态画图）。 */
   private void drawLocked() {
      if (this.layer != null) {
         Gdx.gl.glClear(16640);
         if (this.saveMap) {
            if (this.fbo == null) {
               if (Quarry.Q.desktop) {
                  this.fbo = new FrameBuffer(Format.RGB888, 160, 90, false);
               } else {
                  this.fbo = new FrameBuffer(Format.RGB888, 90, 160, false);
               }

               this.thumbCam = new OrthographicCamera(Const.W, Const.H);
            }

            this.thumbCam.position.set(this.cam.position);
            this.thumbCam.zoom = this.cam.zoom;
            this.thumbCam.update();
            this.fbo.begin();
            Gdx.gl.glClear(16640);
            this.batch.setProjectionMatrix(this.thumbCam.combined);
            this.batch.begin();
            this.layer.draw(this.thumbCam, this.fboCam, this.viewport, this.batch, this.spriter, this.shaper, this.pfxBatch);
            this.batch.end();
            byte[] pixels = ScreenUtils.getFrameBufferPixels(0, 0, this.fbo.getWidth(), this.fbo.getHeight(), true);
            Pixmap p = new Pixmap(this.fbo.getWidth(), this.fbo.getHeight(), Format.RGBA8888);
            ((Buffer)p.getPixels().put(pixels)).position(0);
            String save = this.saveName;
            if (this.autosaving) {
               save = this.getAutosaveName(this.currentGameName);
            }

            this.updateThumbnailCache(getFileName(save), p);
            PixmapIO.writeCIM(this.bafh, p);
            p.dispose();
            this.fbo.end();
            this.viewport.apply();
            if (this.autosaving) {
               Quarry.Q.threadPool.execute(new Runnable() {
                  @Override
                  public void run() {
                     Game.this.saveData(Game.this.currentGameName, true, true);
                     Game.this.lastAutosave = System.currentTimeMillis();
                     Game.this.autosaving = false;
                  }
               });
            } else {
               Quarry.Q.threadPool.execute(new Runnable() {
                  @Override
                  public void run() {
                     Game.this.saveData(Game.this.saveName, false, true);
                  }
               });
            }

            this.saveMap = false;
         }

         if (RECORDMODE) {
            int s = this.layer.width * 64;
            if (this.recordFbo == null) {
               this.recordFbo = new FrameBuffer(Format.RGB888, s, s, true);
               this.recordCam = new OrthographicCamera((float)s, (float)s);
               this.recordCam.position.set((float)(this.layer.width * 64) * 0.5F, (float)(this.layer.height * 64) * 0.5F, 0.0F);
               this.recordCam.zoom = (float)(this.layer.width * 64) / (float)s;
               this.recordCam.update();
            }

            if (System.currentTimeMillis() - this.lastScreenshot >= 5000L) {
               this.takeScreenShot(true);
               this.viewport.apply();
               this.lastScreenshot = System.currentTimeMillis();
            }
         }

         if (SCREENSHOT) {
            int sx = this.layer.width * 64;
            if (this.screenshotFbo == null) {
               this.screenshotFbo = new FrameBuffer(Format.RGB888, sx, sx, true);
               this.screenshotCam = new OrthographicCamera((float)sx, (float)sx);
               this.screenshotCam.position.set((float)(this.layer.width * 64) * 0.5F, (float)(this.layer.height * 64) * 0.5F, 0.0F);
               this.screenshotCam.zoom = (float)(this.layer.width * 64) / (float)sx;
               this.screenshotCam.update();
            }

            this.takeScreenShot(false);
            this.viewport.apply();
            SCREENSHOT = false;
         }

         this.cam.update();
         this.batch.setProjectionMatrix(this.cam.combined);
         this.batch.begin();
         synchronized (layerLock) {
            this.layer.draw(this.cam, this.fboCam, this.viewport, this.batch, this.spriter, this.shaper, this.pfxBatch);
         }

         this.batch.end();
         Gdx.gl.glEnable(3042);
         synchronized (highlightLock) {
            if (this.tutorialHighlight.size != 0) {
               this.shaper.setProjectionMatrix(this.cam.combined);
               this.shaper.begin(ShapeType.Filled);
               this.shaper.setColor(0.0F, 1.0F, 0.0F, 0.25F);
               IntSetIterator iter = this.tutorialHighlight.iterator();

               while (iter.hasNext) {
                  int i = iter.next();
                  this.shaper.rect((float)(i / this.layer.height * 64), (float)(i % this.layer.height * 64), 64.0F, 64.0F);
               }

               this.shaper.end();
            }
         }

         this.batch.begin();
         Gdx.gl.glEnable(3042);
         if (this.activeStructure != null && (this.activeStructure.x > -1 || this.endA.x > -1.0F)) {
            if (this.endB.x > -1.0F) {
               for (Structure<?> sx : this.activeStructureTrail.values()) {
                  sx.draw(this.delegate);
               }

               if (this.camControl.elementPlaceable && this.ui.canAffordStructure) {
                  this.batch.setColor(0.1F, 0.8F, 0.1F, 1.0F);
                  this.shaper.setColor(0.1F, 0.8F, 0.1F, 0.75F);
               } else {
                  this.batch.setColor(0.8F, 0.1F, 0.1F, 1.0F);
                  this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.75F);
               }

               this.batch.draw(this.endMarkerGlow, (this.endA.x + 0.5F) * 64.0F - (float)(this.endMarkerA.getRegionWidth() / 2), (this.endA.y + 0.2F) * 64.0F);
               if (this.endB.x > -1.0F) {
                  this.batch
                     .draw(this.endMarkerGlow, (this.endB.x + 0.5F) * 64.0F - (float)(this.endMarker.getRegionWidth() / 2), (this.endB.y + 0.2F) * 64.0F);
                  Gdx.gl.glEnable(3042);
                  this.batch.end();
                  this.shaper.setProjectionMatrix(this.cam.combined);
                  this.shaper.begin(ShapeType.Filled);
                  Gdx.gl.glEnable(3042);
                  this.shaper
                     .rectLine((this.endA.x + 0.5F) * 64.0F, (this.endA.y + 0.5F) * 64.0F, (this.endB.x + 0.5F) * 64.0F, (this.endB.y + 0.5F) * 64.0F, 6.0F);
                  if (this.camControl.elementPlaceable && this.ui.canAffordStructure) {
                     this.shaper.setColor(0.2F, 1.0F, 0.2F, 0.6F);
                  } else {
                     this.shaper.setColor(1.0F, 0.2F, 0.2F, 0.6F);
                  }

                  this.shaper
                     .rectLine((this.endA.x + 0.5F) * 64.0F, (this.endA.y + 0.5F) * 64.0F, (this.endB.x + 0.5F) * 64.0F, (this.endB.y + 0.5F) * 64.0F, 2.0F);
                  this.shaper.end();
                  this.batch.begin();
               }

               this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               float factorA = this.activeEnd == 1 ? 1.35F : 1.0F;
               float widthA = (float)this.endMarkerA.getRegionWidth() * factorA;
               float heightA = (float)this.endMarkerA.getRegionHeight() * factorA;
               if (this.endA.y > this.endB.y) {
                  this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
                  if (this.endB.x > -1.0F) {
                     float factorB = this.activeEnd == 2 ? 1.35F : 1.0F;
                     float widthB = (float)this.endMarker.getRegionWidth() * factorB;
                     float heightB = (float)this.endMarker.getRegionHeight() * factorB;
                     this.batch.draw(this.endMarker, (this.endB.x + 0.5F) * 64.0F - widthB / 2.0F, (this.endB.y + 0.2F / factorB) * 64.0F, widthB, heightB);
                  }
               } else {
                  if (this.endB.x > -1.0F) {
                     float factorB = this.activeEnd == 2 ? 1.35F : 1.0F;
                     float widthB = (float)this.endMarker.getRegionWidth() * factorB;
                     float heightB = (float)this.endMarker.getRegionHeight() * factorB;
                     this.batch.draw(this.endMarker, (this.endB.x + 0.5F) * 64.0F - widthB / 2.0F, (this.endB.y + 0.2F / factorB) * 64.0F, widthB, heightB);
                  }

                  this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
               }
            } else {
               if (this.structureDestroyMode || this.cableDestroyMode) {
                  this.batch.setColor(1.0F, 0.5F, 0.5F, 1.0F);
               } else if (this.camControl.elementPlaceable && this.ui.canAffordStructure) {
                  this.batch.setColor(0.75F, 1.0F, 0.5F, 1.0F);
               } else {
                  this.batch.setColor(1.0F, 0.5F, 0.5F, 1.0F);
               }

               this.activeStructure.draw(this.delegate);
               if (this.endA.x > -1.0F) {
                  float factorA = this.activeEnd == 1 ? 1.35F : 1.0F;
                  float widthA = (float)this.endMarkerA.getRegionWidth() * factorA;
                  float heightA = (float)this.endMarkerA.getRegionHeight() * factorA;
                  if (this.camControl.elementPlaceable && this.ui.canAffordStructure) {
                     this.batch.setColor(0.1F, 0.8F, 0.1F, 1.0F);
                     this.shaper.setColor(0.1F, 0.8F, 0.1F, 0.75F);
                  } else {
                     this.batch.setColor(0.8F, 0.1F, 0.1F, 1.0F);
                     this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.75F);
                  }

                  this.batch
                     .draw(this.endMarkerGlow, (this.endA.x + 0.5F) * 64.0F - (float)(this.endMarkerA.getRegionWidth() / 2), (this.endA.y + 0.2F) * 64.0F);
                  this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
               }

               this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            }

            if (!this.structureDestroyMode) {
               this.drawStructureAssists(this.activeStructure, this.ui.currentRecipe);
            }
         }

         if ((this.bulkDestroyMode || this.bulkCableMode || this.copyMode) && this.endA.x > -1.0F) {
            float factorA = this.activeEnd == 1 ? 1.35F : 1.0F;
            float widthA = (float)this.endMarkerA.getRegionWidth() * factorA;
            float heightA = (float)this.endMarkerA.getRegionHeight() * factorA;
            if (this.endB.x > -1.0F) {
               if (this.copyMode) {
                  this.batch.setColor(0.1F, 0.1F, 0.8F, 0.5F);
                  this.shaper.setColor(0.1F, 0.1F, 0.8F, 0.3F);
               } else {
                  this.batch.setColor(0.8F, 0.1F, 0.1F, 0.5F);
                  this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.3F);
               }

               Gdx.gl.glEnable(3042);
               this.batch.end();
               this.shaper.setProjectionMatrix(this.cam.combined);
               this.shaper.begin(ShapeType.Filled);
               Gdx.gl.glEnable(3042);
               this.shaper
                  .rect(
                     Math.min(this.endA.x, this.endB.x) * 64.0F,
                     Math.min(this.endA.y, this.endB.y) * 64.0F,
                     (Math.max(this.endA.x, this.endB.x) - Math.min(this.endA.x, this.endB.x) + 1.0F) * 64.0F,
                     (Math.max(this.endA.y, this.endB.y) - Math.min(this.endA.y, this.endB.y) + 1.0F) * 64.0F
                  );
               this.shaper.end();
               this.shaper.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               this.batch.begin();
               this.batch.draw(this.endMarkerGlow, (this.endA.x + 0.5F) * 64.0F - (float)(this.endMarkerA.getRegionWidth() / 2), (this.endA.y + 0.2F) * 64.0F);
               this.batch.draw(this.endMarkerGlow, (this.endB.x + 0.5F) * 64.0F - (float)(this.endMarker.getRegionWidth() / 2), (this.endB.y + 0.2F) * 64.0F);
               this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               if (this.endA.y > this.endB.y) {
                  this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
                  if (this.endB.x > -1.0F) {
                     float factorB = this.activeEnd == 2 ? 1.35F : 1.0F;
                     float widthB = (float)this.endMarker.getRegionWidth() * factorB;
                     float heightB = (float)this.endMarker.getRegionHeight() * factorB;
                     this.batch.draw(this.endMarker, (this.endB.x + 0.5F) * 64.0F - widthB / 2.0F, (this.endB.y + 0.2F / factorB) * 64.0F, widthB, heightB);
                  }
               } else {
                  if (this.endB.x > -1.0F) {
                     float factorB = this.activeEnd == 2 ? 1.35F : 1.0F;
                     float widthB = (float)this.endMarker.getRegionWidth() * factorB;
                     float heightB = (float)this.endMarker.getRegionHeight() * factorB;
                     this.batch.draw(this.endMarker, (this.endB.x + 0.5F) * 64.0F - widthB / 2.0F, (this.endB.y + 0.2F / factorB) * 64.0F, widthB, heightB);
                  }

                  this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
               }
            } else {
               if (this.copyMode) {
                  this.batch.setColor(0.1F, 0.1F, 0.8F, 0.5F);
                  this.shaper.setColor(0.1F, 0.1F, 0.8F, 0.3F);
               } else {
                  this.batch.setColor(0.8F, 0.1F, 0.1F, 0.5F);
                  this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.3F);
               }

               Gdx.gl.glEnable(3042);
               this.batch.end();
               this.shaper.setProjectionMatrix(this.cam.combined);
               this.shaper.begin(ShapeType.Filled);
               Gdx.gl.glEnable(3042);
               this.shaper.rect(this.endA.x * 64.0F, this.endA.y * 64.0F, 64.0F, 64.0F);
               this.shaper.end();
               this.shaper.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               this.batch.begin();
               this.batch.draw(this.endMarkerGlow, (this.endA.x + 0.5F) * 64.0F - (float)(this.endMarkerA.getRegionWidth() / 2), (this.endA.y + 0.2F) * 64.0F);
               this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
               this.batch.draw(this.endMarkerA, (this.endA.x + 0.5F) * 64.0F - widthA / 2.0F, (this.endA.y + 0.2F / factorA) * 64.0F, widthA, heightA);
            }
         }

         if (this.pasteMode) {
            this.batch.setColor(0.1F, 0.1F, 0.8F, 0.5F);
            this.shaper.setColor(0.1F, 0.1F, 0.8F, 0.3F);
            Gdx.gl.glEnable(3042);
            this.batch.end();
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Filled);
            Gdx.gl.glEnable(3042);
            this.shaper
               .rect((float)(this.copyRegion[0] * 64), (float)(this.copyRegion[1] * 64), (float)(this.copyRegion[2] * 64), (float)(this.copyRegion[3] * 64));
            this.shaper.end();
            this.batch.begin();
            if (this.camControl.elementPlaceable && this.ui.canAffordStructure) {
               this.batch.setColor(0.75F, 1.0F, 0.5F, 1.0F);
               this.shaper.setColor(0.1F, 0.8F, 0.1F, 0.4F);

               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyStructures.values()) {
                  sx.getKey().draw(this.delegate);
               }

               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyCables.values()) {
                  sx.getKey().draw(this.delegate);
               }
            } else if (!this.ui.canAffordStructure) {
               this.batch.setColor(1.0F, 0.5F, 0.5F, 1.0F);
               this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.4F);

               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyStructures.values()) {
                  sx.getKey().draw(this.delegate);
               }

               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyCables.values()) {
                  sx.getKey().draw(this.delegate);
               }
            } else {
               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyStructures.values()) {
                  if (this.copyCollisions.contains(sx.getKey())) {
                     this.batch.setColor(1.0F, 0.5F, 0.5F, 1.0F);
                     this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.4F);
                  } else {
                     this.batch.setColor(0.75F, 1.0F, 0.5F, 1.0F);
                     this.shaper.setColor(0.1F, 0.8F, 0.1F, 0.4F);
                  }

                  sx.getKey().draw(this.delegate);
               }

               for (Pair<Structure<?>, NBT.CompoundTag> sx : this.copyCables.values()) {
                  if (this.copyCollisions.contains(sx.getKey())) {
                     this.batch.setColor(1.0F, 0.5F, 0.5F, 1.0F);
                     this.shaper.setColor(0.8F, 0.1F, 0.1F, 0.4F);
                  } else {
                     this.batch.setColor(0.75F, 1.0F, 0.5F, 1.0F);
                     this.shaper.setColor(0.1F, 0.8F, 0.1F, 0.4F);
                  }

                  sx.getKey().draw(this.delegate);
               }
            }

            this.shaper.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.batch.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.batch.end();
            Gdx.gl.glEnable(3042);
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Line);
            this.shaper.setColor(0.0F, 0.0F, 0.0F, 0.25F);

            for (int i = 1; i < this.layer.width; i++) {
               this.shaper.line((float)(i * 64), 0.0F, (float)(i * 64), (float)(this.layer.height * 64));
               this.shaper.line(0.0F, (float)(i * 64), (float)(this.layer.width * 64), (float)(i * 64));
            }

            this.shaper.end();
            this.batch.begin();
            Gdx.gl.glDisable(3042);
         }

         if (this.ui.currentClickedStructure != null
            && (
               this.ui.structureUIRecipes.isChecked() && !(this.ui.currentClickedStructure instanceof ShaftDrillHead)
                  || !this.ui.structureUIRecipes.isVisible() && !(this.ui.currentClickedStructure instanceof ProducerStructure)
            )
            && this.ui.currentClickedStructure.layer == this.layer) {
            this.drawStructureAssists(this.ui.currentClickedStructure, this.ui.currentRecipe);
         }

         this.batch.end();
         if (Gdx.app.getType() == ApplicationType.Desktop
            && this.hoverTileX > -1
            && this.hoverTileX < this.layer.width
            && this.hoverTileY > -1
            && this.hoverTileY < this.layer.height) {
            Gdx.gl.glEnable(3042);
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Line);
            this.shaper.setColor(0.0F, 0.0F, 0.0F, 0.25F);
            this.shaper.rect((float)(this.hoverTileX * 64), (float)(this.hoverTileY * 64), 64.0F, 64.0F);
            this.shaper.end();
         }

         if (this.activeStructure != null && (this.activeStructure.x > -1 || this.endA.x > -1.0F)) {
            if (this.activeStructure.x > -1 && this.endA.x == -1.0F) {
               Gdx.gl.glEnable(3042);
               this.batch.begin();
               this.shaper.setProjectionMatrix(this.cam.combined);
               this.shaper.begin(ShapeType.Filled);
               this.activeStructure.drawFrame(this.delegate, this.shaper, null);
               this.shaper.end();
               this.batch.end();
            }

            Gdx.gl.glEnable(3042);
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Line);
            this.shaper.setColor(0.0F, 0.0F, 0.0F, 0.25F);

            for (int i = 1; i < this.layer.width; i++) {
               this.shaper.line((float)(i * 64), 0.0F, (float)(i * 64), (float)(this.layer.height * 64));
               this.shaper.line(0.0F, (float)(i * 64), (float)(this.layer.width * 64), (float)(i * 64));
            }

            this.shaper.end();
            Gdx.gl.glDisable(3042);
         } else if (this.activeTile.x > -1.0F) {
            Gdx.gl.glEnable(3042);
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Filled);
            this.shaper.setColor(Structure.selectionColor);
            this.shaper.line(this.activeTile.x * 64.0F, this.activeTile.y * 64.0F, (this.activeTile.x + 1.0F) * 64.0F, this.activeTile.y * 64.0F);
            this.shaper
               .line(this.activeTile.x * 64.0F, (this.activeTile.y + 1.0F) * 64.0F, (this.activeTile.x + 1.0F) * 64.0F, (this.activeTile.y + 1.0F) * 64.0F);
            this.shaper.line(this.activeTile.x * 64.0F, this.activeTile.y * 64.0F, this.activeTile.x * 64.0F, (this.activeTile.y + 1.0F) * 64.0F);
            this.shaper
               .line((this.activeTile.x + 1.0F) * 64.0F, this.activeTile.y * 64.0F, (this.activeTile.x + 1.0F) * 64.0F, (this.activeTile.y + 1.0F) * 64.0F);
            this.shaper.setColor(Structure.selectionColor.r, Structure.selectionColor.g, Structure.selectionColor.b, 0.25F);
            this.shaper.rect(this.activeTile.x * 64.0F, this.activeTile.y * 64.0F, 64.0F, 64.0F);
            this.shaper.end();
            Gdx.gl.glDisable(3042);
         }

         if (this.ui.tutorial.getStepNum() == 1) {
            Gdx.gl.glEnable(3042);
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Line);
            this.shaper.setColor(0.0F, 0.0F, 0.0F, 0.25F);
            int len = 20;

            for (int i = 1; i < len; i++) {
               int y = this.layer.width / 2 - len / 2 + i;
               if (y >= 1 && y <= this.layer.height - 1) {
                  this.shaper
                     .line(
                        (float)(Math.max(0, this.layer.width / 2 - len / 2) * 64),
                        (float)(y * 64),
                        (float)(Math.min(this.layer.width, this.layer.width / 2 + len / 2) * 64),
                        (float)(y * 64)
                     );
               }
            }

            for (int ix = 1; ix < len; ix++) {
               int x = Math.max(0, this.layer.width / 2 - len / 2 + ix);
               if (x >= 1 && x <= this.layer.width - 1) {
                  this.shaper
                     .line(
                        (float)(x * 64),
                        (float)(Math.max(0, this.layer.width / 2 - len / 2) * 64),
                        (float)(x * 64),
                        (float)(Math.min(this.layer.height, this.layer.width / 2 + len / 2) * 64)
                     );
               }
            }

            this.shaper.end();
            Gdx.gl.glDisable(3042);
         }

         if (this.ui.currentClickedStructure instanceof Substation) {
            Gdx.gl.glEnable(3042);
            this.shaper.begin(ShapeType.Filled);

            for (Edge e : this.ui.currentClickedStructure.getPowerNetwork().getMinimumSpanningTree()) {
               if (e.getA().layer == this.layer && e.getB().layer == G.layer) {
                  if (e.getNetworkStrength() == PowerNetwork.NetworkStrength.PowerPole) {
                     this.shaper.setColor(1.0F, 0.0F, 1.0F, 1.0F);
                  } else {
                     this.shaper.setColor(1.0F, 1.0F, 0.0F, 1.0F);
                  }

                  this.shaper
                     .rect(
                        ((float)e.getA().getX() + (float)e.getA().getWidth() / 2.0F) * 64.0F - 10.0F,
                        ((float)e.getA().getY() + (float)e.getA().getHeight() / 2.0F) * 64.0F - 10.0F,
                        20.0F,
                        20.0F
                     );
                  this.shaper
                     .rect(
                        ((float)e.getB().getX() + (float)e.getB().getWidth() / 2.0F) * 64.0F - 10.0F,
                        ((float)e.getB().getY() + (float)e.getB().getHeight() / 2.0F) * 64.0F - 10.0F,
                        20.0F,
                        20.0F
                     );
                  this.shaper
                     .rectLine(
                        ((float)e.getA().getX() + (float)e.getA().getWidth() / 2.0F) * 64.0F,
                        ((float)e.getA().getY() + (float)e.getA().getHeight() / 2.0F) * 64.0F,
                        ((float)e.getB().getX() + (float)e.getB().getWidth() / 2.0F) * 64.0F,
                        ((float)e.getB().getY() + (float)e.getB().getHeight() / 2.0F) * 64.0F,
                        4.0F
                     );
               }
            }

            this.shaper.end();
         }

         if (DRAW_DEBUG) {
            this.shaper.setProjectionMatrix(this.cam.combined);
            this.shaper.begin(ShapeType.Line);
            this.shaper.setColor(Color.GRAY);

            for (int ixx = 0; ixx < this.layer.width; ixx++) {
               for (int j = 0; j < this.layer.height; j++) {
                  this.shaper.line((float)(ixx * 64), (float)(j * 64), (float)((ixx + 1) * 64), (float)(j * 64));
                  this.shaper.line((float)(ixx * 64), (float)(j * 64), (float)(ixx * 64), (float)((j + 1) * 64));
               }
            }

            this.shaper.end();
            Gdx.gl.glEnable(3042);
            this.shaper.setColor(Color.GREEN);
            this.shaper.begin(ShapeType.Filled);

            for (int ixx = 0; ixx < this.layer.width; ixx += 32) {
               for (int j = 0; j < this.layer.height; j += 32) {
                  if (j > 0) {
                     this.shaper.rectLine((float)(ixx * 64), (float)(j * 64), (float)((ixx + 32) * 64), (float)(j * 64), 10.0F);
                  }

                  if (ixx > 0) {
                     this.shaper.rectLine((float)(ixx * 64), (float)(j * 64), (float)(ixx * 64), (float)((j + 32) * 64), 10.0F);
                  }
               }
            }

            for (int ixx = 0; ixx < this.layer.width; ixx++) {
               for (int j = 0; j < this.layer.height; j++) {
                  this.shaper.setColor(1.0F, 0.0F, 0.0F, 1.0F);
                  byte flag = this.layer.getFlags(ixx, j);
                  if ((flag & 1) != 0) {
                     this.shaper.rect((float)(ixx * 64), (float)(j * 64), 32.0F, 32.0F);
                  }

                  this.shaper.setColor(0.0F, 0.0F, 1.0F, 1.0F);
                  if ((flag & 2) != 0) {
                     this.shaper.rect(((float)ixx + 0.5F) * 64.0F, (float)(j * 64), 32.0F, 32.0F);
                  }

                  this.shaper.setColor(0.0F, 1.0F, 1.0F, 1.0F);
                  if ((flag & 16) != 0) {
                     this.shaper.rect(((float)ixx + 0.25F) * 64.0F, ((float)j + 0.25F) * 64.0F, 32.0F, 32.0F);
                  }

                  this.shaper.setColor(0.0F, 1.0F, 0.0F, 1.0F);
                  if ((flag & 4) != 0) {
                     this.shaper.rect((float)(ixx * 64), ((float)j + 0.5F) * 64.0F, 32.0F, 32.0F);
                  }

                  this.shaper.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                  if ((flag & 8) != 0) {
                     this.shaper.rect(((float)ixx + 0.5F) * 64.0F, ((float)j + 0.5F) * 64.0F, 32.0F, 32.0F);
                  }
               }
            }

            for (int ixx : this.tilePathfinding.getOpenList()) {
               this.shaper.setColor(0.0F, 0.0F, 1.0F, 0.3F);
               int x = ixx / this.layer.height;
               int y = ixx % this.layer.height;
               this.shaper.rect((float)(x * 64), (float)(y * 64), 64.0F, 64.0F);
            }

            List<Integer> closed = this.tilePathfinding.getClosedList();
            this.shaper.setColor(0.0F, 1.0F, 0.0F, 0.3F);

            for (int ixx : closed) {
               this.shaper.setColor(1.0F, 0.0F, 0.0F, 0.3F);
               int x = ixx / this.layer.height;
               int y = ixx % this.layer.height;
               this.shaper.rect((float)(x * 64), (float)(y * 64), 64.0F, 64.0F);
            }

            for (PowerNetwork n : this.powerGrid.getNetworks()) {
               for (Edge ex : n.getMinimumSpanningTree()) {
                  if (ex.getA().layer == this.layer && ex.getB().layer == this.layer) {
                     if (ex.getNetworkStrength() == PowerNetwork.NetworkStrength.PowerPole) {
                        this.shaper.setColor(1.0F, 0.0F, 1.0F, 1.0F);
                     } else {
                        this.shaper.setColor(1.0F, 1.0F, 0.0F, 1.0F);
                     }

                     int off = 32;
                     this.shaper.circle((float)(ex.getA().getX() * 64 + off), (float)(ex.getA().getY() * 64 + off), 12.0F);
                     this.shaper.circle((float)(ex.getB().getX() * 64 + off), (float)(ex.getB().getY() * 64 + off), 12.0F);
                     this.shaper
                        .rectLine(
                           (float)(ex.getA().getX() * 64 + off),
                           (float)(ex.getA().getY() * 64 + off),
                           (float)(ex.getB().getX() * 64 + off),
                           (float)(ex.getB().getY() * 64 + off),
                           4.0F
                        );
                  }
               }
            }

            this.shaper.end();
         }

         this.ui.draw();
         synchronized (renderThreadLock) {
            if (this.renderThreadTasks.size > 0) {
               ((Runnable)this.renderThreadTasks.removeIndex(0)).run();
            }
         }
      }
   }

   public void drawStructureAssists(Structure<?> structure, RecipeList.Recipe activeRecipe) {
      Dock[] docks = structure.getDocks();
      if (structure instanceof ProducerStructure && activeRecipe != null) {
         RecipeList.Recipe r = activeRecipe;
         Item.Items in = activeRecipe.getInput();
         Item.Items out = activeRecipe.getOutput();
         int i = 0;
         int o = 0;

         for (Dock d : docks) {
            if (d.type != Dock.DockType.Power && d.type != Dock.DockType.BigPower) {
               Item.Items.Amount a = null;
               if (in != null && (d.type == Dock.DockType.ItemIn || d.type == Dock.DockType.FluidIn) && in.entries.length > i) {
                  a = in.entries[i++];
               } else if (out != null && (d.type == Dock.DockType.ItemOut || d.type == Dock.DockType.FluidOut) && out.entries.length > o) {
                  a = out.entries[o++];
               }

               if (a != null) {
                  this.batch
                     .draw(
                        a.getCat() != null ? a.getCat().icon : a.getItem().icon,
                        ((float)(structure.x + d.x) + (float)d.dir.dx * 1.5F) * 64.0F + 20.0F,
                        ((float)(structure.y + d.y) + (float)d.dir.dy * 1.5F) * 64.0F + 20.0F,
                        24.0F,
                        24.0F
                     );
                  this.batch
                     .draw(
                        this.caret,
                        (float)((structure.x + d.x + d.dir.dx) * 64) + 20.0F,
                        (float)((structure.y + d.y + d.dir.dy) * 64) + 27.0F,
                        12.0F,
                        5.0F,
                        24.0F,
                        10.0F,
                        1.0F,
                        1.0F,
                        (float)(-90 * (d.dir.ordinal() + (d.type != Dock.DockType.FluidOut && d.type != Dock.DockType.ItemOut ? 0 : 2)))
                     );
               }
            }
         }

         if (structure instanceof GasTurbine) {
            GasTurbine t = (GasTurbine)structure;
            if (t.isLevel0()) {
               Direction dir = t.getForwardDirection();

               for (int j = 1; j < t.getLevel(); j++) {
                  Dock dx = t.getDocks()[1];
                  this.batch
                     .draw(
                        Item.ItemType.Lubricant.icon,
                        ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F + (float)(j * structure.getWidth() * dir.dx)) * 64.0F + 20.0F,
                        ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F + (float)(j * structure.getHeight() * dir.dy)) * 64.0F + 20.0F,
                        24.0F,
                        24.0F
                     );
                  this.batch
                     .draw(
                        this.caret,
                        (float)((structure.x + dx.x + dx.dir.dx + j * structure.getWidth() * dir.dx) * 64) + 20.0F,
                        (float)((structure.y + dx.y + dx.dir.dy + j * structure.getHeight() * dir.dy) * 64) + 27.0F,
                        12.0F,
                        5.0F,
                        24.0F,
                        10.0F,
                        1.0F,
                        1.0F,
                        (float)(-90 * (dx.dir.ordinal() + (dx.type != Dock.DockType.FluidOut && dx.type != Dock.DockType.ItemOut ? 0 : 2)))
                     );
                  if (r.getOutput() != null) {
                     dx = t.getDocks()[2];
                     this.batch
                        .draw(
                           Item.ItemType.Water.icon,
                           ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F + (float)(j * structure.getWidth() * dir.dx)) * 64.0F + 20.0F,
                           ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F + (float)(j * structure.getHeight() * dir.dy)) * 64.0F + 20.0F,
                           24.0F,
                           24.0F
                        );
                     this.batch
                        .draw(
                           this.caret,
                           (float)((structure.x + dx.x + dx.dir.dx + j * structure.getWidth() * dir.dx) * 64) + 20.0F,
                           (float)((structure.y + dx.y + dx.dir.dy + j * structure.getHeight() * dir.dy) * 64) + 27.0F,
                           12.0F,
                           5.0F,
                           24.0F,
                           10.0F,
                           1.0F,
                           1.0F,
                           (float)(-90 * (dx.dir.ordinal() + (dx.type != Dock.DockType.FluidOut && dx.type != Dock.DockType.ItemOut ? 0 : 2)))
                        );
                     dx = t.getDocks()[4];
                     this.batch
                        .draw(
                           Item.ItemType.PressurizedSteam.icon,
                           ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F + (float)(j * structure.getWidth() * dir.dx)) * 64.0F + 20.0F,
                           ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F + (float)(j * structure.getHeight() * dir.dy)) * 64.0F + 20.0F,
                           24.0F,
                           24.0F
                        );
                     this.batch
                        .draw(
                           this.caret,
                           (float)((structure.x + dx.x + dx.dir.dx + j * structure.getWidth() * dir.dx) * 64) + 20.0F,
                           (float)((structure.y + dx.y + dx.dir.dy + j * structure.getHeight() * dir.dy) * 64) + 27.0F,
                           12.0F,
                           5.0F,
                           24.0F,
                           10.0F,
                           1.0F,
                           1.0F,
                           (float)(-90 * (dx.dir.ordinal() + (dx.type != Dock.DockType.FluidOut && dx.type != Dock.DockType.ItemOut ? 0 : 2)))
                        );
                  }
               }
            }
         }
      } else if (structure instanceof Boiler) {
         Dock dx = structure.getDocks()[0];
         this.batch
            .draw(
               Item.ItemType.Steam.icon,
               ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 20.0F,
               ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 20.0F,
               24.0F,
               24.0F
            );
         dx = structure.getDocks()[1];
         this.batch
            .draw(
               Item.ItemType.Water.icon,
               ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 20.0F,
               ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 20.0F,
               24.0F,
               24.0F
            );
         dx = structure.getDocks()[2];
         this.batch
            .draw(
               Item.ItemCategory.CoalFuel.icon,
               ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 20.0F,
               ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 20.0F,
               24.0F,
               24.0F
            );
      } else if (structure instanceof Refinery) {
         Dock dx = structure.getDocks()[0];
         this.batch
            .draw(
               Item.ItemType.RefinedOil.icon,
               ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 20.0F,
               ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 20.0F,
               24.0F,
               24.0F
            );
         dx = structure.getDocks()[1];
         this.batch
            .draw(
               Item.ItemType.CrudeOil.icon,
               ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 20.0F,
               ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 20.0F,
               24.0F,
               24.0F
            );

         for (int i = 2; i < 10; i++) {
            dx = structure.getDocks()[i];
            this.batch
               .draw(
                  ((TextureRegionDrawable)DistillationColumn.classSchema.icon).getRegion(),
                  ((float)(structure.x + dx.x) + (float)dx.dir.dx * 1.5F) * 64.0F + 12.0F,
                  ((float)(structure.y + dx.y) + (float)dx.dir.dy * 1.5F) * 64.0F + 12.0F,
                  40.0F,
                  40.0F
               );
         }
      }

      for (Dock dx : docks) {
         if (dx.type != Dock.DockType.Power && dx.type != Dock.DockType.BigPower) {
            float x = (float)((structure.x + dx.x + dx.dir.dx) * 64) + 20.0F;
            float y = (float)((structure.y + dx.y + dx.dir.dy) * 64) + 27.0F;
            float rot = (float)(
               -90 * (dx.dir.ordinal() + (dx.type != Dock.DockType.FluidOut && dx.type != Dock.DockType.ItemOut && !(structure instanceof Filter) ? 0 : 2))
            );
            if (this.activeStructure != null) {
               if (dx.type == Dock.DockType.StackIn) {
                  x -= (float)dx.dir.dx * 111.0F;
                  y -= (float)dx.dir.dy * 111.0F;
                  this.batch.setColor(1.0F, 1.0F, 1.0F, 0.8F);
                  if (this.liningUpInput != Game.LiningUpState.NoOther) {
                     x = (float)((double)x + (double)dx.dir.dx * Math.sin((double)((float)Gdx.graphics.getFrameId() / 15.0F)) * 4.0);
                     y = (float)((double)y + (double)dx.dir.dy * Math.sin((double)((float)Gdx.graphics.getFrameId() / 15.0F)) * 4.0);
                  }

                  if (this.liningUpInput == Game.LiningUpState.NotLiningUp) {
                     this.batch.setColor(1.0F, 0.0F, 0.0F, 0.8F);
                  }
               } else if (dx.type == Dock.DockType.StackOut) {
                  rot += 180.0F;
                  x -= (float)dx.dir.dx * 111.0F;
                  y -= (float)dx.dir.dy * 111.0F;
                  this.batch.setColor(1.0F, 1.0F, 1.0F, 0.8F);
                  if (this.liningUpOutput != Game.LiningUpState.NoOther) {
                     x = (float)((double)x + (double)dx.dir.dx * Math.sin((double)((float)Gdx.graphics.getFrameId() / 15.0F)) * 4.0);
                     y = (float)((double)y + (double)dx.dir.dy * Math.sin((double)((float)Gdx.graphics.getFrameId() / 15.0F)) * 4.0);
                  }

                  if (this.liningUpOutput == Game.LiningUpState.NotLiningUp) {
                     this.batch.setColor(1.0F, 0.0F, 0.0F, 0.8F);
                  }
               }
            } else if (dx.type == Dock.DockType.StackIn || dx.type == Dock.DockType.StackOut) {
               continue;
            }

            this.batch.draw(this.caret, x, y, 12.0F, 5.0F, 24.0F, 10.0F, 1.0F, 1.0F, rot);
            this.batch.setColor(Color.WHITE);
         }
      }
   }

   public boolean trailStructureCanBePlaced(int i, int start, int target) {
      int x = i / this.layer.height;
      int y = i % this.layer.height;
      if (x < 0 || y < 0 || x >= this.layer.width || y >= this.layer.height) {
         return false;
      } else if (this.layer.isInFogOfWar(x, y)) {
         return false;
      } else {
         boolean end = i == start || i == target;
         if (this.activeStructure instanceof CopperCable) {
            if (this.layer.getCable(x, y) != null && !end || this.layer.isPowerDockCollision(x, y)) {
               return false;
            }
         } else if (!end) {
            if (this.layer.isCollision(x, y, !(this.activeStructure instanceof Conveyor), !(this.activeStructure instanceof FluidTubeStructure))) {
               return false;
            }
         } else {
            Structure<?> trailS = (Structure<?>)this.activeStructureTrail.get(i);
            Structure<?> s = this.layer.getStructure(i / this.layer.height, i % this.layer.height);
            if (trailS != null && s != null) {
               if (s.getSchema().type != trailS.getSchema().type) {
                  return false;
               }

               this.activeStructureTrail.remove(i);
               this.activeStructurePath.remove(Integer.valueOf(i));
            }
         }

         return true;
      }
   }

   public synchronized void addResources(Item.ItemType item, int amount, boolean addToAStorage) {
      if (amount > 0) {
         synchronized (resourceLock) {
            int remaining = amount;
            if (addToAStorage) {
               synchronized (layerLock) {
                  label60:
                  for (Layer layer : this.layerIter) {
                     for (StorageStructure b : layer.storages) {
                        if (b.isRefundStorage() && (b.getSchema().type != StructureType.Barrel || !b.isEmpty())) {
                           remaining = b.addToInventoryWithRest(item, remaining);
                           if (remaining == 0) {
                              break label60;
                           }
                        }
                     }
                  }

                  if (remaining > 0) {
                     this.ui.toast.show(Quarry.Q.i18n.get("toast.refund_full"));
                     amount -= remaining;
                  }
               }
            }

            Integer val = this.resources.get(item);
            if (val == null) {
               val = 0;
            }

            val = val + amount;
            this.resources.put(item, val);
            this.resourceChangeNotifier.notify(ChangeNotifier.Event.Type.ADD, this.resourceChangePair.set(item, amount));
            this.getSeenResources().add(item);
            // 可能来自模拟线程（生产），只能置脏标记
            this.ui.markResourcesDirty();
            if (this.activeStructure != null) {
               this.camControl.updateActiveElementPlaceable();
            }
         }
      }
   }

   public synchronized boolean removeResources(Item.ItemType item, int amount, boolean removeFromAStorage) {
      if (amount <= 0) {
         return true;
      } else {
         synchronized (resourceLock) {
            Integer val = this.resources.get(item);
            if (val == null) {
               return false;
            } else {
               if (INCREASE_MODE) {
                  val = val + amount;
               } else {
                  val = val - amount;
               }

               if (val < 0) {
                  val = 0;
               }

               this.resources.put(item, val);
               this.resourceChangeNotifier.notify(ChangeNotifier.Event.Type.REMOVE, this.resourceChangePair.set(item, amount));
               this.getSeenResources().add(item);
               int remaining = amount;
               if (removeFromAStorage) {
                  synchronized (layerLock) {
                     for (Layer layer : this.layers) {
                        for (Chunk c : layer.getChunks()) {
                           if (c != null && c.isInit()) {
                              for (Structure<?> s : c.getStructures()) {
                                 if (s instanceof StorageStructure) {
                                    remaining = ((StorageStructure)s).removeFromInventoryWithRest(item, remaining);
                                    if (remaining == 0) {
                                       break;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }

               this.ui.markResourcesDirty();
               if (this.activeStructure != null) {
                  this.camControl.updateActiveElementPlaceable();
               }

               return remaining <= 0;
            }
         }
      }
   }

   public synchronized int getResource(Item.ItemType item) {
      synchronized (resourceLock) {
         Integer val = this.resources.get(item);
         return val == null ? 0 : val;
      }
   }

   public synchronized void addSeenResource(Item.ItemType item) {
      synchronized (resourceLock) {
         if (this.getSeenResources().add(item)) {
            this.ui.markResourcesDirty();
         }
      }
   }

   public synchronized boolean hasSeenResource(Item.ItemType item) {
      synchronized (resourceLock) {
         return this.getSeenResources().contains(item);
      }
   }

   public EnumSet<Item.ItemType> getSeenResources() {
      return this.seenResources;
   }

   public synchronized Set<Entry<Item.ItemType, Integer>> getAllResources() {
      return this.resources.entrySet();
   }

   public void recalcResources() {
      EnumMap<Item.ItemType, Integer> res = new EnumMap<>(Item.ItemType.class);
      synchronized (layerLock) {
         for (Layer l : this.layers) {
            for (Chunk c : l.getChunks()) {
               if (c != null) {
                  for (Structure<?> s : c.getStructures()) {
                     if (s instanceof Storage) {
                        CInventory ci = (CInventory)s.getComponents()[0];

                        for (Entry<Item.ItemType, Integer> e : ci.getAll()) {
                           Integer v = res.get(e.getKey());
                           v = v == null ? 0 : v;
                           res.put(e.getKey(), v + e.getValue());
                        }
                     }

                     if (s instanceof Barrel) {
                        CSingleInventory ci = (CSingleInventory)s.getComponents()[0];
                        if (ci.getItem() != null) {
                           Integer v = res.get(ci.getItem());
                           v = v == null ? 0 : v;
                           res.put(ci.getItem(), v + ci.getCount());
                        }
                     }
                  }
               }
            }
         }
      }

      synchronized (resourceLock) {
         this.resources.clear();
         this.resources.putAll(res);
         this.seenResources.addAll(res.keySet());
         this.ui.updateResources(true);
      }
   }

   public synchronized boolean hasSciences(Collection<Science.ScienceType> sciencesRequired) {
      return GOD_MODE ? true : this.sciences.containsAll(sciencesRequired);
   }

   public synchronized boolean hasSciences(Science.ScienceType... sciencesRequired) {
      if (GOD_MODE) {
         return true;
      } else {
         for (Science.ScienceType s : sciencesRequired) {
            if (!this.sciences.contains(s)) {
               return false;
            }
         }

         return true;
      }
   }

   public synchronized boolean hasScience(Science.ScienceType science) {
      return this.sciences.contains(science) || GOD_MODE;
   }

   public synchronized boolean hasCurrentScience(Science.ScienceType science) {
      return this.currentSciences.contains(science);
   }

   public synchronized void addScience(Science.ScienceType science) {
      this.currentSciences.remove(science);
      this.sciences.add(science);
      this.ui.onScienceChange();
   }

   public synchronized void removeCurrentScience(Science.ScienceType science) {
      this.currentSciences.remove(science);
   }

   public synchronized void addCurrentScience(Science.ScienceType science) {
      this.currentSciences.add(science);
   }

   public void save(Callback<Void> callback) {
      this.save(this.currentGameName, true, callback);
   }

   public String getAutosaveName(String saveName) {
      if (saveName == null || saveName.length() == 0) {
         return "Autosave";
      } else {
         return saveName.endsWith(" Autosave") ? saveName : saveName + " Autosave";
      }
   }

   public boolean saveExists(String name) {
      return Quarry.Q.file("TheQuarry/saves/" + getFileName(name) + ".qsf", false).exists();
   }

   public void save(String name, boolean thumbnail, final Callback<Void> callback) {
      this.saveName = name;
      if (thumbnail) {
         this.saveMap = true;
         this.saveCallback = callback;
      } else {
         Quarry.Q.threadPool.execute(new Runnable() {
            @Override
            public void run() {
               Game.this.saveData(Game.this.saveName, false, false);
               if (callback != null) {
                  callback.call(null);
               }
            }
         });
      }
   }

   public static String getFileName(String saveName) {
      if (saveName == null) {
         saveName = Quarry.Q.i18n.get("ui.unnamed_save");
      }

      return fileRegex.matcher(saveName).replaceAll("_").toLowerCase();
   }

   protected void saveData(String save, boolean isAutosave, boolean saveMap) {
      Delta.r();
      if (save == null && isAutosave) {
         save = Quarry.Q.i18n.get("ui.unnamed_save");
      }

      // 序列化会读整个世界状态、并用共享的 NBT 缓冲，必须与模拟线程互斥。
      // 本方法跑在 Quarry.threadPool 上（见 draw 里的 autosave/thumbnail 分支）。
      synchronized (this.simLock) {
         this.saveDataLocked(save, isAutosave, saveMap);
      }
   }

   private void saveDataLocked(String save, boolean isAutosave, boolean saveMap) {
      try {
         NBT.Builder metaBuilder = new NBT.Builder("Meta");
         this.playTime = this.getPlayTime();
         this.lastTimerStart = System.currentTimeMillis();
         metaBuilder.Byte("version", (byte)23)
            .Int("build", Quarry.Q.versionNumber)
            .Byte("full", (byte)(Quarry.Q.fullVersion ? 1 : 0))
            .Long("playTime", this.playTime)
            .Long("seed", Generator.G.getSeed())
            .String("name", save);
         if (saveMap) {
            metaBuilder.ByteArray("thumbnail", this.bafh.getBytes());
         }

         NBT.CompoundTag meta = metaBuilder.Get();
         String saveName = save;
         if (isAutosave) {
            saveName = this.getAutosaveName(save);
         }

         String file = getFileName(saveName);
         FileHandle fh = Quarry.Q.file("TheQuarry/saves/" + file + ".qmf", true);
         if (fh.exists()) {
            fh.copyTo(Quarry.Q.file("TheQuarry/saves/" + file + "-old.qmf", true));
         }

         NBT.write(fh.write(false), meta, NBT.CompressionType.Fast);
         meta.free();
         NBT.CompoundTag data = this.buildSaveTag(save);
         System.out.println("[SimHash] save=" + saveName + " tick=" + this.simTick + " hash=" + Long.toHexString(this.simHash()));
         FileHandle fh1 = Quarry.Q.file("TheQuarry/saves/" + file + ".qsf", true);
         if (fh1.exists()) {
            fh1.copyTo(Quarry.Q.file("TheQuarry/saves/" + file + "-old.qsf", true));
         }

         NBT.write(fh1.write(false), data, NBT.CompressionType.Fast);
         if (Quarry.Q.desktop && Quarry.Q.version.equals("debug")) {
            Quarry.Q.file("TheQuarry/saves/" + file + ".txt", true).writeString(data.toString(), false);
         }

         saveThumbnailCache.remove(file);
         data.free();
         this.postUi(() -> this.ui.toast.show(Quarry.Q.i18n.get("toast.game_saved")));
         if (this.saveCallback != null) {
            this.saveCallback.call(null);
            this.saveCallback = null;
         }
         } catch (Exception var19) {
            Quarry.Q.pi.message(1, var19);
            this.postUi(() -> this.ui.toast.show(Quarry.Q.i18n.get("toast.save_error")));
         }
   }

   /**
    * 把当前世界拼成一个 NBT tag（内容与存盘完全一致，但不落盘）。联机重同步时直接序列化到内存传输，
    * 也可以拿它做「序列化 → 解析 → 应用」的内存 round-trip 自检。调用方负责 free()。
    */
   public NBT.CompoundTag buildSaveTag(String save) {
      NBT.Builder builder = new NBT.Builder("Save");
      builder.Byte("version", (byte)23)
         .Int("build", Quarry.Q.versionNumber)
         .Byte("full", (byte)(Quarry.Q.fullVersion ? 1 : 0))
         .Long("playTime", this.playTime)
         .String("name", save)
         .Short("layer", (short)this.layerIndex)
         .Byte("infinite", (byte)(this.infinite ? 1 : 0))
         .Long("seed", Generator.G.getSeed())
         .LongArray("rng", Generator.G.getState())
         .LongArray("simrng", SimRandom.G.getState())
         .List("Map", NBT.TagType.Compound);
      synchronized (layerLock) {
         for (Layer l : this.layers) {
            l.save(builder);
         }
      }

      builder.End().Compound("Resources");
      synchronized (resourceLock) {
         Array<Short> seen = new Array();

         for (Item.ItemType seenResource : this.getSeenResources()) {
            seen.add(seenResource.value);
         }

         short[] s = new short[seen.size];
         int i = 0;

         for (Short q : seen) {
            s[i++] = q;
         }

         Util.NBTwriteInventory(builder, this.resources);
         builder.ShortArray("Seen", s);
      }

      builder.End();
      if (this.sciences.size() > 0) {
         byte[] science = new byte[this.sciences.size()];
         int i = 0;

         for (Science.ScienceType s : this.sciences) {
            science[i++] = s.id;
         }

         builder.ByteArray("Sciences", science);
      }

      builder.Compound("camera")
         .Float("x", this.cam.position.x)
         .Float("y", this.cam.position.y)
         .Float("z", this.cam.position.z)
         .Float("zoom", this.cam.zoom)
         .End();
      return builder.Get();
   }

   /**
    * 把当前世界序列化成一段字节（NBT + LZ4），联机时发给加入方。必须在渲染线程调用。
    */
   public byte[] serializeWorld() throws IOException {
      NBT.CompoundTag tag = this.buildSaveTag(this.currentGameName == null ? "net" : this.currentGameName);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      NBT.write(baos, tag, NBT.CompressionType.Fast);
      tag.free();
      return baos.toByteArray();
   }

   /**
    * 计算一段世界字节的哈希：只解析 + 遍历 NBT，不应用、不碰模拟状态（可在网络线程调用）。
    * 主机发送前与客户端收到后都用它，两边数字相等即说明「世界传输是忠实的」，
    * 和 simHash（含 simTick 与本地输入）不一致是两回事。
    */
   public long worldBytesHash(byte[] bytes) {
      try {
         NBT.CompoundTag tag = NBT.read(new ByteArrayInputStream(bytes), NBT.CompressionType.Fast);
         SimHash h = new SimHash();
         h.hashTag(tag);
         tag.free();
         return h.value();
      } catch (Exception var3) {
         Quarry.Q.pi.message(1, var3);
         return 0L;
      }
   }

   /**
    * 用主机发来的世界字节替换当前世界（加入方调用）。必须在渲染线程调用。
    * 注意：不推进 simTick，M1c 做锁步时会由主机把 tick 一起带过来。
    */
   public void applyWorldBytes(byte[] bytes, long tick, boolean paused, int speed) {
      // 由 NetSession 的 postRunnable 触发（渲染线程，但在 render() 之外），必须自己加锁。
      // worldApplying 挡住模拟线程：layers 是在下面的延迟 runnable 里才换上的，
      // 在那之前模拟线程会拿着"旧世界 + 新 tick 编号"跑，两端必然分叉。
      this.worldApplying = true;
      synchronized (this.simLock) {
         this.applyWorldBytesLocked(bytes, tick, paused, speed);
      }
   }

   private void applyWorldBytesLocked(byte[] bytes, long tick, boolean paused, int speed) {
      try {
         NBT.CompoundTag data = this.loadSaveData(new ByteArrayInputStream(bytes));
         NBT.CompoundTag meta = new NBT.Builder("Meta")
            .Int("build", Quarry.Q.versionNumber)
            .Long("playTime", this.playTime)
            .Long("seed", Generator.G.getSeed())
            .String("name", "net")
            .Get();
         Vector3 camBefore = this.cam.position.cpy();
         float zoomBefore = this.cam.zoom;
         final int layerBefore = this.layerIndex;
         this.reset();
         this.applySaveTag(meta, data, new Callback<Object>() {
            public void call(Object d) {
               if (d != null && d != Boolean.TRUE) {
                  Quarry.Q.pi.message(1, d instanceof Throwable ? (Throwable)d : new RuntimeException(String.valueOf(d)));
               }
            }
         }, Quarry.Q.versionNumber);
         meta.free();
         // 相机是每个玩家自己的视角：存档里带了主机的相机，applySaveTag 会顺手应用它，
         // 那样一重同步（甚至只是加入）客户端的视角就被主机"抢"走了。这里恢复本机视角。
         this.cam.position.set(camBefore);
         this.cam.zoom = zoomBefore;
         this.cam.update();
         this.simTick = tick;
         // 当前所在层同样是"每个玩家自己的视角"：存档里带的是主机的层，applySaveTag 会切过去，
         // 这里在它的换层 runnable 之后排队恢复本机的层（FIFO，所以能看到新 layers）
         // 当前所在层同样是"每个玩家自己的视角"：存档里带的是主机的层，applySaveTag 会切过去，
         // 这里在它的换层 runnable 之后排队恢复本机的层（FIFO，所以能看到新 layers）。
         // 走 postUi 是为了在 simLock 内改 layerIndex/layer（模拟线程会读它们）。
         // 它排在 applySaveTag 的换层 runnable 之后，是最后一个，所以在这里解除 worldApplying。
         this.postUi(new Runnable() {
            @Override
            public void run() {
               if (Game.this.layers != null && layerBefore >= 0 && layerBefore < Game.this.layers.size) {
                  Game.this.layerIndex = layerBefore;
                  Game.this.layer = Game.this.layers.get(layerBefore);
                  Game.this.layerChangeNotifier.notify(ChangeNotifier.Event.Type.CHANGE, Game.this.layer);
               }

               Game.this.worldApplying = false;
            }
         });
         // 暂停/倍速跟随主机（世界 tag 里不带这两个值），且必须立刻生效，
         // 否则命令生效前的那几 tick 两端速度不同，会被判定为漂移
         this.applyPause(paused);
         this.applySpeed(speed);
         this.ui.pauseButton.setChecked(paused);
         // 与主机从同一 tick 起步；起点之前的对端命令包豁免等待（那时对端还不存在）
         NetSession.markStartTick(tick);
         try {
            System.out.println("[Net] world applied, tick=" + this.simTick + " hash=" + Long.toHexString(this.simHash()));
         } catch (Exception var5) {
            Quarry.Q.pi.message(1, var5);
         }
      } catch (Exception var4) {
         Quarry.Q.pi.message(1, var4);
         System.out.println("[Net] 应用世界失败：" + var4);
         // 失败也要解除 worldApplying：否则它一直为 true，模拟线程永久停 tick
         this.worldApplying = false;
      }
   }

   /**
    * 内存 round-trip 自检：存档 → 序列化到内存 → 解析回来 → 应用 → 比对 simHash。
    * 不需要存档文件，也不需要第二台机器，按 F11 触发。前后的哈希必须相等。
    */
   public void roundTripSelfCheck() {
      final long before = this.simHash();

      try {
         final byte[] bytes = this.serializeWorld();
         NBT.CompoundTag meta = new NBT.Builder("Meta")
            .Int("build", Quarry.Q.versionNumber)
            .Long("playTime", this.playTime)
            .Long("seed", Generator.G.getSeed())
            .String("name", "selfcheck")
            .Get();
         NBT.CompoundTag parsed = NBT.read(new ByteArrayInputStream(bytes), NBT.CompressionType.Fast);
         this.applySaveTag(meta, parsed, new Callback<Object>() {
            public void call(Object data) {
               if (data != null && data != Boolean.TRUE) {
                  Quarry.Q.pi.message(1, data instanceof Throwable ? (Throwable)data : new RuntimeException(String.valueOf(data)));
               }
            }
         }, Quarry.Q.versionNumber);
         meta.free();
         // applySaveTag 内部用 Gdx.app.postRunnable 换层，所以对比必须在它之后排队（FIFO）
         Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
               long after = Game.this.simHash();
               System.out.println(
                  "[SimHash] roundtrip bytes="
                     + bytes.length
                     + " before="
                     + Long.toHexString(before)
                     + " after="
                     + Long.toHexString(after)
                     + (before == after ? " OK" : " MISMATCH")
               );
            }
         });
      } catch (Exception var8) {
         Quarry.Q.pi.message(1, var8);
      }
   }

   public Texture getThumbnail(String filename) {
      return saveThumbnailCache.get(filename);
   }

   protected void updateThumbnailCache(String filename, Pixmap p) {
      Texture t = saveThumbnailCache.get(filename);
      if (t != null) {
         t.dispose();
      }

      saveThumbnailCache.put(filename, new Texture(p));
   }

   public NBT.CompoundTag loadMetaData(String filename) throws IOException {
      if (!Quarry.Q.file("TheQuarry/saves/" + filename + ".qmf", false).exists()) {
         FileHandle qsf = Quarry.Q.file("TheQuarry/saves/" + filename + ".qsf", false);
         if (!qsf.exists()) {
            throw new FileNotFoundException();
         } else {
            NBT.CompoundTag data = null;

            try {
               data = NBT.read(new BufferedInputStream(Quarry.Q.file("TheQuarry/saves/" + filename + ".qsf", false).read()), NBT.CompressionType.Fast);
            } catch (Exception var6) {
               Quarry.Q.pi.message(1, var6);
            }

            NBT.Builder metaBuilder = new NBT.Builder("Meta");
            metaBuilder.Byte("version", (byte)23)
               .Int("build", data != null ? data.Int("build", 0) : 0)
               .Byte("full", (byte)(Quarry.Q.fullVersion ? 1 : 0))
               .Long("playTime", data != null ? data.Long("build", 0L) : 0L)
               .Long("seed", data != null ? data.Long("seed", 0L) : 0L)
               .String("name", data != null ? data.String("name", filename) : filename);
            NBT.CompoundTag meta = metaBuilder.Get();
            NBT.write(Quarry.Q.file("TheQuarry/saves/" + filename + ".qmf", true).write(false), meta, NBT.CompressionType.Fast);
            return meta;
         }
      } else {
         NBT.CompoundTag data = NBT.read(new BufferedInputStream(Quarry.Q.file("TheQuarry/saves/" + filename + ".qmf", false).read()), NBT.CompressionType.Fast);
         if (!saveThumbnailCache.containsKey(filename) && data.has("thumbnail")) {
            try {
               ByteArrayFileHandle bafh = new ByteArrayFileHandle(data.ByteArray("thumbnail"));
               Pixmap p = PixmapIO.readCIM(bafh);
               this.updateThumbnailCache(filename, p);
               p.dispose();
            } catch (NBT.NBTException var7) {
               Quarry.Q.pi.message(1, var7);
            }
         }

         return data;
      }
   }

   public NBT.CompoundTag loadSaveData(String filename) throws IOException {
      if (!Quarry.Q.file("TheQuarry/saves/" + filename + ".qsf", false).exists()) {
         throw new FileNotFoundException();
      } else {
         return this.loadSaveData(Quarry.Q.file("TheQuarry/saves/" + filename + ".qsf", false).read());
      }
   }

   /**
    * 从任意输入流读一份存档 tag（不落盘也能重同步）。压缩类型与文件版一致。
    */
   public NBT.CompoundTag loadSaveData(InputStream in) throws IOException {
      long t = System.currentTimeMillis();
      NBT.CompoundTag data = NBT.read(new BufferedInputStream(in), NBT.CompressionType.Fast);
      System.out.println("NBT loading took " + (System.currentTimeMillis() - t));
      return data;
   }

   public void load(final String filename, final Callback<Object> callback) {
      try {
         this.reset();
         System.out.println("Loading " + filename);
         final NBT.CompoundTag meta = this.loadMetaData(filename);
         if (meta == null) {
            callback.call(false);
            return;
         }

         final int build = meta.Int("build", 0);
         boolean full = meta.Byte("full", (byte)0) == 1;
         if (build > Quarry.Q.versionNumber && Quarry.Q.isVersion()) {
            if (Quarry.Q.getScene() == this) {
               this.ui.alert.show(this.ui, Quarry.Q.i18n.get("alert.newer_version"), new Callback<Void>() {
                  public void call(Void data) {
                     callback.call(false);
                  }
               });
            } else {
               ((MainMenu)Quarry.Q.getScene()).alert.show((MainMenu)Quarry.Q.getScene(), Quarry.Q.i18n.get("alert.newer_version"), new Callback<Void>() {
                  public void call(Void data) {
                     callback.call(false);
                  }
               });
            }
         } else if (full && !Quarry.Q.fullVersion) {
            if (Quarry.Q.getScene() == this) {
               this.ui.alert.show(this.ui, Quarry.Q.i18n.get("alert.full_version_save"), new Callback<Void>() {
                  public void call(Void data) {
                     callback.call(false);
                  }
               });
            } else {
               ((MainMenu)Quarry.Q.getScene()).alert.show((MainMenu)Quarry.Q.getScene(), Quarry.Q.i18n.get("alert.full_version_save"), new Callback<Void>() {
                  public void call(Void data) {
                     callback.call(false);
                  }
               });
            }
         } else if (!full && Quarry.Q.fullVersion) {
            if (Quarry.Q.getScene() == this) {
               this.ui.confirm.show(this.ui, Quarry.Q.i18n.get("confirm.demo_version_save"), new Callback<Boolean>() {
                  public void call(Boolean data) {
                     if (data == null) {
                        callback.call(null);
                     } else if (data) {
                        Game.this.loadData(meta, filename, callback, build);
                     } else {
                        callback.call(false);
                     }
                  }
               });
            } else {
               ((MainMenu)Quarry.Q.getScene())
                  .confirm
                  .show((MainMenu)Quarry.Q.getScene(), Quarry.Q.i18n.get("confirm.demo_version_save"), new Callback<Boolean>() {
                     public void call(Boolean data) {
                        if (data == null) {
                           callback.call(null);
                        } else if (data) {
                           Game.this.loadData(meta, filename, callback, build);
                        } else {
                           callback.call(false);
                        }
                     }
                  });
            }
         } else {
            this.loadData(meta, filename, callback, build);
         }
      } catch (Exception var6) {
         callback.call(var6);
      }
   }

   protected void loadData(NBT.CompoundTag meta, String filename, Callback<Object> callback, final int build) {
      try {
         NBT.CompoundTag data = this.loadSaveData(filename);
         if (data == null) {
            callback.call(false);
            return;
         }

         this.applySaveTag(meta, data, callback, build);
      } catch (Exception var22) {
         callback.call(var22);
      }
   }

   /**
    * 应用一份已解析的存档 tag：与文件读档的区别只是不碰文件，联机重同步时把对端发来的世界字节
    * 解析后直接灌进来。本方法内部会 free 掉 data；若要完全重同步，调用方先自己 reset()。
    * 注意换层是通过 Gdx.app.postRunnable 延后到下一帧执行的。
    */
   public void applySaveTag(NBT.CompoundTag meta, NBT.CompoundTag data, Callback<Object> callback, final int build) {
      try {
         long t0 = System.currentTimeMillis();
      if (build < Quarry.Q.versionNumber) {
         LoadingCompat.instance.upgrade(data, build);
      }

         this.currentGameName = meta.String("name");
         this.playTime = meta.Long("playTime", 0L);
         this.lastTimerStart = System.currentTimeMillis();
         this.infinite = data.Byte("infinite", (byte)0) == 1;
         Generator.G.setSeed(data.Long("seed", 0L));
         if (data.has("rng")) {
            Generator.G.setState(data.LongArray("rng"));
         }

         SimRandom.G.setSeed(Generator.G.getSeed());
         if (data.has("simrng")) {
            SimRandom.G.setState(data.LongArray("simrng"));
         }

         try {
            this.layerIndex = data.Short("layer");
         } catch (NBT.NBTException var21) {
            this.layerIndex = data.Byte("layer");
            if (this.layerIndex < 0) {
               this.layerIndex = 0;
            }
         }

         NBT.ListTag map = data.List("Map", NBT.TagType.Compound);
         final Array<Layer> myLayers = new Array();
         int index = 0;

         for (NBT.Tag c : map.data) {
            Layer l = Layer.load(index++, (NBT.CompoundTag)c);
            myLayers.add(l);
         }

         Layer active = (Layer)myLayers.get(this.layerIndex);
         NBT.CompoundTag camera = data.Compound("camera");
         if (camera != null) {
            this.cam
               .position
               .set(camera.Float("x", (float)(active.width * 64 / 2)), camera.Float("y", (float)(active.height * 64 / 2)), camera.Float("z", 0.0F));
            this.cam.zoom = camera.Float("zoom", 0.5F);
            this.cam.update();
         }

         NBT.CompoundTag res = data.Compound("Resources");
         Util.NBTreadInventory(res, this.resources);
         this.getSeenResources().clear();
         short[] seen = res.ShortArray("Seen");

         for (short s : seen) {
            Item.ItemType t = Item.get(s);
            if (t != null) {
               this.getSeenResources().add(t);
            }
         }

         byte[] science = data.ByteArray("Sciences", null);
         if (science != null) {
            this.sciences.clear();

            for (byte sx : science) {
               this.sciences.add(Science.sciences[sx & 255]);
            }
         }

         this.sciences.add(Science.ScienceType.Start);
         this.ui.buildMenuSciences.clear();
         this.ui.onScienceChange();
         data.free();
         this.ui.updateResources(true);

         for (Layer l : myLayers) {
            l.dirtyBounds.set(0, 0, l.width, l.height, Integer.MAX_VALUE);
         }

         System.out.println("Game loading took " + (System.currentTimeMillis() - t0));
         Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
               synchronized (Game.layerLock) {
                  if (Game.this.layers == null) {
                     Game.this.layers = new Array();
                  }

                  if (Game.this.layers.size > 0) {
                     for (Layer l : Game.this.layers) {
                        l.dispose();
                     }

                     Game.this.layers.clear();
                  }

                  Game.this.layers.addAll(myLayers);

                  for (Layer l : Game.this.layers) {
                     l.postAllLayersLoad(true);
                  }

                  for (Layer l : Game.this.layers) {
                     l.postAllLayersLoad(false);
                  }

                  Game.this.powerGrid.clearHighPowerCache();
                  Game.this.layer = (Layer)Game.this.layers.get(Game.this.layerIndex);
                  Game.this.lastAutosave = System.currentTimeMillis();
                  System.gc();
               }

               Game.this.ui.updateResources(true);
               Game.this.layerChangeNotifier.notify(ChangeNotifier.Event.Type.BULK_ADD, null);
               if (build < 115) {
                  Game.this.recalcResources();
               }
            }
         });
         callback.call(true);
      } catch (Exception var22) {
         callback.call(var22);
      }
   }

   public void exportBlueprint() {
      NBT.Builder b = new NBT.Builder("Blueprint")
         .Int("build", Quarry.Q.versionNumber)
         .IntArray("region", G.copyRegion)
         .List("structures", NBT.TagType.Compound);

      for (Pair<Structure<?>, NBT.CompoundTag> p : G.copyStructures.values()) {
         b.add(p.getVal());
      }

      b.End().List("cables", NBT.TagType.Compound);

      for (Pair<Structure<?>, NBT.CompoundTag> p : G.copyCables.values()) {
         b.add(p.getVal());
      }

      b.End();

      try {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         NBT.write(baos, b.Get(), NBT.CompressionType.Small);
         Quarry.Q.pi.message(18, new String(Base64Coder.encode(baos.toByteArray())));
      } catch (IOException var4) {
         var4.printStackTrace();
      }
   }

   public void importBlueprint() {
      Object payload = Quarry.Q.pi.message(17, null);
      if (payload == null) {
         this.ui.toast.show(Quarry.Q.i18n.get("toast.clipboard_empty"));
      } else {
         NBT.CompoundTag tag = null;

         try {
            byte[] data = Base64Coder.decode(payload.toString().trim());
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            tag = NBT.read(bais, NBT.CompressionType.Small);
         } catch (Exception var12) {
            var12.printStackTrace();
            this.ui.toast.show(Quarry.Q.i18n.get("toast.clipboard_invalid"));
            return;
         }

         try {
            int build = tag.Int("build");
            if (build > Quarry.Q.versionNumber) {
               this.ui.alert.show(this.ui, Quarry.Q.i18n.get("alert.blueprint_newer_version"), null);
               return;
            }

            this.copyRegion = tag.IntArray("region");
            Vector3 worldPos = this.cam.unproject(new Vector3((float)(Gdx.graphics.getWidth() / 2), (float)(Gdx.graphics.getHeight() / 2), 0.0F));
            this.copyRegion[0] = MathUtils.clamp((int)(worldPos.x / 64.0F - (float)this.copyRegion[2] / 2.0F), 0, this.layer.width - this.copyRegion[2]);
            this.copyRegion[1] = MathUtils.clamp((int)(worldPos.y / 64.0F - (float)this.copyRegion[3] / 2.0F), 0, this.layer.height - this.copyRegion[3]);
            this.copyStructures.clear();
            this.copyCables.clear();
            this.copyCost.clear();

            for (NBT.Tag t : tag.List("structures", NBT.TagType.Compound).data) {
               Structure<?> s = Structure.loadPaste(this.copyRegion, (NBT.CompoundTag)t);
               if (!this.hasSciences(s.getSchema().sciencesRequired) && !GOD_MODE) {
                  this.ui.alert.show(this.ui, Quarry.Q.i18n.get("alert.blueprint_insufficient_science"), null);
                  return;
               }

               this.copyStructures.put(s.x * this.layer.height + s.y, new Pair<>(s, (NBT.CompoundTag)t));

               for (Item.Items.Amount a : s.getSchema().buildCosts.entries) {
                  this.copyCost.put(a.getItem(), a.getAmount() + (this.copyCost.containsKey(a.getItem()) ? this.copyCost.get(a.getItem()) : 0));
               }
            }

            for (NBT.Tag t : tag.List("cables", NBT.TagType.Compound).data) {
               Structure<?> s = Structure.loadPaste(this.copyRegion, (NBT.CompoundTag)t);
               if (!this.hasSciences(s.getSchema().sciencesRequired) && !GOD_MODE) {
                  this.ui.alert.show(this.ui, Quarry.Q.i18n.get("alert.blueprint_insufficient_science"), null);
                  return;
               }

               this.copyCables.put(s.x * this.layer.height + s.y, new Pair<>(s, (NBT.CompoundTag)t));

               for (Item.Items.Amount a : s.getSchema().buildCosts.entries) {
                  this.copyCost.put(a.getItem(), a.getAmount() + (this.copyCost.containsKey(a.getItem()) ? this.copyCost.get(a.getItem()) : 0));
               }
            }

            for (Pair<Structure<?>, NBT.CompoundTag> s : this.copyStructures.values()) {
               s.getKey().paste(this.copyRegion, s.getVal());
            }

            for (Pair<Structure<?>, NBT.CompoundTag> s : this.copyCables.values()) {
               s.getKey().paste(this.copyRegion, s.getVal());
            }
         } catch (Exception var13) {
            var13.printStackTrace();
            this.ui.toast.show(Quarry.Q.i18n.get("toast.clipboard_invalid"));
            return;
         }

         this.ui.hideTooltip();
         this.ui.hideBuildMenu();
         this.ui.hideScienceUI();
         this.resetActiveStructure();
         this.ui.copyButton.setChecked(true);
         this.copyMode = false;
         this.pasteMode = true;
         this.ui.updateCopyTableResources(true);
         this.camControl.updateActiveElementPlaceable();
      }
   }

   public void resetActiveStructure() {
      this.activeStructure = null;
      this.endA.x = -1.0F;
      this.endB.x = -1.0F;
      this.activeStructureTrail.clear();
      this.ui.hideTooltip();
      this.ui.clearBuildSelection();
   }

   public void resetActiveTile() {
      this.activeTile.x = -1.0F;
   }

   @Override
   public void dispose() {
      this.stopSimThread();
      for (FrameBuffer fbo : this.chunkFBOs) {
         if (fbo != null) {
            fbo.dispose();
         }
      }

      this.batch.dispose();
      synchronized (layerLock) {
         if (this.layers.size > 0) {
            for (Layer l : this.layers) {
               l.dispose();
            }
         }
      }

      if (this.fbo != null) {
         this.fbo.dispose();
      }

      for (Texture t : saveThumbnailCache.values()) {
         t.dispose();
      }

      this.colorShader.dispose();
      this.music.dispose();
      this.spriter.dispose();
      this.shaper.dispose();
   }

   @Override
   public void resize(int width, int height) {
      this.viewport.update(width, height);
      this.ui.resize(width, height);
   }

   @Override
   public boolean keyDown(int keycode) {
      if (Quarry.Q.version.equals("debug")) {
         switch (keycode) {
            case 31:
               RECORDMODE = true;
               break;
            case 32:
               DRAW_DEBUG = !DRAW_DEBUG;
               this.ui.toggleDebug();
            case 33:
            case 37:
            case 38:
            case 41:
            case 42:
            case 45:
            case 47:
            default:
               break;
            case 34:
               FLUIDMODE = !FLUIDMODE;
               break;
            case 35:
               GOD_MODE = !GOD_MODE;
               this.ui.onScienceChange();
               break;
            case 36:
               SINGLE_FRAME = true;
               break;
            case 39:
               this.recalcResources();
               break;
            case 40:
               this.addLayer();
               break;
            case 43:
               this.resetSpeed();
               break;
            case 44:
               this.increaseSpeed();
               break;
            case 46:
               this.layer.dirtyBounds.set(0, 0, this.layer.width, this.layer.height, Integer.MAX_VALUE);
               break;
            case 48:
               for (Chunk c : this.layer.getChunks()) {
                  if (c != null) {
                     c.dirty = true;
                  }
               }
               break;
            case 49:
               FOGMODE = true;
               break;
            case 50:
               SCREENSHOT = true;
               break;
            case 51:
               for (int i = 0; i < this.layer.width; i++) {
                  for (int j = 0; j < this.layer.height; j++) {
                     this.layer.removeMeta(i, j, 1);
                  }
               }
               break;
            case 52:
               FILLMODE = true;
         }
      }

      switch (keycode) {
         case 9:
            if (!this.ui.layerSelection.up10.isDisabled()) {
               this.ui.layerSelection.up10.setChecked(!this.ui.layerSelection.up10.isChecked());
            }

            return true;
         case 10:
            if (this.ui.destroyButton.isChecked()) {
               this.ui.bulkButton.setChecked(!this.ui.bulkButton.isChecked());
            }

            return true;
         case 11:
            if (this.ui.destroyButton.isChecked()) {
               this.ui.bulkCableButton.setChecked(!this.ui.bulkCableButton.isChecked());
            }

            return true;
         case 12:
            if (this.ui.destroyButton.isChecked()) {
               this.ui.cableRemoveButton.setChecked(!this.ui.cableRemoveButton.isChecked());
            }

            return true;
         case 19:
            if (!this.ui.layerSelection.up.isDisabled()) {
               this.ui.layerSelection.up.setChecked(!this.ui.layerSelection.up.isChecked());
            }

            return true;
         case 20:
            if (!this.ui.layerSelection.down.isDisabled()) {
               this.ui.layerSelection.down.setChecked(!this.ui.layerSelection.down.isChecked());
            }

            return true;
         case 31:
            if (Gdx.input.isKeyPressed(129) || Gdx.input.isKeyPressed(130)) {
               if (this.ui.copyButton.isVisible()) {
                  this.ui.copyButton.setChecked(true);
               }

               return true;
            }

            this.resetActiveStructure();
            if (G.hasScience(Science.ScienceType.Electricity) || GOD_MODE) {
               this.ui.cableRemoveButton.setChecked(!this.ui.destroyButton.isChecked());
               this.ui.destroyButton.setChecked(!this.ui.destroyButton.isChecked());
               return true;
            }
         case 54:
            if (this.ui.currentClickedStructure instanceof ProducerStructure) {
               this.ui.structureUIButtons[0].setChecked(!this.ui.structureUIButtons[0].isChecked());
               return true;
            }
            break;
         case 33:
         case 67:
         case 112:
            this.resetActiveStructure();
            this.ui.destroyButton.setChecked(!this.ui.destroyButton.isChecked());
            return true;
         case 34:
            this.ui.flipActiveStructure();
            return true;
         case 35:
            if (Gdx.input.isKeyPressed(129) || Gdx.input.isKeyPressed(130)) {
               GOD_MODE = !GOD_MODE;
               this.ui.onScienceChange();
            }

            return true;
         case 40:
            if (GOD_MODE) {
               this.addLayer();
            }

            return true;
         case 41:
            INCREASE_MODE = !INCREASE_MODE;
            Quarry.Q.prefs.putBoolean("increase_mode", INCREASE_MODE).flush();
            return true;
         case 44:
            if (this.ui.currentClickedStructure instanceof StorageStructure || this.ui.currentClickedStructure instanceof Tank) {
               this.ui.structureUIButtons[0].setChecked(!this.ui.structureUIButtons[0].isChecked());
            }

            return true;
         case 45:
            this.ui.destroyButton.setChecked(false);
            if (!this.ui.buildButton.isChecked()) {
               this.ui.showBuildMenu();
               this.ui.buildButton.setChecked(true);
            } else {
               this.ui.hideBuildMenu();
               this.ui.buildButton.setChecked(false);
            }

            return true;
         case 46:
            this.ui.rotateActiveStructure();
            return true;
         case 52:
            if (!this.ui.layerSelection.down10.isDisabled()) {
               this.ui.layerSelection.down10.setChecked(!this.ui.layerSelection.down10.isChecked());
            }

            return true;
         case 62:
            this.setPaused(!this.isPaused());
            this.ui.pauseButton.setChecked(this.isPaused());
            return true;
         case 131:
            this.ui.menu.menuButton.setChecked(!this.ui.menu.menuButton.isChecked());
            return true;
         case 244:
            UI_VISIBLE = !UI_VISIBLE;
            break;
         case 247:
            SMOOTH_CAMERA = !SMOOTH_CAMERA;
            break;
         case 250:
            NetSession.disconnect();
            return true;
         case 251:
            NetSession.host(NetSession.DEFAULT_PORT);
            return true;
         case 252:
            this.ui.prompt.show(this.ui, "联机加入：输入 ip[:端口]", "127.0.0.1:" + NetSession.DEFAULT_PORT, new Response<String, Boolean>() {
               public Boolean call(String data) {
                  NetSession.join(data);
                  return true;
               }
            });
            return true;
         case 253:
            LOCKSTEP = !LOCKSTEP;
            Quarry.Q.prefs.putBoolean("lockstep", LOCKSTEP).flush();
            System.out.println("[Lockstep] " + LOCKSTEP);
            return true;
         case 254:
            this.roundTripSelfCheck();
            return true;
         case 255:
            System.out.println("[SimHash] tick=" + this.simTick + " hash=" + Long.toHexString(this.simHash()));
            return true;
      }

      return false;
   }

   @Override
   public InputProcessor getInput() {
      return this.input;
   }

   public float getZoom() {
      return this.cam.zoom;
   }

   public OrthographicCamera getCamera() {
      return this.cam;
   }

   public void highlightArea(int... tiles) {
      synchronized (highlightLock) {
         this.tutorialHighlight.clear();
         this.tutorialHighlight.addAll(tiles);
      }
   }

   public IntSet getTutorialHighlight() {
      return this.tutorialHighlight;
   }

   public long getPlayTime() {
      return this.playTime + (System.currentTimeMillis() - this.lastTimerStart);
   }

   @Override
   public void pause() {
      super.pause();
      this.playTime = this.getPlayTime();
   }

   @Override
   public void resume() {
      super.resume();
      this.lastTimerStart = System.currentTimeMillis();
   }

   public boolean isInfinite() {
      return this.infinite;
   }

   public void setInfinite(boolean infinite) {
      this.infinite = infinite;
   }

   public boolean isPaused() {
      return this.gamePaused;
   }

   public void setPaused(boolean value) {
      if (NetSession.isActive()) {
         // 联机：暂停由主机裁决，且要等 tick 边界两端同时生效（否则两边 tick 流会分叉）
         if (NetSession.isHost() && value != this.gamePaused) {
            NetSession.issue(NetCommand.pause(value));
         }

         return;
      }

      this.gamePaused = value;
   }

   private void applyPause(boolean value) {
      this.gamePaused = value;
   }

   public void play() {
      this.gamePaused = false;
      this.gameSpeed = 1;
   }

   public int getGameSpeed() {
      return this.gameSpeed;
   }

   public void resetSpeed() {
      if (NetSession.isActive()) {
         if (NetSession.isHost()) {
            NetSession.issue(NetCommand.speed(1));
         }

         return;
      }

      this.gameSpeed = 1;
   }

   public void increaseSpeed() {
      if (NetSession.isActive()) {
         if (NetSession.isHost()) {
            NetSession.issue(NetCommand.speed(Math.min(this.gameSpeed * 2, 100)));
         }

         return;
      }

      this.gameSpeed = Math.min(this.gameSpeed * 2, 100);
   }

   private void applySpeed(int speed) {
      this.gameSpeed = speed;
   }

   public int getItemCount() {
      int sum = 0;
      synchronized (layerLock) {
         for (Layer l : this.layers) {
            sum += l.getItemCount();
         }

         return sum;
      }
   }

   public int getStructureCount() {
      int sum = 0;
      synchronized (layerLock) {
         for (Layer l : this.layers) {
            sum += l.getStructureCount();
         }

         return sum;
      }
   }

   public boolean changeLayer(int delta) {
      if (this.layerIndex == 0 && delta < 0) {
         return false;
      } else if (this.layerIndex + delta >= this.layers.size) {
         return false;
      } else {
         this.deltaLayer = delta;
         return true;
      }
   }

   public int getLayerCount() {
      return this.layers.size;
   }

   /**
    * 世界状态哈希：把会进存档的模拟状态压成一个 64 位值。
    * 同一存档 / 同一 simTick 必须得到相同结果——用来验证确定性（同种子同 tick 应一致、读档后应稳定），
    * 也是将来联机快照比对的雏形。按 F12 打印，存档时也会打一条。
    *
    * 注意：内部会把每层重新序列化成 NBT，工厂很大时有几十毫秒开销，只在调试时调用。
    */
   public long simHash() {
      SimHash h = new SimHash();
      h.put(this.simTick);
      h.put(this.infinite);
      h.put(Generator.G.getSeed());
      h.put(Generator.G.getState());
      h.put(SimRandom.G.getState());
      synchronized (layerLock) {
         h.put(this.layers.size);
         // 注意：Layer.save 写出的是「无名 compound」，只能挂进 List（存档路径就是这么用的），
         // 直接挂到 compound 下会被 NBT 拒绝（Compound Tag expects named tags）。
         NBT.Builder b = new NBT.Builder("Hash");
         b.List("Map", NBT.TagType.Compound);

         for (Layer l : this.layers) {
            l.save(b);
         }

         b.End();
         h.hashTag(b.Get());
      }

      for (Item.ItemType t : Item.ItemType.values) {
         h.put(t.ordinal());
         h.put(this.getResource(t));
      }

      for (Item.ItemType t : Item.ItemType.values) {
         h.put(t.ordinal());
         h.put(this.seenResources.contains(t));
      }

      for (Science.ScienceType s : Science.ScienceType.values) {
         h.put(s.ordinal());
         h.put(this.sciences.contains(s));
      }

      return h.value();
   }

   /**
    * 诊断用：把 simHash 拆成各子系统的哈希，联机分叉时打印，用来定位到底哪部分不一致。
    */
   public String simHashDebug() {
      StringBuilder sb = new StringBuilder();
      sb.append("tick=").append(this.simTick).append(" infinite=").append(this.infinite);

      SimHash rng = new SimHash();
      rng.put(Generator.G.getSeed());
      rng.put(Generator.G.getState());
      rng.put(SimRandom.G.getState());
      sb.append(" rng=").append(Long.toHexString(rng.value()));

      synchronized (layerLock) {
         sb.append(" layers=").append(this.layers.size);

         for (int i = 0; i < this.layers.size; i++) {
            Layer l = this.layers.get(i);
            NBT.Builder b = new NBT.Builder("L");
            b.List("Map", NBT.TagType.Compound);
            l.save(b);
            b.End();
            SimHash lh = new SimHash();
            lh.hashTag(b.Get());
            sb.append(" L").append(i).append("=").append(Long.toHexString(lh.value()));

            // 逐 chunk 哈希，用于进一步定位到具体哪一块（瓦片 + 结构）
            sb.append(" chunks=[");
            Chunk[] cs = l.getChunks();

            for (int ci = 0; ci < cs.length; ci++) {
               NBT.Builder cb = new NBT.Builder("C");
               cb.List("Map", NBT.TagType.Compound);
               cs[ci].save(cb);
               cb.End();
               SimHash cch = new SimHash();
               cch.hashTag(cb.Get());

               if (ci > 0) {
                  sb.append(' ');
               }

               sb.append(ci).append(':').append(Long.toHexString(cch.value()));

               // 逐结构哈希：定位到具体是哪个结构分叉
               Structure<?>[] ss = (Structure<?>[])cs[ci].getStructures().items;

               for (int si = 0; si < ss.length; si++) {
                  Structure<?> s = ss[si];
                  if (s == null) {
                     break;
                  }

                  NBT.Builder sbb = new NBT.Builder("S");
                  sbb.List("Map", NBT.TagType.Compound);
                  s.save(sbb);
                  sbb.End();
                  SimHash srh = new SimHash();
                  srh.hashTag(sbb.Get());
                  sb.append(' ').append(s.getSchema().type.name).append('@').append(s.x).append(',').append(s.y).append('=').append(Long.toHexString(srh.value()));
               }
            }

            sb.append(']');
         }
      }

      SimHash res = new SimHash();

      for (Item.ItemType t : Item.ItemType.values) {
         res.put(t.ordinal());
         res.put(this.getResource(t));
      }

      sb.append(" res=").append(Long.toHexString(res.value()));
      SimHash seen = new SimHash();

      for (Item.ItemType t : Item.ItemType.values) {
         seen.put(t.ordinal());
         seen.put(this.seenResources.contains(t));
      }

      sb.append(" seen=").append(Long.toHexString(seen.value()));
      SimHash sci = new SimHash();

      for (Science.ScienceType s : Science.ScienceType.values) {
         sci.put(s.ordinal());
         sci.put(this.sciences.contains(s));
      }

      sb.append(" sci=").append(Long.toHexString(sci.value()));
      return sb.toString();
   }

   public Layer getLayer(int index) {
      synchronized (layerLock) {
         return this.layers != null && index >= 0 && index < this.layers.size ? (Layer)this.layers.get(index) : null;
      }
   }

   public Layer addLayer() {
      synchronized (layerLock) {
         Layer l = new Layer(this.layers.size, 64, 64, Tile.TileType.Stone, true, false);
         Generator.G.generate(l);
         this.layers.add(l);
         this.layerChangeNotifier.notify(ChangeNotifier.Event.Type.ADD, l);
         return l;
      }
   }

   public void stopSfx() {
      synchronized (layerLock) {
         this.layer.stopSfx();
      }
   }

   private class LayerIterable implements Iterable<Layer> {
      private Game.LayerIterable.LayerIterator iter = new Game.LayerIterable.LayerIterator();

      private LayerIterable() {
      }

      @Override
      public Iterator<Layer> iterator() {
         this.iter.reset();
         return this.iter;
      }

      private class LayerIterator implements Iterator<Layer> {
         boolean current = false;
         int index = 0;

         private LayerIterator() {
         }

         public void reset() {
            this.current = false;
            this.index = 0;
         }

         @Override
         public boolean hasNext() {
            return !this.current || this.index < Game.this.layers.size - 1;
         }

         public Layer next() {
            if (!this.current) {
               this.current = true;
               return Game.this.layer;
            } else {
               if (this.index == Game.this.layer.getIndex()) {
                  this.index++;
               }

               return (Layer)Game.this.layers.get(this.index++);
            }
         }
      }
   }

   private static enum LiningUpState {
      NoOther,
      LiningUp,
      NotLiningUp;
   }

   public class QuarryCameraControl extends EditorCameraControl {
      boolean placingTrail;

      @Override
      public boolean touchUp(int screenX, int screenY, int pointer, int button) {
         Game.this.activeEnd = 0;
         return super.touchUp(screenX, screenY, pointer, button);
      }

      @Override
      public boolean mouseMoved(int screenX, int screenY) {
         if (Gdx.app.getType() == ApplicationType.Desktop) {
            this.viewport.unproject(this.tmp.set((float)screenX, (float)screenY));
            Game.this.hoverTileX = (int)(this.tmp.x / (float)this.tileSize);
            Game.this.hoverTileY = (int)(this.tmp.y / (float)this.tileSize);
            this.updateGhostToCursor();
            return true;
         } else {
            return false;
         }
      }

      private void updateGhostToCursor() {
         Structure<?> s = Game.this.activeStructure;
         if (s == null || Game.this.layer == null) {
            return;
         }

         if (Game.this.bulkDestroyMode
            || Game.this.bulkCableMode
            || Game.this.copyMode
            || Game.this.structureDestroyMode
            || Game.this.pasteMode) {
            return;
         }

         if (s.getSchema().has(Schema.Flags.Draggable)) {
            if (Game.this.endA.x > -1.0F) {
               if (Game.this.endB.x != (float)Game.this.hoverTileX || Game.this.endB.y != (float)Game.this.hoverTileY) {
                  Game.this.activeEnd = 0;
                  this.activeElementPos.set((float)(Game.this.hoverTileX * 64), (float)(Game.this.hoverTileY * 64));
                  this.setActiveElementPosition(this.activeElementPos);
                  this.updateActiveElementPlaceable();
               }
            } else {
               this.setGhostAtCursor(s);
            }

            return;
         }

         this.setGhostAtCursor(s);
      }

      private void setGhostAtCursor(Structure<?> s) {
         int x = (int)Math.ceil(
            (double)MathUtils.clamp(
               (float)Game.this.hoverTileX - (float)s.getSchema().width / 2.0F + 0.5F,
               0.0F,
               (float)(Game.this.layer.width - s.getWidth())
            )
         );
         int y = (int)Math.ceil(
            (double)MathUtils.clamp(
               (float)Game.this.hoverTileY - (float)s.getSchema().height / 2.0F + 0.5F,
               0.0F,
               (float)(Game.this.layer.height - s.getHeight())
            )
         );
         if (s.x != x || s.y != y) {
            s.x = x;
            s.y = y;
            s.onPlacement(false);
            this.smartChecksOnPlacement();
            this.updateActiveElementPlaceable();
         }
      }

      public void deleteStructure(Structure<?> s) {
         if (NetSession.isActive()) {
            // 联机：本地不改世界，改成命令；退料也在命令执行侧按同样规则算
            NetSession.issue(NetCommand.remove(s.layer != null ? s.layer.getIndex() : Game.this.layer.getIndex(), s.x, s.y, s instanceof CopperCable));
            return;
         }

         if (s instanceof CopperCable) {
            Game.this.layer.removeCable((CopperCable)s);
         } else {
            Game.this.layer.removeStructure(s);
         }

         int removedHoppers = Game.this.layer.removeAttachedHoppers(s);
         if (Quarry.Q.sound.isPlaySound()) {
            Game.this.destroySfx.play(Quarry.Q.sound.getSoundVolume(), (float)(Math.random() * 0.2 + 0.8), 0.0F);
         }

         Item.Items costs = s.getSchema().buildCosts;

         for (Item.Items.Amount e : costs.entries) {
            if (e.getAmount() > 1) {
               Game.this.addResources(
                  e.getItem(),
                  (int)Math.ceil((double)((float)e.getAmount() * (Game.this.hasScience(Science.ScienceType.ConsiderateConstruction) ? 0.8F : 0.25F))),
                  true
               );
            }
         }

         costs = Hopper.classSchema.buildCosts;

         for (Item.Items.Amount ex : costs.entries) {
            if (ex.getAmount() > 1) {
               Game.this.addResources(
                  ex.getItem(),
                  (int)Math.ceil(
                     (double)((float)(ex.getAmount() * removedHoppers) * (Game.this.hasScience(Science.ScienceType.ConsiderateConstruction) ? 0.8F : 0.25F))
                  ),
                  true
               );
            }
         }
      }

      @Override
      public boolean handleTap(int x, int y, int tileX, int tileY) {
         Game.this.activeEnd = 0;
         if (tileX >= 0 && tileX < Game.this.layer.width && tileY >= 0 && tileY < Game.this.layer.height) {
            System.out
               .println(
                  tileX
                     + ":"
                     + tileY
                     + " ("
                     + (Game.this.layer.getIndex() * Game.this.layer.width * Game.this.layer.height + tileX * Game.this.layer.height + tileY)
                     + ")"
               );
            if (Game.FOGMODE) {
               Game.this.layer.removeMeta(tileX, tileY, 1);
               Game.FOGMODE = false;
            }

            if (Game.this.cableDestroyMode) {
               CopperCable c = Game.this.layer.getCable(tileX, tileY);
               if (c != null) {
                  if (Game.this.activeStructure == c) {
                     this.deleteStructure(c);
                     Game.this.resetActiveStructure();
                  } else if (!Game.this.bulkCableMode && !Game.this.bulkDestroyMode) {
                     Game.this.activeStructure = c;
                  }
               }

               return true;
            } else if ((Game.this.bulkDestroyMode || Game.this.bulkCableMode) && Game.this.activeEnd == 0) {
               if ((this.isInEndMarkerHitbox(x, y, true) || this.isInEndMarkerHitbox(x, y, false)) && Game.this.endB.x > -1.0F) {
                  final Array<Structure<?>> toDelete = new Array();
                  int x1 = (int)Math.min(Game.this.endA.x, Game.this.endB.x);
                  int y1 = (int)Math.min(Game.this.endA.y, Game.this.endB.y);
                  int width = (int)Math.max(Game.this.endA.x, Game.this.endB.x) - x1 + 1;
                  int height = (int)Math.max(Game.this.endA.y, Game.this.endB.y) - y1 + 1;

                  for (Chunk c : Game.this.layer.getChunks()) {
                     if (Game.this.bulkDestroyMode) {
                        for (Structure<?> str : c.getStructures()) {
                           if (!str.getSchema().has(Schema.Flags.Indestructible)
                              && str.x < x1 + width
                              && str.x + str.getWidth() > x1
                              && str.y + str.getHeight() > y1
                              && str.y < y1 + height) {
                              toDelete.add(str);
                           }
                        }
                     } else {
                        for (CopperCable strx : c.getCables().values()) {
                           if (!strx.getSchema().has(Schema.Flags.Indestructible)
                              && strx.x < x1 + width
                              && strx.x + strx.getWidth() > x1
                              && strx.y + strx.getHeight() > y1
                              && strx.y < y1 + height) {
                              toDelete.add(strx);
                           }
                        }
                     }
                  }

                  if (!toDelete.isEmpty()) {
                     Game.this.ui.confirm.show(Game.this.ui, Quarry.Q.i18n.get("confirm.destroy_structure_bulk"), new Callback<Boolean>() {
                        public void call(Boolean e) {
                           if (e instanceof Boolean && e) {
                              for (Structure<?> s : toDelete) {
                                 QuarryCameraControl.this.deleteStructure(s);
                              }

                              Game.this.resetActiveStructure();
                           }
                        }
                     });
                  }
               } else if (Game.this.endA.x == -1.0F) {
                  Game.this.endA.set((float)tileX, (float)tileY);
               } else {
                  Game.this.endB.set((float)tileX, (float)tileY);
               }

               return true;
            } else if (Game.this.copyMode && Game.this.activeEnd == 0) {
               if ((this.isInEndMarkerHitbox(x, y, true) || this.isInEndMarkerHitbox(x, y, false))
                  && Game.this.endB.x > -1.0F
                  && Game.this.ui.canAffordStructure) {
                  Game.this.removeResources(Item.ItemType.Blueprint, Game.this.copyCost.get(Item.ItemType.Blueprint), true);
                  Game.this.copyStructures.clear();
                  Game.this.copyCables.clear();
                  Game.this.copyCost.clear();

                  for (Chunk cx : Game.this.layer.getChunks()) {
                     for (Structure<?> strxx : cx.getStructures()) {
                        if (!(strxx instanceof ShaftDrillHead)
                           && !(strxx instanceof ItemLift)
                           && !(strxx instanceof TubeShaft)
                           && !(strxx instanceof CableShaft)
                           && !(strxx instanceof HighPowerShaft)
                           && strxx.x < Game.this.copyRegion[0] + Game.this.copyRegion[2]
                           && strxx.x + strxx.getWidth() > Game.this.copyRegion[0]
                           && strxx.y + strxx.getHeight() > Game.this.copyRegion[1]
                           && strxx.y < Game.this.copyRegion[1] + Game.this.copyRegion[3]) {
                           Structure<?> clone = (Structure<?>)strxx.clone();
                           Game.this.copyStructures.put(clone.x * Game.this.layer.height + clone.y, new Pair<>(clone, strxx.copy(Game.this.copyRegion)));

                           for (Item.Items.Amount t : strxx.getSchema().buildCosts.entries) {
                              Game.this.copyCost
                                 .put(t.getItem(), t.getAmount() + (Game.this.copyCost.containsKey(t.getItem()) ? Game.this.copyCost.get(t.getItem()) : 0));
                           }
                        }
                     }

                     for (CopperCable strxxx : cx.getCables().values()) {
                        if (strxxx.x < Game.this.copyRegion[0] + Game.this.copyRegion[2]
                           && strxxx.x + strxxx.getWidth() > Game.this.copyRegion[0]
                           && strxxx.y + strxxx.getHeight() > Game.this.copyRegion[1]
                           && strxxx.y < Game.this.copyRegion[1] + Game.this.copyRegion[3]) {
                           Structure<?> clone = (Structure<?>)strxxx.clone();
                           Game.this.copyCables.put(clone.x * Game.this.layer.height + clone.y, new Pair<>(clone, strxxx.copy(Game.this.copyRegion)));

                           for (Item.Items.Amount t : strxxx.getSchema().buildCosts.entries) {
                              Game.this.copyCost
                                 .put(t.getItem(), t.getAmount() + (Game.this.copyCost.containsKey(t.getItem()) ? Game.this.copyCost.get(t.getItem()) : 0));
                           }
                        }
                     }
                  }

                  for (Pair<Structure<?>, NBT.CompoundTag> p : Game.this.copyStructures.values()) {
                     p.getKey().paste(Game.this.copyRegion, p.getVal());
                  }

                  for (Pair<Structure<?>, NBT.CompoundTag> p : Game.this.copyCables.values()) {
                     p.getKey().paste(Game.this.copyRegion, p.getVal());
                  }

                  Game.this.resetActiveStructure();
                  Game.this.pasteMode = true;
                  Game.this.copyMode = false;
                  Game.this.ui.updateCopyTableResources(true);
                  this.updateActiveElementPlaceable();
               } else if (Game.this.endA.x == -1.0F) {
                  Game.this.endA.set((float)tileX, (float)tileY);
               } else {
                  Game.this.endB.set((float)tileX, (float)tileY);
                  this.updateCopySelection();
               }

               return true;
            } else {
               Structure<?> s = Game.this.layer.getStructure(tileX, tileY);
               if (s != null) {
                  if (Game.FLUIDMODE && s instanceof BrickChannel) {
                     s.acceptFluid(Item.ItemType.MoltenCopper, 500, null);
                  } else if (Game.FLUIDMODE && s instanceof FluidTubeStructure) {
                     s.acceptFluid(Item.ItemType.Water, 1000, null);
                  }

                  if (Game.FILLMODE) {
                     if (s instanceof Storage) {
                        for (Item.ItemType t : Item.ItemType.values) {
                           if (!t.categories.contains(Item.ItemCategory.Fluid) && !t.categories.contains(Item.ItemCategory.Abstract)) {
                              ((Storage)s).putBack(t, 100);
                           }
                        }
                     } else if (s instanceof Substation) {
                        ((Substation)s).acceptPower(1.0E8, PowerNetwork.NetworkStrength.PowerPole.maxPowerPerSecond);
                     } else if (s instanceof Tank) {
                        ((Tank)s).acceptFluid(Item.ItemType.Water, 100000000, null);
                     }

                     Game.FILLMODE = false;
                  }

                  if (Game.this.structureDestroyMode) {
                     if (!s.getSchema().has(Schema.Flags.Indestructible)) {
                        if (Game.this.activeStructure == s) {
                           final Structure<?> finalS = s;
                           Callback<Boolean> callback = new Callback<Boolean>() {
                              public void call(Boolean data) {
                                 if (data != null && data) {
                                    QuarryCameraControl.this.deleteStructure(finalS);
                                 }

                                 Game.this.resetActiveStructure();
                              }
                           };
                           if (s.getSchema().has(Schema.Flags.ConfirmDestruction)) {
                              String text = Quarry.Q.i18n.get("confirm.destroy_structure." + s.getSchema().type.name().toLowerCase());
                              Game.this.ui
                                 .confirm
                                 .show(Game.G.ui, Quarry.Q.i18n.get("confirm.destroy_structure") + (text.startsWith("???") ? "" : " " + text), callback);
                           } else if (Game.this.layer.hasHoppersAttached(s)) {
                              Game.this.ui
                                 .confirm
                                 .show(
                                    Game.G.ui,
                                    Quarry.Q.i18n.get("confirm.destroy_structure") + " " + Quarry.Q.i18n.get("confirm.destroy_structure.hoppers"),
                                    callback
                                 );
                           } else {
                              callback.call(true);
                           }
                        } else {
                           Game.this.activeStructure = s;
                        }
                     }

                     return true;
                  } else {
                     if (Game.this.activeStructure == null
                        && Game.this.ui.tooltipCurrentStructure == null
                        && s.getSchema().clickable
                        && !Game.this.bulkDestroyMode
                        && !Game.this.bulkCableMode) {
                        Game.this.ui.showStructureUI(s);
                     }

                     return true;
                  }
               } else if (Game.this.structureDestroyMode && !Game.this.bulkDestroyMode && !Game.this.bulkCableMode) {
                  Game.this.resetActiveStructure();
                  return true;
               } else {
                  Game.this.ui.hideStructureUI();
                  if (Game.this.activeStructure == null
                     && Game.this.ui.currentClickedStructure == null
                     && !Game.this.structureDestroyMode
                     && !Game.this.cableDestroyMode
                     && !Game.this.bulkDestroyMode
                     && !Game.this.bulkCableMode) {
                     Tile.TileType tx = Game.this.layer.get(tileX, tileY);
                     if (tx != null
                        && tx != Tile.TileType.Air
                        && (Game.this.activeTile == null || Game.this.activeTile.x != (float)tileX || Game.this.activeTile.y != (float)tileY)) {
                        Game.this.ui.tileUI.show();
                        Game.this.ui.tileUI.setText((Game.this.layer.getMeta(tileX, tileY) & 1) == 0 ? tx : null);
                        Game.this.activeTile.set((float)tileX, (float)tileY);
                     } else {
                        Game.this.ui.tileUI.hide();
                        Game.this.resetActiveTile();
                     }

                     return true;
                  } else if (tileX >= 0 && tileX < Game.this.layer.width && tileY >= 0 && tileY < Game.this.layer.height) {
                     Game.this.ui.tileUI.hide();
                     Game.this.resetActiveTile();
                     return false;
                  } else {
                     Game.this.ui.hideStructureUI();
                     Game.this.ui.tileUI.hide();
                     Game.this.resetActiveTile();
                     return false;
                  }
               }
            }
         } else {
            return true;
         }
      }

      public QuarryCameraControl(Viewport viewport) {
         super(64, viewport);
      }

      public boolean isInEndMarkerHitbox(int x, int y, boolean isA) {
         Vector2 end = isA ? Game.this.endA : Game.this.endB;
         float ax = end.x * 64.0F;
         float ay = (end.y + 0.4F) * 64.0F;
         return (float)x >= ax && (float)x <= ax + 64.0F && (float)y >= ay & (float)y <= ay + 64.0F + 10.0F;
      }

      @Override
      protected boolean isWithinActiveElement(int x, int y, int tileX, int tileY) {
         if (Game.this.pasteMode) {
            return tileX >= Game.this.copyRegion[0]
               && tileX < Game.this.copyRegion[0] + Game.this.copyRegion[2]
               && tileY >= Game.this.copyRegion[1]
               && tileY < Game.this.copyRegion[1] + Game.this.copyRegion[3];
         } else if (!Game.this.bulkDestroyMode && !Game.this.bulkCableMode && !Game.this.copyMode) {
            return Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable) && Game.this.endB.x > -1.0F
               ? this.isInEndMarkerHitbox(x, y, true) || this.isInEndMarkerHitbox(x, y, false)
               : tileX >= Game.this.activeStructure.x
                  && tileX < Game.this.activeStructure.x + Game.this.activeStructure.getWidth()
                  && tileY >= Game.this.activeStructure.y
                  && tileY < Game.this.activeStructure.y + Game.this.activeStructure.getHeight();
         } else {
            return this.isInEndMarkerHitbox(x, y, true) || this.isInEndMarkerHitbox(x, y, false);
         }
      }

      protected void updateCopySelection() {
         int x1 = (int)Math.min(Game.this.endA.x, Game.this.endB.x);
         int y1 = (int)Math.min(Game.this.endA.y, Game.this.endB.y);
         int width = (int)Math.max(Game.this.endA.x, Game.this.endB.x) - x1 + 1;
         int height = (int)Math.max(Game.this.endA.y, Game.this.endB.y) - y1 + 1;
         Game.this.copyRegion[0] = x1;
         Game.this.copyRegion[1] = y1;
         Game.this.copyRegion[2] = width;
         Game.this.copyRegion[3] = height;
         Game.this.copyCost.put(Item.ItemType.Blueprint, width * height);
         Game.this.ui.updateCopyTableResources(false);
      }

      @Override
      protected void setParamRawPosition(Vector2 rawPosition, int x, int y, int tileX, int tileY) {
         if (Game.this.pasteMode) {
            rawPosition.set((float)(Game.this.copyRegion[0] * 64), (float)(Game.this.copyRegion[1] * 64));
         } else if ((
               Game.this.bulkDestroyMode || Game.this.bulkCableMode || Game.this.copyMode || Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable)
            )
            && Game.this.endB.x > -1.0F) {
            rawPosition.set((float)(tileX * 64), (float)(tileY * 64));
            if (this.isInEndMarkerHitbox(x, y, false)) {
               Game.this.activeEnd = 2;
            } else if (this.isInEndMarkerHitbox(x, y, true)) {
               Game.this.activeEnd = 1;
            }
         } else if (Game.this.activeStructure != null) {
            rawPosition.set((float)(Game.this.activeStructure.x * 64), (float)(Game.this.activeStructure.y * 64));
         }
      }

      @Override
      protected boolean isActiveElementEnabled() {
         if (Game.this.pasteMode) {
            return true;
         } else if (Game.this.bulkDestroyMode || Game.this.bulkCableMode || Game.this.copyMode) {
            return Game.this.endA.x > -1.0F;
         } else if (Game.this.activeStructure == null) {
            return false;
         } else {
            return Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable)
               ? Game.this.endA.x > -1.0F && !Game.this.structureDestroyMode
               : Game.this.activeStructure.x > -1 && !Game.this.structureDestroyMode;
         }
      }

      @Override
      protected void resetActiveElement() {
         Game.this.resetActiveStructure();
         this.canPan = true;
         this.dragX = -1.0F;
         this.dragY = -1.0F;
         this.activeElementPos.setZero();
      }

      @Override
      public boolean tap(float x, float y, int count, int button) {
         if (button == 1 && Game.this.activeStructure != null) {
            this.resetActiveElement();
            this.updateActiveElementPlaceable();
            return true;
         }

         return super.tap(x, y, count, button);
      }

      @Override
      public void clampCam(OrthographicCamera cam) {
         cam.position.x = MathUtils.clamp(cam.position.x, 0.0F, (float)(Game.this.layer.width * 64));
         cam.position.y = MathUtils.clamp(cam.position.y, 0.0F, (float)(Game.this.layer.height * 64));
      }

      @Override
      protected boolean clampZoom(float zoom) {
         this.maxZoom = Math.max(
            Math.max(Const.H, (float)(Game.this.layer.height * 64)) / Const.H, Math.max(Const.W, (float)(Game.this.layer.width * 64)) / Const.W
         );
         return super.clampZoom(zoom);
      }

      @Override
      public boolean scrolled(int amount) {
         if (Game.SMOOTH_CAMERA) {
            Game.this.cameraZoomAcc += (float)(amount * 2);
            return true;
         } else {
            return super.scrolled(amount);
         }
      }

      protected void setCopyPosition(int x, int y) {
         for (Pair<Structure<?>, NBT.CompoundTag> p : Game.this.copyStructures.values()) {
            Structure<?> s = p.getKey();
            s.x = s.x - Game.this.copyRegion[0] + x;
            s.y = s.y - Game.this.copyRegion[1] + y;
         }

         for (Pair<Structure<?>, NBT.CompoundTag> p : Game.this.copyCables.values()) {
            Structure<?> s = p.getKey();
            s.x = s.x - Game.this.copyRegion[0] + x;
            s.y = s.y - Game.this.copyRegion[1] + y;
         }

         Game.this.copyRegion[0] = x;
         Game.this.copyRegion[1] = y;
      }

      @Override
      protected void setActiveElementPosition(Vector2 rawPosition) {
         int x = (int)(rawPosition.x / 64.0F + 0.5F);
         int y = (int)(rawPosition.y / 64.0F + 0.5F);
         x = MathUtils.clamp(x, 0, Game.this.layer.width - 1);
         y = MathUtils.clamp(y, 0, Game.this.layer.height - 1);
         if (!Game.this.bulkDestroyMode && !Game.this.bulkCableMode && !Game.this.copyMode) {
            if (Game.this.pasteMode) {
               if (Game.this.copyRegion[0] != x || Game.this.copyRegion[1] != y) {
                  this.setCopyPosition(x, y);
               }
            } else if (Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable)) {
               if (Game.this.activeEnd == 1) {
                  Game.this.endA.set((float)x, (float)y);
                  Game.this.activeStructure.x = x;
                  Game.this.activeStructure.y = y;
                  Game.this.activeStructure.onPlacement(false);
                  if (Game.this.endB.equals(Game.this.endA) && Game.this.endA.x > -1.0F) {
                     Game.this.endB.x = -1.0F;
                     Game.this.endB.y = -1.0F;
                     Game.this.activeStructureTrail.clear();
                     Game.this.activeStructure.update(0.0, 1, Game.G.layer.dirtyBounds);
                  }
               } else {
                  Game.this.endB.set((float)x, (float)y);
                  if (Game.this.endB.equals(Game.this.endA) && Game.this.endA.x > -1.0F) {
                     Game.this.endB.x = -1.0F;
                     Game.this.endB.y = -1.0F;
                     Game.this.activeStructureTrail.clear();
                     Game.this.activeStructure.update(0.0, 1, Game.G.layer.dirtyBounds);
                  }
               }

               this.updateTrail();
               Game.this.ui.updateTooltipResources();
            } else if (Game.this.activeStructure.x != x || Game.this.activeStructure.y != y) {
               Game.this.activeStructure.x = x;
               Game.this.activeStructure.y = y;
               Game.this.activeStructure.onPlacement(false);
               this.smartChecksOnPlacement();
            }
         } else {
            if (Game.this.activeEnd == 1) {
               Game.this.endA.set((float)x, (float)y);
            } else {
               Game.this.endB.set((float)x, (float)y);
            }

            if (Game.this.copyMode && Game.this.endB.x > -1.0F) {
               this.updateCopySelection();
            }
         }
      }

      public void updateTrail() {
         if (Game.this.endA.x != -1.0F) {
            Game.this.activeStructure.update(0.0, 1, Game.G.layer.dirtyBounds);
            if (!this.placingTrail && Game.this.endB.x != -1.0F) {
               Game.this.activeStructureTrail.clear();
               Game.this.activeStructurePath.clear();
               if (Game.this.activeStructure instanceof CopperCable) {
                  Game.this.activeStructurePath
                     .addAll(
                        Game.this.tilePathfinding
                           .findPath(
                              Game.this.cableNetwork,
                              (int)(Game.this.endB.x * (float)Game.this.layer.height + Game.this.endB.y),
                              (int)(Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y)
                           )
                     );
               } else {
                  Game.this.activeStructurePath
                     .addAll(
                        Game.this.tilePathfinding
                           .findPath(
                              Game.this.tileNetwork,
                              (int)(Game.this.endB.x * (float)Game.this.layer.height + Game.this.endB.y),
                              (int)(Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y)
                           )
                     );
               }

               Structure<?> last = null;
               if (Game.this.activeStructurePath.size() > 0) {
                  for (int i : Game.this.activeStructurePath) {
                     int x = i / Game.this.layer.height;
                     int y = i % Game.this.layer.height;
                     Structure<?> s = (Structure<?>)Game.this.activeStructure.clone();
                     if (s instanceof IRotatable) {
                        Direction rotation = ((IRotatable)Game.this.activeStructure).getDirection();
                        if (last != null) {
                           if (x == last.x) {
                              rotation = y > last.y ? Direction.South : Direction.North;
                           } else {
                              rotation = x > last.x ? Direction.West : Direction.East;
                           }
                        }

                        ((IRotatable)s).setRotation(rotation);
                        if (last != null && (float)last.x == Game.this.endB.x && (float)last.y == Game.this.endB.y) {
                           ((IRotatable)last).setRotation(rotation);
                        }

                        last = s;
                     }

                     s.x = x;
                     s.y = y;
                     Game.this.activeStructureTrail.put(i, s);
                  }
               }

               if (Game.this.activeStructure instanceof Conveyor
                  && Game.this.activeStructureTrail.containsKey((int)(Game.this.endB.x * (float)Game.this.layer.height + Game.this.endB.y))
                  && Game.this.endB.x * (float)Game.this.layer.height + Game.this.endB.y != Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y
                  )
                {
                  Structure<?> target = null;
                  Direction targetDirection = null;
                  Structure<?> s = (Structure<?>)Game.this.activeStructureTrail.get((int)Game.this.endB.x * Game.this.layer.height + (int)Game.this.endB.y);

                  label111:
                  for (Direction d : Direction.values) {
                     Structure<?> q = Game.this.layer.getStructure((int)Game.this.endB.x + d.dx, (int)Game.this.endB.y + d.dy);
                     if (q != null && !Game.this.activeStructureTrail.containsKey(q.getIndex())) {
                        if (q instanceof Conveyor) {
                           if (target != null) {
                              target = null;
                              break;
                           }

                           if (q instanceof ElectricConveyorCore && ((Conveyor)q).getDirection() != d && ((Conveyor)q).getDirection() != d.inv()) {
                              target = null;
                              break;
                           }

                           target = q;
                           targetDirection = d;
                        } else if (q.getDocks().length > 0) {
                           for (Dock dock : q.getDocks()) {
                              if (q.isNextToDock((int)Game.this.endB.x, (int)Game.this.endB.y, d, dock)) {
                                 if (target != null) {
                                    target = null;
                                    break label111;
                                 }

                                 target = q;
                                 targetDirection = d;
                                 break;
                              }
                           }
                        }
                     }
                  }

                  if (target != null) {
                     ((IRotatable)s).setRotation(targetDirection);
                  }
               }

               for (Structure<?> s : Game.this.activeStructureTrail.values()) {
                  s.onPlacement(false);
               }
            }
         }
      }

      protected void smartChecksOnPlacement() {
         if (Game.this.activeStructure instanceof Hopper && !Game.this.pasteMode) {
            Structure<?> target = null;
            Direction targetDirection = null;

            label47:
            for (Direction d : Direction.values) {
               Structure<?> q = Game.this.layer.getStructure(Game.this.activeStructure.x + d.dx, Game.this.activeStructure.y + d.dy);
               if (q != null) {
                  if (q instanceof Conveyor) {
                     if (target != null) {
                        target = null;
                        break;
                     }

                     target = q;
                     targetDirection = d;
                  } else if (q.getDocks().length > 0) {
                     for (Dock dock : q.getDocks()) {
                        if (q.isNextToDock(Game.this.activeStructure.x, Game.this.activeStructure.y, d, dock)) {
                           if (target != null) {
                              target = null;
                              break label47;
                           }

                           target = q;
                           targetDirection = d;
                           break;
                        }
                     }
                  }
               }
            }

            if (target != null) {
               ((IRotatable)Game.this.activeStructure).setRotation(targetDirection);
            }
         }
      }

      protected boolean isStructurePlaceable(Structure<?> structure) {
         synchronized (Game.highlightLock) {
            if (Game.this.tutorialHighlight.size > 0) {
               for (int i = 0; i < structure.getWidth(); i++) {
                  for (int j = 0; j < structure.getHeight(); j++) {
                     if (!Game.this.tutorialHighlight.contains((i + structure.x) * Game.this.layer.height + j + structure.y)) {
                        return false;
                     }
                  }
               }

               if (structure instanceof Mine && structure.getUpDirection() != Direction.North) {
                  return false;
               }
            }
         }

         boolean fog = !Game.this.layer.isNotInFogOfWar(structure, structure instanceof AirPurifier);
         if (structure instanceof CopperCable) {
            return Game.this.layer.getCable(structure.x, structure.y) == null && !Game.this.layer.isPowerDockCollision(structure.x, structure.y) ? !fog : false;
         } else if (!(structure instanceof PowerPole)
            || Game.this.layer.getCable(structure.x, structure.y) == null && !Game.this.layer.isPowerDockCollision(structure.x, structure.y)) {
            if ((structure instanceof SolarPanel || structure instanceof SolarPanelOutlet) && Game.this.layerIndex != 0) {
               return false;
            } else {
               if (!(structure instanceof ItemLiftBelow)
                  && !(structure instanceof CableShaftBelow)
                  && !(structure instanceof TubeShaftBelow)
                  && !(structure instanceof HighPowerShaftBelow)) {
                  if ((
                        structure instanceof ItemLift
                           || structure instanceof CableShaft
                           || structure instanceof TubeShaft
                           || structure instanceof HighPowerShaft
                     )
                     && (
                        Game.this.layers.size <= Game.this.layerIndex + 1
                           || Game.this.getLayer(Game.this.layerIndex + 1).isColliding(structure, true, true)
                           || !Game.this.getLayer(Game.this.layerIndex + 1).isNotInFogOfWar(structure, false)
                     )) {
                     return false;
                  }
               } else if (Game.this.layerIndex == 0
                  || Game.this.getLayer(Game.this.layerIndex - 1).isColliding(structure, true, true)
                  || !Game.this.getLayer(Game.this.layerIndex - 1).isNotInFogOfWar(structure, false)) {
                  return false;
               }

               if (structure instanceof OilWell) {
                  int count = 0;

                  label263:
                  for (int i = 0; i < structure.getWidth(); i++) {
                     for (int jx = 0; jx < structure.getHeight(); jx++) {
                        if (Game.this.layer.get(structure.x + i, structure.y + jx) == Tile.TileType.CrudeOil) {
                           if (++count >= 4) {
                              break label263;
                           }
                        }
                     }
                  }

                  if (count < 4) {
                     return false;
                  }
               }

               if (structure instanceof Excavator) {
                  EnumMap<Tile.TileType, Integer> counts = new EnumMap<>(Tile.TileType.class);

                  for (int i = 0; i < structure.getWidth(); i++) {
                     for (int jxx = 0; jxx < structure.getHeight(); jxx++) {
                        Tile.TileType tile = Game.this.layer.get(structure.x + i, structure.y + jxx);
                        if (tile == Tile.TileType.CopperOre || tile == Tile.TileType.IronOre || tile == Tile.TileType.TinOre || tile == Tile.TileType.CoalOre) {
                           counts.put(tile, (counts.containsKey(tile) ? counts.get(tile) : 0) + 1);
                        }
                     }
                  }

                  boolean any = false;

                  for (Entry<Tile.TileType, Integer> e : counts.entrySet()) {
                     if (e.getValue() >= 7) {
                        if (any) {
                           return false;
                        }

                        any = true;
                     }
                  }

                  if (!any) {
                     return false;
                  }
               }

               if (structure instanceof Hopper) {
                  Hopper h = (Hopper)structure;
                  if (!h.isValidRotation(h.getDirection())) {
                     return false;
                  }
               }

               if (structure instanceof Mine && ((Mine)structure).getMineableItems() == 0) {
                  return false;
               } else {
                  boolean colliding = Game.this.layer.isColliding(structure, !(structure instanceof Conveyor), !(structure instanceof FluidTubeStructure));
                  if (structure instanceof ConveyorBridge && !fog && !Game.this.pasteMode) {
                     Structure<?> x = Game.this.layer.getStructure(structure.x, structure.y);
                     if (x != null && x.getSchema().type == StructureType.Conveyor) {
                        ((ConveyorBridge)structure).setRotation(((Conveyor)x).getDirection());
                        return true;
                     }
                  }

                  if (!fog && structure.getSchema().has(Schema.Flags.Stackable)) {
                     Game.this.liningUpInput = Game.LiningUpState.NoOther;
                     Game.this.liningUpOutput = Game.LiningUpState.NoOther;
                     Game.this.liningUpStructureInput = null;
                     Game.this.liningUpStructureOutput = null;
                     this.getStateOfLiningUpWithOtherStackable(structure, Dock.DockType.StackIn);
                     this.getStateOfLiningUpWithOtherStackable(structure, Dock.DockType.StackOut);
                     if (structure instanceof DistillationColumn) {
                        ((DistillationColumn)structure).setLevel(0);
                     }

                     if (Game.this.liningUpInput == Game.LiningUpState.NotLiningUp || Game.this.liningUpOutput == Game.LiningUpState.NotLiningUp) {
                        return false;
                     }

                     if (Game.this.liningUpInput == Game.LiningUpState.LiningUp || Game.this.liningUpOutput == Game.LiningUpState.LiningUp) {
                        boolean collisionFree = this.isCollisionFreeStacking(structure);
                        if (!collisionFree) {
                           return false;
                        }

                        if (structure instanceof DistillationColumn) {
                           if (Game.this.liningUpInput == Game.LiningUpState.LiningUp
                              && Game.this.liningUpOutput == Game.LiningUpState.LiningUp
                              && (
                                 ((DistillationColumn)Game.this.liningUpStructureInput).getLevels() > 1
                                    || ((DistillationColumn)Game.this.liningUpStructureOutput).getLevels() > 1
                              )) {
                              Game.this.liningUpInput = Game.LiningUpState.NoOther;
                              Game.this.liningUpOutput = Game.LiningUpState.NoOther;
                              Game.this.liningUpStructureInput = null;
                              Game.this.liningUpStructureOutput = null;
                              return false;
                           }

                           if (Game.this.liningUpInput == Game.LiningUpState.LiningUp) {
                              if (((DistillationColumn)Game.this.liningUpStructureInput).getLevel() == 2) {
                                 Game.this.liningUpInput = Game.LiningUpState.NoOther;
                                 Game.this.liningUpStructureInput = null;
                                 return false;
                              }

                              ((DistillationColumn)structure).setLevel(((DistillationColumn)Game.this.liningUpStructureInput).getLevel() + 1);
                           } else if (Game.this.liningUpOutput == Game.LiningUpState.LiningUp) {
                              if (((DistillationColumn)Game.this.liningUpStructureOutput).getLevels() == 3) {
                                 Game.this.liningUpOutput = Game.LiningUpState.NoOther;
                                 Game.this.liningUpStructureOutput = null;
                                 return false;
                              }

                              ((DistillationColumn)structure).setLevel(0);
                           } else {
                              ((DistillationColumn)structure).setLevel(0);
                           }
                        }

                        return true;
                     }
                  }

                  return !fog && !colliding;
               }
            }
         } else {
            return false;
         }
      }

      @Override
      protected boolean checkActiveElementPlaceable() {
         if (!Game.this.ui.canAffordStructure && !Game.GOD_MODE) {
            return false;
         } else if (Game.this.pasteMode) {
            Game.this.copyCollisions.clear();

            for (Pair<Structure<?>, NBT.CompoundTag> s : Game.this.copyStructures.values()) {
               if (!this.isStructurePlaceable(s.getKey())) {
                  Game.this.copyCollisions.add(s.getKey());
               }
            }

            for (Pair<Structure<?>, NBT.CompoundTag> sx : Game.this.copyCables.values()) {
               if (!this.isStructurePlaceable(sx.getKey())) {
                  Game.this.copyCollisions.add(sx.getKey());
               }
            }

            return Game.this.copyCollisions.isEmpty();
         } else if (Game.this.activeStructure != null
            && (!Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable) || Game.this.endA.x != -1.0F || Game.this.endB.x != -1.0F)) {
            if (!(Game.this.endB.x > -1.0F)) {
               return this.isStructurePlaceable(Game.this.activeStructure);
            } else if (Game.this.activeStructurePath.isEmpty()) {
               return false;
            } else {
               synchronized (Game.highlightLock) {
                  if (Game.this.tutorialHighlight.size > 0) {
                     for (int i : Game.this.activeStructurePath) {
                        if (!Game.this.tutorialHighlight.contains(i)) {
                           return false;
                        }
                     }
                  }
               }

               Keys iter = Game.this.activeStructureTrail.keys();

               while (iter.hasNext) {
                  if (!Game.this.trailStructureCanBePlaced(
                     iter.next(),
                     (int)(Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y),
                     (int)(Game.this.endB.x * (float)Game.this.layer.height + Game.this.endB.y)
                  )) {
                     return false;
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      }

      protected boolean isCollisionFreeStacking(Structure<?> structure) {
         for (int i = 0; i < structure.getWidth(); i++) {
            int j = 0;

            while (j < structure.getHeight()) {
               if (structure.x + i >= 0 && structure.x + i < Game.this.layer.width) {
                  if (structure.y + j >= 0 && structure.y + j < Game.this.layer.height) {
                     if (Game.this.layer.isCollision(structure.x + i, structure.y + j, false, false)) {
                        return false;
                     }

                     if (Game.this.layer.isCollision(structure.x + i, structure.y + j, true, true)) {
                        for (Dock d : structure.getDocks()) {
                           if ((d.type == Dock.DockType.StackIn || d.type == Dock.DockType.StackOut) && d.x - d.dir.dx == i && d.y - d.dir.dy == j) {
                              for (Direction dir : Direction.values) {
                                 Structure<?> s = Game.this.layer.getStructure(structure.x + i + dir.dx, structure.y + j + dir.dy);
                                 if (s != null && s != Game.this.liningUpStructureInput && s != Game.this.liningUpStructureOutput) {
                                    return false;
                                 }
                              }

                              return true;
                           }
                        }

                        return false;
                     }

                     j++;
                     continue;
                  }

                  return false;
               }

               return false;
            }
         }

         for (Dock dx : structure.getDocks()) {
            int sx = structure.x + dx.x + dx.dir.dx;
            int sy = structure.y + dx.y + dx.dir.dy;
            if (dx.type != Dock.DockType.StackIn && dx.type != Dock.DockType.StackOut) {
               if (sx < 0 || sy < 0 || sx >= Game.this.layer.width || sy >= Game.this.layer.height) {
                  return false;
               }

               if (dx.type != Dock.DockType.Power && dx.type != Dock.DockType.BigPower) {
                  if ((Game.this.layer.getFlags(sx, sy) & 1) != 0) {
                     Structure<?> s = Game.this.layer.getStructure(sx, sy);
                     return s == Game.this.liningUpStructureInput || s == Game.this.liningUpStructureOutput;
                  }
               } else if ((Game.this.layer.getFlags(sx, sy) & 16) != 0) {
                  return false;
               }
            }
         }

         return true;
      }

      protected void getStateOfLiningUpWithOtherStackable(Structure<?> structure, Dock.DockType myType) {
         Dock.DockType theirType = myType == Dock.DockType.StackIn ? Dock.DockType.StackOut : Dock.DockType.StackIn;
         Structure<?> other = null;
         boolean allDocks = true;

         label83:
         for (Dock d : structure.getDocks()) {
            if (d.type == myType) {
               Structure<?> s = Game.this.layer.getStructure(structure.x + d.x, structure.y + d.y);
               if (s != null && s.getSchema().type == structure.getSchema().type && (other == null || s == other)) {
                  if (s.getUpDirection() != structure.getUpDirection()) {
                     other = s;
                     allDocks = false;
                     break;
                  }

                  for (Dock d1 : s.getDocks()) {
                     if (d1.type == theirType
                        && d1.dir == d.dir.inv()
                        && s.x + d1.x - d1.dir.dx == structure.x + d.x
                        && s.y + d1.y - d1.dir.dy == structure.y + d.y) {
                        other = s;
                        continue label83;
                     }
                  }

                  allDocks = false;
                  break;
               } else {
                  allDocks = false;
               }
            }
         }

         if (myType == Dock.DockType.StackIn) {
            if (other == null) {
               Game.this.liningUpInput = Game.LiningUpState.NoOther;
            } else {
               Game.this.liningUpInput = allDocks ? Game.LiningUpState.LiningUp : Game.LiningUpState.NotLiningUp;
            }

            Game.this.liningUpStructureInput = other;
         } else {
            if (other == null) {
               Game.this.liningUpOutput = Game.LiningUpState.NoOther;
            } else {
               Game.this.liningUpOutput = allDocks ? Game.LiningUpState.LiningUp : Game.LiningUpState.NotLiningUp;
            }

            Game.this.liningUpStructureOutput = other;
         }
      }

      @Override
      protected void placeActiveElement() {
         if (Game.this.endB.x > -1.0F) {
            this.placingTrail = true;

            for (Structure<?> s : Game.this.activeStructureTrail.values()) {
               this.placeStructure(Game.this.layer, s);
            }

            if (Game.this.activeStructure instanceof Conveyor
               && !NetSession.isActive()
               && !Game.this.activeStructureTrail.containsKey((int)(Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y))
               && Game.this.activeStructurePath.size() > 0) {
               Structure<?> s = Game.this.layer.getStructure((int)Game.this.endA.x, (int)Game.this.endA.y);
               if (s != null && s instanceof Conveyor) {
                  int first = Game.this.activeStructurePath.get(Game.this.activeStructurePath.size() - 1);
                  int dx = first / Game.this.layer.height - (int)Game.this.endA.x;
                  int dy = first % Game.this.layer.height - (int)Game.this.endA.y;
                  if (dx == -1) {
                     ((IRotatable)s).setRotation(Direction.West);
                  } else if (dx == 1) {
                     ((IRotatable)s).setRotation(Direction.East);
                  } else if (dy == -1) {
                     ((IRotatable)s).setRotation(Direction.South);
                  } else if (dy == 1) {
                     ((IRotatable)s).setRotation(Direction.North);
                  }
               }
            }

            if (Game.this.activeStructure instanceof Conveyor
               && !NetSession.isActive()
               && Game.this.activeStructureTrail.containsKey((int)(Game.this.endA.x * (float)Game.this.layer.height + Game.this.endA.y))) {
               for (Direction d : Direction.values) {
                  if (!Game.this.activeStructureTrail
                     .containsKey((int)((Game.this.endA.x + (float)d.dx) * (float)Game.this.layer.height + Game.this.endA.y + (float)d.dy))) {
                     Structure<?> q = Game.this.layer.getStructure((int)Game.this.endA.x + d.dx, (int)Game.this.endA.y + d.dy);
                     if (q != null
                        && q.getSchema().type == StructureType.Conveyor
                        && ((Conveyor)q).getStructureInDirection(((Conveyor)q).getDirection()) == null) {
                        ((Conveyor)q).setRotation(d.inv());
                        break;
                     }
                  }
               }
            }

            Game.this.activeStructureTrail.clear();
            Game.this.endA.set(-1.0F, 0.0F);
            Game.this.endB.set(-1.0F, 0.0F);
            Game.this.activeStructure.x = -1;
            Game.this.activeStructure.y = 0;
            Game.this.ui.updateTooltipResources();
            this.placingTrail = false;
         } else if (Game.this.pasteMode) {
            if (NetSession.isActive()) {
               // 蓝图粘贴带额外 NBT（Components/pasteData），M1d 暂不支持，联机时禁用入口
               Game.this.ui.toast.show("联机时暂不支持蓝图粘贴");
            } else {
            Array<Pair<Structure<?>, NBT.CompoundTag>> pairs = new Array();

            for (Pair<Structure<?>, NBT.CompoundTag> p : Game.this.copyStructures.values()) {
               Structure<?> s = p.getKey();
               Structure<?> q = (Structure<?>)s.clone();
               this.placeStructure(Game.this.layer, q);
               pairs.add(new Pair<>(q, p.getVal()));
            }

            for (Pair<Structure<?>, NBT.CompoundTag> s : Game.this.copyCables.values()) {
               Structure<?> q = (Structure<?>)s.getKey().clone();
               this.placeStructure(Game.this.layer, q);
               pairs.add(new Pair<>(q, s.getVal()));
            }

            for (Pair<Structure<?>, NBT.CompoundTag> s : pairs) {
               s.getKey().paste(Game.this.copyRegion, s.getVal());
            }
            }
         } else {
            if (Game.this.activeStructure instanceof Conveyor && !NetSession.isActive()) {
               for (Direction dx : Direction.values) {
                  Structure<?> q = Game.this.layer.getStructure(Game.this.activeStructure.x + dx.dx, Game.this.activeStructure.y + dx.dy);
                  if (q != null && q.getSchema().type == StructureType.Conveyor && ((Conveyor)q).getStructureInDirection(((Conveyor)q).getDirection()) == null) {
                     ((Conveyor)q).setRotation(dx.inv());
                     break;
                  }
               }
            }

            if (Game.this.activeStructure instanceof ConveyorBridge && !NetSession.isActive()) {
               Structure<?> x = Game.this.layer.getStructure(Game.this.activeStructure.x, Game.this.activeStructure.y);
               if (x != null && x.getSchema().type == StructureType.Conveyor) {
                  Game.this.layer.removeStructure(x);
               }
            }

            this.placeStructure(Game.this.layer, (Structure<?>)Game.this.activeStructure.clone());
            if (Game.this.endA.x > -1.0F) {
               Game.this.endA.set(-1.0F, 0.0F);
               Game.this.endB.set(-1.0F, 0.0F);
               Game.this.activeStructure.x = -1;
               Game.this.activeStructure.y = 0;
            }
         }
      }

      protected void placeStructure(Layer currentLayer, Structure<?> structure) {
         if (NetSession.isActive()) {
            // 联机：本地不改世界，改成命令，等 tick 边界两端一起执行
            NetCommand cmd = NetCommand.place(
               currentLayer.getIndex(),
               structure.x,
               structure.y,
               structure.getSchema().type.id,
               structure instanceof IRotatable ? ((IRotatable)structure).getDirection().ordinal() : structure.getUpDirection().ordinal(),
               GOD_MODE
            );
            System.out.println(
               "[Net] issue " + cmd + " (tick " + Game.this.simTick + " → " + (Game.this.simTick + NetSession.INPUT_DELAY) + ")"
            );
            NetSession.issue(cmd);
            return;
         }

         if (currentLayer.addStructure(structure)) {
            if (!Game.GOD_MODE) {
               Item.Items costs = structure.getSchema().buildCosts;

               for (Item.Items.Amount e : costs.entries) {
                  Game.this.removeResources(e.getItem(), e.getAmount(), true);
               }
            }

            synchronized (Game.highlightLock) {
               if (Game.this.tutorialHighlight.size > 0) {
                  for (int i = 0; i < structure.getWidth(); i++) {
                     for (int j = 0; j < structure.getHeight(); j++) {
                        Game.this.tutorialHighlight.remove((i + structure.x) * currentLayer.height + j + structure.y);
                     }
                  }
               }
            }

            if (!structure.getSchema().has(Schema.Flags.NoDustEffect)) {
               for (int i = 0; i < structure.getWidth(); i++) {
                  PooledEffect effect = (PooledEffect)Game.this.dustPfxD.obtain();
                  effect.reset();
                  effect.setPosition((float)(64 * (structure.x + i)), (float)(64 * structure.y));
                  effect.setDuration(10);
                  Game.this.layer.addParticleEffect(effect, true);
                  effect = (PooledEffect)Game.this.dustPfxU.obtain();
                  effect.reset();
                  effect.setPosition((float)(64 * (structure.x + i)), (float)(64 * (structure.y + structure.getHeight())));
                  effect.setDuration(10);
                  currentLayer.addParticleEffect(effect, true);
               }

               for (int i = 0; i < structure.getHeight(); i++) {
                  PooledEffect effect = (PooledEffect)Game.this.dustPfxL.obtain();
                  effect.reset();
                  effect.setPosition((float)(64 * structure.x), (float)(64 * (structure.y + i)));
                  effect.setDuration(10);
                  Game.this.layer.addParticleEffect(effect, true);
                  effect = (PooledEffect)Game.this.dustPfxR.obtain();
                  effect.reset();
                  effect.setPosition((float)(64 * (structure.x + structure.getWidth())), (float)(64 * (structure.y + i)));
                  effect.setDuration(10);
                  currentLayer.addParticleEffect(effect, true);
               }
            }

            if (Quarry.Q.sound.isPlaySound()) {
               Game.this.buildSfx.play(Quarry.Q.sound.getSoundVolume(), (float)(Math.random() * 0.4 + 0.6), 0.0F);
            }
         }
      }

      @Override
      protected boolean handleInitialPlacement(int tileX, int tileY) {
         if (Game.this.pasteMode) {
            int x = (int)Math.ceil(
               (double)MathUtils.clamp(
                  (float)tileX - (float)Game.this.copyRegion[2] / 2.0F + 0.5F, 0.0F, (float)(Game.this.layer.width - Game.this.copyRegion[2])
               )
            );
            int y = (int)Math.ceil(
               (double)MathUtils.clamp(
                  (float)tileY - (float)Game.this.copyRegion[3] / 2.0F + 0.5F, 0.0F, (float)(Game.this.layer.height - Game.this.copyRegion[3])
               )
            );
            if (Game.this.copyRegion[0] != x || Game.this.copyRegion[1] != y) {
               this.setCopyPosition(x, y);
               return true;
            }
         }

         if (Game.this.activeStructure != null && !Game.this.structureDestroyMode && !Game.this.cableDestroyMode) {
            int x = (int)Math.ceil(
               (double)MathUtils.clamp(
                  (float)tileX - (float)Game.this.activeStructure.getSchema().width / 2.0F + 0.5F,
                  0.0F,
                  (float)(Game.this.layer.width - Game.this.activeStructure.getWidth())
               )
            );
            int y = (int)Math.ceil(
               (double)MathUtils.clamp(
                  (float)tileY - (float)Game.this.activeStructure.getSchema().height / 2.0F + 0.5F,
                  0.0F,
                  (float)(Game.this.layer.height - Game.this.activeStructure.getHeight())
               )
            );
            if (Game.this.activeStructure.getSchema().has(Schema.Flags.Draggable)) {
               if (Game.this.endA.x == -1.0F) {
                  Game.this.endA.set((float)x, (float)y);
                  Game.this.activeStructure.x = x;
                  Game.this.activeStructure.y = y;
                  Game.this.activeStructure.onPlacement(false);
               } else if ((float)x != Game.this.endA.x || (float)y != Game.this.endA.y) {
                  Game.this.endB.set((float)x, (float)y);
               }

               if (Game.this.endA.x > -1.0F && Game.this.endB.x > -1.0F) {
                  this.updateTrail();
               }
            } else if (Game.this.activeStructure.x != x || Game.this.activeStructure.y != y) {
               Game.this.activeStructure.x = x;
               Game.this.activeStructure.y = y;
               Game.this.activeStructure.onPlacement(false);
               Game.this.activeStructureTrail.clear();
               this.smartChecksOnPlacement();
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
