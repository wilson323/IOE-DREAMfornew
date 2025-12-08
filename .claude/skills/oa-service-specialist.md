# OA办公服务专家技能
## OA Service Specialist

**🎯 技能定位**: IOE-DREAM智慧园区OA办公业务专家，精通企业组织管理、工作流程、会议管理、文档协作、公告通知等核心OA办公功能

**⚡ 技能等级**: ★★★★★ (顶级专家)
**🎯 适用场景**: OA办公系统开发、流程引擎集成、文档管理、协作办公、企业信息化
**📊 技能覆盖**: 组织管理 | 工作流程 | 会议管理 | 文档协作 | 公告通知 | 考勤管理 | 审批流程

---

## 📋 技能概述

### **核心专长**
- **企业组织建模**: 部门管理、岗位管理、员工关系、组织架构树
- **工作流引擎**: Camunda BPM、流程设计、流程监控、流程优化
- **会议管理系统**: 会议室预订、会议调度、视频会议、会议纪要
- **文档协作平台**: 文档管理、版本控制、在线编辑、权限控制
- **公告通知系统**: 多渠道通知、消息推送、公告发布、阅读统计
- **审批流程集成**: 多级审批、并行审批、条件审批、审批统计

### **解决能力**
- **OA系统开发**: 完整的OA办公系统和业务流程实现
- **流程引擎集成**: 强大的工作流引擎和审批流程设计
- **企业协作优化**: 高效的企业协作和沟通平台建设
- **文档管理体系**: 完善的文档管理和知识库系统
- **审批效率提升**: 自动化审批流程和效率优化方案

---

## 🎯 业务场景覆盖

### 🏢 组织架构管理
```java
// 企业组织架构管理核心流程
@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationServiceImpl implements OrganizationService {

    @Resource
    private OrganizationManager organizationManager;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private EmployeeDao employeeDao;

    @Override
    public OrganizationTreeVO getFullOrganizationTree() {
        // 获取根部门
        List<DepartmentEntity> rootDepartments = departmentDao.selectRootDepartments();

        // 构建完整组织架构树
        return organizationManager.buildOrganizationTree(rootDepartments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createDepartment(CreateDepartmentRequestDTO request) {
        // 1. 验证上级部门
        validateParentDepartment(request.getParentId());

        // 2. 验证部门编码唯一性
        validateDepartmentCodeUnique(request.getDeptCode());

        // 3. 创建部门
        DepartmentEntity department = convertToDepartmentEntity(request);
        department.setDeptPath(generateDepartmentPath(request.getParentId()));
        department.setTreePath(generateTreePath(request.getParentId(), request.getDeptCode()));

        departmentDao.insert(department);

        // 4. 更新父部门的子部门数
        if (request.getParentId() != null) {
            organizationManager.updateParentDepartmentChildrenCount(request.getParentId());
        }

        // 5. 初始化部门岗位
        if (request.getInitPositions() != null) {
            initializeDepartmentPositions(department.getDeptId(), request.getInitPositions());
        }
    }

    @Override
    public DepartmentDetailVO getDepartmentDetail(Long deptId) {
        DepartmentEntity department = departmentDao.selectById(deptId);
        if (department == null) {
            throw new BusinessException("DEPT_NOT_FOUND", "部门不存在");
        }

        // 获取部门统计信息
        DepartmentStatistics statistics = organizationManager.getDepartmentStatistics(deptId);

        // 获取子部门列表
        List<DepartmentEntity> childDepartments = departmentDao.selectChildDepartments(deptId);

        // 获取部门员工列表
        List<EmployeeEntity> employees = employeeDao.selectByDepartmentId(deptId);

        return DepartmentDetailVO.builder()
            .department(convertToDepartmentVO(department))
            .statistics(convertToStatisticsVO(statistics))
            .childDepartments(convertToDepartmentVOList(childDepartments))
            .employees(convertToEmployeeVOList(employees))
            .build();
    }

    private void validateParentDepartment(Long parentId) {
        if (parentId != null) {
            DepartmentEntity parentDept = departmentDao.selectById(parentId);
            if (parentDept == null) {
                throw new BusinessException("PARENT_DEPT_NOT_FOUND", "上级部门不存在");
            }
            if (parentDept.getStatus() != DepartmentStatusEnum.ACTIVE.getCode()) {
                throw new BusinessException("PARENT_DEPT_INACTIVE", "上级部门已停用");
            }
        }
    }

    private void validateDepartmentCodeUnique(String deptCode) {
        if (departmentDao.existsByCode(deptCode)) {
            throw new BusinessException("DEPT_CODE_EXISTS", "部门编码已存在");
        }
    }
}
```

