<template>
  <div class="admin-layout">
    <!-- 深色顶栏 Header - 全宽横跨 -->
    <div class="layout-header">
      <div class="header-left">
        <div class="logo-bar">
          <Icon icon="ri:terminal-window-line" width="24" height="24" class="logo-icon" />
          <span class="logo-text">Skill Manager</span>
        </div>
      </div>
      <div class="header-right">
        <a-button
          type="outline"
          size="small"
          class="chat-nav-btn"
          @click="router.push('/chat')"
        >
          <Icon icon="ri:chat-3-line" width="14" height="14" />
          对话模式
        </a-button>
        <a-dropdown trigger="click">
          <div class="user-bar">
            <a-avatar :size="30" class="user-avatar">{{ avatarText }}</a-avatar>
            <span class="user-name">{{ userStore.nickname }}</span>
            <Icon icon="ri:arrow-down-s-line" width="14" height="14" />
          </div>
          <template #content>
            <a-doption @click="router.push('/profile')">
              <Icon icon="ri:user-3-line" width="14" height="14" />
              个人信息
            </a-doption>
            <a-doption divided @click="handleLogout">
              <Icon icon="ri:logout-box-line" width="14" height="14" />
              退出登录
            </a-doption>
          </template>
        </a-dropdown>
      </div>
    </div>

    <!-- 下方区域：侧边栏 + 内容 -->
    <div class="layout-body">
      <!-- 白色侧边栏 -->
      <div :class="['layout-sider', { collapsed: collapsed }]">
        <div class="sider-scroll">
          <a-menu
            class="sider-menu"
            :collapsed="collapsed"
            :selected-keys="selectedKeys"
            @menu-item-click="handleMenuClick"
            auto-open-selected-tree
          >
            <template v-for="item in menuTree" :key="item.meta?.uuid || item.name">
              <a-sub-menu
                v-if="item.children && item.children.length > 0"
                :key="item.meta?.uuid || item.name"
              >
                <template #title>
                  <span class="menu-icon">
                    <Icon :icon="iconMap[item.meta?.icon] || 'ri:folder-line'" width="16" height="16" />
                  </span>
                  <span v-show="!collapsed">{{ item.meta?.title || item.name }}</span>
                </template>
                <a-menu-item v-for="child in item.children" :key="child.meta?.uuid || child.name">
                  <template #icon>
                    <Icon :icon="iconMap[child.meta?.icon] || 'ri:file-line'" width="16" height="16" />
                  </template>
                  {{ child.meta?.title || child.name }}
                </a-menu-item>
              </a-sub-menu>

              <a-menu-item v-else :key="item.meta?.uuid || item.name">
                <template #icon>
                  <Icon :icon="iconMap[item.meta?.icon] || 'ri:file-line'" width="16" height="16" />
                </template>
                <span v-show="!collapsed">{{ item.meta?.title || item.name }}</span>
              </a-menu-item>
            </template>
          </a-menu>
        </div>
        <div class="sider-bottom" @click="collapsed = !collapsed">
          <Icon :icon="collapsed ? 'ri:menu-unfold-line' : 'ri:menu-fold-line'" width="16" height="16" />
        </div>
      </div>

      <!-- 右侧内容区 -->
      <div class="layout-main">
        <!-- Topbar 面包屑栏 -->
        <div class="layout-topbar">
          <div class="topbar-left">
            <a-breadcrumb>
              <a-breadcrumb-item>
                <Icon icon="ri:home-4-line" width="14" height="14" />
              </a-breadcrumb-item>
              <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
            </a-breadcrumb>
          </div>
        </div>

        <!-- Content -->
        <div class="layout-content">
          <router-view />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Modal } from '@arco-design/web-vue'
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'
import { Icon } from '@iconify/vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

const collapsed = ref(false)

// Map Arco icon names → Iconify ri: icons
const iconMap = {
  IconDashboard: 'ri:dashboard-line',
  IconApps: 'ri:apps-line',
  IconSafe: 'ri:shield-check-line',
  IconBrain: 'ri:brain-line',
  IconUser: 'ri:user-3-line',
  IconUserGroup: 'ri:group-line',
  IconLock: 'ri:lock-line',
  IconSettings: 'ri:settings-3-line',
  IconList: 'ri:list-check-2',
  IconCode: 'ri:code-box-line',
  IconFolder: 'ri:folder-line',
  IconFile: 'ri:file-line',
  IconMenu: 'ri:menu-line'
}

