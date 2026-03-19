// ===== ЗАЧЕМ ЭТОТ ФАЙЛ? =====
//
// BattleLogExporter — экспорт журнала боёв в текстовый файл.
// После серии сражений игрок может сохранить историю боёв для просмотра вне игры.
//
// Этот класс демонстрирует СИМВОЛЬНЫЕ (character) потоки ввода-вывода из главы 6:
//   - ByteArrayOutputStream (6.4) — формирование текста В ПАМЯТИ (байтовый поток)
//   - FileWriter + BufferedWriter (6.8, 6.9) — запись символов в файл с буферизацией
//   - PrintWriter (6.6) — удобные методы printf/println/format для форматированного вывода
//   - FileReader + BufferedReader (6.8, 6.9) — чтение символов из файла с буферизацией
//
// ===== БАЙТОВЫЕ vs СИМВОЛЬНЫЕ ПОТОКИ (глава 6.1) =====
//
// В Java существуют ДВА семейства потоков ввода-вывода:
//
// ┌─────────────────────────────────────────────────────────────────────────────┐
// │                     БАЙТОВЫЕ ПОТОКИ (byte streams)                         │
// │                                                                             │
// │  InputStream (чтение байтов)           OutputStream (запись байтов)         │
// │    ├─ FileInputStream                    ├─ FileOutputStream                │
// │    ├─ ByteArrayInputStream               ├─ ByteArrayOutputStream           │
// │    ├─ BufferedInputStream                ├─ BufferedOutputStream             │
// │    ├─ DataInputStream                    ├─ DataOutputStream                │
// │    └─ ObjectInputStream                  ├─ ObjectOutputStream              │
// │                                          └─ PrintStream (System.out)        │
// │                                                                             │
// │  Работают с БАЙТАМИ (byte, int). Один byte = 8 бит = число от -128 до 127. │
// │  Подходят для: изображений, аудио, бинарных данных, сериализации объектов.  │
// └─────────────────────────────────────────────────────────────────────────────┘
//
// ┌─────────────────────────────────────────────────────────────────────────────┐
// │                     СИМВОЛЬНЫЕ ПОТОКИ (character streams)                   │
// │                                                                             │
// │  Reader (чтение символов)              Writer (запись символов)             │
// │    ├─ FileReader                         ├─ FileWriter                      │
// │    ├─ BufferedReader                     ├─ BufferedWriter                  │
// │    ├─ InputStreamReader                  ├─ OutputStreamWriter              │
// │    └─ StringReader                       └─ PrintWriter                    │
// │                                                                             │
// │  Работают с СИМВОЛАМИ (char). Один char = 16 бит = символ Unicode (UTF-16).│
// │  Подходят для: текстовых файлов, журналов, конфигураций, CSV, JSON.        │
// └─────────────────────────────────────────────────────────────────────────────┘
//
// КЛЮЧЕВОЕ ОТЛИЧИЕ:
//   - Байтовый поток: read() возвращает int (0-255 или -1), write(int b) пишет 1 байт.
//   - Символьный поток: read() возвращает int (0-65535 или -1), write(int c) пишет 1 символ.
//   - Символьные потоки АВТОМАТИЧЕСКИ конвертируют байты ↔ символы с учётом кодировки (charset).
//
// КОГДА ЧТО ВЫБИРАТЬ:
//   - Текст → символьные потоки (Reader/Writer). Они правильно обрабатывают кодировки (UTF-8 и т.д.).
//   - Всё остальное → байтовые потоки (InputStream/OutputStream).
//
// ПАТТЕРН ДЕКОРАТОР (Decorator Pattern):
//   Потоки можно ОБОРАЧИВАТЬ друг в друга, добавляя функциональность:
//     BufferedWriter(FileWriter(file)) — FileWriter пишет символы в файл,
//                                        BufferedWriter добавляет буферизацию.
//     PrintWriter(BufferedWriter(FileWriter(file))) — ещё один слой: удобные методы println/printf.
//   Каждый «обёрточный» поток ДЕЛЕГИРУЕТ запись/чтение внутреннему потоку,
//   добавляя свою логику (буфер, форматирование, сжатие и т.д.).
//
// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== ИМПОРТЫ ПОТОКОВ ВВОДА-ВЫВОДА (глава 6) =====

