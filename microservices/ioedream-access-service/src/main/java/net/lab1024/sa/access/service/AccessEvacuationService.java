package net.lab1024.sa.access.service;

import net.lab1024.sa.access.domain.form.AccessEvacuationPointAddForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointQueryForm;
import net.lab1024.sa.access.domain.form.AccessEvacuationPointUpdateForm;
import net.lab1024.sa.access.domain.vo.AccessEvacuationPointVO;

/**
 * 门禁疏散点管理Service接口
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
public interface AccessEvacuationService {

    /**
     * 分页查询疏散点
     *
     * @param queryForm 查询表单
     * @return 分页结果
     */
    net.lab1024.sa.common.domain.PageResult<AccessEvacuationPointVO> queryPage(AccessEvacuationPointQueryForm queryForm);

    /**
     * 根据ID查询疏散点
     *
     * @param pointId 疏散点ID
     * @return 疏散点VO
     */
    AccessEvacuationPointVO getById(Long pointId);

    /**
     * 新增疏散点
     *
     * @param addForm 新增表单
     * @return 疏散点ID
     */
    Long addPoint(AccessEvacuationPointAddForm addForm);

    /**
     * 更新疏散点
     *
     * @param pointId 疏散点ID
     * @param updateForm 更新表单
     * @return 是否成功
     */
    Boolean updatePoint(Long pointId, AccessEvacuationPointUpdateForm updateForm);

    /**
     * 删除疏散点
     *
     * @param pointId 疏散点ID
     * @return 是否成功
     */
    Boolean deletePoint(Long pointId);

    /**
     * 更新疏散点启用状态
     *
     * @param pointId 疏散点ID
     * @param enabled 启用状态 (1-启用 0-禁用)
     * @return 是否成功
     */
    Boolean updateEnabled(Long pointId, Integer enabled);

    /**
     * 触发一键疏散
     *
     * @param pointId 疏散点ID
     * @return 执行结果
     */
    String triggerEvacuation(Long pointId);

    /**
     * 取消疏散
     *
     * @param pointId 疏散点ID
     * @return 执行结果
     */
    String cancelEvac(Long pointId);

    /**
     * 测试疏散点
     *
     * @param pointId 疏散点ID
     * @return 测试结果
     */
    String testPoint(Long pointId);
}
