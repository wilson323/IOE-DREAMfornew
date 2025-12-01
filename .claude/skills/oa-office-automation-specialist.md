# OA办公自动化专家 - Office Automation Specialist

**🎯 技能定位**: 专精于企业级OA办公自动化系统的设计、开发和运维技术专家

**⚡ 技能等级**: ★★★ (高级专家)
**🎯 适用场景**: 智慧园区OA系统、企业办公自动化、工作流管理系统、文档协作平台
**📊 技能覆盖**: Office Automation | Workflow Management | Document Collaboration | Enterprise Integration

---

## 📋 技能概述

### **核心专长**
- **工作流引擎**: 企业级工作流系统设计、流程建模、审批流程管理
- **文档管理**: 文档版本控制、权限管理、协同编辑、全文检索
- **消息通知**: 多渠道消息推送、邮件通知、即时通讯集成
- **系统集成**: 第三方系统对接、数据同步、统一身份认证
- **移动办公**: 移动端OA应用、审批流程移动化、实时通知

### **解决能力**
- **流程自动化**: 复杂业务流程的自动化设计和实现
- **文档协同**: 多人实时协作编辑、版本控制、权限管理
- **审批管理**: 多级审批、条件分支、会签、委托审批等复杂场景
- **数据集成**: 与HR、财务、CRM等系统的数据集成和同步
- **移动支持**: 全功能移动端应用，随时随地办公

---

## 🛠️ 技术能力矩阵

### **OA技术栈**
```
🔴 核心技术 (精通)
├── 工作流引擎: Activiti、Camunda、Flowable
├── 文档处理: POI、iText、PDF处理、图片处理
├── 消息队列: RabbitMQ、RocketMQ、Kafka
├── 实时通讯: WebSocket、SSE、移动推送
├── 全文检索: Elasticsearch、Lucene、中文分词

🟡 架构设计 (熟练)
├── 微服务架构: 流程服务、文档服务、通知服务
├── 数据库设计: 流程定义、文档元数据、审批记录
├── 缓存策略: 流程实例缓存、用户权限缓存
├── 安全设计: 数据加密、访问控制、审计日志

🟢 前端技术 (掌握)
├── 在线编辑: TinyMCE、CKEditor、Office Online
├── 文档预览: PDF.js、Office文档预览
├── 移动开发: React Native、Flutter、小程序
└── 可视化设计: 流程图绘制、数据可视化
```

### **业务场景覆盖**
```
✅ 企业审批流程 (100+流程类型)
✅ 文档管理系统
✅ 会议管理平台
✅ 考勤请假管理
✅ 费用报销系统
✅ 合同管理系统
```

---

## 🎨 核心业务模块

### **1. 工作流管理**
```java
@RestController
@RequestMapping("/api/oa/workflow")
@Tag(name = "工作流管理", description = "工作流定义和实例管理")
@Slf4j
public class WorkflowController {

    @Resource
    private WorkflowDefinitionService definitionService;

    @Resource
    private WorkflowInstanceService instanceService;

    @PostMapping("/definition/deploy")
    @Operation(summary = "部署工作流定义")
    @SaCheckPermission("oa:workflow:deploy")
    public ResponseDTO<String> deployDefinition(@RequestBody @Valid WorkflowDeployForm form) {
        log.info("部署工作流定义: {}", form.getDefinitionName());

        try {
            // 1. 验证流程定义
            WorkflowValidationResult validation = definitionService.validateDefinition(form);
            if (!validation.isValid()) {
                return ResponseDTO.error(UserErrorCode.PARAM_ERROR, validation.getErrorMessage());
            }

            // 2. 部署流程定义
            String deploymentId = definitionService.deployWorkflow(form);

            // 3. 缓存流程定义信息，使用稳定数据类型
            cacheService.set(CacheModule.OA, "workflow",
                "definition:" + deploymentId,
                form,
                BusinessDataType.STABLE);

            return ResponseDTO.ok(deploymentId, "工作流定义部署成功");
        } catch (Exception e) {
            log.error("部署工作流定义失败: {}", form.getDefinitionName(), e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "部署失败: " + e.getMessage());
        }
    }

    @PostMapping("/instance/start")
    @Operation(summary = "启动工作流实例")
    @SaCheckPermission("oa:workflow:start")
    public ResponseDTO<WorkflowInstanceVO> startInstance(@RequestBody @Valid WorkflowStartForm form) {
        log.info("启动工作流实例: processDefinitionKey={}, businessKey={}",
                 form.getProcessDefinitionKey(), form.getBusinessKey());

        try {
            WorkflowInstanceVO instance = instanceService.startWorkflow(form);

            // 4. 实时通知相关用户
            notifyStakeholders(instance);

            // 5. 缓存实例信息，使用实时数据类型
            cacheService.set(CacheModule.OA, "workflow-instance",
                "instance:" + instance.getInstanceId(),
                instance,
                BusinessDataType.REALTIME);

            return ResponseDTO.ok(instance, "工作流实例启动成功");
        } catch (Exception e) {
            log.error("启动工作流实例失败: {}", form.getProcessDefinitionKey(), e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "启动失败: " + e.getMessage());
        }
    }

    @PostMapping("/task/complete")
    @Operation(summary = "完成任务")
    @SaCheckPermission("oa:workflow:complete")
    public ResponseDTO<String> completeTask(@RequestBody @Valid WorkflowTaskCompleteForm form) {
        log.info("完成任务: taskId={}, assignee={}", form.getTaskId(), form.getAssignee());

        try {
            // 1. 验证任务权限
            if (!instanceService.canCompleteTask(form.getTaskId(), getCurrentUserId())) {
                return ResponseDTO.error(SystemErrorCode.PERMISSION_DENIED, "无权限完成任务");
            }

            // 2. 完成任务
            instanceService.completeTask(form);

            // 3. 清除相关缓存
            cacheService.delete(CacheModule.OA, "task", "detail:" + form.getTaskId());

            return ResponseDTO.ok("任务完成成功");
        } catch (Exception e) {
            log.error("完成任务失败: taskId={}", form.getTaskId(), e);
            return ResponseDTO.error(SystemErrorCode.SYSTEM_ERROR, "完成失败: " + e.getMessage());
        }
    }
}
```

