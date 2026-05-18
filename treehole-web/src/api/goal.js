import { http } from './http'

export function listGoals(status) {
  const params = {}
  if (status != null) params.status = status
  return http.get('/student/goals', { params })
}

export function getGoal(id) {
  return http.get(`/student/goals/${id}`)
}

export function createGoal(data) {
  return http.post('/student/goals', data)
}

export function updateGoal(id, data) {
  return http.put(`/student/goals/${id}`, data)
}

export function deleteGoal(id) {
  return http.delete(`/student/goals/${id}`)
}
