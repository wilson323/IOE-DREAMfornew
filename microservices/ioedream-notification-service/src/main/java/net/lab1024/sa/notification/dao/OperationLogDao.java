package net.lab1024.sa.notification.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.notification.domain.entity.OperationLogEntity;

/**
 * 操作日志Dao
 *
 * @author IOE-DREAM Team
 * @since 2025-11-29
 */
@Mapper
public interface OperationLogDao extends BaseMapper<OperationLogEntity> {
}