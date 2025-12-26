package net.lab1024.sa.consume.dao.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import net.lab1024.sa.common.entity.device.DeviceQualityRecordEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 设备质量记录数据访问对象
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Mapper
public interface DeviceQualityDao extends BaseMapper<DeviceQualityRecordEntity> {
}
