package consulo.gmaven.util;

import consulo.application.util.SystemInfo;
import consulo.application.util.registry.Registry;
import consulo.externalSystem.model.project.ModuleData;
import consulo.externalSystem.rt.model.ExternalSystemException;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.gmaven.Constants;
import consulo.gmaven.MavenLog;
import consulo.gmaven.api.model.MavenId;
import consulo.gmaven.api.model.MavenProject;
import consulo.gmaven.settings.DistributionSettings;
import consulo.gmaven.wrapper.MavenWrapperDistribution;
import consulo.ide.impl.idea.ide.actions.OpenProjectFileChooserDescriptor;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.StringUtil;
import consulo.util.lang.SystemProperties;
import consulo.util.nodep.io.FileUtilRt;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

import static consulo.util.io.JarUtil.getJarAttribute;
import static consulo.util.io.JarUtil.loadProperties;
import static consulo.util.lang.StringUtil.*;

public final class MavenUtils {
    private MavenUtils() {
    }

    @Nonnull
    public static String toGAString(@NotNull ModuleData moduleData) {
        return moduleData.getGroup() + ":" + moduleData.getExternalName();
    }

    @Nonnull
    public static String toGAString(@Nonnull MavenId mavenId) {
        return mavenId.getGroupId() + ":" + mavenId.getArtifactId();
    }

    @Nonnull
    public static Path resolveM2() {
        return Path.of(SystemProperties.getUserHome(), ".m2");
    }

    public static @Nonnull Path getGeneratedSourcesDirectory(@Nonnull String buildDirectory, boolean testSources) {
        return Path.of(buildDirectory, (testSources ? "generated-test-sources" : "generated-sources"));
    }

    @Nonnull
    public static Path getMavenHome(DistributionSettings distributionSettings) {
        if (distributionSettings.getPath() != null) return Path.of(distributionSettings.getPath());
        if (distributionSettings.getUrl() != null) {
            var mavenHome = MavenWrapperDistribution.getOrDownload(distributionSettings.getUrl());
            distributionSettings.setPath(mavenHome.path().toString());
            return mavenHome.path();
        }
        throw new ExternalSystemException("maven home is empty");
    }

    public static boolean equalsPaths(String path1, String path2) {
        if (path1 == null || path2 == null) return Objects.equals(path1, path2);
        return Path.of(path1).equals(Path.of(path2));
    }

    public static boolean isValidMavenHome(@Nullable File home) {
        return home != null && isValidMavenHome(home.toPath());
    }

    public static boolean isValidMavenHome(@Nullable Path home) {
        if (home == null) return false;
        return home.resolve("bin").resolve("m2.conf").toFile().exists();
    }

    @Nullable
    public static File resolveMavenHome() {
        String m2home = System.getenv("M2_HOME");
        if (!StringUtil.isEmptyOrSpaces(m2home)) {
            final File homeFromEnv = new File(m2home);
            if (isValidMavenHome(homeFromEnv)) {
                return homeFromEnv;
            }
        }

        String mavenHome = System.getenv("MAVEN_HOME");
        if (!StringUtil.isEmptyOrSpaces(mavenHome)) {
            final File mavenHomeFile = new File(mavenHome);
            if (isValidMavenHome(mavenHomeFile)) {
                return mavenHomeFile;
            }
        }

        String userHome = SystemProperties.getUserHome();
        if (!StringUtil.isEmptyOrSpaces(userHome)) {
            File underUserHome = new File(userHome, "m2");
            if (isValidMavenHome(underUserHome)) {
                return underUserHome;
            }

            File sdkManMavenHome = Path.of(userHome, ".sdkman", "candidates", "maven", "current").toFile();
            if (isValidMavenHome(sdkManMavenHome)) {
                return sdkManMavenHome;
            }
        }

        if (SystemInfo.isMac) {
            File home = fromBrew();
            if (home != null) {
                return home;
            }

            if ((home = fromMacSystemJavaTools()) != null) {
                return home;
            }
        } else if (SystemInfo.isLinux) {
            File home = new File("/usr/share/maven");
            if (isValidMavenHome(home)) {
                return home;
            }

            home = new File("/usr/share/maven2");
            if (isValidMavenHome(home)) {
                return home;
            }
        }

        return null;
    }

