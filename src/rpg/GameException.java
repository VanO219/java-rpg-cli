// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== ПОЛЬЗОВАТЕЛЬСКИЕ ИСКЛЮЧЕНИЯ (Custom Exceptions, глава 4.3) =====
//
// Исключение (Exception) — объект, описывающий ошибочную ситуацию.
// Когда метод не может выполнить свою работу, он БРОСАЕТ (throw) исключение.
// Вызывающий код ЛОВИТ (catch) исключение и решает, что делать с ошибкой.
//
// В Java есть два вида исключений:
//
//   1. Checked (проверяемые) — наследники Exception (но НЕ RuntimeException).
//      Компилятор ЗАСТАВЛЯЕТ их обработать: либо try-catch, либо throws в сигнатуре.
//      Примеры: IOException, SQLException, наш GameException.
//      Используются для ОЖИДАЕМЫХ ошибок: файл не найден, сеть недоступна, инвентарь полон.
//
//   2. Unchecked (непроверяемые) — наследники RuntimeException.
//      Компилятор НЕ требует обработки. Обычно это ошибки в коде (баги).
//      Примеры: NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException.
//      Используются для НЕОЖИДАЕМЫХ ошибок: обращение к null, выход за границы массива.
//
// ===== ИЕРАРХИЯ ИСКЛЮЧЕНИЙ В JAVA =====
//
//   Throwable                  ← корень всех «бросаемых» объектов
//   ├── Error                  ← фатальные ошибки JVM (OutOfMemoryError, StackOverflowError)
//   │                            НЕ нужно ловить — программа не может продолжить работу.
//   └── Exception              ← ошибки, с которыми можно справиться
//       ├── RuntimeException   ← unchecked-исключения (баги в коде)
//       │   ├── NullPointerException
//       │   ├── NumberFormatException
//       │   └── IllegalArgumentException
//       └── (все остальные)    ← checked-исключения (ожидаемые ошибки)
//           ├── IOException
//           ├── SQLException
//           └── GameException  ← НАШЕ пользовательское checked-исключение
//
// ===== ИЕРАРХИЯ НАШИХ ИГРОВЫХ ИСКЛЮЧЕНИЙ =====
//
//   GameException                        ← базовый класс для ВСЕХ игровых ошибок
//   ├── InsufficientResourceException    ← не хватает ресурса (мана, ярость, стрелы)
//   ├── InvalidActionException           ← некорректное действие (пустое имя, атака мёртвого)
//   └── InventoryFullException           ← инвентарь переполнен
//
// Зачем базовый класс GameException?
//   - catch (GameException e) ловит ВСЕ три вида ошибок разом (полиморфизм).
//   - catch (InventoryFullException e) ловит ТОЛЬКО переполнение инвентаря.
//   - Можно добавлять новые исключения (WeaponBrokenException и т.д.), не меняя catch-блоки.
//
// ===== ЗАЧЕМ СВОИ ИСКЛЮЧЕНИЯ? =====
//
// Стандартные исключения (IOException, IllegalArgumentException) слишком общие.
// Пользовательские исключения позволяют:
//   1. Точно описать тип ошибки: InventoryFullException вместо RuntimeException.
//   2. Нести дополнительные данные: какой ресурс, сколько не хватает, какой слот.
//   3. Ловить конкретные ошибки игры, не перехватывая все подряд.
//   4. Строить ИЕРАРХИЮ: GameException → InsufficientResourceException,
//      InvalidActionException, InventoryFullException.
//
// ===== CHECKED vs UNCHECKED — КАК ВЫБРАТЬ? =====
//
// Мы выбрали extends Exception (checked), а НЕ extends RuntimeException (unchecked).
// Почему?
//   - Все наши исключения описывают ОЖИДАЕМЫЕ игровые ситуации:
//     мало маны, инвентарь полон, невалидное имя — это НЕ баги, это игровая логика.
//   - Checked exception заставляет вызывающий код ЯВНО обработать ошибку.
//     Программист не сможет «забыть» обработать переполнение инвентаря.
//   - Если бы мы выбрали RuntimeException, компилятор НЕ предупреждал бы об ошибке.
//     Исключение могло бы «проскочить» вверх по стеку и завершить программу.
//
// Когда использовать unchecked (RuntimeException)?
//   - Ошибки в коде, которые НЕЛЬЗЯ предвидеть: null-ссылки, выход за массив.
//   - Ситуации, когда КАЖДЫЙ вызывающий код обрабатывать их не должен.
//
// ===== КОНВЕНЦИЯ: НАБОР КОНСТРУКТОРОВ =====
//
// По конвенции Java, у каждого пользовательского исключения создают ДО 4 конструкторов:
//   1. (String message)                      — только текст ошибки
//   2. (String message, Throwable cause)     — текст + причина (для цепочки)
//   3. (Throwable cause)                     — только причина
//   4. ()                                    — без параметров (редко полезен)
//
// Наш GameException предоставляет первые три — это достаточно для большинства случаев.
// Дочерние классы (InsufficientResourceException, InventoryFullException) добавляют
// свои конструкторы с игровыми параметрами (resourceName, required, currentSize и т.д.).

