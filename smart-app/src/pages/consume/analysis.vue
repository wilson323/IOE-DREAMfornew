<template>
  <view class="analysis-page">
    <!-- 顶部导航 -->
    <view class="nav-bar">
      <text class="back-btn" @click="goBack">‹</text>
      <text class="nav-title">消费分析</text>
      <view class="nav-actions">
        <text class="refresh-btn" @click="refreshData">刷新</text>
        <text class="export-btn" @click="showExportMenu">导出</text>
      </view>
    </view>

    <!-- 导出菜单弹窗 -->
    <view v-if="exportMenuVisible" class="export-menu-overlay" @click="hideExportMenu">
      <view class="export-menu" @click.stop>
        <view class="export-menu-title">选择导出格式</view>
        <view class="export-menu-item" @click="exportPdf">
          <text class="export-menu-icon">📄</text>
          <text class="export-menu-text">导出PDF报告</text>
        </view>
        <view class="export-menu-item" @click="exportExcel">
          <text class="export-menu-icon">📊</text>
          <text class="export-menu-text">导出Excel报告</text>
        </view>
      </view>
    </view>

    <!-- 时间选择器 -->
    <view class="time-selector">
      <view
        v-for="(period, index) in periods"
        :key="index"
        :class="['period-item', { active: selectedPeriod === period.value }]"
        @click="selectPeriod(period.value)"
      >
        <text class="period-text">{{ period.label }}</text>
      </view>
    </view>

    <!-- 消费总览 -->
    <view class="overview-section">
      <view class="overview-card">
        <view class="overview-icon">💰</view>
        <view class="overview-info">
          <text class="overview-label">总消费</text>
          <text class="overview-value">¥{{ formatAmount(totalAmount) }}</text>
        </view>
      </view>
      <view class="overview-card">
        <view class="overview-icon">📊</view>
        <view class="overview-info">
          <text class="overview-label">消费次数</text>
          <text class="overview-value">{{ totalCount }}次</text>
        </view>
      </view>
      <view class="overview-card">
        <view class="overview-icon">📈</view>
        <view class="overview-info">
          <text class="overview-label">日均消费</text>
          <text class="overview-value">¥{{ formatAmount(dailyAverage) }}</text>
        </view>
      </view>
    </view>

    <!-- 消费趋势图表 -->
    <view class="chart-section">
      <view class="section-header">
        <text class="section-title">消费趋势</text>
        <view class="chart-type-selector">
          <view
            :class="['chart-type-btn', { active: chartType === 'line' }]"
            @click="switchChartType('line')"
          >
            <text class="chart-type-text">折线图</text>
          </view>
          <view
            :class="['chart-type-btn', { active: chartType === 'bar' }]"
            @click="switchChartType('bar')"
          >
            <text class="chart-type-text">柱状图</text>
          </view>
          <view
            :class="['chart-type-btn', { active: chartType === 'radar' }]"
            @click="switchChartType('radar')"
          >
            <text class="chart-type-text">雷达图</text>
          </view>
          <view
            :class="['chart-type-btn', { active: chartType === 'scatter' }]"
            @click="switchChartType('scatter')"
          >
            <text class="chart-type-text">散点图</text>
          </view>
        </view>
      </view>
      <view class="chart-container">
        <canvas
          canvas-id="trendChart"
          id="trendChart"
          class="trend-chart"
          @touchstart="handleChartTouch"
        ></canvas>
      </view>
    </view>

    <!-- 消费分类占比 -->
    <view class="category-section">
      <view class="section-header">
        <text class="section-title">消费分类</text>
      </view>
      <!-- 饼图 -->
      <view class="pie-chart-container">
        <canvas
          canvas-id="pieChart"
          id="pieChart"
          class="pie-chart"
        ></canvas>
        <!-- 图例 -->
        <view class="pie-legend">
          <view
            v-for="(category, index) in categoryData"
            :key="index"
            class="legend-item"
          >
            <view
              class="legend-color"
              :style="{ backgroundColor: category.color }"
            ></view>
            <text class="legend-label">{{ category.name }}</text>
            <text class="legend-percent">{{ category.percent }}%</text>
          </view>
        </view>
      </view>
      <!-- 列表视图 -->
      <view class="category-list">
        <view
          v-for="(category, index) in categoryData"
          :key="index"
          class="category-item"
        >
          <view class="category-info">
            <view class="category-icon">{{ category.icon }}</view>
            <view class="category-details">
              <text class="category-name">{{ category.name }}</text>
              <text class="category-amount">¥{{ formatAmount(category.amount) }}</text>
            </view>
          </view>
          <view class="category-bar">
            <view
              class="category-progress"
              :style="{ width: category.percent + '%' }"
            ></view>
          </view>
          <text class="category-percent">{{ category.percent }}%</text>
        </view>
      </view>
    </view>

    <!-- 智能推荐 -->
    <view class="recommendation-section">
      <view class="section-header">
        <text class="section-title">💡 智能推荐</text>
        <text class="section-subtitle">基于您的消费习惯</text>
      </view>
      <view class="recommendation-list">
        <view
          v-for="(recommend, index) in recommendations"
          :key="index"
          class="recommend-card"
          @click="handleRecommendClick(recommend)"
        >
          <view class="recommend-icon">{{ recommend.icon }}</view>
          <view class="recommend-content">
            <text class="recommend-title">{{ recommend.title }}</text>
            <text class="recommend-desc">{{ recommend.description }}</text>
          </view>
          <text class="recommend-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 消费习惯分析 -->
    <view class="habit-section">
      <view class="section-header">
        <text class="section-title">📊 消费习惯</text>
      </view>
      <view class="habit-grid">
        <view class="habit-item">
          <text class="habit-label">最常消费</text>
          <text class="habit-value">{{ mostFrequentTime }}</text>
        </view>
        <view class="habit-item">
          <text class="habit-label">最喜欢的</text>
          <text class="habit-value">{{ favoriteCategory }}</text>
        </view>
        <view class="habit-item">
          <text class="habit-label">平均单笔</text>
          <text class="habit-value">¥{{ formatAmount(averagePerOrder) }}</text>
        </view>
        <view class="habit-item">
          <text class="habit-label">消费天数</text>
          <text class="habit-value">{{ consumeDays }}天</text>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-overlay" v-if="loading">
      <view class="loading-spinner"></view>
      <text class="loading-text">加载中...</text>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/store/modules/system/user.js'
