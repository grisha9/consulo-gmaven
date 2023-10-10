package consulo.gmaven.event.handler.converter;

import consulo.gmaven.api.model.MavenArtifact;
import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.api.model.MavenRemoteRepository;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Resource;

import java.io.File;
import java.util.*;

public class MavenProjectConverter {

    public static MavenProject convert(org.apache.maven.project.MavenProject mavenProject) {
        List<MavenPlugin> plugins = new ArrayList<>(mavenProject.getBuildPlugins().size());
        for (Plugin plugin : mavenProject.getBuildPlugins()) {
            plugins.add(MavenPluginConverter.convert(plugin, mavenProject));
        }
        List<MavenArtifact> artifacts = new ArrayList<>(mavenProject.getArtifacts().size());

        Map<Artifact, MavenArtifact> convertedArtifactMap = new HashMap<>(mavenProject.getArtifacts().size());
        for (Artifact artifact : mavenProject.getArtifacts()) {
            MavenArtifact mavenArtifact = MavenArtifactConverter.convert(artifact);
            artifacts.add(mavenArtifact);
            convertedArtifactMap.put(artifact, mavenArtifact);
        }
        List<String> modulesDir = convertModules(mavenProject.getBasedir(), mavenProject.getModules());

        MavenProject project = new MavenProject();
        project.setGroupId(mavenProject.getGroupId());
        project.setArtifactId(mavenProject.getArtifactId());
        project.setVersion(mavenProject.getVersion());
        project.setPackaging(mavenProject.getPackaging());
        project.setName(mavenProject.getName());
        project.setBasedir(mavenProject.getBasedir().getAbsolutePath());
        project.setFile(mavenProject.getFile());
        project.setParentFile(mavenProject.getParentFile());
        project.setModulesDir(modulesDir);
        project.setPlugins(plugins);
        project.setSourceRoots(mavenProject.getCompileSourceRoots());
        project.setTestSourceRoots(mavenProject.getTestCompileSourceRoots());
        project.setResourceRoots(convertResorce(mavenProject.getResources()));
        project.setTestResourceRoots(convertResorce(mavenProject.getTestResources()));
        project.setBuildDirectory(mavenProject.getBuild().getDirectory());
        project.setOutputDirectory(mavenProject.getBuild().getOutputDirectory());
        project.setTestOutputDirectory(mavenProject.getBuild().getTestOutputDirectory());
        project.setResolvedArtifacts(artifacts);
        project.setDependencyArtifacts(convertMavenArtifact(mavenProject.getDependencyArtifacts()));
        project.setParentArtifact(mavenProject.getParentArtifact() != null
                ? MavenArtifactConverter.convert(mavenProject.getParentArtifact()) : null);
        project.setProperties(getProperties(mavenProject));
        project.setRemoteRepositories(Collections.<MavenRemoteRepository>emptyList());
        return project;
    }

    private static Map<Object, Object> getProperties(org.apache.maven.project.MavenProject mavenProject) {
        Properties projectProperties = mavenProject.getProperties();
        if (projectProperties == null) {
            return Collections.emptyMap();
        } else {
            HashMap<Object, Object> result = new HashMap<>(projectProperties.size());
            result.putAll(projectProperties);
            return result;
        }
    }

    private static List<String> convertModules(File basedir, List<String> modules) {
        if (modules == null || modules.isEmpty()) return Collections.emptyList();
        ArrayList<String> result = new ArrayList<>(modules.size());
        for (String module : modules) {
            result.add(MavenProjectContainerConverter.getModuleFile(basedir, module).getAbsolutePath());
        }
        return result;
    }

    private static List<String> convertResorce(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) return Collections.emptyList();
        ArrayList<String> result = new ArrayList<>(resources.size());
        for (Resource item : resources) {
            result.add(item.getDirectory());
        }
        return result;
    }

    private static List<MavenArtifact> convertMavenArtifact(Set<Artifact> artifacts) {
        if (artifacts == null || artifacts.isEmpty()) return Collections.emptyList();
        ArrayList<MavenArtifact> result = new ArrayList<>(artifacts.size());
        for (Artifact item : artifacts) {
            result.add(MavenArtifactConverter.convert(item));
        }
        return result;
    }
}
