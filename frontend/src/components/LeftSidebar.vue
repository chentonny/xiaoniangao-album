<template>
  <div class="left-sidebar">
    <router-link to="/" class="sidebar-item" :class="{ active: $route.path === '/' }">首页</router-link>
    <router-link to="/recent-upload" class="sidebar-item" :class="{ active: $route.path === '/recent-upload' }" v-if="userStore.canViewPublicMedia">最新上传</router-link>
    <router-link to="/my-media" class="sidebar-item" :class="{ active: $route.path === '/my-media' }">我的资源</router-link>
    <router-link to="/public-media" class="sidebar-item" :class="{ active: $route.path === '/public-media' }" v-if="userStore.canViewPublicMedia">公共资源</router-link>
    <router-link to="/upload" class="sidebar-item" :class="{ active: $route.path === '/upload' }" v-if="userStore.canUpload">上传文件</router-link>
    <router-link to="/admin" class="sidebar-item" :class="{ active: $route.path === '/admin' }" v-if="userStore.isAdmin">管理员面板</router-link>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import userStore from '../store/userState.js'

export default defineComponent({
  name: 'LeftSidebar',
  setup() {
    return {
      userStore
    }
  }
})
</script>

<style scoped>
/* 样式已在index.html中定义 */
</style>