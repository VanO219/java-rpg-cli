// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в DamageType.java).
package rpg;

// ===== ИМПОРТЫ КОЛЛЕКЦИЙ (глава 5.8, 5.9, 5.10) =====
//
// Java Collections Framework — набор интерфейсов и классов для хранения и обработки данных.
// Все коллекции находятся в пакете java.util.
//
// ===== ИЕРАРХИЯ ИНТЕРФЕЙСОВ MAP =====
//
// Map<K,V>                — базовый интерфейс: хранит пары ключ-значение.
//   └─ SortedMap<K,V>     — ключи ОТСОРТИРОВАНЫ, методы: firstKey(), lastKey(), subMap().
//       └─ NavigableMap<K,V> — расширение SortedMap с навигацией: headMap(), tailMap(),
//                              floorKey(), ceilingKey(), higherKey(), lowerKey().
//
// TreeMap<K,V> реализует NavigableMap → получает ВСЕ методы иерархии.
//
// TreeMap — реализация Map на основе красно-чёрного дерева (Red-Black Tree).
// Ключи ВСЕГДА отсортированы по natural ordering (Comparable) или по Comparator.
import java.util.TreeMap;

// NavigableMap — интерфейс расширяющий SortedMap дополнительными методами навигации.
// Используется как тип параметра/возвращаемого значения для гибкости.
// Принцип: программируй на уровне ИНТЕРФЕЙСА, а не реализации.
//   NavigableMap<K,V> map = new TreeMap<>();  // ХОРОШО — можно заменить реализацию
//   TreeMap<K,V> map = new TreeMap<>();       // ДОПУСТИМО, если нужны методы TreeMap
import java.util.NavigableMap;

// Map — базовый интерфейс для всех словарей (ключ → значение).
// Map.Entry<K,V> — вложенный интерфейс: одна пара «ключ-значение» из Map.
//   entry.getKey()   — возвращает ключ.
//   entry.getValue() — возвращает значение.
// Используется при итерации по Map через entrySet().
import java.util.Map;

// Iterator<T> — интерфейс для последовательного обхода элементов коллекции.
// (Подробное объяснение Iterator — см. Inventory.java)
// Три метода:
//   hasNext() — есть ли ещё элементы? (true/false)
//   next()    — вернуть следующий элемент и перейти к следующему.
//   remove()  — удалить последний возвращённый элемент (необязательный метод).
import java.util.Iterator;

// ===== БЕСТИАРИЙ — TREEMAP И NAVIGABLEMAP (глава 5.9, 5.10) =====
//
// Bestiary — справочник побеждённых врагов, отсортированный по имени.
// Использует TreeMap<String, BestiaryEntry>:
//   Ключ (String) — имя врага, автоматически сортируется по алфавиту.
//   Значение (BestiaryEntry) — данные о враге (количество убийств, макс. урон, ранг).
//
// ===== TreeMap vs HashMap — КОГДА ЧТО ВЫБИРАТЬ (5.8 vs 5.9) =====
//
// ┌─────────────┬──────────────────────────────┬──────────────────────────────┐
// │             │ HashMap                      │ TreeMap                      │
// ├─────────────┼──────────────────────────────┼──────────────────────────────┤
// │ Структура   │ Хеш-таблица (массив корзин)  │ Красно-чёрное дерево         │
// │ Порядок     │ НЕ определён                 │ Ключи ОТСОРТИРОВАНЫ          │
// │ get/put     │ O(1) — мгновенно             │ O(log n) — логарифмический   │
// │ Навигация   │ НЕТ                          │ headMap, tailMap, subMap      │
// │ null ключи  │ Допускает 1 null-ключ        │ НЕ допускает null-ключи      │
// │ Потоко-     │ Не синхронизирован            │ Не синхронизирован           │
// │ безопасность│                              │                              │
// └─────────────┴──────────────────────────────┴──────────────────────────────┘
//
// ВЫБИРАЙ HashMap, когда:
//   - Нужен быстрый доступ по ключу (O(1)), порядок не важен.
//   - Пример: кэш пользователей по ID, подсчёт частот слов.
//
// ВЫБИРАЙ TreeMap, когда:
//   - Нужен ОТСОРТИРОВАННЫЙ порядок ключей или навигация по диапазонам.
//   - Пример: бестиарий (враги по алфавиту), расписание (события по времени).
//
// ===== implements Iterable — ВОЗМОЖНОСТЬ ИСПОЛЬЗОВАНИЯ В for-each (5.10) =====
//
// Iterable<T> — интерфейс из java.lang (не нужно импортировать).
// Содержит один абстрактный метод: Iterator<T> iterator().
// (Подробное объяснение Iterable — см. Inventory.java)
//
// Если класс реализует Iterable<T>, его можно использовать в for-each:
//   for (var entry : bestiary) { ... }
//
// Bestiary реализует Iterable<Map.Entry<String, BestiaryEntry>>, что означает:
//   каждый элемент при итерации — это пара (имя врага, данные о враге).
//
// Map.Entry<String, Bestiary.BestiaryEntry> — обобщённый тип (generic type).
//   Map.Entry — это интерфейс с двумя параметрами типа: <K, V>.
//   K = String (ключ — имя врага), V = Bestiary.BestiaryEntry (значение — данные).
//   Bestiary.BestiaryEntry — обращение к вложенному типу через имя внешнего класса.
public class Bestiary implements Iterable<Map.Entry<String, Bestiary.BestiaryEntry>> {

