package consulo.gmaven.api.model;

import java.io.Serializable;
import java.util.Objects;

public class MavenRemoteRepository implements Serializable {
    public final String id;
    public final String url;

    public MavenRemoteRepository(String id, String url) {
        this.id = id;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MavenRemoteRepository that = (MavenRemoteRepository) o;
        return Objects.equals(id, that.id) && Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url);
    }
}
