package com.cobre.notificationservice.infrastructure.ingestion;

import com.cobre.notificationservice.application.usecase.CreateNotificationFromSourceEventUseCase;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupNotificationEventsImporter implements ApplicationRunner {

    private final NotificationEventsFileLoader fileLoader;
    private final CreateNotificationFromSourceEventUseCase useCase;

    public StartupNotificationEventsImporter(
            NotificationEventsFileLoader fileLoader,
            CreateNotificationFromSourceEventUseCase useCase) {
        this.fileLoader = fileLoader;
        this.useCase = useCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        fileLoader.load().forEach(useCase::handle);
    }
}
