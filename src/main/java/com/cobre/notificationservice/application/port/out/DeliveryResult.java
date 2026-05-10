package com.cobre.notificationservice.application.port.out;

public record DeliveryResult(
        Integer httpStatus,
        boolean success,
        boolean retryable,
        String errorMessage) {

    public static DeliveryResult success(int httpStatus) {
        return new DeliveryResult(httpStatus, true, false, null);
    }

    public static DeliveryResult retryableFailure(Integer httpStatus, String errorMessage) {
        return new DeliveryResult(httpStatus, false, true, errorMessage);
    }

    public static DeliveryResult permanentFailure(Integer httpStatus, String errorMessage) {
        return new DeliveryResult(httpStatus, false, false, errorMessage);
    }
}

