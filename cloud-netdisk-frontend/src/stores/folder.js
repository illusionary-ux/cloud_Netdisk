import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

export const useFolderStore = defineStore('folder', () => {
  const renameFolderDialogVisible = ref(false)
  const currentFolder = ref(null)
  const newFolderDialogVisible = ref(false)
  const userStore = useUserStore()

  const getFolderList = async (parentFolderId) => {
    try {
      const response = await request.get('/folders/getfolderlist', {
        params: { parentFolderId }
      })
      return response.data.map(item => ({
        id: item.folderId,
        name: item.folderName,
        type: 'folder',
        updateTime: item.updateTime || item.createTime,
        creatorId: item.creatorId,
        folderSize: item.folderSize || 0
      }))
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '获取文件夹列表失败')
      throw error
    }
  }

  const createFolder = async (folderName, parentFolderId) => {
    try {
      await request.post('/folders', {
        folderName,
        parentFolderId,
        userId: userStore.userId
      })
      newFolderDialogVisible.value = false
      ElMessage.success('创建成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '创建失败')
      throw error
    }
  }

  const renameFolder = async (folderId, newName) => {
    try {
      await request.put(`/folders/${folderId}/rename`, {
        newName
      })
      renameFolderDialogVisible.value = false
      ElMessage.success('重命名成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '重命名失败')
      throw error
    }
  }

  const deleteFolder = async (folderId) => {
    try {
      await request.delete(`/folders/${folderId}`, {
        params: { userId: userStore.userId }
      })
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
      throw error
    }
  }

  const showRenameFolderDialog = (folder) => {
    currentFolder.value = folder
    renameFolderDialogVisible.value = true
  }

  const showNewFolderDialog = () => {
    newFolderDialogVisible.value = true
  }

  return {
    renameFolderDialogVisible,
    newFolderDialogVisible,
    currentFolder,
    getFolderList,
    createFolder,
    renameFolder,
    deleteFolder,
    showRenameFolderDialog,
    showNewFolderDialog
  }
}) 