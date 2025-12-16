package net.lab1024.sa.access.edge.service;

import java.util.Map;
import java.util.List;
import java.util.concurrent.Future;

import net.lab1024.sa.video.edge.model.EdgeDevice;
import net.lab1024.sa.video.edge.model.InferenceRequest;
import net.lab1024.sa.video.edge.model.InferenceResult;

/**
 * 边缘安全服务接口
 * <p>
 * 定义边缘设备安全推理服务的核心功能接口：
 * - 边缘设备安全推理管理
 * - 安全模型动态更新
 * - 设备状态监控
 * - 统计信息收集
 * - 云边协同推理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface EdgeSecurityService {

    // ==================== 核心推理接口 ====================

    /**
     * 执行安全推理
     * <p>
     * 在边缘设备上执行安全相关的AI推理任务
     * 支持活体检测、人脸识别、行为分析等
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 推理结果Future
     */
    Future<InferenceResult> performSecurityInference(InferenceRequest inferenceRequest);

    /**
     * 执行协同安全推理
     * <p>
     * 复杂推理任务的云边协同处理
     * 边缘设备优先，云端辅助，结果融合
     * </p>
     *
     * @param inferenceRequest 推理请求
     * @return 协同推理结果Future
     */
    Future<InferenceResult> performCollaborativeSecurityInference(InferenceRequest inferenceRequest);

    /**
     * 批量安全推理
     * <p>
     * 批量处理多个安全推理任务
     * 提高边缘设备利用率
     * </p>
     *
     * @param inferenceRequests 推理请求Map (taskId -> InferenceRequest)
     * @return 批量推理结果Map (taskId -> Future<InferenceResult>)
     */
    Map<String, Future<InferenceResult>> performBatchSecurityInference(Map<String, InferenceRequest> inferenceRequests);

    // ==================== 设备管理接口 ====================

    /**
     * 注册边缘安全设备
     * <p>
     * 注册新的边缘安全设备到系统中
     * 建立连接并同步安全AI模型
     * </p>
     *
     * @param edgeDevice 边缘设备信息
     * @return 注册结果
     */
    boolean registerEdgeSecurityDevice(EdgeDevice edgeDevice);

    /**
     * 注销边缘安全设备
     * <p>
     * 从系统中注销边缘安全设备
     * 清理连接和相关资源
     * </p>
     *
     * @param deviceId 设备ID
     * @return 注销结果
     */
    boolean unregisterEdgeSecurityDevice(String deviceId);

    /**
     * 获取边缘设备安全状态
     * <p>
     * 查询边缘安全设备的运行状态和性能指标
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态信息Map
     */
    Map<String, Object> getEdgeDeviceSecurityStatus(String deviceId);

    /**
     * 获取所有边缘安全设备状态
     * <p>
     * 批量查询所有边缘安全设备的状态
     * </p>
     *
     * @return 设备状态列表
     */
    List<Map<String, Object>> getAllEdgeSecurityDeviceStatus();

    // ==================== 模型管理接口 ====================

    /**
     * 更新边缘设备安全模型
     * <p>
     * 动态更新边缘设备上的安全AI模型
     * 支持热更新，不影响设备正常运行
     * </p>
     *
     * @param deviceId 设备ID
     * @param modelType 模型类型
     * @param modelData 模型数据
     * @return 更新结果
     */
    boolean updateEdgeSecurityModel(String deviceId, String modelType, byte[] modelData);

    /**
     * 获取边缘设备模型信息
     * <p>
     * 查询边缘设备上加载的安全模型信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 模型信息Map
     */
    Map<String, Object> getEdgeDeviceModels(String deviceId);

    /**
     * 同步安全模型到边缘设备
     * <p>
     * 将云端的安全模型同步到边缘设备
     * </p>
     *
     * @param deviceId 设备ID
     * @param modelTypes 模型类型列表
     * @return 同步结果
     */
    boolean syncSecurityModelsToDevice(String deviceId, List<String> modelTypes);

    // ==================== 统计信息接口 ====================

    /**
     * 获取边缘安全统计信息
     * <p>
     * 获取边缘安全系统的运行统计信息
     * 包括设备数量、推理成功率、平均响应时间等
     * </p>
     *
     * @return 统计信息Map
     */
    Map<String, Object> getEdgeSecurityStatistics();

    /**
     * 获取边缘设备性能指标
     * <p>
     * 获取指定边缘设备的详细性能指标
     * </p>
     *
     * @param deviceId 设备ID
     * @return 性能指标Map
     */
    Map<String, Object> getEdgeDevicePerformanceMetrics(String deviceId);

    /**
     * 获取推理任务统计信息
     * <p>
     * 获取指定时间范围内的推理任务统计
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @return 任务统计信息Map
     */
    Map<String, Object> getInferenceTaskStatistics(java.time.LocalDateTime startTime,
                                                   java.time.LocalDateTime endTime,
                                                   String deviceId);

    // ==================== 安全策略接口 ====================

    /**
     * 配置边缘设备安全策略
     * <p>
     * 为边缘设备配置安全相关的策略参数
     * </p>
     *
     * @param deviceId 设备ID
     * @param securityPolicies 安全策略Map
     * @return 配置结果
     */
    boolean configureEdgeSecurityPolicies(String deviceId, Map<String, Object> securityPolicies);

    /**
     * 获取边缘设备安全策略
     * <p>
     * 查询边缘设备当前的安全策略配置
     * </p>
     *
     * @param deviceId 设备ID
     * @return 安全策略Map
     */
    Map<String, Object> getEdgeSecurityPolicies(String deviceId);

    /**
     * 评估边缘设备安全风险
     * <p>
     * 基于设备运行状态和安全策略进行风险评估
     * </p>
     *
     * @param deviceId 设备ID
     * @return 风险评估结果Map
     */
    Map<String, Object> assessEdgeSecurityRisk(String deviceId);

    // ==================== 告警通知接口 ====================

    /**
     * 配置边缘设备告警规则
     * <p>
     * 为边缘设备配置安全告警规则
     * </p>
     *
     * @param deviceId 设备ID
     * @param alertRules 告警规则列表
     * @return 配置结果
     */
    boolean configureEdgeSecurityAlerts(String deviceId, List<Map<String, Object>> alertRules);

    /**
     * 发送安全告警通知
     * <p>
     * 当检测到安全威胁时发送告警通知
     * </p>
     *
     * @param alertData 告警数据Map
     * @return 发送结果
     */
    boolean sendSecurityAlertNotification(Map<String, Object> alertData);

    /**
     * 获取边缘安全告警历史
     * <p>
     * 查询边缘设备的安全告警历史记录
     * </p>
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 告警历史列表
     */
    List<Map<String, Object>> getEdgeSecurityAlertHistory(String deviceId,
                                                         java.time.LocalDateTime startTime,
                                                         java.time.LocalDateTime endTime);

    // ==================== 数据管理接口 ====================

    /**
     * 备份边缘安全数据
     * <p>
     * 备份边缘设备上的重要安全数据
     * </p>
     *
     * @param deviceId 设备ID
     * @param dataTypes 数据类型列表
     * @return 备份结果
     */
    boolean backupEdgeSecurityData(String deviceId, List<String> dataTypes);

    /**
     * 恢复边缘安全数据
     * <p>
     * 从云端恢复边缘安全数据
     * </p>
     *
     * @param deviceId 设备ID
     * @param backupData 备份数据
     * @return 恢复结果
     */
    boolean restoreEdgeSecurityData(String deviceId, Map<String, Object> backupData);

    /**
     * 清理边缘安全缓存
     * <p>
     * 清理边缘设备上的安全相关缓存数据
     * </p>
     *
     * @param deviceId 设备ID
     * @param cacheTypes 缓存类型列表
     * @return 清理结果
     */
    boolean clearEdgeSecurityCache(String deviceId, List<String> cacheTypes);

    // ==================== 健康检查接口 ====================

    /**
     * 执行边缘设备健康检查
     * <p>
     * 对边缘设备进行全面的健康检查
     * </p>
     *
     * @param deviceId 设备ID
     * @return 健康检查结果Map
     */
    Map<String, Object> performEdgeDeviceHealthCheck(String deviceId);

    /**
     * 获取边缘设备诊断信息
     * <p>
     * 获取边缘设备的详细诊断信息
     * </p>
     *
     * @param deviceId 设备ID
     * @return 诊断信息Map
     */
    Map<String, Object> getEdgeDeviceDiagnosticInfo(String deviceId);

    /**
     * 修复边缘设备问题
     * <p>
     * 尝试自动修复边缘设备发现的问题
     * </p>
     *
     * @param deviceId 设备ID
     * @param issueTypes 问题类型列表
     * @return 修复结果Map
     */
    Map<String, Object> repairEdgeDeviceIssues(String deviceId, List<String> issueTypes);
}