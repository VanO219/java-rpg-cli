# Руководство по проекту: Текстовая RPG на Java

Этот проект — консольная RPG-игра, написанная специально для изучения Java.
В 24 файлах собраны практически все конструкции языка из глав 2–5 учебника metanit.com.
Ты можешь запустить игру, поиграть, а потом разобрать код по кусочкам.

---

## 1. С чего начать?

### Точка входа

Любая Java-программа начинается с метода `main`. Вот цепочка вызовов:

```
Main.main()
  └─> new Game()                    // создаёт объект игры, инициализирует коллекции, исключения
       └─> game.start()
            ├─> createHero()        // Scanner + switch expression → выбор класса
            ├─> setupInventory()    // Generics: Inventory<ItemInfo>, InventoryFullException
            ├─> setupLootTable()    // HashMap<EnemyRank, List<LootDrop>>
            ├─> loadEnemyData()     // try-with-resources: загрузка из файла
            ├─> gameLoop()          // List<Enemy> + while + for → бои с врагами
            │    ├─> battle()       // PriorityQueue (инициатива), assert, callback
            │    ├─> handleLoot()   // HashMap лут, HashSet достижений
            │    ├─> checkAchievements() // Set<Achievement>
            │    └─> betweenBattlesMenu() // Bestiary (TreeMap), leaderboard (TreeSet)
            └─> showFinalStats()    // record BattleStats, приведение типов, TreeSet рекорды
```

### Рекомендуемый порядок изучения

Читай файлы в таком порядке — от самых простых конструкций к сложным:

| Шаг | Файл                         | Что узнаешь                                                                          |
|-----|------------------------------|--------------------------------------------------------------------------------------|
| 1   | `Main.java`                  | Точка входа, `public static void main`, `new`, вызов метода                          |
| 2   | `EnemyRank.java`             | `enum` с полями и конструктором, метод `applyStat()`                                 |
| 3   | `GameState.java`             | `enum` с конечным автоматом, метод `canTransitionTo()`                               |
| 4   | `Achievement.java`           | `enum` с полями, использование в `HashSet`                                           |
| 5   | `ItemType.java`              | `enum` с абстрактным методом на каждой константе                                     |
| 6   | `DamageType.java`            | `sealed interface`, `record`, вложенные типы, `permits`                              |
| 7   | `Attackable.java`            | Интерфейс, `default`-метод, `static`-метод в интерфейсе                              |
| 8   | `Healable.java`              | Второй интерфейс (закрепление), `void`, `boolean`                                   |
| 9   | `BattleEventListener.java`   | Callback-интерфейс, паттерн «Наблюдатель»                                           |
| 10  | `GameCharacter.java`         | `abstract class`, `implements`, `protected`, `final`-методы, bounded generics       |
| 11  | `Warrior.java`               | `extends`, `super()`, наследование, переопределение методов                          |
| 12  | `Mage.java`                  | `final` на поле, `super.toString()`, `if-else`                                      |
| 13  | `Archer.java`                | `double`, `Math.random()`, тернарный оператор, приведение типов                      |
| 14  | `Enemy.java`                 | `Cloneable`, `clone()`, `Comparable`, `compareTo()`, блоки инициализации             |
| 15  | `BattleStats.java`           | `record` на практике, компактный конструктор, валидация                              |
| 16  | `BattleRecord.java`          | `record` + `Comparable`, используется в `TreeSet`                                   |
| 17  | `LootDrop.java`              | `record` как значение в `HashMap`                                                   |
| 18  | `GameException.java`         | Базовое пользовательское исключение, иерархия исключений                             |
| 19  | `InsufficientResourceException.java` | Checked exception с дополнительными полями                                  |
| 20  | `InvalidActionException.java`       | Checked exception для неверных действий                                      |
| 21  | `InventoryFullException.java`       | Checked exception для переполнения инвентаря                                 |
| 22  | `Inventory.java`             | Generics `<T>`, inner class, static nested class, `Iterable`, `Iterator`            |
| 23  | `Bestiary.java`              | `TreeMap`, `NavigableMap`, `Iterable`, паттерн обёртки                             |
| 24  | `Game.java`                  | Всё вместе: все коллекции, исключения, callbacks, циклы, switch, побитовые операции |

> Совет: открой файл и читай комментарии сверху вниз. Каждый комментарий объясняет конструкцию Java, которая идёт сразу после него.

---

## 2. Карта проекта

### Диаграмма связей между классами

```
                        ┌─────────────┐
                        │  Main.java  │
                        │  (запуск)   │
                        └──────┬──────┘
                               │ создаёт
                               ▼
    ┌──────────────────────────────────────────────────────────────┐
    │                        Game.java                             │
    │  (управляет всем: герой, враги, бои, коллекции, исключения)  │
    │                                                              │
    │  Коллекции:                        Исключения:               │
    │    List<String> battleLog            try-catch-finally        │
    │    LinkedList<String> questLog       multi-catch              │
    │    Map<EnemyRank,List<LootDrop>>     try-with-resources       │
    │    Set<Achievement> achievements     throws / throw           │
    │    TreeSet<BattleRecord> leaderboard assert                  │
    │    ArrayDeque<String> undoStack                              │
    │    Bestiary (обёртка над TreeMap)                            │
    └───┬────────┬────────┬────────┬────────┬─────────────────────┘
        │        │        │        │        │
        ▼        ▼        ▼        ▼        ▼
  ┌─────────┐ ┌──────┐ ┌──────┐ ┌───────┐ ┌──────────────┐
  │  Enemy  │ │Battle│ │Battle│ │Loot   │ │Inventory<T>  │
  │Cloneable│ │Stats │ │Record│ │Drop   │ │  (generics)  │
  │Comparable│ │record│ │record│ │record │ │              │
  └─────────┘ └──────┘ └──────┘ └───────┘ │ ┌──────────┐ │
                                           │ │  Slot    │ │ inner class
                                           │ └──────────┘ │
                                           │ ┌──────────┐ │
                                           │ │ ItemInfo │ │ static nested
                                           │ └──────────┘ │
                                           └──────────────┘
```

