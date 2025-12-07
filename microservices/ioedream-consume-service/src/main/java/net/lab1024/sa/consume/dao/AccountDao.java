package net.lab1024.sa.consume.dao;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.consume.domain.entity.AccountEntity;

/**
 * 账户DAO接口
 * <p>
 * 用于账户的数据访问操作
 * 严格遵循CLAUDE.md规范：
 * - 使用@Mapper注解标识
 * - 继承BaseMapper<Entity>
 * - 使用Dao后缀命名
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@org.apache.ibatis.annotations.Mapper
public interface AccountDao extends BaseMapper<AccountEntity> {

    /**
     * 根据用户ID查询账户信息
     *
     * @param userId 用户ID
     * @return 账户信息
     */
    AccountEntity selectByUserId(@Param("userId") Long userId);
}
