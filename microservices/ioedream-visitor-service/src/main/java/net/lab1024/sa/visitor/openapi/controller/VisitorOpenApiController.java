package net.lab1024.sa.visitor.openapi.controller;

import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.openapi.domain.request.AddToBlacklistRequest;
import net.lab1024.sa.visitor.openapi.domain.request.AppointmentQueryRequest;
import net.lab1024.sa.visitor.openapi.domain.request.ApprovalRequest;
import net.lab1024.sa.visitor.openapi.domain.request.BlacklistQueryRequest;
import net.lab1024.sa.visitor.openapi.domain.request.CancelAppointmentRequest;
import net.lab1024.sa.visitor.openapi.domain.request.CheckInRequest;
import net.lab1024.sa.visitor.openapi.domain.request.CheckOutRequest;
import net.lab1024.sa.visitor.openapi.domain.request.SendInvitationRequest;
import net.lab1024.sa.visitor.openapi.domain.request.UpdateAppointmentRequest;
import net.lab1024.sa.visitor.openapi.domain.request.VisitorAppointmentRequest;
import net.lab1024.sa.visitor.openapi.domain.request.VisitorQueryRequest;
import net.lab1024.sa.visitor.openapi.domain.response.ApprovalResponse;
import net.lab1024.sa.visitor.openapi.domain.response.BlacklistVisitorResponse;
import net.lab1024.sa.visitor.openapi.domain.response.CheckInResponse;
import net.lab1024.sa.visitor.openapi.domain.response.CheckOutResponse;
import net.lab1024.sa.visitor.openapi.domain.response.InvitationResponse;
import net.lab1024.sa.visitor.openapi.domain.response.RealtimeVisitorStatusResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorAppointmentDetailResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorAppointmentResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorDetailResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorInfoResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorStatisticsResponse;
import net.lab1024.sa.visitor.openapi.domain.response.VisitorTrackingResponse;
import net.lab1024.sa.visitor.openapi.service.VisitorOpenApiService;

