// 模拟数据文件，用于在API不可用时提供测试数据

// 模拟用户数据
export const mockUser = {
  id: 1,
  username: '测试用户',
  email: 'test@example.com',
  avatar: null,
  createdAt: '2023-01-01T10:00:00Z',
  updatedAt: '2023-01-01T10:00:00Z'
};

// 模拟角色数据
export const mockRoles = [
  {
    id: 1,
    name: '哈利波特',
    category: '文学人物',
    description: '魔法世界的传奇人物，霍格沃茨的学生，对抗伏地魔的英雄。哈利波特拥有非凡的勇气和智慧，在朋友的帮助下战胜了无数困难。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 2,
    name: '苏格拉底',
    category: '历史人物',
    description: '古希腊哲学家，西方哲学的奠基人之一，以对话法著称。苏格拉底通过不断提问来引导人们思考真理和美德。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 3,
    name: '爱因斯坦',
    category: '科学家',
    description: '理论物理学家，相对论的创立者，20世纪最伟大的科学家之一。爱因斯坦的理论彻底改变了我们对时间、空间和引力的理解。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 4,
    name: '莎士比亚',
    category: '艺术家',
    description: '英国文艺复兴时期最杰出的剧作家和诗人，创作了众多经典作品。莎士比亚的戏剧深刻反映了人性的复杂性和社会现实。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 5,
    name: '拿破仑',
    category: '历史人物',
    description: '法国军事家、政治家，法兰西第一帝国皇帝，被誉为战争之神。拿破仑通过一系列军事胜利改变了欧洲的政治格局。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 6,
    name: '居里夫人',
    category: '科学家',
    description: '物理学家、化学家，首位获得两次诺贝尔奖的人，研究放射性。居里夫人的发现为现代医学和物理学做出了巨大贡献。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 7,
    name: '福尔摩斯',
    category: '虚构角色',
    description: '阿瑟·柯南·道尔创作的侦探角色，以观察力和推理能力著称。福尔摩斯善于通过细节推理解决复杂案件。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  },
  {
    id: 8,
    name: '莫扎特',
    category: '艺术家',
    description: '奥地利作曲家，古典主义音乐的代表人物，被称为音乐神童。莫扎特的音乐作品充满了天才的创造力和情感表达。',
    avatar: null,
    createdAt: '2023-01-01T10:00:00Z'
  }
];

// 模拟聊天会话数据
export const mockChatSessions = [
  {
    id: 1,
    roleId: 1,
    title: '与哈利波特的对话',
    createdAt: '2023-01-02T14:30:00Z',
    updatedAt: '2023-01-02T15:45:00Z',
    isActive: true
  },
  {
    id: 2,
    roleId: 3,
    title: '与爱因斯坦讨论相对论',
    createdAt: '2023-01-01T11:20:00Z',
    updatedAt: '2023-01-01T12:10:00Z',
    isActive: true
  },
  {
    id: 3,
    roleId: 7,
    title: '与福尔摩斯一起破案',
    createdAt: '2022-12-30T09:15:00Z',
    updatedAt: '2022-12-30T10:00:00Z',
    isActive: false
  }
];

// 模拟消息数据
export const mockMessages = [
  {
    id: 1,
    chatId: 1,
    role: 'assistant',
    content: '你好！我是哈利波特。有什么我可以帮助你的吗？',
    createdAt: '2023-01-02T14:30:00Z',
    isRead: true
  },
  {
    id: 2,
    chatId: 1,
    role: 'user',
    content: '哈利，你好！霍格沃茨的生活怎么样？',
    createdAt: '2023-01-02T14:35:00Z',
    isRead: true
  },
  {
    id: 3,
    chatId: 1,
    role: 'assistant',
    content: '霍格沃茨的生活总是充满惊喜和冒险！每天都有新的咒语要学习，新的朋友要认识，当然还有魁地奇比赛！虽然有时候会遇到危险，但有朋友们在身边，一切都变得不同。',
    createdAt: '2023-01-02T14:40:00Z',
    isRead: true
  },
  {
    id: 4,
    chatId: 2,
    role: 'assistant',
    content: '你好！我是爱因斯坦。很高兴与你讨论物理学问题。',
    createdAt: '2023-01-01T11:20:00Z',
    isRead: true
  },
  {
    id: 5,
    chatId: 2,
    role: 'user',
    content: '爱因斯坦先生，您能简单解释一下相对论吗？',
    createdAt: '2023-01-01T11:25:00Z',
    isRead: true
  },
  {
    id: 6,
    chatId: 2,
    role: 'assistant',
    content: '相对论可以分为狭义相对论和广义相对论。简单来说，狭义相对论告诉我们时间和空间是相互关联的，光速在任何参考系中都是恒定的。而广义相对论则进一步将引力解释为时空的弯曲。',
    createdAt: '2023-01-01T11:30:00Z',
    isRead: true
  }
];

// 模拟分类数据
export const mockCategories = ['全部', '文学人物', '历史人物', '科学家', '艺术家', '虚构角色'];

// 导出所有模拟数据
export default {
  user: mockUser,
  roles: mockRoles,
  chatSessions: mockChatSessions,
  messages: mockMessages,
  categories: mockCategories
};