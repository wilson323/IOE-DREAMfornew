package net.lab1024.sa.access.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.access.domain.entity.AccessLinkageLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 联动执行日志数据访问接口
 *
 * @author IOE-DREAM Team
 * @since 2025-12-25
 */
@Mapper
public interface AccessLinkageLogDao extends BaseMapper<AccessLinkageLogEntity> {
}
