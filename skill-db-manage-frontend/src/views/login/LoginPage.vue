<template>
  <div class="login-page">
    <ParticleBg />

    <div class="content-wrapper">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <!-- 标题光场容器：在标题背后打一个柔光环，让文字漂浮在干净空气中 -->
        <div class="brand-glow"></div>
        <div class="brand-content">
          <!-- 顶部装饰线 -->
          <div class="top-decoration">
            <span class="decoration-line"></span>
            <span class="decoration-dot"></span>
            <span class="decoration-line"></span>
          </div>

          <!-- 主标题 - 打字机效果 -->
          <h1 class="main-title" v-if="showTitle">
            <TextScramble
              :text="'Skill Manager'"
              :type-speed="80"
              :start-delay="400"
              font-size="56px"
              color="#fff"
            />
          </h1>

          <!-- 副标题 - 打字机效果 -->
          <p class="sub-title">
            <TextScramble
              :text="'Intelligent Permission Management Platform'"
              :type-speed="60"
              :start-delay="2800"
              font-size="16px"
              color="rgba(255,255,255,0.5)"
            />
          </p>

          <!-- 特性标签（静态展示） -->
          <div class="features">
            <div
              v-for="(feature, index) in features"
              :key="feature.text"
              class="feature-tag"
              :style="{ animationDelay: `${4 + index * 0.3}s` }"
            >
              <span class="feature-icon">
                <Icon :icon="feature.icon" width="12" height="12" />
              </span>
              <span class="feature-text">{{ feature.text }}</span>
            </div>
          </div>

          <!-- 底部版本信息（静态） -->
          <div class="version-info">
            <span style="font-size:11px; color:rgba(255,255,255,0.15)">v1.0.0 · Secure by Design</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="login-section">
        <div class="login-card">
          <div class="login-card-header">
            <div class="card-logo">
              <span class="logo-icon">
                <Icon icon="ri:login-circle-line" width="24" height="24" />
              </span>
              <span class="logo-text">
                <TextScramble
                  text="Sign In"
                  :type-speed="60"
                  :start-delay="0"
                  font-size="20px"
                  color="#fff"
                />
              </span>
            </div>
            <p class="card-welcome">
              <TextScramble
                text="登录以管理您的系统权限"
                :type-speed="50"
                :start-delay="3500"
                font-size="13px"
                color="rgba(255,255,255,0.45)"
              />
            </p>
          </div>

          <!-- Tab切换 -->
          <div class="login-tabs">
            <button
              :class="['tab-btn', { active: activeTab === 'password' }]"
              @click="activeTab = 'password'"
            >
              <span class="tab-indicator"></span>
              <Icon icon="ri:user-3-line" width="14" height="14" class="tab-icon" />
              账号登录
            </button>
            <button
              :class="['tab-btn', { active: activeTab === 'sms' }]"
              @click="activeTab = 'sms'"
            >
              <span class="tab-indicator"></span>
              <Icon icon="ri:smartphone-line" width="14" height="14" class="tab-icon" />
              手机登录
            </button>
          </div>

          <!-- 登录表单 -->
          <transition name="slide-fade" mode="out-in">
            <LoginForm
              v-if="activeTab === 'password'"
              key="password"
              @login="handlePasswordLogin"
            />
            <PhoneLogin
              v-else
              key="sms"
              @login="handleSmsLogin"
            />
          </transition>

          <!-- 注册链接（静态） -->
          <div class="register-link">
            <span style="font-size:12px; color:rgba(255,255,255,0.3)">还没有账号？立即注册</span>
          </div>
        </div>

        <!-- 底部版权（静态） -->
        <div class="copyright">
          <span style="font-size:11px; color:rgba(255,255,255,0.12)">© {{ new Date().getFullYear() }} skill-manage.com</span>
        </div>
      </div>
    </div>

    <!-- 角落装饰 -->
    <div class="corner-decoration top-left">[</div>
    <div class="corner-decoration top-right">]</div>
    <div class="corner-decoration bottom-left">(</div>
    <div class="corner-decoration bottom-right">)</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { Icon } from '@iconify/vue'
import ParticleBg from './components/ParticleBg.vue'
import TextScramble from './components/TextScramble.vue'
import LoginForm from './LoginForm.vue'
import PhoneLogin from './PhoneLogin.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const activeTab = ref('password')
const showTitle = ref(false)

const features = [
  { icon: 'ri:shield-check-line', text: 'RBAC 权限控制' },
  { icon: 'ri:route-line', text: '动态路由生成' },
  { icon: 'ri:key-2-line', text: '多因素认证' },
  { icon: 'ri:file-list-3-line', text: '审计日志追踪' }
]

onMounted(() => {
  setTimeout(() => { showTitle.value = true }, 100)
})

async function handlePasswordLogin(loginData) {
  try {
    await userStore.login(loginData)
    const dynamicRoutes = permissionStore.generateRoutes()
    const rootRoute = router.getRoutes().find(r => r.name === 'Root')
    dynamicRoutes.forEach(route => {
      if (rootRoute) router.addRoute('Root', route)
      else router.addRoute(route)
    })
    const redirect = route.query.redirect || '/'
    router.push(redirect)
    Message.success('登录成功')
  } catch (e) {
    Message.error(e.message || '登录失败')
  }
}

