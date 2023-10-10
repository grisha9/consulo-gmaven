package consulo.gmaven.event.handler;

import consulo.gmaven.api.model.BuildErrors;
import consulo.gmaven.api.model.MavenResult;
import consulo.gmaven.event.handler.converter.MavenErrorConverter;
import consulo.gmaven.event.handler.converter.MavenProjectContainerConverter;
import consulo.gmaven.event.handler.converter.MavenSettingsConverter;
import consulo.gserver.result.ResultHolder;
import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;

import javax.inject.Named;


@Named
public class GMavenEventSpy extends AbstractEventSpy {
    private static EventSpyResultHolder resultHolder;

    @Override
    public void onEvent(Object event) {
        if (event instanceof DefaultSettingsBuildingRequest) {
            resultHolder = new EventSpyResultHolder();
        } else if (event instanceof SettingsBuildingResult) {
            resultHolder.settingsActiveProfiles = ((SettingsBuildingResult) event)
                    .getEffectiveSettings().getActiveProfiles();
        } else if (event instanceof ExecutionEvent) {
            if (((ExecutionEvent) event).getSession() != null) {
                resultHolder.session = ((ExecutionEvent) event).getSession();
            }
        } else if (event instanceof MavenExecutionResult) {
            resultHolder.executionResult = (MavenExecutionResult) event;
            setResult();
        }
    }

    private void setResult() {
        BuildErrors buildErrors = MavenErrorConverter.convert(resultHolder.executionResult);
        ResultHolder.result = new MavenResult(
                buildErrors.pluginNotResolved,
                MavenSettingsConverter.convert(resultHolder),
                MavenProjectContainerConverter.convert(resultHolder),
                buildErrors.exceptions
        );
    }
}
