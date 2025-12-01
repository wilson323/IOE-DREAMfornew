package net.lab1024.sa.base.module.support.operationlog.dao;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import net.lab1024.sa.base.module.support.operationlog.domain.OperationLogEntity;

/**
 * 鎿嶄綔鏃ュ織Dao
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Mapper
public interface OperationLogDao extends BaseMapper<OperationLogEntity> {
}
