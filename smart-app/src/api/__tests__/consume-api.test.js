/**
 * 消费管理API测试
 * <p>
 * 测试移动端消费管理相关API调用
 * 包括账户查询、交易记录、统计等功能
 * </p>
 *
 * @Author:    IOE-DREAM Team
 * @Date:      2025-01-30
 * @Copyright  IOE-DREAM智慧园区一卡通管理平台
 */

import { describe, it, expect, beforeEach, vi } from 'vitest'
import consumeApi from '@/api/business/consume/consume-api'
import { getRequest, postRequest } from '@/lib/smart-request'

// Mock请求库
vi.mock('@/lib/smart-request', () => ({
  getRequest: vi.fn(),
  postRequest: vi.fn()
}))

describe('消费管理API测试', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('账户相关API', () => {
    it('应该成功获取账户余额', async () => {
      const userId = 1001

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: { balance: 1000.00 }
      })

      const result = await consumeApi.getAccountBalance(userId)

      expect(getRequest).toHaveBeenCalledWith(`/api/v1/consume/mobile/account/balance/${userId}`)
      expect(result.success).toBe(true)
      expect(result.data.balance).toBe(1000.00)
    })

    it('应该成功获取用户信息', async () => {
      const userId = 1001

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: {
          userId: 1001,
          balance: 1000.00,
          status: 1,
          accountType: 'NORMAL'
        }
      })

      const result = await consumeApi.getUserInfo(userId)

      expect(getRequest).toHaveBeenCalledWith(`/api/v1/consume/mobile/user/${userId}`)
      expect(result.success).toBe(true)
      expect(result.data.balance).toBe(1000.00)
    })
  })

  describe('交易记录API', () => {
    it('应该成功获取交易记录列表', async () => {
      const params = {
        userId: 1001,
        pageNum: 1,
        pageSize: 20
      }

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: {
          list: [
            { id: 1, amount: 10.00, createTime: '2025-01-30 10:00:00' },
            { id: 2, amount: 20.00, createTime: '2025-01-30 11:00:00' }
          ],
          total: 2
        }
      })

      const result = await consumeApi.getTransactions(params)

      expect(getRequest).toHaveBeenCalledWith('/api/v1/consume/mobile/transactions', params)
      expect(result.success).toBe(true)
      expect(result.data.list).toHaveLength(2)
    })
  })

  describe('统计API', () => {
    it('应该成功获取用户统计', async () => {
      const userId = 1001

      getRequest.mockResolvedValue({
        code: 200,
        success: true,
        data: {
          userId: 1001,
          totalCount: 100,
          totalAmount: 5000.00,
          todayCount: 5,
          todayAmount: 250.00,
          monthCount: 50,
          monthAmount: 2500.00
        }
      })

      const result = await consumeApi.statsApi.getUserStats(userId)

      expect(getRequest).toHaveBeenCalledWith(`/api/v1/consume/mobile/stats/${userId}`)
      expect(result.success).toBe(true)
      expect(result.data.totalCount).toBe(100)
      expect(result.data.totalAmount).toBe(5000.00)
    })
  })
})