    // ===== ВЛОЖЕННЫЙ record (Inner Record) — НЕИЗМЕНЯЕМАЯ ЗАПИСЬ (глава 3.19) =====
    //
    // BestiaryEntry — запись об одном типе врага в бестиарии.
    //
    // Вложенный тип (nested type) — класс или record, объявленный ВНУТРИ другого класса.
    // Зачем вкладывать?
    //   - BestiaryEntry логически связан ТОЛЬКО с Bestiary (не используется отдельно).
    //   - Не «засоряет» пакет rpg лишним файлом.
    //   - Доступ извне: Bestiary.BestiaryEntry (через имя внешнего класса).
    //
    // public — доступен извне класса Bestiary (нужен для Iterable и для вызывающего кода).
    // record — неявно static (вложенный record не имеет ссылки на внешний объект).
    //   Это означает: для создания BestiaryEntry НЕ нужен экземпляр Bestiary.
    //   В отличие от inner class, который привязан к экземпляру внешнего класса.
    //
    // record — неизменяем (immutable): все поля private final, значения нельзя изменить.
    // Для «изменения» создаём НОВЫЙ объект (паттерн withKill() — см. ниже).
    public record BestiaryEntry(
            // Количество убийств этого типа врага. int — примитивный 32-битный тип.
            int killCount,

            // Максимальный урон, нанесённый этому типу врага за один удар.
            // Обновляется при каждом бое, если новый урон превышает рекорд.
            int maxDamageDealt,

            // Ранг врага (COMMON, ELITE, BOSS).
            // EnemyRank — enum из файла EnemyRank.java (см. подробное объяснение там).
            EnemyRank rank,

            // Временная метка первой встречи с этим врагом (epoch time в миллисекундах).
            // long — 64-битное целое, необходим для хранения миллисекунд Unix-времени.
            long firstEncounter
    ) {
        // ===== ПАТТЕРН «WITH-МЕТОД» (Wither Pattern) =====
        //
        // record неизменяем — мы НЕ можем написать this.killCount++ или setKillCount().
        // Вместо модификации существующего объекта создаём НОВЫЙ с изменёнными данными.
        //
        // Это называется «wither» (по аналогии с «getter» и «setter»):
        //   getter — получает значение поля.
        //   setter — устанавливает значение поля (МУТИРУЕТ объект).
        //   wither — возвращает НОВЫЙ объект с изменённым полем (НЕ мутирует).
        //
        // Преимущества неизменяемости:
        //   1. Потокобезопасность: можно безопасно передавать между потоками.
        //   2. Предсказуемость: объект не изменится неожиданно.
        //   3. Безопасность в коллекциях: если объект — ключ HashMap, его hashCode не изменится.
        //
        // Недостаток: создание нового объекта на каждое «изменение» → больше объектов в памяти.
        // Для игрового бестиария это несущественно (десятки объектов, не миллионы).

        /**
         * Создаёт новую запись с увеличенным счётчиком убийств и обновлённым максимальным уроном.
         * Текущий объект (this) НЕ изменяется — создаётся НОВЫЙ.
         *
         * @param newDamage урон, нанесённый в текущем бою
         * @return новый BestiaryEntry с killCount + 1 и обновлённым maxDamageDealt
         */
        public BestiaryEntry withKill(int newDamage) {
            // Math.max(a, b) — статический метод класса Math, возвращает большее из двух значений.
            // Обновляем maxDamageDealt, только если новый урон больше предыдущего рекорда.
            //
            // new BestiaryEntry(...) — создаём новый объект record через канонический конструктор.
            // Передаём обновлённые значения (killCount + 1, max урон) и неизменённые (rank, firstEncounter).
            return new BestiaryEntry(
                    killCount + 1,                          // увеличиваем счётчик
                    Math.max(maxDamageDealt, newDamage),    // обновляем рекорд урона
                    rank,                                    // ранг не меняется
                    firstEncounter                           // время первой встречи не меняется
            );
        }
    }

