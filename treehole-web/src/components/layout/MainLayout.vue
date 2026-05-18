<template>
  <el-container class="layout-root">
    <div 
      v-if="isMobile && !collapsed" 
      class="sidebar-overlay" 
      @click="app.closeSidebar"
    />
    <el-aside 
      :class="['aside', { 'aside-mobile': isMobile, 'aside-open': !collapsed }]" 
      :style="asideStyle"
    >
      <div class="aside-inner">
        <div class="brand" @click="handleBrandClick">
          <span class="brand-dot" />
          <span class="brand-text">树洞</span>
        </div>
        <el-scrollbar class="menu-scrollbar">
          <el-menu
            :default-active="activeMenu"
            :collapse="false"
            router
            class="th-menu"
            background-color="transparent"
            text-color="var(--th-text)"
            active-text-color="var(--th-primary)"
            @select="handleMenuSelect"
          >
            <el-menu-item v-if="user.isStudent()" index="/home">
              <el-icon><House /></el-icon>
              <template #title>首页</template>
            </el-menu-item>
            <el-menu-item v-if="!user.isAdmin()" index="/profile">
              <el-icon><User /></el-icon>
              <template #title>个人中心</template>
            </el-menu-item>

            <template v-if="user.isStudent()">
              <div class="menu-group">学生</div>
              <el-menu-item index="/student/goals">
                <el-icon><Place /></el-icon>
                <template #title>目标管理</template>
              </el-menu-item>
              <el-menu-item index="/student/checkin">
                <el-icon><Calendar /></el-icon>
                <template #title>学习打卡</template>
              </el-menu-item>
              <el-menu-item index="/student/emotion">
                <el-icon><Collection /></el-icon>
                <template #title>Psychological SO</template>
              </el-menu-item>
              <el-menu-item index="/student/history">
                <el-icon><Clock /></el-icon>
                <template #title>历史记录</template>
              </el-menu-item>
            </template>

            <template v-if="user.isCounselor()">
              <div class="menu-group">辅导员</div>
              <el-menu-item index="/counselor/students">
                <el-icon><User /></el-icon>
                <template #title>学生档案</template>
              </el-menu-item>
              <el-menu-item index="/counselor/warnings">
                <el-icon><Warning /></el-icon>
                <template #title>情绪预警</template>
              </el-menu-item>
              <el-menu-item index="/counselor/interventions">
                <el-icon><Notebook /></el-icon>
                <template #title>干预记录</template>
              </el-menu-item>
              <el-menu-item index="/counselor/class-analytics">
                <el-icon><DataLine /></el-icon>
                <template #title>班级数据</template>
              </el-menu-item>
            </template>

            <template v-if="user.isAdmin()">
              <div class="menu-group">管理</div>
              <el-menu-item index="/admin/users">
                <el-icon><UserFilled /></el-icon>
                <template #title>用户管理</template>
              </el-menu-item>
              <el-menu-item index="/admin/models">
                <el-icon><Setting /></el-icon>
                <template #title>模型配置</template>
              </el-menu-item>
              <el-menu-item index="/admin/alert-rules">
                <el-icon><Bell /></el-icon>
                <template #title>预警规则</template>
              </el-menu-item>
              <el-menu-item index="/admin/logs">
                <el-icon><Document /></el-icon>
                <template #title>系统日志</template>
              </el-menu-item>
              <el-menu-item index="/admin/dashboard">
                <el-icon><DataBoard /></el-icon>
                <template #title>数据大屏</template>
              </el-menu-item>
            </template>
          </el-menu>
        </el-scrollbar>
        <div class="aside-bottom">
          <el-menu
            :default-active="activeMenu"
            :collapse="false"
            router
            class="th-menu bottom-menu"
            background-color="transparent"
            text-color="var(--th-text)"
            active-text-color="var(--th-primary)"
            @select="handleMenuSelect"
          >
            <el-menu-item index="/settings">
              <el-icon><Setting /></el-icon>
              <template #title>设置</template>
            </el-menu-item>
          </el-menu>
        </div>
      </div>
    </el-aside>

    <el-container direction="vertical" class="main-wrap">
      <el-header class="header">
        <el-button text class="collapse-btn" @click="app.toggleSidebar()">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        </el-button>
        <span class="header-title">{{ headerTitle }}</span>
        <div class="header-right">
          <span class="who">{{ displayName }}</span>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  House, User, Fold, Expand,
  Place, Calendar, Collection, Clock,
  Warning, Notebook, DataLine,
  UserFilled, Setting, Bell, Document, DataBoard
} from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const app = useAppStore()

