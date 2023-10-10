package consulo.gmaven.event.handler;

import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;

import java.util.List;

public class EventSpyResultHolder {
    public MavenSession session;
    public MavenExecutionResult executionResult;
    public List<String> settingsActiveProfiles;
}
