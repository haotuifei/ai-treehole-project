import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { http, setToken, getToken } from '../api/http'
import { login as apiLogin } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const profile = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  const roles = computed(() => profile.value?.roleCodes || [])

  function hasRole(...codes) {
    const r = roles.value
    return codes.some((c) => r.includes(c))
  }

  function isStudent() {
    return hasRole('STUDENT')
  }
  function isCounselor() {
    return hasRole('COUNSELOR')
  }
  function isAdmin() {
    return hasRole('ADMIN')
  }

  function applyToken(t) {
    token.value = t || ''
    setToken(t)
  }

  async function login(payload) {
    const { data } = await apiLogin(payload)
    if (data.code !== 200 || !data.data?.token) {
      throw new Error(data.message || '登录失败')
    }
    applyToken(data.data.token)
    profile.value = {
      id: data.data.user?.id,
      username: data.data.user?.username,
      realName: data.data.user?.realName,
      roleCodes: data.data.roles || []
    }
    await fetchProfile()
    return data.data
  }

  async function fetchProfile() {
    if (!token.value) {
      profile.value = null
      return null
    }
    try {
      const { data } = await http.get('/auth/profile')
      if (data.code === 200 && data.data) {
        profile.value = data.data
        return data.data
      }
    } catch {
      profile.value = null
    }
    return null
  }

  function logout() {
    applyToken('')
    profile.value = null
  }

  function restoreFromStorage() {
    token.value = getToken() || ''
  }

  return {
    token,
    profile,
    isLoggedIn,
    roles,
    hasRole,
    isStudent,
    isCounselor,
    isAdmin,
    applyToken,
    login,
    fetchProfile,
    logout,
    restoreFromStorage
  }
})
