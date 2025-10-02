<template>
  <div class="media-detail">
    <LeftSidebar />
    <div class="main-content">
      <div class="detail-header">
        <button @click="goBack" class="back-button">
          <span class="back-icon">←</span> 返回
        </button>
        <h2>{{ mediaDetail?.title || mediaDetail?.description || mediaDetail?.fileDescription || '媒体详情' }}</h2>
      </div>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-indicator">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
      
      <!-- 媒体详情内容 -->
      <div v-else-if="mediaDetail">
        <!-- 媒体预览区 -->
        <div class="media-preview">
          <img v-if="isImageFile" :src="getMediaUrl" :alt="mediaDetail.fileDescription || mediaDetail.description" class="preview-image" />
          <div v-else-if="isVideoFile" class="video-container">
            <video
              :src="getMediaUrl"
              controls
              playsinline
              autoplay
              muted
              class="preview-video"
              width="640"
              height="auto"
            >
              您的浏览器不支持视频播放
            </video>
          </div>
          <div v-else class="preview-placeholder">
            <p>不支持的文件类型</p>
          </div>
        </div>
        
        <!-- 媒体信息区 -->
        <div class="media-info">
          <h3>文件信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <label>上传者：</label>
              <span>{{ mediaDetail.uploaderName || mediaDetail.userName || mediaDetail.uploader || '未知' }}</span>
            </div>
            <div class="info-item">
              <label>上传时间：</label>
              <span>{{ formatDate(mediaDetail.createTime || mediaDetail.create_time || mediaDetail.uploadTime || '') || '未知' }}</span>
            </div>
            <div class="info-item">
              <label>文件类型：</label>
              <span>{{ mediaDetail.fileType || '未知' }}</span>
            </div>
            <div class="info-item">
              <label>文件大小：</label>
              <span>{{ formatFileSize(mediaDetail.fileSize) || '未知' }}</span>
            </div>
            <div class="info-item">
              <label>标签：</label>
              <span>{{ mediaDetail.tags || mediaDetail.fileTag || '无标签' }}</span>
            </div>
            <div class="info-item">
              <label>文件名：</label>
              <span>{{ extractFilename(mediaDetail.filePath) || '未知' }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 资源找不到提示 -->
      <div v-else class="media-not-exist">
        <div class="not-exist-icon">❌</div>
        <h3>资源找不到</h3>
        <p>您访问的媒体文件可能已被删除或不存在。</p>
        <button @click="goBack" class="back-button">返回列表</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import LeftSidebar from '../components/LeftSidebar.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(true)
const mediaDetail = ref(null)

// 格式化日期函数
const formatDate = (dateString) => {
  if (!dateString) return ''
  const d = new Date(dateString)
  return d.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (!bytes || bytes < 0) return '未知';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
}

// 从文件路径中提取文件名
const extractFilename = (filePath) => {
  if (!filePath) return '';
  
  // 处理文件路径，提取文件名
  const lastSlashIndex = Math.max(
    filePath.lastIndexOf('/'),
    filePath.lastIndexOf('\\')
  );
  return lastSlashIndex >= 0 ? 
    filePath.substring(lastSlashIndex + 1) : 
    filePath;
};

// 判断是否为图片文件
const isImageFile = computed(() => {
  if (!mediaDetail.value) return false;
  const mediaType = mediaDetail.value.fileType || mediaDetail.value.type;
  return mediaType && mediaType.includes('image');
});

// 判断是否为视频文件
const isVideoFile = computed(() => {
  if (!mediaDetail.value) return false;
  const mediaType = mediaDetail.value.fileType || mediaDetail.value.type;
  return mediaType && mediaType.includes('video');
});

// 获取媒体文件的URL路径
const getMediaUrl = computed(() => {
  if (!mediaDetail.value?.filePath) {
    return '';
  }
  // 从完整路径中提取文件名
  const fileName = extractFilename(mediaDetail.value.filePath);
  // 使用相对路径访问媒体文件
  return `/media-files/${fileName}`;
});

// 返回上一页
const goBack = () => {
  router.back();
}

// 加载媒体详情数据
const loadMediaDetail = async () => {
  loading.value = true
  try {
    // 尝试从路由参数中获取媒体ID，并确保进行正确的类型转换
    const mediaId = route.params.id
    
    // 优先尝试通过ID从API获取数据（解决刷新页面问题）
    if (mediaId) {
      try {
        // 转换mediaId为字符串，确保与后端API兼容
        const mediaIdStr = String(mediaId);
        
        // 使用正确的API端点格式
        const response = await fetch(`/api/media/media-detail?fileId=${mediaIdStr}`)
        
        if (response.ok) {
          const responseData = await response.json()
          
          // 从sessionStorage获取可能存在的完整数据（包含正确的uploaderName）
          const storedMediaData = sessionStorage.getItem('selectedMedia');
          let storedData = null;
          if (storedMediaData) {
            storedData = JSON.parse(storedMediaData);
          }
          
          // 从API返回的数据中获取实际的媒体数据（在data字段里）
          const apiMediaData = responseData.data || {};
          
          // 优先使用sessionStorage中的uploaderName，如果存在
          // 处理媒体数据，确保uploaderName字段存在
          mediaDetail.value = {
            ...apiMediaData,
            uploaderName: storedData?.uploaderName || apiMediaData.uploaderName || apiMediaData.userName || apiMediaData.uploader || apiMediaData.user?.username || apiMediaData.user?.name || '未知'
          }
          
          // 将获取到的数据存入sessionStorage备用
          sessionStorage.setItem('selectedMedia', JSON.stringify(mediaDetail.value));
        } else {
          // API请求失败，尝试从sessionStorage获取数据
          const storedMediaData = sessionStorage.getItem('selectedMedia')
          
          if (storedMediaData) {
            const parsedData = JSON.parse(storedMediaData);
            // 处理媒体数据，确保uploaderName字段存在
            mediaDetail.value = {
              ...parsedData,
              uploaderName: parsedData.uploaderName || parsedData.userName || parsedData.uploader || parsedData.user?.username || parsedData.user?.name || '未知'
            }
          } else {
            // 如果sessionStorage中也没有数据，尝试加载默认媒体
            await loadDefaultMedia();
          }
        }
      } catch (apiError) {
        console.error('API请求出错:', apiError);
        // API请求出错，尝试从sessionStorage获取数据
        const storedMediaData = sessionStorage.getItem('selectedMedia')
        
        if (storedMediaData) {
          mediaDetail.value = JSON.parse(storedMediaData)
        } else {
          // 如果sessionStorage中也没有数据，尝试加载默认媒体
          await loadDefaultMedia();
        }
      }
    } else {
      // 没有媒体ID参数，尝试从sessionStorage获取数据
      const storedData = getMediaFromSession();
      
      if (storedData) {
        mediaDetail.value = storedData;
      } else {
        // 如果sessionStorage中也没有数据，尝试加载默认媒体
        await loadDefaultMedia();
      }
    }
  } catch (error) {
    console.error('加载媒体详情时出错:', error);
    // 如果所有尝试都失败，设置为null显示资源找不到
    mediaDetail.value = null
  } finally {
    loading.value = false
  }
}

// 加载默认媒体数据（当其他方式都失败时）
const loadDefaultMedia = async () => {
  try {
    const recentResponse = await fetch('/api/media/recent')
    
    if (recentResponse.ok) {
      const recentData = await recentResponse.json()
      
      if (recentData && recentData.length > 0) {
        // 处理备用媒体数据，确保uploaderName字段存在
        mediaDetail.value = {
          ...recentData[0],
          uploaderName: recentData[0].uploaderName || recentData[0].userName || recentData[0].uploader || recentData[0].user?.username || recentData[0].user?.name || '未知'
        }
      } else {
        mediaDetail.value = null
      }
    } else {
      mediaDetail.value = null
    }
  } catch (error) {
    console.error('加载默认媒体时出错:', error);
    mediaDetail.value = null
  }
}

// 页面挂载时加载数据
onMounted(() => {
  loadMediaDetail()
});

</script>

<style scoped>
/* 重置媒体详情容器的样式 */
.media-detail {
  display: flex;
  min-height: 100vh;
  margin-left: 0 !important;
  margin-top: 0 !important;
  width: 100%;
  position: relative;
}

/* 媒体不存在提示样式 */
.media-not-exist {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  text-align: center;
  padding: 40px;
  background-color: #f8f9fa;
  border-radius: 8px;
  margin: 20px;
}

.media-not-exist .not-exist-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.7;
}

