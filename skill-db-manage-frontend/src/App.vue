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
}

/* ====== Arco Design 暗色主题 CSS 变量覆盖 ====== */
.dark-theme {
  /* 背景色 */
  --color-bg-1: #141414;
  --color-bg-2: #1f1f1f;
  --color-bg-3: #292929;
  --color-bg-4: #333333;
  --color-bg-5: #404040;
  --color-bg-white: #141414;

  /* 文字色 */
  --color-text-1: rgba(255, 255, 255, 0.9);
  --color-text-2: rgba(255, 255, 255, 0.7);
  --color-text-3: rgba(255, 255, 255, 0.5);
  --color-text-4: rgba(255, 255, 255, 0.35);

  /* 边框 */
  --color-border-1: rgba(255, 255, 255, 0.08);
  --color-border-2: rgba(255, 255, 255, 0.06);
  --color-border-3: rgba(255, 255, 255, 0.04);
  --color-border: rgba(255, 255, 255, 0.08);

  /* 填充 */
  --color-fill-1: rgba(255, 255, 255, 0.04);
  --color-fill-2: rgba(255, 255, 255, 0.06);
  --color-fill-3: rgba(255, 255, 255, 0.08);
  --color-fill-4: rgba(255, 255, 255, 0.12);
}

/* Arco Card 暗色覆盖 */
.dark-theme .arco-card {
  background: #1a1a1a;
  border-color: rgba(255, 255, 255, 0.08);
}

.dark-theme .arco-card-header {
  border-bottom-color: rgba(255, 255, 255, 0.06);
}

/* Arco Table 暗色覆盖 */
.dark-theme .arco-table {
  background: transparent;
}

.dark-theme .arco-table-tr,
.dark-theme .arco-table td {
  background: #1a1a1a !important;
  border-bottom-color: rgba(255, 255, 255, 0.06);
  color: rgba(255, 255, 255, 0.85);
}

.dark-theme .arco-table-th {
  background: #1f1f1f !important;
  border-bottom-color: rgba(255, 255, 255, 0.08);
}

.dark-theme .arco-table-th-title {
  color: rgba(255, 255, 255, 0.65);
}

.dark-theme .arco-table-tr:hover td {
  background: rgba(255, 255, 255, 0.04) !important;
}

.dark-theme .arco-table-stripe .arco-table-tr:nth-child(even) td {
  background: rgba(255, 255, 255, 0.02) !important;
}

/* Arco Tree 暗色覆盖 */
.dark-theme .arco-tree-node {
  color: rgba(255, 255, 255, 0.85);
}

.dark-theme .arco-tree-node-title:hover {
  background: rgba(255, 255, 255, 0.04);
}

.dark-theme .arco-tree-node-selected .arco-tree-node-title {
  background: rgba(99, 102, 241, 0.15) !important;
  color: #6366F1;
}

/* Arco Input / Select / Textarea 暗色覆盖 */
.dark-theme .arco-input-wrapper,
.dark-theme .arco-select-view,
.dark-theme .arco-textarea-wrapper {
  background: #1f1f1f;
  border-color: rgba(255, 255, 255, 0.08);
}

.dark-theme .arco-input,
.dark-theme .arco-select-view-value,
.dark-theme .arco-textarea {
  color: rgba(255, 255, 255, 0.85);
}

.dark-theme .arco-input::placeholder,
.dark-theme .arco-textarea::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

/* Arco Form 标签暗色 */
.dark-theme .arco-form-item-label {
  color: rgba(255, 255, 255, 0.7);
}

/* Arco Modal 暗色覆盖 */
.dark-theme .arco-modal {
  background: #1a1a1a;
}

.dark-theme .arco-modal-header {
  border-bottom-color: rgba(255, 255, 255, 0.06);
}

.dark-theme .arco-modal-title {
  color: rgba(255, 255, 255, 0.9);
}

/* Arco Spin 暗色遮罩 */
.dark-theme .arco-spin-mask {
  background: rgba(20, 20, 20, 0.6);
}

/* Arco Tag 暗色 */
.dark-theme .arco-tag {
  border-color: transparent;
}

/* Arco Empty 暗色 */
.dark-theme .arco-empty .arco-empty-description {
  color: rgba(255, 255, 255, 0.4);
}

/* Arco Pagination 暗色 */
.dark-theme .arco-pagination-item {
  background: #1f1f1f;
  color: rgba(255, 255, 255, 0.65);
  border-color: rgba(255, 255, 255, 0.08);
}

.dark-theme .arco-pagination-item-active {
  background: #6366F1;
  color: #fff;
  border-color: #6366F1;
}

/* Arco Switch 暗色（已禁用状态） */
.dark-theme .arco-switch[aria-checked="false"] {
  background: rgba(255, 255, 255, 0.12);
}

/* Arco Breadcrumb 暗色 */
.dark-theme .arco-breadcrumb-item {
  color: rgba(255, 255, 255, 0.45);
}

.dark-theme .arco-breadcrumb-item:last-child {
  color: rgba(255, 255, 255, 0.85);
}

/* Arco Avatar 暗色 */
.dark-theme .arco-avatar {
  background: #6366F1;
  color: #fff;
}

/* Arco Button text 类型暗色 */
.dark-theme .arco-btn-text {
  color: rgba(255, 255, 255, 0.65);
}

.dark-theme .arco-btn-text:hover {
  background: rgba(255, 255, 255, 0.06);
  color: #fff;
}

/* Arco Divider 暗色 */
.dark-theme .arco-divider {
  border-color: rgba(255, 255, 255, 0.06);
}

/* Arco Alert 暗色 */
.dark-theme .arco-alert-info {
  background: rgba(99, 102, 241, 0.1);
  border-color: rgba(99, 102, 241, 0.2);
}

/* Arco Checkbox 暗色 */
.dark-theme .arco-checkbox-label {
  color: rgba(255, 255, 255, 0.85);
}

/* Arco TreeSelect 弹窗暗色 */
.dark-theme .arco-tree-select-popup .arco-tree-node {
  color: rgba(255, 255, 255, 0.85);
}
</style>