### **2. 文档管理系统**
```java
@Service
@Slf4j
public class DocumentService {

    @Resource
    private DocumentRepository documentRepository;

    @Resource
    private UnifiedCacheService cacheService;

    /**
     * 文档上传处理
     */
    public DocumentUploadResult uploadDocument(MultipartFile file, DocumentUploadForm form) {
        try {
            // 1. 文件安全检查
            SecurityCheckResult securityResult = performSecurityCheck(file);
            if (!securityResult.isSafe()) {
                throw new DocumentException("文件安全检查失败: " + securityResult.getReason());
            }

            // 2. 生成文档信息
            DocumentEntity document = DocumentEntity.builder()
                .documentId(generateDocumentId())
                .originalName(file.getOriginalFilename())
                .fileName(generateFileName(file.getOriginalFilename()))
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .categoryId(form.getCategoryId())
                .creatorId(getCurrentUserId())
                .creatorName(getCurrentUserName())
                .departmentId(getCurrentUserDepartmentId())
                .uploadTime(LocalDateTime.now())
                .status(DocumentStatus.ACTIVE)
                .build();

            // 3. 存储文件
            String filePath = storageService.storeFile(file, document.getFileName());
            document.setFilePath(filePath);

            // 4. 提取文档元数据
            extractDocumentMetadata(document);

            // 5. 权限设置
            setDocumentPermissions(document, form);

            // 6. 保存文档信息
            documentRepository.save(document);

            // 7. 全文索引
            searchService.indexDocument(document);

            // 8. 缓存文档信息，使用正常数据类型
            cacheService.set(CacheModule.OA, "document",
                "detail:" + document.getDocumentId(),
                document,
                BusinessDataType.NORMAL);

            log.info("文档上传成功: documentId={}, fileName={}",
                     document.getDocumentId(), document.getOriginalName());

            return DocumentUploadResult.builder()
                .documentId(document.getDocumentId())
                .fileName(document.getOriginalName())
                .fileSize(document.getFileSize())
                .uploadTime(document.getUploadTime())
                .build();

        } catch (Exception e) {
            log.error("文档上传失败: fileName={}", file.getOriginalFilename(), e);
            throw new DocumentException("文档上传失败: " + e.getMessage());
        }
    }

    /**
     * 文档版本管理
     */
    public DocumentVersionResult createNewVersion(String documentId, MultipartFile file, String versionNote) {
        try {
            // 1. 获取当前文档
            DocumentEntity currentDocument = documentRepository.findById(documentId)
                .orElseThrow(() -> new DocumentException("文档不存在: " + documentId));

            // 2. 创建新版本
            DocumentVersionEntity newVersion = DocumentVersionEntity.builder()
                .versionId(generateVersionId())
                .documentId(documentId)
                .versionNumber(currentDocument.getCurrentVersion() + 1)
                .fileName(generateVersionFileName(file.getOriginalFilename(), currentDocument.getCurrentVersion() + 1))
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .uploaderId(getCurrentUserId())
                .uploaderName(getCurrentUserName())
                .uploadTime(LocalDateTime.now())
                .versionNote(versionNote)
                .build();

            // 3. 存储新版本文件
            String versionFilePath = storageService.storeVersionFile(file, newVersion.getFileName());
            newVersion.setFilePath(versionFilePath);

            // 4. 更新文档当前版本
            currentDocument.setCurrentVersion(newVersion.getVersionNumber());
            currentDocument.setLatestVersionId(newVersion.getVersionId());
            documentRepository.save(currentDocument);

            // 5. 保存版本信息
            documentVersionRepository.save(newVersion);

            // 6. 缓存更新，使用正常数据类型
            cacheService.set(CacheModule.OA, "document",
                "detail:" + documentId,
                currentDocument,
                BusinessDataType.NORMAL);

            log.info("文档版本创建成功: documentId={}, version={}",
                     documentId, newVersion.getVersionNumber());

            return DocumentVersionResult.builder()
                .versionId(newVersion.getVersionId())
                .versionNumber(newVersion.getVersionNumber())
                .fileName(newVersion.getFileName())
                .build();

        } catch (Exception e) {
            log.error("文档版本创建失败: documentId={}", documentId, e);
            throw new DocumentException("创建新版本失败: " + e.getMessage());
        }
    }

    /**
     * 文档权限检查
     */
    public boolean hasDocumentPermission(String documentId, DocumentPermission permission) {
        try {
            // 1. 检查缓存
            String cacheKey = "permission:" + documentId + ":" + getCurrentUserId();
            Boolean cachedPermission = cacheService.get(CacheModule.OA, "permission", cacheKey, Boolean.class);
            if (cachedPermission != null) {
                return cachedPermission;
            }

            // 2. 获取文档信息
            DocumentEntity document = getDocument(documentId);
            if (document == null) {
                return false;
            }

            // 3. 权限检查逻辑
            boolean hasPermission = checkDocumentPermissionLogic(document, permission);

            // 4. 缓存权限结果，使用近实时数据类型
            cacheService.set(CacheModule.OA, "permission",
                cacheKey,
                hasPermission,
                BusinessDataType.NEAR_REALTIME);

            return hasPermission;
        } catch (Exception e) {
            log.error("文档权限检查失败: documentId={}", documentId, e);
            return false;
        }
    }
}
```

