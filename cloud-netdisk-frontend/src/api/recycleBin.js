import request from '@/utils/request'

// 获取回收站列表
export const getRecycleBinItems = (userId) => {
  return request.get('/recycle-bin', {
    params: { userId }
  })
}

// 恢复文件
export const restoreItem = (id, userId) => {
  return request.post(`/recycle-bin/${id}/restore`, null, {
    params: { userId }
  })
}

// 彻底删除文件
export const deleteItem = (id, userId) => {
  return request.delete(`/recycle-bin/${id}`, {
    params: { userId }
  })
}

// 清空回收站
export const emptyRecycleBin = (userId) => {
  return request.delete('/recycle-bin', {
    params: { userId }
  })
} 