package net.lab1024.sa.consume.domain.vo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 消费卡视图对象
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-24
 */
@Data
@Builder
@Schema(description = "消费卡视图对象")
public class ConsumeCardVO {

    @Schema(description = "卡ID", example = "1001")
    private Long cardId;

    @Schema(description = "用户ID", example = "1001")
    private Long userId;

    @Schema(description = "用户名", example = "张三")
    private String username;

    @Schema(description = "卡号", example = "CARD20231201001")
    private String cardNo;

    @Schema(description = "卡类型", example = "STAFF_CARD")
    private String cardType;

    @Schema(description = "卡类型名称", example = "员工卡")
    private String cardTypeName;

    @Schema(description = "卡状态", example = "ACTIVE")
    private String cardStatus;

    @Schema(description = "卡状态名称", example = "正常")
    private String cardStatusName;

    @Schema(description = "卡余额", example = "258.50")
    private BigDecimal balance;

    @Schema(description = "信用额度", example = "500.00")
    private BigDecimal creditLimit;

    @Schema(description = "可用额度", example = "758.50")
    private BigDecimal availableLimit;

    @Schema(description = "卡面编号", example = "0001234567")
    private String physicalCardNo;

    @Schema(description = "发卡日期", example = "2025-01-01")
    private LocalDate issueDate;

    @Schema(description = "有效期至", example = "2028-01-01")
    private LocalDate expiryDate;

    @Schema(description = "剩余天数", example = "740")
    private Integer remainingDays;

    @Schema(description = "是否即将过期", example = "false")
    private Boolean expiringSoon;

    @Schema(description = "锁定次数", example = "3")
    private Integer lockCount;

    @Schema(description = "解锁次数", example = "3")
    private Integer unlockCount;

    @Schema(description = "最后使用时间", example = "2025-12-24T12:30:00")
    private LocalDateTime lastUsedTime;

    @Schema(description = "最后使用地点", example = "中区餐厅")
    private String lastUsedLocation;

    @Schema(description = "创建时间", example = "2025-01-01T09:00:00")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", example = "2025-12-24T10:30:00")
    private LocalDateTime updateTime;

    @Schema(description = "备注", example = "员工消费卡")
    private String remark;

    @Schema(description = "版本号", example = "1")
    private Integer version;
}
