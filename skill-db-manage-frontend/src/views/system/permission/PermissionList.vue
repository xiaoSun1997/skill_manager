<template>
  <div class="permission-list">
    <div class="sc-page-container">
      <!-- 工具栏 -->
      <div class="sc-page-header">
        <div class="toolbar-left">
          <span style="font-size: 14px; font-weight: 500; color: var(--text-primary);">权限字典</span>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="handleAdd(null)">
            <template #icon><Icon icon="ri:add-line" width="14" height="14" /></template>
            新增根权限
          </a-button>
        </div>
      </div>

      <!-- 内容 -->
      <div class="sc-page-main" style="padding: 15px;">
        <a-spin :loading="loading" style="width: 100%;">
          <a-empty v-if="!loading && treeData.length === 0" description="暂无数据" />
          <a-tree
            v-else
            :data="treeData"
            :field-names="{
              key: 'uuid',
              title: 'name',
              children: 'children'
            }"
            default-expand-all
            block-node
            :render-extra="renderExtra"
          />
        </a-spin>
      </div>
    </div>

    <!-- 新增/编辑权限弹窗 -->
    <a-modal
      :visible="formVisible"
      :title="editingUuid ? '编辑权限' : '新增权限'"
      :ok-text="editingUuid ? '保存' : '新增'"
      @ok="handleFormOk"
      @cancel="formVisible = false"
      :loading="formLoading"
      unmount-on-close
      width="580px"
    >
      <a-form :model="formData" :rules="formRules" ref="formRef" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="权限名称" field="name">
              <a-input v-model="formData.name" placeholder="如：用户管理" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="权限编码" field="code">
              <a-input v-model="formData.code" placeholder="如：system:user:list" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="类型" field="type">
              <a-select v-model="formData.type">
                <a-option :value="1">目录</a-option>
                <a-option :value="2">菜单</a-option>
                <a-option :value="3">按钮</a-option>
                <a-option :value="4">API</a-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="父级权限" field="parentId">
              <a-tree-select
                v-model="formData.parentId"
                :data="parentTreeData"
                :field-names="{
                  key: 'uuid',
                  title: 'name',
                  children: 'children'
                }"
                placeholder="选择父级权限"
                allow-clear
                :fallback-option="false"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="路由路径" field="path">
              <a-input v-model="formData.path" placeholder="如：/system/user" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="组件路径" field="component">
              <a-select v-model="formData.component" allow-clear placeholder="选择组件">
                <a-option value="system/user/UserList">system/user/UserList</a-option>
                <a-option value="system/role/RoleList">system/role/RoleList</a-option>
                <a-option value="system/permission/PermissionList">system/permission/PermissionList</a-option>
                <a-option value="dashboard/Dashboard">dashboard/Dashboard</a-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="路由名称" field="routeName">
              <a-input v-model="formData.routeName" placeholder="如：UserList" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="图标" field="icon">
              <a-input v-model="formData.icon" placeholder="如：IconUser" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="排序号" field="sortOrder">
              <a-input-number v-model="formData.sortOrder" :min="0" style="width: 100%;" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="状态" field="status">
              <a-switch v-model="formData.status" :checked-value="1" :unchecked-value="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="缓存页面" field="keepAlive">
              <a-switch v-model="formData.keepAlive" :checked-value="1" :unchecked-value="0" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="隐藏菜单" field="hidden">
              <a-switch v-model="formData.hidden" :checked-value="1" :unchecked-value="0" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, h } from 'vue'
import { Message, Modal } from '@arco-design/web-vue'
import { Icon } from '@iconify/vue'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/permission'

const loading = ref(false)
const treeData = ref([])
const formVisible = ref(false)
const formLoading = ref(false)
const editingUuid = ref(null)
const formRef = ref(null)

const typeLabels = { 1: '目录', 2: '菜单', 3: '按钮', 4: 'API' }
const typeColors = { 1: '#3B82F6', 2: '#00B4FF', 3: '#00CE89', 4: '#FFC800' }

const formData = reactive({
  name: '',
  code: '',
  type: 2,
  parentId: undefined,
  path: '',
  component: '',
  routeName: '',
  icon: '',
  sortOrder: 0,
  status: 1,
  keepAlive: 1,
  hidden: 0
})

