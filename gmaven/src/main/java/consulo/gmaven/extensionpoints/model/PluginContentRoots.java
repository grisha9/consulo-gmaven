package consulo.gmaven.extensionpoints.model;

import java.util.List;
import java.util.Set;

public record PluginContentRoots(List<MavenContentRoot> contentRoots, Set<String> excludedRoots) {
}
