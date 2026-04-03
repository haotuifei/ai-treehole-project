<template>
  <div class="login-page">
    <div class="background-elements">
      <div class="moon"></div>
      <div class="light-spot spot-1"></div>
      <div class="light-spot spot-2"></div>
      <div class="light-spot spot-3"></div>
      <div class="light-spot spot-4"></div>
      <div class="leaf leaf-1"></div>
      <div class="leaf leaf-2"></div>
      <div class="leaf leaf-3"></div>
    </div>

    <div class="login-card">
      <div class="card-header">
        <h1 class="title">欢迎回来</h1>
        <p class="subtitle">在这里，每一份心事都值得被倾听</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @submit.prevent>
        <el-form-item prop="username">
          <div 
            class="input-wrapper" 
            :class="{ 'is-focused': usernameFocused }"
            @mouseenter="usernameHovered = true"
            @mouseleave="usernameHovered = false"
          >
            <span class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            </span>
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              class="custom-input"
              @focus="usernameFocused = true"
              @blur="usernameFocused = false"
            />
            <span
              class="input-clear"
              :class="{ 'is-visible': usernameHovered && form.username }"
              @click="form.username = ''"
            >
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
            </span>
          </div>
        </el-form-item>

        <el-form-item prop="password">
          <div 
            class="input-wrapper" 
            :class="{ 'is-focused': passwordFocused }"
            @mouseenter="passwordHovered = true"
            @mouseleave="passwordHovered = false"
          >
            <span class="input-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
            </span>
            <el-input
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              class="custom-input"
              @focus="passwordFocused = true"
              @blur="passwordFocused = false"
              @keyup.enter="onSubmit"
            />
            <span
              class="password-toggle"
              :class="{ 'is-visible': passwordHovered && form.password }"
              @click="showPassword = !showPassword"
            >
              <svg v-if="!showPassword" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            </span>
          </div>
        </el-form-item>

        <el-button
          type="primary"
          class="login-btn"
          :loading="loading"
          @click="onSubmit"
        >
          <span v-if="!loading">登录</span>
        </el-button>
      </el-form>

      <div class="card-footer">
        <p class="footer-text">说不出口的话，留给树洞吧</p>
      </div>
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
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const usernameHovered = ref(false)
const passwordHovered = ref(false)
const showPassword = ref(false)

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
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #E6F7EE 0%, #d9f7be 25%, #f6ffed 50%, #d9f7be 75%, #E6F7EE 100%);
}

.background-elements {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.moon {
  position: absolute;
  top: 8%;
  right: 12%;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: radial-gradient(circle at 30% 30%, #fff9e6 0%, #ffeaa7 50%, #fdcb6e 100%);
  box-shadow: 0 0 60px rgba(255, 234, 167, 0.6), 0 0 100px rgba(255, 234, 167, 0.4);
  opacity: 0.7;
  animation: moonFloat 8s ease-in-out infinite;
}

@keyframes moonFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

.light-spot {
  position: absolute;
  border-radius: 50%;
  filter: blur(40px);
  opacity: 0.5;
  animation: spotPulse 6s ease-in-out infinite;
}

.spot-1 {
  top: 10%;
  left: 15%;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(82, 196, 26, 0.4) 0%, transparent 70%);
  animation-delay: 0s;
}

.spot-2 {
  top: 60%;
  left: 5%;
  width: 150px;
  height: 150px;
  background: radial-gradient(circle, rgba(135, 208, 104, 0.4) 0%, transparent 70%);
  animation-delay: 2s;
}

.spot-3 {
  top: 20%;
  right: 5%;
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(183, 235, 143, 0.4) 0%, transparent 70%);
  animation-delay: 1s;
}

.spot-4 {
  bottom: 15%;
  right: 15%;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(149, 222, 100, 0.4) 0%, transparent 70%);
  animation-delay: 3s;
}

@keyframes spotPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 0.6; }
}

.leaf {
  position: absolute;
  width: 30px;
  height: 30px;
  opacity: 0.3;
  animation: leafFall 15s linear infinite;
}

.leaf::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #a8d8a8 0%, #7cb87c 100%);
  border-radius: 0 50% 50% 50%;
  transform: rotate(45deg);
}

.leaf-1 {
  left: 10%;
  animation-delay: 0s;
}

.leaf-2 {
  left: 50%;
  animation-delay: 5s;
}

.leaf-3 {
  right: 20%;
  animation-delay: 10s;
}

@keyframes leafFall {
  0% {
    top: -5%;
    transform: rotate(0deg) translateX(0);
    opacity: 0;
  }
  10% { opacity: 0.3; }
  90% { opacity: 0.3; }
  100% {
    top: 105%;
    transform: rotate(360deg) translateX(100px);
    opacity: 0;
  }
}

