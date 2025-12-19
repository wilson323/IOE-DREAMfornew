package net.lab1024.sa.consume.domain.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 消费区域树视图对象
 * <p>
 * 用于区域层级结构展示
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Data
@Schema(description = "消费区域树VO")
public class ConsumeAreaTreeVO extends ConsumeAreaVO {

    /**
     * 子区域列表
     */
    @Schema(description = "子区域列表")
    private List<ConsumeAreaTreeVO> children;

    /**
     * 是否有子区域
     */
    @Schema(description = "是否有子区域")
    private Boolean hasChildren;
}
