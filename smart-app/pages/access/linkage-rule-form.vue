<!--
  * 移动端联动规则表单页面
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-12-01
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->
<template>
  <view class="rule-form">
    <!-- 头部状态栏 -->
    <view class="status-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="status-content">
        <view class="back-btn" @click="goBack">
          <text class="back-icon">‹</text>
        </view>
        <text class="page-title">{{ isEdit ? '编辑规则' : '添加规则' }}</text>
        <text class="save-btn" @click="saveRule">保存</text>
      </view>
    </view>

    <!-- 表单内容 -->
    <scroll-view class="form-content" scroll-y>
      <!-- 基本信息 -->
      <view class="form-section">
        <view class="section-title">基本信息</view>

        <view class="form-item">
          <view class="form-label">规则名称 *</view>
          <input
            v-model="formData.ruleName"
            class="form-input"
            placeholder="请输入规则名称"
            :maxlength="50"
          />
        </view>

        <view class="form-item">
          <view class="form-label">规则类型 *</view>
          <picker
            :value="typeIndex"
            :range="ruleTypeOptions"
            range-key="label"
            @change="onTypeChange"
          >
            <view class="picker-input">
              {{ selectedTypeLabel }}
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <view class="form-label">执行顺序</view>
          <input
            v-model="formData.executionOrder"
            class="form-input"
            type="number"
            placeholder="执行顺序，数字越小优先级越高"
          />
        </view>

        <view class="form-item">
          <view class="form-label">延迟执行(ms)</view>
          <input
            v-model="formData.delayMs"
            class="form-input"
            type="number"
            placeholder="延迟执行时间（毫秒）"
          />
        </view>

        <view class="form-item">
          <view class="form-label">规则描述</view>
          <textarea
            v-model="formData.description"
            class="form-textarea"
            placeholder="请输入规则描述"
            :maxlength="200"
          />
        </view>
      </view>

      <!-- 触发条件 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-title">触发条件</view>
          <text class="add-btn" @click="addCondition">+ 添加条件</text>
        </view>

        <view
          v-for="(condition, index) in triggerConditions"
          :key="index"
          class="condition-card"
        >
          <view class="condition-header">
            <text class="condition-index">条件 {{ index + 1 }}</text>
            <text
              class="remove-btn"
              @click="removeCondition(index)"
              v-if="triggerConditions.length > 1"
            >删除</text>
          </view>

          <view class="form-item">
            <view class="form-label">字段</view>
            <picker
              :value="condition.fieldIndex"
              :range="fieldOptions"
              range-key="label"
              @change="onFieldChange($event, index)"
            >
              <view class="picker-input">
                {{ getFieldLabel(condition.field) }}
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="form-item">
            <view class="form-label">操作符</view>
            <picker
              :value="condition.operatorIndex"
              :range="operatorOptions"
              range-key="label"
              @change="onOperatorChange($event, index)"
            >
              <view class="picker-input">
                {{ getOperatorLabel(condition.operator) }}
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="form-item">
            <view class="form-label">值</view>
            <input
              v-model="condition.value"
              class="form-input"
              placeholder="请输入条件值"
            />
          </view>
        </view>

        <view class="form-item">
          <view class="form-label">条件关系</view>
          <radio-group @change="onRelationChange">
            <view class="radio-group">
              <label class="radio-item">
                <radio value="AND" :checked="formData.conditionRelation === 'AND'" />
                <text class="radio-text">且（AND）- 所有条件都满足</text>
              </label>
              <label class="radio-item">
                <radio value="OR" :checked="formData.conditionRelation === 'OR'" />
                <text class="radio-text">或（OR）- 任一条件满足</text>
              </label>
            </view>
          </radio-group>
        </view>
      </view>

      <!-- 联动动作 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-title">联动动作</view>
          <text class="add-btn" @click="addAction">+ 添加动作</text>
        </view>

        <view
          v-for="(action, index) in linkageActions"
          :key="index"
          class="action-card"
        >
          <view class="action-header">
            <text class="action-index">动作 {{ index + 1 }}</text>
            <text
              class="remove-btn"
              @click="removeAction(index)"
              v-if="linkageActions.length > 1"
            >删除</text>
          </view>

          <view class="form-item">
            <view class="form-label">动作类型</view>
            <picker
              :value="action.typeIndex"
              :range="actionTypeOptions"
              range-key="label"
              @change="onActionTypeChange($event, index)"
            >
              <view class="picker-input">
                {{ getActionTypeLabel(action.type) }}
                <text class="picker-arrow">›</text>
              </view>
            </picker>
          </view>

          <view class="form-item">
            <view class="form-label">动作参数</view>
            <textarea
              v-model="action.parameters"
              class="form-textarea"
              placeholder="动作参数（JSON格式）"
            />
          </view>
        </view>
      </view>

      <!-- 联动设备 -->
      <view class="form-section">
        <view class="section-header">
          <view class="section-title">联动设备</view>
          <text class="selected-count">已选 {{ selectedDevices.length }} 个</text>
        </view>

        <view class="device-list">
          <view
            v-for="device in availableDevices"
            :key="device.deviceId"
            class="device-item"
          >
            <view class="device-info">
              <view class="device-name">{{ device.deviceName }}</view>
              <view class="device-location">{{ device.location }}</view>
            </view>
            <checkbox
              :value="device.deviceId"
              :checked="selectedDevices.includes(device.deviceId)"
              @change="onDeviceChange(device.deviceId, $event)"
            />
          </view>
        </view>
      </view>

      <!-- 执行设置 -->
      <view class="form-section">
        <view class="section-title">执行设置</view>

        <view class="form-item">
          <view class="form-label">最大执行次数</view>
          <input
            v-model="formData.maxExecutions"
            class="form-input"
            type="number"
            placeholder="-1表示无限制"
          />
        </view>

        <view class="form-item">
          <view class="form-label">执行间隔(秒)</view>
          <input
            v-model="formData.executionInterval"
            class="form-input"
            type="number"
            placeholder="执行间隔时间（秒）"
          />
        </view>

        <view class="form-item">
          <view class="form-label">启用状态</view>
          <switch
            :checked="formData.status === 'ACTIVE'"
            @change="onStatusChange"
            color="#667eea"
          />
        </view>
      </view>
    </scroll-view>

    <!-- 底部安全区域 -->
    <view class="safe-area-bottom"></view>
  </view>
