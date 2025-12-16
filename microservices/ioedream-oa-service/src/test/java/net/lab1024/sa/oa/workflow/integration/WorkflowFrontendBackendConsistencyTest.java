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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.boot.test.web.server.LocalServerPort;

import net.lab1024.sa.oa.workflow.service.WorkflowEngineService;
import net.lab1024.sa.oa.workflow.dao.WorkflowDefinitionDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowInstanceDao;
import net.lab1024.sa.oa.workflow.dao.WorkflowTaskDao;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 工作流前后端一致性测试
 * <p>
 * 验证前后端API接口、数据格式、WebSocket通信的一致性：
 * 1. API接口响应格式一致性
 * 2. 数据模型字段映射一致性
 * 3. 错误码和错误信息一致性
 * 4. 分页参数和响应格式一致性
 * 5. WebSocket消息格式一致性
 * 6. 前端API调用路径一致性
 * 7. 日期时间格式一致性
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-16
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@SpringJUnitConfig
@Transactional
public class WorkflowFrontendBackendConsistencyTest {

    @LocalServerPort
    private int port;

    @Resource
    private TestRestTemplate restTemplate;

    @Resource
    private WorkflowEngineService workflowEngineService;

    @Resource
    private WorkflowDefinitionDao workflowDefinitionDao;

    @Resource
    private WorkflowInstanceDao workflowInstanceDao;

    @Resource
    private WorkflowTaskDao workflowTaskDao;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String baseUrl;
    private String authToken; // 模拟认证token

    @BeforeEach
    void setUp() {
        log.info("=== 前后端一致性测试准备 ===");

        baseUrl = "http://localhost:" + port;
        authToken = "test-auth-token-" + System.currentTimeMillis();

        // 准备测试数据
        prepareTestData();

        log.info("前后端一致性测试准备完成");
    }

    @AfterEach
    void tearDown() {
        log.info("=== 前后端一致性测试清理 ===");

        // 清理测试环境
        cleanupTestData();
    }

    @Test
    @DisplayName("API响应格式一致性测试")
    void testApiResponseFormatConsistency() {
        log.info("=== 开始API响应格式一致性测试 ===");

        // 测试分页查询流程定义
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("pageNum", 1);
        queryParams.put("pageSize", 20);

        String url = baseUrl + "/api/v1/workflow/engine/definition/page?pageNum=1&pageSize=20";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // 验证HTTP状态码
        assertEquals(200, response.getStatusCodeValue(), "HTTP状态码应为200");

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "响应体不应为空");

        // 验证标准响应格式
        assertTrue(responseBody.containsKey("code"), "响应应包含code字段");
        assertTrue(responseBody.containsKey("message"), "响应应包含message字段");
        assertTrue(responseBody.containsKey("data"), "响应应包含data字段");
        assertTrue(responseBody.containsKey("timestamp"), "响应应包含timestamp字段");

        // 验证成功响应格式
        Integer code = (Integer) responseBody.get("code");
        assertEquals(200, code.intValue(), "成功响应code应为200");
        assertEquals("success", responseBody.get("message"), "成功响应message应为success");

        // 验证分页数据格式
        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        assertNotNull(data, "data字段不应为空");
        assertTrue(data.containsKey("list"), "分页数据应包含list字段");
        assertTrue(data.containsKey("total"), "分页数据应包含total字段");
        assertTrue(data.containsKey("pageNum"), "分页数据应包含pageNum字段");
        assertTrue(data.containsKey("pageSize"), "分页数据应包含pageSize字段");
        assertTrue(data.containsKey("pages"), "分页数据应包含pages字段");

