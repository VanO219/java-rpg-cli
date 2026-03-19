// Пакет rpg — все классы RPG-игры (см. подробное объяснение package в GameCharacter.java).
package rpg;

// ===== НАСЛЕДОВАНИЕ (extends) =====
//
// extends GameCharacter — класс Warrior НАСЛЕДУЕТ все поля и методы GameCharacter.
// Warrior — это подкласс (дочерний класс, наследник), GameCharacter — суперкласс (родитель).
//
// Что получает Warrior от GameCharacter бесплатно (без повторного написания):
//   - все поля (name, health, attack, defense, level, experience)
//   - все обычные методы (takeDamage, isAlive, heal, addExperience, геттеры)
//   - реализацию интерфейсов (Attackable, Healable) — через GameCharacter
//   - toString(), equals(), hashCode()
//
// Что Warrior ОБЯЗАН реализовать (абстрактные методы родителя):
//   - getClassName() — название класса персонажа
//   - specialAttack() — уникальная спецатака
//
// Наследование — механизм повторного использования кода.
// Зачем? Чтобы не дублировать одинаковый код в Warrior, Mage и Archer.
// Общее поведение — в GameCharacter, уникальное — в каждом наследнике.
//
// В Java класс может наследовать (extends) только ОДИН класс,
// но реализовать (implements) несколько интерфейсов.
//
// ===== ПОЛИМОРФИЗМ В ДЕЙСТВИИ =====
//
// Warrior можно присвоить переменной типа GameCharacter:
//   GameCharacter hero = new Warrior("Артур");
//   hero.specialAttack();  // вызовет Warrior.specialAttack() — физический урон с яростью
//
// Это полиморфизм: ссылка типа GameCharacter, но вызывается метод конкретного класса Warrior.
// Java определяет нужный метод ПО ТИПУ ОБЪЕКТА (Warrior), а не по типу ссылки (GameCharacter).
//
// Warrior также можно присвоить переменным типов Attackable или Healable:
//   Attackable attacker = new Warrior("Артур");
//   attacker.performAttack();  // работает, т.к. Warrior реализует Attackable через GameCharacter
public class Warrior extends GameCharacter {

    // ===== serialVersionUID — ВЕРСИЯ КЛАССА ДЛЯ СЕРИАЛИЗАЦИИ (глава 6.10) =====
    //
    // Warrior наследует интерфейс Serializable от GameCharacter (implements Serializable).
    // В Java если родительский класс реализует интерфейс, все наследники тоже его реализуют
    // автоматически — НЕ нужно писать "implements Serializable" повторно.
    //
    // Однако serialVersionUID нужно объявить В КАЖДОМ классе иерархии отдельно:
    //   - GameCharacter имеет свой serialVersionUID = 1L (для своих полей: name, health, attack...)
    //   - Warrior имеет свой serialVersionUID = 1L (для своего поля: rage)
    //
    // Почему? При десериализации JVM проверяет serialVersionUID КАЖДОГО класса в цепочке
    // наследования. Если Warrior изменится (добавится новое поле), его serialVersionUID
    // нужно будет обновить, не затрагивая GameCharacter.
    //
    // Без явного serialVersionUID: добавление поля в Warrior сломает загрузку старых сохранений,
    // даже если GameCharacter не менялся (см. подробное объяснение в GameCharacter.java).
    private static final long serialVersionUID = 1L;

    // Собственное поле класса Warrior — ярость. Только у воина есть это свойство.
    // private — доступно только внутри Warrior (не видно в Mage или Archer).
    // Инициализация при объявлении: = 0 — начальное значение.
    // Можно инициализировать поле здесь ИЛИ в конструкторе. Если и там и там —
    // значение из конструктора «перезапишет» значение при объявлении.
    private int rage = 0;

    // ===== КОНСТРУКТОР НАСЛЕДНИКА + super() =====
    //
    // Конструктор Warrior принимает только имя: new Warrior("Артур").
    // Остальные характеристики (HP, ATK, DEF) зашиты внутри — это фиксированные
    // параметры класса «Воин» (много здоровья, высокая атака, средняя защита).

