/**
 * 设备管理前端组件测试
 */

import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import DeviceList from '/@/views/common/device/index.vue'
import DeviceModal from '/@/views/common/device/DeviceModal.vue'
import DeviceStatusTag from '/@/components/common/device/DeviceStatusTag.vue'
import DeviceTypeSelect from '/@/components/common/device/DeviceTypeSelect.vue'
import { deviceApi } from '/@/api/common/device/deviceApi'

// Mock API
vi.mock('/@/api/common/device/deviceApi', () => ({
  deviceApi: {
    queryPage: vi.fn(),
    getStatistics: vi.fn(),
    add: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    enable: vi.fn(),
    disable: vi.fn(),
    online: vi.fn(),
    offline: vi.fn(),
    refreshStatus: vi.fn()
  }
}))

// Mock permissions
vi.mock('/@/hooks/usePermission', () => ({
  usePermission: () => ({
    hasPermission: vi.fn(() => true)
  })
}))

describe('设备管理组件测试', () => {
  describe('DeviceStatusTag', () => {
    it('应该正确显示在线状态', () => {
      const wrapper = mount(DeviceStatusTag, {
        props: {
          status: 1
        }
      })

      expect(wrapper.text()).toContain('在线')
      expect(wrapper.find('.device-status-tag').classes()).toContain('device-status-tag-default')
    })

    it('应该正确显示离线状态', () => {
      const wrapper = mount(DeviceStatusTag, {
        props: {
          status: 0
        }
      })

      expect(wrapper.text()).toContain('离线')
    })

    it('应该正确显示故障状态', () => {
      const wrapper = mount(DeviceStatusTag, {
        props: {
          status: 2
        }
      })

      expect(wrapper.text()).toContain('故障')
    })

    it('应该正确显示维护中状态', () => {
      const wrapper = mount(DeviceStatusTag, {
        props: {
          status: 3
        }
      })

      expect(wrapper.text()).toContain('维护中')
    })

    it('应该正确处理未知状态', () => {
      const wrapper = mount(DeviceStatusTag, {
        props: {
          status: 999
        }
      })

      expect(wrapper.text()).toContain('未知')
    })
  })

  describe('DeviceTypeSelect', () => {
    it('应该正确渲染设备类型选项', () => {
      const wrapper = mount(DeviceTypeSelect, {
        props: {
          value: 'CAMERA'
        }
      })

      // 检查是否正确绑定了值
      expect(wrapper.vm.selectedType).toBe('CAMERA')
    })

    it('应该正确处理值变化', async () => {
      const wrapper = mount(DeviceTypeSelect, {
        props: {
          value: 'CAMERA'
        }
      })

      // 模拟选择变化
      await wrapper.vm.handleChange('ACCESS_CONTROLLER')

      // 检查是否正确触发了更新
      expect(wrapper.emitted('update:value')).toBeTruthy()
      expect(wrapper.emitted('update:value')[0]).toEqual(['ACCESS_CONTROLLER'])
    })
  })

  describe('DeviceModal', () => {
    const mockDevice = {
      deviceId: 1,
      deviceCode: 'TEST001',
      deviceName: '测试设备',
      deviceType: 'CAMERA',
      deviceBrand: '测试品牌',
      deviceModel: 'TEST-001',
      deviceSerial: 'SN123456789',
      areaId: 1,
      areaName: '测试区域',
      locationDesc: '测试位置',
      ipAddress: '192.168.1.100',
      port: 8080,
      macAddress: 'AA:BB:CC:DD:EE:FF',
      protocolType: 'TCP',
      vendorInfo: '测试供应商',
      contactPerson: '测试联系人',
      contactPhone: '13800138000',
      installTime: '2024-01-01 00:00:00',
      warrantyEndTime: '2025-01-01 00:00:00',
      configJson: { resolution: '1080p' },
      extendInfo: { warranty: 24 }
    }

    it('应该正确显示编辑模式', () => {
      const wrapper = mount(DeviceModal, {
        props: {
          visible: true,
          device: mockDevice,
          isEdit: true
        }
      })

      expect(wrapper.vm.modalTitle).toBe('编辑设备')
      expect(wrapper.vm.formData.deviceCode).toBe(mockDevice.deviceCode)
      expect(wrapper.vm.formData.deviceName).toBe(mockDevice.deviceName)
    })

    it('应该正确显示新增模式', () => {
      const wrapper = mount(DeviceModal, {
        props: {
          visible: true,
          device: null,
          isEdit: false
        }
      })

      expect(wrapper.vm.modalTitle).toBe('新增设备')
      expect(wrapper.vm.formData.deviceCode).toBe('')
      expect(wrapper.vm.formData.deviceName).toBe('')
    })

    it('应该正确处理JSON配置', () => {
      const wrapper = mount(DeviceModal, {
        props: {
          visible: true,
          device: mockDevice,
          isEdit: true
        }
      })

      // 检查JSON配置是否正确转换为文本
      expect(wrapper.vm.configJsonText).toContain('resolution')
      expect(wrapper.vm.extendInfoText).toContain('warranty')
    })

    it('应该正确验证JSON格式', () => {
      const wrapper = mount(DeviceModal, {
        props: {
          visible: true,
          device: null,
          isEdit: false
        }
      })

      // 模拟无效的JSON
      wrapper.vm.configJsonText = 'invalid json'
      wrapper.vm.handleConfigJsonBlur()

      expect(wrapper.vm.configJsonError).toBe('JSON格式错误')

      // 模拟有效的JSON
      wrapper.vm.configJsonText = '{"test": "value"}'
      wrapper.vm.handleConfigJsonBlur()

      expect(wrapper.vm.configJsonError).toBe('')
      expect(wrapper.vm.formData.configJson).toEqual({ test: 'value' })
    })
  })

  describe('DeviceList', () => {
    const mockTableData = [
      {
        deviceId: 1,
        deviceCode: 'DEV001',
        deviceName: '设备1',
        deviceTypeName: '摄像头',
        deviceBrand: '品牌A',
        ipAddress: '192.168.1.100',
        status: 1,
        isEnabled: 1,
        areaName: '区域A',
        contactPerson: '联系人A',
        contactPhone: '13800138000',
        createTime: '2024-01-01 00:00:00'
      },
      {
        deviceId: 2,
        deviceCode: 'DEV002',
        deviceName: '设备2',
        deviceTypeName: '门禁控制器',
        deviceBrand: '品牌B',
        ipAddress: '192.168.1.101',
        status: 0,
        isEnabled: 0,
        areaName: '区域B',
        contactPerson: '联系人B',
        contactPhone: '13800138001',
        createTime: '2024-01-02 00:00:00'
      }
    ]

    const mockStatistics = {
      totalDevices: 10,
      onlineDevices: 6,
      offlineDevices: 3,
      faultDevices: 1
    }

    beforeEach(() => {
      // 重置mock
      vi.clearAllMocks()

      // 设置API返回值
      deviceApi.queryPage.mockResolvedValue({
        data: {
          records: mockTableData,
          total: mockTableData.length,
          current: 1,
          size: 10
        }
      })

      deviceApi.getStatistics.mockResolvedValue({
        data: mockStatistics
      })
    })

    it('应该正确加载设备列表', async () => {
      const wrapper = mount(DeviceList)

      // 等待数据加载
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(deviceApi.queryPage).toHaveBeenCalled()
      expect(deviceApi.getStatistics).toHaveBeenCalled()
    })

    it('应该正确显示统计数据', async () => {
      const wrapper = mount(DeviceList)

      // 等待数据加载
      await wrapper.vm.$nextTick()
      await new Promise(resolve => setTimeout(resolve, 0))

      expect(wrapper.vm.statistics.totalDevices).toBe(10)
      expect(wrapper.vm.statistics.onlineDevices).toBe(6)
      expect(wrapper.vm.statistics.offlineDevices).toBe(3)
      expect(wrapper.vm.statistics.faultDevices).toBe(1)
    })

    it('应该正确处理搜索', async () => {
      const wrapper = mount(DeviceList)

      // 设置搜索条件
      wrapper.vm.searchForm.deviceName = '设备1'
      wrapper.vm.searchForm.deviceType = 'CAMERA'

      // 执行搜索
      await wrapper.vm.handleSearch()

      expect(deviceApi.queryPage).toHaveBeenCalledWith(
        expect.objectContaining({
          deviceName: '设备1',
          deviceType: 'CAMERA',
          current: 1
        })
      )
    })

    it('应该正确处理重置', async () => {
      const wrapper = mount(DeviceList)

      // 设置搜索条件
      wrapper.vm.searchForm.deviceName = '测试'
      wrapper.vm.searchForm.deviceType = 'CAMERA'

      // 执行重置
      await wrapper.vm.handleReset()

      expect(wrapper.vm.searchForm.deviceName).toBe('')
      expect(wrapper.vm.searchForm.deviceType).toBe('')
      expect(deviceApi.queryPage).toHaveBeenCalled()
    })

    it('应该正确处理新增设备', async () => {
      const wrapper = mount(DeviceList)

      await wrapper.vm.handleAdd()

      expect(wrapper.vm.modalVisible).toBe(true)
      expect(wrapper.vm.isEdit).toBe(false)
      expect(wrapper.vm.currentDevice).toBe(null)
    })

    it('应该正确处理编辑设备', async () => {
      const wrapper = mount(DeviceList)

      await wrapper.vm.handleEdit(1)

      expect(wrapper.vm.modalVisible).toBe(true)
      expect(wrapper.vm.isEdit).toBe(true)
      expect(wrapper.vm.currentDevice).toEqual({ deviceId: 1 })
    })

    it('应该正确处理删除设备', async () => {
      deviceApi.delete.mockResolvedValue({})

      const wrapper = mount(DeviceList)

      await wrapper.vm.handleDelete(1)

      expect(deviceApi.delete).toHaveBeenCalledWith(1)
    })

    it('应该正确处理设备状态切换', async () => {
      deviceApi.enable.mockResolvedValue({})
      deviceApi.disable.mockResolvedValue({})

      const wrapper = mount(DeviceList)

      // 测试启用
      await wrapper.vm.handleToggleEnabled(1, true)
      expect(deviceApi.enable).toHaveBeenCalledWith(1)

      // 测试禁用
      await wrapper.vm.handleToggleEnabled(1, false)
      expect(deviceApi.disable).toHaveBeenCalledWith(1)
    })

    it('应该正确处理设备上线/离线', async () => {
      deviceApi.online.mockResolvedValue({})
      deviceApi.offline.mockResolvedValue({})

      const wrapper = mount(DeviceList)

      // 测试上线
      await wrapper.vm.handleOnline(1)
      expect(deviceApi.online).toHaveBeenCalledWith(1)

      // 测试离线
      await wrapper.vm.handleOffline(1)
      expect(deviceApi.offline).toHaveBeenCalledWith(1)
    })

    it('应该正确处理刷新状态', async () => {
      deviceApi.refreshStatus.mockResolvedValue({})

      const wrapper = mount(DeviceList)

      await wrapper.vm.handleRefresh()

      expect(deviceApi.refreshStatus).toHaveBeenCalled()
      expect(wrapper.vm.refreshLoading).toBe(false)
    })
  })
})