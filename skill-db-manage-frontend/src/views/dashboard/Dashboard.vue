<template>
  <div class="dashboard">
    <a-row :gutter="[16, 16]">
      <a-col :span="6">
        <a-card class="stat-card" hoverable>
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.users }}</div>
            </div>
            <div class="stat-icon" style="background: rgba(99,102,241,0.1); color: #6366F1;">
              <icon-user />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" hoverable>
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">角色数量</div>
              <div class="stat-value">{{ stats.roles }}</div>
            </div>
            <div class="stat-icon" style="background: rgba(0,180,255,0.1); color: #00B4FF;">
              <icon-team />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" hoverable>
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">权限项</div>
              <div class="stat-value">{{ stats.permissions }}</div>
            </div>
            <div class="stat-icon" style="background: rgba(0,206,137,0.1); color: #00CE89;">
              <icon-safe />
            </div>
          </div>
        </a-card>
      </a-col>
      <a-col :span="6">
        <a-card class="stat-card" hoverable>
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-label">今日登录</div>
              <div class="stat-value">{{ stats.logins }}</div>
            </div>
            <div class="stat-icon" style="background: rgba(255,200,0,0.1); color: #FFC800;">
              <icon-clock-circle />
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :span="16">
        <a-card title="快速开始" :header-style="{ borderBottom: '1px solid var(--color-border)' }">
          <a-space direction="vertical" fill>
            <a-alert type="info" show-icon>
              <template #title>
                欢迎使用 Skill 管理系统！请通过左侧菜单导航开始管理。
              </template>
            </a-alert>
            <a-row :gutter="[12, 12]" style="margin-top: 12px;">
              <a-col :span="8" v-for="item in quickActions" :key="item.title">
                <a-card
                  class="action-card"
                  hoverable
                  @click="item.action"
                >
                  <div class="action-content">
                    <div class="action-icon" :style="{ color: item.color }">
                      <component :is="item.icon" />
                    </div>
                    <div class="action-label">{{ item.title }}</div>
                    <div class="action-desc">{{ item.desc }}</div>
                  </div>
                </a-card>
              </a-col>
            </a-row>
          </a-space>
        </a-card>
      </a-col>
      <a-col :span="8">
        <a-card title="系统信息" :header-style="{ borderBottom: '1px solid var(--color-border)' }">
          <a-descriptions :data="systemInfo" layout="horizontal" :column="1" size="small" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const stats = reactive({
  users: '--',
  roles: '--',
  permissions: '--',
  logins: '--'
})

const systemInfo = reactive([
  { label: '系统版本', value: 'v1.0.0' },
  { label: '前端框架', value: 'Vue 3 + Arco Design' },
  { label: '后端框架', value: 'Spring Boot 3.x' },
  { label: '数据库', value: 'MySQL 8.x + Redis' },
  { label: '认证方式', value: 'JWT' }
])

const quickActions = [
  {
    title: '用户管理',
    desc: '管理系统用户账号',
    icon: 'icon-user',
    color: '#6366F1',
    action: () => router.push('/system/user')
  },
  {
    title: '角色管理',
    desc: '配置角色与权限',
    icon: 'icon-team',
    color: '#00B4FF',
    action: () => router.push('/system/role')
  },
  {
    title: '权限管理',
    desc: '维护权限字典',
    icon: 'icon-safe',
    color: '#00CE89',
    action: () => router.push('/system/permission')
  }
]
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
}

.stat-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 13px;
  color: var(--color-text-3);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-1);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.action-card {
  border-radius: 10px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.action-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.35);
}

.action-content {
  text-align: center;
  padding: 12px 0;
}

.action-icon {
  font-size: 32px;
  margin-bottom: 12px;
}

.action-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-1);
  margin-bottom: 4px;
}

.action-desc {
  font-size: 12px;
  color: var(--color-text-3);
}
</style>
