<template>
  <view class="refund-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">退款管理</view>
      <view class="nav-right">
        <text class="help-btn" @click="showHelp">?</text>
      </view>
    </view>

    <!-- 退款统计 -->
    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-icon">💰</text>
        <view class="stat-info">
          <text class="stat-label">已退款</text>
          <text class="stat-value">¥{{ formatAmount(statistics.refundedAmount || 0) }}</text>
        </view>
      </view>
      <view class="stat-card">
        <text class="stat-icon">⏳</text>
        <view class="stat-info">
          <text class="stat-label">退款中</text>
          <text class="stat-value">¥{{ formatAmount(statistics.pendingAmount || 0) }}</text>
        </view>
      </view>
      <view class="stat-card">
        <text class="stat-icon">📊</text>
        <view class="stat-info">
          <text class="stat-label">退款次数</text>
          <text class="stat-value">{{ statistics.refundCount || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 标签页切换 -->
    <view class="tab-container">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'apply' }"
        @click="switchTab('apply')"
      >
        <text class="tab-text">申请退款</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">退款记录</text>
      </view>
    </view>

    <!-- 申请退款 -->
    <view class="apply-section" v-if="activeTab === 'apply'">
      <!-- 选择消费记录 -->
      <view class="select-record-section">
        <text class="section-title">选择消费记录</text>
        <view class="record-list">
          <view
            class="record-item"
            v-for="(record, index) in availableRecords"
            :key="index"
            :class="{ selected: selectedRecord && selectedRecord.consumeId === record.consumeId }"
            @click="selectRecord(record)"
          >
            <view class="record-left">
              <text class="record-merchant">{{ record.merchantName || record.consumeLocation || '消费' }}</text>
              <text class="record-time">{{ formatDateTime(record.consumeTime) }}</text>
            </view>
            <view class="record-right">
              <text class="record-amount">¥{{ formatAmount(record.consumeAmount) }}</text>
              <view class="check-icon" v-if="selectedRecord && selectedRecord.consumeId === record.consumeId">
                <text>✓</text>
              </view>
            </view>
          </view>

          <!-- 骨架屏加载 -->
          <view class="skeleton-wrapper" v-if="loadingAvailable">
            <view class="skeleton-record" v-for="i in 3" :key="i">
              <view class="skeleton-line"></view>
              <view class="skeleton-line short"></view>
            </view>
          </view>

          <!-- 空状态 -->
          <view class="no-data" v-if="availableRecords.length === 0 && !loadingAvailable">
            <view class="no-data-icon-wrapper">
              <text class="no-data-icon">📋</text>
            </view>
            <text class="no-data-text">暂无可退款记录</text>
            <text class="no-data-hint">仅支持7天内消费退款</text>
            <button class="action-btn" @click="loadAvailableRecords">
              <text class="btn-icon">🔄</text>
              <text>刷新记录</text>
            </button>
          </view>
        </view>
      </view>

      <!-- 退款表单 -->
      <view class="refund-form" v-if="selectedRecord">
        <view class="form-section">
          <text class="form-title">退款信息</text>

          <!-- 退款金额 -->
          <view class="form-item">
            <text class="form-label">退款金额</text>
            <view class="amount-input-box">
              <text class="input-prefix">¥</text>
              <input
                class="amount-input"
                type="digit"
                v-model="refundAmount"
                placeholder="请输入退款金额"
              />
            </view>
            <text class="max-tip">最大可退: ¥{{ formatAmount(selectedRecord.consumeAmount) }}</text>
          </view>

          <!-- 退款原因 -->
          <view class="form-item">
            <text class="form-label">退款原因</text>
            <view class="reason-options">
              <text
                class="reason-option"
                :class="{ selected: refundReason === option }"
                v-for="option in reasonOptions"
                :key="option"
                @click="selectReason(option)"
              >
                {{ option }}
              </text>
            </view>
            <textarea
              class="reason-textarea"
              v-if="refundReason === '其他原因'"
              v-model="customReason"
              placeholder="请输入退款原因（必填，最少5个字）"
              maxlength="200"
            />
            <text class="char-count" v-if="refundReason === '其他原因'">
              {{ customReason.length }}/200
            </text>
          </view>

          <!-- 联系电话 -->
          <view class="form-item">
            <text class="form-label">联系电话</text>
            <input
              class="form-input"
              type="tel"
              v-model="contactPhone"
              placeholder="请输入联系电话"
            />
          </view>
        </view>

        <!-- 提交按钮 -->
        <view class="submit-section">
          <button
            :class="['submit-btn', { disabled: !canSubmit || submitting }]"
            :disabled="!canSubmit || submitting"
            @click="submitRefund"
          >
            <text>{{ submitting ? '提交中...' : '提交申请' }}</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 退款记录 -->
    <view class="records-section" v-if="activeTab === 'records'">
      <!-- 状态筛选 -->
      <view class="filter-section">
        <scroll-view class="filter-scroll" scroll-x>
          <view
            class="filter-item"
            :class="{ active: selectedStatus === null }"
            @click="filterByStatus(null)"
          >
            <text class="filter-text">全部</text>
          </view>
          <view
            class="filter-item"
            :class="{ active: selectedStatus === 1 }"
            @click="filterByStatus(1)"
          >
            <text class="filter-text">待审核</text>
          </view>
          <view
            class="filter-item"
            :class="{ active: selectedStatus === 2 }"
            @click="filterByStatus(2)"
          >
            <text class="filter-text">审核通过</text>
          </view>
          <view
            class="filter-item"
            :class="{ active: selectedStatus === 3 }"
            @click="filterByStatus(3)"
          >
            <text class="filter-text">审核拒绝</text>
          </view>
          <view
            class="filter-item"
            :class="{ active: selectedStatus === 4 }"
            @click="filterByStatus(4)"
          >
            <text class="filter-text">退款完成</text>
          </view>
        </scroll-view>
      </view>

      <!-- 退款记录列表 -->
      <view class="refund-list">
        <view
          class="refund-item"
          :class="{ 'pressed': pressedRefundIndex === index }"
          v-for="(refund, index) in refundList"
          :key="index"
          @touchstart="pressedRefundIndex = index"
          @touchend="pressedRefundIndex = -1"
          @touchcancel="pressedRefundIndex = -1"
        >
          <view class="refund-header">
            <view class="refund-info">
              <text class="refund-merchant">{{ refund.merchantName || '退款' }}</text>
              <text class="refund-time">{{ formatDateTime(refund.applyTime) }}</text>
            </view>
            <view class="refund-status" :class="getStatusClass(refund.refundStatus)">
              <text class="status-text">{{ getStatusText(refund.refundStatus) }}</text>
            </view>
          </view>

          <view class="refund-details">
            <view class="detail-row">
              <text class="detail-label">退款金额</text>
              <text class="detail-value amount">¥{{ formatAmount(refund.refundAmount) }}</text>
            </view>
            <view class="detail-row">
              <text class="detail-label">退款原因</text>
              <text class="detail-value">{{ refund.refundReason }}</text>
            </view>
            <view class="detail-row" v-if="refund.auditRemark">
              <text class="detail-label">审核备注</text>
              <text class="detail-value">{{ refund.auditRemark }}</text>
            </view>
          </view>

          <view class="refund-actions">
            <button
              class="action-btn detail"
              @click.stop="handleRefundClick(refund, index)"
            >
              详情
            </button>
            <button
              class="action-btn cancel"
              v-if="refund.refundStatus === 1"
              @click.stop="cancelRefund(refund)"
            >
              取消申请
            </button>
          </view>
        </view>

        <!-- 加载更多 -->
        <view class="load-more" v-if="hasMoreRecords && !loadingRecords" @click="loadMoreRecords">
          <text>加载更多</text>
        </view>

        <!-- 空状态 -->
        <view class="no-data" v-if="refundList.length === 0 && !loadingRecords">
          <text class="no-data-icon">📋</text>
          <text class="no-data-text">暂无退款记录</text>
        </view>

        <!-- 加载状态 -->
        <view class="loading" v-if="loadingRecords">
          <text>加载中...</text>
        </view>
      </view>
    </view>

    <!-- 帮助弹窗 -->
    <view class="modal" v-if="showHelpModal" @click="showHelpModal = false">
      <view class="modal-content help-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">退款说明</text>
          <text class="close-btn" @click="showHelpModal = false">×</text>
        </view>
        <view class="modal-body">
          <view class="help-section">
            <text class="help-title">退款规则</text>
            <text class="help-item">• 仅支持7天内的消费退款申请</text>
            <text class="help-item">• 退款金额不超过原消费金额</text>
            <text class="help-item">• 退款将原路返回到支付账户</text>
          </view>
          <view class="help-section">
            <text class="help-title">处理时效</text>
            <text class="help-item">• 1-3个工作日内完成审核</text>
            <text class="help-item">• 审核通过后3-5个工作日到账</text>
            <text class="help-item">• 节假日顺延</text>
          </view>
          <view class="help-section">
            <text class="help-title">注意事项</text>
            <text class="help-item">• 优惠券、补贴部分不支持退款</text>
            <text class="help-item">• 部分商户商品不支持退款</text>
            <text class="help-item">• 退款申请提交后无法修改</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { refundApi } from '@/api/business/consume/refund-api.js'
import consumeApi from '@/api/business/consume/consume-api.js'
import cacheManager from '@/utils/cache-manager.js'

// 响应式数据
const userStore = useUserStore()
const activeTab = ref('apply')
const statistics = reactive({
  refundedAmount: 0,
  pendingAmount: 0,
  refundCount: 0
})

const availableRecords = ref([])
const selectedRecord = ref(null)
const refundAmount = ref('')
const refundReason = ref('')
const customReason = ref('')
const contactPhone = ref('')
const submitting = ref(false)
const loadingAvailable = ref(false)

const refundList = ref([])
const selectedStatus = ref(null)
const loadingRecords = ref(false)
const hasMoreRecords = ref(true)
const currentRecordsPage = ref(1)
const pageSize = ref(20)
const pressedRefundIndex = ref(-1) // 触摸反馈状态

const showHelpModal = ref(false)

// 退款原因选项
const reasonOptions = ['重复支付', '误操作', '商品问题', '服务不满意', '其他原因']

// 计算是否可以提交
const canSubmit = computed(() => {
  if (!selectedRecord.value) return false

  const amount = Number(refundAmount.value)
  const maxAmount = selectedRecord.value.consumeAmount

  const validAmount = amount > 0 && amount <= maxAmount
  const validReason = refundReason.value && (
    refundReason.value !== '其他原因' ||
    (customReason.value.trim().length >= 5)
  )

  return validAmount && validReason
})

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    1: '待审核',
    2: '审核通过',
    3: '审核拒绝',
    4: '退款完成'
  }
  return statusMap[status] || '未知'
}

