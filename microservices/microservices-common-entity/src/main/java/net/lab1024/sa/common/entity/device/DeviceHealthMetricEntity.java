package net.lab1024.sa.common.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设备健康指标实体
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_health_metric")
@Schema(description = "设备健康指标实体")
public class DeviceHealthMetricEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "指标ID")
    private Long metricId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "指标类型(cpu/memory/temperature/delay/packet_loss)")
    private String metricType;

    @Schema(description = "指标值")
    private BigDecimal metricValue;

    @Schema(description = "指标单位(%,℃,ms,%)")
    private String metricUnit;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;
}
