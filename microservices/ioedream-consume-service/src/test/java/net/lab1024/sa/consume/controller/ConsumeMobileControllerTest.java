package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import net.lab1024.sa.consume.domain.form.ConsumePermissionValidateForm;
import net.lab1024.sa.consume.domain.vo.ConsumeSyncDataVO;
import net.lab1024.sa.consume.domain.vo.ConsumeValidateResultVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * ConsumeMobileControllerTest（同步与权限校验）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端同步与权限校验接口测试")
class ConsumeMobileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("获取同步数据-成功")
    void testGetSyncData() throws Exception {
        ConsumeSyncDataVO syncData = new ConsumeSyncDataVO();
        syncData.setDeviceId(2001L);

        when(consumeMobileService.getSyncData(eq(2001L), any()))
                .thenReturn(syncData);

        mockMvc.perform(get("/api/v1/consume/mobile/sync/data/2001")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("权限验证-成功")
    void testValidatePermission() throws Exception {
        ConsumePermissionValidateForm form = new ConsumePermissionValidateForm();
        form.setUserId(1001L);
        form.setAreaId("3001");
        form.setAmount(new BigDecimal("10.00"));

        ConsumeValidateResultVO result = new ConsumeValidateResultVO();
        result.setAllowed(true);

        when(consumeMobileService.validateConsumePermission(any(ConsumePermissionValidateForm.class)))
                .thenReturn(result);

        mockMvc.perform(post("/api/v1/consume/mobile/validate/permission")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(form)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