### 🔄 工作流程管理
```java
// Camunda工作流引擎集成
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private WorkflowManager workflowManager;

    @Resource
    private ProcessEngine processEngine;

    @Resource
    private TaskService taskService;

    @Resource
    private RuntimeService runtimeService;

    @Override
    public WorkflowDefinitionVO deployWorkflow(DeployWorkflowRequestDTO request) {
        try {
            // 1. 验证BPMN文件
            validateBpmnFile(request.getBpmnFile());

            // 2. 部署流程定义
            Deployment deployment = processEngine.getRepositoryService()
                .createDeployment()
                .name(request.getWorkflowName())
                .addInputStream(request.getWorkflowKey() + ".bpmn",
                    new ByteArrayInputStream(request.getBpmnFile()))
                .deploy();

            // 3. 获取流程定义
            ProcessDefinition processDefinition = processEngine.getRepositoryService()
                .createProcessDefinitionQuery()
                .deploymentId(deployment.getId())
                .singleResult();

            // 4. 保存流程定义信息
            workflowManager.saveWorkflowDefinition(deployment, processDefinition, request);

            return convertToWorkflowDefinitionVO(processDefinition, request);

        } catch (Exception e) {
            log.error("工作流部署失败: workflowName={}", request.getWorkflowName(), e);
            throw new BusinessException("WORKFLOW_DEPLOY_FAILED", "工作流部署失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceVO startWorkflow(StartWorkflowRequestDTO request) {
        try {
            // 1. 验证流程定义
            ProcessDefinition processDefinition = validateProcessDefinition(request.getProcessDefinitionKey());

            // 2. 构建流程变量
            Map<String, Object> variables = buildProcessVariables(request);

            // 3. 启动流程实例
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                request.getProcessDefinitionKey(),
                request.getBusinessKey(),
                variables);

            // 4. 保存流程实例信息
            workflowManager.saveWorkflowInstance(processInstance, request);

            // 5. 处理第一个任务（如果是自动任务）
            handleFirstTask(processInstance);

            return convertToWorkflowInstanceVO(processInstance);

        } catch (Exception e) {
            log.error("工作流启动失败: processDefinitionKey={}, businessKey={}",
                request.getProcessDefinitionKey(), request.getBusinessKey(), e);
            throw new BusinessException("WORKFLOW_START_FAILED", "工作流启动失败", e);
        }
    }

    @Override
    public List<WorkflowTaskVO> getMyTasks(GetMyTasksRequestDTO request) {
        // 获取当前用户
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 构建任务查询
        TaskQuery taskQuery = taskService.createTaskQuery()
            .active()
            .includeProcessVariables()
            .includeTaskLocalVariables()
            .taskAssignee(String.valueOf(currentUserId));

        // 添加查询条件
        if (StringUtils.isNotBlank(request.getProcessDefinitionKey())) {
            taskQuery.processDefinitionKey(request.getProcessDefinitionKey());
        }

        if (request.getPriority() != null) {
            taskQuery.taskMinPriority(request.getPriority());
        }

        // 分页查询
        List<Task> tasks = taskQuery
            .orderByTaskCreateTime().desc()
            .listPage((request.getPageNum() - 1) * request.getPageSize(), request.getPageSize());

        // 转换结果
        return tasks.stream()
            .map(this::convertToWorkflowTaskVO)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeTask(CompleteTaskRequestDTO request) {
        try {
            // 1. 获取任务
            Task task = taskService.createTaskQuery()
                .taskId(request.getTaskId())
                .singleResult();

            if (task == null) {
                throw new BusinessException("TASK_NOT_FOUND", "任务不存在");
            }

            // 2. 验证任务权限
            validateTaskPermission(task);

            // 3. 处理表单数据
            Map<String, Object> variables = handleFormData(request);

            // 4. 完成任务
            taskService.complete(request.getTaskId(), variables);

            // 5. 记录任务完成日志
            workflowManager.recordTaskCompletion(task, request);

            // 6. 触发任务完成事件
            publishTaskCompletedEvent(task, request);

        } catch (Exception e) {
            log.error("任务完成失败: taskId={}", request.getTaskId(), e);
            throw new BusinessException("TASK_COMPLETE_FAILED", "任务完成失败", e);
        }
    }

    private Map<String, Object> buildProcessVariables(StartWorkflowRequestDTO request) {
        Map<String, Object> variables = new HashMap<>();

        // 添加发起人信息
        variables.put("initiator", SecurityUtils.getCurrentUserId());
        variables.put("initiatorName", SecurityUtils.getCurrentUsername());

        // 添加业务数据
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        // 添加流程配置
        if (request.isSkipFirstTask()) {
            variables.put("_skipFirstTask", true);
        }

        return variables;
    }

    private void validateTaskPermission(Task task) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String assignee = task.getAssignee();

        if (!String.valueOf(currentUserId).equals(assignee)) {
            throw new BusinessException("NO_TASK_PERMISSION", "无权限处理该任务");
        }
    }
}
```

