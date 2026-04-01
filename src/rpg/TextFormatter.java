// ===== ЗАЧЕМ ЭТОТ ФАЙЛ? =====
//
// TextFormatter — утилитный класс для красивого форматирования текста RPG-игры:
//   рамки, таблицы, прогресс-бары (HP/XP), центрирование, выравнивание.
//
// Этот класс демонстрирует работу со строками из главы 7:
//
//   Глава 7.1 — String: НЕИЗМЕНЯЕМОСТЬ (immutability) строк, String pool,
//     оператор == vs метод equals(), length(), toCharArray(), isEmpty(), isBlank(), null-проверки.
//
//   Глава 7.3 — StringBuilder: ИЗМЕНЯЕМАЯ строка для эффективной сборки текста.
//     Конструкторы, capacity(), ensureCapacity(), append(), insert(), delete(),
//     deleteCharAt(), replace(), reverse(), charAt(), setCharAt(), substring(),
//     setLength(), toString().
//
//   Также: String.repeat() (Java 11+), String.substring() для обрезки/паддинга.
//
// ===== НЕИЗМЕНЯЕМОСТЬ СТРОК В JAVA (глава 7.1) =====
//
// Строки в Java — НЕИЗМЕНЯЕМЫЕ (immutable). Каждая «модификация» строки на самом деле
// создаёт НОВЫЙ объект String, а старый остаётся без изменений.
//
// Пример:
//   String s = "Привет";   // создаётся объект "Привет" в String pool
//   s = s + " мир";        // создаётся НОВЫЙ объект "Привет мир", старый "Привет" остаётся
//   // Теперь s указывает на "Привет мир", а "Привет" остаётся в памяти (пока GC не соберёт).
//
// ПОЧЕМУ ТАК?
//   1. Безопасность: строки используются как ключи HashMap, в пулах потоков, в Security-контекстах.
//      Если бы строку можно было изменить, это сломало бы хэш-коды, кеши и безопасность.
//   2. Потокобезопасность: неизменяемые объекты можно безопасно передавать между потоками
//      без синхронизации.
//   3. Кеширование: JVM может использовать один объект для одинаковых строковых литералов
//      (String pool) — экономия памяти.
//
// ===== STRING POOL (пул строк) =====
//
// JVM хранит строковые ЛИТЕРАЛЫ (записанные в кавычках в коде) в специальной области памяти —
// String pool (строковый пул). Если два литерала одинаковы, JVM использует ОДИН объект.
//
//   String a = "Java";     // литерал → помещается в пул
//   String b = "Java";     // тот же литерал → берётся ИЗ пула
//   System.out.println(a == b);      // true — ссылки на ОДИН объект в пуле
//
//   String c = new String("Java");   // new — ВСЕГДА создаёт новый объект в куче (heap)
//   System.out.println(a == c);      // false — разные объекты (пул и куча)
//   System.out.println(a.equals(c)); // true — СОДЕРЖИМОЕ одинаковое
//
// ПРАВИЛО: для сравнения строк ВСЕГДА используй equals(), НИКОГДА == !
//   == сравнивает ССЫЛКИ (адреса в памяти), а equals() — СОДЕРЖИМОЕ.
//
// АНАЛОГИЯ С C/C++:
//   В C строка — это char[] с '\0' в конце. strcmp() сравнивает содержимое, а == — указатели.
//   В Java equals() — это аналог strcmp(), а == — аналог сравнения указателей.
//   Но в Java строки — объекты с методами, а не просто массивы символов.
//
// ===== ЗАЧЕМ StringBuilder? (глава 7.3) =====
//
// Если склеивать строки оператором + в цикле, каждая итерация создаёт НОВЫЙ объект String:
//
//   String result = "";
//   for (int i = 0; i < 1000; i++) {
//       result += "x";  // На КАЖДОЙ итерации: выделение памяти, копирование всех предыдущих символов
//   }
//   // Это O(n²) по времени и памяти! 1000 итераций создадут ~1000 промежуточных объектов.
//
// StringBuilder решает эту проблему — он ИЗМЕНЯЕМЫЙ:
//
//   StringBuilder sb = new StringBuilder();
//   for (int i = 0; i < 1000; i++) {
//       sb.append("x");  // Добавляет символы в существующий буфер, без создания новых объектов
//   }
//   String result = sb.toString();
//   // Это O(n) — один объект, растущий по мере надобности.
//
// КОГДА ЧТО ИСПОЛЬЗОВАТЬ:
//   - Простая склейка (2–3 строки): "Hello" + name + "!" — компилятор сам оптимизирует.
//   - Цикл или много частей: StringBuilder — обязательно.
//   - Многопоточность: StringBuffer (синхронизированный аналог, но медленнее).
//
// АНАЛОГИЯ С C++:
//   StringBuilder похож на std::ostringstream или std::string с reserve() и +=.
//   Оба подхода позволяют наращивать строку без постоянного перевыделения памяти.

// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== ИМПОРТ java.util.List (глава 5.1) =====
//
// List<T> — упорядоченная коллекция элементов с доступом по индексу.
// Здесь используется для передачи списков строк и строковых массивов
// в методы buildTable() и buildFrame().
import java.util.List;

// ===== final class — ЗАПРЕТ НАСЛЕДОВАНИЯ (глава 3.23) =====
//
// Модификатор final перед class означает: от этого класса НЕЛЬЗЯ наследоваться.
//   public final class TextFormatter { }
//   class MyFormatter extends TextFormatter { } // ОШИБКА КОМПИЛЯЦИИ!
//
// ЗАЧЕМ final для утилитного класса?
//   1. Утилитный класс — это набор static-методов (как Math, Collections, Arrays в Java).
//      Создавать экземпляры не нужно, наследоваться — тем более.
//   2. final говорит «этот класс завершён, не предназначен для расширения».
//   3. JVM может оптимизировать вызовы методов final-классов (девиртуализация).
//
// АНАЛОГИЯ С C++: final class в Java ≈ final class в C++11:
//   class MyClass final { };  // C++11 — запрет наследования
public final class TextFormatter {

    // ===== ПРИВАТНЫЙ КОНСТРУКТОР — ЗАПРЕТ СОЗДАНИЯ ЭКЗЕМПЛЯРОВ =====
    //
    // Если все методы класса — static, создавать объекты бессмысленно.
    // Приватный конструктор гарантирует, что никто не напишет:
    //   TextFormatter tf = new TextFormatter(); // ОШИБКА — конструктор private
    //
    // Это стандартный паттерн для утилитных классов в Java:
    //   java.lang.Math — приватный конструктор, все методы static.
    //   java.util.Collections — то же самое.
    //   java.util.Arrays — то же самое.
    //
    // БЕЗ приватного конструктора Java автоматически создаёт public конструктор по умолчанию,
    // и кто-то может случайно написать new TextFormatter() — бесполезный объект.
    private TextFormatter() {
    }

