// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в DamageType.java).
package rpg;

// ===== COMMANDPARSER — ПАРСЕР ТЕКСТОВЫХ КОМАНД ИГРОКА (глава 7.2) =====
//
// Этот класс демонстрирует основные операции со строками из раздела 7.2:
//   split(), strip(), trim(), toLowerCase(), toUpperCase(),
//   equals(), equalsIgnoreCase(), startsWith(), endsWith(),
//   contains(), indexOf(), lastIndexOf(), isEmpty(), isBlank(),
//   concat(), regionMatches(), String.join(), substring(),
//   replace(), compareTo(), getChars(), charAt().
//
// ===== ИГРОВАЯ РОЛЬ =====
//
// CommandParser — альтернативный текстовый ввод команд.
// Вместо числового меню (1, 2, 3...) игрок может написать:
//   "атака", "инвентарь", "помощь" и т.д.
// Парсер нормализует ввод (пробелы, регистр) и определяет команду.
//
// ===== ЗАЧЕМ УТИЛИТНЫЙ КЛАСС? =====
//
// Все методы — static (не зависят от состояния объекта).
// Нет полей (кроме констант) → нет смысла создавать экземпляр.
// Паттерн «utility class»:
//   1. Класс объявлен final (нельзя наследовать).
//   2. Конструктор private (нельзя создать экземпляр через new).
//   3. Все методы static.
//
// ===== final class (глава 3.23) =====
//
// Модификатор final перед class запрещает НАСЛЕДОВАНИЕ от этого класса.
//
// Зачем final для утилитного класса?
//   - Утилитный класс — это набор статических методов, а не «тип объекта».
//   - Наследование от него бессмысленно: нет экземпляров → нет полиморфизма.
//   - final явно показывает: «этот класс НЕ предназначен для расширения».
//
// Аналогия с C++:
//   В C++11 тоже можно запретить наследование: class MyClass final { ... };
//   Java использует тот же синтаксис: final class MyClass { ... }
//
// Примеры final-классов в стандартной библиотеке:
//   - java.lang.String — строки неизменяемы, наследование сломало бы контракт.
//   - java.lang.Integer, Long, Double — обёртки примитивов (immutable).
//   - java.lang.Math — утилитный класс с математическими функциями.
//   - java.lang.System — утилитный класс (in, out, err, currentTimeMillis и т.д.).
public final class CommandParser {

    // ===== ПРИВАТНЫЙ КОНСТРУКТОР — ЗАПРЕТ СОЗДАНИЯ ЭКЗЕМПЛЯРОВ =====
    //
    // Если конструктор private — вызвать new CommandParser() можно ТОЛЬКО
    // изнутри самого класса. Снаружи — ошибка компиляции.
    //
    // Зачем?
    //   Класс содержит только static-методы. Создание экземпляра бесполезно:
    //     CommandParser parser = new CommandParser(); // бессмысленно
    //     parser.parse("атака");                      // работает, но зачем объект?
    //     CommandParser.parse("атака");               // вызов напрямую — правильный способ
    //
    // ЧАСТАЯ ОШИБКА:
    //   Забыть приватный конструктор. Тогда компилятор создаст public конструктор
    //   по умолчанию, и кто-то сможет написать new CommandParser() — бессмысленно.
    //
    // Аналогия с C++:
    //   В C++ можно удалить конструктор: ClassName() = delete;
    //   В Java нет delete, поэтому делаем private.
    private CommandParser() {
    }

