package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessInterlockRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessInterlockRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessInterlockRuleVO;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 门禁全局互锁规则 Service接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface AccessInterlockService {

    /**
     * 分页查询互锁规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<AccessInterlockRuleVO> queryPage(AccessInterlockRuleQueryForm queryForm);

    /**
     * 获取规则详情
     *
     * @param ruleId 规则ID
     * @return 规则VO
     */
    AccessInterlockRuleVO getById(Long ruleId);

    /**
     * 新增互锁规则
     *
     * @param addForm 新增表单
     * @return 规则ID
     */
    Long addRule(AccessInterlockRuleAddForm addForm);

    /**
     * 更新互锁规则
     *
     * @param ruleId 规则ID
     * @param updateForm 更新表单
     * @return 是否成功
     */
    Boolean updateRule(Long ruleId, AccessInterlockRuleUpdateForm updateForm);

    /**
     * 删除互锁规则
     *
     * @param ruleId 规则ID
     * @return 是否成功
     */
    Boolean deleteRule(Long ruleId);

    /**
     * 启用/禁用互锁规则
     *
     * @param ruleId 规则ID
     * @param enabled 启用状态 (1-启用 0-禁用)
     * @return 是否成功
     */
    Boolean updateEnabled(Long ruleId, Integer enabled);

    /**
     * 触发互锁
     *
     * @param areaId 触发区域ID
     * @param doorId 触发门ID（可选）
     * @param action 触发动作 (OPEN_DOOR/CLOSE_DOOR)
     * @return 执行结果
     */
    String triggerInterlock(Long areaId, Long doorId, String action);

    /**
     * 手动解锁
     *
     * @param ruleId 规则ID
     * @param areaId 区域ID
     * @return 是否成功
     */
    Boolean manualUnlock(Long ruleId, Long areaId);

    /**
     * 测试互锁规则
     *
     * @param ruleId 规则ID
     * @return 测试结果
     */
    String testRule(Long ruleId);
}
