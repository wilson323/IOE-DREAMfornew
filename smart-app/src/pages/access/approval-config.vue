<template>
  <view class="config-container">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="left" size="20" color="#fff"></uni-icons>
          <text class="navbar-title">审批配置</text>
        </view>
        <view class="navbar-right">
          <view class="save-btn" @tap="saveConfig">
            <text>保存</text>
          </view>
        </view>
      </view>
    </view>

    <scroll-view class="content-scroll" scroll-y>
      <!-- 审批流程配置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">审批流程配置</text>
          <text class="section-desc">设置不同类型审批的流程</text>
        </view>
        <view class="config-list">
          <view
            class="config-item"
            v-for="(item, index) in approvalFlows"
            :key="index"
            @tap="editApprovalFlow(item)"
          >
            <view class="item-icon" :class="item.type">
              <uni-icons :type="getFlowIcon(item.type)" size="20" color="#fff"></uni-icons>
            </view>
            <view class="item-content">
              <text class="item-title">{{ item.name }}</text>
              <text class="item-desc">{{ item.nodeCount }}个审批节点</text>
            </view>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
        <view class="add-flow-btn" @tap="addApprovalFlow">
          <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
          <text class="add-text">添加审批流程</text>
        </view>
      </view>

      <!-- 审批规则设置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">审批规则设置</text>
          <text class="section-desc">设置审批的基本规则</text>
        </view>
        <view class="config-form">
          <view class="form-item">
            <text class="form-label">允许转审</text>
            <switch
              :checked="rules.allowTransfer"
              color="#667eea"
              @change="onRuleChange('allowTransfer', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">允许加签</text>
            <switch
              :checked="rules.allowAddSign"
              color="#667eea"
              @change="onRuleChange('allowAddSign', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">允许撤回</text>
            <switch
              :checked="rules.allowWithdraw"
              color="#667eea"
              @change="onRuleChange('allowWithdraw', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">需要抄送</text>
            <switch
              :checked="rules.requireCc"
              color="#667eea"
              @change="onRuleChange('requireCc', $event)"
            ></switch>
          </view>
          <view class="form-item selector">
            <text class="form-label">超时处理</text>
            <picker
              mode="selector"
              :range="timeoutOptions"
              :value="timeoutIndex"
              @change="onTimeoutChange"
            >
              <view class="selector-value">
                <text>{{ timeoutOptions[timeoutIndex] }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 审批人员管理 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">审批人员管理</text>
          <text class="section-desc">管理各类型审批的审批人</text>
        </view>
        <view class="approver-list">
          <view
            class="approver-item"
            v-for="(item, index) in approvers"
            :key="index"
          >
            <view class="approver-header">
              <text class="approver-type">{{ item.typeName }}</text>
              <view class="approver-count">{{ item.approvers.length }}人</view>
            </view>
            <scroll-view class="approver-scroll" scroll-x>
              <view class="approver-avatars">
                <view
                  class="avatar-item"
                  v-for="(approver, idx) in item.approvers"
                  :key="idx"
                  @tap="viewApprover(approver)"
                >
                  <image
                    class="avatar-image"
                    :src="approver.avatar || '/static/default-avatar.png'"
                    mode="aspectFill"
                  ></image>
                  <text class="avatar-name">{{ approver.name }}</text>
                </view>
                <view class="avatar-item add" @tap="addApprover(item)">
                  <view class="avatar-placeholder">
                    <uni-icons type="plus" size="20" color="#999"></uni-icons>
                  </view>
                  <text class="avatar-name">添加</text>
                </view>
              </view>
            </scroll-view>
          </view>
        </view>
      </view>

      <!-- 通知设置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">通知设置</text>
          <text class="section-desc">设置审批相关通知</text>
        </view>
        <view class="config-form">
          <view class="form-item">
            <text class="form-label">新申请通知</text>
            <switch
              :checked="notification.newApplication"
              color="#667eea"
              @change="onNotificationChange('newApplication', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">审批结果通知</text>
            <switch
              :checked="notification.approvalResult"
              color="#667eea"
              @change="onNotificationChange('approvalResult', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">超时提醒</text>
            <switch
              :checked="notification.timeoutReminder"
              color="#667eea"
              @change="onNotificationChange('timeoutReminder', $event)"
            ></switch>
          </view>
          <view class="form-item">
            <text class="form-label">撤回通知</text>
            <switch
              :checked="notification.withdrawNotice"
              color="#667eea"
              @change="onNotificationChange('withdrawNotice', $event)"
            ></switch>
          </view>
          <view class="form-item selector">
            <text class="form-label">提醒方式</text>
            <picker
              mode="selector"
              :range="reminderMethods"
              :value="reminderMethodIndex"
              @change="onReminderMethodChange"
            >
              <view class="selector-value">
                <text>{{ reminderMethods[reminderMethodIndex] }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 审批权限配置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">审批权限配置</text>
          <text class="section-desc">设置不同角色的审批权限</text>
        </view>
        <view class="permission-list">
          <view
            class="permission-item"
            v-for="(item, index) in permissions"
            :key="index"
          >
            <view class="permission-header">
              <view class="role-info">
                <text class="role-name">{{ item.roleName }}</text>
                <text class="role-desc">{{ item.userCount }}人</text>
              </view>
              <switch
                :checked="item.enabled"
                color="#667eea"
                @change="onPermissionChange(index, $event)"
              ></switch>
            </view>
            <view class="permission-detail" v-if="item.enabled">
              <view class="permission-tags">
                <view
                  class="permission-tag"
                  v-for="(perm, idx) in item.permissions"
                  :key="idx"
                >
                  <text>{{ perm }}</text>
                </view>
              </view>
              <view class="edit-permission" @tap="editPermission(item)">
                <text>编辑权限</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 超时处理配置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">超时处理配置</text>
          <text class="section-desc">设置审批超时后的处理方式</text>
        </view>
        <view class="timeout-config">
          <view class="timeout-item">
            <text class="timeout-label">普通审批超时</text>
            <input
              class="timeout-input"
              type="number"
              v-model="timeoutConfig.normal"
              placeholder="请输入小时数"
            />
            <text class="timeout-unit">小时</text>
          </view>
          <view class="timeout-item">
            <text class="timeout-label">紧急审批超时</text>
            <input
              class="timeout-input"
              type="number"
              v-model="timeoutConfig.urgent"
              placeholder="请输入小时数"
            />
            <text class="timeout-unit">小时</text>
          </view>
          <view class="timeout-item">
            <text class="timeout-label">非常紧急审批超时</text>
            <input
              class="timeout-input"
              type="number"
              v-model="timeoutConfig.veryUrgent"
              placeholder="请输入小时数"
            />
            <text class="timeout-unit">小时</text>
          </view>
          <view class="timeout-action">
            <text class="action-label">超时后自动</text>
            <picker
              mode="selector"
              :range="timeoutActions"
              :value="timeoutActionIndex"
              @change="onTimeoutActionChange"
            >
              <view class="action-selector">
                <text>{{ timeoutActions[timeoutActionIndex] }}</text>
                <uni-icons type="right" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>
        </view>
      </view>

      <!-- 自动审批规则 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">自动审批规则</text>
          <text class="section-desc">设置满足条件自动通过的规则</text>
        </view>
        <view class="auto-rules">
          <view
            class="rule-item"
            v-for="(item, index) in autoRules"
            :key="index"
          >
            <view class="rule-header">
              <view class="rule-info">
                <text class="rule-name">{{ item.name }}</text>
                <view class="rule-status" :class="{ active: item.enabled }">
                  <text>{{ item.enabled ? '已启用' : '已禁用' }}</text>
                </view>
              </view>
              <switch
                :checked="item.enabled"
                color="#667eea"
                @change="onAutoRuleChange(index, $event)"
              ></switch>
            </view>
            <view class="rule-condition">
              <text class="condition-label">触发条件：</text>
              <text class="condition-text">{{ item.condition }}</text>
            </view>
            <view class="rule-actions">
              <view class="action-btn edit" @tap="editAutoRule(item)">
                <uni-icons type="compose" size="14" color="#667eea"></uni-icons>
                <text>编辑</text>
              </view>
              <view class="action-btn delete" @tap="deleteAutoRule(index)">
                <uni-icons type="trash" size="14" color="#ff4d4f"></uni-icons>
                <text>删除</text>
              </view>
            </view>
          </view>
          <view class="add-rule-btn" @tap="addAutoRule">
            <uni-icons type="plus" size="18" color="#667eea"></uni-icons>
            <text class="add-text">添加自动审批规则</text>
          </view>
        </view>
      </view>

      <!-- 其他设置 -->
      <view class="config-section">
        <view class="section-header">
          <text class="section-title">其他设置</text>
        </view>
        <view class="config-form">
          <view class="form-item selector" @tap="gotoApprovalLog">
            <text class="form-label">审批日志</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="form-item selector" @tap="gotoStatistics">
            <text class="form-label">审批统计</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="form-item selector" @tap="exportConfig">
            <text class="form-label">导出配置</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
          <view class="form-item selector" @tap="resetConfig">
            <text class="form-label">恢复默认配置</text>
            <uni-icons type="right" size="16" color="#999"></uni-icons>
          </view>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder"></view>
    </scroll-view>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'

// 状态栏高度
const statusBarHeight = ref(0)

// 审批流程列表
const approvalFlows = ref([
  {
    id: 1,
    type: 'access',
    name: '门禁权限审批',
    nodeCount: 3
  },
  {
    id: 2,
    type: 'visitor',
    name: '访客预约审批',
    nodeCount: 2
  },
  {
    id: 3,
    type: 'overtime',
    name: '加班申请审批',
    nodeCount: 3
  },
  {
    id: 4,
    type: 'leave',
    name: '请假申请审批',
    nodeCount: 4
  }
])

// 审批规则
const rules = reactive({
  allowTransfer: true,
  allowAddSign: true,
  allowWithdraw: true,
  requireCc: false
})

// 超时选项
const timeoutOptions = ['自动通过', '自动拒绝', '提醒上级', '不做处理']
const timeoutIndex = ref(3)

// 审批人列表
const approvers = ref([
  {
    type: 'access',
    typeName: '门禁审批人',
    approvers: [
      { userId: 1, name: '张三', avatar: '/static/avatar1.png' },
      { userId: 2, name: '李四', avatar: '/static/avatar2.png' },
      { userId: 3, name: '王五', avatar: '/static/avatar3.png' }
    ]
  },
  {
    type: 'visitor',
    typeName: '访客审批人',
    approvers: [
      { userId: 4, name: '赵六', avatar: '/static/avatar4.png' },
      { userId: 5, name: '孙七', avatar: '/static/avatar5.png' }
    ]
  },
  {
    type: 'overtime',
    typeName: '加班审批人',
    approvers: [
      { userId: 6, name: '周八', avatar: '/static/avatar6.png' }
    ]
  },
  {
    type: 'leave',
    typeName: '请假审批人',
    approvers: [
      { userId: 7, name: '吴九', avatar: '/static/avatar7.png' },
      { userId: 8, name: '郑十', avatar: '/static/avatar8.png' }
    ]
  }
])

// 通知设置
const notification = reactive({
  newApplication: true,
  approvalResult: true,
  timeoutReminder: true,
  withdrawNotice: true
})

// 提醒方式
const reminderMethods = ['仅应用通知', '仅短信', '应用+短信']
const reminderMethodIndex = ref(2)

// 权限配置
const permissions = ref([
  {
    roleId: 1,
    roleName: '部门经理',
    userCount: 5,
    enabled: true,
    permissions: ['审批', '转审', '加签', '撤回']
  },
  {
    roleId: 2,
    roleName: '人事主管',
    userCount: 3,
    enabled: true,
    permissions: ['审批', '查看']
  },
  {
    roleId: 3,
    roleName: '行政主管',
    userCount: 2,
    enabled: false,
    permissions: []
  }
])

// 超时配置
const timeoutConfig = reactive({
  normal: 24,
  urgent: 12,
  veryUrgent: 6
})

// 超时处理方式
const timeoutActions = ['自动通过', '自动拒绝', '升级到上级']
const timeoutActionIndex = ref(2)

// 自动审批规则
const autoRules = ref([
  {
    id: 1,
    name: '小额门禁申请自动通过',
    enabled: true,
    condition: '门禁权限申请且申请区域≤2个'
  },
  {
    id: 2,
    name: '访客预约自动通过',
    enabled: false,
    condition: '常客预约且预约时间≤1天'
  },
  {
    id: 3,
    name: '加班申请自动通过',
    enabled: false,
    condition: '加班时长≤2小时且非工作时间'
  }
])

// 获取状态栏高度
const getStatusBarHeight = () => {
  const systemInfo = uni.getSystemInfoSync()
  statusBarHeight.value = systemInfo.statusBarHeight || 0
}

// 返回上一页
const goBack = () => {
  uni.navigateBack()
}

// 获取流程图标
const getFlowIcon = (type) => {
  const iconMap = {
    access: 'locked',
    visitor: 'person',
    overtime: 'clock',
    leave: 'calendar'
  }
  return iconMap[type] || 'help'
}

// 编辑审批流程
const editApprovalFlow = (flow) => {
  uni.navigateTo({
    url: `/pages/access/approval-flow-edit?flowId=${flow.id}&type=${flow.type}`
  })
}

// 添加审批流程
const addApprovalFlow = () => {
  uni.navigateTo({
    url: '/pages/access/approval-flow-add'
  })
}

// 规则变更
const onRuleChange = (key, e) => {
  rules[key] = e.detail.value
}

// 超时处理变更
const onTimeoutChange = (e) => {
  timeoutIndex.value = e.detail.value
}

// 通知设置变更
const onNotificationChange = (key, e) => {
  notification[key] = e.detail.value
}

// 提醒方式变更
const onReminderMethodChange = (e) => {
  reminderMethodIndex.value = e.detail.value
}

// 查看审批人详情
const viewApprover = (approver) => {
  uni.navigateTo({
    url: `/pages/access/approver-detail?userId=${approver.userId}`
  })
}

// 添加审批人
const addApprover = (item) => {
  uni.navigateTo({
    url: `/pages/access/approver-select?type=${item.type}`
  })
}

// 权限配置变更
const onPermissionChange = (index, e) => {
  permissions.value[index].enabled = e.detail.value
}

// 编辑权限
const editPermission = (item) => {
  uni.navigateTo({
    url: `/pages/access/permission-edit?roleId=${item.roleId}`
  })
}

// 超时处理方式变更
const onTimeoutActionChange = (e) => {
  timeoutActionIndex.value = e.detail.value
}

// 自动审批规则变更
const onAutoRuleChange = (index, e) => {
  autoRules.value[index].enabled = e.detail.value
}

// 编辑自动规则
const editAutoRule = (rule) => {
  uni.navigateTo({
    url: `/pages/access/auto-rule-edit?ruleId=${rule.id}`
  })
}

// 删除自动规则
const deleteAutoRule = (index) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除这条自动审批规则吗？',
    success: (res) => {
      if (res.confirm) {
        autoRules.value.splice(index, 1)
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
      }
    }
  })
}

