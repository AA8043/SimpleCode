# 会话管理

## 查看与会话列表

在主界面（MainView）中，你可以：

1. 查看当前工作目录下的所有会话
2. 通过搜索框过滤会话
3. 按 `Enter` 打开会话
4. 按 `Ctrl+K` 创建新会话
5. 按 `ESC` 退出应用

在会话界面（SessionView）中：

- 右侧面板显示 TODO 列表和 Token 统计（prompt、completion、cached）
- 按 `ESC` 返回主界面
- 按 `Ctrl+A` 切换自动模式
- 按 `Ctrl+W` 切换永恒模式

每个会话包含：

- 消息（User / Assistant / System / Tool）
- 工具调用记录（ToolCall）
- TODO 任务列表
- 会话元数据（type、reasoningEffort、autoMode、planMode、foreverMode、allowTool）

## 会话模式

### 自动模式（Auto Mode）

开启后，工具调用无需用户确认，AI 将自动执行。shell 命令会先进行安全评估。

### 计划模式（Plan Mode）

开启后，AI 先制定计划，经确认后再执行。计划模式下仅开放计划相关工具，普通工具隐藏。

### 永恒模式（Forever Mode）

开启后，AI 会在完成当前任务后继续工作。
