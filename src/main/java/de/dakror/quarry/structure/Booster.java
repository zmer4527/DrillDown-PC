package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.Pair;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

public class Booster extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.Booster,
         true,
         3,
         3,
         "booster",
         new Item.Items(
            Item.ItemType.AdvancedMachineFrame, 1, Item.ItemType.BronzePlate, 150, Item.ItemType.Dynamo, 5, Item.ItemType.Glass, 80, Item.ItemType.Battery, 20
         ),
         new Sfx("booster" + Const.SFX_FORMAT),
         new Dock(0, 1, Direction.West, Dock.DockType.ItemIn),
         new Dock(1, 0, Direction.South, Dock.DockType.ItemIn),
         new Dock(2, 1, Direction.East, Dock.DockType.ItemIn),
         new Dock(1, 2, Direction.North, Dock.DockType.ItemIn)
      )
      .<Schema>sciences(Science.ScienceType.Boosting)
      .<Schema>button(new Schema.ButtonDef("icon_network", "button.network", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((Booster)data).meshMode = on;
            ((Booster)data).updateMesh();
            if (((Booster)data).clicked) {
               ((Booster)data).updateUI();
            }
         }
      }))
      .<Schema>button(new Schema.ButtonDef("symb_ff", "button.boost", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((Booster)data).setBoosting(on);
         }
      }))
      .flags(Schema.Flags.NoDustEffect, Schema.Flags.NotRotatable);
   public static final float BOOST_RADIUS = 9.5F;
   public static final int BOOST_FACTOR = 3;
   public static final int CONSUME_PER_SECOND = 1000;
   boolean meshMode;
   boolean boosting;
   boolean boostActive;
   double boostTime;
   Table ui;
   Container<Table> container;
   HashSet<Booster> nearby;
   HashSet<Booster> mesh;
   HashSet<Pair<Booster, Booster>> meshEdges;
   Rectangle myBoostArea;
   final Set<Rectangle> rectangles = new TreeSet<>(new Comparator<Rectangle>() {
      public int compare(Rectangle a, Rectangle b) {
         return Float.compare(a.x, b.x) == 0 ? Float.compare(a.y, b.y) : Float.compare(a.x, b.x);
      }
   });
   final FloatArray boostAreaTmp = new FloatArray();
   FloatArray boostArea;
   ShortArray triangleIndices;
   Booster meshHost;

   public Booster(int x, int y) {
      super(x, y, classSchema);
      this.nearby = new HashSet<>();
      this.mesh = new HashSet<>();
      this.meshEdges = new HashSet<>();
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (item.categories.contains(Item.ItemCategory.Fluid)) {
         return false;
      } else {
         for (Dock d : this.getDocks()) {
            if (d.type == Dock.DockType.ItemIn && this.isNextToDock(x, y, dir, d)) {
               return true;
            }
         }

         return false;
      }
   }

   @Override
   public boolean acceptItem(Item.ItemType item, Structure<?> source, Direction dir) {
      this.boostTime = this.boostTime + (double)item.worth;
      this.updateUI();
      return true;
   }

   @Override
   public boolean getButtonState(int buttonIndex) {
      if (buttonIndex == 0) {
         return this.meshMode;
      } else {
         return buttonIndex == 1 ? this.isBoosting() : false;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (Game.G.activeStructure == this || this.clicked) {
         shaper.set(ShapeType.Filled);
         if (this.meshMode) {
            for (Rectangle r : this.meshHost.rectangles) {
               shaper.setColor(1.0F, 0.498F, 0.314F, 0.3F);
               shaper.rect(r.x * 64.0F, r.y * 64.0F, 1216.0F, 1216.0F);
            }
         }

         shaper.setColor(1.0F, 0.498F, 0.314F, 0.2F);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() / 2.0F - 9.5F) * 64.0F, ((float)this.y + (float)this.getHeight() / 2.0F - 9.5F) * 64.0F, 1216.0F, 1216.0F
         );
      }

      shaper.set(ShapeType.Filled);
      if ((Game.G.activeStructure == this || this.clicked) && this.meshMode) {
         shaper.setColor(1.0F, 0.5F, 0.0F, 1.0F);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F - 8.0F, ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F - 8.0F, 16.0F, 16.0F
         );

         for (Pair<Booster, Booster> p : this.meshEdges) {
            shaper.setColor(1.0F, 0.5F, 0.0F, 1.0F);
            shaper.rectLine(
               ((float)p.getKey().x + (float)p.getKey().getWidth() / 2.0F) * 64.0F,
               ((float)p.getKey().y + (float)p.getKey().getHeight() / 2.0F) * 64.0F,
               ((float)p.getVal().x + (float)p.getVal().getWidth() / 2.0F) * 64.0F,
               ((float)p.getVal().y + (float)p.getVal().getHeight() / 2.0F) * 64.0F,
               8.0F
            );
         }

         shaper.setColor(1.0F, 0.75F, 0.25F, 1.0F);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() / 2.0F) * 64.0F - 4.0F, ((float)this.y + (float)this.getHeight() / 2.0F) * 64.0F - 4.0F, 8.0F, 8.0F
         );

         for (Booster b : this.mesh) {
            shaper.setColor(1.0F, 0.75F, 0.25F, 1.0F);
            shaper.rect(((float)b.x + (float)b.getWidth() / 2.0F) * 64.0F - 4.0F, ((float)b.y + (float)b.getHeight() / 2.0F) * 64.0F - 4.0F, 8.0F, 8.0F);
         }

         for (Pair<Booster, Booster> p : this.meshEdges) {
            shaper.setColor(1.0F, 0.75F, 0.25F, 1.0F);
            shaper.rectLine(
               ((float)p.getKey().x + (float)p.getKey().getWidth() / 2.0F) * 64.0F,
               ((float)p.getKey().y + (float)p.getKey().getHeight() / 2.0F) * 64.0F,
               ((float)p.getVal().x + (float)p.getVal().getWidth() / 2.0F) * 64.0F,
               ((float)p.getVal().y + (float)p.getVal().getHeight() / 2.0F) * 64.0F,
               4.0F
            );
         }
      }
   }

   public boolean isBoosting() {
      return this.meshMode && this.meshHost != null && this.meshHost.boosting || !this.meshMode && this.boosting;
   }

   public boolean isBoostActive() {
      return !this.meshMode && this.boostActive || this.meshMode && this.meshHost != null && this.meshHost.boostActive;
   }

   @Override
   public void setNearbyBooster(Booster booster) {
      if (booster != null) {
         this.nearby.add(booster);
      }

      Iterator<Booster> iter = this.nearby.iterator();

      while (iter.hasNext()) {
         Booster b = iter.next();
         if (this.layer.getStructure(b.getX() * this.layer.height + b.getY()) != b) {
            iter.remove();
         }
      }
   }

   public void updateMesh() {
      if (!this.meshMode) {
         for (Booster b : this.mesh) {
            b.updateMesh();
         }

         this.mesh.clear();
         this.meshEdges.clear();
         this.meshHost = null;
         this.rectangles.clear();
      } else {
         this.mesh.clear();
         this.meshEdges.clear();
         this.meshHost = this;
         this.meshHost.rectangles.clear();
         this.addToMeshRecursively(this);

         for (Booster b : this.mesh) {
            b.mesh.clear();
            b.addToMeshRecursively(b);
            b.meshHost = this.meshHost;
         }
      }
   }

   protected void addToMeshRecursively(Booster base) {
      for (Booster b : base.nearby) {
         if (b != this && b.meshMode && !this.mesh.contains(b)) {
            this.mesh.add(b);
            Pair<Booster, Booster> p1 = new Pair<Booster, Booster>().set(base, b);
            Pair<Booster, Booster> p2 = new Pair<Booster, Booster>().set(b, base);
            if (!this.meshEdges.contains(p1) && !this.meshEdges.contains(p2)) {
               this.meshEdges.add(p1);
            }

            if (b.getX() * this.layer.height + b.getY() < this.meshHost.getX() * this.layer.height + this.meshHost.getY()) {
               this.meshHost = b;
            }

            this.addToMeshRecursively(b);
         }
      }
   }

   public void setBoosting(boolean boosting) {
      if (this.meshMode) {
         if (this != this.meshHost) {
            this.meshHost.setBoosting(boosting);
         } else {
            this.boosting = boosting;
         }
      } else {
         this.boosting = boosting;
      }
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null) {
         this.updateStructures(false);
      }

      this.myBoostArea = new Rectangle(
         (float)((int)((float)this.x + (float)this.getWidth() / 2.0F - 9.5F)),
         (float)((int)((float)this.y + (float)this.getHeight() / 2.0F - 9.5F)),
         19.0F,
         19.0F
      );
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateStructures(false);
      this.updateMesh();
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.updateStructures(true);
      if (this.meshMode) {
         this.meshMode = false;
         this.updateMesh();
      }
   }

   protected void updateStructures(boolean delete) {
      if (this.layer != null) {
         int x1 = (int)((float)this.x + (float)this.getWidth() / 2.0F - 9.5F);
         int y1 = (int)((float)this.y + (float)this.getHeight() / 2.0F - 9.5F);

         for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 19; y++) {
               Structure<?> s = this.layer.getStructure(x + x1, y + y1);
               if (s != null && (delete || s.getNearbyBooster() == null)) {
                  s.setNearbyBooster(delete ? null : this);
               }
            }
         }
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.isBoostActive() && gameSpeed > 0) {
         this.playSfx();
      } else {
         this.pauseSfx();
      }

      if (gameSpeed != 0) {
         if (this.boosting && !this.meshMode && this.boostTime > 0.0) {
            this.boostTime -= (double)gameSpeed * deltaTime * 1000.0;
            this.boostActive = this.boostTime > 0.0;
         } else if (!this.meshMode || this.meshHost != this || !this.boosting) {
            this.boostActive = false;
         } else if (this.boostTime > 0.0) {
            this.boostTime = Math.max(0.0, this.boostTime - (double)gameSpeed * deltaTime * 1000.0);
            this.boostActive = true;
         } else {
            boolean any = false;

            for (Booster b : this.mesh) {
               if (b.boostTime > 0.0) {
                  b.boostTime = Math.max(0.0, b.boostTime - (double)gameSpeed * deltaTime * 1000.0);
                  any = true;
                  break;
               }
            }

            this.boostActive = any;
         }

         if (this.isBoostActive() && this.clicked) {
            this.updateUI();
         }
      }
   }

   @Override
   public void postUpdate(Bounds dirtyBounds) {
      super.postUpdate(dirtyBounds);
      if (dirtyBounds.hasAnyFlag(768)
         && dirtyBounds.intersects(
            (int)((float)this.x + (float)this.getWidth() / 2.0F - 9.5F), (int)((float)this.y + (float)this.getHeight() / 2.0F - 9.5F), 19, 19
         )) {
         this.updateStructures(false);
      }
   }

   private void updateUI() {
      if (this.meshMode && this.mesh.contains(Game.G.ui.currentClickedStructure)) {
         ((Booster)Game.G.ui.currentClickedStructure).updateUI();
      } else {
         if (this.container == null) {
            this.container = new Container();
         }

         long time = Math.round(this.boostTime / 1000.0 * 1000.0);
         if (this.meshMode) {
            long sum = 0L;

            for (Booster b : this.mesh) {
               sum += Math.round(b.boostTime / 1000.0 * 1000.0);
            }

            time += sum;
         }

         if (this.ui == null) {
            this.ui = GameUi.createResourceTable(
               32,
               Quarry.Q.skin,
               Quarry.Q.skin.getDrawable("icon_time"),
               String.format("%d:%02d:%02d.%03dh", time / 3600000L, time % 3600000L / 60000L, time % 60000L / 1000L, time % 1000L)
            );
            this.container.setActor(this.ui);
         } else {
            ((Label)this.ui.getChildren().get(1))
               .setText(String.format("%d:%02d:%02d.%03dh", time / 3600000L, time % 3600000L / 60000L, time % 60000L / 1000L, time % 1000L));
         }
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("mesh", (byte)(this.meshMode ? 1 : 0)).Byte("boost", (byte)(this.boosting ? 1 : 0)).Double("time", this.boostTime);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.meshMode = tag.Byte("mesh", (byte)0) == 1;
      this.boosting = tag.Byte("boost", (byte)0) == 1;
      this.boostTime = tag.Double("time", 0.0);
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("mesh", (byte)(this.meshMode ? 1 : 0));
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      this.meshMode = tag.Byte("mesh", (byte)0) == 1;
      this.updateStructures(false);
      this.updateMesh();
   }
}