// 添加自动规则
const addAutoRule = () => {
  uni.navigateTo({
    url: '/pages/access/auto-rule-add'
  })
}

// 保存配置
const saveConfig = () => {
  uni.showLoading({ title: '保存中...' })

  // TODO: 调用实际API保存配置
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '保存成功',
      icon: 'success'
    })
  }, 1000)
}

// 跳转审批日志
const gotoApprovalLog = () => {
  uni.navigateTo({
    url: '/pages/access/approval-log'
  })
}

// 跳转审批统计
const gotoStatistics = () => {
  uni.navigateTo({
    url: '/pages/access/approval-statistics'
  })
}

// 导出配置
const exportConfig = () => {
  uni.showLoading({ title: '导出中...' })

  // TODO: 调用实际导出API
  setTimeout(() => {
    uni.hideLoading()
    uni.showToast({
      title: '导出成功',
      icon: 'success'
    })
  }, 1500)
}

// 恢复默认配置
const resetConfig = () => {
  uni.showModal({
    title: '提示',
    content: '确定要恢复默认配置吗？此操作不可撤销。',
    confirmColor: '#ff4d4f',
    success: (res) => {
      if (res.confirm) {
        // TODO: 调用实际API恢复默认配置
        uni.showToast({
          title: '已恢复默认配置',
          icon: 'success'
        })
      }
    }
  })
}

