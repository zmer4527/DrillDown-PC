package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;
import de.dakror.quarry.Const;
import de.dakror.quarry.Quarry;
import de.dakror.quarry.game.Tile;
import de.dakror.quarry.scenes.Game;
import de.dakror.quarry.util.Util;

@LmlTag(
   tagName = "tileui"
)
public class TileUI extends Table implements CustomTag {
   Label label;
   final String dust = Quarry.Q.i18n.get("tile.dust");

   public TileUI(Skin skin, String style) {
      super(skin);
      this.setBackground(skin.getDrawable("panel_boltsBlue"));
   }

   @Override
   public void postInit() {
      this.label = Util.id("label");
      this.setPosition((Const.UI_W - this.getWidth()) / 2.0F, 130.0F);
      this.setVisible(false);
   }

   public void setText(Tile.TileType t) {
      this.label.setText(t == null ? this.dust : t.name);
   }

   public void show() {
      this.setVisible(true);
   }

   public void hide() {
      Game.G.resetActiveTile();
      this.setVisible(false);
   }
}
