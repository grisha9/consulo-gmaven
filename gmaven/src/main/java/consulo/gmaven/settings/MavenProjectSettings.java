package consulo.gmaven.settings;

import consulo.externalSystem.setting.ExternalProjectSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MavenProjectSettings extends ExternalProjectSettings {
    @Nullable
    private String projectBuildFile;
    @Nonnull
    private DistributionSettings distributionSettings = DistributionSettings.getBundled();
    @Nullable
    private String jdkName;
    @Nullable
    private String vmOptions;
    private boolean nonRecursive  = false;
    private boolean useWholeProjectContext = true;
    private boolean showPluginNodes = true;
    @Nonnull
    private OutputLevelType outputLevel = OutputLevelType.DEFAULT;
    @Nonnull
    private SnapshotUpdateType snapshotUpdateType = SnapshotUpdateType.DEFAULT;
    @Nullable
    private String threadCount;
    @Nullable
    private String arguments;
    @Nullable
    private String argumentsImport;

    @Nullable
    public String getProjectBuildFile() {
        return projectBuildFile;
    }

    public void setProjectBuildFile(@Nullable String projectBuildFile) {
        this.projectBuildFile = projectBuildFile;
    }

    @Nonnull
    public DistributionSettings getDistributionSettings() {
        return distributionSettings;
    }

    public void setDistributionSettings(@Nonnull DistributionSettings distributionSettings) {
        this.distributionSettings = distributionSettings;
    }

    @Nullable
    public String getJdkName() {
        return jdkName;
    }

    public void setJdkName(@Nullable String jdkName) {
        this.jdkName = jdkName;
    }

    @Nullable
    public String getVmOptions() {
        return vmOptions;
    }

    public void setVmOptions(@Nullable String vmOptions) {
        this.vmOptions = vmOptions;
    }

    public boolean isNonRecursive() {
        return nonRecursive;
    }

    public void setNonRecursive(boolean nonRecursive) {
        this.nonRecursive = nonRecursive;
    }

    @Nonnull
    public SnapshotUpdateType getSnapshotUpdateType() {
        return snapshotUpdateType;
    }

    public void setSnapshotUpdateType(@Nonnull SnapshotUpdateType snapshotUpdateType) {
        this.snapshotUpdateType = snapshotUpdateType;
    }

    public boolean isUseWholeProjectContext() {
        return useWholeProjectContext;
    }

    public void setUseWholeProjectContext(boolean useWholeProjectContext) {
        this.useWholeProjectContext = useWholeProjectContext;
    }

    public boolean isShowPluginNodes() {
        return showPluginNodes;
    }

    public void setShowPluginNodes(boolean showPluginNodes) {
        this.showPluginNodes = showPluginNodes;
    }

    @Nonnull
    public OutputLevelType getOutputLevel() {
        return outputLevel;
    }

    public void setOutputLevel(@Nonnull OutputLevelType outputLevel) {
        this.outputLevel = outputLevel;
    }

    @Nullable
    public String getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(@Nullable String threadCount) {
        this.threadCount = threadCount;
    }

    @Nullable
    public String getArguments() {
        return arguments;
    }

    public void setArguments(@Nullable String arguments) {
        this.arguments = arguments;
    }

    @Nullable
    public String getArgumentsImport() {
        return argumentsImport;
    }

    public void setArgumentsImport(@Nullable String argumentsImport) {
        this.argumentsImport = argumentsImport;
    }

    @Nonnull
    @Override
    public ExternalProjectSettings clone() {
        MavenProjectSettings result = new MavenProjectSettings();
        copyTo(result);
        result.projectBuildFile = projectBuildFile;
        result.distributionSettings = distributionSettings;
        result.jdkName = jdkName;
        result.vmOptions = vmOptions;
        result.outputLevel = outputLevel;
        result.threadCount = threadCount;
        result.arguments = arguments;
        result.argumentsImport = argumentsImport;
        result.nonRecursive  = nonRecursive;
        result.snapshotUpdateType = snapshotUpdateType;
        result.useWholeProjectContext = useWholeProjectContext;
        result.showPluginNodes = showPluginNodes;
        return result;
    }
}
