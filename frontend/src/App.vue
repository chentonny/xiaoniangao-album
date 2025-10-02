<template>
    <div class="app">
      <!-- 顶部导航栏 -->
      <TopNav />
      
      <div class="content">
        <!-- 左侧边栏 -->
        <LeftSidebar />
        
        <!-- 路由视图 -->
        <router-view class="router-content" />
      </div>
    
    <!-- 页脚 -->
    <footer class="app-footer">
      <div class="footer-content">
        <div class="beian-info">
          <span class="beian-item">
            <img src="/static/images/gxb.png" alt="备案图标" class="beian-icon">
            <a href="https://beian.miit.gov.cn/#/Integrated/index" class="beian-link" target="_blank">网站备案号: 闽ICP备2021019794号</a>
          </span>
          <span class="beian-item">
            <img src="/static/images/gaj.png" alt="公安备案图标" class="beian-icon">
            <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=35020602002382" class="beian-link" target="_blank">闽公网安备 35020602002382号</a>
          </span>
        </div>
        <p>&copy; 2023 小年糕相册 - 分享美好瞬间</p>
        <div class="footer-links">
          <a href="/about" class="footer-link">关于我们</a>
          <a href="/privacy" class="footer-link">隐私政策</a>
          <a href="/terms" class="footer-link">使用条款</a>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import TopNav from './components/TopNav.vue'
import LeftSidebar from './components/LeftSidebar.vue'

export default defineComponent({
  name: 'App',
  components: {
    TopNav,
    LeftSidebar
  }
})
</script>

<style scoped>
.app {
  min-height: 100vh;
  background-color: #f8f5fd;
  display: flex;
  flex-direction: column;
}

.content {
  display: flex;
  flex: 1;
  margin-top: 60px; /* 顶部导航栏高度 */
}

/* 左侧边栏样式由index.html全局定义 */

/* 路由视图内容 */
.content > .router-content {
  flex: 1;
  margin-left: 200px; /* 与index.html中左侧边栏宽度保持一致 */
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 100; /* 确保内容区域在侧边栏之上 */
}

.app-footer {
  margin-top: auto;
  background-color: #ba68c8;
  color: #fff;
  padding: 20px 0;
  margin-left: 200px; /* 与左侧边栏宽度保持一致 */
  width: calc(100% - 200px);
  position: relative;
  z-index: 100;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
  text-align: center;
}

.footer-content .beian-info {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 10px;
}

.beian-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.beian-icon {
  width: 16px;
  height: 16px;
}

.beian-link {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  opacity: 0.8;
}

.beian-link:hover {
  opacity: 1;
}

.footer-content p {
  margin-bottom: 10px;
  font-size: 14px;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 10px;
}

.footer-link {
  color: #fff;
  text-decoration: none;
  font-size: 14px;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.footer-link:hover {
  opacity: 1;
}

/* 确保输入框可以获得焦点 */
.router-content input,
.router-content textarea {
  position: relative;
  z-index: 200; /* 确保输入框在最顶层 */
}

@media (max-width: 768px) {
  .content > .router-content {
    margin-left: 0;
    margin-bottom: 60px; /* 底部导航栏高度 */
  }
  
  .app-footer {
    margin-left: 0;
    width: 100%;
    margin-bottom: 60px; /* 底部导航栏高度 */
  }
  
  .footer-content .beian-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .footer-links {
    flex-direction: column;
    gap: 8px;
  }
}
</style>