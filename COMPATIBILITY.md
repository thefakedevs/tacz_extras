# Совместимость с TaCZ

Проверено 16 июля 2026 года на последних пяти официальных Forge-релизах TaCZ для Minecraft 1.20.1.

Матрица повторно пройдена для TaCZ Extras `1.1.0` после добавления клиентской частицы `tacz_extras:metal_spark`.

| TaCZ | ForgeGradle + Mixin AP | Mixin-классы | Обязательные цели | Звуковой call site | Результат |
|---|---:|---:|---:|---|---|
| `1.1.8-hotfix` | успешно | 13/13 | 46/46 | tracked `playClientSound` | совместимо |
| `1.1.8-release` | успешно | 13/13 | 46/46 | tracked `playClientSound` | совместимо |
| `1.1.7-hotfix2` | успешно | 13/13 | 46/46 | legacy `playClientSound` | совместимо |
| `1.1.7-hotfix` | успешно | 13/13 | 46/46 | legacy `playClientSound` | совместимо |
| `1.1.7-release` | успешно | 13/13 | 46/46 | legacy `playClientSound` | совместимо |

## Что проверялось

1. Официальные JAR загружались из проекта TaCZ на Modrinth и сверялись с опубликованными SHA-512.
2. Для каждого JAR выполнялась чистая компиляция через ForgeGradle с Mixin annotation processor.
3. В production-байткоде проверялись все 13 целевых классов и 46 обязательных методов/полей.
4. Для `LivingEntityDrawGun.draw()` проверялся вызов поддерживаемого `ShooterDataHolder.initialData()`.
5. Для `SoundPlayManager` отдельно проверялись оба варианта call site: пятиаргументный в `1.1.7` и новый tracked-вариант в `1.1.8`.
6. Изменения целевых классов между `1.1.7` и `1.1.8`, а также между `1.1.8-release` и `1.1.8-hotfix`, сверялись с исходниками официального репозитория.

`SoundPlayManagerMixin` использует точечные `@ModifyArg` для обеих сигнатур `playClientSound`. Благодаря этому TaCZ Extras меняет только громкость и дистанцию, не отключая добавленные в TaCZ 1.1.8 ограничения одновременных звуков и исправления позиционирования из `1.1.8-hotfix`. В версии TaCZ Extras `1.0.1` удалён `@ModifyArgs`, создававший недоступный Forge synthetic-класс `org.spongepowered.asm.synthetic.args.Args$1` при ранней загрузке `SoundPlayManager`.

## Ограничения проверки

Матрица подтверждает сборочную и статическую Mixin-совместимость чистых TaCZ JAR. Полный игровой smoke test с запуском клиента и проверка конфликтов с произвольными сторонними модами в эту проверку не входили.

Официальные источники: [GitHub releases](https://github.com/MCModderAnchor/TACZ/releases), [Modrinth versions](https://modrinth.com/mod/timeless-and-classics-zero/versions?g=1.20.1&l=forge), [CurseForge files](https://www.curseforge.com/minecraft/mc-mods/timeless-and-classics-zero/files/all?page=1&pageSize=20&version=1.20.1).
