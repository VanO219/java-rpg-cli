# Руководство по проекту: Текстовая RPG на Java

Этот проект — консольная RPG-игра, написанная специально для изучения Java.
В 28 файлах собраны практически все конструкции языка из глав 2–6 учебника metanit.com.
Ты можешь запустить игру, поиграть, а потом разобрать код по кусочкам.

---

## Гайды по главам

Подробные руководства с маппингом «подглава учебника → код проекта» находятся в папке `GUIDES/`:

| Глава | Файл | Тема | Подглавы |
|-------|------|------|----------|
| 2 | [GUIDE_CHAPTER_2.md](GUIDES/GUIDE_CHAPTER_2.md) | Основы языка Java | 2.1–2.9 |
| 3 | [GUIDE_CHAPTER_3.md](GUIDES/GUIDE_CHAPTER_3.md) | Классы и ООП | 3.1–3.32 |
| 4 | [GUIDE_CHAPTER_4.md](GUIDES/GUIDE_CHAPTER_4.md) | Обработка исключений | 4.1–4.4 |
| 5 | [GUIDE_CHAPTER_5.md](GUIDES/GUIDE_CHAPTER_5.md) | Коллекции | 5.1–5.10 |
| 6 | [GUIDE_CHAPTER_6.md](GUIDES/GUIDE_CHAPTER_6.md) | Потоки ввода-вывода | 6.1–6.13 |
| 7 | [GUIDE_CHAPTER_7.md](GUIDES/GUIDE_CHAPTER_7.md) | Работа со строками | 7.1–7.4 |

---

## Точка входа

```
Main.main()
  └─> new Game()
       └─> game.start()
            ├─> [загрузка сохранения?]   // глава 6: File, DataInputStream, ObjectInputStream
            ├─> createHero()              // Scanner, switch expression, валидация
            ├─> setupInventory()          // Generics: Inventory<ItemInfo>
            ├─> setupLootTable()          // HashMap<EnemyRank, List<LootDrop>>
            ├─> loadEnemyData()           // try-with-resources: загрузка из файла
            ├─> gameLoop()                // List<Enemy>, бои, коллекции
            │    ├─> battle()             // PriorityQueue, assert, callback
            │    ├─> handleLoot()         // HashMap, HashSet достижений
            │    └─> betweenBattlesMenu() // Bestiary, leaderboard, save/load/export/zip
            │    └─> showStringDemo()        // TextFormatter, BattleNarrator, StringValidator
            └─> showFinalStats()          // record BattleStats, TreeSet рекордов
```

---

## Рекомендуемый порядок изучения

| Шаг | Файл | Что узнаешь |
|-----|------|-------------|
| 1 | `Main.java` | Точка входа, `public static void main` |
| 2 | `EnemyRank.java` | `enum` с полями и конструктором |
| 3 | `GameState.java` | `enum` с конечным автоматом |
| 4 | `Achievement.java` | `enum` с полями |
| 5 | `ItemType.java` | `enum` с абстрактным методом |
| 6 | `DamageType.java` | `sealed interface`, `record` |
| 7 | `Attackable.java` | Интерфейс, `default`-метод |
| 8 | `Healable.java` | Второй интерфейс |
| 9 | `BattleEventListener.java` | Callback-интерфейс |
| 10 | `GameCharacter.java` | `abstract class`, `protected`, `final`, wildcards |
| 11 | `Warrior.java` | `extends`, `super()`, наследование |
| 12 | `Mage.java` | `final` на поле, `super.toString()` |
| 13 | `Archer.java` | `double`, `Math.random()`, тернарный оператор |
| 14 | `Enemy.java` | `Cloneable`, `Comparable`, блоки инициализации |
| 15 | `BattleStats.java` | `record`, компактный конструктор |
| 16 | `BattleRecord.java` | `record` + `Comparable` + `Serializable` |
| 17 | `LootDrop.java` | `record` как значение в `HashMap` |
| 18 | `GameException.java` | Базовое пользовательское исключение |
| 19 | `InsufficientResourceException.java` | Checked exception с полями |
| 20 | `InvalidActionException.java` | Checked exception |
| 21 | `InventoryFullException.java` | Checked exception |
| 22 | `Inventory.java` | Generics, inner class, `Iterable`, `Iterator`, `Serializable` |
| 23 | `Bestiary.java` | `TreeMap`, `NavigableMap`, `Iterable`, `Serializable` |
| 24 | `GameSaveManager.java` | Потоки I/O, `DataOutputStream`, `ObjectOutputStream`, `File` |
| 25 | `BattleLogExporter.java` | `ByteArrayOutputStream`, `PrintWriter`, `BufferedWriter` |
| 26 | `SaveArchiver.java` | `ZipOutputStream`, `ZipInputStream` |
| 27 | `ConsoleDemo.java` | `Console`, `readPassword()` |
| 28 | `Game.java` | Всё вместе: коллекции, исключения, I/O, `Serializable`, `transient` |
| 29 | `TextFormatter.java` | `StringBuilder` полностью, `length`, `toCharArray`, `repeat`, `substring` |
| 30 | `CommandParser.java` | Все операции со строками: `split`, `strip`, `equals`, `indexOf`, `join` |
| 31 | `BattleNarrator.java` | `StringBuilder`: `append`, `insert`, `delete`, `replace`, `reverse`; `StringBuffer` |
| 32 | `StringValidator.java` | `Pattern`, `Matcher`: `compile`, `matches`, `find`, `group`, `start`, `end` |

> Совет: открой файл и читай комментарии сверху вниз. Каждый комментарий объясняет конструкцию Java, которая идёт сразу после него.

---

## Структура проекта

```
src/rpg/
  Main.java                          — точка входа
  Game.java                          — игровой цикл, бой, меню, save/load (~3000 строк)
  GameCharacter.java                 — абстрактный базовый класс героя
  Warrior.java / Mage.java / Archer.java — подклассы героя
  Enemy.java                         — враг (Cloneable, Comparable)
  Attackable.java / Healable.java    — интерфейсы
  DamageType.java                    — sealed interface + record
  BattleStats.java / BattleRecord.java / LootDrop.java — record
  Inventory.java                     — generics, inner class, Iterable
  Bestiary.java                      — TreeMap wrapper, NavigableMap
  GameState.java / ItemType.java / EnemyRank.java / Achievement.java — enum
  BattleEventListener.java           — callback interface
  GameException.java + 3 подкласса   — пользовательские исключения
  GameSaveManager.java               — save/load (DataStreams + Serializable)
  BattleLogExporter.java             — экспорт журнала боёв
  SaveArchiver.java                  — ZIP-архивация сохранений
  ConsoleDemo.java                   — демо Console API
  TextFormatter.java                 — форматирование текста (String + StringBuilder)
  CommandParser.java                 — парсинг текстовых команд (операции со строками)
  BattleNarrator.java                — генерация боевых описаний (StringBuilder + StringBuffer)
  StringValidator.java               — валидация с regex (Pattern, Matcher)
```
