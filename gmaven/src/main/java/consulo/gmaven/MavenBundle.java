package consulo.gmaven;

import consulo.component.util.localize.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

import javax.annotation.Nonnull;

public class MavenBundle extends AbstractBundle {

  public static String message(@Nonnull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) String key, @Nonnull Object... params) {
    return BUNDLE.getMessage(key, params);
  }

  public static final String PATH_TO_BUNDLE = "i18n.MavenBundle";
  private static final MavenBundle BUNDLE = new MavenBundle();

  public MavenBundle() {
    super(PATH_TO_BUNDLE);
  }
}
