package consulo.gmaven.api.model;

import java.util.List;
import java.util.Objects;

public class BuildErrors {
    public final boolean pluginNotResolved;
    public final List<MavenException> exceptions;

    public BuildErrors(boolean pluginNotResolved, List<MavenException> exceptions) {
        this.pluginNotResolved = pluginNotResolved;
        this.exceptions = exceptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildErrors that = (BuildErrors) o;
        return pluginNotResolved == that.pluginNotResolved && Objects.equals(exceptions, that.exceptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginNotResolved, exceptions);
    }
}
