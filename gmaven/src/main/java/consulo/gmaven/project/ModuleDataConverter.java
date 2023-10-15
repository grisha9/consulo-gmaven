package consulo.gmaven.project;

import com.intellij.java.language.LanguageLevel;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.ContentRootData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.rt.model.ExternalSystemSourceType;
import consulo.gmaven.Constants;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.api.model.MavenProjectContainer;
import consulo.gmaven.extensionpoints.model.MavenContentRoot;
import consulo.gmaven.extensionpoints.model.PluginContentRoots;
import consulo.gmaven.extensionpoints.plugin.ApacheMavenCompilerPlugin;
import consulo.gmaven.extensionpoints.plugin.CompilerData;
import consulo.gmaven.extensionpoints.plugin.MavenCompilerFullImportPlugin;
import consulo.gmaven.externalSystem.model.CompilerPluginData;
import consulo.gmaven.externalSystem.model.LifecycleData;
import consulo.gmaven.externalSystem.model.PluginData;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.gmaven.util.MavenArtifactUtil;
import consulo.gmaven.util.MavenUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static consulo.gmaven.Constants.SYSTEM_ID;
import static java.util.Collections.emptyList;

public class ModuleDataConverter {

    @Nonnull
    public static DataNode<ModuleData> createModuleData(
            @Nonnull MavenProjectContainer container,
            @Nonnull DataNode<?> parentDataNode,
            @Nonnull ProjectResolverContext context
    ) {
        var project = container.getProject();
        var parentExternalName = getModuleName(parentDataNode.getData(), true);
        var parentInternalName = getModuleName(parentDataNode.getData(), false);
        var id = (parentExternalName == null) ? project.getArtifactId() : ":" + project.getArtifactId();
        var projectPath = project.getBasedir();
        //val moduleFileDirectoryPath: String = getIdeaModulePath(context, projectPath)
        var moduleData = new ModuleData(id, SYSTEM_ID, project.getArtifactId(), projectPath, projectPath);
        moduleData.setInternalName(getInternalModuleName(parentInternalName, project.getArtifactId()));
        moduleData.setGroup(project.getGroupId());
        moduleData.setVersion(project.getVersion());

        moduleData.setInheritProjectCompileOutputPath(false);
        moduleData.setCompileOutputPath(ExternalSystemSourceType.SOURCE, project.getOutputDirectory());
        moduleData.setCompileOutputPath(ExternalSystemSourceType.TEST, project.getTestOutputDirectory());
        //moduleData.setProperty(MODULE_PROP_BUILD_FILE, MavenUtils.getBuildFilePath(project.file.absolutePath))

        var moduleDataNode = parentDataNode.createChild(ProjectKeys.MODULE, moduleData);

        var pluginsData = getPluginsData(project, context);
        var pluginContentRoots = pluginsData.contentRoots;
        var rootPaths = Stream.of(
                getContentRootPath(project.getSourceRoots(), ExternalSystemSourceType.SOURCE),
                getContentRootPath(project.getResourceRoots(), ExternalSystemSourceType.RESOURCE),
                getContentRootPath(project.getTestSourceRoots(), ExternalSystemSourceType.TEST),
                getContentRootPath(project.getTestResourceRoots(), ExternalSystemSourceType.TEST_RESOURCE),
                pluginContentRoots.contentRoots()
        ).flatMap(Collection::stream).toList();

        var generatedPaths = getGeneratedSources(project, pluginContentRoots.excludedRoots());
        var contentRoot = new ContentRoots(rootPaths, generatedPaths, project.getBuildDirectory());

        var compilerData = pluginsData.compilerData;
        LanguageLevel sourceLanguageLevel = compilerData.getSourceLevel();
        LanguageLevel targetBytecodeLevel = compilerData.getTargetLevel();
        //moduleDataNode.createChild(ModuleSdkData.KEY, ModuleSdkData(null));

        /*var targetBytecodeVersion = targetBytecodeLevel.toJavaVersion().toFeatureString();
        moduleDataNode.createChild(
                JavaModuleData.KEY, JavaModuleData(SYSTEM_ID, sourceLanguageLevel, targetBytecodeVersion)
        );*/

        populateAnnotationProcessorData(project, moduleDataNode, compilerData);
        populateTasks(moduleDataNode, project, context);

        var contentRootData = createContentRootData(contentRoot, project.getBasedir());
        contentRootData.forEach(it -> moduleDataNode.createChild(ProjectKeys.CONTENT_ROOT, it));
        context.moduleDataByArtifactId.put(project.getId(), moduleDataNode);

        setParentGA(project, context, moduleData);
        if (parentDataNode.getData() instanceof ModuleData) {
            for (var childContainer : container.getModules()) {
                createModuleData(childContainer, moduleDataNode, context);
            }
        }
        return moduleDataNode;
    }

