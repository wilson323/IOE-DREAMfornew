package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AccessEvacuationPointEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 门禁疏散点DAO
 *
 * @author IOE-DREAM
 * @since 2025-01-25
 */
@Mapper
public interface AccessEvacuationPointDao extends BaseMapper<AccessEvacuationPointEntity> {
}
