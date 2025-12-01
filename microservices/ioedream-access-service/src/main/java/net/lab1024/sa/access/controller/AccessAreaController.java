package net.lab1024.sa.access.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.entity.AccessAreaEntity;
import net.lab1024.sa.access.domain.form.AccessAreaForm;
import net.lab1024.sa.access.domain.query.AccessAreaQuery;
import net.lab1024.sa.access.domain.vo.AccessAreaTreeVO;
import net.lab1024.sa.access.service.AccessAreaService;
import net.lab1024.sa.common.annotation.SaCheckPermission;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.common.util.SmartResponseUtil;
import net.lab1024.sa.common.annotation.RequireResource;

/**
 * 门禁区域管理控制器
 * <p>
 * 严格遵循repowiki规范实现：
 * - 使用@Resource依赖注入
 * - Controller只做参数验证和调用Service
 * - 统一异常处理和响应格式
 * - 完整的权限控制和参数验证
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/access/area")
@Tag(name = "门禁区域管理", description = "门禁区域管理相关接口")
@Validated
public class AccessAreaController {

    @Resource
    private AccessAreaService accessAreaService;

    /**
     * 获取区域树形结构
     *
     * @param parentId        父级区域ID，0表示获取根区域
     * @param includeChildren 是否包含子区域
     * @return 区域树结构
     */
    @GetMapping("/tree")
    @Operation(summary = "获取区域树形结构", description = "获取门禁区域树形结构，支持按父级查询")
    @RequireResource(resource = "access:area:query", dataScope = "ALL")
    public ResponseDTO<List<AccessAreaTreeVO>> getAreaTree(
            @Parameter(description = "父级区域ID，0表示根区域", example = "0") @RequestParam(defaultValue = "0") Long parentId,

            @Parameter(description = "是否包含子区域", example = "true") @RequestParam(defaultValue = "true") Boolean includeChildren) {

        log.info("获取区域树，parentId: {}, includeChildren: {}", parentId, includeChildren);
        List<AccessAreaTreeVO> areaTree = accessAreaService.getAreaTree(parentId, includeChildren);
        return SmartResponseUtil.success(areaTree);
    }

    /**
     * 获取区域详情
     *
     * @param areaId 区域ID
     * @return 区域详情信息
     */
    @GetMapping("/{areaId}")
    @Operation(summary = "获取区域详情", description = "根据区域ID获取详细信息")
    @SaCheckPermission("access:area:query")
    public ResponseDTO<AccessAreaEntity> getAreaDetail(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId) {

        log.info("获取区域详情，areaId: {}", areaId);
        AccessAreaEntity area = accessAreaService.getAreaById(areaId);
        return SmartResponseUtil.success(area);
    }

    /**
     * 分页查询区域列表
     *
     * @param pageParam 分页参数
     * @param areaName  区域名称（可选）
     * @param areaType  区域类型（可选）
     * @param status    区域状态（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询区域列表", description = "支持按名称、类型、状态等条件筛选")
    @SaCheckPermission("access:area:query")
    public ResponseDTO<PageResult<AccessAreaEntity>> getAreaPage(
            PageParam pageParam,
            @Parameter(description = "区域名称", example = "主园区") @RequestParam(required = false) String areaName,

            @Parameter(description = "区域类型", example = "1") @RequestParam(required = false) Integer areaType,

            @Parameter(description = "区域状态", example = "1") @RequestParam(required = false) Integer status) {

        log.info("分页查询区域，areaName: {}, areaType: {}, status: {}", areaName, areaType, status);

        // 构建查询对象
        AccessAreaQuery query = new AccessAreaQuery();
        query.setPageNum(pageParam.getPageNum());
        query.setPageSize(pageParam.getPageSize());
        query.setAreaName(areaName);
        query.setAreaType(areaType != null ? areaType.toString() : null);
        query.setStatus(status);

        PageResult<AccessAreaEntity> pageResult = accessAreaService.getAreaPage(query);
        return SmartResponseUtil.success(pageResult);
    }

    /**
     * 创建区域
     *
     * @param areaForm 区域表单
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "创建区域", description = "创建新的门禁区域")
    @SaCheckPermission("access:area:add")
    public ResponseDTO<String> addArea(@Valid @RequestBody AccessAreaForm areaForm) {

        log.info("创建区域，areaForm: {}", areaForm);
        accessAreaService.addArea(areaForm);
        return SmartResponseUtil.success("区域创建成功");
    }

    /**
     * 更新区域
     *
     * @param areaId   区域ID
     * @param areaForm 区域表单
     * @return 操作结果
     */
    @PutMapping("/{areaId}")
    @Operation(summary = "更新区域", description = "更新指定区域的信息")
    @SaCheckPermission("access:area:update")
    public ResponseDTO<String> updateArea(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId,

            @Valid @RequestBody AccessAreaForm areaForm) {

        log.info("更新区域，areaId: {}, areaForm: {}", areaId, areaForm);
        areaForm.setAreaId(areaId);
        accessAreaService.updateArea(areaForm);
        return SmartResponseUtil.success("区域更新成功");
    }

    /**
     * 删除区域
     *
     * @param areaId 区域ID
     * @return 操作结果
     */
    @DeleteMapping("/{areaId}")
    @Operation(summary = "删除区域", description = "删除指定的区域，会检查是否存在子区域或关联设备")
    @SaCheckPermission("access:area:delete")
    public ResponseDTO<String> deleteArea(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId) {

        log.info("删除区域，areaId: {}", areaId);
        accessAreaService.deleteArea(areaId);
        return SmartResponseUtil.success("区域删除成功");
    }

    /**
     * 批量删除区域
     *
     * @param areaIds 区域ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除区域", description = "批量删除多个区域")
    @SaCheckPermission("access:area:delete")
    public ResponseDTO<String> batchDeleteAreas(@RequestBody List<Long> areaIds) {

        log.info("批量删除区域，areaIds: {}", areaIds);
        accessAreaService.batchDeleteAreas(areaIds);
        return SmartResponseUtil.success("批量删除成功");
    }

    /**
     * 获取区域下的设备列表
     *
     * @param areaId          区域ID
     * @param includeChildren 是否包含子区域设备
     * @return 设备列表
     */
    @GetMapping("/{areaId}/devices")
    @Operation(summary = "获取区域设备列表", description = "获取指定区域下的设备列表")
    @SaCheckPermission("access:area:query")
    public ResponseDTO<List<Object>> getAreaDevices(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId,

            @Parameter(description = "是否包含子区域设备", example = "false") @RequestParam(defaultValue = "false") Boolean includeChildren) {

        log.info("获取区域设备列表，areaId: {}, includeChildren: {}", areaId, includeChildren);
        List<Object> devices = accessAreaService.getAreaDevices(areaId, includeChildren);
        return SmartResponseUtil.success(devices);
    }

    /**
     * 移动区域到指定父级
     *
     * @param areaId      区域ID
     * @param newParentId 新父级区域ID
     * @return 操作结果
     */
    @PutMapping("/{areaId}/move")
    @Operation(summary = "移动区域", description = "将区域移动到指定父级下")
    @SaCheckPermission("access:area:update")
    public ResponseDTO<String> moveArea(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId,

            @Parameter(description = "新父级区域ID", required = true, example = "2") @RequestParam @NotNull(message = "新父级区域ID不能为空") Long newParentId) {

        log.info("移动区域，areaId: {}, newParentId: {}", areaId, newParentId);
        accessAreaService.moveArea(areaId, newParentId);
        return SmartResponseUtil.success("区域移动成功");
    }

    /**
     * 更新区域状态
     *
     * @param areaId 区域ID
     * @param status 新状态
     * @return 操作结果
     */
    @PutMapping("/{areaId}/status")
    @Operation(summary = "更新区域状态", description = "启用或禁用区域")
    @SaCheckPermission("access:area:update")
    public ResponseDTO<String> updateAreaStatus(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId,

            @Parameter(description = "区域状态", required = true, example = "1") @RequestParam @NotNull(message = "区域状态不能为空") Integer status) {

        log.info("更新区域状态，areaId: {}, status: {}", areaId, status);
        accessAreaService.updateAreaStatus(areaId, status);
        return SmartResponseUtil.success("区域状态更新成功");
    }

    /**
     * 验证区域编码唯一性
     *
     * @param areaCode      区域编码
     * @param excludeAreaId 排除的区域ID（编辑时使用）
     * @return 验证结果
     */
    @GetMapping("/validate/code")
    @Operation(summary = "验证区域编码", description = "验证区域编码是否已存在")
    @SaCheckPermission("access:area:query")
    public ResponseDTO<Boolean> validateAreaCode(
            @Parameter(description = "区域编码", required = true, example = "AREA_001") @RequestParam @NotNull(message = "区域编码不能为空") String areaCode,

            @Parameter(description = "排除的区域ID（编辑时使用）", example = "1") @RequestParam(required = false) Long excludeAreaId) {

        log.info("验证区域编码，areaCode: {}, excludeAreaId: {}", areaCode, excludeAreaId);
        boolean isUnique = accessAreaService.validateAreaCode(areaCode, excludeAreaId);
        return SmartResponseUtil.success(isUnique);
    }

    /**
     * 获取区域统计信息
     *
     * @param areaId 区域ID
     * @return 统计信息
     */
    @GetMapping("/{areaId}/statistics")
    @Operation(summary = "获取区域统计", description = "获取区域的设备、用户等统计信息")
    @SaCheckPermission("access:area:query")
    public ResponseDTO<Object> getAreaStatistics(
            @Parameter(description = "区域ID", required = true, example = "1") @PathVariable @NotNull(message = "区域ID不能为空") Long areaId) {

        log.info("获取区域统计，areaId: {}", areaId);
        Object statistics = accessAreaService.getAreaStatistics(areaId);
        return SmartResponseUtil.success(statistics);
    }
}