    // ===== ПОЛЕ: TreeMap =====
    //
    // TreeMap<String, BestiaryEntry> — отсортированная карта (словарь).
    //   Ключ: String (имя врага) — String реализует Comparable<String>,
    //          поэтому ключи сортируются по алфавиту (natural ordering).
    //   Значение: BestiaryEntry — данные о враге (record).
    //
    // new TreeMap<>() — создаём пустую карту. <> — diamond operator (компилятор выводит типы).
    //
    // private — доступно только внутри класса Bestiary (инкапсуляция).
    // final — ссылку на объект TreeMap нельзя переназначить (entries = new TreeMap<>() — ошибка).
    //   НО содержимое TreeMap можно менять (put, remove) — final защищает только ССЫЛКУ.
    //   Аналогия: final ящик — нельзя заменить ящик, но можно менять содержимое.
    private final TreeMap<String, BestiaryEntry> entries = new TreeMap<>();

    /**
     * Добавляет или обновляет запись о враге в бестиарии.
     * Если враг уже встречался — увеличивает счётчик убийств и обновляет макс. урон.
     * Если враг новый — создаёт первую запись.
     *
     * @param enemyName  имя врага (ключ в TreeMap)
     * @param rank       ранг врага (COMMON, ELITE, BOSS)
     * @param damageDealt урон, нанесённый врагу в этом бою
     */
    public void addEntry(String enemyName, EnemyRank rank, int damageDealt) {
        // ===== Map.containsKey() — ПРОВЕРКА НАЛИЧИЯ КЛЮЧА (5.8) =====
        //
        // containsKey(key) — возвращает true, если ключ существует в Map.
        // В TreeMap поиск по ключу — O(log n) (обход красно-чёрного дерева).
        // В HashMap поиск — O(1) (по хеш-коду).
        //
        // Альтернативный способ (более компактный):
        //   entries.merge(enemyName, newEntry, (old, nw) -> old.withKill(damageDealt));
        //   merge() (Java 8+) — добавляет новую запись или объединяет с существующей.
        // Здесь используем containsKey() для наглядности (учебный проект).
        if (entries.containsKey(enemyName)) {
            // ===== Map.get() + Map.put() — ЧТЕНИЕ И ОБНОВЛЕНИЕ (5.8) =====
            //
            // get(key) — возвращает значение по ключу, или null если ключа нет.
            // put(key, value) — добавляет или заменяет значение по ключу.
            //   Если ключ уже был — старое значение перезаписывается новым.
            //   put() возвращает предыдущее значение (или null).
            //
            // Обновляем запись: создаём новую с withKill() (record неизменяем — см. выше).
            BestiaryEntry existing = entries.get(enemyName);
            entries.put(enemyName, existing.withKill(damageDealt));
        } else {
            // Первая встреча с этим врагом — создаём новую запись.
            // System.currentTimeMillis() — текущее время в миллисекундах (epoch time).
            // Аргументы конструктора BestiaryEntry: killCount=1, maxDamage, rank, timestamp.
            entries.put(enemyName, new BestiaryEntry(1, damageDealt, rank, System.currentTimeMillis()));
        }
    }