import consumeApi, { analysisApi } from '@/api/business/consume/consume-api.js'
import cacheManager from '@/utils/cache-manager.js'

const userStore = useUserStore()

// 时间周期选项
const periods = [
  { label: '本周', value: 'week' },
  { label: '本月', value: 'month' },
  { label: '本季', value: 'quarter' }
]
const selectedPeriod = ref('week')

// 消费数据
const totalAmount = ref(0)
const totalCount = ref(0)
const dailyAverage = ref(0)
const categoryData = ref([])
const trendData = ref([])

// 图表类型
const chartType = ref('line') // line-折线图, bar-柱状图, radar-雷达图, scatter-散点图

// 饼图颜色配置
const pieColors = ['#667eea', '#764ba2', '#f093fb', '#f5576c']

// 消费习惯
const mostFrequentTime = ref('午餐时段')
const favoriteCategory = ref('中餐')
const averagePerOrder = ref(0)
const consumeDays = ref(0)

// 智能推荐
const recommendations = ref([])

// 加载状态
const loading = ref(false)

// WebSocket相关
let websocket = null
const wsConnected = ref(false)
const wsMessages = ref([])
const wsNotificationVisible = ref(false)

// 导出相关
const exportMenuVisible = ref(false)

// 格式化金额
const formatAmount = (amount) => {
  if (!amount && amount !== 0) return '0.00'
  return Number(amount).toFixed(2)
}

// 选择时间周期
const selectPeriod = (period) => {
  selectedPeriod.value = period
  loadAnalysisData()
}

// 加载消费分析数据
const loadAnalysisData = async () => {
  loading.value = true
  try {
    const userId = userStore.employeeId
    const cacheKey = `consume_analysis_${userId}_${selectedPeriod.value}`

    // 先尝试从缓存获取
    const cachedData = cacheManager.getCache(cacheKey)
    if (cachedData) {
      console.log('[消费分析] 使用缓存数据')
      applyAnalysisData(cachedData)
      loading.value = false
      return
    }

    // 请求API
    const result = await analysisApi.getConsumptionAnalysis({
      userId,
      period: selectedPeriod.value
    })

    if (result.success && result.data) {
      applyAnalysisData(result.data)
      // 缓存数据，有效期30分钟
      cacheManager.setCache(cacheKey, result.data, 1800000)
      console.log('[消费分析] 已缓存数据')
    } else {
      // API返回失败，使用模拟数据
      loadMockData()
    }
  } catch (error) {
    console.error('加载消费分析失败:', error)
    // 使用模拟数据（开发阶段）
    loadMockData()
  } finally {
    loading.value = false
  }
}

// 应用分析数据
const applyAnalysisData = (data) => {
  totalAmount.value = data.totalAmount || 0
  totalCount.value = data.totalCount || 0
  dailyAverage.value = data.dailyAverage || 0

  // 为分类数据添加颜色
  const categories = data.categories || []
  categoryData.value = categories.map((cat, index) => ({
    ...cat,
    color: pieColors[index % pieColors.length]
  }))

  trendData.value = data.trend || []

  // 消费习惯
  mostFrequentTime.value = data.mostFrequentTime || '午餐时段'
  favoriteCategory.value = data.favoriteCategory || '中餐'
  averagePerOrder.value = data.averagePerOrder || 0
  consumeDays.value = data.consumeDays || 0

  // 智能推荐
  recommendations.value = generateRecommendations(data)

  // 绘制图表
  setTimeout(() => {
    if (chartType.value === 'line') {
      drawTrendChart()
    } else {
      drawBarChart()
    }
    drawPieChart()
  }, 100)
}

