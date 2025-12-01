package net.lab1024.sa.visitor.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PageResultVO
 */
@Data
@Schema(description = "PageResultVO")
public class PageResultVO {
    @Schema(description = "字段")
    private String field;
}
