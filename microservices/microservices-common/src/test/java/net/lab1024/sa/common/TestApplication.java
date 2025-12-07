package net.lab1024.sa.common;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * 测试应用主类
 *
 * @Author IOE-DREAM Team
 * @Date 2025-12-05
 * @Copyright IOE-DREAM智慧园区一卡通管理平台
 * 
 *            说明：此类的唯一目的是为microservices-common模块的测试提供Spring Boot配置
 *            由于microservices-common是纯JAR库，不包含Spring Boot应用类，
 *            因此需要此测试配置类来支持@SpringBootTest注解的测试
 * 
 *            注意：测试时需要配置数据源，可以通过application-test.yml或环境变量配置
 * 
 *            注意：使用多个@MapperScan分别扫描不同的包，避免同名DAO冲突
 */
@SpringBootApplication(exclude = {
        HibernateJpaAutoConfiguration.class,
        WebMvcAutoConfiguration.class
}, excludeName = {
        "io.github.resilience4j.springboot3.autoconfigure.Resilience4jAutoConfiguration"
}, scanBasePackages = "net.lab1024.sa.common")
@MapperScan("net.lab1024.sa.common.auth.dao")
@MapperScan("net.lab1024.sa.common.security.dao")
@MapperScan("net.lab1024.sa.common.hr.dao")
@MapperScan("net.lab1024.sa.common.system.employee.dao")
@MapperScan("net.lab1024.sa.common.access.dao")
@MapperScan("net.lab1024.sa.common.visitor.dao")
@MapperScan("net.lab1024.sa.common.audit.dao")
@MapperScan("net.lab1024.sa.common.monitor.dao")
@MapperScan("net.lab1024.sa.common.config.dao")
@MapperScan("net.lab1024.sa.common.document.dao")
@MapperScan("net.lab1024.sa.common.file.dao")
@MapperScan("net.lab1024.sa.common.menu.dao")
@MapperScan("net.lab1024.sa.common.dict.dao")
@MapperScan("net.lab1024.sa.common.organization.dao")
@MapperScan("net.lab1024.sa.common.workflow.dao")
@MapperScan("net.lab1024.sa.common.system.dao")
@ComponentScan("net.lab1024.sa.common")
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }
}
