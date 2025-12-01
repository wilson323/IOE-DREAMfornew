package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

/**
 * 账户解冻请求VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户解冻请求")
public class AccountUnfreezeRequest {

    @Schema(description = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "解冻原因")
    private String unfreezeReason;

    @Schema(description = "操作人员ID")
    private Long operatorId;

    // 手动添加的getter/setter方法 (Lombok失效备用)






}
