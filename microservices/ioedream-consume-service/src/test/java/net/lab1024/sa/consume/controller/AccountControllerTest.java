package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.config.TestSecurityConfiguration;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserInfoVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * AccountControllerTest（移动端消费用户信息）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端消费用户信息接口测试")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("获取用户消费信息-成功")
    void testGetUserConsumeInfo() throws Exception {
        ConsumeMobileUserInfoVO userInfo = new ConsumeMobileUserInfoVO();
        userInfo.setUserId(1001L);
        userInfo.setAccountBalance(new BigDecimal("100.00"));

        when(consumeMobileService.getUserConsumeInfo(eq(1001L)))
                .thenReturn(userInfo);

        mockMvc.perform(get("/api/v1/consume/mobile/user/consume-info/1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
