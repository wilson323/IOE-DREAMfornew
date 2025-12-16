package net.lab1024.sa.access.service;

import java.time.LocalDate;
import java.util.List;

import net.lab1024.sa.access.controller.AccessMobileController.MobileAccessRecord;
import net.lab1024.sa.access.domain.form.AccessRecordQueryForm;
import net.lab1024.sa.access.domain.vo.AccessRecordStatisticsVO;
import net.lab1024.sa.access.domain.vo.AccessRecordVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

/**
 * 门禁事件服务接口
 * <p>
 * 提供门禁事件管理相关业务功能
 * 严格遵循CLAUDE.md规范：
 * - Service接口定义在业务服务模块中
 * - 方法返回ResponseDTO统一格式
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AccessEventService {

    /**
     * 获取移动端访问记录
     *
     * @param userId 用户ID
     * @param size 记录数量
     * @return 访问记录列表
     */
    ResponseDTO<List<MobileAccessRecord>> getMobileAccessRecords(Long userId, Integer size);

    /**
     * 分页查询门禁记录
     *
     * @param queryForm 查询表单
     * @return 门禁记录分页结果
     */
    ResponseDTO<PageResult<AccessRecordVO>> queryAccessRecords(AccessRecordQueryForm queryForm);

    /**
     * 获取门禁记录统计
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param areaId 区域ID（可选）
     * @return 统计数据
     */
    ResponseDTO<AccessRecordStatisticsVO> getAccessRecordStatistics(LocalDate startDate, LocalDate endDate, String areaId);

    /**
     * 创建门禁记录
     * <p>
     * 用于设备协议推送门禁记录
     * 通过审计日志记录门禁事件
     * </p>
     *
     * @param form 门禁记录创建表单
     * @return 创建的门禁记录ID（审计日志ID）
     */
    ResponseDTO<Long> createAccessRecord(net.lab1024.sa.access.domain.form.AccessRecordAddForm form);
}

