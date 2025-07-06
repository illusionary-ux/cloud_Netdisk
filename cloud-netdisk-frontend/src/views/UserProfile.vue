<template>
  <div class="profile-container">
    <el-header>
      <div class="header-content">
        <div class="logo">
          <el-icon><Folder /></el-icon>
          <span>企业网盘</span>
        </div>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
    </el-header>
    
    <div class="profile-content">
      <div class="profile-card">
        <h2>个人信息</h2>
        
        <div class="info-group">
          <div class="info-item">
            <span class="label">用户名</span>
            <span class="value">{{ userInfo.username }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">邮箱</span>
            <span class="value">{{ userInfo.email }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">存储空间</span>
            <div class="storage-info">
              <div class="storage-bar">
                <div 
                  class="storage-used" 
                  :style="{ width: storagePercentage + '%' }"
                  :class="{ 'warning': storagePercentage > 80 }"
                ></div>
              </div>
              <span class="storage-text">
                已使用 {{ formatStorage(userInfo.usedStorage) }} GB / {{ formatStorage(userInfo.storageLimit) }} GB
              </span>
            </div>
          </div>
          
          <div class="info-item vip-section">
            <span class="label">会员状态</span>
            <div class="vip-info">
              <span class="value" :class="{ 'vip-active': isVip }">
                {{ isVip ? 'VIP会员' : '普通用户' }}
              </span>
              <el-button 
                v-if="!isVip"
                type="primary" 
                class="upgrade-btn"
                @click="showUpgradeDialog"
              >
                升级VIP
              </el-button>
            </div>
          </div>
          
          <div class="info-item">
            <span class="label">注册时间</span>
            <span class="value">{{ formatDate(userInfo.createTime) }}</span>
          </div>
          
          <div class="info-item">
            <span class="label">最后登录</span>
            <span class="value">{{ formatDate(userInfo.lastLogin) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- VIP升级对话框 -->
    <el-dialog
      v-model="upgradeDialogVisible"
      title="升级VIP会员"
      width="400px"
      center
    >
      <div class="upgrade-content">
        <h3>VIP会员特权</h3>
        <ul class="vip-benefits">
          <li>存储空间提升至 10GB</li>
          <li>支持大文件上传</li>
          <li>文件分享无限制</li>
          <li>优先客服支持</li>
        </ul>
        <div class="price-section">
          <span class="price">￥99.9</span>
          <el-button type="primary" @click="handleUpgrade">立即升级</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { Folder } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '../utils/request'

export default {
  name: 'UserProfile',
  components: {
    Folder
  },
  setup() {
    const userInfo = ref({
      username: '',
      email: '',
      storageLimit: 0,
      usedStorage: 0,
      createTime: null,
      lastLogin: null
    })

    const router = useRouter()

    const upgradeDialogVisible = ref(false)

    const goHome = () => {
      router.push('/folders/0')
    }

    const formatDate = (dateString) => {
      if (!dateString) return '暂无记录'
      try {
        const date = new Date(dateString)
        if (isNaN(date.getTime())) {
          return '暂无记录'
        }
        return new Intl.DateTimeFormat('zh-CN', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit',
          hour: '2-digit',
          minute: '2-digit',
          hour12: false
        }).format(date)
      } catch (error) {
        console.error('日期格式化错误:', error)
        return '暂无记录'
      }
    }

    const formatStorage = (bytes) => {
      if (typeof bytes !== 'number' || bytes < 0) return '0.00'
      const gb = bytes / (1024 * 1024 * 1024)
      return Number(gb.toFixed(2))
    }

    const storagePercentage = computed(() => {
      const limit = userInfo.value.storageLimit
      const used = userInfo.value.usedStorage
      if (!limit || !used || typeof limit !== 'number' || typeof used !== 'number') return 0
      return Math.min((used / limit) * 100, 100)
    })

    const isVip = computed(() => {
      return userInfo.value.storageLimit >= 10 * 1024 * 1024 * 1024 // 10GB
    })

    const showUpgradeDialog = () => {
      upgradeDialogVisible.value = true
    }

    const handleUpgrade = async () => {
      try {
        const res = await request.post('/user/upgrade-vip')
        console.log('升级响应数据:', res)
        console.log('响应状态码:', res.code)
        console.log('响应消息:', res.msg)
        console.log('响应数据:', res.data)
        
        if (res.code === 1) {
          ElMessage.success('升级成功！')
          upgradeDialogVisible.value = false
          await fetchUserInfo()
        } else {
          ElMessage.error(res.data.msg || '升级失败')
        }
      } catch (error) {
        console.error('升级失败，完整错误信息:', error)
        console.error('错误响应:', error.response)
        ElMessage.error('升级失败，请稍后重试')
      }
    }

    const fetchUserInfo = async () => {
      try {
        const res = await request.get('/user/profile')
        if (res.data) {
          const data = res.data
          console.log('获取到的用户数据:', data)
          userInfo.value = {
            userId: data.userId,
            username: data.username || '未设置用户名',
            email: data.email || '未设置邮箱',
            storageLimit: Number(data.storageLimit) || 1073741824, // 默认1GB
            usedStorage: Number(data.usedStorage) || 0,
            createTime: data.createTime,
            lastLogin: data.lastLogin
          }
        }
      } catch (error) {
        console.error('获取用户信息失败:', error)
        ElMessage.error('获取用户信息失败，请稍后重试')
      }
    }

    onMounted(() => {
      fetchUserInfo()
    })

    return {
      userInfo,
      upgradeDialogVisible,
      formatDate,
      formatStorage,
      storagePercentage,
      isVip,
      showUpgradeDialog,
      handleUpgrade,
      goHome
    }
  }
}
</script>

<style scoped>
.profile-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, rgba(250, 252, 255, 0.87) 5%, rgba(202, 225, 243, 0.8) 100%);
}

.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  color: #409EFF;
  cursor: pointer;
}

