package net.lab1024.sa.visitor.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 访客门禁控制服务接口
 * <p>
 * 内存优化设计原则：
 * - 接口精简，职责单一
 * - 使用异步处理，提高并发性能
 * - 熔断器保护，防止级联故障
 * - 批量操作支持，减少IO开销
 * - 合理的缓存策略，减少重复查询
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface VisitorAccessControlService {

    /**
     * 访客门禁授权
     * <p>
     * 为访客授权门禁通行权限
     * 支持时间范围和区域限制
     * 自动生成临时访问凭证
     * </p>
     *
     * @param visitorId 访客ID
     * @param deviceId 门禁设备ID
     * @param areaIds 允许访问的区域ID列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 授权结果，包含访问凭证ID
     */
    @CircuitBreaker(name = "visitorAccessControl", fallbackMethod = "authorizeAccessFallback")
    @TimeLimiter(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<String>> authorizeAccess(
            Long visitorId,
            String deviceId,
            List<Long> areaIds,
            String startTime,
            String endTime
    );

    /**
     * 门禁通行验证
     * <p>
     * 验证访客的门禁通行权限
     * 支持多种验证方式（人脸、卡片、二维码）
     * 实时检查访问时间和区域限制
     * 记录通行日志
     * </p>
     *
     * @param deviceId 门禁设备ID
     * @param verifyType 验证类型（face/card/qrcode）
     * @param verifyData 验证数据
     * @return 验证结果，包含访客信息和通行权限
     */
    @CircuitBreaker(name = "visitorAccessControl", fallbackMethod = "verifyAccessFallback")
    @TimeLimiter(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> verifyAccess(
            String deviceId,
            String verifyType,
            String verifyData
    );

    /**
     * 撤销门禁权限
     * <p>
     * 撤销访客的门禁通行权限
     * 支持即时生效和定时生效
     * 清除相关的缓存数据
     * 发送撤销通知
     * </p>
     *
     * @param visitorId 访客ID
     * @param credentialId 访问凭证ID（可选）
     * @param reason 撤销原因
     * @return 撤销结果
     */
    @CircuitBreaker(name = "visitorAccessControl", fallbackMethod = "revokeAccessFallback")
    @TimeLimiter(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Void>> revokeAccess(Long visitorId, String credentialId, String reason);

    /**
     * 更新门禁权限
     * <p>
     * 更新访客的门禁权限配置
     * 支持延长访问时间、调整区域权限
     * 自动更新相关凭证
     * </p>
     *
     * @param credentialId 访问凭证ID
     * @param areaIds 新的区域权限列表
     * @param endTime 新的结束时间
     * @return 更新结果
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Void>> updateAccessPermission(
            String credentialId,
            List<Long> areaIds,
            String endTime
    );

    /**
     * 批量门禁授权
     * <p>
     * 为多个访客批量授权门禁权限
     * 使用相同的权限配置
     * 提高批量处理效率
     * </p>
     *
     * @param visitorIds 访客ID列表
     * @param deviceId 门禁设备ID
     * @param areaIds 区域权限列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 批量授权结果
     */
    @CircuitBreaker(name = "batchAccessControl")
    CompletableFuture<ResponseDTO<List<String>>> batchAuthorizeAccess(
            List<Long> visitorIds,
            String deviceId,
            List<Long> areaIds,
            String startTime,
            String endTime
    );

    /**
     * 检查门禁权限状态
     * <p>
     * 检查访客的门禁权限状态
     * 支持实时状态查询
     * 返回详细的权限信息
     * </p>
     *
     * @param visitorId 访客ID
     * @param deviceId 门禁设备ID
     * @return 权限状态信息
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> checkAccessPermission(Long visitorId, String deviceId);

    /**
     * 获取访客通行记录
     * <p>
     * 查询访客的门禁通行记录
     * 支持分页和条件筛选
     * 包含通行结果和异常信息
     * </p>
     *
     * @param visitorId 访客ID
     * @param deviceId 门禁设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 通行记录列表
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> getAccessRecords(
            Long visitorId,
            String deviceId,
            String startTime,
            String endTime,
            Integer pageNum,
            Integer pageSize
    );

    /**
     * 实时门禁监控
     * <p>
     * 实时监控门禁设备状态和通行情况
     * 支持多设备同时监控
     * 返回实时统计数据
     * </p>
     *
     * @param deviceIds 门禁设备ID列表
     * @return 实时监控数据
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> getRealTimeAccessMonitor(List<String> deviceIds);

    /**
     * 门禁异常处理
     * <p>
     * 处理门禁通行异常情况
     * 支持自动重试和人工干预
     * 记录异常处理日志
     * </p>
     *
     * @param deviceId 门禁设备ID
     * @param exceptionType 异常类型
     * @param exceptionData 异常数据
     * @return 处理结果
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Void>> handleAccessException(
            String deviceId,
            String exceptionType,
            String exceptionData
    );

    /**
     * 门禁权限统计
     * <p>
     * 获取门禁权限的统计数据
     * 包括授权数量、使用频率、成功率等
     * 用于管理和分析
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计数据
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> getAccessControlStatistics(String startTime, String endTime);

    /**
     * 远程开门控制
     * <p>
     * 远程控制门禁设备开门
     * 支持临时授权和紧急开门
     * 记录开门操作日志
     * </p>
     *
     * @param deviceId 门禁设备ID
     * @param operatorId 操作人ID
     * @param reason 开门原因
     * @return 控制结果
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Void>> remoteOpenDoor(String deviceId, Long operatorId, String reason);

    /**
     * 门禁设备状态查询
     * <p>
     * 查询门禁设备的在线状态和健康情况
     * 支持批量查询
     * 返回详细的设备状态信息
     * </p>
     *
     * @param deviceIds 门禁设备ID列表
     * @return 设备状态信息
     */
    @CircuitBreaker(name = "visitorAccessControl")
    CompletableFuture<ResponseDTO<Object>> getDeviceStatus(List<String> deviceIds);

    // ==================== 降级处理方法 ====================

    /**
     * 门禁授权降级处理
     */
    default CompletableFuture<ResponseDTO<String>> authorizeAccessFallback(
            Long visitorId,
            String deviceId,
            List<Long> areaIds,
            String startTime,
            String endTime,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ACCESS_CONTROL_SERVICE_UNAVAILABLE", "门禁控制服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 门禁验证降级处理
     */
    default CompletableFuture<ResponseDTO<Object>> verifyAccessFallback(
            String deviceId,
            String verifyType,
            String verifyData,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ACCESS_CONTROL_SERVICE_UNAVAILABLE", "门禁控制服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 门禁撤销降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> revokeAccessFallback(
            Long visitorId,
            String credentialId,
            String reason,
            Exception exception
    ) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("ACCESS_CONTROL_SERVICE_UNAVAILABLE", "门禁控制服务暂时不可用，请稍后重试")
        );
    }
}
