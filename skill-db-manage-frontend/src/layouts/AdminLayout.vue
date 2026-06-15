<template>
  <div class="admin-layout dark-theme">
    <a-layout style="height: 100vh">
      <!-- 侧边栏 -->
      <a-layout-sider
        :collapsed="collapsed"
        :width="240"
        :collapsed-width="64"
        theme="dark"
        :style="{
          background: '#141414',
          borderRight: '1px solid rgba(255,255,255,0.06)'
        }"
      >
        <div class="logo-container">
          <span class="logo-icon">
            <Icon icon="ri:terminal-window-line" width="22" height="22" />
          </span>
          <span v-show="!collapsed" class="logo-text">Skill Manager</span>
        </div>

        <a-menu
          :style="{ background: 'transparent', borderRight: 'none' }"
          :collapsed="collapsed"
          :selected-keys="selectedKeys"
          :open-keys="openKeys"
          show-collapse-button
          @menu-item-click="handleMenuClick"
          @collapse="collapsed = !collapsed"
          theme="dark"
          auto-open-selected-tree
        >
          <template v-for="item in menuTree" :key="item.meta?.uuid || item.name">
            <!-- 有子菜单的目录/菜单 -->
            <a-sub-menu
              v-if="item.children && item.children.length > 0"
              :key="item.meta?.uuid || item.name"
            >
              <template #title>
                <span v-if="item.meta?.icon" :class="item.meta.icon" style="font-size: 16px; margin-right: 10px;">
                  <component :is="item.meta.icon" />
                </span>
                <span v-else>
                  <icon-folder />
                </span>
                <span>{{ item.meta?.title || item.name }}</span>
              </template>
              <MenuItem v-for="child in item.children" :key="child.meta?.uuid || child.name" :menu="child" />
            </a-sub-menu>

            <!-- 叶子菜单 -->
            <a-menu-item v-else :key="item.meta?.uuid || item.name">
              <template #icon>
                <span v-if="item.meta?.icon" style="font-size: 16px;">
                  <component :is="item.meta.icon" />
                </span>
                <icon-list v-else />
              </template>
              {{ item.meta?.title || item.name }}
            </a-menu-item>
          </template>
        </a-menu>
      </a-layout-sider>

      <!-- 右侧内容 -->
      <a-layout>
        <!-- 顶部导航 -->
        <a-layout-header
          class="layout-header"
          :style="{
            background: '#141414',
            borderBottom: '1px solid rgba(255,255,255,0.06)',
            height: '56px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            padding: '0 24px'
          }"
        >
          <div class="header-left">
            <a-breadcrumb>
              <a-breadcrumb-item>
                <icon-home />
              </a-breadcrumb-item>
              <a-breadcrumb-item>{{ currentTitle }}</a-breadcrumb-item>
            </a-breadcrumb>
          </div>

          <div class="header-right">
            <a-dropdown trigger="click">
              <a-avatar
                :style="{
                  backgroundColor: '#6366F1',
                  cursor: 'pointer',
                  verticalAlign: 'middle'
                }"
              >
                {{ avatarText }}
              </a-avatar>
              <template #content>
                <a-doption>
                  <icon-user />
                  <span>个人信息</span>
                </a-doption>
                <a-doption>
                  <icon-settings />
                  <span>系统设置</span>
                </a-doption>
                <a-doption divided @click="handleLogout">
                  <icon-export />
                  <span>退出登录</span>
                </a-doption>
              </template>
            </a-dropdown>
            <span class="username">{{ userStore.nickname }}</span>
          </div>
        </a-layout-header>

        <!-- 内容区域 -->
        <a-layout-content
          class="layout-content"
          :style="{
            background: '#0f0f0f',
            padding: '20px 24px',
            overflow: 'auto'
          }"
        >
          <router-view />
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
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
  // 找到对应的路由并跳转
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
.admin-layout {
  height: 100vh;
}

.admin-layout.dark-theme {
  background: #0f0f0f;
}

.logo-container {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}

.logo-icon {
  font-size: 22px;
  color: #6366F1;
  display: flex;
  align-items: center;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
  white-space: nowrap;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.username {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.layout-content {
  min-height: calc(100vh - 56px);
}

:deep(.arco-layout-sider-trigger) {
  background: #1a1a1a !important;
  color: rgba(255, 255, 255, 0.5) !important;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
}

:deep(.arco-menu-dark) {
  background: transparent !important;
}

:deep(.arco-menu-dark .arco-menu-item) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.arco-menu-dark .arco-menu-item:hover) {
  background: rgba(255, 255, 255, 0.06) !important;
  color: #fff;
}

:deep(.arco-menu-dark .arco-menu-item.arco-menu-selected) {
  background: rgba(99, 102, 241, 0.15) !important;
  color: #6366F1;
}

:deep(.arco-menu-dark .arco-menu-inline-header) {
  color: rgba(255, 255, 255, 0.65);
}

:deep(.arco-menu-dark .arco-menu-inline-header:hover) {
  background: rgba(255, 255, 255, 0.04) !important;
  color: #fff;
}

:deep(.arco-breadcrumb) {
  color: rgba(255, 255, 255, 0.5);
}

:deep(.arco-breadcrumb .arco-breadcrumb-item) {
  color: rgba(255, 255, 255, 0.5);
}

:deep(.arco-breadcrumb .arco-breadcrumb-item:last-child) {
  color: rgba(255, 255, 255, 0.85);
}

:deep(.arco-dropdown-menu) {
  background: #1a1a1a;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

:deep(.arco-dropdown-menu .arco-dropdown-option) {
  color: rgba(255, 255, 255, 0.75);
}

:deep(.arco-dropdown-menu .arco-dropdown-option:hover) {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}
</style>
