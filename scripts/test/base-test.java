package {{package}}.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.SmartApplication;
import net.lab1024.sa.base.common.entity.BaseEntity;
import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartVerificationUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * {{EntityName}}Service测试类
 *
 * @author {{author}}
 * @date {{date}}
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = SmartApplication.class)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class {{EntityName}}ServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Mock
    private {{EntityName}}Dao {{entityName}}Dao;

    @Mock
    private {{EntityName}}Manager {{entityName}}Manager;

    @InjectMocks
    private {{EntityName}}ServiceImpl {{entityName}}Service;

    private ObjectMapper objectMapper = new ObjectMapper();

    private {{EntityName}}Entity testEntity;
    private {{EntityName}}AddForm addForm;
    private {{EntityName}}UpdateForm updateForm;

    @BeforeEach
    void setUp() {
        try {
            // 初始化测试数据
            this.initTestData();

            // 清理数据库
            this.cleanDatabase();

            // 插入测试数据
            this.insertTestData();

            log.info("测试环境初始化完成");

        } catch (Exception e) {
            log.error("测试环境初始化失败", e);
            throw e;
        }
    }

    @AfterEach
    void tearDown() {
        try {
            // 清理测试数据
            this.cleanDatabase();

            log.info("测试环境清理完成");

        } catch (Exception e) {
            log.error("测试环境清理失败", e);
        }
    }

    // ==================== 基础CRUD测试 ====================

    /**
     * 测试根据ID查询详情 - 成功
     */
    @Test
    void testGetById_Success() {
        try {
            // 准备测试数据
            Long testId = testEntity.getId();

            // Mock返回数据
            when({{entityName}}Dao.getById(testId)).thenReturn(testEntity);

            // 执行测试
            ResponseDTO<{{EntityName}}VO> result = {{entityName}}Service.getById(testId);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertNotNull(result.getData());
            assertEquals(testId, result.getData().getId());
            assertEquals(testEntity.getName(), result.getData().getName());

            // 验证方法调用
            verify({{entityName}}Dao).getById(testId);

            log.info("testGetById_Success 测试通过");

        } catch (Exception e) {
            log.error("testGetById_Success 测试失败", e);
            fail("testGetById_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试根据ID查询详情 - ID为空
     */
    @Test
    void testGetById_IdIsNull() {
        try {
            // 执行测试
            ResponseDTO<{{EntityName}}VO> result = {{entityName}}Service.getById(null);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.getOk());
            assertTrue(result.getMsg().contains("ID不能为空"));

            // 验证方法调用
            verify({{entityName}}Dao, never()).getById(any());

            log.info("testGetById_IdIsNull 测试通过");

        } catch (Exception e) {
            log.error("testGetById_IdIsNull 测试失败", e);
            fail("testGetById_IdIsNull 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试根据ID查询详情 - 数据不存在
     */
    @Test
    void testGetById_DataNotExists() {
        try {
            Long nonExistentId = 99999L;

            // Mock返回空
            when({{entityName}}Dao.getById(nonExistentId)).thenReturn(null);

            // 执行测试
            ResponseDTO<{{EntityName}}VO> result = {{entityName}}Service.getById(nonExistentId);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.getOk());
            assertTrue(result.getMsg().contains("数据不存在"));

            // 验证方法调用
            verify({{entityName}}Dao).getById(nonExistentId);

            log.info("testGetById_DataNotExists 测试通过");

        } catch (Exception e) {
            log.error("testGetById_DataNotExists 测试失败", e);
            fail("testGetById_DataNotExists 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试分页查询 - 成功
     */
    @Test
    void testGetPage_Success() {
        try {
            // 准备查询表单
            {{EntityName}}QueryForm queryForm = new {{EntityName}}QueryForm();
            queryForm.setPageNum(1);
            queryForm.setPageSize(10);

            // 准备测试数据
            List<{{EntityName}}VO> voList = Arrays.asList(
                TestDataBuilder.create{{EntityName}}VO(),
                TestDataBuilder.create{{EntityName}}VO()
            );

            // Mock返回数据
            when({{entityName}}Manager.getPage(any(), any())).thenReturn(voList);

            // 执行测试
            ResponseDTO<PageResult<{{EntityName}}VO>> result = {{entityName}}Service.getPage(queryForm);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertNotNull(result.getData());
            assertTrue(result.getData().getRows().size() > 0);

            // 验证方法调用
            verify({{entityName}}Manager).getPage(any(), eq(queryForm));

            log.info("testGetPage_Success 测试通过");

        } catch (Exception e) {
            log.error("testGetPage_Success 测试失败", e);
            fail("testGetPage_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试添加 - 成功
     */
    @Test
    void testAdd_Success() {
        try {
            // 准备测试数据
            addForm.setName("测试名称");
            addForm.setDescription("测试描述");

            // Mock返回数据
            when({{entityName}}Dao.insert(any())).thenReturn(1);

            // 执行测试
            ResponseDTO<Long> result = {{entityName}}Service.add(addForm);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertNotNull(result.getData());
            assertTrue(result.getData() > 0);

            // 验证方法调用
            verify({{entityName}}Dao).insert(any());

            log.info("testAdd_Success 测试通过");

        } catch (Exception e) {
            log.error("testAdd_Success 测试失败", e);
            fail("testAdd_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试添加 - 参数验证失败
     */
    @Test
    void testAdd_ValidationError() {
        try {
            // 准备无效数据
            addForm.setName(""); // 空名称

            // 执行测试
            ResponseDTO<Long> result = {{entityName}}Service.add(addForm);

            // 验证结果
            assertNotNull(result);
            assertFalse(result.getOk());
            assertTrue(result.getMsg().contains("名称不能为空"));

            // 验证方法调用
            verify({{entityName}}Dao, never()).insert(any());

            log.info("testAdd_ValidationError 测试通过");

        } catch (Exception e) {
            log.error("testAdd_ValidationError 测试失败", e);
            fail("testAdd_ValidationError 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试更新 - 成功
     */
    @Test
    void testUpdate_Success() {
        try {
            // 准备测试数据
            updateForm.setId(testEntity.getId());
            updateForm.setName("更新后的名称");
            updateForm.setDescription("更新后的描述");

            // Mock返回数据
            when({{entityName}}Dao.getById(testEntity.getId())).thenReturn(testEntity);
            when({{entityName}}Dao.updateById(any())).thenReturn(1);

            // 执行测试
            ResponseDTO<Boolean> result = {{entityName}}Service.update(updateForm);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertTrue(result.getData());

            // 验证方法调用
            verify({{entityName}}Dao).getById(testEntity.getId());
            verify({{entityName}}Dao).updateById(any());

            log.info("testUpdate_Success 测试通过");

        } catch (Exception e) {
            log.error("testUpdate_Success 测试失败", e);
            fail("testUpdate_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试删除 - 成功
     */
    @Test
    void testDelete_Success() {
        try {
            // Mock返回数据
            when({{entityName}}Dao.getById(testEntity.getId())).thenReturn(testEntity);
            when({{entityName}}Dao.deleteById(testEntity.getId())).thenReturn(1);

            // 执行测试
            ResponseDTO<Boolean> result = {{entityName}}Service.delete(testEntity.getId());

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertTrue(result.getData());

            // 验证方法调用
            verify({{entityName}}Dao).getById(testEntity.getId());
            verify({{entityName}}Dao).deleteById(testEntity.getId());

            log.info("testDelete_Success 测试通过");

        } catch (Exception e) {
            log.error("testDelete_Success 测试失败", e);
            fail("testDelete_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试批量删除 - 成功
     */
    @Test
    void testBatchDelete_Success() {
        try {
            List<Long> idList = Arrays.asList(1L, 2L, 3L);

            // Mock返回数据
            when({{entityName}}Dao.getById(any())).thenReturn(testEntity);
            when({{entityName}}Dao.deleteById(any())).thenReturn(1);

            // 执行测试
            ResponseDTO<Boolean> result = {{entityName}}Service.batchDelete(idList);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertTrue(result.getData());

            // 验证方法调用次数
            verify({{entityName}}Dao, times(idList.size())).getById(any());
            verify({{entityName}}Dao, times(idList.size())).deleteById(any());

            log.info("testBatchDelete_Success 测试通过");

        } catch (Exception e) {
            log.error("testBatchDelete_Success 测试失败", e);
            fail("testBatch_Delete_Success 测试失败: " + e.getMessage());
        }
    }

    // ==================== 业务方法测试 ====================

    /**
     * 测试查询完整详情 - 成功
     */
    @Test
    void testGetDetail_Success() {
        try {
            // Mock返回数据
            {{EntityName}}DetailVO detailVO = TestDataBuilder.create{{EntityName}}DetailVO();
            when({{entityName}}Dao.getById(testEntity.getId())).thenReturn(testEntity);
            when({{entityName}}Manager.buildDetailVO(testEntity)).thenReturn(detailVO);

            // 执行测试
            ResponseDTO<{{EntityName}}DetailVO> result = {{entityName}}Service.getDetail(testEntity.getId());

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertNotNull(result.getData());

            // 验证方法调用
            verify({{entityName}}Dao).getById(testEntity.getId());
            verify({{entityName}}Manager).buildDetailVO(testEntity);

            log.info("testGetDetail_Success 测试通过");

        } catch (Exception e) {
            log.error("testGetDetail_Success 测试失败", e);
            fail("testGetDetail_Success 测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试更新状态 - 成功
     */
    @Test
    void testUpdateStatus_Success() {
        try {
            Integer newStatus = 1;

            // Mock返回数据
            when({{entityName}}Dao.getById(testEntity.getId())).thenReturn(testEntity);
            when({{entityName}}Dao.updateById(any())).thenReturn(1);

            // 执行测试
            ResponseDTO<Boolean> result = {{entityName}}Service.updateStatus(testEntity.getId(), newStatus);

            // 验证结果
            assertNotNull(result);
            assertTrue(result.getOk());
            assertTrue(result.getData());

            // 验证状态更新
            assertEquals(newStatus, testEntity.getStatus());

            // 验证方法调用
            verify({{entityName}}Dao).getById(testEntity.getId());
            verify({{entityName}}Dao).updateById(testEntity);

            log.info("testUpdateStatus_Success 测试通过");

        } catch (Exception e) {
            log.error("testUpdateStatus_Success 测试失败", e);
            fail("testUpdateStatus_Success 测试失败: " + e.getMessage());
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 初始化测试数据
     */
    private void initTestData() {
        testEntity = TestDataBuilder.create{{EntityName}}Entity();
        addForm = TestDataBuilder.create{{EntityName}}AddForm();
        updateForm = TestDataBuilder.create{{EntityName}}UpdateForm();
    }

    /**
     * 清理数据库
     */
    private void cleanDatabase() {
        try {
            entityManager.createQuery("DELETE FROM {{EntityName}}Entity").executeUpdate();
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            log.warn("清理数据库失败", e);
        }
    }

    /**
     * 插入测试数据
     */
    private void insertTestData() {
        try {
            entityManager.persist(testEntity);
            entityManager.flush();
            entityManager.clear();
        } catch (Exception e) {
            log.warn("插入测试数据失败", e);
        }
    }
}