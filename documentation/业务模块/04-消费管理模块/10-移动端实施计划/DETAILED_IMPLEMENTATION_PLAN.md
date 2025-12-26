# 消费管理模块 - 移动端完整实施计划

> **版本**: v1.0.0
> **创建日期**: 2025-12-24
> **预计工期**: 4-6周
> **优先级**: P0/P1级功能

---

## 📋 总体计划

### 时间线概览

```
Week 1-2: 补贴查询模块 (P1)
Week 2-3: 卡片管理模块 (P1)
Week 3-4: 充值功能完善 (P1)
Week 4-5: 退款功能 (P1)
Week 5-6: 在线订餐模块 (P1)
Week 6+:  增强功能 (P2)
```

---

## 🎯 第一阶段：补贴查询模块

### 目标
实现完整的补贴查询功能，包括余额查询、发放记录、使用明细

### 工作量
**总计**: 5-7个工作日

### 任务分解

#### 1.1 后端API开发 (2-3天)

##### 1.1.1 创建补贴相关实体和VO

**文件位置**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/domain/vo/`

```java
// ConsumeSubsidyBalanceVO.java - 补贴余额VO
@Data
@Schema(description = "补贴余额信息")
public class ConsumeSubsidyBalanceVO {
    @Schema(description = "总补贴余额")
    private BigDecimal totalBalance;

    @Schema(description = "餐补余额")
    private BigDecimal mealSubsidyBalance;

    @Schema(description = "交通补贴余额")
    private BigDecimal trafficSubsidyBalance;

    @Schema(description = "通用补贴余额")
    private BigDecimal generalSubsidyBalance;

    @Schema(description = "本月发放总额")
    private BigDecimal monthlyGrantAmount;

    @Schema(description = "本月使用总额")
    private BigDecimal monthlyUsedAmount;
}

// ConsumeSubsidyRecordVO.java - 补贴记录VO
@Data
@Schema(description = "补贴记录")
public class ConsumeSubsidyRecordVO {
    @Schema(description = "补贴ID")
    private Long subsidyId;

    @Schema(description = "补贴类型")
    private String subsidyTypeName;

    @Schema(description = "补贴金额")
    private BigDecimal amount;

    @Schema(description = "发放时间")
    private LocalDateTime grantTime;

    @Schema(description = "有效期开始")
    private LocalDateTime validStartTime;

    @Schema(description = "有效期结束")
    private LocalDateTime validEndTime;

    @Schema(description = "使用状态")
    private Integer useStatus;

    @Schema(description = "已使用金额")
    private BigDecimal usedAmount;

    @Schema(description = "剩余金额")
    private BigDecimal remainAmount;
}

// ConsumeSubsidyUsageVO.java - 补贴使用明细VO
@Data
@Schema(description = "补贴使用明细")
public class ConsumeSubsidyUsageVO {
    @Schema(description = "使用ID")
    private Long usageId;

    @Schema(description = "消费时间")
    private LocalDateTime consumeTime;

    @Schema(description = "消费地点")
    private String consumePlace;

    @Schema(description = "使用的补贴类型")
    private String subsidyTypeName;

    @Schema(description = "使用金额")
    private BigDecimal usageAmount;

    @Schema(description = "对应消费流水号")
    private String transactionNo;
}
```

##### 1.1.2 创建补贴Service接口和实现

**文件位置**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/service/`

