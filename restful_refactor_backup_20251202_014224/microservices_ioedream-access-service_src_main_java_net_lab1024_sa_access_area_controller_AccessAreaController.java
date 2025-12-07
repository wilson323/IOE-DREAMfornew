package net.lab1024.sa.access.area.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.area.service.AccessAreaService;
import net.lab1024.sa.access.area.domain.form.AreaCreateForm;
import net.lab1024.sa.access.area.domain.form.AreaUpdateForm;
import net.lab1024.sa.access.area.domain.form.AreaPermissionForm;
import net.lab1024.sa.access.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.access.area.domain.vo.AreaDetailVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 门禁区域管理控制器
 * 严格遵循四层架构规范：
 * - Controller层只负责参数验证和调用Service
 * - 使用统一响应格式ResponseDTO
 * - 权限控制注解@SaCheckPermission
 *
 * @author SmartAdmin Team
 * @since 2025-12-01
 */
@RestController
@RequestMapping("/api/access/area")
@Slf4j
public class AccessAreaController {

    @Resource
    private AccessAreaService accessAreaService;

    /**
     * 获取区域树形结构
     *
     * @return 区域树
     */
    @GetMapping("/tree")
    @SaCheckPermission("access:area:tree")
    public ResponseDTO<List<AreaTreeVO>> getAreaTree() {
        log.debug("[AccessAreaController] 获取区域树形结构");

        List<AreaTreeVO> areaTree = accessAreaService.getAreaTree();
        return ResponseDTO.userOk(areaTree);
    }

