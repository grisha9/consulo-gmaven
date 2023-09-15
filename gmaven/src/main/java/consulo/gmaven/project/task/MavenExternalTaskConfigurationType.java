package consulo.gmaven.project.task;

import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.service.execution.AbstractExternalSystemTaskConfigurationType;

import javax.annotation.Nonnull;

import static consulo.gmaven.Constants.SYSTEM_ID;

@ExtensionImpl
public class MavenExternalTaskConfigurationType extends AbstractExternalSystemTaskConfigurationType {
  @Nonnull
  public static MavenExternalTaskConfigurationType getInstance() {
    return EP_NAME.findExtensionOrFail(MavenExternalTaskConfigurationType.class);
  }

  public MavenExternalTaskConfigurationType() {
    super(SYSTEM_ID);
  }
}
