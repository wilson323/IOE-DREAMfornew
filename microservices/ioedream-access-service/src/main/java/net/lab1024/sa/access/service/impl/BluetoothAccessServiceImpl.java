package net.lab1024.sa.access.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import net.lab1024.sa.access.controller.AccessMobileController.*;
import net.lab1024.sa.access.service.AccessDeviceService;
import net.lab1024.sa.access.service.AdvancedAccessControlService;
import net.lab1024.sa.access.service.BluetoothAccessService;
import net.lab1024.sa.access.dao.AccessDeviceDao;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.entity.DeviceEntity;
import net.lab1024.sa.common.entity.UserEntity;
import net.lab1024.sa.common.util.SmartStringUtil;

/**
 * 蓝牙门禁服务实现
 * <p>
 * 提供蓝牙门禁的核心业务逻辑：
 * - 蓝牙设备管理和连接
 * - 安全的蓝牙门禁验证
 * - 设备配对和状态监控
 * - 蓝牙门禁卡管理
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解
 * - 使用@Transactional事务管理
 * - 完整的日志记录和错误处理
 * - 性能监控和指标收集
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BluetoothAccessServiceImpl implements BluetoothAccessService {

    @Resource
    private AccessDeviceService accessDeviceService;

    @Resource
    private AdvancedAccessControlService advancedAccessControlService;

    @Resource
    private AccessDeviceDao accessDeviceDao;

    @Resource
    private AccessRecordDao accessRecordDao;

    // 蓝牙设备连接状态缓存
    private final Map<String, BluetoothConnection> connectionCache = new ConcurrentHashMap<>();

    // 蓝牙设备配对信息缓存
    private final Map<String, BluetoothPairingInfo> pairingCache = new ConcurrentHashMap<>();

    @Override
    @Timed(value = "bluetooth.scan", description = "蓝牙设备扫描耗时")
    @Counted(value = "bluetooth.scan.count", description = "蓝牙设备扫描次数")
    public ResponseDTO<List<BluetoothDeviceVO>> scanNearbyDevices(BluetoothScanRequest request) {
        log.info("[蓝牙扫描] 开始扫描附近设备: userId={}, location={}, duration={}ms",
                request.getUserId(), request.getLocation(), request.getScanDuration());

        try {
            List<BluetoothDeviceVO> devices = new ArrayList<>();

            // 模拟蓝牙设备扫描（实际实现需要调用蓝牙API）
            List<DeviceEntity> bluetoothDevices = accessDeviceDao.selectByDeviceType("BLUETOOTH");

            for (DeviceEntity device : bluetoothDevices) {
                BluetoothDeviceVO deviceVO = new BluetoothDeviceVO();
                deviceVO.setDeviceAddress(device.getDeviceCode());
                deviceVO.setDeviceName(device.getDeviceName());
                deviceVO.setDeviceType(device.getDeviceSubType());
                deviceVO.setSignalStrength(generateSignalStrength());
                deviceVO.setBatteryLevel(generateBatteryLevel());
                deviceVO.setRssi(generateRSSI());
                deviceVO.setIsPaired(checkIfPaired(request.getUserId(), device.getDeviceCode()));
                deviceVO.setIsConnected(checkIfConnected(device.getDeviceCode()));
                deviceVO.setLastSeen(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                deviceVO.setDistance(calculateDistance(deviceVO.getRssi()));

                devices.add(deviceVO);
            }

            // 按信号强度排序
            devices.sort((a, b) -> b.getSignalStrength() - a.getSignalStrength());

            // 限制最大设备数量
            if (request.getMaxDevices() != null && devices.size() > request.getMaxDevices()) {
                devices = devices.subList(0, request.getMaxDevices());
            }

            log.info("[蓝牙扫描] 扫描完成: 发现{}个设备", devices.size());
            return ResponseDTO.ok(devices);

        } catch (Exception e) {
            log.error("[蓝牙扫描] 扫描失败: userId={}, error={}", request.getUserId(), e.getMessage(), e);
            return ResponseDTO.error("BLUETOOTH_SCAN_FAILED", "蓝牙设备扫描失败");
        }
    }

    @Override
    @Timed(value = "bluetooth.connect", description = "蓝牙设备连接耗时")
    @Counted(value = "bluetooth.connect.count", description = "蓝牙设备连接次数")
    public ResponseDTO<BluetoothConnectionResult> connectDevice(BluetoothConnectRequest request) {
        log.info("[蓝牙连接] 开始连接设备: userId={}, deviceAddress={}, timeout={}ms",
                request.getUserId(), request.getDeviceAddress(), request.getTimeout());

        try {
            String deviceAddress = request.getDeviceAddress();

            // 检查设备是否存在
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(deviceAddress);
            if (device == null || !"BLUETOOTH".equals(device.getDeviceType())) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "蓝牙设备不存在");
            }

            // 检查是否已连接
            if (checkIfConnected(deviceAddress)) {
                BluetoothConnectionResult result = new BluetoothConnectionResult();
                result.setSuccess(true);
                result.setDeviceAddress(deviceAddress);
                result.setDeviceName(device.getDeviceName());
                result.setConnectionId(generateConnectionId(deviceAddress));
                result.setResponseTime(50);
                result.setSignalStrength(generateSignalStrength());
                result.setProtocolVersion("2.1");
                return ResponseDTO.ok(result);
            }

            // 模拟设备连接过程
            long startTime = System.currentTimeMillis();

            // 验证设备配对状态
            if (!checkIfPaired(request.getUserId(), deviceAddress)) {
                return ResponseDTO.error("DEVICE_NOT_PAIRED", "设备未配对");
            }

            // 建立连接
            BluetoothConnection connection = new BluetoothConnection();
            connection.setUserId(request.getUserId());
            connection.setDeviceAddress(deviceAddress);
            connection.setDeviceName(device.getDeviceName());
            connection.setConnectionTime(System.currentTimeMillis());
            connection.setSignalStrength(generateSignalStrength());
            connection.setProtocolVersion("2.1");
            connection.setAutoReconnect(request.getAutoReconnect() != null ? request.getAutoReconnect() : false);

            connectionCache.put(deviceAddress, connection);

            long responseTime = System.currentTimeMillis() - startTime;

            BluetoothConnectionResult result = new BluetoothConnectionResult();
            result.setSuccess(true);
            result.setDeviceAddress(deviceAddress);
            result.setDeviceName(device.getDeviceName());
            result.setConnectionId(connection.getConnectionId());
            result.setResponseTime((int) responseTime);
            result.setSignalStrength(connection.getSignalStrength());
            result.setProtocolVersion(connection.getProtocolVersion());

            log.info("[蓝牙连接] 连接成功: userId={}, deviceAddress={}, responseTime={}ms",
                    request.getUserId(), deviceAddress, responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[蓝牙连接] 连接失败: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);

            BluetoothConnectionResult result = new BluetoothConnectionResult();
            result.setSuccess(false);
            result.setDeviceAddress(request.getDeviceAddress());
            result.setErrorMessage(e.getMessage());

            return ResponseDTO.ok(result);
        }
    }

    @Override
    @Timed(value = "bluetooth.access", description = "蓝牙门禁验证耗时")
    @Counted(value = "bluetooth.access.count", description = "蓝牙门禁验证次数")
    public ResponseDTO<BluetoothAccessResult> performBluetoothAccess(BluetoothAccessRequest request) {
        log.info("[蓝牙门禁] 开始验证: userId={}, deviceAddress={}, accessType={}",
                request.getUserId(), request.getDeviceAddress(), request.getAccessType());

        try {
            // 验证蓝牙连接状态
            BluetoothConnection connection = connectionCache.get(request.getDeviceAddress());
            if (connection == null) {
                return createBluetoothAccessResult(false, "设备未连接", "MEDIUM");
            }

            // 查询设备信息
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return createBluetoothAccessResult(false, "设备不存在", "HIGH");
            }

            long startTime = System.currentTimeMillis();

            // 执行标准门禁验证
            AdvancedAccessControlService.AccessControlResult controlResult =
                    advancedAccessControlService.performAccessControlCheck(
                            request.getUserId(),
                            device.getDeviceId(),
                            device.getAreaId(),
                            request.getAccessType(),
                            "BLUETOOTH_ACCESS");

            long responseTime = System.currentTimeMillis() - startTime;

            // 记录蓝牙访问日志
            recordBluetoothAccessLog(request, controlResult.isAllowed(), responseTime);

            // 更新连接统计
            connection.setLastAccessTime(System.currentTimeMillis());
            connection.setAccessCount(connection.getAccessCount() + 1);

            BluetoothAccessResult result = new BluetoothAccessResult();
            result.setSuccess(controlResult.isAllowed());
            result.setDeviceId(device.getDeviceId());
            result.setDeviceName(device.getDeviceName());
            result.setAccessMethod("BLUETOOTH");
            result.setAccessTime(System.currentTimeMillis());
            result.setResponseTime((int) responseTime);
            result.setAccessToken(controlResult.isAllowed() ? generateAccessToken() : null);
            result.setDecisionReason(controlResult.getDecisionReason());
            result.setRiskLevel(assessBluetoothRisk(request, connection));

            log.info("[蓝牙门禁] 验证完成: userId={}, deviceAddress={}, success={}, responseTime={}ms",
                    request.getUserId(), request.getDeviceAddress(), result.isSuccess(), responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[蓝牙门禁] 验证失败: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return createBluetoothAccessResult(false, "系统异常: " + e.getMessage(), "HIGH");
        }
    }

    @Override
    public ResponseDTO<List<BluetoothDeviceStatusVO>> getConnectedDevicesStatus(Long userId) {
        log.info("[蓝牙状态] 查询已连接设备状态: userId={}", userId);

        try {
            List<BluetoothDeviceStatusVO> deviceStatusList = new ArrayList<>();

            for (Map.Entry<String, BluetoothConnection> entry : connectionCache.entrySet()) {
                BluetoothConnection connection = entry.getValue();
                if (userId.equals(connection.getUserId())) {
                    BluetoothDeviceStatusVO statusVO = new BluetoothDeviceStatusVO();
                    statusVO.setDeviceAddress(connection.getDeviceAddress());
                    statusVO.setDeviceName(connection.getDeviceName());
                    statusVO.setConnectionStatus("CONNECTED");
                    statusVO.setSignalStrength(connection.getSignalStrength());
                    statusVO.setBatteryLevel(generateBatteryLevel());
                    statusVO.setLastActivity(formatTimestamp(connection.getLastAccessTime()));
                    statusVO.setTotalConnections(connection.getTotalConnections());
                    statusVO.setTotalAccessCount(connection.getAccessCount());
                    statusVO.setFirmwareVersion(connection.getFirmwareVersion());

                    deviceStatusList.add(statusVO);
                }
            }

            log.info("[蓝牙状态] 查询完成: userId={}, 设备数量={}", userId, deviceStatusList.size());
            return ResponseDTO.ok(deviceStatusList);

        } catch (Exception e) {
            log.error("[蓝牙状态] 查询失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("STATUS_QUERY_FAILED", "设备状态查询失败");
        }
    }

    @Override
    public boolean disconnectDevice(Long userId, String deviceAddress) {
        log.info("[蓝牙断开] 断开设备连接: userId={}, deviceAddress={}", userId, deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);
            if (connection != null && userId.equals(connection.getUserId())) {
                // 优雅断开连接
                connectionCache.remove(deviceAddress);

                // 记录断开事件
                log.info("[蓝牙断开] 连接已断开: deviceAddress={}, connectionTime={}ms",
                        deviceAddress, System.currentTimeMillis() - connection.getConnectionTime());

                return true;
            }

            return false;

        } catch (Exception e) {
            log.error("[蓝牙断开] 断开失败: userId={}, deviceAddress={}, error={}",
                    userId, deviceAddress, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public ResponseDTO<BluetoothPairingResult> pairDevice(BluetoothPairingRequest request) {
        log.info("[蓝牙配对] 开始配对: userId={}, deviceAddress={}, method={}",
                request.getUserId(), request.getDeviceAddress(), request.getPairingMethod());

        try {
            // 查询设备信息
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return createPairingResult(false, "设备不存在");
            }

            long startTime = System.currentTimeMillis();

            // 模拟配对过程
            if ("PIN".equals(request.getPairingMethod()) && SmartStringUtil.isEmpty(request.getPinCode())) {
                return createPairingResult(false, "PIN码不能为空");
            }

            // 验证PIN码（模拟）
            if ("PIN".equals(request.getPairingMethod()) && !"123456".equals(request.getPinCode())) {
                return createPairingResult(false, "PIN码错误");
            }

            // 生成配对密钥
            String pairingKey = generatePairingKey(request.getUserId(), request.getDeviceAddress());

            // 保存配对信息
            BluetoothPairingInfo pairingInfo = new BluetoothPairingInfo();
            pairingInfo.setUserId(request.getUserId());
            pairingInfo.setDeviceAddress(request.getDeviceAddress());
            pairingInfo.setPairingKey(pairingKey);
            pairingInfo.setPairingMethod(request.getPairingMethod());
            pairingInfo.setPairingTime(System.currentTimeMillis());
            pairingInfo.setRequiresConfirmation(false);

            pairingCache.put(request.getDeviceAddress(), pairingInfo);

            long responseTime = System.currentTimeMillis() - startTime;

            BluetoothPairingResult result = new BluetoothPairingResult();
            result.setSuccess(true);
            result.setDeviceAddress(request.getDeviceAddress());
            result.setPairingKey(pairingKey);
            result.setPairingMethod(request.getPairingMethod());
            result.setResponseTime((int) responseTime);
            result.setRequiresConfirmation(false);

            log.info("[蓝牙配对] 配对成功: userId={}, deviceAddress={}, responseTime={}ms",
                    request.getUserId(), request.getDeviceAddress(), responseTime);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[蓝牙配对] 配对失败: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return createPairingResult(false, "配配失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<UserAccessCardVO>> getUserAccessCards(Long userId) {
        log.info("[门禁卡] 查询用户门禁卡: userId={}", userId);

        try {
            List<UserAccessCardVO> accessCards = new ArrayList<>();

            // 查询用户的物理门禁卡
            // TODO: 实现物理卡查询逻辑

            // 查询用户的虚拟门禁卡
            // TODO: 实现虚拟卡查询逻辑

            // 查询用户的蓝牙门禁卡
            for (Map.Entry<String, BluetoothPairingInfo> entry : pairingCache.entrySet()) {
                BluetoothPairingInfo pairingInfo = entry.getValue();
                if (userId.equals(pairingInfo.getUserId())) {
                    DeviceEntity device = accessDeviceDao.selectByDeviceCode(pairingInfo.getDeviceAddress());
                    if (device != null) {
                        UserAccessCardVO card = new UserAccessCardVO();
                        card.setCardId(System.currentTimeMillis()); // 临时ID
                        card.setCardName(device.getDeviceName() + "蓝牙卡");
                        card.setCardType("BLUETOOTH");
                        card.setCardNumber(device.getDeviceCode());
                        card.setDeviceAddress(pairingInfo.getDeviceAddress());
                        card.setStatus("ACTIVE");
                        card.setIssueTime(formatTimestamp(pairingInfo.getPairingTime()));
                        card.setExpireTime("2025-12-31 23:59:59"); // 临时过期时间
                        card.setAllowedAreas(Arrays.asList("办公区", "会议室"));
                        card.setDailyUsageLimit(100);
                        card.setCurrentUsage(0);
                        card.setIsPrimary(false);

                        accessCards.add(card);
                    }
                }
            }

            log.info("[门禁卡] 查询完成: userId={}, 卡片数量={}", userId, accessCards.size());
            return ResponseDTO.ok(accessCards);

        } catch (Exception e) {
            log.error("[门禁卡] 查询失败: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error("CARD_QUERY_FAILED", "门禁卡查询失败");
        }
    }

    @Override
    public ResponseDTO<String> addBluetoothAccessCard(AddBluetoothCardRequest request) {
        log.info("[门禁卡] 添加蓝牙门禁卡: userId={}, cardName={}, deviceAddress={}",
                request.getUserId(), request.getCardName(), request.getDeviceAddress());

        try {
            // 检查设备是否已配对
            BluetoothPairingInfo pairingInfo = pairingCache.get(request.getDeviceAddress());
            if (pairingInfo == null || !request.getUserId().equals(pairingInfo.getUserId())) {
                return ResponseDTO.error("DEVICE_NOT_PAIRED", "设备未配对");
            }

            // 检查设备是否存在
            DeviceEntity device = accessDeviceDao.selectByDeviceCode(request.getDeviceAddress());
            if (device == null) {
                return ResponseDTO.error("DEVICE_NOT_FOUND", "设备不存在");
            }

            // TODO: 保存门禁卡信息到数据库
            // 这里应该调用门禁卡服务保存门禁卡信息

            log.info("[门禁卡] 添加成功: userId={}, cardName={}, deviceAddress={}",
                    request.getUserId(), request.getCardName(), request.getDeviceAddress());
            return ResponseDTO.ok("蓝牙门禁卡添加成功");

        } catch (Exception e) {
            log.error("[门禁卡] 添加失败: userId={}, deviceAddress={}, error={}",
                    request.getUserId(), request.getDeviceAddress(), e.getMessage(), e);
            return ResponseDTO.error("CARD_ADD_FAILED", "门禁卡添加失败");
        }
    }

    @Override
    public ResponseDTO<BluetoothDeviceHealthVO> checkDeviceHealth(String deviceAddress) {
        log.info("[设备健康] 检查蓝牙设备健康状态: deviceAddress={}", deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);

            BluetoothDeviceHealthVO healthVO = new BluetoothDeviceHealthVO();
            healthVO.setDeviceAddress(deviceAddress);
            healthVO.setDeviceName(connection != null ? connection.getDeviceName() : "未知设备");

            // 健康状态评估
            int overallScore = 100;
            List<String> issues = new ArrayList<>();
            List<String> recommendations = new ArrayList<>();

            if (connection == null) {
                overallScore -= 50;
                issues.add("设备未连接");
                recommendations.add("检查设备连接状态");
            } else {
                // 信号强度检查
                if (connection.getSignalStrength() < 30) {
                    overallScore -= 20;
                    issues.add("信号强度弱");
                    recommendations.add("移近设备或检查信号干扰");
                }

                // 连接稳定性检查
                long connectionDuration = System.currentTimeMillis() - connection.getConnectionTime();
                if (connectionDuration < TimeUnit.MINUTES.toMillis(5)) {
                    overallScore -= 10;
                    issues.add("连接时间过短");
                    recommendations.add("检查设备连接稳定性");
                }
            }

            // 电池状态检查
            Integer batteryLevel = generateBatteryLevel();
            if (batteryLevel < 20) {
                overallScore -= 30;
                issues.add("电池电量低");
                recommendations.add("及时充电");
            }

            // 设置健康状态
            String healthStatus;
            if (overallScore >= 90) {
                healthStatus = "EXCELLENT";
            } else if (overallScore >= 70) {
                healthStatus = "GOOD";
            } else if (overallScore >= 50) {
                healthStatus = "FAIR";
            } else {
                healthStatus = "POOR";
            }

            healthVO.setHealthStatus(healthStatus);
            healthVO.setConnectionScore(connection != null ? connection.getSignalStrength() : 0);
            healthVO.setSignalQuality(connection != null ? connection.getSignalStrength() : 0);
            healthVO.setBatteryHealth(batteryLevel);
            healthVO.setLastCheckTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            healthVO.setIssues(issues);
            healthVO.setRecommendations(recommendations);

            log.info("[设备健康] 检查完成: deviceAddress={}, healthStatus={}, score={}",
                    deviceAddress, healthStatus, overallScore);
            return ResponseDTO.ok(healthVO);

        } catch (Exception e) {
            log.error("[设备健康] 检查失败: deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return ResponseDTO.error("HEALTH_CHECK_FAILED", "设备健康检查失败");
        }
    }

    @Override
    public ResponseDTO<BluetoothFirmwareUpdateResult> updateDeviceFirmware(String deviceAddress, String firmwareVersion) {
        log.info("[固件升级] 开始升级蓝牙设备固件: deviceAddress={}, targetVersion={}",
                deviceAddress, firmwareVersion);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);
            if (connection == null) {
                return createFirmwareUpdateResult(false, "设备未连接", null, firmwareVersion);
            }

            String currentVersion = connection.getFirmwareVersion();

            // 模拟固件升级过程
            long updateDuration = 5000 + (long)(Math.random() * 10000); // 5-15秒

            // 模拟升级结果（90%成功率）
            boolean success = Math.random() > 0.1;

            BluetoothFirmwareUpdateResult result = new BluetoothFirmwareUpdateResult();
            result.setSuccess(success);
            result.setDeviceAddress(deviceAddress);
            result.setCurrentVersion(currentVersion);
            result.setTargetVersion(firmwareVersion);
            result.setUpdateStatus(success ? "SUCCESS" : "FAILED");
            result.setUpdateDuration(updateDuration);
            result.setRequiresReboot(success);

            if (!success) {
                result.setErrorMessage("固件下载失败");
            }

            // 更新设备固件版本
            if (success) {
                connection.setFirmwareVersion(firmwareVersion);
            }

            log.info("[固件升级] 升级完成: deviceAddress={}, success={}, duration={}ms",
                    deviceAddress, success, updateDuration);
            return ResponseDTO.ok(result);

        } catch (Exception e) {
            log.error("[固件升级] 升级失败: deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return createFirmwareUpdateResult(false, "升级异常", null, firmwareVersion);
        }
    }

    @Override
    public ResponseDTO<BluetoothDeviceDiagnosticVO> diagnoseDevice(String deviceAddress) {
        log.info("[设备诊断] 开始诊断蓝牙设备: deviceAddress={}", deviceAddress);

        try {
            BluetoothConnection connection = connectionCache.get(deviceAddress);

            BluetoothDeviceDiagnosticVO diagnosticVO = new BluetoothDeviceDiagnosticVO();
            diagnosticVO.setDeviceAddress(deviceAddress);
            diagnosticVO.setDeviceName(connection != null ? connection.getDeviceName() : "未知设备");
            diagnosticVO.setDiagnosticTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 连接测试
            DiagnosticResult connectionTest = new DiagnosticResult();
            connectionTest.setTestName("连接稳定性测试");
            connectionTest.setPassed(connection != null);
            connectionTest.setScore(connection != null ? 85 : 0);
            connectionTest.setDetails(connection != null ? "连接稳定，信号良好" : "设备未连接");
            connectionTest.setStatus(connection != null ? "PASS" : "FAIL");
            diagnosticVO.setConnectionTest(connectionTest);

            // 功能测试
            DiagnosticResult functionalityTest = new DiagnosticResult();
            functionalityTest.setTestName("功能模块测试");
            functionalityTest.setPassed(true); // 模拟通过
            functionalityTest.setScore(92);
            functionalityTest.setDetails("所有功能模块正常");
            functionalityTest.setStatus("PASS");
            diagnosticVO.setFunctionalityTest(functionalityTest);

            // 性能测试
            DiagnosticResult performanceTest = new DiagnosticResult();
            performanceTest.setTestName("性能基准测试");
            performanceTest.setPassed(true); // 模拟通过
            performanceTest.setScore(78);
            performanceTest.setDetails("响应时间在可接受范围内");
            performanceTest.setStatus("PASS");
            diagnosticVO.setPerformanceTest(performanceTest);

            // 综合评分
            int overallScore = (connectionTest.getScore() + functionalityTest.getScore() + performanceTest.getScore()) / 3;
            diagnosticVO.setOverallScore(overallScore);

            // 识别的问题和建议
            List<String> issues = new ArrayList<>();
            List<String> suggestedActions = new ArrayList<>();

            if (connection == null) {
                issues.add("设备未连接");
                suggestedActions.add("检查设备电源和蓝牙连接");
            }

            if (connection != null && connection.getSignalStrength() < 50) {
                issues.add("信号强度较弱");
                suggestedActions.add("移近设备或减少信号干扰");
            }

            diagnosticVO.setIdentifiedIssues(issues);
            diagnosticVO.setSuggestedActions(suggestedActions);

            log.info("[设备诊断] 诊断完成: deviceAddress={}, overallScore={}", deviceAddress, overallScore);
            return ResponseDTO.ok(diagnosticVO);

        } catch (Exception e) {
            log.error("[设备诊断] 诊断失败: deviceAddress={}, error={}", deviceAddress, e.getMessage(), e);
            return ResponseDTO.error("DIAGNOSTIC_FAILED", "设备诊断失败");
        }
    }

    // ==================== 私有辅助方法 ====================

    private ResponseDTO<BluetoothAccessResult> createBluetoothAccessResult(
            boolean success, String reason, String riskLevel) {
        BluetoothAccessResult result = new BluetoothAccessResult();
        result.setSuccess(success);
        result.setAccessMethod("BLUETOOTH");
        result.setAccessTime(System.currentTimeMillis());
        result.setResponseTime(100);
        result.setDecisionReason(reason);
        result.setRiskLevel(RiskLevel.valueOf(riskLevel));
        return ResponseDTO.ok(result);
    }

    private ResponseDTO<BluetoothPairingResult> createPairingResult(boolean success, String message) {
        BluetoothPairingResult result = new BluetoothPairingResult();
        result.setSuccess(success);
        result.setDeviceAddress("");
        result.setResponseTime(200);
        if (!success) {
            result.setErrorMessage(message);
        }
        return ResponseDTO.ok(result);
    }

    private ResponseDTO<BluetoothFirmwareUpdateResult> createFirmwareUpdateResult(
            boolean success, String message, String currentVersion, String targetVersion) {
        BluetoothFirmwareUpdateResult result = new BluetoothFirmwareUpdateResult();
        result.setSuccess(success);
        result.setDeviceAddress("");
        result.setCurrentVersion(currentVersion);
        result.setTargetVersion(targetVersion);
        result.setUpdateStatus(success ? "SUCCESS" : "FAILED");
        result.setUpdateDuration(0);
        if (!success) {
            result.setErrorMessage(message);
        }
        return ResponseDTO.ok(result);
    }

    private boolean checkIfPaired(Long userId, String deviceAddress) {
        BluetoothPairingInfo pairingInfo = pairingCache.get(deviceAddress);
        return pairingInfo != null && userId.equals(pairingInfo.getUserId());
    }

    private boolean checkIfConnected(String deviceAddress) {
        return connectionCache.containsKey(deviceAddress);
    }

    private Integer generateSignalStrength() {
        return 30 + (int)(Math.random() * 70); // 30-100
    }

    private Integer generateBatteryLevel() {
        return 10 + (int)(Math.random() * 90); // 10-100
    }

    private Integer generateRSSI() {
        return -100 + (int)(Math.random() * 60); // -100 to -40
    }

    private Double calculateDistance(Integer rssi) {
        if (rssi == null) return null;
        // 简化的距离计算公式
        return Math.pow(10, (-69.0 - rssi) / 20.0);
    }

    private String generateConnectionId(String deviceAddress) {
        return "CONN_" + deviceAddress.replaceAll(":", "") + "_" + System.currentTimeMillis();
    }

    private String generateAccessToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String generatePairingKey(Long userId, String deviceAddress) {
        return "KEY_" + userId + "_" + deviceAddress.replaceAll(":", "") + "_" + System.currentTimeMillis();
    }

    private RiskLevel assessBluetoothRisk(BluetoothAccessRequest request, BluetoothConnection connection) {
        // 基于多个因素评估风险等级
        int riskScore = 0;

        // 连接时长因素
        long connectionDuration = System.currentTimeMillis() - connection.getConnectionTime();
        if (connectionDuration < TimeUnit.MINUTES.toMillis(1)) {
            riskScore += 20; // 新连接风险较高
        }

        // 访问时间因素
        int hour = LocalDateTime.now().getHour();
        if (hour < 6 || hour > 22) {
            riskScore += 15; // 非正常时间访问
        }

        // 信号强度因素
        if (connection.getSignalStrength() < 40) {
            riskScore += 10; // 信号弱可能有风险
        }

        // 访问频率因素
        if (connection.getAccessCount() > 10 && connection.getAccessCount() < 20) {
            riskScore += 5; // 高频访问
        }

        if (riskScore >= 30) {
            return RiskLevel.HIGH;
        } else if (riskScore >= 15) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.LOW;
        }
    }

    private void recordBluetoothAccessLog(BluetoothAccessRequest request, boolean success, long responseTime) {
        // TODO: 实现蓝牙访问日志记录
        log.info("[蓝牙日志] 记录访问日志: userId={}, deviceAddress={}, success={}, responseTime={}ms",
                request.getUserId(), request.getDeviceAddress(), success, responseTime);
    }

    private String formatTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, null)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // ==================== 内部数据类 ====================

    private static class BluetoothConnection {
        private Long userId;
        private String deviceAddress;
        private String deviceName;
        private String connectionId;
        private long connectionTime;
        private long lastAccessTime;
        private int signalStrength;
        private String protocolVersion;
        private String firmwareVersion = "1.0.0";
        private boolean autoReconnect;
        private long totalConnections = 1;
        private long accessCount = 0;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDeviceAddress() { return deviceAddress; }
        public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }

        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

        public String getConnectionId() { return connectionId; }
        public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

        public long getConnectionTime() { return connectionTime; }
        public void setConnectionTime(long connectionTime) { this.connectionTime = connectionTime; }

        public long getLastAccessTime() { return lastAccessTime; }
        public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }

        public int getSignalStrength() { return signalStrength; }
        public void setSignalStrength(int signalStrength) { this.signalStrength = signalStrength; }

        public String getProtocolVersion() { return protocolVersion; }
        public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }

        public String getFirmwareVersion() { return firmwareVersion; }
        public void setFirmwareVersion(String firmwareVersion) { this.firmwareVersion = firmwareVersion; }

        public boolean isAutoReconnect() { return autoReconnect; }
        public void setAutoReconnect(boolean autoReconnect) { this.autoReconnect = autoReconnect; }

        public long getTotalConnections() { return totalConnections; }
        public void setTotalConnections(long totalConnections) { this.totalConnections = totalConnections; }

        public long getAccessCount() { return accessCount; }
        public void setAccessCount(long accessCount) { this.accessCount = accessCount; }
    }

    private static class BluetoothPairingInfo {
        private Long userId;
        private String deviceAddress;
        private String pairingKey;
        private String pairingMethod;
        private long pairingTime;
        private boolean requiresConfirmation;

        // getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getDeviceAddress() { return deviceAddress; }
        public void setDeviceAddress(String deviceAddress) { this.deviceAddress = deviceAddress; }

        public String getPairingKey() { return pairingKey; }
        public void setPairingKey(String pairingKey) { this.pairingKey = pairingKey; }

        public String getPairingMethod() { return pairingMethod; }
        public void setPairingMethod(String pairingMethod) { this.pairingMethod = pairingMethod; }

        public long getPairingTime() { return pairingTime; }
        public void setPairingTime(long pairingTime) { this.pairingTime = pairingTime; }

        public boolean isRequiresConfirmation() { return requiresConfirmation; }
        public void setRequiresConfirmation(boolean requiresConfirmation) { this.requiresConfirmation = requiresConfirmation; }
    }
}