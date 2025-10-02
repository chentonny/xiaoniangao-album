<template>
  <div class="login-container">
      <div class="login-box">
        <div class="login-header">
          <h2>小年糕相册</h2>
          <p>欢迎回来！请登录您的账号</p>
        </div>
        
        <!-- 防止浏览器自动填充的增强策略 -->
        <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" class="login-form" autocomplete="off">
          <el-form-item prop="username">
            <!-- 添加隐藏字段来欺骗浏览器自动填充 -->
            <input type="text" v-show="false" readonly autocomplete="off" />
            <input type="password" v-show="false" readonly autocomplete="off" />
            
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              :maxlength="20"
              show-word-limit
              class="custom-input"
              autocomplete="new-username"
              :name="randomName('username')"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              placeholder="请输入密码"
              show-password
              :maxlength="20"
              show-word-limit
              class="custom-input"
              autocomplete="new-password"
              :name="randomName('password')"
            />
          </el-form-item>
          
          <el-form-item prop="captcha">
            <el-input
              v-model="loginForm.captcha"
              placeholder="请输入验证码"
              :maxlength="6"
              show-word-limit
              class="custom-input captcha-input"
            />
            <div class="captcha-container">
              <img :src="captchaImageUrl" @click="refreshCaptcha" class="captcha-image" alt="验证码" />
              <span @click="refreshCaptcha" class="captcha-refresh">换一张</span>
            </div>
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              class="login-button"
              :loading="loading"
              @click="handleLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>
        
        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
        </div>
      </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onActivated, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request.js';
import { useUserStore } from '../store/userState.js';
import router from '../router.js';

export default {
  name: 'Login',
  setup() {
    // 使用已导入的userStore对象
    const loginForm = reactive({
      username: '',
      password: '',
      captcha: ''
    });
    
    const captchaImageUrl = ref('/api/captcha?t=' + Date.now()); // 使用后端验证码API
    const loading = ref(false);
    const loginRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      captcha: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
        { min: 4, max: 6, message: '验证码长度为 4 到 6 个字符', trigger: 'blur' }
      ]
    };
    
    const loginFormRef = ref(null);
    
    // 生成随机name属性，防止浏览器根据name识别字段
    const randomName = (prefix) => {
      return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    };
    
    // 刷新验证码 - 使用后端API
    const refreshCaptcha = () => {
      captchaImageUrl.value = '/api/captcha?t=' + Date.now();
    };
    
    // 已移除自动清空表单的功能，防止用户输入被意外清空
    
    // 初始化时加载验证码
    
    // 页面加载完成后再执行一次清空操作
    const clearInputsAfterLoad = () => {
      setTimeout(() => {
        loginForm.username = '';
        loginForm.password = '';
        loginForm.captcha = '';
      }, 500);
    };
    
    // 在setup函数中创建store实例
    const userStore = useUserStore();
    
    // 处理登录
    const handleLogin = async () => {
      loginFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          try {
            // 检查验证码是否已加载
            if (!captchaImageUrl.value) {
              refreshCaptcha();
              ElMessage.warning('请先加载验证码');
              loading.value = false;
              return;
            }
            
            // 准备登录数据
            const loginData = {
              username: loginForm.username,
              password: loginForm.password,
              captcha: loginForm.captcha
            };
            
            // 使用request对象发送请求
            const response = await request.post('/api/login', loginData);
            
            // 检查响应数据 - 修复响应结构处理
            if (response && response.code === 1 && response.data) {
              // 用户信息在response.data中
              const userInfo = response.data;
              
              // 保存用户信息到 store
              userStore.login(userInfo);
              
              // 只使用sessionStorage存储用户信息，避免关闭浏览器后仍然保持登录状态
              sessionStorage.setItem('userInfo', JSON.stringify(userInfo));
              
              ElMessage.success('登录成功！即将跳转到最新上传页面...');
              setTimeout(() => {
                router.push('/recent-upload');
              }, 1500);
            } else {
              // 显示具体的错误信息
              const errorMessage = response?.data?.message || '登录失败';
              console.error('登录失败原因:', errorMessage);
              console.error('完整响应数据:', response);
              ElMessage.error(errorMessage);
              // 登录失败时刷新验证码
              refreshCaptcha();
            }
          } catch (error) {
            console.error('登录请求异常:', error);
            // 详细分析错误类型
            if (error.response) {
              // 服务器返回了错误状态码
              const status = error.response.status;
              const errorData = error.response.data;
              
              console.error('服务器错误状态码:', status);
              console.error('服务器错误数据:', errorData);
              
              if (status === 403) {
                // 403错误的详细处理
                if (errorData && errorData.message) {
                  ElMessage.error(`权限错误：${errorData.message}`);
                } else {
                  ElMessage.error('权限错误：可能是验证码错误、账户被禁用或其他权限问题');
                }
                // 特别提示验证码错误的可能性
                ElMessage.warning('请确认您输入的验证码与图片上显示的一致');
              } else if (status === 401) {
                ElMessage.error('认证失败：用户名或密码错误');
              } else if (status === 404) {
                ElMessage.error('API不存在，请确认后端服务是否正确启动');
              } else if (status >= 500) {
                ElMessage.error('服务器内部错误，请稍后重试');
              } else {
                ElMessage.error(`登录失败：${errorData?.message || '未知错误'}`);
              }
            } else if (error.request) {
              // 请求已发送但没有收到响应
              ElMessage.error('网络错误：服务器没有响应，请检查网络连接和后端服务是否正常运行');
              ElMessage.warning('请确认您的服务器是否已启动，端口是否正确');
            } else {
              // 请求配置出错
              ElMessage.error('请求错误：无法发送登录请求');
              console.error('请求配置错误:', error.message);
            }
            // 登录失败时刷新验证码
            refreshCaptcha();
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    // 组件挂载时清空所有输入框
    onMounted(() => {
      // 使用setTimeout确保清空操作在浏览器自动填充之后执行
      setTimeout(() => {
        loginForm.username = '';
        loginForm.password = '';
        loginForm.captcha = '';
      }, 100);
      
      // 调用额外的清空函数
      clearInputsAfterLoad();
    });
    
    // 组件激活时也清空输入框，确保从其他页面导航回来时输入框也是空的
    onActivated(() => {
      setTimeout(() => {
        loginForm.username = '';
        loginForm.password = '';
        loginForm.captcha = '';
      }, 100);
    });
    
    return {
      loginForm,
      loginFormRef,
      loading,
      loginRules,
      handleLogin,
      captchaImageUrl,
      refreshCaptcha,
      randomName
    };
  }
};
</script>

