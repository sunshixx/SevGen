import React from 'react';
import { Paper, Typography } from '@mui/material';

const MessageBubble = ({ message, isUser }) => {
  const bubbleStyle = {
    maxWidth: '70%',
    marginBottom: 12,
    padding: 12,
    borderRadius: 18,
    alignSelf: isUser ? 'flex-end' : 'flex-start',
    backgroundColor: isUser ? '#3f51b5' : '#f5f5f5',
    color: isUser ? 'white' : 'black',
    wordBreak: 'break-word',
    position: 'relative',
    boxShadow: '0 1px 2px rgba(0,0,0,0.1)'
  };

  const timestampStyle = {
    fontSize: 12,
    opacity: 0.7,
    marginTop: 4,
    textAlign: isUser ? 'right' : 'left'
  };

  // 格式化日期显示
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    const now = new Date();
    const isToday = date.toDateString() === now.toDateString();
    
    if (isToday) {
      return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } else {
      return date.toLocaleDateString([], { month: '2-digit', day: '2-digit' });
    }
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column' }}>
      <Paper elevation={1} style={bubbleStyle}>
        <Typography variant="body1" style={{ marginBottom: 4 }}>
          {message.content}
        </Typography>
        <Typography variant="caption" style={timestampStyle}>
          {formatDate(message.sentAt)}
        </Typography>
      </Paper>
      {/* 如果有音频，这里可以添加音频播放组件 */}
      {message.audioUrl && (
        <div style={{ alignSelf: isUser ? 'flex-end' : 'flex-start', marginTop: 4 }}>
          <audio controls style={{ width: '100%' }}>
            <source src={message.audioUrl} type="audio/mpeg" />
            您的浏览器不支持音频播放。
          </audio>
        </div>
      )}
    </div>
  );
};

export default MessageBubble;