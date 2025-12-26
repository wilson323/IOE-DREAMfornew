package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessPersonRestrictionAddForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionQueryForm;
import net.lab1024.sa.access.domain.form.AccessPersonRestrictionUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessPersonRestrictionVO;

/**
 * 门禁人员限制Service接口
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
public interface AccessPersonRestrictionService {

    /**
     * 分页查询人员限制规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    net.lab1024.sa.common.domain.PageResult<AccessPersonRestrictionVO> queryPage(AccessPersonRestrictionQueryForm queryForm);

    /**
     * 根据ID查询人员限制规则
     *
     * @param restrictionId 限制规则ID
     * @return 人员限制VO
     */
    AccessPersonRestrictionVO getById(Long restrictionId);

    /**
     * 新增人员限制规则
     *
     * @param addForm 新增表单
     * @return 限制规则ID
     */
    Long addRestriction(AccessPersonRestrictionAddForm addForm);

    /**
     * 更新人员限制规则
     *
     * @param restrictionId 限制规则ID
     * @param updateForm 更新表单
     * @return 是否成功
     */
    Boolean updateRestriction(Long restrictionId, AccessPersonRestrictionUpdateForm updateForm);

    /**
     * 删除人员限制规则
     *
     * @param restrictionId 限制规则ID
     * @return 是否成功
     */
    Boolean deleteRestriction(Long restrictionId);

    /**
     * 更新人员限制启用状态
     *
     * @param restrictionId 限制规则ID
     * @param enabled 启用状态 (1-启用 0-禁用)
     * @return 是否成功
     */
    Boolean updateEnabled(Long restrictionId, Integer enabled);

    /**
     * 检查用户是否有权限访问指定区域
     *
     * @param userId 用户ID
     * @param areaId 区域ID
     * @return 是否允许访问
     */
    Boolean checkAccessAllowed(Long userId, Long areaId);

    /**
     * 获取用户的所有限制规则
     *
     * @param userId 用户ID
     * @return 限制规则列表
     */
    java.util.List<AccessPersonRestrictionVO> getUserRestrictions(Long userId);
}
