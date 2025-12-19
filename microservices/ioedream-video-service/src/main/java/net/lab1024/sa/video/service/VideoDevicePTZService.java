package net.lab1024.sa.video.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 视频设备PTZ控制服务接口
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
 * @since 2025-12-16
 */
public interface VideoDevicePTZService {

    // ==================== 云台移动控制 ====================

    /**
     * 云台上移
     *
     * @param deviceId 设备ID
     * @param speed    移动速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> ptzUp(Long deviceId, Integer speed);

    /**
     * 云台下移
     *
     * @param deviceId 设备ID
     * @param speed    移动速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> ptzDown(Long deviceId, Integer speed);

    /**
     * 云台左移
     *
     * @param deviceId 设备ID
     * @param speed    移动速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> ptzLeft(Long deviceId, Integer speed);

    /**
     * 云台右移
     *
     * @param deviceId 设备ID
     * @param speed    移动速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> ptzRight(Long deviceId, Integer speed);

    /**
     * 云台停止移动
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> ptzStop(Long deviceId);

    // ==================== 变焦控制 ====================

    /**
     * 放大变焦
     *
     * @param deviceId 设备ID
     * @param speed    变焦速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> zoomIn(Long deviceId, Integer speed);

    /**
     * 缩小变焦
     *
     * @param deviceId 设备ID
     * @param speed    变焦速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> zoomOut(Long deviceId, Integer speed);

    /**
     * 停止变焦
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> zoomStop(Long deviceId);

    // ==================== 预设位管理 ====================

    /**
     * 设置预设位
     *
     * @param deviceId   设备ID
     * @param presetId   预设位编号(1-255)
     * @param presetName 预设位名称
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> setPreset(Long deviceId, Integer presetId, String presetName);

    /**
     * 调用预设位
     *
     * @param deviceId 设备ID
     * @param presetId 预设位编号
     * @param speed    移动速度(1-10)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> callPreset(Long deviceId, Integer presetId, Integer speed);

    /**
     * 删除预设位
     *
     * @param deviceId 设备ID
     * @param presetId 预设位编号
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> deletePreset(Long deviceId, Integer presetId);

    /**
     * 获取预设位列表
     *
     * @param deviceId 设备ID
     * @return 预设位列表
     */
    @CircuitBreaker(name = "videoPTZService")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Object>> getPresetList(Long deviceId);

    // ==================== 巡航控制 ====================

    /**
     * 开始巡航
     *
     * @param deviceId   设备ID
     * @param presetIds  预设位列表，用逗号分隔
     * @param speed      巡航速度(1-10)
     * @param dwellTime  停留时间(秒)
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> startPatrol(Long deviceId, String presetIds, Integer speed, Integer dwellTime);

    /**
     * 停止巡航
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> stopPatrol(Long deviceId);

    /**
     * 暂停巡航
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> pausePatrol(Long deviceId);

    /**
     * 恢复巡航
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> resumePatrol(Long deviceId);

    // ==================== 状态查询 ====================

    /**
     * 获取PTZ状态
     *
     * @param deviceId 设备ID
     * @return PTZ状态信息
     */
    @CircuitBreaker(name = "videoPTZService")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Object>> getPTZStatus(Long deviceId);

    /**
     * 获取当前位置
     *
     * @param deviceId 设备ID
     * @return 当前位置坐标
     */
    @CircuitBreaker(name = "videoPTZService")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Object>> getCurrentPosition(Long deviceId);

    /**
     * 重置PTZ
     *
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> resetPTZ(Long deviceId);

    /**
     * 自动校准
     *
     * @param deviceId 设备ID
     * @return 校准结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Map<String, Object>>> autoCalibrate(Long deviceId);

    // ==================== 3D定位控制 ====================

    /**
     * 3D定位控制
     *
     * @param deviceId 设备ID
     * @param x        X坐标
     * @param y        Y坐标
     * @param zoom     缩放级别
     * @return 操作结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Void>> setPosition3D(Long deviceId, Integer x, Integer y, Integer zoom);

    // ==================== 辅助功能 ====================

    /**
     * 获取PTZ能力
     *
     * @param deviceId 设备ID
     * @return PTZ能力信息
     */
    @CircuitBreaker(name = "videoPTZService")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Object>> getPTZCapabilities(Long deviceId);

    /**
     * 批量PTZ控制
     *
     * @param deviceIds 设备ID列表
     * @param command   控制命令
     * @param params    控制参数
     * @return 批量控制结果
     */
    @CircuitBreaker(name = "videoPTZService", fallbackMethod = "ptzBatchOperationFallback")
    @TimeLimiter(name = "videoPTZService")
    CompletableFuture<ResponseDTO<Map<Long, Object>>> batchPTZControl(String deviceIds, String command, Map<String, Object> params);

    // ==================== 降级处理方法 ====================

    /**
     * PTZ操作通用降级处理
     */
    default CompletableFuture<ResponseDTO<Void>> ptzOperationFallback(Long deviceId, Object param, Exception exception) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PTZ_SERVICE_UNAVAILABLE", "PTZ控制服务暂时不可用，请稍后重试")
        );
    }

    /**
     * 批量PTZ操作降级处理
     */
    default CompletableFuture<ResponseDTO<Map<Long, Object>>> ptzBatchOperationFallback(
            String deviceIds, String command, Map<String, Object> params, Exception exception) {
        return CompletableFuture.completedFuture(
                ResponseDTO.error("PTZ_BATCH_SERVICE_UNAVAILABLE", "批量PTZ控制服务暂时不可用，请稍后重试")
        );
    }
}