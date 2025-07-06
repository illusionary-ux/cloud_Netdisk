<template>
  <div class="user-management-content">
    <div class="page-header">
      <h2>用户管理</h2>
      <p class="description">管理系统中的所有用户账号</p>
    </div>
    <UserSearchForm @search="handleSearch" @reset="handleReset" />
    <el-card class="table-card">
      <el-table
          v-loading="loading"
          :data="userList"
          border
          stripe
          style="width: 100%"
          :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="userId" label="用户ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="120" align="center">
          <template #default="scope">
            <el-tag>{{ scope.row.username }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" align="center" />
        <el-table-column prop="storageLimit" label="存储限制" width="120" align="center">
          <template #default="scope">
            <el-tag type="success">{{ formatStorage(scope.row.storageLimit) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="usedStorage" label="已用存储" width="180" align="center">
          <template #default="scope">
            <UserStorageInfo
              :used-storage="scope.row.usedStorage"
              :storage-limit="scope.row.storageLimit"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.lastLogin) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button
                type="danger"
                size="small"
                @click="handleDelete(scope.row)"
                :icon="Delete"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-container">
        <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { formatDateTime, formatStorage } from '@/utils/format'
import UserSearchForm from '@/components/user/UserSearchForm.vue'
import UserStorageInfo from '@/components/user/UserStorageInfo.vue'

const loading = ref(false)
const userList = ref([])
const total = ref(0)

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  email: ''
})

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const response = await request.get('/user/list', {
      params: {
        pageNum: queryParams.pageNum,
        pageSize: queryParams.pageSize,
        username: queryParams.username.trim(),
        email: queryParams.email.trim()
      }
    })
    console.log('用户列表响应数据:', response)
    if (response && response.code === 1 && response.data) {
      userList.value = response.data.list || []
      total.value = response.data.total || 0
      console.log('处理后的用户列表:', userList.value)
      console.log('总记录数:', total.value)
    } else {
      console.error('响应数据格式不正确:', response)
      ElMessage.error(response?.msg || '获取用户列表失败')
    }
  } catch (error) {
    console.error('获取用户列表错误:', error)
    if (error.response) {
      console.error('错误响应数据:', error.response.data)
      ElMessage.error(`获取用户列表失败：${error.response.data?.msg || '服务器错误'}`)
    } else if (error.request) {
      ElMessage.error('获取用户列表失败：网络请求失败')
    } else {
      ElMessage.error('获取用户列表失败：' + error.message)
    }
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = (params) => {
  queryParams.username = params.username
  queryParams.email = params.email
  queryParams.pageNum = 1
  getList()
}

// 重置处理
const handleReset = () => {
  queryParams.username = ''
  queryParams.email = ''
  queryParams.pageNum = 1
  getList()
}

// 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确认删除用户 ${row.username} 吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      const response = await request.delete(`/user/${row.userId}`)
      if (response.code === 1) {
        ElMessage.success('删除成功')
        getList()
      } else {
        ElMessage.error(response.msg || '删除失败')
      }
    } catch (error) {
      console.error('删除用户错误:', error)
      if (error.response) {
        ElMessage.error(`删除失败：${error.response.data?.msg || '服务器错误'}`)
      } else if (error.request) {
        ElMessage.error('删除失败：网络请求失败')
      } else {
        ElMessage.error('删除失败：' + error.message)
      }
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

// 分页大小改变
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getList()
}

// 页码改变
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.user-management-content {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

  .page-header {
    margin-bottom: 20px;

    h2 {
      font-size: 24px;
      color: #303133;
      margin: 0;
      margin-bottom: 8px;
    }

    .description {
      font-size: 14px;
      color: #909399;
      margin: 0;
    }
  }

  .table-card {
    .el-table {
      margin: 10px 0;
    }
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}

:deep(.el-card) {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

:deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.el-button) {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
</style>