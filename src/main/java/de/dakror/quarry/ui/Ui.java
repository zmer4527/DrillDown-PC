package de.dakror.quarry.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Window;

public interface Ui {
   void show(Window var1);

   void hide(Window var1);

   Stage getStage();
}
