<template>
  <div class="share-container">
    <h2>下载分享</h2>
    
    <!-- 分享方式选择 -->
    <div class="share-options">
      <el-radio-group v-model="currentShareType" class="share-type-selector">
        <el-radio-button :value="'link'">链接分享</el-radio-button>
        <el-radio-button :value="'qrcode'">二维码分享</el-radio-button>
        <el-radio-button :value="'email'">邮件分享</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 分享内容区域 -->
    <div class="share-content">
      <!-- 链接分享 -->
      <div v-if="currentShareType === 'link'" class="share-section">
        <div class="share-buttons">
          <el-button type="primary" @click="generateShareLink">生成下载分享链接</el-button>
          <el-input v-model="fileSharePassword" placeholder="可选：设置提取码" style="width: 200px; margin-left: 12px;" />
          <el-button type="success" @click="generateFileShareLink" style="margin-left: 12px;">生成文件分享链接</el-button>
        </div>
        
        <!-- 下载分享链接 -->
        <div v-if="downloadShareLink" class="share-link-container">
          <h4>下载分享链接：</h4>
          <el-input v-model="downloadShareLink" readonly>
            <template #append>
              <el-button @click="copyDownloadShareLink">复制</el-button>
            </template>
          </el-input>
        </div>
        
        <!-- 文件分享链接 -->
        <div v-if="fileShareLink" class="share-link-container">
          <h4>文件分享链接：</h4>
          <el-input v-model="fileShareLink" readonly>
            <template #append>
              <el-button @click="copyFileShareLink">复制</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 二维码分享 -->
      <div v-if="currentShareType === 'qrcode'" class="share-section">
        <el-button type="primary" @click="generateQRCode">生成二维码</el-button>
        <div class="qrcode-container">
          <canvas id="qrcode" ref="qrcodeRef"></canvas>
          <el-button v-if="shareLink" type="success" @click="downloadQRCode">下载二维码</el-button>
        </div>
      </div>

      <!-- 邮件分享 -->
      <div v-if="currentShareType === 'email'" class="share-section">
        <el-form :model="emailForm" label-width="80px">
          <el-form-item label="收件人">
            <el-input v-model="emailForm.recipients" placeholder="请输入收件人邮箱，多个收件人用逗号分隔"></el-input>
          </el-form-item>
          <el-form-item label="主题">
            <el-input v-model="emailForm.subject" placeholder="请输入邮件主题"></el-input>
          </el-form-item>
          <el-form-item label="内容">
            <el-input type="textarea" v-model="emailForm.content" :rows="4" placeholder="请输入邮件内容"></el-input>
          </el-form-item>
          <el-form-item>
            <el-button 
              type="primary" 
              @click="sendEmail" 
              :loading="isSending"
              :disabled="isSending"
            >
              {{ isSending ? '发送中...' : '发送邮件' }}
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 分享类型
const currentShareType = ref('link')
// 分享链接
const shareLink = ref('')
// 二维码引用
const qrcodeRef = ref(null)

// 获取文件信息
const fileInfo = ref({
  fileId: route.query.fileId,
  fileName: route.query.fileName || ''  // 从路由参数中获取文件名
})

console.log('Share组件初始化，路由参数:', route.query)
console.log('文件信息:', fileInfo.value)

// 邮件表单
const emailForm = ref({
  recipients: '',
  subject: '',
  content: ''
})

// 添加发送状态控制
const isSending = ref(false)

// 新增文件分享密码
const fileSharePassword = ref('')

// 新增下载分享链接
const downloadShareLink = ref('')

// 新增文件分享链接
const fileShareLink = ref('')

// 获取文件详情
const getFileInfo = async () => {
  try {
    // 如果已经有文件名，就不需要再请求了
    if (fileInfo.value.fileName) {
      emailForm.value.subject = `分享文件：${fileInfo.value.fileName}`
      return
    }

    const response = await request.get('/files/info', {
      params: {
        fileId: fileInfo.value.fileId
      }
    })
    fileInfo.value.fileName = response.data.fileName
    // 设置默认的邮件主题和内容
    emailForm.value.subject = `分享文件：${fileInfo.value.fileName}`
  } catch (error) {
    console.error('获取文件信息失败:', error)
    ElMessage.error('获取文件信息失败')
  }
}

