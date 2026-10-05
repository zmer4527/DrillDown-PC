package de.dakror.quarry.structure.base.component;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import de.dakror.common.libgdx.io.NBT;
import de.dakror.common.libgdx.render.SpriteRenderer;
import de.dakror.quarry.structure.base.Structure;
import de.dakror.quarry.util.Bounds;

public abstract class Component {
   protected Structure<?> structure;

   public abstract void init();

   public void onPlacement() {
   }

   public abstract void update(double var1, int var3, Bounds var4);

   public void postLoad() {
   }

   public void drawFrame(SpriteRenderer spriter, ShapeRenderer shaper) {
   }

   public abstract void saveData(NBT.Builder var1);

   public abstract void loadData(NBT.CompoundTag var1);

   public abstract Component clone();

   public void setStructure(Structure<?> structure) {
      this.structure = structure;
   }
}
