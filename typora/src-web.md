## V 1.2.0

> [!TIP]
>
> - 撰稿人：曹蓓
> - 日期：2024.5.12 10.30
> - 主题：[添加 登录、注册 功能及 dev INFO 相关的文档](https://github.com/caobaoqi1029/monitor/issues/7) web 端源代码说明部分

## 一、项目前端说明

### 1.1 项目结构说明

![image-20240512151058585](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240512151058585.png)

### 1.2 项目环境搭建

1. 克隆此仓库 `git clone https://github.com/caobaoqi1029/monitor.git --depth=1 && cd monitor`
2. 进入前端模块 `cd web-ui`
3. 安装依赖通过 `pnpm install`
4. 启动 `pnpm run dev`

> [!CAUTION]
>
> - 登录名 `cbq` 或 `2024cbq@gmail.com` 
> - 密码 `cbq.monitor` 

![image-20240512150603701](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240512150603701.png)

![image-20240512150613240](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240512150613240.png)

### 1.3 项目部署

web 端通过`github workflow web-ui/build.yaml ` 上传至 DockerHub 即 `cbh0817/monitor-web-ui:version` 

```yaml
name: CI|CD build-web-ui

on:
  push:
    tags:
      - v*

permissions:
  contents: write

jobs:
  docket-build:
    name: web-ui docker build
    runs-on: ubuntu-latest
    defaults:
      run:
        shell: bash
        working-directory: ./web-ui

    steps:
      - name: 读取仓库内容 👓
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: 安装 pnpm
        uses: pnpm/action-setup@v2
        with:
          run_install: true
          version: 8


      - name: 设置 Node.js
        uses: actions/setup-node@v3
        with:
          node-version: 20
          cache: pnpm

      - name: 构建 dist 🔨
        env:
          NODE_OPTIONS: --max_old_space_size=8192
        run:  pnpm run build

      - name: Set up QEMU
        uses: docker/setup-qemu-action@v3

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: 登录到 DockerHub 😘
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_HUB_USERNAME_CBH }}
          password: ${{ secrets.DOCKER_HUB_TOKEN_CBH }}

      - name: 构建并推送到 Dockerhub ❤️
        uses: docker/build-push-action@v5
        with:
          context: ./web-ui
          file: ./web-ui/Dockerfile
          push: true
          tags: cbh0817/monitor-web-ui:latest



```

```dockerfile
FROM nginx
LABEL authors="cbq"

COPY dist ./usr/share/nginx/html
COPY conf/nginx.conf ./etc/nginx/conf.conf
COPY conf/conf.d ./etc/nginx/conf.d

EXPOSE 5173
```



## 二、src 源代码说明

### 2.1 components 组件

> [!IMPORTANT]
>
> components 组件 目录用于存放 Vue.js 的组件。组件是可复用的 Vue 实例，通常用来构建界面的各个部分
>
> - `TabItem.vue`  标签组件

```vue
<script setup>
defineProps({
  name: String,
  active: Boolean
})
</script>

<template>
  <div :class="`tab-item ${active ? 'active' : ''}`">
    {{name}}
  </div>
</template>

<style scoped>
.tab-item {
  font-size: 15px;
  width: 55px;
  height: 55px;
  text-align: center;
  line-height: 55px;
  box-sizing: border-box;
  transition: color .3s;

  &:hover {
    cursor: pointer;
    color: var(--el-color-primary);
  }
}

.active {
  border-bottom: solid 2px var(--el-color-primary);
}
</style>
```



### 2.2 net 网络工具
> [!IMPORTANT]
>
> `net` 目录可能用于存放与网络请求相关的工具或服务

```js
import axios from "axios";
import {ElMessage} from "element-plus";
import {useStore} from "@/store";

const authItemName = "authorize"

const accessHeader = () => {
    return {
        'Authorization': `Bearer ${takeAccessToken()}`
    }
}

const defaultError = (error) => {
    console.error(error)
    ElMessage.error('发生了一些错误，请联系管理员')
}

const defaultFailure = (message, status, url) => {
    console.warn(`请求地址: ${url}, 状态码: ${status}, 错误信息: ${message}`)
    ElMessage.warning(message)
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName);
    if (!str) return null
    const authObj = JSON.parse(str)
    if (new Date(authObj.expire) <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登录状态已过期，请重新登录！")
        return null
    }
    return authObj.token
}

function storeAccessToken(remember, token, expire) {
    const authObj = {
        token: token,
        expire: expire
    }
    const str = JSON.stringify(authObj)
    if (remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

function deleteAccessToken() {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

function internalPost(url, data, headers, success, failure, error = defaultError) {
    axios.post(url, data, {headers: headers}).then(({data}) => {
        if (data.code === 200)
            success(data.data)
        else
            failure(data.message, data.code, url)
    }).catch(err => error(err))
}

function internalGet(url, headers, success, failure, error = defaultError) {
    axios.get(url, {headers: headers}).then(({data}) => {
        if (data.code === 200)
            success(data.data)
        else
            failure(data.message, data.code, url)
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure = defaultFailure) {
    internalPost('/api/auth/login', {
        username: username,
        password: password
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(remember, data.token, data.expire)
        const store = useStore()
        store.user.role = data.role
        store.user.username = data.username
        store.user.email = data.email
        store.user.avatar = data.avatar
        ElMessage.success(`登录成功，欢迎 ${data.username} 来到我们的系统`)
        success(data)
    }, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure)
}

function logout(success, failure = defaultFailure) {
    get('/api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success(`退出登录成功，欢迎您再次使用`)
        success()
    }, failure)
}

function get(url, success, failure = defaultFailure) {
    internalGet(url, accessHeader(), success, failure)
}

function unauthorized() {
    return !takeAccessToken()
}

export {post, get, login, logout, unauthorized}
```
### 2.3 router 路由管理
> [!IMPORTANT]
>
> `router` 目录负责定义和管理前端路由

```js
import {createRouter, createWebHistory} from 'vue-router'
import {unauthorized} from "@/net";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/',
            name: 'welcome',
            component: () => import('@/views/WelcomeView.vue'),
            children: [
                {
                    path: '',
                    name: 'welcome-login',
                    component: () => import('@/views/welcome/LoginPage.vue')
                }, {
                    path: 'register',
                    name: 'welcome-register',
                    component: () => import('@/views/welcome/RegisterPage.vue')
                }, {
                    path: 'forget',
                    name: 'welcome-forget',
                    component: () => import('@/views/welcome/ForgetPage.vue')
                }
            ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children: [
                {
                    path: '',
                    name: 'manage',
                    component: () => import('@/views/main/Manage.vue')
                }, {
                    path: 'security',
                    name: 'security',
                    component: () => import('@/views/main/Security.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const isUnauthorized = unauthorized()
    if (to.name.startsWith('welcome') && !isUnauthorized) {
        next('/index')
    } else if (to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    } else {
        next()
    }
})

export default router
```
### 2.4 store 数据存储
> [!IMPORTANT]
>
> `store` 目录用于通过 Pinia 对 Vue.js 进行状态管理

```js
import {defineStore} from "pinia";

export const useStore = defineStore('general', {
    state: () => {
        return {
            user: {
                role: '',
                username: '',
                email: '',
                avatar: ''
            }
        }
    },
    getters: {
        isAdmin() {
            return this.user.role === 'admin'
        }
    },
    persist: true
})
```
### 2.5 tools 常用工具
> [!IMPORTANT]
>
> 目录包含项目中用到的各种辅助工具和公用函数

```js
import {useClipboard} from "@vueuse/core";
import {ElMessage, ElMessageBox} from "element-plus";
import {post} from "@/net";

function fitByUnit(value, unit) {
    const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB']
    let index = units.indexOf(unit)
    while (((value < 1 && value !== 0) || value >= 1024) && (index >= 0 || index < units.length)) {
        if (value >= 1024) {
            value = value / 1024
            index = index + 1
        } else {
            value = value * 1024
            index = index - 1
        }
    }
    return `${parseInt(value)} ${units[index]}`
}

function percentageToStatus(percentage) {
    if (percentage < 50)
        return 'success'
    else if (percentage < 80)
        return 'warning'
    else
        return 'exception'
}

function osNameToIcon(name) {
    if (name.indexOf('Ubuntu') >= 0)
        return {icon: 'fa-ubuntu', color: '#db4c1a'}
    else if (name.indexOf('CentOS') >= 0)
        return {icon: 'fa-centos', color: '#9dcd30'}
    else if (name.indexOf('macOS') >= 0)
        return {icon: 'fa-apple', color: 'grey'}
    else if (name.indexOf('Windows') >= 0)
        return {icon: 'fa-windows', color: '#3578b9'}
    else if (name.indexOf('Debian') >= 0)
        return {icon: 'fa-debian', color: '#a80836'}
    else
        return {icon: 'fa-linux', color: 'grey'}
}

function cpuNameToImage(name) {
    if (name.indexOf('Apple') >= 0)
        return 'Apple.png'
    else if (name.indexOf('AMD') >= 0)
        return 'AMD.png'
    else
        return 'Intel.png'
}

const {copy} = useClipboard()
const copyIp = ip => copy(ip).then(() => ElMessage.success('成功复制IP地址到剪贴板'))

function rename(id, name, after) {
    ElMessageBox.prompt('请输入新的服务器主机名称', '修改名称', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        inputValue: name,
        inputPattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]{1,10}$/,
        inputErrorMessage: '名称只能包含中英文字符、数字和下划线',
    }).then(({value}) => post('/api/monitor/rename', {
                id: id,
                name: value
            }, () => {
                ElMessage.success('主机名称已更新')
                after()
            })
    )
}

export {fitByUnit, percentageToStatus, cpuNameToImage, osNameToIcon, rename, copyIp}
```
### 2.6 views 视图
> [!IMPORTANT]
>
> `views` 目录通常用于存放应用的页面级组件
>
> - main
>   - `Manage.vue` 管理页
>   - `Security.vue` 安全页
> - welcome
>   - `ForgetPage.vue` 忘记密码页面
>   - `LoginPage.vue` 登录页面
>   - `RegisterPage.vue `注册页面
> - `IndexView.vue` 主页
> - `WelcomeView.vue `欢迎页

#### 2.6.1 main

```vue
<script setup>

</script>

<template>
<b>Manage</b>
</template>

<style scoped>

</style>
```

```vue
<script setup>

</script>

<template>
<b>Security</b>
</template>

<style scoped>

</style>
```
#### 2.6.2 welcome
```vue
<template>
    <div>
        <div style="margin: 30px 20px">
            <el-steps :active="active" finish-status="success" align-center>
                <el-step title="验证电子邮件" />
                <el-step title="重新设定密码" />
            </el-steps>
        </div>
        <transition name="el-fade-in-linear" mode="out-in">
            <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请输入需要重置密码的电子邮件地址</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                        <el-form-item prop="email">
                            <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
                                <template #prefix>
                                    <el-icon><Message /></el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="code">
                            <el-row :gutter="10" style="width: 100%">
                                <el-col :span="17">
                                    <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                                        <template #prefix>
                                            <el-icon><EditPen /></el-icon>
                                        </template>
                                    </el-input>
                                </el-col>
                                <el-col :span="5">
                                    <el-button type="success" @click="validateEmail"
                                               :disabled="!isEmailValid || coldTime > 0">
                                        {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                                    </el-button>
                                </el-col>
                            </el-row>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button @click="confirmReset()" style="width: 270px;" type="danger" plain>开始重置密码</el-button>
                </div>
            </div>
        </transition>
        <transition name="el-fade-in-linear" mode="out-in">
            <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1">
                <div style="margin-top: 80px">
                    <div style="font-size: 25px;font-weight: bold">重置密码</div>
                    <div style="font-size: 14px;color: grey">请填写您的新密码，务必牢记，防止丢失</div>
                </div>
                <div style="margin-top: 50px">
                    <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                        <el-form-item prop="password">
                            <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码">
                                <template #prefix>
                                    <el-icon><Lock /></el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                        <el-form-item prop="password_repeat">
                            <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码">
                                <template #prefix>
                                    <el-icon><Lock /></el-icon>
                                </template>
                            </el-input>
                        </el-form-item>
                    </el-form>
                </div>
                <div style="margin-top: 70px">
                    <el-button @click="doReset()" style="width: 270px;" type="danger" plain>立即重置密码</el-button>
                </div>
            </div>
        </transition>
    </div>
</template>

<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {get, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const active = ref(0)

const form = reactive({
    email: '',
    code: '',
    password: '',
    password_repeat: '',
})

const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}

const rules = {
    email: [
        { required: true, message: '请输入邮件地址', trigger: 'blur' },
        {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
    ],
    code: [
        { required: true, message: '请输入获取的验证码', trigger: 'blur' },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ],
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
    if(prop === 'email')
        isEmailValid.value = isValid
}

const validateEmail = () => {
    coldTime.value = 60
    get(`/api/auth/ask-code?email=${form.email}&type=reset`, () => {
        ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`)
        const handle = setInterval(() => {
          coldTime.value--
          if(coldTime.value === 0) {
            clearInterval(handle)
          }
        }, 1000)
    }, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
    })
}