// ===== ByteArrayOutputStream (глава 6.4) =====
//
// ByteArrayOutputStream — байтовый поток вывода, который пишет данные В ПАМЯТЬ (в массив байтов),
// а НЕ в файл и НЕ в сеть.
//
// Зачем? Когда нужно:
//   1. Сначала СФОРМИРОВАТЬ данные целиком в памяти.
//   2. Потом решить, что с ними делать: записать в файл, отправить по сети, преобразовать в строку.
//
// Внутри ByteArrayOutputStream — динамический массив byte[], который автоматически растёт
// при записи (как ArrayList, но для байтов).
//
// Основные методы:
//   write(int b)        — записать один байт в буфер.
//   write(byte[] b)     — записать массив байтов в буфер.
//   toString()          — преобразовать содержимое буфера в String (используя кодировку платформы).
//   toString(charset)   — преобразовать с указанной кодировкой (рекомендуется UTF-8).
//   toByteArray()       — получить копию внутреннего массива байтов.
//   size()              — текущий размер данных в буфере.
//   reset()             — очистить буфер (size() станет 0), но массив не пересоздаётся.
//
// ВАЖНО: ByteArrayOutputStream НЕ нужно закрывать (close() ничего не делает),
//   но мы всё равно используем try-with-resources для единообразия стиля.
import java.io.ByteArrayOutputStream;

// ===== PrintStream (глава 6.6) =====
//
// PrintStream — байтовый поток вывода с удобными методами печати.
// System.out — это PrintStream! Когда пишем System.out.println("Hello") —
// это вызов метода println() объекта PrintStream.
//
// Основные методы:
//   print(x)    — вывести значение без перевода строки.
//   println(x)  — вывести значение С переводом строки.
//   printf(format, args) — форматированный вывод (как в C: %s, %d, %f).
//   format(format, args) — синоним printf().
//
// PrintStream работает с БАЙТАМИ, а PrintWriter — с СИМВОЛАМИ.
// Для записи текста в файл лучше PrintWriter (символьный поток).
// Здесь PrintStream используем для записи в ByteArrayOutputStream (байтовый поток в памяти).
import java.io.PrintStream;

// ===== FileWriter (глава 6.8) =====
//
// FileWriter — символьный поток вывода, который записывает СИМВОЛЫ (char) в файл.
//
// FileWriter vs FileOutputStream:
//   - FileOutputStream записывает БАЙТЫ (byte). Для текста нужно вручную кодировать символы.
//   - FileWriter записывает СИМВОЛЫ (char). Автоматически конвертирует char → byte
//     с учётом кодировки (по умолчанию — кодировка платформы).
//
// Конструкторы:
//   new FileWriter("file.txt")         — создать/перезаписать файл (кодировка платформы).
//   new FileWriter("file.txt", true)   — ДОПИСАТЬ в конец файла (append mode).
//   new FileWriter("file.txt", charset) — указать кодировку (Java 11+).
//
// ВАЖНО: FileWriter пишет ПОСИМВОЛЬНО. Каждый write() — системный вызов (дорого!).
//   Поэтому FileWriter ВСЕГДА оборачивают в BufferedWriter для буферизации.
import java.io.FileWriter;

// ===== BufferedWriter (глава 6.9) =====
//
// BufferedWriter — обёртка (декоратор) над любым Writer, добавляющая БУФЕРИЗАЦИЮ.
//
// БЕЗ буфера: каждый write() → системный вызов → запись на диск.
//   Если записываем 1000 строк — 1000 системных вызовов. МЕДЛЕННО!
//
// С БУФЕРОМ: данные копятся в памяти (char[] buf, по умолчанию 8192 символа).
//   Когда буфер заполнен ИЛИ вызван flush() ИЛИ поток закрыт → одна запись на диск.
//   1000 строк → возможно 1-2 системных вызова. БЫСТРО!
//
// Методы:
//   write(String s)  — записать строку в буфер.
//   newLine()        — записать платформо-зависимый перевод строки (\n или \r\n).
//   flush()          — принудительно сбросить буфер на диск (не дожидаясь заполнения).
//   close()          — вызывает flush(), затем закрывает поток.
//
// Паттерн использования (цепочка декораторов):
//   BufferedWriter bw = new BufferedWriter(new FileWriter("file.txt"));
//   bw.write("Hello");
//   bw.newLine();
//   bw.close();  // или try-with-resources
import java.io.BufferedWriter;

