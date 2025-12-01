package net.lab1024.sa.admin.module.consume.domain.result;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;
import net.lab1024.sa.admin.module.consume.domain.vo.AbnormalDetectionResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 批量检测结果
 *
 * @author SmartAdmin Team
 * @since 2025-11-22
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "批量检测结果")
public class BatchDetectionResult {

    @Schema(description = "结果ID")
    private Long resultId;

    @Schema(description = "检测的用户数量")
    private Integer userCount;

    @Schema(description = "检测的异常数量")
    private Integer anomalyCount;

    @Schema(description = "检测时间")
    private LocalDateTime detectionTime;

    @Schema(description = "检测结果列表")
    private List<AbnormalDetectionResult> detectionResults;

    public static BatchDetectionResult create(List<AbnormalDetectionResult> results) {
        return BatchDetectionResult.builder()
                .resultId(System.currentTimeMillis())
                .userCount(results.size())
                .anomalyCount((int) results.stream().filter(r -> r.getRiskLevel().equals("HIGH")).count())
                .detectionTime(LocalDateTime.now())
                .detectionResults(results)
                .build();
    }

    // 手动添加的getter/setter方法 (Lombok失效备用)










}
