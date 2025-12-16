package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.consume.domain.entity.OfflineConsumeRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线消费记录DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Mapper
public interface OfflineConsumeRecordDao extends BaseMapper<OfflineConsumeRecordEntity> {

    /**
     * 查询待同步的记录
     */
    @Select("SELECT * FROM t_offline_consume_record WHERE sync_status = 'PENDING' " +
            "AND retry_count < #{maxRetry} ORDER BY consume_time ASC LIMIT #{limit}")
    List<OfflineConsumeRecordEntity> selectPendingRecords(@Param("maxRetry") int maxRetry, @Param("limit") int limit);

    /**
     * 根据离线交易编号查询
     */
    @Select("SELECT * FROM t_offline_consume_record WHERE offline_trans_no = #{offlineTransNo}")
    OfflineConsumeRecordEntity selectByOfflineTransNo(@Param("offlineTransNo") String offlineTransNo);

    /**
     * 更新同步状态
     */
    @Update("UPDATE t_offline_consume_record SET sync_status = #{syncStatus}, sync_time = #{syncTime}, " +
            "sync_message = #{syncMessage}, online_record_id = #{onlineRecordId}, update_time = #{syncTime} " +
            "WHERE id = #{id}")
    int updateSyncStatus(@Param("id") Long id, @Param("syncStatus") String syncStatus,
                         @Param("syncTime") LocalDateTime syncTime, @Param("syncMessage") String syncMessage,
                         @Param("onlineRecordId") Long onlineRecordId);

    /**
     * 增加重试次数
     */
    @Update("UPDATE t_offline_consume_record SET retry_count = retry_count + 1, update_time = NOW() WHERE id = #{id}")
    int incrementRetryCount(@Param("id") Long id);

    /**
     * 标记冲突
     */
    @Update("UPDATE t_offline_consume_record SET sync_status = 'CONFLICT', conflict_type = #{conflictType}, " +
            "conflict_status = 'PENDING', sync_message = #{message}, update_time = NOW() WHERE id = #{id}")
    int markConflict(@Param("id") Long id, @Param("conflictType") String conflictType, @Param("message") String message);

    /**
     * 查询冲突记录
     */
    @Select("SELECT * FROM t_offline_consume_record WHERE sync_status = 'CONFLICT' AND conflict_status = 'PENDING'")
    List<OfflineConsumeRecordEntity> selectConflictRecords();

    /**
     * 查询设备的离线记录
     */
    @Select("SELECT * FROM t_offline_consume_record WHERE device_id = #{deviceId} ORDER BY consume_time DESC LIMIT #{limit}")
    List<OfflineConsumeRecordEntity> selectByDeviceId(@Param("deviceId") Long deviceId, @Param("limit") int limit);
}
