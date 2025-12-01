package net.lab1024.sa.admin.module.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.admin.module.access.domain.entity.AccessRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * Access record DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-11-16
 */
@Mapper
public interface AccessRecordDao extends BaseMapper<AccessRecordEntity> {
}