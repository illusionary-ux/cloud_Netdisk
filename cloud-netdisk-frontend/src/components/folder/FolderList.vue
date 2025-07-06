<template>
  <div class="folder-list">
    <el-table
        :data="items"
        style="width: 100%"
        v-loading="loading"
        @row-dblclick="handleRowDblClick"
    >
      <el-table-column width="50">
        <template #default="{ row }">
          <el-icon v-if="row.type === 'folder'"><Folder /></el-icon>
          <el-icon v-else><Document /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称">
        <template #default="{ row }">
          <span
              class="item-name"
              :class="{ 'folder-name': row.type === 'folder' }"
              @click="row.type === 'folder' ? handleOpenFolder(row) : null"
          >
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="updateTime" label="修改时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.type === 'folder' ? row.updateTime : row.uploadTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="size" label="大小" width="100">
        <template #default="{ row }">
          {{ row.type === 'file' ? formatFileSize(row.fileSize) : formatFileSize(row.folderSize) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="350">
        <template #default="{ row }">
          <el-button-group>
            <!-- 文件夹操作 -->
            <template v-if="row.type === 'folder'">
              <el-button size="small" @click="handleRename(row)" :disabled="!canManageFolder(row)">
                重命名
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteFolder(row)" :disabled="!canManageFolder(row)">
                删除
              </el-button>
            </template>
            <!-- 文件操作 -->
            <template v-else>
              <el-button size="small" @click="handlePreview(row)">
                预览
              </el-button>
              <el-button size="small" @click="handleDownload(row)" :disabled="!canDownloadFile(row)">
                下载
              </el-button>
              <el-button size="small" @click="handleRename(row)" :disabled="!canManageFile(row)">
                重命名
              </el-button>
              <el-button size="small" type="danger" @click="handleDeleteFile(row)" :disabled="!canManageFile(row)">
                删除
              </el-button>
              <el-button size="small" type="primary" @click="handleShare(row)" :disabled="!canShareFile(row)">
                分享
              </el-button>
              <el-button
                size="small"
                type="warning"
                @click="handleCollect(row)"
                :disabled="userStore.isGuest()"
                v-if="!row.isCollected"
              >
                收藏
              </el-button>
              <el-button
                size="small"
                type="info"
                @click="handleCancelCollect(row)"
                v-else
              >
                取消收藏
              </el-button>
            </template>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
  </div>

  <!-- 添加文件预览对话框 -->
  <file-preview
    v-if="previewVisible"
    :fileId="currentFileId"
    :visible="previewVisible"
    @close="previewVisible = false"
  />
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Document } from '@element-plus/icons-vue'
import { useFolderStore } from '@/stores/folder'
import { useUserStore } from '@/stores/user'
import { useDateTime } from '@/hooks/useDateTime'
import request from '@/utils/request'
import { collectFile, cancelCollect, isFileCollected } from '@/api/fileCollect'
import FilePreview from '@/components/FilePreview.vue'  // 导入文件预览组件

const props = defineProps({
  parentFolderId: {
    type: Number,
    default: 0
  },
  canManageFile: {
    type: Function,
    required: true
  },
  canManageFolder: {
    type: Function,
    required: true
  },
  canPreviewFile: {
    type: Function,
    required: true
  },
  canDownloadFile: {
    type: Function,
    required: true
  },
  canShareFile: {
    type: Function,
    required: true
  }
})

const router = useRouter()
const folderStore = useFolderStore()
const userStore = useUserStore()
const { formatDateTime } = useDateTime()

const loading = ref(false)
const folders = ref([])
const files = ref([])

// 合并文件夹和文件列表
const items = computed(() => [
  ...folders.value.map(f => ({
    ...f,
    type: 'folder',
    folderSize: f.folderSize || 0
  })),
  ...files.value.map(f => ({
    ...f,
    type: 'file',
    name: f.fileName
  }))
])

// 格式化文件大小
const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let index = 0
  while (size >= 1024 && index < units.length - 1) {
    size /= 1024
    index++
  }
  return `${size.toFixed(2)} ${units[index]}`
}

// 检查文件收藏状态
const checkFileCollectStatus = async (fileId) => {
  if (userStore.isGuest()) {
    return false
  }

  try {
    const response = await isFileCollected(fileId)
    return response.data
  } catch (error) {
    console.error('检查文件收藏状态失败:', error)
    return false
  }
}

// 加载文件夹和文件列表
const loadFolders = async () => {
  loading.value = true
  try {
    // 加载文件夹
    const foldersResponse = await folderStore.getFolderList(props.parentFolderId)
    folders.value = foldersResponse

    // 加载文件
    const filesResponse = await request.get(`/files/files`, {
      params: { folderId: props.parentFolderId }
    })

    // 如果是访客用户，直接设置所有文件为未收藏状态
    if (userStore.isGuest()) {
      files.value = filesResponse.data.map(file => ({ ...file, isCollected: false }))
    } else {
      // 检查每个文件的收藏状态
      const filesWithCollectStatus = await Promise.all(
        filesResponse.data.map(async (file) => {
          const isCollected = await checkFileCollectStatus(file.fileId)
          return { ...file, isCollected }
        })
      )
      files.value = filesWithCollectStatus
    }

  } catch (error) {
    ElMessage.error('加载列表失败')
  } finally {
    loading.value = false
  }
}

