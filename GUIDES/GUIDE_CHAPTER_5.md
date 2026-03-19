# Глава 5: Коллекции (5.1–5.10)

Этот гайд соответствует главе 5 учебника metanit.com.
Каждый подраздел показывает: какую концепцию демонстрирует код, в каком файле она живёт и как выглядит в реальном коде игры.

---

## Содержание

1. [Иерархия коллекций](#1-иерархия-коллекций)
2. [Таблица: коллекция → RPG-применение](#2-таблица-коллекция--rpg-применение)
3. [Подглава 5.1 — Интерфейс Collection](#3-подглава-51--интерфейс-collection)
4. [Подглава 5.2 — ArrayList](#4-подглава-52--arraylist)
5. [Подглава 5.3 — LinkedList](#5-подглава-53--linkedlist)
6. [Подглава 5.4 — HashSet](#6-подглава-54--hashset)
7. [Подглава 5.5 — TreeSet и SortedSet](#7-подглава-55--treeset-и-sortedset)
8. [Подглава 5.6 — Comparable и Comparator](#8-подглава-56--comparable-и-comparator)
9. [Подглава 5.7 — PriorityQueue и ArrayDeque](#9-подглава-57--priorityqueue-и-arraydeque)
10. [Подглава 5.8 — HashMap](#10-подглава-58--hashmap)
11. [Подглава 5.9 — TreeMap и NavigableMap](#11-подглава-59--treemap-и-navigablemap)
12. [Подглава 5.10 — Iterator и Iterable](#12-подглава-510--iterator-и-iterable)

---

## 1. Иерархия коллекций

Все коллекции Java образуют единую иерархию интерфейсов. Диаграмма показывает связи и реализации, которые используются в проекте.

```
java.lang.Iterable<E>
    └─ java.util.Collection<E>          ← общий интерфейс (5.1)
         ├─ java.util.List<E>           ← упорядоченный список, допускает дубликаты
         │   ├─ ArrayList<E>            ← динамический массив           [battleLog, enemies]
         │   └─ LinkedList<E>           ← двусвязный список             [questLog]
         │
         ├─ java.util.Set<E>            ← уникальные элементы
         │   ├─ HashSet<E>              ← хеш-таблица, без порядка      [achievements]
         │   └─ TreeSet<E>              ← красно-чёрное дерево, сортир. [leaderboard]
         │
         └─ java.util.Queue<E>          ← очередь (FIFO / приоритет)
              ├─ PriorityQueue<E>       ← мин-куча с приоритетом        [initiativeQueue]
              └─ java.util.Deque<E>     ← двусторонняя очередь
                   └─ ArrayDeque<E>     ← на массиве, стек/очередь      [undoStack]

java.util.Map<K,V>                      ← пары ключ-значение (НЕ Collection!)
    ├─ HashMap<K,V>                     ← хеш-таблица, без порядка      [lootTable]
    └─ TreeMap<K,V>                     ← красно-чёрное дерево, сортир. [Bestiary.entries]
         └─ реализует NavigableMap<K,V>
```

Замечание: `Map` намеренно вынесен за пределы `Collection` — он хранит **пары**, а не отдельные элементы.

---

## 2. Таблица: коллекция → RPG-применение

| Коллекция | Тип | Файл | Поле / переменная | Зачем именно эта коллекция |
|---|---|---|---|---|
| `ArrayList<String>` | List | `Game.java:299` | `battleLog` | Быстрая запись в конец O(1), доступ по индексу O(1) |
| `ArrayList<Enemy>` | List | `Game.java:909` (лок.) | `enemies` (в `gameLoop`) | Хранит врагов по порядку; сортировка через `Collections.sort()` |
| `LinkedList<String>` | List + Deque | `Game.java:314` | `questLog` | `addFirst()` O(1) — «закрепить» последнюю победу наверху |
| `HashSet<Achievement>` | Set | `Game.java:342` | `achievements` | Гарантированная уникальность; `add()` возвращает `false` на дубликате |
| `TreeSet<BattleRecord>` | SortedSet | `Game.java:353` | `leaderboard` | Авто-сортировка по `compareTo()`; первый элемент = лучший рекорд |
| `PriorityQueue<Combatant>` | Queue | `Game.java:1721` (лок.) | `initiativeQueue` (в `battle`) | Определение порядка хода по скорости персонажа |
| `ArrayDeque<String>` | Deque (стек) | `Game.java:373` | `undoStack` | LIFO-стек отмены действий; быстрее устаревшего `Stack` |
| `HashMap<EnemyRank,List<LootDrop>>` | Map | `Game.java:331` | `lootTable` | Доступ по ключу-enum O(1); у каждого ранга своя таблица лута |
| `TreeMap<String,BestiaryEntry>` | NavigableMap | `Bestiary.java:248` | `entries` (внутри `Bestiary`) | Алфавитная сортировка врагов; `firstKey()`, `headMap()` |
| `Iterator<Slot>` / `Iterable` | — | `Inventory.java:575` | через `iterator()` | for-each по инвентарю с пользовательским итератором |
| `Iterator<Entry>` / `Iterable` | — | `Bestiary.java:440` | через `iterator()` | for-each по бестиарию (делегирует `TreeMap.entrySet().iterator()`) |

---

## 3. Подглава 5.1 — Интерфейс Collection

**Концепция:** `Collection<E>` — корневой интерфейс всех одиночных коллекций. Определяет общий контракт: `add()`, `remove()`, `size()`, `isEmpty()`, `contains()`, `iterator()`, `toArray()`.

**Зачем это важно:** Благодаря `Collection` можно писать методы, которые работают с любой коллекцией:

```java
// Game.java — объявление полей (строки 299–342)
// Тип объявлен как List/Set (интерфейс), реализация — ArrayList/HashSet.
// Это полиморфизм: если позже заменить ArrayList на LinkedList — менять
// остальной код не нужно. «Программируй к интерфейсу, а не к реализации».

private transient List<String> battleLog;     // интерфейс List
private Set<Achievement> achievements;         // интерфейс Set
private TreeSet<BattleRecord> leaderboard;    // конкретный TreeSet (нужны методы SortedSet)
```

```java
// Game.java — конструктор, строки 474–479
// Инициализируем конкретными классами, но переменные объявлены как интерфейсы.
battleLog    = new ArrayList<>();   // List<String> ← ArrayList
questLog     = new LinkedList<>();  // LinkedList<String> ← LinkedList
lootTable    = new HashMap<>();     // Map<EnemyRank, List<LootDrop>> ← HashMap
achievements = new HashSet<>();     // Set<Achievement> ← HashSet
leaderboard  = new TreeSet<>();     // TreeSet<BattleRecord> (нет менее конкретного интерфейса)
undoStack    = new ArrayDeque<>();  // ArrayDeque<String>
bestiary     = new Bestiary();      // обёртка над TreeMap
```

**Общие методы всех коллекций:**

| Метод | Что делает | Пример в коде |
|---|---|---|
| `size()` | количество элементов | `achievements.size()` `Game.java:2794` |
| `isEmpty()` | `true` если пустая | `battleLog.isEmpty()` `Game.java:2884` |
| `add(e)` | добавить элемент | `achievements.add(...)` `Game.java:1424` |
| `contains(e)` | проверить наличие | `bestiary.contains(name)` `Bestiary.java:397` |
| `clear()` | очистить | `undoStack.clear()` `Game.java:1611` |
| `iterator()` | получить итератор | `inventory.iterator()` `Inventory.java:575` |

---

## 4. Подглава 5.2 — ArrayList

**Концепция:** `ArrayList<E>` — динамический массив. Внутри хранит обычный `Object[]`. При заполнении автоматически расширяется (копирует все элементы в массив вдвое большего размера).

**Временная сложность:**
- `add(e)` — O(1) амортизировано (иногда O(n) при расширении)
- `get(i)` — O(1)
- `remove(i)` — O(n) (сдвиг элементов)
- `contains(e)` — O(n) (перебор)

### Применение 1: `battleLog` — журнал боевых событий

```java
// Game.java — объявление поля, строка 299
private transient List<String> battleLog;

// Game.java — инициализация в конструкторе, строка 474
battleLog = new ArrayList<>();

// Game.java — gameLoop(), строка 1022
// Сбрасываем журнал перед каждым боем — создаём новый ArrayList.
// Альтернатива: battleLog.clear(), но new ArrayList<>() — чистый старт.
battleLog = new ArrayList<>();

// Game.java — battle(), строка 1655
// add() — добавление в конец списка: O(1).
battleLog.add("Начало боя: " + hero.getName() + " vs " + enemy.getName());
```

### Применение 2: `enemies` — список врагов в бою

```java
// Game.java — gameLoop(), строки 909–929
// new ArrayList<>(MAX_ENEMIES) — начальная ёмкость 5.
// Оптимизация: ArrayList не расширяет внутренний массив → нет лишних копирований.
List<Enemy> enemies = new ArrayList<>(MAX_ENEMIES);

// add() — O(1) в среднем.
enemies.add(new Enemy("Гоблин",  30,  5,  20, EnemyRank.COMMON));
enemies.add(new Enemy("Скелет",  40,  8,  30, EnemyRank.COMMON));
enemies.add(new Enemy("Орк",     60, 12,  50, EnemyRank.ELITE));
enemies.add(new Enemy("Тролль",  80, 15,  70, EnemyRank.ELITE));
enemies.add(new Enemy("Дракон", 120, 25, 100, EnemyRank.BOSS));

// Collections.sort() — сортировка по natural ordering (Enemy.compareTo).
Collections.sort(enemies);

// get(index) — O(1) доступ по индексу.
Enemy enemy = enemies.get(enemyIndex);

// for-each по ArrayList — компилятор разворачивает в вызовы iterator().
for (String event : battleLog) {
    System.out.println("  " + event);
}
```

### Применение 3: сбор новых достижений

```java
// Game.java — checkAchievements(), строка 1420
// ArrayList используется как временный буфер для отображения только НОВЫХ достижений.
// HashSet (achievements) не знает, что было «только что добавлено».
List<Achievement> newlyUnlocked = new ArrayList<>();
if (achievements.add(Achievement.FIRST_BLOOD)) {
    newlyUnlocked.add(Achievement.FIRST_BLOOD);  // только если add() вернул true
}
```

**Когда использовать ArrayList:**
- нужен быстрый доступ по индексу
- элементы добавляются преимущественно в конец
- порядок вставки важен

---

## 5. Подглава 5.3 — LinkedList

**Концепция:** `LinkedList<E>` — двусвязный список. Каждый узел хранит ссылки на предыдущий (`prev`) и следующий (`next`) элементы.

**Временная сложность:**
- `addFirst()` / `addLast()` — O(1) (только обновление ссылок)
- `get(i)` — O(n) (перебор с начала или конца)
- `remove(узел)` — O(1), если есть ссылка на узел

**Сравнение с ArrayList:**

| Операция | ArrayList | LinkedList |
|---|---|---|
| `get(i)` | **O(1)** | O(n) |
| `add` в конец | O(1) амортиз. | **O(1)** |
| `add` в начало | O(n) сдвиг! | **O(1)** |
| `remove(i)` | O(n) сдвиг | O(n) поиск |
| Доп. память | минимальная | +2 ссылки на узел |

### Применение: `questLog` — журнал квестов

```java
// Game.java — объявление, строка 314
private LinkedList<String> questLog;

// Game.java — инициализация, строка 475
questLog = new LinkedList<>();

// Game.java — gameLoop(), строки 1061–1068
// Новые записи — в конец (хронологический порядок).
questLog.addLast("Победа над " + enemy.getName() + " (бой " + (enemyIndex + 1) + ")");

// «Закреплённая» запись — в начало: O(1)!
// Для ArrayList это было бы O(n) — сдвиг всех элементов вправо.
questLog.addFirst("★ Последняя победа: " + enemy.getName() + "!");

// Game.java — showFinalStats(), строка 2820
System.out.println("║  Записей: " + questLog.size());
```

```java
// Game.java — showQuestLog(), строка 1554
// for-each работает, т.к. LinkedList реализует Iterable.
// Обход всегда O(n) — последовательно от первого к последнему.
for (String entry : questLog) {
    System.out.println("  " + entry);
}
```

**Когда использовать LinkedList:**
- часто нужно добавлять/удалять в начало или конец
- не нужен доступ по индексу
- реализация Deque (двусторонней очереди)

**Когда НЕ использовать LinkedList:**
- нужен случайный доступ `get(i)` — используйте ArrayList
- хранятся примитивы — LinkedList тратит +2 ссылки на каждый узел

---

## 6. Подглава 5.4 — HashSet

**Концепция:** `HashSet<E>` — множество уникальных элементов на хеш-таблице. Гарантирует отсутствие дубликатов. Порядок элементов не определён.

**Механизм уникальности:**
1. При `add(e)` вычисляется `e.hashCode()` → определяется номер «корзины»
2. Внутри корзины через `equals()` ищется равный элемент
3. Если найден — элемент не добавляется, `add()` возвращает `false`
4. Если не найден — элемент добавляется, `add()` возвращает `true`

**Временная сложность:** `add()`, `contains()`, `remove()` — O(1) в среднем.

### Применение: `achievements` — достижения игрока

```java
// Game.java — объявление, строка 342
// Объявляем как Set<Achievement> (интерфейс), а не HashSet<Achievement> (реализация).
// Если нужна сортировка — достаточно заменить new HashSet<>() на new TreeSet<>().
private Set<Achievement> achievements;

// Game.java — инициализация, строка 477
achievements = new HashSet<>();

// Game.java — checkAchievements(), строки 1422–1508
// Ключевой паттерн: add() возвращает boolean.
// true  → элемент добавлен (достижение НОВОЕ).
// false → элемент уже был (достижение уже получено, ничего не делаем).
if (achievements.add(Achievement.FIRST_BLOOD)) {
    newlyUnlocked.add(Achievement.FIRST_BLOOD);
}

// Для enum == правильный способ сравнения (не equals()).
// Каждая константа enum — единственный объект в JVM (singleton).
if (enemy.getRank() == EnemyRank.BOSS) {
    if (achievements.add(Achievement.DRAGON_SLAYER)) {
        newlyUnlocked.add(Achievement.DRAGON_SLAYER);
    }
}

// Game.java — showFinalStats(), строки 2786–2794
// isEmpty() — O(1).
if (achievements.isEmpty()) {
    System.out.println("║  Нет достижений.");
} else {
    // for-each по HashSet — порядок НЕ гарантирован.
    for (Achievement a : achievements) {
        System.out.println("║  " + a);
    }
}
// Achievement.values() — возвращает массив всех констант enum.
System.out.println("║  Всего: " + achievements.size() + "/" + Achievement.values().length);

// Game.java — gameLoop(), строка 1122
// contains() НЕ используется — достаточно add() (идемпотентная операция).
// add() к уже существующему элементу — просто вернёт false.
achievements.add(Achievement.FULL_CLEAR);
```

**Enum и HashSet — идеальная пара:**

```
Achievement.java — каждый enum-элемент имеет hashCode() и equals() из java.lang.Enum:
  hashCode() — основан на ordinal() (порядковом номере: FIRST_BLOOD=0, FLAWLESS=1...)
  equals()   — сравнивает через ==  (у каждой константы один экземпляр в JVM)
Коллизии между разными enum-константами практически невозможны → O(1) всегда.
```

**Когда использовать HashSet:**
- нужна проверка «есть ли элемент?» O(1)
- важна уникальность, но не порядок
- хранятся объекты с корректными `hashCode()` и `equals()`

---

## 7. Подглава 5.5 — TreeSet и SortedSet

**Концепция:** `TreeSet<E>` — отсортированное множество на красно-чёрном дереве (balanced BST). Элементы всегда хранятся в порядке, определённом `Comparable` или `Comparator`.

**Временная сложность:** `add()`, `remove()`, `contains()` — O(log n).

**Ключевые методы SortedSet/TreeSet:**
- `first()` — наименьший элемент
- `last()` — наибольший элемент
- `headSet(to)` — подмножество с элементами < to
- `tailSet(from)` — подмножество с элементами >= from
- `subSet(from, to)` — подмножество в диапазоне

### Применение: `leaderboard` — таблица рекордов

```java
// Game.java — объявление, строка 353
// TreeSet (а не просто SortedSet), т.к. SortedSet не предоставляет first()/last().
// TreeSet объявлен конкретным типом намеренно.
private TreeSet<BattleRecord> leaderboard;

// Game.java — инициализация, строка 478
// new TreeSet<>() без аргументов — порядок определяется BattleRecord.compareTo().
// Если передать Comparator — он имеет приоритет над compareTo().
leaderboard = new TreeSet<>();

// Game.java — showFinalStats(), строки 2846–2873
// Создаём запись о текущем прохождении.
BattleRecord record = new BattleRecord(
    hero.getName(),
    totalScore,        // long — накопленные очки
    enemiesDefeated,   // int — количество побед
    System.currentTimeMillis()  // long — временная метка
);

// add() — вставляет в правильную позицию по compareTo(): O(log n).
// Если compareTo() вернёт 0 — запись НЕ добавится (Set не хранит дубликаты)!
// BattleRecord.compareTo() использует 3 уровня: score → имя → timestamp,
// чтобы гарантировать уникальность даже при одинаковом счёте.
leaderboard.add(record);

// for-each по TreeSet — обход В ПОРЯДКЕ compareTo() (убывание score).
// Первый элемент = лучший рекорд.
int place = 1;
for (BattleRecord br : leaderboard) {
    System.out.println(place + ". " + br.heroName() + " — Счёт: " + br.score());
    place++;
    if (place > 5) break;  // показываем только топ-5
}
```

**Важная ловушка TreeSet:**

```
TreeSet определяет «равенство» через compareTo(), а НЕ через equals()!
Если два объекта A и B: A.compareTo(B) == 0 → TreeSet считает их дубликатами.
B НЕ будет добавлен, даже если A.equals(B) == false!

BattleRecord решает это 3-уровневой сортировкой:
  1. score (убывание) — разные очки → разные позиции
  2. heroName — одинаковые очки → по имени
  3. timestamp (убывание) — одинаковые очки и имя → по времени
Три уровня почти исключают ситуацию compareTo() == 0 у разных записей.
```

---

## 8. Подглава 5.6 — Comparable и Comparator

**Концепции:**
- `Comparable<T>` — **встроенный** порядок, реализуется в самом классе
- `Comparator<T>` — **внешний** порядок, создаётся отдельно, можно иметь несколько

### Comparable: `Enemy` и `BattleRecord`

```java
// Enemy.java — строка 43
// implements Comparable<Enemy> → определяет «естественный» порядок врагов.
// Используется в: Collections.sort(enemies), TreeSet<Enemy>, Arrays.sort(enemies[]).
public class Enemy implements Cloneable, Comparable<Enemy> {
    // ...
    @Override
    public int compareTo(Enemy other) {
        // Сортировка по «мощи» = health + attack.
        // Слабые враги первыми (возрастание).
        int thisPower  = this.getHealth()  + this.getAttack();
        int otherPower = other.getHealth() + other.getAttack();
        return Integer.compare(thisPower, otherPower);
    }
}

// Game.java — gameLoop(), строка 926
// Collections.sort() использует Enemy.compareTo() — слабые враги первыми.
Collections.sort(enemies);
```

```java
// BattleRecord.java — строка 150
// Сортировка по score убывание → имя → timestamp убывание.
@Override
public int compareTo(BattleRecord other) {
    int scoreCompare = Long.compare(other.score, this.score); // УБЫВАНИЕ
    if (scoreCompare != 0) return scoreCompare;

    int nameCompare = this.heroName.compareTo(other.heroName);
    if (nameCompare != 0) return nameCompare;

    return Long.compare(other.timestamp, this.timestamp); // УБЫВАНИЕ
}

// ПОЧЕМУ Long.compare(), а НЕ вычитание?
// other.score - this.score → ПЕРЕПОЛНЕНИЕ (overflow) при больших значениях!
// Long.MAX_VALUE - (-1) = Long.MAX_VALUE + 1 = ОТРИЦАТЕЛЬНОЕ ЧИСЛО (неверно!)
// Long.compare() использует условные операторы — переполнение невозможно.
```

### Comparator: альтернативные сортировки врагов

```java
// Game.java — gameLoop(), строки 939–966

// Comparator.comparing(Enemy::getName) — сортировка по имени (алфавит, возрастание).
// Enemy::getName — ссылка на метод (method reference, Java 8+).
// Эквивалент лямбды: (e1, e2) -> e1.getName().compareTo(e2.getName())
Comparator<Enemy> byName = Comparator.comparing(Enemy::getName);

List<Enemy> sortedByName = new ArrayList<>(enemies);
sortedByName.sort(byName);  // List.sort() принимает Comparator

// .reversed() — инвертирует порядок: возрастание → убывание.
// Безопаснее, чем (e1, e2) -> e2.getExpReward() - e1.getExpReward()
// (при больших int тоже возможно переполнение!).
Comparator<Enemy> byExpDesc = Comparator.comparing(Enemy::getExpReward).reversed();
List<Enemy> sortedByExp = new ArrayList<>(enemies);
sortedByExp.sort(byExpDesc);
```

**Сравнение подходов:**

| Характеристика | Comparable | Comparator |
|---|---|---|
| Количество порядков | один | любое |
| Где определяется | внутри класса | снаружи |
| Применение | «главный» порядок | альтернативные сортировки |
| Синтаксис (Java 8+) | `implements Comparable<T>` | `Comparator.comparing(...)` |
| Пример в проекте | `Enemy`, `BattleRecord` | `byName`, `byExpDesc` в `gameLoop` |

---

## 9. Подглава 5.7 — PriorityQueue и ArrayDeque

### PriorityQueue: очередь инициативы

**Концепция:** `PriorityQueue<E>` — очередь с приоритетом на основе бинарной кучи (binary heap). По умолчанию — min-heap: `poll()` возвращает наименьший элемент.

**Временная сложность:** `offer()`, `poll()` — O(log n); `peek()` — O(1).

**Важно:** for-each по PriorityQueue НЕ гарантирует порядок! Порядок гарантируется только при последовательных вызовах `poll()`.

```java
// Game.java — battle(), строки 1704–1749

// Локальный record — объявляется внутри метода (Java 16+).
// Доступен только внутри battle(). Удобно для временных структур.
record Combatant(String name, int speed, boolean isHero) {}

// Comparator.comparingInt(Combatant::speed) — сортировка по полю speed (int).
// .reversed() — убывание: самый быстрый первым.
//
// Без .reversed(): poll() вернёт САМОГО МЕДЛЕННОГО (min-heap по умолчанию).
// С .reversed(): poll() вернёт САМОГО БЫСТРОГО — это нам и нужно.
//
// Альтернатива: Comparator.comparingInt(c -> -c.speed())
// Но: -Integer.MIN_VALUE вызывает переполнение! .reversed() безопаснее.
PriorityQueue<Combatant> initiativeQueue = new PriorityQueue<>(
    Comparator.comparingInt(Combatant::speed).reversed()
);

// offer() — добавить в очередь: O(log n). Аналог add(), но не бросает исключение.
initiativeQueue.offer(new Combatant(hero.getName(),  hero.getSpeed(),  true));
initiativeQueue.offer(new Combatant(enemy.getName(), enemy.getSpeed(), false));

// poll() — извлечь элемент с наивысшим приоритетом: O(log n).
// После вызова элемент удаляется из очереди.
// peek() — то же, но элемент остаётся в очереди: O(1).
Combatant first = initiativeQueue.poll();  // самый быстрый
boolean heroFirst = first.isHero();
```

### ArrayDeque: стек отмены действий

**Концепция:** `ArrayDeque<E>` — двусторонняя очередь (Deque) на массиве. Работает как стек (LIFO) или очередь (FIFO). Современная замена устаревшему `Stack`.

**Временная сложность:** `push()`, `pop()`, `peek()` — O(1).

```java
// Game.java — объявление, строка 373
// ArrayDeque — рекомендованная замена Stack (Java 1.0).
// Stack наследует Vector (синхронизирован, медленнее).
// ArrayDeque — не синхронизирован, современный, быстрее.
private transient ArrayDeque<String> undoStack;

// Game.java — инициализация, строка 479
undoStack = new ArrayDeque<>();

// Game.java — battle(), строка 1611
// clear() — очищает стек перед новым боем: O(n).
undoStack.clear();

// Паттерн «стек действий»:
// Перед действием — сохраняем описание на стек.
// push(e) — положить на вершину (эквивалент addFirst).
undoStack.push("Обычная атака по " + enemy.getName());

// При отмене:
// pop() — снять с вершины (эквивалент removeFirst).
// Бросает NoSuchElementException если стек пуст!
// peek() — посмотреть вершину без снятия.
String lastAction = undoStack.pop();
```

**Сравнение Queue-реализаций:**

| Класс | Внутри | Доступ к обоим концам | Синхронизирован | Когда использовать |
|---|---|---|---|---|
| `PriorityQueue` | бинарная куча | нет | нет | элементы с приоритетом |
| `ArrayDeque` | массив | да | нет | стек / очередь (современный) |
| `LinkedList` | двусвязный список | да | нет | стек / очередь (старый код) |
| `Stack` | массив (Vector) | нет | да | устарел, не использовать |

---

## 10. Подглава 5.8 — HashMap

**Концепция:** `HashMap<K,V>` — словарь (ассоциативный массив) на хеш-таблице. Хранит пары ключ-значение. Порядок не гарантирован.

**Временная сложность:** `put()`, `get()`, `containsKey()` — O(1) в среднем.

**Ключевые методы:**
- `put(k, v)` — добавить/перезаписать
- `get(k)` — получить (или `null`)
- `getOrDefault(k, def)` — получить или значение по умолчанию
- `containsKey(k)` — проверить наличие ключа
- `entrySet()` — набор пар для перебора
- `keySet()` — набор ключей
- `values()` — коллекция значений

### Применение: `lootTable` — таблица лута

```java
// Game.java — объявление, строка 331
// Ключ: EnemyRank (enum, 3 значения: COMMON, ELITE, BOSS).
// Значение: List<LootDrop> — список возможных наград для ранга.
// transient — данные константные, восстанавливаются вызовом setupLootTable().
private transient Map<EnemyRank, List<LootDrop>> lootTable;

// Game.java — setupLootTable(), строки 806–823
// List.of() — неизменяемый список (Java 9+). Нельзя add/remove.
// Подходит для константных данных.
lootTable.put(EnemyRank.COMMON, List.of(
    new LootDrop("Малое зелье здоровья", 10, 0, 0.7),
    new LootDrop("Кусок руды",           5,  0, 0.3)
));
lootTable.put(EnemyRank.ELITE, List.of(
    new LootDrop("Зелье здоровья",  25, 1, 0.8),
    new LootDrop("Свиток силы",     15, 2, 0.5),
    new LootDrop("Эликсир маны",    20, 1, 0.4)
));
lootTable.put(EnemyRank.BOSS, List.of(
    new LootDrop("Великое зелье",         100,  5, 1.0),
    new LootDrop("Легендарный артефакт",   50, 10, 0.3),
    new LootDrop("Сокровище дракона",     200, 15, 0.2)
));

// Game.java — handleLoot(), строка 1350
// getOrDefault() безопаснее get(): если ключа нет — List.of() (пустой список).
// get() вернул бы null → NullPointerException при isEmpty()!
List<LootDrop> possibleLoot = lootTable.getOrDefault(enemy.getRank(), List.of());
```

### Применение: Bestiary — TreeMap как более сложный Map

```java
// Bestiary.java — addEntry(), строки 270–285
// Map.containsKey() — O(log n) для TreeMap.
if (entries.containsKey(enemyName)) {
    // get() + put() — паттерн «прочитай, обнови, запиши».
    // record BestiaryEntry неизменяем → withKill() создаёт НОВУЮ запись.
    BestiaryEntry existing = entries.get(enemyName);
    entries.put(enemyName, existing.withKill(damageDealt));
} else {
    entries.put(enemyName, new BestiaryEntry(1, damageDealt, rank, System.currentTimeMillis()));
}
```

**LootDrop** как значение в Map:

```java
// LootDrop.java — record (строки 35–63)
// record — неизменяемый носитель данных (Java 16+).
// Автоматически генерирует equals() и hashCode() по всем полям.
// Это важно: если LootDrop использовался бы как КЛЮЧ Map — equals/hashCode обязательны.
// Здесь LootDrop — значение (value), поэтому equals/hashCode не критичны для доступа,
// но полезны при сравнении списков в тестах.
public record LootDrop(
    String itemName,  // название предмета
    int gold,         // количество золота
    int gems,         // количество самоцветов
    double chance     // шанс выпадения: 0.0..1.0
) { ... }

// Доступ к полям record — через геттеры БЕЗ префикса get:
// drop.itemName(), drop.gold(), drop.gems(), drop.chance()
```

---

## 11. Подглава 5.9 — TreeMap и NavigableMap

**Концепция:** `TreeMap<K,V>` — словарь на красно-чёрном дереве. Ключи всегда отсортированы. Реализует `NavigableMap` — расширение `SortedMap` с методами навигации по диапазону.

**Временная сложность:** `put()`, `get()`, `containsKey()` — O(log n).

**Когда выбирать TreeMap вместо HashMap:**
- нужен порядок ключей (алфавитный, числовой)
- нужны методы `firstKey()`, `lastKey()`, `headMap()`, `tailMap()`
- нужен обход в отсортированном порядке

### Применение: `Bestiary` — бестиарий врагов

```java
// Bestiary.java — строка 248
// TreeMap гарантирует алфавитный порядок имён врагов.
// Это и есть «естественный порядок» String — лексикографический.
private final TreeMap<String, BestiaryEntry> entries = new TreeMap<>();

// Bestiary.java — display(), строки 321–332
// entrySet() — возвращает Set<Map.Entry<K,V>>.
// Map.Entry<K,V> — вложенный интерфейс, представляет одну пару ключ-значение.
// entry.getKey()   — имя врага (String).
// entry.getValue() — BestiaryEntry (record с killCount, maxDamageDealt, rank, firstEncounter).
//
// В TreeMap for-each по entrySet() идёт в АЛФАВИТНОМ ПОРЯДКЕ имён.
for (Map.Entry<String, BestiaryEntry> entry : entries.entrySet()) {
    String name = entry.getKey();
    BestiaryEntry data = entry.getValue();
    // data.rank(), data.killCount(), data.maxDamageDealt() — геттеры record
    System.out.println("║  " + name + " (" + data.rank() + ")");
    System.out.println("║    Убито: " + data.killCount() + " | Макс. урон: " + data.maxDamageDealt());
}
```

**NavigableMap — методы навигации:**

```java
// Bestiary.java — display(), строки 347–377

// firstKey() — наименьший ключ (первый по алфавиту).
// Бросает NoSuchElementException если TreeMap пуст!
System.out.println("Первый по алфавиту: " + entries.firstKey());

// lastKey() — наибольший ключ (последний по алфавиту).
System.out.println("Последний по алфавиту: " + entries.lastKey());

// headMap(toKey) — представление (view!) ключей СТРОГО МЕНЬШЕ toKey.
// Это не копия данных! Изменения в headMap отразятся в entries.
NavigableMap<String, BestiaryEntry> firstHalf = entries.headMap("М");

// tailMap(fromKey) — ключи >= fromKey (включительно).
NavigableMap<String, BestiaryEntry> secondHalf = entries.tailMap("М");

// subMap(from, to) — ключи от from (включительно) до to (исключительно).
NavigableMap<String, BestiaryEntry> subset = entries.subMap("Г", "Д");

// Дополнительные методы:
// floorKey(k)   — наибольший ключ <= k (или null)
// ceilingKey(k) — наименьший ключ >= k (или null)
// lowerKey(k)   — наибольший ключ СТРОГО < k (или null)
// higherKey(k)  — наименьший ключ СТРОГО > k (или null)
```

**BestiaryEntry** — record внутри Bestiary:

```java
// Bestiary.java — строки 175+
// record внутри класса (статически вложенный по умолчанию).
// implements Serializable — для сохранения в файл.
public record BestiaryEntry(
    int killCount,       // сколько раз убит этот тип врага
    int maxDamageDealt,  // рекордный урон за один удар
    EnemyRank rank,      // ранг врага
    long firstEncounter  // время первой встречи (Unix epoch, мс)
) implements Serializable {

    // withKill() — «обновить» запись (record неизменяем, поэтому создаём новую).
    // Паттерн «wither-методы» для неизменяемых объектов.
    public BestiaryEntry withKill(int newDamage) {
        return new BestiaryEntry(
            killCount + 1,
            Math.max(maxDamageDealt, newDamage),
            rank,
            firstEncounter
        );
    }
}
```

---

## 12. Подглава 5.10 — Iterator и Iterable

**Концепция:**
- `Iterable<E>` — объект, по которому можно итерироваться (через for-each)
- `Iterator<E>` — объект, управляющий позицией обхода

**Связь:** for-each компилируется в вызовы `iterator().hasNext()` / `next()`.

```java
// for-each:
for (Inventory.Slot slot : inventory) { ... }

// Компилятор разворачивает в:
Iterator<Inventory.Slot> it = inventory.iterator();
while (it.hasNext()) {
    Inventory.Slot slot = it.next();
    ...
}
```

**Интерфейс Iterator:**
- `boolean hasNext()` — есть ли следующий элемент?
- `E next()` — вернуть следующий элемент и сдвинуть позицию
- `void remove()` — удалить последний возвращённый элемент (опционально)

### Применение 1: `Inventory` — пользовательский итератор

```java
// Inventory.java — класс, строка 80
// Inventory<T> implements Iterable<Inventory<T>.Slot>
// Реализация Iterable позволяет использовать for-each по инвентарю.
public class Inventory<T> implements Iterable<Inventory<T>.Slot>, Serializable {

    // iterator() — единственный метод Iterable (строка 575).
    // Возвращает новый объект InventoryIterator.
    @Override
    public Iterator<Slot> iterator() {
        return new InventoryIterator();
    }

    // InventoryIterator — inner class (нестатический вложенный класс, строка 602).
    // Имеет доступ к полям внешнего класса Inventory (slots, size) напрямую.
    // Если бы был static — пришлось бы передавать Inventory как параметр.
    private class InventoryIterator implements Iterator<Slot> {
        private int currentIndex = 0;

        @Override
        public boolean hasNext() {
            // Позиция ещё не вышла за границу заполненной части slots[].
            return currentIndex < size;
        }

        @Override
        public Slot next() {
            // Защита: если нет следующего — выбросить стандартное исключение.
            // NoSuchElementException — контракт Iterator, описан в Javadoc.
            if (!hasNext()) throw new NoSuchElementException("Нет больше элементов");

            // slots[currentIndex++] — постфиксный инкремент:
            // сначала берём элемент по currentIndex, затем currentIndex++.
            // Если бы написали ++currentIndex — пропустили бы slots[0]!
            return (Slot) slots[currentIndex++];
        }

        @Override
        public void remove() {
            // removeSlot(currentIndex - 1) — удаляем последний возвращённый элемент.
            // currentIndex уже был увеличен в next(), поэтому -1.
            removeSlot(currentIndex - 1);
            currentIndex--;  // скорректировать позицию
        }
    }
}
```

```java
// Game.java — handleUseItem(), for-each по инвентарю
// Благодаря Iterable можно писать простой for-each.
for (Inventory.Slot slot : inventory) {
    System.out.println(slot);
}
```

### Применение 2: `Bestiary` — делегирующий итератор

```java
// Bestiary.java — строка 114
// Bestiary implements Iterable<Map.Entry<String, Bestiary.BestiaryEntry>>
// Делегирует итерацию внутреннему TreeMap через entrySet().iterator().
public class Bestiary implements Iterable<Map.Entry<String, Bestiary.BestiaryEntry>>, Serializable {

    // iterator() — строка 440
    @Override
    public Iterator<Map.Entry<String, BestiaryEntry>> iterator() {
        // TreeMap.entrySet() возвращает Set<Map.Entry<K,V>>.
        // .iterator() возвращает итератор этого Set.
        // Обход в алфавитном порядке ключей (TreeMap гарантирует).
        return entries.entrySet().iterator();
    }
}

// Game.java — showFinalStats(), строка 2812
// for-each по Bestiary — работает благодаря Iterable.
// var — вывод типа (Java 10+): компилятор определяет Map.Entry<String, BestiaryEntry>.
for (var entry : bestiary) {
    System.out.println("║    " + entry.getKey() + ": " + entry.getValue().killCount() + " убийств");
}
```

**Способы перебора Map — сравнение:**

```java
// 1. entrySet() — самый распространённый, доступ и к ключу, и к значению.
for (Map.Entry<String, BestiaryEntry> e : entries.entrySet()) {
    System.out.println(e.getKey() + ": " + e.getValue().killCount());
}

// 2. keySet() — только ключи (если значение не нужно).
for (String name : entries.keySet()) {
    System.out.println(name);
}

// 3. values() — только значения.
for (BestiaryEntry data : entries.values()) {
    System.out.println(data.killCount());
}

// 4. forEach() — лямбда (Java 8+), самый краткий.
entries.forEach((name, data) -> System.out.println(name + ": " + data.killCount()));
```

---

## Карта подглав → файлы → строки

| Подглава | Концепция | Файл | Строки |
|---|---|---|---|
| 5.1 | Collection (объявление как интерфейс) | `Game.java` | 299–384 |
| 5.1 | Инициализация коллекций | `Game.java` | 474–480 |
| 5.2 | ArrayList: объявление, battleLog | `Game.java` | 299, 474, 1022, 1655 |
| 5.2 | ArrayList: enemies, add/get/sort | `Game.java` | 909–966 |
| 5.2 | ArrayList: for-each | `Game.java` | 1081–1083 |
| 5.3 | LinkedList: объявление, questLog | `Game.java` | 314, 475 |
| 5.3 | LinkedList: addFirst/addLast | `Game.java` | 1061–1068 |
| 5.4 | HashSet: объявление, achievements | `Game.java` | 342, 477 |
| 5.4 | HashSet: add() возвращает boolean | `Game.java` | 1420–1508 |
| 5.4 | enum для HashSet | `Achievement.java` | 31–62 |
| 5.5 | TreeSet: объявление, leaderboard | `Game.java` | 353, 478 |
| 5.5 | TreeSet: add, for-each (топ-5) | `Game.java` | 2846–2874 |
| 5.6 | Comparable: Enemy | `Enemy.java` | 43–72 |
| 5.6 | Comparable: BattleRecord | `BattleRecord.java` | 150–201 |
| 5.6 | Comparator: byName, byExpDesc | `Game.java` | 939–966 |
| 5.7 | PriorityQueue: инициатива | `Game.java` | 1704–1749 |
| 5.7 | ArrayDeque: стек отмены | `Game.java` | 373, 479, 1611 |
| 5.8 | HashMap: объявление, lootTable | `Game.java` | 331, 476 |
| 5.8 | HashMap: put/getOrDefault | `Game.java` | 806–823, 1350 |
| 5.8 | HashMap: containsKey/get/put | `Bestiary.java` | 270–285 |
| 5.8 | record LootDrop как значение Map | `LootDrop.java` | 35–63 |
| 5.9 | TreeMap: bестиарий | `Bestiary.java` | 248, 259–286 |
| 5.9 | NavigableMap: firstKey, headMap | `Bestiary.java` | 347–377 |
| 5.9 | entrySet + for-each | `Bestiary.java` | 321–332 |
| 5.9 | BestiaryEntry record + withKill() | `Bestiary.java` | 175–230 |
| 5.10 | Iterable + Iterator: Inventory | `Inventory.java` | 80, 575–720 |
| 5.10 | Iterable: Bestiary делегирует | `Bestiary.java` | 114, 440 |
| 5.10 | for-each по Bestiary | `Game.java` | 2812–2814 |

---

## Рекомендуемый порядок изучения файлов

1. `Achievement.java` — enum + комментарии про HashSet (5.4, 3.8)
2. `LootDrop.java` — record + compact constructor, значение для HashMap (5.8)
3. `BattleRecord.java` — record + Comparable, элемент TreeSet (5.5, 5.6)
4. `Bestiary.java` — TreeMap + NavigableMap + Iterable (5.9, 5.10)
5. `Inventory.java` — generics + inner class + Iterator (5.10)
6. `Enemy.java` — Comparable (5.6), Cloneable
7. `Game.java` — всё вместе: объявления полей → конструктор → `gameLoop()` → `battle()` → `handleLoot()` → `checkAchievements()` → `showFinalStats()`
