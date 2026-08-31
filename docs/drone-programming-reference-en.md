# DrTech Drone Visual Programming Guide

[中文版速查](drone-programming-reference.md) · [完整中文 145 节点手册](drone-module-manual.md) · [Devices](drone-device-guide-en.md) · [Fleet logistics](drone-fleet-guide-en.md)

This guide describes the current built-in 145-node registry, graph rules, runtime statuses, and hardware requirements. Internal IDs are stable `drtech:<id>` values and remain searchable in either game language.

## 1. Graph and Editor Rules

Flow ports use arrows; Boolean ports diamonds; numbers circles; coordinates crosshairs; areas hollow squares. Port shape remains meaningful without colour. Required inputs must have exactly one valid source unless the port explicitly accepts multiple flow inputs. Data nodes are evaluated on demand; action nodes advance through flow edges over ticks.

Every executable graph needs one Start, a reachable End, valid data types, and no illegal flow cycle. `repeat`, `while`, and `for_each_*` provide legal loop back-edges. A cycle with no exit is an error; a cycle that may never exit is a warning. Compile errors block writing and do not consume EU.

The editor supports search by localized name, internal ID, category, and underscore-as-space aliases. It also supports multi-selection, graph copy/paste, aliases, conditional breakpoints, groups/comments, auto-layout, minimap, world point/area selection, diagnostics, and a persistent 256-entry remote timeline.

## 2. Node Catalogue by Category

The following lists cover every built-in node ID. Names shown in game are localized.

### Flow, loops, subprograms, and events

`start`, `end`, `comment`, `group`, `wait`, `branch`, `repeat`, `while`, `for_each_coordinate`, `for_each_item_filter`, `current_item_filter`, `break_loop`, `continue_loop`, `wait_for_redstone`, `wait_for_owner`, `program_reference`, `call_program`.

- `branch` selects true/false flow from a Boolean.
- `repeat` is bounded; `while` reevaluates its condition each iteration.
- `for_each_coordinate` supports deterministic, nearest, vertical, reverse, randomized, air-skip, and block-filter traversal.
- `call_program` uses an owner-authorized program reference and a pinned revision; recursion/depth limits are enforced.

### Coordinates, areas, shapes, and filters

`coordinate`, `coordinate_offset`, `area`, `area_from_corners`, `sphere_area`, `cylinder_area`, `pyramid_area`, `path_area`, `plane_area`, `cuboid_surface_area`, `grid_area`, `random_points_area`, `area_union`, `area_intersection`, `area_difference`, `area_offset`, `area_expand`, `area_inset`, `area_boundary`, `area_scale`, `area_contains`, `area_volume`, `item_filter`, `block_filter`, `fluid_filter`, `entity_filter`.

Static points and two-corner areas can be selected in the world. Large procedural shapes are built incrementally at up to 1,024 candidate points per tick. Final regions must remain within the chassis limit: HV 1,024, EV 2,048, IV 4,096 coordinates, with the validated per-axis bound.

Filters support whitelist/blacklist behavior. Item filters may use registry ID, metadata/NBT, ore dictionary, or namespace rules; block filters may match BlockState properties; entity filters use registry IDs and explicit owned-entity permission.

### Navigation, docks, and safety

`move_to`, `return_to_dock`, `charge_until`, `find_nearest_dock`, `dock_reference`, `bind_dock`, `unbind_dock`, `configure_safety`, `coordinate_reachable`, `dock_available`.

Movement uses tick-based path following and reports unreachable/unloaded targets rather than teleporting. Dock selection is owner-, dimension-, voltage-, online-, and availability-aware. Safety firmware interrupts normal execution at its return threshold and resumes only after the configured recovery threshold.

### Blocks, crops, trees, and world interaction

`break_block`, `break_block_at`, `place_block`, `place_area`, `fell_trees`, `replant_area`, `harvest_crop`, `interact_block`, `use_item_on_block`, `use_item`, `edit_sign`, `set_redstone_output`, `redstone_strength`, `redstone_output_level`, `light_level`, `block_matches`, `area_block_count`.

Block changes use normal FakePlayer permission/protection hooks. Region actions process bounded work over ticks. Replanting handles saplings and ordinary crops; harvesting supports vanilla crops and DrTech CropQT hybrid crops without destroying their identity/genetics.

### Item, fluid, EU, and crafting logistics

`import_items`, `export_items`, `pickup_dropped_items`, `drop_items`, `cargo_item_count`, `cargo_free_slots`, `cargo_used_percent`, `inventory_item_count`, `import_fluid`, `export_fluid`, `drain_fluid`, `drone_fluid_amount`, `drone_fluid_percent`, `container_fluid_amount`, `find_fluid_container`, `wait_for_fluid_amount`, `import_eu`, `export_eu`, `charge_target_percent`, `energy_level`, `target_energy`, `target_energy_capacity`, `target_energy_percent`, `craft_items`, `can_craft`, `craftable_count`, `craft_grid`.

Import/export actions accept either one coordinate or one area, never both. Area mode supports nearest/ordered/random search and unavailable-target skipping. Partial successful transfer is retained and reported. Fluid actions require Fluid Cargo; EU actions require EU Interface and enforce voltage/packet safety. Crafting requires the Crafting module and uses real cargo ingredients/results.

### Machines, GregTech maintenance, and Thaumcraft

