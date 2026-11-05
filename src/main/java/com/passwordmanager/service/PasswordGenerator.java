package com.passwordmanager.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Генератор надёжных случайных паролей.
 * Пароль всегда содержит строчные и заглавные буквы, цифры и спецсимволы.
 */
public final class PasswordGenerator {

    /** Длина пароля по умолчанию. */
    public static final int DEFAULT_LENGTH = 16;

    /** Минимальная допустимая длина пароля. */
    public static final int MIN_LENGTH = 8;

    /** Максимальная допустимая длина пароля. */
    public static final int MAX_LENGTH = 64;

    /** Группы символов, каждая из которых обязательно присутствует в пароле. */
    private static final List<String> CHARACTER_GROUPS = List.of(
            "abcdefghijkmnopqrstuvwxyz",
            "ABCDEFGHJKLMNPQRSTUVWXYZ",
            "23456789",
            "!@#$%^&*-_=+?");

    /** Все допустимые символы. */
    private static final String ALPHABET = String.join("", CHARACTER_GROUPS);

    /** Криптографически стойкий генератор случайных чисел. */
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Закрытый конструктор: класс содержит только статические методы. */
    private PasswordGenerator() {
    }

    /**
     * Генерирует пароль длины {@link #DEFAULT_LENGTH}.
     *
     * @return новый пароль
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Генерирует пароль заданной длины.
     *
     * @param length длина пароля ({@link #MIN_LENGTH}..{@link #MAX_LENGTH})
     * @return новый пароль
     * @throws IllegalArgumentException если длина вне допустимых пределов
     */
    public static String generate(int length) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Длина пароля должна быть от " + MIN_LENGTH + " до " + MAX_LENGTH);
        }
        return Stream.generate(() -> randomString(length))
                .filter(PasswordGenerator::containsAllGroups)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Проверяет, что пароль содержит символы из каждой группы.
     *
     * @param password пароль
     * @return {@code true}, если все группы представлены
     */
    static boolean containsAllGroups(String password) {
        return CHARACTER_GROUPS.stream()
                .allMatch(group -> password.chars().anyMatch(symbol -> group.indexOf(symbol) >= 0));
    }

    /**
     * Составляет строку из случайных символов алфавита.
     *
     * @param length длина строки
     * @return случайная строка
     */
    private static String randomString(int length) {
        return RANDOM.ints(length, 0, ALPHABET.length())
                .mapToObj(index -> String.valueOf(ALPHABET.charAt(index)))
                .collect(Collectors.joining());
    }
}