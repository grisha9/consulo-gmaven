package consulo.gmaven.api.model;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Collection;

public class MavenSettings implements Serializable {
    public final int modulesCount;
    public final String localRepository;
    public final String settingsPath;
    @Nonnull
    public final Collection<MavenProfile> profiles;
    @Nonnull
    public final Collection<MavenRemoteRepository> remoteRepositories;

    public MavenSettings(int modulesCount,
                         String localRepository,
                         String settingsPath,
                         @Nonnull Collection<MavenProfile> profiles,
                         @Nonnull Collection<MavenRemoteRepository> remoteRepositories) {
        this.modulesCount = modulesCount;
        this.localRepository = localRepository;
        this.settingsPath = settingsPath;
        this.profiles = profiles;
        this.remoteRepositories = remoteRepositories;
    }
}
