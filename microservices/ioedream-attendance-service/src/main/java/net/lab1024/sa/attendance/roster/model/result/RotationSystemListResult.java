package net.lab1024.sa.attendance.roster.model.result;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 轮班制度列表结果
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RotationSystemListResult {
    private Boolean success;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<RotationSystemSummary> data;
    private String errorMessage;
    private String errorCode;
}
