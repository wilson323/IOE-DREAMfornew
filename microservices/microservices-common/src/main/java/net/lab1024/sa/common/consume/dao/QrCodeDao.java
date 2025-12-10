package net.lab1024.sa.common.consume.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.common.consume.entity.QrCodeEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 二维码数据访问层
 * <p>
 * 企业级二维码数据访问接口，提供完整的CRUD操作和查询功能
 * 严格遵循CLAUDE.md全局架构规范：
 * - 统一使用 @Mapper 注解，禁止使用 @Repository
 * - 必须继承 BaseMapper<Entity>
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-09
 */
@Mapper
public interface QrCodeDao extends BaseMapper<QrCodeEntity> {

    /**
     * 根据二维码标识查询
     *
     * @param qrToken 二维码标识
     * @return 二维码实体
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE qr_token = #{qrToken} AND deleted_flag = 0")
    QrCodeEntity selectByToken(@Param("qrToken") String qrToken);

    /**
     * 根据用户ID查询二维码列表
     *
     * @param userId 用户ID
     * @return 二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE user_id = #{userId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<QrCodeEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和类型查询二维码列表
     *
     * @param userId 用户ID
     * @param qrType 二维码类型
     * @return 二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE user_id = #{userId} AND qr_type = #{qrType} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<QrCodeEntity> selectByUserIdAndType(@Param("userId") Long userId, @Param("qrType") Integer qrType);

    /**
     * 根据用户ID和业务模块查询二维码列表
     *
     * @param userId 用户ID
     * @param businessModule 业务模块
     * @return 二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE user_id = #{userId} AND business_module = #{businessModule} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<QrCodeEntity> selectByUserIdAndModule(@Param("userId") Long userId, @Param("businessModule") String businessModule);

    /**
     * 查询有效的二维码
     *
     * @param currentTime 当前时间
     * @return 有效二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE qr_status = 1 AND " +
            "(expire_time IS NULL OR expire_time > #{currentTime}) AND deleted_flag = 0")
    List<QrCodeEntity> selectValidQrCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 查询过期的二维码
     *
     * @param currentTime 当前时间
     * @return 过期二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE qr_status = 1 AND expire_time <= #{currentTime} AND deleted_flag = 0")
    List<QrCodeEntity> selectExpiredQrCodes(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 根据区域ID查询二维码列表
     *
     * @param areaId 区域ID
     * @return 二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<QrCodeEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据设备ID查询二维码列表
     *
     * @param deviceId 设备ID
     * @return 二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE device_id = #{deviceId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<QrCodeEntity> selectByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 分页查询二维码
     *
     * @param page 分页对象
     * @param userId 用户ID（可选）
     * @param qrType 二维码类型（可选）
     * @param businessModule 业务模块（可选）
     * @param qrStatus 二维码状态（可选）
     * @param areaId 区域ID（可选）
     * @return 分页结果
     */
    default IPage<QrCodeEntity> selectPageByCondition(Page<QrCodeEntity> page,
                                                      @Param("userId") Long userId,
                                                      @Param("qrType") Integer qrType,
                                                      @Param("businessModule") String businessModule,
                                                      @Param("qrStatus") Integer qrStatus,
                                                      @Param("areaId") Long areaId) {
        LambdaQueryWrapper<QrCodeEntity> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(userId != null, QrCodeEntity::getUserId, userId)
               .eq(qrType != null, QrCodeEntity::getQrType, qrType)
               .eq(businessModule != null, QrCodeEntity::getBusinessModule, businessModule)
               .eq(qrStatus != null, QrCodeEntity::getQrStatus, qrStatus)
               .eq(areaId != null, QrCodeEntity::getAreaId, areaId)
               .eq(QrCodeEntity::getDeletedFlag, 0)
               .orderByDesc(QrCodeEntity::getCreateTime);

        return selectPage(page, wrapper);
    }