// ===== PrintWriter (глава 6.6) =====
//
// PrintWriter — символьный поток вывода с удобными методами печати.
// Похож на PrintStream, но работает с СИМВОЛАМИ, а не байтами.
//
// PrintWriter vs PrintStream:
//   - PrintStream → байтовый поток (OutputStream). System.out — это PrintStream.
//   - PrintWriter → символьный поток (Writer). Лучше для текстовых файлов.
//
// Основные методы (те же, что у PrintStream):
//   print(x)                 — вывести без перевода строки.
//   println(x)               — вывести С переводом строки.
//   printf(format, args)     — форматированный вывод: %s=строка, %d=целое, %f=дробное, %n=новая строка.
//   format(format, args)     — синоним printf().
//
// printf/format — спецификаторы формата (как в C):
//   %s     — строка:                printf("Имя: %s", name)        → "Имя: Иван"
//   %d     — целое число:           printf("HP: %d", 100)          → "HP: 100"
//   %f     — дробное число:         printf("Шанс: %.1f%%", 30.5)   → "Шанс: 30.5%"
//   %n     — перенос строки:        printf("строка 1%nстрока 2")
//   %-20s  — выравнивание влево:    printf("%-20s|", "Текст")      → "Текст               |"
//   %10d   — выравнивание вправо:   printf("%10d", 42)             → "        42"
//
// ВАЖНО: PrintWriter никогда НЕ выбрасывает IOException!
//   Ошибки записи «глотаются». Чтобы проверить, была ли ошибка: pw.checkError().
//   Это удобно для вывода (не нужен try-catch), но опасно для критических данных.
//
// Конструкторы:
//   new PrintWriter(Writer out)       — обернуть любой Writer.
//   new PrintWriter(Writer out, true) — с auto-flush (flush после каждого println).
//   new PrintWriter("file.txt")       — записать в файл (создаёт внутри BufferedOutputStream).
//   new PrintWriter(OutputStream out) — обернуть байтовый поток.
import java.io.PrintWriter;

// ===== FileReader (глава 6.8) =====
//
// FileReader — символьный поток ввода, который читает СИМВОЛЫ (char) из файла.
//
// FileReader vs FileInputStream:
//   - FileInputStream читает БАЙТЫ (byte). Для текста нужно вручную декодировать.
//   - FileReader читает СИМВОЛЫ (char). Автоматически конвертирует byte → char
//     с учётом кодировки (по умолчанию — кодировка платформы).
//
// Конструкторы:
//   new FileReader("file.txt")          — открыть файл (кодировка платформы).
//   new FileReader("file.txt", charset) — указать кодировку (Java 11+).
//
// Методы:
//   read()         — прочитать один символ (возвращает int, -1 = конец файла).
//   read(char[] buf) — прочитать массив символов.
//
// ВАЖНО: FileReader читает ПОСИМВОЛЬНО. Каждый read() — системный вызов.
//   Поэтому FileReader ВСЕГДА оборачивают в BufferedReader для буферизации.
import java.io.FileReader;

// ===== BufferedReader (глава 6.9) =====
//
// BufferedReader — обёртка над Reader с БУФЕРИЗАЦИЕЙ и дополнительным методом readLine().
//
// readLine() — главная причина использования BufferedReader!
//   Читает одну строку текста (до \n, \r или \r\n). Возвращает String.
//   Возвращает null, когда файл закончился (End Of File).
//
// Паттерн чтения текстового файла:
//   try (BufferedReader br = new BufferedReader(new FileReader("file.txt"))) {
//       String line;
//       while ((line = br.readLine()) != null) {
//           System.out.println(line);
//       }
//   }
//
// Буферизация:
//   Без буфера: каждый read() → системный вызов → чтение с диска.
//   С буфером: read() берёт данные из char[] buf (8192 символа по умолчанию).
//     Когда буфер опустошён — одно чтение с диска заполняет буфер целиком.
import java.io.BufferedReader;

// IOException — проверяемое (checked) исключение для ошибок ввода-вывода.
// Все операции с файлами (открытие, чтение, запись, закрытие) могут выбросить IOException.
// Компилятор ЗАСТАВЛЯЕТ обработать: try-catch или throws в сигнатуре метода.
import java.io.IOException;

// File — класс для работы с путями файловой системы (глава 6.11).
// Здесь используется для создания директории saves/ и формирования пути файла.
import java.io.File;

// List<String> — для передачи журнала боёв (battleLog).
import java.util.List;

// ===== КЛАСС BattleLogExporter (глава 6.4, 6.6, 6.8, 6.9) =====
//
// Экспортирует журнал боёв (List<String> battleLog) в текстовый файл.
//
// Цепочка обработки (демонстрация нескольких типов потоков):
//
//   Шаг 1: ByteArrayOutputStream — формируем весь текст В ПАМЯТИ.
//           Это позволяет собрать данные из разных источников (заголовок, журнал, статистика)
//           в один буфер, а потом записать в файл одним блоком.
//
//   Шаг 2: BufferedWriter(FileWriter) — записываем сформированный текст в файл.
//           FileWriter конвертирует символы → байты, BufferedWriter буферизует запись.
//
//   Шаг 3: PrintWriter — дублируем вывод через PrintWriter с использованием
//           printf/format/println для демонстрации форматированного вывода.
//
//   Шаг 4: BufferedReader(FileReader) — читаем файл обратно и выводим первые строки
//           для проверки, что запись прошла успешно.
//
public class BattleLogExporter {

    // ===== КОНСТАНТА: ДИРЕКТОРИЯ СОХРАНЕНИЙ =====
    //
    // private — доступна только внутри этого класса (инкапсуляция).
    // static — принадлежит КЛАССУ, а не экземпляру. Одно значение на все объекты.
    // final — значение присваивается ОДИН РАЗ и больше не меняется.
    //
    // Все файлы экспорта сохраняются в директорию "saves/".
    // Та же директория, что и у GameSaveManager — все файлы игры в одном месте.
    private static final String DEFAULT_SAVES_DIR = "saves";

