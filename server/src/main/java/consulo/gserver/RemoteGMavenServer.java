package consulo.gserver;


import consulo.gserver.impl.GMavenServerImpl;
import consulo.util.rmi.RemoteServer;


public class RemoteGMavenServer extends RemoteServer {

    public static void main(String[] args) throws Exception {
        start(new GMavenServerImpl());
    }
}

