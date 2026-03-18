// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в Enemy.java).
package rpg;

// ===== ИМПОРТ Iterator (глава 5.10) =====
//
// java.util.Iterator — интерфейс для последовательного обхода элементов коллекции.
// Iterator предоставляет три метода:
//   hasNext() — есть ли ещё элементы?
//   next()    — вернуть следующий элемент и перейти к следующему.
//   remove()  — удалить последний возвращённый элемент (необязательный default-метод).
//
// Iterator — это паттерн проектирования «Итератор»: разделяет ХРАНЕНИЕ данных
// от ОБХОДА данных. Коллекция хранит, итератор обходит.
// Без Iterator каждая коллекция имела бы свой способ обхода — хаос.
// С Iterator любой код, работающий с Iterator, работает с ЛЮБОЙ коллекцией.
import java.util.Iterator;

// ===== ИМПОРТ NoSuchElementException =====
//
// NoSuchElementException — unchecked исключение (наследник RuntimeException).
// Стандартное исключение для Iterator.next(), когда элементов больше нет.
// Unchecked — потому что это ошибка программиста: нужно было сначала вызвать hasNext().
// Компилятор не требует обработки unchecked исключений (но можно поймать через try-catch).
import java.util.NoSuchElementException;

// ===== Inventory<T> — ОБОБЩЁННЫЙ КЛАСС (generic class) =====
//
// <T> — параметр типа (type parameter). T — «заполнитель», вместо которого
// при создании объекта подставляется конкретный тип.
// Пример: Inventory<String> — инвентарь строк, Inventory<ItemType> — инвентарь предметов.
//
// Типобезопасность (type safety) — ГЛАВНАЯ цель дженериков:
//   - Без дженериков: Object[] хранит всё → нужно приведение типов → ошибки при выполнении.
//   - С дженериками: Inventory<String> хранит ТОЛЬКО строки → ошибки при КОМПИЛЯЦИИ.
// Ошибка при компиляции ВСЕГДА лучше ошибки при выполнении (легче найти и исправить).
//
// Раньше (до Java 5) использовали Object и приведение типов:
//   Object item = list.get(0);
//   String s = (String) item;  // ClassCastException если не String!
// Дженерики устранили эту проблему.
//
// ===== implements Iterable<Inventory<T>.Slot> — КОНТРАКТ ДЛЯ for-each =====
//
// Iterable<T> — интерфейс из java.lang (импортировать не нужно).
// Содержит ОДИН абстрактный метод: Iterator<T> iterator().
//
// Если класс реализует Iterable, его объекты можно использовать в цикле for-each:
//   for (var slot : inventory) { ... }
//
// Это работает, потому что for-each — СИНТАКСИЧЕСКИЙ САХАР (syntactic sugar).
// Компилятор преобразует for-each в код с Iterator:
//   Iterator<Slot> it = inventory.iterator();
//   while (it.hasNext()) {
//       var slot = it.next();
//       ...
//   }
//
// Итерируемый тип — Inventory<T>.Slot (внутренний класс, содержащий предмет и количество).
// Запись Inventory<T>.Slot нужна, потому что Slot — нестатический внутренний класс,
// и его полное имя включает тип внешнего класса.
public class Inventory<T> implements Iterable<Inventory<T>.Slot> {

    // ===== INNER CLASS (внутренний нестатический класс) — Slot =====
    //
    // Slot — ячейка инвентаря, хранящая один предмет с количеством.
    //
    // Inner class (нестатический) vs static nested class:
    //   Inner class (class Slot):
    //     - Привязан к ЭКЗЕМПЛЯРУ внешнего класса Inventory<T>.
    //     - Имеет неявную ссылку на внешний объект (Inventory.this).
    //     - МОЖЕТ использовать параметр типа T внешнего класса.
    //     - Создание: inventory.new Slot(item, 1) (нужен экземпляр Inventory).
    //     - В памяти: каждый Slot хранит скрытую ссылку на свой Inventory.
    //
    //   Static nested class (static class ItemInfo):
    //     - НЕ привязан к экземпляру внешнего класса.
    //     - НЕТ неявной ссылки на внешний объект.
    //     - НЕ МОЖЕТ использовать параметр T (не знает, какой тип у Inventory).
    //     - Создание: new Inventory.ItemInfo("Меч", 10) (не нужен экземпляр Inventory).
    //
    // Slot — inner class, потому что ИСПОЛЬЗУЕТ тип T из Inventory<T>.
    // Без привязки к Inventory Slot не знал бы, какой тип T хранить.
    //
    // public — Slot доступен извне (for-each возвращает Slot-объекты, их нужно использовать).
    public class Slot {

