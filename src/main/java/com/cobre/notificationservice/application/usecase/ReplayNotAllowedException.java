package com.cobre.notificationservice.application.usecase;

public class ReplayNotAllowedException extends RuntimeException {

    public ReplayNotAllowedException(String notificationEventId) {
        super("Replay not allowed for notification event: " + notificationEventId);
    }
}

