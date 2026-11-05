package com.passwordmanager.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Способ защиты пароля в записи менеджера паролей.
 */
public enum StorageMode {

    /** Кодирование Base64. */
    BASE64("Base64", true,
            "Обратимое кодирование. Пароль можно посмотреть, но защита минимальная."),

    /** Необратимый MD5-хеш. */
    MD5("MD5", false,
            "Необратимый хеш. Пароль нельзя посмотреть — только проверить."),

    /** Шифр Фейстеля с секретным ключом приложения. */
    FEISTEL("Шифр Фейстеля", true,
            "Шифрование секретным ключом приложения. Пароль можно посмотреть."),

    /** Необратимый SHA-256-хеш со случайной солью и секретным «перцем». */
    SALTED_HASH("Хеш с солью", false,
            "SHA-256 со случайной солью и секретным ключом. Пароль можно только проверить.");

    /** Название для интерфейса. */
    private final String displayName;

    /** Можно ли восстановить исходный пароль. */
    private final boolean reversible;

    /** Пояснение для пользователя. */
    private final String description;

    /**
     * Создаёт способ защиты.
     *
     * @param displayName название для интерфейса
     * @param reversible  признак обратимости
     * @param description пояснение для пользователя
     */
    StorageMode(String displayName, boolean reversible, String description) {
        this.displayName = displayName;
        this.reversible = reversible;
        this.description = description;
    }

    /**
     * Возвращает название для интерфейса.
     *
     * @return человекочитаемое название
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Сообщает, можно ли восстановить исходный пароль.
     *
     * @return {@code true} для обратимых способов
     */
    public boolean isReversible() {
        return reversible;
    }

    /**
     * Возвращает пояснение для пользователя.
     *
     * @return описание способа защиты
     */
    public String getDescription() {
        return description;
    }

    /**
     * Ищет способ защиты по имени константы без учёта регистра.
     *
     * @param name имя (например, {@code "FEISTEL"})
     * @return найденный способ или пустой {@link Optional}
     */
    public static Optional<StorageMode> fromName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(name))
                .findFirst();
    }

    /**
     * Возвращает название для отображения (используется выпадающим списком JavaFX).
     *
     * @return название способа защиты
     */
    @Override
    public String toString() {
        return displayName;
    }
}