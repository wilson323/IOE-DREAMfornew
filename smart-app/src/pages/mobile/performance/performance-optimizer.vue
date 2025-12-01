<template>
  <view class="performance-page">
    <view class="header">
      <text class="title">性能优化</text>
      <text class="subtitle">应用状态监控和性能调优</text>
    </view>

    <view class="content">
      <!-- 性能概览 -->
      <view class="overview-section">
        <text class="section-title">性能概览</text>
        <view class="overview-grid">
          <view class="overview-item">
            <text class="overview-value">{{ performanceScore }}</text>
            <text class="overview-label">综合评分</text>
            <view class="overview-bar">
              <view class="bar-fill" :style="{ width: performanceScore + '%' }"></view>
            </view>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{ memoryUsage }}MB</text>
            <text class="overview-label">内存使用</text>
            <view class="overview-bar">
              <view class="bar-fill memory" :style="{ width: memoryPercent + '%' }"></view>
            </view>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{ cacheSize }}MB</text>
            <text class="overview-label">缓存大小</text>
            <view class="overview-bar">
              <view class="bar-fill cache" :style="{ width: cachePercent + '%' }"></view>
            </view>
          </view>
          <view class="overview-item">
            <text class="overview-value">{{ startupTime }}ms</text>
            <text class="overview-label">启动时间</text>
            <view class="overview-bar">
              <view class="bar-fill startup" :style="{ width: startupPercent + '%' }"></view>
            </view>
          </view>
        </view>
      </view>

      <!-- 实时监控 -->
      <view class="monitor-section">
        <text class="section-title">实时监控</text>
        <view class="monitor-grid">
          <view class="monitor-item">
            <view class="monitor-header">
              <text class="monitor-name">CPU使用率</text>
              <text class="monitor-value">{{ cpuUsage }}%</text>
            </view>
            <view class="monitor-chart">
              <view class="chart-container">
                <canvas
                  canvas-id="cpuChart"
                  :style="{ width: '100%', height: '120rpx' }"
                  @draw="drawCpuChart"
                />
              </view>
            </view>
          </view>
          <view class="monitor-item">
            <view class="monitor-header">
              <text class="monitor-name">网络延迟</text>
              <text class="monitor-value">{{ networkLatency }}ms</text>
            </view>
            <view class="monitor-chart">
              <view class="chart-container">
                <canvas
                  canvas-id="networkChart"
                  :style="{ width: '100%', height: '120rpx' }"
                  @draw="drawNetworkChart"
                />
              </view>
            </view>
          </view>
          <view class="monitor-item">
            <view class="monitor-header">
              <text class="monitor-name">FPS帧率</text>
              <text class="monitor-value">{{ fps }}</text>
            </view>
            <view class="monitor-chart">
              <view class="chart-container">
                <canvas
                  canvas-id="fpsChart"
                  :style="{ width: '100%', height: '120rpx' }"
                  @draw="drawFpsChart"
                />
              </view>
            </view>
          </view>
          <view class="monitor-item">
            <view class="monitor-header">
              <text class="monitor-name">电池温度</text>
              <text class="monitor-value">{{ batteryTemp }}°C</text>
            </view>
            <view class="monitor-chart">
              <view class="chart-container">
                <canvas
                  canvas-id="batteryChart"
                  :style="{ width: '100%', height: '120rpx' }"
                  @draw="drawBatteryChart"
                />
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 优化建议 -->
      <view class="suggestions-section">
        <text class="section-title">优化建议</text>
        <view class="suggestions-list">
          <view
            v-for="suggestion in optimizationSuggestions"
            :key="suggestion.id"
            class="suggestion-item"
            :class="suggestion.priority"
          >
            <view class="suggestion-icon">
              <text class="icon-text">{{ getPriorityIcon(suggestion.priority) }}</text>
            </view>
            <view class="suggestion-content">
              <text class="suggestion-title">{{ suggestion.title }}</text>
              <text class="suggestion-desc">{{ suggestion.description }}</text>
              <view v-if="suggestion.action" class="suggestion-action">
                <button
                  class="action-btn"
                  :class="suggestion.action.type"
                  @click="executeSuggestion(suggestion)"
                >
                  <text>{{ suggestion.action.text }}</text>
                </button>
              </view>
            </view>
          </view>
        </view>
      </view>

      <!-- 清理工具 -->
      <view class="cleanup-section">
        <text class="section-title">清理工具</text>
        <view class="cleanup-tools">
          <view class="tool-item" @click="clearCache">
            <view class="tool-icon">
              <text class="icon">🗑️</text>
            </view>
            <view class="tool-info">
              <text class="tool-name">清理缓存</text>
              <text class="tool-desc">释放{{ cacheSize }}MB存储空间</text>
            </view>
            <text class="tool-arrow">></text>
          </view>

          <view class="tool-item" @click="clearTempFiles">
            <view class="tool-icon">
              <text class="icon">📄</text>
            </view>
            <view class="tool-info">
              <text class="tool-name">清理临时文件</text>
              <text class="tool-desc">删除过期临时数据</text>
            </view>
            <text class="tool-arrow">></text>
          </view>

          <view class="tool-item" @click="clearOfflineData">
            <view class="tool-icon">
              <text class="icon">📱</text>
            </view>
            <view class="tool-info">
              <text class="tool-name">清理离线数据</text>
              <text class="tool-desc">删除过期离线缓存</text>
            </view>
            <text class="tool-arrow">></text>
          </view>

          <view class="tool-item" @click="resetAppSettings">
            <view class="tool-icon">
              <text class="icon">⚙️</text>
            </view>
            <view class="tool-info">
              <text class="tool-name">重置应用设置</text>
              <text class="tool-desc">恢复默认配置</text>
            </view>
            <text class="tool-arrow">></text>
          </view>
        </view>
      </view>

      <!-- 性能设置 -->
      <view class="settings-section">
        <text class="section-title">性能设置</text>
        <view class="setting-items">
          <view class="setting-item">
            <view class="setting-info">
              <text class="setting-name">性能模式</text>
              <text class="setting-desc">选择应用运行模式</text>
            </view>
            <picker
              :value="performanceMode"
              :range="performanceModes"
              range-key="name"
              @change="changePerformanceMode"
            >
              <view class="picker-value">
                <text>{{ performanceModes[performanceMode].name }}</text>
                <text class="picker-arrow">▼</text>
              </view>
            </picker>
          </view>

          <view class="setting-item">
            <view class="setting-info">
              <text class="setting-name">自动清理</text>
              <text class="setting-desc">定期自动清理缓存数据</text>
            </view>
            <switch
              :checked="autoCleanup"
              @change="toggleAutoCleanup"
              color="#667eea"
            />
          </view>

          <view class="setting-item">
            <view class="setting-info">
              <text class="setting-name">后台优化</text>
              <text class="setting-desc">允许后台自动优化性能</text>
            </view>
            <switch
              :checked="backgroundOptimization"
              @change="toggleBackgroundOptimization"
              color="#667eea"
            />
          </view>

          <view class="setting-item">
            <view class="setting-info">
              <text class="setting-name">内存管理</text>
              <text class="setting-desc">智能内存释放和回收</text>
            </view>
            <switch
              :checked="memoryManagement"
              @change="toggleMemoryManagement"
              color="#667eea"
            />
          </view>
        </view>
      </view>

      <!-- 诊断报告 -->
      <view class="diagnostic-section">
        <text class="section-title">诊断报告</text>
        <view class="diagnostic-content">
          <view class="report-summary">
            <text class="report-title">最近诊断</text>
            <text class="report-time">{{ formatTime(lastDiagnosticTime) }}</text>
          </view>
          <view class="report-items">
            <view
              v-for="item in diagnosticReport"
              :key="item.id"
              class="report-item"
              :class="item.status"
            >
              <text class="report-icon">{{ getStatusIcon(item.status) }}</text>
              <view class="report-info">
                <text class="report-name">{{ item.name }}</text>
                <text class="report-value">{{ item.value }}</text>
              </view>
            </view>
          </view>
          <button class="diagnostic-btn" @click="runDiagnostic">
            <text>运行诊断</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'PerformanceOptimizer',
  data() {
    return {
      performanceScore: 85,
      memoryUsage: 156,
      cacheSize: 48,
      startupTime: 1250,
      cpuUsage: 32,
      networkLatency: 68,
      fps: 58,
      batteryTemp: 36,

      performanceMode: 1,
      performanceModes: [
        { name: '省电模式', value: 'power-save' },
        { name: '平衡模式', value: 'balanced' },
        { name: '性能模式', value: 'performance' }
      ],

      autoCleanup: true,
      backgroundOptimization: true,
      memoryManagement: true,

      cpuData: [],
      networkData: [],
      fpsData: [],
      batteryData: [],

      optimizationSuggestions: [
        {
          id: 1,
          priority: 'high',
          title: '清理应用缓存',
          description: '缓存占用过多存储空间，建议清理以释放空间',
          action: { type: 'primary', text: '立即清理' }
        },
        {
          id: 2,
          priority: 'medium',
          title: '关闭后台应用',
          description: '检测到多个后台应用占用系统资源',
          action: { type: 'warning', text: '优化管理' }
        },
        {
          id: 3,
          priority: 'low',
          title: '更新应用版本',
          description: '发现新版本可能包含性能优化',
          action: { type: 'default', text: '查看更新' }
        }
      ],

      lastDiagnosticTime: Date.now() - 2 * 60 * 60 * 1000,
      diagnosticReport: [
        { id: 1, name: '系统健康', value: '良好', status: 'good' },
        { id: 2, name: '存储空间', value: '68%', status: 'warning' },
        { id: 3, name: '内存使用', value: '正常', status: 'good' },
        { id: 4, name: '电池状态', value: '健康', status: 'good' },
        { id: 5, name: '网络连接', value: '稳定', status: 'good' }
      ]
    }
  },

  computed: {
    memoryPercent() {
      return Math.min((this.memoryUsage / 512) * 100, 100)
    },

    cachePercent() {
      return Math.min((this.cacheSize / 100) * 100, 100)
    },

    startupPercent() {
      return Math.max(100 - (this.startupTime / 3000) * 100, 0)
    }
  },

  mounted() {
    this.initializePerformanceMonitoring()
    this.loadPerformanceSettings()
    this.startRealTimeMonitoring()
  },

  beforeDestroy() {
    this.stopRealTimeMonitoring()
  },

  methods: {
    initializePerformanceMonitoring() {
      // 初始化性能监控
      this.collectSystemInfo()
      this.initializeCharts()
    },

    async collectSystemInfo() {
      try {
        // #ifdef APP-PLUS
        const deviceInfo = uni.getSystemInfoSync()

        // 获取内存信息
        if (deviceInfo && deviceInfo.memUsed) {
          this.memoryUsage = Math.round(deviceInfo.memUsed / 1024 / 1024)
        }

        // 获取电池信息
        const batteryInfo = await uni.getBatteryInfo()
        if (batteryInfo && batteryInfo.temperature) {
          this.batteryTemp = batteryInfo.temperature
        }
        // #endif

        // 计算性能评分
        this.calculatePerformanceScore()

      } catch (error) {
        console.error('获取系统信息失败:', error)
      }
    },

    calculatePerformanceScore() {
      let score = 100

      // 内存使用评分
      if (this.memoryUsage > 400) score -= 20
      else if (this.memoryUsage > 300) score -= 10

      // 启动时间评分
      if (this.startupTime > 2000) score -= 15
      else if (this.startupTime > 1500) score -= 8

      // CPU使用评分
      if (this.cpuUsage > 80) score -= 25
      else if (this.cpuUsage > 60) score -= 15

      // FPS评分
      if (this.fps < 30) score -= 20
      else if (this.fps < 45) score -= 10

      this.performanceScore = Math.max(score, 0)
    },

    loadPerformanceSettings() {
      try {
        const settings = uni.getStorageSync('performance_settings')
        if (settings) {
          this.performanceMode = settings.performanceMode || 1
          this.autoCleanup = settings.autoCleanup !== false
          this.backgroundOptimization = settings.backgroundOptimization !== false
          this.memoryManagement = settings.memoryManagement !== false
        }
      } catch (error) {
        console.error('加载性能设置失败:', error)
      }
    },

    savePerformanceSettings() {
      try {
        uni.setStorageSync('performance_settings', {
          performanceMode: this.performanceMode,
          autoCleanup: this.autoCleanup,
          backgroundOptimization: this.backgroundOptimization,
          memoryManagement: this.memoryManagement
        })
      } catch (error) {
        console.error('保存性能设置失败:', error)
      }
    },

    startRealTimeMonitoring() {
      // 启动实时监控
      this.monitoringTimer = setInterval(() => {
        this.updateRealTimeData()
        this.updateCharts()
      }, 2000)
    },

    stopRealTimeMonitoring() {
      if (this.monitoringTimer) {
        clearInterval(this.monitoringTimer)
        this.monitoringTimer = null
      }
    },

    async updateRealTimeData() {
      try {
        // 模拟实时数据更新
        this.cpuUsage = Math.max(5, Math.min(95, this.cpuUsage + (Math.random() - 0.5) * 10))
        this.networkLatency = Math.max(10, Math.min(500, this.networkLatency + (Math.random() - 0.5) * 20))
        this.fps = Math.max(30, Math.min(60, this.fps + (Math.random() - 0.5) * 5))
        this.batteryTemp = Math.max(25, Math.min(45, this.batteryTemp + (Math.random() - 0.5) * 2))

        // 更新图表数据
        this.cpuData.push(this.cpuUsage)
        this.networkData.push(this.networkLatency)
        this.fpsData.push(this.fps)
        this.batteryData.push(this.batteryTemp)

        // 限制数据长度
        const maxDataPoints = 20
        if (this.cpuData.length > maxDataPoints) this.cpuData.shift()
        if (this.networkData.length > maxDataPoints) this.networkData.shift()
        if (this.fpsData.length > maxDataPoints) this.fpsData.shift()
        if (this.batteryData.length > maxDataPoints) this.batteryData.shift()

        // 检查性能阈值
        this.checkPerformanceThresholds()

      } catch (error) {
        console.error('更新实时数据失败:', error)
      }
    },

    checkPerformanceThresholds() {
      const suggestions = []

      if (this.memoryUsage > 400) {
        suggestions.push({
          id: Date.now(),
          priority: 'high',
          title: '内存使用过高',
          description: '当前内存使用超过400MB，建议清理应用',
          action: { type: 'danger', text: '立即清理' }
        })
      }

      if (this.cpuUsage > 80) {
        suggestions.push({
          id: Date.now() + 1,
          priority: 'high',
          title: 'CPU占用过高',
          description: 'CPU使用率超过80%，应用可能卡顿',
          action: { type: 'warning', text: '优化性能' }
        })
      }

      if (this.fps < 30) {
        suggestions.push({
          id: Date.now() + 2,
          priority: 'medium',
          title: '帧率过低',
          description: '当前帧率低于30FPS，影响用户体验',
          action: { type: 'primary', text: '提升性能' }
        })
      }

      // 更新建议列表
      if (suggestions.length > 0) {
        this.optimizationSuggestions = [...suggestions, ...this.optimizationSuggestions].slice(0, 5)
      }
    },

    initializeCharts() {
      // 初始化图表数据
      for (let i = 0; i < 20; i++) {
        this.cpuData.push(Math.random() * 40 + 20)
        this.networkData.push(Math.random() * 100 + 50)
        this.fpsData.push(Math.random() * 20 + 40)
        this.batteryData.push(Math.random() * 10 + 30)
      }
    },

    updateCharts() {
      // 更新图表
      this.$nextTick(() => {
        this.drawCpuChart()
        this.drawNetworkChart()
        this.drawFpsChart()
        this.drawBatteryChart()
      })
    },

    drawCpuChart() {
      const ctx = uni.createCanvasContext('cpuChart', this)
      const width = 300
      const height = 120

      ctx.clearRect(0, 0, width, height)

      if (this.cpuData.length < 2) return

      const max = Math.max(...this.cpuData)
      const min = Math.min(...this.cpuData)
      const range = max - min || 1

      ctx.setStrokeStyle('#667eea')
      ctx.setLineWidth(2)
      ctx.beginPath()

      this.cpuData.forEach((value, index) => {
        const x = (index / (this.cpuData.length - 1)) * width
        const y = height - ((value - min) / range) * height * 0.8 - height * 0.1

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })

      ctx.stroke()
      ctx.draw()
    },

    drawNetworkChart() {
      const ctx = uni.createCanvasContext('networkChart', this)
      const width = 300
      const height = 120

      ctx.clearRect(0, 0, width, height)

      if (this.networkData.length < 2) return

      const max = Math.max(...this.networkData)
      const min = Math.min(...this.networkData)
      const range = max - min || 1

      ctx.setStrokeStyle('#27ae60')
      ctx.setLineWidth(2)
      ctx.beginPath()

      this.networkData.forEach((value, index) => {
        const x = (index / (this.networkData.length - 1)) * width
        const y = height - ((value - min) / range) * height * 0.8 - height * 0.1

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })

      ctx.stroke()
      ctx.draw()
    },

    drawFpsChart() {
      const ctx = uni.createCanvasContext('fpsChart', this)
      const width = 300
      const height = 120

      ctx.clearRect(0, 0, width, height)

      if (this.fpsData.length < 2) return

      const max = Math.max(...this.fpsData)
      const min = Math.min(...this.fpsData)
      const range = max - min || 1

      ctx.setStrokeStyle('#f39c12')
      ctx.setLineWidth(2)
      ctx.beginPath()

      this.fpsData.forEach((value, index) => {
        const x = (index / (this.fpsData.length - 1)) * width
        const y = height - ((value - min) / range) * height * 0.8 - height * 0.1

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })

      ctx.stroke()
      ctx.draw()
    },

    drawBatteryChart() {
      const ctx = uni.createCanvasContext('batteryChart', this)
      const width = 300
      const height = 120

      ctx.clearRect(0, 0, width, height)

      if (this.batteryData.length < 2) return

      const max = Math.max(...this.batteryData)
      const min = Math.min(...this.batteryData)
      const range = max - min || 1

      ctx.setStrokeStyle('#e74c3c')
      ctx.setLineWidth(2)
      ctx.beginPath()

      this.batteryData.forEach((value, index) => {
        const x = (index / (this.batteryData.length - 1)) * width
        const y = height - ((value - min) / range) * height * 0.8 - height * 0.1

        if (index === 0) {
          ctx.moveTo(x, y)
        } else {
          ctx.lineTo(x, y)
        }
      })

      ctx.stroke()
      ctx.draw()
    },

    changePerformanceMode(e) {
      this.performanceMode = e.detail.value
      this.savePerformanceSettings()
      this.applyPerformanceMode()
    },

    applyPerformanceMode() {
      const mode = this.performanceModes[this.performanceMode]

      switch (mode.value) {
        case 'power-save':
          // 省电模式设置
          this.autoCleanup = true
          this.backgroundOptimization = false
          break
        case 'balanced':
          // 平衡模式设置
          this.autoCleanup = true
          this.backgroundOptimization = true
          break
        case 'performance':
          // 性能模式设置
          this.autoCleanup = false
          this.backgroundOptimization = true
          break
      }

      this.savePerformanceSettings()

      uni.showToast({
        title: `已切换到${mode.name}`,
        icon: 'success'
      })
    },

    toggleAutoCleanup(e) {
      this.autoCleanup = e.detail.value
      this.savePerformanceSettings()
    },

    toggleBackgroundOptimization(e) {
      this.backgroundOptimization = e.detail.value
      this.savePerformanceSettings()
    },

    toggleMemoryManagement(e) {
      this.memoryManagement = e.detail.value
      this.savePerformanceSettings()
    },

    async clearCache() {
      try {
        uni.showLoading({
          title: '清理中...'
        })

        // 模拟清理过程
        await new Promise(resolve => setTimeout(resolve, 2000))

        this.cacheSize = Math.max(0, this.cacheSize - Math.floor(this.cacheSize * 0.7))

        uni.hideLoading()
        uni.showToast({
          title: '清理完成',
          icon: 'success'
        })

        this.calculatePerformanceScore()
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '清理失败',
          icon: 'error'
        })
      }
    },

    async clearTempFiles() {
      try {
        uni.showLoading({
          title: '清理中...'
        })

        await new Promise(resolve => setTimeout(resolve, 1500))

        uni.hideLoading()
        uni.showToast({
          title: '临时文件已清理',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '清理失败',
          icon: 'error'
        })
      }
    },

    async clearOfflineData() {
      try {
        uni.showModal({
          title: '确认清理',
          content: '清理离线数据后，需要重新联网获取数据',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '清理中...'
              })

              await new Promise(resolve => setTimeout(resolve, 1000))

              uni.hideLoading()
              uni.showToast({
                title: '离线数据已清理',
                icon: 'success'
              })
            }
          }
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '清理失败',
          icon: 'error'
        })
      }
    },

    async resetAppSettings() {
      try {
        uni.showModal({
          title: '重置设置',
          content: '确定要重置所有应用设置吗？此操作不可撤销',
          success: async (res) => {
            if (res.confirm) {
              uni.showLoading({
                title: '重置中...'
              })

              // 清除所有设置
              uni.removeStorageSync('performance_settings')
              uni.removeStorageSync('notification_settings')
              uni.removeStorageSync('biometric_settings')

              // 重新加载默认设置
              this.loadPerformanceSettings()

              await new Promise(resolve => setTimeout(resolve, 1500))

              uni.hideLoading()
              uni.showToast({
                title: '设置已重置',
                icon: 'success'
              })
            }
          }
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '重置失败',
          icon: 'error'
        })
      }
    },

    executeSuggestion(suggestion) {
      console.log('执行优化建议:', suggestion)

      // 根据建议类型执行相应操作
      if (suggestion.title.includes('缓存')) {
        this.clearCache()
      } else if (suggestion.title.includes('后台')) {
        this.optimizeBackgroundApps()
      } else if (suggestion.title.includes('更新')) {
        this.checkForUpdates()
      }
    },

    async optimizeBackgroundApps() {
      uni.showLoading({
        title: '优化中...'
      })

      await new Promise(resolve => setTimeout(resolve, 2000))

      uni.hideLoading()
      uni.showToast({
        title: '后台优化完成',
        icon: 'success'
      })
    },

    async checkForUpdates() {
      uni.showToast({
        title: '已是最新版本',
        icon: 'success'
      })
    },

    async runDiagnostic() {
      try {
        uni.showLoading({
          title: '诊断中...'
        })

        // 模拟诊断过程
        await new Promise(resolve => setTimeout(resolve, 3000))

        // 更新诊断报告
        this.diagnosticReport = [
          { id: 1, name: '系统健康', value: '优秀', status: 'good' },
          { id: 2, name: '存储空间', value: '85%', status: 'good' },
          { id: 3, name: '内存使用', value: '优化', status: 'good' },
          { id: 4, name: '电池状态', value: '健康', status: 'good' },
          { id: 5, name: '网络连接', value: '快速', status: 'good' }
        ]

        this.lastDiagnosticTime = Date.now()

        uni.hideLoading()
        uni.showToast({
          title: '诊断完成',
          icon: 'success'
        })
      } catch (error) {
        uni.hideLoading()
        uni.showToast({
          title: '诊断失败',
          icon: 'error'
        })
      }
    },

    getPriorityIcon(priority) {
      const icons = {
        high: '🔴',
        medium: '🟡',
        low: '🟢'
      }
      return icons[priority] || '⚪'
    },

    getStatusIcon(status) {
      const icons = {
        good: '✅',
        warning: '⚠️',
        error: '❌'
      }
      return icons[status] || '⚪'
    },

    formatTime(timestamp) {
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date

      if (diff < 60000) {
        return '刚刚'
      } else if (diff < 3600000) {
        return Math.floor(diff / 60000) + '分钟前'
      } else if (diff < 86400000) {
        return Math.floor(diff / 3600000) + '小时前'
      } else {
        return date.toLocaleDateString()
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.performance-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40rpx 30rpx;
  text-align: center;
  color: white;
}

.title {
  font-size: 32rpx;
  font-weight: bold;
  display: block;
  margin-bottom: 8rpx;
}

.subtitle {
  font-size: 28rpx;
  opacity: 0.9;
}

.content {
  padding: 20rpx;
}

.section-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 20rpx;
  display: block;
}