const confirmReset = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/auth/reset-confirm', {
                email: form.email,
                code: form.code
            }, () => active.value++)
        }
    })
}

const doReset = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/auth/reset-password', {
                email: form.email,
                code: form.code,
                password: form.password
            }, () => {
                ElMessage.success('密码重置成功，请重新登录')
                router.push('/')
            })
        }
    })
}

</script>

<style scoped>

</style>

```

```vue
<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: grey">在进入系统之前请先输入用户名和密码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" maxlength="10" type="text" placeholder="用户名/邮箱">
            <template #prefix>
              <el-icon>
                <User/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" maxlength="20" style="margin-top: 10px" placeholder="密码">
            <template #prefix>
              <el-icon>
                <Lock/>
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-row style="margin-top: 5px">
          <el-col :span="12" style="text-align: left">
            <el-form-item prop="remember">
              <el-checkbox v-model="form.remember" label="记住我"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" style="text-align: right">
            <el-link @click="router.push('/forget')">忘记密码？</el-link>
          </el-col>
        </el-row>
      </el-form>
    </div>
    <div style="margin-top: 40px">
      <el-button @click="userLogin()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: grey;font-size: 13px">没有账号</span>
    </el-divider>
    <div>
      <el-button style="width: 270px" @click="router.push('/register')" type="warning" plain>注册账号</el-button>
    </div>
  </div>
