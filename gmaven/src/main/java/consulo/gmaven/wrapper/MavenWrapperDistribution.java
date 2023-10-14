package consulo.gmaven.wrapper;

import consulo.application.progress.ProgressIndicator;
import consulo.application.progress.Task;
import consulo.application.util.SystemInfo;
import consulo.gmaven.MavenLog;
import consulo.gmaven.notfication.MavenNotification;
import consulo.gmaven.util.MavenUtils;
import consulo.http.HttpRequests;
import consulo.ide.impl.idea.openapi.progress.impl.BackgroundableProcessIndicator;
import consulo.localize.LocalizeValue;
import consulo.project.ui.notification.NotificationType;
import consulo.util.io.FilePermissionCopier;
import consulo.util.io.FileUtil;
import consulo.util.lang.ControlFlowException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static consulo.gmaven.MavenBundle.message;
import static java.util.Objects.requireNonNullElse;

public class MavenWrapperDistribution {

    @Nonnull
    public static WrapperDistribution getOrDownload(@Nonnull String urlString) {
        var current = getCurrentDistribution(urlString);
        if (current != null) return current;

        var taskInfo = new Task.Backgroundable(null, message("gmaven.wrapper.downloading"), true) {
            @Override
            public void run(@Nonnull ProgressIndicator progressIndicator) {
            }
        };
        var indicator = new BackgroundableProcessIndicator(taskInfo);
        try {
            return downloadMavenDistribution(urlString, indicator);
        } finally {
            indicator.finish(taskInfo);
        }
    }

    @Nullable
    private static WrapperDistribution getCurrentDistribution(@Nonnull String urlString) {
        var zipFile = getZipFile(urlString);
        File currentMavenWrapperPath = getCurrentMavenWrapperPath(zipFile.toFile());
        return currentMavenWrapperPath != null
                ? new WrapperDistribution(currentMavenWrapperPath.toPath(), urlString) : null;
    }

