package net.lab1024.sa.system.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.domain.form.DepartmentAddForm;
import net.lab1024.sa.system.domain.form.DepartmentQueryForm;
import net.lab1024.sa.system.domain.form.DepartmentUpdateForm;
import net.lab1024.sa.system.domain.vo.DepartmentVO;

/**
 * 部门服务接口
 * <p>
 * 严格遵循repowiki Service接口规范：
 * - 完整的CRUD操作
 * - 树形结构支持
 * - 权限控制
 * - 数据验证
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
public interface DepartmentService {

    /**
     * 分页查询部门
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    PageResult<DepartmentVO> queryDepartmentPage(DepartmentQueryForm queryForm);

    /**
     * 查询部门列表（不分页）
     *
     * @param queryForm 查询条件
     * @return 部门列表
     */
    List<DepartmentVO> queryDepartmentList(DepartmentQueryForm queryForm);

    /**
     * 获取部门树形结构
     *
     * @param onlyEnabled 是否只查询启用的部门
     * @return 部门树形结构
     */
    List<DepartmentVO> getDepartmentTree(Boolean onlyEnabled);

    /**
     * 根据ID获取部门详情
     *
     * @param departmentId 部门ID
     * @return 部门详情
     */
    ResponseDTO<DepartmentVO> getDepartmentById(Long departmentId);

    /**
     * 根据编码获取部门详情
     *
     * @param departmentCode 部门编码
     * @return 部门详情
     */
    ResponseDTO<DepartmentVO> getDepartmentByCode(String departmentCode);

    /**
     * 新增部门
     *
     * @param addForm 新增表单
     * @param userId  操作人ID
     * @return 操作结果
     */
    ResponseDTO<Long> addDepartment(DepartmentAddForm addForm, Long userId);

    /**
     * 更新部门
     *
     * @param updateForm 更新表单
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> updateDepartment(DepartmentUpdateForm updateForm, Long userId);

    /**
     * 删除部门（逻辑删除）
     *
     * @param departmentId 部门ID
     * @param userId       操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> deleteDepartment(Long departmentId, Long userId);

    /**
     * 批量删除部门
     *
     * @param departmentIds 部门ID列表
     * @param userId        操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> batchDeleteDepartment(List<Long> departmentIds, Long userId);

    /**
     * 启用/禁用部门
     *
     * @param departmentId 部门ID
     * @param status       状态（1-启用，0-禁用）
     * @param userId       操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> changeDepartmentStatus(Long departmentId, Integer status, Long userId);

    /**
     * 批量启用/禁用部门
     *
     * @param departmentIds 部门ID列表
     * @param status        状态（1-启用，0-禁用）
     * @param userId        操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> batchChangeDepartmentStatus(List<Long> departmentIds, Integer status, Long userId);

    /**
     * 移动部门（改变父部门）
     *
     * @param departmentId 部门ID
     * @param newParentId  新父部门ID
     * @param userId       操作人ID
     * @return 操作结果
     */
    ResponseDTO<String> moveDepartment(Long departmentId, Long newParentId, Long userId);

    /**
     * 获取部门及其所有子部门的ID
     *
     * @param departmentId 部门ID
     * @return 部门ID列表
     */
    List<Long> getDepartmentSelfAndChildrenIds(Long departmentId);

    /**
     * 获取部门路径
     *
     * @param departmentId 部门ID
     * @return 部门路径字符串（例如：总公司/技术部/研发组）
     */
    String getDepartmentPath(Long departmentId);

    /**
     * 获取部门路径映射
     *
     * @return 部门ID -> 路径字符串的映射
     */
    Map<Long, String> getDepartmentPathMap();

    /**
     * 检查部门编码是否存在
     *
     * @param departmentCode 部门编码
     * @param excludeId      排除的部门ID
     * @return 是否存在
     */
    boolean checkDepartmentCodeExists(String departmentCode, Long excludeId);

    /**
     * 检查部门名称在同一父部门下是否存在
     *
     * @param departmentName 部门名称
     * @param parentId       父部门ID
     * @param excludeId      排除的部门ID
     * @return 是否存在
     */
    boolean checkDepartmentNameExists(String departmentName, Long parentId, Long excludeId);

    /**
     * 检查是否存在子部门
     *
     * @param departmentId 部门ID
     * @return 是否存在子部门
     */
    boolean hasChildren(Long departmentId);

    /**
     * 获取所有启用的部门（用于下拉选择）
     *
     * @return 部门列表
     */
    List<DepartmentVO> getAllEnabledDepartments();

    /**
     * 根据负责人查询部门
     *
     * @param managerUserId 负责人ID
     * @return 部门列表
     */
    List<DepartmentVO> getDepartmentsByManager(Long managerUserId);

    /**
     * 获取部门统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDepartmentStatistics();

    /**
     * 导出部门数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    List<Map<String, Object>> exportDepartmentData(DepartmentQueryForm queryForm);

    /**
     * 导入部门数据
     *
     * @param importData 导入数据
     * @param userId     操作人ID
     * @return 导入结果
     */
    ResponseDTO<Map<String, Object>> importDepartmentData(List<Map<String, Object>> importData, Long userId);

    /**
     * 验证部门数据完整性
     *
     * @param departmentId 部门ID
     * @return 验证结果
     */
    ResponseDTO<Map<String, Object>> validateDepartmentData(Long departmentId);

    /**
     * 同步部门缓存
     *
     * @param departmentId 部门ID（为空时同步全部）
     */
    void syncDepartmentCache(Long departmentId);

    /**
     * 清除部门缓存
     *
     * @param departmentId 部门ID（为空时清除全部）
     */
    void clearDepartmentCache(Long departmentId);
}
