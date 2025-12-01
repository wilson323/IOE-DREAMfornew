<template>
  <view class="attendance-rule-page">
    <!-- 顶部导航栏 -->
    <view class="custom-navbar">
      <view class="nav-left" @click="goBack">
        <text class="iconfont icon-back"></text>
      </view>
      <view class="nav-title">考勤规则</view>
      <view class="nav-right"></view>
    </view>

    <!-- 页面内容 -->
    <view class="page-content">
      <!-- 规则概览 -->
      <view class="rule-overview">
        <view class="overview-header">
          <text class="title">当前考勤规则</text>
          <button class="edit-btn" @click="editRule">
            <text class="iconfont icon-edit"></text>
            <text>编辑</text>
          </button>
        </view>

        <view class="rule-item">
          <text class="label">工作日</text>
          <text class="value">{{ rule.workDays?.join('、') || '周一至周五' }}</text>
        </view>

        <view class="rule-item">
          <text class="label">上班时间</text>
          <text class="value">{{ rule.workStartTime || '09:00' }}</text>
        </view>

        <view class="rule-item">
          <text class="label">下班时间</text>
          <text class="value">{{ rule.workEndTime || '18:00' }}</text>
        </view>

        <view class="rule-item">
          <text class="label">迟到判定</text>
          <text class="value">上班后{{ rule.lateMinutes || 5 }}分钟</text>
        </view>

        <view class="rule-item">
          <text class="label">早退判定</text>
          <text class="value">下班前{{ rule.earlyLeaveMinutes || 5 }}分钟</text>
        </view>

        <view class="rule-item">
          <text class="label">加班判定</text>
          <text class="value">下班后{{ rule.overtimeMinutes || 60 }}分钟</text>
        </view>

        <view class="rule-item">
          <text class="label">打卡范围</text>
          <text class="value">{{ rule.punchRange || 100 }}米</text>
        </view>

        <view class="rule-item">
          <text class="label">允许补卡</text>
          <text class="value">{{ rule.allowCorrection ? '是' : '否' }}</text>
        </view>

        <view class="rule-item" v-if="rule.allowCorrection">
          <text class="label">补卡次数</text>
          <text class="value">每月{{ rule.correctionLimit || 3 }}次</text>
        </view>
      </view>

      <!-- 考勤时段设置 -->
      <view class="time-sections">
        <view class="section-header">
          <text class="title">考勤时段设置</text>
        </view>

        <view class="section-content">
          <view class="time-item" v-for="(time, index) in rule.timeSections" :key="index">
            <view class="time-label">{{ time.name }}</view>
            <view class="time-value">{{ time.startTime }} - {{ time.endTime }}</view>
          </view>
        </view>
      </view>

      <!-- 例外规则 -->
      <view class="exception-rules">
        <view class="section-header">
          <text class="title">例外规则</text>
        </view>

        <view class="section-content">
          <view class="exception-item" v-for="(exception, index) in rule.exceptions" :key="index">
            <view class="exception-date">{{ exception.date }}</view>
            <view class="exception-desc">{{ exception.description }}</view>
          </view>
          <view class="no-data" v-if="!rule.exceptions || rule.exceptions.length === 0">
            <text>暂无例外规则</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 编辑规则弹窗 -->
    <view class="rule-modal" v-if="showRuleModal">
      <view class="modal-overlay" @click="closeRuleModal"></view>
      <view class="modal-content">
        <view class="modal-header">
          <text class="title">编辑考勤规则</text>
          <text class="close-btn" @click="closeRuleModal">×</text>
        </view>
        <scroll-view class="modal-body" scroll-y>
          <view class="form-group">
            <view class="form-item">
              <text class="label">工作日</text>
              <view class="checkbox-group">
                <label class="checkbox-item" v-for="day in workDaysOptions" :key="day.value">
                  <checkbox
                    :value="day.value"
                    :checked="tempRule.workDays.includes(day.value)"
                    @change="onWorkDayChange"
                  />
                  <text>{{ day.label }}</text>
                </label>
              </view>
            </view>

            <view class="form-item">
              <text class="label">上班时间</text>
              <picker mode="time" :value="tempRule.workStartTime" @change="onWorkStartTimeChange">
                <view class="picker">
                  {{ tempRule.workStartTime }}
                </view>
              </picker>
            </view>

            <view class="form-item">
              <text class="label">下班时间</text>
              <picker mode="time" :value="tempRule.workEndTime" @change="onWorkEndTimeChange">
                <view class="picker">
                  {{ tempRule.workEndTime }}
                </view>
              </picker>
            </view>

            <view class="form-item">
              <text class="label">迟到判定(分钟)</text>
              <input
                class="input"
                type="number"
                :value="tempRule.lateMinutes"
                @input="onLateMinutesInput"
              />
            </view>

            <view class="form-item">
              <text class="label">早退判定(分钟)</text>
              <input
                class="input"
                type="number"
                :value="tempRule.earlyLeaveMinutes"
                @input="onEarlyLeaveMinutesInput"
              />
            </view>

            <view class="form-item">
              <text class="label">加班判定(分钟)</text>
              <input
                class="input"
                type="number"
                :value="tempRule.overtimeMinutes"
                @input="onOvertimeMinutesInput"
              />
            </view>

            <view class="form-item">
              <text class="label">打卡范围(米)</text>
              <input
                class="input"
                type="number"
                :value="tempRule.punchRange"
                @input="onPunchRangeInput"
              />
            </view>

            <view class="form-item">
              <text class="label">允许补卡</text>
              <switch
                :checked="tempRule.allowCorrection"
                @change="onAllowCorrectionChange"
              />
            </view>

            <view class="form-item" v-if="tempRule.allowCorrection">
              <text class="label">补卡次数限制</text>
              <input
                class="input"
                type="number"
                :value="tempRule.correctionLimit"
                @input="onCorrectionLimitInput"
              />
            </view>
          </view>
        </scroll-view>
        <view class="modal-footer">
          <button class="cancel-btn" @click="closeRuleModal">取消</button>
          <button class="confirm-btn" @click="saveRule">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ruleApi } from '@/api/business/attendance/rule-api.js'

