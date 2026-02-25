package com.posdb.sync.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class JwtProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtProvider.class);

    @ConfigProperty(name = "quarkus.smallrye-jwt.verify.issuer", defaultValue = "pos-db-sync")
    String issuer;

    @ConfigProperty(name = "quarkus.smallrye-jwt.audience", defaultValue = "pos-mobile-app")
    String audience;

    public String generateToken(UUID userId, UUID restaurantId, String role) {
        LOGGER.info("Generating JWT token for userId: {}, restaurantId: {}", userId, restaurantId);

        try {
            String token = Jwt.issuer(issuer)
                    .audience(audience)
                    .subject(userId.toString())
                    .claim("user_id", userId.toString())
                    .claim("restaurant_id", restaurantId.toString())
                    .claim("role", role)
                    .expiresAt(Instant.now().getEpochSecond() + 86400) // 24 hours
                    .sign();

            LOGGER.info("JWT token generated successfully for userId: {}", userId);
            return token;
        } catch (Exception e) {
            LOGGER.error("Error generating JWT token", e);
            throw new RuntimeException("Failed to generate token", e);
        }
    }
}

