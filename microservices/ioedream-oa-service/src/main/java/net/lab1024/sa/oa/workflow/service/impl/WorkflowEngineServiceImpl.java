package net.lab1024.sa.oa.workflow.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.observation.annotation.Observed;
import jakarta.annotation.Resource;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.ParamException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.oa.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableHistoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableManagementService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableRepositoryService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableRuntimeService;
import net.lab1024.sa.oa.workflow.config.wrapper.FlowableTaskService;
import net.lab1024.sa.oa.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.oa.workflow.metrics.WorkflowEngineMetricsCollector;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;

/**
 * 工作流引擎服务实现类
 * 提供工作流流程定义、实例、任务管理的完整业务逻辑实现
 * 严格遵循CLAUDE.md架构规范
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
/**
 * 工作流引擎服务实现类 - 集成Flowable 6.8.0
 * <p>
 * 提供企业级工作流流程定义、实例、任务管理的完整业务逻辑实现
 * 基于Flowable 6.8.0工作流引擎，支持BPMN 2.0标准
 * 严格遵循CLAUDE.md架构规范和四层架构设计
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-16
 * @version 4.0.0 - Flowable 6.8.0 集成版
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    @Resource
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    @Resource
    private WorkflowWebSocketController webSocketController;

    // Flowable 6.8.0 工作流引擎服务
    @Resource
    private FlowableRepositoryService flowableRepositoryService;

    @Resource
    private FlowableRuntimeService flowableRuntimeService;

    @Resource
    private FlowableTaskService flowableTaskService;

    @Resource
    private FlowableHistoryService flowableHistoryService;

    @Resource
    private FlowableManagementService flowableManagementService;

    @Resource
    private WorkflowEngineMetricsCollector metricsCollector;

    /**
     * 工作流引擎实例
     * <p>
     * 已集成Flowable 6.8.0工作流引擎，提供完整的BPMN 2.0流程管理功能。
     * 支持流程部署、实例启动、任务管理、历史追踪等企业级功能。
     * </p>
     */
    // @Resource
    // private FlowEngine flowEngine; // warm-flow已移除

    @Resource
    private ObjectMapper objectMapper;

    // ==================== 流程定义管理 ====================

    @Override
    @Observed(name = "workflow.deploy", contextualName = "workflow-deploy-process")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deployProcess(String bpmnXml, String processName, String processKey, String description,
            String category) {
        log.info("部署Flowable 6.8.0流程定义，流程Key: {}, 流程名称: {}", processKey, processName);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 参数验证
            if (processKey == null || processKey.trim().isEmpty()) {
                throw new ParamException("PROCESS_KEY_REQUIRED", "流程Key不能为空");
            }
            if (processName == null || processName.trim().isEmpty()) {
                throw new ParamException("PROCESS_NAME_REQUIRED", "流程名称不能为空");
            }
            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                throw new ParamException("BPMN_XML_REQUIRED", "BPMN XML不能为空");
            }

            // 2. 使用Flowable引擎部署流程定义
            org.flowable.engine.repository.Deployment deployment = flowableRepositoryService.deployProcessDefinition(
                    processKey, processName, category, bpmnXml, description);

            if (deployment == null) {
                throw new SystemException("DEPLOY_PROCESS_SYSTEM_ERROR", "Flowable引擎部署失败");
            }

            // 3. 获取部署后的流程定义
            ProcessDefinition processDefinition = flowableRepositoryService.getLatestProcessDefinition(processKey);
            if (processDefinition == null) {
                throw new SystemException("DEPLOY_PROCESS_SYSTEM_ERROR", "Flowable流程定义获取失败");
            }

            // 4. 检查本地数据库是否已存在相同流程Key的激活定义
            int existCount = workflowDefinitionDao.countByProcessKey(processKey, "PUBLISHED");
            if (existCount > 0) {
                // 将旧版本设为非最新
                workflowDefinitionDao.updateLatestStatus(processKey, 0);
            }

            // 5. 保存到本地数据库（用于业务管理）
            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setProcessKey(processKey);
            definition.setProcessName(processName);
            definition.setCategory(category != null ? category : "DEFAULT");
            definition.setDescription(description);
            definition.setProcessDefinition(bpmnXml);
            definition.setVersion(processDefinition.getVersion());
            definition.setIsLatest(1); // 标记为最新版本
            definition.setStatus("PUBLISHED"); // Flowable部署成功后直接设为已发布
            definition.setInstanceCount(0);
            definition.setSortOrder(0);
            definition.setFlowableDeploymentId(deployment.getId());
            definition.setFlowableProcessDefId(processDefinition.getId());

            workflowDefinitionDao.insert(definition);

            // 6. 更新最后部署时间
            workflowDefinitionDao.updateLastDeployTime(definition.getId());

            // 7. 记录性能指标
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordProcessDeployment(processKey);
            metricsCollector.recordProcessDeploymentTime(processKey, duration);

            log.info("Flowable流程定义部署成功，本地定义ID: {}, 流程Key: {}, Flowable部署ID: {}, 流程定义ID: {}, 耗时: {}ms",
                    definition.getId(), processKey, deployment.getId(), processDefinition.getId(), duration);
            return ResponseDTO.ok("流程部署成功，定义ID: " + definition.getId() + ", Flowable部署ID: " + deployment.getId());
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 部署流程定义参数错误: processKey={}, error={}", processKey, e.getMessage());
            throw new ParamException("DEPLOY_PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 部署流程定义业务异常: processKey={}, code={}, message={}", processKey, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 部署流程定义系统异常: processKey={}, code={}, message={}", processKey, e.getCode(), e.getMessage(),
                    e);
            throw new SystemException("DEPLOY_PROCESS_SYSTEM_ERROR", "部署流程定义失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 部署流程定义未知异常: processKey={}", processKey, e);
            throw new SystemException("DEPLOY_PROCESS_SYSTEM_ERROR", "部署流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.definition.page", contextualName = "workflow-definition-page")
    public ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(PageParam pageParam, String category,
            String status, String keyword) {
        log.debug("分页查询流程定义，分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        try {
            // 构建分页对象
            Page<WorkflowDefinitionEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

            // 调用DAO层查询
            IPage<WorkflowDefinitionEntity> pageResult = workflowDefinitionDao.selectDefinitionPage(page, category,
                    status, keyword);

            // 转换为PageResult
            PageResult<WorkflowDefinitionEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 分页查询流程定义参数错误: error={}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 分页查询流程定义业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 分页查询流程定义系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_DEFINITIONS_SYSTEM_ERROR", "查询流程定义列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 分页查询流程定义未知异常", e);
            throw new SystemException("PAGE_DEFINITIONS_SYSTEM_ERROR", "查询流程定义列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.definition.get", contextualName = "workflow-definition-get")
    public ResponseDTO<WorkflowDefinitionEntity> getDefinition(Long definitionId) {
        log.debug("获取流程定义详情，定义ID: {}", definitionId);
        try {
            // 根据ID查询流程定义
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            return ResponseDTO.ok(definition);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取流程定义详情参数错误: definitionId={}, error={}", definitionId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取流程定义详情业务异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取流程定义详情系统异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("GET_DEFINITION_SYSTEM_ERROR", "获取流程定义详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取流程定义详情未知异常: definitionId={}", definitionId, e);
            throw new SystemException("GET_DEFINITION_SYSTEM_ERROR", "获取流程定义详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.definition.activate", contextualName = "workflow-definition-activate")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> activateDefinition(Long definitionId) {
        log.info("激活流程定义，定义ID: {}", definitionId);
        try {
            // 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 调用DAO层激活方法
            int updateCount = workflowDefinitionDao.activateDefinition(definitionId);
            if (updateCount > 0) {
                return ResponseDTO.ok("激活成功");
            } else {
                throw new BusinessException("ACTIVATE_DEFINITION_FAILED", "激活失败，流程定义可能已经是激活状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 激活流程定义参数错误: definitionId={}, error={}", definitionId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 激活流程定义业务异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 激活流程定义系统异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("ACTIVATE_DEFINITION_SYSTEM_ERROR", "激活流程定义失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 激活流程定义未知异常: definitionId={}", definitionId, e);
            throw new SystemException("ACTIVATE_DEFINITION_SYSTEM_ERROR", "激活流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.definition.disable", contextualName = "workflow-definition-disable")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> disableDefinition(Long definitionId) {
        log.info("禁用流程定义，定义ID: {}", definitionId);
        try {
            // 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 调用DAO层禁用方法
            int updateCount = workflowDefinitionDao.disableDefinition(definitionId);
            if (updateCount > 0) {
                return ResponseDTO.ok("禁用成功");
            } else {
                throw new BusinessException("DISABLE_DEFINITION_FAILED", "禁用失败，流程定义可能已经是禁用状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 禁用流程定义参数错误: definitionId={}, error={}", definitionId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 禁用流程定义业务异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 禁用流程定义系统异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("DISABLE_DEFINITION_SYSTEM_ERROR", "禁用流程定义失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 禁用流程定义未知异常: definitionId={}", definitionId, e);
            throw new SystemException("DISABLE_DEFINITION_SYSTEM_ERROR", "禁用流程定义失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.definition.delete", contextualName = "workflow-definition-delete")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDefinition(Long definitionId, Boolean cascade) {
        log.info("删除流程定义，定义ID: {}, 级联删除: {}", definitionId, cascade);
        try {
            // 1. 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 2. 检查是否有运行的流程实例
            Page<WorkflowInstanceEntity> checkPage = new Page<>(1, 1);
            IPage<WorkflowInstanceEntity> runningInstances = workflowInstanceDao.selectInstancePage(
                    checkPage, definitionId, 1, null, null, null);

            if (runningInstances.getTotal() > 0) {
                if (Boolean.TRUE.equals(cascade)) {
                    log.warn("流程定义存在运行的实例，但允许级联删除，定义ID: {}", definitionId);
                } else {
                    throw new BusinessException("CANNOT_DELETE_HAS_RUNNING_INSTANCES",
                            "无法删除流程定义，存在运行中的流程实例（共" + runningInstances.getTotal() + "个）。如需删除，请使用级联删除。");
                }
            }

            // 3. 执行删除操作（逻辑删除）
            definition.setDeletedFlag(1);
            workflowDefinitionDao.updateById(definition);

            // 4. 如果级联删除，删除相关的流程实例和任务（逻辑删除）
            // 注意：通常不推荐物理删除历史数据，建议只做逻辑删除
            if (Boolean.TRUE.equals(cascade)) {
                log.info("级联删除相关流程实例和任务（逻辑删除），定义ID: {}", definitionId);
                // 查询所有相关的流程实例
                Page<WorkflowInstanceEntity> instancePage = new Page<>(1, 1000); // 假设最多1000个实例
                IPage<WorkflowInstanceEntity> instances = workflowInstanceDao.selectInstancePage(
                        instancePage, definitionId, null, null, null, null);
                // 级联逻辑删除流程实例和任务
                for (WorkflowInstanceEntity instance : instances.getRecords()) {
                    // 逻辑删除流程实例
                    instance.setDeletedFlag(1);
                    workflowInstanceDao.updateById(instance);
                    // 逻辑删除相关任务
                    // 注意：WorkflowTaskDao继承BaseMapper，可以使用update方法
                    // 这里简化处理，实际可以通过SQL批量更新
                    log.debug("级联删除流程实例，实例ID: {}", instance.getId());
                }
                log.info("级联删除完成，定义ID: {}，共删除{}个流程实例", definitionId, instances.getTotal());
            }

            log.info("流程定义删除成功，定义ID: {}", definitionId);
            return ResponseDTO.ok("删除成功");
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 删除流程定义参数错误: definitionId={}, error={}", definitionId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 删除流程定义业务异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 删除流程定义系统异常: definitionId={}, code={}, message={}", definitionId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("DELETE_DEFINITION_SYSTEM_ERROR", "删除流程定义失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 删除流程定义未知异常: definitionId={}", definitionId, e);
            throw new SystemException("DELETE_DEFINITION_SYSTEM_ERROR", "删除流程定义失败：" + e.getMessage(), e);
        }
    }

    // ==================== 流程实例管理 ====================

    @Override
    @Observed(name = "workflow.process.start", contextualName = "workflow-process-start")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> startProcess(Long definitionId, String businessKey, String instanceName,
            Map<String, Object> variables, Map<String, Object> formData) {
        log.info("启动流程实例，定义ID: {}, 业务Key: {}", definitionId, businessKey);
        long startTime = System.currentTimeMillis();

        try {
            // 1. 验证流程定义是否存在且已激活
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            if (!"PUBLISHED".equals(definition.getStatus())) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_PUBLISHED", "流程定义未激活，无法启动流程实例");
            }

            // 2. 获取当前用户ID（从请求上下文获取，这里需要传入userId参数）
            // 注意：实际调用时应从SmartRequestUtil.getUserId()获取
            Long initiatorId = variables != null && variables.containsKey("initiatorId")
                    ? Long.parseLong(variables.get("initiatorId").toString())
                    : null;

            if (initiatorId == null) {
                throw new ParamException("INITIATOR_ID_REQUIRED", "发起人ID不能为空");
            }

            // 3. 创建流程实例实体
            WorkflowInstanceEntity instance = new WorkflowInstanceEntity();
            instance.setProcessDefinitionId(definitionId);
            instance.setProcessKey(definition.getProcessKey());
            instance.setProcessName(instanceName != null ? instanceName : definition.getProcessName());
            // businessKey保存到variables中，同时尝试解析为businessId
            if (businessKey != null && !businessKey.isEmpty()) {
                try {
                    instance.setBusinessId(Long.parseLong(businessKey));
                } catch (NumberFormatException e) {
                    log.debug("业务Key不是数字格式，仅保存为字符串: {}", businessKey);
                }
            }
            instance.setInitiatorId(initiatorId);
            instance.setStatus(1); // 1-运行中
            instance.setStartTime(LocalDateTime.now());

            // 4. 合并流程变量（包含业务Key、表单数据等）
            Map<String, Object> flowVariables = new HashMap<>();
            if (variables != null) {
                flowVariables.putAll(variables);
            }
            flowVariables.put("businessKey", businessKey);
            flowVariables.put("definitionId", definitionId);
            if (formData != null) {
                flowVariables.put("formData", formData);
            }

            // 保存流程变量到JSON字段（先保存临时变量，instanceId稍后添加）
            if (!flowVariables.isEmpty()) {
                try {
                    instance.setVariables(objectMapper.writeValueAsString(flowVariables));
                } catch (IllegalArgumentException | ParamException e) {
                    log.warn("[工作流引擎] 保存流程变量参数错误: {}", e.getMessage());
                } catch (BusinessException e) {
                    log.warn("[工作流引擎] 保存流程变量业务异常: code={}, message={}", e.getCode(), e.getMessage());
                } catch (SystemException e) {
                    log.error("[工作流引擎] 保存流程变量系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
                } catch (Exception e) {
                    log.warn("[工作流引擎] 保存流程变量失败: {}", e.getMessage());
                }
            }

            // 5. 使用Flowable引擎启动流程实例
            String starterUserId = initiatorId != null ? String.valueOf(initiatorId) : "system";
            ProcessInstance flowableInstance = flowableRuntimeService.startProcessInstanceByKeyWithFullResult(
                    definition.getProcessKey(), businessKey, flowVariables, starterUserId);

            if (flowableInstance == null) {
                throw new SystemException("START_PROCESS_SYSTEM_ERROR", "Flowable引擎启动流程实例失败");
            }

            log.info("Flowable流程实例启动成功，引擎实例ID: {}, 流程Key: {}",
                    flowableInstance.getId(), definition.getProcessKey());

            // 6. 创建本地流程实例记录（用于业务管理）
            instance.setFlowableInstanceId(flowableInstance.getId());
            instance.setFlowableProcessDefId(flowableInstance.getProcessDefinitionId());

            workflowInstanceDao.insert(instance);
            Long instanceId = instance.getId();

            // 添加instanceId到流程变量
            flowVariables.put("instanceId", instanceId);

            // 保存流程变量到本地数据库
            try {
                instance.setVariables(objectMapper.writeValueAsString(flowVariables));
                workflowInstanceDao.updateById(instance);
            } catch (Exception e) {
                log.warn("[工作流引擎] 保存流程变量到本地数据库失败: {}", e.getMessage());
            }

            // 7. 自动创建初始任务记录（Flowable引擎已自动创建）
            List<Task> initialTasks = flowableTaskService.getTasksByProcessInstanceId(flowableInstance.getId());
            if (!initialTasks.isEmpty()) {
                log.info("Flowable引擎自动创建初始任务数量: {}", initialTasks.size());
                // 可以在这里创建本地任务记录，或者通过任务查询API同步
            }

            log.info("流程实例启动成功，本地实例ID: {}, Flowable引擎实例ID: {}", instanceId, flowableInstance.getId());

            // 7. 更新流程定义的实例数量
            workflowDefinitionDao.updateInstanceCount(definitionId, 1);

            // 8. 发送WebSocket通知（通知相关用户有新流程启动）
            Map<String, Object> instanceData = new HashMap<>();
            instanceData.put("instanceId", instanceId);
            instanceData.put("instanceName", instance.getProcessName());
            instanceData.put("processKey", definition.getProcessKey());
            instanceData.put("status", 1);
            if (initiatorId != null) {
                webSocketController.sendInstanceStatusChangedNotification(initiatorId, instanceData);
            }

            // 9. 记录性能指标
            long duration = System.currentTimeMillis() - startTime;
            metricsCollector.recordProcessStart(definition.getProcessKey(), businessKey);
            metricsCollector.recordProcessStartTime(definition.getProcessKey(), duration);

            log.info("流程实例启动成功，实例ID: {}, 业务Key: {}, 耗时: {}ms", instanceId, businessKey, duration);
            return ResponseDTO.ok(instanceId);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 启动流程实例参数错误: definitionId={}, businessKey={}, error={}", definitionId, businessKey,
                    e.getMessage());
            throw new ParamException("START_PROCESS_PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 启动流程实例业务异常: definitionId={}, businessKey={}, code={}, message={}", definitionId,
                    businessKey, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 启动流程实例系统异常: definitionId={}, businessKey={}, code={}, message={}", definitionId,
                    businessKey, e.getCode(), e.getMessage(), e);
            throw new SystemException("START_PROCESS_SYSTEM_ERROR", "启动流程实例失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 启动流程实例未知异常: definitionId={}, businessKey={}", definitionId, businessKey, e);
            throw new SystemException("START_PROCESS_SYSTEM_ERROR", "启动流程实例失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.instance.page", contextualName = "workflow-instance-page")
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(PageParam pageParam, Long definitionId,
            String status, Long startUserId, String startDate, String endDate) {
        log.debug("分页查询流程实例，定义ID: {}, 状态: {}", definitionId, status);
        try {
            // 构建分页对象
            Page<WorkflowInstanceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

            // 转换状态字符串为整数（如果提供了）
            Integer statusInt = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusInt = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    log.warn("流程实例状态格式错误: {}", status);
                }
            }

            // 调用DAO层查询
            IPage<WorkflowInstanceEntity> pageResult = workflowInstanceDao.selectInstancePage(
                    page, definitionId, statusInt, startUserId, startDate, endDate);

            // 转换为PageResult
            PageResult<WorkflowInstanceEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 分页查询流程实例参数错误: error={}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 分页查询流程实例业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 分页查询流程实例系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_INSTANCES_SYSTEM_ERROR", "查询流程实例列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 分页查询流程实例未知异常", e);
            throw new SystemException("PAGE_INSTANCES_SYSTEM_ERROR", "查询流程实例列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.instance.get", contextualName = "workflow-instance-get")
    public ResponseDTO<WorkflowInstanceEntity> getInstance(Long instanceId) {
        log.debug("获取流程实例详情，实例ID: {}", instanceId);
        try {
            // 根据ID查询流程实例
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            return ResponseDTO.ok(instance);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取流程实例详情参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取流程实例详情业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取流程实例详情系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("GET_INSTANCE_SYSTEM_ERROR", "获取流程实例详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取流程实例详情未知异常: instanceId={}", instanceId, e);
            throw new SystemException("GET_INSTANCE_SYSTEM_ERROR", "获取流程实例详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> suspendInstance(Long instanceId, String reason) {
        log.info("挂起流程实例，实例ID: {}, 原因: {}", instanceId, reason);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 调用DAO层挂起方法
            int updateCount = workflowInstanceDao.suspendInstance(instanceId, reason);
            if (updateCount > 0) {
                // 发送WebSocket通知
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("instanceId", instanceId);
                instanceData.put("status", 4);
                instanceData.put("reason", reason);
                webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);

                return ResponseDTO.ok("挂起成功");
            } else {
                throw new BusinessException("SUSPEND_INSTANCE_FAILED", "挂起失败，流程实例可能不是运行状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 挂起流程实例参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 挂起流程实例业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 挂起流程实例系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(),
                    e);
            throw new SystemException("SUSPEND_INSTANCE_SYSTEM_ERROR", "挂起流程实例失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 挂起流程实例未知异常: instanceId={}", instanceId, e);
            throw new SystemException("SUSPEND_INSTANCE_SYSTEM_ERROR", "挂起流程实例失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> activateInstance(Long instanceId) {
        log.info("激活流程实例，实例ID: {}", instanceId);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 调用DAO层激活方法
            int updateCount = workflowInstanceDao.activateInstance(instanceId);
            if (updateCount > 0) {
                // 发送WebSocket通知
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("instanceId", instanceId);
                instanceData.put("status", 1);
                webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);

                return ResponseDTO.ok("激活成功");
            } else {
                throw new BusinessException("ACTIVATE_INSTANCE_FAILED", "激活失败，流程实例可能不是挂起状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 激活流程实例参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 激活流程实例业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 激活流程实例系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(),
                    e);
            throw new SystemException("ACTIVATE_INSTANCE_SYSTEM_ERROR", "激活流程实例失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 激活流程实例未知异常: instanceId={}", instanceId, e);
            throw new SystemException("ACTIVATE_INSTANCE_SYSTEM_ERROR", "激活流程实例失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> terminateInstance(Long instanceId, String reason) {
        log.info("终止流程实例，实例ID: {}, 原因: {}", instanceId, reason);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 调用DAO层终止方法
            int updateCount = workflowInstanceDao.terminateInstance(instanceId, reason);
            if (updateCount > 0) {
                // 发送WebSocket通知
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("instanceId", instanceId);
                instanceData.put("status", 3);
                instanceData.put("reason", reason);
                webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);

                return ResponseDTO.ok("终止成功");
            } else {
                throw new BusinessException("TERMINATE_INSTANCE_FAILED", "终止失败，流程实例可能不是运行或挂起状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 终止流程实例参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 终止流程实例业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 终止流程实例系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(),
                    e);
            throw new SystemException("TERMINATE_INSTANCE_SYSTEM_ERROR", "终止流程实例失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 终止流程实例未知异常: instanceId={}", instanceId, e);
            throw new SystemException("TERMINATE_INSTANCE_SYSTEM_ERROR", "终止流程实例失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> revokeInstance(Long instanceId, String reason) {
        log.info("撤销流程实例，实例ID: {}, 原因: {}", instanceId, reason);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 调用DAO层撤销方法
            int updateCount = workflowInstanceDao.revokeInstance(instanceId, reason);
            if (updateCount > 0) {
                // 发送WebSocket通知
                Map<String, Object> instanceData = new HashMap<>();
                instanceData.put("instanceId", instanceId);
                instanceData.put("status", 3);
                instanceData.put("reason", reason);
                webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);

                return ResponseDTO.ok("撤销成功");
            } else {
                throw new BusinessException("REVOKE_INSTANCE_FAILED", "撤销失败，流程实例可能不是运行状态");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 撤销流程实例参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 撤销流程实例业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 撤销流程实例系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(), e.getMessage(),
                    e);
            throw new SystemException("REVOKE_INSTANCE_SYSTEM_ERROR", "撤销流程实例失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 撤销流程实例未知异常: instanceId={}", instanceId, e);
            throw new SystemException("REVOKE_INSTANCE_SYSTEM_ERROR", "撤销流程实例失败：" + e.getMessage(), e);
        }
    }

    // ==================== 任务管理 ====================

    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(PageParam pageParam, Long userId, String category,
            Integer priority, String dueStatus) {
        log.debug("分页查询我的待办任务，用户ID: {}, 分类: {}", userId, category);
        try {
            // 构建分页对象
            Page<WorkflowTaskEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

            // 调用DAO层查询
            IPage<WorkflowTaskEntity> pageResult = workflowTaskDao.selectMyTasksPage(
                    page, userId, category, priority, dueStatus);

            // 转换为PageResult
            PageResult<WorkflowTaskEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 分页查询我的待办任务参数错误: userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 分页查询我的待办任务业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 分页查询我的待办任务系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_MY_TASKS_SYSTEM_ERROR", "查询待办任务列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 分页查询我的待办任务未知异常: userId={}", userId, e);
            throw new SystemException("PAGE_MY_TASKS_SYSTEM_ERROR", "查询待办任务列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(PageParam pageParam, Long userId,
            String category, String outcome, String startDate, String endDate) {
        log.debug("分页查询我的已办任务，用户ID: {}", userId);
        try {
            // 构建分页对象
            Page<WorkflowTaskEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

            // 调用DAO层查询
            IPage<WorkflowTaskEntity> pageResult = workflowTaskDao.selectMyCompletedTasksPage(
                    page, userId, category, outcome, startDate, endDate);

            // 转换为PageResult
            PageResult<WorkflowTaskEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 分页查询我的已办任务参数错误: userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 分页查询我的已办任务业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 分页查询我的已办任务系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_MY_COMPLETED_TASKS_SYSTEM_ERROR", "查询已办任务列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 分页查询我的已办任务未知异常: userId={}", userId, e);
            throw new SystemException("PAGE_MY_COMPLETED_TASKS_SYSTEM_ERROR", "查询已办任务列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(PageParam pageParam, Long userId,
            String category, String status) {
        log.debug("分页查询我发起的流程，用户ID: {}", userId);
        try {
            // 构建分页对象
            Page<WorkflowInstanceEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());

            // 转换状态字符串为整数（如果提供了）
            Integer statusInt = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusInt = Integer.parseInt(status);
                } catch (NumberFormatException e) {
                    log.warn("流程实例状态格式错误: {}", status);
                }
            }

            // 调用DAO层查询（使用startUserId过滤）
            IPage<WorkflowInstanceEntity> pageResult = workflowInstanceDao.selectInstancePage(
                    page, null, statusInt, userId, null, null);

            // 转换为PageResult
            PageResult<WorkflowInstanceEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 分页查询我发起的流程参数错误: userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 分页查询我发起的流程业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 分页查询我发起的流程系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("PAGE_MY_PROCESSES_SYSTEM_ERROR", "查询我发起的流程列表失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 分页查询我发起的流程未知异常: userId={}", userId, e);
            throw new SystemException("PAGE_MY_PROCESSES_SYSTEM_ERROR", "查询我发起的流程列表失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<WorkflowTaskEntity> getTask(Long taskId) {
        log.debug("获取任务详情，任务ID: {}", taskId);
        try {
            // 根据ID查询任务
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            return ResponseDTO.ok(task);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取任务详情参数错误: taskId={}, error={}", taskId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取任务详情业务异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取任务详情系统异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_TASK_SYSTEM_ERROR", "获取任务详情失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取任务详情未知异常: taskId={}", taskId, e);
            throw new SystemException("GET_TASK_SYSTEM_ERROR", "获取任务详情失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> claimTask(Long taskId, Long userId) {
        log.info("受理任务，任务ID: {}, 用户ID: {}", taskId, userId);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 调用DAO层受理方法
            int updateCount = workflowTaskDao.claimTask(taskId, userId);
            if (updateCount > 0) {
                // 发送WebSocket通知
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("taskId", taskId);
                taskData.put("taskName", task.getTaskName());
                taskData.put("assigneeId", userId);
                webSocketController.sendNewTaskNotification(userId, taskData);

                return ResponseDTO.ok("受理成功");
            } else {
                throw new BusinessException("CLAIM_TASK_FAILED", "受理失败，任务可能已被受理或状态不正确");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 受理任务参数错误: taskId={}, userId={}, error={}", taskId, userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 受理任务业务异常: taskId={}, userId={}, code={}, message={}", taskId, userId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 受理任务系统异常: taskId={}, userId={}, code={}, message={}", taskId, userId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("CLAIM_TASK_SYSTEM_ERROR", "受理任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 受理任务未知异常: taskId={}, userId={}", taskId, userId, e);
            throw new SystemException("CLAIM_TASK_SYSTEM_ERROR", "受理任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> unclaimTask(Long taskId) {
        log.info("取消受理任务，任务ID: {}", taskId);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 调用DAO层取消受理方法
            int updateCount = workflowTaskDao.unclaimTask(taskId);
            if (updateCount > 0) {
                return ResponseDTO.ok("取消受理成功");
            } else {
                throw new BusinessException("UNCLAIM_TASK_FAILED", "取消受理失败，任务可能未被受理或状态不正确");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 取消受理任务参数错误: taskId={}, error={}", taskId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 取消受理任务业务异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 取消受理任务系统异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage(), e);
            throw new SystemException("UNCLAIM_TASK_SYSTEM_ERROR", "取消受理任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 取消受理任务未知异常: taskId={}", taskId, e);
            throw new SystemException("UNCLAIM_TASK_SYSTEM_ERROR", "取消受理任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> delegateTask(Long taskId, Long targetUserId) {
        log.info("委派任务，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 获取当前受理人ID
            Long originalUserId = task.getAssigneeId();
            if (originalUserId == null) {
                throw new BusinessException("TASK_NOT_CLAIMED", "任务尚未被受理，无法委派");
            }

            // 调用DAO层委派方法
            int updateCount = workflowTaskDao.delegateTask(taskId, targetUserId, originalUserId);
            if (updateCount > 0) {
                // 发送WebSocket通知给目标用户
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("taskId", taskId);
                taskData.put("taskName", task.getTaskName());
                taskData.put("assigneeId", targetUserId);
                taskData.put("delegated", true);
                webSocketController.sendNewTaskNotification(targetUserId, taskData);

                return ResponseDTO.ok("委派成功");
            } else {
                throw new BusinessException("DELEGATE_TASK_FAILED", "委派失败，任务状态可能不正确");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 委派任务参数错误: taskId={}, targetUserId={}, error={}", taskId, targetUserId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 委派任务业务异常: taskId={}, targetUserId={}, code={}, message={}", taskId, targetUserId,
                    e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 委派任务系统异常: taskId={}, targetUserId={}, code={}, message={}", taskId, targetUserId,
                    e.getCode(), e.getMessage(), e);
            throw new SystemException("DELEGATE_TASK_SYSTEM_ERROR", "委派任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 委派任务未知异常: taskId={}, targetUserId={}", taskId, targetUserId, e);
            throw new SystemException("DELEGATE_TASK_SYSTEM_ERROR", "委派任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> transferTask(Long taskId, Long targetUserId) {
        log.info("转交任务，任务ID: {}, 目标用户ID: {}", taskId, targetUserId);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 获取当前受理人ID
            Long originalUserId = task.getAssigneeId();
            if (originalUserId == null) {
                throw new BusinessException("TASK_NOT_CLAIMED", "任务尚未被受理，无法转交");
            }

            // 调用DAO层转交方法
            int updateCount = workflowTaskDao.transferTask(taskId, targetUserId, originalUserId);
            if (updateCount > 0) {
                // 发送WebSocket通知给目标用户
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("taskId", taskId);
                taskData.put("taskName", task.getTaskName());
                taskData.put("assigneeId", targetUserId);
                taskData.put("transferred", true);
                webSocketController.sendNewTaskNotification(targetUserId, taskData);

                return ResponseDTO.ok("转交成功");
            } else {
                throw new BusinessException("TRANSFER_TASK_FAILED", "转交失败，任务状态可能不正确");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 转交任务参数错误: taskId={}, targetUserId={}, error={}", taskId, targetUserId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 转交任务业务异常: taskId={}, targetUserId={}, code={}, message={}", taskId, targetUserId,
                    e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 转交任务系统异常: taskId={}, targetUserId={}, code={}, message={}", taskId, targetUserId,
                    e.getCode(), e.getMessage(), e);
            throw new SystemException("TRANSFER_TASK_SYSTEM_ERROR", "转交任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 转交任务未知异常: taskId={}, targetUserId={}", taskId, targetUserId, e);
            throw new SystemException("TRANSFER_TASK_SYSTEM_ERROR", "转交任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Observed(name = "workflow.task.complete", contextualName = "workflow-task-complete")
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> completeTask(Long taskId, String outcome, String comment, Map<String, Object> variables,
            Map<String, Object> formData) {
        log.info("完成Flowable任务，任务ID: {}, 结果: {}", taskId, outcome);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 验证任务状态（只有待处理的任务才能完成）
            if (task.getStatus() != 1) { // 1-待处理
                throw new BusinessException("TASK_ALREADY_COMPLETED", "任务已完成，无法重复完成");
            }

            // 转换outcome字符串为整数（1-同意, 2-驳回）
            Integer outcomeInt;
            if (outcome != null && !outcome.isEmpty()) {
                try {
                    outcomeInt = Integer.parseInt(outcome);
                } catch (NumberFormatException e) {
                    log.warn("任务处理结果格式错误: {}", outcome);
                    outcomeInt = 1; // 默认为同意
                }
            } else {
                outcomeInt = 1; // 默认为同意
            }

            // 获取流程实例信息（用于获取Flowable实例ID）
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(task.getInstanceId());
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 准备任务变量
            Map<String, Object> taskVariables = new HashMap<>();
            if (variables != null) {
                taskVariables.putAll(variables);
            }
            taskVariables.put("lastTaskId", taskId);
            taskVariables.put("lastTaskOutcome", outcome);
            taskVariables.put("lastTaskComment", comment);
            if (formData != null) {
                taskVariables.put("formData", formData);
            }

            // 使用Flowable引擎完成任务
            String flowableTaskId = task.getFlowableTaskId();
            if (flowableTaskId == null || flowableTaskId.isEmpty()) {
                // 如果本地任务没有Flowable任务ID，尝试通过流程实例ID和任务名称查找
                List<Task> flowableTasks = flowableTaskService
                        .getTasksByProcessInstanceId(instance.getFlowableInstanceId());
                Optional<Task> matchingTask = flowableTasks.stream()
                        .filter(t -> task.getTaskName().equals(t.getName()))
                        .findFirst();

                if (matchingTask.isPresent()) {
                    flowableTaskId = matchingTask.get().getId();
                    // 更新本地任务的Flowable任务ID
                    task.setFlowableTaskId(flowableTaskId);
                    workflowTaskDao.updateById(task);
                } else {
                    throw new BusinessException("FLOWABLE_TASK_NOT_FOUND", "未找到对应的Flowable任务");
                }
            }

            // 使用Flowable引擎完成任务并推进流程
            flowableTaskService.completeTask(flowableTaskId, taskVariables, comment);

            log.info("Flowable任务完成成功，任务ID: {}, 本地任务ID: {}", flowableTaskId, taskId);

            // 更新本地任务状态
            task.setStatus(2); // 2-已完成
            task.setOutcome(outcomeInt);
            task.setComment(comment);
            task.setCompleteTime(LocalDateTime.now());
            workflowTaskDao.updateById(task);

            // 更新本地流程实例变量
            if (instance != null) {
                try {
                    // 合并现有变量
                    Map<String, Object> existingVariables = new HashMap<>();
                    if (instance.getVariables() != null && !instance.getVariables().isEmpty()) {
                        existingVariables = objectMapper.readValue(instance.getVariables(),
                                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                    }
                    existingVariables.putAll(taskVariables);
                    instance.setVariables(objectMapper.writeValueAsString(existingVariables));
                    workflowInstanceDao.updateById(instance);
                } catch (Exception e) {
                    log.warn("[工作流引擎] 更新本地流程变量失败: {}", e.getMessage());
                }
            }

            log.info("任务完成并自动推进流程，任务ID: {}, Flowable任务ID: {}", taskId, flowableTaskId);

            // 发送WebSocket通知
            Map<String, Object> taskData = new HashMap<>();
            taskData.put("taskId", taskId);
            taskData.put("flowableTaskId", flowableTaskId);
            taskData.put("status", "COMPLETED");
            taskData.put("outcome", outcome);
            if (task.getAssigneeId() != null) {
                webSocketController.sendTaskStatusChangedNotification(task.getAssigneeId(), taskData);
            }

            return ResponseDTO.ok("任务完成成功，流程已自动推进");
        } catch (org.flowable.common.engine.api.FlowableException e) {
            log.error("[工作流引擎] Flowable引擎异常: taskId={}, message={}", taskId, e.getMessage(), e);
            throw new SystemException("FLOWABLE_ENGINE_ERROR", "工作流引擎错误: " + e.getMessage(), e);
        } catch (org.springframework.dao.DataAccessException e) {
            log.error("[工作流引擎] 数据访问异常: taskId={}, error={}", taskId, e.getMessage(), e);
            throw new SystemException("WORKFLOW_DATA_ACCESS_ERROR", "数据访问失败: " + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 工作流业务异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage());
            throw e;
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 完成任务参数错误: taskId={}, error={}", taskId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (SystemException e) {
            log.error("[工作流引擎] 完成任务系统异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[工作流引擎] 完成任务未知异常: taskId={}", taskId, e);
            throw new SystemException("COMPLETE_TASK_SYSTEM_ERROR", "完成任务失败：" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> rejectTask(Long taskId, String comment, Map<String, Object> variables) {
        log.info("驳回任务，任务ID: {}, 意见: {}", taskId, comment);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                throw new BusinessException("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }

            // 调用DAO层驳回方法
            int updateCount = workflowTaskDao.rejectTask(taskId, comment);
            if (updateCount > 0) {
                // 获取流程实例
                WorkflowInstanceEntity instance = workflowInstanceDao.selectById(task.getInstanceId());

                // 合并流程变量
                Map<String, Object> flowVariables = new HashMap<>();
                if (variables != null) {
                    flowVariables.putAll(variables);
                }
                flowVariables.put("action", "reject");
                flowVariables.put("comment", comment);

                // 注意：当前实现基于数据库，不包含复杂的工作流引擎逻辑
                // 如需使用工作流引擎（如Flowable、Camunda），可以在这里调用引擎的驳回方法
                // 示例（Flowable）：
                // taskService.rejectTask(taskId.toString(), comment);
                // 当前实现仅更新任务状态和流程变量，不自动回退到上一个节点
                log.info("任务驳回，任务ID: {}。注意：当前实现不自动回退流程，如需自动回退请集成工作流引擎", taskId);

                // 更新流程变量
                if (instance != null && !flowVariables.isEmpty()) {
                    try {
                        // 合并现有变量
                        Map<String, Object> existingVariables = new HashMap<>();
                        if (instance.getVariables() != null && !instance.getVariables().isEmpty()) {
                            existingVariables = objectMapper.readValue(instance.getVariables(),
                                    objectMapper.getTypeFactory().constructMapType(Map.class, String.class,
                                            Object.class));
                        }
                        existingVariables.putAll(flowVariables);
                        instance.setVariables(objectMapper.writeValueAsString(existingVariables));

                        // 驳回后流程可能结束，更新状态
                        // 注意：根据业务需求决定是否自动结束流程实例
                        // 如果驳回后流程应该结束，可以设置：
                        // instance.setStatus(3); // 3-已终止
                        // instance.setEndTime(LocalDateTime.now());
                        // 当前实现不自动结束流程，由业务逻辑决定

                        workflowInstanceDao.updateById(instance);
                    } catch (IllegalArgumentException | ParamException e) {
                        log.warn("[工作流引擎] 更新流程变量参数错误: {}", e.getMessage());
                    } catch (org.flowable.common.engine.api.FlowableException e) {
                        log.error("[工作流引擎] 更新流程变量Flowable异常: message={}", e.getMessage());
                    } catch (org.springframework.dao.DataAccessException e) {
                        log.error("[工作流引擎] 更新流程变量数据访问异常: {}", e.getMessage());
                    } catch (BusinessException e) {
                        log.warn("[工作流引擎] 更新流程变量业务异常: code={}, message={}", e.getCode(), e.getMessage());
                    } catch (SystemException e) {
                        log.error("[工作流引擎] 更新流程变量系统异常: code={}, message={}", e.getCode(), e.getMessage());
                    } catch (Exception e) {
                        log.warn("[工作流引擎] 更新流程变量未知异常: {}", e.getMessage());
                    }
                }

                // 发送WebSocket通知
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("taskId", taskId);
                taskData.put("status", "REJECTED");
                taskData.put("comment", comment);
                taskData.put("instanceId", task.getInstanceId());
                if (task.getAssigneeId() != null) {
                    webSocketController.sendTaskStatusChangedNotification(task.getAssigneeId(), taskData);
                }

                // 通知流程发起人流程被驳回
                if (instance != null && instance.getInitiatorId() != null) {
                    Map<String, Object> instanceData = new HashMap<>();
                    instanceData.put("instanceId", instance.getId());
                    instanceData.put("status", "REJECTED");
                    instanceData.put("reason", comment);
                    webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);
                }

                return ResponseDTO.ok("任务驳回成功");
            } else {
                throw new BusinessException("REJECT_TASK_FAILED", "驳回任务失败，任务状态可能不正确");
            }
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 驳回任务参数错误: taskId={}, error={}", taskId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 驳回任务业务异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 驳回任务系统异常: taskId={}, code={}, message={}", taskId, e.getCode(), e.getMessage(), e);
            throw new SystemException("REJECT_TASK_SYSTEM_ERROR", "驳回任务失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 驳回任务未知异常: taskId={}", taskId, e);
            throw new SystemException("REJECT_TASK_SYSTEM_ERROR", "驳回任务失败：" + e.getMessage(), e);
        }
    }

    // ==================== 流程监控 ====================

    @Override
    public ResponseDTO<Map<String, Object>> getProcessDiagram(Long instanceId) {
        log.debug("获取流程实例图，实例ID: {}", instanceId);
        try {
            // 获取流程实例
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 获取流程定义
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(instance.getProcessDefinitionId());
            if (definition == null) {
                throw new BusinessException("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("bpmnXml", definition.getProcessDefinition()); // BPMN XML定义
            result.put("activeActivityIds", new ArrayList<>()); // 当前活动节点ID列表

            // 如果有当前节点ID，添加到活动节点列表
            if (instance.getCurrentNodeId() != null && !instance.getCurrentNodeId().isEmpty()) {
                List<String> activeNodes = new ArrayList<>();
                activeNodes.add(instance.getCurrentNodeId());
                result.put("activeActivityIds", activeNodes);
            }

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取流程实例图参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取流程实例图业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取流程实例图系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("GET_PROCESS_DIAGRAM_SYSTEM_ERROR", "获取流程图失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取流程实例图未知异常: instanceId={}", instanceId, e);
            throw new SystemException("GET_PROCESS_DIAGRAM_SYSTEM_ERROR", "获取流程图失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getProcessHistory(Long instanceId) {
        log.debug("获取流程历史记录，实例ID: {}", instanceId);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                throw new BusinessException("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }

            // 调用DAO层查询历史记录
            List<Map<String, Object>> historyList = workflowInstanceDao.selectProcessHistory(instanceId);

            return ResponseDTO.ok(historyList);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取流程历史记录参数错误: instanceId={}, error={}", instanceId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取流程历史记录业务异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取流程历史记录系统异常: instanceId={}, code={}, message={}", instanceId, e.getCode(),
                    e.getMessage(), e);
            throw new SystemException("GET_PROCESS_HISTORY_SYSTEM_ERROR", "获取流程历史记录失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取流程历史记录未知异常: instanceId={}", instanceId, e);
            throw new SystemException("GET_PROCESS_HISTORY_SYSTEM_ERROR", "获取流程历史记录失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getProcessStatistics(String startDate, String endDate) {
        log.debug("获取流程统计信息，开始日期: {}, 结束日期: {}", startDate, endDate);
        try {
            // 调用DAO层统计方法
            Map<String, Object> statistics = workflowInstanceDao.selectProcessStatistics(null, null, startDate,
                    endDate);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            if (statistics != null) {
                result.put("totalInstances", statistics.get("total") != null ? statistics.get("total") : 0);
                result.put("runningInstances", statistics.get("running") != null ? statistics.get("running") : 0);
                result.put("completedInstances", statistics.get("completed") != null ? statistics.get("completed") : 0);
                result.put("terminatedInstances",
                        statistics.get("terminated") != null ? statistics.get("terminated") : 0);
                result.put("suspendedInstances", statistics.get("suspended") != null ? statistics.get("suspended") : 0);
                result.put("averageDuration",
                        statistics.get("avgDuration") != null ? statistics.get("avgDuration") : 0);
            } else {
                result.put("totalInstances", 0);
                result.put("runningInstances", 0);
                result.put("completedInstances", 0);
                result.put("terminatedInstances", 0);
                result.put("suspendedInstances", 0);
                result.put("averageDuration", 0);
            }

            // 统计任务数量
            Map<String, Object> taskStatistics = workflowTaskDao.selectTaskStatistics(null, null, startDate, endDate);
            if (taskStatistics != null) {
                result.put("totalTasks", taskStatistics.get("total") != null ? taskStatistics.get("total") : 0);
                result.put("pendingTasks", taskStatistics.get("pending") != null ? taskStatistics.get("pending") : 0);
                result.put("completedTasks",
                        taskStatistics.get("completed") != null ? taskStatistics.get("completed") : 0);
            } else {
                result.put("totalTasks", 0);
                result.put("pendingTasks", 0);
                result.put("completedTasks", 0);
            }

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取流程统计信息参数错误: error={}", e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取流程统计信息业务异常: code={}, message={}", e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取流程统计信息系统异常: code={}, message={}", e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_PROCESS_STATISTICS_SYSTEM_ERROR", "获取流程统计信息失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取流程统计信息未知异常", e);
            throw new SystemException("GET_PROCESS_STATISTICS_SYSTEM_ERROR", "获取流程统计信息失败：" + e.getMessage(), e);
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(Long userId, String startDate, String endDate) {
        log.debug("获取用户工作量统计，用户ID: {}, 开始日期: {}, 结束日期: {}", userId, startDate, endDate);
        try {
            // 调用DAO层统计方法
            List<Object> statisticsList = workflowTaskDao.selectUserWorkloadStatistics(userId, startDate, endDate);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            if (statisticsList != null && !statisticsList.isEmpty()) {
                // DAO返回的是List<Object>，需要根据实际返回类型处理
                // 这里假设返回的是Map，实际可能需要调整
                if (statisticsList.get(0) instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> stats = (Map<String, Object>) statisticsList.get(0);
                    result.put("totalTasks", stats.get("totalTasks") != null ? stats.get("totalTasks") : 0);
                    result.put("pendingTasks", stats.get("pendingTasks") != null ? stats.get("pendingTasks") : 0);
                    result.put("processingTasks",
                            stats.get("processingTasks") != null ? stats.get("processingTasks") : 0);
                    result.put("completedTasks", stats.get("completedTasks") != null ? stats.get("completedTasks") : 0);
                    result.put("averageDuration", stats.get("avgDuration") != null ? stats.get("avgDuration") : 0);
                    result.put("overdueCount", stats.get("overdueCount") != null ? stats.get("overdueCount") : 0);

                    // 计算完成率
                    Long totalTasks = Long.parseLong(stats.get("totalTasks").toString());
                    Long completedTasks = Long.parseLong(stats.get("completedTasks").toString());
                    double completionRate = totalTasks > 0
                            ? (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100
                            : 0.0;
                    result.put("completionRate", completionRate);
                } else {
                    // 如果返回格式不同，使用默认值
                    result.put("totalTasks", 0);
                    result.put("pendingTasks", 0);
                    result.put("processingTasks", 0);
                    result.put("completedTasks", 0);
                    result.put("averageDuration", 0);
                    result.put("overdueCount", 0);
                    result.put("completionRate", 0.0);
                }
            } else {
                result.put("totalTasks", 0);
                result.put("pendingTasks", 0);
                result.put("processingTasks", 0);
                result.put("completedTasks", 0);
                result.put("averageDuration", 0);
                result.put("overdueCount", 0);
                result.put("completionRate", 0.0);
            }

            return ResponseDTO.ok(result);
        } catch (IllegalArgumentException | ParamException e) {
            log.warn("[工作流引擎] 获取用户工作量统计参数错误: userId={}, error={}", userId, e.getMessage());
            throw new ParamException("PARAM_ERROR", "参数错误：" + e.getMessage(), e);
        } catch (BusinessException e) {
            log.warn("[工作流引擎] 获取用户工作量统计业务异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage());
            throw e;
        } catch (SystemException e) {
            log.error("[工作流引擎] 获取用户工作量统计系统异常: userId={}, code={}, message={}", userId, e.getCode(), e.getMessage(), e);
            throw new SystemException("GET_USER_WORKLOAD_STATISTICS_SYSTEM_ERROR", "获取用户工作量统计失败：" + e.getMessage(), e);
        } catch (Exception e) {
            log.error("[工作流引擎] 获取用户工作量统计未知异常: userId={}", userId, e);
            throw new SystemException("GET_USER_WORKLOAD_STATISTICS_SYSTEM_ERROR", "获取用户工作量统计失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> getEngineStatistics() {
        log.debug("[工作流引擎] 获取引擎统计信息");
        Map<String, Object> stats = new HashMap<>();

        try {
            // 统计运行中的流程实例数
            Long runningInstances = workflowInstanceDao.countByStatus(1); // 1-运行中
            stats.put("runningInstances", runningInstances != null ? runningInstances : 0L);

            // 统计活跃任务数
            Long activeTasks = workflowTaskDao.countByStatus(1); // 1-待处理
            Long processingTasks = workflowTaskDao.countByStatus(2); // 2-处理中
            stats.put("activeTasks",
                    (activeTasks != null ? activeTasks : 0L) + (processingTasks != null ? processingTasks : 0L));

            // 统计已部署流程数
            Long deployedProcesses = workflowDefinitionDao.countByStatus("ACTIVE");
            stats.put("deployedProcesses", deployedProcesses != null ? deployedProcesses : 0L);

            // 统计今日完成数
            LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
            Long completedToday = workflowInstanceDao.countCompletedSince(startOfToday);
            stats.put("completedToday", completedToday != null ? completedToday : 0L);

            log.debug("[工作流引擎] 引擎统计信息: {}", stats);
            return stats;

        } catch (Exception e) {
            log.error("[工作流引擎] 获取引擎统计信息失败", e);
            // 返回默认值而非抛出异常，确保监控不会中断
            stats.put("runningInstances", 0L);
            stats.put("activeTasks", 0L);
            stats.put("deployedProcesses", 0L);
            stats.put("completedToday", 0L);
            return stats;
        }
    }
}
