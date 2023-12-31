package consulo.gmaven.api.model;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;

public class MavenProject extends MavenId {
    private static final long serialVersionUID = -3453607480882347421L;

    private String name;
    private String packaging;
    private File file;
    private File parentFile;
    private String basedir;
    private MavenArtifact parentArtifact;
    private List<MavenPlugin> plugins;
    private List<String> modulesDir;
    private List<String> sourceRoots;
    private List<String> testSourceRoots;
    private List<String> resourceRoots;
    private List<String> testResourceRoots;
    private List<MavenArtifact> resolvedArtifacts;
    private List<MavenArtifact> dependencyArtifacts;
    private String buildDirectory;//target dir
    private String outputDirectory;
    private String testOutputDirectory;
    private Map<Object, Object> properties;
    private List<MavenRemoteRepository> remoteRepositories;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackaging() {
        return packaging;
    }

    public void setPackaging(String packaging) {
        this.packaging = packaging;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getParentFile() {
        return parentFile;
    }

    public void setParentFile(File parentFile) {
        this.parentFile = parentFile;
    }

    public String getBasedir() {
        return basedir;
    }

    public void setBasedir(String basedir) {
        this.basedir = basedir;
    }

    public MavenArtifact getParentArtifact() {
        return parentArtifact;
    }

    public void setParentArtifact(MavenArtifact parentArtifact) {
        this.parentArtifact = parentArtifact;
    }

    public List<MavenPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<MavenPlugin> plugins) {
        this.plugins = plugins;
    }

    public List<String> getModulesDir() {
        return modulesDir;
    }

    public void setModulesDir(List<String> modulesDir) {
        this.modulesDir = modulesDir;
    }

    public List<String> getSourceRoots() {
        return sourceRoots;
    }

    public void setSourceRoots(List<String> sourceRoots) {
        this.sourceRoots = sourceRoots;
    }

    public List<String> getTestSourceRoots() {
        return testSourceRoots;
    }

    public void setTestSourceRoots(List<String> testSourceRoots) {
        this.testSourceRoots = testSourceRoots;
    }

    public List<String> getResourceRoots() {
        return resourceRoots;
    }

    public void setResourceRoots(List<String> resourceRoots) {
        this.resourceRoots = resourceRoots;
    }

    public List<String> getTestResourceRoots() {
        return testResourceRoots;
    }

    public void setTestResourceRoots(List<String> testResourceRoots) {
        this.testResourceRoots = testResourceRoots;
    }

    public List<MavenArtifact> getResolvedArtifacts() {
        return resolvedArtifacts;
    }

    public void setResolvedArtifacts(List<MavenArtifact> resolvedArtifacts) {
        this.resolvedArtifacts = resolvedArtifacts;
    }

    public List<MavenArtifact> getDependencyArtifacts() {
        return dependencyArtifacts;
    }

    public void setDependencyArtifacts(List<MavenArtifact> dependencyArtifacts) {
        this.dependencyArtifacts = dependencyArtifacts;
    }

    public String getBuildDirectory() {
        return buildDirectory;
    }

    public void setBuildDirectory(String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getTestOutputDirectory() {
        return testOutputDirectory;
    }

    public void setTestOutputDirectory(String testOutputDirectory) {
        this.testOutputDirectory = testOutputDirectory;
    }

    public Map<Object, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<Object, Object> properties) {
        this.properties = properties;
    }

    public List<MavenRemoteRepository> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List<MavenRemoteRepository> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public @Nonnull String getDisplayName() {
        return (name == null || name.isEmpty()) ? artifactId : name;
    }
}
