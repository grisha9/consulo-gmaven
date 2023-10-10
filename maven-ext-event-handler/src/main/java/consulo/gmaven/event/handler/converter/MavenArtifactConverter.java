package consulo.gmaven.event.handler.converter;

import consulo.gmaven.api.model.MavenArtifact;
import org.apache.maven.artifact.Artifact;

public class MavenArtifactConverter {
    public static MavenArtifact convert(Artifact artifact) {
        return new MavenArtifact(artifact.getGroupId(),
                artifact.getArtifactId(),
                artifact.getBaseVersion() != null ? artifact.getBaseVersion() : artifact.getVersion(),
                artifact.getType(),
                artifact.getClassifier(),
                artifact.getScope(),
                artifact.isOptional(),
                artifact.getFile(),
                artifact.isResolved());
    }
}
