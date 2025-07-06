// 格式化日期时间
export const formatDateTime = (val) => {
  if (!val) return '暂无记录'
  // 支持字符串
  if (typeof val === 'string') {
    try {
      const date = new Date(val)
      if (isNaN(date.getTime())) return '暂无记录'
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).format(date)
    } catch {
      return '暂无记录'
    }
  }
  // 支持数组
  if (Array.isArray(val) && val.length === 6) {
    try {
      const [year, month, day, hour, minute, second] = val
      const date = new Date(year, month - 1, day, hour, minute, second)
      if (isNaN(date.getTime())) return '暂无记录'
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false
      }).format(date)
    } catch {
      return '暂无记录'
    }
  }
  return '暂无记录'
}

// 格式化存储大小
export const formatStorage = (bytes) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化文件大小
export const formatFileSize = (bytes) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
} 