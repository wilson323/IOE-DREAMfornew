package net.lab1024.sa.visitor.openapi.service;

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
import net.lab1024.sa.visitor.openapi.domain.request.VisitorStatisticsRequest;
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

/**
 * 访客OpenAPI服务接口
 *
 * @author IOE-DREAM团队
 * @version 1.0.0
 * @since 2025-12-19
 */
public interface VisitorOpenApiService {

    /**
     * 创建访客预约
     */
    ResponseDTO<VisitorAppointmentResponse> createAppointment(VisitorAppointmentRequest request);

    /**
     * 更新访客预约
     */
    ResponseDTO<VisitorAppointmentResponse> updateAppointment(UpdateAppointmentRequest request);

    /**
     * 取消访客预约
     */
    ResponseDTO<VisitorAppointmentResponse> cancelAppointment(CancelAppointmentRequest request);

    /**
     * 获取访客统计信息
     */
    ResponseDTO<VisitorStatisticsResponse> getVisitorStatistics(VisitorStatisticsRequest request);

    /**
     * 获取访客详细信息
     */
    ResponseDTO<VisitorInfoResponse> getVisitorInfo(Long visitorId);

    /**
     * 获取访客详细信息
     */
    ResponseDTO<VisitorDetailResponse> getVisitorDetail(Long visitId);

    /**
     * 添加访客到黑名单
     */
    ResponseDTO<Void> addToBlacklist(AddToBlacklistRequest request);

    /**
     * 发送访客邀请
     */
    ResponseDTO<InvitationResponse> sendInvitation(SendInvitationRequest request);

    /**
     * 获取黑名单访客信息
     */
    ResponseDTO<BlacklistVisitorResponse> getBlacklistVisitors();

    /**
     * 访客预约（开放API鉴权token透传）
     */
    VisitorAppointmentResponse createAppointment(VisitorAppointmentRequest request, String token, String clientIp);

    /**
     * 更新访客预约（开放API鉴权token透传）
     */
    VisitorAppointmentResponse updateAppointment(Long appointmentId, UpdateAppointmentRequest request, String token,
            String clientIp);

    /**
     * 取消访客预约（开放API鉴权token透传）
     */
    void cancelAppointment(Long appointmentId, CancelAppointmentRequest request, String token, String clientIp);

    /**
     * 查询预约列表（开放API鉴权token透传）
     */
    PageResult<VisitorAppointmentResponse> getAppointments(AppointmentQueryRequest request, String token);

    /**
     * 获取预约详情（开放API鉴权token透传）
     */
    VisitorAppointmentDetailResponse getAppointmentDetail(Long appointmentId, String token);

    /**
     * 审批预约（开放API鉴权token透传）
     */
    ApprovalResponse approveAppointment(Long appointmentId, ApprovalRequest request, String token, String clientIp);

    /**
     * 访客签到（开放API鉴权token透传）
     */
    CheckInResponse checkIn(Long appointmentId, CheckInRequest request, String token, String clientIp);

    /**
     * 访客签退（开放API鉴权token透传）
     */
    CheckOutResponse checkOut(Long appointmentId, CheckOutRequest request, String token, String clientIp);

    /**
     * 获取访客轨迹（开放API鉴权token透传）
     */
    VisitorTrackingResponse getVisitorTracking(Long appointmentId, String trackingType, String token);

    /**
     * 获取实时访客状态（鉴权token由网关透传）
     */
    RealtimeVisitorStatusResponse getRealtimeVisitorStatus(Long areaId, String visitorStatus, String token);

    /**
     * 获取访客统计（鉴权token由网关透传）
     */
    VisitorStatisticsResponse getVisitorStatistics(
            String statisticsType,
            String startDate,
            String endDate,
            Long areaId,
            Long visitedUserId,
            String token);

    /**
     * 查询访客列表（鉴权token由网关透传）
     */
    PageResult<VisitorInfoResponse> getVisitors(VisitorQueryRequest request, String token);

    /**
     * 获取访客详情（鉴权token由网关透传）
     */
    VisitorDetailResponse getVisitorDetail(Long visitorId, String token);

    /**
     * 添加访客到黑名单（鉴权token由网关透传）
     */
    void addToBlacklist(AddToBlacklistRequest request, String token, String clientIp);

    /**
     * 从黑名单移除访客（鉴权token由网关透传）
     */
    void removeFromBlacklist(Long visitorId, String token, String clientIp);

    /**
     * 发送访客邀请（鉴权token由网关透传）
     */
    InvitationResponse sendInvitation(SendInvitationRequest request, String token, String clientIp);

    /**
     * 查询黑名单列表（鉴权token由网关透传）
     */
    PageResult<BlacklistVisitorResponse> getBlacklistVisitors(BlacklistQueryRequest request, String token);
}
