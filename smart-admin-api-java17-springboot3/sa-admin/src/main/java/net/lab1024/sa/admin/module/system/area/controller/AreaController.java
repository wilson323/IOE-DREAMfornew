package net.lab1024.sa.admin.module.system.area.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaAddForm;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaQueryForm;
import net.lab1024.sa.admin.module.system.area.domain.form.AreaUpdateForm;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaTreeVO;
import net.lab1024.sa.admin.module.system.area.domain.vo.AreaVO;
import net.lab1024.sa.admin.module.system.area.service.AreaService;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区域管理Controller
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@RestController
@RequestMapping("/api/system/area")
@Tag(name = "区域管理", description = "区域管理相关接口")
@SaCheckLogin
public class AreaController {

    @Resource
    private AreaService areaService;

    @Operation(summary = "分页查询区域")
    @PostMapping("/page")
    @SaCheckPermission("area:page")
    public ResponseDTO<PageResult<AreaVO>> queryPage(@RequestBody @Valid AreaQueryForm queryForm) {
        PageResult<AreaVO> result = areaService.queryPage(queryForm);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取区域树")
    @GetMapping("/tree")
    @SaCheckPermission("area:tree")
    public ResponseDTO<List<AreaTreeVO>> getAreaTree() {
        List<AreaTreeVO> result = areaService.getAreaTree();
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取区域详情")
    @GetMapping("/detail/{areaId}")
    @SaCheckPermission("area:detail")
    public ResponseDTO<AreaVO> getDetail(@PathVariable Long areaId) {
        AreaVO result = areaService.getDetail(areaId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "新增区域")
    @PostMapping("/add")
    @SaCheckPermission("area:add")
    public ResponseDTO<Long> add(@RequestBody @Valid AreaAddForm addForm) {
        Long areaId = areaService.add(addForm);
        return ResponseDTO.ok(areaId);
    }

    @Operation(summary = "更新区域")
    @PostMapping("/update")
    @SaCheckPermission("area:update")
    public ResponseDTO<Void> update(@RequestBody @Valid AreaUpdateForm updateForm) {
        areaService.update(updateForm);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除区域")
    @PostMapping("/delete/{areaId}")
    @SaCheckPermission("area:delete")
    public ResponseDTO<Void> delete(@PathVariable Long areaId) {
        areaService.delete(areaId);
        return ResponseDTO.ok();
    }

    @Operation(summary = "批量删除区域")
    @PostMapping("/batchDelete")
    @SaCheckPermission("area:delete")
    public ResponseDTO<Void> batchDelete(@RequestBody List<Long> areaIds) {
        areaService.batchDelete(areaIds);
        return ResponseDTO.ok();
    }

}
