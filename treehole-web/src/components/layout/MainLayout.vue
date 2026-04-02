<template>
  <el-container class="layout-root">
    <el-aside :width="collapsed ? '72px' : '220px'" class="aside">
      <div class="brand" @click="$router.push('/home')">
        <span class="brand-dot" />
        <span v-show="!collapsed" class="brand-text">树洞</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          router
          class="th-menu"
          background-color="transparent"
          text-color="var(--th-text)"
          active-text-color="var(--th-primary)"
        >
          <el-menu-item index="/home">
            <el-icon><House /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>

          <template v-if="user.isStudent()">
            <div v-show="!collapsed" class="menu-group">学生</div>
            <el-menu-item index="/student/goals">目标管理</el-menu-item>
            <el-menu-item index="/student/checkin">学习打卡</el-menu-item>
            <el-menu-item index="/student/treehole">AI 树洞</el-menu-item>
            <el-menu-item index="/student/emotion">情绪档案</el-menu-item>
            <el-menu-item index="/student/history">历史记录</el-menu-item>
          </template>

          <template v-if="user.isCounselor()">
            <div v-show="!collapsed" class="menu-group">辅导员</div>
            <el-menu-item index="/counselor/students">学生档案</el-menu-item>
            <el-menu-item index="/counselor/warnings">情绪预警</el-menu-item>
            <el-menu-item index="/counselor/interventions">干预记录</el-menu-item>
            <el-menu-item index="/counselor/class-analytics">班级数据</el-menu-item>
          </template>

          <template v-if="user.isAdmin()">
            <div v-show="!collapsed" class="menu-group">管理</div>
            <el-menu-item index="/admin/users">用户管理</el-menu-item>
            <el-menu-item index="/admin/models">模型配置</el-menu-item>
            <el-menu-item index="/admin/alert-rules">预警规则</el-menu-item>
            <el-menu-item index="/admin/logs">系统日志</el-menu-item>
            <el-menu-item index="/admin/dashboard">数据大屏</el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container direction="vertical" class="main-wrap">
      <el-header class="header">
        <el-button text class="collapse-btn" @click="app.toggleSidebar()">
          <el-icon><Fold v-if="!collapsed" /><Expand v-else /></el-icon>
        </el-button>
        <span class="header-title">{{ headerTitle }}</span>
        <div class="header-right">
          <span class="who">{{ displayName }}</span>
          <el-button type="primary" plain round size="small" @click="onLogout">退出</el-button>
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
import { House, User, Fold, Expand } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'
import { useAppStore } from '../../stores/app'

const route = useRoute()
const router = useRouter()
const user = useUserStore()
const app = useAppStore()

const collapsed = computed(() => app.sidebarCollapsed)
const activeMenu = computed(() => route.path)

const displayName = computed(
  () => user.profile?.realName || user.profile?.username || '访客'
)

const headerTitle = computed(() => {
  const m = route.meta?.title
  return m ? String(m) : ''
})

function onLogout() {
  user.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-root {
  min-height: 100vh;
  background: var(--th-bg);
}
.aside {
  background: var(--th-surface);
  border-right: 1px solid var(--th-border);
  transition: width 0.2s ease;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  cursor: pointer;
  font-weight: 600;
  color: var(--th-text);
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
.th-menu :deep(.el-menu-item) {
  border-radius: var(--th-radius-sm);
  margin: 4px 10px;
  height: 42px;
}
.th-menu :deep(.el-menu-item.is-active) {
  background: var(--th-primary-soft) !important;
  font-weight: 600;
}
.main-wrap {
  min-width: 0;
}
.header {
  height: 56px !important;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 20px;
  background: var(--th-surface);
  border-bottom: 1px solid var(--th-border);
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
  min-height: calc(100vh - 56px);
}
</style>
