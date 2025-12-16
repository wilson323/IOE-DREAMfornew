package net.lab1024.sa.consume.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.time.LocalDateTime;
import java.util.Collections;
import net.lab1024.sa.consume.domain.dto.MobileQuickConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.MobileScanConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.MobileRechargeRequestDTO;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.consume.consume.domain.vo.MobileConsumeStatisticsVO;
import net.lab1024.sa.consume.consume.domain.vo.MobileAccountInfoVO;
import net.lab1024.sa.common.organization.entity.DeviceEntity;
import net.lab1024.sa.consume.domain.dto.ConsumeQueryDTO;
import net.lab1024.sa.consume.domain.dto.ConsumeRequestDTO;
import net.lab1024.sa.consume.domain.dto.RechargeRequestDTO;
import net.lab1024.sa.consume.domain.vo.ConsumeRecordVO;
import net.lab1024.sa.consume.domain.vo.ConsumeTransactionResultVO;
import net.lab1024.sa.consume.domain.vo.MobileBillDetailVO;
import net.lab1024.sa.consume.service.ConsumeService;
import net.lab1024.sa.consume.service.ConsumeMobileService;
import net.lab1024.sa.consume.service.MobileAccountInfoService;
import net.lab1024.sa.consume.service.MobileConsumeStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 移动端消费控制器测试
 * 移动端API接口单元测试
 *
 * @author IOE-DREAM架构团队
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("移动端消费控制器测试")
@SuppressWarnings("null")
@MockitoSettings(strictness = Strictness.LENIENT)
class MobileConsumeControllerTest {

    @Mock
    private ConsumeService consumeService;

    @Mock
    private MobileConsumeStatisticsService mobileConsumeStatisticsService;

    @Mock
    private MobileAccountInfoService mobileAccountInfoService;

    @Mock
    private net.lab1024.sa.common.gateway.GatewayServiceClient gatewayServiceClient;

    @Mock
    private ConsumeMobileService consumeMobileService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private MobileConsumeController mobileConsumeController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(mobileConsumeController).setValidator(validator).build();

        ConsumeTransactionResultVO transactionResult = new ConsumeTransactionResultVO();
        transactionResult.setTransactionNo("TXN001");
        transactionResult.setTransactionStatus(2);
        transactionResult.setTransactionTime(LocalDateTime.now());
        transactionResult.setAmount(new BigDecimal("50.00"));
        transactionResult.setBalanceAfter(new BigDecimal("950.00"));
        when(consumeService.consume(any(ConsumeRequestDTO.class))).thenReturn(ResponseDTO.ok(transactionResult));

        @SuppressWarnings("unchecked")
        IPage<ConsumeRecordVO> page = mock(IPage.class);
        when(page.getRecords()).thenReturn(Collections.emptyList());
        when(page.getTotal()).thenReturn(0L);
        when(page.getCurrent()).thenReturn(1L);
        when(page.getSize()).thenReturn(20L);
        when(page.getPages()).thenReturn(0L);
        when(consumeService.queryConsumeRecordPage(any(ConsumeQueryDTO.class))).thenReturn(ResponseDTO.ok(page));

        when(consumeService.recharge(any(RechargeRequestDTO.class))).thenReturn(ResponseDTO.ok());

        MobileConsumeStatisticsVO stats = new MobileConsumeStatisticsVO();
        stats.setTodayConsumeCount(15);
        stats.setTodayConsumeAmount(new BigDecimal("320.50"));
        stats.setWeekConsumeCount(89);
        stats.setWeekConsumeAmount(new BigDecimal("1250.80"));
        stats.setMonthConsumeCount(312);
        stats.setMonthConsumeAmount(new BigDecimal("5630.20"));
        when(mobileConsumeStatisticsService.getConsumeStatistics(eq(1001L), anyString(), any(), any()))
                .thenReturn(ResponseDTO.ok(stats));

        MobileAccountInfoVO accountInfo = new MobileAccountInfoVO();
        accountInfo.setAccountId(1L);
        accountInfo.setAccountNumber("ACC1001");
        accountInfo.setBalance(new BigDecimal("850.30"));
        accountInfo.setStatus("ACTIVE");
        when(mobileAccountInfoService.getAccountInfo(anyLong(), any())).thenReturn(ResponseDTO.ok(accountInfo));

        DeviceEntity deviceEntity = new DeviceEntity();
        when(gatewayServiceClient.callCommonService(anyString(), any(), isNull(), eq(DeviceEntity.class)))
                .thenReturn(ResponseDTO.ok(deviceEntity));