// 生成智能推荐
const generateRecommendations = (data) => {
  const recommends = []

  // 根据消费习惯生成推荐
  if (data.averagePerOrder > 50) {
    recommends.push({
      icon: '🍱️',
      title: '套餐优惠',
      description: '根据您的消费习惯，推荐购买套餐更实惠',
      action: 'ordering'
    })
  }

  if (data.favoriteCategory === '中餐' && data.mostFrequentTime === '午餐时段') {
    recommends.push({
      icon: '⏰',
      title: '错峰优惠',
      description: '11:00前订餐享受9折优惠',
      action: 'discount'
    })
  }

  if (data.totalCount > 20) {
    recommends.push({
      icon: '🎁',
      title: '会员特权',
      description: '您已达到VIP等级，可享受专属优惠',
      action: 'vip'
    })
  }

  recommends.push({
    icon: '💳',
    title: '充值优惠',
    description: '当前充值满500送50，限时优惠',
    action: 'recharge'
  })

  return recommends
}

// 加载模拟数据（开发阶段）
const loadMockData = () => {
  const mockData = {
    totalAmount: 1258.50,
    totalCount: 42,
    dailyAverage: 179.79,
    categories: [
      { name: '中餐', amount: 580, percent: 46, icon: '🍚' },
      { name: '晚餐', amount: 420, percent: 33, icon: '🍜' },
      { name: '早餐', amount: 158.5, percent: 13, icon: '🥐' },
      { name: '其他', amount: 100, percent: 8, icon: '🍰' }
    ],
    trend: [120, 180, 95, 220, 150, 200, 175],
    mostFrequentTime: '午餐时段',
    favoriteCategory: '中餐',
    averagePerOrder: 29.96,
    consumeDays: 18
  }

  applyAnalysisData(mockData)
}

// 绘制趋势图表
const drawTrendChart = () => {
  const ctx = uni.createCanvasContext('trendChart')

  const canvasWidth = 330
  const canvasHeight = 200
  const padding = 40
  const chartWidth = canvasWidth - padding * 2
  const chartHeight = canvasHeight - padding * 2

  // 清空画布
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  // 绘制背景网格
  ctx.setStrokeStyle('#f0f0f0')
  ctx.setLineWidth(1)
  for (let i = 0; i <= 4; i++) {
    const y = padding + (chartHeight / 4) * i
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(canvasWidth - padding, y)
    ctx.stroke()
  }

  // 绘制趋势线
  const data = trendData.value
  if (data.length === 0) return

  const maxValue = Math.max(...data)
  const minValue = Math.min(...data)
  const valueRange = maxValue - minValue || 1

  ctx.setStrokeStyle('#667eea')
  ctx.setLineWidth(3)
  ctx.setLineCap('round')
  ctx.setLineJoin('round')

  ctx.beginPath()
  data.forEach((value, index) => {
    const x = padding + (chartWidth / (data.length - 1)) * index
    const y = padding + chartHeight - ((value - minValue) / valueRange) * chartHeight

    if (index === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  })
  ctx.stroke()

  // 绘制数据点
  data.forEach((value, index) => {
    const x = padding + (chartWidth / (data.length - 1)) * index
    const y = padding + chartHeight - ((value - minValue) / valueRange) * chartHeight

    // 外圈
    ctx.setFillStyle('#fff')
    ctx.beginPath()
    ctx.arc(x, y, 6, 0, 2 * Math.PI)
    ctx.fill()

    // 内圈
    ctx.setFillStyle('#667eea')
    ctx.beginPath()
    ctx.arc(x, y, 4, 0, 2 * Math.PI)
    ctx.fill()
  })

  ctx.draw()
}

// 切换图表类型
const switchChartType = (type) => {
  chartType.value = type
  uni.vibrateShort()

  setTimeout(() => {
    if (type === 'line') {
      drawTrendChart()
    } else if (type === 'bar') {
      drawBarChart()
    } else if (type === 'radar') {
      drawRadarChart()
    } else if (type === 'scatter') {
      drawScatterChart()
    }
  }, 100)
}

// 绘制饼图
const drawPieChart = () => {
  const ctx = uni.createCanvasContext('pieChart')
  const canvasWidth = 300
  const canvasHeight = 300
  const centerX = canvasWidth / 2
  const centerY = canvasHeight / 2
  const radius = 100

  // 清空画布
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  const data = categoryData.value
  if (data.length === 0) return

  // 计算总值
  const total = data.reduce((sum, item) => sum + item.amount, 0)
  if (total === 0) return

  // 绘制饼图
  let currentAngle = -0.5 * Math.PI // 从顶部开始

  data.forEach((item, index) => {
    const sliceAngle = (item.amount / total) * 2 * Math.PI
    const endAngle = currentAngle + sliceAngle

    // 绘制扇形
    ctx.setFillStyle(item.color)
    ctx.beginPath()
    ctx.moveTo(centerX, centerY)
    ctx.arc(centerX, centerY, radius, currentAngle, endAngle)
    ctx.closePath()
    ctx.fill()

    // 绘制白色边框
    ctx.setStrokeStyle('#fff')
    ctx.setLineWidth(2)
    ctx.stroke()

    currentAngle = endAngle
  })

  // 绘制中心圆（实现环形饼图效果）
  ctx.setFillStyle('#fff')
  ctx.beginPath()
  ctx.arc(centerX, centerY, radius * 0.6, 0, 2 * Math.PI)
  ctx.closePath()
  ctx.fill()

  ctx.draw()
}

// 绘制柱状图
const drawBarChart = () => {
  const ctx = uni.createCanvasContext('trendChart')
  const canvasWidth = 330
  const canvasHeight = 200
  const padding = 40
  const chartWidth = canvasWidth - padding * 2
  const chartHeight = canvasHeight - padding * 2

  // 清空画布
  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  // 绘制背景网格
  ctx.setStrokeStyle('#f0f0f0')
  ctx.setLineWidth(1)
  for (let i = 0; i <= 4; i++) {
    const y = padding + (chartHeight / 4) * i
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(canvasWidth - padding, y)
    ctx.stroke()
  }

  // 绘制柱状图
  const data = trendData.value
  if (data.length === 0) return

  const maxValue = Math.max(...data)
  const minValue = Math.min(...data)
  const valueRange = maxValue - minValue || 1

  const barWidth = (chartWidth / data.length) * 0.6
  const barGap = (chartWidth / data.length) * 0.4

  data.forEach((value, index) => {
    const x = padding + (chartWidth / data.length) * index + barGap / 2
    const barHeight = ((value - minValue) / valueRange) * chartHeight
    const y = padding + chartHeight - barHeight

    // 绘制柱子（渐变色）
    const gradient = ctx.createLinearGradient(x, y, x, y + barHeight)
    gradient.addColorStop(0, '#667eea')
    gradient.addColorStop(1, '#764ba2')
    ctx.setFillStyle(gradient)

    // 绘制圆角柱子
    const radius = 4
    ctx.beginPath()
    ctx.moveTo(x + radius, y)
    ctx.lineTo(x + barWidth - radius, y)
    ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + radius)
    ctx.lineTo(x + barWidth, y + barHeight - radius)
    ctx.quadraticCurveTo(x + barWidth, y + barHeight, x + barWidth - radius, y + barHeight)
    ctx.lineTo(x + radius, y + barHeight)
    ctx.quadraticCurveTo(x, y + barHeight, x, y + barHeight - radius)
    ctx.lineTo(x, y + radius)
    ctx.quadraticCurveTo(x, y, x + radius, y)
    ctx.closePath()
    ctx.fill()

    // 绘制数值标签
    ctx.setFillStyle('#667eea')
    ctx.setFontSize(10)
    ctx.setTextAlign('center')
    ctx.fillText(value.toFixed(0), x + barWidth / 2, y - 5)
  })

  ctx.draw()
}

