package net.lab1024.sa.access.service;

import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 蓝牙门禁服务接口
 * <p>
 * 提供蓝牙门禁相关的核心功能：
 * - 蓝牙设备扫描和连接
 * - 蓝牙门禁验证
 * - 设备配对和管理
 * - 蓝牙门禁卡管理
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
public interface BluetoothAccessService {

    /**
     * 扫描附近的蓝牙设备
     * <p>
     * 扫描用户附近的蓝牙门禁设备，支持过滤和排序：
     * - 按信号强度排序
     * - 按设备类型过滤
     * - 支持最大设备数限制
     * </p>
     *
     * @param request 扫描请求
     * @return 发现的蓝牙设备列表
     */
    ResponseDTO<List<BluetoothDeviceVO>> scanNearbyDevices(BluetoothScanRequest request);

    /**
     * 连接蓝牙设备
     * <p>
     * 与指定的蓝牙设备建立连接：
     * - 支持自动重连
     * - 连接状态监控
     * - 协议版本协商
     * </p>
     *
     * @param request 连接请求
     * @return 连接结果
     */
    ResponseDTO<BluetoothConnectionResult> connectDevice(BluetoothConnectRequest request);

    /**
     * 执行蓝牙门禁验证
     * <p>
     * 通过蓝牙进行门禁权限验证：
     * - 设备身份验证
     * - 用户权限检查
     * - 安全加密通信
     * </p>
     *
     * @param request 验证请求
     * @return 验证结果
     */
    ResponseDTO<BluetoothAccessResult> performBluetoothAccess(BluetoothAccessRequest request);

    /**
     * 获取已连接设备状态
     * <p>
     * 查询用户已连接的蓝牙设备状态：
     * - 连接状态
     * - 信号强度
     * - 电池电量
     * - 使用统计
     * </p>
     *
     * @param userId 用户ID
     * @return 设备状态列表
     */
    ResponseDTO<List<BluetoothDeviceStatusVO>> getConnectedDevicesStatus(Long userId);

    /**
     * 断开蓝牙设备连接
     * <p>
     * 断开与指定蓝牙设备的连接：
     * - 优雅断开连接
     * - 清理连接状态
     * - 释放资源
     * </p>
     *
     * @param userId 用户ID
     * @param deviceAddress 设备MAC地址
     * @return 是否成功断开
     */
    boolean disconnectDevice(Long userId, String deviceAddress);

    /**
     * 配对蓝牙设备
     * <p>
     * 与蓝牙设备进行安全配对：
     * - PIN码验证
     * - 加密密钥交换
     * - 配对状态管理
     * </p>
     *
     * @param request 配对请求
     * @return 配对结果
     */
    ResponseDTO<BluetoothPairingResult> pairDevice(BluetoothPairingRequest request);

    /**
     * 获取用户门禁卡信息
     * <p>
     * 获取用户的所有门禁卡，包括蓝牙门禁卡：
     * - 物理门禁卡
     * - 虚拟门禁卡
     * - 蓝牙门禁卡
     * </p>
     *
     * @param userId 用户ID
     * @return 门禁卡列表
     */
    ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(Long userId);

    /**
     * 添加蓝牙门禁卡
     * <p>
     * 为用户添加新的蓝牙门禁卡：
     * - 设备绑定
     * - 权限配置
     * - 有效期管理
     * </p>
     *
     * @param request 添加请求
     * @return 添加结果
     */
    ResponseDTO<String> addBluetoothAccessCard(AddBluetoothCardRequest request);

    /**
     * 蓝牙设备健康检查
     * <p>
     * 检查蓝牙设备的健康状态：
     * - 连接稳定性
     * - 电池状态
     * - 信号质量
     * - 固件版本
     * </p>
     *
     * @param deviceAddress 设备MAC地址
     * @return 健康状态信息
     */
    ResponseDTO<BluetoothDeviceHealthVO> checkDeviceHealth(String deviceAddress);

    /**
     * 更新蓝牙设备固件
     * <p>
     * 为蓝牙设备进行固件升级：
     * - 版本检查
     * - 固件下载
     * - 安全升级
     * </p>
     *
     * @param deviceAddress 设备MAC地址
     * @param firmwareVersion 目标固件版本
     * @return 升级结果
     */
    ResponseDTO<BluetoothFirmwareUpdateResult> updateDeviceFirmware(
            String deviceAddress, String firmwareVersion);

    /**
     * 蓝牙设备诊断
     * <p>
     * 对蓝牙设备进行全面诊断：
     * - 连接质量测试
     * - 功能模块测试
     * - 性能基准测试
     * </p>
     *
     * @param deviceAddress 设备MAC地址
     * @return 诊断报告
     */
    ResponseDTO<BluetoothDeviceDiagnosticVO> diagnoseDevice(String deviceAddress);

    // ==================== 内部数据传输对象 ====================

    /**
     * 蓝牙设备健康状态
     */
    class BluetoothDeviceHealthVO {
        private String deviceAddress;
        private String deviceName;
        private String healthStatus;        // 健康状态：EXCELLENT, GOOD, FAIR, POOR
        private Integer connectionScore;     // 连接评分（0-100）
        private Integer signalQuality;       // 信号质量（0-100）
        private Integer batteryHealth;       // 电池健康度（0-100）
        private String lastCheckTime;        // 最后检查时间
        private List<String> issues;         // 发现的问题
        private List<String> recommendations; // 改进建议
    }

    /**
     * 蓝牙固件升级结果
     */
    class BluetoothFirmwareUpdateResult {
        private Boolean success;
        private String deviceAddress;
        private String currentVersion;      // 当前版本
        private String targetVersion;       // 目标版本
        private String updateStatus;        // 升级状态
        private Long updateDuration;        // 升级耗时（毫秒）
        private String errorMessage;        // 错误信息
        private Boolean requiresReboot;     // 是否需要重启
    }

    /**
     * 蓝牙设备诊断报告
     */
    class BluetoothDeviceDiagnosticVO {
        private String deviceAddress;
        private String deviceName;
        private String diagnosticTime;      // 诊断时间
        private Integer overallScore;       // 综合评分（0-100）
        private DiagnosticResult connectionTest;  // 连接测试结果
        private DiagnosticResult functionalityTest; // 功能测试结果
        private DiagnosticResult performanceTest;   // 性能测试结果
        private List<String> identifiedIssues;     // 识别的问题
        private List<String> suggestedActions;      // 建议操作
    }

    /**
     * 诊断测试结果
     */
    class DiagnosticResult {
        private String testName;           // 测试名称
        private Boolean passed;            // 是否通过
        private Integer score;             // 评分（0-100）
        private String details;            // 详细信息
        private String status;             // 状态：PASS, FAIL, WARNING
    }
}