// Пакет rpg — все классы игры находятся в одном пакете (см. Main.java).
package rpg;

// ===== ИМПОРТЫ — ПОДКЛЮЧЕНИЕ КЛАССОВ ИЗ ДРУГИХ ПАКЕТОВ =====
//
// import — подключает класс из другого пакета, чтобы использовать его по короткому имени.
// Без импорта пришлось бы писать полное имя: java.util.Scanner scanner = ...

// java.util.Scanner — класс для чтения пользовательского ввода (клавиатура, файлы).
import java.util.Scanner;

// ===== ИМПОРТЫ КОЛЛЕКЦИЙ (глава 5) =====
//
// Java Collections Framework — набор интерфейсов и классов для хранения данных.
// Основные интерфейсы: Collection → List, Set, Queue; Map (отдельная иерархия).
//
// Иерархия коллекций:
//   Collection<E>     ← базовый интерфейс
//   ├── List<E>       ← упорядоченная коллекция с индексами (ArrayList, LinkedList)
//   ├── Set<E>        ← коллекция уникальных элементов (HashSet, TreeSet)
//   └── Queue<E>      ← очередь (PriorityQueue, ArrayDeque)
//
//   Map<K,V>          ← отдельная иерархия: коллекция пар ключ-значение (HashMap, TreeMap)

// ArrayList — реализация List на основе динамического массива.
// Быстрый доступ по индексу: O(1). Медленная вставка в середину: O(n).
// Подходит для: хранения списков с частым доступом по индексу.
import java.util.ArrayList;

// List — интерфейс упорядоченной коллекции (список).
// Элементы имеют индексы (0, 1, 2...), допускаются дубликаты.
// Реализации: ArrayList (быстрый доступ), LinkedList (быстрая вставка).
import java.util.List;

// LinkedList — реализация List на основе двусвязного списка.
// Каждый элемент хранит ссылки на предыдущий и следующий элемент.
// Быстрая вставка/удаление в начало/конец: O(1). Медленный доступ по индексу: O(n).
// Также реализует Deque (двусторонняя очередь): addFirst/addLast, peekFirst/peekLast.
import java.util.LinkedList;

// Map — интерфейс коллекции пар «ключ → значение».
// Каждый ключ уникален, каждому ключу соответствует одно значение.
// Реализации: HashMap (быстрый доступ), TreeMap (сортировка ключей).
import java.util.Map;

// HashMap — реализация Map на основе хеш-таблицы.
// Доступ по ключу: O(1) в среднем. Порядок элементов НЕ гарантирован.
// Ключ ОБЯЗАН корректно реализовать equals() и hashCode()!
import java.util.HashMap;

// Set — интерфейс коллекции уникальных элементов (множество).
// Не допускает дубликатов: add() игнорирует элемент, если он уже есть.
// Реализации: HashSet (быстрый), TreeSet (отсортированный).
import java.util.Set;

// HashSet — реализация Set на основе хеш-таблицы.
// Проверка «содержит ли?»: O(1) в среднем. Порядок НЕ гарантирован.
// Элементы ОБЯЗАНЫ корректно реализовать equals() и hashCode()!
// Для enum это реализовано автоматически.
import java.util.HashSet;

// TreeSet — реализация Set на основе красно-чёрного дерева (Red-Black Tree).
// Элементы ВСЕГДА отсортированы (по Comparable или Comparator).
// Доступ и поиск: O(log n). Гарантирует порядок обхода.
import java.util.TreeSet;

// PriorityQueue — очередь с приоритетом на основе бинарной кучи (heap).
// Элемент с наивысшим приоритетом (наименьший по Comparator/Comparable) извлекается первым.
// Добавление/извлечение: O(log n). Используется для: инициативы в бою, задач с приоритетами.
import java.util.PriorityQueue;

// ArrayDeque — двусторонняя очередь (deque) на основе массива.
// Может использоваться как СТЕК (push/pop — LIFO) или как ОЧЕРЕДЬ (offer/poll — FIFO).
// Быстрее Stack и LinkedList для этих задач.
// Используется для: стек отмены действий (undo), BFS-алгоритмы.
import java.util.ArrayDeque;

// Comparator — интерфейс для определения КАСТОМНОГО порядка сортировки.
// В отличие от Comparable (один порядок, встроенный в класс),
// Comparator позволяет создавать МНОЖЕСТВО порядков извне.
import java.util.Comparator;

// Collections — утилитный класс с static-методами для работы с коллекциями.
// sort(), unmodifiableList(), emptyList(), singletonList() и др.
import java.util.Collections;

// ===== ИМПОРТЫ ДЛЯ ИСКЛЮЧЕНИЙ (глава 4) =====
//
// NoSuchElementException — unchecked исключение (наследник RuntimeException).
// Бросается, когда запрашиваемый элемент не существует:
//   - Scanner.nextLine() при отсутствии ввода.
//   - Iterator.next() при отсутствии следующего элемента.
//   - ArrayDeque.pop() при пустой очереди.
import java.util.NoSuchElementException;

// ===== ИМПОРТ ДЛЯ try-with-resources (глава 4.2) =====
//
// java.io — пакет для ввода/вывода (Input/Output): файлы, потоки байтов/символов.
// File — класс, представляющий ПУТЬ к файлу или директории (не содержимое!).
// FileNotFoundException — checked exception: файл по указанному пути не найден.
//   Наследник IOException → наследник Exception (НЕ RuntimeException).
import java.io.File;
import java.io.FileNotFoundException;

// Game — главный класс игры, управляющий игровым процессом.
// Этот файл — самый насыщенный конструкциями Java в проекте.
// Новое в этой версии: коллекции (ArrayList, HashMap, HashSet, TreeSet, PriorityQueue, ArrayDeque),
// пользовательские исключения, try-with-resources, multi-catch, finally, assert.
//
// public class Game — объявление КЛАССА.
// public — класс доступен из любого пакета (в нашем случае из Main.java).
// class — ключевое слово для создания класса (в отличие от record, interface, enum).
//   class используется, когда нужно ИЗМЕНЯЕМОЕ СОСТОЯНИЕ (поля меняются в ходе игры).
//   record — для НЕИЗМЕНЯЕМЫХ данных (BattleStats, LootDrop).
//   interface — для КОНТРАКТА без состояния (Attackable, Healable).
//   enum — для ФИКСИРОВАННОГО НАБОРА констант (GameState, EnemyRank).
// Game — имя класса. По соглашению Java — PascalCase (каждое слово с заглавной буквы).
public class Game {

    // ===== КОНСТАНТЫ И СТАТИЧЕСКИЕ ПОЛЯ =====
    //
    // private static final — КОНСТАНТА класса. Три модификатора вместе:
    //   private — доступна только внутри Game (инкапсуляция).
    //   static  — принадлежит КЛАССУ, а не экземпляру. Одна на все объекты Game.
    //             Доступ: Game.MAX_ENEMIES (но извне private не даст обратиться).
    //   final   — значение нельзя изменить после инициализации (настоящая константа).
    //
    // Соглашение Java: имена констант — UPPER_SNAKE_CASE (MAX_ENEMIES, не maxEnemies).
    // По смыслу аналог #define MAX_ENEMIES 5 в C, но ТИПОБЕЗОПАСНАЯ.
    //
    // int — примитивный целочисленный тип (32 бита, от -2^31 до 2^31-1).
    // Для небольших констант int — оптимальный выбор (не нужен long).
    private static final int MAX_ENEMIES = 5;

    // String — класс для хранения строк (НЕИЗМЕНЯЕМЫЙ — immutable).
    // Каждое изменение строки создаёт НОВЫЙ объект String.
    // final здесь означает: ссылка GAME_VERSION не может быть переназначена.
    // Сам объект String и так неизменяемый — final тут для семантики «это константа».
    private static final String GAME_VERSION = "2.0";

    // static БЕЗ final — статическое поле, которое МОЖНО менять.
    // Оно общее для ВСЕХ экземпляров Game (если создать несколько объектов Game,
    // все они видят одну и ту же переменную totalGamesPlayed).
    // Без final — значение увеличивается в конструкторе при каждом создании Game.
    private static int totalGamesPlayed = 0;

    // ===== ПОЛЯ ЭКЗЕМПЛЯРА =====
    //
    // Поля экземпляра (instance fields) — каждый объект Game имеет СОБСТВЕННУЮ копию.
    // В отличие от static-полей, они создаются при new Game() и живут пока объект жив.

    // GameCharacter — АБСТРАКТНЫЙ класс (abstract class, см. GameCharacter.java).
    // Тип поля — абстрактный, а реальный объект будет Warrior, Mage или Archer.
    // Это ПОЛИМОРФИЗМ: переменная типа родителя хранит ссылку на объект-потомок.
    // hero = new Warrior("Имя") → hero.attack() вызовет Warrior.attack(), а не GameCharacter.attack().
    private GameCharacter hero;

    // Inventory<Inventory.ItemInfo> — обобщённый (generic) тип.
    // Inventory<T> — класс-контейнер с параметром типа T (см. Inventory.java).
    // Inventory.ItemInfo — ВЛОЖЕННЫЙ СТАТИЧЕСКИЙ КЛАСС (static nested class) внутри Inventory.
    //   Обращение через имя внешнего класса: Inventory.ItemInfo (как пространство имён).
    //   static nested class не имеет доступа к полям внешнего экземпляра Inventory.
    // <Inventory.ItemInfo> — конкретизация дженерика: этот инвентарь хранит ItemInfo.
    private Inventory<Inventory.ItemInfo> inventory;

    // Scanner — класс из java.util для чтения пользовательского ввода.
    // Scanner(System.in) — привязывается к стандартному потоку ввода (клавиатура).
    // Методы: nextLine() (строка), nextInt() (число), hasNextLine() (есть ли ввод).
    // ВАЖНО: Scanner(System.in) не нужно закрывать — это закроет System.in навсегда!
    private Scanner scanner;

    // ===== ПОБИТОВЫЕ ФЛАГИ ДЛЯ СТАТУСНЫХ ЭФФЕКТОВ (глава 2 — побитовые операции) =====
    //
    // byte — примитивный тип, 8 бит (от -128 до 127). Достаточно для хранения 8 флагов.
    // Каждый бит — отдельный флаг: 0 = нет эффекта, 1 = эффект активен.
    //
    // Зачем побитовые флаги вместо нескольких boolean?
    //   1. Компактность: 8 флагов в 1 байте (вместо 8 boolean = 8 байт).
    //   2. Операции над множеством: statusFlags = 0 сбрасывает ВСЕ эффекты разом.
    //   3. Комбинации: if ((statusFlags & (POISONED | STUNNED)) != 0) — проверка нескольких сразу.
    //   4. Традиция: такой подход используется в сетевых протоколах, ОС, играх.
    //
    // Побитовые операции:
    //   & (AND)  — проверка: (statusFlags & POISONED) != 0 → отравлен?
    //   | (OR)   — установка: statusFlags |= POISONED → добавить отравление.
    //   ~ (NOT)  — снятие: statusFlags &= ~POISONED → убрать отравление.
    //   << (сдвиг влево) — создание масок: 1 << 2 = 0b00000100 = 4.
    private byte statusFlags = 0;

    // static final byte — побитовые маски (константы для каждого эффекта).
    // 1       = 0b00000001 (бит 0) — отравление.
    // 1 << 1  = 0b00000010 (бит 1) — оглушение.
    // 1 << 2  = 0b00000100 (бит 2) — щит.
    // 1 << 3  = 0b00001000 (бит 3) — ярость.
    private static final byte POISONED = 1;
    private static final byte STUNNED = 1 << 1;
    private static final byte SHIELDED = 1 << 2;
    private static final byte ENRAGED = 1 << 3;

    // Состояние игры (enum, глава 3.8).
    // GameState — enum-класс (конечный автомат): MENU → EXPLORING → BATTLE → GAME_OVER.
    // Enum гарантирует, что gameState может принимать ТОЛЬКО допустимые значения.
    // Если бы использовали int (0, 1, 2, 3) — можно было бы присвоить gameState = 99 → баг.
    private GameState gameState = GameState.MENU;

    // Слушатель событий (callback, глава 3.16).
    // BattleEventListener — ФУНКЦИОНАЛЬНЫЙ ИНТЕРФЕЙС (интерфейс с методами-callback'ами).
    // Паттерн «Наблюдатель» (Observer): Game уведомляет listener о событиях боя,
    // не зная КТО слушает и ЧТО делает с информацией (слабая связанность — loose coupling).
    // Инициализируется в конструкторе через анонимный класс (см. ниже).
    private BattleEventListener listener;

    // ===== СЧЁТЧИКИ СТАТИСТИКИ =====
    //
    // Примитивный тип int — для хранения целых чисел.
    // Инициализация = 0 — ЯВНАЯ. Java по умолчанию инициализирует int-поля нулём,
    // но ЯВНАЯ инициализация делает код понятнее: «мы НАМЕРЕННО начинаем с нуля».
    // Частая ошибка: забыть, что ЛОКАЛЬНЫЕ переменные НЕ инициализируются автоматически!
    //   int x; System.out.println(x); → ОШИБКА КОМПИЛЯЦИИ! Но для полей класса x = 0 автоматически.
    private int totalDamageDealt = 0;
    private int totalDamageReceived = 0;
    private int enemiesDefeated = 0;
    private int totalHealing = 0;
    private int damageDealtThisBattle = 0; // урон за текущий бой (для бестиария)

    // ===== НОВЫЕ ПОЛЯ — КОЛЛЕКЦИИ (глава 5) =====

    // ===== List<String> — ЖУРНАЛ БОЕВЫХ СОБЫТИЙ (5.1, 5.2) =====
    //
    // ArrayList<String> — динамический массив строк.
    // В отличие от обычного массива (String[]), ArrayList автоматически растёт при добавлении.
    // Операции: add() — O(1) в среднем, get(i) — O(1), size() — O(1).
    //
    // battleLog записывает ключевые события боя для отображения после сражения.
    private List<String> battleLog;

    // ===== LinkedList<String> — ЖУРНАЛ КВЕСТОВ (5.3) =====
    //
    // LinkedList — двусвязный список. Каждый элемент хранит ссылки на prev и next.
    //
    // LinkedList vs ArrayList:
    //   ArrayList:  быстрый доступ по индексу O(1), медленная вставка в начало O(n).
    //   LinkedList: медленный доступ по индексу O(n), быстрая вставка в начало/конец O(1).
    //
    // LinkedList также реализует Deque — двустороннюю очередь.
    // addFirst() — добавить в начало, addLast() — добавить в конец.
    // peekFirst() — посмотреть первый элемент, peekLast() — посмотреть последний.
    //
    // questLog хранит хронологию событий игры (квестовые записи).
    private LinkedList<String> questLog;

    // ===== Map<EnemyRank, List<LootDrop>> — ТАБЛИЦА ЛУТА (5.8) =====
    //
    // HashMap — коллекция пар «ключ → значение» на основе хеш-таблицы.
    // Ключ: EnemyRank (enum: COMMON, ELITE, BOSS).
    // Значение: List<LootDrop> — список возможных наград для данного ранга.
    //
    // Раньше лут хранился в int[][] lootTable — двумерном массиве.
    // HashMap лучше:
    //   1. Типобезопасность: ключ — enum, нельзя передать невалидный индекс.
    //   2. Читаемость: lootTable.get(EnemyRank.BOSS) вместо lootTable[4].
    //   3. Гибкость: у каждого ранга может быть РАЗНОЕ количество лута.
    private Map<EnemyRank, List<LootDrop>> lootTable;

    // ===== Set<Achievement> — ДОСТИЖЕНИЯ (5.4) =====
    //
    // HashSet — множество уникальных элементов на хеш-таблице.
    // Ключевое свойство Set: НЕ ХРАНИТ ДУБЛИКАТОВ.
    // Если achievements.add(Achievement.FIRST_BLOOD) вызвать дважды,
    // второй вызов вернёт false и ничего не добавит.
    //
    // Это идеально для достижений: каждое засчитывается один раз.
    // Проверка наличия: achievements.contains(Achievement.FIRST_BLOOD) — O(1).
    private Set<Achievement> achievements;

    // ===== TreeSet<BattleRecord> — ТАБЛИЦА РЕКОРДОВ (5.5) =====
    //
    // TreeSet — отсортированное множество на красно-чёрном дереве.
    // Элементы ВСЕГДА упорядочены по compareTo() (BattleRecord реализует Comparable).
    // BattleRecord.compareTo() сортирует по score (убывание) → первый элемент = лучший рекорд.
    //
    // TreeSet vs HashSet:
    //   HashSet — неупорядоченный, O(1) доступ. Для «есть ли элемент?»
    //   TreeSet — отсортированный, O(log n) доступ. Для «топ-5 результатов», «первый/последний».
    private TreeSet<BattleRecord> leaderboard;