.logo .el-icon {
  font-size: 24px;
}

.profile-content {
  flex: 1;
  padding: 2rem;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.profile-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  padding: 2rem;
}

h2 {
  color: #2c3e50;
  margin-bottom: 2.5rem;
  text-align: center;
  font-size: 1.8rem;
  font-weight: 600;
}

.info-group {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.info-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.label {
  color: #606266;
  font-size: 0.9rem;
  font-weight: 500;
}

.value {
  color: #2c3e50;
  font-size: 1.1rem;
  font-weight: 500;
}

.storage-info {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.storage-bar {
  width: 100%;
  height: 8px;
  background: #e6e6e6;
  border-radius: 4px;
  overflow: hidden;
}

.storage-used {
  height: 100%;
  background: linear-gradient(90deg, #409eff, #36d1dc);
  transition: width 0.3s ease;
  border-radius: 4px;
}

.storage-used.warning {
  background: linear-gradient(90deg, #f56c6c, #ff9a9e);
}

.storage-text {
  font-size: 0.9rem;
  color: #606266;
  text-align: right;
}

.vip-section {
  grid-column: 1 / -1;
}

.vip-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.vip-active {
  color: #f0a020;
  font-weight: bold;
}

.upgrade-btn {
  margin-left: 1rem;
}

.upgrade-content {
  text-align: center;
}

.vip-benefits {
  list-style: none;
  padding: 0;
  margin: 1.5rem 0;
  text-align: left;
}

.vip-benefits li {
  padding: 0.5rem 0;
  color: #606266;
  position: relative;
  padding-left: 1.5rem;
}

.vip-benefits li::before {
  content: "✓";
  color: #67c23a;
  position: absolute;
  left: 0;
}

.price-section {
  margin-top: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
}

.price {
  font-size: 1.5rem;
  color: #f56c6c;
  font-weight: bold;
}

@media (max-width: 768px) {
  .profile-content {
    padding: 1rem;
  }
  
  .info-group {
    grid-template-columns: 1fr;
  }
  
  .info-item {
    padding: 0.8rem;
  }
  
  .storage-text {
    text-align: left;
  }
}
</style> 