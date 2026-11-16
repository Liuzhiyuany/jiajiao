# 🎓 大学生家教撮合与实时交流平台

## 📖 项目简介
基于 Java 27 + Spring Boot 4.x + MySQL + WebSocket 开发的家教撮合平台。支持老师/家长双角色登录、多条件检索、实时私信与历史聊天记录持久化。

## 🛠️ 技术栈
- **后端**：Java 27, Spring Boot 4.x, Spring Data JPA, Spring Security Crypto
- **数据库**：MySQL 8.x
- **前端**：原生 HTML, CSS, JavaScript (Fetch API)
- **通信**：WebSocket

## 🚀 本地运行指南 (How to run)
1. **准备数据库**：在 MySQL 中创建一个名为 `jiajiao_db` 的空数据库。
2. **修改配置**：在 `src/main/resources/application.properties` 中，修改你的 MySQL 账号和密码。
3. **启动项目**：
   - 方式一：在 IDEA 中运行 `JiajiaoApplication.java`。
   - 方式二：使用 Maven 打包 `mvn clean package`，然后在 `target` 目录下运行 `java -jar jiajiao-0.0.1-SNAPSHOT.jar`。
4. **访问网页**：浏览器打开 `http://localhost:8080/index.html`。

## ✨ 核心功能
- [x] 用户注册与登录 (BCrypt 密码加密)
- [x] 老师资料录入 / 家长需求发布
- [x] 资源大厅多条件动态模糊搜索
- [x] WebSocket 在线一对一私信
- [x] 聊天记录漫游 (MySQL 持久化)
