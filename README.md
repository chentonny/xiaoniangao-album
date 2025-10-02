# 小年糕相册 - 多媒体相册管理系统

## 📸 项目概述

小年糕相册是一款专为儿童和家庭设计的多媒体相册管理系统，提供简洁友好的界面和丰富的功能，让用户可以轻松上传、管理、分享和欣赏照片和视频。

### 主要特点

- 🎨 儿童友好的界面设计，色彩明亮活泼，操作简单直观
- 📤 支持多种格式的图片和视频上传、预览和管理
- 🏷️ 灵活的标签系统，方便分类和查找媒体文件
- 👨‍👩‍👧‍👦 多用户角色支持（管理员、普通用户、访客），确保内容安全
- 📱 响应式设计，完美适配桌面端和移动端
- 🔐 完善的用户认证和权限控制机制

## 🛠️ 技术架构

项目采用前后端分离的架构设计：

### 前端技术栈

- **框架**: Vue 3 + Composition API
- **UI组件库**: Element Plus
- **构建工具**: Vite
- **HTTP客户端**: Axios
- **路由**: Vue Router
- **状态管理**: Pinia
- **样式**: CSS3

### 后端技术栈

- **框架**: Spring Boot 2.7.5
- **数据库**: MySQL
- **ORM**: MyBatis
- **安全框架**: Spring Security
- **API文档**: Swagger

## 📁 项目结构

```
├── frontend/            # 前端项目目录
│   ├── src/             # 前端源代码
│   │   ├── components/  # 公共组件
│   │   ├── views/       # 页面视图
│   │   ├── store/       # 状态管理
│   │   ├── utils/       # 工具函数
│   │   ├── middleware/  # 中间件
│   │   ├── assets/      # 静态资源
│   │   ├── router.js    # 路由配置
│   │   ├── main.js      # 入口文件
│   │   └── App.vue      # 根组件
│   ├── public/          # 静态资源目录
│   ├── dist/            # 构建输出目录
│   ├── package.json     # 前端依赖配置
│   └── vite.config.js   # Vite配置文件
├── backend/             # 后端项目目录
│   ├── src/             # 后端源代码
│   │   └── main/        # 主要代码
│   │       ├── java/    # Java源代码
│   │       └── resources/ # 配置文件
│   ├── target/          # 构建输出目录
│   └── pom.xml          # Maven依赖配置
├── media-files/         # 媒体文件存储目录
├── create_tag_table.sql # 标签表创建SQL
└── add_tags.sql         # 标签数据初始化SQL
```

## 🔧 开发指南

### 前置要求

- **前端**: Node.js 16+、npm 8+
- **后端**: JDK 1.8+、Maven 3.6+、MySQL 5.7+

### 前端开发环境搭建

1. 克隆项目代码

```bash
git clone <项目仓库地址>
cd 小年糕相册/frontend
```

2. 安装依赖

```bash
npm install
```

3. 启动开发服务器

```bash
npm run dev
```

4. 构建生产版本

```bash
npm run build
```

5. 预览生产版本

```bash
npm run preview
```

### 后端开发环境搭建

1. 进入后端目录

```bash
cd 小年糕相册/backend
```

2. 配置数据库
   - 创建MySQL数据库
   - 修改`src/main/resources/application.properties`中的数据库连接配置

3. 初始化数据库

```bash
# 导入数据表结构和初始数据
source create_tag_table.sql
source add_tags.sql
```

4. 启动后端服务

```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用编译后的jar包启动
mvn clean package
java -jar target/xiaoniangao-album-1.0-SNAPSHOT.jar
```

### 开发配置

#### 前端代理配置

前端通过Vite的代理配置将`/api`请求转发到后端服务。配置位于`vite.config.js`文件中：

```javascript
export default defineConfig({
  // ...
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        // 路径重写，将/api前缀移除
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

## 🚀 功能模块

### 用户管理模块

- 用户注册、登录和登出
- 用户信息管理
- 密码修改
- 权限控制

### 媒体文件管理模块

- 图片和视频上传
- 文件预览和播放
- 文件删除和下载
- 批量操作
- 文件信息编辑

### 标签管理模块

- 创建和删除标签
- 为媒体文件添加和移除标签
- 通过标签筛选媒体文件

### 搜索模块

- 根据文件名、标签等条件搜索媒体文件
- 分页展示搜索结果

## 👨‍💻 开发规范

### 代码规范

- 前端遵循Vue 3 Composition API风格
- 组件命名采用大驼峰命名法
- 函数和变量命名采用小驼峰命名法
- 代码注释清晰明了

### 提交规范

推荐使用以下格式提交代码：

```
[类型]: [简要描述]

[详细描述]
```

类型包括：

- `feat`: 新增功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码风格调整
- `refactor`: 代码重构
- `test`: 测试代码
- `chore`: 构建过程或辅助工具变动

## 🔍 API文档

项目集成了Swagger用于API文档管理。启动后端服务后，可以通过以下地址访问API文档：

```
http://localhost:8082/api/swagger-ui.html
```

## 📝 注意事项

1. 媒体文件上传大小限制：单文件最大50MB
2. 支持的图片格式：jpg、jpeg、png、gif、bmp
3. 支持的视频格式：mp4、webm、mov、avi、mkv、wmv、flv、m4v
4. 开发环境需要确保前后端服务都正常运行
5. 生产环境需要配置正确的数据库连接信息和文件存储路径

## 🤝 贡献指南

1. Fork项目仓库
2. 创建新的分支 (`git checkout -b feature/xxxx`)
3. 提交代码变更 (`git commit -am 'Add some feature'`)
4. 推送到分支 (`git push origin feature/xxxx`)
5. 创建Pull Request

## 📄 License

本项目采用MIT许可证 - 查看LICENSE文件了解详情

## 📞 联系我们

如有任何问题或建议，欢迎联系我们：

- 项目维护者: [项目维护者名称]
- 邮箱: [维护者邮箱]
- GitHub: [项目GitHub地址]

---

⭐️ 如果您喜欢这个项目，请给我们点个星，谢谢支持！
