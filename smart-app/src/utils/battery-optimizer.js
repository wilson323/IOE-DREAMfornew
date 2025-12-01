/**
 * 移动端电池优化管理器
 *
 * 提供电池状态监听、功耗优化、智能调度等功能
 * 支持Android和iOS平台
 *
 * @author SmartAdmin
 * @since 2025-11-14
 */

import { ref, reactive } from 'vue'

/**
 * 电池状态枚举
 */
export const BatteryStatus = {
  UNKNOWN: 'unknown',
  CHARGING: 'charging',
  DISCHARGING: 'discharging',
  FULL: 'full',
  NOT_CHARGING: 'not_charging'
}

/**
 * 性能模式枚举
 */
export const PerformanceMode = {
  HIGH_PERFORMANCE: 'high_performance',
  BALANCED: 'balanced',
  POWER_SAVING: 'power_saving',
  ULTRA_POWER_SAVING: 'ultra_power_saving'
}

/**
 * 电池优化管理器类
 */
export class BatteryOptimizer {
  constructor(options = {}) {
    // 默认配置
    this.config = {
      enableAutoOptimization: true,
      enableBatteryMonitoring: true,
      monitoringInterval: 30000, // 30秒
      lowBatteryThreshold: 20,    // 低电量阈值
      criticalBatteryThreshold: 5, // 严重低电量阈值
      enableThermalMonitoring: true,
      enableBackgroundOptimization: true,
      maxBackgroundTasks: 3,
      taskTimeoutMs: 30000,
      ...options
    }

    // 电池状态
    this.batteryStatus = reactive({
      level: 100,
      isCharging: false,
      chargingTime: -1,
      dischargingTime: -1,
      status: BatteryStatus.UNKNOWN
    })

    // 性能模式
    this.performanceMode = ref(PerformanceMode.BALANCED)

    // 系统信息
    this.systemInfo = reactive({
      platform: '',
      version: '',
      deviceModel: '',
      thermalState: 'unknown'
    })

    // 任务管理
    this.taskQueue = []
    this.activeTasks = new Set()
    this.backgroundTasks = []
    this.optimizedTasks = new Map()

    // 统计信息
    this.statistics = reactive({
      totalOptimizations: 0,
      energySaved: 0,
      backgroundTasksOptimized: 0,
      thermalThrottlingEvents: 0,
      batteryLifeExtended: 0
    })

    // 监听定时器
    this.monitoringTimer = null
    this.optimizationTimer = null

    // 事件监听器
    this.eventListeners = new Map()

    // 初始化
    this.init()
  }

  /**
   * 初始化电池优化器
   */
  init() {
    console.log('初始化电池优化管理器')

    this.getSystemInfo()
    this.startBatteryMonitoring()
    this.setupPerformanceMode()
    this.startOptimizationScheduler()

    console.log('电池优化管理器初始化完成')
  }

  /**
   * 获取系统信息
   */
  getSystemInfo() {
    try {
      const systemInfo = uni.getSystemInfoSync()

      this.systemInfo.platform = systemInfo.platform
      this.systemInfo.version = systemInfo.system || systemInfo.version
      this.systemInfo.deviceModel = systemInfo.model

      console.log('系统信息:', this.systemInfo)

    } catch (error) {
      console.error('获取系统信息失败:', error)
    }
  }

  /**
   * 开始电池监控
   */
  startBatteryMonitoring() {
    if (!this.config.enableBatteryMonitoring) return

    // 初始获取电池状态
    this.updateBatteryStatus()

    // 设置定时监控
    this.monitoringTimer = setInterval(() => {
      this.updateBatteryStatus()
    }, this.config.monitoringInterval)

    // #ifdef APP-PLUS
    this.setupNativeBatteryMonitoring()
    // #endif

    console.log('电池监控已启动')
  }

