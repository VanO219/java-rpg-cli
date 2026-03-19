# Глава 3: Классы и ООП (3.1–3.32)

Эта глава — самая большая в курсе. Она охватывает объектно-ориентированное программирование
от базовых классов до продвинутых дженериков, sealed-иерархий и паттерн-матчинга.

Код проекта демонстрирует все 32 подглавы на примере консольной RPG-игры.

---

## Навигация по разделам

| Раздел | Темы |
|--------|------|
| [Базовые классы (3.1–3.7)](#раздел-1-базовые-классы-31–37) | Класс, пакет, конструктор, модификаторы, static, final, перегрузка |
| [Перечисления (3.8)](#раздел-2-перечисления-enum-38) | enum с полями, методами, абстрактными методами, конечный автомат |
| [Наследование (3.9–3.12)](#раздел-3-наследование-39–312) | extends, abstract, super, @Override |
| [Методы Object (3.10)](#раздел-4-методы-object-310) | toString, equals, hashCode |
| [instanceof и ссылочные типы (3.13–3.14)](#раздел-5-instanceof-и-ссылочные-типы-313–314) | instanceof, Cloneable, передача объектов |
| [Обобщения (3.15, 3.17, 3.31, 3.32)](#раздел-6-обобщения-generics-315-317-331-332) | Generics, bounded, wildcards, type erasure |
| [Callback (3.16)](#раздел-7-callback-316) | Интерфейс-слушатель, паттерн Observer |
| [Анонимные классы (3.18)](#раздел-7а-анонимные-классы-318) | anonymous class, реализация интерфейса на месте |
| [Records (3.19)](#раздел-8-records-319) | record, компактный конструктор |
| [Вывод типов var (3.20)](#раздел-8а-вывод-типов-var-320) | var, local variable type inference |
| [Блоки инициализации и цепочка конструкторов (3.21)](#раздел-9-блоки-инициализации-и-цепочка-конструкторов-321) | static {}, instance {}, this() |
| [Инкапсуляция (3.22)](#раздел-10-инкапсуляция-322) | private, геттеры, сеттеры |
| [final класс и метод (3.23)](#раздел-11-final-класс-и-метод-323) | final class, final method |
| [Полиморфизм (3.24)](#раздел-12-полиморфизм-324) | Приведение типов, upcasting, downcasting |
| [Sealed-классы (3.25)](#раздел-13-sealed-классы-325) | sealed interface, permits |
| [Pattern matching (3.26–3.27)](#раздел-14-pattern-matching-326–327) | instanceof с переменной, record patterns |
| [Интерфейсы и полиморфизм (3.28–3.29)](#раздел-15-интерфейсы-и-полиморфизм-328–329) | default/static методы, реализация нескольких интерфейсов |
| [Вложенные и внутренние классы (3.30)](#раздел-16-вложенные-и-внутренние-классы-330) | inner class, static nested class, local class |

---

## Раздел 1: Базовые классы (3.1–3.7)

### 3.1 — Классы и объекты

**Концепция:** Класс — шаблон объекта. Объект — экземпляр класса (данные + поведение).

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 75 | `public abstract class GameCharacter` — объявление класса |
| `Enemy.java` | строка 112 | `public class Enemy` — конкретный класс |
| `Warrior.java` | строка 77 | `new Warrior(name)` — создание объекта через `new` |

**Фрагмент кода — объявление класса с полями:**
```java
// Enemy.java, строки 208–230
// Поля класса — данные, которые хранит каждый объект
private String name;     // имя врага (у каждого объекта своё)
private int health;      // текущее здоровье
private int attack;      // сила атаки
private int expReward;   // награда опытом при победе
```

---

### 3.2 — Пакеты (package)

**Концепция:** Пакет — пространство имён для группировки классов. Соответствует папке на диске.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| Все файлы | строка 1–4 | `package rpg;` — объявление пакета |
| `DamageType.java` | строки 14–16 | Комментарий с объяснением зачем нужен пакет |

**Фрагмент кода:**
```java
// DamageType.java, строка 14–17
// Пакет (package) — группирует связанные классы в одно пространство имён.
// Имя пакета соответствует структуре папок: rpg → папка src/rpg/.
// Все классы в одном пакете могут обращаться друг к другу без импортов.
package rpg;
```

---

### 3.3 — Модификаторы доступа (access modifiers)

**Концепция:** Управляют видимостью полей и методов. Четыре уровня от `private` до `public`.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 105–133 | Сравнение `private` и `protected` с объяснением |
| `Inventory.java` | строка 131 | `public class Slot` — вложенный класс, доступный снаружи |
| `Inventory.java` | строка 602 | `private class InventoryIterator` — вложенный класс, скрытый от внешнего мира |
| `Enemy.java` | строки 573–589 | Доступ к `private`-полю другого объекта того же класса |

**Таблица модификаторов (из комментария GameCharacter.java, строки 112–116):**

| Модификатор | Видимость |
|-------------|-----------|
| `private` | только этот класс |
| *(нет)* | package-private: классы в том же пакете |
| `protected` | класс + наследники + пакет |
| `public` | доступно отовсюду |

**Фрагмент кода — protected для наследников:**
```java
// GameCharacter.java, строки 135–146
// protected — поле видно в этом классе, в наследниках (Warrior, Mage, Archer)
// и в других классах того же пакета.
protected int level;
protected int experience;
```

---

### 3.4 — Конструкторы

**Концепция:** Метод инициализации объекта. Вызывается при `new`. Имя совпадает с именем класса.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 160–169 | Объявление конструктора, параметры |
| `Warrior.java` | строки 65–92 | Конструктор наследника, вызов `super(...)` |
| `Enemy.java` | строки 310–390 | Два конструктора (перегрузка) |

**Фрагмент кода — конструктор наследника:**
```java
// Warrior.java, строки 77–92
public Warrior(String name) {
    // super() — вызов конструктора родительского класса (GameCharacter).
    // ОБЯЗАТЕЛЬНОЕ ПРАВИЛО: super() должен быть ПЕРВОЙ строкой конструктора наследника.
    super(name, 120, 15, 10);
}
```

---

### 3.5 — Статические поля и методы (static)

**Концепция:** `static` — принадлежит классу, а не объекту. Одно на все экземпляры.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 148–158 | `private static int characterCount` — счётчик объектов |
| `GameCharacter.java` | строка 587 | `public static int getCharacterCount()` — статический геттер |
| `Enemy.java` | строки 136–144 | `private static int totalEnemiesCreated` — статический счётчик |
| `Attackable.java` | строка 89 | `static int calculateCritDamage(int)` — статический метод в интерфейсе |
| `Game.java` | строки 175–187 | `static final` константы: `MAX_ENEMIES`, `GAME_VERSION` |

**Фрагмент кода:**
```java
// GameCharacter.java, строки 148–158
// static-поле — одно на ВСЕ объекты класса. Это общая переменная.
// У каждого Warrior/Mage/Archer есть своё поле name, health и т.д.,
// но characterCount — один на всех. Он считает, сколько всего персонажей создано.
//
// Обращение: GameCharacter.getCharacterCount() — через имя класса, а не объекта.
private static int characterCount = 0;
```

---

### 3.6 — Перегрузка методов (method overloading)

**Концепция:** Несколько методов с одним именем, но разными параметрами.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 319, 352, 363 | Три варианта `performAttack()`: без параметров, с `int`, с `double` |
| `Enemy.java` | строки 310, 328 | Два конструктора `Enemy(4 параметра)` и `Enemy(5 параметров)` |

**Фрагмент кода:**
```java
// GameCharacter.java, строки 319–364
// Три перегруженных метода performAttack:
public DamageType performAttack()               { ... } // базовая атака
public DamageType performAttack(int bonus)      { ... } // с бонусом
public DamageType performAttack(double mult)    { ... } // с множителем
```

---

### 3.7 — Ключевое слово this

**Концепция:** Ссылка на текущий объект. Разрешает конфликт имён параметра и поля.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameState.java` | строки 128–131 | `this.displayName = displayName` — разделение поля и параметра |
| `GameCharacter.java` | строка 323 | `this.attack` — явный доступ к полю класса |

---

## Раздел 2: Перечисления enum (3.8)

**Концепция:** `enum` — тип с фиксированным набором констант. Каждая константа — объект. Может иметь поля, методы, конструктор.

### Три варианта enum в проекте:

| Файл | Строки | Особенность |
|------|--------|-------------|
| `EnemyRank.java` | 34–200 | Простой enum с полями и методами |
| `GameState.java` | 64–220 | Enum + конечный автомат (switch-выражение) |
| `ItemType.java` | 35–192 | Enum с **абстрактным методом** (каждая константа имеет своё поведение) |

---

### EnemyRank.java — базовый enum с полями

```java
// EnemyRank.java, строки 34–110
public enum EnemyRank {
    COMMON("Обычный", 1.0, ""),   // обычный враг, множитель x1
    ELITE("Элитный",  1.5, "★"),  // элитный враг, множитель x1.5
    BOSS("Босс",      2.5, "☠");  // босс,         множитель x2.5

    private final String displayName;
    private final double multiplier;  // умножается на базовые характеристики
    private final String icon;

    // Конструктор enum — всегда private (нельзя вызвать снаружи)
    EnemyRank(String displayName, double multiplier, String icon) { ... }

    public int applyStat(int baseStat) { ... }  // baseStat * multiplier
}
```

---

### GameState.java — enum как конечный автомат (state machine)

```java
// GameState.java, строки 64–220
public enum GameState {
    MENU("Главное меню", false),
    EXPLORING("Исследование", true),
    BATTLE("Бой", true),
    GAME_OVER("Игра окончена", false);

    // Метод enum — логика допустимых переходов между состояниями
    public boolean canTransitionTo(GameState next) {
        return switch (this) {            // switch с enum: не нужен default!
            case MENU      -> next == EXPLORING;
            case EXPLORING -> next == BATTLE || next == GAME_OVER;
            case BATTLE    -> next == EXPLORING || next == GAME_OVER;
            case GAME_OVER -> next == MENU;
        };
    }
}
```

Применение в бою:
```java
// Game.java, строка 1633
assert gameState.canTransitionTo(GameState.BATTLE)
    : "Недопустимый переход в состояние BATTLE из " + gameState;
```

---

### ItemType.java — enum с абстрактным методом

```java
// ItemType.java, строки 35–130
public enum ItemType {
    POTION("Зелье") {
        @Override
        public String describe() { return "Восстанавливает здоровье или ману"; }
    },
    WEAPON("Оружие") {
        @Override
        public String describe() { return "Увеличивает силу атаки"; }
    };
    // ...

    // Абстрактный метод — КАЖДАЯ константа обязана реализовать его
    public abstract String describe();

    // Встроенные методы enum: values(), valueOf(), ordinal(), name()
    public static ItemType fromDisplayName(String name) {
        for (ItemType type : values()) { // values() — все константы
            if (type.displayName.equals(name)) return type;
        }
        throw new IllegalArgumentException("Неизвестный тип предмета: " + name);
    }
}
```

**Ключевые особенности enum (из комментариев в коде):**
- `values()` — статический метод, возвращает массив всех констант
- `ordinal()` — номер константы (0, 1, 2...). **Не используй для БД!**
- `name()` — строковое имя константы (`"POTION"`)
- `valueOf("POTION")` — поиск константы по имени

---

## Раздел 3: Наследование (3.9–3.12)

### 3.9 — Наследование (extends)

**Концепция:** Наследник получает все поля и методы родителя. Добавляет свои.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 75 | `abstract class GameCharacter` — родительский абстрактный класс |
| `Warrior.java` | строка 38 | `class Warrior extends GameCharacter` — наследник |
| `Mage.java` | строка 14 | `class Mage extends GameCharacter` — наследник |
| `Archer.java` | строка 15 | `class Archer extends GameCharacter` — наследник |

**Иерархия наследования:**
```
Object
  └── GameCharacter  (abstract, implements Attackable, Healable, Serializable)
        ├── Warrior   (rage-механика)
        ├── Mage      (mana-механика)
        └── Archer    (crit-механика)
```

---

### 3.10 — Абстрактные классы (abstract)

**Концепция:** Класс, который нельзя создать напрямую. Содержит абстрактные методы без тела — наследники обязаны их реализовать.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 75 | `public abstract class GameCharacter` |
| `GameCharacter.java` | строка 278 | `public abstract String getClassName()` — абстрактный метод |
| `GameCharacter.java` | строка 295 | `public abstract DamageType specialAttack()` — абстрактный метод |
| `Warrior.java` | строка 106 | Реализация `getClassName()` |
| `Warrior.java` | строка 121 | Реализация `specialAttack()` |

**Зачем абстрактный класс, а не интерфейс?**
- `GameCharacter` содержит **общий код** (поля `name`, `health`, метод `takeDamage`)
- Интерфейс не может хранить состояние (поля)
- Абстрактный класс = общий скелет + обязательный контракт для наследников

---

### 3.11 — super и вызов родительского конструктора

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Warrior.java` | строка 89 | `super(name, 120, 15, 10)` — первой строкой конструктора |
| `Mage.java` | строка 56+ | `super(name, 80, 20, 8)` — маг: меньше HP, больше атаки |
| `Archer.java` | строка 66+ | `super(name, 90, 12, 6)` — лучник: средние показатели |
| `Mage.java` | строка 145 | `@Override toString()` — переопределение с вызовом `super.toString()` |

---

### 3.12 — @Override

**Концепция:** Аннотация-маркер. Говорит компилятору: «этот метод переопределяет метод родителя». Защищает от опечаток.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 297–319 | Подробное объяснение `@Override` |
| `Warrior.java` | строка 106 | `@Override getClassName()` |
| `Enemy.java` | строки 530, 654, 711, 740, 792 | Переопределение `clone`, `compareTo`, `equals`, `hashCode`, `toString` |

---

## Раздел 4: Методы Object (3.10)

Каждый Java-класс неявно наследует `java.lang.Object`. Три самых важных метода для переопределения: `toString()`, `equals()`, `hashCode()`.

### toString()

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 612 | Переопределение `toString()` — полная статистика персонажа |
| `Mage.java` | строка 145 | Переопределение `toString()` с `super.toString()` для добавления маны |
| `Enemy.java` | строка 792 | `toString()` для врага |
| `Inventory.java` | строка 201 | `toString()` внутреннего класса `Slot` |

---

### equals() и hashCode()

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 676–731 | `equals()` + `hashCode()` с подробными объяснениями |
| `Enemy.java` | строки 711–748 | `equals()` + `hashCode()` для `Enemy` |

**Главное правило (из комментария GameCharacter.java, строки 712–714):**
> Если два объекта `equals() == true`, их `hashCode()` ОБЯЗАН быть одинаковым.
> Нарушение → `HashMap`/`HashSet` работают некорректно!

**Фрагмент кода — equals() с pattern matching:**
```java
// GameCharacter.java, строки 676–701
@Override
public boolean equals(Object obj) {
    if (this == obj) return true;  // одинаковые ссылки = точно равны
    // instanceof с pattern variable (Java 16+): проверка типа + приведение в одну строку
    if (!(obj instanceof GameCharacter other)) return false;
    // Objects.equals() — null-безопасное сравнение
    return Objects.equals(name, other.name)
        && Objects.equals(getClassName(), other.getClassName());
}

@Override
public int hashCode() {
    // Objects.hash() — удобный метод для хеша из нескольких полей
    return Objects.hash(name, getClassName());
}
```

---

## Раздел 5: instanceof и ссылочные типы (3.13–3.14)

### 3.13 — Ссылочные типы и клонирование (Cloneable)

**Концепция:** Объекты передаются по ссылке. Клонирование создаёт независимую копию.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Enemy.java` | строка 112 | `implements Cloneable` — разрешает клонирование |
| `Enemy.java` | строки 530–547 | Метод `clone()` через `super.clone()` |
| `Enemy.java` | строки 573–589 | `createVariant()` — фабричный метод, использующий `baseHealth`/`baseAttack` |

**Поверхностное клонирование (shallow copy):**
```java
// Enemy.java, строки 530–546
@Override
public Enemy clone() {
    try {
        // super.clone() — поверхностная копия: новая память, скопированные поля.
        // (Enemy) — приведение типа: Object → Enemy (ковариантный тип возврата)
        return (Enemy) super.clone();
    } catch (CloneNotSupportedException e) {
        // Этот catch НИКОГДА не сработает (класс реализует Cloneable).
        // Но компилятор требует обработать checked exception.
        throw new AssertionError("Клонирование не поддерживается", e);
    }
}
```

**Доступ к `private`-полю другого объекта того же класса:**
```java
// Enemy.java, строки 583–588
// template.baseHealth — прямой доступ к private-полю ДРУГОГО объекта того же класса.
// Это РАЗРЕШЕНО в Java: private ограничивает доступ на уровне КЛАССА, а не объекта.
return new Enemy(newName, template.baseHealth, template.baseAttack, ...);
```

---

### 3.14 — Передача объектов как параметров

**Концепция:** Объекты передаются как ссылки. Изменение полей внутри метода видно снаружи.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Game.java` | строка 1598 | `battle(Enemy enemy)` — метод принимает объект; изменения видны вызывающему |
| `GameCharacter.java` | строка 793 | `findStrongest(T[] characters)` — передача массива объектов |
| `Inventory.java` | строка 533 | `displayAnyInventory(Inventory<?> inv)` — передача с wildcard |

---

## Раздел 6: Обобщения / Generics (3.15, 3.17, 3.31, 3.32)

### 3.15 — Дженерики и наследование

**Концепция:** `List<Warrior>` **не является** `List<GameCharacter>`, даже если `Warrior extends GameCharacter`.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Inventory.java` | строка 80 | `class Inventory<T>` — обобщённый класс |
| `Game.java` | строка 206 | `Inventory<Inventory.ItemInfo>` — конкретный параметр типа |
| `GameCharacter.java` | строки 793–823 | `<T extends GameCharacter> T findStrongest(T[])` — обобщённый метод |

**Обобщённый класс Inventory<T>:**
```java
// Inventory.java, строка 80
// T — параметр типа (type parameter). Имя произвольное, по соглашению — одна буква.
// T определяется при использовании: Inventory<ItemInfo>, Inventory<String>, etc.
public class Inventory<T> implements Iterable<Inventory<T>.Slot>, Serializable {

    public class Slot {  // inner class (нестатический): имеет доступ к T внешнего класса
        private T item;  // item хранит элемент конкретного типа T
        ...
    }

    public void addItem(T item, int quantity) throws InventoryFullException { ... }
}
```

---

### 3.17 — Ограниченные дженерики (Bounded Type Parameters)

**Концепция:** `<T extends GameCharacter>` — T должен быть GameCharacter или его наследником.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 775–823 | Полное объяснение `<T extends GameCharacter>` |

**Фрагмент кода:**
```java
// GameCharacter.java, строка 793
// <T extends GameCharacter> — T обязан быть GameCharacter или наследником.
// Это гарантирует: у T есть метод getHealth().
// Без ограничения (<T>) компилятор не позволит вызвать t.getHealth().
//
// Метод сохраняет КОНКРЕТНЫЙ тип:
//   Warrior[] warriors = { w1, w2 };
//   Warrior best = findStrongest(warriors);  // возвращает Warrior, не GameCharacter!
public static <T extends GameCharacter> T findStrongest(T[] characters) { ... }
```

---

### 3.31 — Стирание типов (Type Erasure)

**Концепция:** Дженерики существуют только во время компиляции. В байткоде вместо `T` остаётся `Object` (или граница ограничения).

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Inventory.java` | строки 396–413 | `@SuppressWarnings("unchecked")` — необходим из-за стирания типов |

**Фрагмент кода:**
```java
// Inventory.java, строка 396
// Массив slots объявлен как Object[] (не T[]) — из-за стирания типов.
// Java не позволяет создавать массивы параметризованных типов: new T[n] — ошибка!
// Поэтому хранится Object[], а при чтении нужен unchecked-каст.
// @SuppressWarnings("unchecked") — подавляет предупреждение компилятора об этом касте.
@SuppressWarnings("unchecked")
public Slot getSlot(int index) {
    return (Slot) slots[index];  // (Slot) — приведение, безопасное в рантайме
}
```

---

### 3.32 — Wildcards (подстановочные знаки)

**Концепция:** `?` — неизвестный тип. Три формы: `?`, `? extends T`, `? super T`.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 753–773 | Подробное объяснение трёх видов wildcards с мнемоникой PECS |
| `Inventory.java` | строка 533 | `Inventory<?>` — неограниченный wildcard |

**Три вида wildcards (из GameCharacter.java, строки 760–772):**

| Форма | Пример | Что можно делать |
|-------|--------|-----------------|
| `?` | `Inventory<?>` | только читать как `Object` |
| `? extends T` | `Inventory<? extends GameCharacter>` | читать как `GameCharacter`, **нельзя добавлять** |
| `? super T` | `List<? super Warrior>` | добавлять `Warrior`, читать только как `Object` |

**Мнемоника PECS: Producer Extends, Consumer Super.**

```java
// Inventory.java, строка 533
// Inventory<?> — принимает ЛЮБОЙ Inventory (ItemInfo, String, Integer...).
// Внутри можно только читать элементы как Object, не добавлять.
public static void displayAnyInventory(Inventory<?> inv) {
    System.out.println("=== Инвентарь (размер: " + inv.getSize() + "/"
        + inv.getCapacity() + ") ===");
    for (int i = 0; i < inv.getSize(); i++) {
        System.out.println("  " + (i + 1) + ". " + inv.getSlot(i));
    }
}
```

---

## Раздел 7: Callback (3.16)

**Концепция:** Интерфейс-слушатель. Объект A регистрирует слушателя у объекта B. Когда в B происходит событие — B вызывает метод слушателя. Паттерн Observer.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `BattleEventListener.java` | строки 14–87 | Объявление callback-интерфейса, подробное объяснение паттерна |
| `BattleEventListener.java` | строки 103–170 | Абстрактные методы + `default`-методы |
| `Game.java` | строка 272 | `private transient BattleEventListener listener` — поле для хранения слушателя |
| `Game.java` | строки 1650–1652 | Вызов `listener.onBattleStart(...)` — уведомление слушателя |
| `Game.java` | строки 2088–2092 | Вызов `listener.onAttack(...)` — при каждой атаке |

**Структура паттерна:**
```java
// BattleEventListener.java — интерфейс (контракт слушателя)
public interface BattleEventListener {
    void onAttack(GameCharacter attacker, DamageType damage, int actualDamage);
    void onEnemyDefeated(GameCharacter hero, String enemyName, int expReward);
    void onLevelUp(GameCharacter hero, int newLevel);

    // default-методы — не обязательны к реализации
    default void onBattleStart(GameCharacter hero, String enemyName) { ... }
    default void onBattleEnd(GameCharacter hero, boolean victory) { ... }
}

// Использование в Game.java
if (listener != null) {
    listener.onAttack(hero, damage, dmg);  // callback — Game сам не знает, что именно произойдёт
}
```

**Почему это не `@FunctionalInterface`?**
`BattleEventListener` имеет несколько абстрактных методов → нельзя реализовать лямбдой. Нужен анонимный класс или именованный класс.

---

## Раздел 7а: Анонимные классы (3.18)

**Концепция:** Анонимный класс — класс без имени, объявляемый и создаваемый одновременно прямо в выражении. Используется, когда реализация нужна в одном месте и создавать отдельный файл избыточно.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Game.java` | строки 482–517 | Анонимная реализация `BattleEventListener` с подробным объяснением |

**Синтаксис анонимного класса:**
```java
// Game.java, строки 482–517
//
// new BattleEventListener() { ... } — АНОНИМНЫЙ КЛАСС.
// Создаётся безымянный класс, реализующий интерфейс BattleEventListener.
//
// Что происходит за кулисами:
//   1. Компилятор создаёт скрытый класс Game$1, реализующий BattleEventListener.
//   2. new BattleEventListener() { ... } — создаёт экземпляр этого скрытого класса.
//   3. Внутри { ... } — тело класса: переопределённые методы интерфейса.
listener = new BattleEventListener() {
    @Override
    public void onAttack(GameCharacter attacker, DamageType damage, int actualDamage) {
        System.out.println("  [EVENT] " + attacker.getName() + " атакует, урон: " + actualDamage);
    }

    @Override
    public void onEnemyDefeated(GameCharacter hero, String enemyName, int expReward) {
        System.out.println("  [EVENT] " + enemyName + " повержен! +" + expReward + " XP");
    }

    @Override
    public void onLevelUp(GameCharacter hero, int newLevel) {
        System.out.println("  ★ [EVENT] " + hero.getName() + " достиг уровня " + newLevel + "!");
    }
};
```

**Когда анонимный класс, а не лямбда?**

| Ситуация | Что использовать |
|----------|-----------------|
| Интерфейс с **одним** абстрактным методом (`@FunctionalInterface`) | Лямбда: `x -> x.doSomething()` |
| Интерфейс с **несколькими** абстрактными методами | Анонимный класс (как здесь) |
| Нужен `Comparator` одноразово | Лямбда: `(a, b) -> a.getName().compareTo(b.getName())` |

**Особенности анонимного класса:**
- **Нет имени** → нельзя ссылаться на тип явно. Ссылка хранится в переменной типа интерфейса.
- **Может захватывать переменные** из окружающего метода, но только `effectively final` (не изменяющиеся после присвоения).
- **Единственный способ добавить код в конструктор** — блок инициализации экземпляра `{ }`, так как у анонимного класса нет имени для конструктора.
- Компилятор генерирует файл `Game$1.class` — скрытый класс.

**Зачем использовать, если есть лямбды?**
Лямбды (Java 8+) заменяют анонимные классы для **функциональных интерфейсов** (один метод). Но `BattleEventListener` имеет три метода — лямбда не подходит. Анонимный класс остаётся единственным компактным способом для таких случаев без создания отдельного файла.

---

## Раздел 8: Records (3.19)

**Концепция:** `record` — неизменяемый класс данных (Java 16+). Автоматически генерирует конструктор, геттеры (без префикса `get`), `equals()`, `hashCode()`, `toString()`. Всегда `final`.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `BattleStats.java` | строки 12–120 | `record` с компактным конструктором |
| `BattleRecord.java` | строки 12–131 | `record` + `implements Comparable<BattleRecord>` |
| `LootDrop.java` | строки 2–35 | `record` для данных лут-таблицы, поля с комментариями |
| `DamageType.java` | строки 55–109 | Три `record` как реализации `sealed interface` |

### BattleStats.java — record с дополнительным методом

```java
// BattleStats.java
// Компоненты record — автоматически становятся:
//   - private final полем
//   - параметром конструктора
//   - публичным геттером (метод с тем же именем, без "get"!)
public record BattleStats(
    String characterName,   // имя персонажа
    int totalDamageDealt,   // нанесённый урон
    int totalDamageReceived,// полученный урон
    int enemiesDefeated,    // побеждённые враги
    int healingDone         // исцеление
) {
    // В record можно добавлять обычные методы
    public void display() { ... }
}

// Использование: геттеры без "get"!
BattleStats stats = new BattleStats("Артур", 150, 80, 5, 30);
String name = stats.characterName(); // НЕ getName()!
```

### BattleRecord.java — record + Comparable

```java
// BattleRecord.java, строки 12–131
// record может реализовывать интерфейсы
public record BattleRecord(
    String heroName,       // String — ссылочный тип
    long score,            // long — 64 бита, диапазон больше int
    int enemiesDefeated,   // int — 32 бита, достаточно для малых значений
    long timestamp         // System.currentTimeMillis(): требует long (int переполнится в 2038!)
) implements Comparable<BattleRecord>, Serializable {

    private static final long serialVersionUID = 1L; // record тоже может быть Serializable

    @Override
    public int compareTo(BattleRecord other) {
        return Long.compare(other.score(), this.score()); // сортировка по убыванию счёта
    }
}
```

**Что заменяет `record` (из комментариев в коде):**
> Раньше писали обычный `class` с ~40 строками шаблонного кода: конструктор, геттеры, `equals()`, `hashCode()`, `toString()`. `record` делает всё это за одну строку.

---

## Раздел 8а: Вывод типов var (3.20)

**Концепция:** `var` (Java 10+) — ключевое слово для **вывода типа локальной переменной**. Компилятор сам определяет тип из правой части выражения. Тип фиксируется на этапе компиляции — это **не динамическая типизация**.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Game.java` | строки 2107–2113 | `var amt`, `var elem`, `var phys`, `var mag` внутри record-паттернов |
| `Game.java` | строка 2219 | Комментарий: «`var` — тип фиксируется при компиляции» |
| `Bestiary.java` | строка 93 | `for (var entry : bestiary)` — вывод типа в for-each |

**Синтаксис:**
```java
// Без var — явный тип:
String name = "Артур";
List<Enemy> enemies = new ArrayList<>();

// С var — компилятор определяет тип сам:
var name = "Артур";           // тип: String
var enemies = new ArrayList<Enemy>();  // тип: ArrayList<Enemy>
```

**Использование в record-паттернах (Game.java, строки 2107–2113):**
```java
switch (damage) {
    // var amt — компилятор знает, что record Physical(int amount),
    // значит amt имеет тип int.
    case DamageType.Physical(var amt) ->
        System.out.println(hero.getName() + " наносит " + amt + " физического урона!");
    // var amt — int, var elem — String (из record Magical(int amount, String element))
    case DamageType.Magical(var amt, var elem) ->
        System.out.println(hero.getName() + " наносит " + amt + " магического урона (" + elem + ")!");
    // var phys — int, var mag — int (из record Mixed(int physical, int magical))
    case DamageType.Mixed(var phys, var mag) ->
        System.out.println(hero.getName() + " наносит " + phys + " физ. + " + mag + " маг. урона!");
}
```

**Использование в for-each (пример из Bestiary.java, строка 93):**
```java
// var entry — компилятор определяет тип из итерируемого bestiary.
// Без var: Map.Entry<String, BestiaryEntry> entry — длинно.
// С var: читается компактнее, тип всё равно известен компилятору.
for (var entry : bestiary) { ... }
```

**Правила применения var:**

| Можно | Нельзя |
|-------|--------|
| Локальные переменные в методах | Поля класса |
| Переменные в for-each | Параметры методов |
| Переменные в for (инициализатор) | Возвращаемый тип метода |
| Переменные в try-with-resources | Лямбда-параметры |

**Когда использовать var:**
- Когда тип очевиден из правой части: `var scanner = new Scanner(System.in)`
- В длинных параметризованных типах: `var map = new HashMap<String, List<Integer>>()`
- В for-each, когда тип понятен из контекста

**Когда НЕ использовать var:**
- Когда тип неочевиден: `var result = getResult()` — непонятно, что вернёт метод
- С примитивными литералами без контекста: `var x = 42` — технически работает, но избыточно

**var — это НЕ:**
- Не динамическая типизация (как в Python/JavaScript): тип фиксируется НАВСЕГДА при компиляции
- Не `Object`: нельзя потом присвоить другой тип

---

## Раздел 9: Блоки инициализации и цепочка конструкторов (3.21)

**Концепция:** Два вида блоков инициализации: `static {}` (один раз при загрузке класса) и `{}` (при каждом создании объекта).

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Enemy.java` | строки 146–175 | `static { }` — статический блок, выполняется при загрузке класса |
| `Enemy.java` | строки 177–206 | `{ }` — блок инициализации экземпляра, выполняется при каждом `new` |
| `Game.java` | строка 426 | `static { }` — статический блок в классе `Game` |

**Порядок выполнения при `new Enemy(...)`:**
```
1. Загрузка класса (один раз): static { }
2. Выделение памяти, поля → значения по умолчанию (0, null, false)
3. Блок инициализации экземпляра { } (вставляется компилятором в каждый конструктор)
4. Тело конструктора
```

**Фрагменты кода:**
```java
// Enemy.java, строка 170 — статический блок
static {
    System.out.println("[Enemy] Класс Enemy загружен в память.");
    totalEnemiesCreated = 0;
}

// Enemy.java, строка 204 — блок инициализации экземпляра
// Выполняется при каждом new Enemy(...), независимо от конструктора
{
    totalEnemiesCreated++;
}
```

**Зачем нужен блок экземпляра, если есть конструктор?**
Если у класса **несколько конструкторов** — одинаковый код не нужно дублировать в каждом. Блок выполнится в любом случае. Также это **единственный способ** добавить инициализирующий код в анонимный класс (у него нет имени → нельзя написать конструктор).

---

## Раздел 10: Инкапсуляция (3.22)

**Концепция:** Скрытие внутреннего состояния объекта. Доступ только через методы (геттеры/сеттеры). Защита данных от некорректного изменения.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 105–133 | Все поля `private` — инкапсуляция данных |
| `GameCharacter.java` | строки 534–570 | Геттеры: `getName()`, `getHealth()`, `getLevel()` |
| `GameCharacter.java` | строка 570 | Геттер без `public` — package-private: виден только внутри пакета `rpg` |
| `Inventory.java` | строки 166–195 | Геттеры и модифицирующий метод в inner class `Slot` |

**Зачем нужна инкапсуляция:**
```java
// Без инкапсуляции — опасно:
enemy.health = -100;  // здоровье не может быть отрицательным — баг!

// С инкапсуляцией — безопасно:
enemy.takeDamage(100);  // метод гарантирует: health = Math.max(0, health - damage)
```

---

## Раздел 11: final класс и метод (3.23)

**Концепция:** `final` у метода — нельзя переопределить в наследнике. `final` у класса — нельзя создать наследника вовсе.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 417 | `public final void takeDamage(int)` — final-метод |
| `GameCharacter.java` | строка 450 | `public final boolean isAlive()` — final-метод |
| `DamageType.java` | строка 74 | `record Physical(...)` — record неявно `final` |

**Когда делать метод `final`:**
```java
// GameCharacter.java, строка 417
// takeDamage — КРИТИЧЕСКАЯ боевая механика.
// Нельзя позволить наследнику (Warrior) переопределить формулу расчёта урона —
// это нарушит баланс игры.
// final гарантирует: формула «damage - defense, минимум 1» одна для всех классов.
public final void takeDamage(int damage) {
    int actualDamage = Math.max(1, damage - defense);
    health = Math.max(0, health - actualDamage);
}
```

**`record` всегда `final`** — нельзя наследоваться от `record`.

---

## Раздел 12: Полиморфизм (3.24)

**Концепция:** Переменная типа `GameCharacter` может хранить `Warrior`, `Mage` или `Archer`. Метод, вызванный у такой переменной, выполнит реализацию реального объекта (позднее связывание, late binding).

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Game.java` | строка 198 | `private GameCharacter hero` — переменная базового типа |
| `Game.java` | строки 709–716 | `hero = switch(choice) { case 1 -> new Warrior(name); ... }` |
| `Game.java` | строка 2064 | `hero.performAttack()` — полиморфный вызов |
| `Game.java` | строка 2203 | `hero.specialAttack()` — полиморфный вызов |
| `Game.java` | строки 2191, 2250 | Downcast: `hero instanceof Mage m`, `hero instanceof Warrior w` |

**Приведение типов (upcasting / downcasting):**
```java
// Upcasting — неявное, всегда безопасно
GameCharacter hero = new Warrior("Артур");  // Warrior → GameCharacter

// Downcasting — явное, может бросить ClassCastException
if (hero instanceof Warrior w) {       // pattern matching: проверка + приведение
    System.out.println(w.getRage());   // w уже типа Warrior, геттер rage доступен
}

// Полиморфный вызов: реальный тип hero определяет, какой код выполнится
DamageType damage = hero.performAttack();
// Если hero — Warrior: DamageType.Physical(15)
// Если hero — Mage:    DamageType.Magical(20, "огонь")
// Если hero — Archer:  DamageType.Mixed(8, 6)
```

---

## Раздел 13: Sealed-классы (3.25)

**Концепция:** `sealed interface` ограничивает список реализаций. Компилятор знает все варианты → может проверить полноту `switch`.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `DamageType.java` | строки 19–110 | `sealed interface DamageType` с тремя `record`-реализациями |
| `Game.java` | строки 2107–2114 | `switch (damage)` по `DamageType` — без `default` (компилятор проверяет полноту) |

**Структура:**
```java
// DamageType.java
// permits не указан явно: реализации объявлены ВНУТРИ интерфейса.
// Полная форма: public sealed interface DamageType permits Physical, Magical, Mixed {}
public sealed interface DamageType {
    int totalDamage();

    record Physical(int amount) implements DamageType {
        @Override public int totalDamage() { return amount; }
    }
    record Magical(int amount, String element) implements DamageType {
        @Override public int totalDamage() { return amount; }
    }
    record Mixed(int physical, int magical) implements DamageType {
        @Override public int totalDamage() { return physical + magical; }
    }
}
```

**Зачем `sealed` вместо обычного интерфейса?**

| Обычный интерфейс | sealed interface |
|-------------------|-----------------|
| Любой класс может реализовать | Только перечисленные в `permits` |
| `switch` требует `default` | `switch` без `default` (компилятор знает все случаи) |
| Новый тип → `switch` молча пропустит | Новый тип → ошибка компиляции везде, где нет case |

---

## Раздел 14: Pattern Matching (3.26–3.27)

### 3.26 — Pattern Matching с instanceof (Java 16+)

**Концепция:** `obj instanceof Type var` — проверяет тип И создаёт переменную в одну строку.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строки 682–701 | Базовое объяснение `instanceof` с pattern variable |
| `Game.java` | строка 2191 | `if (hero instanceof Mage m)` — проверка с доступом к `getMana()` |
| `Game.java` | строки 2233–2251 | `if (hero instanceof Warrior w)` с подробным комментарием |

**Было / стало:**
```java
// ДО Java 16 (два шага):
if (hero instanceof Warrior) {
    Warrior w = (Warrior) hero;  // явный каст
    System.out.println(w.getRage());
}

// ПОСЛЕ Java 16 (один шаг):
if (hero instanceof Warrior w) {
    System.out.println(w.getRage());  // w уже типа Warrior, каст не нужен
}
```

---

### 3.27 — Record Patterns (Java 21+)

**Концепция:** Деконструкция (destructuring) record прямо в `case`. Извлекает поля из record без вызова геттеров.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Game.java` | строки 2094–2114 | `case DamageType.Physical(var amt)` — запись с var |
| `Game.java` | строки 2217–2231 | Повторное использование record-паттернов |

**Фрагмент кода:**
```java
// Game.java, строки 2107–2114
// switch по sealed interface — компилятор ГАРАНТИРУЕТ полноту (нет default).
// Record-паттерны: извлекают поля record прямо в case.
switch (damage) {
    case DamageType.Physical(var amt) ->
        System.out.println(hero.getName() + " наносит " + amt + " физического урона!");
    case DamageType.Magical(var amt, var elem) ->
        System.out.println(hero.getName() + " наносит " + amt + " магического урона (" + elem + ")!");
    case DamageType.Mixed(var phys, var mag) ->
        System.out.println(hero.getName() + " наносит " + phys + " физ. + " + mag + " маг. урона!");
}
```

`var` внутри record-паттерна — вывод типа при компиляции. `var amt` → компилятор определяет тип `int` из `record Physical(int amount)`.

---

## Раздел 15: Интерфейсы и полиморфизм (3.28–3.29)

### 3.28 — Интерфейсы: default и static методы

**Концепция:** `default`-метод в интерфейсе имеет реализацию. Класс может переопределить или унаследовать как есть. `static`-метод в интерфейсе — вызывается через имя интерфейса.

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `Attackable.java` | строка 43 | `interface Attackable` — объявление интерфейса |
| `Attackable.java` | строка 55 | `DamageType performAttack()` — абстрактный метод |
| `Attackable.java` | строка 71 | `default String getBattleCry()` — default-метод |
| `Attackable.java` | строка 89 | `static int calculateCritDamage(int)` — static-метод |
| `Healable.java` | строка 37 | `void heal(int amount)` — абстрактный метод |
| `Healable.java` | строка 59 | `default boolean needsHealing()` — default-метод |
| `Healable.java` | строка 72 | `static int baseHealAmount(int level)` — static-метод |
| `Warrior.java` | строка 159 | `@Override getBattleCry()` — переопределение default-метода |

**Зачем default-методы?**
До Java 8 нельзя было добавить новый метод в интерфейс без поломки всех его реализаций. `default` позволяет добавить метод с реализацией «по умолчанию» — существующие классы ничего не ломают.

---

### 3.29 — Реализация нескольких интерфейсов

**Концепция:** Класс может `implements` несколько интерфейсов через запятую (в отличие от наследования: `extends` только один класс).

| Файл | Строка | Что демонстрирует |
|------|--------|-------------------|
| `GameCharacter.java` | строка 75 | `implements Attackable, Healable, Serializable` — три интерфейса |
| `BattleRecord.java` | строка 87 | `implements Comparable<BattleRecord>, Serializable` — два интерфейса |
| `Enemy.java` | строка 112 | `implements Cloneable, Comparable<Enemy>, Serializable` — три интерфейса |

**Фрагмент кода:**
```java
// GameCharacter.java, строка 75
// Один класс реализует три интерфейса одновременно:
//   Attackable  — умеет атаковать (performAttack, getBattleCry)
//   Healable    — умеет лечиться (heal, needsHealing)
//   Serializable — можно сохранить/загрузить из файла
public abstract class GameCharacter implements Attackable, Healable, Serializable {
```

---

## Раздел 16: Вложенные и внутренние классы (3.30)

**Концепция:** Класс, объявленный внутри другого класса. Четыре вида: inner class, static nested class, local class, anonymous class.

### Сравнение видов вложенных классов

| Вид | static? | Доступ к внешнему классу | Когда использовать |
|-----|---------|--------------------------|-------------------|
| Inner class | нет | Да (через `Внешний.this`) | Когда нужен доступ к полям внешнего объекта |
| Static nested class | да | Нет (только к static-членам) | Логически связанный, но независимый тип |
| Local class | нет | Да + effectively final переменные метода | Редко, обычно заменяется лямбдой |
| Anonymous class | нет | Да + effectively final переменные метода | Одноразовая реализация интерфейса/абстрактного класса |

---

### Inner class — Inventory.Slot (Inventory.java:131)

```java
// Inventory.java, строка 131
// Slot — ВНУТРЕННИЙ КЛАСС (inner class, нестатический).
//
// Ключевое свойство: Slot НЕЯВНО имеет ссылку на экземпляр Inventory.
// Это значит:
//   1. Для создания Slot нужен существующий объект Inventory:
//      inventory.new Slot(item, 1)  // синтаксис создания inner class
//   2. Slot имеет доступ ко всем полям Inventory (в т.ч. private):
//      Через неявную ссылку Inventory.this.slots, Inventory.this.size
//   3. Slot может использовать параметр типа T внешнего класса Inventory<T>:
//      private T item;  // T — тот же T, что у Inventory<T>
public class Slot implements Serializable {
    private T item;      // тип из внешнего Inventory<T>
    private int quantity;
    ...
}
```

---

### Static nested class — Inventory.ItemInfo (Inventory.java:239)

```java
// Inventory.java, строка 239
// ItemInfo — СТАТИЧЕСКИЙ ВЛОЖЕННЫЙ КЛАСС (static nested class).
//
// static означает: ItemInfo НЕ привязан к конкретному экземпляру Inventory.
// Для создания объекта ItemInfo НЕ нужен объект Inventory:
//   Inventory.ItemInfo info = new Inventory.ItemInfo("Меч", 15);  // напрямую
//
// Почему static (а не inner class)?
//   ItemInfo не нужен доступ к полям Inventory (slots, size и т.д.).
//   Без static каждый ItemInfo хранил бы скрытую ссылку на Inventory — лишняя память.
//   Правило: если вложенный класс НЕ обращается к экземпляру внешнего — делай static.
public static class ItemInfo implements Serializable {
    private final String name;
    private final int value;
    ...
}
```

---

### Private inner class — Inventory.InventoryIterator (Inventory.java:602)

```java
// Inventory.java, строка 602
// InventoryIterator — ПРИВАТНЫЙ ВНУТРЕННИЙ КЛАСС.
//
// private — класс полностью скрыт: ни один внешний код не знает о его существовании.
// Это реализует паттерн инкапсуляции: снаружи виден только контракт Iterator<Slot>,
// а конкретная реализация скрыта.
//
// Нестатический (inner class): имеет доступ к полям внешнего Inventory:
//   slots — массив ячеек (для извлечения элементов)
//   size  — количество занятых ячеек (для проверки границ итератора)
// Доступ осуществляется как к собственным полям (компилятор добавит Inventory.this).
private class InventoryIterator implements Iterator<Slot> {
    private int currentIndex = 0;

    @Override public boolean hasNext() { return currentIndex < size; }
    @Override public Slot next() { return getSlot(currentIndex++); }
}
```

---

### Nested record — Bestiary.BestiaryEntry (Bestiary.java:146)

```java
// Bestiary.java, строка 146
// BestiaryEntry — вложенный RECORD внутри класса Bestiary.
//
// Все вложенные record неявно STATIC (как и static nested class).
// Создание: new Bestiary.BestiaryEntry("Гоблин", 3, ...)
//           — НЕ нужен экземпляр Bestiary.
//
// Зачем вкладывать record в класс?
//   BestiaryEntry логически связан ТОЛЬКО с Bestiary — нигде больше не используется.
//   Вложение «объявляет» эту связь явно и не «засоряет» пакет rpg лишним файлом.
//   Доступ извне: Bestiary.BestiaryEntry (через имя внешнего класса).
public record BestiaryEntry(String name, int killCount, ...) { ... }
```

---

### Вложенные records внутри sealed interface — DamageType.Physical/Magical/Mixed (DamageType.java:55–109)

```java
// DamageType.java, строки 55–109
// Physical, Magical, Mixed — три record-реализации внутри sealed interface DamageType.
//
// Вложенные в интерфейс типы всегда неявно static.
// Преимущество: permits не нужен явно — компилятор видит все реализации «рядом».
// Читаемость: весь тип DamageType с реализациями — в одном файле.
public sealed interface DamageType {
    record Physical(int amount) implements DamageType { ... }
    record Magical(int amount, String element) implements DamageType { ... }
    record Mixed(int physical, int magical) implements DamageType { ... }
}
```

---

### Итог: как выбрать вид вложенного класса

```
Нужен доступ к полям внешнего объекта?
  ДА  → Inner class (нестатический)
  НЕТ → Static nested class (или nested record)

Нужна реализация только в одном методе?
  ДА  → Anonymous class (или лямбда, если функциональный интерфейс)
  НЕТ → Inner / static nested class
```

---

## Дополнительные темы (3.19 compact record, 3.22 encapsulation variant)

### Компактный конструктор record (3.19)

`BattleStats.java` использует **компактный конструктор** (compact constructor) — форму без явного перечисления параметров. Применяется для валидации или нормализации значений при создании.

```java
// BattleStats.java — компактный конструктор (если бы был в этом проекте)
// public record BattleStats(String characterName, int totalDamageDealt, ...) {
//     // Компактный конструктор — без параметров в скобках, они неявны
//     public BattleStats {
//         if (totalDamageDealt < 0) throw new IllegalArgumentException();
//         characterName = characterName.trim();  // нормализация
//     }
// }
```

Компактная форма присваивает значения полям **автоматически** после выполнения тела.

---

## Сводная таблица: подглавы 3.1–3.32

| Подглава | Концепция | Основной файл | Дополнительные файлы |
|----------|-----------|--------------|----------------------|
| 3.1 | Классы и объекты | `GameCharacter.java` | `Enemy.java`, `Warrior.java` |
| 3.2 | Пакеты | `DamageType.java` (стр. 14–17) | все файлы (`package rpg;`) |
| 3.3 | Модификаторы доступа | `GameCharacter.java` (стр. 105–133) | `Inventory.java` (стр. 131, 602) |
| 3.4 | Конструкторы | `Warrior.java` (стр. 65–92) | `Enemy.java` (стр. 310, 328) |
| 3.5 | static | `GameCharacter.java` (стр. 148–158) | `Attackable.java`, `Game.java` |
| 3.6 | Перегрузка методов | `GameCharacter.java` (стр. 319, 352, 363) | `Enemy.java` (стр. 310, 328) |
| 3.7 | this | `GameState.java` (стр. 128–131) | `GameCharacter.java` (стр. 323) |
| 3.8 | enum | `EnemyRank.java`, `GameState.java`, `ItemType.java` | `Achievement.java` |
| 3.9 | extends (наследование) | `Warrior.java`, `Mage.java`, `Archer.java` | `GameCharacter.java` |
| 3.10 | abstract | `GameCharacter.java` (стр. 75, 278, 295) | `ItemType.java` (стр. 129) |
| 3.10 | Методы Object | `GameCharacter.java` (стр. 612, 676, 725) | `Enemy.java` (стр. 792, 711, 740) |
| 3.11 | super | `Warrior.java` (стр. 89) | `Mage.java`, `Archer.java` |
| 3.12 | @Override | `Warrior.java` (стр. 106) | все наследники |
| 3.13 | Ссылочные типы, Cloneable | `Enemy.java` (стр. 530–589) | — |
| 3.14 | Объекты как параметры | `Game.java` (стр. 1598) | `GameCharacter.java` (стр. 793) |
| 3.15 | Generics + наследование | `Inventory.java` (стр. 80) | `Game.java` (стр. 206) |
| 3.16 | Callback | `BattleEventListener.java` | `Game.java` (стр. 272, 1650) |
| 3.17 | Bounded generics | `GameCharacter.java` (стр. 793–823) | — |
| 3.18 | Анонимные классы | `Game.java` (стр. 482–517) | — |
| 3.19 | record | `BattleStats.java`, `BattleRecord.java`, `LootDrop.java`, `DamageType.java` | — |
| 3.20 | var (вывод типов) | `Game.java` (стр. 2107–2113) | `Bestiary.java` (стр. 93) |
| 3.21 | Блоки инициализации | `Enemy.java` (стр. 146–206) | `Game.java` (стр. 426) |
| 3.22 | Инкапсуляция | `GameCharacter.java` (стр. 105–570) | `Inventory.java` (стр. 131–195) |
| 3.23 | final метод/класс | `GameCharacter.java` (стр. 417, 450) | `DamageType.java` (record = final) |
| 3.24 | Полиморфизм | `Game.java` (стр. 198–716, 2064, 2203) | — |
| 3.25 | sealed interface | `DamageType.java` | — |
| 3.26 | Pattern matching instanceof | `GameCharacter.java` (стр. 682–701) | `Game.java` (стр. 2191, 2250) |
| 3.27 | Record patterns | `Game.java` (стр. 2094–2114) | — |
| 3.28 | Интерфейсы (default, static) | `Attackable.java`, `Healable.java` | `BattleEventListener.java` |
| 3.29 | Несколько интерфейсов | `GameCharacter.java` (стр. 75) | `Enemy.java` (стр. 112) |
| 3.30 | Вложенные/внутренние классы | `Inventory.java` (стр. 131, 239, 602) | `Bestiary.java` (стр. 146), `DamageType.java` (стр. 55–109) |
| 3.31 | Type erasure | `Inventory.java` (стр. 396) | — |
| 3.32 | Wildcards | `GameCharacter.java` (стр. 753–773) | `Inventory.java` (стр. 533) |

---

## Рекомендуемый порядок изучения главы 3

```
1. Enemy.java           — поля, конструктор, static, блоки инициализации, toString
2. EnemyRank.java       — базовый enum (поля, конструктор, методы)
3. GameState.java       — enum как конечный автомат, switch-выражение
4. ItemType.java        — enum с абстрактным методом
5. Attackable.java      — интерфейс: abstract, default, static методы
6. Healable.java        — второй интерфейс (pattern: несколько implements)
7. GameCharacter.java   — абстрактный класс: все модификаторы, generics, wildcards
8. Warrior.java         — наследование, super(), @Override
9. Mage.java            — переопределение toString()
10. Archer.java         — своя механика (critChance)
11. DamageType.java     — sealed interface + record внутри интерфейса
12. BattleStats.java    — record с методом
13. BattleRecord.java   — record + Comparable + Serializable
14. LootDrop.java       — record для данных
15. BattleEventListener.java — callback-интерфейс, паттерн Observer
16. Inventory.java      — generics, inner/static nested class, Iterable/Iterator
17. Game.java           — полиморфизм, pattern matching, record patterns
```
