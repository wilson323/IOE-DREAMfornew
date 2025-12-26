package net.lab1024.sa.attendance.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyAddForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyQueryForm;
import net.lab1024.sa.attendance.domain.form.AttendanceOvertimeApplyUpdateForm;
import net.lab1024.sa.attendance.domain.vo.AttendanceOvertimeApplyVO;
import net.lab1024.sa.common.domain.PageResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 加班申请Service接口
 * <p>
 * 提供加班申请的业务逻辑操作
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface AttendanceOvertimeApplyService {

    /**
     * 分页查询加班申请
     *
     * @param form 查询表单
     * @return 分页结果
     */
    PageResult<AttendanceOvertimeApplyVO> queryPage(AttendanceOvertimeApplyQueryForm form);

    /**
     * 根据ID查询加班申请详情
     *
     * @param applyId 申请ID
     * @return 申请详情
     */
    AttendanceOvertimeApplyVO queryDetail(Long applyId);

    /**
     * 根据申请编号查询详情
     *
     * @param applyNo 申请编号
     * @return 申请详情
     */
    AttendanceOvertimeApplyVO queryByApplyNo(String applyNo);

    /**
     * 新增加班申请
     *
     * @param form 新增表单
     * @return 申请ID
     */
    Long add(AttendanceOvertimeApplyAddForm form);

    /**
     * 更新加班申请
     *
     * @param applyId 申请ID
     * @param form    更新表单
     */
    void update(Long applyId, AttendanceOvertimeApplyUpdateForm form);

    /**
     * 删除加班申请（逻辑删除）
     *
     * @param applyId 申请ID
     */
    void delete(Long applyId);

    /**
     * 批量删除加班申请
     *
     * @param applyIds 申请ID列表
     */
    void batchDelete(List<Long> applyIds);

    /**
     * 提交加班申请
     *
     * @param applyId 申请ID
     */
    void submit(Long applyId);

    /**
     * 撤销加班申请
     *
     * @param applyId     申请ID
     * @param cancelReason 撤销原因
     */
    void cancel(Long applyId, String cancelReason);

    /**
     * 审批加班申请（批准）
     *
     * @param applyId          申请ID
     * @param approvalLevel    审批层级
     * @param approverId       审批人ID
     * @param approvalComment   审批意见
     */
    void approve(Long applyId, Integer approvalLevel, Long approverId, String approvalComment);

    /**
     * 审批加班申请（驳回）
     *
     * @param applyId          申请ID
     * @param approvalLevel    审批层级
     * @param approverId       审批人ID
     * @param rejectReason     驳回原因
     */
    void reject(Long applyId, Integer approvalLevel, Long approverId, String rejectReason);

    /**
     * 查询我的加班申请列表
     *
     * @param applicantId 申请人ID
     * @return 申请列表
     */
    List<AttendanceOvertimeApplyVO> queryMyApplications(Long applicantId);

    /**
     * 查询待我审批的申请列表
     *
     * @param approverId 审批人ID
     * @return 申请列表
     */
    List<AttendanceOvertimeApplyVO> queryPendingApprovals(Long approverId);

    /**
     * 查询指定部门的加班申请列表
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 申请列表
     */
    List<AttendanceOvertimeApplyVO> queryByDepartmentAndDateRange(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 统计用户的加班申请数量（按状态）
     *
     * @param applicantId 申请人ID
     * @param applyStatus 申请状态
     * @return 申请数量
     */
    Integer countByApplicantAndStatus(Long applicantId, String applyStatus);

    /**
     * 统计部门加班时长
     *
     * @param departmentId 部门ID
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 总加班时长（小时）
     */
    BigDecimal sumOvertimeHoursByDepartment(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取加班统计报表（按部门）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    Map<String, Object> generateDepartmentStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取加班统计报表（按员工）
     *
     * @param departmentId 部门ID（可选，为空则全公司）
     * @param startDate    开始日期
     * @param endDate      结束日期
     * @return 统计数据
     */
    Map<String, Object> generateEmployeeStatistics(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取加班统计报表（按类型）
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据
     */
    Map<String, Object> generateTypeStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 导出加班申请数据
     *
     * @param form 查询表单
     * @return Excel文件字节数组
     */
    byte[] exportData(AttendanceOvertimeApplyQueryForm form);
}
