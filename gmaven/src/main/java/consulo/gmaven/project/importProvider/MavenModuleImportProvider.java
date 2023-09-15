package consulo.gmaven.project.importProvider;

import com.intellij.java.impl.externalSystem.JavaProjectData;
import com.intellij.java.language.projectRoots.JavaSdk;
import com.intellij.java.language.projectRoots.JavaSdkVersion;
import consulo.annotation.component.ExtensionImpl;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.service.project.ProjectData;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.gmaven.Constants;
import consulo.gmaven.util.MavenUtils;
import consulo.ide.impl.externalSystem.service.module.wizard.AbstractExternalModuleImportProvider;
import consulo.ide.impl.externalSystem.service.module.wizard.ExternalModuleImportContext;
import consulo.ide.impl.idea.openapi.externalSystem.service.project.manage.ProjectDataManager;
import consulo.maven.icon.MavenIconGroup;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.LocalFileSystem;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.inject.Inject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

import static consulo.gmaven.Constants.SYSTEM_ID;

@ExtensionImpl
public class MavenModuleImportProvider extends AbstractExternalModuleImportProvider<ImportMavenControl> {
    @Nonnull
    public static MavenModuleImportProvider getInstance() {
        return EP_NAME.findExtensionOrFail(MavenModuleImportProvider.class);
    }

    @Inject
    public MavenModuleImportProvider(@Nonnull ProjectDataManager dataManager) {
        super(dataManager, new ImportMavenControl(), SYSTEM_ID);
    }

    @Nonnull
    @Override
    public String getName() {
        return SYSTEM_ID.getDisplayName().getValue();
    }

    @Nonnull
    @Override
    public Image getIcon() {
        return MavenIconGroup.mavenlogo();
    }

    @Override
    public boolean canImport(@Nonnull File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            return new File(fileOrDirectory, Constants.POM_XML).exists();
        } else {
            return MavenUtils.isPomFileName(fileOrDirectory.getName())
                    || MavenUtils.isPotentialPomFile(fileOrDirectory.getName());
        }
    }

    @Nonnull
    @Override
    public String getFileSample() {
        return "<b>Maven</b> build script (*.xml)";
    }

    @Override
    protected void doPrepare(@Nonnull ExternalModuleImportContext<ImportMavenControl> context) {
        String importFile = context.getFileToImport();
        VirtualFile file = LocalFileSystem.getInstance().refreshAndFindFileByPath(importFile);
        if (file != null && !file.isDirectory()) {
            //getControl().setLinkedProjectPath(file.getParent().getPath());
        }
    }

    @Override
    protected void beforeCommit(@Nonnull DataNode<ProjectData> dataNode, @Nonnull Project project) {

    }

    @Nonnull
    @Override
    protected File getExternalProjectConfigToUse(@Nonnull File file) {
        return file.isDirectory() ? file : file.getParentFile();
    }

    @Override
    protected void applyExtraSettings(@Nonnull ExternalModuleImportContext<ImportMavenControl> context) {
        DataNode<ProjectData> node = getExternalProjectNode();
        if (node == null) {
            return;
        }

        DataNode<JavaProjectData> javaProjectNode = ExternalSystemApiUtil.find(node, JavaProjectData.KEY);
        if (javaProjectNode != null) {
            JavaProjectData data = javaProjectNode.getData();
            // todo context.setCompilerOutputDirectory(data.getCompileOutputPath());
            JavaSdkVersion version = data.getJdkVersion();
            Sdk jdk = findJdk(version);
            if (jdk != null) {
                //context.setProjectJdk(jdk);
            }
        }
    }

    @Nullable
    private static Sdk findJdk(@Nonnull JavaSdkVersion version) {
        JavaSdk javaSdk = JavaSdk.getInstance();
        List<Sdk> javaSdks = SdkTable.getInstance().getSdksOfType(javaSdk);
        Sdk candidate = null;
        for (Sdk sdk : javaSdks) {
            JavaSdkVersion v = javaSdk.getVersion(sdk);
            if (v == version) {
                return sdk;
            } else if (candidate == null && v != null && version.getMaxLanguageLevel().isAtLeast(version.getMaxLanguageLevel())) {
                candidate = sdk;
            }
        }
        return candidate;
    }
}


