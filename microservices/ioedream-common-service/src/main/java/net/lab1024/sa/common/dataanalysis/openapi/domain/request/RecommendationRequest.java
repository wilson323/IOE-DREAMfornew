package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能推荐请求
 *
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    /** 推荐类型 */
    private String recommendationType;
    /** 目标区域 */
    private String targetArea;
    /** 目标对象 */
    private String targetObject;
    /** 上下文信息 */
    private String context;
    /** 优先级 */
    private String priority;
    /** 用户ID */
    private Long userId;
    /** 限制数量 */
    private Integer limit;
}
