package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

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
import net.lab1024.sa.consume.domain.form.ConsumeOfflineSyncForm;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * RefundApplicationControllerTest（离线消费同步）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端离线消费同步接口测试")
class RefundApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("离线交易同步-成功")
    void testSyncOfflineTransactions() throws Exception {
        ConsumeOfflineSyncForm form = new ConsumeOfflineSyncForm();
        form.setDeviceId(2001L);
        ConsumeOfflineSyncForm.OfflineTransaction record = new ConsumeOfflineSyncForm.OfflineTransaction();
        record.setTransactionNo("OFFLINE_TX_001");
        record.setUserId(1001L);
        record.setAmount(new BigDecimal("10.00"));
        record.setConsumeTime(LocalDateTime.now());
        record.setConsumeMode("FIXED");
        form.setTransactions(Collections.singletonList(record));

        ConsumeSyncResultVO result = new ConsumeSyncResultVO();
        result.setSuccess(true);

        when(consumeMobileService.syncOfflineTransactions(any(ConsumeOfflineSyncForm.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/consume/mobile/sync/offline")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
