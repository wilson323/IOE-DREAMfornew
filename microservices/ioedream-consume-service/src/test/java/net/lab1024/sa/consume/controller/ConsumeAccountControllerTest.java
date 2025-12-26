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
import net.lab1024.sa.consume.domain.vo.ConsumeMobileUserVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * ConsumeAccountControllerTest（移动端快速用户查询）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端快速用户查询接口测试")
class ConsumeAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("快速用户查询-成功")
    void testQuickUserInfo() throws Exception {
        ConsumeMobileUserVO userVO = new ConsumeMobileUserVO();
        userVO.setUserId(1001L);
        userVO.setUserName("张三");
        userVO.setAccountBalance(new BigDecimal("200.00"));

        when(consumeMobileService.quickUserInfo(eq("userId"), eq("1001")))
                .thenReturn(userVO);

        mockMvc.perform(get("/api/v1/consume/mobile/user/quick")
                .param("queryType", "userId")
                .param("queryValue", "1001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
