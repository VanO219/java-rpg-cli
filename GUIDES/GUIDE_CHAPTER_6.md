# Глава 6: Потоки ввода-вывода (6.1–6.13)

Эта глава демонстрирует систему ввода-вывода Java через реальные игровые сценарии:
сохранение игры, экспорт журнала боёв, ZIP-архивирование бэкапов и Console API.

---

## Содержание

1. [Иерархия потоков](#1-иерархия-потоков-61)
2. [Паттерн Декоратор](#2-паттерн-декоратор)
3. [Try-with-resources — закрытие потоков](#3-try-with-resources--закрытие-потоков-62)
4. [FileInputStream / FileOutputStream](#4-fileinputstream--fileoutputstream-63)
5. [ByteArrayInputStream / ByteArrayOutputStream](#5-bytearrayinputstream--bytearrayoutputstream-64)
6. [BufferedInputStream / BufferedOutputStream](#6-bufferedinputstream--bufferedoutputstream-65)
7. [PrintStream / PrintWriter](#7-printstream--printwriter-66)
8. [DataOutputStream / DataInputStream](#8-dataoutputstream--datainputstream-67)
9. [FileReader / FileWriter](#9-filereader--filewriter-68)
10. [BufferedReader / BufferedWriter](#10-bufferedreader--bufferedwriter-69)
11. [Сериализация — Serializable](#11-сериализация--serializable-610)
12. [Класс File](#12-класс-file-611)
13. [ZIP-архивы](#13-zip-архивы-612)
14. [Класс Console](#14-класс-console-613)
15. [Сводные таблицы](#15-сводные-таблицы)

---

## 1. Иерархия потоков (6.1)

В Java весь ввод-вывод построен на системе **потоков (streams)**. Поток — это последовательность данных, текущих от источника к приёмнику. Потоки делятся на два семейства: байтовые и символьные.

### Байтовые потоки (работают с `byte`, 8 бит)

Используются для **бинарных данных**: изображения, сериализованные объекты, `.dat`-файлы.

```
InputStream (абстрактный — корень чтения байтов)
│
├── FileInputStream          — чтение из ФАЙЛА (6.3)
│     Открывает файл и читает побайтово.
│     new FileInputStream("data.bin") или new FileInputStream(file)
│
├── ByteArrayInputStream     — чтение из МАССИВА БАЙТОВ в памяти (6.4)
│     Полезен для тестов или когда данные уже есть в byte[].
│
├── FilterInputStream        — абстрактная основа для «обёрток» (6.5)
│     Сам по себе ничего не добавляет, только делегирует вложенному потоку.
│     │
│     ├── BufferedInputStream  — БУФЕРИЗАЦИЯ чтения (6.5)
│     │     Внутренний буфер 8 КБ. Вместо чтения по 1 байту — читает блоками.
│     │     new BufferedInputStream(new FileInputStream("file"))
│     │
│     └── DataInputStream      — чтение ПРИМИТИВНЫХ типов (6.7)
│           Добавляет: readInt(), readLong(), readDouble(), readUTF(), readBoolean()
│
└── ObjectInputStream        — чтение ОБЪЕКТОВ через десериализацию (6.10)
      readObject() воссоздаёт объект из байтов.
      Прямой наследник InputStream, не FilterInputStream.

OutputStream (абстрактный — корень записи байтов)
│
├── FileOutputStream         — запись в ФАЙЛ (6.3)
│     new FileOutputStream("data.bin")        — перезапись
│     new FileOutputStream("data.bin", true)  — дозапись (append)
│
├── ByteArrayOutputStream    — запись в МАССИВ БАЙТОВ в памяти (6.4)
│     Для формирования данных в памяти перед записью на диск.
│
├── FilterOutputStream       — абстрактная основа для «обёрток» записи (6.5)
│     │
│     ├── BufferedOutputStream — БУФЕРИЗАЦИЯ записи (6.5)
│     │     Накапливает в буфере, записывает блоками. ВАЖНО: вызвать flush() или close()!
│     │
│     ├── DataOutputStream     — запись ПРИМИТИВНЫХ типов (6.7)
│     │     Добавляет: writeInt(), writeLong(), writeDouble(), writeUTF(), writeBoolean()
│     │
│     └── PrintStream          — форматированный вывод (6.6)
│           System.out — это PrintStream! Методы: print(), println(), printf()
│
└── ObjectOutputStream       — запись ОБЪЕКТОВ через сериализацию (6.10)
      writeObject(obj) преобразует объект в байты.
      Прямой наследник OutputStream, не FilterOutputStream.
```

### Символьные потоки (работают с `char`, 16 бит, Unicode)

Используются для **текстовых данных**: `.txt`, `.csv`, `.log`-файлы. Автоматически учитывают кодировку (UTF-8, UTF-16 и т.д.).

```
Reader (абстрактный — корень чтения символов)
│
├── InputStreamReader  — мост: InputStream → Reader (конвертирует byte → char)
│     │
│     └── FileReader   — чтение из текстового ФАЙЛА (6.8)
│
└── BufferedReader     — БУФЕРИЗАЦИЯ + метод readLine() (6.9)
      new BufferedReader(new FileReader("text.txt"))

Writer (абстрактный — корень записи символов)
│
├── OutputStreamWriter — мост: OutputStream → Writer (конвертирует char → byte)
│     │
│     └── FileWriter   — запись в текстовый ФАЙЛ (6.8)
│
├── BufferedWriter     — БУФЕРИЗАЦИЯ + метод newLine() (6.9)
│     new BufferedWriter(new FileWriter("text.txt"))
│
└── PrintWriter        — форматированный текстовый вывод (6.6)
      Символьный аналог PrintStream. Методы: println(), printf(), format()
```

### Правило выбора потока

| Данные | Поток |
|--------|-------|
| Текст (`.txt`, `.log`, `.csv`) | `Reader` / `Writer` |
| Бинарные данные (`.dat`, изображения) | `InputStream` / `OutputStream` |
| Примитивы Java (`int`, `long`, `double`) | `DataInputStream` / `DataOutputStream` |
| Целые объекты Java | `ObjectInputStream` / `ObjectOutputStream` |
| ZIP-архивы | `ZipInputStream` / `ZipOutputStream` |

---

## 2. Паттерн Декоратор

Потоки Java построены на классическом паттерне **«Декоратор» (Decorator)**:

- **Базовый поток** (`FileInputStream`) предоставляет основную функциональность — физическое чтение данных.
- **Обёртки** (`BufferedInputStream`, `DataInputStream`) **добавляют** возможности, принимая другой поток через конструктор.
- Каждый слой расширяет предыдущий, не изменяя его.

### Как читать цепочку

Цепочку читают **изнутри наружу** — самый внутренний поток самый «близкий к железу»:

```java
// Запись: DataOutputStream → BufferedOutputStream → FileOutputStream → файл на диске
DataOutputStream dos = new DataOutputStream(
    new BufferedOutputStream(
        new FileOutputStream("saves/hero.dat")));
//  ^--- внешний слой: методы writeInt(), writeUTF()
//            ^--- средний слой: буферизация (запись блоками 8 КБ)
//                     ^--- внутренний слой: запись на диск
```

```java
// Чтение: DataInputStream → BufferedInputStream → FileInputStream → файл на диске
DataInputStream dis = new DataInputStream(
    new BufferedInputStream(
        new FileInputStream("saves/hero.dat")));
```

```java
// Текст: PrintWriter → BufferedWriter → FileWriter → файл на диске
PrintWriter pw = new PrintWriter(
    new BufferedWriter(
        new FileWriter("saves/battle_log_detailed.txt")));
```

```java
// ZIP: ZipOutputStream → BufferedOutputStream → FileOutputStream → .zip файл
ZipOutputStream zos = new ZipOutputStream(
    new BufferedOutputStream(
        new FileOutputStream("saves/hero_backup.zip")));
```

### Закрытие каскадное

Закрытие **внешнего** потока автоматически закрывает **все вложенные**. Поэтому в `try-with-resources` достаточно объявить только внешний поток.

---

## 3. Try-with-resources — закрытие потоков (6.2)

**Файл:** `GameSaveManager.java`, строки 439–607 (метод `saveBinary`)

### Проблема без try-with-resources

```java
// Старый способ — нужен finally для гарантированного закрытия
DataOutputStream dos = null;
try {
    dos = new DataOutputStream(new FileOutputStream("file.dat"));
    dos.writeInt(42);
} catch (IOException e) {
    e.printStackTrace();
} finally {
    if (dos != null) {
        try { dos.close(); } catch (IOException e) { /* вложенный try! */ }
    }
}
```

### Новый способ — try-with-resources (Java 7+)

```java
// Из GameSaveManager.java — saveBinary(), строка 439
//
// try (ресурс = new ...) { ... }
// Ресурс ДОЛЖЕН реализовывать AutoCloseable (метод close()).
// Все потоки ввода-вывода реализуют AutoCloseable.
//
// При выходе из блока — нормально ИЛИ по исключению — close() вызывается автоматически.
// Закрытие DataOutputStream каскадно закрывает BufferedOutputStream и FileOutputStream.
try (DataOutputStream dos = new DataOutputStream(
        new BufferedOutputStream(
                new FileOutputStream(saveFile)))) {
    dos.writeInt(hero.getHealth());
    dos.writeUTF(hero.getName());
    // ...
} catch (IOException e) {
    System.out.println("[Ошибка] Не удалось сохранить игру: " + e.getMessage());
}
```

### Несколько ресурсов

```java
// Из BattleLogExporter.java — строка 523
// Два ресурса через точку с запятой.
// Закрываются в ОБРАТНОМ порядке: сначала bw, потом fw.
try (FileWriter fw = new FileWriter(filePath);
     BufferedWriter bw = new BufferedWriter(fw)) {
    bw.write(content);
}
```

### Главное правило

> Никогда не открывай поток без `try-with-resources`. Буфер не сброшен → данные не записаны → файл повреждён.

---

## 4. FileInputStream / FileOutputStream (6.3)

**Файл:** `GameSaveManager.java` — используются в цепочках декораторов  
**Файл:** `SaveArchiver.java` — строки 295, 457 (чтение файлов для ZIP)

Самые базовые потоки для работы с файлами. Открывают файл и читают/пишут побайтово.

### FileOutputStream — запись

```java
// Из GameSaveManager.java — saveBinary(), строка 441
// new FileOutputStream(saveFile) — создаёт файл (или перезаписывает существующий).
// Передаётся во внутрь цепочки декораторов.
new FileOutputStream(saveFile)

// FileOutputStream(file, true) — режим дозаписи (append).
// Данные добавляются В КОНЕЦ файла, а не перезаписывают его.
new FileOutputStream("log.txt", true)
```

### FileInputStream — чтение

```java
// Из SaveArchiver.java — exportToZip(), строка 295
// Открывает каждый файл героя для чтения и помещает содержимое в ZIP.
try (var fis = new FileInputStream(file)) {
    int bytesRead;
    // Паттерн «чтение блоками»: читаем до buffer.length байт за раз.
    // read() возвращает ФАКТИЧЕСКОЕ число прочитанных байт, или -1 при EOF.
    while ((bytesRead = fis.read(buffer)) != -1) {
        zos.write(buffer, 0, bytesRead);
    }
}
```

**Важно:** `FileNotFoundException` (подкласс `IOException`) возникает, если файл не найден при открытии для чтения.

---

## 5. ByteArrayInputStream / ByteArrayOutputStream (6.4)

**Файл:** `BattleLogExporter.java`, строки 385–498 (метод `exportBattleLog`, шаг 1)

`ByteArrayOutputStream` — поток, который пишет данные **в массив байтов в памяти** (а не на диск). Аналог `StringBuilder`, но для байтов.

### Зачем формировать данные в памяти?

1. Собрать данные из разных источников (заголовок + журнал + статистика) в один буфер.
2. Использовать один текст для разных целей (файл, консоль, сеть).
3. Атомарность: если что-то пошло не так при формировании — файл не будет создан.

### Пример из кода

```java
// Из BattleLogExporter.java — строки 385–498
//
// ByteArrayOutputStream — запись в память.
// PrintStream оборачивает его: даёт удобные методы println/printf.
String content;
try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(baos)) {

    ps.println("╔══════════════════════════════════╗");
    ps.println("║     ЖУРНАЛ БОЁВ — ЭКСПОРТ        ║");
    ps.printf("║  Герой: %s%n", heroName);

    // Перебор журнала боёв
    int lineNumber = 1;
    for (String entry : battleLog) {
        ps.printf("  %4d. %s%n", lineNumber++, entry);
    }

    ps.flush(); // сбрасываем буфер PrintStream в ByteArrayOutputStream

    // toString() — преобразует накопленные байты в строку.
    // toByteArray() — возвращает КОПИЮ внутреннего массива байтов.
    // size() — текущий размер данных в буфере.
    content = baos.toString();
    byte[] rawBytes = baos.toByteArray(); // демонстрация API
}
// Теперь content — полный текст в памяти, готов для записи в файл.
```

**Ловушка:** `baos.toString()` использует кодировку платформы (может быть не UTF-8 на Windows). Надёжнее: `baos.toString(StandardCharsets.UTF_8)`.

---

## 6. BufferedInputStream / BufferedOutputStream (6.5)

**Файл:** `GameSaveManager.java` — во всех цепочках потоков  
**Файл:** `SaveArchiver.java` — строки 250, 376

Буферизация кардинально улучшает производительность при большом количестве мелких операций.

### Зачем нужен буфер?

| Без буферизации | С буферизацией |
|----------------|----------------|
| Каждый `write(4 байта)` = системный вызов ОС | Данные копятся в буфере (8 КБ) |
| 1000 полей → 1000 системных вызовов | 1000 полей → 1–2 системных вызова |
| Медленно (переключение режимов ядро/юзер-спейс) | В ~100 раз быстрее |

### Использование в игре

```java
// Из GameSaveManager.java — saveBinary(), строка 439
// BufferedOutputStream — средний слой в цепочке:
//   DataOutputStream → BufferedOutputStream → FileOutputStream
try (DataOutputStream dos = new DataOutputStream(
        new BufferedOutputStream(          // ← буфер 8 КБ
                new FileOutputStream(saveFile)))) {
    dos.writeInt(hero.getHealth());
    dos.writeInt(hero.getMaxHealth());
    // ... ещё 50+ вызовов writeInt/writeUTF
    // Все данные копятся в буфере, на диск запишутся при close()
}
```

```java
// Из SaveArchiver.java — importFromZip(), строка 376
// BufferedInputStream — буферизация чтения ZIP-файла:
try (var zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {
    // zis.read() читает из буфера (быстро), буфер пополняется с диска (реже)
}
```

**Важно:** при `close()` буфер автоматически сбрасывается (`flush`). Если не закрыть поток — последние данные **не попадут на диск**.

---

## 7. PrintStream / PrintWriter (6.6)

**Файл:** `BattleLogExporter.java`, строки 385–678 (шаги 1 и 3)

Оба класса предоставляют удобные методы форматированного вывода. Разница в «родословной»:

| | `PrintStream` | `PrintWriter` |
|-|---------------|---------------|
| Основа | `OutputStream` (байтовый) | `Writer` (символьный) |
| Типичное использование | `System.out`, `ByteArrayOutputStream` | Текстовые файлы, `BufferedWriter` |
| Исключения | Не бросает `IOException` | Не бросает `IOException` |
| Проверка ошибок | `checkError()` | `checkError()` |

### PrintStream — шаг 1: запись в память

```java
// Из BattleLogExporter.java — строка 386
// PrintStream оборачивает ByteArrayOutputStream — пишем в память, а не в файл.
try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
     PrintStream ps = new PrintStream(baos)) {

    // println(x) — вывод строки с переводом строки (\n)
    ps.println("ЖУРНАЛ БОЁВ");

    // printf(format, args) — форматированный вывод (как в языке C)
    // %s — строка, %d — целое, %f — дробное, %n — перенос строки
    ps.printf("Герой: %s%n", heroName);

    // format(format, args) — СИНОНИМ printf()
    // %-25s — влево, минимум 25 символов; %d — целое число
    ps.format("  %-25s %d%n", "Нанесено урона:", totalDamageDealt);

    ps.flush();
    content = baos.toString();
}
```

### PrintWriter — шаг 3: запись в файл с форматированием

```java
// Из BattleLogExporter.java — строка 581
// Цепочка: PrintWriter → BufferedWriter → FileWriter → файл
// autoFlush = false (по умолчанию): flush только при close()
try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(detailedFilePath)))) {

    pw.println("ДЕТАЛЬНЫЙ ОТЧЁТ");
    pw.printf("Герой: %s%n", heroName);

    // Метод chaining (цепочка вызовов): format() возвращает тот же PrintWriter
    pw.format("  %-30s %d%n", "Нанесено урона:", totalDamageDealt)
      .format("  %-30s %d%n", "Получено урона:", totalDamageReceived);

    // ВАЖНО: PrintWriter не бросает IOException при записи!
    // Если диск переполнен — ошибка «проглатывается» молча.
    // Единственный способ проверить: checkError()
    if (pw.checkError()) {
        System.out.println("Ошибка при записи!");
    }
}
```

### Спецификаторы формата printf/format

| Спецификатор | Значение | Пример |
|--------------|----------|--------|
| `%s` | строка | `"Артас"` |
| `%d` | целое число | `42` |
| `%f`, `%.2f` | дробное (2 знака) | `3.14` |
| `%n` | перенос строки (платформенный) | `\n` или `\r\n` |
| `%%` | символ `%` | `100%` |
| `%-25s` | влево, минимум 25 символов | `"текст         "` |
| `%+d` | знак всегда | `+42` или `-3` |
| `%06d` | дополнить нулями до 6 символов | `000042` |
| `%4d` | вправо, минимум 4 символа | `"  42"` |

---

## 8. DataOutputStream / DataInputStream (6.7)

**Файл:** `GameSaveManager.java`, строки 399–827 (методы `saveBinary` и `loadBinary`)

Позволяют записывать и читать **примитивные Java-типы** (`int`, `long`, `double`, `boolean`, `byte`) и строки в бинарном формате.

### Методы DataOutputStream

| Метод | Байт | Описание |
|-------|------|----------|
| `writeInt(int)` | 4 | Целое число в формате big-endian |
| `writeLong(long)` | 8 | Длинное целое |
| `writeDouble(double)` | 8 | Дробное число (IEEE 754) |
| `writeUTF(String)` | 2 + N | 2 байта длины + строка в Modified UTF-8 |
| `writeByte(int)` | 1 | Один байт (младшие 8 бит) |
| `writeBoolean(boolean)` | 1 | `true` → 1, `false` → 0 |

### Запись состояния игры

```java
// Из GameSaveManager.java — saveBinary(), строки 456–594
//
// КРИТИЧЕСКИ ВАЖНО: порядок записи ФИКСИРУЕТ формат файла.
// loadBinary() обязан читать поля СТРОГО в том же порядке!
try (DataOutputStream dos = new DataOutputStream(
        new BufferedOutputStream(
                new FileOutputStream(saveFile)))) {

    // Базовые поля героя
    dos.writeUTF(hero.getClassName());   // "Воин", "Маг", "Лучник"
    dos.writeUTF(hero.getName());
    dos.writeInt(hero.getHealth());
    dos.writeInt(hero.getMaxHealth());
    dos.writeInt(hero.getAttack());
    dos.writeInt(hero.getDefense());
    dos.writeInt(hero.getLevel());
    dos.writeInt(hero.getExperience());

    // Специфичные поля подклассов (pattern matching instanceof, Java 16+)
    if (hero instanceof Warrior w) {
        dos.writeInt(w.getRage());         // 4 байта
    } else if (hero instanceof Mage m) {
        dos.writeInt(m.getMana());         // 4 байта
    } else if (hero instanceof Archer a) {
        dos.writeDouble(a.getCritChance()); // 8 байт (double)
    }

    // Инвентарь: сначала размер, потом каждый элемент
    dos.writeInt(inventory.getCapacity());
    dos.writeInt(inventory.getSize());
    for (int i = 0; i < inventory.getSize(); i++) {
        dos.writeUTF(item.getName());
        dos.writeInt(item.getValue());
        dos.writeInt(slot.getQuantity());
    }

    // Enum сохраняем через name() — возвращает имя константы строкой
    // "EXPLORING" → GameState.valueOf("EXPLORING") при загрузке
    dos.writeUTF(game.getGameState().name());
    dos.writeByte(game.getStatusFlags());

    // Статистика: 6 счётчиков подряд
    dos.writeInt(game.getTotalDamageDealt());
    dos.writeInt(game.getTotalDamageReceived());
    dos.writeInt(game.getEnemiesDefeated());
    dos.writeInt(game.getTotalHealing());
    dos.writeInt(game.getSpecialAttackCount());
    dos.writeInt(game.getLootItemsCollected());

    // Коллекции: паттерн «сначала размер, потом элементы»
    dos.writeInt(achievements.size());
    for (Achievement a : achievements) {
        dos.writeUTF(a.name()); // имя enum-константы
    }
    // ... аналогично для бестиария, таблицы рекордов, журнала квестов
}
```

### Чтение состояния игры

```java
// Из GameSaveManager.java — loadBinary(), строки 657–826
// Цепочка зеркальна к записи: DataInputStream → BufferedInputStream → FileInputStream
try (DataInputStream dis = new DataInputStream(
        new BufferedInputStream(
                new FileInputStream(saveFile)))) {

    // Читаем СТРОГО в том же порядке, что писали!
    String heroType = dis.readUTF();
    String heroName = dis.readUTF();
    int health = dis.readInt();
    int maxHealth = dis.readInt();
    // ...

    // switch-expression (Java 14+): определяем подкласс и читаем нужный тип
    switch (heroType) {
        case "Воин"  -> rage = dis.readInt();
        case "Маг"   -> mana = dis.readInt();
        case "Лучник" -> critChance = dis.readDouble();
    }

    // Коллекции: сначала узнаём размер, потом читаем N элементов
    int achievementCount = dis.readInt();
    for (int i = 0; i < achievementCount; i++) {
        achievements.add(Achievement.valueOf(dis.readUTF()));
    }
} catch (EOFException e) {
    // Файл закончился раньше, чем прочитаны все поля — файл повреждён
} catch (IllegalArgumentException e) {
    // enum.valueOf() не нашёл константу — файл от другой версии игры
}
```

**Главная ловушка:** если порядок чтения не совпадает с порядком записи — получаем **мусорные данные** или исключение. `DataStream` не хранит метаданные о типах — только сырые байты.

---

## 9. FileReader / FileWriter (6.8)

**Файл:** `BattleLogExporter.java`, строки 523–563 (шаг 2), строки 703–728 (шаг 4)

`FileReader` и `FileWriter` — символьные потоки для работы с **текстовыми файлами**. Конвертируют `byte ↔ char` с учётом кодировки платформы.

```java
// Из BattleLogExporter.java — строка 523
// FileWriter — записывает символы в файл.
// Каждый write() — системный вызов (медленно без буферизации).
// Поэтому FileWriter почти всегда оборачивается в BufferedWriter.
try (FileWriter fw = new FileWriter(filePath);
     BufferedWriter bw = new BufferedWriter(fw)) {
    bw.write(content);
    bw.flush();
}
```

```java
// Из BattleLogExporter.java — строка 703
// FileReader — читает символы из файла.
// Без BufferedWriter нет метода readLine() — только read() по одному символу.
// Поэтому оборачиваем в BufferedReader.
try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
    // ...
}
```

**Ловушка кодировки:** `FileReader`/`FileWriter` используют кодировку платформы (на Windows это часто `Windows-1251`, не UTF-8). Русские символы могут «сломаться» при переносе файла между системами. Для надёжности: `new FileWriter(file, StandardCharsets.UTF_8)` (Java 11+).

---

## 10. BufferedReader / BufferedWriter (6.9)

**Файл:** `BattleLogExporter.java`, строки 523–728 (шаги 2 и 4)

Добавляют буферизацию поверх `FileReader`/`FileWriter`. Ключевое преимущество `BufferedReader` — метод `readLine()`.

### BufferedWriter — запись

```java
// Из BattleLogExporter.java — строки 523–563
// Цепочка: BufferedWriter → FileWriter → файл
// BufferedWriter.write(String) — копирует строку в буфер char[8192].
// Данные записываются на диск при заполнении буфера или вызове flush()/close().
try (FileWriter fw = new FileWriter(filePath);
     BufferedWriter bw = new BufferedWriter(fw)) {

    bw.write(content);  // вся строка в буфер

    // flush() — принудительный сброс буфера на диск прямо сейчас.
    // Здесь избыточен (close() сделает то же самое автоматически),
    // но показываем в учебных целях.
    bw.flush();
}
// При выходе из try-with-resources: bw.close() → flush() → данные на диске.
```

### BufferedReader — чтение строка за строкой

```java
// Из BattleLogExporter.java — строки 703–728
// Цепочка: BufferedReader → FileReader → файл
try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
    String line;
    int linesRead = 0;

    // Идиоматический паттерн чтения файла:
    //   (line = br.readLine()) — присваивание в условии цикла
    //   readLine() возвращает null при EOF (конец файла)
    //   Читаем не более VERIFICATION_LINES строк
    while ((line = br.readLine()) != null && linesRead < VERIFICATION_LINES) {
        System.out.println("  " + line);
        linesRead++;
    }
}
```

**Ключевые методы:**

| Класс | Метод | Описание |
|-------|-------|----------|
| `BufferedWriter` | `write(String)` | Записывает строку в буфер |
| `BufferedWriter` | `newLine()` | Платформенный перенос строки |
| `BufferedWriter` | `flush()` | Принудительный сброс буфера |
| `BufferedReader` | `readLine()` | Читает одну строку, `null` при EOF |
| `BufferedReader` | `lines()` | Возвращает `Stream<String>` строк файла |

---

## 11. Сериализация — Serializable (6.10)

**Файл:** `GameSaveManager.java`, строки 829–1006 (методы `saveObject` и `loadObject`)  
**Файлы с `implements Serializable`:** `Game.java`, `GameCharacter.java`, `Warrior.java`, `Mage.java`, `Archer.java`, `Enemy.java`, `Inventory.java`, `Bestiary.java`, `BattleRecord.java`

**Сериализация** — автоматическое преобразование объекта и всего графа зависимостей в байты. Один вызов `writeObject(game)` рекурсивно сохраняет все поля.

### Требования к классу

1. Класс реализует маркерный интерфейс `java.io.Serializable`.
2. Объявлен `serialVersionUID` — версия класса для проверки совместимости.
3. Все поля тоже `Serializable` — или помечены `transient`.

```java
// Из Game.java — строки 125, 141
// implements Serializable — маркерный интерфейс (без методов!).
// Сигнал JVM: «разрешено сериализовать объекты этого класса».
// Без него ObjectOutputStream.writeObject(game) бросит NotSerializableException.
public class Game implements Serializable {

    // serialVersionUID — уникальный идентификатор версии класса.
    // При десериализации JVM сравнивает UID из файла с UID класса.
    // Если они не совпадают → InvalidClassException.
    // Если не объявить — JVM генерирует UID сам (нестабильно, меняется при изменении класса!).
    private static final long serialVersionUID = 1L;
```

### transient — поля, которые не нужно сохранять

```java
// Из Game.java — строки 212–400
//
// transient — модификатор, запрещающий сериализацию поля.
// При writeObject() поля transient ПРОПУСКАЮТСЯ.
// После readObject() они равны null / 0 / false.

// Scanner не Serializable (связан с System.in — ресурсом ОС).
// После загрузки пересоздаётся в reinitializeTransients().
private transient Scanner scanner;

// Callback-объект: создаётся заново при каждом запуске.
private transient BattleEventListener listener;

// Журнал боёв начинается заново каждую сессию.
private transient List<String> battleLog;

// Таблица лута — статические данные, зашитые в коде. Нет смысла сохранять.
private transient Map<EnemyRank, List<LootDrop>> lootTable;

// Стек отмены — сессионный, начинается заново.
private transient ArrayDeque<String> undoStack;
```

### saveObject — запись объекта

```java
// Из GameSaveManager.java — saveObject(), строки 876–911
// Цепочка: ObjectOutputStream → BufferedOutputStream → FileOutputStream
try (ObjectOutputStream oos = new ObjectOutputStream(
        new BufferedOutputStream(
                new FileOutputStream(saveFile)))) {

    // writeObject(game) — сохраняет ВСЕ не-transient не-static поля,
    // рекурсивно проходя весь граф объектов (hero, inventory, bestiary...).
    // Полиморфизм сохраняется: если hero — Warrior, запишется именно Warrior.
    oos.writeObject(game);
}
```

### loadObject — чтение объекта

```java
// Из GameSaveManager.java — loadObject(), строки 944–1006
// Цепочка: ObjectInputStream → BufferedInputStream → FileInputStream
try (ObjectInputStream ois = new ObjectInputStream(
        new BufferedInputStream(
                new FileInputStream(saveFile)))) {

    // readObject() возвращает Object — нужно приведение типа.
    Object obj = ois.readObject();

    // Pattern matching instanceof (Java 16+): проверка типа и создание переменной.
    if (obj instanceof Game loadedGame) {
        // ВАЖНО: конструктор НЕ вызывается при десериализации!
        // transient-поля = null. Нужно пересоздать их вручную.
        loadedGame.reinitializeTransients();
        return loadedGame;
    }
} catch (InvalidClassException e) {
    // serialVersionUID в файле ≠ serialVersionUID в классе.
    // Класс был изменён после сохранения.
} catch (ClassNotFoundException e) {
    // Класс из файла не найден в classpath.
    // Например: сохранили объект Paladin, потом удалили Paladin.java.
}
```

### Сравнение: DataOutputStream vs ObjectOutputStream

| | `DataOutputStream` | `ObjectOutputStream` |
|-|-------------------|---------------------|
| Запись | Вручную каждое поле | Один `writeObject()` |
| Чтение | Вручную в том же порядке | Один `readObject()` |
| Размер файла | Компактный | Больше (метаданные классов) |
| Версионирование | Ручное (порядок полей) | `serialVersionUID` |
| Полиморфизм | Ручной (`switch` по типу) | Автоматический |
| Совместимость | Любой язык может прочитать | Только Java |
| Подходит для | Протоколы, компактность | Автосохранение, граф объектов |

---

## 12. Класс File (6.11)

**Файл:** `GameSaveManager.java`, строки 291–361 (поля, конструктор, `ensureSavesDir`)  
**Файл:** `GameSaveManager.java`, строки 1009–1116 (методы `listSaves`, `displaySavesList`)  
**Файл:** `BattleLogExporter.java`, строки 328–331  
**Файл:** `SaveArchiver.java`, строки 203–237

`File` — это **путь** к файлу или директории в файловой системе. Сам по себе он **не открывает файл** и не читает его содержимое.

### Создание объекта File

```java
// Из GameSaveManager.java — строка 310
// new File("saves") — относительный путь от рабочей директории программы.
// НЕ создаёт директорию на диске!
this.savesDirectory = new File(SAVES_DIR);

// Конструктор с родителем и потомком — безопасное объединение путей:
// new File(savesDirectory, "hero.dat") → "saves/hero.dat"
File saveFile = new File(savesDirectory, fileName);
```

### Проверки и создание директорий

```java
// Из GameSaveManager.java — ensureSavesDir(), строки 338–361
// exists()      — существует ли файл/директория?
// isDirectory() — это именно директория (а не файл)?
// mkdirs()      — создать директорию И все промежуточные (как mkdir -p в Linux)
//   mkdir() (без s) — создаёт только одну директорию (без промежуточных)
if (savesDirectory.exists()) {
    if (!savesDirectory.isDirectory()) {
        System.out.println("Ошибка: существует файл с таким именем!");
        return false;
    }
    return true;
}
boolean created = savesDirectory.mkdirs();
```

### Информация о файле

```java
// Из GameSaveManager.java — displaySavesList(), строки 1108–1111
// getName()      — только имя файла: "hero_binary.dat" (без пути)
// length()       — размер файла в байтах
// lastModified() — время последнего изменения (epoch milliseconds)
// getAbsolutePath() — полный абсолютный путь
System.out.printf("%s [%d байт] %s%n",
    f.getName(),
    f.length(),
    new java.util.Date(f.lastModified()));
```

### Фильтрация файлов в директории

```java
// Из GameSaveManager.java — listSaves(), строки 1046–1053
// listFiles(FileFilter) — возвращает только файлы, прошедшие фильтр.
// FileFilter — функциональный интерфейс: boolean accept(File file)
// Лямбда заменяет анонимный класс.
File[] saves = savesDirectory.listFiles(f ->
    f.isFile() && (f.getName().endsWith(".dat") || f.getName().endsWith(".sav"))
);
// listFiles() может вернуть null при ошибке доступа — защитная проверка:
return saves != null ? saves : new File[0];
```

```java
// Из SaveArchiver.java — exportToZip(), строки 223–224
// FilenameFilter — другой вариант фильтра: boolean accept(File dir, String name)
// Лямбда принимает два параметра: директорию и имя файла.
File[] files = dir.listFiles((d, name) ->
    name.startsWith(heroName) && !name.endsWith(".zip")
);
```

### Защита от Path Traversal (ZipSlip)

```java
// Из SaveArchiver.java — importFromZip(), строки 418–424
// getCanonicalPath() — разрешает "..", символические ссылки:
//   "saves/../../../etc/passwd" → "/etc/passwd"
// Сравниваем канонический путь с целевой директорией.
String canonicalTarget = targetFile.getCanonicalPath();
String canonicalDir = targetDir.getCanonicalPath() + File.separator;
if (!canonicalTarget.startsWith(canonicalDir)) {
    // Подозрительный путь — пропускаем запись
    zis.closeEntry();
    continue;
}
```

### Основные методы File

| Метод | Описание |
|-------|----------|
| `exists()` | Существует ли путь? |
| `isFile()` | Это файл (не директория)? |
| `isDirectory()` | Это директория? |
| `canRead()` / `canWrite()` | Есть ли права? |
| `getName()` | Имя файла (без пути) |
| `getAbsolutePath()` | Полный абсолютный путь |
| `getCanonicalPath()` | Канонический путь (без `..`, ссылок) |
| `length()` | Размер в байтах |
| `lastModified()` | Время последнего изменения (epoch ms) |
| `mkdir()` | Создать одну директорию |
| `mkdirs()` | Создать директорию и все промежуточные |
| `listFiles()` | Массив `File[]` содержимого |
| `listFiles(FileFilter)` | С фильтрацией |
| `delete()` | Удалить файл или пустую директорию |

---

## 13. ZIP-архивы (6.12)

**Файл:** `SaveArchiver.java` — полностью (методы `exportToZip`, `importFromZip`, `listZipFiles`)

ZIP — формат архивирования: один `.zip`-файл содержит несколько файлов (`ZipEntry`) в сжатом виде. Java поддерживает ZIP через пакет `java.util.zip`.

### Структура ZIP-файла

```
┌─────────────────────────────────────────┐
│ [Local File Header 1] + [Data 1]        │ ← первый файл (сжатые данные)
│ [Local File Header 2] + [Data 2]        │ ← второй файл
│ ...                                      │
│ [Central Directory]                      │ ← «оглавление» всего архива
│ [End of Central Directory]               │ ← конец: указатель на каталог
└─────────────────────────────────────────┘
```

### Создание ZIP — exportToZip

```java
// Из SaveArchiver.java — exportToZip(), строки 250–330
//
// Цепочка потоков (паттерн Декоратор):
//   ZipOutputStream      → управляет структурой ZIP-архива
//     └── BufferedOutputStream → буферизация (записывает блоками)
//           └── FileOutputStream → физическая запись на диск
try (var zos = new ZipOutputStream(
        new BufferedOutputStream(
                new FileOutputStream(zipFile)))) {

    byte[] buffer = new byte[BUFFER_SIZE]; // буфер переиспользуем для всех файлов

    for (File file : files) {
        // ZipEntry — описание одного файла в архиве.
        // Указываем только имя; CRC, размер — ZipOutputStream заполнит сам.
        ZipEntry entry = new ZipEntry(file.getName());

        // putNextEntry() — начинает новую запись в архиве.
        // Все последующие write() относятся к этой записи.
        zos.putNextEntry(entry);

        // Читаем исходный файл и пишем в ZIP блоками.
        // ВАЖНО: писать именно bytesRead байт, а не buffer.length!
        // Последний блок файла может быть неполным.
        try (var fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }

        // closeEntry() — завершает запись текущего файла.
        // ZipOutputStream финализирует сжатие, вычисляет CRC-32.
        // ОБЯЗАТЕЛЬНО вызывать перед putNextEntry() следующего файла!
        zos.closeEntry();
    }
} // zos.close() записывает Central Directory — без него архив будет повреждён!
```

### Чтение ZIP — importFromZip

```java
// Из SaveArchiver.java — importFromZip(), строки 376–481
//
// Цепочка потоков (зеркальна к записи):
//   ZipInputStream         → читает записи из ZIP-архива
//     └── BufferedInputStream → буферизация чтения
//           └── FileInputStream → физическое чтение с диска
try (var zis = new ZipInputStream(
        new BufferedInputStream(
                new FileInputStream(zipFile)))) {

    ZipEntry entry;
    // Итерация по всем записям архива.
    // getNextEntry() возвращает null, когда записи закончились.
    while ((entry = zis.getNextEntry()) != null) {
        File targetFile = new File(targetDir, entry.getName());

        // --- Защита от ZipSlip-уязвимости ---
        // Вредоносный архив может содержать "../../etc/passwd"
        String canonicalTarget = targetFile.getCanonicalPath();
        String canonicalDir = targetDir.getCanonicalPath() + File.separator;
        if (!canonicalTarget.startsWith(canonicalDir)) {
            zis.closeEntry();
            continue; // пропускаем подозрительную запись
        }

        // Извлекаем файл: zis.read() читает данные ТОЛЬКО текущей записи.
        try (var fos = new FileOutputStream(targetFile)) {
            int bytesRead;
            while ((bytesRead = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        }

        // closeEntry() — завершаем чтение текущей записи перед переходом к следующей.
        zis.closeEntry();
    }
}
```

### Порядок работы с ZipOutputStream

```
1. new ZipOutputStream(...)        — создать архив
2.   putNextEntry(new ZipEntry())  — начать файл
3.   write(buffer, 0, bytesRead)   — записать данные (повторять)
4.   closeEntry()                   — завершить файл
   (повторить 2–4 для каждого файла)
5. close() — закрыть архив (записывает Central Directory)
```

---

## 14. Класс Console (6.13)

**Файл:** `ConsoleDemo.java` — полностью (метод `runDemo`)

`Console` — специальный класс для работы с **системным терминалом**. Главная особенность: метод `readPassword()` скрывает вводимые символы.

### Получение Console

```java
// Из ConsoleDemo.java — строка 127
// System.console() — статический метод. Возвращает Console или null.
// null означает: программа запущена не из терминала (например, из IDE).
//
// ТИПИЧНАЯ ОШИБКА: не проверить на null → NullPointerException.
Console console = System.console();
if (console != null) {
    // Консоль доступна — используем Console API
} else {
    // Запуск из IDE — используем Scanner как fallback
}
```

### Методы Console

```java
// Из ConsoleDemo.java — строки 154–189

// printf() — форматированный вывод в терминал.
// Работает как System.out.printf(), но пишет напрямую в консоль
// (не перенаправляется при > в файл).
console.printf("[Console] Charset: %s%n", console.charset());

// readLine(fmt, args) — вывести приглашение и прочитать строку.
// Возвращает введённую строку (без \n) или null при EOF (Ctrl+D).
// Отличие от Scanner.nextLine(): совмещает приглашение и чтение в одном вызове.
playerName = console.readLine("[Console] Введи своё имя, герой: ");

// readPassword(fmt, args) — прочитать пароль БЕЗ отображения символов на экране.
// Console просит ОС отключить echo-режим терминала на время ввода.
// Возвращает char[] (не String!) — для безопасного хранения и очистки.
// НЕЛЬЗЯ вызвать из IDE — console будет null.
char[] passwordInput = console.readPassword("[Console] Секретный код: ");
```

### Почему char[], а не String?

```java
// Из ConsoleDemo.java — строки 263–280
//
// String неизменяем (immutable): очистить данные НЕВОЗМОЖНО.
// Строка "dragon" будет лежать в памяти до следующей сборки мусора.
// Если сделать heap dump процесса — пароль будет виден в памяти.
//
// char[] можно обнулить сразу после использования:
// Стандартная практика безопасности в Java:
//   1. readPassword() → char[]
//   2. Проверить пароль
//   3. Arrays.fill(password, '\0') — немедленно затереть

// Проверка пароля: == сравнивает ссылки, Arrays.equals — содержимое!
if (Arrays.equals(passwordInput, SECRET_CODE)) {
    System.out.println("Доступ разрешён!");
}

// Очистка — пароль больше не нужен
Arrays.fill(passwordInput, '\0');
```

### Console vs Scanner

| Возможность | `Console` | `Scanner` |
|-------------|-----------|-----------|
| Приглашение (prompt) | `readLine("Введи: ")` | Отдельный `print()` |
| Скрытый ввод (пароль) | `readPassword()` | Недоступно |
| Доступность в IDE | Нет (null) | Да |
| Доступность в терминале | Да | Да |
| Возврат пароля | `char[]` | `String` |
| Форматированный вывод | `printf()` | Нет |

---

## 15. Сводные таблицы

### Таблица: RPG-функция → I/O классы

| RPG-функция | Класс | I/O классы | Подглава |
|-------------|-------|------------|---------|
| Бинарное сохранение игры | `GameSaveManager.saveBinary()` | `DataOutputStream` + `BufferedOutputStream` + `FileOutputStream` | 6.3, 6.5, 6.7 |
| Бинарная загрузка игры | `GameSaveManager.loadBinary()` | `DataInputStream` + `BufferedInputStream` + `FileInputStream` | 6.3, 6.5, 6.7 |
| Объектное сохранение | `GameSaveManager.saveObject()` | `ObjectOutputStream` + `BufferedOutputStream` + `FileOutputStream` | 6.10 |
| Объектная загрузка | `GameSaveManager.loadObject()` | `ObjectInputStream` + `BufferedInputStream` + `FileInputStream` | 6.10 |
| Список файлов сохранений | `GameSaveManager.listSaves()` | `File.listFiles(FileFilter)` | 6.11 |
| Создание директории | `GameSaveManager.ensureSavesDir()` | `File.mkdirs()` | 6.11 |
| Формирование текста в памяти | `BattleLogExporter.exportBattleLog()` | `ByteArrayOutputStream` + `PrintStream` | 6.4, 6.6 |
| Запись журнала в файл | `BattleLogExporter.exportBattleLog()` | `BufferedWriter` + `FileWriter` | 6.8, 6.9 |
| Детальный отчёт с форматированием | `BattleLogExporter.exportBattleLog()` | `PrintWriter` + `BufferedWriter` + `FileWriter` | 6.6, 6.8, 6.9 |
| Верификация: чтение файла | `BattleLogExporter.exportBattleLog()` | `BufferedReader` + `FileReader` | 6.8, 6.9 |
| Создание ZIP-бэкапа | `SaveArchiver.exportToZip()` | `ZipOutputStream` + `BufferedOutputStream` + `FileOutputStream` | 6.12 |
| Распаковка ZIP | `SaveArchiver.importFromZip()` | `ZipInputStream` + `BufferedInputStream` + `FileInputStream` | 6.12 |
| Скрытый ввод пароля | `ConsoleDemo.runDemo()` | `Console.readPassword()` | 6.13 |
| Сериализуемое состояние | `Game`, `GameCharacter`, `Enemy`, `Inventory`, `Bestiary`, `BattleRecord` | `implements Serializable`, `transient` | 6.10 |

### Таблица: подглава → концепция → файл:метод

| Подглава | Концепция | Файл | Метод / Строки |
|---------|-----------|------|----------------|
| 6.1 | Иерархия потоков: дерево классов | `GameSaveManager.java` | Комментарий-схема, строки 1–110 |
| 6.2 | `try-with-resources`, AutoCloseable | `GameSaveManager.java` | `saveBinary()`, строки 419–607 |
| 6.2 | Несколько ресурсов в `try` | `BattleLogExporter.java` | строки 523–563 |
| 6.3 | `FileOutputStream` — запись в файл | `GameSaveManager.java` | `saveBinary()`, строка 441 |
| 6.3 | `FileInputStream` — чтение из файла | `SaveArchiver.java` | `exportToZip()`, строка 295 |
| 6.4 | `ByteArrayOutputStream` — в память | `BattleLogExporter.java` | строки 385–498 |
| 6.4 | `toByteArray()`, `toString()`, `size()` | `BattleLogExporter.java` | строки 455–485 |
| 6.5 | `BufferedOutputStream` — буфер записи | `GameSaveManager.java` | все методы с `DataOutputStream` |
| 6.5 | `BufferedInputStream` — буфер чтения | `SaveArchiver.java` | `importFromZip()`, строка 376 |
| 6.6 | `PrintStream.printf/format/println` | `BattleLogExporter.java` | строки 391–453 |
| 6.6 | `PrintWriter.printf/format/println` | `BattleLogExporter.java` | строки 581–672 |
| 6.6 | `PrintWriter.checkError()` | `BattleLogExporter.java` | строки 655–672 |
| 6.7 | `DataOutputStream.writeInt/writeUTF` | `GameSaveManager.java` | `saveBinary()`, строки 456–594 |
| 6.7 | `DataInputStream.readInt/readUTF` | `GameSaveManager.java` | `loadBinary()`, строки 672–802 |
| 6.7 | `EOFException` при чтении | `GameSaveManager.java` | `loadBinary()`, строки 810–815 |
| 6.8 | `FileWriter` — символьная запись | `BattleLogExporter.java` | строки 523–563 |
| 6.8 | `FileReader` — символьное чтение | `BattleLogExporter.java` | строки 703–728 |
| 6.9 | `BufferedWriter.write`, `flush` | `BattleLogExporter.java` | строки 523–563 |
| 6.9 | `BufferedReader.readLine()` | `BattleLogExporter.java` | строки 703–728 |
| 6.10 | `implements Serializable` | `Game.java` строка 141, `GameCharacter.java` строка 75 | Объявление класса |
| 6.10 | `serialVersionUID` | `Game.java`, `Bestiary.java` | Объявление класса |
| 6.10 | `transient` — исключение поля | `Game.java` | строки 229, 272, 286, 299, 327, 372 |
| 6.10 | `ObjectOutputStream.writeObject` | `GameSaveManager.java` | `saveObject()`, строка 895 |
| 6.10 | `ObjectInputStream.readObject` | `GameSaveManager.java` | `loadObject()`, строка 955 |
| 6.10 | `InvalidClassException` | `GameSaveManager.java` | `loadObject()`, строки 983–995 |
| 6.10 | `ClassNotFoundException` | `GameSaveManager.java` | `loadObject()`, строки 996–1001 |
| 6.10 | `reinitializeTransients()` — восстановление transient | `GameSaveManager.java` | `loadObject()`, строка 966 |
| 6.11 | `new File(path)` — путь к файлу | `GameSaveManager.java` | `ensureSavesDir()`, строки 338–361 |
| 6.11 | `mkdirs()` — создание директории | `GameSaveManager.java` | строка 354 |
| 6.11 | `listFiles(FileFilter)` | `GameSaveManager.java` | `listSaves()`, строки 1046–1053 |
| 6.11 | `listFiles(FilenameFilter)` — лямбда | `SaveArchiver.java` | `exportToZip()`, строки 223–224 |
| 6.11 | `getName()`, `length()`, `lastModified()` | `GameSaveManager.java` | `displaySavesList()`, строки 1099–1116 |
| 6.11 | `getCanonicalPath()` — защита от ZipSlip | `SaveArchiver.java` | `importFromZip()`, строки 418–424 |
| 6.12 | `ZipOutputStream.putNextEntry` | `SaveArchiver.java` | `exportToZip()`, строка 280 |
| 6.12 | `ZipOutputStream.closeEntry` | `SaveArchiver.java` | `exportToZip()`, строка 319 |
| 6.12 | `ZipInputStream.getNextEntry` | `SaveArchiver.java` | `importFromZip()`, строка 392 |
| 6.12 | `ZipEntry` — метаданные файла в архиве | `SaveArchiver.java` | строки 268, 391 |
| 6.13 | `System.console()` — получение Console | `ConsoleDemo.java` | строка 127 |
| 6.13 | `Console.readLine()` | `ConsoleDemo.java` | строка 168 |
| 6.13 | `Console.readPassword()` — скрытый ввод | `ConsoleDemo.java` | строка 189 |
| 6.13 | `Arrays.fill(char[], '\0')` — очистка пароля | `ConsoleDemo.java` | строка 280 |

---

## Рекомендуемый порядок изучения файлов

| Шаг | Файл | Что изучить |
|-----|------|-------------|
| 1 | `GameSaveManager.java` (начало) | Иерархия потоков (6.1), схема классов, паттерн Декоратор |
| 2 | `GameSaveManager.java` — `ensureSavesDir()` | Класс `File`: `exists()`, `mkdirs()` (6.11) |
| 3 | `GameSaveManager.java` — `saveBinary()` | `try-with-resources` (6.2), `DataOutputStream` (6.7), `BufferedOutputStream` (6.5) |
| 4 | `GameSaveManager.java` — `loadBinary()` | `DataInputStream` (6.7), порядок чтения, `EOFException` |
| 5 | `GameSaveManager.java` — `listSaves()` | `File.listFiles(FileFilter)`, лямбда-фильтр (6.11) |
| 6 | `Game.java` — поля `transient` | `Serializable`, `transient`, `serialVersionUID` (6.10) |
| 7 | `GameSaveManager.java` — `saveObject()`/`loadObject()` | `ObjectOutputStream`/`ObjectInputStream` (6.10) |
| 8 | `BattleLogExporter.java` | `ByteArrayOutputStream` + `PrintStream` (6.4, 6.6), `FileWriter`+`BufferedWriter` (6.8, 6.9), `PrintWriter` (6.6), `BufferedReader`+`FileReader` (6.9) |
| 9 | `SaveArchiver.java` | `ZipOutputStream`/`ZipInputStream` (6.12), защита от ZipSlip |
| 10 | `ConsoleDemo.java` | `Console` (6.13), `readPassword()`, очистка `char[]` |
