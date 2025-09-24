import React from 'react';
import { Card, CardContent, Typography, Button, Box, Avatar } from '@mui/material';
import { Message } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

const RoleCard = ({ role }) => {
  const navigate = useNavigate();

  const handleChatWithRole = async () => {
    try {
      // 这里可以添加创建聊天会话的逻辑
      // 或者直接跳转到聊天页面，让聊天页面处理创建逻辑
      navigate(`/chat/${role.id}`, { state: { role } });
    } catch (error) {
      console.error('Failed to start chat with role:', error);
    }
  };

  const handleViewDetails = () => {
    navigate(`/role/${role.id}`);
  };

  // 生成角色头像（如果没有头像URL，则使用角色名称的首字母）
  const getRoleAvatar = () => {
    if (role.avatar) {
      return <Avatar src={role.avatar} alt={role.name} />;
    } else {
      return <Avatar>{role.name.charAt(0).toUpperCase()}</Avatar>;
    }
  };

  // 限制描述文本长度
  const truncateDescription = (text, maxLength = 100) => {
    if (!text) return '暂无描述';
    return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
  };

  return (
    <Card 
      elevation={2} 
      style={{ 
        height: '100%', 
        display: 'flex', 
        flexDirection: 'column' 
      }}
    >
      <CardContent>
        <div style={{ display: 'flex', alignItems: 'center', marginBottom: 16 }}>
          {getRoleAvatar()}
          <div style={{ marginLeft: 16 }}>
            <Typography variant="h6" component="h3" style={{ fontWeight: 'bold' }}>
              {role.name}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              {role.category || '未分类'}
            </Typography>
          </div>
        </div>
        
        <Typography variant="body2" paragraph>
          {truncateDescription(role.description)}
        </Typography>
      </CardContent>
      
      <Box 
        sx={{ 
          p: 1.5, 
          borderTop: 1, 
          borderColor: 'divider',
          display: 'flex',
          justifyContent: 'space-between'
        }}
      >
        <Button 
          variant="outlined" 
          size="small" 
          onClick={handleViewDetails}
          style={{ borderColor: '#3f51b5', color: '#3f51b5' }}
        >
          查看详情
        </Button>
        <Button 
          variant="contained" 
          size="small" 
          onClick={handleChatWithRole}
          style={{ backgroundColor: '#3f51b5' }}
          startIcon={<Message size={16} />}
        >
          开始聊天
        </Button>
      </Box>
    </Card>
  );
};

export default RoleCard;