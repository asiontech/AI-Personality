# 悟心AI性格测试系统

开源不易，点个 star 鼓励一下吧！

## 项目概述

悟心AI性格测试系统是一个基于若依管理系统的多平台应用，支持微博、微信登录，提供PC端和小程序端访问。用户可以通过回答问卷来判断其16种人格类型所属。该系统已集成到Jeepay项目中，作为一项收费业务，为用户提供个性化的人格分析和情感检测服务。基础框架依赖于**若依管理系统**[点我传送](https://gitee.com/y_project/RuoYi-Vue)

## 主要功能

1. **多平台登录**：支持微博、微信账号登录，方便用户快速接入系统。
2. **人格类型测试**：用户通过回答问卷，系统自动分析并判断其16种人格类型。
3. **情感检测**：基于用户的问卷回答，系统提供情感倾向分析。
4. **收费服务**：通过Jeepay项目集成，用户需支付费用以获取完整的人格分析和情感检测报告。
5. **多端支持**：提供PC端和小程序端，用户可根据需求选择使用。

## 技术栈

- **后端框架**：若依管理系统
- **前端技术**：Vue.js（PC端）、微信小程序（小程序端）
- **支付集成**：Jeepay
- **数据库**：MySQL、Redis
- **第三方登录**：微博、微信OAuth2.0



## 安装与部署

### 后端部署

1. **克隆项目**：

   ```
   git clone https://github.com/asiontech/AI-Personality.git
   ```

2. **导入数据库**：

   - 创建MySQL数据库，并导入`backend/resources/sql/`目录下的SQL文件。

3. **配置数据库连接**：

   - 修改`backend/config/application.yml`中的数据库连接信息。

4. **启动项目**：

   - 进入`backend`目录，运行以下命令启动后端服务：

     bash

     复制

     ```
     mvn spring-boot:run
     ```

### 前端部署

1. **安装依赖**：

   - 进入`frontend`目录，运行以下命令安装依赖：

     ```
     npm install
     ```

2. **启动开发服务器**：

   - 运行以下命令启动前端开发服务器：

     ```
     npm run serve
     ```

3. **构建生产环境**：

   - 运行以下命令构建生产环境代码：

     ```
     npm run build
     ```

### 小程序部署

1. **安装依赖**：

   - 进入`miniprogram`目录，运行以下命令安装依赖：

     bash

     复制

     ```
     npm install
     ```

2. **导入小程序开发工具**：

   - 使用微信开发者工具导入`miniprogram`目录，并配置AppID。

3. **编译运行**：

   - 在微信开发者工具中点击“编译”并运行小程序。

## 使用说明

1. **登录系统**：
   - 用户可通过微博或微信账号登录系统。
2. **填写问卷**：
   - 登录后，用户可进入问卷页面，回答相关问题。
3. **查看结果**：
   - 提交问卷后，系统将自动分析并显示用户的人格类型和情感倾向。
4. **支付获取报告**：
   - 用户可通过Jeepay支付获取详细的报告和分析结果。

## 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork项目仓库。
2. 创建新的分支（`git checkout -b feature/YourFeatureName`）。
3. 提交更改（`git commit -m 'Add some feature'`）。
4. 推送到分支（`git push origin feature/YourFeatureName`）。
5. 创建Pull Request。

## 许可证

本项目采用MIT许可证，详情请参阅[LICENSE](https://license/)文件。

## 联系方式

如有任何问题或建议，请联系项目维护者：

- GitHub: [[悟心AI性格测试](https://github.com/asiontech/AI-Personality)](https://github.com/asiontech/AI-Personality.git)

------

感谢您使用悟心AI性格测试系统！希望我们的服务能为您带来价值。
