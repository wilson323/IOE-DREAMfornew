<template>
  <div class="discovery-statistics">
    <!-- 统计卡片 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="总扫描任务"
            :value="statistics.totalTasks"
            :prefix="h(SearchOutlined)"
            :value-style="{ color: '#1890ff' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="发现设备总数"
            :value="statistics.totalDiscoveredDevices"
            :prefix="h(DesktopOutlined)"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="在线设备数"
            :value="statistics.onlineDevices"
            :prefix="h(WifiOutlined)"
            :value-style="{ color: '#52c41a' }"
          />
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card>
          <a-statistic
            title="支持协议数"
            :value="statistics.supportedProtocols"
            :prefix="h(SettingOutlined)"
            :value-style="{ color: '#722ed1' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <!-- 协议分布图表 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="12">
        <a-card title="协议分布" :bordered="false">
          <div ref="protocolChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
      <a-col :span="12">
        <a-card title="设备类型分布" :bordered="false">
          <div ref="deviceTypeChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 厂商分布图表 -->
    <a-row :gutter="16" style="margin-bottom: 24px;">
      <a-col :span="24">
        <a-card title="厂商分布" :bordered="false">
          <div ref="vendorChartRef" style="height: 300px;"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 发现任务列表 -->
    <a-card title="最近发现任务" :bordered="false">
      <a-table
        :columns="taskColumns"
        :data-source="taskList"
        :pagination="taskPagination"
        :loading="loading"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'taskId'">
            <a-tag color="blue">{{ record.taskId }}</a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getStatusColor(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>

          <template v-else-if="column.key === 'duration'">
            {{ formatDuration(record.duration) }}
          </template>

          <template v-else-if="column.key === 'startTime'">
            {{ formatDateTime(record.startTime) }}
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button size="small" @click="viewTaskDetail(record)">
                详情
              </a-button>
              <a-button size="small" type="primary" @click="exportTaskResult(record)">
                导出
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  DesktopOutlined,
  WifiOutlined,
  SettingOutlined
} from '@ant-design/icons-vue'
import { deviceDiscoveryApi } from '/@/api/business/device/discovery'

