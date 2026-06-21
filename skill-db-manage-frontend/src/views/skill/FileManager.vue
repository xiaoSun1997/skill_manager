<template>
  <div class="file-manager">
    <div class="sc-page-container">
      <div class="sc-page-header">
        <div class="toolbar-left">
          <a-breadcrumb>
            <a-breadcrumb-item>
              <a-link @click="router.push('/skill/group')">技能组管理</a-link>
            </a-breadcrumb-item>
            <a-breadcrumb-item>{{ groupName }}</a-breadcrumb-item>
          </a-breadcrumb>
        </div>
        <div class="toolbar-right">
          <a-button type="primary" @click="uploadVisible = true">
            <template #icon><Icon icon="ri:upload-line" width="14" height="14" /></template>
            上传文件
          </a-button>
        </div>
      </div>

      <div class="sc-page-main">
        <a-tabs v-model:active-key="activeTab" @change="handleTabChange">
          <a-tab-pane v-for="folder in folders" :key="folder" :title="folder" />
        </a-tabs>

        <a-spin :loading="loading" style="width: 100%;">
          <a-table
            :data="fileList"
            :bordered="{ cell: false }"
            :stripe="true"
            row-key="uuid"
          >
            <template #columns>
              <a-table-column title="文件名" data-index="fileName">
                <template #cell="{ record }">
                  <a-space>
                    <Icon icon="ri:file-line" width="16" height="16" style="color: var(--color-text-3);" />
                    <span>{{ record.fileName }}</span>
                  </a-space>
                </template>
              </a-table-column>
              <a-table-column title="大小" :width="120">
                <template #cell="{ record }">
                  {{ formatSize(record.fileSize) }}
                </template>
              </a-table-column>
              <a-table-column title="类型" :width="140">
                <template #cell="{ record }">
                  {{ record.fileType || '-' }}
                </template>
              </a-table-column>
              <a-table-column title="上传时间" :width="180">
                <template #cell="{ record }">
                  {{ record.createdAt ? formatTime(record.createdAt) : '-' }}
                </template>
              </a-table-column>
              <a-table-column title="操作" :width="140" fixed="right" align="right">
                <template #cell="{ record }">
                  <a-button-group>
                    <a-button type="text" size="mini" @click="handleDownload(record)">下载</a-button>
                    <a-popconfirm content="确认删除该文件？" @ok="handleDelete(record)">
                      <a-button type="text" size="mini" status="danger">删除</a-button>
                    </a-popconfirm>
                  </a-button-group>
                </template>
              </a-table-column>
            </template>
          </a-table>
          <a-empty v-if="!loading && fileList.length === 0" description="暂无文件" style="margin-top: 40px;" />
        </a-spin>
      </div>
    </div>

    <!-- Upload Modal -->
    <a-modal
      :visible="uploadVisible"
      title="上传文件"
      @ok="handleUpload"
      @cancel="uploadVisible = false"
      :ok-loading="uploading"
      width="480px"
    >
      <a-form layout="vertical">
        <a-form-item label="目标文件夹" required>
          <a-select v-model="uploadFolder" placeholder="选择文件夹">
            <a-option v-for="f in folders" :key="f" :value="f">{{ f }}</a-option>
          </a-select>
        </a-form-item>
        <a-form-item label="选择文件" required>
          <a-upload
            :auto-upload="false"
            :limit="1"
            :file-list="uploadFileList"
            @change="handleFileChange"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { Icon } from '@iconify/vue'
import { getSkillFiles, uploadSkillFile, deleteSkillFile, getSkillGroupByUuid } from '@/api/skill'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const groupUuid = route.params.uuid
const groupName = ref(route.query.name || '技能组')

const loading = ref(false)
const folders = ref([])
const activeTab = ref('')
const fileList = ref([])

const uploadVisible = ref(false)
const uploading = ref(false)
const uploadFolder = ref('')
const uploadFileList = ref([])

async function loadGroupInfo() {
  try {
    const res = await getSkillGroupByUuid(groupUuid)
    const group = res.data
    groupName.value = group.name
    try {
      const parsed = typeof group.folderNames === 'string' ? JSON.parse(group.folderNames) : group.folderNames
      folders.value = Array.isArray(parsed) ? parsed : ['references', 'scripts', 'templates']
    } catch {
      folders.value = ['references', 'scripts', 'templates']
    }
    if (folders.value.length > 0) {
      activeTab.value = folders.value[0]
    }
  } catch (e) {
    Message.error('加载技能组信息失败')
  }
}

async function loadFiles() {
  if (!activeTab.value) return
  loading.value = true
  try {
    const res = await getSkillFiles(groupUuid, activeTab.value)
    fileList.value = res.data || []
  } catch (e) {
    Message.error(e.message || '加载文件失败')
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  loadFiles()
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let i = 0
  let size = bytes
  while (size >= 1024 && i < units.length - 1) {
    size /= 1024
    i++
  }
  return size.toFixed(1) + ' ' + units[i]
}

function formatTime(time) {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

function handleFileChange(fileList) {
  uploadFileList.value = fileList
}

async function handleUpload() {
  if (!uploadFolder.value) {
    Message.warning('请选择目标文件夹')
    return
  }
  if (uploadFileList.value.length === 0) {
    Message.warning('请选择文件')
    return
  }

  uploading.value = true
  try {
    const file = uploadFileList.value[0].file
    const formData = new FormData()
    formData.append('folderName', uploadFolder.value)
    formData.append('file', file)
    await uploadSkillFile(groupUuid, formData)
    Message.success('上传成功')
    uploadVisible.value = false
    uploadFolder.value = ''
    uploadFileList.value = []
    loadFiles()
  } catch (e) {
    Message.error(e.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

async function handleDownload(file) {
  try {
    const res = await request({
      url: `/skill/groups/${groupUuid}/files/${file.uuid}`,
      method: 'get',
      responseType: 'blob'
    })
    const blob = new Blob([res.data])
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = file.fileName
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    Message.error('下载失败')
  }
}

async function handleDelete(file) {
  try {
    await deleteSkillFile(groupUuid, file.uuid)
    Message.success('删除成功')
    loadFiles()
  } catch (e) {
    Message.error(e.message || '删除失败')
  }
}

onMounted(async () => {
  await loadGroupInfo()
  loadFiles()
})
</script>