<style scoped>
.app {
  min-height: 100vh;
  background-color: #f8f5fd;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding-top: 60px;
  background: linear-gradient(135deg, #e1bee7 0%, #d1c4e9 100%);
  background-size: 400% 400%;
  animation: gradientBG 15s ease infinite;
}

@keyframes gradientBG {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.login-box {
  background-color: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
  text-align: center;
  position: relative;
  overflow: hidden;
}

.login-box::before {
  content: '';
  position: absolute;
  top: -50px;
  right: -50px;
  width: 200px;
  height: 200px;
  background-color: rgba(186, 104, 200, 0.2);
  border-radius: 50%;
  z-index: 0;
}

.login-box::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: -50px;
  width: 150px;
  height: 150px;
  background-color: rgba(156, 39, 176, 0.2);
  border-radius: 50%;
  z-index: 0;
}

.login-header,
.login-form,
.login-footer {
  position: relative;
  z-index: 1;
}

.login-header h2 {
  margin: 0 0 10px 0;
  font-size: 32px;
  color: #9c27b0;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.login-header p {
  margin: 0 0 30px 0;
  color: #666;
  font-size: 16px;
}

.login-form {
  margin-bottom: 20px;
}

.login-form .el-form-item {
  margin-bottom: 25px;
}

/* 验证码输入框样式 */
.login-form .captcha-input {
  display: inline-block;
  width: 60%;
}

/* 验证码容器样式 */
.captcha-container {
  display: inline-block;
  width: 38%;
  margin-left: 2%;
  vertical-align: middle;
  position: relative;
}

/* 验证码图片样式 */
.captcha-image {
  width: 100%;
  height: 44px;
  border-radius: 25px;
  cursor: pointer;
  background-color: #f0f0f0;
}

/* 刷新按钮样式 */
.captcha-refresh {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #9c27b0;
  font-size: 14px;
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.captcha-container:hover .captcha-refresh {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.8);
  padding: 4px 8px;
  border-radius: 15px;
}

.login-form .el-input {
  border-radius: 25px;
  overflow: hidden;
}

.login-form .el-input__wrapper {
    border-radius: 25px;
    border: 2px solid #e6e6e6;
    transition: all 0.3s ease;
  }

  /* 淡紫色边框 - 增强优先级 */
  .login-form .el-input.custom-input .el-input__wrapper:focus-within,
  .login-form .el-input.custom-input.is-focus .el-input__wrapper {
    border-color: #d1c4e9 !important;
    box-shadow: 0 0 0 2px rgba(209, 196, 233, 0.5) !important;
    outline: none !important;
  }
  
  /* 移除验证错误的红色边框 - 增强优先级 */
  .login-form .el-form-item.is-error .el-input.custom-input .el-input__wrapper {
    border-color: #e6e6e6 !important;
    box-shadow: none !important;
  }
  
  .login-form .el-form-item.is-error .el-input.custom-input .el-input__wrapper:focus-within,
  .login-form .el-form-item.is-error .el-input.custom-input.is-focus .el-input__wrapper {
    border-color: #d1c4e9 !important;
    box-shadow: 0 0 0 2px rgba(209, 196, 233, 0.5) !important;
  }
  
  /* 基础状态样式 */
  .login-form .el-input.custom-input .el-input__wrapper {
    border-color: #e6e6e6 !important;
    box-shadow: none !important;
  }

.login-form .el-input__inner {
  border-radius: 25px;
  font-size: 16px;
  height: 44px;
}

.login-button {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  font-size: 18px;
  font-weight: bold;
  background-color: #9c27b0;
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  background-color: #7b1fa2;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-footer {
  margin-top: 20px;
}

.login-footer span {
  color: #666;
  font-size: 14px;
}

.register-link {
  color: #9c27b0;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link:hover {
  color: #7b1fa2;
  text-decoration: underline;
}

/* 儿童风格的装饰元素 */
.login-box {
  position: relative;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  border-radius: 20px;
}

.login-box::before {
  background-color: rgba(186, 104, 200, 0.1);
  animation: float 6s ease-in-out infinite;
}

.login-box::after {
  background-color: rgba(156, 39, 176, 0.1);
  animation: float 8s ease-in-out infinite 1s;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-container {
    margin-left: 0;
    padding-top: 60px;
    padding-bottom: 60px;
  }
  
  .login-box {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 24px;
  }
  
  .login-button {
    height: 44px;
    font-size: 16px;
  }
}
</style>