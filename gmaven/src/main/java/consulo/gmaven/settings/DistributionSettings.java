package consulo.gmaven.settings;

import consulo.gmaven.Constants;
import consulo.util.xml.serializer.Converter;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Objects;

import static consulo.gmaven.settings.DistributionType.BUNDLED;
import static java.util.Objects.requireNonNull;

public class DistributionSettings {
    private DistributionType type;
    private String path;
    private String url;

    public DistributionSettings(DistributionType type, String path, String url) {
        this.type = requireNonNull(type);
        this.path = path;
        this.url = url;
    }

    public static DistributionSettings getBundled() {
        return new DistributionSettings(BUNDLED, null, Constants.getBundledDistributionUrl());
    }

    public static DistributionSettings getWrapper(String distributionUrl) {
        return new DistributionSettings(DistributionType.WRAPPER, null, requireNonNull(distributionUrl));
    }

    public static DistributionSettings getLocal(String mavenPath) {
        return new DistributionSettings(DistributionType.MVN, requireNonNull(mavenPath), null);
    }

    public DistributionSettings() {
    }

    public DistributionType getType() {
        return type;
    }

    public void setType(DistributionType type) {
        this.type = requireNonNull(type);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DistributionSettings that = (DistributionSettings) o;

        if (type != that.type) return false;
        if (!Objects.equals(path, that.path)) return false;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

    private static final class PathConverter extends Converter<Path> {

        @Override
        public @Nonnull Path fromString(@Nonnull String string) {
            return Path.of(string);
        }

        @Override
        public @Nonnull String toString(@Nonnull Path path) {
            return path.toString();
        }
    }
}
