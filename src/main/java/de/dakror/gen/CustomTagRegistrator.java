package de.dakror.gen;

import com.github.czyzby.lml.util.LmlParserBuilder;
import de.dakror.common.libgdx.lml.CustomTagProvider;
import de.dakror.quarry.ui.Alert;
import de.dakror.quarry.ui.Confirm;
import de.dakror.quarry.ui.LayerSelection;
import de.dakror.quarry.ui.Prompt;
import de.dakror.quarry.ui.SeedPrompt;
import de.dakror.quarry.ui.TileUI;
import de.dakror.quarry.ui.Toast;
import de.dakror.quarry.ui.Upgrade;
import de.dakror.quarry.ui.VolumeButton;

public class CustomTagRegistrator {
   public static void addCustomTags(LmlParserBuilder lml) {
      lml.tag(new CustomTagProvider(Confirm.class), new String[]{"confirm"});
      lml.tag(new CustomTagProvider(TileUI.class), new String[]{"tileui"});
      lml.tag(new CustomTagProvider(VolumeButton.class), new String[]{"volumebutton"});
      lml.tag(new CustomTagProvider(Toast.class), new String[]{"toast"});
      lml.tag(new CustomTagProvider(SeedPrompt.class), new String[]{"seedprompt"});
      lml.tag(new CustomTagProvider(Upgrade.class), new String[]{"upgrade"});
      lml.tag(new CustomTagProvider(LayerSelection.class), new String[]{"layerselection"});
      lml.tag(new CustomTagProvider(Prompt.class), new String[]{"prompt"});
      lml.tag(new CustomTagProvider(Alert.class), new String[]{"alert"});
   }
}
