package net.lab1024.sa.access.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.organization.entity.AreaPersonEntity;

/**
 * 人员区域授权DAO
 * <p>
 * 严格遵循DAO架构规范：
 * - 统一DAO模式，使用Dao命名
 * - 使用@Mapper注解，禁止使用@Repository
 * - 查询方法使用@Transactional(readOnly = true)
 * - 继承BaseMapper使用MyBatis-Plus
 * - 职责单一：只负责人员区域授权数据访问
 * - 使用公共AreaPersonEntity替代自定义Entity
 *
 * @author SmartAdmin Team
 * @date 2025/11/18
 * @updated 2025-12-01 遵循DAO架构规范重构
 * @updated 2025-12-02 使用公共AreaPersonEntity，遵循repowiki规范
 */
@Mapper
public interface AreaPersonDao extends BaseMapper<AreaPersonEntity> {

    /**
     * 根据人员ID查询区域ID列表
     *
     * @param personId 人员ID
     * @return 区域ID列表
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByPersonId(@Param("personId") Long personId);

    /**
     * 根据时间范围查询有效权限
     *
     * @param personId  人员ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 权限列表
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getEffectivePermissionsByTimeRange(
            @Param("personId") Long personId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询即将过期的权限
     *
     * @param days 天数
     * @return 即将过期的权限列表
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getExpiringPermissions(@Param("days") Integer days);

    /**
     * 清理过期权限
     *
     * @param expireTime 过期时间
     * @return 清理数量
     */
    @Transactional(rollbackFor = Exception.class)
    int cleanExpiredPermissions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 统计区域权限数量
     *
     * @param areaId 区域ID
     * @return 权限数量
     */
    @Transactional(readOnly = true)
    Long countAreaPermissions(@Param("areaId") Long areaId);

    /**
     * 统计人员在指定区域的权限数量
     *
     * @param personId 人员ID
     * @return 权限数量
     */
    @Transactional(readOnly = true)
    Long countPersonPermissionsByArea(@Param("personId") Long personId);

    /**
     * 获取区域权限统计信息
     *
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> getAreaPermissionStatistics();

    /**
     * 根据人员ID列表查询权限
     *
     * @param personIds 人员ID列表
     * @return 权限列表
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> selectByPersonIds(@Param("personIds") List<Long> personIds);

    /**
     * 批量检查区域权限
     *
     * @param personAreaMap 人员ID到区域ID列表的映射
     * @return 检查结果
     */
    @Transactional(readOnly = true)
    List<AreaPersonEntity> batchCheckAreaPermission(@Param("personAreaMap") Map<Long, List<Long>> personAreaMap);

    /**
     * 检查是否具有区域权限
     *
     * @param personId 人员ID
     * @param areaId   区域ID
     * @return 是否具有权限
     */
    @Transactional(readOnly = true)
    boolean hasAreaPermission(@Param("personId") Long personId, @Param("areaId") Long areaId);

    /**
     * 检查是否具有区域路径权限
     *
     * @param personId 人员ID
     * @param areaPath 区域路径
     * @return 是否具有权限
     */
    @Transactional(readOnly = true)
    boolean hasAreaPathPermission(@Param("personId") Long personId, @Param("areaPath") String areaPath);

    /**
     * 根据数据范围获取区域ID列表
     *
     * @param personId  人员ID
     * @param dataScope 数据范围
     * @return 区域ID列表
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByDataScope(@Param("personId") Long personId, @Param("dataScope") String dataScope);

    /**
     * 批量插入区域权限
     *
     * @param areaPersonList 区域权限列表
     * @return 插入数量
     */
    @Transactional(rollbackFor = Exception.class)
    int batchInsert(@Param("areaPersonList") List<AreaPersonEntity> areaPersonList);

    /**
     * 根据区域ID批量删除权限
     *
     * @param areaIds    区域ID列表
     * @param operatorId 操作人ID
     * @return 删除数量
     */
    @Transactional(rollbackFor = Exception.class)
    int batchDeleteByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("operatorId") Long operatorId);

    /**
     * 根据区域ID批量更新状态
     *
     * @param areaIds    区域ID列表
     * @param operatorId 操作人ID
     * @param status     状态
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    int batchUpdateStatusByAreaIds(@Param("areaIds") List<Long> areaIds, @Param("operatorId") Long operatorId,
            @Param("status") Integer status);

    /**
     * 获取所有授权的区域ID列表
     *
     * @param personId 人员ID
     * @return 区域ID列表
     */
    @Transactional(readOnly = true)
    List<Long> getAllAuthorizedAreaIds(@Param("personId") Long personId);

    /**
     * 获取直接授权的区域ID列表
     *
     * @param personId 人员ID
     * @return 区域ID列表
     */
    @Transactional(readOnly = true)
    List<Long> getDirectAuthorizedAreaIds(@Param("personId") Long personId);

    /**
     * 根据路径前缀获取区域ID列表
     *
     * @param personId   人员ID
     * @param pathPrefix 路径前缀
     * @return 区域ID列表
     */
    @Transactional(readOnly = true)
    List<Long> getAreaIdsByPathPrefix(@Param("personId") Long personId, @Param("pathPrefix") String pathPrefix);

    /**
     * 获取人员的区域路径列表
     *
     * @param personId 人员ID
     * @return 区域路径列表
     */
    @Transactional(readOnly = true)
    List<String> getAreaPathsByPersonId(@Param("personId") Long personId);
}

