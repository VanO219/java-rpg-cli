// ===== ЗАЧЕМ ЭТОТ ФАЙЛ? =====
//
// StringValidator — утилитный класс для валидации пользовательского ввода в RPG-игре
// с помощью регулярных выражений (regex). Проверяет имена героев, парсит чит-коды,
// цензурирует нежелательные слова, находит числа в тексте.
//
// Этот класс демонстрирует работу с регулярными выражениями из главы 7.4:
//
//   - Pattern.compile() — компиляция регулярного выражения в объект Pattern
//   - Pattern.matches() — статический метод для быстрой проверки соответствия
//   - Pattern.split() — разбиение строки по regex-разделителю
//   - Matcher.matches() — проверка полного соответствия строки паттерну
//   - Matcher.find() — поиск СЛЕДУЮЩЕГО вхождения паттерна в строке (в цикле)
//   - Matcher.group() — получение текста, совпавшего с паттерном (или группой)
//   - Matcher.start() — индекс НАЧАЛА текущего совпадения
//   - Matcher.end() — индекс КОНЦА текущего совпадения (следующий символ после)
//   - Matcher.replaceAll() — замена ВСЕХ совпадений на указанную строку
//   - String.matches() — удобный метод (делегирует в Pattern.matches)
//   - String.replaceAll() — удобный метод (делегирует в Pattern/Matcher)
//   - String.split() — разбиение строки по regex
//
// ===== ЧТО ТАКОЕ РЕГУЛЯРНЫЕ ВЫРАЖЕНИЯ (REGEX)? (7.4) =====
//
// Regex (regular expression) — это ШАБЛОН для поиска и обработки текста.
// Вместо того чтобы искать конкретную строку ("Java"), можно задать ПАТТЕРН:
//   "Java\\w*" — найдёт "Java", "JavaScript", "JavaSE" и т.д.
//
// Аналогия из C/C++:
//   В C стандартная библиотека НЕ имеет regex (нужен POSIX regex.h или <regex> в C++11).
//   В C++ regex появился в <regex> (#include <regex>, std::regex, std::smatch).
//   В Java regex встроен в стандартную библиотеку java.util.regex.
//
// ===== СИНТАКСИС REGEX (основные элементы) =====
//
// СИМВОЛЬНЫЕ КЛАССЫ:
//   .      — любой символ (кроме \n)
//   \d     — цифра [0-9]                     (в Java: "\\d" — двойной слеш!)
//   \D     — НЕ цифра [^0-9]
//   \w     — «словесный» символ [a-zA-Z0-9_] (в Java: "\\w")
//   \W     — НЕ словесный символ
//   \s     — пробельный символ [ \t\n\r\f]   (в Java: "\\s")
//   \S     — НЕ пробельный символ
//   [abc]  — один из символов: a, b или c
//   [a-z]  — диапазон: от a до z
//   [^abc] — любой символ КРОМЕ a, b, c
//
// КВАНТИФИКАТОРЫ (количество повторений):
//   *      — 0 или более раз       ("a*" → "", "a", "aaa")
//   +      — 1 или более раз       ("a+" → "a", "aaa", но НЕ "")
//   ?      — 0 или 1 раз           ("a?" → "" или "a")
//   {n}    — ровно n раз           ("a{3}" → "aaa")
//   {n,}   — n или более раз       ("a{2,}" → "aa", "aaa", "aaaa"...)
//   {n,m}  — от n до m раз         ("a{2,4}" → "aa", "aaa", "aaaa")
//
// ЯКОРЯ (позиция в строке):
//   ^      — начало строки          ("^Hello" → строка начинается с "Hello")
//   $      — конец строки           ("world$" → строка заканчивается на "world")
//
// ГРУППЫ И АЛЬТЕРНАЦИЯ:
//   (abc)  — группа: захватывает подстроку для Matcher.group(n)
//   |      — ИЛИ: "cat|dog" → "cat" или "dog"
//   (?:ab) — группа БЕЗ захвата (не попадёт в group())
//
// ЭКРАНИРОВАНИЕ:
//   \\     — экранирование спецсимволов: \., \*, \+, \?, \(, \), \[, \], \{, \}
//
// ВАЖНО! В Java строках обратный слеш сам по себе — escape-символ:
//   \n = перевод строки, \t = табуляция
//   Поэтому для regex \d нужно писать "\\d" (первый \ экранирует второй).
//   Для литерального обратного слеша в regex: "\\\\" (4 слеша: Java-экранирование + regex-экранирование).
//
// ===== Pattern vs String.matches() (7.4) =====
//
// String.matches(regex) — удобно, но КАЖДЫЙ вызов компилирует regex заново!
//   "hello".matches("\\w+") → внутри вызывает Pattern.compile("\\w+").matcher("hello").matches()
//
// Pattern.compile(regex) — компилирует ОДИН раз, потом переиспользуем:
//   static final Pattern P = Pattern.compile("\\w+");   // компиляция 1 раз при загрузке класса
//   P.matcher("hello").matches();                        // только создание Matcher — быстро!
//
// ПРАВИЛО: если regex используется МНОГОКРАТНО — всегда Pattern.compile() в static final.
// Если один раз (например, в unit-тесте) — String.matches() допустим.
//
package rpg;

