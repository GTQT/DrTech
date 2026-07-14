# Repository Guidelines

## Project Structure & Module Organization
`src/main/java` contains the mod sources, mainly under `com.drppp.drtech`, plus bundled modules such as `com.meowmel.cropQT` and `com.brachy84.mechtech`. Keep gameplay logic in `common`, client-only code in `Client`, registrations/loaders in `loaders`, and integrations in `intergations`. `src/main/resources` holds Forge metadata, mixin JSON, and assets under `assets/drtech`, `assets/gregtech`, and related namespaces. `src/api/java` contains third-party API stubs; avoid editing it unless you are updating vendored interfaces. Generated output belongs in `build/`, and local runtime data belongs in `run/`.

## Build, Test, and Development Commands
Use the Gradle wrapper from the repo root:

- `.\gradlew.bat setupDecompWorkspace` prepares the 1.12.2 Forge workspace.
- `.\gradlew.bat runClient` launches the dev client using the configured `developmentEnvironmentUserName`.
- `.\gradlew.bat build` compiles, processes resources, and produces the jar.
- `.\gradlew.bat test` runs the JUnit 5 test task.
- `.\gradlew.bat clean` removes generated output.
- `.\gradlew.bat updateBuildScript` refreshes the shared GTCEu buildscript.

Add dependencies in `dependencies.gradle` and extra repositories in `repositories.gradle`, not in `build.gradle`.

## Coding Style & Naming Conventions
Java sources use UTF-8 and 4-space indentation. Follow the package layout already in use and keep class names descriptive and feature-based, for example `MetaTileEntityIndustrialMixer` or `LootTableJeiPlugin`. Use `UpperCamelCase` for classes, `lowerCamelCase` for methods and fields, and `SCREAMING_SNAKE_CASE` for constants. Spotless support exists but is disabled by default; enable `enableSpotless=true` in `gradle.properties` before running `.\gradlew.bat spotlessApply`.

## Testing Guidelines
JUnit 5 is enabled in Gradle, but the repository currently has no `src/test/java` tree. Add new tests under `src/test/java` with names ending in `Test`. Prefer focused unit tests for recipe helpers, registries, and utility code, then run `.\gradlew.bat test` before opening a PR.

## Commit & Pull Request Guidelines
Recent history follows Conventional Commits with a scope, often in Chinese, for example `feat(lootgames): ...` or `refactor(crop): ...`. Keep commits small and feature-focused. PRs should summarize gameplay or API impact, list changed systems, link the related issue, and include screenshots when touching models, textures, GUIs, or world generation behavior.
