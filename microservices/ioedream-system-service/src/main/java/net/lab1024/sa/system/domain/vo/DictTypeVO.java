package net.lab1024.sa.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据字典类型视图对象
 * <p>
 * 用于前端展示的字典类型信息 *
 * @author SmartAdmin Team
 * @since 2025-11-29
 */
@Data
@Schema(description = "数据字典类型视图对象")
public class DictTypeVO {

    /**
     * 字典类型ID
     */
    @Schema(description = "字典类型ID", example = "1")
    private Long dictTypeId;

    /**
     * 字典类型名称
     */
    @Schema(description = "字典类型名称", example = "用户状态")
    private String dictTypeName;

    /**
     * 字典类型编码
     */
    @Schema(description = "字典类型编码", example = "USER_STATUS")
    private String dictTypeCode;

    /**
     * 字典类型描述
     */
    @Schema(description = "字典类型描述", example = "用户状态字典")
    private String description;

    /**
     * 状态
     */
    @Schema(description = "状态", example = "1")
    private Integer status;

    /**
     * 状态名称
     */
    @Schema(description = "状态名称", example = "启用")
    private String statusName;

    /**
     * 排序
     */
    @Schema(description = "排序", example = "1")
    private Integer sortNumber;

    /**
     * 是否内置
     */
    @Schema(description = "是否内置", example = "1")
    private Integer isSystem;

    /**
     * 是否内置名称
     */
    @Schema(description = "是否内置名称", example = "自定义")
    private String isSystemName;

    /**
     * 字典项数量
     */
    @Schema(description = "字典项数量", example = "3")
    private Integer dataCount;

    /**
     * 字典项列表
     */
    @Schema(description = "字典项列表")
    private List<DictDataVO> dictDataList;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "创建时间", example = "2025-01-01 12:00:00")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "更新时间", example = "2025-01-01 12:00:00")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Schema(description = "创建人", example = "管理员")
    private String createUserName;

    /**
     * 更新人
     */
    @Schema(description = "更新人", example = "管理员")
    private String updateUserName;

    /**
     * 扩展字段解析结果
     */
    @Schema(description = "扩展字段", example = "{}")
    private Object extendInfo;
}