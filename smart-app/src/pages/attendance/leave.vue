<template>
  <view class="leave-container">
    <!-- 顶部标签页 -->
    <view class="tabs">
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'apply' }"
        @click="switchTab('apply')"
      >
        <text class="tab-text">请假申请</text>
      </view>
      <view
        class="tab-item"
        :class="{ 'active': activeTab === 'records' }"
        @click="switchTab('records')"
      >
        <text class="tab-text">请假记录</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view class="apply-form" v-if="activeTab === 'apply'">
      <!-- 请假类型 -->
      <view class="form-item">
        <text class="item-label">请假类型</text>
        <picker
          mode="selector"
          :range="leaveTypes"
          :value="leaveTypeIndex"
          @change="onLeaveTypeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !leaveForm.leaveType }">
              {{ leaveTypes[leaveTypeIndex] || '请选择请假类型' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 请假时长 -->
      <view class="form-item">
        <text class="item-label">请假时长</text>
        <view class="duration-display">
          <text class="duration-value">{{ calculatedDuration }}</text>
          <text class="duration-unit">天</text>
        </view>
      </view>

      <!-- 开始时间 -->
      <view class="form-item">
        <text class="item-label">开始时间</text>
        <picker
          mode="multiSelector"
          :range="[dateList, timeList]"
          :value="[startDateIndex, startTimeIndex]"
          @change="onStartTimeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !leaveForm.startTime }">
              {{ leaveForm.startTime || '请选择开始时间' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 结束时间 -->
      <view class="form-item">
        <text class="item-label">结束时间</text>
        <picker
          mode="multiSelector"
          :range="[dateList, timeList]"
          :value="[endDateIndex, endTimeIndex]"
          @change="onEndTimeChange"
        >
          <view class="picker-value">
            <text class="value-text" :class="{ 'placeholder': !leaveForm.endTime }">
              {{ leaveForm.endTime || '请选择结束时间' }}
            </text>
            <text class="arrow">›</text>
          </view>
        </picker>
      </view>

      <!-- 请假事由 -->
      <view class="form-item reason-item">
        <text class="item-label">请假事由</text>
        <textarea
          class="reason-input"
          v-model="leaveForm.reason"
          placeholder="请输入请假事由"
          maxlength="200"
          :show-confirm-bar="false"
        ></textarea>
        <text class="char-count">{{ leaveForm.reason.length }}/200</text>
      </view>

      <!-- 附件上传 -->
      <view class="form-item attachment-item">
        <text class="item-label">附件上传</text>
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
        <text class="attachment-tip">最多上传3张图片，如病假证明等</text>
      </view>

      <!-- 审批人（可选） -->
      <view class="form-item">
        <text class="item-label">审批人</text>
        <view class="approver-list" v-if="approvers.length > 0">
          <view class="approver-item" v-for="(approver, index) in approvers" :key="index">
            <image class="approver-avatar" :src="approver.avatar"></image>
            <text class="approver-name">{{ approver.name }}</text>
            <text class="remove-approver" @click="removeApprover(index)">×</text>
          </view>
          <view class="add-approver" @click="chooseApprover">
            <text class="add-icon">+</text>
          </view>
        </view>
        <view class="add-approver-btn" v-else @click="chooseApprover">
          <text class="btn-text">+ 添加审批人</text>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-section">
        <button
          class="submit-btn"
          :disabled="!canSubmit"
          @click="submitLeave"
        >
          <text class="btn-text">{{ submitting ? '提交中...' : '提交申请' }}</text>
        </button>
      </view>

      <!-- 请假须知 -->
      <view class="notice-section">
        <view class="notice-title">
          <text class="title-icon">📌</text>
          <text class="title-text">请假须知</text>
        </view>
        <view class="notice-content">
          <text class="notice-item">1. 请提前申请请假，紧急情况需事后补办</text>
          <text class="notice-item">2. 请假时长由系统自动计算</text>
          <text class="notice-item">3. 病假需上传医院证明等附件</text>
          <text class="notice-item">4. 年假需提前3天申请，婚假需提前7天</text>
        </view>
      </view>
    </view>

    <!-- 请假记录 -->
    <view class="records-list" v-if="activeTab === 'records'">
      <!-- 筛选器 -->
      <view class="filter-bar">
        <view class="filter-tabs">
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'all' }"
            @click="setRecordFilter('all')"
          >
            <text class="tab-text">全部</text>
          </view>
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'pending' }"
            @click="setRecordFilter('pending')"
          >
            <text class="tab-text">待审批</text>
          </view>
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'approved' }"
            @click="setRecordFilter('approved')"
          >
            <text class="tab-text">已通过</text>
          </view>
          <view
            class="filter-tab"
            :class="{ 'active': recordFilter === 'rejected' }"
            @click="setRecordFilter('rejected')"
          >
            <text class="tab-text">已拒绝</text>
          </view>
        </view>
      </view>

      <!-- 记录列表 -->
      <scroll-view
        class="list-scroll"
        scroll-y
        refresher-enabled
        :refresher-triggered="refreshingRecords"
        @refresherrefresh="refreshRecords"
        @scrolltolower="loadMoreRecords"
      >
        <view class="record-card" v-for="record in leaveRecords" :key="record.id">
          <!-- 卡片头部 -->
          <view class="card-header">
            <text class="leave-type">{{ record.leaveType }}</text>
            <text class="record-status" :class="getStatusClass(record.status)">
              {{ getStatusText(record.status) }}
            </text>
          </view>

          <!-- 请假时间 -->
          <view class="time-info">
            <view class="time-row">
              <text class="time-label">开始时间:</text>
              <text class="time-value">{{ record.startTime }}</text>
            </view>
            <view class="time-row">
              <text class="time-label">结束时间:</text>
              <text class="time-value">{{ record.endTime }}</text>
            </view>
            <view class="time-row">
              <text class="time-label">请假时长:</text>
              <text class="time-value">{{ record.duration }}天</text>
            </view>
          </view>

          <!-- 请假事由 -->
          <view class="reason-info">
            <text class="reason-label">请假事由:</text>
            <text class="reason-text">{{ record.reason }}</text>
          </view>

          <!-- 审批信息 -->
          <view class="approval-info" v-if="record.approver">
            <text class="approval-label">审批人:</text>
            <text class="approval-value">{{ record.approver }}</text>
          </view>

          <!-- 操作按钮 -->
          <view class="card-actions" v-if="record.status === 'PENDING'">
            <button class="action-btn cancel-btn" @click="cancelLeave(record.id)">
              <text class="btn-text">撤销申请</text>
            </button>
          </view>

          <!-- 审批意见 -->
          <view class="approval-comment" v-if="record.comment">
            <text class="comment-label">审批意见:</text>
            <text class="comment-text">{{ record.comment }}</text>
          </view>
        </view>

        <!-- 加载提示 -->
        <view class="load-more" v-if="hasMoreRecords">
          <text class="load-text">{{ loadingRecords ? '加载中...' : '上拉加载更多' }}</text>
        </view>

        <!-- 空状态 -->
        <view class="empty-state" v-if="leaveRecords.length === 0 && !loadingRecords">
          <text class="empty-icon">📭</text>
          <text class="empty-text">暂无请假记录</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import attendanceApi from '@/api/business/attendance/attendance-api'
import dayjs from 'dayjs'

// ==================== 响应式数据 ====================
const activeTab = ref('apply')

// 请假表单
const leaveForm = reactive({
  leaveType: '',
  startTime: '',
  endTime: '',
  reason: ''
})

const leaveTypes = ['事假', '病假', '年假', '婚假', '产假', '丧假', '调休', '其他']
const leaveTypeIndex = ref(0)

// 日期时间列表
const dateList = ref([])
const timeList = ref([])
const startDateIndex = ref(0)
const startTimeIndex = ref(0)
const endDateIndex = ref(0)
const endTimeIndex = ref(0)

// 附件
const attachments = ref([])

// 审批人
const approvers = ref([])

// 提交状态
const submitting = ref(false)

// 请假记录
const leaveRecords = ref([])
const recordFilter = ref('all')
const refreshingRecords = ref(false)
const loadingRecords = ref(false)
const hasMoreRecords = ref(true)

const recordPagination = reactive({
  pageNum: 1,
  pageSize: 20
})

const employeeId = ref(1001) // TODO: 从用户信息获取

// ==================== 计算属性 ====================
/**
 * 计算请假时长
 */
const calculatedDuration = computed(() => {
  if (!leaveForm.startTime || !leaveForm.endTime) {
    return 0
  }

  const start = dayjs(leaveForm.startTime)
  const end = dayjs(leaveForm.endTime)

  // 计算小时数
  const hours = end.diff(start, 'hour', true)

  // 转换为天数（按8小时工作制）
  const days = (hours / 8).toFixed(1)

  return days
})

/**
 * 是否可以提交
 */
const canSubmit = computed(() => {
  return (
    leaveForm.leaveType &&
    leaveForm.startTime &&
    leaveForm.endTime &&
    leaveForm.reason &&
    calculatedDuration.value > 0
  )
})

// ==================== 生命周期 ====================
onMounted(() => {
  initDateTimeLists()
  loadLeaveRecords()
})

// ==================== 初始化 ====================
/**
 * 初始化日期时间列表
 */
const initDateTimeLists = () => {
  // 生成未来30天的日期
  const dates = []
  for (let i = 0; i < 30; i++) {
    const date = dayjs().add(i, 'day')
    dates.push(date.format('YYYY-MM-DD'))
  }
  dateList.value = dates

  // 生成时间列表（每半小时一个）
  const times = []
  for (let hour = 0; hour < 24; hour++) {
    times.push(`${String(hour).padStart(2, '0')}:00`)
    times.push(`${String(hour).padStart(2, '0')}:30`)
  }
  timeList.value = times
}

// ==================== 事件处理 ====================
/**
 * 切换标签页
 */
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'records') {
    loadLeaveRecords()
  }
}

