/**
 * 访客管理API测试
 * <p>
 * 测试移动端访客管理相关API调用
 * 包括预约、签到、OCR识别等功能
 * </p>
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import visitorApi, { ocrApi } from '@/api/business/visitor/visitor-api'
import { getRequest, postRequest } from '@/lib/smart-request'

// Mock请求库
vi.mock('@/lib/smart-request', () => ({
  getRequest: vi.fn(),
  postRequest: vi.fn(),
  putRequest: vi.fn(),
  deleteRequest: vi.fn()
}))

// Mock uni对象
global.uni = {
  uploadFile: vi.fn(),
  getStorageSync: vi.fn(() => 'mock-token')
}

describe('访客管理API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('预约相关API', () => {
    it('应该成功创建预约', async () => {
      const mockData = {
        visitorName: '张三',
        idCard: '110101199001011234',
        phone: '13800138000',
        hostUserId: 1001,
        purpose: '商务洽谈'
      }

      postRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: { appointmentId: 12345 }
      })

      const result = await visitorApi.createAppointment(mockData)

      expect(postRequest).toHaveBeenCalledWith('/api/v1/mobile/visitor/appointment', mockData)
      expect(result.success).toBe(true)
      expect(result.data.appointmentId).toBe(12345)
    })

    it('应该成功获取我的预约列表', async () => {
      const userId = 1001
      const status = 1

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: [
          { appointmentId: 1, visitorName: '张三', status: 1 },
          { appointmentId: 2, visitorName: '李四', status: 1 }
        ]
      })

      const result = await visitorApi.getMyAppointments(userId, status)

      expect(getRequest).toHaveBeenCalledWith('/api/v1/mobile/visitor/my-appointments', { userId, status })
      expect(result.success).toBe(true)
      expect(result.data).toHaveLength(2)
    })

    it('应该成功取消预约', async () => {
      const appointmentId = 12345
      const userId = 1001

      putRequest.mockResolvedValue({
        code: 200,
        success: true,
        message: '取消成功'
      })

      const result = await visitorApi.cancelAppointment(appointmentId, userId)

      expect(putRequest).toHaveBeenCalledWith(
        `/api/v1/mobile/visitor/appointment/${appointmentId}/cancel?userId=${userId}`,
        undefined
      )
      expect(result.success).toBe(true)
    })
  })

  describe('OCR识别API', () => {
    it('应该成功识别身份证', async () => {
      const imagePath = '/tmp/test-idcard.jpg'
      const cardSide = 'FRONT'

      const mockOcrResult = {
        name: '张三',
        idCard: '110101199001011234',
        gender: '男',
        birthday: '1990-01-01',
        address: '北京市东城区XX街道XX号'
      }

      global.uni.uploadFile.mockImplementation((options) => {
        return Promise.resolve({
          data: JSON.stringify({
            code: 200,
            success: true,
            data: mockOcrResult
          })
        })
      })

      const result = await ocrApi.recognizeIdCard(imagePath, cardSide)

      expect(global.uni.uploadFile).toHaveBeenCalled()
      expect(result.success).toBe(true)
      expect(result.data.name).toBe('张三')
      expect(result.data.idCard).toBe('110101199001011234')
    })

    it('应该处理OCR识别失败', async () => {
      const imagePath = '/tmp/invalid-image.jpg'

      global.uni.uploadFile.mockImplementation((options) => {
        return Promise.resolve({
          data: JSON.stringify({
            code: 500,
            success: false,
            message: '识别失败'
          })
        })
      })

      await expect(ocrApi.recognizeIdCard(imagePath)).rejects.toThrow('识别失败')
    })
  })

  describe('签到签退API', () => {
    it('应该成功签到', async () => {
      const appointmentId = 12345

      postRequest.mockResolvedValue({
        code: 200,
        success: true,
        message: '签到成功'
      })

      const result = await visitorApi.checkInByQRCode('QR_CODE_123')

      expect(postRequest).toHaveBeenCalled()
      expect(result.success).toBe(true)
    })

    it('应该成功签退', async () => {
      const appointmentId = 12345

      postRequest.mockResolvedValue({
        code: 200,
        success: true,
        message: '签退成功'
      })

      const result = await visitorApi.checkout(appointmentId)

      expect(postRequest).toHaveBeenCalledWith(`/api/v1/mobile/visitor/checkout/${appointmentId}`, undefined)
      expect(result.success).toBe(true)
    })
  })

  describe('统计API', () => {
    it('应该成功获取个人统计', async () => {
      const userId = 1001

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: {
          totalAppointments: 10,
          todayAppointments: 2,
          monthAppointments: 5
        }
      })

      const result = await visitorApi.getPersonalStatistics(userId)

      expect(getRequest).toHaveBeenCalledWith(`/api/v1/mobile/visitor/statistics/${userId}`)
      expect(result.success).toBe(true)
      expect(result.data.totalAppointments).toBe(10)
    })
  })
})