</template>

<script>
import { getMobileLinkageRules } from '@/api/access';

export default {
  name: 'LinkageRuleForm',
  data() {
    return {
      statusBarHeight: 0,
      isEdit: false,
      ruleId: null,

      // 表单数据
      formData: {
        ruleName: '',
        ruleType: '',
        description: '',
        executionOrder: 1,
        delayMs: 0,
        conditionRelation: 'AND',
        maxExecutions: -1,
        executionInterval: 60,
        status: 'ACTIVE',
      },

      // 触发条件
      triggerConditions: [
        { field: '', operator: '', value: '', fieldIndex: 0, operatorIndex: 0 }
      ],

      // 联动动作
      linkageActions: [
        { type: '', parameters: '', typeIndex: 0 }
      ],

      // 选项数据
      ruleTypeOptions: [
        { value: 'DEVICE_STATUS', label: '设备状态联动' },
        { value: 'ACCESS_RESULT', label: '通行结果联动' },
        { value: 'TIME_BASED', label: '时间触发联动' },
        { value: 'EMERGENCY', label: '紧急情况联动' },
      ],

      fieldOptions: [
        { value: 'deviceStatus', label: '设备状态' },
        { value: 'accessResult', label: '通行结果' },
        { value: 'timeCondition', label: '时间条件' },
        { value: 'emergencyLevel', label: '紧急级别' },
      ],

      operatorOptions: [
        { value: 'eq', label: '等于' },
        { value: 'ne', label: '不等于' },
        { value: 'gt', label: '大于' },
        { value: 'lt', label: '小于' },
        { value: 'in', label: '包含' },
        { value: 'not_in', label: '不包含' },
      ],

      actionTypeOptions: [
        { value: 'openDoor', label: '远程开门' },
        { value: 'closeDoor', label: '远程关门' },
        { value: 'lockDevice', label: '锁定设备' },
        { value: 'unlockDevice', label: '解锁设备' },
        { value: 'sendAlert', label: '发送告警' },
        { value: 'startRecording', label: '开始录像' },
        { value: 'triggerAlarm', label: '触发警报' },
      ],

      // 可用设备
      availableDevices: [],
      selectedDevices: [],
    };
  },

  computed: {
    /**
     * 选中类型的索引
     */
    typeIndex() {
      return this.ruleTypeOptions.findIndex(option => option.value === this.formData.ruleType);
    },

    /**
     * 选中类型的标签
     */
    selectedTypeLabel() {
      const option = this.ruleTypeOptions[this.typeIndex];
      return option ? option.label : '请选择规则类型';
    },
  },

  onLoad(options) {
    const systemInfo = uni.getSystemInfoSync();
    this.statusBarHeight = systemInfo.statusBarHeight || 44;

    if (options.id) {
      this.isEdit = true;
      this.ruleId = options.id;
      this.loadRuleDetail();
    }

    this.loadAvailableDevices();
  },

  methods: {
    /**
     * 加载规则详情
     */
    async loadRuleDetail() {
      try {
        // 规则详情API - 待后端接口完成后对接
        console.log('加载规则详情:', this.ruleId);
      } catch (error) {
        console.error('加载规则详情失败:', error);
      }
    },

    /**
     * 加载可用设备
     */
    async loadAvailableDevices() {
      try {
        // 模拟设备数据
        this.availableDevices = [
          { deviceId: '1', deviceName: '前门禁1', location: '大楼前门' },
          { deviceId: '2', deviceName: '侧门禁2', location: '大楼侧门' },
          { deviceId: '3', deviceName: '后门禁3', location: '大楼后门' },
        ];
      } catch (error) {
        console.error('加载设备列表失败:', error);
      }
    },

    /**
     * 类型改变
     */
    onTypeChange(e) {
      const index = e.detail.value;
      this.formData.ruleType = this.ruleTypeOptions[index].value;
    },

    /**
     * 字段改变
     */
    onFieldChange(e, index) {
      const fieldIndex = e.detail.value;
      this.triggerConditions[index].field = this.fieldOptions[fieldIndex].value;
      this.triggerConditions[index].fieldIndex = fieldIndex;
    },

    /**
     * 操作符改变
     */
    onOperatorChange(e, index) {
      const operatorIndex = e.detail.value;
      this.triggerConditions[index].operator = this.operatorOptions[operatorIndex].value;
      this.triggerConditions[index].operatorIndex = operatorIndex;
    },

    /**
     * 动作类型改变
     */
    onActionTypeChange(e, index) {
      const typeIndex = e.detail.value;
      this.linkageActions[index].type = this.actionTypeOptions[typeIndex].value;
      this.linkageActions[index].typeIndex = typeIndex;
    },

    /**
     * 条件关系改变
     */
    onRelationChange(e) {
      this.formData.conditionRelation = e.detail.value;
    },

    /**
     * 状态改变
     */
    onStatusChange(e) {
      this.formData.status = e.detail.value ? 'ACTIVE' : 'INACTIVE';
    },

    /**
     * 设备选择改变
     */
    onDeviceChange(deviceId, e) {
      if (e.detail.value.length > 0 && !this.selectedDevices.includes(deviceId)) {
        this.selectedDevices.push(deviceId);
      } else if (!e.detail.value.length) {
        const index = this.selectedDevices.indexOf(deviceId);
        if (index > -1) {
          this.selectedDevices.splice(index, 1);
        }
      }
    },

    /**
     * 添加条件
     */
    addCondition() {
      this.triggerConditions.push({
        field: '',
        operator: '',
        value: '',
        fieldIndex: 0,
        operatorIndex: 0,
      });
    },

    /**
     * 移除条件
     */
    removeCondition(index) {
      this.triggerConditions.splice(index, 1);
    },

    /**
     * 添加动作
     */
    addAction() {
      this.linkageActions.push({
        type: '',
        parameters: '',
        typeIndex: 0,
      });
    },

    /**
     * 移除动作
     */
    removeAction(index) {
      this.linkageActions.splice(index, 1);
    },

    /**
     * 获取字段标签
     */
    getFieldLabel(field) {
      const option = this.fieldOptions.find(opt => opt.value === field);
      return option ? option.label : '请选择字段';
    },

    /**
     * 获取操作符标签
     */
    getOperatorLabel(operator) {
      const option = this.operatorOptions.find(opt => opt.value === operator);
      return option ? option.label : '请选择操作符';
    },

    /**
     * 获取动作类型标签
     */
    getActionTypeLabel(type) {
      const option = this.actionTypeOptions.find(opt => opt.value === type);
      return option ? option.label : '请选择动作类型';
    },

    /**
     * 保存规则
     */
    async saveRule() {
      // 表单验证
      if (!this.formData.ruleName.trim()) {
        uni.showToast({
          title: '请输入规则名称',
          icon: 'none',
        });
        return;
      }

      if (!this.formData.ruleType) {
        uni.showToast({
          title: '请选择规则类型',
          icon: 'none',
        });
        return;
      }

      // 验证触发条件
      const invalidCondition = this.triggerConditions.find(
        cond => !cond.field || !cond.operator || !cond.value
      );

      if (invalidCondition) {
        uni.showToast({
          title: '请完善触发条件',
          icon: 'none',
        });
        return;
      }

      // 验证联动动作
      const invalidAction = this.linkageActions.find(
        action => !action.type
      );

      if (invalidAction) {
        uni.showToast({
          title: '请完善联动动作',
          icon: 'none',
        });
        return;
      }

      try {
        uni.showLoading({
          title: '保存中...',
        });

        // 构建提交数据
        const submitData = {
          ...this.formData,
          triggerCondition: JSON.stringify({
            conditions: this.triggerConditions.map(cond => ({
              field: cond.field,
              operator: cond.operator,
              value: cond.value,
            })),
            relation: this.formData.conditionRelation,
          }),
          linkageAction: JSON.stringify({
            actions: this.linkageActions.map(action => ({
              type: action.type,
              parameters: action.parameters,
            })),
          }),
          linkageDevices: this.selectedDevices.map(deviceId => ({ deviceId })),
        };

        // TODO: 调用保存API
        console.log('保存规则数据:', submitData);

        // 模拟保存
        await new Promise(resolve => setTimeout(resolve, 1000));

        uni.hideLoading();
        uni.showToast({
          title: this.isEdit ? '更新成功' : '添加成功',
          icon: 'success',
        });

        // 延迟返回
        setTimeout(() => {
          uni.navigateBack();
        }, 1500);

      } catch (error) {
        console.error('保存规则失败:', error);
        uni.hideLoading();
        uni.showToast({
          title: '保存失败',
          icon: 'none',
        });
      }
    },

    /**
     * 返回
     */
    goBack() {
      uni.navigateBack();
    },
  },
};
</script>

