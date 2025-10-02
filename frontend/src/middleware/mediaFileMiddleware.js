import path from 'path';
import { statSync, createReadStream } from 'fs';

/**
 * 增强型媒体文件处理中间件
 * 支持处理前端对 /media-files/ 路径和直接根路径下媒体文件的请求
 * 特别针对Edge浏览器添加了全面的视频识别优化
 */
function mediaFileMiddleware() {
  // 定义支持的媒体文件扩展名
  const mediaExtensions = ['mp4', 'jpg', 'jpeg', 'png', 'gif', 'webp'];
  
  return {
    name: 'media-file-middleware',
    configureServer(server) {
      // 监听所有路径，筛选出媒体文件请求
      server.middlewares.use((req, res, next) => {
        try {
          // 获取请求的URL路径（去掉查询参数）
          if (!req.url) {
            return next();
          }
          
          const urlPath = req.url.split('?')[0];
          const pathParts = urlPath.split('/').filter(Boolean);
          
          if (pathParts.length === 0) {
            return next();
          }
          
          const fileName = pathParts[pathParts.length - 1];
          
          // 检查请求是否为媒体文件
          const fileExt = fileName.split('.').pop()?.toLowerCase();
          
          // 只处理GET请求和特定的媒体文件
          const isRootMediaRequest = pathParts.length === 1 && mediaExtensions.includes(fileExt);
          const isMediaFilesRequest = urlPath.startsWith('/media-files/');
          
          if (req.method !== 'GET' || !(isRootMediaRequest || isMediaFilesRequest)) {
            return next();
          }
          
          // 构建实际的文件路径
          const cleanFileName = isMediaFilesRequest ? fileName : fileName;
          const filePath = path.join(process.cwd(), '..', 'media-files', cleanFileName);
          
          const startTime = Date.now();
          
          // 增强的Edge浏览器检测逻辑
          const userAgent = req.headers['user-agent'] || '';
          const isEdgeBrowser = userAgent.indexOf('Edg') > -1 || userAgent.indexOf('Edge') > -1;
          
          try {
            const stats = statSync(filePath);
            const fileSize = stats.size;
            
            // 设置基础响应头
            res.setHeader('Accept-Ranges', 'bytes');
            res.setHeader('Access-Control-Allow-Origin', '*');
            res.setHeader('Access-Control-Allow-Methods', 'GET, HEAD, OPTIONS');
            res.setHeader('Access-Control-Allow-Headers', 'Range, Cache-Control, Accept, Origin, If-Match, If-Modified-Since, Content-Type');
            
            // 处理范围请求
            let start = 0;
            let end = fileSize - 1;
            
            if (req.headers.range) {
              const parts = req.headers.range.replace(/bytes=/, "").split("-");
              start = parseInt(parts[0], 10);
              end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
              
              if (isNaN(start) || start >= fileSize) {
                res.statusCode = 416;
                res.setHeader('Content-Range', `bytes */${fileSize}`);
                res.end();
                return;
              }
              
              end = Math.min(end, fileSize - 1);
              res.statusCode = 206;
              res.setHeader('Content-Range', `bytes ${start}-${end}/${fileSize}`);
              res.setHeader('Content-Length', end - start + 1);
              
              // 处理范围请求逻辑已实现
            } else {
              res.setHeader('Content-Length', fileSize);
            }
            
            // 根据文件扩展名设置正确的Content-Type并处理特定类型
            if (fileExt === 'jpg' || fileExt === 'jpeg') {
              res.setHeader('Content-Type', 'image/jpeg');
              // 图片文件缓存一天
              res.setHeader('Cache-Control', 'public, max-age=86400, must-revalidate');
              res.setHeader('Expires', new Date(Date.now() + 86400000).toUTCString());
            } else if (fileExt === 'png') {
              res.setHeader('Content-Type', 'image/png');
              // 图片文件缓存一天
              res.setHeader('Cache-Control', 'public, max-age=86400, must-revalidate');
              res.setHeader('Expires', new Date(Date.now() + 86400000).toUTCString());
            } else if (fileExt === 'gif') {
              res.setHeader('Content-Type', 'image/gif');
              // 图片文件缓存一天
              res.setHeader('Cache-Control', 'public, max-age=86400, must-revalidate');
              res.setHeader('Expires', new Date(Date.now() + 86400000).toUTCString());
            } else if (fileExt === 'webp') {
              res.setHeader('Content-Type', 'image/webp');
              // 图片文件缓存一天
              res.setHeader('Cache-Control', 'public, max-age=86400, must-revalidate');
              res.setHeader('Expires', new Date(Date.now() + 86400000).toUTCString());
            } else if (fileExt === 'mp4') {
              // 视频文件特殊处理
              // 如果是Edge浏览器，添加全面的视频优化
              if (isEdgeBrowser) {
                // 核心修复：使用带有编解码器参数的完整视频类型声明
                res.setHeader('Content-Type', 'video/mp4; codecs=\"avc1.4D401E, mp4a.40.2\"');
                res.setHeader('X-Content-Type-Options', 'nosniff');
                // 额外的Edge浏览器优化头
                res.setHeader('X-Edge-MP4-Fix', 'true');
                res.setHeader('X-Edge-Video-Optimization', 'true');
                res.setHeader('Content-Disposition', `inline; filename="${cleanFileName}"`);
                res.setHeader('X-Accel-Buffering', 'no');
                res.setHeader('Access-Control-Expose-Headers', 'Content-Range,Accept-Ranges,Content-Type,Content-Length');
                // 处理范围请求的Edge特定优化
                if (req.headers.range) {
                  res.setHeader('Transfer-Encoding', 'chunked');
                  res.setHeader('Connection', 'keep-alive');
                  res.setHeader('Keep-Alive', 'timeout=300, max=100');
                }
              } else {
                // 非Edge浏览器使用标准视频类型
                res.setHeader('Content-Type', 'video/mp4');
                res.setHeader('X-Content-Type-Options', 'nosniff');
              }
              // 视频文件不缓存，确保每次都获取最新内容
              res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
              res.setHeader('Pragma', 'no-cache');
              res.setHeader('Expires', '0');
            }
            
            // 添加ETag和Last-Modified头
            const lastModified = stats.mtime.toUTCString();
            const etag = `"${fileSize}-${lastModified.replace(/[: ]/g, '-')}"`;
            res.setHeader('Last-Modified', lastModified);
            res.setHeader('ETag', etag);
            
            // 创建文件读取流
            const readStream = createReadStream(filePath, {
              start,
              end,
              highWaterMark: 64 * 1024 // 64KB chunks
            });
            
            // 增加响应缓冲区大小
            res.writeBufferSize = 1024 * 1024; // 1MB
            
            // 管道流到响应
            readStream.pipe(res);
            
            readStream.on('error', (err) => {
              res.statusCode = 500;
              res.end('Internal Server Error');
            });
            
            res.on('finish', () => {
              // 请求完成处理
            });
            
            res.on('close', () => {
              readStream.destroy();
            });
            
            // 设置5分钟超时
            req.setTimeout(300000, () => {
              readStream.destroy();
              res.statusCode = 504;
              res.end('Gateway Timeout');
            });
          } catch (error) {
            res.statusCode = 404;
            res.end('File not found');
          }
        } catch (error) {
          res.statusCode = 500;
          res.end('Internal Server Error');
        }
      });
    }
  };
}

export default mediaFileMiddleware;