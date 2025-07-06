<template>
  <div class="share-management-content">
    <div class="header">
      <h2>我的分享</h2>
    </div>
    <el-table :data="shareList" v-loading="loading" style="width: 100%">
      <el-table-column prop="filename" label="文件名" />
      <el-table-column label="分享链接">
        <template #default="{ row }">
          <el-input :model-value="getShareUrl(row.urlid)" readonly style="width: 320px;">
            <template #append>
              <el-button @click="copyShareUrl(row.urlid)">复制</el-button>
            </template>
          </el-input>
        </template>
      </el-table-column>
      <el-table-column label="提取码" width="120">
        <template #default="{ row }">
          <span>{{ row.password || '' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="生成时间" width="180">
        <template #default="{ row }">
          {{ formatCreateTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column prop="shareCount" label="分享次数" />
      <el-table-column prop="isExpired" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.isExpired ? 'danger' : 'success'">
            {{ row.isExpired ? '已失效' : '有效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const shareList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadShareList = async () => {
  loading.value = true
  try {
    console.log('[前端分享分页调试] 请求参数:', { 
      userId: userStore.userId, 
      pageNum: currentPage.value, 
      pageSize: pageSize.value 
    })
    
    const res = await request.get('/fileshare/listFileShares', {
      params: { 
        userId: userStore.userId, 
        pageNum: currentPage.value, 
        pageSize: pageSize.value 
      }
    })
    
    console.log('[前端分享分页调试] 响应数据:', res.data)
    
    if (res.code === 1 && res.data) {
      shareList.value = res.data.list || res.data
      total.value = res.data.total || res.data.length || 0
      
      console.log('[前端分享分页调试] 处理后的数据:', { 
        total: total.value, 
        listLength: shareList.value.length,
        currentPage: currentPage.value,
        pageSize: pageSize.value
      })
      
      // 检查每个分享链接的过期状态
      for (const share of shareList.value) {
        await getRemainingTime(share)
      }
    } else {
      ElMessage.error(res.msg || '获取分享列表失败')
    }
  } catch (e) {
    console.error('[前端分享分页调试] 错误:', e)
    ElMessage.error('获取分享列表失败')
  } finally {
    loading.value = false
  }
}

const formatCreateTime = (createTime) => {
  if (!createTime) return '未知'
  
  try {
    // 如果是数组格式 [year, month, day, hour, minute, second]
    if (Array.isArray(createTime)) {
      const [year, month, day, hour, minute] = createTime
      // 月份需要补零，因为数组中的月份是从0开始的
      const monthStr = String(month + 1).padStart(2, '0')
      const dayStr = String(day).padStart(2, '0')
      const hourStr = String(hour).padStart(2, '0')
      const minuteStr = String(minute).padStart(2, '0')
      
      return `${year}-${monthStr}-${dayStr} ${hourStr}:${minuteStr}`
    }
    
    // 如果是字符串格式，直接返回
    if (typeof createTime === 'string') {
      return createTime
    }
    
    // 其他格式，尝试转换为Date对象
    const date = new Date(createTime)
    if (!isNaN(date.getTime())) {
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false
      })
    }
    
    return '时间格式错误'
  } catch (error) {
    console.error('格式化时间失败:', error, '原始数据:', createTime)
    return '时间格式错误'
  }
}

const getRemainingTime = async (row) => {
  if (!row.createTime) return '未知'
  
  try {
    const createDate = new Date(row.createTime)
    const now = new Date()
    
    // 假设分享链接有效期为7天
    const validDuration = 7 * 24 * 60 * 60 * 1000 // 7天的毫秒数
    const expireTime = new Date(createDate.getTime() + validDuration)
    
    // 如果已过期且未标记为失效，自动调用标记失效接口
    if (now >= expireTime && !row.isExpired) {
      try {
        await request.put(`/fileshare/markExpired/${row.urlid}`)
        row.isExpired = true
        console.log(`分享链接 ${row.urlid} 已自动标记为失效`)
      } catch (error) {
        console.error('自动标记失效失败:', error)
      }
      return '已过期'
    }
    
    if (row.isExpired) {
      return '已失效'
    }
    
    const remainingMs = expireTime.getTime() - now.getTime()
    const remainingMinutes = Math.floor(remainingMs / (1000 * 60))
    
    if (remainingMinutes < 60) {
      return `${remainingMinutes}分钟`
    } else if (remainingMinutes < 24 * 60) {
      const hours = Math.floor(remainingMinutes / 60)
      const minutes = remainingMinutes % 60
      return `${hours}小时${minutes}分钟`
    } else {
      const days = Math.floor(remainingMinutes / (24 * 60))
      const hours = Math.floor((remainingMinutes % (24 * 60)) / 60)
      const minutes = remainingMinutes % 60
      return `${days}天${hours}小时${minutes}分钟`
    }
  } catch (error) {
    console.error('计算剩余时间失败:', error)
    return '计算错误'
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该分享链接吗？此操作不可恢复！', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  try {
    const res = await request.delete(`/fileshare/delete/${row.urlid}`)
    if (res.code === 1) {
      ElMessage.success('删除成功')
      loadShareList()
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

const getShareUrl = (urlid) => `http://localhost:8080/fileshare/acceptShare?urlid=${urlid}`
const copyShareUrl = (urlid) => {
  const url = getShareUrl(urlid)
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('分享链接已复制')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

const handleSizeChange = (val) => {
  console.log('[前端分享分页调试] 页面大小改变:', val)
  pageSize.value = val
  currentPage.value = 1 // 重置到第一页
  loadShareList()
}

const handleCurrentChange = (val) => {
  console.log('[前端分享分页调试] 当前页改变:', val)
  currentPage.value = val
  loadShareList()
}

onMounted(() => {
  loadShareList()
})
</script>

<style scoped>
.share-management-content {
  padding: 20px;
}
.header {
  margin-bottom: 20px;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 