.media-not-exist h3 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.media-not-exist p {
  font-size: 16px;
  color: #666;
  margin-bottom: 20px;
}

.media-not-exist .back-button {
  background-color: #4a90e2;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.media-not-exist .back-button:hover {
  background-color: #357abd;
}

/* 修复主内容区域布局 */
.media-detail .main-content {
  flex: 1;
  padding: 20px;
  margin-left: 220px; /* 为左侧固定导航栏留出空间 */
  overflow-y: auto;
}

/* 详情页头部样式 */
.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.back-button {
  display: flex;
  align-items: center;
  background: none;
  border: none;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  margin-right: 20px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.back-button:hover {
  background-color: #f0f0f0;
}

.back-icon {
  margin-right: 5px;
}

.detail-header h2 {
  font-size: 20px;
  margin: 0;
  color: #333;
}

/* 媒体预览区样式 */
.media-preview {
  margin-bottom: 30px;
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.preview-image {
  max-width: 100%;
  max-height: 600px;
  border-radius: 4px;
}

/* 视频容器样式 */
.video-container {
  position: relative;
  width: 100%;
  max-width: 800px;
  height: auto;
  background-color: #000;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.preview-video {
  width: 100%;
  height: auto;
  min-height: 300px;
  background-color: #000;
  object-fit: contain;
  z-index: 5;
}

/* 视频封面样式 */
.video-cover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 10;
}

.video-cover-image {
  width: 100%;
  height: auto;
  object-fit: cover;
  border-radius: 4px;
}

.video-cover-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.play-icon {
  display: block;
  font-size: 48px;
  color: rgba(255, 255, 255, 0.8);
  background-color: rgba(0, 0, 0, 0.5);
  width: 80px;
  height: 80px;
  border-radius: 50%;
  line-height: 80px;
  text-align: center;
  margin: 0 auto 10px;
  transition: all 0.3s;
}

.video-cover:hover .play-icon {
  color: rgba(255, 255, 255, 1);
  background-color: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

.play-text {
  color: rgba(255, 255, 255, 0.8);
  background-color: rgba(0, 0, 0, 0.5);
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 14px;
}

/* 不支持的文件类型占位符 */
.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  background-color: #f0f0f0;
  border-radius: 4px;
}

/* 媒体信息区样式 */
.media-info {
  background-color: #f9f9f9;
  padding: 20px;
  border-radius: 8px;
}

.media-info h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 18px;
  color: #333;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.info-item label {
  font-weight: bold;
  margin-right: 10px;
  color: #666;
  min-width: 80px;
}

.info-item span {
  color: #333;
}

/* 加载指示器样式 */
.loading-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .media-detail .main-content {
    padding: 15px;
    margin-left: 0; /* 在小屏幕设备上不需要左边距 */
  }
  
  .detail-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .back-button {
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .media-preview {
    padding: 10px;
    min-height: 300px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>