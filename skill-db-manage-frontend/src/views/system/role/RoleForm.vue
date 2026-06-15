<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑角色' : '新增角色'"
    :ok-text="isEdit ? '保存' : '新增'"
    @ok="handleOk"
    @cancel="emit('close')"
    :loading="loading"
    unmount-on-close
    width="520px"
  >
    <a-form :model="formData" :rules="rules" ref="formRef" layout="vertical">
      <a-form-item label="角色名称" field="name">
        <a-input v-model="formData.name" placeholder="如：ROLE_ADMIN" />
      </a-form-item>
      <a-form-item label="角色描述" field="description">
        <a-textarea v-model="formData.description" placeholder="角色描述" :rows="3" />
      </a-form-item>
      <a-form-item label="状态" field="status">
        <a-switch v-model="formData.status" :checked-value="1" :unchecked-value="0" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { createRole, updateRole } from '@/api/role'

const props = defineProps({
  visible: Boolean,
  role: { type: Object, default: null }
})
const emit = defineEmits(['close', 'success'])

const isEdit = ref(false)
const loading = ref(false)
const formRef = ref(null)

const formData = reactive({
  name: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入角色名称' }]
}

watch(() => props.visible, (val) => {
  if (val) {
    if (props.role) {
      isEdit.value = true
      formData.name = props.role.name || ''
      formData.description = props.role.description || ''
      formData.status = props.role.status ?? 1
    } else {
      isEdit.value = false
      formData.name = ''
      formData.description = ''
      formData.status = 1
    }
  }
})

async function handleOk() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid !== undefined && valid !== true) return

  loading.value = true
  try {
    if (isEdit.value) {
      await updateRole(props.role.uuid, { ...formData })
      Message.success('更新成功')
    } else {
      await createRole({ ...formData })
      Message.success('新增成功')
    }
    emit('success')
    emit('close')
  } catch (e) {
    Message.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>
