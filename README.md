# SimpleCode

![Java](https://img.shields.io/badge/Java-21-blue)
![Gradle](https://img.shields.io/badge/Gradle-9.3.0-green)

**SimpleCode** 是一个基于大语言模型的智能编码助手。它能够理解你的项目结构，自动执行文件操作、命令运行、代码分析等任务，并支持复杂的多步骤工作流。

## 核心特性

- **自然语言交互**：用日常语言描述需求，AI 自动完成编码任务
- **内置工具集**：文件读写、搜索、命令执行、用户询问等开箱即用
- **计划模式**：复杂任务先制定计划，确认后再执行
- **TODO 管理**：任务拆解与进度追踪
- **子代理**：并行处理子任务，提高效率
- **推理强度调节**：根据任务复杂度动态调整模型推理深度

## 快速开始

### 安装

使用 [PackageIris](https://github.com/AA8043/PackageIris)

```bash
pi install AA8043/SimpleCode
```

### 运行

```bash
simplecode
```

## 项目结构

```
SimpleCode/
├── core/                    # 核心
│   ├── src/main/java/org/a8043/simpleCode/
│   │   ├── api/            # API 接口与实现
│   │   ├── model/          # 模型（Provider、Model）
│   │   ├── session/        # 会话与内容管理
│   │   └── tools/          # 内置工具实现
│   └── src/main/resources/ # 提示词与配置
├── cli/                    # 命令行界面（Tamboui TUI）
│   ├── src/main/java/org/a8043/simpleCode/cli/
│   │   └── views/          # 界面
│   └── src/main/resources/ # 样式与语言
└── docs/                   # 使用文档
```

## 文档

- [快速开始](docs/getting-started.md) — 安装、配置与首次运行
- [功能特性](docs/features.md) — 核心能力详解
- [会话管理](docs/sessions.md) — 多会话与持久化
- [配置说明](docs/configuration.md) — Provider、模型与工作目录

## 数据存储

应用数据默认保存在用户目录下：

```
~/.simpleCode/
├── settings.json        # 设置
├── cli.json             # CLI 设置
└── folders/             # 按工作目录组织的数据
    └── <workdir>/       # 工作目录绝对路径作为目录名
        └── sessions/    # 会话记录
```

## 快捷键

在主界面：

- `Ctrl+K`: 创建新会话
- `ESC`: 退出应用

在会话界面：

- `ESC`: 返回主界面
- `Ctrl+A`: 切换自动模式
- `Ctrl+W`: 切换永久模式
- `Enter`: 发送消息

## 技术栈

- **语言**: Java 21
- **构建**: Gradle 9.3.0
- **UI**: Tamboui（TUI 终端界面）
- **工具库**: Hutool
- **日志**: Log4j2

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可

详见 [LICENSE](LICENSE) 文件。