`set_machine_working`, `wait_machine_idle`, `machine_active`, `machine_enabled`, `machine_progress`, `wait_machine_cycle`, `machine_waiting_input`, `machine_output_blocked`, `machine_low_energy`, `machine_diagnostic`, `machine_needs_maintenance`, `machine_maintenance_problems`, `repair_machine`, `transfer_thaumcraft_essentia`.

Machine actions inspect real GT capabilities and fail cleanly when unsupported or unloaded. Repair requires Tool Arm hardware and actual maintenance resources. Essentia transport requires the Alchemy Upgrade plus a jar in the dedicated slot. Connect the furnace coordinate and optionally a jar area; without an area it scans `5×5×5` around the furnace. Label-matched non-full jars have priority, and successful transfers are at least 10 ticks apart.

### Entities, combat, transport, and fishing

`interact_entity`, `use_item_on_entity`, `follow_entity`, `avoid_entity`, `attack_entity`, `patrol_attack_area`, `load_entity`, `release_entity`, `entity_count`, `entity_sensor`, `drone_damage`, `fish_at`.

Entity actions lock a UUID so movement does not silently switch targets. Lost or invalid targets are reacquired only where the node allows it. Ordinary pursuit is bounded by time and distance. Owner, player, pet, boss, and permission checks remain authoritative. Combat requires Entity Scanner + Combat hardware and at least one valid weapon. `attack_entity` and `patrol_attack_area` support `STRONGEST`, `PRIMARY`, `SECONDARY`, and persistent `ALTERNATE` weapon-slot strategies plus a validated 4..40 tick attack interval; old graphs default to strongest/8 ticks. A specifically selected empty slot reports `NO_RESOURCE` rather than silently switching. Entity transport requires the containment module and only loads supported, authorized entities. Fishing requires the Fishing module and a real rod and uses vanilla fishing mechanics.

`patrol_attack_area` can prioritize nearest/lowest-health/highest-health/hostile targets, wait or finish when empty, clear one or all matches, reacquire vanished targets, limit chase distance/time, and return to a valid position in the patrol area.

### Values, variables, logic, state, and presentation

`number`, `boolean`, `string`, `number_math`, `compare_number`, `boolean_logic`, `boolean_not`, `compare_string`, `get_number_variable`, `set_number_variable`, `add_number_variable`, `get_string_variable`, `set_string_variable`, `last_action_status`, `last_action_error`, `compare_action_status`, `rename_drone`, `set_status_label`, `set_rotor_mode`, `set_status_light`, `display_string`, `remote_alert`.

Variables persist with the current runtime state. Invalid arithmetic such as division by zero fails rather than producing NaN/Infinity. Status and error nodes let graphs route expected failures. Display/status nodes affect observability only; they do not bypass permissions or alter control flow by themselves.

## 3. Standard Action Statuses

| Status | Meaning and usual response |
| --- | --- |
| `SUCCESS` | Action completed; continue normally. |
| `NOT_FOUND` | No matching resource/entity/target; wait, choose another target, or finish. |
| `NO_SPACE` | Drone or target is full; export, change cargo policy, or return. |
| `NO_RESOURCE` | Required item, fluid, weapon, rod, jar, or tool is absent. |
| `NO_ENERGY` | Not enough flight/action EU; return to a dock. |
| `OUT_OF_RANGE` | Wireless/entity chase/interaction bound exceeded. |
| `UNLOADED` | Required chunk/device is not loaded. |
| `DENIED` | Ownership, protection, or security policy rejected the action. |
| `UNREACHABLE` | No safe path or valid working position. |
| `INVALID_TARGET` | Wrong capability, block, entity, voltage, or target form. |
| `FAILURE` / `ERROR` | General runtime or extension failure; inspect `last_action_error` and timeline. |

Action nodes expose a failure flow where applicable. Use `last_action_status` + `compare_action_status` for explicit recovery instead of blindly looping.

## 4. Hardware Requirements

The same authoritative mapping is checked in the editor, before writing, and immediately before runtime evaluation/action:

- Tool Arm: block breaking/placing, tree felling, replanting, harvesting, block/entity interaction, item use, sign editing, and GT repair.
- Fluid Cargo: fluid transfer and fluid state nodes.
- EU Interface: EU import/export/target charging.
- Crafting: crafting grid and crafting actions.
- Advanced Navigation: advanced navigation/large-area capabilities where indicated by diagnostics.
- Entity Scanner: entity sensing and targeting.
- Combat + Entity Scanner: attack and patrol combat; a real weapon is also required.
- Entity Containment: load/release entity.
- Fishing: fishing; a real rod is also required.
- Alchemy Upgrade: Thaumcraft essentia transfer; a real essentia jar is also required.

Missing hardware does not delete or migrate an old program. It stops execution before EU consumption or world side effects and reports the missing stable module ID.

## 5. Recommended Graph Patterns

Harvest loop: `start → for_each_coordinate(area) → harvest_crop → cargo_used_percent → branch`. Send a nearly-full branch to `return_to_dock`; otherwise continue the loop and finish at End.

Loss-aware transfer: `start → import_items(target/filter) → compare_action_status(SUCCESS) → export_items(target)`. Route `NO_SPACE`, `UNLOADED`, and `NO_ENERGY` to distinct wait/return paths.

Machine service: sense maintenance and output blockage first, repair only when required, wait for a completed machine cycle, and return to the dock on energy or path failure.

Before production use, replace all template placeholder coordinates with world selection, run Diagnostics until there are no errors, verify module requirements against the target drone, and test expected failure branches in a protected area.