const handleRowDblClick = (row) => {
  if (row.type === 'folder') {
    router.push(`/folders/${row.id}`)
  }
}

const handleOpenFolder = (row) => {
  router.push(`/folders/${row.id}`)
}

const handleRename = async (row) => {
  // 检查权限
  if (row.type === 'folder' && !props.canManageFolder(row)) {
    ElMessage.warning(userStore.getNoPermissionMessage('重命名文件夹'))
    return
  }
  if (row.type === 'file' && !props.canManageFile(row)) {
    ElMessage.warning(userStore.getNoPermissionMessage('重命名文件'))
    return
  }

  try {
    // 弹出输入框
    const { value: newName } = await ElMessageBox.prompt('请输入新名称', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: row.name,
      inputValidator: (value) => {
        if (!value) {
          return '名称不能为空'
        }
        return true
      }
    })

    if (newName) {
      if (row.type === 'folder') {
        // 重命名文件夹
        await folderStore.renameFolder(row.id, newName)
      } else {
        // 重命名文件
        await request.put(`/files/${row.fileId}/rename`, null, {
          params: {
            newName,
            userId: userStore.userId
          }
        })
        ElMessage.success('重命名成功')
      }
      // 重新加载列表
      loadFolders()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '重命名失败')
    }
  }
}

const handleDeleteFolder = async (row) => {
  if (!props.canManageFolder(row)) {
    ElMessage.warning(userStore.getNoPermissionMessage('删除文件夹'))
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该文件夹吗？', '提示', {
      type: 'warning'
    })
    await folderStore.deleteFolder(row.id)
    ElMessage.success('删除成功')
    loadFolders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleDeleteFile = async (row) => {
  if (!props.canManageFile(row)) {
    ElMessage.warning(userStore.getNoPermissionMessage('删除文件'))
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该文件吗？', '提示', {
      type: 'warning'
    })
    await request.delete(`/files/${row.fileId}`, {
      params: {
        userId: userStore.userId
      }
    })
    ElMessage.success('删除成功')
    // 重新加载文件列表
    await loadFolders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 文件预览相关变量
const previewVisible = ref(false);
const currentFileId = ref('');

// 处理预览
const handlePreview = (row) => {
  if (row.type !== 'file') return;
  currentFileId.value = row.fileId;
  previewVisible.value = true;
}

const handleDownload = async (row) => {
  // 检查下载权限
  if (!props.canDownloadFile(row)) {
    ElMessage.warning(userStore.getNoPermissionMessage('下载文件'));
    return;
  }

  try {
    console.log('开始下载文件:', row.fileId);

    // 调用后端接口获取下载链接
    const res = await request.get('/files/down', {
      params: {
        fileId: row.fileId
      }
    });

    // 详细记录响应内容
    console.log('完整响应:', res);
    console.log('响应数据类型:', typeof res);
    console.log('响应数据内容:', res.data);

    // 检查响应数据
    if (!res.data) {
      throw new Error('未获取到下载链接');
    }

    // 直接在新窗口打开下载链接
    window.open(res.data, '_blank');

    ElMessage.success('开始下载');
  } catch (error) {
    console.error('下载失败:', error);
    ElMessage.error('下载失败，请稍后重试');
  }
};

const handleShare = (row) => {
  console.log('开始处理分享操作，文件信息:', row)

  if (!props.canShareFile(row)) {
    console.warn('用户没有分享权限')
    ElMessage.warning('您没有权限分享此文件')
    return
  }

  console.log('准备跳转到分享页面，文件ID:', row.fileId)
  router.push({
    path: '/share',
    query: {
      fileId: row.fileId,
      fileName: row.name
    }
  }).then(() => {
    console.log('成功跳转到分享页面')
  }).catch(error => {
    console.error('跳转到分享页面失败:', error)
    ElMessage.error('跳转失败，请重试')
  })
}

// 收藏文件
const handleCollect = async (row) => {
  if (userStore.isGuest()) {
    ElMessage.warning(userStore.getNoPermissionMessage('收藏文件'))
    return
  }

  try {
    await collectFile(row.fileId)
    ElMessage.success('收藏成功')
    await loadFolders() // 重新加载列表以更新收藏状态
  } catch (error) {
    ElMessage.error('收藏失败')
  }
}

// 取消收藏
const handleCancelCollect = async (row) => {
  try {
    await cancelCollect(row.fileId)
    ElMessage.success('取消收藏成功')
    await loadFolders() // 重新加载列表以更新收藏状态
  } catch (error) {
    ElMessage.error('取消收藏失败')
  }
}

onMounted(() => {
  loadFolders()
})

watch(
    () => props.parentFolderId,
    () => {
      loadFolders()
    }
)

defineExpose({
  loadFolders
})
</script>

<style lang="scss" scoped>
.folder-list {
  .item-name {
    &.folder-name {
      cursor: pointer;
      color: #409EFF;
      &:hover {
        text-decoration: underline;
      }
    }
  }
}

.el-table .el-table__header .el-table_1_column_4,
.el-table .el-table__row .el-table_1_column_4 {
  width: 350px !important;
  min-width: 350px !important;
}
</style>