// ===== ИМПОРТЫ java.util.regex (7.4) =====
//
// Пакет java.util.regex содержит два ключевых класса:
//   Pattern — скомпилированное регулярное выражение (шаблон).
//   Matcher — движок поиска: применяет Pattern к конкретной строке.
//
// Workflow: Pattern.compile(regex) → pattern.matcher(text) → matcher.find()/matches()/group()
//
// Аналогия с C++11:
//   Pattern ≈ std::regex (компиляция шаблона)
//   Matcher ≈ std::smatch + std::regex_search/std::regex_match (поиск совпадений)
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// ===== ИМПОРТЫ коллекций =====
//
// List и ArrayList используются для возврата результатов поиска
// (список найденных совпадений, чисел, позиций).
import java.util.List;
import java.util.ArrayList;

// ===== final class StringValidator =====
//
// Утилитный класс: final class + private конструктор + static методы.
// Паттерн идентичен TextFormatter, CommandParser, BattleNarrator (ADR-001).
//
// final — запрет наследования (глава 3.23). Все методы статические,
// наследование бессмысленно. JVM может инлайнить вызовы методов final-класса.
//
// Аналогия с C++: class StringValidator final { ... };
// В Java final ПЕРЕД class, в C++ — ПОСЛЕ имени.
public final class StringValidator {

    // ===== ПРИВАТНЫЙ КОНСТРУКТОР (запрет создания экземпляров) =====
    //
    // Все методы статические — экземпляр класса не нужен.
    // Без приватного конструктора Java создаст публичный конструктор по умолчанию,
    // и кто-нибудь напишет: new StringValidator() — бессмысленный объект.
    //
    // Частая ошибка: забыть приватный конструктор у утилитного класса.
    private StringValidator() {
    }

    // =====================================================================
    // ===== СКОМПИЛИРОВАННЫЕ ПАТТЕРНЫ (static final Pattern) (7.4) ========
    // =====================================================================
    //
    // Pattern.compile(regex) — компилирует строку regex в объект Pattern.
    // Это ДОРОГАЯ операция (построение конечного автомата), поэтому результат
    // сохраняем в static final константу — компиляция происходит ОДИН раз
    // при загрузке класса.
    //
    // Аналогия с C++: static const std::regex pattern("...");
    //
    // ПРАВИЛО: если regex используется многократно — ВСЕГДА static final Pattern.
    // Никогда не вызывайте Pattern.compile() внутри цикла или часто вызываемого метода!

    // ===== HERO_NAME_PATTERN — паттерн для имени героя =====
    //
    // ^[a-zA-Zа-яА-ЯёЁ]         — начинается с буквы (латиница или кириллица)
    //   ^ — якорь «начало строки»
    //   [a-zA-Zа-яА-ЯёЁ] — символьный класс: любая буква
    //
    // [a-zA-Zа-яА-ЯёЁ\\s-]{0,19} — далее до 19 букв, пробелов или дефисов
    //   {0,19} — квантификатор: от 0 до 19 повторений
    //   \\s — пробельный символ (\s в regex, \\ для экранирования в Java)
    //   - — дефис (в конце символьного класса не нужно экранировать)
    //
    // $ — якорь «конец строки»
    //
    // Итого: имя от 1 до 20 символов, начинается с буквы, содержит буквы/пробелы/дефисы.
    //
    // В Game.java:readHeroName() уже есть проверка через String.matches():
    //   name.matches("[a-zA-Zа-яА-ЯёЁ\\s-]+")
    // Здесь мы используем скомпилированный Pattern для лучшей производительности
    // и добавляем ГРУППЫ для извлечения частей имени.
    //
    // ГРУППЫ в regex: круглые скобки () создают "группы захвата".
    // Matcher.group(0) — всё совпадение целиком.
    // Matcher.group(1) — первая группа (первая пара скобок).
    // Matcher.group(2) — вторая группа и т.д.
    //
    // В нашем паттерне:
    //   group(1) — первое слово (имя)
    //   group(2) — остаток (фамилия/прозвище), может быть null если имя из одного слова
    private static final Pattern HERO_NAME_PATTERN = Pattern.compile(
            "^([a-zA-Zа-яА-ЯёЁ]+)([-\\s][a-zA-Zа-яА-ЯёЁ-\\s]*)?$"
    );

