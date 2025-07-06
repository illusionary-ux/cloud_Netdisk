import request from '@/utils/request'

// 收藏文件
export function collectFile(fileId) {
  return request({
    url: `/file-collect/collect/${fileId}`,
    method: 'post'
  })
}

// 取消收藏
export function cancelCollect(fileId) {
  return request({
    url: `/file-collect/cancel/${fileId}`,
    method: 'delete'
  })
}

// 获取收藏文件列表
export function getCollectedFiles(pageNum = 1, pageSize = 10) {
  return request({
    url: '/file-collect/list',
    method: 'get',
    params: {
      pageNum,
      pageSize
    }
  })
}

// 检查文件是否已收藏
export function isFileCollected(fileId) {
  return request({
    url: `/file-collect/check/${fileId}`,
    method: 'get'
  })
}

// 检查文件是否是别人上传的
export function checkIsOther(fileId, userId) {
  return request({
    url: `/file-collect/checkisother/${fileId}/${userId}`,
    method: 'get'
  })
} 