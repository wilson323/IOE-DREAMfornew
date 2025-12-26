<template>
  <view class="device-rules-page">
    <!-- 自定义导航栏 -->
    <view class="custom-navbar">
      <view class="navbar-content">
        <view class="navbar-left" @tap="goBack">
          <uni-icons type="back" size="20" color="#ffffff"></uni-icons>
          <text class="back-text">返回</text>
        </view>
        <view class="navbar-title">访问规则</view>
        <view class="navbar-right" @tap="showAddRule">
          <uni-icons type="plus" size="20" color="#ffffff"></uni-icons>
        </view>
      </view>
    </view>

    <!-- 主内容区域 -->
    <scroll-view
      class="main-scroll"
      scroll-y
      @scrolltolower="loadMoreRules"
      :refresher-enabled="true"
      :refresher-triggered="refreshing"
      @refresherrefresh="onRefresh"
    >

      <!-- 规则类型筛选标签 -->
      <view class="filter-tabs">
        <view
          v-for="tab in ruleTypeTabs"
          :key="tab.value"
          class="tab-item"
          :class="{ active: activeRuleType === tab.value }"
          @tap="switchRuleType(tab.value)"
        >
          <text class="tab-text">{{ tab.label }}</text>
          <view v-if="activeRuleType === tab.value" class="tab-indicator"></view>
        </view>
      </view>

      <!-- 规则列表 -->
      <view class="rules-list">
        <view v-if="filteredRules.length === 0" class="empty-state">
          <uni-icons type="folder" size="60" color="#CCCCCC"></uni-icons>
          <text class="empty-text">暂无规则</text>
          <text class="empty-hint">点击右上角"+"添加规则</text>
        </view>

        <view
          v-for="rule in filteredRules"
          :key="rule.ruleId"
          class="rule-card"
          @tap="viewRuleDetail(rule)"
        >
          <!-- 规则类型标签 -->
          <view class="rule-type-badge" :style="{ background: getRuleTypeGradient(rule.ruleType) }">
            <text class="rule-type-text">{{ getRuleTypeLabel(rule.ruleType) }}</text>
          </view>

          <!-- 规则状态 -->
          <view class="rule-status" :class="{ enabled: rule.enabled }">
            <text class="status-text">{{ rule.enabled ? '已启用' : '已禁用' }}</text>
          </view>

          <!-- 规则名称 -->
          <view class="rule-header">
            <text class="rule-name">{{ rule.ruleName }}</text>
          </view>

          <!-- 规则详情 -->
          <view class="rule-detail">
            <!-- 时间段规则 -->
            <block v-if="rule.ruleType === 1">
              <view class="detail-row">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="detail-label">开始时间：</text>
                <text class="detail-value">{{ rule.startTime }}</text>
              </view>
              <view class="detail-row">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="detail-label">结束时间：</text>
                <text class="detail-value">{{ rule.endTime }}</text>
              </view>
            </block>

            <!-- 周规则 -->
            <block v-else-if="rule.ruleType === 2">
              <view class="detail-row">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="detail-label">有效星期：</text>
                <text class="detail-value">{{ formatWeekDays(rule.weekDays) }}</text>
              </view>
              <view class="detail-row">
                <uni-icons type="time" size="14" color="#999"></uni-icons>
                <text class="detail-label">时间段：</text>
                <text class="detail-value">{{ rule.startTime }} - {{ rule.endTime }}</text>
              </view>
            </block>

            <!-- 特殊日期规则 -->
            <block v-else-if="rule.ruleType === 3">
              <view class="detail-row">
                <uni-icons type="calendar" size="14" color="#999"></uni-icons>
                <text class="detail-label">特殊日期：</text>
                <text class="detail-value">{{ rule.specialDate }}</text>
              </view>
              <view class="detail-row">
                <uni-icons type="info" size="14" color="#999"></uni-icons>
                <text class="detail-label">日期类型：</text>
                <text class="detail-value">{{ rule.dateType === 1 ? '工作日' : '节假日' }}</text>
              </view>
            </block>
          </view>

          <!-- 优先级 -->
          <view class="rule-priority">
            <text class="priority-label">优先级：</text>
            <view class="priority-badge" :style="{ background: getPriorityGradient(rule.priority) }">
              <text class="priority-text">P{{ rule.priority }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="rule-actions" @tap.stop>
            <view class="action-btn" @tap="toggleRuleStatus(rule)">
              <uni-icons
                :type="rule.enabled ? 'eye-slash' : 'eye'"
                size="18"
                :color="rule.enabled ? '#999' : '#667eea'"
              ></uni-icons>
              <text class="action-text">{{ rule.enabled ? '禁用' : '启用' }}</text>
            </view>
            <view class="action-btn" @tap="editRule(rule)">
              <uni-icons type="compose" size="18" color="#667eea"></uni-icons>
              <text class="action-text">编辑</text>
            </view>
            <view class="action-btn" @tap="deleteRule(rule)">
              <uni-icons type="trash" size="18" color="#ff4d4f"></uni-icons>
              <text class="action-text delete">删除</text>
            </view>
          </view>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="hasMore" class="load-more">
        <uni-icons type="spinner-cycle" size="16" color="#CCCCCC"></uni-icons>
        <text class="load-text">加载更多...</text>
      </view>
      <view v-else-if="filteredRules.length > 0" class="load-more">
        <text class="load-text">没有更多了</text>
      </view>
    </scroll-view>

    <!-- 添加规则弹窗 -->
    <uni-popup ref="addRulePopup" type="bottom" :safe-area="false">
      <view class="add-rule-popup">
        <view class="popup-header">
          <text class="popup-title">添加规则</text>
          <view class="popup-close" @tap="closeAddRule">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <view class="popup-content">
          <!-- 规则类型选择 -->
          <view class="form-item">
            <text class="form-label">规则类型</text>
            <picker mode="selector" :range="ruleTypes" range-key="label" @change="onRuleTypeChange">
              <view class="picker-value">
                <text :class="{ placeholder: !formData.ruleType }">
                  {{ formData.ruleType ? getRuleTypeLabel(formData.ruleType) : '请选择规则类型' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 规则名称 -->
          <view class="form-item">
            <text class="form-label">规则名称</text>
            <input
              class="form-input"
              v-model="formData.ruleName"
              placeholder="请输入规则名称"
              placeholder-class="input-placeholder"
            />
          </view>

          <!-- 时间段规则字段 -->
          <block v-if="formData.ruleType === 1">
            <view class="form-item">
              <text class="form-label">开始时间</text>
              <picker mode="time" :value="formData.startTime" @change="onStartTimeChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.startTime }">
                    {{ formData.startTime || '请选择开始时间' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">结束时间</text>
              <picker mode="time" :value="formData.endTime" @change="onEndTimeChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.endTime }">
                    {{ formData.endTime || '请选择结束时间' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </block>

          <!-- 周规则字段 -->
          <block v-else-if="formData.ruleType === 2">
            <view class="form-item">
              <text class="form-label">有效星期</text>
              <view class="weekdays-selector">
                <view
                  v-for="(day, index) in weekDays"
                  :key="index"
                  class="weekday-item"
                  :class="{ selected: formData.weekDays.includes(index + 1) }"
                  @tap="toggleWeekDay(index + 1)"
                >
                  <text class="weekday-text">{{ day }}</text>
                </view>
              </view>
            </view>
            <view class="form-item">
              <text class="form-label">开始时间</text>
              <picker mode="time" :value="formData.startTime" @change="onStartTimeChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.startTime }">
                    {{ formData.startTime || '请选择开始时间' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">结束时间</text>
              <picker mode="time" :value="formData.endTime" @change="onEndTimeChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.endTime }">
                    {{ formData.endTime || '请选择结束时间' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </block>

          <!-- 特殊日期规则字段 -->
          <block v-else-if="formData.ruleType === 3">
            <view class="form-item">
              <text class="form-label">特殊日期</text>
              <picker mode="date" :value="formData.specialDate" @change="onSpecialDateChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.specialDate }">
                    {{ formData.specialDate || '请选择日期' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
            <view class="form-item">
              <text class="form-label">日期类型</text>
              <picker mode="selector" :range="dateTypes" range-key="label" @change="onDateTypeChange">
                <view class="picker-value">
                  <text :class="{ placeholder: !formData.dateType }">
                    {{ formData.dateType ? getDateTypeLabel(formData.dateType) : '请选择日期类型' }}
                  </text>
                  <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
                </view>
              </picker>
            </view>
          </block>

          <!-- 优先级 -->
          <view class="form-item">
            <text class="form-label">优先级</text>
            <picker mode="selector" :range="priorities" range-key="label" @change="onPriorityChange">
              <view class="picker-value">
                <text :class="{ placeholder: !formData.priority }">
                  {{ formData.priority ? `P${formData.priority}` : '请选择优先级' }}
                </text>
                <uni-icons type="arrowdown" size="14" color="#999"></uni-icons>
              </view>
            </picker>
          </view>

          <!-- 启用开关 -->
          <view class="form-item switch-item">
            <text class="form-label">启用规则</text>
            <switch :checked="formData.enabled" @change="onEnabledChange" color="#667eea" />
          </view>
        </view>

        <view class="popup-footer">
          <button class="btn-cancel" @tap="closeAddRule">取消</button>
          <button class="btn-confirm" @tap="confirmAddRule">确定</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { deviceApi } from '@/api/business/access/access-api.js'

// 页面参数
const deviceId = ref('')

// 数据状态
const rulesList = ref([])
const refreshing = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

// 筛选状态
const activeRuleType = ref(0) // 0-全部 1-时间段规则 2-周规则 3-特殊日期规则
const ruleTypeTabs = [
  { label: '全部', value: 0 },
  { label: '时间段', value: 1 },
  { label: '周规则', value: 2 },
  { label: '特殊日期', value: 3 }
]

// 表单数据
const formData = reactive({
  ruleType: null,
  ruleName: '',
  startTime: '',
  endTime: '',
  weekDays: [],
  specialDate: '',
  dateType: null,
  priority: 1,
  enabled: true
})

// 选项数据
const ruleTypes = [
  { label: '时间段规则', value: 1 },
  { label: '周规则', value: 2 },
  { label: '特殊日期规则', value: 3 }
]

const weekDays = ['一', '二', '三', '四', '五', '六', '日']

const dateTypes = [
  { label: '工作日', value: 1 },
  { label: '节假日', value: 2 }
]

const priorities = [
  { label: 'P1（最高）', value: 1 },
  { label: 'P2（高）', value: 2 },
  { label: 'P3（中）', value: 3 },
  { label: 'P4（低）', value: 4 },
  { label: 'P5（最低）', value: 5 }
]

// 计算属性
const filteredRules = computed(() => {
  let list = rulesList.value
  if (activeRuleType.value !== 0) {
    list = list.filter(rule => rule.ruleType === activeRuleType.value)
  }
  return list
})

// 页面加载
onMounted(() => {
  const pages = getCurrentPages()
  const currentPage = pages[pages.length - 1]
  const options = currentPage.options
  deviceId.value = options.deviceId || ''

  loadRules(true)
})

// 加载规则列表
const loadRules = async (refresh = false) => {
  if (refresh) {
    currentPage.value = 1
    hasMore.value = true
  }

  try {
    const params = {
      deviceId: deviceId.value,
      pageNum: currentPage.value,
      pageSize: pageSize
    }

    const res = await deviceApi.getDeviceRules(deviceId.value, params)
    if (res.code === 200 && res.data) {
      const newList = res.data.list || []

      if (refresh) {
        rulesList.value = newList
      } else {
        rulesList.value.push(...newList)
      }

      hasMore.value = newList.length >= pageSize
    }
  } catch (error) {
    console.error('加载规则失败:', error)
    uni.showToast({
      title: '加载失败',
      icon: 'none'
    })
  } finally {
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  loadRules(true)
}

// 加载更多
const loadMoreRules = () => {
  if (!hasMore.value) return
  currentPage.value++
  loadRules()
}

// 切换规则类型
const switchRuleType = (type) => {
  activeRuleType.value = type
}

// 获取规则类型标签
const getRuleTypeLabel = (type) => {
  const item = ruleTypes.find(item => item.value === type)
  return item ? item.label : '未知'
}

// 获取规则类型渐变色
const getRuleTypeGradient = (type) => {
  const gradients = {
    1: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    2: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    3: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)'
  }
  return gradients[type] || gradients[1]
}

// 获取优先级渐变色
const getPriorityGradient = (priority) => {
  if (priority <= 2) return 'linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%)'
  if (priority <= 3) return 'linear-gradient(135deg, #faad14 0%, #ffc53d 100%)'
  return 'linear-gradient(135deg, #52c41a 0%, #73d13d 100%)'
}

// 格式化星期
const formatWeekDays = (days) => {
  if (!days || days.length === 0) return '未设置'
  return days.map(day => `周${weekDays[day - 1]}`).join('、')
}

// 获取日期类型标签
const getDateTypeLabel = (type) => {
  const item = dateTypes.find(item => item.value === type)
  return item ? item.label : '未知'
}

// 查看规则详情
const viewRuleDetail = (rule) => {
  uni.showModal({
    title: '规则详情',
    content: `规则名称：${rule.ruleName}\n规则类型：${getRuleTypeLabel(rule.ruleType)}\n优先级：P${rule.priority}\n状态：${rule.enabled ? '已启用' : '已禁用'}`,
    showCancel: false
  })
}

// 切换规则状态
const toggleRuleStatus = async (rule) => {
  const action = rule.enabled ? '禁用' : '启用'
  uni.showModal({
    title: '确认操作',
    content: `确定要${action}规则"${rule.ruleName}"吗？`,
    success: async (res) => {
      if (res.confirm) {
        try {
          const res = await deviceApi.updateDeviceRule(deviceId.value, rule.ruleId, {
            enabled: !rule.enabled
          })
          if (res.code === 200) {
            rule.enabled = !rule.enabled
            uni.showToast({
              title: `${action}成功`,
              icon: 'success'
            })
          }
        } catch (error) {
          console.error(`${action}规则失败:`, error)
        }
      }
    }
  })
}

// 编辑规则
const editRule = (rule) => {
  uni.showToast({
    title: '编辑功能开发中',
    icon: 'none'
  })
}

// 删除规则
const deleteRule = (rule) => {
  uni.showModal({
    title: '确认删除',
    content: `确定要删除规则"${rule.ruleName}"吗？`,
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        try {
          const res = await deviceApi.deleteDeviceRule(deviceId.value, rule.ruleId)
          if (res.code === 200) {
            const index = rulesList.value.findIndex(item => item.ruleId === rule.ruleId)
            if (index > -1) {
              rulesList.value.splice(index, 1)
            }
            uni.showToast({
              title: '删除成功',
              icon: 'success'
            })
          }
        } catch (error) {
          console.error('删除规则失败:', error)
        }
      }
    }
  })
}

// 显示添加规则弹窗
const showAddRule = () => {
  resetFormData()
  uni.$refs.addRulePopup.open()
}

// 关闭添加规则弹窗
const closeAddRule = () => {
  uni.$refs.addRulePopup.close()
}

// 重置表单数据
const resetFormData = () => {
  formData.ruleType = null
  formData.ruleName = ''
  formData.startTime = ''
  formData.endTime = ''
  formData.weekDays = []
  formData.specialDate = ''
  formData.dateType = null
  formData.priority = 1
  formData.enabled = true
}

// 规则类型变更
const onRuleTypeChange = (e) => {
  formData.ruleType = ruleTypes[e.detail.value].value
}

// 开始时间变更
const onStartTimeChange = (e) => {
  formData.startTime = e.detail.value
}

// 结束时间变更
const onEndTimeChange = (e) => {
  formData.endTime = e.detail.value
}

// 切换星期
const toggleWeekDay = (day) => {
  const index = formData.weekDays.indexOf(day)
  if (index > -1) {
    formData.weekDays.splice(index, 1)
  } else {
    formData.weekDays.push(day)
  }
}

// 特殊日期变更
const onSpecialDateChange = (e) => {
  formData.specialDate = e.detail.value
}

// 日期类型变更
const onDateTypeChange = (e) => {
  formData.dateType = dateTypes[e.detail.value].value
}

// 优先级变更
const onPriorityChange = (e) => {
  formData.priority = priorities[e.detail.value].value
}

// 启用开关变更
const onEnabledChange = (e) => {
  formData.enabled = e.detail.value
}

// 确认添加规则
const confirmAddRule = async () => {
  if (!formData.ruleType) {
    uni.showToast({
      title: '请选择规则类型',
      icon: 'none'
    })
    return
  }

  if (!formData.ruleName) {
    uni.showToast({
      title: '请输入规则名称',
      icon: 'none'
    })
    return
  }

  // 根据规则类型验证必填字段
  if (formData.ruleType === 1) {
    if (!formData.startTime || !formData.endTime) {
      uni.showToast({
        title: '请选择时间段',
        icon: 'none'
      })
      return
    }
  } else if (formData.ruleType === 2) {
    if (formData.weekDays.length === 0) {
      uni.showToast({
        title: '请选择有效星期',
        icon: 'none'
      })
      return
    }
    if (!formData.startTime || !formData.endTime) {
      uni.showToast({
        title: '请选择时间段',
        icon: 'none'
      })
      return
    }
  } else if (formData.ruleType === 3) {
    if (!formData.specialDate) {
      uni.showToast({
        title: '请选择特殊日期',
        icon: 'none'
      })
      return
    }
    if (!formData.dateType) {
      uni.showToast({
        title: '请选择日期类型',
        icon: 'none'
      })
      return
    }
  }

  try {
    const params = { ...formData }
    const res = await deviceApi.addDeviceRule(deviceId.value, params)
    if (res.code === 200) {
      uni.showToast({
        title: '添加成功',
        icon: 'success'
      })
      closeAddRule()
      loadRules(true)
    }
  } catch (error) {
    console.error('添加规则失败:', error)
  }
}

// 返回
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.device-rules-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #f5f7fa 0%, #ffffff 100%);
}

// 自定义导航栏
.custom-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding-top: var(--status-bar-height);
  padding-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(102, 126, 234, 0.3);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30rpx;
  height: 88rpx;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 10rpx;
}

.back-text {
  font-size: 28rpx;
  color: #ffffff;
}

.navbar-title {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  font-size: 32rpx;
  font-weight: 600;
  color: #ffffff;
}

.navbar-right {
  width: 80rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 主内容区域
.main-scroll {
  height: 100vh;
  padding-top: calc(var(--status-bar-height) + 108rpx);
  padding-bottom: 30rpx;
}

// 筛选标签
.filter-tabs {
  display: flex;
  background: #ffffff;
  padding: 20rpx 30rpx;
  margin: 20rpx 30rpx;
  border-radius: 16rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.tab-item {
  flex: 1;
  text-align: center;
  position: relative;
  padding: 16rpx 0;
}

.tab-text {
  font-size: 28rpx;
  color: #666666;
  transition: all 0.3s;
}

.tab-item.active .tab-text {
  color: #667eea;
  font-weight: 600;
}

.tab-indicator {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40rpx;
  height: 6rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 3rpx;
}

// 规则列表
.rules-list {
  padding: 0 30rpx;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999999;
  margin-top: 30rpx;
}

.empty-hint {
  font-size: 24rpx;
  color: #CCCCCC;
  margin-top: 16rpx;
}

.rule-card {
  background: #ffffff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 24rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;

  &:active {
    transform: scale(0.98);
    transition: transform 0.2s;
  }
}

.rule-type-badge {
  position: absolute;
  top: 20rpx;
  right: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(102, 126, 234, 0.3);
}

.rule-type-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 600;
}

.rule-status {
  position: absolute;
  top: 20rpx;
  left: 20rpx;
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  background: #f5f5f5;

  &.enabled {
    background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
    box-shadow: 0 2rpx 8rpx rgba(82, 196, 26, 0.3);
  }
}

.status-text {
  font-size: 22rpx;
  color: #666666;
}

.rule-status.enabled .status-text {
  color: #ffffff;
}

.rule-header {
  padding: 50rpx 0 20rpx;
}

.rule-name {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.rule-detail {
  padding: 20rpx 0;
  border-top: 1rpx solid #f0f0f0;
  border-bottom: 1rpx solid #f0f0f0;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 16rpx;

  &:last-child {
    margin-bottom: 0;
  }
}

.detail-label {
  font-size: 26rpx;
  color: #999999;
  margin-left: 8rpx;
}

.detail-value {
  font-size: 26rpx;
  color: #333333;
  margin-left: 8rpx;
}

.rule-priority {
  display: flex;
  align-items: center;
  padding: 20rpx 0;
}

.priority-label {
  font-size: 26rpx;
  color: #666666;
}

.priority-badge {
  padding: 8rpx 16rpx;
  border-radius: 12rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.priority-text {
  font-size: 22rpx;
  color: #ffffff;
  font-weight: 600;
}

.rule-actions {
  display: flex;
  gap: 20rpx;
  padding-top: 20rpx;
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8rpx;
  padding: 16rpx 0;
  background: #f5f7fa;
  border-radius: 12rpx;
}

.action-text {
  font-size: 22rpx;
  color: #667eea;
}

.action-text.delete {
  color: #ff4d4f;
}

// 加载更多
.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16rpx;
  padding: 40rpx 0;
}

.load-text {
  font-size: 24rpx;
  color: #CCCCCC;
}

// 添加规则弹窗
.add-rule-popup {
  background: #ffffff;
  border-radius: 32rpx 32rpx 0 0;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 40rpx 30rpx 20rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.popup-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333333;
}

.popup-close {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.popup-content {
  flex: 1;
  overflow-y: auto;
  padding: 30rpx;
}

.form-item {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #333333;
  margin-bottom: 16rpx;
  font-weight: 600;
}

.form-input {
  width: 100%;
  height: 80rpx;
  padding: 0 24rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #333333;
}

.input-placeholder {
  color: #CCCCCC;
}

.picker-value {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 80rpx;
  padding: 0 24rpx;
  background: #f5f7fa;
  border-radius: 16rpx;
  font-size: 28rpx;
  color: #333333;
}

.picker-value .placeholder {
  color: #CCCCCC;
}

.weekdays-selector {
  display: flex;
  gap: 12rpx;
  flex-wrap: wrap;
}

.weekday-item {
  width: 80rpx;
  height: 80rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.weekday-item.selected {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: #667eea;
  box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
}

.weekday-text {
  font-size: 26rpx;
  color: #666666;
}

.weekday-item.selected .weekday-text {
  color: #ffffff;
  font-weight: 600;
}

.switch-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.popup-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 1rpx solid #f0f0f0;
}

.btn-cancel,
.btn-confirm {
  flex: 1;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  border-radius: 16rpx;
  font-size: 28rpx;
  font-weight: 600;
}

.btn-cancel {
  background: #f5f7fa;
  color: #666666;
}

.btn-confirm {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  box-shadow: 0 4rpx 16rpx rgba(102, 126, 234, 0.4);
}
</style>
