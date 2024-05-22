# monitor
> [!TIP]
>
> 晋中学院 - 2024.5 - 软件工程课程设计

# 一、概述

> [!NOTE]
>
> 该项目采用 SpringBoot3 + Vue3 编写的前后端分离模版项目，集成多种技术栈，使用 JWT 校验方案。

### 1.1 后端功能与技术点

用户注册、用户登录、重置密码等基础功能以及对应接口

- 采用 Mybatis-Plus 作为持久层框架，使用更便捷
- 采用 Redis 存储注册/重置操作验证码，带过期时间控制
- 采用 RabbitMQ 积压短信发送任务，再由监听器统一处理
- 采用 SpringSecurity 作为权限校验框架，手动整合 Jwt 校验方案
- 采用 Redis 进行 IP 地址限流处理，防刷接口
- 视图层对象和数据层对象分离，编写工具方法利用反射快速互相转换
- 错误和异常页面统一采用 JSON 格式返回，前端处理响应更统一
- 手动处理跨域，采用过滤器实现
- 使用 Swagger 作为接口文档自动生成，已自动配置登录相关接口
- 采用过滤器实现对所有请求自动生成雪花 ID 方便线上定位问题
- 针对于多环境进行处理，开发环境和生产环境采用不同的配置
- 日志中包含单次请求完整信息以及对应的雪花 ID，支持文件记录
- 项目整体结构清晰，职责明确，注释全面，开箱即用

### 1.2 前端功能与技术点

用户注册、用户登录、重置密码等界面，以及一个简易的主页

- 采用 Vue-Router 作为路由
- 采用 Axios 作为异步请求框架
- 采用 Element-Plus 作为 UI 组件库
- 使用 VueUse 适配深色模式切换
- 使用 unplugin-auto-import 按需引入，减少打包后体积

# 二、安装

1. 克隆此仓库 `git clone https://github.com/caobaoqi1029/monitor.git --depth=1 && cd monitor`
2. 安装依赖
   - server:  通过 maven 安装即可
   - web-ui: `cd web-ui` 然后 `pnpm install`
3. 安装环境 (通过 docker 直接安装) `cd docker/path-install ` 然后 `docker compose up -d`
4. 参照 `application-prod.yaml`的信息配置 `application-dev.yaml` 即可
5. 启动
   - server: SpringBoot 启动 ！
   - web-ui: `cd web-ui && pnpm run dev`

![image-20240511103306658](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511103306658.png)

> [!TIP]
>
> 默认登录用户信息为： `cbq` + `cbq.monitor`

 ![image-20240511103407106](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511103407106.png)

![image-20240511103432227](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511103432227.png)

# 三、INFO

- 许可信息 [MIT License](./LICENSE)
- 邮箱 `1203952894@qq.com`
