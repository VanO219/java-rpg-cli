// Пакет rpg — все классы нашей RPG-игры.
package rpg;

// ===== ИМПОРТЫ ПОТОКОВ ВВОДА-ВЫВОДА (глава 6.1 — Иерархия потоков) =====
//
// В Java ввод-вывод (I/O) построен на системе ПОТОКОВ (streams).
// Поток — это последовательность данных, текущих от источника к приёмнику.
//
// ========================================================================
//         ИЕРАРХИЯ БАЙТОВЫХ ПОТОКОВ (работают с byte, 8 бит)
// ========================================================================
//
// InputStream (абстрактный класс — корень чтения байтов)
//   │
//   ├── FileInputStream       — чтение из ФАЙЛА (глава 6.3)
//   │     Открывает файл и читает байты. Самый простой способ прочитать файл.
//   │     Конструктор: new FileInputStream("data.bin") или new FileInputStream(file)
//   │
//   ├── ByteArrayInputStream  — чтение из МАССИВА БАЙТОВ в памяти (глава 6.4)
//   │     Полезен для тестов или когда данные уже есть в byte[].
//   │
//   ├── FilterInputStream     — абстрактная основа для «обёрток» (глава 6.5)
//   │     Сам по себе FilterInputStream ничего не добавляет, только делегирует
//   │     вызовы вложенному потоку. Нужен как общий предок для декораторов.
//   │     │
//   │     ├── BufferedInputStream — БУФЕРИЗАЦИЯ чтения (глава 6.5)
//   │     │     Добавляет внутренний буфер (по умолчанию 8192 байта).
//   │     │     Вместо чтения по 1 байту с диска — читает блоками, кэшируя в памяти.
//   │     │     Использование: new BufferedInputStream(new FileInputStream("file"))
//   │     │
//   │     └── DataInputStream — чтение ПРИМИТИВНЫХ типов (глава 6.7)
//   │           Добавляет методы: readInt(), readLong(), readDouble(), readUTF(), readBoolean().
//   │           Обёртка: new DataInputStream(new BufferedInputStream(new FileInputStream("file")))
//   │
//   └── ObjectInputStream     — чтение ОБЪЕКТОВ через десериализацию (глава 6.10)
//         Метод readObject() воссоздаёт объект из байтов.
//         Объект должен реализовывать Serializable.
//         (Прямой наследник InputStream, не FilterInputStream — работает иначе.)
//
// OutputStream (абстрактный класс — корень записи байтов)
//   │
//   ├── FileOutputStream      — запись в ФАЙЛ (глава 6.3)
//   │     Создаёт файл (или перезаписывает существующий).
//   │     new FileOutputStream("data.bin") — перезапись.
//   │     new FileOutputStream("data.bin", true) — дозапись (append).
//   │
//   ├── ByteArrayOutputStream — запись в МАССИВ БАЙТОВ в памяти (глава 6.4)
//   │     Полезен для формирования данных в памяти перед записью на диск.
//   │
//   ├── FilterOutputStream    — абстрактная основа для «обёрток» записи (глава 6.5)
//   │     Аналог FilterInputStream, но для записи. Делегирует вызовы вложенному потоку.
//   │     │
//   │     ├── BufferedOutputStream — БУФЕРИЗАЦИЯ записи (глава 6.5)
//   │     │     Накапливает данные в буфере и записывает блоками.
//   │     │     Важно: вызвать flush() или close() для сброса последнего блока!
//   │     │
//   │     ├── DataOutputStream  — запись ПРИМИТИВНЫХ типов (глава 6.7)
//   │     │     Добавляет методы: writeInt(), writeLong(), writeDouble(), writeUTF(), writeBoolean().
//   │     │
//   │     └── PrintStream       — форматированный вывод (глава 6.6)
//   │           System.out — это PrintStream! Методы: print(), println(), printf().
//   │
//   └── ObjectOutputStream    — запись ОБЪЕКТОВ через сериализацию (глава 6.10)
//         Метод writeObject(obj) преобразует объект в последовательность байтов.
//         (Прямой наследник OutputStream, не FilterOutputStream.)
//
// ========================================================================
//             ПАТТЕРН ДЕКОРАТОР (Decorator Pattern) В ПОТОКАХ
// ========================================================================
//
// Потоки в Java используют паттерн «Декоратор»:
//   - Базовый поток (FileInputStream) предоставляет основную функциональность.
//   - Обёртки (BufferedInputStream, DataInputStream) ДОБАВЛЯЮТ возможности.
//   - Обёртки принимают другой поток через конструктор.
//
// Пример цепочки (от внешнего к внутреннему):
//   DataOutputStream          → методы writeInt(), writeUTF()
//     └── BufferedOutputStream → буферизация (запись блоками)
//           └── FileOutputStream → физическая запись на диск
//
// Код:
//   var dos = new DataOutputStream(
//                 new BufferedOutputStream(
//                     new FileOutputStream("save.dat")));
//
// Каждый слой добавляет функциональность, не изменяя внутренний поток.
// Это позволяет комбинировать возможности: буферизация + типизация + файл.
//
// ========================================================================
//         РАЗНИЦА: БАЙТОВЫЕ vs СИМВОЛЬНЫЕ ПОТОКИ
// ========================================================================
//
// Байтовые потоки (InputStream/OutputStream):
//   - Работают с БАЙТАМИ (byte, 8 бит).
//   - Используются для бинарных данных: изображения, сериализованные объекты, .dat файлы.
//   - Методы: read(), write(byte), readInt(), writeObject().
//
// Символьные потоки (Reader/Writer):
//   - Работают с СИМВОЛАМИ (char, 16 бит, Unicode).
//   - Используются для ТЕКСТОВЫХ данных: .txt, .csv, .json файлы.
//   - Методы: read(), write(char), readLine(), write(String).
//   - Автоматически учитывают кодировку (UTF-8, UTF-16, и т.д.).
//
// Правило выбора:
//   ТЕКСТ → Reader/Writer (корректная работа с Unicode и кодировками).
//   БИНАРНЫЕ ДАННЫЕ → InputStream/OutputStream (побайтовая точность).
//
// В этом файле мы используем БАЙТОВЫЕ потоки, т.к. сохраняем игровые данные
// в бинарном формате (DataOutputStream) и объектном формате (ObjectOutputStream).
//

// FileInputStream — чтение байтов из файла (глава 6.3).
// Самый базовый поток для чтения: открывает файл и читает побайтово.
import java.io.FileInputStream;

// FileOutputStream — запись байтов в файл (глава 6.3).
// Создаёт файл если его нет, или перезаписывает существующий.
import java.io.FileOutputStream;

// BufferedInputStream — буферизированное чтение (глава 6.5).
// Оборачивает FileInputStream, добавляя внутренний буфер (8 КБ по умолчанию).
// Без буферизации: каждый read() = обращение к диску (медленно).
// С буферизацией: читаем блок 8 КБ за раз, дальше отдаём из памяти (быстро).
import java.io.BufferedInputStream;

