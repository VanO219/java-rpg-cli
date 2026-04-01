# Глава 7: Работа со строками (7.1–7.4)

Эта глава демонстрирует строковые классы Java через реальные игровые сценарии:
форматирование консольного вывода, парсинг текстовых команд, генерация боевых описаний и валидация ввода с помощью регулярных выражений.

---

## Содержание

1. [Класс String — введение](#1-класс-string--введение-71)
2. [Основные операции со строками](#2-основные-операции-со-строками-72)
3. [StringBuilder и StringBuffer](#3-stringbuilder-и-stringbuffer-73)
4. [Регулярные выражения](#4-регулярные-выражения-74)
5. [Рефакторинг toString() с StringBuilder](#5-рефакторинг-tostring-с-stringbuilder)
6. [Интеграция в Game.java](#6-интеграция-в-gamejava)
7. [Новые файлы](#7-новые-файлы)
8. [Сводные таблицы](#8-сводные-таблицы)

---

## 1. Класс String — введение (7.1)

**Основной файл:** `TextFormatter.java`, строки 109–768  
**Вспомогательный:** `CommandParser.java`, строки 47–490

### Неизменяемость строк (immutability)

Строки в Java — неизменяемые объекты (`final class String`). Любая «модификация» создаёт **новый объект**, оригинал остаётся нетронутым. Это принципиально отличается от `char[]` в C и `std::string` в C++.

```java
// Из TextFormatter.java, строка 185–190
// substring(0, width) возвращает НОВУЮ строку — оригинал text не меняется.
// В C: strncpy(buf, text, width) изменяет buf.
// В Java: text.substring(0, 3) → новый объект String.
if (text.length() >= width) {
    return text.substring(0, width);
}
```

Подробное объяснение неизменяемости — `TextFormatter.java`, строки 154–190 (метод `centerText`).

### String pool и == vs equals()

```java
// TextFormatter.java, строки 151–160 (комментарий в centerText)
// В Java строковые литералы ("текст") хранятся в общем пуле (String Pool).
// Две переменные с одним литералом указывают на ОДИН объект.
//
//   String a = "hello";
//   String b = "hello";
//   a == b     → true  (один объект в пуле)
//
//   String c = new String("hello");
//   c == b     → false (new создаёт НОВЫЙ объект за пределами пула)
//   c.equals(b) → true (содержимое одинаковое)
//
// ПРАВИЛО: для сравнения строк всегда используй equals(), не ==!
// == сравнивает ССЫЛКИ (адреса объектов), equals() — СОДЕРЖИМОЕ.
```

### Методы String (7.1)

| Метод | Где продемонстрирован | Строки |
|-------|-----------------------|--------|
| `length()` | `TextFormatter.centerText()` — вычисление отступов | 181, 194 |
| `isEmpty()` | `CommandParser.parse()` — проверка после `strip()` | 192 |
| `isBlank()` | `CommandParser.scrambleText()` — отличие от `isEmpty()` | 699 |
| `toCharArray()` | `TextFormatter.scrambleText()` — посимвольная обработка | 704 |
| `new String(char[])` | `TextFormatter.scrambleText()` — создание строки из массива | 727 |
| `substring(0, n)` | `TextFormatter.centerText()` — обрезка при превышении ширины | 190 |
| `substring(n)` | `TextFormatter.reverseText()` — весь буфер как строка | 766 |
| `repeat(n)` | `TextFormatter.centerText()` — рисование пробелов | 202 |

#### isEmpty() vs isBlank()

```java
// TextFormatter.scrambleText(), строка 697–699
// isEmpty() — строка пустая? Длина == 0.
//   "".isEmpty()         → true
//   " ".isEmpty()        → false (есть символ пробела!)
//   "hi".isEmpty()       → false
//
// isBlank() — строка состоит ТОЛЬКО из пробельных символов (или пуста)? (Java 11+)
//   "".isBlank()         → true
//   " ".isBlank()        → true  ← ключевое отличие
//   "\t\n".isBlank()     → true
//   "hi".isBlank()       → false
//
// Для проверки пользовательского ввода нужен isBlank(), а не isEmpty().
if (text == null || text.isEmpty()) {
    return "";
}
```

#### toCharArray() и new String(char[])

```java
// TextFormatter.scrambleText(), строки 703–727
//
// toCharArray() — возвращает КОПИЮ символов строки как char[].
// Изменение массива НЕ влияет на оригинальную строку (она immutable).
//   В C: strcpy(arr, str) — копируем в char[]. В Java — аналогично, но через метод.
//
char[] chars = text.toCharArray();   // строка 704

// Прямой доступ к элементам массива — быстрее, чем text.charAt(i) в цикле.
char temp = chars[i];
chars[i] = chars[i + 1];
chars[i + 1] = temp;

// new String(char[]) — создание строки из массива.
// Конструктор копирует содержимое массива в строку.
// Дальнейшие изменения массива НЕ повлияют на созданную строку.
return new String(chars);            // строка 727
```

---

## 2. Основные операции со строками (7.2)

**Основной файл:** `CommandParser.java`, строки 47–490  
**Вспомогательный:** `BattleNarrator.java`, строки 76–829

### split() — разбиение строки

```java
// CommandParser.parse(), строки 196–222
//
// split(String regex) разбивает строку по регулярному выражению.
// Возвращает массив String[].
//
// ЛОВУШКА: split(" ") и несколько пробелов подряд → пустые строки в массиве!
//   "one  two".split(" ") → ["one", "", "two"]   ← нежелательно
//   "one  two".split("\\s+") → ["one", "two"]    ← правильно
//
// \\s — пробельный символ в regex. + — один или более.
// ВАЖНО: параметр split() — это REGEX, не просто строка!
//   "a.b".split(".")  → []           (точка = «любой символ» в regex!)
//   "a.b".split("\\.") → ["a", "b"] (экранируем точку)
//
// Аналог в C: strtok(str, " ") — но strtok модифицирует строку! Java — нет.
String[] parts = stripped.split("\\s+");  // строка 222
```

### strip() / trim() / stripLeading() / stripTrailing()

```java
// CommandParser.parse(), строки 157–181
//
// trim() — старый метод (Java 1.0). Удаляет символы с кодом ≤ U+0020.
// strip() — новый метод (Java 11). Удаляет ВСЕ Unicode-пробелы.
//
// Разница на Unicode-пробелах:
//   '\u2000' (En Quad, типографский пробел):
//     "x\u2000".trim()  → "x\u2000"  (trim НЕ удалил!)
//     "x\u2000".strip() → "x"        (strip удалил)
//
// РЕКОМЕНДАЦИЯ: всегда strip() вместо trim() в новом коде.
//
// stripLeading()  — только слева:  "  Hi  ".stripLeading()  → "Hi  "
// stripTrailing() — только справа: "  Hi  ".stripTrailing() → "  Hi"
String stripped = input.strip();  // строка 181
```

### toLowerCase() / toUpperCase()

```java
// CommandParser.parse(), строки 224–258
// toLowerCase() возвращает НОВУЮ строку, оригинал не меняется.
// Для кириллицы работает корректно (требует корректной локали JVM).
//
//   "АТАКА".toLowerCase() → "атака"
//   "Hello".toUpperCase() → "HELLO"
//
// Частая ошибка: parts[0].toLowerCase() == "атака" → всегда false!
//   Используй toLowerCase().equals("атака") или equalsIgnoreCase().
String command = parts[0].toLowerCase();  // примерно строка 261
```

### equals() / equalsIgnoreCase() / compareTo()

```java
// CommandParser.matchCommand(), строки 319–418
//
// equals(Object) — сравнение СОДЕРЖИМОГО двух строк (глава 7.2).
//   НИКОГДА не используй == для сравнения строк — это сравнение ССЫЛОК!
//
// equalsIgnoreCase(String) — сравнение без учёта регистра:
//   "АТАКА".equalsIgnoreCase("атака") → true
//
// compareTo(String) — лексикографическое сравнение (нужен для сортировки).
//   Возвращает int: 0 если равны, < 0 если меньше, > 0 если больше.
//   Используется в BattleRecord (глава 5.6) для Comparable.
```

### indexOf() / lastIndexOf() / contains() / startsWith() / endsWith()

```java
// CommandParser.extractArgument(), строки 420–456
// BattleNarrator.buildBattleSummary(), строка 596
//
// indexOf(String) — первое вхождение подстроки. Возвращает -1 если не найдено.
//   "Hello World".indexOf("World")  → 6
//   "Hello".indexOf("xyz")          → -1  ← проверяй результат перед substring!
//
// lastIndexOf(String) — последнее вхождение.
//   "aabbcc".lastIndexOf("b")       → 3
//
// contains(CharSequence) — содержит ли строка подстроку?
//   "attack fire".contains("fire")  → true
//
// startsWith(String) — начинается ли с данного префикса?
//   "/give_gold 100".startsWith("/")  → true
//
// endsWith(String) — заканчивается ли на данный суффикс?
//   "hero.sav".endsWith(".sav")  → true

// Из Game.java, readHeroName(), строка 774:
// startsWith("OK") — проверяем результат валидации StringValidator.
if (!validationResult.startsWith("OK")) { ... }
```

### replace() / replaceAll()

```java
// BattleNarrator.narrateDefeat(), строки 344–430
//
// replace(CharSequence target, CharSequence replacement):
//   Заменяет ВСЕ ВХОЖДЕНИЯ target (строка, не regex) на replacement.
//   "Goblin defeated!".replace("Goblin", "Orc") → "Orc defeated!"
//
// replaceAll(String regex, String replacement):
//   Заменяет по регулярному выражению (медленнее — компилирует regex каждый раз).
//   "abc123def".replaceAll("\\d+", "#") → "abc#def"
//
// В StringValidator.censorBadWords() — Matcher.replaceAll() (глава 7.4).
```

### regionMatches()

```java
// CommandParser.matchCommand(), строки 380–418
//
// regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len)
//   Сравнивает подстроки двух строк.
//   Полезно когда нужно сравнить часть строки, не создавая substring.
//
//   "Hello World".regionMatches(true, 6, "WORLD", 0, 5) → true
//   (игнорируем регистр, сравниваем с позиции 6, 5 символов)
//
// ЗАЧЕМ вместо substring+equals?
//   regionMatches не создаёт временный объект — чуть эффективнее.
```

### String.join()

```java
// CommandParser.buildHelp(), строки 458–490
//
// String.join(CharSequence delimiter, Iterable<? extends CharSequence> elements)
//   Соединяет строки через разделитель.
//   String.join(", ", List.of("атака", "защита", "инвентарь")) → "атака, защита, инвентарь"
//
// До Java 8 писали цикл: sb.append(item).append(", ") + обрезка хвостовой запятой.
// String.join() делает это в одну строку и работает с любым Iterable.
```

### concat() vs +

```java
// CommandParser, строки 290–318 (комментарий в parse())
//
// concat(String str) — метод String, добавляет str в конец.
//   "Hello".concat(" World") → "Hello World"
//
// Отличие от +:
//   + работает с ЛЮБЫМИ типами (int, boolean, Object), компилятор использует StringBuilder.
//   concat() принимает только String, не null.
//   "text".concat(null) → NullPointerException!
//   "text" + null     → "textnull" (безопаснее)
//
// РЕКОМЕНДАЦИЯ: в большинстве случаев используй +.
//   concat() полезен только в редких случаях.
```

### getChars()

```java
// BattleNarrator.narrateAttack(), строки 200–250 (комментарий)
//
// getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin)
//   Копирует символы из строки в существующий массив char[].
//   "Hello".getChars(0, 3, arr, 0) → arr[0..2] = ['H', 'e', 'l']
//
// Отличие от toCharArray(): toCharArray() создаёт НОВЫЙ массив.
//   getChars() копирует в СУЩЕСТВУЮЩИЙ — полезно для буферизации.
```

---

## 3. StringBuilder и StringBuffer (7.3)

**Основной файл:** `TextFormatter.java`, строки 268–768  
**Вспомогательный:** `BattleNarrator.java`, строки 76–829

### Зачем нужен StringBuilder

```java
// GameCharacter.toString(), строки 614–636 (объяснение-точка входа)
//
// Проблема с +:
//   String s = "a" + b + "c" + d + "e";
//   Компилятор превращает это в цепочку StringBuilder, НО:
//   В цикле:
//     String s = "";
//     for (var item : list) { s += item; }  // O(N²) — N промежуточных объектов!
//
// Решение: StringBuilder — ИЗМЕНЯЕМЫЙ буфер символов (mutable).
//   Не создаёт промежуточных строк при append().
//   Один объект → одна аллокация → sb.toString() в конце.
//
// Аналогия с C++: std::ostringstream или std::string::reserve() + +=.
//
// ПРАВИЛО:
//   2–3 склейки вне цикла → «+» допустим.
//   3+ склейки ИЛИ цикл  → StringBuilder обязателен.
```

### Конструкторы и capacity / ensureCapacity()

```java
// TextFormatter.buildProgressBar(), строки 278–346
//
// new StringBuilder()           — ёмкость 16 символов.
// new StringBuilder(int cap)    — ёмкость cap символов.
// new StringBuilder(String s)   — копия s, ёмкость = s.length() + 16.
//
// capacity() — размер внутреннего char[]-массива.
// length()   — сколько символов реально записано. Всегда capacity >= length.
//
// ensureCapacity(int min) — гарантирует capacity >= min.
//   Если capacity уже достаточна — ничего не происходит.
//   Если нет — расширяет (обычно до max(min, capacity*2+2)).
//
// АНАЛОГИЯ: std::string::reserve(n) или std::vector::reserve(n) в C++.
StringBuilder sb = new StringBuilder(width + 15);  // строка 339
sb.ensureCapacity(width + 15);                      // строка 346 (демонстрация)
```

### append()

```java
// BattleNarrator.narrateAttack(), строки 147–165
// TextFormatter.buildProgressBar(), строки 351–395
//
// append() — главный метод StringBuilder. Добавляет данные в КОНЕЦ буфера.
// Перегружен для всех типов:
//   append(String)  — строка
//   append(int)     — число (Integer.toString() внутри)
//   append(char)    — один символ (эффективнее, чем append("]"))
//   append(boolean) — "true" или "false"
//   append(Object)  — вызывает obj.toString()
//
// Возвращает this → можно цепочкой (method chaining):
//   sb.append("HP: ").append(health).append("/").append(maxHealth);
//
// АНАЛОГИЯ: оператор << для std::ostringstream.

sb.append('[');                        // добавить char (строка 365)
for (int i = 0; i < width; i++) {
    sb.append(i < filled ? '█' : '░'); // append в цикле — главный паттерн
}
sb.append(']');
```

### setCharAt() и charAt()

```java
// TextFormatter.buildProgressBar(), строки 307–392
//
// charAt(int index) — прочитать символ по индексу.
//   sb.charAt(0) → первый символ. Индексация с 0.
//   Выбросит StringIndexOutOfBoundsException если index < 0 или index >= length().
//   АНАЛОГИЯ С C: str[index] — доступ к char по индексу.
//
// setCharAt(int index, char ch) — заменить символ по индексу «на месте».
//   В отличие от String (immutable), StringBuilder позволяет изменять символы.
//   АНАЛОГИЯ С C: str[index] = ch — прямая запись в массив.
//
//   StringBuilder sb = new StringBuilder("Java");
//   sb.setCharAt(0, 'j');  // sb = "java"

// Если HP критически низкое (< 20%) — меняем первый символ на '!'.
if ((double) current / max < 0.2) {
    sb.setCharAt(1, '!');  // строка 391 (индекс 0 = '[', индекс 1 = первый блок)
}
```

### insert()

```java
// BattleNarrator.demonstrateStringBuffer(), строки 787–789
// BattleNarrator.highlightCritical(), строки 680–770
//
// insert(int offset, String str) — вставить str ПЕРЕД позицией offset.
// Остальные символы сдвигаются вправо.
//
//   StringBuilder sb = new StringBuilder("Hello!");
//   sb.insert(5, " World");  // → "Hello World!"
//
// Перегружен для всех типов (int, char, boolean и т.д.).
// ОСТОРОЖНО: после insert() индексы символов СДВИГАЮТСЯ.

buffer.insert(19, "[");                          // строка 788
buffer.insert(buffer.indexOf("символов."), "] ");  // строка 789
```

### delete() и deleteCharAt()

```java
// BattleNarrator.buildBattleSummary(), строки 602–637
//
// delete(int start, int end) — удалить символы с позиции start до end-1.
//   Оставшиеся символы сдвигаются влево.
//   StringBuilder sb = new StringBuilder("Hello World");
//   sb.delete(5, 11);  // → "Hello"
//
// deleteCharAt(int index) — удалить один символ по индексу.
//   Отличие от delete(): один символ, а не диапазон.
//   sb.deleteCharAt(0)  // удалить первый символ
//
// АНАЛОГИЯ С C: memmove(buf + start, buf + end, остаток) — сдвиг данных в массиве.

int doubleNewline = sb.indexOf("\n\n");
if (doubleNewline >= 0) {
    sb.delete(doubleNewline, doubleNewline + 1);  // строки 616–620
}
```

### replace(start, end, str) в StringBuilder

```java
// BattleNarrator.buildBattleSummary(), строки 575–599
//
// StringBuilder.replace(int start, int end, String str)
//   ОТЛИЧАЕТСЯ от String.replace()!
//
//   StringBuilder: замена по ПОЗИЦИИ (от start до end-1 на str).
//   String:        замена по СОДЕРЖИМОМУ (все вхождения target на replacement).
//
//   StringBuilder sb = new StringBuilder("Hello World!");
//   sb.replace(6, 11, "Java");  // → "Hello Java!"
//
// ОСТОРОЖНО: после replace() индексы СДВИГАЮТСЯ, если новая строка длиннее/короче.

int titleStart = sb.indexOf("ИТОГИ СРАЖЕНИЯ");  // строка 596
int titleEnd   = titleStart + "ИТОГИ СРАЖЕНИЯ".length();
sb.replace(titleStart, titleEnd, "БОЕВОЙ ЖУРНАЛ ");  // строка 599
```

### reverse()

```java
// TextFormatter.reverseText(), строки 730–767
//
// StringBuilder.reverse() — переворачивает содержимое «на месте» (in-place).
//   new StringBuilder("hello").reverse().toString() → "olleh"
//
// Корректно обрабатывает суррогатные пары Unicode (emoji и редкие символы
// переворачиваются как единое целое, а не побуквенно).
//
// АНАЛОГИЯ С C++: std::reverse(str.begin(), str.end()) для std::string.
// Для неизменяемого String в Java аналога нет — только через StringBuilder.

StringBuilder sb = new StringBuilder(text);
sb.reverse();
return sb.substring(0);  // строка 766: substring(0) = копия всего буфера как String
```

### setLength() и substring() в StringBuilder

```java
// BattleNarrator.narrateAttack(), строки 220–270
//
// setLength(int newLength) — изменить длину строки.
//   Если newLength < length() — строка ОБРЕЗАЕТСЯ (как truncate).
//   Если newLength > length() — строка дополняется символами '\u0000'.
//
//   StringBuilder sb = new StringBuilder("Hello World");
//   sb.setLength(5);  // sb = "Hello"
//
// substring(int start) / substring(int start, int end)
//   Аналогичны String.substring(), но возвращают String, а не StringBuilder.
//   Сам StringBuilder НЕ изменяется.
```

### toString()

```java
// GameCharacter.toString(), строки 663–667
// BattleNarrator.narrateAttack() и все методы
//
// toString() — финальный вызов: создаёт ОДНУ неизменяемую строку String
// из содержимого буфера.
//
// Это единственный момент выделения памяти под String.
// Вместо N промежуточных строк при конкатенации через + —
// один StringBuilder + один вызов toString() в конце.
return sb.toString();
```

### StringBuffer — потокобезопасный вариант

```java
// BattleNarrator.demonstrateStringBuffer(), строки 770–828
//
// StringBuffer — то же самое, что StringBuilder, но с synchronized-методами.
// Потокобезопасен: несколько потоков могут вызывать append() одновременно.
//
// Когда использовать StringBuffer?
//   - Несколько потоков пишут в ОДИН буфер одновременно.
//   - Например: параллельный сбор событий из разных потоков в один лог.
//
// Когда НЕ использовать StringBuffer?
//   - В однопоточном коде (95% случаев) — лишние накладные расходы на синхронизацию.
//   - ВСЕГДА предпочитай StringBuilder в однопоточном коде!
//
// ПРАВИЛО: StringBuilder в однопоточном коде. StringBuffer — только при реальной
// многопоточной записи в один объект.
StringBuffer buffer = new StringBuffer("Демо StringBuffer: ");  // строка 780
```

---

## 4. Регулярные выражения (7.4)

**Основной файл:** `StringValidator.java`, строки 117–800

### Pattern.compile() — компиляция паттернов

```java
// StringValidator.java, строки 130–204
//
// Pattern.compile(regex) — ДОРОГАЯ операция: строит конечный автомат из regex.
// Результат — объект Pattern — хранится как static final константа.
// Компиляция происходит ОДИН РАЗ при загрузке класса.
//
// ПРАВИЛО: если regex используется многократно — ВСЕГДА static final Pattern.
// НИКОГДА не вызывай Pattern.compile() внутри цикла или часто вызываемого метода!
//
// String.matches(regex) внутри тоже вызывает Pattern.compile() каждый раз →
// это нормально для одноразовых проверок, но неэффективно в цикле.
//
// АНАЛОГИЯ С C++: static const std::regex pattern("...") — компиляция при первом вызове.

private static final Pattern HERO_NAME_PATTERN = Pattern.compile(
    "^([a-zA-Zа-яА-ЯёЁ]+)([-\\s][a-zA-Zа-яА-ЯёЁ-\\s]*)?$"
);  // строка 171

private static final Pattern CHEAT_CODE_PATTERN = Pattern.compile(
    "^/([a-z_]+)(?:\\s+(\\d+))?$"
);  // строка 192

private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");  // строка 204
```

### Синтаксис регулярных выражений

```
Базовые элементы (объяснены в StringValidator.java, строки 143–204):

  ^   — начало строки (якорь)
  $   — конец строки (якорь)
  .   — любой символ (кроме переноса строки)
  \d  — цифра [0-9]   (в Java: \\d из-за двойного экранирования)
  \w  — слово: [a-zA-Z0-9_]
  \s  — пробельный символ (пробел, \t, \n, \r)
  \D  — не цифра
  \W  — не слово
  \S  — не пробел

  *   — 0 или более повторений (жадный)
  +   — 1 или более повторений (жадный)
  ?   — 0 или 1 повторение (опционально)
  {n} — ровно n повторений
  {n,m} — от n до m повторений

  [abc]  — символьный класс: a, b или c
  [a-z]  — диапазон
  [^a]   — НЕ символ a

  (...)  — группа захвата (group(1), group(2), ...)
  (?:...) — группа БЕЗ захвата (не увеличивает нумерацию)

ДВОЙНОЕ ЭКРАНИРОВАНИЕ В JAVA:
  В Java строки используют \ как escape. Поэтому \d в regex записывается как \\d.
  \\d в коде Java → \d в regex → цифра.
  \\s → \s → пробельный символ.
  \\. → \. → литеральная точка (не «любой символ»).
```

### Pattern.matcher() и Matcher.matches()

```java
// StringValidator.validateHeroName(), строки 285–301
//
// pattern.matcher(input) — создаёт Matcher для данной строки.
// Один Pattern → много Matcher-ов для разных строк.
//
// Matcher.matches() — соответствует ли ВСЯ строка паттерну?
//   Отличие от find(): find() ищет ПОДСТРОКУ, matches() требует полного совпадения.
//
//   Pattern p = Pattern.compile("\\d+");
//   p.matcher("123").matches();    → true  (вся строка = цифры)
//   p.matcher("abc123").matches(); → false (не целиком цифры)
//   p.matcher("abc123").find();    → true  (подстрока "123" найдена!)
//
// АНАЛОГИЯ С C++: std::regex_match vs std::regex_search.

Matcher matcher = HERO_NAME_PATTERN.matcher(name);  // строка 285
if (!matcher.matches()) { ... }                     // строка 297
```

### Matcher.group() — извлечение групп захвата

```java
// StringValidator.validateHeroName(), строки 303–319
//
// После успешного matches() или find() — извлекаем группы:
//   group()  или group(0) — ВСЁ совпадение целиком
//   group(1) — первая пара скобок ()
//   group(2) — вторая пара скобок ()
//   и т.д.
//
// Если группа необязательная (с ?) и не участвовала — group(n) вернёт null.
//
// ЧАСТАЯ ОШИБКА: вызвать group() ДО find()/matches() →
//   IllegalStateException: No match found
//
// АНАЛОГИЯ С C++: std::smatch m; m[1].str() — первая группа.

String firstName = matcher.group(1);  // строка 318 — первое слово имени
String rest      = matcher.group(2);  // строка 319 — фамилия/прозвище (может быть null)
```

### Matcher.find() — поиск всех вхождений

```java
// StringValidator.highlightPattern(), строки 617–644
// StringValidator.extractNumbers(), строки 668–700
//
// while (matcher.find()) — ключевой паттерн для поиска ВСЕХ совпадений в строке.
// Каждый вызов find() перемещает «курсор» к следующему совпадению.
// После успешного find() доступны group(), start(), end().
//
// ПОРЯДОК СТРОГО: find() → group()/start()/end() → find() → ...
// Нельзя вызвать group() без предварительного find()!
//
// АНАЛОГИЯ С C++: do { ... } while (std::regex_search(s, m, re));

while (matcher.find()) {              // строка 624
    String match = matcher.group();   // строка 628 — совпавший текст
    int start    = matcher.start();   // строка 631 — позиция начала
    int end      = matcher.end();     // строка 635 — позиция ПОСЛЕ конца
}
```

### Matcher.start() / end()

```java
// StringValidator.highlightPattern(), строки 630–641
//
// start() — индекс первого символа совпадения в исходной строке.
// end()   — индекс ПОСЛЕ последнего символа совпадения.
//
// Длина совпадения: end() - start() == group().length()
//
//   Строка: "Урон: 42 HP"
//   Паттерн: \d+
//   Совпадение: "42"
//     start() = 6, end() = 8, group() = "42"
```

### Matcher.replaceAll()

```java
// StringValidator.censorBadWords(), строки 452–590
//
// matcher.replaceAll(String replacement) — заменить ВСЕ совпадения на replacement.
// Аналог String.replaceAll(), но через предварительно скомпилированный Pattern.
// Эффективнее String.replaceAll() при многократном вызове.
```

### String.matches() и Pattern.matches()

```java
// StringValidator.validateHeroName(), строки 252–274
//
// String.matches(regex) — удобный метод. Внутри:
//   Pattern.compile(regex).matcher(this).matches()
// Компилирует regex КАЖДЫЙ раз → только для одноразовых проверок.
if (name.matches(".*\\d.*")) { ... }  // строка 261

// Pattern.matches(regex, input) — статический метод, аналогичен String.matches().
// Тот же результат, другой синтаксис. Тоже компилирует каждый раз.
if (Pattern.matches("^\\s*$", name)) { ... }  // строка 274
```

### Switch по строке

```java
// Game.java, handleCheatCode(), строки 3657–3700
//
// Java поддерживает switch по String с Java 7+.
// Внутри использует hashCode() + equals() для сопоставления.
// Регистрозависимо: "Attack" ≠ "attack".
//
// ВАЖНО: если в switch нет default — неизвестная команда будет молча проигнорирована.
// Всегда добавляй default!
switch (cheatResult[1]) {
    case "give_gold" -> { ... }
    case "heal"      -> { ... }
    case "levelup"   -> { ... }
    default          -> { ... }
}
```

---

## 5. Рефакторинг toString() с StringBuilder

**Файл:** `GameCharacter.java`, строки 591–668  
**Файл:** `Enemy.java`, строки 760–824

Два существующих метода `toString()` переписаны с конкатенации `+` на `StringBuilder`. Выходной текст идентичен — демонстрируем разницу подходов.

### GameCharacter.toString() — подробное объяснение

```java
// GameCharacter.java, строки 613–667
//
// ДО рефакторинга (конкатенация через +):
//   return getClassName() + " " + name + " [HP: " + health + "/" + maxHealth + " ATK: " + attack + "]";
//   → ~8 промежуточных String-объектов (один за каждый +)
//
// ПОСЛЕ рефакторинга (StringBuilder):
StringBuilder sb = new StringBuilder(80);  // строка 637: ёмкость 80 → одно выделение памяти
sb.append(getClassName())
  .append(" ")
  .append(name)
  .append(" [HP: ")
  .append(health)       // append(int) — автоматически в строку
  .append("/")
  .append(maxHealth)
  .append(" ATK: ")
  .append(attack)
  .append(" DEF: ")
  .append(defense)
  .append(" LVL: ")
  .append(level)
  .append("]");
return sb.toString();  // строка 667: одна финальная String-аллокация
```

### Enemy.toString() — напоминание

```java
// Enemy.java, строки 793–823
// Аналогичный рефакторинг. Подробное объяснение StringBuilder — см. GameCharacter.toString().
// Здесь добавлен append(char) для ']': append(']') эффективнее, чем append("]").
StringBuilder sb = new StringBuilder();  // строка 806
sb.append(displayName).append(" [HP: ").append(health).append(" ATK: ").append(attack).append(']');
return sb.toString();
```

---

## 6. Интеграция в Game.java

Новые классы вызываются из трёх мест `Game.java`:

### showStringDemo() — демо форматирования (строки 3427–3563)

Пункт 11 меню `betweenBattlesMenu()`. Показывает все возможности `TextFormatter`:

```
TextFormatter.centerText()     — строка 3442 — 7.1: length, repeat
TextFormatter.buildProgressBar() — строка 3454 — 7.3: StringBuilder, setCharAt
TextFormatter.buildFrame()     — строка ~3480  — 7.3: StringBuilder, ensureCapacity
TextFormatter.buildTable()     — строка ~3500  — 7.3: StringBuilder, insert
TextFormatter.scrambleText()   — строка ~3520  — 7.1: toCharArray, new String(char[])
TextFormatter.reverseText()    — строка ~3540  — 7.3: reverse()
BattleNarrator.buildBattleSummary() — строка ~3550 — 7.3: StringBuilder в цикле
BattleNarrator.demonstrateStringBuffer() — строка ~3560 — 7.3: StringBuffer
StringValidator.demonstrateRegexSyntax() — строка ~3562 — 7.4: Pattern, Matcher
```

### tryParseTextCommand() — текстовый ввод (строки 3564–3631)

Вызывается из `heroTurn()` как fallback при нечисловом вводе. Позволяет писать `"атака"` вместо `"1"`:

```java
// Game.java, строки 3564–3631
// CommandParser.parse(input)          — строка 3576 — 7.2: split, strip, toLowerCase
// CommandParser.matchCommand(...)     — строка ~3585 — 7.2: equalsIgnoreCase, regionMatches
```

### handleCheatCode() — чит-коды (строки 3632–3700)

Вызывается из `readHeroName()` и `start()` при вводе строк начинающихся с `/`:

```java
// Game.java, строки 3632–3700
// StringValidator.parseCheatCode(input) — строка 3647 — 7.4: Pattern, Matcher, group
// switch по строке                      — строка 3660 — switch(String), Java 7+
```

### readHeroName() — делегирование валидации (строки 749–792)

```java
// Game.java, строки 749–792
// StringValidator.validateHeroName(name) — строка 772 — 7.4: Pattern + Matcher
// validationResult.startsWith("OK")      — строка 774 — 7.2: startsWith
// validationResult.substring(8)          — строка 779 — 7.1: substring
```

### betweenBattlesMenu() — новые пункты (строки 1190–1307)

```java
// Game.java, строки 1190–1307
// Пункт 11: showStringDemo()  — вызов демо TextFormatter (глава 7.1, 7.3)
// Пункт 12: narrativeMode = !narrativeMode — включение BattleNarrator (глава 7.3)
//           BattleNarrator используется в handleNormalAttack() при narrativeMode = true
```

---

## 7. Новые файлы

| Файл | Строк | Что демонстрирует |
|------|-------|-------------------|
| `TextFormatter.java` | ~768 | Глава 7.1 (length, toCharArray, immutability, repeat) + 7.3 (StringBuilder полностью) |
| `CommandParser.java` | ~490 | Глава 7.2 (все операции со строками: split, strip, equals, indexOf, startsWith, join) |
| `BattleNarrator.java` | ~829 | Глава 7.3 (StringBuilder: append, insert, delete, replace, reverse, setLength; StringBuffer) |
| `StringValidator.java` | ~800 | Глава 7.4 (Pattern, Matcher, matches, find, group, start, end, replaceAll, split) |

### TextFormatter.java — обзор методов

| Метод | Строки | Ключевые конструкты |
|-------|--------|---------------------|
| `centerText(text, width)` | 161–203 | `length()`, `repeat()`, `substring()` |
| `padRight(text, width)` | 211–228 | `length()`, `repeat()` |
| `padLeft(text, width)` | 229–251 | `length()`, `repeat()` |
| `truncate(text, maxLen)` | 252–266 | `length()`, `substring(0, n)` |
| `buildProgressBar(cur, max, w)` | 319–440 | `StringBuilder(cap)`, `ensureCapacity()`, `append()`, `setCharAt()`, `toString()` |
| `buildFrame(title, lines)` | 465–556 | `StringBuilder`, `append()`, `length()`, `repeat()` |
| `buildTable(rows)` | 557–696 | `StringBuilder`, `insert()`, `padRight()` |
| `scrambleText(text)` | 697–728 | `toCharArray()`, `charAt()`, `new String(char[])`, `isBlank()` |
| `reverseText(text)` | 757–767 | `StringBuilder.reverse()`, `substring(0)` |

### CommandParser.java — обзор методов

| Метод | Строки | Ключевые конструкты |
|-------|--------|---------------------|
| `parse(input)` | 140–315 | `strip()`, `split("\\s+")`, `toLowerCase()`, `isEmpty()` |
| `matchCommand(input, aliases...)` | 319–418 | `equalsIgnoreCase()`, `regionMatches()`, `contains()`, `startsWith()` |
| `extractArgument(input)` | 420–456 | `indexOf()`, `substring()`, `strip()` |
| `buildHelp()` | 458–490 | `String.join()` |
| `ParsedCommand.hasArgument()` | 123–138 | `isEmpty()`, `isBlank()` |

### BattleNarrator.java — обзор методов

| Метод | Строки | Ключевые конструкты |
|-------|--------|---------------------|
| `narrateAttack(...)` | 109–261 | `StringBuilder(cap)`, `append()`, `deleteCharAt()`, `setLength()` |
| `narrateHeal(...)` | 262–343 | `StringBuilder`, `append()`, `toString()` |
| `narrateDefeat(...)` | 344–537 | `replace()` (String), `toUpperCase()` |
| `buildBattleSummary(events)` | 538–679 | `StringBuilder` в цикле, `replace()`, `delete()`, `indexOf()` |
| `highlightCritical(text)` | 680–769 | `insert()`, `toUpperCase()`, `StringBuilder` |
| `demonstrateStringBuffer()` | 770–828 | `StringBuffer` (все методы) |

### StringValidator.java — обзор методов

| Метод | Строки | Ключевые конструкты |
|-------|--------|---------------------|
| `validateHeroName(name)` | 243–365 | `String.matches()`, `Pattern.matches()`, `Matcher.matches()`, `group()` |
| `parseCheatCode(input)` | 366–451 | `Pattern.compile()`, `Matcher.matches()`, `group(1)`, `group(2)` |
| `censorBadWords(text, words)` | 452–591 | `Pattern.compile()` в цикле (с объяснением), `Matcher.replaceAll()` |
| `highlightPattern(text, regex)` | 592–651 | `Matcher.find()`, `group()`, `start()`, `end()` |
| `extractNumbers(text)` | 668–720 | `NUMBER_PATTERN`, `Matcher.find()`, `Integer.parseInt(group())` |
| `demonstrateRegexSyntax()` | 729–800 | Сводная демонстрация всего синтаксиса regex |

---

## 8. Сводные таблицы

### Покрытие подглав

| Подглава | Концепция | Файл | Строки |
|----------|-----------|------|--------|
| 7.1 | Неизменяемость String | `TextFormatter.centerText()` | 154–190 |
| 7.1 | String pool, == vs equals | `TextFormatter` (комментарий) | 151–160 |
| 7.1 | `length()` | `TextFormatter.centerText()` | 181 |
| 7.1 | `isEmpty()` / `isBlank()` | `TextFormatter.scrambleText()` | 694–699 |
| 7.1 | `toCharArray()` | `TextFormatter.scrambleText()` | 704 |
| 7.1 | `new String(char[])` | `TextFormatter.scrambleText()` | 727 |
| 7.1 | `substring()` | `TextFormatter.centerText()` | 190 |
| 7.1 | `repeat()` | `TextFormatter.centerText()` | 202 |
| 7.2 | `split("\\s+")` | `CommandParser.parse()` | 222 |
| 7.2 | `strip()` / `stripLeading()` / `stripTrailing()` | `CommandParser.parse()` | 157–181 |
| 7.2 | `toLowerCase()` / `toUpperCase()` | `CommandParser.parse()` | 224 |
| 7.2 | `equals()` / `equalsIgnoreCase()` | `CommandParser.matchCommand()` | 319 |
| 7.2 | `compareTo()` | `CommandParser.matchCommand()` | 399 |
| 7.2 | `indexOf()` / `lastIndexOf()` | `CommandParser.extractArgument()` | 420 |
| 7.2 | `contains()` / `startsWith()` / `endsWith()` | `CommandParser.matchCommand()` | 380 |
| 7.2 | `replace()` | `BattleNarrator.narrateDefeat()` | 344 |
| 7.2 | `concat()` | `CommandParser.parse()` (комментарий) | 290 |
| 7.2 | `regionMatches()` | `CommandParser.matchCommand()` | 380 |
| 7.2 | `String.join()` | `CommandParser.buildHelp()` | 458 |
| 7.2 | `getChars()` | `BattleNarrator.narrateAttack()` | 200 |
| 7.3 | `new StringBuilder(cap)` | `BattleNarrator.narrateAttack()` | 147 |
| 7.3 | `capacity()` / `ensureCapacity()` | `TextFormatter.buildProgressBar()` | 284–346 |
| 7.3 | `append()` | `BattleNarrator.narrateAttack()` | 149 |
| 7.3 | `insert()` | `BattleNarrator.highlightCritical()` | 680 |
| 7.3 | `delete()` / `deleteCharAt()` | `BattleNarrator.buildBattleSummary()` | 602 |
| 7.3 | `replace(start, end, str)` | `BattleNarrator.buildBattleSummary()` | 575 |
| 7.3 | `reverse()` | `TextFormatter.reverseText()` | 757 |
| 7.3 | `charAt()` / `setCharAt()` | `TextFormatter.buildProgressBar()` | 374–391 |
| 7.3 | `substring()` в StringBuilder | `TextFormatter.reverseText()` | 766 |
| 7.3 | `setLength()` | `BattleNarrator.narrateAttack()` | 220 |
| 7.3 | `toString()` | `GameCharacter.toString()` | 667 |
| 7.3 | `StringBuffer` (vs StringBuilder) | `BattleNarrator.demonstrateStringBuffer()` | 770 |
| 7.4 | `Pattern.compile()` | `StringValidator` (константы) | 171–204 |
| 7.4 | `Pattern.matches()` | `StringValidator.validateHeroName()` | 274 |
| 7.4 | `Pattern.split()` | `StringValidator.demonstrateRegexSyntax()` | 729 |
| 7.4 | `Matcher.matches()` | `StringValidator.validateHeroName()` | 297 |
| 7.4 | `Matcher.find()` | `StringValidator.highlightPattern()` | 624 |
| 7.4 | `Matcher.group()` | `StringValidator.validateHeroName()` | 318 |
| 7.4 | `Matcher.start()` / `end()` | `StringValidator.highlightPattern()` | 631 |
| 7.4 | `Matcher.replaceAll()` | `StringValidator.censorBadWords()` | 452 |
| 7.4 | `String.matches()` | `StringValidator.validateHeroName()` | 261 |
| 7.4 | `String.replaceAll()` | `BattleNarrator.narrateDefeat()` | 400 |
| 7.4 | Синтаксис regex: `\d`, `\w`, `\s`, `*`, `+`, `{n}`, `[]`, `()` | `StringValidator` (комментарии) | 143–204 |
| 7.4 | Группы захвата `()` vs `(?:...)` | `StringValidator` (CHEAT_CODE_PATTERN) | 175–194 |
| 7.4 | switch по строке (Java 7+) | `Game.handleCheatCode()` | 3657 |

### Утилитный класс — паттерн (ADR-001)

Все четыре новых класса следуют одному паттерну:

```java
// Паттерн "utility class" в Java:
public final class ИмяКласса {  // final — запрет наследования (3.23)
    private ИмяКласса() { }     // private конструктор — запрет new
    public static ... метод() { ... }  // только static-методы
}
// Примеры из JDK: java.lang.Math, java.util.Arrays, java.util.Collections.
```

### String vs StringBuilder — когда что использовать

| Ситуация | Что использовать |
|----------|-----------------|
| Хранение текста, сравнение, передача | `String` (immutable, безопасно) |
| 2–3 склейки вне цикла | `+` (компилятор оптимизирует сам) |
| 3+ склейки или цикл | `StringBuilder` |
| Конкурентная запись из нескольких потоков | `StringBuffer` |
| Одноразовая проверка по regex | `String.matches()` |
| Многократная проверка по regex | `static final Pattern` + `Matcher` |
