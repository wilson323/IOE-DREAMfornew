package net.lab1024.sa.admin.module.consume.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 账户冻结信息VO
 * 严格遵循repowiki规范：VO类用于数据传输
 *
 * @author SmartAdmin Team
 * @date 2025/11/21
 */




@Schema(description = "账户冻结信息")
public class AccountFreezeInfo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "是否冻结")
    private Boolean frozen;

    @Schema(description = "冻结时间")
    private LocalDateTime freezeTime;

    @Schema(description = "解冻时间")
    private LocalDateTime unfreezeTime;

    @Schema(description = "冻结原因")
    private String freezeReason;

    @Schema(description = "操作人员")
    private Long operatorId;

    // 手动添加的getter/setter方法 (Lombok失效备用)


    public Boolean isFrozen() {
        return frozen;
    }










}