### Иерархия наследования

```
    ┌───────────────────────┐     ┌──────────────────────────────┐
    │ Attackable            │     │ Healable                     │
    │                       │     │                              │
    │ + performAttack()     │     │ + heal(amount)               │
    │ + getBattleCry()    D │     │ + needsHealing()           D │
    │ + calculateCrit()   S │     │ + baseHealAmount(level)    S │
    └───────────┬───────────┘     └──────────────┬───────────────┘
                │   implements                   │   implements
                └──────────┬─────────────────────┘
                           ▼
              ┌────────────────────────┐
              │ <<abstract>>           │
              │   GameCharacter        │
              │                        │
              │ - name, health, attack │
              │ - defense, level, exp  │
              │ # level, experience    │   # = protected
              │                        │
              │ + performAttack()      │   3 перегрузки!
              │ + heal(), takeDamage() │   final — нельзя переопределить
              │ + toString()           │
              │ + equals(), hashCode() │
              │                        │
              │ ~ getClassName()       │   ~ = abstract
              │ ~ specialAttack()      │
              │                        │
              │ + findStrongest(T[])   │   bounded generic: <T extends GameCharacter>
              └─────────┬──────────────┘
                        │  extends
           ┌────────────┼────────────┐
           ▼            ▼            ▼
    ┌────────────┐ ┌──────────┐ ┌──────────┐
    │  Warrior   │ │   Mage   │ │  Archer  │
    │            │ │          │ │          │
    │ - rage     │ │ - mana   │ │ - crit   │
    │            │ │ - maxMana│ │   Chance │
    └────────────┘ └──────────┘ └──────────┘
```

### sealed interface + record

```
    ┌───────────────────────────────┐
    │ DamageType                    │
    │                               │
    │ + totalDamage(): int          │
    │                               │
    │ permits: Physical,            │
    │          Magical, Mixed       │
    ├───────────────────────────────┤
    │ <<record>> Physical(amount)   │──── Warrior использует
    │ <<record>> Magical(amount,    │──── Mage использует
    │                    element)   │
    │ <<record>> Mixed(physical,    │──── Archer использует
    │                  magical)     │
    └───────────────────────────────┘
```

### Иерархия исключений

```
    java.lang.Throwable
     └─ java.lang.Exception          ← checked: компилятор требует обработки
         └─ GameException            ← наш базовый checked exception
             ├─ InsufficientResourceException   ← недостаточно маны/ярости
             ├─ InvalidActionException          ← недопустимое действие
             └─ InventoryFullException          ← инвентарь заполнен

    java.lang.RuntimeException       ← unchecked: компилятор НЕ требует обработки
     ├─ NumberFormatException        ← строка не является числом
     ├─ IndexOutOfBoundsException    ← индекс за пределами массива/списка
     ├─ IllegalStateException        ← объект в неверном состоянии
     └─ NoSuchElementException       ← Scanner.nextLine() без ввода

    java.lang.AssertionError         ← assert: нарушение инварианта программы
```

### Коллекции в проекте

```
    ┌──────────────────────────────────────────────────────────────┐
    │ Коллекция                │ Файл          │ Зачем             │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ List<String>             │ Game.java     │ Лог боя (ArrayList│
    │ (battleLog)              │ строка 235    │ — быстрый доступ) │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ LinkedList<String>       │ Game.java     │ Журнал квестов    │
    │ (questLog)               │ строка 250    │ addFirst() O(1)   │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ Map<EnemyRank,List<...>> │ Game.java     │ Таблица лута      │
    │ (lootTable — HashMap)    │ строка 263    │ доступ по ключу   │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ Set<Achievement>         │ Game.java     │ Достижения        │
    │ (achievements — HashSet) │ строка 274    │ без дубликатов    │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ TreeSet<BattleRecord>    │ Game.java     │ Таблица рекордов  │
    │ (leaderboard)            │ строка 285    │ сортировка auto   │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ ArrayDeque<String>       │ Game.java     │ Стек отмены (LIFO)│
    │ (undoStack)              │ строка 304    │ push/pop          │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ PriorityQueue<Combatant> │ Game.java     │ Инициатива в бою  │
    │ (initiativeQueue)        │ строка 1444   │ по скорости       │
    ├──────────────────────────┼───────────────┼───────────────────┤
    │ TreeMap<String, Entry>   │ Bestiary.java │ Бестиарий врагов  │
    │ (entries)                │ строка 177    │ алфавитный порядок│
    └──────────────────────────┴───────────────┴───────────────────┘
```

### Enum-файлы

```
    GameState.java      — состояние игры (MENU → EXPLORING → BATTLE → GAME_OVER)
                          enum + canTransitionTo() — конечный автомат
    EnemyRank.java      — ранг врага (COMMON, ELITE, BOSS)
                          enum + multiplier + applyStat() — множитель характеристик
    Achievement.java    — достижения игрока (8 штук)
                          enum с description + icon, хранится в HashSet
    ItemType.java       — типы предметов (HEALTH_POTION, MANA_POTION, BOMB)
                          enum с abstract-методом на каждой константе
```