### 📅 会议管理系统
```java
// 会议预订和管理
@Service
@Transactional(rollbackFor = Exception.class)
public class MeetingServiceImpl implements MeetingService {

    @Resource
    private MeetingManager meetingManager;

    @Resource
    private MeetingDao meetingDao;

    @Resource
    private MeetingRoomDao meetingRoomDao;

    @Resource
    private NotificationService notificationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MeetingVO bookMeeting(BookMeetingRequestDTO request) {
        try {
            // 1. 验证会议室可用性
            validateMeetingRoomAvailability(request.getMeetingRoomId(), request.getStartTime(), request.getEndTime());

            // 2. 验证参会人员时间冲突
            validateAttendeeAvailability(request.getAttendees(), request.getStartTime(), request.getEndTime());

            // 3. 创建会议
            MeetingEntity meeting = convertToMeetingEntity(request);
            meeting.setMeetingNo(generateMeetingNo());
            meeting.setOrganizerId(SecurityUtils.getCurrentUserId());
            meeting.setStatus(MeetingStatusEnum.SCHEDULED.getCode());

            meetingDao.insert(meeting);

            // 4. 保存参会人员
            saveMeetingAttendees(meeting.getMeetingId(), request.getAttendees());

            // 5. 更新会议室预订状态
            updateMeetingRoomBooking(meeting.getMeetingRoomId(), meeting.getMeetingId(),
                request.getStartTime(), request.getEndTime());

            // 6. 发送会议通知
            sendMeetingNotification(meeting);

            return convertToMeetingVO(meeting);

        } catch (Exception e) {
            log.error("会议预订失败: room={}, startTime={}, endTime={}",
                request.getMeetingRoomId(), request.getStartTime(), request.getEndTime(), e);
            throw new BusinessException("MEETING_BOOK_FAILED", "会议预订失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelMeeting(Long meetingId, String reason) {
        // 1. 获取会议信息
        MeetingEntity meeting = meetingDao.selectById(meetingId);
        if (meeting == null) {
            throw new BusinessException("MEETING_NOT_FOUND", "会议不存在");
        }

        // 2. 验证取消权限
        validateCancelPermission(meeting);

        // 3. 更新会议状态
        meeting.setStatus(MeetingStatusEnum.CANCELLED.getCode());
        meeting.setCancelReason(reason);
        meeting.setCancelTime(LocalDateTime.now());
        meetingDao.updateById(meeting);

        // 4. 释放会议室
        releaseMeetingRoom(meeting.getMeetingRoomId(), meetingId);

        // 5. 发送取消通知
        sendMeetingCancelNotification(meeting, reason);

        // 6. 处理日历更新
        updateCalendarEvent(meeting, CalendarActionEnum.CANCEL);
    }

    @Override
    public List<MeetingRoomVO> getAvailableMeetingRooms(MeetingRoomQueryRequestDTO request) {
        // 获取所有可用会议室
        List<MeetingRoomEntity> allRooms = meetingRoomDao.selectByStatus(MeetingRoomStatusEnum.AVAILABLE.getCode());

        // 过滤时间冲突的会议室
        return allRooms.stream()
            .filter(room -> isRoomAvailable(room.getRoomId(), request.getStartTime(), request.getEndTime()))
            .map(this::convertToMeetingRoomVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<MeetingVO> getMyMeetingSchedule(MeetingScheduleRequestDTO request) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 查询我组织的会议
        List<MeetingEntity> organizedMeetings = meetingDao.selectByOrganizer(currentUserId,
            request.getStartDate(), request.getEndDate());

        // 查询我参加的会议
        List<MeetingEntity> attendedMeetings = meetingDao.selectByAttendee(currentUserId,
            request.getStartDate(), request.getEndDate());

        // 合并和去重
        Map<Long, MeetingEntity> allMeetings = new HashMap<>();
        organizedMeetings.forEach(meeting -> allMeetings.put(meeting.getMeetingId(), meeting));
        attendedMeetings.forEach(meeting -> allMeetings.putIfAbsent(meeting.getMeetingId(), meeting));

        return allMeetings.values().stream()
            .sorted(Comparator.comparing(MeetingEntity::getStartTime))
            .map(this::convertToMeetingVO)
            .collect(Collectors.toList());
    }

    private void validateMeetingRoomAvailability(Long roomId, LocalDateTime startTime, LocalDateTime endTime) {
        List<MeetingEntity> conflicts = meetingDao.selectConflictingMeetings(roomId, startTime, endTime);
        if (!conflicts.isEmpty()) {
            MeetingEntity conflict = conflicts.get(0);
            throw new BusinessException("ROOM_UNAVAILABLE",
                String.format("会议室在该时间段已被预订，会议时间: %s - %s",
                    conflict.getStartTime(), conflict.getEndTime()));
        }
    }

    private void validateAttendeeAvailability(List<Long> attendees, LocalDateTime startTime, LocalDateTime endTime) {
        for (Long attendeeId : attendees) {
            List<MeetingEntity> conflicts = meetingDao.selectAttendeeConflictingMeetings(attendeeId, startTime, endTime);
            if (!conflicts.isEmpty()) {
                MeetingEntity conflict = conflicts.get(0);
                throw new BusinessException("ATTENDEE_UNAVAILABLE",
                    String.format("参会人员在时间段已有其他会议，会议: %s，时间: %s - %s",
                        conflict.getMeetingName(), conflict.getStartTime(), conflict.getEndTime()));
            }
        }
    }

    private void sendMeetingNotification(MeetingEntity meeting) {
        // 获取参会人员
        List<MeetingAttendeeEntity> attendees = meetingManager.getMeetingAttendees(meeting.getMeetingId());

        // 构建通知消息
        MeetingNotificationMessage message = MeetingNotificationMessage.builder()
            .meetingId(meeting.getMeetingId())
            .meetingName(meeting.getMeetingName())
            .meetingRoom(meeting.getMeetingRoomName())
            .startTime(meeting.getStartTime())
            .endTime(meeting.getEndTime())
            .organizer(meeting.getOrganizerName())
            .build();

        // 发送通知给所有参会人员
        attendees.forEach(attendee -> {
            notificationService.sendNotification(attendee.getAttendeeId(),
                NotificationTypeEnum.MEETING_INVITATION, message);
        });
    }
}
```