// BufferedOutputStream — буферизированная запись (глава 6.5).
// Аналогично: накапливает данные в буфере и записывает блоками.
// ВАЖНО: при close() буфер автоматически сбрасывается (flush).
// Если не закрыть поток — последние данные могут не записаться!
import java.io.BufferedOutputStream;

// DataInputStream — чтение примитивных Java-типов (глава 6.7).
// Добавляет к потоку методы для чтения конкретных типов:
//   readInt()    — читает 4 байта как int
//   readLong()   — читает 8 байт как long
//   readDouble() — читает 8 байт как double
//   readUTF()    — читает строку в модифицированном UTF-8 формате
//   readByte()   — читает 1 байт как byte
//   readBoolean()— читает 1 байт как boolean
//
// Данные ДОЛЖНЫ читаться в ТОМ ЖЕ ПОРЯДКЕ, в котором были записаны!
// Если записали writeInt() → writeUTF(), то читать нужно readInt() → readUTF().
// Нарушение порядка → мусорные данные или исключение.
import java.io.DataInputStream;

// DataOutputStream — запись примитивных Java-типов (глава 6.7).
// Парный класс к DataInputStream. Методы:
//   writeInt(42)     — записывает 4 байта (big-endian)
//   writeLong(100L)  — записывает 8 байт
//   writeDouble(3.14)— записывает 8 байт (IEEE 754)
//   writeUTF("Текст")— записывает длину (2 байта) + символы в Modified UTF-8
//   writeByte(0xFF)  — записывает 1 байт
//   writeBoolean(true)— записывает 1 байт (0 или 1)
//
// Порядок записи фиксирует ФОРМАТ файла.
// Формат нужно документировать, чтобы loadBinary() читал в том же порядке.
import java.io.DataOutputStream;

// ObjectInputStream — десериализация объектов из потока байтов (глава 6.10).
// Метод readObject() воссоздаёт объект из последовательности байтов.
// Возвращает Object — нужно приведение типа (cast):
//   Game game = (Game) ois.readObject();
//
// Требования:
//   1. Класс объекта реализует Serializable.
//   2. serialVersionUID совпадает (иначе InvalidClassException).
//   3. ВСЕ поля объекта тоже Serializable (или помечены transient).
import java.io.ObjectInputStream;

// ObjectOutputStream — сериализация объектов в поток байтов (глава 6.10).
// Метод writeObject(obj) преобразует ВЕСЬ граф объектов в байты:
//   - Все поля объекта (включая вложенные объекты) записываются рекурсивно.
//   - Поля, помеченные transient, ПРОПУСКАЮТСЯ.
//   - Поля, помеченные static, ПРОПУСКАЮТСЯ (static не принадлежит экземпляру).
//
// Сериализация сохраняет ПОЛНОЕ состояние объекта автоматически —
// не нужно вручную записывать каждое поле (в отличие от DataOutputStream).
// Цена удобства: больший размер файла, привязка к структуре классов.
import java.io.ObjectOutputStream;

// IOException — базовое checked-исключение для всех ошибок ввода-вывода.
// Checked означает: компилятор ЗАСТАВЛЯЕТ обработать (try-catch или throws).
// Примеры ситуаций: файл не найден, нет прав доступа, диск переполнен,
// сетевое соединение оборвалось, файл повреждён.
import java.io.IOException;

// EOFException — конец файла достигнут раньше, чем ожидалось (глава 6.7).
// Наследник IOException. Возникает при readInt()/readUTF(), если файл обрезан.
// Пример: записали 10 полей, но файл повредился и содержит только 7.
import java.io.EOFException;

// InvalidClassException — ошибка десериализации: класс изменился (глава 6.10).
// Возникает, когда serialVersionUID в файле не совпадает с serialVersionUID в классе.
// Это значит: класс был изменён после сохранения (добавлены/удалены поля).
// Решение: либо не менять класс, либо писать custom readObject()/writeObject().
import java.io.InvalidClassException;

// ClassNotFoundException — класс не найден при десериализации (глава 6.10).
// Возникает, если файл содержит объект класса, которого нет в classpath.
// Пример: сохранили объект класса Paladin, потом удалили Paladin.java.
//
// ПРИМЕЧАНИЕ: ClassNotFoundException находится в пакете java.lang (не java.io!).
// Классы из java.lang импортируются автоматически — import не нужен.
// Но readObject() объявляет throws ClassNotFoundException, поэтому
// мы обрабатываем его в catch-блоке loadObject().

// FileNotFoundException — файл не найден (глава 6.3).
// Наследник IOException. Возникает при попытке открыть несуществующий файл для чтения.
import java.io.FileNotFoundException;

// ===== КЛАСС FILE — РАБОТА С ФАЙЛОВОЙ СИСТЕМОЙ (глава 6.11) =====
//
// File — это НЕ файл, а ПУТЬ к файлу или директории.
// File не открывает файл и не читает его содержимое!
// File предоставляет информацию о файловой системе:
//
// Создание:
//   new File("saves")           — относительный путь (от рабочей директории)
//   new File("saves/game.dat")  — путь к файлу
//   new File("saves", "game.dat") — родитель + потомок
//
// Проверки:
//   file.exists()       — существует ли файл/директория?
//   file.isFile()       — это файл? (не директория)
//   file.isDirectory()  — это директория?
//   file.canRead()      — можно читать?
//   file.canWrite()     — можно писать?
//
// Информация:
//   file.getName()         — имя файла ("game.dat")
//   file.getAbsolutePath() — полный путь
//   file.length()          — размер в байтах
//   file.lastModified()    — время последнего изменения (epoch ms)
//
// Директории:
//   file.mkdir()    — создать одну директорию
//   file.mkdirs()   — создать директорию И все промежуточные (как mkdir -p в Linux)
//   file.listFiles()— массив File[] содержимого директории
//   file.listFiles(filter) — с фильтром (лямбда или FileFilter)
//
// Операции:
//   file.delete()   — удалить файл или ПУСТУЮ директорию
//   file.renameTo() — переименовать/переместить
//
// ВАЖНО: File из пакета java.io — старый API (Java 1.0).
// Современная альтернатива: java.nio.file.Path + Files (Java 7+).
// В учебных целях используем File, т.к. это тема главы 6.11.
import java.io.File;

// ===== GAMESAVEMANAGER — УПРАВЛЕНИЕ СОХРАНЕНИЯМИ ИГРЫ =====
//
// Этот класс демонстрирует:
//   1. Иерархию потоков (6.1) — цепочки декораторов.
//   2. Try-with-resources (6.2) — автоматическое закрытие потоков.
//   3. FileInputStream/FileOutputStream (6.3) — чтение/запись файлов.
//   4. BufferedInputStream/BufferedOutputStream (6.5) — буферизация.
//   5. DataInputStream/DataOutputStream (6.7) — примитивные типы.
//   6. ObjectInputStream/ObjectOutputStream (6.10) — сериализация/десериализация.
//   7. File (6.11) — работа с директориями и списками файлов.
//
// Два режима сохранения:
//   БИНАРНЫЙ (DataStreams) — ручная запись каждого поля. Полный контроль формата.
//     Плюсы: компактный файл, не зависит от структуры классов.
//     Минусы: нужно вручную поддерживать порядок чтения/записи.
//
//   ОБЪЕКТНЫЙ (Serializable) — одна команда writeObject(game).
//     Плюсы: автоматически сохраняет весь граф объектов.
//     Минусы: файл больше, привязан к версии классов (serialVersionUID).
//
public class GameSaveManager {

