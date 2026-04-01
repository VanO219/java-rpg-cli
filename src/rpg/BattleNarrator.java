// ===== ЗАЧЕМ ЭТОТ ФАЙЛ? =====
//
// BattleNarrator — генератор красочных боевых описаний для RPG-игры.
// Вместо сухих сообщений вроде "Артас: 42 урона" генерирует описания:
//   "⚔ Воин Артас обрушивает удар на Гоблина, нанося 42 урона!"
//
// Этот класс демонстрирует работу с классами StringBuilder и StringBuffer
// из главы 7.3, а также строковые операции из главы 7.2:
//   - StringBuilder (7.3): append(), insert(), delete(), deleteCharAt(),
//     replace(), reverse(), setLength(), toString()
//   - StringBuffer (7.3): потокобезопасный аналог StringBuilder
//   - String (7.2): replace(), toUpperCase(), substring(), compareTo(), getChars()
//
// ===== StringBuilder vs String (7.3) =====
//
// Проблема String: строки в Java НЕИЗМЕНЯЕМЫ (immutable).
// Каждая операция + создаёт НОВЫЙ объект String:
//
//   String result = "";
//   result += "Привет";   // → новый String("Привет")
//   result += " мир";     // → новый String("Привет мир") — предыдущий отброшен!
//   result += "!";        // → новый String("Привет мир!") — предыдущий отброшен!
//
// Итого: 3 конкатенации = 3 промежуточных объекта String в памяти (потом уберёт GC).
// В цикле с 1000 итераций — 1000 лишних объектов! Это O(n²) по памяти.
//
// Решение: StringBuilder — изменяемая (mutable) строка.
// Внутри — массив char[], который расширяется по мере необходимости.
// Все операции ИЗМЕНЯЮТ тот же объект, не создавая новых:
//
//   StringBuilder sb = new StringBuilder();  // создаём ОДИН объект
//   sb.append("Привет");   // дописываем в тот же массив
//   sb.append(" мир");     // дописываем в тот же массив
//   sb.append("!");        // дописываем в тот же массив
//   String result = sb.toString();  // создаём String ОДИН раз в конце
//
// Аналогия с C/C++:
//   String ≈ const char* (неизменяемый, при "конкатенации" — strcat в новый буфер)
//   StringBuilder ≈ std::string или динамический char[] (изменяемый буфер)
//
// ===== КОГДА ИСПОЛЬЗОВАТЬ ЧТО? =====
//
// 1. String + оператор "+" — для ПРОСТЫХ конкатенаций (2-3 части, не в цикле).
//    Компилятор Java сам оптимизирует: "a" + "b" + "c" → StringBuilder под капотом.
//
// 2. StringBuilder — для СЛОЖНЫХ случаев:
//    - Сборка строки в ЦИКЛЕ (главное правило!)
//    - Много условных append (if/else добавления частей)
//    - Модификация строки (insert, delete, replace)
//    - Нужна промежуточная работа со строкой перед финализацией
//
// 3. StringBuffer — только для МНОГОПОТОЧНОГО доступа к одному объекту.
//    Если строку собирает только один поток — всегда StringBuilder.
//
package rpg;

// ===== ИМПОРТ java.util.List (глава 5) =====
//
// List<String> используется в методе buildBattleSummary() для списка событий боя.
// Это единственный импорт — класс утилитный и самодостаточный.
import java.util.List;

// ===== final class BattleNarrator =====
//
// Класс объявлен как final — его НЕЛЬЗЯ наследовать (extends BattleNarrator — ошибка).
// Почему final для утилитного класса?
//   1. Все методы статические — наследование бессмысленно.
//   2. Предотвращаем случайное создание подклассов.
//   3. JVM может оптимизировать вызовы методов final-класса (инлайнинг).
//
// Аналогия в C++: class BattleNarrator final { ... };
// В Java ключевое слово final ПЕРЕД class, в C++ — ПОСЛЕ имени.
//
// Паттерн "утилитный класс": final class + private конструктор + static методы.
// Примеры из Java: Math, Arrays, Collections — все устроены так же.
public final class BattleNarrator {

    // ===== ПРИВАТНЫЙ КОНСТРУКТОР (запрет создания экземпляров) =====
    //
    // Зачем? Все методы статические — экземпляр не нужен.
    // Без приватного конструктора Java создаст публичный конструктор по умолчанию,
    // и кто-нибудь напишет: new BattleNarrator() — бессмысленный объект.
    //
    // private — модификатор доступа: конструктор виден ТОЛЬКО внутри этого класса.
    // Попытка вызвать извне: new BattleNarrator() → ошибка компиляции!
    //
    // Частая ошибка: забыть приватный конструктор у утилитного класса.
    private BattleNarrator() {
    }

