package consulo.gmaven.api.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Objects;

public class MavenId implements Serializable {
    private static final long serialVersionUID = -6113607480882347420L;
    public static final String UNKNOWN_VALUE = "Unknown";

    @Nonnull
    String groupId;
    @Nonnull
    protected String artifactId;
    @Nonnull
    protected String version;

    public MavenId(@Nullable String groupId, @Nullable String artifactId, @Nullable String version) {
        this.groupId = ObjectUtils.defaultIfNull(groupId, UNKNOWN_VALUE);
        this.artifactId = ObjectUtils.defaultIfNull(artifactId, UNKNOWN_VALUE);
        this.version = ObjectUtils.defaultIfNull(version, UNKNOWN_VALUE);
    }

    public MavenId() {
        this(null);
    }

    public MavenId(@Nullable String coord) {
        if (coord == null) {
            groupId = artifactId = version = UNKNOWN_VALUE;
        } else {
            String[] parts = coord.split(":");
            groupId = parts.length > 0 ? parts[0] : UNKNOWN_VALUE;
            artifactId = parts.length > 1 ? parts[1] : UNKNOWN_VALUE;
            version = parts.length > 2 ? parts[2] : UNKNOWN_VALUE;
        }
    }

    @Nonnull
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(@Nonnull String groupId) {
        this.groupId = groupId;
    }

    @Nonnull
    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(@Nonnull String artifactId) {
        this.artifactId = artifactId;
    }

    @Nonnull
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nonnull String version) {
        this.version = version;
    }

    public boolean equals(@Nullable String groupId, @Nullable String artifactId) {
        if (!Objects.equals(this.artifactId, artifactId)) return false;
        if (!Objects.equals(this.groupId, groupId)) return false;
        return true;
    }

    public boolean equals(@Nullable String groupId, @Nullable String artifactId, @Nullable String version) {
        if (!equals(groupId, artifactId)) return false;
        if (!Objects.equals(this.version, version)) return false;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MavenId other = (MavenId) o;
        return equals(other.groupId, other.artifactId, other.version);
    }

    @Override
    public int hashCode() {
        int result;
        result = groupId.hashCode();
        result = 31 * result + artifactId.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
}
