<!--
  * 消费报表统计 - 企业级完整实现
  * 支持多维度统计、图表可视化、数据导出
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="consume-report-page">
    <!-- 报表类型选择 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <template #title>
        <span>报表类型</span>
      </template>
      <a-radio-group v-model:value="reportType" @change="handleReportTypeChange" button-style="solid">
        <a-radio-button value="SALES">销售报表</a-radio-button>
        <a-radio-button value="AREA">区域分析</a-radio-button>
        <a-radio-button value="ACCOUNT">账户分析</a-radio-button>
        <a-radio-button value="DEVICE">设备统计</a-radio-button>
        <a-radio-button value="TIME">时间趋势</a-radio-button>
      </a-radio-group>
    </a-card>

    <!-- 查询条件 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <template #title>
        <span>查询条件</span>
      </template>
      <a-form ref="formRef" :model="queryForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="时间范围">
              <a-range-picker
                v-model:value="queryForm.dateRange"
                format="YYYY-MM-DD"
                :ranges="dateRanges"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="粒度">
              <a-select v-model:value="queryForm.granularity">
                <a-select-option value="HOUR">按小时</a-select-option>
                <a-select-option value="DAY">按天</a-select-option>
                <a-select-option value="WEEK">按周</a-select-option>
                <a-select-option value="MONTH">按月</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="区域">
              <a-tree-select
                v-model:value="queryForm.areaIds"
                multiple
                tree-checkable
                :tree-data="areaTree"
                placeholder="全部区域"
                allow-clear
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="设备类型">
              <a-select v-model:value="queryForm.deviceType" placeholder="全部设备" allow-clear>
                <a-select-option value="POS">POS机</a-select-option>
                <a-select-option value="CONSUME_MACHINE">消费机</a-select-option>
                <a-select-option value="CARD_READER">读卡器</a-select-option>
                <a-select-option value="BIOMETRIC">生物识别设备</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="账户类别">
              <a-select v-model:value="queryForm.accountKindId" placeholder="全部类别" allow-clear>
                <a-select-option v-for="kind in accountKindList" :key="kind.accountKindId" :value="kind.accountKindId">
                  {{ kind.kindName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item>
              <a-space>
                <a-button type="primary" @click="handleQuery" :loading="loading">
                  <template #icon><SearchOutlined /></template>
                  查询
                </a-button>
                <a-button @click="handleReset">
                  <template #icon><ClearOutlined /></template>
                  重置
                </a-button>
                <a-button @click="handleExport" :loading="exporting">
                  <template #icon><DownloadOutlined /></template>
                  导出
                </a-button>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-card>

    <!-- 统计概览 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="总交易笔数"
            :value="statistics.totalTransactions"
            suffix="笔"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <TransactionOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="总交易金额"
            :value="statistics.totalAmount"
            prefix="¥"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <MoneyCollectOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="活跃用户数"
            :value="statistics.activeUsers"
            suffix="人"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card :bordered="false">
          <a-statistic
            title="平均笔单价"
            :value="statistics.avgAmount"
            prefix="¥"
            :value-style="{ color: '#eb2f96' }"
          >
            <template #prefix>
              <BarChartOutlined />
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表展示 -->
    <a-row :gutter="16" style="margin-bottom: 16px">
      <!-- 交易趋势图 -->
      <a-col :span="16">
        <a-card :bordered="false" title="交易趋势">
          <div ref="trendChartRef" style="width: 100%; height: 350px"></div>
        </a-card>
      </a-col>
      <!-- 区域分布图 -->
      <a-col :span="8">
        <a-card :bordered="false" title="区域分布">
          <div ref="areaChartRef" style="width: 100%; height: 350px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="16" style="margin-bottom: 16px">
      <!-- 消费时段分布 -->
      <a-col :span="12">
        <a-card :bordered="false" title="消费时段分布">
          <div ref="timeChartRef" style="width: 100%; height: 300px"></div>
        </a-card>
      </a-col>
      <!-- 消费模式分布 -->
      <a-col :span="12">
        <a-card :bordered="false" title="消费模式分布">
          <div ref="modeChartRef" style="width: 100%; height: 300px"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 详细数据表格 -->
    <a-card :bordered="false">
      <template #title>
        <span>详细数据</span>
      </template>
      <template #extra>
        <a-radio-group v-model:value="detailTab" button-style="solid">
          <a-radio-button value="AREA">按区域</a-radio-button>
          <a-radio-button value="DEVICE">按设备</a-radio-button>
          <a-radio-button value="ACCOUNT">按账户类别</a-radio-button>
        </a-radio-group>
      </template>
      <a-table
        :columns="detailColumns"
        :data-source="detailData"
        :pagination="detailPagination"
        :loading="loading"
        row-key="id"
        @change="handleDetailTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'amount'">
            <span style="color: #52c41a; font-weight: 600">¥{{ formatAmount(record.amount) }}</span>
          </template>
          <template v-else-if="column.key === 'percentage'">
            <a-progress
              :percent="record.percentage"
              :stroke-color="{
                '0%': '#108ee9',
                '100%': '#87d068',
              }"
            />
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, watch, computed, nextTick } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    SearchOutlined,
    ClearOutlined,
    DownloadOutlined,
    TransactionOutlined,
    MoneyCollectOutlined,
    UserOutlined,
    BarChartOutlined,
  } from '@ant-design/icons-vue';
  import * as echarts from 'echarts';
  import dayjs from 'dayjs';

  const formRef = ref();
  const loading = ref(false);
  const exporting = ref(false);
  const reportType = ref('SALES');
  const detailTab = ref('AREA');

  // 日期范围快捷选项
  const dateRanges = {
    '今天': [dayjs().startOf('day'), dayjs().endOf('day')],
    '本周': [dayjs().startOf('week'), dayjs().endOf('week')],
    '本月': [dayjs().startOf('month'), dayjs().endOf('month')],
    '最近7天': [dayjs().subtract(7, 'day'), dayjs()],
    '最近30天': [dayjs().subtract(30, 'day'), dayjs()],
    '本季度': [dayjs().startOf('quarter'), dayjs().endOf('quarter')],
  };

  const queryForm = reactive({
    dateRange: [dayjs().subtract(7, 'day'), dayjs()],
    granularity: 'DAY',
    areaIds: [],
    deviceType: undefined,
    accountKindId: undefined,
  });

  // 模拟数据
  const areaTree = ref([
    {
      title: '第一食堂',
      value: '1',
      children: [
        { title: '一楼主食区', value: '1-1' },
        { title: '一楼副食区', value: '1-2' },
        { title: '二楼风味区', value: '1-3' },
      ],
    },
    {
      title: '第二食堂',
      value: '2',
      children: [
        { title: '清真餐厅', value: '2-1' },
        { title: '自助餐厅', value: '2-2' },
      ],
    },
    {
      title: '超市',
      value: '3',
      children: [
        { title: '一楼超市', value: '3-1' },
        { title: '二楼便利店', value: '3-2' },
      ],
    },
  ]);

  const accountKindList = ref([
    { accountKindId: 1, kindName: '员工账户' },
    { accountKindId: 2, kindName: '学生账户' },
    { accountKindId: 3, kindName: '临时账户' },
  ]);

  // 统计概览
  const statistics = ref({
    totalTransactions: 15234,
    totalAmount: 456780.50,
    activeUsers: 892,
    avgAmount: 29.98,
  });

  // 图表引用
  const trendChartRef = ref();
  const areaChartRef = ref();
  const timeChartRef = ref();
  const modeChartRef = ref();

  // 图表实例
  let trendChart = null;
  let areaChart = null;
  let timeChart = null;
  let modeChart = null;

  // 详情数据
  const detailData = ref([
    { id: 1, name: '第一食堂-一楼主食区', transactions: 3456, amount: 51840, percentage: 35 },
    { id: 2, name: '第一食堂-二楼风味区', transactions: 2345, amount: 35175, percentage: 25 },
    { id: 3, name: '第二食堂-清真餐厅', transactions: 2123, amount: 31845, percentage: 20 },
    { id: 4, name: '超市-一楼超市', transactions: 1876, amount: 28140, percentage: 15 },
    { id: 5, name: '其他区域', transactions: 5434, amount: 54340, percentage: 5 },
  ]);

  const detailPagination = reactive({
    current: 1,
    pageSize: 10,
    total: 5,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  const detailColumns = computed(() => {
    if (detailTab.value === 'AREA') {
      return [
        { title: '区域名称', dataIndex: 'name', key: 'name', width: 200 },
        { title: '交易笔数', dataIndex: 'transactions', key: 'transactions', width: 120, align: 'right' },
        { title: '交易金额', key: 'amount', width: 120, align: 'right' },
        { title: '占比', key: 'percentage', width: 150 },
      ];
    } else if (detailTab.value === 'DEVICE') {
      return [
        { title: '设备名称', dataIndex: 'name', key: 'name', width: 180 },
        { title: '设备类型', dataIndex: 'type', key: 'type', width: 120 },
        { title: '交易笔数', dataIndex: 'transactions', key: 'transactions', width: 120, align: 'right' },
        { title: '交易金额', key: 'amount', width: 120, align: 'right' },
        { title: '占比', key: 'percentage', width: 150 },
      ];
    } else {
      return [
        { title: '账户类别', dataIndex: 'name', key: 'name', width: 150 },
        { title: '用户数', dataIndex: 'users', key: 'users', width: 100, align: 'right' },
        { title: '交易笔数', dataIndex: 'transactions', key: 'transactions', width: 120, align: 'right' },
        { title: '交易金额', key: 'amount', width: 120, align: 'right' },
        { title: '人均消费', key: 'avgAmount', width: 120, align: 'right' },
      ];
    }
  });

  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return Number(amount).toFixed(2);
  };

  // 初始化趋势图
  const initTrendChart = () => {
    if (!trendChartRef.value) return;
    trendChart = echarts.init(trendChartRef.value);

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
      },
      legend: {
        data: ['交易笔数', '交易金额'],
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: ['01-15', '01-16', '01-17', '01-18', '01-19', '01-20', '01-21'],
      },
      yAxis: [
        {
          type: 'value',
          name: '交易笔数',
          position: 'left',
        },
        {
          type: 'value',
          name: '交易金额',
          position: 'right',
        },
      ],
      series: [
        {
          name: '交易笔数',
          type: 'line',
          smooth: true,
          data: [2100, 2300, 1950, 2450, 2200, 2600, 2334],
          itemStyle: { color: '#1890ff' },
        },
        {
          name: '交易金额',
          type: 'line',
          smooth: true,
          yAxisIndex: 1,
          data: [63000, 69000, 58500, 73500, 66000, 78000, 70280],
          itemStyle: { color: '#52c41a' },
        },
      ],
    };

    trendChart.setOption(option);
  };

  // 初始化区域饼图
  const initAreaChart = () => {
    if (!areaChartRef.value) return;
    areaChart = echarts.init(areaChartRef.value);

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        orient: 'vertical',
        right: 10,
        top: 'center',
      },
      series: [
        {
          name: '交易金额',
          type: 'pie',
          radius: ['40%', '70%'],
          center: ['40%', '50%'],
          avoidLabelOverlap: false,
          label: {
            show: false,
            position: 'center',
          },
          emphasis: {
            label: {
              show: true,
              fontSize: 20,
              fontWeight: 'bold',
            },
          },
          labelLine: {
            show: false,
          },
          data: [
            { value: 51840, name: '一楼主食区' },
            { value: 35175, name: '二楼风味区' },
            { value: 31845, name: '清真餐厅' },
            { value: 28140, name: '一楼超市' },
            { value: 54340, name: '其他' },
          ],
        },
      ],
    };

    areaChart.setOption(option);
  };

  // 初始化时段分布图
  const initTimeChart = () => {
    if (!timeChartRef.value) return;
    timeChart = echarts.init(timeChartRef.value);

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true,
      },
      xAxis: {
        type: 'category',
        data: ['6-8点', '8-10点', '10-12点', '12-14点', '14-16点', '16-18点', '18-20点', '20-22点'],
      },
      yAxis: {
        type: 'value',
        name: '交易笔数',
      },
      series: [
        {
          name: '早餐',
          type: 'bar',
          stack: 'total',
          data: [800, 400, 100, 50, 30, 20, 10, 5],
          itemStyle: { color: '#91cc75' },
        },
        {
          name: '午餐',
          type: 'bar',
          stack: 'total',
          data: [100, 1200, 1800, 800, 200, 100, 50, 20],
          itemStyle: { color: '#fac858' },
        },
        {
          name: '晚餐',
          type: 'bar',
          stack: 'total',
          data: [50, 300, 600, 1500, 700, 300, 100, 30],
          itemStyle: { color: '#ee6666' },
        },
      ],
      legend: {
        data: ['早餐', '午餐', '晚餐'],
      },
    };

    timeChart.setOption(option);
  };

  // 初始化消费模式图
  const initModeChart = () => {
    if (!modeChartRef.value) return;
    modeChart = echarts.init(modeChartRef.value);

    const option = {
      tooltip: {
        trigger: 'item',
        formatter: '{a} <br/>{b}: {c} ({d}%)',
      },
      legend: {
        bottom: 0,
      },
      series: [
        {
          name: '消费模式',
          type: 'pie',
          radius: '60%',
          data: [
            { value: 5234, name: '固定金额' },
            { value: 3456, name: '自由金额' },
            { value: 2345, name: '商品模式' },
            { value: 1876, name: '订餐模式' },
            { value: 1234, name: '计量计费' },
            { value: 1089, name: '智能模式' },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    };

    modeChart.setOption(option);
  };

  // 查询
  const handleQuery = async () => {
    loading.value = true;
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));
      message.success('查询成功');
    } finally {
      loading.value = false;
    }
  };

  // 重置
  const handleReset = () => {
    queryForm.dateRange = [dayjs().subtract(7, 'day'), dayjs()];
    queryForm.granularity = 'DAY';
    queryForm.areaIds = [];
    queryForm.deviceType = undefined;
    queryForm.accountKindId = undefined;
  };

  // 导出
  const handleExport = async () => {
    exporting.value = true;
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 1000));
      message.success('导出成功');
    } finally {
      exporting.value = false;
    }
  };

  // 报表类型切换
  const handleReportTypeChange = () => {
    // 重新加载图表数据
    refreshCharts();
  };

  // 详情表格切换
  watch(detailTab, () => {
    // 切换详情数据源
    refreshDetailData();
  });

  // 详情表格分页变化
  const handleDetailTableChange = (pag) => {
    detailPagination.current = pag.current;
    detailPagination.pageSize = pag.pageSize;
  };

  // 刷新图表
  const refreshCharts = () => {
    nextTick(() => {
      initTrendChart();
      initAreaChart();
      initTimeChart();
      initModeChart();
    });
  };

  // 刷新详情数据
  const refreshDetailData = () => {
    // 根据不同标签页加载不同的数据
    if (detailTab.value === 'DEVICE') {
      detailData.value = [
        { id: 1, name: '一楼主食区POS机', type: 'POS机', transactions: 3456, amount: 51840, percentage: 35 },
        { id: 2, name: '二楼消费机01', type: '消费机', transactions: 2345, amount: 35175, percentage: 25 },
        { id: 3, name: '人脸识别消费机', type: '生物识别', transactions: 2123, amount: 31845, percentage: 20 },
        { id: 4, name: '一楼超市POS', type: 'POS机', transactions: 1876, amount: 28140, percentage: 15 },
        { id: 5, name: '其他设备', type: '-', transactions: 5434, amount: 54340, percentage: 5 },
      ];
    } else if (detailTab.value === 'ACCOUNT') {
      detailData.value = [
        { id: 1, name: '员工账户', users: 456, transactions: 8765, amount: 131475, avgAmount: 15 },
        { id: 2, name: '学生账户', users: 321, transactions: 5432, amount: 108640, avgAmount: 20 },
        { id: 3, name: '临时账户', users: 115, transactions: 1037, amount: 20665.5, avgAmount: 19.93 },
      ];
    } else {
      detailData.value = [
        { id: 1, name: '第一食堂-一楼主食区', transactions: 3456, amount: 51840, percentage: 35 },
        { id: 2, name: '第一食堂-二楼风味区', transactions: 2345, amount: 35175, percentage: 25 },
        { id: 3, name: '第二食堂-清真餐厅', transactions: 2123, amount: 31845, percentage: 20 },
        { id: 4, name: '超市-一楼超市', transactions: 1876, amount: 28140, percentage: 15 },
        { id: 5, name: '其他区域', transactions: 5434, amount: 54340, percentage: 5 },
      ];
    }
  };

  // 组件挂载
  onMounted(() => {
    refreshCharts();
  });
</script>

<style lang="less" scoped>
  .consume-report-page {
    :deep(.ant-statistic) {
      .ant-statistic-title {
        font-size: 14px;
        color: #666;
      }
      .ant-statistic-content {
        font-size: 24px;
        font-weight: 600;
      }
    }
  }
</style>
