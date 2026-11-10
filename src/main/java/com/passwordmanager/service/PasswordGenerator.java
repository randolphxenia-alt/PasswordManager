package com.passwordmanager.service;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PasswordGenerator {
    public static final int DEFAULT_LENGTH = 16;
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    private static final List<String> CHARACTER_GROUPS = List.of(
            "abcdefghijkmnopqrstuvwxyz",
            "ABCDEFGHJKLMNPQRSTUVWXYZ",
            "23456789",
            "!@#$%^&*-_=+?");
    private static final String ALPHABET = String.join("", CHARACTER_GROUPS);
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordGenerator() {
    }
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }
    public static String generate(int length) {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException("Длина пароля должна быть от " + MIN_LENGTH + " до " + MAX_LENGTH);
        }
        return Stream.generate(() -> randomString(length))
                .filter(PasswordGenerator::containsAllGroups)
                .findFirst()
                .orElseThrow();
    }
    static boolean containsAllGroups(String password) {
        return CHARACTER_GROUPS.stream()
                .allMatch(group -> password.chars().anyMatch(symbol -> group.indexOf(symbol) >= 0));
    }
    private static String randomString(int length) {
        return RANDOM.ints(length, 0, ALPHABET.length())
                .mapToObj(index -> String.valueOf(ALPHABET.charAt(index)))
                .collect(Collectors.joining());
    }
}