        // T item — предмет, хранящийся в ячейке.
        // Тип T берётся из внешнего класса Inventory<T>.
        // Если Inventory<String>, то item — String.
        // Если Inventory<ItemType>, то item — ItemType.
        // private — доступен только внутри Slot (инкапсуляция).
        private T item;

        // int quantity — количество предметов в этой ячейке.
        // Примитив int: не может быть null, занимает 4 байта.
        private int quantity;

        // Конструктор Slot — создаёт ячейку с предметом и его количеством.
        //
        // Параметры: T item — предмет (тип определяется параметром внешнего класса),
        //            int quantity — количество.
        public Slot(T item, int quantity) {
            // this.item — поле объекта Slot.
            // item — параметр конструктора.
            // this различает их, когда имена совпадают.
            this.item = item;
            this.quantity = quantity;
        }

        /**
         * Возвращает предмет из ячейки.
         * Тип возврата — T (обобщённый, определяется при создании Inventory).
         *
         * @return предмет в ячейке
         */
        public T getItem() {
            return item;
        }

        /**
         * Возвращает количество предметов в ячейке.
         *
         * @return количество предметов (>= 0)
         */
        public int getQuantity() {
            return quantity;
        }

        /**
         * Уменьшает количество предметов на 1 (при использовании предмета).
         * Если количество уже 0 — ничего не делает (защита от отрицательных значений).
         */
        public void decreaseQuantity() {
            if (quantity > 0) {
                quantity--;
            }
        }

        /**
         * Проверяет, пуста ли ячейка (предметов не осталось).
         *
         * @return true, если количество <= 0
         */
        public boolean isEmpty() {
            return quantity <= 0;
        }

        // @Override — переопределяем метод toString() из Object.
        // (см. подробное объяснение @Override и toString() в Enemy.java)
        // Вызывается автоматически при конкатенации со строкой: "Слот: " + slot
        @Override
        public String toString() {
            // item.toString() вызывается неявно при конкатенации (+).
            return item + " x" + quantity;
        }
    }

    // ===== STATIC NESTED CLASS (статический вложенный класс) — ItemInfo =====
    //
    // ItemInfo — справочная информация о предмете (имя и сила).
    //
    // static — ключевое слово, делающее вложенный класс НЕЗАВИСИМЫМ от экземпляра Inventory.
    //
    // Почему static?
    //   1. ItemInfo НЕ нужен доступ к полям Inventory (slots, size, capacity).
    //   2. ItemInfo НЕ использует параметр типа T.
    //   3. Без static каждый ItemInfo хранил бы скрытую ссылку на Inventory — лишний расход памяти.
    //
    // Правило выбора: если вложенный класс НЕ обращается к полям/методам внешнего экземпляра —
    // делай его static. Это экономит память и делает зависимости явными.
    //
    // Создание: new Inventory.ItemInfo("Меч", 10) — не нужен экземпляр Inventory.
    // Сравни с inner class: inventory.new Slot(item, 1) — нужен экземпляр.
    //
    // Аналогия: static nested class — как обычный класс, просто «живёт внутри» другого
    // для логической группировки. Inventory.ItemInfo говорит: «это ItemInfo для инвентаря».
    public static class ItemInfo {

        // final — значение нельзя изменить после инициализации (в конструкторе).
        // String name — название предмета (например, "Зелье здоровья").
        private final String name;

        // int value — числовая характеристика предмета (сила, стоимость и т.д.).
        private final int value;

        /**
         * Создаёт справочную информацию о предмете.
         *
         * @param name  название предмета
         * @param value числовая характеристика (сила, стоимость)
         */
        public ItemInfo(String name, int value) {
            this.name = name;
            this.value = value;
        }

        /** @return название предмета */
        public String getName() {
            return name;
        }

        /** @return числовая характеристика предмета */
        public int getValue() {
            return value;
        }

