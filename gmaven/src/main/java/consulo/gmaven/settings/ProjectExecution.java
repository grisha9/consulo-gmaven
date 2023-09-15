package consulo.gmaven.settings;

import consulo.util.xml.serializer.annotation.Transient;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.io.Serializable;

public class ProjectExecution implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Nonnull
    private final String name;
    private final boolean enabled;

    public ProjectExecution(@Nonnull String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Transient
    public String toRawName() {
        return enabled ? name : "!" + name;
    }
}