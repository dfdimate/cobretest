# Notification Service

Spring Boot project for asynchronous webhook delivery and a self-service notification events API.

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
