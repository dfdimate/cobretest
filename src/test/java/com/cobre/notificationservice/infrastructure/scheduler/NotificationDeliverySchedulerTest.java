package com.cobre.notificationservice.infrastructure.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.cobre.notificationservice.application.usecase.RetryDueNotificationsUseCase;
import org.junit.jupiter.api.Test;

class NotificationDeliverySchedulerTest {

    @Test
    void shouldTriggerDueNotificationProcessing() {
        RetryDueNotificationsUseCase useCase = mock(RetryDueNotificationsUseCase.class);
        NotificationDeliveryScheduler scheduler = new NotificationDeliveryScheduler(useCase);

        scheduler.deliverDueNotifications();

        verify(useCase).processDueNotifications();
    }
}
