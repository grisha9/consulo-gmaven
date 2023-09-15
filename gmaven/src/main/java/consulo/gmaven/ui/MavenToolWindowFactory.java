package consulo.gmaven.ui;

import consulo.annotation.component.ExtensionImpl;
import consulo.ide.impl.idea.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory;
import consulo.maven.icon.MavenIconGroup;
import consulo.ui.ex.toolWindow.ToolWindowAnchor;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;

import static consulo.gmaven.Constants.SYSTEM_ID;


@ExtensionImpl
public class MavenToolWindowFactory extends AbstractExternalSystemToolWindowFactory {
    public MavenToolWindowFactory() {
        super(SYSTEM_ID);
    }

    @Nonnull
    @Override
    public ToolWindowAnchor getAnchor() {
        return ToolWindowAnchor.RIGHT;
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return MavenIconGroup.toolwindowmaven();
    }
}
