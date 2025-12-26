package net.lab1024.sa.common.system.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 系统配置更新DTO
 *
 * @author IOE-DREAM Team
 * @since 2025-12-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "系统配置更新DTO")
public class ConfigUpdateDTO extends ConfigCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "配置ID", required = true, example = "1001")
    @NotNull(message = "配置ID不能为空")
    private Long configId;
}