        // @Override — переопределяем toString() из Object.
        @Override
        public String toString() {
            return name + " (сила: " + value + ")";
        }
    }

    // ===== ПОЛЯ ВНЕШНЕГО КЛАССА Inventory<T> =====

    // ===== Object[] slots — МАССИВ ЯЧЕЕК (СТИРАНИЕ ТИПОВ / TYPE ERASURE) =====
    //
    // Почему Object[], а не T[]?
    // В Java дженерики реализованы через СТИРАНИЕ ТИПОВ (type erasure, глава 3.31):
    //   - При КОМПИЛЯЦИИ: компилятор проверяет типы (Inventory<String> → String).
    //   - При ВЫПОЛНЕНИИ: параметр T заменяется на Object (JVM не знает про T).
    //
    // Из-за type erasure НЕЛЬЗЯ создать массив обобщённого типа:
    //   T[] slots = new T[capacity];  // ОШИБКА КОМПИЛЯЦИИ!
    //   — JVM не знает, какой тип T → не может создать массив конкретного типа.
    //
    // Решение: создаём Object[] и приводим элементы к нужному типу при извлечении.
    // Это безопасно, потому что компилятор уже проверил типы на этапе компиляции.
    //
    // Раньше (до Java 5, без дженериков) ВСЕ коллекции хранили Object[].
    // Дженерики добавили безопасность НА ЭТАПЕ КОМПИЛЯЦИИ, но внутри всё по-прежнему Object.
    private Object[] slots;

    // size — текущее количество занятых ячеек в инвентаре.
    // Отличается от capacity: size <= capacity всегда.
    // size = 3, capacity = 10 → в инвентаре 3 предмета из 10 возможных.
    private int size;

    // capacity — максимальное количество ячеек (слотов) в инвентаре.
    // final — неизменяемо после создания объекта.
    // Задаётся один раз в конструкторе и больше не меняется.
    private final int capacity;

    /**
     * Создаёт пустой инвентарь с заданной максимальной ёмкостью.
     *
     * @param capacity максимальное количество ячеек (слотов) в инвентаре
     */
    public Inventory(int capacity) {
        this.capacity = capacity;
        // new Object[capacity] — создаём массив Object нужного размера.
        // Не new T[capacity] — из-за type erasure (см. объяснение выше).
        this.slots = new Object[capacity];
        this.size = 0;
    }

    // ===== addItem() — БРОСАЕТ CHECKED EXCEPTION (глава 4.3) =====
    //
    // РАНЬШЕ: метод возвращал boolean (true/false — успех/неуспех).
    //   Проблема: вызывающий код мог проигнорировать результат:
    //     inventory.addItem(item, 1);  // Забыли проверить return — молчаливая потеря предмета!
    //
    // ТЕПЕРЬ: метод бросает InventoryFullException (checked exception).
    //   Преимущества:
    //     1. НЕВОЗМОЖНО проигнорировать — компилятор требует try-catch или throws.
    //     2. Исключение содержит полезные данные (currentSize, maxCapacity).
    //     3. Вызывающий код чётко показывает обработку ошибки.
    //
    // throws InventoryFullException — ОБЪЯВЛЕНИЕ ИСКЛЮЧЕНИЯ В СИГНАТУРЕ МЕТОДА.
    // Для checked exceptions (наследники Exception, но не RuntimeException) объявление ОБЯЗАТЕЛЬНО.
    // Для unchecked exceptions (наследники RuntimeException) — не нужно.
    //
    // Вызывающий код должен ЛИБО:
    //   1. Обернуть вызов в try-catch:
    //      try { inventory.addItem(item, 1); } catch (InventoryFullException e) { ... }
    //   2. Пробросить дальше через throws в своей сигнатуре:
    //      public void myMethod() throws InventoryFullException { inventory.addItem(item, 1); }
    //
    // void — метод ничего не возвращает. Успешное завершение = предмет добавлен.
    // Ошибка = исключение (а не return false как раньше).