### **3. 消息通知系统**
```java
@Service
@Slf4j
public class NotificationService {

    @Resource
    private EmailService emailService;

    @Resource
    private SmsService smsService;

    @Resource
    private WebSocketService webSocketService;

    @Resource
    private MobilePushService mobilePushService;

    /**
     * 多渠道消息发送
     */
    public NotificationResult sendMultiChannelNotification(NotificationMessage message) {
        try {
            NotificationResult result = NotificationResult.builder()
                .messageId(generateMessageId())
                .message(message)
                .build();

            // 1. 确定接收渠道
            List<NotificationChannel> channels = determineNotificationChannels(message);

            // 2. 多渠道发送
            for (NotificationChannel channel : channels) {
                try {
                    switch (channel) {
                        case EMAIL:
                            sendEmailNotification(message);
                            result.addChannelResult(EMAIL, true);
                            break;
                        case SMS:
                            sendSmsNotification(message);
                            result.addChannelResult(SMS, true);
                            break;
                        case WEBSOCKET:
                            sendWebSocketNotification(message);
                            result.addChannelResult(WEBSOCKET, true);
                            break;
                        case MOBILE_PUSH:
                            sendMobilePushNotification(message);
                            result.addChannelResult(MOBILE_PUSH, true);
                            break;
                        default:
                            log.warn("不支持的通知渠道: {}", channel);
                    }
                } catch (Exception e) {
                    log.error("通知发送失败: channel={}, messageId={}", channel, message.getMessageId(), e);
                    result.addChannelResult(channel, false);
                }
            }

            // 3. 记录发送日志
            logNotificationSent(message, result);

            // 4. 缓存通知状态，使用实时数据类型
            cacheService.set(CacheModule.OA, "notification",
                "status:" + result.getMessageId(),
                result,
                BusinessDataType.REALTIME);

            return result;
        } catch (Exception e) {
            log.error("多渠道通知发送失败: messageId={}", message.getMessageId(), e);
            return NotificationResult.error("通知发送失败: " + e.getMessage());
        }
    }

    /**
     * 工作流任务通知
     */
    public void sendWorkflowTaskNotification(WorkflowTaskEvent event) {
        try {
            // 1. 构建通知消息
            NotificationMessage message = NotificationMessage.builder()
                .messageType(NotificationMessageType.WORKFLOW_TASK)
                .title("待办任务通知")
                .content(buildTaskNotificationContent(event))
                .recipients(event.getAssignees())
                .priority(event.getPriority())
                .templateCode("WORKFLOW_TASK_TEMPLATE")
                .templateData(buildTemplateData(event))
                .build();

            // 2. 添加工作流相关数据
            message.addMetadata("taskId", event.getTaskId());
            message.addMetadata("instanceId", event.getInstanceId());
            message.addMetadata("processDefinitionKey", event.getProcessDefinitionKey());

            // 3. 发送通知
            sendMultiChannelNotification(message);

            // 4. 发送实时WebSocket通知
            sendRealtimeTaskNotification(event);

            log.info("工作流任务通知发送成功: taskId={}, recipients={}",
                     event.getTaskId(), event.getAssignees().size());

        } catch (Exception e) {
            log.error("工作流任务通知发送失败: taskId={}", event.getTaskId(), e);
        }
    }

    /**
     * 文档分享通知
     */
    public void sendDocumentShareNotification(DocumentShareEvent event) {
        try {
            NotificationMessage message = NotificationMessage.builder()
                .messageType(NotificationMessageType.DOCUMENT_SHARE)
                .title("文档分享通知")
                .content(buildDocumentShareContent(event))
                .recipients(event.getShareTargets())
                .priority(NotificationPriority.NORMAL)
                .build();

            // 添加文档相关信息
            message.addMetadata("documentId", event.getDocumentId());
            message.addMetadata("documentName", event.getDocumentName());
            message.addMetadata("shareType", event.getShareType().name());
            message.addMetadata("sharerId", event.getSharerId());
            message.addMetadata("sharerName", event.getSharerName());

            sendMultiChannelNotification(message);

        } catch (Exception e) {
            log.error("文档分享通知发送失败: documentId={}", event.getDocumentId(), e);
        }
    }

    /**
     * 实时WebSocket通知
     */
    private void sendRealtimeNotification(NotificationMessage message) {
        try {
            for (String recipient : message.getRecipients()) {
                WebSocketMessage wsMessage = WebSocketMessage.builder()
                    .type(WebSocketMessageType.NOTIFICATION)
                    .recipient(recipient)
                    .data(message)
                    .timestamp(LocalDateTime.now())
                    .build();

                webSocketService.sendToUser(recipient, wsMessage);
            }
        } catch (Exception e) {
            log.error("实时通知发送失败: recipients={}", message.getRecipients(), e);
        }
    }
}
```

