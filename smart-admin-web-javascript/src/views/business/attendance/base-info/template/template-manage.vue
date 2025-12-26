<template>
  <div class="template-manage">
    <!-- 模板列表 -->
    <a-card title="规则模板管理" :bordered="false">
      <!-- 工具栏 -->
      <template #extra>
        <a-space>
          <a-button type="primary" @click="showAddModal">
            <template #icon><PlusOutlined /></template>
            新增模板
          </a-button>
          <a-button @click="showSystemTemplates">
            系统模板
          </a-button>
          <a-button @click="showImportModal">
            导入模板
          </a-button>
        </a-space>
      </template>

      <!-- 搜索表单 -->
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="模板名称">
          <a-input
            v-model:value="searchForm.keyword"
            placeholder="模板名称/编码/标签"
            style="width: 200px"
            allowClear
          />
        </a-form-item>
        <a-form-item label="模板类型">
          <a-select
            v-model:value="searchForm.templateType"
            placeholder="全部类型"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="SYSTEM">系统模板</a-select-option>
            <a-select-option value="CUSTOM">自定义模板</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模板分类">
          <a-select
            v-model:value="searchForm.templateCategory"
            placeholder="全部分类"
            style="width: 120px"
            allowClear
          >
            <a-select-option value="TIME">时间规则</a-select-option>
            <a-select-option value="ABSENCE">缺勤规则</a-select-option>
            <a-select-option value="OVERTIME">加班规则</a-select-option>
            <a-select-option value="LEAVE">请假规则</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadTemplateList">
              查询
            </a-button>
            <a-button @click="resetSearch">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 模板表格 -->
      <a-table
        :columns="templateColumns"
        :data-source="templateList"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="templateId"
        :scroll="{ x: 1600 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'templateCategory'">
            <a-tag :color="getCategoryColor(record.templateCategory)">
              {{ getCategoryName(record.templateCategory) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'templateType'">
            <a-tag :color="record.templateType === 'SYSTEM' ? 'blue' : 'green'">
              {{ record.templateType === 'SYSTEM' ? '系统模板' : '自定义模板' }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-switch
              :checked="record.status === 1"
              @change="(checked) => handleToggleStatus(record.templateId, checked)"
            />
          </template>

          <template v-else-if="column.key === 'useCount'">
            <a-tag color="orange">{{ record.useCount || 0 }}次</a-tag>
          </template>

          <template v-else-if="column.key === 'tags'">
            <a-tag v-for="tag in parseTags(record.tags)" :key="tag" color="cyan">
              {{ tag }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewTemplate(record)">
                查看
              </a-button>
              <a-button type="link" size="small" @click="editTemplate(record)" :disabled="record.isSystem === 1">
                编辑
              </a-button>
              <a-button type="link" size="small" @click="applyTemplate(record)">
                应用
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="copyTemplate(record)">
                      <CopyOutlined /> 复制模板
                    </a-menu-item>
                    <a-menu-item @click="exportTemplate(record)">
                      <ExportOutlined /> 导出模板
                    </a-menu-item>
                    <a-menu-item @click="deleteTemplate(record)" :disabled="record.isSystem === 1">
                      <DeleteOutlined /> 删除模板
                    </a-menu-item>
                  </a-menu>
                </template>
                <a-button type="link" size="small">
                  更多 <DownOutlined />
                </a-button>
              </a-dropdown>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑模板Modal -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form ref="templateFormRef" :model="templateForm" :rules="templateRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="模板编码" name="templateCode">
          <a-input
            v-model:value="templateForm.templateCode"
            placeholder="请输入模板编码"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="模板名称" name="templateName">
          <a-input v-model:value="templateForm.templateName" placeholder="请输入模板名称" />
        </a-form-item>
        <a-form-item label="模板分类" name="templateCategory">
          <a-select v-model:value="templateForm.templateCategory" placeholder="请选择模板分类">
            <a-select-option value="TIME">时间规则</a-select-option>
            <a-select-option value="ABSENCE">缺勤规则</a-select-option>
            <a-select-option value="OVERTIME">加班规则</a-select-option>
            <a-select-option value="LEAVE">请假规则</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模板类型" name="templateType">
          <a-select v-model:value="templateForm.templateType" placeholder="请选择模板类型">
            <a-select-option value="CUSTOM">自定义模板</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模板条件" name="templateCondition">
          <a-textarea
            v-model:value="templateForm.templateCondition"
            placeholder='例如: {"lateMinutes": 5}'
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="模板动作" name="templateAction">
          <a-textarea
            v-model:value="templateForm.templateAction"
            placeholder='例如: {"deductAmount": 50}'
            :rows="3"
          />
        </a-form-item>
        <a-form-item label="模板描述" name="description">
          <a-textarea v-model:value="templateForm.description" placeholder="请输入模板描述" :rows="2" />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-input v-model:value="templateForm.tags" placeholder="逗号分隔，如：迟到,扣款,常用" />
        </a-form-item>
        <a-form-item label="排序号" name="sort">
          <a-input-number v-model:value="templateForm.sort" :min="0" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-switch v-model:checked="templateForm.statusChecked" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { PlusOutlined, CopyOutlined, ExportOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons-vue';
import SmartRequest from '/@/utils/request';
import { ruleTemplateApi } from '/@/api/business/attendance/rule-template';

// 搜索表单
const searchForm = reactive({
  keyword: '',
  templateType: undefined,
  templateCategory: undefined,
  status: undefined
});

// 模板列表
const templateList = ref([]);
const loading = ref(false);

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showTotal: (total) => `共 ${total} 条`
});

// Modal相关
const modalVisible = ref(false);
const modalTitle = ref('新增模板');
const isEdit = ref(false);
const templateFormRef = ref();

// 模板表单
const templateForm = reactive({
  templateCode: '',
  templateName: '',
  templateCategory: undefined,
  templateType: 'CUSTOM',
  templateCondition: '',
  templateAction: '',
  description: '',
  tags: '',
  sort: 0,
  statusChecked: true,
  status: 1
});

// 表单验证规则
const templateRules = {
  templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }],
  templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
  templateCategory: [{ required: true, message: '请选择模板分类', trigger: 'change' }],
  templateCondition: [{ required: true, message: '请输入模板条件', trigger: 'blur' }],
  templateAction: [{ required: true, message: '请输入模板动作', trigger: 'blur' }]
};

// 表格列定义
const templateColumns = [
  { title: '模板ID', dataIndex: 'templateId', key: 'templateId', width: 100 },
  { title: '模板编码', dataIndex: 'templateCode', key: 'templateCode', width: 150 },
  { title: '模板名称', dataIndex: 'templateName', key: 'templateName', width: 200 },
  { title: '模板分类', dataIndex: 'templateCategory', key: 'templateCategory', width: 120 },
  { title: '模板类型', dataIndex: 'templateType', key: 'templateType', width: 120 },
  { title: '使用次数', dataIndex: 'useCount', key: 'useCount', width: 100 },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 200 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '排序', dataIndex: 'sort', key: 'sort', width: 80 },
  { title: '操作', key: 'action', fixed: 'right', width: 200 }
];