    // ===== narrateAttack() — описание атаки через StringBuilder (7.3) =====
    //
    // Генерирует красочное описание атаки. Демонстрирует:
    //   - new StringBuilder(int capacity) — создание с начальной ёмкостью
    //   - append(String) — добавление текста
    //   - append(int) — добавление числа (автоматическое преобразование)
    //   - toString() — преобразование в String
    //   - deleteCharAt(int) — удаление символа по индексу (7.3)
    //   - setLength(int) — изменение длины (обрезка) (7.3)
    //
    // Параметры:
    //   attacker — имя атакующего ("Воин Артас")
    //   target   — имя цели ("Гоблин")
    //   damage   — нанесённый урон (42)
    //   isCrit   — критический удар? (true/false)
    //
    // Возвращает: "⚔ Воин Артас обрушивает удар на Гоблина, нанося 42 урона!"
    //         или: "💥 КРИТИЧЕСКИЙ УДАР! Воин Артас обрушивает удар на Гоблина, нанося 42 урона!!!"
    public static String narrateAttack(String attacker, String target, int damage, boolean isCrit) {

        // ===== ЗАЩИТА ОТ null — NULL SAFETY (7.2, 7.3) =====
        //
        // В Java строковая переменная может содержать null — это значит,
        // что ссылка «никуда не указывает» (аналог nullptr в C++).
        //
        // Если передать null в StringBuilder.append(), он НЕ бросит NullPointerException,
        // а вставит ТЕКСТ "null" (четыре буквы). Это скрытый баг:
        //   sb.append(null) → "null" в строке → пользователь увидит "⚔ null обрушивает удар"
        //
        // Правильный подход — проверять null ДО использования и подставлять безопасное значение.
        // Это называется «защитное программирование» (defensive programming).
        //
        // Аналогия с C: перед использованием указателя проверяем if (ptr != NULL).
        if (attacker == null) {
            attacker = "Неизвестный";
        }
        if (target == null) {
            target = "Неизвестный";
        }

        // ===== СОЗДАНИЕ StringBuilder С НАЧАЛЬНОЙ ЁМКОСТЬЮ (7.3) =====
        //
        // new StringBuilder() — создаёт пустой StringBuilder с ёмкостью 16 символов.
        // new StringBuilder(64) — создаёт пустой StringBuilder с ёмкостью 64 символа.
        // new StringBuilder("текст") — создаёт StringBuilder с копией строки + 16 символов запаса.
        //
        // Зачем указывать ёмкость?
        //   StringBuilder хранит символы в массиве char[]. Когда массив заполняется,
        //   создаётся НОВЫЙ массив удвоенного размера, и данные копируются (как std::vector в C++).
        //   Если заранее знаем примерную длину — избегаем лишних копирований.
        //
        // capacity() — текущая ёмкость (размер внутреннего массива).
        // length()   — текущая длина строки (сколько символов ЗАПИСАНО).
        // capacity >= length всегда.
        //
        // Здесь 80 символов — приблизительная длина описания атаки.
        StringBuilder sb = new StringBuilder(80);

        // ===== append() — ДОБАВЛЕНИЕ В КОНЕЦ СТРОКИ (7.3) =====
        //
        // append() — главный метод StringBuilder. Добавляет данные в КОНЕЦ буфера.
        // Перегружен для ВСЕХ типов данных:
        //   append(String s)    — добавить строку
        //   append(int i)       — добавить число (будет преобразовано в текст)
        //   append(char c)      — добавить символ
        //   append(boolean b)   — добавить "true" или "false"
        //   append(double d)    — добавить дробное число
        //   append(Object obj)  — вызовет obj.toString()
        //
        // ЦЕПОЧКА ВЫЗОВОВ (method chaining):
        //   sb.append("a").append("b").append("c");
        //   Это работает потому, что append() возвращает ССЫЛКУ НА ТОТ ЖЕ StringBuilder.
        //   Аналогия: std::ostream& operator<< в C++ тоже возвращает ссылку для цепочки.
        //
        // Частая ошибка: путать append() с конкатенацией +.
        //   sb.append("a" + "b") — сначала создаст новый String "ab", потом добавит.
        //   sb.append("a").append("b") — добавит напрямую, БЕЗ промежуточного String.
        //   Второй вариант эффективнее!
        if (isCrit) {
            sb.append("💥 КРИТИЧЕСКИЙ УДАР! ");
        } else {
            sb.append("⚔ ");
        }

        sb.append(attacker)
          .append(" обрушивает удар на ")
          .append(target)
          .append(", нанося ")
          .append(damage)   // append(int) — число автоматически преобразуется в текст
          .append(" урона");

        // ===== deleteCharAt(int index) — УДАЛЕНИЕ СИМВОЛА ПО ИНДЕКСУ (7.3) =====
        //
        // deleteCharAt(index) удаляет ОДИН символ на позиции index.
        // Символы после удалённого сдвигаются влево (как при удалении из массива в C).
        //
        // Индексация с 0 (как в массиве):
        //   "hello" → deleteCharAt(1) → "hllo"
        //
        // ОСТОРОЖНО: если index < 0 или index >= length() — выбросит
        //   StringIndexOutOfBoundsException!
        //
        // Отличие от delete(start, end):
        //   deleteCharAt(i) — удаляет ОДИН символ
        //   delete(start, end) — удаляет ДИАПАЗОН символов [start, end)
        //
        // Здесь: если описание заканчивается на пробел перед "урона" — убираем его.
        // Это демонстрация метода; в реальном коде пробел контролируется при сборке.
        if (damage == 0) {
            // При нулевом уроне заменяем "нанося 0 урона" на более короткий текст.
            // Демонстрируем setLength() — обрезку StringBuilder.

            // ===== setLength(int newLength) — ИЗМЕНЕНИЕ ДЛИНЫ (7.3) =====
            //
            // setLength() устанавливает ДЛИНУ строки (не ёмкость!).
            //   - Если newLength < текущей длины — строка ОБРЕЗАЕТСЯ (символы в конце теряются).
            //   - Если newLength > текущей длины — строка ДОПОЛНЯЕТСЯ нулевыми символами ('\0').
            //
            // Это самый быстрый способ «сбросить» StringBuilder:
            //   sb.setLength(0);  // — теперь sb пустой, но массив char[] остался (ёмкость та же)
            //
            // Отличие от delete(0, length()):
            //   setLength(0) — просто меняет счётчик длины (O(1))
            //   delete(0, length()) — то же, но через метод delete (чуть больше проверок)
            //
            // Аналогия в C: memset(buffer, 0, ...) + сброс длины vs memmove.
            //
            // Здесь: обрезаем до начала текста " нанося..." и дописываем новый конец.
            //
            // ===== indexOf(String str) у StringBuilder (7.3) =====
            //
            // Работает аналогично String.indexOf(): возвращает индекс первого вхождения
            // подстроки или -1 если не найдено. Позволяет искать позицию ВНУТРИ буфера,
            // не создавая промежуточную строку через toString().
            int cutPos = sb.indexOf(", нанося");
            if (cutPos > 0) {
                sb.setLength(cutPos);
                sb.append(", но промахивается!");
            }
        } else {
            sb.append("!");
        }

        if (isCrit) {
            sb.append("!!");
        }

        // ===== toString() — ПРЕОБРАЗОВАНИЕ В String (7.3) =====
        //
        // toString() создаёт НОВЫЙ неизменяемый String из содержимого StringBuilder.
        // После вызова StringBuilder можно продолжать использовать (он НЕ сбрасывается).
        //
        // ВАЖНО: результат toString() — НЕЗАВИСИМАЯ КОПИЯ.
        // Если потом изменить StringBuilder, строка НЕ изменится (и наоборот).
        //
        // Типичный паттерн: собрать в StringBuilder → вернуть toString().
        return sb.toString();
    }

