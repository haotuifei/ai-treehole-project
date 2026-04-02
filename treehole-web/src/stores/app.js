import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 布局与轻量 UI 状态 */
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  return { sidebarCollapsed, toggleSidebar }
})
