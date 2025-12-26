<template>
  <view class="visitor-appointment-page">
    <!-- 导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <uni-icons type="left" size="20" color="#333"></uni-icons>
      </view>
      <view class="nav-title">访客预约</view>
      <view class="nav-right">
        <text class="history-btn" @click="viewHistory">历史记录</text>
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

    <!-- 新增预约 -->
    <view v-if="currentTab === 0" class="form-content">
      <!-- 访客信息 -->
      <view class="form-section">
        <view class="section-title">访客信息</view>

        <!-- 访客姓名 -->
        <view class="form-item">
          <view class="form-label required">访客姓名</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入访客姓名"
            v-model="formData.visitorName"
            maxlength="50"
          />
        </view>

        <!-- 访客手机 -->
        <view class="form-item">
          <view class="form-label required">访客手机</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入访客手机号"
            v-model="formData.visitorPhone"
            maxlength="11"
          />
        </view>

        <!-- 访客单位 -->
        <view class="form-item">
          <view class="form-label">访客单位</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入访客单位（可选）"
            v-model="formData.visitorCompany"
            maxlength="100"
          />
        </view>

        <!-- 访客类型 -->
        <view class="form-item">
          <view class="form-label required">访客类型</view>
          <picker mode="selector" :range="visitorTypes" range-key="label" :value="visitorTypeIndex" @change="onVisitorTypeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.visitorType }]">
                {{ getVisitorTypeLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 同行人数 -->
        <view class="form-item">
          <view class="form-label required">同行人数</view>
          <input
            class="form-input"
            type="number"
            placeholder="请输入同行人数（包含本人）"
            v-model="formData.visitorCount"
            maxlength="2"
          />
        </view>

        <!-- 车牌号码 -->
        <view class="form-item">
          <view class="form-label">车牌号码</view>
          <input
            class="form-input"
            type="text"
            placeholder="请输入车牌号码（可选）"
            v-model="formData.plateNumber"
            maxlength="10"
          />
        </view>
      </view>

      <!-- 访问信息 -->
      <view class="form-section">
        <view class="section-title">访问信息</view>

        <!-- 被访人 -->
        <view class="form-item">
          <view class="form-label required">被访人</view>
          <picker mode="selector" :range="employeeList" range-key="label" :value="employeeIndex" @change="onEmployeeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.visitEmployeeId }]">
                {{ getEmployeeLabel() }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

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

        <!-- 访问事由 -->
        <view class="form-item">
          <view class="form-label required">访问事由</view>
          <textarea
            class="form-textarea"
            placeholder="请详细说明访问事由"
            v-model="formData.visitReason"
            maxlength="500"
          />
          <view class="form-count">{{ formData.visitReason.length }}/500</view>
        </view>
      </view>

      <!-- 预约时间 -->
      <view class="form-section">
        <view class="section-title">预约时间</view>

        <!-- 预约日期 -->
        <view class="form-item">
          <view class="form-label required">预约日期</view>
          <picker mode="date" :value="formData.appointmentDate" @change="onAppointmentDateChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.appointmentDate }]">
                {{ formData.appointmentDate || '请选择预约日期' }}
              </text>
              <uni-icons type="calendar" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

        <!-- 预约时间 -->
        <view class="form-item">
          <view class="form-label required">预约时间</view>
          <picker mode="time" :value="formData.appointmentTime" @change="onAppointmentTimeChange">
            <view class="form-picker">
              <text :class="['picker-text', { placeholder: !formData.appointmentTime }]">
                {{ formData.appointmentTime || '请选择预约时间' }}
              </text>
              <uni-icons type="right" size="16" color="#d9d9d9"></uni-icons>
            </view>
          </picker>
        </view>

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
      </view>

      <!-- 提交按钮 -->
      <view class="submit-container">
        <button class="submit-btn" @click="submitAppointment">提交预约</button>
      </view>
    </view>

    <!-- 我的预约 -->
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
        <view v-if="appointmentList.length === 0 && !loading" class="empty-state">
          <uni-icons type="person" size="80" color="#d9d9d9"></uni-icons>
          <text class="empty-text">暂无预约记录</text>
        </view>

        <!-- 预约列表 -->
        <view v-for="item in appointmentList" :key="item.appointmentId" class="appointment-card" @click="viewDetail(item)">
          <!-- 状态标签 -->
          <view :class="['status-badge', getStatusClass(item.appointmentStatus)]">
            {{ getStatusText(item.appointmentStatus) }}
          </view>

          <!-- 访客信息 -->
          <view class="card-section">
            <view class="section-label">访客信息</view>
            <view class="info-row">
              <text class="info-label">姓名：</text>
              <text class="info-value">{{ item.visitorName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">手机：</text>
              <text class="info-value">{{ item.visitorPhone }}</text>
            </view>
            <view v-if="item.visitorCompany" class="info-row">
              <text class="info-label">单位：</text>
              <text class="info-value">{{ item.visitorCompany }}</text>
            </view>
          </view>

          <!-- 访问信息 -->
          <view class="card-section">
            <view class="section-label">访问信息</view>
            <view class="info-row">
              <text class="info-label">被访人：</text>
              <text class="info-value">{{ item.visitEmployeeName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">区域：</text>
              <text class="info-value">{{ item.areaName }}</text>
            </view>
            <view class="info-row">
              <text class="info-label">时间：</text>
              <text class="info-value">{{ formatDateTime(item.appointmentDate, item.appointmentTime) }}</text>
            </view>
          </view>

          <!-- 操作按钮 -->
          <view class="card-actions">
            <button
              v-if="item.appointmentStatus === 1"
              class="action-btn primary-btn"
              @click.stop="showQRCode(item)"
            >
              查看通行证
            </button>
            <button
              v-if="item.appointmentStatus === 1"
              class="action-btn cancel-btn"
              @click.stop="cancelAppointment(item)"
            >
              取消预约
            </button>
            <button
              v-if="item.appointmentStatus === 2 && !item.checkInTime"
              class="action-btn primary-btn"
              @click.stop="checkIn(item)"
            >
              签到
            </button>
            <button
              v-if="item.appointmentStatus === 2 && item.checkInTime && !item.checkOutTime"
              class="action-btn success-btn"
              @click.stop="checkOut(item)"
            >
              签退
            </button>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="appointmentList.length > 0" class="load-more">
          <text v-if="loading" class="loading-text">加载中...</text>
          <text v-else-if="!hasMore" class="no-more-text">没有更多了</text>
        </view>
      </scroll-view>
    </view>

    <!-- 二维码弹窗 -->
    <uni-popup ref="qrcodePopup" type="center">
      <view class="qrcode-popup">
        <view class="popup-header">
          <view class="popup-title">临时通行证</view>
          <view class="popup-close" @click="closeQRCode">
            <uni-icons type="close" size="20" color="#999"></uni-icons>
          </view>
        </view>

        <view class="qrcode-content">
          <!-- 访客信息 -->
          <view class="visitor-info">
            <view class="info-name">{{ currentAppointment.visitorName }}</view>
            <view class="info-phone">{{ currentAppointment.visitorPhone }}</view>
          </view>

          <!-- 二维码 -->
          <view class="qrcode-container">
            <image v-if="currentAppointment.qrCode" :src="currentAppointment.qrCode" class="qrcode-image" mode="widthFix"></image>
            <view v-else class="qrcode-placeholder">
              <text class="placeholder-text">二维码生成中...</text>
            </view>
          </view>

          <!-- 访问信息 -->
          <view class="visit-info">
            <view class="visit-row">
              <uni-icons type="person" size="14" color="#666"></uni-icons>
              <text class="visit-text">被访人：{{ currentAppointment.visitEmployeeName }}</text>
            </view>
            <view class="visit-row">
              <uni-icons type="location" size="14" color="#666"></uni-icons>
              <text class="visit-text">访问区域：{{ currentAppointment.areaName }}</text>
            </view>
            <view class="visit-row">
              <uni-icons type="calendar" size="14" color="#666"></uni-icons>
              <text class="visit-text">访问时间：{{ formatDateTime(currentAppointment.appointmentDate, currentAppointment.appointmentTime) }}</text>
            </view>
          </view>

          <!-- 使用说明 -->
          <view class="usage-tips">
            <view class="tips-title">使用说明</view>
            <view class="tips-list">
              <view class="tips-item">• 请在预约时间内到达访区域</view>
              <view class="tips-item">• 入园时请向保安出示此二维码</view>
              <view class="tips-item">• 访问结束后请主动签退</view>
            </view>
          </view>
        </view>

        <view class="popup-footer">
          <button class="popup-btn" @click="shareQRCode">分享给访客</button>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import accessApi from '@/api/access.js'

// Tab选项
const tabs = ref([
  { label: '新增预约' },
  { label: '我的预约' }
])
const currentTab = ref(0)

// 访客类型
const visitorTypes = [
  { value: 1, label: '商务访客' },
  { value: 2, label: '面试访客' },
  { value: 3, label: '施工访客' },
  { value: 4, label: '快递访客' },
  { value: 5, label: '其他访客' }
]

// 有效时长选项
const durationOptions = [
  { value: 2, label: '2小时' },
  { value: 4, label: '4小时' },
  { value: 8, label: '8小时' },
  { value: 24, label: '1天' },
  { value: 48, label: '2天' }
]

// 表单数据
const formData = ref({
  visitorName: '',
  visitorPhone: '',
  visitorCompany: '',
  visitorType: null,
  visitorCount: 1,
  plateNumber: '',
  visitEmployeeId: null,
  areaId: null,
  visitReason: '',
  appointmentDate: '',
  appointmentTime: '',
  duration: null
})

// 员工列表
const employeeList = ref([])
const employeeIndex = ref(-1)

// 区域列表
const areaList = ref([])
const areaIndex = ref(-1)

// 访客类型索引
const visitorTypeIndex = ref(-1)

// 有效时长索引
const durationIndex = ref(-1)

// 预约列表
const appointmentList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const hasMore = ref(true)
const pageNum = ref(1)
const pageSize = ref(20)

// 当前预约
const currentAppointment = ref({})

// 页面生命周期
onLoad((options) => {
  loadEmployeeList()
  loadAreaList()

  // 设置默认日期为今天
  const today = new Date()
  formData.value.appointmentDate = formatDate(today)

  // 如果有tab参数，切换到对应tab
  if (options.tab) {
    currentTab.value = parseInt(options.tab)
    if (currentTab.value === 1) {
      loadAppointments()
    }
  }
})

/**
 * 切换Tab
 */
const switchTab = (index) => {
  currentTab.value = index
  if (index === 1) {
    loadAppointments()
  }
}

/**
 * 加载员工列表
 */
const loadEmployeeList = async () => {
  try {
    const result = await accessApi.getEmployeeList({
      pageNum: 1,
      pageSize: 1000,
      enabled: 1
    })

    if (result.success && result.data) {
      employeeList.value = result.data.list || []
    }
  } catch (error) {
    console.error('加载员工列表失败:', error)
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
    }
  } catch (error) {
    console.error('加载区域列表失败:', error)
  }
}

/**
 * 加载预约列表
 */
const loadAppointments = async () => {
  if (loading.value) return

  loading.value = true

  try {
    const result = await accessApi.getVisitorAppointmentList({
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })

    if (result.success && result.data) {
      const newList = result.data.list || []

      if (pageNum.value === 1) {
        appointmentList.value = newList
      } else {
        appointmentList.value.push(...newList)
      }

      hasMore.value = newList.length >= pageSize.value
    }
  } catch (error) {
    console.error('加载预约列表失败:', error)
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
  loadAppointments()
}

/**
 * 加载更多
 */
const loadMore = () => {
  if (!hasMore.value || loading.value) return
  pageNum.value++
  loadAppointments()
}

/**
 * 访客类型变更
 */
const onVisitorTypeChange = (e) => {
  const index = e.detail.value
  visitorTypeIndex.value = index
  formData.value.visitorType = visitorTypes[index].value
}

/**
 * 被访人变更
 */
const onEmployeeChange = (e) => {
  const index = e.detail.value
  employeeIndex.value = index
  formData.value.visitEmployeeId = employeeList.value[index].userId
}

/**
 * 访问区域变更
 */
const onAreaChange = (e) => {
  const index = e.detail.value
  areaIndex.value = index
  formData.value.areaId = areaList.value[index].areaId
}

/**
 * 预约日期变更
 */
const onAppointmentDateChange = (e) => {
  formData.value.appointmentDate = e.detail.value
}

/**
 * 预约时间变更
 */
const onAppointmentTimeChange = (e) => {
  formData.value.appointmentTime = e.detail.value
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
 * 获取访客类型标签
 */
const getVisitorTypeLabel = () => {
  if (!formData.value.visitorType) {
    return '请选择访客类型'
  }
  const type = visitorTypes.find(t => t.value === formData.value.visitorType)
  return type ? type.label : '请选择访客类型'
}

/**
 * 获取被访人标签
 */
const getEmployeeLabel = () => {
  if (!formData.value.visitEmployeeId) {
    return '请选择被访人'
  }
  const employee = employeeList.value.find(e => e.userId === formData.value.visitEmployeeId)
  return employee ? employee.label : '请选择被访人'
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
 * 格式化日期
 */
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

/**
 * 格式化日期时间
 */
const formatDateTime = (date, time) => {
  if (!date || !time) return ''
  return `${date} ${time}`
}

/**
 * 验证表单
 */
const validateForm = () => {
  if (!formData.value.visitorName || formData.value.visitorName.trim().length === 0) {
    uni.showToast({ title: '请输入访客姓名', icon: 'none' })
    return false
  }

  if (!formData.value.visitorPhone || formData.value.visitorPhone.length !== 11) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }

  if (!formData.value.visitorType) {
    uni.showToast({ title: '请选择访客类型', icon: 'none' })
    return false
  }

  if (!formData.value.visitorCount || formData.value.visitorCount < 1) {
    uni.showToast({ title: '请输入同行人数', icon: 'none' })
    return false
  }

  if (!formData.value.visitEmployeeId) {
    uni.showToast({ title: '请选择被访人', icon: 'none' })
    return false
  }

  if (!formData.value.areaId) {
    uni.showToast({ title: '请选择访问区域', icon: 'none' })
    return false
  }

  if (!formData.value.visitReason || formData.value.visitReason.trim().length === 0) {
    uni.showToast({ title: '请输入访问事由', icon: 'none' })
    return false
  }

  if (!formData.value.appointmentDate) {
    uni.showToast({ title: '请选择预约日期', icon: 'none' })
    return false
  }

  if (!formData.value.appointmentTime) {
    uni.showToast({ title: '请选择预约时间', icon: 'none' })
    return false
  }

  if (!formData.value.duration) {
    uni.showToast({ title: '请选择有效时长', icon: 'none' })
    return false
  }

  return true
}

/**
 * 提交预约
 */
const submitAppointment = async () => {
  if (!validateForm()) {
    return
  }

  uni.showLoading({ title: '提交中...', mask: true })

  try {
    const result = await accessApi.createVisitorAppointment(formData.value)

    uni.hideLoading()

    if (result.success) {
      uni.showToast({ title: '预约成功', icon: 'success' })

      // 重置表单
      resetForm()

      // 切换到我的预约
      setTimeout(() => {
        currentTab.value = 1
        pageNum.value = 1
        loadAppointments()
      }, 500)
    } else {
      uni.showToast({ title: result.message || '预约失败', icon: 'none' })
    }
  } catch (error) {
    uni.hideLoading()
    console.error('提交预约失败:', error)
    uni.showToast({ title: '预约失败', icon: 'none' })
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  formData.value = {
    visitorName: '',
    visitorPhone: '',
    visitorCompany: '',
    visitorType: null,
    visitorCount: 1,
    plateNumber: '',
    visitEmployeeId: null,
    areaId: null,
    visitReason: '',
    appointmentDate: formatDate(new Date()),
    appointmentTime: '',
    duration: null
  }
  visitorTypeIndex.value = -1
  employeeIndex.value = -1
  areaIndex.value = -1
  durationIndex.value = -1
}

/**
 * 查看详情
 */
const viewDetail = (item) => {
  uni.navigateTo({
    url: `/pages/access/visitor-appointment-detail?id=${item.appointmentId}`
  })
}

/**
 * 显示二维码
 */
const showQRCode = (item) => {
  currentAppointment.value = item

  // 如果没有二维码，生成二维码
  if (!item.qrCode) {
    generateQRCode(item)
  }

  $refs.qrcodePopup.open()
}

/**
 * 关闭二维码
 */
const closeQRCode = () => {
  $refs.qrcodePopup.close()
}

/**
 * 生成二维码
 */
const generateQRCode = async (item) => {
  try {
    const result = await accessApi.generateVisitorQRCode(item.appointmentId)

    if (result.success && result.data) {
      item.qrCode = result.data.qrCode
      currentAppointment.value.qrCode = result.data.qrCode
    }
  } catch (error) {
    console.error('生成二维码失败:', error)
  }
}

/**
 * 分享二维码
 */
const shareQRCode = () => {
  // TODO: 实现分享功能
  uni.showToast({ title: '分享功能开发中', icon: 'none' })
}

/**
 * 取消预约
 */
const cancelAppointment = (item) => {
  uni.showModal({
    title: '取消预约',
    content: '确定要取消该预约吗？',
    confirmColor: '#ff4d4f',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '取消中...', mask: true })

        try {
          const result = await accessApi.cancelVisitorAppointment(item.appointmentId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '已取消', icon: 'success' })
            onRefresh()
          } else {
            uni.showToast({ title: result.message || '取消失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('取消预约失败:', error)
          uni.showToast({ title: '取消失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 签到
 */
const checkIn = (item) => {
  uni.showModal({
    title: '确认签到',
    content: '确认访客已到达并签到？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '签到中...', mask: true })

        try {
          const result = await accessApi.visitorCheckIn(item.appointmentId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '签到成功', icon: 'success' })
            onRefresh()
          } else {
            uni.showToast({ title: result.message || '签到失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('签到失败:', error)
          uni.showToast({ title: '签到失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 签退
 */
const checkOut = (item) => {
  uni.showModal({
    title: '确认签退',
    content: '确认访客已离开并签退？',
    success: async (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '签退中...', mask: true })

        try {
          const result = await accessApi.visitorCheckOut(item.appointmentId)

          uni.hideLoading()

          if (result.success) {
            uni.showToast({ title: '签退成功', icon: 'success' })
            onRefresh()
          } else {
            uni.showToast({ title: result.message || '签退失败', icon: 'none' })
          }
        } catch (error) {
          uni.hideLoading()
          console.error('签退失败:', error)
          uni.showToast({ title: '签退失败', icon: 'none' })
        }
      }
    }
  })
}

/**
 * 查看历史记录
 */
const viewHistory = () => {
  currentTab.value = 1
  pageNum.value = 1
  loadAppointments()
}

/**
 * 获取状态样式类
 */
const getStatusClass = (status) => {
  const classMap = {
    1: 'status-pending',    // 待访问
    2: 'status-visiting',   // 访问中
    3: 'status-completed',  // 已完成
    4: 'status-cancelled'   // 已取消
  }
  return classMap[status] || 'status-pending'
}

/**
 * 获取状态文本
 */
const getStatusText = (status) => {
  const textMap = {
    1: '待访问',
    2: '访问中',
    3: '已完成',
    4: '已取消'
  }
  return textMap[status] || '未知'
}

/**
 * 返回上一页
 */
const goBack = () => {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
.visitor-appointment-page {
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
        color: #1890ff;
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
        background-color: #1890ff;
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
    height: calc(100vh - 150px);
  }
}

// 表单区块
.form-section {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 15px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    margin-bottom: 15px;
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
}

// 提交容器
.submit-container {
  padding: 20px 0;

  .submit-btn {
    width: 100%;
    height: 48px;
    line-height: 48px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #fff;
    border-radius: 24px;
    font-size: 16px;
    font-weight: 500;
    border: none;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
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

// 预约卡片
.appointment-card {
  background-color: #fff;
  border-radius: 12px;
  padding: 15px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: relative;

  .status-badge {
    position: absolute;
    top: 15px;
    right: 15px;
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;

    &.status-pending {
      background-color: #fff7e6;
      color: #fa8c16;
    }

    &.status-visiting {
      background-color: #e6f7ff;
      color: #1890ff;
    }

    &.status-completed {
      background-color: #f6ffed;
      color: #52c41a;
    }

    &.status-cancelled {
      background-color: #fff1f0;
      color: #ff4d4f;
    }
  }

  .card-section {
    margin-bottom: 12px;

    &:last-of-type {
      margin-bottom: 0;
    }

    .section-label {
      font-size: 13px;
      color: #999;
      margin-bottom: 8px;
    }

    .info-row {
      display: flex;
      margin-bottom: 6px;

      &:last-child {
        margin-bottom: 0;
      }

      .info-label {
        font-size: 14px;
        color: #666;
        min-width: 60px;
      }

      .info-value {
        flex: 1;
        font-size: 14px;
        color: #333;
        word-break: break-all;
      }
    }
  }

  .card-actions {
    display: flex;
    gap: 10px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f0f0f0;

    .action-btn {
      flex: 1;
      height: 36px;
      line-height: 36px;
      border-radius: 8px;
      font-size: 14px;
      text-align: center;
      border: none;

      &.primary-btn {
        background-color: #1890ff;
        color: #fff;
      }

      &.success-btn {
        background-color: #52c41a;
        color: #fff;
      }

      &.cancel-btn {
        background-color: #f5f5f5;
        color: #666;
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

// 二维码弹窗
.qrcode-popup {
  width: 85vw;
  max-height: 85vh;
  background-color: #fff;
  border-radius: 16px;
  overflow: hidden;

  .popup-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px;
    border-bottom: 1px solid #eee;

    .popup-title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .popup-close {
      width: 30px;
      height: 30px;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }

  .qrcode-content {
    padding: 20px;
    max-height: 60vh;
    overflow-y: auto;

    .visitor-info {
      text-align: center;
      margin-bottom: 20px;

      .info-name {
        font-size: 18px;
        font-weight: 500;
        color: #333;
        margin-bottom: 6px;
      }

      .info-phone {
        font-size: 14px;
        color: #999;
      }
    }

    .qrcode-container {
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 20px;
      background-color: #f5f5f5;
      border-radius: 12px;
      margin-bottom: 20px;

      .qrcode-image {
        width: 200px;
        height: 200px;
      }

      .qrcode-placeholder {
        width: 200px;
        height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;

        .placeholder-text {
          font-size: 14px;
          color: #999;
        }
      }
    }

    .visit-info {
      margin-bottom: 20px;

      .visit-row {
        display: flex;
        align-items: center;
        margin-bottom: 10px;

        &:last-child {
          margin-bottom: 0;
        }

        .visit-text {
          margin-left: 8px;
          font-size: 14px;
          color: #666;
        }
      }
    }

    .usage-tips {
      padding: 12px;
      background-color: #fff7e6;
      border-radius: 8px;

      .tips-title {
        font-size: 14px;
        font-weight: 500;
        color: #fa8c16;
        margin-bottom: 8px;
      }

      .tips-list {
        .tips-item {
          font-size: 13px;
          color: #666;
          line-height: 1.6;
        }
      }
    }
  }

  .popup-footer {
    padding: 15px;
    border-top: 1px solid #eee;

    .popup-btn {
      width: 100%;
      height: 44px;
      line-height: 44px;
      background-color: #1890ff;
      color: #fff;
      border-radius: 8px;
      font-size: 15px;
      text-align: center;
      border: none;
    }
  }
}
</style>