---

## 🏗️ 架构设计规范

### 四层架构实现

#### Controller层 - 接口控制层
```java
@RestController
@RequestMapping("/api/v1/oa/organization")
@Tag(name = "组织架构管理")
@Validated
public class OrganizationController {

    @Resource
    private OrganizationService organizationService;

    @PostMapping("/department/create")
    @Operation(summary = "创建部门")
    public ResponseDTO<DepartmentVO> createDepartment(@Valid @RequestBody CreateDepartmentRequestDTO request) {
        DepartmentVO department = organizationService.createDepartment(request);
        return ResponseDTO.ok(department);
    }

    @GetMapping("/department/tree")
    @Operation(summary = "获取组织架构树")
    public ResponseDTO<OrganizationTreeVO> getOrganizationTree() {
        OrganizationTreeVO tree = organizationService.getFullOrganizationTree();
        return ResponseDTO.ok(tree);
    }

    @GetMapping("/department/detail/{deptId}")
    @Operation(summary = "获取部门详情")
    public ResponseDTO<DepartmentDetailVO> getDepartmentDetail(@PathVariable Long deptId) {
        DepartmentDetailVO detail = organizationService.getDepartmentDetail(deptId);
        return ResponseDTO.ok(detail);
    }
}
```

#### Service层 - 核心业务层
```java
@Service
@Transactional(rollbackFor = Exception.class)
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private WorkflowManager workflowManager;

    @Resource
    private ProcessEngine processEngine;

    @Override
    public WorkflowInstanceVO startWorkflow(StartWorkflowRequestDTO request) {
        // 业务规则验证
        validateWorkflowRequest(request);

        // 核心业务逻辑
        return workflowManager.startWorkflow(request);
    }

    private void validateWorkflowRequest(StartWorkflowRequestDTO request) {
        // 验证流程定义是否存在
        ProcessDefinition definition = processEngine.getRepositoryService()
            .createProcessDefinitionQuery()
            .processDefinitionKey(request.getProcessDefinitionKey())
            .latestVersion()
            .singleResult();

        if (definition == null) {
            throw new BusinessException("WORKFLOW_NOT_FOUND", "工作流定义不存在");
        }

        // 验证发起权限
        if (!hasWorkflowPermission(request.getProcessDefinitionKey())) {
            throw new BusinessException("NO_WORKFLOW_PERMISSION", "无权限发起该工作流");
        }
    }
}
```

