package net.lab1024.sa.common.organization.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.MultiPersonRecordEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 多人验证记录数据访问对象
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识数据访问层
 * - 继承BaseMapper<Entity>使用MyBatis-Plus
 * - 查询方法使用@Transactional(readOnly = true)
 * - 写操作方法使用@Transactional(rollbackFor = Exception.class)
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Mapper
public interface MultiPersonRecordDao extends BaseMapper<MultiPersonRecordEntity> {

    /**
     * 根据会话ID查询验证记录
     *
     * @param sessionId 会话ID
     * @return 验证记录，不存在返回null
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_multi_person_record " +
            "WHERE verification_session_id = #{sessionId} AND deleted_flag = 0 " +
            "LIMIT 1")
    MultiPersonRecordEntity selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 查询区域设备的活跃验证会话
     * <p>
     * 用于多人验证，查找区域设备中等待中的验证会话
     * </p>
     *
     * @param areaId 区域ID
     * @param deviceId 设备ID
     * @return 活跃验证会话列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_multi_person_record " +
            "WHERE area_id = #{areaId} AND device_id = #{deviceId} " +
            "AND status = 0 AND deleted_flag = 0 " +
            "AND expire_time > NOW() " +
            "ORDER BY start_time DESC")
    List<MultiPersonRecordEntity> selectActiveSessions(
            @Param("areaId") Long areaId,
            @Param("deviceId") Long deviceId);

    /**
     * 查询过期的验证会话
     * <p>
     * 用于定时任务清理过期会话
     * </p>
     *
     * @param expireTime 过期时间
     * @return 过期会话列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_access_multi_person_record " +
            "WHERE status = 0 AND deleted_flag = 0 " +
            "AND expire_time <= #{expireTime}")
    List<MultiPersonRecordEntity> selectExpiredSessions(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 更新会话状态为超时
     *
     * @param sessionId 会话ID
     * @return 更新数量
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_access_multi_person_record " +
            "SET status = 2 " +
            "WHERE verification_session_id = #{sessionId} AND deleted_flag = 0")
    int updateStatusToTimeout(@Param("sessionId") String sessionId);
}
