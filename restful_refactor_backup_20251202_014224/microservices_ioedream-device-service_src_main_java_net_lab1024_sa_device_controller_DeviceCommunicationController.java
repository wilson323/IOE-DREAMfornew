package net.lab1024.sa.device.controller;

import java.util.List;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.device.form.DeviceDataCollectionForm;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartVerificationUtil;
import net.lab1024.sa.device.domain.form.DeviceProtocolConfigForm;
import net.lab1024.sa.device.domain.vo.DeviceDataVO;
import net.lab1024.sa.device.domain.vo.DeviceProtocolVO;
import net.lab1024.sa.device.service.DeviceCommunicationService;

/**
 * 设备通信控制器
 *
 * 负责设备的通信管理、数据采集、协议配置等功能：
 * - 设备协议配置
 * - 数据采集管理
 * - 实时数据传输
 * - 设备通信监控
 *
 * @author IOE-DREAM Team
 */
@Slf4j
@RestController
@RequestMapping("/api/device/communication")
@Tag(name = "设备通信管理", description = "设备通信、数据采集、协议配置等功能")
@Validated
public class DeviceCommunicationController {

    @Resource
    private DeviceCommunicationService deviceCommunicationService;

    @PostMapping("/protocol/config")
    @Operation(summary = "配置设备协议", description = "为设备配置通信协议参数")
    public ResponseDTO<Long> configureDeviceProtocol(@RequestBody @Valid DeviceProtocolConfigForm configForm) {
        SmartVerificationUtil.verify(configForm.getDeviceId() != null && configForm.getDeviceId() > 0, "设备ID不能为空");

        log.info("配置设备协议，设备ID：{}，协议类型：{}", configForm.getDeviceId(), configForm.getProtocolType());

        Long configId = deviceCommunicationService.configureDeviceProtocol(configForm);

        log.info("设备协议配置完成，配置ID：{}", configId);

        return ResponseDTO.ok(configId);
    }

    @GetMapping("/protocol/list")
    @Operation(summary = "获取协议配置列表", description = "获取设备的协议配置列表")
    public ResponseDTO<List<DeviceProtocolVO>> getProtocolConfigList(@RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String protocolType) {
        log.info("获取协议配置列表，设备ID：{}，协议类型：{}", deviceId, protocolType);

        List<DeviceProtocolVO> protocolList = deviceCommunicationService.getProtocolConfigList(deviceId, protocolType);

        log.info("协议配置列表获取完成，配置数量：{}", protocolList.size());

        return ResponseDTO.ok(protocolList);
    }

    @PostMapping("/data/collect")
    @Operation(summary = "启动数据采集", description = "启动设备的数据采集任务")
    public ResponseDTO<Void> startDataCollection(@RequestBody @Valid DeviceDataCollectionForm collectionForm) {
        SmartVerificationUtil.verify(collectionForm.getDeviceId() != null && collectionForm.getDeviceId() > 0,
                "设备ID不能为空");

        log.info("启动设备数据采集，设备ID：{}，采集类型：{}", collectionForm.getDeviceId(), collectionForm.getCollectionType());

        deviceCommunicationService.startDataCollection(collectionForm);

        log.info("设备数据采集启动完成");

        return ResponseDTO.ok();
    }

    @PostMapping("/data/collect/{collectionId}/stop")
    @Operation(summary = "停止数据采集", description = "停止指定的数据采集任务")
    public ResponseDTO<Void> stopDataCollection(@PathVariable Long collectionId) {
        SmartVerificationUtil.verify(collectionId != null && collectionId > 0, "采集任务ID不能为空");

        log.info("停止设备数据采集，采集任务ID：{}", collectionId);

        deviceCommunicationService.stopDataCollection(collectionId);

        log.info("设备数据采集停止完成");

        return ResponseDTO.ok();
    }

    @GetMapping("/data/latest/{deviceId}")
    @Operation(summary = "获取最新数据", description = "获取设备的最新采集数据")
    public ResponseDTO<List<DeviceDataVO>> getLatestDeviceData(@PathVariable Long deviceId,
            @RequestParam(defaultValue = "10") Integer limit) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");
        SmartVerificationUtil.verify(limit > 0 && limit <= 1000, "数据量限制应在1-1000之间");

        log.info("获取设备最新数据，设备ID：{}，限制数量：{}", deviceId, limit);

        List<DeviceDataVO> latestData = deviceCommunicationService.getLatestDeviceData(deviceId, limit);

        log.info("设备最新数据获取完成，数据条数：{}", latestData.size());

