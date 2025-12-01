package net.lab1024.sa.consume.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.domain.entity.ConsumeRecordEntity;

/**
 * 消费记录DAO接口
 *
 * @author SmartAdmin Team
 * @date 2025/11/17
 */
@Mapper
public interface ConsumeRecordDao extends BaseMapper<ConsumeRecordEntity> {

    /**
     * 根据设备ID和时间范围查询消费记录
     *
     * @param deviceId 设备ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM consume_record WHERE device_id = #{deviceId} " +
            "AND consume_time >= #{startTime} AND consume_time <= #{endTime} " +
            "AND deleted = 0 ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByDeviceIdAndTimeRange(@Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围查询消费记录
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 消费记录列表
     */
    @Select("SELECT * FROM consume_record WHERE consume_time >= #{startTime} " +
            "AND consume_time <= #{endTime} AND deleted = 0 ORDER BY consume_time DESC")
    List<ConsumeRecordEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}