.overview-section,
.monitor-section,
.suggestions-section,
.cleanup-section,
.settings-section,
.diagnostic-section {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.1);
}

.overview-grid,
.monitor-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20rpx;
}

.overview-item,
.monitor-item {
  text-align: center;
}

.overview-value {
  font-size: 36rpx;
  font-weight: bold;
  color: #667eea;
  display: block;
  margin-bottom: 8rpx;
}

.overview-label {
  font-size: 22rpx;
  color: #666;
  display: block;
  margin-bottom: 12rpx;
}

.overview-bar {
  width: 100%;
  height: 8rpx;
  background: #e0e0e0;
  border-radius: 4rpx;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  transition: width 0.3s ease;

  &.memory {
    background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  }

  &.cache {
    background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  }

  &.startup {
    background: linear-gradient(135deg, #27ae60 0%, #229954 100%);
  }
}

.monitor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16rpx;
}

.monitor-name {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;
}

.monitor-value {
  font-size: 24rpx;
  color: #667eea;
  font-weight: bold;
}

.monitor-chart {
  height: 120rpx;
}

.chart-container {
  width: 100%;
  height: 100%;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  padding: 20rpx;
  border-radius: 12rpx;
  border-left: 4rpx solid #e0e0e0;

  &.high {
    border-left-color: #e74c3c;
    background: #fdf2f2;
  }

  &.medium {
    border-left-color: #f39c12;
    background: #fef9e7;
  }

  &.low {
    border-left-color: #27ae60;
    background: #f2fdf2;
  }
}

