package com.passwordmanager.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

/**
 * Хеширование мастер-паролей пользователей алгоритмом PBKDF2-HMAC-SHA256
 * со случайной солью (параметры соответствуют рекомендациям OWASP).
 * <p>
 * Результат хранится одной строкой формата {@code соль:хеш}.
 */
public final class PasswordHasher {

    /** Имя алгоритма в Java Cryptography Architecture. */
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

    /** Количество итераций PBKDF2: замедляет подбор пароля. */
    private static final int ITERATIONS = 210_000;

    /** Длина получаемого ключа в битах. */
    private static final int KEY_LENGTH = 256;

    /** Закрытый конструктор: класс содержит только статические методы. */
    private PasswordHasher() {
    }

    /**
     * Хеширует пароль с новой случайной солью.
     *
     * @param password пароль в открытом виде
     * @return строка формата {@code соль:хеш}
     * @throws CryptoException если алгоритм недоступен
     */
    public static String hash(String password) {
        byte[] salt = SaltedValue.randomSalt();
        return new SaltedValue(salt, derive(password, salt)).format();
    }

    /**
     * Проверяет пароль по сохранённой строке {@code соль:хеш}.
     * Сравнение выполняется за постоянное время.
     *
     * @param password пароль в открытом виде
     * @param stored   сохранённая строка {@code соль:хеш}
     * @return {@code true}, если пароль верный
     * @throws CryptoException если сохранённая строка повреждена или алгоритм недоступен
     */
    public static boolean verify(String password, String stored) {
        SaltedValue expected = SaltedValue.parse(stored);
        return MessageDigest.isEqual(derive(password, expected.salt()), expected.hash());
    }

    /**
     * Вычисляет PBKDF2-хеш пароля с заданной солью.
     *
     * @param password пароль
     * @param salt     соль
     * @return байты хеша
     * @throws CryptoException если алгоритм недоступен
     */
    private static byte[] derive(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new CryptoException("Не удалось вычислить PBKDF2-хеш", e);
        } finally {
            spec.clearPassword();
        }
    }
}