// GameException — базовый класс для ВСЕХ игровых исключений.
//
// class — обычный класс (не record, не interface, не enum), потому что:
//   1. Исключения ДОЛЖНЫ быть классами (extends Exception).
//   2. Record не может наследовать другой класс (record неявно наследует java.lang.Record).
//   3. Нам нужна изменяемая иерархия наследования (InsufficientResourceException extends GameException).
//
// extends Exception — наследуем от Exception, делая его CHECKED.
// Это значит: метод, бросающий GameException, ОБЯЗАН объявить throws GameException,
// а вызывающий код ОБЯЗАН обработать (try-catch или пробросить дальше).
//
// Если бы мы написали extends RuntimeException — получили бы unchecked-исключение:
// компилятор НЕ требовал бы throws/try-catch, и ошибку можно было бы пропустить.
public class GameException extends Exception {

    // ===== КОНСТРУКТОРЫ ИСКЛЮЧЕНИЙ =====
    //
    // У исключений принято создавать несколько конструкторов для разных случаев.
    // Все они вызывают конструктор родителя (super) — класса Exception.
    //
    // Класс Exception (наш родитель) хранит два ключевых поля:
    //   - message (String) — текстовое описание ошибки, доступное через getMessage()
    //   - cause (Throwable) — причина ошибки, доступная через getCause()
    //
    // getMessage() — возвращает строку, переданную в конструктор.
    //   Пример: new GameException("Ошибка!").getMessage() → "Ошибка!"
    //
    // getCause() — возвращает «оригинальное» исключение, если одно было обёрнуто в другое.
    //   Пример: new GameException("Обёртка", ioException).getCause() → ioException
    //   Если причины нет — возвращает null.
    //
    // «Цепочка исключений» (exception chaining): когда одно исключение вызвано другим.
    // Пример: IOException → оборачивается в GameException. Оригинальная причина сохраняется.
    // Зачем это нужно? Чтобы НЕ ПОТЕРЯТЬ информацию о первоначальной ошибке.
    // В стектрейсе (stack trace) будет видно: GameException → caused by: IOException.

    /**
     * Конструктор с сообщением об ошибке.
     * Используется, когда известна только текстовая причина.
     *
     * Пример: throw new GameException("Невозможно выполнить действие");
     *
     * @param message описание ошибки (будет доступно через getMessage())
     */
    public GameException(String message) {
        // super(message) — вызов конструктора родительского класса Exception(String message).
        // Ключевое слово super используется для вызова конструктора РОДИТЕЛЯ.
        // Exception сохраняет сообщение, которое потом доступно через getMessage().
        //
        // Частая ошибка: забыть вызвать super(message). Тогда getMessage() вернёт null!
        // Конструктор родителя ВСЕГДА нужно вызывать для передачи message/cause.
        super(message);
    }