// 生成分享链接
const generateShareLink = async () => {
  console.log('开始生成分享链接，文件ID:', fileInfo.value.fileId)
  try {
    const response = await request.get('/files/down', {
      params: {
        fileId: fileInfo.value.fileId
      }
    })
    console.log('获取分享链接响应:', response)
    // 设置默认的邮件内容
    emailForm.value.content = `我分享了一个文件给您，请点击以下链接下载：\n${response.data}`
    ElMessage.success('下载分享链接生成成功')

    // 更新下载分享链接
    downloadShareLink.value = response.data
  } catch (error) {
    console.error('生成分享链接失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    })
    ElMessage.error('生成分享链接失败')
  }
}

// 复制分享链接
const copyShareLink = () => {
  if (!shareLink.value) {
    ElMessage.warning('请先生成分享链接')
    return
  }
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 复制下载分享链接
const copyDownloadShareLink = () => {
  if (!downloadShareLink.value) {
    ElMessage.warning('请先生成下载分享链接')
    return
  }
  navigator.clipboard.writeText(downloadShareLink.value).then(() => {
    ElMessage.success('下载分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 复制文件分享链接
const copyFileShareLink = () => {
  if (!fileShareLink.value) {
    ElMessage.warning('请先生成文件分享链接')
    return
  }
  navigator.clipboard.writeText(fileShareLink.value).then(() => {
    ElMessage.success('文件分享链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

// 生成二维码
const generateQRCode = async () => {
  console.log('开始生成二维码，文件ID:', fileInfo.value.fileId)
  try {
    const response = await request.get('/files/down', {
      params: {
        fileId: fileInfo.value.fileId
      }
    })
    console.log('获取二维码链接响应:', response)
    
    // 设置下载分享链接
    downloadShareLink.value = response.data
    
    const canvas = document.querySelector('#qrcode')
    if (!canvas) {
      console.error('未找到二维码canvas元素')
      return
    }
    QRCode.toCanvas(canvas, response.data, {
      width: 200,
      margin: 1,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    }, (error) => {
      if (error) {
        console.error('生成二维码失败:', error)
        ElMessage.error('生成二维码失败')
      } else {
        console.log('二维码生成成功')
        ElMessage.success('二维码生成成功')
      }
    })
  } catch (error) {
    console.error('生成二维码失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    })
    ElMessage.error('生成二维码失败')
  }
}

// 下载二维码
const downloadQRCode = () => {
  const canvas = document.querySelector('#qrcode')
  if (!canvas) {
    ElMessage.warning('请先生成二维码')
    return
  }
  const link = document.createElement('a')
  link.download = 'qrcode.png'
  link.href = canvas.toDataURL('image/png')
  link.click()
}

// 发送邮件
const sendEmail = async () => {
  console.log('开始发送邮件，文件ID:', fileInfo.value.fileId)
  
  // 表单验证
  if (!emailForm.value.recipients) {
    console.warn('未输入收件人邮箱')
    ElMessage.warning('请输入收件人邮箱')
    return
  }

  // 防止重复点击
  if (isSending.value) {
    return
  }
  
  isSending.value = true
  try {
    // 获取文件下载链接
    const response = await request.get('/files/down', {
      params: {
        fileId: fileInfo.value.fileId
      }
    })
    console.log('获取文件下载链接响应:', response)
    
    // 发送邮件
    await request.post('/files/email', {
      to: emailForm.value.recipients,
      subject: emailForm.value.subject || '文件分享',
      content: emailForm.value.content || '我分享了一个文件给您，请点击以下链接下载：\n' + response.data,
      fileUrl: response.data
    })
    console.log('邮件发送成功')
    
    ElMessage({
      type: 'success',
      message: '邮件发送成功',
      duration: 2000
    })

    // 清空表单
    emailForm.value.recipients = ''
    emailForm.value.subject = ''
    emailForm.value.content = ''
  } catch (error) {
    console.error('发送邮件失败:', error)
    console.error('错误详情:', {
      message: error.message,
      response: error.response,
      status: error.response?.status,
      data: error.response?.data
    })
    ElMessage({
      type: 'error',
      message: '发送邮件失败：' + (error.response?.data?.message || error.message),
      duration: 3000
    })
  } finally {
    isSending.value = false
  }
}

// 新增生成文件分享链接逻辑
const generateFileShareLink = async () => {
  try {
    const response = await request.post('/fileshare/add', null, {
      params: {
        fileId: fileInfo.value.fileId,
        userId: userStore.userId,
        password: fileSharePassword.value
      }
    })
    if (response.code === 1 && response.data) {
      fileShareLink.value = response.data
      ElMessage.success('文件分享链接生成成功')
    } else {
      ElMessage.error(response.msg || '生成文件分享链接失败')
    }
  } catch (error) {
    console.error('生成文件分享链接失败:', error)
    if (error.response && error.response.data) {
      ElMessage.error(error.response.data.msg || '生成文件分享链接失败')
    } else {
      ElMessage.error('生成文件分享链接失败')
    }
  }
}

// 组件挂载时检查文件ID并获取文件信息
onMounted(async () => {
  if (!fileInfo.value.fileId) {
    ElMessage.error('未找到文件信息')
    router.push('/home')
    return
  }
  await getFileInfo()
})
</script>

<style lang="scss" scoped>
.share-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;

  h2 {
    margin-bottom: 20px;
    color: #303133;
  }

  .share-options {
    margin-bottom: 30px;
  }

  .share-content {
    background: #fff;
    padding: 20px;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }

  .share-section {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .share-link-container {
    margin-top: 20px;
  }

  .qrcode-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
    margin-top: 20px;

    canvas {
      border: 1px solid #dcdfe6;
      padding: 10px;
      background: #fff;
    }
  }
}
</style> 