const selectedKeys = computed(() => {
  return route.meta?.uuid ? [route.meta.uuid] : []
})

const currentTitle = computed(() => {
  return route.meta?.title || ''
})

const avatarText = computed(() => {
  return userStore.nickname?.charAt(0)?.toUpperCase() || 'U'
})

// 从 permissionStore 生成菜单树
const menuTree = computed(() => {
  return permissionStore.generateRoutes()
})

function handleMenuClick(key) {
  const allRoutes = router.getRoutes()
  for (const r of allRoutes) {
    if (r.meta?.uuid === key && r.path) {
      router.push(r.path)
      return
    }
  }
}

function handleLogout() {
  Modal.confirm({
    title: '确认退出',
    content: '确认退出当前账号吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      await userStore.logout()
      router.push('/login')
    }
  })
}
</script>

<style scoped>
/* ── 根布局 ── */
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 深色顶栏 Header ── */
.layout-header {
  height: var(--header-height);
  background: var(--header-bg);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  flex-shrink: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
}

.logo-bar {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  color: var(--primary-color);
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 对话模式按钮 - 深色Header中的样式 */
.chat-nav-btn {
  border-color: rgba(255, 255, 255, 0.2) !important;
  color: rgba(255, 255, 255, 0.8) !important;
  background: rgba(255, 255, 255, 0.08) !important;
}
.chat-nav-btn:hover {
  border-color: rgba(255, 255, 255, 0.4) !important;
  color: #fff !important;
  background: rgba(255, 255, 255, 0.15) !important;
}

/* 用户栏 */
.user-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 10px;
  border-radius: 4px;
  transition: background 0.2s;
}
.user-bar:hover {
  background: rgba(255, 255, 255, 0.1);
}

.user-avatar {
  background-color: var(--primary-color);
}

.user-name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
}

/* ── 下方主体区域 ── */
.layout-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ── 白色侧边栏 ── */
.layout-sider {
  width: var(--sider-width);
  background: var(--sider-bg);
  box-shadow: var(--sider-shadow);
  border-right: 1px solid var(--sider-border);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.3s;
  z-index: 50;
  overflow: hidden;
}

.layout-sider.collapsed {
  width: var(--sider-collapsed-width);
}

.sider-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
}

.sider-bottom {
  height: 51px;
  border-top: 1px solid var(--sider-border);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--sider-text);
  transition: color 0.2s;
  flex-shrink: 0;
}
.sider-bottom:hover {
  color: var(--primary-color);
}

/* 侧边栏 Menu */
.sider-menu {
  background: transparent !important;
  border-right: none !important;
}

:deep(.arco-menu-item) {
  color: var(--sider-text);
}
:deep(.arco-menu-item:hover) {
  background: var(--sider-item-hover) !important;
  color: var(--sider-text-active);
}
:deep(.arco-menu-item.arco-menu-selected) {
  background: var(--sider-item-selected) !important;
  color: var(--primary-color);
  font-weight: 500;
}
:deep(.arco-menu-inline-header) {
  color: var(--sider-text);
}
:deep(.arco-menu-inline-header:hover) {
  background: var(--sider-item-hover) !important;
  color: var(--sider-text-active);
}

.menu-icon {
  margin-right: 8px;
  display: inline-flex;
  align-items: center;
}

/* ── 右侧主体 ── */
.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* ── Topbar 面包屑栏 ── */
.layout-topbar {
  height: var(--topbar-height);
  background: var(--topbar-bg);
  border-bottom: 1px solid var(--topbar-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  flex-shrink: 0;
}

.topbar-left {
  display: flex;
  align-items: center;
}

/* ── Content 内容区 ── */
.layout-content {
  flex: 1;
  background: var(--content-bg);
  padding: var(--spacing-page);
  overflow: auto;
}
</style>