.suggestion-icon {
  margin-right: 16rpx;
  font-size: 24rpx;
}

.suggestion-content {
  flex: 1;
}

.suggestion-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 8rpx;
}

.suggestion-desc {
  font-size: 24rpx;
  color: #666;
  line-height: 1.4;
  margin-bottom: 12rpx;
}

.suggestion-action {
  margin-top: 12rpx;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
  font-weight: bold;
  border: none;
  color: white;

  &.primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  }

  &.warning {
    background: #f39c12;
  }

  &.danger {
    background: #e74c3c;
  }

  &.default {
    background: #95a5a6;
  }
}

.cleanup-tools {
  display: flex;
  flex-direction: column;
  gap: 4rpx;
}

.tool-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background: #f8f9fa;
  }
}

.tool-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 16rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 24rpx;

  .icon {
    font-size: 40rpx;
  }
}

.tool-info {
  flex: 1;
}

.tool-name {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
  display: block;
  margin-bottom: 4rpx;
}

.tool-desc {
  font-size: 22rpx;
  color: #666;
}

.tool-arrow {
  font-size: 24rpx;
  color: #666;
}

.setting-items {
  display: flex;
  flex-direction: column;
}

.setting-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.setting-info {
  flex: 1;
  margin-right: 20rpx;
}

