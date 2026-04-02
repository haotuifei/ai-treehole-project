<template>
  <div class="th-page">
    <h1 class="th-page-title">个人中心</h1>
    <p class="th-page-desc">查看与维护你的账号信息（接口联调后展示完整字段）。</p>

    <div class="th-card profile-card">
      <el-descriptions v-if="user.profile" :column="1" border>
        <el-descriptions-item label="用户名">{{ user.profile.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ user.profile.realName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="学号">{{ user.profile.studentNo || '—' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ user.profile.className || '—' }}</el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag v-for="n in user.profile.roleNames || []" :key="n" class="tag" type="success" effect="plain">
            {{ n }}
          </el-tag>
          <span v-if="!(user.profile.roleNames || []).length">{{ (user.profile.roleCodes || []).join(', ') || '—' }}</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-skeleton v-else :rows="5" animated />
      <div class="actions">
        <el-button type="primary" plain round @click="refresh">刷新资料</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const user = useUserStore()

async function refresh() {
  try {
    await user.fetchProfile()
    ElMessage.success('已更新')
  } catch {
    ElMessage.error('刷新失败')
  }
}

onMounted(() => {
  if (user.isLoggedIn) refresh()
})
</script>

<style scoped>
.profile-card {
  max-width: 560px;
}
.tag {
  margin-right: 6px;
}
.actions {
  margin-top: 20px;
}
</style>
