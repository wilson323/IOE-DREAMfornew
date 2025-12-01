package net.lab1024.sa.system.dict.service;

import java.util.List;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.domain.form.DictQueryForm;
import net.lab1024.sa.system.domain.form.DictTypeAddForm;
import net.lab1024.sa.system.domain.form.DictTypeUpdateForm;
import net.lab1024.sa.system.domain.vo.DictTypeVO;

/**
 * 字典类型服务接口
 * <p>
 * 提供字典类型的CRUD操作和业务逻辑处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
public interface DictTypeService {

    /**
     * 新增字典类型
     *
     * @param addForm 新增表单
     * @param userId  操作人ID
     * @return 字典类型ID
     */
    ResponseDTO<Long> addDictType(DictTypeAddForm addForm, Long userId);

    /**
     * 更新字典类型
     *
     * @param updateForm 更新表单
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> updateDictType(DictTypeUpdateForm updateForm, Long userId);

    /**
     * 删除字典类型
     *
     * @param dictTypeId 字典类型ID
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteDictType(Long dictTypeId, Long userId);

    /**
     * 获取字典类型详情
     *
     * @param dictTypeId 字典类型ID
     * @return 字典类型详情
     */
    ResponseDTO<DictTypeVO> getDictTypeDetail(Long dictTypeId);

    /**
     * 分页查询字典类型
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<DictTypeVO>> queryDictTypePage(DictQueryForm queryForm);

    /**
     * 查询字典类型列表
     *
     * @param queryForm 查询条件
     * @return 字典类型列表
     */
    ResponseDTO<List<DictTypeVO>> queryDictTypeList(DictQueryForm queryForm);

    /**
     * 获取所有字典类型
     *
     * @return 字典类型列表
     */
    ResponseDTO<List<DictTypeVO>> getAllDictTypes();

    /**
     * 刷新字典类型缓存
     *
     * @param dictTypeId 字典类型ID，为空则刷新所有
     * @return 操作结果
     */
    ResponseDTO<Void> refreshDictTypeCache(Long dictTypeId);

    /**
     * 检查字典类型编码是否唯一
     *
     * @param dictTypeCode 字典类型编码
     * @param excludeId    排除的字典类型ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkDictTypeCodeUnique(String dictTypeCode, Long excludeId);

    /**
     * 修改字典类型状态
     *
     * @param dictTypeId 字典类型ID
     * @param status     状态
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> changeDictTypeStatus(Long dictTypeId, Integer status, Long userId);
}