    /**
     * Выводит содержимое бестиария в консоль.
     * Записи отсортированы по имени врага (TreeMap сортирует ключи автоматически).
     */
    public void display() {
        // ===== Map.isEmpty() — ПРОВЕРКА ПУСТОТЫ КОЛЛЕКЦИИ =====
        //
        // isEmpty() — возвращает true, если в Map нет ни одной пары.
        // Эквивалентно: entries.size() == 0, но isEmpty() читабельнее.
        if (entries.isEmpty()) {
            System.out.println("Бестиарий пуст. Победите врагов, чтобы заполнить его!");
            return; // Ранний возврат — выходим из метода, не выполняя остальной код.
        }

        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║            БЕСТИАРИЙ                 ║");
        System.out.println("╠══════════════════════════════════════╣");

        // ===== Map.entrySet() — ПЕРЕБОР ПАР КЛЮЧ-ЗНАЧЕНИЕ (5.8) =====
        //
        // entrySet() — возвращает Set<Map.Entry<K, V>> — множество ВСЕХ пар (ключ, значение).
        // Map.Entry<K, V> — вложенный интерфейс Map, представляющий одну пару.
        //   entry.getKey()   — возвращает ключ (String — имя врага).
        //   entry.getValue() — возвращает значение (BestiaryEntry — данные о враге).
        //
        // for-each по entrySet() — стандартный идиоматический способ перебора Map.
        // В TreeMap порядок обхода — по ВОЗРАСТАНИЮ ключей (алфавитный порядок имён врагов).
        //
        // Другие способы перебора Map:
        //   entries.keySet()    — Set<K> — только ключи.
        //   entries.values()    — Collection<V> — только значения.
        //   entries.forEach((k, v) -> ...)  — лямбда-способ (Java 8+).
        for (Map.Entry<String, BestiaryEntry> entry : entries.entrySet()) {
            // entry.getKey() — имя врага (String).
            String name = entry.getKey();
            // entry.getValue() — данные о враге (BestiaryEntry record).
            BestiaryEntry data = entry.getValue();

            // data.rank() — вызов геттера record (без префикса get).
            // data.killCount() и data.maxDamageDealt() — аналогично.
            System.out.println("║  " + name + " (" + data.rank() + ")");
            System.out.println("║    Убито: " + data.killCount()
                    + " | Макс. урон: " + data.maxDamageDealt());
        }

        System.out.println("╚══════════════════════════════════════╝");

        // ===== ДЕМОНСТРАЦИЯ NavigableMap (5.9) =====
        //
        // NavigableMap — расширение SortedMap с дополнительными методами навигации.
        // TreeMap реализует NavigableMap, поэтому все эти методы доступны напрямую.
        //
        // МЕТОДЫ НАВИГАЦИИ:
        //
        // firstKey() — возвращает НАИМЕНЬШИЙ ключ (первый по алфавиту).
        //   Бросает NoSuchElementException, если карта пуста (мы уже проверили isEmpty).
        //
        // lastKey()  — возвращает НАИБОЛЬШИЙ ключ (последний по алфавиту).
        System.out.println("  Первый по алфавиту: " + entries.firstKey());
        System.out.println("  Последний по алфавиту: " + entries.lastKey());

        // ===== headMap(), tailMap(), subMap() — ВЫБОРКА ДИАПАЗОНА (5.9) =====
        //
        // Эти методы возвращают ПРЕДСТАВЛЕНИЕ (view) — не копию данных!
        // Изменения в представлении отразятся в оригинальной карте, и наоборот.
        //
        // headMap(toKey) — подкарта с ключами СТРОГО МЕНЬШЕ toKey (не включая toKey).
        //   entries.headMap("М") → все враги, чьё имя < "М" (например: "Гоблин", "Дракон").
        //   headMap(toKey, inclusive) — с параметром inclusive: true — включая toKey.
        //
        // tailMap(fromKey) — подкарта с ключами >= fromKey (ВКЛЮЧАЯ fromKey).
        //   entries.tailMap("М") → все враги, чьё имя >= "М" (например: "Минотавр", "Скелет").
        //   tailMap(fromKey, inclusive) — с параметром inclusive: false — не включая fromKey.
        //
        // subMap(fromKey, toKey) — подкарта от fromKey (ВКЛЮЧИТЕЛЬНО) до toKey (НЕ включая).
        //   entries.subMap("Г", "Д") → все враги на букву "Г" (например: "Гоблин", "Горгулья").
        //   subMap(fromKey, fromInclusive, toKey, toInclusive) — полная форма с двумя флагами.
        //
        // Дополнительные методы NavigableMap:
        //   floorKey(key)   — наибольший ключ <= key (или null).
        //   ceilingKey(key) — наименьший ключ >= key (или null).
        //   lowerKey(key)   — наибольший ключ СТРОГО < key (или null).
        //   higherKey(key)  — наименьший ключ СТРОГО > key (или null).
        //
        // ВНИМАНИЕ: view-объекты бросают IllegalArgumentException,
        // если попытаться добавить ключ вне диапазона!
        if (entries.size() > 1) {
            System.out.println("  Записей в бестиарии: " + entries.size());
        }
    }

