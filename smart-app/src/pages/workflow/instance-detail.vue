<!--
  * 实例详情页面
  * 工作流实例详细信息展示
-->
<template>
  <view class="instance-detail">
    <!-- 自定义导航栏 -->
    <uni-nav-bar
      :fixed="true"
      :shadow="false"
      :border="false"
      background-color="#ffffff"
      title="实例详情"
      left-icon="left"
      @clickLeft="goBack"
    />

    <scroll-view scroll-y class="detail-scroll" v-if="instanceDetail">
      <!-- 实例状态卡片 -->
      <view class="status-card">
        <view class="status-header">
          <view class="status-icon" :class="'status-' + instanceDetail.status">
            <uni-icons :type="getStatusIcon(instanceDetail.status)" size="48" color="#fff"></uni-icons>
          </view>
          <view class="status-info">
            <view class="status-text">{{ getStatusText(instanceDetail.status) }}</view>
            <view class="workflow-name">{{ instanceDetail.workflowName }}</view>
          </view>
        </view>

        <!-- 进度展示 -->
        <view class="progress-overview" v-if="instanceDetail.progress">
          <view class="progress-header">
            <text class="progress-label">流程进度</text>
            <text class="progress-value">{{ instanceDetail.progress.completed }}/{{ instanceDetail.progress.total }}</text>
          </view>
          <view class="progress-bar">
            <view
              class="progress-fill"
              :style="{ width: (instanceDetail.progress.completed / instanceDetail.progress.total * 100) + '%' }"
            ></view>
          </view>
          <view class="progress-percentage">
            {{ Math.round(instanceDetail.progress.completed / instanceDetail.progress.total * 100) }}%
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="info-card">
        <view class="card-header">
          <text class="card-title">基本信息</text>
        </view>
        <view class="info-list">
          <view class="info-item">
            <text class="info-label">实例标题</text>
            <text class="info-value">{{ instanceDetail.title }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">流程名称</text>
            <text class="info-value">{{ instanceDetail.workflowName }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">发起人</text>
            <text class="info-value">{{ instanceDetail.initiatorName }} ({{ instanceDetail.initiatorDept }})</text>
          </view>
          <view class="info-item">
            <text class="info-label">发起时间</text>
            <text class="info-value">{{ formatDateTime(instanceDetail.startTime) }}</text>
          </view>
          <view class="info-item" v-if="instanceDetail.endTime">
            <text class="info-label">完成时间</text>
            <text class="info-value">{{ formatDateTime(instanceDetail.endTime) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">持续时间</text>
            <text class="info-value">{{ calculateDuration(instanceDetail.startTime, instanceDetail.endTime) }}</text>
          </view>
          <view class="info-item">
            <text class="info-label">当前节点</text>
            <text class="info-value current-node">{{ instanceDetail.currentNodeName }}</text>
          </view>
        </view>
      </view>

      <!-- 流程节点图 -->
      <view class="workflow-nodes">
        <view class="card-header">
          <text class="card-title">流程节点</text>
        </view>
        <view class="nodes-container">
          <view
            class="node-item"
            v-for="(node, index) in instanceDetail.nodes"
            :key="node.nodeId"
            :class="{ 'node-completed': node.completed, 'node-current': node.isCurrent }"
          >
            <!-- 节点图标 -->
            <view class="node-icon">
              <uni-icons
                :type="getNodeIcon(node.completed, node.isCurrent)"
                size="24"
                :color="getNodeColor(node.completed, node.isCurrent)"
              ></uni-icons>
            </view>

            <!-- 节点信息 -->
            <view class="node-info">
              <view class="node-title">{{ node.nodeName }}</view>
              <view class="node-time" v-if="node.completedTime">
                {{ formatDateTime(node.completedTime) }}
              </view>
            </view>

            <!-- 处理人信息 -->
            <view class="node-assignee" v-if="node.assigneeName">
              <image class="assignee-avatar" :src="node.assigneeAvatar || '/static/images/default-avatar.png'"></image>
              <text class="assignee-name">{{ node.assigneeName }}</text>
            </view>

            <!-- 连接线 -->
            <view class="node-connector" v-if="index < instanceDetail.nodes.length - 1"></view>
          </view>
        </view>
      </view>

      <!-- 申请人信息 -->
      <view class="applicant-card">
        <view class="card-header">
          <text class="card-title">申请人信息</text>
        </view>
        <view class="applicant-info">
          <view class="applicant-avatar">
            <image class="avatar-image" :src="instanceDetail.initiatorAvatar || '/static/images/default-avatar.png'"></image>
          </view>
          <view class="applicant-details">
            <view class="applicant-name">{{ instanceDetail.initiatorName }}</view>
            <view class="applicant-dept">{{ instanceDetail.initiatorDept }}</view>
            <view class="applicant-contact">
              <text class="contact-item" v-if="instanceDetail.initiatorPhone">
                <uni-icons type="phone" size="14" color="#1890ff"></uni-icons>
                {{ instanceDetail.initiatorPhone }}
              </text>
              <text class="contact-item" v-if="instanceDetail.initiatorEmail">
                <uni-icons type="email" size="14" color="#1890ff"></uni-icons>
                {{ instanceDetail.initiatorEmail }}
              </text>
            </view>
          </view>
        </view>
      </view>

      <!-- 表单数据 -->
      <view class="form-data-card" v-if="instanceDetail.formData && instanceDetail.formData.length > 0">
        <view class="card-header">
          <text class="card-title">申请数据</text>
        </view>
        <view class="form-data">
          <view
            class="form-item"
            v-for="field in instanceDetail.formData"
            :key="field.key"
          >
            <text class="field-label">{{ field.label }}</text>
            <text class="field-value">{{ field.value }}</text>
          </view>
        </view>
      </view>

      <!-- 相关附件 -->
      <view class="attachments-card" v-if="instanceDetail.attachments && instanceDetail.attachments.length > 0">
        <view class="card-header">
          <text class="card-title">相关附件</text>
        </view>
        <view class="attachments">
          <view
            class="attachment-item"
            v-for="attachment in instanceDetail.attachments"
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
              <uni-icons type="download" size="20" color="#999"></uni-icons>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批历史 -->
      <view class="approval-history-card" v-if="instanceDetail.approvalHistory && instanceDetail.approvalHistory.length > 0">
        <view class="card-header">
          <text class="card-title">审批历史</text>
        </view>
        <view class="timeline">
          <view
            class="timeline-item"
            v-for="(record, index) in instanceDetail.approvalHistory"
            :key="record.id"
          >
            <view class="timeline-dot" :class="'timeline-' + record.action"></view>
            <view class="timeline-content">
              <view class="timeline-header">
                <text class="timeline-action">{{ getActionText(record.action) }}</text>
                <text class="timeline-time">{{ formatDateTime(record.createTime) }}</text>
              </view>
              <view class="timeline-user">{{ record.userName }}</view>
              <view class="timeline-node">{{ record.nodeName }}</view>
              <view class="timeline-comment" v-if="record.comment">
                "{{ record.comment }}"
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="action-buttons" v-if="instanceDetail.status === 'running'">
        <button class="action-btn pause-btn" @tap="handlePause">
          <uni-icons type="pause" size="16" color="#fff"></uni-icons>
          暂停
        </button>
        <button class="action-btn cancel-btn" @tap="handleCancel">
          <uni-icons type="close" size="16" color="#fff"></uni-icons>
          终止
        </button>
        <button class="action-btn restart-btn" @tap="handleRestart">
          <uni-icons type="refresh" size="16" color="#fff"></uni-icons>
          重启
        </button>
      </view>

      <!-- 底部间距 -->
      <view class="bottom-space"></view>
    </scroll-view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <uni-load-more status="loading"></uni-load-more>
    </view>

    <!-- 操作结果弹窗 -->
    <uni-popup ref="resultPopup" type="center">
      <view class="result-popup">
        <view class="result-icon" :class="'result-' + actionResult">
          <uni-icons :type="getActionResultIcon(actionResult)" size="48" color="#fff"></uni-icons>
        </view>
        <view class="result-title">{{ getActionResultTitle(actionResult) }}</view>
        <view class="result-message">{{ resultMessage || '操作成功' }}</view>
        <button class="confirm-btn" @tap="closeResultPopup">确定</button>
      </view>
    </uni-popup>
  </view>
</template>

<script>
import { ref, onMounted } from 'vue'

export default {
  name: 'InstanceDetail',
  setup() {
    // 响应式数据
    const loading = ref(true)
    const instanceDetail = ref(null)
    const actionResult = ref('')
    const resultMessage = ref('')
    const resultPopup = ref(null)

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const loadInstanceDetail = async () => {
      loading.value = true

      try {
        // 获取传递的instanceId
        const pages = getCurrentPages()
        const currentPage = pages[pages.length - 1]
        const instanceId = currentPage.options.instanceId

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 1500))

        // 生成模拟数据
        instanceDetail.value = generateMockInstanceDetail(instanceId)

      } catch (error) {
        console.error('加载实例详情失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        loading.value = false
      }
    }

    const generateMockInstanceDetail = (instanceId) => {
      const mockInstance = {
        instanceId: instanceId || 'instance_001',
        title: '员工年假申请流程 - 张三 2024年度年假',
        workflowName: '员工请假申请流程',
        status: 'running',
        initiatorName: '张三',
        initiatorDept: '技术研发部',
        initiatorPhone: '13800138000',
        initiatorEmail: 'zhangsan@company.com',
        initiatorAvatar: '/static/images/avatar/user-1.jpg',
        startTime: '2024-12-08T10:30:00Z',
        endTime: null,
        currentNodeName: '部门审批',
        progress: {
          total: 5,
          completed: 2
        },
        nodes: [
          {
            nodeId: 'node_1',
            nodeName: '发起申请',
            completed: true,
            completedTime: '2024-12-08T10:30:00Z',
            assigneeName: '张三',
            assigneeAvatar: '/static/images/avatar/user-1.jpg',
            isCurrent: false
          },
          {
            nodeId: 'node_2',
            nodeName: '部门审批',
            completed: true,
            completedTime: '2024-12-08T14:30:00Z',
            assigneeName: '李经理',
            assigneeAvatar: '/static/images/avatar/user-2.jpg',
            isCurrent: false
          },
          {
            nodeId: 'node_3',
            nodeName: '主管审批',
            completed: false,
            completedTime: null,
            assigneeName: '王总监',
            assigneeAvatar: '/static/images/avatar/user-3.jpg',
            isCurrent: true
          },
          {
            nodeId: 'node_4',
            nodeName: 'HR审批',
            completed: false,
            completedTime: null,
            assigneeName: '赵HR',
            assigneeAvatar: '/static/images/avatar/user-4.jpg',
            isCurrent: false
          },
          {
            nodeId: 'node_5',
            nodeName: '完成',
            completed: false,
            completedTime: null,
            isCurrent: false
          }
        ],
        formData: [
          { key: 'leaveType', label: '请假类型', value: '年假' },
          { key: 'startDate', label: '开始时间', value: '2024-12-10' },
          { key: 'endDate', label: '结束时间', value: '2024-12-12' },
          { key: 'duration', label: '请假天数', value: '3天' },
          { key: 'reason', label: '请假事由', value: '年度休假安排' },
          { key: 'contactPhone', label: '联系电话', value: '13800138000' },
          { key: 'emergencyContact', label: '紧急联系人', value: '李四 13900139000' },
          { key: 'workArrangement', label: '工作安排', value: '工作已交接给李四同事' },
          { key: 'backupContact', label: '备用联系人', value: '王五 13900149000' }
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
            nodeName: '发起申请',
            comment: '提交年假申请，共3天',
            createTime: '2024-12-08T10:30:00Z'
          },
          {
            id: '2',
            action: 'approve',
            userName: '李经理',
            nodeName: '部门审批',
            comment: '部门工作已安排妥当，同意申请',
            createTime: '2024-12-08T14:30:00Z'
          }
        ]
      }

      return mockInstance
    }

    const getStatusIcon = (status) => {
      const map = {
        running: 'loop',
        completed: 'checkmarkempty',
        paused: 'pause',
        cancelled: 'closeempty'
      }
      return map[status] || 'loop'
    }

    const getStatusText = (status) => {
      const map = {
        running: '运行中',
        completed: '已完成',
        paused: '已暂停',
        cancelled: '已终止'
      }
      return map[status] || '未知'
    }

    const getNodeIcon = (completed, isCurrent) => {
      if (isCurrent) {
        return 'loop'
      } else if (completed) {
        return 'checkmarkempty'
      } else {
        return 'circle'
      }
    }

    const getNodeColor = (completed, isCurrent) => {
      if (isCurrent) {
        return '#1890ff'
      } else if (completed) {
        return '#52c41a'
      } else {
        return '#d9d9d9'
      }
    }

    const getActionText = (action) => {
      const map = {
        submit: '提交申请',
        approve: '审批通过',
        reject: '审批拒绝',
        return: '退回修改',
        pause: '暂停流程',
        restart: '重启流程',
        cancel: '终止流程'
      }
      return map[action] || '未知操作'
    }

    const getActionResultIcon = (result) => {
      const map = {
        paused: 'pause',
        cancelled: 'closeempty',
        restarted: 'refresh'
      }
      return map[result] || 'checkmarkempty'
    }

    const getActionResultTitle = (result) => {
      const map = {
        paused: '已暂停',
        cancelled: '已终止',
        restarted: '已重启'
      }
      return map[result] || '操作成功'
    }

    const calculateDuration = (startTime, endTime) => {
      const start = new Date(startTime)
      const end = endTime ? new Date(endTime) : new Date()
      const diff = end - start
      const hours = Math.floor(diff / (1000 * 60 * 60))
      const days = Math.floor(hours / 24)

      if (days > 0) {
        return `${days}天${hours % 24}小时`
      } else if (hours > 0) {
        return `${hours}小时`
      } else {
        const minutes = Math.floor(diff / (1000 * 60))
        return `${minutes}分钟`
      }
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

    const previewAttachment = (attachment) => {
      uni.showToast({
        title: '预览文件',
        icon: 'success'
      })
      // 实际实现中打开文件预览
    }

    const showResult = (result, message) => {
      actionResult.value = result
      resultMessage.value = message
      resultPopup.value.open()
    }

    const closeResultPopup = () => {
      resultPopup.value.close()
      // 重新加载数据
      loadInstanceDetail()
    }

    const handlePause = async () => {
      try {
        uni.showLoading({
          title: '处理中...'
        })

        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 2000))

        // 更新状态
        instanceDetail.value.status = 'paused'

        uni.hideLoading()
        showResult('paused', '流程已暂停，可在操作历史中恢复')

      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    const handleCancel = async () => {
      try {
        uni.showModal({
          title: '确认终止',
          content: '确认终止此流程实例吗？此操作不可恢复。',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 2000))

              // 更新状态
              instanceDetail.value.status = 'cancelled'
              instanceDetail.value.endTime = new Date().toISOString()
              instanceDetail.value.currentNodeName = '已终止'

              uni.hideLoading()
              showResult('cancelled', '流程已终止')

              // 更新节点状态
              instanceDetail.value.nodes.forEach(node => {
                if (!node.completed) {
                  node.completed = false
                  node.isCurrent = false
                }
              })
            }
          }
        })
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    const handleRestart = async () => {
      try {
        uni.showModal({
          title: '确认重启',
          content: '确认重启此流程实例吗？流程将从当前节点继续执行。',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '处理中...'
              })

              // 模拟API调用
              await new Promise(resolve => setTimeout(resolve, 2000))

              // 更新状态
              instanceDetail.value.status = 'running'

              uni.hideLoading()
              showResult('restarted', '流程已重启')

              // 恢复节点状态
              let currentNodeFound = false
              instanceDetail.value.nodes.forEach(node => {
                if (!node.completed && !currentNodeFound) {
                  node.isCurrent = true
                  currentNodeFound = true
                } else {
                  node.isCurrent = false
                }
              })
            }
          }
        })
      } catch (error) {
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      }
    }

    // 生命周期
    onMounted(() => {
      loadInstanceDetail()
    })

    // 返回
    return {
      loading,
      instanceDetail,
      actionResult,
      resultMessage,
      resultPopup,
      goBack,
      handlePause,
      handleCancel,
      handleRestart,
      closeResultPopup,
      previewAttachment,
      showResult,
      getStatusIcon,
      getStatusText,
      getNodeIcon,
      getNodeColor,
      getActionText,
      getActionResultIcon,
      getActionResultTitle,
      calculateDuration,
      formatDateTime,
      formatFileSize,
      getAttachmentIcon
    }
  }
}
</script>

