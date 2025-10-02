<template>
  <div class="public-media-container">
    <LeftSidebar />
    <div class="main-content">
      <div class="header-section">
        <h2>公共相册</h2>
        <div class="search-container">
      <el-input v-model="searchKeyword" placeholder="搜索图片、视频或标签" clearable @input="handleSearch" />
    </div>
      </div>
      
      <div v-if="userStore.canViewPublicMedia" class="media-grid">
        <div v-if="loading" class="loading-container">
          <el-loading-spinner size="large" />
          <p>加载中...</p>
        </div>
        
        <div v-else-if="publicMedia.length > 0" class="media-items">
          <div v-for="media in publicMedia" :key="media.id" class="media-card">
        <div class="media-thumbnail" @click="viewMediaDetail(media.id)">
          <!-- 根据实际数据结构，使用filePath来构建URL -->
          <!-- 按照用户要求，直接使用后台返回的coverPath或filePath作为URL -->
          <img 
            v-if="isImage(media.fileType)" 
            :src="getMediaUrl(media.coverPath || media.filePath)"
            :alt="media.title || media.description || '媒体文件'"
            @error="onImageError"
            @load="onImageLoad"
          />
          <!-- 视频直接使用filePath作为URL -->
          <video v-else-if="isVideo(media.fileType)" controls :src="getMediaUrl(media.filePath)" />
          <div v-else class="file-icon">
            <el-icon><document /></el-icon>
          </div>
        </div>
        
        <div class="media-info">
          <!-- 优先使用title字段，如果不存在则使用其他可能包含标题信息的字段 -->
          <h3>{{ media.title || media.name || media.originalName || media.description || '未命名' }}</h3>
          <p class="upload-time">{{ formatDate(media.createTime) }}</p>
              
              <div class="tags">
                <el-tag v-for="tag in media.tags" :key="tag.id" size="small" type="primary" effect="light">
                  {{ tag.name }}
                </el-tag>
              </div>
            </div>
            
            <div class="media-actions">
              <el-button type="primary" size="small" @click="downloadMedia(media.id, media.fileName)">
                <el-icon><download /></el-icon>
                下载
              </el-button>
            </div>
          </div>
        </div>
        
        <div v-else class="empty-state">
          <el-empty description="暂无公共媒体文件" />
        </div>
        
        <div v-if="publicMedia.length > 0" class="pagination">
          <el-pagination
            v-model:current-page="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[12, 24, 48]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
      
      <div v-else class="access-denied">
        <el-empty description="只有亲密好友可以查看公共媒体" />
        <el-button type="primary" @click="$router.push('/login')">登录</el-button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { get } from '../utils/request.js';
import LeftSidebar from '../components/LeftSidebar.vue';
import useUserStore from '../store/userState.js';
import { Document, Download } from '@element-plus/icons-vue';