    // ===== narrateHeal() — описание лечения через StringBuilder (7.3) =====
    //
    // Демонстрирует:
    //   - StringBuilder.append() с разными типами (String, int, char)
    //   - StringBuilder.insert(int offset, String) — вставка в произвольную позицию
    //   - StringBuilder.reverse() — переворот строки (декоративный эффект)
    //
    // Параметры:
    //   healer — имя лечащего ("Маг Гэндальф")
    //   amount — количество восстановленного здоровья (25)
    //
    // Возвращает: "✨ Маг Гэндальф призывает целительную магию! [+25 HP]"
    public static String narrateHeal(String healer, int amount) {

        // Защита от null (см. подробный комментарий в narrateAttack).
        // При null подставляем безопасное значение, чтобы метод НИКОГДА не падал.
        // Без этой проверки new StringBuilder(healer) на строке с reverse()
        // бросит NullPointerException, потому что конструктор StringBuilder(String)
        // вызывает str.length() — а у null нет метода length()!
        if (healer == null) {
            healer = "Неизвестный";
        }

        StringBuilder sb = new StringBuilder(64);

        sb.append("✨ ")
          .append(healer)
          .append(" призывает целительную магию!");

        // ===== insert(int offset, String str) — ВСТАВКА В ПОЗИЦИЮ (7.3) =====
        //
        // insert() вставляет текст в указанную позицию, СДВИГАЯ остальные символы вправо.
        //
        //   StringBuilder sb = new StringBuilder("Hello!");
        //   sb.insert(5, " World");  // → "Hello World!"
        //
        // Перегрузки (как у append):
        //   insert(offset, String)   — вставить строку
        //   insert(offset, char)     — вставить символ
        //   insert(offset, int)      — вставить число как текст
        //   insert(offset, char[])   — вставить массив символов
        //
        // offset = 0 — вставка В НАЧАЛО
        // offset = length() — вставка В КОНЕЦ (то же, что append)
        //
        // ОСТОРОЖНО: если offset < 0 или offset > length() — StringIndexOutOfBoundsException!
        //
        // Аналогия с C: это как memmove(buffer + offset + len, buffer + offset, ...)
        //   + memcpy(buffer + offset, newData, len). Дорогая операция при большой строке!
        //
        // Здесь: добавляем количество HP в конец через insert (демонстрация).
        String hpText = " [+" + amount + " HP]";
        sb.insert(sb.length(), hpText);

        // ===== reverse() — ПЕРЕВОРОТ СТРОКИ (7.3) =====
        //
        // reverse() переворачивает ВСЮ строку в StringBuilder:
        //   "Hello" → "olleH"
        //
        // ОСТОРОЖНО с Unicode: reverse() корректно обрабатывает суррогатные пары
        //   (символы вне BMP, например эмодзи). Пара из двух char переворачивается как единое целое.
        //
        // Практическое применение (редко):
        //   - Проверка палиндрома: str.equals(new StringBuilder(str).reverse().toString())
        //   - Декоративные эффекты в играх
        //   - Переворот числа (через toString + reverse + parseInt)
        //
        // Аналогия с C: std::reverse(str.begin(), str.end())
        //
        // Здесь: демонстрируем reverse + reverse для учебных целей (переворот и обратно).
        // Это показывает, что двойной reverse восстанавливает исходную строку.
        // В реальном коде двойной reverse бесполезен — это чисто УЧЕБНАЯ демонстрация.
        //
        // Создадим декоративный "отзвук" лечения — перевёрнутое имя лекаря.
        StringBuilder echo = new StringBuilder(healer);
        echo.reverse();
        sb.append(" ~").append(echo).append("~");

        return sb.toString();
    }