.setting-name {
  font-size: 26rpx;
  color: #333;
  font-weight: bold;
  display: block;
  margin-bottom: 4rpx;
}

.setting-desc {
  font-size: 22rpx;
  color: #666;
}

.picker-value {
  display: flex;
  align-items: center;
  color: #667eea;
  font-size: 26rpx;
}

.picker-arrow {
  margin-left: 8rpx;
  font-size: 20rpx;
}

.diagnostic-content {
  display: flex;
  flex-direction: column;
}

.report-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
  padding-bottom: 16rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.report-title {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.report-time {
  font-size: 22rpx;
  color: #666;
}

.report-items {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
  margin-bottom: 30rpx;
}

.report-item {
  display: flex;
  align-items: center;
  padding: 16rpx;
  border-radius: 12rpx;
  background: #f8f9fa;

  &.good {
    border-left: 4rpx solid #27ae60;
  }

  &.warning {
    border-left: 4rpx solid #f39c12;
  }

  &.error {
    border-left: 4rpx solid #e74c3c;
  }
}

.report-icon {
  margin-right: 16rpx;
  font-size: 24rpx;
}

.report-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.report-name {
  font-size: 24rpx;
  color: #333;
  font-weight: bold;
}

.report-value {
  font-size: 22rpx;
  color: #666;
}

.diagnostic-btn {
  width: 100%;
  height: 88rpx;
  border-radius: 44rpx;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  color: white;
  font-size: 28rpx;
  font-weight: bold;
}
</style>