    // ===== centerText() — ЦЕНТРИРОВАНИЕ СТРОКИ =====
    //
    // Размещает текст по центру в поле заданной ширины, добавляя пробелы слева и справа.
    // Пример: centerText("HP", 10) → "    HP    "
    //
    // Демонстрирует:
    //   - length() (7.1) — длина строки для вычисления отступов
    //   - repeat() (String, Java 11+) — повторение пробела нужное число раз
    //   - substring() (7.1) — обрезка, если текст длиннее ширины
    //
    // ===== String.repeat(int count) =====
    //
    // Метод repeat() возвращает строку, повторённую count раз:
    //   "=".repeat(5) → "====="
    //   " ".repeat(3) → "   "
    //   "ab".repeat(3) → "ababab"
    //   "x".repeat(0) → "" (пустая строка)
    //
    // repeat() появился в Java 11. До этого приходилось писать цикл:
    //   StringBuilder sb = new StringBuilder();
    //   for (int i = 0; i < 5; i++) sb.append("=");
    //
    // АНАЛОГИЯ С C++: нет прямого аналога в std::string, но можно:
    //   std::string(5, '=') → "=====" (только для одного символа)
    //
    // ===== null-проверки (7.1) =====
    //
    // В Java строковая переменная может быть null — не указывать ни на какой объект.
    // Вызов ЛЮБОГО метода на null приведёт к NullPointerException:
    //   String s = null;
    //   s.length(); // NullPointerException!
    //
    // Поэтому в каждом публичном методе мы проверяем аргументы на null.
    // Это защитное программирование (defensive programming).
    public static String centerText(String text, int width) {
        // Проверка на null — без неё вызов text.length() ниже выбросит NullPointerException.
        if (text == null) {
            text = "";
        }

        // Если ширина некорректна, возвращаем текст как есть.
        if (width <= 0) {
            return text;
        }

        // ===== length() (7.1) — количество СИМВОЛОВ (char) в строке =====
        //
        // length() возвращает число 16-битных char-единиц (UTF-16 code units).
        // Для латиницы и кириллицы один символ = один char, поэтому length() == количество символов.
        // Но для эмодзи или иероглифов один «видимый» символ может занимать 2 char (суррогатная пара).
        //
        // АНАЛОГИЯ С C: strlen() в C считает байты до '\0'. В Java length() считает char-единицы.
        //   В C strlen("Привет") вернёт 12 (6 символов × 2 байта в UTF-8).
        //   В Java "Привет".length() вернёт 6 (6 символов, каждый = 1 char в UTF-16).
        if (text.length() >= width) {
            // ===== substring(0, width) — ОБРЕЗКА СТРОКИ =====
            //
            // substring(beginIndex, endIndex) возвращает НОВУЮ строку от beginIndex до endIndex-1.
            // Оригинал НЕ изменяется (строки неизменяемы!).
            //   "Привет".substring(0, 3) → "При" (символы с индексами 0, 1, 2)
            //   "Привет".substring(3)    → "вет" (от индекса 3 до конца)
            //
            // ВНИМАНИЕ: если endIndex > length(), будет StringIndexOutOfBoundsException!
            return text.substring(0, width);
        }

        // Вычисляем количество пробелов слева (половина свободного пространства).
        int totalPadding = width - text.length();
        int leftPadding = totalPadding / 2;
        int rightPadding = totalPadding - leftPadding;

        // ===== repeat() — повторение строки =====
        // " ".repeat(leftPadding) создаёт строку из leftPadding пробелов.
        // Затем конкатенация (+) собирает результат.
        // Здесь + допустим, так как склейка однократная (не в цикле).
        return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
    }

