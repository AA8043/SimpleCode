# 功能特性

## 工具调用

SimpleCode 内置多种工具，AI 可根据任务自动调用：

| 工具                                                      | 说明                 |
|-----------------------------------------------------------|----------------------|
| `read_file`                                               | 读取文件内容         |
| `write_file`                                              | 创建或修改文件       |
| `search_file`                                             | 按名称或内容搜索文件 |
| `list_files`                                              | 列出目录下的文件     |
| `run_command`                                             | 执行系统命令         |
| `ask_user`                                                | 向用户提问并获取输入 |
| `reasoning_effort`                                        | 调整推理强度         |
| `list_todo` / `add_todo` / `update_todo`                  | 任务管理             |
| `create_sub_agent` / `wait_sub_agent` / `list_sub_agents` | 子代理管理           |
| `enter_plan_mode` / `exit_plan_mode` / `update_plan`      | 计划模式             |

## 计划模式（Plan Mode）

进入计划模式后，AI 会先制定详细的实施计划，适合复杂任务。

在计划模式下，仅开放计划模式专用的工具，普通工具暂时隐藏。

## 任务管理（TODO）

AI 可将复杂任务拆解为 TODO 项，并实时更新进度。

## 子代理（SubAgent）

可将子任务委托给独立的子代理执行，实现并行处理和信息隔离。

子代理有两种类型：

- **normal**: 普通子代理
- **explore**: 探索型子代理

## 安全机制

- **自动模式安全评估**: 自动模式下执行 shell 命令前，会调用低等级模型进行安全评估，判断命令是否安全
- **工具调用确认**: 非自动模式下，工具调用前需用户确认

## 数据持久化

- 会话数据每 3 秒自动保存一次
- 退出时自动保存 Settings 和 CLI 设置
- 支持多工作目录，数据按目录隔离存储

## 快捷键

### 主界面（MainView）

- `Ctrl+K`: 创建新会话
- `ESC`: 退出应用

### 会话界面（SessionView）

- `ESC`: 返回主界面
- `Ctrl+A`: 切换自动模式
- `Ctrl+W`: 切换永恒模式
- `Enter`: 确认选择 / 发送消息

## 永恒模式（Forever Mode）

开启后，AI 会在完成当前任务后继续工作。
