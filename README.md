# 体重记录 App

[![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)](https://github.com/kleinlsl/stitch_log_weight/releases/tag/v1.0.0)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com/)
[![License](https://img.shields.io/badge/license-Internal-red.svg)](#)

一款基于 Jetpack Compose 的 Android 体重追踪应用，帮助用户记录和管理体重数据，保持健康生活方式。

## 🎉 最新版本

**V1.0.0** (2026-03-23) - [更新日志](docs/CHANGELOG.md) | [发布页面](https://github.com/kleinlsl/stitch_log_weight/releases/tag/v1.0.0)

## ✨ 功能特性

### 核心功能
- 📊 **体重记录** - 支持日期、时间、体重、心情、备注记录
- 📈 **趋势分析** - 周/月/年趋势图表，直观展示体重变化
- 🎯 **目标追踪** - 设置起始体重和目标体重，实时追踪进度
- 📋 **BMI 分析** - 自动计算 BMI，提供健康建议
- 📅 **历史记录** - 按月度聚合显示，支持编辑和删除

### 数据管理
- 📤 **Excel 导出** - 支持自定义保存路径（SAF）
- 📥 **Excel 导入** - 增量覆盖模式导入
- 💾 **数据备份** - JSON 格式备份到本地
- 🔄 **数据恢复** - 从备份文件恢复数据
- 🗑️ **自动清理** - 清理 1 年前的旧数据
- 📱 **数据迁移** - 导出迁移文件，方便换机

### 界面特性
- 🎨 **Material Design 3** - 现代化设计语言
- 📱 **刘海屏适配** - 支持各种屏幕挖孔和刘海
- 🌓 **深色模式** - 跟随系统主题切换
- 🖼️ **Edge-to-Edge** - 全屏沉浸式体验

## 🛠️ 技术栈

| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI 框架 | Jetpack Compose |
| 架构模式 | MVVM + Clean Architecture |
| 依赖注入 | Hilt |
| 数据库 | Room |
| 构建工具 | Gradle 8.4 |
| 最低 SDK | 26 (Android 8.0) |
| 目标 SDK | 34 (Android 14) |

## 📁 项目结构

```
stitch_log_weight/
├── android/                        # Android 项目
│   ├── app/
│   │   └── src/main/java/com/weighttracker/app/
│   │       ├── data/               # 数据层
│   │       │   ├── file/           # Excel/Backup 管理
│   │       │   ├── local/          # Room 数据库
│   │       │   └── repository/     # 仓库实现
│   │       ├── domain/             # 领域层
│   │       │   ├── model/          # 数据模型
│   │       │   ├── repository/     # 仓库接口
│   │       │   └── usecase/        # 用例
│   │       ├── presentation/       # 表现层
│   │       │   ├── components/     # 可复用组件
│   │       │   ├── navigation/     # 导航
│   │       │   ├── screens/        # 各页面
│   │       │   └── theme/          # 主题
│   │       └── di/                 # Hilt 模块
│   ├── build.gradle.kts
│   └── gradle-8.4/
├── docs/                           # 项目文档
│   ├── TODO.md                     # 任务清单
│   ├── USER_GUIDE.md               # 用户指南
│   ├── TECHNICAL_DESIGN.md         # 技术设计
│   ├── AUTOMATED_TESTING.md        # 测试文档
│   ├── product_requirements_document.md  # PRD
│   ├── DESIGN_SYSTEM.md            # 设计系统
│   └── AGENTS.md                   # 项目知识库
├── images/                         # 设计稿图片
├── stitch_log_weight/              # UI 原型 (HTML)
└── README.md
```

## 🚀 快速开始

### 环境要求
- Android Studio Hedgehog 或更高版本
- JDK 17
- Android SDK 34

### 构建运行

```bash
# 进入 Android 项目目录
cd android

# 构建 Debug APK
export JAVA_HOME=$(/usr/libexec/java_home)
./gradle-8.4/bin/gradle :app:assembleDebug --no-daemon

# APK 输出位置
# app/build/outputs/apk/debug/app-debug.apk
```

## 📖 文档

| 文档 | 说明 |
|------|------|
| [更新日志](docs/CHANGELOG.md) | 版本更新记录 |
| [用户指南](docs/USER_GUIDE.md) | 应用使用说明 |
| [任务清单](docs/TODO.md) | 开发进度追踪 |
| [技术设计](docs/TECHNICAL_DESIGN.md) | 架构设计文档 |
| [PRD](docs/product_requirements_document.md) | 产品需求文档 |
| [测试文档](docs/AUTOMATED_TESTING.md) | 自动化测试说明 |
| [设计系统](docs/DESIGN_SYSTEM.md) | UI 设计规范 |

## 🏗️ 架构说明

```
┌─────────────────────────────────────────────────────┐
│                   Presentation                       │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐│
│  │ Screens │  │   VM    │  │Components│  │ Navigation││
│  └─────────┘  └─────────┘  └─────────┘  └─────────┘│
├─────────────────────────────────────────────────────┤
│                    Domain                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐             │
│  │  Model  │  │Use Cases│  │Repository│ (接口)      │
│  └─────────┘  └─────────┘  └─────────┘             │
├─────────────────────────────────────────────────────┤
│                     Data                            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐             │
│  │  Room   │  │Excel Mgr│  │Backup Mgr│             │
│  └─────────┘  └─────────┘  └─────────┘             │
└─────────────────────────────────────────────────────┘
```

## 📝 开发规范

- 遵循 MVVM + Clean Architecture
- 使用 Hilt 进行依赖注入
- 异步操作使用 Kotlin Coroutines + Flow
- UI 状态使用 StateFlow 管理
- 单元测试覆盖率 > 70%

## 📄 License

本项目为内部项目，仅供学习和参考使用。