// 页面加载
onMounted(() => {
  getStatusBarHeight()
})
</script>

<style lang="scss" scoped>
.config-container {
  width: 100%;
  min-height: 100vh;
  background: #f5f5f5;
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 1000;

  .navbar-content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 44px;
    padding: 0 30rpx;

    .navbar-left {
      display: flex;
      align-items: center;

      .navbar-title {
        margin-left: 20rpx;
        font-size: 18px;
        font-weight: 500;
        color: #fff;
      }
    }

    .navbar-right {
      .save-btn {
        padding: 8rpx 24rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 20rpx;
        font-size: 13px;
        color: #fff;
      }
    }
  }
}

// 内容滚动区
.content-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 44px);
}

// 配置区块
.config-section {
  margin-bottom: 20rpx;
  background: #fff;
  padding: 30rpx;

  .section-header {
    margin-bottom: 30rpx;

    .section-title {
      display: block;
      font-size: 16px;
      font-weight: 500;
      color: #333;
      margin-bottom: 6rpx;
    }

    .section-desc {
      font-size: 12px;
      color: #999;
    }
  }

  .config-list,
  .approver-list,
  .permission-list,
  .auto-rules {
    background: #f9f9f9;
    border-radius: 16rpx;
    padding: 20rpx;
  }
}

