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

import io.micrometer.observation.annotation.Observed;
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
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.exception.SystemException;
import net.lab1024.sa.common.exception.ParamException;
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
    @Observed(name = "visitor.mobile.getAppointmentDetail", contextualName = "visitor-mobile-get-appointment-detail")
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
    @Observed(name = "visitor.mobile.getCheckInStatus", contextualName = "visitor-mobile-get-checkin-status")
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
    @Observed(name = "visitor.mobile.createAppointment", contextualName = "visitor-mobile-create-appointment")
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
    @Observed(name = "visitor.mobile.checkIn", contextualName = "visitor-mobile-checkin")
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
    @Observed(name = "visitor.mobile.checkOut", contextualName = "visitor-mobile-checkout")
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
    @Observed(name = "visitor.mobile.queryVisitorRecords", contextualName = "visitor-mobile-query-records")
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
    @Observed(name = "visitor.mobile.queryMyAppointments", contextualName = "visitor-mobile-query-my-appointments")
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
    @Observed(name = "visitor.mobile.getStatistics", contextualName = "visitor-mobile-get-statistics")
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
    @Observed(name = "visitor.mobile.updateAppointmentStatus", contextualName = "visitor-mobile-update-status")
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
     * 获取个人访问统计
     * <p>
     * 统计用户作为被访人的访客数据
     * </p>
     *
     * @param userId 用户ID
     * @return 个人统计数据
     */
    @Observed(name = "visitor.mobile.getPersonalStatistics", contextualName = "visitor-mobile-personal-statistics")
    @Operation(summary = "获取个人访问统计", description = "获取用户作为被访人的访客统计数据")
    @GetMapping("/statistics/{userId}")
    public ResponseDTO<Map<String, Object>> getPersonalStatistics(@PathVariable Long userId) {
        log.info("[移动端访客] 获取个人访问统计，userId={}", userId);
        try {
            return visitorStatisticsService.getPersonalStatistics(userId);
        } catch (ParamException e) {
            log.warn("[移动端访客] 获取个人访问统计参数错误: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端访客] 获取个人访问统计业务异常: userId={}, error={}", userId, e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端访客] 获取个人访问统计系统异常: userId={}, error={}", userId, e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[移动端访客] 获取个人访问统计未知异常: userId={}", userId, e);
            return ResponseDTO.error("GET_PERSONAL_STATISTICS_ERROR", "获取个人统计数据失败");
        }
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
    @Observed(name = "visitor.mobile.recognizeIdCard", contextualName = "visitor-mobile-ocr-idcard")
    @Operation(summary = "OCR识别身份证", description = "通过OCR识别身份证信息，支持正面和背面识别")
    @PostMapping("/ocr/idcard")
    public ResponseDTO<Map<String, Object>> recognizeIdCard(
            @RequestParam("file") MultipartFile imageFile,
            @RequestParam(value = "cardSide", defaultValue = "FRONT") String cardSide) {
        log.info("[移动端OCR] 识别身份证, cardSide={}, fileSize={}", cardSide, imageFile.getSize());
        try {
            Map<String, Object> result = ocrService.recognizeIdCard(imageFile, cardSide);
            return ResponseDTO.ok(result);
        } catch (ParamException e) {
            log.warn("[移动端OCR] 识别身份证参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端OCR] 识别身份证业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (SystemException e) {
            log.error("[移动端OCR] 识别身份证系统异常: {}", e.getMessage(), e);
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[移动端OCR] 识别身份证执行异常: {}", e.getMessage(), e);
            return ResponseDTO.error("OCR_RECOGNIZE_ERROR", "身份证识别失败");
        }
    }

    /**
     * 获取访问区域列表
     *
     * @return 区域列表
     */
    @Observed(name = "visitor.mobile.getVisitAreas", contextualName = "visitor-mobile-get-areas")
    @Operation(summary = "获取访问区域列表", description = "获取可访问的区域列表")
    @GetMapping("/areas")
    public ResponseDTO<java.util.List<Map<String, Object>>> getVisitAreas() {
        log.info("[移动端访客] 获取访问区域列表");
        try {
            // 返回模拟数据，实际应从区域服务获取
            java.util.List<Map<String, Object>> areas = new java.util.ArrayList<>();
            Map<String, Object> area1 = new java.util.HashMap<>();
            area1.put("areaId", "AREA001");
            area1.put("areaName", "A栋办公区");
            area1.put("description", "A栋1-10层办公区域");
            areas.add(area1);

            Map<String, Object> area2 = new java.util.HashMap<>();
            area2.put("areaId", "AREA002");
            area2.put("areaName", "B栋办公区");
            area2.put("description", "B栋1-8层办公区域");
            areas.add(area2);

            Map<String, Object> area3 = new java.util.HashMap<>();
            area3.put("areaId", "AREA003");
            area3.put("areaName", "会议中心");
            area3.put("description", "园区会议中心");
            areas.add(area3);

            return ResponseDTO.ok(areas);
        } catch (Exception e) {
            log.error("[移动端访客] 获取访问区域列表失败", e);
            return ResponseDTO.error("GET_AREAS_ERROR", "获取区域列表失败");
        }
    }

    /**
     * 获取预约类型列表
     *
     * @return 预约类型列表
     */
    @Observed(name = "visitor.mobile.getAppointmentTypes", contextualName = "visitor-mobile-get-appointment-types")
    @Operation(summary = "获取预约类型列表", description = "获取可选的预约类型")
    @GetMapping("/appointment-types")
    public ResponseDTO<java.util.List<Map<String, Object>>> getAppointmentTypes() {
        log.info("[移动端访客] 获取预约类型列表");
        try {
            java.util.List<Map<String, Object>> types = new java.util.ArrayList<>();

            Map<String, Object> type1 = new java.util.HashMap<>();
            type1.put("typeCode", "BUSINESS");
            type1.put("typeName", "商务拜访");
            type1.put("description", "商务洽谈、合作会议等");
            types.add(type1);

            Map<String, Object> type2 = new java.util.HashMap<>();
            type2.put("typeCode", "INTERVIEW");
            type2.put("typeName", "面试");
            type2.put("description", "应聘面试");
            types.add(type2);

            Map<String, Object> type3 = new java.util.HashMap<>();
            type3.put("typeCode", "DELIVERY");
            type3.put("typeName", "快递/外卖");
            type3.put("description", "快递配送、外卖送餐");
            types.add(type3);

            Map<String, Object> type4 = new java.util.HashMap<>();
            type4.put("typeCode", "MAINTENANCE");
            type4.put("typeName", "维修/施工");
            type4.put("description", "设备维修、工程施工");
            types.add(type4);

            Map<String, Object> type5 = new java.util.HashMap<>();
            type5.put("typeCode", "OTHER");
            type5.put("typeName", "其他");
            type5.put("description", "其他类型访问");
            types.add(type5);

            return ResponseDTO.ok(types);
        } catch (Exception e) {
            log.error("[移动端访客] 获取预约类型列表失败", e);
            return ResponseDTO.error("GET_APPOINTMENT_TYPES_ERROR", "获取预约类型列表失败");
        }
    }

    /**
     * 导出访问记录
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 导出文件路径
     */
    @Observed(name = "visitor.mobile.exportRecords", contextualName = "visitor-mobile-export-records")
    @Operation(summary = "导出访问记录", description = "导出指定时间范围的访问记录")
    @GetMapping("/export")
    public ResponseDTO<String> exportRecords(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("[移动端访客] 导出访问记录，startDate={}, endDate={}", startDate, endDate);
        try {
            String exportPath = visitorExportService.exportRecords(startDate, endDate);
            return ResponseDTO.ok(exportPath);
        } catch (ParamException e) {
            log.warn("[移动端访客] 导出访问记录参数错误: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (BusinessException e) {
            log.warn("[移动端访客] 导出访问记录业务异常: {}", e.getMessage());
            return ResponseDTO.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("[移动端访客] 导出访问记录失败", e);
            return ResponseDTO.error("EXPORT_RECORDS_ERROR", "导出访问记录失败");
        }
    }

    /**
     * 获取帮助信息
     *
     * @param helpType 帮助类型
     * @return 帮助信息
     */
    @Observed(name = "visitor.mobile.getHelpInfo", contextualName = "visitor-mobile-get-help")
    @Operation(summary = "获取帮助信息", description = "获取访客系统帮助信息")
    @GetMapping("/help")
    public ResponseDTO<Map<String, Object>> getHelpInfo(
            @RequestParam(required = false) String helpType) {
        log.info("[移动端访客] 获取帮助信息，helpType={}", helpType);
        try {
            Map<String, Object> helpInfo = new java.util.HashMap<>();
            helpInfo.put("title", "访客预约帮助");
            helpInfo.put("content", "1. 填写访客信息\n2. 选择被访人\n3. 选择预约时间\n4. 提交预约申请\n5. 等待审批通过\n6. 到访时出示预约码签到");
            helpInfo.put("contactPhone", "400-888-8888");
            helpInfo.put("contactEmail", "support@ioe-dream.com");
            return ResponseDTO.ok(helpInfo);
        } catch (Exception e) {
            log.error("[移动端访客] 获取帮助信息失败", e);
            return ResponseDTO.error("GET_HELP_ERROR", "获取帮助信息失败");
        }
    }

    /**
     * 验证访客信息
     *
     * @param requestParams 请求参数
     * @return 验证结果
     */
    @Observed(name = "visitor.mobile.validateVisitorInfo", contextualName = "visitor-mobile-validate")
    @Operation(summary = "验证访客信息", description = "验证访客身份证号和手机号")
    @PostMapping("/validate")
    public ResponseDTO<Map<String, Object>> validateVisitorInfo(@RequestBody Map<String, String> requestParams) {
        String idCardNumber = requestParams.get("idCardNumber");
        String phoneNumber = requestParams.get("phoneNumber");
        log.info("[移动端访客] 验证访客信息，idCardNumber={}, phoneNumber={}",
                idCardNumber != null ? idCardNumber.substring(0, 6) + "****" : null,
                phoneNumber != null ? phoneNumber.substring(0, 3) + "****" : null);
        try {
            Map<String, Object> result = new java.util.HashMap<>();
            // 简单验证逻辑
            boolean idCardValid = idCardNumber != null && idCardNumber.length() == 18;
            boolean phoneValid = phoneNumber != null && phoneNumber.length() == 11;
            result.put("idCardValid", idCardValid);
            result.put("phoneValid", phoneValid);
            result.put("valid", idCardValid && phoneValid);
            return ResponseDTO.ok(result);
        } catch (Exception e) {
            log.error("[移动端访客] 验证访客信息失败", e);
            return ResponseDTO.error("VALIDATE_ERROR", "验证访客信息失败");
        }
    }

    /**
     * 获取被访人信息
     *
     * @param userId 被访人ID
     * @return 被访人信息
     */
    @Observed(name = "visitor.mobile.getVisiteeInfo", contextualName = "visitor-mobile-get-visitee")
    @Operation(summary = "获取被访人信息", description = "根据用户ID获取被访人信息")
    @GetMapping("/visitee/{userId}")
    public ResponseDTO<Map<String, Object>> getVisiteeInfo(@PathVariable Long userId) {
        log.info("[移动端访客] 获取被访人信息，userId={}", userId);
        try {
            // 实际应从用户服务获取
            Map<String, Object> visiteeInfo = new java.util.HashMap<>();
            visiteeInfo.put("userId", userId);
            visiteeInfo.put("userName", "员工" + userId);
            visiteeInfo.put("department", "技术部");
            visiteeInfo.put("phone", "138****8888");
            visiteeInfo.put("email", "user" + userId + "@company.com");
            return ResponseDTO.ok(visiteeInfo);
        } catch (Exception e) {
            log.error("[移动端访客] 获取被访人信息失败，userId={}", userId, e);
            return ResponseDTO.error("GET_VISITEE_ERROR", "获取被访人信息失败");
        }
    }
}

