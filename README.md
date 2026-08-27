#  财务记账 - 个人财务管理系统

Vue 3 + Spring Boot + MySQL 轻量级个人记账应用。

## 功能清单

| 功能 | 说明 |
|------|------|
| 用户认证 | 注册/登录，JWT 鉴权，密码 bcrypt 加密 |
| 记账流水 | 记录收入/支出，支持金额、分类、日期、备注、账户 |
| 账单管理 | 按时间/分类/关键词筛选，分页，编辑/删除 |
| 数据看板 | 本月收支汇总、结余、近7天趋势图、支出分类饼图 |
| 消费板块 | 自定义新增/编辑/删除消费分类，选图标选颜色 |
| 预算管理 | 按分类按月设预算，进度条预警（超支红色） |
| 账户管理 | 现金/银行卡/支付宝/微信多账户管理 |
| 数据导出 | 导出 Excel |
| 暗色模式 | 亮色/暗色主题切换，默认跟随系统 |

## 技术栈

- **前端**：Vue 3 + TypeScript + Vite + Tailwind CSS + Pinia + Vue Router + ECharts + Axios
- **后端**：Spring Boot 3 + Spring Security + Spring Data JPA + JWT
- **数据库**：MySQL 8.0
- **部署**：Docker + docker-compose

## 快速开始

### 方式一：Docker 一键启动

```bash
# 启动所有服务（MySQL + 后端 + 前端）
docker-compose up -d

# 访问前端
open http://localhost:3000

# 默认需要注册一个账号
```

### 方式二：本地开发

#### 1. 启动 MySQL

```bash
# 确保 MySQL 运行在 3306 端口
# 创建数据库
mysql -u root -p < backend/src/main/resources/init.sql
```

#### 2. 启动后端

```bash
cd backend
# 如果用 Maven Wrapper，先生成
# Maven 3.9+ / JDK 17+
mvn spring-boot:run
# 后端运行在 http://localhost:8080
```

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
# 前端运行在 http://localhost:3000
```

## 项目结构

```
.
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/finance/
│   │   ├── config/             # 安全配置（JWT、CORS、异常处理）
│   │   ├── controller/         # REST 接口
│   │   ├── dto/                # 请求 DTO + 校验
│   │   ├── entity/             # JPA 实体
│   │   ├── repository/         # 数据访问层
│   │   ├── service/            # 业务逻辑
│   │   └── common/             # 公共类
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   └── init.sql            # 数据库初始化脚本
│   └── pom.xml
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 封装
│   │   ├── components/         # 组件
│   │   ├── router/             # 路由
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── types/              # 类型定义
│   │   ├── views/              # 页面
│   │   └── main.ts
│   └── package.json
├── docker-compose.yml          # 一键启动
└── README.md
```

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| GET | /api/dashboard/summary | 看板汇总 |
| GET/POST | /api/transactions | 流水查询/新增 |
| PUT/DELETE | /api/transactions/{id} | 编辑/删除流水 |
| GET/POST | /api/categories | 分类查询/新增 |
| PUT/DELETE | /api/categories/{id} | 编辑/删除分类 |
| GET/POST | /api/budgets | 预算查询/设置 |
| DELETE | /api/budgets/{id} | 删除预算 |
| GET/POST | /api/accounts | 账户查询/新增 |
| PUT/DELETE | /api/accounts/{id} | 编辑/删除账户 |
| GET | /api/export/excel | 导出 Excel |

所有接口返回统一格式：`{ code: 0, data: any, message: string }`

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | localhost | MySQL 地址 |
| DB_PORT | 3306 | MySQL 端口 |
| DB_NAME | finance_db | 数据库名 |
| DB_USER | root | 数据库用户 |
| DB_PASS | root | 数据库密码 |
| JWT_SECRET | (base64) | JWT 签名密钥 |
| FRONTEND_URL | http://localhost:3000 | 前端地址（CORS） |
