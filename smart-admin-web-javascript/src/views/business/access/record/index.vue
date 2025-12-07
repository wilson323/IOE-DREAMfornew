<!--
  * 门禁记录查询
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-record-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
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

          <a-form-item label="通行时间" class="smart-query-form-item">
            <a-range-picker
              v-model:value="dateRange"
              format="YYYY-MM-DD"
              style="width: 240px"
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
            </a-space>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 表格区域 -->
    <a-card :bordered="false" style="margin-top: 16px">
      <template #title>
        <a-space>
          <span>门禁记录</span>
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
          <template v-if="column.key === 'result'">
            <a-tag :color="record.result === 'SUCCESS' ? 'green' : 'red'">
              {{ record.result === 'SUCCESS' ? '成功' : '失败' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
            </a-space>
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
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    userId: null,
    deviceId: null,
    startTime: null,
    endTime: null,
    result: null,
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
      dataIndex: 'id',
      key: 'id',
      width: 120,
    },
    {
      title: '用户ID',
      dataIndex: 'userId',
      key: 'userId',
      width: 120,
    },
    {
      title: '设备ID',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 120,
    },
    {
      title: '通行结果',
      dataIndex: 'result',
      key: 'result',
      width: 100,
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
      width: 100,
      fixed: 'right',
    },
  ]);

  // 监听日期范围变化
  watch(dateRange, (val) => {
    if (val && val.length === 2) {
      queryForm.startTime = dayjs(val[0]).format('YYYY-MM-DD 00:00:00');
      queryForm.endTime = dayjs(val[1]).format('YYYY-MM-DD 23:59:59');
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
        deviceId: queryForm.deviceId || undefined,
        startTime: queryForm.startTime || undefined,
        endTime: queryForm.endTime || undefined,
        result: queryForm.result || undefined,
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

  // 查看详情
  const handleView = (record) => {
    message.info('查看详情功能开发中');
  };

  // 初始化
  onMounted(() => {
    queryRecordList();
  });
</script>

<style lang="less" scoped>
  .access-record-page {
    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