    // ===== narrateDefeat() — описание поражения (7.2 + 7.3) =====
    //
    // Демонстрирует строковые операции из главы 7.2:
    //   - toUpperCase() — перевод в верхний регистр
    //   - String.replace(CharSequence, CharSequence) — замена подстрок
    //   - substring(int, int) — извлечение подстроки
    //   - compareTo(String) — лексикографическое сравнение строк
    //   - getChars(int, int, char[], int) — копирование символов в массив
    //
    // Параметр:
    //   defeated — имя побеждённого ("Гоблин")
    //
    // Возвращает: "☠ ГОБЛИН ПОВЕРЖЕН! Слава победителю!"
    public static String narrateDefeat(String defeated) {

        // Защита от null И пустой строки (см. подробный комментарий в narrateAttack).
        //
        // Здесь нужна ДВОЙНАЯ проверка:
        //   1) null — иначе defeated.toUpperCase() бросит NullPointerException.
        //   2) isEmpty() — иначе defeated.substring(0, 1) бросит
        //      StringIndexOutOfBoundsException, потому что в пустой строке
        //      нет символа с индексом 0!
        //
        // isEmpty() — метод String (7.2), возвращает true если length() == 0.
        // ПОРЯДОК ВАЖЕН: сначала проверяем null, потом isEmpty().
        //   if (defeated.isEmpty()) — при null бросит NPE ещё ДО проверки!
        //   if (defeated == null || defeated.isEmpty()) — при null Java НЕ вычисляет
        //   второе условие (short-circuit evaluation, «ленивое вычисление»).
        if (defeated == null || defeated.isEmpty()) {
            return "☠ Неизвестный враг повержен!";
        }

        // ===== toUpperCase() — ПЕРЕВОД В ВЕРХНИЙ РЕГИСТР (7.2) =====
        //
        // toUpperCase() возвращает НОВУЮ строку, где все буквы в верхнем регистре.
        // Исходная строка НЕ меняется (String — immutable)!
        //
        //   String s = "Hello";
        //   String upper = s.toUpperCase();  // "HELLO"
        //   // s по-прежнему "Hello" — оригинал не изменился!
        //
        // Есть вариант с Locale:
        //   s.toUpperCase(Locale.ROOT) — безопасен для всех языков.
        //   Без Locale — используется системная локаль, что может дать
        //   неожиданный результат для турецкого языка ('i' → 'İ', а не 'I').
        //
        // Парный метод: toLowerCase() — в нижний регистр.
        //
        // Аналогия с C: toupper() для одного символа, в C нет встроенной toUpperCase для строк.
        // В C++ можно: std::transform(s.begin(), s.end(), s.begin(), ::toupper);
        //
        // Важно: toUpperCase() корректно работает с кириллицей!
        //   "гоблин".toUpperCase() → "ГОБЛИН" ✓
        String upperName = defeated.toUpperCase();

        // ===== String.replace(CharSequence target, CharSequence replacement) (7.2) =====
        //
        // replace() заменяет ВСЕ вхождения target на replacement.
        // Возвращает НОВУЮ строку (оригинал не меняется — immutable!).
        //
        //   "hello world hello".replace("hello", "hi") → "hi world hi"
        //
        // ВАЖНО: replace() НЕ использует regex! Это простая замена подстрок.
        // Для regex-замен есть replaceAll() и replaceFirst() (глава 7.4).
        //
        // Есть также replace(char, char) — замена одного символа на другой:
        //   "hello".replace('l', 'r') → "herro"
        //
        // Аналогия с C++: std::string::replace(), но Java-версия заменяет ВСЕ вхождения
        //   (в C++ std::string::replace() работает по позиции, не по содержимому).
        //
        // Здесь: заменяем пробелы на " ✦ " для декоративного эффекта в имени.
        String decoratedName = upperName.replace(" ", " ✦ ");

        // ===== substring(int beginIndex, int endIndex) — ИЗВЛЕЧЕНИЕ ПОДСТРОКИ (7.2) =====
        //
        // substring() возвращает НОВУЮ строку — часть исходной.
        //
        // Две формы:
        //   substring(beginIndex)             — от beginIndex до КОНЦА строки
        //   substring(beginIndex, endIndex)   — от beginIndex до endIndex (НЕ включая endIndex)
        //
        //   "Hello World".substring(6)      → "World"
        //   "Hello World".substring(0, 5)   → "Hello"
        //
        // ОСТОРОЖНО: beginIndex и endIndex — это индексы СИМВОЛОВ (с 0).
        //   - beginIndex < 0 → StringIndexOutOfBoundsException
        //   - endIndex > length() → StringIndexOutOfBoundsException
        //   - beginIndex > endIndex → StringIndexOutOfBoundsException
        //
        // Аналогия с C++: std::string::substr(pos, count).
        //   Но в Java — начало и конец (не начало и длина)!
        //   Java: substring(2, 5) = 3 символа (индексы 2, 3, 4)
        //   C++:  substr(2, 3)    = 3 символа (от позиции 2, длина 3)
        //
        // Здесь: берём первый символ имени для иконки.
        String initial = defeated.substring(0, 1).toUpperCase();

        // ===== compareTo(String anotherString) — ЛЕКСИКОГРАФИЧЕСКОЕ СРАВНЕНИЕ (7.2) =====
        //
        // compareTo() сравнивает две строки посимвольно по значениям Unicode.
        //
        // Возвращает:
        //   0           — строки РАВНЫ
        //   < 0 (отрицательное) — текущая строка "меньше" (идёт раньше в алфавитном порядке)
        //   > 0 (положительное) — текущая строка "больше" (идёт позже в алфавитном порядке)
        //
        //   "apple".compareTo("banana")  → отрицательное ('a' < 'b')
        //   "banana".compareTo("apple")  → положительное ('b' > 'a')
        //   "hello".compareTo("hello")   → 0
        //
        // Это метод интерфейса Comparable<String>:
        //   - используется при сортировке (Collections.sort(), TreeSet, TreeMap)
        //   - определяет "естественный порядок" строк
        //
        // ВАЖНО: compareTo() чувствителен к регистру!
        //   "Apple".compareTo("apple") → отрицательное ('A' = 65 < 'a' = 97)
        //   Для сравнения без регистра: compareToIgnoreCase()
        //
        // Аналогия с C: strcmp(str1, str2) — точно такая же семантика!
        //   Java compareTo() ≡ C strcmp() по возвращаемому значению.
        //
        // Здесь: выбираем суффикс в зависимости от того, начинается ли имя
        //   с буквы из первой или второй половины алфавита (декоративный эффект).
        String suffix;
        if (initial.compareTo("О") < 0) {
            suffix = "Слава победителю!";
        } else {
            suffix = "Враг повержен навеки!";
        }

        // ===== getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) (7.2) =====
        //
        // getChars() копирует символы из строки в массив char[].
        //
        // Параметры:
        //   srcBegin — начальный индекс в строке (включительно)
        //   srcEnd   — конечный индекс в строке (не включительно)
        //   dst      — целевой массив char[]
        //   dstBegin — начальный индекс в целевом массиве
        //
        //   String s = "Hello World";
        //   char[] buf = new char[5];
        //   s.getChars(6, 11, buf, 0);  // buf = ['W', 'o', 'r', 'l', 'd']
        //
        // ЗАЧЕМ? Когда нужно работать с символами строки как с массивом.
        // Быстрее, чем charAt() в цикле, потому что копирует блок за один вызов
        // (внутри — System.arraycopy, как memcpy в C).
        //
        // ОСТОРОЖНО: массив dst должен быть достаточного размера!
        //   Иначе — ArrayIndexOutOfBoundsException.
        //
        // Аналогия с C: strncpy(dst, src + srcBegin, srcEnd - srcBegin)
        //   Но Java проверяет границы — нет переполнения буфера (buffer overflow)!
        //
        // Здесь: копируем имя побеждённого в буфер для демонстрации метода.
        int nameLen = Math.min(defeated.length(), 20);
        char[] nameBuffer = new char[nameLen];
        defeated.getChars(0, nameLen, nameBuffer, 0);

        // Используем буфер для создания «эхо»-эффекта (угасающее имя).
        // Берём первые 3 символа (или меньше, если имя короткое).
        int echoLen = Math.min(nameLen, 3);
        String echo = new String(nameBuffer, 0, echoLen);

        // Собираем финальную строку через StringBuilder.
        StringBuilder sb = new StringBuilder(80);
        sb.append("☠ ")
          .append(decoratedName)
          .append(" ПОВЕРЖЕН! ")
          .append(suffix)
          .append(" (")
          .append(echo)
          .append("...)");

        return sb.toString();
    }

