package com.posdb.sync.resource;

import com.posdb.sync.dto.response.ApiResponse;
import com.posdb.sync.dto.response.DailyDetailedReportResponse;
import com.posdb.sync.dto.response.DashboardResponse;
import com.posdb.sync.dto.response.MonthlyReportResponse;
import com.posdb.sync.dto.response.DailyChartDataResponse;
import com.posdb.sync.service.DashboardService;
import io.smallrye.common.constraint.NotNull;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Path("/api/v1/dashboard")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
@Slf4j
public class DashboardResource {

    @Inject
    DashboardService dashboardService;

    @GET
    @Path("/")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDashboardData(@QueryParam("restaurant") String restaurantId) {
        log.info("DashboardResource:: Dashboard data requested for restaurantId: {}", restaurantId);
        DashboardResponse dashboardResponse=  dashboardService.getDashboardData(restaurantId);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(200, dashboardResponse, null))
                .build();
    }

    @GET
    @Path("/by-date")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDashboardDataByDate(@QueryParam("restaurant") String restaurantId, @NotNull @QueryParam("selectedDate")  LocalDate selectedDate) {
        log.info("DashboardResource:: Dashboard data requested for restaurantId: {}, selectedDate: {}", restaurantId, selectedDate);
        DashboardResponse dashboardResponse=  dashboardService.getDashboardDataByDate(restaurantId, selectedDate);
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(200, dashboardResponse, null))
                .build();
    }

    @GET
    @Path("/daily-detailed-report")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDailyDetailedReport(
            @QueryParam("restaurant") String restaurantId,
            @NotNull @QueryParam("selectedDate") LocalDate selectedDate) {
        log.info("DashboardResource:: Daily detailed report requested for restaurantId: {}, selectedDate: {}",
                restaurantId, selectedDate);
        DailyDetailedReportResponse reportResponse = dashboardService.getDailyDetailedReport(restaurantId, selectedDate);
        return Response.ok()
                .entity(new ApiResponse<>(200, reportResponse, null))
                .build();
    }

    @GET
    @Path("/monthly-report")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getMonthlyReport(
            @QueryParam("restaurant") String restaurantId,
            @NotNull @QueryParam("month") String month) {
        log.info("DashboardResource:: Monthly report requested for restaurantId: {}, month: {}",
                restaurantId, month);
        MonthlyReportResponse reportResponse = dashboardService.getMonthlyReport(restaurantId, month);
        return Response.ok()
                .entity(new ApiResponse<>(200, reportResponse, null))
                .build();
    }

    @GET
    @Path("/daily-chart-data")
    @RolesAllowed({"OWNER", "MANAGER"})
    public Response getDailyChartData(
            @QueryParam("restaurant") String restaurantId,
            @NotNull @QueryParam("month") String month) {
        log.info("DashboardResource:: Daily chart data requested for restaurantId: {}, month: {}",
                restaurantId, month);
        DailyChartDataResponse chartResponse = dashboardService.getDailyChartDataForMonth(restaurantId, month);
        return Response.ok()
                .entity(new ApiResponse<>(200, chartResponse, null))
                .build();
    }

}