<style lang="scss" scoped>
.rule-form {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.status-bar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 0 16px;
  padding-bottom: 16px;
}

.status-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.back-btn {
  padding: 8px;
}

.back-icon {
  font-size: 24px;
  color: white;
  font-weight: bold;
}

.page-title {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.save-btn {
  padding: 8px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 14px;
}

.form-content {
  flex: 1;
  padding: 16px;
  padding-bottom: 100px;
}

.form-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.add-btn {
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.selected-count {
  font-size: 14px;
  color: #666;
}

.form-item {
  margin-bottom: 16px;
}

.form-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background: white;
}

.form-textarea {
  width: 100%;
  min-height: 80px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  background: white;
}

.picker-input {
  height: 44px;
  padding: 0 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: white;
  font-size: 14px;
  color: #333;
}

.picker-arrow {
  color: #ccc;
  font-size: 16px;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.radio-text {
  font-size: 14px;
  color: #333;
}

.condition-card,
.action-card {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
}

.condition-header,
.action-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.condition-index,
.action-index {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.remove-btn {
  color: #ff3b30;
  font-size: 14px;
}

.device-list {
  max-height: 200px;
  overflow-y: auto;
}

.device-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.device-item:last-child {
  border-bottom: none;
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 2px;
}

.device-location {
  font-size: 12px;
  color: #666;
}

.safe-area-bottom {
  height: env(safe-area-inset-bottom);
  height: constant(safe-area-inset-bottom);
}
</style>
