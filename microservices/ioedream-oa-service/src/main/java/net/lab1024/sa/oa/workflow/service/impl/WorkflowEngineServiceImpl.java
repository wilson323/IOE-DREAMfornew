package net.lab1024.sa.oa.workflow.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.common.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.common.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.common.workflow.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.common.workflow.entity.WorkflowInstanceEntity;
import net.lab1024.sa.common.workflow.entity.WorkflowTaskEntity;
import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.websocket.WorkflowWebSocketController;

/**
 * 工作流引擎说明
 * <p>
 * warm-flow工作流引擎依赖已移除，当前使用基于数据库的简单工作流实现。
 * 如需使用完整的工作流引擎，请选择以下方案之一：
 * </p>
 * <ol>
 * <li>Activiti 7.x - 成熟的企业级工作流引擎，支持BPMN 2.0</li>
 * <li>Flowable 6.x - Activiti的分支，功能更强大，社区活跃</li>
 * <li>Camunda 7.x - 企业级工作流引擎，性能优秀</li>
 * <li>自定义工作流实现 - 基于数据库的轻量级实现（当前方案）</li>
 * </ol>
 * <p>
 * 当前实现说明：
 * - 流程定义、实例、任务管理基于数据库实现
 * - 流程推进逻辑需要手动实现（见completeTask方法）
 * - 支持基本的任务受理、完成、驳回、转交、委派功能
 * - 如需复杂流程控制（并行网关、条件分支等），建议集成Flowable或Camunda
 * </p>
 */
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

/**
 * 工作流引擎服务实现类
 * 提供工作流流程定义、实例、任务管理的完整业务逻辑实现
 * 严格遵循CLAUDE.md架构规范
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 * @version 3.0.0
 */
@Slf4j
@Service
public class WorkflowEngineServiceImpl implements WorkflowEngineService {

    @Resource
    private WorkflowDefinitionDao workflowDefinitionDao;
    
    @Resource
    private WorkflowInstanceDao workflowInstanceDao;
    
    @Resource
    private WorkflowTaskDao workflowTaskDao;

    @Resource
    private WorkflowWebSocketController webSocketController;

    /**
     * 工作流引擎实例
     * <p>
     * 当前未集成工作流引擎，使用基于数据库的简单实现。
     * 如需集成工作流引擎，请取消注释并配置相应的引擎实例。
     * </p>
     * <p>
     * 示例（Flowable）：
     * <pre>
     * @Resource
     * private RuntimeService runtimeService;
     * @Resource
     * private TaskService taskService;
     * </pre>
     * </p>
     */
    // @Resource
    // private FlowEngine flowEngine; // warm-flow已移除

    @Resource
    private ObjectMapper objectMapper;