### **4. 会议管理系统**
```java
@Service
@Slf4j
public class MeetingService {

    @Resource
    private MeetingRepository meetingRepository;

    @Resource
    private MeetingRoomService roomService;

    /**
     * 会议安排
     */
    public MeetingScheduleResult scheduleMeeting(MeetingScheduleForm form) {
        try {
            // 1. 检查会议室可用性
            List<MeetingRoomEntity> availableRooms = checkRoomAvailability(
                form.getRoomId(), form.getStartTime(), form.getEndTime());

            if (availableRooms.isEmpty()) {
                return MeetingScheduleResult.error("会议室在指定时间不可用");
            }

            // 2. 检查参与者可用性
            List<Long> unavailableParticipants = checkParticipantAvailability(
                form.getParticipants(), form.getStartTime(), form.getEndTime());

            if (!unavailableParticipants.isEmpty()) {
                return MeetingScheduleResult.conflict("以下参与者时间冲突", unavailableParticipants);
            }

            // 3. 创建会议
            MeetingEntity meeting = MeetingEntity.builder()
                .meetingId(generateMeetingId())
                .title(form.getTitle())
                .description(form.getDescription())
                .organizerId(getCurrentUserId())
                .organizerName(getCurrentUserName())
                .roomId(form.getRoomId())
                .startTime(form.getStartTime())
                .endTime(form.getEndTime())
                .participants(form.getParticipants())
                .status(MeetingStatus.SCHEDULED)
                .createTime(LocalDateTime.now())
                .build();

            meetingRepository.save(meeting);

            // 4. 锁定会议室
            roomService.lockRoom(form.getRoomId(), meeting.getMeetingId(),
                                 form.getStartTime(), form.getEndTime());

            // 5. 发送会议通知
            sendMeetingNotifications(meeting);

            // 6. 缓存会议信息，使用正常数据类型
            cacheService.set(CacheModule.OA, "meeting",
                "detail:" + meeting.getMeetingId(),
                meeting,
                BusinessDataType.NORMAL);

            log.info("会议安排成功: meetingId={}, title={}",
                     meeting.getMeetingId(), meeting.getTitle());

            return MeetingScheduleResult.builder()
                .meetingId(meeting.getMeetingId())
                .roomId(form.getRoomId())
                .participantCount(meeting.getParticipants().size())
                .build();

        } catch (Exception e) {
            log.error("会议安排失败: title={}", form.getTitle(), e);
            throw new MeetingException("会议安排失败: " + e.getMessage());
        }
    }

    /**
     * 会议签到
     */
    public MeetingCheckInResult checkInMeeting(String meetingId, Long participantId) {
        try {
            // 1. 验证会议存在且有效
            MeetingEntity meeting = getMeeting(meetingId);
            if (meeting == null || meeting.getStatus() != MeetingStatus.SCHEDULED) {
                return MeetingCheckInResult.error("会议不存在或已结束");
            }

            // 2. 检查是否为会议参与者
            if (!meeting.getParticipants().contains(participantId)) {
                return MeetingCheckInResult.error("您不是该会议的参与者");
            }

            // 3. 检查签到时间
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(meeting.getStartTime().minusMinutes(15))) {
                return MeetingCheckInResult.error("会议开始前15分钟内才能签到");
            }

            if (now.isAfter(meeting.getEndTime())) {
                return MeetingCheckInResult.error("会议已结束");
            }

            // 4. 创建签到记录
            MeetingCheckInEntity checkIn = MeetingCheckInEntity.builder()
                .checkInId(generateCheckInId())
                .meetingId(meetingId)
                .participantId(participantId)
                .checkInTime(now)
                .checkInLocation(getCurrentLocation())
                .build();

            meetingCheckInRepository.save(checkIn);

            // 5. 更新参会状态
            updateParticipantAttendance(meetingId, participantId, true);

            return MeetingCheckInResult.builder()
                .checkInId(checkIn.getCheckInId())
                .meetingTitle(meeting.getTitle())
                .checkInTime(checkIn.getCheckInTime())
                .build();

        } catch (Exception e) {
            log.error("会议签到失败: meetingId={}, participantId={}", meetingId, participantId, e);
            throw new MeetingException("会议签到失败: " + e.getMessage());
        }
    }
}
```

