import { defineStore } from 'pinia'
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const username = ref('')
  const email = ref('')
  const userId = ref(null)
  const userRole = ref('') // 用户角色
  const permissions = ref([]) // 用户权限列表
  const updateEmailDialogVisible = ref(false)

  const fetchUserInfo = async () => {
    try {
      const response = await request.get('/user/profile')
      if (response.data) {
        const data = response.data
        username.value = data.username
        email.value = data.email
        userId.value = data.userId
        // 获取用户角色和权限
        const roleResponse = await request.get('/user/roles')
        if (roleResponse.data && roleResponse.data.length > 0) {
          userRole.value = roleResponse.data[0] // 暂时只取第一个角色
        }
        const permissionResponse = await request.get('/user/permissions')
        if (permissionResponse.data) {
          permissions.value = permissionResponse.data
        }
        console.log('用户信息获取成功:', { 
          username: username.value, 
          email: email.value, 
          userId: userId.value,
          role: userRole.value,
          permissions: permissions.value
        })
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
      ElMessage.error('获取用户信息失败')
    }
  }

  const hasPermission = (permission) => {
    return permissions.value.includes(permission)
  }

  const hasRole = (role) => {
    return userRole.value === role
  }

  const isGuest = () => hasRole('guest')
  const isUser = () => hasRole('user')
  const isVip = () => hasRole('vip')
  const isAdmin = () => hasRole('admin')

  const canManageFile = (file) => {
    if (isAdmin()) return true
    if (isGuest()) return false
    // user和vip可以操作自己的文件和公共文件
    return file.uploadUser === userId.value || file.isPublic
  }

  const canManageFolder = (folder) => {
    if (isAdmin()) return true
    if (isGuest()) return false
    // user和vip可以操作自己的文件夹和公共文件夹
    return folder.creatorId === userId.value || folder.isPublic
  }

  const canPreviewFile = (file) => {
    // 所有用户都可以预览文件
    return true
  }

  const canUploadFile = () => {
    // guest不能上传文件
    return !isGuest()
  }

  const canUseRecycleBin = () => {
    // guest不能使用回收站
    return !isGuest()
  }

  const canUseFavorites = () => {
    // guest不能使用收藏夹
    return !isGuest()
  }

  const canCreateFolder = () => {
    // guest不能创建文件夹
    return !isGuest()
  }

  const canDownloadFile = (file) => {
    if (isAdmin()) return true
    if (isGuest()) return file.isPublic
    return file.uploadUser === userId.value || file.isPublic
  }

  const canShareFile = (file) => {
    if (isAdmin()) return true
    if (isGuest()) return false
    return file.uploadUser === userId.value
  }

  const getNoPermissionMessage = (action) => {
    if (isGuest()) {
      return `访客用户不能${action}，请注册或登录后操作`
    }
    return `您没有权限${action}`
  }

  const logout = async () => {
    try {
      await request.post('/user/logout')
      localStorage.removeItem('token')
      username.value = ''
      email.value = ''
      userId.value = null
      userRole.value = ''
      permissions.value = []
      ElMessage.success('退出成功')
    } catch (error) {
      console.error('退出登录错误:', error)
      if (error.response?.status === 401) {
        ElMessage.warning('登录已过期，请重新登录')
      } else {
        ElMessage.error('退出失败，请稍后重试')
      }
      localStorage.removeItem('token')
      username.value = ''
      email.value = ''
      userId.value = null
      userRole.value = ''
      permissions.value = []
      throw error
    }
  }

  const showUpdateEmailDialog = () => {
    updateEmailDialogVisible.value = true
  }

  const updateEmail = async (newEmail, verificationCode) => {
    if (!email.value) {
      await fetchUserInfo() // 确保有当前邮箱
    }
    try {
      await request.put('/user/email', {
        email: email.value,
        new_email: newEmail,
        verificationCode
      })
      email.value = newEmail
      updateEmailDialogVisible.value = false
      ElMessage.success('邮箱更新成功')
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '邮箱更新失败')
      throw error
    }
  }

  return {
    username,
    email,
    userId,
    userRole,
    permissions,
    updateEmailDialogVisible,
    fetchUserInfo,
    logout,
    showUpdateEmailDialog,
    updateEmail,
    hasPermission,
    hasRole,
    isGuest,
    isUser,
    isVip,
    isAdmin,
    canManageFile,
    canManageFolder,
    canPreviewFile,
    canUploadFile,
    canUseRecycleBin,
    canUseFavorites,
    canCreateFolder,
    canDownloadFile,
    canShareFile,
    getNoPermissionMessage
  }
}) 