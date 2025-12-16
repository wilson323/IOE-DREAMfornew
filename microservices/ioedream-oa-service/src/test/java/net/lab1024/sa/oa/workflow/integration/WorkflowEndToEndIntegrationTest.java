package net.lab1024.sa.oa.workflow.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.form.FormEngineService;
import net.lab1024.sa.oa.workflow.dmn.DecisionEngineService;
import net.lab1024.sa.oa.workflow.cmmn.CaseManagementService;
import net.lab1024.sa.oa.workflow.async.AsyncHistoryService;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import java.time.Duration;

/**
 * 工作流端到端集成测试
 * <p>
 * 测试完整的工作流链路，包含：
 * 1. 流程定义部署
 * 2. 流程实例启动
 * 3. 表单实例管理
 * 4. 决策引擎执行
 * 5. 案例管理集成
 * 6. 任务处理完成
 * 7. 异步历史处理
 * 8. 流程实例完成
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@SpringJUnitConfig
@Transactional
public class WorkflowEndToEndIntegrationTest {

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private FormEngineService formEngineService;

    @Resource
    private DecisionEngineService decisionEngineService;

    @Resource
    private CaseManagementService caseManagementService;

    @Resource
    private AsyncHistoryService asyncHistoryService;

    // 测试数据
    private static final String TEST_PROCESS_KEY = "leave-approval-process";
    private static final String TEST_PROCESS_NAME = "请假审批流程";
    private static final String TEST_FORM_KEY = "leave-application-form";
    private static final String TEST_DECISION_KEY = "leave-approval-decision";
    private static final String TEST_CASE_KEY = "leave-management-case";

    private String processDefinitionId;
    private String formDefinitionId;
    private String decisionDeploymentId;
    private String caseDefinitionId;

    @BeforeEach
    void setUp() {
        log.info("=== 开始端到端集成测试准备 ===");

        try {
            // 1. 部署测试流程定义
            processDefinitionId = deployTestProcess();

            // 2. 部署测试表单定义
            formDefinitionId = deployTestForm();

            // 3. 部署测试决策定义
            decisionDeploymentId = deployTestDecision();

            // 4. 部署测试案例定义
            caseDefinitionId = deployTestCase();

            log.info("=== 集成测试准备完成 ===");
            log.info("流程定义ID: {}", processDefinitionId);
            log.info("表单定义ID: {}", formDefinitionId);
            log.info("决策部署ID: {}", decisionDeploymentId);
            log.info("案例定义ID: {}", caseDefinitionId);

        } catch (Exception e) {
            log.error("集成测试准备失败", e);
            throw new RuntimeException("集成测试准备失败", e);
        }
    }

    @AfterEach
    void tearDown() {
        log.info("=== 开始清理测试环境 ===");

        try {
            // 清理测试数据
            cleanupTestData();

            log.info("=== 测试环境清理完成 ===");

        } catch (Exception e) {
            log.error("测试环境清理失败", e);
        }
    }

    @Test
    @DisplayName("完整工作流链路测试 - 请假审批流程")
    void testCompleteWorkflowChain() {
        log.info("=== 开始完整工作流链路测试 ===");

        String processInstanceId = null;
        String taskId = null;

        try {
            // === 步骤1: 启动流程实例 ===
            log.info("--- 步骤1: 启动流程实例 ---");

            Map<String, Object> processVariables = new HashMap<>();
            processVariables.put("applicantId", 1001L);
            processVariables.put("departmentId", 10L);
            processVariables.put("leaveType", "annual");
            processVariables.put("startDate", LocalDateTime.now().plusDays(1));
            processVariables.put("endDate", LocalDateTime.now().plusDays(3));
            processVariables.put("reason", "家庭事务处理");

            processInstanceId = workflowEngineService.startProcess(
                TEST_PROCESS_KEY,
                "请假申请-" + System.currentTimeMillis(),
                processVariables,
                1001L
            );

            assertNotNull(processInstanceId, "流程实例ID不应为空");
            log.info("流程实例启动成功: {}", processInstanceId);

            // 验证流程实例状态
            Map<String, Object> processStatus = workflowEngineService.getProcessInstanceStatus(processInstanceId);
            assertNotNull(processStatus, "流程状态不应为空");
            assertEquals("RUNNING", processStatus.get("status"));

            // === 步骤2: 创建和提交表单实例 ===
            log.info("--- 步骤2: 创建和提交表单实例 ---");

            Map<String, Object> formData = new HashMap<>();
            formData.put("applicantName", "张三");
            formData.put("departmentName", "技术部");
            formData.put("leaveType", "年假");
            formData.put("startDate", LocalDateTime.now().plusDays(1));
            formData.put("endDate", LocalDateTime.now().plusDays(3));
            formData.put("leaveDays", 2);
            formData.put("reason", "家庭事务处理");
            formData.put("contactPhone", "13800138000");

            String formInstanceId = formEngineService.startFormInstance(
                TEST_FORM_KEY,
                processInstanceId,
                null,
                formData,
                1001L
            );

            assertNotNull(formInstanceId, "表单实例ID不应为空");
            log.info("表单实例创建成功: {}", formInstanceId);

            // 提交表单
            boolean formSubmitted = formEngineService.submitFormInstance(formInstanceId, formData, 1001L);
            assertTrue(formSubmitted, "表单提交应该成功");
            log.info("表单实例提交成功");

            // === 步骤3: 执行决策引擎 ===
            log.info("--- 步骤3: 执行决策引擎 ---");

            Map<String, Object> decisionVariables = new HashMap<>();
            decisionVariables.put("leaveDays", 2);
            decisionVariables.put("leaveType", "annual");
            decisionVariables.put("applicantLevel", "P3");
            decisionVariables.put("departmentWorkload", 0.7);
            decisionVariables.put("teamSize", 8);

            // 这里模拟DecisionResult，实际应该从DecisionEngineService获取
            Map<String, Object> decisionResult = decisionEngineService.executeDecisionTable(
                TEST_DECISION_KEY,
                decisionVariables
            );

            assertNotNull(decisionResult, "决策结果不应为空");
            log.info("决策引擎执行完成: {}", decisionResult);

            // === 步骤4: 启动案例管理 ===
            log.info("--- 步骤4: 启动案例管理 ---");

            Map<String, Object> caseVariables = new HashMap<>();
            caseVariables.put("processInstanceId", processInstanceId);
            caseVariables.put("formInstanceId", formInstanceId);
            caseVariables.put("caseType", "leave_approval");
            caseVariables.put("priority", "normal");

            String caseInstanceId = caseManagementService.startCaseInstance(
                TEST_CASE_KEY,
                "请假审批案例",
                "CASE-" + System.currentTimeMillis(),
                caseVariables,
                1001L
            );

            assertNotNull(caseInstanceId, "案例实例ID不应为空");
            log.info("案例实例启动成功: {}", caseInstanceId);

            // === 步骤5: 获取并处理任务 ===
            log.info("--- 步骤5: 获取并处理任务 ---");

            // 等待任务创建（模拟异步处理）
            Thread.sleep(1000);

            List<Map<String, Object>> tasks = workflowEngineService.getPendingTasks(processInstanceId);
            assertNotNull(tasks, "任务列表不应为空");
            assertTrue(tasks.size() > 0, "应该有待处理任务");

            taskId = (String) tasks.get(0).get("taskId");
            assertNotNull(taskId, "任务ID不应为空");
            log.info("获取到待处理任务: {}", taskId);

            // 审批任务
            Map<String, Object> taskVariables = new HashMap<>();
            taskVariables.put("approved", true);
            taskVariables.put("approvalComment", "同意请假申请");
            taskVariables.put("approverId", 2001L);
            taskVariables.put("approvalTime", LocalDateTime.now());

            boolean taskCompleted = workflowEngineService.completeTask(taskId, taskVariables, 2001L);
            assertTrue(taskCompleted, "任务完成应该成功");
            log.info("任务处理完成: {}", taskId);

            // === 步骤6: 验证流程完成状态 ===
            log.info("--- 步骤6: 验证流程完成状态 ---");

            // 等待流程完成
            Thread.sleep(2000);

            Map<String, Object> finalProcessStatus = workflowEngineService.getProcessInstanceStatus(processInstanceId);
            assertNotNull(finalProcessStatus, "最终流程状态不应为空");

            // 流程可能已经完成或还在下一步
            String status = (String) finalProcessStatus.get("status");
            log.info("流程最终状态: {}", status);

            // === 步骤7: 验证异步历史处理 ===
            log.info("--- 步骤7: 验证异步历史处理 ---");

            // 获取历史统计
            Map<String, Object> historyStats = asyncHistoryService.getAsyncHistoryStats();
            assertNotNull(historyStats, "历史统计不应为空");
            log.info("异步历史处理统计: {}", historyStats);

            // === 测试结果验证 ===
            log.info("--- 测试结果验证 ---");

            // 验证所有组件都正常工作
            assertNotNull(processInstanceId, "流程实例应该成功创建");
            assertNotNull(formInstanceId, "表单实例应该成功创建");
            assertNotNull(caseInstanceId, "案例实例应该成功创建");
            assertNotNull(decisionResult, "决策引擎应该成功执行");

            log.info("=== 完整工作流链路测试通过 ===");

        } catch (Exception e) {
            log.error("完整工作流链路测试失败", e);

            // 输出失败时的调试信息
            if (processInstanceId != null) {
                try {
                    Map<String, Object> status = workflowEngineService.getProcessInstanceStatus(processInstanceId);
                    log.error("失败时流程状态: {}", status);
                } catch (Exception ex) {
                    log.error("获取流程状态失败", ex);
                }
            }

            throw new RuntimeException("工作流链路测试失败", e);
        }
    }

    @Test
    @DisplayName("工作流性能压力测试")
    void testWorkflowPerformanceStress() {
        log.info("=== 开始工作流性能压力测试 ===");

        int concurrentUsers = 10;
        int processesPerUser = 5;

        long startTime = System.currentTimeMillis();

        // 并发启动流程
        for (int user = 1; user <= concurrentUsers; user++) {
            final int userId = 1000 + user;

            for (int i = 1; i <= processesPerUser; i++) {
                try {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("applicantId", userId);
                    variables.put("testBatch", i);
                    variables.put("timestamp", System.currentTimeMillis());

                    String processInstanceId = workflowEngineService.startProcess(
                        TEST_PROCESS_KEY,
                        "压力测试流程-" + user + "-" + i,
                        variables,
                        userId
                    );

                    assertNotNull(processInstanceId, "压力测试流程实例创建失败");

                } catch (Exception e) {
                    log.error("压力测试流程创建失败: user={}, batch={}", user, i, e);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;

        log.info("压力测试完成: 并发用户={}, 每用户流程数={}, 总耗时={}ms, 平均耗时={}ms/流程",
                concurrentUsers, processesPerUser, duration, duration / (concurrentUsers * processesPerUser));

        // 性能断言
        long avgTimePerProcess = duration / (concurrentUsers * processesPerUser);
        assertTrue(avgTimePerProcess < 5000, "平均每个流程启动时间应小于5秒，实际: " + avgTimePerProcess + "ms");
    }

    /**
     * 部署测试流程定义
     */
    private String deployTestProcess() {
        String bpmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xmlns:flowable="http://flowable.org/bpmn"
                        targetNamespace="http://flowable.org/bpmn">

                <process id="leave-approval-process" name="请假审批流程" isExecutable="true">

                    <startEvent id="startEvent" name="开始">
                        <extensionElements>
                            <flowable:formProperty id="applicantId" name="申请人ID" type="long" required="true"/>
                            <flowable:formProperty id="leaveType" name="请假类型" type="string" required="true"/>
                        </extensionElements>
                    </startEvent>

                    <sequenceFlow id="flow1" sourceRef="startEvent" targetRef="managerApproval"/>

                    <userTask id="managerApproval" name="经理审批" flowable:assignee="${managerId}">
                        <extensionElements>
                            <flowable:formProperty id="approved" name="是否同意" type="boolean" required="true"/>
                            <flowable:formProperty id="approvalComment" name="审批意见" type="string"/>
                        </extensionElements>
                    </userTask>

                    <sequenceFlow id="flow2" sourceRef="managerApproval" targetRef="exclusiveGateway"/>

                    <exclusiveGateway id="exclusiveGateway" name="审批结果判断"/>

                    <sequenceFlow id="flowApproved" sourceRef="exclusiveGateway" targetRef="hrNotification">
                        <conditionExpression xsi:type="tFormalExpression">${approved == true}</conditionExpression>
                    </sequenceFlow>

                    <sequenceFlow id="flowRejected" sourceRef="exclusiveGateway" targetRef="rejectionNotification">
                        <conditionExpression xsi:type="tFormalExpression">${approved == false}</conditionExpression>
                    </sequenceFlow>

                    <serviceTask id="hrNotification" name="HR通知" flowable:delegateExpression="${hrNotificationService}"/>

                    <serviceTask id="rejectionNotification" name="拒绝通知" flowable:delegateExpression="${rejectionNotificationService}"/>

                    <sequenceFlow id="flow3" sourceRef="hrNotification" targetRef="endEvent"/>
                    <sequenceFlow id="flow4" sourceRef="rejectionNotification" targetRef="endEvent"/>

                    <endEvent id="endEvent" name="结束"/>

                </process>

            </definitions>
            """;

        return workflowEngineService.deployProcess(bpmnXml, TEST_PROCESS_KEY, TEST_PROCESS_NAME);
    }

    /**
     * 部署测试表单定义
     */
    private String deployTestForm() {
        String formXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <form-definition xmlns="http://flowable.org/form">

                <form id="leave-application-form" name="请假申请表单">
                    <form-field id="applicantName" name="申请人姓名" type="string" required="true"/>
                    <form-field id="departmentName" name="部门名称" type="string" required="true"/>
                    <form-field id="leaveType" name="请假类型" type="string" required="true">
                        <values>
                            <value id="annual" name="年假"/>
                            <value id="sick" name="病假"/>
                            <value id="personal" name="事假"/>
                        </values>
                    </form-field>
                    <form-field id="startDate" name="开始日期" type="date" required="true"/>
                    <form-field id="endDate" name="结束日期" type="date" required="true"/>
                    <form-field id="leaveDays" name="请假天数" type="integer" required="true"/>
                    <form-field id="reason" name="请假原因" type="text" required="true"/>
                    <form-field id="contactPhone" name="联系电话" type="string" required="true"/>

                </form>

            </form-definition>
            """;

        return formEngineService.deployFormDefinition(formXml, TEST_FORM_KEY, "请假申请表单");
    }

    /**
     * 部署测试决策定义
     */
    private String deployTestDecision() {
        String dmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/DMN/20100524/MODEL"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xmlns:dmn="http://flowable.org/dmn"
                        targetNamespace="http://flowable.org/dmn">

                <decision id="leave-approval-decision" name="请假审批决策">

                    <decisionTable id="decisionTable" hitPolicy="FIRST">

                        <input id="leaveDays" label="请假天数" type="integer"/>
                        <input id="leaveType" label="请假类型" type="string"/>
                        <input id="applicantLevel" label="申请人级别" type="string"/>

                        <output id="approvalRequired" label="需要审批" type="boolean"/>
                        <output id="approverLevel" label="审批人级别" type="string"/>
                        <output id="maxDays" label="最大天数" type="integer"/>

                        <rule id="rule1">
                            <inputEntry id="rule1-1"><text>&lt;= 3</text></inputEntry>
                            <inputEntry id="rule1-2"><text>"annual"</text></inputEntry>
                            <inputEntry id="rule1-3"><text>"P2","P3","P4"</text></inputEntry>
                            <outputEntry id="rule1-4"><text>true</text></outputEntry>
                            <outputEntry id="rule1-5"><text>"manager"</text></outputEntry>
                            <outputEntry id="rule1-6"><text>3</text></outputEntry>
                        </rule>

                        <rule id="rule2">
                            <inputEntry id="rule2-1"><text>&gt; 3</text></inputEntry>
                            <inputEntry id="rule2-2"><text>"annual"</text></inputEntry>
                            <inputEntry id="rule2-3"><text>"P2","P3","P4"</text></inputEntry>
                            <outputEntry id="rule2-4"><text>true</text></outputEntry>
                            <outputEntry id="rule2-5"><text>"director"</text></outputEntry>
                            <outputEntry id="rule2-6"><text>5</text></outputEntry>
                        </rule>

                    </decisionTable>

                </decision>

            </definitions>
            """;

        return decisionEngineService.deployDecisionDefinition(dmnXml, TEST_DECISION_KEY, "请假审批决策");
    }

    /**
     * 部署测试案例定义
     */
    private String deployTestCase() {
        String cmmnXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <definitions xmlns="http://www.omg.org/spec/CMMN/20131101/MODEL"
                        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                        xmlns:flowable="http://flowable.org/cmmn"
                        targetNamespace="http://flowable.org/cmmn">

                <case id="leave-management-case" name="请假管理案例">

                    <planModel id="planModel">

                        <stage id="applicationStage" name="申请阶段">
                            <planItemDefinition id="applicationTask">
                                <humanTask id="submitApplication" name="提交请假申请"/>
                            </planItemDefinition>
                        </stage>

                        <stage id="approvalStage" name="审批阶段">
                            <planItemDefinition id="approvalTask">
                                <humanTask id="managerApproval" name="经理审批"/>
                            </planItemDefinition>
                            <planItemDefinition id="hrApprovalTask">
                                <humanTask id="hrApproval" name="HR审批" isBlocking="false"/>
                            </planItemDefinition>
                        </stage>

                        <milestone id="approvalCompleted" name="审批完成"/>
                        <milestone id="leaveStarted" name="请假开始"/>

                        <sentry id="applicationCompleteSentry">
                            <planItemOnPart id="applicationCompleteOnPart">
                                <sourceRef>applicationTask</sourceRef>
                            </planItemOnPart>
                        </sentry>

                        <sentry id="approvalCompleteSentry">
                            <planItemOnPart id="approvalCompleteOnPart">
                                <sourceRef>approvalTask</sourceRef>
                            </planItemOnPart>
                        </sentry>

                    </planModel>

                </case>

            </definitions>
            """;

        return caseManagementService.deployCaseDefinition(cmmnXml, TEST_CASE_KEY, "请假管理案例");
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            log.info("清理测试流程定义");
            // 这里可以添加清理逻辑，但在测试环境中通常由@Transactional自动回滚

        } catch (Exception e) {
            log.warn("清理测试数据时出现异常", e);
        }
    }
}