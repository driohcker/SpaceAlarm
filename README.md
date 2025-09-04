# SpaceAlarm - 空间闹钟应用

一个基于地理位置的智能闹钟应用，当您到达预设位置时自动提醒您。

## 📱 项目介绍

SpaceAlarm（空间闹钟）是一款创新的位置提醒应用，通过不知道啥技术，让您在到达特定地点时收到提醒，不再错过重要事项。无论是上班打卡、取快递、约会地点还是其他需要位置提醒的场景，SpaceAlarm都能为您提供精准的服务。

## ✨ 主要功能

- **地图定位与标记**：使用百度地图SDK实现精准定位和地图交互
- **地理围栏提醒**：设置特定地点的提醒范围，到达时自动触发通知
- **自定义提醒设置**：支持设置提醒标题、地址、范围半径
- **通知与震动**：支持自定义通知铃声和震动模式
- **语音朗读**：支持语音播报提醒内容
- **前台定位服务**：确保应用在后台持续运行并准确监测位置
- **权限管理**：完善的权限请求和管理机制

## 🛠️ 技术栈

- **开发语言**：Java 11
- **开发平台**：Android
- **最低支持版本**：Android 12 (API 31)
- **目标版本**：Android 14 (API 36)
- **核心依赖**：
  - 百度地图SDK (BaiduLBS_Android.jar)
  - AndroidX & Material Design
  - RecyclerView
  - OkHttp
  - Gson
  - Firebase Crashlytics

## 📁 项目结构

```
src/main/java/com/example/spacealarm/
├── activity/           # 应用界面活动
│   ├── MainActivity.java         # 主活动
│   ├── PermissionActivity.java   # 权限请求界面
│   └── OptimizeSettingsActivity.java  # 优化设置界面
├── fragment/           # 应用界面碎片
│   ├── AlarmFragment.java        # 闹钟列表界面
│   ├── MapFragment.java          # 地图界面
│   └── SettingsFragment.java     # 设置界面
├── service/            # 服务组件
│   ├── AlarmService.java         # 闹钟管理服务
│   ├── BaiduLocationService.java # 百度定位服务
│   ├── ForegroundLocationService.java # 前台定位服务
│   ├── NotificationService.java  # 通知服务
│   └── TextToSpeechManager.java  # 语音朗读管理
├── controller/         # 控制器
│   ├── AlarmController.java      # 闹钟控制器
│   ├── MapController.java        # 地图控制器
│   └── SettingsController.java   # 设置控制器
├── entity/             # 实体类
│   └── Alarm.java                # 闹钟实体
├── mapper/             # 数据访问层
│   ├── AlarmMapper.java          # 闹钟数据接口
│   └── impl/                     # 实现类
│       └── AlarmMapperImpl.java
└── service/manager/    # 服务管理器
    ├── BaiduMapManager.java      # 百度地图管理器
    ├── PermissionManager.java    # 权限管理器
    └── CustomToolbarManager.java # 自定义工具栏管理器
```

## 🚀 快速开始

### 前提条件
- Android Studio Giraffe或更高版本
- Android SDK 31及以上
- 百度地图API密钥（配置在项目中）

### 安装与运行
1. 克隆项目代码：
```bash
git clone https://github.com/your-username/SpaceAlarm.git
```

2. 在Android Studio中打开项目

3. 同步Gradle依赖

4. 运行应用到Android设备或模拟器

## 📱 使用指南

### 1. 首次启动
- 允许应用获取定位权限、存储权限等必要权限
- 根据引导完成初始设置

### 2. 添加位置提醒
- 切换到地图界面
- 在地图上选择目标位置或搜索地点
- 点击地图上的位置添加标记
- 设置提醒标题、提醒范围等信息
- 保存设置

### 3. 管理提醒
- 在闹钟列表界面查看所有已设置的提醒
- 可以启用/禁用、编辑或删除提醒
- 点击提醒可以查看详细信息

### 4. 自定义设置
- 在设置界面可以配置震动、声音、语音朗读等选项
- 可以管理后台定位服务

## ⚠️ 注意事项

- 为了确保提醒的准确性，请保持应用在后台运行并开启定位服务
- 持续使用后台定位可能会显著消耗电池电量
- 某些Android设备需要特殊设置以允许应用在后台持续运行

## 🔧 已知问题
- 在部分设备上，后台定位可能会被系统限制
- 语音朗读功能依赖系统TTS引擎，需要用户安装中文语音包
- 通知设置修改后可能需要重启应用才能完全生效

## 🤝 贡献指南

欢迎提交Issue和Pull Request来帮助改进SpaceAlarm。提交代码前，请确保：
- 代码风格与项目保持一致
- 新功能已进行充分测试
- 提交描述清晰明了

## 📄 License

[MIT License](LICENSE)

## 📧 联系我们

如有任何问题或建议，请联系我们：
- Email: 1372487981@qq.com

---

        