    @Nonnull
    private static WrapperDistribution downloadMavenDistribution(
            @Nonnull String urlString, BackgroundableProcessIndicator indicator
    ) {
        var zipFilePath = getZipFile(urlString);
        informationEvent(message("gmaven.wrapper.notification.downloading.start"));
        if (!zipFilePath.toFile().isFile()) {
            var parent = zipFilePath.getParent();
            var partFile = parent.resolve("${zipFilePath.name}.part-${System.currentTimeMillis()}");
            indicator.setTextValue(LocalizeValue.of(message("gmaven.wrapper.downloading.from", urlString)));
            try {
                HttpRequests.request(urlString)
                        .forceHttps(false)
                        .connectTimeout(30_000)
                        .readTimeout(30_000)
                        .saveToFile(partFile.toFile(), indicator);
            } catch (Throwable e) {
                if (e instanceof ControlFlowException) {
                    throw new RuntimeException(message("gmaven.wrapper.downloading.canceled"));
                }
            }
            try {
                FileUtil.rename(partFile.toFile(), zipFilePath.toFile(), FilePermissionCopier.BY_NIO2);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (!zipFilePath.toFile().isFile()) {
            throw new RuntimeException(message("gmaven.wrapper.downloading.cannot.download.zip.from", urlString));
        }
        var home = unpackZipFile(zipFilePath.toFile(), indicator);
        informationEvent(message("gmaven.wrapper.notification.downloading.finish"));
        return new WrapperDistribution(home.toPath(), urlString);
    }

    @Nonnull
    private static Path getZipFile(@Nonnull String distributionUrl) {
        var baseName = getDistName(distributionUrl);
        var distName = FileUtil.getNameWithoutExtension(baseName);
        var md5Hash = getMd5Hash(distributionUrl);

        return MavenUtils.resolveM2()
                .resolve("wrapper").resolve("dists")
                .resolve(distName)
                .resolve(md5Hash)
                .resolve(baseName);
    }

    @Nonnull
    private static String getDistName(String distUrl) {
        var position = distUrl.lastIndexOf("/");
        return (position < 0) ? distUrl : distUrl.substring(position + 1);
    }

    @Nonnull
    private static String getMd5Hash(String string) {
        try {
            var messageDigest = MessageDigest.getInstance("MD5");
            var bytes = string.getBytes(StandardCharsets.UTF_8);
            messageDigest.update(bytes);
            return new BigInteger(1, messageDigest.digest()).toString(32);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash input string.", e);
        }
    }

    @Nonnull
    private static File unpackZipFile(@Nonnull File zipFile, @Nullable ProgressIndicator indicator) {
        unzip(zipFile, indicator);
        var mavenHome = getMavenDir(zipFile);
        if (!SystemInfo.isWindows) {
            makeMavenBinRunnable(mavenHome);
        }
        return mavenHome;
    }

    @Nonnull
    private static File getMavenDir(@Nonnull File zipFile) {
        var dirs = requireNonNullElse(zipFile.getParentFile().listFiles(File::isDirectory), new File[0]);
        if (dirs.length != 1) {
            MavenLog.LOG.warn("Expected exactly 1 top level dir in Maven distribution, found: " + Arrays.toString(dirs));
            throw new IllegalStateException(message("gmaven.wrapper.zip.is.not.correct", zipFile.getAbsoluteFile()));
        }
        var mavenHome = dirs[0];
        if (!MavenUtils.isValidMavenHome(mavenHome)) {
            throw new IllegalStateException(message("gmaven.distribution.error", mavenHome));
        }
        return mavenHome;
    }

    @Nullable
    private static File getCurrentMavenWrapperPath(File zipFile) {
        var dirs = requireNonNullElse(zipFile.getParentFile().listFiles(File::isDirectory), new File[0]);
        if (dirs.length == 1 && MavenUtils.isValidMavenHome(dirs[0])) {
            return dirs[0];
        }
        return null;
    }

    private static void makeMavenBinRunnable(File mavenHome) {
        var mvnExe = new File(mavenHome, "bin/mvn");
        var permissions = PosixFilePermissions.fromString("rwxr-xr-x");
        try {
            Files.setPosixFilePermissions(mvnExe.toPath(), permissions);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void unzip(@Nonnull File zip, @Nullable ProgressIndicator indicator) {
        var unpackDir = zip.getParentFile();
        var destinationCanonicalPath = unpackDir.getAbsolutePath();
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var destFile = new File(unpackDir, entry.getName());
                var canonicalPath = destFile.getAbsolutePath();
                if (!canonicalPath.startsWith(destinationCanonicalPath)) {
                    FileUtil.delete(zip);
                    throw new RuntimeException(
                            "Directory traversal attack detected, zip file is malicious and IDEA dropped it"
                    );
                }
                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    destFile.getParentFile().mkdirs();
                    try (var it = new BufferedOutputStream(new FileOutputStream(destFile))) {
                        FileUtil.copy(zipFile.getInputStream(entry), it);
                    }
                }
            }
        } catch (Exception e) {
            if (indicator != null) {
                indicator.setTextValue(LocalizeValue.of(message("gmaven.wrapper.failure")));
            }
            File[] files = zip.getParentFile().listFiles(it -> !Objects.equals(it.getName(), zip.getName()));
            if (files != null) {
                for (File file : files) {
                    FileUtil.delete(file);
                }
            }
            throw new RuntimeException(e);
        }
    }

    private static void informationEvent(String content) {
        MavenNotification.getInstance().showBalloon(
                message("gmaven.wrapper.notification.title"),
                content,
                NotificationType.INFORMATION
        );
    }

    /*public static int copy(@Nonnull InputStream inputStream, @Nonnull OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        int total = 0;
        while ((read = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, read);
            total += read;
        }
        return total;
    }*/
}