        return ResponseDTO.ok(latestData);
    }

    @GetMapping("/data/history/{deviceId}")
    @Operation(summary = "获取历史数据", description = "获取设备的历史采集数据")
    public ResponseDTO<List<DeviceDataVO>> getDeviceDataHistory(@PathVariable Long deviceId,
            @RequestParam Long startTime,
            @RequestParam Long endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "100") Integer pageSize) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");
        SmartVerificationUtil.verify(startTime != null && endTime != null, "时间范围不能为空");
        SmartVerificationUtil.verify(startTime != null && endTime != null && startTime < endTime, "开始时间必须小于结束时间");

        log.info("获取设备历史数据，设备ID：{}，时间范围：{} - {}", deviceId, startTime, endTime);

        List<DeviceDataVO> historyData = deviceCommunicationService.getDeviceDataHistory(
                deviceId, startTime, endTime, pageNum, pageSize);

        log.info("设备历史数据获取完成，数据条数：{}", historyData.size());

        return ResponseDTO.ok(historyData);
    }

    @PostMapping("/send-command/{deviceId}")
    @Operation(summary = "发送设备命令", description = "向设备发送控制命令")
    public ResponseDTO<Map<String, Object>> sendDeviceCommand(@PathVariable Long deviceId,
            @RequestParam String command,
            @RequestBody(required = false) Map<String, Object> parameters) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");
        SmartVerificationUtil.verify(command != null && !command.trim().isEmpty(), "命令不能为空");

        log.info("发送设备命令，设备ID：{}，命令：{}", deviceId, command);

        Map<String, Object> result = deviceCommunicationService.sendDeviceCommand(deviceId, command, parameters);

        log.info("设备命令发送完成，结果：{}", result.get("status"));

        return ResponseDTO.ok(result);
    }

    @GetMapping("/status/{deviceId}")
    @Operation(summary = "获取通信状态", description = "获取设备的通信连接状态")
    public ResponseDTO<Map<String, Object>> getCommunicationStatus(@PathVariable Long deviceId) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("获取设备通信状态，设备ID：{}", deviceId);

        Map<String, Object> commStatus = deviceCommunicationService.getCommunicationStatus(deviceId);

        log.info("设备通信状态获取完成，连接状态：{}", commStatus.get("connected"));

        return ResponseDTO.ok(commStatus);
    }

    @GetMapping("/statistics/overview")
    @Operation(summary = "获取通信统计概览", description = "获取设备通信的整体统计信息")
    public ResponseDTO<Map<String, Object>> getCommunicationStatisticsOverview() {
        log.info("获取设备通信统计概览");

        Map<String, Object> statistics = deviceCommunicationService.getCommunicationStatisticsOverview();

        log.info("设备通信统计概览获取完成");

        return ResponseDTO.ok(statistics);
    }

    @PostMapping("/protocol/test")
    @Operation(summary = "测试协议连接", description = "测试设备协议连接是否正常")
    public ResponseDTO<Map<String, Object>> testProtocolConnection(
            @RequestBody @Valid DeviceProtocolConfigForm configForm) {
        SmartVerificationUtil.verify(configForm.getDeviceId() != null && configForm.getDeviceId() > 0, "设备ID不能为空");

        log.info("测试设备协议连接，设备ID：{}，协议类型：{}", configForm.getDeviceId(), configForm.getProtocolType());

        Map<String, Object> testResult = deviceCommunicationService.testProtocolConnection(configForm);

        log.info("设备协议连接测试完成，测试结果：{}", testResult.get("success"));

        return ResponseDTO.ok(testResult);
    }

    @GetMapping("/protocols/supported")
    @Operation(summary = "获取支持的协议列表", description = "获取系统支持的设备通信协议列表")
    public ResponseDTO<List<Map<String, Object>>> getSupportedProtocols() {
        log.info("获取支持的设备协议列表");

        List<Map<String, Object>> supportedProtocols = deviceCommunicationService.getSupportedProtocols();

        log.info("支持的设备协议列表获取完成，协议数量：{}", supportedProtocols.size());

        return ResponseDTO.ok(supportedProtocols);
    }

    @PostMapping("/data/batch-upload")
    @Operation(summary = "批量上传设备数据", description = "批量上传多个设备的数据")
    public ResponseDTO<Map<String, Integer>> batchUploadDeviceData(
            @RequestBody List<DeviceDataCollectionForm> dataForms) {
        SmartVerificationUtil.verify(dataForms != null && !dataForms.isEmpty(), "数据列表不能为空");
        int dataSize = dataForms != null ? dataForms.size() : 0;
        SmartVerificationUtil.verify(dataSize <= 1000, "批量上传最多支持1000条数据");

        log.info("批量上传设备数据，数据条数：{}", dataSize);

        Map<String, Integer> result = deviceCommunicationService.batchUploadDeviceData(dataForms);

        log.info("批量设备数据上传完成，成功：{}，失败：{}", result.get("success"), result.get("failed"));

        return ResponseDTO.ok(result);
    }

    @GetMapping("/data/statistics/{deviceId}")
    @Operation(summary = "获取数据统计", description = "获取指定设备的数据采集统计信息")
    public ResponseDTO<Map<String, Object>> getDataCollectionStatistics(@PathVariable Long deviceId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        SmartVerificationUtil.verify(deviceId != null && deviceId > 0, "设备ID不能为空");

        log.info("获取设备数据采集统计，设备ID：{}，时间范围：{} - {}", deviceId, startTime, endTime);

        Map<String, Object> statistics = deviceCommunicationService.getDataCollectionStatistics(deviceId, startTime,
                endTime);

        log.info("设备数据采集统计获取完成");

        return ResponseDTO.ok(statistics);
    }
}
