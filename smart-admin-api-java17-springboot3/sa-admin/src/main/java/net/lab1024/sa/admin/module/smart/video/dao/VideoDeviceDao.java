package net.lab1024.sa.admin.module.smart.video.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.base.common.domain.PageParam;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.entity.VideoDeviceEntity;

import java.util.List;

/**
 * 视频设备DAO
 *
 * @author SmartAdmin Team
 * @since 2025-11-16
 */
@Mapper
public interface VideoDeviceDao extends BaseMapper<VideoDeviceEntity> {

    /**
     * 分页查询视频设备
     *
     * @param pageParam 分页参数
     * @param condition 查询条件
     * @return 分页结果
     */
    PageResult<VideoDeviceEntity> selectPage(PageParam pageParam, VideoDeviceEntity condition);

    /**
     * 根据设备编码查询设备
     *
     * @param deviceCode 设备编码
     * @return 设备实体
     */
    VideoDeviceEntity selectByDeviceCode(@Param("deviceCode") String deviceCode);

    /**
     * 根据设备名称模糊查询设备列表
     *
     * @param deviceName 设备名称
     * @return 设备列表
     */
    List<VideoDeviceEntity> selectByDeviceNameLike(@Param("deviceName") String deviceName);

    /**
     * 根据区域ID查询设备列表
     *
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<VideoDeviceEntity> selectByAreaId(@Param("areaId") Long areaId);

    /**
     * 根据设备类型查询设备列表
     *
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<VideoDeviceEntity> selectByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据设备状态查询设备列表
     *
     * @param deviceStatus 设备状态
     * @return 设备列表
     */
    List<VideoDeviceEntity> selectByDeviceStatus(@Param("deviceStatus") String deviceStatus);

    /**
     * 查询所有在线设备
     *
     * @return 在线设备列表
     */
    List<VideoDeviceEntity> selectOnlineDevices();

    /**
     * 查询所有离线设备
     *
     * @return 离线设备列表
     */
    List<VideoDeviceEntity> selectOfflineDevices();

    /**
     * 查询支持云台控制的设备
     *
     * @return 支持云台控制的设备列表
     */
    List<VideoDeviceEntity> selectPTZEnabledDevices();

    /**
     * 查询启用录像的设备
     *
     * @return 启用录像的设备列表
     */
    List<VideoDeviceEntity> selectRecordingEnabledDevices();

    /**
     * 根据制造商查询设备
     *
     * @param manufacturer 制造商
     * @return 设备列表
     */
    List<VideoDeviceEntity> selectByManufacturer(@Param("manufacturer") String manufacturer);

    /**
     * 批量更新设备状态
     *
     * @param deviceIds 设备ID列表
     * @param deviceStatus 新状态
     * @return 更新行数
     */
    int batchUpdateDeviceStatus(@Param("deviceIds") List<Long> deviceIds, @Param("deviceStatus") String deviceStatus);

    /**
     * 统计设备总数
     *
     * @return 设备总数
     */
    Long countTotalDevices();

    /**
     * 统计在线设备数
     *
     * @return 在线设备数
     */
    Long countOnlineDevices();

    /**
     * 统计离线设备数
     *
     * @return 离线设备数
     */
    Long countOfflineDevices();

    /**
     * 统计各类型设备数量
     *
     * @return 统计结果 Map<设备类型, 数量>
     */
    List<java.util.Map<String, Object>> countDevicesByType();

    /**
     * 统计各区域设备数量
     *
     * @return 统计结果 Map<区域ID, 数量>
     */
    List<java.util.Map<String, Object>> countDevicesByArea();
}
