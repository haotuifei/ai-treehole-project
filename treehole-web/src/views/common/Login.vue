<template>
  <div class="login-page">
    <div class="panel th-card">
      <h1 class="title">欢迎回来</h1>
      <p class="sub">备考路上，树洞陪你慢慢走。</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="form" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名" size="large" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="btn" :loading="loading" round @click="onSubmit">
          登录
        </el-button>
      </el-form>
    </div>

    <WelcomeTransition :visible="showWelcome" :username="form.username" @complete="onWelcomeComplete" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'
import WelcomeTransition from '../../components/common/WelcomeTransition.vue'

const route = useRoute()
const router = useRouter()
const user = useUserStore()

const formRef = ref()
const loading = ref(false)
const showWelcome = ref(false)
const redirectPath = ref('/home')

const form = reactive({
  username: '',
  password: ''
})
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  await formRef.value?.validate().catch(() => null)
  if (!form.username || !form.password) return
  loading.value = true
  try {
    await user.login({ username: form.username, password: form.password })
    const redir = route.query.redirect
    redirectPath.value = typeof redir === 'string' && redir.startsWith('/') ? redir : '/home'
    showWelcome.value = true
  } catch (e) {
    const msg = e?.response?.data?.message || e?.message || '登录失败'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}

function onWelcomeComplete() {
  showWelcome.value = false
  router.push(redirectPath.value)
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background:
    radial-gradient(ellipse 80% 60% at 50% -20%, rgba(124, 154, 130, 0.25), transparent),
    var(--th-bg);
}
.panel {
  width: 100%;
  max-width: 400px;
  padding: 36px 32px 40px;
}
.title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}
.sub {
  margin: 8px 0 28px;
  font-size: 0.95rem;
  color: var(--th-text-muted);
  line-height: 1.5;
}
.form :deep(.el-form-item__label) {
  color: var(--th-text-muted);
  font-weight: 500;
}
.btn {
  width: 100%;
  margin-top: 8px;
}
</style>
