package net.lab1024.sa.common.openapi.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

/**
 * 用户扩展信息请求
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-17
 */
@Data
@Schema(description = "用户扩展信息请求")
public class UserExtendedInfoRequest {

    @Schema(description = "性别（0-未知 1-男 2-女）", example = "1")
    private Integer gender;

    @Schema(description = "生日", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "部门ID", example = "1")
    private Long departmentId;

    @Schema(description = "职位", example = "工程师")
    private String position;

    @Schema(description = "工号", example = "EMP001")
    private String employeeNo;

    @Schema(description = "地址", example = "北京市朝阳区")
    private String address;

    @Schema(description = "备注", example = "备注信息")
    private String remark;
}

