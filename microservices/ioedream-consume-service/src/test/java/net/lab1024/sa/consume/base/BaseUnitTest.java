package net.lab1024.sa.consume.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 单元测试基类
 *
 * 职责：提供统一的测试基础功能和工具方法
 *
 * 功能：
 * 1. 自动配置MockMvc
 * 2. 提供通用的HTTP请求方法
 * 3. 提供JSON序列化/反序列化
 * 4. 测试数据自动回滚
 * 5. 禁用Seata分布式事务（测试环境不需要）
 * 6. 禁用Redis（测试环境使用内存缓存）
 *
 * 使用示例：
 * <pre>
 * &#64;SpringBootTest
 * class ConsumeServiceTest extends BaseUnitTest {
 *
 *     &#64;Test
 *     void testConsumeTransaction() throws Exception {
 *         // 测试逻辑
 *     }
 * }
 * </pre>
 *
 * @author IOE-DREAM架构团队
 * @since 2025-12-23
 */
@Slf4j
@SpringBootTest(properties = {
        "spring.cloud.seata.enabled=false",
        "seata.enabled=false",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.discovery.enabled=false",
        "spring.redis.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,org.redisson.spring.starter.RedissonAutoConfigurationV2,com.alibaba.cloud.nacos.NacosConfigAutoConfiguration,com.alibaba.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // 测试后自动回滚
public abstract class BaseUnitTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * 每个测试方法执行前的初始化
     */
    @BeforeEach
    void setUp() {
        log.debug("[测试初始化] 准备执行测试用例");
    }

    /**
     * 执行GET请求
     *
     * @param url URL路径
     * @return ResultActions
     */
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行GET请求（带路径参数）
     *
     * @param url URL路径格式，如 "/api/users/{id}"
     * @param pathVars 路径参数
     * @return ResultActions
     */
    protected ResultActions performGet(String url, Object... pathVars) throws Exception {
        return mockMvc.perform(get(url, pathVars)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行POST请求
     *
     * @param url URL路径
     * @param requestBody 请求体对象
     * @return ResultActions
     */
    protected ResultActions performPost(String url, Object requestBody) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    /**
     * 执行PUT请求
     *
     * @param url URL路径
     * @param requestBody 请求体对象
     * @return ResultActions
     */
    protected ResultActions performPut(String url, Object requestBody) throws Exception {
        return mockMvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    /**
     * 执行PUT请求（带路径参数）
     *
     * @param url URL路径格式
     * @param requestBody 请求体对象
     * @param pathVars 路径参数
     * @return ResultActions
     */
    protected ResultActions performPut(String url, Object requestBody, Object... pathVars) throws Exception {
        return mockMvc.perform(put(url, pathVars)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)));
    }

    /**
     * 执行DELETE请求
     *
     * @param url URL路径
     * @param pathVars 路径参数
     * @return ResultActions
     */
    protected ResultActions performDelete(String url, Object... pathVars) throws Exception {
        return mockMvc.perform(delete(url, pathVars)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 验证成功响应
     *
     * @param actions ResultActions
     * @return ResultActions
     */
    protected ResultActions expectSuccess(ResultActions actions) throws Exception {
        return actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    /**
     * 验证分页响应
     *
     * @param actions ResultActions
     * @return ResultActions
     */
    protected ResultActions expectPage(ResultActions actions) throws Exception {
        return actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.list").isArray())
                .andExpect(jsonPath("$.data.total").exists());
    }

    /**
     * 验证错误响应
     *
     * @param actions ResultActions
     * @param expectedCode 期望的错误码
     * @return ResultActions
     */
    protected ResultActions expectError(ResultActions actions, String expectedCode) throws Exception {
        return actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(expectedCode));
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 将JSON字符串转换为对象
     *
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 对象实例
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
