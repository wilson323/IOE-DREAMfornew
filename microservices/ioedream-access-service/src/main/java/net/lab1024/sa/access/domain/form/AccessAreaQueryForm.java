package net.lab1024.sa.access.domain.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.lang.Nullable;

/**
 * 门禁区域查询表单
 * <p>
 * 严格遵循CLAUDE.md规范：
 * - 使用Form后缀命名
 * - 使用Jakarta验证注解
 * - 支持分页查询参数
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
public class AccessAreaQueryForm {

    /**
     * 页码（从1开始）
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @NotNull(message = "每页大小不能为空")
    @Min(value = 1, message = "每页大小必须大于0")
    private Integer pageSize = 20;

    /**
     * 区域ID（可选）
     */
    @Nullable
    private Long areaId;

    /**
     * 区域名称关键字（可选）
     */
    @Nullable
    private String areaNameKeyword;

    /**
     * 父区域ID（可选）
     */
    @Nullable
    private Long parentAreaId;

    /**
     * 区域类型（可选）
     * <p>
     * 1-园区 2-建筑 3-楼层 4-房间 5-区域 6-点位
     * </p>
     */
    @Nullable
    private Integer areaType;

    /**
     * 区域状态（可选）
     * <p>
     * 1-正常 2-停用 3-装修 4-关闭
     * </p>
     */
    @Nullable
    private Integer areaStatus;
}
