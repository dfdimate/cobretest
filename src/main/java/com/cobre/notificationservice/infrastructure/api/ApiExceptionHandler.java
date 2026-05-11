package com.cobre.notificationservice.infrastructure.api;

import com.cobre.notificationservice.application.usecase.NotificationEventNotFoundException;
import com.cobre.notificationservice.application.usecase.ReplayNotAllowedException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotificationEventNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotificationEventNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(ReplayNotAllowedException.class)
    public ResponseEntity<Map<String, String>> handleReplayConflict(ReplayNotAllowedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}
