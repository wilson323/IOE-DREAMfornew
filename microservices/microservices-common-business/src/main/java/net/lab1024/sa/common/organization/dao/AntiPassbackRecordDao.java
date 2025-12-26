package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AntiPassbackRecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 反潜回记录DAO接口
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface AntiPassbackRecordDao extends BaseMapper<AntiPassbackRecordEntity> {

    /**
     * 查询用户最近的进出记录
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     * @param limit    限制条数
     * @return 记录列表
     */
    @Select("SELECT * FROM t_access_anti_passback_record " +
            "WHERE user_id = #{userId} AND device_id = #{deviceId} " +
            "AND deleted_flag = 0 " +
            "ORDER BY record_time DESC LIMIT #{limit}")
    List<AntiPassbackRecordEntity> selectRecentRecords(
            @Param("userId") Long userId,
            @Param("deviceId") Long deviceId,
            @Param("limit") Integer limit
    );
}

