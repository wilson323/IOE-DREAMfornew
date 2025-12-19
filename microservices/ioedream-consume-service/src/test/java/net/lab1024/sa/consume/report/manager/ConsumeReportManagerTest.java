package net.lab1024.sa.consume.report.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.lab1024.sa.consume.dao.ConsumeTransactionDao;
import net.lab1024.sa.consume.entity.ConsumeTransactionEntity;
import net.lab1024.sa.consume.report.dao.ConsumeReportTemplateDao;
import net.lab1024.sa.consume.report.domain.entity.ConsumeReportTemplateEntity;
import net.lab1024.sa.consume.report.domain.form.ReportParams;
import net.lab1024.sa.consume.report.manager.impl.ConsumeReportManagerImpl;

/**
 * ConsumeReportManager单元测试
 * <p>
 * 目标覆盖率：>= 80%
 * 测试范围：报表生成和导出功能
 * </p>
 *
 * @author IOE-DREAM Team
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ConsumeReportManager单元测试")
class ConsumeReportManagerTest {

        @Mock
        private ConsumeReportTemplateDao reportTemplateDao;

        @Mock
        private ConsumeTransactionDao consumeTransactionDao;

        @Mock
        private ObjectMapper objectMapper;

        @InjectMocks
        private ConsumeReportManagerImpl reportManager;

        private ConsumeReportTemplateEntity mockTemplate;
        private ConsumeTransactionEntity mockTransaction;
        private ReportParams defaultParams;

        @BeforeEach
        void setUp() {
                // 准备模拟报表模板
                mockTemplate = new ConsumeReportTemplateEntity();
                mockTemplate.setId(1L);
                mockTemplate.setTemplateName("消费统计报表");
                mockTemplate.setTemplateType("DAILY");
                mockTemplate.setEnabled(true);
                mockTemplate.setReportConfig("{\"includeDetails\":false}");

                // 准备模拟交易数据
                mockTransaction = new ConsumeTransactionEntity();
                mockTransaction.setId(1L);
                mockTransaction.setUserId(100L);
                mockTransaction.setFinalMoney(new BigDecimal("10.00"));
                mockTransaction.setConsumeTime(LocalDateTime.now());
                mockTransaction.setDeletedFlag(0); // 0表示未删除（继承自BaseEntity的deletedFlag字段）

                defaultParams = new ReportParams();
                defaultParams.setStartTime(LocalDateTime.now().minusDays(7));
                defaultParams.setEndTime(LocalDateTime.now());
        }

        @Test
        @DisplayName("测试生成对账报告-成功场景")
        void testGenerateReconciliationReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("CUSTOM");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
                verify(reportTemplateDao, times(1)).selectById(mockTemplate.getId());
        }

        @Test
        @DisplayName("测试生成消费统计报告-成功场景")
        void testGenerateConsumeStatisticsReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("DAILY");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
                assertTrue(response.getData().containsKey("data"));
                Object dataObj = response.getData().get("data");
                assertTrue(dataObj instanceof Map);
                @SuppressWarnings("unchecked")
                Map<String, Object> reportData = (Map<String, Object>) dataObj;
                assertTrue(reportData.containsKey("totalCount") || reportData.containsKey("totalMoney"));
        }

        @Test
        @DisplayName("测试生成日报-成功场景")
        void testGenerateDailyReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("DAILY");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
        }

        @Test
        @DisplayName("测试生成周报-成功场景")
        void testGenerateWeeklyReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("WEEKLY");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
        }

        @Test
        @DisplayName("测试生成月报-成功场景")
        void testGenerateMonthlyReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("MONTHLY");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
        }

        @Test
        @DisplayName("测试生成通用报表-成功场景")
        void testGenerateGenericReport_Success() throws Exception {
                // Given
                mockTemplate.setTemplateType("CUSTOM");
                List<ConsumeTransactionEntity> transactions = new ArrayList<>();
                transactions.add(mockTransaction);

                when(reportTemplateDao.selectById(mockTemplate.getId())).thenReturn(mockTemplate);
                // 正确的 Mockito Matcher 用法：使用带泛型的 TypeReference
                when(objectMapper.readValue(anyString(),
                                org.mockito.ArgumentMatchers.<TypeReference<Map<String, Object>>>any()))
                                .thenReturn(Map.of("includeDetails", false));
                when(consumeTransactionDao.selectByTimeRange(any(LocalDateTime.class), any(LocalDateTime.class)))
                                .thenReturn(transactions);

                // When - 使用公共方法generateReport
                net.lab1024.sa.common.dto.ResponseDTO<Map<String, Object>> response = reportManager.generateReport(
                                mockTemplate.getId(), defaultParams);

                // Then
                assertNotNull(response);
                assertTrue(response.isSuccess());
                assertNotNull(response.getData());
        }
}
