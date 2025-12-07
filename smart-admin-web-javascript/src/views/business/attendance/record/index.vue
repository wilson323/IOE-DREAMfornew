<!--
  * 考勤记录查询
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="attendance-record-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
          <a-form-item label="员工ID" class="smart-query-form-item">
            <a-input-number
              v-model:value="queryForm.employeeId"
              placeholder="请输入员工ID"
              style="width: 150px"
              :min="1"
            />
          </a-form-item>

          <a-form-item label="考勤日期" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              style="width: 240px"
            />
          </a-form-item>

          <a-form-item label="考勤状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择"
            >
              <a-select-option value="NORMAL">正常</a-select-option>
              <a-select-option value="LATE">迟到</a-select-option>
              <a-select-option value="EARLY_LEAVE">早退</a-select-option>
              <a-select-option value="ABSENT">缺勤</a-select-option>
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
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>考勤记录</span>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'workDuration'">
            {{ formatDuration(record.workDuration) }}
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, watch } from 'vue';
  import { message } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { attendanceApi } from '/@/api/business/attendance/attendance-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    employeeId: null,
    startDate: null,
    endDate: null,
    status: null,
  };
  const queryForm = reactive({ ...queryFormState });
  const dateRange = ref(null);

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

  // 表格列定义
  const columns = ref([
    {
      title: '记录ID',
      dataIndex: 'recordId',
      key: 'recordId',
      width: 120,
    },
    {
      title: '员工ID',
      dataIndex: 'employeeId',
      key: 'employeeId',
      width: 120,
    },
    {
      title: '员工姓名',
      dataIndex: 'employeeName',
      key: 'employeeName',
      width: 120,
    },
    {
      title: '日期',
      dataIndex: 'date',
      key: 'date',
      width: 120,
    },
    {
      title: '上班打卡',
      dataIndex: 'checkInTime',
      key: 'checkInTime',
      width: 150,
    },
    {
      title: '下班打卡',
      dataIndex: 'checkOutTime',
      key: 'checkOutTime',
      width: 150,
    },
    {
      title: '工作时长',
      dataIndex: 'workDuration',
      key: 'workDuration',
      width: 120,
    },
    {
      title: '考勤状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
    },
  ]);

  // 监听日期范围变化
  watch(dateRange, (val) => {
    if (val && val.length === 2) {
      queryForm.startDate = dayjs(val[0]).format('YYYY-MM-DD');
      queryForm.endDate = dayjs(val[1]).format('YYYY-MM-DD');
    } else {
      queryForm.startDate = null;
      queryForm.endDate = null;
    }
  });

  // 查询记录列表
  const queryRecordList = async () => {
    loading.value = true;
    try {
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        employeeId: queryForm.employeeId || undefined,
        startDate: queryForm.startDate || undefined,
        endDate: queryForm.endDate || undefined,
        status: queryForm.status || undefined,
      };

      const result = await attendanceApi.queryAttendanceRecords(params);
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
        pagination.current = result.data.pageNum || 1;
        pagination.pageSize = result.data.pageSize || PAGE_SIZE;
      } else {
        message.error(result.message || '查询考勤记录失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('查询考勤记录失败');
    } finally {
      loading.value = false;
    }
  };

  // 重置查询
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    dateRange.value = null;
    queryRecordList();
  };

  // 表格变化
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryRecordList();
  };

  // 获取状态颜色
  const getStatusColor = (status) => {
    const colorMap = {
      NORMAL: 'green',
      LATE: 'orange',
      EARLY_LEAVE: 'orange',
      ABSENT: 'red',
    };
    return colorMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      NORMAL: '正常',
      LATE: '迟到',
      EARLY_LEAVE: '早退',
      ABSENT: '缺勤',
    };
    return textMap[status] || '未知';
  };

  // 格式化工作时长
  const formatDuration = (minutes) => {
    if (!minutes) return '-';
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return `${hours}小时${mins}分钟`;
  };

  // 初始化
  onMounted(() => {
    queryRecordList();
  });
</script>

<style lang="less" scoped>
  .attendance-record-page {
    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

