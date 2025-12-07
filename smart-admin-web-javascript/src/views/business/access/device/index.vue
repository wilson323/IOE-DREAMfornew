<!--
  * 门禁设备管理
  * 
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="access-device-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" layout="inline">
        <a-row class="smart-query-form-row">
          <a-form-item label="关键词" class="smart-query-form-item">
            <a-input
              style="width: 200px"
              v-model:value="queryForm.keyword"
              placeholder="设备名称、设备编号"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="区域ID" class="smart-query-form-item">
            <a-input
              v-model:value="queryForm.areaId"
              placeholder="请输入区域ID"
              style="width: 150px"
            />
          </a-form-item>

          <a-form-item label="设备状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option :value="1">在线</a-select-option>
              <a-select-option :value="2">离线</a-select-option>
              <a-select-option :value="3">故障</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-space>
              <a-button type="primary" @click="queryDeviceList">
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
          <span>设备列表</span>
        </a-space>
      </template>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            添加设备
          </a-button>
        </a-space>
      </template>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        row-key="deviceId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-badge
              :status="getStatusBadge(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">详情</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-dropdown>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleStatusUpdate(record, 1)" v-if="record.status !== 1">
                      设为在线
                    </a-menu-item>
                    <a-menu-item @click="handleStatusUpdate(record, 2)" v-if="record.status !== 2">
                      设为离线
                    </a-menu-item>
                    <a-menu-item @click="handleStatusUpdate(record, 3)" v-if="record.status !== 3">
                      设为故障
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleDelete(record)" danger>
                      删除设备
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 设备表单弹窗 -->
    <DeviceFormModal
      v-model:visible="formModalVisible"
      :form-data="currentDevice"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />

    <!-- 设备详情弹窗 -->
    <DeviceDetailModal
      v-model:visible="detailModalVisible"
      :device-id="currentDeviceId"
    />
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined, PlusOutlined, DownOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { accessApi } from '/@/api/business/access/access-api';
  import { smartSentry } from '/@/lib/smart-sentry';
  import DeviceFormModal from './components/DeviceFormModal.vue';
  import DeviceDetailModal from './components/DeviceDetailModal.vue';

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    keyword: null,
    areaId: null,
    status: null,
  };
  const queryForm = reactive({ ...queryFormState });

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
      title: '设备ID',
      dataIndex: 'deviceId',
      key: 'deviceId',
      width: 120,
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
      width: 200,
    },
    {
      title: '设备编号',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
      width: 150,
    },
    {
      title: '区域ID',
      dataIndex: 'areaId',
      key: 'areaId',
      width: 120,
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 120,
    },
    {
      title: '设备状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
    },
  ]);

  // 弹窗相关
  const formModalVisible = ref(false);
  const detailModalVisible = ref(false);
  const isEdit = ref(false);
  const currentDevice = ref(null);
  const currentDeviceId = ref(null);

  // 查询设备列表
  const queryDeviceList = async () => {
    loading.value = true;
    try {
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        keyword: queryForm.keyword || undefined,
        areaId: queryForm.areaId || undefined,
        status: queryForm.status || undefined,
      };

      const result = await accessApi.queryDevices(params);
      if (result.code === 200 && result.data) {
        tableData.value = result.data.list || [];
        pagination.total = result.data.total || 0;
        pagination.current = result.data.pageNum || 1;
        pagination.pageSize = result.data.pageSize || PAGE_SIZE;
      } else {
        message.error(result.message || '查询设备列表失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('查询设备列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 重置查询
  const resetQuery = () => {
    Object.assign(queryForm, queryFormState);
    queryDeviceList();
  };

  // 表格变化
  const handleTableChange = (pag) => {
    queryForm.pageNum = pag.current;
    queryForm.pageSize = pag.pageSize;
    queryDeviceList();
  };

  // 添加设备
  const handleAdd = () => {
    isEdit.value = false;
    currentDevice.value = null;
    formModalVisible.value = true;
  };

  // 编辑设备
  const handleEdit = (record) => {
    isEdit.value = true;
    currentDevice.value = { ...record };
    formModalVisible.value = true;
  };

  // 查看详情
  const handleView = (record) => {
    currentDeviceId.value = record.deviceId;
    detailModalVisible.value = true;
  };

  // 更新设备状态
  const handleStatusUpdate = async (record, status) => {
    try {
      const result = await accessApi.updateDeviceStatus({
        deviceId: record.deviceId,
        status: status,
      });
      if (result.code === 200) {
        message.success('状态更新成功');
        queryDeviceList();
      } else {
        message.error(result.message || '状态更新失败');
      }
    } catch (error) {
      smartSentry.captureError(error);
      message.error('状态更新失败');
    }
  };

  // 删除设备
  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除设备 "${record.deviceName}" 吗？`,
      onOk: async () => {
        try {
          const result = await accessApi.deleteDevice(record.deviceId);
          if (result.code === 200) {
            message.success('删除成功');
            queryDeviceList();
          } else {
            message.error(result.message || '删除失败');
          }
        } catch (error) {
          smartSentry.captureError(error);
          message.error('删除失败');
        }
      },
    });
  };

  // 表单成功回调
  const handleFormSuccess = () => {
    queryDeviceList();
  };

  // 获取状态Badge
  const getStatusBadge = (status) => {
    const badgeMap = {
      1: 'success',
      2: 'default',
      3: 'error',
    };
    return badgeMap[status] || 'default';
  };

  // 获取状态文本
  const getStatusText = (status) => {
    const textMap = {
      1: '在线',
      2: '离线',
      3: '故障',
    };
    return textMap[status] || '未知';
  };

  // 初始化
  onMounted(() => {
    queryDeviceList();
  });
</script>

<style lang="less" scoped>
  .access-device-page {
    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

