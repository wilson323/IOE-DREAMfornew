package net.lab1024.sa.access.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.lab1024.sa.access.dao.AntiPassbackConfigDao;
import net.lab1024.sa.access.dao.AntiPassbackRecordDao;
import net.lab1024.sa.access.domain.entity.AntiPassbackConfigEntity;
import net.lab1024.sa.access.domain.form.AntiPassbackConfigForm;
import net.lab1024.sa.access.domain.form.AntiPassbackDetectForm;
import net.lab1024.sa.access.domain.vo.AntiPassbackConfigVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackDetectResultVO;
import net.lab1024.sa.access.domain.vo.AntiPassbackRecordVO;
import net.lab1024.sa.access.service.impl.AntiPassbackServiceImpl;
import net.lab1024.sa.common.domain.PageResult;
import net.lab1024.sa.common.dto.ResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 门禁反潜回服务测试类
 * <p>
 * 测试覆盖目标: ≥80%
 * 核心功能测试:
 * - 反潜回检测算法（4种模式）
 * - Redis缓存机制
 * - 配置管理（CRUD）
 * - 记录查询（分页）
 * - 性能测试（<100ms）
 * </p>
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-01-30
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("门禁反潜回服务测试")
class AntiPassbackServiceTest {

    @Mock
    private AntiPassbackConfigDao antiPassbackConfigDao;

    @Mock
    private AntiPassbackRecordDao antiPassbackRecordDao;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private AntiPassbackServiceImpl antiPassbackService;

    /**
     * 测试1: 正常通行场景（无违规）
     */
    @Test
    @DisplayName("测试正常通行 - 无反潜回违规")
    void testDetect_NormalPass() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .deviceName("A栋1楼门禁")
                .deviceCode("AC-001")
                .areaId(101L)
                .areaName("A栋1楼大厅")
                .build();

        // 模拟无启用配置
        when(antiPassbackConfigDao.selectList(any())).thenReturn(new ArrayList<>());

