package consulo.gmaven.server;


import consulo.content.bundle.Sdk;
import consulo.execution.DefaultExecutionResult;
import consulo.execution.ExecutionResult;
import consulo.execution.configuration.CommandLineState;
import consulo.execution.executor.Executor;
import consulo.execution.runner.ProgramRunner;
import consulo.gmaven.MavenLog;
import consulo.gmaven.api.GMavenServer;
import consulo.gmaven.extensionpoints.plugin.MavenFullImportPlugin;
import consulo.gmaven.settings.MavenExecutionSettings;
import consulo.ide.impl.idea.util.PathUtil;
import consulo.ide.impl.idea.util.net.NetUtils;
import consulo.java.execution.configurations.OwnJavaParameters;
import consulo.java.execution.projectRoots.OwnJdkUtil;
import consulo.process.ExecutionException;
import consulo.process.ProcessHandler;
import consulo.process.ProcessHandlerBuilder;
import consulo.process.cmd.GeneralCommandLine;
import consulo.process.cmd.ParametersList;
import consulo.process.cmd.ParametersListUtil;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static consulo.gmaven.api.GMavenServer.*;

public class MavenServerCmdState extends CommandLineState {

    public static final String REMOTE_GMAVEN_SERVER = "consulo.maven.server.RemoteGMavenServer";
    private final Sdk jdk;
    private final Path mavenPath;
    private final Path workingDirectory;
    private final List<String> jvmConfigOptions;
    private final MavenExecutionSettings executionSettings;
    private final Integer debugPort;

    public MavenServerCmdState(@Nonnull Sdk jdk,
                               @Nonnull Path mavenPath,
                               @Nonnull Path workingDirectory,
                               @Nonnull List<String> jvmConfigOptions,
                               @Nonnull MavenExecutionSettings executionSettings) {
        super(null);
        this.jdk = jdk;
        this.mavenPath = mavenPath;
        this.workingDirectory = workingDirectory;
        this.jvmConfigOptions = jvmConfigOptions;
        this.executionSettings = executionSettings;
        this.debugPort = getDebugPort();
    }

    protected OwnJavaParameters createJavaParameters() {
        final OwnJavaParameters params = new OwnJavaParameters();
        params.setJdk(jdk);
        params.setWorkingDirectory(workingDirectory.toFile());
        setupMavenOpts(params);
        setupDebugParam(params);
        setupClasspath(params);
        setupGmavenPluginsProperty(params);
        processVmOptions(jvmConfigOptions, params);
        params.setMainClass(REMOTE_GMAVEN_SERVER);
        return params;
    }

    private void setupGmavenPluginsProperty(OwnJavaParameters params) {
        List<MavenFullImportPlugin> extensionList = MavenFullImportPlugin.EP_NAME.getExtensionList();
        List<String> pluginsForImport = new ArrayList<>(extensionList.size());
        /*for (MavenFullImportPlugin plugin : extensionList) {
            pluginsForImport.add(plugin.getKey());
            String annotationPath = plugin instanceof MavenCompilerFullImportPlugin
                    ? ((MavenCompilerFullImportPlugin) plugin).getAnnotationProcessorTagName() : null;
            if (StringUtilRt.isEmpty(annotationPath)) continue;

            params.getVMParametersList()
                    .addProperty(format(GMAVEN_PLUGIN_ANNOTATION_PROCESSOR, plugin.getArtifactId()), annotationPath);
        }*/

        if (!pluginsForImport.isEmpty()) {
            params.getVMParametersList().addProperty(GMAVEN_PLUGINS, String.join(";", pluginsForImport));
        }
    }

