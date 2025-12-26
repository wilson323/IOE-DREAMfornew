package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.access.dao.AlertRuleDao;
import net.lab1024.sa.access.domain.entity.AlertRuleEntity;
import net.lab1024.sa.access.domain.form.AlertRuleForm;
import net.lab1024.sa.access.domain.form.AlertRuleQueryForm;
import net.lab1024.sa.access.domain.vo.AlertRuleVO;
import net.lab1024.sa.access.service.impl.AlertRuleServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import net.lab1024.sa.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * 告警规则服务单元测试
 * <p>
 * 测试范围：
 * - 规则CRUD操作
 * - 规则验证
 * - 规则启用/禁用
 * - 批量操作
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
class AlertRuleServiceTest {

    @Mock
    private AlertRuleDao alertRuleDao;

    @InjectMocks
    private AlertRuleServiceImpl alertRuleService;

    private AlertRuleForm testRuleForm;
    private AlertRuleEntity testRuleEntity;

    @BeforeEach
    void setUp() {
        log.info("[测试准备] 初始化测试数据");

        // 准备测试表单
        testRuleForm = new AlertRuleForm();
        testRuleForm.setRuleName("设备离线告警");
        testRuleForm.setRuleCode("DEVICE_OFFLINE");
        testRuleForm.setRuleType("DEVICE_OFFLINE");
        testRuleForm.setConditionType(1);
        testRuleForm.setAlertLevel(3);
        testRuleForm.setAlertTitleTemplate("设备离线告警");
        testRuleForm.setAlertMessageTemplate("设备 {deviceCode} 已离线");
        testRuleForm.setEnabled(1);
        testRuleForm.setPriority(100);

        // 准备测试实体
        testRuleEntity = new AlertRuleEntity();
        testRuleEntity.setRuleId(1L);
        testRuleEntity.setRuleName("设备离线告警");
        testRuleEntity.setRuleCode("DEVICE_OFFLINE");
        testRuleEntity.setRuleType("DEVICE_OFFLINE");
        testRuleEntity.setConditionType(1);
        testRuleEntity.setAlertLevel(3);
        testRuleEntity.setEnabled(1);
        testRuleEntity.setPriority(100);
        testRuleEntity.setCreateTime(LocalDateTime.now());
        testRuleEntity.setUpdateTime(LocalDateTime.now());

        log.info("[测试准备] 测试数据初始化完成");
    }

