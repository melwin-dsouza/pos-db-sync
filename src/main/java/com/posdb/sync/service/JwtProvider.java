package com.posdb.sync.service;

import com.posdb.sync.entity.User;
import com.posdb.sync.exception.AppException;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class JwtProvider {

    @ConfigProperty(name = "quarkus.smallrye-jwt.verify.issuer", defaultValue = "pos-db-sync")
    String issuer;

    @ConfigProperty(name = "quarkus.smallrye-jwt.audience", defaultValue = "pos-mobile-app")
    String audience;

    @ConfigProperty(name = "quarkus.smallrye-jwt.expiry", defaultValue = "86400")
    Integer expireInSeconds;

    public String generateToken(User user) {
        log.info("JwtProvider:: Generating JWT token for userId: {}, restaurantId: {}", user.getEmail(), user.getRestaurant().getName());

        try {
            String token = Jwt.issuer(issuer)
                    .audience(audience)
                    .subject(user.getEmail())
                    .claim("user_name", user.getFullName())
                    .claim("restaurant_name", user.getRestaurant().getName())
                    .claim("role", user.getRole().name())
                    .expiresAt(Instant.now().getEpochSecond() + expireInSeconds) // 24 hours
                    .sign();

            log.info("JwtProvider::JWT token generated successfully for user: {}", user);
            return token;
        } catch (Exception e) {
            log.error("JwtProvider::Error generating JWT token", e);
            throw new AppException("Failed to generate token", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}

