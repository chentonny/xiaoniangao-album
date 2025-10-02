import { defineStore } from 'pinia'
import { defineStore as createPiniaStore, getActivePinia } from 'pinia';

// 定义用户状态的默认值
const defaultState = {
  userInfo: null,
  isLoggedIn: false
};

// 创建Pinia store
export const useUserStore = createPiniaStore('user', {
  state: () => ({ ...defaultState }),
  
  getters: {
    // 获取用户信息
    user: (state) => state.userInfo,
    
    // 角色判断
    isAdmin: (state) => state.userInfo && String(state.userInfo.role) === '1',
    
    isRegularUser: (state) => state.userInfo && String(state.userInfo.role) === '2',
    
    isGuest: (state) => state.userInfo && String(state.userInfo.role) === '3',
    
    // 权限判断
    canUpload: (state) => state.userInfo && (String(state.userInfo.role) === '1' || String(state.userInfo.role) === '2'),
    
    canViewPublicMedia: (state) => state.userInfo && (String(state.userInfo.role) === '1' || String(state.userInfo.role) === '2'),
    
    token: (state) => state.userInfo && state.userInfo.token || ''
  },
  
  actions: {
    // 登录方法
    login(userData) {
      try {
        // 确保userData是有效的对象
        if (userData && typeof userData === 'object') {
          this.userInfo = userData;
          this.isLoggedIn = true;
          
          // 确保userName存在，如果不存在则设置默认值
          if (!this.userInfo.userName) {
            this.userInfo.userName = '用户' + (this.userInfo.userID || '');
          }
          
          // 只使用sessionStorage存储用户信息，避免关闭浏览器后仍然保持登录状态
          sessionStorage.setItem('userInfo', JSON.stringify(userData));
        }
      } catch (error) {
        this.userInfo = null;
        this.isLoggedIn = false;
      }
    },
    
    // 退出登录方法
    logout() {
      this.userInfo = null;
      this.isLoggedIn = false;
      sessionStorage.removeItem('userInfo');
    },
    
    // 从本地存储恢复用户信息
    restoreUserInfo() {
      try {
        // 只从sessionStorage恢复用户信息，避免关闭浏览器后仍然保持登录状态
        let userInfo = sessionStorage.getItem('userInfo');
        
        if (userInfo) {
          try {
            const parsedData = JSON.parse(userInfo);
            this.userInfo = parsedData;
            this.isLoggedIn = true;
          } catch (e) {
            // 如果解析失败，清除无效数据并设置为未登录状态
            sessionStorage.removeItem('userInfo');
            this.userInfo = null;
            this.isLoggedIn = false;
          }
        }
      } catch (e) {
        this.userInfo = null;
        this.isLoggedIn = false;
      }
    }
  }
});

// 简单的默认值对象，用于在store不可用时提供基本的mock数据
const mockStore = {
  userInfo: null,
  isLoggedIn: false,
  token: '',
  isAdmin: false,
  isRegularUser: false,
  isGuest: false,
  canUpload: false,
  canViewPublicMedia: false,
  // 模拟方法
  login: function() {},
  logout: function() {},
  restoreUserInfo: function() {}
};

// 全局store实例
let globalStore = null;

// 创建store实例的函数
function createStore() {
  try {
    const pinia = getActivePinia();
    if (pinia) {
      globalStore = useUserStore(pinia);
      return globalStore;
    }
  } catch (e) {
    // 静默处理，返回mockStore
  }
  return mockStore;
}

// 确保全局store已初始化
function ensureStore() {
  if (!globalStore) {
    globalStore = createStore();
  }
  return globalStore;
}

// 导出简化版的store代理，直接访问属性和方法
const userStore = {
  // 状态属性
  get userInfo() { return ensureStore().userInfo; },
  set userInfo(value) { ensureStore().userInfo = value; },
  
  get isLoggedIn() { return ensureStore().isLoggedIn; },
  set isLoggedIn(value) { ensureStore().isLoggedIn = value; },
  
  // getter属性
  get isAdmin() { return ensureStore().isAdmin; },
  get isRegularUser() { return ensureStore().isRegularUser; },
  get isGuest() { return ensureStore().isGuest; },
  get canUpload() { return ensureStore().canUpload; },
  get canViewPublicMedia() { return ensureStore().canViewPublicMedia; },
  get token() { return ensureStore().token; },
  
  // action方法
  login: function(userData) {
    return ensureStore().login(userData);
  },
  logout: function() {
    return ensureStore().logout();
  },
  restoreUserInfo: function() {
    return ensureStore().restoreUserInfo();
  }
};

export default userStore;