export default {
  name: 'AttendanceRule',
  setup() {
    // 响应式数据
    const rule = ref({
      workDays: ['周一', '周二', '周三', '周四', '周五'],
      workStartTime: '09:00',
      workEndTime: '18:00',
      lateMinutes: 5,
      earlyLeaveMinutes: 5,
      overtimeMinutes: 60,
      punchRange: 100,
      allowCorrection: true,
      correctionLimit: 3,
      timeSections: [
        { name: '上午上班', startTime: '09:00', endTime: '12:00' },
        { name: '下午上班', startTime: '13:00', endTime: '18:00' }
      ],
      exceptions: [
        { date: '2025-01-01', description: '元旦假期' },
        { date: '2025-02-10', description: '春节假期' }
      ]
    })

    const showRuleModal = ref(false)
    const tempRule = reactive({ ...rule.value })
    const workDaysOptions = [
      { label: '周一', value: '周一' },
      { label: '周二', value: '周二' },
      { label: '周三', value: '周三' },
      { label: '周四', value: '周四' },
      { label: '周五', value: '周五' },
      { label: '周六', value: '周六' },
      { label: '周日', value: '周日' }
    ]

    // 方法
    const goBack = () => {
      uni.navigateBack()
    }

    const editRule = () => {
      // 深拷贝当前规则到临时对象
      Object.assign(tempRule, { ...rule.value })
      showRuleModal.value = true
    }

    const closeRuleModal = () => {
      showRuleModal.value = false
    }

    const onWorkDayChange = (e) => {
      const values = e.detail.value
      tempRule.workDays = values
    }

    const onWorkStartTimeChange = (e) => {
      tempRule.workStartTime = e.detail.value
    }

    const onWorkEndTimeChange = (e) => {
      tempRule.workEndTime = e.detail.value
    }

    const onLateMinutesInput = (e) => {
      tempRule.lateMinutes = parseInt(e.detail.value) || 0
    }

    const onEarlyLeaveMinutesInput = (e) => {
      tempRule.earlyLeaveMinutes = parseInt(e.detail.value) || 0
    }

    const onOvertimeMinutesInput = (e) => {
      tempRule.overtimeMinutes = parseInt(e.detail.value) || 0
    }

    const onPunchRangeInput = (e) => {
      tempRule.punchRange = parseInt(e.detail.value) || 0
    }

    const onAllowCorrectionChange = (e) => {
      tempRule.allowCorrection = e.detail.value
    }

    const onCorrectionLimitInput = (e) => {
      tempRule.correctionLimit = parseInt(e.detail.value) || 0
    }

    const saveRule = async () => {
      try {
        // 这里应该调用API保存规则
        // const result = await ruleApi.updateRule(ruleId, tempRule)

        // 更新本地规则
        Object.assign(rule.value, { ...tempRule })

        uni.showToast({
          title: '规则保存成功',
          icon: 'success'
        })

        closeRuleModal()
      } catch (error) {
        uni.showToast({
          title: '规则保存失败',
          icon: 'none'
        })
      }
    }

    const loadRule = async () => {
      try {
        // 这里应该调用API获取规则
        // const result = await ruleApi.getEmployeeRules(employeeId)
        // if (result.success) {
        //   rule.value = result.data
        // }
      } catch (error) {
        console.error('加载考勤规则失败:', error)
      }
    }

    // 生命周期
    onMounted(() => {
      loadRule()
    })

    // 暴露给模板的数据和方法
    return {
      rule,
      showRuleModal,
      tempRule,
      workDaysOptions,
      goBack,
      editRule,
      closeRuleModal,
      onWorkDayChange,
      onWorkStartTimeChange,
      onWorkEndTimeChange,
      onLateMinutesInput,
      onEarlyLeaveMinutesInput,
      onOvertimeMinutesInput,
      onPunchRangeInput,
      onAllowCorrectionChange,
      onCorrectionLimitInput,
      saveRule
    }
  }
}
</script>

