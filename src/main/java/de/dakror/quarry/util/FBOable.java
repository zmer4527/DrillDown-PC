package de.dakror.quarry.util;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface FBOable {
   void draw(OrthographicCamera var1, OrthographicCamera var2, Viewport var3, Batch var4);
}