    /**
     * Добавляет предмет в инвентарь.
     *
     * @param item     предмет для добавления (тип T)
     * @param quantity количество предметов
     * @throws InventoryFullException если инвентарь полон (checked exception)
     */
    public void addItem(T item, int quantity) throws InventoryFullException {
        // Проверка: есть ли свободное место в инвентаре.
        if (size >= capacity) {
            // throw new InventoryFullException(...) — БРОСАЕМ исключение вместо return false.
            // throw — оператор, который прерывает выполнение метода и передаёт управление
            // ближайшему catch-блоку в стеке вызовов.
            // new — создаём объект исключения (исключение — обычный объект Java).
            throw new InventoryFullException(size, capacity);
        }
        // Если место есть — создаём новый Slot и помещаем в массив.
        // new Slot(item, quantity) — создание inner class НЕ требует синтаксиса outer.new Slot()
        // внутри самого внешнего класса (Inventory). Контекст this уже известен.
        slots[size] = new Slot(item, quantity);
        size++;
        // Нет return true — успешное завершение метода = предмет добавлен.
    }

    // ===== @SuppressWarnings("unchecked") — ПОДАВЛЕНИЕ ПРЕДУПРЕЖДЕНИЯ =====
    //
    // @SuppressWarnings — аннотация, говорящая компилятору: «не показывай это предупреждение».
    // "unchecked" — предупреждение о НЕПРОВЕРЕННОМ ПРИВЕДЕНИИ ТИПА (unchecked cast).
    //
    // Почему возникает предупреждение?
    // Массив slots имеет тип Object[]. При извлечении мы приводим элемент к Slot:
    //   (Slot) slots[index]
    // Компилятор не может проверить, что в Object[] лежит именно Slot — type erasure
    // стирает информацию о типе при выполнении. Поэтому компилятор предупреждает:
    //   «Unchecked cast: Object → Slot — я не могу гарантировать безопасность».
    //
    // Мы ЗНАЕМ, что в slots лежат ТОЛЬКО Slot-объекты (addItem() кладёт только Slot).
    // Поэтому подавление предупреждения безопасно.
    //
    // Важно: @SuppressWarnings ставь на МИНИМАЛЬНЫЙ возможный элемент (метод, а не класс).
    // Подавление на весь класс скроет ВСЕ предупреждения — можно пропустить настоящую ошибку.

    /**
     * Возвращает ячейку инвентаря по индексу.
     *
     * @param index номер ячейки (0-based, от 0 до size-1)
     * @return ячейка Slot с предметом и количеством
     * @throws IndexOutOfBoundsException если индекс вне допустимого диапазона
     */
    @SuppressWarnings("unchecked")
    public Slot getSlot(int index) {
        if (index < 0 || index >= size) {
            // IndexOutOfBoundsException — unchecked исключение (наследник RuntimeException).
            // Бросается при обращении к элементу за пределами допустимого диапазона.
            // Unchecked — потому что это ошибка программиста (неверный индекс).
            throw new IndexOutOfBoundsException("Индекс " + index + " вне диапазона [0, " + size + ")");
        }
        // (Slot) — приведение типа (cast): Object → Slot.
        // Безопасно, потому что addItem() кладёт в slots только объекты Slot.
        return (Slot) slots[index];
    }

    /**
     * Возвращает текущее количество занятых ячеек.
     *
     * @return количество предметов в инвентаре (0..capacity)
     */
    public int getSize() {
        return size;
    }

    /**
     * Возвращает максимальную ёмкость инвентаря.
     *
     * @return максимальное количество ячеек
     */
    public int getCapacity() {
        return capacity;
    }

