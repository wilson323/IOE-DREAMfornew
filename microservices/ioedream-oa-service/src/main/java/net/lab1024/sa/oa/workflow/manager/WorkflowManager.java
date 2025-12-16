package net.lab1024.sa.oa.workflow.manager;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.oa.workflow.model.*;

import java.util.*;

/**
 * 工作流管理器
 *
 * @author IOE-DREAM Team
 * @since 2025-12-08
 */
@Slf4j
public class WorkflowManager {

    private final Map<String, WorkflowDefinition> workflowDefinitions = new HashMap<>();
    private final Map<String, WorkflowTemplate> workflowTemplates = new HashMap<>();

    public WorkflowManager() {
        initializeBuiltinWorkflows();
        initializeBuiltinTemplates();
    }

    public void registerWorkflowDefinition(WorkflowDefinition definition) {
        workflowDefinitions.put(definition.getId(), definition);
        log.info("[工作流管理] 注册工作流定义: {} ({})", definition.getName(), definition.getId());
    }

    private void initializeBuiltinWorkflows() {
        log.info("[工作流管理] 内置工作流初始化完成, 数量: {}", workflowDefinitions.size());
    }

    private void initializeBuiltinTemplates() {
        log.info("[工作流管理] 内置工作流模板初始化完成, 数量: {}", workflowTemplates.size());
    }

    public List<WorkflowDefinition> getWorkflowDefinitions() {
        return new ArrayList<>(workflowDefinitions.values());
    }

    public List<WorkflowTemplate> getWorkflowTemplates() {
        return new ArrayList<>(workflowTemplates.values());
    }
}




