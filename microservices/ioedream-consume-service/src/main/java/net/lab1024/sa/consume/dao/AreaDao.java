package net.lab1024.sa.consume.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.organization.entity.AreaEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 区域数据访问对象
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface AreaDao extends BaseMapper<AreaEntity> {
}
