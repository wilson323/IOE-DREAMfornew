package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.AccessPersonRestrictionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁人员限制DAO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Mapper
public interface AccessPersonRestrictionDao extends BaseMapper<AccessPersonRestrictionEntity> {
}
