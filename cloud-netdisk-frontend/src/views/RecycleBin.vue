<template>
  <div class="recycle-bin-content">
    <div class="recycle-header-row">
      <h2>回收站</h2>
      <el-button 
        type="primary" 
        @click="handleEmptyRecycleBin"
        v-if="isAdmin"
        style="margin-left: 24px;"
      >
        清空回收站
      </el-button>
    </div>
    <!-- 文件列表 -->
    <el-table
      v-loading="loading"
      :data="recycleBinItems"
      style="width: 100%"
    >
      <el-table-column label="名称" min-width="200">
        <template #default="{ row }">
          <div class="file-name">
            <el-icon :size="20" class="mr-2">
              <component :is="row.itemType === 1 ? 'Document' : 'Folder'" />
            </el-icon>
            <span>{{ row.name }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          {{ row.itemType === 1 ? '文件' : '文件夹' }}
        </template>
      </el-table-column>
      <el-table-column prop="deleteTime" label="删除时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.deleteTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button 
            type="primary" 
            link 
            @click="handleRestore(row)"
            :disabled="!canOperateItem(row)"
          >
            恢复
          </el-button>
          <el-button 
            type="danger" 
            link 
            @click="handleDelete(row)"
            :disabled="!canOperateItem(row)"
          >
            彻底删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <el-empty
      v-if="recycleBinItems.length === 0 && !loading"
      description="回收站是空的"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import { getRecycleBinItems, restoreItem, deleteItem, emptyRecycleBin } from '@/api/recycleBin'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 获取路由实例
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const recycleBinItems = ref([])

// 计算属性
const isAdmin = computed(() => userStore.isAdmin())

// 权限控制
const canOperateItem = (item) => {
  if (isAdmin.value) return true
  // 确保类型一致，将userId转为数字进行比较
  return Number(item.deleteUser) === Number(userStore.userId)
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未知时间'
  try {
    return new Date(date).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  } catch (error) {
    console.error('日期格式化错误:', error)
    return '日期格式错误'
  }
}

// 加载回收站数据
const loadRecycleBinItems = async () => {
  loading.value = true
  try {
    const response = await getRecycleBinItems(userStore.userId)
    if (response.data) {
      // 打印一条数据用于调试
      if (response.data.length > 0) {
        console.log('回收站第一条数据:', response.data[0])
      }
      // 确保数据格式正确
      recycleBinItems.value = response.data.map(item => ({
        ...item,
        itemType: item.itemType || (item.fileId ? 1 : 2), // 1表示文件，2表示文件夹
        name: item.fileName || item.folderName || item.name || '未命名',
        id: item.fileId || item.folderId || item.id
      }))
    } else {
      recycleBinItems.value = []
    }
  } catch (error) {
    console.error('加载回收站数据失败:', error.response || error)
    ElMessage.error(error.response?.data?.msg || '加载回收站数据失败')
    recycleBinItems.value = []
  } finally {
    loading.value = false
  }
}

// 恢复文件
const handleRestore = async (item) => {
  if (!canOperateItem(item)) {
    ElMessage.warning('您只能操作自己删除的文件或文件夹')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确定要恢复${item.itemType === 1 ? '文件' : '文件夹'} "${item.name}" 吗？`,
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await restoreItem(item.id, userStore.userId)
    ElMessage.success('恢复成功')
    // 如果是文件夹，刷新当前文件夹内容
    if (item.itemType === 2 && route.name === 'Folders') {
      await router.push({ 
        name: 'Folders', 
        params: { folderId: route.params.folderId }, 
        force: true 
      })
    }
    await loadRecycleBinItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('恢复失败:', error.response || error)
      ElMessage.error(error.response?.data?.msg || '恢复失败')
    }
  }
}

// 彻底删除
const handleDelete = async (item) => {
  if (!canOperateItem(item)) {
    ElMessage.warning('您只能操作自己删除的文件或文件夹')
    return
  }

  try {
    await ElMessageBox.confirm(
      `此操作将永久删除${item.itemType === 1 ? '文件' : '文件夹'} "${item.name}"，是否继续？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await deleteItem(item.id, userStore.userId)
    ElMessage.success('删除成功')
    await loadRecycleBinItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error.response || error)
      ElMessage.error(error.response?.data?.msg || '删除失败')
    }
  }
}

// 清空回收站
const handleEmptyRecycleBin = async () => {
  if (!isAdmin.value) {
    ElMessage.warning('只有管理员可以清空回收站')
    return
  }

  try {
    await ElMessageBox.confirm(
      '此操作将清空回收站中的所有文件，是否继续？',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await emptyRecycleBin(userStore.userId)
    ElMessage.success('回收站已清空')
    await loadRecycleBinItems()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清空回收站失败:', error.response || error)
      ElMessage.error(error.response?.data?.msg || '清空回收站失败')
    }
  }
}

// 初始化
onMounted(() => {
  loadRecycleBinItems()
})
</script>

<style lang="scss" scoped>
.recycle-bin-content {
  padding: 20px;
  padding-top: 20px;
  //background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.file-name {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: #909399;
  }

  span {
    color: #303133;
  }
}

.recycle-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style> 