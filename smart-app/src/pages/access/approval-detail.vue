<template>
  <view class="approval-detail-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">审批详情</view>
      <view class="nav-right">
        <text class="more-btn" @click="showMoreMenu">更多</text>
      </view>
    </view>

    <!-- 内容滚动区 -->
    <scroll-view scroll-y class="content-scroll">
      <!-- 状态卡片 -->
      <view class="status-card" :class="'status-' + approvalDetail.approvalStatus">
        <view class="status-icon">
          <uni-icons :type="getStatusIcon(approvalDetail.approvalStatus)" size="40" color="#fff"></uni-icons>
        </view>
        <view class="status-info">
          <view class="status-text">{{ getStatusText(approvalDetail.approvalStatus) }}</view>
          <view class="status-desc">{{ getStatusDesc(approvalDetail.approvalStatus) }}</view>
        </view>
      </view>

      <!-- 申请信息 -->
      <view class="info-section">
        <view class="section-title">申请信息</view>

        <!-- 申请人信息 -->
        <view class="applicant-card">
          <image :src="approvalDetail.applicantAvatar || '/static/images/default-avatar.png'" class="applicant-avatar"></image>
          <view class="applicant-details">
            <view class="applicant-name">{{ approvalDetail.applicantName }}</view>
            <view class="applicant-dept">{{ approvalDetail.departmentName }}</view>
          </view>
          <view class="apply-time">{{ formatTime(approvalDetail.createTime) }}</view>
        </view>

        <!-- 申请类型 -->
        <view class="info-item">
          <view class="item-label">申请类型</view>
          <view class="item-value">
            <uni-icons :type="getTypeIcon(approvalDetail.approvalType)" size="18" color="#1890ff"></uni-icons>
            <text class="value-text">{{ getTypeText(approvalDetail.approvalType) }}</text>
          </view>
        </view>

        <!-- 申请理由 -->
        <view class="info-item">
          <view class="item-label">申请理由</view>
          <view class="item-value value-full">{{ approvalDetail.applyReason || '无' }}</view>
        </view>

        <!-- 访问区域 -->
        <view v-if="approvalDetail.areaName" class="info-item">
          <view class="item-label">访问区域</view>
          <view class="item-value">{{ approvalDetail.areaName }}</view>
        </view>

        <!-- 预期时间 -->
        <view v-if="approvalDetail.expectedTime" class="info-item">
          <view class="item-label">预期时间</view>
          <view class="item-value">{{ formatExpectedTime(approvalDetail.expectedTime) }}</view>
        </view>

        <!-- 有效期 -->
        <view v-if="approvalDetail.expiryTime" class="info-item">
          <view class="item-label">有效期至</view>
          <view class="item-value">{{ formatExpiryTime(approvalDetail.expiryTime) }}</view>
        </view>

        <!-- 访客信息（访客预约） -->
        <view v-if="approvalDetail.approvalType === 2 && approvalDetail.visitorName">
          <view class="subsection-title">访客信息</view>

          <view class="info-item">
            <view class="item-label">访客姓名</view>
            <view class="item-value">{{ approvalDetail.visitorName }}</view>
          </view>

          <view class="info-item">
            <view class="item-label">访客手机</view>
            <view class="item-value">{{ approvalDetail.visitorPhone }}</view>
          </view>

          <view v-if="approvalDetail.visitorCompany" class="info-item">
            <view class="item-label">访客单位</view>
            <view class="item-value">{{ approvalDetail.visitorCompany }}</view>
          </view>

          <view class="info-item">
            <view class="item-label">同行人数</view>
            <view class="item-value">{{ approvalDetail.visitorCount || 1 }}人</view>
          </view>
        </view>

        <!-- 紧急信息（紧急权限） -->
        <view v-if="approvalDetail.approvalType === 3">
          <view class="subsection-title">紧急信息</view>

          <view class="info-item">
            <view class="item-label">紧急程度</view>
            <view :class="['item-value', 'urgency-' + approvalDetail.urgencyLevel]">
              {{ getUrgencyText(approvalDetail.urgencyLevel) }}
            </view>
          </view>

          <view class="info-item">
            <view class="item-label">紧急联系人</view>
            <view class="item-value">{{ approvalDetail.emergencyContact }}</view>
          </view>

          <view class="info-item">
            <view class="item-label">联系电话</view>
            <view class="item-value">{{ approvalDetail.emergencyPhone }}</view>
          </view>
        </view>
      </view>

      <!-- 审批流程 -->
      <view class="flow-section">
        <view class="section-title">审批流程</view>

        <view class="timeline">
          <view v-for="(step, index) in approvalFlow" :key="index" class="timeline-item">
            <!-- 时间线 -->
            <view v-if="index > 0" class="timeline-line" :class="{ 'line-active': step.completed }"></view>

            <!-- 节点图标 -->
            <view class="timeline-icon" :class="getStepClass(step.status)">
              <uni-icons v-if="step.status === 2" type="checkmarkempty" size="18" color="#fff"></uni-icons>
              <uni-icons v-else-if="step.status === 3" type="closeempty" size="18" color="#fff"></uni-icons>
              <view v-else class="step-number">{{ index + 1 }}</view>
            </view>

            <!-- 节点内容 -->
            <view class="timeline-content">
              <view class="step-title">{{ step.title }}</view>
              <view class="step-approver" v-if="step.approver">
                <text>审批人：{{ step.approver }}</text>
              </view>
              <view class="step-time" v-if="step.approvalTime">
                <text>{{ formatTime(step.approvalTime) }}</text>
              </view>
              <view v-if="step.comment" class="step-comment">
                <text>{{ step.comment }}</text>
              </view>
              <view v-if="step.status === 0" class="step-status pending">
                <text>等待中</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 审批历史 -->
      <view class="history-section">
        <view class="section-title">审批历史</view>

        <view v-for="(record, index) in approvalHistory" :key="index" class="history-item">
          <view class="history-header">
            <view class="history-action">
              <text v-if="record.action === 'approve'" class="action-approve">通过</text>
              <text v-else-if="record.action === 'reject'" class="action-reject">拒绝</text>
              <text v-else class="action-submit">提交</text>
            </view>
            <view class="history-time">{{ formatTime(record.actionTime) }}</view>
          </view>

          <view class="history-content">
            <view class="history-user">
              <image :src="record.userAvatar || '/static/images/default-avatar.png'" class="history-avatar"></image>
              <view class="user-info">
                <view class="user-name">{{ record.userName }}</view>
                <view class="user-role">{{ record.userRole }}</view>
              </view>
            </view>

            <view v-if="record.comment" class="history-comment">
              <text class="comment-label">审批意见：</text>
              <text class="comment-text">{{ record.comment }}</text>
            </view>
          </view>
        </view>

        <view v-if="approvalHistory.length === 0" class="history-empty">
          <text class="empty-text">暂无审批历史</text>
        </view>
      </view>

      <!-- 备注 -->
      <view v-if="approvalDetail.remark" class="remark-section">
        <view class="section-title">备注信息</view>
        <view class="remark-content">{{ approvalDetail.remark }}</view>
      </view>
    </scroll-view>

    <!-- 底部操作栏 -->
    <view v-if="approvalDetail.approvalStatus === 1 && canApprove" class="bottom-bar">
      <button class="bar-btn reject-btn" @click="handleReject">拒绝</button>
      <button class="bar-btn approve-btn" @click="handleApprove">通过</button>
    </view>

    <!-- 拒绝意见弹窗 -->
    <uni-popup ref="rejectPopup" type="center">
      <view class="reject-popup">
        <view class="popup-header">
          <view class="popup-title">审批拒绝</view>
        </view>

        <view class="popup-content">
          <view class="form-item">
            <view class="form-label required">拒绝原因</view>
            <textarea
              class="form-textarea"
              placeholder="请输入拒绝原因（必填）"
              v-model="rejectForm.comment"
              maxlength="500"
            />
            <view class="form-count">{{ rejectForm.comment.length }}/500</view>
          </view>
        </view>

        <view class="popup-footer">
          <button class="popup-btn cancel-btn" @click="closeRejectPopup">取消</button>
          <button class="popup-btn confirm-btn" @click="confirmReject">确认拒绝</button>
        </view>
      </view>
    </uni-popup>

    <!-- 更多菜单 -->
    <uni-popup ref="moreMenuPopup" type="bottom">
      <view class="more-menu">
        <view class="menu-item" @click="withdrawApproval">
          <uni-icons type="refresh" size="20" color="#ff4d4f"></uni-icons>
          <text class="menu-text">撤回申请</text>
        </view>
        <view class="menu-item" @click="copyApprovalInfo">
          <uni-icons type="copy" size="20" color="#1890ff"></uni-icons>
          <text class="menu-text">复制信息</text>
        </view>
        <view class="menu-cancel" @click="closeMoreMenu">
          <text class="cancel-text">取消</text>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// 审批详情
