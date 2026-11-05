package com.passwordmanager.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

/**
 * Защищённое хранилище секретных ключей приложения.
 * <p>
 * Ключи известны только приложению: они не вводятся пользователем, не хранятся в БД
 * и не лежат в коде открытым текстом. Каждый ключ зашифрован алгоритмом
 * AES-128-GCM; ключ хранилища вычисляется как SHA-256 от внутреннего «зерна».
 * Режим GCM дополнительно проверяет целостность: изменённый ключ не расшифруется.
 * Ключи расшифровываются только в памяти и только в момент использования.
 */
public final class KeyVault {

    /** Зашифрованный ключ шифра Фейстеля: Base64(IV || AES-GCM(ключ)). */
    private static final String SEALED_FEISTEL_KEY =
            "/LdrayUXLroAWV26IY9IiPHUKzLo8iZgQA9pX8mwHHd93b7K5x/JClswp7pdqA9uEQ==";

    /** Зашифрованный «перец» для хешей с солью: Base64(IV || AES-GCM(перец)). */
    private static final String SEALED_SALT_PEPPER =
            "RjTFheUw+Y5OmxmWbz0MDSvGryhIZlMk+2gWHZhowtZDHFS6pp0rQYuAgtLvxIo=";

    /** Зерно, из которого вычисляется ключ хранилища. */
    private static final byte[] VAULT_SEED = {
            (byte) 0x3A, (byte) 0x91, (byte) 0x5C, (byte) 0xE7, (byte) 0x12, (byte) 0xB4,
            (byte) 0x6F, (byte) 0xD8, (byte) 0x29, (byte) 0x47, (byte) 0xAC, (byte) 0x03,
            (byte) 0xF1, (byte) 0x8E, (byte) 0x55, (byte) 0xC6};

    /** Алгоритм шифрования хранилища. */
    private static final String CIPHER = "AES/GCM/NoPadding";

    /** Длина вектора инициализации GCM в байтах. */
    private static final int IV_LENGTH = 12;

    /** Длина тега аутентификации GCM в битах. */
    private static final int TAG_LENGTH = 128;

    /** Длина ключа AES в байтах. */
    private static final int AES_KEY_LENGTH = 16;

    /** Закрытый конструктор: класс содержит только статические методы. */
    private KeyVault() {
    }

    /**
     * Возвращает ключ шифра Фейстеля.
     *
     * @return ключ в открытом виде
     * @throws CryptoException если хранилище повреждено
     */
    public static String feistelKey() {
        return unseal(SEALED_FEISTEL_KEY);
    }

    /**
     * Возвращает «перец» — секретную добавку к хешам с солью.
     *
     * @return перец в открытом виде
     * @throws CryptoException если хранилище повреждено
     */
    public static String saltPepper() {
        return unseal(SEALED_SALT_PEPPER);
    }

    /**
     * Расшифровывает значение из хранилища.
     *
     * @param sealed значение в формате Base64(IV || шифртекст с тегом)
     * @return расшифрованная строка
     * @throws CryptoException если значение повреждено или изменено
     */
    private static String unseal(String sealed) {
        try {
            byte[] payload = Base64.getDecoder().decode(sealed);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, vaultKey(), new GCMParameterSpec(TAG_LENGTH, payload, 0, IV_LENGTH));
            byte[] plain = cipher.doFinal(payload, IV_LENGTH, payload.length - IV_LENGTH);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new CryptoException("Хранилище ключей повреждено", e);
        }
    }

    /**
     * Вычисляет ключ хранилища: первые 16 байт SHA-256 от зерна.
     *
     * @return ключ AES
     * @throws GeneralSecurityException если SHA-256 недоступен
     */
    private static SecretKeySpec vaultKey() throws GeneralSecurityException {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(VAULT_SEED);
        return new SecretKeySpec(Arrays.copyOf(digest, AES_KEY_LENGTH), "AES");
    }
}