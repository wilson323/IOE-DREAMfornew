package net.lab1024.sa.consume.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import net.lab1024.sa.consume.config.TestSecurityConfiguration;
import net.lab1024.sa.consume.domain.vo.ConsumeDeviceConfigVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileStatsVO;
import net.lab1024.sa.consume.domain.vo.ConsumeMobileSummaryVO;
import net.lab1024.sa.consume.service.ConsumeMobileService;

/**
 * MobileConsumeControllerTest（设备与统计接口）
 *
 * @author IOE-DREAM Team
 * @since 2025-12-22
 */
@WebMvcTest(ConsumeMobileController.class)
@Import(TestSecurityConfiguration.class)
@DisplayName("移动端设备与统计接口测试")
class MobileConsumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConsumeMobileService consumeMobileService;

    @Test
    @DisplayName("获取有效餐别-成功")
    void testGetAvailableMeals () throws Exception {
        when (consumeMobileService.getAvailableMeals ()).thenReturn (Collections.emptyList ());

        mockMvc.perform (get ("/api/v1/consume/mobile/meal/available").contentType (MediaType.APPLICATION_JSON))
                .andExpect (status ().isOk ()).andExpect (jsonPath ("$.code").value (200));
    }

    @Test
    @DisplayName("获取设备配置-成功")
    void testGetDeviceConfig () throws Exception {
        ConsumeDeviceConfigVO config = new ConsumeDeviceConfigVO ();
        config.setDeviceId (2001L);

        when (consumeMobileService.getDeviceConfig (eq (2001L))).thenReturn (config);

        mockMvc.perform (get ("/api/v1/consume/mobile/device/config/2001").contentType (MediaType.APPLICATION_JSON))
                .andExpect (status ().isOk ()).andExpect (jsonPath ("$.code").value (200));
    }

    @Test
    @DisplayName("获取设备今日统计-成功")
    void testGetDeviceTodayStats () throws Exception {
        ConsumeMobileStatsVO stats = new ConsumeMobileStatsVO ();
        stats.setDeviceId (2001L);

        when (consumeMobileService.getDeviceTodayStats (eq (2001L))).thenReturn (stats);

        mockMvc.perform (
                get ("/api/v1/consume/mobile/device/today-stats/2001").contentType (MediaType.APPLICATION_JSON))
                .andExpect (status ().isOk ()).andExpect (jsonPath ("$.code").value (200));
    }

    @Test
    @DisplayName("获取实时交易汇总-成功")
    void testGetTransactionSummary () throws Exception {
        ConsumeMobileSummaryVO summary = new ConsumeMobileSummaryVO ();
        summary.setAreaId ("3001");

        when (consumeMobileService.getTransactionSummary (eq ("3001"))).thenReturn (summary);

        mockMvc.perform (get ("/api/v1/consume/mobile/transaction/summary").param ("areaId", "3001")
                .contentType (MediaType.APPLICATION_JSON)).andExpect (status ().isOk ())
                .andExpect (jsonPath ("$.code").value (200));
    }
}
