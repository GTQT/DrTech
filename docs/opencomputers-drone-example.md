# OpenComputers 无人机回调示例

## Lua

```lua
local fleet = component.drtech_drone_fleet
local page = fleet.query(0, 16)
if page.ok then
  for _, drone in ipairs(page.entries) do
    print(drone.id, drone.status)
  end
end
```

任务提交和取消都应检查返回值：

```lua
local result = fleet.submit("transport:iron_ingot:64")
if not result.ok then error(result.error) end
fleet.cancel(result.jobId)
```

## 中文说明

`drtech_drone_dock`、`drtech_drone_programmer` 和 `drtech_drone_fleet` 是稳定组件名。所有查询接口使用从 0 开始的页码，单页最多 64 条；失败返回 `ok=false` 和 `error`，不会抛出未捕获的 Java 异常。未安装 OpenComputers 时核心无人机功能仍可正常运行。
