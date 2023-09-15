
package consulo.gmaven.settings;

import consulo.disposer.Disposable;
import consulo.externalSystem.service.execution.ExternalSystemSettingsControl;
import consulo.externalSystem.ui.awt.ExternalSystemUiUtil;
import consulo.externalSystem.ui.awt.PaintAwarePanel;
import consulo.gmaven.MavenBundle;
import consulo.ui.ex.awt.JBCheckBox;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class SystemSettingsControl implements ExternalSystemSettingsControl<MavenSettings> {

    @NotNull
    private final MavenSettings myInitialSettings;

    @Nullable
    private JBCheckBox offlineCheckBox;
    private JBCheckBox skipTestsCheckBox;

    public SystemSettingsControl(@Nonnull MavenSettings initialSettings) {
        myInitialSettings = initialSettings;
    }

    @Override
    public void fillUi(@Nonnull Disposable uiDisposable, @Nonnull PaintAwarePanel canvas, int indentLevel) {
        offlineCheckBox = new JBCheckBox(MavenBundle.message("gmaven.settings.system.offline"));
        skipTestsCheckBox = new JBCheckBox(MavenBundle.message("gmaven.settings.system.skip.tests"));

        canvas.add(offlineCheckBox, ExternalSystemUiUtil.getLabelConstraints(indentLevel));
        canvas.add(skipTestsCheckBox, ExternalSystemUiUtil.getFillLineConstraints(0));
    }

    @Override
    public void showUi(boolean show) {
        ExternalSystemUiUtil.showUi(this, show);
    }

    @Override
    public void reset() {
        if (offlineCheckBox != null) {
            offlineCheckBox.setSelected(myInitialSettings.isOfflineMode());
        }
        if (skipTestsCheckBox != null) {
            skipTestsCheckBox.setSelected(myInitialSettings.isSkipTests());
        }
    }

    @Override
    public boolean isModified() {
        if (offlineCheckBox != null && offlineCheckBox.isSelected() != myInitialSettings.isOfflineMode()) {
            return true;
        }
        if (skipTestsCheckBox != null && skipTestsCheckBox.isSelected() != myInitialSettings.isSkipTests()) {
            return true;
        }
        return false;
    }

    @Override
    public void apply(@Nonnull MavenSettings settings) {
        if (offlineCheckBox != null) {
            settings.setOfflineMode(offlineCheckBox.isSelected());
        }
        if (skipTestsCheckBox != null) {
            settings.setSkipTests(skipTestsCheckBox.isSelected());
        }
    }

    @Override
    public boolean validate(@Nonnull MavenSettings settings) {
        return true;
    }

    @Override
    public void disposeUIResources() {
        ExternalSystemUiUtil.disposeUi(this);
    }
}
