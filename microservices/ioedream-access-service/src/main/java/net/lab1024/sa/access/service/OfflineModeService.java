package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 离线模式服务接口
 * <p>
 * 提供离线模式下的门禁管理功能：
 * - 离线权限数据同步
 * - 离线访问记录管理
 * - 数据完整性验证
 * - 离线模式状态监控
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 清晰的方法注释
 * - 统一的数据传输对象
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface OfflineModeService {

    /**
     * 同步离线数据
     * <p>
     * 同步离线模式所需的门禁数据：
     * - 用户权限数据
     * - 设备信息数据
     * - 配置参数数据
     * - 支持增量同步
     * </p>
     *
     * @param request 同步请求
     * @return 同步结果
     */
    ResponseDTO<OfflineSyncResult> syncOfflineData(OfflineSyncRequest request);

    /**
     * 获取离线访问权限
     * <p>
     * 获取用户在离线模式下的访问权限：
     * - 区域访问权限
     * - 设备访问权限
     * - 时间范围限制
     * - 访问方式限制
     * </p>
     *
     * @param userId 用户ID
     * @param lastSyncTime 上次同步时间
     * @return 离线权限数据
     */
    ResponseDTO<OfflinePermissionsVO> getOfflinePermissions(Long userId, String lastSyncTime);

    /**
     * 上报离线访问记录
     * <p>
     * 处理离线模式下产生的访问记录：
     * - 记录验证和校验
     * - 数据完整性检查
     * - 异常记录标记
     * - 批量数据处理
     * </p>
     *
     * @param request 上报请求
     * @return 上报结果
     */
    ResponseDTO<OfflineReportResult> reportOfflineRecords(OfflineRecordsReportRequest request);

    /**
     * 验证离线访问权限
     * <p>
     * 在离线模式下验证用户访问权限：
     * - 本地权限数据验证
     * - 时间有效性检查
     * - 访问次数限制
     * - 安全规则验证
     * </p>
     *
     * @param userId 用户ID
     * @param deviceId 设备ID
     * @param accessType 访问类型
     * @param verificationData 验证数据
     * @return 验证结果
     */
    ResponseDTO<OfflineAccessValidationResult> validateOfflineAccess(
            Long userId, Long deviceId, String accessType, String verificationData);

    /**
     * 获取离线模式状态
     * <p>
     * 查询离线模式的运行状态：
     * - 数据同步状态
     * - 设备离线状态
     * - 网络连接状态
     * - 异常事件统计
     * </p>
     *
     * @param userId 用户ID
     * @return 离线模式状态
     */
    ResponseDTO<OfflineModeStatusVO> getOfflineModeStatus(Long userId);

    /**
     * 清理过期离线数据
     * <p>
     * 清理过期的离线数据：
     * - 过期权限数据
     * - 历史访问记录
     * - 临时缓存数据
     * - 无效设备信息
     * </p>
     *
     * @param userId 用户ID
     * @return 清理结果
     */
    ResponseDTO<OfflineDataCleanupResult> cleanupExpiredOfflineData(Long userId);

    /**
     * 生成离线数据包
     * <p>
     * 为指定用户生成完整的离线数据包：
     * - 权限数据打包
     * - 设备信息打包
     * - 配置参数打包
     * - 加密和压缩
     * </p>
     *
     * @param userId 用户ID
     * @param deviceIds 设备ID列表
     * @return 离线数据包
     */
    ResponseDTO<OfflineDataPackageVO> generateOfflineDataPackage(Long userId, List<Long> deviceIds);

    /**
     * 验证离线数据完整性
     * <p>
     * 验证离线数据的完整性和有效性：
     * - 数据校验和验证
     * - 数字签名验证
     * - 数据格式验证
     * - 时效性验证
     * </p>
     *
     * @param userId 用户ID
     * @param dataPackage 数据包
     * @return 验证结果
     */
    ResponseDTO<OfflineDataIntegrityResult> validateOfflineDataIntegrity(Long userId, String dataPackage);

    /**
     * 离线模式统计报告
     * <p>
     * 生成离线模式的统计报告：
     * - 访问次数统计
     * - 成功率统计
     * - 异常事件统计
     * - 性能指标统计
     * </p>
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计报告
     */
    ResponseDTO<OfflineModeStatisticsVO> generateOfflineStatisticsReport(
            Long userId, String startTime, String endTime);

    // ==================== 内部数据传输对象 ====================

    /**
     * 离线访问验证结果
     */
    class OfflineAccessValidationResult {
        private Boolean allowed;              // 是否允许访问
        private String validationReason;     // 验证原因
        private String permissionLevel;      // 权限等级
        private Long remainingQuota;         // 剩余配额
        private String validUntil;           // 权限有效期
        private String validationMode;       // 验证模式
        private List<String> warnings;       // 警告信息
    }

    /**
     * 离线模式状态
     */
    class OfflineModeStatusVO {
        private Long userId;
        private Boolean isOfflineMode;       // 是否离线模式
        private String lastSyncTime;         // 最后同步时间
        private Integer syncedDevices;       // 已同步设备数
        private Integer pendingRecords;      // 待上报记录数
        private Integer failedRecords;       // 失败记录数
        private String networkStatus;        // 网络状态
        private String storageUsage;         // 存储使用情况
        private List<String> activeDevices;  // 活跃设备列表
    }

    /**
     * 离线数据清理结果
     */
    class OfflineDataCleanupResult {
        private Boolean success;
        private Long userId;
        private Integer cleanedPermissions;  // 清理的权限数量
        private Integer cleanedRecords;      // 清理的记录数量
        private Integer cleanedDevices;      // 清理的设备数量
        private Long freedStorage;           // 释放的存储空间（字节）
        private Long cleanupDuration;        // 清理耗时（毫秒）
        private List<String> errors;         // 错误信息
    }

    /**
     * 离线数据包
     */
    class OfflineDataPackageVO {
        private String packageId;            // 数据包ID
        private Long userId;
        private String packageVersion;      // 数据包版本
        private Long packageSize;            // 数据包大小（字节）
        private String checksum;             // 校验和
        private String encryptionMethod;     // 加密方式
        private String compressionMethod;   // 压缩方式
        private String generatedTime;        // 生成时间
        private String expiryTime;           // 过期时间
        private Integer deviceCount;         // 设备数量
        private Integer permissionCount;     // 权限数量
    }

    /**
     * 离线数据完整性验证结果
     */
    class OfflineDataIntegrityResult {
        private Boolean valid;               // 数据是否有效
        private String packageId;            // 数据包ID
        private Boolean checksumValid;       // 校验和是否有效
        private Boolean signatureValid;      // 签名是否有效
        private Boolean formatValid;         // 格式是否有效
        private Boolean expired;             // 是否过期
        private List<String> validationErrors; // 验证错误
        private String validationTime;       // 验证时间
    }

    /**
     * 离线模式统计报告
     */
    class OfflineModeStatisticsVO {
        private Long userId;
        private String reportPeriod;         // 统计周期
        private Long totalAccessAttempts;    // 总访问尝试次数
        private Long successfulAccesses;     // 成功访问次数
        private Long failedAccesses;         // 失败访问次数
        private Double successRate;          // 成功率
        private Long offlineTransactions;    // 离线交易数
        private Long onlineTransactions;     // 在线交易数
        private Integer averageResponseTime; // 平均响应时间（毫秒）
        private Integer maxResponseTime;     // 最大响应时间（毫秒）
        private Integer minResponseTime;     // 最小响应时间（毫秒）
        private List<DeviceStatistics> deviceStats; // 设备统计
    }

    /**
     * 设备统计信息
     */
    class DeviceStatistics {
        private Long deviceId;
        private String deviceName;
        private Long accessCount;            // 访问次数
        private Long successCount;           // 成功次数
        private Long failureCount;           // 失败次数
        private Double successRate;          // 成功率
        private Integer averageResponseTime; // 平均响应时间
        private String lastAccessTime;       // 最后访问时间
        private String status;               // 设备状态
    }
}