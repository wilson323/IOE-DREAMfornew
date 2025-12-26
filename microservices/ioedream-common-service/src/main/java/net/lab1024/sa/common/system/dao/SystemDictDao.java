package net.lab1024.sa.common.system.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.domain.entity.SystemDictEntity;

@Mapper
public interface SystemDictDao extends BaseMapper<SystemDictEntity> {

    List<SystemDictEntity> selectByTypeCode(@Param("typeCode") String typeCode);

    List<SystemDictEntity> selectAllEnabled();

    List<SystemDictEntity> selectByTypeId(@Param("typeId") Long typeId);

    List<SystemDictEntity> selectEnabledByTypeId(@Param("typeId") Long typeId);

    List<SystemDictEntity> selectEnabledByTypeCode(@Param("typeCode") String typeCode);

    int updateDictDataStatus(@Param("dictId") Long dictId,
                             @Param("status") Integer status,
                             @Param("updateUserId") Long updateUserId);

    int batchUpdateDictDataStatus(@Param("dictIds") List<Long> dictIds,
                                  @Param("status") Integer status,
                                  @Param("updateUserId") Long updateUserId);

    Long countDictData(@Param("typeId") Long typeId,
                       @Param("dictValue") String dictValue,
                       @Param("status") Integer status);

    List<Map<String, Object>> getDictDataStatistics();

    Long countDictDataByTypeId(@Param("typeId") Long typeId);

    SystemDictEntity selectDefaultByTypeId(@Param("typeId") Long typeId);

    SystemDictEntity selectDefaultByTypeCode(@Param("typeCode") String typeCode);

    List<SystemDictEntity> selectByStatus(@Param("status") Integer status);

    List<SystemDictEntity> selectByDictValue(@Param("dictValue") String dictValue);

    List<SystemDictEntity> selectByDictLabel(@Param("dictLabel") String dictLabel);

    List<Map<String, Object>> selectDictDataWithType();

    int batchInsertDictData(@Param("dictList") List<SystemDictEntity> dictList);

    int batchUpdateDictData(@Param("dictList") List<SystemDictEntity> dictList);

    int batchDeleteDictData(@Param("dictIds") List<Long> dictIds,
                            @Param("updateUserId") Long updateUserId);

    int deleteDictDataByTypeId(@Param("typeId") Long typeId,
                               @Param("updateUserId") Long updateUserId);

    List<SystemDictEntity> selectRecentDictData(@Param("limit") Integer limit);

    List<Map<String, Object>> selectDictCacheData();

    List<Map<String, Object>> selectCacheDataByTypeCode(@Param("typeCode") String typeCode);

    List<Map<String, Object>> exportDictData(@Param("typeId") Long typeId,
                                             @Param("dictLabel") String dictLabel,
                                             @Param("status") Integer status);

    Integer getMaxSortOrder(@Param("typeId") Long typeId);

    int updateSortOrder(@Param("dictId") Long dictId,
                        @Param("sortOrder") Integer sortOrder,
                        @Param("updateUserId") Long updateUserId);

    int batchUpdateSortOrder(@Param("updates") List<Map<String, Object>> updates,
                             @Param("updateUserId") Long updateUserId);

    int clearOtherDefaultValues(@Param("typeId") Long typeId,
                                @Param("dictId") Long dictId);

    SystemDictEntity selectByTypeCodeAndValue(@Param("typeCode") String typeCode,
                                              @Param("dictValue") String dictValue);

    List<Map<String, Object>> selectDictTree(@Param("typeCode") String typeCode);

    /**
     * 统计字典总数
     */
    @Select("SELECT COUNT(*) FROM t_system_dict WHERE deleted_flag = 0")
    long selectCount();

    /**
     * 完整的字典值唯一性检查
     */
    @Select("SELECT COUNT(*) FROM t_system_dict WHERE dict_type_id = #{typeId} AND dict_value = #{dictValue} AND deleted_flag = 0 AND dict_id != #{excludeDictId}")
    long checkDictValueUnique(@Param("typeId") Long typeId, @Param("dictValue") String dictValue, @Param("excludeDictId") Long excludeDictId);
}
