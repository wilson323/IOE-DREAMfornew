package net.lab1024.sa.common.audit.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.audit.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志DAO
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-14
 */
@Mapper
public interface AuditLogDao extends BaseMapper<AuditLogEntity> {
}
