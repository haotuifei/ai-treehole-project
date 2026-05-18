import { http } from './http'

export function listCheckins(params) {
  return http.get('/student/checkins', { params })
}

export function createCheckin(data) {
  return http.post('/student/checkins', data)
}

export function updateCheckin(id, data) {
  return http.put(`/student/checkins/${id}`, data)
}

export function deleteCheckin(id) {
  return http.delete(`/student/checkins/${id}`)
}

export function getStreak() {
  return http.get('/student/checkins/streak')
}

export function getCalendar(yearMonth) {
  return http.get('/student/checkins/calendar', { params: { yearMonth } })
}