    /**
     * Конструктор с сообщением и причиной.
     * Используется для ОБОРАЧИВАНИЯ одного исключения в другое (exception chaining).
     *
     * Пример:
     *   try {
     *       loadFile();
     *   } catch (IOException e) {
     *       // Оборачиваем IOException в GameException, сохраняя оригинальную причину.
     *       // Так вызывающий код может ловить GameException, но при отладке
     *       // через e.getCause() мы увидим оригинальный IOException.
     *       throw new GameException("Ошибка загрузки данных", e);
     *   }
     *
     * @param message описание ошибки
     * @param cause   исходное исключение, вызвавшее эту ошибку (доступно через getCause())
     */
    public GameException(String message, Throwable cause) {
        // super(message, cause) — вызов Exception(String, Throwable).
        //
        // Throwable — базовый класс для ВСЕХ исключений и ошибок.
        // Параметр типа Throwable, а не Exception, потому что причиной может быть и Error.
        //
        // cause доступна через getCause(): gameException.getCause() → оригинальный IOException.
        // При выводе стектрейса (e.printStackTrace()) будет строка:
        //   "Caused by: java.io.IOException: файл не найден"
        super(message, cause);
    }

    /**
     * Конструктор только с причиной (без дополнительного сообщения).
     * Сообщение формируется автоматически: getMessage() вернёт cause.toString().
     *
     * Используется когда оригинальное исключение уже содержит достаточно информации,
     * и добавлять своё сообщение нет смысла.
     *
     * @param cause исходное исключение
     */
    public GameException(Throwable cause) {
        // super(cause) — вызов Exception(Throwable).
        // Сообщение (message) автоматически формируется как cause.toString().
        // Пример: если cause — IOException("файл не найден"),
        //   то getMessage() вернёт "java.io.IOException: файл не найден".
        super(cause);
    }

    // ===== КАК ЭТИ ИСКЛЮЧЕНИЯ ИСПОЛЬЗУЮТСЯ В ПРОЕКТЕ (Game.java) =====
    //
    // 1. readHeroName() объявляет throws InvalidActionException.
    //    Метод бросает исключение при невалидном имени.
    //    Вызывающий код в startGame() ловит его в цикле while и просит ввести имя заново:
    //
    //      while (name == null) {
    //          try {
    //              name = readHeroName();  // может бросить InvalidActionException
    //          } catch (InvalidActionException e) {
    //              System.out.println("Ошибка: " + e.getMessage());
    //          }
    //      }
    //
    // 2. Inventory.addItem() бросает InventoryFullException при переполнении.
    //    В методе combat() при попытке добавить лут:
    //
    //      try {
    //          inventory.addItem(item, count);
    //      } catch (InventoryFullException e) {
    //          System.out.println("Инвентарь полон, предмет утерян.");
    //      }
    //
    // 3. handleSpecialAttack() бросает и ловит InsufficientResourceException:
    //
    //      try {
    //          if (mage.getMana() < 20) {
    //              throw new InsufficientResourceException("мана", 20, mage.getMana());
    //          }
    //      } catch (InsufficientResourceException e) {
    //          System.out.println("Недостаточно маны: " + e.getMessage());
    //      }
    //
    // ===== ПОРЯДОК CATCH-БЛОКОВ (ВАЖНО!) =====
    //
    // Если нужно ловить несколько исключений, порядок catch-блоков КРИТИЧЕСКИ важен:
    // СНАЧАЛА конкретные → ПОТОМ общие.
    //
    //   try { ... }
    //   catch (InventoryFullException e) { ... }    // ← сначала конкретный
    //   catch (GameException e) { ... }             // ← потом общий (ловит остальные)
    //
    // Если поставить GameException первым — компилятор выдаст ОШИБКУ:
    //   "exception InventoryFullException has already been caught"
    // Потому что GameException уже перехватит все подклассы, до InventoryFullException дело не дойдёт.
    //
    // Аналогия: если у входа стоит охранник, проверяющий ВСЕХ — отдельные проверки бессмысленны.
}