    // ===== padRight() — ВЫРАВНИВАНИЕ ПО ЛЕВОМУ КРАЮ (дополнение пробелами справа) =====
    //
    // Пример: padRight("HP", 10) → "HP        "
    // Используется для колонок таблиц, списков инвентаря и т.п.
    //
    // Демонстрирует: length(), repeat(), substring() — те же приёмы, что и centerText.
    public static String padRight(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (width <= 0) {
            return text;
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        // Дополняем пробелами справа до нужной ширины.
        return text + " ".repeat(width - text.length());
    }

    // ===== padLeft() — ВЫРАВНИВАНИЕ ПО ПРАВОМУ КРАЮ (дополнение пробелами слева) =====
    //
    // Пример: padLeft("42", 6) → "    42"
    // Используется для числовых колонок (урон, здоровье, опыт) — числа выравнивают вправо.
    public static String padLeft(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (width <= 0) {
            return text;
        }
        if (text.length() >= width) {
            return text.substring(0, width);
        }
        // Пробелы СЛЕВА + текст.
        return " ".repeat(width - text.length()) + text;
    }

    // ===== truncate() — ОБРЕЗКА СТРОКИ С МНОГОТОЧИЕМ =====
    //
    // Если строка длиннее maxLen, обрезает её и добавляет "..." в конце.
    // Пример: truncate("Длинное описание предмета", 15) → "Длинное описа..."
    //
    // Зачем: предотвращает «поломку» форматирования при выводе слишком длинных имён
    // врагов, предметов или описаний. Текст вписывается в отведённое пространство.
    //
    // Демонстрирует: length(), substring().
    public static String truncate(String text, int maxLen) {
        if (text == null) {
            return "";
        }
        // Минимальная длина 4: хотя бы 1 символ + "..."
        if (maxLen < 4) {
            return text.length() <= maxLen ? text : text.substring(0, maxLen);
        }
        if (text.length() <= maxLen) {
            return text;
        }
        // Обрезаем и добавляем многоточие.
        // substring(0, maxLen - 3) — оставляем место для "..." (3 символа).
        return text.substring(0, maxLen - 3) + "...";
    }

    // ===== buildProgressBar() — ПОЛОСКА ПРОГРЕССА (HP, XP, Мана) =====
    //
    // Создаёт визуальную полоску:  [████████░░░░░░░░░░░░] 80/100
    // Заполненная часть = current/max * width, остаток — пустые клетки.
    //
    // Пример: buildProgressBar(80, 100, 20) → "[████████████████░░░░] 80/100"
    //
    // Демонстрирует ВСЕ ключевые методы StringBuilder (7.3):
    //
    // ===== КОНСТРУКТОРЫ StringBuilder (7.3) =====
    //
    //   new StringBuilder()          — пустой, начальная ёмкость 16 символов.
    //   new StringBuilder(int cap)   — пустой, начальная ёмкость cap символов.
    //   new StringBuilder(String s)  — копия строки s, ёмкость = s.length() + 16.
    //   new StringBuilder(CharSequence cs) — копия CharSequence.
    //
    // ===== capacity() и ensureCapacity() (7.3) =====
    //
    // capacity() — текущая ЁМКОСТЬ буфера (сколько символов помещается БЕЗ перераспределения).
    // length()   — текущая ДЛИНА (сколько символов реально в строке).
    // Ёмкость ≥ длина. Когда длина превышает ёмкость, StringBuilder автоматически
    // увеличивает буфер (обычно удваивает + 2).
    //
    //   StringBuilder sb = new StringBuilder(); // capacity = 16, length = 0
    //   sb.append("Java");                      // capacity = 16, length = 4
    //   sb.append("x".repeat(20));              // capacity = 34 (автоматически увеличилось), length = 24
    //
    // ensureCapacity(int min) — ГАРАНТИРУЕТ, что ёмкость будет не менее min.
    //   Если текущая ёмкость уже ≥ min, ничего не происходит.
    //   Если нет — буфер увеличивается (обычно до max(min, старая*2+2)).
    //
    // ЗАЧЕМ? Если мы заранее знаем итоговый размер, одна предварительная аллокация
    //   лучше, чем несколько автоматических увеличений.
    //
    // АНАЛОГИЯ С C++:
    //   capacity() ≈ std::string::capacity()
    //   ensureCapacity() ≈ std::string::reserve()
    //   length() ≈ std::string::size()
    //
    // ===== setCharAt(int index, char ch) (7.3) =====
    //
    // Заменяет символ в StringBuilder по индексу. В отличие от String (неизменяемый),
    // StringBuilder позволяет менять символы «на месте» — без создания нового объекта.
    //
    //   StringBuilder sb = new StringBuilder("Java");
    //   sb.setCharAt(0, 'j');  // "java" — заменили 'J' на 'j'
    //
    // Если index < 0 или index >= length() — StringIndexOutOfBoundsException.
    //
    // АНАЛОГИЯ С C: прямое присваивание char str[0] = 'j'; — меняем символ в массиве.
    //   В Java String так нельзя (неизменяемый), но StringBuilder — можно.
    public static String buildProgressBar(int current, int max, int width) {
        // Защита от некорректных значений.
        if (max <= 0) {
            max = 1;
        }
        if (current < 0) {
            current = 0;
        }
        if (current > max) {
            current = max;
        }
        if (width < 3) {
            width = 3;
        }

        // ===== new StringBuilder(int capacity) — конструктор с указанием ёмкости (7.3) =====
        //
        // Мы заранее знаем примерный размер результата:
        //   "[" (1) + полоска (width) + "] " (2) + "текущее/макс" (~7) ≈ width + 10.
        // Указав ёмкость заранее, избегаем перераспределения буфера.
        StringBuilder sb = new StringBuilder(width + 15);

        // ===== ensureCapacity() — демонстрация (7.3) =====
        //
        // Здесь ensureCapacity() избыточен (мы уже указали ёмкость в конструкторе),
        // но демонстрируем метод для учебных целей.
        // ensureCapacity гарантирует, что буфер вмещает не менее указанного числа символов.
        sb.ensureCapacity(width + 15);

        // Вычисляем, сколько клеток закрашено.
        int filled = (int) ((double) current / max * width);

        // ===== append() — добавление в конец StringBuilder (7.3) =====
        //
        // append() — самый частый метод StringBuilder. Добавляет данные в конец буфера.
        // Перегружен для ВСЕХ типов: String, char, int, long, double, boolean, Object и т.д.
        //
        //   sb.append("hello");   // добавить строку
        //   sb.append(' ');       // добавить символ
        //   sb.append(42);        // добавить число (преобразуется в "42")
        //   sb.append(true);      // добавить boolean (преобразуется в "true")
        //
        // append() возвращает ссылку на тот же StringBuilder → можно цепочкой:
        //   sb.append("a").append("b").append("c"); // "abc"
        //
        // АНАЛОГИЯ С C++: оператор << для std::ostringstream или += для std::string.
        sb.append('[');

        // Заполняем полоску символами '█' (заполнено) и '░' (пусто).
        for (int i = 0; i < width; i++) {
            sb.append(i < filled ? '█' : '░');
        }

        sb.append(']');

        // ===== charAt(int index) (7.3) =====
        //
        // Возвращает символ по индексу (0-based) из StringBuilder.
        // Аналог String.charAt(), но для изменяемого буфера.
        //
        //   StringBuilder sb = new StringBuilder("Java");
        //   char first = sb.charAt(0);  // 'J'
        //   char last = sb.charAt(3);   // 'a'
        //
        // Если index < 0 или index >= length() — StringIndexOutOfBoundsException.
        //
        // АНАЛОГИЯ С C: str[index] — доступ к элементу массива по индексу.

        // Демонстрируем setCharAt: если здоровье критически низкое (< 20%),
        // заменяем первый символ полоски на '!' как индикатор опасности.
        if (current > 0 && (double) current / max < 0.2) {
            // setCharAt(1, '!') — индекс 1, потому что индекс 0 = '['.
            sb.setCharAt(1, '!');
        }

        // Добавляем числовое значение: " 80/100"
        sb.append(' ').append(current).append('/').append(max);

        // ===== toString() — преобразование StringBuilder в String (7.3) =====
        //
        // toString() создаёт НОВУЮ неизменяемую строку (String) из содержимого StringBuilder.
        // После вызова toString() StringBuilder можно продолжать использовать — он не изменяется.
        //
        //   StringBuilder sb = new StringBuilder("hello");
        //   String s = sb.toString();  // "hello" — новый объект String
        //   sb.append(" world");       // sb = "hello world", s по-прежнему = "hello"
        return sb.toString();
    }

    // ===== buildFrame() — РАМКА С ЗАГОЛОВКОМ =====
    //
    // Создаёт декоративную рамку вокруг текста:
    //   ╔══════════════════╗
    //   ║   Инвентарь      ║
    //   ╠══════════════════╣
    //   ║ Меч              ║
    //   ║ Щит              ║
    //   ╚══════════════════╝
    //
    // Демонстрирует:
    //   - StringBuilder с предварительным расчётом ёмкости (7.3)
    //   - append() для сборки многострочного текста (7.3)
    //   - repeat() для рисования горизонтальных линий
    //   - insert() для вставки символов (7.3)
    //   - delete() для удаления лишних символов (7.3)
    //
    // ===== insert(int offset, String/char/...) (7.3) =====
    //
    // Вставляет строку или символ в StringBuilder по указанному индексу.
    // Все символы начиная с offset сдвигаются вправо.
    //
    //   StringBuilder sb = new StringBuilder("word");
    //   sb.insert(0, "s");   // "sword" — вставили "s" в начало
    //   sb.insert(5, "!");   // "sword!" — вставили "!" в конец
    //   sb.insert(1, "w");   // "swword!" — вставили "w" на позицию 1
    //
    // insert() перегружен для всех типов (String, char, int, double и т.д.),
    // как и append().
    //
    // ВНИМАНИЕ: insert() в середину длинной строки медленнее, чем append() в конец,
    //   потому что нужно сдвигать все символы после точки вставки.
    //   Это O(n), где n — количество символов после точки вставки.
    //
    // ===== delete(int start, int end) (7.3) =====
    //
    // Удаляет символы из StringBuilder от start (включительно) до end (не включая).
    //
    //   StringBuilder sb = new StringBuilder("assembler");
    //   sb.delete(0, 2);  // "sembler" — удалили символы с индексами 0 и 1
    //
    // deleteCharAt(int index) — удаляет ОДИН символ по индексу:
    //   sb.deleteCharAt(6);  // "semble" — удалили символ с индексом 6
    //
    // Как и insert(), delete() сдвигает символы — O(n) в худшем случае.
    //
    // ===== replace(int start, int end, String str) — ЗАМЕНА В StringBuilder (7.3) =====
    //
    // Заменяет символы от start до end на строку str.
    // Длина замены может отличаться от длины заменяемого участка.
    //
    //   StringBuilder sb = new StringBuilder("hello world!");
    //   sb.replace(6, 11, "java");  // "hello java!" — заменили "world" (5 символов) на "java" (4 символа)
    //
    // ВАЖНО: replace() в StringBuilder работает ПО ИНДЕКСАМ, а не по подстроке (как String.replace).
    //   String.replace("world", "java")     — ищет подстроку "world"
    //   StringBuilder.replace(6, 11, "java") — заменяет символы с 6 по 10 (индексы)
    public static String buildFrame(String title, List<String> lines) {
        if (title == null) {
            title = "";
        }

        // Определяем ширину рамки: максимум из длины заголовка и всех строк + отступы.
        int contentWidth = title.length() + 4;
        if (lines != null) {
            for (String line : lines) {
                if (line != null && line.length() + 4 > contentWidth) {
                    contentWidth = line.length() + 4;
                }
            }
        }

        // ===== Предварительный расчёт ёмкости StringBuilder =====
        //
        // Оцениваем итоговый размер: каждая строка рамки ≈ contentWidth + 4 (рамочные символы + \n).
        // Количество строк: 3 (верх + заголовок + разделитель) + количество строк + 1 (низ).
        int lineCount = (lines != null ? lines.size() : 0) + 4;
        int estimatedSize = lineCount * (contentWidth + 4);

        // new StringBuilder(estimatedSize) — создаём с запасом, чтобы не перераспределять буфер.
        StringBuilder sb = new StringBuilder(estimatedSize);

        // Ширина внутренней части (между рамочными символами ║...║).
        int innerWidth = contentWidth;
        String horizontalLine = "═".repeat(innerWidth);

        // Верхняя рамка: ╔════════╗
        sb.append('╔').append(horizontalLine).append('╗').append('\n');

        // Заголовок: ║  Текст  ║ (центрирован)
        sb.append('║').append(centerText(title, innerWidth)).append('║').append('\n');

        // Разделитель: ╠════════╣
        sb.append('╠').append(horizontalLine).append('╣').append('\n');

        // Строки содержимого.
        if (lines != null) {
            for (String line : lines) {
                if (line == null) {
                    line = "";
                }
                // ===== insert() — демонстрация вставки (7.3) =====
                //
                // Вместо простого append мы сначала добавляем содержимое строки,
                // а затем используем insert() для вставки начального символа рамки.
                // Это менее эффективно, чем прямой append, но демонстрирует метод insert().
                int insertPos = sb.length();
                sb.append(padRight(line, innerWidth)).append('║').append('\n');
                sb.insert(insertPos, '║');
            }
        }

        // ===== delete() — демонстрация удаления (7.3) =====
        //
        // Если последний символ — лишний перевод строки после содержимого,
        // мы могли бы его удалить так:
        //   sb.delete(sb.length() - 1, sb.length());  // удалить последний символ
        //   sb.deleteCharAt(sb.length() - 1);          // то же самое, но для одного символа
        //
        // В нашем случае не удаляем — нижняя рамка идёт сразу после.

        // Нижняя рамка: ╚════════╝
        sb.append('╚').append(horizontalLine).append('╝');

        return sb.toString();
    }

    // ===== buildTable() — ТАБЛИЦА С КОЛОНКАМИ =====
    //
    // Строит текстовую таблицу из списка строк (каждая строка = массив ячеек).
    // Первая строка считается заголовком.
    //
    // Пример:
    //   buildTable(List.of(
    //       new String[]{"Имя", "HP", "ATK"},
    //       new String[]{"Воин", "100", "25"},
    //       new String[]{"Маг", "60", "40"}
    //   ))
    //   Результат:
    //   | Имя  | HP  | ATK |
    //   |------|-----|-----|
    //   | Воин | 100 | 25  |
    //   | Маг  | 60  | 40  |
    //
    // Демонстрирует (глава 7.3):
    //   - new StringBuilder(int) — предварительное выделение ёмкости
    //   - append() в двойном цикле — главный паттерн сборки таблицы
    //   - setLength(int) — обрезка завершающего '\n' (удаление trailing separator)
    //   - "repeat()" для строки разделителя |------|
    public static String buildTable(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }

        // Шаг 1: определяем количество колонок и максимальную ширину каждой.
        int cols = 0;
        for (String[] row : rows) {
            if (row != null && row.length > cols) {
                cols = row.length;
            }
        }
        if (cols == 0) {
            return "";
        }

        int[] colWidths = new int[cols];
        for (String[] row : rows) {
            if (row == null) {
                continue;
            }
            for (int i = 0; i < row.length; i++) {
                if (row[i] != null && row[i].length() > colWidths[i]) {
                    colWidths[i] = row[i].length();
                }
            }
        }

        // Шаг 2: строим таблицу через StringBuilder.
        // Оцениваем размер: строк × (сумма ширин + разделители + переводы строки).
        int totalWidth = 0;
        for (int w : colWidths) {
            totalWidth += w + 3;
        }
        totalWidth += 2;

        StringBuilder sb = new StringBuilder(rows.size() * totalWidth * 2);

        // Шаг 3: вывод строк.
        boolean isFirstRow = true;
        for (String[] row : rows) {
            // Формируем строку таблицы: | ячейка1 | ячейка2 | ...
            sb.append('|');
            for (int i = 0; i < cols; i++) {
                String cell = (row != null && i < row.length && row[i] != null) ? row[i] : "";
                sb.append(' ').append(padRight(cell, colWidths[i])).append(" |");
            }
            sb.append('\n');

            // После первой строки (заголовка) добавляем разделитель: |------|-----|
            if (isFirstRow) {
                sb.append('|');
                for (int i = 0; i < cols; i++) {
                    // ===== replace() — демонстрация замены в StringBuilder (7.3) =====
                    //
                    // Альтернативный способ построить разделитель: сначала заполнить пробелами,
                    // потом заменить на дефисы. Но проще сразу использовать repeat().
                    // Демонстрируем replace() ниже при scrambleText().
                    sb.append('-').append("-".repeat(colWidths[i])).append("-|");
                }
                sb.append('\n');
                isFirstRow = false;
            }
        }

        // ===== setLength() — ИЗМЕНЕНИЕ ДЛИНЫ StringBuilder (7.3) =====
        //
        // setLength(int newLength) устанавливает длину StringBuilder:
        //   - Если newLength < текущей длины: строка ОБРЕЗАЕТСЯ (символы после newLength удаляются).
        //   - Если newLength > текущей длины: строка ДОПОЛНЯЕТСЯ нулевыми символами ('\0').
        //
        //   StringBuilder sb = new StringBuilder("hello world");
        //   sb.setLength(5);  // "hello" — обрезали
        //   sb.setLength(10); // "hello\0\0\0\0\0" — дополнили нулевыми символами
        //
        // АНАЛОГИЯ С C++: std::string::resize(n) — то же поведение.
        //
        // Здесь используем setLength для удаления завершающего '\n', если он есть.
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    // ===== scrambleText() — «ЗАШИФРОВАННЫЙ» ТЕКСТ =====
    //
    // Перемешивает символы строки, создавая эффект «шифрованного» или «нечитаемого» текста.
    // Используется как игровой эффект: зашифрованные свитки, заклинания, скрытые послания.
    //
    // Пример: scrambleText("Привет мир") → "мвеПиртр и" (псевдослучайный порядок)
    //
    // Демонстрирует:
    //   - toCharArray() (7.1) — преобразование строки в массив символов
    //   - charAt() (7.1) — доступ к символу по индексу
    //   - new String(char[]) — создание строки из массива символов
    //
    // ===== toCharArray() (7.1) =====
    //
    // toCharArray() возвращает КОПИЮ содержимого строки как массив char[].
    // Исходная строка НЕ изменяется (она неизменяемая!).
    //
    //   String s = "Java";
    //   char[] chars = s.toCharArray();  // {'J', 'a', 'v', 'a'}
    //   chars[0] = 'j';                  // chars = {'j', 'a', 'v', 'a'}, s по-прежнему "Java"
    //
    // ЗАЧЕМ?
    //   1. Посимвольная обработка в цикле — часто удобнее массива, чем charAt() каждый раз.
    //   2. Алгоритмы (сортировка символов, перемешивание, шифрование).
    //   3. Создание новой строки из модифицированного массива: new String(chars).
    //
    // АНАЛОГИЯ С C: строка в C — это УЖЕ массив символов (char[]).
    //   toCharArray() как бы «извлекает» внутренний массив, чтобы работать как в C.
    //
    // ===== charAt(int index) (7.1) =====
    //
    // Возвращает символ строки по индексу (0-based).
    //   "Java".charAt(0) → 'J'
    //   "Java".charAt(3) → 'a'
    //
    // Если index < 0 или index >= length() — StringIndexOutOfBoundsException.
    //
    // АНАЛОГИЯ С C: str[index] — но в Java строка не массив, поэтому используется метод.
    //
    // ===== isEmpty() и isBlank() (7.1) =====
    //
    // isEmpty() — true, если строка пустая (length() == 0).
    //   "".isEmpty()   → true
    //   " ".isEmpty()  → false (пробел — это символ!)
    //   "ab".isEmpty() → false
    //
    // isBlank() (Java 11+) — true, если строка пустая ИЛИ содержит только пробельные символы.
    //   "".isBlank()    → true
    //   " ".isBlank()   → true  (только пробелы)
    //   "\t\n".isBlank()→ true  (табуляция + перевод строки — тоже пробельные)
    //   "ab".isBlank()  → false
    //
    // ЧАСТАЯ ОШИБКА: путать isEmpty() и isBlank().
    //   " ".isEmpty() = false, но " ".isBlank() = true!
    //   Для проверки пользовательского ввода обычно нужен isBlank(), а не isEmpty().
    public static String scrambleText(String text) {
        // Проверка на null и пустую строку.
        if (text == null || text.isEmpty()) {
            return "";
        }

        // toCharArray() — получаем КОПИЮ символов строки как массив.
        char[] chars = text.toCharArray();

        // Простой алгоритм «шифрования»: меняем местами соседние пары символов.
        // "Привет" → "рПвиетм " (каждая пара меняется местами)
        for (int i = 0; i < chars.length - 1; i += 2) {
            // charAt() в String и прямой доступ chars[i] в массиве — один и тот же символ.
            // Здесь используем массив напрямую, что быстрее, чем text.charAt(i).
            char temp = chars[i];
            chars[i] = chars[i + 1];
            chars[i + 1] = temp;
        }

        // ===== new String(char[]) — создание строки из массива символов (7.1) =====
        //
        // Конструктор String(char[]) создаёт НОВУЮ строку, КОПИРУЯ содержимое массива.
        // Дальнейшие изменения массива НЕ повлияют на строку (и наоборот).
        //
        //   char[] arr = {'J', 'a', 'v', 'a'};
        //   String s = new String(arr);  // "Java"
        //   arr[0] = 'j';               // s по-прежнему "Java" (копия!)
        //
        // Есть также конструктор String(char[], int offset, int count):
        //   new String(arr, 1, 3) → "ava" — 3 символа начиная с индекса 1.
        return new String(chars);
    }

    // ===== reverse() — ДЕМОНСТРАЦИЯ РАЗВОРОТА СТРОКИ (7.3) =====
    //
    // StringBuilder.reverse() разворачивает содержимое «на месте» (in-place),
    // меняя порядок символов на обратный.
    //
    //   StringBuilder sb = new StringBuilder("hello");
    //   sb.reverse();  // sb = "olleh"
    //
    // reverse() модифицирует САМ StringBuilder и возвращает ссылку на него (для цепочки).
    //
    // ЗАЧЕМ В RPG? Зеркальные заклинания, палиндромные загадки, декоративные эффекты.
    //
    // ВАЖНО: reverse() корректно обрабатывает суррогатные пары (emoji, редкие символы) —
    //   пара (high + low surrogate) разворачивается как единое целое, а не побуквенно.
    //
    // АНАЛОГИЯ С C++: std::reverse(str.begin(), str.end()) для std::string.
    //   В Java аналога для String нет (он неизменяемый), поэтому нужен StringBuilder.
    //
    // ===== substring(int start) и substring(int start, int end) в StringBuilder (7.3) =====
    //
    // StringBuilder тоже имеет метод substring(), аналогичный String.substring():
    //   StringBuilder sb = new StringBuilder("hello java!");
    //   String s1 = sb.substring(6);     // "java!"
    //   String s2 = sb.substring(3, 9);  // "lo jav"
    //
    // ВАЖНО: substring() возвращает String (неизменяемый), а не StringBuilder.
    //   Сам StringBuilder не изменяется.
    public static String reverseText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // Создаём StringBuilder из строки и разворачиваем.
        // substring(0) здесь используется только для демонстрации метода —
        // он возвращает копию всего содержимого как String.
        StringBuilder sb = new StringBuilder(text);
        sb.reverse();
        return sb.substring(0);
    }
}