/**
 * 请假类型变更
 */
const onLeaveTypeChange = (e) => {
  leaveTypeIndex.value = e.detail.value
  leaveForm.leaveType = leaveTypes[leaveTypeIndex.value]
}

/**
 * 开始时间变更
 */
const onStartTimeChange = (e) => {
  const [dateIndex, timeIndex] = e.detail.value
  startDateIndex.value = dateIndex
  startTimeIndex.value = timeIndex

  const date = dateList.value[dateIndex]
  const time = timeList.value[timeIndex]
  leaveForm.startTime = `${date} ${time}`

  // 如果结束时间早于开始时间，自动调整
  if (leaveForm.endTime && dayjs(leaveForm.endTime).isBefore(dayjs(leaveForm.startTime))) {
    const nextDate = dayjs(date).add(1, 'day').format('YYYY-MM-DD')
    leaveForm.endTime = `${nextDate} ${time}`
    endDateIndex.value = dateIndex + 1
    endTimeIndex.value = timeIndex
  }
}

/**
 * 结束时间变更
 */
const onEndTimeChange = (e) => {
  const [dateIndex, timeIndex] = e.detail.value
  endDateIndex.value = dateIndex
  endTimeIndex.value = timeIndex

  const date = dateList.value[dateIndex]
  const time = timeList.value[timeIndex]
  leaveForm.endTime = `${date} ${time}`
}