// 绘制雷达图
const drawRadarChart = () => {
  const ctx = uni.createCanvasContext('trendChart')
  const canvasWidth = 330
  const canvasHeight = 330
  const centerX = canvasWidth / 2
  const centerY = canvasHeight / 2
  const radius = 100

  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  // 雷达图维度
  const dimensions = ['早餐', '午餐', '晚餐', '夜宵', '零食', '饮品']
  const dimensionCount = dimensions.length
  const angleStep = (2 * Math.PI) / dimensionCount

  // 获取分类数据
  const data = categoryData.value || []
  const values = []

  for (let i = 0; i < dimensionCount; i++) {
    const category = data.find(d => d.name.includes(dimensions[i]))
    values.push(category ? category.amount : 0)
  }

  const maxValue = Math.max(...values, 1)

  // 绘制背景网格（5层）
  for (let i = 5; i > 0; i--) {
    const levelRadius = (radius / 5) * i
    ctx.beginPath()
    ctx.setStrokeStyle('#e0e0e0')
    ctx.setLineWidth(1)

    for (let j = 0; j < dimensionCount; j++) {
      const angle = j * angleStep - Math.PI / 2
      const x = centerX + Math.cos(angle) * levelRadius
      const y = centerY + Math.sin(angle) * levelRadius

      if (j === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    }

    ctx.closePath()
    ctx.stroke()
  }

  // 绘制轴线和标签
  ctx.setFontSize(10)
  ctx.setTextAlign('center')
  ctx.setTextBaseline('middle')

  for (let i = 0; i < dimensionCount; i++) {
    const angle = i * angleStep - Math.PI / 2
    const x = centerX + Math.cos(angle) * radius
    const y = centerY + Math.sin(angle) * radius

    // 轴线
    ctx.beginPath()
    ctx.setStrokeStyle('#d0d0d0')
    ctx.setLineWidth(1)
    ctx.moveTo(centerX, centerY)
    ctx.lineTo(x, y)
    ctx.stroke()

    // 标签
    const labelX = centerX + Math.cos(angle) * (radius + 20)
    const labelY = centerY + Math.sin(angle) * (radius + 20)
    ctx.setFillStyle('#666')
    ctx.fillText(dimensions[i], labelX, labelY)
  }

  // 绘制数据区域
  ctx.beginPath()
  ctx.setFillStyle('rgba(102, 126, 234, 0.3)')
  ctx.setStrokeStyle('#667eea')
  ctx.setLineWidth(2)

  for (let i = 0; i < dimensionCount; i++) {
    const angle = i * angleStep - Math.PI / 2
    const value = values[i] / maxValue
    const x = centerX + Math.cos(angle) * (radius * value)
    const y = centerY + Math.sin(angle) * (radius * value)

    if (i === 0) {
      ctx.moveTo(x, y)
    } else {
      ctx.lineTo(x, y)
    }
  }

  ctx.closePath()
  ctx.fill()
  ctx.stroke()

  // 绘制数据点
  for (let i = 0; i < dimensionCount; i++) {
    const angle = i * angleStep - Math.PI / 2
    const value = values[i] / maxValue
    const x = centerX + Math.cos(angle) * (radius * value)
    const y = centerY + Math.sin(angle) * (radius * value)

    ctx.beginPath()
    ctx.arc(x, y, 4, 0, 2 * Math.PI)
    ctx.setFillStyle('#667eea')
    ctx.fill()
  }

  ctx.draw()
}

// 绘制散点图
const drawScatterChart = () => {
  const ctx = uni.createCanvasContext('trendChart')
  const canvasWidth = 330
  const canvasHeight = 200
  const padding = 40

  ctx.clearRect(0, 0, canvasWidth, canvasHeight)

  // 获取分类数据
  const data = categoryData.value || []
  if (data.length === 0) return

  const chartWidth = canvasWidth - padding * 2
  const chartHeight = canvasHeight - padding * 2

  // 找出最大值
  const maxAmount = Math.max(...data.map(d => d.amount))
  const maxCount = Math.max(...data.map(d => d.count))

  // 绘制坐标轴
  ctx.setStrokeStyle('#d0d0d0')
  ctx.setLineWidth(1)

  // Y轴
  ctx.beginPath()
  ctx.moveTo(padding, padding)
  ctx.lineTo(padding, canvasHeight - padding)
  ctx.stroke()

  // X轴
  ctx.beginPath()
  ctx.moveTo(padding, canvasHeight - padding)
  ctx.lineTo(canvasWidth - padding, canvasHeight - padding)
  ctx.stroke()

  // 绘制网格
  for (let i = 0; i <= 4; i++) {
    const y = padding + (chartHeight / 4) * i
    ctx.beginPath()
    ctx.moveTo(padding, y)
    ctx.lineTo(canvasWidth - padding, y)
    ctx.stroke()

    const x = padding + (chartWidth / 4) * i
    ctx.beginPath()
    ctx.moveTo(x, padding)
    ctx.lineTo(x, canvasHeight - padding)
    ctx.stroke()
  }

  // 绘制散点
  data.forEach((item, index) => {
    const x = padding + (item.amount / maxAmount) * chartWidth
    const y = canvasHeight - padding - (item.count / maxCount) * chartHeight

    // 绘制点
    const gradient = ctx.createRadialGradient(x, y, 0, x, y, 10)
    gradient.addColorStop(0, 'rgba(102, 126, 234, 0.8)')
    gradient.addColorStop(1, 'rgba(102, 126, 234, 0.2)')
    ctx.setFillStyle(gradient)

    ctx.beginPath()
    ctx.arc(x, y, 10, 0, 2 * Math.PI)
    ctx.fill()

    // 绘制边框
    ctx.setStrokeStyle(pieColors[index % pieColors.length])
    ctx.setLineWidth(2)
    ctx.stroke()
  })

  // 绘制坐标轴标签
  ctx.setFillStyle('#999')
  ctx.setFontSize(9)
  ctx.setTextAlign('center')

  // X轴标签
  ctx.fillText('消费金额', canvasWidth / 2, canvasHeight - 10)

  // Y轴标签
  ctx.save()
  ctx.translate(15, canvasHeight / 2)
  ctx.rotate(-Math.PI / 2)
  ctx.fillText('消费次数', 0, 0)
  ctx.restore()

  ctx.draw()
}

// 处理图表触摸
const handleChartTouch = (e) => {
  // 可以添加触摸交互，显示具体数据点
  console.log('Chart touched:', e)
}

// 处理推荐点击
const handleRecommendClick = (recommend) => {
  uni.vibrateShort()

  switch (recommend.action) {
    case 'ordering':
      uni.navigateTo({ url: '/pages/consume/ordering' })
      break
    case 'recharge':
      uni.navigateTo({ url: '/pages/consume/recharge' })
      break
    case 'discount':
      uni.showToast({ title: '已为您展示优惠菜品', icon: 'none' })
      break
    case 'vip':
      uni.showToast({ title: '会员特权已激活', icon: 'success' })
      break
    default:
      uni.showToast({ title: '功能开发中', icon: 'none' })
  }
}

// 刷新数据
const refreshData = () => {
  uni.vibrateShort()
  loadAnalysisData()
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// ==================== WebSocket相关函数 ====================

// 初始化WebSocket连接
const initWebSocket = () => {
  try {
    const userId = userStore.userInfo?.userId
    if (!userId) {
      console.warn('[WebSocket] 用户未登录')
      return
    }

    // 构建WebSocket URL
    const wsUrl = `ws://localhost:8094/ws/consume?userId=${userId}`

    console.log('[WebSocket] 正在连接:', wsUrl)
    websocket = uni.connectSocket({
      url: wsUrl,
      protocols: ['websocket']
    })

    // 监听连接打开事件
    websocket.onOpen(() => {
      console.log('[WebSocket] 连接成功')
      wsConnected.value = true

      // 发送心跳
      startHeartbeat()
    })

    // 监听收到消息事件
    websocket.onMessage((event) => {
      console.log('[WebSocket] 收到消息:', event.data)
      handleWebSocketMessage(event.data)
    })

    // 监听连接关闭事件
    websocket.onClose(() => {
      console.log('[WebSocket] 连接关闭')
      wsConnected.value = false
      stopHeartbeat()
    })

    // 监听错误事件
    websocket.onError((error) => {
      console.error('[WebSocket] 连接错误:', error)
      wsConnected.value = false
    })
  } catch (error) {
    console.error('[WebSocket] 初始化失败:', error)
  }
}

// 处理WebSocket消息
const handleWebSocketMessage = (dataStr) => {
  try {
    const data = JSON.parse(dataStr)

    switch (data.type) {
      case 'welcome':
        console.log('[WebSocket] 欢迎消息:', data.data.message)
        break

      case 'consume_notification':
        // 消费通知
        handleConsumeNotification(data.data)
        break

      case 'balance_update':
        // 余额更新
        handleBalanceUpdate(data.data)
        break

      case 'system_notification':
        // 系统公告
        handleSystemNotification(data.data)
        break

      case 'pong':
        // 心跳响应
        console.log('[WebSocket] 收到pong响应')
        break

      default:
        console.log('[WebSocket] 未知消息类型:', data.type)
    }
  } catch (error) {
    console.error('[WebSocket] 消息解析失败:', error)
  }
}

// 处理消费通知
const handleConsumeNotification = (consumeData) => {
  uni.vibrateShort()

  const message = consumeData.message || '消费成功'

  // 添加到消息列表
  wsMessages.value.unshift({
    id: Date.now(),
    type: consumeData.type,
    message: message,
    time: new Date().toLocaleTimeString(),
    data: consumeData
  })

  // 限制消息数量
  if (wsMessages.value.length > 50) {
    wsMessages.value = wsMessages.value.slice(0, 50)
  }

  // 显示通知
  uni.showToast({
    title: message,
    icon: 'success',
    duration: 2000
  })

  // 刷新数据
  loadAnalysisData()
}

// 处理余额更新
const handleBalanceUpdate = (balanceData) => {
  console.log('[WebSocket] 余额更新:', balanceData)

  // 刷新数据
  loadAnalysisData()
}

// 处理系统公告
const handleSystemNotification = (notification) => {
  uni.vibrateShort()

  uni.showModal({
    title: notification.title,
    content: notification.content,
    showCancel: false,
    confirmText: '我知道了'
  })
}

// 心跳定时器
let heartbeatTimer = null

// 启动心跳
const startHeartbeat = () => {
  stopHeartbeat()

  heartbeatTimer = setInterval(() => {
    if (websocket && wsConnected.value) {
      try {
        websocket.send({
          data: JSON.stringify({ action: 'ping' })
        })
        console.log('[WebSocket] 发送ping')
      } catch (error) {
        console.error('[WebSocket] 发送ping失败:', error)
      }
    }
  }, 30000) // 30秒一次心跳
}

// 停止心跳
const stopHeartbeat = () => {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer)
    heartbeatTimer = null
  }
}