    // ===== buildBattleSummary() — итог боя через StringBuilder в цикле (7.3) =====
    //
    // Демонстрирует ГЛАВНОЕ применение StringBuilder — сборку строки в ЦИКЛЕ.
    // Это самый важный паттерн: никогда не используйте += в цикле для строк!
    //
    // ❌ ПЛОХО:
    //   String result = "";
    //   for (String event : events) {
    //       result += event + "\n";  // каждая итерация создаёт НОВЫЙ String!
    //   }
    //   // Для 100 событий — 200 промежуточных объектов String!
    //
    // ✅ ХОРОШО:
    //   StringBuilder sb = new StringBuilder();
    //   for (String event : events) {
    //       sb.append(event).append("\n");  // все в ОДИН буфер
    //   }
    //   String result = sb.toString();  // ОДИН новый String в конце
    //
    // Демонстрирует:
    //   - StringBuilder(int capacity) — предварительное выделение ёмкости
    //   - append() в цикле — основной паттерн
    //   - delete(int start, int end) — удаление диапазона символов
    //   - replace(int start, int end, String) — замена подстроки в StringBuilder
    //
    // Параметр:
    //   events — список событий боя (["Удар: 20 урона", "Лечение: +15 HP", ...])
    //
    // Возвращает: многострочный итог боя.
    public static String buildBattleSummary(List<String> events) {

        if (events == null || events.isEmpty()) {
            return "Бой не состоялся.";
        }

        // ===== ПРЕДВАРИТЕЛЬНОЕ ВЫДЕЛЕНИЕ ЁМКОСТИ (7.3) =====
        //
        // Примерная длина: заголовок (~30) + каждое событие (~50) + футер (~30).
        // Лучше выделить чуть больше, чем нужно, чем расширять массив несколько раз.
        //
        // Формула: начальная_ёмкость = 60 + количество_событий * 50
        // Это ПРИБЛИЗИТЕЛЬНАЯ оценка — точную ёмкость знать не обязательно.
        int estimatedCapacity = 60 + events.size() * 50;
        StringBuilder sb = new StringBuilder(estimatedCapacity);

        sb.append("╔══════════════════════════════╗\n");
        sb.append("║      ИТОГИ СРАЖЕНИЯ          ║\n");
        sb.append("╠══════════════════════════════╣\n");

        // ===== СБОРКА В ЦИКЛЕ — ГЛАВНЫЙ ПАТТЕРН StringBuilder (7.3) =====
        //
        // for-each цикл с append() — самый частый сценарий использования StringBuilder.
        // Без StringBuilder здесь пришлось бы конкатенировать += в цикле,
        // создавая N промежуточных объектов String.
        for (int i = 0; i < events.size(); i++) {
            sb.append("║ ")
              .append(i + 1)         // append(int) — номер события
              .append(". ")
              .append(events.get(i))
              .append('\n');         // append(char) — перенос строки
        }

        sb.append("╠══════════════════════════════╣\n");
        sb.append("║ Всего ходов: ").append(events.size()).append('\n');
        sb.append("╚══════════════════════════════╝");

        // ===== replace(int start, int end, String str) — ЗАМЕНА В StringBuilder (7.3) =====
        //
        // replace() в StringBuilder ОТЛИЧАЕТСЯ от replace() в String!
        //
        // StringBuilder.replace(start, end, str):
        //   - Заменяет символы с позиции start до end (не включая end) на str.
        //   - Длина замены может ОТЛИЧАТЬСЯ от длины заменяемого фрагмента.
        //   - Работает ПО ПОЗИЦИИ (индексы), а не по содержимому.
        //
        // String.replace(target, replacement):
        //   - Заменяет ВСЕ ВХОЖДЕНИЯ target на replacement.
        //   - Работает ПО СОДЕРЖИМОМУ (ищет подстроку).
        //
        //   StringBuilder sb = new StringBuilder("Hello World!");
        //   sb.replace(6, 11, "Java");  // → "Hello Java!"
        //
        // ОСТОРОЖНО: после replace() индексы СДВИГАЮТСЯ, если новая подстрока
        //   длиннее или короче заменённой!
        //
        // Здесь: заменяем "ИТОГИ СРАЖЕНИЯ" на "БОЕВОЙ ЖУРНАЛ" (демонстрация).
        // indexOf(String) — поиск подстроки в буфере (см. объяснение в narrateAttack()).
        int titleStart = sb.indexOf("ИТОГИ СРАЖЕНИЯ");
        if (titleStart >= 0) {
            int titleEnd = titleStart + "ИТОГИ СРАЖЕНИЯ".length();
            sb.replace(titleStart, titleEnd, "БОЕВОЙ ЖУРНАЛ ");
        }

        // ===== delete(int start, int end) — УДАЛЕНИЕ ДИАПАЗОНА (7.3) =====
        //
        // delete(start, end) удаляет символы с позиции start до end (не включая end).
        // Оставшиеся символы сдвигаются влево.
        //
        //   StringBuilder sb = new StringBuilder("Hello World");
        //   sb.delete(5, 11);  // → "Hello"
        //
        // Отличие от deleteCharAt(index): delete() удаляет ДИАПАЗОН, deleteCharAt() — ОДИН символ.
        //
        // Аналогия с C: memmove(buf + start, buf + end, ...) — сдвиг данных.
        //
        // Здесь: если в итоге есть двойные переносы строк — удаляем лишний
        // (демонстрация метода delete в контексте очистки текста).
        int doubleNewline = sb.indexOf("\n\n");
        if (doubleNewline >= 0) {
            sb.delete(doubleNewline, doubleNewline + 1);
        }

        // ===== deleteCharAt(int index) — УДАЛЕНИЕ ОДНОГО СИМВОЛА (7.3) =====
        //
        // deleteCharAt(index) удаляет ровно ОДИН символ на позиции index.
        // Все символы после него сдвигаются на одну позицию влево.
        //
        //   StringBuilder sb = new StringBuilder("Hello!");
        //   sb.deleteCharAt(5);  // → "Hello" (удалён '!' на позиции 5)
        //
        // Отличие от delete(start, end):
        //   deleteCharAt(i)    — удаляет ОДИН символ (эквивалент delete(i, i+1))
        //   delete(start, end) — удаляет ДИАПАЗОН символов [start, end)
        //
        // ОСТОРОЖНО: если index < 0 или index >= length() — выбросит
        //   StringIndexOutOfBoundsException!
        //
        // Здесь: демонстрируем типичный паттерн — удаление завершающего разделителя
        // (trailing separator removal).
        //
        // Часто при сборке строки вида "a, b, c, " в цикле остаётся лишняя запятая
        // в конце. Вместо сложной логики «если последний элемент — не добавляй запятую»
        // проще добавлять запятую ВСЕГДА, а потом удалить последнюю через deleteCharAt().
        //
        // Аналогия с C: buf[strlen(buf) - 1] = '\0'; — обрезка последнего символа.
        //
        // Строим мини-строку с номерами ходов: "1, 2, 3" — через deleteCharAt.
        StringBuilder turns = new StringBuilder(events.size() * 3);
        for (int i = 1; i <= events.size(); i++) {
            turns.append(i).append(',');
        }
        if (turns.length() > 0) {
            turns.deleteCharAt(turns.length() - 1);
        }

        // Вставляем строку с номерами ходов ПЕРЕД закрывающей рамкой ╚.
        // indexOf() — см. объяснение в narrateAttack().
        int footerPos = sb.indexOf("╚");
        if (footerPos >= 0) {
            sb.insert(footerPos, "║ Ходы: " + turns + "\n");
        }

        return sb.toString();
    }