const formRules = {
  name: [{ required: true, message: '请输入权限名称' }],
  code: [{ required: true, message: '请输入权限编码' }]
}

const parentTreeData = computed(() => {
  return treeData.value
})

function renderExtra(nodeData) {
  const data = nodeData
  return h('span', { class: 'node-extra' }, [
    h('span', {
      class: 'type-tag',
      style: {
        color: typeColors[data.type] || '#999',
        fontSize: '12px',
        marginRight: '12px'
      }
    }, typeLabels[data.type] || '未知'),
    h('span', { style: { color: 'var(--color-text-3)', fontSize: '12px', marginRight: '12px' } },
      data.code || ''),
    h('a-button', {
      type: 'text',
      size: 'small',
      style: { color: '#00B4FF', marginRight: '4px' },
      onClick: (e) => { e.stopPropagation(); handleAdd(data) }
    }, [h(Icon, { icon: 'ri:add-line' })]),
    h('a-button', {
      type: 'text',
      size: 'small',
      style: { color: '#FFC800', marginRight: '4px' },
      onClick: (e) => { e.stopPropagation(); handleEdit(data) }
    }, [h(Icon, { icon: 'ri:edit-line' })]),
    h('a-button', {
      type: 'text',
      size: 'small',
      style: { color: '#F53F3F' },
      onClick: (e) => { e.stopPropagation(); handleDelete(data) }
    }, [h(Icon, { icon: 'ri:delete-bin-line' })])
  ])
}

async function loadTree() {
  loading.value = true
  try {
    const res = await getPermissionTree()
    treeData.value = res.data || []
  } catch (e) {
    Message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleAdd(parent) {
  editingUuid.value = null
  Object.assign(formData, {
    name: '',
    code: '',
    type: parent ? (parent.type < 2 ? 2 : 3) : 1,
    parentId: parent ? parent.uuid : undefined,
    path: '',
    component: '',
    routeName: '',
    icon: parent ? 'IconList' : 'IconFolder',
    sortOrder: 0,
    status: 1,
    keepAlive: 1,
    hidden: 0
  })
  formVisible.value = true
}

function handleEdit(data) {
  editingUuid.value = data.uuid
  Object.assign(formData, {
    name: data.name || '',
    code: data.code || '',
    type: data.type || 2,
    parentId: data.parentId || undefined,
    path: data.path || '',
    component: data.component || '',
    routeName: data.routeName || '',
    icon: data.icon || '',
    sortOrder: data.sortOrder || 0,
    status: data.status ?? 1,
    keepAlive: data.keepAlive ?? 1,
    hidden: data.hidden ?? 0
  })
  formVisible.value = true
}

function handleDelete(data) {
  Modal.confirm({
    title: '确认删除',
    content: `确认删除权限「${data.name}」吗？`,
    okText: '确认删除',
    okButtonProps: { status: 'danger' },
    onOk: async () => {
      try {
        await deletePermission(data.uuid)
        Message.success('删除成功')
        loadTree()
      } catch (e) {
        Message.error(e.message || '删除失败')
      }
    }
  })
}

async function handleFormOk() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (valid !== undefined && valid !== true) return

  formLoading.value = true
  try {
    const submitData = { ...formData }
    if (editingUuid.value) {
      await updatePermission(editingUuid.value, submitData)
      Message.success('更新成功')
    } else {
      await createPermission(submitData)
      Message.success('新增成功')
    }
    formVisible.value = false
    loadTree()
  } catch (e) {
    Message.error(e.message || '操作失败')
  } finally {
    formLoading.value = false
  }
}

onMounted(loadTree)
</script>

<style scoped>
.permission-list {
  padding: 0;
}

:deep(.arco-tree-node) {
  padding: 6px 0;
}

:deep(.arco-tree-node-title) {
  font-size: 14px;
  font-weight: 500;
}

:deep(.arco-tree-node-selected) {
  background: rgba(59, 130, 246, 0.08) !important;
}

.node-extra {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
}

:deep(.arco-tree-node-title:hover .node-extra) {
  opacity: 1;
}
</style>
