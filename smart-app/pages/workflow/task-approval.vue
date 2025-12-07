<!--
  * 任务审批页 - 移动端
  * 提供任务审批、驳回、转办、委派功能
  *
  * @Author:    IOE-DREAM Team
  * @Date:      2025-01-30
  * @Copyright  IOE-DREAM智慧园区一卡通管理平台
-->

<template>
  <view class="task-approval-page">
    <!-- 快速审批按钮 -->
    <view class="quick-actions">
      <button
        class="quick-btn approve"
        @click="handleQuickApprove"
        v-if="action !== 'reject'"
      >
        快速同意
      </button>
      <button
        class="quick-btn reject"
        @click="handleQuickReject"
        v-if="action !== 'approve'"
      >
        快速驳回
      </button>
    </view>

    <!-- 审批表单 -->
    <view class="approval-form">
      <view class="form-item">
        <text class="form-label">审批意见</text>
        <textarea
          v-model="form.comment"
          class="form-textarea"
          placeholder="请输入审批意见（可选）"
          :maxlength="500"
        />
        <text class="char-count">{{ form.comment.length }}/500</text>
      </view>

      <!-- 附件上传 -->
      <view class="form-item">
        <text class="form-label">附件</text>
        <view class="upload-section">
          <view class="upload-btn" @click="chooseFile">
            <text class="upload-icon">📎</text>
            <text class="upload-text">选择附件</text>
          </view>
          <view class="file-list" v-if="fileList.length > 0">
            <view
              class="file-item"
              v-for="(file, index) in fileList"
              :key="index"
            >
              <text class="file-name">{{ file.name }}</text>
              <text class="file-remove" @click="removeFile(index)">×</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部操作栏 -->
    <view class="bottom-actions">
      <button class="action-btn cancel" @click="goBack">取消</button>
      <button class="action-btn transfer" @click="handleTransfer">转办</button>
      <button class="action-btn delegate" @click="handleDelegate">委派</button>
      <button
        class="action-btn submit"
        :class="{ 'approve': action === 'approve', 'reject': action === 'reject' }"
        @click="handleSubmit"
      >
        {{ action === 'approve' ? '同意' : action === 'reject' ? '驳回' : '提交' }}
      </button>
    </view>

    <!-- 转办/委派弹窗 -->
    <uni-popup ref="transferPopup" type="dialog">
      <uni-popup-dialog
        :title="transferForm.type === 'transfer' ? '转办任务' : '委派任务'"
        mode="input"
        placeholder="请输入目标用户ID"
        @confirm="handleTransferConfirm"
        @close="closeTransferPopup"
      />
    </uni-popup>
  </view>
</template>

<script setup>
  import { reactive, ref, onMounted } from 'vue';
  import { onLoad, onUnload } from '@dcloudio/uni-app';
  import { useWorkflowStore } from '@/store/workflow';

  const workflowStore = useWorkflowStore();

  const taskId = ref(null);
  const action = ref('approve'); // approve, reject
  const form = reactive({
    comment: '',
  });
  const fileList = ref([]);
  const transferPopup = ref(null);
  const transferForm = reactive({
    type: 'transfer',
    targetUserId: null,
  });

  /**
   * 快速同意
   */
  function handleQuickApprove() {
    form.comment = '快速审批通过';
    handleSubmit();
  }

  /**
   * 快速驳回
   */
  function handleQuickReject() {
    form.comment = '快速驳回';
    action.value = 'reject';
    handleSubmit();
  }

  /**
   * 提交审批
   */
  async function handleSubmit() {
    if (action.value === 'reject' && !form.comment) {
      uni.showToast({
        title: '驳回必须填写意见',
        icon: 'none',
      });
      return;
    }

    try {
      if (action.value === 'approve') {
        await workflowStore.completeTask(taskId.value, {
          outcome: '1',
          comment: form.comment,
        });
      } else if (action.value === 'reject') {
        await workflowStore.rejectTask(taskId.value, {
          comment: form.comment,
        });
      }

      uni.showToast({
        title: '操作成功',
        icon: 'success',
      });

      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('提交审批失败:', err);
    }
  }

  /**
   * 转办
   */
  function handleTransfer() {
    transferForm.type = 'transfer';
    transferPopup.value?.open();
  }

  /**
   * 委派
   */
  function handleDelegate() {
    transferForm.type = 'delegate';
    transferPopup.value?.open();
  }

  /**
   * 确认转办/委派
   * @param {String} userId - 用户ID
   */
  async function handleTransferConfirm(userId) {
    if (!userId) {
      uni.showToast({
        title: '请输入目标用户ID',
        icon: 'none',
      });
      return;
    }

    try {
      if (transferForm.type === 'transfer') {
        await workflowStore.transferTask(taskId.value, parseInt(userId));
      } else {
        await workflowStore.delegateTask(taskId.value, parseInt(userId));
      }

      uni.showToast({
        title: '操作成功',
        icon: 'success',
      });

      closeTransferPopup();
      setTimeout(() => {
        uni.navigateBack();
      }, 1500);
    } catch (err) {
      console.error('转办/委派失败:', err);
    }
  }

  /**
   * 关闭转办/委派弹窗
   */
  function closeTransferPopup() {
    transferPopup.value?.close();
    transferForm.targetUserId = null;
  }

  /**
   * 选择文件
   */
  function chooseFile() {
    uni.chooseImage({
      count: 9,
      success: (res) => {
        fileList.value = res.tempFiles.map((file) => ({
          name: file.path.split('/').pop(),
          path: file.path,
        }));
      },
    });
  }

  /**
   * 移除文件
   * @param {Number} index - 文件索引
   */
  function removeFile(index) {
    fileList.value.splice(index, 1);
  }

  /**
   * 返回
   */
  function goBack() {
    uni.navigateBack();
  }

  /**
   * 获取URL参数
   * @param {String} key - 参数名
   * @returns {String}
   */
  function getQueryParam(key) {
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    return currentPage.options[key];
  }

  onLoad((options) => {
    taskId.value = options.taskId || getQueryParam('taskId');
    action.value = options.action || 'approve';
  });
