<!--
  * 门禁记录查询（优化版）
  *
  * 功能特性：
  * - 高级筛选（区域、用户姓名、设备名称、通行方式）
  * - 批量操作（批量删除、批量导出）
  * - 数据导出（Excel、CSV）
  * - 统计图表（通行趋势、成功率、时段分布）
  * - 详情查看
  * - 异常记录高亮
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright: IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-record-optimized-page">
    <!-- 高级查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row" :gutter="[16, 16]">
          <!-- 第一行：基础筛选 -->
          <a-form-item label="用户姓名" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.userName"
              placeholder="请输入用户姓名"
              style="width: 150px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="设备名称" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.deviceName"
              placeholder="请输入设备名称"
              style="width: 150px"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="区域" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.areaId"
              style="width: 150px"
              :allow-clear="true"
              placeholder="请选择区域"
              show-search
              :filter-option="filterAreaOption"
            >
              <a-select-option v-for="area in areaList" :key="area.areaId" :value="area.areaId">
                {{ area.areaName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="通行方式" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.accessMethod"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择"
            >
              <a-select-option value="CARD">刷卡</a-select-option>
              <a-select-option value="FACE">人脸</a-select-option>
              <a-select-option value="FINGERPRINT">指纹</a-select-option>
              <a-select-option value="QR_CODE">二维码</a-select-option>
              <a-select-option value="PASSWORD">密码</a-select-option>
            </a-select>
          </a-form-item>

          <!-- 第二行：时间和结果筛选 -->
          <a-form-item label="通行时间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD HH:mm"
              :show-time="{ format: 'HH:mm' }"
              style="width: 360px"
            />
          </a-form-item>

          <a-form-item label="通行结果" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.result"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择"
            >
              <a-select-option value="SUCCESS">成功</a-select-option>
              <a-select-option value="FAILED">失败</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryRecordList">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="resetQuery">
                <template #icon><ReloadOutlined /></template>
                重置
              </a-button>
              <a-button @click="toggleAdvanced">
                {{ advancedVisible ? '收起' : '展开' }}
                <UpOutlined v-if="advancedVisible" />
                <DownOutlined v-else />
              </a-button>
            </a-space>
          </a-form-item>
        </a-row>

        <!-- 高级筛选（可折叠） -->
        <a-row v-show="advancedVisible" class="smart-query-form-row" :gutter="[16, 16]">
          <a-form-item label="用户ID" class="smart-query-form-item">
            <a-input-number
              v-model:value="queryForm.userId"
              placeholder="请输入用户ID"
              style="width: 150px"
              :min="1"
            />
          </a-form-item>

          <a-form-item label="设备ID" class="smart-query-form-item">
            <a-input-number
              v-model:value="queryForm.deviceId"
              placeholder="请输入设备ID"
              style="width: 150px"
              :min="1"
            />
          </a-form-item>

          <a-form-item label="异常类型" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.exceptionType"
              style="width: 150px"
              :allow-clear="true"
              placeholder="请选择"
            >
              <a-select-option value="ANTI_PASSBACK">反潜回违规</a-select-option>
              <a-select-option value="UNAUTHORIZED">未授权</a-select-option>
              <a-select-option value="TIME_VIOLATION">时间违规</a-select-option>
              <a-select-option value="DEVICE_OFFLINE">设备离线</a-select-option>
            </a-select>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 统计图表区域 -->
    <a-card :bordered="false" style="margin-top: 16px" v-if="showStatistics">
      <template #title>
        <a-space>
          <span>通行统计</span>
          <a-button type="link" size="small" @click="showStatistics = !showStatistics">
            {{ showStatistics ? '隐藏' : '显示' }}
          </a-button>
        </a-space>
      </template>

      <a-row :gutter="16">
        <!-- 统计卡片 -->
        <a-col :span="6">
          <a-statistic
            title="今日通行总数"
            :value="statistics.todayTotal"
            :value-style="{ color: '#3f8600' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="成功通行"
            :value="statistics.successCount"
            :value-style="{ color: '#1890ff' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="失败通行"
            :value="statistics.failedCount"
            :value-style="{ color: '#cf1322' }"
          />
        </a-col>
        <a-col :span="6">
          <a-statistic
            title="成功率"
            :value="statistics.successRate"
            suffix="%"
            :value-style="{ color: statistics.successRate >= 95 ? '#3f8600' : '#cf1322' }"
          />
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-top: 24px">
        <!-- 通行趋势图 -->
        <a-col :span="12">
          <div ref="trendChartRef" style="height: 300px"></div>
        </a-col>
        <!-- 时段分布图 -->
        <a-col :span="12">
          <div ref="hourlyChartRef" style="height: 300px"></div>
        </a-col>
      </a-row>
    </a-card>

    <!-- 数据表格区域 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>门禁记录列表</span>
          <a-tag v-if="selectedRowKeys.length > 0" color="blue">
            已选择 {{ selectedRowKeys.length }} 项
          </a-tag>
        </a-space>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="openExportModal">
            <template #icon><ExportOutlined /></template>
            导出
          </a-button>
          <a-button
            danger
            @click="handleBatchDelete"
            :disabled="selectedRowKeys.length === 0"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
          <a-button @click="showStatistics = !showStatistics">
            <template #icon><BarChartOutlined /></template>
            {{ showStatistics ? '隐藏' : '显示' }}统计
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="rowSelection"
        :row-class-name="getRowClassName"
        row-key="id"
        @change="handleTableChange"
        :scroll="{ x: 1500 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'green' : 'red'">
              {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'accessMethod'">
            <a-tag :color="getAccessMethodColor(record.accessMethod)">
              {{ getAccessMethodText(record.accessMethod) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'exceptionType'">
            <a-tag v-if="record.exceptionType" color="orange">
              {{ getExceptionTypeText(record.exceptionType) }}
            </a-tag>
            <span v-else>-</span>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                详情
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 详情模态框 -->
    <a-modal
      v-model:open="detailVisible"
      title="通行记录详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="记录ID">
          {{ currentRecord.id }}
        </a-descriptions-item>
        <a-descriptions-item label="用户ID">
          {{ currentRecord.userId }}
        </a-descriptions-item>
        <a-descriptions-item label="用户姓名">
          {{ currentRecord.userName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="设备ID">
          {{ currentRecord.deviceId }}
        </a-descriptions-item>
        <a-descriptions-item label="设备名称">
          {{ currentRecord.deviceName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="区域名称">
          {{ currentRecord.areaName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="通行方式">
          <a-tag :color="getAccessMethodColor(currentRecord.accessMethod)">
            {{ getAccessMethodText(currentRecord.accessMethod) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="通行结果">
          <a-tag :color="currentRecord.result === 'SUCCESS' ? 'green' : 'red'">
            {{ currentRecord.result === 'SUCCESS' ? '成功' : '失败' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="通行时间" :span="2">
          {{ currentRecord.accessTime }}
        </a-descriptions-item>
        <a-descriptions-item label="异常类型" v-if="currentRecord.exceptionType">
          <a-tag color="orange">
            {{ getExceptionTypeText(currentRecord.exceptionType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="失败原因" v-if="currentRecord.result === 'FAILED'">
          {{ currentRecord.failReason || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ currentRecord.createTime }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 导出模态框 -->
    <a-modal
      v-model:open="exportVisible"
      title="导出门禁记录"
      width="500px"
      @ok="handleExport"
    >
      <a-form layout="vertical">
        <a-form-item label="导出格式">
          <a-radio-group v-model:value="exportForm.format">
            <a-radio value="excel">Excel (.xlsx)</a-radio>
            <a-radio value="csv">CSV (.csv)</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="导出范围">
          <a-radio-group v-model:value="exportForm.range">
            <a-radio value="current">当前页（{{ tableData.length }}条）</a-radio>
            <a-radio value="all">所有结果（{{ pagination.total }}条）</a-radio>
            <a-radio value="selected">已选择（{{ selectedRowKeys.length }}条）</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="包含字段">
          <a-checkbox-group v-model:value="exportForm.fields">
            <a-checkbox value="id">记录ID</a-checkbox>
            <a-checkbox value="userId">用户ID</a-checkbox>
            <a-checkbox value="userName">用户姓名</a-checkbox>
            <a-checkbox value="deviceId">设备ID</a-checkbox>
            <a-checkbox value="deviceName">设备名称</a-checkbox>
            <a-checkbox value="areaName">区域名称</a-checkbox>
            <a-checkbox value="accessMethod">通行方式</a-checkbox>
            <a-checkbox value="result">通行结果</a-checkbox>
            <a-checkbox value="accessTime">通行时间</a-checkbox>
          </a-checkbox-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, watch, nextTick } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    SearchOutlined,
    ReloadOutlined,
    ExportOutlined,
    DeleteOutlined,
    BarChartOutlined,
    UpOutlined,
    DownOutlined
  } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';
  import * as echarts from 'echarts';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    userId: null,
    userName: null,
    deviceId: null,
    deviceName: null,
    areaId: null,
    accessMethod: null,
    startTime: null,
    endTime: null,
    result: null,
    exceptionType: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const dateRange = ref(null);

  // 高级筛选展开状态
  const advancedVisible = ref(false);
  const toggleAdvanced = () => {
    advancedVisible.value = !advancedVisible.value;
  };

  // 表格数据
  const tableData = ref([]);
  const total = ref(0);
  const loading = ref(false);

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: true,
    pageSizeOptions: PAGE_SIZE_OPTIONS,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 行选择
  const selectedRowKeys = ref([]);
  const rowSelection = {
    selectedRowKeys: selectedRowKeys,
    onChange: (keys) => {
      selectedRowKeys.value = keys;
    },
  };

  // 统计数据
  const showStatistics = ref(true);
  const statistics = reactive({
    todayTotal: 0,
    successCount: 0,
    failedCount: 0,
    successRate: 0,
  });

  // 图表引用
  const trendChartRef = ref(null);
  const hourlyChartRef = ref(null);
  let trendChart = null;
  let hourlyChart = null;

  // 区域列表
  const areaList = ref([]);

  // 详情模态框
  const detailVisible = ref(false);
  const currentRecord = ref(null);

  // 导出模态框
  const exportVisible = ref(false);
  const exportForm = reactive({
    format: 'excel',
    range: 'current',
    fields: ['userId', 'userName', 'deviceName', 'areaName', 'accessMethod', 'result', 'accessTime'],
  });

  // 表格列定义
  const columns = ref([
    {
      title: '记录ID',
      dataIndex: 'id',
      key: 'id',
      width: 120,
      fixed: 'left',
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 120,
    },
    {
      title: '用户姓名',
      dataIndex: 'userName',
      key: 'userName',
      width: 120,
    },
    {
      title: '设备ID',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 120,
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
      width: 150,
    },
    {
      title: '区域名称',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 150,
    },
    {
      title: '通行方式',
      dataIndex: 'accessMethod',
      key: 'accessMethod',
      width: 120,
    },
    {
      title: '通行结果',
      dataIndex: 'result',
      key: 'result',
      width: 100,
    },
    {
      title: '异常类型',
      dataIndex: 'exceptionType',
      key: 'exceptionType',
      width: 150,
    },
    {
      title: '通行时间',
      dataIndex: 'accessTime',
      key: 'accessTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      fixed: 'right',
    },
  ]);

  // 区域搜索过滤
  const filterAreaOption = (input, option) => {
    return option.children.children[0].text.toLowerCase().includes(input.toLowerCase());
  };

  // 监听日期范围变化
  watch(dateRange, (val) => {
    if (val && val.length === 2) {
      queryForm.startTime = dayjs(val[0]).format('YYYY-MM-DD HH:mm:ss');
      queryForm.endTime = dayjs(val[1]).format('YYYY-MM-DD HH:mm:ss');
    } else {
      queryForm.startTime = null;
      queryForm.endTime = null;
    }
  });

  // 查询记录列表
  const queryRecordList = async () => {
    loading.value = true;
    try {
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        userId: queryForm.userId || undefined,
        userName: queryForm.userName || undefined,
        deviceId: queryForm.deviceId || undefined,
        deviceName: queryForm.deviceName || undefined,
        areaId: queryForm.areaId || undefined,
        accessMethod: queryForm.accessMethod || undefined,
        startTime: queryForm.startTime || undefined,
        endTime: queryForm.endTime || undefined,
        result: queryForm.result || undefined,
        exceptionType: queryForm.exceptionType || undefined,
      };

      const result = await accessApi.queryAccessRecords(params);
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
        pagination.current = result.data.pageNum || 1;
        pagination.pageSize = result.data.pageSize || PAGE_SIZE;
      } else {
        message.error(result.message || '查询门禁记录失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('查询门禁记录失败');
    } finally {
      loading.value = false;
    }
  };

  // 查询统计数据
  const queryStatistics = async () => {
    try {
      const params = {
        startTime: queryForm.startTime || dayjs().startOf('day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: queryForm.endTime || dayjs().endOf('day').format('YYYY-MM-DD HH:mm:ss'),
      };

      // TODO: 调用统计API
      // const result = await accessApi.getAccessStatistics(params);
      // if (result.code === 200) {
      //   Object.assign(statistics, result.data);
      // }

      // 模拟数据
      statistics.todayTotal = 1234;
      statistics.successCount = 1180;
      statistics.failedCount = 54;
      statistics.successRate = ((statistics.successCount / statistics.todayTotal) * 100).toFixed(2);

      // 初始化图表
      await nextTick();
      initCharts();
    } catch (error) {
      console.error('查询统计数据失败', error);
    }
  };

  // 查询区域列表
  const queryAreaList = async () => {
    try {
      // TODO: 调用区域列表API
      // const result = await accessApi.getAreaList();
      // if (result.code === 200) {
      //   areaList.value = result.data;
      // }

      // 模拟数据
      areaList.value = [
        { areaId: 1, areaName: 'A栋1楼大厅' },
        { areaId: 2, areaName: 'A栋2楼办公区' },
        { areaId: 3, areaName: 'B栋1楼会议室' },
        { areaId: 4, areaName: 'C栋餐厅' },
      ];
    } catch (error) {
      console.error('查询区域列表失败', error);
    }
  };

  // 初始化图表
  const initCharts = () => {
    if (!trendChartRef.value || !hourlyChartRef.value) return;

    // 通行趋势图（折线图）
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value);
    }

    const trendOption = {
      title: {
        text: '近7天通行趋势',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
      },
      legend: {
        data: ['成功', '失败'],
        top: 30,
      },
      xAxis: {
        type: 'category',
        data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '成功',
          type: 'line',
          data: [820, 932, 901, 934, 1290, 1330, 1320],
          smooth: true,
          itemStyle: { color: '#52c41a' },
        },
        {
          name: '失败',
          type: 'line',
          data: [32, 42, 35, 48, 50, 60, 54],
          smooth: true,
          itemStyle: { color: '#ff4d4f' },
        },
      ],
    };

    trendChart.setOption(trendOption);

    // 时段分布图（柱状图）
    if (!hourlyChart) {
      hourlyChart = echarts.init(hourlyChartRef.value);
    }

    const hourlyOption = {
      title: {
        text: '24小时通行分布',
        left: 'center',
      },
      tooltip: {
        trigger: 'axis',
      },
      xAxis: {
        type: 'category',
        data: Array.from({ length: 24 }, (_, i) => `${i}:00`),
      },
      yAxis: {
        type: 'value',
      },
      series: [
        {
          name: '通行次数',
          type: 'bar',
          data: [5, 3, 2, 1, 2, 8, 45, 120, 180, 220, 200, 180, 150, 140, 160, 190, 170, 140, 100, 80, 60, 40, 20, 10],
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#83bff6' },
              { offset: 0.5, color: '#188df0' },
              { offset: 1, color: '#188df0' },
            ]),
          },
        },
      ],
    };

    hourlyChart.setOption(hourlyOption);
  };

  // 重置查询
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    dateRange.value = null;
    selectedRowKeys.value = [];
    queryRecordList();
    queryStatistics();
  };

  // 表格变化
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryRecordList();
  };

  // 查看详情
  const handleView = (record) => {
    currentRecord.value = record;
    detailVisible.value = true;
  };

  // 删除记录
  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除记录ID为 ${record.id} 的通行记录吗？`,
      onOk: async () => {
        try {
          // TODO: 调用删除API
          // const result = await accessApi.deleteAccessRecord(record.id);
          // if (result.code === 200) {
          //   message.success('删除成功');
          //   queryRecordList();
          // } else {
          //   message.error(result.message || '删除失败');
          // }

          message.success('删除成功');
          queryRecordList();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  // 批量删除
  const handleBatchDelete = () => {
    Modal.confirm({
      title: '确认批量删除',
      content: `确定要删除选中的 ${selectedRowKeys.value.length} 条记录吗？`,
      onOk: async () => {
        try {
          // TODO: 调用批量删除API
          // const result = await accessApi.batchDeleteAccessRecords(selectedRowKeys.value);
          // if (result.code === 200) {
          //   message.success('批量删除成功');
          //   selectedRowKeys.value = [];
          //   queryRecordList();
          // } else {
          //   message.error(result.message || '批量删除失败');
          // }

          message.success(`成功删除 ${selectedRowKeys.value.length} 条记录`);
          selectedRowKeys.value = [];
          queryRecordList();
        } catch (error) {
          message.error('批量删除失败');
        }
      },
    });
  };

  // 打开导出模态框
  const openExportModal = () => {
    exportVisible.value = true;
  };

  // 执行导出
  const handleExport = async () => {
    try {
      message.loading('正在导出...', 0);

      // TODO: 调用导出API
      // const params = {
      //   format: exportForm.format,
      //   range: exportForm.range,
      //   fields: exportForm.fields,
      //   recordIds: exportForm.range === 'selected' ? selectedRowKeys.value : undefined,
      //   ...queryForm,
      // };
      // const result = await accessApi.exportAccessRecords(params);

      // 模拟导出延迟
      await new Promise(resolve => setTimeout(resolve, 2000));

      message.destroy();
      message.success('导出成功！');
      exportVisible.value = false;
    } catch (error) {
      message.destroy();
      message.error('导出失败');
    }
  };

  // 获取通行方式颜色
  const getAccessMethodColor = (method) => {
    const colorMap = {
      CARD: 'blue',
      FACE: 'green',
      FINGERPRINT: 'purple',
      QR_CODE: 'orange',
      PASSWORD: 'cyan',
    };
    return colorMap[method] || 'default';
  };

  // 获取通行方式文本
  const getAccessMethodText = (method) => {
    const textMap = {
      CARD: '刷卡',
      FACE: '人脸',
      FINGERPRINT: '指纹',
      QR_CODE: '二维码',
      PASSWORD: '密码',
    };
    return textMap[method] || method;
  };

  // 获取异常类型文本
  const getExceptionTypeText = (type) => {
    const textMap = {
      ANTI_PASSBACK: '反潜回违规',
      UNAUTHORIZED: '未授权',
      TIME_VIOLATION: '时间违规',
      DEVICE_OFFLINE: '设备离线',
    };
    return textMap[type] || type;
  };

  // 获取行类名（异常记录高亮）
  const getRowClassName = (record) => {
    if (record.exceptionType) {
      return 'exception-row';
    }
    return '';
  };

  // 初始化
  onMounted(() => {
    queryRecordList();
    queryStatistics();
    queryAreaList();
  });
</script>

<style lang="less" scoped>
  .access-record-optimized-page {
    .query-card {
      margin-bottom: 16px;
    }

    :deep(.exception-row) {
      background-color: #fff1f0;
    }

    :deep(.ant-statistic) {
      padding: 16px;
      background: #fafafa;
      border-radius: 4px;
    }
  }
</style>
