# Redis Lua脚本相关



## Lua中调用Redis相关的命令返回值的转化



| Redis的响应值 | Lua转化的值                  |
| ------------- | ---------------------------- |
| 整形          | number                       |
| 批量          | string                       |
| 多批量        | table                        |
| 状态类型      | table{"ok":"OK"}             |
| 错误类型      | table{"err":"error message"} |
| nil           | false                        |



