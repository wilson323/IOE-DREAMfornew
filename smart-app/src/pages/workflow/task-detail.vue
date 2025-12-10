<!--
  * 任务详情页面
  * 工作流任务详细信息展示
-->
<template>
  <view class="task-detail">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="任务详情"
      left-icon="left"
      @clickLeft="goBack"
    />

    <scroll-view scroll-y class="detail-scroll" v-if="taskDetail">
      <!-- 任务状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="'status-' + taskDetail.status">
            <uni-icons :type="getStatusIcon(taskDetail.status)" size="48" color="#fff"></uni-icons>
          </view>
          <view class="status-info">
            <view class="status-text">{{ getStatusText(taskDetail.status) }}</view>
            <view class="status-desc">{{ getStatusDesc(taskDetail.status) }}</view>
          </view>
        </view>

        <view class="result-info" v-if="taskDetail.result">
          <view class="result-badge" :class="'result-' + taskDetail.result">
            {{ getResultText(taskDetail.result) }}
          </view>
        </view>
      </view>

      <!-- 任务基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="card-title">任务信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">任务标题</text>
            <text class="info-value">{{ taskDetail.title }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">任务类型</text>
            <text class="info-value">{{ taskDetail.typeName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">优先级</text>
            <view class="priority-tag" :class="'priority-' + taskDetail.priority">
              {{ getPriorityText(taskDetail.priority) }}
            </view>
          </view>
          <view class="info-item">
            <text class="info-label">发起时间</text>
            <text class="info-value">{{ formatDateTime(taskDetail.createTime) }}</text>
          </view>
          <view class="info-item" v-if="taskDetail.completeTime">
            <text class="info-label">完成时间</text>
            <text class="info-value">{{ formatDateTime(taskDetail.completeTime) }}</text>
          </view>
        </view>
      </view>

      <!-- 任务内容 -->
      <view class="content-card">
        <view class="card-header">
          <text class="card-title">任务内容</text>
        </view>
        <view class="content-text">
          {{ taskDetail.description }}
        </view>

        <!-- 附件列表 -->
        <view class="attachment-list" v-if="taskDetail.attachments && taskDetail.attachments.length > 0">
          <view class="attachment-title">相关附件</view>
          <view
            class="attachment-item"
            v-for="attachment in taskDetail.attachments"
            :key="attachment.id"
            @tap="downloadAttachment(attachment)"
          >
            <uni-icons type="paperclip" size="18" color="#1890ff"></uni-icons>
            <text class="attachment-name">{{ attachment.name }}</text>
            <text class="attachment-size">{{ formatFileSize(attachment.size) }}</text>
          </view>
        </view>
      </view>

      <!-- 发起人信息 -->
      <view class="initiator-card">
        <view class="card-header">
          <text class="card-title">发起人信息</text>
        </view>
        <view class="initiator-info">
          <view class="initiator-avatar">
            <image class="avatar-image" :src="taskDetail.initiatorAvatar || '/static/images/default-avatar.png'"></image>
          </view>
          <view class="initiator-details">
            <view class="initiator-name">{{ taskDetail.initiatorName }}</view>
            <view class="initiator-dept">{{ taskDetail.initiatorDept }}</view>
            <view class="initiator-contact">
              <text>{{ taskDetail.initiatorPhone }}</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批记录 -->
      <view class="approval-history" v-if="taskDetail.approvalHistory && taskDetail.approvalHistory.length > 0">
        <view class="card-header">
          <text class="card-title">审批记录</text>
        </view>
        <view class="timeline">
          <view
            class="timeline-item"
            v-for="(record, index) in taskDetail.approvalHistory"
            :key="record.id"
          >
            <view class="timeline-dot" :class="'timeline-' + record.action"></view>
            <view class="timeline-content">
              <view class="timeline-header">
                <text class="timeline-action">{{ getActionText(record.action) }}</text>
                <text class="timeline-time">{{ formatDateTime(record.createTime) }}</text>
              </view>
              <view class="timeline-user">{{ record.userName }}</view>
              <view class="timeline-comment" v-if="record.comment">
                "{{ record.comment }}"
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons" v-if="taskDetail.status === 'pending'">
        <button class="action-btn approve-btn" @tap="handleApprove">
          <uni-icons type="checkmarkempty" size="16" color="#fff"></uni-icons>
          同意
        </button>
        <button class="action-btn reject-btn" @tap="handleReject">
          <uni-icons type="closeempty" size="16" color="#fff"></uni-icons>
          拒绝
        </button>
        <button class="action-btn return-btn" @tap="handleReturn">
          <uni-icons type="undo" size="16" color="#fff"></uni-icons>
          退回
        </button>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading"></uni-load-more>
    </view>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'TaskDetail',
  setup() {
    // 响应式数据
    const loading = ref(true)
    const taskDetail = ref(null)

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const loadTaskDetail = async () => {
      loading.value = true

      try {
        // 获取传递的taskId
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const taskId = currentPage.options.taskId

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        // 生成模拟数据
        taskDetail.value = generateMockTaskDetail(taskId)

      } catch (error) {
        console.error('加载任务详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        loading.value = false
      }
    }

    const generateMockTaskDetail = (taskId) => {
      const mockTask = {
        taskId: taskId || 'task_001',
        title: '员工请假申请 - 张三 2024年度年假',
        type: 'leave',
        typeName: '请假审批',
        priority: 'medium',
        status: 'pending',
        result: null,
        description: '本人张三申请于2024年12月10日至2024年12月12日休年假，共计3天。请假期间工作已安排妥当，不会影响正常工作流程。请领导批准，谢谢！',
        createTime: '2024-12-08T10:30:00Z',
        completeTime: null,
        initiatorName: '张三',
        initiatorDept: '技术研发部',
        initiatorPhone: '13800138000',
        initiatorAvatar: '/static/images/avatar/user-1.jpg',
        attachments: [
          { id: '1', name: '请假申请表.pdf', size: 2048576, url: '/files/leave-form.pdf' },
          { id: '2', name: '工作安排说明.docx', size: 524288, url: '/files/work-arrangement.docx' }
        ],
        approvalHistory: [
          {
            id: '1',
            action: 'submit',
            userName: '张三',
            comment: '提交请假申请',
            createTime: '2024-12-08T10:30:00Z'
          }
        ]
      }

      return mockTask
    }

    const getStatusIcon = (status) => {
      const map = {
        pending: 'clock',
        processing: 'loop',
        completed: 'checkmarkempty',
        cancelled: 'closeempty'
      }
      return map[status] || 'clock'
    }

    const getStatusText = (status) => {
      const map = {
        pending: '待审批',
        processing: '处理中',
        completed: '已完成',
        cancelled: '已取消'
      }
      return map[status] || '未知'
    }

    const getStatusDesc = (status) => {
      const map = {
        pending: '任务等待审批处理',
        processing: '任务正在处理中',
        completed: '任务已完成',
        cancelled: '任务已取消'
      }
      return map[status] || ''
    }

    const getResultText = (result) => {
      const map = {
        approved: '已同意',
        rejected: '已拒绝',
        returned: '已退回'
      }
      return map[result] || '未知'
    }

    const getPriorityText = (priority) => {
      const map = {
        high: '紧急',
        medium: '普通',
        low: '低级'
      }
      return map[priority] || '普通'
    }

    const getActionText = (action) => {
      const map = {
        submit: '提交申请',
        approve: '审批通过',
        reject: '审批拒绝',
        return: '退回修改',
        cancel: '撤销申请'
      }
      return map[action] || '未知操作'
    }

    const formatDateTime = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatFileSize = (bytes) => {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    }

    const downloadAttachment = (attachment) => {
      uni.showToast({
        title: '开始下载',
        icon: 'success'
      })
      // 实际实现中调用下载API
    }

    const handleApprove = () => {
      uni.showModal({
        title: '审批确认',
        content: '确认同意此任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1500))

              // 更新状态
              taskDetail.value.status = 'completed'
              taskDetail.value.result = 'approved'
              taskDetail.value.completeTime = new Date().toISOString()

              // 添加审批记录
              taskDetail.value.approvalHistory.push({
                id: Date.now().toString(),
                action: 'approve',
                userName: '当前用户',
                comment: '已同意申请',
                createTime: new Date().toISOString()
              })

              uni.hideLoading()
              uni.showToast({
                title: '已同意',
                icon: 'success'
              })

            } catch (error) {
              uni.hideLoading()
              uni.showToast({
                title: '操作失败',
                icon: 'error'
              })
            }
          }
        }
      })
    }

    const handleReject = () => {
      uni.showModal({
        title: '审批确认',
        content: '确认拒绝此任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1500))

              // 更新状态
              taskDetail.value.status = 'completed'
              taskDetail.value.result = 'rejected'
              taskDetail.value.completeTime = new Date().toISOString()

              // 添加审批记录
              taskDetail.value.approvalHistory.push({
                id: Date.now().toString(),
                action: 'reject',
                userName: '当前用户',
                comment: '已拒绝申请',
                createTime: new Date().toISOString()
              })

              uni.hideLoading()
              uni.showToast({
                title: '已拒绝',
                icon: 'success'
              })

            } catch (error) {
              uni.hideLoading()
              uni.showToast({
                title: '操作失败',
                icon: 'error'
              })
            }
          }
        }
      })
    }

    const handleReturn = () => {
      uni.showModal({
        title: '退回确认',
        content: '确认退回此任务吗？',
        success: async (res) => {
          if (res.confirm) {
            try {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 1500))

              // 更新状态
              taskDetail.value.status = 'completed'
              taskDetail.value.result = 'returned'
              taskDetail.value.completeTime = new Date().toISOString()

              // 添加审批记录
              taskDetail.value.approvalHistory.push({
                id: Date.now().toString(),
                action: 'return',
                userName: '当前用户',
                comment: '已退回修改',
                createTime: new Date().toISOString()
              })

              uni.hideLoading()
              uni.showToast({
                title: '已退回',
                icon: 'success'
              })

            } catch (error) {
              uni.hideLoading()
              uni.showToast({
                title: '操作失败',
                icon: 'error'
              })
            }
          }
        }
      })
    }

    // 生命周期
    onMounted(() => {
      loadTaskDetail()
    })

    // 返回
    return {
      loading,
      taskDetail,
      goBack,
      handleApprove,
      handleReject,
      handleReturn,
      downloadAttachment,
      getStatusIcon,
      getStatusText,
      getStatusDesc,
      getResultText,
      getPriorityText,
      getActionText,
      formatDateTime,
      formatFileSize
    }
  }
}
</script>

