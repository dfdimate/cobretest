# Notification Service

Spring Boot project for asynchronous webhook delivery and a self-service notification events API.

## System design

```mermaid
flowchart LR
    A["notification_events.json"] --> B["Startup importer"]
    B --> C["Create notification event"]
    C --> D[("PostgreSQL")]

    E["Client REST calls"] --> F["Self-service API"]
    F --> D

    G["Scheduler"] --> H["Retry due notifications"]
    H --> D
    H --> I["Webhook delivery adapter"]
    I --> J["Client webhook endpoint"]
    I --> K["Structured logs and metrics"]
    I --> D
```

The service loads source events at startup, persists them as notification delivery records, exposes a self-service API for querying and replaying them, and processes webhook delivery asynchronously with retries backed by PostgreSQL.

## Local development

Start PostgreSQL:

```bash
docker compose up -d postgres
```

Note: Docker Desktop must be running before the compose command can start the PostgreSQL container.

Run the application:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```
