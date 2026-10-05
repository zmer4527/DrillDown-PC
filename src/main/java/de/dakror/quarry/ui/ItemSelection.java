package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.modified.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import de.dakror.common.Callback;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.scenes.GameUi;
import java.util.EnumMap;

public class ItemSelection extends Table {
   ImageButton x;
   ImageButton nil;
   ClickListener listener;
   Callback<Item.ItemType> callback;
   HorizontalGroup group;
   ImageButton chevronLeft;
   ImageButton chevronRight;
   EnumMap<Item.ItemType, ImageButton> buttons = new EnumMap<>(Item.ItemType.class);

   public ItemSelection(Skin skin, String style) {
      this.setVisible(false);
      this.setSize(700.0F, 100.0F);
      this.setPosition((Const.UI_W - this.getWidth()) / 2.0F, 450.0F);
      this.addListener(new InputListener() {
         public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            return true;
         }
      });
      this.setBackground(GameUi.getDrawable(skin, "panel_metalDark", 16, 16, 16, 16));
      this.group = new HorizontalGroup();
      this.group.pad(20.0F).center().left().grow().space(15.0F);
      final ScrollPane scroll = new ScrollPane(this.group, Quarry.Q.skin) {
         public void act(float delta) {
            super.act(delta);
            if (this.isFlinging() || this.isPanning() || this.getVisualScrollX() != this.getScrollX()) {
               float scroll = this.getVisualScrollX();
               ItemSelection.this.chevronLeft.setVisible(scroll > 0.0F);
               ItemSelection.this.chevronRight.setVisible(scroll < this.getMaxX());
            }
         }
      };
      ScrollPaneStyle sps = new ScrollPaneStyle(scroll.getStyle());
      sps.background = null;
      scroll.setStyle(sps);
      scroll.setTouchable(Touchable.enabled);
      scroll.setScrollbarsOnTop(true);
      scroll.setScrollingDisabled(false, true);
      this.x = new ImageButton(Quarry.Q.skin.getDrawable("symb_x"));
      this.listener = new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            ItemSelection.this.callback.call((Item.ItemType)event.getTarget().getParent().getUserObject());
            ItemSelection.this.hide();
         }
      };
      this.x.addListener(this.listener);
      this.nil = new ImageButton(Item.ItemType.Nil.drawable);
      this.nil.getImageCell().size(40.0F);
      this.nil.setUserObject(Item.ItemType.Nil);
      this.nil.addListener(this.listener);
      this.chevronLeft = new ImageButton(Quarry.Q.skin.getDrawable("caret_left"));
      this.chevronLeft.getImageCell().pad(0.0F, 15.0F, 0.0F, 15.0F);
      this.chevronLeft.setVisible(false);
      this.chevronRight = new ImageButton(Quarry.Q.skin.getDrawable("caret_right"));
      this.chevronRight.getImageCell().pad(0.0F, 15.0F, 0.0F, 15.0F);
      this.add(this.chevronLeft).growY();
      this.add(scroll).grow();
      this.add(this.chevronRight).growY();
      this.chevronLeft.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            scroll.setScrollX(scroll.getScrollX() - 300.0F);
         }
      });
      this.chevronRight.addListener(new ClickListener() {
         public void clicked(InputEvent event, float x, float y) {
            Quarry.Q.sound.play(Quarry.Q.clickSfx);
            scroll.setScrollX(scroll.getScrollX() + 300.0F);
         }
      });
   }

   public void show(Callback<Item.ItemType> callback, boolean withNil) {
      this.callback = callback;
      this.group.clearChildren();
      this.group.addActor(this.x);
      if (withNil) {
         this.group.addActor(this.nil);
      }

      for (Item.ItemType t : Game.G.getSeenResources()) {
         if (!t.categories.contains(Item.ItemCategory.Fluid)) {
            ImageButton ib = this.buttons.get(t);
            if (ib == null) {
               ib = new ImageButton(t.drawable);
               ib.getImageCell().size(40.0F);
               ib.setUserObject(t);
               ib.addListener(new TextTooltip(t.title.trim(), Quarry.Q.skin));
               ib.setSize(60.0F, 60.0F);
               ib.addListener(this.listener);
               this.buttons.put(t, ib);
            }

            this.group.addActor(ib);
         }
      }

      this.setVisible(true);
      ((ScrollPane)this.group.getParent()).addListener(new InputListener() {
         public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
            super.enter(event, x, y, pointer, fromActor);
            event.getStage().setScrollFocus(event.getTarget());
         }

         public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
            super.exit(event, x, y, pointer, toActor);
            event.getStage().setScrollFocus(toActor);
         }
      });
      ((ScrollPane)this.group.getParent()).setScrollX(0.0F);
      ((ScrollPane)this.group.getParent()).updateVisualScroll();
      ((ScrollPane)this.group.getParent()).invalidateHierarchy();
      ((ScrollPane)this.group.getParent()).validate();
      this.chevronRight.setVisible(((ScrollPane)this.group.getParent()).getMaxX() >= ((ScrollPane)this.group.getParent()).getWidth());
   }

   public void hide() {
      this.setVisible(false);
   }
}
