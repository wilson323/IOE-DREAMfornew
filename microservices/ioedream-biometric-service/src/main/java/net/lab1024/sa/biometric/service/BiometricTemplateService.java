package net.lab1024.sa.biometric.service;

import net.lab1024.sa.common.entity.biometric.BiometricTemplateEntity;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateAddForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateUpdateForm;
import net.lab1024.sa.biometric.domain.form.BiometricTemplateQueryForm;
import net.lab1024.sa.biometric.domain.vo.BiometricTemplateVO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;

import java.util.List;

/**
 * 生物模板管理服务接口
 * <p>
 * 严格遵循CLAUDE.md规范:
 * - Service层只定义接口，不包含实现
 * - 使用@Resource依赖注入
 * - 完整的业务方法定义
 * </p>
 * <p>
 * 核心职责:
 * - 模板CRUD操作
 * - 模板状态管理
 * - 模板版本管理
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-18
 */
public interface BiometricTemplateService {

    /**
     * 添加生物模板
     * <p>
     * 场景：用户入职时上传生物特征，创建模板并同步到设备
     * </p>
     *
     * @param addForm 添加表单
     * @return 模板信息
     */
    ResponseDTO<BiometricTemplateVO> addTemplate(BiometricTemplateAddForm addForm);

    /**
     * 更新生物模板
     *
     * @param updateForm 更新表单
     * @return 更新结果
     */
    ResponseDTO<Void> updateTemplate(BiometricTemplateUpdateForm updateForm);

    /**
     * 删除生物模板
     * <p>
     * 场景：用户离职时删除模板，并从所有设备删除
     * </p>
     *
     * @param templateId 模板ID
     * @return 删除结果
     */
    ResponseDTO<Void> deleteTemplate(Long templateId);

    /**
     * 根据用户ID和类型删除模板
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @return 删除结果
     */
    ResponseDTO<Void> deleteTemplateByUserAndType(Long userId, Integer biometricType);

    /**
     * 根据ID查询模板
     *
     * @param templateId 模板ID
     * @return 模板信息
     */
    ResponseDTO<BiometricTemplateVO> getTemplateById(Long templateId);

    /**
     * 根据用户ID查询模板列表
     *
     * @param userId 用户ID
     * @return 模板列表
     */
    ResponseDTO<List<BiometricTemplateVO>> getTemplatesByUserId(Long userId);

    /**
     * 根据用户ID和类型查询模板
     *
     * @param userId 用户ID
     * @param biometricType 生物识别类型
     * @return 模板信息
     */
    ResponseDTO<BiometricTemplateVO> getTemplateByUserAndType(Long userId, Integer biometricType);

    /**
     * 分页查询模板
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<BiometricTemplateVO>> pageTemplate(BiometricTemplateQueryForm queryForm);

    /**
     * 更新模板状态
     *
     * @param templateId 模板ID
     * @param templateStatus 模板状态
     * @return 更新结果
     */
    ResponseDTO<Void> updateTemplateStatus(Long templateId, Integer templateStatus);

    /**
     * 批量更新模板状态
     *
     * @param templateIds 模板ID列表
     * @param templateStatus 模板状态
     * @return 更新结果
     */
    ResponseDTO<Void> batchUpdateTemplateStatus(List<Long> templateIds, Integer templateStatus);
}