    // ===== CHEAT_CODE_PATTERN — паттерн для чит-кодов =====
    //
    // Формат чит-кода: "/команда аргумент", например:
    //   "/give_gold 100"   — дать 100 золота
    //   "/heal 50"         — восстановить 50 HP
    //   "/levelup"         — повысить уровень (без аргумента)
    //
    // Разбор паттерна:
    //   ^/                 — начинается со слеша (литеральный символ, нужно экранировать в regex: /)
    //   ([a-z_]+)          — группа 1: команда (строчные буквы + подчёркивание)
    //                        + — один или более символов (команда не может быть пустой)
    //   (?:\\s+(\\d+))?    — необязательная часть: пробел(ы) + числовой аргумент
    //     (?:...) — группа БЕЗ захвата (не попадёт в group(), не увеличит нумерацию)
    //     \\s+   — один или более пробелов (разделитель между командой и аргументом)
    //     (\\d+) — группа 2: число (одна или более цифр)
    //     ?      — вся эта часть необязательна (команда может быть без аргумента)
    //   $                  — конец строки
    private static final Pattern CHEAT_CODE_PATTERN = Pattern.compile(
            "^/([a-z_]+)(?:\\s+(\\d+))?$"
    );

    // ===== NUMBER_PATTERN — паттерн для извлечения чисел =====
    //
    // \\d+ — одна или более цифр подряд.
    //   \\d — символьный класс "цифра" (эквивалент [0-9])
    //   +  — квантификатор "один или более"
    //
    // Этот паттерн используется с Matcher.find() для поиска ВСЕХ чисел в тексте.
    // Например, в строке "Урон: 42, защита: 15, HP: 100" найдёт: 42, 15, 100.
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

    // =====================================================================
    // ===== validateHeroName() — валидация имени героя (7.4) ==============
    // =====================================================================
    //
    // Проверяет имя героя с помощью скомпилированного Pattern и Matcher.
    // Возвращает строку с результатом валидации:
    //   - если имя корректно: "OK: Имя '<имя>' допустимо (имя: ..., прозвище: ...)"
    //   - если некорректно: "ОШИБКА: ..." с описанием проблемы
    //
    // Демонстрирует:
    //   - pattern.matcher(input) — создание Matcher для строки
    //   - matcher.matches() — проверка ПОЛНОГО соответствия (вся строка должна совпасть)
    //     ВАЖНО: matches() проверяет ВСЮ строку! Для поиска подстроки — find().
    //   - matcher.group(n) — извлечение n-й группы захвата
    //   - Pattern.matches(regex, input) — статический метод для быстрой проверки
    //   - String.matches(regex) — ещё более короткий способ (уже есть в readHeroName)
    //
    // Сравнение трёх способов проверки:
    //
    //   1. String.matches(regex):
    //      name.matches("[a-zA-Z]+");
    //      Плюс: самый короткий код.
    //      Минус: компилирует regex КАЖДЫЙ вызов. Нельзя извлечь группы.
    //
    //   2. Pattern.matches(regex, input):
    //      Pattern.matches("[a-zA-Z]+", name);
    //      Плюс: более явный. Чуть лучше читается.
    //      Минус: тоже компилирует КАЖДЫЙ вызов. Нельзя извлечь группы.
    //
    //   3. Pattern.compile() + Matcher:
    //      Pattern p = Pattern.compile("[a-zA-Z]+");   // один раз
    //      Matcher m = p.matcher(name);                 // для каждой строки
    //      m.matches();  m.group(1);                    // проверка + извлечение
    //      Плюс: компиляция один раз. Можно извлекать группы, позиции, заменять.
    //      Минус: больше кода.
    //
    // РЕКОМЕНДАЦИЯ: для повторного использования — всегда вариант 3.
    public static String validateHeroName(String name) {
        // Проверка на null — защитное программирование.
        // В C/C++ обращение к NULL-указателю — undefined behavior (краш).
        // В Java обращение к null — NullPointerException.
        // Лучше проверить заранее и вернуть понятное сообщение.
        if (name == null) {
            return "ОШИБКА: имя не может быть null";
        }

        // ===== String.matches() — удобный метод (7.4) =====
        //
        // Внутри вызывает Pattern.compile(regex).matcher(this).matches().
        // Удобен для одноразовых проверок. Здесь — для демонстрации.
        //
        // ".*\\d.*" — содержит ли строка хотя бы одну цифру?
        //   .* — любые символы (0 или более)
        //   \\d — одна цифра
        //   .* — любые символы после
        if (name.matches(".*\\d.*")) {
            return "ОШИБКА: имя не должно содержать цифры ('" + name + "')";
        }

        // ===== Pattern.matches() — статический метод (7.4) =====
        //
        // Pattern.matches(regex, input) — эквивалент String.matches(),
        // но вызывается не на строке, а как статический метод класса Pattern.
        // Внутри тоже компилирует regex каждый раз.
        //
        // Здесь проверяем: не пустая ли строка (только пробелы)?
        //   "^\\s*$" — строка, состоящая только из пробельных символов (или пустая).
        //   ^ — начало строки, \\s* — 0+ пробелов, $ — конец строки.
        if (Pattern.matches("^\\s*$", name)) {
            return "ОШИБКА: имя не может быть пустым или состоять только из пробелов";
        }

        // ===== Pattern + Matcher — основной способ (7.4) =====
        //
        // pattern.matcher(input) — создаёт Matcher для данной строки.
        // Matcher — это «курсор», который проходит по строке и ищет совпадения.
        // Один Pattern может создать МНОГО Matcher-ов для разных строк.
        //
        // Аналогия с C++: Pattern ≈ std::regex, Matcher ≈ std::smatch
        Matcher matcher = HERO_NAME_PATTERN.matcher(name);

        // ===== Matcher.matches() — полное совпадение (7.4) =====
        //
        // matches() — проверяет, соответствует ли ВСЯ строка паттерну (от начала до конца).
        // Отличие от find(): find() ищет ПОДСТРОКУ, matches() требует ПОЛНОГО совпадения.
        //
        // Пример:
        //   Pattern p = Pattern.compile("\\d+");
        //   p.matcher("123").matches();   → true  (вся строка — цифры)
        //   p.matcher("abc123").matches(); → false (строка НЕ целиком цифры)
        //   p.matcher("abc123").find();    → true  (ПОДСТРОКА "123" найдена!)
        if (!matcher.matches()) {
            return "ОШИБКА: имя '" + name + "' содержит недопустимые символы. "
                    + "Разрешены: буквы (латиница, кириллица), пробелы, дефисы. "
                    + "Имя должно начинаться с буквы.";
        }

        // ===== Matcher.group(n) — извлечение групп захвата (7.4) =====
        //
        // После успешного matches() или find() можно извлечь группы:
        //   group(0) или group() — ВСЁ совпадение целиком
        //   group(1) — текст, совпавший с первой парой скобок ()
        //   group(2) — текст, совпавший со второй парой скобок ()
        //   и т.д.
        //
        // Если группа не участвовала в совпадении (необязательная группа с ?),
        // group(n) вернёт null.
        //
        // Частая ошибка: вызвать group() ДО find()/matches() или после неудачного find() →
        //   IllegalStateException: No match found
        //
        // Аналогия с C++: std::smatch m; m[1].str() — первая группа.
        String firstName = matcher.group(1);
        String rest = matcher.group(2);

        // ===== StringBuilder для сборки результата (7.3) =====
        StringBuilder result = new StringBuilder("OK: Имя '");
        result.append(name).append("' допустимо");
        result.append(" (имя: ").append(firstName);

        if (rest != null && !rest.isBlank()) {
            // strip() — удаляет пробелы с обоих концов (Java 11+).
            // trim() делает то же, но strip() корректно работает с Unicode-пробелами.
            result.append(", прозвище: ").append(rest.strip());
        }

        result.append(")");
        return result.toString();
    }

