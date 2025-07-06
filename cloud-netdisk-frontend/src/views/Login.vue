<template>
  <div class="login-container">
    <div class="login-box">
      <h2>企业网盘</h2>
      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-width="0">
        <el-form-item prop="email">
          <el-input 
            v-model="loginForm.email" 
            placeholder="邮箱"
            prefix-icon="Message"
          />
        </el-form-item>
        
        <el-form-item prop="password" v-if="!isCodeLogin">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="verificationCode" v-if="isCodeLogin">
          <div class="code-input">
            <el-input 
              v-model="loginForm.verificationCode" 
              placeholder="验证码"
              prefix-icon="Key"
            />
            <el-button 
              type="primary" 
              :disabled="!!countdown"
              @click="sendCode"
            >
              {{ countdown ? `${countdown}s后重试` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            class="login-button" 
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form-item>

        <div class="login-options">
          <el-link type="primary" @click="isCodeLogin = !isCodeLogin">
            {{ isCodeLogin ? '密码登录' : '验证码登录' }}
          </el-link>
          <el-link type="primary" @click="$router.push('/register')">
            注册账号
          </el-link>
        </div>

        <div class="guest-login">
          <el-divider>或</el-divider>
          <el-button 
            type="info" 
            plain 
            class="guest-button"
            @click="handleGuestLogin"
          >
            以访客身份进入
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)
const isCodeLogin = ref(false)
const countdown = ref(0)

const loginForm = reactive({
  email: '',
  password: '',
  verificationCode: ''
})

const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { len: 6, message: '验证码长度应为6位', trigger: 'blur' }
  ]
}

const startCountdown = () => {
  countdown.value = 60
  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

const sendCode = async () => {
  try {
    await loginFormRef.value.validateField('email')
    await axios.post('/api/user/send-code', null, {
      params: {
        email: loginForm.email
      }
    })
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response?.data?.message || '验证码发送失败')
    }
  }
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    await loginFormRef.value.validate()
    loading.value = true
    
    const response = await axios.post('/api/user/login', {
      email: loginForm.email,
      password: isCodeLogin.value ? undefined : loginForm.password,
      verificationCode: isCodeLogin.value ? loginForm.verificationCode : undefined,
      loginType: isCodeLogin.value ? 1 : 0
    })
    
    if (response.data.code === 1) {
      localStorage.setItem('token', response.data.data.token)
      ElMessage.success('登录成功')
      router.push('/home')
    } else {
      ElMessage.error(response.data.msg || '登录失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response?.data?.msg || '登录失败')
    } else {
      ElMessage.error('登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}

const handleGuestLogin = async () => {
  try {
    loading.value = true
    const response = await axios.post('/api/user/login', {
      email: 'guest@guest.com',
      loginType: 0,
      password: '123456'
    })
    
    if (response.data.code === 1) {
      localStorage.setItem('token', response.data.data.token)
      ElMessage({
        type: 'success',
        message: '访客登录成功！',
        duration: 2000
      })
      router.push('/home')
    } else {
      ElMessage.error(response.data.msg || '访客登录失败')
    }
  } catch (error) {
    if (error.response) {
      ElMessage.error(error.response?.data?.msg || '访客登录失败')
    } else {
      ElMessage.error('访客登录失败，请稍后重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, rgba(250, 252, 255, 0.87) 5%, rgba(202, 225, 243, 0.8) 100%);

  .login-box {
    width: 400px;
    padding: 40px;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    h2 {
      text-align: center;
      margin-bottom: 30px;
      color: #409EFF;
    }

    .code-input {
      display: flex;
      gap: 10px;

      .el-input {
        flex: 1;
      }

      .el-button {
        width: 120px;
      }
    }

    .login-button {
      width: 100%;
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      margin-top: 20px;
    }

    .guest-login {
      margin-top: 20px;
      text-align: center;

      .guest-button {
        width: 100%;
      }
    }
  }
}
</style> 