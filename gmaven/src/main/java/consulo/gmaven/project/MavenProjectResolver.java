package consulo.gmaven.project;

import com.intellij.java.impl.externalSystem.JavaProjectData;
import com.intellij.java.language.LanguageLevel;
import consulo.application.Application;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkTable;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.ExternalSystemTaskId;
import consulo.externalSystem.model.task.ExternalSystemTaskNotificationListener;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.externalSystem.service.project.ExternalSystemProjectResolver;
import consulo.externalSystem.service.project.ProjectData;
import consulo.gmaven.MavenLog;
import consulo.gmaven.api.GMavenServer;
import consulo.gmaven.api.model.request.GetModelRequest;
import consulo.gmaven.server.GServerRemoteProcessSupport;
import consulo.gmaven.server.GServerRequest;
import consulo.gmaven.settings.*;
import consulo.ide.impl.idea.util.PathUtil;
import consulo.java.execution.impl.util.JreSearchUtil;
import consulo.process.cmd.ParametersListUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

import static consulo.gmaven.Constants.SYSTEM_ID;

public class MavenProjectResolver implements ExternalSystemProjectResolver<MavenExecutionSettings> {
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
        if (isPreviewMode) {
            return getPreviewProjectDataNode(settings, id, projectPath, null, listener);
        }
        var buildPath = Path.of(
                Objects.requireNonNullElse(settings.getExecutionWorkspace().getProjectBuildFile(), projectPath)
        );
        var sdk = JreSearchUtil.findSdkOfLevel(
                Application.get().getInstance(SdkTable.class), LanguageLevel.JDK_1_8, settings.getJdkName()
        );
        var mavenHome = Path.of("/home/Grigoriy.Myasoedov/.sdkman/candidates/maven/3.8.5/");
        var request = new GServerRequest(id, buildPath, mavenHome, sdk, settings, listener);
        GServerRemoteProcessSupport processSupport = new GServerRemoteProcessSupport(request);
        try {
            System.out.println("!!!!!!!!!!!");
            GMavenServer acquire = processSupport.acquire(processSupport.getId(), "");
            System.out.println("zzzz " + acquire);
            var projectModel = acquire
                    .getProjectModel(getModelRequest(request));
            System.out.println("!!!!! " + projectModel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return getPreviewProjectDataNode(settings, id, projectPath, null, listener);
    }

    private GetModelRequest getModelRequest(GServerRequest request) {
        var projectPath = request.projectPath;
        var directory = projectPath.toFile().isDirectory();
        var projectDirectory = directory ? projectPath : projectPath.getParent();

        var modelRequest = new GetModelRequest();
        modelRequest.projectPath = projectDirectory.toString();
        modelRequest.alternativePom = directory ? null : projectPath.toString();
        modelRequest.nonRecursion = request.settings.isNonRecursive();
        modelRequest.updateSnapshots = request.settings.getSnapshotUpdateType() == SnapshotUpdateType.FORCE;
        modelRequest.notUpdateSnapshots = request.settings.getSnapshotUpdateType() == SnapshotUpdateType.NEVER;
        modelRequest.offline = request.settings.isOfflineWork();
        modelRequest.threadCount = request.settings.getThreadCount();
        modelRequest.quiteLogs = request.settings.getOutputLevel() == OutputLevelType.QUITE;
        modelRequest.debugLog = request.settings.getOutputLevel() == OutputLevelType.DEBUG;
        if (request.installGMavenPlugin) {
            var clazz = getaClass();
            if (clazz != null) {
                modelRequest.gMavenPluginPath = PathUtil.getJarPathForClass(clazz);
                modelRequest.nonRecursion = true;
            }
        }
        modelRequest.profiles = request.settings.getExecutionWorkspace().getProfilesData().stream()
                .map(ProfileExecution::toRawName)
                .collect(Collectors.joining(","));

        modelRequest.projectList = request.settings.getExecutionWorkspace().getProjectData().stream()
                .map(ProjectExecution::toRawName)
                .collect(Collectors.joining(","));

        if (request.settings.getArguments() != null) {
            modelRequest.additionalArguments = ParametersListUtil.parse(request.settings.getArguments(), true, true);
        }
        if (request.settings.getArgumentsImport() != null) {
            modelRequest.importArguments = ParametersListUtil.parse(request.settings.getArgumentsImport(), true, true);
        }
        return modelRequest;
    }

    @Nullable
    private static Class<?> getaClass() {
        try {
            return Class.forName("ru.rzn.gmyasoedov.model.reader.DependencyCoordinate");
        } catch (ClassNotFoundException e) {
            MavenLog.LOG.error(e);
            return null;
        }
    }

    @Override
    public boolean cancelTask(@Nonnull ExternalSystemTaskId externalSystemTaskId,
                              @Nonnull ExternalSystemTaskNotificationListener listener) {
        return false;
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
            //var buildPath = Path.of(settings.getExecutionWorkspace().getProjectBuildFile()) ?:projectPath)
            //firstRun(GServerRequest(id, buildPath, distributionPath, sdk, settings, listener = listener))
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

        DataNode<ModuleData> moduleDataDataNode = projectDataNode.createChild(
                ProjectKeys.MODULE,
                new ModuleData(projectName, SYSTEM_ID, projectName, projectDirectory, projectDirectory)
        );

        setupContentRoots(Path.of(projectDirectory), moduleDataDataNode);
        setupJdkData(projectDataNode, Path.of(projectDirectory));
        return projectDataNode;
    }

    private Path getProjectDirectory(String projectPath) {
        var projectNioPath = Path.of(projectPath);
        return Files.isDirectory(projectNioPath) ? projectNioPath : projectNioPath.getParent();
    }

    /*---------*/
    private void setupJdkData(

            DataNode<ProjectData> projectDataNode,
            Path mainModulePath
    ) {
        var javaProjectData = new JavaProjectData(
                SYSTEM_ID, mainModulePath.resolve("target").resolve("classes").toString()
        );
        projectDataNode.createChild(JavaProjectData.KEY, javaProjectData);
    }

    private void setupContentRoots(
            Path projectPath, DataNode<ModuleData> moduleDataNode
    ) {
        var rootData = new ContentRootData(SYSTEM_ID, projectPath.toString());
        rootData.storePath(ExternalSystemSourceType.EXCLUDED, projectPath.resolve("target").toString());
        rootData.storePath(ExternalSystemSourceType.SOURCE, projectPath.resolve("src").resolve("main").resolve("java").toString());
        rootData.storePath(ExternalSystemSourceType.RESOURCE, projectPath.resolve("src").resolve("main").resolve("resources").toString());
        rootData.storePath(ExternalSystemSourceType.TEST, projectPath.resolve("src").resolve("test").resolve("java").toString());
        rootData.storePath(ExternalSystemSourceType.TEST_RESOURCE, projectPath.resolve("src").resolve("test").resolve("resources").toString());

        moduleDataNode.createChild(ProjectKeys.CONTENT_ROOT, rootData);
    }
}
