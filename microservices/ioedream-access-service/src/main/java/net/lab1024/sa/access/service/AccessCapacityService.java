package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessCapacityControlAddForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlQueryForm;
import net.lab1024.sa.access.domain.form.AccessCapacityControlUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessCapacityControlVO;

/**
 * 门禁容量控制Service接口
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
public interface AccessCapacityService {

    /**
     * 分页查询容量控制规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    net.lab1024.sa.common.domain.PageResult<AccessCapacityControlVO> queryPage(AccessCapacityControlQueryForm queryForm);

    /**
     * 根据ID查询容量控制规则
     *
     * @param controlId 容量控制ID
     * @return 容量控制VO
     */
    AccessCapacityControlVO getById(Long controlId);

    /**
     * 新增容量控制规则
     *
     * @param addForm 新增表单
     * @return 容量控制ID
     */
    Long addControl(AccessCapacityControlAddForm addForm);

    /**
     * 更新容量控制规则
     *
     * @param controlId 容量控制ID
     * @param updateForm 更新表单
     * @return 是否成功
     */
    Boolean updateControl(Long controlId, AccessCapacityControlUpdateForm updateForm);

    /**
     * 删除容量控制规则
     *
     * @param controlId 容量控制ID
     * @return 是否成功
     */
    Boolean deleteControl(Long controlId);

    /**
     * 更新容量控制启用状态
     *
     * @param controlId 容量控制ID
     * @param enabled 启用状态 (1-启用 0-禁用)
     * @return 是否成功
     */
    Boolean updateEnabled(Long controlId, Integer enabled);

    /**
     * 增加区域内人数
     *
     * @param areaId 区域ID
     * @param count 增加人数
     * @return 操作结果
     */
    String incrementCount(Long areaId, Integer count);

    /**
     * 减少区域内人数
     *
     * @param areaId 区域ID
     * @param count 减少人数
     * @return 操作结果
     */
    String decrementCount(Long areaId, Integer count);

    /**
     * 重置区域内人数
     *
     * @param controlId 容量控制ID
     * @return 操作结果
     */
    String resetCount(Long controlId);

    /**
     * 获取区域当前人数
     *
     * @param areaId 区域ID
     * @return 当前人数
     */
    Integer getCurrentCount(Long areaId);

    /**
     * 检查区域是否允许进入
     *
     * @param areaId 区域ID
     * @return 检查结果
     */
    Boolean checkEntryAllowed(Long areaId);
}
