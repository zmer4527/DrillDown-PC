package de.dakror.quarry.structure.base;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.logistics.Valve;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.CopyRegionHelper;
import de.dakror.quarry.util.Sfx;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.HashMap;

public abstract class FluidTubeStructure extends Structure<FluidTubeStructure.FluidTubeSchema> implements ITube {
   protected static final HashMap<Class<? extends FluidTubeStructure>, TextureRegion[]> texCache = new HashMap<>();
   private static final int TEX_N = 0;
   private static final int TEX_EW = 1;
   private static final int TEX_SW = 2;
   private static final int TEX_ESW = 3;
   private static final int TEX_NESW = 4;
   static final float decupple = 1.0F;
   static final int size = 40;
   protected int fluidLevel;
   protected Item.FluidType fluid;
   protected Item.ItemType fluidItem;
   protected float delay;
   protected int[] structs;
   protected Structure<?>[] structures = new Structure[4];
   protected Dock.DockType[] types = new Dock.DockType[4];
   protected float[] decuppleTime = new float[4];
   Table ui;
   Container<Table> container;
   Item.ItemType uiType;

   public FluidTubeStructure(int x, int y, FluidTubeStructure.FluidTubeSchema schema) {
      super(x, y, schema);
      this.initTextures();
   }

   private void initTextures() {
      if (!texCache.containsKey(this.getClass())) {
         texCache.put(
            (Class<? extends FluidTubeStructure>)this.getClass(),
            new TextureRegion[]{
               Quarry.Q.atlas.findRegion("structure_" + this.getSchema().texName + "_n"),
               Quarry.Q.atlas.findRegion("structure_" + this.getSchema().texName + "_ew"),
               Quarry.Q.atlas.findRegion("structure_" + this.getSchema().texName + "_sw"),
               Quarry.Q.atlas.findRegion("structure_" + this.getSchema().texName + "_esw"),
               Quarry.Q.atlas.findRegion("structure_" + this.getSchema().texName + "_nesw")
            }
         );
      }
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      if (this.fluidLevel >= this.getSchema().maxFluid) {
         return false;
      } else {
         return this.fluid != null ? item == this.fluidItem : this.isAllowedFluid(item);
      }
   }

   protected abstract boolean isAllowedFluid(Item.ItemType var1);

