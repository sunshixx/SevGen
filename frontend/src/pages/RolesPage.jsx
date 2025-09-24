import React, { useState, useEffect } from 'react';
import { Container, Typography, Grid, TextField, Button, Box, FormControl, InputLabel, Select, MenuItem, CircularProgress, Alert } from '@mui/material';
import { Search, FilterList } from '@mui/icons-material';
import Layout from '../components/Layout';
import RoleCard from '../components/RoleCard';
import { mockRoles, mockCategories } from '../mockData';

const RolesPage = () => {
  const [roles, setRoles] = useState([]);
  const [filteredRoles, setFilteredRoles] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [categories, setCategories] = useState(mockCategories);

  // 加载角色数据（使用模拟数据）
  useEffect(() => {
    try {
      setIsLoading(true);
      // 直接使用mockData中的角色数据
      setRoles(mockRoles);
      setFilteredRoles(mockRoles);
    } catch (err) {
      console.error('Failed to load roles:', err);
      setError('加载角色列表失败');
    } finally {
      setIsLoading(false);
    }
  }, []);

  // 搜索和筛选功能
  useEffect(() => {
    let result = [...roles];

    // 搜索筛选
    if (searchQuery) {
      const query = searchQuery.toLowerCase();
      result = result.filter(role => 
        role.name.toLowerCase().includes(query) || 
        role.description.toLowerCase().includes(query)
      );
    }

    // 分类筛选
    if (categoryFilter && categoryFilter !== '全部') {
      result = result.filter(role => role.category === categoryFilter);
    }

    setFilteredRoles(result);
  }, [searchQuery, categoryFilter, roles]);

  const handleSearch = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleCategoryChange = (e) => {
    setCategoryFilter(e.target.value);
  };

  const handleClearFilters = () => {
    setSearchQuery('');
    setCategoryFilter('');
  };

  return (
    <Layout title="角色浏览">
      <Container maxWidth="lg">
        <Box sx={{ my: 4 }}>
          <Typography variant="h4" component="h1" gutterBottom>
            探索AI角色
          </Typography>
          <Typography variant="body1" paragraph>
            与来自不同领域的AI角色进行对话，体验沉浸式的角色扮演聊天
          </Typography>
        </Box>

        {/* 搜索和筛选栏 */}
        <Box 
          sx={{ 
            mb: 4, 
            p: 2, 
            backgroundColor: '#f5f5f5', 
            borderRadius: 2, 
            display: 'flex', 
            flexWrap: 'wrap', 
            gap: 2, 
            alignItems: 'center'
          }}
        >
          <TextField
            variant="outlined"
            placeholder="搜索角色名称或描述..."
            value={searchQuery}
            onChange={handleSearch}
            fullWidth
            style={{ maxWidth: 400 }}
            InputProps={{
              startAdornment: <Search size={18} style={{ marginRight: 8 }} />,
            }}
          />
          
          <FormControl variant="outlined" style={{ minWidth: 150 }}>
            <InputLabel>分类筛选</InputLabel>
            <Select
              value={categoryFilter}
              onChange={handleCategoryChange}
              label="分类筛选"
              InputProps={{
                startAdornment: <FilterList size={18} style={{ marginRight: 8 }} />,
              }}
            >
              {categories.map((category, index) => (
                <MenuItem key={index} value={category}>{category}</MenuItem>
              ))}
            </Select>
          </FormControl>
          
          {(searchQuery || categoryFilter) && (
            <Button 
              variant="outlined" 
              onClick={handleClearFilters}
              style={{ borderColor: '#3f51b5', color: '#3f51b5' }}
            >
              清除筛选
            </Button>
          )}
        </Box>

        {/* 角色列表 */}
        {isLoading ? (
          <Box display="flex" justifyContent="center" py={8}>
            <CircularProgress size={60} />
          </Box>
        ) : error ? (
          <Alert severity="error" sx={{ mb: 4 }}>
            {error}
          </Alert>
        ) : filteredRoles.length === 0 ? (
          <Box 
            display="flex" 
            flexDirection="column" 
            alignItems="center" 
            justifyContent="center" 
            minHeight="40vh"
          >
            <Typography variant="h6" color="textPrimary" gutterBottom>
              未找到匹配的角色
            </Typography>
            <Typography variant="body1" color="textSecondary" textAlign="center">
              尝试调整搜索条件或清除筛选器
            </Typography>
          </Box>
        ) : (
          <Grid container spacing={3}>
            {filteredRoles.map((role) => (
              <Grid item xs={12} sm={6} md={3} key={role.id}>
                <RoleCard role={role} />
              </Grid>
            ))}
          </Grid>
        )}

        {/* 结果统计 */}
        {filteredRoles.length > 0 && (
          <Box sx={{ mt: 4, textAlign: 'center' }}>
            <Typography variant="body2" color="textSecondary">
              共找到 {filteredRoles.length} 个角色
            </Typography>
          </Box>
        )}
      </Container>
    </Layout>
  );
};

export default RolesPage;