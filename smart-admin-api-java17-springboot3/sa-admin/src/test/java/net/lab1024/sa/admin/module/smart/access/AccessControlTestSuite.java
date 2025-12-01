/*
     * 门禁系统测试套件
     *
     * @Author:    IOE-DREAM Team
     * @Date:      2025-01-17
     * @Copyright  IOE-DREAM智慧园区一卡通管理平台
     */

    package net.lab1024.sa.admin.module.smart.access;

    import org.junit.platform.suite.api.SelectClasses;
    import org.junit.platform.suite.api.Suite;
    import org.junit.platform.suite.api.SuiteDisplayName;

    import net.lab1024.sa.admin.module.smart.access.controller.AccessAreaControllerTest;
    import net.lab1024.sa.admin.module.smart.access.controller.AccessAreaControllerIntegrationTest;
    import net.lab1024.sa.admin.module.smart.access.service.AccessAreaServiceIntegrationTest;
    import net.lab1024.sa.admin.module.smart.access.integration.AccessControlSystemIntegrationTest;

    /**
     * 门禁系统测试套件
     * 包含单元测试、集成测试和端到端测试
     */
    @Suite
    @SuiteDisplayName("门禁系统完整测试套件")
    @SelectClasses({
        // 单元测试
        AccessAreaControllerTest.class,

        // 集成测试
        AccessAreaControllerIntegrationTest.class,
        AccessAreaServiceIntegrationTest.class,

        // 端到端测试
        AccessControlSystemIntegrationTest.class
    })
    public class AccessControlTestSuite {
        /**
         * 测试套件说明：
         *
         * 1. 单元测试 (AccessAreaControllerTest):
         *    - 测试Controller层的各个方法
         *    - 使用Mock对象隔离依赖
         *    - 验证请求处理和响应格式
         *
         * 2. Controller集成测试 (AccessAreaControllerIntegrationTest):
         *    - 使用MockMvc进行完整的HTTP请求测试
         *    - 测试API端点的完整流程
         *    - 验证请求响应格式和数据一致性
         *
         * 3. Service集成测试 (AccessAreaServiceIntegrationTest):
         *    - 使用真实数据库进行业务逻辑测试
         *    - 测试事务管理和数据持久化
         *    - 验证业务规则和数据完整性
         *
         * 4. 端到端测试 (AccessControlSystemIntegrationTest):
         *    - 测试完整的业务流程
         *    - 验证系统各组件间的集成
         *    - 测试并发操作和性能指标
         *
         * 运行方式：
         * - IDE中直接运行此类执行所有测试
         * - Maven命令: mvn test -Dtest=AccessControlTestSuite
         * - Gradle命令: gradle test --tests AccessControlTestSuite
         */
    }