```java
// ConsumeSubsidyService.java
public interface ConsumeSubsidyService {

    /**
     * 获取用户补贴余额
     */
    ConsumeSubsidyBalanceVO getSubsidyBalance(Long userId);

    /**
     * 获取用户补贴记录列表
     */
    PageResult<ConsumeSubsidyRecordVO> getSubsidyRecords(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取补贴详情
     */
    ConsumeSubsidyRecordVO getSubsidyDetail(Long subsidyId);

    /**
     * 获取补贴使用明细
     */
    PageResult<ConsumeSubsidyUsageVO> getSubsidyUsage(Long userId, Long subsidyId, Integer pageNum, Integer pageSize);
}

// ConsumeSubsidyServiceImpl.java
@Slf4j
@Service
public class ConsumeSubsidyServiceImpl implements ConsumeSubsidyService {

    @Resource
    private ConsumeAccountManager consumeAccountManager;

    @Resource
    private ConsumeSubsidyDao consumeSubsidyDao;

    @Override
    public ConsumeSubsidyBalanceVO getSubsidyBalance(Long userId) {
        log.info("[补贴服务] 查询补贴余额: userId={}", userId);

        // 查询账户信息
        ConsumeAccountEntity account = consumeAccountManager.getByUserId(userId);
        if (account == null) {
            throw new BusinessException("账户不存在");
        }

        // 构建余额信息
        ConsumeSubsidyBalanceVO balanceVO = new ConsumeSubsidyBalanceVO();
        balanceVO.setTotalBalance(account.getSubsidyBalance());
        balanceVO.setMealSubsidyBalance(account.getMealSubsidyBalance());
        balanceVO.setTrafficSubsidyBalance(account.getTrafficSubsidyBalance());
        balanceVO.setGeneralSubsidyBalance(account.getGeneralSubsidyBalance());

        // 查询本月统计
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        BigDecimal monthlyGrant = consumeSubsidyDao.sumGrantAmount(userId, monthStart);
        BigDecimal monthlyUsed = consumeSubsidyDao.sumUsedAmount(userId, monthStart);

        balanceVO.setMonthlyGrantAmount(monthingGrant != null ? monthlyGrant : BigDecimal.ZERO);
        balanceVO.setMonthlyUsedAmount(monthlyUsed != null ? monthlyUsed : BigDecimal.ZERO);

        log.info("[补贴服务] 查询补贴余额成功: userId={}, totalBalance={}", userId, balanceVO.getTotalBalance());
        return balanceVO;
    }

    @Override
    public PageResult<ConsumeSubsidyRecordVO> getSubsidyRecords(Long userId, Integer pageNum, Integer pageSize) {
        log.info("[补贴服务] 查询补贴记录: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);

        // 分页查询
        LambdaQueryWrapper<ConsumeSubsidyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConsumeSubsidyEntity::getUserId, userId)
                   .orderByDesc(ConsumeSubsidyEntity::getGrantTime);

        Page<ConsumeSubsidyEntity> page = consumeSubsidyDao.selectPage(
            new Page<>(pageNum, pageSize), queryWrapper
        );

        // 转换VO
        List<ConsumeSubsidyRecordVO> voList = page.getRecords().stream()
            .map(this::convertToRecordVO)
            .collect(Collectors.toList());

        return PageResult.of(voList, page.getTotal(), pageNum, pageSize);
    }

    private ConsumeSubsidyRecordVO convertToRecordVO(ConsumeSubsidyEntity entity) {
        ConsumeSubsidyRecordVO vo = new ConsumeSubsidyRecordVO();
        vo.setSubsidyId(entity.getSubsidyId());
        vo.setSubsidyTypeName(entity.getSubsidyTypeName());
        vo.setAmount(entity.getAmount());
        vo.setGrantTime(entity.getGrantTime());
        vo.setValidStartTime(entity.getValidStartTime());
        vo.setValidEndTime(entity.getValidEndTime());
        vo.setUseStatus(entity.getUseStatus());
        vo.setUsedAmount(entity.getUsedAmount());
        vo.setRemainAmount(entity.getAmount().subtract(entity.getUsedAmount()));
        return vo;
    }

    // ... 其他方法实现
}
```

##### 1.1.3 创建移动端补贴Controller

**文件位置**: `microservices/ioedream-consume-service/src/main/java/net/lab1024/sa/consume/controller/ConsumeSubsidyMobileController.java`

