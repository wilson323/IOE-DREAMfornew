package net.lab1024.sa.common.export;

import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.PostConstruct;

/**
 * 导出模块自动配置
 * <p>
 * 提供EasyExcel表格导出、iText PDF导出、ZXing二维码生成等导出相关功能
 * </p>
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-12-15
 */
@Slf4j
@Configuration
public class ExportModuleAutoConfiguration {

    @PostConstruct
    public void init() {
        log.info("[模块化] microservices-common-export 导出模块已加载");
    }
}
