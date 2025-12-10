/*
 * 解码器管理状态管理
 *
 * @Author:    Claude Code
 * @Date:      2024-11-04
 * @Copyright  1024创新实验室
 */
import { defineStore } from 'pinia';
import { message } from 'ant-design-vue';
import { decoderApi } from '/@/api/business/video/decoder-api';
import decoderMockData from '/@/views/business/smart-video/mock/decoder-mock-data';

export const useDecoderStore = defineStore('decoder', {
  state: () => ({
    // 解码器列表
    decoderList: [],

    // 统计信息
    statistics: {
      totalCount: 0,
      onlineCount: 0,
      offlineCount: 0,
      totalChannels: 0,
      usedChannels: 0,
      usageRate: 0,
      averageCpuUsage: 0,
      averageMemoryUsage: 0,
    },

    // 加载状态
    loading: false,

    // 查询参数
    queryParams: {
      pageNum: 1,
      pageSize: 20,
      decoderName: '',
      status: undefined,
      manufacturer: '',
    },

    // 分页信息
    pagination: {
      current: 1,
      pageSize: 20,
      total: 0,
    },

    // 选中项
    selectedRowKeys: [],
  }),

  getters: {
    // 在线解码器列表
    onlineDecoders: (state) => state.decoderList.filter(d => d.status === 1),

    // 离线解码器列表
    offlineDecoders: (state) => state.decoderList.filter(d => d.status === 0),

    // 是否有选中项
    hasSelected: (state) => state.selectedRowKeys.length > 0,

    // 在线率
    onlineRate: (state) => {
      if (state.statistics.totalCount === 0) return 0;
      return Math.round((state.statistics.onlineCount / state.statistics.totalCount) * 100);
    },

    // 通道使用率
    channelUsageRate: (state) => {
      if (state.statistics.totalChannels === 0) return 0;
      return Math.round((state.statistics.usedChannels / state.statistics.totalChannels) * 100);
    },
  },

  actions: {
    /**
     * 获取解码器列表
     * @param {Object} params - 查询参数
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async fetchDecoderList(params = {}, useMock = true) {
      this.loading = true;

      try {
        // 合并查询参数
        const queryData = {
          ...this.queryParams,
          ...params,
        };

        let response;
        if (useMock) {
          // 使用模拟数据
          response = decoderMockData.mockQueryDecoderList(queryData);
        } else {
          // 使用真实API
          response = await decoderApi.queryDecoderList(queryData);
        }

        if (response.code === 1) {
          this.decoderList = response.data.list;
          this.statistics = response.data.statistics;
          this.pagination = {
            current: response.data.pageNum,
            pageSize: response.data.pageSize,
            total: response.data.total,
          };
          this.queryParams = {
            pageNum: response.data.pageNum,
            pageSize: response.data.pageSize,
            decoderName: queryData.decoderName || '',
            status: queryData.status,
            manufacturer: queryData.manufacturer || '',
          };
        } else {
          message.error(response.msg || '获取解码器列表失败');
        }
      } catch (error) {
        console.error('获取解码器列表失败:', error);
        message.error('获取解码器列表失败');
      } finally {
        this.loading = false;
      }
    },

    /**
     * 添加解码器
     * @param {Object} params - 解码器信息
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async addDecoder(params, useMock = true) {
      try {
        let response;
        if (useMock) {
          response = decoderMockData.mockAddDecoder(params);
        } else {
          response = await decoderApi.addDecoder(params);
        }

        if (response.code === 1) {
          message.success('添加成功');
          // 重新获取列表
          await this.fetchDecoderList();
          return response.data;
        } else {
          message.error(response.msg || '添加失败');
          return null;
        }
      } catch (error) {
        console.error('添加解码器失败:', error);
        message.error('添加失败');
        return null;
      }
    },

    /**
     * 更新解码器
     * @param {Object} params - 解码器信息
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async updateDecoder(params, useMock = true) {
      try {
        let response;
        if (useMock) {
          response = decoderMockData.mockUpdateDecoder(params);
        } else {
          response = await decoderApi.updateDecoder(params);
        }

        if (response.code === 1) {
          message.success('更新成功');
          // 重新获取列表
          await this.fetchDecoderList();
          return response.data;
        } else {
          message.error(response.msg || '更新失败');
          return null;
        }
      } catch (error) {
        console.error('更新解码器失败:', error);
        message.error('更新失败');
        return null;
      }
    },

    /**
     * 删除解码器
     * @param {number} id - 解码器ID
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async deleteDecoder(id, useMock = true) {
      try {
        let response;
        if (useMock) {
          response = decoderMockData.mockDeleteDecoder(id);
        } else {
          response = await decoderApi.deleteDecoder(id);
        }

        if (response.code === 1) {
          message.success('删除成功');
          // 重新获取列表
          await this.fetchDecoderList();
          return true;
        } else {
          message.error(response.msg || '删除失败');
          return false;
        }
      } catch (error) {
        console.error('删除解码器失败:', error);
        message.error('删除失败');
        return false;
      }
    },

    /**
     * 批量删除解码器
     * @param {Array<number>} ids - 解码器ID数组
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async batchDeleteDecoder(ids, useMock = true) {
      try {
        let successCount = 0;

        if (useMock) {
          // 模拟批量删除
          for (const id of ids) {
            const result = await decoderMockData.mockDeleteDecoder(id);
            if (result.code === 1) {
              successCount++;
            }
          }
        } else {
          response = await decoderApi.batchDeleteDecoder(ids);
          successCount = ids.length; // 假设全部成功
        }

        if (successCount > 0) {
          message.success(`成功删除 ${successCount} 个解码器`);
          // 重新获取列表
          await this.fetchDecoderList();
          return successCount;
        } else {
          message.error('批量删除失败');
          return 0;
        }
      } catch (error) {
        console.error('批量删除解码器失败:', error);
        message.error('批量删除失败');
        return 0;
      }
    },

    /**
     * 测试连接
     * @param {Object} params - 连接参数
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async testConnection(params, useMock = true) {
      try {
        let response;
        if (useMock) {
          response = await decoderMockData.mockTestConnection(params);
        } else {
          response = await decoderApi.testConnection(params);
        }

        if (response.code === 1) {
          message.success('连接测试成功');
          return response.data;
        } else {
          message.error(response.msg || '连接测试失败');
          return null;
        }
      } catch (error) {
        console.error('连接测试失败:', error);
        message.error('连接测试失败');
        return null;
      }
    },

    /**
     * 重启解码器
     * @param {number} id - 解码器ID
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async restartDecoder(id, useMock = true) {
      try {
        let response;
        if (useMock) {
          response = await decoderMockData.mockRestartDecoder(id);
        } else {
          response = await decoderApi.restartDecoder(id);
        }

        if (response.code === 1) {
          message.success('重启命令已发送');
          // 延迟更新状态
          setTimeout(() => {
            this.fetchDecoderList();
          }, 3000);
          return true;
        } else {
          message.error(response.msg || '重启失败');
          return false;
        }
      } catch (error) {
        console.error('重启解码器失败:', error);
        message.error('重启失败');
        return false;
      }
    },

    /**
     * 获取解码器详情
     * @param {number} id - 解码器ID
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async getDecoderDetail(id, useMock = true) {
      try {
        if (useMock) {
          const decoder = this.decoderList.find(d => d.id === id);
          return decoder || null;
        } else {
          const response = await decoderApi.getDecoderDetail(id);
          if (response.code === 1) {
            return response.data;
          } else {
            return null;
          }
        }
      } catch (error) {
        console.error('获取解码器详情失败:', error);
        return null;
      }
    },

    /**
     * 获取解码器通道信息
     * @param {number} decoderId - 解码器ID
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async getDecoderChannels(decoderId, useMock = true) {
      try {
        if (useMock) {
          return decoderMockData.getDecoderChannels(decoderId);
        } else {
          const response = await decoderApi.getDecoderChannels(decoderId);
          if (response.code === 1) {
            return response.data;
          } else {
            return [];
          }
        }
      } catch (error) {
        console.error('获取通道信息失败:', error);
        return [];
      }
    },

    /**
     * 获取统计信息
     * @param {boolean} useMock - 是否使用模拟数据
     */
    async getStatistics(useMock = true) {
      try {
        let response;
        if (useMock) {
          response = decoderMockData.mockGetDecoderStatistics();
        } else {
          response = await decoderApi.getDecoderStatistics();
        }

        if (response.code === 1) {
          this.statistics = response.data;
          return response.data;
        } else {
          return null;
        }
      } catch (error) {
        console.error('获取统计信息失败:', error);
        return null;
      }
    },

    /**
     * 设置查询参数
     * @param {Object} params - 查询参数
     */
    setQueryParams(params) {
      Object.assign(this.queryParams, params);
      this.queryParams.pageNum = 1;
    },

    /**
     * 重置查询参数
     */
    resetQueryParams() {
      this.queryParams = {
        pageNum: 1,
        pageSize: 20,
        decoderName: '',
        status: undefined,
        manufacturer: '',
      };
    },

    /**
     * 设置选中项
     * @param {Array} keys - 选中项ID数组
     */
    setSelectedRowKeys(keys) {
      this.selectedRowKeys = keys;
    },

    /**
     * 清空选中项
     */
    clearSelectedRowKeys() {
      this.selectedRowKeys = [];
    },

    /**
     * 设置分页信息
     * @param {Object} pagination - 分页信息
     */
    setPagination(pagination) {
      Object.assign(this.pagination, pagination);
    },

    /**
     * 刷新数据
     */
    async refreshData() {
      await this.fetchDecoderList();
    },
  },
});