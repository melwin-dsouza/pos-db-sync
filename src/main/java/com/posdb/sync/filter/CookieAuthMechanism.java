package com.posdb.sync.filter;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
@Priority(100) //
public class CookieAuthMechanism implements HttpAuthenticationMechanism {

    private static final String AUTH_COOKIE_NAME = "auth_token";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        // If Bearer header already present, let default JWT mechanism handle it
        String authHeader = context.request().getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return Uni.createFrom().nullItem();
        }

        // Read cookie and inject as Authorization header
        io.vertx.core.http.Cookie cookie = context.request().getCookie(AUTH_COOKIE_NAME);
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
            log.debug("CookieAuthMechanism:: Injecting JWT from cookie");
            context.request().headers().set(AUTHORIZATION_HEADER, "Bearer " + cookie.getValue());
        }

        return Uni.createFrom().nullItem(); // let SmallRye JWT mechanism take over
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().nullItem();
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return Uni.createFrom().nullItem();
    }
}