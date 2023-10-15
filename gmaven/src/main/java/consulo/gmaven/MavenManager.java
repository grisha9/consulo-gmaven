package consulo.gmaven;

import com.intellij.java.language.LanguageLevel;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.Application;
import consulo.configurable.Configurable;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.ExternalSystemAutoImportAware;
import consulo.externalSystem.ExternalSystemConfigurableAware;
import consulo.externalSystem.ExternalSystemManager;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.task.ExternalSystemTaskManager;
import consulo.externalSystem.ui.ExternalSystemUiAware;
import consulo.externalSystem.util.ExternalSystemApiUtil;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.gmaven.project.AutoImportAware;
import consulo.gmaven.project.MavenProjectResolver;
import consulo.gmaven.project.task.MavenTaskManager;
import consulo.gmaven.settings.*;
import consulo.gmaven.util.MavenUtils;
import consulo.ide.impl.idea.openapi.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware;
import consulo.java.execution.impl.util.JreSearchUtil;
import consulo.maven.icon.MavenIconGroup;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.ExecutionException;
import consulo.process.cmd.SimpleJavaParameters;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;

import static consulo.gmaven.Constants.SYSTEM_ID;

@ExtensionImpl
public class MavenManager implements
        ExternalSystemManager<MavenProjectSettings, MavenSettingsListener, MavenSettings, LocalSettings, MavenExecutionSettings>,
        ExternalSystemConfigurableAware,
        ExternalSystemAutoImportAware,
        ExternalSystemUiAware {

    @Nonnull
    private final ExternalSystemAutoImportAware autoImportAware = new CachingExternalSystemAutoImportAware(new AutoImportAware());

    public MavenManager() {
    }

    @Nonnull
    @Override
    public Configurable getConfigurable(@Nonnull Project project) {
        return new SettingsConfigurable(project);
    }

    @Nullable
    @Override
    public String getAffectedExternalProjectPath(@Nonnull String changedFileOrDirPath, @Nonnull Project project) {
        return autoImportAware.getAffectedExternalProjectPath(changedFileOrDirPath, project);
    }

    @Nonnull
    @Override
    public String getProjectRepresentationName(@Nonnull String targetProjectPath, @Nullable String rootProjectPath) {
        return ExternalSystemApiUtil.getProjectRepresentationName(targetProjectPath, rootProjectPath);
    }

    @Nullable
    @Override
    public FileChooserDescriptor getExternalProjectConfigDescriptor() {
        return MavenUtils.getProjectFileChooserDescriptor();
    }

    @Nullable
    @Override
    public Image getProjectIcon() {
        return MavenIconGroup.mavenlogo();
    }

    @Nullable
    @Override
    public Image getTaskIcon() {
        return PlatformIconGroup.nodesTask();
    }

    @Nonnull
    @Override
    public ProjectSystemId getSystemId() {
        return SYSTEM_ID;
    }

    @Nonnull
    @Override
    public Function<Project, MavenSettings> getSettingsProvider() {
        return MavenSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Project, LocalSettings> getLocalSettingsProvider() {
        return LocalSettings::getInstance;
    }

    @Nonnull
    @Override
    public Function<Pair<Project, String>, MavenExecutionSettings> getExecutionSettingsProvider() {
        return pair -> {
            Project project = pair.first;
            String projectPath = pair.second;
            MavenSettings settings = MavenSettings.getInstance(project);
            MavenProjectSettings projectSettings = settings.getLinkedProjectSettings(projectPath);
            return getExecutionSettings(project, projectPath, settings, projectSettings);
        };
    }

    @Nonnull
    @Override
    public Class<? extends ExternalSystemProjectResolver<MavenExecutionSettings>> getProjectResolverClass() {
        return MavenProjectResolver.class;
    }

    @Override
    public Class<? extends ExternalSystemTaskManager<MavenExecutionSettings>> getTaskManagerClass() {
        return MavenTaskManager.class;
    }

    @Nonnull
    @Override
    public FileChooserDescriptor getExternalProjectDescriptor() {
        return MavenUtils.getProjectFileChooserDescriptor();
    }

    @Override
    public void enhanceRemoteProcessing(@Nonnull SimpleJavaParameters simpleJavaParameters) throws ExecutionException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void enhanceLocalProcessing(@Nonnull List<URL> list) {

    }

    @Nonnull
    public static MavenExecutionSettings getExecutionSettings(
            @Nonnull Project project,
            @Nonnull String projectPath,
            @Nonnull MavenSettings settings,
            @Nullable MavenProjectSettings projectSettings) {
        MavenExecutionSettings result;
        if (projectSettings == null) {
            result = new MavenExecutionSettings(DistributionSettings.getBundled(), false, settings.isOfflineMode());
        } else {
            result = new MavenExecutionSettings(projectSettings.getDistributionSettings(),
                    projectSettings.isNonRecursive(), settings.isOfflineMode()
            );
            result.setNonRecursive(projectSettings.isNonRecursive());
            result.setSnapshotUpdateType(projectSettings.getSnapshotUpdateType());
            result.setThreadCount(projectSettings.getThreadCount());
            result.setOutputLevel(projectSettings.getOutputLevel());
            result.setShowPluginNodes(projectSettings.isShowPluginNodes());
            result.setVmOptions(projectSettings.getVmOptions());
            //fillExecutionWorkSpace(project, projectSettings, projectPath, result.getExecutionWorkspace());
            if (projectSettings.getArguments() != null) {
                result.setArguments(projectSettings.getArguments());
            }
            if (projectSettings.getArgumentsImport() != null) {
                result.setArgumentsImport(projectSettings.getArgumentsImport());
            }
        }

        String ideProjectPath;
        if (project.getBasePath() == null || StringUtil.endsWith(project.getProjectFilePath(), ".ipr")) {
            ideProjectPath = projectSettings != null ? projectSettings.getExternalProjectPath() : projectPath;
        } else {
            ideProjectPath = Paths.get(project.getBasePath(), ".idea", "modules").toString();
        }
        result.setIdeProjectPath(ideProjectPath);

        String jdkName = projectSettings != null ? projectSettings.getJdkName() : null;
        Sdk targetSdk = JreSearchUtil.findSdkOfLevel(
                Application.get().getInstance(SdkTable.class), LanguageLevel.JDK_1_8, jdkName
        );
        if (targetSdk != null) {
            MavenLog.LOG.info("Instructing gradle to use java from " + targetSdk.getHomePath());
        }
        result.setJavaHome(targetSdk == null ? null : targetSdk.getHomePath());

        if (settings.isSkipTests()) {
            result.addEnvParam("skipTests", "true");
        }
        return result;
    }

    private static void fillExecutionWorkSpace(Project project,
                                               MavenProjectSettings projectSettings,
                                               String projectPath,
                                               MavenExecutionWorkspace workspace) {
        /*ExternalProjectInfo projectData = ProjectDataManager.getInstance()
                .getExternalProjectData(project, SYSTEM_ID, projectSettings.getExternalProjectPath());

        if (projectData == null || projectData.getExternalProjectStructure() == null) return;
        ProjectProfilesStateService profilesStateService = ProjectProfilesStateService.getInstance(project);
        DataNode<ProjectData> projectDataNode = projectData.getExternalProjectStructure();
        DataNode<ModuleData> mainModuleNode = ExternalSystemApiUtil.find(projectDataNode, MODULE);
        if (mainModuleNode == null) return;
        if (projectSettings.getProjectBuildFile() != null) {
            workspace.setProjectBuildFile(projectSettings.getProjectBuildFile());
        } else {
            workspace.setProjectBuildFile(mainModuleNode.getData().getProperty(MODULE_PROP_BUILD_FILE));
        }

        Collection<DataNode<ModuleData>> allModules = ExternalSystemApiUtil.findAll(mainModuleNode, MODULE);

        boolean isRootPath = equalsPaths(projectSettings.getExternalProjectPath(), projectPath);
        if (!isRootPath) {
            allModules.stream()
                    .filter(node -> equalsPaths(node.getData().getLinkedExternalProjectPath(), projectPath))
                    .findFirst()
                    .ifPresent(node -> {
                        ModuleData module = node.getData();
                        workspace.setSubProjectBuildFile(module.getProperty(MODULE_PROP_BUILD_FILE));
                        //addedIgnoredModule(workspace, findAllRecursively(node, MODULE));
                        if (projectSettings.isUseWholeProjectContext()) {
                            String parentBuildFile = getParentBuildFile(node);
                            if (parentBuildFile != null) {
                                workspace.setProjectBuildFile(parentBuildFile);
                                if (equalsPaths(workspace.getProjectBuildFile(), workspace.getSubProjectBuildFile())) {
                                    workspace.setSubProjectBuildFile(null);
                                }
                            }
                            if (workspace.getSubProjectBuildFile() != null) {
                                workspace.addProject(new ProjectExecution(MavenUtils.toGAString(module), true));
                            }
                            workspace.setSubProjectBuildFile(null);
                        }
                    });
        } else {
            addedIgnoredModule(workspace, allModules);
        }*/

        /*for (DataNode<ProfileData> profileDataNode : findAll(projectDataNode, ProfileData.KEY)) {
            ProfileExecution profileExecution = profilesStateService.getProfileExecution(profileDataNode.getData());
            if (profileExecution != null) {
                workspace.addProfile(profileExecution);
            }
        }*/
    }

    /*@Nullable
    private static String getParentBuildFile(@Nonnull DataNode<ModuleData> node) {
        String parentGA = node.getData().getProperty(Constants.MODULE_PROP_PARENT_GA);
        if (parentGA != null && node.getParent() != null && node.getParent().getData() instanceof ModuleData) {
            return getParentBuildFile((DataNode<ModuleData>) node.getParent());
        }
        return node.getData().getProperty(MODULE_PROP_BUILD_FILE);
    }*/
}
