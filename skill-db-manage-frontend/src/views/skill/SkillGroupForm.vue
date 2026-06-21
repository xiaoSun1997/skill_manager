<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑技能组' : '新增技能组'"
    @ok="handleSubmit"
    @cancel="handleClose"
    :ok-loading="submitting"
    width="600px"
  >
    <a-form :model="form" layout="vertical">
      <a-form-item label="技能组名称" required>
        <a-input v-model="form.name" placeholder="请输入技能组名称" :max-length="50" />
      </a-form-item>
      <a-form-item label="描述">
        <a-textarea v-model="form.description" placeholder="请输入描述（选填）" :max-length="500" :auto-size="{ minRows: 2, maxRows: 4 }" />
      </a-form-item>
      <a-form-item label="子文件夹">
        <a-input-tag v-model="folderTags" placeholder="输入后回车添加" allow-clear />
        <div style="margin-top: 4px; font-size: 12px; color: var(--color-text-3);">
          例如：references、scripts、templates
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { createSkillGroup, updateSkillGroup } from '@/api/skill'

const props = defineProps({
  visible: Boolean,
  group: Object
})

const emit = defineEmits(['close', 'success'])

const submitting = ref(false)
const folderTags = ref(['references', 'scripts', 'templates'])

const form = ref({
  name: '',
  description: '',
  folderNames: '["references","scripts","templates"]'
})

const isEdit = computed(() => !!props.group)

watch(() => props.visible, (val) => {
  if (val) {
    if (props.group) {
      form.value.name = props.group.name || ''
      form.value.description = props.group.description || ''
      try {
        const parsed = typeof props.group.folderNames === 'string'
          ? JSON.parse(props.group.folderNames)
          : (props.group.folderNames || ['references', 'scripts', 'templates'])
        folderTags.value = Array.isArray(parsed) ? parsed : ['references', 'scripts', 'templates']
      } catch {
        folderTags.value = ['references', 'scripts', 'templates']
      }
    } else {
      form.value.name = ''
      form.value.description = ''
      folderTags.value = ['references', 'scripts', 'templates']
    }
  }
})

async function handleSubmit() {
  if (!form.value.name.trim()) {
    Message.warning('请输入技能组名称')
    return
  }

  form.value.folderNames = JSON.stringify(folderTags.value)
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSkillGroup(props.group.uuid, form.value)
      Message.success('更新成功')
    } else {
      await createSkillGroup(form.value)
      Message.success('创建成功')
    }
    emit('success')
    handleClose()
  } catch (e) {
    Message.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

function handleClose() {
  emit('close')
}
</script>
