<template>
  <div class="home-container">
    <!-- 页面头部 -->
    <header class="home-header">
      <div class="logo-section">
        <h1>小年糕相册</h1>
        <p>记录美好瞬间，分享精彩生活</p>
      </div>
      
      <!-- 用户认证区域 -->
      <div class="auth-section">
        <router-link to="/login" class="auth-btn login-btn">登录</router-link>
        <router-link to="/register" class="auth-btn register-btn">注册</router-link>
      </div>
    </header>
    
    <!-- 主要内容区域 -->
    <main class="home-content">
      <!-- 特色功能介绍 -->
      <section class="features-section">
        <h2>探索我们的特色功能</h2>
        
        <div class="features-grid">
          <router-link to="/my-media" class="feature-card">
            <div class="feature-icon">
              <img src="/src/assets/cartoon-camera.svg" alt="我上传的" class="cartoon-icon">
            </div>
            <h3>我上传的</h3>
            <p>查看和管理您上传的照片和视频，随时随地访问您的珍贵回忆</p>
          </router-link>
          
          <router-link to="/public-media" class="feature-card">
            <div class="feature-icon">
              <img src="/src/assets/cartoon-tag.svg" alt="大家上传" class="cartoon-icon">
            </div>
            <h3>大家上传的</h3>
            <p>快来看看大家分享的照片和视频</p>
          </router-link>
          
          <router-link to="/upload" class="feature-card">
            <div class="feature-icon">
              <img src="/src/assets/cartoon-lock.svg" alt="现在上传" class="cartoon-icon">
            </div>
            <h3>现在上传</h3>
            <p>把您手里的珍贵照片分享给大家吧</p>
          </router-link>
        </div>
      </section>
      
      <!-- 快速入口 -->
      <section class="quick-access-section">
        <h2>快速开始</h2>
        
        <div class="access-cards">
          <router-link to="/login" class="access-card login-card">
            <div class="card-content">
              <h3>已有账号？立即登录</h3>
              <p>未经授权只能访问您的本人相册，不能查看别人相册</p>
            </div>
          </router-link>
          
          <router-link to="/register" class="access-card register-card">
            <div class="card-content">
              <h3>新用户？免费注册</h3>
              <p>创建账号，开始您的美好之旅</p>
            </div>
          </router-link>
        </div>
      </section>
    </main>
    

  </div>
</template>

<script setup>
import { onMounted } from 'vue';
import router from '../router.js';

// 页面加载时检查用户登录状态
onMounted(() => {
  const userInfo = localStorage.getItem('userInfo');
  
  // 如果用户已登录，可以考虑自动跳转到最新上传页面
  if (userInfo) {
    // 注意：这里没有添加自动跳转逻辑，因为我们希望用户能够看到首页内容
  }
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f8f5fd;
}

/* 页面头部样式 */
.home-header {
  background: linear-gradient(135deg, #9c27b0 0%, #7b1fa2 100%);
  color: white;
  padding: 2rem 0;
  text-align: center;
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.3);
}

.logo-section h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2.5rem;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.2);
}

.logo-section p {
  margin: 0;
  font-size: 1.2rem;
  opacity: 0.9;
}

.auth-section {
  margin-top: 1.5rem;
}

.auth-btn {
  display: inline-block;
  padding: 0.8rem 1.5rem;
  margin: 0 0.5rem;
  border-radius: 25px;
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s ease;
}

.login-btn {
  background-color: white;
  color: #9c27b0;
}

.login-btn:hover {
  background-color: #f3e5f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 255, 255, 0.3);
}

.register-btn {
  background-color: transparent;
  color: white;
  border: 2px solid white;
}

.register-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-2px);
}

/* 主要内容区域样式 */
.home-content {
  flex: 1;
  padding: 2rem;
}

/* 特色功能区域 */
.features-section {
  max-width: 1200px;
  margin: 0 auto 3rem auto;
  text-align: center;
}

.features-section h2 {
  color: #9c27b0;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.feature-card {
    background-color: white;
    border-radius: 15px;
    padding: 2rem;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    text-decoration: none;
    color: inherit;
    display: block;
  }

  .feature-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 24px rgba(156, 39, 176, 0.2);
  }

  .feature-card:visited {
    color: inherit;
  }

.feature-icon {
  margin-bottom: 1rem;
  display: flex;
  justify-content: center;
  align-items: center;
}

.cartoon-icon {
  width: 80px;
  height: 80px;
  transition: transform 0.3s ease;
}

.feature-card:hover .cartoon-icon {
  transform: scale(1.1) rotate(5deg);
}

.feature-card h3 {
  color: #9c27b0;
  margin-bottom: 0.8rem;
  font-size: 1.5rem;
}

.feature-card p {
  color: #666;
  line-height: 1.6;
}

/* 快速入口区域 */
.quick-access-section {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
}

.quick-access-section h2 {
  color: #9c27b0;
  margin-bottom: 2rem;
  font-size: 2rem;
}

.access-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 2rem;
}

.access-card {
  text-decoration: none;
  border-radius: 15px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.access-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(156, 39, 176, 0.2);
}

.login-card {
  background: linear-gradient(135deg, #e1bee7 0%, #d1c4e9 100%);
}

.register-card {
  background: linear-gradient(135deg, #d1c4e9 0%, #e1bee7 100%);
}

.card-content {
  padding: 2rem;
  color: #9c27b0;
}

.card-content h3 {
  margin-bottom: 0.8rem;
  font-size: 1.5rem;
}

.card-content p {
  line-height: 1.6;
}

/* 页脚样式 */
.home-footer {
  background-color: #f5f5f5;
  padding: 1.5rem 0;
  text-align: center;
  color: #666;
  border-top: 1px solid #e0e0e0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .home-header {
    padding: 1.5rem 0;
  }
  
  .logo-section h1 {
    font-size: 2rem;
  }
  
  .logo-section p {
    font-size: 1rem;
  }
  
  .auth-btn {
    padding: 0.6rem 1.2rem;
    margin: 0 0.3rem;
  }
  
  .home-content {
    padding: 1.5rem 1rem;
  }
  
  .features-section h2,
  .quick-access-section h2 {
    font-size: 1.8rem;
  }
  
  .features-grid,
  .access-cards {
    grid-template-columns: 1fr;
  }
  
  .feature-card,
  .access-card {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .auth-section {
    display: flex;
    flex-direction: column;
    align-items: center;
  }
  
  .auth-btn {
    margin: 0.5rem 0;
    width: 80%;
  }
}
</style>