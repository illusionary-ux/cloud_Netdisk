<template>
  <div class="register-container">
    <div class="register-box">
      <h2>注册账号</h2>
      <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="邮箱"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="verificationCode">
          <div class="code-input">
            <el-input 
              v-model="registerForm.verificationCode" 
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
            class="register-button" 
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>

        <div class="register-options">
          <el-link type="primary" @click="$router.push('/login')">
            返回登录
          </el-link>
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
const registerFormRef = ref(null)
const loading = ref(false)
const countdown = ref(0)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  verificationCode: ''
})

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else {
    if (registerForm.confirmPassword !== '') {
      if (value !== registerForm.confirmPassword) {
        callback(new Error('两次输入密码不一致'))
      }
    }
    callback()
  }
}

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
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
    await registerFormRef.value.validateField('email')
    await axios.post('/api/user/send-code', null, {
      params: {
        email: registerForm.email
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

const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    const response = await axios.post('/api/user/register', {
      username: registerForm.username,
      email: registerForm.email,
      passwordHash: registerForm.password,
      verificationCode: registerForm.verificationCode
    })
    
    if (response.data.code === 1) {
      ElMessage({
        type: 'success',
        message: '注册成功！即将跳转到登录页面...',
        duration: 2000
      })
      setTimeout(() => {
        router.push('/login')
      }, 2000)
    } else {
      ElMessage.error(response.data.msg || '注册失败')
    }
  } catch (error) {
    if (error.response) {
      const errorMsg = error.response?.data?.msg || '';
      if (errorMsg.includes('Duplicate entry') && errorMsg.includes('email')) {
        ElMessage.error('该邮箱已被注册');
      } else if (errorMsg.includes('UsernameAlreadyExist')) {
        ElMessage.error('该用户名已被使用');
      } else {
        ElMessage.error('注册失败，请稍后重试');
      }
    } else {
      ElMessage.error('注册失败，请稍后重试');
    }
  } finally {
    loading.value = false;
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, rgba(250, 252, 255, 0.87) 5%, rgba(202, 225, 243, 0.8) 100%);

  .register-box {
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

    .register-button {
      width: 100%;
    }

    .register-options {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }

    .w-100 {
      width: 100%;
    }
  }
}
</style> 