async function handleSmsLogin(loginData) {
  try {
    await userStore.smsLogin(loginData)
    const dynamicRoutes = permissionStore.generateRoutes()
    const rootRoute = router.getRoutes().find(r => r.name === 'Root')
    dynamicRoutes.forEach(route => {
      if (rootRoute) router.addRoute('Root', route)
      else router.addRoute(route)
    })
    const redirect = route.query.redirect || '/'
    router.push(redirect)
    Message.success('登录成功')
  } catch (e) {
    Message.error(e.message || '登录失败')
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #050505;
  display: flex;
  overflow: hidden;
}

.content-wrapper {
  position: relative;
  z-index: 10;
  display: flex;
  width: 100%;
  height: 100%;
}

/* 左侧品牌区 */
.brand-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 80px 60px;
  position: relative;
  overflow: visible;
}

/* 标题光场容器 — 在标题背后投射柔光环，让文字"漂浮在干净空气中" */
.brand-glow {
  position: absolute;
  left: -120px;
  top: 50%;
  transform: translateY(-50%);
  width: 600px;
  height: 400px;
  background: radial-gradient(
    ellipse at center,
    rgba(99, 102, 241, 0.06) 0%,
    rgba(99, 102, 241, 0.03) 25%,
    rgba(0, 0, 0, 0) 70%
  );
  pointer-events: none;
  z-index: 0;
}

.brand-content {
  position: relative;
  z-index: 1;
  max-width: 520px;
}

.top-decoration {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.decoration-line {
  flex: 1;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.3), transparent);
}

.decoration-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #6366F1;
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.5);
}

.main-title {
  font-weight: 700;
  line-height: 1.15;
  letter-spacing: -1px;
  margin-bottom: 16px;
  min-height: 64px;
}

.sub-title {
  margin-bottom: 48px;
  min-height: 24px;
}

.features {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 60px;
}

.feature-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border: 1px solid rgba(99, 102, 241, 0.1);
  border-radius: 20px;
  background: rgba(99, 102, 241, 0.03);
  animation: fadeIn 0.6s ease both;
}

.feature-icon {
  color: #6366F1;
  font-size: 10px;
}

.feature-text {
  color: rgba(255, 255, 255, 0.75);
}

.version-info {
  margin-top: 8px;
}

/* 右侧登录区 */
.login-section {
  position: relative;
  width: 420px;
  min-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 40px 40px;
  border-left: 1px solid rgba(255, 255, 255, 0.04);
  background: transparent;
  z-index: 5;
}

.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 360px;
  /* 三层深度系统 */
  background: rgba(10, 10, 18, 0.65);
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 32px 28px;
  /* 外层投影 → 面板"浮起" */
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.5),
    0 4px 16px rgba(0, 0, 0, 0.3);
  /* 内光 → 面板"有体积" */
  position: relative;
}

.login-card::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 16px;
  pointer-events: none;
  /* 顶部内侧微光 — 模拟环境光打在面板顶部 */
  background: linear-gradient(
    135deg,
    rgba(120, 100, 255, 0.04) 0%,
    rgba(120, 100, 255, 0.01) 30%,
    transparent 60%
  );
}

.login-card-header {
  margin-bottom: 32px;
}

.card-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.logo-icon {
  font-size: 24px;
  color: #6366F1;
  text-shadow: 0 0 20px rgba(99, 102, 241, 0.3);
}

.logo-text {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.card-welcome {
  min-height: 20px;
}

/* Tab切换 */
.login-tabs {
  display: flex;
  gap: 0;
  margin-bottom: 28px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  padding: 3px;
  border: 1px solid rgba(255, 255, 255, 0.06);
}

.tab-btn {
  flex: 1;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: rgba(255, 255, 255, 0.4);
  font-size: 13px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.3s ease;
  position: relative;
  font-family: inherit;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.tab-btn.active {
  background: rgba(99, 102, 241, 0.15);
  color: #6366F1;
}

.tab-btn:hover:not(.active) {
  color: rgba(255, 255, 255, 0.7);
}

.tab-indicator {
  display: none;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.04);
  min-height: 36px;
}

.copyright {
  margin-top: auto;
  padding-top: 40px;
  min-height: 20px;
}

/* 角落装饰 */
.corner-decoration {
  position: fixed;
  font-size: 40px;
  font-weight: 100;
  color: rgba(99, 102, 241, 0.08);
  font-family: 'Courier New', monospace;
  z-index: 10;
  pointer-events: none;
}

.corner-decoration.top-left { top: 30px; left: 30px; }
.corner-decoration.top-right { top: 30px; right: 30px; }
.corner-decoration.bottom-left { bottom: 30px; left: 30px; }
.corner-decoration.bottom-right { bottom: 30px; right: 30px; }

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 表单切换过渡 */
.slide-fade-enter-active,
.slide-fade-leave-active {
  transition: all 0.25s ease;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(12px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

/* 响应式 */
@media (max-width: 860px) {
  .content-wrapper {
    flex-direction: column;
  }
  .brand-section {
    display: none;
  }
  .login-section {
    width: 100%;
    min-width: unset;
    border-left: none;
  }
}
</style>
