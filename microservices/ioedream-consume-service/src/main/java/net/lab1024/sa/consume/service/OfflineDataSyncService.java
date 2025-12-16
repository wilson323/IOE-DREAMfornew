package net.lab1024.sa.consume.service;

import java.util.Map;
import java.util.List;
import java.util.concurrent.Future;

import net.lab1024.sa.consume.domain.vo.OfflineSyncResultVO;

/**
 * 离线数据同步服务接口
 * <p>
 * 提供完整的离线数据同步能力，包括：
 * - 离线数据缓存与同步管理
 * - 数据一致性校验和冲突解决
 * - 智能同步策略和网络状态感知
 * - 本地验证和离线业务支持
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface OfflineDataSyncService {

    // ==================== 离线数据管理核心接口 ====================

    /**
     * 准备离线数据包
     * <p>
     * 为指定设备准备完整的离线数据包，包括：
     * - 用户账户信息（本地缓存）
     * - 消费配置和权限数据
     * - 设备认证密钥
     * - 本地验证规则
     * </p>
     *
     * @param deviceId 设备ID
     * @param userId 用户ID（可选，为个人设备时使用）
     * @return 离线数据包准备结果
     */
    Map<String, Object> prepareOfflineDataPackage(String deviceId, Long userId);

    /**
     * 同步离线数据包到设备
     * <p>
     * 将准备好的离线数据包同步到目标设备：
     * - 增量同步优化（仅传输变更数据）
     * - 数据完整性校验（MD5/CRC32校验）
     * - 同步状态跟踪和重试机制
     * - 版本管理和回滚支持
     * </p>
     *
     * @param deviceId 设备ID
     * @param dataPackage 数据包Map
     * @param syncType 同步类型（FULL全量/INCREMENTAL增量）
     * @return 同步结果Future
     */
    Future<OfflineSyncResultVO> syncOfflineDataToDevice(String deviceId,
                                                        Map<String, Object> dataPackage,
                                                        String syncType);

    /**
     * 验证离线数据完整性
     * <p>
     * 验证设备上的离线数据是否完整有效：
     * - 数据文件完整性校验
     * - 权限和配置有效性检查
     * - 数据版本一致性验证
     * - 过期数据和清理机制
     * </p>
     *
     * @param deviceId 设备ID
     * @return 验证结果Map
     */
    Map<String, Object> validateOfflineDataIntegrity(String deviceId);

    /**
     * 检查设备离线状态
     * <p>
     * 检查设备的离线状态和准备情况：
     * - 网络连接状态检测
     * - 离线数据准备状态
     * - 本地验证能力检查
     * - 设备存储空间状态
     * </p>
     *
     * @param deviceId 设备ID
     * @return 离线状态信息Map
     */
    Map<String, Object> checkDeviceOfflineStatus(String deviceId);

    // ==================== 离线业务处理接口 ====================

    /**
     * 处理离线消费记录
     * <p>
     * 处理设备上传的离线消费记录：
     * - 批量记录处理和验证
     * - 重复记录检测和去重
     * - 本地余额和权限校验
     * - 异常记录标记和处理
     * </p>
     *
     * @param deviceId 设备ID
     * @param offlineRecords 离线记录列表
     * @return 处理结果Future
     */
    Future<OfflineSyncResultVO> processOfflineConsumeRecords(String deviceId,
                                                           List<Map<String, Object>> offlineRecords);

    /**
     * 验证离线交易合法性
     * <p>
     * 对离线交易进行完整合法性验证：
     * - 交易签名验证
     * - 时间戳和防重放验证
     * - 用户权限和余额验证
     * - 设备授权状态检查
     * </p>
     *
     * @param transactionData 交易数据Map
     * @return 验证结果
     */
    boolean validateOfflineTransactionLegality(Map<String, Object> transactionData);

    /**
     * 冲突检测和解决
     * <p>
     * 检测和解决离线数据冲突：
     * - 账户余额冲突检测
     * - 交易时间冲突分析
     * - 自动冲突解决策略
     * - 手动冲突处理接口
     * </p>
     *
     * @param conflicts 冲突记录列表
     * @return 冲突解决结果
     */
    Map<String, Object> detectAndResolveConflicts(List<Map<String, Object>> conflicts);

    /**
     * 数据一致性校验
     * <p>
     * 执行完整的数据一致性校验：
     * - 设备与云端数据对比
     * - 账户余额一致性检查
     * - 交易记录完整性验证
     * - 差异报告生成
     * </p>
     *
     * @param deviceId 设备ID
     * @param 校验类型（FULL完整/PARTIAL部分）
     * @return 一致性校验报告
     */
    Map<String, Object> performDataConsistencyCheck(String deviceId, String checkType);

    // ==================== 同步策略管理接口 ====================

    /**
     * 配置设备同步策略
     * <p>
     * 为设备配置个性化的数据同步策略：
     * - 同步频率和触发条件
     * - 数据类型和优先级配置
     * - 网络状况自适应策略
     * - 存储空间管理策略
     * </p>
     *
     * @param deviceId 设备ID
     * @param syncStrategy 同步策略配置Map
     * @return 配置结果
     */
    boolean configureDeviceSyncStrategy(String deviceId, Map<String, Object> syncStrategy);

    /**
     * 获取设备同步策略
     * <p>
     * 查询设备当前的同步策略配置：
     * - 同步计划和时间表
     * - 数据包含范围
     * - 网络和存储限制
     * - 异常处理策略
     * </p>
     *
     * @param deviceId 设备ID
     * @return 同步策略信息Map
     */
    Map<String, Object> getDeviceSyncStrategy(String deviceId);

    /**
     * 执行智能同步调度
     * <p>
     * 基于网络状况和优先级进行智能同步：
     * - 网络带宽检测和分配
     * - 数据优先级排序
     * - 断点续传支持
     * - 失败重试和降级处理
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @return 调度执行结果
     */
    Future<Map<String, Object>> executeIntelligentSyncScheduling(List<String> deviceIds);

    /**
     * 获取同步统计信息
     * <p>
     * 获取离线数据同步的详细统计：
     * - 同步成功率和失败率
     * - 数据传输量和时间
     * - 冲突检测和解决统计
     * - 设备在线状态分布
     * </p>
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deviceId 设备ID（可选）
     * @return 统计信息Map
     */
    Map<String, Object> getSyncStatistics(java.time.LocalDateTime startTime,
                                          java.time.LocalDateTime endTime,
                                          String deviceId);

    // ==================== 网络状态感知接口 ====================

    /**
     * 检测网络连接质量
     * <p>
     * 实时检测网络连接质量状况：
     * - 延迟和丢包率检测
     * - 带宽和稳定性评估
     * - 连接中断预测
     * - 最佳同步时机推荐
     * </p>
     *
     * @param deviceId 设备ID
     * @return 网络质量评估结果
     */
    Map<String, Object> detectNetworkQuality(String deviceId);

    /**
     * 适应网络状况的同步策略
     * <p>
     * 根据网络状况动态调整同步策略：
     * - 低带宽环境数据压缩
     * - 高延迟环境分批同步
     * - 不稳定网络重试策略
     * - 离线模式自动切换
     * </p>
     *
     * @param deviceId 设备ID
     * @param networkStatus 网络状态信息
     * @return 适应性策略配置
     */
    Map<String, Object> adaptNetworkConditionStrategy(String deviceId,
                                                      Map<String, Object> networkStatus);

    /**
     * 批量设备离线状态检查
     * <p>
     * 批量检查多个设备的离线状态：
     * - 设备在线状态汇总
     * - 离线数据包准备状态
     * - 同步等待队列长度
     * - 异常设备识别和标记
     * </p>
     *
     * @param deviceIds 设备ID列表
     * @return 批量状态检查结果
     */
    Map<String, Object> batchCheckOfflineStatus(List<String> deviceIds);
}