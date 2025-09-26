import { marked } from 'marked';
import DOMPurify from 'dompurify';
import hljs from 'highlight.js';
import 'highlight.js/styles/github-dark.css'; // 使用暗色主题，适合聊天界面

// 配置marked使用highlight.js进行代码高亮
marked.setOptions({
  highlight: function(code, lang) {
    // 如果指定了语言且highlight.js支持该语言，则使用指定语言高亮
    if (lang && hljs.getLanguage(lang)) {
      try {
        return hljs.highlight(code, { language: lang }).value;
      } catch (err) {
        console.warn(`代码高亮失败: ${err}`);
      }
    }
    
    // 否则尝试自动检测语言
    try {
      return hljs.highlightAuto(code).value;
    } catch (err) {
      console.warn(`自动检测语言高亮失败: ${err}`);
    }
    
    // 如果所有方法都失败，则返回原始代码
    return code;
  },
  breaks: true, // 将换行符转换为<br>
  gfm: true,    // 启用GitHub风格的Markdown
});

/**
 * 将Markdown文本转换为安全的HTML，支持代码高亮
 * @param markdown Markdown格式的文本
 * @returns 安全的HTML字符串
 */
export const renderMarkdown = (markdown: string): string => {
  if (!markdown || typeof markdown !== 'string') {
    return '';
  }
  
  // 解析Markdown为HTML
  const html = marked.parse(markdown);
  
  // 净化HTML以防止XSS攻击
  const sanitizedHtml = DOMPurify.sanitize(html);
  
  return sanitizedHtml;
};

/**
 * 检查文本是否包含Markdown语法
 * @param text 要检查的文本
 * @returns 是否包含Markdown语法
 */
export const containsMarkdown = (text: string): boolean => {
  if (!text || typeof text !== 'string') {
    return false;
  }
  
  // 简单检查常见的Markdown语法
  const markdownPatterns = [
    /[#*_`~>\-=+\[\](){}]/, // 常见的Markdown特殊字符
    /^\s*(\d+\.|-|\*)\s+/m, // 列表项
    /\[.+\]\(.+\)/, // 链接
    /`{1,3}[^`]+`{1,3}/, // 代码块或内联代码
    /\*\*[^*]+\*\*/, // 粗体
    /\*[^*]+\*/ // 斜体
  ];
  
  return markdownPatterns.some(pattern => pattern.test(text));
};