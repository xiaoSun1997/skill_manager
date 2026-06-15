<template>
  <div class="user-list">
    <a-card>
      <template #title>
        <a-space>
          <icon-user />
          <span>用户管理</span>
        </a-space>
      </template>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <a-space>
          <a-input v-model="searchKeyword" placeholder="用户名/昵称/手机号" allow-clear style="width: 240px;">
            <template #prefix><icon-search /></template>
          </a-input>
          <a-select v-model="searchStatus" placeholder="状态" allow-clear style="width: 120px;">
            <a-option :value="1">启用</a-option>
            <a-option :value="0">禁用</a-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">
            <template #icon><icon-search /></template>
            查询
          </a-button>
          <a-button @click="handleReset">
            <template #icon><icon-refresh /></template>
            重置
          </a-button>
        </a-space>
        <a-button type="primary" @click="handleAdd">
          <template #icon><icon-plus /></template>
          新增用户
        </a-button>
      </div>

      <a-spin :loading="loading" style="width: 100%;">
        <a-table
          :data="userList"
          :pagination="pagination"
          :bordered="false"
          :stripe="true"
          row-key="uuid"
          @page-change="handlePageChange"
          @page-size-change="handlePageSizeChange"
        >
          <template #columns>
            <a-table-column title="用户名" data-index="username" :width="150">
              <template #cell="{ record }">
                <a-space>
                  <a-avatar :size="28" :style="{ backgroundColor: '#6366F1' }">
                    {{ (record.nickname || record.username)?.charAt(0)?.toUpperCase() }}
                  </a-avatar>
                  <span>{{ record.username }}</span>
                </a-space>
              </template>
            </a-table-column>
            <a-table-column title="昵称" data-index="nickname" :width="150">
              <template #cell="{ record }">
                {{ record.nickname || '-' }}
              </template>
            </a-table-column>
            <a-table-column title="手机号" data-index="phone" :width="140">
              <template #cell="{ record }">
                {{ record.phone || '-' }}
              </template>
            </a-table-column>
            <a-table-column title="邮箱" data-index="email">
              <template #cell="{ record }">
                {{ record.email || '-' }}
              </template>
            </a-table-column>
            <a-table-column title="状态" data-index="status" :width="100">
              <template #cell="{ record }">
                <a-tag :color="record.status === 1 ? 'green' : 'red'">
                  {{ record.status === 1 ? '启用' : '禁用' }}
                </a-tag>
              </template>
            </a-table-column>
            <a-table-column title="创建时间" data-index="createdAt" :width="180">
              <template #cell="{ record }">
                {{ record.createdAt ? formatTime(record.createdAt) : '-' }}
              </template>
            </a-table-column>
            <a-table-column title="操作" :width="280" fixed="right">
              <template #cell="{ record }">
                <a-space>
                  <a-button type="text" size="small" @click="handleEdit(record)">
                    <template #icon><icon-edit /></template>
                    编辑
                  </a-button>
                  <a-button type="text" size="small" @click="handleAssignRole(record)">
                    <template #icon><icon-team /></template>
                    分配角色
                  </a-button>
                  <a-popconfirm content="确认删除该用户？" @ok="handleDelete(record)">
                    <a-button type="text" size="small" status="danger">
                      <template #icon><icon-delete /></template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </a-space>
              </template>
            </a-table-column>
          </template>
        </a-table>
      </a-spin>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <UserForm :visible="formVisible" :user="editingUser" @close="formVisible = false" @success="loadUsers" />

    <!-- 分配角色弹窗 -->
    <a-modal
      :visible="roleAssignVisible"
      title="分配角色"
      @ok="handleAssignRoleOk"
      @cancel="roleAssignVisible = false"
      :loading="roleAssignLoading"
      width="500px"
    >
      <a-spin :loading="roleTreeLoading" style="width: 100%;">
        <a-checkbox-group v-model="assignRoleUuids" direction="vertical">
          <div v-for="role in allAssignRoles" :key="role.uuid" style="padding: 8px 0;">
            <a-checkbox :value="role.uuid">
              <a-tag color="orange" style="margin-right: 8px;">{{ role.name }}</a-tag>
              <span style="font-size: 12px; color: var(--color-text-3);">{{ role.description }}</span>
            </a-checkbox>
          </div>
        </a-checkbox-group>
        <a-empty v-if="allAssignRoles.length === 0" description="暂无角色数据" />
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Message } from '@arco-design/web-vue'
import { getUserList, deleteUser, getUserRoles, assignUserRoles } from '@/api/user'
import { getRoleList } from '@/api/role'
import UserForm from './UserForm.vue'

const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')
const searchStatus = ref(undefined)

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50]
})

// Form
const formVisible = ref(false)
const editingUser = ref(null)

// Role assign
const roleAssignVisible = ref(false)
const roleAssignLoading = ref(false)
const roleTreeLoading = ref(false)
const allAssignRoles = ref([])
const assignRoleUuids = ref([])
const assigningUser = ref(null)

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function loadUsers() {
  loading.value = true
  try {
    const res = await getUserList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value || undefined,
      status: searchStatus.value
    })
    const pageData = res.data
    userList.value = pageData.records || []
    pagination.total = pageData.total || 0
  } catch (e) {
    Message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadUsers()
}

function handleReset() {
  searchKeyword.value = ''
  searchStatus.value = undefined
  pagination.current = 1
  loadUsers()
}

function handlePageChange(page) {
  pagination.current = page
  loadUsers()
}

function handlePageSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadUsers()
}

function handleAdd() {
  editingUser.value = null
  formVisible.value = true
}

function handleEdit(user) {
  editingUser.value = user
  formVisible.value = true
}

async function handleDelete(user) {
  try {
    await deleteUser(user.uuid)
    Message.success('删除成功')
    loadUsers()
  } catch (e) {
    Message.error(e.message || '删除失败')
  }
}

async function handleAssignRole(user) {
  assigningUser.value = user
  roleAssignVisible.value = true
  roleTreeLoading.value = true
  try {
    const [rolesRes, userRoleRes] = await Promise.all([
      getRoleList(),
      getUserRoles(user.uuid)
    ])
    allAssignRoles.value = rolesRes.data || []
    assignRoleUuids.value = (userRoleRes.data || [])
      .filter(r => r.assigned)
      .map(r => r.uuid)
  } catch (e) {
    Message.error(e.message || '加载数据失败')
  } finally {
    roleTreeLoading.value = false
  }
}

async function handleAssignRoleOk() {
  roleAssignLoading.value = true
  try {
    await assignUserRoles(assigningUser.value.uuid, assignRoleUuids.value)
    Message.success('角色分配成功')
    roleAssignVisible.value = false
  } catch (e) {
    Message.error(e.message || '分配失败')
  } finally {
    roleAssignLoading.value = false
  }
}

onMounted(loadUsers)
</script>

<style scoped>
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}
</style>
