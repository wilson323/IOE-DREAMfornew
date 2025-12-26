package net.lab1024.sa.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanAddForm;
import net.lab1024.sa.attendance.domain.form.smartSchedule.SmartSchedulePlanQueryForm;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanDetailVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartSchedulePlanVO;
import net.lab1024.sa.attendance.domain.vo.smartSchedule.SmartScheduleResultVO;
import net.lab1024.sa.attendance.engine.optimizer.OptimizationResult;
import net.lab1024.sa.common.domain.PageResult;

import java.time.LocalDate;
import java.util.List;

/**
 * 智能排班计划服务接口
 * <p>
 * 核心功能：
 * - 创建排班计划
 * - 执行排班优化
 * - 查询排班结果
 * - 删除排班计划
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface SmartScheduleService {

    /**
     * 创建智能排班计划
     *
     * @param form 排班计划表单
     * @return 计划ID
     */
    Long createPlan(SmartSchedulePlanAddForm form);

    /**
     * 执行排班优化
     *
     * @param planId 排班计划ID
     * @return 优化结果
     */
    OptimizationResult executeOptimization(Long planId);

    /**
     * 查询排班计划列表（分页）
     *
     * @param form 查询表单
     * @return 分页结果
     */
    PageResult<SmartSchedulePlanVO> queryPlanPage(SmartSchedulePlanQueryForm form);

    /**
     * 查询排班计划详情
     *
     * @param planId 计划ID
     * @return 计划详情
     */
    SmartSchedulePlanDetailVO getPlanDetail(Long planId);

    /**
     * 查询排班结果列表（分页）
     *
     * @param planId     计划ID
     * @param pageNum    页码
     * @param pageSize   页大小
     * @param employeeId 员工ID（可选）
     * @param startDate  开始日期（可选）
     * @param endDate    结束日期（可选）
     * @return 分页结果
     */
    PageResult<SmartScheduleResultVO> queryResultPage(Long planId, Integer pageNum, Integer pageSize,
                                                      Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * 查询排班结果列表（不分页）
     *
     * @param planId 讒划ID
     * @return 结果列表
     */
    List<SmartScheduleResultVO> queryResultList(Long planId);

    /**
     * 删除排班计划
     *
     * @param planId 计划ID
     */
    void deletePlan(Long planId);

    /**
     * 批量删除排班计划
     *
     * @param planIds 计划ID列表
     */
    void batchDeletePlan(List<Long> planIds);

    /**
     * 确认排班计划
     *
     * @param planId 计划ID
     */
    void confirmPlan(Long planId);

    /**
     * 取消排班计划
     *
     * @param planId 计划ID
     * @param reason 取消原因
     */
    void cancelPlan(Long planId, String reason);

    /**
     * 导出排班结果
     *
     * @param planId 计划ID
     * @return Excel文件字节数组
     */
    byte[] exportScheduleResult(Long planId);
}
