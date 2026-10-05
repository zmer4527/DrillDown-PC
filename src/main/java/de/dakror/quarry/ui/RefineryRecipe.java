package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.GameUi;

public class RefineryRecipe extends Table {
   public RefineryRecipe(Skin skin) {
      this.setBackground(skin.getDrawable("panel_metal"));
      this.pad(20.0F, 14.0F, 20.0F, 14.0F);
      Table left = new Table();
      left.defaults();
      left.add(
            new IconLabel(32, skin, Item.ItemType.CrudeOil.drawable, GameUi.formatResourceAmount(10000.0F / 1000.0F) + "L")
               .tooltip(skin, GameUi.formatResourceAmount(10000.0F / 1000.0F) + "L " + Item.ItemType.CrudeOil.title)
         )
         .expandX()
         .center();
      left.row();
      Table t = new Table();
      Image arrow = new Image(skin.getDrawable("symb_rightarrow"));
      arrow.setOrigin(12.0F, 7.0F);
      arrow.setRotation(-90.0F);
      t.add(arrow).size(24.0F, 15.0F).padRight(10.0F);
      t.add(new IconLabel(25, skin, skin.getDrawable("icon_time"), GameUi.formatResourceAmount(2.0F) + "s", "small-font"));
      left.add(t);
      left.row();
      Table outputs = new Table();
      outputs.defaults().left();
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.IntermediateOilToColumn.drawable, GameUi.formatResourceAmount(7500.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(7500.0F / 1000.0F) + "L " + Item.ItemType.IntermediateOilToColumn.title)
      );
      left.add(outputs).center();
      this.add(left).grow();
      this.add(GameUi.createResourceTable(25, skin, skin.getDrawable("icon_power"), GameUi.formatPowerAmount((double)(600.0F * 60.0F)) + "/s", "small-font"))
         .pad(0.0F, 5.0F, 0.0F, 5.0F);
      Table right = new Table();
      right.defaults();
      right.add(
            new IconLabel(32, skin, Item.ItemType.IntermediateOilToRefinery.drawable, GameUi.formatResourceAmount(24000.0F / 1000.0F) + "L")
               .tooltip(skin, GameUi.formatResourceAmount(24000.0F / 1000.0F) + "L " + Item.ItemType.IntermediateOilToRefinery.title)
         )
         .expandX()
         .center();
      right.row();
      t = new Table();
      arrow = new Image(skin.getDrawable("symb_rightarrow"));
      arrow.setOrigin(12.0F, 7.0F);
      arrow.setRotation(-90.0F);
      t.add(arrow).size(24.0F, 15.0F).padRight(10.0F);
      t.add(new IconLabel(25, skin, skin.getDrawable("icon_time"), GameUi.formatResourceAmount(5.0F) + "s", "small-font"));
      right.add(t);
      right.row();
      outputs = new Table();
      outputs.defaults().left();
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.RefinedOil.drawable, GameUi.formatResourceAmount(18000.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(18000.0F / 1000.0F) + "L " + Item.ItemType.RefinedOil.title)
      );
      right.add(outputs).center();
      this.add(right).grow();
   }
}
