package net.lab1024.sa.common.permission.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.permission.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户DAO接口
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
