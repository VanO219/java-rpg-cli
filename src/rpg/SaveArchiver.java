// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в DamageType.java).
package rpg;

// ===== ИМПОРТЫ ДЛЯ РАБОТЫ С ZIP-АРХИВАМИ (глава 6.12) =====
//
// Java предоставляет классы для работы с ZIP-форматом в пакете java.util.zip.
// ZIP — это формат архивирования и сжатия файлов:
//   - Один ZIP-файл может содержать НЕСКОЛЬКО файлов (entries).
//   - Каждый файл внутри ZIP называется ZipEntry.
//   - ZIP использует алгоритм DEFLATE для сжатия данных.
//
// ===== ИЕРАРХИЯ ПОТОКОВ ДЛЯ ZIP =====
//
// Запись в ZIP:
//   FileOutputStream       — запись байтов в файл (6.3)
//     └─ BufferedOutputStream — буферизация для производительности (6.5)
//       └─ ZipOutputStream    — добавление записей (entries) в ZIP-архив (6.12)
//
// Чтение из ZIP:
//   FileInputStream        — чтение байтов из файла (6.3)
//     └─ BufferedInputStream  — буферизация для производительности (6.5)
//       └─ ZipInputStream     — извлечение записей (entries) из ZIP-архива (6.12)
//
// Принцип ДЕКОРАТОРА (Decorator Pattern):
//   Каждый поток «оборачивает» предыдущий, добавляя функциональность.
//   ZipOutputStream не умеет писать в файл — он передаёт данные BufferedOutputStream,
//   который передаёт FileOutputStream, который записывает на диск.
//   Это как матрёшка: внешний слой добавляет ZIP-сжатие, средний — буферизацию,
//   внутренний — собственно запись в файл.

// ZipOutputStream — поток для СОЗДАНИЯ ZIP-архива.
// Основные методы:
//   putNextEntry(ZipEntry) — начать запись новой записи (файла) в архив.
//   write(byte[], off, len) — записать данные текущей записи.
//   closeEntry()           — завершить текущую запись (зафиксировать сжатие).
//   close()                — завершить ZIP-архив (записать центральный каталог).
//
// ВАЖНО: порядок вызовов строго определён:
//   1. putNextEntry() — «открыть» новый файл в архиве
//   2. write() — записать содержимое (можно несколько раз)
//   3. closeEntry() — «закрыть» файл в архиве
//   Повторить 1-3 для каждого файла.
import java.util.zip.ZipOutputStream;

// ZipInputStream — поток для ЧТЕНИЯ ZIP-архива.
// Основные методы:
//   getNextEntry() — перейти к следующей записи, вернуть ZipEntry (или null — конец архива).
//   read(byte[], off, len) — прочитать данные текущей записи.
//   closeEntry()           — завершить чтение текущей записи.
//
// Паттерн итерации по записям:
//   ZipEntry entry;
//   while ((entry = zis.getNextEntry()) != null) {
//       // обработать entry
//       zis.closeEntry();
//   }
import java.util.zip.ZipInputStream;

// ZipEntry — описание одного файла внутри ZIP-архива.
// Каждый файл в ZIP — это отдельный ZipEntry с метаданными:
//   getName()           — имя файла (относительный путь внутри архива).
//   getSize()           — размер НЕСЖАТЫХ данных (-1 если неизвестно).
//   getCompressedSize() — размер СЖАТЫХ данных.
//   getTime()           — время последней модификации (в миллисекундах).
//   getMethod()         — метод сжатия: ZipEntry.DEFLATED (сжатие) или ZipEntry.STORED (без сжатия).
//
// При СОЗДАНИИ ZipEntry достаточно указать только имя:
//   new ZipEntry("saves/hero_binary.dat")
// Остальные метаданные ZipOutputStream заполнит автоматически.
import java.util.zip.ZipEntry;

