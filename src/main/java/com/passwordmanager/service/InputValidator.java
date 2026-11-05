package com.passwordmanager.service;

import com.passwordmanager.model.EntryData;

import java.util.regex.Pattern;

/**
 * Проверка данных, введённых пользователем.
 * При ошибке выбрасывается {@link AppException} с понятным сообщением.
 */
public final class InputValidator {

    /** Минимальная длина имени пользователя. */
    public static final int USERNAME_MIN = 3;

    /** Максимальная длина имени пользователя. */
    public static final int USERNAME_MAX = 32;

    /** Минимальная длина мастер-пароля. */
    public static final int MASTER_PASSWORD_MIN = 6;

    /** Максимальная длина любого текстового поля. */
    public static final int FIELD_MAX = 200;

    /** Допустимые символы имени пользователя: латиница, цифры, точка, дефис, подчёркивание. */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9._-]+");

    /** Закрытый конструктор: класс содержит только статические методы. */
    private InputValidator() {
    }

    /**
     * Проверяет данные регистрации.
     *
     * @param username     имя пользователя (уже без пробелов по краям)
     * @param password     мастер-пароль
     * @param confirmation повтор мастер-пароля
     * @throws AppException если данные некорректны или пароли не совпадают
     */
    public static void validateRegistration(String username, String password, String confirmation)
            throws AppException {
        validateUsername(username);
        requireLength(password, "Мастер-пароль", MASTER_PASSWORD_MIN, FIELD_MAX);
        if (!password.equals(confirmation)) {
            throw new AppException("Пароли не совпадают");
        }
    }

    /**
     * Проверяет данные записи перед сохранением.
     *
     * @param data             данные записи (строки уже без пробелов по краям)
     * @param passwordRequired обязателен ли пароль (при изменении пустой пароль означает «не менять»)
     * @throws AppException если данные некорректны
     */
    public static void validateEntry(EntryData data, boolean passwordRequired) throws AppException {
        requireLength(data.title(), "Название", 1, FIELD_MAX);
        requireLength(data.website(), "Сайт", 0, FIELD_MAX);
        requireLength(data.login(), "Логин", 1, FIELD_MAX);
        requireLength(data.password(), "Пароль", passwordRequired ? 1 : 0, FIELD_MAX);
        if (data.storageMode() == null) {
            throw new AppException("Выберите способ защиты пароля");
        }
    }

    /**
     * Убирает пробелы по краям строки; {@code null} превращает в пустую строку.
     *
     * @param value исходная строка
     * @return нормализованная строка
     */
    public static String normalize(String value) {
        return value == null ? "" : value.strip();
    }

    /**
     * Проверяет длину и допустимые символы имени пользователя.
     *
     * @param username имя пользователя
     * @throws AppException если имя некорректно
     */
    private static void validateUsername(String username) throws AppException {
        requireLength(username, "Имя пользователя", USERNAME_MIN, USERNAME_MAX);
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new AppException("Имя пользователя: только латиница, цифры и символы . _ -");
        }
    }

    /**
     * Проверяет, что длина строки лежит в заданных пределах.
     *
     * @param value     проверяемая строка
     * @param fieldName название поля для сообщения об ошибке
     * @param min       минимальная длина
     * @param max       максимальная длина
     * @throws AppException если строка отсутствует или её длина вне пределов
     */
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