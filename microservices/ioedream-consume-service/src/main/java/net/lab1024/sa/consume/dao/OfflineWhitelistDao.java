package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.consume.OfflineWhitelistEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 离线消费白名单DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface OfflineWhitelistDao extends BaseMapper<OfflineWhitelistEntity> {

    /**
     * 查询用户有效白名单
     */
    List<OfflineWhitelistEntity> selectValidByUserId(
        @Param("userId") Long userId,
        @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 查询设备有效白名单
     */
    List<OfflineWhitelistEntity> selectValidByDeviceId(
        @Param("deviceId") Long deviceId,
        @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * 查询区域有效白名单
     */
    List<OfflineWhitelistEntity> selectValidByAreaId(
        @Param("areaId") Long areaId,
        @Param("currentTime") LocalDateTime currentTime
    );
}