    @Nonnull
    private static List<MavenContentRoot> getContentRootPath(
            @Nonnull List<String> paths, @Nonnull ExternalSystemSourceType type) {
        return paths.stream()
                .filter(it -> !it.isEmpty())
                .map(it -> new MavenContentRoot(type, it))
                .toList();
    }

    private static void setParentGA(
            @Nonnull MavenProject project,
            @Nonnull ProjectResolverContext context,
            @Nonnull ModuleData moduleData
    ) {
        if (project.getParentArtifact() != null) {
            /*Optional.ofNullable(context.moduleDataByArtifactId.get(project.getParentArtifact().getId()))
                    .map(DataNode::getData)
                    .ifPresent(it -> it.setProperty(MODULE_PROP_PARENT_GA, MavenUtils.toGAString(it)));*/
        }
    }

    @Nullable
    private static String getModuleName(@Nonnull Object data, boolean external) {
        return data instanceof ModuleData
                ? (external ? ((ModuleData) data).getExternalName() : ((ModuleData) data).getInternalName())
                : null;
    }

    @Nonnull
    private static String getInternalModuleName(@Nullable String parentName, @Nonnull String moduleName) {
        if (parentName == null) return moduleName;
        return parentName + "." + moduleName;
    }

    @Nonnull
    private static List<ContentRootData> createContentRootData(@Nonnull ContentRoots contentRoots,
                                                               @Nonnull String rootPath) {
        var roots = new ArrayList<ContentRootData>();
        roots.add(new ContentRootData(SYSTEM_ID, rootPath));
        if (contentRoots.excludedPath != null) {
            addExcludedContentRoot(roots, contentRoots.excludedPath);
        }
        for (var contentRoot : contentRoots.paths) {
            addContentRoot(roots, contentRoot.type(), contentRoot.path());
        }
        for (var generatedContentRoot : contentRoots.generatedPaths) {
            addContentRoot(roots, generatedContentRoot);
        }
        return roots;
    }

    private static void addContentRoot(@Nonnull List<ContentRootData> roots,
                                       @Nonnull ExternalSystemSourceType type,
                                       @Nonnull String path) {
        var errorOnStorePath = false;
        for (var root : roots) {
            try {
                root.storePath(type, path);
                return;
            } catch (IllegalArgumentException e) {
                errorOnStorePath = true;
            }
        }
        if (errorOnStorePath) {
            var contentRootData = new ContentRootData(SYSTEM_ID, path);
            contentRootData.storePath(type, path);
            roots.add(contentRootData);
        }
    }

    private static void addContentRoot(@Nonnull List<ContentRootData> roots,
                                       @Nonnull MavenGeneratedContentRoot generatedContentRoot) {
        var errorOnStorePath = false;
        var added = false;
        for (var root : roots) {
            for (var path : generatedContentRoot.paths) {
                try {
                    root.storePath(generatedContentRoot.type, path);
                    added = true;
                } catch (IllegalArgumentException e) {
                    errorOnStorePath = true;
                }
            }
            if (added) return;
        }
        if (errorOnStorePath) {
            var contentRootData = new ContentRootData(SYSTEM_ID, generatedContentRoot.rootPath.toString());
            generatedContentRoot.paths.forEach(it -> contentRootData.storePath(generatedContentRoot.type(), it));
            roots.add(contentRootData);
        }
    }

    private static void addExcludedContentRoot(@Nonnull List<ContentRootData> roots, @Nonnull String path) {
        var errorOnStorePath = false;
        for (var root : roots) {
            try {
                var sizeBefore = root.getPaths(ExternalSystemSourceType.EXCLUDED).size();
                root.storePath(ExternalSystemSourceType.EXCLUDED, path);
                var sizeAfter = root.getPaths(ExternalSystemSourceType.EXCLUDED).size();
                if (sizeBefore == sizeAfter) throw new IllegalArgumentException();
                return;
            } catch (IllegalArgumentException e) {
                errorOnStorePath = true;
            }
        }
        if (errorOnStorePath) {
            var contentRootData = new ContentRootData(SYSTEM_ID, path);
            contentRootData.storePath(ExternalSystemSourceType.EXCLUDED, path);
            roots.add(contentRootData);
        }
    }

    @Nonnull
    private static List<MavenGeneratedContentRoot> getGeneratedSources(@Nonnull MavenProject project,
                                                                       @Nonnull Set<String> excludedRoots) {
        if (MavenUtils.isPomProject(project)) return emptyList();
        var generatedSourcePath = MavenUtils.getGeneratedSourcesDirectory(project.getBuildDirectory(), false);
        var generatedTestSourcePath = MavenUtils.getGeneratedSourcesDirectory(project.getBuildDirectory(), true);
        return Stream.of(
                getGeneratedContentRoot(generatedSourcePath, excludedRoots, ExternalSystemSourceType.SOURCE_GENERATED),
                getGeneratedContentRoot(generatedTestSourcePath, excludedRoots, ExternalSystemSourceType.TEST_GENERATED)
        ).filter(Objects::nonNull).toList();
    }

