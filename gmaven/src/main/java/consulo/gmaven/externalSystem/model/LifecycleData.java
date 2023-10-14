package consulo.gmaven.externalSystem.model;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.model.task.TaskData;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.externalSystem.service.project.ExternalConfigPathAware;

import javax.annotation.Nonnull;

public class LifecycleData extends AbstractExternalEntityData implements ExternalConfigPathAware, Comparable<TaskData> {
    @Nonnull
    public static final Key<LifecycleData> KEY = Key.create(LifecycleData.class, 250);
    @Nonnull
    private final String name;
    @Nonnull
    private final String linkedExternalProjectPath;

    public LifecycleData(@Nonnull ProjectSystemId owner,
                         @Nonnull String name,
                         @Nonnull String linkedExternalProjectPath) {
        super(owner);
        this.name = name;
        this.linkedExternalProjectPath = linkedExternalProjectPath;
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

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + linkedExternalProjectPath.hashCode();
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        LifecycleData data = (LifecycleData) o;

        if (!name.equals(data.name)) return false;
        if (!linkedExternalProjectPath.equals(data.linkedExternalProjectPath)) return false;

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
