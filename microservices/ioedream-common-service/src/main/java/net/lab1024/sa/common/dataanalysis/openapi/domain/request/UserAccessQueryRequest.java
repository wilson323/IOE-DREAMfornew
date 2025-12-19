package net.lab1024.sa.common.dataanalysis.openapi.domain.request;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户访问查询请求
 * @author IOE-DREAM Team
 * @since 2025-12-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessQueryRequest {
    private String startDate;
    private String endDate;
    private Long areaId;
    private String userType;
    private String dimension;
    private Boolean includeDetail;
}
