<template>
  <div class="sc-page-container">
    <div class="sc-page-header">
      <div class="toolbar-left">
        <span style="font-size: 14px; font-weight: 500; color: var(--text-primary);">Skill 管理</span>
      </div>
    </div>
    <div class="placeholder-content">
      <Icon icon="ri:code-box-line" width="48" height="48" />
      <h2>Skill 管理</h2>
      <p>Skill 列表、分组、版本管理功能开发中</p>
      <a-tag color="arcoblue" size="small">即将上线</a-tag>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'
</script>

<style scoped>
.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  color: var(--text-secondary);
}
.placeholder-content :deep(svg) {
  color: var(--primary-color);
  opacity: 0.5;
  margin-bottom: 16px;
}
.placeholder-content h2 {
  color: var(--text-primary);
  font-size: 18px;
  margin: 0 0 8px;
  font-weight: 600;
}
.placeholder-content p {
  color: var(--text-tertiary);
  font-size: 13px;
  margin: 0 0 16px;
}
</style>
