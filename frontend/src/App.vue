<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">
// 主应用组件，只需要渲染路由视图
</script>

<style>
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background: #f5f7fa;
}

#app {
  min-height: 100vh;
}

/* 全局样式重置 */
.el-button {
  border-radius: 8px;
}

.el-input__wrapper {
  border-radius: 8px;
}

.el-card {
  border-radius: 12px;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 响应式辅助类 */
@media (max-width: 768px) {
  .hidden-mobile {
    display: none !important;
  }
}

@media (min-width: 769px) {
  .hidden-desktop {
    display: none !important;
  }
}
</style>