// 关闭WebSocket连接
const closeWebSocket = () => {
  stopHeartbeat()

  if (websocket) {
    try {
      websocket.close()
      console.log('[WebSocket] 主动关闭连接')
    } catch (error) {
      console.error('[WebSocket] 关闭连接失败:', error)
    }

    websocket = null
    wsConnected.value = false
  }
}

// ==================== 导出相关函数 ====================

// 显示导出菜单
const showExportMenu = () => {
  uni.vibrateShort()
  exportMenuVisible.value = true
}

// 隐藏导出菜单
const hideExportMenu = () => {
  exportMenuVisible.value = false
}

// 导出PDF报告
const exportPdf = () => {
  hideExportMenu()
  uni.vibrateShort()

  const userId = userStore.userInfo?.userId
  if (!userId) {
    uni.showToast({ title: '用户未登录', icon: 'none' })
    return
  }

  uni.showLoading({ title: '正在生成PDF...' })

  try {
    // 构建下载链接
    const downloadUrl = `http://localhost:8094/api/v1/consume/mobile/analysis/export/pdf?userId=${userId}&period=${selectedPeriod.value}`

    // 在H5环境可以直接下载，在App环境需要使用其他方式
    // #ifdef H5
    window.open(downloadUrl, '_blank')
    // #endif

    // #ifndef H5
    uni.downloadFile({
      url: downloadUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            success: () => {
              uni.hideLoading()
              uni.showToast({ title: 'PDF导出成功', icon: 'success' })
            },
            fail: () => {
              uni.hideLoading()
              uni.showToast({ title: '打开文件失败', icon: 'none' })
            }
          })
        } else {
          uni.hideLoading()
          uni.showToast({ title: 'PDF导出失败', icon: 'none' })
        }
      },
      fail: (error) => {
        uni.hideLoading()
        console.error('[导出] PDF下载失败:', error)
        uni.showToast({ title: 'PDF下载失败', icon: 'none' })
      }
    })
    // #endif

  } catch (error) {
    uni.hideLoading()
    console.error('[导出] PDF导出异常:', error)
    uni.showToast({ title: 'PDF导出失败', icon: 'none' })
  }
}