// ===== ИМПОРТЫ ПОТОКОВ ВВОДА-ВЫВОДА (глава 6.3, 6.5) =====

// FileOutputStream — байтовый поток для записи в файл (6.3).
// Создаёт файл, если не существует. По умолчанию ПЕРЕЗАПИСЫВАЕТ содержимое.
// Конструктор FileOutputStream(String name, boolean append):
//   append=false → перезаписать (по умолчанию)
//   append=true  → дописать в конец файла
import java.io.FileOutputStream;

// FileInputStream — байтовый поток для чтения из файла (6.3).
// Если файл не существует → FileNotFoundException (подкласс IOException).
import java.io.FileInputStream;

// BufferedOutputStream — буферизованный поток записи (6.5).
// Накапливает данные во внутреннем буфере (по умолчанию 8192 байт)
// и записывает в обёрнутый поток одним блоком.
// БЕЗ буферизации: каждый write() → системный вызов записи → МЕДЛЕННО.
// С буферизацией: много write() → один системный вызов → БЫСТРО.
import java.io.BufferedOutputStream;

// BufferedInputStream — буферизованный поток чтения (6.5).
// Аналогично: читает из файла блоками, а не побайтно.
import java.io.BufferedInputStream;

// File — представление файла или директории в файловой системе (6.11).
// НЕ содержит данные файла! Это только ПУТЬ + метаинформация.
// Основные методы:
//   exists()      — существует ли файл/директория?
//   isDirectory() — это директория?
//   listFiles()   — массив File[] содержимого директории.
//   getName()     — имя файла (без пути).
//   length()      — размер файла в байтах.
//   mkdirs()      — создать директорию (и все родительские).
import java.io.File;

// IOException — базовое проверяемое исключение для всех ошибок ввода-вывода.
// Все потоки могут бросить IOException: файл не найден, диск полон, нет прав доступа и т.д.
import java.io.IOException;

// Scanner — для чтения пользовательского ввода (y/n при распаковке).
import java.util.Scanner;

// ===== SAVEARCHIVER — РАБОТА С ZIP-АРХИВАМИ СОХРАНЕНИЙ (глава 6.12) =====
//
// Этот класс демонстрирует работу с ZIP-форматом через ZipOutputStream и ZipInputStream.
//
// ===== ЧТО ТАКОЕ ZIP? =====
//
// ZIP — один из самых популярных форматов архивирования файлов.
// Архив = контейнер, который содержит один или несколько файлов в сжатом виде.
//
// Структура ZIP-файла:
//   ┌─────────────────────────────────┐
//   │ [Local File Header 1]          │ ← метаданные первого файла
//   │ [File Data 1 (compressed)]     │ ← сжатые данные первого файла
//   │ [Local File Header 2]          │ ← метаданные второго файла
//   │ [File Data 2 (compressed)]     │ ← сжатые данные второго файла
//   │ ...                            │
//   │ [Central Directory]            │ ← каталог всех файлов в архиве
//   │ [End of Central Directory]     │ ← конец архива, указатель на каталог
//   └─────────────────────────────────┘
//
// Central Directory — это «оглавление» в конце архива.
// Благодаря ему можно быстро узнать список файлов без чтения всего архива.
//
// ===== ЗАЧЕМ БУФЕРИЗАЦИЯ? =====
//
// ZipOutputStream(new BufferedOutputStream(new FileOutputStream(file)))
//   — цепочка из трёх потоков (паттерн Декоратор).
//
// BufferedOutputStream собирает мелкие записи в буфер (8 КБ) и записывает
// на диск одним блоком. Без него каждый вызов write() = отдельный системный
// вызов ОС, что в десятки раз медленнее.
//
// ===== ПАТТЕРН ИСПОЛЬЗОВАНИЯ =====
//
// Экспорт: найти все файлы героя в saves/ → упаковать в ZIP.
// Импорт: открыть ZIP → распаковать файлы обратно в saves/.
// Список: показать .zip файлы в saves/.
public class SaveArchiver {

