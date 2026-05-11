package com.cobre.notificationservice.infrastructure.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.cobre.notificationservice.application.usecase.NotificationEventNotFoundException;
import com.cobre.notificationservice.application.usecase.ReplayNotAllowedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldReturnNotFoundResponse() {
        var response = handler.handleNotFound(new NotificationEventNotFoundException("EVT001"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("message", "Notification event not found: EVT001");
    }

    @Test
    void shouldReturnConflictResponse() {
        var response = handler.handleReplayConflict(new ReplayNotAllowedException("EVT002"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("message", "Replay not allowed for notification event: EVT002");
    }

    @Test
    void shouldReturnBadRequestResponse() {
        var response = handler.handleBadRequest(new IllegalArgumentException("bad request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "bad request");
    }
}
