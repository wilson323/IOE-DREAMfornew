package net.lab1024.sa.visitor.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 黑名单查询请求
 *
 * <p>用于开放API查询黑名单列表。</p>
 *
 * @author IOE-DREAM团队
 * @since 2025-12-19
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "黑名单查询请求")
public class BlacklistQueryRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNum;

    @Schema(description = "页大小", example = "20")
    private Integer pageSize;

    @Schema(description = "访客姓名")
    private String visitorName;

    @Schema(description = "手机号")
    private String phoneNumber;

    @Schema(description = "黑名单类型")
    private String blacklistType;
}

