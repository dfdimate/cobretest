package com.cobre.notificationservice.infrastructure.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.cobre.notificationservice.application.usecase.GetNotificationEventDetailUseCase;
import com.cobre.notificationservice.application.usecase.ListNotificationEventsUseCase;
import com.cobre.notificationservice.application.usecase.ReplayFailedNotificationUseCase;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import com.cobre.notificationservice.infrastructure.security.DemoClientIdentityResolver;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = NotificationEventController.class)
@Import(ApiExceptionHandler.class)
class NotificationEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListNotificationEventsUseCase listNotificationEventsUseCase;

    @MockBean
    private GetNotificationEventDetailUseCase getNotificationEventDetailUseCase;

    @MockBean
    private ReplayFailedNotificationUseCase replayFailedNotificationUseCase;

    @MockBean
    private DemoClientIdentityResolver demoClientIdentityResolver;

    @Test
    void shouldListNotificationEventsForAuthenticatedClient() throws Exception {
        NotificationEvent notificationEvent = NotificationEvent.restore(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"),
                com.cobre.notificationservice.domain.model.value.DeliveryStatus.COMPLETED,
                1,
                Instant.parse("2024-03-15T09:31:10Z"),
                Instant.parse("2024-03-15T09:31:10Z"),
                null,
                200,
                null);

        when(demoClientIdentityResolver.resolveClientId(any())).thenReturn(new ClientId("CLIENT001"));
        when(listNotificationEventsUseCase.list(any(), any(), any(), any())).thenReturn(List.of(notificationEvent));

        mockMvc.perform(get("/notification_events").header("X-Client-Id", "CLIENT001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].notification_event_id").value("EVT001"))
                .andExpect(jsonPath("$.items[0].delivery_status").value("COMPLETED"));
    }

    @Test
    void shouldReplayFailedNotification() throws Exception {
        NotificationEvent notificationEvent = NotificationEvent.pending(
                new NotificationEventId("EVT003"),
                new SourceEventId("EVT003"),
                new ClientId("CLIENT002"),
                new EventType("credit_transfer"),
                "Bank transfer received from Account #4567 for $1,500.00",
                Instant.parse("2024-03-15T11:20:18Z"),
                Instant.parse("2024-03-15T11:20:19Z"));

        when(demoClientIdentityResolver.resolveClientId(any())).thenReturn(new ClientId("CLIENT002"));
        when(replayFailedNotificationUseCase.replay(new NotificationEventId("EVT003"), new ClientId("CLIENT002")))
                .thenReturn(notificationEvent);

        mockMvc.perform(post("/notification_events/EVT003/replay").header("X-Client-Id", "CLIENT002"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.notification_event_id").value("EVT003"))
                .andExpect(jsonPath("$.delivery_status").value("PENDING"));
    }
}
