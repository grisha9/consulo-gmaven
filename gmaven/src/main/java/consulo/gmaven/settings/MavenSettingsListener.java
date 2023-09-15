package consulo.gmaven.settings;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.TopicAPI;
import consulo.externalSystem.setting.ExternalSystemSettingsListener;

@TopicAPI(ComponentScope.PROJECT)
public interface MavenSettingsListener extends ExternalSystemSettingsListener<MavenProjectSettings> {

  Class<MavenSettingsListener> TOPIC = MavenSettingsListener.class;

  void onBulkChangeStart();

  void onBulkChangeEnd();
}
