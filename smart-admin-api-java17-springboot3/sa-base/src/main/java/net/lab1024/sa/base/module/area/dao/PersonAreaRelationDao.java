package net.lab1024.sa.base.module.area.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import net.lab1024.sa.base.module.area.domain.entity.PersonAreaRelationEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 人员区域关联数据访问对象
 * 提供人员与区域关联关系的数据访问操作
 *
 * @author SmartAdmin Team
 * @since 2025-11-24
 */
@Mapper
public interface PersonAreaRelationDao extends BaseMapper<PersonAreaRelationEntity> {

    /**
     * 根据人员ID查询关联关系
     *
     * @param personId 人员ID
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByPersonId(@Param("personId") Long personId);

    /**
     * 根据区域ID查询关联关系
     *
     * @param areaId 区域ID
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据人员类型查询关联关系
     *
     * @param personType 人员类型
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByPersonType(@Param("personType") String personType);

    /**
     * 根据关联类型查询关联关系
     *
     * @param relationType 关联类型
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByRelationType(@Param("relationType") String relationType);

    /**
     * 查询有效的关联关系
     *
     * @param currentTime 当前时间
     * @return 有效关联关系列表
     */
    List<PersonAreaRelationEntity> selectActiveRelations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询过期的关联关系
     *
     * @param currentTime 当前时间
     * @return 过期关联关系列表
     */
    List<PersonAreaRelationEntity> selectExpiredRelations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据人员和区域查询关联关系
     *
     * @param personId 人员ID
     * @param areaId 区域ID
     * @return 关联关系
     */
    PersonAreaRelationEntity selectByPersonAndArea(@Param("personId") Long personId, @Param("areaId") Long areaId);

    /**
     * 批量查询人员的所有关联区域
     *
     * @param personIds 人员ID列表
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByPersonIds(@Param("personIds") List<Long> personIds);

    /**
     * 批量查询区域的所有关联人员
     *
     * @param areaIds 区域ID列表
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByAreaIds(@Param("areaIds") List<Long> areaIds);

    /**
     * 递归查询人员可访问的所有区域（包括子区域）
     *
     * @param personId 人员ID
     * @return 可访问区域ID列表
     */
    List<Long> selectAccessibleAreaIds(@Param("personId") Long personId);

    /**
     * 递归查询区域下的所有关联人员（包括子区域）
     *
     * @param areaId 区域ID
     * @return 关联人员ID列表
     */
    List<Long> selectAreaPersonIds(@Param("areaId") Long areaId);

    /**
     * 查询即将过期的关联关系
     *
     * @param currentTime 当前时间
     * @param days 天数
     * @return 即将过期关联关系列表
     */
    List<PersonAreaRelationEntity> selectExpiringSoon(@Param("currentTime") LocalDateTime currentTime, @Param("days") Integer days);

    /**
     * 统计人员关联数量
     *
     * @param personId 人员ID
     * @return 关联数量
     */
    Integer countByPersonId(@Param("personId") Long personId);

    /**
     * 统计区域关联数量
     *
     * @param areaId 区域ID
     * @return 关联数量
     */
    Integer countByAreaId(@Param("areaId") Long areaId);

    /**
     * 检查关联关系是否存在
     *
     * @param personId 人员ID
     * @param areaId 区域ID
     * @param relationType 关联类型
     * @return 是否存在
     */
    boolean existsRelation(@Param("personId") Long personId, @Param("areaId") Long areaId, @Param("relationType") String relationType);

    /**
     * 更新关联关系状态
     *
     * @param relationId 关联ID
     * @param status 状态
     * @return 更新行数
     */
    int updateStatus(@Param("relationId") Long relationId, @Param("status") String status);

    /**
     * 批量更新关联关系状态
     *
     * @param relationIds 关联ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatus(@Param("relationIds") List<Long> relationIds, @Param("status") String status);

    /**
     * 删除人员的所有关联关系
     *
     * @param personId 人员ID
     * @return 删除行数
     */
    int deleteByPersonId(@Param("personId") Long personId);

    /**
     * 删除区域的所有关联关系
     *
     * @param areaId 区域ID
     * @return 删除行数
     */
    int deleteByAreaId(@Param("areaId") Long areaId);

    /**
     * 批量删除关联关系
     *
     * @param relationIds 关联ID列表
     * @return 删除行数
     */
    int batchDelete(@Param("relationIds") List<Long> relationIds);

    /**
     * 查询需要自动续期的关联关系
     *
     * @param currentTime 当前时间
     * @return 需要续期的关联关系列表
     */
    List<PersonAreaRelationEntity> selectAutoRenewRelations(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 更新关联关系的失效时间
     *
     * @param relationId 关联ID
     * @param expireTime 失效时间
     * @return 更新行数
     */
    int updateExpireTime(@Param("relationId") Long relationId, @Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据人员和区域批量更新状态
     *
     * @param personId 人员ID
     * @param areaIds 区域ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatusByPersonAndAreas(@Param("personId") Long personId, @Param("areaIds") List<Long> areaIds, @Param("status") Integer status);

    /**
     * 批量根据ID更新状态
     *
     * @param relationIds 关联ID列表
     * @param status 状态
     * @return 更新行数
     */
    int batchUpdateStatusByIds(@Param("relationIds") List<Long> relationIds, @Param("status") Integer status);

    /**
     * 批量根据ID删除关联关系
     *
     * @param relationIds 关联ID列表
     * @return 删除行数
     */
    int batchDeleteByIds(@Param("relationIds") List<Long> relationIds);

    /**
     * 根据人员ID和时间范围查询关联关系
     *
     * @param personId 人员ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 关联关系列表
     */
    List<PersonAreaRelationEntity> selectByPersonIdAndTimeRange(@Param("personId") Long personId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}