package consulo.gmaven.api;

import consulo.gmaven.api.model.MavenException;
import consulo.gmaven.api.model.MavenResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

import static consulo.gmaven.api.GMavenServer.SERVER_ERROR_MESSAGE;

public abstract class GServerUtils {
    @Nonnull
    public static MavenResult toResult(@Nonnull Exception e) {
        List<MavenException> exceptions = Collections.singletonList(new MavenException(e.getMessage(), null, null));
        return new MavenResult(false, null, null, exceptions);
    }

    @Nonnull
    public static MavenResult toResult(@Nullable MavenResult result) {
        if (result != null) return result;
        List<MavenException> exceptions = Collections
                .singletonList(new MavenException(SERVER_ERROR_MESSAGE, null, null));
        return new MavenResult(false, null, null, exceptions);
    }
}
