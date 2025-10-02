<template>
  <div class="upload-container">
    <LeftSidebar />
    <div class="main-content">
      <div v-if="!userStore.canUpload" class="no-permission">
        <el-icon size="60" color="#999"><i class="el-icon-warning" /></el-icon>
        <h2>您没有上传权限</h2>
        <p>只有注册用户才能上传媒体文件</p>
        <router-link to="/" class="back-btn">返回首页</router-link>
      </div>
      
      <div v-else class="upload-box">
        <div class="upload-header">
          <h2>上传照片/视频</h2>
          <p>分享您美好的回忆吧！</p>
        </div>
        
        <el-form :model="uploadForm" :rules="uploadRules" class="upload-form">
          <!-- 文件上传 -->
          <el-form-item label="选择文件" prop="file">
            <el-upload
              ref="upload"
              :headers="{ 'Authorization': `Bearer ${userStore.token}` }"
              :action="'/api/media/upload'"
              :data="uploadData"
              :multiple="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              :show-file-list="false"
              drag
              class="upload-dragger"
            >
              <i class="el-icon-upload"></i>
              <div class="el-upload__text">
                点击或拖拽文件到此处上传
              </div>
              <div class="el-upload__tip">
                支持.jpg、.jpeg、.png、.gif、.bmp、.mp4、.avi、.mov、.wmv格式，单个文件不超过50MB
              </div>
            </el-upload>
          </el-form-item>
          
          <!-- 选择的文件路径显示 -->
          <el-form-item label="文件名">
            <input
              v-model="selectedFilePath"
              type="text"
              readonly
              style="
                width: 100%;
                padding: 12px 14px;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                font-size: 14px;
                background-color: #f5f7fa;
                color: #606266;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
              "
              title="{{ selectedFilePath }}"
            />
            <div style="font-size: 12px; color: #909399; margin-top: 5px;">
              <i class="el-icon-info"></i> 提示：由于浏览器安全限制，在大多数情况下只能显示文件名
            </div>
          </el-form-item>
          

          
          <!-- 上传预览确认区 -->
          <div v-if="previewVisible" class="upload-preview">
            <div class="preview-title">上传预览</div>
            <div v-if="isImageFile" class="preview-content">
              <img :src="previewUrl" alt="预览图片" class="preview-image" />
            </div>
            <div v-else-if="isVideoFile" class="preview-content">
              <video :src="previewUrl" controls class="preview-video" />
            </div>
          </div>
          
          <!-- 增加间距 -->
          <div v-if="previewVisible" style="margin-bottom: 30px;"></div>
          
          <!-- 标题 - 使用原生input -->
          <el-form-item label="标题" prop="title">
            <div style="position: relative; z-index: 2000; width: 100%;">
              <input
                v-model="uploadForm.title"
                type="text"
                placeholder="请输入媒体标题（必填）"
                :maxlength="50"
                ref="titleInput"
                style="
                  width: 100%;
                  padding: 12px 14px;
                  border: 1px solid #dcdfe6;
                  border-radius: 4px;
                  font-size: 14px;
                  position: relative;
                  z-index: 2000;
                  outline: none;
                  box-sizing: border-box;
                  transition: border-color 0.3s;
                "
              />
              <div style="text-align: right; font-size: 12px; color: #999; margin-top: 5px;">
                {{ uploadForm.title.length }}/50
              </div>
            </div>
          </el-form-item>
          
          <!-- 描述 - 使用原生textarea -->
          <el-form-item label="描述" prop="description">
            <div style="position: relative; z-index: 2000; width: 100%;">
              <textarea
                v-model="uploadForm.description"
                placeholder="请输入媒体描述（选填）"
                :maxlength="200"
                rows="3"
                @input="handleDescriptionInput"
                @focus="handleDescriptionFocus"
                @blur="handleDescriptionBlur"
                ref="descriptionInput"
                style="
                  width: 100%;
                  padding: 12px 14px;
                  border: 1px solid #dcdfe6;
                  border-radius: 4px;
                  resize: vertical;
                  font-size: 14px;
                  position: relative;
                  z-index: 2000;
                  outline: none;
                  box-sizing: border-box;
                  transition: border-color 0.3s;
                "
              ></textarea>
              <div style="text-align: right; font-size: 12px; color: #999; margin-top: 5px;">
                {{ uploadForm.description.length }}/200
              </div>
            </div>
          </el-form-item>
          
          <!-- 标签输入 - 下拉列表框形式 -->
          <el-form-item label="添加标签" prop="tags">
            <!-- 使用虚线粉红色边框 -->
            <div style="border: 2px dashed #ffcccc; padding: 15px; border-radius: 8px; margin-bottom: 15px; width: 100%;">
              <!-- 已选标签显示区域 -->
              <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 15px; min-height: 40px; align-items: center;">
                <span 
                  v-for="tag in tagList" 
                  :key="tag"
                  style="background-color: #4096ff; color: white; padding: 4px 10px; border-radius: 4px; display: inline-flex; align-items: center;"
                >
                  {{ tag }}
                  <button 
                    type="button" 
                    style="margin-left: 5px; background: none; border: none; cursor: pointer; color: white; font-size: 14px; line-height: 1;"
                    @click="tagList.splice(tagList.indexOf(tag), 1)"
                  >×</button>
                </span>
                
                <span v-if="tagList.length === 0" style="color: #909399; font-size: 12px;">
                  请从下拉列表选择标签
                </span>
              </div>
              
              <!-- 使用下拉列表框选择标签 -->
              <el-select
                v-model="tagList"
                multiple
                placeholder="请选择标签（最多10个）"
                style="width: 100%;"
                :max-collapse-tags="10"
                filterable
              >
                <el-option
                  v-for="tag in availableTags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
              
              <!-- 提示信息 -->
              <div style="font-size: 12px; color: #909399; margin-top: 10px;">
                最多选择10个标签（当前{{ tagList.length }}/10）
              </div>
            </div>
          </el-form-item>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <div style="display: flex; gap: 10px;">
              <el-button type="primary" class="submit-button" :loading="submitting" @click="confirmUpload" style="flex: 3;">
                上传
              </el-button>
              <el-button class="submit-button" @click="cancelPreview" style="flex: 3; color: white;">
                取消
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, nextTick, onMounted, watch } from 'vue';
import { ElMessage, ElIcon } from 'element-plus';
import { Warning } from '@element-plus/icons-vue';
import request from '@/utils/request.js';
import router from '../router';
import LeftSidebar from '../components/LeftSidebar.vue';
import { useUserStore } from '../store/userState.js';

