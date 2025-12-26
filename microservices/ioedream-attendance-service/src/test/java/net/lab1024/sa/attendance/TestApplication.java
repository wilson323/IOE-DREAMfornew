package net.lab1024.sa.attendance;

import org.springframework.context.annotation.Configuration;

/**
 * 测试应用配置类
 * <p>
 * 不包含@MapperScan注解，避免在@WebMvcTest中扫描DAO导致Bean创建失败
 * 不使用@SpringBootApplication，避免与AttendanceServiceApplication冲突
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@Configuration
public class TestApplication {
    // 测试环境专用的配置类，不扫描Mapper接口
}
