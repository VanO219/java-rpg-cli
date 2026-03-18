// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== НАСЛЕДОВАНИЕ ИСКЛЮЧЕНИЙ (глава 4.3) =====
//
// InsufficientResourceException extends GameException — иерархия пользовательских исключений.
// Наследование исключений работает ТАК ЖЕ, как наследование обычных классов (extends).
//
// Зачем иерархия?
//   catch (GameException e) — ловит ВСЕ игровые исключения (и InsufficientResource, и InvalidAction).
//   catch (InsufficientResourceException e) — ловит ТОЛЬКО нехватку ресурса.
//
// Это полиморфизм в действии (глава 3.24):
//   - InsufficientResourceException ЯВЛЯЕТСЯ GameException (отношение "is-a").
//   - Переменная типа GameException может хранить ссылку на InsufficientResourceException.
//   - Поэтому catch (GameException e) ловит все дочерние исключения.
//
// ===== КОГДА СОЗДАВАТЬ ОТДЕЛЬНОЕ ИСКЛЮЧЕНИЕ, А КОГДА ДОСТАТОЧНО СТАНДАРТНОГО? =====
//
// Создавай своё исключение, если:
//   1. Ошибка несёт СПЕЦИФИЧЕСКИЕ ДАННЫЕ (resourceName, required, available — как здесь).
//   2. Вызывающий код должен РЕАГИРОВАТЬ ПО-РАЗНОМУ на разные ошибки.
//   3. Стандартные исключения слишком общие: IllegalArgumentException не скажет «не хватает маны».
//
// НЕ создавай своё исключение, если:
//   1. Достаточно стандартного: IllegalArgumentException("возраст не может быть отрицательным").
//   2. Никому не нужно ловить именно этот тип отдельно.
//   3. Нет дополнительных данных — только текстовое сообщение.
//
// ===== ПРИМЕНЕНИЕ В ИГРЕ =====
//
// InsufficientResourceException бросается, когда у персонажа недостаточно ресурса
// для выполнения действия (мало маны для заклинания, мало ярости для спецатаки и т.д.).
//
// Пример из Game.java → handleSpecialAttack():
//   if (mage.getMana() < 20) {
//       throw new InsufficientResourceException("мана", 20, mage.getMana());
//   }
//
// Обработка:
//   catch (InsufficientResourceException e) {
//       System.out.println(e.getMessage());                // "Недостаточно ресурса 'мана': требуется 20, доступно 5"
//       System.out.println("Ресурс: " + e.getResourceName()); // "мана"
//       System.out.println("Нужно: " + e.getRequired());      // 20
//       System.out.println("Есть: " + e.getAvailable());      // 5
//   }
public class InsufficientResourceException extends GameException {

    // ===== ДОПОЛНИТЕЛЬНЫЕ ПОЛЯ ИСКЛЮЧЕНИЯ =====
    //
    // Пользовательское исключение может хранить ЛЮБЫЕ данные, помогающие разобраться в ошибке.
    // Стандартные исключения хранят только message и cause — часто этого мало.
    //
    // Зачем поля, если всё есть в message?
    //   getMessage() возвращает строку: "Недостаточно ресурса 'мана': требуется 20, доступно 5"
    //   Парсить строку для извлечения данных — ПЛОХАЯ практика (хрупкий код).
    //   Геттеры (getResourceName(), getRequired(), getAvailable()) дают ПРЯМОЙ доступ к данным.
    //
    // ===== BEST PRACTICE: НЕИЗМЕНЯЕМЫЕ ПОЛЯ =====
    //
    // private — доступно только внутри этого класса. Внешний код использует геттеры.
    // final — значение устанавливается ОДИН РАЗ в конструкторе и больше НЕ МЕНЯЕТСЯ.
    //
    // Зачем final? Исключение описывает МОМЕНТ ошибки — данные не должны измениться позже.
    // Это делает исключение потокобезопасным (thread-safe) — несколько потоков могут
    // безопасно читать поля одновременно, т.к. никто не может их изменить.
    //
    // Частая ошибка: сделать поля НЕ final. Тогда кто-то может случайно изменить
    // данные исключения после его создания, и отладочная информация станет неверной.

    // Название ресурса, которого не хватает ("мана", "ярость", "стрелы").
    // Тип String — неизменяемый (immutable), что дополнительно гарантирует безопасность.
    private final String resourceName;

    // Сколько ресурса ТРЕБУЕТСЯ для действия.
    // Тип int (примитив), а не Integer (обёртка) — не может быть null, занимает меньше памяти.
    // Для полей исключений int предпочтительнее: количество ресурса всегда имеет значение.
    private final int required;

