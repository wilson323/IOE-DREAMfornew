package net.lab1024.sa.system.dict.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.dict.domain.entity.DictDataEntity;

/**
 * 字典数据DAO
 * <p>
 * 提供字典数据的数据访问操作
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface DictDataDao extends BaseMapper<DictDataEntity> {

    /**
     * 分页查询字典数据
     *
     * @param queryForm 查询条件
     * @return 字典数据列表
     */
    List<DictDataEntity> selectDictDataPage(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据条件查询字典数据列表
     *
     * @param queryForm 查询条件
     * @return 字典数据列表
     */
    List<DictDataEntity> selectDictDataList(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取所有字典数据
     *
     * @return 字典数据列表
     */
    List<DictDataEntity> selectAllDictData();

    /**
     * 根据ID查询字典数据详情
     *
     * @param dictDataId 字典数据ID
     * @return 字典数据详情
     */
    DictDataEntity selectDictDataDetail(@Param("dictDataId") Long dictDataId);

    /**
     * 根据字典类型ID查询字典数据
     *
     * @param dictTypeId 字典类型ID
     * @return 字典数据列表
     */
    List<DictDataEntity> selectByDictTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型编码查询字典数据
     *
     * @param dictTypeCode 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataEntity> selectByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 根据字典类型编码查询启用的字典数据
     *
     * @param dictTypeCode 字典类型编码
     * @return 字典数据列表
     */
    List<DictDataEntity> selectEnabledByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 检查字典值在字典类型下是否唯一
     *
     * @param dictTypeId 字典类型ID
     * @param dictValue  字典值
     * @param excludeId  排除的字典数据ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkDictValueUnique(@Param("dictTypeId") Long dictTypeId,
            @Param("dictValue") String dictValue,
            @Param("excludeId") Long excludeId);

    /**
     * 修改字典数据状态
     *
     * @param dictDataId 字典数据ID
     * @param status     状态
     * @param userId     操作人ID
     * @return 影响行数
     */
    int updateDictDataStatus(@Param("dictDataId") Long dictDataId,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 批量修改字典数据状态
     *
     * @param dictDataIds 字典数据ID列表
     * @param status      状态
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchUpdateDictDataStatus(@Param("dictDataIds") List<Long> dictDataIds,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 统计字典数据数量
     *
     * @param queryForm 查询条件
     * @return 统计数量
     */
    Long countDictData(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取字典数据统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDictDataStatistics();

    /**
     * 根据字典类型ID获取字典数据数量
     *
     * @param dictTypeId 字典类型ID
     * @return 字典数据数量
     */
    Long countDictDataByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 获取指定字典类型的默认值
     *
     * @param dictTypeId 字典类型ID
     * @return 默认值
     */
    DictDataEntity selectDefaultByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型编码获取默认值
     *
     * @param dictTypeCode 字典类型编码
     * @return 默认值
     */
    DictDataEntity selectDefaultByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 根据状态查询字典数据
     *
     * @param status 状态
     * @return 字典数据列表
     */
    List<DictDataEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据字典值查询字典数据
     *
     * @param dictValue 字典值
     * @return 字典数据列表
     */
    List<DictDataEntity> selectByDictValue(@Param("dictValue") String dictValue);

    /**
     * 根据字典标签模糊查询字典数据
     *
     * @param dictLabel 字典标签
     * @return 字典数据列表
     */
    List<DictDataEntity> selectByDictLabel(@Param("dictLabel") String dictLabel);

    /**
     * 获取字典数据及其类型信息
     *
     * @return 字典数据及类型信息
     */
    List<Map<String, Object>> selectDictDataWithType();

    /**
     * 批量插入字典数据
     *
     * @param dictDataList 字典数据列表
     * @return 影响行数
     */
    int batchInsertDictData(@Param("dictDataList") List<DictDataEntity> dictDataList);

    /**
     * 批量更新字典数据
     *
     * @param dictDataList 字典数据列表
     * @return 影响行数
     */
    int batchUpdateDictData(@Param("dictDataList") List<DictDataEntity> dictDataList);

    /**
     * 批量删除字典数据（逻辑删除）
     *
     * @param dictDataIds 字典数据ID列表
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchDeleteDictData(@Param("dictDataIds") List<Long> dictDataIds,
            @Param("userId") Long userId);

    /**
     * 根据字典类型ID删除所有字典数据
     *
     * @param dictTypeId 字典类型ID
     * @param userId     操作人ID
     * @return 影响行数
     */
    int deleteDictDataByTypeId(@Param("dictTypeId") Long dictTypeId,
            @Param("userId") Long userId);

    /**
     * 获取最近的字典数据
     *
     * @param limit 限制数量
     * @return 最近的字典数据列表
     */
    List<DictDataEntity> selectRecentDictData(@Param("limit") Integer limit);

    /**
     * 获取字典缓存数据
     *
     * @return 字典缓存数据
     */
    List<Map<String, Object>> selectDictCacheData();

    /**
     * 根据字典类型编码获取缓存数据
     *
     * @param dictTypeCode 字典类型编码
     * @return 缓存数据
     */
    List<Map<String, Object>> selectCacheDataByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 导出字典数据
     *
     * @param queryForm 查询条件
     * @return 导出数据
     */
    List<Map<String, Object>> exportDictData(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取字典数据的排序号
     *
     * @param dictTypeId 字典类型ID
     * @return 最大排序号
     */
    Integer getMaxSortOrder(@Param("dictTypeId") Long dictTypeId);

    /**
     * 更新字典数据排序
     *
     * @param dictDataId 字典数据ID
     * @param sortOrder  排序号
     * @param userId     操作人ID
     * @return 影响行数
     */
    int updateSortOrder(@Param("dictDataId") Long dictDataId,
            @Param("sortOrder") Integer sortOrder,
            @Param("userId") Long userId);

    /**
     * 批量更新排序
     *
     * @param sortUpdates 排序更新列表
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchUpdateSortOrder(@Param("sortUpdates") List<Map<String, Object>> sortUpdates,
            @Param("userId") Long userId);
}
