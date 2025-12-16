package net.lab1024.sa.access.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessRecord;
import net.lab1024.sa.access.dao.AccessRecordDao;
import net.lab1024.sa.common.access.entity.AccessRecordEntity;
import net.lab1024.sa.access.domain.form.AccessRecordAddForm;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.access.service.AccessEventService;
import net.lab1024.sa.common.audit.dao.AuditLogDao;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁事件服务实现类
 * <p>
 * 实现门禁事件管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - 使用@Service注解标识服务类
 * - 使用@Resource注入依赖
 * - 使用@Transactional管理事务
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessEventServiceImpl implements AccessEventService {

    @Resource
    private AccessRecordDao accessRecordDao;

    @Resource
    private AuditLogDao auditLogDao;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取移动端访问记录
     * <p>
     * 从门禁记录表查询最近的门禁记录，转换为移动端格式
     * </p>
     *
     * @param userId 用户ID
     * @param size 记录数量
     * @return 访问记录列表
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<List<MobileAccessRecord>> getMobileAccessRecords(Long userId, Integer size) {
        log.debug("[门禁事件] 获取移动端访问记录，userId={}, size={}", userId, size);

        // 查询最近的门禁记录（最近30天）
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(30);
        Integer querySize = size != null && size > 0 ? size : 20;

        List<AccessRecordEntity> records = accessRecordDao.selectByUserIdAndTimeRange(
            userId, startTime, endTime, querySize);

        // 转换为移动端格式
        List<MobileAccessRecord> mobileRecords = records.stream()
            .map(this::convertToMobileRecord)
            .collect(Collectors.toList());

        log.debug("[门禁事件] 查询到{}条记录", mobileRecords.size());
        return ResponseDTO.ok(mobileRecords);
    }

    /**
     * 分页查询门禁记录
     * <p>
     * 从门禁记录表分页查询，支持多条件筛选
     * </p>
     *
     * @param queryForm 查询表单
     * @return 门禁记录分页结果
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<PageResult<AccessRecordVO>> queryAccessRecords(AccessRecordQueryForm queryForm) {
        log.debug("[门禁事件] 分页查询门禁记录，queryForm={}", queryForm);

        // 构建分页对象
        Integer pageNum = queryForm.getPageNum() != null ? queryForm.getPageNum() : 1;
        Integer pageSize = queryForm.getPageSize() != null ? queryForm.getPageSize() : 20;
        Page<AccessRecordEntity> page = new Page<>(pageNum, pageSize);

        // 执行分页查询
        Page<AccessRecordEntity> pageResult = accessRecordDao.selectPage(
            page,
            queryForm.getUserId(),
            queryForm.getDeviceId(),
            queryForm.getAreaId(),
            queryForm.getStartDate(),
            queryForm.getEndDate(),
            queryForm.getAccessResult()
        );

        // 转换为VO
        List<AccessRecordVO> voList = pageResult.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        // 构建分页结果
        PageResult<AccessRecordVO> result = new PageResult<>();
        result.setList(voList);
        result.setTotal(pageResult.getTotal());
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setPages((int) pageResult.getPages());

        log.debug("[门禁事件] 分页查询完成，总数={}, 当前页={}", result.getTotal(), pageNum);
        return ResponseDTO.ok(result);
    }

    /**
     * 获取门禁记录统计
     * <p>
     * 统计指定时间范围内的门禁记录，包括总数、成功数、失败数
     * </p>
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID（可选）
     * @return 统计数据
     */
    @Override
    @Transactional(readOnly = true)
    public ResponseDTO<AccessRecordStatisticsVO> getAccessRecordStatistics(LocalDate startDate, LocalDate endDate, String areaId) {
        log.debug("[门禁事件] 获取门禁记录统计，startDate={}, endDate={}, areaId={}", startDate, endDate, areaId);

        // 执行统计查询
        AccessRecordDao.AccessRecordStatistics statistics = accessRecordDao.selectStatistics(
            startDate, endDate, areaId);

        // 转换为VO
        AccessRecordStatisticsVO vo = new AccessRecordStatisticsVO();
        vo.setTotalCount(statistics != null ? statistics.getTotalCount() : 0L);
        vo.setSuccessCount(statistics != null ? statistics.getSuccessCount() : 0L);
        vo.setFailedCount(statistics != null ? statistics.getFailedCount() : 0L);

        log.debug("[门禁事件] 统计完成，总数={}, 成功={}, 失败={}",
            vo.getTotalCount(), vo.getSuccessCount(), vo.getFailedCount());
        return ResponseDTO.ok(vo);
    }

    /**
     * 创建门禁记录
     * <p>
     * 实现步骤：
     * 1. 创建门禁记录实体并保存到数据库
     * 2. 写入审计日志
     * 3. 返回审计日志ID
     * </p>
     *
     * @param form 门禁记录创建表单
     * @return 创建的门禁记录ID（审计日志ID）
     */
    @Override
    public ResponseDTO<Long> createAccessRecord(AccessRecordAddForm form) {
        log.info("[门禁事件] 创建门禁记录，form={}", form);

        // 1. 创建门禁记录实体
        AccessRecordEntity entity = new AccessRecordEntity();
        entity.setUserId(form.getUserId());
        entity.setDeviceId(form.getDeviceId());
        entity.setAreaId(form.getAreaId());
        entity.setAccessResult(form.getAccessResult() != null ? form.getAccessResult() : 1);

        // 处理通行时间
        LocalDateTime accessTime = LocalDateTime.now();
        if (form.getPassTime() != null) {
            if (form.getPassTime() instanceof LocalDateTime) {
                accessTime = (LocalDateTime) form.getPassTime();
            } else if (form.getPassTime() instanceof String) {
                try {
                    accessTime = LocalDateTime.parse((String) form.getPassTime(), DATE_TIME_FORMATTER);
                } catch (Exception e) {
                    log.warn("[门禁事件] 解析通行时间失败，使用当前时间，passTime={}", form.getPassTime());
                }
            }
        }
        entity.setAccessTime(accessTime);

        // 处理通行类型
        if (form.getPassType() != null) {
            entity.setAccessType(form.getPassType() == 0 ? "IN" : "OUT");
        } else {
            entity.setAccessType("IN");
        }

        // 处理验证方式
        if (form.getPassMethod() != null) {
            switch (form.getPassMethod()) {
                case 0:
                    entity.setVerifyMethod("CARD");
                    break;
                case 1:
                    entity.setVerifyMethod("FACE");
                    break;
                case 2:
                    entity.setVerifyMethod("FINGERPRINT");
                    break;
                default:
                    entity.setVerifyMethod("CARD");
            }
        }

        // 保存到数据库
        accessRecordDao.insert(entity);
        log.info("[门禁事件] 门禁记录已保存，recordId={}", entity.getRecordId());

        // 2. 写入审计日志
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setUserId(form.getUserId());
        auditLog.setModuleName("ACCESS");
        auditLog.setOperationType(1); // 1-查询，2-新增，3-修改，4-删除
        auditLog.setOperation("CREATE_ACCESS_RECORD");
        auditLog.setOperationDesc("创建门禁记录");
        auditLog.setResourceType("ACCESS_RECORD");
        auditLog.setResourceId(String.valueOf(entity.getRecordId()));
        auditLog.setResultStatus(entity.getAccessResult() == 1 ? 1 : 0);
        auditLog.setCreateTime(LocalDateTime.now());

        auditLogDao.insert(auditLog);
        log.info("[门禁事件] 审计日志已写入，logId={}", auditLog.getLogId());

        // 返回审计日志ID
        return ResponseDTO.ok(auditLog.getLogId());
    }

    /**
     * 转换为移动端记录格式
     *
     * @param entity 门禁记录实体
     * @return 移动端记录
     */
    private MobileAccessRecord convertToMobileRecord(AccessRecordEntity entity) {
        MobileAccessRecord record = new MobileAccessRecord();
        record.setRecordId(entity.getRecordId());
        record.setUserId(entity.getUserId());
        record.setDeviceId(entity.getDeviceId());
        record.setDeviceName("设备" + entity.getDeviceId()); // 实际应从设备服务获取
        record.setAccessTime(entity.getAccessTime() != null
            ? entity.getAccessTime().format(DATE_TIME_FORMATTER)
            : "");
        record.setAccessType(entity.getAccessType());
        record.setAccessResult(entity.getAccessResult() == 1);
        return record;
    }

    /**
     * 转换为VO格式
     *
     * @param entity 门禁记录实体
     * @return 门禁记录VO
     */
    private AccessRecordVO convertToVO(AccessRecordEntity entity) {
        AccessRecordVO vo = new AccessRecordVO();
        vo.setLogId(entity.getRecordId());
        vo.setUserId(entity.getUserId());
        vo.setUserName("用户" + entity.getUserId()); // 实际应从用户服务获取
        vo.setDeviceId(String.valueOf(entity.getDeviceId()));
        vo.setDeviceName("设备" + entity.getDeviceId()); // 实际应从设备服务获取
        vo.setAreaId(entity.getAreaId() != null ? String.valueOf(entity.getAreaId()) : null);
        vo.setAreaName(entity.getAreaId() != null ? "区域" + entity.getAreaId() : null); // 实际应从区域服务获取
        vo.setOperation(entity.getVerifyMethod());
        vo.setResult(entity.getAccessResult());
        vo.setCreateTime(entity.getAccessTime());
        vo.setIpAddress(null); // 从审计日志获取
        vo.setRemark(null);
        return vo;
    }
}