</template>

<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import router from "@/router";
import {reactive, ref} from "vue";
import {login} from '@/net'

const formRef = ref()
const form = reactive({
  username: '',
  password: '',
  remember: false
})

const rules = {
  username: [
    {required: true, message: '请输入用户名'}
  ],
  password: [
    {required: true, message: '请输入密码'}
  ]
}

function userLogin() {
  formRef.value.validate((isValid) => {
    if (isValid) {
      login(form.username, form.password, form.remember, () => router.push("/index"))
    }
  });
}
</script>

<style scoped>

</style>

```

```vue
<template>
    <div style="text-align: center;margin: 0 20px">
        <div style="margin-top: 100px">
            <div style="font-size: 25px;font-weight: bold">注册新用户</div>
            <div style="font-size: 14px;color: grey">欢迎注册我们的学习平台，请在下方填写相关信息</div>
        </div>
        <div style="margin-top: 50px">
            <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
                <el-form-item prop="username">
                    <el-input v-model="form.username" :maxlength="8" type="text" placeholder="用户名">
                        <template #prefix>
                            <el-icon><User /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password">
                    <el-input v-model="form.password" :maxlength="16" type="password" placeholder="密码">
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="password_repeat">
                    <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复密码">
                        <template #prefix>
                            <el-icon><Lock /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="email">
                    <el-input v-model="form.email" type="email" placeholder="电子邮件地址">
                        <template #prefix>
                            <el-icon><Message /></el-icon>
                        </template>
                    </el-input>
                </el-form-item>
                <el-form-item prop="code">
                    <el-row :gutter="10" style="width: 100%">
                        <el-col :span="17">
                            <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
                                <template #prefix>
                                    <el-icon><EditPen /></el-icon>
                                </template>
                            </el-input>
                        </el-col>
                        <el-col :span="5">
                            <el-button type="success" @click="validateEmail"
                                       :disabled="!isEmailValid || coldTime > 0">
                                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                            </el-button>
                        </el-col>
                    </el-row>
                </el-form-item>
            </el-form>
        </div>
        <div style="margin-top: 80px">
            <el-button style="width: 270px" type="warning" @click="register" plain>立即注册</el-button>
        </div>
        <div style="margin-top: 20px">
            <span style="font-size: 14px;line-height: 15px;color: grey">已有账号? </span>
            <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
        </div>
    </div>