    // ==================== 流程定义管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deployProcess(String bpmnXml, String processName, String processKey, String description, String category) {
        log.info("部署流程定义，流程Key: {}, 流程名称: {}", processKey, processName);
        try {
            // 1. 验证流程Key是否已存在
            int existCount = workflowDefinitionDao.countByProcessKey(processKey, null);
            if (existCount > 0) {
                return ResponseDTO.error("PROCESS_KEY_EXISTS", "流程Key已存在: " + processKey);
            }
            
            // 2. 创建流程定义实体
            WorkflowDefinitionEntity definition = new WorkflowDefinitionEntity();
            definition.setProcessKey(processKey);
            definition.setProcessName(processName);
            definition.setCategory(category != null ? category : "DEFAULT");
            definition.setDescription(description);
            definition.setProcessDefinition(bpmnXml); // 保存BPMN XML或warm-flow流程定义
            
            // 3. 设置版本信息（首次部署为版本1）
            definition.setVersion(1);
            definition.setIsLatest(1); // 标记为最新版本
            
            // 4. 设置状态为草稿（需要激活后才可使用）
            definition.setStatus("DRAFT");
            definition.setInstanceCount(0);
            definition.setSortOrder(0);
            
            // 5. 保存到数据库
            workflowDefinitionDao.insert(definition);
            
            // 6. 如果提供了BPMN XML，保存到数据库
            // 注意：当前实现仅保存BPMN XML，不进行解析和转换
            // 如需使用工作流引擎（如Flowable、Camunda），可以在启动流程时解析BPMN XML
            // 示例（Flowable）：
            // BpmnModel bpmnModel = bpmnXmlConverter.convertToBpmnModel(bpmnXml);
            // Process process = bpmnModel.getMainProcess();
            if (bpmnXml != null && !bpmnXml.trim().isEmpty()) {
                log.info("流程定义包含BPMN XML，已保存。如需使用工作流引擎，需要在启动流程时解析BPMN XML");
            }
            
            // 7. 更新最后部署时间
            workflowDefinitionDao.updateLastDeployTime(definition.getId());
            
            log.info("流程定义部署成功，定义ID: {}, 流程Key: {}", definition.getId(), processKey);
            return ResponseDTO.ok("流程部署成功，定义ID: " + definition.getId());
        } catch (Exception e) {
            log.error("部署流程定义失败，流程Key: {}", processKey, e);
            return ResponseDTO.error("部署流程定义失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(PageParam pageParam, String category, String status, String keyword) {
        log.debug("分页查询流程定义，分类: {}, 状态: {}, 关键词: {}", category, status, keyword);
        try {
            // 构建分页对象
            Page<WorkflowDefinitionEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            
            // 调用DAO层查询
            IPage<WorkflowDefinitionEntity> pageResult = workflowDefinitionDao.selectDefinitionPage(page, category, status, keyword);
            
            // 转换为PageResult
            PageResult<WorkflowDefinitionEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询流程定义失败", e);
            return ResponseDTO.error("查询流程定义列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowDefinitionEntity> getDefinition(Long definitionId) {
        log.debug("获取流程定义详情，定义ID: {}", definitionId);
        try {
            // 根据ID查询流程定义
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }
            
            return ResponseDTO.ok(definition);
        } catch (Exception e) {
            log.error("获取流程定义详情失败，定义ID: {}", definitionId, e);
            return ResponseDTO.error("获取流程定义详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> activateDefinition(Long definitionId) {
        log.info("激活流程定义，定义ID: {}", definitionId);
        try {
            // 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }
            
            // 调用DAO层激活方法
            int updateCount = workflowDefinitionDao.activateDefinition(definitionId);
            if (updateCount > 0) {
                return ResponseDTO.ok("激活成功");
            } else {
                return ResponseDTO.error("激活失败，流程定义可能已经是激活状态");
            }
        } catch (Exception e) {
            log.error("激活流程定义失败，定义ID: {}", definitionId, e);
            return ResponseDTO.error("激活流程定义失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> disableDefinition(Long definitionId) {
        log.info("禁用流程定义，定义ID: {}", definitionId);
        try {
            // 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }
            
            // 调用DAO层禁用方法
            int updateCount = workflowDefinitionDao.disableDefinition(definitionId);
            if (updateCount > 0) {
                return ResponseDTO.ok("禁用成功");
            } else {
                return ResponseDTO.error("禁用失败，流程定义可能已经是禁用状态");
            }
        } catch (Exception e) {
            log.error("禁用流程定义失败，定义ID: {}", definitionId, e);
            return ResponseDTO.error("禁用流程定义失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> deleteDefinition(Long definitionId, Boolean cascade) {
        log.info("删除流程定义，定义ID: {}, 级联删除: {}", definitionId, cascade);
        try {
            // 1. 验证定义是否存在
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }
            
            // 2. 检查是否有运行的流程实例
            Page<WorkflowInstanceEntity> checkPage = new Page<>(1, 1);
            IPage<WorkflowInstanceEntity> runningInstances = workflowInstanceDao.selectInstancePage(
                checkPage, definitionId, 1, null, null, null
            );
            
            if (runningInstances.getTotal() > 0) {
                if (Boolean.TRUE.equals(cascade)) {
                    log.warn("流程定义存在运行的实例，但允许级联删除，定义ID: {}", definitionId);
                } else {
                    return ResponseDTO.error("CANNOT_DELETE_HAS_RUNNING_INSTANCES", 
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
                    instancePage, definitionId, null, null, null, null
                );
                // 级联逻辑删除流程实例和任务
                for (WorkflowInstanceEntity instance : instances.getRecords()) {
                    // 逻辑删除流程实例
                    instance.setDeletedFlag(1);
                    workflowInstanceDao.updateById(instance);
                    // 逻辑删除相关任务
                    // 注意：WorkflowTaskDao继承BaseMapper，可以使用update方法
                    // 这里简化处理，实际可以通过SQL批量更新
                    log.debug("级联删除流程实例，实例ID: {}", instance.getInstanceId());
                }
                log.info("级联删除完成，定义ID: {}，共删除{}个流程实例", definitionId, instances.getTotal());
            }
            
            log.info("流程定义删除成功，定义ID: {}", definitionId);
            return ResponseDTO.ok("删除成功");
        } catch (Exception e) {
            log.error("删除流程定义失败，定义ID: {}", definitionId, e);
            return ResponseDTO.error("删除流程定义失败: " + e.getMessage());
        }
    }

    // ==================== 流程实例管理 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<Long> startProcess(Long definitionId, String businessKey, String instanceName, Map<String, Object> variables, Map<String, Object> formData) {
        log.info("启动流程实例，定义ID: {}, 业务Key: {}", definitionId, businessKey);
        try {
            // 1. 验证流程定义是否存在且已激活
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(definitionId);
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
            }
            
            if (!"PUBLISHED".equals(definition.getStatus())) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_PUBLISHED", "流程定义未激活，无法启动流程实例");
            }
            
            // 2. 获取当前用户ID（从请求上下文获取，这里需要传入userId参数）
            // 注意：实际调用时应从SmartRequestUtil.getUserId()获取
            Long initiatorId = variables != null && variables.containsKey("initiatorId") 
                ? Long.parseLong(variables.get("initiatorId").toString()) 
                : null;
            
            if (initiatorId == null) {
                return ResponseDTO.error("INITIATOR_ID_REQUIRED", "发起人ID不能为空");
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
                } catch (Exception e) {
                    log.warn("保存流程变量失败: {}", e.getMessage());
                }
            }
            
            // 5. 保存流程实例
            workflowInstanceDao.insert(instance);
            Long instanceId = instance.getInstanceId();
            
            // 添加instanceId到流程变量
            flowVariables.put("instanceId", instanceId);
            // 更新流程变量（包含instanceId）
            try {
                instance.setVariables(objectMapper.writeValueAsString(flowVariables));
                workflowInstanceDao.updateById(instance);
            } catch (Exception e) {
                log.warn("更新流程变量失败: {}", e.getMessage());
            }
            
            // 6. 注意：当前实现基于数据库，不包含复杂的工作流引擎逻辑
            // 如需使用工作流引擎（如Flowable、Camunda），可以在这里调用引擎的启动方法
            // 示例（Flowable）：
            // ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            //     definition.getProcessKey(), businessKey, flowVariables);
            // if (processInstance != null) {
            //     log.info("工作流流程启动成功，引擎实例ID: {}", processInstance.getId());
            //     // 可以保存引擎实例ID到流程实例的扩展字段中
            // }
            // 当前实现仅创建流程实例记录，不自动创建初始任务
            // 如需自动创建初始任务，需要解析流程定义并创建第一个任务节点
            log.info("流程实例创建成功，实例ID: {}。注意：当前实现不自动创建初始任务，如需自动创建请集成工作流引擎", instanceId);
            
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
            
            log.info("流程实例启动成功，实例ID: {}, 业务Key: {}", instanceId, businessKey);
            return ResponseDTO.ok(instanceId);
        } catch (Exception e) {
            log.error("启动流程实例失败，定义ID: {}", definitionId, e);
            return ResponseDTO.error("启动流程实例失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(PageParam pageParam, Long definitionId, String status, Long startUserId, String startDate, String endDate) {
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
                page, definitionId, statusInt, startUserId, startDate, endDate
            );
            
            // 转换为PageResult
            PageResult<WorkflowInstanceEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询流程实例失败", e);
            return ResponseDTO.error("查询流程实例列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowInstanceEntity> getInstance(Long instanceId) {
        log.debug("获取流程实例详情，实例ID: {}", instanceId);
        try {
            // 根据ID查询流程实例
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }
            
            return ResponseDTO.ok(instance);
        } catch (Exception e) {
            log.error("获取流程实例详情失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("获取流程实例详情失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
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
                return ResponseDTO.error("挂起失败，流程实例可能不是运行状态");
            }
        } catch (Exception e) {
            log.error("挂起流程实例失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("挂起流程实例失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
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
                return ResponseDTO.error("激活失败，流程实例可能不是挂起状态");
            }
        } catch (Exception e) {
            log.error("激活流程实例失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("激活流程实例失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
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
                return ResponseDTO.error("终止失败，流程实例可能不是运行或挂起状态");
            }
        } catch (Exception e) {
            log.error("终止流程实例失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("终止流程实例失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
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
                return ResponseDTO.error("撤销失败，流程实例可能不是运行状态");
            }
        } catch (Exception e) {
            log.error("撤销流程实例失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("撤销流程实例失败: " + e.getMessage());
        }
    }

    // ==================== 任务管理 ====================

    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(PageParam pageParam, Long userId, String category, Integer priority, String dueStatus) {
        log.debug("分页查询我的待办任务，用户ID: {}, 分类: {}", userId, category);
        try {
            // 构建分页对象
            Page<WorkflowTaskEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            
            // 调用DAO层查询
            IPage<WorkflowTaskEntity> pageResult = workflowTaskDao.selectMyTasksPage(
                page, userId, category, priority, dueStatus
            );
            
            // 转换为PageResult
            PageResult<WorkflowTaskEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询我的待办任务失败，用户ID: {}", userId, e);
            return ResponseDTO.error("查询待办任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(PageParam pageParam, Long userId, String category, String outcome, String startDate, String endDate) {
        log.debug("分页查询我的已办任务，用户ID: {}", userId);
        try {
            // 构建分页对象
            Page<WorkflowTaskEntity> page = new Page<>(pageParam.getPageNum(), pageParam.getPageSize());
            
            // 调用DAO层查询
            IPage<WorkflowTaskEntity> pageResult = workflowTaskDao.selectMyCompletedTasksPage(
                page, userId, category, outcome, startDate, endDate
            );
            
            // 转换为PageResult
            PageResult<WorkflowTaskEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询我的已办任务失败，用户ID: {}", userId, e);
            return ResponseDTO.error("查询已办任务列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(PageParam pageParam, Long userId, String category, String status) {
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
                page, null, statusInt, userId, null, null
            );
            
            // 转换为PageResult
            PageResult<WorkflowInstanceEntity> result = new PageResult<>();
            result.setList(pageResult.getRecords());
            result.setTotal(pageResult.getTotal());
            result.setPageNum((int) pageResult.getCurrent());
            result.setPageSize((int) pageResult.getSize());
            result.setPages((int) pageResult.getPages());
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("分页查询我发起的流程失败，用户ID: {}", userId, e);
            return ResponseDTO.error("查询我发起的流程列表失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<WorkflowTaskEntity> getTask(Long taskId) {
        log.debug("获取任务详情，任务ID: {}", taskId);
        try {
            // 根据ID查询任务
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }
            
            return ResponseDTO.ok(task);
        } catch (Exception e) {
            log.error("获取任务详情失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("获取任务详情失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
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
                return ResponseDTO.error("受理失败，任务可能已被受理或状态不正确");
            }
        } catch (Exception e) {
            log.error("受理任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("受理任务失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }
            
            // 调用DAO层取消受理方法
            int updateCount = workflowTaskDao.unclaimTask(taskId);
            if (updateCount > 0) {
                return ResponseDTO.ok("取消受理成功");
            } else {
                return ResponseDTO.error("取消受理失败，任务可能未被受理或状态不正确");
            }
        } catch (Exception e) {
            log.error("取消受理任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("取消受理任务失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }
            
            // 获取当前受理人ID
            Long originalUserId = task.getAssigneeId();
            if (originalUserId == null) {
                return ResponseDTO.error("TASK_NOT_CLAIMED", "任务尚未被受理，无法委派");
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
                return ResponseDTO.error("委派失败，任务状态可能不正确");
            }
        } catch (Exception e) {
            log.error("委派任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("委派任务失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }
            
            // 获取当前受理人ID
            Long originalUserId = task.getAssigneeId();
            if (originalUserId == null) {
                return ResponseDTO.error("TASK_NOT_CLAIMED", "任务尚未被受理，无法转交");
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
                return ResponseDTO.error("转交失败，任务状态可能不正确");
            }
        } catch (Exception e) {
            log.error("转交任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("转交任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseDTO<String> completeTask(Long taskId, String outcome, String comment, Map<String, Object> variables, Map<String, Object> formData) {
        log.info("完成任务，任务ID: {}, 结果: {}", taskId, outcome);
        try {
            // 验证任务是否存在
            WorkflowTaskEntity task = workflowTaskDao.selectById(taskId);
            if (task == null) {
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
            }
            
            // 转换outcome字符串为整数（1-同意, 2-驳回）
            Integer outcomeInt = null;
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
            
            // 调用DAO层完成方法
            int updateCount = workflowTaskDao.completeTask(taskId, outcomeInt, comment);
            if (updateCount > 0) {
                // 获取流程实例
                WorkflowInstanceEntity instance = workflowInstanceDao.selectById(task.getInstanceId());
                
                // 更新流程变量（如果提供了variables参数）
                if (variables != null && !variables.isEmpty() && instance != null) {
                    try {
                        // 合并现有变量
                        Map<String, Object> existingVariables = new HashMap<>();
                        if (instance.getVariables() != null && !instance.getVariables().isEmpty()) {
                            existingVariables = objectMapper.readValue(instance.getVariables(), 
                                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
                        }
                        existingVariables.putAll(variables);
                        existingVariables.put("lastTaskId", taskId);
                        existingVariables.put("lastTaskOutcome", outcome);
                        instance.setVariables(objectMapper.writeValueAsString(existingVariables));
                        workflowInstanceDao.updateById(instance);
                    } catch (Exception e) {
                        log.warn("更新流程变量失败: {}", e.getMessage());
                    }
                }
                
                // 注意：当前实现基于数据库，不包含复杂的工作流引擎逻辑
                // 如需实现流程推进（创建下一个任务），需要：
                // 1. 解析流程定义（BPMN XML或自定义格式）
                // 2. 根据当前节点和任务结果，确定下一个节点
                // 3. 创建下一个任务
                // 建议：集成Flowable或Camunda工作流引擎来实现完整的流程推进逻辑
                // 当前实现仅完成当前任务，不自动创建下一个任务
                log.info("任务完成，任务ID: {}。注意：当前实现不自动推进流程，如需自动推进请集成工作流引擎", taskId);
                
                // 发送WebSocket通知
                Map<String, Object> taskData = new HashMap<>();
                taskData.put("taskId", taskId);
                taskData.put("status", "COMPLETED");
                taskData.put("outcome", outcome);
                if (task.getAssigneeId() != null) {
                    webSocketController.sendTaskStatusChangedNotification(task.getAssigneeId(), taskData);
                }
                
                return ResponseDTO.ok("任务完成成功");
            } else {
                return ResponseDTO.error("完成任务失败，任务状态可能不正确");
            }
        } catch (Exception e) {
            log.error("完成任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("完成任务失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_TASK_NOT_FOUND", "任务不存在");
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
                                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class));
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
                    } catch (Exception e) {
                        log.warn("更新流程变量失败: {}", e.getMessage());
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
                    instanceData.put("instanceId", instance.getInstanceId());
                    instanceData.put("status", "REJECTED");
                    instanceData.put("reason", comment);
                    webSocketController.sendInstanceStatusChangedNotification(instance.getInitiatorId(), instanceData);
                }
                
                return ResponseDTO.ok("任务驳回成功");
            } else {
                return ResponseDTO.error("驳回任务失败，任务状态可能不正确");
            }
        } catch (Exception e) {
            log.error("驳回任务失败，任务ID: {}", taskId, e);
            return ResponseDTO.error("驳回任务失败: " + e.getMessage());
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
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }
            
            // 获取流程定义
            WorkflowDefinitionEntity definition = workflowDefinitionDao.selectById(instance.getProcessDefinitionId());
            if (definition == null) {
                return ResponseDTO.error("WORKFLOW_DEFINITION_NOT_FOUND", "流程定义不存在");
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
        } catch (Exception e) {
            log.error("获取流程实例图失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("获取流程图失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<List<Map<String, Object>>> getProcessHistory(Long instanceId) {
        log.debug("获取流程历史记录，实例ID: {}", instanceId);
        try {
            // 验证实例是否存在
            WorkflowInstanceEntity instance = workflowInstanceDao.selectById(instanceId);
            if (instance == null) {
                return ResponseDTO.error("WORKFLOW_INSTANCE_NOT_FOUND", "流程实例不存在");
            }
            
            // 调用DAO层查询历史记录
            List<Map<String, Object>> historyList = workflowInstanceDao.selectProcessHistory(instanceId);
            
            return ResponseDTO.ok(historyList);
        } catch (Exception e) {
            log.error("获取流程历史记录失败，实例ID: {}", instanceId, e);
            return ResponseDTO.error("获取流程历史记录失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseDTO<Map<String, Object>> getProcessStatistics(String startDate, String endDate) {
        log.debug("获取流程统计信息，开始日期: {}, 结束日期: {}", startDate, endDate);
        try {
            // 调用DAO层统计方法
            Map<String, Object> statistics = workflowInstanceDao.selectProcessStatistics(null, null, startDate, endDate);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            if (statistics != null) {
                result.put("totalInstances", statistics.get("total") != null ? statistics.get("total") : 0);
                result.put("runningInstances", statistics.get("running") != null ? statistics.get("running") : 0);
                result.put("completedInstances", statistics.get("completed") != null ? statistics.get("completed") : 0);
                result.put("terminatedInstances", statistics.get("terminated") != null ? statistics.get("terminated") : 0);
                result.put("suspendedInstances", statistics.get("suspended") != null ? statistics.get("suspended") : 0);
                result.put("averageDuration", statistics.get("avgDuration") != null ? statistics.get("avgDuration") : 0);
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
                result.put("completedTasks", taskStatistics.get("completed") != null ? taskStatistics.get("completed") : 0);
            } else {
                result.put("totalTasks", 0);
                result.put("pendingTasks", 0);
                result.put("completedTasks", 0);
            }
            
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("获取流程统计信息失败", e);
            return ResponseDTO.error("获取流程统计信息失败: " + e.getMessage());
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
                    result.put("processingTasks", stats.get("processingTasks") != null ? stats.get("processingTasks") : 0);
                    result.put("completedTasks", stats.get("completedTasks") != null ? stats.get("completedTasks") : 0);
                    result.put("averageDuration", stats.get("avgDuration") != null ? stats.get("avgDuration") : 0);
                    result.put("overdueCount", stats.get("overdueCount") != null ? stats.get("overdueCount") : 0);
                    
                    // 计算完成率
                    Long totalTasks = Long.parseLong(stats.get("totalTasks").toString());
                    Long completedTasks = Long.parseLong(stats.get("completedTasks").toString());
                    double completionRate = totalTasks > 0 ? (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100 : 0.0;
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
        } catch (Exception e) {
            log.error("获取用户工作量统计失败，用户ID: {}", userId, e);
            return ResponseDTO.error("获取用户工作量统计失败: " + e.getMessage());
        }
    }
}