    // ===== ArrayDeque<String> — СТЕК ОТМЕНЫ (5.7) =====
    //
    // ArrayDeque — двусторонняя очередь, используемая здесь как СТЕК (LIFO).
    // LIFO = Last In, First Out (последний пришёл — первый ушёл).
    //
    // Методы стека:
    //   push(element) — положить на вершину стека.
    //   pop()         — снять с вершины стека (бросит исключение, если пуст).
    //   peek()        — посмотреть вершину (не снимая).
    //
    // ArrayDeque vs Stack:
    //   Stack — устаревший класс (Java 1.0), наследует Vector, синхронизирован (медленно).
    //   ArrayDeque — современная замена (Java 6+), быстрее, не синхронизирован.
    //   Рекомендация: ВСЕГДА используй ArrayDeque вместо Stack.
    //
    // undoStack сохраняет описания действий перед их выполнением.
    // Если игрок выбирает «Отменить ход» — можно откатить последнее действие.
    private ArrayDeque<String> undoStack;

    // Флаг: использовал ли игрок отмену в текущем бою (разрешена 1 отмена за бой).
    private boolean undoUsedThisBattle;

    // ===== Bestiary — БЕСТИАРИЙ (5.9) =====
    //
    // Bestiary — обёртка над TreeMap<String, BestiaryEntry>.
    // Хранит информацию о побеждённых врагах (количество убийств, макс. урон).
    // Записи отсортированы по имени врага (TreeMap).
    private Bestiary bestiary;

    // ===== ФЛАГ ПОЛУЧЕНИЯ УРОНА В ТЕКУЩЕМ БОЮ (BUG FIX) =====
    //
    // boolean — примитивный тип, хранит true или false.
    // Используется для проверки достижения FLAWLESS: победа без получения урона.
    //
    // Зачем отдельное поле, а не проверка battleLog?
    // Раньше проверяли battleLog.stream().anyMatch(s -> s.contains("получает")),
    // но слово "получает" НИКОГДА не записывалось в лог — враг логируется как "атакует: N урона",
    // яд как "Яд нанёс N урона". Поэтому FLAWLESS давалось ВСЕГДА — баг!
    //
    // Надёжный способ — явный boolean-флаг:
    //   false в начале боя → true при получении любого урона.
    //   Нет урона = false → FLAWLESS заслужен.
    private boolean heroTookDamageThisBattle = false;

    // Счётчик использований спецатаки (для достижения SPECIALIST).
    private int specialAttackCount = 0;

    // Счётчик собранных предметов из лута (для достижения COLLECTOR).
    private int lootItemsCollected = 0;

    // ===== СТАТИЧЕСКИЙ БЛОК ИНИЦИАЛИЗАЦИИ (глава 3.21) =====
    //
    // static { ... } — статический блок инициализации.
    // Выполняется ОДИН РАЗ при ЗАГРУЗКЕ КЛАССА в память (ClassLoader), ДО создания объектов.
    //
    // Порядок инициализации класса Game:
    //   1. static-поля (MAX_ENEMIES=5, GAME_VERSION="2.0", totalGamesPlayed=0) — в порядке объявления.
    //   2. Этот static-блок — выполняется сразу после static-полей.
    //   ---- при вызове new Game(): ----
    //   3. Поля экземпляра (statusFlags=0, gameState=MENU и т.д.).
    //   4. Конструктор Game().
    //
    // Зачем нужен static-блок?
    //   - Для инициализации static-полей, требующей ЛОГИКИ (if/try-catch/циклы).
    //   - Для вывода отладочной информации при загрузке класса.
    //
    // Частая ошибка: static-блок НЕ имеет доступа к полям экземпляра (hero, scanner и т.д.),
    // потому что объект ещё не создан! Только к static-полям и методам.
    static {
        System.out.println("[Game] Версия игры: " + GAME_VERSION + " | Загрузка...");
    }

    // ===== КОНСТРУКТОР (глава 3.2, 3.21) =====
    //
    // Конструктор — специальный метод, вызываемый при создании объекта: new Game().
    // Имя конструктора СОВПАДАЕТ с именем класса. Нет возвращаемого типа (даже void!).
    // public Game() — конструктор без параметров (no-arg constructor).
    //
    // Если не написать конструктор, Java создаст пустой автоматически.
    // Но если написать ХОТЯ БЫ ОДИН конструктор — автоматический НЕ создаётся!
    public Game() {
        // new Scanner(System.in) — создаём Scanner, привязанный к клавиатуре.
        // System.in — стандартный поток ввода (static final поле класса System, тип InputStream).
        //
        // ВАЖНО: этот Scanner намеренно НЕ закрывается (не используем try-with-resources)!
        // Причина: scanner.close() вызывает System.in.close(), а System.in — ГЛОБАЛЬНЫЙ
        // поток ввода, общий для всей программы. После закрытия System.in его НЕВОЗМОЖНО
        // открыть заново — любой new Scanner(System.in) не сможет читать ввод.
        //
        // try-with-resources (глава 4.1) нужен для ФАЙЛОВЫХ и СЕТЕВЫХ Scanner'ов,
        // где ресурс принадлежит конкретному блоку кода:
        //   try (Scanner fileScanner = new Scanner(new File("data.txt"))) { ... }
        //
        // System.in — особый случай: он живёт ВСЮ жизнь программы, управляется JVM.
        // Правило: НЕ закрывайте System.in, System.out и System.err —
        // это глобальные ресурсы, которые закрывает JVM при завершении программы.
        scanner = new Scanner(System.in);

        // new Inventory<>(5) — создаём инвентарь на 5 слотов.
        // <> — diamond operator (Java 7+): компилятор выводит тип Inventory.ItemInfo из поля.
        // Без diamond: new Inventory<Inventory.ItemInfo>(5) — избыточно, тип уже указан слева.
        inventory = new Inventory<>(5);

        // ++ — оператор инкремента. totalGamesPlayed — static-поле, общее для всех объектов Game.
        // Каждый new Game() увеличивает общий счётчик на 1.
        totalGamesPlayed++;

        // ===== ИНИЦИАЛИЗАЦИЯ КОЛЛЕКЦИЙ (глава 5) =====
        //
        // Все коллекции инициализируются ПУСТЫМИ. Данные добавляются позже.
        // new ArrayList<>() — diamond operator (<>): компилятор определяет тип из левой части.
        //
        // ПОЧЕМУ инициализируем в конструкторе, а не при объявлении поля?
        // Можно было: private List<String> battleLog = new ArrayList<>();
        // Оба подхода корректны. Но когда полей много — удобнее видеть всю инициализацию
        // в одном месте (конструкторе), особенно если порядок важен.
        battleLog = new ArrayList<>();
        questLog = new LinkedList<>();
        lootTable = new HashMap<>();
        achievements = new HashSet<>();
        leaderboard = new TreeSet<>();
        undoStack = new ArrayDeque<>();
        bestiary = new Bestiary();

        // ===== АНОНИМНЫЙ КЛАСС — РЕАЛИЗАЦИЯ ИНТЕРФЕЙСА «НА МЕСТЕ» (глава 3.16) =====
        //
        // new BattleEventListener() { ... } — АНОНИМНЫЙ КЛАСС (anonymous class).
        // Создаёт БЕЗЫМЯННЫЙ класс, реализующий интерфейс BattleEventListener.
        //
        // Что происходит за кулисами:
        //   1. Компилятор создаёт скрытый класс Game$1, реализующий BattleEventListener.
        //   2. new BattleEventListener() { ... } — создаёт ЭКЗЕМПЛЯР этого скрытого класса.
        //   3. Внутри { ... } — тело класса: переопределённые методы интерфейса.
        //
        // Зачем анонимный класс?
        //   Когда реализация нужна ТОЛЬКО в одном месте — создавать отдельный файл избыточно.
        //   Анонимный класс — компактный способ реализовать интерфейс «на лету».
        //
        // Альтернатива: если бы BattleEventListener был @FunctionalInterface (ОДИН абстрактный метод),
        // можно было бы использовать ЛЯМБДУ: listener = (attacker, damage, dmg) -> { ... };
        // Но BattleEventListener имеет 3 метода — лямбда не подходит, нужен анонимный класс.
        //
        // @Override — аннотация: подтверждает переопределение метода интерфейса.
        // Если допустить опечатку в имени метода — компилятор выдаст ошибку (защита от багов).
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
    }

    // ===== МЕТОД start() — ТОЧКА ВХОДА В ИГРУ =====
    //
    // public void start() — публичный метод экземпляра.
    //   public — доступен извне (вызывается из Main.java: game.start()).
    //   void   — метод НЕ ВОЗВРАЩАЕТ значения (нет return с результатом).
    //
    // start() — оркестратор: вызывает другие методы в правильном порядке.
    // Это паттерн «Template Method» (упрощённый): высокоуровневый алгоритм
    // описан здесь, а детали реализации — в вызываемых методах.
    public void start() {
        // Конкатенация строк оператором + :
        // "RPG ADVENTURE v" + GAME_VERSION → "RPG ADVENTURE v2.0".
        // + для String — особый оператор, Java вызывает String.concat() или StringBuilder.
        System.out.println("========================================");
        System.out.println("    RPG ADVENTURE v" + GAME_VERSION);
        System.out.println("    Игр сыграно: " + totalGamesPlayed);
        System.out.println("========================================");

        // Присваиваем enum-значение. GameState.EXPLORING — одна из констант enum GameState.
        gameState = GameState.EXPLORING;

        createHero();
        setupInventory();
        setupLootTable();

        // ===== ДЕМОНСТРАЦИЯ СТИРАНИЯ ТИПОВ (Type Erasure, глава 3.31) =====
        //
        // Стирание типов — механизм Java: дженерики существуют ТОЛЬКО во время компиляции.
        // В байт-коде (во время выполнения) Inventory<ItemInfo> и Inventory<String> —
        // это ОДИН И ТОТ ЖЕ класс Inventory (без параметров типа).
        //
        // getClass() — метод класса Object, возвращает объект Class (метаданные о классе).
        // getClass().getName() — полное имя класса: "rpg.Inventory" (без <ItemInfo>!).
        //
        // inventory.getClass() == testInv.getClass() → TRUE!
        // Потому что оба объекта — экземпляры одного класса rpg.Inventory.
        // Параметры типа (<ItemInfo>, <String>) стёрты — JVM их не видит.
        //
        // Следствие: нельзя написать if (obj instanceof Inventory<String>) — тип стёрт!
        // Можно только: if (obj instanceof Inventory<?>) — wildcard «любой тип».
        System.out.println("Тип инвентаря: " + inventory.getClass().getName());
        Inventory<String> testInv = new Inventory<>(1);
        System.out.println("Inventory<ItemInfo> == Inventory<String>? "
                + (inventory.getClass() == testInv.getClass()));
        System.out.println();

        // ===== WILDCARD-МЕТОД В ДЕЙСТВИИ (глава 3.32) =====
        //
        // Inventory.displayAnyInventory(inventory) — статический метод с wildcard-параметром.
        // Сигнатура: static void displayAnyInventory(Inventory<?> inv)
        // Inventory<?> — «инвентарь ЛЮБОГО типа» (? = wildcard, подстановочный знак).
        //
        // Без wildcard: displayAnyInventory(Inventory<ItemInfo> inv) — принимает ТОЛЬКО ItemInfo.
        // С wildcard: displayAnyInventory(Inventory<?> inv) — принимает Inventory<ЛЮБОЙ_ТИП>.
        //
        // Ограничение wildcard: из Inventory<?> НЕЛЬЗЯ добавлять элементы (кроме null),
        // потому что компилятор не знает конкретный тип. Зато можно ЧИТАТЬ как Object.
        Inventory.displayAnyInventory(inventory);
        System.out.println();

        // try-with-resources: загрузка данных врагов из файла (см. loadEnemyData()).
        loadEnemyData();

        // LinkedList.addLast() — добавление в конец двусвязного списка: O(1).
        // (см. подробное объяснение LinkedList в разделе полей выше)
        questLog.addLast("Начало приключения: " + hero.getName() + " отправляется в путь!");

        gameLoop();
        gameState = GameState.GAME_OVER;
        showFinalStats();
    }

    // ===== МЕТОД createHero() — СОЗДАНИЕ ГЕРОЯ =====
    //
    // private — метод доступен ТОЛЬКО внутри класса Game (вызывается из start()).
    // void — ничего не возвращает. Результат работы — инициализация поля hero.
    private void createHero() {
        // ===== readHeroName() — МЕТОД С throws (глава 4.1) =====
        //
        // readHeroName() объявлен с throws InvalidActionException — checked exception.
        // Вызывающий код ОБЯЗАН обработать: try-catch или throws.
        // Здесь используем try-catch с повторным запросом имени.
        //
        // ===== ПАТТЕРН «ЦИКЛ ДО УСПЕШНОГО ВВОДА» =====
        //
        // String name = null — начинаем без имени (null = «ссылка ни на что не указывает»).
        // null — специальное значение для ссылочных типов (String, List, любой класс).
        // Примитивные типы (int, boolean, char) НЕ могут быть null — только объекты.
        //
        // while (name == null) — цикл продолжается, ПОКА name остаётся null.
        // Как только readHeroName() вернёт корректное имя — name != null → цикл завершится.
        //
        // Это стандартный паттерн «цикл с повторным запросом»:
        //   1. Инициализируем переменную «пустым» значением (null/0/false).
        //   2. В цикле пытаемся получить корректное значение.
        //   3. При ошибке (catch) значение остаётся пустым → цикл повторяется.
        //   4. При успехе значение устанавливается → условие цикла становится false.
        String name = null;
        while (name == null) {
            try {
                // readHeroName() может бросить InvalidActionException
                // если имя пустое, слишком длинное или содержит спецсимволы.
                name = readHeroName();
            } catch (InvalidActionException e) {
                // catch (InvalidActionException e) — перехватываем конкретное исключение.
                // e.getMessage() — текстовое описание ошибки (из конструктора исключения).
                System.out.println("Ошибка: " + e.getMessage());
                System.out.println("Попробуйте ещё раз.");
            }
        }

        // ===== do-while — ЦИКЛ С ПОСТУСЛОВИЕМ (глава 2.6) =====
        //
        // do { тело } while (условие); — выполняет тело ХОТЯ БЫ ОДИН РАЗ,
        // затем проверяет условие. Если true — повторяет, если false — выходит.
        //
        // do-while vs while:
        //   while — сначала проверяет условие, может не выполниться ни разу.
        //   do-while — сначала выполняет тело, потом проверяет. Гарантирует минимум 1 итерацию.
        //
        // Идеален для ввода с валидацией: нужно спросить хотя бы раз, потом проверить.
        // Частая ошибка: забыть ; (точку с запятой) после while (условие);
        int choice;
        do {
            System.out.println("Выберите класс:");
            System.out.println("  1. Воин");
            System.out.println("  2. Маг");
            System.out.println("  3. Лучник");
            System.out.print("Ваш выбор: ");
            choice = readInt();
        } while (choice < 1 || choice > 3);
        // choice < 1 || choice > 3 — условие продолжения цикла do-while.
        // || — логическое ИЛИ: цикл повторяется если choice МЕНЬШЕ 1 ИЛИ БОЛЬШЕ 3.
        // То есть допустимые значения: 1, 2 или 3 — только они завершат цикл.

        // ===== switch-ВЫРАЖЕНИЕ (Java 14+) =====
        //
        // hero = switch (choice) { ... }; — switch как ВЫРАЖЕНИЕ, возвращающее значение.
        // В отличие от switch-оператора (statement), switch-выражение:
        //   1. Возвращает значение (можно присвоить переменной).
        //   2. Использует -> (стрелочный синтаксис) — НЕТ «проваливания» (fall-through).
        //   3. Не нужен break — каждый case выполняет только своё действие.
        //   4. Компилятор требует обработать ВСЕ варианты (exhaustiveness check).
        //
        // Здесь switch возвращает объект-потомок GameCharacter (полиморфизм).
        // hero — тип GameCharacter, но хранит Warrior, Mage или Archer.
        hero = switch (choice) {
            case 1 -> new Warrior(name);
            case 2 -> new Mage(name);
            case 3 -> new Archer(name);
            // default — для int компилятор не знает все варианты, поэтому default обязателен.
            // Для enum с покрытием всех констант — default НЕ нужен.
            default -> new Warrior(name);
        };

        System.out.println();
        System.out.println(hero.getBattleCry());
        System.out.println(hero);
        System.out.println("Всего персонажей создано: " + GameCharacter.getCharacterCount());
        System.out.println("────────────────────────────────────────");
        System.out.println();
    }