    // ===== ВЛОЖЕННЫЙ record: ParsedCommand (глава 3.19, 7.2) =====
    //
    // record — неизменяемый класс данных (Java 16+).
    // ParsedCommand хранит результат разбора команды:
    //   - command: нормализованная команда ("атака", "помощь", "инвентарь")
    //   - argument: аргумент команды (может быть пустой строкой "")
    //
    // Пример:
    //   Ввод: "  Атака  огненный шар  "
    //   Результат: ParsedCommand("атака", "огненный шар")
    //
    // Вложенный record — это static по умолчанию (в отличие от вложенных классов).
    // Его можно создать без ссылки на внешний объект:
    //   CommandParser.ParsedCommand cmd = new CommandParser.ParsedCommand("атака", "");
    //
    // Автоматически генерируются:
    //   - конструктор: new ParsedCommand("атака", "огненный шар")
    //   - геттеры: command(), argument() (без префикса get)
    //   - equals(), hashCode() — по ОБОИМ полям
    //   - toString(): "ParsedCommand[command=атака, argument=огненный шар]"
    public record ParsedCommand(
            // Нормализованная команда (lowercase, без пробелов).
            String command,

            // Аргумент после команды (может быть пустой строкой "").
            // Пустая строка "", а не null — избегаем NullPointerException.
            String argument
    ) {
        // ===== isEmpty() и isBlank() — ПРОВЕРКА ПУСТОТЫ СТРОКИ (глава 7.2) =====
        //
        // isEmpty() — строка пустая? Длина == 0, т.е. "".
        //   "".isEmpty()        → true
        //   "  ".isEmpty()      → false  (пробелы — это символы!)
        //   "hello".isEmpty()   → false
        //
        // isBlank() — строка пустая ИЛИ содержит только пробельные символы?
        //   (Java 11+)
        //   "".isBlank()        → true
        //   "  ".isBlank()      → true   (только пробелы — «пусто»)
        //   "\t\n".isBlank()    → true   (табуляция и перевод строки — тоже «пусто»)
        //   "hello".isBlank()   → false
        //
        // КЛЮЧЕВОЕ ОТЛИЧИЕ:
        //   isEmpty() проверяет length() == 0 (строго пустая строка "").
        //   isBlank() проверяет, что ВСЕ символы — пробельные (Character.isWhitespace).
        //
        // Аналогия с C:
        //   isEmpty() ≈ strlen(s) == 0
        //   isBlank() — нет прямого аналога в C, нужно писать цикл проверки isspace().
        //
        // КОГДА ЧТО ИСПОЛЬЗОВАТЬ:
        //   - Проверка пользовательского ввода → isBlank() (пользователь мог нажать только пробел).
        //   - Проверка технических строк (ключи, ID) → isEmpty() (пробел — допустимый символ).
        public boolean hasArgument() {
            return argument != null && !argument.isBlank();
        }
    }