    private void setupClasspath(OwnJavaParameters params) {
        String mavenServerJarPathString;
        String mavenExtClassesJarPathString;
        try {
            mavenServerJarPathString = PathUtil.getJarPathForClass(Class.forName(REMOTE_GMAVEN_SERVER));
            mavenExtClassesJarPathString = PathUtil
                    .getJarPathForClass(Class.forName("consulo.gmaven.event.handler.EventSpyResultHolder"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        String mavenServerApiJarPathString = PathUtil.getJarPathForClass(GMavenServer.class);
        params.getClassPath().add(mavenServerJarPathString);
        params.getClassPath().add(mavenServerApiJarPathString);
        params.getClassPath().add(getPlexusClassWorlds(mavenPath));
        params.getClassPath().addAll(collectIdeaRTLibraries());

        params.getVMParametersList().addProperty(MAVEN_EXT_CLASS_PATH_PROPERTY, mavenExtClassesJarPathString);
        params.getVMParametersList().addProperty(GMAVEN_HOME, mavenPath.toAbsolutePath().toString());
        executionSettings.getEnv().forEach((k, v) -> params.getVMParametersList().addProperty(k, v));
    }

    private void processVmOptions(List<String> jvmConfigOptions, OwnJavaParameters params) {
        List<String> vmOptions = new ArrayList<>(jvmConfigOptions);
        if (executionSettings.getVmOptions() != null) {
            vmOptions.addAll(ParametersListUtil.parse(executionSettings.getVmOptions(), true, true));
        }
        for (String param : vmOptions) {
            if (param.startsWith("-javaagent")) {
                continue;
            }
            params.getVMParametersList().add(param);
        }
    }

    private void setupDebugParam(OwnJavaParameters params) {
        if (debugPort != null) {
            params.getVMParametersList().addProperty(SERVER_DEBUG_PROPERTY, Boolean.TRUE.toString());
            params.getVMParametersList().addParametersString(
                    "-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=*:" + debugPort);
        }
    }

    private static void configureSslRelatedOptions(Map<String, String> defs) {
        for (Map.Entry<Object, Object> each : System.getProperties().entrySet()) {
            Object key = each.getKey();
            Object value = each.getValue();
            if (key instanceof String && value instanceof String && ((String) key).startsWith("javax.net.ssl")) {
                defs.put((String) key, (String) value);
            }
        }
    }

    private void setupMavenOpts(@Nonnull OwnJavaParameters params) {
        String mavenOpts = getMavenOptions();
        Map<String, String> mavenOptsMap;
        if (StringUtil.isNotEmpty(mavenOpts)) {
            ParametersList mavenOptsList = new ParametersList();
            mavenOptsList.addParametersString(mavenOpts);
            mavenOptsMap = mavenOptsList.getProperties();
        } else {
            mavenOptsMap = new HashMap<>();
        }
        configureSslRelatedOptions(mavenOptsMap);
        mavenOptsMap.put("java.awt.headless", "true");

        for (Map.Entry<String, String> each : mavenOptsMap.entrySet()) {
            params.getVMParametersList().defineProperty(each.getKey(), each.getValue());
        }
    }

    @Nullable
    public String getMavenOptions() {
        return System.getenv("MAVEN_OPTS");
    }

    private Integer getDebugPort() {
        if (false) {
            try {
                return NetUtils.findAvailableSocketPort();
            } catch (IOException e) {
                MavenLog.LOG.warn(e);
            }
        }
        return null;
    }

    protected @Nonnull List<String> collectIdeaRTLibraries() {
        return new ArrayList<>(Set.of(
                PathUtil.getJarPathForClass(consulo.util.rmi.RemoteServer.class),//remote-rt
                PathUtil.getJarPathForClass(Nonnull.class)));//annotations-java5
    }

    private static @Nonnull String getPlexusClassWorlds(@Nonnull Path mavenPath) {
        Path mavenBootPath = mavenPath.resolve("boot");
        String bootJarPrefix = "plexus-classworlds";
        try {
            Optional<String> bootJarPath;
            try (Stream<Path> files = Files.walk(mavenBootPath, 1)) {
                bootJarPath = files.filter(f -> isBootJar(f, bootJarPrefix))
                        .map(Path::toString)
                        .findFirst();
            }
            if (bootJarPath.isEmpty()) {
                try (Stream<Path> files = Files.walk(mavenPath)) {
                    bootJarPath = files.filter(f -> isBootJar(f, bootJarPrefix))
                            .map(Path::toString)
                            .findFirst();
                }
            }
            return bootJarPath.orElseThrow();
        } catch (IOException e) {
            throw new RuntimeException("maven boot jar not found", e);
        }
    }

    private static boolean isBootJar(Path f, String bootJarPrefix) {
        String name = f.getFileName().toString();
        return name.startsWith(bootJarPrefix) && name.endsWith(".jar");
    }

    @Nonnull
    @Override
    public ExecutionResult execute(@Nonnull Executor executor, @Nonnull ProgramRunner runner) throws ExecutionException {
        ProcessHandler processHandler = startProcess();
        return new DefaultExecutionResult(null, processHandler);
    }

    @Override
    @Nonnull
    protected ProcessHandler startProcess() throws ExecutionException {
        OwnJavaParameters params = createJavaParameters();
        GeneralCommandLine commandLine = OwnJdkUtil.setupJVMCommandLine(params);
        return ProcessHandlerBuilder.create(commandLine)
                .shouldDestroyProcessRecursively(false)
                .silentReader()
                .build();
    }
}
