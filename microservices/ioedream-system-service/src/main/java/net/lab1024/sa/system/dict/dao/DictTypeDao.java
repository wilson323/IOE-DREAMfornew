package net.lab1024.sa.system.dict.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.system.dict.domain.entity.DictTypeEntity;

/**
 * 字典类型DAO
 * <p>
 * 提供字典类型的数据访问操作
 *
 * @author IOE-DREAM Team
 * @date 2025/11/29
 */
@Mapper
public interface DictTypeDao extends BaseMapper<DictTypeEntity> {

    /**
     * 分页查询字典类型
     *
     * @param queryForm 查询条件
     * @return 字典类型列表
     */
    List<DictTypeEntity> selectDictTypePage(@Param("query") Map<String, Object> queryForm);

    /**
     * 根据条件查询字典类型列表
     *
     * @param queryForm 查询条件
     * @return 字典类型列表
     */
    List<DictTypeEntity> selectDictTypeList(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取所有字典类型
     *
     * @return 字典类型列表
     */
    List<DictTypeEntity> selectAllDictTypes();

    /**
     * 根据ID查询字典类型详情
     *
     * @param dictTypeId 字典类型ID
     * @return 字典类型详情
     */
    DictTypeEntity selectDictTypeDetail(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型编码查询字典类型
     *
     * @param dictTypeCode 字典类型编码
     * @return 字典类型
     */
    DictTypeEntity selectByDictTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 检查字典类型编码是否唯一
     *
     * @param dictTypeCode 字典类型编码
     * @param excludeId    排除的字典类型ID
     * @return 是否唯一（0-不唯一，1-唯一）
     */
    int checkDictTypeCodeUnique(@Param("dictTypeCode") String dictTypeCode,
            @Param("excludeId") Long excludeId);

    /**
     * 修改字典类型状态
     *
     * @param dictTypeId 字典类型ID
     * @param status     状态
     * @param userId     操作人ID
     * @return 影响行数
     */
    int updateDictTypeStatus(@Param("dictTypeId") Long dictTypeId,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 批量修改字典类型状态
     *
     * @param dictTypeIds 字典类型ID列表
     * @param status      状态
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchUpdateDictTypeStatus(@Param("dictTypeIds") List<Long> dictTypeIds,
            @Param("status") Integer status,
            @Param("userId") Long userId);

    /**
     * 统计字典类型数量
     *
     * @param queryForm 查询条件
     * @return 统计数量
     */
    Long countDictType(@Param("query") Map<String, Object> queryForm);

    /**
     * 获取字典类型统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getDictTypeStatistics();

    /**
     * 查询启用的字典类型
     *
     * @return 启用的字典类型列表
     */
    List<DictTypeEntity> selectEnabledDictTypes();

    /**
     * 查询系统内置字典类型
     *
     * @return 系统内置字典类型列表
     */
    List<DictTypeEntity> selectSystemDictTypes();

    /**
     * 根据名称模糊查询字典类型
     *
     * @param dictTypeName 字典类型名称
     * @return 字典类型列表
     */
    List<DictTypeEntity> selectByDictTypeName(@Param("dictTypeName") String dictTypeName);

    /**
     * 根据状态查询字典类型
     *
     * @param status 状态
     * @return 字典类型列表
     */
    List<DictTypeEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 获取字典类型及其数据数量
     *
     * @return 字典类型及数据数量
     */
    List<Map<String, Object>> selectDictTypeWithDataCount();

    /**
     * 批量插入字典类型
     *
     * @param dictTypeList 字典类型列表
     * @return 影响行数
     */
    int batchInsertDictType(@Param("dictTypeList") List<DictTypeEntity> dictTypeList);

    /**
     * 批量更新字典类型
     *
     * @param dictTypeList 字典类型列表
     * @return 影响行数
     */
    int batchUpdateDictType(@Param("dictTypeList") List<DictTypeEntity> dictTypeList);

    /**
     * 批量删除字典类型（逻辑删除）
     *
     * @param dictTypeIds 字典类型ID列表
     * @param userId      操作人ID
     * @return 影响行数
     */
    int batchDeleteDictType(@Param("dictTypeIds") List<Long> dictTypeIds,
            @Param("userId") Long userId);

    /**
     * 获取最近的字典类型
     *
     * @param limit 限制数量
     * @return 最近的字典类型列表
     */
    List<DictTypeEntity> selectRecentDictTypes(@Param("limit") Integer limit);

    /**
     * 检查字典类型是否被使用
     *
     * @param dictTypeId 字典类型ID
     * @return 是否被使用（0-未使用，1-已使用）
     */
    int checkDictTypeIsUsed(@Param("dictTypeId") Long dictTypeId);

    /**
     * 获取字典类型的使用情况
     *
     * @param dictTypeId 字典类型ID
     * @return 使用情况
     */
    Map<String, Object> getDictTypeUsage(@Param("dictTypeId") Long dictTypeId);
}
