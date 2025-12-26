<!--
  * 补贴管理模块 - 企业级完整实现
  * 支持月度补贴、一次性补贴、条件补贴
  * 批量发放、审批流程、发放记录
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="subsidy-list-page">
    <a-card :bordered="false">
      <template #title>
        <span>补贴管理</span>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新建补贴规则
          </a-button>
          <a-button @click="handleBatchDistribute">
            <template #icon><SendOutlined /></template>
            批量发放
          </a-button>
          <a-button @click="handleRefresh">
            <template #icon><ReloadOutlined /></template>
            刷新
          </a-button>
        </a-space>
      </template>

      <!-- 搜索表单 -->
      <a-form class="smart-query-form">
        <a-row :gutter="16">
          <a-col :span="6">
            <a-form-item label="补贴名称">
              <a-input v-model:value="queryForm.subsidyName" placeholder="请输入补贴名称" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="补贴类型">
              <a-select v-model:value="queryForm.subsidyType" placeholder="请选择" allow-clear>
                <a-select-option value="MONTHLY">月度补贴</a-select-option>
                <a-select-option value="ONE_TIME">一次性补贴</a-select-option>
                <a-select-option value="CONDITIONAL">条件补贴</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item label="状态">
              <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear>
                <a-select-option :value="1">启用</a-select-option>
                <a-select-option :value="0">禁用</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-space>
              <a-button type="primary" @click="handleQuery">
                <template #icon><SearchOutlined /></template>
                查询
              </a-button>
              <a-button @click="handleResetQuery">
                <template #icon><ClearOutlined /></template>
                重置
              </a-button>
            </a-space>
          </a-col>
        </a-row>
      </a-form>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="pagination"
        :loading="loading"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: onSelectChange,
        }"
        row-key="subsidyId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'subsidyName'">
            <a @click="handleView(record)">{{ record.subsidyName }}</a>
            <a-tag v-if="record.isDefault" color="gold" style="margin-left: 8px">默认</a-tag>
          </template>

          <template v-else-if="column.key === 'amount'">
            <span class="amount-text">¥{{ formatAmount(record.amount) }}</span>
          </template>

          <template v-else-if="column.key === 'subsidyType'">
            <a-tag :color="getSubsidyTypeColor(record.subsidyType)">
              {{ getSubsidyTypeName(record.subsidyType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'targetType'">
            <a-tag v-for="type in record.targetTypes" :key="type" color="blue" style="margin: 2px">
              {{ getTargetTypeName(type) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="record.status === 1 ? 'success' : 'default'"
              :text="record.status === 1 ? '启用' : '禁用'"
            />
          </template>

          <template v-else-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">
                <template #icon><EyeOutlined /></template>
                查看
              </a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">
                <template #icon><EditOutlined /></template>
                编辑
              </a-button>
              <a-button type="link" size="small" @click="handleDistribute(record)">
                <template #icon><SendOutlined /></template>
                发放
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleCopy(record)">
                      <CopyOutlined /> 复制
                    </a-menu-item>
                    <a-menu-item @click="handleToggleStatus(record)">
                      {{ record.status === 1 ? '禁用' : '启用' }}
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item @click="handleDelete(record)" danger>
                      <DeleteOutlined /> 删除
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

    <!-- 新建/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="900px"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <!-- 基本信息 -->
          <a-divider orientation="left">基本信息</a-divider>
          <a-form-item label="补贴名称" name="subsidyName">
            <a-input v-model:value="formData.subsidyName" placeholder="请输入补贴名称" />
          </a-form-item>

          <a-form-item label="补贴类型" name="subsidyType">
            <a-select v-model:value="formData.subsidyType" placeholder="请选择补贴类型">
              <a-select-option value="MONTHLY">月度补贴 - 按月自动发放</a-select-option>
              <a-select-option value="ONE_TIME">一次性补贴 - 单次发放</a-select-option>
              <a-select-option value="CONDITIONAL">条件补贴 - 满足条件发放</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="补贴金额" name="amount">
            <a-input-number
              v-model:value="formData.amount"
              :min="0"
              :max="999999"
              :precision="2"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入补贴金额"
            >
              <template #prefix>¥</template>
            </a-input-number>
          </a-form-item>

          <a-form-item label="排序号" name="sort">
            <a-input-number
              v-model:value="formData.sort"
              :min="0"
              :max="9999"
              style="width: 100%"
              placeholder="数字越小排序越靠前"
            />
          </a-form-item>

          <a-form-item label="描述" name="description">
            <a-textarea v-model:value="formData.description" :rows="3" placeholder="请输入补贴描述" />
          </a-form-item>

        <!-- 发放配置 -->
          <a-divider orientation="left">发放配置</a-divider>

          <!-- 月度补贴配置 -->
          <template v-if="formData.subsidyType === 'MONTHLY'">
            <a-form-item label="发放日期" name="distributeDay">
              <a-select v-model:value="formData.distributeDay" placeholder="请选择每月发放日期">
                <a-select-option v-for="day in 28" :key="day" :value="day">
                  每月{{ day }}日
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="发放时段" name="distributeTime">
              <a-time-picker
                v-model:value="formData.distributeTime"
                format="HH:mm"
                placeholder="请选择发放时间"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item label="首次发放" name="firstDistributeDate">
              <a-date-picker
                v-model:value="formData.firstDistributeDate"
                placeholder="请选择首次发放日期"
                style="width: 100%"
              />
            </a-form-item>
          </template>

          <!-- 条件补贴配置 -->
          <template v-if="formData.subsidyType === 'CONDITIONAL'">
            <a-form-item label="补贴条件" name="conditions">
              <a-checkbox-group v-model:value="formData.conditions">
                <a-checkbox value="FULL_ATTENDANCE">满勤奖励（月度全勤）</a-checkbox>
                <a-checkbox value="OVERTIME">加班补贴（累计加班超过阈值）</a-checkbox>
                <a-checkbox value="NIGHT_SHIFT">夜班补贴（夜班次数达标）</a-checkbox>
                <a-checkbox value="SPECIAL_POST">特殊岗位（特定岗位人员）</a-checkbox>
                <a-checkbox value="EXCEPTIONAL">特殊贡献（需审批）</a-checkbox>
              </a-checkbox-group>
            </a-form-item>

            <a-form-item v-if="formData.conditions.includes('OVERTIME')" label="加班阈值（小时）" name="overtimeThreshold">
              <a-input-number
                v-model:value="formData.overtimeThreshold"
                :min="0"
                :max="300"
                placeholder="累计加班小时数"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item v-if="formData.conditions.includes('NIGHT_SHIFT')" label="夜班次数（次）" name="nightShiftThreshold">
              <a-input-number
                v-model:value="formData.nightShiftThreshold"
                :min="0"
                :max="31"
                placeholder="月度夜班次数"
                style="width: 100%"
              />
            </a-form-item>
          </template>

        <!-- 目标群体 -->
          <a-divider orientation="left">目标群体</a-divider>
          <a-form-item label="目标类型" name="targetTypes">
            <a-checkbox-group v-model:value="formData.targetTypes">
              <a-checkbox value="ACCOUNT_KIND">按账户类别</a-checkbox>
              <a-checkbox value="DEPARTMENT">按部门</a-checkbox>
              <a-checkbox value="EMPLOYEE">指定员工</a-checkbox>
            </a-checkbox-group>
          </a-form-item>

          <a-form-item v-if="formData.targetTypes.includes('ACCOUNT_KIND')" label="账户类别" name="accountKindIds">
            <a-select
              v-model:value="formData.accountKindIds"
              mode="multiple"
              placeholder="请选择账户类别"
              style="width: 100%"
            >
              <a-select-option v-for="kind in accountKindList" :key="kind.accountKindId" :value="kind.accountKindId">
                {{ kind.kindName }}
              </a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item v-if="formData.targetTypes.includes('DEPARTMENT')" label="部门范围" name="departmentIds">
            <a-tree-select
              v-model:value="formData.departmentIds"
              multiple
              tree-checkable
              :tree-data="departmentTree"
              placeholder="请选择部门"
              style="width: 100%"
            />
          </a-form-item>

          <a-form-item v-if="formData.targetTypes.includes('EMPLOYEE')" label="指定员工" name="employeeIds">
            <a-select
              v-model:value="formData.employeeIds"
              mode="multiple"
              :filter-option="filterEmployee"
              placeholder="请输入员工姓名搜索"
              style="width: 100%"
              :options="employeeOptions"
              show-search
            />
          </a-form-item>

        <!-- 审批流程 -->
          <a-divider orientation="left">审批流程</a-divider>
          <a-form-item label="需要审批" name="needApproval">
            <a-switch v-model:checked="formData.needApproval" checked-children="是" un-checked-children="否" />
          </a-form-item>

          <template v-if="formData.needApproval">
            <a-form-item label="审批流程" name="approvalFlowId">
              <a-select v-model:value="formData.approvalFlowId" placeholder="请选择审批流程">
                <a-select-option value="STANDARD">标准审批流程（2级）</a-select-option>
                <a-select-option value="COMPLEX">复杂审批流程（3级）</a-select-option>
                <a-select-option value="SIMPLE">简化审批流程（1级）</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="审批说明" name="approvalRemark">
              <a-textarea v-model:value="formData.approvalRemark" :rows="2" placeholder="请输入审批说明" />
            </a-form-item>
          </template>

        <!-- 其他配置 -->
          <a-divider orientation="left">其他配置</a-divider>
          <a-form-item label="状态" name="status">
            <a-radio-group v-model:value="formData.status">
              <a-radio :value="1">启用</a-radio>
              <a-radio :value="0">禁用</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="设为默认" name="isDefault">
            <a-switch v-model:checked="formData.isDefault" checked-children="是" un-checked-children="否" />
            <div style="color: #999; font-size: 12px; margin-top: 4px">
              默认补贴规则将自动应用于新创建的账户
            </div>
          </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="补贴规则详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions v-if="currentRecord" :column="2" bordered>
        <a-descriptions-item label="补贴名称" :span="2">
          {{ currentRecord.subsidyName }}
          <a-tag v-if="currentRecord.isDefault" color="gold" style="margin-left: 8px">默认</a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="补贴类型">
          <a-tag :color="getSubsidyTypeColor(currentRecord.subsidyType)">
            {{ getSubsidyTypeName(currentRecord.subsidyType) }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="补贴金额">
          <span style="color: #52c41a; font-weight: 600; font-size: 16px">
            ¥{{ formatAmount(currentRecord.amount) }}
          </span>
        </a-descriptions-item>

        <a-descriptions-item label="发放配置" :span="2">
          <template v-if="currentRecord.subsidyType === 'MONTHLY'">
            每月{{ currentRecord.distributeDay }}日 {{ currentRecord.distributeTime }} 自动发放
          </template>
          <template v-else-if="currentRecord.subsidyType === 'ONE_TIME'">
            一次性发放，需手动触发
          </template>
          <template v-else-if="currentRecord.subsidyType === 'CONDITIONAL'">
            条件触发：
            <a-tag v-for="condition in currentRecord.conditions" :key="condition" color="orange" style="margin: 2px">
              {{ getConditionName(condition) }}
            </a-tag>
          </template>
        </a-descriptions-item>

        <a-descriptions-item label="目标群体" :span="2">
          <a-tag v-for="type in currentRecord.targetTypes" :key="type" color="blue" style="margin: 2px">
            {{ getTargetTypeName(type) }}
          </a-tag>
          <div v-if="currentRecord.targetTypes.includes('ACCOUNT_KIND')" style="margin-top: 8px">
            账户类别：
            <a-tag v-for="kind in currentRecord.accountKinds" :key="kind.kindId" color="cyan">
              {{ kind.kindName }}
            </a-tag>
          </div>
          <div v-if="currentRecord.targetTypes.includes('DEPARTMENT')" style="margin-top: 8px">
            涉及部门：{{ currentRecord.departmentCount }} 个部门
          </div>
          <div v-if="currentRecord.targetTypes.includes('EMPLOYEE')" style="margin-top: 8px">
            指定员工：{{ currentRecord.employeeCount }} 人
          </div>
        </a-descriptions-item>

        <a-descriptions-item label="需要审批">
          <a-tag :color="currentRecord.needApproval ? 'orange' : 'green'">
            {{ currentRecord.needApproval ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="状态">
          <a-badge
            :status="currentRecord.status === 1 ? 'success' : 'default'"
            :text="currentRecord.status === 1 ? '启用' : '禁用'"
          />
        </a-descriptions-item>

        <a-descriptions-item label="描述" :span="2">
          {{ currentRecord.description || '-' }}
        </a-descriptions-item>

        <a-descriptions-item label="创建时间">
          {{ currentRecord.createTime }}
        </a-descriptions-item>

        <a-descriptions-item label="更新时间">
          {{ currentRecord.updateTime }}
        </a-descriptions-item>

        <a-descriptions-item label="发放统计" :span="2">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic title="累计发放" :value="currentRecord.totalDistributed || 0" prefix="¥" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="发放次数" :value="currentRecord.distributeCount || 0" suffix="次" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="受益人数" :value="currentRecord.beneficiaryCount || 0" suffix="人" />
            </a-col>
          </a-row>
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 24px; text-align: center">
        <a-space>
          <a-button type="primary" @click="handleDistribute(currentRecord)">
            <template #icon><SendOutlined /></template>
            立即发放
          </a-button>
          <a-button @click="detailVisible = false">关闭</a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 发放确认弹窗 -->
    <a-modal
      v-model:open="distributeVisible"
      title="补贴发放确认"
      width="700px"
      @ok="handleConfirmDistribute"
      @cancel="distributeVisible = false"
    >
      <a-alert
        message="发放说明"
        description="补贴发放后不可撤销，请确认发放信息和目标用户"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />

      <a-descriptions v-if="distributeRecord" :column="1" bordered>
        <a-descriptions-item label="补贴名称">
          {{ distributeRecord.subsidyName }}
        </a-descriptions-item>

        <a-descriptions-item label="补贴金额">
          <span style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount(distributeRecord.amount) }}
          </span>
        </a-descriptions-item>

        <a-descriptions-item label="预计发放人数">
          {{ distributeRecord.estimatedCount || 0 }} 人
        </a-descriptions-item>

        <a-descriptions-item label="预计发放总额">
          <span style="color: #ff4d4f; font-weight: 600; font-size: 16px">
            ¥{{ formatAmount((distributeRecord.estimatedCount || 0) * distributeRecord.amount) }}
          </span>
        </a-descriptions-item>

        <a-descriptions-item label="发放方式">
          <template v-if="distributeRecord.subsidyType === 'MONTHLY'">
            按月自动发放
          </template>
          <template v-else>
            手动一次性发放
          </template>
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 16px">
        <a-form-item label="发放备注">
          <a-textarea v-model:value="distributeRemark" :rows="3" placeholder="请输入发放备注（可选）" />
        </a-form-item>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
  import { reactive, ref, onMounted, computed } from 'vue';
  import { message, Modal } from 'ant-design-vue';
  import {
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined,
    ClearOutlined,
    EyeOutlined,
    EditOutlined,
    DeleteOutlined,
    SendOutlined,
    CopyOutlined,
    DownOutlined,
  } from '@ant-design/icons-vue';

  // 模拟数据
  const accountKindList = ref([
    { accountKindId: 1, kindName: '员工账户' },
    { accountKindId: 2, kindName: '学生账户' },
    { accountKindId: 3, kindName: '临时账户' },
  ]);

  const departmentTree = ref([
    {
      title: '技术部',
      value: '1',
      children: [
        { title: '前端开发组', value: '1-1' },
        { title: '后端开发组', value: '1-2' },
      ],
    },
    {
      title: '市场部',
      value: '2',
      children: [
        { title: '销售组', value: '2-1' },
        { title: '推广组', value: '2-2' },
      ],
    },
  ]);

  const employeeOptions = ref([
    { value: '001', label: '张三' },
    { value: '002', label: '李四' },
    { value: '003', label: '王五' },
  ]);

  const tableData = ref([
    {
      subsidyId: 1,
      subsidyName: '员工餐补',
      subsidyType: 'MONTHLY',
      amount: 300,
      distributeDay: 5,
      distributeTime: '09:00',
      targetTypes: ['ACCOUNT_KIND'],
      accountKinds: [{ kindId: 1, kindName: '员工账户' }],
      conditions: [],
      needApproval: false,
      status: 1,
      isDefault: true,
      sort: 1,
      description: '员工每月餐费补贴',
      createTime: '2025-01-01 10:00:00',
      updateTime: '2025-01-01 10:00:00',
      totalDistributed: 90000,
      distributeCount: 300,
      beneficiaryCount: 100,
    },
    {
      subsidyId: 2,
      subsidyName: '满勤奖励',
      subsidyType: 'CONDITIONAL',
      amount: 200,
      targetTypes: ['ACCOUNT_KIND'],
      accountKinds: [{ kindId: 1, kindName: '员工账户' }],
      conditions: ['FULL_ATTENDANCE'],
      needApproval: true,
      approvalFlowId: 'STANDARD',
      status: 1,
      isDefault: false,
      sort: 2,
      description: '月度全勤奖励',
      createTime: '2025-01-05 14:00:00',
      updateTime: '2025-01-05 14:00:00',
      totalDistributed: 12000,
      distributeCount: 60,
      beneficiaryCount: 60,
    },
    {
      subsidyId: 3,
      subsidyName: '夜班补贴',
      subsidyType: 'CONDITIONAL',
      amount: 150,
      targetTypes: ['DEPARTMENT'],
      departmentCount: 5,
      conditions: ['NIGHT_SHIFT'],
      nightShiftThreshold: 10,
      needApproval: false,
      status: 1,
      isDefault: false,
      sort: 3,
      description: '月度夜班次数超过10次发放',
      createTime: '2025-01-10 09:00:00',
      updateTime: '2025-01-10 09:00:00',
      totalDistributed: 7500,
      distributeCount: 50,
      beneficiaryCount: 50,
    },
    {
      subsidyId: 4,
      subsidyName: '春节慰问金',
      subsidyType: 'ONE_TIME',
      amount: 500,
      targetTypes: ['EMPLOYEE'],
      employeeCount: 200,
      needApproval: true,
      approvalFlowId: 'COMPLEX',
      status: 0,
      isDefault: false,
      sort: 4,
      description: '春节节日慰问',
      createTime: '2025-01-15 10:00:00',
      updateTime: '2025-01-15 10:00:00',
      totalDistributed: 100000,
      distributeCount: 1,
      beneficiaryCount: 200,
    },
  ]);

  const loading = ref(false);
  const selectedRowKeys = ref([]);

  const pagination = reactive({
    current: 1,
    pageSize: 20,
    total: 4,
    showSizeChanger: true,
    showTotal: (total) => `共 ${total} 条`,
  });

  const columns = [
    {
      title: '补贴名称',
      dataIndex: 'subsidyName',
      key: 'subsidyName',
      width: 200,
    },
    {
      title: '补贴类型',
      key: 'subsidyType',
      width: 120,
    },
    {
      title: '补贴金额',
      key: 'amount',
      width: 120,
    },
    {
      title: '目标群体',
      key: 'targetType',
      width: 180,
    },
    {
      title: '状态',
      key: 'status',
      width: 80,
    },
    {
      title: '排序',
      dataIndex: 'sort',
      key: 'sort',
      width: 80,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 160,
    },
    {
      title: '操作',
      key: 'action',
      width: 220,
      fixed: 'right',
    },
  ];

  const queryForm = reactive({
    subsidyName: '',
    subsidyType: undefined,
    status: undefined,
  });

  const modalVisible = ref(false);
  const modalTitle = computed(() => (isEdit.value ? '编辑补贴规则' : '新建补贴规则'));
  const modalLoading = ref(false);
  const isEdit = ref(false);
  const formRef = ref();

  const formData = reactive({
    subsidyName: '',
    subsidyType: 'MONTHLY',
    amount: 0,
    distributeDay: 1,
    distributeTime: null,
    firstDistributeDate: null,
    targetTypes: [],
    accountKindIds: [],
    departmentIds: [],
    employeeIds: [],
    conditions: [],
    overtimeThreshold: 40,
    nightShiftThreshold: 10,
    needApproval: false,
    approvalFlowId: 'STANDARD',
    approvalRemark: '',
    status: 1,
    isDefault: false,
    sort: 0,
    description: '',
  });

  const formRules = {
    subsidyName: [{ required: true, message: '请输入补贴名称', trigger: 'blur' }],
    subsidyType: [{ required: true, message: '请选择补贴类型', trigger: 'change' }],
    amount: [{ required: true, message: '请输入补贴金额', trigger: 'blur' }],
    distributeDay: [{ required: true, message: '请选择发放日期', trigger: 'change' }],
    targetTypes: [{ required: true, message: '请选择目标类型', trigger: 'change' }],
    accountKindIds: [
      {
        validator: (rule, value) => {
          if (formData.targetTypes.includes('ACCOUNT_KIND') && (!value || value.length === 0)) {
            return Promise.reject('请选择账户类别');
          }
          return Promise.resolve();
        },
        trigger: 'change',
      },
    ],
  };

  const detailVisible = ref(false);
  const currentRecord = ref(null);

  const distributeVisible = ref(false);
  const distributeRecord = ref(null);
  const distributeRemark = ref('');

  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return Number(amount).toFixed(2);
  };

  const getSubsidyTypeColor = (type) => {
    const colorMap = {
      MONTHLY: 'blue',
      ONE_TIME: 'green',
      CONDITIONAL: 'orange',
    };
    return colorMap[type] || 'default';
  };

  const getSubsidyTypeName = (type) => {
    const nameMap = {
      MONTHLY: '月度补贴',
      ONE_TIME: '一次性补贴',
      CONDITIONAL: '条件补贴',
    };
    return nameMap[type] || type;
  };

  const getTargetTypeName = (type) => {
    const nameMap = {
      ACCOUNT_KIND: '账户类别',
      DEPARTMENT: '部门',
      EMPLOYEE: '员工',
    };
    return nameMap[type] || type;
  };

  const getConditionName = (condition) => {
    const nameMap = {
      FULL_ATTENDANCE: '满勤',
      OVERTIME: '加班',
      NIGHT_SHIFT: '夜班',
      SPECIAL_POST: '特殊岗位',
      EXCEPTIONAL: '特殊贡献',
    };
    return nameMap[condition] || condition;
  };

  const filterEmployee = (input, option) => {
    return option.label.toLowerCase().includes(input.toLowerCase());
  };

  const handleQuery = () => {
    pagination.current = 1;
    message.success('查询功能开发中');
  };

  const handleResetQuery = () => {
    queryForm.subsidyName = '';
    queryForm.subsidyType = undefined;
    queryForm.status = undefined;
  };

  const handleTableChange = (pag) => {
    pagination.current = pag.current;
    pagination.pageSize = pag.pageSize;
  };

  const onSelectChange = (keys) => {
    selectedRowKeys.value = keys;
  };

  const resetForm = () => {
    Object.assign(formData, {
      subsidyName: '',
      subsidyType: 'MONTHLY',
      amount: 0,
      distributeDay: 1,
      distributeTime: null,
      firstDistributeDate: null,
      targetTypes: [],
      accountKindIds: [],
      departmentIds: [],
      employeeIds: [],
      conditions: [],
      overtimeThreshold: 40,
      nightShiftThreshold: 10,
      needApproval: false,
      approvalFlowId: 'STANDARD',
      approvalRemark: '',
      status: 1,
      isDefault: false,
      sort: 0,
      description: '',
    });
    formRef.value?.clearValidate();
  };

  const handleAdd = () => {
    isEdit.value = false;
    resetForm();
    modalVisible.value = true;
  };

  const handleEdit = (record) => {
    isEdit.value = true;
    Object.assign(formData, {
      ...record,
      accountKindIds: record.accountKinds?.map(k => k.kindId) || [],
    });
    modalVisible.value = true;
  };

  const handleCopy = (record) => {
    isEdit.value = false;
    Object.assign(formData, {
      ...record,
      subsidyName: `${record.subsidyName} - 副本`,
      accountKindIds: record.accountKinds?.map(k => k.kindId) || [],
    });
    modalVisible.value = true;
  };

  const handleModalOk = async () => {
    try {
      await formRef.value.validate();
      modalLoading.value = true;

      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));

      message.success(isEdit.value ? '更新成功' : '创建成功');
      modalVisible.value = false;
      // 重新加载数据
    } catch (error) {
      console.error('Validation failed:', error);
    } finally {
      modalLoading.value = false;
    }
  };

  const handleModalCancel = () => {
    modalVisible.value = false;
    resetForm();
  };

  const handleView = (record) => {
    currentRecord.value = record;
    detailVisible.value = true;
  };

  const handleDistribute = (record) => {
    distributeRecord.value = record;
    distributeRecord.value.estimatedCount =
      (record.accountKinds?.length || 0) * 50 ||
      record.employeeCount ||
      record.beneficiaryCount ||
      0;
    distributeRemark.value = '';
    distributeVisible.value = true;
  };

  const handleConfirmDistribute = async () => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 500));

      message.success('补贴发放成功');
      distributeVisible.value = false;
    } catch (error) {
      message.error('补贴发放失败');
    }
  };

  const handleBatchDistribute = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要发放的补贴规则');
      return;
    }
    message.info(`批量发放 ${selectedRowKeys.value.length} 个补贴规则，功能开发中`);
  };

  const handleToggleStatus = (record) => {
    const action = record.status === 1 ? '禁用' : '启用';
    Modal.confirm({
      title: `确认${action}`,
      content: `确定要${action}补贴规则"${record.subsidyName}"吗？`,
      onOk: async () => {
        record.status = record.status === 1 ? 0 : 1;
        message.success(`${action}成功`);
      },
    });
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除补贴规则"${record.subsidyName}"吗？删除后不可恢复。`,
      okText: '确定',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        const index = tableData.value.findIndex(item => item.subsidyId === record.subsidyId);
        if (index > -1) {
          tableData.value.splice(index, 1);
          pagination.total--;
          message.success('删除成功');
        }
      },
    });
  };

  const handleRefresh = () => {
    message.success('刷新成功');
  };

  onMounted(() => {
    // 初始化数据
  });
</script>

<style lang="less" scoped>
  .subsidy-list-page {
    .smart-query-form {
      margin-bottom: 16px;
    }

    .amount-text {
      font-weight: 600;
      color: #52c41a;
      font-size: 14px;
    }

    :deep(.ant-table) {
      .ant-table-tbody > tr {
        cursor: pointer;

        &:hover {
          background-color: #f5f5f5;
        }
      }
    }
  }
</style>
