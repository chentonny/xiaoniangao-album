// 媒体工具模块 - 提供统一的媒体数据处理功能

/**
 * 处理媒体数据，确保包含所有必要字段
 * @param {Object} media - 原始媒体数据对象
 * @param {Object} userInfo - 用户信息对象(可选)
 * @returns {Object} 处理后的媒体数据对象
 */
export const processMediaData = (media, userInfo = null) => {
  if (!media) return null;
  
  // 标准化媒体数据，确保所有需要的字段都存在
  return {
    id: media.id,
    // 媒体类型相关字段
    type: media.fileType || media.type,
    fileType: media.fileType || media.type,
    fileSize: media.fileSize,
    
    // 标题/描述相关字段
    title: media.fileTitle || media.title || '未命名',
    fileDescription: media.fileTitle || media.title || '未命名',
    description: media.fileTitle || media.title || '未命名',
    
    // 日期相关字段
    createdAt: media.createTime || media.createdAt || media.uploadTime || '',
    createTime: media.createTime || media.createdAt || media.uploadTime || '',
    uploadTime: media.createTime || media.createdAt || media.uploadTime || '',
    
    // 文件路径相关字段
    filename: media.fileName || media.filename || '',
    filePath: media.filePath || '',
    
    // 标签字段
    tags: media.tags || media.fileTag || '',
    fileTag: media.tags || media.fileTag || '',
    
    // 上传者相关字段
    uploaderName: getUploaderName(media, userInfo)
  };
};

/**
 * 从媒体数据中获取上传者名称
 * @param {Object} media - 媒体数据对象
 * @param {Object} userInfo - 用户信息对象(可选)
 * @returns {string} 上传者名称
 */
export const getUploaderName = (media, userInfo = null) => {
  // 尝试从多个可能的字段中获取上传者名称
  return (
    media.uploaderName || 
    media.userName || 
    media.uploader || 
    (media.user && (media.user.username || media.user.name)) ||
    (userInfo && (userInfo.userName || userInfo.username || userInfo.name)) ||
    '未知'
  );
};

/**
 * 从文件路径中提取文件名
 * @param {string} filePath - 文件路径
 * @returns {string} 文件名
 */
export const extractFilename = (filePath) => {
  if (!filePath) return '';
  
  // 处理文件路径，提取文件名（同时支持Windows和Unix路径格式）
  const lastSlashIndex = Math.max(
    filePath.lastIndexOf('/'),
    filePath.lastIndexOf('\\')
  );
  return lastSlashIndex >= 0 ? 
    filePath.substring(lastSlashIndex + 1) : 
    filePath;
};

/**
 * 构建媒体文件的URL路径
 * @param {Object} media - 媒体数据对象
 * @returns {string} 媒体URL
 */
export const getMediaUrl = (media) => {
  if (!media) return '';
  
  // 优先从filePath中提取文件名
  const fileName = extractFilename(media.filePath);
  
  // 如果filePath中没有文件名，尝试使用filename字段
  const finalFileName = fileName || media.filename || '';
  
  // 使用统一的相对路径格式访问媒体文件
  return finalFileName ? `/media-files/${finalFileName}` : '';
};

/**
 * 判断是否为图片文件
 * @param {Object} media - 媒体数据对象
 * @returns {boolean} 是否为图片
 */
export const isImageFile = (media) => {
  if (!media) return false;
  const mediaType = media.fileType || media.type;
  return mediaType && mediaType.includes('image');
};

/**
 * 判断是否为视频文件
 * @param {Object} media - 媒体数据对象
 * @returns {boolean} 是否为视频
 */
export const isVideoFile = (media) => {
  if (!media) return false;
  const mediaType = media.fileType || media.type;
  return mediaType && mediaType.includes('video');
};

/**
 * 格式化文件大小
 * @param {number} bytes - 文件大小(字节)
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes) => {
  if (!bytes || bytes < 0) return '未知';
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  return (bytes / (1024 * 1024 * 1024)).toFixed(1) + ' GB';
};

/**
 * 格式化日期
 * @param {string|number} dateString - 日期字符串或时间戳
 * @returns {string} 格式化后的日期
 */
export const formatDate = (dateString) => {
  if (!dateString) return '未知';
  try {
    const d = new Date(dateString);
    return d.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  } catch (error) {
    return '未知';
  }
};

/**
 * 将媒体数据存储到sessionStorage
 * @param {Object} media - 媒体数据对象
 */
export const storeMediaToSession = (media) => {
  try {
    // 处理媒体数据，确保格式一致
    const processedMedia = processMediaData(media);
    // 使用统一的键名存储到sessionStorage
    sessionStorage.setItem('selectedMedia', JSON.stringify(processedMedia));
  } catch (error) {
    console.error('存储媒体数据到sessionStorage失败:', error);
  }
};

/**
 * 从sessionStorage获取媒体数据
 * @returns {Object|null} 媒体数据对象或null
 */
export const getMediaFromSession = () => {
  try {
    const storedMedia = sessionStorage.getItem('selectedMedia');
    if (storedMedia) {
      return JSON.parse(storedMedia);
    }
  } catch (error) {
    console.error('从sessionStorage读取媒体数据失败:', error);
  }
  return null;
};



/**
 * 获取媒体类型标签
 * @param {Object} media - 媒体数据对象
 * @returns {string} 媒体类型标签
 */
export const getMediaTypeLabel = (media) => {
  if (isImageFile(media)) {
    return '图片';
  } else if (isVideoFile(media)) {
    return '视频';
  } else {
    return '文件';
  }
};

/**
 * 获取正确的MIME类型
 * @param {string} fileName - 文件名
 * @returns {string} MIME类型
 */
export const getCorrectMimeType = (fileName) => {
  if (!fileName) return 'video/mp4'; // 默认返回标准视频MIME类型
  
  const ext = fileName.split('.').pop().toLowerCase();
  
  if (ext === 'mp4') return 'video/mp4';
  else if (ext === 'webm') return 'video/webm';
  else if (ext === 'ogg') return 'video/ogg';
  else if (ext === 'jpg' || ext === 'jpeg') return 'image/jpeg';
  else if (ext === 'png') return 'image/png';
  else if (ext === 'gif') return 'image/gif';
  
  return 'application/octet-stream';
};