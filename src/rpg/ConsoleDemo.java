// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в DamageType.java).
package rpg;

// ===== ИМПОРТ КЛАССА Console (глава 6.13) =====
//
// java.io.Console — класс для взаимодействия с СИСТЕМНОЙ КОНСОЛЬЮ.
//
// ===== ИЕРАРХИЯ Console В СИСТЕМЕ I/O =====
//
// Console НЕ является потоком (InputStream/OutputStream)!
// Это ВЫСОКОУРОВНЕВЫЙ класс, который внутри использует потоки,
// но предоставляет УДОБНЫЙ API для работы с консолью.
//
// Console vs Scanner:
//   ┌──────────────┬─────────────────────────┬────────────────────────┐
//   │              │ Console                 │ Scanner                │
//   ├──────────────┼─────────────────────────┼────────────────────────┤
//   │ Получение    │ System.console()        │ new Scanner(System.in) │
//   │ Доступность  │ Только в терминале      │ Везде (терминал, IDE)  │
//   │ Чтение строк │ readLine()              │ nextLine()             │
//   │ Чтение пароля│ readPassword() ✓        │ НЕТ (виден при вводе!) │
//   │ Форматиро-   │ printf() / format()     │ НЕТ (только чтение)   │
//   │ вание        │                         │                        │
//   │ null-риск    │ System.console()==null   │ Всегда создаётся       │
//   │ Кодировка    │ charset() (Java 17+)    │ useDelimiter() и т.д.  │
//   └──────────────┴─────────────────────────┴────────────────────────┘
//
// ГЛАВНОЕ ОТЛИЧИЕ: Console.readPassword() СКРЫВАЕТ вводимые символы.
// Scanner НЕ может этого — пароль будет виден на экране.
//
// ===== ПОЧЕМУ System.console() МОЖЕТ ВЕРНУТЬ null? =====
//
// System.console() возвращает null, когда стандартный ввод-вывод
// ПЕРЕНАПРАВЛЕН (redirected) или запуск происходит НЕ из терминала:
//
//   1. Запуск в IDE (IntelliJ, Eclipse, VS Code):
//      IDE перехватывает stdin/stdout через свои потоки → Console недоступна.
//
//   2. Перенаправление потоков:
//      java MyApp < input.txt       → stdin перенаправлен из файла
//      java MyApp > output.txt      → stdout перенаправлен в файл
//      java MyApp | grep "pattern"  → stdout перенаправлен в pipe
//
//   3. Запуск как daemon/service:
//      Фоновые процессы не имеют привязанного терминала.
//
// Поэтому ВСЕГДА нужно проверять: if (console != null) { ... }
// и предусматривать fallback-вариант (обычно Scanner).
import java.io.Console;

// Scanner — класс для чтения ввода из различных источников (глава 2.9).
// Используется как fallback, если Console недоступна.
import java.util.Scanner;

// Arrays — утилитный класс для работы с массивами.
// Arrays.equals(char[], char[]) — безопасное сравнение массивов символов.
import java.util.Arrays;

// ===== CONSOLEDEMO — ДЕМОНСТРАЦИЯ КЛАССА Console (глава 6.13) =====
//
// Этот класс демонстрирует работу с системной консолью: чтение строк,
// чтение паролей (с сокрытием ввода) и форматированный вывод.
//
// Игровой сценарий: «Секретное чит-меню».
// Игрок вводит пароль, который не отображается на экране.
// Если пароль верный — показывается бонусное сообщение.
// Это демонстрация readPassword() в игровом контексте (не реальная безопасность!).
//
// ===== КОГДА ИСПОЛЬЗОВАТЬ Console, А КОГДА Scanner? =====
//
// Используй Console, когда:
//   - Нужно читать пароли или секретные данные (readPassword скрывает ввод).
//   - Нужен форматированный вывод через console.printf().
//   - Программа ВСЕГДА запускается из терминала (CLI-утилита).
//
// Используй Scanner, когда:
//   - Программа может запускаться из IDE (учебный проект!).
//   - Нужно читать числа, токены, регулярные выражения (Scanner умеет, Console нет).
//   - Источник ввода — не только консоль (файл, строка, поток).
//
// Используй ОБА (как в этом классе), когда:
//   - Предпочтителен Console (для readPassword), но нужен fallback для IDE.
public class ConsoleDemo {

    // ===== СЕКРЕТНЫЙ ПАРОЛЬ — МАССИВ char[], А НЕ String =====
    //
    // Пароль хранится как char[], а не как String. Почему?
    //
    // String в Java — НЕИЗМЕНЯЕМЫЙ объект (immutable), живущий в пуле строк.
    // После использования String нельзя «стереть» из памяти:
    //   - String password = "dragon"; ← строка в пуле строк, удалится только при GC
    //   - Даже после password = null; исходная строка "dragon" остаётся в памяти!
    //   - Это НЕБЕЗОПАСНО: дамп памяти покажет пароль.
    //
    // char[] — ИЗМЕНЯЕМЫЙ массив. После использования можно обнулить:
    //   Arrays.fill(password, '\0'); ← перезаписать нулями.
    //   Теперь в памяти вместо пароля — нули. Безопаснее!
    //
    // Console.readPassword() возвращает char[] именно по этой причине.
    //
    // ВАЖНО: в нашей игре это НЕ реальная безопасность, а учебная демонстрация.
    // Мы храним «пароль» в исходном коде (что в реальном проекте НЕДОПУСТИМО).
    private static final char[] SECRET_CODE = {'d', 'r', 'a', 'g', 'o', 'n'};