    // ===== УДАЛЕНИЕ СЛОТА: ОСВОБОЖДЕНИЕ ЁМКОСТИ (управление памятью) =====
    //
    // Зачем нужен removeSlot()?
    // Когда количество предметов в слоте падает до 0 (slot.isEmpty() == true),
    // слот остаётся в массиве и занимает место. Ёмкость инвентаря "утекает":
    //   - getSize() показывает занятые ячейки, хотя предметов в них нет.
    //   - addItem() отказывается добавлять новые предметы — считает, что места нет.
    //
    // Решение: удалить пустой слот из массива, сдвинув оставшиеся элементы влево.
    // Это та же логика, что в ArrayList.remove() — сдвиг элементов через System.arraycopy.
    //
    // System.arraycopy(src, srcPos, dest, destPos, length) — нативный метод,
    // копирующий элементы массива. Работает быстрее цикла, потому что JVM
    // использует оптимизированное копирование памяти (memcpy/memmove).
    //
    // Параметры:
    //   src     — исходный массив (откуда копируем)
    //   srcPos  — начальная позиция в исходном массиве
    //   dest    — целевой массив (куда копируем, может быть тот же массив!)
    //   destPos — начальная позиция в целевом массиве
    //   length  — сколько элементов копировать
    //
    // Пример: slots = [A, B, C, D, null], size = 4, удаляем index = 1 (B):
    //   System.arraycopy(slots, 2, slots, 1, 2) → сдвигает C и D на одну позицию влево
    //   Результат: slots = [A, C, D, D, null], затем slots[3] = null, size = 3
    //   Итого:     slots = [A, C, D, null, null]
    //
    // slots[size] = null — обнуляем ссылку на удалённый объект.
    // Это важно для сборщика мусора (Garbage Collector, GC):
    //   Если не обнулить, массив будет хранить ссылку на объект,
    //   и GC не сможет его удалить — это называется "утечка памяти" (memory leak).
    //   В Java утечки бывают не из-за забытого free(), а из-за лишних ссылок.
    //
    /**
     * Удаляет слот по индексу, сдвигая оставшиеся ячейки влево.
     * Используется когда количество предметов в слоте упало до 0 (slot.isEmpty() == true),
     * чтобы освободить место и не блокировать добавление новых предметов.
     *
     * @param index индекс удаляемого слота (0-based, от 0 до size-1)
     * @throws IndexOutOfBoundsException если индекс вне допустимого диапазона
     */
    public void removeSlot(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Индекс " + index + " вне диапазона [0, " + size + ")");
        }
        // Количество элементов, которые нужно сдвинуть влево.
        // Если удаляем последний элемент (index == size - 1), numMoved == 0 — сдвигать нечего.
        int numMoved = size - 1 - index;
        if (numMoved > 0) {
            // Сдвигаем все элементы после удаляемого на одну позицию влево.
            // arraycopy корректно обрабатывает перекрывающиеся области (src == dest).
            System.arraycopy(slots, index + 1, slots, index, numMoved);
        }
        // Уменьшаем размер и обнуляем «хвост» — предотвращаем утечку памяти.
        size--;
        slots[size] = null;
    }

    /**
     * Выводит содержимое инвентаря в консоль.
     * Каждая ячейка отображается с порядковым номером (1-based для удобства пользователя).
     */
    public void display() {
        System.out.println("--- Инвентарь (" + size + "/" + capacity + ") ---");
        // for с индексом — нужен номер для отображения.
        // i + 1 — пользователям привычнее нумерация с 1, а не с 0.
        for (int i = 0; i < size; i++) {
            System.out.println("  " + (i + 1) + ". " + getSlot(i));
        }
        System.out.println("-----------------------------------");
    }

    // ===== WILDCARD-МЕТОД — Inventory<?> (глава 3.32) =====
    //
    // <?> — подстановочный знак (wildcard): «любой тип».
    // Inventory<?> означает «инвентарь с НЕИЗВЕСТНЫМ параметром типа».
    //
    // Зачем?
    // Без wildcard нельзя написать метод для инвентаря ЛЮБОГО типа:
    //   displayAnyInventory(Inventory<String> inv) — только для строк.
    //   displayAnyInventory(Inventory<ItemType> inv) — только для ItemType.
    // С wildcard: displayAnyInventory(Inventory<?> inv) — принимает ВСЁ.
    //
    // ВАЖНО: Inventory<String> НЕ является подтипом Inventory<Object>!
    // Это отличие от массивов: String[] является подтипом Object[].
    // Дженерики ИНВАРИАНТНЫ: List<String> ≠ List<Object>.
    // Wildcard решает эту проблему: List<?> принимает List<String>, List<Integer> и т.д.
    //
    // Ограничение wildcard: из Inventory<?> НЕЛЬЗЯ извлечь элемент с конкретным типом
    // (только Object). Нельзя и добавить элемент (компилятор не знает тип).
    // Здесь это не проблема — мы только ЧИТАЕМ (getSlot, getSize).
    //
    // Виды wildcards:
    //   <?> — неограниченный: любой тип.
    //   <? extends Number> — верхняя граница: Number или его наследники (для ЧТЕНИЯ).
    //   <? super Integer> — нижняя граница: Integer или его предки (для ЗАПИСИ).
    //   Мнемоника PECS: Producer Extends, Consumer Super.
    //
    // static — метод принадлежит классу, а не экземпляру. Вызов: Inventory.displayAnyInventory(inv).

    /**
     * Выводит содержимое ЛЮБОГО инвентаря (независимо от параметра типа T).
     * Статический метод с wildcard — принимает Inventory<String>, Inventory<ItemType> и т.д.
     *
     * @param inv инвентарь любого типа
     */
    public static void displayAnyInventory(Inventory<?> inv) {
        System.out.println("=== Инвентарь (размер: " + inv.getSize() + "/" + inv.getCapacity() + ") ===");
        for (int i = 0; i < inv.getSize(); i++) {
            System.out.println("  " + (i + 1) + ". " + inv.getSlot(i));
        }
    }

    // ===== РЕАЛИЗАЦИЯ Iterable — МЕТОД iterator() (глава 5.10) =====
    //
    // iterator() — единственный абстрактный метод интерфейса Iterable<T>.
    // Должен вернуть НОВЫЙ объект Iterator<Slot>, который умеет обходить элементы.
    //
    // Каждый вызов iterator() создаёт НОВЫЙ итератор с позицией 0.
    // Это позволяет обходить один и тот же инвентарь несколько раз.
    //
    // После реализации iterator() можно использовать for-each:
    //   for (var slot : inventory) {
    //       System.out.println(slot.getItem());
    //   }
    //
    // ===== ConcurrentModificationException — ВАЖНАЯ КОНЦЕПЦИЯ =====
    //
    // Если коллекцию изменить ВО ВРЕМЯ итерации (добавить/удалить элемент),
    // стандартные итераторы Java бросают ConcurrentModificationException.
    // Это называется «fail-fast» поведение.
    //
    // Пример опасного кода:
    //   for (var slot : inventory) {
    //       inventory.addItem(...);  // ConcurrentModificationException!
    //   }
    //
    // Наш итератор НЕ реализует fail-fast (для простоты), но в стандартных коллекциях
    // (ArrayList, HashMap) это поведение встроено.
    // Решение: не изменять коллекцию внутри for-each. Если нужно — используй Iterator.remove()
    // или собирай изменения и применяй их после цикла.

    /**
     * Возвращает итератор для обхода ячеек инвентаря.
     * Позволяет использовать этот инвентарь в цикле for-each.
     *
     * @return новый итератор, начинающий с первой ячейки
     */
    @Override
    public Iterator<Slot> iterator() {
        // new InventoryIterator() — создаём экземпляр кастомного итератора.
        // InventoryIterator — inner class (нестатический), поэтому имеет доступ
        // к полям внешнего Inventory (slots, size) через неявную ссылку Inventory.this.
        return new InventoryIterator();
    }

    // ===== КАСТОМНЫЙ ИТЕРАТОР — InventoryIterator (глава 5.10) =====
    //
    // InventoryIterator implements Iterator<Slot> — реализует стандартный интерфейс Iterator.
    //
    // Зачем создавать свой итератор?
    //   1. Инкапсуляция: скрываем внутреннюю структуру (Object[]) от внешнего кода.
    //      Внешний код работает с Iterator<Slot>, не зная про Object[] и приведение типов.
    //   2. Безопасность: итератор контролирует границы (не выйдет за size).
    //   3. Стандартный API: любой код, работающий с Iterator/Iterable, работает с нашим инвентарём.
    //      Например: StreamSupport.stream(inventory.spliterator(), false) — создание Stream.
    //
    // private — итератор доступен только внутри Inventory.
    // Внешний код получает его через iterator() и работает через интерфейс Iterator<Slot>.
    // Это инкапсуляция: реализация скрыта, виден только контракт (Iterator<Slot>).
    //
    // Inner class (нестатический) — имеет доступ к полям внешнего Inventory:
    //   slots — массив ячеек (для извлечения элементов).
    //   size — количество занятых ячеек (для проверки границ).
    // Доступ осуществляется через неявную ссылку Inventory.this.
    private class InventoryIterator implements Iterator<Slot> {

        // Текущая позиция итератора. Начинаем с 0 (первый элемент).
        // Каждый вызов next() увеличивает currentIndex на 1.
        // Когда currentIndex == size — элементы закончились.
        private int currentIndex = 0;

        // ===== hasNext() — ПРОВЕРКА НАЛИЧИЯ СЛЕДУЮЩЕГО ЭЛЕМЕНТА =====
        //
        // Возвращает true, пока не дошли до конца (currentIndex < size).
        // Не изменяет состояние итератора (можно вызывать многократно без побочных эффектов).
        //
        // size — поле ВНЕШНЕГО класса Inventory.
        // Inner class имеет прямой доступ к полям внешнего класса
        // (как если бы они были его собственными полями).
        // Под капотом: компилятор обращается через Inventory.this.size.

        /**
         * Проверяет, есть ли ещё элементы для обхода.
         *
         * @return true, если ещё есть необработанные ячейки
         */
        @Override
        public boolean hasNext() {
            return currentIndex < size;
        }

        // ===== next() — ВОЗВРАТ ТЕКУЩЕГО ЭЛЕМЕНТА И ПЕРЕХОД К СЛЕДУЮЩЕМУ =====
        //
        // Контракт метода next():
        //   1. Если hasNext() == true → возвращает текущий элемент, перемещает указатель вперёд.
        //   2. Если hasNext() == false → ОБЯЗАН бросить NoSuchElementException.
        //
        // Вызов next() без предварительной проверки hasNext() — частая ошибка!
        // Правильный паттерн:
        //   while (it.hasNext()) {
        //       var slot = it.next();
        //   }

        /**
         * Возвращает текущую ячейку и перемещает указатель к следующей.
         *
         * @return текущая ячейка Slot
         * @throws NoSuchElementException если элементов больше нет
         */
        // @SuppressWarnings("unchecked") — подавляем предупреждение о приведении Object → Slot.
        // (см. подробное объяснение @SuppressWarnings в методе getSlot() выше)
        @SuppressWarnings("unchecked")
        @Override
        public Slot next() {
            // Защита: если вызвали next() когда элементов нет — бросаем исключение.
            // NoSuchElementException — стандартное unchecked исключение для итераторов.
            if (!hasNext()) {
                throw new NoSuchElementException("Нет больше элементов в инвентаре");
            }
            // slots[currentIndex] — текущий элемент (тип Object в массиве).
            // (Slot) — приведение типа к Slot (мы знаем, что в slots лежат только Slot-объекты).
            //
            // currentIndex++ — ПОСТФИКСНЫЙ ИНКРЕМЕНТ:
            //   Сначала ИСПОЛЬЗУЕТСЯ текущее значение currentIndex (для обращения к slots[]),
            //   затем значение УВЕЛИЧИВАЕТСЯ на 1.
            //   Эквивалент в две строки:
            //     Slot result = (Slot) slots[currentIndex];
            //     currentIndex = currentIndex + 1;
            //     return result;
            //
            // Отличие от ПРЕФИКСНОГО инкремента (++currentIndex):
            //   ++currentIndex — сначала увеличивает, потом использует (мы бы пропустили первый элемент!).
            return (Slot) slots[currentIndex++];
        }

        // ===== remove() — НЕОБЯЗАТЕЛЬНЫЙ МЕТОД ИТЕРАТОРА =====
        //
        // В интерфейсе Iterator метод remove() объявлен как default (Java 8+):
        //   default void remove() { throw new UnsupportedOperationException("remove"); }
        //
        // default-метод — метод с РЕАЛИЗАЦИЕЙ в интерфейсе.
        // Класс может:
        //   1. Не переопределять — будет использована default-реализация.
        //   2. Переопределить с собственной логикой.
        //
        // Мы явно переопределяем, чтобы дать понятное русскоязычное сообщение об ошибке.
        // UnsupportedOperationException — unchecked исключение, означающее:
        //   «Эта операция не поддерживается данной реализацией».
        //
        // Когда remove() нужен?
        //   Когда нужно безопасно удалять элементы из коллекции ВО ВРЕМЯ итерации:
        //     Iterator<String> it = list.iterator();
        //     while (it.hasNext()) {
        //         if (it.next().isEmpty()) {
        //             it.remove();  // безопасное удаление (в отличие от list.remove() в for-each)
        //         }
        //     }

        /**
         * Удаление через итератор не поддерживается для этого инвентаря.
         *
         * @throws UnsupportedOperationException всегда
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Удаление через итератор не поддерживается");
        }
    }
}
