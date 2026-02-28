package com.posdb.sync.service;

import com.posdb.sync.dto.request.LoginRequest;
import com.posdb.sync.entity.User;
import com.posdb.sync.exception.AppException;
import com.posdb.sync.utils.PasswordUtil;
import com.posdb.sync.utils.TextUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    PasswordUtil passwordUtil;

    @Inject
    JwtProvider jwtProvider;
    

    public String login(LoginRequest request) {
        try {
            log.info("AuthService:: Login attempt for getEmail(): {}", request.getEmail());

            if (TextUtil.isEmpty(request.getEmail()) || TextUtil.isEmpty(request.getPassword())) {
                log.warn("AuthService:: Login failed - missing Email or Password");
                throw new AppException("Email and Password are required", Response.Status.BAD_REQUEST);
            }

            User user = User.<User>find("email", request.getEmail())
                    .firstResultOptional().orElse(null);

            if (user == null || passwordUtil.verifyPassword(request.getPassword(), user.getPasswordHash())) {
                log.warn("AuthService:: Login failed - invalid credentials for email: {}", request.getEmail());
                throw new AppException("Invalid email or password", Response.Status.UNAUTHORIZED);
            }

            String token = jwtProvider.generateToken(user);

            log.info("AuthService:: Login successful for email: {} with userId: {}", request.getEmail(), user.getId());
            return token;
        } catch (Exception e) {
            log.error("AuthService:: Error during login. ", e);
            throw new AppException("An error occurred during login", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
