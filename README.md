# 多部门领导评分系统（基础版）

Java 17 + Spring Boot 3 + Maven。默认使用本地 H2 文件数据库，数据保存在项目目录 `data/`。打开 IDEA 后可不安装 MySQL 先体验完整流程。项目提供 REST API，不含图形界面和登录认证，因此仅适合本机演示。

## IDEA 启动

1. 用 IDEA 打开本目录的 `pom.xml`，选择 **Open as Project**，等待 Maven 下载依赖。
2. 设置 Project SDK 为 **JDK 17**。
3. 运行 `src/main/java/com/example/scoring/ScoringApplication.java`。
4. 看到 `Started ScoringApplication` 后，使用 IDEA HTTP Client、Postman 或 curl 请求 `http://localhost:8080/api`。也可在 IDEA Maven 面板运行 `spring-boot:run`。

## 基础流程

请求头统一使用 `Content-Type: application/json`。示例中的 ID 以创建接口实际返回为准。

```http
POST http://localhost:8080/api/departments
Content-Type: application/json

{"name":"研发部"}
```

再创建另一个部门及领导、员工：

```http
POST http://localhost:8080/api/departments
Content-Type: application/json

{"name":"市场部"}

POST http://localhost:8080/api/users
Content-Type: application/json

{"username":"boss","name":"总经理","departmentId":1,"role":"LEADER"}

POST http://localhost:8080/api/users
Content-Type: application/json

{"username":"alice","name":"小李","departmentId":1,"role":"EMPLOYEE"}

POST http://localhost:8080/api/users
Content-Type: application/json

{"username":"bob","name":"小王","departmentId":2,"role":"EMPLOYEE"}
```

创建跨部门任务，省略 `templateId` 即自动生成默认评分表。仅让本部门参与时传 `"departmentIds":[1]`；跨部门时传 `[1,2]`：

```http
POST http://localhost:8080/api/tasks
Content-Type: application/json

{"name":"总经理季度评价","leaderId":1,"departmentIds":[1,2]}
```

先请求 `GET /api/templates/{templateId}/items` 取得评分项 ID，再提交，每项 0 至 25 分：

```http
POST http://localhost:8080/api/tasks/1/submissions
Content-Type: application/json

{"evaluatorId":2,"scores":{"1":22,"2":23,"3":24,"4":25}}
```

`GET /api/tasks/1/results` 查看参与人数、已提交人数和平均分。`GET /api/tasks/1/participants` 查看本任务参与者。`POST /api/tasks/1/close` 关闭任务。

### 自定义评分表

```http
POST http://localhost:8080/api/templates
Content-Type: application/json

{"name":"管理能力表","items":[{"title":"决策能力","maxScore":60},{"title":"团队建设","maxScore":40}]}
```

创建任务时传入返回的模板 ID，例如 `"templateId":2`。实际提交时，用 `GET /api/templates/2/items` 的项目 ID 作为 `scores` 的键。

## 切换 MySQL

在 MySQL 8 中执行 `database.sql`。配置文件 `src/main/resources/application-mysql.yml` 已提供数据库 URL；在 IDEA 运行配置的 **Active profiles** 填 `mysql`，环境变量设置 `DB_USER=你的用户名;DB_PASSWORD=你的密码`（Windows IDEA 可用分号分隔），或者直接修改该配置文件。MySQL 地址是 `localhost:3306`、数据库名是 `scoring_system`，按你的环境修改。首次启动会由 JPA 建表，建库 SQL 只创建数据库。

## 说明

当前版本没有账号登录和权限控制，`evaluatorId` 由请求传入，不能部署到公网或用于正式考核。评分任务创建时会冻结参与人员名单；支持指定本部门或多个部门。任务使用创建时的评分表，开始评分后请勿直接修改数据库中的评分项。

## 登录页面

启动后访问 `http://localhost:8080/login.html`。首次使用 MySQL 时，系统会自动创建一个管理员：

```text
用户名：admin
密码：admin123
```

登录成功后进入 `index.html`。登录页位于 `src/main/resources/static/login.html`，首页位于 `src/main/resources/static/index.html`。当前使用简单前端会话保存登录状态，适合本地基础演示；正式部署还需要接入 JWT 或 Spring Session，并增加权限拦截。

## MySQL 配置

1. 安装 MySQL 8，执行项目根目录 `database.sql`。
2. 修改 `src/main/resources/application-mysql.yml` 中的用户名和密码，或设置 `DB_USER`、`DB_PASSWORD` 环境变量。
3. 在 IDEA 的 Run Configuration 中将 **Active profiles** 设置为 `mysql`。
4. 运行项目，JPA 会自动创建表，初始化 `admin` 账号。