    // =====================================================================
    // ===== parseCheatCode() — парсинг чит-кодов (7.4) ====================
    // =====================================================================
    //
    // Разбирает строку чит-кода формата "/команда [число]".
    // Возвращает массив строк:
    //   [0] — "OK" или "ОШИБКА"
    //   [1] — команда (например, "give_gold")
    //   [2] — аргумент (например, "100") или "" если нет
    //
    // Демонстрирует:
    //   - Pattern + Matcher для разбора структурированного ввода
    //   - Группы захвата (numbered groups) для извлечения частей
    //   - Незахватывающие группы (?:...) — для группировки без нумерации
    //   - Pattern.split() — разбиение строки по regex-разделителю
    //   - String.split() — аналог через удобный метод String
    //
    // ===== Pattern.split() vs String.split() (7.4) =====
    //
    // String.split(regex):
    //   "a,b,,c".split(",")    → ["a", "b", "", "c"]
    //   "a,b,,c".split(",", 2) → ["a", "b,,c"] (лимит: макс 2 части)
    //
    // Pattern.split(input):
    //   Pattern p = Pattern.compile(",");
    //   p.split("a,b,,c")      → ["a", "b", "", "c"]
    //
    // Разница: String.split() компилирует regex каждый раз.
    // Pattern.split() использует уже скомпилированный Pattern.
    // Для частого использования — Pattern.split() быстрее.
    public static String[] parseCheatCode(String input) {
        if (input == null || input.isBlank()) {
            return new String[]{"ОШИБКА", "пустой ввод", ""};
        }

        // ===== String.split() с regex — демонстрация (7.4) =====
        //
        // String.split(regex) — разбивает строку по regex-разделителю.
        // Под капотом вызывает Pattern.compile(regex).split(this).
        //
        // "\\s+" — один или более пробельных символов (regex).
        // Используем для демонстрации, хотя основной парсинг — через Pattern+Matcher.
        //
        // Пример:
        //   "/give_gold  100".split("\\s+") → ["/give_gold", "100"]
        //   "  слово  ".split("\\s+")       → ["", "слово"] (пустая строка перед первым пробелом!)
        //
        // Частая ошибка: split("\\s+") на строке, начинающейся с пробелов,
        // даёт пустую строку "" как первый элемент массива!
        String[] parts = input.strip().split("\\s+");

        // ===== Pattern.split() — демонстрация (7.4) =====
        //
        // Тот же результат, но через скомпилированный Pattern.
        // Здесь используем как учебный пример — показываем, что Pattern тоже умеет split.
        Pattern spacePattern = Pattern.compile("\\s+");
        String[] partsByPattern = spacePattern.split(input.strip());

        // partsByPattern и parts дают одинаковый результат.
        // В реальном коде используйте Pattern.split() если паттерн нужен многократно.

        // ===== Основной парсинг через Matcher (7.4) =====
        //
        // Используем CHEAT_CODE_PATTERN с группами захвата.
        // Это надёжнее, чем split: Pattern проверяет ФОРМАТ, а не просто разбивает.
        Matcher matcher = CHEAT_CODE_PATTERN.matcher(input.strip());

        if (!matcher.matches()) {
            // Если строка не соответствует формату чит-кода, проверяем:
            // может, пользователь забыл слеш?
            if (parts.length > 0 && !parts[0].startsWith("/")) {
                return new String[]{"ОШИБКА", "чит-код должен начинаться с /", ""};
            }
            return new String[]{"ОШИБКА", "неверный формат чит-кода", ""};
        }

        // ===== Matcher.group(1), group(2) — извлечение групп (7.4) =====
        //
        // group(1) — первая группа: ([a-z_]+) — команда
        // group(2) — вторая группа: (\\d+) — числовой аргумент (может быть null!)
        //
        // ВАЖНО: незахватывающая группа (?:...) НЕ увеличивает нумерацию!
        // В паттерне "^/([a-z_]+)(?:\\s+(\\d+))?$":
        //   group(1) = ([a-z_]+)  — команда
        //   group(2) = (\\d+)     — число (вторая захватывающая группа, несмотря на (?:...))
        //   (?:\\s+(\\d+)) — незахватывающая обёртка, не считается
        String command = matcher.group(1);
        String argument = matcher.group(2);

        return new String[]{
                "OK",
                command,
                argument != null ? argument : ""
        };
    }

