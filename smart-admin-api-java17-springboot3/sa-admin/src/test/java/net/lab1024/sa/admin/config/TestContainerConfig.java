/*
     * 测试容器配置类 - 用于集成测试
     *
     * @Author:    IOE-DREAM Team
     * @Date:      2025-01-17
     * @Copyright  IOE-DREAM智慧园区一卡通管理平台
     */

    package net.lab1024.sa.admin.config;

    import org.junit.jupiter.api.BeforeAll;
    import org.springframework.boot.test.context.TestConfiguration;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Primary;
    import org.springframework.context.annotation.Profile;
    import org.testcontainers.containers.MySQLContainer;
    import org.testcontainers.containers.RedisContainer;
    import org.testcontainers.containers.GenericContainer;
    import org.testcontainers.utility.DockerImageName;

    import jakarta.sql.DataSource;

    /**
     * 测试容器配置类
     * 提供集成测试所需的数据库和缓存环境
     */
    @TestConfiguration
    @Profile("test")
    public class TestContainerConfig {

        /**
         * MySQL测试容器
         */
        public static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("smart_admin_test")
                .withUsername("test_user")
                .withPassword("test_password")
                .withReuse(true);

        /**
         * Redis测试容器
         */
        public static RedisContainer redisContainer = new RedisContainer(DockerImageName.parse("redis:7-alpine"))
                .withReuse(true);

        @BeforeAll
        static void startContainers() {
            mysqlContainer.start();
            redisContainer.start();
        }

        /**
         * 测试数据源配置
         */
        @Bean
        @Primary
        @Profile("test")
        public DataSource testDataSource() {
            org.springframework.boot.jdbc.DataSourceBuilder<?> builder =
                org.springframework.boot.jdbc.DataSourceBuilder.create();
            builder.driverClassName("com.mysql.cj.jdbc.Driver");
            builder.url(mysqlContainer.getJdbcUrl());
            builder.username(mysqlContainer.getUsername());
            builder.password(mysqlContainer.getPassword());
            return builder.build();
        }

        /**
         * 测试Redis配置
         */
        @Bean
        @Primary
        @Profile("test")
        public String testRedisConfig() {
            return "redis://" + redisContainer.getHost() + ":" + redisContainer.getFirstMappedPort();
        }

        /**
         * 获取测试数据库连接信息
         */
        public static String getTestJdbcUrl() {
            return mysqlContainer.getJdbcUrl();
        }

        public static String getTestUsername() {
            return mysqlContainer.getUsername();
        }

        public static String getTestPassword() {
            return mysqlContainer.getPassword();
        }

        public static String getTestRedisHost() {
            return redisContainer.getHost();
        }

        public static Integer getTestRedisPort() {
            return redisContainer.getFirstMappedPort();
        }
    }