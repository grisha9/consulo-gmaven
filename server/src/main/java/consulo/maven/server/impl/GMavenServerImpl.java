package consulo.maven.server.impl;

import consulo.gmaven.api.GMavenServer;
import consulo.gmaven.api.GServerUtils;
import consulo.gmaven.api.model.MavenResult;
import consulo.gmaven.api.model.request.GetModelRequest;
import consulo.maven.server.result.ResultHolder;
import org.codehaus.plexus.classworlds.launcher.Launcher;

import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class GMavenServerImpl implements GMavenServer {

    @Override
    public MavenResult getProjectModel(GetModelRequest request) throws RemoteException {
        fillSystemProperties(request);
        try {
            Launcher.mainWithExitCode(getMvnArgs(request));
            return GServerUtils.toResult(ResultHolder.result);
        } catch (Exception e) {
            return GServerUtils.toResult(e);
        }
    }

    private static String[] getMvnArgs(GetModelRequest request) {
        List<String> mvnArgs = new ArrayList<>();
        if (!isEmpty(request.alternativePom)) {
            mvnArgs.add("-f");
            mvnArgs.add(request.alternativePom);
        }
        if (request.nonRecursion) {
            mvnArgs.add("-N");
        }
        if (request.offline) {
            mvnArgs.add("-o");
        }
        if (!isEmpty(request.profiles)) {
            mvnArgs.add("-P");
            mvnArgs.add(request.profiles);
        }
        if (!isEmpty(request.threadCount)) {
            mvnArgs.add("-T");
            mvnArgs.add(request.threadCount);
        }
        if (request.quiteLogs) {
            mvnArgs.add("-q");
        }
        if (request.debugLog) {
            mvnArgs.add("-X");
        }
        if (request.updateSnapshots) {
            mvnArgs.add("-U");
        }
        if (request.notUpdateSnapshots) {
            mvnArgs.add("-nsu");
        }
        if (!isEmpty(request.dependencyAnalyzerGA)) {
            mvnArgs.add("-D" + GMAVEN_DEPENDENCY_TREE + "=true");
            if (!request.dependencyAnalyzerGA.equals(RESOLVE_TASK)) {
                mvnArgs.add("-pl");
                mvnArgs.add(request.dependencyAnalyzerGA);
                mvnArgs.add("-Daether.conflictResolver.verbose=true");
                mvnArgs.add("-Daether.dependencyManager.verbose=true");
                mvnArgs.add("-am");
            }
        } else if (!isEmpty(request.projectList)) {
            mvnArgs.add("-pl");
            mvnArgs.add(request.projectList);
            mvnArgs.add("-am");
            mvnArgs.add("-amd");
        }
        if (request.additionalArguments != null && !request.additionalArguments.isEmpty()) {
            mvnArgs.addAll(request.additionalArguments);
        }
        if (request.importArguments != null && !request.importArguments.isEmpty()) {
            mvnArgs.addAll(request.importArguments);
        }
        if (!isEmpty(request.gMavenPluginPath)) {
            mvnArgs.add("install:install-file");
            mvnArgs.add("-Dfile=" + request.gMavenPluginPath);
            mvnArgs.add("-DgroupId=ru.rzn.gmyasoedov");
            mvnArgs.add("-DartifactId=model-reader");
            mvnArgs.add("-Dversion=1.0-SNAPSHOT");
            mvnArgs.add("-Dpackaging=jar");
        } else if (request.tasks != null && !request.tasks.isEmpty()) {
            mvnArgs.addAll(request.tasks);
        } else {
            mvnArgs.add(RESOLVE_TASK);
        }
        System.out.println("mvn: " + mvnArgs);
        return mvnArgs.toArray(new String[0]);
    }

    private static void fillSystemProperties(GetModelRequest request) {
        String mavenHome = System.getProperty(GMavenServer.GMAVEN_HOME);
        String extClasspath = System.getProperty(GMavenServer.MAVEN_EXT_CLASS_PATH_PROPERTY);
        if (mavenHome == null) throw new RuntimeException("no maven home path");
        if (extClasspath == null) throw new RuntimeException("no maven ext class path");
        String projectPath = request.projectPath;

        System.setProperty("classworlds.conf", Paths.get(mavenHome, "bin", "m2.conf").toString());
        System.setProperty("maven.home", mavenHome);
        System.setProperty("library.jansi.path", Paths.get(mavenHome, "lib", "jansi-native").toString());
        System.setProperty("maven.multiModuleProjectDirectory", projectPath);
        System.setProperty("user.dir", projectPath);
    }

    private static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