    // =====================================================================
    // ===== censorBadWords() — замена нежелательных слов (7.4) ============
    // =====================================================================
    //
    // Заменяет все вхождения «плохих» слов в тексте на звёздочки (***).
    // Используется для фильтрации чата / имён в многопользовательской RPG.
    //
    // Демонстрирует:
    //   - Динамическое построение regex-паттерна из списка слов
    //   - Pattern.compile() с флагом Pattern.CASE_INSENSITIVE (нечувствительность к регистру)
    //   - Matcher.replaceAll() — замена всех совпадений
    //   - String.replaceAll() — удобный метод (для сравнения)
    //   - Альтернация (|) в regex — «ИЛИ» для нескольких вариантов
    //   - Pattern.quote() — экранирование спецсимволов в пользовательском вводе
    //
    // Параметры:
    //   text  — исходный текст (например, сообщение в чате)
    //   words — список слов для цензуры
    //
    // Возвращает: текст с заменёнными словами.
    public static String censorBadWords(String text, List<String> words) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        if (words == null || words.isEmpty()) {
            return text;
        }

        // ===== Динамическое построение regex через StringBuilder (7.3 + 7.4) =====
        //
        // Строим паттерн вида: "слово1|слово2|слово3"
        // Альтернация (|) в regex означает «ИЛИ»: совпадёт одно ИЗ слов.
        //
        // Pattern.quote(word) — экранирует ВСЕ спецсимволы в строке:
        //   Pattern.quote("a.b") → "\\Qa.b\\E"
        //   Без quote() точка означает «любой символ», а нам нужна ЛИТЕРАЛЬНАЯ точка.
        //   В C++ аналог — std::regex_replace с std::regex_constants::literal.
        //
        // ЧАСТАЯ ОШИБКА: забыть Pattern.quote() при динамических паттернах!
        //   Если слово содержит спецсимволы regex (. * + ? и т.д.),
        //   без quote() они будут интерпретированы как regex-операторы.
        // ===== Фильтрация некорректных элементов списка =====
        //
        // Список words может содержать null или пустые строки "".
        // - Pattern.quote(null) бросит NullPointerException
        // - Pattern.quote("") даст "\\Q\\E" — zero-length match, который совпадает
        //   с ПОЗИЦИЕЙ между каждыми двумя символами. replaceAll("***") вставит "***"
        //   между каждым символом текста — катастрофический результат!
        //
        // Поэтому пропускаем null и пустые строки. Это защитное программирование:
        // метод не падает из-за «мусора» в данных, а корректно его игнорирует.
        StringBuilder patternBuilder = new StringBuilder();
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);

            // Пропускаем null и пустые строки — они не являются «плохими словами».
            if (word == null || word.isEmpty()) {
                continue;
            }

            if (patternBuilder.length() > 0) {
                // | — разделитель альтернации (ИЛИ) в regex.
                patternBuilder.append('|');
            }
            patternBuilder.append(Pattern.quote(word));
        }

        // Если после фильтрации не осталось ни одного валидного слова —
        // возвращаем текст без изменений (нечего цензурировать).
        //
        // StringBuilder.isEmpty() (Java 16+) — проверяет, что length() == 0.
        // До Java 16 нужно было писать: patternBuilder.length() == 0
        // Не путать с String.isEmpty() — у String этот метод есть с Java 6.
        if (patternBuilder.isEmpty()) {
            return text;
        }

        // ===== Pattern.compile() с флагами (7.4) =====
        //
        // Второй аргумент compile() — битовые флаги:
        //   Pattern.CASE_INSENSITIVE — нечувствительность к регистру (a = A)
        //   Pattern.MULTILINE       — ^ и $ работают для каждой строки (а не всего текста)
        //   Pattern.DOTALL           — точка . совпадает и с \n
        //   Pattern.UNICODE_CASE     — Unicode-регистр (для кириллицы ОБЯЗАТЕЛЕН с CASE_INSENSITIVE!)
        //
        // Флаги комбинируются через побитовое ИЛИ: Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        //
        // ВАЖНО для кириллицы: CASE_INSENSITIVE по умолчанию работает только для ASCII!
        // Чтобы "Привет" и "ПРИВЕТ" совпадали, нужен UNICODE_CASE.
        //
        // Аналогия с C++: std::regex_constants::icase
        Pattern badWordsPattern = Pattern.compile(
                patternBuilder.toString(),
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
        );

        // ===== Matcher.replaceAll() — замена всех совпадений (7.4) =====
        //
        // replaceAll(replacement) — заменяет ВСЕ найденные совпадения на replacement.
        // Возвращает НОВУЮ строку (String неизменяем!).
        //
        // Отличие от String.replaceAll():
        //   text.replaceAll(regex, replacement) — компилирует regex каждый вызов
        //   matcher.replaceAll(replacement)     — использует уже скомпилированный Pattern
        //
        // Здесь заменяем каждое «плохое» слово на "***".
        // В реальной системе можно заменять на звёздочки по длине слова:
        //   matcher.replaceAll(m -> "*".repeat(m.group().length()))  // Java 9+
        Matcher matcher = badWordsPattern.matcher(text);
        return matcher.replaceAll("***");
    }

    // =====================================================================
    // ===== highlightPattern() — поиск всех совпадений с позициями (7.4) ==
    // =====================================================================
    //
    // Находит ВСЕ совпадения regex-паттерна в тексте и возвращает список строк
    // с информацией о каждом совпадении: текст, начальная и конечная позиции.
    //
    // Демонстрирует:
    //   - Pattern.compile(regex) — компиляция пользовательского паттерна
    //   - Matcher.find() — поиск СЛЕДУЮЩЕГО совпадения (в цикле while)
    //   - Matcher.group() — текст текущего совпадения
    //   - Matcher.start() — индекс НАЧАЛА совпадения (включительно)
    //   - Matcher.end() — индекс КОНЦА совпадения (исключительно, как в substring)
    //
    // ===== Matcher.find() — итерационный поиск (7.4) =====
    //
    // find() — ищет СЛЕДУЮЩЕЕ совпадение паттерна в строке.
    // Каждый вызов find() перемещает «курсор» Matcher к следующему совпадению.
    // Возвращает true, если совпадение найдено, false — если больше нет.
    //
    // Типичный паттерн использования (цикл поиска):
    //   Matcher m = pattern.matcher(text);
    //   while (m.find()) {
    //       String match = m.group();  // текст совпадения
    //       int start = m.start();     // начальная позиция
    //       int end = m.end();         // конечная позиция (exclusive)
    //   }
    //
    // Аналогия с C++:
    //   std::sregex_iterator it(text.begin(), text.end(), pattern);
    //   std::sregex_iterator end;
    //   while (it != end) { std::smatch match = *it; ++it; }
    //
    // ===== Matcher.start() и Matcher.end() (7.4) =====
    //
    // start() — индекс ПЕРВОГО символа совпадения (inclusive).
    // end()   — индекс символа ПОСЛЕ последнего символа совпадения (exclusive).
    //
    // Это соглашение «inclusive start, exclusive end» используется повсюду в Java:
    //   String.substring(start, end) — те же правила
    //   List.subList(fromIndex, toIndex) — те же правила
    //
    // Пример: в строке "Hello Java World" при поиске "Java":
    //   start() = 6   (индекс 'J')
    //   end()   = 10  (индекс пробела после 'a', т.е. символ с индексом 10)
    //   text.substring(start(), end()) == group()  ← всегда true!
    //
    // Частая ошибка: вызвать start()/end() ДО find() → IllegalStateException.
    public static List<String> highlightPattern(String text, String regex) {
        if (text == null || regex == null || regex.isEmpty()) {
            return List.of();
        }

        // Pattern.compile(regex) — компилируем пользовательский паттерн.
        // ВНИМАНИЕ: если regex невалиден, будет PatternSyntaxException!
        // В реальном приложении нужен try-catch.
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (java.util.regex.PatternSyntaxException e) {
            // PatternSyntaxException — исключение при невалидном regex.
            // Например: "(" — незакрытая скобка, "[" — незакрытый класс.
            List<String> errorResult = new ArrayList<>(1);
            errorResult.add("ОШИБКА: невалидный regex '" + regex + "': " + e.getMessage());
            return errorResult;
        }

        Matcher matcher = pattern.matcher(text);

        // Список результатов. ArrayList с начальной ёмкостью —
        // избегаем лишних расширений массива (см. глава 5.2).
        List<String> results = new ArrayList<>(4);

        // ===== Цикл поиска: while (matcher.find()) (7.4) =====
        //
        // Это КЛЮЧЕВОЙ паттерн работы с Matcher!
        // find() ищет следующее совпадение, group()/start()/end() описывают его.
        //
        // Порядок вызовов СТРОГО: find() → group()/start()/end() → find() → ...
        // Нельзя вызвать group() без предварительного find() (или matches()).
        while (matcher.find()) {
            // group() — текст, совпавший с паттерном.
            // Если в паттерне есть группы (), можно вызвать group(1), group(2), ...
            // group() без аргумента = group(0) = ВСЁ совпадение целиком.
            String match = matcher.group();

            // start() — позиция первого символа совпадения.
            int start = matcher.start();

            // end() — позиция ПОСЛЕ последнего символа совпадения.
            // Длина совпадения: end() - start() == group().length()
            int end = matcher.end();

            // Формируем строку с информацией о совпадении.
            // StringBuilder для эффективной сборки (7.3).
            StringBuilder info = new StringBuilder("Найдено: '");
            info.append(match).append("'");
            info.append(" [позиция ").append(start).append("..").append(end).append(")");
            info.append(" (длина: ").append(end - start).append(")");
            results.add(info.toString());
        }

        if (results.isEmpty()) {
            results.add("Совпадений не найдено для паттерна '" + regex + "'");
        }

        return results;
    }

    // =====================================================================
    // ===== extractNumbers() — извлечение чисел из текста (7.4) ===========
    // =====================================================================
    //
    // Находит ВСЕ числа в тексте и возвращает их как List<Integer>.
    // Например: "Урон 42, защита 15, HP 100" → [42, 15, 100]
    //
    // Демонстрирует:
    //   - Переиспользование скомпилированного Pattern (NUMBER_PATTERN)
    //   - Matcher.find() в цикле — поиск всех вхождений
    //   - Matcher.group() — текст совпадения (число как строка)
    //   - Integer.parseInt() — преобразование строки в число
    //
    // Это полезно для RPG: извлечь числа из строки урона, парсинг лога боя,
    // анализ команд вида "использовать зелье 3".
    public static List<String> extractNumbers(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        // Создаём Matcher из заранее скомпилированного Pattern.
        // NUMBER_PATTERN = Pattern.compile("\\d+") — уже скомпилирован при загрузке класса.
        // Мы НЕ вызываем Pattern.compile() здесь — экономим время!
        Matcher matcher = NUMBER_PATTERN.matcher(text);

        // Список для результатов.
        //
        // ===== Почему List<String>, а не List<Integer>? =====
        //
        // Паттерн \\d+ совпадает с ЛЮБОЙ последовательностью цифр, включая числа,
        // которые превышают Integer.MAX_VALUE (2147483647) и даже Long.MAX_VALUE.
        // Например, текст "урон: 99999999999999" — Integer.parseInt() бросит
        // NumberFormatException, хотя regex нашёл корректную строку цифр.
        //
        // Задача этого метода — НАЙТИ числовые подстроки с помощью regex (find/group),
        // а НЕ парсить их в конкретный числовой тип. Это принцип «разделения
        // ответственности» (Separation of Concerns): поиск отдельно, преобразование
        // типов — отдельно. Вызывающий код сам решает, какой тип использовать:
        //   Integer.parseInt(s)   — для чисел до 2 147 483 647
        //   Long.parseLong(s)     — для чисел до 9 223 372 036 854 775 807
        //   new BigInteger(s)     — для произвольно длинных чисел
        List<String> numbers = new ArrayList<>(4);

        // ===== while (matcher.find()) — стандартный цикл поиска (7.4) =====
        //
        // find() находит следующее вхождение \\d+ (последовательность цифр).
        // group() возвращает найденную последовательность как String.
        //
        // Пример для "HP: 100, MP: 50":
        //   Итерация 1: find() → true, group() → "100", start() → 4, end() → 7
        //   Итерация 2: find() → true, group() → "50",  start() → 13, end() → 15
        //   Итерация 3: find() → false → выход из цикла
        while (matcher.find()) {
            // group() возвращает найденную подстроку цифр как String.
            // Сохраняем как есть — без преобразования в число (см. комментарий выше).
            numbers.add(matcher.group());
        }

        return numbers;
    }

    // =====================================================================
    // ===== demonstrateRegexSyntax() — учебная демонстрация regex (7.4) ===
    // =====================================================================
    //
    // Этот метод НЕ используется в игре — он чисто учебный.
    // Возвращает строку с примерами основных элементов regex-синтаксиса,
    // проверенными на реальных строках.
    //
    // Демонстрирует:
    //   - Различные символьные классы: \\d, \\w, \\s, [abc], [a-z]
    //   - Квантификаторы: *, +, ?, {n}, {n,m}
    //   - Якоря: ^, $
    //   - Группы: (), (?:)
    //   - Альтернация: |
    //   - String.matches(), String.replaceAll(), String.split() — удобные методы
    public static String demonstrateRegexSyntax() {
        StringBuilder demo = new StringBuilder(512);

        demo.append("===== Демонстрация regex-синтаксиса (7.4) =====\n\n");

        // --- Символьные классы ---
        demo.append("--- Символьные классы ---\n");

        // \\d — цифра [0-9]
        demo.append("\\d (цифра): \"abc123\".matches(\".*\\\\d.*\") = ");
        demo.append("abc123".matches(".*\\d.*")).append("\n");

        // \\w — «словесный» символ [a-zA-Z0-9_]
        demo.append("\\w (словесный): \"hello_42\".matches(\"\\\\w+\") = ");
        demo.append("hello_42".matches("\\w+")).append("\n");

        // \\s — пробельный символ
        demo.append("\\s (пробел): \"a b\".matches(\".*\\\\s.*\") = ");
        demo.append("a b".matches(".*\\s.*")).append("\n");

        // [abc] — один из символов
        demo.append("[abc]: \"b\".matches(\"[abc]\") = ");
        demo.append("b".matches("[abc]")).append("\n");

        // [^abc] — НЕ один из символов
        demo.append("[^abc]: \"d\".matches(\"[^abc]\") = ");
        demo.append("d".matches("[^abc]")).append("\n");

        demo.append("\n--- Квантификаторы ---\n");

        // * — 0 или более
        demo.append("* (0+): \"\".matches(\"a*\") = ");
        demo.append("".matches("a*")).append("\n");

        // + — 1 или более
        demo.append("+ (1+): \"\".matches(\"a+\") = ");
        demo.append("".matches("a+")).append(" (пустая строка НЕ подходит!)\n");

        // ? — 0 или 1
        demo.append("? (0 или 1): \"a\".matches(\"a?\") = ");
        demo.append("a".matches("a?")).append("\n");

        // {n} — ровно n
        demo.append("{n}: \"aaa\".matches(\"a{3}\") = ");
        demo.append("aaa".matches("a{3}")).append("\n");

        // {n,m} — от n до m
        demo.append("{n,m}: \"aa\".matches(\"a{2,4}\") = ");
        demo.append("aa".matches("a{2,4}")).append("\n");

        demo.append("\n--- Якоря ---\n");

        // ^ — начало строки
        demo.append("^: \"Hello World\".matches(\"^Hello.*\") = ");
        demo.append("Hello World".matches("^Hello.*")).append("\n");

        // $ — конец строки
        demo.append("$: \"Hello World\".matches(\".*World$\") = ");
        demo.append("Hello World".matches(".*World$")).append("\n");

        demo.append("\n--- Группы и альтернация ---\n");

        // | — ИЛИ
        demo.append("| (ИЛИ): \"кот\".matches(\"кот|пёс\") = ");
        demo.append("кот".matches("кот|пёс")).append("\n");

        // () — группа захвата
        demo.append("(): группы используются для извлечения — см. parseCheatCode()\n");

        demo.append("\n--- Удобные методы String ---\n");

        // ===== String.replaceAll() и replaceFirst() — замена по regex (7.4) =====
        //
        // String.replaceAll(regex, replacement) — заменяет ВСЕ совпадения regex.
        // String.replaceFirst(regex, replacement) — заменяет только ПЕРВОЕ совпадение.
        // Оба метода под капотом вызывают Pattern.compile(regex) каждый раз —
        // для частого использования лучше Pattern + Matcher (уже скомпилированный).
        //
        // КРИТИЧЕСКОЕ ОТЛИЧИЕ: replaceAll() использует REGEX, а replace() — литералы!
        //   "a.b.c".replace(".", "-")    → "a-b-c"  (ищет БУКВАЛЬНУЮ точку)
        //   "a.b.c".replaceAll(".", "-") → "-----"  (точка = ЛЮБОЙ символ в regex!)
        //
        // ПРАВИЛО: если нужна замена буквальных строк — используй replace().
        //          если нужен regex-поиск — используй replaceAll() или Matcher.replaceAll().
        String replaceDemo = "Урон: 42, Защита: 15";
        demo.append("replaceAll: \"").append(replaceDemo).append("\"\n");
        demo.append("  .replaceAll(\"\\\\d+\", \"??\") = \"");
        demo.append(replaceDemo.replaceAll("\\d+", "??")).append("\"\n");

        // String.split() — разбиение по regex
        String splitDemo = "Воин;Маг;;Лучник";
        demo.append("split: \"").append(splitDemo).append("\"\n");
        demo.append("  .split(\";\") = ");
        String[] splitResult = splitDemo.split(";");
        demo.append("[");
        for (int i = 0; i < splitResult.length; i++) {
            if (i > 0) demo.append(", ");
            demo.append("\"").append(splitResult[i]).append("\"");
        }
        demo.append("]  (пустая строка между ;; тоже элемент!)\n");

        return demo.toString();
    }
}
