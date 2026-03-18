// ===== ЗАЧЕМ ЭТОТ ФАЙЛ? =====
//
// BattleEventListener — интерфейс для «слушателей» боевых событий.
// Боевая система (Game) генерирует события: атака, победа, повышение уровня.
// Кто угодно может «подписаться» и реагировать на эти события,
// не меняя код Game — например, вести лог, обновлять UI, считать статистику.
//
// Без этого интерфейса: Game был бы перегружен логикой вывода и счётчиками.
// С интерфейсом: Game знает только КАК сражаться, а слушатель — что делать с событиями.
//
// Пакет rpg — все классы нашей RPG-игры (см. подробное объяснение в DamageType.java).
package rpg;

// ===== ИНТЕРФЕЙС КАК МЕХАНИЗМ ОБРАТНОГО ВЫЗОВА (CALLBACK) =====
//
// Что такое callback (обратный вызов)?
// Это способ сказать: «когда произойдёт событие X — вызови мой метод Y».
//
// Пример из жизни: ты оставляешь номер телефона в ресторане (callback),
// и тебе ПЕРЕЗВАНИВАЮТ, когда столик освободится.
// Ты не стоишь и не ждёшь — тебя УВЕДОМЯТ.
//
// В программировании: объект A регистрирует слушателя (listener) у объекта B.
// Когда в B происходит событие — B вызывает метод слушателя.
//
// В нашей игре:
//   1. Определяем интерфейс BattleEventListener с методами-обработчиками событий.
//   2. Создаём класс, реализующий этот интерфейс (например, BattleLogger).
//   3. Регистрируем слушателя в боевой системе: battle.setListener(new BattleLogger()).
//   4. Боевая система ВЫЗЫВАЕТ методы слушателя, когда происходят события:
//        listener.onAttack(hero, damage, 25);        // Атака нанесла 25 урона
//        listener.onEnemyDefeated(hero, "Гоблин", 50); // Враг побеждён, +50 XP
//        listener.onLevelUp(hero, 5);                 // Герой достиг 5-го уровня
//
// ===== ПАТТЕРН НАБЛЮДАТЕЛЬ (Observer Pattern) =====
//
// Этот интерфейс реализует паттерн «Наблюдатель» (Observer / Listener):
//   - СУБЪЕКТ (Subject) — боевая система. Она генерирует события.
//   - НАБЛЮДАТЕЛЬ (Observer) — класс, реализующий BattleEventListener.
//     Он получает уведомления о событиях и реагирует на них.
//
// Зачем? Разделение ответственности (Separation of Concerns):
//   - Боевая система знает КАК сражаться, но НЕ знает, что делать с событиями.
//   - Слушатель знает, что делать с событиями (логировать, обновлять UI, считать статистику),
//     но НЕ знает деталей боевой механики.
//
// Этот паттерн используется ПОВСЕМЕСТНО:
//   - GUI: MouseListener, KeyListener, ActionListener (Java Swing / JavaFX)
//   - Spring: ApplicationListener, @EventListener
//   - Android: OnClickListener, OnScrollListener
//   - JavaScript: addEventListener("click", handler)
//   - В C это достигается через указатели на функции (function pointers).
//   - В C# — через делегаты (delegates) и события (events).
//
// ===== ПОЧЕМУ НЕ @FunctionalInterface? =====
//
// @FunctionalInterface — аннотация для интерфейсов с ОДНИМ абстрактным методом.
// Такие интерфейсы можно реализовать через лямбда-выражение (стрелочную функцию):
//   Runnable r = () -> System.out.println("hello");  // Runnable — @FunctionalInterface
//
// BattleEventListener имеет НЕСКОЛЬКО абстрактных методов (onAttack, onEnemyDefeated, onLevelUp),
// поэтому он НЕ может быть @FunctionalInterface и НЕ может быть реализован лямбдой.
// Для реализации нужен полноценный класс или анонимный класс.
//
// Пример реализации через анонимный класс:
//   BattleEventListener logger = new BattleEventListener() {
//       @Override
//       public void onAttack(GameCharacter attacker, DamageType damage, int actualDamage) {
//           System.out.println(attacker.getName() + " наносит " + actualDamage + " урона!");
//       }
//       @Override
//       public void onEnemyDefeated(GameCharacter hero, String enemyName, int expReward) {
//           System.out.println(enemyName + " побеждён! +" + expReward + " XP");
//       }
//       @Override
//       public void onLevelUp(GameCharacter hero, int newLevel) {
//           System.out.println(hero.getName() + " достиг уровня " + newLevel + "!");
//       }
//   };
//
// Или через отдельный класс:
//   public class BattleLogger implements BattleEventListener {
//       @Override public void onAttack(...) { ... }
//       @Override public void onEnemyDefeated(...) { ... }
//       @Override public void onLevelUp(...) { ... }
//   }
public interface BattleEventListener {

