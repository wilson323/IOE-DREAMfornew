<!--
  * 流程定义管理页
  * 提供工作流流程定义的查询、部署、激活、禁用、删除功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <div class="definition-list-page">
    <!-- 查询表单区域 -->
    <a-card size="small" :bordered="false" class="query-card">
      <a-form class="smart-query-form" v-privilege="'oa:workflow:definition:query'">
        <a-row class="smart-query-form-row">
          <a-form-item label="流程分类" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.category"
              style="width: 150px"
              :show-search="true"
              :allow-clear="true"
              placeholder="请选择分类"
            >
              <a-select-option v-for="item in categoryList" :key="item.value" :value="item.value">
                {{ item.label }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="状态" class="smart-query-form-item">
            <a-select
              v-model:value="queryForm.status"
              style="width: 120px"
              :allow-clear="true"
              placeholder="请选择状态"
            >
              <a-select-option value="DRAFT">草稿</a-select-option>
              <a-select-option value="PUBLISHED">已发布</a-select-option>
              <a-select-option value="DISABLED">已禁用</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="关键词" class="smart-query-form-item">
            <a-input style="width: 200px" v-model:value="queryForm.keyword" placeholder="流程名称、流程Key" />
          </a-form-item>

          <a-form-item class="smart-query-form-item smart-margin-left10">
            <a-button-group>
              <a-button type="primary" @click="onSearch">
                <template #icon>
                  <SearchOutlined />
                </template>
                查询
              </a-button>
              <a-button @click="onReload">
                <template #icon>
                  <ReloadOutlined />
                </template>
                重置
              </a-button>
            </a-button-group>
          </a-form-item>
        </a-row>
      </a-form>
    </a-card>

    <!-- 流程定义列表区域 -->
    <a-card size="small" :bordered="false" class="definition-list-card">
      <a-row class="smart-table-btn-block">
        <div class="smart-table-operate-block">
          <a-button type="primary" @click="handleDeploy" v-privilege="'oa:workflow:definition:deploy'">
            <template #icon>
              <UploadOutlined />
            </template>
            部署流程
          </a-button>
        </div>
      </a-row>

      <a-table
        row-key="definitionId"
        :columns="tableColumns"
        :data-source="definitionList"
        :scroll="{ x: 1400 }"
        :pagination="false"
        :loading="loading"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'processName'">
            <a @click="handleViewDetail(record.definitionId)">{{ text }}</a>
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <a-tag v-if="text === 'PUBLISHED'" color="success">已发布</a-tag>
            <a-tag v-else-if="text === 'DRAFT'" color="default">草稿</a-tag>
            <a-tag v-else-if="text === 'DISABLED'" color="error">已禁用</a-tag>
          </template>
          <template v-else-if="column.dataIndex === 'action'">
            <div class="smart-table-operate">
              <a-button type="link" size="small" @click="handleViewDetail(record.definitionId)">详情</a-button>
              <a-button
                v-if="record.status === 'DISABLED'"
                type="link"
                size="small"
                @click="handleActivate(record.definitionId)"
                v-privilege="'oa:workflow:definition:activate'"
              >
                激活
              </a-button>
              <a-button
                v-if="record.status === 'PUBLISHED'"
                type="link"
                size="small"
                danger
                @click="handleDisable(record.definitionId)"
                v-privilege="'oa:workflow:definition:disable'"
              >
                禁用
              </a-button>
              <a-popconfirm
                title="确认删除此流程定义吗？"
                @confirm="handleDelete(record.definitionId)"
              >
                <a-button
                  type="link"
                  size="small"
                  danger
                  v-privilege="'oa:workflow:definition:delete'"
                >
                  删除
                </a-button>
              </a-popconfirm>
            </div>
          </template>
        </template>
      </a-table>

      <div class="smart-query-table-page">
        <a-pagination
          show-size-changer
          show-quick-jumper
          show-less-items
          :page-size-options="PAGE_SIZE_OPTIONS"
          :default-page-size="queryForm.pageSize"
          v-model:current="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :total="total"
          @change="queryDefinitionList"
          :show-total="(total) => `共${total}条`"
        />
      </div>
    </a-card>

    <!-- 部署流程对话框 -->
    <a-modal
      v-model:open="deployModalVisible"
      title="部署流程定义"
      width="800px"
      @ok="handleDeployConfirm"
      @cancel="handleDeployCancel"
    >
      <a-form :model="deployForm" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="流程名称" required>
          <a-input v-model:value="deployForm.processName" placeholder="请输入流程名称" />
        </a-form-item>
        <a-form-item label="流程Key" required>
          <a-input v-model:value="deployForm.processKey" placeholder="请输入流程Key（唯一标识）" />
        </a-form-item>
        <a-form-item label="流程描述">
          <a-textarea v-model:value="deployForm.description" :rows="3" placeholder="请输入流程描述" />
        </a-form-item>
        <a-form-item label="流程分类">
          <a-select v-model:value="deployForm.category" placeholder="请选择分类">
            <a-select-option v-for="item in categoryList" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="BPMN文件" required>
          <a-upload
            :before-upload="handleBpmnUpload"
            :file-list="bpmnFileList"
            :max-count="1"
            accept=".bpmn,.xml"
          >
            <a-button>
              <template #icon>
                <UploadOutlined />
              </template>
              选择BPMN文件
            </a-button>
          </a-upload>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { message, Modal } from 'ant-design-vue';
  import { SearchOutlined, ReloadOutlined, UploadOutlined } from '@ant-design/icons-vue';
  import { PAGE_SIZE, PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
  import { useWorkflowStore } from '/@/store/modules/business/workflow';
  import { workflowApi } from '/@/api/business/oa/workflow-api';
  import { smartSentry } from '/@/lib/smart-sentry';

  const router = useRouter();
  const workflowStore = useWorkflowStore();

  // 查询表单
  const queryFormState = {
    pageNum: 1,
    pageSize: PAGE_SIZE,
    category: null,
    status: null,
    keyword: null,
  };
  const queryForm = reactive({ ...queryFormState });

  // 表格数据
  const definitionList = ref([]);
  const total = ref(0);
  const loading = ref(false);

  // 流程分类列表
  const categoryList = ref([
    { label: '请假申请', value: 'LEAVE' },
    { label: '报销申请', value: 'EXPENSE' },
    { label: '采购申请', value: 'PURCHASE' },
    { label: '合同审批', value: 'CONTRACT' },
    { label: '其他', value: 'OTHER' },
  ]);

  // 部署相关
  const deployModalVisible = ref(false);
  const deployForm = reactive({
    processName: '',
    processKey: '',
    description: '',
    category: null,
    bpmnXml: '',
  });
  const bpmnFileList = ref([]);

  // 表格列定义
  const tableColumns = ref([
    {
      title: '流程名称',
      dataIndex: 'processName',
      width: 200,
      ellipsis: true,
    },
    {
      title: '流程Key',
      dataIndex: 'processKey',
      width: 200,
      ellipsis: true,
    },
    {
      title: '版本',
      dataIndex: 'version',
      width: 80,
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: 100,
    },
    {
      title: '分类',
      dataIndex: 'category',
      width: 100,
    },
    {
      title: '部署时间',
      dataIndex: 'deployTime',
      width: 180,
    },
    {
      title: '操作',
      dataIndex: 'action',
      fixed: 'right',
      width: 250,
    },
  ]);

  /**
   * 查询流程定义列表
   */
  async function queryDefinitionList() {
    try {
      loading.value = true;
      const params = {
        pageNum: queryForm.pageNum,
        pageSize: queryForm.pageSize,
        category: queryForm.category,
        status: queryForm.status,
        keyword: queryForm.keyword,
      };
      await workflowStore.fetchDefinitionList(params);
      definitionList.value = workflowStore.definitionList;
      total.value = workflowStore.definitionTotal;
    } catch (err) {
      console.error('查询流程定义列表失败:', err);
      smartSentry.captureError(err);
      message.error('查询流程定义列表失败');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 点击查询
   */
  function onSearch() {
    queryForm.pageNum = 1;
    queryDefinitionList();
  }

  /**
   * 点击重置
   */
  function onReload() {
    Object.assign(queryForm, queryFormState);
    queryDefinitionList();
  }

  /**
   * 查看详情
   * @param {Number} definitionId - 流程定义ID
   */
  function handleViewDetail(definitionId) {
    router.push({
      path: '/oa/workflow/definition/definition-detail',
      query: { definitionId },
    });
  }

  /**
   * 部署流程
   */
  function handleDeploy() {
    Object.assign(deployForm, {
      processName: '',
      processKey: '',
      description: '',
      category: null,
      bpmnXml: '',
    });
    bpmnFileList.value = [];
    deployModalVisible.value = true;
  }

  /**
   * BPMN文件上传处理
   * @param {File} file - 文件对象
   * @returns {Boolean} 是否阻止默认上传
   */
  function handleBpmnUpload(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      deployForm.bpmnXml = e.target.result;
    };
    reader.readAsText(file);
    bpmnFileList.value = [file];
    return false; // 阻止默认上传
  }

  /**
   * 确认部署
   */
  async function handleDeployConfirm() {
    if (!deployForm.processName || !deployForm.processKey || !deployForm.bpmnXml) {
      message.warning('请填写完整的部署信息');
      return;
    }

    try {
      loading.value = true;
      const response = await workflowApi.deployProcess({
        bpmnXml: deployForm.bpmnXml,
        processName: deployForm.processName,
        processKey: deployForm.processKey,
        description: deployForm.description,
        category: deployForm.category,
      });

      if (response.code === 1 || response.code === 200) {
        message.success('部署成功');
        handleDeployCancel();
        await queryDefinitionList();
      } else {
        message.error(response.message || '部署失败');
      }
    } catch (err) {
      console.error('部署流程失败:', err);
      smartSentry.captureError(err);
      message.error('部署流程失败');
    } finally {
      loading.value = false;
    }
  }

  /**
   * 取消部署
   */
  function handleDeployCancel() {
    deployModalVisible.value = false;
    Object.assign(deployForm, {
      processName: '',
      processKey: '',
      description: '',
      category: null,
      bpmnXml: '',
    });
    bpmnFileList.value = [];
  }

  /**
   * 激活流程定义
   * @param {Number} definitionId - 流程定义ID
   */
  async function handleActivate(definitionId) {
    try {
      const response = await workflowApi.activateDefinition(definitionId);
      if (response.code === 1 || response.code === 200) {
        message.success('激活成功');
        await queryDefinitionList();
      } else {
        message.error(response.message || '激活失败');
      }
    } catch (err) {
      console.error('激活流程定义失败:', err);
      smartSentry.captureError(err);
      message.error('激活流程定义失败');
    }
  }

  /**
   * 禁用流程定义
   * @param {Number} definitionId - 流程定义ID
   */
  async function handleDisable(definitionId) {
    try {
      const response = await workflowApi.disableDefinition(definitionId);
      if (response.code === 1 || response.code === 200) {
        message.success('禁用成功');
        await queryDefinitionList();
      } else {
        message.error(response.message || '禁用失败');
      }
    } catch (err) {
      console.error('禁用流程定义失败:', err);
      smartSentry.captureError(err);
      message.error('禁用流程定义失败');
    }
  }

  /**
   * 删除流程定义
   * @param {Number} definitionId - 流程定义ID
   */
  async function handleDelete(definitionId) {
    try {
      const response = await workflowApi.deleteDefinition(definitionId, false);
      if (response.code === 1 || response.code === 200) {
        message.success('删除成功');
        await queryDefinitionList();
      } else {
        message.error(response.message || '删除失败');
      }
    } catch (err) {
      console.error('删除流程定义失败:', err);
      smartSentry.captureError(err);
      message.error('删除流程定义失败');
    }
  }

  // 初始化
  onMounted(() => {
    queryDefinitionList();
  });
</script>

<style lang="less" scoped>
  .definition-list-page {
    padding: 16px;

    .query-card {
      margin-bottom: 16px;
    }
  }
</style>