```java
@RestController
@RequestMapping("/api/v1/consume/mobile/subsidy")
@Tag(name = "移动端补贴管理", description = "移动端补贴查询接口")
@Slf4j
public class ConsumeSubsidyMobileController {

    @Resource
    private ConsumeSubsidyService consumeSubsidyService;

    @Operation(summary = "获取补贴余额", description = "获取用户各类补贴余额")
    @GetMapping("/balance/{userId}")
    public ResponseDTO<ConsumeSubsidyBalanceVO> getSubsidyBalance(@PathVariable Long userId) {
        log.info("[移动端补贴] 查询补贴余额: userId={}", userId);
        ConsumeSubsidyBalanceVO result = consumeSubsidyService.getSubsidyBalance(userId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取补贴记录", description = "分页获取用户补贴发放记录")
    @GetMapping("/records/{userId}")
    public ResponseDTO<PageResult<ConsumeSubsidyRecordVO>> getSubsidyRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端补贴] 查询补贴记录: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        PageResult<ConsumeSubsidyRecordVO> result = consumeSubsidyService.getSubsidyRecords(userId, pageNum, pageSize);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取补贴详情", description = "获取单条补贴记录详情")
    @GetMapping("/detail/{subsidyId}")
    public ResponseDTO<ConsumeSubsidyRecordVO> getSubsidyDetail(@PathVariable Long subsidyId) {
        log.info("[移动端补贴] 查询补贴详情: subsidyId={}", subsidyId);
        ConsumeSubsidyRecordVO result = consumeSubsidyService.getSubsidyDetail(subsidyId);
        return ResponseDTO.ok(result);
    }

    @Operation(summary = "获取补贴使用明细", description = "分页获取补贴使用明细")
    @GetMapping("/usage/{userId}")
    public ResponseDTO<PageResult<ConsumeSubsidyUsageVO>> getSubsidyUsage(
            @PathVariable Long userId,
            @RequestParam(required = false) Long subsidyId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        log.info("[移动端补贴] 查询补贴使用明细: userId={}, subsidyId={}", userId, subsidyId);
        PageResult<ConsumeSubsidyUsageVO> result = consumeSubsidyService.getSubsidyUsage(userId, subsidyId, pageNum, pageSize);
        return ResponseDTO.ok(result);
    }
}
```

#### 1.2 前端API封装 (0.5天)

**文件位置**: `smart-app/src/api/business/consume/subsidy-api.js`

```javascript
/**
 * 补贴管理API接口 - 移动端
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-12-24
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { getRequest } from '@/lib/smart-request'

// 补贴管理相关接口
export const subsidyApi = {
  /**
   * 获取补贴余额
   * @param {Number} userId 用户ID
   * @returns {Promise}
   */
  getSubsidyBalance: (userId) => getRequest(`/api/v1/consume/mobile/subsidy/balance/${userId}`),

  /**
   * 获取补贴记录
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getSubsidyRecords: (userId, params) => getRequest(`/api/v1/consume/mobile/subsidy/records/${userId}`, params),

  /**
   * 获取补贴详情
   * @param {Number} subsidyId 补贴ID
   * @returns {Promise}
   */
  getSubsidyDetail: (subsidyId) => getRequest(`/api/v1/consume/mobile/subsidy/detail/${subsidyId}`),

  /**
   * 获取补贴使用明细
   * @param {Number} userId 用户ID
   * @param {Object} params 查询参数
   * @returns {Promise}
   */
  getSubsidyUsage: (userId, params) => getRequest(`/api/v1/consume/mobile/subsidy/usage/${userId}`, params)
}

export default subsidyApi
```

#### 1.3 移动端页面开发 (2-3天)

**文件位置**: `smart-app/src/pages/consume/subsidy.vue`