    // ===== КОНСТАНТА — РАЗМЕР БУФЕРА ДЛЯ КОПИРОВАНИЯ ДАННЫХ =====
    //
    // При копировании данных между потоками (файл → ZIP, ZIP → файл)
    // мы читаем и пишем порциями (chunks), а не побайтно.
    //
    // Размер буфера 4096 байт (4 КБ) — стандартный выбор:
    //   - Слишком маленький буфер (например, 64 байта) → много системных вызовов → медленно.
    //   - Слишком большой буфер (например, 1 МБ) → лишний расход памяти.
    //   - 4 КБ — компромисс: совпадает с типичным размером страницы памяти ОС.
    //
    // private — доступен только внутри класса SaveArchiver.
    // static  — принадлежит классу, а не экземпляру (один на всех).
    // final   — константа, значение нельзя изменить после инициализации.
    private static final int BUFFER_SIZE = 4096;

    // ===== exportToZip() — УПАКОВКА ФАЙЛОВ ГЕРОЯ В ZIP-АРХИВ =====
    //
    // Метод находит все файлы в директории saves/, имя которых начинается с heroName,
    // и упаковывает их в один ZIP-архив.
    //
    // Пример: героя зовут "артас".
    //   Файлы: артас_binary.dat, артас_object.sav, артас_battle_log.txt
    //   Результат: артас_backup.zip (содержит все три файла)
    //
    // Параметры:
    //   heroName — нормализованное имя героя (lowercase, без спецсимволов).
    //   savesDir — путь к директории сохранений (обычно "saves").
    //
    // ===== try-with-resources ДЛЯ ПОТОКОВ (глава 4.1, 6.2) =====
    //
    // try (var zos = new ZipOutputStream(...)) { ... }
    //   — поток автоматически закроется при выходе из блока try.
    //   Это КРИТИЧЕСКИ ВАЖНО для ZIP: close() записывает Central Directory.
    //   Без close() ZIP-файл будет повреждён (неполный — без оглавления)!
    public static void exportToZip(String heroName, String savesDir) {
        // ===== File — ПРЕДСТАВЛЕНИЕ ДИРЕКТОРИИ (6.11) =====
        //
        // new File(savesDir) — создаёт объект File для пути "saves".
        // Это НЕ создаёт файл на диске! File — лишь описание пути.
        // Чтобы проверить существование: dir.exists() и dir.isDirectory().
        // ===== ПРОВЕРКА ПРЕДУСЛОВИЯ: ИМЯ ГЕРОЯ НЕ null =====
        //
        // heroName используется в FilenameFilter: name.startsWith(heroName).
        // Если heroName == null, startsWith() кинет NullPointerException.
        // Проверяем до использования — принцип fail-fast.
        if (heroName == null) {
            System.out.println("[ZIP] Имя героя не задано — невозможно создать архив.");
            return;
        }

        File dir = new File(savesDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("[ZIP] Директория " + savesDir + " не найдена.");
            return;
        }

        // ===== File.listFiles(FilenameFilter) — ФИЛЬТРАЦИЯ ФАЙЛОВ (6.11) =====
        //
        // listFiles() без аргументов — возвращает ВСЕ файлы и папки.
        // listFiles(filter) — возвращает только те, что прошли фильтр.
        //
        // FilenameFilter — функциональный интерфейс с одним методом:
        //   boolean accept(File dir, String name)
        //   dir  — директория, в которой находится файл.
        //   name — имя файла (без пути).
        //
        // Мы используем лямбда-выражение (сокращённая запись анонимного класса):
        //   (d, name) -> name.startsWith(heroName) && !name.endsWith(".zip")
        //   — принимает файлы, начинающиеся с имени героя, исключая ZIP-файлы.
        //   Исключаем .zip, чтобы не упаковать старый бэкап в новый бэкап.
        File[] files = dir.listFiles((d, name) ->
                name.startsWith(heroName) && !name.endsWith(".zip"));

        // ===== ПРОВЕРКА: есть ли файлы для архивации? =====
        //
        // listFiles() может вернуть null (ошибка доступа) или пустой массив.
        // Обрабатываем оба случая: нет файлов → сообщаем и выходим.
        if (files == null || files.length == 0) {
            System.out.println("[ZIP] Нет файлов для архивации героя '" + heroName + "'.");
            return;
        }

        // Формируем имя ZIP-файла: saves/{heroName}_backup.zip
        File zipFile = new File(savesDir, heroName + "_backup.zip");

        // ===== try-with-resources + ЦЕПОЧКА ПОТОКОВ (6.2, 6.3, 6.5, 6.12) =====
        //
        // Создаём цепочку из трёх потоков (паттерн Декоратор):
        //   1. FileOutputStream(zipFile)  — записывает байты в файл на диске (6.3)
        //   2. BufferedOutputStream(...)   — буферизует запись, повышает производительность (6.5)
        //   3. ZipOutputStream(...)        — добавляет ZIP-сжатие и структуру архива (6.12)
        //
        // var — вывод типа (Java 10+): компилятор определяет тип ZipOutputStream автоматически.
        //
        // В try-with-resources достаточно указать ТОЛЬКО внешний поток (ZipOutputStream).
        // При его закрытии вызывается close() → BufferedOutputStream.close() → FileOutputStream.close().
        // Каскадное закрытие: каждый поток закрывает тот, который он обёрнул.
        try (var zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)))) {

            // Буфер для копирования данных из файла в ZIP.
            // Создаём один раз, переиспользуем для всех файлов (экономия памяти).
            byte[] buffer = new byte[BUFFER_SIZE];

            // Цикл по всем найденным файлам героя.
            for (File file : files) {

                // ===== ZipEntry — ОПИСАНИЕ ФАЙЛА ВНУТРИ АРХИВА (6.12) =====
                //
                // new ZipEntry(file.getName()) — создаём запись с именем файла.
                // file.getName() возвращает только имя (без пути): "артас_binary.dat".
                // Это имя будет использоваться при распаковке.
                //
                // ZipEntry хранит метаданные: имя, размер, CRC-32 контрольную сумму,
                // метод сжатия (DEFLATED/STORED), время модификации.
                // При записи через ZipOutputStream большинство заполняется автоматически.
                ZipEntry entry = new ZipEntry(file.getName());

                // ===== putNextEntry() — НАЧАЛО ЗАПИСИ НОВОГО ФАЙЛА В АРХИВ =====
                //
                // putNextEntry(entry) сигнализирует ZipOutputStream:
                //   "Сейчас я буду записывать данные для этого файла."
                // После putNextEntry() все вызовы write() относятся к этой записи.
                //
                // АНАЛОГИЯ: представь ZIP как книгу с главами.
                //   putNextEntry() — начинает новую главу (записывает заголовок).
                //   write()        — пишет текст главы.
                //   closeEntry()   — заканчивает главу (ставит точку).
                zos.putNextEntry(entry);

                // ===== ЧТЕНИЕ ИСХОДНОГО ФАЙЛА И ЗАПИСЬ В ZIP =====
                //
                // Открываем исходный файл для чтения через FileInputStream.
                // try-with-resources гарантирует закрытие FileInputStream.
                //
                // Паттерн «чтение блоками» (chunk reading):
                //   int bytesRead = fis.read(buffer);
                //   — читает до buffer.length байт в массив buffer.
                //   — возвращает ФАКТИЧЕСКОЕ количество прочитанных байт.
                //   — возвращает -1, если достигнут конец файла (EOF).
                //
                // Цикл: читаем блоками по BUFFER_SIZE байт, пока не дойдём до конца файла.
                // Последний блок может быть меньше BUFFER_SIZE — поэтому пишем bytesRead байт.
                try (var fis = new FileInputStream(file)) {
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        // zos.write(buffer, 0, bytesRead) — записать bytesRead байт из buffer.
                        //   buffer — массив с данными.
                        //   0      — начальная позиция в массиве (offset).
                        //   bytesRead — количество байт для записи.
                        //
                        // ВАЖНО: писать именно bytesRead, а НЕ buffer.length!
                        // Последний блок файла может быть меньше размера буфера.
                        // Если писать buffer.length — в ZIP попадут «мусорные» байты.
                        zos.write(buffer, 0, bytesRead);
                    }
                }

                // ===== closeEntry() — ЗАВЕРШЕНИЕ ЗАПИСИ ТЕКУЩЕГО ФАЙЛА =====
                //
                // closeEntry() сигнализирует ZipOutputStream:
                //   "Данные текущего файла закончились."
                // ZipOutputStream финализирует сжатие, вычисляет контрольную сумму CRC-32,
                // записывает размер сжатых/несжатых данных в заголовок записи.
                //
                // ВАЖНО: вызывать closeEntry() ПЕРЕД putNextEntry() для следующего файла!
                // Иначе данные двух файлов смешаются, и архив будет повреждён.
                zos.closeEntry();
            }

            System.out.println("[ZIP] Архив создан: " + zipFile.getName()
                    + " (" + files.length + " файл(ов))");

        } catch (IOException e) {
            // IOException — базовое исключение для всех ошибок ввода-вывода.
            // Возможные причины: нет прав записи, диск полон, файл заблокирован.
            // getMessage() — текст ошибки для пользователя.
            System.out.println("[ZIP] Ошибка при создании архива: " + e.getMessage());
        }
    }

    // ===== importFromZip() — РАСПАКОВКА ZIP-АРХИВА В ДИРЕКТОРИЮ СОХРАНЕНИЙ =====
    //
    // Метод читает ZIP-файл и извлекает все записи (файлы) обратно в saves/.
    // Если файл уже существует — спрашивает пользователя, перезаписать ли.
    //
    // Параметры:
    //   zipFilePath — полный путь к ZIP-файлу (например, "saves/артас_backup.zip").
    //   scanner     — Scanner для чтения пользовательского ввода (y/n).
    //
    // ===== ZipInputStream — ПОСЛЕДОВАТЕЛЬНОЕ ЧТЕНИЕ ZIP-АРХИВА =====
    //
    // ZipInputStream читает записи ПОСЛЕДОВАТЕЛЬНО (одна за другой):
    //   getNextEntry() → read() → closeEntry() → getNextEntry() → ...
    //
    // Это ПОТОКОВОЕ чтение: не нужно загружать весь архив в память.
    // Подходит для больших архивов — обрабатываем файл за файлом.
    //
    // Альтернатива: java.util.zip.ZipFile — читает Central Directory сразу,
    // позволяет произвольный доступ к записям (random access).
    // ZipFile быстрее для «прочитать одну запись из большого архива»,
    // ZipInputStream проще для «извлечь все записи последовательно».
    public static void importFromZip(String zipFilePath, String savesDir, Scanner scanner) {
        File zipFile = new File(zipFilePath);
        if (!zipFile.exists()) {
            System.out.println("[ZIP] Файл не найден: " + zipFilePath);
            return;
        }

        // Убеждаемся, что директория для распаковки существует.
        // mkdirs() создаёт директорию И все родительские (как mkdir -p в Linux).
        // Если директория уже существует — mkdirs() просто вернёт false (не ошибка).
        File targetDir = new File(savesDir);
        targetDir.mkdirs();

        int extractedCount = 0;
        int skippedCount = 0;

        // ===== try-with-resources + ЦЕПОЧКА ПОТОКОВ ДЛЯ ЧТЕНИЯ ZIP (6.12) =====
        //
        // Цепочка потоков (зеркальная к записи):
        //   1. FileInputStream(zipFile)   — читает байты из файла на диске (6.3)
        //   2. BufferedInputStream(...)    — буферизует чтение (6.5)
        //   3. ZipInputStream(...)         — разбирает ZIP-структуру, извлекает записи (6.12)
        try (var zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)))) {

            byte[] buffer = new byte[BUFFER_SIZE];

            // ===== ИТЕРАЦИЯ ПО ЗАПИСЯМ ZIP-АРХИВА =====
            //
            // getNextEntry() — переходит к следующей записи в архиве.
            //   Возвращает ZipEntry с метаданными файла.
            //   Возвращает null, когда все записи прочитаны (конец архива).
            //
            // Паттерн: присваивание в условии цикла.
            //   (entry = zis.getNextEntry()) != null
            //   — присвоить результат getNextEntry() переменной entry,
            //   — затем сравнить с null.
            //   Этот паттерн часто используется при чтении потоков.
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // entry.getName() — имя файла внутри архива (как было при упаковке).
                // Создаём File для целевого пути: saves/{имя_файла}.
                File targetFile = new File(targetDir, entry.getName());

                // ===== ЗАЩИТА ОТ ZipSlip-УЯЗВИМОСТИ (CWE-22: Path Traversal) =====
                //
                // ZipSlip — атака через вредоносный ZIP-архив.
                // Злоумышленник может создать ZIP с записью вида "../../etc/passwd"
                // или "../../../home/user/.bashrc". При распаковке без проверки
                // new File(targetDir, "../../etc/passwd") создаст путь ВЫШЕ целевой директории,
                // и файл будет записан в произвольное место файловой системы!
                //
                // Защита: сравниваем канонический (абсолютный, без ".." и символических ссылок)
                // путь целевого файла с каноническим путём целевой директории.
                // Если файл не начинается с директории — запись подозрительная, пропускаем.
                //
                // getCanonicalPath() — разрешает "..", ".", символические ссылки:
                //   "/home/user/saves/../../../etc/passwd" → "/etc/passwd"
                //   "/home/user/saves/hero.dat"            → "/home/user/saves/hero.dat"
                //
                // File.separator в конце canonicalDir — чтобы "/home/user/saves_evil"
                // не прошло проверку startsWith("/home/user/saves").
                //
                // Это стандартная проверка безопасности, рекомендуемая OWASP и Snyk.
                // Без неё нельзя распаковывать ZIP-архивы из ненадёжных источников!
                String canonicalTarget = targetFile.getCanonicalPath();
                String canonicalDir = targetDir.getCanonicalPath() + File.separator;
                if (!canonicalTarget.startsWith(canonicalDir)) {
                    System.out.println("[ZIP] Подозрительная запись: " + entry.getName() + " — пропущена.");
                    zis.closeEntry();
                    continue;
                }

                // ===== ПРОВЕРКА СУЩЕСТВОВАНИЯ ФАЙЛА + ЗАПРОС ПОЛЬЗОВАТЕЛЯ =====
                //
                // Если файл уже существует, спрашиваем пользователя через Scanner.
                // Это демонстрирует реальный сценарий: пользователь распаковывает бэкап,
                // но у него уже есть более новые сохранения — не затирать их без спроса.
                if (targetFile.exists()) {
                    System.out.print("[ZIP] Файл '" + entry.getName()
                            + "' уже существует. Перезаписать? (y/n): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (!answer.equals("y") && !answer.equals("д")) {
                        System.out.println("  Пропущен: " + entry.getName());
                        // closeEntry() — ОБЯЗАТЕЛЬНО вызвать, даже если пропускаем файл!
                        // Иначе ZipInputStream не перейдёт к следующей записи корректно.
                        zis.closeEntry();
                        skippedCount++;
                        continue;
                    }
                }

                // ===== ИЗВЛЕЧЕНИЕ ФАЙЛА ИЗ ZIP =====
                //
                // Записываем содержимое текущей ZIP-записи в файл.
                // FileOutputStream создаст файл (или перезапишет существующий).
                //
                // Паттерн чтения блоками — аналогичен записи в exportToZip():
                //   zis.read(buffer) — читает данные ТЕКУЩЕЙ записи (не всего архива!).
                //   Возвращает -1, когда данные текущей записи закончились.
                //
                // ВАЖНО: zis.read() читает только данные ТЕКУЩЕЙ ZipEntry.
                // Он автоматически остановится на границе записи.
                // Следующий getNextEntry() перейдёт к следующему файлу.
                try (var fos = new FileOutputStream(targetFile)) {
                    int bytesRead;
                    while ((bytesRead = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }

                System.out.println("  Извлечён: " + entry.getName());
                extractedCount++;

                // closeEntry() — завершить чтение текущей записи.
                // После этого getNextEntry() в следующей итерации цикла
                // перейдёт к следующему файлу в архиве.
                zis.closeEntry();
            }

            System.out.println("[ZIP] Распаковка завершена: извлечено " + extractedCount
                    + ", пропущено " + skippedCount + " файл(ов).");

        } catch (IOException e) {
            // ZipException (подкласс IOException) — специфичная ошибка ZIP-формата:
            //   повреждённый архив, неверная контрольная сумма, некорректная структура.
            // IOException — общая ошибка ввода-вывода: файл не найден, нет прав и т.д.
            System.out.println("[ZIP] Ошибка при распаковке: " + e.getMessage());
        }
    }

    // ===== listZipFiles() — СПИСОК ZIP-ФАЙЛОВ В ДИРЕКТОРИИ СОХРАНЕНИЙ =====
    //
    // Метод показывает все .zip файлы в указанной директории.
    // Используется для того, чтобы игрок мог выбрать архив для распаковки.
    //
    // Возвращает: массив File[] с ZIP-файлами, или null если файлов нет.
    //
    // ===== File.listFiles(FilenameFilter) — ФИЛЬТРАЦИЯ СОДЕРЖИМОГО ДИРЕКТОРИИ (6.11) =====
    //
    // listFiles() возвращает массив File[] (а НЕ коллекцию!).
    // Особенности:
    //   - Может вернуть null, если path не является директорией или ошибка доступа.
    //   - Порядок файлов НЕ гарантирован (зависит от файловой системы).
    //   - Каждый File в массиве — абсолютный или относительный путь.
    //
    // FilenameFilter — функциональный интерфейс:
    //   boolean accept(File dir, String name)
    //   Мы используем лямбда: (d, name) -> name.endsWith(".zip")
    //   — оставляем только файлы с расширением .zip.
    public static File[] listZipFiles(String savesDir) {
        File dir = new File(savesDir);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("[ZIP] Директория " + savesDir + " не найдена.");
            return null;
        }

        File[] zipFiles = dir.listFiles((d, name) -> name.endsWith(".zip"));

        if (zipFiles == null || zipFiles.length == 0) {
            System.out.println("[ZIP] ZIP-архивы не найдены в " + savesDir + ".");
            return null;
        }

        // Выводим пронумерованный список для удобства выбора.
        System.out.println("\n===== ZIP-АРХИВЫ =====");
        for (int i = 0; i < zipFiles.length; i++) {
            // zipFiles[i].getName() — имя файла без пути.
            // zipFiles[i].length()  — размер файла в байтах.
            //
            // Форматирование размера: length() возвращает long (байты).
            // Делим на 1024.0 для получения килобайт (double для дробной части).
            // %.1f — формат с одним знаком после запятой.
            System.out.printf("  %d. %s (%.1f КБ)%n",
                    i + 1, zipFiles[i].getName(), zipFiles[i].length() / 1024.0);
        }

        return zipFiles;
    }
}
