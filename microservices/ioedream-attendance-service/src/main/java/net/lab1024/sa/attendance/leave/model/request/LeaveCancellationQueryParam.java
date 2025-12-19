package net.lab1024.sa.attendance.leave.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.lab1024.sa.attendance.leave.model.LeaveCancellationApplication;

/**
 * 销假查询参数
 *
 * <p>用于查询销假申请列表的过滤与分页条件。</p>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCancellationQueryParam {

    private LeaveCancellationApplication.ApplicationStatus status;
    private LeaveCancellationApplication.CancellationType cancellationType;
    private LeaveCancellationApplication.UrgencyLevel urgencyLevel;

    /**
     * 页码（当前实现按 0-based 使用：start=pageNum*pageSize）
     */
    @Builder.Default
    private Integer pageNum = 0;

    @Builder.Default
    private Integer pageSize = 20;
}
