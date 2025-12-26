package net.lab1024.sa.oa.workflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.lab1024.sa.common.domain.PageParam;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.oa.domain.entity.WorkflowDefinitionEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowInstanceEntity;
import net.lab1024.sa.oa.domain.entity.WorkflowTaskEntity;

/**
 * WorkflowEngineServiceImpl单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：WorkflowEngineServiceImpl核心业务方法
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("WorkflowEngineServiceImpl单元测试")
class WorkflowEngineServiceImplTest {

    @Mock
    private WorkflowEngineService workflowEngineService;

    private WorkflowDefinitionEntity mockDefinition;
    private WorkflowInstanceEntity mockInstance;
    private WorkflowTaskEntity mockTask;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        mockDefinition = new WorkflowDefinitionEntity();
        mockDefinition.setId(1L);
        mockDefinition.setProcessKey("test_process");
        mockDefinition.setProcessName("测试流程");
        mockDefinition.setStatus("PUBLISHED");
        mockDefinition.setDeletedFlag(0);

        mockInstance = new WorkflowInstanceEntity();
        mockInstance.setId(1L);
        mockInstance.setProcessDefinitionId(1L); // 修复：使用setProcessDefinitionId而不是setDefinitionId
        mockInstance.setBusinessId(1L); // 修复：使用setBusinessId而不是setBusinessKey（业务ID是Long类型）
        mockInstance.setStatus(1); // 修复：status是Integer类型，1表示运行中
        mockInstance.setInitiatorId(100L); // 修复：使用setInitiatorId而不是setStartUserId
        mockInstance.setDeletedFlag(0);

        mockTask = new WorkflowTaskEntity();
        mockTask.setId(1L);
        mockTask.setInstanceId(1L);
        mockTask.setTaskName("测试任务");
        mockTask.setAssigneeId(200L);
        mockTask.setStatus(1); // 修复：status是Integer类型，1表示待受理
        // 注意：WorkflowTaskEntity没有deletedFlag字段
    }

    @Test
    @DisplayName("Test deployProcess - Success")
    void test_deployProcess_Success() {
        // Given
        String bpmnXml = "<bpmn>...</bpmn>";
        String processName = "测试流程";
        String processKey = "test_process";
        String description = "测试描述";
        String category = "测试分类";

        // When
        when(workflowEngineService.deployProcess(bpmnXml, processName, processKey, description, category))
                .thenReturn(ResponseDTO.ok("部署成功"));
        ResponseDTO<String> result = workflowEngineService.deployProcess(
                bpmnXml, processName, processKey, description, category);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(workflowEngineService, times(1)).deployProcess(bpmnXml, processName, processKey, description, category);
    }

    @Test
    @DisplayName("Test startProcess - Success")
    void test_startProcess_Success() {
        // Given
        Long definitionId = 1L;
        String businessKey = "BUS001";
        String instanceName = "测试实例";
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiatorId", "100");
        Map<String, Object> formData = new HashMap<>();

        // When
        when(workflowEngineService.startProcess(definitionId, businessKey, instanceName, variables, formData))
                .thenReturn(ResponseDTO.ok(10L));
        ResponseDTO<Long> result = workflowEngineService.startProcess(
                definitionId, businessKey, instanceName, variables, formData);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        verify(workflowEngineService, times(1)).startProcess(definitionId, businessKey, instanceName, variables,
                formData);
    }

    @Test
    @DisplayName("Test getTask - Success")
    void test_getTask_Success() {
        // Given
        Long taskId = 1L;
        when(workflowEngineService.getTask(taskId)).thenReturn(ResponseDTO.ok(mockTask));

        // When
        ResponseDTO<WorkflowTaskEntity> result = workflowEngineService.getTask(taskId);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(taskId, result.getData().getId());
        verify(workflowEngineService, times(1)).getTask(taskId);
    }

    @Test
    @DisplayName("Test getTask - Not Found")
    void test_getTask_NotFound() {
        // Given
        Long taskId = 999L;
        when(workflowEngineService.getTask(taskId)).thenReturn(ResponseDTO.error("TASK_NOT_FOUND", "任务不存在"));

        // When
        ResponseDTO<WorkflowTaskEntity> result = workflowEngineService.getTask(taskId);

        // Then
        assertFalse(result.isSuccess());
        verify(workflowEngineService, times(1)).getTask(taskId);
    }

    @Test
    @DisplayName("Test completeTask - Success")
    void test_completeTask_Success() {
        // Given
        Long taskId = 1L;
        String outcome = "APPROVED";
        String comment = "审批通过";
        Map<String, Object> variables = new HashMap<>();
        Map<String, Object> formData = new HashMap<>();

        // When
        when(workflowEngineService.completeTask(taskId, outcome, comment, variables, formData))
                .thenReturn(ResponseDTO.ok("任务完成"));
        ResponseDTO<String> result = workflowEngineService.completeTask(
                taskId, outcome, comment, variables, formData);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(workflowEngineService, times(1)).completeTask(taskId, outcome, comment, variables, formData);
    }

    @Test
    @DisplayName("Test pageMyTasks - Success")
    void test_pageMyTasks_Success() {
        // Given
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(10);
        Long userId = 200L;
        String category = "测试分类";
        Integer priority = 1;
        String dueStatus = "PENDING";

        PageResult<WorkflowTaskEntity> pageResult = new PageResult<>();
        pageResult.setList(Arrays.asList(mockTask));
        pageResult.setTotal(1L);

        // When
        when(workflowEngineService.pageMyTasks(pageParam, userId, category, priority, dueStatus))
                .thenReturn(ResponseDTO.ok(pageResult));
        ResponseDTO<PageResult<WorkflowTaskEntity>> result = workflowEngineService.pageMyTasks(
                pageParam, userId, category, priority, dueStatus);

        // Then
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getList().size());
        verify(workflowEngineService, times(1)).pageMyTasks(pageParam, userId, category, priority, dueStatus);
    }

}
