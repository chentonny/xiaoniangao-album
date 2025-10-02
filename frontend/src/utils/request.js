// 使用特殊语法防止axios导入被自动删除
import /* 防止自动删除 */ axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../store/userState.js';

// 创建axios实例
const request = axios.create({
  baseURL: '', // 基础URL（空字符串，因为vite代理已处理/api前缀）
  timeout: 300000, // 请求超时时间：5分钟（300000毫秒），以支持大文件加载
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 在请求拦截器中获取userStore实例，因为这里是函数作用域，不是顶层作用域
    const userStore = useUserStore();
    const token = userStore.token;
    
    // 如果有token，添加到请求头
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    // 可以在这里添加一些全局loading状态
    
    return config;
  },
  error => {
    // 请求错误处理
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 这里可以关闭全局loading状态
    
    // 获取响应数据
    const res = response.data;
    
    // 对于登录接口的特殊处理：让所有状态码的响应都通过
    // 因为登录页面有自己的错误处理逻辑
    if (response.config.url && response.config.url.includes('/login')) {
      return res;
    }
    
    // 根据后端返回的状态码进行处理
    // 注意：后端成功状态码是1，不是200
    if (res.code !== 1 && res.code !== 200) {
      // 非成功状态码，显示错误信息
      ElMessage.error(res.message || '请求失败');
      
      // 处理特定错误码
      switch (res.code) {
        case 401:
          // 未授权，跳转到登录页面
          userStore.logout();
          window.location.href = '/login';
          break;
        case 403:
          // 禁止访问，权限不足
          ElMessage.error('权限不足，请联系管理员');
          break;
        case 404:
          // 资源不存在
          ElMessage.error('请求的资源不存在');
          break;
        case 500:
          // 服务器错误
          ElMessage.error('服务器内部错误，请稍后重试');
          break;
        default:
          // 其他错误
          break;
      }
      
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    
    // 请求成功，返回数据
    return res;
  },
  error => {
    // 响应错误处理
    
    // 处理网络错误
    if (!error.response) {
      ElMessage.error('网络错误，请检查网络连接');
      return Promise.reject(error);
    }
    
    // 处理服务器错误
    const status = error.response.status;
    const message = error.response.data?.message || '请求失败';
    
    switch (status) {
      case 401:
        // 未授权，跳转到登录页面
        userStore.logout();
        window.location.href = '/login';
        break;
      case 403:
        // 禁止访问，权限不足
        ElMessage.error('权限不足，请联系管理员');
        break;
      case 404:
        // 资源不存在
        ElMessage.error('请求的资源不存在');
        break;
      case 500:
        // 服务器错误
        ElMessage.error('服务器内部错误，请稍后重试');
        break;
      default:
        // 其他错误
        ElMessage.error(message);
        break;
    }
    
    return Promise.reject(error);
  }
);

// 导出常用的HTTP方法
const get = (url, params = {}, config = {}) => {
  return request.get(url, { params, ...config });
};

const post = (url, data = {}, config = {}) => {
  return request.post(url, data, config);
};

const put = (url, data = {}, config = {}) => {
  return request.put(url, data, config);
};

const del = (url, config = {}) => {
  return request.delete(url, config);
};

const upload = (url, file, data = {}, config = {}) => {
  const formData = new FormData();
  formData.append('file', file);
  
  // 添加其他数据
  Object.keys(data).forEach(key => {
    formData.append(key, data[key]);
  });
  
  return request.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...config
  });
};

// 导出所有方法
export default {
  get,
  post,
  put,
  delete: del,
  upload
};

// 同时支持按需导入
export { get, post, put, del as delete, upload };