    // ===== highlightCritical() — выделение критического текста (7.2 + 7.3) =====
    //
    // Принимает текст и возвращает его «выделенным»:
    //   - переводит в верхний регистр (toUpperCase)
    //   - оборачивает в декоративные символы через StringBuilder.insert()
    //
    // Демонстрирует:
    //   - String.toUpperCase() (7.2) — перевод в верхний регистр
    //   - StringBuilder.insert() (7.3) — вставка в начало и конец
    //   - StringBuilder.append() (7.3) — добавление в конец
    //   - String.substring() (7.2) — при обрезке длинного текста
    //
    // Параметр:
    //   text — текст для выделения ("критический удар")
    //
    // Возвращает: ">>> ★ КРИТИЧЕСКИЙ УДАР ★ <<<"
    public static String highlightCritical(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // toUpperCase() — все буквы в верхний регистр (7.2)
        String upper = text.toUpperCase();

        // substring() — обрезаем слишком длинный текст (7.2)
        // Ограничиваем 30 символами, чтобы выделение не растягивалось.
        if (upper.length() > 30) {
            upper = upper.substring(0, 30) + "...";
        }

        // StringBuilder + insert() — сборка выделенного текста (7.3)
        StringBuilder sb = new StringBuilder(upper);

        // insert(0, str) — вставка В НАЧАЛО строки.
        // Все существующие символы сдвигаются вправо.
        //
        // Это дорогая операция — O(n), где n = текущая длина.
        // Почему? Нужно сдвинуть ВСЕ символы в массиве вправо, чтобы освободить место.
        // Если вставляете в начало часто — подумайте об обратном порядке сборки
        //   или используйте LinkedList<String> + String.join().
        sb.insert(0, ">>> ★ ");
        sb.append(" ★ <<<");

        return sb.toString();
    }