#### Manager层 - 复杂流程管理层
```java
// ✅ 正确：Manager类为纯Java类，不使用Spring注解
public class WorkflowManager {

    private final ProcessEngine processEngine;
    private final WorkflowInstanceDao workflowInstanceDao;
    private final TaskService taskService;
    private final GatewayServiceClient gatewayServiceClient;
    private final RedisTemplate<String, Object> redisTemplate;

    // 构造函数注入依赖
    public WorkflowManager(ProcessEngine processEngine,
                          WorkflowInstanceDao workflowInstanceDao,
                          TaskService taskService,
                          GatewayServiceClient gatewayServiceClient,
                          RedisTemplate<String, Object> redisTemplate) {
        this.processEngine = processEngine;
        this.workflowInstanceDao = workflowInstanceDao;
        this.taskService = taskService;
        this.gatewayServiceClient = gatewayServiceClient;
        this.redisTemplate = redisTemplate;
    }

    @Transactional(rollbackFor = Exception.class)
    public WorkflowInstanceVO startWorkflow(StartWorkflowRequestDTO request) {
        try {
            // 1. 构建流程变量
            Map<String, Object> variables = buildProcessVariables(request);

            // 2. 启动流程实例
            ProcessInstance processInstance = processEngine.getRuntimeService()
                .startProcessInstanceByKey(request.getProcessDefinitionKey(),
                    request.getBusinessKey(), variables);

            // 3. 保存流程实例信息
            saveWorkflowInstance(processInstance, request);

            // 4. 处理流程启动后事件
            handleProcessStartedEvent(processInstance, request);

            // 5. 检查并处理自动任务
            handleAutomaticTasks(processInstance);

            return convertToWorkflowInstanceVO(processInstance);

        } catch (Exception e) {
            log.error("工作流启动失败: processDefinitionKey={}, businessKey={}",
                request.getProcessDefinitionKey(), request.getBusinessKey(), e);
            throw new BusinessException("WORKFLOW_START_FAILED", "工作流启动失败", e);
        }
    }

    private Map<String, Object> buildProcessVariables(StartWorkflowRequestDTO request) {
        Map<String, Object> variables = new HashMap<>();

        // 添加发起人信息
        variables.put("initiator", SecurityUtils.getCurrentUserId());
        variables.put("initiatorName", SecurityUtils.getCurrentUsername());
        variables.put("initiatorDept", SecurityUtils.getCurrentUserDepartment());

        // 添加业务数据
        if (request.getVariables() != null) {
            variables.putAll(request.getVariables());
        }

        // 添加系统变量
        variables.put("startTime", LocalDateTime.now());
        variables.put("systemDate", LocalDate.now());

        // 从其他微服务获取关联数据
        if (request.getRelatedUserId() != null) {
            variables.putAll(getRelatedUserData(request.getRelatedUserId()));
        }

        return variables;
    }

    private Map<String, Object> getRelatedUserData(Long userId) {
        try {
            // 通过网关调用公共服务获取用户信息
            ResponseDTO<UserDetailVO> response = gatewayServiceClient.callCommonService(
                "/api/v1/common/user/" + userId,
                HttpMethod.GET,
                null,
                new TypeReference<ResponseDTO<UserDetailVO>>() {}
            );

            if (response.isSuccess() && response.getData() != null) {
                UserDetailVO user = response.getData();
                Map<String, Object> userData = new HashMap<>();
                userData.put("relatedUserName", user.getUsername());
                userData.put("relatedUserDept", user.getDepartmentName());
                userData.put("relatedUserPosition", user.getPositionName());
                return userData;
            }

        } catch (Exception e) {
            log.warn("获取关联用户信息失败: userId={}", userId, e);
        }

        return Collections.emptyMap();
    }

    private void handleProcessStartedEvent(ProcessInstance processInstance, StartWorkflowRequestDTO request) {
        // 发布流程启动事件
        WorkflowStartedEvent event = WorkflowStartedEvent.builder()
            .processInstanceId(processInstance.getId())
            .processDefinitionKey(processInstance.getProcessDefinitionKey())
            .businessKey(processInstance.getBusinessKey())
            .initiator(SecurityUtils.getCurrentUserId())
            .startTime(LocalDateTime.now())
            .build();

        // 异步处理事件
        CompletableFuture.runAsync(() -> {
            try {
                // 发送到消息队列
                rabbitTemplate.convertAndSend("workflow.started", event);

                // 更新相关业务状态
                updateRelatedBusinessStatus(processInstance, request);

                // 发送通知给相关人员
                sendWorkflowNotification(processInstance, request);

            } catch (Exception e) {
                log.error("处理流程启动事件失败: processInstanceId={}", processInstance.getId(), e);
            }
        });
    }

    private void handleAutomaticTasks(ProcessInstance processInstance) {
        List<Task> automaticTasks = taskService.createTaskQuery()
            .processInstanceId(processInstance.getId())
            .taskCandidateGroups("automatic")
            .list();

        for (Task task : automaticTasks) {
            try {
                // 自动完成任务
                Map<String, Object> variables = new HashMap<>();
                variables.put("automaticComplete", true);
                variables.put("completedBy", "system");
                variables.put("completedTime", LocalDateTime.now());

                taskService.complete(task.getId(), variables);

                log.info("自动任务完成: taskId={}, taskName={}", task.getId(), task.getName());

            } catch (Exception e) {
                log.error("自动任务完成失败: taskId={}, taskName={}", task.getId(), task.getName(), e);
            }
        }
    }
}
```

