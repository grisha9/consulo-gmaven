package consulo.gmaven;

import consulo.externalSystem.model.ProjectSystemId;
import consulo.localize.LocalizeValue;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static consulo.application.util.registry.Registry.stringValue;
import static java.lang.String.format;
import static java.util.Objects.requireNonNullElse;

public interface Constants {
    String SYSTEM = "GMaven";
    ProjectSystemId SYSTEM_ID = new ProjectSystemId(SYSTEM.toUpperCase(Locale.getDefault()), LocalizeValue.of(SYSTEM));
    String BUNDLED_MAVEN_VERSION = "3.9.1";
    String BUNDLED_DISTRIBUTION_URL =
            "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%s/apache-maven-%s-bin.zip";

    String POM_XML = "pom.xml";
    String SUPER_POM_XML = "pom-4.0.0.xml";
    String PROFILES_XML = "profiles.xml";

    String SCOPE_COMPILE = "compile";
    String SCOPE_PROVIDED = "provided";
    String SCOPE_RUNTIME = "runtime";
    String SCOPE_TEST = "test";
    String SCOPE_SYSTEM = "system";
    String SCOPE_IMPORT = "import";
    String M2 = ".m2";
    String MODULE_PROP_BUILD_FILE = "buildFile";
    String MODULE_PROP_HAS_DEPENDENCIES = "hasDependencies";
    List<String> BASIC_PHASES = List.of("clean", "validate", "compile", "test", "package", "verify", "install", "deploy", "site");
    String SOURCE_SET_MODULE_TYPE_KEY = "sourceSet";

    static String getBundledDistributionUrl() {
        String version = requireNonNullElse(stringValue("gmaven.bundled.wrapper.version"), BUNDLED_MAVEN_VERSION);
        return format(BUNDLED_DISTRIBUTION_URL, version, version);
    }

    static Set<String> getScopes() {
        return Set.of(SCOPE_COMPILE, SCOPE_PROVIDED, SCOPE_RUNTIME, SCOPE_TEST, SCOPE_SYSTEM);
    }
}