D = default-метод, S = static-метод

---

## 3. Какие конструкции Java где искать

### Глава 2: Основы языка

| Конструкция                                     | Файл               | Строка                | Зачем в проекте                              |
|-------------------------------------------------|--------------------|-----------------------|----------------------------------------------|
| `public static void main`                       | Main.java          | 53                    | Точка входа в программу                      |
| Типы `int`, `byte`, `double`, `boolean`, `char` | Game.java          | 188, 220–224          | Хранят характеристики, флаги, шанс крита     |
| `String`                                        | Enemy.java         | 172                   | Имя врага, текстовые данные                  |
| Арифметика `+`, `-`, `*`, `/`, `%`              | GameCharacter.java | 363, 435              | Расчёт урона, опыта                          |
| `++`, `--` (инкремент)                          | Game.java          | 392, 1000             | Счётчики, индексы                            |
| `+=`, `-=`, `*=`, `/=`                          | GameCharacter.java, Game.java | 412, 453, 2473 | Накопление опыта, урона                 |
| Сравнение `==`, `!=`, `<`, `>=`                 | Game.java          | 979, 989              | Проверка условий в боях                      |
| `&&`, `\|\|`, `!`                               | Game.java          | 870, 917              | Комбинированные условия                      |
| Побитовые `&`, `\|`, `^`, `~`, `<<`             | Game.java          | 195–198, 1324–1326    | Флаги статусных эффектов                     |
| Тернарный `? :`                                 | Archer.java        | 112                   | Краткие условные выражения                   |
| Приведение типов `(int)`, `(double)`, `(long)`  | Game.java          | 2438–2440             | Конвертация чисел в статистике               |
| `if / else if / else`                           | GameCharacter.java | 410–443               | Проверка уровня, условий повышения           |
| `switch`-выражение (стрелочный `->`)            | Game.java          | 595–601               | Выбор класса героя                           |
| `switch` с блоками `{ }`                        | Game.java          | 1041                  | Меню между боями: несколько действий в case  |
| `while` (цикл с предусловием)                   | Game.java          | 547, 856              | Главный игровой цикл, ввод имени             |
| `do-while` (цикл с постусловием)                | Game.java          | 560–580               | Меню выбора класса                           |
| `for` (цикл со счётчиком)                       | Game.java          | 1389, 2339            | Раунды боя, перебор битов статуса            |
| `for-each` (расширенный for)                    | Game.java          | 814, 836, 967, 2056   | Перебор врагов и инвентаря                   |
| `break`                                         | Game.java          | 983, 996              | Выход из цикла при гибели                    |
| `continue`                                      | Game.java          | 1750                  | Пропустить текущую итерацию (отменённое действие) |
| `return`                                        | Game.java          | 2034, 2645            | Возврат из метода                            |
| `Scanner`, `nextLine()`                         | Game.java          | 370, 2637             | Чтение ввода пользователя                    |
| `Integer.parseInt()`                            | Game.java          | 2641                  | Парсинг числа из строки                      |
| Методы (объявление и вызов)                     | Game.java          | повсюду               | Декомпозиция кода на части                   |
| Перегрузка методов (overloading)                | GameCharacter.java | 262, 295, 306         | 3 варианта `performAttack`                   |
| `varargs` (`String... parts`)                   | Game.java          | 2368                  | Логирование с произвольным числом аргументов |
| `StringBuilder`                                 | Game.java          | ~2375                 | Эффективная конкатенация строк               |
| `String.matches(regex)`                         | Game.java          | 644                   | Проверка имени героя регулярным выражением   |
| `String.trim()`                                 | Game.java          | 626, 2641             | Удаление пробелов по краям строки            |
| `String.valueOf()`                              | Game.java          | 1862                  | Преобразование числа в строку                |
| `String.join()`                                 | Game.java          | 2384                  | Соединение строк с разделителем              |
| `Math.min`, `Math.max`, `Math.random`           | GameCharacter.java | ~330, ~360            | Ограничение здоровья, случайные числа        |

### Глава 3: Классы и ООП