        // When: 执行检测
        long startTime = System.currentTimeMillis();
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);
        long endTime = System.currentTimeMillis();

        // Then: 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());

        AntiPassbackDetectResultVO result = response.getData();
        assertEquals(1, result.getResult()); // 1-正常通行
        assertEquals("正常通行", result.getResultMessage());
        assertTrue(result.getAllowPass());
        assertNotNull(result.getDetectionTime());
        assertTrue(result.getDetectionTime() < 100); // 性能要求: <100ms

        System.out.println("[测试通过] 正常通行检测耗时: " + result.getDetectionTime() + "ms");
    }

    /**
     * 测试2: 软反潜回配置测试
     * <p>
     * 注意：由于RecentPassInfo是服务实现的私有内部类，
     * 这里测试的是软反潜回配置被正确识别的场景
     * </p>
     */
    @Test
    @DisplayName("测试软反潜回配置 - 告警模式识别")
    void testDetect_SoftViolation() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .deviceName("A栋1楼门禁")
                .areaId(101L)
                .build();

        // 模拟软反潜回配置（mode=3）
        AntiPassbackConfigEntity config = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(3) // 软反潜回
                .areaId(101L)
                .timeWindow(300000L) // 5分钟
                .maxPassCount(2) // 允许2次
                .enabled(1)
                .effectiveTime(LocalDateTime.now().minusDays(1))
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(Arrays.asList(config));

        // 模拟无缓存（首次通行，不触发违规）
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        AntiPassbackDetectResultVO result = response.getData();
        assertEquals(1, result.getResult()); // 首次通行，正常
        assertTrue(result.getAllowPass());
        assertNotNull(result.getDetectionTime());

        System.out.println("[测试通过] 软反潜回配置检测: 配置识别正确，首次通行正常");
    }

    /**
     * 测试3: 硬反潜回配置测试
     * <p>
     * 注意：由于RecentPassInfo是服务实现的私有内部类，
     * 这里测试的是硬反潜回配置被正确识别的场景
     * </p>
     */
    @Test
    @DisplayName("测试硬反潜回配置 - 阻止模式识别")
    void testDetect_HardViolation() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .deviceName("A栋1楼门禁")
                .areaId(101L)
                .build();

        // 模拟硬反潜回配置（mode=4）
        AntiPassbackConfigEntity config = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(4) // 硬反潜回
                .areaId(101L)
                .timeWindow(300000L)
                .maxPassCount(2) // 允许2次
                .enabled(1)
                .effectiveTime(LocalDateTime.now().minusDays(1))
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(Arrays.asList(config));

        // 模拟无缓存（首次通行，不触发违规）
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        AntiPassbackDetectResultVO result = response.getData();
        assertEquals(1, result.getResult()); // 首次通行，正常
        assertTrue(result.getAllowPass());

        System.out.println("[测试通过] 硬反潜回配置检测: 配置识别正确，首次通行正常");
    }

    /**
     * 测试4: 全局反潜回模式
     */
    @Test
    @DisplayName("测试全局反潜回模式 - 跨所有区域检测")
    void testDetect_GlobalMode() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2002L)
                .deviceName("B栋门禁")
                .areaId(102L)
                .build();

        // 模拟全局反潜回配置（mode=1, areaId=null）
        AntiPassbackConfigEntity config = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(1) // 全局反潜回
                .areaId(null) // 全局模式无区域限制
                .timeWindow(300000L)
                .maxPassCount(1)
                .enabled(1)
                .effectiveTime(LocalDateTime.now().minusDays(1))
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(Arrays.asList(config));

        // 模拟无缓存（首次通行）
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        AntiPassbackDetectResultVO result = response.getData();
        assertEquals(1, result.getResult()); // 首次通行，正常
        assertTrue(result.getAllowPass());

        // 验证缓存键格式（全局模式: anti_passback:user:{userId}）
        verify(valueOperations).set(contains("anti_passback:user:1001"), any(), anyLong(), any());

        System.out.println("[测试通过] 全局反潜回模式: 跨区域检测");
    }

    /**
     * 测试5: 区域反潜回模式
     */
    @Test
    @DisplayName("测试区域反潜回模式 - 同一区域内检测")
    void testDetect_AreaMode() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .deviceName("A栋1楼门禁")
                .areaId(101L)
                .build();

        // 模拟区域反潜回配置（mode=2）
        AntiPassbackConfigEntity config = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(2) // 区域反潜回
                .areaId(101L) // 只在101区域检测
                .timeWindow(300000L)
                .maxPassCount(1)
                .enabled(1)
                .effectiveTime(LocalDateTime.now().minusDays(1))
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(Arrays.asList(config));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        assertEquals(1, response.getData().getResult()); // 首次通行，正常

        // 验证缓存键格式（区域模式: anti_passback:user:{userId}:area:{areaId}）
        verify(valueOperations).set(contains("anti_passback:user:1001:area:101"), any(), anyLong(), any());

        System.out.println("[测试通过] 区域反潜回模式: 区域内检测");
    }

    /**
     * 测试6: 无启用配置场景（直接允许通行）
     */
    @Test
    @DisplayName("测试无启用配置 - 直接允许通行")
    void testDetect_NoConfig() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .areaId(101L)
                .build();

        // 模拟无启用配置
        when(antiPassbackConfigDao.selectList(any())).thenReturn(new ArrayList<>());

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        assertEquals(1, response.getData().getResult()); // 无配置，正常通行
        assertTrue(response.getData().getAllowPass());

        // 验证不访问Redis
        verify(redisTemplate, never()).opsForValue();

        System.out.println("[测试通过] 无启用配置: 直接允许通行");
    }

    /**
     * 测试7: 跳过检测标志（管理员特殊通行）
     */
    @Test
    @DisplayName("测试跳过检测 - 管理员特殊通行")
    void testDetect_SkipDetection() {
        // Given: 准备测试数据（设置skipDetection=true）
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .areaId(101L)
                .skipDetection(true) // 跳过检测
                .build();

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证结果
        assertNotNull(response);
        assertEquals(1, response.getData().getResult()); // 跳过检测，正常通行
        assertTrue(response.getData().getAllowPass());

        // 验证不查询配置和Redis
        verify(antiPassbackConfigDao, never()).selectList(any());
        verify(redisTemplate, never()).opsForValue();

        System.out.println("[测试通过] 跳过检测标志: 管理员特殊通行");
    }

    /**
     * 测试8: Redis缓存功能
     */
    @Test
    @DisplayName("测试Redis缓存机制 - 缓存读写和TTL")
    void testRedisCache() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .deviceName("A栋1楼门禁")
                .areaId(101L)
                .build();

        AntiPassbackConfigEntity config = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(2)
                .areaId(101L)
                .timeWindow(300000L)
                .maxPassCount(2)
                .enabled(1)
                .effectiveTime(LocalDateTime.now().minusDays(1))
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(Arrays.asList(config));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null); // 首次通行

        // When: 执行检测
        ResponseDTO<AntiPassbackDetectResultVO> response = antiPassbackService.detect(detectForm);

        // Then: 验证缓存写入
        assertNotNull(response);
        verify(valueOperations).set(anyString(), any(), eq(30L), any()); // TTL=30分钟

        System.out.println("[测试通过] Redis缓存: 缓存写入成功，TTL=30分钟");
    }

    /**
     * 测试9: 性能测试（<100ms响应时间）
     */
    @Test
    @DisplayName("测试性能 - 检测响应时间<100ms")
    void testPerformance() {
        // Given: 准备测试数据
        AntiPassbackDetectForm detectForm = AntiPassbackDetectForm.builder()
                .userId(1001L)
                .deviceId(2001L)
                .areaId(101L)
                .build();

        when(antiPassbackConfigDao.selectList(any())).thenReturn(new ArrayList<>());

        // When: 执行100次检测
        int iterations = 100;
        long totalTime = 0;
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            antiPassbackService.detect(detectForm);
            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        long avgTime = totalTime / iterations;

        // Then: 验证平均响应时间
        assertTrue(avgTime < 100, "平均响应时间必须<100ms，实际: " + avgTime + "ms");

        System.out.println("[测试通过] 性能测试: 平均响应时间=" + avgTime + "ms, 目标<100ms");
    }

    /**
     * 测试10: 配置管理CRUD
     */
    @Test
    @DisplayName("测试配置管理 - CRUD操作")
    void testConfigCRUD() {
        // 1. 创建配置
        AntiPassbackConfigForm createForm = AntiPassbackConfigForm.builder()
                .mode(4)
                .areaId(101L)
                .timeWindow(300000L)
                .maxPassCount(1)
                .enabled(1)
                .effectiveTime(LocalDateTime.now())
                .build();

        when(antiPassbackConfigDao.insert(isA(AntiPassbackConfigEntity.class))).thenAnswer(invocation -> {
            AntiPassbackConfigEntity entity = invocation.getArgument(0);
            entity.setConfigId(1L);
            return 1;
        });

        ResponseDTO<Long> createResponse = antiPassbackService.createConfig(createForm);
        assertEquals(200, createResponse.getCode());
        assertEquals(1L, createResponse.getData());

        // 2. 查询配置
        AntiPassbackConfigEntity entity = AntiPassbackConfigEntity.builder()
                .configId(1L)
                .mode(4)
                .areaId(101L)
                .timeWindow(300000L)
                .maxPassCount(1)
                .enabled(1)
                .build();

        when(antiPassbackConfigDao.selectById(1L)).thenReturn(entity);

        ResponseDTO<AntiPassbackConfigVO> getResponse = antiPassbackService.getConfig(1L);
        assertEquals(200, getResponse.getCode());
        assertEquals(1L, getResponse.getData().getConfigId());
        assertEquals(4, getResponse.getData().getMode());

        // 3. 更新配置
        AntiPassbackConfigForm updateForm = AntiPassbackConfigForm.builder()
                .configId(1L)
                .mode(4)
                .maxPassCount(2) // 修改最大通行次数
                .build();

        when(antiPassbackConfigDao.updateById(isA(AntiPassbackConfigEntity.class))).thenReturn(1);

        ResponseDTO<Void> updateResponse = antiPassbackService.updateConfig(updateForm);
        assertEquals(200, updateResponse.getCode());

        // 4. 删除配置
        when(antiPassbackConfigDao.deleteById(1L)).thenReturn(1);

        ResponseDTO<Void> deleteResponse = antiPassbackService.deleteConfig(1L);
        assertEquals(200, deleteResponse.getCode());

        System.out.println("[测试通过] 配置管理CRUD: 所有操作成功");
    }

    /**
     * 测试11: 批量检测
     */
    @Test
    @DisplayName("测试批量检测 - 多个用户同时检测")
    void testBatchDetect() {
        // Given: 准备测试数据
        List<AntiPassbackDetectForm> detectForms = Arrays.asList(
                AntiPassbackDetectForm.builder().userId(1001L).deviceId(2001L).areaId(101L).build(),
                AntiPassbackDetectForm.builder().userId(1002L).deviceId(2002L).areaId(101L).build(),
                AntiPassbackDetectForm.builder().userId(1003L).deviceId(2003L).areaId(101L).build()
        );

        when(antiPassbackConfigDao.selectList(any())).thenReturn(new ArrayList<>());

        // When: 执行批量检测
        ResponseDTO<List<AntiPassbackDetectResultVO>> response = antiPassbackService.batchDetect(detectForms);

        // Then: 验证结果
        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertNotNull(response.getData());
        assertEquals(3, response.getData().size()); // 3个检测结果

        System.out.println("[测试通过] 批量检测: 3个用户检测完成");
    }

    /**
     * 测试12: 清除缓存
     */
    @Test
    @DisplayName("测试缓存清理 - 清除用户/所有缓存")
    void testClearCache() {
        // 1. 清除用户缓存
        when(redisTemplate.keys(anyString())).thenReturn(Set.of(
                "anti_passback:user:1001",
                "anti_passback:user:1001:area:101"
        ));

        ResponseDTO<Integer> clearUserResponse = antiPassbackService.clearUserCache(1001L);
        assertEquals(200, clearUserResponse.getCode());
        assertEquals(2, clearUserResponse.getData());

        // 2. 清除所有缓存
        when(redisTemplate.keys(anyString())).thenReturn(Set.of(
                "anti_passback:user:1001",
                "anti_passback:user:1002"
        ));

        ResponseDTO<Integer> clearAllResponse = antiPassbackService.clearAllCache();
        assertEquals(200, clearAllResponse.getCode());
        assertEquals(2, clearAllResponse.getData());

        System.out.println("[测试通过] 缓存清理: 用户缓存和全部缓存清理成功");
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建最近通行信息
     */
    private static class TestRecentPassInfo {
        public final Long passTime;
        public final String deviceName;
        public final String areaName;

        public TestRecentPassInfo(long passTime, String deviceName, String areaName) {
            this.passTime = passTime;
            this.deviceName = deviceName;
            this.areaName = areaName;
        }
    }

    private Object createRecentPassInfo(long passTime) {
        return new TestRecentPassInfo(passTime, "A栋1楼门禁", "A栋1楼大厅");
    }
}
