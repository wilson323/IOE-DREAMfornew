package net.lab1024.sa.access.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AlertRuleDao;
import net.lab1024.sa.access.dao.DeviceAlertDao;
import net.lab1024.sa.access.domain.entity.AlertRuleEntity;
import net.lab1024.sa.access.domain.entity.DeviceAlertEntity;
import net.lab1024.sa.access.domain.form.DeviceAlertHandleForm;
import net.lab1024.sa.access.domain.form.DeviceAlertQueryForm;
import net.lab1024.sa.access.domain.vo.AlertStatisticsVO;
import net.lab1024.sa.access.domain.vo.DeviceAlertVO;
import net.lab1024.sa.access.manager.impl.AlertManagerImpl;
import net.lab1024.sa.access.service.AlertNotificationService;
import net.lab1024.sa.access.service.AlertRuleService;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 告警管理器单元测试
 * <p>
 * 测试范围：
 * - 告警检测和创建
 * - 告警处理（确认/处理/忽略）
 * - 告警查询
 * - 告警统计
 * - 批量操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlertManagerTest {

    @Mock
    private DeviceAlertDao deviceAlertDao;

    @Mock
    private AlertRuleDao alertRuleDao;

    @Mock
    private AlertRuleService alertRuleService;

    @Mock
    private AlertNotificationService alertNotificationService;

    @Mock
    private net.lab1024.sa.access.websocket.AlertWebSocketService alertWebSocketService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AlertManagerImpl alertManager;

    private DeviceAlertEntity testAlert;
    private AlertRuleEntity testRule;

    @BeforeEach
    void setUp() {
        log.info("[测试准备] 初始化测试数据");

        // 准备测试告警实体
        testAlert = new DeviceAlertEntity();
        testAlert.setAlertId(1L);
        testAlert.setDeviceId(100L);
        testAlert.setDeviceCode("DEV001");
        testAlert.setDeviceName("门禁控制器");
        testAlert.setAlertType("DEVICE_OFFLINE");
        testAlert.setAlertLevel(3);
        testAlert.setAlertStatus(0);
        testAlert.setAlertTitle("设备离线");
        testAlert.setAlertMessage("设备 DEV001 已离线");
        testAlert.setAlertOccurredTime(LocalDateTime.now());
        testAlert.setCreateTime(LocalDateTime.now());

        // 准备测试规则实体
        testRule = new AlertRuleEntity();
        testRule.setRuleId(1L);
        testRule.setRuleName("设备离线告警");
        testRule.setRuleCode("DEVICE_OFFLINE");
        testRule.setRuleType("DEVICE_OFFLINE");
        testRule.setEnabled(1);
        testRule.setPriority(100);

        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        log.info("[测试准备] 测试数据初始化完成");
    }

    @Test
    void testCreateAlert_Success() {
        log.info("[测试用例] 测试成功创建告警");

        // Mock DAO行为
        when(deviceAlertDao.insert(any(DeviceAlertEntity.class))).thenReturn(1);

        // 执行测试
        ResponseDTO<Long> response = alertManager.createAlert(testAlert);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).insert(any(DeviceAlertEntity.class));

        log.info("[测试用例] 测试通过: 告警创建成功, alertId={}", response.getData());
    }

    @Test
    void testHandleAlert_Confirm() {
        log.info("[测试用例] 测试确认告警");

        // 准备处理表单
        DeviceAlertHandleForm handleForm = new DeviceAlertHandleForm();
        handleForm.setAlertId(1L);
        handleForm.setActionType("CONFIRM"); // 确认
        handleForm.setRemark("已确认，正在处理");

        // Mock DAO行为
        when(deviceAlertDao.selectById(1L)).thenReturn(testAlert);
        when(deviceAlertDao.updateById(any(DeviceAlertEntity.class))).thenReturn(1);

        // 执行测试
        ResponseDTO<Void> response = alertManager.handleAlert(handleForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).selectById(1L);
        verify(deviceAlertDao, times(1)).updateById(any(DeviceAlertEntity.class));

        log.info("[测试用例] 测试通过: 告警确认成功");
    }

    @Test
    void testQueryAlerts_Success() {
        log.info("[测试用例] 测试成功分页查询告警");

        // 准备查询表单
        DeviceAlertQueryForm queryForm = new DeviceAlertQueryForm();
        queryForm.setAlertType("DEVICE_OFFLINE");
        queryForm.setAlertLevel(3);
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        // 准备分页结果
        Page<DeviceAlertEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testAlert));
        page.setTotal(1);

        // Mock DAO行为
        when(deviceAlertDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        ResponseDTO<PageResult<DeviceAlertVO>> response = alertManager.queryAlerts(queryForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotal());
        assertEquals(1, response.getData().getList().size());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        log.info("[测试用例] 测试通过: 告警分页查询成功");
    }

    @Test
    void testGetAlertDetail_Success() {
        log.info("[测试用例] 测试成功查询告警详情");

        // Mock DAO行为
        when(deviceAlertDao.selectById(1L)).thenReturn(testAlert);

        // 执行测试
        ResponseDTO<DeviceAlertVO> response = alertManager.getAlertDetail(1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("DEVICE_OFFLINE", response.getData().getAlertType());
        assertEquals("设备离线", response.getData().getAlertTitle());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).selectById(1L);

        log.info("[测试用例] 测试通过: 告警详情查询成功");
    }

    @Test
    void testGetAlertStatistics_Success() {
        log.info("[测试用例] 测试成功获取告警统计");

        // Mock DAO行为 - 返回Map列表，不是Object数组
        Map<String, Object> status0 = new HashMap<>();
        status0.put("alertStatus", 0);
        status0.put("count", 65L);

        Map<String, Object> status1 = new HashMap<>();
        status1.put("alertStatus", 1);
        status1.put("count", 15L);

        Map<String, Object> status2 = new HashMap<>();
        status2.put("alertStatus", 2);
        status2.put("count", 30L);

        Map<String, Object> status3 = new HashMap<>();
        status3.put("alertStatus", 3);
        status3.put("count", 5L);

        Map<String, Object> level4 = new HashMap<>();
        level4.put("alertLevel", 4);
        level4.put("count", 5L);

        Map<String, Object> level3 = new HashMap<>();
        level3.put("alertLevel", 3);
        level3.put("count", 30L);

        Map<String, Object> level2 = new HashMap<>();
        level2.put("alertLevel", 2);
        level2.put("count", 20L);

        Map<String, Object> level1 = new HashMap<>();
        level1.put("alertLevel", 1);
        level1.put("count", 10L);

        when(deviceAlertDao.countByAlertStatus()).thenReturn(Arrays.asList(
                status0, status1, status2, status3
        ));
        when(deviceAlertDao.countByAlertLevel()).thenReturn(Arrays.asList(
                level4, level3, level2, level1
        ));
        when(deviceAlertDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(20L); // 今日

        // 执行测试
        ResponseDTO<AlertStatisticsVO> response = alertManager.getAlertStatistics();

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(115L, response.getData().getTotalAlerts()); // 所有状态总和: 65+15+30+5=115
        assertEquals(65L, response.getData().getUnconfirmedAlerts()); // 状态0的为未确认
        assertEquals(5L, response.getData().getCriticalAlerts());
        assertEquals(20L, response.getData().getTodayAlerts());

        log.info("[测试用例] 测试通过: 告警统计获取成功");
    }

    @Test
    void testBatchConfirmAlerts_Success() {
        log.info("[测试用例] 测试成功批量确认告警");

        // 准备数据
        List<Long> alertIds = Arrays.asList(1L, 2L, 3L);
        String remark = "批量确认";

        // Mock DAO行为 - 每次返回新的对象，避免状态污染
        when(deviceAlertDao.selectById(anyLong())).thenAnswer(invocation -> {
            Long alertId = invocation.getArgument(0);
            DeviceAlertEntity alert = new DeviceAlertEntity();
            alert.setAlertId(alertId);
            alert.setAlertStatus(0);  // 初始状态为未确认
            alert.setDeviceId(100L);
            alert.setAlertTitle("测试告警" + alertId);
            return alert;
        });
        when(deviceAlertDao.updateById(any(DeviceAlertEntity.class))).thenReturn(1);

        // 执行测试
        ResponseDTO<Integer> response = alertManager.batchConfirmAlerts(alertIds, remark);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(3, response.getData());

        // 验证DAO调用次数
        verify(deviceAlertDao, times(3)).selectById(anyLong());
        verify(deviceAlertDao, times(3)).updateById(any(DeviceAlertEntity.class));

        log.info("[测试用例] 测试通过: 批量确认成功, count={}", response.getData());
    }

    @Test
    void testQueryCriticalAlerts_Success() {
        log.info("[测试用例] 测试成功查询紧急告警");

        // Mock DAO行为
        when(deviceAlertDao.selectRecentCriticalAlerts(10)).thenReturn(Arrays.asList(testAlert));

        // 执行测试
        ResponseDTO<List<DeviceAlertVO>> response = alertManager.queryCriticalAlerts(10);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).selectRecentCriticalAlerts(10);

        log.info("[测试用例] 测试通过: 紧急告警查询成功");
    }

    @Test
    void testGetAlertTrend_Success() {
        log.info("[测试用例] 测试成功获取告警趋势");

        // Mock DAO行为
        when(deviceAlertDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Arrays.asList(testAlert));

        // 执行测试
        ResponseDTO<Map<String, Long>> response = alertManager.getAlertTrend();

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());

        log.info("[测试用例] 测试通过: 告警趋势获取成功, dataPoints={}", response.getData().size());
    }

    @Test
    void testCleanupExpiredAlerts_Success() {
        log.info("[测试用例] 测试成功清理过期告警");

        // Mock DAO行为
        when(deviceAlertDao.delete(any(LambdaQueryWrapper.class))).thenReturn(100);

        // 执行测试
        ResponseDTO<Integer> response = alertManager.cleanupExpiredAlerts(90);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals(100, response.getData());

        // 验证DAO调用
        verify(deviceAlertDao, times(1)).delete(any(LambdaQueryWrapper.class));

        log.info("[测试用例] 测试通过: 过期告警清理成功, deletedCount={}", response.getData());
    }

    @Test
    void testDetectAndCreateAlert_NoMatchingRule() {
        log.info("[测试用例] 测试检测告警-无匹配规则");

        // 准备设备数据
        Long deviceId = 100L;
        String deviceStatus = "OFFLINE";
        Map<String, Object> deviceData = new HashMap<>();
        deviceData.put("temperature", 45);
        deviceData.put("status", "FAULT");

        // Mock DAO行为：没有启用的规则
        when(alertRuleDao.selectEnabledRules()).thenReturn(new ArrayList<>());

        // 执行测试
        ResponseDTO<Boolean> response = alertManager.detectAndCreateAlert(deviceId, deviceStatus, deviceData);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertFalse(response.getData()); // 没有创建告警

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectEnabledRules();
        verify(deviceAlertDao, never()).insert(any(DeviceAlertEntity.class));

        log.info("[测试用例] 测试通过: 无匹配规则，未创建告警");
    }
}