    // ===== readHeroName() — МЕТОД С throws (глава 4.1, 4.3) =====
    //
    // throws InvalidActionException — ОБЪЯВЛЕНИЕ В СИГНАТУРЕ.
    // Это объявление сообщает вызывающему коду:
    //   «Этот метод МОЖЕТ бросить InvalidActionException.
    //    Вы ОБЯЗАНЫ обработать это исключение (try-catch или пробросить дальше).»
    //
    // Для checked exceptions (наследники Exception, но не RuntimeException)
    // объявление throws ОБЯЗАТЕЛЬНО — без него компилятор выдаст ошибку.
    //
    // Для unchecked exceptions (наследники RuntimeException) throws НЕ НУЖЕН,
    // хотя можно добавить для документации.
    private String readHeroName() throws InvalidActionException {
        System.out.print("Введите имя героя: ");
        String name = scanner.nextLine().trim();

        // Валидация: пустое имя.
        if (name.isEmpty()) {
            // throw — бросаем исключение. Выполнение метода ПРЕКРАЩАЕТСЯ.
            // Управление передаётся ближайшему catch-блоку в цепочке вызовов.
            throw new InvalidActionException("ввод имени", "имя не может быть пустым");
        }

        // Валидация: слишком длинное имя (больше 20 символов).
        if (name.length() > 20) {
            throw new InvalidActionException("ввод имени",
                    "имя слишком длинное (" + name.length() + " символов, максимум 20)");
        }

        // Валидация: спецсимволы. Разрешены только буквы, пробелы и дефисы.
        // String.matches(regex) — проверяет, соответствует ли строка регулярному выражению.
        // [a-zA-Zа-яА-ЯёЁ\\s-]+ — буквы (латиница + кириллица), пробелы, дефисы.
        if (!name.matches("[a-zA-Zа-яА-ЯёЁ\\s-]+")) {
            throw new InvalidActionException("ввод имени",
                    "имя содержит недопустимые символы (разрешены только буквы, пробелы и дефисы)");
        }

        return name;
    }

    // ===== МЕТОД setupInventory() =====

    private void setupInventory() {
        // ===== try-catch ДЛЯ CHECKED EXCEPTION (глава 4.1) =====
        //
        // Inventory.addItem() теперь бросает InventoryFullException (checked).
        // Вызывающий код ОБЯЗАН обработать — оборачиваем в try-catch.
        //
        // В setupInventory() инвентарь гарантированно не полон (мы только что создали
        // инвентарь на 5 слотов и добавляем 2 предмета), но компилятор этого не знает
        // и требует обработки checked exception.
        try {
            inventory.addItem(new Inventory.ItemInfo("Зелье здоровья", 30), 3);
            inventory.addItem(new Inventory.ItemInfo("Малое зелье здоровья", 5), 1);
        } catch (InventoryFullException e) {
            // Эта ситуация маловероятна при инициализации, но мы обязаны обработать.
            System.out.println("Предупреждение: " + e.getMessage());
        }
        inventory.display();
        System.out.println();
    }

    // ===== setupLootTable() — ЗАПОЛНЕНИЕ ТАБЛИЦЫ ЛУТА (глава 5.8) =====
    //
    // Заполняем HashMap<EnemyRank, List<LootDrop>> — таблицу наград за победу.
    // Каждый ранг врага (COMMON, ELITE, BOSS) имеет свой список возможных наград.
    private void setupLootTable() {
        // ===== List.of() — НЕИЗМЕНЯЕМЫЙ СПИСОК (Java 9+) =====
        //
        // List.of(elements...) — создаёт НЕИЗМЕНЯЕМЫЙ (immutable) список.
        // Нельзя добавлять/удалять элементы: list.add() → UnsupportedOperationException.
        // Подходит для константных данных, которые не меняются после создания.
        //
        // ===== Map.put(key, value) — ДОБАВЛЕНИЕ ПАРЫ В MAP =====
        //
        // put(key, value) — добавляет пару ключ-значение.
        // Если ключ уже существует — ПЕРЕЗАПИСЫВАЕТ значение (не добавляет дубликат).
        // Возвращает предыдущее значение (или null, если ключа не было).

        // Лут для обычных врагов (COMMON) — немного золота, мелкие предметы.
        lootTable.put(EnemyRank.COMMON, List.of(
                new LootDrop("Малое зелье здоровья", 10, 0, 0.7),
                new LootDrop("Кусок руды", 5, 0, 0.3)
        ));

        // Лут для элитных врагов (ELITE) — больше наград, есть самоцветы.
        lootTable.put(EnemyRank.ELITE, List.of(
                new LootDrop("Зелье здоровья", 25, 1, 0.8),
                new LootDrop("Свиток силы", 15, 2, 0.5),
                new LootDrop("Эликсир маны", 20, 1, 0.4)
        ));

        // Лут для боссов (BOSS) — щедрые награды, редкие предметы.
        lootTable.put(EnemyRank.BOSS, List.of(
                new LootDrop("Великое зелье", 100, 5, 1.0),
                new LootDrop("Легендарный артефакт", 50, 10, 0.3),
                new LootDrop("Сокровище дракона", 200, 15, 0.2)
        ));
    }