</template>

<script setup>
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import router from "@/router";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";

const form = reactive({
    username: '',
    password: '',
    password_repeat: '',
    email: '',
    code: ''
})

const validateUsername = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请输入用户名'))
    } else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
        callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))
    } else {
        callback()
    }
}

const validatePassword = (rule, value, callback) => {
    if (value === '') {
        callback(new Error('请再次输入密码'))
    } else if (value !== form.password) {
        callback(new Error("两次输入的密码不一致"))
    } else {
        callback()
    }
}

const rules = {
    username: [
        { validator: validateUsername, trigger: ['blur', 'change'] },
        { min: 2, max: 8, message: '用户名的长度必须在 2-8 个字符之间', trigger: ['blur', 'change'] },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码的长度必须在 6-16 个字符之间', trigger: ['blur', 'change'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ],
    email: [
        { required: true, message: '请输入邮件地址', trigger: 'blur' },
        {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
    ],
    code: [
        { required: true, message: '请输入获取的验证码', trigger: 'blur' },
    ]
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

const onValidate = (prop, isValid) => {
    if(prop === 'email')
        isEmailValid.value = isValid
}

const register = () => {
    formRef.value.validate((isValid) => {
        if(isValid) {
            post('/api/auth/register', {
                username: form.username,
                password: form.password,
                email: form.email,
                code: form.code
            }, () => {
                ElMessage.success('注册成功，欢迎加入我们')
                router.push("/")
            })
        } else {
            ElMessage.warning('请完整填写注册表单内容！')
        }
    })
}

const validateEmail = () => {
    coldTime.value = 60
    get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
        ElMessage.success(`验证码已发送到邮箱: ${form.email}，请注意查收`)
        setInterval(() => coldTime.value--, 1000)
    }, undefined, (message) => {
        ElMessage.warning(message)
        coldTime.value = 0
    })
}
</script>

<style scoped>

</style>

```

#### 2.6.3 主页、欢迎页
```vue
<template>
  <el-container class="main-container">
    <el-header class="main-header">
      <el-image style="height: 30px"
                src="https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/logo.svg"/>
      <div class="tabs">
        <tab-item v-for="item in tabs" :name="item.name"
                  :active="item.id === tab" @click="changePage(item)"/>
        <el-switch style="margin: 0 20px"
                   v-model="dark" active-color="#424242"
                   :active-action-icon="Moon"
                   :inactive-action-icon="Sunny"/>
        <div style="text-align: right;line-height: 16px;margin-right: 10px">
          <div>
            <el-tag type="success" v-if="store.isAdmin" size="small">管理员</el-tag>
            <el-tag v-else size="small">子账户</el-tag>
            {{store.user.username}}
          </div>
          <div style="font-size: 13px;color: grey">"{{store.user.email}}"</div>
        </div>
        <el-dropdown>
          <el-avatar class="avatar"
                     :src="store.user.avatar"/>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="userLogout">
                <el-icon><Back/></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <keep-alive exclude="Security">
            <component :is="Component"/>
          </keep-alive>
        </transition>
      </router-view>
    </el-main>
  </el-container>
</template>

<script setup>
import { logout } from '@/net'
import router from "@/router";
import {Back, Moon, Sunny} from "@element-plus/icons-vue";
import {ref} from "vue";
import {useDark} from "@vueuse/core";
import TabItem from "@/components/TabItem.vue";
import {useRoute} from "vue-router";
import {useStore} from "@/store";

const store = useStore()

const route = useRoute()
const dark = ref(useDark())
const tabs = [
  {id: 1, name: '管理', route: 'manage'},
  {id: 2, name: '安全', route: 'security'}
]
const defaultIndex = () => {
  for (let tab of tabs) {
    if(route.name === tab.route)
      return tab.id
  }
  return 1
}
const tab = ref(defaultIndex())
function changePage(item) {
  tab.value = item.id
  router.push({name: item.route})
}

function userLogout() {
  logout(() => router.push("/"))
}
</script>

<style scoped>
.main-container {
  height: 100vh;
  width: 100vw;

  .main-header {
    height: 55px;
    background-color: var(--el-bg-color);
    border-bottom: solid 1px var(--el-border-color);
    display: flex;
    align-items: center;

    .tabs {
      height: 55px;
      gap: 10px;
      flex: 1px;
      display: flex;
      align-items: center;
      justify-content: right;
    }
  }

  .main-content {
    height: 100%;
    background-color: #f5f5f5;
  }
}

.dark .main-container .main-content {
  background-color: #232323;
}
</style>

```

```vue
<template>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <div style="flex: 1">
      <el-image style="width: 100%;height: 100%" fit="cover"
                src="https://jz-cbq-1311841992.cos.ap-beijing.myqcloud.com/images/login-back.jpg"/>
    </div>
    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到晋中学院</div>
      <div style="margin-top: 10px">士不可以不弘毅，任重而道远。</div>
    </div>
        <div class="right-card">
            <router-view v-slot="{ Component }">
                <transition name="el-fade-in-linear" mode="out-in">
                    <component :is="Component" style="height: 100%"/>
                </transition>
            </router-view>
        </div>
    </div>
</template>

<script setup>

</script>

<style scoped>
.right-card {
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color);
}

