package net.lab1024.sa.admin.module.system.area.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 区域视图对象
 *
 * @author SmartAdmin
 * @date 2025-01-10
 */
@Data
public class AreaVO {

    @Schema(description = "区域ID")
    private Long areaId;

    @Schema(description = "区域编码")
    private String areaCode;

    @Schema(description = "区域名称")
    private String areaName;

    @Schema(description = "区域类型")
    private String areaType;

    @Schema(description = "区域层级")
    private Integer areaLevel;

    @Schema(description = "父区域ID")
    private Long parentId;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "区域配置JSON")
    private Map<String, Object> areaConfig;

    @Schema(description = "区域描述")
    private String areaDesc;

    @Schema(description = "区域负责人ID")
    private Long managerId;

    @Schema(description = "区域负责人姓名")
    private String managerName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "状态：1-启用，0-禁用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "创建人姓名")
    private String createUserName;

    @Schema(description = "更新人ID")
    private Long updateUserId;

    @Schema(description = "更新人姓名")
    private String updateUserName;

}
