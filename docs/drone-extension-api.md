# DrTech 无人机扩展 API

## 生命周期

DrTech 在预初始化期间创建一套权威注册表，然后依次向 `MinecraftForge.EVENT_BUS` 发布：

1. `DroneNodeRegistryEvent`
2. `DroneActionRegistryEvent`
3. `DroneSensorRegistryEvent`
4. `DroneModuleRegistryEvent`
5. `DroneExtensionDisplayRegistryEvent`

事件结束后，节点、动作和传感器注册表立即冻结。编辑器、程序编译器、程序卡导入、无人机实体运行时以及 OpenComputers 编译回调全部使用同一份冻结快照。重复 ID、空处理器和冻结后的修改会直接拒绝，不会静默覆盖。

扩展模组应使用 `@Mod.EventBusSubscriber` 的静态监听器，确保监听器在 DrTech 预初始化前已经注册。

## 完整注册示例

```java
@Mod.EventBusSubscriber(modid = "example")
public final class ExampleDroneExtension {
    private static final ResourceLocation EXTENSION =
            new ResourceLocation("example", "drone_tools");
    private static final ResourceLocation SCANNER =
            new ResourceLocation("example", "scanner");
    private static final ResourceLocation SCAN_NODE =
            new ResourceLocation("example", "scan_block");

    private static final DroneExtensionDescriptor DESCRIPTOR =
            new DroneExtensionDescriptor(
                    EXTENSION,
                    1,
                    Arrays.asList("drone.scan"),
                    Arrays.asList(SCANNER));

    @SubscribeEvent
    public static void registerNodes(DroneNodeRegistryEvent event) {
        DroneNodeDefinition node = DroneNodeDefinition
                .builder(SCAN_NODE, DroneNodeDefinition.FlowRole.NORMAL)
                .category("sensors")
                .port(DronePortDefinition.input("in", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("next", DronePortType.FLOW, true))
                .port(DronePortDefinition.output("failed", DronePortType.FLOW, false))
                .build();

        // 使用此重载把节点和扩展描述绑定起来，客户端才能进行兼容检查。
        event.register(node, DESCRIPTOR);
    }

    @SubscribeEvent
    public static void registerActions(DroneActionRegistryEvent event) {
        event.register(SCAN_NODE, context -> {
            // 读取 context 和服务端环境，禁止访问客户端类。
            return DroneExecutionResult.success("next");
        });
    }

    @SubscribeEvent
    public static void registerModules(DroneModuleRegistryEvent event) {
        event.register(new DroneModuleDefinition(
                SCANNER, "item.example.drone_scanner.name"));
    }

    @SubscribeEvent
    public static void registerDisplay(DroneExtensionDisplayRegistryEvent event) {
        event.register(new DroneExtensionDisplay(
                EXTENSION,
                "example.drone.extension.title",
                "example.drone.extension.description",
                new ResourceLocation("example", "textures/gui/drone_tools")));
    }
}
```

数值型节点或传感器通过 `DroneSensorRegistryEvent.register(ResourceLocation, DroneValueEvaluator)` 注册。动作节点通过 `DroneActionRegistryEvent.register(ResourceLocation, DroneNodeExecutor)` 注册。不要把 Java 类名写入程序 NBT；程序格式只保存稳定 `ResourceLocation` 和节点配置。

## 描述符边界

`DroneExtensionDescriptor` 是不可变对象：

- 扩展 ID 必须是非空 `ResourceLocation`。
- 版本范围为 1–1,000,000；当前 DrTech 扩展 API 版本为 `DroneExtensionRegistry.API_VERSION`。
- 权限最多 32 项，每项最多 64 字符并自动去重。
- 必需模块最多 32 项并自动去重。
- 全局最多接收 1024 个扩展描述符和 4096 个节点到扩展的绑定。
- 同一扩展 ID 可以绑定多个节点，但描述内容必须完全一致；冲突描述会拒绝。

`permissions` 是稳定的声明信息，供保护模组、整合包策略和后续权限服务读取，不代表绕过无人机现有的主人、区块保护或模块检查。

## 缺失模块与版本兼容

`DroneExtensionAvailability.resolve(descriptor)` 会使用当前 API 版本和已经注册的模块生成以下状态之一：

- `AVAILABLE`
- `MISSING_MODULE`
- `VERSION_INCOMPATIBLE`

编辑器仍能安全显示旧程序中的不可用扩展节点，但使用琥珀色占位标题；编译器生成 `EXTENSION_UNAVAILABLE` 错误，因此实体不会构造或执行对应运行逻辑。显示注册只保存本地化键和资源 ID，不引用客户端实现类，服务端可以安全加载。

第三方工具可读取以下只读快照：

```java
DroneExtensionRegistry.nodes().values();
DroneExtensionRegistry.actions().snapshot();
DroneExtensionRegistry.sensors().snapshot();
DroneExtensionRegistry.modules();
DroneExtensionRegistry.descriptors();
DroneExtensionRegistry.displays();
DroneExtensionRegistry.nodeExtensions();
```

## 实体运输保护事件

无人机装载或释放所属实体前，会发布可取消的 `DroneEntityTransportEvent`。取消不会扣除 EU、删除目标或清空已有载荷。

```java
@SubscribeEvent
public static void onDroneEntityTransport(DroneEntityTransportEvent event) {
    if (!canModify(event.getDrone().getOwnerId(), event.getTarget())) event.setCanceled(true);
}
```

`Action.LOAD` 在实体从世界移除前发布；`Action.RELEASE` 在实体重新加入世界前发布。无人机自己的模块、所有权、区块、NBT 大小和碰撞检查始终继续生效。

## 告示牌编辑保护事件

`edit_sign` 通过 FakePlayer 右键授权后、扣除 EU 和写入文本前，会发布可取消的 `DroneSignEditEvent`。前后文本均为防御性副本。

```java
@SubscribeEvent
public static void onDroneSignEdit(DroneSignEditEvent event) {
    if (!canEditSign(event.getDrone().getOwnerId(), event.getTarget(), event.getProposedLines())) {
        event.setCanceled(true);
    }
}
```

取消事件不会扣除 EU，也不会修改 `TileEntitySign`。