    /**
     * 创建区域
     *
     * @param form 创建表单
     * @return 创建结果
     */
    @PostMapping("")
    @SaCheckPermission("access:area:create")
    public ResponseDTO<String> createArea(@Valid @RequestBody AreaCreateForm form) {
        log.info("[AccessAreaController] 创建区域: areaName={}, parentId={}",
                form.getAreaName(), form.getParentId());

        String result = accessAreaService.createArea(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 更新区域
     *
     * @param areaId 区域ID
     * @param form 更新表单
     * @return 更新结果
     */
    @PutMapping("/{areaId}")
    @SaCheckPermission("access:area:update")
    public ResponseDTO<String> updateArea(@PathVariable Long areaId,
                                          @Valid @RequestBody AreaUpdateForm form) {
        log.info("[AccessAreaController] 更新区域: areaId={}, areaName={}", areaId, form.getAreaName());

        String result = accessAreaService.updateArea(areaId, form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 删除区域
     *
     * @param areaId 区域ID
     * @return 删除结果
     */
    @DeleteMapping("/{areaId}")
    @SaCheckPermission("access:area:delete")
    public ResponseDTO<String> deleteArea(@PathVariable Long areaId) {
        log.info("[AccessAreaController] 删除区域: areaId={}", areaId);

        String result = accessAreaService.deleteArea(areaId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取区域详情
     *
     * @param areaId 区域ID
     * @return 区域详情
     */
    @GetMapping("/{areaId}")
    @SaCheckPermission("access:area:detail")
    public ResponseDTO<AreaDetailVO> getAreaDetail(@PathVariable Long areaId) {
        log.debug("[AccessAreaController] 获取区域详情: areaId={}", areaId);

        AreaDetailVO area = accessAreaService.getAreaDetail(areaId);
        return ResponseDTO.userOk(area);
    }

    /**
     * 获取区域下的设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    @GetMapping("/{areaId}/devices")
    @SaCheckPermission("access:area:devices")
    public ResponseDTO<List<Map<String, Object>>> getAreaDevices(@PathVariable Long areaId) {
        log.debug("[AccessAreaController] 获取区域设备列表: areaId={}", areaId);

        List<Map<String, Object>> devices = accessAreaService.getAreaDevices(areaId);
        return ResponseDTO.userOk(devices);
    }

    /**
     * 分配设备到区域
     *
     * @param areaId 区域ID
     * @param deviceIds 设备ID列表
     * @return 分配结果
     */
    @PostMapping("/{areaId}/devices")
    @SaCheckPermission("access:area:assign-devices")
    public ResponseDTO<String> assignDevicesToArea(@PathVariable Long areaId,
                                                  @RequestBody List<String> deviceIds) {
        log.info("[AccessAreaController] 分配设备到区域: areaId={}, deviceCount={}", areaId, deviceIds.size());

        String result = accessAreaService.assignDevicesToArea(areaId, deviceIds);
        return ResponseDTO.userOk(result);
    }

    /**
     * 移除区域的设备
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 移除结果
     */
    @DeleteMapping("/{areaId}/devices/{deviceId}")
    @SaCheckPermission("access:area:remove-device")
    public ResponseDTO<String> removeDeviceFromArea(@PathVariable Long areaId,
                                                    @PathVariable String deviceId) {
        log.info("[AccessAreaController] 移除区域设备: areaId={}, deviceId={}", areaId, deviceId);

        String result = accessAreaService.removeDeviceFromArea(areaId, deviceId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 分配人员权限到区域
     *
     * @param form 权限分配表单
     * @return 分配结果
     */
    @PostMapping("/permission/assign")
    @SaCheckPermission("access:area:assign-permission")
    public ResponseDTO<String> assignPersonPermission(@Valid @RequestBody AreaPermissionForm form) {
        log.info("[AccessAreaController] 分配人员权限: areaId={}, userId={}, permissionType={}",
                form.getAreaId(), form.getUserId(), form.getPermissionType());

        String result = accessAreaService.assignPersonPermission(form);
        return ResponseDTO.userOk(result);
    }

    /**
     * 撤销人员区域权限
     *
     * @param areaId 区域ID
     * @param userId 用户ID
     * @return 撤销结果
     */
    @DeleteMapping("/{areaId}/permission/{userId}")
    @SaCheckPermission("access:area:revoke-permission")
    public ResponseDTO<String> revokePersonPermission(@PathVariable Long areaId,
                                                      @PathVariable Long userId) {
        log.info("[AccessAreaController] 撤销人员权限: areaId={}, userId={}", areaId, userId);

        String result = accessAreaService.revokePersonPermission(areaId, userId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取区域人员权限列表
     *
     * @param areaId 区域ID
     * @param permissionType 权限类型 (可选)
     * @return 权限列表
     */
    @GetMapping("/{areaId}/permissions")
    @SaCheckPermission("access:area:permissions")
    public ResponseDTO<List<Map<String, Object>>> getAreaPermissions(@PathVariable Long areaId,
                                                                   @RequestParam(required = false) String permissionType) {
        log.debug("[AccessAreaController] 获取区域人员权限: areaId={}, permissionType={}", areaId, permissionType);

        List<Map<String, Object>> permissions = accessAreaService.getAreaPermissions(areaId, permissionType);
        return ResponseDTO.userOk(permissions);
    }

    /**
     * 获取区域占用统计
     *
     * @param areaId 区域ID
     * @return 占用统计
     */
    @GetMapping("/{areaId}/occupancy")
    @SaCheckPermission("access:area:occupancy")
    public ResponseDTO<Map<String, Object>> getAreaOccupancy(@PathVariable Long areaId) {
        log.debug("[AccessAreaController] 获取区域占用统计: areaId={}", areaId);

        Map<String, Object> occupancy = accessAreaService.getAreaOccupancy(areaId);
        return ResponseDTO.userOk(occupancy);
    }

    /**
     * 获取区域访问统计
     *
     * @param areaId 区域ID
     * @param days 统计天数
     * @return 访问统计
     */
    @GetMapping("/{areaId}/access-stats")
    @SaCheckPermission("access:area:access-stats")
    public ResponseDTO<Map<String, Object>> getAreaAccessStats(@PathVariable Long areaId,
                                                              @RequestParam(defaultValue = "7") Integer days) {
        log.debug("[AccessAreaController] 获取区域访问统计: areaId={}, days={}", areaId, days);

        Map<String, Object> stats = accessAreaService.getAreaAccessStats(areaId, days);
        return ResponseDTO.userOk(stats);
    }

    /**
     * 批量设置区域权限
     *
     * @param areaId 区域ID
     * @param userIds 用户ID列表
     * @param permissionType 权限类型
     * @return 设置结果
     */
    @PostMapping("/{areaId}/batch-permission")
    @SaCheckPermission("access:area:batch-permission")
    public ResponseDTO<String> batchSetPermissions(@PathVariable Long areaId,
                                                   @RequestParam List<Long> userIds,
                                                   @RequestParam String permissionType) {
        log.info("[AccessAreaController] 批量设置区域权限: areaId={}, userCount={}, permissionType={}",
                areaId, userIds.size(), permissionType);

        String result = accessAreaService.batchSetPermissions(areaId, userIds, permissionType);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取区域层级路径
     *
     * @param areaId 区域ID
     * @return 层级路径
     */
    @GetMapping("/{areaId}/path")
    @SaCheckPermission("access:area:path")
    public ResponseDTO<List<Map<String, Object>>> getAreaPath(@PathVariable Long areaId) {
        log.debug("[AccessAreaController] 获取区域层级路径: areaId={}", areaId);

        List<Map<String, Object>> path = accessAreaService.getAreaPath(areaId);
        return ResponseDTO.userOk(path);
    }

    /**
     * 移动区域
     *
     * @param areaId 区域ID
     * @param newParentId 新父区域ID
     * @return 移动结果
     */
    @PutMapping("/{areaId}/move")
    @SaCheckPermission("access:area:move")
    public ResponseDTO<String> moveArea(@PathVariable Long areaId,
                                        @RequestParam Long newParentId) {
        log.info("[AccessAreaController] 移动区域: areaId={}, newParentId={}", areaId, newParentId);

        String result = accessAreaService.moveArea(areaId, newParentId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 复制区域权限
     *
     * @param sourceAreaId 源区域ID
     * @param targetAreaId 目标区域ID
     * @return 复制结果
     */
    @PostMapping("/{sourceAreaId}/copy-permission/{targetAreaId}")
    @SaCheckPermission("access:area:copy-permission")
    public ResponseDTO<String> copyAreaPermissions(@PathVariable Long sourceAreaId,
                                                    @PathVariable Long targetAreaId) {
        log.info("[AccessAreaController] 复制区域权限: sourceAreaId={}, targetAreaId={}", sourceAreaId, targetAreaId);

        String result = accessAreaService.copyAreaPermissions(sourceAreaId, targetAreaId);
        return ResponseDTO.userOk(result);
    }

    /**
     * 获取区域统计报表
     *
     * @param areaId 区域ID (可选，为空则全部区域)
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数据
     */
    @GetMapping("/report")
    @SaCheckPermission("access:area:report")
    public ResponseDTO<Map<String, Object>> getAreaReport(@RequestParam(required = false) Long areaId,
                                                          @RequestParam String reportType,
                                                          @RequestParam String startDate,
                                                          @RequestParam String endDate) {
        log.debug("[AccessAreaController] 获取区域报表: areaId={}, reportType={}, startDate={}, endDate={}",
                areaId, reportType, startDate, endDate);

        Map<String, Object> report = accessAreaService.getAreaReport(areaId, reportType, startDate, endDate);
        return ResponseDTO.userOk(report);
    }
}