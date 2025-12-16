package net.lab1024.sa.common.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.domain.entity.SystemDictEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 系统字典DAO
 * 整合自ioedream-system-service
 *
 * 符合CLAUDE.md规范：
 * - 使用@Mapper注解（强制）
 * - 继承BaseMapper提供基础CRUD
 * - 提供完整的字典查询方法
 * - 支持字典缓存管理
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Mapper
public interface SystemDictDao extends BaseMapper<SystemDictEntity> {

    /**
     * 根据字典类型编码查询字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_code = #{dictTypeCode} AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<SystemDictEntity> selectByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 查询所有启用的字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE status = 1 AND deleted_flag = 0 ORDER BY dict_type_code, sort_order")
    List<SystemDictEntity> selectAllEnabled();

    /**
     * 根据字典类型ID查询字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_id = #{dictTypeId} AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<SystemDictEntity> selectByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型ID查询启用的字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_id = #{dictTypeId} AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<SystemDictEntity> selectEnabledByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型编码查询启用的字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_code = #{dictTypeCode} AND status = 1 AND deleted_flag = 0 ORDER BY sort_order ASC")
    List<SystemDictEntity> selectEnabledByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 检查字典值在字典类型下是否唯一
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_sys_dict_data " +
            "WHERE dict_type_id = #{dictTypeId} AND dict_value = #{dictValue} AND deleted_flag = 0 " +
            "<if test='excludeId != null'>AND dict_data_id != #{excludeId}</if>" +
            "</script>")
    int checkDictValueUnique(@Param("dictTypeId") Long dictTypeId,
                             @Param("dictValue") String dictValue,
                             @Param("excludeId") Long excludeId);

    /**
     * 修改字典数据状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_sys_dict_data SET status = #{status}, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE dict_data_id = #{dictDataId}")
    int updateDictDataStatus(@Param("dictDataId") Long dictDataId,
                             @Param("status") Integer status,
                             @Param("userId") Long userId);

    /**
     * 批量修改字典数据状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_sys_dict_data SET status = #{status}, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE dict_data_id IN " +
            "<foreach collection='dictDataIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateDictDataStatus(@Param("dictDataIds") List<Long> dictDataIds,
                                   @Param("status") Integer status,
                                   @Param("userId") Long userId);

    /**
     * 统计字典数据数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM t_sys_dict_data WHERE deleted_flag = 0 " +
            "<if test='dictTypeId != null'>AND dict_type_id = #{dictTypeId}</if> " +
            "<if test='dictTypeCode != null'>AND dict_type_code = #{dictTypeCode}</if> " +
            "<if test='status != null'>AND status = #{status}</if>" +
            "</script>")
    Long countDictData(@Param("dictTypeId") Long dictTypeId,
                       @Param("dictTypeCode") String dictTypeCode,
                       @Param("status") Integer status);

    /**
     * 获取字典数据统计信息
     */
    List<Map<String, Object>> getDictDataStatistics();

    /**
     * 根据字典类型ID获取字典数据数量
     */
    @Select("SELECT COUNT(*) FROM t_sys_dict_data WHERE dict_type_id = #{dictTypeId} AND deleted_flag = 0")
    Long countDictDataByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 获取指定字典类型的默认值
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_id = #{dictTypeId} AND is_default = 1 AND deleted_flag = 0 LIMIT 1")
    SystemDictEntity selectDefaultByTypeId(@Param("dictTypeId") Long dictTypeId);

