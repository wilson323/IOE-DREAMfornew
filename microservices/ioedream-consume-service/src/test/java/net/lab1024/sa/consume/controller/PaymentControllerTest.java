package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.consume.config.TestSecurityConfiguration;

import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.domain.form.ConsumeMobileNfcForm;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * PaymentControllerTest（移动端NFC消费）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端NFC消费接口测试")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("NFC消费-成功")
    void testNfcConsume() throws Exception {
        ConsumeMobileNfcForm form = new ConsumeMobileNfcForm();
        form.setDeviceId(2001L);
        form.setCardNumber("NFC_123456");
        form.setAmount(new BigDecimal("20.00"));

        ConsumeMobileResultVO result = new ConsumeMobileResultVO();
        result.setSuccess(true);

        when(consumeMobileService.nfcConsume(any(ConsumeMobileNfcForm.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/consume/mobile/transaction/nfc")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