#### DAO层 - 数据访问层
```java
@Mapper
public interface DepartmentDao extends BaseMapper<DepartmentEntity> {

    @Transactional(readOnly = true)
    List<DepartmentEntity> selectRootDepartments();

    @Transactional(readOnly = true)
    List<DepartmentEntity> selectChildDepartments(@Param("parentId") Long parentId);

    @Transactional(readOnly = true)
    boolean existsByCode(@Param("deptCode") String deptCode);

    @Transactional(rollbackFor = Exception.class)
    int updateChildrenCount(@Param("deptId") Long deptId, @Param("childrenCount") Integer childrenCount);

    @Select("SELECT * FROM t_common_department WHERE status = 1 AND deleted_flag = 0 " +
            "ORDER BY dept_level ASC, sort_order ASC")
    List<DepartmentEntity> selectAllActiveDepartments();

    @Transactional(readOnly = true)
    List<DepartmentEntity> selectByDeptPath(@Param("deptPath") String deptPath);
}

@Mapper
public interface WorkflowInstanceDao extends BaseMapper<WorkflowInstanceEntity> {

    @Transactional(readOnly = true)
    List<WorkflowInstanceEntity> selectByInitiator(@Param("initiatorId") Long initiatorId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);

    @Transactional(readOnly = true)
    List<WorkflowInstanceEntity> selectByBusinessKey(@Param("businessKey") String businessKey);

    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("processInstanceId") String processInstanceId,
                    @Param("status") String status,
                    @Param("endTime") LocalDateTime endTime);

    @Select("SELECT * FROM t_oa_workflow_instance WHERE process_definition_key = #{processDefinitionKey} " +
            "ORDER BY start_time DESC LIMIT #{limit}")
    List<WorkflowInstanceEntity> selectRecentByProcessDefinition(@Param("processDefinitionKey") String processDefinitionKey,
                                                               @Param("limit") int limit);
}
```

---

## 📊 技能质量指标体系

### 核心质量指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **流程执行成功率** | ≥99% | 工作流程执行成功比例 | 流程执行监控 |
| **会议预订准确率** | ≥99.5% | 会议时间和资源预订准确性 | 预订准确性监控 |
| **文档处理效率** | ≤3s | 文档上传下载处理时间 | 文档处理性能测试 |
| **通知到达率** | ≥95% | OA通知消息到达率 | 通知到达监控 |
| **用户满意度** | ≥90% | OA系统用户满意度 | 用户满意度调查 |

