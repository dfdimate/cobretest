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
        try {
            HttpStatusCode statusCode = restClient.post()
                    .uri(URI.create(subscription.targetUrl()))
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
}

