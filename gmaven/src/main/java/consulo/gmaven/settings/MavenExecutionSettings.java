package consulo.gmaven.settings;

import consulo.externalSystem.model.setting.ExternalSystemExecutionSettings;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class MavenExecutionSettings extends ExternalSystemExecutionSettings {

    @Serial
    private static final long serialVersionUID = 1L;

    @Nonnull
    private final MavenExecutionWorkspace executionWorkspace = new MavenExecutionWorkspace();
    @Nonnull
    private final DistributionSettings distributionSettings;
    @Nullable
    private String javaHome;
    @Nullable
    private String jdkName;
    @Nullable
    private String myIdeProjectPath;
    @Nullable
    private String threadCount;
    @Nullable 
    private String vmOptions;
    @Nullable
    private String arguments;
    @Nullable
    private String argumentsImport;
    private final boolean offlineWork;
    private boolean resolveModulePerSourceSet = false;
    private boolean useQualifiedModuleNames = false;
    private boolean nonRecursive = false;
    private boolean updateSnapshots = false;
    private boolean showPluginNodes = true;
    @Nonnull
    private OutputLevelType outputLevel = OutputLevelType.DEFAULT;

    public MavenExecutionSettings(@Nonnull DistributionSettings distributionSettings,
                                  boolean nonRecursive,
                                  boolean offlineWork) {
        this.distributionSettings = Objects.requireNonNull(distributionSettings);
        this.nonRecursive = nonRecursive;
        this.offlineWork = offlineWork;
    }

    public void setIdeProjectPath(@Nullable String ideProjectPath) {
        myIdeProjectPath = ideProjectPath;
    }

    @Nullable
    public String getIdeProjectPath() {
        return myIdeProjectPath;
    }

    @Nonnull
    public DistributionSettings getDistributionSettings() {
        return distributionSettings;
    }

    @Nullable
    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(@Nullable String javaHome) {
        this.javaHome = javaHome;
    }

    @Nullable
    public String getJdkName() {
        return jdkName;
    }

    public void setJdkName(@Nullable String jdkName) {
        this.jdkName = jdkName;
    }

    public boolean isOfflineWork() {
        return offlineWork;
    }

    public boolean isResolveModulePerSourceSet() {
        return resolveModulePerSourceSet;
    }

    public void setResolveModulePerSourceSet(boolean resolveModulePerSourceSet) {
        this.resolveModulePerSourceSet = resolveModulePerSourceSet;
    }

    public boolean isUseQualifiedModuleNames() {
        return useQualifiedModuleNames;
    }

    public void setUseQualifiedModuleNames(boolean useQualifiedModuleNames) {
        this.useQualifiedModuleNames = useQualifiedModuleNames;
    }

    @Nonnull
    public MavenExecutionWorkspace getExecutionWorkspace() {
        return executionWorkspace;
    }

    @Nullable
    public String getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(@Nullable String threadCount) {
        this.threadCount = threadCount;
    }

    public boolean isNonRecursive() {
        return nonRecursive;
    }

    public void setNonRecursive(boolean nonRecursive) {
        this.nonRecursive = nonRecursive;
    }

    public boolean isUpdateSnapshots() {
        return updateSnapshots;
    }

    public void setUpdateSnapshots(boolean updateSnapshots) {
        this.updateSnapshots = updateSnapshots;
    }

    @Nonnull
    public OutputLevelType getOutputLevel() {
        return outputLevel;
    }

    public void setOutputLevel(@Nonnull OutputLevelType outputLevel) {
        this.outputLevel = outputLevel;
    }

    public boolean isShowPluginNodes() {
        return showPluginNodes;
    }

    public void setShowPluginNodes(boolean showPluginNodes) {
        this.showPluginNodes = showPluginNodes;
    }

    @Nullable
    public String getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(@Nullable String vmOptions) {
        this.vmOptions = vmOptions;
    }

    @Nullable
    public String getArguments() {
        return arguments;
    }

    public void setArguments(@Nullable String arguments) {
        this.arguments = arguments;
    }

    public void setArgumentsImport(@Nullable String argumentsImport) {
        this.argumentsImport = argumentsImport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        MavenExecutionSettings that = (MavenExecutionSettings) o;

        if (offlineWork != that.offlineWork) return false;
        if (resolveModulePerSourceSet != that.resolveModulePerSourceSet) return false;
        if (useQualifiedModuleNames != that.useQualifiedModuleNames) return false;
        if (!executionWorkspace.equals(that.executionWorkspace)) return false;
        if (!distributionSettings.equals(that.distributionSettings)) return false;
        if (!Objects.equals(javaHome, that.javaHome)) return false;
        if (!Objects.equals(jdkName, that.jdkName)) return false;
        if (!Objects.equals(argumentsImport, that.argumentsImport)) return false;
        if (!Objects.equals(vmOptions, that.vmOptions)) return false;
        if (!Objects.equals(arguments, that.arguments)) return false;
        return Objects.equals(myIdeProjectPath, that.myIdeProjectPath);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + executionWorkspace.hashCode();
        result = 31 * result + distributionSettings.hashCode();
        result = 31 * result + (offlineWork ? 1 : 0);
        result = 31 * result + (javaHome != null ? javaHome.hashCode() : 0);
        result = 31 * result + (jdkName != null ? jdkName.hashCode() : 0);
        result = 31 * result + (myIdeProjectPath != null ? myIdeProjectPath.hashCode() : 0);
        result = 31 * result + (argumentsImport != null ? argumentsImport.hashCode() : 0);
        result = 31 * result + (vmOptions != null ? vmOptions.hashCode() : 0);
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        result = 31 * result + (resolveModulePerSourceSet ? 1 : 0);
        result = 31 * result + (useQualifiedModuleNames ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return distributionSettings.toString();
    }
}
