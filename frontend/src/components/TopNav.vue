<template>
  <div class="top-nav">
    <div class="logo">
      <img src="/static/images/logo.jpg" alt="小年糕相册" />      <span class="logo-text">小年糕相册</span>
    </div>
    
    <div class="nav-menu">
      <!-- 统一导航元素容器 - 包含搜索、登录/注册、用户头像 -->
      <div class="nav-elements-container">

        
        <!-- 登录和注册标签 - 登录按钮始终显示，注册按钮仅在未登录时显示 -->
        <div class="auth-tags">
          <router-link to="/login" class="auth-tag login-tag">登录</router-link>
          <router-link v-if="!userStore.isLoggedIn" to="/register" class="auth-tag register-tag">注册</router-link>
        </div>
        
        <!-- 用户信息区域 - 对所有用户可见 -->
        <div 
            class="user-dropdown" 
            @click.stop="toggleDropdown"
            @mouseenter="showDropdown = true"
            @mouseleave="handleMouseLeave">
          <img 
                :src="currentUserAvatar" 
                alt="用户头像" 
                class="user-avatar" 
            />
          <!-- 用户名显示 -->
          <span class="user-name">
            {{ currentUserName }}
          </span>
          
          <!-- 下拉菜单 -->
          <div 
                v-show="showDropdown" 
                class="dropdown-menu"
                @mouseenter="showDropdown = true"
                @mouseleave="handleMouseLeave">
            <div class="dropdown-item" @click.stop="goToMyMedia">我的</div>
            <div class="dropdown-item" @click.stop="logout">退出</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, computed } from 'vue'
import userStore from '../store/userState.js'
import { useRouter } from 'vue-router'

export default defineComponent({
  name: 'TopNav',
  props: {
    showAuthButtons: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    const showDropdown = ref(false)
    const router = useRouter()
    
    // 计算当前用户名
    const currentUserName = computed(() => {
      // 首先检查userStore.userInfo是否存在
      if (userStore.userInfo && typeof userStore.userInfo === 'object') {
        // 优先显示用户名
        return userStore.userInfo.userName || 
               userStore.userInfo.username || 
               userStore.userInfo.name || 
               userStore.userInfo.userID || 
               '用户'
      }
      return '用户'
    })
    
    // 计算当前用户头像
    const currentUserAvatar = computed(() => {
      // 无论是否登录，都使用boy.png作为头像
      return '/static/images/boy.png'
    })
    
    // 初始化时恢复用户信息
    onMounted(() => {
      if (userStore) {
        userStore.restoreUserInfo()
      }
      
      // 添加全局点击事件监听
      document.addEventListener('click', handleClickOutside)
    })
    
    const toggleDropdown = () => {
      showDropdown.value = !showDropdown.value
    }

    const logout = () => {
      if (userStore) {
        userStore.logout()
      }
      showDropdown.value = false
      router.push('/')  // 退出后跳转到首页
    }
    
    const goToMyMedia = () => {
      router.push('/user-info')
      showDropdown.value = false
    }
    

    
    // 处理鼠标离开下拉菜单区域
    const handleMouseLeave = () => {
      // 使用setTimeout给用户一个小延迟，防止快速移动鼠标时菜单闪烁
      setTimeout(() => {
        showDropdown.value = false
      }, 200)
    }
    
    // 点击其他地方关闭下拉菜单
    const handleClickOutside = (event) => {
      const dropdown = document.querySelector('.user-dropdown')
      const menu = document.querySelector('.dropdown-menu')
      
      if (showDropdown.value) {
        const isClickInside = (dropdown && dropdown.contains(event.target)) || 
                             (menu && menu.contains(event.target))
        
        if (!isClickInside) {
          showDropdown.value = false
        }
      }
    }
    
    return {
      userStore,
      showDropdown,
      router,
      toggleDropdown,
      logout,
      goToMyMedia,
      showAuthButtons: props.showAuthButtons,
      currentUserName,
      currentUserAvatar,
      handleMouseLeave
    }
  }
})
</script>

<style scoped>

/* 顶部导航栏样式 - 与左侧导航栏统一色系 */
.top-nav {
  height: 60px; /* 设置固定高度 */
  background-color: #ba68c8; /* 与左侧导航栏相同色系 */
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

/* 导航菜单样式 */
.nav-menu {
  display: flex;
  align-items: center;
  height: 100%;
}

/* 统一导航元素容器 */
.nav-elements-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 100%;
  padding: 0 20px;
}

/* 登录和注册标签样式 */
.auth-tags {
  display: flex;
  align-items: center;
  z-index: 1000;
  position: relative;
}

.auth-tag {
  padding: 10px 20px;
  margin-left: 15px;
  color: white !important;
  text-decoration: none;
  font-weight: bold;
  transition: all 0.3s ease;
  cursor: pointer;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  position: relative;
  z-index: 1001;
}

.auth-tag:active {
  transform: scale(0.98);
}

.login-tag:hover,
.register-tag:hover {
  opacity: 1;
  text-decoration: none;
  background-color: rgba(255, 255, 255, 0.2);
}



.user-dropdown {
  position: relative;
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 100%;
}

.user-name {
  margin-left: 8px;
  font-size: 14px;
  font-weight: bold;
  color: white !important;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.dropdown-menu {
  position: absolute;
  top: 50px;
  right: 0;
  background-color: #fff;
  border-radius: 5px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  min-width: 120px;
  z-index: 1001;
}

.dropdown-item {
  padding: 10px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
}

.dropdown-item:hover {
  background-color: #f0f9ff;
  color: #1890ff;
}

.dropdown-item:first-child {
  border-top-left-radius: 5px;
  border-top-right-radius: 5px;
}

.dropdown-item:last-child {
  border-bottom-left-radius: 5px;
  border-bottom-right-radius: 5px;
}

/* 用户头像占位符样式 */
.user-placeholder {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 100%;
}
</style>