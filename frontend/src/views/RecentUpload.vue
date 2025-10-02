<template>
  <div class="home-container">
    <h1 class="page-title">媒体库</h1>
    <!-- 最新上传标题区域 -->
    <div class="section-header">
      <h2 class="section-title">最新上传</h2>
      <button class="more-btn" @click="loadMoreMedia" :disabled="loadingMore">
        {{ loadingMore ? '加载中...' : 'more' }}
      </button>
    </div>
    <div class="media-grid">
      <div v-for="media in mediaList" :key="media.id" class="media-item" @click="goToMediaDetail(media)">
        <div class="media-thumbnail">
          <img v-if="media.type && media.type.includes('image')" :src="getMediaUrl(media)" :alt="media.title || '媒体文件'">
          <!-- 视频预览区域 - 使用视频第一帧作为预览图 -->
          <div v-else-if="media.type && media.type.includes('video')" class="video-preview-container">
            <!-- 视频元素 -->
            <video 
                  :id="`video-${media.id}`"
                  :data-id="media.id"
                  :src="getMediaUrl(media)"
                  :type="getCorrectMimeType(media.filename)"
                  controls 
                  playsinline 
                  muted 
                  preload="metadata"
                  controlslist="nodownload"
                  @loadeddata="handleVideoLoaded($event, media.id)"
                  @error="handleVideoError($event, media.id)"
                  class="video-element">
              <!-- 为Edge浏览器提供明确的视频格式声明 -->
              <source :src="getMediaUrl(media)" :type="getCorrectMimeType(media.filename)">
              您的浏览器不支持视频播放
            </video>
            <!-- 视频预览图容器 -->
            <div v-if="!videoLoaded[media.id]" :key="media.id" class="video-thumbnail-container">
              <img :src="videoThumbnails[media.id] || getVideoPlaceholder(media.id)" 
                   :alt="media.title || '视频预览'"
                   class="video-thumbnail">
              <!-- 播放按钮覆盖层 -->
              <div class="play-overlay" @click.stop="playVideo(media.id)">
                <div class="play-icon">▶</div>
              </div>
            </div>
          </div>
          <div v-else class="file-placeholder">
            {{ media.filename }}
          </div>
        </div>
        <div class="media-info">
          <h3 class="media-title">{{ media.title || '未命名' }}</h3>
          <p v-if="media.description" class="media-description">{{ media.description }}</p>
          <div class="media-meta">
            <span class="media-uploader">{{ media.uploaderName || '未知用户' }}</span>
            <span class="media-date">{{ formatDate(media.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 分页控制区域 -->
    <div class="bottom-controls">
      <!-- 分页器 -->
      <div class="pagination" v-if="mediaList.length > 0">
        <span class="pagination-info">第 {{ page }} 页</span>
        <div class="pagination-controls">
          <button 
            class="pagination-btn"
            @click="goToPage(1)"
            :disabled="page <= 1">
            首页
          </button>
          <button 
            class="pagination-btn"
            @click="goToPage(page - 1)"
            :disabled="page <= 1">
            上一页
          </button>
          <button 
            class="pagination-btn"
            @click="loadMoreMedia"
            :disabled="loadingMore">
            下一页
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '../store/userState.js';

// 导入媒体工具模块
import {
  getMediaUrl as getMediaFileUrl,
  isImage,
  isVideo,
  getMediaTypeLabel,
  getCorrectMimeType,
  storeMediaToSession
} from '../utils/mediaUtils.js';

const router = useRouter();
const userStore = useUserStore();
const mediaList = ref([]);
const loading = ref(false);
const loadingMore = ref(false);
const page = ref(1);
const pageSize = ref(8);

// 处理媒体数据，确保字段格式一致
const processMediaData = (mediaData) => {
  return mediaData.map(media => {
    // 确保media对象包含所有需要的字段
    const processedMedia = {
      id: media.id,
      // 直接使用fileType字段
      type: media.fileType,
      fileType: media.fileType,
      fileSize: media.fileSize,
      title: media.fileTitle || media.title || '未命名',
      createdAt: media.createTime || media.createdAt,
      filename: media.fileName || media.filename,
      filePath: media.filePath,
      // 从后端数据中正确提取上传者名称
      uploaderName: media.uploaderName || media.uploader || media.userName || media.user || media.uploader_username || '未知用户',
      // 处理描述字段
      description: media.description || ''
    };
    
    return processedMedia;
  });
};

// 存储视频缩略图URL
const videoThumbnails = ref({});

// 存储视频加载状态
const videoLoaded = ref({});

// 获取视频占位图
const getVideoPlaceholder = (mediaId) => {
  // 确保mediaId是字符串类型
  const mediaIdStr = String(mediaId);
  // 为每个视频生成唯一的占位图，包含视频标题信息
  const encodedTitle = encodeURIComponent(`视频预览 ${mediaIdStr.substring(0, 5)}`);
  return `data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 width=%22300%22 height=%22200%22 viewBox=%220 0 300 200%22%3E%3Crect width=%22300%22 height=%22200%22 fill=%22%23000%22/%3E%3Ctext x=%22150%22 y=%22100%22 font-family=%22Arial%22 font-size=%2216%22 fill=%22%23666%22 text-anchor=%22middle%22 alignment-baseline=%22middle%22%3E${encodedTitle}%3C/text%3E%3C/svg%3E`;
};

// 播放视频函数
const playVideo = (mediaId) => {
  const videoElement = document.getElementById(`video-${mediaId}`);
  if (videoElement) {
    // 隐藏缩略图容器
    const thumbnailContainer = videoElement.nextElementSibling;
    if (thumbnailContainer) {
      thumbnailContainer.style.display = 'none';
    }
    
    // 确保视频元素完全可见
    videoElement.style.display = 'block';
    videoElement.style.zIndex = '10';
    
    // 尝试播放视频
    videoElement.play().then(() => {
      // 播放成功后取消静音
      videoElement.muted = false;
    }).catch(error => {
      // 静默处理播放错误
    });
  }
};

// 获取媒体URL - 使用工具函数
const getMediaUrl = (media) => {
  return getMediaFileUrl(media);
};

// 格式化日期
const formatDate = (dateString) => {
  try {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  } catch (error) {
    return '';
  }
};

// 跳转到媒体详情页
const goToMediaDetail = (media) => {
  // 使用工具函数存储媒体数据到sessionStorage
  storeMediaToSession(media);
  
  // 跳转到详情页
  router.push({ name: 'MediaDetail', params: { id: media.id } });
};

// 加载媒体数据 - 调用后端API获取最新媒体
const loadRecentMedia = async () => {
  loading.value = true;
  try {
    // 调用后端API获取最新媒体，按照ID降序排序
    const response = await axios.get(`/api/media/recent?page=1&limit=${pageSize.value}&sort=id&order=desc`);
    
    if (response.data.code === 1) {
      // 成功获取数据
      const rawData = response.data.data || [];
      
      // 处理媒体数据，确保字段格式一致
      mediaList.value = processMediaData(rawData);
      page.value = 1; // 重置页码
    } else {
      // API调用成功但数据有问题
      ElMessage.warning('获取媒体数据失败');
      mediaList.value = [];
      page.value = 1;
    }
  } catch (error) {
    ElMessage.error('网络错误，加载失败');
    mediaList.value = [];
  } finally {
    loading.value = false;
  }
};

// 加载更多媒体数据
const loadMoreMedia = async () => {
  if (loadingMore.value) return;
  
  loadingMore.value = true;
  try {
    // 增加页码并请求下一批数据，确保按照ID降序排序
    const nextPage = page.value + 1;
    const response = await axios.get(`/api/media/recent?page=${nextPage}&limit=${pageSize.value}&sort=id&order=desc`);
    
    if (response.data.code === 1) {
      const rawData = response.data.data || [];
      if (rawData.length > 0) {
        // 将新数据追加到现有列表
        const newMedia = processMediaData(rawData);
        mediaList.value = [...mediaList.value, ...newMedia];
        page.value = nextPage;
      } else {
        ElMessage.info('没有更多媒体数据了');
      }
    } else {
      ElMessage.warning('加载更多媒体数据失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，加载更多失败');
  } finally {
    loadingMore.value = false;
  }
};

// 处理视频加载完成事件
const handleVideoLoaded = (event, mediaId) => {
  const videoElement = event.target;
  
  // 为视频元素添加data-id属性，便于后续查找
  videoElement.setAttribute('data-id', mediaId);
  
  // 确保视频比例正确
  if (videoElement.videoWidth && videoElement.videoHeight) {
    videoElement.style.objectFit = 'contain';
  }
  
  // 标记视频已加载
  videoLoaded.value[mediaId] = true;
  
  // 尝试抓取视频的第一帧作为缩略图
  try {
    // 创建Canvas元素来绘制视频帧
    const canvas = document.createElement('canvas');
    canvas.width = videoElement.videoWidth || 320; // 使用视频宽度或默认值
    canvas.height = videoElement.videoHeight || 240; // 使用视频高度或默认值
    
    // 获取Canvas上下文并绘制视频当前帧
    const ctx = canvas.getContext('2d');
    ctx.drawImage(videoElement, 0, 0, canvas.width, canvas.height);
    
    // 将Canvas内容转换为Base64编码的图片URL
    const thumbnailUrl = canvas.toDataURL('image/jpeg');
    
    // 存储缩略图URL
    videoThumbnails.value[mediaId] = thumbnailUrl;
    
    // 视频加载成功后可以暂停，节省带宽
    videoElement.pause();
    
  } catch (error) {
    // 缩略图生成失败时的后备方案
    videoThumbnails.value[mediaId] = getVideoPlaceholder(mediaId);
  }
};

// 处理视频加载错误事件
const handleVideoError = (event, mediaId) => {
  const video = event.target;
  const errorCode = event.target.error ? event.target.error.code : 'unknown';
  
  // 增加重试计数
  if (!video.retryCount) {
    video.retryCount = 0;
  }
  video.retryCount++;
  
  // 最大重试次数为3次
  if (video.retryCount <= 3) {
    setTimeout(() => {
      // 检测是否为Edge浏览器
      const isEdge = navigator.userAgent.indexOf('Edge/') !== -1 || navigator.userAgent.indexOf('Edg/') !== -1;
      
      if (isEdge) {
        
        // 尝试创建新的视频元素
        const parentElement = video.parentNode;
        if (parentElement) {
          const newVideo = document.createElement('video');
          
          // 复制所有属性
          Array.from(video.attributes).forEach(attr => {
            newVideo.setAttribute(attr.name, attr.value);
          });
          
          // 不要硬编码type属性，应该保持从原始视频元素复制的type属性
          // newVideo.setAttribute('type', 'video/mp4');
          newVideo.setAttribute('playsinline', '');
          newVideo.setAttribute('muted', 'false');
          newVideo.setAttribute('data-id', mediaId);
          
          // 设置源地址并添加时间戳防止缓存
          const originalSrc = video.src ? video.src.split('?')[0] : '';
          const timestamp = Date.now();
          newVideo.src = `${originalSrc}?t=${timestamp}`;
          
          // 复制事件监听器
          newVideo.addEventListener('loadeddata', (e) => handleVideoLoaded(e, mediaId));
          newVideo.addEventListener('error', (e) => handleVideoError(e, mediaId));
          
          // 替换原始视频元素
          parentElement.replaceChild(newVideo, video);
          
          // 尝试加载
          try {
            newVideo.load();
          } catch (error) {
            // 静默处理错误，保持用户体验
          }
        }
      } else {
        // 其他浏览器的标准处理
        video.muted = false;
        const originalSrc = video.src ? video.src.split('?')[0] : '';
        const timestamp = Date.now();
        video.src = '';
        video.src = `${originalSrc}?t=${timestamp}`;
        video.load();
      }
    }, 1000 * video.retryCount); // 指数退避策略
  } else {
    // 静默处理已达到最大重试次数的情况
    // 显示备用占位图
    videoThumbnails.value[mediaId] = getVideoPlaceholder(mediaId);
  }
};

// 跳转到指定页码
const goToPage = async (targetPage) => {
  if (targetPage < 1 || loadingMore.value) return;
  
  loadingMore.value = true;
  try {
    const response = await axios.get(`/api/media/recent?page=${targetPage}&limit=${pageSize.value}&sort=id&order=desc`);
    
    if (response.data.code === 1) {
      const rawData = response.data.data || [];
      if (rawData.length > 0) {
        mediaList.value = processMediaData(rawData);
        page.value = targetPage;
        
        // 滚动到页面顶部
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        ElMessage.info('没有更多媒体数据了');
      }
    } else {
      ElMessage.warning('加载媒体数据失败');
    }
  } catch (error) {
    ElMessage.error('网络错误，加载失败');
  } finally {
    loadingMore.value = false;
  }
};

// 页面挂载时加载数据
onMounted(() => {
  loadRecentMedia();
});
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

/* 最新上传标题区域样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #000;
  margin: 0;
}

.more-btn {
  background: none;
  border: none;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.3s ease;
}

.more-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
  color: #333;
}

.more-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.media-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  width: 100%;
}

.media-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 0;
}

.media-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.15);
}

/* 媒体缩略图容器基础样式 */
.media-thumbnail {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
  background-color: #000;
}

/* 图片和视频共用样式 */
.media-thumbnail img,
.media-thumbnail video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}

/* 视频元素特殊样式 */
.media-thumbnail video {
  z-index: 5;
  min-height: 200px;
}

/* 视频预览容器 - 与HTML结构匹配 */
.video-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 视频缩略图容器 */
.video-thumbnail-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: #000;
  z-index: 2;
}

/* 视频缩略图 */
.video-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 播放覆盖层 */
.play-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background-color: rgba(0, 0, 0, 0.3);
  border-radius: 50%;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.3s ease;
  z-index: 10;
}

.play-overlay:hover {
  background-color: rgba(0, 0, 0, 0.7);
}

.play-icon {
  color: white;
  font-size: 24px;
  margin-left: 5px;
}

/* 文件占位符 */
.file-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  color: #666;
  font-size: 14px;
  padding: 10px;
  text-align: center;
}

/* 媒体信息区域 */
.media-info {
  padding: 15px;
}

.media-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.media-description {
  font-size: 13px;
  color: #666;
  margin: 0 0 5px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.4;
}

.media-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}

.media-uploader {
  font-size: 14px;
  color: #666;
}

.media-date {
  font-size: 12px;
  color: #999;
}

/* 底部控制区域样式 */
.bottom-controls {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

/* 分页器样式 */
.pagination {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.pagination-info {
  font-size: 14px;
  color: #666;
}

.pagination-controls {
  display: flex;
  gap: 10px;
}

.pagination-btn {
  background: none;
  border: 1px solid #ddd;
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  font-size: 14px;
  transition: all 0.3s ease;
}

.pagination-btn:hover:not(:disabled) {
  background-color: #f0f0f0;
  color: #333;
  border-color: #ccc;
}

.pagination-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }
  
  .home-container {
    padding: 15px;
  }
  
  .pagination-controls {
    flex-wrap: wrap;
    justify-content: center;
  }
}
</style>