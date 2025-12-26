package net.lab1024.sa.common.system.employee.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 员工更新DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "员工更新DTO")
public class EmployeeUpdateDTO extends EmployeeAddDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "员工ID", required = true, example = "1001")
    @NotNull(message = "员工ID不能为空")
    private Long employeeId;
}

