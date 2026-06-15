<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="text">页面不存在</div>
    <a-button type="primary" @click="$router.push('/')">返回首页</a-button>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: var(--color-bg-1);
}

.code {
  font-size: 120px;
  font-weight: 700;
  color: var(--color-text-4);
  line-height: 1;
  margin-bottom: 16px;
}

.text {
  font-size: 20px;
  color: var(--color-text-3);
  margin-bottom: 32px;
}
</style>
