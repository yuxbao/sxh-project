# sxh-admin 🚀

## 介绍 📖

sxh-admin 是社区系统的后台管理端，基于 React18、React-Router v6、React-Hooks、Redux、TypeScript、Vite3、Ant-Design 5.x、Hook Admin、ECharts 实现。
<br><br>

## 项目功能

- 🚀 采用最新技术找开发：React18、React-Router v6、React-Hooks、TypeScript、Vite3
- 🚀 采用 Vite3 作为项目开发、打包工具（配置了 Gzip 打包、跨域代理、打包预览工具……）
- 🚀 整个项目集成了 TypeScript （学期来很酷哦 🤣）
- 🚀 使用 redux 做状态管理，集成 immer、react-redux、redux-persist 开发
- 🚀 使用 TypeScript 对 Axios 整个二次封装 （全局错误拦截、常用请求封装、全局请求 Loading、取消重复请求……）
- 🚀 支持 Antd 组件大小切换、暗黑 && 灰色 && 色弱模式
- 🚀 使用 自定义高阶组件 进行路由权限拦截（403 页面）、页面按钮权限配置
- 🚀 支持 React-Router v6 路由懒加载配置、菜单手风琴模式、无限级菜单、多标签页、面包屑导航
- 🚀 使用 Prettier 统一格式化代码，集成 Eslint、Stylelint 代码校验规范（项目规范配置）
- 🚀 使用 husky、lint-staged、commitlint、commitizen、cz-git 规范提交信息（项目规范配置）

## 安装使用步骤

### Install：

```text
npm install
cnpm install

# npm install 安装失败，请升级 nodejs 到 16 以上，或尝试使用以下命令：
npm install --registry=http://registry.npmmirror.com

# npm install 如果出现 npm ERR! code ECONNRESET 错误，可尝试执行以下命令后再安装
npm config set registry http://registry.npmjs.org/
```

### Run：

启动 Redis 和服务端后，再启动 admin 端，可以通过 VSCode 来进行开发。

```text
npm run dev
```

会自动在浏览器打开 [http://127.0.0.1:3301](http://127.0.0.1:3301)，如下所示。

本地的用户名和密码均为 admin 和 admin 。

如果遇到 nodejs 环境的问题实在无法启动，可能是一些依赖包的问题，可以尝试删除 node_modules 文件夹后重新安装依赖包。

异常堆栈：

解决方法 1：升级 nodejs 到 18 以上，升级 npm 到 9 以上，然后重新 install。

解决方法 2：删除 node_modules 文件夹，清理 npm 缓存后重新安装依赖。

然后再执行 `npm run dev`。

### Build：

```text
# 生产环境
npm run build:pro
```

## 文件资源目录

```text
sxh-admin
├─ .vscode                # vscode推荐配置
├─ public                 # 静态资源文件（忽略打包）
├─ src
│  ├─ api                 # API 接口管理
│  ├─ assets              # 静态资源文件
│  ├─ components          # 全局组件
│  ├─ config              # 全局配置项
│  ├─ enums               # 项目枚举
│  ├─ hooks               # 常用 Hooks
│  ├─ language            # 语言国际化
│  ├─ layouts             # 框架布局
│  ├─ routers             # 路由管理
│  ├─ redux               # redux store
│  ├─ styles              # 全局样式
│  ├─ typings             # 全局 ts 声明
│  ├─ utils               # 工具库
│  ├─ views               # 项目所有页面
│  ├─ App.tsx             # 入口页面
│  ├─ main.tsx            # 入口文件
│  └─ env.d.ts            # vite 声明文件
├─ .editorconfig          # 编辑器配置（格式化）
├─ .env                   # vite 常用配置
├─ .env.development       # 开发环境配置
├─ .env.production        # 生产环境配置
├─ .env.test              # 测试环境配置
├─ .eslintignore          # 忽略 Eslint 校验
├─ .eslintrc.js           # Eslint 校验配置
├─ .gitignore             # git 提交忽略
├─ .prettierignore        # 忽略 prettier 格式化
├─ .prettierrc.js         # prettier 配置
├─ .stylelintignore       # 忽略 stylelint 格式化
├─ .stylelintrc.js        # stylelint 样式格式化配置
├─ CHANGELOG.md           # 项目更新日志
├─ commitlint.config.js   # git 提交规范配置
├─ index.html             # 入口 html
├─ LICENSE                # 开源协议文件
├─ lint-staged.config     # lint-staged 配置文件
├─ package-lock.json      # 依赖包包版本锁
├─ package.json           # 依赖包管理
├─ postcss.config.js      # postcss 配置
├─ README.md              # README 介绍
├─ tsconfig.json          # typescript 全局配置
└─ vite.config.ts         # vite 配置
```

## 生产环境部署

1、执行 `npm run build:pro`，生成 dist 目录

2、将 dist 目录上传到服务器的 `/home/admin/` 目录下

或者执行 `zip -r dist.zip dist` 压缩为 dist.zip 包，然后上传到服务器的 `/home/admin/` 目录下。再执行 `unzip dist.zip` 解压即可。

3、如果采用 Nginx 的话，请在 server 节点下进行 location 配置。

```
location ^~ /admin {
	alias /home/admin/dist/; # 根 目 录
	index index.html;
}
```

### launch.sh

辅助 shell 脚本，针对 mac/linux 用户而言，提供更好的使用姿势

0. 前提说明

当 launch.sh 执行时，提示 `$‘\r‘: command not found`时，主要原因是 windows 系统编写的 shell 脚本，每行结尾是`\r\n`， 而 linux 的结尾是`\n`，可以通过下面几种方式进行处理

```bash
# case1
sed -i 's/\r//' launch.sh

# case2
# sudo apt-get install -y dos2unix
sudo yum install -y dos2unix
dos2unix launch.sh
```

1.安装依赖：

```bash
./launch.sh install
```

2.本地启动：

```bash
./launch.sh server
```

3.打包上传服务器，并使他生效

```bash
# 下面这个动作，包含以下几步
# 1. 打包 -> 生成 dist 目录， 压缩为 dist.tar.gz 包
# 2. 上传到服务器
# 3. 将之前旧的静态资源备份，然后解压新的上传包
./launch.sh pro
```

## 许可证

[Apache License 2.0](./LICENSE)

Copyright (c) 2022-2023 思享汇
