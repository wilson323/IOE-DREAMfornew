# 前端事件展示能力规格

**能力ID**: frontend-event-display
**优先级**: P0
**创建日期**: 2025-01-30
**状态**: 提案中

---

## ADDED Requirements

### REQ-FRONTEND-EVENT-001: 设备AI事件展示页面

**优先级**: P0
**需求描述**: 前端必须提供设备AI事件展示页面，支持实时更新、筛选、分页和数据导出。

**场景**:

#### Scenario: 事件列表展示

**Given** 管理员登录系统
**When** 访问设备AI事件页面
**Then** 系统应该：
- 显示事件列表（分页）
- 显示事件类型（Tag颜色区分）
- 显示置信度（进度条）
- 显示抓拍图片（缩略图）
- 支持按设备、类型、时间筛选
- 支持导出Excel

**UI原型**:
```vue
<template>
  <div class="device-events-page">
    <!-- 筛选器 -->
    <a-card title="筛选条件">
      <a-form layout="inline">
        <a-form-item label="设备">
          <a-select v-model="filter.deviceId">
            <a-select-option value="">全部设备</a-select-option>
            <a-select-option v-for="device in devices"
                            :key="device.deviceId"
                            :value="device.deviceId">
              {{ device.deviceName }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="事件类型">
          <a-select v-model="filter.eventType">
            <a-select-option value="">全部类型</a-select-option>
            <a-select-option value="FALL_DETECTION">跌倒检测</a-select-option>
            <a-select-option value="LOITERING_DETECTION">徘徊检测</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="时间范围">
          <a-range-picker v-model="filter.timeRange" show-time />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" @click="handleSearch">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
          <a-button @click="handleExport">导出Excel</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 事件列表 -->
    <a-table
      :columns="columns"
      :data-source="events"
      :pagination="pagination"
      @change="handleTableChange">

      <!-- 事件类型Tag -->
      <template #eventType="{ record }">
        <a-tag :color="getEventTypeColor(record.eventType)">
          {{ getEventTypeName(record.eventType) }}
        </a-tag>
      </template>

      <!-- 置信度进度条 -->
      <template #confidence="{ record }">
        <a-progress
          :percent="record.confidence * 100"
          :stroke-color="getConfidenceColor(record.confidence)" />
      </template>

      <!-- 抓拍图片 -->
      <template #snapshot="{ record }">
        <a-image
          v-if="record.snapshotUrl"
          :src="record.snapshotUrl"
          :width="100"
          :preview="{ src: record.snapshotUrl }" />
      </template>

      <!-- 操作 -->
      <template #action="{ record }">
        <a-button type="link" @click="handleViewDetail(record)">
          查看详情
        </a-button>
        <a-button type="link" @click="handleViewVideo(record)">
          查看视频
        </a-button>
      </template>
    </a-table>

    <!-- 事件详情弹窗 -->
    <EventDetailModal
      v-model="detailVisible"
      :event="selectedEvent" />
  </div>
</template>
```

#### Scenario: 实时更新

**Given** 管理员在事件页面
**When** 设备上报新事件
**Then** 页面应该：
- 实时在列表顶部显示新事件
- 高亮新事件（闪烁效果）
- 播放提示音（可选）

**WebSocket集成**:
```javascript
export default {
  data() {
    return {
      events: []
    }
  },

  mounted() {
    // 建立WebSocket连接
    this.$store.dispatch('websocket/connect')

    // 订阅实时事件
    this.$store.subscribe((mutation, state) => {
      if (mutation.type === 'deviceEvents/ADD_EVENT') {
        // 新事件添加到列表顶部
        this.events.unshift(mutation.payload)

        // 高亮新事件
        this.$nextTick(() => {
          this.highlightNewEvent(mutation.payload.eventId)
        })

        // 播放提示音
        this.playNotificationSound()
      }
    })
  },

  methods: {
    highlightNewEvent(eventId) {
      const row = document.querySelector(`[data-event-id="${eventId}"]`)
      if (row) {
        row.classList.add('highlight-animation')
        setTimeout(() => {
          row.classList.remove('highlight-animation')
        }, 3000)
      }
    }
  }
}
```

