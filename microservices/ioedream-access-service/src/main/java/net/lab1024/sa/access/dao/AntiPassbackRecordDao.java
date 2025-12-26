package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AntiPassbackRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 门禁反潜回检测记录DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AntiPassbackRecordDao extends BaseMapper<AntiPassbackRecordEntity> {

    /**
     * 查询用户在指定区域和时间窗口内的通行记录
     *
     * @param userId 用户ID
     * @param areaId 区域ID（null表示全局模式）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通行记录列表
     */
    @Select("""
            SELECT * FROM t_access_anti_passback_record
            WHERE user_id = #{userId}
            AND deleted_flag = 0
            AND pass_time >= #{startTime}
            AND pass_time < #{endTime}
            AND (#{areaId} IS NULL OR area_id = #{areaId})
            ORDER BY pass_time DESC
            """)
    List<AntiPassbackRecordEntity> queryRecentPasses(
            @Param("userId") Long userId,
            @Param("areaId") Long areaId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户在指定区域和时间窗口内的通行次数
     *
     * @param userId 用户ID
     * @param areaId 区域ID（null表示全局模式）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 通行次数
     */
    @Select("""
            SELECT COUNT(*) FROM t_access_anti_passback_record
            WHERE user_id = #{userId}
            AND deleted_flag = 0
            AND pass_time >= #{startTime}
            AND pass_time < #{endTime}
            AND (#{areaId} IS NULL OR area_id = #{areaId})
            """)
    int countRecentPasses(
            @Param("userId") Long userId,
            @Param("areaId") Long areaId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
