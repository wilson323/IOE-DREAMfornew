package net.lab1024.sa.access.device.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.device.service.AccessDeviceService;
import net.lab1024.sa.access.device.domain.form.DeviceConfigForm;
import net.lab1024.sa.access.device.domain.form.DeviceControlForm;
import net.lab1024.sa.access.device.domain.vo.DeviceStatusVO;
import net.lab1024.sa.access.device.domain.vo.DeviceDetailVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门禁设备控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/device")
@Slf4j
public class AccessDeviceController {

    @Resource
    private AccessDeviceService accessDeviceService;

    /**
     * 获取设备列表
     *
     * @param areaId 区域ID (可选)
     * @param deviceType 设备类型 (可选)
     * @param status 设备状态 (可选)
     * @return 设备列表
     */
    @GetMapping("/list")
    @SaCheckPermission("access:device:list")
    public ResponseDTO<List<DeviceStatusVO>> getDeviceList(@RequestParam(required = false) Long areaId,
                                                         @RequestParam(required = false) String deviceType,
                                                         @RequestParam(required = false) String status) {
        log.debug("[AccessDeviceController] 获取设备列表: areaId={}, deviceType={}, status={}",
                areaId, deviceType, status);

        List<DeviceStatusVO> devices = accessDeviceService.getDeviceList(areaId, deviceType, status);
        return ResponseDTO.userOk(devices);
    }

    /**
     * 获取设备详细信息
     *
     * @param deviceId 设备ID
     * @return 设备详情
     */
    @GetMapping("/{deviceId}")
    @SaCheckPermission("access:device:detail")
    public ResponseDTO<DeviceDetailVO> getDeviceDetail(@PathVariable String deviceId) {
        log.debug("[AccessDeviceController] 获取设备详情: deviceId={}", deviceId);

        DeviceDetailVO device = accessDeviceService.getDeviceDetail(deviceId);
        return ResponseDTO.userOk(device);
    }

