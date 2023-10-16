package consulo.gmaven.project;

import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.ProjectKeys;
import consulo.externalSystem.model.project.*;
import consulo.gmaven.Constants;
import consulo.gmaven.api.model.MavenArtifact;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.api.model.MavenProjectContainer;
import consulo.gmaven.model.ProjectResolverContext;
import consulo.module.content.layer.orderEntry.DependencyScope;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static consulo.externalSystem.model.project.LibraryLevel.PROJECT;

public class DependencyDataConverter {

    public static void addDependencies(
            @Nonnull MavenProjectContainer container,
            @Nonnull ProjectResolverContext context
    ) {
        addDependencies(container.getProject(), context);
        for (var childContainer : container.getModules()) {
            addDependencies(childContainer, context);
        }
    }

    private static void addDependencies(
            @Nonnull MavenProject project, @Nonnull ProjectResolverContext context
    ) {
        var moduleDataDataNode = context.moduleDataByArtifactId.get(project.getId());
        if (moduleDataDataNode != null) {
            addDependencies(project, moduleDataDataNode, context);
        }
    }

    private static void addDependencies(
            @Nonnull MavenProject project,
            @Nonnull DataNode<ModuleData> moduleByMavenProject,
            @Nonnull ProjectResolverContext context) {
        for (MavenArtifact artifact : project.getResolvedArtifacts()) {
            var moduleDataNodeByMavenArtifact = context.moduleDataByArtifactId.get(artifact.getId());
            if (moduleDataNodeByMavenArtifact == null) {
                addLibrary(moduleByMavenProject, artifact);
            } else {
                addModuleDependency(moduleByMavenProject, moduleDataNodeByMavenArtifact.getData());
            }
        }
    }

    private static void addLibrary(@Nonnull DataNode<ModuleData> parentNode,
                                   @Nonnull MavenArtifact artifact) {
        var createLibrary = createLibrary(artifact);
        var libraryDependencyData = new LibraryDependencyData(parentNode.getData(), createLibrary, PROJECT);
        libraryDependencyData.setScope(getScope(artifact));
        parentNode.createChild(ProjectKeys.LIBRARY_DEPENDENCY, libraryDependencyData);
        if (libraryDependencyData.getScope() == DependencyScope.RUNTIME) {
            var libraryDependencyDataTest = new LibraryDependencyData(parentNode.getData(), createLibrary, PROJECT);
            libraryDependencyDataTest.setScope(DependencyScope.TEST);
            parentNode.createChild(ProjectKeys.LIBRARY_DEPENDENCY, libraryDependencyDataTest);
        }
    }

    private static void addModuleDependency(@Nonnull DataNode<? extends ModuleData> parentNode,
                                            @Nonnull ModuleData targetModule) {
        var ownerModule = parentNode.getData();
        var data = new ModuleDependencyData(ownerModule, targetModule);
        parentNode.createChild(ProjectKeys.MODULE_DEPENDENCY, data);
    }

    @Nonnull
    private static LibraryData createLibrary(@Nonnull MavenArtifact artifact) {
        var library = new LibraryData(Constants.SYSTEM_ID, artifact.getId(), !artifact.isResolved());
        /*library.artifactId = artifact.getArtifactId()
        library.setGroup(artifact.groupId)
        library.version = artifact.version*/
        if (artifact.getFile() == null) return library;
        var artifactAbsolutePath = artifact.getFile().getAbsolutePath();
        library.addPath(getLibraryPathType(artifact), artifactAbsolutePath);
        var sourceAbsolutePath = artifactAbsolutePath.replace(".jar", "-sources.jar");
        if (!sourceAbsolutePath.equals(artifactAbsolutePath) && Path.of(sourceAbsolutePath).toFile().exists()) {
            library.addPath(LibraryPathType.SOURCE, sourceAbsolutePath);
        }
        return library;
    }

    @Nonnull
    private static DependencyScope getScope(@Nonnull MavenArtifact artifact) {
        if (isTestScope(artifact)) {
            return DependencyScope.TEST;
        } else if (Constants.SCOPE_RUNTIME.equalsIgnoreCase(artifact.getScope())) {
            return DependencyScope.RUNTIME;
        } else if (Constants.SCOPE_PROVIDED.equalsIgnoreCase(artifact.getScope())) {
            return DependencyScope.PROVIDED;
        } else {
            return DependencyScope.COMPILE;
        }
    }

    private int getScopeOrder(DependencyScope scope) {
        return switch (scope) {
            case TEST -> 3;
            case RUNTIME -> 2;
            case PROVIDED -> 1;
            default -> 0;
        };
    }

    @Nonnull
    private static LibraryPathType getLibraryPathType(@Nonnull MavenArtifact artifact) {
        if ("javadoc".equalsIgnoreCase(artifact.getClassifier())) {
            return LibraryPathType.DOC;
        } else if ("sources".equalsIgnoreCase(artifact.getClassifier())) {
            return LibraryPathType.SOURCE;
        } else {
            return LibraryPathType.BINARY;
        }
    }

    private static boolean isTestScope(@Nonnull MavenArtifact artifact) {
        return Constants.SCOPE_TEST.equals(artifact.getScope())
                || "tests".equalsIgnoreCase(artifact.getClassifier())
                || "test-jar".equalsIgnoreCase(artifact.getType());
    }
}
