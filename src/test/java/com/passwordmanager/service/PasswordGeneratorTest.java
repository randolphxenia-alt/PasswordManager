package com.passwordmanager.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Тесты генератора паролей. */
class PasswordGeneratorTest {

    /** Пароль имеет нужную длину и содержит все группы символов. */
    @Test
    @DisplayName("Пароль нужной длины со всеми группами символов")
    void generatedPasswordIsStrong() {
        String password = PasswordGenerator.generate(12);
        assertEquals(12, password.length());
        assertTrue(PasswordGenerator.containsAllGroups(password));
    }

    /** Пароли не повторяются. */
    @Test
    @DisplayName("100 сгенерированных паролей не повторяются")
    void passwordsAreUnique() {
        Set<String> passwords = IntStream.range(0, 100)
                .mapToObj(i -> PasswordGenerator.generate())
                .collect(Collectors.toSet());
        assertEquals(100, passwords.size());
    }

    /** Недопустимая длина отклоняется. */
    @Test
    @DisplayName("Слишком короткий или длинный пароль → ошибка")
    void invalidLength() {
        assertThrows(IllegalArgumentException.class, () -> PasswordGenerator.generate(4));
        assertThrows(IllegalArgumentException.class, () -> PasswordGenerator.generate(100));
    }
}