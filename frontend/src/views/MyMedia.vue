<template>
  <div class="my-media">
    <LeftSidebar />
    
    <div class="main-content">
      <h2>我的媒体</h2>
      
      <div class="search-container">
        <el-input
          v-model="keyword"
          placeholder="搜索媒体标题或描述"
          class="search-input"
          @keyup.enter="searchMedia"
        >
          <template #append>
              <el-button @click="searchMedia" icon="Search">搜索</el-button>
            </template>
        </el-input>
      </div>
      
      <div class="media-grid">
        <div v-for="media in mediaList" :key="media.id" class="media-item" @click="viewMediaDetail(media.id)">
          
          <div class="media-thumbnail">
            <img 
              v-if="isImage(media)" 
              :src="getMediaUrl(media)" 
              :alt="media.fileTitle || media.title" 
              class="media-thumbnail" 
              @error="handleImageError($event)"
            />
            <div v-else-if="isVideo(media)" class="video-preview-container">
              <video 
                :id="`video-${media.id}`"
                :src="getMediaUrl(media)" 
                class="video-element" 
                controls 
                playsinline 
                muted 
                preload="metadata"
                @error="handleVideoError($event, media.id)"
              ></video>
            </div>
            <div v-else class="file-placeholder">
              {{ media.filename || media.fileName || '未知文件' }}
            </div>
          </div>
          <div class="media-info">
            <h3 class="media-title">{{ media.fileTitle || media.title || '未命名' }}</h3>
            <p v-if="media.description" class="media-description">{{ media.description }}</p>
            <div class="media-meta">
              <span class="media-uploader">{{ media.uploaderName || '未知用户' }}</span>
              <span class="media-date">{{ formatDate(media.create_time || media.createdAt) }}</span>
            </div>
            <div class="media-actions">
              <el-button size="small" type="danger" @click.stop="deleteMedia(media.id)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="mediaList.length === 0" class="empty-state">
        <p>您还没有上传任何媒体</p>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[12, 24, 36]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Document } from '@element-plus/icons-vue'
import LeftSidebar from '../components/LeftSidebar.vue'
import { useUserStore } from '../store/userState.js'
import { useRouter } from 'vue-router'

// 导入媒体工具模块
import {
  getMediaUrl,
  isImageFile,
  isVideoFile,
  getMediaTypeLabel,
  storeMediaToSession
} from '../utils/mediaUtils.js';

