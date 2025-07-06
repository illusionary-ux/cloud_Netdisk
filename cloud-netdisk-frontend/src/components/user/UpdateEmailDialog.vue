<template>
  <el-dialog
    v-model="dialogVisible"
    title="更新邮箱"
    width="400px"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="当前邮箱" prop="email">
        <el-input v-model="form.email" disabled />
      </el-form-item>
      <el-form-item label="新邮箱" prop="newEmail">
        <el-input v-model="form.newEmail" placeholder="请输入新邮箱" />
      </el-form-item>
      <el-form-item label="验证码" prop="verificationCode">
        <div class="code-input">
          <el-input v-model="form.verificationCode" placeholder="请输入验证码" />
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
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { watch } from 'vue'

const userStore = useUserStore()
const formRef = ref(null)
const countdown = ref(0)

const dialogVisible = computed({
  get: () => userStore.updateEmailDialogVisible,
  set: (value) => {
    if (!value) {
      userStore.updateEmailDialogVisible = false
    }
  }
})
watch(dialogVisible, (val) => {
  if (val) {
    form.value.email = userStore.email // 弹窗打开时同步
  }
})
const form = ref({
  email: userStore.email,
  newEmail: '',
  verificationCode: ''
})

const rules = {
  newEmail: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
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
    await formRef.value.validateField('newEmail')
    await userStore.sendEmailCode(form.value.newEmail)
    startCountdown()
  } catch (error) {
    // 错误已在 store 中处理
  }
}

const handleCancel = () => {
  dialogVisible.value = false
  form.value.newEmail = ''
  form.value.verificationCode = ''
}

const handleConfirm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    await userStore.updateEmail(
      form.value.newEmail,
      form.value.verificationCode
    )
    handleCancel()
  } catch (error) {
    // 错误已在 store 中处理
  }
}
</script>

<style lang="scss" scoped>
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
</style> 