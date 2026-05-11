package com.cobre.notificationservice.infrastructure.ingestion;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cobre.notificationservice.application.usecase.CreateNotificationFromSourceEventUseCase;
import com.cobre.notificationservice.domain.model.SourceEvent;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;

class StartupNotificationEventsImporterTest {

    @Test
    void shouldImportAllLoadedEventsOnStartup() throws Exception {
        NotificationEventsFileLoader fileLoader = mock(NotificationEventsFileLoader.class);
        CreateNotificationFromSourceEventUseCase useCase = mock(CreateNotificationFromSourceEventUseCase.class);
        StartupNotificationEventsImporter importer = new StartupNotificationEventsImporter(fileLoader, useCase);
        SourceEvent first = new SourceEvent(
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"));
        SourceEvent second = new SourceEvent(
                new SourceEventId("EVT002"),
                new ClientId("CLIENT001"),
                new EventType("debit_card_withdrawal"),
                "ATM withdrawal of $200.00",
                Instant.parse("2024-03-15T10:15:45Z"));
        when(fileLoader.load()).thenReturn(List.of(first, second));

        importer.run(mock(ApplicationArguments.class));

        verify(useCase).handle(first);
        verify(useCase).handle(second);
        Mockito.verifyNoMoreInteractions(useCase);
    }
}