    // ===== loadEnemyData() — try-with-resources ПРИМЕР (глава 4.2) =====
    //
    // try-with-resources — конструкция для АВТОМАТИЧЕСКОГО закрытия ресурсов.
    //
    // Ресурс (resource) — объект, который нужно ЗАКРЫТЬ после использования:
    //   Scanner, FileInputStream, Connection, BufferedReader и т.д.
    //   Все они реализуют интерфейс AutoCloseable (метод close()).
    //
    // Синтаксис:
    //   try (Resource res = new Resource()) {
    //       // Использовать ресурс
    //   } catch (Exception e) {
    //       // Обработка ошибки
    //   }
    //   // res.close() вызывается АВТОМАТИЧЕСКИ при выходе из блока try!
    //
    // Без try-with-resources:
    //   Scanner s = null;
    //   try {
    //       s = new Scanner(file);
    //       // ...
    //   } finally {
    //       if (s != null) s.close();  // Нужно закрывать вручную!
    //   }
    //
    // ВАЖНО: НЕ используем try-with-resources для this.scanner (Scanner(System.in)),
    // потому что это закроет System.in — стандартный ввод нельзя закрывать!
    // Здесь создаём ОТДЕЛЬНЫЙ Scanner для чтения файла.
    private void loadEnemyData() {
        // File — класс, представляющий путь к файлу (НЕ содержимое файла!).
        // new File("enemy_data.txt") — создаёт объект File (файл может не существовать).
        File dataFile = new File("enemy_data.txt");

        // try (Scanner fileScanner = new Scanner(dataFile)) — try-with-resources.
        // Scanner(File) — конструктор, который МОЖЕТ бросить FileNotFoundException.
        // FileNotFoundException — checked exception (наследник IOException).
        // Компилятор требует обработки: try-catch или throws.
        //
        // Ресурс fileScanner будет АВТОМАТИЧЕСКИ закрыт при выходе из try-блока:
        //   - При нормальном завершении.
        //   - При исключении (перед выполнением catch).
        //   - При return или break внутри try.
        try (Scanner fileScanner = new Scanner(dataFile)) {
            System.out.println("[Данные] Загрузка данных врагов из файла...");
            // while (fileScanner.hasNextLine()) — цикл чтения строк из файла.
            // hasNextLine() — возвращает true, если в Scanner'е есть ещё строки для чтения.
            // Когда файл исчерпан → hasNextLine() вернёт false → цикл завершится.
            // nextLine() — читает и возвращает следующую строку (до символа переноса строки).
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                System.out.println("  Загружено: " + line);
            }
            System.out.println("[Данные] Загрузка завершена.");
        } catch (FileNotFoundException e) {
            // FileNotFoundException — файл не найден. Это НОРМАЛЬНАЯ ситуация:
            // файл данных необязателен, используем хардкод.
            System.out.println("[Данные] Файл данных не найден. Используются встроенные данные.");
            // e.getMessage() содержит путь к файлу и причину: "enemy_data.txt (No such file or directory)"
        }
        // fileScanner.close() вызван АВТОМАТИЧЕСКИ — не нужно писать finally!
    }

    // ===== МЕТОД gameLoop() — ГЛАВНЫЙ ИГРОВОЙ ЦИКЛ =====

    private void gameLoop() {
        // ===== List<Enemy> ВМЕСТО ПАРАЛЛЕЛЬНЫХ МАССИВОВ (глава 5.1, 5.2) =====
        //
        // РАНЬШЕ: 5 параллельных массивов (enemyNames[], enemyHealth[], enemyAttack[], enemyExp[], enemyRanks[]).
        //   Проблемы:
        //     1. Нужно синхронизировать индексы вручную — легко ошибиться.
        //     2. Нет типобезопасности — индекс 10 при размере 5 → ArrayIndexOutOfBoundsException.
        //     3. Нельзя передать «одного врага» в метод — нужно передавать 5 отдельных значений.
        //
        // ТЕПЕРЬ: List<Enemy> enemies — один список объектов Enemy.
        //   Преимущества:
        //     1. Все данные врага — в одном объекте (инкапсуляция).
        //     2. Динамический размер: можно добавлять/удалять врагов.
        //     3. Типобезопасность: нельзя добавить строку в список врагов.
        //     4. Богатый API: size(), get(), for-each, sort(), и т.д.
        //
        // new ArrayList<>(MAX_ENEMIES) — создаём ArrayList с начальной ёмкостью MAX_ENEMIES.
        // Указание начальной ёмкости — оптимизация: ArrayList не будет расширять внутренний
        // массив при добавлении (расширение стоит O(n) — копирование всех элементов).
        List<Enemy> enemies = new ArrayList<>(MAX_ENEMIES);

        // ===== List.add() — ДОБАВЛЕНИЕ ЭЛЕМЕНТОВ (5.2) =====
        //
        // add(element) — добавляет элемент в конец списка. Возвращает true.
        // Время: O(1) в среднем (иногда O(n) при расширении массива).
        enemies.add(new Enemy("Гоблин", 30, 5, 20, EnemyRank.COMMON));
        enemies.add(new Enemy("Скелет", 40, 8, 30, EnemyRank.COMMON));
        enemies.add(new Enemy("Орк", 60, 12, 50, EnemyRank.ELITE));
        enemies.add(new Enemy("Тролль", 80, 15, 70, EnemyRank.ELITE));
        enemies.add(new Enemy("Дракон", 120, 25, 100, EnemyRank.BOSS));

        // ===== Collections.sort() + Comparable (глава 5.5) =====
        //
        // Collections.sort(list) — сортирует список по «естественному порядку» (compareTo).
        // Enemy.compareTo() сортирует по мощи (health + attack): слабые первыми.
        // Это демонстрация Comparable в действии.
        Collections.sort(enemies);
        System.out.println("[Сортировка] Враги отсортированы по силе (Comparable):");
        for (Enemy e : enemies) {
            System.out.println("  " + e + " (мощь: " + (e.getHealth() + e.getAttack()) + ")");
        }
        System.out.println();

        // ===== Comparator — АЛЬТЕРНАТИВНАЯ СОРТИРОВКА (глава 5.6) =====
        //
        // Comparator<Enemy> — интерфейс для определения КАСТОМНОГО порядка сортировки.
        // В отличие от Comparable (один порядок, встроенный в класс),
        // Comparator позволяет создавать ЛЮБОЕ количество порядков ИЗВНЕ.
        //
        // Comparator.comparing(Enemy::getName) — создаёт Comparator, который
        // сортирует по результату метода getName() (лексикографически по имени).
        //
        // Enemy::getName — ссылка на метод (method reference, Java 8+).
        // Это эквивалент лямбды: (enemy) -> enemy.getName()
        // :: — оператор ссылки на метод. Читается: «метод getName класса Enemy».
        Comparator<Enemy> byName = Comparator.comparing(Enemy::getName);
        // Создаём копию списка и сортируем по имени (для демонстрации Comparator).
        List<Enemy> sortedByName = new ArrayList<>(enemies);
        sortedByName.sort(byName);
        System.out.println("[Сортировка] Враги по алфавиту (Comparator):");
        for (Enemy e : sortedByName) {
            System.out.println("  " + e);
        }
        System.out.println();

        // ===== Comparator ПО ОПЫТУ (убывание) =====
        //
        // Comparator.comparing(keyExtractor).reversed() — сортировка по ключу В ОБРАТНОМ порядке.
        // .reversed() — инвертирует порядок Comparator (убывание вместо возрастания).
        Comparator<Enemy> byExpDesc = Comparator.comparing(Enemy::getExpReward).reversed();
        List<Enemy> sortedByExp = new ArrayList<>(enemies);
        sortedByExp.sort(byExpDesc);
        System.out.println("[Сортировка] Враги по опыту (убывание, Comparator):");
        for (Enemy e : sortedByExp) {
            System.out.println("  " + e + " (опыт: " + e.getExpReward() + ")");
        }
        System.out.println();

        int enemyIndex = 0;

        // ===== for-each vs индексный while =====
        //
        // Можно было бы использовать for-each: for (Enemy enemy : enemies) { ... }
        // Но нам нужен индекс для отображения номера врага и для break,
        // поэтому оставляем while с индексом.
        //
        // while (условие) — цикл с предусловием (глава 2.6).
        // Проверяет условие ПЕРЕД каждой итерацией. Если изначально false — не выполняется.
        //
        // hero.isAlive() — метод GameCharacter: возвращает true, если hero.getHealth() > 0.
        // enemyIndex < enemies.size() — enemies.size() — количество элементов в ArrayList.
        //   Начинаем с 0, каждый бой увеличиваем на 1, до достижения размера списка.
        //
        // && — оба условия ОБЯЗАТЕЛЬНЫ: бой идёт, пока и герой жив, и враги ещё есть.
        while (hero.isAlive() && enemyIndex < enemies.size()) {
            System.out.println("\n════════════════════════════════════════");
            System.out.println("  Враг " + (enemyIndex + 1) + " из " + enemies.size());
            System.out.println("════════════════════════════════════════");

            // ===== List.get(index) — ДОСТУП ПО ИНДЕКСУ (5.2) =====
            //
            // get(index) — возвращает элемент по индексу. Время: O(1) для ArrayList.
            // Бросает IndexOutOfBoundsException, если index < 0 или index >= size().
            Enemy enemy = enemies.get(enemyIndex);

            // ===== КЛОНИРОВАНИЕ ОБЪЕКТА — ДЕМОНСТРАЦИЯ clone() (глава 3.13) =====
            //
            // clone() — метод интерфейса Cloneable, создаёт КОПИЮ объекта.
            // Оригинал и клон — ДВА РАЗНЫХ объекта в памяти (разные ссылки),
            // но с ОДИНАКОВЫМИ значениями полей на момент клонирования.
            //
            // Проверка (enemy != cloned) доказывает, что это разные объекты:
            //   - оператор == для объектов сравнивает ССЫЛКИ (адреса в памяти), не содержимое.
            //   - два разных объекта с одинаковыми полями: == вернёт false.
            //
            // Проверка одинаковых характеристик показывает, что данные скопированы верно.
            // После клонирования изменение оригинала НЕ влияет на клон (и наоборот) —
            // это называется «глубокая независимость копий».
            Enemy cloned = enemy.clone();
            System.out.println("[Клон] Оригинал: " + enemy.getName()
                    + " (HP=" + enemy.getHealth() + ", ATK=" + enemy.getAttack() + ")");
            System.out.println("[Клон] Копия:    " + cloned.getName()
                    + " (HP=" + cloned.getHealth() + ", ATK=" + cloned.getAttack() + ")");
            System.out.println("[Клон] Это разные объекты? " + (enemy != cloned));

            System.out.println("\nПоявляется: " + enemy);
            System.out.println();

            // Сброс battleLog для нового боя.
            // Присваиваем новый ArrayList<>() — старый список будет удалён сборщиком мусора (GC),
            // когда на него не останется ссылок. Это чище, чем battleLog.clear():
            // clear() оставляет тот же объект, а new ArrayList<>() гарантирует чистый старт.
            battleLog = new ArrayList<>();

            boolean fled = battle(enemy);

            // !fled && !enemy.isAlive() — герой не сбежал И враг мёртв → победа в бою.
            // ! — логическое НЕ (отрицание): инвертирует boolean-значение.
            //   !fled == true, если fled == false (герой остался).
            //   !enemy.isAlive() == true, если isAlive() == false (враг мёртв).
            // && — оба условия ОБЯЗАТЕЛЬНЫ: засчитываем победу только при полной победе.
            if (!fled && !enemy.isAlive()) {
                // ++ — постфиксный инкремент: увеличивает enemiesDefeated на 1.
                // Эквивалентно: enemiesDefeated = enemiesDefeated + 1.
                enemiesDefeated++;
                // hero.addExperience() — возвращает true при повышении уровня.
                // boolean leveledUp — сохраняем результат, чтобы передать в callback.
                boolean leveledUp = hero.addExperience(enemy.getExpReward());

                if (listener != null) {
                    listener.onEnemyDefeated(hero, enemy.getName(), enemy.getExpReward());
                }

                if (leveledUp && listener != null) {
                    listener.onLevelUp(hero, hero.getLevel());
                }

                System.out.println(hero.getName() + " получает " + enemy.getExpReward() + " опыта!");

                // ===== ПОЛУЧЕНИЕ ЛУТА ИЗ HashMap (глава 5.8) =====
                //
                // lootTable.get(key) — получает значение по ключу.
                // Возвращает null, если ключ не найден.
                // lootTable.getOrDefault(key, defaultValue) — безопаснее: возвращает default если нет ключа.
                handleLoot(enemy);

                // Добавляем запись в бестиарий (5.9).
                bestiary.addEntry(enemy.getName(), enemy.getRank(), damageDealtThisBattle);

                // Добавляем в журнал квестов (5.3).
                // addLast() — добавление в конец списка (хронологический порядок).
                questLog.addLast("Победа над " + enemy.getName()
                        + " (бой " + (enemyIndex + 1) + ")");

                // addFirst() — добавление в начало списка: O(1) для LinkedList.
                // Используем для "закреплённых" записей — последняя победа всегда наверху.
                // Это демонстрирует ключевое преимущество LinkedList:
                // вставка в начало — O(1), тогда как у ArrayList — O(n) (сдвиг всех элементов).
                questLog.addFirst("★ Последняя победа: " + enemy.getName() + "!");

                // ===== ПРОВЕРКА ДОСТИЖЕНИЙ ПОСЛЕ БОЯ (5.4) =====
                checkAchievements(enemy);

                // Показ лога боя (5.2).
                if (!battleLog.isEmpty()) {
                    System.out.println("\n--- Лог боя ---");
                    // ===== for-each ПО List (5.2) =====
                    //
                    // for (String event : battleLog) — for-each по ArrayList.
                    // На каждой итерации event получает следующий элемент списка.
                    // Компилятор превращает это в вызовы iterator().hasNext() / next().
                    for (String event : battleLog) {
                        System.out.println("  " + event);
                    }
                    System.out.println("--- Конец лога ---");
                }

                System.out.println();
            }

            // !hero.isAlive() — герой погиб (HP <= 0).
            // ! — логическое НЕ: !true == false, !false == true.
            // isAlive() возвращает true если HP > 0, значит !isAlive() == true при HP <= 0.
            if (!hero.isAlive()) {
                System.out.println(hero.getName() + " погиб...");
                // break — немедленный выход из цикла while (прерывает ближайший цикл).
                // Без break продолжили бы бой с уже мёртвым героем — баг.
                break;
            }

            // enemies.size() - 1 — индекс ПОСЛЕДНЕГО врага.
            // Меню между боями показываем ТОЛЬКО если есть следующий враг.
            // Если enemyIndex == enemies.size() - 1, это последний бой → меню не нужно.
            if (enemyIndex < enemies.size() - 1) {
                // ===== МЕНЮ МЕЖДУ БОЯМИ (глава 5.9) =====
                //
                // Новое меню с доступом к бестиарию, достижениям и журналу квестов.
                boolean continueGame = betweenBattlesMenu();
                if (!continueGame) {
                    System.out.println("Вы покидаете поле боя.");
                    break;
                }
            }

            enemyIndex++;
        }

        if (hero.isAlive() && enemiesDefeated == MAX_ENEMIES) {
            System.out.println("========================================");
            System.out.println("  ПОЗДРАВЛЯЕМ! Все враги повержены!");
            System.out.println("========================================");
            // Достижение FULL_CLEAR.
            achievements.add(Achievement.FULL_CLEAR);
        }
    }

    // ===== betweenBattlesMenu() — МЕНЮ МЕЖДУ БОЯМИ (глава 5.9) =====
    //
    // Новое меню с доступом к различным системам игры.
    // Возвращает boolean: true — продолжить, false — выйти.
    //
    // Паттерн «while(true) + return»:
    //   Бесконечный цикл, который завершается ТОЛЬКО через return.
    //   Это стандартный приём для интерактивных меню:
    //   - Цикл показывает меню снова и снова.
    //   - return true/false — ЕДИНСТВЕННЫЙ способ выйти из метода и цикла.
    //   - Альтернатива: boolean flag + while(!done) — но return проще и яснее.
    private boolean betweenBattlesMenu() {
        // while (true) — бесконечный цикл. Выход — через return внутри switch.
        // Частая ошибка: забыть return/break → бесконечный цикл (зависание).
        while (true) {
            System.out.println("\n────── Что делать? ──────");
            System.out.println("  1. Продолжить");
            System.out.println("  2. Бестиарий");
            System.out.println("  3. Достижения");
            System.out.println("  4. Журнал");
            System.out.println("  5. Покинуть");
            System.out.print("Ваш выбор: ");
            int choice = readInt();

            // switch-ОПЕРАТОР со стрелочным синтаксисом (Java 14+).
            // Здесь switch используется как ОПЕРАТОР (statement), а не выражение,
            // потому что мы не присваиваем результат (в отличие от createHero()).
            // -> синтаксис всё равно полезен: нет fall-through, не нужен break.
            // { } блоки нужны для case с несколькими действиями (return + другие).
            switch (choice) {
                case 1 -> {
                    return true;
                }
                case 2 -> {
                    // Показать бестиарий (5.9).
                    bestiary.display();
                }
                case 3 -> {
                    // Показать достижения (5.4).
                    showAchievements();
                }
                case 4 -> {
                    // Показать журнал квестов (5.3).
                    showQuestLog();
                }
                case 5 -> {
                    return false;
                }
                default -> System.out.println("Неверный выбор.");
            }
        }
    }

    // ===== handleLoot() — ОБРАБОТКА ЛУТА (глава 5.8) =====
    //
    // Получение награды за победу над врагом из таблицы лута (HashMap).
    private void handleLoot(Enemy enemy) {
        // ===== Map.get(key) — ПОЛУЧЕНИЕ ЗНАЧЕНИЯ ПО КЛЮЧУ =====
        //
        // get(key) возвращает значение для ключа, или null если ключа нет.
        // getOrDefault(key, default) — возвращает default вместо null.
        List<LootDrop> possibleLoot = lootTable.getOrDefault(enemy.getRank(), List.of());

        if (possibleLoot.isEmpty()) {
            System.out.println("Враг не оставил лута.");
            return;
        }

        System.out.println("Награда:");
        int totalGold = 0;
        int totalGems = 0;

        // Проверяем каждый возможный лут по шансу выпадения.
        for (LootDrop drop : possibleLoot) {
            // Math.random() возвращает случайное double [0.0, 1.0).
            // Если случайное число < шанса — лут выпал.
            if (Math.random() < drop.chance()) {
                System.out.println("  + " + drop.itemName()
                        + " (" + drop.gold() + " золота, " + drop.gems() + " самоцветов)");
                totalGold += drop.gold();
                totalGems += drop.gems();
                lootItemsCollected++;

                // Пробуем добавить предмет в инвентарь, если это зелье.
                if (drop.itemName().contains("зелье") || drop.itemName().contains("Зелье")) {
                    try {
                        inventory.addItem(
                                new Inventory.ItemInfo(drop.itemName(), drop.gold() / 2),
                                1
                        );
                        System.out.println("    → добавлено в инвентарь!");
                    } catch (InventoryFullException e) {
                        // InventoryFullException — checked, обработка обязательна.
                        // Инвентарь полон — предмет не добавлен, но это не критично.
                        System.out.println("    → инвентарь полон, предмет утерян.");
                    }
                }
            }
        }

        if (totalGold > 0 || totalGems > 0) {
            // ===== ТЕРНАРНЫЙ ОПЕРАТОР (глава 2.4) =====
            //
            // условие ? значение_если_true : значение_если_false
            // Это компактная альтернатива if-else для ВЫРАЖЕНИЙ (когда нужно значение).
            // Здесь: если есть самоцветы — показываем оба, иначе — только золото.
            String reward = totalGems > 0
                    ? totalGold + " золота и " + totalGems + " самоцветов"
                    : totalGold + " золота";
            System.out.println("  Итого: " + reward);
        } else {
            System.out.println("  Увы, ничего ценного не выпало.");
        }
    }

    // ===== checkAchievements() — ПРОВЕРКА ДОСТИЖЕНИЙ (глава 5.4) =====
    //
    // Проверяет условия для каждого достижения после боя.
    // Set.add() возвращает true, если элемент был добавлен (не был ранее),
    // и false, если уже был в множестве (дубликат).
    private void checkAchievements(Enemy enemy) {
        // ===== ПАТТЕРН: Set.add() + ArrayList ДЛЯ ОТСЛЕЖИВАНИЯ НОВЫХ ЭЛЕМЕНТОВ =====
        //
        // Запоминаем новые достижения в отдельный ArrayList.
        // HashSet не имеет понятия «последний добавленный» — порядок не гарантирован.
        // Поэтому собираем новые в ArrayList, чтобы показать пользователю ТОЛЬКО их.
        //
        // Set.add() возвращает boolean:
        //   true  — элемент ДОБАВЛЕН (его раньше не было в множестве).
        //   false — элемент УЖЕ БЫЛ (дубликат, множество не изменилось).
        // Используем эту особенность: если add() вернул true — достижение новое.
        List<Achievement> newlyUnlocked = new ArrayList<>();

        // FIRST_BLOOD — первая победа.
        if (enemiesDefeated == 1) {
            if (achievements.add(Achievement.FIRST_BLOOD)) {
                newlyUnlocked.add(Achievement.FIRST_BLOOD);
            }
        }

        // FLAWLESS — победил без получения урона в текущем бою.
        //
        // ===== Stream API + лямбда (предпросмотр глав 5+) =====
        //
        // battleLog.stream() — создаёт поток (Stream) из списка строк.
        // Stream API (Java 8+) — конвейер обработки коллекций.
        //
        // .anyMatch(s -> s.contains("...")) — терминальная операция:
        //   возвращает true, если ХОТЯ БЫ ОДИН элемент удовлетворяет условию.
        //   s -> s.contains("...") — лямбда-выражение (анонимная функция).
        //   s — параметр (каждая строка из battleLog).
        //   -> — разделитель параметров и тела лямбды.
        //   s.contains("...") — тело: проверяет, содержит ли строка подстроку.
        //
        // ВАЖНО: раньше здесь проверялось s.contains("получает"), но это слово никогда
        // не записывалось в battleLog (враг логируется как "атакует: N урона",
        // яд как "Яд нанёс N урона"). Поэтому условие всегда было true → баг!
        //
        // Исправление: используем boolean-поле heroTookDamageThisBattle,
        // которое устанавливается в true при получении любого урона (атака врага, яд).
        // Это НАДЁЖНЕЕ, чем парсить строки лога — данные хранятся в чётком типе boolean.
        //
        // Пример Stream API оставлен для обучения:
        //   long attackCount = battleLog.stream().filter(s -> s.contains("атакует")).count();
        //   (count() — терминальная операция, возвращает количество элементов в потоке)
        if (!heroTookDamageThisBattle) {
            if (achievements.add(Achievement.FLAWLESS)) {
                newlyUnlocked.add(Achievement.FLAWLESS);
            }
        }

        // DRAGON_SLAYER — победить босса.
        // enemy.getRank() == EnemyRank.BOSS — сравнение enum через ==.
        // Для enum == ПРАВИЛЬНЫЙ способ сравнения (не equals())!
        // Каждая константа enum — единственный объект в памяти (singleton),
        // поэтому == сравнивает ссылки и работает корректно.
        // Для String == сравнивает ссылки (плохо), но для enum == гарантированно верно.
        if (enemy.getRank() == EnemyRank.BOSS) {
            if (achievements.add(Achievement.DRAGON_SLAYER)) {
                newlyUnlocked.add(Achievement.DRAGON_SLAYER);
            }
        }

        // SURVIVOR — выжить с менее чем 10% HP.
        // hero.getMaxHealth() / 10 — целочисленное деление: 100 / 10 = 10 (без дробной части).
        // Если maxHealth = 100, то 10% = 10. Условие: health <= 10.
        if (hero.isAlive() && hero.getHealth() <= hero.getMaxHealth() / 10) {
            if (achievements.add(Achievement.SURVIVOR)) {
                newlyUnlocked.add(Achievement.SURVIVOR);
            }
        }

        // COLLECTOR — собрать 3+ предметов из лута.
        if (lootItemsCollected >= 3) {
            if (achievements.add(Achievement.COLLECTOR)) {
                newlyUnlocked.add(Achievement.COLLECTOR);
            }
        }

        // SPECIALIST — использовать 5+ спецатак.
        if (specialAttackCount >= 5) {
            if (achievements.add(Achievement.SPECIALIST)) {
                newlyUnlocked.add(Achievement.SPECIALIST);
            }
        }

        // Уведомление о новых достижениях.
        // Показываем ТОЛЬКО новые, а не весь HashSet — иначе при каждом бое
        // игрок видит все старые достижения заново. HashSet не имеет порядка,
        // поэтому мы собрали новые в ArrayList (где порядок гарантирован).
        if (!newlyUnlocked.isEmpty()) {
            System.out.println("\n*** НОВОЕ ДОСТИЖЕНИЕ! ***");
            for (Achievement a : newlyUnlocked) {
                System.out.println("  " + a);
            }
            // achievements.size() — количество полученных достижений (размер HashSet).
            // Achievement.values().length — общее количество констант в enum Achievement.
            // Подробное объяснение values().length — в методе showAchievements().
            System.out.println("  Всего достижений: " + achievements.size()
                    + "/" + Achievement.values().length);
        }
    }

    // ===== showAchievements() — ОТОБРАЖЕНИЕ ДОСТИЖЕНИЙ (глава 5.4) =====
    //
    // private — доступен ТОЛЬКО внутри класса Game. Внешний код не вызовет этот метод.
    // void — метод ничего не возвращает (только выводит информацию на экран).
    // Принцип инкапсуляции: детали отображения скрыты от остального кода.
    private void showAchievements() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║           ДОСТИЖЕНИЯ                ║");
        System.out.println("╠══════════════════════════════════════╣");

        if (achievements.isEmpty()) {
            System.out.println("║  Пока нет достижений.               ║");
        } else {
            // ===== for-each ПО Set (5.4) =====
            //
            // for (Achievement a : achievements) — обход HashSet.
            // ВНИМАНИЕ: порядок обхода HashSet НЕ ОПРЕДЕЛЁН!
            // При каждом запуске порядок может быть разным.
            // Если нужен порядок — используй TreeSet или LinkedHashSet.
            for (Achievement a : achievements) {
                System.out.println("║  " + a);
            }
        }

        System.out.println("║                                      ║");
        // ===== enum.values() — ПОЛУЧЕНИЕ ВСЕХ КОНСТАНТ ENUM (глава 3.8) =====
        //
        // Achievement.values() — статический метод, автоматически генерируемый для каждого enum.
        // Возвращает массив Achievement[] со ВСЕМИ константами enum в порядке объявления.
        // .length — длина массива (количество констант в enum).
        //
        // Здесь используется для подсчёта общего числа достижений:
        //   achievements.size() — сколько получено (размер HashSet).
        //   Achievement.values().length — сколько всего существует.
        System.out.println("║  Разблокировано: " + achievements.size()
                + "/" + Achievement.values().length);
        System.out.println("╚══════════════════════════════════════╝");
    }

    // ===== showQuestLog() — ЖУРНАЛ КВЕСТОВ (глава 5.3) =====
    //
    // Демонстрирует возможности LinkedList как двусвязного списка.
    private void showQuestLog() {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║          ЖУРНАЛ КВЕСТОВ              ║");
        System.out.println("╠══════════════════════════════════════╣");

        if (questLog.isEmpty()) {
            System.out.println("║  Журнал пуст.                       ║");
        } else {
            // ===== LinkedList peekFirst() / peekLast() (5.3) =====
            //
            // peekFirst() — возвращает ПЕРВЫЙ элемент, НЕ удаляя его. Возвращает null если пуст.
            // peekLast()  — возвращает ПОСЛЕДНИЙ элемент, НЕ удаляя его.
            //
            // В отличие от getFirst()/getLast(), которые бросают NoSuchElementException при пустом списке,
            // peek-методы безопасно возвращают null.
            System.out.println("║  Первое событие: " + questLog.peekFirst());
            System.out.println("║  Последнее событие: " + questLog.peekLast());
            System.out.println("║  ──────────────────────────────────");

            // for-each по LinkedList — обход от первого к последнему.
            int num = 1;
            for (String entry : questLog) {
                System.out.println("║  " + num + ". " + entry);
                num++;
            }
        }

        System.out.println("║  Всего записей: " + questLog.size());
        System.out.println("╚══════════════════════════════════════╝");
    }

    // ===== МЕТОД battle() — БОЕВОЙ ЦИКЛ =====
    //
    // Главный метод боевой системы. Организует пошаговый бой между героем и врагом.
    //
    // private — вызывается только из gameLoop() внутри Game.
    // boolean — возвращаемый тип: true = герой сбежал, false = бой завершён (победа/поражение).
    //
    // Демонстрирует:
    //   - PriorityQueue с локальным record (система инициативы)
    //   - assert для проверки переходов конечного автомата GameState
    //   - Побитовые операции (&, |=, &= ~) для статусных эффектов
    //   - Callback через интерфейс BattleEventListener

    private boolean battle(Enemy enemy) {
        // ===== СБРОС СОСТОЯНИЯ ПЕРЕД БОЕМ =====
        //
        // statusFlags = 0 — обнуляет ВСЕ биты (снимает все эффекты: яд, оглушение и т.д.).
        // В побитовом виде: 0 = 0b00000000 — ни один бит не установлен.
        statusFlags = 0;

        // undoUsedThisBattle — флаг «была ли отмена в этом бою?»
        // Сбрасываем, чтобы дать игроку одну отмену на новый бой.
        undoUsedThisBattle = false;

        // ArrayDeque.clear() — удаляет все элементы из стека. Время: O(n).
        // Стек действий очищается, т.к. прошлые ходы уже неактуальны.
        undoStack.clear();

        // Счётчик урона за текущий бой (для бестиария).
        damageDealtThisBattle = 0;

        // Сбрасываем флаг получения урона для нового боя.
        // boolean — примитивный тип: хранит true или false.
        // false = герой ещё не получал урон. Если останется false к концу боя — достижение FLAWLESS.
        heroTookDamageThisBattle = false;

        // ===== ПРОВЕРКА ПЕРЕХОДА СОСТОЯНИЯ (глава 3.8 — конечный автомат) =====
        //
        // assert — оператор проверки условия (Java 1.4+).
        // Синтаксис: assert условие : "сообщение об ошибке";
        // Если условие false — выбрасывается AssertionError с указанным сообщением.
        //
        // assert ОТКЛЮЧЁН по умолчанию! Для включения нужен флаг JVM: java -ea (enable assertions).
        // Используется для проверки инвариантов — условий, которые ВСЕГДА должны быть истинны.
        // Не используйте assert для валидации пользовательского ввода — только для логики программы.
        //
        // canTransitionTo() — метод enum GameState, реализующий конечный автомат (state machine).
        // Проверяет, допустим ли переход из текущего состояния в BATTLE.
        assert gameState.canTransitionTo(GameState.BATTLE) : "Недопустимый переход в состояние BATTLE из " + gameState;

        // ===== УСТАНОВКА СОСТОЯНИЯ ИГРЫ =====
        //
        // GameState.BATTLE — фаза боя. Раньше это состояние никогда не устанавливалось,
        // и gameState перескакивал из EXPLORING сразу в GAME_OVER, минуя BATTLE.
        // Теперь конечный автомат работает корректно: EXPLORING → BATTLE → EXPLORING.
        gameState = GameState.BATTLE;

        // ===== ВЫЗОВ CALLBACK onBattleStart() (глава 3.16) =====
        //
        // listener — объект, реализующий интерфейс BattleEventListener (паттерн «Наблюдатель»).
        // Проверяем != null, потому что слушатель необязателен (может не быть задан).
        //
        // onBattleStart() — метод интерфейса, вызываемый в начале каждого боя.
        // Реализация решает, что с этим делать: логировать, показывать анимацию и т.д.
        // Это «callback» — МЫ не знаем, какой код выполнится. Его определяет реализация.
        if (listener != null) {
            listener.onBattleStart(hero, enemy.getName());
        }

        // ArrayList.add() — добавление строки в конец списка: O(1) в среднем.
        battleLog.add("Начало боя: " + hero.getName() + " vs " + enemy.getName());

        // ===== ЦИКЛ РАУНДОВ БОЯ =====
        //
        // for (init; condition; update) — цикл с тремя секциями:
        //   init:      int round = 1 — начинаем с раунда 1.
        //   condition: enemy.isAlive() && hero.isAlive() — бой идёт, пока оба живы.
        //   update:    round++ — после каждой итерации номер раунда увеличивается на 1.
        //
        // && — логическое И (short-circuit): если enemy.isAlive() == false,
        //   hero.isAlive() НЕ вычисляется (оптимизация, называется «ленивые вычисления»).
        for (int round = 1; enemy.isAlive() && hero.isAlive(); round++) {
            System.out.println("────────────────────────────────");
            System.out.println("  Раунд " + round);
            System.out.println("────────────────────────────────");
            System.out.println("  " + hero);
            System.out.println("  " + enemy);
            displayStatusEffects();

            // ===== PriorityQueue — ИНИЦИАТИВА (глава 5.7) =====
            //
            // PriorityQueue — очередь с приоритетом на основе бинарной кучи (min-heap).
            // Элемент с НАИМЕНЬШИМ значением по Comparator извлекается ПЕРВЫМ (poll).
            //
            // Для инициативы нам нужен УБЫВАЮЩИЙ порядок: самый быстрый — первый.
            // Comparator.comparingInt(gc -> -gc.getSpeed()) — инвертируем знак для убывания.
            // Или: Comparator.comparingInt(GameCharacter::getSpeed).reversed()
            //
            // offer(element) — добавить в очередь: O(log n).
            // poll()         — извлечь элемент с наивысшим приоритетом: O(log n).
            // peek()         — посмотреть, не извлекая: O(1).
            //
            // ВНИМАНИЕ: PriorityQueue НЕ является отсортированной коллекцией!
            // Итерация (for-each) НЕ гарантирует порядок. Порядок гарантируется
            // только при последовательном вызове poll().
            //
            // Создаём PriorityQueue с Comparator по убыванию скорости.
            // Самый быстрый персонаж окажется в голове очереди (poll вернёт его первым).
            //
            // Поскольку hero (GameCharacter) и enemy (Enemy) — разные иерархии классов,
            // используем record-обёртку для хранения имени, скорости и флага "это герой?".
            // Это также демонстрирует, что PriorityQueue работает с ЛЮБЫМ типом,
            // если задать Comparator.
            //
            // ===== ЛОКАЛЬНЫЙ RECORD (Java 16+) =====
            //
            // record можно объявить ВНУТРИ метода — он будет доступен только в этом блоке.
            // Локальный record — неявно static (не захватывает переменные из внешнего метода).
            // Удобен для временных структур данных: не загромождает класс.
            record Combatant(String name, int speed, boolean isHero) {}

            // ===== СОЗДАНИЕ PriorityQueue С КОМПАРАТОРОМ =====
            //
            // new PriorityQueue<>(comparator) — создаём очередь с кастомным порядком.
            //
            // Comparator.comparingInt(Combatant::speed) — создаёт Comparator, сравнивающий
            //   объекты Combatant по полю speed (int). По умолчанию — по ВОЗРАСТАНИЮ.
            //   Combatant::speed — ссылка на метод (method reference). Эквивалент: c -> c.speed().
            //
            // .reversed() — ИНВЕРТИРУЕТ порядок Comparator: возрастание → убывание.
            //   Без reversed(): poll() вернёт самого МЕДЛЕННОГО (min-heap).
            //   С reversed(): poll() вернёт самого БЫСТРОГО (нам нужно именно это!).
            //
            // Альтернативный способ: Comparator.comparingInt(c -> -c.speed())
            //   Инвертируем знак, но reversed() читается лучше и безопаснее
            //   (при -Integer.MIN_VALUE произойдёт переполнение!).
            PriorityQueue<Combatant> initiativeQueue = new PriorityQueue<>(
                    Comparator.comparingInt(Combatant::speed).reversed()
            );

            // ===== offer() — ДОБАВЛЕНИЕ В ОЧЕРЕДЬ С ПРИОРИТЕТОМ =====
            //
            // offer(element) — добавляет элемент в PriorityQueue: O(log n).
            // Внутри — вставка в бинарную кучу с «всплытием» (sift up) элемента.
            //
            // Альтернатива: add() — делает то же самое, но бросает исключение при ограничении ёмкости.
            // Для PriorityQueue разницы нет (нет лимита), но offer() — идиоматичный выбор для очередей.
            //
            // Создаём Combatant через канонический конструктор record:
            //   new Combatant(имя, скорость, является_ли_героем).
            initiativeQueue.offer(new Combatant(hero.getName(), hero.getSpeed(), true));
            initiativeQueue.offer(new Combatant(enemy.getName(), enemy.getSpeed(), false));

            // ===== PriorityQueue.poll() — ИЗВЛЕЧЕНИЕ С ПРИОРИТЕТОМ (5.7) =====
            //
            // poll() — извлекает и удаляет элемент с НАИВЫСШИМ приоритетом: O(log n).
            // peek() — подсматривает без извлечения: O(1).
            //
            // Нам достаточно одного poll(), чтобы узнать, кто ходит первым.
            // Второй элемент извлекать не нужно — если первый герой, то второй враг, и наоборот.
            // Избыточные переменные (dead variables) — плохая практика:
            //   - Занимают память (хоть и минимально).
            //   - Вводят в заблуждение: читатель думает, что переменная используется далее.
            //   - IDE и статические анализаторы помечают их как предупреждение (warning).
            Combatant first = initiativeQueue.poll();   // самый быстрый
            boolean heroFirst = first.isHero();

            if (heroFirst) {
                System.out.println("  [Инициатива] " + hero.getName() + " ходит первым! (скорость: "
                        + hero.getSpeed() + " vs " + enemy.getSpeed() + ")");
            } else {
                System.out.println("  [Инициатива] " + enemy.getName() + " ходит первым! (скорость: "
                        + enemy.getSpeed() + " vs " + hero.getSpeed() + ")");
            }

            System.out.println();

            if (heroFirst) {
                // Ход героя (героFirst == true → герой быстрее).
                boolean fled = heroTurn(enemy);
                if (fled) {
                    // Герой сбежал — уведомляем слушателя и возвращаем состояние.
                    // listener.onBattleEnd(hero, false) — false означает «бой не выигран».
                    //
                    // ===== ВЫЗОВ default-МЕТОДА ИНТЕРФЕЙСА (глава 3.16) =====
                    //
                    // onBattleEnd() — default-метод BattleEventListener.
                    // default-метод имеет реализацию прямо в интерфейсе (Java 8+).
                    // Если класс-реализация НЕ переопределил onBattleEnd() — вызовется
                    // пустая default-реализация из интерфейса (ничего не делает).
                    // Если переопределил — вызовется переопределённая версия (полиморфизм).
                    if (listener != null) {
                        listener.onBattleEnd(hero, false);
                    }
                    // ===== ПЕРЕХОД СОСТОЯНИЯ: BATTLE → EXPLORING =====
                    //
                    // Корректный переход по конечному автомату (state machine).
                    // GameState.canTransitionTo() гарантирует, что этот переход допустим.
                    gameState = GameState.EXPLORING;
                    return true;
                }

                // Ход врага (если жив).
                // && — short-circuit И: если enemy.isAlive() == false, вторая проверка не вычисляется.
                if (enemy.isAlive() && hero.isAlive()) {
                    // ===== ПРОВЕРКА БИТА STUNNED (побитовое И) =====
                    //
                    // (statusFlags & STUNNED) — побитовое И: проверяет, установлен ли бит STUNNED.
                    // Если результат == 0 — бит НЕ установлен (враг НЕ оглушён → может ходить).
                    // Если результат != 0 — бит установлен (враг оглушён → пропускает ход).
                    //
                    // &= ~STUNNED — СНЯТИЕ бита STUNNED:
                    //   ~STUNNED = побитовое НЕ: инвертирует все биты (0b00000010 → 0b11111101).
                    //   &= — побитовое И с присваиванием: обнуляет ТОЛЬКО бит STUNNED,
                    //   оставляя все остальные биты (POISONED, SHIELDED и т.д.) без изменений.
                    if ((statusFlags & STUNNED) == 0) {
                        enemyTurn(enemy);
                    } else {
                        System.out.println(enemy.getName() + " оглушён и пропускает ход!");
                        statusFlags &= ~STUNNED;
                    }
                }
            } else {
                // Враг ходит первым (heroFirst == false → враг быстрее).
                // Проверка STUNNED — аналогична описанной выше (см. комментарий).
                if ((statusFlags & STUNNED) == 0) {
                    enemyTurn(enemy);
                } else {
                    System.out.println(enemy.getName() + " оглушён и пропускает ход!");
                    statusFlags &= ~STUNNED;
                }

                // Ход героя (если оба ещё живы после хода врага).
                if (enemy.isAlive() && hero.isAlive()) {
                    boolean fled = heroTurn(enemy);
                    if (fled) {
                        // Callback при бегстве (см. подробное описание выше).
                        if (listener != null) {
                            listener.onBattleEnd(hero, false);
                        }
                        gameState = GameState.EXPLORING;
                        return true;
                    }
                }
            }

            // ===== ОБРАБОТКА ЯДА (POISONED) В КОНЦЕ РАУНДА =====
            //
            // (statusFlags & POISONED) != 0 — проверяем бит POISONED (побитовое И).
            // POISONED = 1 = 0b00000001 (бит 0). Если установлен — герой отравлен.
            //
            // Яд наносит фиксированный урон (3) каждый раунд, пока активен.
            // Math.random() — возвращает случайное double в диапазоне [0.0, 1.0).
            // Вероятность < 0.4 означает: примерно 40% шанс снятия яда каждый раунд.
            if ((statusFlags & POISONED) != 0 && hero.isAlive()) {
                int poisonDamage = 3;
                hero.takeDamage(poisonDamage);

                // += — составной оператор: totalDamageReceived = totalDamageReceived + poisonDamage.
                totalDamageReceived += poisonDamage;

                // Яд тоже считается полученным уроном — FLAWLESS не будет засчитан.
                // Без этой строки можно было бы получить FLAWLESS, даже умирая от яда — баг!
                heroTookDamageThisBattle = true;

                System.out.println("Яд наносит " + poisonDamage + " урона!");
                battleLog.add("Яд нанёс " + poisonDamage + " урона");

                // С вероятностью 40% яд проходит сам.
                // &= ~POISONED — снятие бита (см. подробное описание &= ~ в проверке STUNNED выше).
                if (Math.random() < 0.4) {
                    statusFlags &= ~POISONED;
                    System.out.println("Действие яда закончилось.");
                }
            }

            System.out.println();
        }

        // ===== ВЫЗОВ CALLBACK onBattleEnd() (глава 3.16) =====
        //
        // Уведомляем слушателя о завершении боя.
        // onBattleEnd() — default-метод интерфейса BattleEventListener.
        // Default-реализация пустая (ничего не делает), но классы, реализующие
        // BattleEventListener, могут переопределить его для логирования, статистики и т.д.
        //
        // Параметры:
        //   hero — герой, участвовавший в бою.
        //   hero.isAlive() — true если герой победил (остался жив), false если проиграл.
        //
        // ВАЖНО: вызываем ПЕРЕД return, чтобы слушатель получил уведомление
        // независимо от исхода боя (победа, поражение или бегство).
        // В случае бегства (return true выше) onBattleEnd не вызывается —
        // это обрабатывается в gameLoop(), где fled проверяется отдельно.
        if (listener != null) {
            listener.onBattleEnd(hero, hero.isAlive());
        }

        // ===== ВОЗВРАТ СОСТОЯНИЯ ИГРЫ В EXPLORING =====
        //
        // После завершения боя (победа или поражение) возвращаемся в режим исследования.
        // Это корректный переход по конечному автомату: BATTLE → EXPLORING.
        gameState = GameState.EXPLORING;

        return false;
    }

    // ===== heroTurn() — ХОД ГЕРОЯ =====
    //
    // Выделен в отдельный метод для поддержки инициативы (PriorityQueue).
    // Раньше логика хода была прямо в battle() — но при системе инициативы
    // порядок ходов определяется динамически, поэтому нужен отдельный метод.
    //
    // boolean — возвращаемый тип: true = герой сбежал, false = герой остался.
    // Enemy enemy — параметр метода: враг, с которым идёт бой. Передаётся по ссылке.
    //
    // Демонстрирует:
    //   - ArrayDeque как стек (push/pop) для отмены действий (LIFO)
    //   - switch-выражение (Java 14+) с -> стрелочным синтаксисом
    //   - switch-оператор для диспетчеризации действий игрока
    //   - undoUsedThisBattle — ограничение: одна отмена за бой

    private boolean heroTurn(Enemy enemy) {
        // ===== ВНЕШНИЙ ЦИКЛ ВЫБОРА ДЕЙСТВИЯ =====
        //
        // while (true) — бесконечный цикл, объединяющий ДВЕ проверки:
        //   1. Валидация ввода (число в допустимом диапазоне).
        //   2. Проверка выполнимости действия (например, зелье при полном HP → повтор выбора).
        //
        // Цикл прерывается через return (побег) или break (действие выполнено успешно).
        // Это позволяет игроку выбрать другое действие, если текущее невозможно,
        // без потери хода (враг не атакует «за бесплатно»).
        while (true) {
            System.out.println("Действия:");
            System.out.println("  1. Атаковать");
            System.out.println("  2. Спец. атака");
            System.out.println("  3. Использовать предмет");
            System.out.println("  4. Защита");
            System.out.println("  5. Бежать");
            // ===== ArrayDeque КАК СТЕК ОТМЕНЫ (глава 5.7) =====
            //
            // ArrayDeque используется здесь как СТЕК — структура данных LIFO.
            // LIFO = Last In, First Out: последний добавленный элемент извлекается первым.
            // Аналогия: стопка тарелок — берёшь верхнюю (последнюю положенную).
            //
            // undoStack.isEmpty() — true если стек пуст (нечего отменять).
            // !undoUsedThisBattle — отмена ещё не была использована в этом бою.
            // Обе проверки через && (И): условие показа = стек не пуст И отмена не использована.
            if (!undoUsedThisBattle && !undoStack.isEmpty()) {
                System.out.println("  6. Отменить ход (осталось: 1)");
            }
            // ===== ВАЛИДАЦИЯ ВВОДА В ЦИКЛЕ while (глава 2.5) =====
            //
            // while (true) — бесконечный цикл, который прерывается ТОЛЬКО через break.
            // Зачем? Чтобы не пропускать ход игрока из-за опечатки или неверного ввода.
            // Раньше неверный ввод приводил к default → "Неверный выбор, ход пропущен",
            // и враг атаковал бесплатно. Теперь игрок повторяет ввод до корректного значения.
            //
            // Допустимые значения:
            //   1-5 — стандартные действия (атака, спец. атака, предмет, защита, побег).
            //   6   — отмена хода (только если доступна: стек не пуст и отмена не использована).
            int action;
            while (true) {
                System.out.print("Ваш выбор: ");
                action = readInt();

                // Определяем максимальный допустимый номер действия.
                // Если доступна отмена хода (действие 6) — maxAction = 6, иначе 5.
                int maxAction = (!undoUsedThisBattle && !undoStack.isEmpty()) ? 6 : 5;

                // action >= 1 && action <= maxAction — проверка диапазона допустимых значений.
                // Если ввод корректен — выходим из цикла через break.
                // Иначе — выводим сообщение и повторяем запрос (цикл while продолжается).
                if (action >= 1 && action <= maxAction) {
                    break;
                }
                System.out.println("Неверный выбор. Введите число от 1 до " + maxAction + ".");
            }

            // ===== ОТМЕНА ХОДА — ArrayDeque как стек (5.7) =====
            if (action == 6 && !undoUsedThisBattle && !undoStack.isEmpty()) {
                // ===== ArrayDeque.pop() — ИЗВЛЕЧЬ С ВЕРШИНЫ СТЕКА =====
                //
                // pop() — извлекает и удаляет элемент с вершины стека (последний добавленный).
                // Бросает NoSuchElementException, если стек пуст.
                // LIFO: Last In, First Out — последний добавленный элемент извлекается первым.
                String lastAction = undoStack.pop();
                System.out.println("Отмена действия: " + lastAction);
                undoUsedThisBattle = true;
                battleLog.add("Отмена действия: " + lastAction);
                return false;
            }

            // ===== ArrayDeque.push() — СОХРАНИТЬ НА ВЕРШИНУ СТЕКА =====
            //
            // push(element) — добавляет элемент на вершину стека (в НАЧАЛО deque).
            // Время: O(1). Эквивалентно addFirst() для Deque.
            //
            // Стек отмены работает так:
            //   1. Перед выполнением действия → push(описание) — сохраняем в стек.
            //   2. При отмене → pop() — извлекаем последнее действие (LIFO).
            //   3. peek() — подсматриваем вершину без извлечения (в этом коде не используется).
            //
            // ===== switch-ВЫРАЖЕНИЕ (Java 14+) =====
            //
            // switch-выражение ВОЗВРАЩАЕТ ЗНАЧЕНИЕ (в отличие от switch-оператора).
            // -> (стрелочный синтаксис) — нет «проваливания» (fall-through), не нужен break.
            //
            // Результат switch присваивается в переменную actionDesc.
            // Компилятор требует обработать ВСЕ варианты (exhaustiveness):
            //   для int это значит, что обязателен default (int имеет слишком много значений).
            String actionDesc = switch (action) {
                case 1 -> "Атака";
                case 2 -> "Спец. атака";
                case 3 -> "Предмет";
                case 4 -> "Защита";
                default -> "Другое";
            };
            undoStack.push(actionDesc);

            // ===== switch-ОПЕРАТОР (НЕ выражение) — ДИСПЕТЧЕРИЗАЦИЯ ДЕЙСТВИЙ =====
            //
            // Этот switch НЕ возвращает значение — он ВЫПОЛНЯЕТ действия.
            // -> (стрелочный синтаксис) всё равно используется для читаемости и защиты от fall-through.
            //
            // case 5 -> { ... } — фигурные скобки нужны, когда в ветке больше одной строки.
            // Внутри блока {} можно писать любой код: переменные, if, return.
            //
            // ===== ОБРАБОТКА ОТМЕНЁННЫХ ДЕЙСТВИЙ (continue) =====
            //
            // Некоторые действия могут быть «отменены» — например, использование предмета
            // при полном здоровье. В этом случае handleUseItem() возвращает false.
            // continue — переход к следующей итерации внешнего цикла while (повторный выбор).
            // Без continue враг атаковал бы после неудачной попытки — несправедливо для игрока.
            switch (action) {
                case 1 -> handleNormalAttack(enemy);
                case 2 -> handleSpecialAttack(enemy);
                case 3 -> {
                    if (!handleUseItem()) {
                        // Предмет не использован (полное HP, пустой инвентарь, ошибка выбора).
                        // Убираем записанное действие из стека отмены — оно не состоялось.
                        undoStack.pop();
                        continue;
                    }
                }
                case 4 -> handleDefend();
                case 5 -> {
                    // Math.random() < 0.5 — 50% шанс побега.
                    boolean escaped = Math.random() < 0.5;
                    // Тернарный оператор: условие ? значениеЕслиTrue : значениеЕслиFalse
                    System.out.println(escaped ? "Вы успешно сбежали!" : "Побег не удался!");
                    if (escaped) {
                        return true;  // return из метода heroTurn() — бой прекращается.
                    }
                }
                // default — обязательная ветка для switch по int (компилятор требует exhaustiveness).
                // Благодаря циклу валидации выше, сюда мы никогда не попадём:
                // action гарантированно в диапазоне 1-5 (или 1-6 при доступной отмене).
                // Но default нужен синтаксически — без него switch-выражение не скомпилируется.
                default -> { }
            }
            break;
        }
        return false;  // Герой НЕ сбежал — бой продолжается.
    }

    // ===== МЕТОД handleNormalAttack() — ОБЫЧНАЯ АТАКА =====
    //
    // Обрабатывает обычную атаку героя. Полиморфизм определяет тип урона:
    //   Warrior.performAttack() → DamageType.Physical (физический урон)
    //   Mage.performAttack() → DamageType.Magical (магический урон)
    //   Archer.performAttack() → DamageType.Mixed (смешанный урон)
    //
    // DamageType — sealed interface: компилятор знает ВСЕ возможные реализации.
    // Это позволяет ИСЧЕРПЫВАЮЩИЙ switch без default (см. ниже).

    private void handleNormalAttack(Enemy enemy) {
        // hero.performAttack() — полиморфный вызов: реальный тип hero определяет реализацию.
        // Возвращает DamageType — sealed interface с тремя record-реализациями.
        DamageType damage = hero.performAttack();

        // totalDamage() — метод sealed interface DamageType (реализован в каждом record).
        // Возвращает суммарный урон в виде int.
        int dmg = damage.totalDamage();

        // ===== ПРОВЕРКА И СНЯТИЕ БИТА ENRAGED (побитовые операции) =====
        //
        // (statusFlags & ENRAGED) != 0 — проверка: установлен ли бит ENRAGED?
        // ENRAGED = 1 << 3 = 0b00001000 (бит 3).
        //
        // Если герой «в ярости» — бонус +5 к урону, затем ярость снимается.
        // &= ~ENRAGED — обнуление бита ENRAGED (см. подробное описание в battle()).
        if ((statusFlags & ENRAGED) != 0) {
            dmg += 5;
            statusFlags &= ~ENRAGED;
            System.out.println("Ярость добавляет +5 к урону!");
        }

        // enemy.takeDamage(dmg) — враг получает урон. Метод Enemy уменьшает HP.
        enemy.takeDamage(dmg);
        totalDamageDealt += dmg;
        damageDealtThisBattle += dmg;

        // Callback onAttack() — уведомляем слушателя о каждой атаке.
        // Передаём: кто атаковал, тип урона (DamageType), итоговый урон (int).
        if (listener != null) {
            listener.onAttack(hero, damage, dmg);
        }

        // ===== switch ПО sealed interface С RECORD-ПАТТЕРНАМИ (Java 21+) =====
        //
        // Раньше здесь проверялся только DamageType.Physical через instanceof.
        // Остальные типы (Magical, Mixed) НЕ обрабатывались — при нормальной атаке
        // мага или лучника (если бы performAttack() возвращал другой тип) вывод бы отсутствовал.
        //
        // switch по sealed interface — компилятор ГАРАНТИРУЕТ полноту обработки.
        // Если добавить новый тип DamageType и забыть обработать — ошибка компиляции!
        // Это главное преимущество sealed interface + switch перед цепочкой if-instanceof.
        //
        // Record-паттерны (deconstruction patterns):
        //   case DamageType.Physical(var amt) — извлекает поле amount из record Physical.
        //   var amt — тип определяется автоматически (int). Это деконструкция (destructuring).
        switch (damage) {
            case DamageType.Physical(var amt) ->
                System.out.println(hero.getName() + " наносит " + amt + " физического урона!");
            case DamageType.Magical(var amt, var elem) ->
                System.out.println(hero.getName() + " наносит " + amt + " магического урона (" + elem + ")!");
            case DamageType.Mixed(var phys, var mag) ->
                System.out.println(hero.getName() + " наносит " + phys + " физ. + " + mag + " маг. урона!");
        }

        // Добавляем в лог боя (5.2).
        battleLog.add(hero.getName() + " атакует: " + dmg + " урона");

        // ===== ДОСТИЖЕНИЕ DEVASTATOR — МГНОВЕННОЕ УВЕДОМЛЕНИЕ =====
        //
        // DEVASTATOR присуждается ПРИ НАНЕСЕНИИ 50+ урона за один удар.
        // В отличие от других достижений (проверяемых в checkAchievements после боя),
        // это достижение выдаётся немедленно в момент мощного удара.
        //
        // Set.add() возвращает boolean:
        //   true  — элемент добавлен (достижение НОВОЕ).
        //   false — элемент уже был (достижение уже получено).
        // Используем возвращённое значение для вывода уведомления ТОЛЬКО при первом получении.
        if (dmg >= 50) {
            if (achievements.add(Achievement.DEVASTATOR)) {
                System.out.println("\n*** НОВОЕ ДОСТИЖЕНИЕ: " + Achievement.DEVASTATOR + " ***");
            }
        }

        // logAction() — вспомогательный метод с varargs (String...).
        // String.valueOf(dmg) — преобразует int в String: 25 → "25".
        //   Эквивалент: Integer.toString(dmg) или "" + dmg.
        //   Нужен, потому что logAction принимает String..., а dmg — int (примитив).
        logAction("Атака", hero.getName(), String.valueOf(dmg));
    }

    // ===== МЕТОД handleSpecialAttack() — СПЕЦИАЛЬНАЯ АТАКА =====
    //
    // Обрабатывает спецатаку героя. Каждый класс имеет уникальную механику:
    //   Warrior — удар с яростью (rage)
    //   Mage    — магическое заклинание (расходует ману)
    //   Archer  — прицельный выстрел (шанс критического удара)
    //
    // Демонстрирует:
    //   - Пользовательское checked exception (InsufficientResourceException)
    //   - Полный жизненный цикл throw → catch
    //   - Pattern matching instanceof (Java 16+)
    //   - Механику оглушения (STUNNED) через побитовые операции
    //   - Record-паттерны для деконструкции sealed interface (Java 21+)

    private void handleSpecialAttack(Enemy enemy) {

        // ===== ПРОВЕРКА МАНЫ МАГА — ОБЫЧНАЯ УСЛОВНАЯ ЛОГИКА (глава 4.3 — anti-pattern) =====
        //
        // АНТИПАТТЕРН: бросать исключение и ловить его В ТОМ ЖЕ МЕТОДЕ для обычной проверки.
        // Раньше здесь было:
        //   try { throw new InsufficientResourceException(...); } catch (...) { print; }
        // Это использование исключений как «print-оператора» — ПЛОХАЯ практика!
        //
        // ПОЧЕМУ это плохо:
        //   1. Исключения предназначены для ИСКЛЮЧИТЕЛЬНЫХ ситуаций (ошибки, сбои, нехватка ресурсов),
        //      а не для обычного управления потоком (flow control).
        //   2. Создание исключения ДОРОГО: JVM собирает стек вызовов (stack trace) при каждом throw.
        //   3. try-catch в том же методе, где throw — бессмысленно: вы бросаете мяч и сами же его ловите.
        //   4. Простой if-else делает то же самое, но быстрее и понятнее.
        //
        // ПРАВИЛО: если ситуацию можно проверить через if — используй if.
        // Исключения нужны, когда ошибка обнаруживается ГЛУБОКО в вызове (в другом методе/классе),
        // и нужно «пробросить» информацию ВВЕРХ по стеку вызовов к обработчику.
        //
        // InsufficientResourceException правильно использовать так:
        //   - Метод A вызывает метод B, метод B бросает InsufficientResourceException.
        //   - Метод A (или его вызывающий) ловит и обрабатывает.
        //   - throw и catch — в РАЗНЫХ методах (или даже классах).
        //
        // Mage.specialAttack() уже сам проверяет ману и возвращает слабое заклинание,
        // поэтому достаточно предупредить игрока через обычный if.
        //
        // ===== PATTERN MATCHING instanceof (Java 16+) =====
        //
        // hero instanceof Mage m — одновременно проверяет тип И создаёт переменную:
        //   1. Проверяет: является ли hero экземпляром Mage?
        //   2. Если да — m получает ссылку на hero, уже приведённую к типу Mage.
        // Раньше нужно было два шага: if (hero instanceof Mage) { Mage m = (Mage) hero; ... }
        // Подробное объяснение этого конструкта — ниже, в блоке после hero.specialAttack().
        if (hero instanceof Mage m) {
            if (m.getMana() < 20) {
                System.out.println("⚡ Недостаточно маны для мощного заклинания (нужно: 20, есть: "
                        + m.getMana() + ")");
                System.out.println("  " + hero.getName() + " использует слабое заклинание...");
            }
        }

        // hero.specialAttack() — полиморфный вызов: тип hero определяет реализацию.
        //   Warrior → удар с яростью
        //   Mage    → заклинание (расходует ману)
        //   Archer  → прицельный выстрел (возможный критический удар)
        DamageType damage = hero.specialAttack();
        // damage.totalDamage() — метод sealed interface, реализован в каждом record.
        int dmg = damage.totalDamage();
        enemy.takeDamage(dmg);
        totalDamageDealt += dmg;         // += : totalDamageDealt = totalDamageDealt + dmg
        damageDealtThisBattle += dmg;
        // ++ : specialAttackCount = specialAttackCount + 1 (для достижения SPECIALIST)
        specialAttackCount++;

        // Callback — уведомляем слушателя о спецатаке (аналогично обычной атаке).
        if (listener != null) {
            listener.onAttack(hero, damage, dmg);
        }

        // Record-паттерны (Java 21+) — деконструирование sealed interface.
        //
        // var внутри record-паттерна — автоматическое определение типа (Java 10+).
        // var amt → компилятор определяет тип int из record Physical(int amount).
        // var — НЕ динамическая типизация! Тип фиксируется при КОМПИЛЯЦИИ.
        // Где можно: локальные переменные, for-each, record-паттерны.
        // Где нельзя: параметры методов, поля класса, return-типы.
        switch (damage) {
            case DamageType.Physical(var amt) ->
                System.out.println("Физический удар: " + amt + " урона!");
            case DamageType.Magical(var amt, var elem) ->
                System.out.println("Магический удар (" + elem + "): " + amt + " урона!");
            case DamageType.Mixed(var phys, var mag) ->
                System.out.println("Смешанный удар: " + phys + " физ. + " + mag + " маг. урона!");
        }

        // ===== PATTERN MATCHING instanceof (Java 16+) =====
        //
        // hero instanceof Warrior w — одновременно:
        //   1. Проверяет, является ли hero экземпляром Warrior.
        //   2. Если да — создаёт переменную w типа Warrior (паттерн-переменная).
        //
        // Раньше нужно было писать:
        //   if (hero instanceof Warrior) {
        //       Warrior w = (Warrior) hero;  // явное приведение типа (cast)
        //       w.getRage();
        //   }
        //
        // Теперь в одну строку: if (hero instanceof Warrior w) { w.getRage(); }
        // Переменная w доступна ТОЛЬКО внутри блока if (scope ограничен).
        //
        // Полиморфизм: hero объявлен как GameCharacter, но реальный тип определяется в runtime.
        // instanceof проверяет реальный тип объекта, а не тип переменной.
        if (hero instanceof Warrior w) {
            System.out.println("Ярость воина: " + w.getRage());

            // ===== МЕХАНИКА ОГЛУШЕНИЯ (STUN) ДЛЯ ВОИНА =====
            //
            // При высокой ярости (rage >= 40) воин может оглушить врага мощным ударом.
            // Шанс оглушения: 25% (Math.random() < 0.25).
            //
            // statusFlags |= STUNNED — установка бита STUNNED (побитовое ИЛИ).
            // |= — составной оператор: statusFlags = statusFlags | STUNNED.
            // Устанавливает бит STUNNED в 1, не затрагивая другие биты.
            //
            // STUNNED = 1 << 1 = 0b00000010 (бит 1).
            // Если statusFlags = 0b00000001 (POISONED), после |= STUNNED:
            //   statusFlags = 0b00000011 (POISONED + STUNNED).
            //
            // Оглушённый враг пропускает следующий ход (проверка в battle():
            //   if ((statusFlags & STUNNED) == 0) — ход врага, иначе пропуск).
            if (w.getRage() >= 40 && Math.random() < 0.25) {
                statusFlags |= STUNNED;
                System.out.println("💫 Мощный удар оглушает " + enemy.getName() + "! Враг пропустит ход.");
                battleLog.add(enemy.getName() + " оглушён мощным ударом воина!");
            }
        } else if (hero instanceof Mage m) {
            // Pattern matching instanceof (повторное использование, см. описание для Warrior выше).
            // m — паттерн-переменная типа Mage, доступна в этом блоке else if.
            System.out.println("Оставшаяся мана: " + m.getMana());
        } else if (hero instanceof Archer a) {
            // (int) — явное приведение типа (cast): double → int (отбрасывает дробную часть).
            // a.getCritChance() возвращает double (например, 0.35).
            // Умножаем на 100 → 35.0, затем (int) → 35.
            // ВНИМАНИЕ: (int) обрезает, а не округляет! 0.999 * 100 = 99.9 → (int) = 99.
            System.out.println("Шанс крита: " + (int) (a.getCritChance() * 100) + "%");
        }

        // Лог боя.
        battleLog.add(hero.getName() + " спец.атака: " + dmg + " урона");

        // Достижение DEVASTATOR — мгновенное уведомление (аналогично handleNormalAttack).
        if (dmg >= 50) {
            if (achievements.add(Achievement.DEVASTATOR)) {
                System.out.println("\n*** НОВОЕ ДОСТИЖЕНИЕ: " + Achievement.DEVASTATOR + " ***");
            }
        }

        // logAction() — varargs метод; String.valueOf(dmg) — int → String (см. handleNormalAttack).
        logAction("Спец. атака", hero.getName(), String.valueOf(dmg));
    }

    // ===== МЕТОД handleUseItem() — ИСПОЛЬЗОВАНИЕ ПРЕДМЕТА (глава 4.1, 4.2) =====
    //
    // Рефакторинг: обёрнуто в try-catch-finally с multi-catch.

    // ===== ВОЗВРАЩАЕМЫЙ ТИП boolean — СИГНАЛ УСПЕХА ДЕЙСТВИЯ =====
    //
    // Метод возвращает boolean: true — предмет использован, false — действие отменено.
    // Зачем? Чтобы heroTurn() мог повторить выбор действия, если предмет не был потрачен
    // (например, при полном здоровье). Без этого враг атаковал бы «за бесплатно».
    private boolean handleUseItem() {
        if (inventory.getSize() == 0) {
            System.out.println("Инвентарь пуст!");
            return false;
        }

        // ===== ДЕМОНСТРАЦИЯ for-each ПО Iterable (глава 5.10) =====
        //
        // Inventory<T> теперь реализует Iterable<Inventory<T>.Slot>.
        // Это позволяет использовать for-each для обхода слотов:
        //   for (var slot : inventory) { ... }
        //
        // Это работает, потому что:
        //   1. Inventory реализует Iterable<Slot>.
        //   2. Iterable имеет метод iterator(), возвращающий Iterator<Slot>.
        //   3. for-each — синтаксический сахар для Iterator.
        //
        // Компилятор превращает for-each в:
        //   Iterator<Slot> it = inventory.iterator();
        //   while (it.hasNext()) {
        //       var slot = it.next();
        //       ...
        //   }
        System.out.println("--- Инвентарь (for-each через Iterable) ---");
        int idx = 1;
        for (var slot : inventory) {
            System.out.println("  " + idx + ". " + slot);
            idx++;
        }
        System.out.println("-------------------------------------------");

        System.out.print("Выберите предмет (номер): ");
        int itemIndex = readInt() - 1;

        // ===== try-catch-finally С MULTI-CATCH (глава 4.1, 4.2) =====
        //
        // try { ... }
        // catch (ExceptionA | ExceptionB e) { ... }  ← MULTI-CATCH
        // finally { ... }
        //
        // MULTI-CATCH — перехват НЕСКОЛЬКИХ типов исключений ОДНИМ блоком catch.
        // Синтаксис: catch (ТипА | ТипБ e) { ... }
        //
        // Зачем? Когда обработка одинаковая для разных исключений — не дублируем код.
        // Без multi-catch:
        //   catch (IndexOutOfBoundsException e) { System.out.println("Ошибка: " + e.getMessage()); }
        //   catch (IllegalStateException e) { System.out.println("Ошибка: " + e.getMessage()); }
        // С multi-catch:
        //   catch (IndexOutOfBoundsException | IllegalStateException e) { ... }
        //
        // ВАЖНО: типы в multi-catch НЕ ДОЛЖНЫ быть связаны наследованием.
        // catch (Exception | IOException e) — ОШИБКА! IOException наследует Exception.
        //
        // finally — блок, который выполняется ВСЕГДА: и при успехе, и при ошибке.
        // Используется для «очистки»: закрытие ресурсов, освобождение блокировок.
        // finally выполняется даже если в catch есть return!
        try {
            if (itemIndex < 0 || itemIndex >= inventory.getSize()) {
                // Бросаем исключение для демонстрации multi-catch.
                throw new IndexOutOfBoundsException("Неверный номер предмета: " + (itemIndex + 1));
            }

            // var — автоматический вывод типа (Java 10+, глава 3.19).
            // Компилятор определяет тип из правой части: getSlot() возвращает Inventory<...>.Slot.
            // Без var: Inventory<Inventory.ItemInfo>.Slot slot = inventory.getSlot(itemIndex);
            // var делает код короче при длинных generic-типах.
            var slot = inventory.getSlot(itemIndex);
            if (slot.isEmpty()) {
                throw new IllegalStateException("Слот пуст!");
            }

            // ===== ПРОВЕРКА ПОЛНОГО ЗДОРОВЬЯ ПЕРЕД ЛЕЧЕНИЕМ =====
            //
            // hero.getHealth() == hero.getMaxHealth() — сравнение текущего HP с максимальным.
            // Если HP уже на максимуме — зелье не расходуется (бессмысленно лечить здорового).
            // Раньше зелье тратилось впустую, игрок терял ценный предмет без эффекта.
            // return — выход из метода handleUseItem(), игрок сможет выбрать другое действие.
            if (hero.getHealth() == hero.getMaxHealth()) {
                System.out.println("Здоровье уже полное! Зелье не использовано.");
                return false;
            }

            int healAmount = slot.getItem().getValue();
            hero.heal(healAmount);
            totalHealing += healAmount;
            slot.decreaseQuantity();
            System.out.println(hero.getName() + " использует " + slot.getItem().getName()
                    + " и восстанавливает " + healAmount + " HP!");

            battleLog.add(hero.getName() + " использует " + slot.getItem().getName()
                    + ": +" + healAmount + " HP");

            // ===== ОЧИСТКА ПУСТОГО СЛОТА: ОСВОБОЖДЕНИЕ ЁМКОСТИ =====
            //
            // После уменьшения количества проверяем, не опустел ли слот (quantity == 0).
            // Если предметов не осталось — удаляем слот из массива инвентаря.
            //
            // Зачем? Без удаления пустой слот продолжает занимать место в массиве:
            //   - size не уменьшается → addItem() считает, что места нет.
            //   - Игрок не может подобрать новые предметы, хотя ячейка фактически пуста.
            //   - Это баг: ёмкость инвентаря «утекает» при каждом полном расходе предмета.
            //
            // slot.isEmpty() — метод Slot, возвращающий true если quantity <= 0.
            // inventory.removeSlot(itemIndex) — сдвигает элементы массива и уменьшает size.
            //
            // ВАЖНО: переменная slot всё ещё ссылается на объект Slot в памяти,
            // даже после удаления из массива. Объект будет жив, пока на него есть ссылка.
            // Сборщик мусора (GC) удалит его только когда переменная slot выйдет из области видимости.
            if (slot.isEmpty()) {
                inventory.removeSlot(itemIndex);
                System.out.println("(Предмет закончился и удалён из инвентаря)");
            }

            if (hero.needsHealing()) {
                int recommended = Healable.baseHealAmount(hero.getLevel());
                System.out.println("Герою всё ещё нужно лечение. Рекомендуемое исцеление: " + recommended);
            }
        } catch (IndexOutOfBoundsException | IllegalStateException e) {
            // ===== MULTI-CATCH В ДЕЙСТВИИ =====
            //
            // Один catch обрабатывает ОБА типа исключений одинаково.
            // e — переменная типа «общий родитель» (RuntimeException в данном случае).
            // e.getMessage() — сообщение об ошибке из конструктора исключения.
            System.out.println("Ошибка при использовании предмета: " + e.getMessage());
            battleLog.add("Ошибка: " + e.getMessage());
            return false;
        } finally {
            // ===== finally — ГАРАНТИРОВАННОЕ ВЫПОЛНЕНИЕ (глава 4.1) =====
            //
            // Блок finally выполняется ВСЕГДА:
            //   - Если try завершился успешно → finally → продолжение.
            //   - Если произошло исключение → catch → finally → продолжение.
            //   - Если в catch есть return → finally выполнится ПЕРЕД return!
            //
            // Зачем? Для гарантированной «очистки»:
            //   - Закрытие файлов (хотя try-with-resources лучше).
            //   - Освобождение блокировок (lock.unlock()).
            //   - Логирование завершения операции.
            //
            // ВНИМАНИЕ: не пишите return в finally! Он «перебьёт» return из try/catch.
            System.out.println("[Действие] Ход с предметом завершён.");
        }
        return true;
    }

    // ===== МЕТОД handleDefend() — ЗАЩИТА =====
    //
    // Активирует щит (снижает урон вдвое) и переключает ярость.
    // Демонстрирует ТРИ побитовые операции: OR (|=), XOR (^=), AND (&).
    //
    // Сводная таблица побитовых операций в этом методе:
    //   |=  (OR)  — УСТАНОВИТЬ бит: 0→1, 1→1 (всегда включает)
    //   ^=  (XOR) — ПЕРЕКЛЮЧИТЬ бит: 0→1, 1→0 (toggle)
    //   &   (AND) — ПРОВЕРИТЬ бит: результат != 0 → бит установлен

    private void handleDefend() {
        // ===== OR (|=) — УСТАНОВКА БИТА SHIELDED =====
        //
        // |= (OR-присваивание) устанавливает бит SHIELDED в 1.
        // Если бит уже был 1 — ничего не изменится (идемпотентная операция).
        // SHIELDED = 1 << 2 = 0b00000100 (бит 2).
        //
        // Пример: statusFlags = 0b00001001 (POISONED + ENRAGED)
        //   statusFlags |= 0b00000100 → 0b00001101 (POISONED + SHIELDED + ENRAGED)
        //   Другие биты НЕ затронуты — это ключевое свойство побитового OR.
        statusFlags |= SHIELDED;
        System.out.println(hero.getName() + " поднимает щит!");

        // ===== XOR (^=) — ПЕРЕКЛЮЧЕНИЕ БИТА ENRAGED (toggle) =====
        //
        // ^= (XOR-присваивание) переключает бит:
        //   Если ENRAGED был 0 → станет 1 (ярость ВКЛЮЧАЕТСЯ).
        //   Если ENRAGED был 1 → станет 0 (ярость ВЫКЛЮЧАЕТСЯ).
        //
        // Таблица истинности XOR (^):
        //   0 ^ 0 = 0
        //   0 ^ 1 = 1
        //   1 ^ 0 = 1
        //   1 ^ 1 = 0   ← ключевое: 1 XOR 1 = 0 (переключение!)
        //
        // Это НАМЕРЕННАЯ игровая механика: повторная защита СНИМАЕТ ярость.
        // Тактическое решение: защита через раз даёт/снимает бонус +5 к урону.
        //
        // Сравни с |= (OR выше): |= всегда УСТАНАВЛИВАЕТ бит, никогда не снимает.
        // ^= ПЕРЕКЛЮЧАЕТ: вкл→выкл→вкл→выкл... при каждом вызове.
        //
        // ===== instanceof — ПРОВЕРКА ТИПА ОБЪЕКТА (глава 3.5, 3.26) =====
        //
        // hero instanceof Warrior — проверяет, является ли объект hero экземпляром класса Warrior.
        // Каждый класс-наследник GameCharacter имеет свою боевую механику:
        //   - Warrior: ярость (ENRAGED) — бонус +5 к урону, переключается XOR.
        //   - Mage: концентрация маны — магическая защита.
        //   - Archer: уклонение — тактический манёвр.
        //
        // Раньше сообщение о ярости показывалось ВСЕМ классам — это баг:
        // маг и лучник не используют ярость, им она не подходит по лору.
        // Теперь XOR-переключение ENRAGED выполняется только для воина.
        if (hero instanceof Warrior) {
            // XOR (^=) — переключение бита ENRAGED (toggle).
            // Если ENRAGED был 0 → станет 1 (ярость ВКЛЮЧАЕТСЯ).
            // Если ENRAGED был 1 → станет 0 (ярость ВЫКЛЮЧАЕТСЯ).
            statusFlags ^= ENRAGED;

            // ===== AND (&) — ПРОВЕРКА БИТА =====
            //
            // (statusFlags & ENRAGED) != 0 — проверяем: установлен ли бит ENRAGED ПОСЛЕ переключения?
            // Тернарный оператор выбирает подходящее сообщение.
            String enragedMsg = (statusFlags & ENRAGED) != 0 ? "Ярость активирована!" : "Ярость снята.";
            System.out.println(enragedMsg);
        } else if (hero instanceof Mage) {
            // Маг использует магический щит — тематически подходит к классу.
            System.out.println("Магический щит! Концентрация маны усилена.");
        } else if (hero instanceof Archer) {
            // Лучник уклоняется — подходит к ловкому классу дальнего боя.
            System.out.println("Тактическое отступление! Уклонение активировано.");
        }

        battleLog.add(hero.getName() + " защищается (щит)");
    }

    // ===== МЕТОД enemyTurn() — ХОД ПРОТИВНИКА =====
    //
    // Обрабатывает атаку врага. Враг всегда атакует (нет выбора действий).
    // Урон может быть снижен щитом (SHIELDED). С вероятностью 20% враг отравляет героя.

    private void enemyTurn(Enemy enemy) {
        // enemy.getAttack() — базовый урон врага (зависит от ранга: COMMON/ELITE/BOSS).
        int baseDamage = enemy.getAttack();

        // ===== ПРОВЕРКА И СНЯТИЕ ЩИТА (побитовые операции) =====
        //
        // (statusFlags & SHIELDED) != 0 — проверяем бит SHIELDED.
        // Если щит активен — урон делится на 2 (целочисленное деление: 15 / 2 = 7).
        // &= ~SHIELDED — снимаем щит после использования (одноразовый).
        if ((statusFlags & SHIELDED) != 0) {
            // /= 2 — целочисленное деление: дробная часть отбрасывается.
            // Пример: 15 / 2 = 7 (не 7.5!). Это свойство деления int в Java.
            baseDamage /= 2;
            statusFlags &= ~SHIELDED;
            System.out.println("Щит поглощает часть урона!");
        }

        // ===== ВЫЧИСЛЕНИЕ РЕАЛЬНОГО УРОНА ПЕРЕД ВЫВОДОМ =====
        //
        // hero.takeDamage(baseDamage) внутри вычисляет: Math.max(1, damage - defense).
        // Чтобы показать игроку РЕАЛЬНЫЙ урон (после вычета защиты), вычисляем его здесь ДО вызова.
        // Math.max(1, baseDamage - hero.getDefense()) — минимум 1 урон, даже если защита > атаки.
        // Это та же формула, что в GameCharacter.takeDamage() — дублируем для корректного вывода.
        int actualDamage = Math.max(1, baseDamage - hero.getDefense());

        hero.takeDamage(baseDamage);
        totalDamageReceived += actualDamage;

        // ===== heroTookDamageThisBattle — ФЛАГ ПОЛУЧЕНИЯ УРОНА =====
        //
        // boolean-флаг для проверки достижения FLAWLESS (победа без урона).
        // Устанавливается в true ПРИ ЛЮБОМ получении урона (атака врага или яд).
        // Проверяется в checkAchievements() после боя:
        //   if (!heroTookDamageThisBattle) → achievements.add(FLAWLESS).
        //
        // Это исправление бага: раньше проверяли battleLog.stream().anyMatch(),
        // но нужная строка никогда не записывалась в лог. Явный флаг надёжнее.
        heroTookDamageThisBattle = true;

        // ===== ОТОБРАЖЕНИЕ РЕАЛЬНОГО УРОНА =====
        //
        // Показываем actualDamage (после вычета защиты), а не baseDamage (сырая атака врага).
        // Раньше здесь показывался baseDamage — игрок видел "наносит 15 урона",
        // но на самом деле получал только 5 (при защите 10). Это вводило в заблуждение.
        System.out.println(enemy.getName() + " атакует и наносит " + actualDamage + " урона!");

        battleLog.add(enemy.getName() + " атакует: " + actualDamage + " урона");

        // С вероятностью 20% враг отравляет героя.
        // |= POISONED — устанавливает бит яда (OR, см. handleDefend()).
        if (Math.random() < 0.2) {
            statusFlags |= POISONED;
            System.out.println(enemy.getName() + " отравляет героя!");
        }
    }

    // ===== МЕТОД displayStatusEffects() — ОТОБРАЖЕНИЕ ЭФФЕКТОВ =====
    //
    // Показывает активные статусные эффекты, проверяя каждый бит в statusFlags.
    // Демонстрирует паттерн «параллельные массивы» (parallel arrays):
    //   statusNames[i] и statusBits[i] описывают один и тот же эффект.
    //   Индекс 0: "Отравлен" ↔ POISONED, Индекс 1: "Оглушён" ↔ STUNNED и т.д.
    //
    // Альтернатива параллельным массивам: Map<Byte, String> или enum с полями.
    // Параллельные массивы проще, но хрупки: при изменении одного массива легко забыть второй.

    private void displayStatusEffects() {
        // statusFlags == 0 — ни один бит не установлен (нет эффектов).
        // Ранний возврат (early return) — упрощает код, убирая лишний уровень вложенности.
        if (statusFlags == 0) {
            return;
        }

        // String[] — массив строк (названия эффектов для отображения).
        // byte[] — массив байтов (побитовые маски для проверки).
        String[] statusNames = {"Отравлен", "Оглушён", "Щит", "Ярость"};
        byte[] statusBits = {POISONED, STUNNED, SHIELDED, ENRAGED};

        // System.out.print() — вывод БЕЗ перевода строки (в отличие от println()).
        System.out.print("  Эффекты: ");

        // Классический цикл for по индексу — нужен для доступа к обоим массивам одновременно.
        // for-each здесь не подойдёт: нужен индекс i для statusNames[i] И statusBits[i].
        for (int i = 0; i < statusBits.length; i++) {
            // (statusFlags & statusBits[i]) != 0 — проверка конкретного бита.
            if ((statusFlags & statusBits[i]) != 0) {
                System.out.print("[" + statusNames[i] + "] ");
            }
        }
        System.out.println();  // Перевод строки после всех эффектов.
    }

    // ===== МЕТОД logAction() — ЛОГИРОВАНИЕ С VARARGS (глава 2.7) =====
    //
    // String... parts — VARARGS (variable arguments, переменное число аргументов).
    // Можно вызвать с любым количеством строк:
    //   logAction("Атака")                          → 1 аргумент
    //   logAction("Атака", "Иван", "25")            → 3 аргумента
    //   logAction()                                  → 0 аргументов (пустой массив)
    //
    // Внутри метода parts — обычный массив String[].
    // Компилятор автоматически оборачивает аргументы в массив при вызове.
    //
    // Ограничение: varargs может быть ТОЛЬКО ПОСЛЕДНИМ параметром метода.
    //   void foo(int a, String... parts) — OK.
    //   void foo(String... parts, int a) — ОШИБКА КОМПИЛЯЦИИ!
    //
    // Реализация использует String.join() — это эффективнее ручного цикла:
    //   String.join(" | ", parts) соединяет все части без лишнего разделителя в конце.
    //   Альтернатива через StringBuilder: sb.append(part).append(" | ") для каждой части,
    //   но тогда последний разделитель нужно обрезать вручную: sb.setLength(sb.length() - 3).

    private void logAction(String... parts) {
        // ===== String.join() — СОЕДИНЕНИЕ СТРОК С РАЗДЕЛИТЕЛЕМ =====
        //
        // String.join(delimiter, elements) — статический метод класса String (Java 8+).
        // Соединяет все элементы массива/коллекции через указанный разделитель.
        //
        // Преимущество перед ручным циклом:
        //   - НЕ добавляет разделитель после ПОСЛЕДНЕГО элемента.
        //   - Одна строка вместо цикла с StringBuilder.
        //   - Внутри использует StringJoiner, который эффективно работает с StringBuilder.
        //
        // Пример: String.join(" | ", "Атака", "Иван", "25") → "Атака | Иван | 25"
        //   (без лишнего " | " в конце — раньше было "Атака | Иван | 25 | ")
        //
        // String... parts — varargs (переменное число аргументов, глава 2.8).
        // Внутри метода parts — обычный String[], поэтому String.join() принимает его.
        System.out.println("[LOG] " + String.join(" | ", parts));
    }

    // ===== МЕТОД showFinalStats() — ФИНАЛЬНАЯ СТАТИСТИКА =====
    //
    // Показывает итоги игры: статистику, достижения, бестиарий, таблицу рекордов.
    //
    // Демонстрирует:
    //   - Создание record-объекта BattleStats (контейнер данных)
    //   - Приведение типов: (double), (int), (long) — расширяющее и сужающее
    //   - TreeSet<BattleRecord> — отсортированное множество для таблицы рекордов
    //   - Comparable — интерфейс для естественного порядка сортировки
    //   - for-each по Iterable (Bestiary)
    //   - Вложенные тернарные операторы (цепочка условий)

    private void showFinalStats() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║         ИТОГИ ПРИКЛЮЧЕНИЯ           ║");
        System.out.println("╠══════════════════════════════════════╣");

        // ===== СОЗДАНИЕ record BattleStats =====
        //
        // BattleStats — record (неизменяемый класс данных, Java 16+).
        // Канонический конструктор принимает ВСЕ поля record в порядке объявления.
        // record автоматически генерирует equals(), hashCode(), toString()
        // (см. подробное описание record в BattleStats.java).
        //
        // stats.display() — пользовательский метод record для вывода статистики.
        // record может содержать обычные методы (не только автосгенерированные геттеры).
        BattleStats stats = new BattleStats(
                hero.getName(),
                totalDamageDealt,
                totalDamageReceived,
                enemiesDefeated,
                totalHealing
        );
        stats.display();

        // ===== ПРИВЕДЕНИЕ ТИПОВ (casting) =====
        //
        // (double) enemiesDefeated — РАСШИРЯЮЩЕЕ приведение: int → double.
        //   Без cast: 3 / 5 = 0 (целочисленное деление, дробная часть отброшена!).
        //   С cast: 3.0 / 5 = 0.6 (деление double / int = double).
        //   Затем * 100 = 60.0.
        //
        // (int) winRate — СУЖАЮЩЕЕ приведение: double → int.
        //   Отбрасывает дробную часть: 60.7 → 60 (НЕ округляет!).
        //   Для округления используйте Math.round(): Math.round(60.7) → 61.
        //
        // (long) totalDamageDealt — РАСШИРЯЮЩЕЕ: int → long.
        //   Нужно, чтобы избежать переполнения при умножении.
        //   int * int может превысить Integer.MAX_VALUE (2 147 483 647).
        //   long вмещает значения до 9 223 372 036 854 775 807.
        double winRate = (double) enemiesDefeated / MAX_ENEMIES * 100;
        int winRateInt = (int) winRate;
        long totalScore = (long) totalDamageDealt * enemiesDefeated;

        System.out.println("║  Процент побед: " + winRateInt + "%");
        System.out.println("║  Общий счёт: " + totalScore);

        // ===== char — СИМВОЛЬНЫЙ ТИП =====
        //
        // char — примитивный тип для одного символа Unicode (16 бит, от 0 до 65535).
        // Символьные литералы пишутся в одинарных кавычках: 'S', 'A', 'B'.
        // char можно использовать в арифметике: 'A' + 1 = 66 = 'B'.
        //
        // if-else if-else — лестница условий (chain of conditions).
        // Проверяется сверху вниз: первое истинное условие определяет значение rank.
        // Условия расположены от частного к общему: 100% → 80% → 60% → 40% → остальные.
        char rank;
        if (winRateInt == 100) {
            rank = 'S';
        } else if (winRateInt >= 80) {
            rank = 'A';
        } else if (winRateInt >= 60) {
            rank = 'B';
        } else if (winRateInt >= 40) {
            rank = 'C';
        } else {
            rank = 'D';
        }
        System.out.println("║  Ранг: " + rank);

        // Составные операторы присваивания: +=, *=
        // bonus += 100 эквивалентно bonus = bonus + 100.
        // bonus *= hero.getLevel() эквивалентно bonus = bonus * hero.getLevel().
        int bonus = 0;
        bonus += 100;
        bonus *= hero.getLevel();
        System.out.println("║  Бонус за уровень: " + bonus);

        // ===== ЛОГИЧЕСКИЕ ОПЕРАТОРЫ && и || =====
        //
        // && (И, short-circuit) — true только когда ОБА условия true.
        //   Если левое == false → правое НЕ вычисляется (оптимизация).
        //
        // || (ИЛИ, short-circuit) — true когда ХОТЯ БЫ ОДНО условие true.
        //   Если левое == true → правое НЕ вычисляется.
        boolean isChampion = enemiesDefeated == MAX_ENEMIES && hero.isAlive();
        boolean isGoodResult = enemiesDefeated >= 3 || winRateInt >= 60;

        // ===== ВЛОЖЕННЫЙ ТЕРНАРНЫЙ ОПЕРАТОР (цепочка) =====
        //
        // условие1 ? значение1 : условие2 ? значение2 : значение3
        //
        // Читается как:
        //   if (isChampion) → "Вы — настоящий чемпион!"
        //   else if (isGoodResult) → "Неплохой результат..."
        //   else → "В следующий раз..."
        //
        // ВНИМАНИЕ: вложенные тернарные операторы ухудшают читаемость.
        // В реальном коде лучше использовать if-else или switch.
        // Здесь используется для демонстрации.
        String finalMessage = isChampion ? "Вы — настоящий чемпион!"
                : isGoodResult ? "Неплохой результат, продолжайте тренироваться!"
                : "В следующий раз повезёт больше.";
        System.out.println("║");
        System.out.println("║  " + finalMessage);

        // ===== ИНТЕГРАЦИЯ НОВЫХ СИСТЕМ В ФИНАЛЬНУЮ СТАТИСТИКУ =====

        // Достижения (5.4).
        System.out.println("║");
        System.out.println("║  ── Достижения ──");
        if (achievements.isEmpty()) {
            System.out.println("║  Нет достижений.");
        } else {
            for (Achievement a : achievements) {
                System.out.println("║  " + a);
            }
        }
        // Achievement.values().length — общее количество достижений (см. showAchievements()).
        System.out.println("║  Всего: " + achievements.size() + "/" + Achievement.values().length);

        // Бестиарий (5.9).
        System.out.println("║");
        System.out.println("║  ── Бестиарий ──");
        System.out.println("║  Уникальных врагов встречено: " + bestiary.size());

        // ===== for-each ПО Iterable (Bestiary реализует Iterable) =====
        //
        // Bestiary реализует Iterable<Map.Entry<String, BestiaryEntry>>,
        // поэтому можно использовать for-each (см. подробное описание в handleUseItem()).
        //
        // var entry — компилятор определяет тип: Map.Entry<String, BestiaryEntry>.
        // entry.getKey() — имя врага (String), ключ в TreeMap.
        // entry.getValue() — запись бестиария (BestiaryEntry), значение в TreeMap.
        //   .killCount() — геттер record BestiaryEntry (количество убийств).
        //
        // TreeMap (внутри Bestiary) гарантирует обход в алфавитном порядке ключей.
        for (var entry : bestiary) {
            System.out.println("║    " + entry.getKey() + ": "
                    + entry.getValue().killCount() + " убийств");
        }

        // Журнал квестов (5.3).
        System.out.println("║");
        System.out.println("║  ── Журнал ──");
        System.out.println("║  Записей: " + questLog.size());

        System.out.println("╚══════════════════════════════════════╝");

        // ===== ДОБАВЛЕНИЕ В ТАБЛИЦУ РЕКОРДОВ — TreeSet + Comparable (глава 5.5) =====
        //
        // TreeSet<BattleRecord> — отсортированное множество на красно-чёрном дереве.
        //
        // Как TreeSet поддерживает порядок:
        //   BattleRecord реализует интерфейс Comparable<BattleRecord>.
        //   Comparable требует реализовать метод compareTo(BattleRecord other):
        //     - Возвращает < 0, если this < other.
        //     - Возвращает > 0, если this > other.
        //     - Возвращает 0, если this == other (элементы считаются «равными» для TreeSet!).
        //   BattleRecord.compareTo() сортирует по score УБЫВАНИЕ → лучший рекорд первый.
        //
        // TreeSet.add() — вставляет элемент в ПРАВИЛЬНУЮ позицию: O(log n).
        //   Внутри: проходит по дереву, сравнивая через compareTo(), и находит место.
        //   Если compareTo() вернул 0 — элемент НЕ добавляется (Set не хранит дубликатов!).
        //
        // ВАЖНО: для TreeSet «равенство» определяется через compareTo(), а НЕ через equals()!
        //   Если compareTo() возвращает 0 для двух разных объектов — TreeSet сочтёт их одинаковыми
        //   и второй не добавит. Это частая ловушка!
        //
        // System.currentTimeMillis() — текущее время в миллисекундах (с 1 января 1970 UTC).
        // Используется для различения рекордов с одинаковым score.
        BattleRecord record = new BattleRecord(
                hero.getName(),
                totalScore,
                enemiesDefeated,
                System.currentTimeMillis()
        );
        leaderboard.add(record);

        // ===== ОБХОД TreeSet — ГАРАНТИРОВАННЫЙ ПОРЯДОК =====
        //
        // В отличие от HashSet (порядок не определён), TreeSet гарантирует
        // обход в порядке, определённом compareTo() (для BattleRecord — по убыванию score).
        //
        // for-each по TreeSet: первый элемент = лучший рекорд.
        // br.heroName(), br.score(), br.enemiesDefeated() — геттеры record
        //   (автогенерированные, без префикса get).
        //
        // if (place > 5) break — выход из цикла for-each (показываем только топ-5).
        // break прерывает БЛИЖАЙШИЙ цикл. Без break цикл прошёл бы по всем записям.
        System.out.println();
        System.out.println("══════ ТАБЛИЦА РЕКОРДОВ ══════");
        int place = 1;
        for (BattleRecord br : leaderboard) {
            System.out.println("  " + place + ". " + br.heroName()
                    + " — Счёт: " + br.score()
                    + " | Врагов: " + br.enemiesDefeated());
            place++;
            if (place > 5) break;
        }
        System.out.println("══════════════════════════════");

        System.out.println();
        System.out.println("Спасибо за игру!");
    }

    // ===== МЕТОД readInt() — БЕЗОПАСНОЕ ЧТЕНИЕ ЧИСЛА (глава 4.1, 4.2) =====
    //
    // Читает строку от пользователя и преобразует в int.
    // При ошибке возвращает -1 (невалидный выбор, обработается в switch как default).
    //
    // private — вызывается только из Game (heroTurn, handleUseItem и т.д.).
    // int — возвращаемый примитивный тип (число, введённое пользователем).
    //
    // Демонстрирует:
    //   - try-catch-finally — полная структура обработки исключений
    //   - Multi-catch (Java 7+) — один catch для нескольких типов исключений
    //   - finally — блок гарантированного выполнения
    //   - Integer.parseInt() — преобразование String → int
    //   - Scanner.nextLine().trim() — чтение строки с удалением пробелов

    private int readInt() {
        // ===== try-catch-finally С MULTI-CATCH (глава 4.1) =====
        //
        // Структура обработки исключений:
        //   try { код, который может бросить исключение }
        //   catch (Тип1 | Тип2 e) { обработка ошибки }
        //   finally { гарантированный код }
        //
        // MULTI-CATCH (Java 7+): catch (NumberFormatException | NoSuchElementException e)
        // Ловим ДВА типа исключений ОДНИМ блоком catch:
        //   - NumberFormatException — пользователь ввёл не число ("abc", "", "1.5").
        //     Бросается Integer.parseInt() при невалидном вводе.
        //   - NoSuchElementException — Scanner не может прочитать (stdin закрыт, EOF).
        //     Бросается scanner.nextLine() при отсутствии данных.
        //
        // Оба — unchecked (наследники RuntimeException), но мы ловим для безопасности:
        //   без catch программа аварийно завершится при неправильном вводе.
        try {
            // scanner.nextLine() — читает целую строку (до Enter) из ввода.
            // .trim() — удаляет пробелы по краям: " 42 " → "42".
            // Integer.parseInt("42") — преобразует строку в int. Бросит NumberFormatException,
            //   если строка не является числом ("abc") или выходит за диапазон int.
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException | NoSuchElementException e) {
            // Обработка одинаковая для обоих типов — multi-catch идеален.
            // Возвращаем -1 как маркер ошибки: в switch это обработается веткой default.
            return -1;
        } finally {
            // ===== finally — БЛОК ГАРАНТИРОВАННОГО ВЫПОЛНЕНИЯ (глава 4.1) =====
            //
            // finally выполняется ВСЕГДА, независимо от того:
            //   - Завершился ли try успешно → finally → return из try.
            //   - Произошло ли исключение → catch → finally → return из catch.
            //
            // ПОРЯДОК ВЫПОЛНЕНИЯ с return:
            //   1. try: Integer.parseInt(...) → успех или исключение.
            //   2. Java ЗАПОМИНАЕТ значение return (из try или catch).
            //   3. finally: выполняется код блока.
            //   4. Java ВОЗВРАЩАЕТ запомненное значение.
            //
            // Здесь finally пустой — используется для демонстрации.
            // В реальном коде readInt() не нуждается в finally (нет ресурсов для очистки).
            //
            // Типичные применения finally:
            //   - lock.unlock() — освобождение блокировки.
            //   - connection.close() — закрытие соединения (лучше try-with-resources).
            //   - Логирование: log.debug("Операция завершена").
            //
            // ЧАСТАЯ ОШИБКА: return в finally «перебьёт» return из try/catch!
            //   try { return 42; } finally { return -1; } → вернёт -1, а не 42!
            //   Никогда не пишите return в finally.
        }
    }
}
