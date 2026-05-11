package com.cobre.notificationservice.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class DemoClientIdentityResolverTest {

    private final DemoClientIdentityResolver resolver = new DemoClientIdentityResolver();

    @Test
    void shouldResolveClientIdFromHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-Id", "CLIENT001");

        var clientId = resolver.resolveClientId(request);

        assertThat(clientId.value()).isEqualTo("CLIENT001");
    }

    @Test
    void shouldRejectMissingClientIdHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> resolver.resolveClientId(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing X-Client-Id header");
    }

    @Test
    void shouldRejectBlankClientIdHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Client-Id", "   ");

        assertThatThrownBy(() -> resolver.resolveClientId(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Missing X-Client-Id header");
    }
}
