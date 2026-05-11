package com.cobre.notificationservice.infrastructure.http;

import com.cobre.notificationservice.application.port.out.DeliveryResult;
import com.cobre.notificationservice.application.port.out.WebhookDeliveryPort;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.Subscription;
import java.net.URI;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WebhookHttpClientAdapter implements WebhookDeliveryPort {

    private final RestClient restClient;

    public WebhookHttpClientAdapter(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public DeliveryResult deliver(NotificationEvent notificationEvent, Subscription subscription) {
        URI targetUri = URI.create(subscription.targetUrl());

        if (!isAllowedWebhookUri(targetUri)) {
            return DeliveryResult.permanentFailure(null, "Webhook URL is not allowed");
        }

        try {
            HttpStatusCode statusCode = restClient.post()
                    .uri(targetUri)
                    .body(notificationEvent.content())
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode();

            if (statusCode.is2xxSuccessful()) {
                return DeliveryResult.success(statusCode.value());
            }
            if (statusCode.is5xxServerError() || statusCode.value() == 429) {
                return DeliveryResult.retryableFailure(statusCode.value(), "Webhook endpoint unavailable");
            }
            return DeliveryResult.permanentFailure(statusCode.value(), "Webhook request rejected");
        } catch (RestClientException exception) {
            return DeliveryResult.retryableFailure(null, exception.getMessage());
        }
    }

    private boolean isAllowedWebhookUri(URI uri) {
        String scheme = uri.getScheme();
        String host = uri.getHost();

        if (!"https".equalsIgnoreCase(scheme) || host == null || host.isBlank()) {
            return false;
        }

        String normalizedHost = host.toLowerCase();
        if ("localhost".equals(normalizedHost) || "::1".equals(normalizedHost)) {
            return false;
        }

        return !normalizedHost.startsWith("127.")
                && !normalizedHost.startsWith("10.")
                && !normalizedHost.startsWith("192.168.")
                && !normalizedHost.matches("^172\\.(1[6-9]|2\\d|3[0-1])\\..*");
    }
}
