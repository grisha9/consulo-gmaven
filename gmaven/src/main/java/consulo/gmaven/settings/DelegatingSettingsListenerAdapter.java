package consulo.gmaven.settings;

import consulo.externalSystem.setting.ExternalSystemSettingsListener;
import consulo.ide.impl.idea.openapi.externalSystem.settings.DelegatingExternalSystemSettingsListener;

import javax.annotation.Nonnull;

public class DelegatingSettingsListenerAdapter extends DelegatingExternalSystemSettingsListener<MavenProjectSettings>
        implements MavenSettingsListener {

    public DelegatingSettingsListenerAdapter(@Nonnull ExternalSystemSettingsListener<MavenProjectSettings> delegate) {
        super(delegate);
    }
}
