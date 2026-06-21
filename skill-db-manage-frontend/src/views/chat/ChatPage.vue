<template>
  <div class="chat-page">
    <!-- 顶部栏 -->
    <div class="chat-topbar">
      <span class="topbar-title">
        <Icon icon="ri:chat-3-line" width="18" height="18" />
        智能对话
      </span>
      <div class="topbar-right">
        <a-select
          v-model="selectedModel"
          class="model-select"
          size="small"
          placeholder="选择模型"
        >
          <a-option
            v-for="m in availableModels"
            :key="m.id"
            :value="m.id"
          >
            <span class="model-option">
              <span class="model-name">{{ m.modelName }}</span>
              <a-tag :color="providerColor(m.provider)" size="mini">{{ m.provider }}</a-tag>
            </span>
          </a-option>
        </a-select>
        <a-button type="outline" size="small" class="back-admin-btn" @click="router.push('/dashboard')">
          <Icon icon="ri:settings-3-line" width="14" height="14" />
          返回管理
        </a-button>
      </div>
    </div>

    <div class="chat-layout">
      <!-- 左侧：对话历史 -->
      <div class="chat-sidebar">
        <a-button class="new-chat-btn" long @click="newConversation">
          <Icon icon="ri:add-line" width="16" height="16" />
          新对话
        </a-button>

        <div class="conversation-list">
          <div
            v-for="conv in conversations"
            :key="conv.id"
            :class="['conv-item', { active: activeConv === conv.id }]"
            @click="selectConversation(conv.id)"
          >
            <div class="conv-icon">
              <Icon icon="ri:chat-1-line" width="16" height="16" />
            </div>
            <div class="conv-body">
              <div class="conv-title">{{ conv.title }}</div>
              <div class="conv-meta">
                <span class="conv-msg">{{ conv.lastMsg }}</span>
                <span class="conv-time">{{ conv.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：聊天区 -->
      <div class="chat-main">
        <!-- 消息列表 -->
        <div class="message-area" ref="msgAreaRef">
          <template v-if="currentMessages.length === 0">
            <div class="empty-chat">
              <Icon icon="ri:chat-smile-2-line" width="48" height="48" />
              <h3>开始新的对话</h3>
              <p>输入您的问题，Skill AI 将为您解答</p>
            </div>
          </template>

          <div
            v-for="(msg, idx) in currentMessages"
            :key="idx"
            :class="['message-row', msg.role]"
          >
            <div v-if="msg.role === 'assistant'" class="msg-avatar ai-avatar">
              <Icon icon="ri:robot-2-line" width="18" height="18" />
            </div>
            <div class="msg-bubble">
              <div class="msg-content">{{ msg.content }}</div>
              <!-- 文件附件 -->
              <div v-if="msg.files && msg.files.length" class="msg-files">
                <div v-for="f in msg.files" :key="f.name" class="file-tag">
                  <Icon icon="ri:file-3-line" width="12" height="12" />
                  <span>{{ f.name }}</span>
                  <span class="file-size">{{ f.size }}</span>
                </div>
              </div>
              <div class="msg-time">
                <span v-if="msg.model" class="msg-model">{{ msg.model }}</span>
                {{ msg.time }}
              </div>
            </div>
            <div v-if="msg.role === 'user'" class="msg-avatar user-avatar">
              {{ avatarChar }}
            </div>
          </div>
        </div>

        <!-- 底部输入区 -->
        <div class="input-area">
          <!-- 已选文件预览 -->
          <div v-if="pendingFiles.length" class="pending-files">
            <a-tag
              v-for="(f, i) in pendingFiles"
              :key="i"
              closable
              @close="pendingFiles.splice(i, 1)"
            >
              <Icon icon="ri:file-3-line" width="12" height="12" />
              {{ f.name }}
            </a-tag>
          </div>

          <div class="input-row">
            <a-upload
              :show-file-list="false"
              :auto-upload="false"
              @change="handleFileSelect"
            >
              <a-button type="text" class="upload-btn" title="上传文件">
                <Icon icon="ri:attachment-2" width="20" height="20" />
              </a-button>
            </a-upload>

            <a-textarea
              v-model="inputText"
              class="chat-input"
              placeholder="输入消息..."
              :auto-size="{ minRows: 1, maxRows: 4 }"
              @keydown.enter.exact.prevent="sendMessage"
            />

            <a-button
              type="primary"
              class="send-btn"
              :disabled="!inputText.trim() && !pendingFiles.length"
              @click="sendMessage"
            >
              <Icon icon="ri:send-plane-fill" width="16" height="16" />
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'


const router = useRouter()
const msgAreaRef = ref(null)
const activeConv = ref('c1')
const inputText = ref('')
const pendingFiles = ref([])

// ── 模型选择 ──
const STORAGE_KEY = 'skill_model_configs'
const availableModels = ref([])
const selectedModel = ref('')

function loadModels() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    availableModels.value = raw ? JSON.parse(raw) : []
  } catch {
    availableModels.value = []
  }
  if (availableModels.value.length && !selectedModel.value) {
    selectedModel.value = availableModels.value[0].id
  }
}