    // ===== КОНСТАНТА ДИРЕКТОРИИ СОХРАНЕНИЙ (глава 6.11) =====
    //
    // static — поле принадлежит классу, а не экземпляру.
    // final — значение нельзя изменить после инициализации.
    // String — неизменяемый (immutable) тип: "saves" нельзя модифицировать.
    //
    // Все сохранения хранятся в поддиректории "saves/" относительно рабочей директории.
    // Рабочая директория — откуда запущена программа (обычно корень проекта).
    private static final String SAVES_DIR = "saves";

    // ===== РАСШИРЕНИЯ ФАЙЛОВ — ИДЕНТИФИКАЦИЯ ТИПА СОХРАНЕНИЯ =====
    //
    // .dat — бинарные сохранения (DataOutputStream).
    // .sav — объектные сохранения (ObjectOutputStream/Serializable).
    //
    // Используются в listSaves() для фильтрации файлов.
    private static final String BINARY_EXTENSION = ".dat";
    private static final String OBJECT_EXTENSION = ".sav";

    // ===== File savesDirectory — ОБЪЕКТ ДИРЕКТОРИИ СОХРАНЕНИЙ (глава 6.11) =====
    //
    // File — это объект, представляющий ПУТЬ в файловой системе.
    // new File("saves") создаёт объект пути, но НЕ создаёт директорию!
    // Директорию нужно создать отдельно через mkdirs().
    //
    // private final — поле доступно только внутри класса и не переназначается.
    private final File savesDirectory;

    // ===== КОНСТРУКТОР — ИНИЦИАЛИЗАЦИЯ МЕНЕДЖЕРА СОХРАНЕНИЙ =====
    //
    // Конструктор создаёт объект File для директории сохранений
    // и вызывает ensureSavesDir() для создания директории, если её нет.
    //
    // Без параметров — используется директория по умолчанию "saves".
    public GameSaveManager() {
        // new File(SAVES_DIR) — создаёт объект File для пути "saves".
        // Это относительный путь — отсчитывается от рабочей директории программы.
        // Сам файл/директория на диске НЕ создаётся этой строкой!
        this.savesDirectory = new File(SAVES_DIR);

        // ensureSavesDir() — гарантирует, что директория существует.
        ensureSavesDir();
    }

    // ===== КОНСТРУКТОР С ПАРАМЕТРОМ — КАСТОМНАЯ ДИРЕКТОРИЯ =====
    //
    // Перегрузка конструктора (overloading): два конструктора с разными параметрами.
    // Этот конструктор позволяет задать произвольную директорию для сохранений.
    // Полезно для тестирования: можно сохранять во временную директорию.
    public GameSaveManager(String savesDir) {
        this.savesDirectory = new File(savesDir);
        ensureSavesDir();
    }

    // ===== ensureSavesDir() — СОЗДАНИЕ ДИРЕКТОРИИ ЧЕРЕЗ File (глава 6.11) =====
    //
    // Метод проверяет существование директории и создаёт её при необходимости.
    //
    // File.exists()      — true, если путь существует (файл ИЛИ директория).
    // File.isDirectory() — true, если это именно директория (не файл).
    // File.mkdirs()      — создаёт директорию И ВСЕ промежуточные директории.
    //   Аналог команды `mkdir -p saves/backups/old` в Linux.
    //   mkdir() (без s) — создаёт только ОДНУ директорию (без промежуточных).
    //   mkdirs() безопаснее: создаёт всю цепочку, не падает если часть уже есть.
    //
    // Возвращает true если директория создана, false если уже существовала.
    private boolean ensureSavesDir() {
        // exists() — проверяем, существует ли что-то по этому пути.
        if (savesDirectory.exists()) {
            // isDirectory() — дополнительная проверка: а вдруг это файл с таким именем?
            // Если по пути "saves" существует ФАЙЛ (не директория), это проблема.
            if (!savesDirectory.isDirectory()) {
                System.out.println("[Ошибка] Путь '" + savesDirectory.getAbsolutePath()
                        + "' существует, но это не директория!");
                return false;
            }
            // Директория уже существует — ничего делать не нужно.
            return true;
        }

        // mkdirs() — создаём директорию (и все промежуточные, если нужно).
        // Возвращает true при успехе, false при неудаче (нет прав, и т.д.).
        boolean created = savesDirectory.mkdirs();
        if (created) {
            System.out.println("[Сохранения] Создана директория: " + savesDirectory.getAbsolutePath());
        } else {
            System.out.println("[Ошибка] Не удалось создать директорию: " + savesDirectory.getAbsolutePath());
        }
        return created;
    }