/**
 * 选择附件
 */
const chooseAttachment = () => {
  uni.chooseImage({
    count: 3 - attachments.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePaths = res.tempFilePaths

      tempFilePaths.forEach((filePath) => {
        // TODO: 上传图片到服务器
        attachments.value.push({
          url: filePath,
          file: null
        })
      })

      uni.showToast({
        title: '图片添加成功',
        icon: 'success'
      })
    }
  })
}

/**
 * 删除附件
 */
const removeAttachment = (index) => {
  attachments.value.splice(index, 1)
}

/**
 * 选择审批人
 */
const chooseApprover = () => {
  uni.showToast({
    title: '选择审批人功能开发中',
    icon: 'none'
  })

  // TODO: 实现审批人选择
  // 1. 跳转到组织架构页面
  // 2. 选择审批人
  // 3. 返回并更新审批人列表
}

/**
 * 删除审批人
 */
const removeApprover = (index) => {
  approvers.value.splice(index, 1)
}

/**
 * 提交请假申请
 */
const submitLeave = async () => {
  if (!canSubmit.value) {
    uni.showToast({
      title: '请完善请假信息',
      icon: 'none'
    })
    return
  }

  submitting.value = true

  try {
    const res = await attendanceApi.leaveApi.applyLeave({
      employeeId: employeeId.value,
      leaveType: leaveForm.leaveType,
      startTime: leaveForm.startTime,
      endTime: leaveForm.endTime,
      duration: calculatedDuration.value,
      reason: leaveForm.reason,
      attachments: attachments.value.map(a => a.url),
      approvers: approvers.value.map(a => a.id)
    })

    if (res.success) {
      uni.showToast({
        title: '申请提交成功',
        icon: 'success'
      })

      // 重置表单
      resetForm()

      // 切换到记录标签页
      setTimeout(() => {
        switchTab('records')
      }, 1500)
    } else {
      uni.showToast({
        title: res.message || '提交失败',
        icon: 'none'
      })
    }
  } catch (error) {
    console.error('[请假申请] 提交失败:', error)
    uni.showToast({
      title: '提交失败，请稍后重试',
      icon: 'none'
    })
  } finally {
    submitting.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  leaveForm.leaveType = ''
  leaveForm.startTime = ''
  leaveForm.endTime = ''
  leaveForm.reason = ''
  leaveTypeIndex.value = 0
  attachments.value = []
  approvers.value = []
}

// ==================== 记录列表 ====================
/**
 * 加载请假记录
 */
const loadLeaveRecords = async (refresh = false) => {
  if (refresh) {
    recordPagination.pageNum = 1
    hasMoreRecords.value = true
  }

  if (loadingRecords.value) return
  if (!hasMoreRecords.value && !refresh) return

  loadingRecords.value = true

  try {
    const res = await attendanceApi.leaveApi.getLeaveRecords({
      employeeId: employeeId.value,
      status: recordFilter.value === 'all' ? undefined : recordFilter.value,
      pageSize: recordPagination.pageSize,
      pageNum: recordPagination.pageNum
    })

    if (res.success && res.data) {
      const newRecords = res.data.list || []

      if (refresh) {
        leaveRecords.value = newRecords
      } else {
        leaveRecords.value = [...leaveRecords.value, ...newRecords]
      }

      hasMoreRecords.value = newRecords.length >= recordPagination.pageSize
      recordPagination.pageNum++
    }
  } catch (error) {
    console.error('[请假记录] 加载失败:', error)
  } finally {
    loadingRecords.value = false
    refreshingRecords.value = false
  }
}

/**
 * 刷新记录
 */
const refreshRecords = () => {
  refreshingRecords.value = true
  loadLeaveRecords(true)
}

/**
 * 加载更多记录
 */
const loadMoreRecords = () => {
  if (hasMoreRecords.value && !loadingRecords.value) {
    loadLeaveRecords()
  }
}

/**
 * 设置记录筛选
 */
const setRecordFilter = (filter) => {
  recordFilter.value = filter
  loadLeaveRecords(true)
}

/**
 * 撤销请假申请
 */
const cancelLeave = async (leaveId) => {
  uni.showModal({
    title: '确认撤销',
    content: '确定要撤销此请假申请吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          const result = await attendanceApi.leaveApi.cancelLeave(leaveId, employeeId.value)

          if (result.success) {
            uni.showToast({
              title: '撤销成功',
              icon: 'success'
            })

            // 刷新列表
            loadLeaveRecords(true)
          }
        } catch (error) {
          console.error('[请假记录] 撤销失败:', error)
          uni.showToast({
            title: '撤销失败',
            icon: 'none'
          })
        }
      }
    }
  })
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'CANCELLED': '已撤销'
  }
  return statusMap[status] || '未知'
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    'PENDING': 'status-pending',
    'APPROVED': 'status-approved',
    'REJECTED': 'status-rejected',
    'CANCELLED': 'status-cancelled'
  }
  return classMap[status] || ''
}
</script>

