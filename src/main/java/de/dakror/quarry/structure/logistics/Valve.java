package de.dakror.quarry.structure.logistics;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.FluidTubeStructure;
import de.dakror.quarry.structure.base.IRotatable;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;

public class Valve extends FluidTubeStructure implements IRotatable {
   public static final FluidTubeStructure.FluidTubeSchema classSchema = new FluidTubeStructure.FluidTubeSchema(
         0, StructureType.Valve, 10000, 1, 1, "valve", new Item.Items(Item.ItemType.SteelTube, 4, Item.ItemType.SteelIngot, 8), null
      )
      .<Schema>sciences(new Science.ScienceType[]{Science.ScienceType.OilProcessing})
      .removeFlags(Schema.Flags.Draggable);
   static final TextureRegion top = Quarry.Q.atlas.findRegion("structure_valve_top");
   Direction dir = Direction.South;
   Direction flowDir = Direction.North;
   ImageButton ui;

   public Valve(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void rotate() {
      this.dir = this.dir.next();
      this.flowDir = this.flowDir.next();
      if (this.ui != null) {
         this.ui.getImage().setRotation((float)(this.flowDir.rot - Direction.South.rot));
      }

      this.updateFlowDir();
      this.setDirty();
   }

   private void updateFlowDir() {
      if (this.ui != null) {
         this.ui.getImage().setOrigin(this.ui.getImage().getPrefWidth() / 2.0F, this.ui.getImage().getPrefHeight() / 2.0F);
         this.ui.getImage().setRotation((float)(this.flowDir.rot - Direction.South.rot));
         this.ui.setChecked(this.flowDir == this.dir.inv());
      }

      this.updateStructures();
   }

   @Override
   protected void updateStructures() {
      Layer l = this.layer == null ? Game.G.layer : this.layer;

      for (Direction d : Direction.values) {
         if (d != this.dir.inv() && this.flowDir != this.dir.inv()) {
            Structure<?> s = (Structure<?>)Game.G.activeStructureTrail.get((this.x + d.dx) * l.height + this.y + d.dy);
            if (s == null) {
               s = l.getStructure(this.x + d.dx, this.y + d.dy);
            }

            Dock.DockType type = Dock.DockType.FluidOut;
            if (s != null && s.getDocks().length > 0) {
               boolean any = false;

               for (Dock dock : s.getDocks()) {
                  if (s.isNextToDock(this.x, this.y, d, dock)
                     && (d == this.flowDir && dock.type == Dock.DockType.FluidIn || dock.type == Dock.DockType.FluidOut)) {
                     type = dock.type;
                     any = true;
                     break;
                  }
               }

               if (!any) {
                  type = null;
                  s = null;
               }
            } else if (s == null) {
               s = null;
               type = null;
            }

            if (d == this.flowDir) {
               type = Dock.DockType.FluidIn;
            }

            this.types[d.ordinal()] = type;
            this.structures[d.ordinal()] = s;
         } else {
            this.types[d.ordinal()] = null;
            this.structures[d.ordinal()] = null;
         }
      }
   }

   @Override
   public void postLoad() {
      super.postLoad();
      this.updateStructures();
   }

   @Override
   public boolean canAccept(Item.ItemType item, int x, int y, Direction dir) {
      return item.categories.contains(Item.ItemCategory.Fluid) && Item.base(item) != Item.ItemType._MoltenMetal && dir != this.flowDir.inv() && dir != this.dir;
   }

   @Override
   public void setRotation(Direction direction) {
      this.dir = direction;
      this.updateFlowDir();
      this.setDirty();
   }

   @Override
   public Direction getDirection() {
      return this.dir;
   }

   @Override
   public Object clone() {
      Valve valve = (Valve)super.clone();
      valve.dir = this.dir;
      valve.flowDir = this.flowDir;
      return valve;
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      if (this.ui == null) {
         this.ui = new ImageButton(Quarry.Q.skin, "flow_dir");
         this.ui.pad(16.0F);
         this.ui.addListener(new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
               Valve.this.flowDir = Valve.this.flowDir.next();
               Valve.this.updateFlowDir();
            }
         });
         this.updateFlowDir();
      }

      content.add(this.ui).expand();
   }

   @Override
   public void draw(SpriteRenderer spriter) {
      float var10002 = (float)(this.x * 64);
      float var10003 = (float)(this.y * 64);
      spriter.add(this.getSchema().tex, var10002, var10003, -5.0F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)(this.dir.rot + 90));
      spriter.add(top, (float)(this.x * 64), (float)(this.y * 64), -4.9F, 32.0F, 32.0F, 64.0F, 64.0F, 1.0F, 1.0F, (float)(this.dir.rot + 90));
   }

   @Override
   public void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);

      try {
         this.dir = Direction.values[tag.Byte("dir")];
         this.flowDir = Direction.values[tag.Byte("flow")];
      } catch (NBT.NBTException var3) {
         Quarry.Q.pi.message(1, var3);
      }
   }

   @Override
   public void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("dir", (byte)this.dir.ordinal()).Byte("flow", (byte)this.flowDir.ordinal());
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      b.Byte("dir", (byte)this.dir.ordinal()).Byte("flow", (byte)this.flowDir.ordinal());
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);

      try {
         this.dir = Direction.values[tag.Byte("dir")];
         this.flowDir = Direction.values[tag.Byte("flow")];
      } catch (NBT.NBTException var4) {
         Quarry.Q.pi.message(1, var4);
      }
   }

   @Override
   protected boolean isAllowedFluid(Item.ItemType i) {
      return i.categories.contains(Item.ItemCategory.Fluid) && Item.base(i) != Item.ItemType._MoltenMetal;
   }
}