    // ===== АБСТРАКТНЫЕ МЕТОДЫ — ОБЯЗАТЕЛЬНЫЕ ДЛЯ РЕАЛИЗАЦИИ =====
    //
    // Абстрактный метод в интерфейсе — метод без тела (см. подробное объяснение в Attackable.java).
    // Все методы интерфейса без ключевого слова default или static — неявно abstract и public.
    // Класс, реализующий интерфейс, ОБЯЗАН написать тело для КАЖДОГО абстрактного метода.

    /**
     * Вызывается при каждой атаке в бою.
     * Боевая система вызывает этот метод, когда персонаж наносит урон.
     *
     * @param attacker    атакующий персонаж (герой или враг)
     * @param damage      тип нанесённого урона (Physical, Magical или Mixed)
     * @param actualDamage фактический урон после вычета защиты
     */
    void onAttack(GameCharacter attacker, DamageType damage, int actualDamage);

    /**
     * Вызывается при победе над врагом.
     * Боевая система вызывает этот метод, когда здоровье врага достигает нуля.
     *
     * @param hero      герой, победивший врага
     * @param enemyName имя побеждённого врага
     * @param expReward количество полученного опыта
     */
    void onEnemyDefeated(GameCharacter hero, String enemyName, int expReward);

    /**
     * Вызывается при повышении уровня героя.
     * Боевая система вызывает этот метод, когда герой набирает достаточно опыта.
     *
     * @param hero     герой, получивший новый уровень
     * @param newLevel номер нового уровня
     */
    void onLevelUp(GameCharacter hero, int newLevel);

    // ===== DEFAULT-МЕТОДЫ — НЕОБЯЗАТЕЛЬНЫЕ ДЛЯ РЕАЛИЗАЦИИ =====
    //
    // default-метод — метод С реализацией прямо в интерфейсе (Java 8+).
    // (Подробное объяснение default-методов — см. Attackable.java.)
    //
    // Зачем default-методы в интерфейсе-слушателе?
    //
    // Представим, что BattleEventListener имеет 10 методов, и все абстрактные.
    // Класс, реализующий интерфейс, ОБЯЗАН написать ВСЕ 10 методов,
    // даже если ему нужен только один (например, только onEnemyDefeated).
    // Это неудобно и засоряет код пустыми реализациями.
    //
    // С default-методами: обязательны только ключевые методы (onAttack, onEnemyDefeated, onLevelUp),
    // а вспомогательные (onBattleStart, onBattleEnd) имеют реализацию по умолчанию (ничего не делают).
    // Класс переопределяет их только если ему это нужно.
    //
    // Этот подход широко используется в Java:
    //   - MouseListener vs MouseAdapter — раньше создавали «адаптеры» (пустые реализации).
    //   - Теперь default-методы заменяют адаптеры — проще и чище.

    /**
     * Вызывается при начале боя.
     * Default-реализация — ничего не делает. Переопределите, если нужна логика при старте боя.
     *
     * Пример переопределения:
     *   @Override
     *   public void onBattleStart(GameCharacter hero, String enemyName) {
     *       System.out.println(hero.getName() + " вступает в бой с " + enemyName + "!");
     *   }
     *
     * @param hero      герой, вступающий в бой
     * @param enemyName имя врага
     */
    default void onBattleStart(GameCharacter hero, String enemyName) {
        // Пустая реализация по умолчанию.
        // Классы, которым не нужно реагировать на начало боя, не переопределяют этот метод.
        // Классы, которым нужно — переопределяют с @Override.
    }

    /**
     * Вызывается при завершении боя (победа или поражение).
     * Default-реализация — ничего не делает. Переопределите, если нужна логика при конце боя.
     *
     * @param hero    герой, участвовавший в бою
     * @param victory true, если герой победил; false, если проиграл
     */
    default void onBattleEnd(GameCharacter hero, boolean victory) {
        // Пустая реализация по умолчанию (см. объяснение выше).
    }
}