<style lang="scss" scoped>
.leave-container {
  min-height: 100vh;
  background-color: #f5f5f5;
}

// 标签页
.tabs {
  display: flex;
  background: white;
  padding: 20rpx 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .tab-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    position: relative;

    &.active {
      .tab-text {
        color: #667eea;
        font-weight: bold;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 60rpx;
        height: 4rpx;
        background: #667eea;
        border-radius: 2rpx;
      }
    }

    .tab-text {
      font-size: 28rpx;
      color: #666;
    }
  }
}

// 申请表单
.apply-form {
  padding: 30rpx;

  .form-item {
    background: white;
    padding: 30rpx;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    .item-label {
      display: block;
      font-size: 28rpx;
      color: #333;
      font-weight: bold;
      margin-bottom: 20rpx;
    }

    .picker-value {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .value-text {
        font-size: 28rpx;
        color: #333;

        &.placeholder {
          color: #999;
        }
      }

      .arrow {
        font-size: 32rpx;
        color: #999;
      }
    }

    .duration-display {
      display: flex;
      align-items: baseline;
      justify-content: center;
      padding: 30rpx 0;

      .duration-value {
        font-size: 60rpx;
        font-weight: bold;
        color: #667eea;
      }

      .duration-unit {
        font-size: 28rpx;
        color: #666;
        margin-left: 10rpx;
      }
    }

    &.reason-item {
      .reason-input {
        width: 100%;
        min-height: 150rpx;
        padding: 20rpx;
        background-color: #f5f5f5;
        border-radius: 10rpx;
        font-size: 28rpx;
        color: #333;
      }

      .char-count {
        display: block;
        text-align: right;
        font-size: 22rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }

    &.attachment-item {
      .attachment-list {
        display: flex;
        flex-wrap: wrap;
        margin-top: 20rpx;

        .attachment-item {
          position: relative;
          width: 150rpx;
          height: 150rpx;
          margin-right: 20rpx;
          margin-bottom: 20rpx;

          .attachment-image {
            width: 100%;
            height: 100%;
            border-radius: 10rpx;
          }

          .delete-btn {
            position: absolute;
            top: -10rpx;
            right: -10rpx;
            width: 40rpx;
            height: 40rpx;
            background-color: #ef4444;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;

            .delete-icon {
              color: white;
              font-size: 28rpx;
              line-height: 1;
            }
          }
        }

        .add-attachment {
          width: 150rpx;
          height: 150rpx;
          border: 2rpx dashed #ddd;
          border-radius: 10rpx;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;

          .add-icon {
            font-size: 40rpx;
            color: #999;
            margin-bottom: 10rpx;
          }

          .add-text {
            font-size: 22rpx;
            color: #999;
          }
        }
      }

      .attachment-tip {
        display: block;
        font-size: 22rpx;
        color: #999;
        margin-top: 10rpx;
      }
    }

    .approver-list {
      display: flex;
      flex-wrap: wrap;

      .approver-item {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        margin-right: 20rpx;
        margin-bottom: 20rpx;

        .approver-avatar {
          width: 80rpx;
          height: 80rpx;
          border-radius: 50%;
          margin-bottom: 10rpx;
        }

        .approver-name {
          font-size: 22rpx;
          color: #666;
        }

        .remove-approver {
          position: absolute;
          top: -10rpx;
          right: 0;
          width: 30rpx;
          height: 30rpx;
          background-color: #ef4444;
          color: white;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 24rpx;
          line-height: 1;
        }
      }

      .add-approver {
        width: 80rpx;
        height: 80rpx;
        border: 2rpx dashed #ddd;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 20rpx;
        margin-bottom: 20rpx;

        .add-icon {
          font-size: 32rpx;
          color: #999;
        }
      }
    }

    .add-approver-btn {
      padding: 20rpx;
      border: 2rpx dashed #ddd;
      border-radius: 10rpx;
      text-align: center;

      .btn-text {
        font-size: 26rpx;
        color: #999;
      }
    }
  }

  .submit-section {
    margin-top: 40rpx;

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

      &:disabled {
        opacity: 0.5;
      }

      .btn-text {
        color: white;
      }
    }
  }

  .notice-section {
    background: white;
    padding: 30rpx;
    border-radius: 16rpx;
    margin-top: 30rpx;

    .notice-title {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;

      .title-icon {
        font-size: 24rpx;
        margin-right: 10rpx;
      }

      .title-text {
        font-size: 26rpx;
        font-weight: bold;
        color: #333;
      }
    }

    .notice-content {
      .notice-item {
        display: block;
        font-size: 24rpx;
        color: #666;
        line-height: 1.8;
        margin-bottom: 10rpx;
      }
    }
  }
}

