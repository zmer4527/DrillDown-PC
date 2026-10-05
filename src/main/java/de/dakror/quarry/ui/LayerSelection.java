package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.SnapshotArray;
import de.dakror.common.libgdx.ChangeNotifier;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.game.Layer;
import de.dakror.quarry.game.Science;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.util.Util;

@LmlTag(
   tagName = "layerselection"
)
public class LayerSelection extends Table implements CustomTag, ChangeNotifier.Listener<Layer> {
   Label label;
   public ImageButton up;
   public ImageButton up10;
   public ImageButton down;
   public ImageButton down10;
   VerticalGroup minerals;

   public LayerSelection(Skin skin, String style) {
      super(skin);
   }

   @Override
   public void postInit() {
      this.label = Util.id("label");
      this.up = Util.id("up");
      this.up10 = Util.id("up10");
      this.down = Util.id("down");
      this.down10 = Util.id("down10");
      this.minerals = Util.id("minerals");
      SnapshotArray<Actor> mins = this.minerals.getChildren();
      ((Actor)mins.get(0)).setUserObject(Item.ItemType.Dirt);
      ((Actor)mins.get(1)).setUserObject(Item.ItemType.Clay);
      ((Actor)mins.get(2)).setUserObject(Item.ItemType.CoalOre);
      ((Actor)mins.get(3)).setUserObject(Item.ItemType.IronOre);
      ((Actor)mins.get(4)).setUserObject(Item.ItemType.CopperOre);
      ((Actor)mins.get(5)).setUserObject(Item.ItemType.TinOre);
      ((Actor)mins.get(6)).setUserObject(Item.ItemType.CrudeOil);

      for (Actor a : mins) {
         a.addListener(new TextTooltip(((Item.ItemType)a.getUserObject()).title, this.getSkin()));
      }

      this.up.setDisabled(true);
      Image img = this.up.getImage();
      this.up.getImageCell().size(48.0F);
      img.setOrigin(24.0F, 24.0F);
      img.setRotation(90.0F);
      this.up.getImage().invalidateHierarchy();
      this.up10.setDisabled(true);
      img = this.up10.getImage();
      this.up10.getImageCell().size(48.0F);
      img.setOrigin(24.0F, 24.0F);
      img.setRotation(90.0F);
      this.up10.getImage().invalidateHierarchy();
      img = this.down.getImage();
      this.down.getImageCell().size(48.0F);
      img.setOrigin(24.0F, 24.0F);
      img.setRotation(-90.0F);
      this.down.getImage().invalidateHierarchy();
      img = this.down10.getImage();
      this.down10.getImageCell().size(48.0F);
      img.setOrigin(24.0F, 24.0F);
      img.setRotation(-90.0F);
      this.down10.getImage().invalidateHierarchy();
      this.up.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.changeLayer(-1);
         }
      });
      this.up10.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.changeLayer(-10);
         }
      });
      this.down.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.changeLayer(1);
         }
      });
      this.down10.addListener(new ChangeListener() {
         public void changed(ChangeEvent event, Actor actor) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            Game.G.changeLayer(10);
         }
      });
      this.setVisible(false);
      this.setPosition(Const.UI_W - this.getWidth() + 40.0F, (Const.UI_H - this.getHeight()) / 2.0F);
   }

   public void onScienceChange() {
      ((Actor)this.minerals.getChildren().get(5)).setVisible(Game.G.hasScience(Science.ScienceType.MineExpansion));
      ((Actor)this.minerals.getChildren().get(6)).setVisible(Game.G.hasScience(Science.ScienceType.OilProcessing));
   }

   @Override
   public void onChangeEvent(ChangeNotifier.Event<Layer> event) {
      this.setVisible(Game.G.getLayerCount() > 1);
      this.label.setText(0 - Game.G.layer.getIndex() + "");
      this.up.setDisabled(Game.G.layer.getIndex() == 0);
      this.up10.setDisabled(Game.G.layer.getIndex() - 10 < 0);
      this.down10.setDisabled(Game.G.layer.getIndex() + 10 > Game.G.getLayerCount() - 1);
      this.down.setDisabled(Game.G.layer.getIndex() == Game.G.getLayerCount() - 1);
      Game.G.camControl.updateActiveElementPlaceable();

      for (Actor a : this.minerals.getChildren()) {
         Image i = (Image)a;
         Item.ItemType t = (Item.ItemType)a.getUserObject();
         if (Game.G.layer.minerals.contains(t)) {
            i.setDrawable(t.drawable);
         } else {
            i.setDrawable(Item.ItemType.Nil.drawable);
         }
      }
   }
}
