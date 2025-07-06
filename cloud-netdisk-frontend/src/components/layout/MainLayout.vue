<template>
  <div class="main-layout">
    <app-header />
    <el-container>
      <el-aside width="200px">
        <el-menu
            :default-active="$route.path"
            class="el-menu-vertical"
            :router="true"
        >
          <el-menu-item index="/home">
            <el-icon><Document /></el-icon>
            <span>全部文件</span>
          </el-menu-item>
          <el-menu-item index="/starred" @click="handleMenuClick('收藏夹', '/starred')">
            <el-icon><Star /></el-icon>
            <span>我的收藏</span>
          </el-menu-item>
          <el-menu-item index="/recycle" @click="handleMenuClick('回收站', '/recycle')">
            <el-icon><Delete /></el-icon>
            <span>回收站</span>
          </el-menu-item>
          <el-menu-item v-if="isAdmin" index="/user/management">
            <el-icon><UserFilled /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/share-management" @click="handleMenuClick('分享管理', '/share-management')">
            <el-icon><Share /></el-icon>
            <span>分享管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import AppHeader from './Header.vue'
import { Document, Star, Delete, UserFilled, Share } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { computed } from 'vue'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const isAdmin = computed(() => userStore.isAdmin())

const handleMenuClick = (feature, path) => {
  if (userStore.isGuest()) {
    ElMessage.warning(userStore.getNoPermissionMessage(`访问${feature}`))
    router.push('/home')
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.el-aside {
  background: #689cc8;
  border-radius: 16px;
  margin-top: 10px;
  margin-bottom: 10px;
  height: calc(100vh - 80px);
  min-height: 0;
  box-shadow: 2px 0 8px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
}

.el-menu-vertical {
  background: transparent;
  border-right: none;
  color: #fff;
}

.el-menu-vertical .el-menu-item {
  color: #fff;
  background: transparent;
  border-radius: 8px;
  margin: 8px 12px;
  transition: background 0.2s;
}

.el-menu-vertical .el-menu-item.is-active,
.el-menu-vertical .el-menu-item:hover {
  background: #346695 !important;
  color: #fff !important;
}

.el-menu-vertical .el-menu-item .el-icon {
  color: #fff;
}
</style> 