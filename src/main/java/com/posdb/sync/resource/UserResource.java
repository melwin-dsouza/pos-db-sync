package com.posdb.sync.resource;

import com.posdb.sync.dto.request.ChangePasswordRequest;
import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.service.UserService;
import com.posdb.sync.utils.PasswordUtil;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/v1/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class UserResource {

    @Inject
    UserService userService;

    @POST
    @Path("/change-password")
    @RolesAllowed({"OWNER", "MANAGER", "STAFF"})
    public Response changePassword(ChangePasswordRequest request) {
        log.info("UserResource:: Change password request received");
        userService.changePassword(request);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, "Success"))
                .build();
    }
}