export default defineComponent({
  name: 'MyMedia',
  components: {
    LeftSidebar,
    Document
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const mediaList = ref([])
    const keyword = ref('')
    const currentPage = ref(1)
    const pageSize = ref(12)
    const total = ref(0)
    
    onMounted(() => {
      loadMyMedia()
    })
    
    const loadMyMedia = async () => {
      try {
        // 将userID转换为Number类型，确保与后端的Long类型匹配
        const userId = Number(userStore.userInfo?.userID)
        const response = await axios.get(`/api/media/my-media?userId=${userId}&keyword=${keyword.value}&page=${currentPage.value}&size=${pageSize.value}`)
        if (response.data.code === 1) {
          // 处理媒体数据，确保字段格式一致
          mediaList.value = processMediaData(response.data.data)
          total.value = response.data.total
        } else {
          ElMessage.error('加载媒体失败: ' + (response.data.message || '未知错误'))
        }
      } catch (error) {
        ElMessage.error('加载媒体失败: 网络错误')
      }
    }
    
    // 处理媒体数据，确保字段格式一致
    const processMediaData = (mediaData) => {
      return mediaData.map(media => {
        // 获取当前登录用户的名称
        const currentUserName = userStore.userInfo?.userName || userStore.userInfo?.username || userStore.user?.userName || userStore.user?.username || '未知用户';
        
        // 检查是否有userId字段，如果有，可以尝试根据userId获取用户名
        let uploaderName = '未知用户';
        
        // 优先从媒体数据中直接获取上传者名称
        if (media.uploaderName) {
          uploaderName = media.uploaderName;
        } else if (media.uploader) {
          uploaderName = media.uploader;
        } else if (media.userName) {
          uploaderName = media.userName;
        } else if (media.user) {
          uploaderName = media.user;
        } else if (media.uploader_username) {
          uploaderName = media.uploader_username;
        } else if (media.author) {
          uploaderName = media.author;
        } else if (media.creator) {
          uploaderName = media.creator;
        } else if (media.owner) {
          uploaderName = media.owner;
        } else if (media.userId) {
          // 如果有userId字段，使用当前登录用户的名称（因为是"我的资源"页面）
          uploaderName = currentUserName;
        } else {
          // 如果以上字段都没有，使用当前登录用户的名称
          uploaderName = currentUserName;
        }
        
        // 确保media对象包含所有需要的字段
        const processedMedia = {
          id: media.id,
          // 直接使用fileType字段
          type: media.fileType,
          fileType: media.fileType,
          fileSize: media.fileSize,
          title: media.fileTitle || media.title || '未命名',
          createdAt: media.createTime || media.create_time || media.createdAt,
          filename: media.fileName || media.filename,
          filePath: media.filePath,
          // 使用处理后的uploaderName
          uploaderName: uploaderName,
          // 处理描述字段
          description: media.description || ''
        };
        
        return processedMedia;
      });
    };
    
    const searchMedia = () => {
      currentPage.value = 1
      loadMyMedia()
    }
    
    // 直接使用从mediaUtils.js导入的getMediaUrl函数，不需要再包装一层
    
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 判断是否为图片
    const isImage = (media) => {
      return isImageFile(media);
    }
    
    // 判断是否为视频
    const isVideo = (media) => {
      return isVideoFile(media);
    }
    
    // 处理图片加载错误
    const handleImageError = (event) => {
      event.target.src = '/images/default-placeholder.png';
    }
    
    // 处理图片加载成功
    const handleImageLoad = (event) => {
      // 静默处理，不输出日志
    }
    
    // 处理视频加载错误
    const handleVideoError = (event, mediaId) => {
      ElMessage.error(`视频加载失败，媒体ID: ${mediaId}`);
      console.error('视频加载错误:', event);
    }
    
    // 处理视频加载成功
    const handleVideoLoad = (event) => {
      // 静默处理，不输出日志
    }
    
    const viewMediaDetail = (id) => {
      // 找到被点击的媒体对象
      const selectedMedia = mediaList.value.find(media => media.id === id)
      if (selectedMedia) {
        // 使用工具函数存储媒体数据到sessionStorage
        storeMediaToSession(selectedMedia);
      }
      // 使用Vue Router进行导航，确保路径与router.js中定义的一致
      router.push({ name: 'MediaDetail', params: { id } })
    }
    
    const deleteMedia = async (id) => {
      try {
        const response = await axios.delete(`/api/media/delete-media?fileId=${id}&userId=${userStore.userInfo?.userID || userStore.user?.userID}`)
        if (response.data.code === 1) {
          ElMessage.success('删除成功')
          loadMyMedia()
        } else {
          ElMessage.error(response.data.message || '删除失败')
        }
      } catch (error) {
        ElMessage.error('删除失败')
      }
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadMyMedia()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadMyMedia()
    }
    
    // 强制刷新数据
    const forceRefresh = () => {
      currentPage.value = 1;
      loadMyMedia();
    }
    
    return {
      userStore,
      mediaList,
      keyword,
      currentPage,
      pageSize,
      total,
      loadMyMedia,
      searchMedia,
      getMediaUrl,
      formatDate,
      isImage,
      isVideo,
      getMediaTypeLabel,
      handleImageError,
      handleImageLoad,
      handleVideoError,
      handleVideoLoad,
      viewMediaDetail,
      deleteMedia,
      handleSizeChange,
      handleCurrentChange,
      forceRefresh
    }
  }
})
</script>

<style scoped>
.my-media {
  display: flex;
  min-height: 100vh;
}

.main-content {
  margin-left: 10px; /* 与左侧导航栏保持适当距离，避免重叠 */
  padding: 30px;
  max-width: 1200px;
  width: 100%;
}

.main-content h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.search-container {
  margin-bottom: 20px;
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

/* 视频预览容器 */
.video-preview-container {
  position: relative;
  width: 100%;
  height: 100%;
  background-color: #000;
  display: flex;
  align-items: center;
  justify-content: center;
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

.media-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 18px;
}

.pagination-container {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .media-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
  }
  
  .main-content {
    padding: 15px;
  }
}

</style>