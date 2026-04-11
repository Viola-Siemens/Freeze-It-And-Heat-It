# Freeze-It-And-Heat-It 项目上下文

## 项目概述

**Freeze-It-And-Heat-It**（模组 ID：`fiahi`）是一个 Minecraft 模组，专为整合包“The Winter Frontier”设计。模组核心功能是围绕食物冷冻与加热机制展开，与 Cold Sweat 模组深度联动，为食物系统添加温度相关的玩法。

**版本信息**：

这是一个跨版本 Minecraft 模组，依赖不同环境的工作区位于 GitHub 不同分支中。

- Minecraft 版本：1.18.2 / 1.19.2 / 1.20.1 / 1.21.1
- 模组加载器版本：Forge 40.3.0 / Forge 43.4.0 / Forge 47.2.0 / NeoForge 21.1.192
- Java 版本：17/21

**主要功能**：
- **食物口袋（Food Pouch）**：可储存食物并追踪其温度状态的容器
- **温度系统**：食物可处于冻结（Frozen）或腐烂（Rotten）状态，各有 3 个等级
- **寒颤效果（Shiver Effect）**：自定义药水效果
- **Cold Sweat 联动**：通过 Mixin 与 Boiler（锅炉）和 IceBox（冰盒）交互
- **剩菜系统**：腐烂的食物会转化为剩肉或剩菜

## 技术栈

- **核心框架**：Minecraft Forge / NeoForge
- **构建工具**：Gradle
- **Mixin**：SpongePowered Mixin 0.8.5
- **主要依赖**：
  - Cold Sweat（核心联动模组）
  - JEI（物品管理器，运行时依赖，用于 dev 环境下测试验证）
  - Jade（信息显示，运行时依赖，用于 dev 环境下测试验证）

## 项目结构

```
src/main/java/com/hexagram2021/fiahi/
├── client/          # 客户端渲染、粒子、屏幕
├── common/          # 通用逻辑、配置、物品、菜单
├── mixin/           # Mixin 注入类
└── register/        # 注册器（物品、效果、粒子等）
```

## 构建与运行

### 环境要求
- JDK 17 / JDK 21
- Gradle 包装器已包含

### 构建命令
```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 运行客户端
```bash
./gradlew runClient
```

### 运行服务器
```bash
./gradlew runServer
```

### 数据生成
```bash
./gradlew runData
```

构建产物位于 `build/libs/fiahi-{version}.jar`

## 开发规范

### 代码组织
- **包结构**：按功能分层（client/common/mixin/register）
- **注册模式**：使用 `DeferredRegister` 统一管理注册
- **Capability 系统**：使用 Forge Capability 扩展物品行为
- **网络通信**：基于 Forge Network Registry 的 SimpleChannel

### 命名约定
- **包名**：全小写，如 `com.hexagram2021.fiahi.common.item`
- **类名**：大驼峰，前缀统一为 `FIAHI` 或描述性名称
- **模组 ID**：`fiahi`（全小写）
- **资源位置**：`fiahi:xxx` 格式

### Mixin 规范
- Mixin 类位于 `com.hexagram2021.fiahi.mixin` 包
- 配置文件：`fiahi.mixins.json`
- 目标模组 Mixin 使用子包区分（如 `cold_sweat`）
- 所有 Mixin 必须通过 `refmap` 进行映射

### 配置管理
- 配置文件类型：`ModConfig.Type.COMMON`
- 配置类：`FIAHICommonConfig`
- 使用 `@Config` 注解管理配置项

### 本地化
- 支持语言：简体中文（zh_cn）、繁体中文（zh_tw）、英文（en_us）
- 路径：`src/main/resources/assets/fiahi/lang/`
- 所有文本必须本地化，禁止硬编码

## 关键系统说明

### 食物口袋系统
- 使用 NBT 标签 `Items` 存储物品列表
- 温度状态通过 Capability 系统附加到食物
- 客户端-服务端同步通过 `ClientboundFoodPouchPacket` 实现

### 温度状态流转
```
新鲜 → 轻微冻结 → 大部分冻结 → 完全冻结
新鲜 → 轻微腐烂 → 大部分腐烂 → 完全腐烂
```

### Cold Sweat 联动
- **Boiler（锅炉）**：允许放入可加热食物
- **IceBox（冰盒）**：允许放入可冷冻食物
- 通过 Slot Mixin 控制物品放入逻辑

## 调试与测试

### 日志配置
- 控制台日志级别：DEBUG
- 使用 `FIAHILogger` 工具类统一日志输出

### Mixin 调试
- 启用详细日志：`debug.verbose = true`
- 导出重映射：`debug.export = true`
- 检查 `fiahi.refmap.json` 生成状态

### 常见问题排查
1. **Mixin 不生效**：检查 `accesstransformer.cfg` 和 `refmap.json`
2. **物品渲染异常**：验证 `ModelBakery` 注册和 `TextureAtlas` Mixin
3. **网络包不同步**：确认 `messageId` 递增和版本兼容性

## CI/CD

- **触发条件**：push 和 pull_request
- **构建环境**：Ubuntu + JDK 17/21
- **缓存策略**：Gradle 依赖和生成资源
- **产物上传**：GitHub Actions Artifact

## 相关资源

- **CurseForge**：https://legacy.curseforge.com/minecraft/mc-mods/freeze-it-and-heat-it
- **Modrinth**：https://modrinth.com/mod/freeze-it-and-heat-it
- **MC 百科**：https://www.mcmod.cn/class/12102.html
