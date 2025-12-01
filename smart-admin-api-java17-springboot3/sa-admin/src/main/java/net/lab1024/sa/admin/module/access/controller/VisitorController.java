package net.lab1024.sa.admin.module.access.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import net.lab1024.sa.base.common.annotation.SaCheckLogin;
import net.lab1024.sa.base.common.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorReservationEntity;
import net.lab1024.sa.admin.module.access.domain.entity.VisitorRecordEntity;
import net.lab1024.sa.admin.module.access.domain.form.VisitorReservationForm;
import net.lab1024.sa.admin.module.access.domain.vo.VisitorReservationVO;
import net.lab1024.sa.admin.module.access.domain.vo.VisitorRecordVO;
import net.lab1024.sa.admin.module.access.service.VisitorService;
import net.lab1024.sa.admin.module.access.manager.VisitorCacheManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 访客管理控制器
 * <p>
 * 严格遵循repowiki规范：
 * - 使用@Resource依赖注入
 * - Controller只做参数验证和调用Service
 * - 统一异常处理和响应格式
 * - 完整的权限控制和参数验证
 * - RESTful API设计
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/access/visitor")
@Tag(name = "访客管理", description = "访客管理相关接口")
@Validated
@SaCheckLogin
public class VisitorController {

    @Resource
    private VisitorService visitorService;

    @Resource
    private VisitorCacheManager visitorCacheManager;

    // ====================== 预约管理 ======================

    @PostMapping("/reservation/create")
    @Operation(summary = "创建访客预约")
    @SaCheckPermission("access:visitor:reservation:add")
    public ResponseDTO<String> createReservation(@Valid @RequestBody VisitorReservationForm form) {
        log.info("创建访客预约: {}", form.getVisitorName());

        // 使用SmartBeanUtil进行对象转换
        VisitorReservationEntity reservation = SmartBeanUtil.copy(form, VisitorReservationEntity.class);

        // 生成预约编号
        reservation.setReservationCode(generateReservationCode());
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        reservation.setApprovalStatus(0); // 待审批
        reservation.setDeletedFlag(0);

        return visitorService.createReservation(reservation);
    }

    @PostMapping("/reservation/update")
    @Operation(summary = "更新访客预约")
    @SaCheckPermission("access:visitor:reservation:edit")
    public ResponseDTO<String> updateReservation(@Valid @RequestBody VisitorReservationForm form) {
        log.info("更新访客预约: {}", form.getReservationId());

        VisitorReservationEntity reservation = SmartBeanUtil.copy(form, VisitorReservationEntity.class);
        reservation.setUpdateTime(LocalDateTime.now());

        return visitorService.updateReservation(reservation);
    }

    @PostMapping("/reservation/delete")
    @Operation(summary = "删除访客预约")
    @SaCheckPermission("access:visitor:reservation:delete")
    public ResponseDTO<String> deleteReservation(@Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId) {
        log.info("删除访客预约: {}", reservationId);
        return visitorService.deleteReservation(reservationId);
    }

