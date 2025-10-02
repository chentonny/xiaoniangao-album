<template>
  <div class="user-info-container">
    <h2 class="page-title">我的信息</h2>
    
    <div class="user-info-card">
      <!-- 用户基本信息 -->
      <div class="info-section">
        <h3>基本信息</h3>
        <div class="info-item">
          <label>用户名：</label>
          <span class="info-value">{{ displayForm.userName || '未设置' }}</span>
          <button class="edit-btn" @click="showEditDialog('userName', '用户名')">修改</button>
        </div>
        <div class="info-item">
          <label>昵称：</label>
          <span class="info-value">{{ displayForm.nickname || '未设置' }}</span>
          <button class="edit-btn" @click="showEditDialog('nickname', '昵称')">修改</button>
        </div>
        <div class="info-item">
          <label>邮箱：</label>
          <span class="info-value">{{ displayForm.email || '未设置' }}</span>
          <button class="edit-btn" @click="showEditDialog('email', '邮箱')">修改</button>
        </div>
        <div class="info-item">
          <label>电话号码：</label>
          <span class="info-value">{{ displayForm.phone || '未设置' }}</span>
          <button class="edit-btn" @click="showEditDialog('phone', '电话号码')">修改</button>
        </div>
        <div class="info-item">
          <label>密码：</label>
          <span class="info-value">••••••</span>
          <button class="edit-btn" @click="showChangePasswordDialog = true">修改</button>
        </div>
        <div class="info-item">
          <label>用户角色：</label>
          <span class="role-text">{{ getUserRoleText() }}</span>
        </div>
      </div>
      
      <!-- 操作按钮区域 -->
    <div class="action-section">
      <button class="save-btn" @click="saveUserInfo">保存修改</button>
    </div>
  </div>

  <!-- 修改单个信息的对话框 -->
    <el-dialog
      v-model="showEditDialogVisible"
      :title="`修改${currentFieldLabel}`"
      width="400px"
      :before-close="handleCloseEditDialog"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item :label="currentFieldLabel" prop="tempValue">
          <el-input
            v-model="editForm.tempValue"
            :type="getInputType(currentField)"
            :placeholder="`请输入${currentFieldLabel}`"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleCloseEditDialog">取消</el-button>
          <el-button type="primary" @click="confirmEditField">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="showChangePasswordDialog"
      title="修改密码"
      width="400px"
      :before-close="handleClosePasswordDialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="原密码" prop="currentPassword">
          <el-input v-model="passwordForm.currentPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleClosePasswordDialog">取消</el-button>
          <el-button type="primary" @click="confirmChangePassword">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, computed, reactive } from 'vue'
import userStore from '../store/userState.js'
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus'
import { get, post } from '../utils/request.js'