<style lang="scss" scoped>
.task-detail {
  background-color: #f5f5f5;
  min-height: 100vh;

  .detail-scroll {
    height: calc(100vh - 44px);
    padding-bottom: 120rpx;
  }

  .status-card {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 32rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .status-header {
      display: flex;
      align-items: center;
      margin-bottom: 24rpx;

      .status-icon {
        width: 96rpx;
        height: 96rpx;
        border-radius: 48rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 24rpx;

        &.status-pending {
          background-color: #faad14;
        }

        &.status-processing {
          background-color: #1890ff;
        }

        &.status-completed {
          background-color: #52c41a;
        }

        &.status-cancelled {
          background-color: #ff4d4f;
        }
      }

      .status-info {
        flex: 1;

        .status-text {
          font-size: 36rpx;
          font-weight: 600;
          color: #333;
          margin-bottom: 8rpx;
        }

        .status-desc {
          font-size: 28rpx;
          color: #666;
        }
      }
    }

    .result-info {
      text-align: center;

      .result-badge {
        display: inline-block;
        padding: 8rpx 24rpx;
        border-radius: 20rpx;
        font-size: 28rpx;
        font-weight: 500;

        &.result-approved {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.result-rejected {
          background-color: #fff2f0;
          color: #ff4d4f;
        }

        &.result-returned {
          background-color: #fff7e6;
          color: #fa8c16;
        }
      }
    }
  }

  .info-card,
  .content-card,
  .initiator-card,
  .approval-history {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .card-header {
      margin-bottom: 24rpx;

      .card-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
      }
    }
  }

  .info-list {
    .info-item {
      display: flex;
      align-items: center;
      margin-bottom: 20rpx;
      font-size: 28rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .info-label {
        color: #999;
        min-width: 140rpx;
      }

      .info-value {
        color: #333;
        flex: 1;
      }

      .priority-tag {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 24rpx;

        &.priority-high {
          background-color: #fff2e8;
          color: #ff4d4f;
        }

        &.priority-medium {
          background-color: #e6f7ff;
          color: #1890ff;
        }

        &.priority-low {
          background-color: #f6ffed;
          color: #52c41a;
        }
      }
    }
  }

  .content-text {
    font-size: 28rpx;
    line-height: 1.6;
    color: #333;
    margin-bottom: 24rpx;
  }

  .attachment-list {
    .attachment-title {
      font-size: 28rpx;
      font-weight: 500;
      color: #333;
      margin-bottom: 16rpx;
    }

    .attachment-item {
      display: flex;
      align-items: center;
      padding: 16rpx;
      background-color: #f8f9fa;
      border-radius: 8rpx;
      margin-bottom: 12rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .attachment-name {
        flex: 1;
        font-size: 26rpx;
        color: #333;
        margin: 0 16rpx;
      }

      .attachment-size {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .initiator-info {
    display: flex;
    align-items: center;

    .initiator-avatar {
      width: 80rpx;
      height: 80rpx;
      border-radius: 40rpx;
      overflow: hidden;
      margin-right: 24rpx;

      .avatar-image {
        width: 100%;
        height: 100%;
      }
    }

    .initiator-details {
      flex: 1;

      .initiator-name {
        font-size: 32rpx;
        font-weight: 500;
        color: #333;
        margin-bottom: 8rpx;
      }

      .initiator-dept {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 8rpx;
      }

      .initiator-contact {
        font-size: 24rpx;
        color: #999;
      }
    }
  }

  .timeline {
    .timeline-item {
      display: flex;
      margin-bottom: 32rpx;
      position: relative;

      &:last-child {
        margin-bottom: 0;
      }

      .timeline-dot {
        width: 20rpx;
        height: 20rpx;
        border-radius: 10rpx;
        margin-right: 24rpx;
        margin-top: 6rpx;
        flex-shrink: 0;

        &.timeline-submit {
          background-color: #1890ff;
        }

        &.timeline-approve {
          background-color: #52c41a;
        }

        &.timeline-reject {
          background-color: #ff4d4f;
        }

        &.timeline-return {
          background-color: #fa8c16;
        }
      }

      .timeline-content {
        flex: 1;
        padding-bottom: 8rpx;
        border-bottom: 1px solid #f0f0f0;

        .timeline-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8rpx;

          .timeline-action {
            font-size: 28rpx;
            font-weight: 500;
            color: #333;
          }

          .timeline-time {
            font-size: 24rpx;
            color: #999;
          }
        }

        .timeline-user {
          font-size: 26rpx;
          color: #666;
          margin-bottom: 8rpx;
        }

        .timeline-comment {
          font-size: 26rpx;
          color: #333;
          line-height: 1.5;
          font-style: italic;
        }
      }
    }
  }

  .action-buttons {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    background-color: #fff;
    padding: 24rpx 32rpx;
    border-top: 1px solid #f0f0f0;
    display: flex;
    gap: 16rpx;

    .action-btn {
      flex: 1;
      height: 80rpx;
      border-radius: 40rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28rpx;
      border: none;
      color: #fff;
      font-weight: 500;

      &.approve-btn {
        background-color: #52c41a;
      }

      &.reject-btn {
        background-color: #ff4d4f;
      }

      &.return-btn {
        background-color: #fa8c16;
      }

      &:active {
        opacity: 0.8;
      }
    }
  }

  .bottom-space {
    height: 120rpx;
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200rpx;
  }
}
</style>