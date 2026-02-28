package com.posdb.sync.filter;

import com.posdb.sync.dto.response.ApiResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Base64;

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@Provider
@ApplicationScoped
public class BasicAuthFilter implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthFilter.class);

    @ConfigProperty(name = "admin.username")
    String adminUsername;

    @ConfigProperty(name = "admin.password.hash")
    String adminPasswordHash;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Only apply to admin endpoints
        if (path.startsWith("/api/v1/admin")) {
            String authHeader = requestContext.getHeaderString("Authorization");

            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                LOGGER.warn("Missing or invalid Basic Auth header for admin endpoint: {}", path);
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ApiResponse<>(401, null, "Missing or invalid Basic Auth header")).build());
                return;
            }

            try {
                String encodedAuth = authHeader.substring(6);
                String decodedAuth = new String(Base64.getDecoder().decode(encodedAuth));
                String[] parts = decodedAuth.split(":");

                if (parts.length != 2) {
                    LOGGER.warn("Invalid Basic Auth format for admin endpoint: {}", path);
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ApiResponse<>(401, null, "Invalid Basic Auth format")).build());
                    return;
                }
                String username = parts[0];
                String password = parts[1];

                if (!adminUsername.equals(username) || !BCrypt.checkpw(password, adminPasswordHash)) {
                    LOGGER.warn("Invalid admin credentials attempted for endpoint: {}", path);
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ApiResponse<>(401, null, "Invalid credentials")).build());
                    return;
                }
                LOGGER.info("Admin authentication successful for endpoint: {}", path);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Error decoding Basic Auth header", e);
                requestContext.abortWith(
                    Response.status(Response.Status.UNAUTHORIZED)
                            .entity(new ApiResponse<>(401, null, "Error decoding credentials"))
                            .build());
            }
        }
        else if(path.startsWith("/api/v1/pos")) {
            // For POS endpoints, we can add additional checks if needed (e.g., API key validation)
            String apiKey = requestContext.getHeaderString(API_KEY);

            if (apiKey == null || apiKey.isEmpty()) {
                LOGGER.warn("Missing API Key for POS endpoint: {}", path);
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ApiResponse<>(401, null, "Missing API Key")).build());
                return;
            }

            LOGGER.info("POS endpoint accessed: {}", path);
        }
    }
}

