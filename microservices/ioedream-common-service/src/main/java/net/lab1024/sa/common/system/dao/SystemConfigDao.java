package net.lab1024.sa.common.system.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.system.domain.entity.SystemConfigEntity;

@Mapper
public interface SystemConfigDao extends BaseMapper<SystemConfigEntity> {

    SystemConfigEntity selectByKey(@Param("configKey") String configKey);

    List<SystemConfigEntity> selectByGroup(@Param("configGroup") String configGroup);

    List<SystemConfigEntity> selectAllEnabled();
}
