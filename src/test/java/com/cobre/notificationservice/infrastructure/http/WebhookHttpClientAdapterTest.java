package com.cobre.notificationservice.infrastructure.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.cobre.notificationservice.application.port.out.DeliveryResult;
import com.cobre.notificationservice.domain.model.NotificationEvent;
import com.cobre.notificationservice.domain.model.Subscription;
import com.cobre.notificationservice.domain.model.value.ClientId;
import com.cobre.notificationservice.domain.model.value.EventType;
import com.cobre.notificationservice.domain.model.value.NotificationEventId;
import com.cobre.notificationservice.domain.model.value.SourceEventId;
import java.net.URI;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

class WebhookHttpClientAdapterTest {

    @Test
    void shouldRejectNonHttpsWebhookUrls() {
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(RestClient.builder());

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("http://client.example.com/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Webhook URL is not allowed");
    }

    @Test
    void shouldRejectLocalWebhookUrls() {
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(RestClient.builder());

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://localhost/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isFalse();
        assertThat(result.errorMessage()).isEqualTo("Webhook URL is not allowed");
    }

    @Test
    void shouldReturnSuccessFor2xxResponses() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://client.example.com/webhook"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(builder);

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://client.example.com/webhook"));

        assertThat(result.success()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(204);
        server.verify();
    }

    @Test
    void shouldReturnRetryableFailureFor5xxResponses() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://client.example.com/webhook"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(builder);

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://client.example.com/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(503);
        server.verify();
    }

    @Test
    void shouldReturnRetryableFailureForTooManyRequests() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://client.example.com/webhook"))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(builder);

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://client.example.com/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isTrue();
        assertThat(result.httpStatus()).isEqualTo(429);
        server.verify();
    }

    @Test
    void shouldReturnPermanentFailureForRejectedRequests() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://client.example.com/webhook"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(builder);

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://client.example.com/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isFalse();
        assertThat(result.httpStatus()).isEqualTo(400);
        assertThat(result.errorMessage()).isEqualTo("Webhook request rejected");
        server.verify();
    }

    @Test
    void shouldReturnRetryableFailureWhenHttpClientThrows() {
        RestClient.Builder builder = mock(RestClient.Builder.class);
        RestClient restClient = mock(RestClient.class);
        RestClient.RequestBodyUriSpec requestSpec = mock(RestClient.RequestBodyUriSpec.class);
        when(builder.build()).thenReturn(restClient);
        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri(any(URI.class))).thenReturn(requestSpec);
        when(requestSpec.body(anyString())).thenReturn(requestSpec);
        when(requestSpec.exchange(any())).thenThrow(new ResourceAccessException("boom"));
        WebhookHttpClientAdapter adapter = new WebhookHttpClientAdapter(builder);

        DeliveryResult result = adapter.deliver(notificationEvent(), subscription("https://client.example.com/webhook"));

        assertThat(result.success()).isFalse();
        assertThat(result.retryable()).isTrue();
        assertThat(result.httpStatus()).isNull();
        assertThat(result.errorMessage()).contains("boom");
    }

    private NotificationEvent notificationEvent() {
        return NotificationEvent.pending(
                new NotificationEventId("EVT001"),
                new SourceEventId("EVT001"),
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                "Credit card payment received for $150.00",
                Instant.parse("2024-03-15T09:30:22Z"),
                Instant.parse("2024-03-15T09:30:23Z"));
    }

    private Subscription subscription(String targetUrl) {
        return new Subscription(
                new ClientId("CLIENT001"),
                new EventType("credit_card_payment"),
                targetUrl,
                true);
    }
}
