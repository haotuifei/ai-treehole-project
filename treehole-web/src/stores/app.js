import { defineStore } from 'pinia'
import { ref, computed, onMounted, onUnmounted } from 'vue'

/** 布局与轻量 UI 状态 */
export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(true)
  const screenWidth = ref(typeof window !== 'undefined' ? window.innerWidth : 1200)

  const isMobile = computed(() => screenWidth.value < 768)

  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  function closeSidebar() {
    sidebarCollapsed.value = true
  }

  function updateScreenWidth() {
    screenWidth.value = window.innerWidth
    if (isMobile.value && !sidebarCollapsed.value) {
      sidebarCollapsed.value = true
    }
  }

  if (typeof window !== 'undefined') {
    onMounted(() => {
      window.addEventListener('resize', updateScreenWidth)
      updateScreenWidth()
    })

    onUnmounted(() => {
      window.removeEventListener('resize', updateScreenWidth)
    })
  }

  return { 
    sidebarCollapsed, 
    toggleSidebar,
    closeSidebar,
    screenWidth,
    isMobile
  }
})
