// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== InventoryFullException — ПЕРЕПОЛНЕНИЕ ИНВЕНТАРЯ (глава 4.3) =====
//
// InventoryFullException — бросается при попытке добавить предмет в полный инвентарь.
// Наследует GameException (checked) — вызывающий код ОБЯЗАН обработать.
//
// Это ТРЕТИЙ уровень иерархии (подробнее — в GameException.java):
//   GameException (checked)
//   ├── InsufficientResourceException  ← не хватает ресурса
//   ├── InvalidActionException         ← некорректное действие
//   └── InventoryFullException         ← ← МЫ ЗДЕСЬ: инвентарь полон
//
// ===== ИСКЛЮЧЕНИЕ vs BOOLEAN: КОГДА ЧТО ИСПОЛЬЗОВАТЬ? =====
//
// Раньше метод addItem() мог возвращать boolean (true/false — успех/неуспех).
// Теперь он бросает InventoryFullException — это ЛУЧШИЙ подход, потому что:
//
//   1. НЕВОЗМОЖНО ПРОИГНОРИРОВАТЬ. Checked exception заставляет написать try-catch.
//      С boolean: boolean ok = inventory.addItem(item); ← можно забыть проверить ok!
//      С exception: inventory.addItem(item); ← если не обработать — ошибка компиляции.
//
//   2. НЕСЁТ ДАННЫЕ. currentSize и maxCapacity доступны через геттеры.
//      С boolean: false — и всё. Почему false? Сколько места? Непонятно.
//      С exception: e.getCurrentSize()=5, e.getMaxCapacity()=5 — полная картина.
//
//   3. ЧИСТЫЙ КОД. Нет if/else для проверки результата.
//      С boolean: if (!inventory.addItem(item)) { /* обработка */ }
//      С exception: try { inventory.addItem(item); } catch (InventoryFullException e) { ... }
//
//   4. МНОЖЕСТВЕННЫЙ ВЫЗОВ. В цепочке операций exception прерывает цепочку сразу.
//      С boolean нужно проверять каждый вызов вручную.
//
// Когда ВСЁ ЖЕ использовать boolean?
//   - Когда «неуспех» — это НОРМАЛЬНАЯ ситуация, а не ошибка.
//   - Пример: list.contains(element) → true/false. Отсутствие элемента — не ошибка.
//   - Пример: map.containsKey(key) → true/false. Проверка — не ошибка.
//   - Пример: set.add(element) → false если уже есть. Дубликат — не ошибка.
//
// ===== ПРИМЕНЕНИЕ В ИГРЕ (Game.java) =====
//
// Бросание (в Inventory.addItem()):
//   public void addItem(ItemInfo item, int count) throws InventoryFullException {
//       if (items.size() >= maxCapacity) {
//           throw new InventoryFullException(items.size(), maxCapacity);
//       }
//       items.put(item, count);
//   }
//
// Перехват при инициализации (в startGame()):
//   try {
//       inventory.addItem(new ItemInfo("Зелье здоровья", 30), 3);
//   } catch (InventoryFullException e) {
//       System.out.println("Предупреждение: " + e.getMessage());
//   }
//
// Перехват при сборе лута (в combat()):
//   try {
//       inventory.addItem(lootItem, lootCount);
//       System.out.println("Добавлено в инвентарь!");
//   } catch (InventoryFullException e) {
//       // Предмет утерян — но игра продолжается. Это НЕ фатальная ошибка.
//       System.out.println("Инвентарь полон, предмет утерян.");
//   }
//
// Обрати внимание: catch НЕ завершает программу — игра продолжается.
// Checked exception гарантирует, что разработчик ЗАДУМАЛСЯ об этом сценарии.

// extends GameException — наследование пользовательских исключений (см. GameException.java).
// Можно ловить как InventoryFullException (точно), так и GameException (обобщённо).
public class InventoryFullException extends GameException {

    // ===== ПОЛЯ ИСКЛЮЧЕНИЯ =====
    //
    // Два числовых поля: текущий размер и максимальная вместимость.
    // Позволяют обработчику точно знать состояние инвентаря на момент ошибки.
    //
    // private final — неизменяемые поля (см. подробное объяснение в InsufficientResourceException.java).
    // int, а не Integer — примитивный тип: не может быть null, занимает меньше памяти.

    // Текущее количество предметов в инвентаре на момент попытки добавления.
    private final int currentSize;

    // Максимальная вместимость инвентаря (сколько слотов всего).
    private final int maxCapacity;