    // ===== PARSE — ОСНОВНОЙ МЕТОД РАЗБОРА КОМАНДЫ (глава 7.2) =====
    //
    // Принимает сырой ввод пользователя и возвращает ParsedCommand.
    //
    // Алгоритм:
    //   1. Проверка на null / пустую строку → возврат пустой команды.
    //   2. strip() — удаление пробелов по краям.
    //   3. split() — разбиение на слова.
    //   4. toLowerCase() — приведение команды к нижнему регистру.
    //   5. Сборка аргумента из оставшихся слов.
    //
    // Возвращает ParsedCommand (НИКОГДА не null).
    public static ParsedCommand parse(String input) {

        // ===== ПРОВЕРКА НА null =====
        //
        // В Java строка может быть null — это значит «ссылка никуда не указывает».
        // Вызов ЛЮБОГО метода на null бросит NullPointerException:
        //   null.strip()  → NullPointerException!
        //
        // Поэтому СНАЧАЛА проверяем null, и только ПОТОМ вызываем методы строки.
        //
        // Аналогия с C/C++:
        //   В C указатель может быть NULL, и вызов strlen(NULL) → undefined behavior.
        //   В Java null-вызов → гарантированный NullPointerException (определённое поведение).
        if (input == null) {
            return new ParsedCommand("", "");
        }

        // ===== strip() — УДАЛЕНИЕ ПРОБЕЛОВ ПО КРАЯМ (глава 7.2) =====
        //
        // strip() удаляет ВСЕ пробельные символы (whitespace) с НАЧАЛА и КОНЦА строки.
        // Пробельные символы: пробел ' ', табуляция '\t', перевод строки '\n', '\r' и т.д.
        //
        //   "  Hello World  ".strip()   → "Hello World"   (пробелы по краям удалены)
        //   "  \t Hello \n  ".strip()   → "Hello"         (табуляция и перенос — тоже)
        //   "Hello".strip()             → "Hello"         (нечего удалять)
        //
        // ===== strip() vs trim() — В ЧЁМ РАЗНИЦА? =====
        //
        // trim() — старый метод (с Java 1.0). Удаляет символы с кодом ≤ U+0020 (ASCII пробел).
        // strip() — новый метод (с Java 11). Удаляет ВСЕ Unicode-пробелы (Character.isWhitespace).
        //
        // Разница проявляется на Unicode-пробелах:
        //   '\u2000' (En Quad) — типографский пробел. trim() его НЕ удалит, strip() — удалит.
        //   '\u3000' (Ideographic Space) — пробел в японском тексте. Аналогично.
        //
        // РЕКОМЕНДАЦИЯ: всегда используйте strip() вместо trim() в новом коде.
        //   trim() оставлен для обратной совместимости.
        //
        // Дополнительные методы:
        //   stripLeading()  — удаляет пробелы только СЛЕВА:  "  Hello  ".stripLeading()  → "Hello  "
        //   stripTrailing() — удаляет пробелы только СПРАВА: "  Hello  ".stripTrailing() → "  Hello"
        String stripped = input.strip();

        // ===== isEmpty() — ПРОВЕРКА ПОСЛЕ strip() =====
        //
        // После удаления пробелов строка может стать пустой (если ввод был "   ").
        // isEmpty() возвращает true для строки длиной 0 ("").
        //
        // Почему isEmpty(), а не isBlank()?
        //   Мы уже вызвали strip() — все пробелы удалены.
        //   Если после strip() строка не пуста — она точно содержит непробельные символы.
        //   Поэтому isEmpty() здесь достаточно и даже точнее по смыслу.
        if (stripped.isEmpty()) {
            return new ParsedCommand("", "");
        }

        // ===== split() — РАЗБИЕНИЕ СТРОКИ НА ЧАСТИ (глава 7.2) =====
        //
        // split(String regex) разбивает строку по регулярному выражению (regex).
        // Возвращает массив String[] с частями.
        //
        //   "Hello World".split(" ")       → ["Hello", "World"]
        //   "a,b,c".split(",")             → ["a", "b", "c"]
        //   "one  two  three".split(" ")   → ["one", "", "two", "", "three"]  ← ЛОВУШКА!
        //
        // ЛОВУШКА С НЕСКОЛЬКИМИ ПРОБЕЛАМИ:
        //   split(" ") разбивает по ОДНОМУ пробелу. Два подряд пробела → пустая строка между ними.
        //   Решение: split("\\s+") — разбиваем по ОДНОМУ ИЛИ БОЛЕЕ пробельных символов.
        //
        //   "one  two  three".split("\\s+") → ["one", "two", "three"]  ← правильно!
        //
        // \\s — регулярное выражение для «пробельный символ» (пробел, табуляция, перевод строки).
        // +   — квантификатор «один или более». \\s+ = «один или более пробельных символов».
        //
        // ВНИМАНИЕ: параметр split() — это REGEX (регулярное выражение), НЕ просто строка!
        //   "a.b.c".split(".")   → ["", "", "", "", ""]  ← ОШИБКА! Точка = «любой символ» в regex!
        //   "a.b.c".split("\\.")  → ["a", "b", "c"]      ← Правильно: экранируем точку.
        //
        // Аналогия с C:
        //   В C для разбиения строки используют strtok(str, " ").
        //   strtok() модифицирует исходную строку (вставляет '\0')!
        //   Java split() НЕ модифицирует строку — возвращает НОВЫЙ массив. Строки immutable.
        String[] parts = stripped.split("\\s+");

        // ===== toLowerCase() — ПРИВЕДЕНИЕ К НИЖНЕМУ РЕГИСТРУ (глава 7.2) =====
        //
        // toLowerCase() возвращает НОВУЮ строку, где все буквы — в нижнем регистре.
        //
        //   "Hello World".toLowerCase()   → "hello world"
        //   "АТАКА".toLowerCase()         → "атака"      (кириллица тоже работает!)
        //   "hello".toLowerCase()         → "hello"      (уже в нижнем — возвращает ту же строку)
        //
        // ВАЖНО: toLowerCase() возвращает НОВУЮ строку. Оригинал НЕ меняется!
        //   String s = "HELLO";
        //   s.toLowerCase();      // ← ОШИБКА: результат нигде не сохранён! s всё ещё "HELLO"
        //   s = s.toLowerCase();  // ← ПРАВИЛЬНО: сохраняем результат обратно в переменную
        //
        // Это следствие НЕИЗМЕНЯЕМОСТИ (immutability) строк в Java:
        //   Строки нельзя модифицировать «на месте». Каждая операция возвращает новую строку.
        //
        // toUpperCase() — обратная операция: "hello" → "HELLO".
        //   Используется для выделения текста (критические удары, заголовки).
        //
        // Аналогия с C:
        //   В C функция tolower() меняет ОДИН символ, а для строки нужен цикл.
        //   В Java — один вызов toLowerCase() для всей строки.
        String command = parts[0].toLowerCase();

        // ===== ИЗВЛЕЧЕНИЕ АРГУМЕНТА =====
        //
        // Если пользователь ввёл "атака огненный шар", то:
        //   parts[0] = "атака"       → command
        //   parts[1] = "огненный"  }
        //   parts[2] = "шар"       } → аргумент = "огненный шар"
        //
        // Мы используем indexOf() + substring() для извлечения аргумента
        // из оригинальной (stripped) строки, чтобы сохранить исходные пробелы
        // между словами аргумента.

        // ===== indexOf() — ПОИСК ПОЗИЦИИ ПОДСТРОКИ (глава 7.2) =====
        //
        // indexOf(String str) возвращает индекс ПЕРВОГО вхождения подстроки в строку.
        // Если подстрока не найдена — возвращает -1.
        //
        //   "Hello World".indexOf("World")   → 6
        //   "Hello World".indexOf("o")       → 4   (первое 'o')
        //   "Hello World".indexOf("xyz")     → -1  (не найдено)
        //
        // indexOf(int ch) — ищет СИМВОЛ (char) по его коду:
        //   "Hello".indexOf('l')   → 2  (первая 'l')
        //
        // indexOf(String str, int fromIndex) — ищет, начиная с позиции fromIndex:
        //   "Hello World".indexOf("o", 5)   → 7  (пропускает первое 'o' на позиции 4)
        //
        // lastIndexOf() — ищет ПОСЛЕДНЕЕ вхождение (от конца строки):
        //   "Hello World".lastIndexOf('o')  → 7  (последняя 'o')
        //
        // Аналогия с C:
        //   indexOf() ≈ strstr(haystack, needle) — но возвращает индекс, а не указатель.
        //   lastIndexOf() ≈ нет прямого аналога в C (нужно искать вручную с конца).
        String argument = "";
        if (parts.length > 1) {
            // ===== substring() — ИЗВЛЕЧЕНИЕ ПОДСТРОКИ (глава 7.2) =====
            //
            // substring(int beginIndex) — возвращает подстроку от beginIndex до конца.
            //   "Hello World".substring(6)    → "World"
            //
            // substring(int beginIndex, int endIndex) — от beginIndex до endIndex (не включая endIndex).
            //   "Hello World".substring(0, 5) → "Hello"
            //
            // ВНИМАНИЕ: endIndex — это ЭКСКЛЮЗИВНАЯ граница (не включается).
            //   Это стандартная конвенция Java: [begin, end) — полуоткрытый интервал.
            //   substring(0, 5) = символы с индексами 0, 1, 2, 3, 4 (5 НЕ входит).
            //
            // ЧАСТАЯ ОШИБКА:
            //   "Hello".substring(5)   → ""  (пустая строка, не исключение)
            //   "Hello".substring(6)   → StringIndexOutOfBoundsException!
            //
            // Мы находим позицию первого пробела после команды и берём всё после него.
            // strip() в конце убирает лишние пробелы, если они были.
            int spaceIndex = stripped.indexOf(' ');
            if (spaceIndex != -1) {
                argument = stripped.substring(spaceIndex + 1).strip();
            }
        }

        return new ParsedCommand(command, argument);
    }

