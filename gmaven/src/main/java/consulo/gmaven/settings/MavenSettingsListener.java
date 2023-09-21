package consulo.gmaven.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Set;

@TopicAPI(ComponentScope.PROJECT)
public interface MavenSettingsListener extends ExternalSystemSettingsListener<MavenProjectSettings> {

  Class<MavenSettingsListener> TOPIC = MavenSettingsListener.class;

  void onProjectRenamed(@Nonnull String s, @Nonnull String s1);

  void onProjectsLinked(@Nonnull Collection<MavenProjectSettings> collection);

  void onProjectsUnlinked(@Nonnull Set<String> set);

  void onUseAutoImportChange(boolean b, @Nonnull String s);

  void onBulkChangeStart();

  void onBulkChangeEnd();
}
