package consulo.gmaven.api.model;


import java.io.Serializable;
import java.util.List;

public final class PluginExecution implements Serializable {
    public final String id;
    public final String phase;
    public final List<String> goals;
    public final String configuration;

    public PluginExecution(String id, String phase, List<String> goals, String configuration) {
        this.id = id;
        this.phase = phase;
        this.goals = goals;
        this.configuration = configuration;
    }
}