    // ===== КОНСТАНТА: МАКСИМУМ СТРОК ДЛЯ ВЕРИФИКАЦИИ =====
    //
    // При чтении файла обратно (шаг 4) выводим не весь файл, а только первые строки.
    // Это демонстрирует BufferedReader.readLine() и защищает от огромного вывода.
    private static final int VERIFICATION_LINES = 5;

    // ===== МЕТОД exportBattleLog — ГЛАВНАЯ ТОЧКА ВХОДА =====
    //
    // Параметры:
    //   battleLog          — список строк журнала боёв (List<String> из Game.java)
    //   heroName           — имя героя (для заголовка и имени файла)
    //   totalDamageDealt   — общий нанесённый урон
    //   totalDamageReceived — общий полученный урон
    //   enemiesDefeated    — количество побеждённых врагов
    //   totalHealing       — общее исцеление
    //
    // Возвращает void — результат записывается в файл, а не возвращается.
    //
    // Метод public static — можно вызвать без создания объекта:
    //   BattleLogExporter.exportBattleLog(log, "Артас", 500, 120, 3, 90);
    //
    public static void exportBattleLog(List<String> battleLog, String heroName,
                                       int totalDamageDealt, int totalDamageReceived,
                                       int enemiesDefeated, int totalHealing) {

        // ===== ПРОВЕРКА ПРЕДУСЛОВИЯ: ЖУРНАЛ НЕ ПУСТ =====
        //
        // Если журнал пуст — нечего экспортировать. Сообщаем и выходим.
        // battleLog == null — защита от NullPointerException.
        // battleLog.isEmpty() — проверка пустого списка (size() == 0).
        if (battleLog == null || battleLog.isEmpty()) {
            System.out.println("⚠ Журнал боёв пуст — нечего экспортировать.");
            return;
        }

        // ===== ПРОВЕРКА ПРЕДУСЛОВИЯ: ИМЯ ГЕРОЯ НЕ null =====
        //
        // heroName используется для формирования имени файла (heroName.trim()).
        // Если heroName == null, вызов trim() кинет NullPointerException.
        // Проверяем отдельно от battleLog, чтобы сообщение было информативным.
        if (heroName == null) {
            System.out.println("⚠ Имя героя не задано — невозможно экспортировать журнал.");
            return;
        }

        // ===== СОЗДАНИЕ ДИРЕКТОРИИ saves/ (глава 6.11) =====
        //
        // File — класс, представляющий путь в файловой системе.
        // new File("saves") — создаёт объект File, но НЕ создаёт директорию на диске!
        //   File — это только ОПИСАНИЕ пути. Чтобы создать директорию, нужен mkdirs().
        //
        // mkdirs() — создаёт директорию И все родительские директории, если их нет.
        //   Возвращает true, если директория была создана.
        //   Возвращает false, если директория уже существовала ИЛИ произошла ошибка.
        //
        // exists() — проверяет, существует ли файл/директория на диске.
        //
        // Порядок: сначала создаём директорию, потом пишем в неё файлы.
        File savesDir = new File(DEFAULT_SAVES_DIR);
        if (!savesDir.exists()) {
            savesDir.mkdirs();
        }

        // ===== НОРМАЛИЗАЦИЯ ИМЕНИ ГЕРОЯ ДЛЯ ИМЕНИ ФАЙЛА =====
        //
        // Имя героя может содержать пробелы, спецсимволы, заглавные буквы.
        // Для имени файла нужно: пробелы → подчёркивания, всё в нижнем регистре,
        // убрать символы, запрещённые в именах файлов.
        //
        // replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9_]", "_") — регулярное выражение (regex):
        //   [^...] — символ, НЕ входящий в указанный набор.
        //   a-zA-Z — латинские буквы, а-яА-ЯёЁ — русские буквы, 0-9 — цифры, _ — подчёркивание.
        //   Всё, что не буква/цифра/_ — заменяется на подчёркивание.
        //
        // toLowerCase() — приводит к нижнему регистру ("Артас" → "артас").
        // trim() — удаляет пробелы по краям строки.
        String normalizedName = heroName.trim()
                .toLowerCase()
                .replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9_]", "_");

        // Формируем полный путь к файлу: saves/артас_battle_log.txt
        String filePath = DEFAULT_SAVES_DIR + File.separator + normalizedName + "_battle_log.txt";

        // Дополнительный файл для демонстрации PrintWriter с форматированным выводом.
        String detailedFilePath = DEFAULT_SAVES_DIR + File.separator + normalizedName + "_battle_log_detailed.txt";

