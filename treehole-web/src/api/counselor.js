import { http } from './http'

// ========== 学生档案 ==========

/** 辅导员-学生分页列表 */
export function getStudents(params) {
  return http.get('/counselor/students', { params })
}

/** 辅导员-学生详情 */
export function getStudentDetail(id) {
  return http.get(`/counselor/students/${id}`)
}

// ========== 预警管理 ==========

/** 辅导员-预警分页列表 */
export function getWarnings(params) {
  return http.get('/counselor/warnings', { params })
}

/** 辅导员-预警详情 */
export function getWarningDetail(id) {
  return http.get(`/counselor/warnings/${id}`)
}

/** 辅导员-更新预警状态 */
export function updateWarningStatus(id, data) {
  return http.put(`/counselor/warnings/${id}/status`, data)
}

/** 辅导员-添加干预记录 */
export function addIntervention(warningId, data) {
  return http.post(`/counselor/warnings/${warningId}/interventions`, data)
}

// ========== 干预记录 ==========

/** 辅导员-干预记录分页列表 */
export function getInterventions(params) {
  return http.get('/counselor/interventions', { params })
}

// ========== 班级数据分析 ==========

/** 辅导员-班级数据概览 */
export function getClassAnalytics() {
  return http.get('/counselor/analytics')
}