---

## 🔧 性能优化策略

### **文档缓存优化**
```java
@Configuration
public class DocumentCacheConfig {

    @Bean
    public DocumentCacheManager cacheManager() {
        return DocumentCacheManager.builder()
            .documentCacheDuration(Duration.ofHours(2))
            .permissionCacheDuration(Duration.ofMinutes(30))
            .searchCacheDuration(Duration.ofMinutes(10))
            .build();
    }
}
```

### **异步处理优化**
```java
@Service
@Slf4j
public class AsyncNotificationService {

    @Async("notificationExecutor")
    public CompletableFuture<Void> sendAsyncNotifications(List<NotificationMessage> messages) {
        return CompletableFuture.runAsync(() -> {
            for (NotificationMessage message : messages) {
                try {
                    notificationService.sendMultiChannelNotification(message);
                } catch (Exception e) {
                    log.error("异步通知发送失败: messageId={}", message.getMessageId(), e);
                }
            }
        });
    }
}
```

---

## 📊 关键性能指标

### **性能目标**
- **文档上传**: ≤ 10秒 (10MB以内)
- **流程启动**: ≤ 3秒
- **任务完成**: ≤ 2秒
- **消息推送**: ≤ 1秒
- **搜索响应**: ≤ 500ms

### **质量指标**
- **系统可用性**: 99.9%
- **数据一致性**: 99.99%
- **消息送达率**: 99%
- **审批通过率**: 符合业务规则

---

## 📚 技能应用指南

### **使用时机**
- **OA系统建设**: 设计和部署企业级OA办公自动化系统
- **工作流集成**: 与现有业务系统集成工作流功能
- **文档协作**: 构建企业级文档管理和协作平台
- **移动办公**: 开发全功能移动端办公应用

### **调用方式**
```bash
# OA系统设计
Skill("oa-office-automation-specialist")

# 工作流配置
Skill("oa-office-automation-specialist")

# 文档管理
Skill("oa-office-automation-specialist")
```

### **预期结果**
- **完整OA系统**: 支持工作流、文档、会议等核心OA功能
- **智能工作流**: 灵活配置和执行的工作流引擎
- **高效协作**: 多人实时协作编辑和版本控制
- **移动优先**: 全功能移动端支持和实时通知

---

**掌握此技能，您将成为企业级OA办公自动化系统的技术专家，能够设计和实现功能完整、性能优秀的办公自动化解决方案。**
