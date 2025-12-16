package net.lab1024.sa.common.system.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统配置DAO
 * 整合自ioedream-system-service
 *
 * 符合CLAUDE.md规范：
 * - 使用@Mapper注解（强制）
 * - 继承BaseMapper
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-02（整合自system-service）
 */
@Mapper
public interface SystemConfigDao extends BaseMapper<SystemConfigEntity> {

    @Select("SELECT * FROM t_sys_config WHERE config_key = #{configKey} AND status = 1 AND deleted_flag = 0")
    SystemConfigEntity selectByKey(@Param("configKey") String configKey);

    @Select("SELECT * FROM t_sys_config WHERE config_group = #{configGroup} AND status = 1 AND deleted_flag = 0")
    List<SystemConfigEntity> selectByGroup(@Param("configGroup") String configGroup);

    @Select("SELECT * FROM t_sys_config WHERE status = 1 AND deleted_flag = 0")
    List<SystemConfigEntity> selectAllEnabled();
}

