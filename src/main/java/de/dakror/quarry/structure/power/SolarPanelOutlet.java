package de.dakror.quarry.structure.power;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import de.dakror.quarry.scenes.Game;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.GameUi;
import de.dakror.quarry.structure.base.Direction;
import de.dakror.quarry.structure.base.Dock;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.CopyRegionHelper;
import de.dakror.quarry.util.SpriterDelegateBatch;
import java.util.HashSet;

public class SolarPanelOutlet extends Structure<Schema> {
   public static final Schema classSchema = new Schema(
         0,
         StructureType.SolarPanelOutlet,
         true,
         3,
         3,
         "solarpaneloutlet",
         new Item.Items(Item.ItemType.SiliconWafer, 40, Item.ItemType.Glass, 50, Item.ItemType.Battery, 5, Item.ItemType.SteelCable, 40),
         null,
         new Dock(1, 2, Direction.North, Dock.DockType.BigPower)
      )
      .<Schema>loudness(0.2F)
      .<Schema>flags(Schema.Flags.MirroredTextureHorizontal)
      .sciences(Science.ScienceType.SolarPower);
   public static final int POWER_OUT = 6000;
   Table ui;
   Container<Table> container;
   Array<SolarPanel> connectedPanels = new Array();
   int[] panelsIndex;
   final HashSet<Integer> tmp = new HashSet<>();

   public SolarPanelOutlet(int x, int y) {
      super(x, y, classSchema);
   }

   @Override
   public void onPlacement(boolean fromLoading) {
      super.onPlacement(fromLoading);
      if (!fromLoading && this.layer != null) {
         this.resetAdjacentPanels();
         this.discoverAdjacentPanels(this, this.tmp);
      }
   }

   private void resetAdjacentPanels() {
      this.tmp.clear();

      for (SolarPanel p : this.connectedPanels) {
         p.outlet = null;
         this.layer.markDirty(p, 0);
      }

      this.connectedPanels.clear();
   }

   private void discoverAdjacentPanels(Structure<?> of, HashSet<Integer> history) {
      if ((of instanceof SolarPanel && ((SolarPanel)of).outlet == null || of == this) && !history.contains(of.getX() * this.layer.height + of.getY())) {
         history.add(of.getX() * this.layer.height + of.getY());
         if (of instanceof SolarPanel) {
            this.connectedPanels.add((SolarPanel)of);
            ((SolarPanel)of).outlet = this;
         }

         for (int i = 0; i < of.getWidth(); i++) {
            this.discoverAdjacentPanels(this.layer.getStructure(of.getX() - 1, of.getY() + i), history);
            this.discoverAdjacentPanels(this.layer.getStructure(of.getX() + i, of.getY() - 1), history);
            this.discoverAdjacentPanels(this.layer.getStructure(of.getX() + of.getWidth(), of.getY() + i), history);
            this.discoverAdjacentPanels(this.layer.getStructure(of.getX() + i, of.getY() + of.getHeight()), history);
         }
      }

      if (this.clicked) {
         this.updateUI();
      }
   }

   private void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      if (this.container == null) {
         this.container = new Container();
      }

      if (this.ui == null) {
         this.ui = GameUi.createResourceTable(32, Quarry.Q.skin, Quarry.Q.skin.getDrawable("icon_power"), "");
         this.container.setActor(this.ui);
      }

      ((Label)this.ui.getChildren().get(1)).setText(GameUi.formatPowerAmount((double)((this.connectedPanels.size + 1) * 6000)) + "/s");
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.updateUI();
      content.add(this.container).grow();
   }

   @Override
   public void postLoad() {
      super.postLoad();

      for (int i : this.panelsIndex) {
         Structure<?> p = this.layer.getStructure(i);
         if (p instanceof SolarPanel) {
            this.connectedPanels.add((SolarPanel)p);
            ((SolarPanel)p).outlet = this;
         }
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      this.resetAdjacentPanels();
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (gameSpeed > 0 && this.powerNetwork != null) {
         this.powerNetwork.offerPower(deltaTime, gameSpeed, (double)(6000 * (this.connectedPanels.size + 1)) * deltaTime, this);
      }

      if (!dirtyBounds.isEmpty()) {
         this.resetAdjacentPanels();
         this.discoverAdjacentPanels(this, this.tmp);
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.clicked) {
         shaper.set(ShapeType.Filled);
         shaper.setColor(Color.PURPLE);
         shaper.rect(
            ((float)this.x + (float)this.getWidth() * 0.5F) * 64.0F - 10.0F, ((float)this.y + (float)this.getHeight() * 0.5F) * 64.0F - 10.0F, 20.0F, 20.0F
         );

         for (Structure<?> p : this.connectedPanels) {
            shaper.rectLine(
               ((float)this.x + (float)this.getWidth() * 0.5F) * 64.0F,
               ((float)this.y + (float)this.getHeight() * 0.5F) * 64.0F,
               ((float)p.x + (float)p.getWidth() * 0.5F) * 64.0F,
               ((float)p.y + (float)p.getHeight() * 0.5F) * 64.0F,
               3.0F
            );
            shaper.rect(((float)p.x + (float)p.getWidth() * 0.5F) * 64.0F - 6.0F, ((float)p.y + (float)p.getHeight() * 0.5F) * 64.0F - 6.0F, 12.0F, 12.0F);
         }
      }
   }

   @Override
   public void postUpdate(Bounds dirtyBounds) {
      super.postUpdate(dirtyBounds);
      if (dirtyBounds.hasAnyFlag(768)) {
         this.resetAdjacentPanels();
         this.discoverAdjacentPanels(this, this.tmp);
      }
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      int[] arr = new int[this.connectedPanels.size];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = ((SolarPanel)this.connectedPanels.get(i)).x * this.layer.height + ((SolarPanel)this.connectedPanels.get(i)).y;
      }

      b.IntArray("panels", arr);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      this.panelsIndex = tag.IntArray("panels", new int[0]);
   }

   @Override
   protected void copyData(int[] copyRegion, NBT.Builder b) {
      super.copyData(copyRegion, b);
      int[] arr = new int[this.connectedPanels.size];

      for (int i = 0; i < arr.length; i++) {
         arr[i] = CopyRegionHelper.indexGlobalToRelative(
            copyRegion, ((SolarPanel)this.connectedPanels.get(i)).x * this.layer.height + ((SolarPanel)this.connectedPanels.get(i)).y
         );
      }

      b.IntArray("panels", arr);
   }

   @Override
   protected void pasteData(int[] pasteRegion, NBT.CompoundTag tag) {
      super.pasteData(pasteRegion, tag);
      int[] panels = tag.IntArray("panels", new int[0]);

      for (int i : panels) {
         Structure<?> p = CopyRegionHelper.getStructure(this, pasteRegion, i);
         if (p instanceof SolarPanel) {
            this.connectedPanels.add((SolarPanel)p);
            ((SolarPanel)p).outlet = this;
         }
      }
   }
}