| Конструкция                                      | Файл                   | Строка             | Зачем в проекте                                          |
|--------------------------------------------------|------------------------|--------------------|----------------------------------------------------------|
| `class` (обычный класс)                          | Enemy.java             | 88                 | Описание врага                                           |
| `abstract class`                                 | GameCharacter.java     | 46                 | Общая основа для героев                                  |
| `interface`                                      | Attackable.java        | 43                 | Контракт «умеет атаковать»                               |
| `sealed interface`                               | DamageType.java        | 47                 | Фиксированный набор типов урона                          |
| `record`                                         | BattleStats.java       | 57                 | Неизменяемая статистика боя                              |
| `record` внутри `sealed`                         | DamageType.java        | 74, 91, 103        | Physical, Magical, Mixed                                 |
| `extends` (наследование)                         | Warrior.java           | 38                 | Воин наследует от GameCharacter                          |
| `implements` (реализация)                        | GameCharacter.java     | 46                 | Реализация Attackable + Healable                         |
| `abstract` метод                                 | GameCharacter.java     | 221, 238           | getClassName(), specialAttack()                          |
| `@Override`                                      | Warrior.java           | 88, 103            | Переопределение методов                                  |
| `default`-метод интерфейса                       | Attackable.java        | 71                 | getBattleCry() по умолчанию                              |
| `static`-метод интерфейса                        | Attackable.java        | 89                 | calculateCritDamage()                                    |
| `super()` в конструкторе                         | Warrior.java           | 71                 | Вызов конструктора родителя                              |
| `super.toString()`                               | Mage.java              | 137                | Расширение метода родителя                               |
| `this`                                           | Enemy.java             | 264–280            | Разрешение конфликта имён                                |
| `private`, `protected`, `public`                 | GameCharacter.java     | 62–89              | Контроль доступа к полям                                 |
| `static` поле                                    | GameCharacter.java     | 101                | Счётчик всех персонажей                                  |
| `static` метод                                   | GameCharacter.java     | 519                | getCharacterCount()                                      |
| `static final` (константа)                       | Game.java              | 133, 139           | MAX_ENEMIES, GAME_VERSION                                |
| `final` на поле                                  | Mage.java              | 35                 | maxMana — неизменяемая ёмкость маны                      |
| `final` метод                                    | GameCharacter.java     | 360, 393           | takeDamage(), isAlive() — нельзя переопределить          |
| Конструктор                                      | Enemy.java             | 264, 282           | Создание объекта с параметрами                           |
| Компактный конструктор record                    | BattleStats.java       | 95                 | Валидация данных                                         |
| Геттеры                                          | Enemy.java             | 386–406            | Доступ к private-полям                                   |
| `toString()`                                     | Enemy.java             | 746                | Текстовое представление                                  |
| `equals()` + `hashCode()`                        | GameCharacter.java     | 608, 657           | Сравнение персонажей                                     |
| `instanceof` + pattern matching                  | Game.java              | 1914, 1973         | Определение типа героя (Mage, Warrior)                   |
| Полиморфизм                                      | Game.java              | 595–601            | hero — GameCharacter, реально Warrior/Mage/Archer        |
| Приведение типов (casting)                       | Inventory.java         | 331                | `@SuppressWarnings("unchecked")`, `(Slot) slots[index]`  |
| Generics `<T>`                                   | Inventory.java         | 61                 | Типобезопасный инвентарь                                 |
| Inner class (нестатический)                      | Inventory.java         | 85                 | Slot — привязан к Inventory                              |
| Static nested class                              | Inventory.java         | 177                | ItemInfo — независимый                                   |
| `Objects.equals()`, `Objects.hash()`             | GameCharacter.java     | ~630, ~658         | Null-безопасное сравнение                                |
| `@SuppressWarnings`                              | Inventory.java         | 331, 584           | Подавление предупреждения о типах                        |
| `throw` + исключения                             | Inventory.java         | 289                | Обработка ошибки переполнения                            |
| Ссылочные типы + `clone()`                       | Enemy.java             | 484–525            | Cloneable, глубокое vs поверхностное копирование         |
| Блок инициализации экземпляра                    | Enemy.java             | 158                | Выполняется перед конструктором                          |
| Статический блок инициализации                   | Enemy.java, Game.java  | 124, 355           | Выполняется при загрузке класса                          |
| Цепочка конструкторов (`this(...)`)              | Enemy.java             | 264, 282           | Делегирование между конструкторами                       |
| Callback-интерфейс                               | BattleEventListener.java | 87               | Паттерн «Наблюдатель», onAttack/onLevelUp                |
| Bounded generics `<T extends ...>`               | GameCharacter.java     | 725                | findStrongest(T[]): T extends GameCharacter              |
| Wildcards `?`, `? extends T`                     | GameCharacter.java, Inventory.java | 690, 468 | Гибкие параметры generic-методов                       |
| `enum` с полями и методами                       | EnemyRank.java         | 34–110             | COMMON/ELITE/BOSS + multiplier + applyStat()             |
| `enum` как конечный автомат                      | GameState.java         | 64–180             | MENU → EXPLORING → BATTLE → GAME_OVER                   |
| `enum` с abstract-методом                        | ItemType.java          | 35, 129            | Каждая константа реализует свой метод                    |
| `sealed` + `permits`                             | DamageType.java        | 47                 | Закрытая иерархия типов урона                            |
| Pattern matching в `switch`                      | Game.java              | 1830               | switch по типу DamageType                                |
| Record patterns                                  | Game.java              | 1831               | Деструктуризация record в switch                         |
| Type erasure                                     | Inventory.java         | 331                | Почему нужен @SuppressWarnings("unchecked")              |
| Инкапсуляция                                     | GameCharacter.java     | 62–89              | private поля + public геттеры                            |
| Анонимный класс                                  | Game.java              | 431                | new BattleEventListener() { ... } без имени класса       |
| Локальный `record` (local record)                | Game.java              | 1427               | record Combatant — объявлен внутри метода                |
| Вложенный `record` (nested record)               | Bestiary.java          | 108                | record BestiaryEntry — объявлен внутри класса Bestiary   |
| Статический фабричный метод                      | Enemy.java, ItemType.java | 527, 165        | Enemy.createVariant(), ItemType.fromDisplayName()        |
| `getClass()` + type erasure на практике          | Game.java              | 489                | inventory.getClass().getName() — тип без параметра       |
| `@FunctionalInterface` (концепция)               | BattleEventListener.java | 57               | Почему этот интерфейс НЕ является функциональным         |
| Javadoc (`@param`, `@return`, `@throws`)         | Enemy.java, GameCharacter.java | повсюду    | Документирование методов — стандарт Java                 |
| `enum.values()`, `enum.name()`                   | Game.java              | 1231, 1262         | Achievement.values() — перебор всех констант enum        |
| Паттерн «with» для immutable record              | Bestiary.java          | 149                | withKill() — «изменить» record через создание нового     |
| Паттерн Template Method                          | Game.java              | 456                | start() — высокоуровневый алгоритм из шагов              |