<style lang="scss" scoped>
.attendance-rule-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.custom-navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 44px;
  padding: 0 15px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 999;

  .nav-left, .nav-right {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .nav-title {
    font-size: 17px;
    font-weight: 500;
    color: #333;
  }
}

.page-content {
  padding-top: 44px;
  padding: 15px;
}

.rule-overview {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }

    .edit-btn {
      height: 30px;
      border: 1px solid #667eea;
      border-radius: 5px;
      background-color: #fff;
      color: #667eea;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 12px;
      padding: 0 10px;

      .iconfont {
        font-size: 14px;
        margin-right: 3px;
      }
    }
  }

  .rule-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;

    .label {
      color: #666;
    }

    .value {
      color: #333;
      font-weight: 500;
    }

    &:last-child {
      border-bottom: none;
    }
  }
}

.time-sections, .exception-rules {
  background-color: #fff;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

  .section-header {
    margin-bottom: 15px;

    .title {
      font-size: 16px;
      font-weight: 500;
      color: #333;
    }
  }

  .section-content {
    .time-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      .time-label {
        color: #333;
        font-weight: 500;
      }

      .time-value {
        color: #666;
      }

      &:last-child {
        border-bottom: none;
      }
    }

    .exception-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
      border-bottom: 1px solid #f0f0f0;

      .exception-date {
        color: #333;
        font-weight: 500;
      }

      .exception-desc {
        color: #666;
      }

      &:last-child {
        border-bottom: none;
      }
    }

    .no-data {
      text-align: center;
      padding: 20px;
      color: #999;
    }
  }
}

.rule-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;

  .modal-overlay {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
  }

  .modal-content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 90%;
    max-height: 80%;
    background-color: #fff;
    border-radius: 10px;
    overflow: hidden;

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      background-color: #667eea;
      color: #fff;

      .title {
        font-size: 16px;
        font-weight: 500;
      }

      .close-btn {
        font-size: 24px;
        font-weight: 300;
      }
    }

    .modal-body {
      max-height: 60vh;

      .form-group {
        padding: 20px;

        .form-item {
          margin-bottom: 20px;

          .label {
            display: block;
            margin-bottom: 8px;
            color: #333;
            font-weight: 500;
          }

          .checkbox-group {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;

            .checkbox-item {
              display: flex;
              align-items: center;
              background-color: #f5f5f5;
              padding: 5px 10px;
              border-radius: 5px;

              checkbox {
                margin-right: 5px;
              }
            }
          }

          .picker, .input {
            width: 100%;
            height: 40px;
            border: 1px solid #ddd;
            border-radius: 5px;
            padding: 0 10px;
            font-size: 14px;
            background-color: #fff;
          }

          .picker {
            display: flex;
            align-items: center;
            justify-content: space-between;
            color: #666;
          }
        }
      }
    }

    .modal-footer {
      display: flex;
      border-top: 1px solid #eee;

      .cancel-btn, .confirm-btn {
        flex: 1;
        height: 45px;
        border: none;
        font-size: 16px;
      }

      .cancel-btn {
        background-color: #f5f5f5;
        color: #666;
      }

      .confirm-btn {
        background-color: #667eea;
        color: #fff;
      }
    }
  }
}
</style>