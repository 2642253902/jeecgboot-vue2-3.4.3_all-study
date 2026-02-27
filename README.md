# 云计算课程学习平台

## 项目简介

本项目是基于 **JeecgBoot 3.4.3** 开源低代码平台开发的云计算课程在线学习系统。采用前后端分离架构，为学生提供在线课程学习、资源浏览、学习记录管理等功能，为教师提供课程管理、资源发布、备课等功能。

> **基础框架：** [JeecgBoot Vue2 3.4.3](https://github.com/jeecgboot/jeecg-boot)  
> **开发重点：** Study 模块 - 云计算课程学习平台

---

## 核心功能模块

### Study 模块（云计算课程学习平台）

#### 1. **学习中心** 
- 课程列表展示（按课程分类）
- 课程搜索与筛选
- 课程详情查看
- 课程资源浏览与学习
- 学习进度记录

#### 2. **备课中心**
- 教师课程准备
- 课程内容编辑
- 课程资源上传与管理

#### 3. **课程管理**
- 课程信息维护（课程名称、分类、标签、出版社等）
- 课程状态管理（启用/停用）
- 课程学时设置
- 课程类型：必修课/选修课

#### 4. **课程分类管理**
- 课程分类体系维护
- 分类层级管理

#### 5. **课程资源管理**
- 课程资源上传（视频、文档、图片等）
- 资源分类与排序
- 资源关联课程

#### 6. **学生学习记录**
- 学习时长统计
- 学习进度跟踪
- 课程完成情况记录

---

## 技术架构

### 后端技术栈

- **基础框架：** Spring Boot 2.6.6
- **持久层：** MyBatis-Plus 3.5.1
- **安全框架：** Apache Shiro 1.8.0 + JWT 3.11.0
- **数据库：** MySQL 5.7+
- **开发工具：** JeecgBoot 代码生成器

### 前端技术栈

- **框架：** Vue 2.6.10 + Vuex + Vue Router
- **UI组件：** Ant Design Vue
- **HTTP客户端：** Axios
- **构建工具：** Webpack

---

## 项目结构


jeecgboot-vue2-3.4.3_all/
├── jeecg-boot/                          # 后端项目
│   └── jeecg-module-demo/
│       └── src/main/java/org/jeecg/modules/demo/
│           └── study/                   # 【核心】Study模块
│               ├── controller/          # 控制器层
│               ├── entity/             # 实体类
│               ├── mapper/             # 数据访问层
│               ├── service/            # 业务逻辑层
│               └── vo/                 # 视图对象
├── ant-design-vue-jeecg/               # 前端项目
│   └── src/
│       └── views/
│           └── study/                  # 【核心】Study模块前端页面
│               ├── learningCenter.vue           # 学习中心
│               ├── PreparationCenter.vue        # 备课中心
│               ├── CloudComputingCourseList.vue # 课程列表
│               ├── CloudComputingCourseTypeList.vue     # 课程分类
│               ├── CloudComputingCourseResourceList.vue # 课程资源
│               └── CloudComputingStudentLearningRecordList.vue  # 学习记录
└── jeecg-boot.sql                      # 数据库脚本


---

## 数据库表设计

| 表名 | 说明 |
|------|------|
| cloud_computing_course | 课程表 |
| cloud_computing_course_type | 课程分类表 |
| cloud_computing_course_resource | 课程资源表 |
| cloud_computing_student_learning_record | 学习记录表 |
| cloud_computing_class | 课程班级关联表 |

---

## 快速开始

### 环境要求

- JDK 8+（小于 JDK 17）
- Maven 3.6+
- Node.js 12+
- MySQL 5.7+
- Redis

### 后端启动

1. 导入数据库脚本 jeecg-boot.sql
2. 修改配置文件 jeecg-boot/jeecg-module-system/jeecg-system-start/src/main/resources/application-dev.yml
3. 启动 Redis 服务
4. 运行主类：org.jeecg.JeecgSystemApplication
5. 访问后端接口文档：http://localhost:8080/jeecg-boot/doc.html

### 前端启动

进入前端目录并执行：
- npm install
- npm run serve
- 访问地址：http://localhost:3000

### 默认账号

- **管理员账号：** admin / 123456
- **教师账号：** teacher01 / 123456
- **学生账号：** student01 / 123456

---

## 基于 JeecgBoot 开源框架

本项目基于 **JeecgBoot 3.4.3** 低代码平台开发，充分利用其强大的代码生成器和基础功能模块，在此基础上进行业务定制开发。

### JeecgBoot 主要特性

- ✅ 强大的代码生成器（一键生成前后端代码）
- ✅ 完善的权限管理（支持按钮权限、数据权限）
- ✅ 用户、角色、菜单、部门等基础功能
- ✅ Excel导入导出
- ✅ 在线接口文档（Swagger）
- ✅ 系统监控与日志
- ✅ 数据字典管理
- ✅ 定时任务

---

## 开发说明

### Study 模块开发流程

1. **数据库设计** - 设计云计算课程相关表结构
2. **使用代码生成器** - 通过 JeecgBoot 代码生成器生成基础 CRUD 代码
3. **业务定制开发** - 在生成的代码基础上进行业务逻辑定制
4. **前端页面优化** - 基于生成的Vue页面进行UI优化

---

## 项目特点

1. **基于成熟框架** - 基于 JeecgBoot 开源框架，稳定可靠
2. **快速开发** - 利用代码生成器，大幅提升开发效率
3. **前后端分离** - 前后端完全分离，易于维护和扩展
4. **权限控制** - 基于 Shiro + JWT 的完善权限体系
5. **响应式设计** - 支持PC端和移动端访问
6. **模块化开发** - Study 模块独立清晰，易于二次开发

---

## 技术支持

### JeecgBoot 官方文档
- 官网：http://www.jeecg.com
- 开发文档：http://doc.jeecg.com
- 在线演示：http://boot.jeecg.com
- Github：https://github.com/jeecgboot/jeecg-boot

---

## 开发计划

- [x] 课程管理模块
- [x] 课程分类管理
- [x] 课程资源管理
- [x] 学生学习记录
- [x] 学习中心（学生端）
- [x] 备课中心（教师端）
- [ ] 在线考试功能
- [ ] 作业提交与批改
- [ ] 讨论区功能
- [ ] 学习统计与分析
- [ ] 移动端APP