### Глава 4: Исключения

| Конструкция                                   | Файл                          | Строка     | Зачем в проекте                                              |
|-----------------------------------------------|-------------------------------|------------|--------------------------------------------------------------|
| `try-catch`                                   | Game.java                     | 548–556    | Обработка InvalidActionException при вводе имени            |
| `try-catch-finally`                           | Game.java                     | 2636–2670  | readInt(): multi-catch + finally                             |
| `finally`-блок                               | Game.java                     | 2646–2669  | Гарантированное выполнение кода                              |
| Multi-catch `catch (A \| B e)`               | Game.java                     | 2642       | NumberFormatException \| NoSuchElementException              |
| Multi-catch в handleUseItem                  | Game.java                     | 2087–2149  | IndexOutOfBoundsException \| IllegalStateException          |
| `throws` в сигнатуре                         | Game.java                     | 624        | readHeroName() throws InvalidActionException                 |
| `throw` (бросок исключения)                  | Inventory.java                | 289        | throw new InventoryFullException(...)                        |
| `try-with-resources`                         | Game.java                     | 754–768    | loadEnemyData(): Scanner(File) закрывается автоматически     |
| Checked exception                            | GameException.java            | 99         | Наследник Exception — компилятор требует обработки           |
| Unchecked exception                          | Game.java                     | 2087–2090  | IndexOutOfBoundsException — наследник RuntimeException       |
| Пользовательское исключение с полями         | InsufficientResourceException.java | 47    | resourceName, required, available + геттеры                  |
| Иерархия исключений                          | GameException.java, его наследники | 99     | GameException → 3 конкретных исключения                     |
| `assert`                                     | Game.java                     | 1356       | Проверка перехода GameState, нужен флаг `-ea`                |
| Обработка `FileNotFoundException`            | Game.java                     | 765–770    | Файл не найден — штатная ситуация, не краш                   |
| Перехват `InventoryFullException`            | Game.java                     | 663–668, 1097–1104 | Инвентарь полон при добавлении предмета              |

### Глава 5: Коллекции

| Конструкция                              | Файл              | Строка        | Зачем в проекте                                              |
|------------------------------------------|-------------------|---------------|--------------------------------------------------------------|
| Иерархия Collection (List, Set, Queue)   | Game.java         | 17–23         | Схема в импортах: Collection → List, Set, Queue              |
| `ArrayList<T>` — динамический массив     | Game.java         | 403, 795      | battleLog, список врагов с заданной ёмкостью                 |
| `List<Enemy>` как тип переменной         | Game.java         | 795           | Полиморфизм коллекций: тип — интерфейс, реализация — ArrayList |
| `list.add()`, `list.get()`, `list.size()`| Game.java         | 801–805       | Добавление врагов, обращение по индексу                      |
| `for-each` по `List`                     | Game.java         | 967           | Перебор battleLog после боя                                  |
| `LinkedList<String>`                     | Game.java         | 404, 950–954  | questLog: addFirst() O(1) — преимущество над ArrayList       |
| `LinkedList.addFirst()`, `addLast()`     | Game.java         | 950–954       | Вставка в начало/конец за O(1)                               |
| `HashSet<Achievement>`                   | Game.java         | 406, 265–274  | Достижения: уникальность гарантирована Set                   |
| `Set.add()`, `Set.contains()`            | Game.java         | 1008, ~1145   | Добавление достижения, проверка наличия                      |
| `TreeSet<BattleRecord>`                  | Game.java         | 407, 276–285  | Таблица рекордов: автосортировка по Comparable               |
| `TreeSet.first()`, `.last()`             | Game.java         | ~2548, ~2560  | Первый (лучший) и последний рекорд                           |
| `SortedSet` / `NavigableSet`             | Game.java         | ~2547         | TreeSet является NavigableSet                                |
| `Comparable<T>` + `compareTo()`          | Enemy.java        | 608–660       | Сортировка по мощи (health + attack)                         |
| `Comparable` в record                    | BattleRecord.java | 81–150        | compareTo() по score (убывание) для TreeSet                  |
| `Comparator<T>` (кастомный порядок)      | Game.java         | 831, 845      | byName (алфавит), byExpDesc (опыт убывание)                  |
| `Comparator.comparing(method::ref)`      | Game.java         | 831           | Лямбда-стиль: Enemy::getName                                 |
| `Comparator.reversed()`                  | Game.java         | 845           | Инвертировать порядок сортировки                             |
| `Collections.sort(list)`                 | Game.java         | 812           | Сортировка по Comparable (compareTo)                         |
| `PriorityQueue<T>`                       | Game.java         | 1444          | Инициатива в бою: первым ходит самый быстрый                 |
| `PriorityQueue.offer()`, `.poll()`       | Game.java         | 1450–1461     | offer — добавить, poll — извлечь с приоритетом               |
| `ArrayDeque<String>` как стек            | Game.java         | 408, 287–304  | Стек отмены действий (LIFO): push/pop                        |
| `ArrayDeque.push()`, `.pop()`, `.peek()` | Game.java         | 1701, 1689    | Сохранить/снять действие с вершины стека                     |
| `HashMap<K, V>`                          | Game.java         | 405, 252–263  | Таблица лута: ключ EnemyRank → список LootDrop               |
| `Map.put()`, `Map.get()`                 | Game.java         | ~700, ~1080   | Заполнение и получение лута                                  |
| `TreeMap<K, V>` — отсортированная карта  | Bestiary.java     | 177           | Бестиарий: ключи (имена врагов) в алфавитном порядке         |
| `NavigableMap` + `headMap()`, `tailMap()`| Bestiary.java     | 265–300       | Навигация по диапазону ключей                                |
| `Map.entrySet()`, `Map.Entry<K,V>`       | Bestiary.java     | 244, 370      | Обход пар ключ-значение                                      |
| `Iterable<T>` + `iterator()`             | Inventory.java, Bestiary.java | 61, 89 | Реализация интерфейса для for-each                      |
| `Iterator<T>` + `hasNext()`, `next()`    | Inventory.java    | 537–640       | Ручной обход коллекции                                       |
| `Iterator.remove()`                      | Inventory.java    | 636           | Удаление элемента во время обхода                            |
| for-each через `Iterable`                | Game.java         | 2037–2056     | handleUseItem(): for (var slot : inventory)                  |
| `diamond operator <>`                    | Game.java         | 403–409       | new ArrayList<>() — компилятор выводит тип                   |
| `List.of()` — неизменяемый список        | Game.java         | 692, 698, 705 | Таблица лута: List.of(LootDrop...) — нельзя изменить         |
| `Map.getOrDefault()`                     | Game.java         | 1073          | lootTable.getOrDefault(rank, List.of()) — безопасный доступ  |
| `Map.containsKey()`                      | Bestiary.java     | 199, 328      | Проверить наличие врага в бестиарии перед добавлением        |
| `System.arraycopy()`                     | Inventory.java    | 413           | Нативное смещение массива при удалении слота                 |

