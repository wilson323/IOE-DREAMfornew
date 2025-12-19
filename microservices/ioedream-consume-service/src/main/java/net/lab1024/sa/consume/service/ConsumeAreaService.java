package net.lab1024.sa.consume.service;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.consume.domain.form.ConsumeAreaAddForm;
import net.lab1024.sa.consume.domain.form.ConsumeAreaQueryForm;
import net.lab1024.sa.consume.domain.form.ConsumeAreaUpdateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeAreaTreeVO;
import net.lab1024.sa.consume.domain.vo.ConsumeAreaVO;

import java.util.List;

/**
 * 消费区域服务接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Service后缀命名
 * - 接口定义在service包下
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
public interface ConsumeAreaService {

    /**
     * 添加区域
     *
     * @param form 区域添加表单
     * @return 区域ID
     */
    ResponseDTO<Long> addArea(ConsumeAreaAddForm form);

    /**
     * 更新区域
     *
     * @param form 区域更新表单
     * @return 是否成功
     */
    ResponseDTO<Void> updateArea(ConsumeAreaUpdateForm form);

    /**
     * 删除区域
     *
     * @param id 区域ID
     * @return 是否成功
     */
    ResponseDTO<Void> deleteArea(Long id);

    /**
     * 根据ID查询区域详情
     *
     * @param id 区域ID
     * @return 区域详情
     */
    ResponseDTO<ConsumeAreaVO> getAreaById(Long id);

    /**
     * 根据编码查询区域详情
     *
     * @param areaCode 区域编码
     * @return 区域详情
     */
    ResponseDTO<ConsumeAreaVO> getAreaByCode(String areaCode);

    /**
     * 分页查询区域列表
     *
     * @param form 查询表单
     * @return 分页结果
     */
    ResponseDTO<PageResult<ConsumeAreaVO>> queryAreaPage(ConsumeAreaQueryForm form);

    /**
     * 查询区域树结构
     * <p>
     * 返回多级层级结构，包含所有子区域
     * </p>
     *
     * @param parentId 父级区域ID（null或0表示查询所有顶级区域）
     * @return 区域树列表
     */
    ResponseDTO<List<ConsumeAreaTreeVO>> getAreaTree(Long parentId);

    /**
     * 查询子区域列表
     *
     * @param parentId 父级区域ID（0或null表示查询顶级区域）
     * @return 子区域列表
     */
    ResponseDTO<List<ConsumeAreaVO>> getAreaChildren(Long parentId);

    /**
     * 启用/禁用区域
     *
     * @param id     区域ID
     * @param status 状态（0-禁用，1-启用）
     * @return 是否成功
     */
    ResponseDTO<Void> updateAreaStatus(Long id, Integer status);
}
