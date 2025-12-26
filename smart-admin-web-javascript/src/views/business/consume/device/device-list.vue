<!--
  * 设备管理模块 - 企业级完整实现
  * 支持设备与区域关联、消费模式绑定
  * 设备状态监控、配置管理
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <div class="device-list-page">
    <a-card :bordered="false">
      <template #title>
        <span>消费设备管理</span>
      </template>

      <template #extra>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <template #icon><PlusOutlined /></template>
            新增设备
          </a-button>
          <a-button @click="handleBatchBind">
            <template #icon><LinkOutlined /></template>
            批量绑定区域
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
          <a-col :span="5">
            <a-form-item label="设备名称">
              <a-input v-model:value="queryForm.deviceName" placeholder="请输入设备名称" />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item label="设备类型">
              <a-select v-model:value="queryForm.deviceType" placeholder="请选择" allow-clear>
                <a-select-option value="POS">POS机</a-select-option>
                <a-select-option value="CONSUME_MACHINE">消费机</a-select-option>
                <a-select-option value="CARD_READER">读卡器</a-select-option>
                <a-select-option value="BIOMETRIC">生物识别设备</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item label="所在区域">
              <a-tree-select
                v-model:value="queryForm.areaId"
                tree-checkable
                :tree-data="areaTree"
                placeholder="请选择区域"
                allow-clear
              />
            </a-form-item>
          </a-col>
          <a-col :span="5">
            <a-form-item label="设备状态">
              <a-select v-model:value="queryForm.status" placeholder="请选择" allow-clear>
                <a-select-option value="ONLINE">在线</a-select-option>
                <a-select-option value="OFFLINE">离线</a-select-option>
                <a-select-option value="FAULT">故障</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="4">
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
        row-key="deviceId"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'deviceName'">
            <div style="display: flex; align-items: center">
              <span style="font-weight: 500">{{ record.deviceName }}</span>
              <a-tag v-if="record.isDefault" color="gold" style="margin-left: 8px">主设备</a-tag>
            </div>
          </template>

          <template v-else-if="column.key === 'deviceType'">
            <a-tag :color="getDeviceTypeColor(record.deviceType)">
              {{ getDeviceTypeName(record.deviceType) }}
            </a-tag>
          </template>

          <template v-else-if="column.key === 'areaName'">
            <span v-if="record.areaName">{{ record.areaName }}</span>
            <a-tag v-else color="orange">未绑定</a-tag>
          </template>

          <template v-else-if="column.key === 'consumeMode'">
            <a-tag v-if="record.consumeMode" color="blue">
              {{ getConsumeModeName(record.consumeMode) }}
            </a-tag>
            <span v-else style="color: #999">未配置</span>
          </template>

          <template v-else-if="column.key === 'status'">
            <a-badge
              :status="getStatusBadge(record.status)"
              :text="getStatusText(record.status)"
            />
          </template>

          <template v-else-if="column.key === 'offlineMode'">
            <a-tag v-if="record.offlineEnabled" color="green">
              已启用
            </a-tag>
            <a-tag v-else color="default">
              未启用
            </a-tag>
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
              <a-button type="link" size="small" @click="handleConfig(record)">
                <template #icon><SettingOutlined /></template>
                配置
              </a-button>
              <a-dropdown>
                <template #overlay>
                  <a-menu>
                    <a-menu-item @click="handleBindArea(record)">
                      <LinkOutlined /> 绑定区域
                    </a-menu-item>
                    <a-menu-item @click="handleToggleStatus(record)">
                      {{ record.status === 'ONLINE' ? '标记离线' : '标记在线' }}
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
      width="800px"
      :confirm-loading="modalLoading"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <!-- 基本信息 -->
          <a-divider orientation="left">基本信息</a-divider>
          <a-form-item label="设备编号" name="deviceCode">
            <a-input v-model:value="formData.deviceCode" placeholder="请输入设备编号（唯一标识）" />
          </a-form-item>

          <a-form-item label="设备名称" name="deviceName">
            <a-input v-model:value="formData.deviceName" placeholder="请输入设备名称" />
          </a-form-item>

          <a-form-item label="设备类型" name="deviceType">
            <a-select v-model:value="formData.deviceType" placeholder="请选择设备类型">
              <a-select-option value="POS">POS机 - 支持多种支付方式</a-select-option>
              <a-select-option value="CONSUME_MACHINE">消费机 - 简易消费终端</a-select-option>
              <a-select-option value="CARD_READER">读卡器 - 仅读取卡片信息</a-select-option>
              <a-select-option value="BIOMETRIC">生物识别设备 - 人脸/指纹识别</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item label="设备型号" name="deviceModel">
            <a-input v-model:value="formData.deviceModel" placeholder="请输入设备型号" />
          </a-form-item>

          <a-form-item label="安装位置" name="location">
            <a-input v-model:value="formData.location" placeholder="请输入安装位置描述" />
          </a-form-item>

          <a-form-item label="IP地址" name="ipAddress">
            <a-input v-model:value="formData.ipAddress" placeholder="请输入设备IP地址" />
          </a-form-item>

          <a-form-item label="端口" name="port">
            <a-input-number v-model:value="formData.port" :min="1" :max="65535" style="width: 100%" />
          </a-form-item>

        <!-- 区域绑定 -->
          <a-divider orientation="left">区域绑定</a-divider>
          <a-form-item label="绑定区域" name="areaId">
            <a-tree-select
              v-model:value="formData.areaId"
              :tree-data="areaTree"
              placeholder="请选择设备所在区域"
              allow-clear
            />
          </a-form-item>

          <a-form-item label="设为主设备" name="isDefault">
            <a-switch v-model:checked="formData.isDefault" checked-children="是" un-checked-children="否" />
            <div style="color: #999; font-size: 12px; margin-top: 4px">
              主设备将作为区域的默认消费设备
            </div>
          </a-form-item>

        <!-- 消费配置 -->
          <a-divider orientation="left">消费配置</a-divider>
          <a-form-item label="消费模式" name="consumeMode">
            <a-select v-model:value="formData.consumeMode" placeholder="请选择消费模式">
              <a-select-option value="FIXED_AMOUNT">固定金额模式</a-select-option>
              <a-select-option value="FREE_AMOUNT">自由金额模式</a-select-option>
              <a-select-option value="METERED">计量计费模式</a-select-option>
              <a-select-option value="PRODUCT">商品模式</a-select-option>
              <a-select-option value="ORDER">订餐模式</a-select-option>
              <a-select-option value="INTELLIGENCE">智能模式</a-select-option>
            </a-select>
          </a-form-item>

          <a-form-item v-if="formData.consumeMode === 'FIXED_AMOUNT'" label="固定金额" name="fixedAmount">
            <a-input-number
              v-model:value="formData.fixedAmount"
              :min="0"
              :max="999999"
              :precision="2"
              :step="0.01"
              style="width: 100%"
              placeholder="请输入固定金额"
            >
              <template #prefix>¥</template>
            </a-input-number>
          </a-form-item>

          <a-form-item label="启用离线" name="offlineEnabled">
            <a-switch v-model:checked="formData.offlineEnabled" checked-children="是" un-checked-children="否" />
            <div style="color: #999; font-size: 12px; margin-top: 4px">
              启用后设备支持离线消费，网络恢复后自动上传
            </div>
          </a-form-item>

          <template v-if="formData.offlineEnabled">
            <a-form-item label="离线白名单" name="offlineWhitelist">
              <a-select
                v-model:value="formData.offlineWhitelist"
                mode="multiple"
                placeholder="请选择允许离线消费的账户类别"
                style="width: 100%"
              >
                <a-select-option v-for="kind in accountKindList" :key="kind.accountKindId" :value="kind.accountKindId">
                  {{ kind.kindName }}
                </a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="离线固定额" name="offlineFixedAmount">
              <a-input-number
                v-model:value="formData.offlineFixedAmount"
                :min="0"
                :max="999999"
                :precision="2"
                style="width: 100%"
                placeholder="离线时的固定消费金额"
              >
                <template #prefix>¥</template>
              </a-input-number>
            </a-form-item>

            <a-form-item label="最大离线笔数" name="maxOfflineCount">
              <a-input-number
                v-model:value="formData.maxOfflineCount"
                :min="0"
                :max="1000"
                style="width: 100%"
                placeholder="设备最多存储离线记录数"
              />
            </a-form-item>
          </template>

        <!-- 其他配置 -->
          <a-divider orientation="left">其他配置</a-divider>
          <a-form-item label="打印小票" name="printReceipt">
            <a-switch v-model:checked="formData.printReceipt" checked-children="是" un-checked-children="否" />
          </a-form-item>

          <a-form-item label="语音提示" name="voicePrompt">
            <a-switch v-model:checked="formData.voicePrompt" checked-children="是" un-checked-children="否" />
          </a-form-item>

          <a-form-item label="设备状态" name="status">
            <a-radio-group v-model:value="formData.status">
              <a-radio value="ONLINE">在线</a-radio>
              <a-radio value="OFFLINE">离线</a-radio>
              <a-radio value="FAULT">故障</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="备注" name="remark">
            <a-textarea v-model:value="formData.remark" :rows="3" placeholder="请输入备注信息" />
          </a-form-item>
      </a-form>
    </a-modal>

    <!-- 查看详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="设备详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions v-if="currentRecord" :column="2" bordered>
        <a-descriptions-item label="设备名称" :span="2">
          {{ currentRecord.deviceName }}
          <a-tag v-if="currentRecord.isDefault" color="gold" style="margin-left: 8px">主设备</a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="设备编号">
          {{ currentRecord.deviceCode }}
        </a-descriptions-item>

        <a-descriptions-item label="设备类型">
          <a-tag :color="getDeviceTypeColor(currentRecord.deviceType)">
            {{ getDeviceTypeName(currentRecord.deviceType) }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="设备型号">
          {{ currentRecord.deviceModel || '-' }}
        </a-descriptions-item>

        <a-descriptions-item label="设备状态">
          <a-badge
            :status="getStatusBadge(currentRecord.status)"
            :text="getStatusText(currentRecord.status)"
          />
        </a-descriptions-item>

        <a-descriptions-item label="所在区域" :span="2">
          <a-tag v-if="currentRecord.areaName" color="blue">{{ currentRecord.areaName }}</a-tag>
          <a-tag v-else color="orange">未绑定区域</a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="消费模式">
          <a-tag v-if="currentRecord.consumeMode" color="blue">
            {{ getConsumeModeName(currentRecord.consumeMode) }}
          </a-tag>
          <span v-else style="color: #999">未配置</span>
        </a-descriptions-item>

        <a-descriptions-item label="固定金额">
          <span v-if="currentRecord.fixedAmount" style="color: #52c41a; font-weight: 600">
            ¥{{ formatAmount(currentRecord.fixedAmount) }}
          </span>
          <span v-else>-</span>
        </a-descriptions-item>

        <a-descriptions-item label="离线模式">
          <a-tag :color="currentRecord.offlineEnabled ? 'green' : 'default'">
            {{ currentRecord.offlineEnabled ? '已启用' : '未启用' }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="网络配置">
          {{ currentRecord.ipAddress || '-' }}:{{ currentRecord.port || '-' }}
        </a-descriptions-item>

        <a-descriptions-item label="安装位置" :span="2">
          {{ currentRecord.location || '-' }}
        </a-descriptions-item>

        <a-descriptions-item label="打印小票">
          <a-tag :color="currentRecord.printReceipt ? 'blue' : 'default'">
            {{ currentRecord.printReceipt ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="语音提示">
          <a-tag :color="currentRecord.voicePrompt ? 'blue' : 'default'">
            {{ currentRecord.voicePrompt ? '是' : '否' }}
          </a-tag>
        </a-descriptions-item>

        <a-descriptions-item label="备注" :span="2">
          {{ currentRecord.remark || '-' }}
        </a-descriptions-item>

        <a-descriptions-item label="创建时间">
          {{ currentRecord.createTime }}
        </a-descriptions-item>

        <a-descriptions-item label="更新时间">
          {{ currentRecord.updateTime }}
        </a-descriptions-item>

        <!-- 设备统计 -->
        <a-descriptions-item label="设备统计" :span="2">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic title="今日交易" :value="currentRecord.todayTransactions || 0" suffix="笔" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="今日金额" :value="currentRecord.todayAmount || 0" prefix="¥" />
            </a-col>
            <a-col :span="8">
              <a-statistic title="离线记录" :value="currentRecord.offlineRecordCount || 0" suffix="条" />
            </a-col>
          </a-row>
        </a-descriptions-item>
      </a-descriptions>

      <div style="margin-top: 24px; text-align: center">
        <a-space>
          <a-button type="primary" @click="handleEdit(currentRecord)">
            <template #icon><EditOutlined /></template>
            编辑设备
          </a-button>
          <a-button @click="detailVisible = false">关闭</a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 区域绑定弹窗 -->
    <a-modal
      v-model:open="bindAreaVisible"
      title="绑定区域"
      width="500px"
      @ok="handleConfirmBindArea"
      @cancel="bindAreaVisible = false"
    >
      <a-form :label-col="{ span: 6 }" :wrapper-col="{ span: 18 }">
        <a-form-item label="选择区域">
          <a-tree-select
            v-model:value="bindAreaId"
            :tree-data="areaTree"
            placeholder="请选择要绑定的区域"
          />
        </a-form-item>
        <a-form-item label="设为主设备">
          <a-switch v-model:checked="bindAsDefault" checked-children="是" un-checked-children="否" />
        </a-form-item>
      </a-form>
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
    SettingOutlined,
    LinkOutlined,
    DownOutlined,
  } from '@ant-design/icons-vue';

  // 模拟数据
  const areaTree = ref([
    {
      title: '第一食堂',
      value: '1',
      children: [
        { title: '一楼主食区', value: '1-1' },
        { title: '一楼副食区', value: '1-2' },
        { title: '二楼风味区', value: '1-3' },
      ],
    },
    {
      title: '第二食堂',
      value: '2',
      children: [
        { title: '清真餐厅', value: '2-1' },
        { title: '自助餐厅', value: '2-2' },
      ],
    },
    {
      title: '超市',
      value: '3',
      children: [
        { title: '一楼超市', value: '3-1' },
        { title: '二楼便利店', value: '3-2' },
      ],
    },
  ]);

  const accountKindList = ref([
    { accountKindId: 1, kindName: '员工账户' },
    { accountKindId: 2, kindName: '学生账户' },
    { accountKindId: 3, kindName: '临时账户' },
  ]);

  const tableData = ref([
    {
      deviceId: 1,
      deviceCode: 'POS-001',
      deviceName: '一楼主食区POS机',
      deviceType: 'POS',
      deviceModel: 'SmartPOS-2000',
      areaId: '1-1',
      areaName: '第一食堂-一楼主食区',
      consumeMode: 'FIXED_AMOUNT',
      fixedAmount: 15,
      ipAddress: '192.168.1.101',
      port: 8080,
      location: '一楼主食区收银台',
      offlineEnabled: true,
      offlineWhitelist: [1, 2],
      offlineFixedAmount: 15,
      maxOfflineCount: 100,
      printReceipt: true,
      voicePrompt: true,
      status: 'ONLINE',
      isDefault: true,
      remark: '一楼主食区主设备',
      createTime: '2025-01-01 10:00:00',
      updateTime: '2025-01-20 15:30:00',
      todayTransactions: 156,
      todayAmount: 2340,
      offlineRecordCount: 0,
    },
    {
      deviceId: 2,
      deviceCode: 'CM-001',
      deviceName: '二楼消费机01',
      deviceType: 'CONSUME_MACHINE',
      deviceModel: 'Consume-100',
      areaId: '1-3',
      areaName: '第一食堂-二楼风味区',
      consumeMode: 'FREE_AMOUNT',
      ipAddress: '192.168.1.102',
      port: 8081,
      location: '二楼风味区东侧',
      offlineEnabled: false,
      printReceipt: false,
      voicePrompt: true,
      status: 'ONLINE',
      isDefault: false,
      remark: '二楼风味区辅助设备',
      createTime: '2025-01-05 14:00:00',
      updateTime: '2025-01-20 15:30:00',
      todayTransactions: 89,
      todayAmount: 1567,
      offlineRecordCount: 0,
    },
    {
      deviceId: 3,
      deviceCode: 'BIO-001',
      deviceName: '人脸识别消费机',
      deviceType: 'BIOMETRIC',
      deviceModel: 'BioPay-Pro',
      areaId: '2-1',
      areaName: '第二食堂-清真餐厅',
      consumeMode: 'FIXED_AMOUNT',
      fixedAmount: 20,
      ipAddress: '192.168.1.103',
      port: 8082,
      location: '清真餐厅入口',
      offlineEnabled: true,
      offlineWhitelist: [1, 2, 3],
      offlineFixedAmount: 20,
      maxOfflineCount: 200,
      printReceipt: true,
      voicePrompt: true,
      status: 'ONLINE',
      isDefault: true,
      remark: '支持人脸识别支付',
      createTime: '2025-01-10 09:00:00',
      updateTime: '2025-01-20 15:30:00',
      todayTransactions: 234,
      todayAmount: 4680,
      offlineRecordCount: 0,
    },
    {
      deviceId: 4,
      deviceCode: 'POS-002',
      deviceName: '一楼超市POS',
      deviceType: 'POS',
      deviceModel: 'SmartPOS-2000',
      areaId: '3-1',
      areaName: '超市-一楼超市',
      consumeMode: 'PRODUCT',
      ipAddress: '192.168.1.104',
      port: 8083,
      location: '一楼超市收银台',
      offlineEnabled: true,
      offlineWhitelist: [1, 2],
      offlineFixedAmount: 0,
      maxOfflineCount: 50,
      printReceipt: true,
      voicePrompt: false,
      status: 'OFFLINE',
      isDefault: true,
      remark: '超市主设备，网络故障',
      createTime: '2025-01-15 10:00:00',
      updateTime: '2025-01-21 09:00:00',
      todayTransactions: 45,
      todayAmount: 1234,
      offlineRecordCount: 8,
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
      title: '设备名称',
      key: 'deviceName',
      width: 180,
    },
    {
      title: '设备编号',
      dataIndex: 'deviceCode',
      key: 'deviceCode',
      width: 120,
    },
    {
      title: '设备类型',
      key: 'deviceType',
      width: 120,
    },
    {
      title: '所在区域',
      key: 'areaName',
      width: 160,
    },
    {
      title: '消费模式',
      key: 'consumeMode',
      width: 120,
    },
    {
      title: 'IP地址',
      dataIndex: 'ipAddress',
      key: 'ipAddress',
      width: 130,
    },
    {
      title: '设备状态',
      key: 'status',
      width: 80,
    },
    {
      title: '离线模式',
      key: 'offlineMode',
      width: 90,
    },
    {
      title: '操作',
      key: 'action',
      width: 200,
      fixed: 'right',
    },
  ];

  const queryForm = reactive({
    deviceName: '',
    deviceType: undefined,
    areaId: undefined,
    status: undefined,
  });

  const modalVisible = ref(false);
  const modalTitle = computed(() => (isEdit.value ? '编辑设备' : '新增设备'));
  const modalLoading = ref(false);
  const isEdit = ref(false);
  const formRef = ref();

  const formData = reactive({
    deviceCode: '',
    deviceName: '',
    deviceType: 'POS',
    deviceModel: '',
    location: '',
    ipAddress: '',
    port: 8080,
    areaId: undefined,
    isDefault: false,
    consumeMode: 'FIXED_AMOUNT',
    fixedAmount: 0,
    offlineEnabled: false,
    offlineWhitelist: [],
    offlineFixedAmount: 0,
    maxOfflineCount: 100,
    printReceipt: false,
    voicePrompt: true,
    status: 'ONLINE',
    remark: '',
  });

  const formRules = {
    deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
    deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
    deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
    ipAddress: [
      { required: true, message: '请输入IP地址', trigger: 'blur' },
      { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: 'IP地址格式不正确', trigger: 'blur' },
    ],
    port: [{ required: true, message: '请输入端口号', trigger: 'blur' }],
  };

  const detailVisible = ref(false);
  const currentRecord = ref(null);

  const bindAreaVisible = ref(false);
  const bindAreaRecord = ref(null);
  const bindAreaId = ref(undefined);
  const bindAsDefault = ref(false);

  const formatAmount = (amount) => {
    if (!amount) return '0.00';
    return Number(amount).toFixed(2);
  };

  const getDeviceTypeColor = (type) => {
    const colorMap = {
      POS: 'blue',
      CONSUME_MACHINE: 'green',
      CARD_READER: 'orange',
      BIOMETRIC: 'purple',
    };
    return colorMap[type] || 'default';
  };

  const getDeviceTypeName = (type) => {
    const nameMap = {
      POS: 'POS机',
      CONSUME_MACHINE: '消费机',
      CARD_READER: '读卡器',
      BIOMETRIC: '生物识别',
    };
    return nameMap[type] || type;
  };

  const getConsumeModeName = (mode) => {
    const nameMap = {
      FIXED_AMOUNT: '固定金额',
      FREE_AMOUNT: '自由金额',
      METERED: '计量计费',
      PRODUCT: '商品模式',
      ORDER: '订餐模式',
      INTELLIGENCE: '智能模式',
    };
    return nameMap[mode] || mode;
  };

  const getStatusBadge = (status) => {
    const badgeMap = {
      ONLINE: 'success',
      OFFLINE: 'default',
      FAULT: 'error',
    };
    return badgeMap[status] || 'default';
  };

  const getStatusText = (status) => {
    const textMap = {
      ONLINE: '在线',
      OFFLINE: '离线',
      FAULT: '故障',
    };
    return textMap[status] || status;
  };

  const handleQuery = () => {
    pagination.current = 1;
    message.success('查询功能开发中');
  };

  const handleResetQuery = () => {
    queryForm.deviceName = '';
    queryForm.deviceType = undefined;
    queryForm.areaId = undefined;
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
      deviceCode: '',
      deviceName: '',
      deviceType: 'POS',
      deviceModel: '',
      location: '',
      ipAddress: '',
      port: 8080,
      areaId: undefined,
      isDefault: false,
      consumeMode: 'FIXED_AMOUNT',
      fixedAmount: 0,
      offlineEnabled: false,
      offlineWhitelist: [],
      offlineFixedAmount: 0,
      maxOfflineCount: 100,
      printReceipt: false,
      voicePrompt: true,
      status: 'ONLINE',
      remark: '',
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
    Object.assign(formData, record);
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

  const handleConfig = (record) => {
    message.info(`配置设备 ${record.deviceName}，功能开发中`);
  };

  const handleBindArea = (record) => {
    bindAreaRecord.value = record;
    bindAreaId.value = record.areaId;
    bindAsDefault.value = record.isDefault;
    bindAreaVisible.value = true;
  };

  const handleConfirmBindArea = async () => {
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 300));

      if (bindAreaRecord.value) {
        bindAreaRecord.value.areaId = bindAreaId.value;
        bindAreaRecord.value.isDefault = bindAsDefault.value;
      }

      message.success('区域绑定成功');
      bindAreaVisible.value = false;
    } catch (error) {
      message.error('区域绑定失败');
    }
  };

  const handleBatchBind = () => {
    if (selectedRowKeys.value.length === 0) {
      message.warning('请先选择要绑定的设备');
      return;
    }
    message.info(`批量绑定 ${selectedRowKeys.value.length} 个设备，功能开发中`);
  };

  const handleToggleStatus = (record) => {
    const action = record.status === 'ONLINE' ? '标记离线' : '标记在线';
    Modal.confirm({
      title: `确认${action}`,
      content: `确定要${action}设备"${record.deviceName}"吗？`,
      onOk: async () => {
        record.status = record.status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
        message.success(`${action}成功`);
      },
    });
  };

  const handleDelete = (record) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除设备"${record.deviceName}"吗？删除后不可恢复。`,
      okText: '确定',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        const index = tableData.value.findIndex(item => item.deviceId === record.deviceId);
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
  .device-list-page {
    .smart-query-form {
      margin-bottom: 16px;
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
