<template>
  <div class="starred-files-content">
    <div class="header">
      <h2>我的收藏</h2>
    </div>

    <el-table
      v-loading="loading"
      :data="fileList"
      style="width: 100%"
    >
      <el-table-column prop="fileName" label="文件名" min-width="200">
        <template #default="{ row }">
          <div class="file-name-cell">
            <el-icon :size="20" class="mr-2">
              <Document v-if="!row.isFolder" />
              <Folder v-else />
            </el-icon>
            <span>{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="fileSize" label="大小" width="120">
        <template #default="{ row }">
          {{ row.isFolder ? '-' : formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      
      <el-table-column prop="updateTime" label="修改时间" width="180">
        <template #default="{ row }">
          {{ formatDateTime(row.uploadTime) }}
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="canPreviewFile(row)"
            type="primary"
            link
            @click="handlePreview(row)"
          >
            预览
          </el-button>
          
          <el-button
            type="primary"
            link
            :disabled="row.isOther"
            @click="handleDownload(row)"
          >
            下载
          </el-button>
          
          <el-button
            type="primary"
            link
            :disabled="row.isOther"
            @click="handleShare(row)"
          >
            分享
          </el-button>
          
          <el-button
            type="danger"
            link
            @click="handleCancelCollect(row)"
          >
            取消收藏
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <FilePreview
      v-if="previewVisible"
      :fileId="currentFileId"
      :visible="previewVisible"
      @close="previewVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder } from '@element-plus/icons-vue'
import { getCollectedFiles, cancelCollect, checkIsOther } from '@/api/fileCollect'
import { useUserStore } from '@/stores/user'
import { formatDateTime } from '@/utils/format'
import { formatFileSize } from '@/utils/format'
import request from '@/utils/request'
import { useRouter } from 'vue-router'
import FilePreview from '@/components/FilePreview.vue'

const userStore = useUserStore()
const loading = ref(false)
const fileList = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const router = useRouter()
const previewVisible = ref(false)
const currentFileId = ref('')

// 加载收藏文件列表
const loadCollectedFiles = async () => {
  loading.value = true
  try {
    console.log('[前端分页调试] 请求参数:', { pageNum: currentPage.value, pageSize: pageSize.value })
    const response = await getCollectedFiles(currentPage.value, pageSize.value)
    console.log('[前端分页调试] 响应数据:', response.data)
    
    // 批量检查每个文件是否是他人文件
    const list = response.data.list
    const userId = userStore.userId
    const checkedList = await Promise.all(list.map(async file => {
      try {
        const res = await checkIsOther(file.fileId, userId)
        return { ...file, isOther: res.data }
      } catch {
        return { ...file, isOther: false }
      }
    }))
    fileList.value = checkedList
    total.value = response.data.total
    console.log('[前端分页调试] 处理后的数据:', { 
      total: total.value, 
      listLength: fileList.value.length,
      currentPage: currentPage.value,
      pageSize: pageSize.value
    })
  } catch (error) {
    console.error('[前端分页调试] 错误:', error)
    ElMessage.error('加载收藏文件失败')
  } finally {
    loading.value = false
  }
}

// 取消收藏
const handleCancelCollect = async (file) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏该文件吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await cancelCollect(file.fileId)
    ElMessage.success('取消收藏成功')
    await loadCollectedFiles()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('取消收藏失败')
    }
  }
}

// 预览文件
const handlePreview = (file) => {
  if (file.isFolder) return
  currentFileId.value = file.fileId
  previewVisible.value = true
}

// 下载文件
const handleDownload = async (file) => {
  console.log('[下载调试] file:', file);
  console.log('[下载调试] 当前用户userId:', userStore.userId);
  console.log('[下载调试] isOther:', file.isOther);
  if (file.isFolder) {
    return;
  }
  if (file.isOther) {
    ElMessage.warning('您没有权限下载该文件');
    return;
  }
  try {
    // 调用后端接口获取下载链接，传递userId
    const res = await request.get('/files/down', {
      params: {
        fileId: file.fileId,
        userId: userStore.userId
      }
    });
    if (!res.data) {
      throw new Error('未获取到下载链接');
    }
    window.open(res.data, '_blank');
    ElMessage.success('开始下载');
  } catch (error) {
    if (error?.message?.includes('无权限')) {
      ElMessage.error('您没有权限下载该文件');
    } else {
      ElMessage.error('下载失败，请稍后重试');
    }
    console.error('下载失败:', error);
  }
};

// 分享文件
const handleShare = (file) => {
  console.log('[分享调试] file:', file);
  console.log('[分享调试] 当前用户userId:', userStore.userId);
  console.log('[分享调试] isOther:', file.isOther);
  if (file.isOther) {
    ElMessage.warning('您没有权限分享该文件');
    return;
  }
  // 跳转到分享页面，传递fileId和fileName
  router.push({
    path: '/share',
    query: {
      fileId: file.fileId,
      fileName: file.fileName
    }
  })
};

// 分页相关方法
const handleSizeChange = (val) => {
  console.log('[前端分页调试] 页面大小改变:', val)
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
  loadCollectedFiles()
}

const handleCurrentChange = (val) => {
  console.log('[前端分页调试] 当前页改变:', val)
  currentPage.value = val
  loadCollectedFiles()
}

// 权限控制函数
const canPreviewFile = (file) => {
  return userStore.canPreviewFile(file)
}

onMounted(() => {
  loadCollectedFiles()
})
</script>

<style lang="scss" scoped>
.starred-files-content {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;

  .header {
    margin-bottom: 20px;
  }

  .file-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style> 