const collapsed = computed(() => app.sidebarCollapsed)
const isMobile = computed(() => app.isMobile)
const activeMenu = computed(() => route.path)

const asideStyle = computed(() => {
  if (isMobile.value) {
    return {}
  }
  return { width: collapsed.value ? '0px' : '220px' }
})

const displayName = computed(
  () => user.profile?.realName || user.profile?.username || '访客'
)

const headerTitle = computed(() => {
  const m = route.meta?.title
  return m ? String(m) : ''
})

function handleBrandClick() {
  if (user.isAdmin()) {
    router.push('/admin/users')
  } else if (user.isCounselor()) {
    router.push('/counselor/students')
  } else {
    router.push('/home')
  }
  if (isMobile.value) {
    app.closeSidebar()
  }
}

function handleMenuSelect() {
  if (isMobile.value) {
    app.closeSidebar()
  }
}

</script>

<style scoped>
.layout-root {
  height: 100vh;
  background: var(--th-bg);
  overflow: hidden;
}

.sidebar-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 99;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.aside {
  background: var(--th-surface);
  border-right: 1px solid var(--th-border);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  flex-shrink: 0;
  height: 100vh;
}

.aside-mobile {
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 220px;
  z-index: 100;
  transform: translateX(-100%);
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: none;
}

.aside-mobile.aside-open {
  transform: translateX(0);
  box-shadow: 4px 0 12px rgba(0, 0, 0, 0.15);
}

.aside-inner {
  width: 220px;
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  cursor: pointer;
  font-weight: 600;
  color: var(--th-text);
  flex-shrink: 0;
}

.brand-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--th-primary), var(--th-accent));
  flex-shrink: 0;
}

.brand-text {
  font-size: 1.1rem;
  letter-spacing: 0.08em;
}

.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.menu-scrollbar :deep(.el-scrollbar__wrap) {
  overflow-x: hidden;
}

.menu-group {
  padding: 14px 20px 6px;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--th-text-muted);
}

.th-menu {
  border-right: none !important;
  padding-bottom: 24px;
}

.bottom-menu {
  padding: 8px 0 16px;
}

.th-menu:not(.el-menu--collapse) {
  width: 220px;
}

.th-menu :deep(.el-menu-item) {
  border-radius: var(--th-radius-sm);
  margin: 4px 10px;
  height: 42px;
}

.th-menu :deep(.el-menu-item .el-menu-item__icon-wrapper) {
  margin-right: 10px;
}

.th-menu :deep(.el-menu-item.is-active) {
  background: var(--th-primary-soft) !important;
  font-weight: 600;
}

.main-wrap {
  min-width: 0;
  flex: 1;
  width: 100%;
  height: 100vh;
  overflow: hidden;
}

.aside-bottom {
  border-top: 1px solid var(--th-border);
  padding-top: 8px;
  flex-shrink: 0;
  background: var(--th-surface);
}

.header {
  height: 56px !important;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  background: var(--th-surface);
  border-bottom: 1px solid var(--th-border);
  flex-shrink: 0;
}

.collapse-btn {
  font-size: 18px;
  color: var(--th-text-muted);
}

.header-title {
  flex: 1;
  font-size: 15px;
  color: var(--th-text-muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.who {
  font-size: 14px;
  color: var(--th-text);
}

.main {
  padding: 0;
  background: var(--th-bg);
  height: calc(100vh - 56px);
  overflow-x: hidden;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .header {
    padding: 0 12px;
  }
  
  .header-title {
    font-size: 14px;
  }
  
  .who {
    display: none;
  }
}
</style>
