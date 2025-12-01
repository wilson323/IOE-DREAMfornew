package net.lab1024.sa.admin.module.oa.workflow.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.admin.module.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.admin.module.oa.workflow.dao.WorkflowTaskDao;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.admin.module.oa.workflow.domain.entity.WorkflowTaskEntity;
import net.lab1024.sa.admin.module.oa.workflow.manager.WorkflowEngineManager;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartResponseUtil;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Workflow Engine Service Implementation
 *
 * Provides BPMN 2.0 based workflow engine functionality including process management,
 * instance control, task handling, and monitoring
 * Follows repowiki architecture design: Service layer handles business logic and transaction management,
 * Manager layer handles complex workflow operations and BPMN processing
 *
 * @author SmartAdmin Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@Transactional(rollbackFor = Throwable.class)
public class WorkflowEngineServiceImpl extends ServiceImpl implements WorkflowEngineService {

    @Resource
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    @Resource
    private WorkflowEngineManager workflowEngineManager;

    // ==================== Process Definition Management ====================

    /**
     * Deploy BPMN process definition
     *
     * @param bpmnXml BPMN XML content
     * @param processName process name
     * @param processKey process key
     * @param description description
     * @param category category
     * @return deployment result
     */
    @Override
    public ResponseDTO<String> deployProcess(String bpmnXml, String processName,
                                           String processKey, String description, String category) {
        try {
            log.info("Deploy BPMN process: processName={}, processKey={}", processName, processKey);

            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return SmartResponseUtil.error("BPMN XML content cannot be empty");
            }

            // Use manager for complex BPMN deployment logic
            String deploymentId = workflowEngineManager.deployProcess(bpmnXml, processName, processKey, description, category);

            log.info("BPMN process deployed successfully: processName={}, processKey={}, deploymentId={}",
                    processName, processKey, deploymentId);
            return SmartResponseUtil.success(deploymentId, "Process deployed successfully");

        } catch (Exception e) {
            log.error("Failed to deploy BPMN process: processName={}, processKey={}", processName, processKey, e);
            return SmartResponseUtil.error("Failed to deploy process: " + e.getMessage());
        }
    }

    /**
     * Query process definitions with pagination
     *
     * @param pageParam pagination parameters
     * @param category category (optional)
     * @param status status (optional)
     * @param keyword keyword (optional)
     * @return pagination result
     */
    @Override
    public ResponseDTO<PageResult<WorkflowDefinitionEntity>> pageDefinitions(PageParam pageParam,
                                                                          String category, String status, String keyword) {
        try {
            log.info("Query process definitions: category={}, status={}, keyword={}", category, status, keyword);

            if (pageParam == null) {
                return SmartResponseUtil.error("Page parameters cannot be null");
            }

            // Use manager for complex query logic
            PageResult<WorkflowDefinitionEntity> result = workflowEngineManager.queryDefinitions(pageParam, category, status, keyword);

            log.info("Process definitions queried successfully: totalCount={}", result.getTotalCount());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Failed to query process definitions", e);
            return SmartResponseUtil.error("Failed to query process definitions: " + e.getMessage());
        }
    }

    /**
     * Get process definition details
     *
     * @param definitionId definition ID
     * @return definition details
     */
    @Override
    public ResponseDTO<WorkflowDefinitionEntity> getDefinition(Long definitionId) {
        try {
            log.info("Get process definition: definitionId={}", definitionId);

            if (definitionId == null) {
                return SmartResponseUtil.error("Definition ID cannot be null");
            }

            WorkflowDefinitionEntity definition = workflowEngineManager.getDefinition(definitionId);
            if (definition == null) {
                return SmartResponseUtil.error("Process definition not found");
            }

            log.info("Process definition retrieved successfully: definitionId={}", definitionId);
            return SmartResponseUtil.success(definition);

        } catch (Exception e) {
            log.error("Failed to get process definition: definitionId={}", definitionId, e);
            return SmartResponseUtil.error("Failed to get process definition: " + e.getMessage());
        }
    }

    /**
     * Activate process definition
     *
     * @param definitionId definition ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> activateDefinition(Long definitionId) {
        try {
            log.info("Activate process definition: definitionId={}", definitionId);

            if (definitionId == null) {
                return SmartResponseUtil.error("Definition ID cannot be null");
            }

            String result = workflowEngineManager.activateDefinition(definitionId);

            log.info("Process definition activated successfully: definitionId={}", definitionId);
            return SmartResponseUtil.success(result, "Definition activated successfully");

        } catch (Exception e) {
            log.error("Failed to activate process definition: definitionId={}", definitionId, e);
            return SmartResponseUtil.error("Failed to activate definition: " + e.getMessage());
        }
    }

    /**
     * Disable process definition
     *
     * @param definitionId definition ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> disableDefinition(Long definitionId) {
        try {
            log.info("Disable process definition: definitionId={}", definitionId);

            if (definitionId == null) {
                return SmartResponseUtil.error("Definition ID cannot be null");
            }

            String result = workflowEngineManager.disableDefinition(definitionId);

            log.info("Process definition disabled successfully: definitionId={}", definitionId);
            return SmartResponseUtil.success(result, "Definition disabled successfully");

        } catch (Exception e) {
            log.error("Failed to disable process definition: definitionId={}", definitionId, e);
            return SmartResponseUtil.error("Failed to disable definition: " + e.getMessage());
        }
    }

    /**
     * Delete process definition
     *
     * @param definitionId definition ID
     * @param cascade whether to cascade delete instances
     * @return operation result
     */
    @Override
    public ResponseDTO<String> deleteDefinition(Long definitionId, Boolean cascade) {
        try {
            log.info("Delete process definition: definitionId={}, cascade={}", definitionId, cascade);

            if (definitionId == null) {
                return SmartResponseUtil.error("Definition ID cannot be null");
            }

            String result = workflowEngineManager.deleteDefinition(definitionId, cascade != null ? cascade : false);

            log.info("Process definition deleted successfully: definitionId={}", definitionId);
            return SmartResponseUtil.success(result, "Definition deleted successfully");

        } catch (Exception e) {
            log.error("Failed to delete process definition: definitionId={}", definitionId, e);
            return SmartResponseUtil.error("Failed to delete definition: " + e.getMessage());
        }
    }

    // ==================== Process Instance Management ====================

    /**
     * Start process instance
     *
     * @param definitionId definition ID
     * @param businessKey business key
     * @param instanceName instance name
     * @param variables process variables
     * @param formData form data
     * @return instance ID
     */
    @Override
    public ResponseDTO<Long> startProcess(Long definitionId, String businessKey,
                                         String instanceName, Map<String, Object> variables, Map<String, Object> formData) {
        try {
            log.info("Start process instance: definitionId={}, businessKey={}", definitionId, businessKey);

            if (definitionId == null) {
                return SmartResponseUtil.error("Definition ID cannot be null");
            }

            // Use manager for complex process start logic
            Long instanceId = workflowEngineManager.startProcess(definitionId, businessKey, instanceName, variables, formData);

            log.info("Process instance started successfully: definitionId={}, instanceId={}", definitionId, instanceId);
            return SmartResponseUtil.success(instanceId, "Process started successfully");

        } catch (Exception e) {
            log.error("Failed to start process instance: definitionId={}", definitionId, e);
            return SmartResponseUtil.error("Failed to start process: " + e.getMessage());
        }
    }

    /**
     * Query process instances with pagination
     *
     * @param pageParam pagination parameters
     * @param definitionId definition ID (optional)
     * @param status status (optional)
     * @param startUserId starter user ID (optional)
     * @param startDate start date (optional)
     * @param endDate end date (optional)
     * @return pagination result
     */
    @Override
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageInstances(PageParam pageParam,
                                                                       Long definitionId, String status,
                                                                       Long startUserId, String startDate, String endDate) {
        try {
            log.info("Query process instances: definitionId={}, status={}, startUserId={}", definitionId, status, startUserId);

            if (pageParam == null) {
                return SmartResponseUtil.error("Page parameters cannot be null");
            }

            // Use manager for complex query logic
            PageResult<WorkflowInstanceEntity> result = workflowEngineManager.queryInstances(pageParam, definitionId, status, startUserId, startDate, endDate);

            log.info("Process instances queried successfully: totalCount={}", result.getTotalCount());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Failed to query process instances", e);
            return SmartResponseUtil.error("Failed to query process instances: " + e.getMessage());
        }
    }

    /**
     * Get process instance details
     *
     * @param instanceId instance ID
     * @return instance details
     */
    @Override
    public ResponseDTO<WorkflowInstanceEntity> getInstance(Long instanceId) {
        try {
            log.info("Get process instance: instanceId={}", instanceId);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            WorkflowInstanceEntity instance = workflowEngineManager.getInstance(instanceId);
            if (instance == null) {
                return SmartResponseUtil.error("Process instance not found");
            }

            log.info("Process instance retrieved successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(instance);

        } catch (Exception e) {
            log.error("Failed to get process instance: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to get process instance: " + e.getMessage());
        }
    }

    /**
     * Suspend process instance
     *
     * @param instanceId instance ID
     * @param reason suspension reason
     * @return operation result
     */
    @Override
    public ResponseDTO<String> suspendInstance(Long instanceId, String reason) {
        try {
            log.info("Suspend process instance: instanceId={}, reason={}", instanceId, reason);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            String result = workflowEngineManager.suspendInstance(instanceId, reason);

            log.info("Process instance suspended successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(result, "Instance suspended successfully");

        } catch (Exception e) {
            log.error("Failed to suspend process instance: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to suspend instance: " + e.getMessage());
        }
    }

    /**
     * Activate process instance
     *
     * @param instanceId instance ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> activateInstance(Long instanceId) {
        try {
            log.info("Activate process instance: instanceId={}", instanceId);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            String result = workflowEngineManager.activateInstance(instanceId);

            log.info("Process instance activated successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(result, "Instance activated successfully");

        } catch (Exception e) {
            log.error("Failed to activate process instance: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to activate instance: " + e.getMessage());
        }
    }

    /**
     * Terminate process instance
     *
     * @param instanceId instance ID
     * @param reason termination reason
     * @return operation result
     */
    @Override
    public ResponseDTO<String> terminateInstance(Long instanceId, String reason) {
        try {
            log.info("Terminate process instance: instanceId={}, reason={}", instanceId, reason);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            String result = workflowEngineManager.terminateInstance(instanceId, reason);

            log.info("Process instance terminated successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(result, "Instance terminated successfully");

        } catch (Exception e) {
            log.error("Failed to terminate process instance: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to terminate instance: " + e.getMessage());
        }
    }

    /**
     * Revoke process instance
     *
     * @param instanceId instance ID
     * @param reason revocation reason
     * @return operation result
     */
    @Override
    public ResponseDTO<String> revokeInstance(Long instanceId, String reason) {
        try {
            log.info("Revoke process instance: instanceId={}, reason={}", instanceId, reason);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            String result = workflowEngineManager.revokeInstance(instanceId, reason);

            log.info("Process instance revoked successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(result, "Instance revoked successfully");

        } catch (Exception e) {
            log.error("Failed to revoke process instance: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to revoke instance: " + e.getMessage());
        }
    }

    // ==================== Task Management ====================

    /**
     * Query my pending tasks with pagination
     *
     * @param pageParam pagination parameters
     * @param userId user ID
     * @param category category (optional)
     * @param priority priority (optional)
     * @param dueStatus due status (optional)
     * @return pagination result
     */
    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyTasks(PageParam pageParam, Long userId,
                                                                  String category, Integer priority, String dueStatus) {
        try {
            log.info("Query my pending tasks: userId={}, category={}, priority={}", userId, category, priority);

            if (pageParam == null || userId == null) {
                return SmartResponseUtil.error("Page parameters and user ID cannot be null");
            }

            // Use manager for complex task query logic
            PageResult<WorkflowTaskEntity> result = workflowEngineManager.queryMyTasks(pageParam, userId, category, priority, dueStatus);

            log.info("My pending tasks queried successfully: userId={}, totalCount={}", userId, result.getTotalCount());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Failed to query my pending tasks: userId={}", userId, e);
            return SmartResponseUtil.error("Failed to query my tasks: " + e.getMessage());
        }
    }

    /**
     * Query my completed tasks with pagination
     *
     * @param pageParam pagination parameters
     * @param userId user ID
     * @param category category (optional)
     * @param outcome processing result (optional)
     * @param startDate start date (optional)
     * @param endDate end date (optional)
     * @return pagination result
     */
    @Override
    public ResponseDTO<PageResult<WorkflowTaskEntity>> pageMyCompletedTasks(PageParam pageParam, Long userId,
                                                                          String category, String outcome,
                                                                          String startDate, String endDate) {
        try {
            log.info("Query my completed tasks: userId={}, category={}, outcome={}", userId, category, outcome);

            if (pageParam == null || userId == null) {
                return SmartResponseUtil.error("Page parameters and user ID cannot be null");
            }

            // Use manager for complex task query logic
            PageResult<WorkflowTaskEntity> result = workflowEngineManager.queryMyCompletedTasks(pageParam, userId, category, outcome, startDate, endDate);

            log.info("My completed tasks queried successfully: userId={}, totalCount={}", userId, result.getTotalCount());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Failed to query my completed tasks: userId={}", userId, e);
            return SmartResponseUtil.error("Failed to query my completed tasks: " + e.getMessage());
        }
    }

    /**
     * Query processes I started with pagination
     *
     * @param pageParam pagination parameters
     * @param userId user ID
     * @param category category (optional)
     * @param status status (optional)
     * @return pagination result
     */
    @Override
    public ResponseDTO<PageResult<WorkflowInstanceEntity>> pageMyProcesses(PageParam pageParam, Long userId,
                                                                           String category, String status) {
        try {
            log.info("Query processes I started: userId={}, category={}, status={}", userId, category, status);

            if (pageParam == null || userId == null) {
                return SmartResponseUtil.error("Page parameters and user ID cannot be null");
            }

            // Use manager for complex process query logic
            PageResult<WorkflowInstanceEntity> result = workflowEngineManager.queryMyProcesses(pageParam, userId, category, status);

            log.info("Processes I started queried successfully: userId={}, totalCount={}", userId, result.getTotalCount());
            return SmartResponseUtil.success(result);

        } catch (Exception e) {
            log.error("Failed to query processes I started: userId={}", userId, e);
            return SmartResponseUtil.error("Failed to query my processes: " + e.getMessage());
        }
    }

    /**
     * Get task details
     *
     * @param taskId task ID
     * @return task details
     */
    @Override
    public ResponseDTO<WorkflowTaskEntity> getTask(Long taskId) {
        try {
            log.info("Get workflow task: taskId={}", taskId);

            if (taskId == null) {
                return SmartResponseUtil.error("Task ID cannot be null");
            }

            WorkflowTaskEntity task = workflowEngineManager.getTask(taskId);
            if (task == null) {
                return SmartResponseUtil.error("Task not found");
            }

            log.info("Workflow task retrieved successfully: taskId={}", taskId);
            return SmartResponseUtil.success(task);

        } catch (Exception e) {
            log.error("Failed to get workflow task: taskId={}", taskId, e);
            return SmartResponseUtil.error("Failed to get task: " + e.getMessage());
        }
    }

    /**
     * Claim task
     *
     * @param taskId task ID
     * @param userId user ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> claimTask(Long taskId, Long userId) {
        try {
            log.info("Claim workflow task: taskId={}, userId={}", taskId, userId);

            if (taskId == null || userId == null) {
                return SmartResponseUtil.error("Task ID and user ID cannot be null");
            }

            String result = workflowEngineManager.claimTask(taskId, userId);

            log.info("Workflow task claimed successfully: taskId={}, userId={}", taskId, userId);
            return SmartResponseUtil.success(result, "Task claimed successfully");

        } catch (Exception e) {
            log.error("Failed to claim workflow task: taskId={}, userId={}", taskId, userId, e);
            return SmartResponseUtil.error("Failed to claim task: " + e.getMessage());
        }
    }

    /**
     * Unclaim task
     *
     * @param taskId task ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> unclaimTask(Long taskId) {
        try {
            log.info("Unclaim workflow task: taskId={}", taskId);

            if (taskId == null) {
                return SmartResponseUtil.error("Task ID cannot be null");
            }

            String result = workflowEngineManager.unclaimTask(taskId);

            log.info("Workflow task unclaimed successfully: taskId={}", taskId);
            return SmartResponseUtil.success(result, "Task unclaimed successfully");

        } catch (Exception e) {
            log.error("Failed to unclaim workflow task: taskId={}", taskId, e);
            return SmartResponseUtil.error("Failed to unclaim task: " + e.getMessage());
        }
    }

    /**
     * Delegate task
     *
     * @param taskId task ID
     * @param targetUserId target user ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> delegateTask(Long taskId, Long targetUserId) {
        try {
            log.info("Delegate workflow task: taskId={}, targetUserId={}", taskId, targetUserId);

            if (taskId == null || targetUserId == null) {
                return SmartResponseUtil.error("Task ID and target user ID cannot be null");
            }

            String result = workflowEngineManager.delegateTask(taskId, targetUserId);

            log.info("Workflow task delegated successfully: taskId={}, targetUserId={}", taskId, targetUserId);
            return SmartResponseUtil.success(result, "Task delegated successfully");

        } catch (Exception e) {
            log.error("Failed to delegate workflow task: taskId={}, targetUserId={}", taskId, targetUserId, e);
            return SmartResponseUtil.error("Failed to delegate task: " + e.getMessage());
        }
    }

    /**
     * Transfer task
     *
     * @param taskId task ID
     * @param targetUserId target user ID
     * @return operation result
     */
    @Override
    public ResponseDTO<String> transferTask(Long taskId, Long targetUserId) {
        try {
            log.info("Transfer workflow task: taskId={}, targetUserId={}", taskId, targetUserId);

            if (taskId == null || targetUserId == null) {
                return SmartResponseUtil.error("Task ID and target user ID cannot be null");
            }

            String result = workflowEngineManager.transferTask(taskId, targetUserId);

            log.info("Workflow task transferred successfully: taskId={}, targetUserId={}", taskId, targetUserId);
            return SmartResponseUtil.success(result, "Task transferred successfully");

        } catch (Exception e) {
            log.error("Failed to transfer workflow task: taskId={}, targetUserId={}", taskId, targetUserId, e);
            return SmartResponseUtil.error("Failed to transfer task: " + e.getMessage());
        }
    }

    /**
     * Complete task
     *
     * @param taskId task ID
     * @param outcome processing result
     * @param comment processing comment
     * @param variables variables
     * @param formData form data
     * @return operation result
     */
    @Override
    public ResponseDTO<String> completeTask(Long taskId, String outcome, String comment,
                                           Map<String, Object> variables, Map<String, Object> formData) {
        try {
            log.info("Complete workflow task: taskId={}, outcome={}", taskId, outcome);

            if (taskId == null) {
                return SmartResponseUtil.error("Task ID cannot be null");
            }

            // Use manager for complex task completion logic
            String result = workflowEngineManager.completeTask(taskId, outcome, comment, variables, formData);

            log.info("Workflow task completed successfully: taskId={}, outcome={}", taskId, outcome);
            return SmartResponseUtil.success(result, "Task completed successfully");

        } catch (Exception e) {
            log.error("Failed to complete workflow task: taskId={}, outcome={}", taskId, outcome, e);
            return SmartResponseUtil.error("Failed to complete task: " + e.getMessage());
        }
    }

    /**
     * Reject task
     *
     * @param taskId task ID
     * @param comment rejection reason
     * @param variables variables
     * @return operation result
     */
    @Override
    public ResponseDTO<String> rejectTask(Long taskId, String comment, Map<String, Object> variables) {
        try {
            log.info("Reject workflow task: taskId={}", taskId);

            if (taskId == null) {
                return SmartResponseUtil.error("Task ID cannot be null");
            }

            // Use manager for complex task rejection logic
            String result = workflowEngineManager.rejectTask(taskId, comment, variables);

            log.info("Workflow task rejected successfully: taskId={}", taskId);
            return SmartResponseUtil.success(result, "Task rejected successfully");

        } catch (Exception e) {
            log.error("Failed to reject workflow task: taskId={}", taskId, e);
            return SmartResponseUtil.error("Failed to reject task: " + e.getMessage());
        }
    }

    // ==================== Process Monitoring ====================

    /**
     * Get process diagram and current position
     *
     * @param instanceId instance ID
     * @return process diagram information
     */
    @Override
    public ResponseDTO<Map<String, Object>> getProcessDiagram(Long instanceId) {
        try {
            log.info("Get process diagram: instanceId={}", instanceId);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            Map<String, Object> diagram = workflowEngineManager.getProcessDiagram(instanceId);

            log.info("Process diagram retrieved successfully: instanceId={}", instanceId);
            return SmartResponseUtil.success(diagram);

        } catch (Exception e) {
            log.error("Failed to get process diagram: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to get process diagram: " + e.getMessage());
        }
    }

    /**
     * Get process history
     *
     * @param instanceId instance ID
     * @return history records
     */
    @Override
    public ResponseDTO<List<Map<String, Object>>> getProcessHistory(Long instanceId) {
        try {
            log.info("Get process history: instanceId={}", instanceId);

            if (instanceId == null) {
                return SmartResponseUtil.error("Instance ID cannot be null");
            }

            List<Map<String, Object>> history = workflowEngineManager.getProcessHistory(instanceId);

            log.info("Process history retrieved successfully: instanceId={}, recordCount={}", instanceId, history.size());
            return SmartResponseUtil.success(history);

        } catch (Exception e) {
            log.error("Failed to get process history: instanceId={}", instanceId, e);
            return SmartResponseUtil.error("Failed to get process history: " + e.getMessage());
        }
    }

    /**
     * Get process statistics
     *
     * @param startDate start date
     * @param endDate end date
     * @return statistics
     */
    @Override
    public ResponseDTO<Map<String, Object>> getProcessStatistics(String startDate, String endDate) {
        try {
            log.info("Get process statistics: startDate={}, endDate={}", startDate, endDate);

            Map<String, Object> statistics = workflowEngineManager.getProcessStatistics(startDate, endDate);

            log.info("Process statistics retrieved successfully");
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("Failed to get process statistics", e);
            return SmartResponseUtil.error("Failed to get process statistics: " + e.getMessage());
        }
    }

    /**
     * Get user workload statistics
     *
     * @param userId user ID
     * @param startDate start date
     * @param endDate end date
     * @return workload statistics
     */
    @Override
    public ResponseDTO<Map<String, Object>> getUserWorkloadStatistics(Long userId, String startDate, String endDate) {
        try {
            log.info("Get user workload statistics: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

            if (userId == null) {
                return SmartResponseUtil.error("User ID cannot be null");
            }

            Map<String, Object> statistics = workflowEngineManager.getUserWorkloadStatistics(userId, startDate, endDate);

            log.info("User workload statistics retrieved successfully: userId={}", userId);
            return SmartResponseUtil.success(statistics);

        } catch (Exception e) {
            log.error("Failed to get user workload statistics: userId={}", userId, e);
            return SmartResponseUtil.error("Failed to get user workload statistics: " + e.getMessage());
        }
    }

    // ==================== Additional Helper Methods ====================

    /**
     * Get available process categories
     *
     * @return category list
     */
    public ResponseDTO<List<String>> getProcessCategories() {
        try {
            log.info("Get process categories");

            List<String> categories = workflowEngineManager.getProcessCategories();

            log.info("Process categories retrieved successfully: count={}", categories.size());
            return SmartResponseUtil.success(categories);

        } catch (Exception e) {
            log.error("Failed to get process categories", e);
            return SmartResponseUtil.error("Failed to get process categories: " + e.getMessage());
        }
    }

    /**
     * Validate BPMN process definition
     *
     * @param bpmnXml BPMN XML content
     * @return validation result
     */
    public ResponseDTO<Map<String, Object>> validateBpmnProcess(String bpmnXml) {
        try {
            log.info("Validate BPMN process");

            if (bpmnXml == null || bpmnXml.trim().isEmpty()) {
                return SmartResponseUtil.error("BPMN XML content cannot be empty");
            }

            Map<String, Object> validation = workflowEngineManager.validateBpmnProcess(bpmnXml);

            log.info("BPMN process validation completed: isValid={}", validation.get("isValid"));
            return SmartResponseUtil.success(validation);

        } catch (Exception e) {
            log.error("Failed to validate BPMN process", e);
            return SmartResponseUtil.error("Failed to validate BPMN process: " + e.getMessage());
        }
    }
}