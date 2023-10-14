package consulo.gmaven.externalSystem.model;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.externalSystem.service.project.ExternalConfigPathAware;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PluginData extends AbstractExternalEntityData implements ExternalConfigPathAware, Comparable<TaskData> {
    @Nonnull
    public static final Key<PluginData> KEY = Key.create(PluginData.class, 251);
    @Nonnull
    private final String name;
    @Nonnull
    private final String description;
    @Nonnull
    private final String linkedExternalProjectPath;
    @Nonnull
    private String group;

    public PluginData(@Nonnull ProjectSystemId owner,
                      @Nonnull String name,
                      @Nonnull String linkedExternalProjectPath,
                      @Nullable String description,
                      @Nullable String group) {
        super(owner);
        this.name = name;
        this.linkedExternalProjectPath = linkedExternalProjectPath;
        this.description = description;
        this.group = group;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public String getLinkedExternalProjectPath() {
        return linkedExternalProjectPath;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    @Nullable
    public String getGroup() {
        return group;
    }

    public void setGroup(@Nullable String group) {
        this.group = group;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (group != null ? group.hashCode() : 0);
        result = 31 * result + linkedExternalProjectPath.hashCode();
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PluginData data = (PluginData) o;

        if (!name.equals(data.name)) return false;
        if (group != null ? !group.equals(data.group) : data.group != null) return false;
        if (!linkedExternalProjectPath.equals(data.linkedExternalProjectPath)) return false;
        if (description != null ? !description.equals(data.description) : data.description != null) return false;

        return true;
    }

    @Override
    public int compareTo(@Nonnull TaskData that) {
        return name.compareTo(that.getName());
    }

    @Override
    public String toString() {
        return name;
    }
}