    // ===== УЧЕБНЫЙ БЛОК: StringBuffer vs StringBuilder (7.3) =====
    //
    // Этот метод — ЧИСТО УЧЕБНАЯ ДЕМОНСТРАЦИЯ.
    // Он показывает, что StringBuffer и StringBuilder имеют ОДИНАКОВЫЙ API,
    // но различаются потокобезопасностью.
    //
    // В реальной игре этот метод не вызывается — он существует для обучения.
    // Если бы это был production-код, метод был бы удалён.
    //
    // ===== StringBuffer — ПОТОКОБЕЗОПАСНЫЙ StringBuilder (7.3) =====
    //
    // StringBuffer и StringBuilder — почти БЛИЗНЕЦЫ:
    //   - Одинаковые конструкторы: StringBuffer(), StringBuffer(capacity), StringBuffer(str)
    //   - Одинаковые методы: append(), insert(), delete(), replace(), reverse(), ...
    //   - Одинаковое поведение: изменяемый буфер символов
    //
    // ЕДИНСТВЕННОЕ ОТЛИЧИЕ: StringBuffer — СИНХРОНИЗИРОВАННЫЙ (synchronized).
    //
    // Что это значит?
    //   В многопоточной программе несколько потоков (Thread) могут одновременно
    //   вызывать методы одного объекта. Если два потока одновременно вызовут append(),
    //   данные могут повредиться (race condition).
    //
    //   StringBuffer решает это с помощью synchronized:
    //     public synchronized StringBuffer append(String str) { ... }
    //   Ключевое слово synchronized означает: только ОДИН поток может выполнять
    //   этот метод в любой момент времени. Остальные потоки ЖДУТ.
    //
    //   StringBuilder НЕ synchronized — поэтому быстрее, но НЕ потокобезопасен.
    //
    // ===== КОГДА ЧТО ИСПОЛЬЗОВАТЬ? =====
    //
    //   ┌──────────────────────────────────────────────────────────────────┐
    //   │  Ситуация                            │ Что использовать        │
    //   ├──────────────────────────────────────────────────────────────────┤
    //   │  Один поток (99% случаев)            │ StringBuilder           │
    //   │  Несколько потоков, один буфер        │ StringBuffer            │
    //   │  Несколько потоков, каждый свой буфер │ StringBuilder           │
    //   └──────────────────────────────────────────────────────────────────┘
    //
    // В нашей RPG-игре — один поток. Поэтому везде StringBuilder.
    //
    // Аналогия с C++:
    //   StringBuilder ≈ std::string (без блокировок, быстрый)
    //   StringBuffer  ≈ std::string + std::mutex (с блокировкой, медленнее)
    //
    // История: StringBuffer появился в Java 1.0, StringBuilder — в Java 1.5.
    // До Java 1.5 все использовали StringBuffer, потому что альтернативы не было.
    // Сейчас StringBuffer считается "legacy" — используйте StringBuilder,
    // если нет явной необходимости в потокобезопасности.
    //
    // ===== РАЗНИЦА В ПРОИЗВОДИТЕЛЬНОСТИ =====
    //
    // StringBuffer ~10-20% медленнее StringBuilder из-за synchronized.
    // В однопоточном коде synchronized — это бессмысленная блокировка:
    //   поток «захватывает» монитор объекта перед каждым вызовом append(),
    //   хотя никто другой и не пытается к нему обратиться.
    //
    // JIT-компилятор Java МОЖЕТ оптимизировать ненужную синхронизацию
    //   (lock elision), но это не гарантировано.
    public static String demonstrateStringBuffer() {

        // ===== КОНСТРУКТОРЫ StringBuffer (7.3) =====
        //
        // StringBuffer() — пустой буфер, ёмкость 16 символов.
        // StringBuffer(int capacity) — пустой буфер с указанной ёмкостью.
        // StringBuffer(String str) — буфер с копией строки + 16 символов запаса.
        // StringBuffer(CharSequence chars) — буфер с копией последовательности.
        //
        // Точно такие же, как у StringBuilder!
        StringBuffer buffer = new StringBuffer("Демо StringBuffer: ");

        // append() — добавление в конец (synchronized-версия)
        buffer.append("потокобезопасный ");
        buffer.append("буфер ");
        buffer.append("символов.");

        // insert() — вставка в позицию (synchronized-версия)
        buffer.insert(19, "[");
        buffer.insert(buffer.indexOf("символов."), "] ");

        // delete() — удаление диапазона (synchronized-версия)
        // Удалим лишний пробел, если есть.
        String bufStr = buffer.toString();
        int doubleSpace = bufStr.indexOf("  ");
        if (doubleSpace >= 0) {
            buffer.delete(doubleSpace, doubleSpace + 1);
        }

        // replace() — замена диапазона (synchronized-версия)
        int start = buffer.indexOf("буфер");
        if (start >= 0) {
            buffer.replace(start, start + "буфер".length(), "БУФЕР");
        }

        // reverse() — переворот (synchronized-версия)
        StringBuffer reversDemo = new StringBuffer("Hello");
        reversDemo.reverse();
        String reversed = reversDemo.toString(); // "olleH"

        // capacity() — текущий размер внутреннего char[]-буфера (сколько символов вмещает).
        // length()   — сколько символов РЕАЛЬНО записано в буфер. Всегда length <= capacity.
        // Оба метода доступны в StringBuffer и StringBuilder с одинаковой семантикой.
        // Подробное объяснение capacity/ensureCapacity — см. TextFormatter.buildProgressBar().
        int cap = buffer.capacity();
        int len = buffer.length();

        // toString() — преобразование в String
        // Итоговая строка: результат всех операций + информация о capacity/length.
        StringBuilder result = new StringBuilder(200);
        result.append("=== StringBuffer vs StringBuilder ===\n");
        result.append("StringBuffer результат: ").append(buffer).append('\n');
        result.append("Reversed 'Hello': ").append(reversed).append('\n');
        result.append("Capacity: ").append(cap).append(", Length: ").append(len).append('\n');
        result.append("API идентичен StringBuilder, но все методы synchronized.\n");
        result.append("В однопоточном коде всегда используйте StringBuilder!");

        return result.toString();
    }
}