        MobileBillDetailVO billDetail = new MobileBillDetailVO();
        billDetail.setOrderId("MOBILE_20250130001");
        billDetail.setAmount(new BigDecimal("50.00"));
        billDetail.setConsumeType("DINING");
        when(consumeMobileService.getBillDetail(anyString())).thenReturn(billDetail);
    }

    @Test
    @DisplayName("测试快捷消费 - 成功")
    void testQuickConsume_Success() throws Exception {
        // Given
        MobileQuickConsumeRequestDTO request = new MobileQuickConsumeRequestDTO();
        request.setOrderId("MOBILE_20250130001");
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("50.00"));
        request.setDeviceId("MOBILE_001");
        request.setAreaId(1L);
        request.setConsumeType("DINING");
        request.setDescription("午餐消费");

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/quick-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试快捷消费 - 参数验证失败")
    void testQuickConsume_ValidationError() throws Exception {
        // Given - 无效的请求参数
        MobileQuickConsumeRequestDTO request = new MobileQuickConsumeRequestDTO();
        request.setOrderId(""); // 空的订单ID
        request.setAmount(BigDecimal.ZERO); // 零金额

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/quick-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试获取消费记录 - 成功")
    void testGetConsumeRecords_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/records")
                .param("pageNum", "1")
                .param("pageSize", "20")
                .param("startDate", "2025-01-01T00:00:00")
                .param("endDate", "2025-01-31T23:59:59")
                .param("consumeType", "DINING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取消费统计 - 成功")
    void testGetConsumeStatistics_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/statistics")
                .requestAttr("userId", 1001L)
                .param("statisticsType", "daily")
                .param("startDate", "2025-01-01T00:00:00")
                .param("endDate", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.todayConsumeCount").value(15))
                .andExpect(jsonPath("$.data.todayConsumeAmount").value(320.50))
                .andExpect(jsonPath("$.data.weekConsumeCount").value(89))
                .andExpect(jsonPath("$.data.weekConsumeAmount").value(1250.80))
                .andExpect(jsonPath("$.data.monthConsumeCount").value(312))
                .andExpect(jsonPath("$.data.monthConsumeAmount").value(5630.20));
    }

    @Test
    @DisplayName("测试获取账户信息 - 成功")
    void testGetAccountInfo_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/account-info")
                .param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accountId").value(1))
                .andExpect(jsonPath("$.data.accountNumber").value("ACC1001"))
                .andExpect(jsonPath("$.data.balance").value(850.30))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("测试账户充值 - 成功")
    void testRecharge_Success() throws Exception {
        // Given
        MobileRechargeRequestDTO request = new MobileRechargeRequestDTO();
        request.setOrderId("MOBILE_RECHARGE_20250130001");
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("100.00"));
        request.setRechargeType("ONLINE");
        request.setPaymentMethod("WECHAT");
        request.setDescription("微信充值");

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/recharge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取消费类型 - 成功")
    void testGetConsumeTypes_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/consume-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取设备信息 - 成功")
    void testGetDeviceInfo_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/device-info")
                .param("deviceId", "MOBILE_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deviceId").value("MOBILE_001"));
    }

    @Test
    @DisplayName("测试扫码消费 - 成功")
    void testScanConsume_Success() throws Exception {
        // Given
        MobileScanConsumeRequestDTO request = new MobileScanConsumeRequestDTO();
        request.setQrCode("QR_CONSUME_MERCHANT_001_AREA_001");
        request.setAmount(new BigDecimal("25.00"));
        request.setAccountId(1L);
        request.setConsumeType("DINING");
        request.setDescription("扫码午餐消费");

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/scan-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试获取账单详情 - 成功")
    void testGetBillDetail_Success() throws Exception {
        // Given
        String orderId = "MOBILE_20250130001";

        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/bill/{orderId}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.amount").value(50.00))
                .andExpect(jsonPath("$.data.consumeType").value("DINING"));
    }

    @Test
    @DisplayName("测试参数验证 - 账户ID为空")
    void testGetAccountInfo_MissingAccountId() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/mobile/v1/consume/account-info"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试参数验证 - 消费金额为负数")
    void testQuickConsume_NegativeAmount() throws Exception {
        // Given
        MobileQuickConsumeRequestDTO request = new MobileQuickConsumeRequestDTO();
        request.setOrderId("MOBILE_20250130001");
        request.setAccountId(1L);
        request.setAmount(new BigDecimal("-10.00")); // 负数金额
        request.setDeviceId("MOBILE_001");

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/quick-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试分页查询 - 参数默认值")
    void testGetConsumeRecords_DefaultParameters() throws Exception {
        // When & Then - 使用默认分页参数
        mockMvc.perform(get("/api/mobile/v1/consume/records"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("测试扫码消费 - 二维码内容为空")
    void testScanConsume_EmptyQrCode() throws Exception {
        // Given
        MobileScanConsumeRequestDTO request = new MobileScanConsumeRequestDTO();
        request.setQrCode(""); // 空的二维码
        request.setAmount(new BigDecimal("25.00"));

        // When & Then
        mockMvc.perform(post("/api/mobile/v1/consume/scan-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // 集成测试示例
    @Test
    @DisplayName("集成测试 - 完整的消费流程")
    void testCompleteConsumeFlow() throws Exception {
        // 1. 获取账户信息
        mockMvc.perform(get("/api/mobile/v1/consume/account-info")
                .param("accountId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 2. 获取消费类型
        mockMvc.perform(get("/api/mobile/v1/consume/consume-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 3. 执行快捷消费
        MobileQuickConsumeRequestDTO consumeRequest = new MobileQuickConsumeRequestDTO();
        consumeRequest.setOrderId("MOBILE_20250130001");
        consumeRequest.setAccountId(1L);
        consumeRequest.setAmount(new BigDecimal("50.00"));
        consumeRequest.setDeviceId("MOBILE_001");
        consumeRequest.setConsumeType("DINING");

        mockMvc.perform(post("/api/mobile/v1/consume/quick-consume")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consumeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 4. 获取消费记录
        mockMvc.perform(get("/api/mobile/v1/consume/records")
                .param("pageNum", "1")
                .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 5. 获取消费统计
        mockMvc.perform(get("/api/mobile/v1/consume/statistics").requestAttr("userId", 1001L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}


