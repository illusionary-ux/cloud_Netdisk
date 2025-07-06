<template>
  <div class="app-header">
    <div class="header-content">
      <div class="logo" @click="goHome">
        <el-icon><Folder /></el-icon>
        <span>企业网盘</span>
      </div>
      <div class="user-info">
        <el-button type="primary" @click="acceptShareDialogVisible = true" style="margin-right: 16px;">接受分享</el-button>
        <el-dropdown @command="handleCommand">
          <span class="user-dropdown">
            {{ username }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="!userStore.isGuest()" command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item v-if="!userStore.isGuest()" command="updateEmail">更新邮箱</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 删除nav-menu整个el-menu，彻底去除More省略号 -->

    <!-- 添加更新邮箱对话框 -->
    <el-dialog
        v-model="updateEmailDialogVisible"
        title="更新邮箱"
        width="400px"
    >
      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef">
        <el-form-item label="新邮箱" prop="newEmail">
          <el-input v-model="emailForm.newEmail" placeholder="请输入新的邮箱地址"></el-input>
        </el-form-item>
        <el-form-item label="验证码" prop="verificationCode">
          <div style="display: flex; gap: 10px;">
            <el-input v-model="emailForm.verificationCode" placeholder="请输入验证码"></el-input>
            <el-button
                type="primary"
                :disabled="!!countdown"
                @click="sendCode"
            >
              {{ countdown ? `${countdown}s后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="updateEmailDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleUpdateEmail">确定</el-button>
    </span>
      </template>
<!--      <el-form :model="emailForm" :rules="emailRules" ref="emailFormRef">-->
<!--        <el-form-item label="新邮箱" prop="email">-->
<!--          <el-input v-model="emailForm.email" placeholder="请输入新的邮箱地址"></el-input>-->
<!--        </el-form-item>-->
<!--      </el-form>-->
<!--      <template #footer>-->
<!--        <span class="dialog-footer">-->
<!--          <el-button @click="updateEmailDialogVisible = false">取消</el-button>-->
<!--          <el-button type="primary" @click="handleUpdateEmail">确定</el-button>-->
<!--        </span>-->
<!--      </template>-->
    </el-dialog>

    <el-dialog v-model="acceptShareDialogVisible" title="接受分享" width="500px">
      <div class="custom-form-row">
        <span class="custom-label">分享链接ID</span>
        <el-input v-model="acceptShareForm.urlid" placeholder="请输入分享链接ID" style="width:300px; margin-left: 16px;"></el-input>
      </div>
      <div class="custom-form-row">
        <span class="custom-label">提取码</span>
        <el-input v-model="acceptShareForm.password" placeholder="如有请输入提取码" style="width:300px; margin-left: 16px;"></el-input>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="acceptShareDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAcceptShare">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, watch} from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, Folder, Document, Star, Delete, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'
import { useFolderStore } from '@/stores/folder'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const folderStore = useFolderStore()

const username = computed(() => userStore.username)
const isAdmin = computed(() => userStore.isAdmin())
const activeMenu = computed(() => route.path)
const updateEmailDialogVisible = ref(false)
const emailFormRef = ref(null)
const emailForm = ref({
  email: userStore.email,
  newEmail: '',
  verificationCode: ''
})
const countdown = ref(0)
const emailRules = {
  newEmail: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
}
const sendCode = async () => {
  // 校验邮箱格式
  await emailFormRef.value.validateField('newEmail')
  // await request.post('/user/send-code', { email: emailForm.value.newEmail })
  await request.post(`/user/send-code?email=${emailForm.value.newEmail}`)
  // 启动倒计时
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) clearInterval(timer)
  }, 1000)
}

const acceptShareDialogVisible = ref(false)
const acceptShareForm = ref({ urlid: '', password: '' })

const goHome = () => {
  router.push('/home')
}

const handleCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'updateEmail':
      updateEmailDialogVisible.value = true
      break
    case 'logout':
      try {
        await userStore.logout()
        router.push('/login')
      } catch (error) {
        console.error('退出登录错误:', error)
      }
      break
  }
}

watch(updateEmailDialogVisible, (val) => {
  if (val) {
    emailForm.value.email = userStore.email
  }
})
const handleUpdateEmail = async () => {
  if (!emailFormRef.value) return
  try {
    await emailFormRef.value.validate()
    await request.put('/user/email', {
      email: emailForm.value.email,
      new_email: emailForm.value.newEmail,
      verificationCode: emailForm.value.verificationCode
    })
    ElMessage.success('邮箱更新成功')
    updateEmailDialogVisible.value = false
    emailForm.value.newEmail = ''
    emailForm.value.verificationCode = ''
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response.data.message || '更新邮箱失败')
    } else {
      console.error('更新邮箱错误:', error)
    }
  }
}

const extractUrlId = (input) => {
  // 支持直接输入urlid或完整链接
  if (!input) return ''
  // 尝试从链接中提取urlid参数
  const match = input.match(/[?&]urlid=([a-zA-Z0-9]+)/)
  if (match) return match[1]
  // 如果没有?urlid=，直接返回原始输入
  return input
}

const handleAcceptShare = async () => {
  const urlid = extractUrlId(acceptShareForm.value.urlid)
  if (!urlid) {
    ElMessage.warning('请填写有效的分享链接或ID')
    return
  }
  // 用路由参数获取当前文件夹ID
  let folderId = Number(route.params.folderId) || 0
  console.log('[接受分享] 请求参数:', { userId: userStore.userId, folderId, urlid, password: acceptShareForm.value.password })
  try {
    const res = await request.post('/fileshare/acceptShare', null, {
      params: {
        userId: userStore.userId,
        folderId,
        urlid,
        password: acceptShareForm.value.password
      }
    })
    console.log('[接受分享] 后端返回:', res)
    if (res.code === 1) {
      ElMessage.success('接受分享成功')
      acceptShareDialogVisible.value = false
      acceptShareForm.value = { urlid: '', password: '' }
    } else {
      ElMessage.error(res.msg || '接受分享失败')
    }
  } catch (e) {
    console.error('[接受分享] 异常:', e)
    ElMessage.error('接受分享失败')
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  height: 60px;
  background-color: rgba(251, 251, 251, 0.92);
  border-bottom: 1px solid #dcdfe6;
  width: 100vw;
  min-width: 0;
  left: 0;
  right: 0;
  position: relative;

  .header-content {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    box-sizing: border-box;

    .logo {
      display: flex;
      align-items: center;
      font-size: 20px;
      color: dodgerblue;
      cursor: pointer;
      transition: opacity 0.3s;
      margin-right: 40px;

      &:hover {
        opacity: 0.8;
      }

      .el-icon {
        font-size: 24px;
        margin-right: 8px;
      }
    }

    .user-info {
      margin-left: 20px;
      display: flex;
      align-items: center;
      height: 60px;

      .user-dropdown {
        display: flex;
        align-items: center;
        gap: 4px;
        cursor: pointer;
        height: 60px;
      }
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.custom-form-row {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}
.custom-label {
  min-width: 80px;
  text-align: left;
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
}

.app-header {
  height: 60px !important;
  min-height: 60px !important;
  line-height: 60px !important;
}
</style> 