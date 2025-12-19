package net.lab1024.sa.common.organization.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.organization.domain.vo.AreaPermissionVO;
import net.lab1024.sa.common.organization.service.AreaPermissionService;

/**
 * 区域权限管理接口
 * <p>
 * 当前用于管理端“区域权限弹窗”读取/删除权限记录。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@RestController
@Validated
@RequestMapping("/api/v1/area-permission")
public class AreaPermissionController {

    @Resource
    private AreaPermissionService areaPermissionService;

    @Observed(name = "areaPermission.list", contextualName = "area-permission-list")
    @GetMapping("/area/{areaId}")
    public ResponseDTO<List<AreaPermissionVO>> listByArea(
            @PathVariable @NotNull Long areaId,
            @RequestParam(required = false) String type) {
        return areaPermissionService.listByArea(areaId, type);
    }

    @Observed(name = "areaPermission.delete", contextualName = "area-permission-delete")
    @DeleteMapping("/{permissionId}")
    public ResponseDTO<Void> delete(@PathVariable String permissionId) {
        return areaPermissionService.delete(permissionId);
    }

    @Observed(name = "areaPermission.batchDelete", contextualName = "area-permission-batch-delete")
    @PostMapping("/batch-delete")
    public ResponseDTO<Void> batchDelete(@RequestBody @NotEmpty List<String> permissionIds) {
        return areaPermissionService.batchDelete(permissionIds);
    }
}