// 记录列表
.records-list {
  height: calc(100vh - 120rpx);

  .filter-bar {
    background: white;
    padding: 20rpx 30rpx;

    .filter-tabs {
      display: flex;

      .filter-tab {
        flex: 1;
        text-align: center;
        padding: 16rpx 0;
        position: relative;

        &.active {
          .tab-text {
            color: #667eea;
            font-weight: bold;
          }

          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 40rpx;
            height: 4rpx;
            background: #667eea;
            border-radius: 2rpx;
          }
        }

        .tab-text {
          font-size: 26rpx;
          color: #666;
        }
      }
    }
  }

  .list-scroll {
    height: calc(100% - 100rpx);
    padding: 30rpx;

    .record-card {
      background: white;
      padding: 30rpx;
      border-radius: 16rpx;
      margin-bottom: 20rpx;

      .card-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 20rpx;

        .leave-type {
          font-size: 28rpx;
          font-weight: bold;
          color: #333;
        }

        .record-status {
          font-size: 22rpx;
          padding: 6rpx 16rpx;
          border-radius: 20rpx;

          &.status-pending {
            background-color: #fffbeb;
            color: #f59e0b;
          }

          &.status-approved {
            background-color: #f0fdf4;
            color: #22c55e;
          }

          &.status-rejected {
            background-color: #fef2f2;
            color: #ef4444;
          }

          &.status-cancelled {
            background-color: #f3f4f6;
            color: #9ca3af;
          }
        }
      }

      .time-info {
        margin-bottom: 20rpx;

        .time-row {
          display: flex;
          margin-bottom: 10rpx;

          .time-label {
            font-size: 24rpx;
            color: #999;
            width: 140rpx;
          }

          .time-value {
            font-size: 24rpx;
            color: #333;
          }
        }
      }

      .reason-info {
        margin-bottom: 20rpx;

        .reason-label {
          font-size: 24rpx;
          color: #999;
        }

        .reason-text {
          font-size: 24rpx;
          color: #333;
        }
      }

      .approval-info {
        margin-bottom: 20rpx;

        .approval-label {
          font-size: 24rpx;
          color: #999;
        }

        .approval-value {
          font-size: 24rpx;
          color: #333;
        }
      }

      .card-actions {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 10rpx;

        .action-btn {
          padding: 10rpx 30rpx;
          border-radius: 20rpx;
          font-size: 24rpx;
          border: none;

          &.cancel-btn {
            background-color: #f3f4f6;
            color: #666;
          }

          .btn-text {
            color: inherit;
          }
        }
      }

      .approval-comment {
        padding-top: 20rpx;
        border-top: 1rpx solid #f0f0f0;

        .comment-label {
          font-size: 24rpx;
          color: #999;
        }

        .comment-text {
          font-size: 24rpx;
          color: #333;
        }
      }
    }

    .load-more {
      padding: 30rpx;
      text-align: center;

      .load-text {
        font-size: 24rpx;
        color: #999;
      }
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 120rpx 0;

      .empty-icon {
        font-size: 80rpx;
        margin-bottom: 20rpx;
        opacity: 0.5;
      }

      .empty-text {
        font-size: 26rpx;
        color: #999;
      }
    }
  }
}
</style>
