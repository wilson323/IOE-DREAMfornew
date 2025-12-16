package net.lab1024.sa.oa.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import net.lab1024.sa.common.gateway.GatewayServiceClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 工作流引擎管理器单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("工作流引擎管理器测试")
class WorkflowEngineManagerTest {

    @Mock
    private GatewayServiceClient gatewayServiceClient;

    private WorkflowEngineManager workflowEngineManager;

    @BeforeEach
    void setUp() {
        workflowEngineManager = new WorkflowEngineManager(gatewayServiceClient);
    }

    @Test
    @DisplayName("生成实例编号 - 格式正确")
    void generateInstanceNo_shouldReturnCorrectFormat() {
        String instanceNo = workflowEngineManager.generateInstanceNo();

        assertNotNull(instanceNo);
        assertTrue(instanceNo.startsWith("WF"));
        assertEquals(18, instanceNo.length()); // WF + 12位日期时间 + 4位序列号
    }

    @Test
    @DisplayName("生成实例编号 - 唯一性")
    void generateInstanceNo_shouldBeUnique() {
        String no1 = workflowEngineManager.generateInstanceNo();
        String no2 = workflowEngineManager.generateInstanceNo();

        assertNotEquals(no1, no2);
    }

    @Test
    @DisplayName("解析流程定义 - 无效JSON返回null")
    void parseDefinition_invalidJson_shouldReturnNull() {
        String invalidJson = "invalid json";

        WorkflowEngineManager.WorkflowDefinition result = workflowEngineManager.parseDefinition(invalidJson);

        assertNull(result);
    }

    @Test
    @DisplayName("解析流程定义 - 有效JSON")
    void parseDefinition_validJson_shouldReturnDefinition() {
        String validJson = """
            {
                "id": "def001",
                "name": "请假审批",
                "nodes": [
                    {"id": "start", "name": "开始", "type": "START"},
                    {"id": "approval1", "name": "主管审批", "type": "APPROVAL", "assigneeType": "FIXED", "assigneeIds": [1]},
                    {"id": "end", "name": "结束", "type": "END"}
                ],
                "transitions": [
                    {"id": "t1", "fromNodeId": "start", "toNodeId": "approval1"},
                    {"id": "t2", "fromNodeId": "approval1", "toNodeId": "end", "condition": "APPROVED"}
                ]
            }
            """;

        WorkflowEngineManager.WorkflowDefinition result = workflowEngineManager.parseDefinition(validJson);

        assertNotNull(result);
        assertEquals("def001", result.getId());
        assertEquals("请假审批", result.getName());
        assertEquals(3, result.getNodes().size());
        assertEquals(2, result.getTransitions().size());
    }

    @Test
    @DisplayName("获取开始节点")
    void getStartNode_shouldReturnStartNode() {
        WorkflowEngineManager.WorkflowDefinition definition = createTestDefinition();

        WorkflowEngineManager.WorkflowNode startNode = workflowEngineManager.getStartNode(definition);

        assertNotNull(startNode);
        assertEquals("START", startNode.getType());
        assertEquals("start", startNode.getId());
    }

    @Test
    @DisplayName("获取下一个节点 - 无条件转移")
    void getNextNode_noCondition_shouldReturnNextNode() {
        WorkflowEngineManager.WorkflowDefinition definition = createTestDefinition();

        WorkflowEngineManager.WorkflowNode nextNode = workflowEngineManager.getNextNode(definition, "start", null);

        assertNotNull(nextNode);
        assertEquals("approval1", nextNode.getId());
    }

    @Test
    @DisplayName("获取下一个节点 - 有条件转移")
    void getNextNode_withCondition_shouldReturnCorrectNode() {
        WorkflowEngineManager.WorkflowDefinition definition = createTestDefinition();

        WorkflowEngineManager.WorkflowNode nextNode = workflowEngineManager.getNextNode(definition, "approval1", "APPROVED");

        assertNotNull(nextNode);
        assertEquals("end", nextNode.getId());
    }

    @Test
    @DisplayName("判断结束节点")
    void isEndNode_shouldReturnCorrectResult() {
        WorkflowEngineManager.WorkflowNode endNode = new WorkflowEngineManager.WorkflowNode();
        endNode.setType("END");

        WorkflowEngineManager.WorkflowNode approvalNode = new WorkflowEngineManager.WorkflowNode();
        approvalNode.setType("APPROVAL");

        assertTrue(workflowEngineManager.isEndNode(endNode));
        assertFalse(workflowEngineManager.isEndNode(approvalNode));
        assertFalse(workflowEngineManager.isEndNode(null));
    }

