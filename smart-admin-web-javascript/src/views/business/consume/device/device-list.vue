<!--
  * 设备管理页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM
-->
<template>
  <div class="device-list-page">
    <a-card :bordered="false">
      <template #title>
        <span>设备管理</span>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增设备
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
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
              :status="record.status === 1 ? 'success' : 'error'"
              :text="record.status === 1 ? '在线' : '离线'"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                编辑
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">
                删除
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    ReloadOutlined,
  } from '@ant-design/icons-vue';

  // 表格数据
  const tableData = ref([]);
  const loading = ref(false);

  // 分页配置
  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 0,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  // 表格列配置
  const columns = [
    {
      title: '设备编号',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
    },
    {
      title: '安装位置',
      dataIndex: 'location',
      key: 'location',
    },
    {
      title: '状态',
      key: 'status',
      width: 80,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
    },
  ];

  // 加载设备列表
  const loadDeviceList = async () => {
    loading.value = true;
    try {
      // TODO: 调用API获取设备列表
      // const result = await deviceApi.getDeviceList();
      tableData.value = [];
      pagination.total = 0;
    } catch (error) {
      message.error('加载设备列表失败');
    } finally {
      loading.value = false;
    }
  };

  // 表格变化事件
  const handleTableChange = (pag) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
    loadDeviceList();
  };

  // 新增设备
  const handleAdd = () => {
    message.info('新增设备功能开发中');
  };

  // 查看设备
  const handleView = (record) => {
    Modal.info({
      title: '设备详情',
      content: `设备编号: ${record.deviceCode}`,
    });
  };

  // 编辑设备
  const handleEdit = (record) => {
    message.info('编辑设备功能开发中');
  };

  // 删除设备
  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除设备 ${record.deviceName} 吗？`,
      onOk: async () => {
        try {
          // TODO: 调用删除API
          message.success('删除成功');
          loadDeviceList();
        } catch (error) {
          message.error('删除失败');
        }
      },
    });
  };

  // 刷新列表
  const handleRefresh = () => {
    loadDeviceList();
  };

  // 组件挂载
  onMounted(() => {
    loadDeviceList();
  });
</script>

<style lang="less" scoped>
  .device-list-page {
    .ant-badge {
      margin-right: 8px;
    }
  }
</style>