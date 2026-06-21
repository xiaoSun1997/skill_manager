<template>
  <div class="skill-group-list">
    <div class="sc-page-container">
      <div class="sc-page-header">
        <div class="toolbar-left">
          <a-input v-model="searchKeyword" placeholder="搜索技能组名称/描述" allow-clear style="width: 280px;">
            <template #prefix><Icon icon="ri:search-line" width="14" height="14" /></template>
          </a-input>
          <a-button type="primary" @click="handleSearch">
            <template #icon><Icon icon="ri:search-line" width="14" height="14" /></template>
            查询
          </a-button>
          <a-button @click="handleReset">重置</a-button>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="handleAdd">
            <template #icon><Icon icon="ri:add-line" width="14" height="14" /></template>
            新增技能组
          </a-button>
        </div>
      </div>

      <div class="sc-page-main">
        <a-spin :loading="loading" style="width: 100%;">
          <a-table
            :data="groupList"
            :pagination="pagination"
            :bordered="{ cell: false }"
            :stripe="true"
            row-key="uuid"
            @page-change="handlePageChange"
            @page-size-change="handlePageSizeChange"
          >
            <template #columns>
              <a-table-column title="技能组名称" data-index="name" :width="200">
                <template #cell="{ record }">
                  <a-space>
                    <Icon icon="ri:folder-line" width="18" height="18" style="color: var(--primary-color);" />
                    <span style="font-weight: 500;">{{ record.name }}</span>
                  </a-space>
                </template>
              </a-table-column>
              <a-table-column title="描述" data-index="description">
                <template #cell="{ record }">
                  {{ record.description || '-' }}
                </template>
              </a-table-column>
              <a-table-column title="子文件夹" :width="250">
                <template #cell="{ record }">
                  <a-space wrap>
                    <a-tag v-for="name in parseFolderNames(record.folderNames)" :key="name"
                           color="arcoblue" size="small">
                      {{ name }}
                    </a-tag>
                  </a-space>
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
                    <a-button type="text" size="mini" @click="handleManageFiles(record)">
                      <template #icon><Icon icon="ri:file-line" width="14" height="14" /></template>
                      文件
                    </a-button>
                    <a-button type="text" size="mini" @click="handleDownload(record)">
                      <template #icon><Icon icon="ri:download-line" width="14" height="14" /></template>
                      下载
                    </a-button>
                    <a-button type="text" size="mini" @click="handleEdit(record)">编辑</a-button>
                    <a-popconfirm content="确认删除该技能组？" @ok="handleDelete(record)">
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

    <SkillGroupForm :visible="formVisible" :group="editingGroup" @close="formVisible = false" @success="loadGroups" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { Icon } from '@iconify/vue'
import { getSkillGroupList, deleteSkillGroup, downloadSkillGroup } from '@/api/skill'
import SkillGroupForm from './SkillGroupForm.vue'

const router = useRouter()

const loading = ref(false)
const groupList = ref([])
const searchKeyword = ref('')

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showPageSize: true,
  pageSizeOptions: [10, 20, 50]
})

const formVisible = ref(false)
const editingGroup = ref(null)

function parseFolderNames(folderNames) {
  try {
    if (!folderNames) return []
    return typeof folderNames === 'string' ? JSON.parse(folderNames) : folderNames
  } catch {
    return []
  }
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

async function loadGroups() {
  loading.value = true
  try {
    const res = await getSkillGroupList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value || undefined
    })
    const pageData = res.data
    groupList.value = pageData.records || []
    pagination.total = pageData.total || 0
  } catch (e) {
    Message.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadGroups()
}

function handleReset() {
  searchKeyword.value = ''
  pagination.current = 1
  loadGroups()
}

function handlePageChange(page) {
  pagination.current = page
  loadGroups()
}

function handlePageSizeChange(size) {
  pagination.pageSize = size
  pagination.current = 1
  loadGroups()
}

function handleAdd() {
  editingGroup.value = null
  formVisible.value = true
}

function handleEdit(group) {
  editingGroup.value = group
  formVisible.value = true
}

async function handleDelete(group) {
  try {
    await deleteSkillGroup(group.uuid)
    Message.success('删除成功')
    loadGroups()
  } catch (e) {
    Message.error(e.message || '删除失败')
  }
}

function handleManageFiles(group) {
  router.push({ path: `/skill/group/${group.uuid}/files`, query: { name: group.name } })
}

async function handleDownload(group) {
  try {
    const res = await downloadSkillGroup(group.uuid)
    const blob = new Blob([res.data], { type: 'application/zip' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${group.name}.zip`
    a.click()
    URL.revokeObjectURL(url)
    Message.success('下载成功')
  } catch (e) {
    Message.error(e.message || '下载失败')
  }
}

onMounted(loadGroups)
</script>
