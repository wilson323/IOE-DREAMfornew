<!--
 * 设备选择弹窗
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <a-modal
    :visible="visible"
    title="选择设备"
    :width="1000"
    @ok="handleConfirm"
    @cancel="handleCancel"
    :mask-closable="false"
  >
    <!-- 查询表单 -->
    <a-form layout="inline" style="margin-bottom: 16px">
      <a-form-item label="关键词">
        <a-input
          style="width: 200px"
          v-model:value="queryForm.keyword"
          placeholder="设备名称、设备编号"
          allow-clear
          @pressEnter="queryDeviceList"
        />
      </a-form-item>

      <a-form-item label="设备类型">
        <a-select
          v-model:value="queryForm.deviceType"
          style="width: 150px"
          :allow-clear="true"
          placeholder="请选择设备类型"
          @change="queryDeviceList"
        >
          <a-select-option :value="1">门禁设备</a-select-option>
          <a-select-option :value="2">考勤设备</a-select-option>
          <a-select-option :value="3">消费设备</a-select-option>
          <a-select-option :value="4">视频设备</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item label="区域">
        <a-tree-select
          v-model:value="queryForm.areaId"
          style="width: 200px"
          :tree-data="areaTreeData"
          placeholder="请选择区域"
          tree-default-expand-all
          allow-clear
          @change="queryDeviceList"
        />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" @click="queryDeviceList">
            <template #icon><SearchOutlined /></template>
            查询
          </a-button>
          <a-button @click="resetQuery">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <!-- 批量操作 -->
    <a-space style="margin-bottom: 16px">
      <span>已选择 {{ selectedDeviceIds.size }} 台设备</span>
      <a-button size="small" @click="selectAllDevices" v-if="deviceList.length > 0">
        全选当前页
      </a-button>
      <a-button size="small" @click="clearSelection">清空选择</a-button>
    </a-space>

    <!-- 设备列表 -->
    <a-table
      :columns="columns"
      :data-source="deviceList"
      :pagination="pagination"
      :loading="loading"
      row-key="deviceId"
      :row-selection="{
        selectedRowKeys: selectedDeviceKeys,
        onChange: handleSelectionChange
      }"
      :scroll="{ y: 400 }"
      size="small"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'deviceStatus'">
          <a-badge
            :status="getStatusBadge(record.deviceStatus)"
            :text="getStatusText(record.deviceStatus)"
          />
        </template>
        <template v-else-if="column.key === 'firmwareVersion'">
          {{ record.firmwareVersion || '未知' }}
        </template>
      </template>
    </a-table>
  </a-modal>
</template>

