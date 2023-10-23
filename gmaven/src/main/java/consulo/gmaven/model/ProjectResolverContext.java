package consulo.gmaven.model;

import com.intellij.java.language.LanguageLevel;
import consulo.externalSystem.model.DataNode;
import consulo.externalSystem.model.project.LibraryData;
import consulo.externalSystem.model.project.ModuleData;
import consulo.gmaven.api.model.MavenResult;
import consulo.gmaven.extensionpoints.plugin.MavenFullImportPlugin;
import consulo.gmaven.settings.MavenExecutionSettings;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProjectResolverContext {
    public MavenExecutionSettings settings;
    public String rootProjectPath;
    public MavenResult mavenResult;
    public LanguageLevel languageLevel;
    public Map<String, Element> contextElementMap = new HashMap<>();
    public Map<String, DataNode<ModuleData>> moduleDataByArtifactId = new TreeMap<>();
    public Map<String, LibraryData> libraryDataByName = new TreeMap<>();
    public Map<String, MavenFullImportPlugin> pluginExtensionMap  = MavenFullImportPlugin
            .EP_NAME.getExtensionList().stream()
            .collect(Collectors.toMap(MavenFullImportPlugin::getKey, Function.identity()));
}