// 导出Excel报告
const exportExcel = () => {
  hideExportMenu()
  uni.vibrateShort()

  const userId = userStore.userInfo?.userId
  if (!userId) {
    uni.showToast({ title: '用户未登录', icon: 'none' })
    return
  }

  uni.showLoading({ title: '正在生成Excel...' })

  try {
    // 构建下载链接
    const downloadUrl = `http://localhost:8094/api/v1/consume/mobile/analysis/export/excel?userId=${userId}&period=${selectedPeriod.value}`

    // #ifdef H5
    window.open(downloadUrl, '_blank')
    // #endif

    // #ifndef H5
    uni.downloadFile({
      url: downloadUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          uni.openDocument({
            filePath: res.tempFilePath,
            showMenu: true,
            success: () => {
              uni.hideLoading()
              uni.showToast({ title: 'Excel导出成功', icon: 'success' })
            },
            fail: () => {
              uni.hideLoading()
              uni.showToast({ title: '打开文件失败', icon: 'none' })
            }
          })
        } else {
          uni.hideLoading()
          uni.showToast({ title: 'Excel导出失败', icon: 'none' })
        }
      },
      fail: (error) => {
        uni.hideLoading()
        console.error('[导出] Excel下载失败:', error)
        uni.showToast({ title: 'Excel下载失败', icon: 'none' })
      }
    })
    // #endif

  } catch (error) {
    uni.hideLoading()
    console.error('[导出] Excel导出异常:', error)
    uni.showToast({ title: 'Excel导出失败', icon: 'none' })
  }
}

