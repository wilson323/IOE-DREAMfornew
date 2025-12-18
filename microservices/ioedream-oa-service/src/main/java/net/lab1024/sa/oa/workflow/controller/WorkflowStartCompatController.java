package net.lab1024.sa.oa.workflow.controller;

import java.util.Map;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.permission.annotation.PermissionCheck;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工作流启动兼容入口
 * <p>
 * 兼容 microservices-common 中 WorkflowApprovalManager 现有的调用路径：POST /api/v1/workflow/start
 * 内部转调 OA 工作流引擎的标准入口。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow")
@Tag(name = "工作流兼容入口", description = "兼容旧版工作流启动接口")
@PermissionCheck(value = "OA_WORKFLOW", description = "工作流引擎管理模块权限")
@Validated
public class WorkflowStartCompatController {

    @Resource
    private WorkflowEngineService workflowEngineService;

    /**
     * 启动流程实例（兼容接口）
     * <p>
     * 期望请求体结构与 WorkflowApprovalManager 构建的参数一致：
     * - definitionId
     * - businessKey
     * - instanceName
     * - variables
     * - formData
     * </p>
     */
    @Observed(name = "workflow.start.compat", contextualName = "workflow-start-compat")
    @Operation(summary = "启动流程实例（兼容接口）", description = "兼容旧版 /api/v1/workflow/start 调用路径")
    @PostMapping("/start")
    @PermissionCheck(value = "OA_WORKFLOW_START", description = "启动流程实例")
    public ResponseDTO<Long> start(@RequestBody Map<String, Object> request) {
        if (request == null) {
            return ResponseDTO.error("PARAM_ERROR", "请求体不能为空");
        }

        Long definitionId = toLong(request.get("definitionId"));
        String businessKey = toStringValue(request.get("businessKey"));
        String instanceName = toStringValue(request.get("instanceName"));

        if (definitionId == null) {
            return ResponseDTO.error("PARAM_ERROR", "definitionId不能为空");
        }

        Map<String, Object> variables = toMap(request.get("variables"));
        Map<String, Object> formData = toMap(request.get("formData"));

        log.info("[WorkflowCompat] start workflow, definitionId={}, businessKey={}", definitionId, businessKey);
        return workflowEngineService.startProcess(definitionId, businessKey, instanceName, variables, formData);
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }

    private static String toStringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> toMap(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }
}




