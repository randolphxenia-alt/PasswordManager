package com.passwordmanager.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * «Подсаливание» паролей: {@code SHA-256(соль || пароль || перец)}.
 * <ul>
 *     <li><b>соль</b> — случайные 16 байт, свои для каждого пароля; хранится рядом с хешем,
 *     поэтому одинаковые пароли дают разные хеши и радужные таблицы бесполезны;</li>
 *     <li><b>перец</b> — секретный ключ приложения из {@link KeyVault}; в БД не хранится,
 *     поэтому без приложения хеш нельзя даже начать подбирать.</li>
 * </ul>
 * Результат хранится строкой {@code соль:хеш}.
 */
public final class SaltedHashService {

    /** Имя алгоритма хеширования. */
    private static final String ALGORITHM = "SHA-256";

    /** Закрытый конструктор: класс содержит только статические методы. */
    private SaltedHashService() {
    }

    /**
     * Хеширует текст с новой случайной солью.
     *
     * @param text   открытый текст
     * @param pepper секретный ключ приложения («перец»)
     * @return строка {@code соль:хеш}
     * @throws CryptoException если алгоритм недоступен
     */
    public static String hash(String text, String pepper) {
        byte[] salt = SaltedValue.randomSalt();
        return new SaltedValue(salt, digest(salt, text, pepper)).format();
    }

    /**
     * Проверяет, соответствует ли текст сохранённому хешу.
     *
     * @param text   проверяемый текст
     * @param stored сохранённая строка {@code соль:хеш}
     * @param pepper секретный ключ приложения («перец»)
     * @return {@code true}, если текст совпадает
     * @throws CryptoException если сохранённая строка повреждена
     */
    public static boolean verify(String text, String stored, String pepper) {
        SaltedValue expected = SaltedValue.parse(stored);
        return MessageDigest.isEqual(digest(expected.salt(), text, pepper), expected.hash());
    }

    /**
     * Вычисляет {@code SHA-256(соль || текст || перец)}.
     *
     * @param salt   соль
     * @param text   текст
     * @param pepper перец
     * @return байты хеша
     * @throws CryptoException если алгоритм недоступен
     */
    private static byte[] digest(byte[] salt, String text, String pepper) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            digest.update(text.getBytes(StandardCharsets.UTF_8));
            digest.update(pepper.getBytes(StandardCharsets.UTF_8));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Алгоритм SHA-256 недоступен", e);
        }
    }
}