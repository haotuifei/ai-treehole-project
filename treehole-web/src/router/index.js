import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/http'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    meta: { title: '登录', public: true },
    component: () => import('../views/common/Login.vue')
  },
  {
    path: '/',
    component: () => import('../components/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/home' },
      {
        path: 'home',
        name: 'Home',
        meta: { title: '首页' },
        component: () => import('../views/common/Home.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        meta: { title: '个人中心' },
        component: () => import('../views/common/Profile.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        meta: { title: '设置' },
        component: () => import('../views/common/Settings.vue')
      },
      {
        path: 'student/goals',
        name: 'StudentGoals',
        meta: { title: '目标管理', roles: ['STUDENT'] },
        component: () => import('../views/student/Goals.vue')
      },
      {
        path: 'student/checkin',
        name: 'StudentCheckin',
        meta: { title: '学习打卡', roles: ['STUDENT'] },
        component: () => import('../views/student/Checkin.vue')
      },
      {
        path: 'student/emotion',
        name: 'StudentEmotion',
        meta: { title: 'Psychological SO', roles: ['STUDENT'] },
        component: () => import('../views/student/EmotionArchive.vue')
      },
      {
        path: 'student/history',
        name: 'StudentHistory',
        meta: { title: '历史记录', roles: ['STUDENT'] },
        component: () => import('../views/student/History.vue')
      },
      {
        path: 'counselor/students',
        name: 'CounselorStudents',
        meta: { title: '学生档案', roles: ['COUNSELOR'] },
        component: () => import('../views/counselor/StudentProfiles.vue')
      },
      {
        path: 'counselor/warnings',
        name: 'CounselorWarnings',
        meta: { title: '情绪预警', roles: ['COUNSELOR'] },
        component: () => import('../views/counselor/EmotionWarnings.vue')
      },
      {
        path: 'counselor/interventions',
        name: 'CounselorInterventions',
        meta: { title: '干预记录', roles: ['COUNSELOR'] },
        component: () => import('../views/counselor/Interventions.vue')
      },
      {
        path: 'counselor/class-analytics',
        name: 'CounselorClassAnalytics',
        meta: { title: '班级数据分析', roles: ['COUNSELOR'] },
        component: () => import('../views/counselor/ClassAnalytics.vue')
      },
      {
        path: 'admin/users',
        name: 'AdminUsers',
        meta: { title: '用户管理', roles: ['ADMIN'] },
        component: () => import('../views/admin/UserManage.vue')
      },
      {
        path: 'admin/models',
        name: 'AdminModels',
        meta: { title: '模型配置', roles: ['ADMIN'] },
        component: () => import('../views/admin/ModelConfig.vue')
      },
      {
        path: 'admin/alert-rules',
        name: 'AdminAlertRules',
        meta: { title: '预警规则', roles: ['ADMIN'] },
        component: () => import('../views/admin/AlertRules.vue')
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        meta: { title: '系统日志', roles: ['ADMIN'] },
        component: () => import('../views/admin/SystemLogs.vue')
      },
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        meta: { title: '全局数据大屏', roles: ['ADMIN'] },
        component: () => import('../views/admin/DashboardScreen.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  document.title = to.meta?.title ? `${to.meta.title} · 树洞` : '树洞'

  if (to.meta?.public) {
    if (getToken() && to.path === '/login') {
      const userStore = useUserStore()
      userStore.restoreFromStorage()
      if (userStore.isAdmin()) {
        next('/admin/users')
      } else if (userStore.isCounselor()) {
        next('/counselor/students')
      } else {
        next('/home')
      }
      return
    }
    next()
    return
  }

  if (!getToken()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  const userStore = useUserStore()
  userStore.restoreFromStorage()
  if (!userStore.profile) {
    await userStore.fetchProfile()
  }

  // 管理员访问首页时重定向到管理页面
  if (to.path === '/home' && userStore.isAdmin()) {
    next('/admin/users')
    return
  }

  // 辅导员访问首页时重定向到学生档案
  if (to.path === '/home' && userStore.isCounselor()) {
    next('/counselor/students')
    return
  }

  const need = to.matched.find((r) => r.meta?.roles)?.meta?.roles
  if (need?.length) {
    const ok = need.some((role) => userStore.roles.includes(role))
    if (!ok) {
      next('/home')
      return
    }
  }

  next()
})

export default router
