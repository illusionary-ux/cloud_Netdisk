<template>
  <el-dialog
    v-model="dialogVisible"
    title="新建文件夹"
    width="500px"
  >
    <el-form :model="form" :rules="rules" ref="formRef" label-width="82px" label-position="left" class="folder-dialog-form">
      <div class="custom-form-row">
        <span class="custom-label">文件夹名称</span>
        <el-input v-model="form.folderName" placeholder="请输入文件夹名称" style="width:300px; margin-left: 16px;" />
      </div>
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
import { useFolderStore } from '@/stores/folder'
import { ElMessage } from 'element-plus'

const props = defineProps({
  parentFolderId: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['success'])

const folderStore = useFolderStore()
const formRef = ref(null)

const dialogVisible = computed({
  get: () => folderStore.newFolderDialogVisible,
  set: (value) => {
    if (!value) {
      folderStore.newFolderDialogVisible = false
    }
  }
})

const form = ref({
  folderName: ''
})

const rules = {
  folderName: [
    { required: true, message: '请输入文件夹名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
}

const handleCancel = () => {
  dialogVisible.value = false
  form.value.folderName = ''
}

const handleConfirm = async () => {
  if (!form.value.folderName || !form.value.folderName.trim()) {
    ElMessage.error('文件夹名称不能为空')
    return
  }
  try {
    await folderStore.createFolder(form.value.folderName, props.parentFolderId)
    emit('success')
    handleCancel()
  } catch (error) {
    ElMessage.error('创建失败：' + (error.message || '请检查输入'))
  }
}
</script>

<style scoped>
.folder-dialog-form {
  .el-form-item {
    display: flex;
    align-items: center;
    margin-bottom: 18px;
  }
  .el-form-item__label {
    min-width: 80px;
    text-align: left;
    padding-right: 8px;
    white-space: nowrap;
  }
  .el-form-item__content {
    flex: 1;
  }
}

.custom-form-row {
  display: flex;
  align-items: center;
  margin-bottom: 18px;
}

.custom-label {
  min-width: 80px;
  text-align: left;
  white-space: nowrap;
  font-size: 14px;
  color: #606266;
}
</style> 