---

### REQ-FRONTEND-EVENT-002: 告警管理页面

**优先级**: P0
**需求描述**: 前端必须提供告警管理页面，支持告警列表展示、告警处理和告警统计。

**场景**:

#### Scenario: 告警列表展示

**Given** 管理员登录系统
**When** 访问告警管理页面
**Then** 系统应该：
- 显示告警统计卡片（待处理、今日、本周、完成率）
- 显示告警Tab（待处理、处理中、已处理）
- 显示告警列表（分页）
- 支持告警操作（分配、备注、关闭）

**UI原型**:
```vue
<template>
  <div class="alarm-management-page">
    <!-- 告警统计卡片 -->
    <a-row :gutter="16">
      <a-col :span="6">
        <a-statistic
          title="待处理告警"
          :value="stats.pending"
          :value-style="{ color: '#ff4d4f' }" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="今日告警总数"
          :value="stats.todayTotal" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="本周告警总数"
          :value="stats.weekTotal" />
      </a-col>
      <a-col :span="6">
        <a-statistic
          title="处理完成率"
          :value="stats.completionRate"
          suffix="%" />
      </a-col>
    </a-row>

    <!-- 告警列表 -->
    <a-card title="告警列表">
      <a-tabs v-model:activeKey="activeTab">
        <a-tab-pane key="0" tab="待处理">
          <AlarmList
            :alarms="pendingAlarms"
            @handle="handleAlarm" />
        </a-tab-pane>

        <a-tab-pane key="1" tab="处理中">
          <AlarmList
            :alarms="processingAlarms"
            @handle="handleAlarm" />
        </a-tab-pane>

        <a-tab-pane key="2" tab="已处理">
          <AlarmList
            :alarms="completedAlarms"
            :view-only="true" />
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 处理告警弹窗 -->
    <AlarmHandleModal
      v-model="handleVisible"
      :alarm="selectedAlarm"
      @confirm="handleAlarmConfirm" />
  </div>
</template>
```

#### Scenario: 告警处理

**Given** 管理员打开待处理告警
**When** 点击"处理"按钮
**Then** 系统应该：
- 打开处理弹窗
- 显示告警详情
- 允许选择处理人
- 允许输入处理备注
- 提交后更新告警状态

**处理流程**:
```javascript
handleAlarm(alarm) {
  this.selectedAlarm = alarm
  this.handleVisible = true
},

handleAlarmConfirm({ handlerId, remark }) {
  // 调用API处理告警
  updateAlarmStatus({
    alarmId: this.selectedAlarm.alarmId,
    alarmStatus: 1,  // 处理中
    handlerId,
    remark
  }).then(() => {
    this.$message.success('告警已分配')
    this.handleVisible = false
    this.loadAlarms()
  })
}
```

---

### REQ-FRONTEND-EVENT-003: 实时监控面板

**优先级**: P0
**需求描述**: 前端必须提供实时监控面板，显示实时事件流、设备状态监控和告警趋势。

**场景**:

#### Scenario: 实时事件流

**Given** 管理员访问实时监控面板
**When** 设备持续上报事件
**Then** 面板应该：
- 实时显示最新事件（最多50条）
- 自动滚动到最新事件
- 显示事件速度（事件/分钟）