    @Nullable
    public static String getMavenVersion(@Nullable File mavenHome) {
        if (mavenHome == null) return null;
        File[] libs = new File(mavenHome, "lib").listFiles();


        if (libs != null) {
            for (File mavenLibFile : libs) {
                String lib = mavenLibFile.getName();
                if (lib.equals("maven-core.jar")) {
                    MavenLog.LOG.debug("Choosing version by maven-core.jar");
                    return getMavenLibVersion(mavenLibFile);
                }
                if (lib.startsWith("maven-core-") && lib.endsWith(".jar")) {
                    MavenLog.LOG.debug("Choosing version by maven-core.xxx.jar");
                    String version = lib.substring("maven-core-".length(), lib.length() - ".jar".length());
                    return contains(version, ".x") ? getMavenLibVersion(mavenLibFile) : version;
                }
                if (lib.startsWith("maven-") && lib.endsWith("-uber.jar")) {
                    MavenLog.LOG.debug("Choosing version by maven-xxx-uber.jar");
                    return lib.substring("maven-".length(), lib.length() - "-uber.jar".length());
                }
            }
        }
        MavenLog.LOG.warn("Cannot resolve maven version for " + mavenHome);
        return null;
    }

    public static boolean isPomProject(@Nonnull MavenProject project) {
        return "pom".equalsIgnoreCase(project.getPackaging());
    }

    public static boolean isPomFileName(String fileName) {
        return fileName.equals(Constants.POM_XML) ||
                fileName.endsWith(".pom") || fileName.startsWith("pom.") ||
                fileName.equals(Constants.SUPER_POM_XML);
    }

    public static boolean isPotentialPomFile(String nameOrPath) {
        String[] split;
        try {
            String extensions = Registry.stringValue("gmaven.support.extensions");
            split = extensions.split(",");
        } catch (Exception e) {
            split = new String[]{"pom"};
        }
        return ArrayUtil.contains(FileUtilRt.getExtension(nameOrPath), split);
    }

    private static String getMavenLibVersion(final File file) {
        Properties props = loadProperties(file, "META-INF/maven/org.apache.maven/maven-core/pom.properties");
        return props != null
                ? nullize(props.getProperty("version"))
                : nullize(getJarAttribute(file, java.util.jar.Attributes.Name.IMPLEMENTATION_VERSION));
    }

    @org.jetbrains.annotations.Nullable
    private static File fromBrew() {
        final File brewDir = new File("/usr/local/Cellar/maven");
        final String[] list = brewDir.list();
        if (list == null || list.length == 0) {
            return null;
        }

        if (list.length > 1) {
            Arrays.sort(list, (o1, o2) -> compareVersionNumbers(o2, o1));
        }

        final File file = new File(brewDir, list[0] + "/libexec");
        return isValidMavenHome(file) ? file : null;
    }

    @Nullable
    private static File fromMacSystemJavaTools() {
        final File symlinkDir = new File("/usr/share/maven");
        if (isValidMavenHome(symlinkDir)) {
            return symlinkDir;
        }

        // well, try to search
        final File dir = new File("/usr/share/java");
        final String[] list = dir.list();
        if (list == null || list.length == 0) {
            return null;
        }

        String home = null;
        final String prefix = "maven-";
        final int versionIndex = prefix.length();
        for (String path : list) {
            if (path.startsWith(prefix) &&
                    (home == null || compareVersionNumbers(path.substring(versionIndex), home.substring(versionIndex)) > 0)) {
                home = path;
            }
        }

        if (home != null) {
            File file = new File(dir, home);
            if (isValidMavenHome(file)) {
                return file;
            }
        }

        return null;
    }

    @Nonnull
    public static FileChooserDescriptor getProjectFileChooserDescriptor() {
        return DescriptorHolder.MAVEN_BUILD_FILE_CHOOSER_DESCRIPTOR;
    }

    @Nonnull
    public static FileChooserDescriptor getHomeFileChooserDescriptor() {
        return DescriptorHolder.MAVEN_HOME_FILE_CHOOSER_DESCRIPTOR;
    }

    private static class DescriptorHolder {
        public static final FileChooserDescriptor MAVEN_BUILD_FILE_CHOOSER_DESCRIPTOR = new OpenProjectFileChooserDescriptor(true) {
            @Override
            public boolean isFileSelectable(VirtualFile file) {
                String fileName = file.getName();
                return isPomFileName(fileName) || isPotentialPomFile(fileName);
            }

            @Override
            public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
                if (!super.isFileVisible(file, showHiddenFiles)) {
                    return false;
                }
                String fileName = file.getName();
                return file.isDirectory() || isPomFileName(fileName) || isPotentialPomFile(fileName);
            }
        };

        public static final FileChooserDescriptor MAVEN_HOME_FILE_CHOOSER_DESCRIPTOR
                = new FileChooserDescriptor(false, true, false, false, false, false);
    }
}
