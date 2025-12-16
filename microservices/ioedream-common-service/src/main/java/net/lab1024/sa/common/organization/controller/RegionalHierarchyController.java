package net.lab1024.sa.common.organization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import net.lab1024.sa.common.organization.manager.RegionalHierarchyManager;
import net.lab1024.sa.common.organization.service.RegionalHierarchyService;
import net.lab1024.sa.common.response.ResponseDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 区域层级管理控制器
 * 提供五级层级架构：园区→建筑→楼层→区域→房间
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/organization/regional-hierarchy")
@RequiredArgsConstructor
@Tag(name = "区域层级管理", description = "区域层级管理相关接口")
@Validated
public class RegionalHierarchyController {

    private final RegionalHierarchyService regionalHierarchyService;

    @GetMapping("/path/{areaId}")
    @Operation(summary = "获取区域层级路径", description = "获取指定区域的完整层级路径")
    public ResponseDTO<String> getAreaHierarchyPath(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.getAreaHierarchyPath(areaId);
    }

    @GetMapping("/children")
    @Operation(summary = "获取子区域列表", description = "根据父区域ID和区域级别获取子区域列表")
    public ResponseDTO<List<AreaEntity>> getChildAreas(
            @Parameter(description = "父区域ID，null表示获取顶级园区")
            @RequestParam(required = false) Long parentAreaId,
            @Parameter(description = "区域级别，null表示获取所有级别")
            @RequestParam(required = false) Integer areaLevel) {
        return regionalHierarchyService.getChildAreas(parentAreaId, areaLevel);
    }

    @GetMapping("/children/all/{parentAreaId}")
    @Operation(summary = "获取所有下级区域", description = "递归获取指定区域的所有下级区域")
    public ResponseDTO<List<AreaEntity>> getAllChildAreas(
            @Parameter(description = "父区域ID", required = true)
            @PathVariable @NotNull Long parentAreaId) {
        return regionalHierarchyService.getAllChildAreas(parentAreaId);
    }

    @GetMapping("/parents/all/{areaId}")
    @Operation(summary = "获取所有上级区域", description = "递归获取指定区域的所有上级区域")
    public ResponseDTO<List<AreaEntity>> getAllParentAreas(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.getAllParentAreas(areaId);
    }

    @GetMapping("/validate/{areaId}")
    @Operation(summary = "验证区域层级结构", description = "检查指定区域的层级结构是否合法")
    public ResponseDTO<RegionalHierarchyManager.HierarchyValidationResult> validateHierarchy(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.validateHierarchy(areaId);
    }

    @GetMapping("/siblings/{areaId}")
    @Operation(summary = "获取同级区域", description = "获取指定区域的所有同级区域")
    public ResponseDTO<List<AreaEntity>> getSiblingAreas(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.getSiblingAreas(areaId);
    }

    @GetMapping("/tree")
    @Operation(summary = "获取区域层级树", description = "获取完整的区域层级树结构")
    public ResponseDTO<List<RegionalHierarchyManager.AreaHierarchyTree>> getAreaHierarchyTree(
            @Parameter(description = "根父区域ID，null表示从所有园区开始")
            @RequestParam(required = false) Long rootParentId) {
        return regionalHierarchyService.getAreaHierarchyTree(rootParentId);
    }

    @GetMapping("/statistics/{parentAreaId}")
    @Operation(summary = "获取区域统计信息", description = "获取指定区域下的统计信息")
    public ResponseDTO<RegionalHierarchyManager.AreaStatistics> getAreaStatistics(
            @Parameter(description = "父区域ID", required = true)
            @PathVariable @NotNull Long parentAreaId) {
        return regionalHierarchyService.getAreaStatistics(parentAreaId);
    }

