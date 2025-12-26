package net.lab1024.sa.access.manager;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.DeviceImportBatchDao;
import net.lab1024.sa.access.dao.DeviceImportErrorDao;
import net.lab1024.sa.access.dao.DeviceImportSuccessDao;
import net.lab1024.sa.access.domain.entity.DeviceImportBatchEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportErrorEntity;
import net.lab1024.sa.access.domain.entity.DeviceImportSuccessEntity;
import net.lab1024.sa.access.domain.form.DeviceDiscoveryRequestForm;
import net.lab1024.sa.access.domain.vo.DeviceDiscoveryResultVO;
import net.lab1024.sa.access.domain.vo.DiscoveredDeviceVO;
import net.lab1024.sa.access.service.DeviceDiscoveryService;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.organization.dao.DeviceDao;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备发现管理器
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 纯Java类，不使用Spring注解
 * - 通过构造函数注入依赖
 * - 在配置类中注册为Bean
 * </p>
 * <p>
 * 核心职责：
 * - 编排设备发现的完整业务流程
 * - 设备去重和验证
 * - 批量添加发现的设备
 * - 记录导入历史
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
public class DeviceDiscoveryManager {

    // 显式添加logger声明以确保编译通过

    private final DeviceDao deviceDao;
    private final DeviceImportBatchDao deviceImportBatchDao;
    private final DeviceImportSuccessDao deviceImportSuccessDao;
    private final DeviceImportErrorDao deviceImportErrorDao;
    private final DeviceDiscoveryService deviceDiscoveryService;
    private final GatewayServiceClient gatewayServiceClient;
    private final RestTemplate restTemplate;

    /**
     * 构造函数注入依赖
     */
    public DeviceDiscoveryManager(
            DeviceDao deviceDao,
            DeviceImportBatchDao deviceImportBatchDao,
            DeviceImportSuccessDao deviceImportSuccessDao,
            DeviceImportErrorDao deviceImportErrorDao,
            DeviceDiscoveryService deviceDiscoveryService,
            GatewayServiceClient gatewayServiceClient,
            RestTemplate restTemplate) {
        this.deviceDao = deviceDao;
        this.deviceImportBatchDao = deviceImportBatchDao;
        this.deviceImportSuccessDao = deviceImportSuccessDao;
        this.deviceImportErrorDao = deviceImportErrorDao;
        this.deviceDiscoveryService = deviceDiscoveryService;
        this.gatewayServiceClient = gatewayServiceClient;
        this.restTemplate = restTemplate;
    }

