<template>
  <el-card class="search-card">
    <el-form :inline="true" :model="queryParams" class="demo-form-inline">
      <el-form-item label="用户名">
        <el-input
            v-model="queryParams.username"
            placeholder="请输入用户名"
            clearable
            prefix-icon="User"
        />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input
            v-model="queryParams.email"
            placeholder="请输入邮箱"
            clearable
            prefix-icon="Message"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleQuery">
          <el-icon><Search /></el-icon>
          查询
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, Refresh, User, Message } from '@element-plus/icons-vue'

const emit = defineEmits(['search', 'reset'])

const queryParams = reactive({
  username: '',
  email: ''
})

const handleQuery = () => {
  emit('search', {
    ...queryParams,
    username: queryParams.username.trim(),
    email: queryParams.email.trim()
  })
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.email = ''
  emit('reset')
}
</script>

<style lang="scss" scoped>
.search-card {
  margin-bottom: 20px;

  .el-form {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style> 