    /**
     * 根据字典类型编码获取默认值
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_code = #{dictTypeCode} AND is_default = 1 AND deleted_flag = 0 LIMIT 1")
    SystemDictEntity selectDefaultByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 根据状态查询字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE status = #{status} AND deleted_flag = 0 ORDER BY dict_type_code, sort_order")
    List<SystemDictEntity> selectByStatus(@Param("status") Integer status);

    /**
     * 根据字典值查询字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_value = #{dictValue} AND deleted_flag = 0")
    List<SystemDictEntity> selectByDictValue(@Param("dictValue") String dictValue);

    /**
     * 根据字典标签模糊查询字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_label LIKE CONCAT('%', #{dictLabel}, '%') AND deleted_flag = 0")
    List<SystemDictEntity> selectByDictLabel(@Param("dictLabel") String dictLabel);

    /**
     * 获取字典数据及其类型信息
     */
    List<Map<String, Object>> selectDictDataWithType();

    /**
     * 批量插入字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    int batchInsertDictData(@Param("dictDataList") List<SystemDictEntity> dictDataList);

    /**
     * 批量更新字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateDictData(@Param("dictDataList") List<SystemDictEntity> dictDataList);

    /**
     * 批量删除字典数据（逻辑删除）
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_sys_dict_data SET deleted_flag = 1, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE dict_data_id IN " +
            "<foreach collection='dictDataIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int batchDeleteDictData(@Param("dictDataIds") List<Long> dictDataIds,
                            @Param("userId") Long userId);

    /**
     * 根据字典类型ID删除所有字典数据
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_sys_dict_data SET deleted_flag = 1, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE dict_type_id = #{dictTypeId}")
    int deleteDictDataByTypeId(@Param("dictTypeId") Long dictTypeId,
                               @Param("userId") Long userId);

    /**
     * 获取最近的字典数据
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE deleted_flag = 0 ORDER BY create_time DESC LIMIT #{limit}")
    List<SystemDictEntity> selectRecentDictData(@Param("limit") Integer limit);

    /**
     * 获取字典缓存数据
     */
    List<Map<String, Object>> selectDictCacheData();

    /**
     * 根据字典类型编码获取缓存数据
     */
    List<Map<String, Object>> selectCacheDataByTypeCode(@Param("dictTypeCode") String dictTypeCode);

    /**
     * 导出字典数据
     */
    List<Map<String, Object>> exportDictData(@Param("dictTypeId") Long dictTypeId,
                                             @Param("dictTypeCode") String dictTypeCode,
                                             @Param("status") Integer status);

    /**
     * 获取字典数据的排序号
     */
    @Select("SELECT COALESCE(MAX(sort_order), 0) FROM t_sys_dict_data WHERE dict_type_id = #{dictTypeId} AND deleted_flag = 0")
    Integer getMaxSortOrder(@Param("dictTypeId") Long dictTypeId);

    /**
     * 更新字典数据排序
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_sys_dict_data SET sort_order = #{sortOrder}, update_time = NOW(), update_user_id = #{userId} " +
            "WHERE dict_data_id = #{dictDataId}")
    int updateSortOrder(@Param("dictDataId") Long dictDataId,
                        @Param("sortOrder") Integer sortOrder,
                        @Param("userId") Long userId);

    /**
     * 批量更新排序
     */
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateSortOrder(@Param("sortUpdates") List<Map<String, Object>> sortUpdates,
                             @Param("userId") Long userId);

    /**
     * 清除其他默认值
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_sys_dict_data SET is_default = 0, update_time = NOW() " +
            "WHERE dict_type_id = #{dictTypeId} AND dict_data_id != #{excludeId}")
    int clearOtherDefaultValues(@Param("dictTypeId") Long dictTypeId,
                                 @Param("excludeId") Long excludeId);

    /**
     * 根据字典类型编码和字典值查询
     */
    @Select("SELECT * FROM t_sys_dict_data WHERE dict_type_code = #{dictTypeCode} AND dict_value = #{dictValue} AND deleted_flag = 0")
    SystemDictEntity selectByTypeCodeAndValue(@Param("dictTypeCode") String dictTypeCode,
                                              @Param("dictValue") String dictValue);

    /**
     * 查询字典数据树形结构
     */
    List<Map<String, Object>> selectDictTree(@Param("dictTypeCode") String dictTypeCode);
}
