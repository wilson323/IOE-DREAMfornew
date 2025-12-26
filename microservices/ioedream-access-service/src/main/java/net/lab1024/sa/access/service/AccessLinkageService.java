package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.entity.access.AccessLinkageRuleEntity;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleAddForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleQueryForm;
import net.lab1024.sa.access.domain.form.AccessLinkageRuleUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessLinkageRuleVO;
import net.lab1024.sa.common.domain.PageResult;

/**
 * 门禁联动规则服务接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
public interface AccessLinkageService {

    /**
     * 分页查询联动规则
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    PageResult<AccessLinkageRuleVO> queryPage(AccessLinkageRuleQueryForm queryForm);

    /**
     * 根据ID查询联动规则详情
     *
     * @param ruleId 规则ID
     * @return 规则详情VO
     */
    AccessLinkageRuleVO getById(Long ruleId);

    /**
     * 新增联动规则
     *
     * @param addForm 新增表单
     * @return 新增的规则ID
     */
    Long add(AccessLinkageRuleAddForm addForm);

    /**
     * 更新联动规则
     *
     * @param ruleId   规则ID
     * @param updateForm 更新表单
     */
    void update(Long ruleId, AccessLinkageRuleUpdateForm updateForm);

    /**
     * 删除联动规则
     *
     * @param ruleId 规则ID
     */
    void delete(Long ruleId);

    /**
     * 启用/禁用联动规则
     *
     * @param ruleId  规则ID
     * @param enabled 启用状态
     */
    void updateEnabled(Long ruleId, Integer enabled);

    /**
     * 触发联动规则执行
     *
     * @param triggerDeviceId 触发设备ID
     * @param triggerDoorId    触发门ID
     * @param triggerEvent     触发事件
     * @return 执行结果消息
     */
    String triggerLinkage(Long triggerDeviceId, Long triggerDoorId, String triggerEvent);

    /**
     * 测试联动规则
     *
     * @param ruleId 规则ID
     * @return 测试结果
     */
    String testRule(Long ruleId);
}
