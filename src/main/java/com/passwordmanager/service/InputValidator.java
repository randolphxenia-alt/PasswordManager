package com.passwordmanager.service;

import com.passwordmanager.model.EntryData;

import java.util.regex.Pattern;

public final class InputValidator {

    public static final int USERNAME_MIN = 3;

    public static final int USERNAME_MAX = 32;

    public static final int MASTER_PASSWORD_MIN = 6;

    public static final int FIELD_MAX = 200;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");

    private InputValidator() {
    }

    public static void validateRegistration(String username, String password, String confirmation)
            throws AppException {
        validateUsername(username);
        requireLength(password, "Мастер-пароль", MASTER_PASSWORD_MIN, FIELD_MAX);
        if (!password.equals(confirmation)) {
            throw new AppException("Пароли не совпадают");
        }
    }

    public static void validateEntry(EntryData data, boolean passwordRequired) throws AppException {
        requireLength(data.title(), "Название", 1, FIELD_MAX);
        requireLength(data.website(), "Сайт", 0, FIELD_MAX);
        requireLength(data.login(), "Логин", 1, FIELD_MAX);
        requireLength(data.password(), "Пароль", passwordRequired ? 1 : 0, FIELD_MAX);
        if (data.storageMode() == null) {
            throw new AppException("Выберите способ защиты пароля");
        }
    }

    public static String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    private static void validateUsername(String username) throws AppException {
        requireLength(username, "Имя пользователя", USERNAME_MIN, USERNAME_MAX);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new AppException("Имя пользователя: только латиница, цифры и символы . _ -");
        }
    }

    private static void requireLength(String value, String fieldName, int min, int max)
            throws AppException {
        int length = value == null ? 0 : value.length();
        if (min > 0 && length == 0) {
            throw new AppException("Заполните поле «" + fieldName + "»");
        }
        if (length < min || length > max) {
            throw new AppException("«" + fieldName + "»: от " + min + " до " + max + " символов");
        }
    }
}