function getCurrentModel() {
  return availableModels.value.find(m => m.id === selectedModel.value)
}

function providerColor(p) {
  const map = { OpenAI: 'green', DeepSeek: 'blue', Anthropic: 'purple', Qwen: 'orange', ZhipuAI: 'cyan', Moonshot: 'magenta' }
  return map[p] || 'gray'
}

const avatarChar = 'A'

// ── Mock: 对话历史 ──
const conversations = ref([
  { id: 'c1', title: 'Skill 功能介绍', lastMsg: '我可以帮你分析...', time: '10:30' },
  { id: 'c2', title: '权限配置咨询', lastMsg: 'RBAC 权限模型...', time: '昨天' },
  { id: 'c3', title: '知识图谱使用', lastMsg: 'Graph RAG 提取...', time: '6/12' }
])

// ── Mock: 每个对话的消息 ──
const chatMessages = ref({
  c1: [
    { role: 'user', content: '你好，介绍一下 Skill 管理平台', time: '10:28' },
    { role: 'assistant', content: '您好！Skill Manager 是一个智能权限管理平台，支持 RBAC 权限控制、动态路由生成、多因素认证和审计日志追踪。您可以通过左侧菜单管理用户、角色和权限。有什么我可以帮您的吗？', time: '10:28' },
    { role: 'user', content: '能帮我分析一下权限配置的最佳实践吗？', time: '10:29', files: [{ name: 'rbac-config.json', size: '2.3KB' }] },
    { role: 'assistant', content: '好的，RBAC（基于角色的访问控制）配置的核心原则：\n\n1. **最小权限原则**：每个角色只分配完成任务所需的最小权限集\n2. **角色分层**：建立「超级管理员 → 部门管理员 → 普通用户」的层级\n3. **权限粒度**：建议区分到按钮/API 级别，而非仅页面级别\n4. **定期审计**：建议每月审查一次角色权限分配\n\n您上传的配置文件结构看起来合理，需要我详细解读吗？', time: '10:30' }
  ],
  c2: [
    { role: 'user', content: '什么是 RBAC 权限模型？', time: '昨天 14:20' },
    { role: 'assistant', content: 'RBAC（Role-Based Access Control）即基于角色的访问控制，核心概念：\n\n- **用户**：系统的使用者\n- **角色**：一组权限的集合（如管理员、编辑者、只读用户）\n- **权限**：对资源的操作许可（如 user:create、dashboard:view）\n\n用户 → 角色 → 权限，通过中间的角色层解耦用户和权限的直接绑定，便于管理和扩展。', time: '昨天 14:21' }
  ],
  c3: [
    { role: 'user', content: '知识图谱模块如何使用？', time: '6/12 09:15' },
    { role: 'assistant', content: '知识图谱模块当前处于开发阶段。上线后，它将通过 Graph RAG 技术从 Skill 描述中自动提取实体和关系，存储到 Neo4j 图数据库中，为每个 Skill 建立关联网络，支持可视化探索和智能推荐。敬请期待！', time: '6/12 09:16' }
  ]
})

