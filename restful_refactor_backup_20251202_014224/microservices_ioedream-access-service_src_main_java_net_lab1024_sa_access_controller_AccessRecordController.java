package net.lab1024.sa.access.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.domain.entity.AccessRecordEntity;
import net.lab1024.sa.access.service.AccessRecordService;
import net.lab1024.sa.common.annotation.OperationLog;
import net.lab1024.sa.common.annotation.RequireResource;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;

/**
 * Access record controller
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Slf4j
@RestController
@RequestMapping("/api/access")
@Tag(name = "Access Management", description = "Access record related interfaces")
@SaCheckLogin
@Validated
public class AccessRecordController {

    @Resource
    private AccessRecordService accessRecordService;

    @Operation(summary = "Record access event", description = "Record access passage event")
    @PostMapping("/record")
    @RequireResource(resource = "access:record", dataScope = "ALL")
    @SaCheckPermission("access:record")
    @OperationLog(operationType = "CREATE", operationDesc = "Record access event")
    public ResponseDTO<String> recordAccessEvent(@RequestBody @Valid AccessRecordEntity record) {
        return accessRecordService.recordAccessEvent(record);
    }

    @Operation(summary = "Query access records by page", description = "Query access records by page with conditions")
    @GetMapping("/page")
    @RequireResource(resource = "access:page", dataScope = "ALL")
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
    @RequireResource(resource = "access:detail", dataScope = "ALL")
    @SaCheckPermission("access:detail")
    public ResponseDTO<AccessRecordEntity> getAccessRecordDetail(@PathVariable @NotNull Long recordId) {
        return accessRecordService.getAccessRecordDetail(recordId);
    }

    @Operation(summary = "Verify access permission", description = "Verify user access permission for device")
    @GetMapping("/verify")
    @RequireResource(resource = "access:verify", dataScope = "ALL")
    @SaCheckPermission("access:verify")
    public ResponseDTO<Boolean> verifyAccessPermission(
            @RequestParam @NotNull Long userId,
            @RequestParam @NotNull Long deviceId) {

        return accessRecordService.verifyAccessPermission(userId, deviceId);
    }
}