---

## 4. Как это всё работает вместе (поток игры)

### Шаг 1: Запуск (Main.java)

```java
Game game = new Game();  // Создаём объект игры
game.start();            // Запускаем
```

В конструкторе `Game()` происходит много всего сразу:
- Создаётся `Scanner` для ввода пользователя
- Инициализируются все **коллекции**: `battleLog = new ArrayList<>()`, `questLog = new LinkedList<>()`, `achievements = new HashSet<>()`, `leaderboard = new TreeSet<>()`, `undoStack = new ArrayDeque<>()`, `lootTable = new HashMap<>()`, `bestiary = new Bestiary()`
- Увеличивается статический счётчик `totalGamesPlayed`
- Срабатывает **статический блок инициализации** `static { ... }` при загрузке класса

### Шаг 2: Создание героя — `createHero()` (Game.java, строки 525–620)

Здесь работают вместе:
- **Scanner** — читает имя героя с клавиатуры
- **throws / try-catch** — `readHeroName()` бросает `InvalidActionException`, если имя пустое
- **do-while** — показывает меню, пока игрок не введёт 1, 2 или 3
- **switch-выражение** — по номеру создаёт конкретного героя:
  ```java
  hero = switch (choice) {
      case 1 -> new Warrior(name);   // hero = GameCharacter, реально Warrior
      case 2 -> new Mage(name);      // hero = GameCharacter, реально Mage
      case 3 -> new Archer(name);    // hero = GameCharacter, реально Archer
      default -> new Warrior(name);
  };
  ```
- **Полиморфизм** — `hero.getBattleCry()` вызывает метод того класса, объект которого реально создан

### Шаг 3: Заполнение инвентаря — `setupInventory()` (Game.java, строки 654–676)

Здесь работают **Generics** и **checked exceptions**:
```java
try {
    inventory.addItem(new Inventory.ItemInfo("Зелье здоровья", 30), 3);
} catch (InventoryFullException e) {
    // InventoryFullException — checked, обязательна обработка
}
```
- `Inventory.ItemInfo` — **static nested class** (создаётся без привязки к инвентарю)
- `Slot` — **inner class** (создаётся внутри `addItem`, привязан к `this`)
- `InventoryFullException` — наш `checked exception`, компилятор заставляет обернуть в `try-catch`

### Шаг 4: Заполнение таблицы лута — `setupLootTable()` (Game.java, строки 678–739)

Здесь работает **HashMap**:
```java
lootTable.put(EnemyRank.COMMON, List.of(
    new LootDrop("Зелье здоровья", 10, 0, 0.5),
    new LootDrop("Золото", 25, 0, 0.8)
));
lootTable.put(EnemyRank.BOSS, List.of(
    new LootDrop("Легендарный эликсир", 100, 5, 0.3)
));
```
Ключ — `EnemyRank` (enum), значение — `List<LootDrop>` (список record-объектов).

### Шаг 5: Загрузка данных врагов — `loadEnemyData()` (Game.java, строки 740–770)

Здесь работает **try-with-resources**:
```java
try (Scanner fileScanner = new Scanner(dataFile)) {
    // fileScanner закроется автоматически — даже при исключении!
} catch (FileNotFoundException e) {
    // Файл не найден — нормально, используем данные по умолчанию
}
```
Без `try-with-resources` пришлось бы вручную закрывать `Scanner` в `finally` — легко забыть.

### Шаг 6: Главный игровой цикл — `gameLoop()` (Game.java, строки 776–1010)

Здесь собраны **коллекции**:
- Создаётся `List<Enemy> enemies` — список врагов (`ArrayList`)
- `Collections.sort(enemies)` — сортировка по `Comparable` (Enemy.compareTo)
- `Comparator<Enemy> byName = Comparator.comparing(Enemy::getName)` — кастомный порядок
- Цикл `while` перебирает список, вызывая `battle(enemy)` для каждого