    // Сколько ресурса ИМЕЕТСЯ в наличии.
    private final int available;

    /**
     * Создаёт исключение о недостатке ресурса.
     *
     * Формирует информативное сообщение автоматически из переданных параметров.
     * Вызывающий код НЕ передаёт текстовое сообщение — оно строится из данных.
     * Это гарантирует единообразие сообщений (нельзя написать опечатку в тексте).
     *
     * Пример: throw new InsufficientResourceException("мана", 20, 5);
     * Сообщение: "Недостаточно ресурса 'мана': требуется 20, доступно 5"
     *
     * @param resourceName название ресурса
     * @param required     сколько нужно
     * @param available    сколько есть
     */
    public InsufficientResourceException(String resourceName, int required, int available) {
        // super(message) — вызов конструктора GameException(String).
        // Цепочка вызовов: InsufficientResourceException → GameException → Exception → Throwable.
        //
        // Конкатенация строк через + формирует сообщение из конкретных данных.
        // Это лучше, чем передавать готовую строку — формат всегда одинаковый.
        super("Недостаточно ресурса '" + resourceName + "': требуется " + required + ", доступно " + available);

        // this.resourceName — поле ЭТОГО объекта. resourceName — параметр конструктора.
        // this позволяет отличить поле класса от параметра с тем же именем.
        this.resourceName = resourceName;
        this.required = required;
        this.available = available;
    }

    /**
     * Конструктор с причиной (для цепочки исключений / exception chaining).
     *
     * Когда нехватка ресурса ВЫЗВАНА другим исключением, используем этот конструктор.
     * Пример: парсинг конфигурации ресурсов не удался → бросаем InsufficientResourceException
     * с cause = ParseException.
     *
     * @param resourceName название ресурса
     * @param required     сколько нужно
     * @param available    сколько есть
     * @param cause        исходное исключение (доступно через getCause())
     */
    public InsufficientResourceException(String resourceName, int required, int available, Throwable cause) {
        // super(message, cause) — вызов GameException(String, Throwable).
        // message передаётся в getMessage(), cause — в getCause().
        // При выводе стектрейса (e.printStackTrace()) будет видно:
        //   rpg.InsufficientResourceException: Недостаточно ресурса 'мана': ...
        //     at rpg.Game.handleSpecialAttack(Game.java:1354)
        //   Caused by: java.text.ParseException: ...
        super("Недостаточно ресурса '" + resourceName + "': требуется " + required + ", доступно " + available, cause);
        this.resourceName = resourceName;
        this.required = required;
        this.available = available;
    }

    // ===== ГЕТТЕРЫ ДЛЯ ДОПОЛНИТЕЛЬНЫХ ПОЛЕЙ =====
    //
    // Геттеры — методы для чтения private-полей (инкапсуляция, глава 3.22).
    // Позволяют обработчику (catch) получить детали ошибки ПРОГРАММНО,
    // а не парсить текстовое сообщение getMessage().
    //
    // Пример использования:
    //   catch (InsufficientResourceException e) {
    //       if (e.getRequired() - e.getAvailable() <= 5) {
    //           System.out.println("Почти хватает! Не хватает всего " +
    //               (e.getRequired() - e.getAvailable()) + " единиц " + e.getResourceName());
    //       }
    //   }

    /**
     * Возвращает название недостающего ресурса.
     *
     * @return название ресурса ("мана", "ярость", "стрелы")
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * Возвращает количество ресурса, которое требовалось для действия.
     *
     * @return необходимое количество ресурса
     */
    public int getRequired() {
        return required;
    }

    /**
     * Возвращает количество ресурса, которое было в наличии на момент ошибки.
     *
     * @return имеющееся количество ресурса
     */
    public int getAvailable() {
        return available;
    }

    // ===== ПОЧЕМУ НЕТ toString()? =====
    //
    // Мы НЕ переопределяем toString() — наследуем его от Throwable.
    // Throwable.toString() возвращает: "rpg.InsufficientResourceException: <message>"
    // Этого достаточно для отладки. getMessage() возвращает подробное сообщение.
    //
    // Если бы мы хотели включить в toString() все поля, это выглядело бы так:
    //   @Override
    //   public String toString() {
    //       return "InsufficientResourceException[resource=" + resourceName +
    //              ", required=" + required + ", available=" + available + "]";
    //   }
    // Но это НЕ принято — у исключений toString() обычно оставляют стандартным.
}
