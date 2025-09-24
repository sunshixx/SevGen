import React from 'react';
import { RouterProvider } from 'react-router-dom';
import UserContextProvider from './context/UserContext';
import router from './router';
import './App.css';

function App() {
  return (
    <UserContextProvider>
      <RouterProvider router={router} />
    </UserContextProvider>
  );
}

export default App;
