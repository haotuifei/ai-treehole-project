import { http } from './http'

/**
 * 获取情绪统计数据
 * @param {string} period - day(今日) / week(本周) / month(本月)
 */
export function getEmotionStats(period) {
  return http.get('/student/emotion/stats', { params: { period } })
}

/**
 * 获取情绪记录列表
 * @param {object} params - { pageNum, pageSize, period }
 */
export function getEmotionRecords(params) {
  return http.get('/student/emotion/records', { params })
}
