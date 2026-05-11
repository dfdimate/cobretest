package com.cobre.notificationservice.infrastructure.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class NotificationEventsFileLoaderTest {

    @Test
    void shouldLoadSourceEventsFromClasspathJson() {
        NotificationEventsFileLoader loader = new NotificationEventsFileLoader(new ObjectMapper());

        var events = loader.load();

        assertThat(events).hasSize(10);
        assertThat(events.getFirst().sourceEventId().value()).isEqualTo("EVT001");
        assertThat(events.getFirst().clientId().value()).isEqualTo("CLIENT001");
        assertThat(events.getFirst().eventType().value()).isEqualTo("credit_card_payment");
    }

    @Test
    void shouldWrapIoExceptionsWhenLoadingFile() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.readValue(any(InputStream.class), any(Class.class)))
                .thenThrow(new IOException("broken payload"));
        NotificationEventsFileLoader loader = new NotificationEventsFileLoader(objectMapper);

        assertThatThrownBy(loader::load)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Unable to load notification_events.json")
                .hasCauseInstanceOf(IOException.class);
    }
}
