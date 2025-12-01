package {{package}};

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.BaseTest;
import net.lab1024.sa.base.common.TestDataBuilder;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.page.PageParam;
import net.lab1024.sa.base.common.page.PageResult;

import org.junit.jupiter.api.*;
import jakarta.annotation.Resource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import jakarta.persistence.EntityManager;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {{EntityName}}Service测试类
 * <p>
 * 标准化的Service层测试模板，包含完整的CRUD操作测试
 * 严格遵循repowiki测试架构规范
 * </p>
 *
 * @author {{author}}
 * @since {{date}}
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class {{EntityName}}ServiceTest extends BaseTest {

    @Resource
    private {{EntityName}}Service {{entityName}}Service;

    @Resource
    private {{EntityName}}Dao {{entityName}}Dao;

    @Resource
    private EntityManager entityManager;

    @Mock
    private {{EntityName}}Manager {{entityName}}Manager;

    @InjectMocks
    private {{EntityName}}Service {{entityName}}ServiceWithMocks;

    private {{EntityName}}Entity testEntity;
    private {{EntityName}}AddForm testAddForm;
    private {{EntityName}}UpdateForm testUpdateForm;
    private {{EntityName}}QueryForm testQueryForm;

    @BeforeEach
    void setUp() {
        log.info("=== 开始{{EntityName}}Service测试 ===");
        MockitoAnnotations.openMocks(this);

        // 初始化测试数据
        testEntity = TestDataBuilder.create{{EntityName}}Entity();
        testAddForm = TestDataBuilder.create{{EntityName}}AddForm();
        testUpdateForm = TestDataBuilder.create{{EntityName}}UpdateForm();
        testQueryForm = TestDataBuilder.create{{EntityName}}QueryForm();
    }

    @AfterEach
    void tearDown() {
        log.info("=== {{EntityName}}Service测试结束 ===");
        // 清理测试数据
        cleanTestData();
    }

    // ==================== CRUD基础功能测试 ====================

    /**
     * 测试添加{{EntityName}}
     */
    @Test
    @Order(1)
    @DisplayName("测试添加{{EntityName}}")
    void testAdd() {
        logTestStart("testAdd");

        try {
            // 1. 准备测试数据
            {{EntityName}}AddForm addForm = TestDataBuilder.create{{EntityName}}AddForm();
            logTestData("添加表单", addForm);

            // 2. 执行添加操作
            ResponseDTO<Long> response = {{entityName}}Service.add(addForm);

            // 3. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "添加操作应该成功");
            assertNotNull(response.getData(), "返回的ID不能为空");

            Long createdId = response.getData();
            assertTrue(createdId > 0, "创建的ID应该大于0");

            // 4. 验证数据库中的数据
            {{EntityName}}Entity createdEntity = {{entityName}}Dao.getById(createdId);
            assertNotNull(createdEntity, "数据库中应该存在创建的实体");
            assertEquals(addForm.getBusinessKey(), createdEntity.getBusinessKey(), "业务键应该一致");
            assertEquals(addForm.getBusinessName(), createdEntity.getBusinessName(), "业务名称应该一致");

            log.info("✅ 添加{{EntityName}}测试通过，ID: {}", createdId);

        } catch (Exception e) {
            log.error("❌ 添加{{EntityName}}测试失败", e);
            fail("添加{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testAdd");
    }

    /**
     * 测试获取{{EntityName}}详情
     */
    @Test
    @Order(2)
    @DisplayName("测试获取{{EntityName}}详情")
    void testGetDetail() {
        logTestStart("testGetDetail");

        try {
            // 1. 先创建测试数据
            {{EntityName}}Entity entity = createTestEntity();

            // 2. 执行查询操作
            ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(entity.get{{EntityName}}Id());

            // 3. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "查询操作应该成功");
            assertNotNull(response.getData(), "返回的数据不能为空");

            {{EntityName}}VO vo = response.getData();
            assertEquals(entity.get{{EntityName}}Id(), vo.get{{EntityName}}Id(), "ID应该一致");
            assertEquals(entity.getBusinessKey(), vo.getBusinessKey(), "业务键应该一致");
            assertEquals(entity.getBusinessName(), vo.getBusinessName(), "业务名称应该一致");

            log.info("✅ 获取{{EntityName}}详情测试通过，ID: {}", vo.get{{EntityName}}Id());

        } catch (Exception e) {
            log.error("❌ 获取{{EntityName}}详情测试失败", e);
            fail("获取{{EntityName}}详情测试失败: " + e.getMessage());
        }

        logTestEnd("testGetDetail");
    }

    /**
     * 测试更新{{EntityName}}
     */
    @Test
    @Order(3)
    @DisplayName("测试更新{{EntityName}}")
    void testUpdate() {
        logTestStart("testUpdate");

        try {
            // 1. 先创建测试数据
            {{EntityName}}Entity entity = createTestEntity();

            // 2. 准备更新数据
            {{EntityName}}UpdateForm updateForm = new {{EntityName}}UpdateForm();
            updateForm.set{{EntityName}}Id(entity.get{{EntityName}}Id());
            updateForm.setBusinessName("更新后的业务名称");
            updateForm.setBusinessType("更新后的业务类型");
            updateForm.setRemark("更新后的备注");
            logTestData("更新表单", updateForm);

            // 3. 执行更新操作
            ResponseDTO<Boolean> response = {{entityName}}Service.update(updateForm);

            // 4. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "更新操作应该成功");
            assertTrue(response.getData(), "更新结果应该为true");

            // 5. 验证数据库中的数据
            {{EntityName}}Entity updatedEntity = {{entityName}}Dao.getById(entity.get{{EntityName}}Id());
            assertNotNull(updatedEntity, "更新后的实体应该存在");
            assertEquals(updateForm.getBusinessName(), updatedEntity.getBusinessName(), "业务名称应该已更新");
            assertEquals(updateForm.getBusinessType(), updatedEntity.getBusinessType(), "业务类型应该已更新");

            log.info("✅ 更新{{EntityName}}测试通过，ID: {}", entity.get{{EntityName}}Id());

        } catch (Exception e) {
            log.error("❌ 更新{{EntityName}}测试失败", e);
            fail("更新{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testUpdate");
    }

    /**
     * 测试删除{{EntityName}}
     */
    @Test
    @Order(4)
    @DisplayName("测试删除{{EntityName}}")
    void testDelete() {
        logTestStart("testDelete");

        try {
            // 1. 先创建测试数据
            {{EntityName}}Entity entity = createTestEntity();
            Long deleteId = entity.get{{EntityName}}Id();

            // 2. 执行删除操作
            ResponseDTO<Boolean> response = {{entityName}}Service.delete(deleteId);

            // 3. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "删除操作应该成功");
            assertTrue(response.getData(), "删除结果应该为true");

            // 4. 验证数据库中的数据（软删除）
            {{EntityName}}Entity deletedEntity = {{entityName}}Dao.getById(deleteId);
            assertNotNull(deletedEntity, "软删除的实体应该仍然存在");
            assertTrue(deletedEntity.getDeletedFlag(), "删除标记应该为true");

            log.info("✅ 删除{{EntityName}}测试通过，ID: {}", deleteId);

        } catch (Exception e) {
            log.error("❌ 删除{{EntityName}}测试失败", e);
            fail("删除{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testDelete");
    }

    /**
     * 测试分页查询{{EntityName}}
     */
    @Test
    @Order(5)
    @DisplayName("测试分页查询{{EntityName}}")
    void testGetPage() {
        logTestStart("testGetPage");

        try {
            // 1. 创建测试数据
            List<{{EntityName}}Entity> testEntities = createTestEntities(5);

            // 2. 准备查询参数
            {{EntityName}}QueryForm queryForm = new {{EntityName}}QueryForm();
            queryForm.setPageNum(1);
            queryForm.setPageSize(10);
            logTestData("查询表单", queryForm);

            // 3. 执行分页查询
            ResponseDTO<PageResult<{{EntityName}}VO>> response = {{entityName}}Service.getPage(queryForm);

            // 4. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "分页查询应该成功");
            assertNotNull(response.getData(), "分页结果不能为空");

            PageResult<{{EntityName}}VO> pageResult = response.getData();
            assertTrue(pageResult.getTotal() >= testEntities.size(), "总记录数应该大于等于创建的测试数据数量");
            assertNotNull(pageResult.getRows(), "数据列表不能为空");
            assertFalse(pageResult.getRows().isEmpty(), "数据列表不应该为空");

            log.info("✅ 分页查询{{EntityName}}测试通过，总数: {}, 当前页数据量: {}",
                    pageResult.getTotal(), pageResult.getRows().size());

        } catch (Exception e) {
            log.error("❌ 分页查询{{EntityName}}测试失败", e);
            fail("分页查询{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testGetPage");
    }

    /**
     * 测试查询{{EntityName}}列表
     */
    @Test
    @Order(6)
    @DisplayName("测试查询{{EntityName}}列表")
    void testQueryList() {
        logTestStart("testQueryList");

        try {
            // 1. 创建测试数据
            List<{{EntityName}}Entity> testEntities = createTestEntities(3);

            // 2. 准备查询参数
            {{EntityName}}QueryForm queryForm = new {{EntityName}}QueryForm();
            queryForm.setBusinessType(testEntities.get(0).getBusinessType());
            logTestData("查询表单", queryForm);

            // 3. 执行列表查询
            ResponseDTO<List<{{EntityName}}VO>> response = {{entityName}}Service.queryList(queryForm);

            // 4. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "列表查询应该成功");
            assertNotNull(response.getData(), "查询结果不能为空");

            List<{{EntityName}}VO> voList = response.getData();
            assertFalse(voList.isEmpty(), "查询结果不应该为空");

            // 验证查询条件过滤结果
            for ({{EntityName}}VO vo : voList) {
                assertEquals(queryForm.getBusinessType(), vo.getBusinessType(), "业务类型应该匹配查询条件");
            }

            log.info("✅ 查询{{EntityName}}列表测试通过，结果数量: {}", voList.size());

        } catch (Exception e) {
            log.error("❌ 查询{{EntityName}}列表测试失败", e);
            fail("查询{{EntityName}}列表测试失败: " + e.getMessage());
        }

        logTestEnd("testQueryList");
    }

    // ==================== 批量操作测试 ====================

    /**
     * 测试批量删除{{EntityName}}
     */
    @Test
    @Order(7)
    @DisplayName("测试批量删除{{EntityName}}")
    void testBatchDelete() {
        logTestStart("testBatchDelete");

        try {
            // 1. 创建测试数据
            List<{{EntityName}}Entity> testEntities = createTestEntities(3);
            List<Long> deleteIds = testEntities.stream()
                    .map({{EntityName}}Entity::get{{EntityName}}Id)
                    .collect(Collectors.toList());

            // 2. 执行批量删除
            ResponseDTO<Boolean> response = {{entityName}}Service.batchDelete(deleteIds);

            // 3. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "批量删除应该成功");
            assertTrue(response.getData(), "批量删除结果应该为true");

            // 4. 验证数据库中的数据
            for (Long id : deleteIds) {
                {{EntityName}}Entity deletedEntity = {{entityName}}Dao.getById(id);
                assertNotNull(deletedEntity, "软删除的实体应该仍然存在");
                assertTrue(deletedEntity.getDeletedFlag(), "删除标记应该为true");
            }

            log.info("✅ 批量删除{{EntityName}}测试通过，删除数量: {}", deleteIds.size());

        } catch (Exception e) {
            log.error("❌ 批量删除{{EntityName}}测试失败", e);
            fail("批量删除{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testBatchDelete");
    }

    // ==================== 异常场景测试 ====================

    /**
     * 测试获取不存在的{{EntityName}}详情
     */
    @Test
    @Order(8)
    @DisplayName("测试获取不存在{{EntityName}}详情")
    void testGetDetailNotFound() {
        logTestStart("testGetDetailNotFound");

        try {
            // 1. 使用不存在的ID
            Long nonExistentId = 99999L;

            // 2. 执行查询操作
            ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(nonExistentId);

            // 3. 验证结果
            assertNotNull(response, "响应不能为空");
            assertTrue(response.getOk(), "查询操作应该成功（返回空结果）");
            assertNull(response.getData(), "不存在的数据应该返回null");

            log.info("✅ 获取不存在{{EntityName}}详情测试通过");

        } catch (Exception e) {
            log.error("❌ 获取不存在{{EntityName}}详情测试失败", e);
            fail("获取不存在{{EntityName}}详情测试失败: " + e.getMessage());
        }

        logTestEnd("testGetDetailNotFound");
    }

    /**
     * 测试添加无效数据
     */
    @Test
    @Order(9)
    @DisplayName("测试添加无效{{EntityName}}数据")
    void testAddInvalidData() {
        logTestStart("testAddInvalidData");

        try {
            // 1. 准备无效数据（必填字段为空）
            {{EntityName}}AddForm invalidAddForm = new {{EntityName}}AddForm();
            // 不设置必填字段

            // 2. 执行添加操作
            ResponseDTO<Long> response = {{entityName}}Service.add(invalidAddForm);

            // 3. 验证结果（应该失败）
            assertNotNull(response, "响应不能为空");
            assertFalse(response.getOk(), "添加无效数据应该失败");
            assertNotNull(response.getMsg(), "错误信息不能为空");

            log.info("✅ 添加无效{{EntityName}}数据测试通过，错误信息: {}", response.getMsg());

        } catch (Exception e) {
            log.error("❌ 添加无效{{EntityName}}数据测试失败", e);
            fail("添加无效{{EntityName}}数据测试失败: " + e.getMessage());
        }

        logTestEnd("testAddInvalidData");
    }

    /**
     * 测试更新不存在的{{EntityName}}
     */
    @Test
    @Order(10)
    @DisplayName("测试更新不存在{{EntityName}}")
    void testUpdateNonExistent() {
        logTestStart("testUpdateNonExistent");

        try {
            // 1. 准备更新数据（使用不存在的ID）
            {{EntityName}}UpdateForm updateForm = new {{EntityName}}UpdateForm();
            updateForm.set{{EntityName}}Id(99999L);
            updateForm.setBusinessName("不存在的数据");

            // 2. 执行更新操作
            ResponseDTO<Boolean> response = {{entityName}}Service.update(updateForm);

            // 3. 验证结果（应该失败）
            assertNotNull(response, "响应不能为空");
            assertFalse(response.getOk(), "更新不存在的数据应该失败");
            assertNotNull(response.getMsg(), "错误信息不能为空");

            log.info("✅ 更新不存在{{EntityName}}测试通过，错误信息: {}", response.getMsg());

        } catch (Exception e) {
            log.error("❌ 更新不存在{{EntityName}}测试失败", e);
            fail("更新不存在{{EntityName}}测试失败: " + e.getMessage());
        }

        logTestEnd("testUpdateNonExistent");
    }

    // ==================== 性能测试 ====================

    /**
     * 测试批量查询性能
     */
    @Test
    @Order(11)
    @DisplayName("测试批量查询性能")
    void testBatchQueryPerformance() {
        logTestStart("testBatchQueryPerformance");

        try {
            // 1. 创建大量测试数据
            int batchSize = 100;
            List<{{EntityName}}Entity> testEntities = createTestEntities(batchSize);
            List<Long> ids = testEntities.stream()
                    .map({{EntityName}}Entity::get{{EntityName}}Id)
                    .collect(Collectors.toList());

            // 2. 测试批量查询性能
            long startTime = System.currentTimeMillis();

            List<{{EntityName}}VO> results = new ArrayList<>();
            for (Long id : ids) {
                ResponseDTO<{{EntityName}}VO> response = {{entityName}}Service.getDetail(id);
                if (response.getOk() && response.getData() != null) {
                    results.add(response.getData());
                }
            }

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // 3. 验证结果
            assertEquals(batchSize, results.size(), "查询结果数量应该等于创建的数据数量");
            assertTrue(duration < 5000, "批量查询应该在5秒内完成，实际耗时: " + duration + "ms");

            log.info("✅ 批量查询性能测试通过，数量: {}, 耗时: {}ms", batchSize, duration);

        } catch (Exception e) {
            log.error("❌ 批量查询性能测试失败", e);
            fail("批量查询性能测试失败: " + e.getMessage());
        }

        logTestEnd("testBatchQueryPerformance");
    }

    // ==================== 并发测试 ====================

    /**
     * 测试并发操作
     */
    @Test
    @Order(12)
    @DisplayName("测试并发操作")
    void testConcurrentOperations() {
        logTestStart("testConcurrentOperations");

        try {
            // 1. 创建测试数据
            {{EntityName}}Entity entity = createTestEntity();

            // 2. 创建多个并发任务
            List<CompletableFuture<ResponseDTO<{{EntityName}}VO>>> futures = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                CompletableFuture<ResponseDTO<{{EntityName}}VO>> future = CompletableFuture.supplyAsync(() -> {
                    return {{entityName}}Service.getDetail(entity.get{{EntityName}}Id());
                });
                futures.add(future);
            }

            // 3. 等待所有任务完成
            List<ResponseDTO<{{EntityName}}VO>> results = futures.stream()
                    .map(CompletableFuture::join)
                    .collect(Collectors.toList());

            // 4. 验证结果
            assertEquals(10, results.size(), "并发查询结果数量应该正确");

            int successCount = 0;
            for (ResponseDTO<{{EntityName}}VO> response : results) {
                if (response.getOk() && response.getData() != null) {
                    successCount++;
                    assertEquals(entity.get{{EntityName}}Id(), response.getData().get{{EntityName}}Id(), "查询结果ID应该一致");
                }
            }

            assertTrue(successCount >= 8, "至少8个并发查询应该成功，实际成功: " + successCount);

            log.info("✅ 并发操作测试通过，成功数量: {}/{}", successCount, results.size());

        } catch (Exception e) {
            log.error("❌ 并发操作测试失败", e);
            fail("并发操作测试失败: " + e.getMessage());
        }

        logTestEnd("testConcurrentOperations");
    }

    // ==================== 私有工具方法 ====================

    /**
     * 创建测试实体
     */
    private {{EntityName}}Entity createTestEntity() {
        try {
            {{EntityName}}Entity entity = TestDataBuilder.create{{EntityName}}Entity();
            {{entityName}}Dao.insert(entity);
            entityManager.flush();
            entityManager.clear();
            return entity;
        } catch (Exception e) {
            log.error("创建测试实体失败", e);
            throw new RuntimeException("创建测试实体失败", e);
        }
    }

    /**
     * 创建多个测试实体
     */
    private List<{{EntityName}}Entity> createTestEntities(int count) {
        List<{{EntityName}}Entity> entities = new ArrayList<>();
        try {
            for (int i = 0; i < count; i++) {
                {{EntityName}}Entity entity = TestDataBuilder.create{{EntityName}}Entity();
                entity.setBusinessKey("TEST_KEY_" + i);
                entity.setBusinessName("测试业务名称_" + i);
                {{entityName}}Dao.insert(entity);
                entities.add(entity);
            }
            entityManager.flush();
            entityManager.clear();
            return entities;
        } catch (Exception e) {
            log.error("创建批量测试实体失败", e);
            throw new RuntimeException("创建批量测试实体失败", e);
        }
    }

    /**
     * 清理测试数据
     */
    private void cleanTestData() {
        try {
            // 清理测试期间创建的数据
            // 根据实际需要实现清理逻辑
            entityManager.clear();
            log.debug("测试数据清理完成");
        } catch (Exception e) {
            log.warn("清理测试数据失败", e);
        }
    }

    // ==================== Mock测试 ====================

    /**
     * 测试Mock场景
     */
    @Test
    @Order(13)
    @DisplayName("测试Mock场景")
    void testMockScenarios() {
        logTestStart("testMockScenarios");

        try {
            // 1. 准备Mock数据
            {{EntityName}}VO mockVO = TestDataBuilder.create{{EntityName}}VO();
            when({{entityName}}Manager.getDetail(anyLong())).thenReturn(ResponseDTO.ok(mockVO));

            // 2. 执行测试
            ResponseDTO<{{EntityName}}VO> result = {{entityName}}ServiceWithMocks.getDetail(1L);

            // 3. 验证Mock调用
            verify({{entityName}}Manager, times(1)).getDetail(1L);

            // 4. 验证结果
            assertNotNull(result, "Mock测试结果不能为空");
            assertTrue(result.getOk(), "Mock测试应该成功");
            assertNotNull(result.getData(), "Mock测试数据不能为空");

            log.info("✅ Mock场景测试通过");

        } catch (Exception e) {
            log.error("❌ Mock场景测试失败", e);
            fail("Mock场景测试失败: " + e.getMessage());
        }

        logTestEnd("testMockScenarios");
    }
}