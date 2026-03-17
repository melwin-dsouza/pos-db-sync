//package com.posdb.sync.filter;
//
//import jakarta.annotation.Priority;
//import jakarta.ws.rs.container.ContainerRequestContext;
//import jakarta.ws.rs.container.ContainerRequestFilter;
//import jakarta.ws.rs.ext.Provider;
//import lombok.extern.slf4j.Slf4j;
//
//import java.io.IOException;
//import java.util.Map;
//import jakarta.ws.rs.core.Cookie;
//
//@Provider
//@Slf4j
//@Priority(1000)
//public class CookieTokenFilter implements ContainerRequestFilter {
//
//    private static final String AUTH_COOKIE_NAME = "auth_token";
//    private static final String AUTHORIZATION_HEADER = "Authorization";
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//        // If Authorization header already present (mobile / Bearer), skip — don't override
//        String existingAuth = requestContext.getHeaderString(AUTHORIZATION_HEADER);
//        if (existingAuth != null && !existingAuth.isBlank()) {
//            log.debug("CookieTokenFilter:: Authorization header already present, skipping cookie injection");
//            return;
//        }
//
//        // Check for auth_token cookie (web clients)
//        Map<String, Cookie> cookies = requestContext.getCookies();
//        Cookie authCookie = cookies.get(AUTH_COOKIE_NAME);
//
//        if (authCookie != null && authCookie.getValue() != null && !authCookie.getValue().isBlank()) {
//            log.debug("CookieTokenFilter:: Injecting JWT from cookie into Authorization header for path: {}",
//                    requestContext.getUriInfo().getPath());
//            // Rewrite as Bearer so Quarkus SmallRye JWT picks it up and sets SecurityIdentity
//            requestContext.getHeaders().putSingle(AUTHORIZATION_HEADER, "Bearer " + authCookie.getValue());
//        }
//    }
//}
//