После каждой победы:
- `questLog.addFirst(...)` — **LinkedList** (O(1) вставка в начало)
- `battleLog` показывается и очищается — **ArrayList**
- `checkAchievements(enemy)` — проверка **HashSet** достижений

### Шаг 7: Бой — `battle()` (Game.java, строки 1321–1614)

Самый насыщенный метод! Здесь:
- **assert** — проверка допустимости перехода `GameState` (нужен флаг `-ea`)
- **Callback** — `listener.onBattleStart(hero, enemy.getName())`
- **PriorityQueue** — определяет инициативу (кто ходит первым):
  ```java
  PriorityQueue<Combatant> initiativeQueue = new PriorityQueue<>(
      Comparator.comparingInt(Combatant::speed).reversed()
  );
  ```
- **Побитовые операции** — управление статусными эффектами:
  ```
  statusFlags |= POISONED      // установить флаг (отравлен)
  statusFlags &= ~STUNNED      // снять флаг (оглушение прошло)
  (statusFlags & SHIELDED) != 0  // проверить флаг (есть ли щит?)
  ```
- **instanceof + pattern matching** — определяем тип урона и тип героя:
  ```java
  if (damage instanceof DamageType.Physical p) {
      // p.amount() — доступ к полю record
  }
  ```
- **Полиморфизм** — один вызов `hero.performAttack()` даёт разный результат для Warrior/Mage/Archer

### Шаг 8: Использование предмета — `handleUseItem()` (Game.java, строки 2031–2185)

Здесь работают **Iterator** и **multi-catch**:
```java
// for-each через Iterable (Inventory реализует Iterable<Slot>)
for (var slot : inventory) {
    System.out.println(slot);
}

// try-catch-finally с multi-catch
try {
    var slot = inventory.getSlot(itemIndex);
    // ...
} catch (IndexOutOfBoundsException | IllegalStateException e) {
    // multi-catch: один блок для двух типов исключений
} finally {
    // выполняется ВСЕГДА
}
```

### Шаг 9: Меню между боями (Game.java, строки 1023–1067)

Здесь демонстрируются **TreeMap** и **HashSet**:
- **Бестиарий** — `Bestiary` (обёртка над `TreeMap`): записи в алфавитном порядке, навигация `headMap()`
- **Достижения** — `showAchievements()` перебирает `Set<Achievement>`
- **Журнал квестов** — `showQuestLog()` показывает **LinkedList**

### Шаг 10: Итоги — `showFinalStats()` (Game.java, строки 2399–2618)

Здесь работают:
- **record** `BattleStats` — собирает статистику в неизменяемый объект
- **TreeSet** `leaderboard` — добавление нового рекорда, вывод топ-5
- **for-each по Iterable (Bestiary)** — перебор бестиария в алфавитном порядке:
  ```java
  for (var entry : bestiary) {
      // entry — Map.Entry<String, BestiaryEntry>
      // TreeMap гарантирует алфавитный порядок
  }
  ```
- **Приведение типов**: `(double)`, `(int)`, `(long)` — для расчёта процентов и очков

---

## 5. Советы для самостоятельного изучения

### Что попробовать изменить

1. **Добавь нового персонажа `Paladin`** — создай класс, наследующий `GameCharacter`.
   Паладин может и атаковать, и лечиться одновременно.
   Это закрепит: `extends`, `super()`, `@Override`, абстрактные методы.

2. **Добавь новый тип урона `Poison(int amount, int duration)`** в `DamageType`.
   Компилятор подскажет все места, где нужно добавить обработку (спасибо `sealed`!).
   Это закрепит: `sealed interface`, `record`, `switch`.

3. **Добавь новое достижение** в `Achievement.java` и условие в `checkAchievements()`.
   `HashSet` автоматически не допустит дубликат, если одно условие сработает дважды.
   Это закрепит: `enum`, `HashSet`, `Set.add()`.

4. **Создай новый тип врага `Dragon` с другим рангом** и добавь его в бестиарий вручную.
   Посмотри, как `TreeMap` автоматически расставит его по алфавиту среди других.
   Это закрепит: `TreeMap`, `NavigableMap`, `Comparable` для ключей-строк.

5. **Напиши свой `Comparator`** для сортировки врагов по уровню здоровья по убыванию.
   `Comparator.comparingInt(Enemy::getHealth).reversed()`.
   Это закрепит: `Comparator`, ссылки на методы `::`, лямбды.

6. **Брось `InsufficientResourceException`** из атаки мага, если не хватает маны.
   Потом поймай его в `handleSpecialAttack()` и напечатай понятное сообщение.
   Это закрепит: `throws`, `throw`, `try-catch`, пользовательские исключения.

7. **Добавь сохранение результатов в файл** — после игры записывай статистику.
   Используй `FileWriter` + `try-with-resources`.
   Это закрепит: `try-with-resources`, `IOException`, работу с файлами.

### Соответствие глав metanit.com и файлов проекта