/**
 * 开放平台访客管理API控制器
 * 提供访客预约、审批流程、轨迹追踪等开放接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
@RestController
@RequestMapping("/open/api/v1/visitor")
@Tag(name = "开放平台访客管理API", description = "提供访客预约、审批流程、轨迹追踪等功能")
@Validated
@Slf4j
public class VisitorOpenApiController {

    @Resource
    private VisitorOpenApiService visitorOpenApiService;

    /**
     * 访客预约
     */
    @PostMapping("/appointment")
    @Operation(summary = "访客预约", description = "提交访客预约申请")
    public ResponseDTO<VisitorAppointmentResponse> createAppointment(
            @Valid @RequestBody VisitorAppointmentRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 访客预约申请: visitorName={}, visitorPhone={}, clientIp={}",
                request.getVisitorName(), request.getVisitorPhone(), clientIp);

        VisitorAppointmentResponse response = visitorOpenApiService.createAppointment(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 更新访客预约
     */
    @PutMapping("/appointment/{appointmentId}")
    @Operation(summary = "更新访客预约", description = "更新访客预约信息")
    public ResponseDTO<VisitorAppointmentResponse> updateAppointment(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Valid @RequestBody UpdateAppointmentRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 更新访客预约: appointmentId={}, clientIp={}", appointmentId, clientIp);

        VisitorAppointmentResponse response = visitorOpenApiService.updateAppointment(appointmentId, request, token,
                clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 取消访客预约
     */
    @PostMapping("/appointment/{appointmentId}/cancel")
    @Operation(summary = "取消访客预约", description = "取消访客预约")
    public ResponseDTO<Void> cancelAppointment(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Valid @RequestBody CancelAppointmentRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 取消访客预约: appointmentId={}, reason={}, clientIp={}",
                appointmentId, request.getCancelReason(), clientIp);

        visitorOpenApiService.cancelAppointment(appointmentId, request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 获取预约列表
     */
    @GetMapping("/appointments")
    @Operation(summary = "获取预约列表", description = "分页获取访客预约列表")
    public ResponseDTO<PageResult<VisitorAppointmentResponse>> getAppointments(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "访客姓名") @RequestParam(required = false) String visitorName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "预约状态") @RequestParam(required = false) String appointmentStatus,
            @Parameter(description = "预约类型") @RequestParam(required = false) String appointmentType,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        AppointmentQueryRequest queryRequest = AppointmentQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .visitorName(visitorName)
                .phoneNumber(phoneNumber)
                .appointmentStatus(appointmentStatus)
                .appointmentType(appointmentType)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        log.info("[开放API] 查询访客预约: pageNum={}, pageSize={}, appointmentStatus={}",
                pageNum, pageSize, appointmentStatus);

        PageResult<VisitorAppointmentResponse> result = visitorOpenApiService.getAppointments(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/appointments/{appointmentId}")
    @Operation(summary = "获取预约详情", description = "根据预约ID获取详情")
    public ResponseDTO<VisitorAppointmentDetailResponse> getAppointmentDetail(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询预约详情: appointmentId={}", appointmentId);

        VisitorAppointmentDetailResponse response = visitorOpenApiService.getAppointmentDetail(appointmentId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 审批预约
     */
    @PostMapping("/appointments/{appointmentId}/approve")
    @Operation(summary = "审批预约", description = "审批访客预约")
    public ResponseDTO<ApprovalResponse> approveAppointment(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Valid @RequestBody ApprovalRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 审批访客预约: appointmentId={}, approveResult={}, clientIp={}",
                appointmentId, request.getApproveResult(), clientIp);

        ApprovalResponse response = visitorOpenApiService.approveAppointment(appointmentId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 访客签到
     */
    @PostMapping("/checkin/{appointmentId}")
    @Operation(summary = "访客签到", description = "访客签到")
    public ResponseDTO<CheckInResponse> checkIn(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Valid @RequestBody CheckInRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 访客签到: appointmentId={}, checkinType={}, clientIp={}",
                appointmentId, request.getCheckinType(), clientIp);

        CheckInResponse response = visitorOpenApiService.checkIn(appointmentId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 访客签退
     */
    @PostMapping("/checkout/{appointmentId}")
    @Operation(summary = "访客签退", description = "访客签退")
    public ResponseDTO<CheckOutResponse> checkOut(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Valid @RequestBody CheckOutRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 访客签退: appointmentId={}, clientIp={}", appointmentId, clientIp);

        CheckOutResponse response = visitorOpenApiService.checkOut(appointmentId, request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取访客轨迹
     */
    @GetMapping("/tracking/{appointmentId}")
    @Operation(summary = "获取访客轨迹", description = "获取访客活动轨迹")
    public ResponseDTO<VisitorTrackingResponse> getVisitorTracking(
            @Parameter(description = "预约ID") @PathVariable Long appointmentId,
            @Parameter(description = "轨迹类型") @RequestParam(required = false) String trackingType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询访客轨迹: appointmentId={}, trackingType={}", appointmentId, trackingType);

        VisitorTrackingResponse response = visitorOpenApiService.getVisitorTracking(appointmentId, trackingType, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取实时访客状态
     */
    @GetMapping("/realtime/status")
    @Operation(summary = "获取实时访客状态", description = "获取实时访客状态")
    public ResponseDTO<RealtimeVisitorStatusResponse> getRealtimeVisitorStatus(
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "访客状态") @RequestParam(required = false) String visitorStatus,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取实时访客状态: areaId={}, visitorStatus={}", areaId, visitorStatus);

        RealtimeVisitorStatusResponse response = visitorOpenApiService.getRealtimeVisitorStatus(areaId, visitorStatus,
                token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取访客统计
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取访客统计", description = "获取访客统计信息")
    public ResponseDTO<VisitorStatisticsResponse> getVisitorStatistics(
            @Parameter(description = "统计类型") @RequestParam(defaultValue = "daily") String statisticsType,
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId,
            @Parameter(description = "被访人ID") @RequestParam(required = false) Long visitedUserId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 获取访客统计: statisticsType={}, startDate={}, endDate={}",
                statisticsType, startDate, endDate);

        VisitorStatisticsResponse response = visitorOpenApiService.getVisitorStatistics(
                statisticsType, startDate, endDate, areaId, visitedUserId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/visitors")
    @Operation(summary = "获取访客列表", description = "获取访客列表")
    public ResponseDTO<PageResult<VisitorInfoResponse>> getVisitors(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "访客姓名") @RequestParam(required = false) String visitorName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "身份证号") @RequestParam(required = false) String idCardNumber,
            @Parameter(description = "访客类型") @RequestParam(required = false) String visitorType,
            @Parameter(description = "是否黑名单") @RequestParam(required = false) Boolean isBlacklist,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        VisitorQueryRequest queryRequest = VisitorQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .visitorName(visitorName)
                .phoneNumber(phoneNumber)
                .idCardNumber(idCardNumber)
                .visitorType(visitorType)
                .isBlacklist(isBlacklist)
                .build();

        log.info("[开放API] 查询访客列表: pageNum={}, pageSize={}, visitorType={}",
                pageNum, pageSize, visitorType);

        PageResult<VisitorInfoResponse> result = visitorOpenApiService.getVisitors(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取访客详情
     */
    @GetMapping("/visitors/{visitorId}")
    @Operation(summary = "获取访客详情", description = "根据访客ID获取详情")
    public ResponseDTO<VisitorDetailResponse> getVisitorDetail(
            @Parameter(description = "访客ID") @PathVariable Long visitorId,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);
        log.info("[开放API] 查询访客详情: visitorId={}", visitorId);

        VisitorDetailResponse response = visitorOpenApiService.getVisitorDetail(visitorId, token);
        return ResponseDTO.ok(response);
    }

    /**
     * 添加访客黑名单
     */
    @PostMapping("/blacklist")
    @Operation(summary = "添加访客黑名单", description = "添加访客到黑名单")
    public ResponseDTO<Void> addToBlacklist(
            @Valid @RequestBody AddToBlacklistRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 添加访客黑名单: visitorName={}, visitorPhone={}, reason={}, clientIp={}",
                request.getVisitorName(), request.getVisitorPhone(), request.getBlacklistReason(), clientIp);

        visitorOpenApiService.addToBlacklist(request, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 从黑名单移除访客
     */
    @DeleteMapping("/blacklist/{visitorId}")
    @Operation(summary = "从黑名单移除访客", description = "从黑名单中移除访客")
    public ResponseDTO<Void> removeFromBlacklist(
            @Parameter(description = "访客ID") @PathVariable Long visitorId,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 从黑名单移除访客: visitorId={}, clientIp={}", visitorId, clientIp);

        visitorOpenApiService.removeFromBlacklist(visitorId, token, clientIp);
        return ResponseDTO.ok();
    }

    /**
     * 发送访客邀请
     */
    @PostMapping("/invitation/send")
    @Operation(summary = "发送访客邀请", description = "发送访客邀请")
    public ResponseDTO<InvitationResponse> sendInvitation(
            @Valid @RequestBody SendInvitationRequest request,
            @RequestHeader("Authorization") String authorization,
            HttpServletRequest httpRequest) {

        String clientIp = getClientIpAddress(httpRequest);
        String token = extractTokenFromAuthorization(authorization);

        log.info("[开放API] 发送访客邀请: visitId={}, invitationMethod={}, clientIp={}",
                request.getVisitId(), request.getInvitationMethod(), clientIp);

        InvitationResponse response = visitorOpenApiService.sendInvitation(request, token, clientIp);
        return ResponseDTO.ok(response);
    }

    /**
     * 获取黑名单列表
     */
    @GetMapping("/blacklist")
    @Operation(summary = "获取黑名单列表", description = "获取访客黑名单列表")
    public ResponseDTO<PageResult<BlacklistVisitorResponse>> getBlacklistVisitors(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "访客姓名") @RequestParam(required = false) String visitorName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phoneNumber,
            @Parameter(description = "黑名单类型") @RequestParam(required = false) String blacklistType,
            @RequestHeader("Authorization") String authorization) {

        String token = extractTokenFromAuthorization(authorization);

        BlacklistQueryRequest queryRequest = BlacklistQueryRequest.builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .visitorName(visitorName)
                .phoneNumber(phoneNumber)
                .blacklistType(blacklistType)
                .build();

        log.info("[开放API] 查询黑名单列表: pageNum={}, pageSize={}, blacklistType={}",
                pageNum, pageSize, blacklistType);

        PageResult<BlacklistVisitorResponse> result = visitorOpenApiService.getBlacklistVisitors(queryRequest, token);
        return ResponseDTO.ok(result);
    }

    /**
     * 从Authorization头中提取访问令牌
     */
    private String extractTokenFromAuthorization(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid Authorization header format");
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
