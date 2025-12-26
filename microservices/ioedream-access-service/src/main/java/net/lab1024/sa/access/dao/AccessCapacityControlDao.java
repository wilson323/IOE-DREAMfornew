package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.access.AccessCapacityControlEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁容量控制DAO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Mapper
public interface AccessCapacityControlDao extends BaseMapper<AccessCapacityControlEntity> {
}