    /**
     * 编排设备发现和批量添加流程
     * <p>
     * 完整流程：
     * 1. 启动设备自动发现
     * 2. 等待发现完成
     * 3. 验证发现的设备
     * 4. 去重（排除已存在的设备）
     * 5. 批量添加新设备
     * 6. 记录导入历史
     * </p>
     *
     * @param subnet 子网地址
     * @param protocols 发现协议列表
     * @param timeout 超时时间（秒）
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @return 批量添加结果
     */
    public DeviceDiscoveryResultVO discoverAndBatchAdd(
            String subnet,
            List<String> protocols,
            Integer timeout,
            Long operatorId,
            String operatorName) {

        log.info("[设备发现管理器] 开始设备发现和批量添加: subnet={}, protocols={}, operatorId={}",
                subnet, protocols, operatorId);

        try {
            // 1. 启动设备自动发现
            DeviceDiscoveryRequestForm requestForm = new DeviceDiscoveryRequestForm();
            requestForm.setSubnet(subnet);
            requestForm.setProtocols(protocols);
            requestForm.setTimeout(timeout);

            ResponseDTO<DeviceDiscoveryResultVO> startResponse = deviceDiscoveryService.discoverDevices(requestForm);

            if (startResponse == null || startResponse.getData() == null) {
                throw new RuntimeException("启动设备发现失败");
            }

            String scanId = startResponse.getData().getScanId();
            log.info("[设备发现管理器] 设备发现已启动: scanId={}", scanId);

            // 2. 等待发现完成
            DeviceDiscoveryResultVO finalResult = waitForDiscoveryComplete(scanId, timeout);

            List<DiscoveredDeviceVO> discoveredDevices = finalResult.getDiscoveredDevices();
            log.info("[设备发现管理器] 设备发现完成: scanId={}, discoveredCount={}",
                    scanId, discoveredDevices.size());

            // 3. 批量添加发现的设备
            return batchAddDiscoveredDevices(discoveredDevices, operatorId, operatorName);

        } catch (Exception e) {
            log.error("[设备发现管理器] 设备发现和批量添加异常: error={}", e.getMessage(), e);
            throw new RuntimeException("设备发现和批量添加失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量添加发现的设备
     */
    public DeviceDiscoveryResultVO batchAddDiscoveredDevices(
            List<DiscoveredDeviceVO> devices,
            Long operatorId,
            String operatorName) {

        log.info("[设备发现管理器] 开始批量添加设备: deviceCount={}, operatorId={}",
                devices.size(), operatorId);

        // 创建导入批次记录
        DeviceImportBatchEntity batch = new DeviceImportBatchEntity();
        batch.setBatchName("设备自动发现-" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        batch.setFileName("discovery_batch.json");
        batch.setFileSize((long) devices.size());
        batch.setImportStatus(0); // 处理中
        batch.setStatusMessage("正在批量添加设备");
        batch.setStartTime(LocalDateTime.now());
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setOperatorType(1); // 管理员

        deviceImportBatchDao.insert(batch);
        Long batchId = batch.getBatchId();

        try {
            List<DeviceImportSuccessEntity> successList = new ArrayList<>();
            List<DeviceImportErrorEntity> errorList = new ArrayList<>();

            for (int i = 0; i < devices.size(); i++) {
                DiscoveredDeviceVO discoveredDevice = devices.get(i);
                Integer rowNumber = i + 1;

                try {
                    // 4. 检查设备是否已存在
                    if (isDeviceExists(discoveredDevice.getIpAddress(), discoveredDevice.getMacAddress())) {
                        // 设备已存在，跳过
                        DeviceImportErrorEntity error = new DeviceImportErrorEntity();
                        error.setBatchId(batchId);
                        error.setRowNumber(rowNumber);
                        error.setRawData("IP: " + discoveredDevice.getIpAddress());
                        error.setErrorCode("DEVICE_EXISTS");
                        error.setErrorMessage("设备已存在，跳过添加");
                        errorList.add(error);

                        log.debug("[设备发现管理器] 设备已存在，跳过: ip={}", discoveredDevice.getIpAddress());
                        continue;
                    }

                    // 5. 创建设备实体
                    DeviceEntity device = convertToDeviceEntity(discoveredDevice);
                    device.setCreateUserId(operatorId);
                    device.setUpdateUserId(operatorId);

                    // 6. 保存设备（这里应该调用DeviceService，但简化实现直接插入）
                    deviceDao.insert(device);

                    // 7. 记录成功
                    DeviceImportSuccessEntity success = new DeviceImportSuccessEntity();
                    success.setBatchId(batchId);
                    success.setRowNumber(rowNumber);
                    success.setDeviceCode(device.getDeviceCode());
                    success.setDeviceName(device.getDeviceName());
                    success.setImportedData(device.getDeviceCode());
                    successList.add(success);

                    log.debug("[设备发现管理器] 设备添加成功: ip={}, deviceCode={}",
                            discoveredDevice.getIpAddress(), device.getDeviceCode());

                } catch (Exception e) {
                    // 记录失败
                    DeviceImportErrorEntity error = new DeviceImportErrorEntity();
                    error.setBatchId(batchId);
                    error.setRowNumber(rowNumber);
                    error.setRawData("IP: " + discoveredDevice.getIpAddress());
                    error.setErrorCode("ADD_FAILED");
                    error.setErrorMessage("添加失败: " + e.getMessage());
                    errorList.add(error);

                    log.warn("[设备发现管理器] 设备添加失败: ip={}, error={}",
                            discoveredDevice.getIpAddress(), e.getMessage());
                }
            }

            // 8. 批量保存结果
            if (!successList.isEmpty()) {
                deviceImportSuccessDao.insertBatch(successList);
            }
            if (!errorList.isEmpty()) {
                deviceImportErrorDao.insertBatch(errorList);
            }

            // 9. 更新批次状态
            int totalCount = devices.size();
            int successCount = successList.size();
            int failedCount = errorList.size();
            int skippedCount = 0;

            deviceImportBatchDao.updateStatistics(batchId, totalCount, successCount, failedCount, skippedCount);

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(batch.getStartTime(), endTime).toMillis();

            int importStatus;
            String statusMessage;

            if (failedCount == 0) {
                importStatus = 1; // 全部成功
                statusMessage = "批量添加完成";
            } else if (successCount > 0) {
                importStatus = 2; // 部分成功
                statusMessage = "批量添加部分成功";
            } else {
                importStatus = 3; // 全部失败
                statusMessage = "批量添加失败";
            }

            deviceImportBatchDao.completeBatch(batchId, importStatus, statusMessage, endTime, durationMs);

            // 10. 构建返回结果
            DeviceDiscoveryResultVO result = new DeviceDiscoveryResultVO();
            result.setScanId("BATCH-" + batchId);
            result.setStatus("COMPLETED");
            result.setProgress(100);
            result.setTotalDevices(totalCount);
            result.setDiscoveredDevices(successList.stream()
                    .map(success -> {
                        DiscoveredDeviceVO vo = new DiscoveredDeviceVO();
                        vo.setIpAddress(success.getDeviceCode());
                        vo.setDeviceName(success.getDeviceName());
                        vo.setExistsInSystem(true);
                        return vo;
                    })
                    .collect(Collectors.toList()));

            log.info("[设备发现管理器] 批量添加完成: batchId={}, total={}, success={}, failed={}, durationMs={}",
                    batchId, totalCount, successCount, failedCount, durationMs);

            return result;

        } catch (Exception e) {
            log.error("[设备发现管理器] 批量添加异常: batchId={}, error={}", batchId, e.getMessage(), e);

            // 更新批次状态为失败
            deviceImportBatchDao.updateStatus(batchId, 3, "批量添加失败: " + e.getMessage());

            throw new RuntimeException("批量添加失败: " + e.getMessage(), e);
        }
    }

    /**
     * 等待设备发现完成
     */
    private DeviceDiscoveryResultVO waitForDiscoveryComplete(String scanId, Integer timeout) {
        log.info("[设备发现管理器] 等待设备发现完成: scanId={}", scanId);

        long endTime = System.currentTimeMillis() + timeout * 1000L;

        while (System.currentTimeMillis() < endTime) {
            try {
                Thread.sleep(2000); // 每2秒检查一次

                ResponseDTO<DeviceDiscoveryResultVO> progressResponse =
                        deviceDiscoveryService.getDiscoveryProgress(scanId);

                if (progressResponse != null && progressResponse.getData() != null) {
                    DeviceDiscoveryResultVO result = progressResponse.getData();

                    if ("COMPLETED".equals(result.getStatus()) ||
                        "FAILED".equals(result.getStatus())) {
                        log.info("[设备发现管理器] 设备发现已完成: scanId={}, status={}",
                                scanId, result.getStatus());
                        return result;
                    }

                    log.debug("[设备发现管理器] 设备发现进度: scanId={}, progress={}%",
                            scanId, result.getProgress());
                }

            } catch (Exception e) {
                log.warn("[设备发现管理器] 检查进度异常: scanId={}, error={}", scanId, e.getMessage());
            }
        }

        throw new RuntimeException("设备发现超时: scanId=" + scanId);
    }

    /**
     * 检查设备是否已存在（基于IP地址）
     * <p>
     * 注意：MAC地址存储在extendedAttributes JSON字段中，无法高效查询
     * 因此只使用IP地址进行去重检查
     * </p>
     */
    private boolean isDeviceExists(String ipAddress, String macAddress) {
        try {
            if (ipAddress == null || ipAddress.isEmpty()) {
                return false;
            }

            LambdaQueryWrapper<DeviceEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DeviceEntity::getIpAddress, ipAddress);

            Long count = deviceDao.selectCount(queryWrapper);
            return count != null && count > 0;

        } catch (Exception e) {
            log.warn("[设备发现管理器] 检查设备是否存在异常: ip={}, mac={}, error={}",
                    ipAddress, macAddress, e.getMessage());
            return false;
        }
    }

    /**
     * 转换为设备实体
     */
    private DeviceEntity convertToDeviceEntity(DiscoveredDeviceVO discoveredDevice) {
        DeviceEntity device = new DeviceEntity();

        // 生成设备编码（基于IP地址）
        String deviceCode = "DEV-" + discoveredDevice.getIpAddress().replace(".", "-");
        device.setDeviceCode(deviceCode);

        // 设备名称
        String deviceName = discoveredDevice.getDeviceName();
        if (deviceName == null || deviceName.isEmpty()) {
            deviceName = discoveredDevice.getDeviceBrand() + "-" +
                        discoveredDevice.getDeviceModel() + "-" +
                        discoveredDevice.getIpAddress();
        }
        device.setDeviceName(deviceName);

        // 设备类型
        device.setDeviceType(discoveredDevice.getDeviceType() != null ?
                discoveredDevice.getDeviceType() : 1);

        // 设备型号
        device.setModel(discoveredDevice.getDeviceModel());

        // 设备厂商
        device.setBrand(discoveredDevice.getDeviceBrand());

        // IP地址和端口
        device.setIpAddress(discoveredDevice.getIpAddress());
        device.setPort(discoveredDevice.getPort() != null ? discoveredDevice.getPort() : 80);

        // 设备状态
        device.setDeviceStatus(discoveredDevice.getDeviceStatus() != null ?
                discoveredDevice.getDeviceStatus() : 1);

        // 默认值
        device.setEnabled(1);  // 1-启用
        // deleted字段由MyBatis-Plus @TableLogic管理，无需手动设置

        // 扩展属性（包含MAC地址、位置、协议等）
        Map<String, Object> extendedAttrs = new HashMap<>();
        if (discoveredDevice.getMacAddress() != null) {
            extendedAttrs.put("macAddress", discoveredDevice.getMacAddress());
        }
        if (discoveredDevice.getLocation() != null) {
            extendedAttrs.put("location", discoveredDevice.getLocation());
        }
        if (discoveredDevice.getProtocol() != null) {
            extendedAttrs.put("protocolType", discoveredDevice.getProtocol());
        }
        if (discoveredDevice.getDeviceInfo() != null) {
            extendedAttrs.put("deviceInfo", discoveredDevice.getDeviceInfo());
        }

        // 转换为JSON字符串存储
        try {
            String extendedJson = JSON.toJSONString(extendedAttrs);
            device.setExtendedAttributes(extendedJson);
        } catch (Exception e) {
            log.warn("[设备发现] 扩展属性JSON转换失败: {}", e.getMessage());
        }

        return device;
    }
}