```vue
<template>
  <view class="subsidy-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">我的补贴</view>
      <view class="nav-right"></view>
    </view>

    <!-- 补贴余额卡片 -->
    <view class="subsidy-balance-card">
      <view class="balance-title">补贴总额</view>
      <view class="balance-amount">
        <text class="currency">¥</text>
        <text class="amount">{{ formatAmount(balanceInfo.totalBalance) }}</text>
      </view>

      <!-- 分类余额 -->
      <view class="subsidy-categories">
        <view class="category-item">
          <text class="category-label">餐补</text>
          <text class="category-value">¥{{ formatAmount(balanceInfo.mealSubsidyBalance) }}</text>
        </view>
        <view class="category-item">
          <text class="category-label">交通补贴</text>
          <text class="category-value">¥{{ formatAmount(balanceInfo.trafficSubsidyBalance) }}</text>
        </view>
        <view class="category-item">
          <text class="category-label">通用补贴</text>
          <text class="category-value">¥{{ formatAmount(balanceInfo.generalSubsidyBalance) }}</text>
        </view>
      </view>

      <!-- 本月统计 -->
      <view class="monthly-stats">
        <view class="stat-item">
          <text class="stat-label">本月发放</text>
          <text class="stat-value grant">+¥{{ formatAmount(balanceInfo.monthlyGrantAmount) }}</text>
        </view>
        <view class="stat-item">
          <text class="stat-label">本月使用</text>
          <text class="stat-value used">-¥{{ formatAmount(balanceInfo.monthlyUsedAmount) }}</text>
        </view>
      </view>
    </view>

    <!-- Tab切换 -->
    <view class="tab-container">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text>发放记录</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'usage' }"
        @click="switchTab('usage')"
      >
        <text>使用明细</text>
      </view>
    </view>

    <!-- 发放记录列表 -->
    <view class="records-list" v-if="activeTab === 'records'">
      <view
        class="record-item"
        v-for="(record, index) in subsidyRecords"
        :key="index"
        @click="viewRecordDetail(record.subsidyId)"
      >
        <view class="record-header">
          <text class="record-type">{{ record.subsidyTypeName }}</text>
          <view
            class="record-status"
            :class="getStatusClass(record.useStatus)"
          >
            <text>{{ getStatusText(record.useStatus) }}</text>
          </view>
        </view>

        <view class="record-body">
          <view class="record-amount">
            <text class="amount-label">发放金额</text>
            <text class="amount-value grant">+¥{{ formatAmount(record.amount) }}</text>
          </view>

          <view class="record-progress">
            <text class="progress-label">已使用 ¥{{ formatAmount(record.usedAmount) }}</text>
            <text class="progress-label">剩余 ¥{{ formatAmount(record.remainAmount) }}</text>
          </view>

          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: getUsagePercent(record) + '%' }"
            ></view>
          </view>
        </view>

        <view class="record-footer">
          <text class="record-time">发放时间: {{ formatDateTime(record.grantTime) }}</text>
          <text class="record-valid">有效期至: {{ formatDate(record.validEndTime) }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" @click="loadMoreRecords" v-if="hasMoreRecords">
        <text>加载更多</text>
      </view>

      <view class="no-data" v-if="subsidyRecords.length === 0">
        <text>暂无补贴记录</text>
      </view>
    </view>

    <!-- 使用明细列表 -->
    <view class="usage-list" v-if="activeTab === 'usage'">
      <view
        class="usage-item"
        v-for="(usage, index) in subsidyUsage"
        :key="index"
      >
        <view class="usage-icon">🍽️</view>
        <view class="usage-info">
          <view class="usage-header">
            <text class="usage-type">{{ usage.subsidyTypeName }}</text>
            <text class="usage-amount">-¥{{ formatAmount(usage.usageAmount) }}</text>
          </view>
          <view class="usage-detail">
            <text class="usage-place">{{ usage.consumePlace }}</text>
            <text class="usage-time">{{ formatDateTime(usage.consumeTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" @click="loadMoreUsage" v-if="hasMoreUsage">
        <text>加载更多</text>
      </view>

      <view class="no-data" v-if="subsidyUsage.length === 0">
        <text>暂无使用记录</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { subsidyApi } from '@/api/business/consume/subsidy-api.js'

// 响应式数据
const userStore = useUserStore()
const activeTab = ref('records')
const balanceInfo = ref({
  totalBalance: 0,
  mealSubsidyBalance: 0,
  trafficSubsidyBalance: 0,
  generalSubsidyBalance: 0,
  monthlyGrantAmount: 0,
  monthlyUsedAmount: 0
})
const subsidyRecords = ref([])
const subsidyUsage = ref([])
const currentRecordPage = ref(1)
const currentUsagePage = ref(1)
const hasMoreRecords = ref(false)
const hasMoreUsage = ref(false)

// 页面生命周期
onMounted(() => {
  loadBalanceInfo()
  loadRecords()
})

// 方法实现
const loadBalanceInfo = async () => {
  try {
    const userId = userStore.employeeId
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const result = await subsidyApi.getSubsidyBalance(userId)
    if (result.success && result.data) {
      Object.assign(balanceInfo.value, result.data)
    }
  } catch (error) {
    console.error('加载补贴余额失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

const loadRecords = async () => {
  try {
    const userId = userStore.employeeId
    const result = await subsidyApi.getSubsidyRecords(userId, {
      pageNum: currentRecordPage.value,
      pageSize: 20
    })

    if (result.success && result.data) {
      if (currentRecordPage.value === 1) {
        subsidyRecords.value = result.data.list || []
      } else {
        subsidyRecords.value.push(...(result.data.list || []))
      }
      hasMoreRecords.value = (result.data.list || []).length >= 20
    }
  } catch (error) {
    console.error('加载补贴记录失败:', error)
  }
}

const loadUsage = async () => {
  try {
    const userId = userStore.employeeId
    const result = await subsidyApi.getSubsidyUsage(userId, {
      pageNum: currentUsagePage.value,
      pageSize: 20
    })

    if (result.success && result.data) {
      if (currentUsagePage.value === 1) {
        subsidyUsage.value = result.data.list || []
      } else {
        subsidyUsage.value.push(...(result.data.list || []))
      }
      hasMoreUsage.value = (result.data.list || []).length >= 20
    }
  } catch (error) {
    console.error('加载使用明细失败:', error)
  }
}

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'usage' && subsidyUsage.value.length === 0) {
    currentUsagePage.value = 1
    loadUsage()
  }
}

const loadMoreRecords = () => {
  currentRecordPage.value++
  loadRecords()
}

const loadMoreUsage = () => {
  currentUsagePage.value++
  loadUsage()
}

const viewRecordDetail = (subsidyId) => {
  uni.navigateTo({
    url: `/pages/consume/subsidy-detail?id=${subsidyId}`
  })
}

const getUsagePercent = (record) => {
  if (!record.amount || record.amount === 0) return 0
  return (record.usedAmount / record.amount * 100).toFixed(2)
}

const getStatusClass = (status) => {
  const statusMap = {
    1: 'unused',
    2: 'partial',
    3: 'used',
    4: 'expired'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    1: '未使用',
    2: '部分使用',
    3: '已使用',
    4: '已过期'
  }
  return statusMap[status] || '未知'
}

const formatAmount = (amount) => {
  if (!amount) return '0.00'
  return Number(amount).toFixed(2)
}

const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const formatDate = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.subsidy-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }
}

.subsidy-balance-card {
  margin: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 25px 20px;
  color: #fff;

  .balance-title {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 8px;
  }

  .balance-amount {
    margin-bottom: 20px;

    .currency {
      font-size: 20px;
      margin-right: 4px;
    }

    .amount {
      font-size: 36px;
      font-weight: bold;
    }
  }

  .subsidy-categories {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;

    .category-item {
      flex: 1;
      text-align: center;

      .category-label {
        display: block;
        font-size: 12px;
        opacity: 0.8;
        margin-bottom: 4px;
      }

      .category-value {
        display: block;
        font-size: 16px;
        font-weight: 600;
      }
    }
  }

  .monthly-stats {
    display: flex;
    gap: 20px;
    padding-top: 15px;
    border-top: 1px solid rgba(255, 255, 255, 0.3);

    .stat-item {
      flex: 1;

      .stat-label {
        display: block;
        font-size: 12px;
        opacity: 0.8;
        margin-bottom: 4px;
      }

      .stat-value {
        display: block;
        font-size: 16px;
        font-weight: 600;

        &.grant {
          color: #67e8f9;
        }

        &.used {
          color: #fca5a5;
        }
      }
    }
  }
}

.tab-container {
  display: flex;
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 8px;
  padding: 5px;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px 0;
    font-size: 14px;
    color: #666;
    border-radius: 6px;
    transition: all 0.3s;

    &.active {
      background-color: #667eea;
      color: #fff;
      font-weight: 600;
    }
  }
}

.records-list, .usage-list {
  padding: 0 15px 15px;
}

.record-item, .usage-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 10px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;

  .record-type {
    font-size: 16px;
    font-weight: 600;
    color: #333;
  }

  .record-status {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;

    &.unused {
      background-color: #e0f2fe;
      color: #0284c7;
    }

    &.partial {
      background-color: #fef3c7;
      color: #d97706;
    }

    &.used {
      background-color: #dcfce7;
      color: #16a34a;
    }

    &.expired {
      background-color: #fee2e2;
      color: #dc2626;
    }
  }
}

.record-body {
  margin-bottom: 12px;

  .record-amount {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .amount-label {
      font-size: 14px;
      color: #666;
    }

    .amount-value {
      font-size: 18px;
      font-weight: 600;

      &.grant {
        color: #16a34a;
      }
    }
  }

  .record-progress {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;

    .progress-label {
      font-size: 12px;
      color: #999;
    }
  }

  .progress-bar {
    height: 6px;
    background-color: #e5e7eb;
    border-radius: 3px;
    overflow: hidden;

    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
      transition: width 0.3s;
    }
  }
}

.record-footer {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.usage-item {
  display: flex;
  gap: 12px;

  .usage-icon {
    font-size: 24px;
  }

  .usage-info {
    flex: 1;

    .usage-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 4px;

      .usage-type {
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .usage-amount {
        font-size: 16px;
        font-weight: 600;
        color: #dc2626;
      }
    }

    .usage-detail {
      display: flex;
      justify-content: space-between;

      .usage-place, .usage-time {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

.load-more {
  text-align: center;
  padding: 15px;
  color: #1890ff;
  font-size: 14px;
}

.no-data {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 14px;
}
</style>
```