<style lang="scss" scoped>
.instance-detail {
  background-color: #f5f5f5;
  min-height: 100vh;

  .detail-scroll {
    height: calc(100vh - 44px);
    padding-bottom: 200rpx;
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

        &.status-running {
          background-color: #1890ff;
        }

        &.status-completed {
          background-color: #52c41a;
        }

        &.status-paused {
          background-color: #fa8c16;
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

        .workflow-name {
          font-size: 28rpx;
          color: #666;
        }
      }
    }

    .progress-overview {
      .progress-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16rpx;

        .progress-label {
          font-size: 28rpx;
          font-weight: 500;
          color: #333;
        }

        .progress-value {
          font-size: 26rpx;
          color: #1890ff;
          font-weight: 500;
        }
      }

      .progress-bar {
        height: 16rpx;
        background-color: #f0f0f0;
        border-radius: 8rpx;
        overflow: hidden;
        margin-bottom: 8rpx;

        .progress-fill {
          height: 100%;
          background-color: #52c41a;
          transition: width 0.3s ease;
        }
      }

      .progress-percentage {
        text-align: right;
        font-size: 24rpx;
        color: #52c41a;
        font-weight: 500;
      }
    }
  }

  .info-card,
  .applicant-card,
  .form-data-card,
  .attachments-card,
  .approval-history-card {
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

        &.current-node {
          color: #1890ff;
          font-weight: 500;
        }
      }
    }
  }

  .workflow-nodes {
    .nodes-container {
      position: relative;
      padding: 16rpx 0;

      .node-item {
        display: flex;
        align-items: flex-start;
        margin-bottom: 32rpx;
        position: relative;

        .node-icon {
          width: 64rpx;
          height: 64rpx;
          border-radius: 32rpx;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 16rpx;
          background-color: #f8f9fa;
          flex-shrink: 0;
          z-index: 2;
        }

        .node-info {
          flex: 1;
          margin-top: 8rpx;

          .node-title {
            font-size: 28rpx;
            font-weight: 500;
            color: #333;
            margin-bottom: 4rpx;
          }

          .node-time {
            font-size: 24rpx;
            color: #999;
          }
        }

        .node-assignee {
          display: flex;
          align-items: center;
          margin-top: 8rpx;

          .assignee-avatar {
            width: 40rpx;
            height: 40rpx;
            border-radius: 20rpx;
            overflow: hidden;
            margin-right: 12rpx;

            .avatar-image {
              width: 100%;
              height: 100%;
            }
          }

          .assignee-name {
            font-size: 24rpx;
            color: #666;
          }
        }

        .node-connector {
          position: absolute;
          left: 32rpx;
          top: 64rpx;
          width: 2rpx;
          height: calc(100% + 32rpx);
          background-color: #e8e8e8;
          z-index: 1;
        }

        &.node-completed {
          .node-icon {
            background-color: #f6ffed;
          }
        }

        &.node-current {
          .node-icon {
            background-color: #e6f7ff;
            animation: pulse 2s infinite;
          }
        }
      }
    }
  }

  .applicant-info {
    display: flex;
    align-items: center;

    .applicant-avatar {
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

    .applicant-details {
      flex: 1;

      .applicant-name {
        font-size: 32rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .applicant-dept {
        font-size: 26rpx;
        color: #666;
        margin-bottom: 8rpx;
      }

      .applicant-contact {
        display: flex;
        flex-wrap: wrap;
        gap: 16rpx;

        .contact-item {
          display: flex;
          align-items: center;
          font-size: 24rpx;
          color: #666;
        }
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
        color: #999;
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

  .approval-history {
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

          &.timeline-pause {
            background-color: #fa8c16;
          }

          &.timeline-cancel {
            background-color: #ff4d4f;
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
            color: #333;
            font-weight: 500;
            margin-bottom: 8rpx;
          }

          .timeline-node {
            font-size: 24rpx;
            color: #666;
            margin-bottom: 8rpx;
          }

          .timeline-comment {
            font-size: 26rpx;
            color: #666;
            line-height: 1.5;
            font-style: italic;
          }
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

      &.pause-btn {
        background-color: #fa8c16;
      }

      &.cancel-btn {
        background-color: #ff4d4f;
      }

      &.restart-btn {
        background-color: #52c41a;
      }

      &:active {
        opacity: 0.8;
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

      &.result-paused {
        background-color: #fa8c16;
      }

      &.result-cancelled {
        background-color: #ff4d4f;
      }

      &.result-restarted {
        background-color: #52c41a;
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
    height: 120rpx;
  }

  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 200rpx;
  }

  @keyframes pulse {
    0% {
      transform: scale(1);
      opacity: 1;
    }
    50% {
      transform: scale(1.1);
      opacity: 0.8;
    }
    100% {
      transform: scale(1);
      opacity: 1;
    }
  }
}
</style>