export default {
  name: 'DiscoveryStatistics',
  components: {
    SearchOutlined,
    DesktopOutlined,
    WifiOutlined,
    SettingOutlined
  },
  props: {
    statistics: {
      type: Object,
      required: true
    }
  },
  setup(props) {
    // 响应式数据
    const protocolChartRef = ref()
    const deviceTypeChartRef = ref()
    const vendorChartRef = ref()
    const loading = ref(false)
    const taskList = ref([])

    // 任务分页配置
    const taskPagination = {
      current: 1,
      pageSize: 10,
      total: 0,
      showSizeChanger: true,
      showQuickJumper: true,
      showTotal: (total) => `共 ${total} 条`
    }

    // 任务表格列定义
    const taskColumns = [
      {
        title: '任务ID',
        key: 'taskId',
        width: 120
      },
      {
        title: '网络范围',
        dataIndex: 'networkRange',
        key: 'networkRange',
        ellipsis: true
      },
      {
        title: '状态',
        key: 'status',
        width: 80
      },
      {
        title: '发现设备数',
        dataIndex: 'discoveredDeviceCount',
        key: 'discoveredDeviceCount',
        width: 100
      },
      {
        title: '耗时',
        key: 'duration',
        width: 100
      },
      {
        title: '开始时间',
        key: 'startTime',
        width: 150
      },
      {
        title: '操作',
        key: 'action',
        width: 120,
        fixed: 'right'
      }
    ]

    // 生命周期
    onMounted(() => {
      loadTaskHistory()
      nextTick(() => {
        initCharts()
      })
    })

    // 加载任务历史
    const loadTaskHistory = async () => {
      try {
        loading.value = true
        const response = await deviceDiscoveryApi.getHistory(20)
        if (response.data) {
          taskList.value = response.data.map((task, index) => ({
            taskId: task.taskId || `task-${index + 1}`,
            networkRange: task.networkRange || '未知',
            status: task.status || 'UNKNOWN',
            discoveredDeviceCount: task.discoveredDevices ? Object.keys(task.discoveredDevices).length : 0,
            duration: task.scanDuration || 0,
            startTime: task.startTime,
            endTime: task.endTime,
            task: task
          }))
          taskPagination.total = taskList.value.length
        }
      } catch (error) {
        console.error('加载任务历史失败:', error)
        message.error('加载任务历史失败')
      } finally {
        loading.value = false
      }
    }

    // 初始化图表
    const initCharts = () => {
      initProtocolChart()
      initDeviceTypeChart()
      initVendorChart()
    }

    // 初始化协议分布图表
    const initProtocolChart = () => {
      if (!protocolChartRef.value) return

      const chart = echarts.init(protocolChartRef.value)

      // 模拟数据 - 实际应该从统计数据中获取
      const data = [
        { value: 156, name: '海康威视' },
        { value: 89, name: '大华' },
        { value: 45, name: '宇视' },
        { value: 32, name: '萤石' },
        { value: 28, name: 'Axis' },
        { value: 15, name: '其他' }
      ]

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [
          {
            name: '协议分布',
            type: 'pie',
            radius: '70%',
            data: data,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              show: true,
              formatter: '{b}: {c}'
            }
          }
        ]
      }

      chart.setOption(option)
    }

    // 初始化设备类型分布图表
    const initDeviceTypeChart = () => {
      if (!deviceTypeChartRef.value) return

      const chart = echarts.init(deviceTypeChartRef.value)

      // 模拟数据
      const data = [
        { value: 245, name: 'IP摄像头' },
        { value: 89, name: '网络设备' },
        { value: 56, name: 'PLC' },
        { value: 34, name: '服务器' },
        { value: 18, name: '其他' }
      ]

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        series: [
          {
            name: '设备类型',
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 20,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: data
          }
        ]
      }

      chart.setOption(option)
    }

    // 初始化厂商分布图表
    const initVendorChart = () => {
      if (!vendorChartRef.value) return

      const chart = echarts.init(vendorChartRef.value)

      // 模拟数据
      const data = {
        xAxis: ['海康威视', '大华', '宇视', '萤石', 'Axis', '思科', '西门子', '施耐德', '其他'],
        yAxis: ['数量'],
        series: [{
          type: 'bar',
          data: [156, 89, 45, 32, 28, 24, 18, 15, 25],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' }
            ])
          },
          emphasis: {
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#2378f7' },
                { offset: 0.7, color: '#2378f7' },
                { offset: 1, color: '#83bff6' }
              ])
            }
          }
        }]
      }

      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: data.xAxis,
          axisTick: {
            alignWithLabel: true
          }
        },
        yAxis: {
          type: 'value'
        },
        series: data.series
      }

      chart.setOption(option)
    }

    // 查看任务详情
    const viewTaskDetail = (task) => {
      // 这里可以打开一个弹窗显示任务详情
      message.info(`查看任务详情: ${task.taskId}`)
    }

    // 导出任务结果
    const exportTaskResult = async (task) => {
      try {
        const response = await deviceDiscoveryApi.exportResult(task.taskId, 'json')
        if (response.data) {
          // 下载JSON文件
          const blob = new Blob([response.data], { type: 'application/json;charset=utf-8;' })
          const link = document.createElement('a')
          const url = URL.createObjectURL(blob)
          link.setAttribute('href', url)
          link.setAttribute('download', `discovery_result_${task.taskId}_${Date.now()}.json`)
          link.style.visibility = 'hidden'
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)

          message.success('任务结果导出成功')
        }
      } catch (error) {
        message.error('导出任务结果失败: ' + error.message)
      }
    }

    // 获取状态颜色
    const getStatusColor = (status) => {
      const colorMap = {
        'RUNNING': 'processing',
        'COMPLETED': 'success',
        'FAILED': 'error',
        'CANCELLED': 'warning'
      }
      return colorMap[status] || 'default'
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'RUNNING': '运行中',
        'COMPLETED': '已完成',
        'FAILED': '失败',
        'CANCELLED': '已取消'
      }
      return textMap[status] || status
    }

    // 格式化持续时间
    const formatDuration = (duration) => {
      if (!duration || duration === 0) return '未知'

      const seconds = Math.floor(duration / 1000)
      const minutes = Math.floor(seconds / 60)
      const hours = Math.floor(minutes / 60)

      if (hours > 0) {
        return `${hours}小时${minutes % 60}分钟`
      } else if (minutes > 0) {
        return `${minutes}分钟${seconds % 60}秒`
      } else {
        return `${seconds}秒`
      }
    }

    // 格式化日期时间
    const formatDateTime = (dateTime) => {
      if (!dateTime) return '未知'

      try {
        const date = new Date(dateTime)
        return date.toLocaleString('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          second: '2-digit'
        })
      } catch (error) {
        return '格式化失败'
      }
    }

    return {
      protocolChartRef,
      deviceTypeChartRef,
      vendorChartRef,
      loading,
      taskList,
      taskPagination,
      taskColumns,
      viewTaskDetail,
      exportTaskResult,
      getStatusColor,
      getStatusText,
      formatDuration,
      formatDateTime
    }
  }
}
</script>

<style lang="scss" scoped>
.discovery-statistics {
  .ant-statistic {
    text-align: center;
  }

  .ant-card {
    text-align: center;
  }
}
</style>