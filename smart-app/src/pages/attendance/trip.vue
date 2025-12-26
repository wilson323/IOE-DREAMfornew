<template>
  <view class="trip-container">
    <!-- 顶部标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'apply' }"
        @click="switchTab('apply')"
      >
        <text class="tab-text">出差申请</text>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">出差记录</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="apply-form" v-if="activeTab === 'apply'">
      <!-- 出差类型 -->
      <view class="form-item">
        <text class="item-label">出差类型</text>
        <picker
          mode="selector"
          :range="tripTypes"
          @change="onTripTypeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !tripForm.tripType }">
              {{ tripForm.tripType || '请选择出差类型' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 出差目的地 -->
      <view class="form-item">
        <text class="item-label">出差目的地</text>
        <input
          class="input-field"
          v-model="tripForm.destination"
          placeholder="请输入出差目的地"
        />
      </view>

      <!-- 开始日期 -->
      <view class="form-item">
        <text class="item-label">开始日期</text>
        <picker
          mode="date"
          :value="tripForm.startDate"
          :start="minDate"
          @change="onStartDateChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !tripForm.startDate }">
              {{ tripForm.startDate || '请选择开始日期' }}
            </text>
            <text class="calendar-icon">📅</text>
          </view>
        </picker>
      </view>

      <!-- 结束日期 -->
      <view class="form-item">
        <text class="item-label">结束日期</text>
        <picker
          mode="date"
          :value="tripForm.endDate"
          :start="tripForm.startDate || minDate"
          @change="onEndDateChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !tripForm.endDate }">
              {{ tripForm.endDate || '请选择结束日期' }}
            </text>
            <text class="calendar-icon">📅</text>
          </view>
        </picker>
      </view>

      <!-- 出差时长 -->
      <view class="form-item">
        <text class="item-label">出差时长</text>
        <view class="duration-display">
          <text class="duration-value">{{ calculatedDuration }}</text>
          <text class="duration-unit">天</text>
        </view>
      </view>

      <!-- 出差事由 -->
      <view class="form-item reason-item">
        <text class="item-label">出差事由</text>
        <textarea
          class="reason-input"
          v-model="tripForm.reason"
          placeholder="请简要说明出差事由"
          maxlength="200"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ tripForm.reason.length }}/200</text>
      </view>

      <!-- 备注 -->
      <view class="form-item remark-item">
        <text class="item-label">备注</text>
        <textarea
          class="remark-input"
          v-model="tripForm.remark"
          placeholder="其他需要说明的事项（选填）"
          maxlength="100"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ tripForm.remark.length }}/100</text>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :disabled="!canSubmit"
          @click="submitTrip"
        >
          <text class="btn-text">{{ submitting ? '提交中...' : '提交申请' }}</text>
        </button>
      </view>
    </view>

    <!-- 出差记录 -->
    <view class="records-list" v-if="activeTab === 'records'">
      <scroll-view
        class="list-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshingRecords"
        @refresherrefresh="refreshRecords"
      >
        <view class="record-card" v-for="record in tripRecords" :key="record.id">
          <view class="card-header">
            <text class="trip-destination">{{ record.destination }}</text>
            <text class="record-status" :class="getStatusClass(record.status)">
              {{ getStatusText(record.status) }}
            </text>
          </view>

          <view class="record-info">
            <view class="info-row">
              <text class="info-label">出差类型:</text>
              <text class="info-value">{{ record.tripType }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">出差时间:</text>
              <text class="info-value">{{ formatDate(record.startDate) }} - {{ formatDate(record.endDate) }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">出差时长:</text>
              <text class="info-value">{{ record.duration }}天</text>
            </view>
            <view class="info-row" v-if="record.reason">
              <text class="info-label">出差事由:</text>
              <text class="info-value">{{ record.reason }}</text>
            </view>
          </view>
        </view>

        <view class="empty-state" v-if="tripRecords.length === 0 && !loadingRecords">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无出差记录</text>
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
const tripForm = reactive({
  tripType: '',
  destination: '',
  startDate: '',
  endDate: '',
  reason: '',
  remark: ''
})

const tripTypes = ['国内出差', '国外出差', '市内出差', '其他']
const minDate = dayjs().format('YYYY-MM-DD')
const submitting = ref(false)
const tripRecords = ref([])
const refreshingRecords = ref(false)
const loadingRecords = ref(false)
const employeeId = ref(1001)

const calculatedDuration = computed(() => {
  if (!tripForm.startDate || !tripForm.endDate) return 0
  const start = dayjs(tripForm.startDate)
  const end = dayjs(tripForm.endDate)
  return end.diff(start, 'day') + 1
})

const canSubmit = computed(() => {
  return (
    tripForm.tripType &&
    tripForm.destination &&
    tripForm.startDate &&
    tripForm.endDate &&
    tripForm.reason &&
    calculatedDuration.value > 0
  )
})

const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records') loadTripRecords()
}

const onTripTypeChange = (e) => { tripForm.tripType = tripTypes[e.detail.value] }
const onStartDateChange = (e) => { tripForm.startDate = e.detail.value }
const onEndDateChange = (e) => { tripForm.endDate = e.detail.value }

const submitTrip = async () => {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const res = await attendanceApi.travelApi.applyTravel({
      employeeId: employeeId.value,
      tripType: tripForm.tripType,
      destination: tripForm.destination,
      startTime: tripForm.startDate,
      endTime: tripForm.endDate,
      duration: calculatedDuration.value,
      reason: tripForm.reason,
      remark: tripForm.remark
    })

    if (res.success) {
      uni.showToast({ title: '申请提交成功', icon: 'success' })
      setTimeout(() => switchTab('records'), 1500)
    }
  } finally {
    submitting.value = false
  }
}