   @Override
   public int acceptFluid(Item.ItemType item, int amount, Structure<?> source) {
      if (this.fluidLevel >= this.getSchema().maxFluid) {
         return amount;
      } else if (this.fluid != null && item != this.fluidItem) {
         return amount;
      } else {
         if (this.fluid == null) {
            if (!this.isAllowedFluid(item)) {
               return amount;
            }

            this.fluid = Item.fluid((byte)item.value);
            this.fluidItem = item;
         }

         if (this.delay <= 0.0F) {
            this.delay = this.fluid.viscosity;
         }

         int old = this.fluidLevel;
         this.fluidLevel = Math.min(this.getSchema().maxFluid, this.fluidLevel + amount);
         if (this.clicked) {
            this.updateUI();
         }

         return Math.max(0, old + amount - this.fluidLevel);
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (dirtyBounds.touches(this) || this.layer == null) {
         this.updateStructures();
      }

      if (this.fluidLevel <= 0 && this.fluid != null) {
         this.fluidLevel = 0;
         this.fluid = null;
         this.fluidItem = null;
         this.delay = 0.0F;
         this.updateUI();
      }

      if (this.fluid != null && gameSpeed != 0) {
         this.delay = (float)((double)this.delay - deltaTime * (double)gameSpeed);
         if (this.delay <= 0.0F) {
            for (int i = 0; i < 4; i++) {
               if (this.structures[i] != null
                  && this.types[i] != Dock.DockType.FluidOut
                  && this.structures[i].canAccept(this.fluidItem, this.x, this.y, Direction.values[i])) {
                  int dif = 0;
                  if (!(this.structures[i] instanceof FluidTubeStructure) || this.structures[i] instanceof Valve) {
                     dif = this.fluidLevel;
                  } else if (((FluidTubeStructure)this.structures[i]).fluidLevel < this.fluidLevel) {
                     dif = this.fluidLevel - ((FluidTubeStructure)this.structures[i]).fluidLevel;
                  }

                  dif = (int)Math.ceil((double)((float)dif * this.fluid.pressure));
                  if (dif > 0) {
                     this.fluidLevel = this.fluidLevel - dif + this.structures[i].acceptFluid(this.fluidItem, dif, this);
                     this.decuppleTime[i] = 1.0F;
                  }
               }
            }

            this.delay = this.fluid.viscosity;
            if (this.clicked) {
               this.updateUI();
            }
         }

         for (int ix = 0; ix < 4; ix++) {
            if (this.decuppleTime[ix] > 0.0F) {
               this.decuppleTime[ix] = (float)((double)this.decuppleTime[ix] - deltaTime * (double)gameSpeed);
               if (this.decuppleTime[ix] <= 0.0F) {
                  this.decuppleTime[ix] = 0.0F;
               }
            }
         }
      }
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      if (!fromLoading && this.layer == null) {
         this.updateStructures();
      }
   }

   protected void updateStructures() {
      Layer l = this.layer == null ? Game.G.layer : this.layer;

      for (Direction d : Direction.values) {
         Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + d.dx) * l.height + this.y + d.dy);
         if (s == null) {
            s = l.getStructure(this.x + d.dx, this.y + d.dy);
         }

         Dock.DockType type = null;
         if (s != null && s.getDocks().length > 0) {
            boolean any = false;

            for (Dock dock : s.getDocks()) {
               if (s.isNextToDock(this.x, this.y, d, dock) && (dock.type == Dock.DockType.FluidIn || dock.type == Dock.DockType.FluidOut)) {
                  type = dock.type;
                  any = true;
                  break;
               }
            }

            if (!any) {
               s = null;
            }
         } else if (s == null || !(s instanceof FluidTubeStructure) || s instanceof Valve && ((Valve)s).getDirection() == d) {
            s = null;
            type = null;
         }

         this.types[d.ordinal()] = type;
         this.structures[d.ordinal()] = s;
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.fluidLevel > 0) {
         float width = Math.max(1.0F, 27.5F * ((float)this.fluidLevel / (float)this.getSchema().maxFluid));
         float o = (64.0F - width) / 2.0F;
         spriter.add(this.fluid.icon, (float)(this.x * 64) + o, (float)(this.y * 64) + o, -4.99F, width, width);
         if (this.structures[0] != null || this.decuppleTime[0] > 0.0F) {
            spriter.add(this.fluid.icon, (float)(this.x * 64) + o, (float)(this.y * 64) + (64.0F - width) / 2.0F + width, -4.99F, width, o);
         }

         if (this.structures[1] != null || this.decuppleTime[1] > 0.0F) {
            spriter.add(this.fluid.icon, (float)(this.x * 64) + o + width, (float)(this.y * 64) + o, -4.99F, o, width);
         }

         if (this.structures[2] != null || this.decuppleTime[2] > 0.0F) {
            spriter.add(this.fluid.icon, (float)(this.x * 64) + o, (float)(this.y * 64), -4.99F, width, o);
         }

         if (this.structures[3] != null || this.decuppleTime[3] > 0.0F) {
            spriter.add(this.fluid.icon, (float)(this.x * 64), (float)(this.y * 64) + o, -4.99F, o, width);
         }
      }
   }

   @Override
   public void saveData(NBT.Builder b) {
      super.saveData(b);
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         str[i] = this.structures[i] == null ? -1 : this.structures[i].x * this.layer.height + this.structures[i].y;
      }

      b.IntArray("structs", str);
      if (this.fluidItem != null) {
         b.Short("fluid", this.fluidItem.value);
         b.Short("level", (short)this.fluidLevel);
      }
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      int[] str = new int[4];

      for (int i = 0; i < 4; i++) {
         str[i] = this.structures[i] == null
            ? -1
            : CopyRegionHelper.indexGlobalToRelative(copyRegion, this.structures[i].x * this.layer.height + this.structures[i].y);
      }

      b.IntArray("structs", str);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);

      try {
         int[] str = tag.IntArray("structs");

         for (int i = 0; i < 4; i++) {
            this.structures[i] = CopyRegionHelper.getStructure(this, pasteRegion, str[i]);
         }
      } catch (NBT.NBTException var5) {
         Quarry.Q.pi.message(1, var5);
      }
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);

      try {
         this.structs = tag.IntArray("structs");
      } catch (NBT.NBTException var5) {
         Quarry.Q.pi.message(1, var5);
      }

      short flu = tag.Short("fluid", (short)0);
      if (flu != 0) {
         try {
            this.fluidItem = Item.get(flu);
            if (this.fluidItem != null) {
               this.fluid = Item.fluid((byte)this.fluidItem.value);
            }

            this.fluidLevel = tag.Short("level");
         } catch (NBT.NBTException var4) {
            Quarry.Q.pi.message(1, var4);
         }
      }
   }

   @Override
   public void postLoad() {
      if (this.structs != null) {
         for (int i = 0; i < 4; i++) {
            this.structures[i] = this.structs[i] == -1 ? null : this.layer.getStructure(this.structs[i]);
         }

         this.structs = null;
      }
   }

   private void updateUI() {
      if (this.container == null) {
         this.container = new Container();
      }

      if (this.ui != null && this.uiType == this.fluidItem) {
         ((Label)this.ui.getChildren().get(1)).setText(GameUi.formatResourceAmount((float)this.fluidLevel / 1000.0F, true) + "L");
         ((TextTooltip)this.ui.getListeners().get(0))
            .getActor()
            .setText(GameUi.formatResourceAmount((float)this.fluidLevel / 1000.0F, true) + "L " + this.uiType.title);
      } else {
         this.uiType = this.fluidItem;
         if (this.uiType != null) {
            this.ui = GameUi.createResourceTable(32, Quarry.Q.skin, this.uiType, GameUi.formatResourceAmount((float)this.fluidLevel / 1000.0F, true) + "L");
            this.container.setActor(this.ui);
         } else {
            this.ui = null;
            this.container.setActor(null);
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
   public void draw(SpriteRenderer spriter) {
      int x = this.x * 64 + 12;
      int y = this.y * 64 + 12;
      TextureRegion tex = this.getSchema().tex;
      int rot = 0;
      TextureRegion[] seg = texCache.get(this.getClass());
      if (this.structures[0] != null && this.structures[1] != null && this.structures[2] != null && this.structures[3] != null) {
         tex = seg[4];

         for (Direction d : Direction.values) {
            spriter.add(seg[1], (float)(x + 40 * d.dx), (float)(y + 40 * d.dy), -5.0F, 20.0F, 20.0F, 12.0F, 40.0F, 1.0F, 1.0F, (float)d.rot);
         }
      } else {
         int prio = 0;

         for (Direction d : Direction.values) {
            if (this.structures[d.ordinal()] == null
               || this.structures[d.inv().ordinal()] == null
               || this.structures[d.prev().ordinal()] == null && this.structures[d.next().ordinal()] == null) {
               if (prio <= 2 && this.structures[d.ordinal()] != null && this.structures[d.inv().ordinal()] != null) {
                  prio = 2;
                  tex = seg[1];
                  rot = d.rot;
               } else if (prio <= 1 && this.structures[d.ordinal()] != null && this.structures[d.next().ordinal()] != null) {
                  prio = 1;
                  tex = seg[2];
                  rot = d.rot + 90;
               } else if (prio <= 0 && this.structures[d.ordinal()] != null) {
                  prio = 0;
                  tex = seg[0];
                  rot = d.rot - 90;
               }
            } else {
               prio = 3;
               tex = seg[3];
               rot = d.rot + (this.structures[d.prev().ordinal()] != null ? 180 : 0);
            }

            if (this.structures[d.ordinal()] != null) {
               spriter.add(seg[1], (float)(x + 40 * d.dx), (float)(y + 40 * d.dy), -5.0F, 20.0F, 20.0F, 12.0F, 40.0F, 1.0F, 1.0F, (float)d.rot);
            }
         }
      }

      if (tex != null) {
         spriter.add(tex, (float)x, (float)y, -5.0F, 20.0F, 20.0F, 40.0F, 40.0F, 1.0F, 1.0F, (float)rot);
      }
   }

   public static class FluidTubeSchema extends Schema {
      public final int maxFluid;

      public FluidTubeSchema(int version, StructureType type, int maxFluid, int width, int height, String tex, Item.Items buildCosts, Sfx sfx) {
         super(version, type, true, width, height, tex, buildCosts, sfx);
         this.maxFluid = maxFluid;
         this.flags(new Schema.Flags[]{Schema.Flags.Draggable, Schema.Flags.NoDustEffect});
         this.loudness = 0.5F;
      }
   }
}