export default defineComponent({
  name: 'UserInfo',
  components: {
    ElDialog,
    ElForm,
    ElFormItem,
    ElInput,
    ElButton
  },
  setup() {
    // 表单引用
    const passwordFormRef = ref(null)
    const editFormRef = ref(null)
    
    // 显示修改密码对话框
    const showChangePasswordDialog = ref(false)
    // 显示修改单个字段对话框
    const showEditDialogVisible = ref(false)
    
    // 当前正在编辑的字段
    const currentField = ref('')
    const currentFieldLabel = ref('')
    
    // 用户信息显示表单（显示给用户的值）
    const displayForm = reactive({
      userName: '',
      nickname: '',
      email: '',
      phone: ''
    })
    
    // 用户信息临时修改表单（暂存未保存的修改）
    const tempChanges = reactive({})
    
    // 单个字段编辑表单
    const editForm = reactive({
      tempValue: ''
    })
    
    // 密码修改表单
    const passwordForm = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    // 密码表单验证规则
    const passwordRules = {
      currentPassword: [
        { required: true, message: '请输入原密码', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== passwordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 单个字段编辑表单验证规则
    const editRules = {
      tempValue: [
        { required: true, message: '此字段不能为空', trigger: 'blur' }
      ]
    }
    
    // 获取用户信息
    const userInfo = computed(() => {
      return userStore.userInfo || {}
    })
    
    // 初始化时恢复用户信息
    onMounted(() => {
      if (userStore) {
        userStore.restoreUserInfo()
        // 加载用户完整信息
        loadUserDetails()
      }
    })
    
    // 从数据库加载用户完整信息
    const loadUserDetails = async () => {
      try {
        // 从数据库获取用户完整信息
        const response = await get('/api/user/info')
        
        if (response && response.code === 1) {
          // 检查response.data是否存在及结构
          if (response.data) {
            const userData = response.data
            
            // 更新显示表单
            displayForm.userName = userData.userName || ''
            displayForm.nickname = userData.nickname || ''
            displayForm.email = userData.email || ''
            displayForm.phone = userData.phone || ''
            
            // 同时更新userStore中的role字段，确保角色显示正确
            if (userStore && userData.role) {
              userStore.userInfo.role = userData.role
            }
            
            // 清空临时修改
            Object.keys(tempChanges).forEach(key => delete tempChanges[key])
          } else {
            console.warn('API响应中没有data字段')
            ElMessage.warning('获取用户信息不完整')
          }
        } else {
          console.warn('API响应失败:', response?.message || '未知错误')
          ElMessage.warning('获取用户信息失败: ' + (response?.message || '未知错误'))
        }
      } catch (error) {
        console.error('加载用户信息失败:', error)
        console.error('错误详情:', error.response || error.message)
        
        // 显示错误信息
        let errorMsg = '获取用户信息失败'
        if (error.response) {
          console.error('错误状态码:', error.response.status)
          console.error('错误响应数据:', error.response.data)
          errorMsg += ': ' + (error.response.data?.message || '服务器错误')
        } else {
          errorMsg += ': ' + error.message
        }
        
        // 使用模拟数据确保页面能够正常显示
        if (userInfo.value) {
          displayForm.userName = userInfo.value.userName || userInfo.value.username || ''
          displayForm.nickname = userInfo.value.nickname || userInfo.value.name || ''
          displayForm.email = userInfo.value.email || 'default@example.com'
          displayForm.phone = userInfo.value.phone || '13800138000'
        } else {
          // 如果localStorage中也没有用户信息，使用默认值
          displayForm.userName = 'user' + Math.floor(Math.random() * 1000)
          displayForm.nickname = '用户' + Math.floor(Math.random() * 1000)
          displayForm.email = 'default@example.com'
          displayForm.phone = '13800138000'
        }
        
        ElMessage.error('获取用户信息失败，已使用本地数据或默认值')
      }
    }
    
    // 获取用户角色文本
    const getUserRoleText = () => {
      const role = userInfo.value.role || 0
      switch (parseInt(role)) {
        case 1:
          return '管理员'
        case 2:
          return '普通用户'
        default:
          return '未知角色'
      }
    }
    
    // 根据字段类型获取输入框类型
    const getInputType = (field) => {
      if (field === 'email') {
        return 'email'
      } else if (field === 'phone') {
        return 'tel'
      } else {
        return 'text'
      }
    }
    
    // 显示编辑字段对话框
    const showEditDialog = (field, label) => {
      currentField.value = field
      currentFieldLabel.value = label
      // 设置当前值到编辑表单
      editForm.tempValue = displayForm[field] || ''
      showEditDialogVisible.value = true
    }
    
    // 确认编辑字段
    const confirmEditField = async () => {
      // 验证表单
      if (!editFormRef.value) return
      
      try {
        await editFormRef.value.validate()
        
        // 更新显示表单和临时修改对象
        displayForm[currentField.value] = editForm.tempValue
        tempChanges[currentField.value] = editForm.tempValue
        
        ElMessage.success(`${currentFieldLabel.value}修改成功（暂未保存）`)
        handleCloseEditDialog()
      } catch (error) {
        // 表单验证失败
        console.error('验证失败:', error)
      }
    }
    
    // 关闭编辑字段对话框
    const handleCloseEditDialog = () => {
      // 重置表单
      if (editFormRef.value) {
        editFormRef.value.resetFields()
      }
      editForm.tempValue = ''
      showEditDialogVisible.value = false
    }
    
    // 保存用户信息
    const saveUserInfo = async () => {
      // 检查是否有修改
      if (Object.keys(tempChanges).length === 0) {
        ElMessage.info('没有任何修改需要保存')
        return
      }
      
      try {
        const response = await post('/api/user/update', tempChanges)
        
        if (response && response.code === 1) {
          ElMessage.success('用户信息保存成功')
          // 更新localStorage中的用户信息
          if (userStore && userStore.userInfo) {
            const updatedUserInfo = { ...userStore.userInfo }
            // 应用临时修改到userStore
            Object.keys(tempChanges).forEach(key => {
              updatedUserInfo[key] = tempChanges[key]
            })
            localStorage.setItem('userInfo', JSON.stringify(updatedUserInfo))
            userStore.userInfo = updatedUserInfo
          }
          // 清空临时修改
          Object.keys(tempChanges).forEach(key => delete tempChanges[key])
        } else {
          ElMessage.error('保存失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        ElMessage.error('保存用户信息时发生错误')
        console.error('保存用户信息失败:', error)
      }
    }
    
    // 关闭修改密码对话框
    const handleClosePasswordDialog = () => {
      // 重置表单
      if (passwordFormRef.value) {
        passwordFormRef.value.resetFields()
      }
      passwordForm.currentPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      showChangePasswordDialog.value = false
    }
    
    // 确认修改密码
    const confirmChangePassword = async () => {
      // 验证表单
      if (!passwordFormRef.value) return
      
      try {
        await passwordFormRef.value.validate()
        
        // 调用API修改密码
        const response = await post('/api/user/change-password', {
          currentPassword: passwordForm.currentPassword,
          newPassword: passwordForm.newPassword
        })
        
        if (response && response.code === 1) {
          ElMessage.success('密码修改成功')
          handleClosePasswordDialog()
        } else {
          ElMessage.error('密码修改失败：' + (response.message || '未知错误'))
        }
      } catch (error) {
        // 表单验证失败或API调用失败
        console.error('修改密码失败:', error)
      }
    }
    
    return {
      userInfo,
      displayForm,
      tempChanges,
      editForm,
      passwordForm,
      passwordRules,
      editRules,
      passwordFormRef,
      editFormRef,
      showChangePasswordDialog,
      showEditDialogVisible,
      currentField,
      currentFieldLabel,
      getUserRoleText,
      getInputType,
      showEditDialog,
      confirmEditField,
      handleCloseEditDialog,
      saveUserInfo,
      handleClosePasswordDialog,
      confirmChangePassword
    }
  }
})
</script>

<style scoped>
.user-info-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.page-title {
  color: #333;
  font-size: 24px;
  margin-bottom: 20px;
  text-align: center;
}

.user-info-card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  margin-bottom: 20px;
}

.info-section h3 {
  color: #333;
  font-size: 18px;
  margin-bottom: 15px;
  border-bottom: 2px solid #ba68c8;
  padding-bottom: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.info-item:last-child {
  border-bottom: none;
}

.info-item label {
  font-weight: bold;
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.info-value {
  flex: 1;
  color: #333;
  padding: 0 10px;
}

.role-text {
  color: #333;
  flex: 1;
}

.edit-btn {
  padding: 5px 12px;
  background-color: #1976d2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  transition: background-color 0.3s;
}

.edit-btn:hover {
  background-color: #1565c0;
}

.action-section {
  margin-top: 20px;
  text-align: center;
}

.save-btn {
  padding: 10px 30px;
  background-color: #28a745;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #218838;
}

.quick-access-section {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 20px;
  text-align: center;
}

.quick-access-section h3 {
  color: #333;
  font-size: 18px;
  margin-bottom: 15px;
}

.quick-access-btn {
  display: inline-block;
  background-color: #2196f3;
  color: white;
  text-decoration: none;
  padding: 10px 20px;
  border-radius: 4px;
  transition: background-color 0.3s;
}
</style>