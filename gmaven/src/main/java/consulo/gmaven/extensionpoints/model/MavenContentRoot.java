package consulo.gmaven.extensionpoints.model;

import consulo.externalSystem.rt.model.ExternalSystemSourceType;

public record MavenContentRoot(ExternalSystemSourceType type, String path) {
}
