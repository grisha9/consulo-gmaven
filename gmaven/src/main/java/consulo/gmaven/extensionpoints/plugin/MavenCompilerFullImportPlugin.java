package consulo.gmaven.extensionpoints.plugin;

import consulo.gmaven.api.model.MavenPlugin;
import consulo.gmaven.api.model.MavenProject;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Map;

public interface MavenCompilerFullImportPlugin extends MavenFullImportPlugin {

    @NotNull
    CompilerData getCompilerData(@NotNull MavenProject project,
                                 @NotNull MavenPlugin plugin,
                                 @NotNull Path localRepositoryPath,
                                 @NotNull Map<String, Element> contextElementMap);

    @Nullable
    String getAnnotationProcessorTagName();

}
