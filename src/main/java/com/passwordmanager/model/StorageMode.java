package com.passwordmanager.model;

import java.util.Arrays;
import java.util.Optional;

public enum StorageMode {

    BASE64("Base64", true,
            "Обратимое кодирование. Пароль можно посмотреть, но защита минимальная."),

    MD5("MD5", false,
            "Необратимый хеш. Пароль нельзя посмотреть — только проверить."),

    FEISTEL("Шифр Фейстеля", true,
            "Шифрование секретным ключом приложения. Пароль можно посмотреть."),

    SALTED_HASH("Хеш с солью", false,
            "SHA-256 со случайной солью и секретным ключом. Пароль можно только проверить.");

    private final String displayName;

    private final boolean reversible;

    private final String description;

    StorageMode(String displayName, boolean reversible, String description) {
        this.displayName = displayName;
        this.reversible = reversible;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isReversible() {
        return reversible;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<StorageMode> fromName(String name) {
        return Arrays.stream(values())
                .filter(mode -> mode.name().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public String toString() {
        return displayName;
    }
}