const currentMessages = computed(() => {
  return chatMessages.value[activeConv.value] || []
})

function selectConversation(id) {
  activeConv.value = id
  scrollToBottom()
}

function newConversation() {
  const id = 'c' + Date.now()
  conversations.value.unshift({
    id,
    title: '新对话',
    lastMsg: '',
    time: '刚刚'
  })
  chatMessages.value[id] = []
  activeConv.value = id
  scrollToBottom()
}

function handleFileSelect(file) {
  pendingFiles.value.push({
    name: file.name,
    size: formatSize(file.size || 0)
  })
}

function formatSize(bytes) {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / (1024 * 1024)).toFixed(1) + 'MB'
}

function sendMessage() {
  const text = inputText.value.trim()
  if (!text && !pendingFiles.value.length) return

  const msg = {
    role: 'user',
    content: text || '[文件]',
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
    files: pendingFiles.value.length ? [...pendingFiles.value] : undefined
  }

  chatMessages.value[activeConv.value].push(msg)
  pendingFiles.value = []
  inputText.value = ''

  // 更新对话摘要
  const conv = conversations.value.find(c => c.id === activeConv.value)
  if (conv) {
    conv.lastMsg = text || '[文件]'
    conv.time = '刚刚'
  }

  // Mock AI 回复
  setTimeout(() => {
    const model = getCurrentModel()
    const aiMsg = {
      role: 'assistant',
      content: getAiReply(text),
      model: model?.modelName || '未知模型',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    }
    chatMessages.value[activeConv.value].push(aiMsg)
    scrollToBottom()
  }, 800)

  scrollToBottom()
}

function getAiReply(text) {
  if (text.includes('你好') || text.includes('介绍')) {
    return '您好！很高兴为您服务。我是 Skill Manager 的智能助手，可以帮您管理 Skill、配置权限、分析数据。请问有什么具体需求？'
  }
  if (text.includes('权限') || text.includes('RBAC')) {
    return '权限管理方面，建议您：\n\n1. 先在「权限字典」中定义好所有权限项\n2. 在「角色管理」中创建角色并分配权限\n3. 在「用户管理」中为用户分配角色\n\n这样形成完整的 RBAC 权限链路。需要我详细解释某个步骤吗？'
  }
  if (text.includes('Skill') || text.includes('skill')) {
    return '关于 Skill 管理：您可以通过 Skill 分组卡片查看各组 Skill 的分布情况。当前系统预置了 NLP 处理、数据分析、自动化工作流三个分组。点击左侧「Skill 管理」可以进入详细管理页面。'
  }
  return '收到您的消息。当前为 Mock 模式，后续接入真实 AI 服务后将提供更智能的回复。如果您有具体问题，可以尝试询问「权限」「Skill」或「介绍」相关话题。'
}