#### 1.4 测试验收 (0.5天)

##### 单元测试

```java
@SpringBootTest
@Slf4j
class ConsumeSubsidyServiceTest {

    @Resource
    private ConsumeSubsidyService consumeSubsidyService;

    @Test
    void testGetSubsidyBalance() {
        Long userId = 1L;
        ConsumeSubsidyBalanceVO balance = consumeSubsidyService.getSubsidyBalance(userId);

        assertNotNull(balance);
        assertNotNull(balance.getTotalBalance());
        log.info("[补贴测试] 查询补贴余额成功: {}", balance);
    }

    @Test
    void testGetSubsidyRecords() {
        Long userId = 1L;
        PageResult<ConsumeSubsidyRecordVO> result = consumeSubsidyService.getSubsidyRecords(userId, 1, 20);

        assertNotNull(result);
        assertNotNull(result.getList());
        log.info("[补贴测试] 查询补贴记录成功: {}", result);
    }
}
```

##### 集成测试

```javascript
// smart-app/src/api/__tests__/subsidy-api.test.js
import { subsidyApi } from '@/api/business/consume/subsidy-api.js'

describe('补贴API测试', () => {
  test('获取补贴余额', async () => {
    const result = await subsidyApi.getSubsidyBalance(1)
    expect(result.success).toBe(true)
    expect(result.data).toHaveProperty('totalBalance')
  })

  test('获取补贴记录', async () => {
    const result = await subsidyApi.getSubsidyRecords(1, { pageNum: 1, pageSize: 20 })
    expect(result.success).toBe(true)
    expect(result.data).toHaveProperty('list')
  })
})
```

