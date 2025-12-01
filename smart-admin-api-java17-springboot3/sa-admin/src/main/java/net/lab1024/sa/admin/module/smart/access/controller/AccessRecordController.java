package net.lab1024.sa.admin.module.smart.access.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.admin.module.smart.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.admin.module.smart.access.service.AccessRecordService;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.module.support.rbac.RequireResource;
import net.lab1024.sa.base.module.support.operationlog.annotation.OperationLog;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Access record controller
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@RestController
@RequestMapping("/api/smart/access")
@Slf4j
@Tag(name = "Access Management", description = "Access record related interfaces")
@SaCheckLogin
@Validated
public class AccessRecordController {

    @Resource
    private AccessRecordService accessRecordService;

    @Operation(summary = "Record access event", description = "Record access passage event")
    @PostMapping("/record")
    @RequireResource(code = "access:record", scope = "AREA")
    @SaCheckPermission("access:record")
    @OperationLog(operationType = "CREATE", operationDesc = "Record access event")
    public ResponseDTO<String> recordAccessEvent(@RequestBody @Valid AccessRecordEntity record) {
        return accessRecordService.recordAccessEvent(record);
    }

    @Operation(summary = "Query access records by page", description = "Query access records by page with conditions")
    @GetMapping("/page")
    @RequireResource(code = "access:page", scope = "AREA")
    @SaCheckPermission("access:page")
    public ResponseDTO<PageResult<AccessRecordEntity>> queryAccessRecordPage(
            @Valid PageParam pageParam,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String accessResult) {

        PageResult<AccessRecordEntity> pageResult = accessRecordService.queryAccessRecordPage(pageParam, userId,
                deviceId, accessResult);

        return ResponseDTO.ok(pageResult);
    }

    @Operation(summary = "Get access record detail", description = "Get access record detail by record ID")
    @GetMapping("/{recordId}")
    @RequireResource(code = "access:detail", scope = "AREA")
    @SaCheckPermission("access:detail")
    public ResponseDTO<AccessRecordEntity> getAccessRecordDetail(@PathVariable @NotNull Long recordId) {
        return accessRecordService.getAccessRecordDetail(recordId);
    }

    @Operation(summary = "Verify access permission", description = "Verify user access permission for device")
    @GetMapping("/verify")
    @RequireResource(code = "access:verify", scope = "AREA")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Boolean> verifyAccessPermission(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long deviceId) {

        return accessRecordService.verifyAccessPermission(userId, deviceId);
    }
}