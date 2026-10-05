package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.GameUi;

public class DistillationRecipe extends Table {
   public DistillationRecipe(Skin skin) {
      this.setBackground(skin.getDrawable("panel_metal"));
      this.pad(20.0F, 14.0F, 20.0F, 14.0F);
      Table inputs = new Table();
      inputs.defaults().left();
      inputs.add(
            new IconLabel(32, skin, Item.ItemType.IntermediateOilToColumn.drawable, GameUi.formatResourceAmount(1000.0F / 1000.0F) + "L")
               .tooltip(skin, GameUi.formatResourceAmount(1000.0F / 1000.0F) + "L " + Item.ItemType.IntermediateOilToColumn.title)
         )
         .expandX()
         .center();
      this.add(inputs).center();
      this.row();
      Table t = new Table();
      t.add(GameUi.createResourceTable(25, skin, skin.getDrawable("icon_power"), GameUi.formatPowerAmount((double)(84.0F * 60.0F)) + "/s", "small-font"));
      Image arrow = new Image(skin.getDrawable("symb_rightarrow"));
      arrow.setOrigin(12.0F, 7.0F);
      arrow.setRotation(-90.0F);
      t.add(arrow).size(24.0F, 15.0F).pad(10.0F);
      t.add(new IconLabel(25, skin, skin.getDrawable("icon_time"), GameUi.formatResourceAmount(2.0F) + "s", "small-font"));
      this.add(t);
      this.row();
      Table outputs = new Table();
      outputs.defaults().left().space(10.0F);
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.IntermediateOilToRefinery.drawable, GameUi.formatResourceAmount(600.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(600.0F / 1000.0F) + "L " + Item.ItemType.IntermediateOilToRefinery.title)
      );
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.Lubricant.drawable, GameUi.formatResourceAmount(200.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(200.0F / 1000.0F) + "L " + Item.ItemType.Lubricant.title)
      );
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.NaturalGas.drawable, GameUi.formatResourceAmount(50.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(50.0F / 1000.0F) + "L " + Item.ItemType.NaturalGas.title)
      );
      this.add(outputs).center().grow();
   }
}