---

## 📝 开发注意事项

### 1. 代码规范

- ✅ 使用@Slf4j注解，禁止使用LoggerFactory
- ✅ 使用@Resource注解，禁止使用@Autowired
- ✅ 遵循四层架构：Controller→Service→Manager→DAO
- ✅ 所有API返回ResponseDTO包装
- ✅ Service层返回业务对象，不使用ResponseDTO
- ✅ 日志格式：[模块名] 操作描述: 参数1={}, 参数2={}

### 2. 数据验证

```java
// ✅ 参数校验
@NotNull(message = "用户ID不能为空")
private Long userId;

@NotBlank(message = "补贴类型不能为空")
private String subsidyType;

@NotNull(message = "金额不能为空")
@Min(value = 1, message = "金额必须大于0")
private BigDecimal amount;
```

### 3. 异常处理

```java
// ✅ 业务异常
try {
    // 业务逻辑
} catch (BusinessException e) {
    log.warn("[补贴服务] 业务异常: userId={}, error={}", userId, e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("[补贴服务] 系统异常: userId={}, error={}", userId, e.getMessage(), e);
    throw new SystemException("SUBSIDY_ERROR", "补贴查询失败", e);
}
```

### 4. 性能优化

```java
// ✅ 使用缓存
@Cacheable(value = "subsidy:balance", key = "#userId", unless = "#result == null")
public ConsumeSubsidyBalanceVO getSubsidyBalance(Long userId) {
    // ...
}

// ✅ 分页查询
Page<ConsumeSubsidyEntity> page = consumeSubsidyDao.selectPage(
    new Page<>(pageNum, pageSize), queryWrapper
);
```

---

**📝 文档维护**: IOE-DREAM架构团队 | 2025-12-24
