<template>
  <view class="repair-container">
    <!-- 顶部标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'apply' }"
        @click="switchTab('apply')"
      >
        <text class="tab-text">补卡申请</text>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">补卡记录</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="apply-form" v-if="activeTab === 'apply'">
      <!-- 补卡类型 -->
      <view class="form-item">
        <text class="item-label">补卡类型</text>
        <view class="type-selector">
          <view
            class="type-option"
            :class="{ 'active': repairForm.supplementType === 'IN' }"
            @click="selectType('IN')"
          >
            <text class="type-icon">🕐</text>
            <text class="type-text">上班补卡</text>
          </view>
          <view
            class="type-option"
            :class="{ 'active': repairForm.supplementType === 'OUT' }"
            @click="selectType('OUT')"
          >
            <text class="type-icon">🕑</text>
            <text class="type-text">下班补卡</text>
          </view>
        </view>
      </view>

      <!-- 补卡日期 -->
      <view class="form-item">
        <text class="item-label">补卡日期</text>
        <picker
          mode="date"
          :value="repairForm.supplementDate"
          :end="maxDate"
          @change="onDateChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !repairForm.supplementDate }">
              {{ repairForm.supplementDate || '请选择补卡日期' }}
            </text>
            <text class="calendar-icon">📅</text>
          </view>
        </picker>
      </view>

      <!-- 补卡时间 -->
      <view class="form-item">
        <text class="item-label">补卡时间</text>
        <picker
          mode="time"
          :value="repairForm.supplementTime"
          @change="onTimeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !repairForm.supplementTime }">
              {{ repairForm.supplementTime || '请选择补卡时间' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 补卡原因 -->
      <view class="form-item reason-item">
        <text class="item-label">补卡原因</text>
        <picker
          mode="selector"
          :range="reasonOptions"
          @change="onReasonChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !repairForm.reason }">
              {{ repairForm.reason || '请选择补卡原因' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 详细说明 -->
      <view class="form-item detail-item">
        <text class="item-label">详细说明</text>
        <textarea
          class="detail-input"
          v-model="repairForm.detail"
          placeholder="请详细说明补卡原因（如：忘记打卡、设备故障等）"
          maxlength="200"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ repairForm.detail.length }}/200</text>
      </view>

      <!-- 附件上传 -->
      <view class="form-item attachment-item">
        <text class="item-label">证明材料</text>
        <view class="attachment-list">
          <view class="attachment-item" v-for="(file, index) in attachments" :key="index">
            <image class="attachment-image" :src="file.url" mode="aspectFill"></image>
            <view class="delete-btn" @click="removeAttachment(index)">
              <text class="delete-icon">×</text>
            </view>
          </view>
          <view class="add-attachment" @click="chooseAttachment" v-if="attachments.length < 3">
            <text class="add-icon">+</text>
            <text class="add-text">添加附件</text>
          </view>
        </view>
        <text class="attachment-tip">可上传证明材料（如外出工作证明等）</text>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :disabled="!canSubmit"
          @click="submitRepair"
        >
          <text class="btn-text">{{ submitting ? '提交中...' : '提交申请' }}</text>
        </button>
      </view>

      <!-- 补卡须知 -->
      <view class="notice-section">
        <view class="notice-title">
          <text class="title-icon">📌</text>
          <text class="title-text">补卡须知</text>
        </view>
        <view class="notice-content">
          <text class="notice-item">1. 补卡申请需在缺卡后3个工作日内提交</text>
          <text class="notice-item">2. 每月补卡次数不得超过3次</text>
          <text class="notice-item">3. 请如实填写补卡原因，虚假申请将受处罚</text>
        </view>
      </view>
    </view>

    <!-- 补卡记录 -->
    <view class="records-list" v-if="activeTab === 'records'">
      <scroll-view
        class="list-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshingRecords"
        @refresherrefresh="refreshRecords"
        @scrolltolower="loadMoreRecords"
      >
        <view class="record-card" v-for="record in repairRecords" :key="record.id">
          <view class="card-header">
            <text class="record-type">{{ record.supplementType === 'IN' ? '上班补卡' : '下班补卡' }}</text>
            <text class="record-status" :class="getStatusClass(record.status)">
              {{ getStatusText(record.status) }}
            </text>
          </view>

          <view class="record-info">
            <view class="info-row">
              <text class="info-label">补卡日期:</text>
              <text class="info-value">{{ formatDate(record.supplementDate) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">补卡时间:</text>
              <text class="info-value">{{ record.supplementTime }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">补卡原因:</text>
              <text class="info-value">{{ record.reason }}</text>
            </view>
          </view>

          <view class="card-actions" v-if="record.status === 'PENDING'">
            <button class="action-btn cancel-btn" @click="cancelRepair(record.id)">
              <text class="btn-text">撤销申请</text>
            </button>
          </view>
        </view>

        <view class="empty-state" v-if="repairRecords.length === 0 && !loadingRecords">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无补卡记录</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

const activeTab = ref('apply')
const repairForm = reactive({
  supplementType: 'IN',
  supplementDate: '',
  supplementTime: '',
  reason: '',
  detail: ''
})

const maxDate = dayjs().format('YYYY-MM-DD')
const reasonOptions = ['忘记打卡', '设备故障', '外出工作', '网络问题', '其他']
const attachments = ref([])
const submitting = ref(false)

const repairRecords = ref([])
const refreshingRecords = ref(false)
const loadingRecords = ref(false)
const employeeId = ref(1001)

const canSubmit = computed(() => {
  return repairForm.supplementType && repairForm.supplementDate && repairForm.supplementTime && repairForm.reason
})

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records') loadRepairRecords()
}

const selectType = (type) => { repairForm.supplementType = type }
const onDateChange = (e) => { repairForm.supplementDate = e.detail.value }
const onTimeChange = (e) => { repairForm.supplementTime = e.detail.value }
const onReasonChange = (e) => { repairForm.reason = reasonOptions[e.detail.value] }

const chooseAttachment = () => {
  uni.chooseImage({
    count: 3 - attachments.value.length,
    success: (res) => {
      res.tempFilePaths.forEach(filePath => {
        attachments.value.push({ url: filePath })
      })
    }
  })
}

const removeAttachment = (index) => { attachments.value.splice(index, 1) }

const submitRepair = async () => {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const res = await attendanceApi.supplementApi.applySupplement({
      employeeId: employeeId.value,
      supplementType: repairForm.supplementType,
      supplementDate: repairForm.supplementDate,
      supplementTime: repairForm.supplementTime,
      reason: repairForm.reason,
      detail: repairForm.detail,
      attachments: attachments.value.map(a => a.url)
    })

    if (res.success) {
      uni.showToast({ title: '申请提交成功', icon: 'success' })
      setTimeout(() => switchTab('records'), 1500)
    }
  } finally {
    submitting.value = false
  }
}

const loadRepairRecords = async () => {
  try {
    const res = await attendanceApi.supplementApi.getSupplementRecords({
      employeeId: employeeId.value,
      pageSize: 20,
      pageNum: 1
    })
    if (res.success) {
      repairRecords.value = res.data?.list || []
    }
  } catch (error) {
    console.error('[补卡记录] 加载失败:', error)
  }
}

const refreshRecords = () => {
  refreshingRecords.value = true
  loadRepairRecords()
}

const cancelRepair = async (id) => {
  uni.showModal({
    title: '确认撤销',
    content: '确定要撤销此补卡申请吗？',
    success: async (res) => {
      if (res.confirm) {
        // TODO: 调用撤销接口
        uni.showToast({ title: '撤销成功', icon: 'success' })
        loadRepairRecords()
      }
    }
  })
}

const formatDate = (dateStr) => dayjs(dateStr).format('YYYY年MM月DD日')
const getStatusText = (status) => ({ 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已拒绝' }[status] || '未知')
const getStatusClass = (status) => ({ 'PENDING': 'status-pending', 'APPROVED': 'status-approved', 'REJECTED': 'status-rejected' }[status] || '')
</script>

<style lang="scss" scoped>
.repair-container { min-height: 100vh; background-color: #f5f5f5; }
.tabs { display: flex; background: white; padding: 20rpx 30rpx; border-bottom: 1rpx solid #f0f0f0; }
.tab-item { flex: 1; text-align: center; padding: 16rpx 0; position: relative; }
.tab-item.active .tab-text { color: #667eea; font-weight: bold; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 60rpx; height: 4rpx; background: #667eea; border-radius: 2rpx; }
.tab-text { font-size: 28rpx; color: #666; }

.apply-form { padding: 30rpx; }
.form-item { background: white; padding: 30rpx; border-radius: 16rpx; margin-bottom: 20rpx; }
.item-label { display: block; font-size: 28rpx; color: #333; font-weight: bold; margin-bottom: 20rpx; }
.picker-value { display: flex; justify-content: space-between; align-items: center; }
.value-text { font-size: 28rpx; color: #333; }
.value-text.placeholder { color: #999; }
.arrow, .calendar-icon { font-size: 32rpx; color: #999; }

.type-selector { display: flex; justify-content: space-around; }
.type-option { flex: 1; display: flex; flex-direction: column; align-items: center; padding: 20rpx; border: 2rpx solid #f0f0f0; border-radius: 10rpx; margin: 0 10rpx; }
.type-option.active { border-color: #667eea; background-color: #f0f9ff; }
.type-icon { font-size: 36rpx; margin-bottom: 10rpx; }
.type-text { font-size: 24rpx; color: #666; }

.detail-input { width: 100%; min-height: 120rpx; padding: 20rpx; background-color: #f5f5f5; border-radius: 10rpx; font-size: 28rpx; }
.char-count { display: block; text-align: right; font-size: 22rpx; color: #999; margin-top: 10rpx; }

.attachment-list { display: flex; flex-wrap: wrap; margin-top: 20rpx; }
.attachment-item { position: relative; width: 150rpx; height: 150rpx; margin-right: 20rpx; margin-bottom: 20rpx; }
.attachment-image { width: 100%; height: 100%; border-radius: 10rpx; }
.delete-btn { position: absolute; top: -10rpx; right: -10rpx; width: 40rpx; height: 40rpx; background-color: #ef4444; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
.delete-icon { color: white; font-size: 28rpx; }
.add-attachment { width: 150rpx; height: 150rpx; border: 2rpx dashed #ddd; border-radius: 10rpx; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.add-icon { font-size: 40rpx; color: #999; margin-bottom: 10rpx; }
.add-text { font-size: 22rpx; color: #999; }
.attachment-tip { display: block; font-size: 22rpx; color: #999; margin-top: 10rpx; }

.submit-section { margin-top: 40rpx; }
.submit-btn { width: 100%; height: 90rpx; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; border-radius: 45rpx; display: flex; align-items: center; justify-content: center; border: none; font-size: 32rpx; font-weight: bold; }
.submit-btn:disabled { opacity: 0.5; }

.notice-section { background: white; padding: 30rpx; border-radius: 16rpx; margin-top: 30rpx; }
.notice-title { display: flex; align-items: center; margin-bottom: 20rpx; }
.title-icon { font-size: 24rpx; margin-right: 10rpx; }
.title-text { font-size: 26rpx; font-weight: bold; color: #333; }
.notice-item { display: block; font-size: 24rpx; color: #666; line-height: 1.8; margin-bottom: 10rpx; }

.records-list { height: calc(100vh - 120rpx); }
.list-scroll { height: 100%; padding: 30rpx; }
.record-card { background: white; padding: 30rpx; border-radius: 16rpx; margin-bottom: 20rpx; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.record-type { font-size: 28rpx; font-weight: bold; color: #333; }
.record-status { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.status-pending { background-color: #fffbeb; color: #f59e0b; }
.status-approved { background-color: #f0fdf4; color: #22c55e; }
.status-rejected { background-color: #fef2f2; color: #ef4444; }

.record-info { margin-bottom: 20rpx; }
.info-row { display: flex; margin-bottom: 10rpx; }
.info-label { font-size: 24rpx; color: #999; width: 140rpx; }
.info-value { font-size: 24rpx; color: #333; }

.card-actions { display: flex; justify-content: flex-end; }
.action-btn { padding: 10rpx 30rpx; border-radius: 20rpx; font-size: 24rpx; border: none; }
.cancel-btn { background-color: #f3f4f6; color: #666; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 120rpx 0; }
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; opacity: 0.5; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
