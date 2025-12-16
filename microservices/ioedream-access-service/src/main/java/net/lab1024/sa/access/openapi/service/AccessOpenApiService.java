package net.lab1024.sa.access.openapi.service;

import net.lab1024.sa.access.openapi.domain.request.*;
import net.lab1024.sa.access.openapi.domain.response.*;

import java.util.List;

/**
 * 门禁管理开放API服务接口
 * 提供门禁控制、通行记录查询、门禁设备管理等开放服务
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface AccessOpenApiService {

    /**
     * 门禁通行验证
     *
     * @param request 验证请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 验证响应
     */
    AccessVerifyResponse verifyAccess(AccessVerifyRequest request, String token, String clientIp);

    /**
     * 远程控制门禁设备
     *
     * @param deviceId 设备ID
     * @param action 操作类型
     * @param reason 操作原因
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 控制响应
     */
    AccessControlResponse remoteControl(String deviceId, String action, String reason, String token, String clientIp);

    /**
     * 获取通行记录列表
     *
     * @param request 查询请求
     * @param token 访问令牌
     * @return 分页通行记录
     */
    net.lab1024.sa.common.openapi.domain.response.PageResult<AccessRecordResponse> getAccessRecords(
            AccessRecordQueryRequest request, String token);

    /**
     * 获取通行记录详情
     *
     * @param recordId 记录ID
     * @param token 访问令牌
     * @return 通行记录详情
     */
    AccessRecordDetailResponse getAccessRecordDetail(Long recordId, String token);

    /**
     * 获取门禁设备列表
     *
     * @param areaId 区域ID
     * @param deviceStatus 设备状态
     * @param token 访问令牌
     * @return 设备列表
     */
    List<AccessDeviceResponse> getAccessDevices(Long areaId, Integer deviceStatus, String token);

    /**
     * 获取门禁设备详情
     *
     * @param deviceId 设备ID
     * @param token 访问令牌
     * @return 设备详情
     */
    AccessDeviceDetailResponse getAccessDeviceDetail(String deviceId, String token);

    /**
     * 控制门禁设备
     *
     * @param deviceId 设备ID
     * @param action 操作类型
     * @param parameters 操作参数
     * @param token 访问令牌
     * @param clientIp 客户端IP
     * @return 控制响应
     */
    AccessControlResponse controlDevice(String deviceId, String action, String parameters, String token, String clientIp);

    /**
     * 获取用户门禁权限
     *
     * @param userId 用户ID
     * @param token 访问令牌
     * @return 用户门禁权限
     */
    UserAccessPermissionResponse getUserAccessPermissions(Long userId, String token);

    /**
     * 授予用户门禁权限
     *
     * @param request 授权请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     */
    void grantAccessPermission(GrantAccessPermissionRequest request, String token, String clientIp);

    /**
     * 撤销用户门禁权限
     *
     * @param request 撤销请求
     * @param token 访问令牌
     * @param clientIp 客户端IP
     */
    void revokeAccessPermission(RevokeAccessPermissionRequest request, String token, String clientIp);

    /**
     * 获取实时通行状态
     *
     * @param areaId 区域ID
     * @param token 访问令牌
     * @return 实时状态
     */
    AccessRealtimeStatusResponse getRealtimeAccessStatus(Long areaId, String token);

    /**
     * 获取门禁统计信息
     *
     * @param statisticsType 统计类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID
     * @param token 访问令牌
     * @return 统计信息
     */
    AccessStatisticsResponse getAccessStatistics(String statisticsType, String startDate, String endDate, Long areaId, String token);
}