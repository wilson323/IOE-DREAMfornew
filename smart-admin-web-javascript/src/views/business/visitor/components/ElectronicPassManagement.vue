<!--
 * 电子出门单管理组件
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="electronic-pass-management">
    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="出门单号">
          <a-input
            v-model:value="searchForm.passNo"
            placeholder="请输入出门单号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="车牌号">
          <a-input
            v-model:value="searchForm.vehicleNumber"
            placeholder="请输入车牌号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="PENDING">待审批</a-select-option>
            <a-select-option value="APPROVED">已批准</a-select-option>
            <a-select-option value="REJECTED">已拒绝</a-select-option>
            <a-select-option value="USED">已使用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
            <a-button type="primary" @click="showAddModal">
              <template #icon><PlusOutlined /></template>
              创建出门单
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 出门单列表 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <a-table
        :columns="passColumns"
        :data-source="passList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'status'">
            <a-tag :color="getStatusColor(record.status)">
              {{ getStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button
                v-if="record.status === 'PENDING'"
                type="link"
                size="small"
                @click="handleApprove(record)"
              >
                审批
              </a-button>
              <a-button
                v-if="record.status === 'APPROVED'"
                type="link"
                size="small"
                @click="handlePrint(record)"
              >
                打印
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 创建/编辑出门单弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="isEdit ? '编辑出门单' : '创建出门单'"
      :width="800"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="车牌号" name="vehicleNumber">
          <a-input
            v-model:value="formData.vehicleNumber"
            placeholder="请输入车牌号"
          />
        </a-form-item>
        <a-form-item label="司机姓名" name="driverName">
          <a-input
            v-model:value="formData.driverName"
            placeholder="请输入司机姓名"
          />
        </a-form-item>
        <a-form-item label="物品清单" name="itemList">
          <a-textarea
            v-model:value="formData.itemList"
            placeholder="请输入物品清单，每行一个物品"
            :rows="5"
          />
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-textarea
            v-model:value="formData.remark"
            placeholder="请输入备注"
            :rows="3"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="出门单详情"
      :width="800"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="出门单号">
          {{ currentRecord.passNo }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRecord.status)">
            {{ getStatusText(currentRecord.status) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="车牌号">
          {{ currentRecord.vehicleNumber }}
        </a-descriptions-item>
        <a-descriptions-item label="司机姓名">
          {{ currentRecord.driverName }}
        </a-descriptions-item>
        <a-descriptions-item label="物品清单" :span="2">
          <div style="white-space: pre-line">{{ currentRecord.itemList }}</div>
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatDateTime(currentRecord.createTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="创建人">
          {{ currentRecord.createUserName }}
        </a-descriptions-item>
        <a-descriptions-item
          v-if="currentRecord.approvalTime"
          label="审批时间"
        >
          {{ formatDateTime(currentRecord.approvalTime) }}
        </a-descriptions-item>
        <a-descriptions-item
          v-if="currentRecord.approvalUserName"
          label="审批人"
        >
          {{ currentRecord.approvalUserName }}
        </a-descriptions-item>
        <a-descriptions-item
          v-if="currentRecord.remark"
          label="备注"
          :span="2"
        >
          {{ currentRecord.remark }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { SearchOutlined, PlusOutlined } from '@ant-design/icons-vue';
import visitorApi from '/@/api/business/visitor/visitor-api';

// 搜索表单
const searchForm = reactive({
  passNo: '',
  vehicleNumber: '',
  status: undefined,
});

// 表格数据
const passList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const passColumns = [
  {
    title: '出门单号',
    dataIndex: 'passNo',
    key: 'passNo',
    width: 180,
  },
  {
    title: '车牌号',
    dataIndex: 'vehicleNumber',
    key: 'vehicleNumber',
    width: 150,
  },
  {
    title: '司机姓名',
    dataIndex: 'driverName',
    key: 'driverName',
    width: 120,
  },
  {
    title: '物品清单',
    dataIndex: 'itemList',
    key: 'itemList',
    ellipsis: true,
  },
  {
    title: '状态',
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
];

// 弹窗
const modalVisible = ref(false);
const isEdit = ref(false);
const formRef = ref();
const formData = reactive({
  id: undefined,
  vehicleNumber: '',
  driverName: '',
  itemList: '',
  remark: '',
});

const formRules = {
  vehicleNumber: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
  ],
  driverName: [
    { required: true, message: '请输入司机姓名', trigger: 'blur' },
  ],
  itemList: [
    { required: true, message: '请输入物品清单', trigger: 'blur' },
  ],
};

// 详情弹窗
const detailModalVisible = ref(false);
const currentRecord = ref(null);

// 查询出门单列表
const queryPassList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    const res = await visitorApi.queryElectronicPasses(params);
    if (res.data) {
      passList.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    message.error('查询出门单列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  queryPassList();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    passNo: '',
    vehicleNumber: '',
    status: undefined,
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryPassList();
};

// 显示添加弹窗
const showAddModal = () => {
  isEdit.value = false;
  modalVisible.value = true;
  Object.assign(formData, {
    id: undefined,
    vehicleNumber: '',
    driverName: '',
    itemList: '',
    remark: '',
  });
};

// 查看详情
const handleView = (record) => {
  currentRecord.value = record;
  detailModalVisible.value = true;
};

// 审批
const handleApprove = async (record) => {
  // 审批逻辑，可以打开审批弹窗
  message.info('审批功能待实现');
};

// 打印
const handlePrint = async (record) => {
  try {
    const res = await visitorApi.printElectronicPass(record.id);
    if (res.success) {
      message.success('打印成功');
    } else {
      message.error(res.msg || '打印失败');
    }
  } catch (error) {
    message.error('打印失败：' + error.message);
  }
};

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate();
    const res = isEdit.value
      ? await visitorApi.updateElectronicPass(formData)
      : await visitorApi.createElectronicPass(formData);
    if (res.success) {
      message.success(isEdit.value ? '编辑成功' : '创建成功');
      modalVisible.value = false;
      queryPassList();
    } else {
      message.error(res.msg || '操作失败');
    }
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error('操作失败：' + error.message);
  }
};

// 取消
const handleCancel = () => {
  modalVisible.value = false;
};

// 获取状态颜色
const getStatusColor = (status) => {
  const colorMap = {
    PENDING: 'orange',
    APPROVED: 'green',
    REJECTED: 'red',
    USED: 'default',
  };
  return colorMap[status] || 'default';
};

// 获取状态文本
const getStatusText = (status) => {
  const textMap = {
    PENDING: '待审批',
    APPROVED: '已批准',
    REJECTED: '已拒绝',
    USED: '已使用',
  };
  return textMap[status] || status;
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString('zh-CN');
};

// 暴露方法供父组件调用
defineExpose({
  showAddModal,
});

// 初始化
onMounted(() => {
  queryPassList();
});
</script>

<style scoped lang="less">
.electronic-pass-management {
  .search-card {
    background: #fff;
  }

  .table-card {
    background: #fff;
  }
}
</style>

