export const useDateTime = () => {
  const formatDateTime = (dateString) => {
    if (!dateString) return '暂无记录';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) {
        return '暂无记录';
      }
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
        timeZone : 'Asia/Shanghai'
      }).format(date);
    } catch (error) {
      console.error('日期格式化错误:', error);
      return '暂无记录';
    }
  };

  return {
    formatDateTime
  };
}; 