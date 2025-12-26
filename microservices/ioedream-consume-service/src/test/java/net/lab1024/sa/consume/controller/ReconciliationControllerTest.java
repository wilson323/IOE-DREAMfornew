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
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.consume.config.TestSecurityConfiguration;

import net.lab1024.sa.consume.service.ConsumeTransactionService;

/**
 * ReconciliationControllerTest（交易记录对账）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeTransactionController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("交易记录对账接口测试")
class ReconciliationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumeTransactionService consumeTransactionService;

    @Test
    @DisplayName("交易记录对账-成功")
    void testReconcileTransactions () throws Exception {
        String startDate = "2025-12-21T00:00:00";
        String endDate = "2025-12-21T23:59:59";

        java.util.Map<String, Object> result = new java.util.HashMap<> ();
        result.put ("totalCount", 100);
        result.put ("totalAmount", new BigDecimal ("1000.00"));

        when (consumeTransactionService.reconcileTransactions (any (String.class), any (String.class)))
                .thenReturn (result);

        mockMvc.perform (post ("/api/v1/consume/transaction/reconciliation").param ("startDate", startDate)
                .param ("endDate", endDate)).andExpect (status ().isOk ()).andExpect (jsonPath ("$.code").value (200));
    }
}
