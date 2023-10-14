package consulo.gmaven.notfication;

import consulo.annotation.component.ExtensionImpl;
import consulo.project.ui.notification.NotificationGroup;
import consulo.project.ui.notification.NotificationGroupContributor;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@ExtensionImpl
public class MavenNotificationContributor implements NotificationGroupContributor {
  @Override
  public void contribute(@Nonnull Consumer<NotificationGroup> consumer) {
    consumer.accept(MavenNotification.NOTIFICATION_GROUP);
  }
}
