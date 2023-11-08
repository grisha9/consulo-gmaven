package consulo.gmaven.externalSystem.service;

import com.intellij.java.compiler.impl.javaCompiler.JavaCompilerConfiguration;
import com.intellij.java.compiler.impl.javaCompiler.annotationProcessing.ProcessorConfigProfile;
import com.intellij.java.compiler.impl.javaCompiler.annotationProcessing.impl.ProcessorConfigProfileImpl;
import com.intellij.java.compiler.impl.javaCompiler.javac.JavacCompilerConfiguration;
import consulo.annotation.component.ExtensionImpl;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.service.project.manage.ProjectDataService;
import consulo.gmaven.MavenLog;
import consulo.gmaven.externalSystem.model.CompilerPluginData;
import consulo.ide.impl.idea.openapi.externalSystem.service.project.ProjectStructureHelper;
import consulo.module.Module;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.*;

import static consulo.gmaven.util.MavenUtils.getGeneratedAnnotationsDirectory;

@ExtensionImpl
public class CompilerPluginDataService implements ProjectDataService<CompilerPluginData, ProcessorConfigProfile> {
    private static final String IMPORTED_PROFILE_NAME = "GMaven Imported";

    @Nonnull
    @Override
    public Key<CompilerPluginData> getTargetDataKey() {
        return CompilerPluginData.KEY;
    }

    @Override
    public void importData(@Nonnull Collection<DataNode<CompilerPluginData>> toImport,
                           @Nonnull Project project,
                           boolean b) {
        var importedData = new HashSet<CompilerPluginData>();
        var jvmAgs = new ArrayList<String>();
        JavaCompilerConfiguration config = JavaCompilerConfiguration.getInstance(project);
        for (var node : toImport) {
            ModuleData moduleData = (ModuleData) Optional.ofNullable(node.getParent())
                    .map(DataNode::getData)
                    .filter(it -> it instanceof ModuleData)
                    .orElse(null);
            if (moduleData == null) {
                MavenLog.LOG.debug("Failed to find parent module data. Parent: " + node.getParent());
                continue;
            }

            var ideModule = ProjectStructureHelper.findIdeModule(moduleData, project);
            if (ideModule == null) {
                MavenLog.LOG.debug("Failed to find ide module for module data: ${moduleData}");
                continue;
            }

            configureAnnotationProcessing(config, ideModule, node.getData(), importedData);
            jvmAgs.addAll(node.getData().getArguments());
        }
        if (!jvmAgs.isEmpty()) {
            var jpsJavaCompilerOptions = JavacCompilerConfiguration.getInstance(project);
            jpsJavaCompilerOptions.ADDITIONAL_OPTIONS_STRING = String.join(" ", jvmAgs);
        }
    }

    private void configureAnnotationProcessing(
            JavaCompilerConfiguration config,
            Module ideModule,
            CompilerPluginData data,
            Set<CompilerPluginData> importedData
    ) {
        if (data.getPaths().isEmpty()) return;
        var profile = findOrCreateProcessorConfigProfile(config, ideModule, data);
        if (profile == null) return;
        if (importedData.add(data)) {
            profile.clearModuleNames();
        }
        var profileEnabled = !data.getArguments().contains("-proc:none");

        profile.setEnabled(profileEnabled);
        profile.setObtainProcessorsFromClasspath(false);
        profile.setOutputRelativeToContentRoot(true);
        profile.addModuleName(ideModule.getName());
        profile.setGeneratedSourcesDirectoryName(getRelativePath(data, false), false);
        profile.setGeneratedSourcesDirectoryName(getRelativePath(data, true), true);
    }

    @Nullable
    private ProcessorConfigProfile findOrCreateProcessorConfigProfile(
            JavaCompilerConfiguration config,
            Module ideModule,
            CompilerPluginData data) {
        var moduleExistInUserProfile = config.getModuleProcessorProfiles().stream()
                .filter(it -> !it.getName().equals(IMPORTED_PROFILE_NAME))
                .flatMap(it -> it.getModuleNames().stream())
                .filter(it -> it.equals(ideModule.getName()))
                .findFirst()
                .orElse(null);
        if (moduleExistInUserProfile != null) return null;

        var newProfile = createProcessorConfigProfile(data);
        ProcessorConfigProfile configProfile = config.getModuleProcessorProfiles().stream()
                .filter(it -> matches(it, newProfile))
                .findAny()
                .orElse(null);
        if (configProfile != null) return configProfile;

        config.addModuleProcessorProfile(newProfile);
        return newProfile;
    }

    @Nonnull
    private ProcessorConfigProfileImpl createProcessorConfigProfile(CompilerPluginData compilerPluginData) {
        var newProfile = new ProcessorConfigProfileImpl(IMPORTED_PROFILE_NAME);
        newProfile.setProcessorPath(String.join(File.pathSeparator, compilerPluginData.getPaths()));
        return newProfile;
    }

    private boolean matches(@Nonnull ProcessorConfigProfile thiz, @Nonnull ProcessorConfigProfile other) {
        return thiz.getName().equals(other.getName())
                && thiz.getProcessorPath().equals(other.getProcessorPath())
                && Objects.equals(thiz.getProcessorOptions(), other.getProcessorOptions());
    }

    @Nullable
    private String getRelativePath(CompilerPluginData data, boolean isTest) {
        var annotationProcessorDirectoryFile = getGeneratedAnnotationsDirectory(data.getBuildDirectory(), isTest);
        try {
            return Path.of(data.getBaseDirectory()).relativize(annotationProcessorDirectoryFile).toString();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public void removeData(@Nonnull Collection<? extends ProcessorConfigProfile> toRemove,
                           @Nonnull Project project,
                           boolean b) {
        JavaCompilerConfiguration config = JavaCompilerConfiguration.getInstance(project);
        for (ProcessorConfigProfile configProfile : toRemove) {
            config.removeModuleProcessorProfile(configProfile);
        }
    }
}