**UI原型**:
```vue
<template>
  <div class="realtime-monitor-page">
    <!-- 实时事件流 -->
    <a-card title="实时事件流" class="events-stream">
      <a-list
        :data-source="realtimeEvents"
        :split="false"
        size="small">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :src="item.snapshotUrl" />
              </template>
              <template #title>
                {{ getEventTypeName(item.eventType) }}
              </template>
              <template #description>
                {{ item.deviceCode }} · {{ item.eventTime }}
              </template>
            </a-list-item-meta>
          </a-list-item>
        </template>
      </a-list>
    </a-card>

    <!-- 设备状态监控 -->
    <a-card title="设备状态监控" class="device-status">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-statistic
            title="在线设备"
            :value="deviceStats.online"
            suffix="/ {{ deviceStats.total }}" />
        </a-col>
        <a-col :span="12">
          <a-statistic
            title="今日事件总数"
            :value="eventStats.todayCount" />
        </a-col>
      </a-row>
    </a-card>

    <!-- 告警趋势图 -->
    <a-card title="告警趋势">
      <div ref="trendChart" style="height: 300px"></div>
    </a-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'

export default {
  data() {
    return {
      realtimeEvents: [],
      deviceStats: { online: 45, total: 50 },
      eventStats: { todayCount: 1250 }
    }
  },

  mounted() {
    this.initTrendChart()
    this.subscribeRealtimeEvents()
  },

  methods: {
    initTrendChart() {
      const chart = echarts.init(this.$refs.trendChart)

      chart.setOption({
        xAxis: { type: 'category', data: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00'] },
        yAxis: { type: 'value' },
        series: [{
          data: [12, 45, 120, 200, 150, 80],
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.3 }
        }]
      })
    },

    subscribeRealtimeEvents() {
      this.$store.subscribe((mutation, state) => {
        if (mutation.type === 'deviceEvents/ADD_EVENT') {
          // 新事件添加到实时流（最多保留50条）
          this.realtimeEvents.unshift(mutation.payload)
          if (this.realtimeEvents.length > 50) {
            this.realtimeEvents.pop()
          }
        }
      })
    }
  }
}
</script>
```

---

### REQ-FRONTEND-EVENT-004: 数据导出

**优先级**: P0
**需求描述**: 前端必须支持数据导出功能，支持Excel和PDF格式导出。

**场景**:

#### Scenario: 导出Excel

**Given** 管理员筛选出事件列表
**When** 点击"导出Excel"按钮
**Then** 系统应该：
- 导出当前筛选结果
- 生成Excel文件
- 自动下载

**实现**:
```javascript
async handleExport() {
  const blob = await exportDeviceEvents(this.filter)

  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `设备AI事件_${dayjs().format('YYYYMMDD_HHmmss')}.xlsx`
  a.click()
  window.URL.revokeObjectURL(url)
}
```

---

## MODIFIED Requirements

*（本能力为新增，无修改的需求）*

---

## REMOVED Requirements

*（本能力为新增，无删除的需求）*

---

## 附录

### A. 组件清单

| 组件名 | 功能 | 位置 |
|--------|------|------|
| DeviceEvents.vue | 设备AI事件列表页面 | views/video/ |
| EventList.vue | 事件列表组件 | components/video/ |
| EventDetailModal.vue | 事件详情弹窗 | components/video/ |
| AlarmManagement.vue | 告警管理页面 | views/video/ |
| AlarmList.vue | 告警列表组件 | components/video/ |
| AlarmHandleModal.vue | 告警处理弹窗 | components/video/ |
| RealtimeMonitor.vue | 实时监控面板 | views/video/ |
| RealtimeChart.vue | 实时图表组件 | components/video/ |

### B. 性能要求

| 指标 | 目标值 | 优化措施 |
|------|--------|----------|
| 首屏加载时间 | < 2秒 | 路由懒加载、代码分割 |
| 列表渲染时间 | < 500ms | 虚拟滚动 |
| 实时更新延迟 | < 300ms | WebSocket优化 |
| 图表渲染时间 | < 1秒 | ECharts按需加载 |

### C. 兼容性

| 浏览器 | 版本 | 支持情况 |
|--------|------|----------|
| Chrome | >= 90 | ✅ 完全支持 |
| Firefox | >= 88 | ✅ 完全支持 |
| Edge | >= 90 | ✅ 完全支持 |
| Safari | >= 14 | ✅ 完全支持（WebSocket需要HTTPS） |

---

**规格编写人**: IOE-DREAM 架构委员会
**创建日期**: 2025-01-30
**版本**: 1.0.0
