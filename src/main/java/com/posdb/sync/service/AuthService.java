package com.posdb.sync.service;

import com.posdb.sync.dto.request.LoginRequest;
import com.posdb.sync.dto.response.SessionResponse;
import com.posdb.sync.entity.User;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.utils.PasswordUtil;
import com.posdb.sync.utils.TextUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    JwtProvider jwtProvider;

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    JsonWebToken jwt;

    public String login(LoginRequest request) {
        try {
            log.info("AuthService:: Login attempt for getEmail(): {}", request.getEmail());

            if (TextUtil.isEmpty(request.getEmail()) || TextUtil.isEmpty(request.getPassword())) {
                log.warn("AuthService:: Login failed - missing Email or Password");
                throw new AppException("Email and Password are required", Response.Status.BAD_REQUEST);
            }

            User user = User.<User>find("email", request.getEmail())
                    .firstResultOptional().orElse(null);

            if (user == null || !passwordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                log.warn("AuthService:: Login failed - invalid credentials for email: {}", request.getEmail());
                throw new AppException("Invalid email or password", Response.Status.UNAUTHORIZED);
            }
            if(Boolean.TRUE.equals(user.getMustChangePassword())){
                log.warn("AuthService:: Login failed - user must change password for email: {}", request.getEmail());
                throw new AppException("User must change password", Response.Status.FORBIDDEN);
            }

            String token = jwtProvider.generateToken(user);

            log.info("AuthService:: Login successful for email: {} with userId: {}", request.getEmail(), user.getId());
            return token;
        } catch (AppException e){
            throw e;
        } catch (Exception e) {
            log.error("AuthService:: Error during login. ", e);
            throw new AppException("An error occurred during login", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Validates the current session (cookie or Bearer token) and returns claims.
     * Used by web clients on PWA restart to check if session is still valid.
     */
    public SessionResponse getSessionInfo() {
        try {
            if (securityIdentity == null || securityIdentity.isAnonymous()) {
                log.warn("AuthService:: Session validation failed - anonymous identity");
                throw new AppException("Session is invalid or expired", Response.Status.UNAUTHORIZED);
            }

            String email = securityIdentity.getPrincipal().getName();
            String fullName = jwt.getClaim("user_name");
            String restaurantName = jwt.getClaim("restaurant_name");
            String role = jwt.getClaim("role");
            List<String> restaurants = jwt.getClaim("restaurants");

            log.info("AuthService:: Session validation successful for email: {}", email);

            return new SessionResponse(
                    email,
                    fullName,
                    role,
                    restaurantName,
                    restaurants,
                    true
            );
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("AuthService:: Error validating session", e);
            throw new AppException("Session validation failed", Response.Status.UNAUTHORIZED);
        }
    }
}
