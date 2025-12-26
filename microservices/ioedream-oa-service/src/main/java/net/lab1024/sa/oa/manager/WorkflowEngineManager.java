package net.lab1024.sa.oa.manager;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.common.auth.domain.vo.UserInfoVO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import net.lab1024.sa.common.gateway.domain.response.UserInfoResponse;
import net.lab1024.sa.common.system.employee.domain.vo.EmployeeVO;

/**
 * 工作流引擎管理器
 * <p>
 * 负责工作流的核心执行逻辑
 * 严格遵循CLAUDE.md规范：
 * - Manager类通过构造函数注入依赖，保持为纯Java类
 * - 不使用Spring注解
 * - 通过GatewayServiceClient调用公共服务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
public class WorkflowEngineManager {


    private final ObjectMapper objectMapper;
    private final AtomicLong instanceSequence = new AtomicLong(0);
    private final GatewayServiceClient gatewayServiceClient;

    /**
     * 构造函数注入依赖
     * <p>
     * 符合CLAUDE.md规范：Manager类通过构造函数接收依赖，保持为纯Java类
     * </p>
     *
     * @param gatewayServiceClient 网关服务客户端
     * @param objectMapper         ObjectMapper（由Spring统一提供，避免重复创建）
     */
    public WorkflowEngineManager(GatewayServiceClient gatewayServiceClient, ObjectMapper objectMapper) {
        this.gatewayServiceClient = gatewayServiceClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 生成实例编号
     */
    public String generateInstanceNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        long seq = instanceSequence.incrementAndGet() % 10000;
        return String.format("WF%s%04d", dateStr, seq);
    }

    /**
     * 解析流程定义
     */
    public WorkflowEngineManager.WorkflowDefinition parseDefinition(String definitionJson) {
        try {
            return objectMapper.readValue(definitionJson, WorkflowEngineManager.WorkflowDefinition.class);
        } catch (Exception e) {
            log.error("[工作流引擎] 解析流程定义失败", e);
            return null;
        }
    }

    /**
     * 获取开始节点
     */
    public WorkflowNode getStartNode(WorkflowEngineManager.WorkflowDefinition definition) {
        return definition.getNodes().stream()
                .filter(n -> "START".equals(n.getType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取下一个节点
     */
    public WorkflowNode getNextNode(WorkflowDefinition definition, String currentNodeId, String action) {
        WorkflowNode currentNode = definition.getNodes().stream()
                .filter(n -> n.getId().equals(currentNodeId))
                .findFirst()
                .orElse(null);

        if (currentNode == null) {
            return null;
        }

        // 查找对应的转移
        WorkflowTransition transition = definition.getTransitions().stream()
                .filter(t -> t.getFromNodeId().equals(currentNodeId))
                .filter(t -> action == null || action.equals(t.getCondition()))
                .findFirst()
                .orElse(null);

        if (transition == null) {
            return null;
        }

        return definition.getNodes().stream()
                .filter(n -> n.getId().equals(transition.getToNodeId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 判断是否为结束节点
     */
    public boolean isEndNode(WorkflowNode node) {
        return node != null && "END".equals(node.getType());
    }

    /**
     * 获取审批人列表
     * <p>
     * 实现步骤：
     * 1. 根据节点配置的审批人类型获取审批人
     * 2. 支持：固定人员、角色、部门主管、发起人主管等
     * 3. 通过GatewayServiceClient调用公共服务获取用户信息
     * </p>
     *
     * @param node      工作流节点
     * @param variables 流程变量（包含initiatorId等）
     * @return 审批人ID列表
     */
    public List<Long> getAssignees(WorkflowNode node, Map<String, Object> variables) {
        // 根据节点配置获取审批人
        // 支持：固定人员、角色、部门主管、发起人主管等
        String assigneeType = node.getAssigneeType();

        if ("FIXED".equals(assigneeType)) {
            return node.getAssigneeIds() != null ? node.getAssigneeIds() : List.of();
        } else if ("ROLE".equals(assigneeType)) {
            // 根据角色查询用户
            return getUsersByRole(node.getAssigneeIds());
        } else if ("DEPARTMENT_LEADER".equals(assigneeType)) {
            // 根据发起人部门查询主管
            Long initiatorId = getInitiatorId(variables);
            return getDepartmentLeader(initiatorId);
        } else if ("INITIATOR_LEADER".equals(assigneeType)) {
            // 查询发起人的直接主管
            Long initiatorId = getInitiatorId(variables);
            return getInitiatorLeader(initiatorId);
        }

        return List.of();
    }

    /**
     * 根据角色查询用户
     * <p>
     * 实现步骤：
     * 1. 从节点配置中获取角色ID列表
     * 2. 通过GatewayServiceClient调用公共服务查询每个角色的用户
     * 3. 合并并去重用户ID列表
     * </p>
     *
     * @param roleIds 角色ID列表
     * @return 用户ID列表
     */
    private List<Long> getUsersByRole(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            log.warn("[工作流引擎] 角色ID列表为空，无法查询用户");
            return List.of();
        }

        List<Long> userIds = new ArrayList<>();
        for (Long roleId : roleIds) {
            try {
                // 通过GatewayServiceClient调用公共服务查询角色用户
                // 注意：API路径需要根据实际公共服务接口调整
                @SuppressWarnings("unchecked") ResponseDTO<List<UserInfoVO>> response = (ResponseDTO<List<UserInfoVO>>) gatewayServiceClient.callCommonService(
                        "/api/v1/role/" + roleId + "/users",
                        HttpMethod.GET,
                        null,
                        new TypeReference<ResponseDTO<List<UserInfoVO>>>() {
                        });

                if (response != null && response.isSuccess() && response.getData() != null) {
                    List<UserInfoVO> users = response.getData();
                    for (UserInfoVO user : users) {
                        if (user.getUserId() != null) {
                            userIds.add(user.getUserId());
                        }
                    }
                    log.debug("[工作流引擎] 根据角色查询用户成功，roleId={}, userCount={}", roleId, users.size());
                } else {
                    log.warn("[工作流引擎] 根据角色查询用户失败，roleId={}, message={}",
                            roleId, response != null ? response.getMessage() : "响应为空");
                }
            } catch (Exception e) {
                log.error("[工作流引擎] 根据角色查询用户异常，roleId={}, error={}", roleId, e.getMessage(), e);
            }
        }

        // 去重
        return userIds.stream().distinct().toList();
    }

    /**
     * 根据发起人部门查询主管
     * <p>
     * 实现步骤：
     * 1. 获取发起人用户详情
     * 2. 通过员工信息获取部门ID
     * 3. 查询部门详情获取leaderId（部门负责人）
     * </p>
     *
     * @param initiatorId 发起人ID
     * @return 部门主管ID列表
     */
    private List<Long> getDepartmentLeader(Long initiatorId) {
        if (initiatorId == null) {
            log.warn("[工作流引擎] 发起人ID为空，无法查询部门主管");
            return List.of();
        }

        try {
            // 1. 验证发起人是否存在
            ResponseDTO<UserInfoResponse> userResponse = gatewayServiceClient.callCommonService(
                    "/open/api/v1/users/" + initiatorId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<UserInfoResponse>>() {});

            if (userResponse == null || !userResponse.isSuccess() || userResponse.getData() == null) {
                log.warn("[工作流引擎] 获取发起人信息失败，initiatorId={}", initiatorId);
                return List.of();
            }

            // 2. 通过员工信息获取部门ID
            Long departmentId = getDepartmentIdByUserId(initiatorId);
            if (departmentId == null) {
                log.warn("[工作流引擎] 发起人没有关联部门，initiatorId={}", initiatorId);
                return List.of();
            }

            // 3. 查询部门详情获取leaderId（部门负责人）
            @SuppressWarnings("unchecked")
            ResponseDTO<Map<String, Object>> deptResponse = (ResponseDTO<Map<String, Object>>) gatewayServiceClient.callCommonService(
                    "/api/v1/department/" + departmentId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<Map<String, Object>>>() {
                    });

            if (deptResponse != null && deptResponse.isSuccess() && deptResponse.getData() != null) {
                Map<String, Object> department = deptResponse.getData();
                Object leaderIdObj = department.get("leaderId");
                if (leaderIdObj != null) {
                    Long leaderId = leaderIdObj instanceof Number ? ((Number) leaderIdObj).longValue()
                            : Long.parseLong(leaderIdObj.toString());
                    log.debug("[工作流引擎] 根据部门查询主管成功，departmentId={}, leaderId={}", departmentId, leaderId);
                    return List.of(leaderId);
                } else {
                    log.warn("[工作流引擎] 部门没有负责人，departmentId={}", departmentId);
                }
            } else {
                log.warn("[工作流引擎] 查询部门详情失败，departmentId={}", departmentId);
            }
        } catch (Exception e) {
            log.error("[工作流引擎] 根据部门查询主管异常，initiatorId={}, error={}", initiatorId, e.getMessage(), e);
        }

        return List.of();
    }

    /**
     * 查询发起人的直接主管
     * <p>
     * 实现步骤：
     * 1. 通过用户ID查询员工信息
     * 2. 获取员工的supervisorId（直属上级ID）
     * </p>
     *
     * @param initiatorId 发起人ID
     * @return 直接主管ID列表
     */
    private List<Long> getInitiatorLeader(Long initiatorId) {
        if (initiatorId == null) {
            log.warn("[工作流引擎] 发起人ID为空，无法查询直接主管");
            return List.of();
        }

        try {
            // 通过用户ID查询员工信息
            // 注意：需要通过员工服务查询，因为员工表中有supervisorId字段
            @SuppressWarnings("unchecked")
            ResponseDTO<List<Map<String, Object>>> employeeResponse = (ResponseDTO<List<Map<String, Object>>>) gatewayServiceClient.callCommonService(
                    "/api/v1/system/employee/user/" + initiatorId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<Map<String, Object>>>>() {
                    });

            if (employeeResponse != null && employeeResponse.isSuccess() &&
                    employeeResponse.getData() != null && !employeeResponse.getData().isEmpty()) {
                Map<String, Object> employee = employeeResponse.getData().get(0);
                Object supervisorIdObj = employee.get("supervisorId");
                Long supervisorId = null;
                if (supervisorIdObj instanceof Number number) {
                    supervisorId = number.longValue();
                } else if (supervisorIdObj != null) {
                    supervisorId = Long.parseLong(String.valueOf(supervisorIdObj));
                }

                if (supervisorId != null) {
                    log.debug("[工作流引擎] 查询发起人直接主管成功，initiatorId={}, supervisorId={}",
                            initiatorId, supervisorId);
                    return List.of(supervisorId);
                } else {
                    log.warn("[工作流引擎] 发起人没有直属上级，initiatorId={}", initiatorId);
                }
            } else {
                // 如果员工服务接口不存在，尝试通过用户详情获取
                log.debug("[工作流引擎] 员工服务接口不可用，尝试通过其他方式获取");
            }
        } catch (Exception e) {
            log.error("[工作流引擎] 查询发起人直接主管异常，initiatorId={}, error={}",
                    initiatorId, e.getMessage(), e);
        }

        return List.of();
    }

    /**
     * 通过用户ID获取部门ID
     * <p>
     * 实现步骤：
     * 1. 通过用户ID查询员工信息
     * 2. 从员工信息中获取departmentId
     * </p>
     *
     * @param userId 用户ID
     * @return 部门ID
     */
    private Long getDepartmentIdByUserId(Long userId) {
        if (userId == null) {
            return null;
        }

        try {
            // 通过用户ID查询员工信息
            @SuppressWarnings("unchecked")
            ResponseDTO<List<EmployeeVO>> employeeResponse = (ResponseDTO<List<EmployeeVO>>) gatewayServiceClient.callCommonService(
                    "/api/v1/system/employee/user/" + userId,
                    HttpMethod.GET,
                    null,
                    new TypeReference<ResponseDTO<List<EmployeeVO>>>() {
                    });

            if (employeeResponse != null && employeeResponse.isSuccess() &&
                    employeeResponse.getData() != null && !employeeResponse.getData().isEmpty()) {
                EmployeeVO employee = employeeResponse.getData().get(0);
                return employee.getDepartmentId();
            }
        } catch (Exception e) {
            log.debug("[工作流引擎] 通过用户ID获取部门ID失败，userId={}, error={}", userId, e.getMessage());
        }

        return null;
    }

    /**
     * 从流程变量中获取发起人ID
     *
     * @param variables 流程变量
     * @return 发起人ID
     */
    private Long getInitiatorId(Map<String, Object> variables) {
        if (variables == null) {
            return null;
        }

        Object initiatorIdObj = variables.get("initiatorId");
        if (initiatorIdObj == null) {
            return null;
        }

        if (initiatorIdObj instanceof Number) {
            return ((Number) initiatorIdObj).longValue();
        } else if (initiatorIdObj instanceof String) {
            try {
                return Long.parseLong((String) initiatorIdObj);
            } catch (NumberFormatException e) {
                log.warn("[工作流引擎] 发起人ID格式错误，initiatorId={}", initiatorIdObj);
                return null;
            }
        }

        return null;
    }

    /**
     * 检查会签是否完成
     */
    public boolean isCountersignComplete(List<TaskResult> taskResults, int totalCount) {
        // 会签：所有人都通过才算通过
        long approvedCount = taskResults.stream()
                .filter(r -> "APPROVED".equals(r.status()))
                .count();
        return approvedCount == totalCount;
    }

    /**
     * 检查或签是否完成
     */
    public boolean isOrSignComplete(List<TaskResult> taskResults) {
        // 或签：任一人通过即可
        return taskResults.stream()
                .anyMatch(r -> "APPROVED".equals(r.status()) || "REJECTED".equals(r.status()));
    }

    /**
     * 获取或签结果
     */
    public String getOrSignResult(List<TaskResult> taskResults) {
        return taskResults.stream()
                .filter(r -> "APPROVED".equals(r.status()) || "REJECTED".equals(r.status()))
                .map(TaskResult::status)
                .findFirst()
                .orElse(null);
    }

    // 内部类
    public static class WorkflowDefinition {
        private String id;
        private String name;
        private List<WorkflowNode> nodes;
        private List<WorkflowTransition> transitions;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<WorkflowNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<WorkflowNode> nodes) {
            this.nodes = nodes;
        }

        public List<WorkflowTransition> getTransitions() {
            return transitions;
        }

        public void setTransitions(List<WorkflowTransition> transitions) {
            this.transitions = transitions;
        }
    }

    public static class WorkflowNode {
        private String id;
        private String name;
        private String type; // START, APPROVAL, COUNTERSIGN, OR_SIGN, END
        private String assigneeType; // FIXED, ROLE, DEPARTMENT_LEADER, INITIATOR_LEADER
        private List<Long> assigneeIds;
        private Integer timeoutHours;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAssigneeType() {
            return assigneeType;
        }

        public void setAssigneeType(String assigneeType) {
            this.assigneeType = assigneeType;
        }

        public List<Long> getAssigneeIds() {
            return assigneeIds;
        }

        public void setAssigneeIds(List<Long> assigneeIds) {
            this.assigneeIds = assigneeIds;
        }

        public Integer getTimeoutHours() {
            return timeoutHours;
        }

        public void setTimeoutHours(Integer timeoutHours) {
            this.timeoutHours = timeoutHours;
        }
    }

    public static class WorkflowTransition {
        private String id;
        private String fromNodeId;
        private String toNodeId;
        private String condition; // APPROVED, REJECTED, null(默认)

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFromNodeId() {
            return fromNodeId;
        }

        public void setFromNodeId(String fromNodeId) {
            this.fromNodeId = fromNodeId;
        }

        public String getToNodeId() {
            return toNodeId;
        }

        public void setToNodeId(String toNodeId) {
            this.toNodeId = toNodeId;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }
    }

    public record TaskResult(Long taskId, Long assigneeId, String status, String comment) {
    }
}
