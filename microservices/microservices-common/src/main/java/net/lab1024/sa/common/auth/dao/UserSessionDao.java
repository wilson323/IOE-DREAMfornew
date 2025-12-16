package net.lab1024.sa.common.auth.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.common.auth.domain.entity.UserSessionEntity;

/**
 * 用户会话数据访问层
 *
 * 职责：
 * - 用户会话CRUD操作
 * - 会话查询和统计
 * - 过期会话清理
 *
 * 符合CLAUDE.md规范：
 * - 使用@Mapper注解（禁止@Mapper）
 * - 继承BaseMapper<Entity>
 * - 查询方法使用@Transactional(readOnly = true)
 * - 写操作使用@Transactional(rollbackFor = Exception.class)
 * - 只负责数据访问，不包含业务逻辑
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02
 */
@Mapper
public interface UserSessionDao extends BaseMapper<UserSessionEntity> {

    /**
     * 根据令牌查询会话
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_user_session WHERE token = #{token} AND deleted_flag = 0")
    UserSessionEntity selectByToken(@Param("token") String token);

    /**
     * 根据用户ID查询活跃会话列表
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_user_session WHERE user_id = #{userId} AND status = 1 " +
            "AND expiry_time > NOW() AND deleted_flag = 0 ORDER BY login_time DESC")
    List<UserSessionEntity> selectActiveSessionsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询所有会话
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_user_session WHERE user_id = #{userId} AND deleted_flag = 0 " +
            "ORDER BY login_time DESC")
    List<UserSessionEntity> selectAllSessionsByUserId(@Param("userId") Long userId);

    /**
     * 更新会话最后访问时间
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_user_session SET last_access_time = #{lastAccessTime}, update_time = NOW() " +
            "WHERE token = #{token} AND deleted_flag = 0")
    int updateLastAccessTime(@Param("token") String token, @Param("lastAccessTime") LocalDateTime lastAccessTime);

    /**
     * 更新会话状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("UPDATE t_user_session SET status = #{status}, update_time = NOW() " +
            "WHERE session_id = #{sessionId} AND deleted_flag = 0")
    int updateSessionStatus(@Param("sessionId") Long sessionId, @Param("status") Integer status);

    /**
     * 根据令牌删除会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Delete("UPDATE t_user_session SET deleted_flag = 1, update_time = NOW() " +
            "WHERE token = #{token} AND deleted_flag = 0")
    int deleteByToken(@Param("token") String token);

    /**
     * 根据用户ID删除所有会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Delete("UPDATE t_user_session SET deleted_flag = 1, update_time = NOW() " +
            "WHERE user_id = #{userId} AND deleted_flag = 0")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除过期会话
     */
    @Transactional(rollbackFor = Exception.class)
    @Delete("UPDATE t_user_session SET deleted_flag = 1, update_time = NOW() " +
            "WHERE expiry_time < #{currentTime} AND deleted_flag = 0")
    int deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 统计活跃会话数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_user_session WHERE status = 1 " +
            "AND expiry_time > NOW() AND deleted_flag = 0")
    long countActiveSessions();

    /**
     * 统计用户活跃会话数量
     */
    @Transactional(readOnly = true)
    @Select("SELECT COUNT(*) FROM t_user_session WHERE user_id = #{userId} AND status = 1 " +
            "AND expiry_time > NOW() AND deleted_flag = 0")
    int countUserActiveSessions(@Param("userId") Long userId);

    /**
     * 查询最近登录的会话
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_user_session WHERE deleted_flag = 0 " +
            "ORDER BY login_time DESC LIMIT #{limit}")
    List<UserSessionEntity> selectRecentSessions(@Param("limit") Integer limit);

    /**
     * 查询即将过期的会话
     */
    @Transactional(readOnly = true)
    @Select("SELECT * FROM t_user_session WHERE status = 1 " +
            "AND expiry_time BETWEEN NOW() AND DATE_ADD(NOW(), INTERVAL #{minutes} MINUTE) " +
            "AND deleted_flag = 0")
    List<UserSessionEntity> selectExpiringSessions(@Param("minutes") Integer minutes);

    /**
     * 批量更新会话状态
     */
    @Transactional(rollbackFor = Exception.class)
    @Update("<script>" +
            "UPDATE t_user_session SET status = #{status}, update_time = NOW() " +
            "WHERE session_id IN " +
            "<foreach collection='sessionIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " AND deleted_flag = 0" +
            "</script>")
    int batchUpdateStatus(@Param("sessionIds") List<Long> sessionIds, @Param("status") Integer status);
}