| Тема на metanit.com                              | Файлы проекта                                                                    |
|--------------------------------------------------|----------------------------------------------------------------------------------|
| [2.1] Переменные и типы данных                   | `Game.java` (int, byte, double, boolean, char, String)                           |
| [2.3] Операции с числами                         | `Game.java`, `GameCharacter.java` (арифметика, инкремент, составное присваивание)|
| [2.4] Побитовые операции                         | `Game.java` (флаги: `&`, `\|`, `^`, `~`, `<<`)                                  |
| [2.5] Условные выражения                         | `Game.java` (if/else, тернарный, switch-выражение)                               |
| [2.6] Циклы                                      | `Game.java` (while, do-while, for, for-each, break)                              |
| [2.7] Массивы                                    | `Game.java` (List вместо массивов, пояснение перехода)                           |
| [2.8] Методы                                     | Все файлы (параметры, return, void, перегрузка, varargs)                         |
| [2.9] Введение в обработку исключений            | `Game.java` (try-catch, NumberFormatException)                                   |
| [3.1] Классы и объекты                           | `Enemy.java`, `Game.java` (class, new, поля, конструктор)                        |
| [3.2] Пакеты                                     | Все файлы (`package rpg;`)                                                       |
| [3.3] Модификаторы доступа                       | `GameCharacter.java` (private, protected, public)                                |
| [3.4] Статические члены                          | `Game.java`, `GameCharacter.java` (static поля и методы)                         |
| [3.5] Наследование                               | `Warrior.java`, `Mage.java`, `Archer.java` (extends, super)                      |
| [3.6] Абстрактные классы                         | `GameCharacter.java` (abstract class, abstract methods)                          |
| [3.7] Интерфейсы                                 | `Attackable.java`, `Healable.java` (default, static методы)                      |
| [3.8] Enum                                       | `GameState.java`, `EnemyRank.java`, `Achievement.java`, `ItemType.java`          |
| [3.9] Object и toString                          | `GameCharacter.java`, `Enemy.java` (toString, equals, hashCode)                  |
| [3.10] instanceof                                | `Game.java` (instanceof + pattern matching)                                      |
| [3.11] Обобщения (generics)                      | `Inventory.java` (`<T>`, @SuppressWarnings)                                      |
| [3.12] Внутренние и вложенные классы             | `Inventory.java` (inner class Slot, static nested class ItemInfo)                |
| [3.13] Ссылочные типы и clone()                  | `Enemy.java` (Cloneable, clone(), поверхностное копирование)                     |
| [3.14] Объекты как параметры                     | `GameCharacter.java` (передача hero в методы Game)                               |
| [3.15] Generics + наследование                   | `Inventory.java`, `GameCharacter.java` (наследование generic-типов)              |
| [3.16] Callbacks                                 | `BattleEventListener.java` (паттерн «Наблюдатель», onAttack/onLevelUp)           |
| [3.17] Ограниченные обобщения                    | `GameCharacter.java` строка 725 (`<T extends GameCharacter>`)                    |
| [3.18] Records                                   | `BattleStats.java`, `BattleRecord.java`, `LootDrop.java`, `DamageType.java`      |
| [3.19] Compact code / var                        | `Game.java` (var, текстовые блоки, короткий синтаксис)                           |
| [3.21] Блоки инициализации + цепочка конструкторов | `Enemy.java` (static {}, {}, this(...))                                       |
| [3.22] Инкапсуляция                              | `GameCharacter.java` (private + геттеры/сеттеры)                                 |
| [3.23] final class / final method                | `GameCharacter.java` (final takeDamage(), final isAlive())                       |
| [3.24] Полиморфизм                               | `Game.java` строка 595 (hero = Warrior/Mage/Archer)                              |
| [3.25] Sealed классы                             | `DamageType.java` (sealed interface + permits + records)                         |
| [3.26] Pattern matching                          | `Game.java` (instanceof + pattern variable, switch + patterns)                   |
| [3.27] Record patterns                           | `Game.java` (деструктуризация record в pattern matching)                         |
| [3.28] Интерфейсы + полиморфизм                  | `Attackable.java`, `Healable.java` + `GameCharacter.java`                        |
| [3.29] Множественные интерфейсы                  | `GameCharacter.java` (implements Attackable, Healable)                           |
| [3.31] Type erasure                              | `Inventory.java` строка 331 (почему нужен @SuppressWarnings)                     |
| [3.32] Wildcards                                 | `GameCharacter.java` строка 690, `Inventory.java` строка 468                     |
| [4.1] try-catch-finally                          | `Game.java` строки 548, 2636–2670                                                |
| [4.2] try-with-resources, multi-catch            | `Game.java` строки 754, 2642, 2087                                               |
| [4.3] Пользовательские исключения                | `GameException.java` и 3 наследника                                              |
| [4.4] assert                                     | `Game.java` строка 1356                                                          |
| [5.1] Интерфейс Collection                       | `Game.java` строки 17–23 (иерархия в комментариях)                              |
| [5.2] ArrayList                                  | `Game.java` строки 403, 795–816                                                  |
| [5.3] LinkedList                                 | `Game.java` строки 404, 950–954                                                  |
| [5.4] HashSet                                    | `Game.java` строки 406, 265–274                                                  |
| [5.5] TreeSet + SortedSet                        | `Game.java` строки 407, 276–285, ~2547–2570                                      |
| [5.6] Comparable + Comparator                    | `Enemy.java` строка 608; `Game.java` строки 812, 831, 845                        |
| [5.7] PriorityQueue + ArrayDeque                 | `Game.java` строки 1444, 287–304, 1647–1701                                      |
| [5.8] HashMap                                    | `Game.java` строки 405, 252–263, ~700                                            |
| [5.9] TreeMap + NavigableMap                     | `Bestiary.java` строки 177, 265–300                                              |
| [5.10] Iterator + Iterable                       | `Inventory.java` строки 61, 510, 537; `Bestiary.java` строка 369                |

---

Удачи в изучении! Если что-то непонятно — открой файл и читай комментарии.
Каждая конструкция Java подробно объяснена прямо в коде, на русском языке.