    /**
     * Создаёт исключение о переполнении инвентаря.
     *
     * Сообщение формируется автоматически: "Инвентарь полон: X/Y слотов занято".
     * Формат "X/Y" — привычный для игроков (например, "5/5 слотов занято").
     *
     * Пример: throw new InventoryFullException(5, 5);
     * getMessage(): "Инвентарь полон: 5/5 слотов занято"
     *
     * @param currentSize текущее количество предметов
     * @param maxCapacity максимальная вместимость
     */
    public InventoryFullException(int currentSize, int maxCapacity) {
        // super(message) — вызов конструктора GameException(String).
        // Цепочка: InventoryFullException → GameException → Exception → Throwable.
        // Каждый уровень передаёт message родителю через super().
        super("Инвентарь полон: " + currentSize + "/" + maxCapacity + " слотов занято");
        this.currentSize = currentSize;
        this.maxCapacity = maxCapacity;
    }

    /**
     * Конструктор с причиной (для цепочки исключений / exception chaining).
     *
     * Используется, когда переполнение инвентаря ВЫЗВАНО другим исключением.
     * В текущей версии игры этот конструктор не используется, но предоставлен
     * по конвенции (см. раздел «Конвенция: набор конструкторов» в GameException.java).
     *
     * Пример потенциального использования — при загрузке сохранения:
     *   try {
     *       loadSavedItems(inventory);
     *   } catch (IOException e) {
     *       throw new InventoryFullException(currentSize, maxCapacity, e);
     *       // Сохраняем причину: IOException → InventoryFullException
     *   }
     *
     * @param currentSize текущее количество предметов
     * @param maxCapacity максимальная вместимость
     * @param cause       исходное исключение (доступно через getCause())
     */
    public InventoryFullException(int currentSize, int maxCapacity, Throwable cause) {
        // super(message, cause) — вызов GameException(String, Throwable).
        // cause сохраняется и доступна через getCause().
        super("Инвентарь полон: " + currentSize + "/" + maxCapacity + " слотов занято", cause);
        this.currentSize = currentSize;
        this.maxCapacity = maxCapacity;
    }

    // ===== ГЕТТЕРЫ =====
    //
    // Геттеры (см. подробное объяснение в InsufficientResourceException.java).
    // Дают программный доступ к данным без парсинга строки getMessage().
    //
    // Пример: в будущем можно показать игроку подсказку:
    //   catch (InventoryFullException e) {
    //       System.out.printf("Нет места! Занято %d из %d слотов.%n",
    //           e.getCurrentSize(), e.getMaxCapacity());
    //       System.out.println("Выбросите что-нибудь, чтобы освободить место.");
    //   }

    /**
     * Возвращает текущее количество предметов в инвентаре.
     *
     * @return количество предметов на момент ошибки
     */
    public int getCurrentSize() {
        return currentSize;
    }

    /**
     * Возвращает максимальную вместимость инвентаря.
     *
     * @return максимальное количество слотов
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    // ===== ИТОГО: ВСЯ ИЕРАРХИЯ ИСКЛЮЧЕНИЙ RPG =====
    //
    // GameException (checked, extends Exception)
    // │  Поля: message, cause (от Exception)
    // │  Конструкторы: (message), (message, cause), (cause)
    // │
    // ├── InsufficientResourceException
    // │   Поля: resourceName, required, available
    // │   Когда: мало маны, ярости, стрел для действия
    // │
    // ├── InvalidActionException
    // │   Поля: actionName, reason
    // │   Когда: пустое имя, атака мёртвого, невалидный ввод
    // │
    // └── InventoryFullException
    //     Поля: currentSize, maxCapacity
    //     Когда: попытка добавить предмет в полный инвентарь
    //
    // ===== ПРАКТИЧЕСКИЕ СОВЕТЫ ПО ИСКЛЮЧЕНИЯМ =====
    //
    // 1. Ловите КОНКРЕТНЫЕ исключения, не общие:
    //      ХОРОШО: catch (InventoryFullException e)
    //      ПЛОХО:  catch (Exception e)  ← ловит ВСЁ, включая NullPointerException!
    //
    // 2. НЕ используйте исключения для управления потоком программы:
    //      ПЛОХО:  try { inventory.addItem(item); } catch (...) { /* нормальная логика */ }
    //      ХОРОШО: if (inventory.isFull()) { /* альтернативная логика */ } else { addItem(); }
    //      Исключения ДОРОГИЕ — создание стектрейса занимает время.
    //
    // 3. Всегда добавляйте cause при оборачивании исключений:
    //      ХОРОШО: throw new GameException("Ошибка", originalException);
    //      ПЛОХО:  throw new GameException("Ошибка");  ← потеряли информацию об оригинальной ошибке!
    //
    // 4. Сообщение (message) должно быть ИНФОРМАТИВНЫМ:
    //      ХОРОШО: "Инвентарь полон: 5/5 слотов занято"
    //      ПЛОХО:  "Ошибка" или "error" или "Something went wrong"
    //
    // 5. Поля исключений делайте final — данные об ошибке не должны меняться.
}
