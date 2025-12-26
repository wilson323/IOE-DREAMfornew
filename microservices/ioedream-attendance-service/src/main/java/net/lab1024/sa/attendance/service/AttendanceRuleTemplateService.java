package net.lab1024.sa.attendance.service;

// TODO: 取消注释以下import（待创建相关Form和VO类后）
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateAddForm;
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateQueryForm;
// import net.lab1024.sa.attendance.domain.form.AttendanceRuleTemplateUpdateForm;
// import net.lab1024.sa.attendance.domain.vo.AttendanceRuleTemplateVO;
// import net.lab1024.sa.common.domain.PageResult;

import java.util.List;

/**
 * 考勤规则模板服务接口
 * <p>
 * 提供考勤规则模板管理相关业务功能
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 * <p>
 * TODO: 待创建AttendanceRuleTemplate相关的Form和VO类后，取消注释并实现此接口
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-26
 */
public interface AttendanceRuleTemplateService {

    /**
     * 分页查询规则模板
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    // TODO: 取消注释（待创建相关VO类后）
    // PageResult<AttendanceRuleTemplateVO> queryTemplatePage(AttendanceRuleTemplateQueryForm queryForm);
    Object queryTemplatePage(Object queryForm);

    /**
     * 查询系统模板列表
     *
     * @param category 模板分类（可选）
     * @return 系统模板列表
     */
    // TODO: 取消注释（待创建相关VO类后）
    // List<AttendanceRuleTemplateVO> getSystemTemplates(String category);
    List<Object> getSystemTemplates(String category);

    /**
     * 查询用户自定义模板列表
     *
     * @param userId 用户ID
     * @param category 模板分类（可选）
     * @return 用户模板列表
     */
    // TODO: 取消注释（待创建相关VO类后）
    // List<AttendanceRuleTemplateVO> getUserTemplates(Long userId, String category);
    List<Object> getUserTemplates(Long userId, String category);

    /**
     * 查询模板详情
     *
     * @param templateId 模板ID
     * @return 模板详情
     */
    // TODO: 取消注释（待创建相关VO类后）
    // AttendanceRuleTemplateVO getTemplateDetail(Long templateId);
    Object getTemplateDetail(Long templateId);

    /**
     * 根据模板编码查询模板
     *
     * @param templateCode 模板编码
     * @return 模板详情
     */
    // TODO: 取消注释（待创建相关VO类后）
    // AttendanceRuleTemplateVO getTemplateByCode(String templateCode);
    Object getTemplateByCode(String templateCode);

    /**
     * 创建规则模板
     *
     * @param addForm 新增表单
     * @return 模板ID
     */
    // TODO: 取消注释（待创建相关Form类后）
    // Long createTemplate(AttendanceRuleTemplateAddForm addForm);
    Long createTemplate(Object addForm);

    /**
     * 更新规则模板
     *
     * @param templateId 模板ID
     * @param updateForm 更新表单
     */
    // TODO: 取消注释（待创建相关Form类后）
    // void updateTemplate(Long templateId, AttendanceRuleTemplateUpdateForm updateForm);
    void updateTemplate(Long templateId, Object updateForm);

    /**
     * 删除规则模板
     *
     * @param templateId 模板ID
     */
    void deleteTemplate(Long templateId);

    /**
     * 批量删除规则模板
     *
     * @param templateIds 模板ID列表
     */
    void batchDeleteTemplates(List<Long> templateIds);

    /**
     * 应用模板到规则
     * 从模板复制条件和动作到规则
     *
     * @param templateId 模板ID
     * @param ruleId 规则ID
     */
    void applyTemplateToRule(Long templateId, Long ruleId);

    /**
     * 增加模板使用次数
     *
     * @param templateId 模板ID
     */
    void incrementUseCount(Long templateId);

    /**
     * 导出模板（JSON格式）
     *
     * @param templateId 模板ID
     * @return 模板JSON数据
     */
    String exportTemplate(Long templateId);

    /**
     * 导入模板
     *
     * @param templateJson 模板JSON数据
     * @return 模板ID
     */
    Long importTemplate(String templateJson);

    /**
     * 复制模板
     *
     * @param templateId 模板ID
     * @param newTemplateName 新模板名称
     * @return 新模板ID
     */
    Long copyTemplate(Long templateId, String newTemplateName);
}