    /**
     * Возвращает количество уникальных врагов в бестиарии.
     * Каждый враг учитывается один раз, независимо от количества убийств.
     *
     * @return размер бестиария (количество уникальных имён врагов)
     */
    public int size() {
        // Map.size() — количество пар ключ-значение в карте.
        return entries.size();
    }

    /**
     * Проверяет, есть ли враг в бестиарии (встречался ли ранее).
     *
     * @param enemyName имя врага для проверки
     * @return true, если враг уже встречался (ключ существует в TreeMap)
     */
    public boolean contains(String enemyName) {
        // containsKey() — O(log n) для TreeMap (обход дерева), O(1) для HashMap.
        return entries.containsKey(enemyName);
    }

    // ===== РЕАЛИЗАЦИЯ Iterable — МЕТОД iterator() (5.10) =====
    //
    // iterator() — единственный ОБЯЗАТЕЛЬНЫЙ метод интерфейса Iterable<T>.
    // Возвращает объект Iterator<T>, через который for-each перебирает элементы.
    //
    // @Override — подтверждаем, что переопределяем метод интерфейса Iterable.
    //
    // Благодаря этому методу Bestiary можно использовать в for-each:
    //   for (Map.Entry<String, BestiaryEntry> entry : bestiary) {
    //       System.out.println(entry.getKey() + ": " + entry.getValue());
    //   }
    //
    // Компилятор преобразует for-each в:
    //   Iterator<Map.Entry<...>> it = bestiary.iterator();
    //   while (it.hasNext()) {
    //       Map.Entry<...> entry = it.next();
    //       ...
    //   }
    //
    // ===== КАК РАБОТАЕТ Iterator (ВНУТРЕННИЕ МЕХАНИЗМЫ) =====
    //
    // Iterator — паттерн проектирования «Итератор»:
    //   - РАЗДЕЛЯЕТ хранение данных (коллекция) от обхода данных (итератор).
    //   - Коллекция не знает, в каком порядке её обходят.
    //   - Итератор не знает, как данные хранятся.
    //
    // Жизненный цикл Iterator:
    //   1. iterator() — создаёт новый итератор, указывающий ПЕРЕД первым элементом.
    //   2. hasNext()  — проверяет, есть ли следующий элемент (true/false).
    //   3. next()     — возвращает текущий элемент и ПЕРЕМЕЩАЕТ указатель к следующему.
    //   4. Повторяем hasNext() + next() до конца коллекции.
    //
    // ВАЖНО: вызов next() без проверки hasNext() бросит NoSuchElementException,
    // если элементов больше нет! Всегда проверяйте hasNext() перед next().
    //
    // Мы ДЕЛЕГИРУЕМ итерацию: возвращаем итератор из entrySet() TreeMap.
    // Это значит: мы не пишем свой итератор с нуля, а переиспользуем существующий.
    // entrySet() TreeMap уже обходит записи в порядке сортировки ключей (алфавит).
    @Override
    public Iterator<Map.Entry<String, BestiaryEntry>> iterator() {
        // entries.entrySet() — получаем Set<Map.Entry<K,V>> из TreeMap.
        // .iterator() — получаем Iterator из этого Set.
        // Итератор обходит записи в порядке сортировки ключей (алфавитный порядок имён врагов).
        return entries.entrySet().iterator();
    }
}