// 审批流程列表
.config-list {
  .config-item {
    display: flex;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #fff;
    border-radius: 16rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .item-icon {
      width: 56rpx;
      height: 56rpx;
      border-radius: 16rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;

      &.access {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      }

      &.visitor {
        background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      }

      &.overtime {
        background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
      }

      &.leave {
        background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
      }
    }

    .item-content {
      flex: 1;

      .item-title {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 6rpx;
      }

      .item-desc {
        font-size: 12px;
        color: #999;
      }
    }
  }

  .add-flow-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    background: #fff;
    border: 1rpx dashed #667eea;
    border-radius: 16rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 配置表单
.config-form {
  .form-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 0;
    border-bottom: 1rpx solid #f0f0f0;

    &:last-child {
      border-bottom: none;
    }

    &.selector {
      padding: 30rpx 20rpx;
      background: #f9f9f9;
      border-radius: 12rpx;
      margin-bottom: 20rpx;

      &:last-child {
        margin-bottom: 0;
      }
    }

    .form-label {
      font-size: 14px;
      color: #333;
    }

    .selector-value {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #666;

      text {
        margin-right: 10rpx;
      }
    }
  }
}

// 审批人列表
.approver-list {
  .approver-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .approver-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .approver-type {
        font-size: 14px;
        font-weight: 500;
        color: #333;
      }

      .approver-count {
        padding: 4rpx 16rpx;
        background: #f0f0f0;
        border-radius: 12rpx;
        font-size: 11px;
        color: #999;
      }
    }

    .approver-scroll {
      width: 100%;
      white-space: nowrap;

      .approver-avatars {
        display: inline-flex;

        .avatar-item {
          display: inline-flex;
          flex-direction: column;
          align-items: center;
          margin-right: 30rpx;

          &:last-child {
            margin-right: 0;
          }

          &.add {
            .avatar-placeholder {
              width: 64rpx;
              height: 64rpx;
              border-radius: 50%;
              background: #f5f5f5;
              display: flex;
              align-items: center;
              justify-content: center;
              margin-bottom: 10rpx;
            }

            .avatar-name {
              color: #999;
            }
          }

          .avatar-image {
            width: 64rpx;
            height: 64rpx;
            border-radius: 50%;
            background: #f5f5f5;
            margin-bottom: 10rpx;
          }

          .avatar-name {
            font-size: 11px;
            color: #666;
            white-space: nowrap;
          }
        }
      }
    }
  }
}

