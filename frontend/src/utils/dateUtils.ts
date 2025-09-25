/**
 * 日期时间格式化工具
 */

/**
 * 格式化时间为相对时间显示
 * @param dateString ISO日期字符串
 * @returns 相对时间字符串，如 "刚刚", "5分钟前", "昨天 14:30"
 */
export function formatRelativeTime(dateString: string | undefined): string {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffMinutes = Math.floor(diffMs / (1000 * 60))
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60))
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  // 小于1分钟
  if (diffMinutes < 1) {
    return '刚刚'
  }
  
  // 小于1小时
  if (diffMinutes < 60) {
    return `${diffMinutes}分钟前`
  }
  
  // 小于24小时
  if (diffHours < 24) {
    return `${diffHours}小时前`
  }
  
  // 昨天
  if (diffDays === 1) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 本周内（2-6天前）
  if (diffDays < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${weekdays[date.getDay()]} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 本年内
  const currentYear = now.getFullYear()
  const dateYear = date.getFullYear()
  if (currentYear === dateYear) {
    return `${date.getMonth() + 1}月${date.getDate()}日 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 跨年
  return `${dateYear}年${date.getMonth() + 1}月${date.getDate()}日`
}

/**
 * 格式化消息时间
 * @param dateString ISO日期字符串
 * @returns 格式化的时间字符串，如 "14:30", "昨天 14:30"
 */
export function formatMessageTime(dateString: string | undefined): string {
  if (!dateString) return ''
  
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))
  
  // 今天的消息只显示时间
  if (diffDays === 0) {
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 昨天的消息
  if (diffDays === 1) {
    return `昨天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 本周内
  if (diffDays < 7) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
    return `${weekdays[date.getDay()]} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 本年内
  const currentYear = now.getFullYear()
  const dateYear = date.getFullYear()
  if (currentYear === dateYear) {
    return `${date.getMonth() + 1}/${date.getDate()} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
  }
  
  // 跨年
  return `${dateYear}/${date.getMonth() + 1}/${date.getDate()}`
}

/**
 * 检查两个消息的时间是否需要显示时间分隔符
 * @param prevDateString 前一条消息时间
 * @param currentDateString 当前消息时间
 * @returns 是否需要显示时间分隔符
 */
export function shouldShowTimeLabel(prevDateString: string | undefined, currentDateString: string | undefined): boolean {
  if (!prevDateString || !currentDateString) return true
  
  const prevDate = new Date(prevDateString)
  const currentDate = new Date(currentDateString)
  const diffMinutes = Math.floor((currentDate.getTime() - prevDate.getTime()) / (1000 * 60))
  
  // 超过5分钟间隔显示时间标签
  return diffMinutes > 5
}