    @Test
    void testCreateRule_Success() {
        log.info("[测试用例] 测试成功创建告警规则");

        // Mock DAO行为 - insert后自动回填ruleId
        when(alertRuleDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(alertRuleDao.insert(any(AlertRuleEntity.class))).thenAnswer(invocation -> {
            AlertRuleEntity entity = invocation.getArgument(0);
            entity.setRuleId(1L);  // 模拟数据库回填ID
            return 1;
        });

        // 执行测试
        ResponseDTO<Long> response = alertRuleService.createRule(testRuleForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertTrue(response.getData() > 0);

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(alertRuleDao, times(1)).insert(any(AlertRuleEntity.class));

        log.info("[测试用例] 测试通过: 规则创建成功, ruleId={}", response.getData());
    }

    @Test
    void testCreateRule_RuleCodeExists() {
        log.info("[测试用例] 测试规则编码已存在");

        // Mock DAO行为：规则编码已存在
        when(alertRuleDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        // 执行测试并验证异常
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            alertRuleService.createRule(testRuleForm);
        });

        assertEquals("RULE_CODE_EXISTS", exception.getCode());
        assertEquals("规则编码已存在", exception.getMessage());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectCount(any(LambdaQueryWrapper.class));
        verify(alertRuleDao, never()).insert(any(AlertRuleEntity.class));

        log.info("[测试用例] 测试通过: 正确抛出规则编码已存在异常");
    }

    @Test
    void testUpdateRule_Success() {
        log.info("[测试用例] 测试成功更新告警规则");

        // 设置更新数据
        testRuleForm.setRuleId(1L);
        testRuleForm.setRuleName("设备离线告警（已更新）");

        // Mock DAO行为
        when(alertRuleDao.selectById(1L)).thenReturn(testRuleEntity);
        when(alertRuleDao.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(alertRuleDao.updateById(any(AlertRuleEntity.class))).thenReturn(1);

        // 执行测试
        ResponseDTO<Void> response = alertRuleService.updateRule(testRuleForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectById(1L);
        verify(alertRuleDao, times(1)).updateById(any(AlertRuleEntity.class));

        log.info("[测试用例] 测试通过: 规则更新成功");
    }

    @Test
    void testDeleteRule_Success() {
        log.info("[测试用例] 测试成功删除告警规则");

        // Mock DAO行为
        when(alertRuleDao.selectById(1L)).thenReturn(testRuleEntity);
        when(alertRuleDao.deleteById(1L)).thenReturn(1);

        // 执行测试
        ResponseDTO<Void> response = alertRuleService.deleteRule(1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectById(1L);
        verify(alertRuleDao, times(1)).deleteById(1L);

        log.info("[测试用例] 测试通过: 规则删除成功");
    }

    @Test
    void testGetRule_Success() {
        log.info("[测试用例] 测试成功查询告警规则");

        // Mock DAO行为
        when(alertRuleDao.selectById(1L)).thenReturn(testRuleEntity);

        // 执行测试
        ResponseDTO<AlertRuleVO> response = alertRuleService.getRule(1L);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals("DEVICE_OFFLINE", response.getData().getRuleCode());
        assertEquals("设备离线告警", response.getData().getRuleName());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectById(1L);

        log.info("[测试用例] 测试通过: 规则查询成功");
    }

    @Test
    void testQueryRules_Success() {
        log.info("[测试用例] 测试成功分页查询告警规则");

        // 准备查询表单
        AlertRuleQueryForm queryForm = new AlertRuleQueryForm();
        queryForm.setRuleType("DEVICE_OFFLINE");
        queryForm.setEnabled(1);
        queryForm.setPageNum(1);
        queryForm.setPageSize(10);

        // 准备分页结果
        Page<AlertRuleEntity> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testRuleEntity));
        page.setTotal(1);

        // Mock DAO行为
        when(alertRuleDao.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

        // 执行测试
        ResponseDTO<PageResult<AlertRuleVO>> response = alertRuleService.queryRules(queryForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getTotal());
        assertEquals(1, response.getData().getList().size());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        log.info("[测试用例] 测试通过: 规则分页查询成功");
    }

    @Test
    void testToggleRule_Success() {
        log.info("[测试用例] 测试成功切换规则状态");

        // Mock DAO行为 - Mock updateById方法
        when(alertRuleDao.selectById(1L)).thenReturn(testRuleEntity);
        when(alertRuleDao.updateById(any(AlertRuleEntity.class))).thenReturn(1);

        // 执行测试：禁用规则
        ResponseDTO<Void> response = alertRuleService.toggleRule(1L, 0);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());

        // 验证DAO调用
        verify(alertRuleDao, times(1)).selectById(1L);
        verify(alertRuleDao, times(1)).updateById(any(AlertRuleEntity.class));

        log.info("[测试用例] 测试通过: 规则状态切换成功");
    }

    @Test
    void testValidateRuleExpression_SimpleCondition() {
        log.info("[测试用例] 测试验证简单条件表达式");

        // 设置简单条件
        testRuleForm.setConditionType(1);
        testRuleForm.setConditionExpression(null);

        // 执行测试
        ResponseDTO<Boolean> response = alertRuleService.validateRuleExpression(testRuleForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertTrue(response.getData());

        log.info("[测试用例] 测试通过: 简单条件验证成功");
    }

    @Test
    void testValidateRuleExpression_AviatorExpression() {
        log.info("[测试用例] 测试验证Aviator表达式");

        // 设置Aviator表达式
        testRuleForm.setConditionType(2);
        testRuleForm.setConditionExpression("temperature > 50 && status == 'FAULT'");

        // 执行测试
        ResponseDTO<Boolean> response = alertRuleService.validateRuleExpression(testRuleForm);

        // 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        // 注意：当前实现中Aviator表达式验证返回true（TODO集成）
        assertTrue(response.getData());

        log.info("[测试用例] 测试通过: Aviator表达式验证成功");
    }
}
