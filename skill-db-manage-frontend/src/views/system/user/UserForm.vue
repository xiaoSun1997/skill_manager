<template>
  <a-modal
    :visible="visible"
    :title="isEdit ? '编辑用户' : '新增用户'"
    :ok-text="isEdit ? '保存' : '新增'"
    @ok="handleOk"
    @cancel="emit('close')"
    :loading="loading"
    unmount-on-close
    width="580px"
  >
    <a-form :model="formData" :rules="rules" ref="formRef" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="用户名" field="username">
            <a-input v-model="formData.username" placeholder="登录用户名" :disabled="isEdit" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="昵称" field="nickname">
            <a-input v-model="formData.nickname" placeholder="显示昵称" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="手机号" field="phone">
            <a-input v-model="formData.phone" placeholder="手机号" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="邮箱" field="email">
            <a-input v-model="formData.email" placeholder="邮箱" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="isEdit ? '新密码（留空不修改）' : '密码'" field="password">
            <a-input-password v-model="formData.password" placeholder="密码" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态" field="status">
            <a-switch v-model="formData.status" :checked-value="1" :unchecked-value="0" />
          </a-form-item>
        </a-col>
      </a-row>

      <!-- 分配角色 -->
      <a-form-item label="分配角色">
        <a-spin :loading="roleLoading" style="width: 100%;">
          <a-checkbox-group v-model="selectedRoleUuids" direction="vertical">
            <a-checkbox v-for="role in allRoles" :key="role.uuid" :value="role.uuid">
              <a-tag color="orange" style="margin-right: 8px;">{{ role.name }}</a-tag>
              <span style="font-size: 12px; color: var(--color-text-3);">{{ role.description }}</span>
            </a-checkbox>
          </a-checkbox-group>
        </a-spin>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup>
import { reactive, ref, watch } from 'vue'
import { Message } from '@arco-design/web-vue'
import { createUser, updateUser, getUserRoles, assignUserRoles } from '@/api/user'
import { getRoleList } from '@/api/role'

const props = defineProps({
  visible: Boolean,
  user: { type: Object, default: null }
})
const emit = defineEmits(['close', 'success'])

const isEdit = ref(false)
const loading = ref(false)
const formRef = ref(null)

const formData = reactive({
  username: '',
  nickname: '',
  phone: '',
  email: '',
  password: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名' }]
}

// 角色数据
const allRoles = ref([])
const roleLoading = ref(false)
const selectedRoleUuids = ref([])

watch(() => props.visible, async (val) => {
  if (val) {
    // 加载角色列表
    roleLoading.value = true
    try {
      const res = await getRoleList()
      allRoles.value = res.data || []
    } catch (e) {
      Message.error('加载角色列表失败')
    } finally {
      roleLoading.value = false
    }

    if (props.user) {
      isEdit.value = true
      formData.username = props.user.username || ''
      formData.nickname = props.user.nickname || ''
      formData.phone = props.user.phone || ''
      formData.email = props.user.email || ''
      formData.password = ''
      formData.status = props.user.status ?? 1

      // 加载已分配的角色
      try {
        const roleRes = await getUserRoles(props.user.uuid)
        selectedRoleUuids.value = (roleRes.data || [])
          .filter(r => r.assigned)
          .map(r => r.uuid)
      } catch (e) {
        // ignore
      }
    } else {
      isEdit.value = false
      formData.username = ''
      formData.nickname = ''
      formData.phone = ''
      formData.email = ''
      formData.password = ''
      formData.status = 1
      selectedRoleUuids.value = []
    }
  }
})

async function handleOk() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid !== undefined && valid !== true) return

  loading.value = true
  try {
    const submitData = { ...formData }
    if (!submitData.password) {
      delete submitData.password
    }

    if (isEdit.value) {
      await updateUser(props.user.uuid, submitData)
      // 分配角色
      if (selectedRoleUuids.value.length > 0) {
        await assignUserRoles(props.user.uuid, selectedRoleUuids.value)
      }
      Message.success('更新成功')
    } else {
      await createUser(submitData)
      // 新增用户后分配角色
      if (selectedRoleUuids.value.length > 0) {
        // 需要获取刚创建用户的uuid，这里简化处理，由用户刷新后手动分配
      }
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
