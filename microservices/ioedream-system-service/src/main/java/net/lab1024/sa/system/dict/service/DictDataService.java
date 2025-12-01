package net.lab1024.sa.system.dict.service;

import java.util.List;
import java.util.Map;

import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.domain.ResponseDTO;
import net.lab1024.sa.system.domain.form.DictDataAddForm;
import net.lab1024.sa.system.domain.form.DictDataUpdateForm;
import net.lab1024.sa.system.domain.form.DictQueryForm;
import net.lab1024.sa.system.domain.vo.DictDataVO;

/**
 * 字典数据服务接口
 * <p>
 * 提供字典数据的CRUD操作和业务逻辑处理
 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
public interface DictDataService {

    /**
     * 新增字典数据
     *
     * @param addForm 新增表单
     * @param userId  操作人ID
     * @return 字典数据ID
     */
    ResponseDTO<Long> addDictData(DictDataAddForm addForm, Long userId);

    /**
     * 更新字典数据
     *
     * @param updateForm 更新表单
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> updateDictData(DictDataUpdateForm updateForm, Long userId);

    /**
     * 删除字典数据
     *
     * @param dictDataId 字典数据ID
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> deleteDictData(Long dictDataId, Long userId);

    /**
     * 批量删除字典数据
     *
     * @param dictDataIds 字典数据ID列表
     * @param userId      操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> batchDeleteDictData(List<Long> dictDataIds, Long userId);

    /**
     * 获取字典数据详情
     *
     * @param dictDataId 字典数据ID
     * @return 字典数据详情
     */
    ResponseDTO<DictDataVO> getDictDataDetail(Long dictDataId);

    /**
     * 分页查询字典数据
     *
     * @param queryForm 查询条件
     * @return 分页结果
     */
    ResponseDTO<PageResult<DictDataVO>> queryDictDataPage(DictQueryForm queryForm);

    /**
     * 根据字典类型ID查询字典数据
     *
     * @param dictTypeId 字典类型ID
     * @return 字典数据列表
     */
    ResponseDTO<List<DictDataVO>> getDictDataByTypeId(Long dictTypeId);

    /**
     * 根据字典类型编码查询字典数据
     *
     * @param dictTypeCode 字典类型编码
     * @return 字典数据列表
     */
    ResponseDTO<List<DictDataVO>> getDictDataByTypeCode(String dictTypeCode);

    /**
     * 获取所有字典数据
     *
     * @return 字典数据列表
     */
    ResponseDTO<List<DictDataVO>> getAllDictData();

    /**
     * 刷新字典数据缓存
     *
     * @param dictTypeId 字典类型ID，为空则刷新所有
     * @return 操作结果
     */
    ResponseDTO<Void> refreshDictDataCache(Long dictTypeId);

    /**
     * 导出字典数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    ResponseDTO<List<Map<String, Object>>> exportDictData(DictQueryForm queryForm);

    /**
     * 导入字典数据
     *
     * @param importData 导入数据
     * @param userId     操作人ID
     * @return 导入结果
     */
    ResponseDTO<Map<String, Object>> importDictData(List<Map<String, Object>> importData, Long userId);

    /**
     * 修改字典数据状态
     *
     * @param dictDataId 字典数据ID
     * @param status     状态
     * @param userId     操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> changeDictDataStatus(Long dictDataId, Integer status, Long userId);

    /**
     * 批量修改字典数据状态
     *
     * @param dictDataIds 字典数据ID列表
     * @param status      状态
     * @param userId      操作人ID
     * @return 操作结果
     */
    ResponseDTO<Void> batchChangeDictDataStatus(List<Long> dictDataIds, Integer status, Long userId);

    /**
     * 检查字典值在字典类型下是否唯一
     *
     * @param dictTypeId 字典类型ID
     * @param dictValue  字典值
     * @param excludeId  排除的字典数据ID
     * @return 是否唯一
     */
    ResponseDTO<Boolean> checkDictValueUnique(Long dictTypeId, String dictValue, Long excludeId);

    /**
     * 获取字典缓存
     *
     * @return 字典缓存数据
     */
    ResponseDTO<Map<String, Object>> getDictCache();
}
