# 六、编码实现

## 6.1 项目后端实现

### 1.1 项目结构说明

![image-20240511222435402](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511222435402.png)

### 1.2 项目环境搭建

1. 克隆此仓库 `git clone https://github.com/caobaoqi1029/monitor.git --depth=1 && cd monitor`
2. 安装依赖通过 `maven`安装即可
3. 安装环境 (通过 docker 直接安装) `cd docker/path-install ` 然后 `docker compose up -d`

![image-20240511222640076](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240511222640076.png)

4. 参照 `application-prod.yaml`的信息配置 `application-dev.yaml` 即可
5. SpringBoot 启动 

![image-20240512150352263](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/image-20240512150352263.png)

 ### 1.3 项目部署

项目通过 `github workflow server/build.yaml`打包为 jar 后发布到 release 通过 `java -jar`运行即可 其依赖环境如 `mysql、redis、RabbitMQ、influxdb` 等通过 `docker compose` （docker/path-install/docker-compose.yml）的方式进行构建

```yaml
name: release
permissions:
  contents: write
on:
  push:
    tags:
      - v*

jobs:
  build:
    name: build and release
    runs-on: ${{ matrix.os }}
    defaults:
      run:
        shell: bash
        working-directory: ./server

    if: startsWith(github.ref, 'refs/tags/')
    strategy:
      fail-fast: false
      matrix:
        os: [ubuntu-latest]

    steps:
      - name: 读取仓库内容 👓
        uses: actions/checkout@v4

      - name: 构建 docker-compose.yaml 文件 🔨
        run: |
          cd ..
          zip -r ./docker-compose-server.zip ./docker
          echo "docker-compose-server.zip 打包成功"
          cp ./server-docker-compose.zip ./build/


      - name: 设置 JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'adopt'

      - name: 构建 jar 包 🔨
        run: mvn package -DskipTests

      - name: upload artifacts 📦
        uses: actions/upload-artifact@v4
        with:
          name: ${{ matrix.os }}
          path: build

      - name: release 😶‍🌫️
        uses: softprops/action-gh-release@v1
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: 'build/**'
        env:
          GITHUB_TOKEN: ${{ secrets.ACCESS_TOKEN }}
```

## 6.2 项目前端实现

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

## 6.3 项目文档构建

### 6.3.1 项目结构说明



### 6.3.2 环境搭建



### 6.3.3 部署

```yaml
name: CI|CD 部署文档
permissions:
  contents: write
on:
  push:
    tags:
      - v*

jobs:
  build:
    name: build vite press
    runs-on: ${{ matrix.os }}
    defaults:
      run:
        shell: bash

    if: startsWith(github.ref, 'refs/tags/')
    strategy:
      fail-fast: false
      matrix:
        os: [ ubuntu-latest ]

    steps:
      - name: 读取仓库内容 👓
        uses: actions/checkout@v4
        with:
          fetch-depth: 0

      - name: 设置 Node.js 🎶
        uses: actions/setup-node@v4
        with:
          node-version: 20

      - name: 安装 yarn 🐸
        run: npm install --global yarn

      - name: 构建文档 🔨
        env:
          NODE_OPTIONS: --max_old_space_size=8192
        run: |-
          yarn install
          yarn run docs:build
          > docs/.vitepress/dist/.nojekyll

      - name: 打包 zip 文件 🔨
        run: |
          mkdir -p ./build
          zip -r ./pages.zip ./docs/.vitepress/dist
          echo "pages.zip 打包成功"
          cp ./pages.zip ./build/

      - name: upload artifacts 📦
        uses: actions/upload-artifact@v4
        with:
          name: ${{ matrix.os }}
          path: build

      - name: release 😶‍🌫️
        uses: softprops/action-gh-release@v1
        if: startsWith(github.ref, 'refs/tags/')
        with:
          files: 'build/**'
        env:
          GITHUB_TOKEN: ${{ secrets.ACCESS_TOKEN }}

      - name: 部署文档 👌
        uses: JamesIves/github-pages-deploy-action@v4
        with:
          branch: pages
          folder: docs/.vitepress/dist

```

![image-20240516182545151](https://2024-cbq-1311841992.cos.ap-beijing.myqcloud.com/picgo/202405161825256.png)

## 其它