function scrollToBottom() {
  nextTick(() => {
    const el = msgAreaRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}

watch(activeConv, () => scrollToBottom())
onMounted(loadModels)
</script>

<style scoped>
.chat-page {
  position: relative;
  width: 100%;
  height: 100vh;
  background: #F7F8FA;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 顶部栏 - 深色 ── */
.chat-topbar {
  position: relative;
  z-index: 10;
  height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #222b45;
  border-bottom: none;
  flex-shrink: 0;
}
.topbar-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #FFFFFF;
  font-size: 16px;
  font-weight: 600;
}
.topbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.model-select {
  width: 200px;
}
.model-select :deep(.arco-select-view-single) {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.2);
  color: #fff;
}
.back-admin-btn {
  border-color: rgba(255,255,255,0.2) !important;
  color: rgba(255,255,255,0.8) !important;
  background: rgba(255,255,255,0.08) !important;
}
.back-admin-btn:hover {
  border-color: rgba(255,255,255,0.4) !important;
  color: #fff !important;
  background: rgba(255,255,255,0.15) !important;
}
.model-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.model-name {
  color: var(--text-primary);
  font-size: 13px;
}

/* ── 主体布局 ── */
.chat-layout {
  position: relative;
  z-index: 5;
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* ── 左侧对话历史 ── */
.chat-sidebar {
  width: 280px;
  min-width: 280px;
  background: #f6f8f9;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}
.new-chat-btn {
  margin: 12px;
  width: calc(100% - 24px);
  border-color: rgba(59, 130, 246, 0.3);
  color: var(--text-secondary);
  background: var(--primary-light-2);
}
.new-chat-btn:hover {
  border-color: var(--primary-color);
  color: var(--primary-color);
  background: var(--primary-light-1);
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}
.conv-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 2px;
}
.conv-item:hover {
  background: rgba(0, 0, 0, 0.04);
}
.conv-item.active {
  background: var(--primary-light-1);
}
.conv-icon {
  color: var(--text-tertiary);
  margin-top: 2px;
  flex-shrink: 0;
}
.conv-item.active .conv-icon {
  color: var(--primary-color);
}
.conv-body {
  flex: 1;
  min-width: 0;
}
.conv-title {
  color: var(--text-primary);
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 3px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.conv-item.active .conv-title {
  color: var(--text-primary);
  font-weight: 600;
}
.conv-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.conv-msg {
  color: var(--text-tertiary);
  font-size: 11px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 150px;
}
.conv-time {
  color: var(--text-disabled);
  font-size: 10px;
  flex-shrink: 0;
}

/* ── 右侧聊天区 ── */
.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.message-area {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
}

.empty-chat {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-tertiary);
}
.empty-chat h3 {
  margin: 16px 0 8px;
  color: var(--text-secondary);
  font-size: 18px;
  font-weight: 600;
}
.empty-chat p {
  color: var(--text-tertiary);
  font-size: 13px;
}

/* ── 消息气泡 ── */
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 80%;
}
.message-row.user {
  margin-left: auto;
  flex-direction: row-reverse;
}
.msg-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ai-avatar {
  background: var(--primary-light-1);
  color: var(--primary-color);
}
.user-avatar {
  background: var(--primary-color);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.msg-bubble {
  background: #FFFFFF;
  border: 1px solid #ebeef5;
  border-radius: 14px;
  padding: 12px 16px;
  border-top-left-radius: 4px;
}
.message-row.user .msg-bubble {
  background: var(--primary-light-1);
  border-color: rgba(59, 130, 246, 0.2);
  border-radius: 14px;
  border-top-right-radius: 4px;
}
.msg-content {
  color: var(--text-primary);
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
}
.msg-time {
  color: var(--text-disabled);
  font-size: 10px;
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.msg-model {
  color: var(--primary-color);
  font-size: 10px;
  background: var(--primary-light-1);
  padding: 1px 6px;
  border-radius: 4px;
}

.msg-files {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #ebeef5;
}
.file-tag {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f6f8f9;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 11px;
  color: var(--text-secondary);
}
.file-size {
  color: var(--text-disabled);
  margin-left: 2px;
}

/* ── 输入区 ── */
.input-area {
  padding: 12px 24px 16px;
  border-top: 1px solid #ebeef5;
  background: #FFFFFF;
  flex-shrink: 0;
}
.pending-files {
  display: flex;
  gap: 6px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.input-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}
.upload-btn {
  color: var(--text-tertiary);
  flex-shrink: 0;
}
.upload-btn:hover {
  color: var(--primary-color);
}
.chat-input {
  flex: 1;
}
.send-btn {
  flex-shrink: 0;
  border-radius: 12px;
  background: var(--primary-color);
  border-color: var(--primary-color);
}
.send-btn:hover {
  background: var(--primary-hover);
  border-color: var(--primary-hover);
}
.send-btn:disabled {
  background: rgba(59, 130, 246, 0.3);
  border-color: transparent;
}
</style>