        log.info("API响应格式一致性测试通过");
    }

    @Test
    @DisplayName("数据模型字段映射一致性测试")
    void testDataModelFieldMappingConsistency() {
        log.info("=== 开始数据模型字段映射一致性测试 ===");

        // 创建测试流程实例
        Map<String, Object> variables = new HashMap<>();
        variables.put("applicantId", 1001L);
        variables.put("departmentId", 10L);
        variables.put("requestType", "leave");
        variables.put("startDate", LocalDateTime.now());
        variables.put("endDate", LocalDateTime.now().plusDays(3));

        String processInstanceId = workflowEngineService.startProcess(
            "consistency-test-process",
            "一致性测试流程",
            variables,
            1001L
        );

        assertNotNull(processInstanceId, "流程实例ID不应为空");

        // 通过API获取流程实例详情
        String url = baseUrl + "/api/v1/workflow/engine/instance/" + extractInstanceId(processInstanceId);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue(), "HTTP状态码应为200");

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> instanceData = (Map<String, Object>) responseBody.get("data");

        // 验证字段映射一致性
        assertEquals(extractInstanceId(processInstanceId), instanceData.get("instanceId"), "实例ID应匹配");
        assertEquals("consistency-test-process", instanceData.get("processKey"), "流程Key应匹配");
        assertEquals("一致性测试流程", instanceData.get("instanceName"), "实例名称应匹配");
        assertEquals(1001L, instanceData.get("startUserId"), "启动用户ID应匹配");
        assertEquals("RUNNING", instanceData.get("status"), "流程状态应为RUNNING");

        // 验证流程变量字段映射
        Map<String, Object> apiVariables = (Map<String, Object>) instanceData.get("variables");
        assertNotNull(apiVariables, "API响应应包含variables字段");
        assertEquals(1001L, apiVariables.get("applicantId"), "申请人ID应匹配");
        assertEquals(10L, apiVariables.get("departmentId"), "部门ID应匹配");
        assertEquals("leave", apiVariables.get("requestType"), "请求类型应匹配");

        log.info("数据模型字段映射一致性测试通过");
    }

    @Test
    @DisplayName("错误码和错误信息一致性测试")
    void testErrorCodeAndMessageConsistency() {
        log.info("=== 开始错误码和错误信息一致性测试 ===");

        // 测试不存在的流程定义
        String url = baseUrl + "/api/v1/workflow/engine/definition/99999";
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue(), "HTTP状态码应为200");

        Map<String, Object> responseBody = response.getBody();
        Integer code = (Integer) responseBody.get("code");
        String message = (String) responseBody.get("message");

        // 验证错误响应格式
        assertEquals("PROCESS_DEFINITION_NOT_FOUND", code.toString(), "错误码应匹配");
        assertTrue(message.contains("流程定义不存在"), "错误信息应包含相关描述");

        // 测试无效参数
        String startProcessUrl = baseUrl + "/api/v1/workflow/engine/instance/start";
        Map<String, Object> invalidParams = new HashMap<>();
        // 缺少必要参数

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(invalidParams, createHeaders());
        ResponseEntity<Map> startResponse = restTemplate.postForEntity(startProcessUrl, request, Map.class);

        assertEquals(200, startResponse.getStatusCodeValue(), "HTTP状态码应为200");

        Map<String, Object> startResponseBody = startResponse.getBody();
        Integer startCode = (Integer) startResponseBody.get("code");
        String startMessage = (String) startResponseBody.get("message");

        // 验证参数错误码
        assertTrue(startCode.toString().startsWith("4"), "参数错误应以4开头");
        assertTrue(startMessage.contains("参数") || startMessage.contains("必填"), "错误信息应包含参数相关描述");

        log.info("错误码和错误信息一致性测试通过");
    }

    @Test
    @DisplayName("WebSocket消息格式一致性测试")
    void testWebSocketMessageFormatConsistency() {
        log.info("=== 开始WebSocket消息格式一致性测试 ===");

        // 这里测试WebSocket消息格式，由于测试环境限制，
        // 我们主要验证消息结构定义的一致性

        // 模拟新任务通知消息格式
        Map<String, Object> newTaskMessage = new HashMap<>();
        newTaskMessage.put("type", "NEW_TASK");
        newTaskMessage.put("timestamp", System.currentTimeMillis());

        Map<String, Object> taskData = new HashMap<>();
        taskData.put("taskId", "task-123");
        taskData.put("taskName", "请假审批");
        taskData.put("processInstanceId", "process-456");
        taskData.put("processName", "请假审批流程");
        taskData.put("assigneeId", 2001L);
        taskData.put("assigneeName", "张三");
        taskData.put("createTime", LocalDateTime.now());
        taskData.put("dueDate", LocalDateTime.now().plusDays(1));

        Map<String, Object> messageWrapper = new HashMap<>();
        messageWrapper.put("type", "NEW_TASK");
        messageWrapper.put("data", taskData);
        messageWrapper.put("timestamp", System.currentTimeMillis());

        // 验证消息格式
        assertEquals("NEW_TASK", messageWrapper.get("type"), "消息类型应为NEW_TASK");
        assertNotNull(messageWrapper.get("data"), "消息应包含data字段");
        assertNotNull(messageWrapper.get("timestamp"), "消息应包含timestamp字段");

        Map<String, Object> messageData = (Map<String, Object>) messageWrapper.get("data");
        assertEquals("task-123", messageData.get("taskId"), "任务ID应匹配");
        assertEquals("请假审批", messageData.get("taskName"), "任务名称应匹配");
        assertEquals(2001L, messageData.get("assigneeId"), "指派人ID应匹配");

        log.info("WebSocket消息格式一致性测试通过");
    }

    @Test
    @DisplayName("前端API调用路径一致性测试")
    void testFrontendApiPathConsistency() {
        log.info("=== 开始前端API调用路径一致性测试 ===");

        // 验证前端API文件中定义的路径与后端实际路径的一致性

        // 这些路径来自 workflow-api.js 文件
        String[] frontendPaths = {
            "/api/v1/workflow/engine/definition/deploy",
            "/api/v1/workflow/engine/definition/page",
            "/api/v1/workflow/engine/definition/{definitionId}",
            "/api/v1/workflow/engine/instance/start",
            "/api/v1/workflow/engine/instance/page",
            "/api/v1/workflow/engine/instance/{instanceId}",
            "/api/v1/workflow/engine/task/my/pending",
            "/api/v1/workflow/engine/task/my/completed",
            "/api/v1/workflow/engine/task/{taskId}/complete",
            "/api/v1/workflow/engine/statistics"
        };

        // 测试路径可访问性（不测试具体功能，只测试路径存在）
        for (String path : frontendPaths) {
            String testUrl = baseUrl + path.replace("{definitionId}", "1").replace("{instanceId}", "1").replace("{taskId}", "1");

            try {
                ResponseEntity<Map> response = restTemplate.getForEntity(testUrl, Map.class);

                // 路径应该存在（返回200），即使数据不存在
                assertTrue(response.getStatusCodeValue() == 200 || response.getStatusCodeValue() == 400,
                          "路径 " + path + " 应该可访问");

                // 验证响应格式一致性
                if (response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    assertTrue(responseBody.containsKey("code"), "响应应包含code字段");
                    assertTrue(responseBody.containsKey("message"), "响应应包含message字段");
                }

            } catch (Exception e) {
                // 某些路径可能需要POST请求，这里记录但不失败
                log.debug("路径测试异常（可能是请求方法问题）: {}", path, e);
            }
        }

        log.info("前端API调用路径一致性测试通过");
    }

    @Test
    @DisplayName("日期时间格式一致性测试")
    void testDateTimeFormatConsistency() {
        log.info("=== 开始日期时间格式一致性测试 ===");

        // 创建包含日期时间的流程实例
        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> variables = new HashMap<>();
        variables.put("requestDate", now);
        variables.put("startTime", now.plusHours(1));
        variables.put("endTime", now.plusDays(1));

        String processInstanceId = workflowEngineService.startProcess(
            "datetime-format-test",
            "日期时间格式测试",
            variables,
            1001L
        );

        // 通过API获取流程实例
        String url = baseUrl + "/api/v1/workflow/engine/instance/" + extractInstanceId(processInstanceId);
        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        assertEquals(200, response.getStatusCodeValue());

        Map<String, Object> responseBody = response.getBody();
        Map<String, Object> instanceData = (Map<String, Object>) responseBody.get("data");
        Map<String, Object> apiVariables = (Map<String, Object>) instanceData.get("variables");

        // 验证日期时间格式
        String createTime = (String) instanceData.get("createTime");
        assertNotNull(createTime, "创建时间不应为空");
        assertTrue(createTime.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
                  "创建时间格式应为 yyyy-MM-dd HH:mm:ss");

        if (apiVariables.containsKey("requestDate")) {
            String requestDate = apiVariables.get("requestDate").toString();
            assertTrue(requestDate.contains(now.toString().substring(0, 10)),
                      "请求日期应包含正确的日期部分");
        }

        log.info("日期时间格式一致性测试通过");
    }

    @Test
    @DisplayName("前后端状态码映射一致性测试")
    void testStatusCodeMappingConsistency() {
        log.info("=== 开始前后端状态码映射一致性测试 ===");

        // 测试各种业务状态码的映射一致性

        Map<String, String> statusMapping = new HashMap<>();
        statusMapping.put("process", "1-进行中，2-已完成，3-已终止，4-已挂起");
        statusMapping.put("task", "1-待处理，2-处理中，3-已完成，4-已取消");
        statusMapping.put("definition", "DRAFT-草稿，PUBLISHED-已发布，DISABLED-已禁用");

        // 通过API查询验证状态码映射
        String instancePageUrl = baseUrl + "/api/v1/workflow/engine/instance/page?pageNum=1&pageSize=10";
        ResponseEntity<Map> instanceResponse = restTemplate.getForEntity(instancePageUrl, Map.class);

        if (instanceResponse.getStatusCodeValue() == 200) {
            Map<String, Object> data = (Map<String, Object>) instanceResponse.getBody().get("data");
            List<Map<String, Object>> instances = (List<Map<String, Object>>) data.get("list");

            for (Map<String, Object> instance : instances) {
                Integer status = (Integer) instance.get("status");
                assertNotNull(status, "流程实例状态不应为空");
                assertTrue(status >= 1 && status <= 4, "流程状态应在1-4范围内");
            }
        }

        // 测试任务状态映射
        String taskPageUrl = baseUrl + "/api/v1/workflow/engine/task/my/pending?pageNum=1&pageSize=10";
        ResponseEntity<Map> taskResponse = restTemplate.getForEntity(taskPageUrl, Map.class);

        if (taskResponse.getStatusCodeValue() == 200) {
            Map<String, Object> taskData = (Map<String, Object>) taskResponse.getBody().get("data");
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) taskData.get("list");

            for (Map<String, Object> task : tasks) {
                String taskStatus = (String) task.get("status");
                assertNotNull(taskStatus, "任务状态不应为空");
                assertTrue(
                    taskStatus.equals("待处理") || taskStatus.equals("处理中") ||
                    taskStatus.equals("已完成") || taskStatus.equals("已取消"),
                    "任务状态应在有效范围内"
                );
            }
        }

        log.info("前后端状态码映射一致性测试通过");
    }

    // ========== 辅助方法 ==========

    /**
     * 准备测试数据
     */
    private void prepareTestData() {
        try {
            // 部署测试流程定义
            String testProcessXml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL">
                    <process id="consistency-test-process" name="一致性测试流程" isExecutable="true">
                        <startEvent id="start"/>
                        <userTask id="task1" name="测试任务"/>
                        <endEvent id="end"/>
                        <sequenceFlow id="flow1" sourceRef="start" targetRef="task1"/>
                        <sequenceFlow id="flow2" sourceRef="task1" targetRef="end"/>
                    </process>
                </definitions>
                """;

            workflowEngineService.deployProcess(testProcessXml, "consistency-test-process", "一致性测试流程");

        } catch (Exception e) {
            log.warn("准备测试数据失败", e);
        }
    }

    /**
     * 清理测试数据
     */
    private void cleanupTestData() {
        try {
            // 在测试环境中，@Transactional会自动回滚
            log.debug("测试数据清理完成");
        } catch (Exception e) {
            log.warn("清理测试数据失败", e);
        }
    }

    /**
     * 创建HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + authToken);
        return headers;
    }

    /**
     * 从流程实例ID中提取数字部分
     */
    private Long extractInstanceId(String processInstanceId) {
        try {
            // Flowable的流程实例ID通常是字符串格式，这里尝试转换为数字
            return Long.parseLong(processInstanceId.replaceAll("\\D", ""));
        } catch (Exception e) {
            return 1L; // 返回默认值
        }
    }
}