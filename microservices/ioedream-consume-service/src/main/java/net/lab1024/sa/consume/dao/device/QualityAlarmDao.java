package net.lab1024.sa.consume.dao.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.device.QualityAlarmEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质量告警数据访问对象
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface QualityAlarmDao extends BaseMapper<QualityAlarmEntity> {
}
