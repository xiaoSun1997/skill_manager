<template>
  <div class="dashboard">
    <!-- 区块1：人员信息卡片（横向布局） -->
    <div class="sc-page-container" style="margin-bottom: 15px;">
      <div style="padding: 20px; display: flex; align-items: center; gap: 20px;">
        <a-avatar :size="64" :style="{ backgroundColor: 'var(--primary-color)', fontSize: '24px' }">
          {{ avatarText }}
        </a-avatar>
        <div style="flex: 1;">
          <h2 style="color: var(--text-primary); font-size: 18px; font-weight: 600; margin: 0 0 6px;">
            {{ userStore.nickname || '管理员' }}
          </h2>
          <a-tag color="arcoblue" size="small">系统管理员</a-tag>
          <div style="display: flex; gap: 20px; margin-top: 10px;">
            <span class="meta-item">
              <Icon icon="ri:mail-line" width="13" height="13" />
              admin@skill.com
            </span>
            <span class="meta-item">
              <Icon icon="ri:time-line" width="13" height="13" />
              最后登录 2026-06-14
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 区块2：Skill Group 卡片 -->
    <div class="section-title">
      <Icon icon="ri:apps-line" width="16" height="16" />
      <span>Skill 分组</span>
    </div>
    <a-row :gutter="[15, 15]">
      <a-col :span="8" v-for="group in skillGroups" :key="group.name">
        <div class="sc-page-container skill-card">
          <div style="padding: 15px;">
            <div class="group-header">
              <div class="group-icon" :style="{ background: group.color }">
                <Icon :icon="group.icon" width="20" height="20" />
              </div>
              <div class="group-info">
                <div class="group-name">{{ group.name }}</div>
                <div class="group-count">{{ group.count }} Skills</div>
              </div>
            </div>
            <div style="margin: 10px 0 8px;">
              <a-tag :color="group.statusColor" size="small">{{ group.status }}</a-tag>
            </div>
            <div class="group-desc">{{ group.desc }}</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- 区块3：知识图谱（占位） -->
    <div class="section-title" style="margin-top: 15px;">
      <Icon icon="ri:git-branch-line" width="16" height="16" />
      <span>知识图谱</span>
    </div>
    <div class="sc-page-container">
      <div class="graph-placeholder">
        <div class="graph-icon">
          <Icon icon="ri:node-tree" width="48" height="48" />
        </div>
        <h3>知识图谱 · 即将上线</h3>
        <p>通过 Graph RAG 提取 Neo4j 图数据，可视化 Skill 之间的关联关系</p>
        <a-tag color="arcoblue" size="small">开发中</a-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { Icon } from '@iconify/vue'

const userStore = useUserStore()

const avatarText = userStore.nickname?.charAt(0)?.toUpperCase() || 'A'

const skillGroups = reactive([
  {
    name: 'NLP 处理引擎',
    count: 12,
    icon: 'ri:brain-line',
    color: 'rgba(59,130,246,0.10)',
    status: '活跃',
    statusColor: 'green',
    desc: '自然语言处理相关 Skill 集合，包含分词、NER、情感分析等能力'
  },
  {
    name: '数据分析工具',
    count: 8,
    icon: 'ri:bar-chart-grouped-line',
    color: 'rgba(0,180,255,0.15)',
    status: '维护中',
    statusColor: 'blue',
    desc: '数据清洗、可视化、统计分析类 Skill 工具集'
  },
  {
    name: '自动化工作流',
    count: 5,
    icon: 'ri:git-branch-line',
    color: 'rgba(0,206,137,0.15)',
    status: '新建',
    statusColor: 'orange',
    desc: 'CI/CD 集成、定时任务、触发式流程编排 Skill'
  }
])
</script>

<style scoped>
.dashboard {
  padding: 0;
}

/* ── Section title ── */
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

/* ── 元信息 ── */
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: var(--text-tertiary);
  font-size: 12px;
}

/* ── Skill Group 卡片 ── */
.skill-card {
  border: 1px solid var(--card-border);
}
.group-header {
  display: flex;
  align-items: center;
  gap: 12px;
}
.group-icon {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}
.group-name {
  color: var(--text-primary);
  font-size: 14px;
  font-weight: 600;
}
.group-count {
  color: var(--text-tertiary);
  font-size: 12px;
}
.group-desc {
  color: var(--text-tertiary);
  font-size: 12px;
  line-height: 1.5;
}

/* ── 知识图谱占位 ── */
.graph-placeholder {
  text-align: center;
  padding: 60px 20px;
}
.graph-icon {
  color: var(--primary-light-1);
  margin-bottom: 12px;
}
.graph-placeholder h3 {
  color: var(--text-secondary);
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
}
.graph-placeholder p {
  color: var(--text-tertiary);
  font-size: 13px;
  margin: 0 0 12px;
  max-width: 360px;
  margin-left: auto;
  margin-right: auto;
}
</style>
