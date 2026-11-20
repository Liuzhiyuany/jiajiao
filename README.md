# 🎓 致学优教 - 大学生家教撮合与实时交流平台

基于 **Java 17 + Spring Boot 3.2 + MySQL + WebSocket + DeepSeek 大模型** 开发的家教 C2C 撮合平台。
老师与家长双向选择、公海大厅撮合、实时私信、AI 自然语言发布。

## ✨ 功能特性

- **双角色账号体系**：老师 / 家长注册登录，BCrypt 密码加密（单向哈希，不可逆）
- **JWT 登录态**：后端签发签名 token，前端改 URL 参数无法冒充他人身份
- **资源大厅**：按科目 + 年级模糊检索，支持「初中/高中」聚合搜索
- **双端 AI 助手**：家长用大白话描述需求、老师口述自我介绍，AI 自动结构化为表单字段
- **双向确认接单**：老师申请 → 家长确认，需求才下架，防止单方面锁单
- **实时私信**：WebSocket 长连接，消息落库，支持历史记录漫游
- **未读红点**：联系人列表与导航栏实时未读数
- **移动端自适应**：PC 端三栏布局 / 手机端底部导航 + 全屏聊天

## 🛠 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 17、Spring Boot 3.2、Spring Data JPA、Spring AI |
| 数据库 | MySQL 8 |
| 通信 | WebSocket、RESTful API |
| 安全 | BCrypt、JWT（jjwt） |
| AI | DeepSeek Chat（OpenAI 兼容协议） |
| 前端 | 原生 HTML / CSS / JavaScript（Fetch API、sessionStorage） |

## 🚀 本地运行

1. 建库：`CREATE DATABASE jiajiao_db CHARACTER SET utf8mb4;`
2. 配置环境变量（避免密钥进代码）：setx DEEPSEEK_API_KEY "你的 deepseek api key"
3. 修改 `src/main/resources/application.properties` 中的数据库账号密码
4. 启动：运行 `JiajiaoApplication`，或 `mvn spring-boot:run`
5. 访问：`http://localhost:8080`

## 📐 数据模型

- `users`：账号（username / BCrypt 密码 / role）
- `teacher`：老师资料，`user_id` 关联账号，一账号一份
- `parent`：家长需求，`status` = 1 找老师中 / 0 已结束
- `message`：私信记录，含 `is_read` 用于未读统计

## 🔒 安全说明

- 数据库密码与 API Key 通过环境变量注入，仓库内不含明文密钥
- 所有 `/api/**`（除登录注册）强制校验 JWT
- 写操作的身份以 token 中的 userId 为准，忽略前端传入值