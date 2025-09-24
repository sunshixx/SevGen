import React, { useState, useEffect, useRef } from 'react';
import { Container, Typography, Box, TextField, Button, Paper, IconButton, Avatar, Divider, CircularProgress, Alert } from '@mui/material';
import { Send, Mic, Image, Description as FileText, MoreVert, ArrowBack, AttachFile as Paperclip, SentimentVerySatisfied as Smile } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import Layout from '../components/Layout';
import MessageBubble from '../components/MessageBubble';
import { mockRoles } from '../mockData';
import UserContext from '../context/UserContext';
import Loading from '../components/Loading';

const ChatPage = () => {
  const { chatId, roleId } = useParams();
  const navigate = useNavigate();
  const [messages, setMessages] = useState([]);
  const [newMessage, setNewMessage] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [chatSession, setChatSession] = useState(null);
  const [roleInfo, setRoleInfo] = useState(null);
  const [isSending, setIsSending] = useState(false);
  const messagesEndRef = useRef(null);
  const userContext = React.useContext(UserContext);

  // 初始化聊天会话和消息
  useEffect(() => {
    const initializeChat = async () => {
      setIsLoading(true);
      try {
        // 直接使用模拟数据，不尝试连接后端
        let mockRoleInfo;
        let mockChatSession;
        
        if (roleId) {
          // 根据roleId获取对应的模拟角色信息
          mockRoleInfo = mockRoles.find(role => role.id === parseInt(roleId)) || {
            id: parseInt(roleId),
            name: getMockRoleName(parseInt(roleId)),
            category: '模拟角色',
            description: '这是一个模拟AI角色，用于展示聊天功能。'
          };
        } else {
          // 默认使用第一个角色或创建默认角色
          mockRoleInfo = mockRoles[0] || {
            id: 1,
            name: 'AI助手',
            category: '智能助手',
            description: '这是一个智能AI助手，能够回答各种问题。'
          };
        }
        
        // 创建模拟聊天会话
        mockChatSession = {
          id: chatId || `mock_${Date.now()}`,
          roleId: mockRoleInfo.id,
          title: `与${mockRoleInfo.name}的对话`,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        setRoleInfo(mockRoleInfo);
        setChatSession(mockChatSession);
        
        // 创建欢迎消息
        const welcomeMessage = {
          id: `welcome_${Date.now()}`,
          chatId: mockChatSession.id,
          role: 'assistant',
          content: `你好！我是${mockRoleInfo.name}。${mockRoleInfo.description}有什么我可以帮助你的吗？`,
          createdAt: new Date().toISOString(),
          isRead: true
        };
        
        setMessages([welcomeMessage]);
        
        // 如果是新创建的会话，重定向到包含新chatId的URL
        if (roleId && !chatId) {
          navigate(`/chat/${mockChatSession.id}`);
        }
      } catch (err) {
        console.error('Failed to initialize chat:', err);
        setError('初始化聊天失败');
        
        // 保底的模拟数据
        const defaultRoleInfo = {
          id: 1,
          name: 'AI助手',
          category: '智能助手',
          description: '这是一个智能AI助手，能够回答各种问题。'
        };
        
        const defaultChatSession = {
          id: 'default_session',
          roleId: 1,
          title: '与AI助手的对话',
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        };
        
        setRoleInfo(defaultRoleInfo);
        setChatSession(defaultChatSession);
        setMessages([{
          id: 'default_welcome',
          chatId: 'default_session',
          role: 'assistant',
          content: '你好！我是AI助手。有什么我可以帮助你的吗？',
          createdAt: new Date().toISOString(),
          isRead: true
        }]);
      } finally {
        setIsLoading(false);
      }
    };

    initializeChat();
  }, [chatId, roleId, navigate]);

  // 获取模拟角色名称
  const getMockRoleName = (roleId) => {
    const roleNames = {
      1: '哈利波特',
      2: '苏格拉底',
      3: '爱因斯坦',
      4: '莎士比亚',
      5: '拿破仑',
      6: '居里夫人',
      7: '福尔摩斯',
      8: '莫扎特'
    };
    return roleNames[roleId] || 'AI助手';
  };

  // 自动滚动到最新消息
  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  // 发送消息
  const handleSendMessage = async () => {
    if (!newMessage.trim() || isSending || !chatSession) return;

    setIsSending(true);
    const userMessage = {
      id: `msg_${Date.now()}`,
      chatId: chatSession.id,
      role: 'user',
      content: newMessage.trim(),
      createdAt: new Date().toISOString(),
      isRead: true
    };

    // 添加用户消息到列表
    setMessages(prevMessages => [...prevMessages, userMessage]);
    setNewMessage('');

    try {
      // 模拟AI回复
      setTimeout(async () => {
        const aiResponse = await generateMockAIResponse(newMessage.trim());
        const aiMessage = {
          id: `msg_${Date.now()}_ai`,
          chatId: chatSession.id,
          role: 'assistant',
          content: aiResponse,
          createdAt: new Date().toISOString(),
          isRead: true
        };
        setMessages(prevMessages => [...prevMessages, aiMessage]);
        setIsSending(false);
      }, 1000);
    } catch (err) {
      console.error('Failed to send message:', err);
      setError('发送消息失败，请稍后重试');
      setIsSending(false);
    }
  };

  // 模拟AI回复生成
  const generateMockAIResponse = async (userMessage) => {
    // 简单的模拟回复逻辑，根据用户输入生成不同的回复
    const lowerMessage = userMessage.toLowerCase();
    const roleName = roleInfo?.name || 'AI助手';
    
    if (lowerMessage.includes('你好') || lowerMessage.includes('hi') || lowerMessage.includes('hello')) {
      return `你好！我是${roleName}。很高兴见到你！有什么我可以帮助你的吗？`;
    } else if (lowerMessage.includes('你是谁') || lowerMessage.includes('介绍自己')) {
      return `我是${roleName}。${roleInfo?.description || '我是一个智能AI助手，能够回答各种问题，与你进行交流。'}`;
    } else if (lowerMessage.includes('帮助') || lowerMessage.includes('如何')) {
      return `我可以帮助你解答问题、提供建议、进行对话交流。你可以问我任何问题，我会尽力回答。`;
    } else if (lowerMessage.includes('再见') || lowerMessage.includes('拜拜')) {
      return `再见！有需要的时候随时来找我聊天。`;
    } else {
      // 通用回复
      const responses = [
        `关于这个问题，我认为${Math.random() > 0.5 ? '应该从多个角度考虑' : '有几种可能的解决方案'}。`,
        `你提出了一个很有趣的观点。${Math.random() > 0.5 ? '让我们深入探讨一下' : '我想补充一些内容'}。`,
        `根据我的理解，${Math.random() > 0.5 ? '这个问题涉及到几个关键点' : '我们可以从以下几个方面分析'}。`,
        `${userMessage}确实是一个值得思考的问题。${Math.random() > 0.5 ? '我的看法是' : '我想分享一些见解'}。`,
        `谢谢你的提问！${Math.random() > 0.5 ? '我很高兴能够帮助你' : '这是一个很好的问题'}`
      ];
      return responses[Math.floor(Math.random() * responses.length)];
    }
  };

  // 处理回车键发送消息
  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  // 返回上一页
  const handleGoBack = () => {
    navigate('/');
  };

  return (
    <Layout title={chatSession?.title || '聊天'}>
      <Container maxWidth="lg" sx={{ height: 'calc(100vh - 120px)', display: 'flex', flexDirection: 'column' }}>
        {/* 聊天头部 */}
        <Paper 
          elevation={0} 
          sx={{ 
            p: 2, 
            display: 'flex', 
            alignItems: 'center', 
            gap: 2, 
            borderBottom: '1px solid #eee',
            position: 'sticky',
            top: 0,
            backgroundColor: 'white',
            zIndex: 10
          }}
        >
          <IconButton onClick={handleGoBack}>
            <ArrowBack size={20} />
          </IconButton>
          
          <Avatar sx={{ width: 48, height: 48, bgcolor: '#3f51b5' }}>
            {roleInfo?.name?.charAt(0).toUpperCase()}
          </Avatar>
          
          <div>
            <Typography variant="h6" fontWeight="bold">
              {roleInfo?.name || 'AI助手'}
            </Typography>
            <Typography variant="body2" color="textSecondary">
              在线 • {roleInfo?.category || '智能助手'}
            </Typography>
          </div>
          
          <div style={{ marginLeft: 'auto' }}>
            <IconButton>
              <MoreVert size={20} />
            </IconButton>
          </div>
        </Paper>

        {/* 聊天内容区域 */}
        {isLoading ? (
          <Loading message="正在加载聊天内容..." />
        ) : error ? (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        ) : (
          <Box 
            sx={{ 
              flex: 1, 
              overflowY: 'auto', 
              p: 3, 
              display: 'flex', 
              flexDirection: 'column',
              gap: 2
            }}
          >
            {messages.map((message) => (
              <MessageBubble key={message.id} message={message} />
            ))}
            {isSending && (
              <div style={{ alignSelf: 'flex-start' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <Avatar sx={{ width: 32, height: 32, bgcolor: '#3f51b5' }}>
                    {roleInfo?.name?.charAt(0).toUpperCase()}
                  </Avatar>
                  <div style={{ padding: '8px 16px', backgroundColor: '#f0f0f0', borderRadius: '18px 18px 18px 0' }}>
                    <CircularProgress size={16} sx={{ color: '#3f51b5' }} />
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </Box>
        )}

        {/* 消息输入区域 */}
        <Paper 
          elevation={0} 
          sx={{ 
            p: 2, 
            borderTop: '1px solid #eee',
            position: 'sticky',
            bottom: 0,
            backgroundColor: 'white'
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'flex-end', gap: 1 }}>
            <IconButton>
              <Paperclip size={20} color="primary" />
            </IconButton>
            <IconButton>
              <Image size={20} color="primary" />
            </IconButton>
            <IconButton>
              <FileText size={20} color="primary" />
            </IconButton>
            <IconButton>
              <Smile size={20} color="primary" />
            </IconButton>
            
            <TextField
              variant="outlined"
              placeholder="输入消息..."
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              multiline
              rows={1}
              maxRows={4}
              fullWidth
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: 20,
                  padding: '4px 12px',
                }
              }}
            />
            
            <IconButton onClick={handleSendMessage} disabled={!newMessage.trim() || isSending}>
              <Send size={24} color={newMessage.trim() ? 'primary' : 'disabled'} />
            </IconButton>
            
            <IconButton>
              <Mic size={24} color="primary" />
            </IconButton>
          </Box>
        </Paper>
      </Container>
    </Layout>
  );
};

export default ChatPage;