    /**
     * 批量更新二维码状态
     *
     * @param qrIds 二维码ID列表
     * @param status 新状态
     * @return 更新行数
     */
    default int updateStatusBatch(@Param("qrIds") List<String> qrIds, @Param("status") Integer status) {
        if (qrIds == null || qrIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<QrCodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(QrCodeEntity::getQrId, qrIds);

        QrCodeEntity updateEntity = new QrCodeEntity();
        updateEntity.setQrStatus(status);

        return update(updateEntity, wrapper);
    }

    /**
     * 统计用户二维码数量
     *
     * @param userId 用户ID
     * @return 统计结果
     */
    @Select("SELECT " +
            "qr_type, " +
            "COUNT(*) as total_count, " +
            "SUM(CASE WHEN qr_status = 1 THEN 1 ELSE 0 END) as valid_count, " +
            "SUM(CASE WHEN qr_status = 2 THEN 1 ELSE 0 END) as expired_count, " +
            "SUM(CASE WHEN qr_status = 3 THEN 1 ELSE 0 END) as used_count " +
            "FROM t_consume_qrcode " +
            "WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "GROUP BY qr_type")
    List<java.util.Map<String, Object>> selectUserQrCodeStatistics(@Param("userId") Long userId);

    /**
     * 查询即将过期的二维码
     *
     * @param currentTime 当前时间
     * @param expireThreshold 过期阈值（分钟）
     * @return 即将过期的二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE qr_status = 1 AND " +
            "expire_time > #{currentTime} AND " +
            "expire_time <= DATE_ADD(#{currentTime}, INTERVAL #{expireThreshold} MINUTE) " +
            "AND deleted_flag = 0 ORDER BY expire_time ASC")
    List<QrCodeEntity> selectSoonToExpireQrCodes(@Param("currentTime") LocalDateTime currentTime,
                                                  @Param("expireThreshold") Integer expireThreshold);

    /**
     * 查询高频使用的二维码
     *
     * @param minUsageCount 最小使用次数
     * @param days 查询天数
     * @return 高频使用的二维码列表
     */
    @Select("SELECT * FROM t_consume_qrcode WHERE used_count >= #{minUsageCount} AND " +
            "last_used_time >= DATE_SUB(NOW(), INTERVAL #{days} DAY) " +
            "AND deleted_flag = 0 ORDER BY used_count DESC")
    List<QrCodeEntity> selectHighUsageQrCodes(@Param("minUsageCount") Integer minUsageCount,
                                             @Param("days") Integer days);

    /**
     * 更新二维码使用信息
     *
     * @param qrId 二维码ID
     * @param usedCount 使用次数
     * @param lastUsedTime 最后使用时间
     * @param lastUsedDevice 最后使用设备
     * @param lastUsedLocation 最后使用位置
     * @return 更新行数
     */
    @Select("UPDATE t_consume_qrcode SET " +
            "used_count = #{usedCount}, " +
            "last_used_time = #{lastUsedTime}, " +
            "last_used_device = #{lastUsedDevice}, " +
            "last_used_location = #{lastUsedLocation} " +
            "WHERE qr_id = #{qrId} AND deleted_flag = 0")
    int updateUsageInfo(@Param("qrId") String qrId,
                        @Param("usedCount") Integer usedCount,
                        @Param("lastUsedTime") LocalDateTime lastUsedTime,
                        @Param("lastUsedDevice") String lastUsedDevice,
                        @Param("lastUsedLocation") String lastUsedLocation);

    /**
     * 软删除二维码
     *
     * @param qrId 二维码ID
     * @return 删除行数
     */
    default int softDeleteById(@Param("qrId") String qrId) {
        QrCodeEntity updateEntity = new QrCodeEntity();
        updateEntity.setQrId(qrId);
        updateEntity.setDeletedFlag(1);

        LambdaQueryWrapper<QrCodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QrCodeEntity::getQrId, qrId);

        return update(updateEntity, wrapper);
    }

    /**
     * 批量软删除二维码
     *
     * @param qrIds 二维码ID列表
     * @return 删除行数
     */
    default int softDeleteBatch(@Param("qrIds") List<String> qrIds) {
        if (qrIds == null || qrIds.isEmpty()) {
            return 0;
        }

        LambdaQueryWrapper<QrCodeEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(QrCodeEntity::getQrId, qrIds);

        QrCodeEntity updateEntity = new QrCodeEntity();
        updateEntity.setDeletedFlag(1);

        return update(updateEntity, wrapper);
    }

    /**
     * 检查二维码是否存在
     *
     * @param qrId 二维码ID
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_qrcode WHERE qr_id = #{qrId} AND deleted_flag = 0")
    int checkExists(@Param("qrId") String qrId);

    /**
     * 检查二维码标识是否存在
     *
     * @param qrToken 二维码标识
     * @return 是否存在
     */
    @Select("SELECT COUNT(1) FROM t_consume_qrcode WHERE qr_token = #{qrToken} AND deleted_flag = 0")
    int checkTokenExists(@Param("qrToken") String qrToken);
}