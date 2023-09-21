package consulo.gmaven;

import consulo.externalSystem.model.ProjectSystemId;
import consulo.localize.LocalizeValue;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Locale.getDefault;

public interface Constants {
    String GMAVEN = "GMaven";
    ProjectSystemId SYSTEM_ID = new GMavenProjectSystemId(GMAVEN.toUpperCase(getDefault()), LocalizeValue.of(GMAVEN));
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
    String MODULE_PROP_PARENT_GA = "parentGA";
    String MODULE_PROP_HAS_DEPENDENCIES = "hasDependencies";
    List<String> BASIC_PHASES = List.of("clean", "validate", "compile", "test", "package", "verify", "install", "deploy", "site");
    String SOURCE_SET_MODULE_TYPE_KEY = "sourceSet";

    static String getBundledDistributionUrl() {
        return format(BUNDLED_DISTRIBUTION_URL, BUNDLED_MAVEN_VERSION, BUNDLED_MAVEN_VERSION);
    }

    static Set<String> getScopes() {
        return Set.of(SCOPE_COMPILE, SCOPE_PROVIDED, SCOPE_RUNTIME, SCOPE_TEST, SCOPE_SYSTEM);
    }

    class GMavenProjectSystemId extends ProjectSystemId {
        public GMavenProjectSystemId(@Nonnull String id, @Nonnull LocalizeValue displayName) {
            super(id, displayName);
        }

        @Override
        public boolean isInProcessMode() {
            return true;
        }
    }
}
