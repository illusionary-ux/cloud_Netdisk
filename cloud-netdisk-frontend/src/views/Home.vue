<template>
  <div class="home-content">
    <!-- 面包屑导航 -->
    <div class="breadcrumb-container">
      <el-breadcrumb separator=">">
        <el-breadcrumb-item :to="{ path: '/home' }">全部文件</el-breadcrumb-item>
        <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="{ path: item.path }">
          {{ item.name }}
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <!-- 操作按钮 -->
    <div class="operation-container">
      <el-button type="primary" @click="handleUpload">
        <el-icon><Upload /></el-icon>
        上传文件
      </el-button>
      <el-button @click="handleNewFolder">
        <el-icon><FolderAdd /></el-icon>
        新建文件夹
      </el-button>
    </div>

    <!-- 文件列表 -->
    <folder-list
      ref="folderListRef"
      :parent-folder-id="currentFolderId"
      :can-manage-file="canManageFile"
      :can-manage-folder="canManageFolder"
      :can-preview-file="canPreviewFile"
      :can-download-file="canDownloadFile"
      :can-share-file="canShareFile"
    />

    <!-- 上传文件对话框 -->
    <el-dialog
      v-model="uploadDialogVisible"
      title="上传文件"
      width="500px"
    >
      <el-upload
        class="upload-demo"
        drag
        action="/api/files/upload"
        :headers="uploadHeaders"
        :data="uploadData"
        :on-success="handleUploadSuccess"
        :on-error="handleUploadError"
        multiple
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
      </el-upload>
    </el-dialog>

    <!-- 文件夹操作对话框 -->
    <folder-dialog
      :parent-folder-id="currentFolderId"
      @success="handleFolderOperationSuccess"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import {Document, Star, Delete, Upload, FolderAdd, UploadFilled, UserFilled} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/Header.vue'
import FolderList from '@/components/folder/FolderList.vue'
import FolderDialog from '@/components/folder/FolderDialog.vue'
import { useFolderStore } from '@/stores/folder'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { formatDateTime } from '@/utils/format'

const route = useRoute()
const folderStore = useFolderStore()
const userStore = useUserStore()
const folderListRef = ref(null)

const uploadDialogVisible = ref(false)
const breadcrumbs = ref([])

// 当前文件夹ID
const currentFolderId = computed(() => {
  const folderId = route.params.folderId
  return folderId ? Number(folderId) : 0
})
// 构建上传所需的数据
const uploadData = computed(() => ({
  folderId: currentFolderId.value,
  userId: userStore.userId
}))
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${localStorage.getItem('token')}`
}))

// 加载文件夹路径
const loadFolderPath = async () => {
  if (currentFolderId.value === 0) {
    breadcrumbs.value = []
    return
  }
  try {
    const response = await request.get(`/folders/${currentFolderId.value}/path`)
    breadcrumbs.value = response.data
        .filter(item => item.folderName!=='root')
        .map(item => ({
      name: item.folderName,
      path: `/folders/${item.folderId}`
    }))
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '加载文件夹路径失败')
  }
}

// 初始化
onMounted(async () => {
  await userStore.fetchUserInfo()
  await loadFolderPath()
})

// 监听路由变化
watch(
  () => route.params.folderId,
  async () => {
    await loadFolderPath()
  }
)

// 文件夹操作
const handleNewFolder = () => {
  if (!userStore.canCreateFolder()) {
    ElMessage.warning(userStore.getNoPermissionMessage('创建文件夹'))
    return
  }
  folderStore.showNewFolderDialog()
}

const handleFolderOperationSuccess = () => {
  if (folderListRef.value) {
    folderListRef.value.loadFolders()
  }
}

// 文件上传
const handleUpload = () => {
  if (!userStore.canUploadFile()) {
    ElMessage.warning(userStore.getNoPermissionMessage('上传文件'))
    return
  }
  uploadDialogVisible.value = true
}

const handleUploadSuccess = () => {
  ElMessage.success('上传成功')
  uploadDialogVisible.value = false
  if (folderListRef.value) {
    folderListRef.value.loadFolders()
  }
}

const handleUploadError = () => {
  ElMessage.error('上传失败'+ (error.message || '请检查参数是否完整'))
}

// 权限控制函数
const canManageFile = (file) => {
  return userStore.canManageFile(file)
}

const canManageFolder = (folder) => {
  return userStore.canManageFolder(folder)
}

const canPreviewFile = (file) => {
  return userStore.canPreviewFile(file)
}

const canDownloadFile = (file) => {
  return userStore.canDownloadFile(file)
}

const canShareFile = (file) => {
  return userStore.canShareFile(file)
}
</script>

<style lang="scss" scoped>
.home-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.upload-demo {
  width: 100%;
}

.breadcrumb-container {
  margin-bottom: 16px;
}

.operation-container {
  margin-bottom: 16px;
}
</style>