// 页面生命周期
onMounted(() => {
  loadAnalysisData()

  // 初始化WebSocket连接
  initWebSocket()
})

onUnmounted(() => {
  // 页面卸载时关闭WebSocket连接
  closeWebSocket()
})

onShow(() => {
  // 页面显示时刷新数据
  loadAnalysisData()

  // 如果WebSocket未连接，尝试重新连接
  if (!wsConnected.value) {
    initWebSocket()
  }
})
</script>

<style lang="scss" scoped>
.analysis-page {
  min-height: 100vh;
  background: #f5f7fa;
  padding-bottom: 30rpx;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 88rpx;
  padding: 0 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #e8e8e8;

  .back-btn {
    font-size: 48rpx;
    color: #333;
    font-weight: 300;
  }

  .nav-title {
    font-size: 36rpx;
    font-weight: 600;
    color: #333;
  }

  .refresh-btn {
    font-size: 28rpx;
    color: #667eea;
  }

  .nav-actions {
    display: flex;
    gap: 24rpx;
  }

  .export-btn {
    font-size: 28rpx;
    color: #667eea;
  }
}

.time-selector {
  display: flex;
  background: #fff;
  padding: 24rpx 32rpx;
  margin-bottom: 24rpx;

  .period-item {
    flex: 1;
    text-align: center;
    padding: 16rpx 0;
    margin: 0 8rpx;
    border-radius: 12rpx;
    transition: all 0.3s ease;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

      .period-text {
        color: #fff;
        font-weight: 600;
      }
    }

    .period-text {
      font-size: 28rpx;
      color: #666;
    }
  }
}

.overview-section {
  display: flex;
  gap: 16rpx;
  padding: 0 32rpx 24rpx;

  .overview-card {
    flex: 1;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

    .overview-icon {
      font-size: 48rpx;
      margin-bottom: 12rpx;
    }

    .overview-info {
      text-align: center;

      .overview-label {
        display: block;
        font-size: 22rpx;
        color: rgba(255, 255, 255, 0.85);
        margin-bottom: 8rpx;
      }

      .overview-value {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #fff;
      }
    }
  }
}

.chart-section,
.category-section,
.recommendation-section,
.habit-section {
  background: #fff;
  margin: 0 32rpx 24rpx;
  border-radius: 16rpx;
  padding: 32rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.06);

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24rpx;

    .section-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .section-subtitle {
      font-size: 24rpx;
      color: #999;
    }
  }
}

.chart-container {
  width: 100%;
  height: 400rpx;

  .trend-chart {
    width: 100%;
    height: 100%;
  }
}