</script>

<style lang="scss" scoped>
  .task-approval-page {
    min-height: 100vh;
    background-color: #f5f5f5;
    padding-bottom: 120rpx;

    .quick-actions {
      display: flex;
      gap: 20rpx;
      padding: 20rpx;
      background: #fff;
      margin-bottom: 20rpx;

      .quick-btn {
        flex: 1;
        height: 80rpx;
        line-height: 80rpx;
        text-align: center;
        border-radius: 8rpx;
        font-size: 28rpx;
        border: none;

        &.approve {
          background: #1890ff;
          color: #fff;
        }

        &.reject {
          background: #ff4d4f;
          color: #fff;
        }
      }
    }

    .approval-form {
      background: #fff;
      padding: 30rpx;

      .form-item {
        margin-bottom: 40rpx;

        .form-label {
          display: block;
          font-size: 28rpx;
          font-weight: bold;
          margin-bottom: 20rpx;
          color: #333;
        }

        .form-textarea {
          width: 100%;
          min-height: 200rpx;
          padding: 20rpx;
          border: 1px solid #e8e8e8;
          border-radius: 8rpx;
          font-size: 28rpx;
          background: #fafafa;
        }

        .char-count {
          display: block;
          text-align: right;
          font-size: 24rpx;
          color: #999;
          margin-top: 10rpx;
        }

        .upload-section {
          .upload-btn {
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40rpx;
            border: 2rpx dashed #d9d9d9;
            border-radius: 8rpx;
            background: #fafafa;

            .upload-icon {
              font-size: 48rpx;
              margin-right: 10rpx;
            }

            .upload-text {
              font-size: 28rpx;
              color: #666;
            }
          }

          .file-list {
            margin-top: 20rpx;

            .file-item {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 20rpx;
              background: #f5f5f5;
              border-radius: 8rpx;
              margin-bottom: 10rpx;

              .file-name {
                font-size: 26rpx;
                color: #333;
                flex: 1;
              }

              .file-remove {
                font-size: 40rpx;
                color: #ff4d4f;
                width: 40rpx;
                text-align: center;
              }
            }
          }
        }
      }
    }

    .bottom-actions {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      display: flex;
      gap: 20rpx;
      padding: 20rpx;
      background: #fff;
      box-shadow: 0 -2rpx 8rpx rgba(0, 0, 0, 0.1);

      .action-btn {
        flex: 1;
        height: 80rpx;
        line-height: 80rpx;
        text-align: center;
        border-radius: 8rpx;
        font-size: 28rpx;
        border: none;

        &.cancel {
          background: #f5f5f5;
          color: #666;
        }

        &.transfer,
        &.delegate {
          background: #e6f7ff;
          color: #1890ff;
        }

        &.submit {
          background: #1890ff;
          color: #fff;

          &.approve {
            background: #52c41a;
          }

          &.reject {
            background: #ff4d4f;
          }
        }
      }
    }
  }
</style>

