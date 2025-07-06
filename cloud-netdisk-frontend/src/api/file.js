import request from '@/utils/request'

// 获取文件列表
export function getFileList(folderId) {
  return request({
    url: '/files/files',
    method: 'get',
    params: { folderId }
  })
}

// 上传文件
export function uploadFile(data) {
  return request({
    url: '/files/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export function deleteFile(fileId, userId) {
  return request({
    url: `/files/${fileId}`,
    method: 'delete',
    params: { userId }
  })
}

// 重命名文件
export function renameFile(fileId, newName, userId) {
  return request({
    url: `/files/${fileId}/rename`,
    method: 'put',
    params: { newName, userId }
  })
}

// 获取文件预览信息
export function getPreviewInfo(fileId) {
  return request({
    url: `/files/${fileId}/preview-info`,
    method: 'get'
  })
} 