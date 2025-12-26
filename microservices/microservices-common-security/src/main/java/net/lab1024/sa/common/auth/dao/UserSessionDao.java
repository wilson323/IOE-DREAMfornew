package net.lab1024.sa.common.auth.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.auth.domain.entity.UserSessionEntity;

/**
 * 用户会话数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-16
 */
@Mapper
public interface UserSessionDao extends BaseMapper<UserSessionEntity> {

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<UserSessionEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据令牌查询会话
     *
     * @param token 令牌
     * @return 会话实体
     */
    UserSessionEntity selectByToken(@Param("token") String token);

    /**
     * 删除用户会话
     *
     * @param userId 用户ID
     * @param token 令牌
     */
    void deleteByUserIdAndToken(@Param("userId") Long userId, @Param("token") String token);
}

