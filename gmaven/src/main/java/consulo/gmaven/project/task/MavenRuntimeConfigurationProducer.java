package consulo.gmaven.project.task;

import consulo.annotation.component.ExtensionImpl;
import consulo.ide.impl.idea.openapi.externalSystem.service.execution.AbstractExternalSystemRuntimeConfigurationProducer;

@ExtensionImpl
public class MavenRuntimeConfigurationProducer extends AbstractExternalSystemRuntimeConfigurationProducer {
  public MavenRuntimeConfigurationProducer() {
    super(MavenExternalTaskConfigurationType.getInstance());
  }
}
