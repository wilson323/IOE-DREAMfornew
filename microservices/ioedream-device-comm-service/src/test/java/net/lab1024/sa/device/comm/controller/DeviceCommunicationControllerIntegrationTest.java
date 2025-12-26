package net.lab1024.sa.device.comm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.device.comm.protocol.ProtocolAdapter;
import net.lab1024.sa.device.comm.protocol.domain.ProtocolProcessResult;
import net.lab1024.sa.device.comm.protocol.factory.ProtocolAdapterFactory;

/**
 * DeviceCommunicationController测试（消费业务处理）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(DeviceCommunicationController.class)
@DisplayName("设备通讯消费业务处理接口测试")
class DeviceCommunicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProtocolAdapterFactory protocolAdapterFactory;

    @MockBean
    private ProtocolAdapter protocolAdapter;

    @Test
    @DisplayName("处理消费业务-成功")
    void testProcessConsumeBusiness() throws Exception {
        ProtocolProcessResult result = ProtocolProcessResult.success("CONSUME", Collections.emptyMap());
        when(protocolAdapterFactory.getAdapter(eq("TEST"))).thenReturn(protocolAdapter);
        when(protocolAdapter.processConsumeBusiness(eq("CONSUME"), any(Map.class), eq(1L)))
                .thenReturn(CompletableFuture.completedFuture(result));

        mockMvc.perform(post("/api/v1/device-comm/process-consume")
                .param("protocolType", "TEST")
                .param("businessType", "CONSUME")
                .param("deviceId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Collections.emptyMap())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