const loadTripRecords = async () => {
  try {
    const res = await attendanceApi.travelApi.getTravelRecords({
      employeeId: employeeId.value,
      pageSize: 20,
      pageNum: 1
    })
    if (res.success) {
      tripRecords.value = res.data?.list || []
    }
  } catch (error) {
    console.error('[出差记录] 加载失败:', error)
  }
}

const refreshRecords = () => {
  refreshingRecords.value = true
  loadTripRecords()
}

const formatDate = (dateStr) => dayjs(dateStr).format('MM月DD日')
const getStatusText = (status) => ({ 'PENDING': '待审批', 'APPROVED': '已通过', 'REJECTED': '已拒绝' }[status] || '未知')
const getStatusClass = (status) => ({ 'PENDING': 'status-pending', 'APPROVED': 'status-approved', 'REJECTED': 'status-rejected' }[status] || '')
</script>

<style lang="scss" scoped>
.trip-container { min-height: 100vh; background-color: #f5f5f5; }
.tabs { display: flex; background: white; padding: 20rpx 30rpx; border-bottom: 1rpx solid #f0f0f0; }
.tab-item { flex: 1; text-align: center; padding: 16rpx 0; position: relative; }
.tab-item.active .tab-text { color: #667eea; font-weight: bold; }
.tab-item.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 60rpx; height: 4rpx; background: #667eea; border-radius: 2rpx; }
.tab-text { font-size: 28rpx; color: #666; }

.apply-form { padding: 30rpx; }
.form-item { background: white; padding: 30rpx; border-radius: 16rpx; margin-bottom: 20rpx; }
.item-label { display: block; font-size: 28rpx; color: #333; font-weight: bold; margin-bottom: 20rpx; }

.input-field {
  width: 100%;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
}

.picker-value { display: flex; justify-content: space-between; align-items: center; }
.value-text { font-size: 28rpx; color: #333; }
.value-text.placeholder { color: #999; }
.arrow, .calendar-icon { font-size: 32rpx; color: #999; }

.duration-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  padding: 30rpx 0;
}
.duration-value { font-size: 60rpx; font-weight: bold; color: #667eea; }
.duration-unit { font-size: 28rpx; color: #666; margin-left: 10rpx; }

.reason-input, .remark-input {
  width: 100%;
  min-height: 120rpx;
  padding: 20rpx;
  background-color: #f5f5f5;
  border-radius: 10rpx;
  font-size: 28rpx;
}
.char-count { display: block; text-align: right; font-size: 22rpx; color: #999; margin-top: 10rpx; }

.submit-section { margin-top: 40rpx; }
.submit-btn {
  width: 100%;
  height: 90rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  font-size: 32rpx;
  font-weight: bold;
}
.submit-btn:disabled { opacity: 0.5; }

.records-list { height: calc(100vh - 120rpx); }
.list-scroll { height: 100%; padding: 30rpx; }
.record-card {
  background: white;
  padding: 30rpx;
  border-radius: 16rpx;
  margin-bottom: 20rpx;
}
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
.trip-destination { font-size: 28rpx; font-weight: bold; color: #333; }
.record-status { font-size: 22rpx; padding: 6rpx 16rpx; border-radius: 20rpx; }
.status-pending { background-color: #fffbeb; color: #f59e0b; }
.status-approved { background-color: #f0fdf4; color: #22c55e; }
.status-rejected { background-color: #fef2f2; color: #ef4444; }

.record-info { margin-bottom: 20rpx; }
.info-row { display: flex; margin-bottom: 10rpx; }
.info-label { font-size: 24rpx; color: #999; width: 140rpx; }
.info-value { font-size: 24rpx; color: #333; }

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}
.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; opacity: 0.5; }
.empty-text { font-size: 26rpx; color: #999; }
</style>
