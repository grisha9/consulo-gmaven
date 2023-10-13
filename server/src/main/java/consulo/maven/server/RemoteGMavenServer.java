package consulo.maven.server;

import consulo.maven.server.impl.GMavenServerImpl;
import consulo.util.rmi.RemoteServer;

public class RemoteGMavenServer extends RemoteServer {

    public static void main(String[] args) throws Exception {
        start(new GMavenServerImpl());
    }
}