    @Nullable
    private static MavenGeneratedContentRoot getGeneratedContentRoot(
            @Nonnull Path rootPath,
            @Nonnull Set<String> excludedRoots,
            @Nonnull ExternalSystemSourceType type
    ) {
        var listFiles = rootPath.toFile().listFiles();
        if (listFiles == null) return null;
        var paths = Arrays.stream(listFiles)
                .filter(File::isDirectory)
                .map(File::getAbsolutePath)
                .filter(absolutePath -> !excludedRoots.contains(absolutePath))
                .toList();
        return new MavenGeneratedContentRoot(type, rootPath, paths);
    }

    @Nonnull
    private static PluginsData getPluginsData(
            @Nonnull MavenProject mavenProject, @Nonnull ProjectResolverContext context
    ) {
        var contentRoots = new ArrayList<MavenContentRoot>();
        var excludedRoots = new HashSet<String>(4);

        MavenPlugin compilerPlugin = null;
        CompilerData compilerData = null;

        var localRepoPath = context.mavenResult.settings.localRepository;
        for (var plugin : mavenProject.getPlugins()) {
            var pluginExtension = context.pluginExtensionMap.get(MavenUtils.toGAString(plugin));
            if (pluginExtension == null) continue;
            var pluginContentRoot = pluginExtension.getContentRoots(mavenProject, plugin, context);
            contentRoots.addAll(pluginContentRoot.contentRoots());
            excludedRoots.addAll(pluginContentRoot.excludedRoots());
            if (pluginExtension instanceof MavenCompilerFullImportPlugin && compilerPlugin == null) {
                compilerPlugin = plugin;
                if (localRepoPath != null) {
                    compilerData = ((MavenCompilerFullImportPlugin) pluginExtension)
                            .getCompilerData(mavenProject, plugin, Path.of(localRepoPath), context.contextElementMap);
                }
            }
        }
        if (compilerData == null) {
            compilerData = ApacheMavenCompilerPlugin.getDefaultCompilerData(mavenProject, context.languageLevel);
        }
        return new PluginsData(
                compilerPlugin, compilerData, new PluginContentRoots(contentRoots, excludedRoots)
        );
    }

    private static void populateTasks(
            @Nonnull DataNode<ModuleData> moduleDataNode,
            @Nonnull MavenProject mavenProject,
            @Nonnull ProjectResolverContext context
    ) {
        for (String basicPhase : Constants.BASIC_PHASES) {
            moduleDataNode.createChild(
                    LifecycleData.KEY, new LifecycleData(SYSTEM_ID, basicPhase, mavenProject.getBasedir())
            );
        }
        for (String basicPhase : Constants.BASIC_PHASES) {
            moduleDataNode.createChild(
                    ProjectKeys.TASK, new TaskData(SYSTEM_ID, basicPhase, mavenProject.getBasedir(), null)
            );
        }
        if (!context.settings.isShowPluginNodes()) {
            MavenArtifactUtil.clearPluginDescriptorCache();
            return;
        }
        var localRepoPath = context.mavenResult.settings.localRepository;
        if (localRepoPath == null) {
            return;
        }
        for (var plugin : mavenProject.getPlugins()) {
            var pluginDescriptor = MavenArtifactUtil.readPluginDescriptor(Path.of(localRepoPath), plugin);
            if (pluginDescriptor == null) continue;
            for (var mojo : pluginDescriptor.getMojos()) {
                moduleDataNode.createChild(
                        PluginData.KEY,
                        new PluginData(
                                SYSTEM_ID, mojo.getDisplayName(), mavenProject.getBasedir(),
                                plugin.getArtifactId(), pluginDescriptor.getGoalPrefix()
                        )
                );
            }
        }
    }

    private static void populateAnnotationProcessorData(
            @Nonnull MavenProject project,
            @Nonnull DataNode<ModuleData> moduleDataNode,
            @Nonnull CompilerData compilerData
    ) {
        moduleDataNode.createChild(
                CompilerPluginData.KEY,
                new CompilerPluginData(SYSTEM_ID, compilerData.getAnnotationProcessorPaths(),
                        compilerData.getArguments(), project.getBuildDirectory(), project.getBasedir())
        );
    }

    private record MavenGeneratedContentRoot(
            @Nonnull ExternalSystemSourceType type, @Nonnull Path rootPath, @Nonnull List<String> paths) {

    }

    private record ContentRoots(
            @Nonnull List<MavenContentRoot> paths,
            @Nonnull List<MavenGeneratedContentRoot> generatedPaths,
            @Nullable String excludedPath
    ) {
    }

    private record PluginsData(
            @Nullable MavenPlugin compilerPlugin,
            @Nonnull CompilerData compilerData,
            @Nonnull PluginContentRoots contentRoots
    ) {
    }
}


