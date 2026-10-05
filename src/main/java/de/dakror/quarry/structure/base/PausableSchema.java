package de.dakror.quarry.structure.base;

import de.dakror.common.BiCallback;
import de.dakror.quarry.game.Item;
import de.dakror.quarry.util.Sfx;

public class PausableSchema extends Schema {
   public PausableSchema(int version, StructureType type, boolean clickable, int width, int height, String tex, Item.Items buildCosts, Sfx sfx, Dock... docks) {
      super(version, type, clickable, width, height, tex, buildCosts, sfx, docks);
      this.button(new Schema.ButtonDef("state_sleeping", "button.sleep", Schema.ButtonDef.ButtonType.StateToggle, new BiCallback<Boolean, Structure<?>>() {
         public void call(Boolean on, Structure<?> data) {
            ((PausableStructure)data).setSleeping(on);
         }
      }));
   }
}
