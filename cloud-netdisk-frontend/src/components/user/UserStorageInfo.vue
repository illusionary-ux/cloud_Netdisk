<template>
  <div class="storage-info">
    <div class="storage-text">
      <span>{{ formatStorage(usedStorage) }}</span>
      <span class="percentage">({{ percentage }}%)</span>
    </div>
    <el-progress
        :percentage="percentage"
        :status="storageStatus"
        :stroke-width="15"
        :show-text="false"
        class="storage-progress"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  usedStorage: {
    type: Number,
    required: true
  },
  storageLimit: {
    type: Number,
    required: true
  }
})

const percentage = computed(() => {
  return Math.round((props.usedStorage / props.storageLimit) * 100)
})

const storageStatus = computed(() => {
  if (percentage.value >= 90) return 'exception'
  if (percentage.value >= 70) return 'warning'
  return 'success'
})

const formatStorage = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style lang="scss" scoped>
.storage-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: center;
  padding: 0 10px;

  .storage-text {
    display: flex;
    align-items: center;
    gap: 5px;
    font-size: 13px;
    color: #606266;

    .percentage {
      color: #909399;
    }
  }
}

:deep(.storage-progress) {
  width: 100%;
  margin: 0;

  .el-progress-bar__outer {
    border-radius: 4px;
  }

  .el-progress-bar__inner {
    transition: all 0.3s ease;
  }
}
</style> 