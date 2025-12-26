package net.lab1024.sa.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeApplyDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeApprovalDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeRecordDao;
import net.lab1024.sa.attendance.dao.AttendanceOvertimeRuleDao;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceOvertimeApplyVO;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeApplyEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeApprovalEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeRecordEntity;
import net.lab1024.sa.common.entity.attendance.AttendanceOvertimeRuleEntity;
import net.lab1024.sa.attendance.service.AttendanceOvertimeApplyService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.exception.BusinessException;
import net.lab1024.sa.common.util.SmartBeanUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 加班申请Service实现类
 * <p>
 * 实现加班申请的核心业务逻辑
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@Service
public class AttendanceOvertimeApplyServiceImpl implements AttendanceOvertimeApplyService {

    @Resource
    private AttendanceOvertimeApplyDao overtimeApplyDao;

    @Resource
    private AttendanceOvertimeApprovalDao overtimeApprovalDao;

    @Resource
    private AttendanceOvertimeRecordDao overtimeRecordDao;

    @Resource
    private AttendanceOvertimeRuleDao overtimeRuleDao;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public PageResult<AttendanceOvertimeApplyVO> queryPage(AttendanceOvertimeApplyQueryForm form) {
        log.info("[加班申请服务] 分页查询加班申请: form={}", form);

        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (form.getApplicantId() != null) {
            queryWrapper.eq(AttendanceOvertimeApplyEntity::getApplicantId, form.getApplicantId());
        }
        if (form.getDepartmentId() != null) {
            queryWrapper.eq(AttendanceOvertimeApplyEntity::getDepartmentId, form.getDepartmentId());
        }
        if (form.getOvertimeType() != null) {
            queryWrapper.eq(AttendanceOvertimeApplyEntity::getOvertimeType, form.getOvertimeType());
        }
        if (form.getApplyStatus() != null) {
            queryWrapper.eq(AttendanceOvertimeApplyEntity::getApplyStatus, form.getApplyStatus());
        }
        if (form.getStartDate() != null) {
            queryWrapper.ge(AttendanceOvertimeApplyEntity::getOvertimeDate, form.getStartDate());
        }
        if (form.getEndDate() != null) {
            queryWrapper.le(AttendanceOvertimeApplyEntity::getOvertimeDate, form.getEndDate());
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(AttendanceOvertimeApplyEntity::getCreateTime);

        // 分页查询
        Page<AttendanceOvertimeApplyEntity> page = overtimeApplyDao.selectPage(
            new Page<>(form.getPageNum(), form.getPageSize()),
            queryWrapper
        );

        // 转换为VO
        List<AttendanceOvertimeApplyVO> voList = page.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        log.info("[加班申请服务] 分页查询完成: total={}, records={}", page.getTotal(), voList.size());
        return PageResult.of(voList, page.getTotal(), form.getPageNum(), form.getPageSize());
    }

    @Override
    public AttendanceOvertimeApplyVO queryDetail(Long applyId) {
        log.info("[加班申请服务] 查询加班申请详情: applyId={}", applyId);

        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        AttendanceOvertimeApplyVO vo = convertToVO(entity);

        // 查询审批记录
        List<AttendanceOvertimeApprovalEntity> approvalList = overtimeApprovalDao.selectByApplyId(applyId);
        vo.setApprovalList(new ArrayList<>(approvalList));

        log.info("[加班申请服务] 查询详情完成: applyNo={}", vo.getApplyNo());
        return vo;
    }

    @Override
    public AttendanceOvertimeApplyVO queryByApplyNo(String applyNo) {
        log.info("[加班申请服务] 根据申请编号查询: applyNo={}", applyNo);

        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectByApplyNo(applyNo);
        if (entity == null) {
            log.warn("[加班申请服务] 申请编号不存在: applyNo={}", applyNo);
            throw new BusinessException("APPLY_NO_NOT_FOUND", "申请编号不存在");
        }

        return convertToVO(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(AttendanceOvertimeApplyAddForm form) {
        log.info("[加班申请服务] 新增加班申请: form={}", form);

        // 1. 生成申请编号
        String applyNo = generateApplyNo(form.getOvertimeDate());

        // 2. 检查重复申请
        List<AttendanceOvertimeApplyEntity> duplicateApplies = overtimeApplyDao.selectDuplicateApply(
            form.getApplicantId(), form.getOvertimeDate()
        );
        if (!duplicateApplies.isEmpty()) {
            log.warn("[加班申请服务] 存在重复申请: applicantId={}, date={}", form.getApplicantId(), form.getOvertimeDate());
            throw new BusinessException("DUPLICATE_OVERTIME_APPLY", "该日期已存在加班申请");
        }

        // 3. 转换为Entity
        AttendanceOvertimeApplyEntity entity = SmartBeanUtil.copy(form, AttendanceOvertimeApplyEntity.class);
        entity.setApplyNo(applyNo);
        entity.setApplyStatus("DRAFT"); // 初始状态为草稿
        entity.setApprovalLevel(0); // 初始审批层级为0

        // 4. 保存
        int result = overtimeApplyDao.insert(entity);
        if (result <= 0) {
            log.error("[加班申请服务] 新增加班申请失败: form={}", form);
            throw new BusinessException("ADD_OVERTIME_APPLY_FAILED", "新增加班申请失败");
        }

        log.info("[加班申请服务] 新增加班申请成功: applyId={}, applyNo={}", entity.getApplyId(), entity.getApplyNo());
        return entity.getApplyId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long applyId, AttendanceOvertimeApplyUpdateForm form) {
        log.info("[加班申请服务] 更新加班申请: applyId={}, form={}", applyId, form);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity existingEntity = overtimeApplyDao.selectById(applyId);
        if (existingEntity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 只有草稿状态才能修改
        if (!"DRAFT".equals(existingEntity.getApplyStatus())) {
            log.warn("[加班申请服务] 只有草稿状态才能修改: currentStatus={}", existingEntity.getApplyStatus());
            throw new BusinessException("STATUS_NOT_ALLOW_UPDATE", "当前状态不允许修改");
        }

        // 3. 更新
        AttendanceOvertimeApplyEntity entity = SmartBeanUtil.copy(form, AttendanceOvertimeApplyEntity.class);
        entity.setApplyId(applyId);

        int result = overtimeApplyDao.updateById(entity);
        if (result <= 0) {
            log.error("[加班申请服务] 更新加班申请失败: applyId={}", applyId);
            throw new BusinessException("UPDATE_OVERTIME_APPLY_FAILED", "更新加班申请失败");
        }

        log.info("[加班申请服务] 更新加班申请成功: applyId={}", applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long applyId) {
        log.info("[加班申请服务] 删除加班申请: applyId={}", applyId);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 只有草稿或已撤销状态才能删除
        if (!("DRAFT".equals(entity.getApplyStatus()) || "CANCELLED".equals(entity.getApplyStatus()))) {
            log.warn("[加班申请服务] 当前状态不允许删除: currentStatus={}", entity.getApplyStatus());
            throw new BusinessException("STATUS_NOT_ALLOW_DELETE", "当前状态不允许删除");
        }

        // 3. 逻辑删除
        int result = overtimeApplyDao.deleteById(applyId);
        if (result <= 0) {
            log.error("[加班申请服务] 删除加班申请失败: applyId={}", applyId);
            throw new BusinessException("DELETE_OVERTIME_APPLY_FAILED", "删除加班申请失败");
        }

        log.info("[加班申请服务] 删除加班申请成功: applyId={}", applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> applyIds) {
        log.info("[加班申请服务] 批量删除加班申请: applyIds={}", applyIds);

        if (applyIds == null || applyIds.isEmpty()) {
            throw new BusinessException("PARAM_ERROR", "申请ID列表不能为空");
        }

        for (Long applyId : applyIds) {
            delete(applyId);
        }

        log.info("[加班申请服务] 批量删除完成: count={}", applyIds.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long applyId) {
        log.info("[加班申请服务] 提交加班申请: applyId={}", applyId);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 只有草稿状态才能提交
        if (!"DRAFT".equals(entity.getApplyStatus())) {
            log.warn("[加班申请服务] 只有草稿状态才能提交: currentStatus={}", entity.getApplyStatus());
            throw new BusinessException("STATUS_NOT_ALLOW_SUBMIT", "当前状态不允许提交");
        }

        // 3. 更新状态为待审批
        LambdaUpdateWrapper<AttendanceOvertimeApplyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttendanceOvertimeApplyEntity::getApplyId, applyId)
            .set(AttendanceOvertimeApplyEntity::getApplyStatus, "PENDING")
            .set(AttendanceOvertimeApplyEntity::getApprovalLevel, 1);

        int result = overtimeApplyDao.update(null, updateWrapper);
        if (result <= 0) {
            log.error("[加班申请服务] 提交加班申请失败: applyId={}", applyId);
            throw new BusinessException("SUBMIT_OVERTIME_APPLY_FAILED", "提交加班申请失败");
        }

        log.info("[加班申请服务] 提交加班申请成功: applyId={}", applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long applyId, String cancelReason) {
        log.info("[加班申请服务] 撤销加班申请: applyId={}, reason={}", applyId, cancelReason);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 只有待审批状态才能撤销
        if (!"PENDING".equals(entity.getApplyStatus())) {
            log.warn("[加班申请服务] 只有待审批状态才能撤销: currentStatus={}", entity.getApplyStatus());
            throw new BusinessException("STATUS_NOT_ALLOW_CANCEL", "当前状态不允许撤销");
        }

        // 3. 更新状态为已撤销
        LambdaUpdateWrapper<AttendanceOvertimeApplyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttendanceOvertimeApplyEntity::getApplyId, applyId)
            .set(AttendanceOvertimeApplyEntity::getApplyStatus, "CANCELLED")
            .set(AttendanceOvertimeApplyEntity::getFinalApprovalComment, cancelReason);

        int result = overtimeApplyDao.update(null, updateWrapper);
        if (result <= 0) {
            log.error("[加班申请服务] 撤销加班申请失败: applyId={}", applyId);
            throw new BusinessException("CANCEL_OVERTIME_APPLY_FAILED", "撤销加班申请失败");
        }

        // 4. 记录撤销操作
        AttendanceOvertimeApprovalEntity approval = new AttendanceOvertimeApprovalEntity();
        approval.setApplyId(applyId);
        approval.setApplyNo(entity.getApplyNo());
        approval.setApproverId(entity.getApplicantId());
        approval.setApproverName(entity.getApplicantName());
        approval.setApprovalLevel(entity.getApprovalLevel());
        approval.setApprovalAction("CANCEL");
        approval.setApprovalComment(cancelReason);
        approval.setApprovalTime(LocalDateTime.now());

        overtimeApprovalDao.insert(approval);

        log.info("[加班申请服务] 撤销加班申请成功: applyId={}", applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long applyId, Integer approvalLevel, Long approverId, String approvalComment) {
        log.info("[加班申请服务] 审批通过: applyId={}, level={}, approverId={}", applyId, approvalLevel, approverId);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 检查审批层级是否匹配
        if (!entity.getApprovalLevel().equals(approvalLevel)) {
            log.warn("[加班申请服务] 审批层级不匹配: expected={}, actual={}", entity.getApprovalLevel(), approvalLevel);
            throw new BusinessException("APPROVAL_LEVEL_NOT_MATCH", "审批层级不匹配");
        }

        // 3. 记录审批信息
        AttendanceOvertimeApprovalEntity approval = new AttendanceOvertimeApprovalEntity();
        approval.setApplyId(applyId);
        approval.setApplyNo(entity.getApplyNo());
        approval.setApproverId(approverId);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprovalAction("APPROVE");
        approval.setApprovalComment(approvalComment);
        approval.setApprovalTime(LocalDateTime.now());

        overtimeApprovalDao.insert(approval);

        // 4. 更新申请状态（假设为单级审批，直接批准）
        LambdaUpdateWrapper<AttendanceOvertimeApplyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttendanceOvertimeApplyEntity::getApplyId, applyId)
            .set(AttendanceOvertimeApplyEntity::getApplyStatus, "APPROVED")
            .set(AttendanceOvertimeApplyEntity::getFinalApproverId, approverId)
            .set(AttendanceOvertimeApplyEntity::getFinalApprovalComment, approvalComment)
            .set(AttendanceOvertimeApplyEntity::getFinalApprovalTime, LocalDateTime.now());

        int result = overtimeApplyDao.update(null, updateWrapper);
        if (result <= 0) {
            log.error("[加班申请服务] 审批失败: applyId={}", applyId);
            throw new BusinessException("APPROVE_OVERTIME_APPLY_FAILED", "审批失败");
        }

        log.info("[加班申请服务] 审批通过成功: applyId={}", applyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long applyId, Integer approvalLevel, Long approverId, String rejectReason) {
        log.info("[加班申请服务] 审批驳回: applyId={}, level={}, approverId={}, reason={}", applyId, approvalLevel, approverId, rejectReason);

        // 1. 检查申请是否存在
        AttendanceOvertimeApplyEntity entity = overtimeApplyDao.selectById(applyId);
        if (entity == null) {
            log.warn("[加班申请服务] 加班申请不存在: applyId={}", applyId);
            throw new BusinessException("OVERTIME_APPLY_NOT_FOUND", "加班申请不存在");
        }

        // 2. 记录驳回信息
        AttendanceOvertimeApprovalEntity approval = new AttendanceOvertimeApprovalEntity();
        approval.setApplyId(applyId);
        approval.setApplyNo(entity.getApplyNo());
        approval.setApproverId(approverId);
        approval.setApprovalLevel(approvalLevel);
        approval.setApprovalAction("REJECT");
        approval.setApprovalComment(rejectReason);
        approval.setApprovalTime(LocalDateTime.now());

        overtimeApprovalDao.insert(approval);

        // 3. 更新申请状态为已驳回
        LambdaUpdateWrapper<AttendanceOvertimeApplyEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(AttendanceOvertimeApplyEntity::getApplyId, applyId)
            .set(AttendanceOvertimeApplyEntity::getApplyStatus, "REJECTED")
            .set(AttendanceOvertimeApplyEntity::getFinalApproverId, approverId)
            .set(AttendanceOvertimeApplyEntity::getFinalApprovalComment, rejectReason)
            .set(AttendanceOvertimeApplyEntity::getFinalApprovalTime, LocalDateTime.now());

        int result = overtimeApplyDao.update(null, updateWrapper);
        if (result <= 0) {
            log.error("[加班申请服务] 驳回失败: applyId={}", applyId);
            throw new BusinessException("REJECT_OVERTIME_APPLY_FAILED", "驳回失败");
        }

        log.info("[加班申请服务] 审批驳回成功: applyId={}", applyId);
    }

    @Override
    public List<AttendanceOvertimeApplyVO> queryMyApplications(Long applicantId) {
        log.info("[加班申请服务] 查询我的加班申请: applicantId={}", applicantId);

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectByApplicantId(applicantId);
        return entityList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceOvertimeApplyVO> queryPendingApprovals(Long approverId) {
        log.info("[加班申请服务] 查询待我审批的申请: approverId={}", approverId);

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectPendingApprovalsByApprover(approverId);
        return entityList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceOvertimeApplyVO> queryByDepartmentAndDateRange(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[加班申请服务] 按部门和日期查询: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceOvertimeApplyEntity::getDepartmentId, departmentId)
            .ge(AttendanceOvertimeApplyEntity::getOvertimeDate, startDate)
            .le(AttendanceOvertimeApplyEntity::getOvertimeDate, endDate)
            .orderByDesc(AttendanceOvertimeApplyEntity::getOvertimeDate);

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectList(queryWrapper);
        return entityList.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    @Override
    public Integer countByApplicantAndStatus(Long applicantId, String applyStatus) {
        log.info("[加班申请服务] 统计申请数量: applicantId={}, status={}", applicantId, applyStatus);
        return overtimeApplyDao.countByApplicantAndStatus(applicantId, applyStatus);
    }

    @Override
    public BigDecimal sumOvertimeHoursByDepartment(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[加班申请服务] 统计部门加班时长: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return overtimeApplyDao.sumOvertimeHoursByDepartment(departmentId, startDateTime, endDateTime);
    }

    @Override
    public Map<String, Object> generateDepartmentStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[加班申请服务] 生成部门统计报表: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        // 查询日期范围内的所有已批准申请
        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceOvertimeApplyEntity::getApplyStatus, "APPROVED")
            .ge(AttendanceOvertimeApplyEntity::getOvertimeDate, startDate)
            .le(AttendanceOvertimeApplyEntity::getOvertimeDate, endDate);

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectList(queryWrapper);

        // 按部门统计
        Map<Long, List<AttendanceOvertimeApplyEntity>> departmentMap = entityList.stream()
            .collect(Collectors.groupingBy(AttendanceOvertimeApplyEntity::getDepartmentId));

        List<Map<String, Object>> departmentStats = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceOvertimeApplyEntity>> entry : departmentMap.entrySet()) {
            Map<String, Object> deptStat = new HashMap<>();
            List<AttendanceOvertimeApplyEntity> deptApplies = entry.getValue();

            deptStat.put("departmentId", entry.getKey());
            deptStat.put("departmentName", deptApplies.get(0).getDepartmentName());
            deptStat.put("applyCount", deptApplies.size());
            deptStat.put("totalHours", deptApplies.stream()
                .map(AttendanceOvertimeApplyEntity::getActualHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

            departmentStats.add(deptStat);
        }

        // 按总时长排序
        departmentStats.sort((a, b) -> {
            BigDecimal hoursA = (BigDecimal) a.get("totalHours");
            BigDecimal hoursB = (BigDecimal) b.get("totalHours");
            return hoursB.compareTo(hoursA);
        });

        statistics.put("departmentStats", departmentStats);
        statistics.put("totalApplications", entityList.size());
        statistics.put("totalHours", entityList.stream()
            .map(AttendanceOvertimeApplyEntity::getActualHours)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));

        return statistics;
    }

    @Override
    public Map<String, Object> generateEmployeeStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        log.info("[加班申请服务] 生成员工统计报表: departmentId={}, startDate={}, endDate={}", departmentId, startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceOvertimeApplyEntity::getApplyStatus, "APPROVED")
            .ge(AttendanceOvertimeApplyEntity::getOvertimeDate, startDate)
            .le(AttendanceOvertimeApplyEntity::getOvertimeDate, endDate);

        if (departmentId != null) {
            queryWrapper.eq(AttendanceOvertimeApplyEntity::getDepartmentId, departmentId);
        }

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectList(queryWrapper);

        // 按员工统计
        Map<Long, List<AttendanceOvertimeApplyEntity>> employeeMap = entityList.stream()
            .collect(Collectors.groupingBy(AttendanceOvertimeApplyEntity::getApplicantId));

        List<Map<String, Object>> employeeStats = new ArrayList<>();
        for (Map.Entry<Long, List<AttendanceOvertimeApplyEntity>> entry : employeeMap.entrySet()) {
            Map<String, Object> empStat = new HashMap<>();
            List<AttendanceOvertimeApplyEntity> empApplies = entry.getValue();

            empStat.put("employeeId", entry.getKey());
            empStat.put("employeeName", empApplies.get(0).getApplicantName());
            empStat.put("departmentName", empApplies.get(0).getDepartmentName());
            empStat.put("applyCount", empApplies.size());
            empStat.put("totalHours", empApplies.stream()
                .map(AttendanceOvertimeApplyEntity::getActualHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

            employeeStats.add(empStat);
        }

        // 按总时长排序
        employeeStats.sort((a, b) -> {
            BigDecimal hoursA = (BigDecimal) a.get("totalHours");
            BigDecimal hoursB = (BigDecimal) b.get("totalHours");
            return hoursB.compareTo(hoursA);
        });

        statistics.put("employeeStats", employeeStats);
        statistics.put("totalEmployees", employeeStats.size());

        return statistics;
    }

    @Override
    public Map<String, Object> generateTypeStatistics(LocalDate startDate, LocalDate endDate) {
        log.info("[加班申请服务] 生成类型统计报表: startDate={}, endDate={}", startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();

        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceOvertimeApplyEntity::getApplyStatus, "APPROVED")
            .ge(AttendanceOvertimeApplyEntity::getOvertimeDate, startDate)
            .le(AttendanceOvertimeApplyEntity::getOvertimeDate, endDate);

        List<AttendanceOvertimeApplyEntity> entityList = overtimeApplyDao.selectList(queryWrapper);

        // 按加班类型统计
        Map<String, List<AttendanceOvertimeApplyEntity>> typeMap = entityList.stream()
            .collect(Collectors.groupingBy(AttendanceOvertimeApplyEntity::getOvertimeType));

        List<Map<String, Object>> typeStats = new ArrayList<>();
        for (Map.Entry<String, List<AttendanceOvertimeApplyEntity>> entry : typeMap.entrySet()) {
            Map<String, Object> typeStat = new HashMap<>();
            List<AttendanceOvertimeApplyEntity> typeApplies = entry.getValue();

            typeStat.put("overtimeType", entry.getKey());
            typeStat.put("applyCount", typeApplies.size());
            typeStat.put("totalHours", typeApplies.stream()
                .map(AttendanceOvertimeApplyEntity::getActualHours)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));

            typeStats.add(typeStat);
        }

        statistics.put("typeStats", typeStats);
        statistics.put("totalApplications", entityList.size());

        return statistics;
    }

    @Override
    public byte[] exportData(AttendanceOvertimeApplyQueryForm form) {
        log.info("[加班申请服务] 导出加班申请数据: form={}", form);

        // TODO: 实现Excel导出功能
        // 这里需要使用EasyExcel或其他工具生成Excel文件
        log.warn("[加班申请服务] Excel导出功能待实现");
        return new byte[0];
    }

    /**
     * 生成申请编号
     * 格式：OT-YYYYMMDD-001
     *
     * @param overtimeDate 加班日期
     * @return 申请编号
     */
    private String generateApplyNo(LocalDate overtimeDate) {
        String dateStr = overtimeDate.format(DATE_FORMATTER);

        // 查询当天最大序号
        LambdaQueryWrapper<AttendanceOvertimeApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.likeRight(AttendanceOvertimeApplyEntity::getApplyNo, "OT-" + dateStr)
            .orderByDesc(AttendanceOvertimeApplyEntity::getApplyNo)
            .last("LIMIT 1");

        AttendanceOvertimeApplyEntity lastApply = overtimeApplyDao.selectOne(queryWrapper);

        int sequence = 1;
        if (lastApply != null && lastApply.getApplyNo() != null) {
            String lastNo = lastApply.getApplyNo();
            String lastSeqStr = lastNo.substring(lastNo.lastIndexOf("-") + 1);
            sequence = Integer.parseInt(lastSeqStr) + 1;
        }

        return String.format("OT-%s-%03d", dateStr, sequence);
    }

    /**
     * 转换为VO对象
     *
     * @param entity 实体对象
     * @return VO对象
     */
    private AttendanceOvertimeApplyVO convertToVO(AttendanceOvertimeApplyEntity entity) {
        return SmartBeanUtil.copy(entity, AttendanceOvertimeApplyVO.class);
    }
}
