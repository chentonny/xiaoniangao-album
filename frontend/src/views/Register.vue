<template>
  <div class="register-container">
      <div class="register-box">
      <div class="register-header">
        <h2>创建账号</h2>
        <p>加入小年糕相册，记录美好时光</p>
      </div>
      
      <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            :maxlength="20"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="nick">
          <el-input
            v-model="registerForm.nick"
            placeholder="请输入昵称"
            :maxlength="20"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入电子邮箱（选填）"
            :maxlength="50"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入电话号码"
            :maxlength="11"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            placeholder="请输入密码"
            show-password
            :maxlength="20"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            placeholder="请确认密码"
            show-password
            :maxlength="20"
            show-word-limit
            class="custom-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            class="register-button"
            :loading="loading"
            @click="handleRegister"
          >
            现在注册
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue';
import { ElMessage } from 'element-plus';
import request from '../utils/request.js';
import { Check } from '@element-plus/icons-vue';

export default {
  name: 'Register',
  components: {
    Check
  },
  setup() {
    const registerForm = reactive({
      username: '',
      nick: '',
      email: '',
      phone: '',
      password: '',
      confirmPassword: ''
    });
    const loading = ref(false);
    const registerRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 2, max: 20, message: '用户名长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      nick: [
        { required: true, message: '请输入昵称', trigger: 'blur' },
        { min: 2, max: 20, message: '昵称长度在 2 到 20 个字符', trigger: 'blur' }
      ],
      email: [
        { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入有效的电子邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入电话号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号码', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
        { pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/, message: '密码必须包含大小写字母和数字', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== registerForm.password) {
              callback(new Error('两次输入的密码不一致'));
            } else {
              callback();
            }
          },
          trigger: 'blur'
        }
      ]
    };
    
    const registerFormRef = ref(null);
    
    // 处理注册
    const handleRegister = async () => {
      registerFormRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true;
          try {
            const requestData = {
              userName: registerForm.username,
              nickname: registerForm.nick,
              email: registerForm.email,
              phone: registerForm.phone,
              password: registerForm.password,
              role: 3 // 默认访客角色（根据后端定义，3为访客）
            };
            
            const response = await request.post('/api/register', requestData);
            
            if (response.code === 1) {
                ElMessage.success('注册成功！即将跳转到登录页面');
                // 跳转到登录页面
                setTimeout(() => {
                  window.location.href = '/login';
                }, 1500);
              } else {
                
                ElMessage.error(response.message || '注册失败');
              }
          } catch (error) {
            
            ElMessage.error('注册失败，请检查网络或稍后重试');
          } finally {
            loading.value = false;
          }
        }
      });
    };
    
    return {
      registerForm,
      registerFormRef,
      loading,
      registerRules,
      handleRegister
    };
  }
};
</script>

<style scoped>
.app {
  min-height: 100vh;
  background-color: #f8f5fd;
}

.register-container {
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

.register-box {
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

.register-box::before {
  content: '';
  position: absolute;
  top: -50px;
  left: -50px;
  width: 200px;
  height: 200px;
  background-color: rgba(186, 104, 200, 0.2);
  border-radius: 50%;
  z-index: 0;
}

.register-box::after {
  content: '';
  position: absolute;
  bottom: -50px;
  right: -50px;
  width: 150px;
  height: 150px;
  background-color: rgba(156, 39, 176, 0.2);
  border-radius: 50%;
  z-index: 0;
}

.register-header,
.register-form,
.register-footer {
  position: relative;
  z-index: 1;
}

.register-header h2 {
  margin: 0 0 10px 0;
  font-size: 32px;
  color: #9c27b0;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.register-header p {
  margin: 0 0 30px 0;
  color: #666;
  font-size: 16px;
}

.register-form {
  margin-bottom: 20px;
}

.register-form .el-form-item {
  margin-bottom: 25px;
}

.register-form .el-input {
  border-radius: 25px;
  overflow: hidden;
}

.register-form .el-input__wrapper {
    border-radius: 25px;
    border: 2px solid #e6e6e6;
    transition: all 0.3s ease;
  }

  /* 淡紫色边框 - 增强优先级 */
  .register-form .el-input.custom-input .el-input__wrapper:focus-within,
  .register-form .el-input.custom-input.is-focus .el-input__wrapper {
    border-color: #d1c4e9 !important;
    box-shadow: 0 0 0 2px rgba(209, 196, 233, 0.5) !important;
    outline: none !important;
  }
  
  /* 移除验证错误的红色边框 - 增强优先级 */
  .register-form .el-form-item.is-error .el-input.custom-input .el-input__wrapper {
    border-color: #e6e6e6 !important;
    box-shadow: none !important;
  }
  
  .register-form .el-form-item.is-error .el-input.custom-input .el-input__wrapper:focus-within,
  .register-form .el-form-item.is-error .el-input.custom-input.is-focus .el-input__wrapper {
    border-color: #d1c4e9 !important;
    box-shadow: 0 0 0 2px rgba(209, 196, 233, 0.5) !important;
  }
  
  /* 基础状态样式 */
  .register-form .el-input.custom-input .el-input__wrapper {
    border-color: #e6e6e6 !important;
    box-shadow: none !important;
  }

.register-form .el-input__inner {
  border-radius: 25px;
  font-size: 16px;
  height: 44px;
}

.register-button {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  font-size: 18px;
  font-weight: bold;
  background-color: #9c27b0;
  border: none;
  transition: all 0.3s ease;
}

.register-button:hover {
  background-color: #7b1fa2;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(156, 39, 176, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.register-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.register-footer {
  margin-top: 20px;
}

.register-footer span {
  color: #666;
  font-size: 14px;
}

.login-link {
  color: #9c27b0;
  text-decoration: none;
  font-weight: bold;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.login-link:hover {
  color: #7b1fa2;
  text-decoration: underline;
}

/* 儿童风格的装饰元素 */
.register-box {
  position: relative;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.15);
  border-radius: 20px;
}

.register-box::before {
  background-color: rgba(186, 104, 200, 0.1);
  animation: float 6s ease-in-out infinite;
}

.register-box::after {
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
  .register-container {
    margin-left: 0;
    padding-top: 60px;
    padding-bottom: 60px;
  }
  
  .register-box {
    margin: 20px;
    padding: 30px 20px;
  }
  
  .register-header h2 {
    font-size: 24px;
  }
  
  .register-button {
    height: 44px;
    font-size: 16px;
  }
}
</style>