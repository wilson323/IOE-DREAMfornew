package net.lab1024.sa.consume.manager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import net.lab1024.sa.consume.dao.ConsumeMealCategoryDao;
import net.lab1024.sa.consume.domain.vo.ConsumeMealCategoryVO;
import net.lab1024.sa.common.entity.consume.ConsumeMealCategoryEntity;
import net.lab1024.sa.consume.exception.ConsumeMealCategoryException;

/**
 * ConsumeMealCategoryManager 单元测试
 *
 * @author IOE-DREAM Team
 * @version 1.0.0
 * @since 2025-12-22
 */
@DisplayName("ConsumeMealCategoryManager 单元测试")
class ConsumeMealCategoryManagerTest {

    @Mock
    private ConsumeMealCategoryDao consumeMealCategoryDao;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ConsumeMealCategoryManager consumeMealCategoryManager;

    private ConsumeMealCategoryEntity testCategory;
    private ConsumeMealCategoryEntity parentCategory;
    private ConsumeMealCategoryEntity childCategory;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testCategory = createTestCategory();
        parentCategory = createParentCategory();
        childCategory = createChildCategory();
        testTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);
    }

    private ConsumeMealCategoryEntity createTestCategory() {
        ConsumeMealCategoryEntity category = new ConsumeMealCategoryEntity();
        category.setCategoryId(1L);
        category.setCategoryCode("BREAKFAST");
        category.setCategoryName("早餐");
        category.setParentId(null);
        category.setCategoryLevel(1);
        category.setSortOrder(1);
        category.setCategoryIcon("breakfast-icon.png");
        category.setCategoryColor("#FF6B6B");
        category.setIsSystem(1);
        category.setCategoryStatus(1); // 启用
        category.setAvailableTimePeriods("[\"06:00-10:00\"]");
        category.setRemark("早餐分类");
        return category;
    }

    private ConsumeMealCategoryEntity createParentCategory() {
        ConsumeMealCategoryEntity category = new ConsumeMealCategoryEntity();
        category.setCategoryId(1L);
        category.setCategoryCode("MEAL");
        category.setCategoryName("用餐");
        category.setParentId(null);
        category.setCategoryLevel(1);
        category.setSortOrder(1);
        category.setIsSystem(1);
        category.setCategoryStatus(1);
        return category;
    }

    private ConsumeMealCategoryEntity createChildCategory() {
        ConsumeMealCategoryEntity category = new ConsumeMealCategoryEntity();
        category.setCategoryId(2L);
        category.setCategoryCode("BREAKFAST");
        category.setCategoryName("早餐");
        category.setParentId(1L);
        category.setCategoryLevel(2);
        category.setSortOrder(1);
        category.setIsSystem(0);
        category.setCategoryStatus(1);
        return category;
    }

    @Nested
    @DisplayName("分类编码唯一性验证测试")
    class CategoryCodeUniquenessTests {

        @Test
        @DisplayName("测试分类编码唯一 - 返回true")
        void testIsCategoryCodeUnique_UniqueCode_ReturnsTrue() {
            // Given
            String categoryCode = "UNIQUE_001";
            Long excludeId = 1L;
            when(consumeMealCategoryDao.countByCode(categoryCode, excludeId)).thenReturn(0);

            // When
            boolean result = consumeMealCategoryManager.isCategoryCodeUnique(categoryCode, excludeId);

            // Then
            assertTrue(result);
            verify(consumeMealCategoryDao).countByCode(categoryCode, excludeId);
        }

        @Test
        @DisplayName("测试分类编码重复 - 返回false")
        void testIsCategoryCodeUnique_DuplicateCode_ReturnsFalse() {
            // Given
            String categoryCode = "DUPLICATE_001";
            Long excludeId = 1L;
            when(consumeMealCategoryDao.countByCode(categoryCode, excludeId)).thenReturn(1);

            // When
            boolean result = consumeMealCategoryManager.isCategoryCodeUnique(categoryCode, excludeId);

            // Then
            assertFalse(result);
            verify(consumeMealCategoryDao).countByCode(categoryCode, excludeId);
        }

        @Test
        @DisplayName("测试分类编码为null - 返回false")
        void testIsCategoryCodeUnique_NullCode_ReturnsFalse() {
            // When
            boolean result = consumeMealCategoryManager.isCategoryCodeUnique(null, 1L);

            // Then
            assertFalse(result);
            verify(consumeMealCategoryDao, never()).countByCode(anyString(), any());
        }

        @Test
        @DisplayName("测试分类编码为空字符串 - 返回false")
        void testIsCategoryCodeUnique_EmptyCode_ReturnsFalse() {
            // When
            boolean result = consumeMealCategoryManager.isCategoryCodeUnique("   ", 1L);

            // Then
            assertFalse(result);
            verify(consumeMealCategoryDao, never()).countByCode(anyString(), any());
        }
    }

    @Nested
    @DisplayName("分类层级验证测试")
    class CategoryLevelValidationTests {

        @Test
        @DisplayName("测试层级验证 - 根分类，无父分类")
        void testValidateCategoryLevel_RootCategoryWithoutParent_ReturnsTrue() {
            // When
            boolean result = consumeMealCategoryManager.validateCategoryLevel(null, 1);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试层级验证 - 根分类，有父分类")
        void testValidateCategoryLevel_RootCategoryWithParent_ReturnsFalse() {
            // When
            boolean result = consumeMealCategoryManager.validateCategoryLevel(1L, 1);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试层级验证 - 子分类，父分类存在且层级正确")
        void testValidateCategoryLevel_ChildCategoryWithValidParent_ReturnsTrue() {
            // Given
            Long parentId = 1L;
            Integer categoryLevel = 2;
            ConsumeMealCategoryEntity parent = new ConsumeMealCategoryEntity();
            parent.setCategoryLevel(1);
            when(consumeMealCategoryDao.selectById(parentId)).thenReturn(parent);

            // When
            boolean result = consumeMealCategoryManager.validateCategoryLevel(parentId, categoryLevel);

            // Then
            assertTrue(result);
            verify(consumeMealCategoryDao).selectById(parentId);
        }

        @Test
        @DisplayName("测试层级验证 - 子分类，父分类不存在")
        void testValidateCategoryLevel_ChildCategoryWithInvalidParent_ReturnsFalse() {
            // Given
            Long parentId = 999L;
            Integer categoryLevel = 2;
            when(consumeMealCategoryDao.selectById(parentId)).thenReturn(null);

            // When
            boolean result = consumeMealCategoryManager.validateCategoryLevel(parentId, categoryLevel);

            // Then
            assertFalse(result);
            verify(consumeMealCategoryDao).selectById(parentId);
        }

        @Test
        @DisplayName("测试层级验证 - 子分类，层级关系错误")
        void testValidateCategoryLevel_ChildCategoryWithWrongLevel_ReturnsFalse() {
            // Given
            Long parentId = 1L;
            Integer categoryLevel = 3; // 错误，应该是2
            ConsumeMealCategoryEntity parent = new ConsumeMealCategoryEntity();
            parent.setCategoryLevel(1);
            when(consumeMealCategoryDao.selectById(parentId)).thenReturn(parent);

            // When
            boolean result = consumeMealCategoryManager.validateCategoryLevel(parentId, categoryLevel);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试层级验证 - 层级超出范围")
        void testValidateCategoryLevel_LevelOutOfRange_ReturnsFalse() {
            // When & Then
            assertFalse(consumeMealCategoryManager.validateCategoryLevel(null, null));
            assertFalse(consumeMealCategoryManager.validateCategoryLevel(null, 0));
            assertFalse(consumeMealCategoryManager.validateCategoryLevel(null, 4));
        }
    }

    @Nested
    @DisplayName("排序号测试")
    class SortOrderTests {

        @Test
        @DisplayName("测试获取下一个排序号 - 无现有分类")
        void testGetNextSortOrder_NoExistingCategories_ReturnsOne() {
            // Given
            Long parentId = null;
            when(consumeMealCategoryDao.getMaxSortOrder(parentId)).thenReturn(null);

            // When
            Integer result = consumeMealCategoryManager.getNextSortOrder(parentId);

            // Then
            assertEquals(Integer.valueOf(1), result);
            verify(consumeMealCategoryDao).getMaxSortOrder(parentId);
        }

        @Test
        @DisplayName("测试获取下一个排序号 - 有现有分类")
        void testGetNextSortOrder_HasExistingCategories_ReturnsNext() {
            // Given
            Long parentId = 1L;
            when(consumeMealCategoryDao.getMaxSortOrder(parentId)).thenReturn(5);

            // When
            Integer result = consumeMealCategoryManager.getNextSortOrder(parentId);

            // Then
            assertEquals(Integer.valueOf(6), result);
            verify(consumeMealCategoryDao).getMaxSortOrder(parentId);
        }

        @Test
        @DisplayName("测试获取下一个排序号 - 负数排序号")
        void testGetNextSortOrder_NegativeSortOrder_ReturnsOne() {
            // Given
            Long parentId = 1L;
            when(consumeMealCategoryDao.getMaxSortOrder(parentId)).thenReturn(-1);

            // When
            Integer result = consumeMealCategoryManager.getNextSortOrder(parentId);

            // Then
            assertEquals(Integer.valueOf(1), result);
        }
    }

    @Nested
    @DisplayName("分类删除检查测试")
    class DeleteCheckTests {

        @Test
        @DisplayName("检查分类可以删除 - 自定义分类，无子分类")
        void testCheckDeleteCategory_CustomCategoryNoChildren_CanDelete() {
            // Given
            Long categoryId = 1L;
            ConsumeMealCategoryEntity category = createTestCategory();
            category.setIsSystem(0); // 非系统分类
            when(consumeMealCategoryDao.selectById(categoryId)).thenReturn(category);
            when(consumeMealCategoryDao.countChildren(categoryId)).thenReturn(0);

            Map<String, Long> relatedRecords = new HashMap<>();
            relatedRecords.put("productCount", 0L);
            relatedRecords.put("consumeCount", 5L);
            when(consumeMealCategoryDao.countRelatedRecords(categoryId)).thenReturn(relatedRecords);

            // When
            Map<String, Object> result = consumeMealCategoryManager.checkDeleteCategory(categoryId);

            // Then
            assertTrue((Boolean) result.get("canDelete"));
            assertEquals(relatedRecords, result.get("relatedRecords"));
        }

        @Test
        @DisplayName("检查分类不可删除 - 系统分类")
        void testCheckDeleteCategory_SystemCategory_CannotDelete() {
            // Given
            Long categoryId = 1L;
            ConsumeMealCategoryEntity category = createTestCategory();
            category.setIsSystem(1); // 系统分类
            when(consumeMealCategoryDao.selectById(categoryId)).thenReturn(category);

            // When
            Map<String, Object> result = consumeMealCategoryManager.checkDeleteCategory(categoryId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("系统预设分类不能删除", result.get("reason"));
        }

        @Test
        @DisplayName("检查分类不可删除 - 有子分类")
        void testCheckDeleteCategory_HasChildren_CannotDelete() {
            // Given
            Long categoryId = 1L;
            ConsumeMealCategoryEntity category = createTestCategory();
            category.setIsSystem(0);
            when(consumeMealCategoryDao.selectById(categoryId)).thenReturn(category);
            when(consumeMealCategoryDao.countChildren(categoryId)).thenReturn(3);

            // When
            Map<String, Object> result = consumeMealCategoryManager.checkDeleteCategory(categoryId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("存在子分类，无法删除", result.get("reason"));
            assertEquals(3, result.get("childrenCount"));
        }

        @Test
        @DisplayName("检查分类不存在 - 不能删除")
        void testCheckDeleteCategory_CategoryNotExists_CannotDelete() {
            // Given
            Long categoryId = 999L;
            when(consumeMealCategoryDao.selectById(categoryId)).thenReturn(null);

            // When
            Map<String, Object> result = consumeMealCategoryManager.checkDeleteCategory(categoryId);

            // Then
            assertFalse((Boolean) result.get("canDelete"));
            assertEquals("分类不存在", result.get("reason"));
        }
    }

    @Nested
    @DisplayName("分类树构建测试")
    class CategoryTreeTests {

        @Test
        @DisplayName("测试构建分类树 - 正常结构")
        void testBuildCategoryTree_NormalStructure_ReturnsCorrectTree() {
            // Given
            List<ConsumeMealCategoryVO> categories = new ArrayList<>();

            // 根分类
            ConsumeMealCategoryVO root1 = new ConsumeMealCategoryVO();
            root1.setCategoryId(1L);
            root1.setCategoryName("用餐");
            root1.setParentId(0L);
            root1.setSortOrder(1);
            categories.add(root1);

            // 子分类
            ConsumeMealCategoryVO child1 = new ConsumeMealCategoryVO();
            child1.setCategoryId(2L);
            child1.setCategoryName("早餐");
            child1.setParentId(1L);
            child1.setSortOrder(1);
            categories.add(child1);

            ConsumeMealCategoryVO child2 = new ConsumeMealCategoryVO();
            child2.setCategoryId(3L);
            child2.setCategoryName("午餐");
            child2.setParentId(1L);
            child2.setSortOrder(2);
            categories.add(child2);

            // When
            List<ConsumeMealCategoryVO> result = consumeMealCategoryManager.buildCategoryTree(categories);

            // Then
            assertEquals(1, result.size());
            ConsumeMealCategoryVO root = result.get(0);
            assertEquals("用餐", root.getCategoryName());
            assertNotNull(root.getChildren());
            assertEquals(2, root.getChildren().size());
            assertEquals("早餐", root.getChildren().get(0).getCategoryName());
            assertEquals("午餐", root.getChildren().get(1).getCategoryName());
        }

        @Test
        @DisplayName("测试构建分类树 - 空列表")
        void testBuildCategoryTree_EmptyList_ReturnsEmpty() {
            // When
            List<ConsumeMealCategoryVO> result = consumeMealCategoryManager.buildCategoryTree(new ArrayList<>());

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("测试构建分类树 - null输入")
        void testBuildCategoryTree_NullInput_ReturnsEmpty() {
            // When
            List<ConsumeMealCategoryVO> result = consumeMealCategoryManager.buildCategoryTree(null);

            // Then
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("测试构建分类树 - 多层结构")
        void testBuildCategoryTree_MultipleLevels_ReturnsCorrectTree() {
            // Given
            List<ConsumeMealCategoryVO> categories = new ArrayList<>();

            // 根分类
            ConsumeMealCategoryVO root = new ConsumeMealCategoryVO();
            root.setCategoryId(1L);
            root.setCategoryName("用餐");
            root.setParentId(0L);
            root.setSortOrder(1);
            categories.add(root);

            // 一级子分类
            ConsumeMealCategoryVO level1 = new ConsumeMealCategoryVO();
            level1.setCategoryId(2L);
            level1.setCategoryName("早餐");
            level1.setParentId(1L);
            level1.setSortOrder(1);
            categories.add(level1);

            // 二级子分类
            ConsumeMealCategoryVO level2 = new ConsumeMealCategoryVO();
            level2.setCategoryId(3L);
            level2.setCategoryName("中式早餐");
            level2.setParentId(2L);
            level2.setSortOrder(1);
            categories.add(level2);

            // When
            List<ConsumeMealCategoryVO> result = consumeMealCategoryManager.buildCategoryTree(categories);

            // Then
            assertEquals(1, result.size());
            ConsumeMealCategoryVO rootCategory = result.get(0);
            assertEquals("用餐", rootCategory.getCategoryName());
            assertEquals(1, rootCategory.getChildren().size());

            ConsumeMealCategoryVO level1Category = rootCategory.getChildren().get(0);
            assertEquals("早餐", level1Category.getCategoryName());
            assertEquals(1, level1Category.getChildren().size());

            ConsumeMealCategoryVO level2Category = level1Category.getChildren().get(0);
            assertEquals("中式早餐", level2Category.getCategoryName());
        }
    }

    @Nested
    @DisplayName("分类可用时间检查测试")
    class AvailabilityTimeTests {

        @Test
        @DisplayName("测试分类可用 - 在时间段内")
        void testIsAvailableAtTime_WithinTimePeriod_ReturnsTrue() {
            // Given
            testCategory.setAvailableTimePeriods("[\"06:00-10:00\", \"18:00-22:00\"]");
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 8, 0, 0);

            // When - 使用真实的ObjectMapper解析JSON
            boolean result = consumeMealCategoryManager.isAvailableAtTime(testCategory, currentTime);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试分类不可用 - 在时间段外")
        void testIsAvailableAtTime_OutsideTimePeriod_ReturnsFalse() {
            // Given
            testCategory.setAvailableTimePeriods("[\"06:00-10:00\", \"18:00-22:00\"]");
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 15, 0, 0);

            // When - 使用真实的ObjectMapper解析JSON
            boolean result = consumeMealCategoryManager.isAvailableAtTime(testCategory, currentTime);

            // Then
            assertFalse(result);
        }

        @Test
        @DisplayName("测试分类可用 - 无时间限制")
        void testIsAvailableAtTime_NoTimeLimit_ReturnsTrue() {
            // Given
            testCategory.setAvailableTimePeriods("");
            LocalDateTime currentTime = LocalDateTime.of(2025, 12, 22, 15, 0, 0);

            // When
            boolean result = consumeMealCategoryManager.isAvailableAtTime(testCategory, currentTime);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("测试分类可用 - 时间边界值")
        void testIsAvailableAtTime_TimeBoundary_ReturnsTrue() {
            // Given
            testCategory.setAvailableTimePeriods("[\"06:00-10:00\"]");
            LocalDateTime startTime = LocalDateTime.of(2025, 12, 22, 6, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 12, 22, 10, 0, 0);

            // When & Then - 使用真实的ObjectMapper解析JSON
            assertTrue(consumeMealCategoryManager.isAvailableAtTime(testCategory, startTime));
            assertTrue(consumeMealCategoryManager.isAvailableAtTime(testCategory, endTime));
        }

        @Test
        @DisplayName("测试参数为null - 返回false")
        void testIsAvailableAtTime_NullParameters_ReturnsFalse() {
            // When & Then
            assertFalse(consumeMealCategoryManager.isAvailableAtTime(null, testTime));
            assertFalse(consumeMealCategoryManager.isAvailableAtTime(testCategory, null));
        }
    }

    @Nested
    @DisplayName("分类业务规则验证测试")
    class CategoryValidationTests {

        @Test
        @DisplayName("测试分类验证成功 - 根分类")
        void testValidateCategoryRules_ValidRootCategory_NoException() {
            // Given
            ConsumeMealCategoryEntity rootCategory = createTestCategory();
            rootCategory.setParentId(null);
            rootCategory.setCategoryLevel(1);
            when(consumeMealCategoryDao.countByCode(anyString(), any())).thenReturn(0);

            // When & Then
            assertDoesNotThrow(() -> consumeMealCategoryManager.validateCategoryRules(rootCategory));
            verify(consumeMealCategoryDao).countByCode(rootCategory.getCategoryCode(), rootCategory.getCategoryId());
        }

        @Test
        @DisplayName("测试分类验证成功 - 子分类")
        void testValidateCategoryRules_ValidChildCategory_NoException() {
            // Given
            ConsumeMealCategoryEntity parent = createParentCategory();
            ConsumeMealCategoryEntity child = createChildCategory();

            when(consumeMealCategoryDao.countByCode(anyString(), any())).thenReturn(0);
            when(consumeMealCategoryDao.selectById(child.getParentId())).thenReturn(parent);

            // When & Then
            assertDoesNotThrow(() -> consumeMealCategoryManager.validateCategoryRules(child));
            // selectById被调用了2次：一次在validateCategoryRules中，一次在validateCategoryLevel中
            verify(consumeMealCategoryDao, times(2)).selectById(child.getParentId());
            verify(consumeMealCategoryDao).countByCode(child.getCategoryCode(), child.getCategoryId());
        }

        @Test
        @DisplayName("测试分类验证 - 父分类不存在")
        void testValidateCategoryRules_ParentNotExists_ThrowsException() {
            // Given
            ConsumeMealCategoryEntity child = createChildCategory();
            when(consumeMealCategoryDao.selectById(child.getParentId())).thenReturn(null);

            // When & Then
            ConsumeMealCategoryException exception = assertThrows(
                ConsumeMealCategoryException.class,
                () -> consumeMealCategoryManager.validateCategoryRules(child)
            );
            assertTrue(exception.getMessage().contains("父分类不存在"));
        }

        @Test
        @DisplayName("测试分类验证 - 层级关系错误")
        void testValidateCategoryRules_InvalidLevel_ThrowsException() {
            // Given
            ConsumeMealCategoryEntity parent = createParentCategory();
            ConsumeMealCategoryEntity child = createChildCategory();
            child.setCategoryLevel(3); // 错误层级
            when(consumeMealCategoryDao.selectById(child.getParentId())).thenReturn(parent);

            // When & Then
            ConsumeMealCategoryException exception = assertThrows(
                ConsumeMealCategoryException.class,
                () -> consumeMealCategoryManager.validateCategoryRules(child)
            );
            assertTrue(exception.getMessage().contains("层级不能为"));
        }

        @Test
        @DisplayName("测试分类验证 - 编码重复")
        void testValidateCategoryRules_DuplicateCode_ThrowsException() {
            // Given
            ConsumeMealCategoryEntity category = createTestCategory();
            when(consumeMealCategoryDao.countByCode(anyString(), any())).thenReturn(1);

            // When & Then
            ConsumeMealCategoryException exception = assertThrows(
                ConsumeMealCategoryException.class,
                () -> consumeMealCategoryManager.validateCategoryRules(category)
            );
            assertTrue(exception.getMessage().contains("分类编码已存在"));
        }
    }

    @Nested
    @DisplayName("分类路径计算测试")
    class CategoryPathTests {

        @Test
        @DisplayName("测试计算分类路径 - 根分类")
        void testCalculateCategoryPath_RootCategory_ReturnsName() {
            // Given
            ConsumeMealCategoryEntity rootCategory = createTestCategory();
            rootCategory.setParentId(null);

            // When
            String result = consumeMealCategoryManager.calculateCategoryPath(rootCategory);

            // Then
            assertEquals("早餐", result);
        }

        @Test
        @DisplayName("测试计算分类路径 - 二级分类")
        void testCalculateCategoryPath_TwoLevelCategory_ReturnsPath() {
            // Given
            ConsumeMealCategoryEntity parent = createParentCategory();
            ConsumeMealCategoryEntity child = createChildCategory();

            when(consumeMealCategoryDao.selectById(child.getParentId())).thenReturn(parent);

            // When
            String result = consumeMealCategoryManager.calculateCategoryPath(child);

            // Then
            assertEquals("用餐 > 早餐", result);
            verify(consumeMealCategoryDao).selectById(child.getParentId());
        }

        @Test
        @DisplayName("测试计算分类路径 - 多级分类")
        void testCalculateCategoryPath_MultipleLevelCategory_ReturnsPath() {
            // Given
            ConsumeMealCategoryEntity root = createParentCategory();
            ConsumeMealCategoryEntity level1 = createChildCategory();
            level1.setCategoryName("早餐");

            ConsumeMealCategoryEntity level2 = new ConsumeMealCategoryEntity();
            level2.setCategoryId(3L);
            level2.setCategoryName("中式早餐");
            level2.setParentId(2L);
            level2.setCategoryLevel(3);

            when(consumeMealCategoryDao.selectById(2L)).thenReturn(level1);
            when(consumeMealCategoryDao.selectById(1L)).thenReturn(root);

            // When
            String result = consumeMealCategoryManager.calculateCategoryPath(level2);

            // Then
            assertEquals("用餐 > 早餐 > 中式早餐", result);
            verify(consumeMealCategoryDao).selectById(2L);
            verify(consumeMealCategoryDao).selectById(1L);
        }

        @Test
        @DisplayName("测试计算分类路径 - null分类")
        void testCalculateCategoryPath_NullCategory_ReturnsEmpty() {
            // When
            String result = consumeMealCategoryManager.calculateCategoryPath(null);

            // Then
            assertEquals("", result);
        }
    }

    @Nested
    @DisplayName("分类统计测试")
    class CategoryStatisticsTests {

        @Test
        @DisplayName("测试获取分类统计")
        void testGetCategoryStatistics_Success() {
            // Given
            String startDate = "2025-01-01";
            String endDate = "2025-01-31";

            Map<String, Object> basicStats = new HashMap<>();
            basicStats.put("totalUsage", 1000L);
            basicStats.put("totalAmount", new BigDecimal("50000.00"));

            List<ConsumeMealCategoryVO> categories = new ArrayList<>();
            ConsumeMealCategoryVO systemCategory = new ConsumeMealCategoryVO();
            systemCategory.setIsSystem(1);
            systemCategory.setCategoryId(1L);

            ConsumeMealCategoryVO customCategory = new ConsumeMealCategoryVO();
            customCategory.setIsSystem(0);
            customCategory.setCategoryId(2L);

            categories.add(systemCategory);
            categories.add(customCategory);

            when(consumeMealCategoryDao.getCategoryStatistics(startDate, endDate)).thenReturn(basicStats);
            when(consumeMealCategoryDao.selectAllEnabled()).thenReturn(categories);

            // When
            Map<String, Object> result = consumeMealCategoryManager.getCategoryStatistics(startDate, endDate);

            // Then
            assertEquals(1000L, result.get("totalUsage"));
            assertEquals(new BigDecimal("50000.00"), result.get("totalAmount"));
            assertEquals(2, result.get("totalCategories"));
            assertEquals(1, result.get("systemCategories"));
            assertEquals(1, result.get("customCategories"));
        }

        @Test
        @DisplayName("测试获取分类统计 - 无数据")
        void testGetCategoryStatistics_NoData() {
            // Given
            String startDate = "2025-01-01";
            String endDate = "2025-01-31";

            when(consumeMealCategoryDao.getCategoryStatistics(startDate, endDate)).thenReturn(null);
            when(consumeMealCategoryDao.selectAllEnabled()).thenReturn(new ArrayList<>());

            // When
            Map<String, Object> result = consumeMealCategoryManager.getCategoryStatistics(startDate, endDate);

            // Then
            assertNotNull(result);
            assertEquals(0, result.get("totalCategories"));
            assertEquals(0, result.get("systemCategories"));
            assertEquals(0, result.get("customCategories"));
        }
    }
}