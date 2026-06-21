<template>
  <div class="role-list">
    <div class="sc-page-container">
      <!-- 工具栏 -->
      <div class="sc-page-header">
        <div class="toolbar-left">
          <span style="font-size: 14px; font-weight: 500; color: var(--text-primary);">角色管理</span>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="handleAdd">
            <template #icon><Icon icon="ri:add-line" width="14" height="14" /></template>
            新增角色
          </a-button>
        </div>
      </div>

      <!-- 表格 -->
      <div class="sc-page-main">
        <a-spin :loading="loading" style="width: 100%;">
          <a-table
            :data="roleList"
            :pagination="false"
            :bordered="{ cell: false }"
            :stripe="true"
            row-key="uuid"
          >
            <template #columns>
              <a-table-column title="角色名称" data-index="name" :width="200">
                <template #cell="{ record }">
                  <a-tag color="orange" size="small">{{ record.name }}</a-tag>
                </template>
              </a-table-column>
              <a-table-column title="角色描述" data-index="description" />
              <a-table-column title="状态" data-index="status" :width="100">
                <template #cell="{ record }">
                  <a-tag :color="record.status === 1 ? 'green' : 'red'" size="small">
                    {{ record.status === 1 ? '启用' : '禁用' }}
                  </a-tag>
                </template>
              </a-table-column>
              <a-table-column title="创建时间" data-index="createdAt" :width="180">
                <template #cell="{ record }">
                  {{ record.createdAt ? formatTime(record.createdAt) : '-' }}
                </template>
              </a-table-column>
              <a-table-column title="操作" :width="220" fixed="right" align="right">
                <template #cell="{ record }">
                  <a-button-group>
                    <a-button type="text" size="mini" @click="handleEdit(record)">编辑</a-button>
                    <a-button type="text" size="mini" @click="handleAssignPermission(record)">分配权限</a-button>
                    <a-popconfirm content="确认删除该角色？" @ok="handleDelete(record)">
                      <a-button type="text" size="mini" status="danger">删除</a-button>
                    </a-popconfirm>
                  </a-button-group>
                </template>
              </a-table-column>
            </template>
          </a-table>
        </a-spin>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <RoleForm :visible="formVisible" :role="editingRole" @close="formVisible = false" @success="loadRoles" />

    <!-- 分配权限弹窗 -->
    <a-modal
      :visible="permVisible"
      title="分配权限"
      @ok="handleAssignOk"
      @cancel="permVisible = false"
      :loading="permLoading"
      width="500px"
    >
      <a-spin :loading="permTreeLoading" style="width: 100%;">
        <a-tree
          v-if="permTreeData.length > 0"
          :data="permTreeData"
          :field-names="{
            key: 'uuid',
            title: 'name',
            children: 'children'
          }"
          :checked-keys="checkedKeys"
          checkable
          check-strictly
          default-expand-all
          @check="handleCheck"
        />
        <a-empty v-else description="暂无权限数据" />
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { Icon } from '@iconify/vue'
import { getRoleList, deleteRole, getRolePermissions, assignRolePermissions, getAllPermissionTree, getRolePermissionTree } from '@/api/role'
import RoleForm from './RoleForm.vue'

const loading = ref(false)
const roleList = ref([])

// Form
const formVisible = ref(false)
const editingRole = ref(null)

// Permission assign
const permVisible = ref(false)
const permTreeLoading = ref(false)
const permLoading = ref(false)
const permTreeData = ref([])
const checkedKeys = ref([])
const assigningRole = ref(null)
const checkedState = ref({})

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function loadRoles() {
  loading.value = true
  try {
    const res = await getRoleList()
    roleList.value = res.data || []
  } catch (e) {
    Message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingRole.value = null
  formVisible.value = true
}

function handleEdit(role) {
  editingRole.value = role
  formVisible.value = true
}

async function handleDelete(role) {
  try {
    await deleteRole(role.uuid)
    Message.success('删除成功')
    loadRoles()
  } catch (e) {
    Message.error(e.message || '删除失败')
  }
}

async function handleAssignPermission(role) {
  assigningRole.value = role
  permVisible.value = true
  permTreeLoading.value = true
  try {
    // 获取权限树和已分配的权限
    const [treeRes, assignedRes] = await Promise.all([
      getAllPermissionTree(),
      getRolePermissions(role.uuid)
    ])
    permTreeData.value = treeRes.data || []
    const assignedUuids = assignedRes.data || []
    // 收集所有已勾选的 uuid（包括父节点）
    const keys = new Set()
    assignedUuids.forEach(id => keys.add(id))
    // 父节点也勾选
    function collectParentKeys(nodes) {
      for (const n of nodes) {
        if (assignedUuids.includes(n.uuid)) {
          keys.add(n.uuid)
        }
        if (n.children) {
          collectParentKeys(n.children)
        }
      }
    }
    collectParentKeys(treeRes.data || [])
    checkedKeys.value = Array.from(keys)
  } catch (e) {
    Message.error(e.message || '加载权限树失败')
  } finally {
    permTreeLoading.value = false
  }
}

function handleCheck(keys, extra) {
  checkedKeys.value = keys
}

async function handleAssignOk() {
  permLoading.value = true
  try {
    await assignRolePermissions(assigningRole.value.uuid, checkedKeys.value)
    Message.success('权限分配成功')
    permVisible.value = false
  } catch (e) {
    Message.error(e.message || '分配失败')
  } finally {
    permLoading.value = false
  }
}

onMounted(loadRoles)
</script>
