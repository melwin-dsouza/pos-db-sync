package com.posdb.sync.resource;

import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.SyncResponse;
import com.posdb.sync.dto.sync.MenuItemSyncRequest;
import com.posdb.sync.service.MenuItemSyncService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import static com.posdb.sync.dto.constants.AppConstants.API_KEY;

@Path("/api/v1/pos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class MenuItemSyncResource {

    @Inject
    MenuItemSyncService menuItemSyncService;

    @POST
    @Path("/menuitems/sync")
    public Response syncMenuItems(MenuItemSyncRequest request, @Context HttpHeaders headers) {
        log.info("MenuItemSyncResource:: Received Sync MENU_ITEMS request: {}", request);
        String apiKey = headers.getHeaderString(API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("MenuItemSyncResource:: Missing API Key in headers");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(400, null, "Missing API Key in headers"))
                    .build();
        }

        SyncResponse syncResponse = menuItemSyncService.syncMenuItems(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }

    @POST
    @Path("/menuitems/full-sync")
    public Response fullSyncMenuItems(MenuItemSyncRequest request, @Context HttpHeaders headers) {
        log.info("MenuItemSyncResource:: Received Full Sync MENU_ITEMS request: {}", request);
        String apiKey = headers.getHeaderString(API_KEY);
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("MenuItemSyncResource:: Missing API Key in headers.");
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ApiResponse<>(400, null, "Missing API Key in headers"))
                    .build();
        }

        SyncResponse syncResponse = menuItemSyncService.fullSyncMenuItems(request, headers);
        return Response.status(Response.Status.OK)
                .entity(new ApiResponse<>(200, syncResponse, null))
                .build();
    }
}

