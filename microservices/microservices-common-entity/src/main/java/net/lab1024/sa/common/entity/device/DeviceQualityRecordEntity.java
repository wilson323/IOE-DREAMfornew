package net.lab1024.sa.common.entity.device;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.lab1024.sa.common.entity.BaseEntity;

import java.time.LocalDateTime;

/**
 * 设备质量诊断记录实体
 *
 * @author IOE-DREAM
 * @since 2025-12-26
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_device_quality_record")
@Schema(description = "设备质量诊断记录实体")
public class DeviceQualityRecordEntity extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "记录ID")
    private Long recordId;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备类型(1-门禁 2-考勤 3-消费 4-视频 5-访客)")
    private Integer deviceType;

    @Schema(description = "健康评分(0-100)")
    private Integer healthScore;

    @Schema(description = "质量等级(优秀/良好/合格/较差/危险)")
    private String qualityLevel;

    @Schema(description = "诊断结果(JSON格式)")
    private String diagnosisResult;

    @Schema(description = "告警级别(0-无 1-低 2-中 3-高 4-紧急)")
    private Integer alarmLevel;

    @Schema(description = "诊断时间")
    private LocalDateTime diagnosisTime;
}
