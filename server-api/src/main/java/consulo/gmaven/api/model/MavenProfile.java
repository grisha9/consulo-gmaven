package consulo.gmaven.api.model;

import java.io.Serializable;
import java.util.Objects;

public class MavenProfile implements Serializable {
    public final String name;
    public final boolean activation;

    public MavenProfile(String name, boolean activation) {
        this.name = name;
        this.activation = activation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenProfile that = (MavenProfile) o;
        return activation == that.activation && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, activation);
    }
}
