# TACZ Extras

Отдельный Forge-мод для Minecraft 1.20.1, который переносит runtime-патчи из истории локального форка TACZ в конфигурируемые Mixins. Исходный мод `tacz` не включается в JAR и остаётся обязательной внешней зависимостью.

Разработчик: [TheFakeDevs](https://github.com/thefakedevs).

## Возможности

- звуки и частицы попадания пуль по блокам и воде, включая анимированные жёлто-оранжевые искры на металле;
- исторические профили громкости AWP, Glock 17, M1911, SKS Tactical и UMP45 без замены gun JSON;
- применение fire/silencer multiplier к громкости и настраиваемая дальность выстрела;
- уменьшенная громкость попадания по мишени;
- crawl без оружия, настраиваемые cooldown и допустимая высота падения;
- одинаковая обработка crawl с клавиатуры и контроллера;
- опциональная отмена ADS при начале перезарядки;
- повторная синхронизация ручного затвора и сохранение bolt-состояния при смене оружия;
- скрытие headshot debug AABB при `ReducedDebugInfo`;
- расширенный `tacz:bullet_ignore` tag для тонких и проходимых блоков.

## Конфигурация

После первого запуска Forge создаёт:

- `config/tacz_extras-common.toml` — эффекты попаданий, звуки, crawl, мишень и bolting;
- `config/tacz_extras-client.toml` — reload/aim и debug-hitbox.

Все Mixin-классы загружаются при старте, а большинство функций проверяет конфиг во время выполнения. Изменение datapack-тега `bullet_ignore` является ресурсным патчем и не имеет runtime-переключателя.

## Соответствие историческим патчам

| Коммиты | Реализация |
|---|---|
| `a2d742de`, `2710ac83` | `EntityKineticBulletMixin`, `BulletImpactEffects`, собственные sound events/resources |
| `e24cac35`, `90b6e46c` | `SoundPlayManagerMixin`, `SoundManagerMixin`, `GunSoundProfiles` |
| `0437c9e7`, merge crawl patches | `LocalPlayerCrawlMixin`, `LivingEntityCrawlMixin`, `CrawlKeyMixin` |
| reload/aim merge patch | `LocalPlayerAimMixin` |
| `875b5b70` | `TargetBlockEntityMixin` |
| `be4dd30b` | client/server bolt Mixins и дополнительное состояние `ShooterDataHolder` |
| `2c76c46f` | `RenderHeadShotAabbMixin` |

Коммиты, меняющие только Gradle, Maven URL, публикацию или имя собираемого JAR, намеренно не перенесены: они не являются игровыми runtime-патчами.

## Совместимость

- Minecraft `1.20.1`;
- Forge `47.x`;
- Java `17`;
- TACZ `1.1.x` с архитектурой ветки `1.20.1`.

Сборочная и статическая Mixin-совместимость подтверждена для последних пяти официальных релизов: `1.1.8-hotfix`, `1.1.8-release`, `1.1.7-hotfix2`, `1.1.7-hotfix` и `1.1.7-release`. Подробная матрица и методика проверки находятся в [COMPATIBILITY.md](COMPATIBILITY.md).

Сборка использует локальный `../TACZ/build/libs/tacz-1.20.1-1.1.6-svocraft-20d798dc.jar`. Если его нет, Gradle загружает `com.tacz:tacz-1.20.1:1.1.6-svocraft-e0d0c29f` из Svocraft Maven.

Mixins с `@Overwrite` специально ограничены небольшими TACZ gameplay-классами. При обновлении TACZ на другую основную версию проект нужно пересобрать и заново проверить Mixin audit.

## Сборка

```text
$env:JAVA_HOME = "C:\path\to\jdk-17"
.\gradlew.bat clean build
```

Готовый мод появляется в `build/libs/`.
