package com.cobre.notificationservice.application.port.out;

import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.Subscription;

public interface WebhookDeliveryPort {

    DeliveryResult deliver(NotificationEvent notificationEvent, Subscription subscription);
}

