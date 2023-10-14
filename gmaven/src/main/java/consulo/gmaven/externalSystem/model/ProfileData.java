package consulo.gmaven.externalSystem.model;

import consulo.externalSystem.model.Key;
import consulo.externalSystem.model.ProjectSystemId;
import consulo.externalSystem.service.project.AbstractExternalEntityData;
import consulo.util.xml.serializer.annotation.Transient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public class ProfileData extends AbstractExternalEntityData implements Comparable<ProfileData> {
    @Nonnull
    public static final Key<ProfileData> KEY = Key.create(ProfileData.class, 90);
    @Nonnull
    private final String projectName;
    @Nonnull
    private final String name;

    private final boolean hasActivation;

    public ProfileData(@Nonnull ProjectSystemId owner, @Nonnull String projectName,
                       @Nonnull String name, boolean hasActivation) {
        super(owner);
        this.projectName = projectName;
        this.name = name;
        this.hasActivation = hasActivation;
    }

    @Nonnull
    public String getProjectName() {
        return projectName;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isHasActivation() {
        return hasActivation;
    }

    @Transient
    @Nonnull
    public String getStateKey() {
        return projectName + ":" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ProfileData that = (ProfileData) o;

        if (hasActivation != that.hasActivation) return false;
        if (!projectName.equals(that.projectName)) return false;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + projectName.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (hasActivation ? 1 : 0);
        return result;
    }

    @Override
    public int compareTo(@NotNull ProfileData that) {
        return name.compareTo(that.getName());
    }

    @Override
    public String toString() {
        return name;
    }

    public enum SimpleProfile {ACTIVE, INACTIVE}

    public enum ActivationProfile {ACTIVE, INDETERMINATE, INACTIVE}
}