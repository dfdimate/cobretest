package com.cobre.notificationservice.infrastructure.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class SystemClockAdapterTest {

    @Test
    void shouldReturnCurrentInstant() {
        SystemClockAdapter adapter = new SystemClockAdapter();
        Instant before = Instant.now().minusSeconds(1);

        Instant now = adapter.now();

        assertThat(now).isAfter(before);
    }
}