    /**
     * 远程控制设备
     *
     * @param deviceId 设备ID
     * @param form 控制表单
     * @return 控制结果
     */
    @PostMapping("/{deviceId}/control")
    @SaCheckPermission("access:device:control")
    public ResponseDTO<String> controlDevice(@PathVariable String deviceId,
                                              @Valid @RequestBody DeviceControlForm form) {
        log.info("[AccessDeviceController] 远程控制设备: deviceId={}, action={}",
                deviceId, form.getAction());

        String result = accessDeviceService.controlDevice(deviceId, form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取设备实时状态
     *
     * @param deviceId 设备ID
     * @return 设备状态
     */
    @GetMapping("/{deviceId}/status")
    @SaCheckPermission("access:device:status")
    public ResponseDTO<DeviceStatusVO> getDeviceStatus(@PathVariable String deviceId) {
        log.debug("[AccessDeviceController] 获取设备状态: deviceId={}", deviceId);

        DeviceStatusVO status = accessDeviceService.getDeviceStatus(deviceId);
        return ResponseDTO.userOk(status);
    }

    /**
     * 配置设备参数
     *
     * @param deviceId 设备ID
     * @param form 配置表单
     * @return 配置结果
     */
    @PutMapping("/{deviceId}/config")
    @SaCheckPermission("access:device:config")
    public ResponseDTO<String> configDevice(@PathVariable String deviceId,
                                           @Valid @RequestBody DeviceConfigForm form) {
        log.info("[AccessDeviceController] 配置设备参数: deviceId={}", deviceId);

        String result = accessDeviceService.configDevice(deviceId, form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 重启设备
     *
     * @param deviceId 设备ID
     * @return 重启结果
     */
    @PostMapping("/{deviceId}/restart")
    @SaCheckPermission("access:device:restart")
    public ResponseDTO<String> restartDevice(@PathVariable String deviceId) {
        log.info("[AccessDeviceController] 重启设备: deviceId={}", deviceId);

        String result = accessDeviceService.restartDevice(deviceId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取设备统计信息
     *
     * @param areaId 区域ID (可选)
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @SaCheckPermission("access:device:statistics")
    public ResponseDTO<Map<String, Object>> getDeviceStatistics(@RequestParam(required = false) Long areaId) {
        log.debug("[AccessDeviceController] 获取设备统计: areaId={}", areaId);

        Map<String, Object> statistics = accessDeviceService.getDeviceStatistics(areaId);
        return ResponseDTO.userOk(statistics);
    }

    /**
     * 批量设备操作
     *
     * @param deviceIds 设备ID列表
     * @param action 操作类型
     * @return 操作结果
     */
    @PostMapping("/batch")
    @SaCheckPermission("access:device:batch")
    public ResponseDTO<Map<String, Object>> batchOperateDevices(@RequestParam List<String> deviceIds,
                                                              @RequestParam String action) {
        log.info("[AccessDeviceController] 批量设备操作: deviceCount={}, action={}", deviceIds.size(), action);

        Map<String, Object> result = accessDeviceService.batchOperateDevices(deviceIds, action);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取设备健康检查结果
     *
     * @param deviceId 设备ID
     * @return 健康检查结果
     */
    @GetMapping("/{deviceId}/health")
    @SaCheckPermission("access:device:health")
    public ResponseDTO<Map<String, Object>> getDeviceHealth(@PathVariable String deviceId) {
        log.debug("[AccessDeviceController] 设备健康检查: deviceId={}", deviceId);

        Map<String, Object> health = accessDeviceService.getDeviceHealth(deviceId);
        return ResponseDTO.userOk(health);
    }

    /**
     * 获取设备操作日志
     *
     * @param deviceId 设备ID
     * @param pageSize 页面大小
     * @param pageNum 页码
     * @return 操作日志
     */
    @GetMapping("/{deviceId}/logs")
    @SaCheckPermission("access:device:logs")
    public ResponseDTO<Map<String, Object>> getDeviceLogs(@PathVariable String deviceId,
                                                        @RequestParam(defaultValue = "20") Integer pageSize,
                                                        @RequestParam(defaultValue = "1") Integer pageNum) {
        log.debug("[AccessDeviceController] 获取设备操作日志: deviceId={}, pageSize={}, pageNum={}",
                deviceId, pageSize, pageNum);

        Map<String, Object> logs = accessDeviceService.getDeviceLogs(deviceId, pageSize, pageNum);
        return ResponseDTO.userOk(logs);
    }

    /**
     * 同步设备时间
     *
     * @param deviceId 设备ID
     * @return 同步结果
     */
    @PostMapping("/{deviceId}/sync-time")
    @SaCheckPermission("access:device:sync-time")
    public ResponseDTO<String> syncDeviceTime(@PathVariable String deviceId) {
        log.info("[AccessDeviceController] 同步设备时间: deviceId={}", deviceId);

        String result = accessDeviceService.syncDeviceTime(deviceId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取设备协议信息
     *
     * @param deviceId 设备ID
     * @return 协议信息
     */
    @GetMapping("/{deviceId}/protocol")
    @SaCheckPermission("access:device:protocol")
    public ResponseDTO<Map<String, Object>> getDeviceProtocol(@PathVariable String deviceId) {
        log.debug("[AccessDeviceController] 获取设备协议信息: deviceId={}", deviceId);

        Map<String, Object> protocol = accessDeviceService.getDeviceProtocol(deviceId);
        return ResponseDTO.userOk(protocol);
    }

    /**
     * 测试设备连接
     *
     * @param deviceId 设备ID
     * @return 测试结果
     */
    @PostMapping("/{deviceId}/test-connection")
    @SaCheckPermission("access:device:test-connection")
    public ResponseDTO<Map<String, Object>> testDeviceConnection(@PathVariable String deviceId) {
        log.info("[AccessDeviceController] 测试设备连接: deviceId={}", deviceId);

        Map<String, Object> result = accessDeviceService.testDeviceConnection(deviceId);
        return ResponseDTO.userOk(result);
    }
}