    @Test
    @DisplayName("会签完成检查 - 全部通过")
    void isCountersignComplete_allApproved_shouldReturnTrue() {
        List<WorkflowEngineManager.TaskResult> results = List.of(
                new WorkflowEngineManager.TaskResult(1L, 1L, "APPROVED", "同意"),
                new WorkflowEngineManager.TaskResult(2L, 2L, "APPROVED", "同意"),
                new WorkflowEngineManager.TaskResult(3L, 3L, "APPROVED", "同意")
        );

        assertTrue(workflowEngineManager.isCountersignComplete(results, 3));
    }

    @Test
    @DisplayName("会签完成检查 - 部分通过")
    void isCountersignComplete_partialApproved_shouldReturnFalse() {
        List<WorkflowEngineManager.TaskResult> results = List.of(
                new WorkflowEngineManager.TaskResult(1L, 1L, "APPROVED", "同意"),
                new WorkflowEngineManager.TaskResult(2L, 2L, "PENDING", null)
        );

        assertFalse(workflowEngineManager.isCountersignComplete(results, 3));
    }

    @Test
    @DisplayName("或签完成检查 - 有人通过")
    void isOrSignComplete_oneApproved_shouldReturnTrue() {
        List<WorkflowEngineManager.TaskResult> results = List.of(
                new WorkflowEngineManager.TaskResult(1L, 1L, "APPROVED", "同意"),
                new WorkflowEngineManager.TaskResult(2L, 2L, "PENDING", null)
        );

        assertTrue(workflowEngineManager.isOrSignComplete(results));
    }

    @Test
    @DisplayName("或签完成检查 - 无人处理")
    void isOrSignComplete_noneProcessed_shouldReturnFalse() {
        List<WorkflowEngineManager.TaskResult> results = List.of(
                new WorkflowEngineManager.TaskResult(1L, 1L, "PENDING", null),
                new WorkflowEngineManager.TaskResult(2L, 2L, "PENDING", null)
        );

        assertFalse(workflowEngineManager.isOrSignComplete(results));
    }

    @Test
    @DisplayName("获取或签结果")
    void getOrSignResult_shouldReturnFirstResult() {
        List<WorkflowEngineManager.TaskResult> results = List.of(
                new WorkflowEngineManager.TaskResult(1L, 1L, "REJECTED", "不同意"),
                new WorkflowEngineManager.TaskResult(2L, 2L, "PENDING", null)
        );

        String result = workflowEngineManager.getOrSignResult(results);

        assertEquals("REJECTED", result);
    }

    private WorkflowEngineManager.WorkflowDefinition createTestDefinition() {
        WorkflowEngineManager.WorkflowDefinition definition = new WorkflowEngineManager.WorkflowDefinition();
        definition.setId("test001");
        definition.setName("测试流程");

        WorkflowEngineManager.WorkflowNode startNode = new WorkflowEngineManager.WorkflowNode();
        startNode.setId("start");
        startNode.setName("开始");
        startNode.setType("START");

        WorkflowEngineManager.WorkflowNode approvalNode = new WorkflowEngineManager.WorkflowNode();
        approvalNode.setId("approval1");
        approvalNode.setName("审批");
        approvalNode.setType("APPROVAL");
        approvalNode.setAssigneeType("FIXED");
        approvalNode.setAssigneeIds(List.of(1L));

        WorkflowEngineManager.WorkflowNode endNode = new WorkflowEngineManager.WorkflowNode();
        endNode.setId("end");
        endNode.setName("结束");
        endNode.setType("END");

        definition.setNodes(List.of(startNode, approvalNode, endNode));

        WorkflowEngineManager.WorkflowTransition t1 = new WorkflowEngineManager.WorkflowTransition();
        t1.setId("t1");
        t1.setFromNodeId("start");
        t1.setToNodeId("approval1");

        WorkflowEngineManager.WorkflowTransition t2 = new WorkflowEngineManager.WorkflowTransition();
        t2.setId("t2");
        t2.setFromNodeId("approval1");
        t2.setToNodeId("end");
        t2.setCondition("APPROVED");

        definition.setTransitions(List.of(t1, t2));

        return definition;
    }
}