.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
    color: white;
    text-shadow: 0 0 10px black;
}
</style>
```


### 2.7 App.vue

> [!IMPORTANT]
>
> `App.vue` 是 Vue 应用的根组件。所有的页面级组件都是在这个组件下作为子组件被引用的。它是应用的主入口文件

```vue
<script setup>
import { useDark, useToggle } from '@vueuse/core'

useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
})

useDark({
  onChanged(dark) { useToggle(dark) }
})

</script>

<template>
  <header>
    <div class="wrapper">
      <router-view/>
    </div>
  </header>
</template>

<style scoped>
header {
  line-height: 1.5;
}
</style>

```
### 2.8 main.js
> [!IMPORTANT]
>
> `main.js` 是 Vue 应用的主入口文件。这个文件负责创建 Vue 实例，挂载应用，并引入需要的资源，如路由、状态管理库等

```js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import axios from "axios";

import '@/assets/css/element.less'
import 'flag-icon-css/css/flag-icons.min.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import {createPinia} from "pinia";
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

axios.defaults.baseURL = 'http://localhost:8080'

const app = createApp(App)

const pinia = createPinia()
app.use(pinia)
pinia.use(piniaPluginPersistedstate)
app.use(router)

app.mount('#app')

```
### 2.9 vite.config.js
> [!IMPORTANT]
>
> `vite.config.js` 是 Vite 工具的配置文件（如配置项目的构建、开发服务器、插件等）

```js
import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers'
import {fileURLToPath, URL} from 'node:url';

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [
        vue(),
        AutoImport({
            resolvers: [ElementPlusResolver()],
        }),
        Components({
            resolvers: [ElementPlusResolver()],
        }),
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
})