    /**
     * Создаёт воина с заданным именем.
     * Характеристики воина: HP=120, ATK=15, DEF=10 — высокие показатели ближнего боя.
     *
     * @param name имя воина
     */
    public Warrior(String name) {
        // super(...) — вызов конструктора РОДИТЕЛЬСКОГО класса (GameCharacter).
        //
        // ОБЯЗАТЕЛЬНОЕ ПРАВИЛО: super() должен быть ПЕРВОЙ строкой конструктора наследника.
        // Если конструктор родителя требует параметры — их нужно передать.
        //
        // super(name, 120, 15, 10) → вызывает GameCharacter(name, maxHealth, attack, defense).
        // GameCharacter инициализирует все общие поля (name, health, attack и т.д.).
        //
        // Если в родителе есть конструктор без параметров — Java вызовет super()
        // автоматически (неявно). Но если у родителя ТОЛЬКО конструктор с параметрами —
        // вызов super(...) ОБЯЗАТЕЛЕН, иначе ошибка компиляции.
        super(name, 120, 15, 10);
        // Поле rage уже инициализировано значением 0 (при объявлении выше).
        // Дополнительная инициализация в конструкторе не нужна.
    }

    // ===== @Override — РЕАЛИЗАЦИЯ АБСТРАКТНОГО МЕТОДА =====
    //
    // @Override (см. подробное объяснение в GameCharacter.java).
    // Здесь реализуем абстрактный метод getClassName() из GameCharacter.
    // Warrior ОБЯЗАН это сделать — иначе ошибка компиляции.

    /**
     * Возвращает название класса — "Воин".
     * Реализация абстрактного метода GameCharacter.getClassName().
     *
     * @return строка "Воин"
     */
    @Override
    public String getClassName() {
        return "Воин";
    }

    /**
     * Специальная атака воина: удар с яростью.
     * Реализация абстрактного метода GameCharacter.specialAttack().
     * <p>
     * Механика: каждая спецатака увеличивает ярость на 20.
     * Бонус к урону = rage / 10 (целочисленное деление).
     * Чем больше сражается воин, тем сильнее его удары!
     *
     * @return Physical-урон с бонусом от ярости
     */
    @Override
    public DamageType specialAttack() {
        // Накапливаем ярость с каждой спецатакой.
        rage += 20;

        // Целочисленное деление: 20/10=2, 40/10=4, 60/10=6 и т.д.
        // В Java деление int / int всегда даёт int (дробная часть отбрасывается).
        // Частая ошибка: 1 / 2 == 0 (а не 0.5!). Для дробного результата
        // хотя бы один операнд должен быть double: 1.0 / 2 == 0.5.
        int bonus = rage / 10;

        // getAttack() — вызов геттера из родительского класса GameCharacter.
        // Поле attack — private в GameCharacter, поэтому Warrior не может
        // обратиться к нему напрямую (this.attack — ошибка!).
        // Геттер — единственный способ получить значение private-поля родителя.
        //
        // new DamageType.Physical(...) — создаём record Physical (см. DamageType.java).
        // Воин наносит ЧИСТО ФИЗИЧЕСКИЙ урон (мечом, кулаком). Поэтому Physical, а не Magical.
        // DamageType — sealed interface с тремя вариантами: Physical, Magical, Mixed.
        // Каждый класс персонажа возвращает тот подтип DamageType, который соответствует его стилю боя.
        return new DamageType.Physical(getAttack() + bonus);
    }

    // ===== @Override default-МЕТОДА ИНТЕРФЕЙСА =====
    //
    // getBattleCry() — default-метод из интерфейса Attackable.
    // Default-метод имеет реализацию по умолчанию ("В атаку!"),
    // но любой класс может ПЕРЕОПРЕДЕЛИТЬ его своей версией.
    //
    // Warrior переопределяет default-метод, чтобы у воина был свой уникальный клич.
    // Если бы мы НЕ написали этот метод — Warrior унаследовал бы "В атаку!" из Attackable.

    /**
     * Боевой клич воина.
     * Переопределяет default-метод Attackable.getBattleCry().
     *
     * @return "За честь и славу!"
     */
    @Override
    public String getBattleCry() {
        return "За честь и славу!";
    }

    /**
     * Возвращает текущий уровень ярости воина.
     * Геттер для собственного поля Warrior (см. объяснение геттеров в GameCharacter.java).
     *
     * @return текущая ярость
     */
    public int getRage() {
        return rage;
    }

    // ===== СЕТТЕР ДЛЯ БИНАРНОЙ ДЕСЕРИАЛИЗАЦИИ (глава 6.7) =====
    //
    // При бинарной загрузке (DataInputStream) объект создаётся через конструктор
    // new Warrior(name), а затем поля восстанавливаются по одному.
    // В отличие от ObjectInputStream (который записывает/читает объект целиком),
    // бинарный формат требует «ручной» установки каждого поля.
    //
    // Package-private видимость (без модификатора): доступен только классам
    // из того же пакета rpg (например, Game.restoreState).
    // Это безопаснее, чем public — внешний код не сможет менять ярость напрямую.
    void setRage(int rage) {
        this.rage = rage;
    }
}