// 加载模板列表
const loadTemplateList = async () => {
  loading.value = true;
  try {
    const res = await ruleTemplateApi.queryPage({
      ...searchForm,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    });
    if (res.code === 200) {
      templateList.value = res.data.list || [];
      pagination.total = res.data.total || 0;
    }
  } catch (error) {
    message.error('加载模板列表失败');
  } finally {
    loading.value = false;
  }
};

// 重置搜索
const resetSearch = () => {
  searchForm.keyword = '';
  searchForm.templateType = undefined;
  searchForm.templateCategory = undefined;
  searchForm.status = undefined;
  pagination.current = 1;
  loadTemplateList();
};

// 表格分页变化
const handleTableChange = (pag) => {
  pagination.current = pag.current;
  pagination.pageSize = pag.pageSize;
  loadTemplateList();
};

// 显示新增Modal
const showAddModal = () => {
  modalTitle.value = '新增模板';
  isEdit.value = false;
  Object.assign(templateForm, {
    templateCode: '',
    templateName: '',
    templateCategory: undefined,
    templateType: 'CUSTOM',
    templateCondition: '',
    templateAction: '',
    description: '',
    tags: '',
    sort: 0,
    statusChecked: true,
    status: 1
  });
  modalVisible.value = true;
};

// 编辑模板
const editTemplate = (record) => {
  modalTitle.value = '编辑模板';
  isEdit.value = true;
  Object.assign(templateForm, {
    ...record,
    statusChecked: record.status === 1
  });
  modalVisible.value = true;
};

