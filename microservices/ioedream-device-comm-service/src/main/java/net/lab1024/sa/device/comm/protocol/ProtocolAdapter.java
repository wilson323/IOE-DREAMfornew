package net.lab1024.sa.device.comm.protocol;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * 协议适配器接口
 * <p>
 * 提供设备通讯协议的标准适配器，支持不同厂商设备的协议实现：
 * - 熵基科技门禁/考勤设备 (ACCESS_ENTROPY_V4_8, ATTENDANCE_ENTROPY_V4_0)
 * - 中控智慧消费设备 (CONSUME_ZKTECO_V1_0)
 * - 可扩展支持更多厂商设备协议
 * </p>
 * <p>
 * 严格遵循厂商提供的官方协议文档，确保完全兼容：
 * - 数据格式和字段定义
 * - 消息类型和命令编码
 * - 校验机制和错误处理
 * - 编码规范和字符集要求
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-16
 */
public interface ProtocolAdapter {

    // ==================== 协议标识接口 ====================

    /**
     * 获取协议类型标识
     * <p>
     * 协议类型格式：{厂商}_{设备类型}_{版本号}
     * 示例：
     * - ACCESS_ENTROPY_V4_8 (熵基科技 门禁设备 V4.8)
     * - ATTENDANCE_ENTROPY_V4_0 (熵基科技 考勤设备 V4.0)
     * - CONSUME_ZKTECO_V1_0 (中控智慧 消费设备 V1.0)
     * </p>
     *
     * @return 协议类型标识
     */
    String getProtocolType();

    /**
     * 获取设备厂商
     *
     * @return 厂商名称
     */
    String getManufacturer();

    /**
     * 获取协议版本
     *
     * @return 版本号
     */
    String getVersion();

    /**
     * 获取支持的设备型号列表
     *
     * @return 设备型号列表
     */
    String[] getSupportedDeviceModels();

    /**
     * 检查是否支持指定设备型号
     *
     * @param deviceModel 设备型号
     * @return 是否支持
     */
    boolean isDeviceModelSupported(String deviceModel);

    // ==================== 消息处理核心接口 ====================

    /**
     * 解析原始设备消息
     * <p>
     * 将设备发送的原始数据解析为标准协议消息对象：
     * 1. 验证协议头和设备标识
     * 2. 解析消息类型和命令代码
     * 3. 解析业务数据字段
     * 4. 验证数据完整性和格式
     * 5. 生成消息对象
     * </p>
     *
     * @param rawData 设备原始数据（字节数组）
     * @param deviceId 设备ID
     * @return 解析后的协议消息对象
     * @throws ProtocolParseException 协议解析异常
     */
    ProtocolMessage parseDeviceMessage(byte[] rawData, Long deviceId) throws ProtocolParseException;

    /**
     * 解析原始设备消息（十六进制字符串）
     *
     * @param hexData 设备原始数据（十六进制字符串）
     * @param deviceId 设备ID
     * @return 解析后的协议消息对象
     * @throws ProtocolParseException 协议解析异常
     */
    ProtocolMessage parseDeviceMessage(String hexData, Long deviceId) throws ProtocolParseException;

