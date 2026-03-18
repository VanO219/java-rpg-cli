// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== InvalidActionException — НЕКОРРЕКТНОЕ ДЕЙСТВИЕ (глава 4.3) =====
//
// InvalidActionException — бросается при попытке выполнить некорректное действие.
// Наследует GameException (checked) — вызывающий код ОБЯЗАН обработать.
//
// Это ВТОРОЙ уровень иерархии (подробнее — в GameException.java):
//   GameException (checked)
//   ├── InsufficientResourceException  ← не хватает ресурса
//   ├── InvalidActionException         ← ← МЫ ЗДЕСЬ: некорректное действие
//   └── InventoryFullException         ← инвентарь полон
//
// Примеры использования в нашей игре:
//   - Пустое имя героя при создании персонажа.
//   - Слишком длинное имя (больше 20 символов).
//   - Имя содержит недопустимые символы (цифры, спецсимволы).
//   - Попытка атаковать мёртвого врага.
//
// ===== ЧЕМ ОТЛИЧАЕТСЯ ОТ IllegalArgumentException? =====
//
// IllegalArgumentException — стандартное UNCHECKED исключение (extends RuntimeException).
// Компилятор НЕ заставит его обработать — если забыть catch, программа упадёт.
//
// InvalidActionException — наше CHECKED исключение (extends GameException extends Exception).
// Компилятор ЗАСТАВИТ обработать — метод readHeroName() объявляет throws InvalidActionException,
// и вызывающий startGame() ОБЯЗАН написать try-catch.
//
// Мы выбрали checked, потому что невалидный ввод — ОЖИДАЕМАЯ ситуация в игре:
// пользователь МОЖЕТ ввести пустое имя, и код ОБЯЗАН это обработать (попросить ввести заново).
//
// ===== ПРИМЕНЕНИЕ В ИГРЕ (Game.java) =====
//
// Бросание (в readHeroName()):
//   private String readHeroName() throws InvalidActionException {
//       if (name.isEmpty()) {
//           throw new InvalidActionException("ввод имени", "имя не может быть пустым");
//       }
//       if (name.length() > 20) {
//           throw new InvalidActionException("ввод имени", "имя слишком длинное");
//       }
//   }
//
// Перехват (в startGame()):
//   while (name == null) {
//       try {
//           name = readHeroName();
//       } catch (InvalidActionException e) {
//           System.out.println("Ошибка: " + e.getMessage());
//           // Цикл продолжается — пользователь вводит имя заново.
//       }
//   }
//
// Обрати внимание на ПАТТЕРН: throw в одном методе → catch + retry в другом.
// Метод readHeroName() НЕ ЗНАЕТ, что делать с ошибкой (это не его ответственность).
// Метод startGame() ЗНАЕТ — попросить пользователя ввести имя ещё раз.
// Это РАЗДЕЛЕНИЕ ОТВЕТСТВЕННОСТИ: валидация отдельно, обработка ошибки отдельно.

// extends GameException — наследование пользовательских исключений (см. GameException.java).
// Можно ловить как InvalidActionException (точно), так и GameException (обобщённо).
public class InvalidActionException extends GameException {

    // ===== ПОЛЯ ИСКЛЮЧЕНИЯ =====
    //
    // Два поля хранят КОНТЕКСТ ошибки: ЧТО пытались сделать и ПОЧЕМУ не получилось.
    // Это позволяет обработчику принимать решения на основе данных, а не парсить текст.

    // Название действия, которое не удалось выполнить.
    // Примеры: "ввод имени", "атака", "использование предмета", "смена оружия".
    // private final — неизменяемое, доступно только через геттер getActionName().
    private final String actionName;

    // Причина, почему действие невалидно.
    // Примеры: "имя пустое", "враг уже мёртв", "предмет нельзя использовать в бою".
    // private final — неизменяемое, доступно только через геттер getReason().
    private final String reason;

    /**
     * Создаёт исключение о невалидном действии.
     *
     * Сообщение формируется автоматически из actionName и reason.
     * Формат: "Невалидное действие '<действие>': <причина>"
     *
     * Пример: throw new InvalidActionException("ввод имени", "имя не может быть пустым");
     * getMessage(): "Невалидное действие 'ввод имени': имя не может быть пустым"
     *
     * @param actionName название действия
     * @param reason     причина ошибки
     */
    public InvalidActionException(String actionName, String reason) {
        // super(message) — вызов конструктора GameException(String).
        // GameException(String) → Exception(String) → Throwable(String).
        // Цепочка конструкторов: каждый уровень вызывает super() родителя.
        super("Невалидное действие '" + actionName + "': " + reason);

        // Сохраняем параметры в final-поля для программного доступа через геттеры.
        this.actionName = actionName;
        this.reason = reason;
    }

    /**
     * Конструктор с причиной (для цепочки исключений / exception chaining).
     *
     * Используется, когда невалидное действие ВЫЗВАНО другим исключением.
     * Пример: NumberFormatException при парсинге числового ввода пользователя
     * оборачивается в InvalidActionException:
     *
     *   try {
     *       int amount = Integer.parseInt(input);
     *   } catch (NumberFormatException e) {
     *       throw new InvalidActionException("ввод количества", "ожидается число", e);
     *       // e сохраняется как cause — getCause() вернёт NumberFormatException
     *   }
     *
     * @param actionName название действия
     * @param reason     причина ошибки
     * @param cause      исходное исключение (доступно через getCause())
     */
    public InvalidActionException(String actionName, String reason, Throwable cause) {
        // super(message, cause) — вызов GameException(String, Throwable).
        // Цепочка: GameException → Exception → Throwable сохраняет и message, и cause.
        super("Невалидное действие '" + actionName + "': " + reason, cause);
        this.actionName = actionName;
        this.reason = reason;
    }

    // ===== ГЕТТЕРЫ =====
    //
    // Геттеры (см. подробное объяснение в InsufficientResourceException.java).
    // Позволяют обработчику получить данные об ошибке БЕЗ парсинга строки getMessage().
    //
    // Пример: в обработчике можно принять решение на основе actionName:
    //   catch (InvalidActionException e) {
    //       if ("ввод имени".equals(e.getActionName())) {
    //           showNameInputHelp();  // Показать подсказку по формату имени
    //       }
    //   }

    /**
     * Возвращает название действия, которое не удалось выполнить.
     *
     * @return название действия ("ввод имени", "атака", "использование предмета")
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Возвращает причину, почему действие невалидно.
     *
     * @return текстовое описание причины
     */
    public String getReason() {
        return reason;
    }
}
