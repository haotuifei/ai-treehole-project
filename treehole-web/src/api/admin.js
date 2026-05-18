import { http } from './http'

// ========== 用户管理 ==========

/** 分页查询用户 */
export function pageUsers(params) {
  return http.get('/admin/users', { params })
}

/** 获取用户详情 */
export function getUserDetail(id) {
  return http.get(`/admin/users/${id}`)
}

/** 创建用户 */
export function createUser(data) {
  return http.post('/admin/users', data)
}

/** 更新用户 */
export function updateUser(id, data) {
  return http.put(`/admin/users/${id}`, data)
}

/** 删除用户 */
export function deleteUser(id) {
  return http.delete(`/admin/users/${id}`)
}

// ========== 角色 ==========

/** 获取所有角色 */
export function listRoles() {
  return http.get('/admin/roles')
}

// ========== 模型配置 ==========

/** 查询所有模型配置 */
export function listModelConfigs() {
  return http.get('/admin/model-configs')
}

/** 新增模型配置 */
export function createModelConfig(data) {
  return http.post('/admin/model-configs', data)
}

/** 更新模型配置 */
export function updateModelConfig(id, data) {
  return http.put(`/admin/model-configs/${id}`, data)
}

/** 删除模型配置 */
export function deleteModelConfig(id) {
  return http.delete(`/admin/model-configs/${id}`)
}

// ========== 预警规则 ==========

/** 获取默认预警规则 */
export function getAlertRule() {
  return http.get('/admin/alert-rule')
}

/** 更新默认预警规则 */
export function updateAlertRule(data) {
  return http.put('/admin/alert-rule', data)
}

// ========== 系统日志 ==========

/** 分页查询系统日志 */
export function pageSystemLogs(params) {
  return http.get('/admin/system-logs', { params })
}

// ========== 数据大屏 ==========

/** 获取数据大屏统计 */
export function getDashboard() {
  return http.get('/admin/dashboard')
}

// ========== 辅导员班级管理 ==========

/** 查询所有辅导员及其管辖班级 */
export function listCounselorClasses() {
  return http.get('/admin/counselor-classes')
}

/** 给辅导员分配班级 */
export function assignCounselorClass(data) {
  return http.post('/admin/counselor-classes', data)
}

/** 移除辅导员的班级 */
export function removeCounselorClass(data) {
  return http.delete('/admin/counselor-classes', { data })
}

/** 查询指定辅导员的管辖班级 */
export function getCounselorClasses(counselorUserId) {
  return http.get(`/admin/counselor-classes/${counselorUserId}`)
}

/** 批量同步辅导员的管辖班级 */
export function syncCounselorClasses(counselorUserId, classNames) {
  return http.put(`/admin/counselor-classes/${counselorUserId}`, classNames)
}
