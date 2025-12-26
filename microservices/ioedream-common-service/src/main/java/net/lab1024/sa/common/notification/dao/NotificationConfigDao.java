package net.lab1024.sa.common.notification.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.notification.domain.entity.NotificationConfigEntity;

@Mapper
public interface NotificationConfigDao extends BaseMapper<NotificationConfigEntity> {

    NotificationConfigEntity selectByConfigKey(String configKey);

    List<NotificationConfigEntity> selectByConfigType(String configType);

    List<NotificationConfigEntity> selectByConfigTypeAndStatus(String configType, Integer status);

    int updateConfigValue(Long id, String configValue);

    int updateConfigStatus(Long id, Integer status);
}
