<template>
  <view class="card-manage-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="icon-back">←</text>
      </view>
      <view class="nav-title">卡片管理</view>
      <view class="nav-right"></view>
    </view>

    <!-- 卡片统计 -->
    <view class="stats-section">
      <view class="stat-card">
        <text class="stat-icon">💳</text>
        <view class="stat-info">
          <text class="stat-label">持卡数量</text>
          <text class="stat-value">{{ statistics.totalCards || 0 }}</text>
        </view>
      </view>
      <view class="stat-card">
        <text class="stat-icon">✅</text>
        <view class="stat-info">
          <text class="stat-label">正常卡片</text>
          <text class="stat-value normal">{{ statistics.normalCards || 0 }}</text>
        </view>
      </view>
      <view class="stat-card">
        <text class="stat-icon">⚠️</text>
        <view class="stat-info">
          <text class="stat-label">异常卡片</text>
          <text class="stat-value warning">{{ statistics.abnormalCards || 0 }}</text>
        </view>
      </view>
    </view>

    <!-- 标签页切换 -->
    <view class="tab-container">
      <view
        class="tab-item"
        :class="{ active: activeTab === 'cards' }"
        @click="switchTab('cards')"
      >
        <text class="tab-text">我的卡片</text>
      </view>
      <view
        class="tab-item"
        :class="{ active: activeTab === 'history' }"
        @click="switchTab('history')"
      >
        <text class="tab-text">操作记录</text>
      </view>
    </view>

    <!-- 卡片列表 -->
    <view class="card-list" v-if="activeTab === 'cards'">
      <view
        class="card-item"
        v-for="(card, index) in cardsList"
        :key="index"
      >
        <view class="card-header">
          <view class="card-info">
            <text class="card-name">{{ card.cardName || '一卡通' }}</text>
            <text class="card-number">**** {{ card.physicalCardNo ? card.physicalCardNo.slice(-4) : '****' }}</text>
          </view>
          <view class="card-status" :class="getStatusClass(card)">
            <text class="status-text">{{ getStatusText(card) }}</text>
          </view>
        </view>

        <view class="card-details">
          <view class="detail-row">
            <text class="detail-label">卡号</text>
            <text class="detail-value">{{ card.physicalCardNo || '未绑定' }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">卡片类型</text>
            <text class="detail-value">{{ getCardTypeText(card.cardType) }}</text>
          </view>
          <view class="detail-row">
            <text class="detail-label">绑定时间</text>
            <text class="detail-value">{{ formatDate(card.bindingTime) }}</text>
          </view>
          <view class="detail-row" v-if="card.lastUsedTime">
            <text class="detail-label">最后使用</text>
            <text class="detail-value">{{ formatDateTime(card.lastUsedTime) }}</text>
          </view>
        </view>

        <view class="card-actions">
          <button
            class="action-btn loss"
            v-if="card.cardStatus === 1"
            @click="reportLoss(card)"
          >
            挂失
          </button>
          <button
            class="action-btn unlock"
            v-if="card.cardStatus === 2"
            @click="reportUnlock(card)"
          >
            解挂
          </button>
          <button class="action-btn detail" @click="viewDetail(card)">
            详情
          </button>
        </view>
      </view>

      <!-- 空状态 -->
      <view class="no-data" v-if="cardsList.length === 0 && !loadingCards">
        <text class="no-data-icon">💳</text>
        <text class="no-data-text">暂无卡片</text>
        <text class="no-data-hint">请联系管理员办理卡片</text>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loadingCards">
        <text>加载中...</text>
      </view>
    </view>

    <!-- 操作历史 -->
    <view class="history-list" v-if="activeTab === 'history'">
      <view
        class="history-item"
        v-for="(record, index) in historyList"
        :key="index"
      >
        <view class="history-header">
          <view class="operation-info">
            <text class="operation-icon">{{ getOperationIcon(record.operationType) }}</text>
            <view class="operation-text">
              <text class="operation-type">{{ getOperationTypeText(record.operationType) }}</text>
              <text class="operation-time">{{ formatDateTime(record.operationTime) }}</text>
            </view>
          </view>
          <view class="operation-result" :class="getOperationResultClass(record.operationResult)">
            <text class="result-text">{{ getOperationResultText(record.operationResult) }}</text>
          </view>
        </view>

        <view class="history-details">
          <view class="detail-row" v-if="record.cardNumber">
            <text class="detail-label">卡号</text>
            <text class="detail-value">**** {{ record.cardNumber ? record.cardNumber.slice(-4) : '****' }}</text>
          </view>
          <view class="detail-row" v-if="record.operationReason">
            <text class="detail-label">原因</text>
            <text class="detail-value">{{ record.operationReason }}</text>
          </view>
          <view class="detail-row" v-if="record.operatorName">
            <text class="detail-label">操作人</text>
            <text class="detail-value">{{ record.operatorName }}</text>
          </view>
          <view class="detail-row" v-if="record.remark">
            <text class="detail-label">备注</text>
            <text class="detail-value">{{ record.remark }}</text>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view class="load-more" v-if="hasMoreHistory && !loadingHistory" @click="loadMoreHistory">
        <text>加载更多</text>
      </view>

      <!-- 空状态 -->
      <view class="no-data" v-if="historyList.length === 0 && !loadingHistory">
        <text class="no-data-icon">📋</text>
        <text class="no-data-text">暂无操作记录</text>
      </view>

      <!-- 加载状态 -->
      <view class="loading" v-if="loadingHistory">
        <text>加载中...</text>
      </view>
    </view>

    <!-- 挂失确认弹窗 -->
    <view class="modal" v-if="showLossModal" @click="showLossModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">确认挂失</text>
          <text class="close-btn" @click="showLossModal = false">×</text>
        </view>

        <view class="modal-body">
          <view class="info-text">
            <text class="warning-icon">⚠️</text>
            <text class="warning-text">挂失后该卡片将立即停止使用</text>
          </view>

          <view class="form-group">
            <text class="form-label">挂失原因</text>
            <textarea
              class="form-textarea"
              v-model="lossForm.lossReason"
              placeholder="请输入挂失原因（必填）"
              maxlength="200"
            />
            <text class="char-count">{{ lossForm.lossReason.length }}/200</text>
          </view>
        </view>

        <view class="modal-actions">
          <button class="cancel-btn" @click="showLossModal = false">取消</button>
          <button class="confirm-btn" @click="confirmLoss">确认挂失</button>
        </view>
      </view>
    </view>

    <!-- 解挂确认弹窗 -->
    <view class="modal" v-if="showUnlockModal" @click="showUnlockModal = false">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">确认解挂</text>
          <text class="close-btn" @click="showUnlockModal = false">×</text>
        </view>

        <view class="modal-body">
          <view class="info-text">
            <text class="success-icon">✅</text>
            <text class="success-text">解挂后该卡片将恢复正常使用</text>
          </view>

          <view class="form-group">
            <text class="form-label">解挂原因</text>
            <textarea
              class="form-textarea"
              v-model="unlockForm.unlockReason"
              placeholder="请输入解挂原因（必填）"
              maxlength="200"
            />
            <text class="char-count">{{ unlockForm.unlockReason.length }}/200</text>
          </view>
        </view>

        <view class="modal-actions">
          <button class="cancel-btn" @click="showUnlockModal = false">取消</button>
          <button class="confirm-btn success" @click="confirmUnlock">确认解挂</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import { cardApi } from '@/api/business/consume/card-api.js'

// 响应式数据
const userStore = useUserStore()
const activeTab = ref('cards')
const cardsList = ref([])
const historyList = ref([])
const statistics = reactive({
  totalCards: 0,
  normalCards: 0,
  abnormalCards: 0
})

const loadingCards = ref(false)
const loadingHistory = ref(false)
const hasMoreHistory = ref(true)
const currentHistoryPage = ref(1)
const pageSize = ref(20)

// 弹窗状态
const showLossModal = ref(false)
const showUnlockModal = ref(false)
const selectedCard = ref(null)

// 表单数据
const lossForm = reactive({
  userId: null,
  cardId: null,
  lossReason: ''
})

const unlockForm = reactive({
  userId: null,
  cardId: null,
  unlockReason: ''
})

// 卡片状态映射
const getStatusText = (card) => {
  const statusMap = {
    1: '正常',
    2: '已挂失',
    3: '已冻结',
    4: '已注销',
    5: '过期'
  }
  return statusMap[card.cardStatus] || '未知'
}

const getStatusClass = (card) => {
  const classMap = {
    1: 'normal',
    2: 'loss',
    3: 'frozen',
    4: 'cancelled',
    5: 'expired'
  }
  return classMap[card.cardStatus] || 'unknown'
}

// 卡片类型映射
const getCardTypeText = (type) => {
  const typeMap = {
    'PHYSICAL': '实体卡',
    'VIRTUAL': '虚拟卡',
    'NFC': 'NFC卡',
    'TEMPORARY': '临时卡'
  }
  return typeMap[type] || type || '普通卡'
}

// 操作类型映射
const getOperationTypeText = (type) => {
  const typeMap = {
    'BIND': '绑定卡片',
    'UNBIND': '解绑卡片',
    'LOSS': '卡片挂失',
    'UNLOCK': '卡片解挂',
    'FREEZE': '冻结卡片',
    'UNFREEZE': '解冻卡片',
    'CANCEL': '注销卡片',
    'REISSUE': '补换卡片',
    'RECHARGE': '卡片充值'
  }
  return typeMap[type] || type || '操作'
}

const getOperationIcon = (type) => {
  const iconMap = {
    'BIND': '🔗',
    'UNBIND': '🔓',
    'LOSS': '🚫',
    'UNLOCK': '✅',
    'FREEZE': '❄️',
    'UNFREEZE': '🔥',
    'CANCEL': '🗑️',
    'REISSUE': '🔄',
    'RECHARGE': '💰'
  }
  return iconMap[type] || '📝'
}

// 操作结果映射
const getOperationResultText = (result) => {
  const resultMap = {
    'SUCCESS': '成功',
    'FAILED': '失败',
    'PENDING': '处理中',
    'CANCELLED': '已取消'
  }
  return resultMap[result] || result || '未知'
}

const getOperationResultClass = (result) => {
  const classMap = {
    'SUCCESS': 'success',
    'FAILED': 'failed',
    'PENDING': 'pending',
    'CANCELLED': 'cancelled'
  }
  return classMap[result] || 'unknown'
}

// 格式化日期
const formatDate = (datetime) => {
  if (!datetime) return '-'
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
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
  if (tab === 'history' && historyList.value.length === 0) {
    loadHistory(true)
  }
}

// 加载卡片列表
const loadCards = async () => {
  loadingCards.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const result = await cardApi.getCardStatus(userId)
    if (result.success && result.data) {
      cardsList.value = result.data
    }
  } catch (error) {
    console.error('加载卡片列表失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingCards.value = false
  }
}

// 加载统计信息
const loadStatistics = async () => {
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const result = await cardApi.getCardStatistics(userId)
    if (result.success && result.data) {
      Object.assign(statistics, result.data)
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载操作历史
const loadHistory = async (reset = false) => {
  if (reset) {
    currentHistoryPage.value = 1
    hasMoreHistory.value = true
    historyList.value = []
  }

  loadingHistory.value = true
  try {
    const userId = userStore.employeeId
    if (!userId) return

    const params = {
      pageNum: currentHistoryPage.value,
      pageSize: pageSize.value
    }

    const result = await cardApi.getOperationHistory(userId, params)
    if (result.success && result.data) {
      const newHistory = result.data.list || []

      if (reset) {
        historyList.value = newHistory
      } else {
        historyList.value = [...historyList.value, ...newHistory]
      }

      hasMoreHistory.value = newHistory.length === pageSize.value
    }
  } catch (error) {
    console.error('加载操作历史失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史
const loadMoreHistory = () => {
  currentHistoryPage.value++
  loadHistory()
}

// 挂失操作
const reportLoss = (card) => {
  selectedCard.value = card
  lossForm.userId = userStore.employeeId
  lossForm.cardId = card.cardId
  lossForm.lossReason = ''
  showLossModal.value = true
}

// 确认挂失
const confirmLoss = async () => {
  if (!lossForm.lossReason.trim()) {
    uni.showToast({ title: '请输入挂失原因', icon: 'none' })
    return
  }

  try {
    const result = await cardApi.reportLoss(lossForm)
    if (result.success) {
      uni.showToast({ title: '挂失申请成功', icon: 'success' })
      showLossModal.value = false
      loadCards()
      loadStatistics()
      loadHistory(true)
    }
  } catch (error) {
    console.error('挂失申请失败:', error)
    uni.showToast({ title: '挂失申请失败', icon: 'none' })
  }
}

// 解挂操作
const reportUnlock = (card) => {
  selectedCard.value = card
  unlockForm.userId = userStore.employeeId
  unlockForm.cardId = card.cardId
  unlockForm.unlockReason = ''
  showUnlockModal.value = true
}

// 确认解挂
const confirmUnlock = async () => {
  if (!unlockForm.unlockReason.trim()) {
    uni.showToast({ title: '请输入解挂原因', icon: 'none' })
    return
  }

  try {
    const result = await cardApi.reportUnlock(unlockForm)
    if (result.success) {
      uni.showToast({ title: '解挂申请成功', icon: 'success' })
      showUnlockModal.value = false
      loadCards()
      loadStatistics()
      loadHistory(true)
    }
  } catch (error) {
    console.error('解挂申请失败:', error)
    uni.showToast({ title: '解挂申请失败', icon: 'none' })
  }
}

// 查看详情
const viewDetail = (card) => {
  const cardStr = encodeURIComponent(JSON.stringify(card))
  uni.navigateTo({
    url: `/pages/consume/card-detail?data=${cardStr}`
  })
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 页面生命周期
onMounted(() => {
  loadCards()
  loadStatistics()
})

// 下拉刷新
onPullDownRefresh(() => {
  loadCards()
  loadStatistics()
  if (activeTab.value === 'history') {
    loadHistory(true)
  }
  uni.stopPullDownRefresh()
})

// 触底加载更多
onReachBottom(() => {
  if (activeTab.value === 'history' && hasMoreHistory.value && !loadingHistory.value) {
    loadMoreHistory()
  }
})
</script>

<style lang="scss" scoped>
.card-manage-page {
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
      font-size: 28px;
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
        font-size: 20px;
        font-weight: 700;
        color: #fff;

        &.normal {
          color: #a7ffc7;
        }

        &.warning {
          color: #ffd6e7;
        }
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

.card-list,
.history-list {
  padding: 0 15px 15px;
}

.card-item {
  background-color: #fff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 15px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-left: 5px solid #667eea;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 15px;
    border-bottom: 1px solid #f0f0f0;

    .card-info {
      flex: 1;

      .card-name {
        display: block;
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 6px;
      }

      .card-number {
        display: block;
        font-size: 14px;
        color: #999;
        font-family: 'Courier New', monospace;
      }
    }

    .card-status {
      padding: 6px 12px;
      border-radius: 16px;
      font-size: 12px;
      font-weight: 500;

      &.normal {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.loss {
        background-color: #fff2f0;
        color: #ff4d4f;
      }

      &.frozen {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      &.cancelled {
        background-color: #f0f0f0;
        color: #999;
      }

      &.expired {
        background-color: #f0f0f0;
        color: #999;
      }
    }
  }

  .card-details {
    margin-bottom: 15px;

    .detail-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;

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
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 10px;

    .action-btn {
      flex: 1;
      height: 36px;
      border-radius: 18px;
      font-size: 14px;
      font-weight: 500;
      border: none;

      &.loss {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: #fff;
      }

      &.unlock {
        background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
        color: #fff;
      }

      &.detail {
        background-color: #f5f5f5;
        color: #666;
      }
    }
  }
}

.history-item {
  background-color: #fff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .operation-info {
      flex: 1;
      display: flex;
      gap: 10px;

      .operation-icon {
        font-size: 24px;
      }

      .operation-text {
        .operation-type {
          display: block;
          font-size: 15px;
          font-weight: 600;
          color: #333;
          margin-bottom: 4px;
        }

        .operation-time {
          display: block;
          font-size: 12px;
          color: #999;
        }
      }
    }

    .operation-result {
      padding: 4px 10px;
      border-radius: 12px;
      font-size: 11px;
      font-weight: 500;

      &.success {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.failed {
        background-color: #fff2f0;
        color: #ff4d4f;
      }

      &.pending {
        background-color: #fffbe6;
        color: #faad14;
      }

      &.cancelled {
        background-color: #f0f0f0;
        color: #999;
      }
    }
  }

  .history-details {
    .detail-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;

      &:last-child {
        margin-bottom: 0;
      }

      .detail-label {
        font-size: 12px;
        color: #999;
      }

      .detail-value {
        font-size: 12px;
        color: #666;
        font-weight: 500;
        max-width: 60%;
        text-align: right;
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

      .info-text {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 15px;
        border-radius: 10px;
        margin-bottom: 20px;

        .warning-icon {
          font-size: 24px;
        }

        .warning-text {
          font-size: 14px;
          color: #fa8c16;
        }

        .success-icon {
          font-size: 24px;
        }

        .success-text {
          font-size: 14px;
          color: #52c41a;
        }
      }

      .form-group {
        margin-bottom: 15px;

        .form-label {
          display: block;
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 10px;
        }

        .form-textarea {
          width: 100%;
          min-height: 100px;
          padding: 12px;
          border: 1px solid #d9d9d9;
          border-radius: 8px;
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
      }
    }

    .modal-actions {
      display: flex;
      gap: 10px;
      padding: 15px 20px 20px;
      border-top: 1px solid #f0f0f0;

      .cancel-btn,
      .confirm-btn {
        flex: 1;
        height: 44px;
        border-radius: 22px;
        font-size: 16px;
        font-weight: 500;
        border: none;
      }

      .cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      .confirm-btn {
        background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
        color: #fff;

        &.success {
          background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
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
  padding: 80px 0;

  .no-data-icon {
    display: block;
    font-size: 64px;
    margin-bottom: 15px;
  }

  .no-data-text {
    display: block;
    font-size: 16px;
    color: #999;
    margin-bottom: 8px;
  }

  .no-data-hint {
    display: block;
    font-size: 13px;
    color: #ccc;
  }
}

.loading {
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}
</style>