export default {
  name: 'PublicMedia',
  components: {
    LeftSidebar,
    Document,
    Download
  },
  setup() {
    const userStore = useUserStore;
    const publicMedia = ref([]);
    const loading = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(12);
    const total = ref(0);
    const searchKeyword = ref('');
    
    // 强制刷新媒体列表
    const forceRefreshMedia = () => {
      // 立即重新加载数据
      currentPage.value = 1;
      loadPublicMedia();
    };

    // 加载公共媒体文件
    const loadPublicMedia = async () => {
      loading.value = true;
      try {
        const response = await get('/api/media/public-media', {
          page: currentPage.value,
          size: pageSize.value,
          keyword: searchKeyword.value
        });
        
        if (response.code === 1 || response.code === 200) {
          // 后端直接返回媒体列表在data字段中
          publicMedia.value = response.data || [];
          total.value = response.total || 0;
          
          if (publicMedia.value.length === 0) {
            ElMessage.info('没有找到媒体文件');
          }
        } else {
          ElMessage.error(response.message || '加载失败');
        }
      } catch (error) {
        ElMessage.error('加载失败，请稍后重试');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadPublicMedia();
    };
    
    // 分页大小变化
    const handleSizeChange = (size) => {
      pageSize.value = size;
      loadPublicMedia();
    };
    
    // 当前页码变化
    const handleCurrentChange = (current) => {
      currentPage.value = current;
      loadPublicMedia();
    };
    
    // 查看媒体详情
    const viewMediaDetail = (mediaId) => {
      // 这里可以跳转到媒体详情页面或打开弹窗
      ElMessage.info('查看媒体详情功能待实现');
    };
    
    // 下载媒体文件
    const downloadMedia = async (mediaId, fileName) => {
      try {
        const response = await get(`/api/download-media/${mediaId}`, {}, {
          responseType: 'blob'
        });
        
        const url = window.URL.createObjectURL(new Blob([response]));
        const link = document.createElement('a');
        link.href = url;
        // 提供文件名的回退选项
        link.setAttribute('download', fileName || `media_${mediaId}`);
        document.body.appendChild(link);
        link.click();
        // 使用setTimeout确保点击事件完成后再清理
        setTimeout(() => {
          document.body.removeChild(link);
          window.URL.revokeObjectURL(url);
        }, 0);
        
        ElMessage.success('下载成功');
      } catch (error) {
        ElMessage.error('下载失败，请稍后重试');
      }
    };
      
      // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return '';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      }).format(date);
    };
    
    // 判断是否为图片
    const isImage = (fileType) => {
      return ['jpg', 'jpeg', 'png', 'gif', 'bmp'].includes(fileType.toLowerCase());
    };
    
    // 判断是否为视频
    const isVideo = (fileType) => {
      return ['mp4', 'avi', 'mov', 'wmv'].includes(fileType.toLowerCase());
    };
    
    // 获取媒体URL
    // 将数据库中的绝对路径转换为前端可访问的相对路径
    const getMediaUrl = (filePath) => {
      if (!filePath) return '';
      
      // 从完整路径中提取文件名
      // 处理Windows路径和Unix路径格式
      const fileName = filePath.split(/[\\/]/).pop();
      
      // 返回可通过HTTP访问的路径
      // 根据application.yml配置，媒体文件存储在c:/dev/media-files/
      // 前端通过/api/media-files/路径访问
      return `/api/media-files/${fileName}`;
    };
    
    // 图片加载错误处理 - 按照用户要求，不使用默认图片，让浏览器显示"资源找不到"状态
    const onImageError = (event) => {
      // 不设置默认图片，让浏览器显示"资源找不到"的状态
    };
    
    // 图片加载成功处理
    const onImageLoad = (event) => {
      // 加载成功处理，保持函数存在但移除调试信息
    };
    
    // 组件挂载时加载数据
    onMounted(() => {
      if (userStore.canViewPublicMedia) {
        loadPublicMedia();
      }
    });
    
    return {
      userStore,
      publicMedia,
      loading,
      currentPage,
      pageSize,
      total,
      searchKeyword,
      loadPublicMedia,
      handleSearch,
      handleSizeChange,
      handleCurrentChange,
      viewMediaDetail,
      downloadMedia,
      formatDate,
      isImage,
      isVideo,
      getMediaUrl,
      onImageError
    };
  };
};
</script>

<style scoped>
.public-media-container {
  display: flex;
  min-height: 100vh;
  background-color: #f9f9f9;
}

.main-content {
  flex: 1;
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  background-color: #fff;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-section h2 {
  margin: 0;
  color: #333;
  font-size: 24px;
  font-weight: bold;
}

.search-container {
  width: 400px;
}

.media-grid {
  background-color: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.media-items {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.media-card {
  border: 1px solid #e6e6e6;
  border-radius: 10px;
  overflow: hidden;
  transition: all 0.3s ease;
  background-color: #fff;
}

.media-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.media-thumbnail {
  width: 100%;
  height: 180px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.media-thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.media-thumbnail:hover img {
  transform: scale(1.05);
}

.media-thumbnail video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.file-icon {
  font-size: 48px;
  color: #909399;
}

.media-info {
  padding: 15px;
}

.media-info h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.upload-time {
  margin: 0 0 10px 0;
  font-size: 12px;
  color: #909399;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
}

.media-actions {
  padding: 0 15px 15px 15px;
}

.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.access-denied {
  background-color: #fff;
  border-radius: 10px;
  padding: 60px 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.access-denied .el-button {
  margin-top: 20px;
}

/* 儿童风格的设计元素 */
.media-card {
  position: relative;
  border-radius: 12px;
  border: 2px solid #ffcccc;
}

.media-card:nth-child(odd) {
  border-color: #ccffcc;
}

.media-card:nth-child(3n) {
  border-color: #ccccff;
}

.media-card:nth-child(4n) {
  border-color: #ffffcc;
}

.media-card:nth-child(5n) {
  border-color: #ffccff;
}
</style>