// 简化版标签输入组件
const ElTagInput = {
  name: 'ElTagInput',
  props: {
    modelValue: {
      type: Array,
      default: () => []
    },
    placeholder: {
      type: String,
      default: '添加标签'
    },
    maxinput: {
      type: Number,
      default: 10
    },
    maxlength: {
      type: Number,
      default: 20
    },
    availableTags: {
      type: Array,
      default: () => []
    }
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const inputValue = ref('');
    
    // 添加标签方法
    const addTag = (tag) => {
      if (props.modelValue.length >= props.maxinput) {
        ElMessage.warning(`最多添加${props.maxinput}个标签`);
        return;
      }
      
      if (!props.modelValue.includes(tag)) {
        emit('update:modelValue', [...props.modelValue, tag]);
      }
    };
    
    // 处理输入
    const handleInput = (e) => {
      inputValue.value = e.target.value;
    };
    
    // 处理回车
    const handleKeyup = (e) => {
      if (e.key === 'Enter' && inputValue.value.trim()) {
        const value = inputValue.value.trim();
        if (value.length <= props.maxlength) {
          addTag(value);
          inputValue.value = '';
        } else {
          ElMessage.warning(`标签长度不能超过${props.maxlength}个字符`);
        }
      }
    };
    
    // 移除标签
    const handleRemove = (tag) => {
      emit('update:modelValue', props.modelValue.filter(item => item !== tag));
    };
    
    return {
      inputValue,
      addTag,
      handleInput,
      handleKeyup,
      handleRemove
    };
  },
  template: `
    <div class="el-tag-input-container" style="border: 1px solid #dcdfe6; padding: 8px; border-radius: 4px; min-height: 32px; display: flex; flex-wrap: wrap; gap: 8px;">
      <!-- 已选标签 -->
      <div v-for="tag in modelValue" :key="tag" class="el-tag el-tag--info">
        {{ tag }}
        <i class="el-icon-close" @click.stop="handleRemove(tag)"></i>
      </div>
      
      <!-- 输入框 -->
      <input
        v-model="inputValue"
        type="text"
        :placeholder="placeholder"
        :maxlength="maxlength"
        style="border: none; outline: none; min-width: 120px;"
        @input="handleInput"
        @keyup="handleKeyup"
      />
    </div>
    
    <!-- 可用标签列表 -->
    <div v-if="availableTags.length > 0" style="margin-top: 10px;">
      <div style="font-size: 12px; color: #606266; margin-bottom: 5px;">推荐标签：</div>
      <div style="display: flex; flex-wrap: wrap; gap: 5px;">
        <span
          v-for="tag in availableTags"
          :key="tag"
          v-if="!modelValue.includes(tag)"
          style="padding: 2px 8px; background-color: #f0f9ff; color: #4096ff; border-radius: 12px; cursor: pointer; font-size: 12px;"
          @click="addTag(tag)"
        >
          {{ tag }}
        </span>
      </div>
    </div>
  `
};