    /**
     * 构建设备响应消息
     * <p>
     * 根据业务数据构建符合协议规范的响应消息：
     * 1. 设置响应头信息
     * 2. 序列化业务数据字段
     * 3. 生成校验码和签名
     * 4. 编码为设备可识别格式
     * </p>
     *
     * @param messageType 消息类型
     * @param businessData 业务数据Map
     * @param deviceId 设备ID
     * @return 设备响应消息（字节数组）
     * @throws ProtocolBuildException 协议构建异常
     */
    byte[] buildDeviceResponse(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException;

    /**
     * 构建设备响应消息（十六进制字符串）
     *
     * @param messageType 消息类型
     * @param businessData 业务数据Map
     * @param deviceId 设备ID
     * @return 设备响应消息（十六进制字符串）
     * @throws ProtocolBuildException 协议构建异常
     */
    String buildDeviceResponseHex(String messageType, Map<String, Object> businessData, Long deviceId) throws ProtocolBuildException;

    // ==================== 协议验证接口 ====================

    /**
     * 验证协议消息完整性
     * <p>
     * 根据协议规范验证消息的完整性：
     * 1. 验证消息格式和字段完整性
     * 2. 验证校验码、签名等安全机制
     * 3. 验证数据类型和范围
     * 4. 验证时间戳和有效期
     * </p>
     *
     * @param message 协议消息对象
     * @return 验证结果
     */
    ProtocolValidationResult validateMessage(ProtocolMessage message);

    /**
     * 验证设备权限
     * <p>
     * 验证设备是否有权限执行指定操作：
     * 1. 验证设备身份和认证状态
     * 2. 验证操作权限和访问控制
     * 3. 验证设备状态和在线状态
     * </p>
     *
     * @param deviceId 设备ID
     * @param operation 操作类型
     * @return 权限验证结果
     */
    ProtocolPermissionResult validateDevicePermission(Long deviceId, String operation);

    // ==================== 设备管理接口 ====================

    /**
     * 初始化设备连接
     * <p>
     * 与设备建立通讯连接并进行初始化：
     * 1. 建立TCP/UDP连接
     * 2. 交换设备身份认证信息
     * 3. 同步设备配置和状态
     * 4. 启动心跳和状态监控
     * </p>
     *
     * @param deviceInfo 设备信息（IP地址、端口等）
     * @param config 设备配置参数
     * @return 初始化结果Future
     */
    Future<ProtocolInitResult> initializeDevice(Map<String, Object> deviceInfo, Map<String, Object> config);

    /**
     * 处理设备注册
     * <p>
     * 处理设备注册请求，严格按照厂商协议实现：
     * - 门禁设备：注册设备类型、功能列表、验证方式等
     * - 考勤设备：注册设备能力、生物识别支持、时间同步等
     * - 消费设备：注册支付方式、商品管理、离线支持等
     * </p>
     *
     * @param registrationData 注册数据
     * @param deviceId 设备ID
     * @return 注册结果
     */
    ProtocolRegistrationResult handleDeviceRegistration(Map<String, Object> registrationData, Long deviceId);

    /**
     * 处理设备心跳
     * <p>
     * 处理设备心跳消息，监控设备在线状态：
     * 1. 解析心跳状态信息
     * 2. 更新设备在线状态
     * 3. 检测设备异常状态
     * 4. 触发相应的告警和处理
     * </p>
     *
     * @param heartbeatData 心跳数据
     * @param deviceId 设备ID
     * @return 心跳处理结果
     */
    ProtocolHeartbeatResult handleDeviceHeartbeat(Map<String, Object> heartbeatData, Long deviceId);

    /**
     * 获取设备状态
     * <p>
     * 获取设备的详细状态信息：
     * - 设备基本信息（型号、版本、厂商）
     * - 连接状态（在线、离线、异常）
     * - 功能状态（支持的功能列表）
     * - 性能状态（CPU、内存、存储使用率）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 设备状态信息
     */
    ProtocolDeviceStatus getDeviceStatus(Long deviceId);

    // ==================== 业务数据处理接口 ====================

    /**
     * 处理门禁业务数据
     * <p>
     * 严格遵循熵基科技门禁协议V4.8规范处理门禁相关数据：
     * - 实时事件上传（正常事件、异常事件、报警事件）
     * - 用户权限验证（卡片、生物特征、二维码等）
     * - 门禁控制命令（开门、关门、锁定等）
     * - 设备配置同步（时间规则、权限组等）
     * </p>
     *
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果Future
     */
    Future<ProtocolProcessResult> processAccessBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    /**
     * 处理考勤业务数据
     * <p>
     * 严格遵循熵基科技考勤协议V4.0规范处理考勤相关数据：
     * - 考勤记录上传（打卡记录、照片、生物特征等）
     * - 生物特征模板管理（注册、更新、删除）
     * - 用户信息同步（照片、权限、黑名单等）
     * - 考勤规则配置（时间规则、班次管理等）
     * </p>
     *
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果Future
     */
    Future<ProtocolProcessResult> processAttendanceBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    /**
     * 处理消费业务数据
     * <p>
     * 严格遵循中控智慧消费协议V1.0规范处理消费相关数据：
     * - 消费记录上传（消费明细、商品清单、支付方式等）
     * - 商品管理同步（商品信息、价格更新、库存管理等）
     * - 用户账户管理（充值、消费、补贴、余额等）
     * - 设备功能配置（支付方式、离线模式、广告显示等）
     * </p>
     *
     * @param businessType 业务类型
     * @param businessData 业务数据
     * @param deviceId 设备ID
     * @return 业务处理结果Future
     */
    Future<ProtocolProcessResult> processConsumeBusiness(String businessType, Map<String, Object> businessData, Long deviceId);

    // ==================== 协议配置接口 ====================

    /**
     * 获取协议配置参数
     * <p>
     * 获取协议相关的配置参数：
     * - 通讯参数（端口、超时、重试等）
     * - 数据格式参数（编码、分隔符、字段映射等）
     * - 安全参数（加密、签名、校验等）
     * - 业务参数（功能开关、限制设置等）
     * </p>
     *
     * @param deviceId 设备ID
     * @return 协议配置参数Map
     */
    Map<String, Object> getProtocolConfig(Long deviceId);

    /**
     * 更新协议配置参数
     *
     * @param deviceId 设备ID
     * @param config 配置参数Map
     * @return 更新结果
     */
    boolean updateProtocolConfig(Long deviceId, Map<String, Object> config);

    // ==================== 错误处理接口 ====================

    /**
     * 处理协议错误
     * <p>
     * 根据厂商协议规范处理协议错误：
     * 1. 解析错误代码和错误描述
     * 2. 确定错误类型和严重程度
     * 3. 生成标准化的错误响应
     * 4. 记录错误日志和告警
     * </p>
     *
     * @param errorCode 错误代码
     * @param errorMessage 错误消息
     * @param deviceId 设备ID
     * @return 标准化错误响应
     */
    ProtocolErrorResponse handleProtocolError(String errorCode, String errorMessage, Long deviceId);

    /**
     * 获取错误代码映射
     * <p>
     * 获取厂商协议错误代码与系统错误的映射关系：
     * - 厂商错误代码到系统错误类型的映射
     * - 错误严重程度分类
     * - 推荐的恢复策略
     * </p>
     *
     * @return 错误代码映射表
     */
    Map<String, ProtocolErrorInfo> getErrorCodeMapping();

    // ==================== 组件生命周期接口 ====================

    /**
     * 初始化协议适配器
     * <p>
     * 协议适配器的初始化逻辑：
     * 1. 加载协议配置和映射表
     * 2. 初始化协议解析器和构建器
     * 3. 建立与业务服务的连接
     * 4. 启动监控和统计收集
     * </p>
     */
    void initialize();

    /**
     * 销毁协议适配器
     * <p>
     * 协议适配器的清理和销毁逻辑：
     * 1. 关闭设备连接
     * 2. 清理缓存和临时数据
     * 3. 停止监控和统计
     * 4. 释放系统资源
     * </p>
     */
    void destroy();

    /**
     * 获取适配器状态
     *
     * @return 适配器状态（INITIALIZED, RUNNING, STOPPED, ERROR）
     */
    String getAdapterStatus();

    /**
     * 获取性能统计信息
     *
     * @return 性能统计Map
     */
    Map<String, Object> getPerformanceStatistics();
}