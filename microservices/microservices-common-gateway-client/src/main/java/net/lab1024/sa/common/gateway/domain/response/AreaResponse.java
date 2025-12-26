package net.lab1024.sa.common.gateway.domain.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 区域信息响应对象
 * <p>
 * 用于跨服务传递区域信息，避免直接使用Entity
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "区域信息响应")
public class AreaResponse {

    @Schema(description = "区域ID", example = "1001")
    private Long areaId;

    @Schema(description = "区域编码", example = "A001")
    private String areaCode;

    @Schema(description = "区域名称", example = "A栋1楼大厅")
    private String areaName;

    @Schema(description = "区域类型", example = "ENTRANCE")
    private String areaType;

    @Schema(description = "上级区域ID", example = "100")
    private Long parentAreaId;

    @Schema(description = "区域层级", example = "3")
    private Integer level;

    @Schema(description = "区域描述", example = "主入口门禁区域")
    private String description;

    @Schema(description = "区域地址", example = "北京市朝阳区A栋1楼")
    private String address;

    @Schema(description = "区域面积", example = "100.5")
    private Double area;

    @Schema(description = "经度", example = "116.123456")
    private Double longitude;

    @Schema(description = "纬度", example = "39.123456")
    private Double latitude;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", example = "2025-01-01T10:30:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-21T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "子区域列表")
    private List<AreaResponse> children;

    @Schema(description = "区域设备列表")
    private List<DeviceResponse> devices;

    @Schema(description = "区域权限配置")
    private Object accessConfig;

    @Schema(description = "扩展属性")
    private Object extendedAttributes;
}