    @GetMapping("/can-delete/{areaId}")
    @Operation(summary = "检查是否可删除区域", description = "检查指定区域是否可以删除（无子区域）")
    public ResponseDTO<Boolean> canDeleteArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.canDeleteArea(areaId);
    }

    @GetMapping("/type/{areaType}")
    @Operation(summary = "根据类型获取区域", description = "根据区域类型获取所有区域")
    public ResponseDTO<List<AreaEntity>> getAreasByType(
            @Parameter(description = "区域类型", required = true)
            @PathVariable @NotNull @Min(1) Integer areaType) {
        return regionalHierarchyService.getAreasByType(areaType);
    }

    @GetMapping("/code/{areaCode}")
    @Operation(summary = "根据编码获取区域", description = "根据区域编码查找区域")
    public ResponseDTO<AreaEntity> getAreaByCode(
            @Parameter(description = "区域编码", required = true)
            @PathVariable @NotBlank String areaCode) {
        return regionalHierarchyService.getAreaByCode(areaCode);
    }

    @GetMapping("/search")
    @Operation(summary = "搜索区域", description = "根据关键词搜索区域（按名称或编码）")
    public ResponseDTO<List<AreaEntity>> searchAreas(
            @Parameter(description = "搜索关键词", required = true)
            @RequestParam @NotBlank String keyword) {
        return regionalHierarchyService.searchAreas(keyword);
    }

    @PostMapping
    @Operation(summary = "创建区域", description = "创建新的区域，自动验证层级结构")
    public ResponseDTO<Long> createArea(@Valid @RequestBody AreaEntity areaEntity) {
        return regionalHierarchyService.createArea(areaEntity);
    }

    @PutMapping
    @Operation(summary = "更新区域", description = "更新区域信息")
    public ResponseDTO<Void> updateArea(@Valid @RequestBody AreaEntity areaEntity) {
        return regionalHierarchyService.updateArea(areaEntity);
    }

    @DeleteMapping("/{areaId}")
    @Operation(summary = "删除区域", description = "删除指定区域（检查是否有子区域）")
    public ResponseDTO<Void> deleteArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.deleteArea(areaId);
    }

    @PutMapping("/move/{areaId}")
    @Operation(summary = "移动区域", description = "将区域移动到新的父级下")
    public ResponseDTO<Void> moveArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId,
            @Parameter(description = "新的父区域ID", required = true)
            @RequestParam @NotNull Long newParentId,
            @Parameter(description = "新的层级级别")
            @RequestParam(required = false) Integer newLevel) {
        return regionalHierarchyService.moveArea(areaId, newParentId, newLevel);
    }

    @PutMapping("/batch")
    @Operation(summary = "批量操作区域", description = "对多个区域执行批量操作")
    public ResponseDTO<Void> batchOperation(
            @Parameter(description = "区域ID列表", required = true)
            @RequestParam @NotNull List<Long> areaIds,
            @Parameter(description = "操作类型", required = true)
            @RequestParam @NotBlank String operation) {
        return regionalHierarchyService.batchOperation(areaIds, operation);
    }

    @GetMapping("/config")
    @Operation(summary = "获取层级配置", description = "获取区域层级管理的配置信息")
    public ResponseDTO<Object> getHierarchyConfig() {
        return regionalHierarchyService.getHierarchyConfig();
    }

    @GetMapping("/validate/name")
    @Operation(summary = "验证区域名称唯一性", description = "检查区域名称在同一级别下是否唯一")
    public ResponseDTO<Boolean> validateAreaNameUnique(
            @Parameter(description = "区域名称", required = true)
            @RequestParam @NotBlank String areaName,
            @Parameter(description = "父区域ID")
            @RequestParam(required = false) Long parentAreaId,
            @Parameter(description = "区域级别", required = true)
            @RequestParam @NotNull Integer areaLevel,
            @Parameter(description = "排除的区域ID")
            @RequestParam(required = false) Long excludeAreaId) {
        return regionalHierarchyService.validateAreaNameUnique(areaName, parentAreaId, areaLevel, excludeAreaId);
    }

    @GetMapping("/validate/code")
    @Operation(summary = "验证区域编码唯一性", description = "检查区域编码是否唯一")
    public ResponseDTO<Boolean> validateAreaCodeUnique(
            @Parameter(description = "区域编码", required = true)
            @RequestParam @NotBlank String areaCode,
            @Parameter(description = "排除的区域ID")
            @RequestParam(required = false) Long excludeAreaId) {
        return regionalHierarchyService.validateAreaCodeUnique(areaCode, excludeAreaId);
    }

    @GetMapping("/path/ids/{areaId}")
    @Operation(summary = "获取区域路径ID列表", description = "获取从顶级到当前区域的路径ID列表")
    public ResponseDTO<List<Long>> getAreaPathIds(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.getAreaPathIds(areaId);
    }

    @GetMapping("/permission/check")
    @Operation(summary = "检查用户区域权限", description = "检查用户对指定区域的访问权限")
    public ResponseDTO<Boolean> checkUserAreaPermission(
            @Parameter(description = "用户ID", required = true)
            @RequestParam @NotNull Long userId,
            @Parameter(description = "区域ID", required = true)
            @RequestParam @NotNull Long areaId) {
        return regionalHierarchyService.checkUserAreaPermission(userId, areaId);
    }

    @GetMapping("/permission/user/{userId}")
    @Operation(summary = "获取用户可访问区域", description = "获取指定用户可访问的所有区域")
    public ResponseDTO<List<AreaEntity>> getUserAccessibleAreas(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        return regionalHierarchyService.getUserAccessibleAreas(userId);
    }

    // ==================== 便捷查询接口 ====================

    @GetMapping("/campus/list")
    @Operation(summary = "获取所有园区", description = "获取所有园区级别的区域")
    public ResponseDTO<List<AreaEntity>> getCampusList() {
        return regionalHierarchyService.getAreasByType(RegionalHierarchyManager.AreaType.CAMPUS);
    }

    @GetMapping("/building/list")
    @Operation(summary = "获取所有建筑", description = "获取所有建筑级别的区域")
    public ResponseDTO<List<AreaEntity>> getBuildingList() {
        return regionalHierarchyService.getAreasByType(RegionalHierarchyManager.AreaType.BUILDING);
    }

    @GetMapping("/floor/list")
    @Operation(summary = "获取所有楼层", description = "获取所有楼层级别的区域")
    public ResponseDTO<List<AreaEntity>> getFloorList() {
        return regionalHierarchyService.getAreasByType(RegionalHierarchyManager.AreaType.FLOOR);
    }

    @GetMapping("/room/list")
    @Operation(summary = "获取所有房间", description = "获取所有房间级别的区域")
    public ResponseDTO<List<AreaEntity>> getRoomList() {
        return regionalHierarchyService.getAreasByType(RegionalHierarchyManager.AreaType.ROOM);
    }

    @GetMapping("/building/{campusId}")
    @Operation(summary = "获取园区下的建筑", description = "获取指定园区下的所有建筑")
    public ResponseDTO<List<AreaEntity>> getBuildingsByCampus(
            @Parameter(description = "园区ID", required = true)
            @PathVariable @NotNull Long campusId) {
        return regionalHierarchyService.getChildAreas(campusId, RegionalHierarchyManager.AreaType.BUILDING);
    }

    @GetMapping("/floor/{buildingId}")
    @Operation(summary = "获取建筑下的楼层", description = "获取指定建筑下的所有楼层")
    public ResponseDTO<List<AreaEntity>> getFloorsByBuilding(
            @Parameter(description = "建筑ID", required = true)
            @PathVariable @NotNull Long buildingId) {
        return regionalHierarchyService.getChildAreas(buildingId, RegionalHierarchyManager.AreaType.FLOOR);
    }

    @GetMapping("/room/{floorId}")
    @Operation(summary = "获取楼层下的房间", description = "获取指定楼层下的所有房间")
    public ResponseDTO<List<AreaEntity>> getRoomsByFloor(
            @Parameter(description = "楼层ID", required = true)
            @PathVariable @NotNull Long floorId) {
        return regionalHierarchyService.getChildAreas(floorId, RegionalHierarchyManager.AreaType.ROOM);
    }

    // ==================== 管理操作接口 ====================

    @PutMapping("/enable/{areaId}")
    @Operation(summary = "启用区域", description = "启用指定的区域")
    public ResponseDTO<Void> enableArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.batchOperation(List.of(areaId), "enable");
    }

    @PutMapping("/disable/{areaId}")
    @Operation(summary = "禁用区域", description = "禁用指定的区域")
    public ResponseDTO<Void> disableArea(
            @Parameter(description = "区域ID", required = true)
            @PathVariable @NotNull Long areaId) {
        return regionalHierarchyService.batchOperation(List.of(areaId), "disable");
    }

    @PutMapping("/enable/batch")
    @Operation(summary = "批量启用区域", description = "批量启用多个区域")
    public ResponseDTO<Void> batchEnableAreas(
            @Parameter(description = "区域ID列表", required = true)
            @RequestParam @NotNull List<Long> areaIds) {
        return regionalHierarchyService.batchOperation(areaIds, "enable");
    }

    @PutMapping("/disable/batch")
    @Operation(summary = "批量禁用区域", description = "批量禁用多个区域")
    public ResponseDTO<Void> batchDisableAreas(
            @Parameter(description = "区域ID列表", required = true)
            @RequestParam @NotNull List<Long> areaIds) {
        return regionalHierarchyService.batchOperation(areaIds, "disable");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除区域", description = "批量删除多个区域（检查是否有子区域）")
    public ResponseDTO<Void> batchDeleteAreas(
            @Parameter(description = "区域ID列表", required = true)
            @RequestParam @NotNull List<Long> areaIds) {
        return regionalHierarchyService.batchOperation(areaIds, "delete");
    }
}