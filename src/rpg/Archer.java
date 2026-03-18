// Пакет rpg (см. подробное объяснение package в GameCharacter.java).
package rpg;

// Наследование: Archer extends GameCharacter (см. подробное объяснение extends в Warrior.java).
// Лучник — дальнобойный персонаж с шансом критического удара.
//
// Archer — третий конкретный наследник GameCharacter (наряду с Warrior и Mage).
// Обязан реализовать абстрактные методы: getClassName() и specialAttack().
//
// Ресурс Лучника — шанс крита (critChance, тип double).
// В отличие от Warrior (rage растёт) и Mage (mana тратится),
// critChance — вероятностная механика: каждый выстрел МОЖЕТ быть критическим.
// Это показывает, что наследники одного абстрактного класса могут иметь
// совершенно РАЗНЫЕ внутренние механики, объединённые общим интерфейсом (specialAttack).
public class Archer extends GameCharacter {

    // ===== double — ТИП ДЛЯ ДРОБНЫХ ЧИСЕЛ =====
    //
    // double — примитивный тип для чисел с плавающей точкой (64 бита).
    // Хранит дробные числа: 0.3, 3.14, -2.5 и т.д.
    // Используется, когда нужна дробная точность (проценты, вероятности, координаты).
    //
    // ВАЖНО: double НЕ является точным! Из-за двоичного представления:
    //   0.1 + 0.2 != 0.3 (результат: 0.30000000000000004)
    // Для точных денежных расчётов используют BigDecimal, но для игровой механики double достаточен.
    //
    // Другие дробные типы:
    //   float  — 32 бита, менее точный (редко используется)
    //   double — 64 бита, стандартный выбор для дробных чисел

    // Шанс критического удара: 0.3 = 30%.
    // Литерал 0.3 — это число типа double (в Java дробные литералы по умолчанию double).
    // Для float нужно добавить суффикс f: 0.3f.
    private double critChance = 0.3;

    /**
     * Создаёт лучника с заданным именем.
     * Характеристики лучника: HP=90, ATK=12, DEF=7 — средние показатели,
     * но компенсируются шансом критического удара (двойной урон).
     *
     * @param name имя лучника
     */
    public Archer(String name) {
        // super() — вызов конструктора родителя GameCharacter
        // (см. подробное объяснение super в Warrior.java).
        super(name, 90, 12, 7);
    }

    /**
     * Возвращает название класса — "Лучник".
     * Реализация абстрактного метода GameCharacter.getClassName().
     *
     * @return строка "Лучник"
     */
    @Override
    public String getClassName() {
        return "Лучник";
    }

    /**
     * Специальная атака лучника: выстрел с шансом критического удара.
     * Реализация абстрактного метода GameCharacter.specialAttack().
     *
     * Механика:
     *   - С шансом 30% — критический удар (двойной урон через Attackable.calculateCritDamage).
     *   - Иначе — обычный урон.
     *   - Урон смешанный (Mixed): физический + 30% магического.
     *
     * @return Mixed-урон (физический + магический компонент)
     */
    @Override
    public DamageType specialAttack() {
        // ===== Math.random() — ГЕНЕРАЦИЯ СЛУЧАЙНЫХ ЧИСЕЛ =====
        //
        // Math.random() — статический метод, возвращает случайное число типа double
        // в диапазоне [0.0, 1.0) — от 0.0 (включительно) до 1.0 (не включая).
        //
        // Math.random() < critChance:
        //   - critChance = 0.3 (30%)
        //   - Если случайное число < 0.3 → true (выпал крит, шанс ~30%)
        //   - Если случайное число >= 0.3 → false (обычный удар, шанс ~70%)
        //
        // Результат сравнения (< или >=) — boolean (true/false).
        boolean isCrit = Math.random() < critChance;

        // ===== ТЕРНАРНЫЙ ОПЕРАТОР (condition ? valueIfTrue : valueIfFalse) =====
        //
        // Тернарный оператор — краткая форма if-else, которая ВОЗВРАЩАЕТ значение.
        // Синтаксис: условие ? значение_если_true : значение_если_false
        //
        // isCrit ? Attackable.calculateCritDamage(getAttack()) : getAttack()
        // читается так:
        //   ЕСЛИ isCrit == true → вызвать Attackable.calculateCritDamage(getAttack())
        //   ИНАЧЕ              → вернуть getAttack() (обычный урон)
        //
        // Эквивалент с if-else (но тернарный компактнее):
        //   int damage;
        //   if (isCrit) {
        //       damage = Attackable.calculateCritDamage(getAttack());
        //   } else {
        //       damage = getAttack();
        //   }
        //
        // ===== ВЫЗОВ СТАТИЧЕСКОГО МЕТОДА ИНТЕРФЕЙСА =====
        //
        // Attackable.calculateCritDamage(getAttack()) — вызов static-метода интерфейса.
        // static-метод интерфейса вызывается через имя ИНТЕРФЕЙСА (не через объект).
        // Attackable.calculateCritDamage(12) → 12 * 2 = 24 (двойной урон).
        //
        // Статические методы в интерфейсах (Java 8+) — утилитные функции,
        // связанные с интерфейсом по смыслу, но не привязанные к конкретному объекту.
        int damage = isCrit ? Attackable.calculateCritDamage(getAttack()) : getAttack();

        // ===== ПРИВЕДЕНИЕ ТИПОВ: (int)(double_value) =====
        //
        // damage * 0.3 — умножение int на double даёт double (Java автоматически
        // «расширяет» int до double перед вычислением. Это называется promotion).
        //
        // (int)(damage * 0.3) — явное приведение типа (casting): double → int.
        // Дробная часть ОТБРАСЫВАЕТСЯ (не округляется!):
        //   (int)(12 * 0.3) = (int)(3.6) = 3
        //   (int)(24 * 0.3) = (int)(7.2) = 7
        //
        // Явное приведение нужно, потому что преобразование double → int
        // может потерять данные (дробную часть). Java не делает это автоматически
        // (в отличие от int → double, которое безопасно и происходит само).
        //
        // new DamageType.Mixed(physical, magical) — record Mixed (см. DamageType.java).
        // Лучник наносит СМЕШАННЫЙ урон (зачарованные стрелы) — и физический, и магический.
        // Поэтому используется Mixed, а не Physical (как у Warrior) или Magical (как у Mage).
        //
        // Три класса персонажей — три разных подтипа DamageType:
        //   Warrior → Physical  (один параметр: amount)
        //   Mage    → Magical   (два параметра: amount, element)
        //   Archer  → Mixed     (два параметра: physical, magical)
        // Sealed interface DamageType гарантирует: ДРУГИХ подтипов быть не может.
        // Код, обрабатывающий DamageType в switch, обработает все три варианта без default.
        return new DamageType.Mixed(damage, (int) (damage * 0.3));
    }

    /**
     * Боевой клич лучника.
     * Переопределяет default-метод Attackable.getBattleCry()
     * (см. подробное объяснение переопределения default-метода в Warrior.java).
     *
     * @return "Стрела найдёт цель!"
     */
    @Override
    public String getBattleCry() {
        return "Стрела найдёт цель!";
    }

    /**
     * Возвращает шанс критического удара.
     * Геттер для private-поля critChance (тип double).
     *
     * @return шанс крита (0.3 = 30%)
     */
    public double getCritChance() {
        return critChance;
    }
}
