package com.cobre.notificationservice.application.port.out;

import com.cobre.notificationservice.domain.model.NotificationEvent;

public interface ObservabilityPort {

    void recordCompleted(NotificationEvent notificationEvent);

    void recordRetryableFailure(NotificationEvent notificationEvent);

    void recordFailed(NotificationEvent notificationEvent);
}
