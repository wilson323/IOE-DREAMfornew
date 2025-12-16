package net.lab1024.sa.common.system.area.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaAddForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaQueryForm;
import net.lab1024.sa.common.system.area.domain.form.SystemAreaUpdateForm;
import net.lab1024.sa.common.system.area.domain.vo.SystemAreaVO;
import net.lab1024.sa.common.system.area.service.SystemAreaService;

/**
 * 系统区域管理接口
 * <p>
 * 同时兼容 legacy `/system/area` 与 canonical `/api/v1/system/area`。
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-12-13
 */
@RestController
@Validated
@RequestMapping({"/api/v1/system/area", "/system/area"})
public class SystemAreaController {

    @Resource
    private SystemAreaService systemAreaService;

    @Observed(name = "systemArea.queryPage", contextualName = "system-area-query-page")
    @PostMapping("/page")
    public ResponseDTO<PageResult<SystemAreaVO>> queryPage(@Valid @RequestBody SystemAreaQueryForm queryForm) {
        return ResponseDTO.ok(systemAreaService.queryPage(queryForm));
    }

    @Observed(name = "systemArea.getAreaTree", contextualName = "system-area-get-tree")
    @GetMapping("/tree")
    public ResponseDTO<List<SystemAreaVO>> getAreaTree() {
        return ResponseDTO.ok(systemAreaService.getAreaTree());
    }

    @Observed(name = "systemArea.getDetail", contextualName = "system-area-get-detail")
    @GetMapping("/detail/{areaId}")
    public ResponseDTO<SystemAreaVO> getDetail(@PathVariable Long areaId) {
        return ResponseDTO.ok(systemAreaService.getDetail(areaId));
    }

    @Observed(name = "systemArea.add", contextualName = "system-area-add")
    @PostMapping("/add")
    public ResponseDTO<Long> add(@Valid @RequestBody SystemAreaAddForm form) {
        return ResponseDTO.ok(systemAreaService.add(form));
    }

    @Observed(name = "systemArea.update", contextualName = "system-area-update")
    @PostMapping("/update")
    public ResponseDTO<Void> update(@Valid @RequestBody SystemAreaUpdateForm form) {
        systemAreaService.update(form);
        return ResponseDTO.ok();
    }

    @Observed(name = "systemArea.delete", contextualName = "system-area-delete")
    @PostMapping("/delete/{areaId}")
    public ResponseDTO<Void> delete(@PathVariable Long areaId) {
        systemAreaService.delete(areaId);
        return ResponseDTO.ok();
    }

    @Observed(name = "systemArea.batchDelete", contextualName = "system-area-batch-delete")
    @PostMapping("/batchDelete")
    public ResponseDTO<Void> batchDelete(@RequestBody List<Long> areaIds) {
        systemAreaService.batchDelete(areaIds);
        return ResponseDTO.ok();
    }
}

