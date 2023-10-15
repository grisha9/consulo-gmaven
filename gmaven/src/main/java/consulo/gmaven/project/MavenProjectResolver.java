package consulo.gmaven.project;

import com.intellij.java.impl.externalSystem.JavaProjectData;
import com.intellij.java.language.LanguageLevel;
import consulo.application.Application;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.gmaven.api.model.MavenResult;
import consulo.gmaven.externalSystem.model.ProfileData;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.gmaven.server.GServerHelper;
import consulo.gmaven.server.GServerRemoteProcessSupport;
import consulo.gmaven.server.GServerRequest;
import consulo.gmaven.settings.MavenExecutionSettings;
import consulo.java.execution.impl.util.JreSearchUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static consulo.gmaven.Constants.SYSTEM_ID;
import static consulo.gmaven.util.MavenUtils.getMavenHome;
import static java.util.Objects.requireNonNullElse;

public class MavenProjectResolver implements ExternalSystemProjectResolver<MavenExecutionSettings> {
    private final Map<ExternalSystemTaskId, GServerRemoteProcessSupport> cancellationMap = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public DataNode<ProjectData> resolveProjectInfo(
            @Nonnull ExternalSystemTaskId id,
            @Nonnull String projectPath,
            boolean isPreviewMode,
            @Nullable MavenExecutionSettings settings,
            @Nonnull ExternalSystemTaskNotificationListener listener)
            throws ExternalSystemException, IllegalArgumentException, IllegalStateException {
        if (settings == null) throw new ExternalSystemException("settings is empty");
        var sdk = JreSearchUtil.findSdkOfLevel(
                Application.get().getInstance(SdkTable.class), LanguageLevel.JDK_1_8, settings.getJdkName()
        );
        if (isPreviewMode) {
            return getPreviewProjectDataNode(settings, id, projectPath, sdk, listener);
        }

        if (sdk == null) throw new ExternalSystemException("JDK not found " + settings.getJdkName());
        var mavenHome = getMavenHome(settings.getDistributionSettings());
        var buildPath = Path.of(requireNonNullElse(settings.getExecutionWorkspace().getProjectBuildFile(), projectPath));
        var request = new GServerRequest(id, buildPath, mavenHome, sdk, settings, listener);
        try {
            var projectModel = GServerHelper.getProjectModel(request, (process -> cancellationMap.put(id, process)));
            return getProjectDataNode(projectPath, projectModel, settings);
        } finally {
            cancellationMap.remove(id);
        }
    }

    @Override
    public boolean cancelTask(@Nonnull ExternalSystemTaskId externalSystemTaskId,
                              @Nonnull ExternalSystemTaskNotificationListener listener) {
        GServerRemoteProcessSupport processSupport = cancellationMap.get(externalSystemTaskId);
        if (processSupport != null) processSupport.stopAll();
        return true;
    }

    private DataNode<ProjectData> getPreviewProjectDataNode(
            MavenExecutionSettings settings,
            ExternalSystemTaskId id,
            String projectPath,
            Sdk sdk,
            ExternalSystemTaskNotificationListener listener
    ) {
        var projectDataNode = getPreviewProjectDataNode(projectPath);
        var distributionPath = settings.getDistributionSettings().getPath();
        if (sdk != null && distributionPath != null) {
            GServerHelper.firstRun(
                    new GServerRequest(id, Path.of(projectPath), Path.of(distributionPath), sdk, settings, listener)
            );
        }
        return projectDataNode;
    }

    private DataNode<ProjectData> getPreviewProjectDataNode(
            String projectPath
    ) {
        var projectDirectory = getProjectDirectory(projectPath).toAbsolutePath().toString();
        var projectName = new File(projectDirectory).getName();
        var projectData = new ProjectData(SYSTEM_ID, projectName, projectDirectory, projectDirectory);
        var projectDataNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        projectDataNode.createChild(
                ProjectKeys.MODULE,
                new ModuleData(projectName, SYSTEM_ID, projectName, projectDirectory, projectDirectory)
        );
        return projectDataNode;
    }

    private Path getProjectDirectory(String projectPath) {
        var projectNioPath = Path.of(projectPath);
        return Files.isDirectory(projectNioPath) ? projectNioPath : projectNioPath.getParent();
    }

    @Nonnull
    private DataNode<ProjectData> getProjectDataNode(
            @Nonnull String projectPath, @Nonnull MavenResult mavenResult, @Nonnull MavenExecutionSettings settings
    ) {
        var container = mavenResult.projectContainer;
        var project = container.getProject();
        var projectName = project.getDisplayName();
        var absolutePath = project.getBasedir();
        var projectData = new ProjectData(SYSTEM_ID, projectName, absolutePath, absolutePath);

        var projectDataNode = new DataNode<>(ProjectKeys.PROJECT, projectData, null);

        var sdkName = settings.getJdkName();
        var languageLevel = LanguageLevel.parse(sdkName);
        var javaProjectData = new JavaProjectData(SYSTEM_ID, project.getOutputDirectory());
        projectDataNode.createChild(JavaProjectData.KEY, javaProjectData);


        var context = new ProjectResolverContext();
        context.mavenResult = mavenResult;
        context.settings = settings;
        context.rootProjectPath = absolutePath;
        context.languageLevel = languageLevel;

        var moduleNode = ModuleDataConverter.createModuleData(container, projectDataNode, context);

        for (var childContainer : container.getModules()) {
            ModuleDataConverter.createModuleData(childContainer, moduleNode, context);
        }
        DependencyDataConverter.addDependencies(container, projectDataNode, context);
        populateProfiles(projectDataNode, context.mavenResult.settings);
        //moduleNode.data.setProperty(GMavenConstants.MODULE_PROP_LOCAL_REPO, mavenResult.settings.localRepository)
        return projectDataNode;
    }

    private void populateProfiles(@Nonnull DataNode<ProjectData> dataNode,
                                  @Nonnull consulo.gmaven.api.model.MavenSettings settings) {
        String projectPath = dataNode.getData().getLinkedExternalProjectPath();
        for (var profile : settings.profiles) {
            dataNode.createChild(
                    ProfileData.KEY,
                    new ProfileData(SYSTEM_ID, projectPath, profile.name, profile.activation));
        }
    }
}