export default {
  name: 'UploadMedia',
  components: {
    LeftSidebar,
    ElTagInput
  },
  setup() {
    const userStore = useUserStore();
    
    const uploadForm = reactive({
      file: null,
      title: '',
      description: '',
      tags: []
    });
    
    // 定义描述框引用
    const descriptionInput = ref(null);
    const titleInput = ref(null);
    const tagList = ref([]);
    const submitting = ref(false);
    const upload = ref(null);
    const previewVisible = ref(false);
    const previewUrl = ref('');
    const fileType = ref('');
    const fileSize = ref(0);
    const selectedFilePath = ref(''); // 用于存储用户选择的文件路径
    const fileInput = ref(null); // 用于清空文件输入
    // 直接初始化一些默认标签数据，确保组件加载时就有内容
    const availableTags = ref(['风景', '人物', '宠物', '美食', '旅行']);
    
    // 使用 ElSelect 组件后，不再需要手动的 toggleTag 方法
    // 组件会自动处理标签的选择和取消

    // 获取所有可用标签
    const fetchAvailableTags = async () => {
      try {
        const response = await request.get('/api/admin/tags');
        // request.js已经在拦截器中处理了响应，如果执行到这里，说明请求成功
        // 从返回的数据中提取标签名
        availableTags.value = response.data.map(tag => tag.tagName);
      } catch (error) {
        // 提供一些默认标签作为后备
        availableTags.value = ['风景', '人物', '宠物', '美食', '旅行'];
      }
    };

    // 组件挂载时执行初始化
    onMounted(() => {
      // 恢复用户信息
      userStore.restoreUserInfo();
      
      // 如果用户没有上传权限，阻止直接访问
      if (!userStore.canUpload) {
        // 不显示错误消息，因为模板中已经有提示
      }
      
      // 获取标签列表
      fetchAvailableTags();
    });
    
    // 保存当前选择的文件对象
    const currentFile = ref(null);
    
    // 表单验证规则
    const uploadRules = {
      file: [
        { required: true, message: '请选择文件', trigger: 'change' }
      ],
      title: [
        { required: true, message: '请输入媒体标题', trigger: 'blur' },
        { max: 50, message: '标题长度不能超过50个字符', trigger: 'blur' }
      ]
    };
    
    // 计算属性
    const isImageFile = computed(() => {
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(fileType.value.toLowerCase());
    });
    
    const isVideoFile = computed(() => {
      return ['mp4', 'avi', 'mov', 'wmv'].includes(fileType.value.toLowerCase());
    });
    
    // 上传数据
    const uploadData = computed(() => {
      return {
        description: uploadForm.description,
        tags: tagList.value ? tagList.value.join(',') : ''
      };
    });
    
    // 上传前校验
    const beforeUpload = (file) => {
      // 保存真实的File对象，以便后续上传使用
      currentFile.value = file;
      
      // 保存文件信息
      fileType.value = file.name.split('.').pop();
      fileSize.value = file.size;
      
      // 注意：由于浏览器安全限制，JavaScript无法获取用户本地文件系统中的完整文件路径
      // 下面的代码会尽可能显示更多的路径信息，但在大多数现代浏览器中，只能显示文件名
      
      // 首先尝试使用webkitRelativePath（仅在特定条件下可用，如拖拽文件夹上传）
      if (file.webkitRelativePath && file.webkitRelativePath !== '') {
        selectedFilePath.value = file.webkitRelativePath;
      } else {
        // 在大多数情况下，我们只能显示文件名
        selectedFilePath.value = file.name;
      }
      
      // 然后再进行验证
      const isImageOrVideo = isImageFile.value || isVideoFile.value;
      const isLt50M = file.size / 1024 / 1024 < 50;
      
      if (!isImageOrVideo) {
        ElMessage.error('只能上传图片或视频文件！');
        return false;
      }
      if (!isLt50M) {
        ElMessage.error('文件大小不能超过50MB！');
        return false;
      }
      
      // 创建预览URL
      const reader = new FileReader();
      reader.onload = (e) => {
        previewUrl.value = e.target.result;
        previewVisible.value = true;
      };
      reader.readAsDataURL(file);
      
      // 阻止自动上传，等待用户确认
      return false;
    };
    

    
    // 确认上传
    const confirmUpload = async () => {
      // 检查是否已经选择了文件并生成了预览
      // 使用previewVisible和selectedFilePath来判断，而不是直接从DOM获取fileInput
      if (!previewVisible.value || !selectedFilePath.value || !currentFile.value) {
        ElMessage.warning('请先选择要上传的文件');
        return;
      }
      
      // 使用之前在beforeUpload中保存的真实File对象
      const file = currentFile.value;
      
      // 确保文件对象有效
      if (!file || !(file instanceof File)) {
        ElMessage.error('无法获取有效的文件对象，请重新选择文件');
        submitting.value = false;
        return;
      }
      
      submitting.value = true;
      
      try {
        // 显示上传中提示
        ElMessage.info(`正在上传 ${file.name}...`);
        
        // 检查token是否存在
        if (!userStore.token) {
          ElMessage.error('用户未登录或登录已过期，请重新登录');
          submitting.value = false;
          return;
        }
        
        // 添加其他参数，与后端testUpload方法的@RequestParam注解保持一致
        const titleValue = uploadForm.title;
        const descriptionValue = uploadForm.description;
        const tagsValue = tagList.value ? tagList.value.join(',') : '';
        
        // 发送上传请求到正式接口
        const response = await request.upload('/api/media/upload', file, {
          title: uploadForm.title,
          description: uploadForm.description,
          tags: tagsValue
        }, {
          timeout: 60000 // 设置超时时间为60秒
        });
        
        // request.js已经在拦截器中处理了响应，如果执行到这里，说明请求成功
        ElMessage.success(`文件 ${file.name} 上传成功！`);
        
        // 重置表单
        uploadForm.title = '';
        uploadForm.description = '';
        tagList.value = [];
        previewVisible.value = false;
        previewUrl.value = '';
        selectedFilePath.value = '';
        
        // 清空文件输入
        if (fileInput) {
          fileInput.value = '';
        }
      } catch (error) {
        // 提供更详细的错误信息
        if (error.response) {
          ElMessage.error(`上传失败: ${error.response.data.message || '服务器错误'}`);
        } else if (error.request) {
          ElMessage.error('网络连接失败，请检查网络设置');
        } else {
          ElMessage.error('上传失败，请稍后重试');
        }
      } finally {
        submitting.value = false;
      }
    };
    
    // 取消预览
    const cancelPreview = () => {
      previewVisible.value = false;
      previewUrl.value = '';
      selectedFilePath.value = '';
      // 清空表单内容
      uploadForm.title = '';
      uploadForm.description = '';
      tagList.value = [];
      // 直接清空标题输入框DOM元素内容，确保清除生效
      if (titleInput.value) {
        titleInput.value.value = '';
      }
      // 直接清空描述框DOM元素内容，确保清除生效
      if (descriptionInput.value) {
        descriptionInput.value.value = '';
      }
      // 清空文件输入
      const fileInput = upload.value && upload.value.$el.querySelector('input[type="file"]');
      if (fileInput) {
        fileInput.value = '';
      }
    };
    
    // 上传成功回调
    const handleUploadSuccess = (response) => {
      // 这个方法可能不会被调用，因为我们使用了自定义上传逻辑
    };
    
    // 上传失败回调
    const handleUploadError = (error) => {
      ElMessage.error('上传失败，请稍后重试');
      submitting.value = false;
    };
    
    // 处理描述输入
    const handleDescriptionInput = (event) => {
      // 从事件对象中获取输入值
      const value = event.target.value;
      // 确保数据被正确绑定
      uploadForm.description = value;
    };

    // 处理描述框获得焦点
    const handleDescriptionFocus = () => {
      // 描述框获得焦点的处理逻辑
    };

    // 处理描述框失去焦点
    const handleDescriptionBlur = () => {
      // 描述框失去焦点的处理逻辑
    };

    // 提交表单
    const submitForm = () => {
      // 这里的逻辑已经被整合到确认上传中
      if (!previewVisible.value) {
        ElMessage.warning('请先选择文件并确认预览');
      }
    };
    
    return {
      userStore,
      uploadForm,
      tagList,
      submitting,
      upload,
      previewVisible,
      previewUrl,
      fileType,
      fileSize,
      selectedFilePath,
      isImageFile,
      isVideoFile,
      uploadData,
      uploadRules,
      beforeUpload,
      confirmUpload,
      cancelPreview,
      handleUploadSuccess,
      handleUploadError,
      submitForm,
      handleDescriptionInput,
      handleDescriptionFocus,
      handleDescriptionBlur,
      descriptionInput,
      titleInput,
      availableTags

    };
  }
};
</script>

