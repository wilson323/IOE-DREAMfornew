<!--
 * 访客黑名单管理页面
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="visitor-blacklist-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="page-title">
        <h1>访客黑名单管理</h1>
        <p>管理被禁止访问的访客信息</p>
      </div>
      <div class="quick-actions">
        <a-space>
          <a-button type="primary" danger @click="showAddBlacklistModal">
            <template #icon><UserDeleteOutlined /></template>
            加入黑名单
          </a-button>
          <a-button @click="handleExport">
            <template #icon><DownloadOutlined /></template>
            导出
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 搜索表单 -->
    <a-card :bordered="false" class="search-card">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="访客姓名">
          <a-input
            v-model:value="searchForm.visitorName"
            placeholder="请输入访客姓名"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input
            v-model:value="searchForm.phone"
            placeholder="请输入手机号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="身份证号">
          <a-input
            v-model:value="searchForm.idCard"
            placeholder="请输入身份证号"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="加入原因">
          <a-select
            v-model:value="searchForm.reason"
            placeholder="请选择原因"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="SECURITY">安全原因</a-select-option>
            <a-select-option value="VIOLATION">违规行为</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              查询
            </a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 黑名单列表 -->
    <a-card :bordered="false" class="table-card" style="margin-top: 16px">
      <template #title>
        <span>黑名单列表</span>
      </template>
      <a-table
        :columns="blacklistColumns"
        :data-source="blacklistList"
        :loading="loading"
        :pagination="pagination"
        :row-selection="rowSelection"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'visitorName'">
            <a-space>
              <a-avatar :size="32" :src="record.avatar">
                {{ record.visitorName?.charAt(0) }}
              </a-avatar>
              <span>{{ record.visitorName }}</span>
            </a-space>
          </template>
          <template v-else-if="column.dataIndex === 'phone'">
            <span>{{ formatPhone(record.phone) }}</span>
          </template>
          <template v-else-if="column.dataIndex === 'reason'">
            <a-tag :color="getReasonColor(record.reason)">
              {{ getReasonText(record.reason) }}
            </a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-badge
              :status="record.status === 'ACTIVE' ? 'error' : 'default'"
              :text="record.status === 'ACTIVE' ? '生效中' : '已解除'"
            />
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                查看
              </a-button>
              <a-button
                v-if="record.status === 'ACTIVE'"
                type="link"
                size="small"
                danger
                @click="handleRemove(record)"
              >
                移出黑名单
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 加入黑名单弹窗 -->
    <a-modal
      v-model:open="addBlacklistModalVisible"
      title="加入黑名单"
      :width="600"
      @ok="handleAddBlacklist"
      @cancel="handleCancelAddBlacklist"
    >
      <a-form
        ref="addBlacklistFormRef"
        :model="addBlacklistForm"
        :rules="addBlacklistRules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="访客姓名" name="visitorName">
          <a-input
            v-model:value="addBlacklistForm.visitorName"
            placeholder="请输入访客姓名"
          />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input
            v-model:value="addBlacklistForm.phone"
            placeholder="请输入手机号"
          />
        </a-form-item>
        <a-form-item label="身份证号" name="idCard">
          <a-input
            v-model:value="addBlacklistForm.idCard"
            placeholder="请输入身份证号"
          />
        </a-form-item>
        <a-form-item label="加入原因" name="reason">
          <a-select
            v-model:value="addBlacklistForm.reason"
            placeholder="请选择加入原因"
          >
            <a-select-option value="SECURITY">安全原因</a-select-option>
            <a-select-option value="VIOLATION">违规行为</a-select-option>
            <a-select-option value="OTHER">其他</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="详细说明" name="description">
          <a-textarea
            v-model:value="addBlacklistForm.description"
            placeholder="请输入详细说明"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailModalVisible"
      title="黑名单详情"
      :width="600"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="访客姓名">
          {{ currentRecord.visitorName }}
        </a-descriptions-item>
        <a-descriptions-item label="手机号">
          {{ formatPhone(currentRecord.phone) }}
        </a-descriptions-item>
        <a-descriptions-item label="身份证号">
          {{ currentRecord.idCard }}
        </a-descriptions-item>
        <a-descriptions-item label="加入原因">
          <a-tag :color="getReasonColor(currentRecord.reason)">
            {{ getReasonText(currentRecord.reason) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="详细说明" :span="2">
          {{ currentRecord.description || '无' }}
        </a-descriptions-item>
        <a-descriptions-item label="加入时间">
          {{ formatDateTime(currentRecord.addTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="加入人">
          {{ currentRecord.addUserName }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-badge
            :status="currentRecord.status === 'ACTIVE' ? 'error' : 'default'"
            :text="currentRecord.status === 'ACTIVE' ? '生效中' : '已解除'"
          />
        </a-descriptions-item>
        <a-descriptions-item
          v-if="currentRecord.removeTime"
          label="解除时间"
        >
          {{ formatDateTime(currentRecord.removeTime) }}
        </a-descriptions-item>
        <a-descriptions-item
          v-if="currentRecord.removeUserName"
          label="解除人"
        >
          {{ currentRecord.removeUserName }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import {
  SearchOutlined,
  UserDeleteOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue';
import visitorApi from '/@/api/business/visitor/visitor-api';

// 搜索表单
const searchForm = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  reason: undefined,
});

// 表格数据
const blacklistList = ref([]);
const loading = ref(false);
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`,
});

// 表格列定义
const blacklistColumns = [
  {
    title: '访客姓名',
    dataIndex: 'visitorName',
    key: 'visitorName',
    width: 150,
  },
  {
    title: '手机号',
    dataIndex: 'phone',
    key: 'phone',
    width: 130,
  },
  {
    title: '身份证号',
    dataIndex: 'idCard',
    key: 'idCard',
    width: 180,
  },
  {
    title: '加入原因',
    dataIndex: 'reason',
    key: 'reason',
    width: 120,
  },
  {
    title: '详细说明',
    dataIndex: 'description',
    key: 'description',
    ellipsis: true,
  },
  {
    title: '加入时间',
    dataIndex: 'addTime',
    key: 'addTime',
    width: 180,
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100,
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right',
  },
];

// 行选择
const selectedRowKeys = ref([]);
const rowSelection = {
  selectedRowKeys: selectedRowKeys,
  onChange: (keys) => {
    selectedRowKeys.value = keys;
  },
};

// 加入黑名单弹窗
const addBlacklistModalVisible = ref(false);
const addBlacklistFormRef = ref();
const addBlacklistForm = reactive({
  visitorName: '',
  phone: '',
  idCard: '',
  reason: undefined,
  description: '',
});

const addBlacklistRules = {
  visitorName: [{ required: true, message: '请输入访客姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  reason: [{ required: true, message: '请选择加入原因', trigger: 'change' }],
};

// 详情弹窗
const detailModalVisible = ref(false);
const currentRecord = ref(null);

// 查询黑名单列表
const queryBlacklistList = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      ...searchForm,
    };
    const res = await visitorApi.queryBlacklist(params);
    if (res.data) {
      blacklistList.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    message.error('查询黑名单列表失败：' + error.message);
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  pagination.current = 1;
  queryBlacklistList();
};

// 重置
const handleReset = () => {
  Object.assign(searchForm, {
    visitorName: '',
    phone: '',
    idCard: '',
    reason: undefined,
  });
  handleSearch();
};

// 表格变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  queryBlacklistList();
};

// 显示加入黑名单弹窗
const showAddBlacklistModal = () => {
  addBlacklistModalVisible.value = true;
  Object.assign(addBlacklistForm, {
    visitorName: '',
    phone: '',
    idCard: '',
    reason: undefined,
    description: '',
  });
};

// 加入黑名单
const handleAddBlacklist = async () => {
  try {
    await addBlacklistFormRef.value.validate();
    const res = await visitorApi.addToBlacklist(addBlacklistForm);
    if (res.success) {
      message.success('加入黑名单成功');
      addBlacklistModalVisible.value = false;
      queryBlacklistList();
    } else {
      message.error(res.msg || '加入黑名单失败');
    }
  } catch (error) {
    if (error.errorFields) {
      return;
    }
    message.error('加入黑名单失败：' + error.message);
  }
};

// 取消加入黑名单
const handleCancelAddBlacklist = () => {
  addBlacklistModalVisible.value = false;
};

// 查看详情
const handleView = (record) => {
  currentRecord.value = record;
  detailModalVisible.value = true;
};

// 移出黑名单
const handleRemove = async (record) => {
  try {
    const res = await visitorApi.removeFromBlacklist([record.id]);
    if (res.success) {
      message.success('移出黑名单成功');
      queryBlacklistList();
    } else {
      message.error(res.msg || '移出黑名单失败');
    }
  } catch (error) {
    message.error('移出黑名单失败：' + error.message);
  }
};

// 导出
const handleExport = async () => {
  try {
    const res = await visitorApi.exportBlacklist(searchForm);
    if (res.data) {
      // 创建下载链接
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `黑名单列表_${new Date().getTime()}.xlsx`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      message.success('导出成功');
    }
  } catch (error) {
    message.error('导出失败：' + error.message);
  }
};

// 格式化手机号
const formatPhone = (phone) => {
  if (!phone) return '';
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
};

// 获取原因颜色
const getReasonColor = (reason) => {
  const colorMap = {
    SECURITY: 'red',
    VIOLATION: 'orange',
    OTHER: 'default',
  };
  return colorMap[reason] || 'default';
};

// 获取原因文本
const getReasonText = (reason) => {
  const textMap = {
    SECURITY: '安全原因',
    VIOLATION: '违规行为',
    OTHER: '其他',
  };
  return textMap[reason] || reason;
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString('zh-CN');
};

// 初始化
onMounted(() => {
  queryBlacklistList();
});
</script>

<style scoped lang="less">
.visitor-blacklist-page {
  padding: 24px;
  background: #f0f2f5;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;

    .page-title {
      h1 {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #262626;
      }

      p {
        margin: 4px 0 0 0;
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }

  .search-card {
    background: #fff;
  }

  .table-card {
    background: #fff;
  }
}
</style>