const approvalDetail = ref({})

// 审批流程
const approvalFlow = ref([])

// 审批历史
const approvalHistory = ref([])

// 是否可以审批
const canApprove = ref(false)

// 拒绝表单
const rejectForm = ref({
  comment: ''
})

// 页面生命周期
onLoad((options) => {
  if (options.id) {
    loadApprovalDetail(options.id)
  }
})

/**
 * 加载审批详情
 */
const loadApprovalDetail = async (approvalId) => {
  uni.showLoading({ title: '加载中...', mask: true })

  try {
    const result = await accessApi.getApprovalDetail(approvalId)

    uni.hideLoading()

    if (result.success && result.data) {
      approvalDetail.value = result.data.detail || {}
      approvalFlow.value = result.data.flow || []
      approvalHistory.value = result.data.history || []
      canApprove.value = result.data.canApprove || false
    } else {
      uni.showToast({ title: result.message || '加载失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('加载审批详情失败:', error)
    uni.showToast({ title: '加载失败', icon: 'none' })
  }
}

/**
 * 审批通过
 */
const handleApprove = () => {
  uni.showModal({
    title: '审批通过',
    content: '确定要通过该申请吗？',
    confirmColor: '#52c41a',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '提交中...', mask: true })

        try {
          const result = await accessApi.approveRequest(approvalDetail.value.approvalId, {
            action: 'approve',
            comment: ''
          })

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '已通过', icon: 'success' })

            // 刷新详情
            setTimeout(() => {
              loadApprovalDetail(approvalDetail.value.approvalId)
            }, 500)
          } else {
            uni.showToast({ title: result.message || '操作失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('审批失败:', error)
          uni.showToast({ title: '操作失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 审批拒绝
 */
const handleReject = () => {
  $refs.rejectPopup.open()
}

/**
 * 关闭拒绝弹窗
 */
const closeRejectPopup = () => {
  $refs.rejectPopup.close()
  rejectForm.value.comment = ''
}

/**
 * 确认拒绝
 */
const confirmReject = async () => {
  if (!rejectForm.value.comment || rejectForm.value.comment.trim().length === 0) {
    uni.showToast({ title: '请输入拒绝原因', icon: 'none' })
    return
  }

  closeRejectPopup()

  uni.showLoading({ title: '提交中...', mask: true })

  try {
    const result = await accessApi.approveRequest(approvalDetail.value.approvalId, {
      action: 'reject',
      comment: rejectForm.value.comment
    })

    uni.hideLoading()

    if (result.success) {
      uni.showToast({ title: '已拒绝', icon: 'success' })

      // 刷新详情
      setTimeout(() => {
        loadApprovalDetail(approvalDetail.value.approvalId)
      }, 500)
    } else {
      uni.showToast({ title: result.message || '操作失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('审批失败:', error)
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

/**
 * 撤回申请
 */
const withdrawApproval = () => {
  closeMoreMenu()

  uni.showModal({
    title: '撤回申请',
    content: '确定要撤回该申请吗？撤回后需要重新提交。',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '撤回中...', mask: true })

        try {
          const result = await accessApi.withdrawApproval(approvalDetail.value.approvalId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '已撤回', icon: 'success' })

            setTimeout(() => {
              uni.navigateBack()
            }, 500)
          } else {
            uni.showToast({ title: result.message || '撤回失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('撤回失败:', error)
          uni.showToast({ title: '撤回失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 复制信息
 */
const copyApprovalInfo = () => {
  closeMoreMenu()

  const text = `申请类型：${getTypeText(approvalDetail.value.approvalType)}
申请人：${approvalDetail.value.applicantName}
申请理由：${approvalDetail.value.applyReason}
申请时间：${formatTime(approvalDetail.value.createTime)}`

  uni.setClipboardData({
    data: text,
    success: () => {
      uni.showToast({ title: '已复制', icon: 'success' })
    }
  })
}

/**
 * 显示更多菜单
 */
const showMoreMenu = () => {
  $refs.moreMenuPopup.open()
}

/**
 * 关闭更多菜单
 */
const closeMoreMenu = () => {
  $refs.moreMenuPopup.close()
}

/**
 * 获取状态图标
 */
const getStatusIcon = (status) => {
  const iconMap = {
    1: 'clock',           // 待审批
    2: 'checkmarkempty',  // 已通过
    3: 'closeempty',      // 已拒绝
    4: 'refreshempty'     // 已撤回
  }
  return iconMap[status] || 'info'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    1: '等待审批',
    2: '审批通过',
    3: '审批拒绝',
    4: '已撤回'
  }
  return textMap[status] || '未知'
}

/**
 * 获取状态描述
 */
const getStatusDesc = (status) => {
  const descMap = {
    1: '申请正在审批中，请耐心等待',
    2: '申请已通过审批，可以正常使用',
    3: '申请已被拒绝，请联系审批人',
    4: '申请已撤回，可以重新提交'
  }
  return descMap[status] || ''
}

/**
 * 获取类型图标
 */
const getTypeIcon = (type) => {
  const iconMap = {
    1: 'locked',        // 权限申请
    2: 'person',        // 访客预约
    3: 'fire'           // 紧急权限
  }
  return iconMap[type] || 'document'
}

/**
 * 获取类型文本
 */
const getTypeText = (type) => {
  const textMap = {
    1: '权限申请',
    2: '访客预约',
    3: '紧急权限'
  }
  return textMap[type] || '其他申请'
}

/**
 * 获取紧急程度文本
 */
const getUrgencyText = (level) => {
  const textMap = {
    1: '一般',
    2: '紧急',
    3: '非常紧急'
  }
  return textMap[level] || '未知'
}

/**
 * 获取步骤样式类
 */
const getStepClass = (status) => {
  const classMap = {
    0: 'step-pending',   // 等待
    1: 'step-current',   // 当前
    2: 'step-success',   // 完成
    3: 'step-error'      // 失败
  }
  return classMap[status] || 'step-pending'
}

/**
 * 格式化时间
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

/**
 * 格式化预期时间
 */
const formatExpectedTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

/**
 * 格式化过期时间
 */
const formatExpiryTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.approval-detail-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 80px;
}

// 导航栏
.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: sticky;
  top: 0;
  z-index: 100;

  .nav-left,
  .nav-right {
    width: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
    flex: 1;
    text-align: center;
  }

  .more-btn {
    font-size: 14px;
    color: #1890ff;
  }
}

// 内容滚动区
.content-scroll {
  height: calc(100vh - 44px);
}

// 状态卡片
.status-card {
  display: flex;
  align-items: center;
  padding: 20px 15px;
  margin: 15px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  &.status-1 {
    background: linear-gradient(135deg, #ffeaa7 0%, #fdcb6e 100%);
  }

  &.status-2 {
    background: linear-gradient(135deg, #84fab0 0%, #8fd3f4 100%);
  }

  &.status-3 {
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  }

  &.status-4 {
    background: linear-gradient(135deg, #dcdde1 0%, #c8d6e5 100%);
  }

  .status-icon {
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    margin-right: 15px;
  }

  .status-info {
    flex: 1;

    .status-text {
      font-size: 18px;
      font-weight: bold;
      color: #fff;
      margin-bottom: 6px;
    }

    .status-desc {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

// 信息区块
.info-section {
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;
  }

  .subsection-title {
    font-size: 15px;
    font-weight: 500;
    color: #666;
    margin: 20px 0 12px;
    padding-top: 15px;
    border-top: 1px solid #f0f0f0;
  }
}

// 申请人卡片
.applicant-card {
  display: flex;
  align-items: center;
  padding: 15px;
  background-color: #f5f5f5;
  border-radius: 12px;
  margin-bottom: 15px;

  .applicant-avatar {
    width: 50px;
    height: 50px;
    border-radius: 50%;
    margin-right: 12px;
  }

  .applicant-details {
    flex: 1;

    .applicant-name {
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .applicant-dept {
      font-size: 13px;
      color: #999;
    }
  }

  .apply-time {
    font-size: 12px;
    color: #999;
  }
}

// 信息项
.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  .item-label {
    font-size: 14px;
    color: #666;
    min-width: 80px;
  }

  .item-value {
    flex: 1;
    font-size: 14px;
    color: #333;
    display: flex;
    align-items: center;

    &.value-full {
      flex-direction: column;
      align-items: flex-start;
    }

    .value-text {
      margin-left: 6px;
    }

    &.urgency-1 {
      color: #52c41a;
    }

    &.urgency-2 {
      color: #fa8c16;
    }

    &.urgency-3 {
      color: #ff4d4f;
    }
  }
}

// 审批流程区块
.flow-section {
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 15px;
}

// 时间线
.timeline {
  padding: 10px 0;

  .timeline-item {
    display: flex;
    position: relative;

    .timeline-line {
      position: absolute;
      left: 15px;
      top: 36px;
      bottom: -20px;
      width: 2px;
      background-color: #e6f7ff;
      z-index: 0;

      &.line-active {
        background-color: #1890ff;
      }
    }

    .timeline-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
      z-index: 1;

      &.step-pending {
        background-color: #f0f0f0;
        border: 2px solid #d9d9d9;

        .step-number {
          font-size: 14px;
          color: #999;
        }
      }

      &.step-current {
        background-color: #1890ff;
        border: 2px solid #1890ff;

        .step-number {
          font-size: 14px;
          color: #fff;
        }
      }

      &.step-success {
        background-color: #52c41a;
        border: 2px solid #52c41a;
      }

      &.step-error {
        background-color: #ff4d4f;
        border: 2px solid #ff4d4f;
      }
    }

    .timeline-content {
      flex: 1;
      padding-top: 4px;

      .step-title {
        font-size: 15px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6px;
      }

      .step-approver {
        font-size: 13px;
        color: #666;
        margin-bottom: 4px;
      }

      .step-time {
        font-size: 12px;
        color: #999;
        margin-bottom: 4px;
      }

      .step-comment {
        font-size: 13px;
        color: #666;
        margin-bottom: 4px;
        line-height: 1.5;
      }

      .step-status {
        font-size: 12px;
        padding: 2px 8px;
        border-radius: 4px;
        display: inline-block;

        &.pending {
          background-color: #fff7e6;
          color: #fa8c16;
        }
      }
    }
  }
}

// 审批历史区块
.history-section {
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 15px;
}

// 历史记录项
.history-item {
  padding: 12px;
  background-color: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }

  .history-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;

    .history-action {
      font-size: 14px;
      font-weight: 500;
      padding: 2px 8px;
      border-radius: 4px;

      &.action-submit {
        background-color: #e6f7ff;
        color: #1890ff;
      }

      &.action-approve {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.action-reject {
        background-color: #fff1f0;
        color: #ff4d4f;
      }
    }

    .history-time {
      font-size: 12px;
      color: #999;
    }
  }

  .history-content {
    .history-user {
      display: flex;
      align-items: center;
      margin-bottom: 8px;

      .history-avatar {
        width: 32px;
        height: 32px;
        border-radius: 50%;
        margin-right: 10px;
      }

      .user-info {
        .user-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 2px;
        }

        .user-role {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .history-comment {
      font-size: 13px;
      line-height: 1.6;

      .comment-label {
        color: #666;
      }

      .comment-text {
        color: #333;
      }
    }
  }

  .history-empty {
    text-align: center;
    padding: 30px 0;

    .empty-text {
      font-size: 14px;
      color: #999;
    }
  }
}

// 备注区块
.remark-section {
  background-color: #fff;
  margin: 0 15px 15px;
  border-radius: 12px;
  padding: 15px;

  .remark-content {
    font-size: 14px;
    color: #666;
    line-height: 1.6;
  }
}

// 底部操作栏
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: 10px;
  padding: 10px 15px;
  background-color: #fff;
  border-top: 1px solid #eee;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));

  .bar-btn {
    flex: 1;
    height: 44px;
    line-height: 44px;
    border-radius: 22px;
    font-size: 16px;
    text-align: center;
    border: none;

    &.reject-btn {
      background-color: #fff1f0;
      color: #ff4d4f;
    }

    &.approve-btn {
      background-color: #52c41a;
      color: #fff;
    }
  }
}

// 拒绝弹窗
.reject-popup {
  width: 85vw;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;

  .popup-header {
    padding: 15px;
    border-bottom: 1px solid #eee;
    text-align: center;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .popup-content {
    padding: 15px;

    .form-item {
      .form-label {
        font-size: 15px;
        color: #333;
        margin-bottom: 12px;
        font-weight: 500;

        &.required::before {
          content: '*';
          color: #ff4d4f;
          margin-right: 4px;
        }
      }

      .form-textarea {
        width: 100%;
        min-height: 100px;
        padding: 10px 12px;
        font-size: 15px;
        color: #333;
        background-color: #f5f5f5;
        border-radius: 8px;
        box-sizing: border-box;

        &::placeholder {
          color: #999;
        }
      }

      .form-count {
        text-align: right;
        font-size: 12px;
        color: #999;
        margin-top: 6px;
      }
    }
  }

  .popup-footer {
    display: flex;
    gap: 10px;
    padding: 15px;
    border-top: 1px solid #eee;

    .popup-btn {
      flex: 1;
      height: 40px;
      line-height: 40px;
      border-radius: 8px;
      font-size: 15px;
      text-align: center;
      border: none;

      &.cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      &.confirm-btn {
        background-color: #ff4d4f;
        color: #fff;
      }
    }
  }
}

// 更多菜单
.more-menu {
  background-color: #fff;
  border-radius: 16px 16px 0 0;
  padding-bottom: env(safe-area-inset-bottom);

  .menu-item {
    display: flex;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;

    &:last-of-type {
      border-bottom: none;
    }

    .menu-text {
      margin-left: 12px;
      font-size: 15px;
      color: #333;
    }
  }

  .menu-cancel {
    padding: 15px;
    text-align: center;
    border-top: 8px solid #f5f5f5;

    .cancel-text {
      font-size: 15px;
      color: #666;
    }
  }
}
</style>