```


## 三、其它

### 3.1 项目依赖说明

#### 3.1.1 依赖

- dependencies
  1. `@element-plus/icons-vue`: Element Plus 的 Vue 图标库，提供了一系列可直接在 Vue 项目中使用的图标组件
  2. `@vueuse/core`: 一套基于 Composition API 的 Vue 实用函数库，提供了许多常用功能的封装
  3. `axios`: 一个基于 promise 的 HTTP 客户端，用于在浏览器和 node.js 中发送 HTTP 请求
  4. `element-plus`: 一套为开发者、设计师和产品经理准备的基于 Vue 3 的组件库
  5. `flag-icon-css`: 包含各国国旗图标的 CSS 库
  6. `pinia`: Vue 的官方状态管理库，为 Vue 3 设计，提供了更轻量和更灵活的状态管理能力
  7. `pinia-plugin-persistedstate`: 一个 Pinia 插件，用于将状态持久化到本地存储，以便在页面刷新后保持状态
  8. `vue`: Vue.js 框架的核心库，用于构建用户界面
  9. `vue-router`: Vue.js 的官方路由库，用于构建单页面应用（SPA）

- devDependencies
  1. `@vitejs/plugin-vue`: Vite 的官方 Vue 插件，用于支持 Vue 3 单文件组件（.vue 文件）
  2. `less`: 一种向 CSS 添加动态行为的预处理器
  3. `unplugin-auto-import`: 一个 Vite/webpack 插件，可以自动导入 Vue Composition API、Vue Router、Pinia 等库中使用的 API，减少手动导入的需要
  4. `unplugin-vue-components`: 一个 Vite/webpack 插件，可以自动导入和注册 Vue 组件，减少手动导入组件的需要
  5. `vite`: 一个现代化的前端构建工具，旨在提供一个快速的开发服务器和针对生产的优化

#### 3.1.2 scripts

- dev 项目 dev
- build 项目构建
- preview 项目预览

#### 3.1.3 package.json

```json
{
    "name": "monitor-web-ui",
    "private": true,
    "version": "1.0.0",
    "scripts": {
        "dev": "vite --open",
        "build": "vite build",
        "preview": "vite preview"
    },
    "dependencies": {
        "@element-plus/icons-vue": "^2.1.0",
        "@vueuse/core": "^10.3.0",
        "axios": "^1.4.0",
        "element-plus": "^2.3.9",
        "flag-icon-css": "^4.1.7",
        "pinia": "^2.1.7",
        "pinia-plugin-persistedstate": "^3.2.0",
        "vue": "^3.3.4",
        "vue-router": "^4.2.4"
    },
    "devDependencies": {
        "@vitejs/plugin-vue": "^4.2.3",
        "less": "^4.2.0",
        "unplugin-auto-import": "^0.15.2",
        "unplugin-vue-components": "^0.24.1",
        "vite": "^4.4.6"
    }
}

```