    // ===== runDemo() — ГЛАВНЫЙ МЕТОД ДЕМОНСТРАЦИИ CONSOLE =====
    //
    // Пытается использовать System.console(). Если недоступен — переключается на Scanner.
    //
    // Параметр fallbackScanner — Scanner, который уже создан в Game.java.
    // Мы НЕ создаём новый Scanner(System.in), потому что:
    //   - Два Scanner на одном потоке (System.in) конфликтуют: один «съедает» данные другого.
    //   - Game.java уже имеет Scanner — используем его как fallback.
    public static void runDemo(Scanner fallbackScanner) {
        System.out.println("\n===== СЕКРЕТНОЕ ЧИТ-МЕНЮ (Console demo, глава 6.13) =====");

        // ===== System.console() — ПОЛУЧЕНИЕ ОБЪЕКТА Console =====
        //
        // System.console() — статический метод класса System.
        // Возвращает единственный экземпляр Console (singleton), или null.
        //
        // Это НЕ конструктор! Нельзя написать new Console() — конструктор приватный.
        // Console создаётся JVM при запуске, если терминал доступен.
        //
        // ТИПИЧНАЯ ОШИБКА: не проверить на null → NullPointerException.
        //   System.console().readLine()  ← ОПАСНО! Может быть null!
        //   Всегда: Console c = System.console(); if (c != null) { ... }
        Console console = System.console();

        String playerName;
        char[] passwordInput;

        if (console != null) {
            // ===== ВЕТКА 1: Console ДОСТУПНА (запуск из терминала) =====
            //
            // Console предоставляет три основных метода:
            //   readLine(String fmt, Object... args)   — прочитать строку с приглашением.
            //   readPassword(String fmt, Object... args) — прочитать пароль (без эха).
            //   printf(String fmt, Object... args)      — форматированный вывод.
            //
            // Все три метода поддерживают формат printf (как String.format):
            //   %s — строка, %d — целое число, %f — дробное число, %n — перенос строки.

            // ===== console.printf() — ФОРМАТИРОВАННЫЙ ВЫВОД В КОНСОЛЬ =====
            //
            // console.printf() работает аналогично System.out.printf(),
            // но пишет НАПРЯМУЮ в консольный поток (не через System.out).
            //
            // Разница с System.out.printf():
            //   - console.printf() гарантирует вывод в терминал (не перенаправляется).
            //   - System.out может быть перенаправлен в файл → вывод уйдёт туда.
            //   - На практике разница редко заметна, но для полноты демонстрируем оба.
            //
            // %n — платформо-зависимый перенос строки (\n на Linux/Mac, \r\n на Windows).
            console.printf("%n[Console] Консоль доступна! Используем Console API.%n");
            console.printf("[Console] Console.charset() = %s%n", console.charset());

            // ===== console.readLine() — ЧТЕНИЕ СТРОКИ С ПРИГЛАШЕНИЕМ =====
            //
            // readLine(String fmt, Object... args):
            //   - Выводит отформатированное приглашение (prompt).
            //   - Ждёт, пока пользователь введёт строку и нажмёт Enter.
            //   - Возвращает введённую строку БЕЗ символа перевода строки.
            //   - Возвращает null при EOF (Ctrl+D на Linux/Mac, Ctrl+Z на Windows).
            //
            // Отличие от Scanner.nextLine():
            //   - Scanner.nextLine() не умеет выводить приглашение (нужен отдельный print).
            //   - Console.readLine() совмещает приглашение и чтение в одном вызове.
            playerName = console.readLine("[Console] Введи своё имя, герой: ");

            // ===== console.readPassword() — ЧТЕНИЕ ПАРОЛЯ С СОКРЫТИЕМ ВВОДА =====
            //
            // readPassword(String fmt, Object... args):
            //   - Выводит приглашение (как readLine).
            //   - ОТКЛЮЧАЕТ ЭХО: вводимые символы НЕ отображаются на экране!
            //   - Возвращает char[] (НЕ String) — для безопасности (см. комментарий к SECRET_CODE).
            //   - Возвращает null при EOF.
            //
            // КАК ЭТО РАБОТАЕТ ТЕХНИЧЕСКИ:
            //   Console обращается к ОС с запросом отключить echo-режим терминала.
            //   В обычном режиме: ввод "abc" → на экране появляется "abc".
            //   С отключённым echo: ввод "abc" → на экране ничего не появляется.
            //   После readPassword() echo-режим включается обратно автоматически.
            //
            // ВАЖНО: readPassword() нельзя вызвать из IDE — Console == null.
            // Поэтому во второй ветке (fallback) мы используем Scanner.nextLine(),
            // и пароль будет ВИДЕН на экране (это ограничение, а не баг).
            console.printf("[Console] Для доступа к чит-меню введи секретный код.%n");
            console.printf("[Console] Подсказка: имя легендарного огнедышащего существа.%n");
            passwordInput = console.readPassword("[Console] Секретный код: ");

        } else {
            // ===== ВЕТКА 2: Console НЕДОСТУПНА (запуск из IDE) =====
            //
            // Типичная ситуация для учебных проектов: IntelliJ IDEA, Eclipse, VS Code
            // запускают Java-программу через свой встроенный терминал, который
            // перенаправляет stdin/stdout → System.console() возвращает null.
            //
            // Решение: использовать Scanner как fallback.
            // Ограничения:
            //   - Нет readPassword() → пароль виден при вводе.
            //   - Нет console.printf() → используем System.out.printf().
            System.out.println("[Console] System.console() вернул null — консоль недоступна.");
            System.out.println("[Console] Вероятно, вы запускаете программу из IDE.");
            System.out.println("[Console] Переключаемся на Scanner (пароль будет виден при вводе).");
            System.out.println();

            System.out.print("[Scanner] Введи своё имя, герой: ");
            playerName = fallbackScanner.nextLine().trim();

            System.out.println("[Scanner] Для доступа к чит-меню введи секретный код.");
            System.out.println("[Scanner] Подсказка: имя легендарного огнедышащего существа.");
            System.out.print("[Scanner] Секретный код (виден при вводе!): ");
            String passwordStr = fallbackScanner.nextLine().trim();

            // Преобразуем String в char[] для единого кода проверки ниже.
            // String.toCharArray() создаёт НОВЫЙ массив — копию символов строки.
            passwordInput = passwordStr.toCharArray();
        }

        // ===== ПРОВЕРКА ПАРОЛЯ =====
        //
        // passwordInput может быть null (если пользователь нажал Ctrl+D / EOF).
        if (playerName == null || playerName.isEmpty()) {
            playerName = "Незнакомец";
        }

        if (passwordInput == null) {
            System.out.println("[Чит-меню] Ввод отменён.");
            return;
        }

        // ===== Arrays.equals(char[], char[]) — СРАВНЕНИЕ МАССИВОВ =====
        //
        // Оператор == для массивов сравнивает ССЫЛКИ, а не содержимое!
        //   passwordInput == SECRET_CODE → false (разные объекты в памяти)
        //
        // Arrays.equals() сравнивает поэлементно:
        //   {'d','r','a','g','o','n'} equals {'d','r','a','g','o','n'} → true
        //
        // АНАЛОГИЯ: == проверяет «это один и тот же конверт?»,
        // Arrays.equals() проверяет «содержимое конвертов одинаковое?».
        if (Arrays.equals(passwordInput, SECRET_CODE)) {
            System.out.println();
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║   *** ЧИТ-МЕНЮ АКТИВИРОВАНО! ***       ║");
            System.out.println("║                                          ║");
            System.out.printf("║   Приветствую, %s!%n", playerName);
            System.out.println("║                                          ║");
            System.out.println("║   Секретные коды игры:                   ║");
            System.out.println("║   • IDDQD  — бессмертие (не работает)    ║");
            System.out.println("║   • IDKFA  — все оружие (тоже нет)       ║");
            System.out.println("║   • NOCLIP — проход сквозь стены (увы)   ║");
            System.out.println("║                                          ║");
            System.out.println("║   На самом деле читов нет. Но Console    ║");
            System.out.println("║   работает! readPassword() скрыл ввод.   ║");
            System.out.println("╚══════════════════════════════════════════╝");
        } else {
            System.out.println();
            System.out.println("[Чит-меню] Неверный код. Доступ запрещён!");
            System.out.println("[Чит-меню] Подсказка: попробуй 'dragon'.");
        }

        // ===== ОЧИСТКА ПАРОЛЯ ИЗ ПАМЯТИ =====
        //
        // Arrays.fill(array, value) — заполняет ВЕСЬ массив указанным значением.
        // Здесь перезаписываем символы пароля нулями ('\0'):
        //   {'d','r','a','g','o','n'} → {'\0','\0','\0','\0','\0','\0'}
        //
        // ЗАЧЕМ? Чтобы пароль не «висел» в памяти после использования.
        // Если кто-то сделает дамп памяти Java-процесса (heap dump),
        // он не увидит пароль — только нули.
        //
        // Это стандартная практика безопасности в Java:
        //   1. Получить пароль как char[] (Console.readPassword()).
        //   2. Проверить пароль.
        //   3. Немедленно обнулить массив: Arrays.fill(password, '\0').
        //
        // ВАЖНО: строка из Scanner (String) НЕ может быть обнулена —
        // String неизменяем (immutable). Ещё одна причина предпочитать Console.
        Arrays.fill(passwordInput, '\0');
    }
}
