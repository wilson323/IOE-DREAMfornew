package net.lab1024.sa.visitor.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.visitor.domain.form.VisitorMobileForm;
import net.lab1024.sa.visitor.domain.vo.VisitorAppointmentDetailVO;
import net.lab1024.sa.visitor.service.OcrService;
import net.lab1024.sa.visitor.service.VisitorAppointmentService;
import net.lab1024.sa.visitor.service.VisitorCheckInService;
import net.lab1024.sa.visitor.service.VisitorExportService;
import net.lab1024.sa.visitor.service.VisitorQueryService;
import net.lab1024.sa.visitor.service.VisitorService;
import net.lab1024.sa.visitor.service.VisitorStatisticsService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 移动端访客控制器
 * <p>
 * 提供移动端访客管理相关API
 * 严格遵循CLAUDE.md规范：
 * - 使用@RestController注解
 * - 使用@Resource依赖注入
 * - 使用@Valid参数校验
 * - 返回统一ResponseDTO格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mobile/visitor")
@Tag(name = "移动端访客管理", description = "移动端访客预约、签到、查询等API")
public class VisitorMobileController {

    @Resource
    private VisitorService visitorService;

    @Resource
    private VisitorAppointmentService visitorAppointmentService;

    @Resource
    private VisitorCheckInService visitorCheckInService;

    @Resource
    private VisitorQueryService visitorQueryService;

    @Resource
    private VisitorStatisticsService visitorStatisticsService;

    @Resource
    private VisitorExportService visitorExportService;

    @Resource
    private OcrService ocrService;

    /**
     * 获取预约详情
     *
     * @param appointmentId 预约ID
     * @return 预约详情
     */
    @Operation(summary = "获取预约详情", description = "根据预约ID获取预约详细信息")
    @GetMapping("/appointment/{appointmentId}")
    public ResponseDTO<VisitorAppointmentDetailVO> getAppointmentDetail(
            @PathVariable Long appointmentId) {
        log.info("获取预约详情, appointmentId={}", appointmentId);
        return visitorAppointmentService.getAppointmentDetail(appointmentId);
    }

    /**
     * 获取签到状态
     *
     * @param appointmentId 预约ID
     * @return 签到状态
     */
    @Operation(summary = "获取签到状态", description = "根据预约ID获取签到状态")
    @GetMapping("/checkin/status/{appointmentId}")
    public ResponseDTO<VisitorAppointmentDetailVO> getCheckInStatus(
            @PathVariable Long appointmentId) {
        log.info("获取签到状态, appointmentId={}", appointmentId);
        return visitorAppointmentService.getAppointmentDetail(appointmentId);
    }

    /**
     * 创建预约
     *
     * @param form 预约表单
     * @return 预约ID
     */
    @Operation(summary = "创建预约", description = "创建新的访客预约")
    @PostMapping("/appointment")
    public ResponseDTO<Long> createAppointment(@Valid @RequestBody VisitorMobileForm form) {
        log.info("创建预约, form={}", form);
        return visitorAppointmentService.createAppointment(form);
    }

    /**
     * 访客签到
     *
     * @param appointmentId 预约ID
     * @return 签到结果
     */
    @Operation(summary = "访客签到", description = "访客到达后进行签到")
    @PostMapping("/checkin/{appointmentId}")
    public ResponseDTO<Void> checkIn(@PathVariable Long appointmentId) {
        log.info("访客签到, appointmentId={}", appointmentId);
        return visitorCheckInService.checkIn(appointmentId);
    }

    /**
     * 访客签退
     *
     * @param appointmentId 预约ID
     * @return 签退结果
     */
    @Operation(summary = "访客签退", description = "访客离开后进行签退")
    @PostMapping("/checkout/{appointmentId}")
    public ResponseDTO<Void> checkOut(@PathVariable Long appointmentId) {
        log.info("访客签退, appointmentId={}", appointmentId);
        return visitorCheckInService.checkOut(appointmentId);
    }

    /**
     * 查询访客记录
     *
     * @param phone 手机号
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 访客记录列表
     */
    @Operation(summary = "查询访客记录", description = "根据手机号查询访客记录列表")
    @GetMapping("/records")
    public ResponseDTO<?> queryVisitorRecords(
            @RequestParam String phone,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("查询访客记录, phone={}, pageNum={}, pageSize={}", phone, pageNum, pageSize);
        return visitorQueryService.queryVisitorRecords(phone, pageNum, pageSize);
    }

    /**
     * 查询我的预约
     *
     * @param userId 用户ID
     * @param status 状态
     * @return 预约记录列表
     */
    @Operation(summary = "查询我的预约", description = "查询当前用户的预约记录")
    @GetMapping("/my-appointments")
    public ResponseDTO<?> queryMyAppointments(
            @RequestParam Long userId,
            @RequestParam(required = false) Integer status) {
        log.info("查询我的预约, userId={}, status={}", userId, status);
        return visitorQueryService.queryAppointments(userId, status);
    }

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    @Operation(summary = "获取统计数据", description = "获取访客统计数据")
    @GetMapping("/statistics")
    public ResponseDTO<?> getStatistics() {
        log.info("获取统计数据");
        return visitorStatisticsService.getStatistics();
    }

    /**
     * 更新预约状态（供审批结果监听器调用）
     *
     * @param appointmentId 预约ID
     * @param requestParams 请求参数（包含status和approvalComment）
     * @return 操作结果
     */
    @Operation(summary = "更新预约状态", description = "由审批结果监听器调用，更新预约状态")
    @PutMapping("/appointment/{appointmentId}/status")
    public ResponseDTO<Void> updateAppointmentStatus(
            @PathVariable Long appointmentId,
            @RequestBody Map<String, Object> requestParams) {
        log.info("[访客预约] 接收状态更新请求，appointmentId={}", appointmentId);
        String status = (String) requestParams.get("status");
        String approvalComment = (String) requestParams.get("approvalComment");
        if (visitorAppointmentService instanceof net.lab1024.sa.visitor.service.impl.VisitorAppointmentServiceImpl) {
            ((net.lab1024.sa.visitor.service.impl.VisitorAppointmentServiceImpl) visitorAppointmentService)
                    .updateAppointmentStatus(appointmentId, status, approvalComment);
        }
        return ResponseDTO.ok();
    }

    /**
     * OCR识别身份证
     * <p>
     * 移动端调用OCR服务识别身份证信息
     * 支持身份证正面和背面识别
     * </p>
     *
     * @param imageFile 身份证图片文件
     * @param cardSide 身份证面（FRONT-正面，BACK-背面）
     * @return 识别结果
     */
    @Operation(summary = "OCR识别身份证", description = "通过OCR识别身份证信息，支持正面和背面识别")
    @PostMapping("/ocr/idcard")
    public ResponseDTO<Map<String, Object>> recognizeIdCard(
            @RequestParam("file") MultipartFile imageFile,
            @RequestParam(value = "cardSide", defaultValue = "FRONT") String cardSide) {
        log.info("[移动端OCR] 识别身份证, cardSide={}, fileSize={}", cardSide, imageFile.getSize());
        try {
            Map<String, Object> result = ocrService.recognizeIdCard(imageFile, cardSide);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端OCR] 识别身份证失败", e);
            return ResponseDTO.error("OCR_RECOGNIZE_ERROR", "身份证识别失败：" + e.getMessage());
        }
    }
}
