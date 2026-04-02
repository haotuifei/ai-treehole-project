<template>
  <div class="th-page">
    <h1 class="th-page-title">你好，{{ greeting }}</h1>
    <p class="th-page-desc">这里是你的心理陪伴空间，按需进入各功能模块即可。</p>

    <el-row :gutter="16">
      <el-col v-for="card in cards" :key="card.path" :xs="24" :sm="12" :md="8">
        <div class="entry th-card" @click="go(card.path)">
          <div class="entry-icon">{{ card.emoji }}</div>
          <div class="entry-title">{{ card.title }}</div>
          <div class="entry-desc">{{ card.desc }}</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const user = useUserStore()

const greeting = computed(() => user.profile?.realName || user.profile?.username || '同学')

const cards = computed(() => {
  const list = [
    { path: '/profile', title: '个人中心', desc: '资料与安全', emoji: '🌿', show: true }
  ]
  if (user.isStudent()) {
    list.unshift(
      { path: '/student/treehole', title: 'AI 树洞', desc: '倾诉与陪伴', emoji: '🫧', show: true },
      { path: '/student/goals', title: '目标管理', desc: '备考目标', emoji: '🎯', show: true },
      { path: '/student/checkin', title: '学习打卡', desc: '每日记录', emoji: '✨', show: true }
    )
  }
  if (user.isCounselor()) {
    list.unshift({
      path: '/counselor/warnings',
      title: '情绪预警',
      desc: '关注与跟进',
      emoji: '💚',
      show: true
    })
  }
  if (user.isAdmin()) {
    list.unshift({
      path: '/admin/dashboard',
      title: '数据大屏',
      desc: '全局概览',
      emoji: '📊',
      show: true
    })
  }
  return list.filter((c) => c.show)
})

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.entry {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.entry:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(61, 58, 54, 0.08);
}
.entry-icon {
  font-size: 1.75rem;
  margin-bottom: 10px;
}
.entry-title {
  font-weight: 600;
  margin-bottom: 6px;
}
.entry-desc {
  font-size: 0.88rem;
  color: var(--th-text-muted);
}
</style>