// 查看模板
const viewTemplate = (record) => {
  Modal.info({
    title: '模板详情',
    width: 600,
    content: `
      <p><strong>模板编码:</strong> ${record.templateCode}</p>
      <p><strong>模板名称:</strong> ${record.templateName}</p>
      <p><strong>模板分类:</strong> ${getCategoryName(record.templateCategory)}</p>
      <p><strong>模板条件:</strong> <pre>${record.templateCondition}</pre></p>
      <p><strong>模板动作:</strong> <pre>${record.templateAction}</pre></p>
      <p><strong>描述:</strong> ${record.description || '-'}</p>
    `
  });
};

// 应用模板
const applyTemplate = (record) => {
  Modal.confirm({
    title: '应用模板',
    content: `确定要应用模板"${record.templateName}"吗？`,
    onOk: async () => {
      message.success('模板应用成功（功能待实现）');
    }
  });
};

// 复制模板
const copyTemplate = async (record) => {
  try {
    const res = await ruleTemplateApi.copy(record.templateId, `${record.templateName} - 副本`);
    if (res.code === 200) {
      message.success('模板复制成功');
      loadTemplateList();
    }
  } catch (error) {
    message.error('模板复制失败');
  }
};

// 导出模板
const exportTemplate = async (record) => {
  try {
    const res = await ruleTemplateApi.export(record.templateId);
    if (res.code === 200) {
      // 创建下载链接
      const blob = new Blob([res.data], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${record.templateCode}.json`;
      a.click();
      URL.revokeObjectURL(url);
      message.success('模板导出成功');
    }
  } catch (error) {
    message.error('模板导出失败');
  }
};

// 删除模板
const deleteTemplate = (record) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板"${record.templateName}"吗？`,
    onOk: async () => {
      try {
        const res = await ruleTemplateApi.delete(record.templateId);
        if (res.code === 200) {
          message.success('删除成功');
          loadTemplateList();
        }
      } catch (error) {
        message.error('删除失败');
      }
    }
  });
};

// 切换状态
const handleToggleStatus = async (templateId, checked) => {
  try {
    const status = checked ? 1 : 0;
    const res = await ruleTemplateApi.update(templateId, { status });
    if (res.code === 200) {
      message.success('状态更新成功');
      loadTemplateList();
    }
  } catch (error) {
    message.error('状态更新失败');
  }
};

// Modal确认
const handleModalOk = async () => {
  try {
    await templateFormRef.value.validate();
    const data = {
      ...templateForm,
      status: templateForm.statusChecked ? 1 : 0
    };

    let res;
    if (isEdit.value) {
      res = await ruleTemplateApi.update(templateForm.templateId, data);
    } else {
      res = await ruleTemplateApi.create(data);
    }

    if (res.code === 200) {
      message.success(isEdit.value ? '更新成功' : '创建成功');
      modalVisible.value = false;
      loadTemplateList();
    }
  } catch (error) {
    if (error.errorFields) {
      message.error('请检查表单数据');
    } else {
      message.error('操作失败');
    }
  }
};

// Modal取消
const handleModalCancel = () => {
  modalVisible.value = false;
  templateFormRef.value?.resetFields();
};

// 显示系统模板
const showSystemTemplates = () => {
  searchForm.templateType = 'SYSTEM';
  loadTemplateList();
};

// 显示导入Modal
const showImportModal = () => {
  message.info('导入功能待实现');
};

// 工具函数
const getCategoryName = (category) => {
  const map = {
    TIME: '时间规则',
    ABSENCE: '缺勤规则',
    OVERTIME: '加班规则',
    LEAVE: '请假规则'
  };
  return map[category] || category;
};

const getCategoryColor = (category) => {
  const map = {
    TIME: 'blue',
    ABSENCE: 'red',
    OVERTIME: 'orange',
    LEAVE: 'green'
  };
  return map[category] || 'default';
};

const parseTags = (tags) => {
  if (!tags) return [];
  return tags.split(',').map(tag => tag.trim()).filter(tag => tag);
};

// 初始化
onMounted(() => {
  loadTemplateList();
});
</script>

<style scoped lang="less">
.template-manage {
  padding: 16px;
}
</style>
