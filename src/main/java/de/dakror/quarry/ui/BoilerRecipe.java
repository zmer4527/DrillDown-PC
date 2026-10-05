package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.scenes.GameUi;

public class BoilerRecipe extends Table {
   public BoilerRecipe(Skin skin) {
      this.setBackground(skin.getDrawable("panel_metal"));
      this.pad(20.0F, 14.0F, 20.0F, 14.0F);
      Table inputs = new Table();
      inputs.defaults().left().padLeft(5.0F);
      inputs.add(
            new IconLabel(32, skin, Item.ItemType.Water.drawable, GameUi.formatResourceAmount(3000.0F / 1000.0F) + "L")
               .tooltip(skin, GameUi.formatResourceAmount(3000.0F / 1000.0F) + "L " + Item.ItemType.Water.title)
         )
         .expandX()
         .center();
      inputs.add(
            new IconLabel(32, skin, Item.ItemCategory.CoalFuel.drawable, "1/" + GameUi.formatResourceAmount(150.0F / (5.0F / 0.3F)) + "s")
               .tooltip(skin, Item.ItemCategory.CoalFuel.title)
         )
         .expandX()
         .center();
      this.add(inputs).center();
      this.row();
      Table t = new Table();
      Image arrow = new Image(skin.getDrawable("symb_rightarrow"));
      arrow.setOrigin(12.0F, 7.0F);
      arrow.setRotation(-90.0F);
      t.add(arrow).size(24.0F, 15.0F).pad(10.0F);
      t.add(new IconLabel(25, skin, skin.getDrawable("icon_time"), GameUi.formatResourceAmount(1.0F) + "s", "small-font"));
      this.add(t);
      this.row();
      Table outputs = new Table();
      outputs.defaults().left();
      outputs.add(
         new IconLabel(32, skin, Item.ItemType.PressurizedSteam.drawable, GameUi.formatResourceAmount(5000.0F / 1000.0F) + "L")
            .tooltip(skin, GameUi.formatResourceAmount(5000.0F / 1000.0F) + "L " + Item.ItemType.PressurizedSteam.title)
      );
      this.add(outputs).center();
   }
}
