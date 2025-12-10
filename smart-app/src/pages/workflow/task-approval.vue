<!--
  * 任务审批页面
  * 工作流任务审批操作
-->
<template>
  <view class="task-approval">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="任务审批"
      left-icon="left"
      @clickLeft="goBack"
    />

    <scroll-view scroll-y class="approval-scroll" v-if="taskInfo">
      <!-- 任务信息概览 -->
      <view class="task-overview">
        <view class="overview-header">
          <view class="task-type-tag" :class="'type-' + taskInfo.type">
            {{ taskInfo.typeName }}
          </view>
          <view class="task-priority" :class="'priority-' + taskInfo.priority">
            {{ getPriorityText(taskInfo.priority) }}
          </view>
        </view>
        <view class="task-title">{{ taskInfo.title }}</view>
        <view class="task-meta">
          <text class="meta-item">发起人：{{ taskInfo.initiatorName }}</text>
          <text class="meta-item">时间：{{ formatDateTime(taskInfo.createTime) }}</text>
        </view>
      </view>

      <!-- 任务详情 -->
      <view class="task-details">
        <view class="detail-section">
          <view class="section-title">申请内容</view>
          <view class="section-content">
            {{ taskInfo.description }}
          </view>
        </view>

        <!-- 申请表单数据 -->
        <view class="detail-section" v-if="taskInfo.formData && taskInfo.formData.length > 0">
          <view class="section-title">申请详情</view>
          <view class="form-data">
            <view
              class="form-item"
              v-for="field in taskInfo.formData"
              :key="field.key"
            >
              <text class="field-label">{{ field.label }}</text>
              <text class="field-value">{{ field.value }}</text>
            </view>
          </view>
        </view>

        <!-- 相关附件 -->
        <view class="detail-section" v-if="taskInfo.attachments && taskInfo.attachments.length > 0">
          <view class="section-title">相关附件</view>
          <view class="attachments">
            <view
              class="attachment-item"
              v-for="attachment in taskInfo.attachments"
              :key="attachment.id"
              @tap="previewAttachment(attachment)"
            >
              <view class="attachment-icon">
                <uni-icons :type="getAttachmentIcon(attachment.type)" size="24" color="#1890ff"></uni-icons>
              </view>
              <view class="attachment-info">
                <text class="attachment-name">{{ attachment.name }}</text>
                <text class="attachment-size">{{ formatFileSize(attachment.size) }}</text>
              </view>
              <view class="attachment-action">
                <uni-icons type="eye" size="20" color="#999"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批意见 -->
      <view class="approval-comment">
        <view class="section-title">审批意见</view>
        <textarea
          class="comment-input"
          v-model="approvalComment"
          placeholder="请输入审批意见（选填）"
          maxlength="500"
        ></textarea>
        <view class="comment-count">{{ approvalComment.length }}/500</view>
      </view>

      <!-- 审批操作 -->
      <view class="approval-actions">
        <button class="action-btn approve-btn" @tap="handleApprove">
          <uni-icons type="checkmarkempty" size="20" color="#fff"></uni-icons>
          同意
        </button>
        <button class="action-btn reject-btn" @tap="handleReject">
          <uni-icons type="closeempty" size="20" color="#fff"></uni-icons>
          拒绝
        </button>
        <button class="action-btn return-btn" @tap="handleReturn">
          <uni-icons type="undo" size="20" color="#fff"></uni-icons>
          退回
        </button>
      </view>

      <!-- 快捷意见 -->
      <view class="quick-comments">
        <view class="quick-title">快捷意见</view>
        <view class="quick-tags">
          <view
            class="quick-tag"
            v-for="tag in quickComments"
            :key="tag"
            @tap="addQuickComment(tag)"
          >
            {{ tag }}
          </view>
        </view>
      </view>

      <!-- 历史审批记录 -->
      <view class="approval-history" v-if="taskInfo.approvalHistory && taskInfo.approvalHistory.length > 1">
        <view class="section-title">审批记录</view>
        <view class="history-list">
          <view
            class="history-item"
            v-for="(record, index) in taskInfo.approvalHistory.slice(0, -1)"
            :key="record.id"
          >
            <view class="history-status" :class="'history-' + record.action">
              {{ getActionText(record.action) }}
            </view>
            <view class="history-info">
              <text class="history-user">{{ record.userName }}</text>
              <text class="history-time">{{ formatDateTime(record.createTime) }}</text>
            </view>
            <view class="history-comment" v-if="record.comment">
              "{{ record.comment }}"
            </view>
          </view>
        </view>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading"></uni-load-more>
    </view>

    <!-- 审批结果弹窗 -->
    <uni-popup ref="resultPopup" type="center">
      <view class="result-popup">
        <view class="result-icon" :class="'result-' + approvalResult">
          <uni-icons :type="getResultIcon(approvalResult)" size="48" color="#fff"></uni-icons>
        </view>
        <view class="result-title">{{ getResultTitle(approvalResult) }}</view>
        <view class="result-message">{{ approvalComment || '操作成功' }}</view>
        <button class="confirm-btn" @tap="closeResultPopup">确定</button>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'TaskApproval',
  setup() {
    // 响应式数据
    const loading = ref(true)
    const taskInfo = ref(null)
    const approvalComment = ref('')
    const approvalResult = ref('')
    const resultPopup = ref(null)

    // 快捷意见
    const quickComments = ref([
      '同意申请，按流程执行',
      '申请材料齐全，批准通过',
      '符合规定，同意办理',
      '材料不完整，请补充后重新提交',
      '申请理由不充分，请详细说明',
      '需要补充相关证明材料',
      '按公司规定执行，已批准',
      '情况属实，同意申请'
    ])

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const loadTaskInfo = async () => {
      loading.value = true

      try {
        // 获取传递的taskId
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const taskId = currentPage.options.taskId

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        // 生成模拟数据
        taskInfo.value = generateMockTaskInfo(taskId)

      } catch (error) {
        console.error('加载任务信息失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        loading.value = false
      }
    }

    const generateMockTaskInfo = (taskId) => {
      const mockTask = {
        taskId: taskId || 'task_001',
        title: '员工年假申请 - 张三 2024年度年假',
        type: 'leave',
        typeName: '请假审批',
        priority: 'medium',
        initiatorName: '张三',
        initiatorDept: '技术研发部',
        createTime: '2024-12-08T10:30:00Z',
        description: '本人张三申请于2024年12月10日至2024年12月12日休年假，共计3天。请假期间工作已安排妥当，不会影响正常工作流程。请领导批准，谢谢！',
        formData: [
          { key: 'leaveType', label: '请假类型', value: '年假' },
          { key: 'startDate', label: '开始时间', value: '2024-12-10' },
          { key: 'endDate', label: '结束时间', value: '2024-12-12' },
          { key: 'duration', label: '请假天数', value: '3天' },
          { key: 'reason', label: '请假事由', value: '年度休假安排' },
          { key: 'contactPhone', label: '联系电话', value: '13800138000' },
          { key: 'workArrangement', label: '工作安排', value: '工作已交接给李四同事' }
        ],
        attachments: [
          {
            id: '1',
            name: '请假申请表.pdf',
            type: 'pdf',
            size: 2048576,
            url: '/files/leave-form.pdf'
          },
          {
            id: '2',
            name: '工作安排说明.docx',
            type: 'docx',
            size: 524288,
            url: '/files/work-arrangement.docx'
          },
          {
            id: '3',
            name: '相关证明材料.jpg',
            type: 'image',
            size: 1024000,
            url: '/files/evidence.jpg'
          }
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

    const getPriorityText = (priority) => {
      const map = {
        high: '紧急',
        medium: '普通',
        low: '低级'
      }
      return map[priority] || '普通'
    }

    const getAttachmentIcon = (type) => {
      const map = {
        pdf: 'file-text',
        docx: 'file-text',
        xlsx: 'file-text',
        image: 'image',
        zip: 'folder-add'
      }
      return map[type] || 'file'
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

    const getResultIcon = (result) => {
      const map = {
        approved: 'checkmarkempty',
        rejected: 'closeempty',
        returned: 'undo'
      }
      return map[result] || 'checkmarkempty'
    }

    const getResultTitle = (result) => {
      const map = {
        approved: '审批通过',
        rejected: '审批拒绝',
        returned: '退回修改'
      }
      return map[result] || '操作成功'
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

    const addQuickComment = (comment) => {
      approvalComment.value = comment
    }

    const previewAttachment = (attachment) => {
      uni.showToast({
        title: '预览文件',
        icon: 'success'
      })
      // 实际实现中打开文件预览
    }

    const showResult = (result) => {
      approvalResult.value = result
      resultPopup.value.open()
    }

    const closeResultPopup = () => {
      resultPopup.value.close()
      setTimeout(() => {
        goBack()
      }, 500)
    }

    const handleApprove = async () => {
      if (!approvalComment.value.trim()) {
        uni.showToast({
          title: '请输入审批意见',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000))

        // 更新审批历史
        taskInfo.value.approvalHistory.push({
          id: Date.now().toString(),
          action: 'approve',
          userName: '当前用户',
          comment: approvalComment.value,
          createTime: new Date().toISOString()
        })

        uni.hideLoading()
        showResult('approved')

      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    const handleReject = async () => {
      if (!approvalComment.value.trim()) {
        uni.showToast({
          title: '请输入拒绝理由',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000))

        // 更新审批历史
        taskInfo.value.approvalHistory.push({
          id: Date.now().toString(),
          action: 'reject',
          userName: '当前用户',
          comment: approvalComment.value,
          createTime: new Date().toISOString()
        })

        uni.hideLoading()
        showResult('rejected')

      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    const handleReturn = async () => {
      if (!approvalComment.value.trim()) {
        uni.showToast({
          title: '请输入退回理由',
          icon: 'none'
        })
        return
      }

      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000))

        // 更新审批历史
        taskInfo.value.approvalHistory.push({
          id: Date.now().toString(),
          action: 'return',
          userName: '当前用户',
          comment: approvalComment.value,
          createTime: new Date().toISOString()
        })

        uni.hideLoading()
        showResult('returned')

      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    // 生命周期
    onMounted(() => {
      loadTaskInfo()
    })

    // 返回
    return {
      loading,
      taskInfo,
      approvalComment,
      quickComments,
      resultPopup,
      approvalResult,
      goBack,
      addQuickComment,
      previewAttachment,
      handleApprove,
      handleReject,
      handleReturn,
      closeResultPopup,
      getPriorityText,
      getAttachmentIcon,
      getActionText,
      getResultIcon,
      getResultTitle,
      formatDateTime,
      formatFileSize
    }
  }
}
</script>

<style lang="scss" scoped>
.task-approval {
  background-color: #f5f5f5;
  min-height: 100vh;

  .approval-scroll {
    height: calc(100vh - 44px);
    padding-bottom: 200rpx;
  }

  .task-overview {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .overview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16rpx;

      .task-type-tag {
        padding: 4rpx 12rpx;
        border-radius: 8rpx;
        font-size: 24rpx;

        &.type-leave {
          background-color: #e6f7ff;
          color: #1890ff;
        }

        &.type-expense {
          background-color: #f6ffed;
          color: #52c41a;
        }

        &.type-overtime {
          background-color: #fff7e6;
          color: #fa8c16;
        }

        &.type-business_trip {
          background-color: #f9f0ff;
          color: #722ed1;
        }

        &.type-other {
          background-color: #f0f0f0;
          color: #666;
        }
      }

      .task-priority {
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

    .task-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      line-height: 1.4;
      margin-bottom: 12rpx;
    }

    .task-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 16rpx;
      font-size: 26rpx;

      .meta-item {
        color: #666;
      }
    }
  }

  .task-details {
    margin: 16rpx;

    .detail-section {
      background-color: #fff;
      border-radius: 16rpx;
      padding: 24rpx;
      margin-bottom: 16rpx;
      box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

      .section-title {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 16rpx;
      }

      .section-content {
        font-size: 28rpx;
        line-height: 1.6;
        color: #333;
      }
    }
  }

  .form-data {
    .form-item {
      display: flex;
      padding: 16rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      .field-label {
        color: #666;
        min-width: 140rpx;
        font-size: 28rpx;
      }

      .field-value {
        color: #333;
        flex: 1;
        font-size: 28rpx;
      }
    }
  }

  .attachments {
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

      .attachment-icon {
        width: 60rpx;
        height: 60rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 16rpx;
        background-color: #e6f7ff;
        border-radius: 8rpx;
      }

      .attachment-info {
        flex: 1;

        .attachment-name {
          display: block;
          font-size: 28rpx;
          color: #333;
          margin-bottom: 4rpx;
        }

        .attachment-size {
          font-size: 24rpx;
          color: #999;
        }
      }

      .attachment-action {
        padding: 16rpx;
      }
    }
  }

  .approval-comment {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .comment-input {
      width: 100%;
      min-height: 200rpx;
      padding: 16rpx;
      border: 1px solid #e8e8e8;
      border-radius: 8rpx;
      font-size: 28rpx;
      color: #333;
      line-height: 1.5;
    }

    .comment-count {
      text-align: right;
      font-size: 24rpx;
      color: #999;
      margin-top: 8rpx;
    }
  }

  .approval-actions {
    display: flex;
    gap: 16rpx;
    margin: 16rpx;

    .action-btn {
      flex: 1;
      height: 88rpx;
      border-radius: 44rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 30rpx;
      border: none;
      color: #fff;
      font-weight: 500;
      gap: 8rpx;

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

  .quick-comments {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .quick-title {
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .quick-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 12rpx;

      .quick-tag {
        padding: 8rpx 16rpx;
        background-color: #f0f0f0;
        border-radius: 20rpx;
        font-size: 26rpx;
        color: #666;
        max-width: 100%;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  .approval-history {
    background-color: #fff;
    margin: 16rpx;
    border-radius: 16rpx;
    padding: 24rpx;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .history-list {
      .history-item {
        padding: 16rpx;
        background-color: #f8f9fa;
        border-radius: 8rpx;
        margin-bottom: 12rpx;

        &:last-child {
          margin-bottom: 0;
        }

        .history-status {
          display: inline-block;
          padding: 4rpx 12rpx;
          border-radius: 8rpx;
          font-size: 24rpx;
          margin-bottom: 12rpx;

          &.history-submit {
            background-color: #e6f7ff;
            color: #1890ff;
          }

          &.history-approve {
            background-color: #f6ffed;
            color: #52c41a;
          }

          &.history-reject {
            background-color: #fff2f0;
            color: #ff4d4f;
          }

          &.history-return {
            background-color: #fff7e6;
            color: #fa8c16;
          }
        }

        .history-info {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8rpx;
          font-size: 26rpx;

          .history-user {
            color: #333;
            font-weight: 500;
          }

          .history-time {
            color: #999;
          }
        }

        .history-comment {
          font-size: 26rpx;
          color: #666;
          line-height: 1.5;
          font-style: italic;
        }
      }
    }
  }

  .result-popup {
    background-color: #fff;
    border-radius: 16rpx;
    padding: 48rpx 32rpx;
    width: 600rpx;
    text-align: center;

    .result-icon {
      width: 96rpx;
      height: 96rpx;
      border-radius: 48rpx;
      margin: 0 auto 24rpx;
      display: flex;
      align-items: center;
      justify-content: center;

      &.result-approved {
        background-color: #52c41a;
      }

      &.result-rejected {
        background-color: #ff4d4f;
      }

      &.result-returned {
        background-color: #fa8c16;
      }
    }

    .result-title {
      font-size: 36rpx;
      font-weight: 600;
      color: #333;
      margin-bottom: 16rpx;
    }

    .result-message {
      font-size: 28rpx;
      color: #666;
      line-height: 1.5;
      margin-bottom: 32rpx;
    }

    .confirm-btn {
      background-color: #1890ff;
      color: #fff;
      border: none;
      border-radius: 8rpx;
      padding: 16rpx 48rpx;
      font-size: 28rpx;
    }
  }

  .bottom-space {
    height: 100rpx;
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200rpx;
  }
}
</style>