// 图表类型选择器
.chart-type-selector {
  display: flex;
  gap: 8rpx;
  margin-bottom: 24rpx;
  flex-wrap: wrap;

  .chart-type-btn {
    min-width: 120rpx;
    height: 56rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: #f5f7fa;
    border-radius: 8rpx;
    transition: all 0.3s ease;
    padding: 0 12rpx;

    &.active {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);

      .chart-type-text {
        color: #fff;
        font-weight: 600;
      }
    }

    .chart-type-text {
      font-size: 22rpx;
      color: #666;
      white-space: nowrap;
    }
  }
}

// 饼图容器
.pie-chart-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 32rpx 0;

  .pie-chart {
    width: 300rpx;
    height: 300rpx;
    margin-bottom: 32rpx;
  }

  .pie-legend {
    width: 100%;
    display: flex;
    flex-direction: column;
    gap: 16rpx;

    .legend-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12rpx 16rpx;
      background: #f8f9fa;
      border-radius: 8rpx;

      .legend-color {
        width: 24rpx;
        height: 24rpx;
        border-radius: 4rpx;
        margin-right: 16rpx;
        flex-shrink: 0;
      }

      .legend-label {
        flex: 1;
        font-size: 26rpx;
        color: #333;
      }

      .legend-percent {
        font-size: 28rpx;
        color: #667eea;
        font-weight: 600;
      }
    }
  }
}

.category-list {
  .category-item {
    margin-bottom: 24rpx;

    &:last-child {
      margin-bottom: 0;
    }

    .category-info {
      display: flex;
      align-items: center;
      margin-bottom: 12rpx;

      .category-icon {
        font-size: 36rpx;
        margin-right: 16rpx;
      }

      .category-details {
        flex: 1;
        display: flex;
        justify-content: space-between;
        align-items: center;

        .category-name {
          font-size: 28rpx;
          color: #333;
          font-weight: 500;
        }

        .category-amount {
          font-size: 28rpx;
          color: #ff4d4f;
          font-weight: 600;
        }
      }
    }

    .category-bar {
      height: 12rpx;
      background: #f0f0f0;
      border-radius: 6rpx;
      overflow: hidden;
      margin-bottom: 8rpx;

      .category-progress {
        height: 100%;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
        border-radius: 6rpx;
        transition: width 0.5s ease;
      }
    }

    .category-percent {
      font-size: 22rpx;
      color: #999;
      display: block;
      text-align: right;
    }
  }
}

.recommendation-list {
  .recommend-card {
    display: flex;
    align-items: center;
    padding: 24rpx;
    background: linear-gradient(135deg, #f8f9ff 0%, #f0f5ff 100%);
    border-radius: 12rpx;
    margin-bottom: 16rpx;
    border: 2rpx solid #e6f0ff;
    transition: all 0.3s ease;

    &:active {
      transform: scale(0.98);
      background: linear-gradient(135deg, #eef2ff 0%, #e6f0ff 100%);
    }

    &:last-child {
      margin-bottom: 0;
    }

    .recommend-icon {
      font-size: 48rpx;
      margin-right: 16rpx;
    }

    .recommend-content {
      flex: 1;

      .recommend-title {
        display: block;
        font-size: 28rpx;
        font-weight: 600;
        color: #333;
        margin-bottom: 8rpx;
      }

      .recommend-desc {
        display: block;
        font-size: 24rpx;
        color: #666;
        line-height: 1.5;
      }
    }

    .recommend-arrow {
      font-size: 40rpx;
      color: #667eea;
      font-weight: 300;
    }
  }
}

.habit-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16rpx;

  .habit-item {
    background: #f8f9fa;
    border-radius: 12rpx;
    padding: 24rpx;
    text-align: center;

    .habit-label {
      display: block;
      font-size: 24rpx;
      color: #999;
      margin-bottom: 12rpx;
    }

    .habit-value {
      display: block;
      font-size: 28rpx;
      font-weight: 600;
      color: #333;
    }
  }
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 999;

  .loading-spinner {
    width: 60rpx;
    height: 60rpx;
    border: 4rpx solid #fff;
    border-top-color: #667eea;
    border-radius: 50%;
    animation: spin 1s linear infinite;
  }

  .loading-text {
    margin-top: 24rpx;
    font-size: 28rpx;
    color: #fff;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// ==================== 导出菜单样式 ====================
.export-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;

  .export-menu {
    background: #fff;
    border-radius: 16rpx;
    padding: 32rpx;
    width: 480rpx;
    box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);

    .export-menu-title {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
      text-align: center;
      margin-bottom: 32rpx;
    }

    .export-menu-item {
      display: flex;
      align-items: center;
      padding: 24rpx;
      background: #f8f9fa;
      border-radius: 12rpx;
      margin-bottom: 16rpx;
      transition: all 0.3s ease;

      &:last-child {
        margin-bottom: 0;
      }

      &:active {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        transform: scale(0.98);

        .export-menu-icon,
        .export-menu-text {
          color: #fff;
        }
      }

      .export-menu-icon {
        font-size: 48rpx;
        margin-right: 24rpx;
      }

      .export-menu-text {
        flex: 1;
        font-size: 28rpx;
        color: #333;
        font-weight: 500;
      }
    }
  }
}
</style>
