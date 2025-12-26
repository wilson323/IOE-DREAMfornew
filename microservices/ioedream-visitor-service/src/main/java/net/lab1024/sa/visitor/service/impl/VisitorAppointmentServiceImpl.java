package net.lab1024.sa.visitor.service.impl;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.observation.annotation.Observed;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.util.QueryBuilder;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.workflow.constant.BusinessTypeEnum;
import net.lab1024.sa.common.workflow.constant.WorkflowDefinitionConstants;
import net.lab1024.sa.common.workflow.manager.WorkflowApprovalManager;
import net.lab1024.sa.visitor.dao.VisitorAppointmentDao;
import net.lab1024.sa.common.entity.visitor.VisitorAppointmentEntity;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.visitor.domain.form.VisitorAppointmentQueryForm;
import net.lab1024.sa.visitor.domain.form.VisitorMobileForm;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.service.VisitorAppointmentService;
import net.lab1024.sa.common.gateway.GatewayServiceClient;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.core.type.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * 访客预约服务实现类
 * <p>
 * 实现访客预约相关业务功能，集成工作流审批
 * 严格遵循CLAUDE.md规范：
 * - Service实现类在ioedream-visitor-service中
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * - 严格遵循四层架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class VisitorAppointmentServiceImpl implements VisitorAppointmentService {


    @Resource
    private VisitorAppointmentDao visitorAppointmentDao;

    @Resource
    private WorkflowApprovalManager workflowApprovalManager;

    @Resource
    private GatewayServiceClient gatewayServiceClient;

    @Override
    @Observed(name = "visitor.appointment.create", contextualName = "visitor-appointment-create")
    @Transactional(rollbackFor = Exception.class)
    @Retry(name = "external-service-retry", fallbackMethod = "createAppointmentFallback")
    public ResponseDTO<Long> createAppointment(VisitorMobileForm form) {
        log.info("[访客预约] 创建预约，visitorName={}, visitUserId={}, startTime={}",
                form.getVisitorName(), form.getVisitUserId(), form.getAppointmentStartTime());

        // 1. 创建预约记录
        VisitorAppointmentEntity entity = new VisitorAppointmentEntity();
        entity.setVisitorName(form.getVisitorName());
        entity.setPhoneNumber(form.getPhoneNumber());
        entity.setIdCardNumber(form.getIdCardNumber());
        entity.setAppointmentType(form.getAppointmentType());
        entity.setVisitUserId(form.getVisitUserId());
        entity.setVisitUserName(form.getVisitUserName());
        entity.setAppointmentStartTime(form.getAppointmentStartTime());
        entity.setAppointmentEndTime(form.getAppointmentEndTime());
        entity.setVisitPurpose(form.getVisitPurpose());
        entity.setRemark(form.getRemark());
        entity.setStatus("PENDING");
        visitorAppointmentDao.insert(entity);

        // 2. 构建表单数据（用于审批流程）
        Map<String, Object> formData = new HashMap<>();
        formData.put("appointmentId", entity.getAppointmentId());
        formData.put("visitorName", form.getVisitorName());
        formData.put("phoneNumber", form.getPhoneNumber());
        formData.put("idCardNumber", form.getIdCardNumber());
        formData.put("visitUserId", form.getVisitUserId());
        formData.put("visitUserName", form.getVisitUserName());
        formData.put("appointmentStartTime", form.getAppointmentStartTime());
        formData.put("appointmentEndTime", form.getAppointmentEndTime());
        formData.put("visitPurpose", form.getVisitPurpose());
        formData.put("applyTime", LocalDateTime.now());

        // 3. 构建流程变量
        Map<String, Object> variables = new HashMap<>();
        // 可以根据预约类型、被访人级别等设置流程变量

        // 4. 启动审批流程
        // 注意：这里使用被访人ID作为发起人，因为是被访人需要审批
        Long initiatorId = form.getVisitUserId();
        Long workflowInstanceId = workflowApprovalManager.startApprovalProcess(
                WorkflowDefinitionConstants.VISITOR_APPOINTMENT, // 流程定义ID
                String.valueOf(entity.getAppointmentId()), // 业务Key
                "访客预约-" + form.getVisitorName() + "-" + form.getVisitUserName(), // 流程实例名称
                initiatorId, // 发起人ID（被访人）
                BusinessTypeEnum.VISITOR_APPOINTMENT.name(), // 业务类型
                formData, // 表单数据
                variables // 流程变量
        );

        if (workflowInstanceId == null) {
            log.error("[访客预约] 启动审批流程失败，appointmentId={}", entity.getAppointmentId());
            throw new BusinessException("启动审批流程失败");
        }

        // 5. 更新预约的workflowInstanceId
        entity.setWorkflowInstanceId(workflowInstanceId);
        visitorAppointmentDao.updateById(entity);

        log.info("[访客预约] 预约创建成功，appointmentId={}, workflowInstanceId={}",
                entity.getAppointmentId(), workflowInstanceId);

        return ResponseDTO.ok(entity.getAppointmentId());
    }

    /**
     * 访客预约创建降级方法
     * <p>
     * 当工作流服务调用失败时，使用此降级方法
     * </p>
     */
    public ResponseDTO<Long> createAppointmentFallback(VisitorMobileForm form, Exception e) {
        log.error("[访客预约] 启动审批流程失败，使用降级方案, visitorName={}, error={}", form.getVisitorName(), e.getMessage(), e);
        throw new BusinessException("启动审批流程失败: " + e.getMessage());
    }

    @Override
    @Observed(name = "visitor.appointment.getDetail", contextualName = "visitor-appointment-get-detail")
    @Transactional(readOnly = true)
    public ResponseDTO<VisitorAppointmentDetailVO> getAppointmentDetail(Long appointmentId) {
        log.info("[访客预约] 获取预约详情，appointmentId={}", appointmentId);

        VisitorAppointmentEntity entity = visitorAppointmentDao.selectById(appointmentId);
        if (entity == null) {
            return ResponseDTO.error("预约不存在");
        }

        // 转换为VO（只设置VO中存在的字段）
        VisitorAppointmentDetailVO vo = new VisitorAppointmentDetailVO();
        vo.setAppointmentId(entity.getAppointmentId());
        vo.setVisitorName(entity.getVisitorName());
        vo.setPhoneNumber(entity.getPhoneNumber());
        vo.setIdCardNumber(entity.getIdCardNumber());
        vo.setVisitUserId(entity.getVisitUserId());
        vo.setVisitUserName(entity.getVisitUserName());
        vo.setAppointmentStartTime(entity.getAppointmentStartTime());
        vo.setAppointmentEndTime(entity.getAppointmentEndTime());
        vo.setVisitPurpose(entity.getVisitPurpose());
        // 注意：如果VO中没有某些字段的setter，则跳过

        return ResponseDTO.ok(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAppointmentStatus(Long appointmentId, String status, String approvalComment) {
        log.info("[访客预约] 更新预约状态，appointmentId={}, status={}", appointmentId, status);

        VisitorAppointmentEntity entity = visitorAppointmentDao.selectById(appointmentId);
        if (entity == null) {
            log.warn("[访客预约] 预约不存在，appointmentId={}", appointmentId);
            return;
        }

        entity.setStatus(status);
        entity.setApprovalComment(approvalComment);
        entity.setApprovalTime(LocalDateTime.now());
        visitorAppointmentDao.updateById(entity);

        // 如果审批通过，执行预约确认后的业务逻辑
        if ("APPROVED".equals(status)) {
            executeAppointmentConfirmation(entity);
        }

        log.info("[访客预约] 预约状态更新成功，appointmentId={}, status={}", appointmentId, status);
    }

    /**
     * 执行预约确认后的业务逻辑
     * <p>
     * 预约确认流程：
     * 1. 生成访问权限（临时权限凭证）
     * 2. 发送预约确认通知（通知访客和被访人）
     * 3. 与门禁系统联动（同步访客权限到门禁设备）
     * </p>
     *
     * @param entity 预约实体
     */
    private void executeAppointmentConfirmation(VisitorAppointmentEntity entity) {
        log.info("[访客预约] 执行预约确认，appointmentId={}, visitorName={}, visitUserName={}, startTime={}, endTime={}",
                entity.getAppointmentId(), entity.getVisitorName(), entity.getVisitUserName(),
                entity.getAppointmentStartTime(), entity.getAppointmentEndTime());

        try {
            // 1. 生成访问权限（临时权限凭证）
            // 通过网关调用门禁服务，创建临时访客权限
            generateVisitorAccessPermission(entity);

            // 2. 发送预约确认通知
            sendAppointmentConfirmationNotification(entity);

            log.info("[访客预约] 预约确认处理成功，appointmentId={}, visitorName={}",
                    entity.getAppointmentId(), entity.getVisitorName());

        } catch (Exception e) {
            log.error("[访客预约] 预约确认处理异常，appointmentId={}", entity.getAppointmentId(), e);
            // 不抛出异常，避免影响审批流程
        }
    }

    /**
     * 生成访客访问权限
     * <p>
     * 通过门禁服务创建临时访问权限，允许访客在预约时间段内访问指定区域
     * </p>
     *
     * @param entity 预约实体
     */
    private void generateVisitorAccessPermission(VisitorAppointmentEntity entity) {
        log.info("[访客预约] 生成访客访问权限，appointmentId={}, visitorName={}, startTime={}, endTime={}",
                entity.getAppointmentId(), entity.getVisitorName(),
                entity.getAppointmentStartTime(), entity.getAppointmentEndTime());

        try {
            // 构建访客权限创建请求
            Map<String, Object> permissionRequest = new HashMap<>();
            permissionRequest.put("appointmentId", entity.getAppointmentId());
            permissionRequest.put("visitorName", entity.getVisitorName());
            permissionRequest.put("phoneNumber", entity.getPhoneNumber());
            permissionRequest.put("idCardNumber", entity.getIdCardNumber());
            permissionRequest.put("visitUserId", entity.getVisitUserId());
            permissionRequest.put("validStartTime", entity.getAppointmentStartTime());
            permissionRequest.put("validEndTime", entity.getAppointmentEndTime());
            permissionRequest.put("visitPurpose", entity.getVisitPurpose());

            // 通过网关调用门禁服务创建访客权限
            // 接口路径：/api/v1/access/visitor/permission/create
            // 注意：如果门禁服务未提供此接口，需要创建对应的Controller和Service方法
            ResponseDTO<Void> response = gatewayServiceClient.callCommonService(
                    "/access/api/v1/access/visitor/permission/create",
                    HttpMethod.POST,
                    permissionRequest,
                    new TypeReference<ResponseDTO<Void>>() {}
            );

            if (response != null && response.isSuccess()) {
                log.info("[访客预约] 访客访问权限生成成功，appointmentId={}, visitorName={}",
                        entity.getAppointmentId(), entity.getVisitorName());
            } else {
                log.warn("[访客预约] 访客访问权限生成失败，但不影响预约确认，appointmentId={}, message={}",
                        entity.getAppointmentId(), response != null ? response.getMessage() : "未知错误");
                // 权限生成失败不影响预约确认，仅记录警告
            }

        } catch (Exception e) {
            log.error("[访客预约] 生成访客访问权限异常，但不影响预约确认，appointmentId={}",
                    entity.getAppointmentId(), e);
            // 权限生成异常不影响预约确认，仅记录错误
        }
    }

    /**
     * 发送预约确认通知
     * <p>
     * 通知访客和被访人预约已确认
     * 支持多渠道通知：短信、邮件、WebSocket等
     * </p>
     *
     * @param entity 预约实体
     */
    private void sendAppointmentConfirmationNotification(VisitorAppointmentEntity entity) {
        log.info("[访客预约] 发送预约确认通知，appointmentId={}, visitorName={}, visitUserName={}",
                entity.getAppointmentId(), entity.getVisitorName(), entity.getVisitUserName());

        try {
            // 1. 通知访客：预约已通过，可以按时到访
            sendVisitorNotification(entity);

            // 2. 通知被访人：访客预约已通过，请做好接待准备
            sendHostNotification(entity);

            log.info("[访客预约] 预约确认通知发送成功，appointmentId={}", entity.getAppointmentId());

        } catch (Exception e) {
            log.error("[访客预约] 发送预约确认通知异常，但不影响预约确认，appointmentId={}",
                    entity.getAppointmentId(), e);
            // 通知发送失败不影响预约确认，仅记录错误
        }
    }

    /**
     * 发送访客通知
     *
     * @param entity 预约实体
     */
    private void sendVisitorNotification(VisitorAppointmentEntity entity) {
        try {
            // 构建通知内容
            String title = "访客预约确认通知";
            String content = String.format(
                    "尊敬的%s，您的访客预约已通过审批。\n" +
                    "预约时间：%s 至 %s\n" +
                    "被访人：%s\n" +
                    "访问目的：%s\n" +
                    "请按时到访，感谢配合！",
                    entity.getVisitorName(),
                    entity.getAppointmentStartTime(),
                    entity.getAppointmentEndTime(),
                    entity.getVisitUserName(),
                    entity.getVisitPurpose() != null ? entity.getVisitPurpose() : "未填写"
            );

            log.info("[访客预约] 访客通知内容已准备，appointmentId={}, visitorName={}, phone={}",
                    entity.getAppointmentId(), entity.getVisitorName(), entity.getPhoneNumber());

            // 通过网关调用通知服务发送通知
            try {
                Map<String, Object> notificationRequest = new HashMap<>();
                notificationRequest.put("title", title);
                notificationRequest.put("content", content);
                notificationRequest.put("notificationType", 2); // 2-业务通知
                notificationRequest.put("receiverType", 1); // 1-指定用户
                notificationRequest.put("receiverIds", entity.getPhoneNumber()); // 使用手机号作为接收人标识
                notificationRequest.put("channel", 2); // 2-短信
                notificationRequest.put("status", 0); // 0-待发送

                // 通过网关调用通知服务
                ResponseDTO<Void> notificationResponse = gatewayServiceClient.callCommonService(
                        "/api/v1/notification/send",
                        HttpMethod.POST,
                        notificationRequest,
                        new TypeReference<ResponseDTO<Void>>() {}
                );

                if (notificationResponse != null && notificationResponse.isSuccess()) {
                    log.info("[访客预约] 访客通知发送成功，appointmentId={}", entity.getAppointmentId());
                } else {
                    log.warn("[访客预约] 访客通知发送失败，appointmentId={}, message={}",
                            entity.getAppointmentId(),
                            notificationResponse != null ? notificationResponse.getMessage() : "未知错误");
                }
            } catch (Exception e) {
                log.error("[访客预约] 发送访客通知异常，appointmentId={}", entity.getAppointmentId(), e);
                // 通知发送失败不影响主流程，仅记录日志
            }

        } catch (Exception e) {
            log.error("[访客预约] 发送访客通知异常，appointmentId={}", entity.getAppointmentId(), e);
        }
    }

    /**
     * 发送被访人通知
     *
     * @param entity 预约实体
     */
    private void sendHostNotification(VisitorAppointmentEntity entity) {
        try {
            // 构建通知内容
            String title = "访客预约提醒";
            String content = String.format(
                    "您好%s，您有新的访客预约已通过审批。\n" +
                    "访客姓名：%s\n" +
                    "预约时间：%s 至 %s\n" +
                    "访问目的：%s\n" +
                    "联系电话：%s\n" +
                    "请做好接待准备，感谢配合！",
                    entity.getVisitUserName(),
                    entity.getVisitorName(),
                    entity.getAppointmentStartTime(),
                    entity.getAppointmentEndTime(),
                    entity.getVisitPurpose() != null ? entity.getVisitPurpose() : "未填写",
                    entity.getPhoneNumber() != null ? entity.getPhoneNumber() : "未提供"
            );

            log.info("[访客预约] 被访人通知内容已准备，appointmentId={}, visitUserId={}, visitUserName={}",
                    entity.getAppointmentId(), entity.getVisitUserId(), entity.getVisitUserName());

            // 通过网关调用通知服务发送通知
            if (entity.getVisitUserId() != null) {
                try {
                    Map<String, Object> notificationRequest = new HashMap<>();
                    notificationRequest.put("title", title);
                    notificationRequest.put("content", content);
                    notificationRequest.put("notificationType", 2); // 2-业务通知
                    notificationRequest.put("receiverType", 1); // 1-指定用户
                    notificationRequest.put("receiverIds", entity.getVisitUserId().toString());
                    notificationRequest.put("channel", 4); // 4-微信/WebSocket
                    notificationRequest.put("status", 0); // 0-待发送

                    // 通过网关调用通知服务
                    ResponseDTO<Void> notificationResponse = gatewayServiceClient.callCommonService(
                            "/api/v1/notification/send",
                            HttpMethod.POST,
                            notificationRequest,
                            new TypeReference<ResponseDTO<Void>>() {}
                    );

                    if (notificationResponse != null && notificationResponse.isSuccess()) {
                        log.info("[访客预约] 被访人通知发送成功，appointmentId={}, visitUserId={}",
                                entity.getAppointmentId(), entity.getVisitUserId());
                    } else {
                        log.warn("[访客预约] 被访人通知发送失败，appointmentId={}, visitUserId={}, message={}",
                                entity.getAppointmentId(),
                                entity.getVisitUserId(),
                                notificationResponse != null ? notificationResponse.getMessage() : "未知错误");
                    }
                } catch (Exception e) {
                    log.error("[访客预约] 发送被访人通知异常，appointmentId={}, visitUserId={}",
                            entity.getAppointmentId(), entity.getVisitUserId(), e);
                    // 通知发送失败不影响主流程，仅记录日志
                }
            } else {
                log.warn("[访客预约] 被访人用户ID为空，无法发送通知，appointmentId={}", entity.getAppointmentId());
            }

        } catch (Exception e) {
            log.error("[访客预约] 发送被访人通知异常，appointmentId={}", entity.getAppointmentId(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<VisitorAppointmentDetailVO> queryAppointments(VisitorAppointmentQueryForm queryForm) {
        log.info("[访客预约] 分页查询预约，pageNum={}, pageSize={}, visitorName={}, hostUserId={}, startDate={}, endDate={}, status={}",
                queryForm.getPageNum(), queryForm.getPageSize(), queryForm.getVisitorName(),
                queryForm.getHostUserId(), queryForm.getStartDate(), queryForm.getEndDate(), queryForm.getStatus());
        // 预处理时间和日期参数
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (queryForm.getStartDate() != null) {
            startTime = queryForm.getStartDate().atStartOfDay();
        }
        if (queryForm.getEndDate() != null) {
            endTime = queryForm.getEndDate().atTime(23, 59, 59);
        }

        // 构建查询条件（使用QueryBuilder）
        LambdaQueryWrapper<VisitorAppointmentEntity> wrapper = QueryBuilder.of(VisitorAppointmentEntity.class)
            .keyword(queryForm.getVisitorName(), VisitorAppointmentEntity::getVisitorName)
            .eq(VisitorAppointmentEntity::getVisitUserId, queryForm.getHostUserId())
            .ge(VisitorAppointmentEntity::getAppointmentStartTime, startTime)
            .le(VisitorAppointmentEntity::getAppointmentEndTime, endTime)
            .eq(VisitorAppointmentEntity::getStatus, queryForm.getStatus())
            .eq(VisitorAppointmentEntity::getDeletedFlag, 0)
            .orderByDesc(VisitorAppointmentEntity::getCreateTime)
            .build();

        // 执行分页查询
        Page<VisitorAppointmentEntity> page = new Page<>(queryForm.getPageNum(), queryForm.getPageSize());
        Page<VisitorAppointmentEntity> pageResult = visitorAppointmentDao.selectPage(page, wrapper);

        // 转换为VO列表
        List<VisitorAppointmentDetailVO> voList = pageResult.getRecords().stream()
                .map(this::convertToDetailVO)
                .collect(Collectors.toList());

        // 构建分页结果
        PageResult<VisitorAppointmentDetailVO> result = PageResult.of(
                voList,
                pageResult.getTotal(),
                queryForm.getPageNum(),
                queryForm.getPageSize()
        );

        log.info("[访客预约] 分页查询预约成功，总数={}", pageResult.getTotal());
        return result;

    }

    /**
     * 转换Entity为DetailVO
     *
     * @param entity 预约实体
     * @return 预约详情VO
     */
    private VisitorAppointmentDetailVO convertToDetailVO(VisitorAppointmentEntity entity) {
        VisitorAppointmentDetailVO vo = new VisitorAppointmentDetailVO();
        vo.setAppointmentId(entity.getAppointmentId());
        vo.setVisitorName(entity.getVisitorName());
        vo.setPhoneNumber(entity.getPhoneNumber());
        vo.setIdCardNumber(entity.getIdCardNumber());
        vo.setVisitUserId(entity.getVisitUserId());
        vo.setVisitUserName(entity.getVisitUserName());
        vo.setAppointmentStartTime(entity.getAppointmentStartTime());
        vo.setAppointmentEndTime(entity.getAppointmentEndTime());
        vo.setVisitPurpose(entity.getVisitPurpose());
        vo.setRemark(entity.getRemark());
        // 转换状态：字符串转整数
        if (entity.getStatus() != null) {
            if ("PENDING".equals(entity.getStatus())) {
                vo.setAppointmentStatus(1); // 待审批
            } else if ("APPROVED".equals(entity.getStatus())) {
                vo.setAppointmentStatus(2); // 已通过
            } else if ("REJECTED".equals(entity.getStatus())) {
                vo.setAppointmentStatus(3); // 已驳回
            } else if ("CANCELLED".equals(entity.getStatus())) {
                vo.setAppointmentStatus(4); // 已取消
            } else if ("CHECKED_IN".equals(entity.getStatus())) {
                vo.setAppointmentStatus(5); // 已签到
            } else if ("CHECKED_OUT".equals(entity.getStatus())) {
                vo.setAppointmentStatus(6); // 已签退
            } else {
                vo.setAppointmentStatus(1); // 默认待审批
            }
        } else {
            vo.setAppointmentStatus(1); // 默认待审批
        }
        return vo;
    }
}


