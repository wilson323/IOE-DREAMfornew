<template>
  <view class="task-detail-page">
    <!-- 状态栏占位 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }"></view>

    <!-- 导航栏 -->
    <view class="nav-bar">
      <view class="nav-content">
        <text class="back-btn" @click="goBack">‹</text>
        <text class="nav-title">任务详情</text>
        <text class="more-btn" @click="showMoreMenu = true">⋮</text>
      </view>
    </view>

    <!-- 任务基本信息 -->
    <view class="task-info-card">
      <view class="task-header">
        <text class="task-title">{{ taskDetail.taskName }}</text>
        <view :class="['task-status', taskDetail.status]">
          {{ getStatusText(taskDetail.status) }}
        </view>
      </view>

      <view class="task-meta">
        <view class="meta-row">
          <text class="meta-label">流程名称</text>
          <text class="meta-value">{{ taskDetail.processName }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">发起人</text>
          <text class="meta-value">{{ taskDetail.startUser }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">发起时间</text>
          <text class="meta-value">{{ formatDateTime(taskDetail.createTime) }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">当前节点</text>
          <text class="meta-value">{{ taskDetail.nodeName }}</text>
        </view>
        <view class="meta-row">
          <text class="meta-label">优先级</text>
          <view :class="['priority-badge', taskDetail.priority]">
            {{ getPriorityText(taskDetail.priority) }}
          </view>
        </view>
      </view>
    </view>

    <!-- 表单数据 -->
    <view class="form-data-card" v-if="taskDetail.formData">
      <text class="card-title">申请信息</text>
      <view class="form-content">
        <view
          v-for="(field, index) in taskDetail.formData"
          :key="index"
          class="form-field"
        >
          <text class="field-label">{{ field.label }}</text>
          <text class="field-value">{{ field.value }}</text>
        </view>
      </view>
    </view>

    <!-- 流程历史 -->
    <view class="process-history-card">
      <text class="card-title">处理历史</text>
      <view class="history-timeline">
        <view
          v-for="(history, index) in processHistory"
          :key="history.id"
          class="history-item"
        >
          <view class="timeline-dot" :class="getHistoryDotClass(history)"></view>
          <view class="history-content">
            <view class="history-header">
              <text class="history-action">{{ history.action }}</text>
              <text class="history-time">{{ formatDateTime(history.createTime) }}</text>
            </view>
            <text class="history-node">{{ history.nodeName }}</text>
            <text class="history-user">{{ history.assignee }}</text>
            <view v-if="history.comment" class="history-comment">
              <text class="comment-label">备注：</text>
              <text class="comment-content">{{ history.comment }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 附件列表 -->
    <view class="attachments-card" v-if="taskDetail.attachments && taskDetail.attachments.length > 0">
      <text class="card-title">相关附件</text>
      <view class="attachments-list">
        <view
          v-for="attachment in taskDetail.attachments"
          :key="attachment.id"
          class="attachment-item"
          @click="previewAttachment(attachment)"
        >
          <view class="attachment-icon">
            <text>{{ getFileIcon(attachment.fileName) }}</text>
          </view>
          <view class="attachment-info">
            <text class="attachment-name">{{ attachment.fileName }}</text>
            <text class="attachment-size">{{ formatFileSize(attachment.fileSize) }}</text>
          </view>
          <text class="download-icon">⬇️</text>
        </view>
      </view>
    </view>

    <!-- 操作按钮 -->
    <view class="action-buttons" v-if="showActions">
      <button class="action-btn approve" @click="showApprovalModal = true">
        通过
      </button>
      <button class="action-btn reject" @click="showRejectModal = true">
        驳回
      </button>
      <button class="action-btn delegate" @click="showDelegateModal = true">
        转办
      </button>
    </view>

    <!-- 审批弹窗 -->
    <view class="approval-modal" v-if="showApprovalModal" @click="showApprovalModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">审批确认</text>
        <view class="form-group">
          <text class="form-label">审批意见</text>
          <textarea
            class="form-textarea"
            v-model="approvalForm.comment"
            placeholder="请输入审批意见"
            maxlength="500"
          ></textarea>
        </view>
        <view class="modal-buttons">
          <button class="modal-btn cancel" @click="showApprovalModal = false">
            取消
          </button>
          <button class="modal-btn confirm approve" @click="handleApprove">
            确认通过
          </button>
        </view>
      </view>
    </view>

    <!-- 驳回弹窗 -->
    <view class="reject-modal" v-if="showRejectModal" @click="showRejectModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">驳回确认</text>
        <view class="form-group">
          <text class="form-label">驳回原因</text>
          <textarea
            class="form-textarea"
            v-model="rejectForm.comment"
            placeholder="请输入驳回原因（必填）"
            maxlength="500"
          ></textarea>
        </view>
        <view class="modal-buttons">
          <button class="modal-btn cancel" @click="showRejectModal = false">
            取消
          </button>
          <button class="modal-btn confirm reject" @click="handleReject">
            确认驳回
          </button>
        </view>
      </view>
    </view>

    <!-- 转办弹窗 -->
    <view class="delegate-modal" v-if="showDelegateModal" @click="showDelegateModal = false">
      <view class="modal-content" @click.stop>
        <text class="modal-title">转办任务</text>
        <view class="form-group">
          <text class="form-label">转办人员</text>
          <input
            class="form-input"
            v-model="delegateForm.assignee"
            placeholder="请选择转办人员"
            @click="selectAssignee"
            readonly
          />
        </view>
        <view class="form-group">
          <text class="form-label">转办说明</text>
          <textarea
            class="form-textarea"
            v-model="delegateForm.comment"
            placeholder="请输入转办说明"
            maxlength="500"
          ></textarea>
        </view>
        <view class="modal-buttons">
          <button class="modal-btn cancel" @click="showDelegateModal = false">
            取消
          </button>
          <button class="modal-btn confirm delegate" @click="handleDelegate">
            确认转办
          </button>
        </view>
      </view>
    </view>

    <!-- 更多操作菜单 -->
    <view class="more-menu-modal" v-if="showMoreMenu" @click="showMoreMenu = false">
      <view class="menu-content" @click.stop>
        <view class="menu-item" @click="viewProcessImage">
          <text class="menu-icon">📊</text>
          <text class="menu-name">流程图</text>
        </view>
        <view class="menu-item" @click="downloadAttachments">
          <text class="menu-icon">📁</text>
          <text class="menu-name">下载附件</text>
        </view>
        <view class="menu-item" @click="shareTask">
          <text class="menu-icon">🔗</text>
          <text class="menu-name">分享任务</text>
        </view>
        <view class="menu-item" @click="printTask">
          <text class="menu-icon">🖨️</text>
          <text class="menu-name">打印任务</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import oaApi from '@/api/business/oa/oa-api.js'

// 系统信息
const systemInfo = uni.getSystemInfoSync()
const statusBarHeight = ref(systemInfo.statusBarHeight || 20)

// 页面参数
const taskId = ref('')

// 页面状态
const loading = ref(false)
const showActions = ref(false)
const showApprovalModal = ref(false)
const showRejectModal = ref(false)
const showDelegateModal = ref(false)
const showMoreMenu = ref(false)

// 任务详情
const taskDetail = reactive({
  taskId: '',
  taskName: '',
  processName: '',
  nodeName: '',
  status: '',
  priority: '',
  startUser: '',
  createTime: '',
  formData: [],
  attachments: []
})

// 流程历史
const processHistory = ref([])

// 表单数据
const approvalForm = reactive({
  comment: ''
})

const rejectForm = reactive({
  comment: ''
})

const delegateForm = reactive({
  assignee: '',
  comment: ''
})

// 页面生命周期
onMounted(() => {
  init()
})

// 初始化
const init = async () => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  taskId.value = currentPage.options.taskId || ''

  if (taskId.value) {
    await loadTaskDetail()
    await loadProcessHistory()
  }
}

// 加载任务详情
const loadTaskDetail = async () => {
  try {
    loading.value = true
    const res = await oaApi.getTaskDetail(taskId.value)
    if (res.code === 1 && res.data) {
      Object.assign(taskDetail, res.data)
      showActions.value = res.data.status === 'PENDING'
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

// 加载流程历史
const loadProcessHistory = async () => {
  try {
    const res = await oaApi.getProcessHistory(taskDetail.processInstanceId || '')
    if (res.code === 1 && res.data) {
      processHistory.value = res.data
    }
  } catch (error) {
    console.error('加载流程历史失败:', error)
  }
}

// 处理通过
const handleApprove = async () => {
  if (!approvalForm.comment.trim()) {
    uni.showToast({ title: '请输入审批意见', icon: 'none' })
    return
  }

  try {
    const res = await oaApi.approveTask(taskId.value, {
      approved: true,
      comment: approvalForm.comment
    })

    if (res.code === 1) {
      uni.showToast({ title: '审批成功', icon: 'success' })
      showApprovalModal.value = false
      uni.vibrateShort()
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('审批失败:', error)
    uni.showToast({ title: '审批失败', icon: 'none' })
  }
}

// 处理驳回
const handleReject = async () => {
  if (!rejectForm.comment.trim()) {
    uni.showToast({ title: '请输入驳回原因', icon: 'none' })
    return
  }

  try {
    const res = await oaApi.approveTask(taskId.value, {
      approved: false,
      comment: rejectForm.comment
    })

    if (res.code === 1) {
      uni.showToast({ title: '驳回成功', icon: 'success' })
      showRejectModal.value = false
      uni.vibrateShort()
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('驳回失败:', error)
    uni.showToast({ title: '驳回失败', icon: 'none' })
  }
}

// 处理转办
const handleDelegate = async () => {
  if (!delegateForm.assignee.trim()) {
    uni.showToast({ title: '请选择转办人员', icon: 'none' })
    return
  }

  try {
    const res = await oaApi.delegateTask(taskId.value, {
      assignee: delegateForm.assignee,
      comment: delegateForm.comment
    })

    if (res.code === 1) {
      uni.showToast({ title: '转办成功', icon: 'success' })
      showDelegateModal.value = false
      uni.vibrateShort()
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    }
  } catch (error) {
    console.error('转办失败:', error)
    uni.showToast({ title: '转办失败', icon: 'none' })
  }
}

// 选择转办人员
const selectAssignee = () => {
  uni.navigateTo({
    url: '/pages/oa/select-assignee'
  })
}

// 预览附件
const previewAttachment = (attachment) => {
  uni.previewImage({
    urls: [attachment.fileUrl],
    current: attachment.fileUrl
  })
}

// 查看流程图
const viewProcessImage = () => {
  showMoreMenu.value = false
  uni.navigateTo({
    url: `/pages/oa/process-image?processInstanceId=${taskDetail.processInstanceId}`
  })
}

// 下载附件
const downloadAttachments = () => {
  showMoreMenu.value = false
  uni.showToast({ title: '下载功能开发中', icon: 'none' })
}

// 分享任务
const shareTask = () => {
  showMoreMenu.value = false
  uni.share({
    provider: 'weixin',
    type: 0,
    title: taskDetail.taskName,
    summary: `${taskDetail.processName} - ${taskDetail.nodeName}`
  })
}

// 打印任务
const printTask = () => {
  showMoreMenu.value = false
  uni.showToast({ title: '打印功能开发中', icon: 'none' })
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    PENDING: '待处理',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    COMPLETED: '已完成',
    TERMINATED: '已终止'
  }
  return map[status] || '未知'
}

// 获取优先级文本
const getPriorityText = (priority) => {
  const map = {
    URGENT: '紧急',
    HIGH: '重要',
    MEDIUM: '一般',
    LOW: '低'
  }
  return map[priority] || '一般'
}

// 获取历史节点样式
const getHistoryDotClass = (history) => {
  if (history.action === '通过' || history.action === '完成') {
    return 'success'
  } else if (history.action === '驳回' || history.action === '终止') {
    return 'error'
  } else {
    return 'info'
  }
}

// 获取文件图标
const getFileIcon = (fileName) => {
  const ext = fileName.split('.').pop().toLowerCase()
  const iconMap = {
    doc: '📄',
    docx: '📄',
    pdf: '📑',
    xls: '📊',
    xlsx: '📊',
    ppt: '📽️',
    pptx: '📽️',
    jpg: '🖼️',
    jpeg: '🖼️',
    png: '🖼️',
    zip: '📦',
    rar: '📦'
  }
  return iconMap[ext] || '📄'
}

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let unitIndex = 0
  while (size >= 1024 && unitIndex < units.length - 1) {
    size /= 1024
    unitIndex++
  }
  return `${size.toFixed(2)} ${units[unitIndex]}`
}

// 格式化日期时间
const formatDateTime = (datetime) => {
  if (!datetime) return ''
  const date = new Date(datetime)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.task-detail-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.status-bar {
  background: #fff;
}

.nav-bar {
  background: #fff;
  border-bottom: 1px solid #e8e8e8;

  .nav-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 32rpx;
  }

  .back-btn {
    font-size: 48rpx;
    color: rgba(0, 0, 0, 0.85);
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
  }

  .more-btn {
    font-size: 40rpx;
    color: rgba(0, 0, 0, 0.45);
  }
}

.task-info-card,
.form-data-card,
.process-history-card,
.attachments-card {
  background: #fff;
  margin: 24rpx 32rpx;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .card-title {
    font-size: 32rpx;
    font-weight: 600;
    color: rgba(0, 0, 0, 0.85);
    display: block;
    margin-bottom: 24rpx;
  }
}

.task-info-card {
  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 32rpx;

    .task-title {
      font-size: 36rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      flex: 1;
      margin-right: 16rpx;
    }

    .task-status {
      padding: 8rpx 16rpx;
      border-radius: 8rpx;
      font-size: 24rpx;
      white-space: nowrap;

      &.PENDING {
        background: #e6fffb;
        color: #00b96b;
      }

      &.APPROVED {
        background: #f6ffed;
        color: #52c41a;
      }

      &.REJECTED {
        background: #fff1f0;
        color: #ff4d4f;
      }

      &.COMPLETED {
        background: #e6f7ff;
        color: #1890ff;
      }

      &.TERMINATED {
        background: #f0f0f0;
        color: rgba(0, 0, 0, 0.45);
      }
    }
  }

  .task-meta {
    .meta-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .meta-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.45);
        width: 160rpx;
      }

      .meta-value {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        flex: 1;
        text-align: right;
      }

      .priority-badge {
        padding: 4rpx 12rpx;
        border-radius: 6rpx;
        font-size: 22rpx;
        color: #fff;

        &.URGENT { background: #ff4d4f; }
        &.HIGH { background: #fa8c16; }
        &.MEDIUM { background: #faad14; }
        &.LOW { background: #52c41a; }
      }
    }
  }
}

.form-data-card {
  .form-content {
    .form-field {
      display: flex;
      margin-bottom: 20rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .field-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.45);
        width: 160rpx;
      }

      .field-value {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        flex: 1;
      }
    }
  }
}

.process-history-card {
  .history-timeline {
    .history-item {
      display: flex;
      margin-bottom: 32rpx;

      &:last-child {
        margin-bottom: 0;
      }

      .timeline-dot {
        width: 24rpx;
        height: 24rpx;
        border-radius: 50%;
        margin-right: 24rpx;
        margin-top: 8rpx;
        flex-shrink: 0;

        &.success {
          background: #52c41a;
        }

        &.error {
          background: #ff4d4f;
        }

        &.info {
          background: #1890ff;
        }
      }

      .history-content {
        flex: 1;

        .history-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8rpx;

          .history-action {
            font-size: 28rpx;
            font-weight: 600;
            color: rgba(0, 0, 0, 0.85);
          }

          .history-time {
            font-size: 24rpx;
            color: rgba(0, 0, 0, 0.45);
          }
        }

        .history-node {
          font-size: 26rpx;
          color: rgba(0, 0, 0, 0.65);
          margin-bottom: 4rpx;
          display: block;
        }

        .history-user {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
          margin-bottom: 8rpx;
          display: block;
        }

        .history-comment {
          background: #f5f5f5;
          padding: 16rpx;
          border-radius: 8rpx;

          .comment-label {
            font-size: 24rpx;
            color: rgba(0, 0, 0, 0.45);
          }

          .comment-content {
            font-size: 26rpx;
            color: rgba(0, 0, 0, 0.65);
          }
        }
      }
    }
  }
}

.attachments-card {
  .attachments-list {
    .attachment-item {
      display: flex;
      align-items: center;
      padding: 20rpx 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }

      &:active {
        background: #f5f5f5;
      }

      .attachment-icon {
        font-size: 48rpx;
        margin-right: 24rpx;
      }

      .attachment-info {
        flex: 1;

        .attachment-name {
          font-size: 28rpx;
          color: rgba(0, 0, 0, 0.85);
          margin-bottom: 4rpx;
          display: block;
        }

        .attachment-size {
          font-size: 24rpx;
          color: rgba(0, 0, 0, 0.45);
        }
      }

      .download-icon {
        font-size: 32rpx;
        color: #1890ff;
      }
    }
  }
}

.action-buttons {
  display: flex;
  gap: 16rpx;
  padding: 24rpx 32rpx 32rpx;
  background: #fff;

  .action-btn {
    flex: 1;
    height: 88rpx;
    border: none;
    border-radius: 16rpx;
    font-size: 32rpx;
    font-weight: 600;

    &.approve {
      background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
      color: #fff;
    }

    &.reject {
      background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
      color: #fff;
    }

    &.delegate {
      background: linear-gradient(135deg, #1890ff 0%, #096dd9 100%);
      color: #fff;
    }
  }
}

.approval-modal,
.reject-modal,
.delegate-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;

  .modal-content {
    width: 90%;
    max-width: 600rpx;
    background: #fff;
    border-radius: 24rpx;
    padding: 48rpx 32rpx;

    .modal-title {
      font-size: 36rpx;
      font-weight: 600;
      color: rgba(0, 0, 0, 0.85);
      text-align: center;
      display: block;
      margin-bottom: 32rpx;
    }

    .form-group {
      margin-bottom: 32rpx;

      .form-label {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
        display: block;
        margin-bottom: 16rpx;
      }

      .form-input {
        width: 100%;
        height: 88rpx;
        border: 1px solid #d9d9d9;
        border-radius: 8rpx;
        padding: 0 24rpx;
        font-size: 28rpx;
      }

      .form-textarea {
        width: 100%;
        min-height: 200rpx;
        border: 1px solid #d9d9d9;
        border-radius: 8rpx;
        padding: 24rpx;
        font-size: 28rpx;
      }
    }

    .modal-buttons {
      display: flex;
      gap: 16rpx;

      .modal-btn {
        flex: 1;
        height: 88rpx;
        border: none;
        border-radius: 16rpx;
        font-size: 32rpx;

        &.cancel {
          background: #f0f0f0;
          color: rgba(0, 0, 0, 0.65);
        }

        &.confirm {
          color: #fff;

          &.approve {
            background: #52c41a;
          }

          &.reject {
            background: #ff4d4f;
          }

          &.delegate {
            background: #1890ff;
          }
        }
      }
    }
  }
}

.more-menu-modal {
  position: fixed;
  top: auto;
  bottom: 100rpx;
  right: 32rpx;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.15);
  z-index: 200;

  .menu-content {
    .menu-item {
      display: flex;
      align-items: center;
      padding: 24rpx 32rpx;
      min-width: 200rpx;

      &:active {
        background: #f5f5f5;
      }

      .menu-icon {
        font-size: 36rpx;
        margin-right: 24rpx;
      }

      .menu-name {
        font-size: 28rpx;
        color: rgba(0, 0, 0, 0.85);
      }
    }
  }
}
</style>