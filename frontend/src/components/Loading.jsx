import React from 'react';
import { CircularProgress, Box, Typography } from '@mui/material';

const Loading = ({ message = '加载中...' }) => {
  return (
    <Box
      display="flex"
      flexDirection="column"
      alignItems="center"
      justifyContent="center"
      minHeight="40vh"
      style={{ textAlign: 'center' }}
    >
      <CircularProgress size={60} style={{ marginBottom: 20 }} />
      <Typography variant="h6" color="textPrimary">
        {message}
      </Typography>
    </Box>
  );
};

export default Loading;