package de.dakror.common.libgdx.lml;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TableLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

public class CustomTagProvider implements LmlTagProvider {
   private Class<? extends Actor> clazz;

   public CustomTagProvider(Class<? extends Actor> clazz) {
      this.clazz = clazz;
   }

   public com.github.czyzby.lml.parser.tag.LmlTag create(LmlParser parser, com.github.czyzby.lml.parser.tag.LmlTag parentTag, StringBuilder rawTagData) {
      return new TableLmlTag(parser, parentTag, rawTagData) {
         protected Actor getNewInstanceOfActor(LmlActorBuilder builder) {
            try {
               return CustomTagProvider.this.clazz.getConstructor(Skin.class, String.class).newInstance(this.getSkin(builder), builder.getStyleName());
            } catch (Exception var3) {
               var3.printStackTrace();
               return null;
            }
         }
      };
   }
}