  /**
   * 更新电池状态
   */
  updateBatteryStatus() {
    try {
      // #ifdef APP-PLUS
      this.updateNativeBatteryStatus()
      // #endif

      // #ifdef H5
      this.updateWebBatteryStatus()
      // #endif

      // #ifndef APP-PLUS || H5
      // 模拟电池状态用于测试
      this.simulateBatteryStatus()
      // #endif

      // 触发电池状态变化事件
      this.emit('battery-status-changed', this.getBatteryInfo())

      // 根据电池状态自动优化
      if (this.config.enableAutoOptimization) {
        this.autoOptimize()
      }

    } catch (error) {
      console.error('更新电池状态失败:', error)
    }
  }

  /**
   * 更新原生电池状态
   */
  updateNativeBatteryStatus() {
    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity()

      // Android 电池管理器
      if (this.systemInfo.platform === 'android') {
        const batteryManager = main.getSystemService(
          plus.android.importClass("android.content.Context").BATTERY_SERVICE
        )

        if (batteryManager) {
          // 获取电池电量
          this.batteryStatus.level = batteryManager.getIntProperty(
            plus.android.importClass("android.os.BatteryManager").BATTERY_PROPERTY_CAPACITY
          )

          // 获取充电状态
          const chargingStatus = batteryManager.getIntProperty(
            plus.android.importClass("android.os.BatteryManager").BATTERY_PROPERTY_STATUS
          )

          const BatteryManager = plus.android.importClass("android.os.BatteryManager")
          this.batteryStatus.isCharging = chargingStatus === BatteryManager.BATTERY_STATUS_CHARGING ||
                                        chargingStatus === BatteryManager.BATTERY_STATUS_FULL

          if (this.batteryStatus.isCharging) {
            this.batteryStatus.status = BatteryStatus.CHARGING
          } else if (chargingStatus === BatteryManager.BATTERY_STATUS_FULL) {
            this.batteryStatus.status = BatteryStatus.FULL
          } else if (chargingStatus === BatteryManager.BATTERY_STATUS_DISCHARGING) {
            this.batteryStatus.status = BatteryStatus.DISCHARGING
          } else {
            this.batteryStatus.status = BatteryStatus.NOT_CHARGING
          }
        }
      }

      // iOS 电池管理器
      if (this.systemInfo.platform === 'ios') {
        const device = plus.ios.import("UIDevice.currentDevice")
        device.setBatteryMonitoringEnabled(true)

        this.batteryStatus.level = Math.floor(device.batteryLevel() * 100)
        this.batteryStatus.isCharging = device.batteryState() === 2 // UIDeviceBatteryStateCharging

        this.batteryStatus.status = this.batteryStatus.isCharging
          ? BatteryStatus.CHARGING
          : BatteryStatus.DISCHARGING
      }

    } catch (error) {
      console.warn('获取原生电池状态失败:', error)
    }
    // #endif
  }

  /**
   * 更新Web电池状态
   */
  updateWebBatteryStatus() {
    // #ifdef H5
    try {
      if ('getBattery' in navigator) {
        navigator.getBattery().then(battery => {
          this.batteryStatus.level = Math.floor(battery.level * 100)
          this.batteryStatus.isCharging = battery.charging
          this.batteryStatus.chargingTime = battery.chargingTime
          this.batteryStatus.dischargingTime = battery.dischargingTime

          this.batteryStatus.status = battery.charging
            ? BatteryStatus.CHARGING
            : BatteryStatus.DISCHARGING

          // 监听电池状态变化
          battery.addEventListener('levelchange', () => {
            this.batteryStatus.level = Math.floor(battery.level * 100)
            this.emit('battery-level-changed', this.batteryStatus.level)
          })

          battery.addEventListener('chargingchange', () => {
            this.batteryStatus.isCharging = battery.charging
            this.batteryStatus.status = battery.charging
              ? BatteryStatus.CHARGING
              : BatteryStatus.DISCHARGING
            this.emit('charging-status-changed', this.batteryStatus.isCharging)
          })

        }).catch(error => {
          console.warn('Web Battery API不可用:', error)
          this.simulateBatteryStatus()
        })
      } else {
        this.simulateBatteryStatus()
      }
    } catch (error) {
      console.error('Web电池状态更新失败:', error)
      this.simulateBatteryStatus()
    }
    // #endif
  }

  /**
   * 模拟电池状态（用于测试）
   */
  simulateBatteryStatus() {
    // 模拟电池电量和充电状态
    this.batteryStatus.level = Math.max(10, Math.min(100, this.batteryStatus.level + (Math.random() - 0.5) * 5))
    this.batteryStatus.isCharging = Math.random() > 0.8 // 20%概率充电中
    this.batteryStatus.status = this.batteryStatus.isCharging
      ? BatteryStatus.CHARGING
      : BatteryStatus.DISCHARGING
  }

  /**
   * 设置原生电池监控
   */
  setupNativeBatteryMonitoring() {
    // #ifdef APP-PLUS
    try {
      // Android广播接收器
      if (this.systemInfo.platform === 'android') {
        const main = plus.android.runtimeMainActivity()
        const IntentFilter = plus.android.importClass("android.content.IntentFilter")
        const BatteryManager = plus.android.importClass("android.os.BatteryManager")

        const filter = new IntentFilter()
        filter.addAction(Intent.ACTION_BATTERY_CHANGED)

        // 这里可以注册广播接收器监听电池变化
        // 由于uni-app限制，我们主要使用定时器监控
      }
    } catch (error) {
      console.warn('设置原生电池监控失败:', error)
    }
    // #endif
  }

  /**
   * 设置性能模式
   */
  setupPerformanceMode() {
    this.updatePerformanceMode()
  }

  /**
   * 更新性能模式
   */
  updatePerformanceMode() {
    const { level, isCharging } = this.batteryStatus

    if (isCharging) {
      // 充电时使用高性能模式
      this.performanceMode.value = PerformanceMode.HIGH_PERFORMANCE
    } else if (level <= this.config.criticalBatteryThreshold) {
      // 严重低电量时使用超级省电模式
      this.performanceMode.value = PerformanceMode.ULTRA_POWER_SAVING
    } else if (level <= this.config.lowBatteryThreshold) {
      // 低电量时使用省电模式
      this.performanceMode.value = PerformanceMode.POWER_SAVING
    } else if (level <= 50) {
      // 50%以下时使用平衡模式
      this.performanceMode.value = PerformanceMode.BALANCED
    } else {
      // 高电量时使用高性能模式
      this.performanceMode.value = PerformanceMode.HIGH_PERFORMANCE
    }

    this.applyPerformanceMode()
    this.emit('performance-mode-changed', this.performanceMode.value)
  }

  /**
   * 应用性能模式
   */
  applyPerformanceMode() {
    const mode = this.performanceMode.value

    switch (mode) {
      case PerformanceMode.HIGH_PERFORMANCE:
        this.applyHighPerformanceMode()
        break
      case PerformanceMode.BALANCED:
        this.applyBalancedMode()
        break
      case PerformanceMode.POWER_SAVING:
        this.applyPowerSavingMode()
        break
      case PerformanceMode.ULTRA_POWER_SAVING:
        this.applyUltraPowerSavingMode()
        break
    }
  }

  /**
   * 应用高性能模式
   */
  applyHighPerformanceMode() {
    console.log('应用高性能模式')

    // 提高WebSocket心跳频率
    uni.$emit('set-heartbeat-interval', 15000)

    // 启用所有实时功能
    uni.$emit('enable-realtime-features', true)

    // 提高任务优先级
    this.updateTaskPriority('high')

    // 启用动画效果
    this.updateAnimationSettings(true)
  }

  /**
   * 应用平衡模式
   */
  applyBalancedMode() {
    console.log('应用平衡模式')

    // 标准心跳频率
    uni.$emit('set-heartbeat-interval', 30000)

    // 启用部分实时功能
    uni.$emit('enable-realtime-features', true)
    uni.$emit('reduce-background-activity', true)

    // 标准任务优先级
    this.updateTaskPriority('normal')

    // 启用部分动画效果
    this.updateAnimationSettings(true)
  }

  /**
   * 应用省电模式
   */
  applyPowerSavingMode() {
    console.log('应用省电模式')

    // 降低心跳频率
    uni.$emit('set-heartbeat-interval', 60000)

    // 限制实时功能
    uni.$emit('reduce-realtime-frequency', true)
    uni.$emit('disable-non-essential-features', true)

    // 降低任务优先级
    this.updateTaskPriority('low')

    // 禁用动画效果
    this.updateAnimationSettings(false)

    // 限制后台任务
    this.limitBackgroundTasks(2)
  }

  /**
   * 应用超级省电模式
   */
  applyUltraPowerSavingMode() {
    console.log('应用超级省电模式')

    // 大幅降低心跳频率
    uni.$emit('set-heartbeat-interval', 120000)

    // 仅保留核心实时功能
    uni.$emit('enable-essential-realtime-only', true)

    // 最低任务优先级
    this.updateTaskPriority('minimal')

    // 完全禁用动画
    this.updateAnimationSettings(false)

    // 严格限制后台任务
    this.limitBackgroundTasks(1)

    // 降低屏幕亮度（如果支持）
    this.reduceScreenBrightness()
  }

  /**
   * 自动优化
   */
  autoOptimize() {
    const { level, isCharging } = this.batteryStatus

    // 低电量警告
    if (!isCharging && level <= this.config.lowBatteryThreshold) {
      this.showLowBatteryWarning()
    }

    // 严重低电量警告
    if (!isCharging && level <= this.config.criticalBatteryThreshold) {
      this.showCriticalBatteryWarning()
      this.enterEmergencyPowerSaving()
    }

    // 应用性能模式
    this.updatePerformanceMode()

    // 更新统计
    this.statistics.totalOptimizations++
  }

  /**
   * 显示低电量警告
   */
  showLowBatteryWarning() {
    uni.showToast({
      title: `电量较低 (${this.batteryStatus.level}%)`,
      icon: 'none',
      duration: 3000
    })

    this.emit('low-battery-warning', this.batteryStatus.level)
  }

  /**
   * 显示严重低电量警告
   */
  showCriticalBatteryWarning() {
    uni.showModal({
      title: '电量严重不足',
      content: `当前电量仅剩 ${this.batteryStatus.level}%，建议立即充电或进入超级省电模式`,
      showCancel: true,
      confirmText: '超级省电',
      cancelText: '知道了',
      success: (res) => {
        if (res.confirm) {
          this.performanceMode.value = PerformanceMode.ULTRA_POWER_SAVING
          this.applyUltraPowerSavingMode()
        }
      }
    })

    this.emit('critical-battery-warning', this.batteryStatus.level)
  }

  /**
   * 进入应急省电模式
   */
  enterEmergencyPowerSaving() {
    console.log('进入应急省电模式')

    // 立即进入超级省电模式
    this.performanceMode.value = PerformanceMode.ULTRA_POWER_SAVING
    this.applyUltraPowerSavingMode()

    // 暂停所有非关键任务
    this.pauseNonCriticalTasks()

    // 发送应急状态事件
    this.emit('emergency-power-saving', true)
  }

  /**
   * 更新任务优先级
   */
  updateTaskPriority(priority) {
    uni.$emit('update-task-priority', priority)
  }

  /**
   * 更新动画设置
   */
  updateAnimationSettings(enabled) {
    uni.$emit('update-animation-settings', enabled)
  }

  /**
   * 限制后台任务
   */
  limitBackgroundTasks(maxTasks) {
    const currentTasks = this.backgroundTasks.length

    if (currentTasks > maxTasks) {
      // 暂停多余的任务
      const tasksToPause = this.backgroundTasks.slice(maxTasks)
      tasksToPause.forEach(task => {
        this.pauseTask(task.id)
      })
    }

    uni.$emit('limit-background-tasks', maxTasks)
  }

  /**
   * 暂停非关键任务
   */
  pauseNonCriticalTasks() {
    this.taskQueue.forEach(task => {
      if (task.priority !== 'critical') {
        this.pauseTask(task.id)
      }
    })

    uni.$emit('pause-non-critical-tasks')
  }

  /**
   * 暂停任务
   */
  pauseTask(taskId) {
    uni.$emit('pause-task', taskId)
  }

  /**
   * 降低屏幕亮度
   */
  reduceScreenBrightness() {
    // #ifdef APP-PLUS
    try {
      const main = plus.android.runtimeMainActivity()
      const WindowManager = plus.android.importClass("android.view.WindowManager")
      const layoutParams = main.getWindow().getAttributes()

      // 降低屏幕亮度到30%
      layoutParams.screenBrightness = 0.3
      main.getWindow().setAttributes(layoutParams)
    } catch (error) {
      console.warn('降低屏幕亮度失败:', error)
    }
    // #endif
  }

  /**
   * 开始优化调度器
   */
  startOptimizationScheduler() {
    this.optimizationTimer = setInterval(() => {
      this.performPeriodicOptimization()
    }, 60000) // 每分钟检查一次
  }

  /**
   * 执行周期性优化
   */
  performPeriodicOptimization() {
    // 清理过期的优化记录
    this.cleanupOptimizationRecords()

    // 检查任务执行情况
    this.checkTaskExecution()

    // 更新能耗统计
    this.updateEnergyStatistics()
  }

  /**
   * 清理优化记录
   */
  cleanupOptimizationRecords() {
    // 清理超过1小时的优化记录
    const oneHour = 60 * 60 * 1000
    const now = Date.now()

    this.optimizedTasks.forEach((task, id) => {
      if (now - task.lastOptimized > oneHour) {
        this.optimizedTasks.delete(id)
      }
    })
  }

  /**
   * 检查任务执行情况
   */
  checkTaskExecution() {
    this.taskQueue.forEach(task => {
      if (task.startedAt && Date.now() - task.startedAt > this.config.taskTimeoutMs) {
        // 任务超时，重新调度或取消
        this.handleTaskTimeout(task)
      }
    })
  }

  /**
   * 处理任务超时
   */
  handleTaskTimeout(task) {
    console.warn('任务超时:', task.id)

    // 移除超时任务
    const index = this.taskQueue.findIndex(t => t.id === task.id)
    if (index > -1) {
      this.taskQueue.splice(index, 1)
    }

    // 触发超时事件
    this.emit('task-timeout', task)
  }

  /**
   * 更新能耗统计
   */
  updateEnergyStatistics() {
    // 根据当前性能模式估算能耗节省
    const mode = this.performanceMode.value
    let energySavingRate = 0

    switch (mode) {
      case PerformanceMode.POWER_SAVING:
        energySavingRate = 0.2
        break
      case PerformanceMode.ULTRA_POWER_SAVING:
        energySavingRate = 0.4
        break
      default:
        energySavingRate = 0
    }

    if (energySavingRate > 0) {
      this.statistics.energySaved += energySavingRate
      this.statistics.batteryLifeExtended += energySavingRate * 60 // 分钟
    }
  }

  /**
   * 添加任务到队列
   */
  addTask(task) {
    const taskWithMeta = {
      ...task,
      id: task.id || this.generateTaskId(),
      addedAt: Date.now(),
      startedAt: null,
      priority: task.priority || 'normal',
      optimized: false
    }

    this.taskQueue.push(taskWithMeta)
    this.scheduleNextTask()

    return taskWithMeta.id
  }

  /**
   * 调度下一个任务
   */
  scheduleNextTask() {
    if (this.activeTasks.size >= this.config.maxBackgroundTasks) {
      return
    }

    // 按优先级排序任务
    this.taskQueue.sort((a, b) => {
      const priorityOrder = {
        'critical': 0,
        'high': 1,
        'normal': 2,
        'low': 3,
        'minimal': 4
      }
      return priorityOrder[a.priority] - priorityOrder[b.priority]
    })

    const nextTask = this.taskQueue.shift()
    if (nextTask) {
      this.executeTask(nextTask)
    }
  }

  /**
   * 执行任务
   */
  executeTask(task) {
    console.log('执行任务:', task.id, task.priority)

    this.activeTasks.add(task.id)
    task.startedAt = Date.now()

    // 触发任务执行事件
    this.emit('task-executed', task)

    // 模拟任务执行
    setTimeout(() => {
      this.completeTask(task.id)
    }, this.config.taskTimeoutMs)
  }

  /**
   * 完成任务
   */
  completeTask(taskId) {
    this.activeTasks.delete(taskId)

    // 调度下一个任务
    this.scheduleNextTask()

    console.log('任务完成:', taskId)
  }

  /**
   * 生成任务ID
   */
  generateTaskId() {
    return `task_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }

  /**
   * 事件监听
   */
  on(event, callback) {
    if (!this.eventListeners.has(event)) {
      this.eventListeners.set(event, new Set())
    }
    this.eventListeners.get(event).add(callback)
  }

  /**
   * 取消事件监听
   */
  off(event, callback = null) {
    if (callback) {
      this.eventListeners.get(event)?.delete(callback)
    } else {
      this.eventListeners.delete(event)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data = null) {
    const listeners = this.eventListeners.get(event)
    if (listeners) {
      listeners.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('执行事件回调失败:', error)
        }
      })
    }
  }

  /**
   * 获取电池信息
   */
  getBatteryInfo() {
    return {
      level: this.batteryStatus.level,
      isCharging: this.batteryStatus.isCharging,
      status: this.batteryStatus.status,
      chargingTime: this.batteryStatus.chargingTime,
      dischargingTime: this.batteryStatus.dischargingTime
    }
  }

  /**
   * 获取性能模式
   */
  getPerformanceMode() {
    return this.performanceMode.value
  }

  /**
   * 获取统计信息
   */
  getStatistics() {
    return { ...this.statistics }
  }

  /**
   * 获取系统信息
   */
  getSystemInfo() {
    return { ...this.systemInfo }
  }

  /**
   * 手动设置性能模式
   */
  setPerformanceMode(mode) {
    this.performanceMode.value = mode
    this.applyPerformanceMode()
  }

  /**
   * 启用/禁用自动优化
   */
  setAutoOptimization(enabled) {
    this.config.enableAutoOptimization = enabled
  }

  /**
   * 销毁电池优化器
   */
  destroy() {
    if (this.monitoringTimer) {
      clearInterval(this.monitoringTimer)
    }
    if (this.optimizationTimer) {
      clearInterval(this.optimizationTimer)
    }

    this.eventListeners.clear()
    this.taskQueue = []
    this.activeTasks.clear()
    this.backgroundTasks = []
    this.optimizedTasks.clear()

    console.log('电池优化管理器已销毁')
  }
}

/**
 * 创建电池优化器实例
 */
export function createBatteryOptimizer(options = {}) {
  return new BatteryOptimizer(options)
}

/**
 * 使用电池优化的 Composable
 */
export function useBatteryOptimizer(options = {}) {
  const optimizer = createBatteryOptimizer(options)

  return {
    optimizer,
    batteryStatus: optimizer.batteryStatus,
    performanceMode: optimizer.performanceMode,
    systemInfo: optimizer.systemInfo,
    statistics: optimizer.statistics,
    getBatteryInfo: optimizer.getBatteryInfo.bind(optimizer),
    getPerformanceMode: optimizer.getPerformanceMode.bind(optimizer),
    setPerformanceMode: optimizer.setPerformanceMode.bind(optimizer),
    addTask: optimizer.addTask.bind(optimizer),
    on: optimizer.on.bind(optimizer),
    off: optimizer.off.bind(optimizer),
    destroy: optimizer.destroy.bind(optimizer)
  }
}

export default BatteryOptimizer