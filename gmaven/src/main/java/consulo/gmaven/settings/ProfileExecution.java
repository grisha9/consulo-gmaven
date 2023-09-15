package consulo.gmaven.settings;

import consulo.util.xml.serializer.annotation.Transient;

import javax.annotation.Nonnull;
import java.io.Serial;
import java.io.Serializable;

public class ProfileExecution implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Nonnull
    private final String name;
    private boolean enabled;

    public ProfileExecution(@Nonnull String name) {
        this.name = name;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Transient
    public String toRawName() {
        return enabled ? name : "!" + name;
    }
}