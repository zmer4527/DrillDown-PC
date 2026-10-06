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

   /**
    * 清空未存档的瞬态历史（倒计时、滑动窗口等）。联机发世界前主机会调用，
    * 让序列化出的世界与"客机全新加载"得到的状态完全一致（否则两端起点不同会分叉）。
    */
   public void clearTransientState() {
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
