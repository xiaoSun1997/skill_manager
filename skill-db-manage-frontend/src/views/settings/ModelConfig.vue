<template>
  <div class="model-config">
    <div class="sc-page-container">
      <!-- 工具栏 -->
      <div class="sc-page-header">
        <div class="toolbar-left">
          <span style="font-size: 14px; font-weight: 500; color: var(--text-primary);">智能模型配置</span>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="openAdd">
            <template #icon><Icon icon="ri:add-line" width="14" height="14" /></template>
            新增模型
          </a-button>
        </div>
      </div>

      <!-- 表格/内容 -->
      <div class="sc-page-main">
        <a-table
          v-if="models.length"
          :columns="columns"
          :data="models"
          :pagination="false"
          :bordered="{ cell: false }"
          :stripe="true"
          row-key="id"
        >
          <template #provider="{ record }">
            <a-tag :color="providerColor(record.provider)" size="small">{{ record.provider }}</a-tag>
          </template>
          <template #apiKey="{ record }">
            <span class="masked-key">{{ maskKey(record.apiKey) }}</span>
          </template>
          <template #temperature="{ record }">
            <span v-if="record.temperature !== undefined && record.temperature !== null">{{ record.temperature }}</span>
            <span v-else style="color: var(--text-tertiary)">默认</span>
          </template>
          <template #action="{ record }">
            <a-button-group>
              <a-button type="text" size="mini" @click="openEdit(record)">
                <Icon icon="ri:edit-line" width="14" height="14" />
              </a-button>
              <a-popconfirm content="确认删除该模型配置？" @ok="removeModel(record.id)">
                <a-button type="text" size="mini" status="danger">
                  <Icon icon="ri:delete-bin-line" width="14" height="14" />
                </a-button>
              </a-popconfirm>
            </a-button-group>
          </template>
        </a-table>

        <div v-else class="empty-state">
          <Icon icon="ri:robot-2-line" width="48" height="48" />
          <p>暂无模型配置，点击「新增模型」开始</p>
        </div>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:visible="modalVisible"
      :title="editingId ? '编辑模型' : '新增模型'"
      :width="480"
      @ok="saveModel"
      @cancel="modalVisible = false"
    >
      <a-form :model="form" layout="vertical" size="medium">
        <a-form-item label="模型名称" required>
          <a-input v-model="form.modelName" placeholder="例如 gpt-4、deepseek-chat" />
        </a-form-item>
        <a-form-item label="Provider" required>
          <a-select v-model="form.provider" placeholder="选择提供商" allow-create>
            <a-option>OpenAI</a-option>
            <a-option>DeepSeek</a-option>
            <a-option>Anthropic</a-option>
            <a-option>Qwen</a-option>
            <a-option>ZhipuAI</a-option>
            <a-option>Moonshot</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="API Key" required>
          <a-input-password v-model="form.apiKey" placeholder="sk-..." />
        </a-form-item>
        <a-form-item label="Base URL" required>
          <a-input v-model="form.baseUrl" placeholder="https://api.openai.com/v1" />
        </a-form-item>
        <a-form-item label="Temperature">
          <a-input-number
            v-model="form.temperature"
            :min="0"
            :max="2"
            :step="0.1"
            placeholder="0.7"
            style="width: 100%"
          />
          <div class="form-hint">控制输出随机性，0=确定，2=最大随机，留空使用默认</div>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { Icon } from '@iconify/vue'

const STORAGE_KEY = 'skill_model_configs'

const models = ref([])
const modalVisible = ref(false)
const editingId = ref(null)

const form = reactive({
  modelName: '',
  provider: '',
  apiKey: '',
  baseUrl: '',
  temperature: null
})

const columns = [
  { title: '模型名称', dataIndex: 'modelName', width: 150 },
  { title: 'Provider', slotName: 'provider', width: 100 },
  { title: 'API Key', slotName: 'apiKey', width: 160 },
  { title: 'Base URL', dataIndex: 'baseUrl', ellipsis: true },
  { title: 'Temperature', slotName: 'temperature', width: 100 },
  { title: '操作', slotName: 'action', width: 100, align: 'center' }
]

function providerColor(p) {
  const map = { OpenAI: 'green', DeepSeek: 'blue', Anthropic: 'purple', Qwen: 'orange', ZhipuAI: 'cyan', Moonshot: 'magenta' }
  return map[p] || 'gray'
}

function maskKey(key) {
  if (!key) return ''
  if (key.length <= 8) return '****'
  return key.slice(0, 4) + '****' + key.slice(-4)
}

function loadModels() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    models.value = raw ? JSON.parse(raw) : getDefaults()
  } catch {
    models.value = getDefaults()
  }
}

function getDefaults() {
  return [
    { id: 'm1', modelName: 'gpt-4o', provider: 'OpenAI', apiKey: 'sk-xxxx****xxxx', baseUrl: 'https://api.openai.com/v1', temperature: 0.7 },
    { id: 'm2', modelName: 'deepseek-chat', provider: 'DeepSeek', apiKey: 'sk-xxxx****xxxx', baseUrl: 'https://api.deepseek.com/v1', temperature: 0.8 }
  ]
}

function saveModels() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(models.value))
}

function openAdd() {
  editingId.value = null
  resetForm()
  modalVisible.value = true
}

function openEdit(record) {
  editingId.value = record.id
  form.modelName = record.modelName
  form.provider = record.provider
  form.apiKey = record.apiKey
  form.baseUrl = record.baseUrl
  form.temperature = record.temperature
  modalVisible.value = true
}

function resetForm() {
  form.modelName = ''
  form.provider = ''
  form.apiKey = ''
  form.baseUrl = ''
  form.temperature = null
}

function saveModel() {
  if (!form.modelName || !form.provider || !form.apiKey || !form.baseUrl) {
    Message.warning('请填写所有必填项')
    return
  }

  if (editingId.value) {
    const idx = models.value.findIndex(m => m.id === editingId.value)
    if (idx >= 0) {
      models.value[idx] = { id: editingId.value, ...form }
    }
  } else {
    models.value.push({
      id: 'm' + Date.now(),
      modelName: form.modelName,
      provider: form.provider,
      apiKey: form.apiKey,
      baseUrl: form.baseUrl,
      temperature: form.temperature
    })
  }

  saveModels()
  modalVisible.value = false
  Message.success(editingId.value ? '模型已更新' : '模型已添加')
}

function removeModel(id) {
  models.value = models.value.filter(m => m.id !== id)
  saveModels()
  Message.success('模型已删除')
}

onMounted(loadModels)
</script>

<style scoped>
.model-config {
  padding: 0;
}
.masked-key {
  color: var(--text-tertiary);
  font-family: monospace;
  font-size: 12px;
}
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--text-tertiary);
}
.empty-state p {
  margin-top: 12px;
  color: var(--text-secondary);
  font-size: 13px;
}
.form-hint {
  color: var(--text-tertiary);
  font-size: 11px;
  margin-top: 4px;
}
</style>