// 权限配置列表
.permission-list {
  .permission-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .permission-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .role-info {
        display: flex;
        flex-direction: column;

        .role-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 6rpx;
        }

        .role-desc {
          font-size: 12px;
          color: #999;
        }
      }
    }

    .permission-detail {
      display: flex;
      flex-direction: column;
      padding-top: 20rpx;
      border-top: 1rpx solid #f0f0f0;

      .permission-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10rpx;
        margin-bottom: 20rpx;

        .permission-tag {
          padding: 6rpx 16rpx;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          border-radius: 16rpx;
          font-size: 11px;
          color: #fff;
        }
      }

      .edit-permission {
        display: flex;
        align-items: center;
        justify-content: flex-end;
        font-size: 12px;
        color: #667eea;

        text {
          margin-right: 10rpx;
        }
      }
    }
  }
}

// 超时配置
.timeout-config {
  .timeout-item {
    display: flex;
    align-items: center;
    padding: 30rpx 20rpx;
    background: #f9f9f9;
    border-radius: 12rpx;
    margin-bottom: 20rpx;

    &:last-of-type {
      margin-bottom: 0;
    }

    .timeout-label {
      flex: 1;
      font-size: 14px;
      color: #333;
    }

    .timeout-input {
      width: 150rpx;
      height: 60rpx;
      padding: 0 20rpx;
      background: #fff;
      border: 1rpx solid #e0e0e0;
      border-radius: 8rpx;
      font-size: 14px;
      color: #333;
      text-align: center;
    }

    .timeout-unit {
      margin-left: 20rpx;
      font-size: 13px;
      color: #999;
    }
  }

  .timeout-action {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 30rpx 20rpx;
    background: #f9f9f9;
    border-radius: 12rpx;

    .action-label {
      font-size: 14px;
      color: #333;
    }

    .action-selector {
      display: flex;
      align-items: center;
      font-size: 13px;
      color: #666;

      text {
        margin-right: 10rpx;
      }
    }
  }
}