// 获取状态样式
const getStatusClass = (status) => {
  const classMap = {
    1: 'pending',
    2: 'approved',
    3: 'rejected',
    4: 'completed'
  }
  return classMap[status] || 'unknown'
}

// 格式化金额
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 切换标签
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records' && refundList.value.length === 0) {
    loadRecords(true)
  }
}

// 选择消费记录
const selectRecord = (record) => {
  if (selectedRecord.value && selectedRecord.value.consumeId === record.consumeId) {
    selectedRecord.value = null
    refundAmount.value = ''
  } else {
    selectedRecord.value = record
    refundAmount.value = record.consumeAmount
  }
  uni.vibrateShort()
}

// 选择退款原因
const selectReason = (reason) => {
  refundReason.value = reason
  if (reason !== '其他原因') {
    customReason.value = ''
  }
  uni.vibrateShort()
}

// 加载可退款记录
const loadAvailableRecords = async () => {
  loadingAvailable.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const result = await refundApi.getAvailableRefundRecords(userId)
    if (result.success && result.data) {
      availableRecords.value = result.data
    }
  } catch (error) {
    console.error('加载可退款记录失败:', error)
  } finally {
    loadingAvailable.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const userId = userStore.employeeId
    if (!userId) return

    // 生成缓存key
    const cacheKey = `refund_statistics_${userId}`

    // 先尝试从缓存获取（统计数据有效期较短，5分钟）
    const cachedData = cacheManager.getCache(cacheKey)
    if (cachedData) {
      console.log('[退款] 使用缓存统计数据')
      Object.assign(statistics, cachedData)
      return
    }

    // 缓存未命中，请求API
    const result = await refundApi.getRefundStatistics(userId)
    if (result.success && result.data) {
      Object.assign(statistics, result.data)
      // 缓存统计数据，有效期5分钟（300000ms）
      cacheManager.setCache(cacheKey, result.data, 300000)
      console.log('[退款] 已缓存统计数据')
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载退款记录
const loadRecords = async (reset = false) => {
  if (reset) {
    currentRecordsPage.value = 1
    hasMoreRecords.value = true
    refundList.value = []
  }

  loadingRecords.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const params = {
      pageNum: currentRecordsPage.value,
      pageSize: pageSize.value
    }

    if (selectedStatus.value !== null) {
      params.refundStatus = selectedStatus.value
    }

    const result = await refundApi.getRefundRecords(userId, params)
    if (result.success && result.data) {
      const newRecords = result.data.list || []

      if (reset) {
        refundList.value = newRecords
      } else {
        refundList.value = [...refundList.value, ...newRecords]
      }

      hasMoreRecords.value = newRecords.length === pageSize.value
    }
  } catch (error) {
    console.error('加载退款记录失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingRecords.value = false
  }
}

// 加载更多记录
const loadMoreRecords = () => {
  currentRecordsPage.value++
  loadRecords()
}

// 按状态筛选
const filterByStatus = (status) => {
  selectedStatus.value = status
  loadRecords(true)
}

// 提交退款申请
const submitRefund = async () => {
  if (!canSubmit.value || !selectedRecord.value) return

  const finalReason = refundReason.value === '其他原因' ? customReason.value : refundReason.value

  submitting.value = true

  try {
    const result = await refundApi.applyRefund({
      userId: userStore.employeeId,
      consumeId: selectedRecord.value.consumeId,
      refundAmount: Number(refundAmount.value),
      refundReason: finalReason,
      contactPhone: contactPhone.value
    })

    if (result.success) {
      uni.showToast({ title: '申请成功', icon: 'success' })

      // 重置表单
      selectedRecord.value = null
      refundAmount.value = ''
      refundReason.value = ''
      customReason.value = ''
      contactPhone.value = ''

      // 刷新可退款记录
      await loadAvailableRecords()
      await loadStatistics()

      // 切换到记录标签
      setTimeout(() => {
        switchTab('records')
      }, 1000)
    }
  } catch (error) {
    console.error('提交退款申请失败:', error)
    uni.showToast({ title: '申请失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

// 取消退款申请
const cancelRefund = async (refund) => {
  const confirmed = await showConfirm('确认取消该退款申请？')
  if (!confirmed) return

  try {
    const result = await refundApi.cancelRefund(refund.refundId)
    if (result.success) {
      uni.showToast({ title: '取消成功', icon: 'success' })
      loadRecords(true)
      loadStatistics()
    }
  } catch (error) {
    console.error('取消退款申请失败:', error)
    uni.showToast({ title: '取消失败', icon: 'none' })
  }
}

// 处理退款项点击（带触摸反馈）
const handleRefundClick = (refund, index) => {
  // 触觉反馈
  uni.vibrateShort({ type: 'light' })

  // 延迟以显示按压效果
  setTimeout(() => {
    viewDetail(refund)
  }, 100)
}

// 查看详情
const viewDetail = (refund) => {
  const refundStr = encodeURIComponent(JSON.stringify(refund))
  uni.navigateTo({
    url: `/pages/consume/refund-detail?data=${refundStr}`
  })
}

// 显示确认对话框
const showConfirm = (content) => {
  return new Promise((resolve) => {
    uni.showModal({
      title: '确认操作',
      content,
      success: (res) => resolve(res.confirm)
    })
  })
}

// 显示帮助
const showHelp = () => {
  showHelpModal.value = true
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面生命周期
onMounted(() => {
  loadStatistics()
  loadAvailableRecords()
})

onShow(() => {
  if (activeTab.value === 'records') {
    loadRecords(true)
  }
})

onPullDownRefresh(() => {
  loadStatistics()
  if (activeTab.value === 'apply') {
    loadAvailableRecords()
  } else {
    loadRecords(true)
  }
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (activeTab.value === 'records' && hasMoreRecords.value && !loadingRecords.value) {
    loadMoreRecords()
  }
})
</script>

<style lang="scss" scoped>
.refund-page {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .nav-left, .nav-right {
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 600;
    color: #333;
  }

  .icon-back {
    font-size: 20px;
    color: #333;
  }

  .help-btn {
    width: 28px;
    height: 28px;
    line-height: 28px;
    text-align: center;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 50%;
    font-size: 16px;
    font-weight: 600;
  }
}

.stats-section {
  display: flex;
  gap: 10px;
  padding: 15px;

  .stat-card {
    flex: 1;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    padding: 15px;
    display: flex;
    align-items: center;
    gap: 10px;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

    .stat-icon {
      font-size: 24px;
    }

    .stat-info {
      flex: 1;

      .stat-label {
        display: block;
        font-size: 11px;
        opacity: 0.9;
        margin-bottom: 4px;
        color: #fff;
      }

      .stat-value {
        display: block;
        font-size: 16px;
        font-weight: 700;
        color: #fff;
      }
    }
  }
}

.tab-container {
  display: flex;
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 10px;
    border-radius: 10px;
    transition: all 0.3s ease;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

      .tab-text {
        color: #fff;
        font-weight: 600;
      }
    }

    .tab-text {
      font-size: 14px;
      color: #666;
      font-weight: 500;
    }
  }
}

.select-record-section,
.refund-form {
  margin: 0 15px 15px;
}

.section-title,
.form-title {
  display: block;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.record-list {
  .record-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 15px;
    margin-bottom: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 2px solid #e8e8e8;
    transition: all 0.3s ease;

    &.selected {
      border-color: #667eea;
      background: linear-gradient(135deg, #f0f5ff 0%, #e6f7ff 100%);
    }

    .record-left {
      flex: 1;

      .record-merchant {
        display: block;
        font-size: 15px;
        font-weight: 600;
        color: #333;
        margin-bottom: 6px;
      }

      .record-time {
        display: block;
        font-size: 12px;
        color: #999;
      }
    }

    .record-right {
      display: flex;
      align-items: center;
      gap: 10px;

      .record-amount {
        font-size: 16px;
        font-weight: 700;
        color: #ff4d4f;
      }

      .check-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;

        text {
          color: #fff;
          font-size: 12px;
          font-weight: 700;
        }
      }
    }
  }
}

.refund-form {
  .form-section {
    background-color: #fff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  }

  .form-item {
    margin-bottom: 20px;

    &:last-child {
      margin-bottom: 0;
    }

    .form-label {
      display: block;
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 10px;
    }

    .amount-input-box {
      display: flex;
      align-items: center;
      height: 50px;
      padding: 0 15px;
      background: #f5f5f5;
      border-radius: 10px;

      .input-prefix {
        font-size: 20px;
        font-weight: 600;
        color: #667eea;
        margin-right: 8px;
      }

      .amount-input {
        flex: 1;
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
    }

    .max-tip {
      display: block;
      font-size: 12px;
      color: #999;
      margin-top: 6px;
    }

    .reason-options {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;

      .reason-option {
        padding: 8px 16px;
        background: #f5f5f5;
        border-radius: 20px;
        font-size: 13px;
        color: #666;
        border: 1px solid transparent;
        transition: all 0.3s ease;

        &.selected {
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          color: #fff;
          border-color: #667eea;
        }
      }
    }

    .reason-textarea {
      width: 100%;
      min-height: 100px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 10px;
      font-size: 14px;
      color: #333;
      resize: vertical;
    }

    .char-count {
      display: block;
      text-align: right;
      font-size: 12px;
      color: #999;
      margin-top: 6px;
    }

    .form-input {
      width: 100%;
      height: 50px;
      padding: 0 15px;
      background: #f5f5f5;
      border-radius: 10px;
      font-size: 14px;
      color: #333;
    }
  }

  .submit-section {
    margin-top: 20px;

    .submit-btn {
      width: 100%;
      height: 50px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      border-radius: 25px;
      font-size: 16px;
      font-weight: 600;
      color: #fff;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);

      &:active:not(.disabled) {
        transform: scale(0.98);
      }

      &.disabled {
        background: #d9d9d9;
        box-shadow: none;
      }
    }
  }
}

.filter-section {
  margin: 0 15px 15px;

  .filter-scroll {
    white-space: nowrap;

    .filter-item {
      display: inline-block;
      padding: 8px 16px;
      margin-right: 10px;
      background: #fff;
      border-radius: 20px;
      border: 1px solid #e8e8e8;
      transition: all 0.3s ease;

      &.active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: #667eea;

        .filter-text {
          color: #fff;
        }
      }

      .filter-text {
        font-size: 13px;
        color: #666;
        font-weight: 500;
      }
    }
  }
}

.refund-list {
  padding: 0 15px 15px;

  .refund-item {
    background-color: #fff;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.2s ease;
    overflow: hidden;

    &.pressed {
      transform: scale(0.98);
      box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
    }

    .refund-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .refund-info {
        flex: 1;

        .refund-merchant {
          display: block;
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        .refund-time {
          display: block;
          font-size: 12px;
          color: #999;
        }
      }

      .refund-status {
        padding: 4px 10px;
        border-radius: 12px;
        font-size: 11px;
        font-weight: 500;

        &.pending {
          background-color: #fffbe6;
          color: #faad14;
        }

        &.approved {
          background-color: #e6f7ff;
          color: #1890ff;
        }

        &.rejected {
          background-color: #fff2f0;
          color: #ff4d4f;
        }

        &.completed {
          background-color: #f6ffed;
          color: #52c41a;
        }
      }
    }

    .refund-details {
      margin-bottom: 12px;

      .detail-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 8px;

        &:last-child {
          margin-bottom: 0;
        }

        .detail-label {
          font-size: 13px;
          color: #999;
        }

        .detail-value {
          font-size: 13px;
          color: #666;
          font-weight: 500;

          &.amount {
            font-weight: 700;
            color: #ff4d4f;
          }
        }
      }
    }

    .refund-actions {
      display: flex;
      gap: 10px;

      .action-btn {
        flex: 1;
        height: 36px;
        border-radius: 18px;
        font-size: 13px;
        font-weight: 500;
        border: none;

        &.detail {
          background-color: #f5f5f5;
          color: #666;
        }

        &.cancel {
          background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
          color: #fff;
        }
      }
    }
  }
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.6);
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;

  .modal-content {
    width: 100%;
    max-width: 400px;
    background-color: #fff;
    border-radius: 20px;
    overflow: hidden;
    max-height: 80vh;

    &.help-content {
      max-height: 70vh;
      overflow-y: auto;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      border-bottom: 1px solid #f0f0f0;

      .modal-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }

      .close-btn {
        font-size: 28px;
        color: #999;
        font-weight: 300;
        line-height: 1;
      }
    }

    .modal-body {
      padding: 20px;

      .help-section {
        margin-bottom: 20px;

        &:last-child {
          margin-bottom: 0;
        }

        .help-title {
          display: block;
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin-bottom: 12px;
        }

        .help-item {
          display: block;
          font-size: 14px;
          color: #666;
          line-height: 1.6;
          margin-bottom: 8px;
          padding-left: 10px;
        }
      }
    }
  }
}

.load-more {
  text-align: center;
  padding: 15px;
  color: #1890ff;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 12px;
}

.no-data {
  text-align: center;
  padding: 80px 30px;

  .no-data-icon-wrapper {
    width: 120px;
    height: 120px;
    margin: 0 auto 30px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    animation: pulse 2s ease-in-out infinite;

    .no-data-icon {
      font-size: 64px;
      display: block;
    }
  }

  .no-data-text {
    display: block;
    font-size: 18px;
    font-weight: 600;
    color: #333;
    margin-bottom: 10px;
  }

  .no-data-hint {
    display: block;
    font-size: 14px;
    color: #999;
    margin-bottom: 30px;
  }

  .action-btn {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin: 0 auto;
    padding: 12px 32px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border: none;
    border-radius: 24px;
    font-size: 15px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
    transition: all 0.3s;

    &:active {
      transform: scale(0.95);
      box-shadow: 0 2px 8px rgba(102, 126, 234, 0.2);
    }

    .btn-icon {
      font-size: 18px;
      animation: rotate 2s linear infinite;
    }
  }
}

// 骨架屏样式
.skeleton-wrapper {
  padding: 15px;

  .skeleton-record {
    background-color: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 12px;
    animation: skeleton-pulse 1.5s ease-in-out infinite;

    .skeleton-line {
      height: 14px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      border-radius: 7px;
      margin-bottom: 10px;
      animation: skeleton-shimmer 1.5s infinite;

      &.short {
        width: 40%;
      }

      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}

// 动画关键帧
@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes skeleton-shimmer {
  0% {
    background-position: -200% 0;
  }
  100% {
    background-position: 200% 0;
  }
}

@keyframes skeleton-pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}
</style>
