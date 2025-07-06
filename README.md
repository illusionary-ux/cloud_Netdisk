# 企业云网盘系统

一个基于Spring Boot + Vue3的企业级云存储网盘系统，提供文件存储、分享、权限管理等功能。

## 📋 项目简介

企业云网盘系统是一个功能完整的云存储解决方案，支持多用户管理、文件上传下载、文件夹管理、文件分享、权限控制等核心功能。系统采用前后端分离架构，后端基于Spring Boot框架，前端使用Vue3 + Element Plus构建现代化的用户界面。

## ✨ 主要功能

### 🔐 用户管理
- **用户注册/登录**: 支持邮箱验证码登录和密码登录
- **角色权限**: 支持访客(guest)、普通用户(user)、VIP用户(vip)、管理员(admin)四种角色
- **权限控制**: 基于RBAC的细粒度权限管理系统
- **用户管理**: 管理员可查看、删除用户，管理用户存储空间

### 📁 文件管理
- **文件上传**: 支持拖拽上传，最大支持500MB文件
- **文件夹管理**: 创建、重命名、删除文件夹
- **文件操作**: 下载、删除、重命名、移动、复制文件
- **文件预览**: 支持图片、PDF等文件在线预览
- **存储管理**: 用户存储空间限制和统计

### 🔗 文件分享
- **文件分享**: 生成分享链接，支持设置有效期
- **分享管理**: 查看、管理已分享的文件
- **接受分享**: 通过分享链接访问他人分享的文件

### ⭐ 收藏功能
- **文件收藏**: 收藏重要文件，快速访问
- **收藏管理**: 统一管理收藏的文件

### 🗑️ 回收站
- **文件回收**: 删除的文件进入回收站
- **文件恢复**: 从回收站恢复误删的文件
- **彻底删除**: 永久删除回收站中的文件

### 📊 系统管理
- **用户管理**: 管理员可管理所有用户
- **存储监控**: 查看用户存储使用情况
- **系统日志**: 记录用户操作日志

## 🛠️ 技术栈

### 后端技术
- **框架**: Spring Boot 2.6.13
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: MyBatis Plus 3.4.2
- **权限认证**: Sa-Token 1.39.0
- **文件存储**: 阿里云OSS + MinIO
- **邮件服务**: Spring Mail
- **API文档**: Knife4j (Swagger)
- **日志**: Logback + SLF4J
- **构建工具**: Maven

### 前端技术
- **框架**: Vue 3.3.4
- **构建工具**: Vite 4.4.9
- **UI组件库**: Element Plus 2.3.9
- **状态管理**: Pinia 2.1.6
- **路由**: Vue Router 4.2.4
- **HTTP客户端**: Axios 1.4.0
- **样式**: SCSS
- **图标**: Element Plus Icons

## 📦 项目结构

```
cloud-netdisk/
├── cloud-netdisk-frontend/          # 前端项目
│   ├── src/
│   │   ├── api/                     # API接口
│   │   ├── components/              # 组件
│   │   ├── router/                  # 路由配置
│   │   ├── stores/                  # 状态管理
│   │   ├── views/                   # 页面
│   │   └── utils/                   # 工具函数
│   └── package.json
├── src/main/java/edu/cuit/cloud_netdisk/
│   ├── annotation/                  # 自定义注解
│   ├── aspect/                      # AOP切面
│   ├── config/                      # 配置类
│   ├── constant/                    # 常量定义
│   ├── controller/                  # 控制器
│   ├── dao/                         # 数据访问层
│   ├── exception/                   # 异常处理
│   ├── handler/                     # 全局处理器
│   ├── interceptor/                 # 拦截器
│   ├── pojo/                        # 实体类
│   ├── result/                      # 统一返回结果
│   ├── service/                     # 业务逻辑层
│   └── util/                        # 工具类
└── pom.xml
```

## 🚀 快速开始

### 环境要求
- JDK 1.8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端部署

1. **克隆项目**
```bash
git clone <repository-url>
cd cloud-netdisk
```

2. **配置数据库**
- 创建MySQL数据库
- 修改 `src/main/resources/application.properties` 中的数据库配置

3. **配置Redis**
- 确保Redis服务运行
- 修改配置文件中的Redis连接信息

4. **配置阿里云OSS**
- 在阿里云控制台创建OSS Bucket
- 修改配置文件中的OSS配置信息

5. **配置邮箱服务**
- 修改配置文件中的邮箱配置（用于发送验证码）

6. **启动后端服务**
```bash
mvn clean install
mvn spring-boot:run
```

### 前端部署

1. **进入前端目录**
```bash
cd cloud-netdisk-frontend
```

2. **安装依赖**
```bash
npm install
```

3. **启动开发服务器**
```bash
npm run dev
```

4. **构建生产版本**
```bash
npm run build
```

## 📖 使用说明

### 用户角色说明

- **访客(Guest)**: 只能查看公开文件，无法上传、下载、分享文件
- **普通用户(User)**: 可以上传、下载、分享文件，管理自己的文件
- **VIP用户(VIP)**: 拥有更大的存储空间和更多功能
- **管理员(Admin)**: 可以管理所有用户和文件，拥有系统管理权限

### 主要操作流程

1. **用户注册/登录**
   - 访问系统首页，点击注册或登录
   - 支持邮箱验证码登录和密码登录

2. **文件管理**
   - 在首页可以查看所有文件
   - 点击"上传文件"按钮上传新文件
   - 点击"新建文件夹"创建文件夹
   - 右键文件可以进行下载、删除、重命名等操作

3. **文件分享**
   - 右键文件选择"分享"
   - 设置分享有效期和访问权限
   - 复制分享链接发送给他人

4. **收藏文件**
   - 右键文件选择"收藏"
   - 在左侧菜单"我的收藏"中查看收藏的文件

5. **回收站管理**
   - 删除的文件会进入回收站
   - 在回收站中可以恢复或彻底删除文件

## 🔧 配置说明

### 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/cloud_storage_system
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Redis配置
```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_password
```

### 阿里云OSS配置
```properties
aliyun.oss.endpoint=oss-cn-hangzhou.aliyuncs.com
aliyun.oss.access-key-id=your_access_key_id
aliyun.oss.access-key-secret=your_access_key_secret
aliyun.oss.bucket-name=your_bucket_name
```

### 邮箱配置
```properties
spring.mail.host=smtp.qq.com
spring.mail.port=465
spring.mail.username=your_email@qq.com
spring.mail.password=your_email_password
```

## 📝 API文档

启动后端服务后，访问以下地址查看API文档：
- Swagger UI: `http://localhost:8080/doc.html`
- API接口文档包含所有接口的详细说明和测试功能

## 🔒 安全特性

- **密码加密**: 使用BCrypt对用户密码进行加密存储
- **Token认证**: 基于Sa-Token的JWT Token认证机制
- **权限控制**: 基于RBAC的细粒度权限管理
- **文件安全**: 文件上传验证，防止恶意文件上传
- **SQL注入防护**: 使用MyBatis Plus防止SQL注入

## 🐛 常见问题

### Q: 文件上传失败怎么办？
A: 检查以下几点：
- 文件大小是否超过500MB限制
- 用户存储空间是否充足
- 网络连接是否正常
- OSS配置是否正确

### Q: 登录失败怎么办？
A: 检查以下几点：
- 邮箱和密码是否正确
- 验证码是否过期
- 数据库连接是否正常

### Q: 权限不足怎么办？
A: 联系管理员提升用户角色或分配相应权限

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

感谢以下开源项目的支持：
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Sa-Token](https://sa-token.dev33.cn/)
- [MyBatis Plus](https://baomidou.com/) 