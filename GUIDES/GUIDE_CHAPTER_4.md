# Глава 4: Обработка исключений (4.1–4.4)

Эта глава посвящена механизму исключений в Java — способу сообщать об ошибках и реагировать на них.
В проекте RPG исключения используются для ожидаемых игровых ситуаций: переполнение инвентаря,
недостаток маны, невалидный ввод пользователя.

---

## Содержание

1. [Иерархия исключений в Java и в проекте](#1-иерархия-исключений)
2. [4.1 — try-catch-finally](#2-41--try-catch-finally)
3. [4.2 — multi-catch, throws, throw, try-with-resources](#3-42--multi-catch-throws-throw-try-with-resources)
4. [4.3 — Пользовательские исключения](#4-43--пользовательские-исключения)
5. [4.4 — assert](#5-44--assert)
6. [Сводная таблица](#6-сводная-таблица)

---

## 1. Иерархия исключений

### Иерархия Java (стандартная)

```
Throwable                        ← корень всех «бросаемых» объектов
├── Error                        ← фатальные ошибки JVM, ловить не нужно
│   ├── OutOfMemoryError
│   ├── StackOverflowError
│   └── AssertionError           ← бросается при провале assert (глава 4.4)
└── Exception                    ← ошибки, с которыми можно справиться
    ├── RuntimeException         ← UNCHECKED: компилятор не требует обработки
    │   ├── NullPointerException
    │   ├── NumberFormatException
    │   ├── ArrayIndexOutOfBoundsException
    │   ├── IndexOutOfBoundsException
    │   ├── IllegalArgumentException
    │   ├── IllegalStateException
    │   └── NoSuchElementException
    └── (все остальные)          ← CHECKED: компилятор ТРЕБУЕТ обработки
        ├── IOException
        │   └── FileNotFoundException
        ├── SQLException
        └── GameException        ← наше пользовательское checked-исключение
```

### Иерархия игровых исключений (проект RPG)

```
Exception                        ← стандартный checked
└── GameException                ← базовый класс для ВСЕХ игровых ошибок
    │   Файл: GameException.java
    │   Поля: message, cause (унаследованы от Exception)
    │
    ├── InsufficientResourceException   ← не хватает ресурса (мана, ярость)
    │       Файл: InsufficientResourceException.java
    │       Поля: resourceName, required, available
    │
    ├── InvalidActionException          ← некорректное действие
    │       Файл: InvalidActionException.java
    │       Поля: actionName, reason
    │
    └── InventoryFullException          ← инвентарь переполнен
            Файл: InventoryFullException.java
            Поля: currentSize, maxCapacity
```

**Ключевой принцип иерархии:** `catch (GameException e)` перехватит все три подкласса разом.
`catch (InventoryFullException e)` перехватит только переполнение инвентаря.
Порядок catch-блоков — от конкретного к общему (иначе ошибка компиляции).

---

## 2. 4.1 — try-catch-finally

### Базовая структура

```java
try {
    // Код, который может бросить исключение.
    // При исключении выполнение ПРЕРЫВАЕТСЯ и переходит в catch.
} catch (ТипИсключения e) {
    // Обработка ошибки.
    // e.getMessage() — текстовое описание.
    // e.getCause()   — исходное исключение (если оборачивали).
} finally {
    // Выполняется ВСЕГДА: и при успехе, и при ошибке.
    // Даже если в catch есть return!
}
```

### try-catch — перехват checked exception

**Файл:** `Game.java`, метод `setupInventory()`, строки 768–785

```java
// Inventory.addItem() объявляет throws InventoryFullException (checked).
// Компилятор ТРЕБУЕТ обработки — без try-catch файл не скомпилируется.
try {
    inventory.addItem(new Inventory.ItemInfo("Зелье здоровья", 30), 3);
    inventory.addItem(new Inventory.ItemInfo("Малое зелье здоровья", 5), 1);
} catch (InventoryFullException e) {
    // Ситуация маловероятна при инициализации, но компилятор не знает этого.
    System.out.println("Предупреждение: " + e.getMessage());
}
```

Checked exception — это «обещание» метода вызывающему коду: «я могу не справиться,
будь готов». Компилятор принудительно проверяет это обещание.

### finally — гарантированное выполнение

**Файл:** `Game.java`, метод `handleUseItem()`, строки 2434–2449

```java
try {
    // ... работа с предметом ...
} catch (IndexOutOfBoundsException | IllegalStateException e) {
    System.out.println("Ошибка: " + e.getMessage());
    return false;   // ← даже здесь finally выполнится!
} finally {
    // Выполняется ВСЕГДА:
    //   - try завершился успешно → finally → продолжение
    //   - catch выполнен → finally → return из catch
    //   - В catch есть return → finally выполнится ПЕРЕД возвратом
    System.out.println("[Действие] Ход с предметом завершён.");
}
```

**Порядок выполнения при return в catch:**
1. Исключение → переход в catch
2. catch: выполняется код, Java **запоминает** `return false`
3. finally: выполняется код блока
4. Java **возвращает** запомненное значение `false`

**Ловушка:** никогда не пишите `return` в `finally` — он перебьёт `return` из `try`/`catch`.

**Файл:** `Game.java`, метод `readInt()`, строки 2915–2966

```java
private int readInt() {
    try {
        return Integer.parseInt(scanner.nextLine().trim());
    } catch (NumberFormatException | NoSuchElementException e) {
        return -1;  // маркер ошибки, обрабатывается в switch как default
    } finally {
        // Пустой finally — для демонстрации конструкции.
        // Типичное применение: lock.unlock(), connection.close(), логирование.
    }
}
```

---

## 3. 4.2 — multi-catch, throws, throw, try-with-resources

### throws — объявление в сигнатуре метода

**Файл:** `Game.java`, метод `readHeroName()`, строка 738

```java
// throws InvalidActionException — объявляет: этот метод МОЖЕТ бросить исключение.
// Компилятор заставит вызывающий код обработать его (try-catch) или пробросить дальше.
private String readHeroName() throws InvalidActionException {
    String name = scanner.nextLine().trim();

    if (name.isEmpty()) {
        // throw — бросаем исключение. Выполнение метода ПРЕКРАЩАЕТСЯ здесь.
        // Управление передаётся ближайшему catch в цепочке вызовов.
        throw new InvalidActionException("ввод имени", "имя не может быть пустым");
    }

    if (name.length() > 20) {
        throw new InvalidActionException("ввод имени",
                "имя слишком длинное (" + name.length() + " символов, максимум 20)");
    }

    if (!name.matches("[a-zA-Zа-яА-ЯёЁ\\s-]+")) {
        throw new InvalidActionException("ввод имени",
                "имя содержит недопустимые символы");
    }

    return name;
}
```

**Файл:** `Inventory.java`, метод `addItem()`, строка 354

```java
public void addItem(T item, int quantity) throws InventoryFullException {
    if (items.size() >= maxCapacity) {
        throw new InventoryFullException(items.size(), maxCapacity);
    }
    // ...
}
```

Разделение ответственности: `readHeroName()` обнаруживает ошибку и бросает исключение.
`startGame()` — вызывающий метод — знает, что делать (попросить ввести снова).

### throw — бросание исключения

Ключевое слово `throw` используется непосредственно перед объектом исключения.
После `throw` выполнение текущего метода прекращается немедленно.

```java
// Синтаксис: throw new ТипИсключения(аргументы);
throw new InvalidActionException("ввод имени", "имя не может быть пустым");
throw new InventoryFullException(items.size(), maxCapacity);
throw new InsufficientResourceException("мана", 20, mage.getMana());
```

Отличие от `throws`: `throws` — в сигнатуре (объявление), `throw` — в теле (действие).

### multi-catch — перехват нескольких типов

**Файл:** `Game.java`, метод `handleUseItem()`, строки 2425–2432

```java
// MULTI-CATCH (Java 7+): один catch для нескольких типов исключений.
// Применяется когда обработка одинакова для разных типов.
//
// Без multi-catch пришлось бы дублировать код:
//   catch (IndexOutOfBoundsException e) { ... }
//   catch (IllegalStateException e) { ... }
//
// ВАЖНО: типы в multi-catch НЕ должны быть связаны наследованием.
// catch (Exception | IOException e) — ОШИБКА: IOException наследует Exception.
} catch (IndexOutOfBoundsException | IllegalStateException e) {
    System.out.println("Ошибка при использовании предмета: " + e.getMessage());
    battleLog.add("Ошибка: " + e.getMessage());
    return false;
}
```

**Файл:** `Game.java`, метод `readInt()`, строки 2938–2941

```java
// NumberFormatException — ввёл не число ("abc", "1.5").
// NoSuchElementException — Scanner не может прочитать (EOF, закрытый stdin).
// Оба unchecked, оба обрабатываются одинаково → multi-catch.
} catch (NumberFormatException | NoSuchElementException e) {
    return -1;
}
```

### try-with-resources — автоматическое закрытие

**Файл:** `Game.java`, метод `loadEnemyData()`, строки 854–885

```java
// Без try-with-resources нужно было писать так:
//   Scanner fileScanner = null;
//   try {
//       fileScanner = new Scanner(dataFile);
//       // ...
//   } finally {
//       if (fileScanner != null) fileScanner.close();  // вручную!
//   }
//
// С try-with-resources — Scanner закроется АВТОМАТИЧЕСКИ:
//   - при нормальном выходе из try
//   - при исключении (перед catch)
//   - при return или break внутри try
//
// Требование: ресурс должен реализовывать AutoCloseable (метод close()).
// Scanner, FileInputStream, Connection, BufferedReader — все реализуют AutoCloseable.
try (Scanner fileScanner = new Scanner(dataFile)) {
    while (fileScanner.hasNextLine()) {
        String line = fileScanner.nextLine();
        System.out.println("  Загружено: " + line);
    }
} catch (FileNotFoundException e) {
    // FileNotFoundException — checked (наследник IOException).
    // Файл данных необязателен — используем встроенные данные.
    System.out.println("[Данные] Файл данных не найден. Используются встроенные данные.");
}
// fileScanner.close() вызван АВТОМАТИЧЕСКИ — не нужно писать finally!
```

**Важно:** `this.scanner` (чтение из `System.in`) не заворачивается в try-with-resources —
закрытие `System.in` сделало бы невозможным любой ввод до конца работы программы.

### Checked vs Unchecked — как выбрать

| Критерий | Checked (extends Exception) | Unchecked (extends RuntimeException) |
|---|---|---|
| Кто виноват? | Внешние обстоятельства (файл, ввод) | Баг в коде (null, выход за границу) |
| Может предвидеть вызывающий? | Да | Нет (или не должен) |
| Компилятор требует обработки? | Да | Нет |
| Примеры в проекте | GameException и его подклассы | NullPointerException, IndexOutOfBoundsException |
| Наши игровые примеры | Инвентарь полон, мало маны, плохой ввод | Нет (только ловим стандартные) |

---

## 4. 4.3 — Пользовательские исключения

### Зачем создавать свои исключения?

1. **Точное описание** — `InventoryFullException` вместо `RuntimeException("полон")`.
2. **Дополнительные данные** — поля с контекстом ошибки, доступные через геттеры.
3. **Полиморфный перехват** — `catch (GameException e)` ловит все игровые ошибки разом.
4. **Разделение ответственности** — код, обнаружившый ошибку, не обязан знать, как её обработать.

### GameException — базовый класс

**Файл:** `GameException.java`, строки 86–233

```java
// extends Exception — делает исключение CHECKED.
// Если бы написали extends RuntimeException — получили бы UNCHECKED.
public class GameException extends Exception {

    // По конвенции Java — до 4 конструкторов:
    //   1. (String message)                  — только текст
    //   2. (String message, Throwable cause) — текст + причина (exception chaining)
    //   3. (Throwable cause)                 — только причина
    //   4. ()                                — без параметров (редко нужен)

    public GameException(String message) {
        // super() передаёт message родителю (Exception).
        // Без super(message) → getMessage() вернёт null!
        super(message);
    }

    public GameException(String message, Throwable cause) {
        // Exception chaining: одно исключение вызвано другим.
        // cause доступна через getCause().
        // В стектрейсе видна строка "Caused by: java.io.IOException: ..."
        super(message, cause);
    }

    public GameException(Throwable cause) {
        // getMessage() вернёт cause.toString() автоматически.
        super(cause);
    }
}
```

### InsufficientResourceException — не хватает ресурса

**Файл:** `InsufficientResourceException.java`, строки 47–191

```java
// extends GameException — второй уровень иерархии.
// Полиморфизм: catch (GameException e) поймает и этот тип тоже.
public class InsufficientResourceException extends GameException {

    // Поля final — данные об ошибке не изменятся после создания (thread-safe).
    // int, а не Integer — примитив не может быть null.
    private final String resourceName;  // "мана", "ярость", "стрелы"
    private final int required;         // сколько нужно для действия
    private final int available;        // сколько есть в наличии

    public InsufficientResourceException(String resourceName, int required, int available) {
        // Сообщение строится автоматически из данных — единый формат, нет опечаток.
        super("Недостаточно ресурса '" + resourceName
                + "': требуется " + required + ", доступно " + available);
        this.resourceName = resourceName;
        this.required = required;
        this.available = available;
    }

    // Геттеры — доступ к данным без парсинга строки getMessage().
    public String getResourceName() { return resourceName; }
    public int getRequired()        { return required; }
    public int getAvailable()       { return available; }
}
```

**Применение в Game.java** (правильный паттерн — throw и catch в разных методах):

```java
// Метод A (глубоко в логике): обнаруживает ошибку и бросает
private void castSpell(Mage mage, int manaCost) throws InsufficientResourceException {
    if (mage.getMana() < manaCost) {
        throw new InsufficientResourceException("мана", manaCost, mage.getMana());
    }
    // ...
}

// Метод B (вызывающий): знает, что делать с ошибкой
try {
    castSpell(mage, 20);
} catch (InsufficientResourceException e) {
    System.out.println(e.getMessage());              // "Недостаточно ресурса 'мана': ..."
    System.out.println("Ресурс: " + e.getResourceName()); // "мана"
    System.out.println("Нужно: " + e.getRequired());       // 20
    System.out.println("Есть: " + e.getAvailable());       // 5
}
```

### InvalidActionException — некорректное действие

**Файл:** `InvalidActionException.java`, строки 62–157

```java
public class InvalidActionException extends GameException {

    private final String actionName;  // "ввод имени", "атака", "смена оружия"
    private final String reason;      // "имя пустое", "враг мёртв"

    public InvalidActionException(String actionName, String reason) {
        super("Невалидное действие '" + actionName + "': " + reason);
        this.actionName = actionName;
        this.reason = reason;
    }

    public String getActionName() { return actionName; }
    public String getReason()     { return reason; }
}
```

**Применение — валидация ввода с повтором:**

```java
// readHeroName() бросает, startGame() ловит и повторяет запрос.
String name = null;
while (name == null) {
    try {
        name = readHeroName();  // throws InvalidActionException
    } catch (InvalidActionException e) {
        System.out.println("Ошибка: " + e.getMessage());
        // Цикл продолжается — пользователь вводит снова.
    }
}
```

### InventoryFullException — инвентарь переполнен

**Файл:** `InventoryFullException.java`, строки 72–204

```java
public class InventoryFullException extends GameException {

    private final int currentSize;  // предметов сейчас
    private final int maxCapacity;  // максимальная вместимость

    public InventoryFullException(int currentSize, int maxCapacity) {
        // "Инвентарь полон: 5/5 слотов занято" — формат понятен игроку.
        super("Инвентарь полон: " + currentSize + "/" + maxCapacity + " слотов занято");
        this.currentSize = currentSize;
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentSize() { return currentSize; }
    public int getMaxCapacity() { return maxCapacity; }
}
```

**Применение при сборе лута** (`Game.java`, метод `handleLoot()`):

```java
try {
    inventory.addItem(lootItem, lootCount);
    System.out.println("Добавлено в инвентарь!");
} catch (InventoryFullException e) {
    // Предмет утерян — но игра продолжается. Это не фатальная ошибка.
    System.out.printf("Инвентарь полон (%d/%d). Предмет утерян.%n",
            e.getCurrentSize(), e.getMaxCapacity());
}
```

### Исключение vs boolean: когда что выбрать

| Ситуация | boolean | Exception |
|---|---|---|
| Поиск в коллекции (нормальный исход) | `list.contains(x)` | — |
| Файл не найден (ожидаемая проблема) | — | `FileNotFoundException` |
| Инвентарь полон (ожидаемая проблема) | — | `InventoryFullException` |
| Нельзя проигнорировать | — | Checked exception |
| Нужны детали ошибки | — | Exception с полями |

Checked exception нельзя проигнорировать — компилятор заставит написать `try-catch`.
С `boolean` легко забыть проверить возвращённое значение.

### Порядок catch-блоков (критически важно)

```java
// ПРАВИЛЬНО: сначала конкретные, потом общие
try { ... }
catch (InventoryFullException e) { ... }    // сначала конкретный подкласс
catch (GameException e) { ... }             // потом общий (ловит остальных потомков)
catch (Exception e) { ... }                // потом ещё более общий

// ОШИБКА КОМПИЛЯЦИИ: GameException перехватит InventoryFullException раньше,
// до него не дойдёт никогда → компилятор запрещает.
catch (GameException e) { ... }
catch (InventoryFullException e) { ... }   // ОШИБКА: "already been caught"
```

---

## 5. 4.4 — assert

### Синтаксис и назначение

**Файл:** `GameCharacter.java`, строки 189–222

```java
// assert — проверка условия, которое ВСЕГДА должно быть истинным.
// Если условие false → JVM бросает AssertionError (наследник Error, не Exception).
//
// Синтаксис 1: assert условие;
// Синтаксис 2: assert условие : "сообщение";  ← предпочтительный, с пояснением
//
// КРИТИЧЕСКИ ВАЖНО: assert ОТКЛЮЧЁН по умолчанию!
// Для включения: java -ea rpg.Main        (-ea = enable assertions)
//                java -ea:rpg... rpg.Main  (только для пакета rpg)

assert maxHealth > 0  : "Здоровье должно быть положительным: " + maxHealth;
assert attack >= 0    : "Атака не может быть отрицательной: " + attack;
assert defense >= 0   : "Защита не может быть отрицательной: " + defense;
```

**Файл:** `Game.java`, метод `battle()`, строка 1633

```java
// assert для проверки переходов конечного автомата.
// Если вызвать battle() в неподходящем состоянии — разработчик немедленно узнает.
// В продакшене assert отключён — накладных расходов нет.
assert gameState.canTransitionTo(GameState.BATTLE)
        : "Недопустимый переход в состояние BATTLE из " + gameState;
```

**Файл:** `GameCharacter.java`, строка 796

```java
// assert в private методе — защита от вызова с пустым массивом.
// private метод контролируется только нашим классом → assert уместен.
assert characters != null && characters.length > 0
        : "Массив персонажей не должен быть пустым";
```

### Когда использовать assert, а когда if + throw

| | assert | if + throw IllegalArgumentException |
|---|---|---|
| Где уместен | private/package методы, инварианты | public API, проверка аргументов извне |
| Работает в продакшене? | Только при `-ea` (обычно отключён) | Всегда |
| Что бросает при провале | `AssertionError` (ловить не надо) | `IllegalArgumentException` (RuntimeException) |
| Цель | Поймать баги при разработке | Защитить от неверного использования |

```java
// ПРАВИЛЬНО: public конструктор → if + throw
public GameCharacter(String name, int maxHealth, int attack, int defense) {
    if (name == null) {
        throw new IllegalArgumentException("Имя персонажа не может быть null");
    }
    // assert — для внутренних проверок (числовые параметры), НЕ для null name
    assert maxHealth > 0 : "Здоровье должно быть положительным: " + maxHealth;
}

// НЕПРАВИЛЬНО: assert в public методе — в продакшене не сработает
public void setHealth(int hp) {
    assert hp >= 0 : "hp не может быть отрицательным";  // может быть отключён!
    // Лучше: if (hp < 0) throw new IllegalArgumentException("hp >= 0");
}

// НЕПРАВИЛЬНО: assert с побочным эффектом
assert list.remove(element);  // НЕ ДЕЛАТЬ! При отключённом assert remove() не вызовется
```

### Запуск с assert

```
java -ea rpg.Main               # включить assertions для всего
java -ea:rpg... rpg.Main        # включить только для пакета rpg
java rpg.Main                   # assert отключены (поведение по умолчанию)
```

---

## 6. Сводная таблица

| Подглава | Концепция | Файл | Строки |
|---|---|---|---|
| 4.1 | try-catch базовый | `Game.java` → `setupInventory()` | 768–785 |
| 4.1 | finally | `Game.java` → `handleUseItem()` | 2434–2449 |
| 4.1 | finally с return | `Game.java` → `readInt()` | 2942–2966 |
| 4.2 | throws в сигнатуре | `Game.java` → `readHeroName()` | 738 |
| 4.2 | throw (бросание) | `Game.java` → `readHeroName()` | 744–760 |
| 4.2 | multi-catch | `Game.java` → `handleUseItem()` | 2425–2432 |
| 4.2 | multi-catch | `Game.java` → `readInt()` | 2938–2941 |
| 4.2 | try-with-resources | `Game.java` → `loadEnemyData()` | 868–884 |
| 4.2 | checked vs unchecked | `GameException.java` | 12–72 |
| 4.3 | базовый класс исключения | `GameException.java` | 86–233 |
| 4.3 | поля в исключении, геттеры | `InsufficientResourceException.java` | 47–191 |
| 4.3 | иерархия исключений | `InvalidActionException.java` | 62–157 |
| 4.3 | exception chaining (cause) | `InventoryFullException.java` | 100–134 |
| 4.3 | порядок catch-блоков | `GameException.java` | 219–232 |
| 4.3 | исключение vs boolean | `InventoryFullException.java` | 15–39 |
| 4.4 | assert — синтаксис | `GameCharacter.java` | 189–222 |
| 4.4 | assert — инвариант | `Game.java` → `battle()` | 1623–1633 |
| 4.4 | assert vs if+throw | `GameCharacter.java` | 172–215 |

---

## Рекомендуемый порядок изучения файлов

1. `GameException.java` — иерархия, checked vs unchecked, конструкторы
2. `InsufficientResourceException.java` — поля, геттеры, exception chaining
3. `InvalidActionException.java` — второй пример с полями, разделение ответственности
4. `InventoryFullException.java` — третий пример, исключение vs boolean, итоговые советы
5. `Game.java` → `loadEnemyData()` — try-with-resources на практике
6. `Game.java` → `readHeroName()` — throw + throws на практике
7. `Game.java` → `setupInventory()` — простой try-catch для checked
8. `Game.java` → `handleUseItem()` — multi-catch + finally вместе
9. `Game.java` → `readInt()` — multi-catch + пустой finally (учебный пример)
10. `GameCharacter.java` (конструктор) — assert, когда можно и нельзя
11. `Game.java` → `battle()` — assert для инварианта конечного автомата