    @GetMapping("/reservation/query")
    @Operation(summary = "查询访客预约列表")
    @SaCheckPermission("access:visitor:reservation:query")
    public ResponseDTO<PageResult<VisitorReservationVO>> getReservationPage(
            @Parameter(description = "分页参数") @ModelAttribute PageParam pageParam,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "审批状态") @RequestParam(required = false) Integer approvalStatus,
            @Parameter(description = "访问日期") @RequestParam(required = false) LocalDate visitDate) {

        log.info("查询访客预约列表: keyword={}, approvalStatus={}, visitDate={}", keyword, approvalStatus, visitDate);

        ResponseDTO<PageResult<VisitorReservationEntity>> result = visitorService.getReservationPage(
                pageParam, keyword, approvalStatus, visitDate);

        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        // 转换为VO对象
        PageResult<VisitorReservationEntity> entityPage = result.getData();
        PageResult<VisitorReservationVO> voPage = new PageResult<>();
        voPage.setPageNum(entityPage.getPageNum());
        voPage.setPageSize(entityPage.getPageSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setList(SmartBeanUtil.copyList(entityPage.getList(), VisitorReservationVO.class));

        return ResponseDTO.ok(voPage);
    }

    @GetMapping("/reservation/detail")
    @Operation(summary = "获取访客预约详情")
    @SaCheckPermission("access:visitor:reservation:query")
    public ResponseDTO<VisitorReservationVO> getReservationDetail(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId) {

        log.info("获取访客预约详情: {}", reservationId);

        ResponseDTO<VisitorReservationEntity> result = visitorService.getReservationById(reservationId);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        VisitorReservationVO vo = SmartBeanUtil.copy(result.getData(), VisitorReservationVO.class);
        return ResponseDTO.ok(vo);
    }

    @GetMapping("/reservation/by-code")
    @Operation(summary = "根据预约编号查询预约")
    @SaCheckPermission("access:visitor:reservation:query")
    public ResponseDTO<VisitorReservationVO> getReservationByCode(
            @Parameter(description = "预约编号") @NotNull @RequestParam String reservationCode) {

        log.info("根据编号查询访客预约: {}", reservationCode);

        ResponseDTO<VisitorReservationEntity> result = visitorService.getReservationByCode(reservationCode);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        VisitorReservationVO vo = SmartBeanUtil.copy(result.getData(), VisitorReservationVO.class);
        return ResponseDTO.ok(vo);
    }

    @PostMapping("/reservation/approve")
    @Operation(summary = "审批访客预约")
    @SaCheckPermission("access:visitor:reservation:approve")
    public ResponseDTO<String> approveReservation(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId,
            @Parameter(description = "审批状态") @NotNull @RequestParam Integer approvalStatus,
            @Parameter(description = "审批意见") @RequestParam(required = false) String approvalComment,
            @Parameter(description = "审批人ID") @NotNull @RequestParam Long approverId,
            @Parameter(description = "审批人姓名") @NotNull @RequestParam String approverName) {

        log.info("审批访客预约: {}, 状态: {}", reservationId, approvalStatus);
        return visitorService.approveReservation(reservationId, approvalStatus, approvalComment, approverId, approverName);
    }

    @PostMapping("/reservation/generate-qr")
    @Operation(summary = "生成访客二维码")
    @SaCheckPermission("access:visitor:reservation:qr")
    public ResponseDTO<Map<String, Object>> generateQrCode(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId) {

        log.info("生成访客二维码: {}", reservationId);

        ResponseDTO<String> result = visitorService.generateQrCode(reservationId);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        Map<String, Object> qrData = new HashMap<>();
        qrData.put("qrCode", result.getData());
        qrData.put("reservationId", reservationId);
        qrData.put("generateTime", LocalDateTime.now());
        qrData.put("expireTime", LocalDateTime.now().plusHours(24));

        return ResponseDTO.ok(qrData);
    }

    @PostMapping("/reservation/validate-qr")
    @Operation(summary = "验证访客二维码")
    @SaCheckPermission("access:visitor:reservation:validate")
    public ResponseDTO<VisitorReservationVO> validateQrCode(
            @Parameter(description = "二维码内容") @NotNull @RequestParam String qrCode) {

        log.info("验证访客二维码: {}", qrCode);

        ResponseDTO<VisitorReservationEntity> result = visitorService.validateQrCode(qrCode);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        VisitorReservationVO vo = SmartBeanUtil.copy(result.getData(), VisitorReservationVO.class);
        return ResponseDTO.ok(vo);
    }

    @PostMapping("/reservation/cancel")
    @Operation(summary = "取消访客预约")
    @SaCheckPermission("access:visitor:reservation:cancel")
    public ResponseDTO<String> cancelReservation(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId,
            @Parameter(description = "取消原因") @RequestParam(required = false) String cancelReason) {

        log.info("取消访客预约: {}, 原因: {}", reservationId, cancelReason);
        return visitorService.cancelReservation(reservationId, cancelReason);
    }

    // ====================== 访问记录管理 ======================

    @PostMapping("/record/create")
    @Operation(summary = "创建访问记录")
    @SaCheckPermission("access:visitor:record:add")
    public ResponseDTO<String> createAccessRecord(@Valid @RequestBody VisitorRecordEntity record) {
        log.info("创建访问记录: {}", record.getVisitorName());

        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());

        return visitorService.createAccessRecord(record);
    }

    @GetMapping("/record/query")
    @Operation(summary = "查询访问记录列表")
    @SaCheckPermission("access:visitor:record:query")
    public ResponseDTO<PageResult<VisitorRecordVO>> getAccessRecordPage(
            @Parameter(description = "分页参数") @ModelAttribute PageParam pageParam,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "访问区域ID") @RequestParam(required = false) Long visitAreaId,
            @Parameter(description = "通行结果") @RequestParam(required = false) Integer accessResult,
            @Parameter(description = "开始日期") @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) LocalDate endDate) {

        log.info("查询访问记录列表: keyword={}, visitAreaId={}, accessResult={}, startDate={}, endDate={}",
                keyword, visitAreaId, accessResult, startDate, endDate);

        ResponseDTO<PageResult<VisitorRecordEntity>> result = visitorService.getAccessRecordPage(
                pageParam, keyword, visitAreaId, accessResult, startDate, endDate);

        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        // 转换为VO对象
        PageResult<VisitorRecordEntity> entityPage = result.getData();
        PageResult<VisitorRecordVO> voPage = new PageResult<>();
        voPage.setPageNum(entityPage.getPageNum());
        voPage.setPageSize(entityPage.getPageSize());
        voPage.setTotal(entityPage.getTotal());
        voPage.setPages(entityPage.getPages());
        voPage.setList(SmartBeanUtil.copyList(entityPage.getList(), VisitorRecordVO.class));

        return ResponseDTO.ok(voPage);
    }

    @GetMapping("/record/by-phone")
    @Operation(summary = "根据手机号查询访问记录")
    @SaCheckPermission("access:visitor:record:query")
    public ResponseDTO<List<VisitorRecordVO>> getAccessRecordsByPhone(
            @Parameter(description = "访客手机号") @NotNull @RequestParam String visitorPhone,
            @Parameter(description = "查询数量限制") @Min(value = 1, message = "查询数量必须大于0") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("根据手机号查询访问记录: {}, limit={}", visitorPhone, limit);

        ResponseDTO<List<VisitorRecordEntity>> result = visitorService.getAccessRecordsByPhone(visitorPhone, limit);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        List<VisitorRecordVO> voList = SmartBeanUtil.copyList(result.getData(), VisitorRecordVO.class);
        return ResponseDTO.ok(voList);
    }

    @GetMapping("/record/by-reservation")
    @Operation(summary = "根据预约ID查询访问记录")
    @SaCheckPermission("access:visitor:record:query")
    public ResponseDTO<List<VisitorRecordVO>> getAccessRecordsByReservation(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId) {

        log.info("根据预约ID查询访问记录: {}", reservationId);

        ResponseDTO<List<VisitorRecordEntity>> result = visitorService.getAccessRecordsByReservation(reservationId);
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        List<VisitorRecordVO> voList = SmartBeanUtil.copyList(result.getData(), VisitorRecordVO.class);
        return ResponseDTO.ok(voList);
    }

    @GetMapping("/record/today")
    @Operation(summary = "查询今日访问记录")
    @SaCheckPermission("access:visitor:record:query")
    public ResponseDTO<List<VisitorRecordVO>> getTodayAccessRecords() {
        log.info("查询今日访问记录");

        ResponseDTO<List<VisitorRecordEntity>> result = visitorService.getTodayAccessRecords();
        if (!result.getOk()) {
            return ResponseDTO.error(result.getMsg());
        }

        List<VisitorRecordVO> voList = SmartBeanUtil.copyList(result.getData(), VisitorRecordVO.class);
        return ResponseDTO.ok(voList);
    }

    // ====================== 统计分析 ======================

    @GetMapping("/statistics")
    @Operation(summary = "获取访客统计数据")
    @SaCheckPermission("access:visitor:statistics")
    public ResponseDTO<Map<String, Object>> getVisitorStatistics(
            @Parameter(description = "开始日期") @NotNull @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @NotNull @RequestParam LocalDate endDate) {

        log.info("获取访客统计数据: {} - {}", startDate, endDate);
        return visitorService.getVisitorStatistics(startDate, endDate);
    }

    @GetMapping("/statistics/trend")
    @Operation(summary = "获取访问趋势数据")
    @SaCheckPermission("access:visitor:statistics")
    public ResponseDTO<List<Map<String, Object>>> getAccessTrend(
            @Parameter(description = "天数") @Min(value = 1, message = "天数必须大于0") @RequestParam(defaultValue = "7") Integer days) {

        log.info("获取访问趋势数据: {} 天", days);
        return visitorService.getAccessTrend(days);
    }

    @GetMapping("/statistics/area")
    @Operation(summary = "获取区域访问统计")
    @SaCheckPermission("access:visitor:statistics")
    public ResponseDTO<List<Map<String, Object>>> getAreaAccessStatistics(
            @Parameter(description = "开始日期") @NotNull @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @NotNull @RequestParam LocalDate endDate) {

        log.info("获取区域访问统计: {} - {}", startDate, endDate);
        return visitorService.getAreaAccessStatistics(startDate, endDate);
    }

    @GetMapping("/statistics/device")
    @Operation(summary = "获取设备使用统计")
    @SaCheckPermission("access:visitor:statistics")
    public ResponseDTO<List<Map<String, Object>>> getDeviceUsageStatistics(
            @Parameter(description = "开始日期") @NotNull @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @NotNull @RequestParam LocalDate endDate) {

        log.info("获取设备使用统计: {} - {}", startDate, endDate);
        return visitorService.getDeviceUsageStatistics(startDate, endDate);
    }

    @GetMapping("/statistics/abnormal")
    @Operation(summary = "获取异常访问数据")
    @SaCheckPermission("access:visitor:statistics")
    public ResponseDTO<List<Map<String, Object>>> getAbnormalAccessData(
            @Parameter(description = "时间窗口(分钟)") @Min(value = 1, message = "时间窗口必须大于0") @RequestParam(defaultValue = "30") Integer timeWindow,
            @Parameter(description = "阈值") @Min(value = 2, message = "阈值必须大于1") @RequestParam(defaultValue = "3") Integer threshold) {

        log.info("获取异常访问数据: timeWindow={}, threshold={}", timeWindow, threshold);
        return visitorService.getAbnormalAccessData(timeWindow, threshold);
    }

    // ====================== 业务验证 ======================

    @PostMapping("/validate/permission")
    @Operation(summary = "验证访客访问权限")
    @SaCheckPermission("access:visitor:validate")
    public ResponseDTO<Map<String, Object>> validateAccessPermission(
            @Parameter(description = "预约ID") @NotNull @RequestParam Long reservationId,
            @Parameter(description = "设备ID") @RequestParam(required = false) Long deviceId,
            @Parameter(description = "区域ID") @RequestParam(required = false) Long areaId) {

        log.info("验证访客访问权限: reservationId={}, deviceId={}, areaId={}", reservationId, deviceId, areaId);
        return visitorService.validateAccessPermission(reservationId, deviceId, areaId);
    }

    @PostMapping("/validate/blacklist")
    @Operation(summary = "检查访客黑名单")
    @SaCheckPermission("access:visitor:validate")
    public ResponseDTO<Map<String, Object>> checkVisitorBlacklist(
            @Parameter(description = "访客姓名") @NotNull @RequestParam String visitorName,
            @Parameter(description = "访客手机号") @NotNull @RequestParam String visitorPhone,
            @Parameter(description = "访客身份证号") @RequestParam(required = false) String visitorIdCard) {

        log.info("检查访客黑名单: name={}, phone={}", visitorName, visitorPhone);
        return visitorService.checkVisitorBlacklist(visitorName, visitorPhone, visitorIdCard);
    }

    @PostMapping("/validate/duplicate")
    @Operation(summary = "检查重复预约")
    @SaCheckPermission("access:visitor:validate")
    public ResponseDTO<Boolean> checkDuplicateReservation(
            @Parameter(description = "访客手机号") @NotNull @RequestParam String visitorPhone,
            @Parameter(description = "访问日期") @NotNull @RequestParam LocalDate visitDate,
            @Parameter(description = "排除的预约ID") @RequestParam(required = false) Long excludeId) {

        log.info("检查重复预约: phone={}, date={}, excludeId={}", visitorPhone, visitDate, excludeId);
        return visitorService.checkDuplicateReservation(visitorPhone, visitDate, excludeId);
    }

    // ====================== 导出功能 ======================

    @PostMapping("/export/reservation")
    @Operation(summary = "导出访客预约数据")
    @SaCheckPermission("access:visitor:export")
    public ResponseDTO<String> exportReservationData(
            @Parameter(description = "开始日期") @NotNull @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @NotNull @RequestParam LocalDate endDate,
            @Parameter(description = "审批状态") @RequestParam(required = false) Integer approvalStatus) {

        log.info("导出访客预约数据: {} - {}, status={}", startDate, endDate, approvalStatus);
        return visitorService.exportReservationData(startDate, endDate, approvalStatus);
    }

    @PostMapping("/export/record")
    @Operation(summary = "导出访问记录数据")
    @SaCheckPermission("access:visitor:export")
    public ResponseDTO<String> exportAccessRecordData(
            @Parameter(description = "开始日期") @NotNull @RequestParam LocalDate startDate,
            @Parameter(description = "结束日期") @NotNull @RequestParam LocalDate endDate,
            @Parameter(description = "访问区域ID") @RequestParam(required = false) Long visitAreaId,
            @Parameter(description = "通行结果") @RequestParam(required = false) Integer accessResult) {

        log.info("导出访问记录数据: {} - {}, areaId={}, result={}", startDate, endDate, visitAreaId, accessResult);
        return visitorService.exportAccessRecordData(startDate, endDate, visitAreaId, accessResult);
    }

    // ====================== 辅助方法 ======================

    /**
     * 生成预约编号
     */
    private String generateReservationCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = String.valueOf((int) (Math.random() * 10000));
        return "VIS" + timestamp.substring(timestamp.length() - 10) + randomStr;
    }
}