<script setup>
  import { reactive, ref, watch, computed } from 'vue';
  import { message } from 'ant-design-vue';
  import { SearchOutlined } from '@ant-design/icons-vue';
  import { accessApi } from '/@/api/business/access/access-api';
  import { PAGE_SIZE } from '/@/constants/common-const';

  const props = defineProps({
    visible: {
      type: Boolean,
      default: false
    },
    // 预选设备ID列表
    preselectedDeviceIds: {
      type: Array,
      default: () => []
    }
  });

  const emit = defineEmits(['update:visible', 'confirm']);

  // 状态
  const queryForm = reactive({
    keyword: '',
    deviceType: undefined,
    areaId: undefined
  });

  const deviceList = ref([]);
  const loading = ref(false);
  const pagination = reactive({
    current: 1,
    pageSize: PAGE_SIZE,
    total: 0,
    showSizeChanger: false,
    showTotal: (total) => `共 ${total} 条`
  });

  const selectedDeviceKeys = ref([]);
  const selectedDeviceIdsSet = ref(new Set());
  const selectedDevicesData = ref([]);

  const areaTreeData = ref([
    {
      title: '全部区域',
      value: 'all',
      key: 'all',
      children: [
        { title: 'A栋', value: '1', key: '1' },
        { title: 'B栋', value: '2', key: '2' },
        { title: 'C栋', value: '3', key: '3' }
      ]
    }
  ]);

  const columns = [
    {
      title: '设备编号',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
      width: 150
    },
    {
      title: '设备名称',
      dataIndex: 'deviceName',
      key: 'deviceName',
      ellipsis: true
    },
    {
      title: '设备类型',
      dataIndex: 'deviceType',
      key: 'deviceType',
      width: 100,
      customRender: ({ record }) => getDeviceTypeText(record.deviceType)
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 120
    },
    {
      title: '所在区域',
      dataIndex: 'areaName',
      key: 'areaName',
      width: 120
    },
    {
      title: '当前固件',
      dataIndex: 'firmwareVersion',
      key: 'firmwareVersion',
      width: 100
    },
    {
      title: '设备状态',
      dataIndex: 'deviceStatus',
      key: 'deviceStatus',
      width: 100
    }
  ];

  // 监听visible变化
  watch(() => props.visible, (newVal) => {
    if (newVal) {
      // 打开弹窗时加载设备列表
      selectedDeviceKeys.value = [...props.preselectedDeviceIds];
      selectedDeviceIdsSet.value = new Set(props.preselectedDeviceIds);
      queryDeviceList();
    }
  });

  // 计算属性
  const selectedDeviceIds = computed(() => Array.from(selectedDeviceIdsSet.value));

  // 方法
  async function queryDeviceList() {
    try {
      loading.value = true;
      const params = {
        ...queryForm,
        pageNum: pagination.current,
        pageSize: pagination.pageSize
      };

      const response = await accessApi.queryDevicePage(params);
      if (response.code === 200) {
        deviceList.value = response.data.list || [];
        pagination.total = response.data.total || 0;
      } else {
        message.error(response.message || '查询设备列表失败');
      }
    } catch (e) {
      console.error('查询设备列表异常:', e);
      message.error('查询设备列表异常');
    } finally {
      loading.value = false;
    }
  }

  function resetQuery() {
    Object.assign(queryForm, {
      keyword: '',
      deviceType: undefined,
      areaId: undefined
    });
    pagination.current = 1;
    queryDeviceList();
  }

  function handleSelectionChange(selectedKeys, selectedRows) {
    selectedDeviceKeys.value = selectedKeys;
    selectedDeviceIdsSet.value = new Set(selectedKeys);

    // 更新选中的设备数据
    selectedDevicesData.value = selectedRows;
  }

  function selectAllDevices() {
    const allKeys = deviceList.value.map(d => d.deviceId);
    selectedDeviceKeys.value = [...new Set([...selectedDeviceKeys.value, ...allKeys])];
    selectedDeviceIdsSet.value = new Set(selectedDeviceKeys.value);
  }

  function clearSelection() {
    selectedDeviceKeys.value = [];
    selectedDeviceIdsSet.value.clear();
    selectedDevicesData.value = [];
  }

  function handleConfirm() {
    if (selectedDeviceIdsSet.value.size === 0) {
      message.warning('请至少选择一台设备');
      return;
    }

    const selectedDevices = deviceList.value.filter(d =>
      selectedDeviceIdsSet.value.has(d.deviceId)
    );

    emit('confirm', selectedDevices);
    emit('update:visible', false);
  }

  function handleCancel() {
    emit('update:visible', false);
  }

  function getDeviceTypeText(deviceType) {
    const map = {
      1: '门禁设备',
      2: '考勤设备',
      3: '消费设备',
      4: '视频设备',
      5: '访客设备'
    };
    return map[deviceType] || '未知';
  }

  function getStatusBadge(status) {
    const map = {
      1: 'success',    // 在线
      2: 'default',    // 离线
      3: 'error'       // 故障
    };
    return map[status] || 'default';
  }

  function getStatusText(status) {
    const map = {
      1: '在线',
      2: '离线',
      3: '故障'
    };
    return map[status] || '未知';
  }
</script>

<style lang="less" scoped>
</style>
