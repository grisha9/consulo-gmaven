package consulo.gmaven.externalSystem.service;

import consulo.annotation.component.ExtensionImpl;
import consulo.gmaven.Constants;
import consulo.ide.impl.externalSystem.module.extension.impl.ExternalSystemModuleExtensionImpl;
import consulo.ide.impl.externalSystem.module.extension.impl.ExternalSystemMutableModuleExtensionImpl;
import consulo.localize.LocalizeValue;
import consulo.maven.icon.MavenIconGroup;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ExtensionImpl
public class MavenModuleExtensionProvider implements ModuleExtensionProvider<ExternalSystemModuleExtensionImpl> {
  @Nonnull
  @Override
  public String getId() {
    return Constants.SYSTEM_ID.getId();
  }

  @Nullable
  @Override
  public String getParentId() {
    return "java";
  }

  @Override
  public boolean isSystemOnly() {
    return true;
  }

  @Nonnull
  @Override
  public LocalizeValue getName() {
    return Constants.SYSTEM_ID.getDisplayName();
  }

  @Nonnull
  @Override
  public Image getIcon() {
    return MavenIconGroup.mavenlogo();
  }

  @Nonnull
  @Override
  public ModuleExtension<ExternalSystemModuleExtensionImpl> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
    return new ExternalSystemModuleExtensionImpl(getId(), moduleRootLayer);
  }

  @Nonnull
  @Override
  public MutableModuleExtension<ExternalSystemModuleExtensionImpl> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer) {
    return new ExternalSystemMutableModuleExtensionImpl(getId(), moduleRootLayer);
  }
}
