package com.cobre.notificationservice.infrastructure.security;

import com.cobre.notificationservice.domain.model.value.ClientId;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class DemoClientIdentityResolver {

    public ClientId resolveClientId(HttpServletRequest request) {
        String clientId = request.getHeader("X-Client-Id");

        if (clientId == null || clientId.isBlank()) {
            throw new IllegalArgumentException("Missing X-Client-Id header");
        }

        return new ClientId(clientId);
    }
}

