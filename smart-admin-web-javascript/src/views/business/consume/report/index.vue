<!--
  * 消费报表管理
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="report-management-page">
    <!-- 报表模板选择 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <template #title>
        <a-space>
          <span>报表模板</span>
        </a-space>
      </template>
      <a-radio-group v-model:value="selectedTemplateId" @change="handleTemplateChange">
        <a-radio-button
          v-for="template in reportTemplates"
          :key="template.id"
          :value="template.id"
        >
          {{ template.name }}
        </a-radio-button>
      </a-radio-group>
    </a-card>

    <!-- 报表参数配置 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <template #title>
        <a-space>
          <span>报表参数</span>
        </a-space>
      </template>
      <a-form
        ref="formRef"
        :model="reportParams"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="开始时间" name="startTime">
              <a-date-picker
                v-model:value="reportParams.startTime"
                format="YYYY-MM-DD"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="结束时间" name="endTime">
              <a-date-picker
                v-model:value="reportParams.endTime"
                format="YYYY-MM-DD"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="区域ID" name="areaId">
              <a-input
                v-model:value="reportParams.areaId"
                placeholder="请输入区域ID（可选）"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="用户ID" name="userId">
              <a-input-number
                v-model:value="reportParams.userId"
                placeholder="请输入用户ID（可选）"
                style="width: 100%"
                :min="1"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="generateReport" :loading="generating">
              <template #icon><FileTextOutlined /></template>
              生成报表
            </a-button>
            <a-button @click="exportReport('EXCEL')" :loading="exporting" :disabled="!reportData">
              <template #icon><DownloadOutlined /></template>
              导出Excel
            </a-button>
            <a-button @click="exportReport('PDF')" :loading="exporting" :disabled="!reportData">
              <template #icon><FilePdfOutlined /></template>
              导出PDF
            </a-button>
            <a-button @click="exportReport('CSV')" :loading="exporting" :disabled="!reportData">
              <template #icon><FileOutlined /></template>
              导出CSV
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 报表数据展示 -->
    <a-card :bordered="false" v-if="reportData">
      <template #title>
        <a-space>
          <span>报表数据</span>
        </a-space>
      </template>
      <a-table
        :columns="reportColumns"
        :data-source="reportData.list"
        :pagination="false"
        :loading="loading"
        row-key="id"
      />
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message } from 'ant-design-vue';
  import {
    FileTextOutlined,
    DownloadOutlined,
    FilePdfOutlined,
    FileOutlined,
  } from '@ant-design/icons-vue';
  import { consumeApi } from '/@/api/business/consume/consume-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import dayjs from 'dayjs';

  const formRef = ref();
  const loading = ref(false);
  const generating = ref(false);
  const exporting = ref(false);

  // 报表模板
  const reportTemplates = ref([
    { id: 1, name: '日消费报表', type: 'DAILY' },
    { id: 2, name: '月消费报表', type: 'MONTHLY' },
    { id: 3, name: '年消费报表', type: 'YEARLY' },
    { id: 4, name: '区域消费报表', type: 'AREA' },
  ]);
  const selectedTemplateId = ref(1);

  // 报表参数
  const reportParams = reactive({
    startTime: dayjs().subtract(7, 'day'),
    endTime: dayjs(),
    areaId: null,
    userId: null,
  });

  // 报表数据
  const reportData = ref(null);

  // 报表列定义
  const reportColumns = ref([
    { title: '日期', dataIndex: 'date', key: 'date', width: 120 },
    { title: '交易笔数', dataIndex: 'count', key: 'count', width: 120, align: 'right' },
    { title: '交易金额', dataIndex: 'amount', key: 'amount', width: 120, align: 'right' },
    { title: '用户数', dataIndex: 'userCount', key: 'userCount', width: 100, align: 'right' },
  ]);

  // 模板变化
  const handleTemplateChange = () => {
    reportData.value = null;
  };

  // 生成报表
  const generateReport = async () => {
    try {
      generating.value = true;
      const filters = {};
      if (reportParams.areaId) {
        filters.areaId = reportParams.areaId;
      }
      if (reportParams.userId) {
        filters.userId = reportParams.userId;
      }
      const params = {
        startTime: reportParams.startTime?.startOf('day').format('YYYY-MM-DD[T]HH:mm:ss'),
        endTime: reportParams.endTime?.endOf('day').format('YYYY-MM-DD[T]HH:mm:ss'),
        filters,
      };

      const result = await consumeApi.generateReport(selectedTemplateId.value, params);
      if (result.code === 200 && result.data) {
        reportData.value = result.data;
        message.success('报表生成成功');
      } else {
        message.error(result.message || '报表生成失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('报表生成失败');
    } finally {
      generating.value = false;
    }
  };

  // 导出报表
  const exportReport = async (format) => {
    if (!reportData.value) {
      message.warning('请先生成报表');
      return;
    }

    try {
      exporting.value = true;
      const filters = {};
      if (reportParams.areaId) {
        filters.areaId = reportParams.areaId;
      }
      if (reportParams.userId) {
        filters.userId = reportParams.userId;
      }
      const params = {
        startTime: reportParams.startTime?.startOf('day').format('YYYY-MM-DD[T]HH:mm:ss'),
        endTime: reportParams.endTime?.endOf('day').format('YYYY-MM-DD[T]HH:mm:ss'),
        filters,
      };

      const result = await consumeApi.exportReport(selectedTemplateId.value, params, format);
      if (result.code === 200 && result.data) {
        message.success('报表导出成功');
        // 文件下载由后端返回的URL自动触发
      } else {
        message.error(result.message || '报表导出失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('报表导出失败');
    } finally {
      exporting.value = false;
    }
  };

  // 初始化
  onMounted(() => {
    // 加载报表模板列表
    consumeApi.getReportTemplates()
      .then((result) => {
        if (result.code === 200 && Array.isArray(result.data) && result.data.length > 0) {
          reportTemplates.value = result.data;
          selectedTemplateId.value = result.data[0].id;
        }
      })
      .catch((error) => {
        smartSentry.captureError(error);
      });
  });
</script>

<style lang="less" scoped>
  .report-management-page {
  }
</style>