// 自动审批规则
.auto-rules {
  .rule-item {
    background: #fff;
    border-radius: 16rpx;
    padding: 30rpx;
    margin-bottom: 20rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .rule-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 20rpx;

      .rule-info {
        display: flex;
        flex-direction: column;

        .rule-name {
          font-size: 14px;
          font-weight: 500;
          color: #333;
          margin-bottom: 10rpx;
        }

        .rule-status {
          display: inline-block;
          padding: 4rpx 12rpx;
          background: #f0f0f0;
          border-radius: 12rpx;
          font-size: 10px;
          color: #999;

          &.active {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
          }
        }
      }
    }

    .rule-condition {
      padding: 20rpx;
      background: #f9f9f9;
      border-radius: 12rpx;
      margin-bottom: 20rpx;

      .condition-label {
        font-size: 12px;
        color: #999;
        margin-right: 10rpx;
      }

      .condition-text {
        font-size: 13px;
        color: #333;
      }
    }

    .rule-actions {
      display: flex;
      gap: 20rpx;

      .action-btn {
        display: flex;
        align-items: center;
        padding: 12rpx 20rpx;
        background: #f9f9f9;
        border-radius: 8rpx;
        font-size: 12px;

        &.edit {
          color: #667eea;
        }

        &.delete {
          color: #ff4d4f;
        }

        text {
          margin-left: 8rpx;
        }
      }
    }
  }

  .add-rule-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 80rpx;
    background: #fff;
    border: 1rpx dashed #667eea;
    border-radius: 16rpx;

    .add-text {
      margin-left: 10rpx;
      font-size: 13px;
      color: #667eea;
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 60rpx;
}
</style>