    // ===== matchCommand — СОПОСТАВЛЕНИЕ ВВОДА С НАБОРОМ АЛИАСОВ (глава 7.2) =====
    //
    // Проверяет, совпадает ли пользовательский ввод с одним из алиасов команды.
    // Используется для гибкого распознавания: "атака", "атаковать", "ат", "attack".
    //
    // Varargs (String... aliases) — переменное число аргументов (глава 2.9):
    //   matchCommand("атака", "атака", "атаковать", "ат", "attack")
    //   Внутри метода aliases — это массив String[].
    //
    // Возвращает true, если ввод совпадает хотя бы с одним алиасом.
    public static boolean matchCommand(String input, String... aliases) {

        if (input == null || input.isBlank()) {
            return false;
        }

        // strip() + toLowerCase() — нормализация ввода перед сравнением.
        // Без нормализации: "  АТАКА  " не совпадёт с "атака".
        String normalized = input.strip().toLowerCase();

        for (String alias : aliases) {
            if (alias == null) {
                continue;
            }

            // ===== equalsIgnoreCase() — СРАВНЕНИЕ БЕЗ УЧЁТА РЕГИСТРА (глава 7.2) =====
            //
            // equalsIgnoreCase() сравнивает строки, ИГНОРИРУЯ регистр букв.
            //
            //   "Hello".equalsIgnoreCase("hello")   → true
            //   "АТАКА".equalsIgnoreCase("атака")   → true  (кириллица тоже!)
            //   "Hello".equalsIgnoreCase("World")   → false
            //
            // ОТЛИЧИЕ ОТ equals():
            //   equals()           — строгое сравнение: "Hello" ≠ "hello"
            //   equalsIgnoreCase() — мягкое сравнение:  "Hello" = "hello"
            //
            // ОТЛИЧИЕ ОТ ==:
            //   == сравнивает ССЫЛКИ (адреса в памяти), а не содержимое!
            //   new String("abc") == new String("abc")      → false  (разные объекты!)
            //   new String("abc").equals(new String("abc")) → true   (одинаковое содержимое)
            //
            //   Строковые литералы кешируются в String Pool:
            //   "abc" == "abc"  → true  (один объект в пуле)
            //   Но это ДЕТАЛЬ РЕАЛИЗАЦИИ. Всегда используйте equals() для сравнения строк!
            //
            // Аналогия с C:
            //   equals()           ≈ strcmp(a, b) == 0
            //   equalsIgnoreCase() ≈ strcasecmp(a, b) == 0  (POSIX-функция)
            if (normalized.equalsIgnoreCase(alias.strip())) {
                return true;
            }

            // ===== regionMatches() — СРАВНЕНИЕ ПОДСТРОК (глава 7.2) =====
            //
            // regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)
            //
            // Сравнивает ЧАСТЬ одной строки с ЧАСТЬЮ другой:
            //   - ignoreCase: игнорировать регистр? true/false
            //   - toffset: начальный индекс в ЭТОЙ строке
            //   - other: строка для сравнения
            //   - ooffset: начальный индекс в other
            //   - len: сколько символов сравнивать
            //
            // Пример:
            //   "Hello World".regionMatches(true, 0, "HELLO", 0, 5)  → true
            //   Сравнивает "Hello" (позиции 0–4) с "HELLO" (позиции 0–4), без учёта регистра.
            //
            // Зачем regionMatches(), если есть equalsIgnoreCase()?
            //   - equalsIgnoreCase() сравнивает ВСЕЙ строки целиком.
            //   - regionMatches() сравнивает ПОДСТРОКУ — полезно для «начинается ли на...?»
            //     с дополнительным контролем над позициями.
            //
            // Здесь мы проверяем: «начинается ли ввод с алиаса?»
            // Это позволяет распознать "атака огненный шар" по алиасу "атака".
            String aliasLower = alias.strip().toLowerCase();
            if (aliasLower.length() >= 2 && normalized.length() >= aliasLower.length()) {
                if (normalized.regionMatches(true, 0, aliasLower, 0, aliasLower.length())) {
                    // ===== charAt() — ДОСТУП К СИМВОЛУ ПО ИНДЕКСУ (глава 7.2) =====
                    //
                    // charAt(int index) возвращает символ (char) на заданной позиции.
                    //
                    //   "Hello".charAt(0)   → 'H'
                    //   "Hello".charAt(4)   → 'o'
                    //   "Hello".charAt(5)   → StringIndexOutOfBoundsException!
                    //
                    // Индексация начинается с 0 (как в массивах).
                    //
                    // Аналогия с C:
                    //   charAt(i) ≈ str[i] в C.
                    //   Но в Java нет оператора [] для строк — только метод charAt().
                    //
                    // Проверяем, что после алиаса — пробел или конец строки.
                    // Иначе "ат" совпадёт с "атрибут" — нежелательно.
                    if (normalized.length() == aliasLower.length()
                            || normalized.charAt(aliasLower.length()) == ' ') {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // ===== extractArgument — ИЗВЛЕЧЕНИЕ АРГУМЕНТА ИЗ ВВОДА (глава 7.2) =====
    //
    // Извлекает всё, что идёт после первого слова (команды).
    // Пример: "использовать зелье здоровья" → "зелье здоровья"
    //
    // Демонстрирует: indexOf(), substring(), strip().
    public static String extractArgument(String input) {

        if (input == null || input.isBlank()) {
            return "";
        }

        // strip() удаляет пробелы по краям перед поиском.
        String stripped = input.strip();

        // ===== contains() — ПРОВЕРКА НАЛИЧИЯ ПОДСТРОКИ (глава 7.2) =====
        //
        // contains(CharSequence s) возвращает true, если строка содержит указанную подстроку.
        //
        //   "Hello World".contains("World")   → true
        //   "Hello World".contains("world")   → false  (регистрозависимо!)
        //   "Hello World".contains("")        → true   (пустая строка «содержится» везде)
        //
        // contains() — это удобная обёртка над indexOf():
        //   str.contains(s)  ≡  str.indexOf(s) >= 0
        //
        // Аналогия с C:
        //   contains() ≈ strstr(haystack, needle) != NULL
        //
        // Здесь проверяем: есть ли пробел в строке (т.е. есть ли аргумент)?
        if (!stripped.contains(" ")) {
            return "";
        }

        // indexOf(' ') находит первый пробел → всё после него — аргумент.
        int spaceIndex = stripped.indexOf(' ');
        return stripped.substring(spaceIndex + 1).strip();
    }

    // ===== buildHelp — ПОСТРОЕНИЕ ТЕКСТА ПОМОЩИ (глава 7.2) =====
    //
    // Формирует строку с перечислением доступных команд.
    // Демонстрирует: String.join(), concat(), startsWith(), endsWith(),
    // compareTo(), toUpperCase(), getChars(), replace().
    public static String buildHelp() {

        // ===== String.join() — ОБЪЕДИНЕНИЕ СТРОК С РАЗДЕЛИТЕЛЕМ (глава 7.2) =====
        //
        // String.join(CharSequence delimiter, CharSequence... elements)
        // Объединяет элементы в одну строку, вставляя разделитель между ними.
        //
        //   String.join(", ", "a", "b", "c")       → "a, b, c"
        //   String.join(" - ", "Воин", "Маг", "Лучник") → "Воин - Маг - Лучник"
        //   String.join("", "a", "b", "c")         → "abc"  (пустой разделитель)
        //
        // String.join() — статический метод класса String.
        // Вызывается через имя класса: String.join(...), а не через объект.
        //
        // Преимущества перед конкатенацией (+):
        //   1. Разделитель указывается ОДИН раз (не нужно вставлять ", " между каждой парой).
        //   2. Нет «лишнего» разделителя в конце (", a, b, c, " — нет!).
        //   3. Работает с любым количеством аргументов (varargs).
        //
        // Также принимает Iterable:
        //   List<String> names = List.of("Воин", "Маг", "Лучник");
        //   String.join(", ", names)  → "Воин, Маг, Лучник"
        //
        // Аналогия с C:
        //   В C нет аналога String.join(). Нужно писать цикл с strcat() и разделителем.
        //   В Python: ", ".join(["a", "b", "c"]) — почти идентичный синтаксис.
        String commands = String.join(", ",
                "атака", "защита", "магия", "инвентарь",
                "зелье", "стат", "помощь", "выход"
        );

        // ===== concat() — КОНКАТЕНАЦИЯ СТРОК (глава 7.2) =====
        //
        // concat(String str) объединяет две строки в одну.
        //
        //   "Hello".concat(" World")  → "Hello World"
        //   "Java".concat("")         → "Java"  (конкатенация с пустой строкой)
        //
        // ОТЛИЧИЕ ОТ ОПЕРАТОРА +:
        //   "Hello" + " World"         — тоже конкатенация, но:
        //   - Оператор + автоматически преобразует нестроковые типы: "age: " + 25 → "age: 25"
        //   - concat() принимает ТОЛЬКО String: "age: ".concat(25) → ошибка компиляции!
        //   - concat() на null бросит NullPointerException: null.concat("x") → NPE!
        //   - Оператор + обрабатывает null: null + "x" → "nullx"
        //
        // НА ПРАКТИКЕ: почти всегда используют оператор + (он проще и гибче).
        // concat() показан здесь для учебных целей.
        String header = "Доступные команды: ".concat(commands);

        // ===== startsWith() и endsWith() — ПРОВЕРКА НАЧАЛА И КОНЦА СТРОКИ (глава 7.2) =====
        //
        // startsWith(String prefix) — начинается ли строка с указанного префикса?
        //   "myfile.txt".startsWith("my")    → true
        //   "myfile.txt".startsWith("My")    → false  (регистрозависимо!)
        //
        // endsWith(String suffix) — заканчивается ли строка указанным суффиксом?
        //   "myfile.txt".endsWith(".txt")    → true
        //   "myfile.txt".endsWith(".exe")    → false
        //
        // Аналогия с C:
        //   startsWith() ≈ strncmp(str, prefix, strlen(prefix)) == 0
        //   endsWith() — нет прямого аналога в C, нужен ручной расчёт смещения.
        //
        // Демонстрация: проверяем формат строки помощи.
        // В реальном коде эти проверки используются для файлов (.dat, .zip),
        // URL-адресов (http://, https://), команд (/give, /set) и т.д.
        // assert выражение : "сообщение" — проверяет инвариант в debug-режиме (глава 4.4).
        // Если выражение = false — бросает AssertionError с сообщением.
        // Активируется только при запуске с флагом -ea (enable assertions).
        // Здесь — self-check: убеждаемся, что format вывода не сломан.
        assert header.startsWith("Доступные") : "Заголовок должен начинаться с 'Доступные'";
        assert header.endsWith("выход") : "Список должен заканчиваться на 'выход'";

        // ===== compareTo() — ЛЕКСИКОГРАФИЧЕСКОЕ СРАВНЕНИЕ СТРОК (глава 7.2) =====
        //
        // compareTo(String anotherString) сравнивает две строки в лексикографическом порядке.
        //
        // Возвращает:
        //   < 0  — эта строка МЕНЬШЕ (раньше в алфавите)
        //   = 0  — строки РАВНЫ
        //   > 0  — эта строка БОЛЬШЕ (позже в алфавите)
        //
        //   "apple".compareTo("banana")  → отрицательное число ('a' < 'b')
        //   "banana".compareTo("apple")  → положительное число ('b' > 'a')
        //   "hello".compareTo("hello")   → 0
        //
        // compareToIgnoreCase() — то же, но без учёта регистра:
        //   "Apple".compareToIgnoreCase("apple")  → 0
        //
        // ЗАЧЕМ compareTo()?
        //   - Сортировка строк: Collections.sort() использует compareTo().
        //   - TreeSet<String> и TreeMap<String, V> — хранят строки в порядке compareTo().
        //   - Проверка «алфавитный порядок»: if (a.compareTo(b) < 0) — a перед b.
        //
        // Аналогия с C:
        //   compareTo() ≈ strcmp(a, b) — возвращает <0, 0, >0.
        //   compareToIgnoreCase() ≈ strcasecmp(a, b) (POSIX).
        //
        // Здесь — учебная демонстрация для сортировки команд:
        String[] sortedCommands = {"атака", "выход", "защита", "зелье",
                "инвентарь", "магия", "помощь", "стат"};

        // Проверяем, что массив отсортирован (compareTo должен быть <= 0).
        for (int i = 0; i < sortedCommands.length - 1; i++) {
            assert sortedCommands[i].compareTo(sortedCommands[i + 1]) <= 0
                    : "Команды не отсортированы!";
        }

        // ===== toUpperCase() — ПРЕОБРАЗОВАНИЕ К ВЕРХНЕМУ РЕГИСТРУ (глава 7.2) =====
        //
        // toUpperCase() возвращает НОВУЮ строку, где все буквы — в верхнем регистре.
        //   "hello".toUpperCase()  → "HELLO"
        //   "Атака".toUpperCase()  → "АТАКА"
        //
        // Как и toLowerCase(), НЕ модифицирует оригинал (строки immutable).
        //
        // Используется для выделения: заголовки, критические удары, важные сообщения.
        String title = "помощь".toUpperCase();

        // ===== getChars() — КОПИРОВАНИЕ СИМВОЛОВ В МАССИВ (глава 7.2) =====
        //
        // getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)
        //
        // Копирует символы из строки в массив char[].
        //   - srcBegin: начальный индекс в строке (включительно)
        //   - srcEnd: конечный индекс в строке (НЕ включительно)
        //   - dst: целевой массив символов
        //   - dstBegin: начальный индекс в целевом массиве
        //
        //   String s = "Hello World";
        //   char[] buf = new char[5];
        //   s.getChars(6, 11, buf, 0);  → buf = {'W', 'o', 'r', 'l', 'd'}
        //
        // ЗАЧЕМ getChars()?
        //   - Эффективное копирование фрагмента строки в существующий буфер.
        //   - substring() создаёт НОВЫЙ объект String, getChars() копирует в ИМЕЮЩИЙСЯ массив.
        //   - Полезно при низкоуровневой работе с символами (парсеры, форматтеры).
        //
        // Аналогия с C:
        //   getChars() ≈ memcpy(dst + dstBegin, src + srcBegin, (srcEnd - srcBegin) * sizeof(char))
        //   Но Java проверяет границы — ArrayIndexOutOfBoundsException при выходе за пределы.
        //
        // Демонстрация: копируем заголовок в буфер для декоративной рамки.
        char[] titleBuffer = new char[title.length() + 4];
        titleBuffer[0] = '[';
        titleBuffer[1] = ' ';
        title.getChars(0, title.length(), titleBuffer, 2);
        titleBuffer[titleBuffer.length - 2] = ' ';
        titleBuffer[titleBuffer.length - 1] = ']';
        // titleBuffer = "[ ПОМОЩЬ ]"
        String decoratedTitle = new String(titleBuffer);

        // ===== replace() — ЗАМЕНА ПОДСТРОК (глава 7.2) =====
        //
        // replace(CharSequence target, CharSequence replacement)
        // Заменяет ВСЕ вхождения target на replacement.
        //
        //   "Hello World".replace("World", "Java")  → "Hello Java"
        //   "aabaa".replace("aa", "x")              → "xbx"
        //   "Hello".replace("xyz", "abc")           → "Hello"  (нет вхождений — не меняется)
        //
        // replace(char oldChar, char newChar) — замена СИМВОЛА:
        //   "Hello".replace('l', 'r')  → "Herro"
        //
        // ВАЖНО: replace() НЕ использует регулярные выражения (в отличие от replaceAll).
        //   replace(".", "!") — заменяет ТОЧКУ на восклицательный знак (буквально).
        //   replaceAll(".", "!") — заменяет КАЖДЫЙ СИМВОЛ (точка = «любой символ» в regex)!
        //
        // Аналогия с C:
        //   В C нет встроенного replace для строк. Нужно писать вручную с strstr() + memmove().
        //   Java делает это одним вызовом.
        //
        // Демонстрация: формируем подсказку с маркерами.
        String hint = "Введите [команда] или [номер]:";
        hint = hint.replace("[команда]", commands.split(",")[0].strip());
        hint = hint.replace("[номер]", "1-8");

        // Собираем итоговый текст помощи.
        // Оператор + для конкатенации строк — самый распространённый способ в Java.
        // Компилятор оптимизирует цепочку + в StringBuilder.append() (с Java 9+).
        return decoratedTitle + "\n" + header + "\n" + hint;
    }
}
