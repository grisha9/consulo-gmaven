package consulo.gmaven.notfication;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.gmaven.Constants;
import consulo.ide.ServiceManager;
import consulo.ide.impl.idea.ui.AppUIUtil;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationType;
import consulo.project.ui.notification.event.NotificationListener;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
@Singleton
public class MavenNotification {
    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup(
            Constants.SYSTEM_ID.getId(), Constants.SYSTEM_ID.getDisplayName()
    );

    @Nonnull
    public static MavenNotification getInstance() {
        return ServiceManager.getService(MavenNotification.class);
    }

    public void showBalloon(@Nonnull final String title,
                            @Nonnull final String message,
                            @Nonnull final NotificationType type) {
        showBalloon(title, message, type, null);
    }

    /*public void showBalloon(@Nonnull final String title,
                            @Nonnull final String message,
                            @Nonnull final NotificationType type,
                            @Nullable final NotificationListener listener) {
        AppUIUtil.invokeLaterIfProjectAlive(myProject,
                () -> NOTIFICATION_GROUP.createNotification(title, message, type, listener)
                        .notify(myProject));
    }*/

    public void showBalloon(@Nonnull final String title,
                            @Nonnull final String message,
                            @Nonnull final NotificationType type,
                            @Nullable final NotificationListener listener) {
        AppUIUtil.invokeOnEdt(() -> NOTIFICATION_GROUP.createNotification(title, message, type, listener)
                        .notify());
    }
}