        // ===== ШАГ 1: ФОРМИРОВАНИЕ ТЕКСТА В ПАМЯТИ — ByteArrayOutputStream (глава 6.4) =====
        //
        // ByteArrayOutputStream — поток, который пишет данные В МАССИВ БАЙТОВ В ПАМЯТИ.
        //
        // Зачем формировать текст в памяти, а не сразу в файл?
        //   1. Можно собрать данные из разных источников (заголовок, журнал, статистика).
        //   2. Можно использовать один и тот же текст для разных целей (файл, консоль, сеть).
        //   3. Если при формировании произойдёт ошибка — файл не будет создан (атомарность).
        //
        // PrintStream оборачивает ByteArrayOutputStream — это позволяет использовать
        // удобные методы println/printf вместо write(byte[]).
        //
        // Почему БАЙТОВЫЙ PrintStream (а не символьный PrintWriter)?
        //   ByteArrayOutputStream — байтовый поток (работает с byte[]).
        //   PrintStream тоже байтовый (наследует OutputStream).
        //   Они совместимы: PrintStream может обернуть любой OutputStream.
        //   PrintWriter был бы уместнее для текста, но он оборачивает Writer,
        //   а ByteArrayOutputStream — не Writer, а OutputStream.
        //   Поэтому для этой комбинации используем PrintStream.
        //
        // АНАЛОГИЯ: ByteArrayOutputStream — это как StringBuilder, но для байтов.
        //   StringBuilder собирает символы → toString() даёт String.
        //   ByteArrayOutputStream собирает байты → toString() даёт String, toByteArray() даёт byte[].
        //
        // try-with-resources — автоматически закроет оба потока после блока.
        // Хотя ByteArrayOutputStream.close() ничего не делает (нет внешних ресурсов),
        // мы используем try-with-resources для единообразия и хорошей привычки.
        String content;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos)) {

            // PrintStream.println() — вывод строки с переводом строки.
            // Здесь мы пишем НЕ в консоль (System.out), а в наш ByteArrayOutputStream!
            // PrintStream можно направить в ЛЮБОЙ OutputStream.
            ps.println("╔══════════════════════════════════════════════╗");
            ps.println("║          ЖУРНАЛ БОЁВ — ЭКСПОРТ              ║");
            ps.println("╠══════════════════════════════════════════════╣");

            // printf() — форматированный вывод (как в языке C).
            // %s — подставить строку (heroName).
            // %n — платформо-зависимый перевод строки (\n на Unix, \r\n на Windows).
            //
            // РАЗНИЦА printf vs println:
            //   println("Герой: " + heroName) — конкатенация строк (создаётся новый String).
            //   printf("Герой: %s%n", heroName) — форматирование (часто эффективнее, всегда нагляднее).
            ps.printf("║  Герой: %s%n", heroName);

            // System.currentTimeMillis() — текущее время в миллисекундах от эпохи Unix (1 января 1970).
            // java.util.Date — устаревший класс, но простой для вывода даты строкой.
            // В учебных целях используем его вместо java.time (чтобы не усложнять).
            ps.printf("║  Дата экспорта: %s%n", new java.util.Date());

            ps.println("╠══════════════════════════════════════════════╣");
            ps.println("║  ЗАПИСИ ЖУРНАЛА:                            ║");
            ps.println("╠══════════════════════════════════════════════╣");

            // ===== ПЕРЕБОР СПИСКА — for-each (глава 5.1, 5.2) =====
            //
            // for (String entry : battleLog) — цикл for-each.
            //   Перебирает ВСЕ элементы List<String> battleLog по порядку.
            //   entry — текущая строка журнала.
            //
            // battleLog — это ArrayList<String>, который Game.java заполняет во время боёв.
            // Каждая запись — текстовое описание действия ("Воин атакует Гоблина: 15 урона").
            int lineNumber = 1;
            for (String entry : battleLog) {
                // printf с несколькими спецификаторами:
                //   %4d — целое число, минимум 4 символа (выравнивание вправо).
                //         Пример: "   1", "  10", " 100", "1000".
                //   %s  — строка (запись журнала).
                //   %n  — перевод строки.
                ps.printf("  %4d. %s%n", lineNumber++, entry);
            }

            ps.println("╠══════════════════════════════════════════════╣");
            ps.println("║  ИТОГОВАЯ СТАТИСТИКА:                       ║");
            ps.println("╠══════════════════════════════════════════════╣");

            // format() — СИНОНИМ printf(). Делает абсолютно то же самое.
            // Показываем оба метода для демонстрации: printf выше, format здесь.
            //
            // %-25s — строка, минимум 25 символов, выравнивание ВЛЕВО (знак минус).
            //   "Нанесено урона"     → "Нанесено урона          " (дополнено пробелами справа).
            //   Без минуса (%25s)    → "          Нанесено урона" (дополнено пробелами слева).
            ps.format("  %-25s %d%n", "Нанесено урона:", totalDamageDealt);
            ps.format("  %-25s %d%n", "Получено урона:", totalDamageReceived);
            ps.format("  %-25s %d%n", "Врагов побеждено:", enemiesDefeated);
            ps.format("  %-25s %d%n", "Исцеление:", totalHealing);

            ps.println("╚══════════════════════════════════════════════╝");

            // ===== flush() — ПРИНУДИТЕЛЬНЫЙ СБРОС БУФЕРА =====
            //
            // PrintStream использует внутренний буфер. flush() гарантирует,
            // что ВСЕ данные записаны в ByteArrayOutputStream ДО вызова toString().
            // Без flush() часть данных может остаться в буфере PrintStream.
            ps.flush();

            // ===== ByteArrayOutputStream.toString() — ПОЛУЧЕНИЕ ТЕКСТА ИЗ ПАМЯТИ (6.4) =====
            //
            // toString() — преобразует массив байтов в строку.
            // Использует кодировку платформы по умолчанию (обычно UTF-8 на Linux/Mac,
            // но может быть Windows-1251 или CP866 на Windows!).
            //
            // ЛОВУШКА: на Windows кодировка платформы часто НЕ UTF-8.
            // Русские символы в журнале боёв запишутся через PrintStream,
            // который тоже использует кодировку платформы — и toString() их корректно вернёт.
            // Но если передать этот текст дальше (в файл через FileWriter с другой кодировкой),
            // символы могут «поломаться» (krakozyabry).
            //
            // Надёжнее: baos.toString(StandardCharsets.UTF_8) — явно UTF-8 (Java 10+).
            // В учебных целях используем toString() без аргументов для простоты.
            //
            // Альтернативы:
            //   baos.toString("UTF-8")   — явно указать кодировку строкой (старый способ).
            //   baos.toString(StandardCharsets.UTF_8) — через Charset (Java 10+, рекомендуется).
            //   baos.toByteArray()       — получить сырой массив байтов (для бинарных данных).
            //   baos.size()              — узнать размер данных в буфере.
            content = baos.toString();

            System.out.println("\n✓ Текст журнала сформирован в памяти (ByteArrayOutputStream).");
            System.out.printf("  Размер данных в памяти: %d байт.%n", baos.size());

            // ===== toByteArray() — ДЕМОНСТРАЦИЯ (6.4) =====
            //
            // toByteArray() возвращает КОПИЮ внутреннего массива байтов.
            // Полезно, когда нужно передать байты (например, в OutputStream.write(byte[])).
            byte[] rawBytes = baos.toByteArray();
            System.out.printf("  toByteArray(): массив из %d байтов.%n", rawBytes.length);

        } catch (IOException e) {
            // ===== IOException от try-with-resources (глава 4.1, 6.2) =====
            //
            // ByteArrayOutputStream.close() объявляет throws IOException (наследует от OutputStream),
            // хотя реально НИКОГДА не выбрасывает исключение (нет внешних ресурсов для закрытия).
            // Но компилятор этого не знает и ТРЕБУЕТ обработки — checked exception.
            //
            // PrintStream.close() тоже может теоретически выбросить IOException.
            // На практике для ByteArrayOutputStream + PrintStream ошибок не будет,
            // но мы обязаны написать catch — таковы правила checked exceptions.
            System.out.println("✗ Ошибка при формировании текста в памяти: " + e.getMessage());
            return;
        }

        // ===== ШАГ 2: ЗАПИСЬ В ФАЙЛ — BufferedWriter(FileWriter) (глава 6.8, 6.9) =====
        //
        // Цепочка декораторов: BufferedWriter оборачивает FileWriter.
        //
        //   FileWriter — записывает символы в файл. Конвертирует char → byte.
        //     Но каждый write() — это системный вызов. Для 1000 строк = 1000 вызовов!
        //
        //   BufferedWriter — добавляет буфер (char[8192]). Данные накапливаются в памяти.
        //     Запись на диск происходит только когда:
        //       - буфер заполнен (8192 символа)
        //       - вызван flush()
        //       - поток закрыт (close() вызывает flush())
        //     Результат: 1000 строк → 1-2 системных вызова вместо 1000.
        //
        // try-with-resources — КРИТИЧЕСКИ ВАЖЕН для файловых потоков!
        //   При нормальном завершении: close() → flush() → данные записаны на диск.
        //   При исключении: close() вызывается автоматически, файл не повреждён.
        //   Без try-with-resources: при исключении данные в буфере ПОТЕРЯЮТСЯ (не записаны на диск!).
        //
        // Порядок закрытия при нескольких ресурсах в try-with-resources:
        //   Закрываются в ОБРАТНОМ порядке объявления.
        //   Здесь: сначала bw.close() (flush буфера → запись на диск), потом fw (закрытие файла).
        try (FileWriter fw = new FileWriter(filePath);
             BufferedWriter bw = new BufferedWriter(fw)) {

            // ===== BufferedWriter.write(String) — ЗАПИСЬ СТРОКИ В БУФЕР (6.9) =====
            //
            // write(String s) — записывает строку в буфер BufferedWriter.
            // Строка НЕ сразу попадает на диск — она копируется в char[] buf.
            // Когда буфер заполнится или будет вызван flush()/close() — данные запишутся в файл.
            //
            // Мы записываем ВЕСЬ сформированный текст одним вызовом write().
            // Переменная content содержит полный текст из ByteArrayOutputStream.
            bw.write(content);

            // ===== BufferedWriter.flush() — ПРИНУДИТЕЛЬНЫЙ СБРОС (6.9) =====
            //
            // flush() — записать содержимое буфера на диск ПРЯМО СЕЙЧАС.
            //
            // Когда нужен flush():
            //   - Данные критически важны и не должны быть потеряны.
            //   - Нужно, чтобы другой процесс увидел данные до закрытия файла.
            //   - В цикле записи — периодический flush() для надёжности.
            //
            // Здесь flush() избыточен (close() вызовет его автоматически),
            // но мы показываем его явно в учебных целях.
            bw.flush();

            System.out.println("✓ Журнал записан в файл: " + filePath);
            System.out.println("  (использован BufferedWriter → FileWriter)");

        } catch (IOException e) {
            // ===== ОБРАБОТКА IOException (глава 4.1) =====
            //
            // IOException — проверяемое (checked) исключение.
            // Может возникнуть при:
            //   - Нет прав на запись в директорию
            //   - Диск переполнен
            //   - Путь не существует (если mkdirs не сработал)
            //
            // getMessage() — текстовое описание ошибки от операционной системы.
            System.out.println("✗ Ошибка записи журнала в файл: " + e.getMessage());
            return;
        }

        // ===== ШАГ 3: ЗАПИСЬ ЧЕРЕЗ PrintWriter (глава 6.6) =====
        //
        // PrintWriter — символьный аналог PrintStream.
        // Главное преимущество: удобные методы printf/println/format/print.
        //
        // Цепочка: PrintWriter → BufferedWriter → FileWriter → файл.
        //   PrintWriter добавляет printf/println поверх BufferedWriter.
        //   BufferedWriter добавляет буферизацию поверх FileWriter.
        //   FileWriter конвертирует символы в байты и пишет в файл.
        //
        // Конструктор PrintWriter(Writer out, boolean autoFlush):
        //   autoFlush = true — вызывать flush() после каждого println/printf/format.
        //     Полезно для логов (каждая строка сразу на диске), но медленнее.
        //   autoFlush = false (по умолчанию) — flush только при close() или явном вызове.
        //     Быстрее, но при аварийном завершении данные могут потеряться.
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(detailedFilePath)))) {

            // ===== PrintWriter.println() — ВЫВОД С ПЕРЕВОДОМ СТРОКИ (6.6) =====
            //
            // println(x) — эквивалент print(x) + print("\n").
            // Работает с любым типом: String, int, double, Object (вызывает toString()).
            pw.println("====================================================");
            pw.println("  ДЕТАЛЬНЫЙ ОТЧЁТ О БОЕВЫХ ДЕЙСТВИЯХ");
            pw.println("====================================================");
            pw.println();

            // ===== PrintWriter.printf() — ФОРМАТИРОВАННЫЙ ВЫВОД (6.6) =====
            //
            // printf(String format, Object... args) — форматированная строка.
            //
            // Спецификаторы формата (основные):
            //   %s  — String (строка).
            //   %d  — int/long (целое число в десятичной системе).
            //   %f  — float/double (дробное число). %.2f — 2 знака после точки.
            //   %n  — перенос строки (платформо-зависимый: \n или \r\n).
            //   %% — литеральный символ % (экранирование).
            //
            // Флаги форматирования:
            //   %-20s  — минимум 20 символов, выравнивание ВЛЕВО.
            //   %+d    — всегда показывать знак (+42, -3).
            //   %,d    — разделитель тысяч (1,000,000). Зависит от Locale.
            //   %06d   — дополнить нулями до 6 символов (000042).
            pw.printf("Герой: %s%n", heroName);
            pw.printf("Дата:  %s%n", new java.util.Date());
            pw.println();

            // Заголовок таблицы с выравниванием.
            // %-6s — выровнять влево, минимум 6 символов.
            // %-50s — выровнять влево, минимум 50 символов (для длинных записей журнала).
            pw.printf("%-6s %-50s%n", "  №", "Запись");
            pw.printf("%-6s %-50s%n", "  --", "--------------------------------------------------");

            int num = 1;
            for (String entry : battleLog) {
                pw.printf("  %4d %s%n", num++, entry);
            }

            pw.println();
            pw.println("====================================================");
            pw.println("  СТАТИСТИКА");
            pw.println("====================================================");

            // ===== PrintWriter.format() — СИНОНИМ printf() (6.6) =====
            //
            // format() и printf() — абсолютно одинаковые методы!
            // Оба возвращают PrintWriter (для цепочки вызовов).
            // Разница только в названии: format() пришёл из Java 5, printf() — из C.
            //
            // Цепочка вызовов (method chaining):
            //   pw.format(...).format(...).format(...);
            //   Каждый format() возвращает тот же pw, поэтому можно вызывать друг за другом.
            pw.format("  %-30s %d%n", "Нанесено урона:", totalDamageDealt)
              .format("  %-30s %d%n", "Получено урона:", totalDamageReceived)
              .format("  %-30s %d%n", "Врагов побеждено:", enemiesDefeated)
              .format("  %-30s %d%n", "Исцеление:", totalHealing);

            // Разница урона — пример %+d (знак всегда показывается).
            int netDamage = totalDamageDealt - totalDamageReceived;
            pw.printf("%n  Баланс урона: %+d (положительный = в вашу пользу)%n", netDamage);

            // Процент: %.1f — одна цифра после точки. %% — символ процента.
            if (totalDamageDealt > 0) {
                double efficiency = (double) (totalDamageDealt - totalDamageReceived) / totalDamageDealt * 100;
                pw.printf("  Эффективность: %.1f%%%n", efficiency);
            }

            pw.println();
            pw.println("====================================================");

            // ===== PrintWriter.checkError() — ПРОВЕРКА ОШИБОК (6.6) =====
            //
            // PrintWriter НИКОГДА не выбрасывает IOException!
            // Это удобно (не нужен try-catch на каждый println), но опасно:
            //   если диск переполнен или файл заблокирован — данные потеряются МОЛЧА.
            //
            // checkError() — единственный способ узнать, была ли ошибка:
            //   true  — произошла ошибка при записи.
            //   false — всё в порядке.
            //
            // Вызывайте checkError() после серии записей, чтобы убедиться,
            // что данные действительно записаны.
            if (pw.checkError()) {
                System.out.println("✗ Ошибка при записи детального отчёта!");
            } else {
                System.out.println("✓ Детальный отчёт записан: " + detailedFilePath);
                System.out.println("  (использован PrintWriter → BufferedWriter → FileWriter)");
            }

        } catch (IOException e) {
            // IOException может возникнуть при создании FileWriter (открытие файла).
            // После создания PrintWriter ошибки записи НЕ выбрасываются (см. checkError выше).
            System.out.println("✗ Ошибка открытия файла для детального отчёта: " + e.getMessage());
        }

        // ===== ШАГ 4: ВЕРИФИКАЦИЯ — ЧТЕНИЕ ФАЙЛА ОБРАТНО (глава 6.8, 6.9) =====
        //
        // Демонстрация чтения текстового файла через BufferedReader(FileReader).
        //
        // Цепочка: BufferedReader → FileReader → файл.
        //   FileReader читает символы из файла (конвертирует byte → char).
        //   BufferedReader добавляет буферизацию и КЛЮЧЕВОЙ метод readLine().
        //
        // readLine() — читает одну строку текста (до символа новой строки).
        //   Возвращает String (без символа новой строки).
        //   Возвращает null, когда файл закончился (End Of File, EOF).
        //
        // Паттерн чтения файла строка за строкой:
        //   String line;
        //   while ((line = br.readLine()) != null) { ... }
        //
        // (line = br.readLine()) — присваивание внутри условия:
        //   1. Вызывается br.readLine() — читает следующую строку.
        //   2. Результат присваивается переменной line.
        //   3. Результат сравнивается с null.
        //   Это идиоматический паттерн Java для чтения потоков.
        System.out.println("\n--- Проверка: первые " + VERIFICATION_LINES + " строк из файла ---");

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {

            String line;
            int linesRead = 0;

            // ===== readLine() — ЧТЕНИЕ ОДНОЙ СТРОКИ (6.9) =====
            //
            // Цикл чтения с двумя условиями:
            //   1. line != null — файл ещё не закончился.
            //   2. linesRead < VERIFICATION_LINES — не превышен лимит строк.
            //
            // && — логическое И (short-circuit): если первое условие false,
            //   второе НЕ проверяется (оптимизация и защита от NullPointerException).
            while ((line = br.readLine()) != null && linesRead < VERIFICATION_LINES) {
                System.out.println("  " + line);
                linesRead++;
            }

            System.out.println("  ...");
            System.out.println("--- Конец проверки ---");

        } catch (IOException e) {
            // Ошибка чтения — файл мог быть удалён между записью и чтением,
            // или у нас нет прав на чтение.
            System.out.println("✗ Ошибка чтения файла для проверки: " + e.getMessage());
        }
    }
}
