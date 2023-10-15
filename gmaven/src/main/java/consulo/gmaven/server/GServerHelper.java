package consulo.gmaven.server;

import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.gmaven.MavenLog;
import consulo.gmaven.api.GServerUtils;
import consulo.gmaven.api.model.MavenException;
import consulo.gmaven.api.model.MavenResult;
import consulo.gmaven.api.model.request.GetModelRequest;
import consulo.gmaven.settings.OutputLevelType;
import consulo.gmaven.settings.ProfileExecution;
import consulo.gmaven.settings.ProjectExecution;
import consulo.gmaven.settings.SnapshotUpdateType;
import consulo.ide.impl.idea.util.PathUtil;
import consulo.process.cmd.ParametersListUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GServerHelper {

    @Nonnull
    public static MavenResult getProjectModel(
            @Nonnull GServerRequest request,
            @Nullable Consumer<GServerRemoteProcessSupport> processConsumer
    ) {
        var modelRequest = getModelRequest(request);
        var processSupport = new GServerRemoteProcessSupport(request);
        if (processConsumer != null) processConsumer.accept(processSupport);

        var mavenResult = runMavenTask(processSupport, modelRequest);
        if (tryInstallGMavenPlugin(request, mavenResult)) {
            firstRun(request);
            processSupport = new GServerRemoteProcessSupport(request);
            if (processConsumer != null) processConsumer.accept(processSupport);
            return runMavenTask(processSupport, modelRequest);
        }
        return mavenResult;
    }

    @Nonnull
    public static MavenResult runTasks(
            @Nonnull GServerRequest request,
            @Nonnull List<String> tasks,
            @Nullable Consumer<GServerRemoteProcessSupport> processConsumer
    ) {
        if (tasks.isEmpty()) {
            throw new ExternalSystemException("tasks list is empty");
        }
        if (request.installGMavenPlugin) {
            throw new ExternalSystemException("no need install gmaven read model plugin on task execution");
        }
        var modelRequest = getModelRequest(request);
        modelRequest.tasks = tasks;
        modelRequest.importArguments = null;
        var processSupport = new GServerRemoteProcessSupport(request);
        if (processConsumer != null) processConsumer.accept(processSupport);
        return runMavenTask(processSupport, modelRequest);
    }

    private static GetModelRequest getModelRequest(GServerRequest request) {
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
            var clazz = getMavenModelReaderPluginClass();
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
    private static Class<?> getMavenModelReaderPluginClass() {
        try {
            return Class.forName("ru.rzn.gmyasoedov.model.reader.DependencyCoordinate");
        } catch (ClassNotFoundException e) {
            MavenLog.LOG.error(e);
            return null;
        }
    }

    private static boolean tryInstallGMavenPlugin(@Nonnull GServerRequest request,
                                                  @Nonnull MavenResult mavenResult) {
        return !request.installGMavenPlugin && mavenResult.pluginNotResolved;
    }

    @Nonnull
    public static MavenResult firstRun(@Nonnull GServerRequest gServerRequest) {
        var request = new GServerRequest(
                gServerRequest.taskId,
                gServerRequest.projectPath,
                gServerRequest.mavenPath,
                gServerRequest.sdk,
                gServerRequest.settings,
                null
        );
        request.installGMavenPlugin = true;
        var modelRequest = getModelRequest(request);
        modelRequest.importArguments = null;
        var processSupport = new GServerRemoteProcessSupport(request);
        return runMavenTask(processSupport, modelRequest);
    }

    @Nonnull
    private static MavenResult runMavenTask(@Nonnull GServerRemoteProcessSupport processSupport,
                                            @Nonnull GetModelRequest modelRequest
    ) {
        var mavenResult = runMavenTaskInner(processSupport, modelRequest);
        processExceptions(mavenResult.exceptions);
        return mavenResult;
    }

    @Nonnull
    private static MavenResult runMavenTaskInner(
            @Nonnull GServerRemoteProcessSupport processSupport,
            @Nonnull GetModelRequest modelRequest
    ) {
        try {
            var projectModel = processSupport.acquire(processSupport.getId(), "").getProjectModel(modelRequest);
            return GServerUtils.toResult(projectModel);
        } catch (Exception e) {
            MavenLog.LOG.error(e);
            return GServerUtils.toResult(e);
        } finally {
            processSupport.stopAll();
        }
    }

    private static void processExceptions(@Nonnull List<MavenException> exceptions) {
        if (exceptions.isEmpty()) return;
        String errorString = exceptions.stream()
                .map(it -> it.message)
                .collect(Collectors.joining(System.lineSeparator()));
        throw new ExternalSystemException(errorString);
    }
}
