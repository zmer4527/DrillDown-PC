package de.dakror.quarry.structure;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import de.dakror.common.BiCallback;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.structure.base.Schema;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.structure.base.StructureType;
import de.dakror.quarry.util.Bounds;
import de.dakror.quarry.util.SpriterDelegateBatch;
import net.spookygames.gdx.sfx.SfxSound;

public class ScienceLab extends Structure<Schema> {
   public static ScienceLab M;
   public static final Schema classSchema = new Schema(
      0, StructureType.ScienceLab, true, 2, 2, "sciencelab", new Item.Items(Item.ItemType.StoneBrick, 10, Item.ItemType.Scaffolding, 10), null
   );
   Science.ScienceType activeScience;
   float workingTime;
   Table ui;
   Container<Table> container;
   static SfxSound sfxSound = (SfxSound)Quarry.Q.assets.get("sfx/science" + Const.SFX_FORMAT);

   public ScienceLab(int x, int y) {
      super(x, y, classSchema);
      if (x >= 0) {
         M = this;
      }
   }

   public boolean setActiveScience(Science.ScienceType science) {
      if (this.activeScience != null) {
         return false;
      } else {
         for (Item.Items.Amount a : science.costs.entries) {
            if (a.getItem() != null) {
               int has = Game.G.getResource(a.getItem());
               if (has < a.getAmount()) {
                  return false;
               }
            }
         }

         for (Item.Items.Amount ax : science.costs.entries) {
            if (ax.getItem() != null) {
               Game.G.removeResources(ax.getItem(), ax.getAmount(), true);
            }
         }

         Game.G.addCurrentScience(science);
         this.activeScience = science;
         this.workingTime = science.workingTime;
         this.updateUI();
         return true;
      }
   }

   @Override
   public void update(double deltaTime, int gameSpeed, Bounds dirtyBounds) {
      super.update(deltaTime, gameSpeed, dirtyBounds);
      if (this.activeScience != null) {
         this.workingTime = (float)((double)this.workingTime - deltaTime * (double)gameSpeed);
         if (this.workingTime <= 0.0F) {
            Game.G.removeCurrentScience(this.activeScience);
            Game.G.addScience(this.activeScience);
            this.activeScience = null;
            // SoundManager.update 在渲染线程跑、Stage 也只能在渲染线程碰，所以都回迁
            Game.G.postUi(() -> {
               Quarry.Q.sound.play(this.sfxSound);
               this.updateUI();
            });
         }
      }
   }

   @Override
   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper, SpriterDelegateBatch pfxBatch) {
      super.drawFrame(spriter, shaper, pfxBatch);
      if (this.activeScience != null) {
         shaper.setColor(0.0F, 0.5F, 0.0F, 1.0F);
         float progress = 1.0F - this.workingTime / this.activeScience.workingTime;
         shaper.rect((float)(this.x * 64), (float)(this.y * 64), progress * (float)this.getWidth() * 64.0F, 8.0F);
      }
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      if (this.activeScience != null) {
         Game.G.removeCurrentScience(this.activeScience);
      }

      if (M == this) {
         M = null;
      }
   }

   protected void updateUI() {
      if (!Game.isRenderThread()) {
         Game.G.postUi(this::updateUI);
         return;
      }
      if (this.ui != null) {
         if (this.activeScience != null) {
            this.container.setActor(this.ui);
            ((TextureRegionDrawable)((Image)this.ui.getChildren().get(0)).getDrawable()).setRegion(this.activeScience.icon);
            ((Label)this.ui.getChildren().get(1)).setText(this.activeScience.title + " " + (int)this.workingTime + "s");
         } else {
            this.container.setActor(null);
         }
      }
   }

   @Override
   public void onClick(Table content) {
      super.onClick(content);
      this.ui = new Table();
      this.ui.add(new Image(Quarry.Q.skin, "icon_science")).size(48.0F, 48.0F);
      Label title = new Label("", Quarry.Q.skin, "default-font", Color.WHITE);
      title.setAlignment(1);
      this.ui.add(title).growX();
      this.container = new Container();
      content.add(this.container).grow();
   }

   @Override
   protected void saveData(NBT.Builder b) {
      super.saveData(b);
      b.Byte("activeScience", this.activeScience != null ? this.activeScience.id : -1);
      b.Float("workingTime", this.workingTime);
   }

   @Override
   protected void loadData(NBT.CompoundTag tag) throws NBT.NBTException {
      super.loadData(tag);
      byte id = tag.Byte("activeScience", (byte)-1);
      if (id != -1) {
         this.activeScience = Science.ScienceType.values[id];
         Game.G.addCurrentScience(this.activeScience);
         M = this;
      }

      this.workingTime = tag.Float("workingTime", 0.0F);
   }

   static {
      classSchema.button(
         new Schema.ButtonDef("icon_science", "button.science", Schema.ButtonDef.ButtonType.TempRadio, new BiCallback<Boolean, Structure<?>>() {
            public void call(Boolean u, Structure<?> v) {
               if (u) {
                  if (ScienceLab.M != null && ScienceLab.M.activeScience == null) {
                     Game.G.ui.showScienceUI();
                  }
               } else {
                  Game.G.ui.hideScienceUI();
               }
            }
         })
      );
   }
}
