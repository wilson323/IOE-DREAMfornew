<template>
  <view class="emergency-permission-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">紧急权限</view>
      <view class="nav-right">
        <text class="history-btn" @click="viewHistory">申请记录</text>
      </view>
    </view>

    <!-- 紧急提示横幅 -->
    <view class="emergency-banner">
      <view class="banner-icon">
        <uni-icons type="fire" size="24" color="#ff4d4f"></uni-icons>
      </view>
      <view class="banner-content">
        <view class="banner-title">紧急通道</view>
        <view class="banner-desc">紧急情况下快速申请临时通行权限</view>
      </view>
    </view>

    <!-- Tab切换 -->
    <view class="tab-container">
      <view
        v-for="(tab, index) in tabs"
        :key="index"
        :class="['tab-item', { active: currentTab === index }]"
        @click="switchTab(index)"
      >
        <text class="tab-text">{{ tab.label }}</text>
      </view>
    </view>

    <!-- 申请表单 -->
    <view v-if="currentTab === 0" class="form-content">
      <!-- 紧急程度 -->
      <view class="form-section">
        <view class="section-title">
          <uni-icons type="alert" size="18" color="#ff4d4f"></uni-icons>
          <text class="title-text">紧急程度</text>
        </view>

        <view class="urgency-grid">
          <view
            v-for="(level, index) in urgencyLevels"
            :key="index"
            :class="['urgency-card', { active: formData.urgencyLevel === level.value }]"
            @click="selectUrgency(level.value)"
          >
            <view class="urgency-icon">
              <uni-icons :type="level.icon" size="24" :color="formData.urgencyLevel === level.value ? '#fff' : level.color"></uni-icons>
            </view>
            <view class="urgency-label">{{ level.label }}</view>
            <view class="urgency-desc">{{ level.desc }}</view>
          </view>
        </view>
      </view>

      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <!-- 访问区域 -->
        <view class="form-item">
          <view class="form-label required">访问区域</view>
          <picker mode="selector" :range="areaList" range-key="areaName" :value="areaIndex" @change="onAreaChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.areaId }]">
                {{ getAreaLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 紧急事由 -->
        <view class="form-item">
          <view class="form-label required">紧急事由</view>
          <textarea
            class="form-textarea"
            placeholder="请详细说明紧急情况（必填）"
            v-model="formData.emergencyReason"
            maxlength="500"
          />
          <view class="form-count">{{ formData.emergencyReason.length }}/500</view>
        </view>

        <!-- 预计到达时间 -->
        <view class="form-item">
          <view class="form-label required">预计到达</view>
          <picker mode="datetime" :value="formData.expectedArrival" @change="onExpectedArrivalChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.expectedArrival }]">
                {{ formData.expectedArrival ? formatDateTime(formData.expectedArrival) : '请选择到达时间' }}
              </text>
              <uni-icons type="calendar" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>
      </view>

      <!-- 权限设置 -->
      <view class="form-section">
        <view class="section-title">权限设置</view>

        <!-- 有效时长 -->
        <view class="form-item">
          <view class="form-label required">有效时长</view>
          <picker mode="selector" :range="durationOptions" range-key="label" :value="durationIndex" @change="onDurationChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.duration }]">
                {{ getDurationLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 通行方式 -->
        <view class="form-item">
          <view class="form-label required">通行方式</view>
          <picker mode="selector" :range="accessMethods" range-key="label" :value="accessMethodIndex" @change="onAccessMethodChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.accessMethod }]">
                {{ getAccessMethodLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 通行次数 -->
        <view class="form-item">
          <view class="form-label">通行次数</view>
          <view class="counter-control">
            <button class="counter-btn" @click="decreaseCount" :disabled="formData.passCount <= 1">
              <uni-icons type="minus" size="16" color="#666"></uni-icons>
            </button>
            <input class="counter-input" type="number" v-model="formData.passCount" disabled />
            <button class="counter-btn" @click="increaseCount">
              <uni-icons type="plus" size="16" color="#666"></uni-icons>
            </button>
          </view>
          <view class="form-tip">设置0表示不限次数</view>
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="form-section">
        <view class="section-title">联系信息</view>

        <!-- 申请人 -->
        <view class="form-item">
          <view class="form-label required">申请人</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入申请人姓名"
            v-model="formData.applicantName"
            maxlength="50"
          />
        </view>

        <!-- 联系电话 -->
        <view class="form-item">
          <view class="form-label required">联系电话</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入联系电话"
            v-model="formData.contactPhone"
            maxlength="11"
          />
        </view>

        <!-- 所属部门 -->
        <view class="form-item">
          <view class="form-label">所属部门</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入所属部门（可选）"
            v-model="formData.department"
            maxlength="100"
          />
        </view>
      </view>

      <!-- 快速备注 -->
      <view class="form-section">
        <view class="section-title">快速备注</view>

        <view class="quick-remarks">
          <view
            v-for="(remark, index) in quickRemarks"
            :key="index"
            :class="['remark-chip', { active: formData.remark === remark }]"
            @click="selectRemark(remark)"
          >
            <text class="remark-text">{{ remark }}</text>
          </view>
        </view>

        <view class="form-item" v-if="formData.remark && !quickRemarks.includes(formData.remark)">
          <view class="form-label">自定义备注</view>
          <textarea
            class="form-textarea"
            placeholder="请输入备注信息"
            v-model="formData.remark"
            maxlength="200"
          />
          <view class="form-count">{{ formData.remark.length }}/200</view>
        </view>
      </view>

      <!-- 提交按钮 -->
      <view class="submit-container">
        <button class="submit-btn" @click="submitApplication">
          <uni-icons type="flash" size="18" color="#fff"></uni-icons>
          <text>立即申请</text>
        </button>
      </view>
    </view>

    <!-- 申请记录 -->
    <view v-else class="list-content">
      <scroll-view
        scroll-y
        class="list-scroll"
        @scrolltolower="loadMore"
        :refresher-enabled="true"
        :refresher-triggered="refreshing"
        @refresherrefresh="onRefresh"
      >
        <!-- 空状态 -->
        <view v-if="applicationList.length === 0 && !loading" class="empty-state">
          <uni-icons type="fire" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无申请记录</text>
        </view>

        <!-- 申请列表 -->
        <view v-for="item in applicationList" :key="item.applicationId" class="application-card" @click="viewDetail(item)">
          <!-- 紧急程度标签 -->
          <view :class="['urgency-badge', 'urgency-' + item.urgencyLevel]">
            {{ getUrgencyText(item.urgencyLevel) }}
          </view>

          <!-- 申请信息 -->
          <view class="card-info">
            <view class="info-row">
              <uni-icons type="location" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ item.areaName }}</text>
            </view>
            <view class="info-row">
              <uni-icons type="calendar" size="14" color="#666"></uni-icons>
              <text class="info-text">{{ formatTime(item.createTime) }}</text>
            </view>
          </view>

          <!-- 紧急事由 -->
          <view class="card-reason">{{ item.emergencyReason }}</view>

          <!-- 状态 -->
          <view class="card-footer">
            <view :class="['status-badge', 'status-' + item.approvalStatus]">
              {{ getStatusText(item.approvalStatus) }}
            </view>
            <view class="valid-time" v-if="item.expiryTime">
              <uni-icons type="clock" size="12" color="#999"></uni-icons>
              <text class="time-text">有效期至 {{ formatExpiryTime(item.expiryTime) }}</text>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="applicationList.length > 0" class="load-more">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="!hasMore" class="no-more-text">没有更多了</text>
        </view>
      </scroll-view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// Tab选项
const tabs = ref([
  { label: '紧急申请' },
  { label: '申请记录' }
])
const currentTab = ref(0)

// 紧急程度
const urgencyLevels = [
  { value: 1, label: '一般', desc: '30分钟内审批', icon: 'info', color: '#52c41a' },
  { value: 2, label: '紧急', desc: '15分钟内审批', icon: 'alert', color: '#fa8c16' },
  { value: 3, label: '非常紧急', desc: '5分钟内审批', icon: 'fire', color: '#ff4d4f' }
]

// 有效时长
const durationOptions = [
  { value: 1, label: '1小时' },
  { value: 2, label: '2小时' },
  { value: 4, label: '4小时' },
  { value: 8, label: '8小时' },
  { value: 24, label: '1天' }
]

// 通行方式
const accessMethods = [
  { value: 1, label: '人脸识别' },
  { value: 2, label: '刷卡通行' },
  { value: 3, label: '二维码' },
  { value: 4, label: '全部方式' }
]

// 快速备注
const quickRemarks = [
  '设备故障',
  '紧急维修',
  '临时加班',
  '特殊配送',
  '访客接待'
]

// 表单数据
const formData = ref({
  urgencyLevel: null,
  areaId: null,
  emergencyReason: '',
  expectedArrival: '',
  duration: null,
  accessMethod: null,
  passCount: 1,
  applicantName: '',
  contactPhone: '',
  department: '',
  remark: ''
})

// 区域列表
const areaList = ref([])
const areaIndex = ref(-1)

// 有效时长索引
const durationIndex = ref(-1)

// 通行方式索引
const accessMethodIndex = ref(-1)

// 申请列表
const applicationList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)

// 页面生命周期
onLoad((options) => {
  loadAreaList()

  // 设置默认时间为当前时间
  const now = new Date()
  formData.value.expectedArrival = formatDateTimeValue(now)

  // 如果有areaId参数，预选区域
  if (options.areaId) {
    formData.value.areaId = parseInt(options.areaId)
  }

  // 如果有tab参数，切换到对应tab
  if (options.tab) {
    currentTab.value = parseInt(options.tab)
    if (currentTab.value === 1) {
      loadApplications()
    }
  }
})

/**
 * 切换Tab
 */
const switchTab = (index) => {
  currentTab.value = index
  if (index === 1) {
    loadApplications()
  }
}

/**
 * 加载区域列表
 */
const loadAreaList = async () => {
  try {
    const result = await accessApi.getAreaList({
      pageNum: 1,
      pageSize: 1000,
      enabled: 1
    })

    if (result.success && result.data) {
      areaList.value = result.data.list || []

      // 如果有预选区域，设置索引
      if (formData.value.areaId) {
        const index = areaList.value.findIndex(a => a.areaId === formData.value.areaId)
        if (index >= 0) {
          areaIndex.value = index
        }
      }
    }
  } catch (error) {
    console.error('加载区域列表失败:', error)
  }
}

/**
 * 加载申请列表
 */
const loadApplications = async () => {
  if (loading.value) return

  loading.value = true

  try {
    const result = await accessApi.getEmergencyPermissionList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })

    if (result.success && result.data) {
      const newList = result.data.list || []

      if (pageNum.value === 1) {
        applicationList.value = newList
      } else {
        applicationList.value.push(...newList)
      }

      hasMore.value = newList.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载申请列表失败:', error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

/**
 * 下拉刷新
 */
const onRefresh = () => {
  refreshing.value = true
  pageNum.value = 1
  hasMore.value = true
  loadApplications()
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  pageNum.value++
  loadApplications()
}

/**
 * 选择紧急程度
 */
const selectUrgency = (value) => {
  formData.value.urgencyLevel = value
}

/**
 * 区域变更
 */
const onAreaChange = (e) => {
  const index = e.detail.value
  areaIndex.value = index
  formData.value.areaId = areaList.value[index].areaId
}

/**
 * 预计到达时间变更
 */
const onExpectedArrivalChange = (e) => {
  formData.value.expectedArrival = e.detail.value
}

/**
 * 有效时长变更
 */
const onDurationChange = (e) => {
  const index = e.detail.value
  durationIndex.value = index
  formData.value.duration = durationOptions[index].value
}

/**
 * 通行方式变更
 */
const onAccessMethodChange = (e) => {
  const index = e.detail.value
  accessMethodIndex.value = index
  formData.value.accessMethod = accessMethods[index].value
}

/**
 * 减少通行次数
 */
const decreaseCount = () => {
  if (formData.value.passCount > 0) {
    formData.value.passCount--
  }
}

/**
 * 增加通行次数
 */
const increaseCount = () => {
  formData.value.passCount++
}

/**
 * 选择快速备注
 */
const selectRemark = (remark) => {
  if (formData.value.remark === remark) {
    formData.value.remark = ''
  } else {
    formData.value.remark = remark
  }
}

/**
 * 获取区域标签
 */
const getAreaLabel = () => {
  if (!formData.value.areaId) {
    return '请选择访问区域'
  }
  const area = areaList.value.find(a => a.areaId === formData.value.areaId)
  return area ? area.areaName : '请选择访问区域'
}

/**
 * 获取有效时长标签
 */
const getDurationLabel = () => {
  if (!formData.value.duration) {
    return '请选择有效时长'
  }
  const duration = durationOptions.find(d => d.value === formData.value.duration)
  return duration ? duration.label : '请选择有效时长'
}

/**
 * 获取通行方式标签
 */
const getAccessMethodLabel = () => {
  if (!formData.value.accessMethod) {
    return '请选择通行方式'
  }
  const method = accessMethods.find(m => m.value === formData.value.accessMethod)
  return method ? method.label : '请选择通行方式'
}

/**
 * 获取紧急程度文本
 */
const getUrgencyText = (level) => {
  const levelObj = urgencyLevels.find(l => l.value === level)
  return levelObj ? levelObj.label : '未知'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    1: '待审批',
    2: '已通过',
    3: '已拒绝',
    4: '已失效'
  }
  return textMap[status] || '未知'
}

/**
 * 格式化日期时间值
 */
const formatDateTimeValue = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

/**
 * 格式化日期时间
 */
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
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
 * 格式化过期时间
 */
const formatExpiryTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

/**
 * 验证表单
 */
const validateForm = () => {
  if (!formData.value.urgencyLevel) {
    uni.showToast({ title: '请选择紧急程度', icon: 'none' })
    return false
  }

  if (!formData.value.areaId) {
    uni.showToast({ title: '请选择访问区域', icon: 'none' })
    return false
  }

  if (!formData.value.emergencyReason || formData.value.emergencyReason.trim().length === 0) {
    uni.showToast({ title: '请输入紧急事由', icon: 'none' })
    return false
  }

  if (!formData.value.expectedArrival) {
    uni.showToast({ title: '请选择预计到达时间', icon: 'none' })
    return false
  }

  if (!formData.value.duration) {
    uni.showToast({ title: '请选择有效时长', icon: 'none' })
    return false
  }

  if (!formData.value.accessMethod) {
    uni.showToast({ title: '请选择通行方式', icon: 'none' })
    return false
  }

  if (!formData.value.applicantName || formData.value.applicantName.trim().length === 0) {
    uni.showToast({ title: '请输入申请人姓名', icon: 'none' })
    return false
  }

  if (!formData.value.contactPhone || formData.value.contactPhone.length !== 11) {
    uni.showToast({ title: '请输入正确的联系电话', icon: 'none' })
    return false
  }

  return true
}

/**
 * 提交申请
 */
const submitApplication = async () => {
  if (!validateForm()) {
    return
  }

  uni.showLoading({ title: '提交中...', mask: true })

  try {
    const result = await accessApi.createEmergencyPermission(formData.value)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({ title: '申请成功', icon: 'success' })

      // 重置表单
      resetForm()

      // 切换到申请记录
      setTimeout(() => {
        currentTab.value = 1
        pageNum.value = 1
        loadApplications()
      }, 500)
    } else {
      uni.showToast({ title: result.message || '申请失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('提交申请失败:', error)
    uni.showToast({ title: '申请失败', icon: 'none' })
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {
    urgencyLevel: null,
    areaId: null,
    emergencyReason: '',
    expectedArrival: formatDateTimeValue(new Date()),
    duration: null,
    accessMethod: null,
    passCount: 1,
    applicantName: '',
    contactPhone: '',
    department: '',
    remark: ''
  }
  areaIndex.value = -1
  durationIndex.value = -1
  accessMethodIndex.value = -1
}

/**
 * 查看详情
 */
const viewDetail = (item) => {
  // TODO: 跳转到详情页面
  uni.showToast({ title: '详情功能开发中', icon: 'none' })
}

/**
 * 查看历史记录
 */
const viewHistory = () => {
  currentTab.value = 1
  pageNum.value = 1
  loadApplications()
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.emergency-permission-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-bottom: 20px;
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
    width: 80px;
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

  .history-btn {
    font-size: 14px;
    color: #1890ff;
  }
}

// 紧急提示横幅
.emergency-banner {
  display: flex;
  align-items: center;
  padding: 15px;
  margin: 15px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.2);

  .banner-icon {
    width: 48px;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    margin-right: 12px;
  }

  .banner-content {
    flex: 1;

    .banner-title {
      font-size: 16px;
      font-weight: bold;
      color: #fff;
      margin-bottom: 4px;
    }

    .banner-desc {
      font-size: 13px;
      color: rgba(255, 255, 255, 0.9);
    }
  }
}

// Tab切换
.tab-container {
  display: flex;
  background-color: #fff;
  border-bottom: 1px solid #eee;

  .tab-item {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 12px 0;
    position: relative;

    .tab-text {
      font-size: 15px;
      color: #666;
    }

    &.active {
      .tab-text {
        color: #ff4d4f;
        font-weight: 500;
      }

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 40px;
        height: 3px;
        background-color: #ff4d4f;
        border-radius: 2px;
      }
    }
  }
}

// 表单内容
.form-content {
  padding: 15px;
}

// 列表内容
.list-content {
  padding: 15px;

  .list-scroll {
    height: calc(100vh - 220px);
  }
}

// 表单区块
.form-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;

  .section-title {
    display: flex;
    align-items: center;
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;

    .title-text {
      margin-left: 6px;
    }
  }
}

// 紧急程度网格
.urgency-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;

  .urgency-card {
    padding: 15px 10px;
    background-color: #f5f5f5;
    border-radius: 12px;
    text-align: center;
    border: 2px solid transparent;

    &.active {
      background-color: #ff4d4f;
      border-color: #ff4d4f;

      .urgency-label,
      .urgency-desc {
        color: #fff;
      }
    }

    .urgency-icon {
      margin-bottom: 8px;
    }

    .urgency-label {
      font-size: 14px;
      font-weight: 500;
      color: #333;
      margin-bottom: 4px;
    }

    .urgency-desc {
      font-size: 11px;
      color: #999;
    }
  }
}

// 表单项
.form-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  position: relative;

  &:last-child {
    border-bottom: none;
  }

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

  .form-input {
    width: 100%;
    height: 40px;
    padding: 0 12px;
    font-size: 15px;
    color: #333;
    background-color: #f5f5f5;
    border-radius: 8px;
    box-sizing: border-box;

    &::placeholder {
      color: #999;
    }
  }

  .form-picker {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 40px;
    padding: 0 12px;
    background-color: #f5f5f5;
    border-radius: 8px;

    .picker-text {
      font-size: 15px;
      color: #333;

      &.placeholder {
        color: #999;
      }
    }
  }

  .form-textarea {
    width: 100%;
    min-height: 80px;
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
    position: absolute;
    bottom: 12px;
    right: 15px;
    font-size: 12px;
    color: #999;
  }

  .form-tip {
    font-size: 12px;
    color: #999;
    margin-top: 6px;
  }

  // 计数器控制
  .counter-control {
    display: flex;
    align-items: center;
    gap: 10px;

    .counter-btn {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: #f5f5f5;
      border-radius: 8px;
      border: none;
      padding: 0;

      &[disabled] {
        opacity: 0.5;
      }
    }

    .counter-input {
      flex: 1;
      height: 36px;
      text-align: center;
      font-size: 15px;
      color: #333;
      background-color: #f5f5f5;
      border-radius: 8px;
    }
  }
}

// 快速备注
.quick-remarks {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;

  .remark-chip {
    padding: 6px 16px;
    background-color: #f5f5f5;
    border-radius: 16px;
    font-size: 14px;
    color: #666;

    &.active {
      background-color: #ff4d4f;
      color: #fff;
    }

    .remark-text {
      font-size: 14px;
    }
  }
}

// 提交容器
.submit-container {
  padding: 20px 0;

  .submit-btn {
    width: 100%;
    height: 48px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
    color: #fff;
    border-radius: 24px;
    font-size: 16px;
    font-weight: 500;
    border: none;
    box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;

  .empty-text {
    font-size: 15px;
    color: #999;
    margin-top: 20px;
  }
}

// 申请卡片
.application-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;

  .urgency-badge {
    position: absolute;
    top: 15px;
    right: 15px;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;

    &.urgency-1 {
      background-color: #f6ffed;
      color: #52c41a;
    }

    &.urgency-2 {
      background-color: #fff7e6;
      color: #fa8c16;
    }

    &.urgency-3 {
      background-color: #fff1f0;
      color: #ff4d4f;
    }
  }

  .card-info {
    margin-bottom: 12px;

    .info-row {
      display: flex;
      align-items: center;
      margin-bottom: 6px;

      &:last-child {
        margin-bottom: 0;
      }

      .info-text {
        margin-left: 6px;
        font-size: 14px;
        color: #666;
      }
    }
  }

  .card-reason {
    font-size: 14px;
    color: #333;
    line-height: 1.6;
    margin-bottom: 12px;
    padding: 10px;
    background-color: #f5f5f5;
    border-radius: 8px;
  }

  .card-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 10px;
    border-top: 1px solid #f0f0f0;

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;

      &.status-1 {
        background-color: #fff7e6;
        color: #fa8c16;
      }

      &.status-2 {
        background-color: #f6ffed;
        color: #52c41a;
      }

      &.status-3 {
        background-color: #fff1f0;
        color: #ff4d4f;
      }

      &.status-4 {
        background-color: #f5f5f5;
        color: #999;
      }
    }

    .valid-time {
      display: flex;
      align-items: center;

      .time-text {
        margin-left: 4px;
        font-size: 12px;
        color: #999;
      }
    }
  }
}

// 加载更多
.load-more {
  padding: 20px;
  text-align: center;

  .loading-text,
  .no-more-text {
    font-size: 13px;
    color: #999;
  }
}
</style>
