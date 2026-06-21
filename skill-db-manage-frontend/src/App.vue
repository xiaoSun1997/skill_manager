<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/store/user'
import { usePermissionStore } from '@/store/permission'

const userStore = useUserStore()
const permissionStore = usePermissionStore()
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}
html, body, #app {
  width: 100%;
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f6f8f9;
}

/* ====== SCUI-Style Design Tokens ====== */
:root {
  /* 主色 */
  --primary-color: #3B82F6;
  --primary-hover: #2563EB;
  --primary-active: #1D4ED8;
  --primary-light-1: rgba(59, 130, 246, 0.10);
  --primary-light-2: rgba(59, 130, 246, 0.06);

  /* Header - 深色顶栏 */
  --header-bg: #222b45;
  --header-text: #FFFFFF;
  --header-height: 58px;

  /* Sidebar - 白色侧边栏 */
  --sider-bg: #FFFFFF;
  --sider-text: #4a5568;
  --sider-text-active: #222b45;
  --sider-item-hover: #f6f8f9;
  --sider-item-selected: rgba(59, 130, 246, 0.08);
  --sider-border: #ebeef5;
  --sider-width: 210px;
  --sider-collapsed-width: 65px;
  --sider-shadow: 2px 0 8px 0 rgba(29, 35, 41, .05);

  /* Topbar - 面包屑栏 */
  --topbar-bg: #FFFFFF;
  --topbar-border: #ebeef5;
  --topbar-height: 50px;

  /* 内容区 */
  --content-bg: #f6f8f9;
  --card-bg: #FFFFFF;
  --card-border: #ebeef5;

  /* 文字 */
  --text-primary: #1F2937;
  --text-secondary: #6B7280;
  --text-tertiary: #9CA3AF;
  --text-disabled: #D1D5DB;

  /* 间距 & 圆角 */
  --spacing-page: 15px;
  --spacing-card: 15px;
  --border-radius-card: 4px;
  --border-radius-sm: 3px;
}

/* Arco Design 主色覆盖 */
body {
  --primary-6: 59, 130, 246;
}

/* ====== SCUI-Style 全局滚动条 ====== */
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}
::-webkit-scrollbar-thumb {
  background-color: rgba(50, 50, 50, 0.3);
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background-color: rgba(50, 50, 50, 0.6);
}
::-webkit-scrollbar-track {
  background-color: transparent;
}

/* ====== SCUI-Style 通用页面结构类 ====== */
.sc-page-container {
  background: #fff;
  border-radius: var(--border-radius-card);
  overflow: hidden;
}

.sc-page-header {
  padding: 15px;
  background: #fff;
  border-bottom: 1px solid var(--topbar-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.sc-page-header .toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sc-page-header .toolbar-right {
  display: flex;
  align-items: center;
}

.sc-page-main {
  padding: 0;
}

/* Arco table inside sc-page-main - borderless */
.sc-page-main .arco-table {
  border: none;
}
</style>