.login-card {
  width: 100%;
  max-width: 380px;
  padding: 40px 36px 36px;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  box-shadow:
    0 8px 32px rgba(82, 196, 26, 0.1),
    0 2px 8px rgba(82, 196, 26, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.5);
  position: relative;
  z-index: 10;
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.title {
  margin: 0 0 8px;
  font-size: 1.75rem;
  font-weight: 600;
  color: #4a5568;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(255, 255, 255, 0.8);
}

.subtitle {
  margin: 0;
  font-size: 0.9rem;
  color: #718096;
  line-height: 1.5;
  font-weight: 400;
}

.login-form {
  margin-bottom: 24px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.login-form :deep(.el-form-item__content) {
  width: 100%;
}

.login-form :deep(.el-form-item__error) {
  padding-top: 4px;
  font-size: 0.8rem;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.input-icon {
  position: absolute;
  left: 14px;
  width: 18px;
  height: 18px;
  color: #a0aec0;
  z-index: 2;
  pointer-events: none;
  transition: color 0.3s ease;
}

.input-wrapper.is-focused .input-icon {
  color: #52c41a;
}

.input-icon svg {
  width: 100%;
  height: 100%;
}

.custom-input {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  height: 46px;
  padding: 0 40px 0 42px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.08);
  border: 1px solid rgba(183, 235, 143, 0.5);
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: rgba(82, 196, 26, 0.5);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: rgba(82, 196, 26, 0.8);
  box-shadow: 0 0 0 3px rgba(82, 196, 26, 0.15), 0 2px 8px rgba(82, 196, 26, 0.1);
}

.custom-input :deep(.el-input__inner) {
  height: 46px;
  line-height: 46px;
  color: #4a5568;
  font-size: 0.95rem;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: #a0aec0;
}

.custom-input :deep(.el-input__suffix) {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.custom-input :deep(.el-input__suffix-inner) {
  display: flex;
  align-items: center;
  height: 100%;
}

.custom-input :deep(.el-input__clear) {
  width: 18px;
  height: 18px;
  color: #a0aec0;
  transition: all 0.3s ease;
  line-height: 1;
}

.custom-input :deep(.el-input__clear:hover) {
  color: #52c41a;
}

.input-clear {
  position: absolute;
  right: 14px;
  width: 18px;
  height: 18px;
  color: #a0aec0;
  cursor: pointer;
  z-index: 2;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-clear:hover {
  color: #52c41a;
}

.input-clear.is-visible {
  opacity: 1;
  transform: scale(1);
}

.input-clear svg {
  width: 100%;
  height: 100%;
}

.password-toggle {
  position: absolute;
  right: 14px;
  width: 18px;
  height: 18px;
  color: #a0aec0;
  cursor: pointer;
  z-index: 2;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.password-toggle:hover {
  color: #52c41a;
}

.password-toggle.is-visible {
  opacity: 1;
  transform: scale(1);
}

.password-toggle svg {
  width: 100%;
  height: 100%;
}

.login-btn {
  width: 100%;
  height: 46px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  letter-spacing: 0.05em;
  border: none;
  background: linear-gradient(135deg, #498022 0%, #7ef738 50%, #498022 100%);
  background-size: 200% 200%;
  background-position: 0% 50%;
  box-shadow: 0 4px 15px rgba(82, 196, 26, 0.25);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.login-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.3),
    transparent
  );
  transition: left 0.5s ease;
}

@keyframes gradientMove {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

@keyframes pulse {
  0%, 100% { 
    box-shadow: 0 4px 15px rgba(82, 196, 26, 0.25);
  }
  50% { 
    box-shadow: 0 4px 25px rgba(82, 196, 26, 0.45);
  }
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(82, 196, 26, 0.35);
  animation: gradientMove 3s ease infinite, pulse 2s ease-in-out infinite;
}

.login-btn:hover::before {
  animation: shimmer 1.5s ease infinite;
}

.login-btn:active {
  transform: translateY(0) scale(0.98);
  box-shadow: 0 2px 10px rgba(82, 196, 26, 0.25);
  animation: none;
}

.login-btn :deep(.el-loading-spinner) {
  margin-top: -10px;
}

.card-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid rgba(203, 213, 224, 0.3);
}

.footer-text {
  margin: 0;
  font-size: 0.85rem;
  color: #a0aec0;
  font-style: italic;
  letter-spacing: 0.02em;
}

@media (max-width: 480px) {
  .login-card {
    padding: 32px 24px 28px;
  }

  .title {
    font-size: 1.5rem;
  }

  .subtitle {
    font-size: 0.85rem;
  }

  .moon {
    width: 60px;
    height: 60px;
    top: 5%;
    right: 8%;
  }
}

@media (prefers-reduced-motion: reduce) {
  .moon,
  .light-spot,
  .leaf,
  .login-btn {
    animation: none;
  }
}
</style>