    // ===== БИНАРНОЕ СОХРАНЕНИЕ — DataOutputStream (глава 6.3, 6.5, 6.7) =====
    //
    // Сохраняет ВСЕ игровые данные в бинарном формате.
    // Каждое поле записывается ВРУЧНУЮ через writeInt(), writeUTF(), и т.д.
    //
    // Цепочка потоков (паттерн Декоратор):
    //   DataOutputStream        → добавляет writeInt(), writeUTF(), writeDouble()
    //     └── BufferedOutputStream → буферизация (запись блоками по 8 КБ)
    //           └── FileOutputStream → физическая запись на диск
    //
    // Зачем BufferedOutputStream?
    //   Без буфера: каждый writeInt() = системный вызов записи на диск (4 байта за раз).
    //   С буфером: данные копятся в памяти, записываются блоком (8192 байта за раз).
    //   Результат: в ~100 раз быстрее для множества мелких записей.
    //
    // Формат файла (порядок записи СТРОГО ФИКСИРОВАН):
    //   1. Тип героя (UTF), имя, health, maxHealth, attack, defense, level, experience
    //   2. Специфичные поля подкласса: rage / mana / critChance
    //   3. Инвентарь: capacity, size, затем каждый слот (itemName, itemValue, quantity)
    //   4. gameState (UTF), statusFlags (byte)
    //   5. Счётчики: 6 × writeInt()
    //   6. Достижения: size + enum name каждого
    //   7. Бестиарий: size + данные каждой записи
    //   8. Таблица рекордов: size + данные каждой записи
    //   9. Журнал квестов: size + каждая строка
    //
    // ВНИМАНИЕ: loadBinary() ОБЯЗАН читать поля РОВНО в том же порядке!
    // Изменение порядка или добавление/удаление полей = повреждение формата.
    //
    // Параметр game — объект Game, из которого берутся данные.
    // Метод вызывает package-private геттеры Game (будут добавлены в Batch 3):
    //   getHero(), getInventory(), getGameState(), getStatusFlags(),
    //   getTotalDamageDealt(), getTotalDamageReceived(), getEnemiesDefeated(),
    //   getTotalHealing(), getSpecialAttackCount(), getLootItemsCollected(),
    //   getAchievements(), getBestiary(), getLeaderboard(), getQuestLog()
    //
    public void saveBinary(Game game) {
        // Получаем героя для определения типа и имени файла.
        // getHero() — package-private геттер (будет добавлен в Game.java, Batch 3).
        GameCharacter hero = game.getHero();

        // Формируем имя файла: "арТас Великий" → "артас_великий_binary.dat"
        String fileName = normalizeHeroName(hero.getName()) + "_binary" + BINARY_EXTENSION;

        // new File(savesDirectory, fileName) — конструктор File с родителем и потомком.
        // Создаёт путь: "saves/артас_великий_binary.dat".
        // Не создаёт файл на диске — только объект пути.
        File saveFile = new File(savesDirectory, fileName);

        // success — флаг успешной записи.
        // Объявлен ДО try-with-resources, чтобы быть доступным ПОСЛЕ блока.
        // Зачем: мы хотим вывести размер файла ПОСЛЕ закрытия потока,
        // иначе saveFile.length() вернёт 0 — данные ещё в буфере BufferedOutputStream.
        // Это типичная ошибка: вызов File.length() внутри try, пока буфер не сброшен.
        boolean success = false;

        // ===== TRY-WITH-RESOURCES — АВТОМАТИЧЕСКОЕ ЗАКРЫТИЕ ПОТОКОВ (глава 6.2) =====
        //
        // Синтаксис: try (ресурс = new ...) { ... }
        // Ресурс ДОЛЖЕН реализовывать интерфейс AutoCloseable (метод close()).
        // Все потоки ввода-вывода реализуют AutoCloseable.
        //
        // При выходе из блока try (нормально ИЛИ по исключению) вызывается close().
        // Это ГАРАНТИРУЕТ: файл будет закрыт, буфер будет сброшен (flush).
        //
        // Без try-with-resources пришлось бы писать finally { stream.close(); },
        // а при ошибке в close() — вложенный try-catch. Кода в 3 раза больше!
        //
        // Здесь объявляем ОДНУ переменную — самый внешний поток (DataOutputStream).
        // Закрытие внешнего потока КАСКАДНО закрывает все вложенные:
        //   dos.close() → BufferedOutputStream.close() → FileOutputStream.close()
        //
        // Цепочка декораторов (читать изнутри наружу):
        //   FileOutputStream    — открывает файл для записи
        //   BufferedOutputStream — добавляет буфер 8 КБ
        //   DataOutputStream    — добавляет writeInt(), writeUTF() и т.д.
        try (DataOutputStream dos = new DataOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(saveFile)))) {

            // ===== ЗАПИСЬ ДАННЫХ ГЕРОЯ (writeUTF, writeInt) =====
            //
            // writeUTF(String) — записывает строку в модифицированном формате UTF-8.
            //   Формат: 2 байта (длина строки) + символы в Modified UTF-8.
            //   Максимальная длина: 65535 байт (ограничение 2 байтов на длину).
            //   Русские символы занимают 2 байта каждый (в Modified UTF-8).
            //
            // writeInt(int) — записывает 4 байта в порядке big-endian.
            //   Big-endian: старший байт первым. Пример: 258 → [0, 0, 1, 2].
            //   Java ВСЕГДА использует big-endian для DataOutputStream.

            // 1. Тип героя — строка: "Warrior", "Mage" или "Archer".
            // getClassName() — абстрактный метод GameCharacter, реализован в подклассах.
            dos.writeUTF(hero.getClassName());

            // 2. Имя героя.
            dos.writeUTF(hero.getName());

            // 3-8. Базовые числовые поля героя.
            dos.writeInt(hero.getHealth());
            dos.writeInt(hero.getMaxHealth());
            dos.writeInt(hero.getAttack());
            dos.writeInt(hero.getDefense());
            dos.writeInt(hero.getLevel());

            // experience — protected поле в GameCharacter, нет публичного геттера.
            // getExperience() — package-private геттер (будет добавлен в Batch 3).
            dos.writeInt(hero.getExperience());

            // ===== СПЕЦИФИЧНЫЕ ПОЛЯ ПОДКЛАССОВ =====
            //
            // Каждый подкласс героя имеет уникальные поля:
            //   Warrior — rage (int): ярость, накапливается в бою.
            //   Mage    — mana (int): мана, тратится на заклинания.
            //   Archer  — critChance (double): шанс критического удара.
            //
            // instanceof — оператор проверки типа (глава 3.5).
            // Pattern matching instanceof (Java 16+, глава 3.26):
            //   if (hero instanceof Warrior w) — проверяет тип И создаёт переменную w.
            //   Не нужно отдельное приведение: Warrior w = (Warrior) hero;
            //
            // writeDouble(double) — записывает 8 байт в формате IEEE 754.
            if (hero instanceof Warrior w) {
                dos.writeInt(w.getRage());
            } else if (hero instanceof Mage m) {
                dos.writeInt(m.getMana());
            } else if (hero instanceof Archer a) {
                dos.writeDouble(a.getCritChance());
            }

            // ===== ЗАПИСЬ ИНВЕНТАРЯ =====
            //
            // Инвентарь — Inventory<Inventory.ItemInfo> с Object[] slots внутри.
            // Записываем: capacity (макс. размер), size (текущий), затем каждый слот.
            // Для каждого слота: имя предмета (String), стоимость (int), количество (int).
            Inventory<Inventory.ItemInfo> inventory = game.getInventory();
            dos.writeInt(inventory.getCapacity());
            dos.writeInt(inventory.getSize());

            // Итерируем по занятым слотам.
            // getSlot(i) возвращает Inventory.Slot с методами getItem() и getQuantity().
            // getItem() возвращает ItemInfo с getName() и getValue().
            for (int i = 0; i < inventory.getSize(); i++) {
                Inventory<Inventory.ItemInfo>.Slot slot = inventory.getSlot(i);
                Inventory.ItemInfo item = slot.getItem();
                dos.writeUTF(item.getName());
                dos.writeInt(item.getValue());
                dos.writeInt(slot.getQuantity());
            }

            // ===== ЗАПИСЬ СОСТОЯНИЯ ИГРЫ =====
            //
            // GameState — enum. Для записи в бинарный файл сохраняем имя: name().
            // name() возвращает строку — имя константы enum ("EXPLORING", "BATTLE", ...).
            // Восстановление: GameState.valueOf("EXPLORING") → GameState.EXPLORING.
            dos.writeUTF(game.getGameState().name());

            // writeByte(int) — записывает ОДИН байт (младшие 8 бит параметра).
            // statusFlags — битовые флаги состояния (POISONED, STUNNED, SHIELDED, ENRAGED).
            dos.writeByte(game.getStatusFlags());

            // ===== ЗАПИСЬ СЧЁТЧИКОВ =====
            //
            // 6 целочисленных счётчиков — статистика за всю игру.
            // Записываем подряд через writeInt(). При чтении — readInt() в том же порядке.
            dos.writeInt(game.getTotalDamageDealt());
            dos.writeInt(game.getTotalDamageReceived());
            dos.writeInt(game.getEnemiesDefeated());
            dos.writeInt(game.getTotalHealing());
            dos.writeInt(game.getSpecialAttackCount());
            dos.writeInt(game.getLootItemsCollected());

            // ===== ЗАПИСЬ КОЛЛЕКЦИЙ =====
            //
            // Общий паттерн для коллекций в бинарном формате:
            //   1. writeInt(size) — количество элементов.
            //   2. Цикл: записать каждый элемент.
            //
            // При чтении:
            //   1. int size = readInt() — узнаём сколько элементов читать.
            //   2. Цикл size раз: прочитать каждый элемент.
            //
            // Это стандартный приём для сериализации коллекций переменной длины.

            // --- Достижения (Set<Achievement>) ---
            // Achievement — enum. Сохраняем через name().
            // Set не гарантирует порядок, но для сохранения/загрузки порядок не важен.
            java.util.Set<Achievement> achievements = game.getAchievements();
            dos.writeInt(achievements.size());
            for (Achievement a : achievements) {
                dos.writeUTF(a.name());
            }

            // --- Бестиарий (Bestiary = TreeMap<String, BestiaryEntry>) ---
            // Bestiary реализует Iterable<Map.Entry<String, BestiaryEntry>>.
            // Итерация через for-each: каждый элемент — пара (имя врага, данные).
            //
            // BestiaryEntry — record: killCount (int), maxDamageDealt (int),
            //   rank (EnemyRank enum), firstEncounter (long).
            //
            // writeLong(long) — записывает 8 байт (для timestamp).
            Bestiary bestiary = game.getBestiary();
            dos.writeInt(bestiary.size());
            for (java.util.Map.Entry<String, Bestiary.BestiaryEntry> entry : bestiary) {
                dos.writeUTF(entry.getKey());
                Bestiary.BestiaryEntry be = entry.getValue();
                dos.writeInt(be.killCount());
                dos.writeInt(be.maxDamageDealt());
                dos.writeUTF(be.rank().name());
                dos.writeLong(be.firstEncounter());
            }

            // --- Таблица рекордов (TreeSet<BattleRecord>) ---
            // BattleRecord — record: heroName (String), score (long),
            //   enemiesDefeated (int), timestamp (long).
            java.util.TreeSet<BattleRecord> leaderboard = game.getLeaderboard();
            dos.writeInt(leaderboard.size());
            for (BattleRecord br : leaderboard) {
                dos.writeUTF(br.heroName());
                dos.writeLong(br.score());
                dos.writeInt(br.enemiesDefeated());
                dos.writeLong(br.timestamp());
            }

            // --- Журнал квестов (LinkedList<String>) ---
            // LinkedList<String> — двусвязный список строк.
            // Каждая строка — запись о событии в игре.
            java.util.LinkedList<String> questLog = game.getQuestLog();
            dos.writeInt(questLog.size());
            for (String quest : questLog) {
                dos.writeUTF(quest);
            }

            // Если дошли сюда без исключений — запись прошла успешно.
            // Устанавливаем флаг, чтобы ПОСЛЕ закрытия потока вывести размер файла.
            success = true;

        } catch (IOException e) {
            // IOException — базовое исключение для ВСЕХ ошибок ввода-вывода.
            // Checked exception — компилятор требует обработки.
            //
            // getMessage() — текстовое описание ошибки.
            // Возможные причины: нет прав записи, диск переполнен, путь недоступен.
            System.out.println("[Ошибка] Не удалось сохранить игру: " + e.getMessage());
        }

        // Выводим информацию о файле ПОСЛЕ закрытия потока (после блока try-with-resources).
        // Почему не внутри try: BufferedOutputStream копит данные в буфере (по умолчанию 8 КБ).
        // close() вызывает flush() → данные сбрасываются на диск → File.length() возвращает
        // реальный размер. Внутри try буфер ещё не сброшен и length() вернёт 0.
        if (success) {
            // saveFile.getAbsolutePath() — полный путь к файлу (глава 6.11).
            System.out.println("[Сохранение] Бинарное сохранение: " + saveFile.getAbsolutePath());
            // saveFile.length() — размер файла в байтах (глава 6.11).
            System.out.println("[Сохранение] Размер файла: " + saveFile.length() + " байт");
        }
    }

    // ===== БИНАРНАЯ ЗАГРУЗКА — DataInputStream (глава 6.3, 6.5, 6.7) =====
    //
    // Читает бинарный файл и восстанавливает состояние игры.
    // Порядок чтения СТРОГО соответствует порядку записи в saveBinary().
    //
    // Цепочка потоков (паттерн Декоратор, зеркально к записи):
    //   DataInputStream         → readInt(), readUTF(), readDouble()
    //     └── BufferedInputStream → буферизация чтения
    //           └── FileInputStream → физическое чтение с диска
    //
    // Параметр game — объект Game, в который загружаются данные.
    //   Метод вызывает game.restoreState(...) для установки всех полей.
    //   restoreState() — package-private метод (будет добавлен в Batch 3).
    //
    // Параметр filePath — имя файла (только имя, без директории), например "артас_binary.dat".
    //
    // Возвращает true при успешной загрузке, false при ошибке.
    //
    public boolean loadBinary(Game game, String filePath) {
        File saveFile = new File(savesDirectory, filePath);

        // exists() и isFile() — проверки File (глава 6.11).
        // isFile() отличает файл от директории: new File("saves").isFile() → false.
        if (!saveFile.exists() || !saveFile.isFile()) {
            System.out.println("[Ошибка] Файл не найден: " + saveFile.getAbsolutePath());
            return false;
        }

        // ===== TRY-WITH-RESOURCES ДЛЯ ЧТЕНИЯ (глава 6.2) =====
        //
        // Тот же принцип: объявляем ресурс в try(...), он закроется автоматически.
        // DataInputStream оборачивает BufferedInputStream, который оборачивает FileInputStream.
        //
        // При ошибке (поврежённый файл, неполные данные) выбросится:
        //   EOFException    — файл закончился раньше, чем прочитаны все поля.
        //   IOException     — общая ошибка ввода-вывода.
        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(saveFile)))) {

            // ===== ЧТЕНИЕ В СТРОГО ТОМ ЖЕ ПОРЯДКЕ, ЧТО И ЗАПИСЬ =====
            //
            // Это КРИТИЧЕСКИ ВАЖНО для DataStreams!
            // DataOutputStream записывает "сырые" байты без метаданных:
            //   writeInt(42) → [0, 0, 0, 42] (4 байта, без маркера "это int").
            //   writeUTF("Hi") → [0, 2, 72, 105] (2 байта длины + символы).
            //
            // Если при записи было writeUTF → writeInt, а при чтении readInt → readUTF,
            // то readInt() интерпретирует байты строки как число → мусор!
            // Файл не содержит информации о типах — порядок ФИКСИРОВАН программистом.

            // 1. Тип героя.
            String heroType = dis.readUTF();
            // 2. Имя героя.
            String heroName = dis.readUTF();
            // 3-8. Базовые поля.
            int health = dis.readInt();
            int maxHealth = dis.readInt();
            int attack = dis.readInt();
            int defense = dis.readInt();
            int level = dis.readInt();
            int experience = dis.readInt();

            // 9. Специфичное поле подкласса.
            // Тип определяет, что именно читать: int (rage/mana) или double (critChance).
            int rage = 0;
            int mana = 0;
            double critChance = 0.0;

            // switch-expression (Java 14+) — компактная замена switch-statement.
            // -> синтаксис: нет break, нет fall-through.
            // yield (в блоках {}) — возвращает значение из case.
            //
            // "Воин", "Маг", "Лучник" — это значения, которые возвращает
            // getClassName() в каждом подклассе (Warrior, Mage, Archer).
            // Локализованные строки используются вместо "Warrior" и т.д.,
            // чтобы сохранение было читаемым для русскоязычного пользователя.
            // ВАЖНО: если переименовать возвращаемое значение getClassName()
            // в подклассе — старые сохранения перестанут загружаться (default → return false)!
            switch (heroType) {
                case "Воин" -> rage = dis.readInt();
                case "Маг" -> mana = dis.readInt();
                case "Лучник" -> critChance = dis.readDouble();
                default -> {
                    System.out.println("[Ошибка] Неизвестный тип героя: " + heroType);
                    return false;
                }
            }

            // 10-11. Инвентарь.
            int inventoryCapacity = dis.readInt();
            int inventorySize = dis.readInt();

            // Массивы для хранения данных инвентаря.
            // String[] — массив строк (имена предметов).
            // int[] — массив целых чисел (стоимость, количество).
            String[] itemNames = new String[inventorySize];
            int[] itemValues = new int[inventorySize];
            int[] itemQuantities = new int[inventorySize];

            for (int i = 0; i < inventorySize; i++) {
                itemNames[i] = dis.readUTF();
                itemValues[i] = dis.readInt();
                itemQuantities[i] = dis.readInt();
            }

            // 12-13. Состояние игры и флаги.
            String gameStateName = dis.readUTF();
            byte statusFlags = dis.readByte();

            // valueOf(String) — статический метод enum.
            // Преобразует строку обратно в константу: "EXPLORING" → GameState.EXPLORING.
            // Если строка не соответствует ни одной константе → IllegalArgumentException.
            GameState gameState = GameState.valueOf(gameStateName);

            // Если сохранение было сделано во время боя — переводим в EXPLORING.
            // Бой нельзя восстановить (враги генерируются заново).
            if (gameState == GameState.BATTLE) {
                gameState = GameState.EXPLORING;
            }

            // 14-19. Счётчики.
            int totalDamageDealt = dis.readInt();
            int totalDamageReceived = dis.readInt();
            int enemiesDefeated = dis.readInt();
            int totalHealing = dis.readInt();
            int specialAttackCount = dis.readInt();
            int lootItemsCollected = dis.readInt();

            // 20. Достижения.
            int achievementCount = dis.readInt();
            java.util.Set<Achievement> achievements = new java.util.HashSet<>(achievementCount);
            for (int i = 0; i < achievementCount; i++) {
                // Achievement.valueOf(name) — восстановление enum из строки.
                achievements.add(Achievement.valueOf(dis.readUTF()));
            }

            // 21. Бестиарий.
            int bestiarySize = dis.readInt();
            // TreeMap<String, ...> — отсортированный словарь (по имени врага).
            java.util.TreeMap<String, Bestiary.BestiaryEntry> bestiaryEntries = new java.util.TreeMap<>();
            for (int i = 0; i < bestiarySize; i++) {
                String enemyName = dis.readUTF();
                int killCount = dis.readInt();
                int maxDamageDealt = dis.readInt();
                EnemyRank rank = EnemyRank.valueOf(dis.readUTF());
                long firstEncounter = dis.readLong();
                bestiaryEntries.put(enemyName,
                        new Bestiary.BestiaryEntry(killCount, maxDamageDealt, rank, firstEncounter));
            }

            // 22. Таблица рекордов.
            int leaderboardSize = dis.readInt();
            java.util.TreeSet<BattleRecord> leaderboard = new java.util.TreeSet<>();
            for (int i = 0; i < leaderboardSize; i++) {
                String name = dis.readUTF();
                long score = dis.readLong();
                int defeated = dis.readInt();
                long timestamp = dis.readLong();
                leaderboard.add(new BattleRecord(name, score, defeated, timestamp));
            }

            // 23. Журнал квестов.
            int questLogSize = dis.readInt();
            java.util.LinkedList<String> questLog = new java.util.LinkedList<>();
            for (int i = 0; i < questLogSize; i++) {
                questLog.add(dis.readUTF());
            }

            // ===== ВОССТАНОВЛЕНИЕ СОСТОЯНИЯ ИГРЫ =====
            //
            // game.restoreState(...) — package-private метод (будет добавлен в Batch 3).
            // Принимает ВСЕ загруженные данные и устанавливает поля Game.
            // После restoreState() вызывается reinitializeTransients() для пересоздания
            // transient-полей (scanner, battleLog, undoStack, lootTable).
            game.restoreState(
                    heroType, heroName, health, maxHealth, attack, defense, level, experience,
                    rage, mana, critChance,
                    inventoryCapacity, itemNames, itemValues, itemQuantities,
                    gameState, statusFlags,
                    totalDamageDealt, totalDamageReceived, enemiesDefeated,
                    totalHealing, specialAttackCount, lootItemsCollected,
                    achievements, bestiaryEntries, leaderboard, questLog
            );

            System.out.println("[Загрузка] Бинарная загрузка из: " + saveFile.getName());
            System.out.println("[Загрузка] Герой: " + heroName + " (" + heroType + "), уровень " + level);
            return true;

        } catch (EOFException e) {
            // EOFException — специализированное исключение: файл закончился неожиданно.
            // Наследник IOException. Означает: файл повреждён или был обрезан.
            // Пример: сохранение прервалось на середине (выключили компьютер).
            System.out.println("[Ошибка] Файл повреждён (неполные данные): " + saveFile.getName());
            return false;
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException — при valueOf() для enum.
            // Если в файле строка, не соответствующая ни одному значению enum.
            // Пример: сохранили "EXPLORING", а в новой версии переименовали в "ROAMING".
            System.out.println("[Ошибка] Некорректные данные в файле: " + e.getMessage());
            return false;
        } catch (IOException e) {
            // IOException — все остальные ошибки ввода-вывода.
            System.out.println("[Ошибка] Не удалось загрузить: " + e.getMessage());
            return false;
        }
    }

    // ===== ОБЪЕКТНОЕ СОХРАНЕНИЕ — ObjectOutputStream (глава 6.10) =====
    //
    // Сериализация — автоматическое преобразование ВСЕГО объекта в байты.
    // Один вызов writeObject(game) сохраняет:
    //   - Все НЕ-transient, НЕ-static поля Game.
    //   - Рекурсивно: все вложенные объекты (hero, inventory, bestiary, ...).
    //   - Полиморфизм сохраняется: если hero — Warrior, будет записан именно Warrior.
    //
    // Требования:
    //   - Game implements Serializable (с serialVersionUID).
    //   - ВСЕ вложенные классы implements Serializable:
    //       GameCharacter, Warrior, Mage, Archer, Inventory, Inventory.Slot,
    //       Inventory.ItemInfo, Bestiary, BestiaryEntry, BattleRecord, Achievement, GameState.
    //   - Поля, которые НЕ нужно сохранять, помечены transient:
    //       scanner, battleLog, undoStack, lootTable, listener, damageDealtThisBattle.
    //
    // Сравнение с бинарным сохранением:
    // ┌────────────────┬──────────────────────────┬──────────────────────────┐
    // │                │ DataOutputStream (binary) │ ObjectOutputStream       │
    // ├────────────────┼──────────────────────────┼──────────────────────────┤
    // │ Запись          │ Вручную каждое поле      │ Один вызов writeObject() │
    // │ Чтение          │ Вручную в том же порядке │ Один вызов readObject()  │
    // │ Размер файла    │ Компактный               │ Больше (метаданные)      │
    // │ Версионирование │ Ручное (порядок полей)   │ serialVersionUID         │
    // │ Полиморфизм     │ Ручной (switch по типу)  │ Автоматический           │
    // │ Совместимость   │ Любой язык может прочитать│ Только Java              │
    // └────────────────┴──────────────────────────┴──────────────────────────┘
    //
    // Цепочка потоков:
    //   ObjectOutputStream       → writeObject() + метаданные классов
    //     └── BufferedOutputStream → буферизация
    //           └── FileOutputStream → запись на диск
    //
    public void saveObject(Game game) {
        GameCharacter hero = game.getHero();
        String fileName = normalizeHeroName(hero.getName()) + "_object" + OBJECT_EXTENSION;
        File saveFile = new File(savesDirectory, fileName);

        // success — флаг успешной записи (аналогично saveBinary).
        // Размер файла выводим ПОСЛЕ закрытия потока, иначе буфер не сброшен и length() == 0.
        boolean success = false;

        // try-with-resources: ObjectOutputStream закроет BufferedOutputStream и FileOutputStream.
        //
        // ObjectOutputStream(OutputStream) — конструктор-декоратор.
        // При создании ObjectOutputStream записывает ЗАГОЛОВОК (magic number + версия протокола).
        // Этот заголовок проверяется при чтении ObjectInputStream.
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(
                        new FileOutputStream(saveFile)))) {

            // writeObject(Object) — главный метод сериализации.
            // Принимает ЛЮБОЙ объект (тип Object), но проверяет instanceof Serializable.
            // Если объект НЕ Serializable → NotSerializableException.
            //
            // Что происходит внутри:
            //   1. Записывается имя класса (rpg.Game).
            //   2. Записывается serialVersionUID.
            //   3. Для каждого НЕ-transient, НЕ-static поля:
            //      a. Примитивы (int, double, byte) → записываются напрямую.
            //      b. Объекты → рекурсивно вызывается writeObject().
            //   4. Для полей-коллекций (ArrayList, TreeSet, ...) — записывается
            //      размер и каждый элемент.
            //
            // Граф объектов: если два поля ссылаются на один объект, он записывается
            // ОДИН раз, а второе поле получает ссылку (handle). Нет дублирования.
            oos.writeObject(game);

            success = true;

        } catch (IOException e) {
            // NotSerializableException — подкласс IOException.
            // Возникает если какое-то поле Game (или вложенный объект) не Serializable.
            // Пример: если забыли добавить Serializable к Inventory.
            System.out.println("[Ошибка] Не удалось сериализовать игру: " + e.getMessage());
        }

        // Выводим размер ПОСЛЕ закрытия потока — буфер сброшен, File.length() корректен.
        if (success) {
            System.out.println("[Сохранение] Объектное сохранение: " + saveFile.getAbsolutePath());
            System.out.println("[Сохранение] Размер файла: " + saveFile.length() + " байт");
        }
    }

    // ===== ОБЪЕКТНАЯ ЗАГРУЗКА — ObjectInputStream (глава 6.10) =====
    //
    // Десериализация — восстановление объекта из байтов.
    // readObject() возвращает Object — нужно привести к нужному типу (Game).
    //
    // Процесс десериализации:
    //   1. Читается имя класса из потока.
    //   2. JVM ищет класс в classpath.
    //   3. Проверяется serialVersionUID (в файле) == (в классе).
    //   4. Создаётся объект БЕЗ вызова конструктора (!).
    //   5. Поля заполняются из потока.
    //   6. transient-поля остаются со значениями по умолчанию (null, 0, false).
    //
    // ВАЖНО: конструктор НЕ вызывается при десериализации!
    // Поэтому transient-поля нужно инициализировать вручную после readObject().
    // В нашем случае: game.reinitializeTransients() создаст scanner, battleLog и т.д.
    //
    // Цепочка потоков:
    //   ObjectInputStream        → readObject() + проверка метаданных
    //     └── BufferedInputStream → буферизация чтения
    //           └── FileInputStream → чтение с диска
    //
    // Возвращает Game или null при ошибке.
    //
    public Game loadObject(String filePath) {
        File saveFile = new File(savesDirectory, filePath);

        if (!saveFile.exists() || !saveFile.isFile()) {
            System.out.println("[Ошибка] Файл не найден: " + saveFile.getAbsolutePath());
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(
                        new FileInputStream(saveFile)))) {

            // readObject() — читает объект из потока.
            // Возвращает Object — самый общий тип в Java.
            // Нужно приведение типа: (Game) — downcast от Object к Game.
            //
            // Если в файле записан объект другого класса (не Game),
            // приведение вызовет ClassCastException (unchecked).
            Object obj = ois.readObject();

            // instanceof — проверяем тип ПЕРЕД приведением (безопасный подход).
            // Pattern matching instanceof (Java 16+): проверка + создание переменной.
            if (obj instanceof Game loadedGame) {
                // reinitializeTransients() — пересоздаёт transient-поля:
                //   scanner = new Scanner(System.in);
                //   battleLog = new ArrayList<>();
                //   undoStack = new ArrayDeque<>();
                //   setupLootTable();
                // Без этого вызова transient-поля будут null → NullPointerException.
                loadedGame.reinitializeTransients();

                // Проверяем gameState: если BATTLE → переводим в EXPLORING.
                if (loadedGame.getGameState() == GameState.BATTLE) {
                    loadedGame.setGameState(GameState.EXPLORING);
                }

                System.out.println("[Загрузка] Объектная загрузка из: " + saveFile.getName());
                System.out.println("[Загрузка] Герой: " + loadedGame.getHero().getName()
                        + " (" + loadedGame.getHero().getClassName() + ")");
                return loadedGame;
            } else {
                System.out.println("[Ошибка] Файл содержит объект неизвестного типа: "
                        + obj.getClass().getName());
                return null;
            }

        } catch (InvalidClassException e) {
            // InvalidClassException — serialVersionUID не совпадает.
            // Возникает, когда класс был ИЗМЕНЁН после сохранения:
            //   - Добавлены/удалены поля.
            //   - Изменён тип поля.
            //   - serialVersionUID в коде отличается от записанного в файле.
            //
            // Решение: удалить старое сохранение и создать новое.
            // Или: реализовать custom readObject()/writeObject() для миграции.
            System.out.println("[Ошибка] Версия сохранения несовместима с текущей версией игры.");
            System.out.println("  Причина: " + e.getMessage());
            System.out.println("  Решение: удалите старое сохранение и начните новую игру.");
            return null;
        } catch (ClassNotFoundException e) {
            // ClassNotFoundException — класс из файла не найден в classpath.
            // Возникает, если сохранение содержит объект класса, который был удалён.
            // Пример: сохранили Paladin, затем удалили Paladin.java.
            System.out.println("[Ошибка] Класс из сохранения не найден: " + e.getMessage());
            return null;
        } catch (IOException e) {
            // StreamCorruptedException, EOFException и другие IOException.
            System.out.println("[Ошибка] Не удалось загрузить: " + e.getMessage());
            return null;
        }
    }

    // ===== СПИСОК ФАЙЛОВ СОХРАНЕНИЙ — File.listFiles() (глава 6.11) =====
    //
    // File.listFiles() — возвращает массив File[] содержимого директории.
    // Если вызвать на файле (не директории) или директория не существует → null.
    //
    // File.listFiles(FileFilter) — с фильтром.
    // FileFilter — функциональный интерфейс: boolean accept(File file).
    // Можно передать лямбда-выражение:
    //   dir.listFiles(f -> f.getName().endsWith(".dat"))
    //
    // Возвращает массив File[] файлов сохранений (.dat и .sav), или пустой массив.
    //
    public File[] listSaves() {
        // Проверяем, что директория существует.
        if (!savesDirectory.exists() || !savesDirectory.isDirectory()) {
            // Пустой массив вместо null — безопасный возврат (Null Object Pattern).
            // Вызывающий код может сразу итерировать: for (File f : listSaves()) {...}
            // Если бы вернули null — пришлось бы проверять: if (files != null).
            return new File[0];
        }

        // ===== listFiles(FileFilter) — ФИЛЬТРАЦИЯ СОДЕРЖИМОГО ДИРЕКТОРИИ =====
        //
        // FileFilter — функциональный интерфейс с одним методом:
        //   boolean accept(File pathname) — вернуть true, если файл подходит.
        //
        // Лямбда-выражение (Java 8+) заменяет анонимный класс:
        //   Было:  dir.listFiles(new FileFilter() {
        //              public boolean accept(File f) {
        //                  return f.getName().endsWith(".dat");
        //              }
        //          });
        //   Стало: dir.listFiles(f -> f.getName().endsWith(".dat"))
        //
        // Здесь фильтруем: только файлы (не директории) с расширениями .dat или .sav.
        // String.endsWith(String) — проверяет, заканчивается ли строка на суффикс.
        // || — логическое ИЛИ (short-circuit): если первое true, второе не проверяется.
        File[] saves = savesDirectory.listFiles(f ->
                f.isFile() && (f.getName().endsWith(BINARY_EXTENSION)
                        || f.getName().endsWith(OBJECT_EXTENSION))
        );

        // listFiles() может вернуть null (если нет прав доступа).
        // Защитная проверка: возвращаем пустой массив вместо null.
        return saves != null ? saves : new File[0];
    }

    // ===== НОРМАЛИЗАЦИЯ ИМЕНИ ГЕРОЯ ДЛЯ ИМЕНИ ФАЙЛА =====
    //
    // Имя героя может содержать пробелы, спецсимволы, заглавные буквы.
    // Для имени файла нужно безопасное имя:
    //   "Артас Великий" → "артас_великий"
    //   "O'Brien" → "obrien"
    //
    // String.toLowerCase() — преобразует все символы в нижний регистр.
    //   "HELLO" → "hello", "Артас" → "артас" (работает с Unicode).
    //
    // String.replaceAll(regex, replacement) — замена по регулярному выражению.
    //   "\\s+" — один или более пробельных символов (пробел, табуляция, ...).
    //   "[^a-zA-Zа-яА-ЯёЁ0-9_]" — всё кроме букв (лат. + кир.), цифр и подчёркиваний.
    //
    // static — метод принадлежит классу, не экземпляру.
    //   Вызов: GameSaveManager.normalizeHeroName("Артас")
    //   Не нужен объект GameSaveManager — метод не зависит от состояния экземпляра.
    //
    static String normalizeHeroName(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Zа-яА-ЯёЁ0-9_]", "");
    }

    // ===== ВСПОМОГАТЕЛЬНЫЙ МЕТОД — ОТОБРАЖЕНИЕ СПИСКА СОХРАНЕНИЙ =====
    //
    // Показывает пронумерованный список файлов сохранений.
    // Используется в Game.java при выборе файла для загрузки.
    //
    // File.getName() — только имя файла без пути: "артас_binary.dat".
    // File.length()  — размер файла в байтах.
    // File.lastModified() — время последнего изменения (epoch ms).
    //
    // Возвращает массив File[] для последующего выбора по номеру.
    //
    public File[] displaySavesList() {
        File[] saves = listSaves();

        if (saves.length == 0) {
            System.out.println("[Сохранения] Нет доступных сохранений.");
            return saves;
        }

        System.out.println("\n===== Доступные сохранения =====");
        for (int i = 0; i < saves.length; i++) {
            File f = saves[i];
            // Определяем тип сохранения по расширению.
            String type = f.getName().endsWith(BINARY_EXTENSION) ? "бинарное" : "объектное";

            // java.util.Date — старый класс для работы с датами.
            // new Date(long) — создаёт дату из epoch milliseconds.
            // Используем для отображения времени последнего изменения файла.
            String lastModified = new java.util.Date(f.lastModified()).toString();

            System.out.printf("  %d. %s [%s] — %d байт, %s%n",
                    i + 1, f.getName(), type, f.length(), lastModified);
        }
        System.out.println("================================");

        return saves;
    }

    // ===== ГЕТТЕР ДИРЕКТОРИИ СОХРАНЕНИЙ =====
    //
    // Возвращает объект File директории сохранений.
    // Нужен другим классам (SaveArchiver) для работы с той же директорией.
    //
    public File getSavesDirectory() {
        return savesDirectory;
    }
}