### 性能指标
| 指标名称 | 目标值 | 说明 | 测量方法 |
|---------|--------|------|----------|
| **流程启动响应时间** | ≤2s | 新工作流启动响应时间 | 流程性能测试 |
| **会议查询响应时间** | ≤1s | 会议信息查询响应时间 | 查询性能测试 |
| **文档上传响应时间** | ≤5s | 文档上传处理响应时间 | 上传性能测试 |
| **组织架构查询时间** | ≤1s | 组织架构查询响应时间 | 组织架构性能测试 |

### 版本管理
- **主版本**: v1.0.0 - 初始版本
- **文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
- **创建时间**: 2025-12-08
- **最后更新**: 2025-12-08
- **变更类型**: MAJOR - 新技能创建

---

## 🛠️ 开发规范和最佳实践

### 工作流引擎集成最佳实践
```java
// ✅ 正确的工作流集成
@Service
public class WorkflowServiceImpl implements WorkflowService {

    @Resource
    private ProcessEngine processEngine;

    public WorkflowInstanceVO startWorkflow(StartWorkflowRequestDTO request) {
        try {
            // 启动流程实例
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                request.getProcessDefinitionKey(),
                request.getBusinessKey(),
                buildProcessVariables(request));

            return convertToWorkflowInstanceVO(processInstance);

        } catch (Exception e) {
            log.error("工作流启动失败", e);
            throw new BusinessException("WORKFLOW_START_FAILED", "工作流启动失败", e);
        }
    }

    // ✅ 正确的流程变量构建
    private Map<String, Object> buildProcessVariables(StartWorkflowRequestDTO request) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("initiator", SecurityUtils.getCurrentUserId());
        variables.put("businessData", request.getVariables());
        return variables;
    }
}
```

### 文档管理最佳实践
```java
// ✅ 正确的文档处理
@Service
public class DocumentServiceImpl implements DocumentService {

    public DocumentVO uploadDocument(MultipartFile file, UploadDocumentRequestDTO request) {
        try {
            // 1. 文件验证
            validateFile(file);

            // 2. 存储文件
            String fileUrl = fileStorageService.store(file);

            // 3. 保存文档信息
            DocumentEntity document = createDocumentEntity(file, fileUrl, request);
            documentDao.insert(document);

            // 4. 处理文档权限
            handleDocumentPermissions(document, request);

            return convertToDocumentVO(document);

        } catch (Exception e) {
            log.error("文档上传失败", e);
            throw new BusinessException("DOCUMENT_UPLOAD_FAILED", "文档上传失败", e);
        }
    }
}
```

---

## 🔗 相关文档参考

### 核心架构文档
- **📋 CLAUDE.md**: 全局架构规范 (强制遵循)
- **🏗️ 四层架构详解**: Controller→Service→Manager→DAO架构模式
- **🔧 依赖注入规范**: 统一使用@Resource注解
- **📦 DAO层规范**: 统一使用Dao后缀和@Mapper注解

### 技术栈文档
- **Spring Boot 3.5.8**: 微服务框架文档
- **Camunda BPM**: 工作流引擎文档
- **MinIO**: 对象存储服务文档
- **Elasticsearch**: 文档搜索引擎文档

### 业务模块文档
- **🏢 企业OA系统**: OA办公相关业务
- **🔄 工作流程管理**: 工作流引擎和流程设计
- **📊 组织架构管理**: 企业组织建模和管理

---

**📋 重要提醒**:
1. 本技能严格遵循IOE-DREAM四层架构规范
2. 所有代码示例使用Jakarta EE 3.0+包名规范
3. 统一使用@Resource依赖注入，禁止使用@Autowired
4. 统一使用@Mapper注解和Dao后缀命名
5. 重点关注工作流引擎集成和企业级OA功能
6. 必须支持高并发的组织架构和流程查询

**让我们一起建设高效、智能的OA办公体系！** 🚀

---
**文档版本**: v2.0.0 - IOE-DREAM七微服务专业版
**创建时间**: 2025-12-08
**最后更新**: 2025-12-08
**技能等级**: ★★★★★ (顶级专家)
**适用架构**: Spring Boot 3.5.8 + Camunda BPM + MinIO