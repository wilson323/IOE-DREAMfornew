package net.lab1024.sa.video.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.video.AlarmRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警记录 DAO
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Mapper
public interface AlarmRecordDao extends BaseMapper<AlarmRecordEntity> {
}
