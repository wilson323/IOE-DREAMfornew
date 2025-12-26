package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.VideoDecoderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 解码器数据访问层
 * <p>
 * 提供解码器数据的CRUD操作和复杂查询
 * 严格遵循CLAUDE.md全局架构规范
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
@Transactional(readOnly = true)
public interface VideoDecoderDao extends BaseMapper<VideoDecoderEntity> {

    /**
     * 根据状态查询解码器列表
     *
     * @param deviceStatus 设备状态：1-在线，2-离线，3-故障，4-维护，5-停用
     * @return 解码器列表
     */
    @Select("SELECT * FROM t_video_decoder WHERE device_status = #{deviceStatus} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoDecoderEntity> selectByStatus(@Param("deviceStatus") Integer deviceStatus);

    /**
     * 查询在线解码器列表
     *
     * @return 在线解码器列表
     */
    @Select("SELECT * FROM t_video_decoder WHERE device_status = 1 AND deleted_flag = 0 ORDER BY available_channels DESC, create_time DESC")
    List<VideoDecoderEntity> selectOnlineDecoders();

    /**
     * 根据区域ID查询解码器列表
     *
     * @param areaId 区域ID
     * @return 解码器列表
     */
    @Select("SELECT * FROM t_video_decoder WHERE area_id = #{areaId} AND deleted_flag = 0 ORDER BY create_time DESC")
    List<VideoDecoderEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据编码查询解码器
     *
     * @param decoderCode 解码器编码
     * @return 解码器实体
     */
    @Select("SELECT * FROM t_video_decoder WHERE decoder_code = #{decoderCode} AND deleted_flag = 0 LIMIT 1")
    VideoDecoderEntity selectByCode(@Param("decoderCode") String decoderCode);

    /**
     * 更新解码器状态
     *
     * @param decoderId 解码器ID
     * @param deviceStatus 设备状态
     * @param lastOnlineTime 最后在线时间
     * @return 影响行数
     */
    @Update("UPDATE t_video_decoder SET device_status = #{deviceStatus}, last_online_time = #{lastOnlineTime}, update_time = NOW() WHERE decoder_id = #{decoderId}")
    @Transactional(rollbackFor = Exception.class)
    int updateStatus(@Param("decoderId") Long decoderId, @Param("deviceStatus") Integer deviceStatus, @Param("lastOnlineTime") LocalDateTime lastOnlineTime);

    /**
     * 更新解码器通道使用情况
     *
     * @param decoderId 解码器ID
     * @param usedChannels 已使用通道数
     * @param availableChannels 可用通道数
     * @return 影响行数
     */
    @Update("UPDATE t_video_decoder SET used_channels = #{usedChannels}, available_channels = #{availableChannels}, update_time = NOW() WHERE decoder_id = #{decoderId}")
    @Transactional(rollbackFor = Exception.class)
    int updateChannelUsage(@Param("decoderId") Long decoderId, @Param("usedChannels") Integer usedChannels, @Param("availableChannels") Integer availableChannels);
}
