<template>
  <div class="my-media">
    <LeftSidebar />
    
    <div class="main-content">
      <h2>公共资源</h2>
      
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
              <el-button size="small" type="primary" @click.stop="downloadMedia(media.id)">下载</el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="mediaList.length === 0" class="empty-state">
        <p>暂无公共媒体资源</p>
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
  getMediaUrl as getMediaFileUrl,
  isImageFile,
  isVideoFile,
  getMediaTypeLabel,
  storeMediaToSession
} from '../utils/mediaUtils.js';

export default defineComponent({
  name: 'PublicMedia',
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
      loadPublicMedia()
    })
    
    const loadPublicMedia = async () => {
      try {
        const response = await axios.get(`/api/media/public-media?keyword=${keyword.value}&page=${currentPage.value}&size=${pageSize.value}`)
        if (response.data.code === 1) {
          // 处理媒体数据，确保字段格式一致
          const processedMedia = await processMediaData(response.data.data);
          
          mediaList.value = processedMedia;
          total.value = response.data.total
        } else {
          ElMessage.error('加载媒体失败: ' + (response.data.message || '未知错误'))
          console.error('API返回错误:', response.data);
        }
      } catch (error) {
        ElMessage.error('加载媒体失败: 网络错误')
        console.error('网络错误详情:', error);
      }
    }
    
    // 存储用户信息的映射表
    const userMap = ref({});
    
    // 通过userId获取用户名的函数
    const getUsernameById = async (userId) => {
      // 如果已经缓存了该用户的信息，直接返回
      if (userMap.value[userId]) {
        return userMap.value[userId];
      }
      
      try {
        // 调用后端新添加的API根据userId查询用户基本信息
        const response = await axios.get(`/api/user/${userId}`);
        
        if (response.data && response.data.code === 1 && response.data.data) {
          // 获取实际用户名
          const username = response.data.data.userName || response.data.data.username || 
                          response.data.data.nickname || `用户${userId}`;
                          
          if (username) {
            // 缓存用户信息
            userMap.value[userId] = username;
            return username;
          }
        } else {
          // 添加一些默认的用户名，以防后端不可用
          const defaultUsernames = {
            1: '管理员',
            2: '张三',
            3: '李四',
            4: '王五',
            5: '赵六'
          };
          // 缓存默认用户名
          userMap.value[userId] = defaultUsernames[userId] || `用户${userId}`;
          return userMap.value[userId];
        }
      } catch (error) {
        console.error(`获取用户ID ${userId} 信息失败:`, error);
        // 网络错误，返回默认用户名
        const defaultUsernames = {
          1: '管理员',
          2: '张三',
          3: '李四',
          4: '王五',
          5: '赵六'
        };
        // 缓存默认用户名
        userMap.value[userId] = defaultUsernames[userId] || `用户${userId}`;
        return userMap.value[userId];
      }
    };
    
    // 处理媒体数据，确保字段格式一致
    const processMediaData = async (mediaData) => {
      // 先创建一个包含所有基本信息的媒体数组
      const processedMediaArray = mediaData.map(media => {
        // 从多个可能的字段名中获取用户ID
        const userId = media.userId || media.userId || media.user_id || media.userID;
        
        return {
          id: media.id,
          type: media.fileType,
          fileType: media.fileType,
          fileSize: media.fileSize,
          title: media.fileTitle || media.title || '未命名',
          createdAt: media.createTime || media.create_time || media.createdAt,
          filename: media.fileName || media.filename,
          filePath: media.filePath,
          userId: userId,
          uploaderName: '未知用户', // 初始显示未知用户
          description: media.description || ''
        };
      });
      
      // 对于有userId的媒体，异步获取用户名
      const promises = processedMediaArray.map(async (media) => {
        if (media.userId) {
          try {
            // 使用userId查询用户名
            const username = await getUsernameById(media.userId);
            media.uploaderName = username;
          } catch (error) {
            console.error(`获取用户ID ${media.userId} 的信息失败:`, error);
            // 考虑到后端可能不可用，添加一些默认的用户名
            const defaultUsernames = {
              1: '管理员',
              2: '张三',
              3: '李四',
              4: '王五',
              5: '赵六'
            };
            media.uploaderName = defaultUsernames[media.userId] || `用户${media.userId}`;
          }
        } else {
          media.uploaderName = '未知用户';
        }
      });
      
      // 等待所有用户名获取完成
      await Promise.all(promises);
      
      return processedMediaArray;
    };
    
    const searchMedia = () => {
      currentPage.value = 1
      loadPublicMedia()
    }
    
    const getMediaUrl = (media) => {
      return getMediaFileUrl(media);
    }
    
    const formatDate = (dateString) => {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN')
    }
    
    // 不再需要getUploaderName函数，直接使用media.uploaderName属性
    
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
    
    // 处理视频加载错误
    const handleVideoError = (event, mediaId) => {
      ElMessage.error(`视频加载失败，媒体ID: ${mediaId}`);
      console.error('视频加载错误:', event);
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
    
    const downloadMedia = async (id) => {
      try {
        const response = await axios.get(`/api/download-media/${id}`, {
          responseType: 'blob'
        })
        
        // 创建下载链接
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', `media_${id}`)
        document.body.appendChild(link)
        link.click()
        
        // 清理
        document.body.removeChild(link)
        window.URL.revokeObjectURL(url)
        
        ElMessage.success('下载成功')
      } catch (error) {
        ElMessage.error('下载失败')
      }
    }
    
    const handleSizeChange = (size) => {
      pageSize.value = size
      loadPublicMedia()
    }
    
    const handleCurrentChange = (current) => {
      currentPage.value = current
      loadPublicMedia()
    }
    
    // 强制刷新数据
    const forceRefresh = () => {
      currentPage.value = 1;
      loadPublicMedia();
    }
    
    return {
      userStore,
      mediaList,
      keyword,
      currentPage,
      pageSize,
      total,
      loadPublicMedia,
      searchMedia,
      getMediaUrl,
      formatDate,
      isImage,
      isVideo,
      getMediaTypeLabel,
      handleImageError,
      handleVideoError,
      viewMediaDetail,
      downloadMedia,
      handleSizeChange,
      handleCurrentChange,
      forceRefresh,
      getUsernameById
    }
  }
})
</script>

<style scoped>
.my-media {
  display: flex;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.main-content {
  margin-left: 10px; /* 与左侧导航栏保持适当距离，避免重叠 */
  padding: 30px;
}

.main-content h2 {
  font-size: 28px;
  color: #333;
  margin-bottom: 20px;
}

.search-container {
  margin-bottom: 20px;
  display: flex;
  justify-content: center;
}

.search-input {
  width: 500px;
  max-width: 100%;
}

.media-actions {
  margin-top: 10px;
  display: flex;
  gap: 10px;
}

.empty-state {
  text-align: center;
  padding: 100px 0;
  color: #999;
  font-size: 18px;
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
.video-element {
  object-fit: cover;
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