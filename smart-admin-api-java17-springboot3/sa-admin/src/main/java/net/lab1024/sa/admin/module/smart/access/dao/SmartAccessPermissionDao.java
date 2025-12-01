package net.lab1024.sa.admin.module.smart.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.smart.access.domain.entity.SmartAccessPermissionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁权限 DAO 接口
 *
 * @author SmartAdmin Team
 * @date 2025-11-15
 */
@Mapper
public interface SmartAccessPermissionDao extends BaseMapper<SmartAccessPermissionEntity> {

    /**
     * 根据人员ID查询有效权限
     *
     * @param personId 人员ID
     * @param currentTime 当前时间
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectValidPermissionsByPersonId(
            @Param("personId") Long personId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 根据设备ID查询有效权限
     *
     * @param deviceId 设备ID
     * @param currentTime 当前时间
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectValidPermissionsByDeviceId(
            @Param("deviceId") Long deviceId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 根据卡片编号查询权限
     *
     * @param cardNumber 卡片编号
     * @param currentTime 当前时间
     * @return 权限实体
     */
    SmartAccessPermissionEntity selectByCardNumber(
            @Param("cardNumber") String cardNumber,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 根据人脸特征ID查询权限
     *
     * @param faceFeatureId 人脸特征ID
     * @param currentTime 当前时间
     * @return 权限实体
     */
    SmartAccessPermissionEntity selectByFaceFeatureId(
            @Param("faceFeatureId") String faceFeatureId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 根据指纹特征ID查询权限
     *
     * @param fingerprintId 指纹特征ID
     * @param currentTime 当前时间
     * @return 权限实体
     */
    SmartAccessPermissionEntity selectByFingerprintId(
            @Param("fingerprintId") String fingerprintId,
            @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 更新权限使用次数
     *
     * @param permissionId 权限ID
     * @return 影响行数
     */
    int incrementAccessCount(@Param("permissionId") Long permissionId);

    /**
     * 查询即将过期的权限
     *
     * @param days 天数阈值
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectExpiringPermissions(@Param("days") Integer days);

    /**
     * 查询已过期的权限
     *
     * @param currentTime 当前时间
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectExpiredPermissions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 批量更新权限状态为已过期
     *
     * @param permissionIds 权限ID列表
     * @return 影响行数
     */
    int batchUpdateToExpired(@Param("permissionIds") List<Long> permissionIds);

    /**
     * 统计权限数量按类型
     *
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countByPermissionType();

    /**
     * 统计权限数量按状态
     *
     * @return 统计结果
     */
    List<java.util.Map<String, Object>> countByPermissionStatus();

    /**
     * 查询待审批的权限
     *
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectPendingApprovals();

    /**
     * 查询指定人员在指定设备的权限
     *
     * @param personId 人员ID
     * @param deviceId 设备ID
     * @param currentTime 当前时间
     * @return 权限列表
     */
    List<SmartAccessPermissionEntity> selectPermissionsByPersonAndDevice(
            @Param("personId") Long personId,
            @Param("deviceId") Long deviceId,
            @Param("currentTime") LocalDateTime currentTime
    );
}