<style scoped>
.upload-container {
  display: flex;
  min-height: 100vh;
  background-color: #f9f9f9;
}

.no-permission {
  text-align: center;
  padding: 80px 20px;
  background-color: #fff;
  border-radius: 20px;
  max-width: 500px;
  margin: 0 auto;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.no-permission h2 {
  margin-top: 20px;
  margin-bottom: 10px;
  font-size: 24px;
  color: #333;
}

.no-permission p {
  color: #666;
  margin-bottom: 30px;
}

.back-btn {
  display: inline-block;
  padding: 10px 30px;
  background-color: #1890ff;
  color: #fff;
  border-radius: 5px;
  text-decoration: none;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background-color: #40a9ff;
  transform: translateY(-2px);
}

.main-content {
  flex: 1;
  padding: 20px;
  min-height: calc(100vh - 60px);
  position: relative;
  z-index: 1;
}

.upload-box {
  background-color: #fff;
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  max-width: 600px;
  width: 100%;
  margin: 0 auto;
  position: relative;
}

.upload-header {
  text-align: center;
  margin-bottom: 40px;
}

.upload-header h2 {
  margin: 0 0 10px 0;
  font-size: 32px;
  color: #ff6bff;
  font-weight: bold;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
}

.upload-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.upload-form {
  margin-bottom: 20px;
}

.upload-form .el-form-item {
  margin-bottom: 30px;
}

.upload-form .el-form-item__label {
  font-size: 16px;
  color: #333;
  font-weight: bold;
}

.upload-dragger {
  border: 2px dashed #ffcccc;
  border-radius: 10px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.upload-dragger:hover {
  border-color: #ff6bff;
  background-color: #fff0ff;
}

.upload-dragger .el-icon-upload {
  font-size: 48px;
  color: #ff6bff;
}

.upload-dragger .el-upload__text {
  font-size: 16px;
  color: #666;
  margin: 10px 0;
}

.upload-dragger .el-upload__tip {
  font-size: 12px;
  color: #999;
}

.upload-preview {
  margin-top: 20px;
  padding: 20px;
  border: 2px solid #ccffcc;
  border-radius: 10px;
  background-color: #f8fff8;
}

.preview-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 15px;
}

.preview-content {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.preview-image {
  max-width: 100%;
  max-height: 300px;
  border-radius: 10px;
}

.preview-video {
  max-width: 100%;
  max-height: 300px;
  border-radius: 10px;
}

.preview-actions {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.tag-input-container {
  margin-top: 10px;
}

.tag-tip {
  margin-top: 10px;
  font-size: 12px;
  color: #999;
}

.submit-button {
  height: 48px;
  border-radius: 24px;
  font-size: 18px;
  font-weight: bold;
  background-color: #ff6bff;
  border: none;
  transition: all 0.3s ease;
}

.submit-button:hover {
  background-color: #ff47ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 255, 0.4);
}

.submit-button:active {
  transform: translateY(0);
}

.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 自定义标签输入样式 */
.el-tag-input-container {
  position: relative;
  display: inline-block;
  width: 100%;
  z-index: 3000;
}

.el-tag-input {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  min-height: 32px;
  padding: 4px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  transition: border-color 0.3s;
  cursor: text;
}

.el-tag-input:hover {
  border-color: #c0c4cc;
}

.el-tag-input:focus-within {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.el-tag-input .el-tag {
  margin: 4px;
}

.el-tag-input__input {
  flex: 1;
  min-width: 100px;
  height: 24px;
  padding: 0;
  margin: 4px;
  border: none;
  outline: none;
  font-size: 14px;
}

.el-tag-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  padding: 4px 0;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background-color: #ffffff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 2000;
  max-height: 200px;
  overflow-y: auto;
}

.el-tag-dropdown-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.el-tag-dropdown-item:hover {
  background-color: #f5f7fa;
  color: #409eff;
}

/* 过渡动画 */
.el-zoom-in-bottom-enter-active,
.el-zoom-in-bottom-leave-active {
  transition: opacity 0.3s, transform 0.3s;
}

.el-zoom-in-bottom-enter-from,
.el-zoom-in-bottom-leave-to {
  opacity: 0;
  transform: scaleY(0.8);
}

/* 确保媒体描述框可以获得焦点 */
.upload-form .el-textarea {
  position: relative;
  z-index: 1000; /* 显著提高z-index值 */
}

/* 确保输入框获得焦点时的层级 */
.upload-form .el-textarea__inner:focus {
  position: relative;
  z-index: 1001; /* 获得焦点时层级更高 */
  outline: 2px solid #ff6bff;
  outline-offset: 2px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .upload-box {
    margin: 0;
    padding: 30px 20px;
  }
  
  .upload-header h2 {
    font-size: 24px;
  }
  
  .submit-button {
    height: 44px;
    font-size: 16px;
  }
}
</style>